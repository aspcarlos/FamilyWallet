package com.example.familywallet.presentacion.solicitudes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.familywallet.datos.repositorios.AuthRepositorio
import com.example.familywallet.datos.repositorios.FamiliaRepositorio
import com.example.familywallet.datos.repositorios.SolicitudesRepositorio

class SolicitudesVMFactory(
    private val solicitudesRepo: SolicitudesRepositorio,
    private val familiaRepo: FamiliaRepositorio,
    private val authRepo: AuthRepositorio
) : ViewModelProvider.Factory {

    // Evita warning por el cast genérico del ViewModel
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Crea el SolicitudesViewModel inyectando sus repositorios
        return SolicitudesViewModel(
            solicitudesRepo = solicitudesRepo,
            familiaRepo = familiaRepo,
            authRepo = authRepo
        ) as T
    }
}





