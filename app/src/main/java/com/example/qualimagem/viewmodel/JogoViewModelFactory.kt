package com.example.qualimagem.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.qualimagem.data.repository.PlacarRepository
import com.example.qualimagem.data.repository.QuizRepository

class JogoViewModelFactory(
    private val quizRepository: QuizRepository,
    private val placarRepository: PlacarRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(JogoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JogoViewModel(
                quizRepository,
                placarRepository
            ) as T
        }

        throw IllegalArgumentException("ViewModel desconhecido")
    }
}
