package com.example.familywallet.presentacion.movimientos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    onBack: () -> Unit,
    onExpulsado: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // protección de expulsión
    MembershipGuard(
        familiaIdActual = familiaId,
        familiaVM = familiaVM,
        onExpulsado = onExpulsado
    )

    var cantidadTxt by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var fechaMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Añadir gasto") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<")
                    }
                }
            )
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                OutlinedTextField(
                    value = cantidadTxt,
                    onValueChange = { cantidadTxt = it },
                    label = { Text("Cantidad") },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text("Categoría (opcional)") },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth()
                )

                // Si ya tenías date picker, aquí iría

                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    enabled = !loading,
                    onClick = {
                        val num = cantidadTxt.replace(",", ".").toDoubleOrNull()
                        if (num == null) {
                            error = "Cantidad no válida"
                            return@Button
                        }

                        loading = true
                        error = null

                        scope.launch {
                            try {
                                vm.agregarGasto(
                                    familiaId = familiaId,
                                    cantidad = num,
                                    categoria = categoria.ifBlank { null },
                                    fechaMillis = fechaMillis
                                )
                                loading = false
                                onGuardado()
                            } catch (e: Exception) {
                                loading = false
                                error = e.localizedMessage ?: "No se pudo guardar el gasto"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (loading) "Guardando..." else "Guardar gasto")
                }
            }
        }
    }
}












