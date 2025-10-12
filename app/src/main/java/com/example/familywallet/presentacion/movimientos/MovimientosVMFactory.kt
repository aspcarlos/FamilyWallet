package com.example.familywallet.presentacion.movimientos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.familywallet.datos.repositorios.MovimientoRepositorio

class MovimientosVMFactory(
    private val repo: MovimientoRepositorio
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovimientosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MovimientosViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}


