# Application Design Plan — Feature: Comparação de Pokémon

## Objetivo
Definir componentes, interfaces, métodos (assinaturas) e dependências da feature de comparação, seguindo os padrões existentes (MVI, Channel events, mapper de extensão, NavEntry, Koin). Lógica de negócio detalhada fica no código (Functional Design foi pulado e dobrado aqui).

---

## Decisões de Design — responda as tags [Answer]:

## Question 1 — Estratégia de Use Case
A tela de comparação precisa do detalhe completo dos 2 Pokémon. Q9 já definiu **reutilizar `getPokemonDetail`**. A dúvida é onde orquestrar a busca dos dois:

A) **No ViewModel** — o `PokemonCompareViewModel` chama `GetPokemonDetailUseCase` duas vezes em paralelo (`async`/`await`). Sem novo use case. Mais simples, menos código.

B) **Novo use case em `core:domain`** — `GetPokemonComparisonUseCase(firstId, secondId)` encapsula a busca paralela e a política de erro, retornando um par de resultados. Mais testável no domínio, mas adiciona uma classe.

X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 2 — Política de falha parcial
Se a carga de **apenas um** dos 2 Pokémon falhar (ex.: id válido + id sem rede/cache):

A) **Erro de tela inteira + Retry** — comparação exige os 2; mostra `ErrorContent` com retry que recarrega ambos. (Consistente com FR-7.)

B) **Mostrar o que carregou + erro inline na coluna que falhou** — coluna com erro/retry individual, a outra renderiza normal.

X) Other (please describe after [Answer]: tag below)

[Answer]: B

## Question 3 — Indicação visual de seleção no card da lista
Quando o modo seleção está ativo e um card está selecionado, qual a indicação?

A) **Borda + badge de check** no canto do card (overlay leve, mantém o card visível).

B) **Checkbox** visível no canto de cada card no modo seleção.

C) **Overlay/escurecimento + check** sobre o card selecionado.

X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 4 — Estado da seleção ao navegar para a comparação
Ao tocar "Comparar (2)" e navegar para a tela de comparação, o que acontece com o modo seleção da lista?

A) **Limpa** seleção e **sai** do modo seleção (ao voltar, a lista está no estado normal).

B) **Mantém** seleção e modo (ao voltar, os 2 continuam marcados para ajustar/recomparar).

X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Plano de Artefatos (gerados após as respostas)

- [x] `application-design/components.md` — componentes da feature `pokemon-compare` + alterações em `pokemon-list` + nova `PokemonCompareKey`
- [x] `application-design/component-methods.md` — assinaturas (ViewModel, Reducer, Mapper, NavEntry, função pura de comparação, novos Intents/Events/State)
- [x] `application-design/services.md` — orquestração (busca paralela dos 2 detalhes, política de erro, navegação/deep link)
- [x] `application-design/component-dependency.md` — matriz de dependências + diagrama de comunicação/fluxo
- [x] `application-design/application-design.md` — documento consolidado
- [x] Validar completude e consistência (isolamento de features, regras do GUIDE)

## Decisões já fixadas (de requirements.md — não precisam de pergunta)
- Módulo novo `feature:pokemon-compare` com `ComparePokemonUiModel` próprio (convenção do GUIDE: UiModel é exclusivo por feature; compare NÃO importa nada de pokemon-detail).
- Seleção: exatamente 2; 3º bloqueado com aviso (snackbar via Event); ação "Comparar" no header ativa modo; botão "Comparar (2)" navega.
- Tela: 2 colunas fixas, scroll vertical, todas as infos do detalhe (incl. imagem); destaque do maior valor por stat (com empate).
- Rota `PokemonCompareKey(firstId, secondId)` em `core:route:keys`; nav entry em `core:route:navigation`; deep link `pokedex://compare/{id1}/{id2}` em `core:route:deeplink` com validação de ids (SECURITY-05).
