package com.example.qualimagem.viewmodel

import com.example.qualimagem.data.ItemQuizEntity

data class JogoUiState(
    val categorias: List<String> = emptyList(),
    val categoriaSelecionada: String = "",
    val dificuldadeSelecionada: String = "Fácil",

    val perguntas: List<ItemQuizEntity> = emptyList(),
    val indicePerguntaAtual: Int = 0,
    val perguntaAtual: ItemQuizEntity? = null,
    val alternativas: List<ItemQuizEntity> = emptyList(),

    val vidasRestantes: Int = 3,
    val pontuacao: Int = 0,
    val acertos: Int = 0,
    val perguntasRespondidas: Int = 0,

    val respostaSelecionada: String? = null,
    val respostaCorreta: Boolean? = null,
    val dicaVisivel: Boolean = false,

    val carregando: Boolean = false,
    val partidaIniciada: Boolean = false,
    val partidaFinalizada: Boolean = false,
    val mensagemErro: String? = null
)