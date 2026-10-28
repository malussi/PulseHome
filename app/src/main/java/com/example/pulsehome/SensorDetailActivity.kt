package com.example.pulsehome

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
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
import com.example.pulsehome.ui.viewModel.badgeLabel
import com.example.pulsehome.ui.viewModel.formatLimit
import com.example.pulsehome.ui.viewModel.formatTime
import com.example.pulsehome.ui.viewModel.formatValue
import com.example.pulsehome.ui.viewModel.statusLabel
import com.example.pulsehome.ui.viewModel.timeAgo
import kotlinx.coroutines.launch

// Las tres pantallas de detalle (temperatura, humedad y gas) tienen los mismos ids,
// así que comparten esta clase base y solo cambian el layout y la variable que muestran.
abstract class SensorDetailActivity : AppCompatActivity() {

    protected abstract val layoutRes: Int
    protected abstract val variable: SensorVariable
    protected abstract val previousScreen: Class<*>
    protected abstract val nextScreen: Class<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupScreen(layoutRes)
        setupBottomNav()

        findViewById<ImageView>(R.id.imgVolver).setOnClickListener { finish() }
        findViewById<ImageView>(R.id.imgAnterior).setOnClickListener { goTo(previousScreen) }
        findViewById<ImageView>(R.id.imgSiguiente).setOnClickListener { goTo(nextScreen) }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                devicesViewModel.uiState.collect { render(it) }
            }
        }
    }

    private fun goTo(target: Class<*>) {
        startActivity(Intent(this, target))
        finish() // así las flechas no apilan pantallas
    }

    private fun render(state: DevicesUiState) {
        val sensor = state.sensorNode
        val reading = sensor?.reading
        val status = sensor?.statusOf(variable) ?: SensorStatus.OFFLINE

        findViewById<TextView>(R.id.tvTituloSensor).text = variable.label
        findViewById<TextView>(R.id.tvNombreSensor).text = sensor?.name ?: "Sin dispositivo"
        findViewById<TextView>(R.id.tvUbicacionEstado).text =
            "${sensor?.location ?: "--"} · ${statusLabel(status)}"
        findViewById<TextView>(R.id.tvValor).text = formatValue(variable, reading)

        // Un sensor desconectado conserva su última lectura, pero no se presenta como actual.
        findViewById<TextView>(R.id.tvUltimaLectura).text = when {
            reading == null -> "Sin lecturas disponibles"
            status == SensorStatus.OFFLINE ->
                "Última lectura guardada (${formatTime(reading.measuredAtMillis)}); no es actual"
            else ->
                "Lectura: ${formatTime(reading.measuredAtMillis)} · " +
                    timeAgo(reading.measuredAtMillis, System.currentTimeMillis())
        }

        // Insignia de estado: el texto dice lo mismo que el color.
        findViewById<TextView>(R.id.tvConexion).text = badgeLabel(status)
        findViewById<CardView>(R.id.cardConexion).setCardBackgroundColor(
            ContextCompat.getColor(this, when (status) {
                SensorStatus.NORMAL -> R.color.ph_normal
                SensorStatus.ALERT -> R.color.ph_alerta
                SensorStatus.OFFLINE -> R.color.ph_borde
            })
        )

        findViewById<TextView>(R.id.tvUbicacion).text = sensor?.location ?: "--"
        findViewById<TextView>(R.id.tvEstado).text = statusLabel(status)
        findViewById<TextView>(R.id.tvRango).text = formatLimit(variable)
    }
}
