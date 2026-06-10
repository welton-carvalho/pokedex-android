# Capítulo 2 — Hands-on: construindo a feature de comparação

> Premissa: a feature **não existe**. Vamos criá-la do zero. Cada bloco tem **caminho do arquivo + código completo + porquê**. Os checkpoints **⏸️** mandam você compilar/testar antes de seguir.
>
> Rastreabilidade: os requisitos (FR-x) vêm de `aidlc-docs/inception/requirements/requirements.md`; o desenho, de `aidlc-docs/inception/application-design/`.

## 2.0 Ordem de construção (por dependência de build)

```
A) rota/scaffolding → B) modelos/estado → C) reducer + comparador (com testes)
→ D) mapper + viewmodel → E) UI → F) navegação/deeplink/DI
→ G) seleção na lista → H) app + build + testes
```
Construímos "de baixo para cima" para que cada etapa **compile** antes da próxima.

---

## 2.1 Módulo A — Fundações de rota

### A1. Registrar o novo módulo Gradle
`settings.gradle.kts` (adicione na seção de features):
```kotlin
// Feature modules
include(":feature:pokemon-list")
include(":feature:pokemon-detail")
include(":feature:pokemon-compare")   // ← novo
```

### A2. `build.gradle.kts` do módulo
`feature/pokemon-compare/build.gradle.kts` — espelha o `pokemon-detail` (é uma tela de detalhe dupla):
```kotlin
plugins {
    alias(libs.plugins.pokedexlab.android.feature)   // 1 plugin só (convention)
}

android {
    namespace = "br.com.pokedex.feature.pokemoncompare"
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.koin.compose)
    implementation(libs.bundles.navigation3)
    implementation(libs.bundles.coil)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.compose.material.icons)

    implementation(project(":core:route:keys"))   // só as KEYS (isolamento!)
    implementation(project(":core:common"))
    implementation(project(":core:design-system"))
    implementation(project(":core:domain"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":data:repository"))

    debugImplementation(libs.bundles.compose.debug)

    testImplementation(project(":core:testing"))
    testImplementation(libs.bundles.junit5)
    testImplementation(libs.mockk.android)
    testImplementation(libs.mockk.agent)
    testImplementation(libs.kotlinx.coroutines.test)
    testRuntimeOnly(libs.junit5.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}
```
> **Por quê só `core:route:keys`?** Porque a feature só precisa **anunciar** sua rota (a NavKey). Quem monta o grafo é `core:route:navigation` (convenção 2 do Cap. 1).

### A3. Declarar a rota (NavKey)
`core/route/keys/src/main/kotlin/br/com/pokedex/core/route/keys/RouteKeys.kt` — adicione:
```kotlin
@Serializable
data class PokemonCompareKey(val firstId: Int, val secondId: Int) : NavKey
```
> Contrato type-safe entre quem navega (lista/deeplink) e quem recebe (a tela). **FR-8**.

⏸️ **Pare e teste:**
```bash
./gradlew :core:route:keys:compileDebugKotlin
```
(O módulo compare ainda está vazio; tudo bem.)

---

## 2.2 Módulo B — Modelos e estado

### B1. UiModels da feature
`feature/pokemon-compare/src/main/kotlin/br/com/pokedex/feature/pokemoncompare/ui/model/ComparePokemonUiModel.kt`:
```kotlin
package br.com.pokedex.feature.pokemoncompare.ui.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class ComparePokemonUiModel(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val typeColor: Color,
    val types: List<String>,
    val height: String,
    val weight: String,
    val abilities: List<String>,
    val description: String,
    val stats: List<CompareStatUiModel>,
)

@Immutable
data class CompareStatUiModel(val label: String, val value: Int)

enum class StatWinner { FIRST, SECOND, TIE }

@Immutable
data class StatComparisonRow(
    val label: String,
    val firstValue: Int,
    val secondValue: Int,
    val winner: StatWinner,
)
```
> **`@Immutable`** = regra de performance Compose (evita recomposições). UiModel é **exclusivo desta feature** (convenção 2).

### B2. Estado por coluna
`.../ui/state/PokemonCompareState.kt`:
```kotlin
package br.com.pokedex.feature.pokemoncompare.ui.state

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import br.com.pokedex.core.common.result.DomainError
import br.com.pokedex.feature.pokemoncompare.ui.model.ComparePokemonUiModel

enum class CompareSide { FIRST, SECOND }

@Immutable
sealed interface SideUiState {
    data object Loading : SideUiState
    data class Success(val pokemon: ComparePokemonUiModel) : SideUiState
    data class Error(val error: DomainError) : SideUiState
}

@Stable
data class PokemonCompareState(
    val first: SideUiState = SideUiState.Loading,
    val second: SideUiState = SideUiState.Loading,
)
```
> **Decisão-chave (Q2=B do design):** cada coluna tem estado **independente** — uma pode falhar e a outra carregar. Por isso `SideUiState` por lado, e não um único `loading/error` global.

### B3. Intent e Event
`.../ui/intent/PokemonCompareIntent.kt`:
```kotlin
package br.com.pokedex.feature.pokemoncompare.ui.intent

import br.com.pokedex.feature.pokemoncompare.ui.state.CompareSide

sealed interface PokemonCompareIntent {
    data class Retry(val side: CompareSide) : PokemonCompareIntent   // retry POR coluna
    data object NavigateBack : PokemonCompareIntent
}
```
`.../ui/event/PokemonCompareEvent.kt`:
```kotlin
package br.com.pokedex.feature.pokemoncompare.ui.event

sealed interface PokemonCompareEvent {
    data object NavigateBack : PokemonCompareEvent
}
```

---

## 2.3 Módulo C — Reducer + Comparador (TDD)

Aqui vale escrever o **teste antes** — é tudo lógica pura.

### C1. Reducer puro
`.../ui/reducer/PokemonCompareReducer.kt`:
```kotlin
package br.com.pokedex.feature.pokemoncompare.ui.reducer

import br.com.pokedex.core.common.result.DomainError
import br.com.pokedex.feature.pokemoncompare.ui.intent.PokemonCompareIntent
import br.com.pokedex.feature.pokemoncompare.ui.model.ComparePokemonUiModel
import br.com.pokedex.feature.pokemoncompare.ui.state.CompareSide
import br.com.pokedex.feature.pokemoncompare.ui.state.PokemonCompareState
import br.com.pokedex.feature.pokemoncompare.ui.state.SideUiState

object PokemonCompareReducer {

    fun loading(state: PokemonCompareState, side: CompareSide) =
        state.setSide(side, SideUiState.Loading)

    fun success(state: PokemonCompareState, side: CompareSide, pokemon: ComparePokemonUiModel) =
        state.setSide(side, SideUiState.Success(pokemon))

    fun error(state: PokemonCompareState, side: CompareSide, domainError: DomainError) =
        state.setSide(side, SideUiState.Error(domainError))

    fun reduce(state: PokemonCompareState, intent: PokemonCompareIntent) = when (intent) {
        is PokemonCompareIntent.Retry -> loading(state, intent.side)
        is PokemonCompareIntent.NavigateBack -> state
    }

    private fun PokemonCompareState.setSide(side: CompareSide, value: SideUiState) = when (side) {
        CompareSide.FIRST -> copy(first = value)
        CompareSide.SECOND -> copy(second = value)
    }
}
```

### C2. Comparador (maior por stat) — **FR-5**
`.../comparison/StatComparator.kt`:
```kotlin
package br.com.pokedex.feature.pokemoncompare.comparison

import br.com.pokedex.feature.pokemoncompare.ui.model.ComparePokemonUiModel
import br.com.pokedex.feature.pokemoncompare.ui.model.StatComparisonRow
import br.com.pokedex.feature.pokemoncompare.ui.model.StatWinner

/** Alinha stats por label (ordem do primeiro) e calcula o vencedor. Ausente = 0. */
fun buildStatComparison(
    first: ComparePokemonUiModel,
    second: ComparePokemonUiModel,
): List<StatComparisonRow> {
    val firstByLabel = first.stats.associate { it.label to it.value }
    val secondByLabel = second.stats.associate { it.label to it.value }
    val orderedLabels = LinkedHashSet<String>().apply {
        first.stats.forEach { add(it.label) }
        second.stats.forEach { add(it.label) }
    }
    return orderedLabels.map { label ->
        val a = firstByLabel[label] ?: 0
        val b = secondByLabel[label] ?: 0
        StatComparisonRow(label, a, b, when {
            a > b -> StatWinner.FIRST
            b > a -> StatWinner.SECOND
            else -> StatWinner.TIE
        })
    }
}
```

### C3. Testes (escreva e rode)
`.../src/test/kotlin/.../comparison/StatComparatorTest.kt`:
```kotlin
package br.com.pokedex.feature.pokemoncompare.comparison

import androidx.compose.ui.graphics.Color
import br.com.pokedex.feature.pokemoncompare.ui.model.CompareStatUiModel
import br.com.pokedex.feature.pokemoncompare.ui.model.ComparePokemonUiModel
import br.com.pokedex.feature.pokemoncompare.ui.model.StatWinner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StatComparatorTest {
    private fun model(vararg stats: Pair<String, Int>) = ComparePokemonUiModel(
        id = 0, name = "X", imageUrl = "", typeColor = Color.Unspecified, types = emptyList(),
        height = "", weight = "", abilities = emptyList(), description = "",
        stats = stats.map { CompareStatUiModel(it.first, it.second) },
    )

    @Test fun `first wins`() =
        assertEquals(StatWinner.FIRST, buildStatComparison(model("HP" to 50), model("HP" to 30)).single().winner)

    @Test fun `tie`() =
        assertEquals(StatWinner.TIE, buildStatComparison(model("HP" to 40), model("HP" to 40)).single().winner)

    @Test fun `missing stat counts as zero`() {
        val rows = buildStatComparison(model("HP" to 10), model("ATK" to 10)).associateBy { it.label }
        assertEquals(StatWinner.FIRST, rows.getValue("HP").winner)    // 10 vs 0
        assertEquals(StatWinner.SECOND, rows.getValue("ATK").winner)  // 0 vs 10
    }
}
```

⏸️ **Pare e teste** (já roda sem precisar de Android):
```bash
./gradlew :feature:pokemon-compare:testDebugUnitTest
```

---

## 2.4 Módulo D — Mapper + ViewModel

### D1. Mapper `Pokemon → ComparePokemonUiModel`
`.../mapper/PokemonCompareUiMapper.kt` (mesma lógica do detalhe, mas UiModel próprio):
```kotlin
package br.com.pokedex.feature.pokemoncompare.mapper

import br.com.pokedex.core.designsystem.theme.pokemonTypeColor
import br.com.pokedex.core.designsystem.util.PokemonImageUrlProvider
import br.com.pokedex.core.model.Pokemon
import br.com.pokedex.feature.pokemoncompare.ui.model.CompareStatUiModel
import br.com.pokedex.feature.pokemoncompare.ui.model.ComparePokemonUiModel

private val statLabelMap = mapOf(
    "hp" to "HP", "attack" to "ATK", "defense" to "DEF",
    "special-attack" to "SATK", "special-defense" to "SDEF", "speed" to "SPD",
)

fun Pokemon.toCompareUiModel(): ComparePokemonUiModel {
    val primaryType = types.minByOrNull { it.slot }?.name ?: "normal"
    return ComparePokemonUiModel(
        id = id,
        name = name.replaceFirstChar { it.uppercase() },
        imageUrl = PokemonImageUrlProvider.officialArtwork(id),
        typeColor = pokemonTypeColor(primaryType),
        types = types.sortedBy { it.slot }.map { it.name },
        height = "${height / 10.0}m",
        weight = "${weight / 10.0}kg",
        abilities = abilities.filter { !it.isHidden }
            .map { it.name.replace('-', ' ').replaceFirstChar { c -> c.uppercase() } },
        description = description,
        stats = stats.map { CompareStatUiModel(statLabelMap[it.name] ?: it.name.uppercase(), it.baseStat) },
    )
}
```

### D2. ViewModel — carga paralela + retry por lado (**FR-6/FR-7, Q1=A**)
`.../viewmodel/PokemonCompareViewModel.kt`:
```kotlin
package br.com.pokedex.feature.pokemoncompare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.pokedex.core.common.result.Result
import br.com.pokedex.core.domain.usecase.GetPokemonDetailUseCase
import br.com.pokedex.feature.pokemoncompare.mapper.toCompareUiModel
import br.com.pokedex.feature.pokemoncompare.ui.event.PokemonCompareEvent
import br.com.pokedex.feature.pokemoncompare.ui.intent.PokemonCompareIntent
import br.com.pokedex.feature.pokemoncompare.ui.reducer.PokemonCompareReducer
import br.com.pokedex.feature.pokemoncompare.ui.state.CompareSide
import br.com.pokedex.feature.pokemoncompare.ui.state.PokemonCompareState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PokemonCompareViewModel(
    private val getPokemonDetailUseCase: GetPokemonDetailUseCase,   // REUSO
    private val firstId: Int,
    private val secondId: Int,
) : ViewModel() {

    private val _state = MutableStateFlow(PokemonCompareState())
    val state: StateFlow<PokemonCompareState> = _state

    private val _events = Channel<PokemonCompareEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadSide(CompareSide.FIRST)    // as duas colunas carregam
        loadSide(CompareSide.SECOND)   // em paralelo e independentes
    }

    fun onIntent(intent: PokemonCompareIntent) {
        when (intent) {
            is PokemonCompareIntent.Retry -> loadSide(intent.side)
            is PokemonCompareIntent.NavigateBack -> {
                viewModelScope.launch { _events.send(PokemonCompareEvent.NavigateBack) }
            }
        }
    }

    private fun loadSide(side: CompareSide) {
        val id = if (side == CompareSide.FIRST) firstId else secondId
        viewModelScope.launch {
            _state.update { PokemonCompareReducer.loading(it, side) }
            when (val result = getPokemonDetailUseCase(id)) {
                is Result.Success -> _state.update { PokemonCompareReducer.success(it, side, result.data.toCompareUiModel()) }
                is Result.Error -> _state.update { PokemonCompareReducer.error(it, side, result.error) }
            }
        }
    }
}
```
> Cada `loadSide` é uma coroutine separada → **paralelismo** sem `async` explícito, e o `Retry(side)` recarrega só aquela coluna.

---

## 2.5 Módulo E — UI

### E1. Item de stat com destaque
`.../ui/component/StatComparisonRowItem.kt`:
```kotlin
package br.com.pokedex.feature.pokemoncompare.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import br.com.pokedex.core.ui.PokemonStatBar
import br.com.pokedex.feature.pokemoncompare.ui.model.CompareStatUiModel

val StatWinnerColor = Color(0xFF4CAF50)

@Composable
fun StatComparisonRowItem(stat: CompareStatUiModel, isWinner: Boolean, baseColor: Color, modifier: Modifier = Modifier) {
    PokemonStatBar(
        label = stat.label,
        value = stat.value,
        color = if (isWinner) StatWinnerColor else baseColor,   // verde se vence
        modifier = modifier.fillMaxWidth(),
    )
}
```

### E2. Coluna (renderiza o `SideUiState`)
`.../ui/component/CompareColumn.kt` — reage ao estado **da própria coluna** e aplica destaque só se `winnerByLabel != null` (ou seja, quando ambas carregaram):
```kotlin
package br.com.pokedex.feature.pokemoncompare.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.pokedex.core.designsystem.component.ErrorContent
import br.com.pokedex.core.designsystem.component.LoadingIndicator
import br.com.pokedex.core.designsystem.component.PokemonTypeChip
import br.com.pokedex.core.designsystem.theme.*
import br.com.pokedex.feature.pokemoncompare.ui.model.ComparePokemonUiModel
import br.com.pokedex.feature.pokemoncompare.ui.model.StatWinner
import br.com.pokedex.feature.pokemoncompare.ui.state.CompareSide
import br.com.pokedex.feature.pokemoncompare.ui.state.SideUiState
import coil3.compose.AsyncImage

@Composable
fun CompareColumn(
    side: CompareSide,
    state: SideUiState,
    winnerByLabel: Map<String, StatWinner>?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        when (state) {
            is SideUiState.Loading -> Box(Modifier.fillMaxWidth().height(240.dp), Alignment.Center) { LoadingIndicator() }
            is SideUiState.Error -> ErrorContent(
                message = "Could not load. Please try again.",
                onRetry = onRetry,
                modifier = Modifier.fillMaxWidth().height(240.dp).testTag("compare-error-${side.name.lowercase()}"),
            )
            is SideUiState.Success -> Content(state.pokemon, side, winnerByLabel)
        }
    }
}

@Composable
private fun Content(pokemon: ComparePokemonUiModel, side: CompareSide, winnerByLabel: Map<String, StatWinner>?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        AsyncImage(model = pokemon.imageUrl, contentDescription = pokemon.name, modifier = Modifier.size(120.dp))
        Text("#${pokemon.id.toString().padStart(3, '0')}", style = Body3Regular, color = Gray2)
        Text(pokemon.name, style = Subtitle1Bold, color = pokemon.typeColor)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { pokemon.types.forEach { PokemonTypeChip(typeName = it) } }
        Spacer(Modifier.height(12.dp))
        Text("Weight: ${pokemon.weight}", style = Body3Regular)
        Text("Height: ${pokemon.height}", style = Body3Regular)
        Spacer(Modifier.height(4.dp))
        Text("Abilities", style = Subtitle2Bold, color = pokemon.typeColor)
        pokemon.abilities.forEach { Text(it, style = Body3Regular, textAlign = TextAlign.Center) }
        if (pokemon.description.isNotBlank()) {
            Spacer(Modifier.height(8.dp)); Text(pokemon.description, style = Body3Regular, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(12.dp))
        Text("Base Stats", style = Subtitle1Bold, color = pokemon.typeColor)
        Spacer(Modifier.height(8.dp))
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            pokemon.stats.forEach { stat ->
                val winSide = if (side == CompareSide.FIRST) StatWinner.FIRST else StatWinner.SECOND
                StatComparisonRowItem(stat, isWinner = winnerByLabel?.get(stat.label) == winSide, baseColor = pokemon.typeColor)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
```

### E3. A tela (2 colunas + scroll vertical — **FR-4/FR-7, Q7=A**)
`.../ui/screen/PokemonCompareScreen.kt`:
```kotlin
package br.com.pokedex.feature.pokemoncompare.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.pokedex.core.designsystem.theme.HeadlineBold
import br.com.pokedex.core.designsystem.theme.PokedexRed
import br.com.pokedex.core.designsystem.theme.White
import br.com.pokedex.feature.pokemoncompare.comparison.buildStatComparison
import br.com.pokedex.feature.pokemoncompare.ui.component.CompareColumn
import br.com.pokedex.feature.pokemoncompare.ui.event.PokemonCompareEvent
import br.com.pokedex.feature.pokemoncompare.ui.intent.PokemonCompareIntent
import br.com.pokedex.feature.pokemoncompare.ui.model.StatWinner
import br.com.pokedex.feature.pokemoncompare.ui.state.CompareSide
import br.com.pokedex.feature.pokemoncompare.ui.state.SideUiState
import br.com.pokedex.feature.pokemoncompare.viewmodel.PokemonCompareViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PokemonCompareScreen(
    firstId: Int,
    secondId: Int,
    onBack: () -> Unit,
    viewModel: PokemonCompareViewModel = koinViewModel(
        key = "pokemon_compare_${firstId}_$secondId",
        parameters = { parametersOf(firstId, secondId) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.events.collect { if (it is PokemonCompareEvent.NavigateBack) onBack() }
    }

    val first = state.first
    val second = state.second
    // Destaque do maior só quando AMBAS carregaram (Q2=B):
    val winnerByLabel: Map<String, StatWinner>? = remember(first, second) {
        if (first is SideUiState.Success && second is SideUiState.Success)
            buildStatComparison(first.pokemon, second.pokemon).associate { it.label to it.winner }
        else null
    }

    Column(Modifier.fillMaxSize().background(White)) {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().background(PokedexRed).statusBarsPadding().padding(horizontal = 4.dp, vertical = 8.dp)) {
            IconButton(onClick = { viewModel.onIntent(PokemonCompareIntent.NavigateBack) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = White)
            }
            Text("Compare", style = HeadlineBold, color = White)
        }
        Row(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(vertical = 16.dp)) {
            CompareColumn(CompareSide.FIRST, first, winnerByLabel,
                onRetry = { viewModel.onIntent(PokemonCompareIntent.Retry(CompareSide.FIRST)) },
                modifier = Modifier.weight(1f).testTag("compare-column-first"))
            VerticalDivider(color = Color(0xFFE0E0E0))
            CompareColumn(CompareSide.SECOND, second, winnerByLabel,
                onRetry = { viewModel.onIntent(PokemonCompareIntent.Retry(CompareSide.SECOND)) },
                modifier = Modifier.weight(1f).testTag("compare-column-second"))
        }
    }
}
```
> (Opcional) crie um `@Preview` em `.../ui/preview/` envolvendo `CompareColumn` em `PokedexLabTheme { ... }` com 2 modelos de exemplo — ajuda a iterar sem rodar o app.

---

## 2.6 Módulo F — Navegação, deep link e DI

### F1. NavEntry da tela
`.../navigation/PokemonCompareNavEntry.kt`:
```kotlin
package br.com.pokedex.feature.pokemoncompare.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import br.com.pokedex.core.route.keys.AppNavigator
import br.com.pokedex.core.route.keys.PokemonCompareKey
import br.com.pokedex.feature.pokemoncompare.ui.screen.PokemonCompareScreen

fun EntryProviderScope<NavKey>.pokemonCompareEntry(navigator: AppNavigator) {
    entry<PokemonCompareKey> { key ->
        PokemonCompareScreen(
            firstId = key.firstId,
            secondId = key.secondId,
            onBack = { navigator.navigateBack() },
        )
    }
}
```

### F2. DI (Koin)
`.../di/PokemonCompareModule.kt`:
```kotlin
package br.com.pokedex.feature.pokemoncompare.di

import br.com.pokedex.feature.pokemoncompare.viewmodel.PokemonCompareViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val pokemonCompareModule = module {
    // GetPokemonDetailUseCase NÃO é re-registrado aqui (pokemonDetailModule já registra).
    // get() o resolve do grafo Koin → evita DefinitionOverrideException.
    viewModel { (firstId: Int, secondId: Int) -> PokemonCompareViewModel(get(), firstId, secondId) }
}
```
> **Armadilha de DI explicada:** se você registrar `factory { GetPokemonDetailUseCase(get()) }` aqui E em `pokemonDetailModule`, o Koin 4 lança `DefinitionOverrideException` no startup. Como o grafo Koin é **plano**, basta um módulo registrar o use case; o `get()` o encontra. (Trade-off documentado em `aidlc-docs/inception/application-design/services.md`.)

### F3. Deep link com validação (**FR-8 + SECURITY-05**)
`core/route/deeplink/.../DeepLinkRouter.kt` — adicione no `when` de `fromUrl`:
```kotlin
url.startsWith("pokedex://compare/") -> {
    val parts = url.removePrefix("pokedex://compare/").split("/")
    val first = parts.getOrNull(0)?.toIntOrNull()
    val second = parts.getOrNull(1)?.toIntOrNull()
    if (first != null && first > 0 && second != null && second > 0)
        PokemonCompareKey(first, second) else null   // ids inválidos → não resolve
}
```
> **Por que validar?** É input externo (vem de um Intent). A regra SECURITY-05 (extensão Security) exige validação de entrada. Veja o Cap. 3 para criar suas próprias regras.

### F4. Registrar a entry no grafo
`core/route/navigation/.../AppNavDisplay.kt` — importe `pokemonCompareEntry` e chame no `entryProvider`:
```kotlin
entryProvider = entryProvider {
    pokemonListEntry(navigator)
    pokemonDetailEntry(navigator)
    pokemonCompareEntry(navigator)   // ← novo
}
```
E em `core/route/navigation/build.gradle.kts`:
```kotlin
implementation(project(":feature:pokemon-compare"))
```
> É **aqui** (no módulo de montagem) que a dependência da feature é declarada — não nas outras features.

⏸️ **Pare e teste:**
```bash
./gradlew :feature:pokemon-compare:compileDebugKotlin :core:route:navigation:compileDebugKotlin
```

---

## 2.7 Módulo G — Seleção na lista (`feature:pokemon-list`)

### G1. Estado, Intent, Event
`.../ui/state/PokemonListState.kt`:
```kotlin
@Stable
data class PokemonListState(
    val isLoading: Boolean = false,
    val error: DomainError? = null,
    val isSelectionMode: Boolean = false,
    val selectedIds: List<Int> = emptyList(),
) {
    val canCompare: Boolean get() = selectedIds.size == MAX_COMPARE_SELECTION
    companion object { const val MAX_COMPARE_SELECTION = 2 }
}
```
`.../ui/intent/PokemonListIntent.kt` — adicione:
```kotlin
data object ToggleSelectionMode : PokemonListIntent
data class ToggleSelection(val id: Int) : PokemonListIntent
data object ClickCompare : PokemonListIntent
```
`.../ui/event/PokemonListEvent.kt` — adicione:
```kotlin
data class NavigateToCompare(val firstId: Int, val secondId: Int) : PokemonListEvent
data object ShowSelectionLimit : PokemonListEvent
```

### G2. Reducer (seleção exata de 2 — **FR-2**)
`.../ui/reducer/PokemonListReducer.kt`:
```kotlin
is PokemonListIntent.ToggleSelectionMode -> {
    val enabling = !state.isSelectionMode
    state.copy(isSelectionMode = enabling, selectedIds = if (enabling) state.selectedIds else emptyList())
}
is PokemonListIntent.ToggleSelection -> when {
    intent.id in state.selectedIds -> state.copy(selectedIds = state.selectedIds - intent.id)
    state.selectedIds.size < MAX_COMPARE_SELECTION -> state.copy(selectedIds = state.selectedIds + intent.id)
    else -> state // 3º bloqueado; o ViewModel emite ShowSelectionLimit
}
is PokemonListIntent.ClickCompare -> state // navegação/limpeza no ViewModel
```
> O reducer é puro: ele **não** mostra snackbar nem navega. Ele só recusa o 3º (no-op). O efeito (snackbar/navegar) é do ViewModel.

### G3. ViewModel (efeitos: limite + navegar + limpar — **FR-3/FR-9, Q4=A**)
`.../viewmodel/PokemonListViewModel.kt` no `onIntent`:
```kotlin
fun onIntent(intent: PokemonListIntent) {
    val limitReached = intent is PokemonListIntent.ToggleSelection &&
        intent.id !in _state.value.selectedIds &&
        _state.value.selectedIds.size >= PokemonListState.MAX_COMPARE_SELECTION
    val compareIds = if (intent is PokemonListIntent.ClickCompare) _state.value.selectedIds else emptyList()

    _state.update { PokemonListReducer.reduce(it, intent) }

    when (intent) {
        is PokemonListIntent.ClickPokemon ->
            viewModelScope.launch { _events.send(PokemonListEvent.NavigateToDetail(intent.id)) }
        is PokemonListIntent.ToggleSelection ->
            if (limitReached) viewModelScope.launch { _events.send(PokemonListEvent.ShowSelectionLimit) }
        is PokemonListIntent.ClickCompare -> if (compareIds.size == 2) {
            viewModelScope.launch { _events.send(PokemonListEvent.NavigateToCompare(compareIds[0], compareIds[1])) }
            _state.update { it.copy(isSelectionMode = false, selectedIds = emptyList()) } // Q4=A
        }
        is PokemonListIntent.Retry, is PokemonListIntent.ToggleSelectionMode -> Unit
    }
}
```
> **Detalhe sutil:** lemos `compareIds`/`limitReached` **antes** de aplicar o reducer (que pode limpar a seleção), senão a navegação perderia os ids.

### G4. Card selecionável (**Q3=A: borda + badge**)
`.../ui/component/PokemonCard.kt` — adicione o parâmetro `selected` e a decoração:
```kotlin
@Composable
fun PokemonCard(pokemon: PokemonListUiModel, onClick: () -> Unit, modifier: Modifier = Modifier, selected: Boolean = false) {
    val shape = RoundedCornerShape(8.dp)
    Card(
        modifier = modifier.height(108.dp).clickable(onClick = onClick)
            .then(if (selected) Modifier.border(2.dp, PokedexRed, shape) else Modifier)
            .testTag("pokemon-card-${pokemon.id}"),
        shape = shape, /* ...resto igual... */
    ) {
        Box(Modifier.fillMaxSize()) {
            if (selected) {
                Box(contentAlignment = Alignment.Center,
                    modifier = Modifier.align(Alignment.TopStart).padding(4.dp).size(20.dp).clip(CircleShape).background(PokedexRed)) {
                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = White, modifier = Modifier.size(14.dp))
                }
            }
            /* ...name tag, número, artwork como já existiam... */
        }
    }
}
```

### G5. Tela: toggle no header, roteamento do toque, botão e snackbar
No `PokemonListScreen`:
- assine `onNavigateToCompare: (Int, Int) -> Unit`;
- colete `state` (`collectAsStateWithLifecycle`) e crie `SnackbarHostState`;
- no `LaunchedEffect`, trate `NavigateToCompare` e `ShowSelectionLimit` (`snackbarHostState.showSnackbar("You can only compare 2 Pokémon")`);
- no header, um `Text("Compare"/"Cancel")` clicável → `ToggleSelectionMode`;
- no card: `onClick = { if (isSelectionMode) ToggleSelection(id) else ClickPokemon(id) }`, `selected = id in selectedIds`;
- um `Button("Compare (n)", enabled = canCompare)` → `ClickCompare`, e um `SnackbarHost`, ambos `align(BottomCenter)`.

(O código completo está em `feature/pokemon-list/.../ui/screen/PokemonListScreen.kt`.)

### G6. NavEntry da lista repassa o callback
`.../navigation/PokemonListNavEntry.kt`:
```kotlin
PokemonListScreen(
    onNavigateToDetail = { id -> navigator.navigateTo(PokemonDetailKey(id)) },
    onNavigateToCompare = { a, b -> navigator.navigateTo(PokemonCompareKey(a, b)) },
)
```

---

## 2.8 Módulo H — App, build e testes

### H1. Registrar o módulo Koin no app
`app/.../PokedexLabApplication.kt` → `startKoin { modules(..., pokemonCompareModule) }`.
E `app/build.gradle.kts`:
```kotlin
implementation(project(":feature:pokemon-compare"))
```
> **Por que o app precisa depender do módulo** se `core:route:navigation` já depende? Porque aquela dependência é `implementation` (não vaza transitivamente). O app precisa enxergar `pokemonCompareModule` diretamente para chamá-lo no `startKoin`.

### H2. Build + testes
```bash
./gradlew assembleDebug --console=plain
./gradlew :feature:pokemon-compare:testDebugUnitTest :feature:pokemon-list:testDebugUnitTest --console=plain
```
Esperado: `BUILD SUCCESSFUL` e suites verdes (`StatComparatorTest`, `PokemonCompareReducerTest`, `PokemonCompareUiMapperTest`, `PokemonListReducerTest`).

### H3. Smoke test no app / deep link
```bash
adb shell am start -a android.intent.action.VIEW -d "pokedex://compare/1/4"   # abre comparação #1 vs #4
adb shell am start -a android.intent.action.VIEW -d "pokedex://compare/abc/4" # inválido → não resolve
```

---

## 2.9 Checklist final

- [ ] `settings.gradle.kts` inclui `:feature:pokemon-compare`
- [ ] `PokemonCompareKey` em `core:route:keys`
- [ ] models / state / intent / event / reducer / comparator / mapper / viewmodel
- [ ] UI: `StatComparisonRowItem`, `CompareColumn`, `PokemonCompareScreen` (+ preview)
- [ ] navEntry + módulo Koin
- [ ] deep link com validação de ids
- [ ] `AppNavDisplay` registra a entry + build.gradle de `route:navigation` depende da feature
- [ ] lista: state/intent/event/reducer/viewmodel/card/screen/navEntry
- [ ] app: `startKoin` + dependência no `build.gradle.kts`
- [ ] `assembleDebug` verde + testes verdes

> Construiu tudo? Você reproduziu exatamente o que o fluxo AI-DLC entregou. Agora vá para o [Capítulo 3 — Extensions](03-extensions-validador-design-system.md) para aprender a **governar** esse tipo de construção.
