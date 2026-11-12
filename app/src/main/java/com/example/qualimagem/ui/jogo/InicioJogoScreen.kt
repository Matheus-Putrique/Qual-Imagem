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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CenterFocusWeak
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qualimagem.viewmodel.JogoViewModel

/**
 * Tela "Bora descobrir?": o jogador confirma o apelido,
 * escolhe a categoria (lida do banco) e o nível, e inicia a partida.
 */
@Composable
fun InicioJogoScreen(
    viewModel: JogoViewModel,
    apelidoInicial: String,
    onIniciarJogo: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Reaproveita o apelido digitado no login
    LaunchedEffect(apelidoInicial) {
        viewModel.definirApelidoInicial(apelidoInicial)
    }

    // Navega só uma vez quando o ViewModel prepara a partida
    LaunchedEffect(uiState.navegarParaQuiz) {
        if (uiState.navegarParaQuiz) {
            viewModel.navegacaoRealizada()
            onIniciarJogo()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CoresJogo.Fundo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // ---- Cabeçalho ----
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Bora descobrir?", color = CoresJogo.Texto, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Escolha um desafio e mostre o que sabe.", color = CoresJogo.TextoSecundario, fontSize = 14.sp)
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CoresJogo.Superficie),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.CenterFocusWeak, contentDescription = null, tint = CoresJogo.Primaria)
            }
        }

        Spacer(Modifier.height(20.dp))

        // ---- Banner com as regras ----
        CaixaDestaque {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Public, contentDescription = null, tint = CoresJogo.Primaria, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("O mundo é o seu desafio", color = CoresJogo.Texto, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text(
                        "${JogoViewModel.TOTAL_RODADAS} rodadas · ${JogoViewModel.VIDAS_INICIAIS} vidas · muitas descobertas",
                        color = CoresJogo.TextoSecundario,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ---- Apelido ----
        Text("Seu apelido", color = CoresJogo.TextoSecundario, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        CampoTextoJogo(
            valor = uiState.apelido,
            onValorChange = viewModel::alterarApelido,
            placeholder = "Como podemos te chamar?",
            icone = Icons.Outlined.Person
        )

        Spacer(Modifier.height(24.dp))

        // ---- Categorias ----
        Text("Escolha a categoria", color = CoresJogo.Texto, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))

        if (uiState.categorias.isEmpty()) {
            BancoVazio(
                sincronizando = uiState.sincronizando,
                onBaixar = viewModel::sincronizarBandeiras
            )
        } else {
            // Grade de 2 colunas: categorias do banco + o card "Em Breve" no final.
            // O null no fim da lista representa o card "Em Breve".
            val itensGrade = uiState.categorias + null

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                itensGrade.chunked(2).forEach { linha ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        linha.forEach { categoria ->
                            if (categoria == null) {
                                CardEmBreve(Modifier.weight(1f))
                            } else {
                                CardCategoria(
                                    categoria = categoria,
                                    selecionado = categoria.nome == uiState.categoriaSelecionada,
                                    onClick = { viewModel.selecionarCategoria(categoria.nome) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        if (linha.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ---- Nível ----
        Text("Qual vai ser o nível?", color = CoresJogo.Texto, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            JogoViewModel.DIFICULDADES.forEach { nivel ->
                ChipOpcao(
                    texto = nivel,
                    selecionado = nivel == uiState.dificuldadeSelecionada,
                    onClick = { viewModel.selecionarDificuldade(nivel) }
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        uiState.mensagemErro?.let { erro ->
            Text(
                erro,
                color = CoresJogo.Erro,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
        }

        // ---- Iniciar ----
        BotaoPilula(
            texto = "Iniciar Partida",
            icone = Icons.Rounded.PlayArrow,
            onClick = { viewModel.iniciarPartida() },
            habilitado = uiState.podeIniciar,
            carregando = uiState.carregando
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Seu próximo recorde começa aqui.",
            color = CoresJogo.TextoApagado,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Mostrado quando o Room ainda não tem nenhum item: permite baixar as bandeiras da API. */
@Composable
private fun BancoVazio(sincronizando: Boolean, onBaixar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CoresJogo.Superficie)
            .border(1.dp, CoresJogo.Borda, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Nenhuma categoria no aparelho ainda.",
            color = CoresJogo.Texto,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Baixe as bandeiras para começar a jogar.",
            color = CoresJogo.TextoSecundario,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(14.dp))
        BotaoPilula(
            texto = "Baixar bandeiras",
            icone = Icons.Outlined.CloudDownload,
            onClick = onBaixar,
            carregando = sincronizando,
            primario = false
        )
    }
}
