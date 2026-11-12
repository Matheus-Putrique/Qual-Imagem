package com.example.qualimagem.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Esquema de cores único do app: o Figma só tem tema escuro.
 * Os componentes Material (barra inferior, diálogos, botões) leem daqui.
 */
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

/**
 * Tema global. A cor das barras do sistema (status e navegação) é definida
 * no MainActivity com enableEdgeToEdge, não aqui.
 */
@Composable
fun QualImagemTema(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaCores,
        typography = Typography,
        content = content
    )
}
