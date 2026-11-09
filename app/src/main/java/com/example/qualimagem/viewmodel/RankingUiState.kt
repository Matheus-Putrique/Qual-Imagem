package com.example.qualimagem.viewmodel

import com.example.qualimagem.data.PlacarEntity
import com.example.qualimagem.data.ItemQuizEntity

data class RankingUiState(
    val placares: List<PlacarEntity> = emptyList(),
    val itens: List<ItemQuizEntity> = emptyList(),
    val itensGaleria: List<ItemQuizEntity> = emptyList(),
    val categorias: List<String> = emptyList(),
    val categoriasGaleria: List<String> = emptyList(),
    val dificuldades: List<String> = emptyList(),
    val categoriaSelecionada: String = "",
    val categoriaGaleriaSelecionada: String = "",
    val dificuldadeSelecionada: String = "",
    val busca: String = "",
    val buscaGaleria: String = "",
    val mensagemErro: String? = null
)
