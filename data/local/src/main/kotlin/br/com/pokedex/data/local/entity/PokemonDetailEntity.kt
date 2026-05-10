package br.com.pokedex.data.local.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

// types, stats and abilities are serialized as pipe-separated strings to avoid relations overhead.
// Format examples:
//   types    → "grass|poison"
//   stats    → "hp:45|attack:49|defense:49"
//   abilities → "overgrow:false|chlorophyll:true"
@Entity
data class PokemonDetailEntity(
    @Id var dbId: Long = 0,
    var pokemonId: Int = 0,
    var name: String = "",
    var height: Int = 0,
    var weight: Int = 0,
    var typesRaw: String = "",
    var statsRaw: String = "",
    var abilitiesRaw: String = "",
    var description: String = "",
    var cachedAt: Long = 0L,
)
