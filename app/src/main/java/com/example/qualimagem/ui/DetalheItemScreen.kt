package com.example.qualimagem.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.qualimagem.viewmodel.RankingViewModel

@Composable
fun DetalheItemScreen(
    id: Int,
    viewModel: RankingViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val item = uiState.itens.firstOrNull { it.id == id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (item == null) {
            Text("Item não encontrado")
        } else {
            AsyncImage(
                model = item.imagemUrl,
                contentDescription = item.nomeCorreto,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Região: ${item.subcategoria}", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(12.dp))

            Text("Dica: ${item.dica}")
        }
    }
}
