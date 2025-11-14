package com.example.familywallet.datos.modelos

enum class Rol { ADMIN, MIEMBRO }

data class Miembro(
    val id: String = "",
    val uid: String = "",
    val alias: String = "",
    val rol: Rol = Rol.MIEMBRO
)

// Extensiones para mapear con Firestore
fun String?.toRol(): Rol =
    if (this.equals("admin", ignoreCase = true)) Rol.ADMIN else Rol.MIEMBRO

fun Rol.asFs(): String =
    if (this == Rol.ADMIN) "admin" else "miembro"

