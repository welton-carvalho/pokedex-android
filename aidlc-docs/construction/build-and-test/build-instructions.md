# Build Instructions

## Prerequisites
- **Build Tool**: Gradle (Kotlin DSL) 9.3.1 via wrapper (`./gradlew`)
- **AGP / Kotlin**: 9.1.1 / 2.2.10
- **JDK**: 17+ (toolchain via foojay resolver)
- **Android SDK**: compileSdk 37 (configurado em `local.properties` → `sdk.dir`)
- **System**: ~2GB RAM livre; conexão para resolver dependências na 1ª build

## Build Steps

### 1. Compilar a feature e módulos afetados
```bash
./gradlew :feature:pokemon-compare:compileDebugKotlin \
          :feature:pokemon-list:compileDebugKotlin \
          :core:route:navigation:compileDebugKotlin \
          :app:compileDebugKotlin --console=plain
```

### 2. Build completo do APK debug
```bash
./gradlew assembleDebug --console=plain
```

### 3. Verificar sucesso
- **Saída esperada**: `BUILD SUCCESSFUL`
- **Artefato**: `app/build/outputs/apk/debug/app-debug.apk`
- **Avisos aceitáveis**: avisos de configuration-cache / deprecation do Gradle.

## Troubleshooting
- **`Cannot add extension with name 'kotlin'`**: nunca aplicar `org.jetbrains.kotlin.android` explicitamente (AGP 9.x gerencia). Ver `docs/GUIDE.md`.
- **`DefinitionOverrideException` (Koin)**: garantir que `GetPokemonDetailUseCase` seja registrado uma única vez (apenas `pokemonDetailModule`); `pokemonCompareModule` só registra o ViewModel.
- **Símbolo do módulo compare não encontrado no `:app`**: confirmar `implementation(project(":feature:pokemon-compare"))` em `app/build.gradle.kts` (dependência via `core:route:navigation` é `implementation` e não vaza).
