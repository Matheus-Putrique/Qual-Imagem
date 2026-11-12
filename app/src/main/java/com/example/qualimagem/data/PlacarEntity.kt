package com.example.qualimagem.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "placares")
data class PlacarEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nomeJogador: String,
    val categoriaJogada: String,
    val pontuacao: Int,
    val acertos: Int,
    val dificuldade: String,
    val dataHora: Long
)
