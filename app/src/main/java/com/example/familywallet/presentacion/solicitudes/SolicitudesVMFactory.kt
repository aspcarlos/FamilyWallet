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
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SolicitudesViewModel(
            solicitudesRepo = solicitudesRepo,
            familiaRepo = familiaRepo,
            authRepo = authRepo
        ) as T
    }
}




