# Requirements Verification Questions — Feature: Comparação de Pokémon

Responda cada pergunta preenchendo a letra escolhida após a tag `[Answer]:`.
Se nenhuma opção servir, escolha a última (Other) e descreva após o `[Answer]:`.
Quando terminar, me avise ("done" / "pronto").

---

## Parte A — Seleção na lista

## Question 1
Como o usuário inicia/realiza a seleção dos Pokémon na tela de lista?

A) Long-press em um card entra em "modo seleção"; depois toques simples marcam/desmarcam. Toque simples normal continua abrindo o detalhe.

B) Um botão/ícone "Comparar" no header ativa o modo seleção; enquanto ativo, toques marcam/desmarcam Pokémon.

C) Cada card sempre exibe um checkbox/indicador de seleção (sem modo); marcar 2 habilita comparar.

X) Other (please describe after [Answer]: tag below)

[Answer]: B

## Question 2
Quantos Pokémon podem ser selecionados para comparação?

A) Exatamente 2 (trava a seleção ao atingir 2).

B) Até 2 (pode comparar com 1 selecionado também? — não; mínimo 2 para comparar).

X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 3
O que dispara a navegação para a tela de comparação?

A) Automático: assim que o 2º Pokémon é selecionado, navega para a comparação.

B) Manual: aparece um botão "Comparar (2)" / FAB que o usuário toca para navegar.

X) Other (please describe after [Answer]: tag below)

[Answer]: B

## Question 4
Quando o 2º Pokémon já está selecionado e o usuário toca um 3º, o que acontece?

A) Bloqueia a seleção (precisa desmarcar um antes) e mostra um aviso.

B) Substitui o mais antigo (FIFO): o novo entra, o primeiro sai.

X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Parte B — Tela de comparação

## Question 5
"As mesmas informações de detalhes" lado a lado — quais blocos devem aparecer para cada Pokémon? (a tela de detalhe atual tem: artwork, nº, nome, type chips, About [weight/height/abilities], descrição, Base Stats)

A) Tudo do detalhe: artwork + nº + nome + types + About (weight/height/abilities) + descrição + Base Stats.

B) Versão enxuta focada em comparar: artwork + nome + types + Base Stats (sem descrição/About).

C) Artwork + nome + types + Base Stats + About (weight/height/abilities), mas SEM a descrição (texto longo).

X) Other (please describe after [Answer]: tag below)

[Answer]: resposta A, inclusive a imagem do pokemon

## Question 6
Destaque visual de qual Pokémon "ganha" em cada stat (ex.: número/barra do maior valor destacado em verde, ou seta)?

A) Sim — destacar por stat o maior valor (e indicar empate).

B) Sim — destacar e também mostrar o total de stats (soma) com o vencedor geral.

C) Não — apenas mostrar os valores lado a lado, sem destaque.

X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 7
Layout em telas estreitas (celular em retrato), já que são 2 colunas com bastante conteúdo:

A) Duas colunas fixas lado a lado sempre, com a tela rolando verticalmente (conteúdo comprimido para caber na largura).

B) Duas colunas lado a lado + rolagem vertical; se necessário, rolagem horizontal para blocos largos.

X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 8
Na tela de comparação, o usuário pode trocar/remover um dos Pokémon sem voltar para a lista?

A) Não — para trocar, volta para a lista e refaz a seleção (mais simples).

B) Sim — cada coluna tem um botão para trocar aquele Pokémon (abre um seletor/volta para a lista mantendo o outro).

X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Parte C — Técnico / Não-funcional

## Question 9
Fonte dos dados de cada Pokémon na comparação (cada um precisa do detalhe completo, hoje vindo de `getPokemonDetail`):

A) Reutilizar o `PokemonRepository.getPokemonDetail(id)` existente (mesma cache strategy NETWORK_FIRST, fallback offline) — buscar os 2 detalhes.

B) Criar um caminho novo/otimizado de comparação no repositório.

X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 10
Deep link para a comparação (ex.: `pokedex://compare/{id1}/{id2}`)?

A) Sim — adicionar deep link no `DeepLinkRouter` para abrir a comparação direto.

B) Não — comparação acessível somente a partir da seleção na lista.

X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 11
Cobertura de testes esperada para a feature (padrão do projeto: reducers, use cases, repositório):

A) Padrão do projeto: testar reducer da comparação + qualquer use case novo + mapeamentos.

B) Padrão + testes de UI (Compose) da tela de comparação.

C) Mínimo: só o reducer.

X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Parte D — Extensões (opt-in)

## Question 12 — Security Extensions
Should security extension rules be enforced for this project?

A) Yes — enforce all SECURITY rules as blocking constraints (recommended for production-grade applications)

B) No — skip all SECURITY rules (suitable for PoCs, prototypes, and experimental projects)

X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 13 — Resiliency Extensions
Should the resiliency baseline be applied to this project? (directional best-practices derived from AWS Well-Architected Reliability Pillar; a starting point, not a production certification)

A) Yes — apply the resiliency baseline as directional best practices and design-time guidance

B) No — skip the resiliency baseline (suitable for PoCs, prototypes, and experimental projects)

X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 14 — Property-Based Testing Extension
Should property-based testing (PBT) rules be enforced for this project?

A) Yes — enforce all PBT rules as blocking constraints (recommended for projects with business logic, data transformations, serialization, or stateful components)

B) Partial — enforce PBT rules only for pure functions and serialization round-trips

C) No — skip all PBT rules (suitable for simple CRUD/UI-only projects or thin integration layers)

X) Other (please describe after [Answer]: tag below)

[Answer]: C
