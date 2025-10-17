package com.example.familywallet.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(private val repo: ThemeRepository) : ViewModel() {
    val isDark = repo.isDarkFlow.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )
    fun toggle() = viewModelScope.launch { repo.setDark(!isDark.value) }
}




