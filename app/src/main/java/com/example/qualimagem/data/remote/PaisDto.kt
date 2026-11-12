package com.example.qualimagem.data.remote

import com.google.gson.annotations.SerializedName

data class PaisDto(
    val translations: Translations,
    val flags: Flags,
    val region: String,
    val capital: List<String>?,
    val population: Long
)

data class Translations(
    val por: TranslationPor
)

data class TranslationPor(
    val common: String
)

data class Flags(
    val png: String
)
