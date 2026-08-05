# ADR 004: Stack Tecnológica do Projeto

## Status
**Aceito** (Atualizado a partir da Documentação Inicial do Projeto 02)

## Contexto
A POC 2 exige suporte aos padrões Event Sourcing, CQRS, SAGA Orquestrada, Feature Flags e observabilidade. A equipe avaliou duas opções principais de stack tecnológica para equilibrar a maturidade do ecossistema e a velocidade de desenvolvimento dentro do prazo da disciplina.

## Opções Consideradas

### Opção A: Python (FastAPI + Celery + SQLAlchemy)
- Familiaridade da equipe com a linguagem.
- Ecossistema menos consolidado para padrões avançados de microsserviços distribuídos em comparação ao ecossistema Java.

### Opção B: Java 21 + Spring Boot 3.3 (Escolhida)
- Ecossistema extremamente maduro para microsserviços e padrões de sistemas distribuídos.
- Suporte a Spring Kafka para mensageria e Event Sourcing, Resilience4j para resiliência e Spring Data para CQRS.
- Orientação a Objetos clara facilitando a divisão de tarefas entre a equipe.

## Decisão
Adotamos **Java 21 + Spring Boot 3.3** como stack principal do backend com a seguinte infraestrutura:
- **Apache Kafka:** Broker de eventos para log ordenado e particionado (*Event Sourcing*).
- **PostgreSQL 16:** Banco de escrita (*Write Model*) para persistência transacional e histórico auditável de quarentenas.
- **Redis 7:** Banco de leitura (*Read Model CQRS*) e armazenamento de Feature Flags e pesos de scoring em memória.
- **Docker + Docker Compose:** Conteinerização portável de todos os microsserviços e dependências.
- **Prometheus + Grafana:** Coleta de métricas e observabilidade dos microsserviços.

## Consequências

### Positivas
- **Suporte Nativo a Padrões:** Todos os padrões arquiteturais exigidos possuem bibliotecas maduras e documentadas no ecossistema Spring.
- **Portabilidade:** Onboarding simples com `docker compose up --build`.

### Negativas / Riscos
- **Verbosidade:** Maior tempo de setup inicial dos microsserviços em relação a Python.
