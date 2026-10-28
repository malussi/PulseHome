package com.example.pulsehome

import com.example.pulsehome.model.SensorVariable

class DetalleTemperaturaActivity : SensorDetailActivity() {
    override val layoutRes = R.layout.activity_detalle_temperatura
    override val variable = SensorVariable.TEMPERATURE
    override val previousScreen = DetalleGasActivity::class.java
    override val nextScreen = DetalleHumedadActivity::class.java
}
