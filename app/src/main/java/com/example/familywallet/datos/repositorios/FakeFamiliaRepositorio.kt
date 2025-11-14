package com.example.familywallet.datos.repositorios

import com.example.familywallet.datos.modelos.Familia
import java.util.UUID

object FakeFamiliaRepositorio {
    private val familias = mutableListOf<Familia>()

    fun crearFamilia(adminUid: String, nombre: String): Familia {
        val f = Familia(
            id = UUID.randomUUID().toString(),
            nombre = nombre,
            adminUid = adminUid
        )
        familias.add(f)
        return f
    }

    private val nombres = mutableMapOf<String, String>()

    suspend fun nombreDe(familiaId: String): String? = nombres[familiaId]
}


