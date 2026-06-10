# Integration Test Instructions

Integração aqui = interações entre lista → navegação → comparação → repositório. Compose UI tests não foram incluídos neste escopo (Q11=A); cenários abaixo são manuais/instrumentados (executar no estágio Operations ou ad-hoc).

## Cenário 1 — Lista → Comparação (fluxo feliz)
- **Setup**: app instalado, rede disponível.
- **Passos**: abrir lista → tocar "Compare" (header) → selecionar 2 Pokémon → tocar "Compare (2)".
- **Esperado**: navega para a tela de comparação; 2 colunas lado a lado com imagem, types, About, descrição e Base Stats; maior valor por stat destacado (verde); empate sem destaque. Ao voltar, a lista está sem seleção e fora do modo (Q4=A).

## Cenário 2 — Limite de seleção
- **Passos**: no modo seleção, com 2 já marcados, tocar um 3º.
- **Esperado**: seleção inalterada + snackbar "You can only compare 2 Pokémon".

## Cenário 3 — Falha parcial por coluna (Q2=B)
- **Setup**: forçar erro em um id (ex.: desligar rede após cachear só um; ou id inexistente via deep link).
- **Esperado**: a coluna que falhou mostra erro + retry; a outra renderiza normal; destaque de vencedor só aparece quando ambas carregam. Retry recarrega apenas o lado afetado.

## Cenário 4 — Deep link (SECURITY-05)
- **Passos**:
```bash
adb shell am start -a android.intent.action.VIEW -d "pokedex://compare/1/4"
adb shell am start -a android.intent.action.VIEW -d "pokedex://compare/abc/4"   # inválido
```
- **Esperado**: o primeiro abre a comparação (#1 vs #4) com a lista no backstack; o segundo não resolve (ids inválidos rejeitados).

## Recomendação
Adicionar um teste unitário de `DeepLinkRouter.fromUrl` (módulo `core:route:deeplink` hoje sem testes) para cobrir `pokedex://compare/...` válido e inválido — reforça a verificação de SECURITY-05.
