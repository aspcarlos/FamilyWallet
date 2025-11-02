package com.example.familywallet.presentacion.movimientos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.familywallet.presentacion.familia.FamiliaViewModel
import com.example.familywallet.presentacion.ui.MembershipGuard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAgregarIngreso(
    familiaId: String,
    vm: MovimientosViewModel,
    familiaVM: FamiliaViewModel,
    onGuardado: () -> Unit,
    onExpulsado: () -> Unit,
    onBack: () -> Unit = {}
) {
    // Bloquea acceso si fue expulsado en caliente
    MembershipGuard(
        familiaIdActual = familiaId,
        familiaVM = familiaVM,
        onExpulsado = onExpulsado
    )

    var cantidadText by remember { mutableStateOf("") }
    var nota by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Límite 25 palabras
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
            ) { OutlinedButton(onClick = onBack) { Text("Atrás") } }
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
                    .align(Alignment.Center)   // centra TODO el bloque
                    .fillMaxWidth(0.9f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Nuevo ingreso",
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
                            else -> {
                                error = null
                                val fecha = System.currentTimeMillis()
                                scope.launch {
                                    vm.agregarIngreso(
                                        familiaId = familiaId,
                                        cantidad = cantidad,
                                        categoria = if (nota.isBlank()) null else nota.trim(),
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
                ) { Text("Guardar ingreso") }
            }
        }
    }
}







