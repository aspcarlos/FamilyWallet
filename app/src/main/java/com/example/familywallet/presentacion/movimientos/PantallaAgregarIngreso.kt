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
import com.example.familywallet.presentacion.ui.ScreenScaffold
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAgregarIngreso(
    familiaId: String,
    vm: MovimientosViewModel,
    onGuardado: () -> Unit
) {
    var cantidadText by remember { mutableStateOf("") }
    var nota by remember { mutableStateOf("") } // opcional
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    ScreenScaffold(
        topBar = {
            TopAppBar(title = { Text("Nuevo ingreso") })
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
                                vm.agregarIngreso(
                                    familiaId = familiaId,
                                    cantidad = cantidad,
                                    categoria = if (nota.isBlank()) null else nota,
                                    fechaMillis = fecha
                                )
                                onGuardado()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar ingreso")
            }
        }
    }
}






