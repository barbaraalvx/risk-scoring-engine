# 🤖 Relatório de Uso de Ferramentas de IA (AI Disclosure)

Este documento detalha o uso de **Ferramentas de Inteligência Artificial Generativa e LLMs** durante o desenvolvimento da **POC 2 (Motor Antifraude e Risk Scoring Engine)**, seguindo a estrutura de avaliação da disciplina.

---

## 1. Quais ferramentas foram usadas
Durante o ciclo de vida do projeto, foram utilizadas as seguintes ferramentas e modelos:
* **Google Antigravity IDE:** Modelos Gemini 3.6 Flash Medium, Gemini 3.1 Pro e Claude Sonnet 4.6 Thinking.
* **Claude AI & Claude Code:** Modelos Anthropic Claude 3.5 / 5 (Sonnet), integrado ao harness *Agent Starter Kit*.
* **ChatGPT:** OpenAI GPT-5.5.
* **GitHub Copilot:** Modelo GPT-5.3 Codex (Integrado ao VSCode).

---

## 2. Em quais partes do trabalho cada ferramenta atuou
* **Geração e Implementação de Código:** 
  - **ChatGPT & GitHub Copilot:** Geração inicial de classes Java Spring Boot 3, DTOs e entidades JPA dos microserviços.
  - **Claude Code & Agent Starter Kit:** Implementação automatizada e incremental de componentes de software com personas dedicadas (Orquestração, Desenvolvimento, Revisão).
  - **Google Antigravity IDE:** Implementação e refatoração da suíte de testes de carga e ataques em Python (`scripts/load_test.py`).
* **Análise e Diagnóstico de Erros:**
  - **GitHub Copilot & Antigravity IDE:** Análise ampla do repositório no VSCode/IDE para identificação da origem de erros integrados de runtime (ex: desserialização Kafka, WebClient 404/405 e dessincronização de quarentena no Admin Dashboard).
* **Documentação e Arquitetura:**
  - **Claude AI:** Especificação teórica do escopo inicial, refinamento de requisitos e estrutura do *Quarantine Service*.
  - **Google Antigravity IDE:** Elaboração dos Registros de Decisão de Arquitetura ([ADRs](adr/README.md)) e consolidação do relatório de testes de desempenho ([TEST_REPORT.md](TEST_REPORT.md)).
* **Testes e Qualidade:**
  - **Antigravity IDE & Claude Code:** Construção de cenários de estresse, medição de percentis de latência (P50/P90/P95/P99) e validação de gates de qualidade.

---

## 3. Como a IA foi orientada — Instruções e Contexto Fornecido
* **In-Context Learning (Prompts com Contexto Técnico):** Para evitar que as LLMs gerassem código fora do padrão do projeto, os prompts eram acompanhados por arquivos de microserviços já finalizados ou especificações OpenAPI/C4. Isso garantiu a manutenção de convenções e contratos REST/Kafka.
* **Harness com Personas Especializadas (Agent Starter Kit):** Uso de um *harness* de agentes com 5 papéis definidos (*Orquestrador*, *Planejador*, *Desenvolvedor*, *Revisor de Qualidade*, e *Contextualizador*), forçando a validação e o refinamento do código em múltiplos níveis antes da aprovação.
* **Desenvolvimento Incremental Passo a Passo:** Instrução explícita para que a IA executasse o desenvolvimento em etapas curtas e testáveis. A cada fim de etapa, a aplicação era recompilada via Docker Compose para validação funcional antes de avançar.

---

## 4. Avaliação honesta

### ✅ O que funcionou bem
* **Desenvolvimento Incremental:** A abordagem de solicitar alterações em pequenos blocos funcionais se mostrou altamente eficaz, mantendo a stack Docker sempre operacional.
* **Diagnóstico Amplo de Erros na IDE:** O uso do GitHub Copilot e Antigravity com visibilidade de todo o repositório permitiu identificar causas raízes de falhas de comunicação entre microserviços (ex: falta de endpoints REST no CQRS) muito mais rápido do que a depuração manual.
* **Automação de Testes e Documentação:** A IA gerou relatórios de testes de carga rigorosos com cálculos estatísticos de latência e ADRs no formato padrão de engenharia de software.

### ⚠️ O que precisou ser corrigido
* **Falta de Memória de Sessão em Prompts Isolados:** Prompts no ChatGPT sem o código completo do projeto resultaram em classes desalinhadas com o restante da arquitetura, exigindo refatorações manuais para ajustar contratos de API.
* **Ordenação de Dependências de Tarefas:** LLMs utilizadas para documentação (como Claude AI) sugeriram em alguns momentos o desenvolvimento de funcionalidades sem respeitar a ordem lógica de dependências prévias (ex: implementar o consumo de eventos antes da estrutura do evento em si).

### ❌ O que foi descartado
* **Loop Engineering Totalmente Autônomo:** A tentativa de deixar a IA desenvolvendo em *loop* contínuo sem intervenção humana foi descartada. Essa estratégia esgotou rapidamente a cota de tokens das APIs e gerou trechos de código incompletos que quebravam o build da aplicação.
