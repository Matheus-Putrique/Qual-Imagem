package com.example.qualimagem.data.repository

import android.util.Log
import com.example.qualimagem.data.ItemQuizDAO
import com.example.qualimagem.data.ItemQuizEntity
import com.example.qualimagem.data.remote.PaisesApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class QuizRepository(
    private val itemQuizDao: ItemQuizDAO,
    private val apiService: PaisesApiService
) {
    suspend fun sincronizarBandeirasDaApi(): Result<Unit> {
        return try {
            val paises = apiService.getPaises()
            val nomesExistentes = itemQuizDao.buscarTodos().first()
                .map { it.nomeCorreto }
                .toSet()

            val itens = paises.map { dto ->
                val dificuldade = when {
                    dto.population > 50000000 -> "Fácil"
                    dto.population > 10000000 -> "Médio"
                    else -> "Difícil"
                }

                ItemQuizEntity(
                    nomeCorreto = dto.translations.por.common,
                    imagemUrl = dto.flags.png,
                    categoria = "Bandeiras",
                    subcategoria = dto.region,
                    dica = "Capital: ${dto.capital?.firstOrNull() ?: "Desconhecida"}",
                    dificuldade = dificuldade
                )
            }.filter { it.nomeCorreto !in nomesExistentes }

            itens.forEach { itemQuizDao.inserir(it) }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("QuizRepository", "Erro ao sincronizar API: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun inserirItem(item: ItemQuizEntity): Result<Unit> = try {
        itemQuizDao.inserir(item)
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("QuizRepository", "Erro ao inserir item: ${e.message}")
        Result.failure(e)
    }

    suspend fun atualizarItem(item: ItemQuizEntity): Result<Unit> = try {
        itemQuizDao.atualizar(item)
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("QuizRepository", "Erro ao atualizar item: ${e.message}")
        Result.failure(e)
    }

    suspend fun deletarItem(item: ItemQuizEntity): Result<Unit> = try {
        itemQuizDao.deletar(item)
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("QuizRepository", "Erro ao deletar item: ${e.message}")
        Result.failure(e)
    }

    fun buscarTodos(): Flow<List<ItemQuizEntity>> = itemQuizDao.buscarTodos()

    fun buscarPorCategoria(categoria: String): Flow<List<ItemQuizEntity>> =
        itemQuizDao.buscarPorCategoria(categoria)
}
