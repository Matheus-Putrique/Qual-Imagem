package com.example.qualimagem.viewmodel

import com.example.qualimagem.data.PlacarEntity
import com.example.qualimagem.data.ItemQuizEntity

data class RankingUiState(
    val placares: List<PlacarEntity> = emptyList(),
    val itens: List<ItemQuizEntity> = emptyList(),
    val categorias: List<String> = emptyList(),
    val dificuldades: List<String> = emptyList(),
    val categoriaSelecionada: String = "",
    val dificuldadeSelecionada: String = "",
    val busca: String = "",
    val mensagemErro: String? = null
)
