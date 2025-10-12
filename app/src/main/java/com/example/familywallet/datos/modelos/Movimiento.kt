package com.example.familywallet.datos.modelos

data class Movimiento(
    val id: String = "",
    val familiaId: String,
    val cantidad: Double,
    val categoria: String?,      // puede ser null en ingresos
    val fechaMillis: Long,       // <-- clave consistente en todo el código
    val tipo: Tipo
) {
    enum class Tipo { GASTO, INGRESO }
}










