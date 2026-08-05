# 📊 Guia de Execução: Testes de Carga e Simulação de Cenários Antifraude

Este documento descreve como executar o script de testes de carga e simulação automatizada dos **4 cenários de fraude da POC 2**, bem como coletar métricas para a entrega final da disciplina de **Engenharia de Sistemas Distribuídos (ESD - 2026.1)**.

---

## 🚀 1. Pré-Requisitos

1. Certifique-se de que a stack completa do projeto está rodando via Docker Compose:
   ```bash
   docker compose up --build -d
   ```
2. Verifique se os serviços estão ativos:
   * **Event Ingestion Service:** `http://localhost:8080/actuator/health`
   * **Risk Scoring Service:** `http://localhost:8081/actuator/health`
   * **Quarantine Service:** `http://localhost:8082/health`
   * **Prometheus:** `http://localhost:9090`
   * **Grafana:** `http://localhost:3000`

3. O script foi escrito em **Python 3** nativo (sem necessidade de instalar dependências via `pip`).

---

## 🛠️ 2. Como Executar os Cenários de Teste

Navegue até a raiz do repositório e execute:

### 🔹 Execução Completa (Todos os Cenários + Teste de Estresse)
```bash
python scripts/load_test.py --scenario all
```

---

### 🔹 Execução de Cenários de Fraude Específicos

#### 1. Cenário Bot (Velocidade de Ação — `ActionVelocityRule`)
Dispara rajada contínua de eventos em fração de segundo para o mesmo jogador.
```bash
python scripts/load_test.py --scenario bot
```

#### 2. Cenário Device Suspeito (`DeviceFingerprintRule`)
Dispara eventos com fingerprints de emuladores Android/iOS, dispositivos com root ou omissos.
```bash
python scripts/load_test.py --scenario device
```

#### 3. Cenário Multi-Conta / Conluio (`MultiAccountRule`)
Dispara eventos de 5 jogadores diferentes operando a partir do mesmo endereço IP e mesmo `deviceFingerprint`.
```bash
python scripts/load_test.py --scenario multiaccount
```

#### 4. Cenário Padrão de Escolha Atípico (`ChoicePatternRule`)
Dispara saques/apostas com valores desproporcionais (ex: R$ 250.000,00).
```bash
python scripts/load_test.py --scenario choicepattern
```

---

### 🔹 Execução Apenas do Teste de Estresse (Carga Concorrente)
Executa estresse ajustando o número total de requisições e a concorrência (threads):
```bash
python scripts/load_test.py --scenario stress --requests 1000 --workers 30
```

Parâmetros suportados:
* `--url`: Endpoint alvo (Padrão: `http://localhost:8080/api/v1/actions`)
* `--scenario`: `all`, `bot`, `device`, `multiaccount`, `choicepattern`, `stress`
* `--requests`: Número total de requisições no teste de estresse (Padrão: `500`)
* `--workers`: Número de threads simultâneas (Padrão: `20`)

---

## 📈 3. Coleta de Métricas para o Relatório / Apresentação

Durante ou imediatamente após a execução dos testes, acesse os dashboards de observabilidade:

1. **Prometheus (`http://localhost:9090`):**
   * Verifique a taxa de eventos processados: `http_requests_total`
   * Latência do Spring Actuator: `http_server_requests_seconds_sum`
2. **Grafana (`http://localhost:3000`):**
   * Acompanhe a curva de throughput (RPS), tempo de resposta (P95 e P99) e consumo de CPU/Memória da JVM e do Kafka.
3. **Verificação no Redis (Read Model CQRS):**
   * Consulte o estado imediato de quarentena de um jogador testado:
     ```bash
     curl http://localhost:8082/quarantine/bot_player_99
     ```

---

## 📝 4. Exemplo de Relatório Gerado pelo Script

```text
📊 RELATÓRIO DE MÉTRICAS: Teste de Carga Concorrente (20 Workers)
-----------------------------------------------------------------
 Total de Requisições:   500
 Sucessos (202 Accepted): 500
 Falhas / Erros:         0
 Tempo Total:            1.42 segundos
 Vazão (Throughput):     352.11 req/sec (RPS)
-----------------------------------------------------------------
 Latência Mínima:        8.12 ms
 Latência Média:         15.40 ms
 Latência P50 (Mediana): 13.80 ms
 Latência P90:           22.10 ms
 Latência P95:           27.50 ms
 Latência P99:           41.20 ms
 Latência Máxima:        58.90 ms
=================================================================
```
