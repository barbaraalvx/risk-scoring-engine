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
