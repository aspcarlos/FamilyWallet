package com.example.familywallet.presentacion.movimientos

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.familywallet.datos.modelos.Movimiento
import com.example.familywallet.presentacion.ui.rememberCurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
private fun FechaCorta(millis: Long): String {
    val fmt = remember { SimpleDateFormat("dd MMM yyyy", Locale("es", "ES")) }
    return fmt.format(Date(millis))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHistorialMes(
    familiaId: String,
    year: Int,
    month: Int,
    vm: MovimientosViewModel,
    onBack: () -> Unit
) {
    val items = vm.itemsDelMesState.value
    val formatter = rememberCurrencyFormatter(vm.monedaActual)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle • $year/$month") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            items(items) { mov ->
                ListItem(
                    headlineContent = { Text(mov.categoria ?: "Movimiento") },
                    supportingContent = { Text(FechaCorta(mov.fechaMillis)) },
                    trailingContent = {
                        val sign = if (mov.tipo == Movimiento.Tipo.GASTO) -1 else 1
                        Text(formatter.format(sign * abs(mov.cantidad)))
                    }
                )
                Divider()
            }
        }
    }
}








