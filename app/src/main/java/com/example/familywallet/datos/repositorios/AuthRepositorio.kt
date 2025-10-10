package com.example.familywallet.datos.repositorios

interface AuthRepositorio {
    interface AuthRepositorio {
        suspend fun usuarioActualId(): String?
        suspend fun loginDemo(): String   // de momento “falso” para pruebas
    }
}