package com.example.familywallet.datos.repositorios

import com.example.familywallet.datos.modelos.Familia

interface FamiliaRepositorio {
    suspend fun familiasDeUsuario(uid: String): List<Familia>
    suspend fun crearFamilia(uid: String, nombre: String): Familia
    suspend fun unirseAFamilia(uid: String, codigo: String): Familia
}