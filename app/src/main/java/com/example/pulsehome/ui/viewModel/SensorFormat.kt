package com.example.pulsehome.ui.viewModel

import com.example.pulsehome.model.SensorReading
import com.example.pulsehome.model.SensorStatus
import com.example.pulsehome.model.SensorVariable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Funciones puras que convierten los datos del modelo en texto para las pantallas.
// Viven aparte de las Activities para poder probarlas con tests unitarios.

fun formatValue(
    variable: SensorVariable,
    reading: SensorReading?,
    locale: Locale = Locale.getDefault()
): String {
    if (reading == null) return "--"
    return formatNumber(variable, reading.valueOf(variable), locale)
}

// Da formato a un número suelto según la variable (se usa también para las alertas).
fun formatNumber(
    variable: SensorVariable,
    value: Double,
    locale: Locale = Locale.getDefault()
): String = when (variable) {
    SensorVariable.TEMPERATURE -> String.format(locale, "%.1f°C", value)
    SensorVariable.HUMIDITY, SensorVariable.GAS -> String.format(locale, "%.0f%%", value)
}

// Texto del límite de alerta que se muestra en el detalle de cada sensor.
fun formatLimit(variable: SensorVariable, locale: Locale = Locale.getDefault()): String =
    when (variable) {
        SensorVariable.TEMPERATURE -> String.format(
            locale, "%.0f°C a %.0f°C",
            SensorReading.TEMPERATURE_MIN_CELSIUS,
            SensorReading.TEMPERATURE_MAX_CELSIUS
        )
        SensorVariable.HUMIDITY -> String.format(
            locale, "%.0f%%", SensorReading.HUMIDITY_MIN_PERCENT
        )
        SensorVariable.GAS -> String.format(
            locale, "%.0f%%", SensorReading.GAS_MAX_PERCENT
        )
    }

fun formatTime(epochMillis: Long, locale: Locale = Locale.getDefault()): String =
    SimpleDateFormat("HH:mm:ss", locale).format(Date(epochMillis))

// "hace 5 s", "hace 3 min" o "hace 2 h".
fun timeAgo(measuredAtMillis: Long, nowMillis: Long): String {
    val seconds = ((nowMillis - measuredAtMillis) / 1_000L).coerceAtLeast(0L)
    return when {
        seconds < 60L -> "hace $seconds s"
        seconds < 3_600L -> "hace ${seconds / 60L} min"
        else -> "hace ${seconds / 3_600L} h"
    }
}

fun statusLabel(status: SensorStatus): String = when (status) {
    SensorStatus.NORMAL -> "Activo"
    SensorStatus.ALERT -> "En alerta"
    SensorStatus.OFFLINE -> "Sin conexión"
}

// Texto de la insignia de la tarjeta de lectura en las pantallas de detalle.
fun badgeLabel(status: SensorStatus): String = when (status) {
    SensorStatus.NORMAL -> "Operativo"
    SensorStatus.ALERT -> "Alerta"
    SensorStatus.OFFLINE -> "Sin conexión"
}

// Texto de la etiqueta de cada tarjeta en la pantalla de Inicio (tvEstadoTemperatura, tvEstadoHumedad, tvEstadoGas).
fun estadoLabel(status: SensorStatus): String = when (status) {
    SensorStatus.NORMAL -> "Normal"
    SensorStatus.ALERT -> "Alerta"
    SensorStatus.OFFLINE -> "Sin conexión"
}
