package com.example.familywallet.datos.repositorios

import com.example.familywallet.datos.modelos.Miembro
import kotlinx.coroutines.flow.Flow

interface FamiliaRepositorio {
    suspend fun crearFamilia(nombre: String, ownerUid: String, aliasOwner: String): String
    suspend fun miFamiliaId(uid: String): String?
    suspend fun eliminarFamilia(familiaId: String)
    suspend fun buscarFamiliaPorNombre(nombre: String): String?
    suspend fun esAdmin(familiaId: String, uid: String): Boolean
    suspend fun nombreDe(familiaId: String): String?
    // observar en tiempo real si el usuario es owner o miembro
    fun observarMiFamiliaId(uid: String): Flow<String?>
    suspend fun salirDeFamilia(uid: String, familiaId: String)
    suspend fun miembrosDe(familiaId: String): List<Miembro>
    suspend fun expulsarMiembro(familiaId: String, miembroUid: String)
    suspend fun ownerUidDe(familiaId: String): String?
}





