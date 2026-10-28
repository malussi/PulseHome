package com.example.pulsehome

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pulsehome.data.DevicesRepository
import com.example.pulsehome.data.FakeDeviceRepository
import com.example.pulsehome.ui.viewModel.DevicesViewModel

// La aplicación guarda UN solo DevicesViewModel para que todas las pantallas
// (Inicio, detalles, historial...) vean la misma simulación y el mismo historial de alertas.
// Si cada Activity creara el suyo, cada una tendría su propia simulación distinta.
class PulseHomeApp : Application(), ViewModelStoreOwner {

    override val viewModelStore = ViewModelStore()

    val devicesViewModel: DevicesViewModel by lazy {
        // Punto de sustitución de la fuente de datos (igual que en la guía de Kotlin):
        // más adelante aquí se cambia FakeDeviceRepository por el repositorio MQTT.
        val repository: DevicesRepository = FakeDeviceRepository()
        val factory = viewModelFactory {
            initializer { DevicesViewModel(repository) }
        }
        ViewModelProvider(this, factory)[DevicesViewModel::class.java]
    }
}
