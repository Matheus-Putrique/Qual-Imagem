package com.example.qualimagem.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlacarDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(placar: PlacarEntity)

    @Update
    suspend fun atualizar(placar: PlacarEntity)

    @Delete
    suspend fun deletar(placar: PlacarEntity)

    @Query("SELECT * FROM placares")
    fun buscarTodos(): Flow<List<PlacarEntity>>
}
