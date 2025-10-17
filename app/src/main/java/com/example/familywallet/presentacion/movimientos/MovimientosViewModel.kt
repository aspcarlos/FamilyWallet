package com.example.familywallet.presentacion.movimientos

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familywallet.datos.modelos.Movimiento
import com.example.familywallet.datos.repositorios.MovimientoRepositorio
import com.example.familywallet.presentacion.ui.FiltroPeriodo
import com.example.familywallet.presentacion.ui.RangoFecha
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class MovimientosViewModel(
    private val repo: MovimientoRepositorio
) : ViewModel() {

    // Lista observable
    private val _itemsDelMesState = mutableStateOf<List<Movimiento>>(emptyList())
    val itemsDelMesState: State<List<Movimiento>> get() = _itemsDelMesState

    // Error observable
    private val _errorMsg = mutableStateOf<String?>(null)
    val errorMsg: State<String?> get() = _errorMsg

    // 🔥 Estas deben ser State para que Compose se recom­ponga
    var monedaActual by mutableStateOf("EUR")
        private set
    var totalIngresos by mutableStateOf(0.0)
        private set
    var totalGastos by mutableStateOf(0.0)
        private set
    var etiquetaPeriodo by mutableStateOf("")
        private set
    var filtroPeriodo by mutableStateOf(FiltroPeriodo.MES)
        private set

    // Para recargas
    private var ultimoRango: RangoFecha? = null
    private var yearActual: Int = 0
    private var monthActual: Int = 0

    // Conversión simple offline
    private val conversionRates = mapOf(
        "EUR" to 1.0, "USD" to 1.08, "GBP" to 0.85, "JPY" to 161.3, "MXN" to 19.5
    )

    // ---- helpers ----
    private fun setItems(nuevos: List<Movimiento>) {
        _itemsDelMesState.value = nuevos
        recomputarTotales()
    }

    private fun recomputarTotales() {
        val lista = _itemsDelMesState.value
        totalIngresos = lista.filter { it.tipo == Movimiento.Tipo.INGRESO }.sumOf { it.cantidad }
        totalGastos   = lista.filter { it.tipo == Movimiento.Tipo.GASTO   }.sumOf { it.cantidad }
    }

    // ---- cargas ----
    fun cargarMesActual(familiaId: String) {
        val cal = Calendar.getInstance()
        cargarMes(familiaId, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    fun cargarMes(familiaId: String, year: Int, month: Int) {
        yearActual = year
        monthActual = month
        viewModelScope.launch {
            try {
                val lista = repo.movimientosDeMes(familiaId, year, month)
                setItems(lista)
                _errorMsg.value = null
            } catch (e: Exception) {
                _itemsDelMesState.value = emptyList()
                _errorMsg.value = e.localizedMessage ?: "No se pudo cargar el mes."
            }
        }
    }

    fun cargarRango(familiaId: String, inicio: Long, fin: Long) {
        viewModelScope.launch {
            try {
                val lista = repo.movimientosEntre(familiaId, inicio, fin)
                setItems(lista)
                _errorMsg.value = null
            } catch (e: Exception) {
                _itemsDelMesState.value = emptyList()
                _errorMsg.value = e.localizedMessage ?: "No se pudo cargar el rango."
            }
        }
    }

    fun aplicarRango(familiaId: String, rango: RangoFecha) {
        etiquetaPeriodo = rango.etiqueta
        ultimoRango = rango
        cargarRango(familiaId, rango.inicio, rango.fin)
    }

    // ---- inserciones (suspend para poder esperar desde la UI) ----
    suspend fun agregarGasto(
        familiaId: String,
        cantidad: Double,
        categoria: String?,
        fechaMillis: Long
    ) {
        repo.agregarMovimiento(
            Movimiento(
                id = "",
                familiaId = familiaId,
                cantidad = cantidad,
                categoria = categoria,
                fechaMillis = fechaMillis,
                tipo = Movimiento.Tipo.GASTO
            )
        )
        recargarDespuesDeInsert(familiaId)
    }

    suspend fun agregarIngreso(
        familiaId: String,
        cantidad: Double,
        categoria: String?,
        fechaMillis: Long
    ) {
        repo.agregarMovimiento(
            Movimiento(
                id = "",
                familiaId = familiaId,
                cantidad = cantidad,
                categoria = categoria,
                fechaMillis = fechaMillis,
                tipo = Movimiento.Tipo.INGRESO
            )
        )
        recargarDespuesDeInsert(familiaId)
    }

    private fun recargarDespuesDeInsert(familiaId: String) {
        val r = ultimoRango
        if (r != null) {
            cargarRango(familiaId, r.inicio, r.fin)
        } else {
            cargarMes(familiaId, yearActualOrNow(), monthActualOrNow())
        }
    }

    // ---- moneda ----
    fun cambiarMoneda(nueva: String) {
        if (nueva == monedaActual) return
        val factor = (conversionRates[nueva] ?: 1.0) / (conversionRates[monedaActual] ?: 1.0)
        setItems(_itemsDelMesState.value.map { it.copy(cantidad = it.cantidad * factor) })
        monedaActual = nueva
    }

    // ---- soporte a cambio de familia ----
    fun onFamiliaCambiada(familiaId: String) {
        resetEstado()
        cargarMesActual(familiaId)
    }

    private fun resetEstado() {
        _itemsDelMesState.value = emptyList()
        totalIngresos = 0.0
        totalGastos = 0.0
        filtroPeriodo = FiltroPeriodo.MES
        etiquetaPeriodo = ""
        ultimoRango = null
        yearActual = 0
        monthActual = 0
        _errorMsg.value = null
    }

    // ---- util ----
    private fun yearActualOrNow(): Int =
        if (yearActual == 0) Calendar.getInstance().get(Calendar.YEAR) else yearActual
    private fun monthActualOrNow(): Int =
        if (monthActual == 0) Calendar.getInstance().get(Calendar.MONTH) + 1 else monthActual

    fun nombreMesActual(): String {
        val y = yearActualOrNow()
        val m = monthActualOrNow()
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, y); set(Calendar.MONTH, m - 1); set(Calendar.DAY_OF_MONTH, 1)
        }
        val fmt = java.text.SimpleDateFormat("LLLL yyyy", Locale("es", "ES"))
        return fmt.format(cal.time)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es","ES")) else it.toString() }
    }
}


















