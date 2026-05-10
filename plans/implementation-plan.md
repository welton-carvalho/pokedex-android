# PokedexLab — Plano de Implementação

> Última atualização: 2026-05-10
> Retomar a partir da **Fase 3**.

---

## Contexto

Aplicativo Android de laboratório com tema Pokémon. Arquitetura: Clean Architecture + MVI + Modularização.
Stack completa documentada em `CLAUDE.md` e `memory/project_stack_versions.md`.

Todos os 16 módulos estão declarados e compilando (`assembleDebug` BUILD SUCCESSFUL).
As Fases 1 e 2 estão completas — a próxima sessão começa na Fase 3.

---

## Status das Fases

| Fase | Status | Resumo |
|---|---|---|
| 1 — Fundação | ✅ Concluída | build-logic, libs.versions.toml, 16 módulos |
| 2 — Core modules | ✅ Concluída | model, common, domain, navigation, observability, design-system, ui |
| 3 — Data layer | ✅ Concluída | network (Retrofit+Paging), local (in-memory cache), repository |
| 4 — App Shell | ✅ Concluída | Application, Splash, Navigation3 host (NavDisplay) |
| 5 — feature:pokemon-list | ✅ Concluída | MVI completo + Paging3 + LazyVerticalGrid |
| 6 — feature:pokemon-detail | ✅ Concluída | MVI + artwork + stats + type colors |
| 7 — core:testing | ✅ Concluída | FakePokemonRepository, FakePokemonData, TestDispatcherProvider, MainDispatcherRule |
| 8 — Testes | ✅ Concluída | 20 testes (ErrorHandler, UseCases, RepositoryImpl, Reducer) |
| 9 — Polish | ✅ Concluída | @Immutable, Coil cache, proguard |

---

## Fase 3 — Data Layer (próxima)

### 3.1 — data:network

**Pacote base:** `br.com.pokedex.data.network`

Criar:
- `dto/PokemonListResponseDto.kt` — wrapper da resposta paginada (`count`, `next`, `previous`, `results`)
- `dto/PokemonListItemDto.kt` — `name: String`, `url: String` (extrair ID da URL)
- `dto/PokemonDetailDto.kt` — `id`, `name`, `height`, `weight`, `types`, `stats`, `abilities`
- `dto/TypeSlotDto.kt`, `StatDto.kt`, `AbilitySlotDto.kt`
- `api/PokemonApiService.kt` — interface Retrofit
  ```kotlin
  @GET("pokemon/")
  suspend fun getPokemonList(@Query("offset") offset: Int, @Query("limit") limit: Int): PokemonListResponseDto

  @GET("pokemon/{id}")
  suspend fun getPokemonDetail(@Path("id") id: Int): PokemonDetailDto
  ```
- `source/RemotePokemonDataSource.kt` — encapsula chamadas, converte exceções via `ErrorHandler`
- `paging/PokemonRemotePagingSource.kt` — `PagingSource<Int, PokemonSummary>` com page size 50
- `di/NetworkModule.kt` — Koin `single` para OkHttpClient, Retrofit, ApiService, DataSource

**Detalhes de configuração Retrofit:**
```kotlin
Retrofit.Builder()
    .baseUrl("https://pokeapi.co/api/v2/")
    .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()))
    .client(okHttpClient)
    .build()
```

OkHttpClient deve incluir:
- `ChuckerInterceptor` (debug) via `core:observability`
- `HttpLoggingInterceptor` (debug)
- timeout de 30s

---

### 3.2 — data:local

**Pacote base:** `br.com.pokedex.data.local`

Criar:
- `entity/PokemonSummaryEntity.kt` — `@Entity` ObjectBox com `id`, `name`
- `entity/PokemonDetailEntity.kt` — `@Entity` ObjectBox com todos os campos (types/stats como JSON string)
- `source/LocalPokemonDataSource.kt` — leitura e escrita via ObjectBox Box<T>
- `di/LocalModule.kt` — Koin `single` para `BoxStore`, `Box<PokemonSummaryEntity>`, `Box<PokemonDetailEntity>`

**Init do BoxStore** ficará no `:app` via `MyObjectBox.builder().androidContext(this).build()`.

---

### 3.3 — data:repository

**Pacote base:** `br.com.pokedex.data.repository`

Criar:
- `CacheStrategy.kt` — enum: `MEMORY, DISK, NETWORK_FIRST, CACHE_FIRST, NETWORK_ONLY`
- `mapper/PokemonDtoMapper.kt` — `PokemonDetailDto → Pokemon`
- `mapper/PokemonEntityMapper.kt` — `PokemonSummaryEntity → PokemonSummary`, `PokemonDetailEntity → Pokemon`
- `PokemonRepositoryImpl.kt` — implementa `PokemonRepository` de `core:domain`
  - `getPokemonList()` → retorna `Pager(PokemonRemotePagingSource).flow`
  - `getPokemonDetail(id)` → `NETWORK_FIRST` por default: tenta remote, salva local, fallback local
- `di/RepositoryModule.kt` — Koin `single<PokemonRepository> { PokemonRepositoryImpl(...) }`

---

## Fase 4 — App Shell

**Pacote base:** `br.com.pokedex`

Criar:
- `PokedexLabApplication.kt`
  ```kotlin
  class PokedexLabApplication : Application() {
      override fun onCreate() {
          super.onCreate()
          AppLogger.init(BuildConfig.DEBUG)
          MyObjectBox.builder().androidContext(this).build()  // ObjectBox
          startKoin {
              androidContext(this@PokedexLabApplication)
              modules(commonModule, networkModule, localModule, repositoryModule,
                      pokemonListModule, pokemonDetailModule)
          }
      }
  }
  ```
- Registrar `PokedexLabApplication` no `AndroidManifest.xml`
- Configurar Splash Screen API no `AndroidManifest.xml` (tema `Theme.SplashScreen`)
- `MainActivity.kt` — inicializa splash + hospeda `NavDisplay` do Navigation3
- `navigation/AppNavGraph.kt` — mapeia `PokemonListRoute` → `PokemonListScreen`, `PokemonDetailRoute` → `PokemonDetailScreen`

---

## Fase 5 — feature:pokemon-list

**Pacote base:** `br.com.pokedex.feature.pokemonlist`

### Estrutura completa
```
ui/
  component/
    PokemonCard.kt         # 104x108dp, 3 colunas, Figma node 1017:431
    PokemonLoadingCard.kt  # shimmer placeholder
  model/
    PokemonListUiModel.kt  # id, displayName, imageUrl
  screen/
    PokemonListScreen.kt   # LazyVerticalGrid 3 colunas
  preview/
    PokemonListPreview.kt
  state/
    PokemonListState.kt    # loading: Boolean, error: DomainError?, isEmpty: Boolean
  intent/
    PokemonListIntent.kt   # Retry, ClickPokemon(id)
  reducer/
    PokemonListReducer.kt
  event/
    PokemonListEvent.kt    # NavigateToDetail(id)
viewmodel/
  PokemonListViewModel.kt
mapper/
  PokemonListUiMapper.kt   # PokemonSummary → PokemonListUiModel
di/
  PokemonListModule.kt
```

### PokemonCard — spec Figma
- Container: `104x108dp`, `white`, `8dp` radius, drop shadow 2dp
- Imagem: `72x72dp` topo, official artwork via Coil
- Número: `CaptionRegular` (8sp), `Gray2`, canto superior direito
- Nome: `Body3Regular` (10sp), `Gray1`, centralizado em fundo `GrayBackground` com `7dp` radius
- Grid: 3 colunas, gap `8dp`, padding horizontal `12dp`

### MVI
```kotlin
data class PokemonListState(
    val isLoading: Boolean = false,
    val error: DomainError? = null,
)

sealed interface PokemonListIntent {
    data object Retry : PokemonListIntent
    data class ClickPokemon(val id: Int) : PokemonListIntent
}

sealed interface PokemonListEvent {
    data class NavigateToDetail(val id: Int) : PokemonListEvent
}
```

`LazyPagingItems` é gerenciado diretamente pelo ViewModel via `getPokemonListUseCase().cachedIn(viewModelScope)`.

### Header — spec Figma
- Background: `PokedexRed`
- Pokéball icon + "Pokédex" em `HeadlineBold` branco
- Search bar: branco, `16dp` radius, ícone de busca
- Sort button: branco, `16dp` radius, ícone de tag

---

## Fase 6 — feature:pokemon-detail

**Pacote base:** `br.com.pokedex.feature.pokemondetail`

Mesma estrutura interna do pokemon-list.

### Tela — spec Figma (node 1016:1461)
- Background da tela: cor do primeiro tipo do Pokémon (`pokemonTypeColor(types[0].name)`)
- Header: back button + nome (`HeadlineBold`, branco) + número (`Subtitle2Bold`, branco)
- Pokéball watermark: 10% opacity no background
- Artwork: `200x200dp`, centralizado
- Card branco: `8dp` radius, `top=56dp` overlap sobre a imagem, padding `20dp`
- Type chips: `Subtitle3Bold` branco, `10dp` radius, padding `8dp × 2dp`
- Seções: About (descrição, peso, altura, habilidades) e Base Stats (barras com `PokemonStatBar`)
- Stat labels: HP, ATK, DEF, SATK, SDEF, SPD — `Subtitle3Bold`, cor do tipo
- Divider: `Gray3`, 1dp vertical

---

## Fase 7 — core:testing

**Pacote base:** `br.com.pokedex.core.testing`

Criar:
- `fake/FakePokemonRepository.kt` — implementa `PokemonRepository`, retorna dados fixos
- `fake/FakePokemonData.kt` — objetos `Pokemon` e `PokemonSummary` prontos para testes
- `dispatcher/TestDispatcherProvider.kt` — usa `UnconfinedTestDispatcher`
- `rule/MainDispatcherRule.kt` — JUnit 5 extension que seta `Dispatchers.Main`

---

## Fase 8 — Testes

### Unit tests (JUnit 5 + MockK)
- `PokemonListReducerTest` — testa cada intent → state transition
- `GetPokemonListUseCaseTest` — verifica delegação ao repositório
- `GetPokemonDetailUseCaseTest`
- `PokemonRepositoryImplTest` — com `FakePokemonRemoteDataSource` e `FakeLocalDataSource`
- `ErrorHandlerTest` — cada tipo de exceção → DomainError correto

### Integration tests
- `PokemonRepositoryImplIntegrationTest` — datasources reais em memória

### UI tests (Compose)
- `PokemonListScreenTest` — verifica loading, lista, erro, retry
- `PokemonDetailScreenTest` — verifica artwork, tipo, stats

---

## Fase 9 — Polish

- Adicionar `@Immutable` a todos os UiModels
- `@Stable` em classes que não são data class mas têm state estável
- Coil: configurar `MemoryCache` e `DiskCache` no `Application`
- Paging `prefetchDistance = 10`
- Proguard rules para Retrofit, Koin, ObjectBox, Coil
- Chucker somente no `debugImplementation`

---

## Arquivos-chave já existentes

| Arquivo | Módulo |
|---|---|
| `core/model/src/.../Pokemon.kt` | core:model |
| `core/common/src/.../result/Result.kt` | core:common |
| `core/common/src/.../result/DomainError.kt` | core:common |
| `core/common/src/.../error/ErrorHandler.kt` | core:common |
| `core/common/src/.../dispatcher/DispatcherProvider.kt` | core:common |
| `core/common/src/.../di/CommonModule.kt` | core:common |
| `core/domain/src/.../repository/PokemonRepository.kt` | core:domain |
| `core/domain/src/.../usecase/GetPokemonListUseCase.kt` | core:domain |
| `core/domain/src/.../usecase/GetPokemonDetailUseCase.kt` | core:domain |
| `core/navigation/src/.../Routes.kt` | core:navigation |
| `core/observability/src/.../AppLogger.kt` | core:observability |
| `core/design-system/src/.../theme/Color.kt` | core:design-system |
| `core/design-system/src/.../theme/Type.kt` | core:design-system |
| `core/design-system/src/.../theme/Shape.kt` | core:design-system |
| `core/design-system/src/.../theme/Spacing.kt` | core:design-system |
| `core/design-system/src/.../theme/Theme.kt` | core:design-system |
| `core/design-system/src/.../component/PokemonTypeChip.kt` | core:design-system |
| `core/design-system/src/.../component/LoadingIndicator.kt` | core:design-system |
| `core/design-system/src/.../component/ErrorContent.kt` | core:design-system |
| `core/design-system/src/.../component/EmptyContent.kt` | core:design-system |
| `core/design-system/src/.../component/ShimmerBox.kt` | core:design-system |
| `core/design-system/src/.../util/PokemonImageUrlProvider.kt` | core:design-system |
| `core/ui/src/.../PokemonStatBar.kt` | core:ui |
| `build-logic/convention/src/.../AndroidExtensions.kt` | build-logic |
| `build-logic/convention/src/.../AndroidApplicationConventionPlugin.kt` | build-logic |
| `build-logic/convention/src/.../AndroidLibraryConventionPlugin.kt` | build-logic |
| `build-logic/convention/src/.../AndroidComposeConventionPlugin.kt` | build-logic |
| `build-logic/convention/src/.../AndroidFeatureConventionPlugin.kt` | build-logic |
| `build-logic/convention/src/.../KotlinAndroidConventionPlugin.kt` | build-logic |

---

## Verificação antes de continuar

```bash
# Deve retornar BUILD SUCCESSFUL antes de iniciar a Fase 3
./gradlew assembleDebug --no-daemon
```
