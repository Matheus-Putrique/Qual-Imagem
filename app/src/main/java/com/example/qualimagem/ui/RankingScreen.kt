package com.example.qualimagem.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qualimagem.data.PlacarEntity
import com.example.qualimagem.ui.jogo.CampoTextoJogo
import com.example.qualimagem.ui.jogo.CoresJogo
import com.example.qualimagem.viewmodel.PeriodoRanking
import com.example.qualimagem.viewmodel.RankingViewModel

private val Ouro = Color(0xFFF5B83D)
private val Prata = Color(0xFFCBD5E1)
private val Bronze = Color(0xFFD08A4E)

@Composable
fun RankingScreen(viewModel: RankingViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    var placarParaEditar by remember { mutableStateOf<PlacarEntity?>(null) }
    var placarParaApagar by remember { mutableStateOf<PlacarEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CoresJogo.Fundo),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            CabecalhoTela(
                titulo = "Hall da fama",
                subtitulo = "Grandes descobertas, grandes pontuações.",
                icone = Icons.Outlined.EmojiEvents
            )
        }

        item {
            Spacer(Modifier.height(8.dp))
            CampoTextoJogo(
                valor = uiState.busca,
                onValorChange = viewModel::alterarBusca,
                placeholder = "Buscar jogador...",
                icone = Icons.Outlined.Search
            )
        }

        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChipMenu(
                    texto = uiState.categoriaSelecionada.ifBlank { "Categorias" },
                    ativo = uiState.categoriaSelecionada.isNotBlank(),
                    icone = Icons.Outlined.Flag,
                    opcoes = listOf("Todas as categorias" to { viewModel.selecionarCategoria("") }) +
                        uiState.categorias.map { categoria -> categoria to { viewModel.selecionarCategoria(categoria) } }
                )
                ChipMenu(
                    texto = uiState.dificuldadeSelecionada.ifBlank { "Todos os níveis" },
                    ativo = uiState.dificuldadeSelecionada.isNotBlank(),
                    opcoes = listOf("Todos os níveis" to { viewModel.selecionarDificuldade("") }) +
                        uiState.dificuldades.map { nivel -> nivel to { viewModel.selecionarDificuldade(nivel) } }
                )
                ChipMenu(
                    texto = uiState.periodoSelecionado.rotulo,
                    ativo = uiState.periodoSelecionado != PeriodoRanking.GERAL,
                    opcoes = PeriodoRanking.entries.map { periodo -> periodo.rotulo to { viewModel.selecionarPeriodo(periodo) } }
                )
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Melhores pontuações", color = CoresJogo.TextoSecundario, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text(
                    "${uiState.totalJogadores} ${if (uiState.totalJogadores == 1) "jogador" else "jogadores"}",
                    color = CoresJogo.Primaria,
                    fontSize = 12.sp
                )
            }
        }

        uiState.mensagemErro?.let { erro ->
            item { Text(erro, color = CoresJogo.Erro, fontSize = 13.sp) }
        }

        if (uiState.placares.isEmpty()) {
            item { RankingVazio() }
        }

        itemsIndexed(uiState.placares, key = { _, placar -> placar.id }) { indice, placar ->
            CardRanking(
                posicao = indice + 1,
                placar = placar,
                onEditar = { placarParaEditar = placar },
                onApagar = { placarParaApagar = placar }
            )
        }

        item {
            RodapeInfo(
                "Cada descoberta conta. Supere seu recorde!",
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    placarParaEditar?.let { placar ->
        var novoApelido by remember(placar.id) { mutableStateOf(placar.nomeJogador) }
        AlertDialog(
            onDismissRequest = { placarParaEditar = null },
            containerColor = CoresJogo.Superficie,
            title = { Text("Editar apelido", color = CoresJogo.Texto) },
            text = {
                CampoTextoJogo(
                    valor = novoApelido,
                    onValorChange = { novoApelido = it },
                    placeholder = "Novo apelido",
                    icone = Icons.Outlined.Edit
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.editarApelido(placar, novoApelido)
                        placarParaEditar = null
                    },
                    enabled = novoApelido.isNotBlank()
                ) { Text("Salvar", color = CoresJogo.Primaria) }
            },
            dismissButton = {
                TextButton(onClick = { placarParaEditar = null }) { Text("Cancelar", color = CoresJogo.TextoSecundario) }
            }
        )
    }

    placarParaApagar?.let { placar ->
        AlertDialog(
            onDismissRequest = { placarParaApagar = null },
            containerColor = CoresJogo.Superficie,
            title = { Text("Apagar registro?", color = CoresJogo.Texto) },
            text = {
                Text(
                    "A pontuação de ${placar.nomeJogador} (${formatarPontos(placar.pontuacao)} pts) será removida do ranking.",
                    color = CoresJogo.TextoSecundario
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.apagarRegistro(placar)
                    placarParaApagar = null
                }) { Text("Apagar", color = CoresJogo.Erro) }
            },
            dismissButton = {
                TextButton(onClick = { placarParaApagar = null }) { Text("Cancelar", color = CoresJogo.TextoSecundario) }
            }
        )
    }
}

@Composable
private fun CardRanking(
    posicao: Int,
    placar: PlacarEntity,
    onEditar: () -> Unit,
    onApagar: () -> Unit
) {
    val corMedalha = when (posicao) {
        1 -> Ouro
        2 -> Prata
        3 -> Bronze
        else -> null
    }
    val primeiro = posicao == 1
    val forma = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(if (primeiro) CoresJogo.DicaFundo else CoresJogo.Superficie)
            .border(if (primeiro) 1.5.dp else 1.dp, if (primeiro) Ouro else CoresJogo.Borda, forma)
            .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(36.dp)) {
            if (corMedalha != null) {
                Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = corMedalha, modifier = Modifier.size(20.dp))
            }
            Text(
                "#$posicao",
                color = corMedalha ?: CoresJogo.TextoSecundario,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                placar.nomeJogador,
                color = CoresJogo.Texto,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TagPequena(placar.categoriaJogada, CoresJogo.Primaria)
                Spacer(Modifier.width(6.dp))
                Text(
                    "${placar.acertos} ${if (placar.acertos == 1) "acerto" else "acertos"} · ${placar.dificuldade}",
                    color = CoresJogo.TextoSecundario,
                    fontSize = 11.sp
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${formatarPontos(placar.pontuacao)} pts",
                color = if (primeiro) Ouro else CoresJogo.Texto,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            Row {
                IconButton(onClick = onEditar, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Editar apelido", tint = CoresJogo.Primaria, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onApagar, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Apagar registro", tint = CoresJogo.Erro, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun RankingVazio() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Outlined.Leaderboard, contentDescription = null, tint = CoresJogo.TextoApagado, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp))
        Text("Nenhuma pontuação por aqui", color = CoresJogo.Texto, fontWeight = FontWeight.SemiBold)
        Text(
            "Jogue uma partida ou mude os filtros.",
            color = CoresJogo.TextoSecundario,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}
