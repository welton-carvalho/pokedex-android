col# Capítulo 3 — Extension: validador de Design System

> Objetivo: criar uma **extension do AI-DLC** que verifica se as telas foram construídas usando o **Design System** (tokens e componentes compartilhados) em vez de valores hardcoded. Vamos usar **exatamente** o mecanismo oficial de extensions do AI-DLC.

## 3.1 Como uma extension funciona no AI-DLC (oficial)

As extensions vivem em `aws-aidlc-rule-details/extensions/<categoria>/` (neste projeto, `.aidlc-rule-details/extensions/<categoria>/`) e usam um **padrão de dois arquivos**:

1. **Arquivo de regras** `<nome>.md` — restrições *blocking*, no formato `## Rule <PREFIX-NN>: <Título>` com seções **Rule** e **Verification**.
2. **Arquivo de opt-in** `<nome>.opt-in.md` — apresenta uma pergunta de múltipla escolha durante a **Requirements Analysis**.

Comportamento oficial:
- Ao **opt-in**, o arquivo de regras é carregado e passa a valer. Ao **opt-out**, o arquivo de regras **nunca** é carregado.
- Extensions **sem** arquivo opt-in são **sempre** aplicadas.
- Em cada stage aplicável, o modelo avalia as regras e inclui um resumo de compliance (compliant / non-compliant / N/A). Qualquer regra aplicável não cumprida é **blocking finding** — o stage não oferece "Continue" até resolver.

> É o mesmo mecanismo das extensions que você já usou neste projeto: `extensions/security/baseline/` e `extensions/resiliency/baseline/`.

## 3.2 Criando a extension "Design System Baseline"

### Arquivo 1 — opt-in
`.aidlc-rule-details/extensions/design-system/baseline/design-system-baseline.opt-in.md`:
````markdown
# Design System Baseline — Opt-In

**Extension**: Design System Baseline

## Opt-In Prompt

The following question is automatically included in the Requirements Analysis clarifying questions when this extension is loaded:

```markdown
## Question: Design System Extension
Should design-system compliance rules be enforced for screens/components in this project?

A) Yes — enforce all DESIGNSYS rules as blocking constraints (recommended for apps with a shared design system)

B) No — skip all DESIGNSYS rules (suitable for spikes/prototypes)

X) Other (please describe after [Answer]: tag below)

[Answer]:
```
````

### Arquivo 2 — regras
`.aidlc-rule-details/extensions/design-system/baseline/design-system-baseline.md`:
````markdown
# Baseline Design System Rules

## Overview
These design-system rules are MANDATORY cross-cutting constraints applied when generating UI design
artifacts and UI code. They are hard constraints, not guidance.

**Enforcement**: At each applicable stage (Application Design, Code Generation), the model MUST verify
compliance before presenting the stage completion message.

### Blocking Finding Behavior
A blocking design-system finding means:
1. The finding MUST be listed in the stage completion message under a "Design System Findings" section
   with the DESIGNSYS rule ID and the offending file/element.
2. The stage MUST NOT present "Continue to Next Stage" until all blocking findings are resolved.
3. The finding MUST be logged in `aidlc-docs/audit.md` with the rule ID, description, and stage context.

If a rule is not applicable (e.g., no UI in the change), mark it as **N/A** — not a blocking finding.

### Default Enforcement
All rules are **blocking** by default.

---

## Rule DESIGNSYS-01: Use Color Tokens
**Rule**: Feature UI MUST obtain colors from the design system — `core:design-system` theme tokens
(e.g., `PokedexRed`, `Gray1`, `White`) or `pokemonTypeColor(...)`. Raw `Color(0x…)` literals MUST NOT
appear in feature UI; new colors MUST be added to `core:design-system` as named tokens first.

**Verification**:
- No `androidx.compose.ui.graphics.Color(0x…)` literal in `feature/**/ui/**`.
- Colors referenced are imported from `core.designsystem.theme` or produced by `pokemonTypeColor`.

---

## Rule DESIGNSYS-02: Use Typography Tokens
**Rule**: Text styles MUST come from the design-system typography tokens (e.g., `HeadlineBold`,
`Subtitle1Bold`, `Body2Regular`). Inline `TextStyle(...)` or hardcoded `fontSize`/`fontWeight` in feature
UI MUST NOT be used.

**Verification**:
- No inline `TextStyle(` or `fontSize =`/`fontWeight =` in `feature/**/ui/**`.
- `Text(style = …)` references a token from `core.designsystem.theme`.

---

## Rule DESIGNSYS-03: Use Spacing and Shape Tokens
**Rule**: Spacing and corner shapes MUST use the design-system tokens (`LocalSpacing`/`PokedexTheme.spacing`
and `PokedexShapes`) where a token exists, instead of arbitrary `*.dp` magic numbers.

**Verification**:
- Spacing/shape values map to existing tokens when an equivalent token exists.
- Net-new spacing/shape values are added to the design system rather than scattered as literals.

---

## Rule DESIGNSYS-04: Reuse Shared Components
**Rule**: Standard UI states and primitives MUST reuse existing shared components — `LoadingIndicator`,
`ErrorContent`, `EmptyContent`, `PokemonTypeChip` (`core:design-system`), `PokemonStatBar` (`core:ui`) —
instead of re-implementing them in a feature.

**Verification**:
- Loading/error/empty states use the shared components.
- No feature-local re-implementation of a primitive that already exists in `core:design-system`/`core:ui`.

---

## Rule DESIGNSYS-05: Promote Shared Primitives
**Rule**: A visual primitive used by more than one feature MUST live in `core:design-system` or `core:ui`,
never be duplicated per feature. (Feature-exclusive UiModels remain per-feature.)

**Verification**:
- No duplicated composable primitive across two feature modules.

---

## Enforcement Integration
At Application Design and Code Generation:
- Evaluate every DESIGNSYS rule against the UI artifacts/code produced.
- Include a "Design System Compliance" section in the stage completion summary (compliant/non-compliant/N/A).
- Any non-compliant applicable rule is a blocking finding.
````

## 3.3 O que muda no fluxo quando você liga essa extension

1. **Requirements Analysis**: a pergunta de opt-in (`## Question: Design System Extension`) entra no `requirement-verification-questions.md`. Se você responder **A**, o `design-system-baseline.md` é carregado; a decisão é gravada em `aidlc-docs/aidlc-state.md` na tabela `## Extension Configuration`.
2. **Application Design** e **Code Generation**: ao final de cada um, o modelo inclui uma seção **"Design System Compliance"** e **não** oferece "Continue" enquanto houver violação aplicável.

## 3.4 Aplicando ao que construímos (o validador em ação)

Rodando mentalmente as regras DESIGNSYS sobre a feature do Capítulo 2:

| Regra | Resultado | Evidência |
|---|---|---|
| DESIGNSYS-01 (cores) | ⚠️ **Non-compliant** | `StatComparisonRowItem.kt` usa `val StatWinnerColor = Color(0xFF4CAF50)` — literal de cor em feature UI. Também há `VerticalDivider(color = Color(0xFFE0E0E0))` na `PokemonCompareScreen`. |
| DESIGNSYS-02 (tipografia) | ✅ Compliant | usa `HeadlineBold`, `Subtitle1Bold`, `Subtitle2Bold`, `Body3Regular` (tokens). |
| DESIGNSYS-04 (componentes) | ✅ Compliant | reusa `LoadingIndicator`, `ErrorContent`, `PokemonTypeChip`, `PokemonStatBar`. |
| DESIGNSYS-05 (promover) | ✅ Compliant | nenhum primitivo duplicado entre features. |

**Blocking finding (DESIGNSYS-01):** a cor verde de "vencedor" e o cinza do divisor estão hardcoded. Pela regra, o stage de Code Generation **não** poderia oferecer "Continue" sem resolver.

**Como resolver (seguindo a própria regra):**
- O cinza do divisor (`#E0E0E0`) **já existe** como token: `Gray3` no design system (veja a tabela de Cores do `docs/GUIDE.md`, "divisores, bordas"). A própria regra manda **reusar o token existente** — então basta trocar por `Gray3`.
- O verde de "vencedor" (`#4CAF50`) **não existe** na paleta → é um **token novo**, que deve ser adicionado ao design system antes de usar.

`core/design-system/.../theme/Color.kt` (adicionar só o que falta):
```kotlin
val StatWinner = Color(0xFF4CAF50)   // verde de "vence o stat"
```
E na feature, trocar os literais pelos tokens:
```kotlin
// StatComparisonRowItem.kt
import br.com.pokedex.core.designsystem.theme.StatWinner
color = if (isWinner) StatWinner else baseColor

// PokemonCompareScreen.kt
import br.com.pokedex.core.designsystem.theme.Gray3
VerticalDivider(color = Gray3)        // token existente, não criar um novo
```
Depois disso, DESIGNSYS-01 passa a **compliant** e o stage libera o "Continue".

> Esse é o ponto da extension: ela transforma "boas práticas de design system" em um **gate verificável** dentro do AI-DLC — exatamente como Security e Resiliency já fazem. Você não precisa lembrar de revisar manualmente; a regra é avaliada em cada stage.
>
> 👉 **Mas como aplicar esse fix mantendo as specs/docs atualizadas?** É exatamente o que o [Capítulo 6](06-hands-on-alteracao-mantendo-specs.md) demonstra, passo a passo, pelo ciclo adaptativo do AI-DLC.

## 3.5 Resumo
- Extension = par `<nome>.opt-in.md` + `<nome>.md` em `extensions/<categoria>/`.
- Regras no formato `## Rule <PREFIX-NN>` com **Rule**/**Verification**, **blocking** por padrão.
- Opt-in entra na Requirements Analysis; decisão fica em `aidlc-state.md` → `## Extension Configuration`.
- Enforcement nos stages de UI (Application Design, Code Generation) com seção de compliance.

Próximo: [Capítulo 4 — Supporting Tools](04-ferramentas-de-apoio.md).
