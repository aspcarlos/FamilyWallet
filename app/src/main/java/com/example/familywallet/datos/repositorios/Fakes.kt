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
                cal.timeInMillis = mov.timeMillis
                cal.get(Calendar.YEAR)  == year &&
                        cal.get(Calendar.MONTH) == month
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


