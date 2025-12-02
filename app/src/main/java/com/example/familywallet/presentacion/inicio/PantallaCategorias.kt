package com.example.familywallet.presentacion.inicio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.familywallet.datos.modelos.Movimiento
import com.example.familywallet.presentacion.movimientos.MovimientosViewModel
import com.example.familywallet.presentacion.ui.ScreenScaffold
import com.example.familywallet.presentacion.ui.rememberCurrencyFormatter

private data class CategoriaResumen(
    val nombreCompleto: String,
    val total: Double,
    val ultimaFecha: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCategorias(
    vm: MovimientosViewModel,
    onBack: () -> Unit
) {
    val formatter = rememberCurrencyFormatter(vm.monedaActual)
    val items by vm.itemsDelMesState

    // Agrupamos igual que antes, pero ahora calculamos también la fecha más reciente
    val totalesPorCategoria = remember(items) {
        items
            .asSequence()
            .filter { it.tipo == Movimiento.Tipo.GASTO }
            .groupBy { it.categoria ?: "Otros" }
            .map { (categoriaCompleta, lista) ->
                CategoriaResumen(
                    nombreCompleto = categoriaCompleta,
                    total = lista.sumOf { it.cantidad },
                    ultimaFecha = lista.maxOfOrNull { it.fechaMillis } ?: 0L
                )
            }
            // categoría cuyo gasto sea más reciente, primero
            .sortedByDescending { it.ultimaFecha }
    }

    ScreenScaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Categorías de gasto",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (totalesPorCategoria.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay gastos en este periodo.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(totalesPorCategoria, key = { it.nombreCompleto }) { cat ->
                        val nombreVisible = cat.nombreCompleto
                            .substringBefore(" · ")
                            .ifBlank { "Otros" }

                        ListItem(
                            headlineContent = { Text(nombreVisible) },
                            trailingContent = {
                                Text(
                                    text = formatter.format(cat.total),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}













