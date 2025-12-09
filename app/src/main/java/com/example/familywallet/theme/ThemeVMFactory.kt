package com.example.familywallet.theme

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// Factory para crear ThemeViewModel inyectando el ThemeRepository con el contexto de la app
class ThemeVMFactory(private val app: Application) : ViewModelProvider.Factory {

    // Genera una instancia de ThemeViewModel cuando se solicita desde viewModel(...)
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ThemeViewModel(ThemeRepository(app.applicationContext)) as T
    }
}


