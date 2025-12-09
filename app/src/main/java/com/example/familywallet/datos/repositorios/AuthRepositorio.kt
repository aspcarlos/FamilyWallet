package com.example.familywallet.datos.repositorios

// Contrato de autenticación para desacoplar la app del proveedor (Firebase u otro).
interface AuthRepositorio {

    // Devuelve el UID del usuario autenticado actualmente, o null si no hay sesión.
    val usuarioActualUid: String?

    // Inicia sesión y registra/valida el deviceId para evitar sesiones simultáneas en otro móvil.
    suspend fun login(
        email: String,
        pass: String,
        deviceId: String
    )

    // Cierra sesión y marca la sesión como inactiva en la capa de datos.
    suspend fun logout()
}


