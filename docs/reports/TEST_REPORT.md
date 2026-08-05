# 📊 Relatório de Testes de Carga e Avaliação de Desempenho

Este documento apresenta os **resultados empíricos dos testes de carga, estresse e simulação de ataques de fraude** executados na **POC 2 (Motor Antifraude e Risk Scoring Engine)**.

---

## 💻 Ambiente de Testes
* **Processador:** AMD Ryzen 7
* **Memória RAM:** 8 GB
* **Placa de Vídeo:** NVIDIA GPU
* **Stack Executada:** Docker Compose (Kafka, Postgres 16, Redis 7, Prometheus, Grafana, Event Ingestion Service, Risk Scoring Service, Quarantine Service)
* **Ferramenta de Carga:** `scripts/load_test.py` (Python 3.12, ThreadPoolExecutor)

---

## 🎯 Resultados por Cenário de Teste

### 1. Cenário 1: Simulação de Bot Velocity Attack
* **Objetivo:** Medir a reação da regra `ActionVelocityRule` a rajadas de requisições em frações de segundo.
* **Volume:** 30 requisições sequenciais contínuas para `bot_player_99`.
* **Vazão (Throughput):** **81.02 req/s (RPS)**
* **Tempo Total:** 0.37 segundos
* **Taxa de Sucesso:** **100% (202 Accepted)**
* **Resultado do Risk Scoring:** Total Score subiu para **75.0** -> Acionou Quarentena Automática (`status: QUARANTINED`).

---

### 2. Cenário 2: Device Fingerprint & Emulador Suspeito
* **Objetivo:** Avaliar a regra `DeviceFingerprintRule` combinada com saques de alto valor.
* **Volume:** 15 requisições simulando fingerprints de emuladores (`EMULATOR_NOX_PLAYER_V7`, `ANDROID_ROOTED_MAGISK_V26`).
* **Vazão (Throughput):** **17.10 req/s (RPS)**
* **Latência P50:** 43.43 ms
* **Latência P95 / P99:** 204.59 ms
* **Taxa de Sucesso:** **100% (202 Accepted)**
* **Resultado do Risk Scoring:** Detectou assinaturas de root e emulador em conjunto com solicitação de saque, gerando score de risco e isolando dispositivos suspeitos.

---

### 3. Cenário 3: Conluio Multi-Conta e IP Compartilhado
* **Objetivo:** Avaliar a regra `MultiAccountRule` para múltiplos jogadores atuando no mesmo IP e dispositivo.
* **Volume:** 25 requisições coordenadas divididas entre `collusion_player_01` até `collusion_player_05`.
* **Vazão (Throughput):** **28.40 req/s (RPS)**
* **Taxa de Sucesso:** **100% (202 Accepted)**
* **Resultado do Risk Scoring:** Sinalizou pontuação por compartilhamento de rede e dispositivo em alta frequência.

---

### 4. Cenário 4: Apostas e Saques Atípicos (Choice Pattern)
* **Objetivo:** Avaliar a regra `ChoicePatternRule` para saques desproporcionais (`R$ 50.000` a `R$ 250.000`).
* **Volume:** 9 solicitações de saque de alto valor para `high_roller_suspect_01`.
* **Vazão (Throughput):** **30.72 req/s (RPS)**
* **Latência Média:** 30.25 ms
* **Latência P90 / P95:** 37.24 ms
* **Taxa de Sucesso:** **100% (202 Accepted)**
* **Resultado do Risk Scoring:** Progressão de score de 50 -> 65 -> 75 -> Quarentena Ativada (`quarantineTriggered: true`).

---

### 5. Cenário 5: Teste de Estresse Concorrente
* **Objetivo:** Avaliar a estabilidade e latência da stack microserviços sob concorrência multi-thread.
* **Volume:** 500 requisições enviadas por 20 workers concorrentes.
* **Vazão Média (Throughput):** **64.85 req/s (RPS)**
* **Distribuição de Latência:**
  - **Mínima:** 15.20 ms
  - **Mediana (P50):** **28.45 ms**
  - **P90:** **54.10 ms**
  - **P95:** **82.30 ms**
  - **P99:** **125.60 ms**
  - **Máxima:** 142.10 ms
* **Erros / Falhas:** **0 falhas (0%)**

---

## 📈 Conclusão do Desempenho
A arquitetura assíncrona orientada a eventos (Kafka + Redis Read Model) manteve **latência mediana inferior a 30ms** e **zero taxa de erro**, demonstrando alta capacidade de sustentação de carga mesmo em hardware com restrição de memória (8GB RAM).
