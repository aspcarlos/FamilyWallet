package com.example.familywallet.datos.repositorios

interface FamiliaRepositorio {
    suspend fun crearFamilia(nombre: String, ownerUid: String, aliasOwner: String): String
    suspend fun miFamiliaId(ownerUid: String): String?
    suspend fun eliminarFamilia(familiaId: String)
}




