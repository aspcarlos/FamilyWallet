package com.example.familywallet.presentacion.movimientos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PantallaAgregarGasto(
    familiaId: String,
    vm: MovimientosViewModel,
    onGuardado: () -> Unit
) {
    var cantidadText by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Nuevo gasto", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = cantidadText,
            onValueChange = { cantidadText = it },
            label = { Text("Cantidad (€)") },
            singleLine = true
        )

        // ejemplo simple: selector mínimo
        val categorias = listOf(
            "automovil","casa","comida","comunicaciones","deportes","entretenimiento",
            "facturas","higiene","mascotas","regalos","restaurante","ropa","salud","taxi","transporte"
        )
        ExposedDropdownMenuBoxSample(categorias, categoria) { categoria = it }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(
            onClick = {
                val cantidad = cantidadText.replace(',', '.').toDoubleOrNull()
                when {
                    cantidad == null || cantidad <= 0.0 -> error = "Introduce una cantidad válida"
                    categoria.isNullOrBlank()          -> error = "Elige una categoría"
                    else -> {
                        vm.agregarGasto(familiaId, cantidad, categoria!!)
                        onGuardado()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Guardar gasto") }
    }
}

/** Dropdown mínimo sin dependencias extra (puedes sustituir por el tuyo) */
@Composable
private fun ExposedDropdownMenuBoxSample(
    opciones: List<String>,
    seleccionActual: String?,
    onSeleccion: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = seleccionActual ?: "",
        onValueChange = {},
        label = { Text("Categoría") },
        readOnly = true,
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = { IconButton(onClick = { expanded = !expanded }) { Text("▼") } }
    )
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        opciones.forEach { op ->
            DropdownMenuItem(
                text = { Text(op) },
                onClick = { onSeleccion(op); expanded = false }
            )
        }
    }
}




