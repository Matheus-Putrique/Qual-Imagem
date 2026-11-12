package com.example.qualimagem.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.qualimagem.data.ItemQuizEntity
import com.example.qualimagem.ui.jogo.CaixaDestaque
import com.example.qualimagem.ui.jogo.CoresJogo
import com.example.qualimagem.viewmodel.RankingViewModel

@Composable
fun DetalheItemScreen(
    id: Int,
    viewModel: RankingViewModel,
    onVoltar: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val item = uiState.itens.firstOrNull { it.id == id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CoresJogo.Fundo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Voltar", tint = CoresJogo.Texto)
            }
            Text("Detalhe do item", color = CoresJogo.Texto, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(12.dp))

        if (item == null) {
            ItemNaoEncontrado()
        } else {
            ConteudoDetalhe(item)
        }
    }
}

@Composable
private fun ConteudoDetalhe(item: ItemQuizEntity) {
    val ehBandeira = item.categoria.equals("Bandeiras", ignoreCase = true)

    CaixaDestaque {
        AsyncImage(
            model = item.imagemUrl,
            contentDescription = item.nomeCorreto,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
        )
    }

    Spacer(Modifier.height(20.dp))

    Text(item.nomeCorreto, color = CoresJogo.Texto, fontSize = 30.sp, fontWeight = FontWeight.Bold)

    Spacer(Modifier.height(10.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TagPequena(item.categoria, CoresJogo.Primaria, icone = Icons.Outlined.Flag, grande = true)
        TagPequena(item.dificuldade, CoresJogo.corDificuldade(item.dificuldade), grande = true)
    }

    Spacer(Modifier.height(20.dp))

    CaixaDestaque {
        Column {
            LinhaInfo(Icons.Outlined.Place, "Região", traduzirRegiao(item.subcategoria))
            HorizontalDivider(color = CoresJogo.Borda, modifier = Modifier.padding(vertical = 12.dp))
            LinhaInfo(Icons.Outlined.Layers, "Categoria", if (ehBandeira) "Bandeiras do mundo" else item.categoria)
        }
    }

    Spacer(Modifier.height(16.dp))

    CaixaDestaque(fundo = CoresJogo.DicaFundo, borda = CoresJogo.Dica.copy(alpha = 0.4f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = CoresJogo.Dica)
            Spacer(Modifier.width(14.dp))
            Column {
                Text("PARA LEMBRAR", color = CoresJogo.Dica, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                Text(item.dica, color = CoresJogo.Texto, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    Spacer(Modifier.height(16.dp))

    Text(
        "${if (ehBandeira) "Esta bandeira" else "Esta imagem"} aparece no quiz no nível ${item.dificuldade}. " +
            "Use a dica acima para lembrar dela na próxima partida.",
        color = CoresJogo.TextoSecundario,
        fontSize = 13.sp,
        lineHeight = 19.sp
    )
}

@Composable
private fun LinhaInfo(icone: ImageVector, titulo: String, valor: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icone, contentDescription = null, tint = CoresJogo.Primaria, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column {
            Text(titulo, color = CoresJogo.TextoSecundario, fontSize = 12.sp)
            Text(valor, color = CoresJogo.Texto, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ItemNaoEncontrado() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.SearchOff, contentDescription = null, tint = CoresJogo.TextoApagado, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text("Item não encontrado", color = CoresJogo.Texto, fontWeight = FontWeight.SemiBold)
            Text("Ele pode ter sido removido do catálogo.", color = CoresJogo.TextoSecundario, fontSize = 13.sp)
        }
    }
}
