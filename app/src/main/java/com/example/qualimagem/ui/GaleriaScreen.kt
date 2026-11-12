package com.example.qualimagem.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.qualimagem.data.ItemQuizEntity
import com.example.qualimagem.ui.jogo.CaixaDestaque
import com.example.qualimagem.ui.jogo.CampoTextoJogo
import com.example.qualimagem.ui.jogo.CoresJogo
import com.example.qualimagem.viewmodel.RankingViewModel

/**
 * "Um mundo para ver": catálogo de todos os itens salvos no Room,
 * com busca, filtro por categoria, ordenação A-Z/Z-A e toque para abrir o detalhe.
 */
@Composable
fun GaleriaScreen(
    viewModel: RankingViewModel,
    onAbrirDetalhe: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CoresJogo.Fundo),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            CabecalhoTela(
                titulo = "Um mundo para ver",
                subtitulo = "Explore as bandeiras. Descubra suas histórias.",
                icone = Icons.Outlined.Public
            )
        }

        item {
            Spacer(Modifier.height(8.dp))
            CampoTextoJogo(
                valor = uiState.buscaGaleria,
                onValorChange = viewModel::alterarBuscaGaleria,
                placeholder = "Buscar país...",
                icone = Icons.Outlined.Search
            )
        }

        // ---- Chips: Todas + categorias do banco + "Em Breve" bloqueado ----
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChipFiltro(
                    texto = "Todas",
                    ativo = uiState.categoriaGaleriaSelecionada.isBlank(),
                    onClick = { viewModel.selecionarCategoriaGaleria("") }
                )
                uiState.categoriasGaleria.forEach { categoria ->
                    ChipFiltro(
                        texto = categoria,
                        ativo = categoria == uiState.categoriaGaleriaSelecionada,
                        icone = Icons.Outlined.Flag,
                        onClick = { viewModel.selecionarCategoriaGaleria(categoria) }
                    )
                }
                ChipFiltro(
                    texto = "Em Breve",
                    ativo = false,
                    icone = Icons.Outlined.Lock,
                    habilitado = false,
                    onClick = {}
                )
            }
        }

        // ---- Contador + ordenação ----
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    textoContador(uiState.itensGaleria),
                    color = CoresJogo.TextoSecundario,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = viewModel::alternarOrdemGaleria)
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(uiState.ordemGaleria.rotulo, color = CoresJogo.Primaria, fontSize = 12.sp)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Rounded.SwapVert, contentDescription = "Inverter ordem", tint = CoresJogo.Primaria, modifier = Modifier.size(16.dp))
                }
            }
        }

        if (uiState.itensGaleria.isEmpty()) {
            item { GaleriaVazia(temBusca = uiState.buscaGaleria.isNotBlank()) }
        }

        // ---- Lista (LazyColumn só desenha o que está visível na tela) ----
        items(uiState.itensGaleria, key = { it.id }) { item ->
            CardItemGaleria(item = item, onClick = { onAbrirDetalhe(item.id) })
        }

        item {
            CaixaDestaque(fundo = CoresJogo.PrimariaFundo.copy(alpha = 0.5f), borda = CoresJogo.PrimariaFundo) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = CoresJogo.Primaria, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Toque em um país para ver detalhes e se preparar para o próximo desafio.",
                        color = CoresJogo.TextoSecundario,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/** "250 países na coleção" quando só há bandeiras; "N itens na coleção" se misturar categorias. */
private fun textoContador(itens: List<ItemQuizEntity>): String {
    val soBandeiras = itens.isNotEmpty() && itens.all { it.categoria.equals("Bandeiras", ignoreCase = true) }
    val unidade = when {
        soBandeiras && itens.size == 1 -> "país"
        soBandeiras -> "países"
        itens.size == 1 -> "item"
        else -> "itens"
    }
    return "${itens.size} $unidade na coleção"
}

/** Card da lista: imagem, nome, nível colorido e seta ">". */
@Composable
private fun CardItemGaleria(item: ItemQuizEntity, onClick: () -> Unit) {
    val forma = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(CoresJogo.Superficie)
            .border(1.dp, CoresJogo.Borda, forma)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.imagemUrl,
            contentDescription = item.nomeCorreto,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(64.dp)
                .height(42.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(CoresJogo.SuperficieElevada)
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                item.nomeCorreto,
                color = CoresJogo.Texto,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(item.dificuldade, color = CoresJogo.corDificuldade(item.dificuldade), fontSize = 12.sp)
        }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = CoresJogo.TextoSecundario)
    }
}

@Composable
private fun GaleriaVazia(temBusca: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Outlined.SearchOff, contentDescription = null, tint = CoresJogo.TextoApagado, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp))
        Text(
            if (temBusca) "Nenhum país encontrado" else "A coleção está vazia",
            color = CoresJogo.Texto,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            if (temBusca) "Tente outro nome." else "Baixe as bandeiras na tela Início.",
            color = CoresJogo.TextoSecundario,
            fontSize = 13.sp
        )
    }
}
