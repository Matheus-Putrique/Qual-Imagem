package com.example.qualimagem.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.qualimagem.data.repository.PlacarRepository
import com.example.qualimagem.data.repository.QuizRepository

class RankingViewModelFactory(
    private val placarRepository: PlacarRepository,
    private val quizRepository: QuizRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RankingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RankingViewModel(placarRepository, quizRepository) as T
        }

        throw IllegalArgumentException("ViewModel desconhecido")
    }
}
