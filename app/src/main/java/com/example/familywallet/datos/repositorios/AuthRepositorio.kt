package com.example.familywallet.datos.repositorios

interface AuthRepositorio {

    val usuarioActualUid: String?

    // login con deviceId y control de sesión
    suspend fun login(
        email: String,
        pass: String,
        deviceId: String
    )

    // logout que marca la sesión como inactiva
    suspend fun logout()
}

