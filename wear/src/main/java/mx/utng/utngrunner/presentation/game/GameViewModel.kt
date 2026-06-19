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
import mx.utng.utngrunner.domain.model.GamePhase
import mx.utng.utngrunner.domain.model.GameState
import mx.utng.utngrunner.domain.usecase.GetHighScoreUseCase
import mx.utng.utngrunner.domain.usecase.SaveHighScoreUseCase

/**
 * ViewModel del juego — gestiona el estado de UI y el game loop.
 *
 * Responsabilidades:
 * - Mantener [GameState] como StateFlow para que [GameScreen] recomponga
 * - Arrancar/detener el game loop a ~60 fps usando coroutines
 * - Delegar lógica de dominio a [GetHighScoreUseCase] / [SaveHighScoreUseCase]
 * - Coordinar con [GameEngine] para calcular cada frame
 *
 * NO conoce Composables ni Canvas — eso es responsabilidad de GameScreen/GameRenderer.
 */
class GameViewModel(
    private val getHighScoreUseCase: GetHighScoreUseCase,
    private val saveHighScoreUseCase: SaveHighScoreUseCase,
    private val context: Context,
) : ViewModel() {

    companion object {
        private const val FRAME_DELAY_MS = 16L  // ~60 fps
    }

    // ── Estado de UI ──────────────────────────────────────────────────────────
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // ── Dimensiones de pantalla ───────────────────────────────────────────────
    private var screenWidth: Float = 454f   // Galaxy Watch 6 por defecto

    private var gameLoopJob: Job? = null

    // ── API pública ───────────────────────────────────────────────────────────

    /** Inicia una nueva partida cargando el récord histórico. */
    fun startGame() {
        viewModelScope.launch {
            val highScore = getHighScoreUseCase()
            GameEngine.reset()
            _gameState.value = GameState(
                phase     = GamePhase.PLAYING,
                highScore = highScore,
            )
            startGameLoop()
        }
    }

    /** El jugador toca la pantalla → salto. */
    fun onTap() {
        _gameState.value = GameEngine.jump(_gameState.value)
    }

    /** El jugador hace swipe hacia abajo → deslizamiento. */
    fun onSlide() {
        _gameState.value = GameEngine.slide(_gameState.value)
    }

    /** Pausa o reanuda el game loop. */
    fun togglePause() {
        val state = _gameState.value
        when (state.phase) {
            GamePhase.PLAYING -> {
                gameLoopJob?.cancel()
                _gameState.value = state.copy(phase = GamePhase.PAUSED)
            }
            GamePhase.PAUSED -> {
                _gameState.value = state.copy(phase = GamePhase.PLAYING)
                startGameLoop()
            }
            else -> Unit
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
            while (_gameState.value.phase == GamePhase.PLAYING) {
                delay(FRAME_DELAY_MS)
                val next = GameEngine.tick(_gameState.value, screenWidth)
                _gameState.value = next
                if (next.phase == GamePhase.DEAD) {
                    onGameOver(next.score)
                    break
                }
            }
        }
    }

    private fun onGameOver(score: Int) {
        gameLoopJob?.cancel()
        vibrate()
        viewModelScope.launch {
            saveHighScoreUseCase(score)
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
