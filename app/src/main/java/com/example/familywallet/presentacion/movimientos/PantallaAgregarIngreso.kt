package com.example.familywallet.presentacion.movimientos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.familywallet.presentacion.familia.FamiliaViewModel
import com.example.familywallet.presentacion.ui.MembershipGuard
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun PantallaAgregarIngreso(
    familiaId: String,
    vm: MovimientosViewModel,
    familiaVM: FamiliaViewModel,
    onGuardado: () -> Unit,
    onBack: () -> Unit,
    onExpulsado: () -> Unit
) {
    // Comprueba en tiempo real si el usuario sigue perteneciendo a la familia.
    // Si no pertenece, ejecuta onExpulsado para redirigir fuera.
    MembershipGuard(
        familiaIdActual = familiaId,
        familiaVM = familiaVM,
        onExpulsado = onExpulsado
    )

    // Scope para lanzar corrutinas desde la UI.
    val scope = rememberCoroutineScope()

    // Estados del formulario.
    var cantidadTxt by remember { mutableStateOf("") }
    var notaTxt by remember { mutableStateOf("") }

    // Estados de error y carga.
    var cantidadError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    // Valida que la cantidad sea numérica y mayor que 0.
    fun validar(): Boolean {
        cantidadError = null
        generalError = null

        val valor = cantidadTxt.replace(",", ".").toDoubleOrNull()
        if (valor == null || valor <= 0.0) {
            cantidadError = "Introduce una cantidad válida"
            return false
        }
        return true
    }

    // Habilita guardar solo si hay cantidad y no está cargando.
    val puedeGuardar = !cargando && cantidadTxt.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Botón de volver.
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Título de la pantalla.
            Text(
                text = "Añadir ingreso",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            // Campo de cantidad.
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
                supportingText = {
                    cantidadError?.let { msg ->
                        Text(msg, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            // Campo opcional de nota.
            OutlinedTextField(
                value = notaTxt,
                onValueChange = { notaTxt = it },
                label = { Text("Nota (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3
            )

            // Muestra un error general si falla el guardado.
            generalError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(8.dp))

            // Guarda el ingreso usando el ViewModel.
            // Usa categoria = null porque los ingresos no requieren categoría.
            Button(
                onClick = {
                    if (!validar()) return@Button

                    val cantidad = cantidadTxt.replace(",", ".").toDouble()
                    val fecha = Calendar.getInstance().timeInMillis
                    val nota = notaTxt.trim().ifBlank { null }

                    cargando = true
                    scope.launch {
                        try {
                            vm.agregarIngreso(
                                familiaId = familiaId,
                                cantidad = cantidad,
                                categoria = null,
                                nota = nota,
                                fechaMillis = fecha
                            )
                            cargando = false
                            onGuardado()
                        } catch (e: Exception) {
                            cargando = false
                            generalError = e.message ?: "No se pudo guardar el ingreso."
                        }
                    }
                },
                enabled = puedeGuardar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                // Texto cambia según estado de carga.
                Text(if (cargando) "Guardando..." else "Guardar ingreso")
            }
        }
    }
}










