package br.com.pokedex.data.local.mapper

import br.com.pokedex.core.model.Pokemon
import br.com.pokedex.core.model.PokemonAbility
import br.com.pokedex.core.model.PokemonStat
import br.com.pokedex.core.model.PokemonSummary
import br.com.pokedex.core.model.PokemonType
import br.com.pokedex.data.local.entity.PokemonDetailEntity
import br.com.pokedex.data.local.entity.PokemonSummaryEntity

fun PokemonSummaryEntity.toDomain() = PokemonSummary(id = pokemonId, name = name)

fun PokemonSummary.toEntity() = PokemonSummaryEntity(pokemonId = id, name = name)

fun Pokemon.toEntity() = PokemonDetailEntity(
    pokemonId = id,
    name = name,
    height = height,
    weight = weight,
    typesRaw = types.joinToString("|") { "${it.slot}:${it.name}" },
    statsRaw = stats.joinToString("|") { "${it.name}:${it.baseStat}" },
    abilitiesRaw = abilities.joinToString("|") { "${it.name}:${it.isHidden}" },
    description = description,
    cachedAt = System.currentTimeMillis(),
)

fun PokemonDetailEntity.toDomain() = Pokemon(
    id = pokemonId,
    name = name,
    height = height,
    weight = weight,
    types = typesRaw.split("|").filter { it.isNotBlank() }.map {
        val (slot, typeName) = it.split(":")
        PokemonType(slot = slot.toInt(), name = typeName)
    },
    stats = statsRaw.split("|").filter { it.isNotBlank() }.map {
        val (statName, base) = it.split(":")
        PokemonStat(name = statName, baseStat = base.toInt())
    },
    abilities = abilitiesRaw.split("|").filter { it.isNotBlank() }.map {
        val (abilityName, hidden) = it.split(":")
        PokemonAbility(name = abilityName, isHidden = hidden.toBoolean())
    },
    description = description,
)
