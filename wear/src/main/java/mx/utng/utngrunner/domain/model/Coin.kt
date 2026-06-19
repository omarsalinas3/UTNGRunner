package mx.utng.utngrunner.domain.model

/**
 * Entidad moneda coleccionable.
 * Kotlin puro — sin dependencias Android.
 *
 * @property x          posición horizontal (se mueve con gameSpeed)
 * @property y          posición vertical fija
 * @property phase      offset de animación senoidal (parpadeo / flotación)
 * @property collected  true cuando el jugador ya la recogió
 */
data class Coin(
    val x: Float,
    val y: Float,
    val phase: Float       = 0f,
    val collected: Boolean = false,
)
