package com.example.qualimagem.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Componentes globais do app (Pessoa 5). Todos leem as cores do MaterialTheme,
 * que o Tema.kt define com a paleta escura do Figma.
 */

/** Barra superior das telas internas: seta de voltar + título, sem fundo colorido (estilo Figma). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraTopo(
    titulo: String,
    onVoltar: (() -> Unit)? = null,
    acoes: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(text = titulo, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
        },
        navigationIcon = {
            if (onVoltar != null) {
                IconButton(onClick = onVoltar) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Voltar")
                }
            }
        },
        actions = acoes,
        // O Scaffold do AppNavigation já aplica o espaço da barra de status
        windowInsets = WindowInsets(0.dp),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.primary
        )
    )
}

/**
 * Botão padrão em formato de pílula.
 * `secundario = true` gera a versão escura com borda (ex.: "Entrar como Administrador").
 */
@Composable
fun BotaoPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    carregando: Boolean = false,
    icone: ImageVector? = null,
    secundario: Boolean = false
) {
    val cores = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        enabled = habilitado && !carregando,
        shape = CircleShape,
        border = if (secundario) BorderStroke(1.dp, cores.outline) else null,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (secundario) cores.surface else cores.primary,
            contentColor = if (secundario) cores.onSurface else cores.onPrimary
        )
    ) {
        if (carregando) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = if (secundario) cores.primary else cores.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            if (icone != null) {
                Icon(icone, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(text = texto, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
    }
}

/** Card escuro com borda fina, sem sombra (no tema escuro a borda separa melhor que a sombra). */
@Composable
fun CardPadrao(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    conteudo: @Composable () -> Unit
) {
    val forma = RoundedCornerShape(16.dp)
    val cores = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val borda = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)

    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = forma, colors = cores, border = borda) {
            conteudo()
        }
    } else {
        Card(modifier = modifier.fillMaxWidth(), shape = forma, colors = cores, border = borda) {
            conteudo()
        }
    }
}

/** Tela/área sem conteúdo: ícone + mensagem + subtítulo opcional. */
@Composable
fun EstadoVazio(
    mensagem: String,
    modifier: Modifier = Modifier,
    subtitulo: String? = null,
    icone: ImageVector = Icons.Outlined.Info
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icone,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = mensagem,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        if (subtitulo != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
