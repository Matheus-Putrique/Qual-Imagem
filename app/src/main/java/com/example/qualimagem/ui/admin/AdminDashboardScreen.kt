package com.example.qualimagem.ui.admin

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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.qualimagem.data.ItemQuizEntity
import com.example.qualimagem.ui.CabecalhoTela
import com.example.qualimagem.ui.ChipFiltro
import com.example.qualimagem.ui.components.BotaoPrincipal
import com.example.qualimagem.ui.components.CardPadrao
import com.example.qualimagem.ui.components.EstadoVazio
import com.example.qualimagem.ui.jogo.CampoTextoJogo
import com.example.qualimagem.ui.jogo.CoresJogo
import com.example.qualimagem.ui.theme.QualImagemTema
import com.example.qualimagem.viewmodel.AdminUiState
import com.example.qualimagem.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onNovoItem: () -> Unit,
    onEditarItem: (Int) -> Unit
) {
    AdminDashboardConteudo(
        estado = viewModel.uiState,
        onBuscar = viewModel::alterarBusca,
        onSelecionarCategoria = viewModel::selecionarCategoria,
        onSincronizar = viewModel::sincronizarApi,
        onNovoItem = {
            viewModel.limparMensagens()
            onNovoItem()
        },
        onEditarItem = { item ->
            viewModel.limparMensagens()
            onEditarItem(item.id)
        },
        onExcluirItem = viewModel::excluirItem,
        onPaginaAnterior = viewModel::paginaAnterior,
        onProximaPagina = viewModel::proximaPagina
    )
}

@Composable
fun AdminDashboardConteudo(
    estado: AdminUiState,
    onBuscar: (String) -> Unit,
    onSelecionarCategoria: (String) -> Unit,
    onSincronizar: () -> Unit,
    onNovoItem: () -> Unit,
    onEditarItem: (ItemQuizEntity) -> Unit,
    onExcluirItem: (ItemQuizEntity) -> Unit,
    onPaginaAnterior: () -> Unit,
    onProximaPagina: () -> Unit
) {
    var itemParaExcluir by remember { mutableStateOf<ItemQuizEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            CabecalhoTela(
                titulo = "Painel Admin",
                subtitulo = "Seu catálogo, sempre em dia.",
                icone = Icons.Outlined.Shield
            )
        }

        item {
            Spacer(Modifier.height(8.dp))
            CardStatus(estado = estado, onSincronizar = onSincronizar)
        }

        estado.mensagemSucesso?.let { msg ->
            item { LinhaMensagem(msg, Icons.Outlined.CheckCircle, CoresJogo.Acerto) }
        }
        estado.mensagemErro?.let { msg ->
            item { LinhaMensagem(msg, Icons.Outlined.ErrorOutline, CoresJogo.Erro) }
        }

        item {
            BotaoPrincipal(texto = "Novo Item", icone = Icons.Rounded.Add, onClick = onNovoItem)
        }

        item {
            CampoTextoJogo(
                valor = estado.busca,
                onValorChange = onBuscar,
                placeholder = "Buscar item...",
                icone = Icons.Outlined.Search
            )
        }

        if (estado.categorias.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChipFiltro("Todas", ativo = estado.categoriaFiltro.isBlank(), onClick = { onSelecionarCategoria("") })
                    estado.categorias.forEach { categoria ->
                        ChipFiltro(
                            texto = categoria,
                            ativo = categoria == estado.categoriaFiltro,
                            icone = Icons.Outlined.Flag,
                            onClick = { onSelecionarCategoria(categoria) }
                        )
                    }
                }
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Itens do catálogo",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${estado.totalFiltrado} ${if (estado.totalFiltrado == 1) "item" else "itens"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }

        if (estado.itensPagina.isEmpty()) {
            item {
                EstadoVazio(
                    mensagem = if (estado.totalNoBanco == 0) "Catálogo vazio" else "Nenhum item encontrado",
                    subtitulo = if (estado.totalNoBanco == 0) "Toque em \"Sincronizar Bandeiras da API\" ou cadastre um item." else "Tente outro nome ou categoria.",
                    icone = Icons.Outlined.Inventory2
                )
            }
        }

        items(estado.itensPagina, key = { it.id }) { item ->
            CardItemAdmin(
                item = item,
                onEditar = { onEditarItem(item) },
                onExcluir = { itemParaExcluir = item }
            )
        }

        if (estado.totalFiltrado > 0) {
            item {
                Paginacao(estado, onPaginaAnterior, onProximaPagina)
            }
        }
    }

    itemParaExcluir?.let { item ->
        AlertDialog(
            onDismissRequest = { itemParaExcluir = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Excluir item?") },
            text = { Text("\"${item.nomeCorreto}\" será removido do catálogo e não aparecerá mais no quiz.") },
            confirmButton = {
                TextButton(onClick = {
                    onExcluirItem(item)
                    itemParaExcluir = null
                }) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { itemParaExcluir = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun CardStatus(estado: AdminUiState, onSincronizar: () -> Unit) {
    CardPadrao {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${estado.totalNoBanco}",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (estado.categorias.size <= 1 && estado.categorias.firstOrNull() == "Bandeiras") "bandeiras" else "itens",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                SeloStatus(estado)
            }
            Text(
                textoUltimaSincronizacao(estado.ultimaSincronizacao),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(16.dp))
            BotaoPrincipal(
                texto = if (estado.sincronizandoApi) "Sincronizando..." else "Sincronizar Bandeiras da API",
                icone = Icons.Rounded.Sync,
                secundario = true,
                carregando = estado.sincronizandoApi,
                onClick = onSincronizar
            )
        }
    }
}

@Composable
private fun SeloStatus(estado: AdminUiState) {
    val (texto, cor) = when {
        estado.ultimaSincronizacao != null -> "Em dia" to CoresJogo.Acerto
        estado.totalNoBanco == 0 -> "Vazio" to CoresJogo.Dica
        else -> "Local" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(cor.copy(alpha = 0.12f))
            .border(1.dp, cor.copy(alpha = 0.4f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (estado.ultimaSincronizacao != null) {
            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = cor, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(texto, color = cor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

private fun textoUltimaSincronizacao(momento: Long?): String =
    if (momento == null) {
        "Itens salvos no aparelho. Sincronize para buscar novidades."
    } else {
        "Atualizado hoje, às " + SimpleDateFormat("HH:mm", Locale("pt", "BR")).format(Date(momento))
    }

@Composable
private fun LinhaMensagem(texto: String, icone: androidx.compose.ui.graphics.vector.ImageVector, cor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icone, contentDescription = null, tint = cor, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(texto, color = cor, fontSize = 13.sp)
    }
}

@Composable
private fun CardItemAdmin(item: ItemQuizEntity, onEditar: () -> Unit, onExcluir: () -> Unit) {
    CardPadrao(onClick = onEditar) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
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
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.nomeCorreto,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row {
                    Text("${item.categoria} · ", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text(item.dificuldade, color = CoresJogo.corDificuldade(item.dificuldade), fontSize = 12.sp)
                }
            }
            IconButton(onClick = onEditar) {
                Icon(Icons.Outlined.Edit, contentDescription = "Editar ${item.nomeCorreto}", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onExcluir) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Excluir ${item.nomeCorreto}", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun Paginacao(estado: AdminUiState, onAnterior: () -> Unit, onProxima: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Exibindo ${estado.primeiroDaPagina}–${estado.ultimoDaPagina} de ${estado.totalFiltrado}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
        BotaoPagina("Anteriores", Icons.AutoMirrored.Rounded.KeyboardArrowLeft, iconeAntes = true, habilitado = estado.temPaginaAnterior, onClick = onAnterior)
        Spacer(Modifier.width(8.dp))
        BotaoPagina("Próximos", Icons.AutoMirrored.Rounded.KeyboardArrowRight, iconeAntes = false, habilitado = estado.temProximaPagina, onClick = onProxima)
    }
}

@Composable
private fun BotaoPagina(
    texto: String,
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    iconeAntes: Boolean,
    habilitado: Boolean,
    onClick: () -> Unit
) {
    val cor = if (habilitado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = habilitado, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconeAntes) Icon(icone, contentDescription = null, tint = cor, modifier = Modifier.size(18.dp))
        Text(texto, color = cor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        if (!iconeAntes) Icon(icone, contentDescription = null, tint = cor, modifier = Modifier.size(18.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminDashboardPreview() {
    val exemplos = listOf(
        ItemQuizEntity(1, "Japão", "https://flagcdn.com/w320/jp.png", "Bandeiras", "Asia", "Capital: Tóquio", "Fácil"),
        ItemQuizEntity(2, "França", "https://flagcdn.com/w320/fr.png", "Bandeiras", "Europe", "Capital: Paris", "Fácil"),
        ItemQuizEntity(3, "Itália", "https://flagcdn.com/w320/it.png", "Bandeiras", "Europe", "Capital: Roma", "Médio")
    )
    QualImagemTema {
        AdminDashboardConteudo(
            estado = AdminUiState(
                itensPagina = exemplos,
                totalNoBanco = 250,
                totalFiltrado = 250,
                categorias = listOf("Bandeiras"),
                ultimaSincronizacao = System.currentTimeMillis()
            ),
            onBuscar = {}, onSelecionarCategoria = {}, onSincronizar = {}, onNovoItem = {},
            onEditarItem = {}, onExcluirItem = {}, onPaginaAnterior = {}, onProximaPagina = {}
        )
    }
}
