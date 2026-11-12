package com.example.qualimagem.ui.jogo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.HeartBroken
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.qualimagem.data.ItemQuizEntity
import com.example.qualimagem.viewmodel.JogoUiState
import com.example.qualimagem.viewmodel.JogoViewModel

@Composable
fun JogoQuizScreen(
    viewModel: JogoViewModel,
    onVoltarInicio: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val sair = {
        viewModel.sairDaPartida()
        onVoltarInicio()
    }

    BackHandler(onBack = sair)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CoresJogo.Fundo)
    ) {
        val pergunta = uiState.perguntaAtual
        when {
            uiState.partidaFinalizada -> FimDeJogoConteudo(
                uiState = uiState,
                onJogarNovamente = viewModel::jogarNovamente,
                onVoltarInicio = sair
            )

            pergunta != null -> RodadaConteudo(
                uiState = uiState,
                pergunta = pergunta,
                onResponder = viewModel::responder,
                onMostrarDica = viewModel::mostrarDica,
                onVoltar = sair
            )

            else -> CircularProgressIndicator(
                color = CoresJogo.Primaria,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun RodadaConteudo(
    uiState: JogoUiState,
    pergunta: ItemQuizEntity,
    onResponder: (ItemQuizEntity) -> Unit,
    onMostrarDica: () -> Unit,
    onVoltar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Sair da partida", tint = CoresJogo.Texto)
            }
            Text(
                "Rodada ${uiState.rodadaAtual}/${uiState.totalRodadas}",
                color = CoresJogo.Texto,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            IndicadorVidas(
                vidasRestantes = uiState.vidasRestantes,
                vidasTotais = JogoViewModel.VIDAS_INICIAIS
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
            BarraProgressoRodadas(progresso = uiState.progresso, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(14.dp))
            Icon(Icons.Rounded.Bolt, contentDescription = null, tint = CoresJogo.Primaria, modifier = Modifier.size(18.dp))
            Text("${uiState.pontuacao} pts", color = CoresJogo.Primaria, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "${uiState.categoriaSelecionada} · ${uiState.dificuldadeSelecionada}".uppercase(),
            color = CoresJogo.Primaria,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            textoPergunta(uiState.categoriaSelecionada),
            color = CoresJogo.Texto,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 30.sp
        )

        Spacer(Modifier.height(14.dp))

        CaixaDestaque {
            AsyncImage(
                model = pergunta.imagemUrl,
                contentDescription = "Imagem da rodada",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        if (uiState.dicaVisivel) {
            CaixaDestaque(fundo = CoresJogo.DicaFundo, borda = CoresJogo.Dica.copy(alpha = 0.5f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = CoresJogo.Dica)
                    Spacer(Modifier.width(10.dp))
                    Text(pergunta.dica, color = CoresJogo.Texto, fontWeight = FontWeight.Medium)
                }
            }
        } else {
            BotaoVerDica(
                habilitado = !uiState.respondida,
                onClick = onMostrarDica,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            uiState.alternativas.forEachIndexed { indice, alternativa ->
                CardAlternativa(
                    letra = 'A' + indice,
                    texto = alternativa.nomeCorreto,
                    estado = estadoDaAlternativa(uiState, pergunta, alternativa),
                    onClick = { onResponder(alternativa) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        when (uiState.respostaCorreta) {
            true -> Feedback(
                texto = "Boa! Resposta correta. +${uiState.pontosUltimaResposta} pts",
                acerto = true
            )
            false -> Feedback(
                texto = "Era ${pergunta.nomeCorreto}. Você perdeu uma vida.",
                acerto = false
            )
            null -> Unit
        }
    }
}

private fun estadoDaAlternativa(
    uiState: JogoUiState,
    pergunta: ItemQuizEntity,
    alternativa: ItemQuizEntity
): EstadoAlternativa = when {
    !uiState.respondida -> EstadoAlternativa.NEUTRA
    alternativa.id == pergunta.id -> EstadoAlternativa.CORRETA
    alternativa.id == uiState.respostaSelecionada?.id -> EstadoAlternativa.ERRADA
    else -> EstadoAlternativa.DESATIVADA
}

private fun textoPergunta(categoria: String): String =
    if (categoria.equals("Bandeiras", ignoreCase = true)) {
        "De qual país é\nesta bandeira?"
    } else {
        "Qual é o nome\ndesta imagem?"
    }

@Composable
private fun BotaoVerDica(habilitado: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val cor = if (habilitado) CoresJogo.Dica else CoresJogo.TextoApagado
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(CoresJogo.DicaFundo)
            .border(1.dp, cor, CircleShape)
            .clickable(enabled = habilitado, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = cor, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text("Ver Dica", color = cor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
private fun Feedback(texto: String, acerto: Boolean) {
    val cor = if (acerto) CoresJogo.Acerto else CoresJogo.Erro
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (acerto) Icons.Outlined.AutoAwesome else Icons.Outlined.HeartBroken,
            contentDescription = null,
            tint = cor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(texto, color = cor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}
