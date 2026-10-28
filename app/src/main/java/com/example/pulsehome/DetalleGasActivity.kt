package com.example.pulsehome

import com.example.pulsehome.model.SensorVariable

class DetalleGasActivity : SensorDetailActivity() {
    override val layoutRes = R.layout.activity_detalle_gas
    override val variable = SensorVariable.GAS
    override val previousScreen = DetalleHumedadActivity::class.java
    override val nextScreen = DetalleTemperaturaActivity::class.java
}
