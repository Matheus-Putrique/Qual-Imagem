package com.example.qualimagem.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.qualimagem.data.AppDatabase
import com.example.qualimagem.data.remote.RetrofitClient
import com.example.qualimagem.data.repository.PlacarRepository
import com.example.qualimagem.data.repository.QuizRepository
import com.example.qualimagem.ui.DetalheItemScreen
import com.example.qualimagem.ui.GaleriaScreen
import com.example.qualimagem.ui.LoginScreen
import com.example.qualimagem.ui.RankingScreen
import com.example.qualimagem.ui.admin.AdminDashboardScreen
import com.example.qualimagem.ui.admin.AdminFormularioScreen
import com.example.qualimagem.ui.components.EstadoVazio
import com.example.qualimagem.ui.jogo.InicioJogoScreen
import com.example.qualimagem.ui.jogo.JogoQuizScreen
import com.example.qualimagem.viewmodel.AdminViewModel
import com.example.qualimagem.viewmodel.AdminViewModelFactory
import com.example.qualimagem.viewmodel.JogoViewModel
import com.example.qualimagem.viewmodel.JogoViewModelFactory
import com.example.qualimagem.viewmodel.RankingViewModel
import com.example.qualimagem.viewmodel.RankingViewModelFactory
import com.example.qualimagem.viewmodel.SessaoViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current.applicationContext

    val database = remember { AppDatabase.getDatabase(context) }
    val quizRepository = remember {
        QuizRepository(
            itemQuizDao = database.itemQuizDao(),
            apiService = RetrofitClient.apiService
        )
    }
    val placarRepository = remember {
        PlacarRepository(placarDao = database.placarDao())
    }

    val sessaoViewModel: SessaoViewModel = viewModel()

    val jogoViewModel: JogoViewModel = viewModel(
        factory = JogoViewModelFactory(quizRepository, placarRepository)
    )

    val rankingViewModel: RankingViewModel = viewModel(
        factory = RankingViewModelFactory(placarRepository, quizRepository)
    )

    val adminViewModel: AdminViewModel = viewModel(
        factory = AdminViewModelFactory(quizRepository)
    )

    val sessaoUiState by sessaoViewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rotaAtual = navBackStackEntry?.destination?.route
    val deveExibirBottomBar = rotaAtual in listOf(
        Rotas.INICIO_JOGO,
        Rotas.GALERIA,
        Rotas.DETALHE,
        Rotas.RANKING,
        Rotas.ADMIN_DASHBOARD
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (deveExibirBottomBar) {
                BottomNavBar(
                    navController = navController,
                    isAdmin = sessaoUiState.isAdmin
                )
            }
        }
    ) { paddingValores ->
        NavHost(
            navController = navController,
            startDestination = Rotas.LOGIN,
            modifier = Modifier.padding(paddingValores)
        ) {
            composable(Rotas.LOGIN) {
                LoginScreen(
                    viewModel = sessaoViewModel,
                    onEntrar = { isAdmin ->
                        navController.navigate(if (isAdmin) Rotas.ADMIN_DASHBOARD else Rotas.INICIO_JOGO) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Rotas.INICIO_JOGO) {
                InicioJogoScreen(
                    viewModel = jogoViewModel,
                    onIniciarJogo = {
                        navController.navigate(Rotas.QUIZ)
                    }
                )
            }

            composable(Rotas.QUIZ) {
                JogoQuizScreen(
                    viewModel = jogoViewModel,
                    onVoltarInicio = {
                        navController.popBackStack(Rotas.INICIO_JOGO, inclusive = false)
                    }
                )
            }

            composable(Rotas.RANKING) {
                RankingScreen(
                    viewModel = rankingViewModel
                )
            }

            composable(Rotas.GALERIA) {
                GaleriaScreen(
                    viewModel = rankingViewModel,
                    onAbrirDetalhe = { idItem ->
                        navController.navigate(Rotas.detalhe(idItem))
                    }
                )
            }

            composable(
                route = Rotas.DETALHE,
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getInt("id") ?: 0
                DetalheItemScreen(
                    id = itemId,
                    viewModel = rankingViewModel,
                    onVoltar = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Rotas.ADMIN_DASHBOARD) {
                if (sessaoUiState.isAdmin) {
                    AdminDashboardScreen(
                        viewModel = adminViewModel,
                        onNovoItem = { navController.navigate(Rotas.adminFormulario()) },
                        onEditarItem = { idItem -> navController.navigate(Rotas.adminFormulario(idItem)) }
                    )
                } else {
                    AcessoNegado()
                }
            }

            composable(
                route = Rotas.ADMIN_FORMULARIO,
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.IntType
                        defaultValue = Rotas.SEM_ID
                    }
                )
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getInt("id") ?: Rotas.SEM_ID
                if (sessaoUiState.isAdmin) {
                    AdminFormularioScreen(
                        id = itemId,
                        viewModel = adminViewModel,
                        onVoltar = { navController.popBackStack() }
                    )
                } else {
                    AcessoNegado()
                }
            }
        }
    }
}

@Composable
private fun AcessoNegado() {
    EstadoVazio(
        mensagem = "Acesso Negado",
        subtitulo = "Entre como Administrador para acessar esta área.",
        icone = Icons.Outlined.Lock
    )
}
