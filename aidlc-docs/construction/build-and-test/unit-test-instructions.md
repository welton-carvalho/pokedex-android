# Unit Test Execution

## Run — testes da feature de comparação
```bash
./gradlew :feature:pokemon-compare:testDebugUnitTest \
          :feature:pokemon-list:testDebugUnitTest --console=plain
```

## Resultados (execução de 2026-06-10)
| Suite | Tests | Falhas |
|---|---|---|
| `StatComparatorTest` | 5 | 0 |
| `PokemonCompareUiMapperTest` | 5 | 0 |
| `PokemonCompareReducerTest` | 4 | 0 |
| `PokemonListReducerTest` | 10 | 0 |
| **Total** | **24** | **0** |

- **Relatórios**: `feature/pokemon-compare/build/reports/tests/testDebugUnitTest/index.html` e `feature/pokemon-list/build/reports/tests/...`

## Cobertura (NFR-3)
- **Reducer da comparação** (`PokemonCompareReducer`): por lado loading/success/error, retry, navigateBack.
- **Lógica de vencedor por stat** (`StatComparator`): FIRST/SECOND/TIE, alinhamento por label, stat ausente = 0.
- **Mapper** (`Pokemon.toCompareUiModel`): nome, height/weight, abilities ocultas filtradas, labels de stats.
- **Seleção na lista** (`PokemonListReducer`): toggle de modo, add/remove, limite de 2, `canCompare`.

## Observação — falha PRÉ-EXISTENTE (fora do escopo desta feature)
`./gradlew test` (todos os módulos) falha ao compilar `data:repository` test:
```
PokemonRepositoryImplTest.kt:53 No value passed for parameter 'summaryBox'/'detailBox'
```
- `git status` de `data/repository` está limpo → teste já estava desatualizado no repositório (assinatura do `LocalPokemonDataSource` mudou e o teste não foi atualizado).
- **Não tem relação** com a feature de comparação. Os módulos desta feature compilam e passam isoladamente.
