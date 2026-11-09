package com.example.qualimagem.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.qualimagem.data.PlacarEntity
import com.example.qualimagem.viewmodel.RankingViewModel

@Composable
fun RankingScreen(viewModel: RankingViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var categoriaExpandida by remember { mutableStateOf(false) }
    var dificuldadeExpandida by remember { mutableStateOf(false) }
    var placarParaEditar by remember { mutableStateOf<PlacarEntity?>(null) }
    var novoApelido by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Ranking", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.busca,
            onValueChange = viewModel::alterarBusca,
            label = { Text("Buscar jogador") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        TextButton(onClick = { categoriaExpandida = true }) {
            Text(uiState.categoriaSelecionada.ifBlank { "Todas as categorias" })
        }
        DropdownMenu(
            expanded = categoriaExpandida,
            onDismissRequest = { categoriaExpandida = false }
        ) {
            DropdownMenuItem(
                text = { Text("Todas as categorias") },
                onClick = {
                    viewModel.selecionarCategoria("")
                    categoriaExpandida = false
                }
            )
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

        TextButton(onClick = { dificuldadeExpandida = true }) {
            Text(uiState.dificuldadeSelecionada.ifBlank { "Todas as dificuldades" })
        }
        DropdownMenu(
            expanded = dificuldadeExpandida,
            onDismissRequest = { dificuldadeExpandida = false }
        ) {
            DropdownMenuItem(
                text = { Text("Todas as dificuldades") },
                onClick = {
                    viewModel.selecionarDificuldade("")
                    dificuldadeExpandida = false
                }
            )
            uiState.dificuldades.forEach { dificuldade ->
                DropdownMenuItem(
                    text = { Text(dificuldade) },
                    onClick = {
                        viewModel.selecionarDificuldade(dificuldade)
                        dificuldadeExpandida = false
                    }
                )
            }
        }

        uiState.mensagemErro?.let { mensagem ->
            Text(mensagem, color = MaterialTheme.colorScheme.error)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.placares, key = { it.id }) { placar ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = placar.nomeJogador,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("Pontos: ${placar.pontuacao}")
                        Text("${placar.categoriaJogada} - ${placar.dificuldade}")

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = {
                                novoApelido = placar.nomeJogador
                                placarParaEditar = placar
                            }) {
                                Text("Editar apelido")
                            }
                            TextButton(onClick = { viewModel.apagarRegistro(placar) }) {
                                Text("Apagar")
                            }
                        }
                    }
                }
            }
        }
    }

    placarParaEditar?.let { placar ->
        AlertDialog(
            onDismissRequest = { placarParaEditar = null },
            title = { Text("Editar apelido") },
            text = {
                OutlinedTextField(
                    value = novoApelido,
                    onValueChange = { novoApelido = it },
                    label = { Text("Apelido") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.editarApelido(placar, novoApelido)
                        placarParaEditar = null
                    },
                    enabled = novoApelido.isNotBlank()
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { placarParaEditar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
