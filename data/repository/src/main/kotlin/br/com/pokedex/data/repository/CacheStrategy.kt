package br.com.pokedex.data.repository

enum class CacheStrategy {
    MEMORY,
    DISK,
    NETWORK_FIRST,
    CACHE_FIRST,
    NETWORK_ONLY,
}
