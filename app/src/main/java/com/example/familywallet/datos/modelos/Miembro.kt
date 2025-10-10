package com.example.familywallet.datos.modelos

enum class Rol { ADMIN, USUARIO }

data class Miembro(
    val uid: String,
    val nombre: String,
    val rol: Rol
)
