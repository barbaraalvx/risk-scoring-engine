# Risk Scoring Service (`risk-scoring-service`)

O **Risk Scoring Service** é o coração estatístico do sistema antifraude. Ele consome as ações de jogadores emitidas no Kafka (`player-actions`), calcula uma pontuação de risco multifatorial (0 - 100) baseada em regras ponderadas por **Feature Flags**, persiste o histórico de pontuações no banco de escrita (PostgreSQL) e emite eventos de risco atualizado (`risk-score-updated`) para o Orquestrador de Quarentena (SAGA).

---

## 🎯 Regras de Scoring Multifatorial

O motor pondera 4 sub-scores (0 - 25 cada) agregados em um score final (0 - 100):

1. **Device Fingerprint Rule:** Identificação de emuladores, root, ou fingerprints omissos/ausentes.
2. **Action Velocity Rule:** Detecção de bots com base na frequência e volume de ações por janela de tempo no PostgreSQL.
3. **Choice Pattern Rule:** Análise de desvio comportamental e apostas com valores atípicos.
4. **Multi-Account Correlation Rule:** Identificação de logins correlacionados, compartilhamento de IPs suspeitos e vínculos entre contas.

---

## ⚙️ Feature Flags via Redis (Hot-Reload)

Os pesos das regras e o limiar de quarentena (`quarantineThreshold`) são gerenciados dinamicamente via Redis sem necessidade de redeploy da aplicação.

### Consultar Pesos Ativos
- **Método:** `GET`
- **Path:** `/api/v1/flags`

### Atualizar Pesos em Tempo Real (Admin)
- **Método:** `PUT`
- **Path:** `/api/v1/admin/scoring-weights`
- **Body:**
```json
{
  "deviceWeight": 0.30,
  "velocityWeight": 0.30,
  "patternWeight": 0.20,
  "multiAccountWeight": 0.20,
  "quarantineThreshold": 75.0
}
```

---

## 📖 API REST de Consulta de Scores (CQRS Query Side)

### 1. Obter Último Score do Jogador
- **Método:** `GET`
- **Path:** `/api/v1/scores/{playerId}`
- **Response:** `200 OK` com o registro `PlayerScoreRecord` mais recente.

### 2. Obter Histórico Paginado do Jogador
- **Método:** `GET`
- **Path:** `/api/v1/scores/{playerId}/history?limit=20`
- **Response:** `200 OK` com a lista dos últimos N registros.
