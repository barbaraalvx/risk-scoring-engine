# ADR 003: Separação de Leitura e Escrita no Painel Administrativo (CQRS)

## Status
**Aceito** (Atualizado a partir da Documentação Inicial do Projeto 02)

## Contexto
O painel administrativo (`admin-dashboard.html`) precisa exibir em tempo real a lista de jogadores suspeitos, sinais recentes e status de quarentena, ao mesmo tempo em que o sistema continua processando requisições em alta velocidade no pipeline de ingestão e scoring. 

Se as consultas pesadas de monitoramento administrativo utilizarem o mesmo caminho de escrita e o mesmo banco relacional do motor de risco, a competição por recursos degradará o desempenho do sistema em tempo real.

## Decisão
Adotamos o padrão **CQRS (Command Query Responsibility Segregation)**:
- **Write Path (Comandos/Escrita):** Recebimento de eventos brutos de jogo, cálculo de regras de scoring e persistência de histórico auditável no PostgreSQL.
- **Read Path (Consultas/Leitura):** Consultas ultrarrápidas do painel admin (`GET /api/v1/admin/dashboard` e `GET /quarantine/{playerId}`) lendo diretamente da projeção em **Redis**, atualizada assincronamente a partir dos eventos.

## Consequências

### Positivas
- **Escalabilidade Independente:** As consultas do painel admin não causam contenção nem lentidão no pipeline de ingestão e cálculo de risco.
- **Otimização de Leitura:** O Redis fornece tempos de resposta inferiores a 2ms para as visões do painel administrativo.

### Negativas / Riscos
- **Consistência Eventual:** Pequeno atraso de milissegundos na propagação entre a escrita no Kafka/Postgres e a atualização da projeção em Redis.
