# PokedexLab — CLAUDE.md

Laboratório de arquitetura Android moderno usando Jetpack Compose e tema Pokémon.
Este arquivo é a fonte de verdade para decisões arquiteturais, padrões e restrições do projeto.

---

## Stack

| Camada | Tecnologia | Versão |
|---|---|---|
| Build system | Gradle (Kotlin DSL) | 9.3.1 |
| AGP | Android Gradle Plugin | 9.1.1 |
| Linguagem | Kotlin | 2.2.10 |
| UI | Jetpack Compose + Material3 | BOM 2026.02.01 |
| Arquitetura | Clean Architecture + MVI | — |
| DI | Koin | 4.2.1 |
| Network | Retrofit | 3.0.0 |
| Imagens | Coil | 3.4.0 |
| Banco local | ObjectBox | 5.4.2 |
| Paginação | Paging 3 | 3.3.6 |
| Navegação | Navigation3 | 1.1.1 |
| Logging | Timber | 5.0.1 |
| Network inspector | Chucker | 4.0.0 |
| Testes unit | JUnit 5 + MockK | 5.11.0 / 1.13.12 |
| Testes UI | Compose UI Tests | — |

**SDK:**
- `minSdk = 26`
- `targetSdk = 36`
- `compileSdk = 37` ← obrigatório; `material3-adaptive-navigation3:1.3.0-beta01` exige 37

---

## Estrutura de Módulos

```
app                         # APK/AAB, Koin init, splash, navegação raiz

build-logic/
└── convention/             # Convention plugins Gradle

core/
├── common                  # DomainError, Result<T>, ErrorHandler, DispatcherProvider
├── design-system           # Tema, cores, tipografia, shapes, componentes base
├── domain                  # Interfaces de repositório, use cases
├── model                   # Modelos de domínio compartilhados
├── navigation              # Rotas serializáveis (PokemonListRoute, PokemonDetailRoute)
├── observability           # AppLogger (Timber), Chucker
├── testing                 # Fakes, TestDispatcherProvider, helpers de teste
└── ui                      # Componentes visuais compartilhados entre features

data/
├── network                 # Retrofit, APIs, DTOs, RemoteDataSource
├── local                   # ObjectBox, entities, LocalDataSource
└── repository              # PokemonRepositoryImpl, CacheStrategy, mappers

feature/
├── pokemon-list            # Tela de listagem com Paging3 e MVI
└── pokemon-detail          # Tela de detalhe com MVI
```

---

## Grafo de Dependências

```
app → feature:*, core:navigation, core:observability, core:design-system, data:*

feature:* → core:common, core:design-system, core:domain, core:model,
            core:navigation, core:ui, data:repository

data:repository → core:domain, core:model, data:network, data:local, core:common
data:network    → core:model, core:common, core:observability
data:local      → core:model
core:domain     → core:model, core:common
core:ui         → core:design-system, core:common
core:testing    → core:domain, core:model, core:common
```

**Regra absoluta:** features NÃO se conhecem. `feature:pokemon-list` nunca importa nada de `feature:pokemon-detail` e vice-versa. Comunicação somente via `core:navigation`.

---

## Convention Plugins

Definidos em `build-logic/convention/src/main/kotlin/`.

| Plugin | ID | Uso |
|---|---|---|
| `AndroidApplicationConventionPlugin` | `pokedexlab.android.application` | módulo `:app` |
| `AndroidLibraryConventionPlugin` | `pokedexlab.android.library` | qualquer biblioteca Android |
| `AndroidFeatureConventionPlugin` | `pokedexlab.android.feature` | feature modules |
| `AndroidComposeConventionPlugin` | `pokedexlab.android.compose` | adiciona Compose a library |
| `KotlinAndroidConventionPlugin` | `pokedexlab.kotlin.android` | configura Kotlin compiler |

### Regra crítica — AGP 9.x + Kotlin 2.x

**NUNCA aplique `org.jetbrains.kotlin.android` explicitamente.** O AGP 9.x registra a extensão `kotlin` internamente para todos os módulos Android. Aplicar o plugin manualmente lança `IllegalArgumentException: Cannot add extension with name 'kotlin'`.

Padrão correto nos convention plugins:
```kotlin
// Biblioteca sem Compose
pluginManager.apply("com.android.library")          // AGP gerencia Kotlin

// Biblioteca com Compose
pluginManager.apply("com.android.library")
pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

// Application
pluginManager.apply("com.android.application")
pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
```

---

## Padrão MVI

Fluxo obrigatório em todos os feature modules:

```
UI Screen → Intent → ViewModel → Reducer → UseCase → Repository → State Update → UI
                                         ↘ Event (navegação, snackbar)
```

Estrutura interna de cada feature:

```
feature:xxx/
├── ui/
│   ├── component/    # Composables reutilizáveis da feature
│   ├── model/        # UiModel exclusivo da feature (não compartilhar com outras features)
│   ├── screen/       # Composable de tela — só renderiza state e dispara intents
│   ├── preview/      # @Preview functions
│   ├── state/        # data class XxxState
│   ├── intent/       # sealed interface XxxIntent
│   ├── reducer/      # XxxReducer — função pura: (State, Intent) → State
│   └── event/        # sealed interface XxxEvent — efeitos efêmeros
├── viewmodel/        # XxxViewModel — coordena tudo, expõe StateFlow
├── mapper/           # Domain → UiModel
└── di/               # Koin module da feature
```

Estados obrigatórios: `loading`, `success`, `error`, `empty`.

---

## Navegação

Usar Navigation3 com rotas type-safe serializáveis definidas em `core:navigation`:

```kotlin
@Serializable data object PokemonListRoute
@Serializable data class PokemonDetailRoute(val pokemonId: Int)
```

Features nunca criam rotas próprias — sempre usam as de `core:navigation`.

---

## Design System

Tokens extraídos do Figma (arquivo `ZhswWnNVkMM5mjIYB1M554`).

### Cores (`core/design-system/.../theme/Color.kt`)

| Token | Hex | Uso |
|---|---|---|
| `PokedexRed` | `#DC0A2D` | cor primária, header |
| `Gray1` | `#212121` | texto principal |
| `Gray2` | `#666666` | texto secundário |
| `Gray3` | `#E0E0E0` | divisores, bordas |
| `GrayBackground` | `#EFEFEF` | background de telas |
| `White` | `#FFFFFF` | cards, surface |

18 cores de tipo Pokémon via `pokemonTypeColor(typeName: String): Color`.

### Tipografia (`Type.kt`) — Poppins

| Token | Tamanho | Peso | Line Height |
|---|---|---|---|
| `HeadlineBold` | 24sp | Bold | 32sp |
| `Subtitle1Bold` | 14sp | Bold | 16sp |
| `Subtitle2Bold` | 12sp | Bold | 16sp |
| `Subtitle3Bold` | 10sp | Bold | 16sp |
| `Body1Regular` | 14sp | Normal | 16sp |
| `Body2Regular` | 12sp | Normal | 16sp |
| `Body3Regular` | 10sp | Normal | 16sp |
| `CaptionRegular` | 8sp | Normal | 12sp |

### Shapes

| Token | Radius | Uso |
|---|---|---|
| `extraSmall` | 7dp | name tag do card |
| `small` | 8dp | cards, content areas |
| `medium` | 12dp | sort card |
| `large` | 16dp | search bar, botões |
| `TypeChipShape` | 10dp | chips de tipo |

### Spacing (`LocalSpacing`)

`xxs=2dp` · `xs=4dp` · `s=8dp` · `m=12dp` · `l=16dp` · `xl=20dp` · `xxl=24dp` · `xxxl=56dp`

---

## Imagens

```kotlin
PokemonImageUrlProvider.officialArtwork(id: Int): String
// → https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/{id}.png
```

---

## API

```
Base: https://pokeapi.co/api/v2/

Listagem: GET /pokemon/?offset={offset}&limit=50
Detalhe:  GET /pokemon/{id}
```

---

## Cache

Estratégia configurável em runtime via enum em `data:repository`:

```kotlin
enum class CacheStrategy { MEMORY, DISK, NETWORK_FIRST, CACHE_FIRST, NETWORK_ONLY }
```

---

## Injeção de Dependência (Koin)

- Usar `single` por padrão
- Usar `factory` apenas quando o objeto não deve ser singleton
- ViewModels via `viewModel { }` DSL do Koin Compose
- Cada módulo Gradle expõe seu próprio Koin module
- O `:app` centraliza o `startKoin` e carrega todos os módulos

Localização dos módulos Koin:
```
core:common      → commonModule
data:network     → networkModule
data:local       → localModule
data:repository  → repositoryModule
feature:*        → featureModule (registrado no di/ de cada feature)
```

---

## Paginação

- Paging 3, 50 itens por página
- `cachedIn(viewModelScope)` obrigatório no ViewModel
- `collectAsLazyPagingItems()` na tela
- `LazyColumn` com `key` estável e `contentType`

---

## Error Handling

Features **nunca** capturam exceções diretamente. Exceptions são convertidas para `DomainError` pelo `ErrorHandler` em `core:common`:

```kotlin
sealed interface DomainError {
    data object Network : DomainError
    data object Timeout : DomainError
    data object Unauthorized : DomainError
    data object NotFound : DomainError
    data object Unknown : DomainError
}
```

---

## Performance — Regras Compose

- Modelos de UI devem ser `@Immutable` ou `@Stable`
- Usar `remember` e `derivedStateOf` para evitar recomposições
- Coil: configurar memory cache + disk cache
- Paging: ajustar `prefetchDistance`
- `LazyColumn`: `key` estável + `contentType`

---

## Testes

- **Unit:** JUnit 5, MockK, `TestDispatcherProvider` de `core:testing`
- **Integration:** repositório com datasources reais em memória
- **UI:** Compose UI Tests
- Cobrir obrigatoriamente: reducers, use cases, repositório

---

## Nomenclatura

- Todos os packages, classes, métodos e arquivos em **inglês**
- Packages seguem o padrão `br.com.pokedex.<módulo>.<camada>`
- Exemplos: `br.com.pokedex.feature.pokemonlist.ui.screen`, `br.com.pokedex.data.network.dto`

---

## Comandos Úteis

```bash
# Verificar se tudo compila
./gradlew assembleDebug

# Build de um módulo específico
./gradlew :core:design-system:assembleDebug

# Testes unitários
./gradlew test

# Sync rápido (sem compilar)
./gradlew help
```
