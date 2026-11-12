package com.example.qualimagem.ui.jogo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qualimagem.viewmodel.JogoUiState

@Composable
fun FimDeJogoConteudo(
    uiState: JogoUiState,
    onJogarNovamente: () -> Unit,
    onVoltarInicio: () -> Unit
) {
    val semVidas = uiState.vidasRestantes <= 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(CoresJogo.PrimariaFundo)
                .border(1.dp, CoresJogo.Primaria, RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = CoresJogo.Primaria, modifier = Modifier.size(48.dp))
        }

        Spacer(Modifier.height(20.dp))

        Text(
            if (semVidas) "Fim de jogo!" else "Partida concluída!",
            color = CoresJogo.Texto,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            if (semVidas) "Suas vidas acabaram, mas cada descoberta conta."
            else "Mandou bem, ${uiState.apelido}! Seu resultado já está no ranking.",
            color = CoresJogo.TextoSecundario,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        CaixaDestaque {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("PONTUAÇÃO", color = CoresJogo.Primaria, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                Text("${uiState.pontuacao} pts", color = CoresJogo.Texto, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${uiState.categoriaSelecionada} · ${uiState.dificuldadeSelecionada}",
                    color = CoresJogo.TextoSecundario,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Estatistica("Acertos", "${uiState.acertos}", Modifier.weight(1f))
            Estatistica("Rodadas", "${uiState.perguntasRespondidas}/${uiState.totalRodadas}", Modifier.weight(1f))
            Estatistica("Vidas", "${uiState.vidasRestantes.coerceAtLeast(0)}", Modifier.weight(1f))
        }

        uiState.mensagemErro?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = CoresJogo.Erro, fontSize = 13.sp)
        }

        Spacer(Modifier.height(28.dp))

        BotaoPilula(texto = "Jogar novamente", icone = Icons.Rounded.Replay, onClick = onJogarNovamente)
        Spacer(Modifier.height(12.dp))
        BotaoPilula(texto = "Voltar ao início", icone = Icons.Outlined.Home, onClick = onVoltarInicio, primario = false)
    }
}

@Composable
private fun Estatistica(titulo: String, valor: String, modifier: Modifier = Modifier) {
    CaixaDestaque(modifier = modifier) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(valor, color = CoresJogo.Texto, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(titulo, color = CoresJogo.TextoSecundario, fontSize = 12.sp)
        }
    }
}
