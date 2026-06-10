# Capítulo 4 — Supporting Tools

> O AI-DLC traz, oficialmente, uma seção **Supporting Tools** com **duas** ferramentas (em `scripts/` do repositório [awslabs/aidlc-workflows](https://github.com/awslabs/aidlc-workflows#supporting-tools)). Elas **não** são stages do fluxo — servem para **validar e melhorar** os artefatos gerados nas fases de Inception e Construction. Este capítulo explica cada uma e mostra como usá-las no contexto do PokedexLab.

## 4.1 AIDLC Design Reviewer

**Local:** `scripts/aidlc-designreview/`
**Status:** experimental, com integração AWS Bedrock.
**O que faz:** ferramenta de **revisão de design por IA** que analisa os artefatos de design do AI-DLC (o conteúdo de `aidlc-docs/`).

**Recursos:**
- **Multi-Agent Review** — três agentes de IA especializados.
- **Quality Scoring** — pontuação de qualidade com análise de severidade.
- **Dois modos de uso:** ferramenta de **CLI** e **Claude Code Hook**.

**Invocação (CLI):**
```bash
design-reviewer --aidlc-docs /path/to/aidlc-docs
```

### Exemplo no PokedexLab
Depois de gerar os artefatos da feature de comparação, você aponta a ferramenta para a pasta de docs do projeto:
```bash
design-reviewer --aidlc-docs ./aidlc-docs
```
Ela analisaria, por exemplo:
- `aidlc-docs/inception/application-design/` — coerência entre `components.md`, `component-methods.md`, `services.md` e `component-dependency.md` (ex.: o trade-off de DI documentado em `services.md`, o isolamento de features no `component-dependency.md`).
- `aidlc-docs/inception/requirements/requirements.md` — completude dos FRs/NFRs e da seção de compliance das extensions.

O resultado é um relatório com **score de qualidade** e achados por severidade — útil como uma segunda opinião antes de aprovar um stage. No modo **Claude Code Hook**, a revisão pode ser disparada automaticamente durante o trabalho com o agente.

## 4.2 AIDLC Evaluator

**Local:** `scripts/aidlc-evaluator/`
**O que faz:** framework de **testes e relatórios automatizados** para validar **mudanças nos próprios workflows do AI-DLC** (as regras em `aidlc-rules`/`*-rule-details`).

**Recursos:**
- **Golden Test Cases** — casos de referência (baseline) para validação.
- **Execution Framework** — orquestração dos testes.
- **Semantic Evaluation** — avaliação baseada em IA.
- **Code Evaluation** — análise estática do código gerado.
- **NFR Evaluation** — avaliação de requisitos não-funcionais.
- **CI/CD Integration** — validação automática em PRs.

**Invocação:**
```bash
cd scripts/aidlc-evaluator
uv sync
uv run python run.py test
```

### Exemplo no PokedexLab
Esta ferramenta é para quem **estende o próprio AI-DLC**. No Capítulo 3 criamos a extension **Design System Baseline**. Para garantir que essa mudança nas regras não quebra o fluxo, você rodaria o Evaluator:
```bash
cd scripts/aidlc-evaluator
uv sync
uv run python run.py test
```
Os **Golden Test Cases** validam se o comportamento do fluxo continua correto (ex.: a pergunta de opt-in aparece na Requirements Analysis; as regras `DESIGNSYS-NN` são avaliadas nos stages de UI; o opt-out não carrega as regras). A **Code Evaluation** e a **NFR Evaluation** checam a qualidade do que o fluxo produz. Em PR, a **CI/CD Integration** roda isso automaticamente.

## 4.3 Quando usar cada uma

| Situação | Ferramenta |
|---|---|
| "Quero uma revisão dos meus artefatos de design da feature" (ex.: a comparação) | **AIDLC Design Reviewer** |
| "Alterei/Criei regras do AI-DLC (ex.: a extension de Design System) e quero validar o fluxo" | **AIDLC Evaluator** |

Ambas operam **sobre** os artefatos de Inception/Construction — não substituem os approval gates, complementam-nos.

> Observação: as duas ferramentas vivem em `scripts/` do repositório oficial do AI-DLC e têm dependências próprias (a Design Reviewer usa AWS Bedrock; o Evaluator usa `uv`/Python). Consulte o repositório [awslabs/aidlc-workflows](https://github.com/awslabs/aidlc-workflows#supporting-tools) para a configuração de cada uma.

Próximo: [Capítulo 5 — AI-DLC em múltiplas features](05-aidlc-multiplas-features.md).
