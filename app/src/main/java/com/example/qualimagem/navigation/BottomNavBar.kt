package com.example.qualimagem.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SportsEsports
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
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class ItemAba(
    val rota: String,
    val titulo: String,
    val iconeSelecionado: ImageVector,
    val iconeNaoSelecionado: ImageVector
) {
    object Jogar : ItemAba(RotasAbas.ABA_JOGAR, "Jogar", Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports)
    object Galeria : ItemAba(RotasAbas.ABA_GALERIA, "Galeria", Icons.Filled.PhotoLibrary, Icons.Outlined.PhotoLibrary)
    object Ranking : ItemAba(RotasAbas.ABA_RANKING, "Ranking", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents)
    object Admin : ItemAba(RotasAbas.ABA_ADMIN, "Admin", Icons.Filled.Shield, Icons.Outlined.Shield)
}

/** Aba que fica marcada para cada rota. O Detalhe pertence à aba Galeria. */
private fun abaDaRota(rota: String?): String? = when (rota) {
    Rotas.DETALHE -> RotasAbas.ABA_GALERIA
    else -> rota
}

@Composable
fun BottomNavBar(
    navController: NavController,
    isAdmin: Boolean = false
) {
    // Ordem do Figma: Jogar · Galeria · Ranking · Admin (Admin só aparece para o administrador)
    val itens = buildList {
        add(ItemAba.Jogar)
        add(ItemAba.Galeria)
        add(ItemAba.Ranking)
        if (isAdmin) add(ItemAba.Admin)
    }

    // A primeira tela depois da entrada é a "base" das abas: voltar sempre retorna a ela
    val rotaBase = if (isAdmin) RotasAbas.ABA_ADMIN else RotasAbas.ABA_JOGAR

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val abaAtual = abaDaRota(navBackStackEntry?.destination?.route)

    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        itens.forEach { item ->
            val selecionado = abaAtual == item.rota

            NavigationBarItem(
                selected = selecionado,
                onClick = {
                    if (selecionado) {
                        // Já está nesta aba (ex.: no Detalhe da Galeria): volta para a raiz da aba
                        navController.popBackStack(item.rota, inclusive = false)
                    } else {
                        navController.navigate(item.rota) {
                            // Remove as abas abertas até a base, guardando o estado delas
                            popUpTo(rotaBase) { saveState = true }
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
