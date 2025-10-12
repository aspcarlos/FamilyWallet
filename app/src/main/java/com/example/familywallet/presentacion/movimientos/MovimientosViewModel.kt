package com.example.familywallet.presentacion.movimientos

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familywallet.datos.modelos.Movimiento
import com.example.familywallet.datos.repositorios.MovimientoRepositorio
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MovimientosViewModel(
    private val repo: MovimientoRepositorio
) : ViewModel() {

    // ---- Estado que observa la UI ----
    private val _itemsDelMesState = mutableStateOf<List<Movimiento>>(emptyList())
    val itemsDelMesState: State<List<Movimiento>> = _itemsDelMesState

    var totalIngresos by mutableDoubleStateOf(0.0)
        private set
    var totalGastos by mutableDoubleStateOf(0.0)
        private set

    // Mes/año actuales cargados (para recargar después de insertar)
    private var yearActual: Int = 0
    private var monthActual: Int = 0  // 1..12

    // ---- Cargas ----
    fun cargarMesActual(familiaId: String) {
        val cal = Calendar.getInstance()
        cargarMes(familiaId, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    fun cargarMes(familiaId: String, year: Int, month: Int) {
        yearActual = year
        monthActual = month
        viewModelScope.launch {
            val lista = repo.movimientosDeMes(familiaId, year, month)
            _itemsDelMesState.value = lista
            totalIngresos = lista.filter { it.tipo == Movimiento.Tipo.INGRESO }
                .sumOf { it.cantidad }
            totalGastos = lista.filter { it.tipo == Movimiento.Tipo.GASTO }
                .sumOf { it.cantidad }
        }
    }

    // ---- Inserciones ----
    fun agregarGasto(
        familiaId: String,
        cantidad: Double,
        categoria: String?,
        fechaMillis: Long
    ) {
        viewModelScope.launch {
            val mov = Movimiento(
                id = "",                // el repo lo rellena si usa Firestore
                familiaId = familiaId,
                cantidad = cantidad,
                categoria = categoria,
                fechaMillis = fechaMillis,
                tipo = Movimiento.Tipo.GASTO
            )
            repo.agregarMovimiento(mov)
            // Recarga el mismo mes que está viendo el usuario
            cargarMes(familiaId, yearActualOrNow(), monthActualOrNow())
        }
    }

    fun agregarIngreso(
        familiaId: String,
        cantidad: Double,
        categoria: String?,
        fechaMillis: Long
    ) {
        viewModelScope.launch {
            val mov = Movimiento(
                id = "",
                familiaId = familiaId,
                cantidad = cantidad,
                categoria = categoria,
                fechaMillis = fechaMillis,
                tipo = Movimiento.Tipo.INGRESO
            )
            repo.agregarMovimiento(mov)
            cargarMes(familiaId, yearActualOrNow(), monthActualOrNow())
        }
    }

    // ---- Utilidades para la UI ----
    fun nombreMesActual(): String {
        val y = yearActualOrNow()
        val m = monthActualOrNow() // 1..12
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, y)
            set(Calendar.MONTH, m - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val fmt = SimpleDateFormat("LLLL yyyy", Locale("es", "ES"))
        // Capitaliza la primera letra (opcional)
        return fmt.format(cal.time).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }
    }

    private fun yearActualOrNow(): Int =
        if (yearActual == 0) Calendar.getInstance().get(Calendar.YEAR) else yearActual

    private fun monthActualOrNow(): Int =
        if (monthActual == 0) Calendar.getInstance().get(Calendar.MONTH) + 1 else monthActual
}












