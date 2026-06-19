package mx.utng.utngrunner.domain.model

/**
 * Representa el estado global del juego en un instante dado.
 * Es una entidad de dominio pura (sin dependencias Android).
 *
 * @property isRunning  true si el game loop está activo
 * @property isGameOver true cuando el jugador colisionó con un obstáculo
 * @property score      puntuación acumulada en la sesión actual
 * @property highScore  récord histórico recuperado desde DataStore
 */
data class GameState(
    val isRunning: Boolean = false,
    val isGameOver: Boolean = false,
    val score: Int = 0,
    val highScore: Int = 0,
    val player: Player = Player(),
    val obstacles: List<Obstacle> = emptyList(),
    val coins: List<Coin> = emptyList(),
)
