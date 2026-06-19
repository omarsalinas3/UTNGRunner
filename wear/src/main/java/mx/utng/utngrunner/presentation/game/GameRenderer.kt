package mx.utng.utngrunner.presentation.game

import android.graphics.Paint as AndroidPaint
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import mx.utng.utngrunner.domain.model.Coin
import mx.utng.utngrunner.domain.model.GameState
import mx.utng.utngrunner.domain.model.Obstacle
import mx.utng.utngrunner.domain.model.ObstacleType
import mx.utng.utngrunner.domain.model.Player
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sin

/**
 * GameRenderer: SOLO dibuja. No toca la lógica de juego.
 * Cumple con SRP (Single Responsibility Principle).
 */
object GameRenderer {

    // Paleta centralizada
    private object COLORS {
        val skyStart  = Color(0xFF0D1B4A)
        val skyEnd    = Color(0xFF1A237E)
        val player    = Color(0xFFE65100)
        val helmet    = Color(0xFF1A237E)
        val ground    = Color(0xFF1E3A5F)
        val coinOuter = Color(0xFFFFB300)
        val coinInner = Color(0xFFFFF176)
        val text      = android.graphics.Color.WHITE
    }

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun draw(drawScope: DrawScope, state: GameState) {
        with(drawScope) {
            drawBackground(size)
            drawGround(size)
            drawCoins(state.coins)
            drawObstacles(state.obstacles)
            drawPlayer(state.player)
            drawHUD(size, state)
        }
    }

    private fun DrawScope.drawBackground(size: Size) {
        // Gradiente de cielo nocturno
        val brush = Brush.linearGradient(
            colors = listOf(COLORS.skyStart, COLORS.skyEnd),
            start = Offset(0f, 0f),
            end = Offset(0f, size.height)
        )
        drawRect(brush = brush, size = size)
    }

    private fun DrawScope.drawGround(size: Size) {
        drawRect(
            color = COLORS.ground,
            topLeft = Offset(0f, Player.FLOOR_Y),
            size = Size(size.width, size.height - Player.FLOOR_Y)
        )
    }

    private fun DrawScope.drawPlayer(player: Player) {
        // Parpadeo de invencibilidad (usando los invincibleFrames que calcula GameEngine)
        val alpha = if (player.isInvincible && (player.invincibleFrames / 4) % 2 == 0) 0.3f else 1f
        val yPos = player.y

        // Cuerpo del personaje
        drawRect(
            color = COLORS.player.copy(alpha = alpha),
            topLeft = Offset(player.x - 6f, yPos - 10f),
            size = Size(20f, 24f)
        )

        // Casco UTNG
        drawRect(
            color = COLORS.helmet.copy(alpha = alpha),
            topLeft = Offset(player.x - 5f, yPos - 24f),
            size = Size(18f, 10f)
        )
    }

    private fun DrawScope.drawCoins(coins: List<Coin>) {
        coins.forEach { coin ->
            // Animación visual de flotación
            val animY = coin.y + sin(coin.phase) * 4f
            drawCircle(color = COLORS.coinOuter, radius = 8f, center = Offset(coin.x, animY))
            drawCircle(color = COLORS.coinInner, radius = 6f, center = Offset(coin.x, animY))
        }
    }

    private fun DrawScope.drawObstacles(obstacles: List<Obstacle>) {
        obstacles.forEach { obs ->
            val obsY = Player.FLOOR_Y - obs.height

            // Sombra
            drawRect(
                color = Color(0x40000000),
                topLeft = Offset(obs.x + 3f, obsY + 3f),
                size = Size(obs.width.toFloat(), obs.height.toFloat())
            )

            val obsColor = when (obs.type) {
                ObstacleType.EXAMEN -> Color(0xFFE74C3C)
                ObstacleType.TAREA  -> Color(0xFFE67E22)
                ObstacleType.BUG    -> Color(0xFF8E44AD)
                ObstacleType.REPO   -> Color(0xFF16A085)
            }

            drawRect(
                color = obsColor,
                topLeft = Offset(obs.x, obsY),
                size = Size(obs.width.toFloat(), obs.height.toFloat())
            )

            // Etiqueta centrada sobre el obstáculo
            drawCenteredText(obs.type.label, obs.x + obs.width / 2f, obsY - 4f, 10f)
        }
    }

    private fun DrawScope.drawHUD(size: Size, state: GameState) {
        val cx = size.width / 2f

        // Hora del sistema en la parte superior
        val currentTime = timeFormat.format(Date())
        drawCenteredText(currentTime, cx, 30f, 14f)

        // Puntuación inferior
        drawCenteredText("${state.score} pts", cx, size.height - 20f, 12f)

        // Frecuencia cardíaca (BPM) 
        drawCenteredText("❤️ ${state.heartRate} bpm", cx, 55f, 10f, android.graphics.Color.parseColor("#FF5252"))

        // Vidas dibujadas como iconos
        val startX = cx - (state.lives * 8f) + 8f
        repeat(state.lives) { i ->
            drawCenteredText("❤", startX + (i * 16f), 80f, 12f, android.graphics.Color.RED)
        }
    }

    // --- Helpers nativos para textos limpios en Canvas ---

    private fun DrawScope.drawCenteredText(
        text: String,
        x: Float,
        y: Float,
        textSizeSp: Float,
        textColor: Int = COLORS.text
    ) {
        drawIntoCanvas { canvas ->
            val paint = AndroidPaint().apply {
                color = textColor
                textSize = textSizeSp * density
                textAlign = AndroidPaint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.nativeCanvas.drawText(text, x, y, paint)
        }
    }
}
