package com.example.proyectoculinario.api.meal

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MealService {
    @GET("search.php")
    fun searchMeal(@Query("s") query: String): Call<MealResponse>
}
