package com.example.familywallet.datos.repositorios

import com.example.familywallet.datos.modelos.Movimiento
import java.util.Calendar

object FakeMovimientoRepositorio : MovimientoRepositorio {

    private val movimientos = mutableListOf<Movimiento>()

    override suspend fun movimientosDeMes(
        familiaId: String,
        year: Int,
        month: Int
    ): List<Movimiento> {
        val cal = Calendar.getInstance()
        return movimientos.filter { mov ->
            mov.familiaId == familiaId && run {
                cal.timeInMillis = mov.fechaMillis
                val y = cal.get(Calendar.YEAR)
                val m = cal.get(Calendar.MONTH) + 1  // Calendar es 0..11 → +1
                y == year && m == month
            }
        }
    }

    override suspend fun agregarMovimiento(m: Movimiento): Movimiento {
        movimientos.add(m)
        return m
    }

    override suspend fun eliminarMovimiento(familiaId: String, id: String) {
        movimientos.removeAll { it.familiaId == familiaId && it.id == id }
    }

}


