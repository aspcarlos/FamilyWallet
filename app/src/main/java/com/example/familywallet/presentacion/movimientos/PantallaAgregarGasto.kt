package com.example.familywallet.presentacion.movimientos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.familywallet.presentacion.familia.FamiliaViewModel
import com.example.familywallet.presentacion.ui.MembershipGuard
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAgregarGasto(
    familiaId: String,
    vm: MovimientosViewModel,
    familiaVM: FamiliaViewModel,
    onGuardado: () -> Unit,
    onBack: () -> Unit,
    onExpulsado: () -> Unit
) {
    // Control de expulsión
    MembershipGuard(
        familiaIdActual = familiaId,
        familiaVM = familiaVM,
        onExpulsado = onExpulsado
    )

    val scope = rememberCoroutineScope()

    var cantidadTxt by remember { mutableStateOf("") }
    var categoriaTxt by remember { mutableStateOf("") }
    var notaTxt      by remember { mutableStateOf("") }

    var cantidadError  by remember { mutableStateOf<String?>(null) }
    var categoriaError by remember { mutableStateOf<String?>(null) }
    var generalError   by remember { mutableStateOf<String?>(null) }
    var cargando       by remember { mutableStateOf(false) }

    val categoriasSugeridas = listOf("Comida","Transporte","Casa","Ocio","Salud","Educación","Otros")

    fun validar(): Boolean {
        var ok = true
        cantidadError = null
        categoriaError = null
        generalError = null

        val valor = cantidadTxt.replace(",", ".").toDoubleOrNull()
        if (valor == null || valor <= 0.0) {
            cantidadError = "Introduce una cantidad válida"
            ok = false
        }
        if (categoriaTxt.isBlank()) {
            categoriaError = "La categoría es obligatoria"
            ok = false
        }
        return ok
    }

    val puedeGuardar = !cargando && cantidadTxt.isNotBlank() && categoriaTxt.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Flecha atrás
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
        }

        // Contenido centrado
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text("Añadir gasto", style = MaterialTheme.typography.headlineSmall)

            // Cantidad
            OutlinedTextField(
                value = cantidadTxt,
                onValueChange = {
                    cantidadTxt = it
                    if (cantidadError != null) cantidadError = null
                },
                label = { Text("Cantidad") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = cantidadError != null,
                supportingText = { cantidadError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
            )

            // Categoría desplegable obligatoria
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = categoriaTxt,
                    onValueChange = {
                        categoriaTxt = it
                        if (categoriaError != null) categoriaError = null
                    },
                    label = { Text("Categoría") },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    isError = categoriaError != null,
                    singleLine = true,
                    supportingText = { categoriaError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = MaterialTheme.colorScheme.background // verde claro
                ) {
                    categoriasSugeridas.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                categoriaTxt = cat
                                categoriaError = null
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Nota (opcional)
            OutlinedTextField(
                value = notaTxt,
                onValueChange = { notaTxt = it },
                label = { Text("Nota (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3
            )

            generalError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (!validar()) return@Button
                    val cantidad = cantidadTxt.replace(",", ".").toDouble()
                    val categoria = categoriaTxt.trim()
                    val fecha = Calendar.getInstance().timeInMillis

                    cargando = true
                    scope.launch {
                        try {
                            vm.agregarGasto(
                                familiaId = familiaId,
                                cantidad = cantidad,
                                categoria = categoria,
                                nota = notaTxt.trim().ifBlank { null }, // ⬅️ ENVIAMOS LA NOTA
                                fechaMillis = fecha
                            )
                            cargando = false
                            onGuardado()
                        } catch (e: Exception) {
                            cargando = false
                            generalError = e.message ?: "No se pudo guardar el gasto."
                        }
                    }
                },
                enabled = puedeGuardar,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(if (cargando) "Guardando..." else "Guardar gasto")
            }
        }
    }
}
















