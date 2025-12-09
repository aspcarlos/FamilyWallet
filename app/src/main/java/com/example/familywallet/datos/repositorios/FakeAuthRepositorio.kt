package com.example.familywallet.datos.repositorios

// Repositorio fake para pruebas/demos sin Firebase.
// Simula un usuario autenticado guardando un UID en memoria.
object FakeAuthRepositorio {
    private var uid: String? = null

    // Asigna manualmente el usuario "logueado" en esta simulación.
    fun setUsuario(id: String) { uid = id }

    // Devuelve el UID simulado actual, o null si no hay usuario asignado.
    fun usuarioActualId(): String? = uid
}




