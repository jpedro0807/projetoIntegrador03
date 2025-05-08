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

        // se btnReportarAmeaca for realmente um <Button> no XML, ok
        val btnReportarAmeaca: Button      = findViewById(R.id.btnReportarAmeaca)
        // mas se btnAddAmeaca for um <ImageButton>, declare como ImageButton
        val btnAddAmeaca:     ImageButton = findViewById(R.id.btnAddAmeaca)

        val navegarPraReport = {
            startActivity(Intent(this, ReportActivity::class.java))
        }

        btnReportarAmeaca.setOnClickListener { navegarPraReport() }
        btnAddAmeaca   .setOnClickListener { navegarPraReport() }
    }
}
