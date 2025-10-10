package com.example.familywallet.presentacion.movimientos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PantallaAgregarIngreso(
    familiaId: String,
    vm: MovimientosViewModel,
    onGuardado: () -> Unit
) {
    var cantidadText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Nuevo ingreso", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = cantidadText,
            onValueChange = { cantidadText = it },
            label = { Text("Cantidad (€)") },
            singleLine = true
        )

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(
            onClick = {
                val cantidad = cantidadText.replace(',', '.').toDoubleOrNull()
                if (cantidad == null || cantidad <= 0.0) {
                    error = "Introduce una cantidad válida"
                } else {
                    vm.agregarIngreso(familiaId, cantidad)
                    onGuardado()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Guardar ingreso") }
    }
}




