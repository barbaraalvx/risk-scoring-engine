# Event Ingestion Service (`event-ingestion-service`)

O **Event Ingestion Service** é o microsserviço responsável pela porta de entrada do sistema distribuído de antifraude. Ele recebe ações brutas dos jogadores via API REST, valida os dados de entrada, grava o evento de forma imutável no **Event Store** (PostgreSQL) e o publica no broker **Kafka** para processamento assíncrono downstream.

---

## 🚀 Contrato da API REST

### 1. Ingestão de Ações do Jogador

- **Método:** `POST`
- **Path:** `/api/v1/actions` (ou `/events` para retrocompatibilidade)
- **Headers:** `Content-Type: application/json`

#### Request Body
```json
{
  "eventId": "optional-uuid-string",
  "playerId": "player-123",
  "eventType": "BET | LOGIN | MOVE | ACCOUNT_LINK | DEVICE_FINGERPRINT | WITHDRAWAL | DEPOSIT",
  "timestamp": "2026-07-25T23:00:00Z",
  "sessionId": "sess-abc-123",
  "deviceFingerprint": "fp-device-999",
  "ipAddress": "192.168.1.100",
  "payload": {
    "amount": 500.0,
    "currency": "BRL"
  }
}
```

#### Response Body (Sucesso — `202 Accepted`)
```json
{
  "eventId": "generated-or-kept-uuid"
}
```

#### Response Body (Erro — `400 Bad Request`)
```json
{
  "timestamp": "2026-07-25T23:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Payload invalido.",
  "details": [
    {
      "field": "playerId",
      "message": "playerId é obrigatório",
      "rejectedValue": ""
    }
  ]
}
```

---

### 2. Operational & Health Check

- **Método:** `GET`
- **Path:** `/actuator/health`
- **Response:** `200 OK` → `{"status": "UP"}`

---

## 🔄 Estratégia de Idempotência e Concorrência

1. **Geração de Event ID:** Caso o cliente envie a requisição sem o campo `eventId`, o serviço gera automaticamente um UUID v4. Se o `eventId` for fornecido, ele é preservado.
2. **Re-envio / Duplicação:** Se um cliente re-enviar uma mensagem com o mesmo `eventId`, o `event-ingestion-service` aceitará a requisição (`202 Accepted`) e a republicará no Kafka. A deduplicação estrita de eventos fica a cargo dos consumidores downstream (CQRS / Scoring).
3. **Particionamento Garantido:** Todas as mensagens publicadas no tópico Kafka `player-actions` utilizam o **`playerId` como Partition Key**. Isso garante que todos os eventos do mesmo jogador caiam na mesma partição e sejam processados em ordem cronológica estrita.
