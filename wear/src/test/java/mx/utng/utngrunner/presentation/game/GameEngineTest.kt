package mx.utng.utngrunner.presentation.game

import mx.utng.utngrunner.domain.model.GamePhase
import mx.utng.utngrunner.domain.model.GameState
import mx.utng.utngrunner.domain.model.Obstacle
import mx.utng.utngrunner.domain.model.ObstacleType
import mx.utng.utngrunner.domain.model.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    @Test
    fun `player falls due to gravity`() {
        val state = GameState(
            phase = GamePhase.PLAYING,
            player = Player(y = 100f, velocityY = 0f)
        )
        val next = GameEngine.update(state, frame = 0)
        assertTrue(next.player.y > 100f)  // cayó
    }

    @Test
    fun `score increases every frame`() {
        val state = GameState(phase = GamePhase.PLAYING, score = 0)
        // En nuestro motor actualizamos los puntos cuando (frame % 6 == 0), por lo que en el frame 0 suma +1
        val next = GameEngine.update(state, frame = 0)
        assertEquals(1, next.score)
    }

    @Test
    fun `level increases at score 300`() {
        val state = GameState(phase = GamePhase.PLAYING, score = 299)
        // El puntaje sube a 300 en el frame 0
        val next = GameEngine.update(state, frame = 0)
        assertEquals(2, next.level)
    }

    @Test
    fun `lives decrease on obstacle collision`() {
        // Configuramos un obstáculo para que colisione con el jugador
        val obstacle = Obstacle(
            x = Player(y = 160f).x - 5f,
            width = 20, 
            height = 35, 
            type = ObstacleType.TAREA
        )
        val state = GameState(
            phase = GamePhase.PLAYING,
            player = Player(y = 160f, isInvincible = false),
            obstacles = listOf(obstacle), 
            lives = 3
        )
        val next = GameEngine.update(state, frame = 0)
        assertTrue(next.lives < 3)
    }

    @Test
    fun `game over when lives reach zero`() {
        val state = GameState(phase = GamePhase.PLAYING, lives = 0)
        val next = GameEngine.update(state, frame = 0)
        assertEquals(GamePhase.DEAD, next.phase)
    }
}
