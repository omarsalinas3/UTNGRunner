package mx.utng.utngrunner.presentation.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text

/**
 * Pantalla principal del juego.
 *
 * Responsabilidades de esta capa (Presentación):
 * - Observar [GameViewModel.gameState] y recomponer cuando cambia
 * - Delegar el dibujo a [GameRenderer]
 * - Capturar inputs del usuario (tap = salto) y enviarlos al ViewModel
 * - Mostrar overlay de Game Over con el score final
 *
 * NO contiene lógica de juego — eso es trabajo del GameEngine y ViewModel.
 */
@Composable
fun GameScreen(viewModel: GameViewModel) {
    val state by viewModel.gameState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
            .clickable { if (state.isRunning) viewModel.onTap() },
    ) {
        // ── Canvas del juego ────────────────────────────────────────────────
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coords ->
                    viewModel.setScreenWidth(coords.size.width.toFloat())
                },
        ) {
            GameRenderer.draw(this, state)
        }

        // ── HUD: puntuación ─────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "⭐ ${state.score}",
                color = Color(0xFFFFD700),
                fontSize = 14.sp,
            )
            if (state.highScore > 0) {
                Text(
                    text = "🏆 ${state.highScore}",
                    color = Color(0xFFBDBDBD),
                    fontSize = 11.sp,
                )
            }
        }

        // ── Overlay: Game Over ──────────────────────────────────────────────
        if (state.isGameOver) {
            GameOverOverlay(
                score = state.score,
                highScore = state.highScore,
                onRestart = { viewModel.startGame() },
            )
        }

        // ── Overlay: Pausa ──────────────────────────────────────────────────
        if (!state.isRunning && !state.isGameOver) {
            PauseIndicator()
        }
    }

    // Al entrar a la pantalla, arrancar el juego
    LaunchedEffect(Unit) {
        if (!state.isRunning && !state.isGameOver) {
            viewModel.startGame()
        }
    }
}

// ── Sub-composables privados ─────────────────────────────────────────────────

@Composable
private fun GameOverOverlay(
    score: Int,
    highScore: Int,
    onRestart: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "GAME OVER",
                color = Color(0xFFE74C3C),
                fontSize = 18.sp,
            )
            Text(
                text = "Score: $score",
                color = Color(0xFFFFD700),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
            if (score >= highScore && score > 0) {
                Text(
                    text = "¡Nuevo récord!",
                    color = Color(0xFFFFF176),
                    fontSize = 11.sp,
                )
            }
            Button(
                onClick = onRestart,
                modifier = Modifier.padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFFFFD700),
                ),
            ) {
                Text(text = "▶", color = Color(0xFF0D1B2A), fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun PauseIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "⏸", fontSize = 32.sp, color = Color.White)
    }
}
