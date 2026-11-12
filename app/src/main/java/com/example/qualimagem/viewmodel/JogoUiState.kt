package com.example.qualimagem.viewmodel

import com.example.qualimagem.data.ItemQuizEntity

data class CategoriaResumo(
    val nome: String,
    val quantidadeItens: Int,
    val imagemCapaUrl: String?
)

data class JogoUiState(
    val apelido: String = "",
    val categorias: List<CategoriaResumo> = emptyList(),
    val categoriaSelecionada: String = "",
    val dificuldadeSelecionada: String = "Fácil",
    val sincronizando: Boolean = false,

    val perguntas: List<ItemQuizEntity> = emptyList(),
    val indicePerguntaAtual: Int = 0,
    val perguntaAtual: ItemQuizEntity? = null,
    val alternativas: List<ItemQuizEntity> = emptyList(),

    val vidasRestantes: Int = JogoViewModel.VIDAS_INICIAIS,
    val pontuacao: Int = 0,
    val acertos: Int = 0,
    val perguntasRespondidas: Int = 0,

    val respostaSelecionada: ItemQuizEntity? = null,
    val respostaCorreta: Boolean? = null,
    val pontosUltimaResposta: Int = 0,
    val dicaVisivel: Boolean = false,

    val carregando: Boolean = false,
    val partidaIniciada: Boolean = false,
    val partidaFinalizada: Boolean = false,
    val navegarParaQuiz: Boolean = false,
    val mensagemErro: String? = null
) {
    val totalRodadas: Int get() = perguntas.size

    val rodadaAtual: Int get() = indicePerguntaAtual + 1

    val progresso: Float
        get() = if (totalRodadas == 0) 0f else perguntasRespondidas.toFloat() / totalRodadas

    val respondida: Boolean get() = respostaCorreta != null

    val podeIniciar: Boolean
        get() = apelido.isNotBlank() && categoriaSelecionada.isNotBlank() && !carregando
}
