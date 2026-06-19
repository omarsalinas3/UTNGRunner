package mx.utng.utngrunner.domain.model

/**
 * Entidad obstáculo con tipo temático UTNG.
 * Kotlin puro — sin dependencias Android.
 *
 * El tipo determina las dimensiones canónicas y la etiqueta visual.
 * [x] es la posición actual (se decrementa cada frame por [GameState.gameSpeed]).
 */
data class Obstacle(
    val x: Float,
    val width: Int,
    val height: Int,
    val type: ObstacleType,
)

/**
 * Tipos de obstáculo temáticos de la UTNG.
 * Cada tipo tiene dimensiones fijas (w, h) y una etiqueta para el Canvas.
 */
enum class ObstacleType(val label: String, val w: Int, val h: Int) {
    TAREA("TAREA",  20, 35),
    EXAMEN("EXAMEN", 14, 50),
    BUG("BUG",      22, 22),
    REPO("REPO",    30, 18),
}
