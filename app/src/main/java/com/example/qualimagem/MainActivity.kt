package com.example.qualimagem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.qualimagem.navigation.AppNavigation
import com.example.qualimagem.ui.theme.QualImagemTema

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QualImagemTema {
                AppNavigation()
            }
        }
    }
}