package com.example.familywallet.datos.modelos

data class Solicitud(
    val id: String = "",
    val familiaId: String = "",
    val uid: String = "",
    val alias: String = "",
    val createdAt: Long = System.currentTimeMillis()
)



