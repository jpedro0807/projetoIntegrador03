package com.example.riskfree

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
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

        // Configura navegação da barra inferior
        setupNavigationBar()
    }

    private fun fetchAmeacas() {
        db.collection("ameaca")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(this, "Nenhuma ameaça encontrada.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Para cada documento de ameaça encontrado
                for (document in result.documents) {
                    val descricaoFull = document.getString("descricao") ?: ""
                    // O snippet já contém os primeiros 20 caracteres ou a descrição completa
                    val snippet = if (descricaoFull.length > 20) {
                        descricaoFull.substring(0, 20) + "..."
                    } else {
                        descricaoFull
                    }

                    // Criando o CardView
                    val cardView = CardView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            // Adiciona uma margem inferior para espaçamento entre os cards
                            bottomMargin = 16 // em pixels, ajuste conforme necessário
                        }
                        radius = 16f
                        cardElevation = 4f
                        setContentPadding(16, 16, 16, 16)
                    }

                    // Criando o LinearLayout interno para texto
                    val layout = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(0, 0, 0, 8)
                    }

                    val titulo = TextView(this).apply {
                        // Agora o título será o snippet da descrição
                        text = snippet
                        textSize = 18f
                        setTextColor(resources.getColor(android.R.color.black))
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }


                    val descricao = TextView(this).apply {
                        text = snippet
                        textSize = 14f
                        setTextColor(resources.getColor(android.R.color.darker_gray))
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }

                    layout.addView(titulo)
                    layout.addView(descricao)

                    // Adicionando a LinearLayout ao CardView
                    cardView.addView(layout)

                    // Adicionando o CardView ao LinearLayout principal
                    linearLayoutAmeacas.addView(cardView)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar ameaças: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setupNavigationBar() {
        val btnHomeAmeacas: ImageButton = findViewById(R.id.btnHomeAmeacas)
        btnHomeAmeacas.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        val btnAddAmeaca: ImageButton = findViewById(R.id.btnAddAmeaca)
        btnAddAmeaca.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
            finish()
        }

    }
}
