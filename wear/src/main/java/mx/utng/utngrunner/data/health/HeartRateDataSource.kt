package mx.utng.utngrunner.data.health

import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.PassiveListenerCallback
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fuente de datos de frecuencia cardíaca usando Health Services API.
 *
 * Pertenece a la capa Data — puede importar el SDK de Health Services.
 * Expone [heartRate] como StateFlow para que el ViewModel lo coleccione
 * y lo incluya en [GameState.heartRate].
 *
 * @param healthServicesClient cliente inyectado de Health Services
 */
class HeartRateDataSource(
    private val healthServicesClient: HealthServicesClient,
) {
    private val _heartRate = MutableStateFlow(72)

    /** Frecuencia cardíaca en pulsaciones por minuto (bpm). Valor inicial: 72. */
    val heartRate: StateFlow<Int> = _heartRate.asStateFlow()

    /**
     * Inicia la escucha pasiva del sensor de frecuencia cardíaca.
     * No requiere que el usuario tenga la app en primer plano.
     *
     * Llama esta función desde el ViewModel al iniciar el juego.
     */
    suspend fun startMonitoring() {
        val client = healthServicesClient.passiveMonitoringClient

        val config = PassiveListenerConfig.builder()
            .setDataTypes(setOf(DataType.HEART_RATE_BPM))
            .build()

        client.setPassiveListenerCallback(
            config,
            object : PassiveListenerCallback {
                override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
                    // En Health Services 1.1.x el value de HEART_RATE_BPM es Double directamente
                    val bpm = dataPoints
                        .getData(DataType.HEART_RATE_BPM)
                        .lastOrNull()
                        ?.value
                        ?.toInt() ?: return

                    _heartRate.value = bpm
                }
            },
        )
    }
}
