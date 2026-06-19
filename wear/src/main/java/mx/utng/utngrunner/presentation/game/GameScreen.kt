package mx.utng.utngrunner.presentation.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import kotlinx.coroutines.delay
import mx.utng.utngrunner.domain.model.GamePhase

@Composable
fun GameScreen(
    viewModel: GameViewModel
) {
    val state by viewModel.state.collectAsState()
    var frame by remember { mutableLongStateOf(0L) }

    // Loop de renderizado puramente visual para compatibilidad con la vista
    LaunchedEffect(state.phase) {
        while (state.phase == GamePhase.PLAYING) {
            delay(16L)
            frame++
        }
    }

    val hapticFeedback = androidx.compose.ui.platform.LocalHapticFeedback.current
    LaunchedEffect(Unit) {
        viewModel.hapticEvents.collect { type ->
            when (type) {
                mx.utng.utngrunner.presentation.game.HapticType.JUMP -> 
                    hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                mx.utng.utngrunner.presentation.game.HapticType.HIT  -> 
                    hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            }
        }
    }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onRotaryScrollEvent { event ->
                // Corona del reloj = saltar (giro hacia arriba) o deslizar (giro abajo)
                if (event.verticalScrollPixels < 0) viewModel.onJump()
                else viewModel.onSlide()
                true
            }
            .clickable { viewModel.onJump() }
    ) {
        // Canvas principal: circular en Wear OS
        Canvas(modifier = Modifier.fillMaxSize()) {
            GameRenderer.draw(this, state)
        }

        // Overlays de estado
        when (state.phase) {
            GamePhase.IDLE -> IdleOverlay(onStart = viewModel::onJump)
            GamePhase.DEAD -> GameOverOverlay(
                score     = state.score,
                highScore = state.highScore,
                onRetry   = viewModel::onJump
            )
            else -> Unit
        }
    }
}

@Composable
private fun IdleOverlay(onStart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xBB000000))
            .clickable(onClick = onStart),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "UTNG Runner",
                style = MaterialTheme.typography.title3,
                color = Color(0xFFF9A825),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Toca o gira la corona",
                style = MaterialTheme.typography.body2,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun GameOverOverlay(score: Int, highScore: Int, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable(onClick = onRetry),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("GAME OVER", color = Color(0xFFE74C3C), fontSize = 18.sp)
            Text("Score: $score", color = Color(0xFFFFD700), fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp))
            
            if (score > 0 && score >= highScore) {
                Text("¡Nuevo récord! 🏆", color = Color(0xFFFFF176), fontSize = 11.sp)
            }
            
            Spacer(Modifier.height(8.dp))
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFD700)),
            ) {
                Text("▶ JUGAR", color = Color(0xFF0D1B2A), fontSize = 13.sp)
            }
        }
    }
}
