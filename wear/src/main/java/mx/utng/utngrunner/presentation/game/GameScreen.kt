package mx.utng.utngrunner.presentation.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import mx.utng.utngrunner.domain.model.GamePhase

/**
 * Pantalla principal del juego.
 *
 * Responsabilidades:
 * - Observar [GameViewModel.gameState] y recomponer cuando cambia
 * - Delegar el dibujo a [GameRenderer]
 * - Capturar tap (salto) y enviarlos al ViewModel
 * - Mostrar HUD con score, vidas y nivel
 * - Mostrar overlays de Game Over y Pausa según [GamePhase]
 *
 * NO contiene lógica de juego.
 */
@Composable
fun GameScreen(viewModel: GameViewModel) {
    val state by viewModel.gameState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
            .clickable { if (state.phase == GamePhase.PLAYING) viewModel.onTap() },
    ) {
        // ── Canvas del juego ─────────────────────────────────────────────────
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coords ->
                    viewModel.setScreenWidth(coords.size.width.toFloat())
                },
        ) {
            GameRenderer.draw(this, state)
        }

        // ── HUD superior — score, nivel, vidas ───────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "⭐ ${state.score}",
                    color = Color(0xFFFFD700),
                    fontSize = 13.sp,
                )
                Text(
                    text = "  Nv.${state.level}",
                    color = Color(0xFF90CAF9),
                    fontSize = 11.sp,
                )
            }
            // Vidas como corazones
            Text(
                text = "❤".repeat(state.lives) + "🖤".repeat((3 - state.lives).coerceAtLeast(0)),
                fontSize = 9.sp,
            )
        }

        // ── Overlay: Game Over ───────────────────────────────────────────────
        if (state.phase == GamePhase.DEAD) {
            GameOverOverlay(
                score     = state.score,
                highScore = state.highScore,
                onRestart = { viewModel.startGame() },
            )
        }

        // ── Overlay: Pausa ───────────────────────────────────────────────────
        if (state.phase == GamePhase.PAUSED) {
            PauseOverlay(onResume = { viewModel.togglePause() })
        }
    }

    // Al entrar a la pantalla por primera vez → arrancar el juego
    LaunchedEffect(Unit) {
        if (state.phase == GamePhase.IDLE) {
            viewModel.startGame()
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun GameOverOverlay(score: Int, highScore: Int, onRestart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("GAME OVER",     color = Color(0xFFE74C3C), fontSize = 18.sp)
            Text("Score: $score", color = Color(0xFFFFD700), fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp))
            if (score > 0 && score >= highScore) {
                Text("¡Nuevo récord! 🏆", color = Color(0xFFFFF176), fontSize = 11.sp)
            }
            Button(
                onClick = onRestart,
                modifier = Modifier.padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFD700)),
            ) {
                Text("▶ JUGAR", color = Color(0xFF0D1B2A), fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun PauseOverlay(onResume: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            .clickable { onResume() },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⏸", fontSize = 32.sp, color = Color.White)
            Text("Toca para continuar", color = Color(0xFFBDBDBD), fontSize = 10.sp,
                modifier = Modifier.padding(top = 4.dp))
        }
    }
}
