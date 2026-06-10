# Capítulo 1 — Setup e Fundamentos

> Objetivo: deixar o ambiente pronto e entender as **convenções** que tornam o código previsível. Sem isso, o hands-on do Capítulo 2 vira tentativa e erro.

## 1.1 Stack (resumo do `docs/GUIDE.md`)

| Camada | Tecnologia | Versão |
|---|---|---|
| Build | Gradle (Kotlin DSL) | 9.3.1 |
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
| Testes | JUnit 5 + MockK | 5.11.0 / 1.13.12 |

**SDK:** `minSdk 26`, `targetSdk 36`, `compileSdk 37` (obrigatório por causa de `material3-adaptive-navigation3`).

## 1.2 Estrutura de módulos

```
app                         # APK, Koin init (startKoin), MainActivity
build-logic/convention/     # convention plugins Gradle (config compartilhada)
core/
├── common                  # Result<T>, DomainError, ErrorHandler, DispatcherProvider
├── design-system           # tema, cores, tipografia, shapes, componentes base
├── domain                  # interfaces de repositório, use cases
├── model                   # modelos de domínio (Pokemon, ...)
├── observability           # AppLogger (Timber), Chucker
├── testing                 # fakes, TestDispatcherProvider, regras de teste
├── ui                      # componentes visuais compartilhados (PokemonStatBar)
└── route/
    ├── keys                # NavKeys serializáveis + interface AppNavigator
    ├── navigation          # AppNavDisplay (monta NavDisplay + entries das features)
    └── deeplink            # DeepLinkRouter (Intent/URL → NavKey)
data/
├── network                 # Retrofit, DTOs, RemoteDataSource, paging source
├── local                   # ObjectBox, entities, LocalDataSource
└── repository              # PokemonRepositoryImpl, CacheStrategy, mappers
feature/
├── pokemon-list            # listagem (Paging3 + MVI)
├── pokemon-detail          # detalhe (MVI)
└── pokemon-compare         # ← vamos CRIAR este no Cap. 2
```

## 1.3 As 5 convenções que você precisa respeitar

1. **MVI unidirecional** em toda feature:
   ```
   Screen → Intent → ViewModel → Reducer (puro) → State → Screen
                                ↘ Event (navegação, snackbar) — efêmero
   ```
   Estrutura interna de cada feature: `ui/{state,intent,reducer,event,model,screen,component,preview}`, `viewmodel/`, `mapper/`, `navigation/`, `di/`.

2. **Features NÃO se conhecem.** `feature:pokemon-list` nunca importa de `feature:pokemon-detail`. A comunicação é só por **NavKeys** em `core:route:keys`. Quem "costura" as features é o módulo de montagem `core:route:navigation`.
   - Corolário: cada feature tem seu **próprio UiModel** (não compartilhe UiModel entre features).

3. **Reducer é função pura** `(State, Intent) → State`. Efeitos colaterais (navegar, snackbar, I/O) ficam no ViewModel (via `Event` ou chamada de use case). Isso torna o reducer 100% testável sem Android.

4. **Convention plugins** centralizam build. Um módulo de feature tem só isto no topo do `build.gradle.kts`:
   ```kotlin
   plugins { alias(libs.plugins.pokedexlab.android.feature) }
   ```
   ⚠️ **Nunca** aplique `org.jetbrains.kotlin.android` manualmente — o AGP 9.x já registra a extensão `kotlin`; aplicar de novo lança `IllegalArgumentException: Cannot add extension with name 'kotlin'`.

5. **Erros viram `DomainError`.** Features não capturam exceções; o repositório retorna `Result<T>` (`Success`/`Error(DomainError)`). A UI sempre tem estados `loading/success/error/empty`.

## 1.4 Rodando o projeto

```bash
# Sincroniza/baixa dependências e compila tudo
./gradlew assembleDebug

# Testes unitários
./gradlew test

# Compilar um módulo específico (mais rápido durante o hands-on)
./gradlew :feature:pokemon-list:compileDebugKotlin
```

Pré-requisitos de ambiente:
- `local.properties` com `sdk.dir=...` apontando para o Android SDK.
- JDK 17+ (o toolchain é resolvido pelo foojay plugin).

## 1.5 De onde partimos: lista e detalhe

A feature de comparação **reaproveita** dois pilares já existentes:

- **`GetPokemonDetailUseCase`** (em `core:domain`): `suspend operator fun invoke(id: Int): Result<Pokemon>`. É a mesma fonte que a tela de detalhe usa — com cache + fallback offline do repositório.
- **Componentes visuais**: `PokemonTypeChip` (design-system), `PokemonStatBar` (core:ui), `LoadingIndicator`/`ErrorContent` (design-system).

E vamos espelhar o padrão das features existentes:
- `PokemonDetailViewModel` mostra como carregar 1 Pokémon e mapear para UiModel.
- `PokemonListViewModel` mostra `StateFlow` + `Channel<Event>`.
- `PokemonDetailNavEntry`/`PokemonListNavEntry` mostram como registrar uma tela no grafo.

> ✅ **Checkpoint do Capítulo 1:** rode `./gradlew assembleDebug` e confirme `BUILD SUCCESSFUL`. Se falhar aqui, resolva o ambiente antes de seguir — o Capítulo 2 assume um build verde.

Próximo: [Capítulo 2 — Hands-on](02-hands-on-construindo-a-feature.md).
