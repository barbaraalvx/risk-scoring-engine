## 📌 Visão Geral do Projeto
Este repositório contém a solução funcional da **POC 2 - Antifraude Mínimo Viável**, desenvolvida para a disciplina de **Engenharia de Sistemas Distribuídos (2026.1)**. 

O objetivo do projeto é construir um motor de *risk scoring* multifatorial capaz de detectar atividades fraudulentas ou suspeitas em tempo real, aplicando quarentena automática com base em limiares configuráveis.

### 🎯 Escopo da POC
*   **Risk Score Multifatorial:** Análise combinada de *device fingerprint*, velocidade de ação, padrão de escolhas e correlação entre contas.
*   **Quarentena Automática:** Isolamento de usuários suspeitos baseado em *thresholds* dinâmicos.
*   **Simulação de Ataques:** Cenários controlados para mitigar ações de bots, multi-contas e conluios coordenados.
*   **Painel Admin:** Integração com *flags* de administração para monitoramento e controle.

### 🛠️ Painel Admin e Flags de Administração
O serviço de scoring expõe um endpoint administrativo para monitorar o estado geral do motor:

- `GET /api/v1/admin/dashboard` — retorna o painel consolidado com flags ativas, quantidade total de scores processados, quantidade de registros em quarentena e os sinais mais recentes.
- `GET /api/v1/flags` — lista os pesos e thresholds atualmente aplicados.
- `PUT /api/v1/admin/scoring-weights` — atualiza pesos, threshold e flags de administração em tempo real no Redis.

As flags administrativas incluem:
- `deviceRuleEnabled`
- `velocityRuleEnabled`
- `patternRuleEnabled`
- `multiAccountRuleEnabled`
- `quarantineEnabled`
- `adminMonitoringEnabled`

Essas flags permitem desativar regras individuais, suspender automaticamente a quarentena e controlar a observabilidade do painel sem reiniciar os serviços.

### 🧪 Testes de Carga e Simulação de Fraudes
O repositório inclui um script em Python para simulação dos 4 cenários de ataque e testes de estresse com relatórios de latência (P50, P95, P99) e vazão (RPS).

```bash
# Executar suíte completa de testes e carga
python scripts/load_test.py --scenario all

# Teste de estresse com 1000 requisições e 30 workers concorrentes
python scripts/load_test.py --scenario stress --requests 1000 --workers 30
```
Para detalhes completos de uso e guia de observabilidade no Grafana/Prometheus, consulte [LOAD_TESTING.md](docs/LOAD_TESTING.md).

---

### 🚀 Como executar o projeto completo
1. Certifique-se de ter instalado:
   - Docker
   - Docker Compose
2. Na raiz do repositório, execute:
   ```bash
   docker compose up --build
   ```
3. Aguarde todos os serviços subirem.

### 🌐 URLs de acesso
* Event ingestion service: `http://localhost:8080`
* Risk scoring service: `http://localhost:8081`
* Quarantine service: `http://localhost:8082`
* Admin dashboard: `http://localhost:8081/admin-dashboard.html`
* Prometheus: `http://localhost:9090`
* Grafana: `http://localhost:3000`

### 🧠 Endpoints principais
* `GET /api/v1/admin/dashboard` — painel administrativo consolidado
* `GET /api/v1/flags` — flags e pesos ativos do scoring
* `PUT /api/v1/admin/scoring-weights` — atualiza pesos, threshold e flags em tempo real
* `GET /quarantine/{playerId}` — estado atual de quarentena de um jogador
* `GET /quarantine/{playerId}/history` — histórico de bloqueios do jogador

### 🛠️ Observações
* O serviço de scoring usa Redis e PostgreSQL para estado e persistência.
* O painel admin se conecta ao serviço de quarentena para exibir status por jogador.

---

### 👥 Equipe
* Bárbara Geovanna Alves Cavalcante - barbara.gacavalcante@gmail.com
* Fellype Dias Fontes - fellypedias2004@gmail.com
* Thaís Melquíades Macêdo - thaismelquiades4@gmail.com
* Tobias Freire Numeriano - tobias.freire@academico.ufpb.br

### 🤖 Ferramentas de IA Utilizadas

Abaixo está o resumo do uso de Ferramentas de Inteligência Artificial Generativa no projeto, conforme as diretrizes da disciplina:

* **1. Ferramentas Utilizadas:** Google Antigravity IDE (Gemini 3.6 Flash / Sonnet 4.6), Claude AI & Claude Code (Sonnet 5 + Agent Starter Kit), ChatGPT (GPT-5.5) e GitHub Copilot (GPT-5.3 Codex).
* **2. Atuação no Trabalho:** Geração inicial de código Java Spring Boot, diagnóstico amplo de erros integrados no VSCode/IDE, arquitetura (ADRs, CQRS/SAGA), criação dos scripts de teste de carga em Python e redação da documentação técnica.
* **3. Orientação da IA (Prompts e Contexto):** Uso de *In-Context Learning* (passando exemplos de código e especificações junto aos prompts), desenvolvimento incremental passo a passo validado via Docker Compose, e *harness* de agentes com 5 personas especializadas (*Agent Starter Kit*).
* **4. Avaliação Honesta:**
  - **O que funcionou:** Desenvolvimento incremental, análise de erros com contexto amplo da IDE e geração automatizada de relatórios estatísticos de testes de carga.
  - **O que precisou de correção:** Código gerado por prompts isolados sem o contexto completo do projeto e desalinhamento na ordem de dependências de tarefas sugeridas pelas LLMs.
  - **O que foi descartado:** *Loop engineering* totalmente autônomo, devido ao consumo excessivo de tokens e geração de código inacabado que quebrava o build da aplicação.

Para o relatório detalhado com metodologias e análises, consulte [docs/AI_TOOLS.md](docs/AI_TOOLS.md).

---

### 📹 Apresentações e Entregas
* Projeto 01 (26/06): Definição de Tema
* Projeto 02 (10/07): Documentação Inicial & Arquitetura -> [Link do Videocast 1]
* Projeto 03 (07/08): Documentação Final & Solução Completa -> [Link do Videocast Final]

