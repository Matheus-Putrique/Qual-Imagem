package com.example.qualimagem.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CropFree
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qualimagem.ui.components.BotaoPrincipal
import com.example.qualimagem.viewmodel.SessaoViewModel

@Composable
fun LoginScreen(
    viewModel: SessaoViewModel,
    onEntrar: (isAdmin: Boolean) -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.logout()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(1f))

        IlustracaoLogo()

        Spacer(Modifier.height(40.dp))

        Text(
            text = "Qual é a Imagem?",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Um mundo de imagens.\nQuantas você conhece?",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.weight(1f))

        BotaoPrincipal(
            texto = "Entrar como Usuário",
            icone = Icons.Outlined.Person,
            onClick = {
                viewModel.entrarComoJogador()
                onEntrar(false)
            }
        )
        Spacer(Modifier.height(14.dp))
        BotaoPrincipal(
            texto = "Entrar como Administrador",
            icone = Icons.Outlined.Shield,
            secundario = true,
            onClick = {
                viewModel.entrarComoAdmin()
                onEntrar(true)
            }
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun IlustracaoLogo() {
    val cores = MaterialTheme.colorScheme
    Box(modifier = Modifier.size(width = 200.dp, height = 150.dp), contentAlignment = Alignment.Center) {
        BandeiraDesenhada(
            modifier = Modifier
                .offset(x = (-52).dp, y = 30.dp)
                .rotate(-10f)
        ) { tamanho ->
            val faixa = tamanho.width / 3
            drawRect(Color(0xFF1E3A8A), size = Size(faixa, tamanho.height))
            drawRect(Color.White, topLeft = Offset(faixa, 0f), size = Size(faixa, tamanho.height))
            drawRect(Color(0xFFDC2626), topLeft = Offset(faixa * 2, 0f), size = Size(faixa, tamanho.height))
        }
        BandeiraDesenhada(
            modifier = Modifier
                .offset(x = 52.dp, y = 38.dp)
                .rotate(8f)
        ) { tamanho ->
            drawRect(Color.White, size = tamanho)
            drawCircle(Color(0xFFBC002D), radius = tamanho.height * 0.3f)
        }
        Box(
            modifier = Modifier
                .size(108.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(cores.primaryContainer)
                .border(1.5.dp, cores.primary, RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.CropFree, contentDescription = null, tint = cores.primary, modifier = Modifier.size(56.dp))
            Icon(Icons.Outlined.Visibility, contentDescription = null, tint = cores.primary, modifier = Modifier.size(26.dp))
        }
    }
}

@Composable
private fun BandeiraDesenhada(
    modifier: Modifier,
    desenho: androidx.compose.ui.graphics.drawscope.DrawScope.(Size) -> Unit
) {
    Canvas(
        modifier = modifier
            .size(width = 64.dp, height = 44.dp)
            .clip(RoundedCornerShape(4.dp))
    ) {
        desenho(size)
    }
}
