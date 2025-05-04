package com.example.riskfree

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa o FirebaseAuth
        auth = Firebase.auth

        // Botão de login
        findViewById<Button>(R.id.buttonLogin).setOnClickListener {
            val email = findViewById<EditText>(R.id.editEmail).text.toString()
            val password = findViewById<EditText>(R.id.editPassword).text.toString()
            loginEmailSenha(email, password)
        }

        // Botão de cadastro
        findViewById<Button>(R.id.buttonCreateAccount).setOnClickListener {
            val email = findViewById<EditText>(R.id.editEmail).text.toString()
            val password = findViewById<EditText>(R.id.editPassword).text.toString()
            criarContaEmailSenha(email, password)
        }
    }

    private fun loginEmailSenha(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login bem-sucedido
                    val user = auth.currentUser
                    Toast.makeText(this, "Olá ${user?.email}", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Se falhar, mostra o erro
                    Toast.makeText(this, "Erro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun criarContaEmailSenha(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Conta criada
                    val user = auth.currentUser
                    Toast.makeText(this, "Conta criada: ${user?.email}", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Erro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    // Para logout:
    private fun logout() {
        auth.signOut()
        // Redirecionar para tela de login
    }
}
/*
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val emailEditText = findViewById<EditText>(R.id.editTextText)
        val senhaEditText = findViewById<EditText>(R.id.editTextText2)
        val loginButton = findViewById<Button>(R.id.buttonLogin)
        //val createAccountButton = findViewById<Button>(R.id.buttonCreateAccount)
        //val forgotPasswordText = findViewById<TextView>(R.id.textForgotPassword)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val senha = senhaEditText.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            } else {
                val db = Firebase.firestore

                db.collection("usuario")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener { result ->
                        if (result.isEmpty) {
                            Toast.makeText(this, "Email não encontrado!", Toast.LENGTH_SHORT).show()
                        } else {
                            val userDoc = result.documents.first()
                            val senhaSalva = userDoc.getString("senha")

                            if (senha == senhaSalva) {
                                Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this, "Senha incorreta!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                        Toast.makeText(this, "Erro ao acessar o banco: ${e.message}", Toast.LENGTH_LONG).show()
                    }

            }
        }


    }
}
*/