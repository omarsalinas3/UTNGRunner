package mx.utng.utngrunner.presentation.game

import mx.utng.utngrunner.domain.model.Coin
import mx.utng.utngrunner.domain.model.GamePhase
import mx.utng.utngrunner.domain.model.GameState
import mx.utng.utngrunner.domain.model.Obstacle
import mx.utng.utngrunner.domain.model.ObstacleType
import mx.utng.utngrunner.domain.model.Player
import kotlin.math.sin
import kotlin.random.Random

/**
 * Motor del juego — lógica de simulación a ~60 fps.
 *
 * Responsabilidades:
 * - Física del jugador (gravedad, salto, deslizamiento, invencibilidad)
 * - Generación y movimiento de obstáculos temáticos (TAREA/EXAMEN/BUG/REPO)
 * - Generación y animación senoidal de monedas
 * - Detección de colisiones con hitbox por tipo de obstáculo
 * - Incremento de velocidad por nivel
 *
 * Objeto singleton — no tiene estado propio; todo el estado va en [GameState].
 */
object GameEngine {

    // ── Intervalos de spawn (frames) ─────────────────────────────────────────
    private const val OBS_INTERVAL_MIN  = 60
    private const val OBS_INTERVAL_MAX  = 120
    private const val COIN_INTERVAL_MIN = 40
    private const val COIN_INTERVAL_MAX = 90
    private const val SCORE_PER_LEVEL   = 20

    private var frameCount      = 0
    private var nextObsIn       = OBS_INTERVAL_MIN
    private var nextCoinIn      = COIN_INTERVAL_MIN
    private var obstacleCounter = 0

    // ── API pública ───────────────────────────────────────────────────────────

    /** Reinicia contadores internos para una nueva partida. */
    fun reset() {
        frameCount      = 0
        nextObsIn       = randomInterval(OBS_INTERVAL_MIN, OBS_INTERVAL_MAX)
        nextCoinIn      = randomInterval(COIN_INTERVAL_MIN, COIN_INTERVAL_MAX)
        obstacleCounter = 0
    }

    /**
     * Calcula el siguiente [GameState] dado el estado actual.
     * Llamado en cada tick del game loop (~60 fps) desde [GameViewModel].
     */
    fun tick(current: GameState, screenWidth: Float): GameState {
        if (current.phase != GamePhase.PLAYING) return current

        frameCount++

        // ── Velocidad progresiva por nivel ────────────────────────────────────
        val level     = (current.score / SCORE_PER_LEVEL) + 1
        val gameSpeed = (3f + (level - 1) * 0.4f).coerceAtMost(10f)

        // ── Física del jugador ────────────────────────────────────────────────
        val player = tickPlayer(current.player)

        // ── Obstáculos ────────────────────────────────────────────────────────
        var obstacles = current.obstacles
            .map { it.copy(x = it.x - gameSpeed) }
            .filter { it.x + it.width > 0 }

        nextObsIn--
        if (nextObsIn <= 0) {
            obstacles  = obstacles + spawnObstacle(screenWidth)
            nextObsIn  = randomInterval(OBS_INTERVAL_MIN, OBS_INTERVAL_MAX)
        }

        // ── Monedas ───────────────────────────────────────────────────────────
        var coins = current.coins
            .map { it.copy(x = it.x - gameSpeed) }
            .filter { it.x + 8f > 0 && !it.collected }

        nextCoinIn--
        if (nextCoinIn <= 0) {
            coins     = coins + spawnCoin(screenWidth)
            nextCoinIn = randomInterval(COIN_INTERVAL_MIN, COIN_INTERVAL_MAX)
        }

        // ── Detección de colisiones ───────────────────────────────────────────
        val playerHitbox = playerHitbox(player)

        val hitObstacle = !player.isInvincible && obstacles.any { obs ->
            rectsOverlap(playerHitbox, obstacleHitbox(obs, player.y))
        }

        val (collectedCoins, remainingCoins) = coins.partition { coin ->
            circleRectOverlap(coin.x, coin.y + sin(coin.phase) * 4f, 6f, playerHitbox)
        }

        val newScore = current.score + collectedCoins.size
        val newLives = if (hitObstacle) current.lives - 1 else current.lives
        val newPhase = if (newLives <= 0) GamePhase.DEAD else GamePhase.PLAYING

        // Al golpear obstáculo → invencibilidad temporal
        val updatedPlayer = if (hitObstacle && newLives > 0)
            player.copy(isInvincible = true, invincibleFrames = Player.INVINCIBLE_FRAMES)
        else
            player

        return current.copy(
            phase     = newPhase,
            score     = newScore,
            level     = level,
            lives     = newLives,
            gameSpeed = gameSpeed,
            player    = updatedPlayer,
            obstacles = obstacles,
            coins     = remainingCoins.map { it.copy(phase = it.phase + 0.15f) },
            highScore = maxOf(current.highScore, newScore),
        )
    }

    /**
     * Aplica impulso de salto (solo si está en el suelo y no está deslizando).
     */
    fun jump(current: GameState): GameState {
        val p = current.player
        if (p.isJumping || p.isSliding) return current
        return current.copy(
            player = p.copy(
                velocityY = Player.JUMP_VELOCITY,
                isJumping = true,
            ),
        )
    }

    /**
     * Inicia deslizamiento (solo si está en el suelo y no está saltando).
     */
    fun slide(current: GameState): GameState {
        val p = current.player
        if (p.isJumping || p.isSliding) return current
        return current.copy(
            player = p.copy(
                isSliding  = true,
                slideFrames = Player.SLIDE_DURATION,
            ),
        )
    }

    // ── Física privada ────────────────────────────────────────────────────────

    private fun tickPlayer(p: Player): Player {
        // Gravedad
        var vy = p.velocityY + Player.GRAVITY
        var y  = p.y + vy
        var jumping = p.isJumping

        if (y >= Player.FLOOR_Y) {
            y       = Player.FLOOR_Y
            vy      = 0f
            jumping = false
        }

        // Deslizamiento
        val slideFrames = (p.slideFrames - 1).coerceAtLeast(0)
        val sliding     = slideFrames > 0

        // Invencibilidad
        val invFrames   = (p.invincibleFrames - 1).coerceAtLeast(0)
        val invincible  = invFrames > 0

        return p.copy(
            y                = y,
            velocityY        = vy,
            isJumping        = jumping,
            isSliding        = sliding,
            slideFrames      = slideFrames,
            isInvincible     = invincible,
            invincibleFrames = invFrames,
        )
    }

    // ── Spawn ─────────────────────────────────────────────────────────────────

    private fun spawnObstacle(screenWidth: Float): Obstacle {
        val type = ObstacleType.entries.random()
        return Obstacle(x = screenWidth + 10f, width = type.w, height = type.h, type = type)
    }

    private fun spawnCoin(screenWidth: Float): Coin {
        val y = Player.FLOOR_Y - Random.nextFloat() * 60f - 20f
        return Coin(x = screenWidth + 10f, y = y, phase = Random.nextFloat() * 6.28f)
    }

    // ── Hitboxes ──────────────────────────────────────────────────────────────

    private data class Rect(val x: Float, val y: Float, val w: Float, val h: Float)

    private fun playerHitbox(p: Player): Rect {
        val h = if (p.isSliding) 14f else 28f
        val w = 18f
        return Rect(p.x - w / 2f, p.y - h, w, h)
    }

    private fun obstacleHitbox(obs: Obstacle, floorY: Float): Rect =
        Rect(obs.x, floorY - obs.height, obs.width.toFloat(), obs.height.toFloat())

    private fun rectsOverlap(a: Rect, b: Rect): Boolean =
        a.x < b.x + b.w && a.x + a.w > b.x &&
            a.y < b.y + b.h && a.y + a.h > b.y

    private fun circleRectOverlap(cx: Float, cy: Float, r: Float, rect: Rect): Boolean {
        val nx = cx.coerceIn(rect.x, rect.x + rect.w)
        val ny = cy.coerceIn(rect.y, rect.y + rect.h)
        val dx = cx - nx
        val dy = cy - ny
        return dx * dx + dy * dy <= r * r
    }

    private fun randomInterval(min: Int, max: Int) = Random.nextInt(min, max + 1)
}
