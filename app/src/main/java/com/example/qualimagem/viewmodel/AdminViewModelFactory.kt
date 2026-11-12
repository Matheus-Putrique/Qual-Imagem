package com.example.qualimagem.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.qualimagem.data.repository.QuizRepository

class AdminViewModelFactory(
    private val quizRepository: QuizRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminViewModel(quizRepository) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
}
