package com.example.riskfree

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.ByteArrayOutputStream
import android.provider.MediaStore

class ReportActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ReportActivity"
    }

    private lateinit var etData: EditText
    private lateinit var etNivel: EditText
    private lateinit var etDescricao: EditText
    private lateinit var btnEnviar: Button
    private lateinit var flImageContainer: FrameLayout
    private lateinit var ivSelectedImage: ImageView
    private lateinit var ivCameraIcon: ImageView
    private lateinit var tvPlaceholder: TextView

    // Firebase
    private val db   = Firebase.firestore
    private val auth = Firebase.auth

    // Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // URI da imagem selecionada
    private var imageUri: Uri? = null

    // 1) Picker de imagem
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
                ivSelectedImage.setImageURI(it)
                ivSelectedImage.visibility = View.VISIBLE
                ivCameraIcon.visibility    = View.GONE
                tvPlaceholder.visibility   = View.GONE
            }
        }

    // 2) Permissão de localização em runtime
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) fetchLocationAndSubmit()
            else Toast.makeText(this, "Permissão de localização negada.", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_ameaca)

        // Init Location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Map views
        etData           = findViewById(R.id.etData)
        etNivel          = findViewById(R.id.etNivelAmeaca)
        etDescricao      = findViewById(R.id.etDescricaoAmeaca)
        btnEnviar        = findViewById(R.id.btnEnviarAmeaca)
        flImageContainer = findViewById(R.id.flImageContainer)
        ivSelectedImage  = findViewById(R.id.ivSelectedImage)
        ivCameraIcon     = findViewById(R.id.ivCameraIcon)
        tvPlaceholder    = findViewById(R.id.tvPlaceholder)

        // Escolher imagem
        flImageContainer.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Enviar ameaça
        btnEnviar.setOnClickListener {
            val data      = etData.text.toString().trim()
            val nivel     = etNivel.text.toString().trim()
            val descricao = etDescricao.text.toString().trim()

            if (data.isEmpty() || nivel.isEmpty() || descricao.isEmpty()) {
                Toast.makeText(this, "Preencha data, nível e descrição.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (imageUri == null) {
                Toast.makeText(this, "Insira uma imagem.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // checa permissão de localização
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                fetchLocationAndSubmit()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    // Usa getCurrentLocation para forçar leitura de GPS
    private fun fetchLocationAndSubmit() {
        val tokenSource = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            tokenSource.token
        )
            .addOnSuccessListener { location: Location? ->
                if (location != null) uploadImageAndSave(location)
                else {
                    Log.e(TAG, "Localização retornou nula")
                    Toast.makeText(
                        this,
                        "Não foi possível obter localização. Verifique o GPS.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro ao obter localização", e)
                Toast.makeText(
                    this,
                    "Erro ao obter localização: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }



    // Converte a imagem em Base64 e salva tudo no Firestore
    private fun uploadImageAndSave(location: Location) {
        val uri = imageUri
        if (uri == null) {
            Toast.makeText(this, "Selecione uma foto antes de enviar.", Toast.LENGTH_LONG).show()
            return
        }

        try {
            // 1) Carrega bitmap da URI
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)

            // 2) Reduz o tamanho e comprime em JPEG
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val imageBytes = outputStream.toByteArray()

            // 3) Converte para Base64
            val imageBase64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

            // 4) Prepara dados para o Firestore
            val data      = etData.text.toString().trim()
            val nivel     = etNivel.text.toString().trim()
            val descricao = etDescricao.text.toString().trim()
            val usuarioId = auth.currentUser?.uid

            val ameaca = hashMapOf(
                "data"        to data,
                "nivel"       to nivel,
                "descricao"   to descricao,
                "localizacao" to mapOf(
                    "latitude"  to location.latitude,
                    "longitude" to location.longitude
                ),
                "usuarioId"   to usuarioId,
                "imageBase64" to imageBase64
            )

            // 5) Grava no Firestore
            db.collection("ameaca")
                .add(ameaca)
                .addOnSuccessListener {
                    Toast.makeText(this, "Ameaça enviada com sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Erro ao salvar no Firestore", e)
                    Toast.makeText(this, "Erro ao enviar: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao processar imagem", e)
            Toast.makeText(this, "Erro ao processar imagem: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
