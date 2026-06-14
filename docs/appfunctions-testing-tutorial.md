# Tutorial: Testando AppFunctions no PokedexLab

## Contexto importante

A integração do AppFunctions com o **Gemini no celular** está em **prévia privada** (maio 2026) — não disponível publicamente ainda. Mas existem **três formas** de testar agora:

| Método | Dificuldade | O que testa |
|---|---|---|
| **ADB (verificação)** | Fácil | Registro e execução direta via linha de comando |
| **Gemini no Android Studio** | Médio | Simula um agente de IA chamando as funções |
| **AppFunctions Testing Agent** | Avançado | Agente real com LLM via API key do Gemini |

---

## Pré-requisito: Emulador Android 16

AppFunctions exigem Android 16 (API 36). Configure o emulador no Android Studio:

1. **Android Studio → Device Manager → `+` → Create Virtual Device**
2. Categoria **Phone** → qualquer modelo (ex: Pixel 8)
3. Na tela de System Image: aba **"Google Play"** → selecione **API 36 (Android 16)**
   - Precisa da coluna "Google Play" (ícone da Play Store) — essa imagem inclui Google Services
4. Nome: `Pixel8_API36` → **Finish**
5. Inicie o emulador

---

## Nota sobre quoting no ADB

O ADB no Windows tem um comportamento diferente do Linux ao repassar argumentos para o shell do Android. A regra para cada plataforma:

- **PowerShell (Windows):** envolva o comando inteiro em aspas simples. Aspas duplas dentro do JSON usam `\"`.
- **Bash/zsh (Linux/macOS):** use aspas simples ao redor do JSON. Aspas duplas dentro são preservadas normalmente.

---

## Método 1 — ADB (mais rápido para verificar)

### Passo 1: Instalar o app

```bash
./gradlew :app:installDebug
```

### Passo 2: Verificar se as funções foram registradas

```bash
adb shell dumpsys app_function
```

Procure pelo bloco `br.com.pokedex`. Você deve ver as duas funções:

```
AppFunctionDocument(s) for package: br.com.pokedex

  AppFunctionMetadata for: br.com.pokedex.appfunctions.PokemonAppFunctions#listPokemons
    ...
  AppFunctionMetadata for: br.com.pokedex.appfunctions.PokemonAppFunctions#openPokemonDetail
    ...
```

> Se o bloco `br.com.pokedex` não aparecer logo após instalar, aguarde 10–30 segundos e repita — o sistema indexa as funções de forma assíncrona.

> `adb shell cmd app_function list-app-functions` pode retornar `No shell command implementation` em alguns builds de emulador. Use `dumpsys app_function` nesses casos — o resultado é equivalente.

### Passo 3: Listar os Pokémons

**PowerShell (Windows):**
```powershell
adb shell 'cmd app_function execute-app-function --package br.com.pokedex --function "br.com.pokedex.appfunctions.PokemonAppFunctions#listPokemons" --parameters "{}"'
```

**Bash/zsh (Linux/macOS):**
```bash
adb shell cmd app_function execute-app-function \
  --package br.com.pokedex \
  --function "br.com.pokedex.appfunctions.PokemonAppFunctions#listPokemons" \
  --parameters '{}'
```

Você verá uma lista JSON com `id` e `name` dos 50 primeiros Pokémons.

### Passo 4: Invocar openPokemonDetail

Copie o `id` retornado (ex: `1` = Bulbasaur) e execute:

**PowerShell (Windows):**
```powershell
adb shell 'cmd app_function execute-app-function --package br.com.pokedex --function "br.com.pokedex.appfunctions.PokemonAppFunctions#openPokemonDetail" --parameters "{\"pokemonId\":1}"'
```

**Bash/zsh (Linux/macOS):**
```bash
adb shell cmd app_function execute-app-function \
  --package br.com.pokedex \
  --function "br.com.pokedex.appfunctions.PokemonAppFunctions#openPokemonDetail" \
  --parameters '{"pokemonId": 1}'
```

A resposta será `{}` — isso é **esperado**. A função retornou um `PendingIntent`, mas o `cmd app_function` não sabe dispará-lo (só retorna o resultado serializado). Quem dispara o `PendingIntent` é o agente de IA real.

### Passo 5: Testar a navegação pelo deeplink

Para confirmar que a navegação funciona, dispare o deeplink diretamente — isso simula o que o agente de IA faz ao receber o `PendingIntent`:

**PowerShell (Windows):**
```powershell
adb shell 'am start -a android.intent.action.VIEW -d "pokedex://pokemon/1"'
```

**Bash/zsh (Linux/macOS):**
```bash
adb shell am start -a android.intent.action.VIEW -d "pokedex://pokemon/1"
```

O emulador deve abrir o PokedexLab **direto na tela de detalhe** do Bulbasaur. Troque `1` pelo `id` de qualquer Pokémon.

> **Resumo do fluxo completo via ADB:**
> 1. `listPokemons` → retorna lista JSON → você escolhe um `id`
> 2. `openPokemonDetail` → retorna `{}` (PendingIntent) → `cmd` não dispara, normal
> 3. `am start -d "pokedex://pokemon/ID"` → abre o app na tela certa ✅
>
> Com um agente real (AppFunctions Testing Agent ou Gemini), o passo 3 é automático.

### Por que o quoting é diferente entre plataformas

| Plataforma | O que você escreve | O que o shell Android recebe |
|---|---|---|
| PowerShell | `'...{\"pokemonId\":1}...'` (string literal + `\"`) | `{"pokemonId":1}` ✅ |
| PowerShell (errado) | `` `--parameters '{"pokemonId": 1}' `` (multi-linha) | `{pokemonId: 1}` ❌ aspas perdidas |
| Bash | `'{"pokemonId": 1}'` (aspas simples) | `{"pokemonId": 1}` ✅ |

---

## Método 2 — Gemini no Android Studio (simula agente de IA)

Essa é a abordagem descrita na documentação oficial para simular como um agente de IA usaria as funções.

### Passo 1: Abrir o Gemini no Android Studio

- Menu **View → Tool Windows → Gemini** (ou ícone do Gemini na barra lateral)
- Faça login com conta Google se necessário

### Passo 2: Garantir que o app está instalado e rodando

```bash
./gradlew :app:installDebug
```

Lance o app no emulador.

### Passo 3: Cole este prompt no chat do Gemini

```
Execute `adb shell dumpsys app_function` to learn about the AppFunctions
available in br.com.pokedex, then act as a chat agent trying to help the
user browse Pokémons. Use the AppFunction descriptions as your instructions.
Start by listing the available Pokémons for the user to choose.
```

O Gemini vai:
1. Executar o ADB para descobrir as funções disponíveis
2. Chamar `listPokemons` e mostrar a lista
3. Aguardar você escolher um Pokémon
4. Chamar `openPokemonDetail` com o ID escolhido

### Passo 4: Interagir como usuário

Responda no chat do Gemini, por exemplo: **"Quero ver o Charmander"** — e observe o emulador abrindo a tela de detalhe.

> **Nota:** O Gemini no Android Studio precisa ter permissão para executar comandos ADB. Se ele perguntar sobre permissões, confirme.

---

## Método 3 — AppFunctions Testing Agent (agente real com Gemini API)

Esse método usa o app oficial de testes do Google com uma API Key do Gemini.

### Pré-requisito: API Key do Gemini

1. Acesse [aistudio.google.com](https://aistudio.google.com)
2. Clique em **"Get API key"** → **"Create API key"**
3. Copie a chave (formato: `AIza...`)

### Passo 1: Clonar o repositório do agente

**Windows (Git Bash) ou Linux/macOS:**
```bash
git clone https://github.com/android/appfunctions.git
cd appfunctions/agent
```

### Passo 2: Compilar e instalar o agente

Abra o projeto `appfunctions/agent` no Android Studio como um projeto separado, depois execute no terminal:

**Windows (Git Bash ou WSL):**
```bash
./run_privileged.sh --build --flavor retail --api-key SUA_API_KEY_AQUI
```

**Linux/macOS:**
```bash
./run_privileged.sh --build --flavor retail --api-key SUA_API_KEY_AQUI
```

> No Windows, o script requer **Git Bash** (incluso com Git for Windows) ou **WSL**. Não funciona no PowerShell ou CMD nativos.

### Passo 3: Usar o agente

O agente instala no emulador com permissões privilegiadas. Abra o app **"AppFunctions Tester"** no emulador:

1. Escolha o pacote `br.com.pokedex`
2. Aba **"LLM Agent"** → o Gemini vai descobrir as funções automaticamente
3. Digite no campo de chat: **"Me mostre os Pokémons disponíveis"**
4. O agente chama `listPokemons`, exibe a lista e aguarda sua escolha
5. Diga qual Pokémon quer ver → ele chama `openPokemonDetail` e abre o app

---

## IDs das funções implementadas

| Função | ID completo |
|---|---|
| Listar Pokémons | `br.com.pokedex.appfunctions.PokemonAppFunctions#listPokemons` |
| Abrir detalhe | `br.com.pokedex.appfunctions.PokemonAppFunctions#openPokemonDetail` |

---

## Solução de problemas

| Problema | Causa | Solução |
|---|---|---|
| `No shell command implementation` | Build de emulador sem interface shell do `app_function` | Use `dumpsys app_function` em vez de `cmd app_function` |
| `End of input at character N` | Quoting errado no PowerShell | Use aspas simples envolvendo o comando inteiro + `\"` no JSON |
| Funções não aparecem no `dumpsys` | AppSearch ainda indexando | Aguarde 30s após instalar e repita |
| `openPokemonDetail` retorna `{}` e não abre o app | Comportamento esperado — `cmd` não dispara PendingIntents | Use `adb shell am start -d "pokedex://pokemon/ID"` para testar a navegação |
| Deeplink `am start` não abre o app | App não instalado ou deeplink não registrado | Reinstale o app com `./gradlew :app:installDebug` |
| App não aparece no Testing Agent | `app_functions_v2.xml` ausente do APK | Verifique se o `srcDir` do KSP está configurado no `build.gradle.kts` |
| Gemini no AS não executa ADB | Sem permissão | Confirme as permissões no prompt do Android Studio |
