package com.example.qualimagem.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qualimagem.ui.jogo.CoresJogo
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CabecalhoTela(titulo: String, subtitulo: String, icone: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(titulo, color = CoresJogo.Texto, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.size(4.dp))
            Text(subtitulo, color = CoresJogo.TextoSecundario, fontSize = 14.sp)
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(CoresJogo.Superficie),
            contentAlignment = Alignment.Center
        ) {
            Icon(icone, contentDescription = null, tint = CoresJogo.Primaria)
        }
    }
}

@Composable
fun ChipFiltro(
    texto: String,
    ativo: Boolean,
    onClick: () -> Unit,
    icone: ImageVector? = null,
    habilitado: Boolean = true,
    iconeFinal: ImageVector? = null
) {
    val corConteudo = when {
        !habilitado -> CoresJogo.TextoApagado
        ativo -> CoresJogo.Primaria
        else -> CoresJogo.TextoSecundario
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (ativo) CoresJogo.PrimariaFundo else CoresJogo.Superficie)
            .border(1.dp, if (ativo) CoresJogo.Primaria else CoresJogo.Borda, RoundedCornerShape(10.dp))
            .clickable(enabled = habilitado, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icone != null) {
            Icon(icone, contentDescription = null, tint = corConteudo, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(texto, color = corConteudo, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        if (iconeFinal != null) {
            Spacer(Modifier.width(2.dp))
            Icon(iconeFinal, contentDescription = null, tint = corConteudo, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun ChipMenu(
    texto: String,
    ativo: Boolean,
    opcoes: List<Pair<String, () -> Unit>>,
    icone: ImageVector? = null
) {
    var aberto by remember { mutableStateOf(false) }

    Box {
        ChipFiltro(
            texto = texto,
            ativo = ativo,
            icone = icone,
            iconeFinal = Icons.Rounded.KeyboardArrowDown,
            onClick = { aberto = true }
        )
        DropdownMenu(
            expanded = aberto,
            onDismissRequest = { aberto = false },
            containerColor = CoresJogo.SuperficieElevada
        ) {
            opcoes.forEach { (rotulo, acao) ->
                DropdownMenuItem(
                    text = { Text(rotulo, color = CoresJogo.Texto) },
                    onClick = {
                        acao()
                        aberto = false
                    }
                )
            }
        }
    }
}

@Composable
fun TagPequena(texto: String, cor: Color, icone: ImageVector? = null, grande: Boolean = false) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(if (grande) 10.dp else 6.dp))
            .background(cor.copy(alpha = 0.12f))
            .border(1.dp, cor.copy(alpha = 0.45f), RoundedCornerShape(if (grande) 10.dp else 6.dp))
            .padding(horizontal = if (grande) 12.dp else 6.dp, vertical = if (grande) 7.dp else 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icone != null) {
            Icon(icone, contentDescription = null, tint = cor, modifier = Modifier.size(if (grande) 16.dp else 12.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(texto, color = cor, fontSize = if (grande) 13.sp else 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun RodapeInfo(texto: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Info, contentDescription = null, tint = CoresJogo.TextoSecundario, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(texto, color = CoresJogo.TextoSecundario, fontSize = 12.sp)
    }
}

fun formatarPontos(pontos: Int): String =
    NumberFormat.getIntegerInstance(Locale("pt", "BR")).format(pontos)

fun traduzirRegiao(regiao: String): String = when (regiao.lowercase()) {
    "africa" -> "África"
    "americas" -> "Américas"
    "asia" -> "Ásia"
    "europe" -> "Europa"
    "oceania" -> "Oceania"
    "antarctic", "antarctica" -> "Antártida"
    else -> regiao.ifBlank { "Não informada" }
}
