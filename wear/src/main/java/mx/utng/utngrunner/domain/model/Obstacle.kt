package mx.utng.utngrunner.domain.model

/**
 * Entidad obstáculo.
 * Kotlin puro — sin dependencias Android.
 *
 * @property id     identificador único para Compose key
 * @property x      posición horizontal (se mueve hacia la izquierda cada frame)
 * @property y      posición vertical (base del obstáculo)
 * @property width  ancho del obstáculo en píxeles lógicos
 * @property height alto del obstáculo en píxeles lógicos
 */
data class Obstacle(
    val id: Int,
    val x: Float,
    val y: Float,
    val width: Float = 30f,
    val height: Float = 60f,
)
