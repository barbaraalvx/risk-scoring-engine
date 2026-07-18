# Serviço de Ingestão de Eventos

Este documento descreve o novo módulo `event-ingestion-service`

## Task 1 - Criação do módulo Maven (services/event-ingestion-service)

- Foi criado um módulo Maven Spring Boot em `services/event-ingestion-service`.
- Foi adicionado o `pom.xml` com as dependências:
  - `spring-boot-starter-web`
  - `spring-kafka`
  - `spring-boot-starter-validation`
  - `spring-boot-starter-actuator`
  - `micrometer-registry-prometheus`
- Foi criado `Application.java` em `src/main/java/com/antifraude/ingestion`.
- Foi criado um `HealthController.java` simples que expõe `GET /health` retornando `event-ingestion-service OK`.
- Foi adicionado `src/main/resources/application.yml` configurado para rodar na porta `8083` e expondo os endpoints de gerenciamento `health,info,metrics`.
- Foi adicionado um `Dockerfile` seguindo o mesmo padrão multi-stage usado pelos outros serviços.
- Adicionado event-ingestion-service à matrix em ci.yml:1.
- Registrado event-ingestion-service em docker-compose.yml:1 com depends_on: [kafka] e ports: ["8083:8083"].

## Como testar

1. Inicie a stack:

```bash
docker compose up
```

2. Verifique os endpoints de health:

```bash
curl http://localhost:8083/health
curl http://localhost:8083/actuator/health
```

## Task 2 - Modelo de dados `GameEvent`

A ideia aqui é centralizar a estrutura do evento de entrada em um único tipo de domínio, para que o restante do fluxo trabalhe sempre com o mesmo contrato.

### Implementação

- Criar o tipo `GameEvent` como `record` ou POJO em `src/main/java/com/antifraude/ingestion/model`.
- Criar o enum `GameEventType` com os valores:
  - `BET`
  - `LOGIN`
  - `WITHDRAWAL`
  - `DEPOSIT`
  - `MULTI_ACCOUNT_SUSPECT`
- Validar os campos obrigatórios com Bean Validation.
- Tratar `eventId` como opcional na entrada. Se vier `null`, o servidor gera um `UUID` antes de persistir, publicar ou processar.
- Manter `payload` como `Map<String, Object>` para permitir dados específicos de cada evento sem forçar um schema rígido demais.

### Estrutura sugerida

```java
public record GameEvent(
    UUID eventId,
    @NotBlank String playerId,
    @NotNull GameEventType eventType,
    @NotNull Instant timestamp,
    Map<String, Object> payload
) {
}
```

### Exemplo de uso esperado

```json
{
  "playerId": "player-123",
  "eventType": "BET",
  "timestamp": "2026-07-18T12:30:00Z",
  "payload": {
    "amount": 150.75,
    "currency": "BRL",
    "originIp": "192.168.0.10",
    "deviceFingerprint": "abc123"
  }
}
```

Nesse formato, o `eventId` pode ser omitido na requisição e gerado automaticamente no servidor.
