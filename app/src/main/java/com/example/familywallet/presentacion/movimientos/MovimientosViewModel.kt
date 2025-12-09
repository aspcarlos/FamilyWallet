package com.example.familywallet.presentacion.movimientos

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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

/**
 * ViewModel principal de la parte económica.
 * Gestiona lista de movimientos, totales, filtros de periodo y conversión de moneda.
 */
class MovimientosViewModel(
    private val repo: MovimientoRepositorio
) : ViewModel() {

    // Lista usada en pantallas de historial/detalle.
    private val _itemsDelMesState = mutableStateOf<List<Movimiento>>(emptyList())
    val itemsDelMesState: State<List<Movimiento>> get() = _itemsDelMesState

    // Error observable para mostrar mensajes en UI.
    private val _errorMsg = mutableStateOf<String?>(null)
    val errorMsg: State<String?> get() = _errorMsg

    // Estado que leen las pantallas Compose.
    var monedaActual by mutableStateOf("EUR")
        private set
    var totalIngresos by mutableDoubleStateOf(0.0)
        private set
    var totalGastos by mutableDoubleStateOf(0.0)
        private set
    var etiquetaPeriodo by mutableStateOf("")
        private set
    var filtroPeriodo by mutableStateOf(FiltroPeriodo.MES)
        private set

    // Último rango aplicado (día/semana/mes/año) para recargas coherentes.
    private var ultimoRango: RangoFecha? = null
    private var yearActual: Int = 0
    private var monthActual: Int = 0

    // Conversión simple offline para demo sin dependencias externas.
    private val conversionRates = mapOf(
        "EUR" to 1.0,
        "USD" to 1.08,
        "GBP" to 0.85,
        "JPY" to 161.3,
        "MXN" to 19.5
    )

    // Cache de movimientos recibidos por el listener en tiempo real.
    private var movimientosTiempoReal: List<Movimiento> = emptyList()

    // Job para cancelar la escucha cuando cambia la familia o se destruye el VM.
    private var escuchaJob: Job? = null

    /**
     * Inicia un listener en tiempo real para todos los movimientos de una familia.
     * Cada actualización recalcula los totales según el rango actual.
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
     * Aplica un rango de fechas y actualiza etiqueta.
     * Si hay tiempo real activo, recalcula desde cache; si no, carga desde repositorio.
     */
    fun aplicarRango(familiaId: String, rango: RangoFecha) {
        etiquetaPeriodo = rango.etiqueta
        ultimoRango = rango

        if (movimientosTiempoReal.isNotEmpty()) {
            recomputarTotalesTiempoReal()
        } else {
            cargarRango(familiaId, rango.inicio, rango.fin)
        }
    }

    /**
     * Recalcula ingresos y gastos usando la lista en memoria del tiempo real.
     * Si no hay rango seleccionado, usa el mes actual calculado localmente.
     */
    private fun recomputarTotalesTiempoReal() {
        val lista = movimientosTiempoReal
        val rango = ultimoRango

        val filtrados = if (rango != null) {
            lista.filter { it.fechaMillis in rango.inicio..rango.fin }
        } else {
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

    /**
     * Actualiza la lista principal usada por pantallas de historial.
     * Tras setear, recalcula totales en base a esa lista.
     */
    private fun setItems(nuevos: List<Movimiento>) {
        _itemsDelMesState.value = nuevos
        recomputarTotalesLista()
    }

    /**
     * Recalcula totales leyendo directamente la lista de items del mes/rango.
     * Se usa cuando no dependemos del flujo en tiempo real.
     */
    private fun recomputarTotalesLista() {
        val lista = _itemsDelMesState.value
        totalIngresos =
            lista.filter { it.tipo == Movimiento.Tipo.INGRESO }.sumOf { it.cantidad }
        totalGastos =
            lista.filter { it.tipo == Movimiento.Tipo.GASTO }.sumOf { it.cantidad }
    }

    // ---------------- CARGAS ----------------

    /**
     * Carga el mes actual del calendario del dispositivo.
     * Útil como estado inicial cuando el usuario entra a la familia.
     */
    fun cargarMesActual(familiaId: String) {
        val cal = Calendar.getInstance()
        cargarMes(familiaId, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    /**
     * Carga movimientos de un mes concreto desde el repositorio.
     * Guarda year/month para mantener coherencia en futuras recargas.
     */
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

    /**
     * Carga movimientos entre dos fechas en milisegundos.
     * Se usa para filtros de día/semana/mes/año.
     */
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

    // ---------------- INSERCIONES ----------------

    /**
     * Crea y guarda un movimiento de tipo GASTO.
     * Después recarga el rango/mes actual para reflejar cambios en UI.
     */
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

    /**
     * Crea y guarda un movimiento de tipo INGRESO.
     * Mantiene el mismo flujo de recarga para consistencia visual.
     */
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

    /**
     * Recarga la pantalla según el último rango aplicado.
     * Si no hay rango, usa el último mes recordado o el actual.
     */
    private fun recargarDespuesDeInsert(familiaId: String) {
        val r = ultimoRango
        if (r != null) {
            cargarRango(familiaId, r.inicio, r.fin)
        } else {
            cargarMes(familiaId, yearActualOrNow(), monthActualOrNow())
        }
    }

    // ---------------- MONEDA ----------------

    /**
     * Cambia la moneda aplicando un factor de conversión local.
     * Reescribe la lista actual para que los totales se actualicen automáticamente.
     */
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

    // ---------------- CAMBIO DE FAMILIA ----------------

    /**
     * Se llama cuando el usuario cambia de familia.
     * Limpia estado y carga el mes actual para la nueva familia.
     */
    fun onFamiliaCambiada(familiaId: String) {
        resetEstado()
        cargarMesActual(familiaId)
    }

    /**
     * Limpia caches, totales, filtros y errores.
     * También cancela el listener en tiempo real activo.
     */
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

    // ---------------- UTILIDADES ----------------

    /** Devuelve el año recordado; si no existe, usa el año actual del sistema. */
    private fun yearActualOrNow(): Int =
        if (yearActual == 0) Calendar.getInstance().get(Calendar.YEAR) else yearActual

    /** Devuelve el mes recordado; si no existe, usa el mes actual del sistema. */
    private fun monthActualOrNow(): Int =
        if (monthActual == 0) Calendar.getInstance().get(Calendar.MONTH) + 1 else monthActual

    /**
     * Calcula inicio y fin del mes actual (según year/month recordados).
     * Se usa como fallback cuando no se ha seleccionado un rango explícito.
     */
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


    // Devuelve un texto “Mes Año” para mostrarlo en UI.
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

    // Cancela la escucha en tiempo real al destruir el ViewModel.
    override fun onCleared() {
        super.onCleared()
        escuchaJob?.cancel()
    }
}




















