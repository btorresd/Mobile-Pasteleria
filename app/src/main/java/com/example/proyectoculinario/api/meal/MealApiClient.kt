package com.example.proyectoculinario.api.meal


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MealApiClient {
    private const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"

    val service: MealService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MealService::class.java)
    }
}

