package mx.utng.utngrunner.data.repository

import mx.utng.utngrunner.data.datasource.PreferencesDataSource
import mx.utng.utngrunner.domain.repository.ScoreRepository

/**
 * Implementación concreta de [ScoreRepository].
 *
 * Vive en la capa Data: puede depender de PreferencesDataSource (Android).
 * Implementa la interfaz del dominio → satisface la inversión de dependencias.
 *
 * @param dataSource fuente de datos local (DataStore).
 */
class ScoreRepositoryImpl(
    private val dataSource: PreferencesDataSource,
) : ScoreRepository {

    override suspend fun getHighScore(): Int =
        dataSource.getHighScore()

    override suspend fun saveHighScore(score: Int) {
        dataSource.saveHighScore(score)
    }
}
