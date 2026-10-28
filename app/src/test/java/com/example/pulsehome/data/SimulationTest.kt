package com.example.pulsehome.data

import com.example.pulsehome.model.ConnectionStatus
import com.example.pulsehome.model.Device
import com.example.pulsehome.model.DeviceKind
import com.example.pulsehome.model.SensorReading
import com.example.pulsehome.model.SensorStatus
import com.example.pulsehome.model.SensorVariable
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulationTest {

    private fun sensorOf(snapshot: List<Device>) =
        snapshot.first { it.kind == DeviceKind.SENSOR_NODE }


    // ---------- Rangos del modelo ----------

    @Test
    fun lecturaNormal_noEstaFueraDeRango() {
        val reading = SensorReading(24.0, 45.0, 8.0, 0L)
        assertFalse(reading.isOutsideDemoRange)
    }

    @Test
    fun temperaturaFueraDeRango_esAlerta() {
        assertTrue(SensorReading(31.2, 45.0, 8.0, 0L).isTemperatureOutOfRange)
        assertTrue(SensorReading(14.9, 45.0, 8.0, 0L).isTemperatureOutOfRange)
        assertFalse(SensorReading(15.0, 45.0, 8.0, 0L).isTemperatureOutOfRange)
        assertFalse(SensorReading(30.0, 45.0, 8.0, 0L).isTemperatureOutOfRange)
    }

    @Test
    fun limiteDeTemperatura_30esDentroYMasDe30esFuera() {
        assertFalse(SensorReading(30.0, 45.0, 8.0, 0L).isOutsideDemoRange)
        assertTrue(SensorReading(30.1, 45.0, 8.0, 0L).isOutsideDemoRange)
    }

    @Test
    fun humedadBajaYGasAlto_sonAlerta() {
        assertTrue(SensorReading(24.0, 27.0, 8.0, 0L).isHumidityOutOfRange)
        assertTrue(SensorReading(24.0, 45.0, 62.0, 0L).isGasOutOfRange)
        assertFalse(SensorReading(24.0, 30.0, 20.0, 0L).isOutsideDemoRange)
    }

    @Test
    fun dispositivoDesconectado_noTieneAlertaActual() {
        val reading = SensorReading(31.2, 45.0, 8.0, 0L)
        val device = Device("d", "Nodo", "Casa", ConnectionStatus.DISCONNECTED, reading = reading)
        assertFalse(device.hasCurrentAlert)
        assertEquals(SensorStatus.OFFLINE, device.statusOf(SensorVariable.TEMPERATURE))
    }

    // ---------- Simulador ----------

    @Test
    fun fase0_nodoSensorConectadoYNormal() {
        val sensor = sensorOf(simulatedSnapshot(0, emptyList(), 1_000L))
        assertEquals(ConnectionStatus.CONNECTED, sensor.connection)
        assertFalse(sensor.hasCurrentAlert)
        assertEquals(1_000L, sensor.reading?.measuredAtMillis)
    }

    @Test
    fun fases2a4_generanUnaAlertaCadaUna() {
        val temp = sensorOf(simulatedSnapshot(2, emptyList(), 0L))
        val hum = sensorOf(simulatedSnapshot(3, emptyList(), 0L))
        val gas = sensorOf(simulatedSnapshot(4, emptyList(), 0L))
        assertEquals(SensorStatus.ALERT, temp.statusOf(SensorVariable.TEMPERATURE))
        assertEquals(SensorStatus.NORMAL, temp.statusOf(SensorVariable.GAS))
        assertEquals(SensorStatus.ALERT, hum.statusOf(SensorVariable.HUMIDITY))
        assertEquals(SensorStatus.ALERT, gas.statusOf(SensorVariable.GAS))
    }

    @Test
    fun fase5_nodoSensorSeDesconectaYGuardaLaUltimaLectura() {
        val fase4 = simulatedSnapshot(4, emptyList(), 4_000L)
        val fase5 = simulatedSnapshot(5, fase4, 5_000L)
        val sensor = sensorOf(fase5)
        assertEquals(ConnectionStatus.DISCONNECTED, sensor.connection)
        assertEquals(4_000L, sensor.reading?.measuredAtMillis)
        assertFalse(sensor.hasCurrentAlert)
    }

    @Test
    fun sensorDesconectado_noCambiaValorNiHoraMientrasSigueDesconectado() {
        val conectado = simulatedSnapshot(4, emptyList(), 4_000L)
        val primera = simulatedSnapshot(5, conectado, 5_000L)
        val segunda = simulatedSnapshot(5, primera, 6_000L)
        assertEquals(sensorOf(primera).reading, sensorOf(segunda).reading)
    }

    @Test
    fun cadaFoto_traeLosTresDispositivos() {
        val snapshot = simulatedSnapshot(0, emptyList(), 0L)
        assertEquals(3, snapshot.size)
        assertEquals(1, snapshot.count { it.kind == DeviceKind.SENSOR_NODE })
        assertEquals(1, snapshot.count { it.kind == DeviceKind.CENTRAL_NODE })
        assertEquals(1, snapshot.count { it.kind == DeviceKind.PHONE })
    }

    // ---------- Repositorio ----------

    @Test(expected = IllegalArgumentException::class)
    fun repositorio_rechazaIntervaloCeroONegativo() {
        FakeDeviceRepository(intervalMillis = 0L)
    }

    @Test
    fun repositorio_recorreLasSeisFasesYVuelveAlInicio() = runBlocking {
        var reloj = 0L
        val repository = FakeDeviceRepository(intervalMillis = 1L, nowMillis = { reloj++ })
        val snapshots = repository.observeDevices().take(7).toList()
        assertEquals(7, snapshots.size)
        val estados = snapshots.map { sensorOf(it).connection }
        assertEquals(ConnectionStatus.DISCONNECTED, estados[5])
        assertEquals(ConnectionStatus.CONNECTED, estados[6])
        assertTrue(snapshots.any { sensorOf(it).hasCurrentAlert })
    }
}
