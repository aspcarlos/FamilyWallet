package com.example.familywallet.datos.repositorios

import com.example.familywallet.datos.modelos.Movimiento

interface MovimientoRepositorio {
    suspend fun movimientosDeMes(
        familiaId: String,
        year: Int,
        month: Int
    ): List<Movimiento>

    suspend fun agregarMovimiento(m: Movimiento): Movimiento

    suspend fun eliminarMovimiento(familiaId: String, id: String)
}
