# Defesa — Pessoa 3: Motor da Partida (Quiz)

Escopo da parte 3, conforme o card do grupo:

> `JogoUiState`, `JogoViewModel` e `JogoViewModelFactory` controlam a categoria escolhida (lida dinamicamente do banco), dificuldade, rodada atual, vidas, pontos, sorteio de 1 resposta certa + 3 erradas da mesma categoria (usando `.filter`, `.shuffled`, `.map`), exibição de dica e salvamento no `PlacarRepository` ao fim da partida. `InicioJogoScreen` escolhe categoria/dificuldade e inicia; `JogoQuizScreen` mostra imagem (Coil), vidas, placar, dica, 4 alternativas e o resumo de Fim de Jogo.

**Frase para abrir a defesa:**
"Toda a regra do jogo fica no `JogoViewModel`. As telas só desenham o `JogoUiState` e avisam o ViewModel quando o jogador toca em algo. O ViewModel recebe os repositórios pela `JogoViewModelFactory`, então ele não sabe nada de Room nem de Retrofit diretamente."

---

## 1. Mapa dos arquivos

| Arquivo | Camada | Responsabilidade em uma frase |
|---|---|---|
| `viewmodel/JogoUiState.kt` | Estado | Foto de tudo que a tela precisa mostrar (apelido, categorias, pergunta, alternativas, vidas, pontos, rodada, flags de controle). |
| `viewmodel/JogoViewModel.kt` | Lógica | Regras do jogo: sorteio, resposta, pontos, vidas, dica, avanço automático, fim de partida e gravação do placar. |
| `viewmodel/JogoViewModelFactory.kt` | Injeção | Cria o `JogoViewModel` passando `QuizRepository` e `PlacarRepository`. |
| `ui/jogo/InicioJogoScreen.kt` | Tela | "Bora descobrir?": apelido, cards de categoria, chips de nível e botão Iniciar Partida. |
| `ui/jogo/JogoQuizScreen.kt` | Tela | Rodada em andamento: topo com rodada/vidas/pontos, imagem, dica, alternativas A–D e feedback. |
| `ui/jogo/FimDeJogoConteudo.kt` | Tela | Resumo final: pontuação, acertos, rodadas, vidas, "Jogar novamente" e "Voltar ao início". |
| `ui/jogo/ComponentesJogo.kt` | Componentes | Peças visuais reaproveitadas: botão pílula, chip, campo de texto, card de categoria, card de alternativa, corações, barra de progresso. |
| `ui/jogo/CoresJogo.kt` | Visual | Paleta de cores tirada do Figma (azul-marinho, ciano, verde de acerto, vermelho de erro, âmbar de dica). |

Nenhum arquivo fora da parte 3 precisa ser alterado. Como a tela de entrada do app (Pessoa 5) agora é só dois botões, sem apelido, o apelido é pedido **só aqui**, no campo "Seu apelido".

---

## 2. Fluxo completo (o que acontece quando o jogador usa)

```
InicioJogoScreen                 JogoViewModel                         Room / Repositories
────────────────                 ─────────────                         ───────────────────
(abre a tela)  ───────────────►  init { observarCategorias() }  ◄────  quizRepository.buscarTodos(): Flow
                                 monta List<CategoriaResumo>
◄──── uiState.categorias ─────
toca "Bandeiras" / "Médio" ───►  selecionarCategoria / selecionarDificuldade
toca "Iniciar Partida" ───────►  iniciarPartida()
                                   filter (categoria + nível) → shuffled → take(10)
                                   criarAlternativas(1 certa + 3 erradas)
                                   navegarParaQuiz = true
LaunchedEffect vê a flag ─────►  navegacaoRealizada()  → navega para JogoQuizScreen

JogoQuizScreen
toca alternativa ─────────────►  responder()
                                   acertou? +pontos : -1 vida
                                   espera 1,5 s (delay) → avancarPergunta()
                                   sem vidas OU fim das rodadas → finalizarPartida()
                                                                   ───►  placarRepository.inserir(PlacarEntity)
◄──── partidaFinalizada = true ─
mostra FimDeJogoConteudo
```

---

## 3. `JogoUiState.kt` — o estado da tela

**O que é:** uma `data class` imutável. Cada mudança cria uma cópia nova com `copy(...)`, e o Compose redesenha só o que mudou.

```kotlin
data class CategoriaResumo(
    val nome: String,
    val quantidadeItens: Int,
    val imagemCapaUrl: String?
)
```
Resumo de cada categoria para os cards da tela inicial ("Bandeiras · 250 países para explorar", com a primeira bandeira como capa).

Grupos de campos do `JogoUiState`:
- **Tela inicial:** `apelido`, `categorias`, `categoriaSelecionada`, `dificuldadeSelecionada`, `sincronizando`.
- **Partida:** `perguntas`, `indicePerguntaAtual`, `perguntaAtual`, `alternativas`, `vidasRestantes`, `pontuacao`, `acertos`, `perguntasRespondidas`.
- **Resposta da rodada:** `respostaSelecionada`, `respostaCorreta` (null = ainda não respondeu), `pontosUltimaResposta`, `dicaVisivel`.
- **Controle:** `carregando`, `partidaIniciada`, `partidaFinalizada`, `navegarParaQuiz`, `mensagemErro`.

**Propriedades calculadas** (não são guardadas, são derivadas dos outros campos):
```kotlin
val totalRodadas: Int get() = perguntas.size
val rodadaAtual: Int get() = indicePerguntaAtual + 1
val progresso: Float get() = if (totalRodadas == 0) 0f else perguntasRespondidas.toFloat() / totalRodadas
val respondida: Boolean get() = respostaCorreta != null
val podeIniciar: Boolean get() = apelido.isNotBlank() && categoriaSelecionada.isNotBlank() && !carregando
```
*Por que calcular em vez de guardar?* Para que nunca fiquem dessincronizados. Por exemplo, `rodadaAtual` nunca pode divergir de `indicePerguntaAtual`.

---

## 4. `JogoViewModel.kt` — o motor do jogo

### 4.1 Constantes (as regras em um lugar só)
```kotlin
companion object {
    const val TOTAL_RODADAS = 10
    const val VIDAS_INICIAIS = 3
    const val ALTERNATIVAS_POR_PERGUNTA = 4
    const val TEMPO_FEEDBACK_MS = 1500L
    val DIFICULDADES = listOf("Fácil", "Médio", "Difícil")

    fun pontosPorAcerto(dificuldade: String): Int = when (dificuldade) {
        "Médio" -> 150
        "Difícil" -> 200
        else -> 100
    }
}
```
Se o professor perguntar "e se quiser 5 vidas?", basta trocar uma constante. A tela também lê essas constantes (o banner "10 rodadas · 3 vidas"), então texto e regra nunca divergem.

### 4.2 StateFlow: como a tela recebe o estado
```kotlin
private val _uiState = MutableStateFlow(JogoUiState())
val uiState: StateFlow<JogoUiState> = _uiState.asStateFlow()
```
- `_uiState` (privado e mutável): só o ViewModel altera.
- `uiState` (público e somente leitura): a tela observa com `collectAsState()`.
- Alteração sempre com `_uiState.update { it.copy(...) }`, que é atômico e seguro entre corrotinas.

### 4.3 Categorias dinâmicas a partir do banco
```kotlin
private fun observarCategorias() {
    viewModelScope.launch {
        quizRepository.buscarTodos().collect { itens ->
            todosItens = itens
            val categorias = itens
                .groupBy { it.categoria }
                .map { (nome, itensDaCategoria) ->
                    CategoriaResumo(nome, itensDaCategoria.size, itensDaCategoria.firstOrNull()?.imagemUrl)
                }
                .sortedBy { it.nome }
            _uiState.update { ... categorias = categorias ... }
        }
    }
}
```
- `buscarTodos()` devolve um **`Flow` do Room**. Toda vez que a tabela muda (sincronização da API ou CRUD do Admin), o `collect` roda de novo e os cards se atualizam sozinhos.
- Nenhuma categoria está fixa no código: se o Admin cadastrar "Monumentos", aparece um card novo automaticamente.
- `viewModelScope` cancela a coleta quando o ViewModel é destruído, então não há vazamento de memória.

### 4.4 Iniciar partida: filtro, embaralhamento e limite de rodadas
```kotlin
val itensDaCategoria = itens.filter { it.categoria == categoria }

val perguntas = itensDaCategoria
    .filter { it.dificuldade.equals(dificuldade, ignoreCase = true) }
    .distinctBy { it.id }
    .shuffled()
    .take(TOTAL_RODADAS)
```
- `filter`: só a categoria e o nível escolhidos.
- `distinctBy { it.id }` + `shuffled()`: **sem repetir pergunta** e em ordem aleatória.
- `take(10)`: no máximo 10 rodadas. Se houver menos itens, a partida tem menos rodadas.
- Validação: precisa de pelo menos 1 pergunta e 4 itens na categoria (para montar 4 alternativas). Senão aparece "Ainda não há itens suficientes em Bandeiras · Difícil."
- Tudo dentro de `try/catch`: erro no banco vira `mensagemErro` na tela, sem fechar o app.

### 4.5 Sorteio das alternativas (1 certa + 3 erradas)
```kotlin
private fun criarAlternativas(correta: ItemQuizEntity, itensDaCategoria: List<ItemQuizEntity>): List<ItemQuizEntity> {
    val erradas = itensDaCategoria
        .filter { it.id != correta.id && it.nomeCorreto != correta.nomeCorreto }
        .distinctBy { it.nomeCorreto }
        .shuffled()
        .take(ALTERNATIVAS_POR_PERGUNTA - 1)

    return (erradas + correta).shuffled()
}
```
- As erradas vêm **da mesma categoria** (bandeira só concorre com bandeira).
- Removo a própria resposta e nomes repetidos, então nunca aparecem duas opções iguais.
- `shuffled()` no final para a certa não ficar sempre na mesma posição.
- As erradas podem ser de qualquer nível, o que deixa as opções mais variadas.

### 4.6 Responder: pontos, vidas e avanço automático
```kotlin
fun responder(alternativa: ItemQuizEntity) {
    val estado = _uiState.value
    val pergunta = estado.perguntaAtual ?: return
    if (!estado.partidaIniciada || estado.respondida) return   // trava toque duplo

    val acertou = alternativa.id == pergunta.id
    val pontos = if (acertou) pontosPorAcerto(estado.dificuldadeSelecionada) else 0

    _uiState.update {
        it.copy(
            respostaSelecionada = alternativa,
            respostaCorreta = acertou,
            pontosUltimaResposta = pontos,
            pontuacao = it.pontuacao + pontos,
            acertos = if (acertou) it.acertos + 1 else it.acertos,
            vidasRestantes = if (acertou) it.vidasRestantes else it.vidasRestantes - 1,
            perguntasRespondidas = it.perguntasRespondidas + 1
        )
    }

    avancoAutomatico?.cancel()
    avancoAutomatico = viewModelScope.launch {
        delay(TEMPO_FEEDBACK_MS)
        avancarPergunta()
    }
}
```
- Comparação por `id`, não por texto: mais seguro.
- A trava `estado.respondida` impede somar pontos duas vezes se o jogador tocar rápido.
- O avanço é automático: mostra verde/vermelho por 1,5 s e vai para a próxima. `delay` **não trava a tela**, porque é uma corrotina suspensa.
- Guardo o `Job` em `avancoAutomatico` para **cancelar** se o jogador sair no meio do feedback.

### 4.7 Avançar ou terminar
```kotlin
if (estado.vidasRestantes <= 0 || proximoIndice >= estado.perguntas.size) {
    finalizarPartida()
    return
}
```
A partida termina por **vidas zeradas** ou **fim das rodadas**. Caso contrário, sorteia novas alternativas para a próxima pergunta e limpa resposta e dica.

### 4.8 Salvar o placar no fim
```kotlin
private fun finalizarPartida() {
    if (resultadoSalvo) return
    resultadoSalvo = true
    viewModelScope.launch {
        val placar = PlacarEntity(
            nomeJogador = estado.apelido.ifBlank { "Jogador" },
            categoriaJogada = estado.categoriaSelecionada,
            pontuacao = estado.pontuacao,
            acertos = estado.acertos,
            dificuldade = estado.dificuldadeSelecionada,
            dataHora = System.currentTimeMillis()
        )
        val resultado = placarRepository.inserir(placar)
        _uiState.update { it.copy(partidaIniciada = false, partidaFinalizada = true, mensagemErro = if (resultado.isFailure) "Não foi possível salvar o resultado." else null) }
    }
}
```
- `resultadoSalvo` garante **uma gravação por partida**.
- `placarRepository.inserir` devolve `Result` (try/catch feito pela Pessoa 2). Se falhar, mostro a mensagem sem quebrar o app.
- Como o Ranking observa o `Flow` da tabela `placares`, o resultado aparece lá **na hora**.

### 4.9 Dica
```kotlin
fun mostrarDica() {
    if (!estado.partidaIniciada || estado.respondida) return
    _uiState.update { it.copy(dicaVisivel = true) }
}
```
A dica vem do campo `dica` do item ("Capital: Tóquio"), que o Repository monta a partir da API. Ela some ao trocar de rodada (`dicaVisivel = false` em `avancarPergunta`).

### 4.10 Navegação de evento único
```kotlin
// ViewModel
navegarParaQuiz = navegar           // dentro de iniciarPartida()
fun navegacaoRealizada() { _uiState.update { it.copy(navegarParaQuiz = false) } }
```
**Bug que existia antes:** a tela navegava quando `partidaIniciada == true`. Se o jogador apertava "voltar" no meio da partida, ao chegar na tela inicial a flag ainda era `true` e ele era jogado de volta para o quiz. Agora a navegação é um **evento consumido**: a tela navega e logo avisa `navegacaoRealizada()`.

### 4.11 Sair e jogar novamente
- `sairDaPartida()`: cancela o avanço automático e limpa a partida.
- `jogarNovamente()`: `sairDaPartida()` + `iniciarPartida(navegar = false)`. Usa o mesmo apelido, categoria e nível, sem navegar, porque já está na tela do quiz.

### 4.12 Atalho "Baixar bandeiras"
```kotlin
fun sincronizarBandeiras() {
    viewModelScope.launch {
        _uiState.update { it.copy(sincronizando = true) }
        val resultado = quizRepository.sincronizarBandeirasDaApi()
        _uiState.update { it.copy(sincronizando = false, mensagemErro = if (resultado.isFailure) "Não foi possível baixar..." else null) }
    }
}
```
Se o banco estiver vazio, a tela inicial mostra o botão "Baixar bandeiras", que chama a **mesma função do Repository** que o painel Admin usa. Assim o jogo é testável mesmo sem o Admin pronto. Quando a sincronização grava no Room, o `Flow` do item 4.3 dispara e o card "Bandeiras" aparece sozinho.

---

## 5. `JogoViewModelFactory.kt` — injeção de dependências

```kotlin
class JogoViewModelFactory(
    private val quizRepository: QuizRepository,
    private val placarRepository: PlacarRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JogoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JogoViewModel(quizRepository, placarRepository) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
}
```
**Por que existe:** o Android cria ViewModels com construtor vazio. Como o `JogoViewModel` precisa dos repositórios, a Factory ensina como montá-lo. No `AppNavigation`:
```kotlin
val jogoViewModel: JogoViewModel = viewModel(factory = JogoViewModelFactory(quizRepository, placarRepository))
```
Vantagem: o ViewModel sobrevive à rotação de tela e pode ser testado com repositórios falsos.

---

## 6. `InicioJogoScreen.kt` — tela "Bora descobrir?"

| Parte da tela | Código | Explicação |
|---|---|---|
| Ler estado | `val uiState by viewModel.uiState.collectAsState()` | Converte o `StateFlow` em estado do Compose: mudou, redesenha. |
| Navegar | `LaunchedEffect(uiState.navegarParaQuiz) { if (...) { viewModel.navegacaoRealizada(); onIniciarJogo() } }` | Evento único (ver 4.10). |
| Banner | `"${JogoViewModel.TOTAL_RODADAS} rodadas · ${JogoViewModel.VIDAS_INICIAIS} vidas"` | O texto vem das constantes da regra. |
| Apelido | `CampoTextoJogo(valor = uiState.apelido, onValorChange = viewModel::alterarApelido, ...)` | O valor fica no ViewModel e não se perde ao girar a tela. |
| Grade de categorias | `val itensGrade = uiState.categorias + null` → `chunked(2)` → `Row` com `weight(1f)` | Grade de 2 colunas. O `null` no fim representa o card "Em Breve". |
| Banco vazio | `if (uiState.categorias.isEmpty()) BancoVazio(...)` | Mostra o botão "Baixar bandeiras" (ver 4.12). |
| Nível | `JogoViewModel.DIFICULDADES.forEach { ChipOpcao(...) }` | Chips Fácil/Médio/Difícil. O selecionado fica com borda ciano e ✓. |
| Botão | `BotaoPilula(habilitado = uiState.podeIniciar, carregando = uiState.carregando)` | Desabilitado sem apelido ou categoria. Mostra spinner enquanto sorteia. |

**Regra de ouro:** a tela não tem `if` de regra de jogo. Ela só chama funções do ViewModel (`selecionarCategoria`, `selecionarDificuldade`, `iniciarPartida`).

---

## 7. `JogoQuizScreen.kt` — tela da partida

### 7.1 Escolha do conteúdo
```kotlin
when {
    uiState.partidaFinalizada -> FimDeJogoConteudo(...)
    pergunta != null          -> RodadaConteudo(...)
    else                      -> CircularProgressIndicator(...)
}
```
Uma tela só, com 3 estados. É o estado que decide o que aparece.

### 7.2 Voltar
```kotlin
val sair = { viewModel.sairDaPartida(); onVoltarInicio() }
BackHandler(onBack = sair)
```
Tanto a seta ← quanto o botão "voltar" do Android encerram a partida direito: cancelam o timer e limpam o estado.

### 7.3 Elementos da rodada
| Elemento (Figma) | Código |
|---|---|
| "Rodada 4/10" | `"Rodada ${uiState.rodadaAtual}/${uiState.totalRodadas}"` |
| ❤️❤️♡ vidas | `IndicadorVidas(vidasRestantes, VIDAS_INICIAIS)`: `repeat` desenha um coração cheio ou vazado para cada vida |
| Barra de progresso | `BarraProgressoRodadas(uiState.progresso)` |
| "⚡ 350 pts" | `"${uiState.pontuacao} pts"` |
| "BANDEIRAS · FÁCIL" | `"${categoria} · ${dificuldade}".uppercase()` |
| Pergunta | `textoPergunta(categoria)`: "De qual país é esta bandeira?" para Bandeiras, texto genérico para outras categorias |
| Imagem | `AsyncImage(model = pergunta.imagemUrl)` do **Coil**: baixa a imagem pela URL salva no Room, em segundo plano, com cache |
| "💡 Ver Dica" | `BotaoVerDica(habilitado = !uiState.respondida)` → vira a caixa âmbar com `pergunta.dica` |
| A / B / C / D | `forEachIndexed { indice, alt -> CardAlternativa(letra = 'A' + indice, ...) }` (`'A' + 1 = 'B'`) |
| "Boa! Resposta correta. +100 pts" | `when (uiState.respostaCorreta) { true -> ...; false -> "Era França. Você perdeu uma vida."; null -> nada }` |

### 7.4 Regra das cores das alternativas
```kotlin
private fun estadoDaAlternativa(uiState, pergunta, alternativa) = when {
    !uiState.respondida                                -> EstadoAlternativa.NEUTRA
    alternativa.id == pergunta.id                      -> EstadoAlternativa.CORRETA     // verde ✓
    alternativa.id == uiState.respostaSelecionada?.id  -> EstadoAlternativa.ERRADA      // vermelha ✗
    else                                               -> EstadoAlternativa.DESATIVADA  // apagada
}
```
Se o jogador errou, ele vê a escolha dele em vermelho **e** a certa em verde, então aprende a resposta. Depois de responder, `clickable(enabled = estado == NEUTRA)` bloqueia novos toques.

---

## 8. `FimDeJogoConteudo.kt` — resumo final
- Título: "Fim de jogo!" se as vidas acabaram, "Partida concluída!" se jogou as 10 rodadas.
- Caixa grande com a pontuação + "Bandeiras · Fácil".
- Três estatísticas: Acertos, Rodadas (`4/10`) e Vidas.
- "Jogar novamente" → `viewModel.jogarNovamente()`; "Voltar ao início" → `sairDaPartida()` + volta.
- Quando essa tela aparece, o placar **já está salvo** e visível no Ranking.

---

## 9. `ComponentesJogo.kt` e `CoresJogo.kt` — visual do Figma
| Componente | Onde aparece | Detalhe técnico |
|---|---|---|
| `BotaoPilula` | Iniciar Partida, Baixar bandeiras, Jogar novamente | `shape = CircleShape` deixa o botão em pílula; `primario = false` gera a versão escura com borda; mostra spinner quando `carregando`. |
| `ChipOpcao` | Nível | Fundo, borda e ✓ mudam com `selecionado`. |
| `CampoTextoJogo` | Apelido | `BasicTextField` com `decorationBox` para ter ícone + placeholder no estilo do Figma. |
| `CardCategoria` / `CardEmBreve` | Categorias | Capa com `AsyncImage`, texto "N países para explorar", ✓ quando selecionado. |
| `CardAlternativa` + `enum EstadoAlternativa` | Quiz | Cor do fundo/borda por estado, usando `Triple(fundo, borda, corTexto)`. |
| `IndicadorVidas` | Quiz | `Icons.Filled.Favorite` / `Icons.Outlined.FavoriteBorder`. |
| `BarraProgressoRodadas` | Quiz | `LinearProgressIndicator(progress = { progresso })`. |
| `CaixaDestaque` | Banner, imagem, dica, resumo | Caixa arredondada com fundo e borda configuráveis. |

`CoresJogo` é um `object` com as cores do Figma (`Fundo`, `Superficie`, `Primaria`, `Acerto`, `Erro`, `Dica`...) e `corDificuldade()` (verde/âmbar/vermelho). Ficou dentro de `ui/jogo/` para a parte 3 não alterar arquivos da Pessoa 5.

---

## 10. O que mudou em relação à versão anterior (caso perguntem)

| Antes | Agora | Motivo |
|---|---|---|
| Jogava **todos** os itens do nível (podiam ser dezenas de rodadas) | `take(10)` → 10 rodadas | Regra do Figma ("10 rodadas · 3 vidas") |
| 10 / 20 / 30 pts por acerto | 100 / 150 / 200 | Figma mostra "+100 pts" no Fácil |
| Alternativas erradas só do mesmo nível | Erradas de qualquer nível da categoria | Mais variedade; funciona mesmo com poucos itens no nível |
| Botão "Próxima" | Avanço automático após 1,5 s (`delay` + `Job`) | Figma não tem botão Próxima |
| Feedback em texto ("França - Correto!") | Card verde/vermelho + mensagem colorida | Figma |
| Dropdowns de categoria e nível | Cards de categoria + chips | Figma |
| Pedia "Nome do jogador" aqui **e** no login | Apelido pedido só na tela "Bora descobrir?" (a entrada virou só dois botões) | Não perguntar a mesma coisa duas vezes |
| Navegação por `partidaIniciada` (voltar reabria o quiz) | Evento `navegarParaQuiz` + `navegacaoRealizada()` | Correção de bug |
| Voltar do Android não encerrava a partida | `BackHandler` | Correção de bug |
| Banco vazio = jogo travado | Botão "Baixar bandeiras" | Jogo testável sem o Admin |
| Sem resumo visual no fim | `FimDeJogoConteudo` | Card pedia "resumo de Fim de Jogo" |

---

## 11. Perguntas prováveis do professor

**Por que a lógica está no ViewModel e não na tela?**
Separação de responsabilidades (MVVM). A tela só desenha e repassa eventos. O ViewModel sobrevive à rotação, então a partida não reinicia ao girar o celular, e pode ser testado sem interface.

**O que é StateFlow e por que não LiveData?**
É um fluxo observável que sempre tem um valor atual, nativo de Kotlin/corrotinas. No Compose, `collectAsState()` transforma em estado e redesenha automaticamente.

**Como garante que as alternativas não se repetem?**
`filter` remove a resposta certa e nomes iguais, `distinctBy { it.nomeCorreto }` remove duplicados e `take(3)` pega só 3. Depois soma a certa e `shuffled()`.

**Como garante que a mesma pergunta não volta?**
A lista de perguntas é sorteada uma vez no início (`distinctBy { it.id }.shuffled().take(10)`) e percorrida por índice, então cada item aparece uma vez só.

**E se o jogador tocar duas vezes rápido?**
`responder()` retorna cedo se `respondida == true`, e o card fica com `clickable(enabled = false)`.

**O `delay(1500)` não trava o app?**
Não. `delay` suspende a corrotina sem bloquear a thread principal. E o `Job` é cancelado se o jogador sair.

**Como o Ranking sabe da nova pontuação?**
`finalizarPartida()` insere via `PlacarRepository`. O `RankingViewModel` coleta o `Flow` da tabela `placares`, e o Room emite a lista nova automaticamente.

**Para que serve a Factory?**
O `ViewModelProvider` só sabe criar ViewModel sem parâmetros. A Factory passa os repositórios para o construtor (injeção de dependência manual).

**Como o jogo suporta outras categorias?**
As categorias vêm do banco por `groupBy { it.categoria }`. Um item novo com `categoria = "Monumentos"` gera um card novo e a pergunta muda para "Qual é o nome desta imagem?".

---

## 12. Observações para o grupo
- A dica de capital vem da API em inglês ("Capital: Berlin"). Isso é da Pessoa 2 (`QuizRepository`); dá para trocar por um mapa de tradução se quiserem.
- **Commits:** a defesa é individual e o professor olha os commits. Quem é a Pessoa 3 deve fazer o commit destas mudanças pela própria conta e conseguir explicar cada seção acima.
