package com.example.pulsehome

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.pulsehome.model.SensorStatus
import com.example.pulsehome.model.SensorVariable
import com.example.pulsehome.ui.viewModel.DevicesUiState
import com.example.pulsehome.ui.viewModel.estadoLabel
import com.example.pulsehome.ui.viewModel.formatNumber
import com.example.pulsehome.ui.viewModel.formatValue
import com.example.pulsehome.ui.viewModel.statusLabel
import com.example.pulsehome.ui.viewModel.timeAgo
import kotlinx.coroutines.launch

class pantallaInicioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupScreen(R.layout.activity_pantalla_inicio)
        setupBottomNav()

        findViewById<CardView>(R.id.cardTemperatura).setOnClickListener {
            startActivity(android.content.Intent(this, DetalleTemperaturaActivity::class.java))
        }
        findViewById<CardView>(R.id.cardHumedad).setOnClickListener {
            startActivity(android.content.Intent(this, DetalleHumedadActivity::class.java))
        }
        findViewById<CardView>(R.id.cardGas).setOnClickListener {
            startActivity(android.content.Intent(this, DetalleGasActivity::class.java))
        }

        // Mientras la pantalla está visible escucha al ViewModel; al salir, deja de escuchar.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                devicesViewModel.uiState.collect { render(it) }
            }
        }
    }

    private fun render(state: DevicesUiState) {
        findViewById<TextView>(R.id.tvResumenEstado).text =
            if (state.alertCount > 0) "Hay alertas" else "Todo en orden"
        findViewById<TextView>(R.id.tvConexion).text =
            if (state.connectedCount > 0) "Sistema conectado" else "Sin conexión"

        bindSensor(R.id.cardTemperatura, R.id.tvValorTemperatura, R.id.tvDetallesTemperatura,
            R.id.tvEstadoTemperatura, SensorVariable.TEMPERATURE, state)
        bindSensor(R.id.cardHumedad, R.id.tvValorHumedad, R.id.tvDetallesHumedad,
            R.id.tvEstadoHumedad, SensorVariable.HUMIDITY, state)
        bindSensor(R.id.cardGas, R.id.tvValorGas, R.id.tvDetallesGas,
            R.id.tvEstadoGas, SensorVariable.GAS, state)

        val lastAlert = state.alerts.firstOrNull()
        findViewById<TextView>(R.id.tvUltimaAlerta).text =
            if (lastAlert == null) {
                "Sin alertas por ahora"
            } else {
                "${lastAlert.title}: ${formatNumber(lastAlert.variable, lastAlert.value)} · " +
                    timeAgo(lastAlert.measuredAtMillis, System.currentTimeMillis())
            }
    }

    private fun bindSensor(
        cardId: Int, valueId: Int, detailId: Int, estadoId: Int,
        variable: SensorVariable, state: DevicesUiState
    ) {
        val sensor = state.sensorNode
        val status = sensor?.statusOf(variable) ?: SensorStatus.OFFLINE
        val colorRes = when (status) {
            SensorStatus.NORMAL -> R.color.ph_normal
            SensorStatus.ALERT -> R.color.ph_alerta
            SensorStatus.OFFLINE -> R.color.ph_borde
        }
        findViewById<CardView>(cardId)
            .setCardBackgroundColor(ContextCompat.getColor(this, colorRes))
        findViewById<TextView>(valueId).text = formatValue(variable, sensor?.reading)
        findViewById<TextView>(detailId).text = "Ver detalles · ${statusLabel(status)}"
        // La etiqueta de arriba a la derecha de la tarjeta sigue el estado real del sensor.
        findViewById<TextView>(estadoId).text = estadoLabel(status)
    }
}
