package mx.utng.utngrunner.domain.model

/**
 * Entidad del jugador.
 * Kotlin puro — sin dependencias Android.
 *
 * @property x          posición horizontal en píxeles lógicos
 * @property y          posición vertical en píxeles lógicos
 * @property velocityY  velocidad vertical (positivo = cae, negativo = salta)
 * @property isAlive    false cuando el jugador ha colisionado fatalmente
 */
data class Player(
    val x: Float = 80f,
    val y: Float = 200f,
    val velocityY: Float = 0f,
    val isAlive: Boolean = true,
)
