package com.example.familywallet.presentacion.movimientos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.familywallet.datos.repositorios.MovimientoRepositorio

class MovimientosVMFactory(
    private val repo: MovimientoRepositorio
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(MovimientosViewModel::class.java))
        return MovimientosViewModel(repo) as T
    }
}



