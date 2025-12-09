package com.example.familywallet.datos.repositorios

import com.example.familywallet.datos.modelos.Familia
import java.util.UUID

// Repositorio fake para pruebas/desarrollo sin Firebase.
// Simula creación y consulta básica de familias en memoria.
object FakeFamiliaRepositorio {
    private val familias = mutableListOf<Familia>()

    // Crea una familia de forma local con un id aleatorio y la añade a la lista en memoria.
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

    // Devuelve el nombre asociado a un id de familia en la simulación (si existe).
    suspend fun nombreDe(familiaId: String): String? = nombres[familiaId]
}



