package br.com.pokedex.data.repository.mapper

import br.com.pokedex.core.model.Pokemon
import br.com.pokedex.core.model.PokemonAbility
import br.com.pokedex.core.model.PokemonStat
import br.com.pokedex.core.model.PokemonType
import br.com.pokedex.data.network.dto.PokemonDetailDto

fun PokemonDetailDto.toDomain(description: String = ""): Pokemon = Pokemon(
    id = id,
    name = name,
    height = height,
    weight = weight,
    types = types.map { PokemonType(slot = it.slot, name = it.type.name) },
    stats = stats.map { PokemonStat(name = it.stat.name, baseStat = it.baseStat) },
    abilities = abilities.map { PokemonAbility(name = it.ability.name, isHidden = it.isHidden) },
    description = description,
)
