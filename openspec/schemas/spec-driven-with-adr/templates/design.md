# Design: <!-- nome da mudanca -->

## Approach
<!-- Visao geral da solucao tecnica em 1 paragrafo. -->

## Module & Layer Impact
<!-- Tabela: modulo Gradle -> arquivos novos/alterados -> camada (ui/viewmodel/...) -->

## MVI Contract (por feature afetada)
- State: <!-- campos novos -->
- Intent: <!-- intents novos -->
- Event: <!-- eventos efemeros (navegacao) -->
- Reducer: <!-- transicoes puras (State, Intent) -> State -->

## Navigation
<!-- NavKey novo em core:route:keys, entry provider, wiring no AppNavDisplay -->

## Domain / Data
<!-- UseCase novo? Reuso de repositorio/cache? Sem mudanca de DTO se possivel. -->

## Dependency Injection (Koin)
<!-- Modulo Koin novo e onde e carregado (:app startKoin). -->

## Testing Strategy
<!-- Reducer (JUnit5+MockK), UseCase, UI test do compare. -->

## Open Questions
<!-- Pontos que viram ADR. -->