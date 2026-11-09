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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RankingViewModel(
    private val placarRepository: PlacarRepository,
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    private var todosPlacares: List<PlacarEntity> = emptyList()
    private var todosItens: List<ItemQuizEntity> = emptyList()

    init {
        viewModelScope.launch {
            placarRepository.buscarTodos().collect { placares ->
                todosPlacares = placares
                atualizarLista()
            }
        }
        viewModelScope.launch {
            quizRepository.buscarTodos().collect { itens ->
                todosItens = itens
                atualizarGaleria()
            }
        }
    }

    fun selecionarCategoria(categoria: String) {
        _uiState.update { it.copy(categoriaSelecionada = categoria) }
        atualizarLista()
    }

    fun selecionarDificuldade(dificuldade: String) {
        _uiState.update { it.copy(dificuldadeSelecionada = dificuldade) }
        atualizarLista()
    }

    fun alterarBusca(busca: String) {
        _uiState.update { it.copy(busca = busca) }
        atualizarLista()
    }

    fun selecionarCategoriaGaleria(categoria: String) {
        _uiState.update { it.copy(categoriaGaleriaSelecionada = categoria) }
        atualizarGaleria()
    }

    fun alterarBuscaGaleria(busca: String) {
        _uiState.update { it.copy(buscaGaleria = busca) }
        atualizarGaleria()
    }

    private fun atualizarLista() {
        val estado = _uiState.value
        val busca = estado.busca.trim()

        val placaresFiltrados = todosPlacares
            .filter { placar ->
                (estado.categoriaSelecionada.isBlank() ||
                    placar.categoriaJogada == estado.categoriaSelecionada) &&
                    (estado.dificuldadeSelecionada.isBlank() ||
                        placar.dificuldade == estado.dificuldadeSelecionada) &&
                    (busca.isBlank() ||
                        placar.nomeJogador.contains(busca, ignoreCase = true))
            }
            .sortedByDescending { it.pontuacao }

        _uiState.update {
            it.copy(
                placares = placaresFiltrados,
                categorias = todosPlacares.map { placar -> placar.categoriaJogada }
                    .distinct().sorted(),
                dificuldades = todosPlacares.map { placar -> placar.dificuldade }
                    .distinct().sorted()
            )
        }
    }

    private fun atualizarGaleria() {
        val estado = _uiState.value
        val busca = estado.buscaGaleria.trim()

        val itensFiltrados = todosItens
            .filter { item ->
                (estado.categoriaGaleriaSelecionada.isBlank() ||
                    item.categoria == estado.categoriaGaleriaSelecionada) &&
                    (busca.isBlank() ||
                        item.nomeCorreto.contains(busca, ignoreCase = true))
            }
            .sortedBy { it.nomeCorreto }

        _uiState.update {
            it.copy(
                itens = todosItens,
                itensGaleria = itensFiltrados,
                categoriasGaleria = todosItens.map { item -> item.categoria }
                    .distinct().sorted()
            )
        }
    }

    fun editarApelido(placar: PlacarEntity, novoApelido: String) {
        if (novoApelido.isBlank()) return

        viewModelScope.launch {
            val resultado = placarRepository.atualizar(
                placar.copy(nomeJogador = novoApelido.trim())
            )
            if (resultado.isFailure) {
                _uiState.update {
                    it.copy(mensagemErro = "Não foi possível editar o apelido.")
                }
            } else {
                _uiState.update { it.copy(mensagemErro = null) }
            }
        }
    }

    fun apagarRegistro(placar: PlacarEntity) {
        viewModelScope.launch {
            val resultado = placarRepository.deletar(placar)
            if (resultado.isFailure) {
                _uiState.update {
                    it.copy(mensagemErro = "Não foi possível apagar o registro.")
                }
            } else {
                _uiState.update { it.copy(mensagemErro = null) }
            }
        }
    }
}
