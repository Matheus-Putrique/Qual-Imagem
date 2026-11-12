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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Calendar
import java.util.Locale

class RankingViewModel(
    private val placarRepository: PlacarRepository,
    private val quizRepository: QuizRepository
) : ViewModel() {

    companion object {
        val ORDEM_DIFICULDADES = listOf("Fácil", "Médio", "Difícil")
    }

    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    private var todosPlacares: List<PlacarEntity> = emptyList()
    private var todosItens: List<ItemQuizEntity> = emptyList()

    private val collatorPortugues: Collator = Collator.getInstance(Locale("pt", "BR")).apply {
        strength = Collator.PRIMARY
    }

    init {
        viewModelScope.launch {
            placarRepository.buscarTodos().collect { placares ->
                todosPlacares = placares
                atualizarRanking()
            }
        }
        viewModelScope.launch {
            quizRepository.buscarTodos().collect { itens ->
                todosItens = itens
                atualizarGaleria()
            }
        }
    }

    fun alterarBusca(busca: String) {
        _uiState.update { it.copy(busca = busca) }
        atualizarRanking()
    }

    fun selecionarCategoria(categoria: String) {
        _uiState.update { it.copy(categoriaSelecionada = categoria) }
        atualizarRanking()
    }

    fun selecionarDificuldade(dificuldade: String) {
        _uiState.update { it.copy(dificuldadeSelecionada = dificuldade) }
        atualizarRanking()
    }

    fun selecionarPeriodo(periodo: PeriodoRanking) {
        _uiState.update { it.copy(periodoSelecionado = periodo) }
        atualizarRanking()
    }

    private fun atualizarRanking() {
        val estado = _uiState.value
        val busca = estado.busca.trim()
        val inicioPeriodo = inicioDoPeriodo(estado.periodoSelecionado)

        val filtrados = todosPlacares
            .filter { placar ->
                (estado.categoriaSelecionada.isBlank() || placar.categoriaJogada == estado.categoriaSelecionada) &&
                    (estado.dificuldadeSelecionada.isBlank() || placar.dificuldade == estado.dificuldadeSelecionada) &&
                    (busca.isBlank() || placar.nomeJogador.contains(busca, ignoreCase = true)) &&
                    placar.dataHora >= inicioPeriodo
            }
            .sortedWith(
                compareByDescending<PlacarEntity> { it.pontuacao }
                    .thenByDescending { it.acertos }
                    .thenBy { it.dataHora }
            )

        val niveisExistentes = todosPlacares.map { it.dificuldade }.toSet()

        _uiState.update {
            it.copy(
                placares = filtrados,
                totalJogadores = filtrados.map { placar -> placar.nomeJogador.lowercase() }.distinct().size,
                categorias = todosPlacares.map { placar -> placar.categoriaJogada }.distinct().sorted(),
                dificuldades = ORDEM_DIFICULDADES.filter { nivel -> nivel in niveisExistentes }
            )
        }
    }

    private fun inicioDoPeriodo(periodo: PeriodoRanking): Long {
        if (periodo == PeriodoRanking.GERAL) return 0L

        val calendario = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (periodo == PeriodoRanking.SEMANA) {
            calendario.add(Calendar.DAY_OF_YEAR, -6)
        }
        return calendario.timeInMillis
    }

    fun editarApelido(placar: PlacarEntity, novoApelido: String) {
        val apelido = novoApelido.trim()
        if (apelido.isBlank()) return

        viewModelScope.launch {
            val resultado = placarRepository.atualizar(placar.copy(nomeJogador = apelido))
            _uiState.update {
                it.copy(mensagemErro = if (resultado.isFailure) "Não foi possível editar o apelido." else null)
            }
        }
    }

    fun apagarRegistro(placar: PlacarEntity) {
        viewModelScope.launch {
            val resultado = placarRepository.deletar(placar)
            _uiState.update {
                it.copy(mensagemErro = if (resultado.isFailure) "Não foi possível apagar o registro." else null)
            }
        }
    }

    fun alterarBuscaGaleria(busca: String) {
        _uiState.update { it.copy(buscaGaleria = busca) }
        atualizarGaleria()
    }

    fun selecionarCategoriaGaleria(categoria: String) {
        _uiState.update { it.copy(categoriaGaleriaSelecionada = categoria) }
        atualizarGaleria()
    }

    fun alternarOrdemGaleria() {
        _uiState.update {
            it.copy(ordemGaleria = if (it.ordemGaleria == OrdemGaleria.A_Z) OrdemGaleria.Z_A else OrdemGaleria.A_Z)
        }
        atualizarGaleria()
    }

    private fun atualizarGaleria() {
        val estado = _uiState.value
        val busca = estado.buscaGaleria.trim()

        val filtrados = todosItens.filter { item ->
            (estado.categoriaGaleriaSelecionada.isBlank() || item.categoria == estado.categoriaGaleriaSelecionada) &&
                (busca.isBlank() || item.nomeCorreto.contains(busca, ignoreCase = true))
        }

        val porNome = compareBy<ItemQuizEntity, String>(collatorPortugues) { it.nomeCorreto }
        val ordenados = when (estado.ordemGaleria) {
            OrdemGaleria.A_Z -> filtrados.sortedWith(porNome)
            OrdemGaleria.Z_A -> filtrados.sortedWith(porNome.reversed())
        }

        _uiState.update {
            it.copy(
                itens = todosItens,
                itensGaleria = ordenados,
                categoriasGaleria = todosItens.map { item -> item.categoria }.distinct().sorted()
            )
        }
    }
}
