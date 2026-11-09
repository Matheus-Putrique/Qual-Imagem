package com.example.qualimagem.data.remote

import retrofit2.http.GET

interface PaisesApiService {
    @GET("countriesV3.1.json")
    suspend fun getPaises(): List<PaisDto>
}
