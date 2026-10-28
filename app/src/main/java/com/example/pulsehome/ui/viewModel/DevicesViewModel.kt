package com.example.pulsehome.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulsehome.data.DevicesRepository
import com.example.pulsehome.model.AlertEvent
import com.example.pulsehome.model.ConnectionStatus
import com.example.pulsehome.model.Device
import com.example.pulsehome.model.DeviceKind
import com.example.pulsehome.model.newAlerts
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn

data class DevicesUiState(
    val isLoading: Boolean = true,
    val devices: List<Device> = emptyList(),
    val alerts: List<AlertEvent> = emptyList(), // la más reciente primero
    val errorMessage: String? = null
) {
    val connectedCount: Int
        get() = devices.count {
            it.connection == ConnectionStatus.CONNECTED
        }
    val alertCount: Int
        get() = devices.count { it.hasCurrentAlert }

    // El nodo sensor es el dispositivo que entrega temperatura, humedad y gas.
    val sensorNode: Device?
        get() = devices.firstOrNull { it.kind == DeviceKind.SENSOR_NODE }

    companion object {
        const val MAX_ALERTS = 20
    }
}

// Entrega el estado nuevo a partir del anterior y de la última foto de dispositivos.
// Guarda en el historial solo las alertas que recién empiezan.
fun DevicesUiState.withDevices(newDevices: List<Device>): DevicesUiState {
    val previousReading = sensorNode?.reading
    val currentReading = newDevices
        .firstOrNull { it.kind == DeviceKind.SENSOR_NODE }
        ?.takeIf { it.connection == ConnectionStatus.CONNECTED }
        ?.reading
    val fresh = if (currentReading != null) newAlerts(previousReading, currentReading) else emptyList()
    return DevicesUiState(
        isLoading = false,
        devices = newDevices,
        alerts = (fresh + alerts).take(DevicesUiState.MAX_ALERTS)
    )
}

class DevicesViewModel(
    repository: DevicesRepository
) : ViewModel() {
    val uiState: StateFlow<DevicesUiState> = repository
        .observeDevices()
        .scan(DevicesUiState()) { state, devices ->
            state.withDevices(devices)
        }
        .catch {
            emit(
                DevicesUiState(
                    isLoading = false,
                    errorMessage = "No se pudieron cargar los datos."
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DevicesUiState()
        )
}
