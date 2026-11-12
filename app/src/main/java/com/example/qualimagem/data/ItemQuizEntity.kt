package com.example.qualimagem.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "itens_quiz")
data class ItemQuizEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nomeCorreto: String,
    val imagemUrl: String,
    val categoria: String = "Bandeiras",
    val subcategoria: String,
    val dica: String,
    val dificuldade: String
)
