package com.example.familywallet.presentacion.movimientos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.familywallet.datos.modelos.CATEGORIAS_GASTO
import com.example.familywallet.presentacion.familia.FamiliaViewModel
import com.example.familywallet.presentacion.ui.MembershipGuard
import com.example.familywallet.presentacion.ui.ScreenScaffold
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAgregarGasto(
    familiaId: String,
    vm: MovimientosViewModel,
    familiaVM: FamiliaViewModel,
    onGuardado: () -> Unit,
    onExpulsado: () -> Unit
) {

    MembershipGuard(
        familiaIdActual = familiaId,
        familiaVM = familiaVM,
        onExpulsado = onExpulsado
    )

    var cantidadText by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val categorias = CATEGORIAS_GASTO

    ScreenScaffold(
        topBar = {
            TopAppBar(title = { Text("Nuevo gasto") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                                vm.agregarGasto(
                                    familiaId = familiaId,
                                    cantidad = cantidad,
                                    categoria = categoria!!,
                                    fechaMillis = fecha
                                )
                                onGuardado()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar gasto") }
        }
    }
}







