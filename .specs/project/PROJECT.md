# PokedexLab

**Vision:** Laboratório pessoal de arquitetura Android moderna — um app Pokédex (tema Pokémon) usado como bancada para experimentar e dominar padrões, bibliotecas e ferramentas de ponta do ecossistema.
**For:** O próprio desenvolvedor (uso pessoal, aprendizado).
**Solves:** Ter um espaço seguro e realista para estudar arquitetura limpa, modularização, MVI e libs recentes (Navigation3, Compose, Koin, ObjectBox, Paging3) sem o risco de um produto em produção. O tema Pokédex dá dados ricos e gratuitos (PokeAPI) e telas variadas para exercitar cada padrão.

## Goals

- **Cada feature exercita um padrão distinto.** Toda nova feature deve introduzir ou aprofundar pelo menos um conceito de arquitetura/lib (não repetir o já dominado). Métrica: ao concluir, conseguir explicar o "porquê" da decisão em 1 parágrafo.
- **Build sempre verde.** `./gradlew assembleDebug` compila e `./gradlew test` passa após cada feature. Métrica: nenhuma feature é dada como concluída com teste quebrado (hoje há 1 pendência — ver STATE.md).
- **Aprendizado documentado.** Decisões e lições ficam registradas (CLAUDE.md como fonte de verdade arquitetural + `.specs/`). Métrica: CLAUDE.md reflete o código real (validado no mapeamento de 2026-06-07).
- **Regras de modularização preservadas.** Features nunca se conhecem; comunicação só via `core:route:keys`. Métrica: zero arestas `feature:* → feature:*` no grafo Gradle.

## Tech Stack

Resumo — detalhes e versões exatas em `.specs/codebase/STACK.md` e `gradle/libs.versions.toml`.

**Core:**
- Build: Gradle 9.3.1 (Kotlin DSL) + AGP 9.1.1 + convention plugins
- Linguagem: Kotlin 2.2.10 (JVM 11)
- UI: Jetpack Compose (BOM 2026.02.01) + Material3
- Arquitetura: Clean Architecture + MVI, 16 módulos
- Banco local: ObjectBox 5.4.2

**Key dependencies:** Koin 4.2.1 (DI) · Retrofit 3.0 (rede) · Navigation3 1.1.1 (navegação) · Paging 3.3.6 (paginação) · Coil 3.4 (imagens) · JUnit 5 + MockK (testes)

## Scope

Por ser um **lab contínuo**, não há "v1 fechado". O escopo é definido por *o que se quer estudar*, não por um conjunto fixo de funcionalidades.

**Em escopo (o que o lab explora):**
- Padrões de arquitetura Android (Clean Arch, MVI, modularização por convention plugins)
- Bibliotecas modernas do AndroidX/Jetpack e do ecossistema Kotlin
- Telas client-side ricas alimentadas pela PokeAPI pública
- Estratégias de cache, paginação, navegação type-safe, deep links, testes

**Explicitamente fora de escopo:**
- Backend próprio / API autoral (consome só a PokeAPI pública)
- Autenticação / contas de usuário
- Publicação na Play Store / distribuição / monetização
- Suporte multiplataforma (KMP) — por enquanto Android-only

## Constraints

- **Recursos:** desenvolvedor solo; ritmo de estudo, não de entrega.
- **Técnicos:** versões bleeding-edge (AGP 9.x, Kotlin 2.2, Compose BOM 2026, betas de Navigation3/material3-adaptive) — incompatibilidades são esperadas e fazem parte do aprendizado. `compileSdk = 37` obrigatório pelo `material3-adaptive-navigation3:1.3.0-beta01`.
- **Filosofia:** favorecer **aprendizado e experimentação** sobre simplicidade. Uma solução mais complexa que ensina mais é preferível à mais simples — exceto quando a regra de modularização é violada (essa é inegociável).
- **Ambiente:** desenvolvimento em Windows (PowerShell).
