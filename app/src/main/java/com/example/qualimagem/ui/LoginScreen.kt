package com.example.qualimagem.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.qualimagem.ui.components.BotaoPrincipal
import com.example.qualimagem.ui.components.CardPadrao
import com.example.qualimagem.viewmodel.SessaoViewModel

@Composable
fun LoginScreen(
    viewModel: SessaoViewModel,
    onLoginSucesso: (isAdmin: Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var abaSelecionada by remember { mutableIntStateOf(0) }
    var nickname by remember { mutableStateOf("") }
    var usuarioAdmin by remember { mutableStateOf("") }
    var senhaAdmin by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.usuarioLogado) {
        if (uiState.usuarioLogado) {
            onLoginSucesso(uiState.isAdmin)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SportsEsports,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Qual é a Imagem?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Teste seus conhecimentos em bandeiras e muito mais",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        TabRow(
            selectedTabIndex = abaSelecionada,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = abaSelecionada == 0,
                onClick = {
                    abaSelecionada = 0
                    viewModel.limparErro()
                },
                text = { Text("Jogador") },
                icon = { Icon(Icons.Default.Person, contentDescription = null) }
            )
            Tab(
                selected = abaSelecionada == 1,
                onClick = {
                    abaSelecionada = 1
                    viewModel.limparErro()
                },
                text = { Text("Administrador") },
                icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        CardPadrao {
            Column(modifier = Modifier.padding(20.dp)) {
                if (abaSelecionada == 0) {
                    Text(
                        text = "Entrar como Jogador",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Escolha seu apelido para registrar sua pontuação no ranking.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = nickname,
                        onValueChange = {
                            nickname = it
                            viewModel.limparErro()
                        },
                        label = { Text("Apelido / Nickname") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    BotaoPrincipal(
                        texto = "Entrar no Jogo",
                        onClick = { viewModel.loginJogador(nickname) },
                        habilitado = nickname.isNotBlank()
                    )

                } else {
                    Text(
                        text = "Painel Administrativo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Acesso restrito para sincronização de API e gerenciamento.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = usuarioAdmin,
                        onValueChange = {
                            usuarioAdmin = it
                            viewModel.limparErro()
                        },
                        label = { Text("Usuário") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = senhaAdmin,
                        onValueChange = {
                            senhaAdmin = it
                            viewModel.limparErro()
                        },
                        label = { Text("Senha") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                                Icon(
                                    imageVector = if (senhaVisivel) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (senhaVisivel) "Ocultar senha" else "Mostrar senha"
                                )
                            }
                        },
                        visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    BotaoPrincipal(
                        texto = "Entrar como Administrador",
                        onClick = { viewModel.loginAdmin(usuarioAdmin, senhaAdmin) },
                        habilitado = usuarioAdmin.isNotBlank() && senhaAdmin.isNotBlank()
                    )
                }

                uiState.mensagemErro?.let { erro ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = erro,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

