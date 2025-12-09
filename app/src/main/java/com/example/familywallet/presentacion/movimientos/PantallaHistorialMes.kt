package com.example.familywallet.presentacion.movimientos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.familywallet.datos.modelos.Movimiento
import com.example.familywallet.presentacion.familia.FamiliaViewModel
import com.example.familywallet.presentacion.ui.MembershipGuard
import com.example.familywallet.presentacion.ui.rememberCurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

// Formatea la fecha del movimiento en un formato corto y legible
@Composable
private fun FechaCorta(millis: Long): String {
    // Se memoriza el formatter para no recrearlo en cada recomposición
    val fmt = remember { SimpleDateFormat("dd MMM yyyy", Locale("es", "ES")) }
    return fmt.format(Date(millis))
}

// Obtiene un título coherente y una nota opcional para mostrar en el listado
private fun tituloYNota(mov: Movimiento): Pair<String, String?> {
    // Título principal según el tipo y la categoría
    val titulo = when (mov.tipo) {
        Movimiento.Tipo.GASTO   -> mov.categoria ?: "Gasto"
        Movimiento.Tipo.INGRESO -> mov.categoria?.takeIf { it.isNotBlank() } ?: "Ingreso"
    }

    // Si existe nota en el campo nuevo, se usa directamente
    val notaCampo = mov.nota?.takeIf { it.isNotBlank() }
    if (notaCampo != null) return titulo to notaCampo

    // Compatibilidad con datos antiguos donde la nota iba dentro de la categoría
    val cat = mov.categoria
    val sep = " · "
    if (!cat.isNullOrBlank() && cat.contains(sep)) {
        val posibleNota = cat.substringAfter(sep).ifBlank { null }
        return titulo to posibleNota
    }

    // Si no hay nota, devolvemos null
    return titulo to null
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
    // Evita que un usuario expulsado siga viendo datos de la familia
    MembershipGuard(
        familiaIdActual = familiaId,
        familiaVM = familiaVM,
        onExpulsado = onExpulsado
    )

    // Lista de movimientos cargados para ese mes
    val items = vm.itemsDelMesState.value

    // Formateador de moneda según la moneda actual del usuario
    val formatter = rememberCurrencyFormatter(vm.monedaActual)

    // Carga los movimientos del mes cuando cambian familia/año/mes
    LaunchedEffect(familiaId, year, month) {
        vm.cargarMes(familiaId, year, month)
    }

    // Contenedor principal de la pantalla
    Scaffold { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            // Botón de volver en la esquina superior izquierda
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Atrás",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Contenido centrado con el título y la lista
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.9f)
            ) {

                // Título del detalle del mes seleccionado
                Text(
                    text = "Detalle • $year/$month",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                // Lista de movimientos del mes
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(items) { mov ->
                        // Calcula cómo mostrar título y nota de forma robusta
                        val (titulo, nota) = tituloYNota(mov)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Columna izquierda: fecha y categoría/título
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = FechaCorta(mov.fechaMillis),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = titulo,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Zona central: nota si existe
                            if (!nota.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = MaterialTheme.shapes.medium
                                    ) {
                                        Text(
                                            text = nota,
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 8.dp
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            } else {
                                // Mantiene alineación visual cuando no hay nota
                                Spacer(Modifier.weight(1f))
                            }

                            // Columna derecha: importe con signo según tipo
                            val sign = if (mov.tipo == Movimiento.Tipo.GASTO) -1 else 1
                            Text(
                                text = formatter.format(sign * abs(mov.cantidad)),
                                modifier = Modifier.weight(0.6f),
                                textAlign = TextAlign.End,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Separador entre movimientos
                        Divider()
                    }
                }
            }
        }
    }
}


















