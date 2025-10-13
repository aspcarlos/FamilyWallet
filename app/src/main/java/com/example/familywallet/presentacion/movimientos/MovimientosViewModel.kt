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

    // ------------------------------
    // Estado
    // ------------------------------
    private val _itemsDelMesState = mutableStateOf<List<Movimiento>>(emptyList())
    val itemsDelMesState: State<List<Movimiento>> get() = _itemsDelMesState

    var monedaActual by mutableStateOf("EUR")
        private set

    var totalIngresos by mutableStateOf(0.0)
        private set
    var totalGastos by mutableStateOf(0.0)
        private set

    // Mes/año cargados (para recargar tras insertar)
    private var yearActual: Int = 0
    private var monthActual: Int = 0

    // ------------------------------
    // Moneda (tasas de ejemplo)
    // ------------------------------
    private val conversionRates = mapOf(
        "EUR" to 1.0,
        "USD" to 1.08,
        "GBP" to 0.85,
        "JPY" to 161.3,
        "MXN" to 19.5
    )

    fun cambiarMoneda(nueva: String) {
        val tasaNueva = conversionRates[nueva] ?: 1.0
        val tasaVieja = conversionRates[monedaActual] ?: 1.0
        val factor = tasaNueva / tasaVieja

        // Convertir todas las cantidades del mes actual
        val convertida = _itemsDelMesState.value.map { mov ->
            mov.copy(cantidad = mov.cantidad * factor)
        }
        _itemsDelMesState.value = convertida

        monedaActual = nueva
        recomputarTotales()
    }

    // ------------------------------
    // Totales
    // ------------------------------
    private fun recomputarTotales() {
        val lista = _itemsDelMesState.value

        totalIngresos = lista
            .filter { it.tipo == Movimiento.Tipo.INGRESO }
            .sumOf { it.cantidad }

        totalGastos = lista
            .filter { it.tipo == Movimiento.Tipo.GASTO }
            .sumOf { it.cantidad }
    }

    // ------------------------------
    // Cargas
    // ------------------------------
    fun cargarMesActual(familiaId: String) {
        val cal = Calendar.getInstance()
        cargarMes(
            familiaId = familiaId,
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH) + 1
        )
    }

    fun cargarMes(familiaId: String, year: Int, month: Int) {
        yearActual = year
        monthActual = month

        viewModelScope.launch {
            val lista = repo.movimientosDeMes(familiaId, year, month)
            _itemsDelMesState.value = lista
            recomputarTotales()
        }
    }

    // ------------------------------
    // Inserciones
    // ------------------------------
    fun agregarGasto(
        familiaId: String,
        cantidad: Double,
        categoria: String?,
        fechaMillis: Long
    ) {
        viewModelScope.launch {
            val mov = Movimiento(
                id = "", // lo rellenará el repo si aplica
                familiaId = familiaId,
                cantidad = cantidad,
                categoria = categoria,
                fechaMillis = fechaMillis,
                tipo = Movimiento.Tipo.GASTO
            )
            repo.agregarMovimiento(mov)
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

    // ------------------------------
    // Utilidades UI
    // ------------------------------
    fun nombreMesActual(): String {
        val y = yearActualOrNow()
        val m = monthActualOrNow() // 1..12
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, y)
            set(Calendar.MONTH, m - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val fmt = SimpleDateFormat("LLLL yyyy", Locale("es", "ES"))
        return fmt.format(cal.time)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }
    }

    private fun yearActualOrNow(): Int =
        if (yearActual == 0) Calendar.getInstance().get(Calendar.YEAR) else yearActual

    private fun monthActualOrNow(): Int =
        if (monthActual == 0) Calendar.getInstance().get(Calendar.MONTH) + 1 else monthActual
}













