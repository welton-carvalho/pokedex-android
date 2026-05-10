package br.com.pokedex.data.local.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class PokemonSummaryEntity(
    @Id var dbId: Long = 0,
    var pokemonId: Int = 0,
    var name: String = "",
)
