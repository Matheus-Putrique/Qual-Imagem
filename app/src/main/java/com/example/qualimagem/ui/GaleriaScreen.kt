package com.example.qualimagem.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.qualimagem.viewmodel.RankingViewModel

@Composable
fun GaleriaScreen(
    viewModel: RankingViewModel,
    onAbrirDetalhe: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val itens = uiState.itens
    var busca by remember { mutableStateOf("") }
    var categoriaSelecionada by remember { mutableStateOf("") }
    var categoriaExpandida by remember { mutableStateOf(false) }

    val categorias = itens.map { it.categoria }.distinct().sorted()
    val itensFiltrados = itens.filter { item ->
        (categoriaSelecionada.isBlank() || item.categoria == categoriaSelecionada) &&
            (busca.isBlank() || item.nomeCorreto.contains(busca.trim(), ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Galeria", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = busca,
            onValueChange = { busca = it },
            label = { Text("Buscar item") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        TextButton(onClick = { categoriaExpandida = true }) {
            Text(categoriaSelecionada.ifBlank { "Todas as categorias" })
        }
        DropdownMenu(
            expanded = categoriaExpandida,
            onDismissRequest = { categoriaExpandida = false }
        ) {
            DropdownMenuItem(
                text = { Text("Todas as categorias") },
                onClick = {
                    categoriaSelecionada = ""
                    categoriaExpandida = false
                }
            )
            categorias.forEach { categoria ->
                DropdownMenuItem(
                    text = { Text(categoria) },
                    onClick = {
                        categoriaSelecionada = categoria
                        categoriaExpandida = false
                    }
                )
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(itensFiltrados, key = { it.id }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAbrirDetalhe(item.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = item.imagemUrl,
                            contentDescription = item.nomeCorreto,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(item.nomeCorreto)
                    }
                }
            }
        }
    }
}
