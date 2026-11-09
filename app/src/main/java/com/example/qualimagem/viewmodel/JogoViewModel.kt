package com.example.qualimagem.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qualimagem.data.ItemQuizEntity
import com.example.qualimagem.data.PlacarEntity
import com.example.qualimagem.data.repository.PlacarRepository
import com.example.qualimagem.data.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JogoViewModel(
    private val quizRepository: QuizRepository,
    private val placarRepository: PlacarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JogoUiState())
    val uiState: StateFlow<JogoUiState> = _uiState.asStateFlow()

    private var nomeJogador: String = "Jogador"
    private var resultadoSalvo = false

    init {
        carregarCategorias()
    }

    private fun carregarCategorias() {
        viewModelScope.launch {
            quizRepository.buscarTodos().collect { itens ->
                val categorias = itens
                    .map { it.categoria }
                    .distinct()
                    .sorted()

                _uiState.update {
                    val categoriaPadrao = if (it.categoriaSelecionada.isBlank() && categorias.isNotEmpty()) {
                        categorias.first()
                    } else {
                        it.categoriaSelecionada
                    }
                    it.copy(
                        categorias = categorias,
                        categoriaSelecionada = categoriaPadrao
                    )
                }
            }
        }
    }

    fun selecionarCategoria(categoria: String) {
        _uiState.update {
            it.copy(categoriaSelecionada = categoria)
        }
    }

    fun selecionarDificuldade(dificuldade: String) {
        _uiState.update {
            it.copy(dificuldadeSelecionada = dificuldade)
        }
    }

    fun sairDaPartida() {
        _uiState.update {
            it.copy(
                partidaIniciada = false,
                partidaFinalizada = false
            )
        }
    }

    fun iniciarPartida(
        categoria: String,
        dificuldade: String,
        jogador: String
    ) {
        if (categoria.isBlank()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    carregando = true,
                    mensagemErro = null,
                    partidaIniciada = false,
                    partidaFinalizada = false
                )
            }

            try {
                val todosItens = quizRepository.buscarTodos().first()

                val perguntasDisponiveis = todosItens
                    .filter {
                        it.categoria == categoria &&
                                it.dificuldade.equals(dificuldade, ignoreCase = true)
                    }
                    .shuffled()

                if (perguntasDisponiveis.size < 4) {
                    _uiState.update {
                        it.copy(
                            carregando = false,
                            mensagemErro = "São necessários pelo menos 4 itens nessa categoria e dificuldade."
                        )
                    }
                    return@launch
                }

                nomeJogador = jogador.ifBlank { "Jogador" }
                resultadoSalvo = false

                val primeiraPergunta = perguntasDisponiveis.first()

                val alternativas = criarAlternativas(
                    primeiraPergunta,
                    perguntasDisponiveis
                )

                _uiState.update {
                    JogoUiState(
                        categorias = it.categorias,
                        categoriaSelecionada = categoria,
                        dificuldadeSelecionada = dificuldade,
                        perguntas = perguntasDisponiveis,
                        indicePerguntaAtual = 0,
                        perguntaAtual = primeiraPergunta,
                        alternativas = alternativas,
                        vidasRestantes = 3,
                        pontuacao = 0,
                        acertos = 0,
                        perguntasRespondidas = 0,
                        carregando = false,
                        partidaIniciada = true,
                        partidaFinalizada = false
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        carregando = false,
                        mensagemErro = "Erro ao carregar perguntas: ${e.message}"
                    )
                }
            }
        }
    }

    private fun criarAlternativas(
        perguntaCorreta: ItemQuizEntity,
        perguntasDisponiveis: List<ItemQuizEntity>
    ): List<ItemQuizEntity> {

        val alternativasErradas = perguntasDisponiveis
            .filter { it.id != perguntaCorreta.id }
            .shuffled()
            .take(3)
            .map { it }

        return (listOf(perguntaCorreta) + alternativasErradas).shuffled()
    }

    fun responder(alternativa: ItemQuizEntity) {
        val estado = _uiState.value
        val perguntaAtual = estado.perguntaAtual ?: return

        if (!estado.partidaIniciada ||
            estado.partidaFinalizada ||
            estado.respostaCorreta != null ||
            estado.alternativas.none { it.id == alternativa.id }
        ) return

        val acertou = alternativa.id == perguntaAtual.id

        val pontosGanhos = if (acertou) {
            when (estado.dificuldadeSelecionada) {
                "Fácil" -> 10
                "Médio" -> 20
                "Difícil" -> 30
                else -> 10
            }
        } else {
            0
        }

        _uiState.update {
            it.copy(
                respostaSelecionada = alternativa.nomeCorreto,
                respostaCorreta = acertou,
                pontuacao = it.pontuacao + pontosGanhos,
                acertos = if (acertou) it.acertos + 1 else it.acertos,
                vidasRestantes = if (acertou) {
                    it.vidasRestantes
                } else {
                    it.vidasRestantes - 1
                },
                perguntasRespondidas = it.perguntasRespondidas + 1
            )
        }
    }

    fun mostrarDica() {
        val estado = _uiState.value

        if (!estado.partidaIniciada ||
            estado.partidaFinalizada ||
            estado.respostaCorreta != null
        ) return

        _uiState.update {
            it.copy(dicaVisivel = true)
        }
    }

    fun avancarPergunta() {
        val estado = _uiState.value

        if (!estado.partidaIniciada ||
            estado.partidaFinalizada ||
            estado.respostaCorreta == null
        ) return

        val proximoIndice = estado.indicePerguntaAtual + 1

        if (estado.vidasRestantes <= 0 ||
            proximoIndice >= estado.perguntas.size
        ) {
            finalizarPartida()
            return
        }

        val proximaPergunta = estado.perguntas[proximoIndice]

        val alternativas = criarAlternativas(
            proximaPergunta,
            estado.perguntas
        )

        _uiState.update {
            it.copy(
                indicePerguntaAtual = proximoIndice,
                perguntaAtual = proximaPergunta,
                alternativas = alternativas,
                respostaSelecionada = null,
                respostaCorreta = null,
                dicaVisivel = false
            )
        }
    }

    private fun finalizarPartida() {
        if (resultadoSalvo) return

        resultadoSalvo = true

        viewModelScope.launch {
            try {
                val estado = _uiState.value

                val placar = PlacarEntity(
                    nomeJogador = nomeJogador,
                    categoriaJogada = estado.categoriaSelecionada,
                    pontuacao = estado.pontuacao,
                    acertos = estado.acertos,
                    dificuldade = estado.dificuldadeSelecionada,
                    dataHora = System.currentTimeMillis()
                )

                val resultado = placarRepository.inserir(placar)

                _uiState.update {
                    it.copy(
                        partidaFinalizada = true,
                        partidaIniciada = false,
                        mensagemErro = if (resultado.isFailure) {
                            "Não foi possível salvar o resultado."
                        } else {
                            null
                        }
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        partidaFinalizada = true,
                        partidaIniciada = false,
                        mensagemErro = "Ocorreu um erro ao salvar o resultado."
                    )
                }
            }
        }
    }
}
