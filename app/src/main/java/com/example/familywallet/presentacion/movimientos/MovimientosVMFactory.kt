package com.example.familywallet.presentacion.movimientos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.familywallet.datos.repositorios.MovimientoRepositorio

// Factory del MovimientosViewModel.
// Sirve para inyectar el repositorio sin usar Hilt/Dagger.
class MovimientosVMFactory(
    private val repo: MovimientoRepositorio
) : ViewModelProvider.Factory {

    // Crea una instancia del ViewModel solicitado.
    // Verifica que el tipo pedido sea MovimientosViewModel y le pasa el repositorio.
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(MovimientosViewModel::class.java))
        return MovimientosViewModel(repo) as T
    }
}




