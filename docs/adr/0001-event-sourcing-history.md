# ADR 001: Armazenamento do Histórico de Eventos de Suspeita (Event Sourcing)

## Status
**Aceito** (Atualizado a partir da Documentação Inicial do Projeto 02)

## Contexto
O sistema de antifraude precisa registrar cada ação suspeita de um jogador (velocidade de resposta, múltiplos acessos, padrão de escolhas, fingerprint de dispositivo) para calcular a pontuação de risco multifatorial e permitir auditoria posterior. 

Existem duas abordagens principais consideradas:
1. Armazenar apenas o estado atual de cada jogador (pontuação de risco + flags ativas).
2. Armazenar cada evento individualmente em ordem cronológica num log imutável e derivar o estado a partir deles.

## Decisão
Adotamos o padrão **Event Sourcing**. Cada ação suspeita é registrada como um evento imutável contendo `timestamp`, `playerId`, `eventType`, `deviceFingerprint`, `ipAddress` e dados contextuais no Apache Kafka e PostgreSQL. O estado de risco do jogador é derivado pela leitura sequencial desses eventos.

## Consequências

### Positivas
- **Rastreabilidade Absoluta:** Fornece um histórico imutável completo de todas as ações dos usuários, facilitando investigações complexas de fraude e auditoria.
- **Simulação de Cenários:** Permite reproduzir (*replay*) eventos passados para calibrar novas regras de detecção de bots ou conluios coordenados sem afetar o ambiente de produção.
- **Performance de Escrita:** Como os eventos são apenas inseridos (*append-only*), a gravação das ações no log é extremamente rápida.

### Negativas / Riscos
- **Volume de Dados:** Maior volume de dados armazenados no PostgreSQL/Kafka.
- **Evolução do Schema:** Alterar a estrutura de eventos antigos exige estratégia de versionamento de schemas de eventos.
