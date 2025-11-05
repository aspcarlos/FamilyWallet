package com.example.familywallet.datos.repositorios

import com.example.familywallet.datos.modelos.Movimiento
import kotlinx.coroutines.flow.Flow

interface MovimientoRepositorio {
    suspend fun movimientosDeMes(familiaId: String, year: Int, month: Int): List<Movimiento>
    suspend fun agregarMovimiento(m: Movimiento): Movimiento
    suspend fun eliminarMovimiento(familiaId: String, id: String)
    suspend fun movimientosEntre(
        familiaId: String,
        inicioMillis: Long,
        finMillis: Long
    ): List<Movimiento>

    // flujo en tiempo real de todos los movimientos de la familia
    fun observarMovimientosFamilia(familiaId: String): Flow<List<Movimiento>>
}



