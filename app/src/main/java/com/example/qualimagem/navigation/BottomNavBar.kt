package com.example.qualimagem.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class ItemAba(
    val rota: String,
    val titulo: String,
    val iconeSelecionado: ImageVector,
    val iconeNaoSelecionado: ImageVector
) {
    object Inicio : ItemAba(
        rota = RotasAbas.ABA_INICIO,
        titulo = "Início",
        iconeSelecionado = Icons.Filled.PlayArrow,
        iconeNaoSelecionado = Icons.Outlined.PlayArrow
    )

    object Ranking : ItemAba(
        rota = RotasAbas.ABA_RANKING,
        titulo = "Ranking",
        iconeSelecionado = Icons.Filled.Leaderboard,
        iconeNaoSelecionado = Icons.Outlined.Leaderboard
    )

    object Galeria : ItemAba(
        rota = RotasAbas.ABA_GALERIA,
        titulo = "Galeria",
        iconeSelecionado = Icons.Filled.Collections,
        iconeNaoSelecionado = Icons.Outlined.Collections
    )

    object Admin : ItemAba(
        rota = RotasAbas.ABA_ADMIN,
        titulo = "Admin",
        iconeSelecionado = Icons.Filled.AdminPanelSettings,
        iconeNaoSelecionado = Icons.Outlined.AdminPanelSettings
    )
}

@Composable
fun BottomNavBar(
    navController: NavController,
    isAdmin: Boolean = false
) {
    val itens = mutableListOf(
        ItemAba.Inicio,
        ItemAba.Ranking,
        ItemAba.Galeria
    )

    if (isAdmin) {
        itens.add(ItemAba.Admin)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rotaAtual = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        itens.forEach { item ->
            val selecionado = rotaAtual == item.rota

            NavigationBarItem(
                selected = selecionado,
                onClick = {
                    if (rotaAtual != item.rota) {
                        navController.navigate(item.rota) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selecionado) item.iconeSelecionado else item.iconeNaoSelecionado,
                        contentDescription = item.titulo
                    )
                },
                label = { Text(item.titulo) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

