

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import com.example.proyectoculinario.api.vision.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

        // Aquí más adelante llamaremos a analizarImagen(bitmap)
    }

    // Convierte una imagen Bitmap en Base64 (texto)
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

        val call = VisionApiClient.service.analyzeImage(apiKey, request)

        call.enqueue(object : Callback<VisionResponse> {
            override fun onResponse(
                call: Call<VisionResponse>,
                response: Response<VisionResponse>
            ) {
                if (response.isSuccessful) {
                    val labels = response.body()
                        ?.responses
                        ?.firstOrNull()
                        ?.labelAnnotations

                    if (!labels.isNullOrEmpty()) {
                        val mejor = labels.maxByOrNull { it.score ?: 0f }
                        val nombreDetectado = mejor?.description ?: "Desconocido"
                        Log.d("VISION", "Detectado: $nombreDetectado")
                    } else {
                        Log.d("VISION", "No se detectaron etiquetas")
                    }
                } else {
                    Log.e("VISION", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<VisionResponse>, t: Throwable) {
                Log.e("VISION", "Fallo: ${t.message}")
            }
        })
    }
}