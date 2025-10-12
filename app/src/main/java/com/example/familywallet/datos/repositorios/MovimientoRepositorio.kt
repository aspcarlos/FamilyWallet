package com.example.familywallet.datos.repositorios

import com.example.familywallet.datos.modelos.Movimiento

interface MovimientoRepositorio {
    suspend fun movimientosDeMes(familiaId: String, year: Int, month: Int): List<com.example.familywallet.datos.modelos.Movimiento>
    suspend fun agregarMovimiento(m: com.example.familywallet.datos.modelos.Movimiento): com.example.familywallet.datos.modelos.Movimiento
    suspend fun eliminarMovimiento(familiaId: String, id: String)
}

