
package com.example.qualimagem.ui.jogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.qualimagem.viewmodel.JogoViewModel

@Composable
fun InicioJogoScreen(
    viewModel: JogoViewModel,
    onIniciarJogo: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var nomeJogador by remember { mutableStateOf("") }
    var categoriaExpandida by remember { mutableStateOf(false) }
    var dificuldadeExpandida by remember { mutableStateOf(false) }

    val dificuldades = listOf("Fácil", "Médio", "Difícil")

    LaunchedEffect(uiState.partidaIniciada) {
        if (uiState.partidaIniciada) {
            onIniciarJogo()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Qual-Imagem",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Prepare-se para testar seus conhecimentos!",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = nomeJogador,
            onValueChange = { nomeJogador = it },
            label = { Text("Nome do jogador") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Categoria")

        TextButton(
            onClick = { categoriaExpandida = true },
            enabled = uiState.categorias.isNotEmpty()
        ) {
            Text(
                text = uiState.categoriaSelecionada.ifBlank {
                    "Selecione uma categoria"
                }
            )
        }

        DropdownMenu(
            expanded = categoriaExpandida,
            onDismissRequest = { categoriaExpandida = false }
        ) {
            uiState.categorias.forEach { categoria ->
                DropdownMenuItem(
                    text = { Text(categoria) },
                    onClick = {
                        viewModel.selecionarCategoria(categoria)
                        categoriaExpandida = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Dificuldade")

        TextButton(
            onClick = { dificuldadeExpandida = true }
        ) {
            Text(uiState.dificuldadeSelecionada)
        }

        DropdownMenu(
            expanded = dificuldadeExpandida,
            onDismissRequest = { dificuldadeExpandida = false }
        ) {
            dificuldades.forEach { dificuldade ->
                DropdownMenuItem(
                    text = { Text(dificuldade) },
                    onClick = {
                        viewModel.selecionarDificuldade(dificuldade)
                        dificuldadeExpandida = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.categorias.isEmpty()) {
            Text(
                text = "Nenhuma categoria disponível. Aguarde o cadastro dos itens do quiz."
            )
        }

        uiState.mensagemErro?.let { mensagem ->
            Text(
                text = mensagem,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = {
                viewModel.iniciarPartida(
                    categoria = uiState.categoriaSelecionada,
                    dificuldade = uiState.dificuldadeSelecionada,
                    jogador = nomeJogador
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.categoriaSelecionada.isNotBlank() &&
                    !uiState.carregando
        ) {
            Text("Iniciar partida")
        }

        if (uiState.carregando) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}
