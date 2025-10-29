package com.example.familywallet.datos.modelos

enum class Rol { ADMIN, USUARIO }

data class Miembro(
    val id: String = "",
    val uid: String = "",
    val alias: String = "",
    val rol: String = "miembro" // "admin" o "miembro"
)
