package mx.utng.utngrunner.data.datasource

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Extensión de Context para acceder al DataStore como singleton */
private val Context.dataStore by preferencesDataStore(name = "utng_runner_prefs")

/**
 * Fuente de datos local — persiste el récord usando DataStore Preferences.
 *
 * Pertenece a la capa Data: puede importar Android SDK (Context, DataStore).
 * La capa Domain NO conoce esta clase — solo conoce la interfaz [ScoreRepository].
 */
class PreferencesDataSource(private val context: Context) {

    private object Keys {
        val HIGH_SCORE = intPreferencesKey("high_score")
    }

    /**
     * Lee el récord actual de DataStore.
     * @return valor persistido, o 0 si aún no existe.
     */
    suspend fun getHighScore(): Int =
        context.dataStore.data
            .map { it[Keys.HIGH_SCORE] ?: 0 }
            .first()

    /**
     * Escribe el nuevo récord en DataStore.
     * @param score puntaje a persistir.
     */
    suspend fun saveHighScore(score: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HIGH_SCORE] = score
        }
    }
}
