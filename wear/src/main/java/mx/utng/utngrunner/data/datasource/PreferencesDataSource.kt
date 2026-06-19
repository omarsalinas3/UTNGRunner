package mx.utng.utngrunner.data.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Extensión para acceder al DataStore desde un Context */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "utngrunner_prefs",
)

/**
 * Fuente de datos local basada en DataStore Preferences.
 * Persiste el récord histórico de forma asíncrona y reactiva.
 *
 * Pertenece a la capa Data — puede importar Android SDK (Context, DataStore).
 * La capa Domain NO conoce esta clase, solo conoce [ScoreRepository].
 */
class PreferencesDataSource(private val context: Context) {

    companion object {
        private val KEY_HIGH_SCORE = intPreferencesKey("high_score")
    }

    /**
     * Lee el récord actual de DataStore.
     * @return valor persistido, o 0 si aún no existe.
     */
    suspend fun getHighScore(): Int =
        context.dataStore.data
            .map { prefs -> prefs[KEY_HIGH_SCORE] ?: 0 }
            .first()

    /**
     * Escribe el nuevo récord en DataStore.
     * @param score nuevo récord a persistir.
     */
    suspend fun saveHighScore(score: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HIGH_SCORE] = score
        }
    }
}
