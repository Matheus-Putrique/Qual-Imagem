package com.example.qualimagem.viewmodel

data class SessaoUiState(
    val nickname: String = "",
    val perfil: String = "",
    val isAdmin: Boolean = false,
    val mensagemErro: String? = null,
    val usuarioLogado: Boolean = false
)

