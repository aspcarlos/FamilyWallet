package com.example.familywallet.presentacion.movimientos

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familywallet.datos.modelos.Movimiento
import com.example.familywallet.datos.repositorios.MovimientoRepositorio
import com.example.familywallet.presentacion.ui.FiltroPeriodo
import com.example.familywallet.presentacion.ui.RangoFecha
import com.example.familywallet.presentacion.ui.rangoPorFiltro
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class MovimientosViewModel(
    private val repo: MovimientoRepositorio
) : ViewModel() {

    // Lista reactiva del mes
    private val _itemsDelMesState = mutableStateOf<List<Movimiento>>(emptyList())
    val itemsDelMesState: State<List<Movimiento>> get() = _itemsDelMesState

    // Totales y moneda actual
    var monedaActual by mutableStateOf("EUR")
        private set
    var totalIngresos by mutableStateOf(0.0)
        private set
    var totalGastos by mutableStateOf(0.0)
        private set

    // --- Filtro/etiqueta visibles en la UI ---
    var filtroPeriodo by mutableStateOf(FiltroPeriodo.MES)
        private set

    var etiquetaPeriodo by mutableStateOf("")   // se muestra en el título

    // Cargar por rango (si tu repo no tiene entre-fechas,
    // filtra localmente como fallback)
    fun cargarRango(familiaId: String, inicio: Long, fin: Long) {
        viewModelScope.launch {
            val lista = try {
                repo.movimientosEntre(familiaId, inicio, fin)
            } catch (_: Throwable) {
                // Fallback: carga "grande" y filtra por millis
                val cal = Calendar.getInstance()
                cal.timeInMillis = inicio
                val yIni = cal.get(Calendar.YEAR)
                val mIni = cal.get(Calendar.MONTH) + 1
                repo.movimientosDeMes(familiaId, yIni, mIni)
                    .filter { it.fechaMillis in inicio..fin }
            }

            _itemsDelMesState.value = lista
            recomputarTotales()
        }
    }

    fun aplicarRango(familiaId: String, rango: RangoFecha) {
        etiquetaPeriodo = rango.etiqueta
        cargarRango(familiaId, rango.inicio, rango.fin)
    }


    // Mes/año que está viendo el usuario (para recargar tras insertar)
    private var yearActual: Int = 0
    private var monthActual: Int = 0

    // Tasas simples (offline) para conversión
    private val conversionRates = mapOf(
        "EUR" to 1.0,
        "USD" to 1.08,
        "GBP" to 0.85,
        "JPY" to 161.3,
        "MXN" to 19.5
    )

    // ---------------------------------------
    // Helpers de estado
    // ---------------------------------------
    private fun setItems(nuevos: List<Movimiento>) {
        _itemsDelMesState.value = nuevos
        recomputarTotales()
    }

    private fun recomputarTotales() {
        val lista = _itemsDelMesState.value
        totalIngresos = lista.filter { it.tipo == Movimiento.Tipo.INGRESO }.sumOf { it.cantidad }
        totalGastos   = lista.filter { it.tipo == Movimiento.Tipo.GASTO   }.sumOf { it.cantidad }
    }

    // ---------------------------------------
    // Cargas
    // ---------------------------------------
    fun cargarMesActual(familiaId: String) {
        val cal = Calendar.getInstance()
        cargarMes(familiaId, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    fun cargarMes(familiaId: String, year: Int, month: Int) {
        yearActual = year
        monthActual = month
        viewModelScope.launch {
            val lista = repo.movimientosDeMes(familiaId, year, month)
            setItems(lista) // asignación única + recálculo de totales
        }
    }

    // ---------------------------------------
    // Inserciones
    // ---------------------------------------
    fun agregarGasto(
        familiaId: String,
        cantidad: Double,
        categoria: String?,
        fechaMillis: Long
    ) {
        viewModelScope.launch {
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
            cargarMes(familiaId, yearActualOrNow(), monthActualOrNow())
        }
    }

    // ---------------------------------------
    // Moneda
    // ---------------------------------------
    fun cambiarMoneda(nueva: String) {
        if (nueva == monedaActual) return
        val tasaNueva = conversionRates[nueva] ?: 1.0
        val tasaVieja = conversionRates[monedaActual] ?: 1.0
        val factor = tasaNueva / tasaVieja

        // Convertimos las cantidades en la lista actual
        setItems(_itemsDelMesState.value.map { it.copy(cantidad = it.cantidad * factor) })

        monedaActual = nueva
        // (recomputarTotales() ya se llama dentro de setItems)
    }

    // ---------------------------------------
    // Utilidades internas
    // ---------------------------------------
    private fun yearActualOrNow(): Int =
        if (yearActual == 0) Calendar.getInstance().get(Calendar.YEAR) else yearActual

    private fun monthActualOrNow(): Int =
        if (monthActual == 0) Calendar.getInstance().get(Calendar.MONTH) + 1 else monthActual

    fun nombreMesActual(): String {
        val y = yearActualOrNow()
        val m = monthActualOrNow() // 1..12
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, y)
            set(Calendar.MONTH, m - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val fmt = java.text.SimpleDateFormat("LLLL yyyy", java.util.Locale("es", "ES"))
        return fmt.format(cal.time)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale("es", "ES")) else it.toString() }
    }

}














