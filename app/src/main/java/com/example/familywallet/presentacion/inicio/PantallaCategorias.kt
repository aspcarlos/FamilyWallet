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

// Modelo interno de UI para resumir gastos por categoría.
private data class CategoriaResumen(
    val nombreCompleto: String, // Nombre guardado en el movimiento (puede incluir compatibilidad "Categoria · Nota").
    val total: Double,          // Suma total de gastos de esa categoría en el periodo cargado.
    val ultimaFecha: Long       // Fecha más reciente encontrada para ordenar por "más nuevo".
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCategorias(
    vm: MovimientosViewModel,
    onBack: () -> Unit
) {
    // Formateador de moneda según la moneda actual del ViewModel.
    val formatter = rememberCurrencyFormatter(vm.monedaActual)

    // Lista de movimientos del periodo activo (mes/rango) ya cargado por el VM.
    val items by vm.itemsDelMesState

    // Genera el resumen de categorías solo cuando cambia la lista de movimientos.
    val totalesPorCategoria = remember(items) {
        items
            .asSequence()
            .filter { it.tipo == Movimiento.Tipo.GASTO }     // Solo gastos.
            .groupBy { it.categoria ?: "Otros" }             // Agrupa por categoría.
            .map { (categoriaCompleta, lista) ->
                CategoriaResumen(
                    nombreCompleto = categoriaCompleta,
                    total = lista.sumOf { it.cantidad },     // Suma de importes.
                    ultimaFecha = lista.maxOfOrNull { it.fechaMillis } ?: 0L // Último gasto.
                )
            }
            .sortedByDescending { it.ultimaFecha }          // Ordena por gasto más reciente.
    }

    // Scaffold común de la app con barra superior y botón de volver.
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
            // Título centrado de pantalla.
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

            // Estado vacío si no hay gastos en el periodo.
            if (totalesPorCategoria.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay gastos en este periodo.")
                }
            } else {
                // Lista de categorías con su total formateado.
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(totalesPorCategoria, key = { it.nombreCompleto }) { cat ->
                        // Limpia el nombre por compatibilidad con formato antiguo "Categoria · Nota".
                        val nombreVisible = cat.nombreCompleto
                            .substringBefore(" · ")
                            .ifBlank { "Otros" }

                        ListItem(
                            headlineContent = { Text(nombreVisible) },
                            trailingContent = {
                                Text(
                                    text = formatter.format(cat.total), // Muestra total por categoría.
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














