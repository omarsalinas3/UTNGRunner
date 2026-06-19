package mx.utng.utngrunner.presentation.game

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.utng.utngrunner.data.health.HeartRateDataSource
import mx.utng.utngrunner.domain.model.GamePhase
import mx.utng.utngrunner.domain.model.GameState
import mx.utng.utngrunner.domain.model.Player
import mx.utng.utngrunner.domain.usecase.GetHighScoreUseCase
import mx.utng.utngrunner.domain.usecase.SaveHighScoreUseCase

enum class HapticType { JUMP, HIT }

class GameViewModel(
    private val getHighScore: GetHighScoreUseCase,
    private val saveHighScore: SaveHighScoreUseCase,
    private val heartRateSource: HeartRateDataSource,
    private val context: Context // Preservamos Context para la vibración
) : ViewModel() {

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()
    
    // Alias de retrocompatibilidad para no romper GameScreen
    val gameState: StateFlow<GameState> = state

    private val _hapticChannel = Channel<HapticType>()
    val hapticEvents = _hapticChannel.receiveAsFlow()

    private var gameFrame = 0L
    private var gameJob: Job? = null

    init {
        loadHighScore()
        observeHeartRate()
    }

    fun startGame() {
        _state.update { it.copy(
            phase     = GamePhase.PLAYING,
            highScore = it.highScore,
            score     = 0,
            lives     = 3,
            player    = Player(),
            obstacles = emptyList(),
            coins     = emptyList()
        ) }
        gameFrame = 0L
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            // 60 fps → ~16ms por frame
            while (_state.value.phase == GamePhase.PLAYING) {
                delay(16L)
                val prevLives = _state.value.lives
                _state.update { GameEngine.update(it, gameFrame++) }
                if (_state.value.lives < prevLives) {
                    _hapticChannel.trySend(HapticType.HIT)
                }
            }
            if (_state.value.phase == GamePhase.DEAD) {
                vibrate()
                saveHighScore(_state.value.score)
            }
        }
    }

    fun onJump() {
        val s = _state.value
        when (s.phase) {
            GamePhase.IDLE, GamePhase.DEAD -> startGame()
            GamePhase.PLAYING -> {
                if (!s.player.isJumping && s.player.y >= Player.FLOOR_Y - 5f) {
                    _state.update { it.copy(player = it.player.copy(
                        velocityY = Player.JUMP_VELOCITY, isJumping = true
                    ))}
                    _hapticChannel.trySend(HapticType.JUMP)
                }
            }
            else -> {}
        }
    }

    fun onSlide() {
        if (_state.value.phase != GamePhase.PLAYING || _state.value.player.isJumping) return
        _state.update { it.copy(player = it.player.copy(
            slideFrames = Player.SLIDE_DURATION
        ))}
    }

    // Alias para GameScreen (que llama a onTap)
    fun onTap() = onJump()
    
    fun togglePause() {
        val s = _state.value
        if (s.phase == GamePhase.PLAYING) {
            gameJob?.cancel()
            _state.update { it.copy(phase = GamePhase.PAUSED) }
        } else if (s.phase == GamePhase.PAUSED) {
            _state.update { it.copy(phase = GamePhase.PLAYING) }
            gameJob = viewModelScope.launch {
                while (_state.value.phase == GamePhase.PLAYING) {
                    delay(16L)
                    _state.update { GameEngine.update(it, gameFrame++) }
                }
                if (_state.value.phase == GamePhase.DEAD) {
                    vibrate()
                    saveHighScore(_state.value.score)
                }
            }
        }
    }

    fun setScreenWidth(width: Float) {
        // En el PASO 4 omitimos el parámetro y usamos 240/250 fijo en GameEngine.
        // Se preserva la función vacía para que GameScreen.kt siga compilando.
    }

    private fun loadHighScore() {
        viewModelScope.launch {
            val hs = getHighScore()
            _state.update { it.copy(highScore = hs) }
        }
    }

    private fun observeHeartRate() {
        viewModelScope.launch {
            heartRateSource.startMonitoring()
            heartRateSource.heartRate.collect { bpm ->
                _state.update { it.copy(heartRate = bpm) }
            }
        }
    }

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
        gameJob?.cancel() 
        super.onCleared()
    }
    
}
