## Review Round 1 — proposal.md

### 🔴 Remaining Issues

**[R1] Comportamento do terceiro toque de seleção não está definido**

O proposal diz que o usuário "toca em até 2 cards" e que o "segundo toque deseleciona", mas não especifica o que acontece quando 2 Pokémon já estão selecionados e o usuário toca em um terceiro card distinto.

Por que causa retrabalho: o `PokemonListReducer` para `ToggleSelectForCompare` precisa tomar uma decisão concreta sobre esse caso — ignora o toque? Substitui a seleção mais antiga? Mostra feedback visual (card tremendo, toast)? Sem isso, o implementador vai inventar uma regra de produto durante o coding, e há 80% de chance de precisar reverter quando o PO vir a feature em funcionamento.

Ação necessária: adicionar uma cláusula no **What Changes** ou no escopo especificando explicitamente o comportamento (ex.: "toque em um terceiro card diferente é ignorado enquanto os dois estiverem selecionados; apenas deselecionar um libera a seleção").

---

### 🟡 Should Fix

**[S1] Divergência de nomes de módulos entre explore-brief e proposal**

O explore-brief (tabela "Módulos tocados") usa `core:route:keys` e `core:route:navigation` como módulos de destino para o NavKey e o NavEntry. O proposal usa `core:navigation`, que é o nome correto segundo o CLAUDE.md.

Por que importa: um implementador que ler o explore-brief antes da proposal pode criar um novo módulo `core:route:keys` em vez de modificar o existente `core:navigation`. Isso gera um módulo Gradle órfão difícil de remover depois.

Ação: adicionar uma nota explícita no Impact ou no Scope, ex.: "NavKey adicionado em `core:navigation` (módulo existente; `core:route:keys` no explore-brief é um alias informal para o mesmo módulo)."

---

**[S2] Estado `empty` ausente — MVI de compare e de seleção na lista**

CLAUDE.md (seção "Padrão MVI") exige explicitamente: *"Estados obrigatórios: loading, success, error, empty."* O proposal apresenta `CompareState` com `AsyncResult<CompareUiModel>` apenas Loading/Success/Error por lado. Além disso, a extensão do MVI da lista para modo seleção não menciona nenhum estado.

Por que importa: se specs e tasks seguirem o proposal sem abordar `empty`, o implementador não saberá se deve criar `AsyncResult.Empty` para o caso "nenhum Pokémon selecionado ainda" na lista, ou se `empty` não se aplica ao compare (argumento válido, mas precisa ser declarado). Testes de reducer também ficam incompletos.

Ação: adicionar uma justificativa explícita no Scope ou What Changes, ex.: "Para `CompareState`, o estado `empty` não se aplica pois a tela sempre recebe `idA` e `idB` na navegação. Para o modo seleção da lista, `isCompareMode = false && selectedForCompare.isEmpty()` equivale ao estado vazio e não requer variante adicional do `AsyncResult`."

---

**[S3] Relação entre `CompareStatRow` e `PokemonStatBar` não especificada**

O explore-brief diz que `PokemonStatBar` "já existe em `core:ui` — precisará de variante espelhada". O proposal lista `CompareStatRow` como novo componente em `core:ui`, mas não diz se ele wraps/estende `PokemonStatBar` ou parte do zero.

Por que importa: sem essa definição, há risco real de um implementador duplicar a lógica de cálculo de fill ratio e cor de barra em `CompareStatRow`, criando inconsistência visual e dois lugares para corrigir bugs futuros.

Ação: uma frase basta, ex.: "`CompareStatRow` é um novo componente que reutiliza internamente dois `PokemonStatBar` (um invertido), evitando duplicação de lógica de barra."

---

**[S4] `@Serializable` não declarado explicitamente no NavKey**

O proposal descreve `PokemonCompareKey(idA: Int, idB: Int)` mas omite a anotação `@Serializable`. O CLAUDE.md mostra `@Serializable data class PokemonDetailRoute(...)` como padrão canônico.

Por que importa: a anotação é um requisito de build (o Navigation3 serializa rotas); se a spec/task não a mencionar, um implementador menos familiarizado pode esquecer e ter um crash em runtime que é difícil de diagnosticar. Além disso, specs e tasks geradas a partir desta proposal precisam deixar esse contrato explícito.

Ação: atualizar a linha para `@Serializable data class PokemonCompareKey(val idA: Int, val idB: Int)` no proposal.

---

### 💡 Suggestion

**[C1] UX de ativação do modo seleção não mencionada**

O proposal diz "o usuário ativa o modo" mas não indica o elemento de UI (FAB, botão na toolbar, long-press no card, ícone no header). O explore-brief também não documenta isso. Para a tela de listagem existente, adicionar qualquer novo affordance afeta o layout.

Sugestão: uma frase opcional no What Changes, ex.: "Ativação via botão 'Compare' na toolbar da tela de lista." Não bloqueia a proposal, mas evita uma pergunta de design na hora da spec.

---

**[C2] `core:domain` ausente na lista de módulos afetados (Impact)**

A seção Impact lista `feature:pokemon-compare (novo), feature:pokemon-list, core:navigation, core:ui, :app`. O novo feature depende de `GetPokemonDetailUseCase` em `core:domain`, mas `core:domain` não aparece como "afetado".

Tecnicamente `core:domain` não é *modificado* — apenas consumido —, então a ausência pode ser intencional. Se a lista se refere apenas a módulos que recebem commits, está correto. Se é uma lista de dependências da change, `core:domain` deveria aparecer. Clarificar a intenção resolve a ambiguidade.

---

## Review Round 2 — proposal.md

### 🟡 Should Fix

**[S5] Contradição interna na descrição do estado `empty` do compare**

O proposal afirma simultaneamente duas coisas incompatíveis:
- Linha "What Changes": *"Cada lado da tela de comparação tem **loading, sucesso, erro e empty independentes**"* — implica que `AsyncResult` precisa de uma variante `Empty` e que cada lado pode atingi-la de forma independente.
- Linha seguinte: *"O estado `empty` no compare screen é aplicável quando `idA == idB`"* — isso é uma condição de invariante **de tela**, não de lado; ambos os lados são afetados simultaneamente, nunca de forma independente.

Por que importa: o implementador do `CompareViewModel` precisará decidir entre duas abordagens que resultam em código completamente diferente:
1. Adicionar `data object Empty : AsyncResult<Nothing>` e emiti-lo para cada `AsyncResult` quando `idA == idB`.
2. Tratar `idA == idB` como um estado de tela inteira (ex.: `CompareScreenState.InvalidInput`), sem `Empty` na `AsyncResult`.

Sem resolução na proposal, essa ambiguidade vai para specs/tasks, e o implementador vai inventar a regra, com chance real de retrabalho.

Ação sugerida: escolher uma das duas formulações e ser consistente. Ex.: *"Para o compare screen, o CLAUDE.md exige `empty`; aqui é tratado como estado de tela inteira (`InvalidInput`) quando `idA == idB`, verificado no ViewModel antes de disparar qualquer load. `AsyncResult` permanece com três variantes (Loading/Success/Error)."*

---

### 💡 Suggestion

**[C1] UX de ativação do modo seleção ainda não especificada** *(mantida do Round 1)*

O proposal diz "o usuário ativa o modo" sem indicar o affordance de UI (FAB, botão na toolbar, long-press etc.). Não bloqueia, mas evita uma pergunta de design na hora da spec.

---

**[C2] `core:domain` ausente no Impact** *(mantida do Round 1)*

`feature:pokemon-compare` consome `GetPokemonDetailUseCase` de `core:domain`, que não aparece na lista de módulos afetados. Se a lista reflete apenas módulos que recebem commits, está correto; caso contrário, vale acrescentar uma nota de leitura.

---

## Review Round 1 — specs

Artefatos revisados:
- `specs/pokemon-compare-screen/spec.md`
- `specs/pokemon-list-selection-mode/spec.md`

---

### 🔴 Remaining Issues

**[R1] `CompareState.InvalidInput` referencia um tipo que contradiz o shape de `CompareState` no explore-brief (`pokemon-compare-screen`, Req. 4)**

O spec escreve: *"the system SHALL display `CompareState.InvalidInput` for the entire screen"*. Isso implica que `CompareState` é uma sealed interface com a variante `InvalidInput`. No entanto, o explore-brief define `CompareState` como uma `data class CompareState(val pokemonA: AsyncResult<CompareUiModel>, val pokemonB: AsyncResult<CompareUiModel>)` — uma data class simples, sem variantes sealed.

Por que causa retrabalho: o implementador do `CompareViewModel` precisará decidir sozinho entre três abordagens com código completamente diferente:
- (A) Converter `CompareState` em sealed interface com `Content(...)` e `InvalidInput` — mudança de contrato com impacto em todos os `when(state)` na UI.
- (B) Adicionar campo `isInvalidInput: Boolean` à data class — viola a semântica sealed esperada pelo nome `CompareState.InvalidInput`.
- (C) Criar um wrapper `CompareScreenState` sealed no nível da tela — dobra a quantidade de tipos de estado a gerenciar.

A escolha errada gera refatoração não-trivial no ViewModel, na Screen e potencialmente nos testes.

Ação necessária: o spec deve definir explicitamente o shape de `CompareState` — sealed interface ou data class com campo extra — e garantir consistência com o explore-brief.

---

**[R2] Feedback de limite de seleção não especificado como `PokemonListEvent` (`pokemon-list-selection-mode`, Req. 2c)**

O Requisito 2 e o Scenario "Tapping third Pokémon when two are already selected is ignored" dizem que o sistema deve *"show a snackbar/toast indicating the selection limit has been reached"*. O spec não especifica como esse efeito efêmero é entregue.

Por que causa retrabalho: no padrão MVI mandatório do CLAUDE.md (*UI Screen → Intent → ViewModel → Reducer → State Update → UI*, com Events para efeitos efêmeros como navegação e snackbar), um snackbar é um `Event` one-shot — nunca estado. Se o spec não nomear o evento (ex.: `data object ShowSelectionLimitReached : PokemonListEvent`), há risco real de o implementador adicionar `showLimitReachedToast: Boolean` ao `PokemonListState`, o que é um antipadrão MVI e causa reexibição do toast a cada recomposição.

Ação necessária: adicionar ao spec a declaração do evento, ex.: *"the system SHALL emit `ShowSelectionLimitReached : PokemonListEvent` to the screen, which renders it as a snackbar/toast"*.

---

### 🟡 Should Fix

**[S1] Sem requisito normativo para layout das seções artwork / tipos / altura-peso (`pokemon-compare-screen`)**

O spec cobre stats (Req. 2), erros (Req. 3), invalid input (Req. 4) e scroll (Req. 5) com cláusulas SHALL. O layout das seções de artwork + nome/número, chips de tipo e altura/peso aparece apenas como asserção de cenário dentro do Req. 1 ("side A displays Bulbasaur's artwork, name (#001), type chips, height, weight"). Não existe um requisito normativo que diga qual conteúdo cada lado MUST renderizar quando em estado de sucesso.

Por que importa: na verificação pós-implementação (`/opsx:verify`), não haverá baseline para checar se artwork e tipos foram implementados corretamente. Um implementador que omitir height/weight não viola nenhum SHALL do spec.

Ação: adicionar um requisito do tipo "WHEN both Pokémon load successfully, each side SHALL display: official artwork, formatted name and Pokédex number, type chip(s), height, and weight."

---

**[S2] Escape hatch "or its internal rendering logic" enfraquece o requisito de não-duplicação (`pokemon-compare-screen`, Req. 2)**

O Scenario "CompareStatRow reuses PokemonStatBar internals" diz: *"the bar logic is provided by `PokemonStatBar` composable (or its internal rendering logic)"*. O parêntese permite ao implementador copiar a lógica interna de `PokemonStatBar` para dentro de `CompareStatRow`, o que é exatamente a duplicação que o proposal e o CLAUDE.md proíbem.

Por que importa: dois lugares com lógica de barra = dois lugares para corrigir bugs de fill ratio, cor ou acessibilidade. O explore-brief já decidiu "reutiliza `PokemonStatBar`"; o spec deve ser mais restritivo, não mais permissivo.

Ação: remover o parêntese e alterar para: *"the bar is rendered by composing `PokemonStatBar` directly — no duplicated bar drawing code exists in `CompareStatRow`."*

---

**[S3] Comportamento de `isCompareMode` após navegação para compare não especificado (`pokemon-list-selection-mode`, Req. 2)**

O Scenario "Selecting second Pokémon triggers navigation" especifica que `NavigateToCompare` é emitido, mas não define o estado de `isCompareMode` e `selectedForCompare` após a navegação. Quando o usuário pressiona "back" para retornar à lista, o estado anterior persiste (2 Pokémon selecionados, modo compare ativo) ou é resetado?

Por que importa: se o estado persiste, o usuário retorna à lista com 2 Pokémon selecionados e precisa deselecionar manualmente ou pressionar o toggle duas vezes. Se deve ser resetado, essa lógica precisa estar no reducer/ViewModel como parte do handler do evento `NavigateToCompare`. Sem spec, o implementador inventará uma das duas, e a decisão de produto é diferente.

Ação: adicionar uma cláusula ao scenario ou um novo scenario: ex. "AND `isCompareMode` becomes `false` AND `selectedForCompare` is cleared to `emptySet()`" — ou justificar por que a persistência é intencional.

---

### 💡 Suggestion

**[C1] `@Serializable` não mencionado em `PokemonCompareKey` no spec de isolamento de feature (`pokemon-list-selection-mode`, Req. 3)**

O Requisito 3 e o scenario de navegação referenciam `PokemonCompareKey(idA: Int, idB: Int)` sem a anotação `@Serializable`. O padrão canônico do CLAUDE.md exige a anotação (ex.: `@Serializable data class PokemonDetailRoute(...)`). O problema já foi apontado para o proposal (Round 1, S4) mas não foi carregado para o spec.

Sugestão: mencionar `@Serializable data class PokemonCompareKey(val idA: Int, val idB: Int)` ao referenciar o NavKey no Requisito 3.

---

**[C2] Mecanismo de scroll não especificado (`pokemon-compare-screen`, Req. 5)**

O Requisito 5 define que o conteúdo deve ser scrollável mas não especifica se a implementação deve usar `Column + verticalScroll(rememberScrollState())` (adequado para conteúdo fixo com ≤10 rows) ou `LazyColumn` (overhead desnecessário para conteúdo não paginado). Não bloqueia, mas pode gerar round de code review.

Sugestão: uma frase, ex.: "The layout SHALL use `Column` with `verticalScroll(rememberScrollState())` since the content is fixed and non-paginated."

---

## Review Round 2 — specs

Artefatos revisados:
- `specs/pokemon-compare-screen/spec.md`
- `specs/pokemon-list-selection-mode/spec.md`

### Verificação das correções do Round 1

| Issue | Status | Observação |
|---|---|---|
| R1 — `CompareState` como `data class` com `isInvalidInput: Boolean` | ✅ Resolvido | Snippet Kotlin explícito adicionado ao Req. 4 |
| R2 — `ShowSelectionLimitReached : PokemonListEvent` declarado | ✅ Resolvido | Regra (c) agora nomeia o evento e proíbe armazenamento no state |
| S1 — SHALL explícito para artwork/tipos/altura-peso | ✅ Resolvido | Novo requisito normativo adicionado com cenário completo |
| S2 — "or its internal rendering logic" removido | ✅ Resolvido | Cenário agora diz "delegates" e "no bar-drawing logic copied" |
| S3 — Reset ao retornar do compare adicionado | ✅ Resolvido | Novo requisito "Compare mode resets after returning" com SHALL e cenário |
| C1 — `@Serializable data class PokemonCompareKey : NavKey` explicitado | ✅ Resolvido | Req. 3 do spec de seleção agora contém a declaração completa |

---

### 🔴 Remaining Issues

*(Nenhum — todos os bloqueantes do Round 1 foram corretamente resolvidos.)*

---

### 🟡 Should Fix

**[S4] `AsyncResult<T>` referenciado no spec sem declarar módulo proprietário nem sinalizar que é tipo novo (`pokemon-compare-screen`, Req. 4 e Req. 1)**

O spec apresenta o snippet de `CompareState`:
```kotlin
data class CompareState(
    val isInvalidInput: Boolean = false,
    val pokemonA: AsyncResult<CompareUiModel> = AsyncResult.Loading,
    val pokemonB: AsyncResult<CompareUiModel> = AsyncResult.Loading,
)
```
e referencia `AsyncResult.Loading` e variantes implícitas `Success`/`Error` em vários cenários — mas em nenhum momento o spec declara:
1. Que `AsyncResult<T>` é um **tipo novo** a ser criado (não existe em `core:common` conforme CLAUDE.md, que lista apenas `DomainError`, `Result<T>`, `ErrorHandler`, `DispatcherProvider`).
2. Qual módulo Gradle é seu dono.

Por que causa retrabalho: se o implementador cria `AsyncResult<T>` dentro de `feature:pokemon-compare`, ele se torna feature-private. Qualquer futura feature que precise de um wrapper loading/success/error terá que duplicá-lo ou criar uma dependência cruzada entre features — violação da regra "features não se conhecem". A localização correta é `core:common`, mas sem essa definição no spec o task gerado provavelmente não inclui uma tarefa de "adicionar `AsyncResult<T>` ao módulo `core:common`", deixando uma lacuna no grafo de dependências.

O explore-brief define a sealed interface, mas é um documento de exploração, não uma spec vinculante. O spec precisa referenciar o tipo de forma normativa.

Ação sugerida: adicionar uma nota ao Req. 1 ou Req. 4, ex.: *"Note: `AsyncResult<T>` is a new `sealed interface` to be added to `core:common` (see explore-brief for shape: `Loading`, `Success(data: T)`, `Error(error: DomainError)`). `feature:pokemon-compare` depends on `core:common`."*

---

**[S5] Mecanismo MVI do reset de compare mode não especificado (`pokemon-list-selection-mode`, Req. "Compare mode resets after returning from compare screen")**

O requisito diz *"the system SHALL reset `isCompareMode = false` and `selectedForCompare = emptySet()`"* quando o usuário retorna da tela de comparação, mas não especifica o mecanismo MVI que dispara essa mudança de estado.

Por que causa retrabalho: no padrão MVI obrigatório do projeto, mudanças de estado chegam via intents. Um "retorno da tela de compare" não é um Intent óbvio — o implementador precisará escolher entre pelo menos três abordagens com código completamente diferente:
- (A) Intent `ResetCompareMode` disparado por um `DisposableEffect`/`LaunchedEffect` na tela de lista ao reaparecer na back-stack.
- (B) Handler no evento `NavigateToCompare` que emite o reset imediatamente ao navegar (antes de chegar na tela de compare).
- (C) Callback `onBackPressed` / `BackHandler` na `CompareScreen` que dispara o intent antes do pop.

As três abordagens resultam em comportamento diferente em edge cases (ex.: configuração change enquanto na tela de compare). Sem a escolha explícita no spec, o implementador inventa a regra e há chance real de inconsistência de UX ou retrabalho.

Ação sugerida: nomear o mecanismo, ex.: *"The reset is triggered by a `ResetCompareMode : PokemonListIntent` dispatched in a `LaunchedEffect` keyed on navigation resumption (e.g., observing the back-stack entry becoming the top entry again via Navigation3 lifecycle)."*

---

### 💡 Suggestion

**[C1] Mecanismo de scroll não especificado (`pokemon-compare-screen`, Req. 5)** *(mantida do Round 1 C2)*

O requisito de scroll não prescreve `Column + verticalScroll(rememberScrollState())` vs `LazyColumn`. Para conteúdo fixo e não paginado (6 stat rows + header fixo), `Column + verticalScroll` é mais adequado — mas a ausência pode gerar comentário de code review.

Sugestão: *"The layout SHALL use `Column` with `verticalScroll(rememberScrollState())` since content is bounded and non-paginated."*

---

## Review Round 3 — specs

Artefatos revisados:
- `specs/pokemon-compare-screen/spec.md`
- `specs/pokemon-list-selection-mode/spec.md`

### Verificação das correções do Round 2

| Issue | Status | Observação |
|---|---|---|
| S4 — `AsyncResult<T>` declarado em `core:common` com shape explícito | ✅ Resolvido | Snippet Kotlin com módulo owner e variantes `Loading`/`Success`/`Error` adicionado ao Req. 5 do compare screen |
| S5 — Reset especifica `ResetCompareMode`, `LifecycleResumeEffect` e comportamento do reducer | ✅ Resolvido | Req. "Compare mode resets" agora tem bloco MVI mechanism com intent, hook e transformação de estado |
| C1 — Scroll prescrito como `Column + Modifier.verticalScroll` | ✅ Resolvido | Req. scroll agora nomeia explicitamente `Column + Modifier.verticalScroll` e justifica a exclusão de `LazyColumn` |

---

### 🔴 Remaining Issues

*(Nenhum — todos os bloqueantes anteriores foram corretamente resolvidos.)*

---

### 🟡 Should Fix

**[S6] `NavigateToCompare` não declarado como subtipo de `PokemonListEvent` (`pokemon-list-selection-mode`, Req. 2 e Req. 4)**

O spec referencia `NavigateToCompare(idA = 1, idB = 4)` como "event" nos cenários e no Req. 4 ("WHEN `NavigateToCompare(idA, idB)` event is emitted from `feature:pokemon-list`"), mas nunca fornece uma declaração formal de tipo — ao contrário de `ShowSelectionLimitReached`, que é explicitamente declarado na regra (c) como `ShowSelectionLimitReached : PokemonListEvent`.

Por que causa retrabalho: sem `data class NavigateToCompare(val idA: Int, val idB: Int) : PokemonListEvent`, o implementador precisa inferir a assinatura do evento. Se inferir incorretamente (ex.: `object NavigateToCompare` sem parâmetros, ou campos com nomes diferentes), o handler no `PokemonListScreen` que chama `navigator.navigateTo(PokemonCompareKey(event.idA, event.idB))` não compila sem refatoração. Além disso, a task de "adicionar o evento ao sealed interface `PokemonListEvent`" provavelmente ficará ausente ou incompleta no `tasks.md`.

Ação: adicionar ao Req. 2 ou Req. 4 a declaração explícita: `data class NavigateToCompare(val idA: Int, val idB: Int) : PokemonListEvent`.

---

**[S7] "Reducer emits event" contradiz o fluxo MVI do CLAUDE.md (`pokemon-list-selection-mode`, Req. 4 — scenario)**

O cenário do Req. 4 afirma: *"WHEN the reducer detects `|selectedForCompare| == 2` after adding id = 4, THEN `NavigateToCompare(idA = 1, idB = 4)` event is emitted to the host."*

O CLAUDE.md define explicitamente: `UI Screen → Intent → ViewModel → Reducer → State Update → UI ↘ Event (navegação, snackbar)`. No padrão canônico do projeto, **Events são emitidos pelo ViewModel** (tipicamente via `SharedFlow`), não pelo Reducer. Reducers são funções puras `(State, Intent) → State` — sem efeitos colaterais, sem emissão de eventos — o que é fundamental para testabilidade unitária (os testes de reducer usam apenas `assertEquals(expectedState, reducer(state, intent))`).

Por que causa retrabalho: se o implementador seguir o spec literalmente e colocar a emissão de `NavigateToCompare` dentro do Reducer, quebrará a pureza da função. Testes de reducer que verifiquem apenas o estado retornado não conseguirão capturar eventos, forçando um redesign do mecanismo de teste. O code review quase certamente rejeitará o Reducer com efeito colateral e exigirá mover a lógica para o ViewModel.

Ação: corrigir o cenário para deixar explícito que é o **ViewModel** que observa a mudança de estado pós-reduce e emite o evento. Ex.: *"The ViewModel observes `state.selectedForCompare.size == 2` after reducing `ToggleSelectForCompare(id = 4)` and emits `NavigateToCompare(idA, idB)` via its event channel."*

---

**[S8] `CompareUiModel` nunca declarado no spec (`pokemon-compare-screen`, Req. 1, 2, 4)**

O spec usa `AsyncResult<CompareUiModel>` no shape de `CompareState` e descreve o que exibir (artwork URL, nome, número formatado, chips de tipo, altura, peso, 6 stats), mas em nenhum momento define os campos de `CompareUiModel`. O mapeador `GetPokemonDetailUseCase result → CompareUiModel` não tem contrato de spec para trabalhar.

Por que causa retrabalho: sem shape explícito, dois cenários de risco surgem: (1) o implementador cria `CompareUiModel` com campos diferentes dos que o layout da tela espera (ex.: `imageUrl` vs. `artworkUrl`, `formattedNumber: String` vs. `number: Int`), gerando inconsistência entre mapper e composable; (2) o campo de stats — seja `List<StatUiModel>` ou `Map<String, Int>` — impacta diretamente como `CompareStatRow` itera sobre ele. Sem spec, o implementador escolhe livremente a estrutura, e o componente fica acoplado a uma decisão não documentada. A task de "criar `CompareUiModel`" no `tasks.md` ficará incompleta sem os campos especificados.

Ação: adicionar ao Req. 1 ou Req. 4 um snippet de shape mínimo, ex.:
```kotlin
data class CompareUiModel(
    val id: Int,
    val name: String,
    val formattedNumber: String, // ex.: "#001"
    val imageUrl: String,
    val types: List<String>,
    val heightM: Float,
    val weightKg: Float,
    val stats: List<StatUiModel>,
)
data class StatUiModel(val name: String, val value: Int, val maxValue: Int = 255)
```

---

### 💡 Suggestion

**[C1] `LifecycleResumeEffect` reseta compare mode em TODOS os resumes de lifecycle, não só ao retornar do compare (`pokemon-list-selection-mode`, Req. "Compare mode resets")**

O título do requisito é "Compare mode resets after returning from compare screen", mas o mecanismo (`LifecycleResumeEffect`) dispara em qualquer `onResume` da tela de lista — inclusive quando o usuário responde a uma ligação, abre a barra de notificações ou alterna apps enquanto está no processo de seleção com 1 Pokémon já escolhido. Nesse caso, o compare mode é resetado involuntariamente.

Isso pode ser uma decisão de produto intencional ("qualquer saída da tela reseta o modo"), e se for, o spec deveria dizer isso explicitamente. Se não for intencional, o mecanismo correto seria observar o back-stack para detectar especificamente o retorno da `PokemonCompareKey` entry.

Sugestão: acrescentar uma frase de decisão de produto, ex.: *"Note: this reset applies to any lifecycle resume of the list screen — compare mode is treated as a transient UI mode that should not persist across app background/foreground transitions."*

---

**[C2] Shape completo de `PokemonListState` com os novos campos não aparece em nenhum spec (`pokemon-list-selection-mode`)**

O proposal menciona `isCompareMode: Boolean = false` e `selectedForCompare: Set<Int> = emptySet()` como extensão aditiva de `PokemonListState`. Os specs os referenciam consistentemente nos cenários, mas nenhum spec mostra o `data class PokemonListState(...)` com os novos campos incluídos.

Não bloqueia — os campos são claramente inferíveis dos cenários. Mas uma declaração explícita (mesmo que truncada com `// ... existing fields ...`) facilitaria a verificação pós-implementação e garantiria que os testes de reducer incluam os campos corretos.

---

## Review Round 4 — specs

Artefatos revisados:
- `specs/pokemon-compare-screen/spec.md`
- `specs/pokemon-list-selection-mode/spec.md`

### Verificação das correções declaradas do Round 3

| Issue | Status | Observação |
|---|---|---|
| S6 — `NavigateToCompare` e `ShowSelectionLimitReached` declarados como `: PokemonListEvent` | ✅ Resolvido | Bloco Kotlin nas linhas 83–87 do spec de seleção declara ambos explicitamente |
| S7 — ViewModel emite eventos; reducer permanece função pura | ✅ Resolvido | Nota normativa adicionada (linha 89) + cenário "Navigation event dispatched correctly" agora diz "the ViewModel emits" |
| S8 — `CompareUiModel` e `StatUiModel` declarados com todos os campos, module owner e tipo de coleção | ✅ Resolvido | Snippet Kotlin completo adicionado ao spec do compare screen com ownership (`feature:pokemon-compare`) explícito |

---

### 🔴 Remaining Issues

*(Nenhum — todos os bloqueantes anteriores foram corretamente resolvidos.)*

---

### 🟡 Should Fix

**[S9] `Side` referenciado em `RetryPokemon(side: Side)` sem declaração de tipo ou módulo owner (`pokemon-compare-screen`, Req. 3)**

O Requisito 3 define a intent de retry como `RetryPokemon(side: Side)` e os cenários fazem referência a `Side.A` implicitamente ("Retry recovers the failed side" / "side A transitions to loading state"). No entanto, `Side` nunca é declarado no spec — sem variants, sem shape, sem módulo owner.

Por que causa retrabalho: o implementador precisará criar um tipo `Side` para a intent `RetryPokemon` sem qualquer contrato spec. As perguntas que surgem durante a implementação são:
1. Onde `Side` mora? Se for colocado em `feature:pokemon-compare/ui/intent/`, está correto. Se for colocado em `core:model` (confundindo-o como domain type), cria dependência desnecessária.
2. Qual é o tipo exato? `enum class Side { A, B }` é o mais óbvio, mas `sealed interface` ou simples `Boolean` ("isSideA") também são opções que o implementador pode escolher.
3. A task de "declarar a intent `RetryPokemon`" no `tasks.md` provavelmente ficará incompleta sem a declaração de `Side`, da mesma forma que S8 detectou a ausência de `CompareUiModel`.

O padrão estabelecido pelos rounds anteriores (S6, S8) é declarar todos os tipos novos referenciados no spec. `Side` segue o mesmo padrão.

Ação: adicionar ao Req. 3 a declaração, ex.:
```kotlin
// feature:pokemon-compare — declared alongside other intents
enum class Side { A, B }
data class RetryPokemon(val side: Side) : CompareIntent
```

---

### 💡 Suggestion

**[C1] `LifecycleResumeEffect` reseta compare mode em TODOS os resumes, não apenas ao retornar do compare screen** *(mantida do Round 3)*

O mecanismo `LifecycleResumeEffect` dispara em qualquer `onResume` da tela de lista — inclusive ao retornar de uma ligação, abrir notificações ou alternar apps. Se o usuário tiver selecionado 1 Pokémon e o app for para background, ao retornar o compare mode será resetado involuntariamente.

Se for decisão de produto intencional ("compare mode é transiente e não persiste além da sessão de tela"), o spec deveria dizer isso explicitamente. Sugestão de frase: *"Note: this reset applies to any lifecycle resume of the list screen — compare mode is treated as a transient UI mode that does not persist across app background/foreground transitions."*

---

**[C2] Shape completo de `PokemonListState` com os novos campos não aparece no spec** *(mantida do Round 3)*

Os campos `isCompareMode: Boolean` e `selectedForCompare: Set<Int>` são inferíveis dos cenários, mas uma declaração explícita (mesmo que truncada com `// ... existing fields ...`) facilitaria a verificação pós-implementação e garantiria cobertura correta nos testes de reducer.

---

## Review Round 5 — specs

Artefatos revisados:
- `specs/pokemon-compare-screen/spec.md`
- `specs/pokemon-list-selection-mode/spec.md`

### Verificação do fix declarado do Round 4

| Issue | Status | Observação |
|---|---|---|
| S9 — `Side` e `CompareIntent` declarados explicitamente com `Load` e `RetryPokemon` | ✅ Resolvido | Snippet Kotlin nos lines 41–49 do compare-screen spec declara `enum class Side { A, B }` e `sealed interface CompareIntent { Load(idA, idB); RetryPokemon(side: Side) }` com module owner `feature:pokemon-compare` |

---

### Verificação de regressões (todos os issues dos rounds anteriores)

Todos os bloqueantes e should-fixes dos Rounds 1–4 foram verificados nos artefatos atuais e permanecem resolvidos:

| Issue | Verificado em |
|---|---|
| R1 — comportamento do 3º toque de seleção | `pokemon-list-selection-mode` Req. 2, regra (c) |
| R2 / S7 — ViewModel emite eventos; reducer é função pura | `pokemon-list-selection-mode` linha 89 + scenario "Navigation event dispatched correctly" |
| S2 / S5 — estado `empty` tratado como `isInvalidInput: Boolean` de tela inteira | `pokemon-compare-screen` Req. 5, shape de `CompareState` |
| S3 — reset ao retornar do compare via `LifecycleResumeEffect` + `ResetCompareMode` | `pokemon-list-selection-mode` Req. 3 |
| S4 — `AsyncResult<T>` declarado em `core:common` | `pokemon-compare-screen` linhas 106–114 |
| S6 — `NavigateToCompare` e `ShowSelectionLimitReached` declarados como `: PokemonListEvent` | `pokemon-list-selection-mode` linhas 83–87 |
| S8 — `CompareUiModel` e `StatUiModel` com shape completo e module owner | `pokemon-compare-screen` linhas 85–104 |
| S9 — `Side` e `CompareIntent` declarados | `pokemon-compare-screen` linhas 41–49 |
| `@Serializable data class PokemonCompareKey : NavKey` | `pokemon-list-selection-mode` Req. 4 |
| `Column + Modifier.verticalScroll` prescrito | `pokemon-compare-screen` Req. 5 (scroll) |
| `CompareStatRow` delega para `PokemonStatBar` sem duplicação | `pokemon-compare-screen` Req. 2, scenario "CompareStatRow reuses PokemonStatBar without code duplication" |

---

### 🔴 Remaining Issues

*(Nenhum — todos os bloqueantes anteriores foram corretamente resolvidos. Artefatos aprovados para `/opsx:apply`.)*

---

### 🟡 Should Fix

*(Nenhum — todos os should-fixes anteriores foram resolvidos.)*

---

### 💡 Suggestion

**[C1] `LifecycleResumeEffect` reseta compare mode em todos os resumes, não apenas ao retornar do compare** *(mantida do Round 3, não endereçada)*

O mecanismo `LifecycleResumeEffect` dispara em qualquer `onResume` da tela de lista — inclusive ao retornar de uma chamada, abrir notificações ou alternar apps enquanto o usuário tem 1 Pokémon selecionado. O compare mode seria resetado involuntariamente nesses casos.

Se for decisão de produto intencional, sugestão de frase no Req. 3: *"Note: this reset applies to any lifecycle resume of the list screen — compare mode is treated as a transient UI mode that does not persist across app background/foreground transitions."*

---

**[C2] Shape de `PokemonListState` com os novos campos não aparece explicitamente no spec** *(mantida do Round 3, não endereçada)*

`isCompareMode: Boolean` e `selectedForCompare: Set<Int>` são inferíveis dos cenários. Uma declaração truncada (ex.: `data class PokemonListState(/* … existing fields … */, val isCompareMode: Boolean = false, val selectedForCompare: Set<Int> = emptySet())`) facilitaria a verificação pós-implementação e garantiria cobertura correta nos testes de reducer. Não bloqueia.

---

## Review Round 1 — design.md

### 🔴 Remaining Issues

**[R1] Navigation wiring usa `NavEntryDecorator` como receiver de registro de entries — tipo errado em Navigation3**

A seção Navigation (linhas 116–121) define:
```kotlin
// feature:pokemon-compare — entry provider (ex: CompareNavEntry.kt)
fun NavEntryDecorator.pokemonCompareEntry(navigator: Navigator) {
    entry<PokemonCompareKey> { key ->
        CompareScreen(idA = key.idA, idB = key.idB, onBack = { navigator.goBack() })
    }
}
```
e a chama como função livre dentro de `NavDisplay`:
```kotlin
fun NavDisplay(navigator: Navigator) {
    pokemonCompareEntry(navigator)  // sem receiver NavEntryDecorator
}
```

Há dois problemas distintos que causam falha de compilação:

1. **Receiver type errado**: Em Navigation3, `NavEntryDecorator` é a interface para *decorar* entries já registradas (ex.: adicionar animações ou shared elements). O *registro* de entries acontece no contexto `EntryProviderBuilder` via `entryProvider { entry<Key> { ... } }`. Usar `NavEntryDecorator` como receiver de `entry<PokemonCompareKey>` causará `Unresolved reference: entry` em tempo de compilação.

2. **Chamada sem receiver**: `pokemonCompareEntry(navigator)` é chamado como função livre dentro de `NavDisplay`, mas a função é declarada como extension em `NavEntryDecorator`. Em Kotlin, uma extension function não pode ser chamada sem instância do receiver — isso é um erro de compilação.

Por que causa retrabalho: a seção Navigation é o único código de wiring explícito do design para a integração de `feature:pokemon-compare` no `:app`. Se o implementador copiar essa estrutura, o `:app` não compilará. A correção requer mudar o receiver para `EntryProviderBuilder` (ou equivalente) e ajustar a chamada no `AppNavDisplay`, o que pode implicar revisão de como as demais features registram suas entries (para manter consistência).

Ação necessária: substituir `NavEntryDecorator` por `EntryProviderBuilder` (ou o tipo correto do Navigation3 1.1.1 que o projeto usa) e garantir que a chamada no `AppNavDisplay` seja dentro do lambda `entryProvider { ... }`. Ex.:
```kotlin
// feature:pokemon-compare — CompareNavEntry.kt
fun EntryProviderBuilder.pokemonCompareEntries() {
    entry<PokemonCompareKey> { key ->
        CompareScreen(idA = key.idA, idB = key.idB)
    }
}

// :app — AppNavDisplay.kt
NavDisplay(
    backStack = backStack,
    entryProvider = entryProvider {
        // ... other entries ...
        pokemonCompareEntries()
    }
)
```

---

**[R2] `GetPokemonDetailUseCase` retorna `Flow<Result<Pokemon>>` mas o ViewModel usa `async { useCase(id) }` como se fosse suspend function**

A linha 126 do design declara explicitamente a assinatura:
```
Reutiliza GetPokemonDetailUseCase(id: Int): Flow<Result<Pokemon>>
```
As linhas 97–98 descrevem a lógica do ViewModel como:
```
3. lança dois async { GetPokemonDetailUseCase(id) } em paralelo via viewModelScope
```

`async { GetPokemonDetailUseCase(id) }` com uma use case que retorna `Flow<Result<Pokemon>>` resulta em `Deferred<Flow<Result<Pokemon>>>`, não em `Deferred<Result<Pokemon>>`. Não há erro de compilação na expressão `async { ... }` em si, mas a tentativa de usar o resultado (`deferred.await()`) produz um `Flow` que nunca foi coletado — os Pokémon nunca chegam ao state. Isso é um bug silencioso de difícil diagnóstico.

Além disso, o mecanismo de retry (`RetryPokemon(side: Side)`) precisa cancelar e reiniciar a coleta do Flow para o lado afetado. O padrão `async/await` não expõe o `Job` de cada lado por nome, tornando o cancelamento seletivo não-trivial sem um job tracking explícito.

Por que causa retrabalho: o implementador do `CompareViewModel` seguirá o `async/await` descrito, criará um `Deferred<Flow<...>>` em vez de `Deferred<Result<...>>`, e o loading ficará eterno. O diagnóstico é confuso porque não há crash. A correção exige refatoração da estratégia de carregamento e do retry.

Ação necessária: o design deve escolher e especificar UMA das abordagens:
- **(A) Coletar primeira emissão como suspend**: `async { useCase(id).first() }` — funciona para o carregamento inicial, mas precisa explicar como o retry relança sem reprocessar emissões anteriores.
- **(B) Usar `launch` + `collect` com Job por lado**: cada lado tem um `Job` nomeado (`jobA`/`jobB`) cancelável; retry cancela e relança o job do lado afetado. Requer tracking explícito no ViewModel.
- **(C) Redefinir a use case como suspend**: se o use case puder retornar `Result<Pokemon>` diretamente (sem stream), `async/await` é válido — mas isso é uma mudança de contrato no `core:domain` que precisa de justificativa.

---

### 🟡 Should Fix

**[S1] Grafo de dependências de `feature:pokemon-compare` ausente no design**

A tabela "Module & Layer Impact" lista os arquivos que serão criados em `feature:pokemon-compare`, mas nunca especifica as dependências Gradle do módulo. O explore-brief (linhas 48–55) contém o grafo:
```
feature:pokemon-compare
    ├── core:common
    ├── core:design-system
    ├── core:domain
    ├── core:model
    ├── core:navigation    (PokemonCompareKey)
    └── core:ui            (CompareStatRow)
```
O design é o artefato vinculante para implementação — o explore-brief é apenas exploração. Sem o grafo no design, a task de "criar `build.gradle.kts` de `feature:pokemon-compare`" no `tasks.md` ficará sem contrato verificável: o implementador pode omitir `core:navigation` (necessário para `PokemonCompareKey`) ou `core:ui` (necessário para `CompareStatRow`), causando falhas de compilação só detectadas ao final.

Ação: adicionar uma sub-seção "Gradle Dependencies — `feature:pokemon-compare`" com a lista explícita de `implementation(project(...))`.

---

**[S2] Race condition nas atualizações de estado concorrentes do ViewModel**

Os pontos 3–4 do ViewModel descrevem:
> "lança dois `async { ... }` em paralelo" e "Cada resultado atualiza apenas o seu lado no state (`pokemonA` ou `pokemonB`)"

Se as duas coroutines fazem `_state.value = _state.value.copy(pokemonA = result)` e `_state.value = _state.value.copy(pokemonB = result)` concorrentemente, há uma race read-modify-write clássica: a segunda `copy` lê o estado *antes* da primeira escrever e sobrescreve o resultado do lado A com `Loading` de volta — o estado final só mostra o lado que terminar *por último*.

Por que causa retrabalho: o bug se manifesta de forma não-determinística (depende da velocidade da rede para cada lado). Em testes unitários com `TestDispatcherProvider` no modo `UnconfinedTestDispatcher` o comportamento pode ser diferente do runtime real, dificultando a reprodução. O code review quase certamente pedirá `_state.update { it.copy(...) }`.

Ação: o design deve prescrever o uso de `_state.update { current -> current.copy(pokemonA = result) }` (operação atômica via `StateFlow.update`) para cada atualização de lado.

---

### 💡 Suggestion

**[C1] `navigator.goBack()` — verificar nome real do método em Navigation3 1.1.1**

Linha 119 usa `onBack = { navigator.goBack() }`. Em Navigation3, o método de back navigation é tipicamente `navigateBack()` no `Navigator`. Se o nome estiver errado, o `:app` não compila ao registrar o entry. Não é possível confirmar sem acesso ao código do app, mas o design deveria usar o nome exato que o projeto já utiliza em outros entries — ou adicionar uma nota `// verify exact method name`.

---

## Review Round 2 — design.md

**Fixes validados de Round 1:**
- ✅ **R1**: Navigation wiring substituído corretamente pelo padrão `koin-compose-navigation3` — `navigation<PokemonCompareKey>` DSL dentro de `activityRetainedScope`, `NavDisplay` usa `entryProvider = getEntryProvider()`. Alinhado com o recipe de referência.
- ✅ **R2**: Carregamento paralelo agora usa dois `launch` independentes com `.first()` + try/catch + `_state.update { }`. Race condition eliminada.
- ✅ **S1**: Grafo de dependências Gradle de `feature:pokemon-compare` adicionado (linhas 152–164).
- ✅ **S2**: `_state.update { it.copy(...) }` prescrito em todas as atualizações do ViewModel.
- ✅ **C1**: `navigator.goBack()` consistente com o método real do `Navigator` no recipe de referência.

---

### 🔴 Remaining Issues

**[R3] `scoped {}` incompatível com `koinViewModel` — ViewModel não será resolvido e seria singleton de atividade**

A seção Dependency Injection (linhas 178–202) declara `CompareViewModel` como `scoped` dentro de `activityRetainedScope`:

```kotlin
activityRetainedScope {
    navigation<PokemonCompareKey> { key ->
        val viewModel = koinViewModel<CompareViewModel>(
            parameters = { parametersOf(key.idA, key.idB) }
        )
        CompareScreen(viewModel = viewModel)
    }

    scoped { params ->
        CompareViewModel(idA = params.get(), idB = params.get(), getPokemonDetail = get())
    }
}
```

Há dois defeitos distintos que causariam falha em produção:

**1. `NoBeanDefFoundException` em runtime.** `koinViewModel<CompareViewModel>()` em Koin Compose resolve o ViewModel via o mecanismo padrão de `ViewModelProvider.Factory` do Koin, que exige uma declaração `viewModel { }` (DSL específica para ViewModels) no módulo Koin. Uma definição `scoped {}` dentro de `activityRetainedScope` não é visível para `koinViewModel` — ela é resolúvel apenas por `get<CompareViewModel>()` chamado de dentro do mesmo escopo Koin. Ao navegar para a tela de compare, `koinViewModel<CompareViewModel>` lançará `NoBeanDefFoundException: No definition found for 'CompareViewModel'`.

**2. Singleton de atividade mesmo que `scoped` fosse aceito.** Se o implementador, tentando corrigir o erro em runtime, substituir `scoped` por `viewModel` dentro de `activityRetainedScope`, o escopo continuará sendo da atividade inteira — haverá uma única instância de `CompareViewModel` para todo o ciclo de vida da `Activity`. Na segunda navegação para compare (par diferente de Pokémon), o ViewModel retornado será o mesmo da primeira navegação, com `idA`/`idB` originais. A tela exibirá o par anterior até que o intent `Load` seja re-disparado, criando flash de conteúdo errado e estado inconsistente entre o ViewModel e a key de navegação.

Por que causa retrabalho: o bug de `NoBeanDefFoundException` se manifesta apenas em runtime ao navegar para a tela. O implementador pode depurar por horas sem perceber que o problema é o tipo de declaração Koin. O padrão correto para ViewModel parameterizado em koin-compose-navigation3 é declarar o ViewModel com `viewModel {}` ou `factory {}` fora de `activityRetainedScope` e resolvê-lo com `koinViewModel` dentro do lambda `navigation<Key>`:

```kotlin
// feature:pokemon-compare — CompareKoinModule.kt
@OptIn(KoinExperimentalAPI::class)
val compareModule = module {
    // viewModel: nova instância por ViewModelStoreOwner (entrada do backstack)
    viewModel { params ->
        CompareViewModel(
            idA = params.get(),
            idB = params.get(),
            getPokemonDetail = get(),
        )
    }

    activityRetainedScope {
        navigation<PokemonCompareKey> { key ->
            val viewModel = koinViewModel<CompareViewModel>(
                parameters = { parametersOf(key.idA, key.idB) }
            )
            CompareScreen(viewModel = viewModel)
        }
    }
}
```

---

### 🟡 Should Fix

**[S3] Inconsistência de tipo: `GetPokemonDetailUseCase` declarado como `Flow<Result<Pokemon>>` mas tratado como `Flow<Pokemon>` no pseudo-código do ViewModel**

A linha 168 do design declara explicitamente:
> "Reutiliza `GetPokemonDetailUseCase(id: Int): Flow<Result<Pokemon>>`"

No entanto, o pseudo-código do ViewModel (linhas 107–113) faz:
```kotlin
val pokemon = getPokemonDetail(idA).first()  // coleta primeira emissão
val uiModel = mapper.map(pokemon)            // trata `pokemon` como Pokemon
```

Se a assinatura real for `Flow<Result<Pokemon>>`, então `.first()` retorna `Result<Pokemon>`, não `Pokemon`. Passar um `Result<Pokemon>` para `mapper.map(pokemon)` — que espera `Pokemon` — é um erro de tipo que falha em compilação. O `try/catch` ao redor não capturaria essa discrepância: um `Result.failure()` não lança exceção ao ser emitido e coletado via `.first()`.

O padrão de uso com `try/catch` + `errorHandler.handle(e)` é consistente com um use case que **emite `Pokemon` e lança exceção em caso de falha** (`Flow<Pokemon>`). Se esse for o contrato real do `GetPokemonDetailUseCase` em `core:domain`, a declaração na linha 168 deve ser corrigida de `Flow<Result<Pokemon>>` para `Flow<Pokemon>`. Alternativamente, se `Flow<Result<Pokemon>>` for o contrato correto, o pseudo-código precisa mostrar o unwrapping explícito antes de `mapper.map(...)`.

A inconsistência importa porque `CompareViewModel` e `CompareMapper` são implementados com base nesta seção do design — se o tipo declarado diverge do tipo real, ambos falharão em compilação ou emitirão resultados incorretos silenciosamente.

Ação: verificar o contrato real de `GetPokemonDetailUseCase` em `core:domain` e alinhar a declaração em "Domain / Data" com o pseudo-código do ViewModel — um dos dois deve ser corrigido para que sejam consistentes.

---

### 💡 Suggestion

Nenhuma sugestão adicional nesta rodada — todos os itens de Round 1 resolvidos.


---

## Review Round 3 — design.md

### 🟡 Should Fix

**[S4] Inconsistência entre a tabela de impacto e a seção Navigation sobre `pokemonCompareEntry`**

A linha 27 da tabela "Module & Layer Impact" descreve a modificação em `:app/AppNavDisplay.kt` como:
> "registra `pokemonCompareEntry`"

Mas a seção Navigation (linha 151) afirma explicitamente:
> "Nenhum `pokemonCompareEntry()` extension function separada é necessário — o Koin descobre o entry automaticamente via `navigation<PokemonCompareKey>` registrado no `compareModule`."

O implementador que ler a tabela primeiro tentará escrever ou localizar uma função `pokemonCompareEntry()` que não existe (e não deve existir). O termo "registra `pokemonCompareEntry`" na tabela implica código a ser escrito; a verdade é que nenhuma linha nova em `AppNavDisplay.kt` é necessária para registrar a entry — a modificação real, se houver, é garantir que `entryProvider = getEntryProvider()` já esteja em uso (como mostrado nas linhas 144–148).

Correção sugerida para a tabela: substituir "registra `pokemonCompareEntry`" por algo como "nenhuma mudança necessária — `getEntryProvider()` já descobre entradas via Koin" (ou remover a linha caso `AppNavDisplay.kt` ainda não exista, visto que Phase 4 está pendente).

---

### 💡 Suggestion

**Comentário do módulo Koin descreve o escopo incorretamente**

Linha 184:
```kotlin
// Koin cria uma instância nova a cada vez que parametersOf é chamado
```

Tecnicamente impreciso: Koin cria uma instância por `ViewModelStoreOwner` (cada entrada do backstack de Navigation3), não "a cada vez que `parametersOf` é chamado". Se o mesmo `ViewModelStoreOwner` invocar `koinViewModel` duas vezes na mesma composição, receberá a mesma instância. O comportamento relevante para o implementador é: uma instância por entrada do backstack. Sugestão de redação: "Koin cria uma instância por entrada do backstack (ViewModelStoreOwner); IDs passados via `parametersOf` na primeira resolução".

---

## Review Round 1 — adr

### 🟡 Should Fix

**[S1] `AsyncResult<T>` não tem shape definida no ADR — documento incompleto como registro permanente**

A Decisão 1 decide *onde* colocar `AsyncResult<T>` (`core:common`) mas nunca especifica *o que* é: quais casos sealed tem, e que tipo carrega o caso `Error`. O `design.md` mostra o uso (`AsyncResult.Loading`, `AsyncResult.Success(uiModel)`, `AsyncResult.Error(error)`), mas um leitor que consultar apenas este ADR em 6 meses não conseguirá inferir a assinatura nem verificar se a consequência "todos os módulos que dependem de `core:common` recompilam ao mudar a assinatura" se aplica.

Por que causa retrabalho: o implementador pode desenhar `AsyncResult.Error` carregando `Exception` em vez de `DomainError`, quebrando o padrão de `ErrorHandler` que é a justificativa central da Opção A. Sem a shape no ADR não há gate de revisão para esse desvio.

Correção sugerida: adicionar no corpo da Decisão 1, antes de "### Decision", um bloco de duas linhas com a interface acordada:
```kotlin
sealed interface AsyncResult<out T> {
    data object Loading : AsyncResult<Nothing>
    data class Success<T>(val data: T) : AsyncResult<T>
    data class Error(val error: DomainError) : AsyncResult<Nothing>
}
```

---

**[S2] `PokemonListKey` no código da Opção B não existe em nenhum artefato — nome inconsistente com a convenção do projeto**

O código ilustrativo da Opção B (linha ~106 do ADR) usa:
```kotlin
derivedStateOf { backStack.lastOrNull() is PokemonListKey }
```

`PokemonListKey` nunca é definido em nenhum outro artefato desta change. O `CLAUDE.md` define as rotas Navigation3 como `PokemonListRoute` e `PokemonDetailRoute` (sufixo `Route`), enquanto o `design.md` introduz `PokemonCompareKey` (sufixo `Key`). Há portanto dois sufixos concorrentes e a Opção B usa um terceiro nome fantasma.

Por que causa retrabalho: embora seja uma opção rejeitada, o ADR é um documento permanente. Um engenheiro que leia o ADR no futuro pode tentar localizar `PokemonListKey` no codebase, não encontrar, e incorretamente assumir que a lista ainda não tem NavKey — levando a implementação divergente. Além disso, o inconsistente `PokemonListKey` no exemplo torna o argumento arquitetural da Opção B mais difícil de avaliar (a "dependência circular" citada nos Cons pressupõe que a screen conhece sua própria chave de rota, o que precisa ser explicado ou exemplificado com o nome real).

Correção sugerida: substituir `PokemonListKey` pelo nome real definido em `core:navigation` (seja `PokemonListRoute` ou `PokemonListNavKey` — conforme o que for estabelecido ao criar `RouteKeys.kt`) e adicionar uma nota "(nome a confirmar quando `core:navigation` for criado)".

---

### 💡 Suggestion

**Mencionar a dependência Gradle necessária para `LifecycleResumeEffect`**

A Decisão 2 descreve `LifecycleResumeEffect` como "API simples e disponível no Compose lifecycle", mas não menciona que exige `androidx.lifecycle:lifecycle-runtime-compose` como dependência de `feature:pokemon-list`. Se o módulo não tiver essa dependência ainda, o implementador pode passar horas depurando um símbolo não resolvido. Uma linha nas Consequences ("Requer `lifecycle-runtime-compose` em `feature:pokemon-list`, se ainda não presente") tornaria o ADR autocontido.

---

## Review Round 1 — tasks.md

### 🔴 Remaining Issues

**[R1] Task 5.6 — `NavigateToCompare` event collection ausente na screen**

A task 5.6 descreve o que deve ser adicionado em `PokemonListScreen.kt`:
> "adicionar overlay de seleção, botão toggle de compare mode, coletar `ShowSelectionLimitReached` para snackbar, `LifecycleResumeEffect` para dispatchar `ResetCompareMode`"

Omite completamente: **coletar o evento `NavigateToCompare` e chamar `navigator.navigateTo(PokemonCompareKey(idA, idB))`**.

Por que é bloqueante: a task 5.5 cobre apenas a emissão do evento pelo ViewModel (`emitir NavigateToCompare`). A coleta do evento na screen e a chamada ao navigator são responsabilidade exclusiva de `PokemonListScreen.kt`. Um desenvolvedor que implemente exatamente o que está descrito na Task 5.6 produzirá uma screen funcional sob todos os aspectos — exceto que nenhum toque no segundo Pokémon jamais abrirá o compare. O bug seria silencioso: o estado muda corretamente, o evento é emitido, mas a navegação nunca ocorre. Detectar isso apenas no teste de integração/UI gera retrabalho garantido.

Evidência no spec (`pokemon-list-selection-mode/spec.md`):
> "the event handler in the screen calls `navigator.navigateTo(PokemonCompareKey(1, 4))`"

Correção necessária: adicionar à descrição de Task 5.6 — "coletar `NavigateToCompare(idA, idB)` e chamar `navigator.navigateTo(PokemonCompareKey(idA, idB))`".

---

### 🟡 Should Fix

**[S1] Task 4.8 (CompareViewModel) provavelmente > 2h — dividir em dois**

A tarefa engloba: criar o ViewModel com parâmetros `idA`/`idB`; implementar `load()` com dois `launch` independentes, coleta via `.first()`, `try/catch` + `ErrorHandler`, `_state.update {}` atômico; guard `idA == idB`; e `retry()` com cancelamento de Job por lado via `jobA`/`jobB` referências. São 5 conceitos distintos e a interação entre coroutines canceláveis exige testes mentais de race condition.

O CLAUDE.md impõe tasks <= 2h. Com um desenvolvedor experiente em coroutines isso é factível, mas está no limite superior. Recomendação: dividir em:
- 4.8a: Scaffolding do ViewModel + guard `idA == idB` + `load()` com happy-path paralelo (ambos os jobs, `.first()`, `_state.update`)
- 4.8b: Retry por lado (`jobA?.cancel()` + `loadSide()`), integração com `ErrorHandler`, tratamento de erros parciais

---

**[S2] Task 4.9 (CompareScreen) provavelmente > 2h — dividir em dois**

A tela tem três branches de renderização distintos:
1. `isInvalidInput = true` → mensagem de erro de tela inteira
2. Por lado — `AsyncResult.Loading` → indicador; `AsyncResult.Success` → artwork + nome + número + chips + altura/peso + 6× `CompareStatRow`; `AsyncResult.Error` → mensagem + botão retry
3. Layout `Column + Modifier.verticalScroll` envolvendo ambos os lados

O branch de sucesso por lado sozinho (artwork via Coil, formatação de número `#001`, chips de tipo, dois campos de metadata, seis linhas de stat com componente externo) é uma composição significativa. Recomendação: dividir em:
- 4.9a: Scaffolding da screen + branch `isInvalidInput` + estados `Loading` e `Error`+retry por lado
- 4.9b: Branch `Success` por lado — artwork, nome/número, chips de tipo, altura/peso, 6× `CompareStatRow`

---

### 💡 Suggestion

**Explicitar em Task 4.1 que `data:*` NÃO deve ser adicionado como dependência**

O `design.md` afirma: "`feature:pokemon-compare` NÃO depende de `feature:pokemon-list`, `feature:pokemon-detail`, `data:*` ou `core:observability`". Task 4.1 lista corretamente as dependências permitidas, mas não menciona a proibição de `data:*`. Um desenvolvedor que se pergunte "mas `GetPokemonDetailUseCase` não está em `core:domain` — sua implementação não exige `data:repository`?" pode ser tentado a adicionar a dependência. Uma nota "(NÃO adicionar `data:*`)" na descrição de 4.1 previne a dúvida sem custo.

---

**Adicionar `ui.preview` ao pacote criado em Task 4.3**

O CLAUDE.md lista `ui/preview/` como parte da estrutura canônica de feature modules. Task 4.3 cria os pacotes `ui.state, ui.intent, ui.screen, ui.model, ui.component, viewmodel, mapper, di` — omitindo `ui.preview`. Task 4.9 não menciona criar previews da `CompareScreen`. Incluir `ui.preview` em 4.3 e uma linha sobre `CompareScreenPreview.kt` em 4.9 mantém consistência com o padrão do projeto.

---

## Review Round 2 — tasks.md

### Validação dos fixes desta rodada

- **R1 ✅ Corrigido** — Task 5.6 agora inclui explicitamente "coletar `NavigateToCompare` event e chamar `navigator.navigateTo(PokemonCompareKey(idA, idB))`". O contrato da screen com o navigator está completo.
- **S1 ✅ Corrigido** — Task 4.8 dividida em 4.8a (scaffolding + happy-path paralelo) e 4.8b (retry + error handling). A divisão de responsabilidades é coerente e ambas as subtasks ficam dentro do limite de 2h.
- **S2 ✅ Corrigido** — Task 4.9 dividida em 4.9a (scaffolding + loading/error/invalidInput) e 4.9b (success layout por lado). Divisão adequada; 4.9a e 4.9b são independentemente entregáveis.

---

### 🟡 Should Fix

**[S1] Task 5.4 declara "5 casos" de `ToggleSelectForCompare` mas enumera apenas 4**

Task 5.4: "implementar todos os 5 casos de `ToggleSelectForCompare` (add, remove, limit-ignored, unicidade)".

O parêntese lista 4 itens. O caso ausente é o primeiro da tabela do `design.md`: `ToggleSelectForCompare(id)` | `!isCompareMode` → ignorado. Essa guarda do reducer protege o estado de seleção fora do modo compare — sem ela, taps acidentais fora do modo podem poluir `selectedForCompare`.

Task 7.1 espelha o problema: menciona "5 casos de `ToggleSelectForCompare`" sem listá-los. Um desenvolvedor escrevendo os testes seguindo apenas as tasks pode cobrir apenas os 4 casos nomeados e não escrever o cenário `isCompareMode = false → noop`, deixando o guard sem cobertura de teste.

Ação: em 5.4, adicionar `not-in-compare-mode` à lista parentética: `(not-in-compare-mode, add, remove, limit-ignored, unicidade)`. Replicar em 7.1.

---

**[S2] `CompareIntent.Load` definida em Task 4.5 mas nunca despachada — ambiguidade bloqueante de implementação**

Task 4.5 define `Load(val idA: Int, val idB: Int) : CompareIntent`. Task 4.8a descreve o ViewModel recebendo `idA`/`idB` como parâmetros de construtor via Koin `parametersOf` — o pseudo-código do `design.md` mostra `CompareViewModel.load(idA, idB)` chamado diretamente (sugerindo `init {}`). Task 4.9a não menciona nenhum `LaunchedEffect` despachando `Load`.

Resultado: há duas interpretações contraditórias para o mesmo código:
1. ViewModel carrega em `init {}` → `CompareIntent.Load` é código morto (task 4.5 cria um intent que nunca é despachado por ninguém).
2. Screen despacha `Load` via `LaunchedEffect(Unit)` → task 4.9a está incompleta (falta o bloco `LaunchedEffect`), e o ViewModel não deve carregar em `init`.

O desenvolvedor que implementar na ordem das tasks (4.5 → 4.8a → 4.9a) chegará ao 4.9a sem saber que precisa despachar `Load`, resultando em uma tela que fica eternamente em `AsyncResult.Loading`.

Ação: escolher explicitamente uma das duas abordagens e refletir nas tasks:
- Opção A (init-based): remover `Load` de `CompareIntent` em 4.5 (manter apenas `RetryPokemon`); clarificar em 4.8a que o `load(idA, idB)` é chamado no `init {}`.
- Opção B (intent-based): manter `Load`; adicionar em 4.9a um `LaunchedEffect(Unit) { onIntent(CompareIntent.Load(key.idA, key.idB)) }`, e clarificar em 4.8a que o ViewModel não carrega em `init` — aguarda o intent.

---

### 💡 Suggestion

**Task 4.8a/4.8b não menciona explicitamente o reset para `AsyncResult.Loading` antes de cada job**

O pseudo-código do `design.md` mostra `_state.update { it.copy(pokemonA = AsyncResult.Loading) }` como primeira instrução de cada job — antes do `try/catch`. Task 4.8a menciona atualizar para `Success` mas não para `Loading`. No caminho de retry (4.8b), a omissão é mais crítica: sem o reset para `Loading`, a tela permanece exibindo o estado de erro enquanto o retry está em progresso.

Ambas as tasks estão cobertas pelo `design.md`, mas mencioná-lo explicitamente em 4.8b ("reset do lado afetado para `AsyncResult.Loading` antes de relançar o job") elimina um esquecimento comum.

---

**`ui.preview` ainda ausente de Task 4.3 (remanescente da Round 1)**

Não endereçado na iteração atual. Task 4.3 continua sem `ui.preview` na lista de pacotes e Tasks 4.9a/4.9b não mencionam criar `CompareScreenPreview.kt`. Sugestão idêntica à Round 1: adicionar `ui.preview` em 4.3 e uma linha de preview em 4.9b.

---

## Review Round 3 — tasks.md

### Validação dos fixes desta rodada

- **S1 ✅ Corrigido** — Task 5.4 agora enumera explicitamente os 5 casos com labels (a)–(e), incluindo `(a) !isCompareMode → state inalterado (guard)`. Task 7.1 espelha a enumeração completa: `(a) !isCompareMode ignorado, (b) deselect, (c) add, (d) limit-ignored quando size==2, (e) unicidade por Set`. Ambas as tasks agora guiam o implementador sem ambiguidade.
- **S2 ✅ Corrigido** — Task 4.5 afirma explicitamente `Load` é removido e que o ViewModel usa `init {}`. Task 4.8a repete a nota "bloco `init {}` dispara carregamento imediatamente (sem `CompareIntent.Load`)". Ambiguidade eliminada; o caminho de implementação é unívoco.

---

### 🟡 Should Fix

**`CompareIntent.Load` ainda definida no MVI Contract do `design.md` — inconsistência cross-artefato**

O `design.md` (seção **MVI Contract › feature:pokemon-compare**, bloco kotlin) ainda descreve:

```kotlin
sealed interface CompareIntent {
    data class Load(val idA: Int, val idB: Int) : CompareIntent
    data class RetryPokemon(val side: Side) : CompareIntent
}
```

O `tasks.md` (4.5) é agora autoritativo e correto: `Load` é removido. Porém, ao implementar a Task 4.8a, o desenvolvedor naturalmente abre o `design.md` para consultar o pseudo-código do ViewModel — e encontrará `CompareIntent.Load` na definição da sealed interface logo acima. O risco concreto: o implementador pode adicionar `Load` de volta ao `CompareIntent.kt` seguindo o `design.md`, desfazendo o fix.

Ação: atualizar o bloco kotlin do `design.md` para refletir o contrato correto — apenas `RetryPokemon(side: Side)`.

---

### 💡 Suggestion

**Task 4.8b não menciona o reset para `AsyncResult.Loading` antes de relançar o job no retry**

O pseudo-código do `design.md` mostra `_state.update { it.copy(pokemonA = AsyncResult.Loading) }` como primeira instrução de cada job. Task 4.8b descreve "cancela o job do lado afetado e relança a carga" sem mencionar explicitamente esse reset. Sem o reset, a tela permanece exibindo o estado de erro enquanto o retry está em progresso. Adicionar "(primeiro reseta o lado afetado para `AsyncResult.Loading`)" na descrição de 4.8b elimina o esquecimento mais provável nesse path.

---

**`ui.preview` ausente de Task 4.3 — terceira ocorrência**

Remanescente das Rounds 1 e 2. Task 4.3 não inclui `ui.preview` na lista de pacotes; Tasks 4.9a/4.9b não mencionam `CompareScreenPreview.kt`. O padrão canônico do CLAUDE.md lista `ui/preview/` como parte obrigatória da estrutura de feature modules. Adicionar em 4.3 e uma linha de preview em 4.9b resolve a divergência com o restante do projeto.

