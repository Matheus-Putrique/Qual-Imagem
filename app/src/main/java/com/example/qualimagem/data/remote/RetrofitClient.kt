package com.example.qualimagem.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://gitlab.com/restcountries/restcountries/-/raw/master/src/main/resources/"

    val apiService: PaisesApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PaisesApiService::class.java)
    }
}
