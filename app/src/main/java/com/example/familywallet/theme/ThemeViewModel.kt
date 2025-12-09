package com.example.familywallet.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ViewModel que expone el estado del tema y delega la persistencia al repositorio
class ThemeViewModel(private val repo: ThemeRepository) : ViewModel() {

    // Convierte el Flow del repositorio en un StateFlow listo para usar en Compose
    val isDark = repo.isDarkFlow.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )

    // Alterna el valor actual del tema oscuro y lo guarda en DataStore
    fun toggle() = viewModelScope.launch { repo.setDark(!isDark.value) }
}





