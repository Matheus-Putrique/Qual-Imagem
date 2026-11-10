package com.example.qualimagem.navigation

object Rotas {
    const val LOGIN = "login"
    const val INICIO_JOGO = "inicio_jogo"
    const val QUIZ = "quiz"
    const val RANKING = "ranking"
    const val GALERIA = "galeria"
    const val DETALHE = "detalhe/{id}"
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_FORMULARIO = "admin_formulario"

    fun detalhe(id: Int): String = "detalhe/$id"
}

object RotasAbas {
    const val ABA_INICIO = Rotas.INICIO_JOGO
    const val ABA_RANKING = Rotas.RANKING
    const val ABA_GALERIA = Rotas.GALERIA
    const val ABA_ADMIN = Rotas.ADMIN_DASHBOARD
}

