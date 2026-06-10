# Capítulo 5 — AI-DLC com mais de uma feature (e alterações)

> Sua dúvida: "agora que concluí o fluxo, parece que Inception e Construction (menos a pasta `pokemon-compare`) seriam compartilhados entre outras features; alguns arquivos (`audit.md`, `aidlc-state.md`, a pasta `requirements`) deveriam ser específicos da feature?"
>
> Este capítulo responde **como o AI-DLC realmente funciona** — sem propor convenções fora do framework. Tudo aqui vem das regras em `.aidlc-rule-details/` e da estrutura de diretórios definida no `CLAUDE.md`.

## 5.1 O fato central: existe **um** `aidlc-docs/` por workspace

O AI-DLC oficial **não** define pastas por feature para os artefatos de Inception. A estrutura de diretórios prescrita (do `CLAUDE.md`) é esta:

```text
aidlc-docs/                          # DOCUMENTAÇÃO (um por workspace)
├── inception/
│   ├── plans/
│   ├── reverse-engineering/         # descreve o SISTEMA inteiro
│   ├── requirements/
│   ├── user-stories/
│   └── application-design/
├── construction/
│   ├── plans/
│   ├── {unit-name}/                 # ← ESTA parte é por-unidade (ex.: pokemon-compare/)
│   │   ├── functional-design/
│   │   ├── nfr-requirements/
│   │   ├── nfr-design/
│   │   ├── infrastructure-design/
│   │   └── code/
│   └── build-and-test/
├── aidlc-state.md                   # ARQUIVO ÚNICO (estado/progresso)
└── audit.md                         # ARQUIVO ÚNICO (journal append-only)
```

Ou seja, **sua observação está correta**:
- **Por unidade (separa naturalmente):** só `construction/{unit-name}/` (no nosso caso, `construction/pokemon-compare/`) e o plano `construction/plans/pokemon-compare-code-generation-plan.md` (o nome do arquivo carrega a unidade).
- **NÃO namespeado por feature:** `inception/requirements/`, `inception/plans/`, `inception/application-design/` — ficam nos mesmos caminhos fixos.
- **Compartilhado/persistente (descreve o sistema):** `inception/reverse-engineering/`.
- **Arquivos únicos do workspace:** `aidlc-state.md` (estado) e `audit.md` (journal cronológico).

> Por que `audit.md` e `aidlc-state.md` são únicos? Pela definição do framework: `audit.md` é um **log cronológico append-only de TODAS as interações** ("ALWAYS append... NEVER overwrite"), e `aidlc-state.md` é o **rastreador de estado** que o Workspace Detection lê para retomar o trabalho. Eles são, por natureza, do **workspace**, não de uma feature.

## 5.2 Classificação dos arquivos (como o AI-DLC os trata)

| Arquivo / pasta | Natureza no AI-DLC | Vale para outra feature? |
|---|---|---|
| `reverse-engineering/*` | Documenta o **sistema** | Sim — compartilhado; re-rodado se o código mudou (ver 5.3) |
| `aidlc-state.md` | **Estado/progresso** do workspace | Sim — é retomado e atualizado |
| `audit.md` | **Journal** cronológico append-only | Sim — continua recebendo entradas |
| `inception/requirements/*` | Artefato do **trabalho atual** | Não namespeado — caminho fixo |
| `inception/plans/*` | Artefato do **trabalho atual** | Não namespeado — caminho fixo |
| `inception/application-design/*` | Artefato do **trabalho atual** | Não namespeado — caminho fixo |
| `construction/{unit-name}/*` | **Por unidade** | Separa por nome da unidade |

## 5.3 Como o AI-DLC inicia uma **nova feature**

Você não "começa do zero". O AI-DLC é orientado por **Workspace Detection** + **Session Continuity**:

1. **Workspace Detection** (regra `inception/workspace-detection.md`, Step 1): checa se `aidlc-docs/aidlc-state.md` existe. Como já existe, ele **retoma** ("resume from last phase") em vez de criar um projeto novo. A regra de Session Continuity manda apresentar o prompt "Welcome back" com o estado atual.
2. Você informa a **nova feature** como o novo pedido. O fluxo então roda a **Requirements Analysis** de novo (sempre executa, em profundidade adaptativa), gerando novo conteúdo em `inception/requirements/`.
3. **Reverse Engineering** (regra `workspace-detection.md`, Step 3): como já há artefatos de RE, eles são **carregados e o RE é pulado** — **a menos que** estejam *stale* (o código mudou de forma relevante desde a última análise, ex.: depois que `pokemon-compare` entrou) ou você peça explicitamente um rerun. Nesse caso, o RE é **re-rodado para refletir o código atual**.
4. **Construction** da nova feature vai para a **própria** pasta `construction/{nova-unidade}/` (ex.: `construction/pokemon-favorites/`), separada da `pokemon-compare/`.
5. **`audit.md`** continua sendo **apendado** (as interações da nova feature ficam após as anteriores — é um journal contínuo). **`aidlc-state.md`** é atualizado para refletir o novo progresso.

**Sobre os artefatos de Inception não namespeados** (`requirements/`, `plans/`, `application-design/`): como os caminhos são fixos, a nova feature **gera conteúdo nesses mesmos arquivos**. O AI-DLC, como framework, **não define** arquivamento/versionamento dos artefatos de Inception da feature anterior — isso simplesmente não faz parte da especificação. (Para preservar histórico, o que o framework garante é o `audit.md`, que mantém o registro cronológico de tudo.)

## 5.4 Como o AI-DLC trata uma **alteração na mesma feature**

Aqui entra o **Adaptive Workflow Principle** ("the workflow adapts to the work"): mudanças simples **pulam stages**.

1. **Workspace Detection** retoma o estado atual.
2. **Requirements Analysis** roda em profundidade **mínima** se a mudança é clara — pode apenas **atualizar** o `requirements.md` existente (ex.: adicionar um FR).
3. **Workflow Planning** decide o que executar; para uma alteração pequena, tende a **pular** Application Design / Units / NFR e ir direto ao **Code Generation**.
4. **Code Generation** reusa a **mesma unidade** `construction/pokemon-compare/`. Pela regra de brownfield (`construction/code-generation.md`): **modificar arquivos in-place**, nunca criar cópias (`Classe_modified.kt`).
5. **Build and Test** roda de novo.
6. **`audit.md`** apenda as interações da alteração; **`aidlc-state.md`** reflete os stages reabertos/concluídos.

> Resumo da diferença:
> - **Nova feature** → novo ciclo (Requirements → … → nova `construction/{unidade}/`), RE possivelmente re-rodado por staleness.
> - **Alteração na mesma feature** → ciclo **adaptativo enxuto** sobre a **mesma unidade**, geralmente pulando a maior parte da Inception.

## 5.5 Resposta direta à sua dúvida

- **"Inception e Construction seriam compartilhados?"** — Inception (`requirements/`, `plans/`, `application-design/`) usa **caminhos fixos compartilhados** (não há separação por feature no AI-DLC); `reverse-engineering/` é do **sistema**; só `construction/{unit-name}/` separa por feature. **Sua leitura está certa.**
- **"`audit.md` / `aidlc-state.md` / `requirements/` deveriam ser por feature?"** — No AI-DLC, **não são**: `audit.md` e `aidlc-state.md` são do **workspace** (journal e estado), e `requirements/` é o artefato do **trabalho atual** em caminho fixo. É assim que o framework foi desenhado.
- **"Como inicio outra feature?"** — Disparando o fluxo de novo: o Workspace Detection **retoma** pelo `aidlc-state.md`, você descreve a nova feature, a Requirements Analysis roda de novo, o RE é reusado (ou re-rodado se o código mudou), e a Construction vai para uma **nova `construction/{unidade}/`**.

> Quer ver a **alteração na mesma feature** na prática, com cada passo mantendo os docs em sincronia? Vá para o [Capítulo 6 — Hands-on de alteração](06-hands-on-alteracao-mantendo-specs.md).

---

← Voltar ao [índice](README.md)
