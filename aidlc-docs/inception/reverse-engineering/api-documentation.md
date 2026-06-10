# API Documentation

## External REST APIs (PokeAPI, base `https://pokeapi.co/api/v2/`)

Consumed via `PokemonApiService` (Retrofit).

### Get Pokemon List
- **Method**: GET
- **Path**: `pokemon/?offset={offset}&limit={limit}`
- **Purpose**: Paginated list of Pokemon (limit 50 per page).
- **Response**: `PokemonListResponseDto` (count, next, previous, results[name,url]).

### Get Pokemon Detail
- **Method**: GET
- **Path**: `pokemon/{id}`
- **Purpose**: Full detail for one Pokemon.
- **Response**: `PokemonDetailDto` (id, name, types, stats, abilities, height, weight, sprites).

### Get Pokemon Species
- **Method**: GET
- **Path**: `pokemon-species/{id}`
- **Purpose**: Provides flavor text (English description).
- **Response**: `PokemonSpeciesDto` (flavorTextEntries[flavorText, language]).

## Internal APIs

### PokemonRepository (core:domain)
- `fun getPokemonList(): Flow<PagingData<PokemonSummary>>`
- `suspend fun getPokemonDetail(id: Int): Result<Pokemon>`

### GetPokemonListUseCase / GetPokemonDetailUseCase (core:domain)
- Thin invokers delegating to the repository.

### RemotePokemonDataSource (data:network)
- `getPokemonDetail(id)`, `getPokemonSpecies(id)` returning `Result<...>`.

### LocalPokemonDataSource (data:local)
- `savePokemonDetail(pokemon)`, `getPokemonDetail(id): Pokemon?`.

### Navigation (core:route)
- `PokemonListKey : NavKey`
- `PokemonDetailKey(pokemonId: Int) : NavKey`
- `DeepLinkRouter` resolves external links to nav keys.

## Data Models (core:model)

### Pokemon
- **Fields**: `id:Int`, `name:String`, `types:List<PokemonType>`, `stats:List<PokemonStat>`, `abilities:List<PokemonAbility>`, `height:Int`, `weight:Int`, `description:String`.

### PokemonType
- **Fields**: `slot:Int`, `name:String`.

### PokemonStat
- **Fields**: `name:String`, `baseStat:Int`.

### PokemonAbility
- **Fields**: `name:String`, `isHidden:Boolean`.

### PokemonSummary
- **Fields**: `id:Int`, `name:String`.

### Result<T> / DomainError (core:common)
- `Result.Success(data)` | `Result.Error(...)`.
- `DomainError`: `Network`, `Timeout`, `Unauthorized`, `NotFound`, `Unknown`.
