package com.example.familywallet.datos.repositorios

import com.example.familywallet.datos.modelos.Miembro
import kotlinx.coroutines.flow.Flow

interface FamiliaRepositorio {

    suspend fun ownerUidDe(familiaId: String): String?

    suspend fun miembrosDe(familiaId: String): List<Miembro>

    // ID de mi familia en tiempo real (owner o miembro)
    fun observarMiFamiliaId(uid: String): Flow<String?>

    suspend fun expulsarMiembro(familiaId: String, uidMiembro: String)

    suspend fun salirDeFamilia(familiaId: String, uid: String)

    suspend fun nombreDe(familiaId: String): String?

    // lookup rápido (lectura puntual)
    suspend fun miFamiliaId(uid: String): String?

    suspend fun crearFamilia(
        nombre: String,
        ownerUid: String,
        aliasOwner: String
    ): String

    suspend fun eliminarFamilia(familiaId: String)

    suspend fun buscarFamiliaPorNombre(nombre: String): String?

    suspend fun esAdmin(familiaId: String, uid: String): Boolean
}






