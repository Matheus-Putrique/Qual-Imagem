package com.example.qualimagem.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val AzulPrimario = Color(0xFF1E3A8A)
val AzulPrimarioClaro = Color(0xFF3B82F6)
val AmbarSecundario = Color(0xFFF59E0B)
val EsmeraldaAcerto = Color(0xFF10B981)
val VermelhoErro = Color(0xFFEF4444)
val FundoEscuro = Color(0xFF0F172A)
val SuperficieEscura = Color(0xFF1E293B)
val FundoClaro = Color(0xFFF8FAFC)
val SuperficieClara = Color(0xFFFFFFFF)

private val EsquemaCoresEscuro = darkColorScheme(
    primary = AzulPrimarioClaro,
    onPrimary = Color.White,
    secondary = AmbarSecundario,
    onSecondary = Color.Black,
    tertiary = EsmeraldaAcerto,
    background = FundoEscuro,
    surface = SuperficieEscura,
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    error = VermelhoErro
)

private val EsquemaCoresClaro = lightColorScheme(
    primary = AzulPrimario,
    onPrimary = Color.White,
    secondary = AmbarSecundario,
    onSecondary = Color.Black,
    tertiary = EsmeraldaAcerto,
    background = FundoClaro,
    surface = SuperficieClara,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    error = VermelhoErro
)

@Composable
fun QualImagemTema(
    temaEscuro: Boolean = isSystemInDarkTheme(),
    corDinamica: Boolean = false,
    content: @Composable () -> Unit
) {
    val esquemaCores = when {
        corDinamica && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val contexto = LocalContext.current
            if (temaEscuro) dynamicDarkColorScheme(contexto) else dynamicLightColorScheme(contexto)
        }

        temaEscuro -> EsquemaCoresEscuro
        else -> EsquemaCoresClaro
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = esquemaCores.primary.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !temaEscuro
            }
        }
    }

    MaterialTheme(
        colorScheme = esquemaCores,
        typography = Typography,
        content = content
    )
}

