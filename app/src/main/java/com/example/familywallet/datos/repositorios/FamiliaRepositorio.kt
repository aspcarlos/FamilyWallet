package com.example.familywallet.datos.repositorios

import com.example.familywallet.datos.modelos.Familia

interface FamiliaRepositorio {
    suspend fun crearFamilia(
        nombre: String,
        ownerUid: String,
        aliasOwner: String
    ): String // devuelve familiaId
}