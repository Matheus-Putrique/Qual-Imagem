package com.example.qualimagem.viewmodel

import com.example.qualimagem.data.ItemQuizEntity
import com.example.qualimagem.data.PlacarEntity

enum class PeriodoRanking(val rotulo: String) {
    GERAL("Geral"),
    SEMANA("Últimos 7 dias"),
    HOJE("Hoje")
}

enum class OrdemGaleria(val rotulo: String) {
    A_Z("Nome A-Z"),
    Z_A("Nome Z-A")
}

data class RankingUiState(
    val placares: List<PlacarEntity> = emptyList(),
    val categorias: List<String> = emptyList(),
    val dificuldades: List<String> = emptyList(),
    val categoriaSelecionada: String = "",
    val dificuldadeSelecionada: String = "",
    val periodoSelecionado: PeriodoRanking = PeriodoRanking.GERAL,
    val busca: String = "",
    val totalJogadores: Int = 0,

    val itens: List<ItemQuizEntity> = emptyList(),
    val itensGaleria: List<ItemQuizEntity> = emptyList(),
    val categoriasGaleria: List<String> = emptyList(),
    val categoriaGaleriaSelecionada: String = "",
    val buscaGaleria: String = "",
    val ordemGaleria: OrdemGaleria = OrdemGaleria.A_Z,

    val mensagemErro: String? = null
)
