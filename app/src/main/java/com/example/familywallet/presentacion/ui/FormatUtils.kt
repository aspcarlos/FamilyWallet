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

// Enum para representar el filtro de periodo elegido en la app
enum class FiltroPeriodo { DIA, SEMANA, MES, ANIO }

// Modelo simple para un rango de fechas con etiqueta de UI
data class RangoFecha(
    val inicio: Long,
    val fin: Long,
    val etiqueta: String
)

// Crea un Calendar base consistente (lunes como inicio de semana)
private fun calBase(
    tz: TimeZone = TimeZone.getDefault(),
    locale: Locale = Locale("es", "ES")
): Calendar = Calendar.getInstance(tz, locale).apply {
    firstDayOfWeek = Calendar.MONDAY
}

// Devuelve el rango del día actual en millis + etiqueta legible
fun rangoDiaActual(locale: Locale = Locale("es", "ES")): RangoFecha {
    val c = calBase(locale = locale)
    // Ajusta al inicio del día
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    val ini = c.timeInMillis
    // Fin del día = justo antes de empezar el siguiente
    c.add(Calendar.DAY_OF_MONTH, 1)
    val fin = c.timeInMillis - 1

    val fmt = SimpleDateFormat("d MMM yyyy", locale)
    return RangoFecha(ini, fin, fmt.format(Date(ini)))
}

// Devuelve el rango de la semana actual (lunes a domingo) + etiqueta
fun rangoSemanaActual(locale: Locale = Locale("es", "ES")): RangoFecha {
    val c = calBase(locale = locale)
    // Ajusta al inicio del día
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)

    // Fuerza inicio en lunes
    c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    val ini = c.timeInMillis

    // Fin de semana = 7 días después menos 1 ms
    c.add(Calendar.DAY_OF_MONTH, 7)
    val fin = c.timeInMillis - 1

    val fmt = SimpleDateFormat("d MMM", locale)
    val etiqueta = "${fmt.format(Date(ini))} - ${fmt.format(Date(fin))}"
    return RangoFecha(ini, fin, etiqueta)
}

// Devuelve el rango del mes actual completo + etiqueta del mes
fun rangoMesActual(locale: Locale = Locale("es", "ES")): RangoFecha {
    val c = calBase(locale = locale)
    // Primer día del mes a las 00:00
    c.set(Calendar.DAY_OF_MONTH, 1)
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    val ini = c.timeInMillis

    // Fin de mes = inicio del mes siguiente menos 1 ms
    c.add(Calendar.MONTH, 1)
    val fin = c.timeInMillis - 1

    val fmt = SimpleDateFormat("LLLL yyyy", locale)
    val etiqueta = fmt.format(Date(ini)).replaceFirstChar { it.titlecase(locale) }
    return RangoFecha(ini, fin, etiqueta)
}

// Devuelve el rango del año actual completo + etiqueta del año
fun rangoAnioActual(locale: Locale = Locale("es", "ES")): RangoFecha {
    val c = calBase(locale = locale)
    // 1 de enero a las 00:00
    c.set(Calendar.MONTH, Calendar.JANUARY)
    c.set(Calendar.DAY_OF_MONTH, 1)
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    val ini = c.timeInMillis

    // Fin de año = inicio del siguiente año menos 1 ms
    c.add(Calendar.YEAR, 1)
    val fin = c.timeInMillis - 1

    val fmt = SimpleDateFormat("yyyy", locale)
    return RangoFecha(ini, fin, fmt.format(Date(ini)))
}

// Mapea el filtro elegido a su rango concreto
fun rangoPorFiltro(
    filtro: FiltroPeriodo,
    locale: Locale = Locale("es", "ES")
): RangoFecha = when (filtro) {
    FiltroPeriodo.DIA    -> rangoDiaActual(locale)
    FiltroPeriodo.SEMANA -> rangoSemanaActual(locale)
    FiltroPeriodo.MES    -> rangoMesActual(locale)
    FiltroPeriodo.ANIO   -> rangoAnioActual(locale)
}

// Crea y memoriza un formateador de moneda para no recrearlo en cada recomposición
@Composable
fun rememberCurrencyFormatter(
    currencyCode: String,
    locale: Locale = Locale.getDefault()
): NumberFormat {
    return remember(currencyCode, locale) {
        // Devuelve formato local con la divisa seleccionada
        NumberFormat.getCurrencyInstance(locale).apply {
            currency = Currency.getInstance(currencyCode)
        }
    }
}







