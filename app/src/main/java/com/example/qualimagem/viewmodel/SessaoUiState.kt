package com.example.qualimagem.viewmodel

/** Perfil escolhido na tela inicial. */
enum class Perfil { NENHUM, JOGADOR, ADMIN }

/**
 * Estado da sessão: só guarda QUAL perfil entrou.
 * Não há usuário nem senha: a escolha é feita por dois botões na tela inicial.
 */
data class SessaoUiState(
    val perfil: Perfil = Perfil.NENHUM
) {
    val isAdmin: Boolean get() = perfil == Perfil.ADMIN
    val logado: Boolean get() = perfil != Perfil.NENHUM
}
