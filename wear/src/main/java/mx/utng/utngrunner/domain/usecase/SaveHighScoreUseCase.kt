package mx.utng.utngrunner.domain.usecase

import mx.utng.utngrunner.domain.repository.ScoreRepository

/** Caso de uso: encapsula UNA operación de negocio — guardar el récord si mejora */
class SaveHighScoreUseCase(private val repository: ScoreRepository) {
    suspend operator fun invoke(score: Int) {
        val current = repository.getHighScore()
        if (score > current) repository.saveHighScore(score)
    }
}
