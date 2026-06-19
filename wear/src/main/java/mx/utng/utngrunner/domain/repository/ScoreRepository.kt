package mx.utng.utngrunner.domain.repository

/**
 * Contrato del repositorio de puntuaciones.
 * Solo conoce tipos del dominio — NINGUNA dependencia Android aquí.
 *
 * La implementación concreta vive en la capa Data (ScoreRepositoryImpl).
 * Esta inversión de dependencias permite testear los use-cases
 * sin necesidad de un dispositivo real ni DataStore.
 */
interface ScoreRepository {

    /**
     * Recupera el récord histórico almacenado de forma persistente.
     * @return el mejor puntaje registrado, o 0 si nunca se guardó.
     */
    suspend fun getHighScore(): Int

    /**
     * Persiste el nuevo récord si es mayor al actual.
     * @param score puntaje obtenido en la sesión actual.
     */
    suspend fun saveHighScore(score: Int)
}
