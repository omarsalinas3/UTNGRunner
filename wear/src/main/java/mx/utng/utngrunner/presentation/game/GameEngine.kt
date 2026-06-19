package mx.utng.utngrunner.presentation.game

import mx.utng.utngrunner.domain.model.Coin
import mx.utng.utngrunner.domain.model.GameState
import mx.utng.utngrunner.domain.model.Obstacle
import mx.utng.utngrunner.domain.model.Player

/**
 * Motor del juego — lógica de simulación a ~60 fps.
 *
 * Responsabilidades:
 * - Física del jugador (gravedad, salto)
 * - Generación y movimiento de obstáculos y monedas
 * - Detección de colisiones
 * - Incremento de puntuación
 *
 * Pertenece a Presentación porque opera sobre estado de UI (GameState)
 * y es controlado por el ViewModel. No tiene imports Android directos.
 */
object GameEngine {

    // ── Constantes físicas ───────────────────────────────────────────────────
    private const val GRAVITY = 1.2f
    private const val JUMP_FORCE = -18f
    private const val GROUND_Y = 200f
    private const val SCROLL_SPEED = 5f
    private const val OBSTACLE_SPAWN_INTERVAL = 80   // frames
    private const val COIN_SPAWN_INTERVAL = 50        // frames

    private var frameCount = 0
    private var obstacleIdCounter = 0
    private var coinIdCounter = 0

    /**
     * Calcula el siguiente estado del juego dado el estado actual.
     * Llama este método en cada tick del game loop (CoroutineTimer en ViewModel).
     *
     * @param current estado actual del juego
     * @param screenWidth ancho de la pantalla en píxeles lógicos
     * @return nuevo [GameState] con las entidades actualizadas
     */
    fun tick(current: GameState, screenWidth: Float): GameState {
        if (!current.isRunning || current.isGameOver) return current

        frameCount++

        // ── Física del jugador ───────────────────────────────────────────────
        var player = current.player
        var newVelocityY = player.velocityY + GRAVITY
        var newY = player.y + newVelocityY

        if (newY >= GROUND_Y) {
            newY = GROUND_Y
            newVelocityY = 0f
        }
        player = player.copy(y = newY, velocityY = newVelocityY)

        // ── Obstáculos ───────────────────────────────────────────────────────
        var obstacles = current.obstacles
            .map { it.copy(x = it.x - SCROLL_SPEED) }
            .filter { it.x + it.width > 0 }

        if (frameCount % OBSTACLE_SPAWN_INTERVAL == 0) {
            obstacles = obstacles + Obstacle(
                id = obstacleIdCounter++,
                x = screenWidth,
                y = GROUND_Y - 60f,
            )
        }

        // ── Monedas ──────────────────────────────────────────────────────────
        var coins = current.coins
            .map { it.copy(x = it.x - SCROLL_SPEED) }
            .filter { it.x + it.radius > 0 && !it.isCollected }

        if (frameCount % COIN_SPAWN_INTERVAL == 0) {
            coins = coins + Coin(
                id = coinIdCounter++,
                x = screenWidth,
                y = GROUND_Y - 80f,
            )
        }

        // ── Detección de colisiones ──────────────────────────────────────────
        val playerRect = FloatRect(player.x - 15f, player.y - 30f, 30f, 30f)

        val hitObstacle = obstacles.any { obs ->
            rectsOverlap(playerRect, FloatRect(obs.x, obs.y, obs.width, obs.height))
        }

        val collectedCoins = coins.filter { coin ->
            circleRectOverlap(coin.x, coin.y, coin.radius, playerRect)
        }
        val remainingCoins = coins.map { coin ->
            if (collectedCoins.contains(coin)) coin.copy(isCollected = true) else coin
        }.filter { !it.isCollected }

        val newScore = current.score + collectedCoins.size

        return current.copy(
            player = player,
            obstacles = obstacles,
            coins = remainingCoins,
            score = newScore,
            isGameOver = hitObstacle,
            isRunning = !hitObstacle,
        )
    }

    /**
     * Aplica impulso de salto al jugador (solo si está en el suelo).
     */
    fun jump(current: GameState): GameState {
        if (current.player.y < GROUND_Y) return current // ya en el aire
        return current.copy(
            player = current.player.copy(velocityY = JUMP_FORCE),
        )
    }

    /** Reinicia contadores internos del motor para una nueva partida. */
    fun reset() {
        frameCount = 0
        obstacleIdCounter = 0
        coinIdCounter = 0
    }

    // ── Helpers de colisión (privados) ───────────────────────────────────────
    private data class FloatRect(val x: Float, val y: Float, val w: Float, val h: Float)

    private fun rectsOverlap(a: FloatRect, b: FloatRect): Boolean =
        a.x < b.x + b.w && a.x + a.w > b.x &&
            a.y < b.y + b.h && a.y + a.h > b.y

    private fun circleRectOverlap(cx: Float, cy: Float, r: Float, rect: FloatRect): Boolean {
        val nearestX = cx.coerceIn(rect.x, rect.x + rect.w)
        val nearestY = cy.coerceIn(rect.y, rect.y + rect.h)
        val dx = cx - nearestX
        val dy = cy - nearestY
        return dx * dx + dy * dy <= r * r
    }
}
