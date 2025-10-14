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
import java.util.*

@Composable
private fun rememberCurrencyFormatter(currencyCode: String): NumberFormat {
    return remember(currencyCode) {
        NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance(currencyCode)
        }
    }
}

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
    onCambiarMoneda: () -> Unit = {},
    onVerCategorias: () -> Unit,
    appName: String = "FamilyWallet"
) {
    // 1) Cargar datos al entrar / cambiar familia
    LaunchedEffect(familiaId) {
        vm.cargarMesActual(familiaId)
    }

    // 2) Estado y formateador
    val items by vm.itemsDelMesState
    val ingresos = vm.totalIngresos
    val gastos   = vm.totalGastos
    val formatter = rememberCurrencyFormatter(vm.monedaActual)

    // 3) Menú del AppBar
    var menuAbierto by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FamilyWallet") },
                actions = {
                    IconButton(onClick = { menuAbierto = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
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
                        DropdownMenuItem(
                            text = { Text("Moneda") },
                            onClick = {
                                menuAbierto = false
                                onCambiarMoneda()    // <-- aquí lo usamos
                            }
                        )
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Mes en curso (si tienes un método en el VM que lo devuelva, úsalo)
            Text(
                text = vm.nombreMesActual(),
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(24.dp))

            Text(text = "Ingresos: ${formatter.format(ingresos)}")
            Spacer(Modifier.height(8.dp))
            Text(text = "Gastos: ${formatter.format(gastos)}")
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Resumen: ${formatter.format(ingresos - gastos)}",
                color = if ((ingresos - gastos) >= 0) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )

            Spacer(Modifier.height(24.dp))

            Button(onClick = onIrHistorial) {
                Text("Ver historial")
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                OutlinedButton(onClick = onIrAddGasto) { Text("Añadir gasto") }
                OutlinedButton(onClick = onIrAddIngreso) { Text("Añadir ingreso") }
            }

            Spacer(Modifier.weight(1f))

            // Botón Atrás (a ConfigFamilia)
            OutlinedButton(
                onClick = onBackToConfig,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text("Atrás")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}












