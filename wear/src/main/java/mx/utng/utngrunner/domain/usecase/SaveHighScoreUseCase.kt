package mx.utng.utngrunner.domain.usecase

import mx.utng.utngrunner.domain.repository.ScoreRepository

/**
 * Caso de uso: guardar el récord histórico.
 *
 * Contiene la regla de negocio: solo persistir si el nuevo puntaje
 * supera al récord actual. Esto mantiene la lógica fuera de la capa Data
 * y hace que sea fácil de probar unitariamente.
 *
 * @param repository Implementación inyectada del contrato ScoreRepository.
 */
class SaveHighScoreUseCase(
    private val repository: ScoreRepository,
) {
    /**
     * Ejecuta el caso de uso.
     * @param score puntaje de la sesión terminada.
     */
    suspend operator fun invoke(score: Int) {
        val current = repository.getHighScore()
        if (score > current) {
            repository.saveHighScore(score)
        }
    }
}
