package com.example.familywallet.presentacion.inicio

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.familywallet.presentacion.movimientos.MovimientosViewModel
import com.example.familywallet.presentacion.ui.rememberCurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(
    familiaId: String,
    vm: MovimientosViewModel,
    onIrAddGasto: () -> Unit,
    onIrAddIngreso: () -> Unit,
    onIrHistorial: () -> Unit,
    onBackToConfig: () -> Unit,
    onAbrirConfiguracion: () -> Unit,
    onVerCategorias: () -> Unit,
    onCambiarMoneda: () -> Unit,        // <- NUEVO
    appName: String = "FamilyWallet"
) {
    // ← formateador se “engancha” a la moneda actual del VM
    val formatter = rememberCurrencyFormatter(vm.monedaActual)

    // Si totalIngresos/totalGastos son State<Double> en el VM, léelos con 'by'
    val totalIngresos by remember { vm::totalIngresos }.let { derivedStateOf { it.get() } }
    val totalGastos   by remember { vm::totalGastos   }.let { derivedStateOf { it.get() } }

    var menuAbierto by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(appName) },
                actions = {
                    IconButton(onClick = { menuAbierto = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = menuAbierto,
                        onDismissRequest = { menuAbierto = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Categorías de gastos") },
                            onClick = {
                                menuAbierto = false
                                onVerCategorias()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Moneda") },               // <- NUEVA OPCIÓN
                            onClick = {
                                menuAbierto = false
                                onCambiarMoneda()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Configuración") },
                            onClick = {
                                menuAbierto = false
                                onAbrirConfiguracion()
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                OutlinedButton(onClick = onBackToConfig) { Text("Atrás") }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            // Mes actual ya lo tienes calculado en tu UI
            Text("Octubre 2025", style = MaterialTheme.typography.titleLarge)

            Text("Ingresos: ${formatter.format(totalIngresos)}")
            Text("Gastos:   ${formatter.format(totalGastos)}")

            val resumen = totalIngresos - totalGastos
            Text(
                "Resumen: ${formatter.format(resumen)}",
                color = if (resumen >= 0.0) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )

            Spacer(Modifier.height(8.dp))
            Button(onClick = onIrHistorial) { Text("Ver historial") }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = onIrAddGasto)   { Text("Añadir gasto") }
                OutlinedButton(onClick = onIrAddIngreso) { Text("Añadir ingreso") }
            }
        }
    }
}











