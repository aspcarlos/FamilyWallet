package com.example.familywallet.presentacion.movimientos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.familywallet.datos.modelos.CATEGORIAS_GASTO
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAgregarGasto(
    familiaId: String,
    vm: MovimientosViewModel,
    onGuardado: () -> Unit
) {
    var cantidadText by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val categorias = CATEGORIAS_GASTO

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Nuevo gasto", style = MaterialTheme.typography.headlineSmall)

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        OutlinedTextField(
            value = cantidadText,
            onValueChange = { cantidadText = it },
            label = { Text("Cantidad") },
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = categoria ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Categoría") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categorias.forEach { c ->
                    DropdownMenuItem(
                        text = { Text(c) },
                        onClick = { categoria = c; expanded = false }
                    )
                }
            }
        }

        Button(
            onClick = {
                val cantidad = cantidadText.replace(',', '.').toDoubleOrNull()
                when {
                    cantidad == null || cantidad <= 0.0 ->
                        error = "Introduce una cantidad válida"
                    categoria.isNullOrBlank() ->
                        error = "Elige una categoría"
                    else -> {
                        error = null
                        val fecha = System.currentTimeMillis()
                        scope.launch {
                            // 👇 ahora espera a que termine de guardar y recargar
                            vm.agregarGasto(
                                familiaId = familiaId,
                                cantidad = cantidad,
                                categoria = categoria!!,
                                fechaMillis = fecha
                            )
                            onGuardado() // aquí ya se habrá recargado el periodo
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Guardar gasto") }

    }
}






