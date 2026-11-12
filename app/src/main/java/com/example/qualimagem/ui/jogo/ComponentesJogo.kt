package com.example.qualimagem.ui.jogo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.qualimagem.viewmodel.CategoriaResumo

private val CantoCard = RoundedCornerShape(16.dp)

@Composable
fun BotaoPilula(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icone: ImageVector? = null,
    habilitado: Boolean = true,
    carregando: Boolean = false,
    primario: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = habilitado && !carregando,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = CircleShape,
        border = if (primario) null else BorderStroke(1.dp, CoresJogo.Borda),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (primario) CoresJogo.Primaria else CoresJogo.Superficie,
            contentColor = if (primario) CoresJogo.NaPrimaria else CoresJogo.Texto,
            disabledContainerColor = if (primario) CoresJogo.Primaria.copy(alpha = 0.35f) else CoresJogo.Superficie,
            disabledContentColor = CoresJogo.NaPrimaria.copy(alpha = 0.6f)
        )
    ) {
        if (carregando) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = CoresJogo.NaPrimaria
            )
        } else {
            if (icone != null) {
                Icon(icone, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(texto, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
    }
}

@Composable
fun ChipOpcao(
    texto: String,
    selecionado: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selecionado) CoresJogo.PrimariaFundo else CoresJogo.Superficie)
            .border(
                1.dp,
                if (selecionado) CoresJogo.Primaria else CoresJogo.Borda,
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selecionado) {
            Icon(
                Icons.Rounded.Check,
                contentDescription = null,
                tint = CoresJogo.Primaria,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            texto,
            color = if (selecionado) CoresJogo.Primaria else CoresJogo.TextoSecundario,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CampoTextoJogo(
    valor: String,
    onValorChange: (String) -> Unit,
    placeholder: String,
    icone: ImageVector,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = valor,
        onValueChange = onValorChange,
        singleLine = true,
        textStyle = TextStyle(color = CoresJogo.Texto, fontSize = 15.sp),
        cursorBrush = SolidColor(CoresJogo.Primaria),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { campo ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CoresJogo.Superficie)
                    .border(1.dp, CoresJogo.Borda, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icone, contentDescription = null, tint = CoresJogo.TextoSecundario, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f)) {
                    if (valor.isEmpty()) {
                        Text(placeholder, color = CoresJogo.TextoApagado, fontSize = 15.sp)
                    }
                    campo()
                }
            }
        }
    )
}

@Composable
fun CardCategoria(
    categoria: CategoriaResumo,
    selecionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 150.dp)
            .clip(CantoCard)
            .background(if (selecionado) CoresJogo.PrimariaFundo else CoresJogo.Superficie)
            .border(1.dp, if (selecionado) CoresJogo.Primaria else CoresJogo.Borda, CantoCard)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            AsyncImage(
                model = categoria.imagemCapaUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(56.dp)
                    .height(38.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CoresJogo.SuperficieElevada)
            )
            Spacer(Modifier.weight(1f))
            if (selecionado) {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = "Selecionada",
                    tint = CoresJogo.Primaria,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))
        Text(
            categoria.nome,
            color = CoresJogo.Texto,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            textoQuantidade(categoria),
            color = CoresJogo.Primaria,
            fontSize = 12.sp
        )
    }
}

private fun textoQuantidade(categoria: CategoriaResumo): String {
    val unidade = if (categoria.nome.equals("Bandeiras", ignoreCase = true)) "países" else "imagens"
    return "${categoria.quantidadeItens} $unidade para explorar"
}

@Composable
fun CardEmBreve(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .heightIn(min = 150.dp)
            .clip(CantoCard)
            .background(CoresJogo.Superficie.copy(alpha = 0.6f))
            .border(1.dp, CoresJogo.Borda, CantoCard)
            .padding(14.dp)
    ) {
        Icon(Icons.Outlined.Lock, contentDescription = null, tint = CoresJogo.TextoSecundario, modifier = Modifier.size(28.dp))
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))
        Text("Em Breve", color = CoresJogo.TextoSecundario, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Text("Novos temas a caminho", color = CoresJogo.TextoApagado, fontSize = 12.sp)
    }
}

enum class EstadoAlternativa { NEUTRA, CORRETA, ERRADA, DESATIVADA }

@Composable
fun CardAlternativa(
    letra: Char,
    texto: String,
    estado: EstadoAlternativa,
    onClick: () -> Unit
) {
    val (fundo, borda, corTexto) = when (estado) {
        EstadoAlternativa.CORRETA -> Triple(CoresJogo.AcertoFundo, CoresJogo.Acerto, CoresJogo.Texto)
        EstadoAlternativa.ERRADA -> Triple(CoresJogo.ErroFundo, CoresJogo.Erro, CoresJogo.Texto)
        EstadoAlternativa.DESATIVADA -> Triple(CoresJogo.Superficie, CoresJogo.Borda, CoresJogo.TextoApagado)
        EstadoAlternativa.NEUTRA -> Triple(CoresJogo.Superficie, CoresJogo.Borda, CoresJogo.Texto)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(fundo)
            .border(if (estado == EstadoAlternativa.NEUTRA) 1.dp else 1.5.dp, borda, RoundedCornerShape(12.dp))
            .clickable(enabled = estado == EstadoAlternativa.NEUTRA, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            letra.toString(),
            color = if (estado == EstadoAlternativa.CORRETA) CoresJogo.Acerto else CoresJogo.TextoSecundario,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.width(16.dp))
        Text(
            texto,
            color = corTexto,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
        when (estado) {
            EstadoAlternativa.CORRETA ->
                Icon(Icons.Outlined.CheckCircle, contentDescription = "Correta", tint = CoresJogo.Acerto, modifier = Modifier.size(20.dp))
            EstadoAlternativa.ERRADA ->
                Icon(Icons.Outlined.Cancel, contentDescription = "Errada", tint = CoresJogo.Erro, modifier = Modifier.size(20.dp))
            else -> Unit
        }
    }
}

@Composable
fun IndicadorVidas(vidasRestantes: Int, vidasTotais: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(vidasTotais) { indice ->
            val cheio = indice < vidasRestantes
            Icon(
                imageVector = if (cheio) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = if (cheio) CoresJogo.Erro else CoresJogo.Borda,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun BarraProgressoRodadas(progresso: Float, modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        progress = { progresso },
        modifier = modifier
            .height(5.dp)
            .clip(CircleShape),
        color = CoresJogo.Primaria,
        trackColor = CoresJogo.SuperficieElevada,
        strokeCap = StrokeCap.Round,
        gapSize = 0.dp,
        drawStopIndicator = {}
    )
}

@Composable
fun CaixaDestaque(
    modifier: Modifier = Modifier,
    fundo: Color = CoresJogo.Superficie,
    borda: Color = CoresJogo.Borda,
    paddingInterno: PaddingValues = PaddingValues(16.dp),
    conteudo: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CantoCard)
            .background(fundo)
            .border(1.dp, borda, CantoCard)
            .padding(paddingInterno)
    ) {
        conteudo()
    }
}
