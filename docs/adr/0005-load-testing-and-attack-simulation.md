# ADR 005: Suíte de Testes de Carga e Simulação de Ataques em Python

## Status
**Aceito** (Adicionado no Projeto 03 para validação dos requisitos da POC 2)

## Contexto
A validação prática da POC 2 exige testar o comportamento do motor de risco em tempo real contra 4 vetores de fraude (bots de velocidade, emuladores/root, conluio de IP/multi-conta e saques atípicos) e medir a capacidade de vazão (RPS) e latência sob carga concorrente.

## Decisão
Criamos uma suíte leve e portátil em **Python 3** (`scripts/load_test.py`) utilizando bibliotecas nativas da linguagem (`urllib.request`, `concurrent.futures`, `json`, `time`, `statistics`).

A suíte contempla 5 cenários independentes:
1. **Cenário 1 (Bot Velocity Attack):** Rajada contínua para testar `ActionVelocityRule`.
2. **Cenário 2 (Device Fingerprint Anomaly):** Requisições com assinaturas de emulador e root testando `DeviceFingerprintRule`.
3. **Cenário 3 (Multi-Account Collusion):** Múltiplos jogadores atuando do mesmo IP e dispositivo testando `MultiAccountRule`.
4. **Cenário 4 (Choice Pattern Anomalies):** Saques desproporcionais testando `ChoicePatternRule`.
5. **Cenário 5 (Concurrent Stress Test):** Teste de estresse com cálculo estatístico de vazão e percentis de latência (P50, P90, P95, P99).

## Consequências

### Positivas
- **Portabilidade:** Executa nativamente sem necessidade de instalação de pacotes externos (`pip`).
- **Comprovação Empírica:** Fornece métricas estatísticas detalhadas comprovando mais de 80 RPS e mediana de latência < 30ms.

### Negativas / Riscos
- **Dependência de Stack em Execução:** Requer que os containers Docker da aplicação estejam no ar antes da execução.
