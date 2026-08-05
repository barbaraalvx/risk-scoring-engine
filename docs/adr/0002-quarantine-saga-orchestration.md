# ADR 002: Mecanismo de Acionamento da Quarentena Automática (SAGA Orchestration)

## Status
**Aceito** (Atualizado a partir da Documentação Inicial do Projeto 02)

## Contexto
Quando a pontuação de risco de um jogador ultrapassa o *threshold* configurado (ex: >= 70.0), o sistema precisa acionar o fluxo de quarentena automaticamente. 

Duas abordagens foram avaliadas:
1. **Choreography:** Cada microsserviço reage de forma independente a eventos de quarentena publicados no Kafka, sem um coordenador central.
2. **Orchestration:** Um serviço orquestrador centralizado coordena a sequência de ações (persistir quarentena, bloquear jogador no Game Backend, atualizar visões e notificar o painel admin).

## Decisão
Adotamos o padrão **Orchestration via SAGA** gerenciado pelo `quarantine-service` (`QuarantineOrchestrator`). 

O serviço orquestrador consome o evento de pontuação atualizada, verifica se o *threshold* foi violado e executa sequencialmente:
1. Persistência do registro de quarentena no PostgreSQL (`PENDING`).
2. Chamada à API de bloqueio do Game Backend (`POST /api/v1/players/{id}/block`).
3. Transição para o estado `QUARANTINED` e atualização da projeção de leitura em Redis.
4. Caso ocorra falha em qualquer etapa, o orquestrador aciona ações de compensação (*rollback* explícito para `COMPENSATED`).

## Consequências

### Positivas
- **Fluxo Centralizado e Controlado:** Torna o fluxo de quarentena mais simples de entender, documentar, auditar e debugar.
- **Resiliência com Rollback Explícito:** Se o bloqueio do jogador falhar no meio do caminho, o sistema reverte o estado automaticamente evitando inconsistências entre bancos de dados.

### Negativas / Riscos
- **Ponto Central de Controle:** O orquestrador gerencia a integridade do processo; exige réplicas e alta disponibilidade na infraestrutura.
