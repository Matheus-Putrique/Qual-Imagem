package com.example.qualimagem.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val EsquemaCores = darkColorScheme(
    primary = CianoPrimario,
    onPrimary = CianoTexto,
    primaryContainer = CianoFundo,
    onPrimaryContainer = CianoPrimario,
    secondary = AmbarDica,
    onSecondary = CianoTexto,
    tertiary = VerdeAcerto,
    background = AzulMarinhoFundo,
    onBackground = TextoClaro,
    surface = AzulSuperficie,
    onSurface = TextoClaro,
    surfaceVariant = AzulSuperficieElevada,
    onSurfaceVariant = TextoCinza,
    surfaceContainer = AzulBarraInferior,
    outline = AzulBorda,
    error = VermelhoErro
)

@Composable
fun QualImagemTema(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaCores,
        typography = Typography,
        content = content
    )
}
