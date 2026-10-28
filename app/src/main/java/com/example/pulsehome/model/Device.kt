package com.example.pulsehome.model

// Estado de la conexión de un dispositivo con el sistema.
enum class ConnectionStatus {
    CONNECTED, DISCONNECTED
}

// Tipo de dispositivo que muestra PulseHome.
enum class DeviceKind {
    SENSOR_NODE,   // Nodo sensor ESP32 (DHT22 y MQ-2) en la casa del adulto mayor
    CENTRAL_NODE,  // Nodo central ESP32 en la casa del familiar
    PHONE          // Celular con la app instalada
}

// Variables que mide el nodo sensor.
enum class SensorVariable(val label: String) {
    TEMPERATURE("Temperatura"),
    HUMIDITY("Humedad"),
    GAS("Gas")
}

// Estado de una variable para pintar la tarjeta: verde, roja o gris.
enum class SensorStatus {
    NORMAL, ALERT, OFFLINE
}

data class SensorReading(
    val temperatureCelsius: Double,
    val relativeHumidity: Double,
    val gasPercent: Double,
    val measuredAtMillis: Long
) {
    // Programar los rangos de medición. Un solo lugar para cambiar los umbrales.
    companion object {
        const val TEMPERATURE_MIN_CELSIUS = 15.0
        const val TEMPERATURE_MAX_CELSIUS = 30.0
        const val HUMIDITY_MIN_PERCENT = 30.0
        // Gas como % del límite inferior de explosividad (%LEL). Alarma baja: 20 %.
        const val GAS_MAX_PERCENT = 20.0
    }

    val isTemperatureOutOfRange: Boolean
        get() = temperatureCelsius !in TEMPERATURE_MIN_CELSIUS..TEMPERATURE_MAX_CELSIUS

    val isHumidityOutOfRange: Boolean
        get() = relativeHumidity < HUMIDITY_MIN_PERCENT

    val isGasOutOfRange: Boolean
        get() = gasPercent > GAS_MAX_PERCENT

    val isOutsideDemoRange: Boolean
        get() = isTemperatureOutOfRange || isHumidityOutOfRange || isGasOutOfRange

    fun isOutOfRange(variable: SensorVariable): Boolean = when (variable) {
        SensorVariable.TEMPERATURE -> isTemperatureOutOfRange
        SensorVariable.HUMIDITY -> isHumidityOutOfRange
        SensorVariable.GAS -> isGasOutOfRange
    }

    fun valueOf(variable: SensorVariable): Double = when (variable) {
        SensorVariable.TEMPERATURE -> temperatureCelsius
        SensorVariable.HUMIDITY -> relativeHumidity
        SensorVariable.GAS -> gasPercent
    }
}

data class Device(
    val id: String,
    val name: String,
    val location: String,
    val connection: ConnectionStatus,
    val kind: DeviceKind = DeviceKind.SENSOR_NODE,
    val reading: SensorReading? = null
) {
    val hasCurrentAlert: Boolean
        get() = connection == ConnectionStatus.CONNECTED &&
                reading?.isOutsideDemoRange == true

    // Estado de una variable concreta: sirve para las tarjetas de Inicio y de Detalle.
    fun statusOf(variable: SensorVariable): SensorStatus = when {
        connection != ConnectionStatus.CONNECTED || reading == null -> SensorStatus.OFFLINE
        reading.isOutOfRange(variable) -> SensorStatus.ALERT
        else -> SensorStatus.NORMAL
    }
}
