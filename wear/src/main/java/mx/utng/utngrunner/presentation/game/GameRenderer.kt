package mx.utng.utngrunner.presentation.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import mx.utng.utngrunner.domain.model.GamePhase
import mx.utng.utngrunner.domain.model.GameState
import mx.utng.utngrunner.domain.model.ObstacleType
import mx.utng.utngrunner.domain.model.Player
import kotlin.math.sin

/**
 * Renderizador del juego sobre un DrawScope de Compose.
 *
 * Responsabilidad única: convertir [GameState] en llamadas de dibujo.
 * Sin estado propio — función pura de transformación visual.
 *
 * Paleta UTNG:
 * - Fondo:      azul marino  #0D1B2A
 * - Jugador:    dorado       #FFD700
 * - Obstáculos: rojo coral   #E74C3C  (con label por tipo)
 * - Monedas:    amarillo     #FFF176  con anillo #FFB300
 * - Suelo:      azul acero   #1E3A5F
 * - HUD vidas:  corazones ❤
 */
object GameRenderer {

    // ── Paleta ────────────────────────────────────────────────────────────────
    private val BG           = Color(0xFF0D1B2A)
    private val GROUND       = Color(0xFF1E3A5F)
    private val GROUND_LINE  = Color(0xFF2980B9)
    private val PLAYER       = Color(0xFFFFD700)
    private val PLAYER_SLIDE = Color(0xFFFFC107)
    private val OBS_COLOR    = Color(0xFFE74C3C)
    private val OBS_SHADOW   = Color(0x40000000)
    private val COIN         = Color(0xFFFFF176)
    private val COIN_RING    = Color(0xFFFFB300)
    private val INVINCIBLE   = Color(0x80FFFFFF)

    /**
     * Dibuja un frame completo del juego.
     *
     * @param drawScope contexto de dibujo de Compose
     * @param state     estado del juego en este frame
     */
    fun draw(drawScope: DrawScope, state: GameState) {
        with(drawScope) {
            val floorY = Player.FLOOR_Y

            // ── Fondo ─────────────────────────────────────────────────────────
            drawRect(color = BG, size = size)

            // ── Suelo ─────────────────────────────────────────────────────────
            drawRect(
                color     = GROUND,
                topLeft   = Offset(0f, floorY + 2f),
                size      = Size(size.width, size.height - floorY),
            )
            drawLine(
                color       = GROUND_LINE,
                start       = Offset(0f, floorY + 1f),
                end         = Offset(size.width, floorY + 1f),
                strokeWidth = 2f,
            )

            // ── Monedas ───────────────────────────────────────────────────────
            state.coins.forEach { coin ->
                val coinY = coin.y + sin(coin.phase) * 4f
                drawCircle(color = COIN_RING, radius = 8f,  center = Offset(coin.x, coinY))
                drawCircle(color = COIN,      radius = 6f,  center = Offset(coin.x, coinY))
            }

            // ── Obstáculos ────────────────────────────────────────────────────
            state.obstacles.forEach { obs ->
                val obsY = floorY - obs.height
                // Sombra
                drawRect(
                    color   = OBS_SHADOW,
                    topLeft = Offset(obs.x + 3f, obsY + 3f),
                    size    = Size(obs.width.toFloat(), obs.height.toFloat()),
                )
                // Cuerpo (color varía por tipo)
                val obsColor = when (obs.type) {
                    ObstacleType.EXAMEN -> Color(0xFFE74C3C)
                    ObstacleType.TAREA  -> Color(0xFFE67E22)
                    ObstacleType.BUG    -> Color(0xFF8E44AD)
                    ObstacleType.REPO   -> Color(0xFF16A085)
                }
                drawRect(
                    color   = obsColor,
                    topLeft = Offset(obs.x, obsY),
                    size    = Size(obs.width.toFloat(), obs.height.toFloat()),
                )
                // Label del tipo sobre el obstáculo
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color     = android.graphics.Color.WHITE
                        textSize  = 10f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                    canvas.nativeCanvas.drawText(
                        obs.type.label,
                        obs.x + obs.width / 2f,
                        obsY - 3f,
                        paint,
                    )
                }
            }

            // ── Jugador ───────────────────────────────────────────────────────
            val p = state.player
            val pw = 18f
            val ph = if (p.isSliding) 14f else 28f
            val px = p.x - pw / 2f
            val py = p.y - ph

            // Halo de invencibilidad
            if (p.isInvincible && (p.invincibleFrames / 4) % 2 == 0) {
                drawRect(
                    color   = INVINCIBLE,
                    topLeft = Offset(px - 3f, py - 3f),
                    size    = Size(pw + 6f, ph + 6f),
                )
            }

            val playerColor = if (p.isSliding) PLAYER_SLIDE else PLAYER
            drawRect(
                color   = playerColor,
                topLeft = Offset(px, py),
                size    = Size(pw, ph),
            )
        }
    }
}
