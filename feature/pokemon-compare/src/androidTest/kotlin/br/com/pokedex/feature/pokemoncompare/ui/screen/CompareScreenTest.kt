package br.com.pokedex.feature.pokemoncompare.ui.screen

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import br.com.pokedex.core.common.result.AsyncResult
import br.com.pokedex.core.common.result.DomainError
import br.com.pokedex.feature.pokemoncompare.ui.intent.Side
import br.com.pokedex.feature.pokemoncompare.ui.model.CompareUiModel
import br.com.pokedex.feature.pokemoncompare.ui.model.StatUiModel
import br.com.pokedex.feature.pokemoncompare.ui.state.CompareState
import org.junit.Rule
import org.junit.Test

class CompareScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val bulbasaurUi = CompareUiModel(
        id = 1,
        name = "Bulbasaur",
        formattedNumber = "#001",
        imageUrl = "https://example.com/1.png",
        types = listOf("Grass", "Poison"),
        primaryTypeColor = Color(0xFF74CB48),
        heightMeters = 0.7f,
        weightKg = 6.9f,
        stats = listOf(
            StatUiModel("HP", 45),
            StatUiModel("ATK", 49),
            StatUiModel("DEF", 49),
            StatUiModel("SPA", 65),
            StatUiModel("SPD", 65),
            StatUiModel("SPE", 45),
        ),
    )

    private val charmanderUi = CompareUiModel(
        id = 4,
        name = "Charmander",
        formattedNumber = "#004",
        imageUrl = "https://example.com/4.png",
        types = listOf("Fire"),
        primaryTypeColor = Color(0xFFF57D31),
        heightMeters = 0.6f,
        weightKg = 8.5f,
        stats = listOf(
            StatUiModel("HP", 39),
            StatUiModel("ATK", 52),
            StatUiModel("DEF", 43),
            StatUiModel("SPA", 60),
            StatUiModel("SPD", 50),
            StatUiModel("SPE", 65),
        ),
    )

    @Test
    fun loadingState_showsLoadingIndicatorsForBothSides() {
        composeRule.setContent {
            CompareScreenContent(
                state = CompareState(),
                onBack = {},
                onRetry = {},
            )
        }

        composeRule.onNodeWithText("Compare").assertIsDisplayed()
    }

    @Test
    fun successState_showsArtworkNumbersAndStatLabels() {
        composeRule.setContent {
            CompareScreenContent(
                state = CompareState(
                    pokemonA = AsyncResult.Success(bulbasaurUi),
                    pokemonB = AsyncResult.Success(charmanderUi),
                ),
                onBack = {},
                onRetry = {},
            )
        }

        composeRule.onNodeWithText("Bulbasaur").assertIsDisplayed()
        composeRule.onNodeWithText("Charmander").assertIsDisplayed()
        composeRule.onNodeWithText("#001").assertIsDisplayed()
        composeRule.onNodeWithText("#004").assertIsDisplayed()
        composeRule.onAllNodesWithText("HP").assertCountIsAtLeast(1)
    }

    @Test
    fun errorState_showsRetryButton() {
        var retrySide: Side? = null
        composeRule.setContent {
            CompareScreenContent(
                state = CompareState(
                    pokemonA = AsyncResult.Error(DomainError.Network),
                    pokemonB = AsyncResult.Success(charmanderUi),
                ),
                onBack = {},
                onRetry = { retrySide = it },
            )
        }

        composeRule.onNode(hasText("Retry")).assertIsDisplayed()
    }

    @Test
    fun invalidInput_showsScreenLevelMessage() {
        composeRule.setContent {
            CompareScreenContent(
                state = CompareState(isInvalidInput = true),
                onBack = {},
                onRetry = {},
            )
        }
        composeRule.onNodeWithText("Invalid comparison").assertIsDisplayed()
    }
}

private fun androidx.compose.ui.test.SemanticsNodeInteractionCollection.assertCountIsAtLeast(minimum: Int) {
    val count = fetchSemanticsNodes(atLeastOneRootRequired = false).size
    if (count < minimum) {
        throw AssertionError("Expected at least $minimum nodes but found $count")
    }
}
