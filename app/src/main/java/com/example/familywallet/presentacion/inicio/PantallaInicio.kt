package com.example.familywallet.presentacion.inicio

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.familywallet.datos.repositorios.ServiceLocator
import com.example.familywallet.presentacion.movimientos.MovimientosVMFactory
import com.example.familywallet.presentacion.movimientos.MovimientosViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PantallaInicio(
    familiaId: String,
    vm: MovimientosViewModel,
    onIrAddGasto: () -> Unit,
    onIrAddIngreso: () -> Unit,
    onIrHistorial: () -> Unit
) {
    // ✅ Crear el ViewModel con la factory conectada a Firebase
    val vm: MovimientosViewModel = viewModel(
        factory = MovimientosVMFactory(ServiceLocator.movimientosRepo)
    )

    val items by vm.itemsDelMesState

    // ✅ Cargar datos al abrir o cambiar familia
    LaunchedEffect(familiaId) {
        vm.cargarMesActual(familiaId)
    }

    // Formato de moneda
    val moneda = remember { NumberFormat.getCurrencyInstance(Locale("es", "ES")) }

    // Contenido principal
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                vm.nombreMesActual(),
                style = MaterialTheme.typography.headlineSmall
            )

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

            OutlinedButton(onClick = onIrHistorial) {
                Text("Ver historial")
            }
        }
    }

    // Botones inferiores
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        ElevatedButton(onClick = onIrAddGasto) { Text("Añadir gasto") }
        ElevatedButton(onClick = onIrAddIngreso) { Text("Añadir ingreso") }
    }
}








