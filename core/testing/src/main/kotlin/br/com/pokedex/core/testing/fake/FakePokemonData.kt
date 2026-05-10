package br.com.pokedex.core.testing.fake

import br.com.pokedex.core.model.Pokemon
import br.com.pokedex.core.model.PokemonAbility
import br.com.pokedex.core.model.PokemonStat
import br.com.pokedex.core.model.PokemonSummary
import br.com.pokedex.core.model.PokemonType

object FakePokemonData {

    val bulbasaur = Pokemon(
        id = 1,
        name = "bulbasaur",
        types = listOf(PokemonType(slot = 1, name = "grass"), PokemonType(slot = 2, name = "poison")),
        stats = listOf(
            PokemonStat(name = "hp", baseStat = 45),
            PokemonStat(name = "attack", baseStat = 49),
            PokemonStat(name = "defense", baseStat = 49),
            PokemonStat(name = "special-attack", baseStat = 65),
            PokemonStat(name = "special-defense", baseStat = 65),
            PokemonStat(name = "speed", baseStat = 45),
        ),
        abilities = listOf(
            PokemonAbility(name = "overgrow", isHidden = false),
            PokemonAbility(name = "chlorophyll", isHidden = true),
        ),
        height = 7,
        weight = 69,
    )

    val charmander = Pokemon(
        id = 4,
        name = "charmander",
        types = listOf(PokemonType(slot = 1, name = "fire")),
        stats = listOf(
            PokemonStat(name = "hp", baseStat = 39),
            PokemonStat(name = "attack", baseStat = 52),
            PokemonStat(name = "defense", baseStat = 43),
            PokemonStat(name = "special-attack", baseStat = 60),
            PokemonStat(name = "special-defense", baseStat = 50),
            PokemonStat(name = "speed", baseStat = 65),
        ),
        abilities = listOf(PokemonAbility(name = "blaze", isHidden = false)),
        height = 6,
        weight = 85,
    )

    val summaries = listOf(
        PokemonSummary(id = 1, name = "bulbasaur"),
        PokemonSummary(id = 4, name = "charmander"),
        PokemonSummary(id = 7, name = "squirtle"),
    )
}
