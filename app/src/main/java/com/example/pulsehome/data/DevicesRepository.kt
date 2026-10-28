package com.example.pulsehome.data

import com.example.pulsehome.model.Device
import kotlinx.coroutines.flow.Flow

interface DevicesRepository {
    fun observeDevices(): Flow<List<Device>>
}
