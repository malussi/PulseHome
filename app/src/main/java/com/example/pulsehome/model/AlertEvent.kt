package com.example.pulsehome.model

// Una alerta que el nodo sensor genera cuando una variable sale de rango.
data class AlertEvent(
    val variable: SensorVariable,
    val value: Double,
    val measuredAtMillis: Long,
    val title: String
)

// Compara la lectura anterior con la nueva y entrega solo las alertas que recién empiezan.
// Así, una variable que sigue fuera de rango no repite la alerta en cada lectura.
fun newAlerts(previous: SensorReading?, current: SensorReading): List<AlertEvent> =
    SensorVariable.entries.mapNotNull { variable ->
        val wasOutOfRange = previous?.isOutOfRange(variable) == true
        if (current.isOutOfRange(variable) && !wasOutOfRange) {
            AlertEvent(
                variable = variable,
                value = current.valueOf(variable),
                measuredAtMillis = current.measuredAtMillis,
                title = alertTitle(variable, current)
            )
        } else {
            null
        }
    }

private fun alertTitle(variable: SensorVariable, reading: SensorReading): String =
    when (variable) {
        SensorVariable.TEMPERATURE ->
            if (reading.temperatureCelsius > SensorReading.TEMPERATURE_MAX_CELSIUS) {
                "Temperatura alta"
            } else {
                "Temperatura baja"
            }
        SensorVariable.HUMIDITY -> "Humedad baja"
        SensorVariable.GAS -> "Gas alto"
    }
