package com.example.familywallet.presentacion.inicio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.familywallet.datos.modelos.Movimiento
import com.example.familywallet.presentacion.movimientos.MovimientosViewModel
import com.example.familywallet.presentacion.ui.rememberCurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCategorias(
    vm: MovimientosViewModel,
    onBack: () -> Unit
) {
    // Formateador de moneda reactivo al cambio de vm.monedaActual
    val formatter = rememberCurrencyFormatter(vm.monedaActual)

    // Lista reactiva del mes
    val items by vm.itemsDelMesState

    // Totales de GASTO por categoría (se recalcula cuando 'items' cambia)
    val totalesPorCategoria = remember(items) {
        items
            .asSequence()
            .filter { it.tipo == Movimiento.Tipo.GASTO }      // si tu 'tipo' es String, usa: it.tipo == "GASTO"
            .groupBy { it.categoria ?: "otros" }
            .map { (categoria, lista) ->                      // devolvemos Pair(categoria,total)
                categoria to lista.sumOf { it.cantidad }
            }
            .sortedBy { it.first }                             // opcional: orden alfabético
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Categorías de gasto") })
        },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                OutlinedButton(onClick = onBack) { Text("Atrás") }
            }
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(totalesPorCategoria, key = { it.first }) { (categoria, total) ->
                ListItem(
                    headlineContent = { Text(categoria) },
                    trailingContent = {
                        Text(
                            text = formatter.format(total),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                )
                Divider()
            }
        }
    }
}




