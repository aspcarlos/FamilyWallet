package com.example.familywallet.presentacion.movimientos

import androidx.benchmark.traceprocessor.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.familywallet.datos.modelos.Movimiento
import com.example.familywallet.presentacion.familia.FamiliaViewModel
import com.example.familywallet.presentacion.ui.MembershipGuard
import com.example.familywallet.presentacion.ui.ScreenScaffold
import com.example.familywallet.presentacion.ui.rememberCurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
private fun FechaCorta(millis: Long): String {
    val fmt = remember { SimpleDateFormat("dd MMM yyyy", Locale("es", "ES")) }
    return fmt.format(Date(millis))
}

// Divide "Categoría · Nota" → (categoría, nota)
private fun parseCategoriaNota(cat: String?): Pair<String, String?> {
    if (cat.isNullOrBlank()) return "Movimiento" to null
    val sep = " · "
    return if (cat.contains(sep)) {
        val idx = cat.indexOf(sep)
        val titulo = cat.substring(0, idx).ifBlank { "Movimiento" }
        val nota = cat.substring(idx + sep.length).ifBlank { null }
        titulo to nota
    } else cat to null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHistorialMes(
    familiaId: String,
    year: Int,
    month: Int,
    vm: MovimientosViewModel,
    familiaVM: FamiliaViewModel,
    onBack: () -> Unit,
    onExpulsado: () -> Unit
) {
    MembershipGuard(
        familiaIdActual = familiaId,
        familiaVM = familiaVM,
        onExpulsado = onExpulsado
    )

    val items = vm.itemsDelMesState.value
    val formatter = rememberCurrencyFormatter(vm.monedaActual)

    LaunchedEffect(familiaId, year, month) {
        vm.cargarMes(familiaId, year, month)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle • $year/$month") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            items(items) { mov ->
                val (titulo, nota) =
                    if (mov.tipo == Movimiento.Tipo.GASTO) {
                        parseCategoriaNota(mov.categoria)
                    } else {
                        "Ingreso" to mov.categoria?.takeIf { it.isNotBlank() }
                    }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Izquierda: fecha + título
                    Column(Modifier.weight(1f)) {
                        Text(FechaCorta(mov.fechaMillis), style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(4.dp))
                        Text(titulo, style = MaterialTheme.typography.titleMedium)
                    }

                    // Centro: nota centrada
                    if (!nota.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.medium,
                                tonalElevation = 1.dp
                            ) {
                                Text(
                                    nota,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    } else {
                        Spacer(Modifier.weight(1f)) // mantiene el importe alineado a la derecha
                    }

                    // Derecha: importe
                    val sign = if (mov.tipo == Movimiento.Tipo.GASTO) -1 else 1
                    Text(
                        rememberCurrencyFormatter(vm.monedaActual).format(sign * abs(mov.cantidad)),
                        modifier = Modifier.weight(0.6f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Divider()
            }

        }
    }
}












