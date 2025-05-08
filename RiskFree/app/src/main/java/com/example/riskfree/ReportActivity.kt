package com.example.riskfree

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.*

class ReportActivity : AppCompatActivity() {

    // Views
    private lateinit var etData: EditText
    private lateinit var etNivel: EditText
    private lateinit var etDescricao: EditText
    private lateinit var btnEnviar: Button

    private lateinit var flImageContainer: FrameLayout
    private lateinit var ivSelectedImage: ImageView
    private lateinit var ivCameraIcon: ImageView
    private lateinit var tvPlaceholder: TextView

    // Firebase
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage

    // Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Para manter a URI da imagem escolhida
    private var imageUri: Uri? = null

    // 1) Picker de imagem
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
                ivSelectedImage.setImageURI(it)
                ivSelectedImage.visibility = View.VISIBLE
                ivCameraIcon.visibility = View.GONE
                tvPlaceholder.visibility = View.GONE
            }
        }

    // 2) Solicitação de permissão de localização
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) fetchLocationAndSubmit()
            else Toast.makeText(this, "Permissão de localização negada.", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_ameaca)

        // Instancia o cliente de localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Mapeia as views
        etData           = findViewById(R.id.etData)
        etNivel          = findViewById(R.id.etNivelAmeaca)
        etDescricao      = findViewById(R.id.etDescricaoAmeaca)
        btnEnviar        = findViewById(R.id.btnEnviarAmeaca)

        flImageContainer = findViewById(R.id.flImageContainer)
        ivSelectedImage  = findViewById(R.id.ivSelectedImage)
        ivCameraIcon     = findViewById(R.id.ivCameraIcon)
        tvPlaceholder    = findViewById(R.id.tvPlaceholder)

        // Ao clicar no container, abre o seletor de imagens
        flImageContainer.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Ao clicar em Enviar
        btnEnviar.setOnClickListener {
            val data      = etData.text.toString().trim()
            val nivel     = etNivel.text.toString().trim()
            val descricao = etDescricao.text.toString().trim()

            // Valida campos
            if (data.isEmpty() || nivel.isEmpty() || descricao.isEmpty()) {
                Toast.makeText(this, "Preencha data, nível e descrição.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (imageUri == null) {
                Toast.makeText(this, "Insira uma imagem.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Verifica permissão de localização
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                fetchLocationAndSubmit()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    // Obtém a última localização conhecida e segue para upload
    private fun fetchLocationAndSubmit() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location == null) {
                    Toast.makeText(this, "Não foi possível obter localização.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                uploadImageAndSave(location)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao obter localização.", Toast.LENGTH_SHORT).show()
            }
    }

    // Faz o upload da imagem para o Storage e obtém sua URL
    private fun uploadImageAndSave(location: Location) {
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val storageRef = storage.reference.child("ameaca_images/$fileName")
        val uri = imageUri ?: return

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                        saveToFirestore(location, downloadUri.toString())
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao obter URL da imagem: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao enviar imagem: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Salva o registro no Firestore com todos os campos
    private fun saveToFirestore(location: Location, imageUrl: String) {
        val data      = etData.text.toString().trim()
        val nivel     = etNivel.text.toString().trim()
        val descricao = etDescricao.text.toString().trim()

        val ameaca = hashMapOf(
            "data"        to data,
            "nivel"       to nivel,
            "localizacao" to mapOf(
                "latitude"  to location.latitude,
                "longitude" to location.longitude
            ),
            "descricao"   to descricao,
            "usuarioId"   to auth.currentUser?.uid,
            "imageUrl"    to imageUrl
        )

        db.collection("ameaca")
            .add(ameaca)
            .addOnSuccessListener {
                Toast.makeText(this, "Ameaça enviada com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao enviar dados: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
