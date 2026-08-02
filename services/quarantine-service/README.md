# Quarantine Service (`quarantine-service`)

O **Quarantine Service** é o microsserviço responsável por agir sobre o risco calculado pelo `risk-scoring-service`. Ele consome os scores de risco publicados via **Kafka**, executa uma **SAGA** para bloquear (colocar em quarentena) jogadores que ultrapassam o limite de risco no backend do jogo, e expõe uma **API de consulta CQRS** — combinando um read model rápido em **Redis** com uma trilha de auditoria completa em **PostgreSQL** — para que um futuro painel administrativo acompanhe o estado de cada quarentena.

---

## 🚀 Contrato da API REST (CQRS — Query Side)

### 1. Estado Atual da Quarentena

- **Método:** `GET`
- **Path:** `/quarantine/{playerId}`
- **Fonte de dados:** read model em Redis (hot path — nunca consulta o Postgres).

#### Response Body (Sucesso — `200 OK`)
```json
{
  "playerId": "player-123",
  "status": "QUARANTINED",
  "reason": "TotalScore acima do threshold.",
  "totalScore": 87,
  "updatedAt": "2026-08-02T23:00:00Z"
}
```

#### Response Body (Erro — `404 Not Found`)
Retornado quando não existe projeção em Redis para o `playerId` informado (nenhum evento de score cruzou o threshold para esse jogador ainda).

---

### 2. Histórico de Quarentenas

- **Método:** `GET`
- **Path:** `/quarantine/{playerId}/history`
- **Fonte de dados:** Postgres, via `QuarantineRepository` (trilha de auditoria do write model).
- **Paginação:** parâmetros padrão do Spring `Pageable` (`page`, `size`, `sort`) via query string.

#### Response Body (Sucesso — `200 OK`)
```json
[
  {
    "id": "b6e2a1f0-9c3a-4e7a-9b2e-1f0a2c3d4e5f",
    "playerId": "player-123",
    "eventId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "totalScore": 87,
    "status": "QUARANTINED",
    "reason": "TotalScore acima do threshold.",
    "createdAt": "2026-08-02T22:59:58Z",
    "resolvedAt": "2026-08-02T23:00:00Z"
  }
]
```

---

### 3. Operational & Health Check

- **Método:** `GET`
- **Path:** `/health`
- **Response:** `200 OK` → `quarantine-service OK` (texto plano)

Também expõe os endpoints padrão do Actuator em `/actuator/health`, `/actuator/info` e `/actuator/metrics`.

---

## 📥 Contrato de Consumo Kafka

### Evento de Entrada — `ScoreUpdatedEvent`

- **Tópico:** `risk-score-updated` (configurável via `quarantine.topics.input-events`)
- **Publicado por:** `risk-scoring-service`
- **Consumer group:** `quarantine-service`

```json
{
  "eventId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "playerId": "player-123",
  "totalScore": 87,
  "quarantineThreshold": 80.0,
  "calculatedAt": "2026-08-02T22:59:58Z"
}
```

### Retry e Dead-Letter Topic

O listener (`ScoreUpdatedConsumer`) é decorado com `@RetryableTopic`, garantindo que uma mensagem malformada ("poison pill") não trave a partição:

- **Tentativas:** 3
- **Backoff:** exponencial, iniciando em 1s, multiplicador 2.0
- **Tópicos de retry/DLT:** criados automaticamente pelo Spring Kafka
- **Esgotadas as tentativas:** a mensagem é roteada ao dead-letter topic e tratada por `@DltHandler`, que apenas registra o ocorrido em log para investigação manual — não há remediação automática.

### Evento de Saída — `QuarantineUpdatedEvent`

- **Tópico:** `quarantine-updated` (configurável via `quarantine.topics.output-scores`)
- **Publicado quando:** a SAGA atinge um estado terminal (`QUARANTINED` ou `COMPENSATED`)
- **Consumido por:** nenhum serviço deste repositório atualmente — destinado a um futuro painel administrativo.

```json
{
  "playerId": "player-123",
  "eventId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "status": "QUARANTINED",
  "reason": "TotalScore acima do threshold.",
  "timestamp": "2026-08-02T23:00:00Z"
}
```

---

## 🔄 Fluxo da SAGA de Quarentena

O `QuarantineOrchestrator` executa o seguinte fluxo para cada `ScoreUpdatedEvent` recebido:

1. **Idempotência:** verifica via `eventId` se o evento já foi processado (`repository.existsByEventId`). Se sim, a mensagem é descartada sem efeito colateral.
2. **Avaliação do threshold:** se `totalScore < quarantineThreshold`, o evento é ignorado — o jogador não entra em quarentena.
3. **Registro em `PENDING`:** um novo `QuarantineRecord` é persistido no Postgres com status `PENDING` e imediatamente projetado no read model do Redis.
4. **Bloqueio no backend do jogo:** `GameBackendClient.blockPlayer` é chamado, protegido por um **circuit breaker Resilience4j** (`game-backend`: janela por contagem de 10 chamadas, mínimo de 5 chamadas para calcular a taxa, limiar de falha de 50%, 10s em estado aberto antes de permitir sondagem em half-open).
   - **Sucesso:** o registro é atualizado para `QUARANTINED`, reprojetado no Redis e o `QuarantineUpdatedEvent` é publicado no tópico de saída.
   - **Falha** (exceção do circuito ou do backend): a SAGA aciona `handleFailure` — o registro é marcado `FAILED`, reprojetado, e a **compensação** é executada.
5. **Compensação:** tenta desbloquear o jogador (`unblockPlayer`) em caráter de melhor esforço; se essa chamada também falhar, a falha é apenas registrada em log e **não interrompe a SAGA**. Em seguida o registro é marcado `COMPENSATED`, reprojetado no Redis e o `QuarantineUpdatedEvent` terminal é publicado.

Nenhum registro permanece preso em `PENDING`: todo caminho após o passo 3 termina em `QUARANTINED` ou em `FAILED` → `COMPENSATED`.

---

## ⚠️ Notas Operacionais

1. **Cliente do backend do jogo simulado:** `GameBackendClient.blockPlayer`/`unblockPlayer` atualmente apenas registram a ação em log — não há chamada HTTP/gRPC real implementada ainda. A integração com o circuit breaker já está pronta para quando o cliente real for implementado.
2. **Desserialização Kafka restrita:** `spring.json.trusted.packages` está limitado a `com.antifraude.quarantine.event` (endurecido pela correção `2e1ce79`, "restringe pacotes confiaveis na desserializacao Kafka"). Esse limite é uma decisão deliberada de segurança e não deve ser ampliado sem revisão.
3. **CQRS com fontes distintas:** a consulta de estado atual (`GET /quarantine/{playerId}`) nunca toca o Postgres — depende inteiramente da projeção em Redis, que é atualizada a cada transição de status da SAGA. Já o histórico (`GET /quarantine/{playerId}/history`) lê diretamente do Postgres, a fonte de verdade auditável.
4. **Schema gerenciado por Flyway:** a aplicação usa `ddl-auto: validate` — alterações de schema devem passar por migrações Flyway (histórico registrado na tabela `flyway_schema_history_quarantine`), nunca por auto-geração do Hibernate.
