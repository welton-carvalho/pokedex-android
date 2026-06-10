# Services & Orchestration — Feature: Comparação de Pokémon

Não há camada de "service" separada no projeto; a orquestração vive no **ViewModel** (apresentação) reusando **use cases** do `core:domain`. Esta seção descreve os fluxos de orquestração.

## S1 — Orquestração da comparação (PokemonCompareViewModel)
- **Entrada**: `firstId`, `secondId` (da `PokemonCompareKey`).
- **Use case reutilizado**: `GetPokemonDetailUseCase` (Q9=A) — invocado **uma vez por lado**.
- **Paralelismo**: no `init`, dispara as 2 buscas em paralelo (`viewModelScope.launch { val a = async { useCase(firstId) }; val b = async { useCase(secondId) } ... }`) — cada lado atualiza seu `SideUiState` assim que resolve (independente).
- **Mapeamento**: `Result.Success` → `pokemon.toCompareUiModel()` → `SideUiState.Success`. `Result.Error` → `SideUiState.Error(error)`.
- **Falha parcial (Q2=B)**: cada lado falha/carrega de forma independente; a coluna que falhou mostra erro + retry; a que carregou renderiza normal.
- **Retry por lado**: `Intent.Retry(side)` recarrega **somente** aquele lado (volta a `Loading` e reexecuta o use case do id correspondente).
- **Comparação de stats**: o destaque do maior por stat (`buildStatComparison`) só é computado quando **ambos** os lados estão `Success`; caso contrário, cada coluna exibe seus stats sem marcação de vencedor.
- **Resiliência (RESILIENCY-10/06)**: timeouts e fallback offline já vêm do `PokemonRepository` (cache strategy NETWORK_FIRST); a UI tem estados explícitos loading/success/error por coluna.

## S2 — Orquestração da seleção (PokemonListViewModel)
- **Ativação**: `ToggleSelectionMode` (ação "Comparar" no header) liga/desliga o modo; ao desligar, limpa `selectedIds`.
- **Seleção**: no modo seleção, toque no card → `ToggleSelection(id)`; o reducer adiciona até 2 ou remove; tentar um 3º (size==2 e id novo) → ViewModel emite `ShowSelectionLimit` (snackbar) sem alterar estado.
- **Disparo**: botão "Comparar (2)" habilitado só com 2 selecionados → `ClickCompare` → ViewModel emite `NavigateToCompare(id1, id2)` e **limpa** a seleção/modo (Q4=A).
- **Fora do modo seleção**: toque no card mantém o comportamento atual → `ClickPokemon(id)` → `NavigateToDetail`.

## S3 — Navegação e Deep Link (core:route)
- **In-app**: `pokemonListEntry` → `navigator.navigateTo(PokemonCompareKey(a, b))`. `pokemonCompareEntry` resolve `PokemonCompareScreen(firstId, secondId, onBack = navigator::navigateBack)`.
- **Deep link**: `DeepLinkRouter.fromUrl("pokedex://compare/{id1}/{id2}")` → valida 2 ids (`toIntOrNull`, `> 0`) → `PokemonCompareKey`; `fromIntent` retorna backstack sintético `[PokemonListKey, PokemonCompareKey]` (mesmo padrão do detalhe). Ids inválidos → `null` (não resolve) — SECURITY-05.

## DI (Koin) — decisão registrada
- `pokemonCompareModule` registra **apenas** o `PokemonCompareViewModel`.
- `GetPokemonDetailUseCase` **não** é re-registrado pelo módulo de comparação (já é registrado por `pokemonDetailModule`); o `get()` o resolve do grafo Koin plano. Isso evita `DefinitionOverrideException` e mantém `core:domain` livre de Koin (convenção atual).
- **Trade-off documentado**: a comparação depende, em runtime, de o app carregar um módulo que registre `GetPokemonDetailUseCase` (hoje `pokemonDetailModule`, sempre carregado pelo `:app`). Se no futuro se desejar independência estrita, extrair um módulo Koin de domínio compartilhado.
- `:app` `startKoin` passa a incluir `pokemonCompareModule`.
