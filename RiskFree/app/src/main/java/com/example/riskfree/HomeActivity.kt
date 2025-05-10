package com.example.riskfree

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        val btnReportarAmeaca: Button      = findViewById(R.id.btnReportarAmeaca)

        val btnAddAmeaca:     ImageButton = findViewById(R.id.btnAddAmeaca)

        val btnAmeacasReportadas : Button = findViewById(R.id.btnAmeacasReportadas)

        val navegarPraReport = {
            startActivity(Intent(this, ReportActivity::class.java))
        }
        val navegarPraAmeacasReportadas = {
            startActivity(Intent(this, AmeacasReportadasActivity::class.java))
        }

        btnReportarAmeaca.setOnClickListener { navegarPraReport() }
        btnAddAmeaca   .setOnClickListener { navegarPraReport() }
        btnAmeacasReportadas.setOnClickListener{navegarPraAmeacasReportadas()}
    }
}
