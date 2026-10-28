package com.example.pulsehome

import com.example.pulsehome.model.SensorVariable

class DetalleHumedadActivity : SensorDetailActivity() {
    override val layoutRes = R.layout.activity_detalle_humedad
    override val variable = SensorVariable.HUMIDITY
    override val previousScreen = DetalleTemperaturaActivity::class.java
    override val nextScreen = DetalleGasActivity::class.java
}
