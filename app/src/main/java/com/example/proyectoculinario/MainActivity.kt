package com.example.proyectoculinario

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.proyectoculinario.api.vision.*
import com.example.proyectoculinario.api.meal.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream



class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var btnCapturar: Button

    // Inicialización de servicios API
    private val visionApi = VisionApiClient.service
    private val mealApi = MealApiClient.service

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        // Referencias a elementos del layout
        imageView = findViewById(R.id.imageView)
        btnCapturar = findViewById(R.id.btnCapturar)

        // Acción del botón para tomar una foto
        btnCapturar.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            resultLauncher.launch(intent)
        }
    }

    // Lanza la cámara y recibe el resultado (foto)
    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
            analizarImagen(imageBitmap)
        }
    }

    // Convierte una imagen a Base64 para enviarla a la API
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    // Envía la imagen a la API de Google Vision
    private fun analizarImagen(bitmap: Bitmap) {
        val apiKey = getString(R.string.google_vision_api_key)
        val base64 = bitmapToBase64(bitmap)

        val image = ImageContent(content = base64)
        val feature = Feature(type = "LABEL_DETECTION", maxResults = 5)
        val request = VisionRequest(
            requests = listOf(
                AnnotateImageRequest(
                    image = image,
                    features = listOf(feature)
                )
            )
        )

        visionApi.analyzeImage(apiKey, request)
            .enqueue(object : Callback<VisionResponse> {
                override fun onResponse(
                    call: Call<VisionResponse>,
                    response: Response<VisionResponse>
                ) {
                    if (response.isSuccessful) {
                        val labels = response.body()?.responses?.firstOrNull()?.labelAnnotations
                        val mejor = labels?.maxByOrNull { it.score ?: 0f }
                        val nombreDetectado = mejor?.description ?: "Desconocido"

                        Log.d("VISION", "Detectado: $nombreDetectado")
                        buscarReceta(nombreDetectado)
                    } else {
                        Log.e("VISION", "Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<VisionResponse>, t: Throwable) {
                    Log.e("VISION", "Fallo: ${t.message}")
                }
            })
    }

    // Busca una receta usando TheMealDB
    private fun buscarReceta(nombre: String) {
        mealApi.searchMeal(nombre).enqueue(object : Callback<MealResponse> {
            override fun onResponse(call: Call<MealResponse>, response: Response<MealResponse>) {
                if (response.isSuccessful) {
                    val meal = response.body()?.meals?.firstOrNull()
                    if (meal != null) {
                        Log.i("MEAL", "🍽️ Receta encontrada: ${meal.strMeal}")
                        Log.i("MEAL", "📝 Instrucciones: ${meal.strInstructions}")
                        Log.i("MEAL", "📸 Imagen: ${meal.strMealThumb}")
                    } else {
                        Log.i("MEAL", "No se encontró receta para: $nombre")
                    }
                } else {
                    Log.e("MEAL", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<MealResponse>, t: Throwable) {
                Log.e("MEAL", "Fallo conexión: ${t.message}")
            }
        })
    }
}
