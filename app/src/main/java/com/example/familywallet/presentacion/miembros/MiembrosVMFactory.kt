package com.example.familywallet.presentacion.miembros

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.familywallet.datos.repositorios.FamiliaRepositorio

class MiembrosVMFactory(
    private val familiaRepo: FamiliaRepositorio
) : ViewModelProvider.Factory {

    // Factory para crear el MiembrosViewModel inyectando el repositorio.
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MiembrosViewModel(familiaRepo) as T
    }
}



