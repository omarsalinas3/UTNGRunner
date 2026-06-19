package mx.utng.utngrunner.domain.model

/**
 * Entidad moneda coleccionable.
 * Kotlin puro — sin dependencias Android.
 *
 * @property id         identificador único para Compose key
 * @property x          posición horizontal
 * @property y          posición vertical
 * @property radius     radio de colisión/dibujo
 * @property isCollected true cuando el jugador ya la recogió (se elimina del estado)
 */
data class Coin(
    val id: Int,
    val x: Float,
    val y: Float,
    val radius: Float = 10f,
    val isCollected: Boolean = false,
)
