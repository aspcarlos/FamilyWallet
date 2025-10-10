package com.example.familywallet.presentacion.movimientos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHistorialMes(
    familiaId: String,
    year: Int,
    month: Int, // 1..12
    vm: MovimientosViewModel,
    onBack: () -> Unit
) {
    // ------------- DATOS Y FORMATO -------------
    val items = remember(familiaId, year, month, vm.lista.size) {
        vm.movimientosDeMes(familiaId, year, month)
    }

    val moneda = remember {
        NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    }

    // ------------- UI -------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle • %04d/%02d".format(year, month)) },
                navigationIcon = { IconButton(onClick = onBack) { Text("<") } }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay movimientos en este mes.")
                }
            } else {
                items.forEach { mov ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                if (mov.tipo == Tipo.GASTO) "Gasto" else "Ingreso",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text("Cantidad: ${moneda.format(mov.cantidad)}")
                            mov.categoria?.let { Text("Categoría: $it") }
                        }
                    }
                }
            }
        }
    }
}





