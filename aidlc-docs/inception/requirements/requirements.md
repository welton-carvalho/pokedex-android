# Requirements — Feature: Comparação de Pokémon

## Intent Analysis
- **User Request**: Criar uma feature de comparação de Pokémon. Na tela de lista, selecionar 2 Pokémon e comparar seus stats. A nova tela coloca os dois lado a lado com as mesmas informações da tela de detalhe.
- **Request Type**: New Feature
- **Scope Estimate**: Multiple Components — novo módulo `feature:pokemon-compare` (MVI) + mudanças de seleção em `feature:pokemon-list` + nova `NavKey` em `core:route:keys` + nav entry montada em `core:route:navigation` + deep link em `core:route:deeplink`
- **Complexity Estimate**: Moderate

## Source
Derivado de `requirement-verification-questions.md` (respostas do usuário) + contexto de engenharia reversa (`architecture.md`, `component-inventory.md`, `technology-stack.md`).

---

## Functional Requirements

### FR-1 — Modo de seleção na lista (Q1=B, Q3=B)
- A tela de lista (`feature:pokemon-list`) exibe uma ação **"Comparar"** no header que **ativa/desativa o modo de seleção**.
- Com o modo de seleção **ativo**, tocar em um card **marca/desmarca** o Pokémon (não navega para o detalhe).
- Com o modo de seleção **inativo**, o toque mantém o comportamento atual (abre o detalhe).
- Cards selecionados têm indicação visual clara (ex.: borda/checkmark/overlay).

### FR-2 — Quantidade exata de 2 (Q2=A, Q4=A)
- A comparação exige **exatamente 2** Pokémon.
- Ao tentar selecionar um **3º** Pokémon com 2 já selecionados, a seleção é **bloqueada** e um **aviso** é exibido (ex.: snackbar "Você só pode comparar 2 Pokémon"). A seleção existente é preservada.

### FR-3 — Disparo da comparação (Q3=B)
- Enquanto o modo seleção está ativo, é exibida uma ação **"Comparar (n)"** (botão/FAB) refletindo a contagem selecionada.
- A ação fica **habilitada somente quando há 2 selecionados**; ao tocá-la, navega para a tela de comparação passando os 2 ids.

### FR-4 — Tela de comparação lado a lado (Q5=A, Q7=A)
- Nova tela com **duas colunas fixas lado a lado** (Pokémon A | Pokémon B), com **rolagem vertical** da tela.
- Cada coluna exibe **todas as informações do detalhe**: **imagem (artwork)**, número (#), nome, **type chips**, **About** (weight, height, abilities), **descrição** e **Base Stats**.
- Reaproveitar os componentes visuais existentes onde possível (`PokemonTypeChip`, `PokemonStatBar`, blocos do `PokemonDetailScreen`).

### FR-5 — Destaque do vencedor por stat (Q6=A)
- Para **cada base stat**, destacar visualmente o **maior valor** entre os 2 Pokémon (ex.: cor/realce no número e/ou barra).
- **Empates** são indicados (ambos destacados ou marcador neutro).
- (Sem soma/total geral — escopo do Q6=A, não B.)

### FR-6 — Origem dos dados (Q9=A)
- A tela de comparação obtém o detalhe completo de cada Pokémon **reutilizando `PokemonRepository.getPokemonDetail(id)`** (cache strategy `NETWORK_FIRST`, com fallback offline existente).
- Carrega os **2 detalhes** (idealmente em paralelo) e agrega em um estado de comparação.

### FR-7 — Estados de UI (padrão MVI do projeto)
- A tela de comparação implementa os estados obrigatórios: **loading**, **success**, **error**, **empty**.
- **loading**: enquanto qualquer um dos 2 detalhes está carregando.
- **error**: se a carga de um (ou ambos) falhar, com ação **retry**.
- Fluxo MVI completo: `State / Intent / Reducer (puro) / Event`.

### FR-8 — Deep link (Q10=A)
- Adicionar deep link no `DeepLinkRouter`: `pokedex://compare/{id1}/{id2}` → abre a tela de comparação diretamente (backstack sintético com a lista por baixo, seguindo o padrão atual do router).
- IDs inválidos/ausentes → o deep link não resolve (retorna null), conforme padrão atual.

### FR-9 — Navegação de saída (Q8=A)
- Para trocar os Pokémon comparados, o usuário **volta para a lista** e refaz a seleção (sem troca in-place na tela de comparação).
- Botão de voltar padrão retorna à lista; ao retornar, o modo seleção pode ser limpo.

---

## Non-Functional Requirements

### NFR-1 — Arquitetura e convenções
- Seguir Clean Architecture + MVI e todas as regras do `docs/GUIDE.md`.
- Novo módulo `feature:pokemon-compare` segue a estrutura padrão de feature (`ui/{state,intent,reducer,event,model,screen,component}`, `viewmodel`, `mapper`, `navigation`, `di`).
- **Regra absoluta**: a nova feature não conhece outras features; usa apenas `core:route:keys`. A nav entry é registrada em `core:route:navigation`.
- DI via Koin (`compareModule` registrado no `:app`).

### NFR-2 — Performance Compose
- Modelos de UI `@Immutable`/`@Stable`; `remember`/`derivedStateOf` para o cálculo de "maior por stat".
- Imagens via Coil com cache memória+disco.

### NFR-3 — Testes (Q11=A)
- Cobrir: **reducer da comparação**, qualquer **use case novo**, e **mappers** (Domain → UiModel de comparação, incluindo a lógica de destaque por stat).
- JUnit 5 + MockK + `TestDispatcherProvider`/fakes de `core:testing`.
- (UI tests Compose não exigidos neste escopo — Q11=A, não B.)

### NFR-4 — Internacionalização/nomenclatura
- Código, packages e classes em inglês (`br.com.pokedex.feature.pokemoncompare...`). Strings de UI conforme padrão atual do projeto.

---

## Extension Decisions & Compliance

| Extensão | Habilitada | Decidido em |
|---|---|---|
| Security Baseline | Yes (Q12=A) | Requirements Analysis |
| Resiliency Baseline | Yes (Q13=A) | Requirements Analysis |
| Property-Based Testing | No (Q14=C) | Requirements Analysis |

### Aplicabilidade (contexto: app cliente Android, sem backend/cloud/IaC, dados públicos read-only)

**Security — regras aplicáveis a esta feature:**
- **SECURITY-05 (Input validation)**: o deep link `pokedex://compare/{id1}/{id2}` DEVE validar os ids (`toIntOrNull`, descartar negativos/zero/fora de faixa) antes de criar a `NavKey`. **Aplicável — requisito de implementação.**
- **SECURITY-03 (App logging)**: logs via `AppLogger`/Timber, sem PII/segredos (não há dados sensíveis aqui). **Compliant.**
- **SECURITY-15 (Exception handling / fail-safe)**: as 2 cargas usam `Result`/`DomainError`; falha → estado de erro com retry, nunca crash. **Aplicável — coberto por FR-7.**
- **SECURITY-10 (Supply chain)**: dependências fixadas via version catalog (`libs.versions.toml`). Sem novas libs externas previstas. **Compliant.**
- **SECURITY-09 (Hardening / erros genéricos)**: mensagens de erro ao usuário são genéricas. **Compliant.**

**Security — N/A (sem componente servidor/cloud):** SECURITY-01 (encryption-at-rest gerenciada — ObjectBox local com dados públicos não-sensíveis), SECURITY-02 (LB/gateway logging), SECURITY-04 (HTTP headers web), SECURITY-06 (IAM), SECURITY-07 (rede/firewall), SECURITY-08 (auth/IDOR server-side), SECURITY-11 (rate limiting/abuse server), SECURITY-12 (autenticação — app não tem login), SECURITY-13 (SRI/CI integrity — sem páginas web/CDN), SECURITY-14 (alerting central server-side).

**Resiliency — regras aplicáveis (em espírito, no cliente):**
- **RESILIENCY-10 (Timeouts / graceful degradation)**: chamadas externas com timeout (Retrofit/OkHttp); degradação graciosa via fallback de cache offline já existente; comparação tolera erro com retry. **Aplicável — coberto por FR-6/FR-7.**
- **RESILIENCY-06 (Health/erro percebido pelo usuário)**: estados loading/error explícitos. **Aplicável — coberto por FR-7.**

**Resiliency — N/A (workload mobile cliente, sem deploy cloud, sem estado servidor persistente):** RESILIENCY-01/02 (criticidade/RTO/RPO/DR de workload cloud), RESILIENCY-03/04 (change mgmt/CI-CD/rollback/deploy style de produção cloud), RESILIENCY-05/07 (observabilidade central/resiliency posture cloud), RESILIENCY-08/09 (multi-zona/multi-região/auto-scaling), RESILIENCY-11/12/13 (DR strategy/backup/replicação/failover), RESILIENCY-14/15 (chaos/DR testing e incident response de produção cloud).

> **Nota sobre as perguntas obrigatórias de Resiliency (RTO/RPO, topologia regional, rollback, etc.):** essas decisões pressupõem um workload de servidor/cloud que **não existe** neste projeto (app Android distribuído via APK/loja). Por isso foram avaliadas como **N/A com justificativa** em vez de apresentadas como questionário. Se você quiser que eu trate a app como parte de um sistema maior com backend e levante essas decisões de DR formalmente, escolha **Request Changes**.

---

## Key Requirements Summary
- Nova feature **`feature:pokemon-compare`** (MVI) + modo de seleção na lista (header toggle "Comparar", exatamente 2, 3º bloqueado, botão "Comparar (2)").
- Tela com **2 colunas lado a lado**, scroll vertical, **todas as infos do detalhe** (incl. imagem) e **destaque do maior valor por stat** (com empate).
- **Reúso** de `getPokemonDetail` + componentes de design existentes; estados loading/success/error/empty.
- **Deep link** `pokedex://compare/{id1}/{id2}` com validação de ids (SECURITY-05).
- Testes: reducer + use case(s) + mappers.
