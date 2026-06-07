# Pokemon Compare — Context (decisões de zonas cinzentas)

Decisões do dono do projeto capturadas na fase Specify (2026-06-07). Estas resolvem ambiguidades de comportamento de UI e guiam spec/design.

| # | Zona cinzenta | Decisão | Implicação |
|---|---|---|---|
| C-1 | Quantos Pokémon comparar | **Exatamente 2** | Layout fixo de 2 colunas lado a lado; sem scroll horizontal; estado de seleção limitado a 2. |
| C-2 | Como selecionar na lista | **Modo seleção na lista**: botão "Comparar" no header ativa o modo; toque nos cards marca/desmarca; ao marcar o 2º, abre a comparação | Não quebra o comportamento atual (toque único → detalhe) fora do modo. Exige alterar `feature:pokemon-list` (header + estado de seleção + feedback visual). |
| C-3 | Conteúdo da tela de comparação | **Espelha o detalhe SEM About/description**: artwork, nome, #número, tipos (chips) e Base Stats | Reusa `PokemonStatBar` (core:ui), `PokemonTypeChip` + `pokemonTypeColor` (core:design-system). Não exibe peso/altura/abilities nem flavor text. |
| C-4 | Destaque de diferenças | **Realçar o maior valor de cada stat** (vencedor por linha) | Por linha de status, comparar os 2 valores e destacar o maior. Empate: destacar ambos (a definir no design). Sem total geral nesta versão. |

**Não decidido / adiado:** comparar 3+ Pokémon; salvar/compartilhar comparação; trocar Pokémon dentro da tela de comparação (candidato a P3).
