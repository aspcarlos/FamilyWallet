package com.example.familywallet.presentacion.familia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.familywallet.datos.repositorios.AuthRepositorio
import com.example.familywallet.datos.repositorios.FamiliaRepositorio

// Factory para crear FamiliaViewModel con sus dependencias (repos).
class FamiliaVMFactory(
    private val familiaRepo: FamiliaRepositorio,
    private val authRepo: AuthRepositorio
) : ViewModelProvider.Factory {

    // Verifica el tipo de ViewModel solicitado y devuelve una instancia de FamiliaViewModel.
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(FamiliaViewModel::class.java))
        return FamiliaViewModel(familiaRepo, authRepo) as T
    }
}



