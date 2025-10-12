package com.example.familywallet.presentacion.movimientos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.familywallet.datos.modelos.Movimiento
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHistorialMes(
    familiaId: String,
    year: Int,
    month: Int,
    vm: MovimientosViewModel,
    onBack: () -> Unit
) {
    // Cargar/recargar datos del mes seleccionado
    LaunchedEffect(familiaId, year, month) {
        vm.cargarMes(familiaId, year, month)
    }

    // Obtén la lista del VM (StateFlow/LiveData o simple State)
    // Adapta esta línea a cómo lo tengas:
    // Si usas StateFlow:
    // val items by vm.itemsDelMes.collectAsState()
    // Si usas un State interno del VM:
    val items by vm.itemsDelMesState

    val moneda = remember { NumberFormat.getCurrencyInstance(Locale("es", "ES")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle • %04d/%02d".format(year, month)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("<") }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (items.isEmpty()) {
                Text("No hay movimientos en este mes.")
            } else {
                items.forEach { mov: Movimiento ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                text = if (mov.tipo == Movimiento.Tipo.GASTO) "Gasto" else "Ingreso",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text("Cantidad: ${moneda.format(mov.cantidad)}")
                            mov.categoria?.let { Text("Categoría: $it") }
                            // Si quieres mostrar fecha:
                            // Text("Fecha: " + vm.formatoFechaCorta(mov.fechaMillis))
                        }
                    }
                }
            }
        }
    }
}






