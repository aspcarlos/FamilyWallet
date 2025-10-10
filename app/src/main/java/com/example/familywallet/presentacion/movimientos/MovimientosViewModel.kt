package com.example.familywallet.presentacion.movimientos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.util.Calendar
import java.util.Locale

enum class Tipo { INGRESO, GASTO }

data class Movimiento(
    val familiaId: String,
    val tipo: Tipo,
    val cantidad: Double,
    val categoria: String?,      // null para ingresos
    val timeMillis: Long         // fecha en millis
)

class MovimientosViewModel : ViewModel() {

    private val _lista = mutableStateListOf<Movimiento>()
    val lista: List<Movimiento> get() = _lista

    var totalIngresos by mutableStateOf(0.0)
        private set
    var totalGastos by mutableStateOf(0.0)
        private set

    /** Recalcula totales del **mes actual** del dispositivo para una familia */
    fun cargarMesActual(familiaId: String) {
        val cal = Calendar.getInstance()
        val y = cal.get(Calendar.YEAR)
        val m0 = cal.get(Calendar.MONTH) // 0..11

        val delMes = _lista.filter {
            it.familiaId == familiaId && esMismoMes(it.timeMillis, y, m0)
        }
        totalIngresos = delMes.filter { it.tipo == Tipo.INGRESO }.sumOf { it.cantidad }
        totalGastos   = delMes.filter { it.tipo == Tipo.GASTO   }.sumOf { it.cantidad }
    }

    fun agregarGasto(
        familiaId: String,
        cantidad: Double,
        categoria: String,
        fechaMillis: Long = System.currentTimeMillis()
    ) {
        _lista.add(
            Movimiento(
                familiaId = familiaId,
                tipo = Tipo.GASTO,
                cantidad = cantidad,
                categoria = categoria,
                timeMillis = fechaMillis
            )
        )
        cargarMesActual(familiaId)
    }

    fun agregarIngreso(
        familiaId: String,
        cantidad: Double,
        fechaMillis: Long = System.currentTimeMillis()
    ) {
        _lista.add(
            Movimiento(
                familiaId = familiaId,
                tipo = Tipo.INGRESO,
                cantidad = cantidad,
                categoria = null,
                timeMillis = fechaMillis
            )
        )
        cargarMesActual(familiaId)
    }

    /** Devuelve los movimientos de un mes concreto (month = 1..12) */
    fun movimientosDeMes(familiaId: String, year: Int, month: Int): List<Movimiento> {
        val m0 = month - 1 // Calendar.MONTH es 0..11
        val cal = Calendar.getInstance()
        return _lista.filter { mov ->
            mov.familiaId == familiaId && cal.run {
                timeInMillis = mov.timeMillis
                get(Calendar.YEAR) == year && get(Calendar.MONTH) == m0
            }
        }.sortedByDescending { it.timeMillis }
    }

    /** Comprueba si un instante cae en el año/mes dados (mes 0-based). */
    private fun esMismoMes(millis: Long, year: Int, month0: Int): Boolean {
        val c = Calendar.getInstance().apply { timeInMillis = millis }
        return c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) == month0
    }

    /** Nombre del mes actual, útil para cabeceras */
    fun nombreMesActual(): String {
        val c = Calendar.getInstance()
        val mes = c.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) ?: ""
        val year = c.get(Calendar.YEAR)
        return mes.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } +
                " $year"
    }

    fun fechaHoyDiaMes(): String {
        val c = Calendar.getInstance()
        val dia = c.get(Calendar.DAY_OF_MONTH)
        val mes = c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) ?: ""
        return "$dia ${mes.lowercase(Locale.getDefault())}"
    }
}








