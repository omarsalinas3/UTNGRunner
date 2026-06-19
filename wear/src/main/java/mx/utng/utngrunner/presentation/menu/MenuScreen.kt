package mx.utng.utngrunner.presentation.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text

/**
 * Pantalla del menú principal del juego.
 *
 * Muestra el nombre del juego y el botón para iniciar la partida.
 * No tiene estado propio: todo lo delega a la función [onStartGame].
 *
 * @param highScore mejor puntaje histórico para mostrar en el menú
 * @param onStartGame callback que navega a [GameScreen]
 */
@Composable
fun MenuScreen(
    highScore: Int = 0,
    onStartGame: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // ── Logo / Título ────────────────────────────────────────────────────
        Text(
            text = "🏃",
            fontSize = 36.sp,
        )
        Text(
            text = "UTNG",
            color = Color(0xFFFFD700),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Runner",
            color = Color(0xFFECF0F1),
            fontSize = 16.sp,
        )

        // ── Récord histórico ─────────────────────────────────────────────────
        if (highScore > 0) {
            Text(
                text = "🏆 $highScore",
                color = Color(0xFFBDBDBD),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        // ── Botón Jugar ──────────────────────────────────────────────────────
        Button(
            onClick = onStartGame,
            modifier = Modifier.padding(top = 12.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFFFFD700),
            ),
        ) {
            Text(
                text = "JUGAR",
                color = Color(0xFF0D1B2A),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
        }
    }
}
