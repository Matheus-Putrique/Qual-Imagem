package com.example.qualimagem.data.repository

import com.example.qualimagem.data.PlacarDAO
import com.example.qualimagem.data.PlacarEntity
import kotlinx.coroutines.flow.Flow

class PlacarRepository(
    private val placarDao: PlacarDAO
) {
    fun buscarTodos(): Flow<List<PlacarEntity>> = placarDao.buscarTodos()

    suspend fun inserir(placar: PlacarEntity): Result<Unit> {
        return try {
            placarDao.inserir(placar)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun atualizar(placar: PlacarEntity): Result<Unit> {
        return try {
            placarDao.atualizar(placar)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletar(placar: PlacarEntity): Result<Unit> {
        return try {
            placarDao.deletar(placar)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}