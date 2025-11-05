package com.example.familywallet.presentacion.movimientos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.familywallet.datos.modelos.CATEGORIAS_GASTO
import com.example.familywallet.presentacion.familia.FamiliaViewModel
import com.example.familywallet.presentacion.ui.MembershipGuard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAgregarGasto(
    familiaId: String,
    vm: MovimientosViewModel,
    familiaVM: FamiliaViewModel,
    onGuardado: () -> Unit,
    onExpulsado: () -> Unit,
    onBack: () -> Unit = {}
) {
    MembershipGuard(
        familiaIdActual = familiaId,
        familiaVM = familiaVM,
        onExpulsado = onExpulsado
    )

    var cantidadText by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var nota by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun limitTo25Words(input: String): String {
        val words = input.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return if (words.size <= 25) input else words.take(25).joinToString(" ")
    }
    val remaining = (25 - nota.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size)
        .coerceAtLeast(0)

    Scaffold(
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Button(onClick = onBack) { Text("Atrás") }
            }
        }
    ) { inner ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.9f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Nuevo gasto",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

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
                        onDismissRequest = { expanded = false },
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        CATEGORIAS_GASTO.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = { categoria = c; expanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = nota,
                    onValueChange = { nota = limitTo25Words(it) },
                    label = { Text("Nota (opcional)") },
                    supportingText = { Text("Máx. 25 palabras • Restantes: $remaining") },
                    modifier = Modifier.fillMaxWidth()
                )

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
                                val catFinal =
                                    if (nota.isBlank()) categoria!! else "${categoria!!} · ${nota.trim()}"
                                scope.launch {
                                    vm.agregarGasto(
                                        familiaId = familiaId,
                                        cantidad = cantidad,
                                        categoria = catFinal,
                                        fechaMillis = fecha
                                    )
                                    onGuardado()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) { Text("Guardar gasto") }
            }
        }
    }
}











