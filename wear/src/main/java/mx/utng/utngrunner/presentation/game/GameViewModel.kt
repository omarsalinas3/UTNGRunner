package mx.utng.utngrunner.presentation.game

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.utng.utngrunner.domain.model.GameState
import mx.utng.utngrunner.domain.usecase.GetHighScoreUseCase
import mx.utng.utngrunner.domain.usecase.SaveHighScoreUseCase

/**
 * ViewModel del juego — gestiona el estado de UI y el game loop.
 *
 * Responsabilidades:
 * - Mantener [GameState] como StateFlow para que GameScreen recomponga
 * - Arrancar/detener el game loop a 60 fps usando coroutines
 * - Delegar lógica de puntuación a los use-cases del dominio
 * - Coordinar con [GameEngine] para calcular cada frame
 *
 * NO conoce nada de Composable ni del Canvas — eso es trabajo de GameScreen/GameRenderer.
 */
class GameViewModel(
    private val getHighScoreUseCase: GetHighScoreUseCase,
    private val saveHighScoreUseCase: SaveHighScoreUseCase,
    private val context: Context,
) : ViewModel() {

    companion object {
        private const val FRAME_DELAY_MS = 16L // ~60 fps
    }

    // ── Estado de UI ─────────────────────────────────────────────────────────
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // ── Dimensiones de pantalla (se fijan desde GameScreen al primer layout) ─
    var screenWidth: Float = 454f  // Wear OS Galaxy Watch 6 default
        private set

    private var gameLoopJob: Job? = null

    // ── Ciclo de vida del juego ───────────────────────────────────────────────

    /** Inicia el juego cargando el récord histórico y arrancando el game loop. */
    fun startGame() {
        viewModelScope.launch {
            val highScore = getHighScoreUseCase()
            GameEngine.reset()
            _gameState.value = GameState(
                isRunning = true,
                highScore = highScore,
            )
            startGameLoop()
        }
    }

    /** El jugador toca la pantalla → salto. */
    fun onTap() {
        _gameState.value = GameEngine.jump(_gameState.value)
    }

    /** Pausa / reanuda el game loop. */
    fun togglePause() {
        val current = _gameState.value
        if (current.isGameOver) return
        if (current.isRunning) {
            gameLoopJob?.cancel()
            _gameState.value = current.copy(isRunning = false)
        } else {
            _gameState.value = current.copy(isRunning = true)
            startGameLoop()
        }
    }

    /** Llamado al detectar game over: guarda récord y detiene el loop. */
    fun onGameOver() {
        gameLoopJob?.cancel()
        vibrate()
        viewModelScope.launch {
            saveHighScoreUseCase(_gameState.value.score)
        }
    }

    /** Actualiza el ancho de pantalla cuando GameScreen resuelve su layout. */
    fun setScreenWidth(width: Float) {
        screenWidth = width
    }

    // ── Game Loop ─────────────────────────────────────────────────────────────

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (_gameState.value.isRunning) {
                delay(FRAME_DELAY_MS)
                val next = GameEngine.tick(_gameState.value, screenWidth)
                _gameState.value = next
                if (next.isGameOver) {
                    onGameOver()
                    break
                }
            }
        }
    }

    // ── Feedback háptico ──────────────────────────────────────────────────────

    @Suppress("DEPRECATION")
    private fun vibrate() {
        val vibrator = context.getSystemService(VIBRATOR_SERVICE) as? Vibrator ?: return
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(200)
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    /**
     * Factory para instanciar GameViewModel con sus dependencias
     * sin necesidad de un framework DI (Hilt, Koin).
     */
    class Factory(
        private val getHighScoreUseCase: GetHighScoreUseCase,
        private val saveHighScoreUseCase: SaveHighScoreUseCase,
        private val context: Context,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            GameViewModel(getHighScoreUseCase, saveHighScoreUseCase, context) as T
    }
}
