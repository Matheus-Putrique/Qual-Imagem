package com.example.qualimagem.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SessaoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SessaoUiState())
    val uiState: StateFlow<SessaoUiState> = _uiState.asStateFlow()

    fun entrarComoJogador() {
        _uiState.update { it.copy(perfil = Perfil.JOGADOR) }
    }

    fun entrarComoAdmin() {
        _uiState.update { it.copy(perfil = Perfil.ADMIN) }
    }

    fun logout() {
        _uiState.value = SessaoUiState()
    }
}
