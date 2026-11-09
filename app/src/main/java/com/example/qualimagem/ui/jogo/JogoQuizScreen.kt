package com.example.qualimagem.ui.jogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.qualimagem.viewmodel.JogoViewModel

@Composable
fun JogoQuizScreen(
    viewModel: JogoViewModel,
    onVoltarInicio: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState.partidaFinalizada) {
            Text(
                text = "Fim de jogo!",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Resultado da partida")
            Text("Pontuação: ${uiState.pontuacao}")
            Text("Acertos: ${uiState.acertos}")
            Text("Perguntas respondidas: ${uiState.perguntasRespondidas}")

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.sairDaPartida()
                    onVoltarInicio()
                }
            ) {
                Text("Voltar ao início")
            }

        } else {
            Text(
                text = "Qual-Imagem",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Vidas: ${uiState.vidasRestantes}")
                Text("Pontos: ${uiState.pontuacao}")
                Text("Acertos: ${uiState.acertos}")
            }

            Spacer(modifier = Modifier.height(24.dp))

            val pergunta = uiState.perguntaAtual

            if (pergunta != null) {
                Text(
                    text = "Qual é o nome desta imagem?",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(20.dp))

                AsyncImage(
                    model = pergunta.imagemUrl,
                    contentDescription = "Imagem para identificação",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                uiState.alternativas.forEach { alternativa ->
                    val textoBotao = alternativa.nomeCorreto

                    val respostaEscolhida =
                        uiState.respostaSelecionada == textoBotao

                    val textoAlternativa = when {
                        uiState.respostaCorreta == null -> textoBotao
                        respostaEscolhida && uiState.respostaCorreta == true ->
                            "$textoBotao - Correto!"
                        respostaEscolhida && uiState.respostaCorreta == false ->
                            "$textoBotao - Incorreto!"
                        else -> textoBotao
                    }

                    OutlinedButton(
                        onClick = { viewModel.responder(alternativa) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.respostaCorreta == null
                    ) {
                        Text(textoAlternativa)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (uiState.dicaVisivel) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Dica: ${pergunta.dica}",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Text(
                    text = "Pergunta ${uiState.indicePerguntaAtual + 1} de ${uiState.perguntas.size}"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = { viewModel.mostrarDica() },
                        enabled = !uiState.dicaVisivel &&
                                uiState.respostaCorreta == null
                    ) {
                        Text("Ver dica")
                    }

                    Button(
                        onClick = { viewModel.avancarPergunta() },
                        enabled = uiState.respostaCorreta != null
                    ) {
                        Text("Próxima")
                    }
                }

            } else {
                Text("Carregando pergunta...")
            }

            uiState.mensagemErro?.let { mensagem ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = mensagem,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
