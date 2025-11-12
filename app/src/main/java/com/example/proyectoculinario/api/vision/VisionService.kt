package com.example.proyectoculinario.api.vision


import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface VisionService {

    @POST("v1/images:annotate")
    fun analyzeImage(
        @Query("key") apiKey: String,
        @Body request: VisionRequest
    ): Call<VisionResponse>
}
