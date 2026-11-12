package com.example.qualimagem.viewmodel

import com.example.qualimagem.data.ItemQuizEntity

/**
 * Resumo de uma categoria para os cards da tela inicial do jogo.
 * Montado dinamicamente a partir dos itens salvos no Room.
 */
data class CategoriaResumo(
    val nome: String,
    val quantidadeItens: Int,
    val imagemCapaUrl: String?
)

/**
 * Estado único da partida (padrão MVVM/UDF).
 * A tela só LÊ este objeto; quem altera é o JogoViewModel.
 */
data class JogoUiState(
    // ---- Tela de início ----
    val apelido: String = "",
    val categorias: List<CategoriaResumo> = emptyList(),
    val categoriaSelecionada: String = "",
    val dificuldadeSelecionada: String = "Fácil",
    val sincronizando: Boolean = false,

    // ---- Partida ----
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

    // ---- Controle de fluxo ----
    val carregando: Boolean = false,
    val partidaIniciada: Boolean = false,
    val partidaFinalizada: Boolean = false,
    val navegarParaQuiz: Boolean = false,
    val mensagemErro: String? = null
) {
    /** Total de rodadas desta partida (no máximo 10). */
    val totalRodadas: Int get() = perguntas.size

    /** Rodada exibida para o jogador, começando em 1. */
    val rodadaAtual: Int get() = indicePerguntaAtual + 1

    /** Progresso de 0f a 1f para a barra do topo do quiz. */
    val progresso: Float
        get() = if (totalRodadas == 0) 0f else perguntasRespondidas.toFloat() / totalRodadas

    /** true depois que o jogador escolheu uma alternativa na rodada atual. */
    val respondida: Boolean get() = respostaCorreta != null

    /** O botão "Iniciar Partida" só fica ativo com apelido e categoria válidos. */
    val podeIniciar: Boolean
        get() = apelido.isNotBlank() && categoriaSelecionada.isNotBlank() && !carregando
}
