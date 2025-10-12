package com.example.familywallet.presentacion.inicio

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.familywallet.presentacion.movimientos.MovimientosViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PantallaInicio(
    familiaId: String,
    vm: MovimientosViewModel,
    onIrAddGasto: () -> Unit,
    onIrAddIngreso: () -> Unit,
    onIrHistorial: () -> Unit,
    onBackToConfig: () -> Unit
) {

    LaunchedEffect(familiaId) {
        vm.cargarMesActual(familiaId)
    }

    val moneda = remember { NumberFormat.getCurrencyInstance(Locale("es", "ES")) }

    Scaffold(
        // Botón "Atrás" fijo abajo a la izquierda
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                OutlinedButton(onClick = onBackToConfig) {
                    Text("Atrás")
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Mes actual
            Text(
                text = vm.nombreMesActual(), // asegúrate de tener este helper en tu VM
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(12.dp))

            // Totales
            Text("Ingresos: ${moneda.format(vm.totalIngresos)}")
            Text("Gastos:   ${moneda.format(vm.totalGastos)}")

            val resumen = vm.totalIngresos - vm.totalGastos
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Resumen: ${moneda.format(resumen)}",
                style = MaterialTheme.typography.headlineMedium,
                color = if (resumen < 0) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )

            // Botón Ver historial centrado
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onIrHistorial,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) { Text("Ver historial") }

            // Botones de añadir centrados y debajo del historial
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onIrAddGasto,
                    modifier = Modifier.weight(1f)
                ) { Text("Añadir gasto") }

                Button(
                    onClick = onIrAddIngreso,
                    modifier = Modifier.weight(1f)
                ) { Text("Añadir ingreso") }
            }
        }
    }
}









