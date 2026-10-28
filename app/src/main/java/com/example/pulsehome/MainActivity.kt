package com.example.pulsehome

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

// Pantalla de arranque: por ahora solo deriva al Login (la validación se verá después).
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
