package com.example.pulsehome

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class HistorialActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupScreen(R.layout.activity_historial)
        setupBottomNav()
    }
}
