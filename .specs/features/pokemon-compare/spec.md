# Pokemon Compare Specification

## Problem Statement

Hoje o app mostra um Pokémon por vez (tela de detalhe). Não há como confrontar os atributos de dois Pokémon diretamente — o usuário precisa memorizar valores ou alternar entre telas. Esta feature permite **selecionar 2 Pokémon na lista e comparar seus tipos e Base Stats lado a lado**, com destaque para o maior valor de cada status.

(Contexto/lab: também serve para exercitar a criação de uma *feature module* nova de ponta a ponta sobre a arquitetura existente — ver `.specs/project/PROJECT.md`.)

## Goals

- [ ] Usuário compara 2 Pokémon lado a lado a partir da lista, em ≤ 3 toques (Comparar → card A → card B).
- [ ] Reuso máximo do que já existe: zero mudança em `core:domain`/`data:*`; reaproveitar `PokemonStatBar`, `PokemonTypeChip`, `pokemonTypeColor`.
- [ ] Nova `feature:pokemon-compare` segue 100% as convenções (MVI, convention plugin, navegação por `NavKey`, regra de modularização).

## Out of Scope

| Feature | Motivo |
|---|---|
| Comparar 3+ Pokémon | Decisão C-1: exatamente 2 nesta versão |
| Seção "About" (peso/altura/abilities) | Decisão C-3: foco em atributos de combate |
| Description / flavor text | Decisão C-3 |
| Total geral de stats / vencedor global | Decisão C-4: só realce por linha nesta versão |
| Trocar um Pokémon dentro da tela de comparação | Adiado (candidato a P3) |
| Salvar / compartilhar comparação | Não é objetivo agora |
| Busca/filtro para escolher Pokémon | Seleção é feita na lista existente |

---

## User Stories

### P1: Comparar dois Pokémon lado a lado ⭐ MVP

**User Story**: Como usuário, quero selecionar 2 Pokémon na lista e ver seus tipos e Base Stats lado a lado, para comparar diretamente qual é mais forte em cada atributo.

**Why P1**: É a fatia vertical completa da feature — sem ela não há nada a demonstrar.

**Acceptance Criteria**:

1. WHEN o usuário toca no botão "Comparar" no header da lista THEN o sistema SHALL entrar em **modo seleção** (o toque no card passa a marcar em vez de abrir o detalhe).
2. WHEN o usuário toca em um card no modo seleção THEN o sistema SHALL marcá-lo visualmente e atualizar o contador de seleção (ex.: "1/2").
3. WHEN o usuário toca novamente em um card já marcado THEN o sistema SHALL desmarcá-lo e decrementar o contador.
4. WHEN o usuário marca o **2º** Pokémon THEN o sistema SHALL navegar para a tela de comparação com os dois ids selecionados.
5. WHEN a tela de comparação abre THEN o sistema SHALL exibir, para **cada** Pokémon em 2 colunas: artwork oficial, nome, #número e chips de tipo.
6. WHEN a tela de comparação carrega os dados THEN o sistema SHALL exibir os **Base Stats** de ambos lado a lado, alinhados por status (mesma ordem/labels).
7. WHEN a tela de comparação é exibida THEN o sistema SHALL **NÃO** exibir About (peso/altura/abilities) nem description.
8. WHEN os dados ainda estão carregando THEN o sistema SHALL exibir estado `loading`.
9. WHEN ambos os Pokémon carregam com sucesso THEN o sistema SHALL exibir estado `success`.

**Independent Test**: Abrir a lista, tocar "Comparar", tocar em Bulbasaur e Charmander → a tela de comparação abre mostrando os dois com tipos e Base Stats lado a lado, sem About/description.

---

### P1: Sair do modo seleção ⭐ MVP

**User Story**: Como usuário, quero cancelar o modo de comparação, para voltar a navegar normalmente abrindo detalhes.

**Why P1**: Sem saída, o modo seleção sequestra o toque da lista — inutiliza a navegação normal.

**Acceptance Criteria**:

1. WHEN o usuário está no modo seleção e toca em "Cancelar" (ou usa o back) THEN o sistema SHALL sair do modo e limpar a seleção.
2. WHEN o modo seleção está desativado e o usuário toca em um card THEN o sistema SHALL abrir a tela de **detalhe** (comportamento atual preservado).

**Independent Test**: Ativar "Comparar", marcar 1 card, tocar "Cancelar" → seleção some, tocar num card abre o detalhe normalmente.

---

### P1: Erro/estado vazio na comparação ⭐ MVP

**User Story**: Como usuário, quero feedback claro quando a comparação não pode ser carregada, para poder tentar de novo.

**Why P1**: Regra do projeto: toda feature trata `loading/success/error/empty` (CLAUDE.md → Padrão MVI).

**Acceptance Criteria**:

1. WHEN o carregamento de **qualquer um** dos 2 Pokémon falha THEN o sistema SHALL exibir estado `error` com ação de **retry**.
2. WHEN o usuário aciona o retry THEN o sistema SHALL recarregar **ambos** os Pokémon.
3. WHEN não há rede THEN o sistema SHALL exibir o `DomainError.Network` mapeado (via `ErrorHandler`), nunca uma exceção crua.

**Independent Test**: Com rede desligada, abrir comparação → estado de erro com botão de retry; religar rede e dar retry → carrega.

---

### P2: Realçar o maior valor por status

**User Story**: Como usuário, quero ver destacado quem tem o maior valor em cada status, para identificar o vencedor de cada linha sem ler os números.

**Why P2**: A comparação já é demonstrável sem o realce; é um aprimoramento de leitura (mas faz parte do comportamento desejado — decisão C-4).

**Acceptance Criteria**:

1. WHEN os dois valores de um status diferem THEN o sistema SHALL destacar visualmente o **maior**.
2. WHEN os dois valores de um status são iguais THEN o sistema SHALL destacar **ambos** (empate).

**Independent Test**: Comparar dois Pokémon com stats diferentes → em cada linha, o maior valor aparece destacado; numa linha de empate, ambos destacados.

---

### P3: Trocar um Pokémon na tela de comparação

**User Story**: Como usuário, quero trocar um dos Pokémon sem voltar à lista, para comparar rapidamente várias combinações.

**Why P3**: Conveniência; a feature é completa sem isso.

**Acceptance Criteria**:

1. WHEN o usuário aciona "trocar" em uma coluna THEN o sistema SHALL permitir escolher outro Pokémon e recarregar a comparação.

---

## Edge Cases

- WHEN o usuário tenta marcar um 3º card (já com 2 marcados) THEN o sistema SHALL ignorar (ou exigir desmarcar um) — máximo 2.
- WHEN o usuário marca e desmarca até zerar THEN o sistema SHALL permanecer no modo seleção com contador "0/2".
- WHEN os dois ids selecionados são o mesmo Pokémon THEN o sistema SHALL impedir (não é possível marcar o mesmo card como 1º e 2º).
- WHEN um Pokémon carrega e o outro falha THEN o sistema SHALL tratar como `error` da tela inteira (não exibir comparação parcial).
- WHEN o usuário gira a tela / processo é recriado durante a seleção THEN o sistema SHALL preservar a seleção e o estado da comparação (chave de navegação serializável).

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
|---|---|---|---|
| COMPARE-01 | P1: Comparar (entrar no modo seleção) | Design | Pending |
| COMPARE-02 | P1: Comparar (marcar/desmarcar + contador) | Design | Pending |
| COMPARE-03 | P1: Comparar (navegar ao marcar o 2º) | Design | Pending |
| COMPARE-04 | P1: Comparar (artwork + nome + # + tipos, 2 colunas) | Design | Pending |
| COMPARE-05 | P1: Comparar (Base Stats lado a lado) | Design | Pending |
| COMPARE-06 | P1: Comparar (sem About/description) | Design | Pending |
| COMPARE-07 | P1: Comparar (estados loading/success) | Design | Pending |
| COMPARE-08 | P1: Sair do modo seleção (cancelar/back) | Design | Pending |
| COMPARE-09 | P1: Sair do modo (preservar toque→detalhe) | Design | Pending |
| COMPARE-10 | P1: Erro/empty (error + retry recarrega ambos) | Design | Pending |
| COMPARE-11 | P1: Erro (DomainError via ErrorHandler) | Design | Pending |
| COMPARE-12 | P2: Realçar maior valor por status | Design | Pending |
| COMPARE-13 | P2: Realçar empate (ambos) | Design | Pending |
| COMPARE-14 | P3: Trocar Pokémon na comparação | - | Pending |

**ID format:** `COMPARE-[NUMBER]`
**Status values:** Pending → In Design → In Tasks → Implementing → Verified
**Coverage:** 14 total, 0 mapeados a tasks (Design/Tasks pendentes), 1 fora do MVP (COMPARE-14, P3) ⚠️

---

## Success Criteria

- [ ] Usuário completa "comparar 2 Pokémon" em ≤ 3 toques a partir da lista.
- [ ] Tela de comparação mostra tipos + Base Stats dos 2 lado a lado, sem About/description, com o maior valor de cada status destacado.
- [ ] Modo seleção é reversível e não quebra o toque-único→detalhe existente.
- [ ] Erro de rede vira `error` com retry funcional (zero crash / zero exceção crua).
- [ ] Zero alteração em `core:domain`/`data:*`; `./gradlew assembleDebug test` verde.
- [ ] Regra de modularização intacta: `feature:pokemon-compare` não depende de outras features.
