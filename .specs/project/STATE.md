# State

**Last Updated:** 2026-06-07
**Current Work:** Comparar Pokémon — **CONCLUÍDO** (commit `af7bf24`). Todas as 17 tasks implementadas e gates passando. Próximo: UAT manual / nova feature.

---

## Recent Decisions (Last 60 days)

### AD-001: PokedexLab é um lab pessoal contínuo (2026-06-07)

**Decision:** Definir o projeto como laboratório de aprendizado pessoal, **sem v1 fechado**. O roadmap é uma fila aberta de temas de estudo; cada feature deve exercitar um padrão/lib distinto.
**Reason:** Resposta do dono do projeto na inicialização SDD (propósito = aprendizado pessoal; horizonte = lab contínuo).
**Trade-off:** Abre mão de uma "definição de pronto" objetiva e de pressão por entrega; o sucesso é medido por aprendizado, não por feature shipping.
**Impact:** Decisões devem favorecer aprendizado sobre simplicidade (ver PROJECT.md). Roadmap organizado em "study tracks", não em releases.

### AD-003: Escopo da feature Comparar Pokémon (2026-06-07)

**Decision:** Comparar **exatamente 2** Pokémon; seleção via **modo seleção na lista** (botão "Comparar" no header); tela espelha o detalhe **sem About/description** (artwork + nome/# + tipos + Base Stats); **realçar o maior valor por status**.
**Reason:** Decisões do dono na fase Specify (detalhe em `.specs/features/pokemon-compare/context.md`).
**Trade-off:** Abre mão de comparar 3+, total geral de stats e troca in-screen (adiados); modo seleção exige alterar `feature:pokemon-list`.
**Impact:** Guia spec → design. Novo `feature:pokemon-compare`, nova `PokemonCompareKey`, sem mudanças em `core:domain`/`data:*`.

### AD-002: Navegação em 3 módulos `core:route:*` (pré-existente, documentado em 2026-06-07)

**Decision:** Navegação dividida em `core:route:keys` (NavKeys + AppNavigator), `core:route:deeplink` (DeepLinkRouter) e `core:route:navigation` (host NavDisplay, agrega nav entries das features).
**Reason:** Manter features desacopladas (dependem só de `keys`) e centralizar a composição em um único módulo que conhece as features.
**Trade-off:** Mais módulos/boilerplate vs. um único `core:navigation`.
**Impact:** `core:route:navigation` é o único módulo autorizado a depender de `feature:*`. CLAUDE.md atualizado para refletir isso.

---

## Active Blockers

### B-001: `PokemonRepositoryImplTest` não compila

**Discovered:** 2026-06-07 (mapeamento brownfield)
**Impact:** `./gradlew :data:repository:test` (e `./gradlew test`) falha no compile — a lógica de cache do repositório está sem teste e o gate global de testes é não confiável.
**Workaround:** nenhum; rodar testes por módulo evitando `data:repository`.
**Resolution:** extrair uma interface `LocalPokemonDataSource` (ou um fake em `core:testing`) para o teste injetar sem `BoxStore` real; atualizar o construtor no teste. Detalhe em `.specs/codebase/CONCERNS.md` #1.

---

## Lessons Learned

### L-001: Documentação divergia do código real

**Context:** Mapeamento brownfield de 2026-06-07.
**Problem:** CLAUDE.md e `memory/project_*.md` descreviam um `core:navigation` com `PokemonListRoute/PokemonDetailRoute` e marcavam fases 3–9 como pendentes — nada disso batia com o código (eram `core:route:*` com `*Key`, e todas as fases estavam concluídas).
**Solution:** Validar tudo contra o código; CLAUDE.md e as memórias foram corrigidos; criada a análise em `.specs/codebase/`.
**Prevents:** Construir features sobre suposições erradas. Regra: tratar docs/memória como pistas, confirmar no código.

---

## Quick Tasks Completed

| #   | Description | Date | Commit | Status |
| --- | ----------- | ---- | ------ | ------ |
| —   | (nenhuma ainda) | — | — | — |

---

## Deferred Ideas

Ideias capturadas durante o trabalho, para features/fases futuras (evita scope creep).

- [ ] Adaptive list-detail (duas colunas em tablet) usando `material3-adaptive-navigation3` — Captured during: brownfield mapping
- [ ] Animações de transição de navegação (Navigation3) — Captured during: brownfield mapping
- [ ] Telas extras para novos padrões: evoluções, movimentos, favoritos — Captured during: roadmap

---

## Todos

Itens de ação que não cabem em uma tarefa ativa (dívida técnica do CONCERNS).

- [ ] Wire deep links: adicionar `intent-filter` `pokedex://` no `AndroidManifest` (CONCERNS #4)
- [ ] Usar `DispatcherProvider` ocioso para I/O de ObjectBox/rede (CONCERNS #3)
- [ ] Remover arquivo morto `app/.../navigation/AppNavGraph.kt` e theme duplicado em `app/.../ui/theme` (CONCERNS #6)
- [ ] Gate de log HTTP por `BuildConfig.DEBUG` (CONCERNS #8)
- [ ] Definir `targetSdk` nos convention plugins (CONCERNS #9)
- [ ] `.gitignore`: `bash.exe.stackdump`, `.agents/`, `.cursor/` (CONCERNS #10)

---

## Preferences

**Model Guidance Shown:** never
