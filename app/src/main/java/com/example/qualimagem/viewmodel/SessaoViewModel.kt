package com.example.qualimagem.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SessaoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SessaoUiState())
    val uiState: StateFlow<SessaoUiState> = _uiState.asStateFlow()

    fun loginJogador(nickname: String) {
        val apelidoLimpo = nickname.trim()
        if (apelidoLimpo.isBlank()) {
            _uiState.update {
                it.copy(mensagemErro = "Por favor, digite seu apelido para jogar.")
            }
            return
        }

        if (apelidoLimpo.length < 3) {
            _uiState.update {
                it.copy(mensagemErro = "O apelido deve conter no mínimo 3 caracteres.")
            }
            return
        }

        _uiState.update {
            it.copy(
                nickname = apelidoLimpo,
                perfil = "Jogador",
                isAdmin = false,
                mensagemErro = null,
                usuarioLogado = true
            )
        }
    }

    fun loginAdmin(usuario: String, senha: String) {
        val usuarioLimpo = usuario.trim()
        val senhaLimpa = senha.trim()

        if (usuarioLimpo.isBlank() || senhaLimpa.isBlank()) {
            _uiState.update {
                it.copy(mensagemErro = "Informe o usuário e a senha de administrador.")
            }
            return
        }

        if (usuarioLimpo == "admin" && senhaLimpa == "admin123") {
            _uiState.update {
                it.copy(
                    nickname = "Administrador",
                    perfil = "Administrador",
                    isAdmin = true,
                    mensagemErro = null,
                    usuarioLogado = true
                )
            }
        } else {
            _uiState.update {
                it.copy(mensagemErro = "Usuário ou senha de administrador incorretos.")
            }
        }
    }

    fun logout() {
        _uiState.value = SessaoUiState()
    }

    fun limparErro() {
        _uiState.update { it.copy(mensagemErro = null) }
    }
}

