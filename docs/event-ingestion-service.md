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

## Task 3 - Endpoint de ingestao `POST /events`

Esta task adiciona a porta de entrada de eventos do sistema. O endpoint recebe um `GameEvent` em JSON, valida o payload, gera `eventId` quando ausente e publica no Kafka.

### Implementacao

- Foi criado `EventIngestionController.java` em `src/main/java/com/antifraude/ingestion` com `POST /events`.
- O endpoint recebe `@Valid @RequestBody GameEvent`, aplicando Bean Validation automaticamente.
- Quando o `eventId` nao vem no payload, o servidor gera um UUID com `gameEvent.withGeneratedEventId()`.
- Foi criado `EventPublisher.java` para rotear o evento para o Kafka usando `KafkaTemplate<String, String>`.
- O evento e serializado em JSON com `ObjectMapper` e publicado no topico configuravel `ingestion.events-topic`.
- Foi adicionado `ingestion.events-topic: antifraude.game-events` em `application.yml`.
- Foi adicionado `ApiExceptionHandler.java` com tratamento global de erros de entrada:
  - `MethodArgumentNotValidException` retorna `400 Bad Request` com lista de campos invalidos.
  - `HttpMessageNotReadableException` retorna `400 Bad Request` para JSON malformado.
- Foram criados os DTOs `EventAcceptedResponse` (sucesso) e `ApiErrorResponse`/`ValidationErrorDetail` (erro).

### Comportamento da API

- Sucesso:
  - Retorna `202 Accepted` com o `eventId` gerado/normalizado.
- Payload invalido:
  - Retorna `400 Bad Request` com detalhes em `details` (campo, mensagem e valor rejeitado).

### Garantia de arquitetura stateless

- O `event-ingestion-service` nao persiste estado de negocio localmente.
- O fluxo implementado e: receber -> validar -> gerar `eventId` (se necessario) -> publicar no Kafka.
- Nenhum repositório, cache de sessao ou escrita direta em banco foi adicionado nesta task.
- Esse desenho mantem o servico escalavel horizontalmente, com instancias fungiveis atras de balanceador.

### Como testar

1. Suba a stack:

```bash
docker compose up
```

2. Envie um evento valido (sem `eventId`):

```bash
curl -i -X POST http://localhost:8083/events \
  -H "Content-Type: application/json" \
  -d '{
    "playerId": "player-123",
    "eventType": "BET",
    "timestamp": "2026-07-20T12:30:00Z",
    "payload": {
      "amount": 150.75,
      "currency": "BRL"
    }
  }'
```

3. Valide retorno de erro com payload invalido:

```bash
curl -i -X POST http://localhost:8083/events \
  -H "Content-Type: application/json" \
  -d '{
    "playerId": "",
    "eventType": "BET"
  }'
```
