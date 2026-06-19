package mx.utng.utngrunner.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import mx.utng.utngrunner.data.datasource.PreferencesDataSource
import mx.utng.utngrunner.data.repository.ScoreRepositoryImpl
import mx.utng.utngrunner.domain.usecase.GetHighScoreUseCase
import mx.utng.utngrunner.domain.usecase.SaveHighScoreUseCase
import mx.utng.utngrunner.presentation.game.GameScreen
import mx.utng.utngrunner.presentation.game.GameViewModel
import mx.utng.utngrunner.presentation.menu.MenuScreen

/**
 * Activity única del juego Wear OS.
 *
 * Responsabilidades:
 * - Ensamblar el grafo de dependencias manualmente (sin Hilt):
 *   Context → PreferencesDataSource → ScoreRepositoryImpl → Use-Cases → GameViewModel
 * - Configurar la navegación Wear OS con [SwipeDismissableNavHost]
 * - Conectar [MenuScreen] y [GameScreen]
 *
 * Arquitectura de dependencias (de afuera hacia adentro):
 * GameActivity → GameViewModel → Use-Cases → ScoreRepository ← ScoreRepositoryImpl
 *                                                             ← PreferencesDataSource
 */
class GameActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UTNGRunnerApp()
        }
    }
}

// ── Constantes de navegación ─────────────────────────────────────────────────
private object Routes {
    const val MENU = "menu"
    const val GAME = "game"
}

// ── Composable raíz ──────────────────────────────────────────────────────────
@Composable
private fun UTNGRunnerApp() {
    val navController = rememberSwipeDismissableNavController()

    // ── Ensamblado manual de dependencias extraído a GameViewModelFactory ───
    val context = androidx.compose.ui.platform.LocalContext.current
    val vmFactory = remember { mx.utng.utngrunner.presentation.game.GameViewModelFactory(context) }
    val gameViewModel: GameViewModel = viewModel(factory = vmFactory)

    // ── Navegación Wear OS ───────────────────────────────────────────────────
    SwipeDismissableNavHost(
        navController = navController,
        startDestination = Routes.MENU,
    ) {
        composable(Routes.MENU) {
            val state by gameViewModel.gameState.collectAsState()
            MenuScreen(
                highScore = state.highScore,
                onStartGame = {
                    gameViewModel.startGame()
                    navController.navigate(Routes.GAME)
                },
            )
        }

        composable(Routes.GAME) {
            GameScreen(viewModel = gameViewModel)
        }
    }
}
