package com.example.qualimagem.navigation

object Rotas {
    const val LOGIN = "login"
    const val INICIO_JOGO = "inicio_jogo"
    const val QUIZ = "quiz"
    const val RANKING = "ranking"
    const val GALERIA = "galeria"
    const val DETALHE = "detalhe/{id}"
    const val ADMIN_DASHBOARD = "admin_dashboard"

    /** "?id=" é opcional: sem id = cadastrar item novo; com id = editar o item. */
    const val ADMIN_FORMULARIO = "admin_formulario?id={id}"

    /** Valor usado quando o formulário abre sem id (novo item). */
    const val SEM_ID = -1

    fun detalhe(id: Int): String = "detalhe/$id"

    fun adminFormulario(id: Int? = null): String =
        if (id == null) "admin_formulario" else "admin_formulario?id=$id"
}

object RotasAbas {
    const val ABA_JOGAR = Rotas.INICIO_JOGO
    const val ABA_GALERIA = Rotas.GALERIA
    const val ABA_RANKING = Rotas.RANKING
    const val ABA_ADMIN = Rotas.ADMIN_DASHBOARD
}
