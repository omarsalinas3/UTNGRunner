package mx.utng.utngrunner.domain.usecase

import mx.utng.utngrunner.domain.repository.ScoreRepository

/**
 * Caso de uso: obtener el récord histórico.
 *
 * Encapsula la regla de negocio de consulta de puntuación máxima.
 * El ViewModel invoca este use-case; NUNCA accede al repositorio directamente.
 *
 * @param repository Implementación inyectada del contrato ScoreRepository.
 */
class GetHighScoreUseCase(
    private val repository: ScoreRepository,
) {
    /**
     * Ejecuta el caso de uso.
     * @return el mejor puntaje registrado (0 si no existe aún).
     */
    suspend operator fun invoke(): Int = repository.getHighScore()
}
