package com.example.familywallet.presentacion.movimientos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHistorial(
    familiaId: String,
    vm: MovimientosViewModel,
    onAbrirMes: (year: Int, month: Int) -> Unit,
    onBack: () -> Unit
) {
    val c = Calendar.getInstance()
    val year = c.get(Calendar.YEAR)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial $year") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            (1..12).forEach { m ->
                val nombreMes = c.apply { set(Calendar.MONTH, m - 1) }
                    .getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
                    ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    ?: "Mes $m"

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAbrirMes(year, m) }
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(nombreMes, style = MaterialTheme.typography.titleMedium)
                        Text("Ver detalles", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}






