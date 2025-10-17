package com.example.familywallet.presentacion.movimientos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun PantallaAgregarIngreso(
    familiaId: String,
    vm: MovimientosViewModel,
    onGuardado: () -> Unit
) {
    var cantidadText by remember { mutableStateOf("") }
    var nota by remember { mutableStateOf("") } // opcional, si quieres una “categoría/nota”
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Nuevo ingreso", style = MaterialTheme.typography.headlineSmall)

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        OutlinedTextField(
            value = cantidadText,
            onValueChange = { cantidadText = it },
            label = { Text("Cantidad") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = nota,
            onValueChange = { nota = it },
            label = { Text("Nota (opcional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val cantidad = cantidadText.replace(',', '.').toDoubleOrNull()
                when {
                    cantidad == null || cantidad <= 0.0 ->
                        error = "Introduce una cantidad válida"
                    else -> {
                        error = null
                        val fecha = System.currentTimeMillis()
                        scope.launch {
                            // 👇 espera a que termine de guardar y recargar
                            vm.agregarIngreso(
                                familiaId = familiaId,
                                cantidad = cantidad,
                                categoria = if (nota.isBlank()) null else nota,
                                fechaMillis = fecha
                            )
                            onGuardado() // vuelve después de recargar
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Guardar ingreso") }

    }
}





