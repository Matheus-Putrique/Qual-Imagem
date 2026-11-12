package com.example.qualimagem.viewmodel

enum class Perfil { NENHUM, JOGADOR, ADMIN }

data class SessaoUiState(
    val perfil: Perfil = Perfil.NENHUM
) {
    val isAdmin: Boolean get() = perfil == Perfil.ADMIN
    val logado: Boolean get() = perfil != Perfil.NENHUM
}
