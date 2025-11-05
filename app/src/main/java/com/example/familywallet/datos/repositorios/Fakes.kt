package com.example.familywallet.datos.repositorios

import com.example.familywallet.datos.modelos.Movimiento
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.Calendar

object FakeMovimientoRepositorio : MovimientoRepositorio {

    private val movimientos = mutableListOf<Movimiento>()
    private val movimientosFlow = MutableStateFlow<List<Movimiento>>(emptyList())

    private fun notificarCambio() {
        movimientosFlow.value = movimientos.toList()
    }

    // --- NUEVO: flujo en “tiempo real” para la familia ---
    override fun observarMovimientosFamilia(familiaId: String): Flow<List<Movimiento>> =
        movimientosFlow.map { lista ->
            lista.filter { it.familiaId == familiaId }
        }

    override suspend fun movimientosDeMes(
        familiaId: String,
        year: Int,
        month: Int
    ): List<Movimiento> {
        val cal = Calendar.getInstance()
        return movimientos.filter { mov ->
            mov.familiaId == familiaId && cal.apply {
                timeInMillis = mov.fechaMillis
            }.let {
                it.get(Calendar.YEAR) == year &&
                        it.get(Calendar.MONTH) + 1 == month
            }
        }
    }

    override suspend fun movimientosEntre(
        familiaId: String,
        inicioMillis: Long,
        finMillis: Long
    ): List<Movimiento> =
        movimientos.filter { mov ->
            mov.familiaId == familiaId &&
                    mov.fechaMillis in inicioMillis..finMillis
        }

    override suspend fun agregarMovimiento(m: Movimiento): Movimiento {
        val conId = if (m.id.isBlank())
            m.copy(id = (movimientos.size + 1).toString())
        else
            m

        movimientos.add(conId)
        notificarCambio()
        return conId
    }

    override suspend fun eliminarMovimiento(familiaId: String, id: String) {
        movimientos.removeAll { it.familiaId == familiaId && it.id == id }
        notificarCambio()
    }
}



