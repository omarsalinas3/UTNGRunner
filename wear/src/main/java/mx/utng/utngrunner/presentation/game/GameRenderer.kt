package mx.utng.utngrunner.presentation.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import mx.utng.utngrunner.domain.model.GameState

/**
 * Renderizador del juego sobre un Canvas de Compose.
 *
 * Responsabilidad única: convertir [GameState] en llamadas de dibujo.
 * No tiene estado propio — es un objeto puro de transformación visual.
 *
 * Paleta de colores inspirada en el estilo UTNG:
 * - Fondo: azul oscuro marino
 * - Jugador: amarillo dorado UTNG
 * - Obstáculos: rojo coral
 * - Monedas: amarillo brillante
 * - Suelo: gris azulado
 */
object GameRenderer {

    // ── Paleta ────────────────────────────────────────────────────────────────
    private val COLOR_BG         = Color(0xFF0D1B2A)
    private val COLOR_GROUND     = Color(0xFF1E3A5F)
    private val COLOR_PLAYER     = Color(0xFFFFD700)
    private val COLOR_OBSTACLE   = Color(0xFFE74C3C)
    private val COLOR_COIN       = Color(0xFFFFF176)
    private val COLOR_COIN_RING  = Color(0xFFFFB300)
    private val COLOR_TEXT       = Color(0xFFECF0F1)
    private val COLOR_SCORE_BG   = Color(0x80000000)

    private const val GROUND_Y = 200f
    private const val PLAYER_SIZE = 30f
    private const val PLAYER_HALF = PLAYER_SIZE / 2f

    /**
     * Dibuja un frame completo del juego.
     * Llamado en cada recomposición del [Canvas] en GameScreen.
     *
     * @param drawScope contexto de dibujo de Compose
     * @param state     estado del juego en este frame
     */
    fun draw(drawScope: DrawScope, state: GameState) {
        with(drawScope) {
            // ── Fondo ────────────────────────────────────────────────────────
            drawRect(color = COLOR_BG, size = size)

            // ── Suelo ────────────────────────────────────────────────────────
            drawRect(
                color = COLOR_GROUND,
                topLeft = Offset(0f, GROUND_Y + PLAYER_HALF + 2f),
                size = Size(size.width, size.height - GROUND_Y),
            )

            // ── Monedas ──────────────────────────────────────────────────────
            state.coins.forEach { coin ->
                // Anillo exterior
                drawCircle(
                    color = COLOR_COIN_RING,
                    radius = coin.radius + 2f,
                    center = Offset(coin.x, coin.y),
                )
                // Relleno
                drawCircle(
                    color = COLOR_COIN,
                    radius = coin.radius,
                    center = Offset(coin.x, coin.y),
                )
            }

            // ── Obstáculos ───────────────────────────────────────────────────
            state.obstacles.forEach { obs ->
                // Sombra
                drawRect(
                    color = Color(0x40000000),
                    topLeft = Offset(obs.x + 3f, obs.y + 3f),
                    size = Size(obs.width, obs.height),
                )
                // Cuerpo
                drawRect(
                    color = COLOR_OBSTACLE,
                    topLeft = Offset(obs.x, obs.y),
                    size = Size(obs.width, obs.height),
                )
            }

            // ── Jugador ──────────────────────────────────────────────────────
            val player = state.player
            // Sombra del jugador
            drawRect(
                color = Color(0x40000000),
                topLeft = Offset(player.x - PLAYER_HALF + 3f, player.y - PLAYER_HALF + 3f),
                size = Size(PLAYER_SIZE, PLAYER_SIZE),
            )
            // Cuerpo
            drawRect(
                color = COLOR_PLAYER,
                topLeft = Offset(player.x - PLAYER_HALF, player.y - PLAYER_HALF),
                size = Size(PLAYER_SIZE, PLAYER_SIZE),
            )
        }
    }
}
