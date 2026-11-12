package com.example.qualimagem.ui.jogo

import androidx.compose.ui.graphics.Color

/**
 * Paleta tirada do Figma (tema escuro azul-marinho).
 * Fica dentro do pacote ui/jogo para a parte 3 não depender do tema global.
 */
object CoresJogo {
    val Fundo = Color(0xFF0A1228)
    val Superficie = Color(0xFF16213A)
    val SuperficieElevada = Color(0xFF1E2A44)
    val Borda = Color(0xFF2A3754)

    val Primaria = Color(0xFF38BDF8)
    val PrimariaFundo = Color(0xFF0E3A52)
    val NaPrimaria = Color(0xFF06223A)

    val Texto = Color(0xFFF1F5F9)
    val TextoSecundario = Color(0xFF94A3B8)
    val TextoApagado = Color(0xFF64748B)

    val Acerto = Color(0xFF22C55E)
    val AcertoFundo = Color(0xFF0F3B2C)

    val Erro = Color(0xFFEF4444)
    val ErroFundo = Color(0xFF3F1820)

    val Dica = Color(0xFFF59E0B)
    val DicaFundo = Color(0xFF3A2C0E)

    /** Cor do texto de cada nível de dificuldade. */
    fun corDificuldade(dificuldade: String): Color = when (dificuldade) {
        "Médio" -> Dica
        "Difícil" -> Erro
        else -> Acerto
    }
}
