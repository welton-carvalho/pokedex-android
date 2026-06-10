# 📘 Apostila Hands-On — Feature de Comparação de Pokémon (PokedexLab)

Esta apostila ensina, **do zero e na prática**, como construir a feature de **comparação de Pokémon** no PokedexLab — assumindo que ela ainda **não existe**. Você vai criar um módulo Android novo (`feature:pokemon-compare`) seguindo a arquitetura do projeto (Clean Architecture + MVI), e ao final terá uma tela que coloca 2 Pokémon lado a lado destacando o maior valor por stat.

A apostila também mostra como **estender o processo AI-DLC** (com um exemplo de *extension* que valida o uso do Design System), apresenta uma **ferramenta de apoio** concreta, e esclarece como organizar os arquivos do AI-DLC quando você tem **mais de uma feature**.

## Para quem é
- Devs Android (Kotlin/Compose) que querem aprender a arquitetura modular do PokedexLab construindo algo real.
- Quem quer entender como o fluxo **AI-DLC** se aplica a features novas e a alterações.

## Pré-requisitos
- Android Studio recente + JDK 17, Android SDK com `compileSdk 37`.
- Familiaridade básica com Kotlin, Coroutines/Flow e Jetpack Compose.
- Ter lido (ou ter por perto) o `docs/GUIDE.md` — é a **fonte de verdade** de arquitetura do projeto.

## Como usar
Siga os capítulos em ordem. No Capítulo 2 (hands-on) há checkpoints **"⏸️ Pare e teste"** — rode os comandos antes de avançar. Todo código é copiável e foi validado contra a base real.

## Índice

| Cap. | Arquivo | Conteúdo |
|---|---|---|
| 1 | [01-setup-e-fundamentos.md](01-setup-e-fundamentos.md) | Stack, estrutura de módulos, convenções (MVI, isolamento de features, convention plugins), como rodar |
| 2 | [02-hands-on-construindo-a-feature.md](02-hands-on-construindo-a-feature.md) | **Hands-on**: construir `feature:pokemon-compare` passo a passo + seleção na lista + rota/deep link + testes |
| 3 | [03-extensions-validador-design-system.md](03-extensions-validador-design-system.md) | **Extension AI-DLC**: criar um validador que verifica se a tela usa o Design System (opt-in + regras + enforcement) |
| 4 | [04-ferramentas-de-apoio.md](04-ferramentas-de-apoio.md) | **Ferramenta de apoio**: case do *Feature Scaffolder* + testes de arquitetura (Konsist) como guardrails |
| 5 | [05-aidlc-multiplas-features.md](05-aidlc-multiplas-features.md) | **A dúvida**: o que é compartilhado vs. específico por feature; como iniciar uma nova feature; como tratar alteração na mesma feature |

## Mapa mental do que vamos construir (Cap. 2)

```text
feature:pokemon-list (modificado)            feature:pokemon-compare (NOVO)
  modo de seleção (2 Pokémon)                  Screen (2 colunas lado a lado)
        │  Comparar (2)                         ViewModel (carga paralela, retry/coluna)
        ▼                                        Reducer + StatComparator (maior por stat)
  core:route:keys  ──PokemonCompareKey──▶  core:route:navigation (registra a entry)
        ▲                                              │
  core:route:deeplink (pokedex://compare/{a}/{b})      ▼
                                              GetPokemonDetailUseCase (REUSO)
```

> Esta apostila acompanha os artefatos gerados pelo fluxo AI-DLC em `aidlc-docs/` (requirements, application-design, plans, build-and-test). Sempre que possível, ela aponta o artefato de origem de cada decisão.
