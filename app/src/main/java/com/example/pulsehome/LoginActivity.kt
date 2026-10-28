package com.example.pulsehome

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupScreen(R.layout.activity_login)

        // Por indicación del docente, el login no valida todavía: solo entra a Inicio.
        findViewById<View>(R.id.btnIngresar).setOnClickListener {
            startActivity(Intent(this, pantallaInicioActivity::class.java))
            finish()
        }
        findViewById<View>(R.id.tvIrRegistro).setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }
}
