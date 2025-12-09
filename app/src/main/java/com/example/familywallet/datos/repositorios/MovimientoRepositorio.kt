package com.example.familywallet.datos.repositorios

import com.example.familywallet.datos.modelos.Movimiento
import kotlinx.coroutines.flow.Flow

// Contrato de acceso a datos de ingresos y gastos.
// Permite cambiar la fuente de datos (Firebase o Fake) sin tocar ViewModels ni UI.
interface MovimientoRepositorio {

    // Devuelve los movimientos de una familia filtrados por mes y año.
    suspend fun movimientosDeMes(
        familiaId: String,
        year: Int,
        month: Int
    ): List<Movimiento>

    // Inserta un nuevo movimiento (gasto o ingreso) y devuelve el movimiento con id asignado.
    suspend fun agregarMovimiento(
        m: Movimiento
    ): Movimiento

    // Elimina un movimiento concreto por id dentro de una familia.
    suspend fun eliminarMovimiento(
        familiaId: String,
        id: String
    )

    // Devuelve movimientos de una familia dentro de un rango de fechas.
    suspend fun movimientosEntre(
        familiaId: String,
        inicioMillis: Long,
        finMillis: Long
    ): List<Movimiento>

    // Emite en tiempo real la lista de movimientos de una familia cuando hay cambios.
    fun observarMovimientosFamilia(
        familiaId: String
    ): Flow<List<Movimiento>>
}




