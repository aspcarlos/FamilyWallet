package com.example.familywallet.datos.modelos

data class Movimiento(
    val id: String = "",
    val familiaId: String = "",
    val cantidad: Double = 0.0,
    val categoria: String? = null,
    val nota: String? = null,
    val fechaMillis: Long = 0L,
    val tipo: Tipo = Tipo.GASTO
) {
    enum class Tipo { GASTO, INGRESO }
}











