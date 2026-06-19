package mx.utng.utngrunner.domain.model

/**
 * Estado inmutable del juego — Unidireccional Data Flow
 *
 * Usamos [GamePhase] en lugar de booleanos isRunning/isGameOver
 * para representar el ciclo de vida del juego de forma exhaustiva.
 */
data class GameState(
    val phase: GamePhase  = GamePhase.IDLE,
    val score: Int        = 0,
    val level: Int        = 1,
    val lives: Int        = 3,
    val highScore: Int    = 0,
    val player: Player    = Player(),
    val obstacles: List<Obstacle> = emptyList(),
    val coins: List<Coin>         = emptyList(),
    val heartRate: Int    = 72,
    val gameSpeed: Float  = 3f,
)

/** Ciclo de vida del juego — reemplaza los booleanos isRunning / isGameOver */
enum class GamePhase { IDLE, PLAYING, PAUSED, DEAD }
