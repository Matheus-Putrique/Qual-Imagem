package com.example.qualimagem.data.remote

import retrofit2.http.GET

interface PaisesApiService {
    @GET("v3.1/all")
    suspend fun getPaises(): List<PaisDto>
}