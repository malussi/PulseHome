package com.example.pulsehome.data

import com.example.pulsehome.model.ConnectionStatus
import com.example.pulsehome.model.Device
import com.example.pulsehome.model.DeviceKind
import com.example.pulsehome.model.SensorReading
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

// Simulador local del nodo sensor. No usa red ni hardware.
class FakeDeviceRepository(
    private val intervalMillis: Long = 3_000L,
    private val nowMillis: () -> Long = {
        System.currentTimeMillis()
    }
) : DevicesRepository {

    init {
        require(intervalMillis > 0)
    }

    override fun observeDevices(): Flow<List<Device>> = flow {
        var previous = emptyList<Device>()
        var phase = 0
        while (currentCoroutineContext().isActive) {
            previous = simulatedSnapshot(phase, previous, nowMillis())
            emit(previous)
            delay(intervalMillis)
            phase = (phase + 1) % PHASES
        }
    }

    companion object {
        const val PHASES = 6
        const val SENSOR_NODE_ID = "nodo-sensor-01"
        const val CENTRAL_NODE_ID = "nodo-central-01"
        const val PHONE_ID = "celular-01"
    }
}

// Función pura: dada la fase, entrega la foto de los dispositivos en ese instante.
// Fases 0 y 1: todo normal. Fase 2: temperatura alta. Fase 3: humedad baja.
// Fase 4: gas alto. Fase 5: el nodo sensor pierde la conexión.
fun simulatedSnapshot(
    phase: Int,
    previous: List<Device>,
    nowMillis: Long
): List<Device> {
    val previousReading = previous
        .firstOrNull { it.id == FakeDeviceRepository.SENSOR_NODE_ID }
        ?.reading

    val reading: SensorReading? = when (phase) {
        0 -> SensorReading(24.0, 45.0, 8.0, nowMillis)
        1 -> SensorReading(24.6, 44.0, 10.0, nowMillis)
        2 -> SensorReading(31.2, 41.0, 12.0, nowMillis)
        3 -> SensorReading(25.0, 27.0, 11.0, nowMillis)
        4 -> SensorReading(24.2, 46.0, 62.0, nowMillis)
        else -> previousReading // sin conexión: queda la última lectura guardada
    }
    val sensorConnection =
        if (phase in 0..4) ConnectionStatus.CONNECTED else ConnectionStatus.DISCONNECTED

    return listOf(
        Device(
            id = FakeDeviceRepository.SENSOR_NODE_ID,
            name = "Nodo Sensor ESP32",
            location = "Casa Abuela",
            connection = sensorConnection,
            kind = DeviceKind.SENSOR_NODE,
            reading = reading
        ),
        Device(
            id = FakeDeviceRepository.CENTRAL_NODE_ID,
            name = "Nodo Central ESP32",
            location = "Casa del familiar",
            connection = ConnectionStatus.CONNECTED,
            kind = DeviceKind.CENTRAL_NODE
        ),
        Device(
            id = FakeDeviceRepository.PHONE_ID,
            name = "Celular del cuidador",
            location = "Casa del familiar",
            connection = ConnectionStatus.CONNECTED,
            kind = DeviceKind.PHONE
        )
    )
}
