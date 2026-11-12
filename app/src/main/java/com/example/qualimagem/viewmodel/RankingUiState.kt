package com.example.qualimagem.viewmodel

import com.example.qualimagem.data.ItemQuizEntity
import com.example.qualimagem.data.PlacarEntity

/** Filtro de período do ranking (chip "Geral" do Figma). */
enum class PeriodoRanking(val rotulo: String) {
    GERAL("Geral"),
    SEMANA("Últimos 7 dias"),
    HOJE("Hoje")
}

/** Ordem da lista da galeria (botão "Nome A-Z"). */
enum class OrdemGaleria(val rotulo: String) {
    A_Z("Nome A-Z"),
    Z_A("Nome Z-A")
}

/**
 * Estado das telas da Pessoa 4: Ranking, Galeria e Detalhe.
 * As listas "todos" ficam no ViewModel; aqui ficam só as listas JÁ filtradas e ordenadas.
 */
data class RankingUiState(
    // ---- Ranking ----
    val placares: List<PlacarEntity> = emptyList(),
    val categorias: List<String> = emptyList(),
    val dificuldades: List<String> = emptyList(),
    val categoriaSelecionada: String = "",
    val dificuldadeSelecionada: String = "",
    val periodoSelecionado: PeriodoRanking = PeriodoRanking.GERAL,
    val busca: String = "",
    val totalJogadores: Int = 0,

    // ---- Galeria e Detalhe ----
    val itens: List<ItemQuizEntity> = emptyList(),
    val itensGaleria: List<ItemQuizEntity> = emptyList(),
    val categoriasGaleria: List<String> = emptyList(),
    val categoriaGaleriaSelecionada: String = "",
    val buscaGaleria: String = "",
    val ordemGaleria: OrdemGaleria = OrdemGaleria.A_Z,

    val mensagemErro: String? = null
)
