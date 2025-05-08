package com.example.riskfree

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val btnReportarAmeaca: Button = findViewById(R.id.btnReportarAmeaca)
        val btnAddAmeaca:    Button = findViewById(R.id.btnAddAmeaca)

        val navegarPraReport = {
            startActivity(Intent(this, ReportActivity::class.java))
        }

        btnReportarAmeaca.setOnClickListener { navegarPraReport() }
        btnAddAmeaca   .setOnClickListener { navegarPraReport() }
    }

}
