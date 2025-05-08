package com.example.riskfree

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AmeacasReportadasActivity : AppCompatActivity() {

    private lateinit var linearLayoutAmeacas: LinearLayout
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ameacas_reportadas)

        // 1) container de ameaças
        linearLayoutAmeacas = findViewById(R.id.linearLayoutAmeacas)

        // 2) botão para adicionar nova ameaça
        val btnAddAmeaca: ImageButton = findViewById(R.id.btnAddAmeaca)
        val navegarPraReport = {
            startActivity(Intent(this, ReportActivity::class.java))
        }
        btnAddAmeaca.setOnClickListener { navegarPraReport() }

        // 3) busca e exibe as ameaças já reportadas
        fetchAmeacas()
    }

    private fun fetchAmeacas() {
        db.collection("ameaca")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(this, "Nenhuma ameaça encontrada.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                for (document in result.documents) {
                    val descricaoFull = document.getString("descricao") ?: ""
                    val snippet = if (descricaoFull.length > 20) {
                        descricaoFull.substring(0, 20) + "..."
                    } else {
                        descricaoFull
                    }

                    val tv = TextView(this).apply {
                        text = snippet
                        textSize = 16f
                        setPadding(0, 16, 0, 16)
                    }

                    linearLayoutAmeacas.addView(tv)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Erro ao carregar ameaças: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
