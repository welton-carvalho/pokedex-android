# Roadmap

**Current Milestone:** Comparar Pokémon
**Status:** In Progress

> PokedexLab é um **lab contínuo** (ver PROJECT.md): o roadmap é uma **fila aberta de temas de estudo**, não uma corrida até um v1. Cada "milestone" abaixo é uma faixa de aprendizado (study track) que introduz um padrão/lib. As prioridades do backlog ainda não foram definidas — a ordem é sugestiva, não comprometida.

---

## Base — App Pokédex funcional — COMPLETE

**Goal:** Pédex navegável (lista + detalhe) com arquitetura modular completa, servindo de fundação para todos os experimentos seguintes.
**Target:** Concluído (Fases 1–9, ver `memory/project_implementation_status.md`).

### Features

**Listagem de Pokémon** - COMPLETE
- Grid paginado (Paging 3, 50/página, `cachedIn`)
- Cards no padrão Figma, cores por tipo
- Estados loading/error via `LoadState`

**Detalhe de Pokémon** - COMPLETE
- Artwork oficial, chips de tipo, stat bars, abilities, peso/altura, descrição (species)
- MVI completo (State/Intent/Reducer/Event) + cache ObjectBox (TTL 1h)

**Fundação de arquitetura** - COMPLETE
- 16 módulos, 5 convention plugins, navegação Navigation3 (`core:route:*`)
- DI Koin, observabilidade (Timber/Chucker), design system (tokens Figma)
- `core:testing` + suíte de testes unitários

---

## Comparar Pokémon — IN PROGRESS

**Goal:** Permitir comparar Pokémon lado a lado (stats e tipos), exercitando a criação de uma **feature nova de ponta a ponta** sobre a arquitetura existente — sem mudanças em domínio/dados. Study track conduzido via skill `/tlc-spec-driven` (fluxo Specify→Design→Tasks→Execute).
**Target:** Branch `feat/pokemon-compare-tlc-sdd`. Escopo estimado: **Large** (módulo novo + MVI + navegação + UI).

### Features

**Comparação lado a lado** - IN PROGRESS

Capacidades (nível roadmap — requisitos com IDs serão definidos no Specify):
- Selecionar Pokémon a comparar a partir da listagem (UX de seleção é zona cinzenta → decidir no Discuss)
- Tela de comparação exibindo, por Pokémon: artwork, nome/#, tipos e Base Stats lado a lado
- Comparação visual direta dos status entre os Pokémon selecionados
- Estados `loading` / `success` / `error` / `empty` (regra MVI do CLAUDE.md)

Reúso já disponível no código (ver `.specs/codebase/`):
- `GetPokemonDetailUseCase(id): Result<Pokemon>` (core:domain) — chamado 1× por Pokémon; **nenhuma** mudança em data/domínio
- `PokemonStatBar` (core:ui) para as barras de status
- `PokemonTypeChip` + `pokemonTypeColor(...)` (core:design-system) para tipos

Esboço de arquitetura (a confirmar no Design):
- Novo módulo `feature:pokemon-compare` via convention plugin `pokedexlab.android.feature`
- MVI espelhando `feature:pokemon-detail` (State/Intent/Reducer puro/Event + ViewModel + mapper)
- Nova `PokemonCompareKey : NavKey` em `core:route:keys`; entry registrado em `core:route:navigation` (`AppNavDisplay`)
- Decisão em aberto: `StatUiModel`/label de stats hoje é privado de `pokemon-detail` (features não se conhecem) → duplicar mapper pequeno **ou** promover modelo compartilhado para `core:ui` (registrar como AD no STATE.md ao decidir)

---

## Backlog — temas candidatos (fila aberta, prioridade indefinida)

Cada item é um study track potencial; serão promovidos a "Current Milestone" conforme o interesse.

### Busca + ordenação na listagem — PLANNED
- Search bar e sort (o design system já tem tokens: search bar 16dp, sort card 12dp, sem tela hoje)
- Estudo: filtros sobre Paging, `derivedStateOf`, debounce

### Offline real / estratégias de cache — PLANNED
- `RemoteMediator` (Paging 3) para listagem cacheada
- Diferenciar de fato o `CacheStrategy` (hoje cache de summaries e MEMORY/DISK são inertes — ver CONCERNS #5)
- Estudo: single source of truth, sincronização rede↔banco

### Saúde de testes — PLANNED
- Corrigir `PokemonRepositoryImplTest` (quebrado pós-ObjectBox — CONCERNS #1)
- Cobrir ViewModels, mappers, paging source, `DeepLinkRouter`
- Testes de UI Compose
- Estudo: test doubles, `paging-testing`, Compose UI testing

---

## Future Considerations

- **Deep links de verdade:** adicionar `intent-filter` `pokedex://` no manifest (infra já existe — CONCERNS #4)
- **Threading explícito:** usar `DispatcherProvider` (hoje ocioso) para I/O de ObjectBox/rede (CONCERNS #3)
- **Adaptive / list-detail:** aproveitar `material3-adaptive-navigation3` para layout de duas colunas em tablets
- **R8 / shrinking** no release (hoje desligado — CONCERNS #7)
- **Animações de navegação** (Navigation3 transitions)
- **Telas extras** que exercitem novos padrões: evoluções, movimentos, favoritos
