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
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(
    familiaId: String,
    vm: MovimientosViewModel,
    onIrAddGasto: () -> Unit,
    onIrAddIngreso: () -> Unit,
    onIrHistorial: () -> Unit,

    onVerCategorias: () -> Unit,
    onAbrirConfiguracion: () -> Unit,
    onBackToConfig: () -> Unit, // si ya lo tienes
    appName: String = "FamilyWallet"
) {
    // Recalcular al entrar o cambiar lista
    LaunchedEffect(familiaId, vm.itemsDelMesState.value) {
        vm.cargarMesActual(familiaId)
    }

    val moneda = remember { NumberFormat.getCurrencyInstance(Locale("es","ES")) }
    var menuAbierto by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(appName) },
                actions = {
                    IconButton(onClick = { menuAbierto = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Más opciones"
                        )
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
            // Si mantienes el botón "Atrás" abajo a la izquierda:
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = onBackToConfig) { Text("Atrás") }
                // Si quieres, aquí ya no pones Add gasto/ingreso
            }
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(vm.nombreMesActual(), style = MaterialTheme.typography.headlineSmall)

                Text("Ingresos: ${moneda.format(vm.totalIngresos)}")
                Text("Gastos:   ${moneda.format(vm.totalGastos)}")

                val resumen = vm.totalIngresos - vm.totalGastos
                Text(
                    "Resumen: ${moneda.format(resumen)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (resumen < 0)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(8.dp))

                // Botón historial
                OutlinedButton(onClick = onIrHistorial) { Text("Ver historial") }

                // Botones centrados, debajo del historial
                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ElevatedButton(onClick = onIrAddGasto) { Text("Añadir gasto") }
                    ElevatedButton(onClick = onIrAddIngreso) { Text("Añadir ingreso") }
                }
            }
        }
    }
}










