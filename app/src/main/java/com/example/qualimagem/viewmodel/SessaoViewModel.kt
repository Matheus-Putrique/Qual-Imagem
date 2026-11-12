package com.example.qualimagem.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Controla qual perfil está usando o app.
 * O AppNavigation e a BottomNavBar leem `isAdmin` para liberar (ou não) a área do Admin.
 */
class SessaoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SessaoUiState())
    val uiState: StateFlow<SessaoUiState> = _uiState.asStateFlow()

    fun entrarComoJogador() {
        _uiState.update { it.copy(perfil = Perfil.JOGADOR) }
    }

    fun entrarComoAdmin() {
        _uiState.update { it.copy(perfil = Perfil.ADMIN) }
    }

    /** Volta ao estado inicial (nenhum perfil). Chamado ao voltar para a tela de entrada. */
    fun logout() {
        _uiState.value = SessaoUiState()
    }
}
