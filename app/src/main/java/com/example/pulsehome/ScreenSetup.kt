package com.example.pulsehome

import android.content.Intent
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pulsehome.ui.viewModel.DevicesViewModel

// Funciones comunes a todas las pantallas, para no repetir el mismo código en cada Activity.

val AppCompatActivity.devicesViewModel: DevicesViewModel
    get() = (application as PulseHomeApp).devicesViewModel

// Carga el layout y aplica los márgenes de las barras del sistema (edge-to-edge).
fun AppCompatActivity.setupScreen(layoutRes: Int) {
    enableEdgeToEdge()
    setContentView(layoutRes)
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
        insets
    }
}

// Barra inferior: lleva a cada sección sin apilar pantallas repetidas.
fun AppCompatActivity.setupBottomNav() {
    goOnClick(R.id.navInicio, pantallaInicioActivity::class.java)
    goOnClick(R.id.navHistorial, HistorialActivity::class.java)
    goOnClick(R.id.navNotificaciones, NotificacionesActivity::class.java)
    goOnClick(R.id.navDispositivos, DispositivosActivity::class.java)
    goOnClick(R.id.navPerfil, PerfilActivity::class.java)
}

private fun AppCompatActivity.goOnClick(viewId: Int, target: Class<*>) {
    // findViewById devuelve null si la pantalla no tiene ese id (ej. navPerfil2 en Dispositivos)
    findViewById<View?>(viewId)?.setOnClickListener {
        if (this::class.java != target) {
            startActivity(
                Intent(this, target).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            )
        }
    }
}
