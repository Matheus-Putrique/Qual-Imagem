package com.example.qualimagem.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemQuizDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(item: ItemQuizEntity)

    @Update
    suspend fun atualizar(item: ItemQuizEntity)

    @Delete
    suspend fun deletar(item: ItemQuizEntity)

    @Query("SELECT * FROM itens_quiz WHERE id = :id")
    suspend fun buscarPorId(id: Int): ItemQuizEntity?

    @Query("SELECT * FROM itens_quiz WHERE categoria = :categoria")
    fun buscarPorCategoria(categoria: String): Flow<List<ItemQuizEntity>>

    @Query("SELECT * FROM itens_quiz")
    fun buscarTodos(): Flow<List<ItemQuizEntity>>
}
