package com.example.familywallet.presentacion.inicio

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.familywallet.presentacion.movimientos.MovimientosViewModel
import com.example.familywallet.datos.modelos.Movimiento // usa tu paquete real de Movimiento
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCategorias(
    vm: MovimientosViewModel,
    onBack: () -> Unit
) {
    // Lista del mes ya cargada por PantallaInicio (reactiva)
    val items by vm.itemsDelMesState  // <- tu State<List<Movimiento>> del VM

    val moneda = remember { NumberFormat.getCurrencyInstance(Locale("es", "ES")) }

    // Tus categorías “fijas”
    val categorias = listOf(
        "automóvil","casa","comida","comunicaciones","deportes","entretenimiento",
        "facturas","higiene","mascotas","regalos","restaurante","ropa","salud","taxi","transporte"
    )

    // Totales por categoría SOLO de GASTOS del mes (reactivo: se recalcula cuando 'items' cambia)
    val totalesPorCategoria: Map<String, Double> = remember(items) {
        items
            .asSequence()
            .filter { it.tipo == Movimiento.Tipo.GASTO }
            .groupBy { it.categoria ?: "otros" }
            .mapValues { (_, lista) -> lista.sumOf { it.cantidad } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categorías de gasto") }
            )
        }
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp) // hueco para el botón Atrás
            ) {
                items(categorias) { c ->
                    val total = totalesPorCategoria[c] ?: 0.0
                    ListItem(
                        headlineContent = { Text(c) },
                        trailingContent = { Text(moneda.format(total)) }
                    )
                    Divider()
                }
            }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) { Text("Atrás") }
        }
    }
}

