package com.example.familywallet.presentacion.familia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.familywallet.datos.repositorios.AuthRepositorio
import com.example.familywallet.datos.repositorios.FamiliaRepositorio

class FamiliaVMFactory(
    private val familiaRepo: FamiliaRepositorio,
    private val authRepo: AuthRepositorio
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(FamiliaViewModel::class.java))
        return FamiliaViewModel(familiaRepo, authRepo) as T
    }
}


