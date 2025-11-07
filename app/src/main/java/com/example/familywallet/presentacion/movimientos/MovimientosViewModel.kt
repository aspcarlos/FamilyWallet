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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class MovimientosViewModel(
    private val repo: MovimientoRepositorio
) : ViewModel() {

    // Lista usada en pantallas de historial
    private val _itemsDelMesState = mutableStateOf<List<Movimiento>>(emptyList())
    val itemsDelMesState: State<List<Movimiento>> get() = _itemsDelMesState

    // Error observable
    private val _errorMsg = mutableStateOf<String?>(null)
    val errorMsg: State<String?> get() = _errorMsg

    // Estado que lee Compose
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

    // Rango seleccionado (día, semana, mes, año)
    private var ultimoRango: RangoFecha? = null
    private var yearActual: Int = 0
    private var monthActual: Int = 0

    // Conversión simple offline
    private val conversionRates = mapOf(
        "EUR" to 1.0,
        "USD" to 1.08,
        "GBP" to 0.85,
        "JPY" to 161.3,
        "MXN" to 19.5
    )

    // --- TIEMPO REAL ---

    // Última lista recibida por el listener
    private var movimientosTiempoReal: List<Movimiento> = emptyList()

    // Job para poder cancelar la escucha al cambiar de familia / destruir VM
    private var escuchaJob: Job? = null

    /**
     * Empieza a escuchar en tiempo real TODOS los movimientos de la familia.
     * PantallaInicio usará esto para que móvil y emulador se sincronicen.
     */
    fun iniciarEscuchaTiempoReal(familiaId: String) {
        escuchaJob?.cancel()

        escuchaJob = viewModelScope.launch {
            repo.observarMovimientosFamilia(familiaId).collect { lista ->
                movimientosTiempoReal = lista
                recomputarTotalesTiempoReal()
            }
        }
    }

    /**
     * Aplica un rango (día, semana, mes, año) desde PantallaInicio.
     * Si ya tenemos escucha en tiempo real, sólo re-filtra la lista.
     * Si NO la tenemos (por ejemplo en historial), hace llamada normal a Firestore.
     */
    fun aplicarRango(familiaId: String, rango: RangoFecha) {
        etiquetaPeriodo = rango.etiqueta
        ultimoRango = rango

        if (movimientosTiempoReal.isNotEmpty()) {
            // Inicio con tiempo real
            recomputarTotalesTiempoReal()
        } else {
            // Historial sin escucha
            cargarRango(familiaId, rango.inicio, rango.fin)
        }
    }

    /**
     * Recalcula ingresos/gastos usando la lista en tiempo real y el rango seleccionado.
     */
    private fun recomputarTotalesTiempoReal() {
        val lista = movimientosTiempoReal
        val rango = ultimoRango

        val filtrados = if (rango != null) {
            lista.filter { it.fechaMillis in rango.inicio..rango.fin }
        } else {
            // Si no hay rango seleccionado, usamos el mes actual
            val inicioFin = rangoMesActualMillis()
            lista.filter { it.fechaMillis in inicioFin.first..inicioFin.second }
        }

        totalIngresos = filtrados
            .filter { it.tipo == Movimiento.Tipo.INGRESO }
            .sumOf { it.cantidad }

        totalGastos = filtrados
            .filter { it.tipo == Movimiento.Tipo.GASTO }
            .sumOf { it.cantidad }
    }

    // ---------- helpers de lista "tradicional" (historial) ----------

    private fun setItems(nuevos: List<Movimiento>) {
        _itemsDelMesState.value = nuevos
        recomputarTotalesLista()
    }

    private fun recomputarTotalesLista() {
        val lista = _itemsDelMesState.value
        totalIngresos =
            lista.filter { it.tipo == Movimiento.Tipo.INGRESO }.sumOf { it.cantidad }
        totalGastos =
            lista.filter { it.tipo == Movimiento.Tipo.GASTO }.sumOf { it.cantidad }
    }

    // ---------- cargas "tradicionales" ----------

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

    // ---------- inserciones ----------

    suspend fun agregarGasto(
        familiaId: String,
        cantidad: Double,
        categoria: String?,
        nota: String? = null,
        fechaMillis: Long
    ) {
        repo.agregarMovimiento(
            Movimiento(
                id = "",
                familiaId = familiaId,
                cantidad = cantidad,
                categoria = categoria,
                nota = nota,
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
        nota: String? = null,
        fechaMillis: Long
    ) {
        repo.agregarMovimiento(
            Movimiento(
                id = "",
                familiaId = familiaId,
                cantidad = cantidad,
                categoria = categoria,
                nota = nota,
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

    // ---------- moneda ----------

    fun cambiarMoneda(nueva: String) {
        if (nueva == monedaActual) return
        val factor =
            (conversionRates[nueva] ?: 1.0) / (conversionRates[monedaActual] ?: 1.0)
        setItems(
            _itemsDelMesState.value.map {
                it.copy(cantidad = it.cantidad * factor)
            }
        )
        monedaActual = nueva
    }

    // ---------- cambio de familia ----------

    fun onFamiliaCambiada(familiaId: String) {
        resetEstado()
        cargarMesActual(familiaId)
    }

    private fun resetEstado() {
        escuchaJob?.cancel()
        movimientosTiempoReal = emptyList()

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

    // ---------- util ----------

    private fun yearActualOrNow(): Int =
        if (yearActual == 0) Calendar.getInstance().get(Calendar.YEAR) else yearActual

    private fun monthActualOrNow(): Int =
        if (monthActual == 0) Calendar.getInstance().get(Calendar.MONTH) + 1 else monthActual

    private fun rangoMesActualMillis(): Pair<Long, Long> {
        val y = yearActualOrNow()
        val m = monthActualOrNow()
        val inicio = Calendar.getInstance().apply {
            set(Calendar.YEAR, y)
            set(Calendar.MONTH, m - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val fin = Calendar.getInstance().apply {
            set(Calendar.YEAR, y)
            set(Calendar.MONTH, m - 1)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        return inicio to fin
    }

    fun nombreMesActual(): String {
        val y = yearActualOrNow()
        val m = monthActualOrNow()
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, y)
            set(Calendar.MONTH, m - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val fmt = java.text.SimpleDateFormat("LLLL yyyy", Locale("es", "ES"))
        return fmt.format(cal.time)
            .replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString()
            }
    }

    override fun onCleared() {
        super.onCleared()
        escuchaJob?.cancel()
    }
}



















