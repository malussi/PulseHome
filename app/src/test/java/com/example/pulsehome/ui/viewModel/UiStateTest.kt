package com.example.pulsehome.ui.viewModel

import com.example.pulsehome.data.FakeDeviceRepository
import com.example.pulsehome.data.simulatedSnapshot
import com.example.pulsehome.model.Device
import com.example.pulsehome.model.SensorReading
import com.example.pulsehome.model.SensorVariable
import com.example.pulsehome.model.newAlerts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class UiStateTest {

    // ---------- Alertas e historial ----------

    @Test
    fun alertaNueva_soloSeCreaCuandoLaVariableEmpiezaAFallar() {
        val normal = SensorReading(24.0, 45.0, 8.0, 1L)
        val calor = SensorReading(31.2, 45.0, 8.0, 2L)
        val sigueCalor = SensorReading(32.0, 45.0, 8.0, 3L)
        assertEquals(1, newAlerts(normal, calor).size)
        assertEquals("Temperatura alta", newAlerts(normal, calor).first().title)
        assertTrue(newAlerts(calor, sigueCalor).isEmpty())
        assertEquals(1, newAlerts(null, calor).size)
    }

    @Test
    fun estadoDeLaUi_acumulaAlertasYLimitaElHistorial() {
        var state = DevicesUiState()
        assertTrue(state.isLoading)
        var previous = emptyList<Device>()
        for (ciclo in 0 until 5) {
            for (phase in 0 until FakeDeviceRepository.PHASES) {
                previous = simulatedSnapshot(phase, previous, (ciclo * 10 + phase).toLong())
                state = state.withDevices(previous)
            }
        }
        assertFalse(state.isLoading)
        assertNotNull(state.sensorNode)
        // 3 alertas por ciclo (temperatura, humedad y gas) x 5 ciclos = 15, bajo el límite
        assertEquals(15, state.alerts.size)
        assertTrue(state.alerts.size <= DevicesUiState.MAX_ALERTS)
        // la más reciente va primero
        assertEquals("Gas alto", state.alerts.first().title)
        // la última fase simulada (5) deja al nodo sensor desconectado
        assertEquals(2, state.connectedCount)
    }

    @Test
    fun estadoDeLaUi_contadoresDeConexionYAlerta() {
        val state = DevicesUiState().withDevices(simulatedSnapshot(2, emptyList(), 0L))
        assertEquals(3, state.connectedCount)
        assertEquals(1, state.alertCount)
        assertNull(state.errorMessage)
    }

    // ---------- Textos de la interfaz ----------

    @Test
    fun formatoDeTextos() {
        val reading = SensorReading(24.6, 44.4, 10.0, 0L)
        assertEquals("24.6°C", formatValue(SensorVariable.TEMPERATURE, reading, Locale.US))
        assertEquals("44%", formatValue(SensorVariable.HUMIDITY, reading, Locale.US))
        assertEquals("--", formatValue(SensorVariable.GAS, null, Locale.US))
        assertEquals("15°C a 30°C", formatLimit(SensorVariable.TEMPERATURE, Locale.US))
        assertEquals("hace 5 s", timeAgo(1_000L, 6_000L))
        assertEquals("hace 3 min", timeAgo(0L, 180_000L))
    }
}
