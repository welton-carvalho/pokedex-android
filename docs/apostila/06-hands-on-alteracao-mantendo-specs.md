# Capítulo 6 — Hands-on: alterar mantendo specs e docs atualizados

> Objetivo: aplicar, ponta a ponta, o fix do achado **DESIGNSYS-01** (Cap. 3) — mover cores hardcoded para tokens do Design System — **seguindo o AI-DLC** como uma "alteração na mesma feature" (Cap. 5.4).
>
> O ponto desta apostila inteira culmina aqui: **toda alteração de código tem uma alteração de doc correspondente**, para as specs nunca ficarem desatualizadas. No AI-DLC isso não é opcional — é o que os stages e o `audit.md`/`aidlc-state.md` garantem.

## 6.0 Princípios do AI-DLC que valem aqui

- **Adaptive Workflow**: mudança pequena **pula stages**. Não refazemos toda a Inception.
- **`audit.md` é append-only**: cada interação vira uma entrada nova (nunca sobrescrever).
- **`aidlc-state.md` reflete o progresso** (stages reabertos/concluídos).
- **Brownfield**: modificar arquivos **in-place** (nunca criar `Color_new.kt`).
- **Specs do projeto também contam**: a tabela de Cores do `docs/GUIDE.md` é a **fonte de verdade** dos tokens — adicionar token e **não** atualizar o GUIDE = spec desatualizada.

A mudança concreta:
1. Cinza do divisor `#E0E0E0` → token **já existente** `Gray3` (só trocar).
2. Verde de "vencedor" `#4CAF50` → **token novo** `StatWinner` em `core:design-system` (+ registrar no GUIDE).

---

## Passo 0 — Retomar o fluxo (Workspace Detection / Session Continuity)

**O que o AI-DLC faz:** ao disparar o fluxo, o Workspace Detection lê `aidlc-docs/aidlc-state.md`, vê que já existe e **retoma** (prompt "Welcome back"). Você descreve a alteração:

> "Alteração na feature `pokemon-compare`: resolver DESIGNSYS-01 — mover cores hardcoded (`StatWinnerColor`, cor do `VerticalDivider`) para tokens do Design System."

**Doc tocada:** nenhuma ainda — mas registre a intenção no journal.

`aidlc-docs/audit.md` (append no final):
```markdown
## Change Request — pokemon-compare (DESIGNSYS-01)
**Timestamp**: <ISO>
**User Input**: "Alteração na feature pokemon-compare: mover cores hardcoded para tokens do Design System (fix DESIGNSYS-01)"
**AI Response**: "Workspace Detection retomou o estado existente. Alteração tratada como 'change to same feature' (adaptive). Próximo: Requirements Analysis em profundidade mínima."
**Context**: Resume — change to existing feature pokemon-compare.

---
```

---

## Passo 1 — Requirements Analysis (profundidade mínima)

**O que o AI-DLC faz:** sempre executa, mas em **profundidade mínima** quando a mudança é clara. Aqui só **atualizamos** o `requirements.md` existente — adicionar um NFR de conformidade com o Design System.

`aidlc-docs/inception/requirements/requirements.md` (adicionar na seção Non-Functional):
```markdown
### NFR-5 — Conformidade com o Design System (DESIGNSYS-01)
- Telas/componentes da feature NÃO usam literais de cor (`Color(0x…)`); cores vêm de tokens de
  `core:design-system` ou de `pokemonTypeColor`.
- Cor reutilizável nova é adicionada como token nomeado no design system **antes** de ser usada
  (e registrada na tabela de Cores do `docs/GUIDE.md`).
```

`aidlc-docs/audit.md` (append):
```markdown
## Requirements Analysis — Change (minimal depth)
**Timestamp**: <ISO>
**AI Response**: "Adicionado NFR-5 (conformidade Design System / DESIGNSYS-01) ao requirements.md. Sem novos FRs — comportamento da feature não muda, só a origem das cores."
**Context**: Adaptive change — minimal requirements update.

---
```

> **⛔ Approval gate:** o AI-DLC pede aprovação antes de prosseguir. Aprovado → segue.

---

## Passo 2 — Workflow Planning (adaptive)

**Decisão (e por quê):** é uma mudança de **implementação** + 2 tokens no design system; o comportamento e o desenho de componentes não mudam. Pelo princípio adaptativo:
- **PULAR**: User Stories, Units Generation, Application Design, Functional Design, NFR Design, Infrastructure Design.
- **EXECUTAR**: Code Generation + Build & Test.

`aidlc-docs/aidlc-state.md` (atualizar o status / reabrir stages):
```markdown
## Current Status
- **Lifecycle Phase**: CONSTRUCTION
- **Current Stage**: Change "DESIGNSYS-01 tokens" — Code Generation
- **Scope**: pokemon-compare (UI) + core:design-system (token novo) + docs/GUIDE.md
```

`aidlc-docs/audit.md` (append):
```markdown
## Workflow Planning — Change (adaptive)
**Timestamp**: <ISO>
**AI Response**: "Plano adaptativo: EXECUTE Code Generation + Build & Test; SKIP demais stages (mudança de implementação + tokens). Justificativa registrada."
**Context**: Adaptive change — workflow planning.

---
```

---

## Passo 3 — Code Generation · Parte 1 (plano)

Plano por unidade, curto. `aidlc-docs/construction/plans/pokemon-compare-designsys-fix-plan.md`:
```markdown
# Code Generation Plan — pokemon-compare · DESIGNSYS-01 fix

- [ ] Step 1 — Adicionar token `StatWinner` em core:design-system (Color.kt)
- [ ] Step 2 — `StatComparisonRowItem.kt` usa o token `StatWinner`
- [ ] Step 3 — `PokemonCompareScreen.kt` troca `Color(0xFFE0E0E0)` por `Gray3` (token existente)
- [ ] Step 4 — Atualizar docs/GUIDE.md (tabela de Cores) com `StatWinner`
- [ ] Step 5 — Atualizar code-summary.md da unidade
```
> **⛔ Approval gate** do plano → aprovado.

---

## Passo 4 — Code Generation · Parte 2 (edições in-place + docs)

### Step 1 — token novo no design system
`core/design-system/src/main/kotlin/br/com/pokedex/core/designsystem/theme/Color.kt` (adicionar):
```kotlin
val StatWinner = Color(0xFF4CAF50)   // destaque do maior valor em uma comparação de stat
```

### Step 2 — feature usa o token
`feature/pokemon-compare/.../ui/component/StatComparisonRowItem.kt`:
```kotlin
import br.com.pokedex.core.designsystem.theme.StatWinner   // em vez do val local

@Composable
fun StatComparisonRowItem(stat: CompareStatUiModel, isWinner: Boolean, baseColor: Color, modifier: Modifier = Modifier) {
    PokemonStatBar(
        label = stat.label,
        value = stat.value,
        color = if (isWinner) StatWinner else baseColor,   // token, não literal
        modifier = modifier.fillMaxWidth(),
    )
}
```
> Remova o antigo `val StatWinnerColor = Color(0xFF4CAF50)` do arquivo da feature.

### Step 3 — usar token existente para o divisor
`feature/pokemon-compare/.../ui/screen/PokemonCompareScreen.kt`:
```kotlin
import br.com.pokedex.core.designsystem.theme.Gray3
// ...
VerticalDivider(color = Gray3)   // antes: Color(0xFFE0E0E0)
```
> (Remova o `import androidx.compose.ui.graphics.Color` se ele tiver ficado sem uso.)

### Step 4 — **atualizar a SPEC** (`docs/GUIDE.md`)
Na seção *Design System → Cores*, adicione a linha na tabela:
```markdown
| `StatWinner` | `#4CAF50` | destaque do maior valor por stat (tela de comparação) |
```
> **Este é o passo que mantém a spec viva.** Sem ele, o design system "real" (GUIDE) e o código divergem — o mesmo tipo de drift que a Engenharia Reversa pegou no começo do projeto.

### Step 5 — atualizar o resumo de código da unidade
`aidlc-docs/construction/pokemon-compare/code/code-summary.md` (append):
```markdown
## Alteração — DESIGNSYS-01 (tokens de cor)
- core:design-system Color.kt: + token `StatWinner` (#4CAF50)
- StatComparisonRowItem.kt: usa `StatWinner` (removido literal local)
- PokemonCompareScreen.kt: `VerticalDivider` usa `Gray3` (token existente)
- docs/GUIDE.md: tabela de Cores atualizada com `StatWinner`
```

Marque os checkboxes do plano `[x]` à medida que executa (regra de checkbox enforcement do AI-DLC).

---

## Passo 5 — Build and Test

```bash
./gradlew :core:design-system:compileDebugKotlin \
          :feature:pokemon-compare:compileDebugKotlin --console=plain
./gradlew :feature:pokemon-compare:testDebugUnitTest --console=plain
```
Esperado: `BUILD SUCCESSFUL` (a lógica de comparação não muda; só a origem das cores).

`aidlc-docs/audit.md` (append):
```markdown
## Build and Test — Change
**Timestamp**: <ISO>
**Build Status**: Success
**AI Response**: "Compilação verde após tokenização; testes da feature inalterados e verdes. DESIGNSYS-01 agora compliant."
**Context**: Adaptive change — build & test.

---
```

`aidlc-docs/aidlc-state.md` (marcar concluído):
```markdown
- [x] Change "DESIGNSYS-01 tokens" — Completed <ISO> (code + GUIDE atualizados; compliant)
```

---

## 6.6 A matriz "código → doc" (o coração do capítulo)

Cada arquivo de código tocado teve sua doc/spec correspondente atualizada:

| Mudança de código | Doc/spec atualizada | Por quê |
|---|---|---|
| `Color.kt` + token `StatWinner` | `docs/GUIDE.md` (tabela de Cores) | GUIDE é a fonte de verdade dos tokens |
| `StatComparisonRowItem.kt` / `PokemonCompareScreen.kt` | `construction/pokemon-compare/code/code-summary.md` | registro do que mudou na unidade |
| requisito de conformidade | `inception/requirements/requirements.md` (NFR-5) | a spec funcional/NFR reflete a regra |
| (qualquer interação) | `aidlc-docs/audit.md` (append) | journal cronológico do AI-DLC |
| progresso dos stages | `aidlc-docs/aidlc-state.md` | estado retomável do workspace |

## 6.7 Lição

No AI-DLC, "alterar" = passar a mudança pelo **mesmo ciclo adaptativo** (resume → requirements mínimo → planning → code gen → build & test), e **cada passo deixa rastro nos artefatos**. É isso que impede o drift entre código e specs — exatamente o problema que a Engenharia Reversa detectou no `docs/GUIDE.md` no início deste projeto. Manter os docs atualizados não é uma etapa extra no fim: é parte de cada passo.

← Voltar ao [índice](README.md)
