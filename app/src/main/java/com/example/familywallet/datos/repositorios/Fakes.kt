package com.example.familywallet.datos.repositorios

import com.example.familywallet.datos.modelos.Movimiento
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.Calendar

// Repositorio fake en memoria para pruebas/demos sin Firebase.
// Implementa el mismo contrato que el repositorio real.
object FakeMovimientoRepositorio : MovimientoRepositorio {

    // Lista local donde se guardan los movimientos simulados.
    private val movimientos = mutableListOf<Movimiento>()

    // Flujo que emite la lista completa cuando hay cambios.
    private val movimientosFlow = MutableStateFlow<List<Movimiento>>(emptyList())

    // Actualiza el flow con una copia de la lista actual.
    private fun notificarCambio() {
        movimientosFlow.value = movimientos.toList()
    }

    // Devuelve un flujo "en tiempo real" filtrado por familia.
    override fun observarMovimientosFamilia(familiaId: String): Flow<List<Movimiento>> =
        movimientosFlow.map { lista ->
            lista.filter { it.familiaId == familiaId }
        }

    // Obtiene movimientos de un mes concreto calculando año/mes desde fechaMillis.
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

    // Obtiene movimientos dentro de un rango de fechas.
    override suspend fun movimientosEntre(
        familiaId: String,
        inicioMillis: Long,
        finMillis: Long
    ): List<Movimiento> =
        movimientos.filter { mov ->
            mov.familiaId == familiaId &&
                    mov.fechaMillis in inicioMillis..finMillis
        }

    // Añade un movimiento y genera un id simple si viene vacío.
    override suspend fun agregarMovimiento(m: Movimiento): Movimiento {
        val conId = if (m.id.isBlank())
            m.copy(id = (movimientos.size + 1).toString())
        else
            m

        movimientos.add(conId)
        notificarCambio()
        return conId
    }

    // Elimina un movimiento por id dentro de la familia y notifica el cambio.
    override suspend fun eliminarMovimiento(familiaId: String, id: String) {
        movimientos.removeAll { it.familiaId == familiaId && it.id == id }
        notificarCambio()
    }
}




