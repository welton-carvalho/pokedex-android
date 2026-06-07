# 📘 Apostila SDD — Construindo "Comparar Pokémon" com a skill `/tlc-spec-driven`

> Guia passo a passo para **você** conduzir o fluxo Spec-Driven Development (SDD) e construir a feature de comparação.
> Cada passo tem: 🎯 objetivo · ⌨️ o que você digita · ⚙️ o que a skill faz · 📄 artefato gerado · 🔍 onde conferir (arquivo da skill) · 💡 dica específica da feature.
>
> **Tudo que afirmo aqui é verificável.** Os caminhos `references/*.md` ficam em `.claude/skills/tlc-spec-driven/`. Os caminhos de código são do seu próprio projeto.

---

## 0. Como a skill funciona (leia antes)

A `/tlc-spec-driven` organiza o trabalho em **4 fases**:

```
┌──────────┐   ┌──────────┐   ┌─────────┐   ┌─────────┐
│ SPECIFY  │ → │  DESIGN  │ → │  TASKS  │ → │ EXECUTE │
└──────────┘   └──────────┘   └─────────┘   └─────────┘
 obrigatório    opcional*      opcional*    obrigatório
                        * a skill pula sozinha quando o escopo não precisa
```

Três coisas essenciais para usar bem:

1. **Não é slash-command a cada passo.** Depois de ativada, você dirige a skill por **frases naturais** (ex: *"mapear o codebase"*, *"especificar a feature"*). Eu reconheço a frase e sigo o arquivo de referência correto.
   - 🔍 Conferir: `SKILL.md` → seção *"Commands"* (tabela de "Trigger Pattern" → "Reference").
2. **Auto-sizing:** a skill mede a complexidade e **pula fases que não agregam**. Bug pequeno = "quick mode". Feature multi-componente (nosso caso) = roda as 4.
   - 🔍 Conferir: `SKILL.md` → seção *"Auto-Sizing: The Core Principle"* (tabela Small/Medium/Large/Complex).
3. **Cadeia de verificação de conhecimento:** antes de decidir qualquer coisa técnica, a skill checa nesta ordem: código → docs do projeto → Context7 → web → (só então) "incerto, verifique". Nunca inventa API.
   - 🔍 Conferir: `SKILL.md` → seção *"Knowledge Verification Chain"*.

**Como (re)ativar numa sessão nova:** digite `/tlc-spec-driven` ou simplesmente uma frase-gatilho como *"vamos mapear o codebase"*.

---

## 🎯 A feature usada como exemplo nesta apostila

> **Comparar 2 Pokémon.** Na tela inicial (lista), o usuário seleciona **2** Pokémon → abre uma tela de comparação mostrando os **status lado a lado**. O conteúdo espelha a tela de **Detalhe**, **exceto** as seções **"About"** (peso/altura/abilities) e a **description** (texto de flavor).

Escopo = **Large** (módulo novo + MVI + navegação + UI) → roda as 4 fases.

---

## 🗺️ Mapa geral — a sequência de comandos

| # | Você digita | Fase | Artefato | Onde conferir |
|---|---|---|---|---|
| 1 | *"mapear o codebase"* | (brownfield) | `.specs/codebase/` (7 docs) | `references/brownfield-mapping.md` |
| 2 | *"inicializar o projeto"* | (setup) | `.specs/project/PROJECT.md` + `STATE.md` | `references/project-init.md` |
| 3 | *"criar o roadmap"* | (setup) | `.specs/project/ROADMAP.md` | `references/roadmap.md` |
| 4 | *"especificar a feature de comparação"* | **SPECIFY** | `.specs/features/pokemon-compare/spec.md` | `references/specify.md` |
| 5 | *(automático, se houver ambiguidade)* | DISCUSS | `…/context.md` | `references/discuss.md` |
| 6 | *"desenhar a arquitetura"* | **DESIGN** | `…/design.md` | `references/design.md` |
| 7 | *"quebrar em tasks"* | **TASKS** | `…/tasks.md` | `references/tasks.md` |
| 8 | *"implementar"* | **EXECUTE** | código + commits atômicos | `references/implement.md` |
| 9 | *"validar"* / *"UAT"* | (verificação) | relatório de validação | `references/validate.md` |

**A qualquer momento:** *"registrar decisão / blocker"* (`references/state-management.md`) · *"pausar trabalho"* / *"retomar"* (`references/session-handoff.md`).

🔍 A estrutura de pastas `.specs/` inteira está documentada no `SKILL.md` → *"Project Structure"*.

---

## 🔢 Passo a passo detalhado

### Passo 1 — Mapear o codebase

- 🎯 **Objetivo:** documentar o estado **real** do código antes de planejar. Projeto existente sempre começa aqui.
- ⌨️ **Você digita:** `mapear o codebase`
- ⚙️ **O que a skill faz:** explora a estrutura, lê os manifestos (`libs.versions.toml`, `settings.gradle.kts`), amostra 5–10 arquivos representativos por categoria e escreve 7 documentos.
- 📄 **Artefato:** `.specs/codebase/` → `STACK.md`, `ARCHITECTURE.md`, `CONVENTIONS.md`, `STRUCTURE.md`, `TESTING.md`, `INTEGRATIONS.md`, `CONCERNS.md`.
- 🔍 **Conferir:** `references/brownfield-mapping.md` (processo + template de cada doc + limites de tokens).
- 💡 **Pontos de atenção que o mapa vai revelar** (já levantados no seu código — peça para eu confirmar quando rodar):
  - A tabela *"Estado de Implementação"* do `CLAUDE.md` está **desatualizada**: `data:*`, `feature:pokemon-list`, `feature:pokemon-detail`, `core:testing` e o `:app` shell **já existem**.
  - A **navegação real** é `core:route:keys` + `core:route:deeplink` + `core:route:navigation` — **não** `core:navigation` como o `CLAUDE.md` diz. As rotas são `NavKey` em `core/route/keys/RouteKeys.kt` (ex: `PokemonDetailKey(val pokemonId: Int)`).
  - Já existe `core/ui/PokemonStatBar.kt` — **reutilizável** na comparação.
  - **Anatomia da tela de Detalhe** (`feature/pokemon-detail/.../ui/screen/PokemonDetailScreen.kt`): tipos (chips) · **"About"** (peso/altura/abilities) · **description** · **"Base Stats"** (lista de `PokemonStatBar`). → Para a comparação: **mantém** tipos + Base Stats + imagem + nome; **remove** About + description.

> ⚠️ Por que este passo importa: sem ele, o planejamento seguiria a tabela desatualizada do `CLAUDE.md` e refaria trabalho já pronto.

---

### Passo 2 — Inicializar o projeto

- 🎯 **Objetivo:** registrar visão/objetivos e criar a **memória persistente** entre sessões.
- ⌨️ **Você digita:** `inicializar o projeto`
- ⚙️ **O que a skill faz:** cria a pasta `project/` com a visão e o arquivo de estado (decisões, blockers, lições, TODOs, ideias adiadas).
- 📄 **Artefato:** `.specs/project/PROJECT.md` + `.specs/project/STATE.md`.
- 🔍 **Conferir:** `references/project-init.md`.
- 💡 **Dica:** o `STATE.md` é o "cérebro" entre sessões. Toda decisão de arquitetura da comparação (ex: como espelhar os stats) deve acabar aqui via *"registrar decisão"*.

---

### Passo 3 — Criar o roadmap

- 🎯 **Objetivo:** listar features/milestones priorizadas.
- ⌨️ **Você digita:** `criar o roadmap`
- ⚙️ **O que a skill faz:** monta `ROADMAP.md` com as features planejadas.
- 📄 **Artefato:** `.specs/project/ROADMAP.md`.
- 🔍 **Conferir:** `references/roadmap.md`.
- 💡 **Dica:** adicione *"Pokémon Compare"* como item do roadmap. Para uma única feature isolada, este passo é leve — a skill pode até sugerir pular se você quiser ir direto ao Specify.

---

### Passo 4 — Especificar a feature (SPECIFY) — *obrigatório*

- 🎯 **Objetivo:** definir **o quê** com requisitos rastreáveis (IDs).
- ⌨️ **Você digita:** `especificar a feature de comparação`
- ⚙️ **O que a skill faz:** escreve requisitos numerados (REQ-001, REQ-002…), critérios de aceite e estados obrigatórios.
- 📄 **Artefato:** `.specs/features/pokemon-compare/spec.md`.
- 🔍 **Conferir:** `references/specify.md`.
- 💡 **Como ficaria o spec da comparação** (esboço para você validar):
  - `REQ-001` — Na lista, o usuário consegue selecionar exatamente **2** Pokémon.
  - `REQ-002` — A tela de comparação mostra, para cada Pokémon: imagem (artwork), nome, #número, tipos e **Base Stats**.
  - `REQ-003` — Os **status** aparecem **lado a lado** para comparação direta.
  - `REQ-004` — **Não** exibe a seção "About" nem a description.
  - `REQ-005` — Estados de UI: `loading`, `success`, `error`, `empty` (regra do `CLAUDE.md`).
  - 🔍 Confira os estados obrigatórios no `CLAUDE.md` → *"Padrão MVI"*.

---

### Passo 5 — Discutir zonas cinzentas (DISCUSS) — *automático*

- 🎯 **Objetivo:** capturar **suas decisões** onde o spec é ambíguo. A skill dispara isto sozinha quando detecta ambiguidade.
- ⌨️ **Você digita:** nada — eu pergunto. (Ou force com *"discutir a feature"*.)
- 📄 **Artefato:** `.specs/features/pokemon-compare/context.md`.
- 🔍 **Conferir:** `references/discuss.md`.
- 💡 **A zona cinzenta nº 1 desta feature:** *como selecionar 2 Pokémon na lista?* Hoje `feature/pokemon-list/.../PokemonListScreen.kt` faz toque único → `ClickPokemon(id)` → navega pro detalhe. Você vai precisar decidir:
  - Entrar em "modo seleção" (ex: botão "Comparar" no header) e tocar em 2 cards? **(recomendado — menos intrusivo)**
  - Long-press para iniciar seleção?
  - Tela separada de seleção?
  - O que acontece ao escolher o 2º (navega automático? confirma?).

---

### Passo 6 — Desenhar a arquitetura (DESIGN) — *opcional, mas SIM neste caso*

- 🎯 **Objetivo:** decidir módulos, componentes, fluxo de dados e navegação.
- ⌨️ **Você digita:** `desenhar a arquitetura`
- ⚙️ **O que a skill faz:** documenta o design — novo módulo, contratos MVI, reuso, integração de navegação.
- 📄 **Artefato:** `.specs/features/pokemon-compare/design.md`.
- 🔍 **Conferir:** `references/design.md`.
- 💡 **Decisões-chave do design da comparação** (baseadas no código real):
  - **Novo módulo** `feature:pokemon-compare` seguindo o `pokedexlab.android.feature` (convention plugin).
  - **Reuso de domínio:** `GetPokemonDetailUseCase(id): Result<Pokemon>` (em `core/domain`) chamado **2×** — um por Pokémon. Nenhuma mudança de data/domínio necessária.
  - **MVI** espelhando `feature:pokemon-detail`: `CompareState`, `CompareIntent`, `CompareReducer` (objeto puro), `CompareViewModel` (`MutableStateFlow` + `Channel`), `CompareUiModel`, mapper `Pokemon.toCompareUiModel()`.
  - **Reuso de UI:** `core/ui/PokemonStatBar.kt` para as barras de status; `PokemonTypeChip` e `pokemonTypeColor(...)` do `core:design-system`.
  - **Decisão a registrar (ver CONCERNS):** o `StatUiModel`/label map de hoje é **privado** do `feature:pokemon-detail` e features não se importam entre si. Escolha: (a) duplicar um mapper pequeno na comparação, ou (b) promover um `StatUiModel` compartilhado para `core:ui`.
  - **Navegação:** nova `PokemonCompareKey(val firstId: Int, val secondId: Int) : NavKey` em `core/route/keys/RouteKeys.kt`; novo `pokemonCompareEntry(navigator)`; **registrar** em `core/route/navigation/AppNavDisplay.kt` (lembre: esse arquivo conhece todas as features).
  - 🔧 Se a skill `mermaid-studio` estiver instalada, ela é usada para os diagramas; senão, diagramas vão inline.

---

### Passo 7 — Quebrar em tasks (TASKS) — *opcional, mas SIM neste caso*

- 🎯 **Objetivo:** transformar o design em **tarefas atômicas** com critério de verificação e plano de commits.
- ⌨️ **Você digita:** `quebrar em tasks`
- ⚙️ **O que a skill faz:** lista tasks com *What / Where / Depends on / Reuses / Done when / Tests / Gate*, marca paralelizáveis com `[P]`.
- 📄 **Artefato:** `.specs/features/pokemon-compare/tasks.md`.
- 🔍 **Conferir:** `references/tasks.md`.
- 💡 **Esboço das tasks da comparação:**
  1. Criar módulo `feature:pokemon-compare` (build.gradle + Koin module + registro no `:app`).
  2. Adicionar `PokemonCompareKey` em `core:route:keys`.
  3. MVI: `CompareState` / `CompareIntent` / `CompareReducer` (+ teste do reducer).
  4. `CompareUiModel` + mapper `Pokemon.toCompareUiModel()` (+ teste do mapper).
  5. `CompareViewModel` (chama o use case 2×, junta resultados).
  6. `CompareScreen` (status lado a lado, sem About/description) + nav entry.
  7. Registrar entry no `AppNavDisplay`.
  8. Alterar `feature:pokemon-list` para o modo de seleção de 2 (conforme decidido no Passo 5).
  9. Gate final: `./gradlew assembleDebug test`.

> 🛟 **Válvula de segurança:** mesmo se você pular o passo de Tasks, a fase Execute **sempre** começa listando os passos atômicos. Se aparecerem >5 passos ou dependências complexas, a skill **para** e cria o `tasks.md` formal. (🔍 `SKILL.md` → *"Safety valve"*.)

---

### Passo 8 — Implementar (EXECUTE) — *obrigatório*

- 🎯 **Objetivo:** escrever o código, task por task, verificando cada uma.
- ⌨️ **Você digita:** `implementar` (ou *"implementar a task 1"* para uma específica).
- ⚙️ **O que a skill faz:** executa cada task, roda o "gate check" (testes/build), faz **commits atômicos** e atualiza o status no `tasks.md`.
- 📄 **Artefato:** código + commits (1 por task, mensagem rastreável ao REQ/Task).
- 🔍 **Conferir:** `references/implement.md` e `references/coding-principles.md`.
- 💡 **Dica:** siga as convenções já existentes (eu as documento no `CONVENTIONS.md` quando rodar o Passo 1). O gate rápido do projeto é `./gradlew :feature:pokemon-compare:test`; o gate de build é `./gradlew assembleDebug test`.

---

### Passo 9 — Validar / UAT — *verificação final*

- 🎯 **Objetivo:** conferir que cada requisito foi atendido (e, para features de UI, te guiar num teste manual).
- ⌨️ **Você digita:** `validar` ou `UAT`
- ⚙️ **O que a skill faz:** cruza o que foi implementado com os REQ-IDs do spec e reporta lacunas.
- 🔍 **Conferir:** `references/validate.md`.
- 💡 **Dica:** tarefas leves (validação, atualização de estado, handoff) rodam bem em modelos mais rápidos/baratos — você pode trocar de modelo só para essas.

---

## 🧰 Comandos de apoio (use a qualquer momento)

| Você digita | Para quê | Conferir |
|---|---|---|
| *"registrar decisão: …"* / *"logar blocker: …"* | Gravar no `STATE.md` (memória) | `references/state-management.md` |
| *"pausar trabalho"* / *"encerrar sessão"* | Salvar handoff para retomar depois | `references/session-handoff.md` |
| *"retomar"* / *"continuar"* | Recarregar contexto e seguir de onde parou | `references/session-handoff.md` |
| *"documentar concerns"* | Levantar dívidas/riscos | `references/concerns.md` |

---

## ✅ Checklist do fluxo completo

- [ ] **1.** `mapear o codebase` → 7 docs em `.specs/codebase/`
- [ ] **2.** `inicializar o projeto` → `PROJECT.md` + `STATE.md`
- [ ] **3.** `criar o roadmap` → `ROADMAP.md` (leve / opcional)
- [ ] **4.** `especificar a feature de comparação` → `spec.md` com REQ-IDs
- [ ] **5.** (auto) decidir a seleção de 2 na lista → `context.md`
- [ ] **6.** `desenhar a arquitetura` → `design.md`
- [ ] **7.** `quebrar em tasks` → `tasks.md`
- [ ] **8.** `implementar` → código + commits atômicos + gate verde
- [ ] **9.** `validar` → todos os REQ-IDs atendidos

---

## 💡 Boas práticas para tirar o máximo da skill

- **Confie no auto-sizing.** Não force as 4 fases num bug pequeno — diga *"quick fix: …"* e a skill usa o modo expresso.
- **Uma fonte de verdade só.** Quando o `CLAUDE.md` divergir do código, o código (e os docs `.specs/codebase/`) ganham.
- **Memória é de graça e persiste.** Sempre que decidir algo não óbvio, *"registrar decisão"*. Na próxima sessão, *"retomar"*.
- **Contexto enxuto.** A skill mira <40k tokens de contexto; ela carrega só os docs necessários por fase. (🔍 `SKILL.md` → *"Context Loading Strategy"*.)
- **Sub-agents** podem ser usados para tarefas pesadas (mapeamento, implementação de tasks em paralelo). (🔍 `SKILL.md` → *"Sub-Agent Delegation"*.)

---

*Apostila gerada para o projeto PokedexLab · feature de exemplo: Comparar Pokémon · skill `tlc-spec-driven`.*
*Esta apostila descreve o fluxo — nada foi executado. Quando você quiser começar, é só digitar o comando do Passo 1.*
