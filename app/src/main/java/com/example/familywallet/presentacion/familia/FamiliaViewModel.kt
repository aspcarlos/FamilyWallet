package com.example.familywallet.presentacion.familia

import androidx.lifecycle.ViewModel
import com.example.familywallet.datos.repositorios.AuthRepositorio
import com.example.familywallet.datos.repositorios.FamiliaRepositorio


class FamiliaViewModel(
    private val familiaRepo: FamiliaRepositorio,
    private val authRepo: AuthRepositorio
) : ViewModel() {

    suspend fun crearFamilia(nombre: String, aliasOwner: String): String {
        val ownerUid = authRepo.usuarioActualUid
            ?: throw IllegalStateException("No hay usuario autenticado")

        return familiaRepo.crearFamilia(
            nombre = nombre,
            ownerUid = ownerUid,
            aliasOwner = aliasOwner
        )
    }
}










