package com.example.familywallet.datos.modelos

import java.util.UUID

enum class TipoMov { INGRESO, GASTO }

data class Movimiento(
    val id: String = UUID.randomUUID().toString(),
    val familiaId: String,
    val tipo: TipoMov,
    val cantidad: Double,
    val categoria: String? = null,
    val timeMillis: Long = System.currentTimeMillis()
)






