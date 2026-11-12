package com.example.qualimagem.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qualimagem.data.ItemQuizEntity
import com.example.qualimagem.data.PlacarEntity
import com.example.qualimagem.data.repository.PlacarRepository
import com.example.qualimagem.data.repository.QuizRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Motor da partida. Toda a regra do jogo fica aqui (e não na tela):
 * sorteio das perguntas e alternativas, vidas, pontos, dica,
 * avanço de rodada e gravação do placar no fim.
 */
class JogoViewModel(
    private val quizRepository: QuizRepository,
    private val placarRepository: PlacarRepository
) : ViewModel() {

    companion object {
        const val TOTAL_RODADAS = 10
        const val VIDAS_INICIAIS = 3
        const val ALTERNATIVAS_POR_PERGUNTA = 4
        const val TEMPO_FEEDBACK_MS = 1500L

        val DIFICULDADES = listOf("Fácil", "Médio", "Difícil")

        /** Pontos ganhos por acerto em cada nível. */
        fun pontosPorAcerto(dificuldade: String): Int = when (dificuldade) {
            "Médio" -> 150
            "Difícil" -> 200
            else -> 100
        }
    }

    private val _uiState = MutableStateFlow(JogoUiState())
    val uiState: StateFlow<JogoUiState> = _uiState.asStateFlow()

    /** Todos os itens do banco, sempre atualizados pelo Flow do Room. */
    private var todosItens: List<ItemQuizEntity> = emptyList()

    /** Job do avanço automático, para poder cancelar se o jogador sair. */
    private var avancoAutomatico: Job? = null

    private var resultadoSalvo = false

    init {
        observarCategorias()
    }

    // ------------------------------------------------------------------
    // Tela de início
    // ------------------------------------------------------------------

    /**
     * Coleta o Flow do Room: sempre que um item é inserido/removido
     * (pela sincronização ou pelo Admin), os cards de categoria se atualizam sozinhos.
     */
    private fun observarCategorias() {
        viewModelScope.launch {
            quizRepository.buscarTodos().collect { itens ->
                todosItens = itens

                val categorias = itens
                    .groupBy { it.categoria }
                    .map { (nome, itensDaCategoria) ->
                        CategoriaResumo(
                            nome = nome,
                            quantidadeItens = itensDaCategoria.size,
                            imagemCapaUrl = itensDaCategoria.firstOrNull()?.imagemUrl
                        )
                    }
                    .sortedBy { it.nome }

                _uiState.update { estado ->
                    val selecionadaAindaExiste = categorias.any { it.nome == estado.categoriaSelecionada }
                    estado.copy(
                        categorias = categorias,
                        categoriaSelecionada = if (selecionadaAindaExiste) {
                            estado.categoriaSelecionada
                        } else {
                            categorias.firstOrNull()?.nome.orEmpty()
                        }
                    )
                }
            }
        }
    }

    fun alterarApelido(apelido: String) {
        _uiState.update { it.copy(apelido = apelido, mensagemErro = null) }
    }

    fun selecionarCategoria(categoria: String) {
        _uiState.update { it.copy(categoriaSelecionada = categoria, mensagemErro = null) }
    }

    fun selecionarDificuldade(dificuldade: String) {
        _uiState.update { it.copy(dificuldadeSelecionada = dificuldade, mensagemErro = null) }
    }

    /**
     * Atalho para quando o banco está vazio: baixa as bandeiras pela API
     * (mesma função do Repository que o painel Admin usa).
     */
    fun sincronizarBandeiras() {
        if (_uiState.value.sincronizando) return

        viewModelScope.launch {
            _uiState.update { it.copy(sincronizando = true, mensagemErro = null) }

            val resultado = quizRepository.sincronizarBandeirasDaApi()

            _uiState.update {
                it.copy(
                    sincronizando = false,
                    mensagemErro = if (resultado.isFailure) {
                        "Não foi possível baixar as bandeiras. Verifique a internet."
                    } else {
                        null
                    }
                )
            }
        }
    }

    // ------------------------------------------------------------------
    // Início da partida
    // ------------------------------------------------------------------

    /**
     * @param navegar true quando chamado pela tela inicial (precisa abrir o quiz);
     * false no "Jogar novamente", que já está na tela do quiz.
     */
    fun iniciarPartida(navegar: Boolean = true) {
        val estado = _uiState.value
        val apelido = estado.apelido.trim()
        val categoria = estado.categoriaSelecionada
        val dificuldade = estado.dificuldadeSelecionada

        if (apelido.isBlank()) {
            _uiState.update { it.copy(mensagemErro = "Digite seu apelido para jogar.") }
            return
        }
        if (categoria.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, mensagemErro = null) }

            try {
                // Garante a lista mais recente do banco antes de sortear
                val itens = quizRepository.buscarTodos().first()
                todosItens = itens

                val itensDaCategoria = itens.filter { it.categoria == categoria }

                // Perguntas: só do nível escolhido, embaralhadas e sem repetição
                val perguntas = itensDaCategoria
                    .filter { it.dificuldade.equals(dificuldade, ignoreCase = true) }
                    .distinctBy { it.id }
                    .shuffled()
                    .take(TOTAL_RODADAS)

                if (perguntas.isEmpty() || itensDaCategoria.size < ALTERNATIVAS_POR_PERGUNTA) {
                    _uiState.update {
                        it.copy(
                            carregando = false,
                            mensagemErro = "Ainda não há itens suficientes em $categoria · $dificuldade."
                        )
                    }
                    return@launch
                }

                resultadoSalvo = false
                avancoAutomatico?.cancel()

                val primeira = perguntas.first()

                _uiState.update {
                    it.copy(
                        apelido = apelido,
                        perguntas = perguntas,
                        indicePerguntaAtual = 0,
                        perguntaAtual = primeira,
                        alternativas = criarAlternativas(primeira, itensDaCategoria),
                        vidasRestantes = VIDAS_INICIAIS,
                        pontuacao = 0,
                        acertos = 0,
                        perguntasRespondidas = 0,
                        respostaSelecionada = null,
                        respostaCorreta = null,
                        pontosUltimaResposta = 0,
                        dicaVisivel = false,
                        carregando = false,
                        partidaIniciada = true,
                        partidaFinalizada = false,
                        navegarParaQuiz = navegar,
                        mensagemErro = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(carregando = false, mensagemErro = "Erro ao carregar perguntas: ${e.message}")
                }
            }
        }
    }

    /** A tela avisa que já navegou, para não navegar de novo ao voltar. */
    fun navegacaoRealizada() {
        _uiState.update { it.copy(navegarParaQuiz = false) }
    }

    /**
     * Sorteia 1 resposta certa + 3 erradas da MESMA categoria.
     * As erradas podem ser de qualquer nível, o que deixa as opções mais variadas.
     */
    private fun criarAlternativas(
        correta: ItemQuizEntity,
        itensDaCategoria: List<ItemQuizEntity>
    ): List<ItemQuizEntity> {
        val erradas = itensDaCategoria
            .filter { it.id != correta.id && it.nomeCorreto != correta.nomeCorreto }
            .distinctBy { it.nomeCorreto }
            .shuffled()
            .take(ALTERNATIVAS_POR_PERGUNTA - 1)

        return (erradas + correta).shuffled()
    }

    // ------------------------------------------------------------------
    // Durante a partida
    // ------------------------------------------------------------------

    fun responder(alternativa: ItemQuizEntity) {
        val estado = _uiState.value
        val pergunta = estado.perguntaAtual ?: return

        // Ignora toques repetidos ou fora de partida
        if (!estado.partidaIniciada || estado.respondida) return

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

        // Mostra o feedback (verde/vermelho) por um instante e avança sozinho
        avancoAutomatico?.cancel()
        avancoAutomatico = viewModelScope.launch {
            delay(TEMPO_FEEDBACK_MS)
            avancarPergunta()
        }
    }

    fun mostrarDica() {
        val estado = _uiState.value
        if (!estado.partidaIniciada || estado.respondida) return
        _uiState.update { it.copy(dicaVisivel = true) }
    }

    private fun avancarPergunta() {
        val estado = _uiState.value
        if (!estado.partidaIniciada || !estado.respondida) return

        val proximoIndice = estado.indicePerguntaAtual + 1

        // Fim de jogo: acabaram as vidas ou as rodadas
        if (estado.vidasRestantes <= 0 || proximoIndice >= estado.perguntas.size) {
            finalizarPartida()
            return
        }

        val proxima = estado.perguntas[proximoIndice]
        val itensDaCategoria = todosItens.filter { it.categoria == estado.categoriaSelecionada }

        _uiState.update {
            it.copy(
                indicePerguntaAtual = proximoIndice,
                perguntaAtual = proxima,
                alternativas = criarAlternativas(proxima, itensDaCategoria),
                respostaSelecionada = null,
                respostaCorreta = null,
                pontosUltimaResposta = 0,
                dicaVisivel = false
            )
        }
    }

    /** Salva o placar no Room (via PlacarRepository) uma única vez por partida. */
    private fun finalizarPartida() {
        if (resultadoSalvo) return
        resultadoSalvo = true

        viewModelScope.launch {
            val estado = _uiState.value

            val placar = PlacarEntity(
                nomeJogador = estado.apelido.ifBlank { "Jogador" },
                categoriaJogada = estado.categoriaSelecionada,
                pontuacao = estado.pontuacao,
                acertos = estado.acertos,
                dificuldade = estado.dificuldadeSelecionada,
                dataHora = System.currentTimeMillis()
            )

            val resultado = placarRepository.inserir(placar)

            _uiState.update {
                it.copy(
                    partidaIniciada = false,
                    partidaFinalizada = true,
                    mensagemErro = if (resultado.isFailure) "Não foi possível salvar o resultado." else null
                )
            }
        }
    }

    /** Volta para a tela inicial (botão voltar ou "Voltar ao início"). */
    fun sairDaPartida() {
        avancoAutomatico?.cancel()
        _uiState.update {
            it.copy(
                partidaIniciada = false,
                partidaFinalizada = false,
                navegarParaQuiz = false,
                perguntas = emptyList(),
                perguntaAtual = null,
                alternativas = emptyList(),
                respostaSelecionada = null,
                respostaCorreta = null,
                dicaVisivel = false,
                mensagemErro = null
            )
        }
    }

    /** "Jogar novamente": mesma categoria, nível e apelido. */
    fun jogarNovamente() {
        sairDaPartida()
        iniciarPartida(navegar = false)
    }
}
