package com.example.familywallet.presentacion.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Currency
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Filtros para la barra de “Día / Semana / Mes / Año”
enum class FiltroPeriodo { DIA, SEMANA, MES, ANIO }

// Rango temporal + etiqueta lista para mostrar en la UI
data class RangoFecha(
    val inicio: Long,   // millis (inclusive)
    val fin: Long,      // millis (inclusive)
    val etiqueta: String
)

/** Calendario base consistente (lunes como inicio de semana). */
private fun calBase(
    tz: TimeZone = TimeZone.getDefault(),
    locale: Locale = Locale("es", "ES")
): Calendar = Calendar.getInstance(tz, locale).apply {
    firstDayOfWeek = Calendar.MONDAY
}

/** Rango del día actual (00:00:00.000 … 23:59:59.999). */
fun rangoDiaActual(locale: Locale = Locale("es", "ES")): RangoFecha {
    val c = calBase(locale = locale)
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    val ini = c.timeInMillis
    c.add(Calendar.DAY_OF_MONTH, 1)
    val fin = c.timeInMillis - 1

    val fmt = SimpleDateFormat("d MMM yyyy", locale)
    return RangoFecha(ini, fin, fmt.format(Date(ini)))
}

/** Rango de la semana actual (lunes 00:00 … domingo 23:59). */
fun rangoSemanaActual(locale: Locale = Locale("es", "ES")): RangoFecha {
    val c = calBase(locale = locale)
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)

    // inicio semana (lunes)
    c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    val ini = c.timeInMillis

    // fin semana (domingo)
    c.add(Calendar.DAY_OF_MONTH, 7)
    val fin = c.timeInMillis - 1

    val fmt = SimpleDateFormat("d MMM", locale)
    val etiqueta = "${fmt.format(Date(ini))} - ${fmt.format(Date(fin))}"
    return RangoFecha(ini, fin, etiqueta)
}

/** Rango del mes actual (1er día 00:00 … último día 23:59). */
fun rangoMesActual(locale: Locale = Locale("es", "ES")): RangoFecha {
    val c = calBase(locale = locale)
    c.set(Calendar.DAY_OF_MONTH, 1)
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    val ini = c.timeInMillis

    c.add(Calendar.MONTH, 1)
    val fin = c.timeInMillis - 1

    val fmt = SimpleDateFormat("LLLL yyyy", locale)
    val etiqueta = fmt.format(Date(ini)).replaceFirstChar { it.titlecase(locale) }
    return RangoFecha(ini, fin, etiqueta)
}

/** Rango del año actual (1 de enero 00:00 … 31 de dic. 23:59). */
fun rangoAnioActual(locale: Locale = Locale("es", "ES")): RangoFecha {
    val c = calBase(locale = locale)
    c.set(Calendar.MONTH, Calendar.JANUARY)
    c.set(Calendar.DAY_OF_MONTH, 1)
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    val ini = c.timeInMillis

    c.add(Calendar.YEAR, 1)
    val fin = c.timeInMillis - 1

    val fmt = SimpleDateFormat("yyyy", locale)
    return RangoFecha(ini, fin, fmt.format(Date(ini)))
}

/** Helper para obtener el rango a partir del filtro seleccionado. */
fun rangoPorFiltro(
    filtro: FiltroPeriodo,
    locale: Locale = Locale("es", "ES")
): RangoFecha = when (filtro) {
    FiltroPeriodo.DIA    -> rangoDiaActual(locale)
    FiltroPeriodo.SEMANA -> rangoSemanaActual(locale)
    FiltroPeriodo.MES    -> rangoMesActual(locale)
    FiltroPeriodo.ANIO   -> rangoAnioActual(locale)
}

/**
 * NumberFormat memorizado por código de moneda y locale.
 * Cuando cambie `currencyCode` o `locale`, Compose recompone y el símbolo/normas
 * de formateo se actualizan automáticamente.
 */
@Composable
fun rememberCurrencyFormatter(
    currencyCode: String,
    locale: Locale = Locale.getDefault()
): NumberFormat {
    return remember(currencyCode, locale) {
        NumberFormat.getCurrencyInstance(locale).apply {
            currency = Currency.getInstance(currencyCode)
        }
    }
}






