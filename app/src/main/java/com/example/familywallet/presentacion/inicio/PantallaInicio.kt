package com.example.familywallet.presentacion.inicio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.familywallet.datos.repositorios.ServiceLocator
import com.example.familywallet.presentacion.movimientos.MovimientosViewModel
import com.example.familywallet.presentacion.ui.rangoAnioActual
import com.example.familywallet.presentacion.ui.rangoDiaActual
import com.example.familywallet.presentacion.ui.rangoMesActual
import com.example.familywallet.presentacion.ui.rangoSemanaActual
import com.example.familywallet.presentacion.ui.rememberCurrencyFormatter
import java.util.Locale

@Composable
private fun TriLinesIcon(modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.onSurface
    Canvas(modifier = modifier.size(24.dp)) {
        val h = size.height
        val w = size.width
        val stroke = 3f
        // 1ª línea
        drawLine(
            color = lineColor,
            start = Offset(w * 0.25f, h * 0.30f),
            end   = Offset(w * 0.95f, h * 0.30f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // 2ª línea
        drawLine(
            color = lineColor,
            start = Offset(w * 0.40f, h * 0.50f),
            end   = Offset(w * 0.95f, h * 0.50f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // 3ª línea
        drawLine(
            color = lineColor,
            start = Offset(w * 0.55f, h * 0.70f),
            end   = Offset(w * 0.95f, h * 0.70f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(
    familiaId: String,
    vm: MovimientosViewModel,
    onIrAddGasto: () -> Unit,
    onIrAddIngreso: () -> Unit,
    onIrHistorial: () -> Unit,
    onBackToConfig: () -> Unit,
    onAbrirConfiguracion: () -> Unit,
    onCambiarMoneda: () -> Unit = {},
    onVerCategorias: () -> Unit,
    onIrSolicitudes: () -> Unit,
    esAdmin: Boolean = false,
    appName: String = "FamilyWallet"
) {
    // Cargar mes actual al entrar
    LaunchedEffect(familiaId) { vm.cargarMesActual(familiaId) }

    // Nombre de la familia
    var nombreFamilia by remember(familiaId) { mutableStateOf<String?>(null) }
    LaunchedEffect(familiaId) {
        nombreFamilia = try {
            ServiceLocator.familiaRepo.nombreDe(familiaId)
        } catch (_: Exception) {
            null
        }
    }

    val ingresos  = vm.totalIngresos
    val gastos    = vm.totalGastos
    val formatter = rememberCurrencyFormatter(vm.monedaActual)
    val locale    = Locale("es", "ES")

    var menuPeriodoAbierto  by remember { mutableStateOf(false) }
    var menuOpcionesAbierto by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    Text(
                        text  = appName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                },
                title = {},
                actions = {
                    // Menú de periodo (icono 3 líneas)
                    Box {
                        IconButton(onClick = { menuPeriodoAbierto = true }) { TriLinesIcon() }
                        DropdownMenu(
                            expanded = menuPeriodoAbierto,
                            onDismissRequest = { menuPeriodoAbierto = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Día") },
                                onClick = {
                                    vm.aplicarRango(familiaId, rangoDiaActual(locale))
                                    menuPeriodoAbierto = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Semana") },
                                onClick = {
                                    vm.aplicarRango(familiaId, rangoSemanaActual(locale))
                                    menuPeriodoAbierto = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Mes") },
                                onClick = {
                                    vm.aplicarRango(familiaId, rangoMesActual(locale))
                                    menuPeriodoAbierto = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Año") },
                                onClick = {
                                    vm.aplicarRango(familiaId, rangoAnioActual(locale))
                                    menuPeriodoAbierto = false
                                }
                            )
                        }
                    }

                    Spacer(Modifier.width(4.dp))

                    // 3 puntos (opciones)
                    Box {
                        IconButton(onClick = { menuOpcionesAbierto = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                        }
                        DropdownMenu(
                            expanded = menuOpcionesAbierto,
                            onDismissRequest = { menuOpcionesAbierto = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Categorías de gastos") },
                                onClick = { menuOpcionesAbierto = false; onVerCategorias() }
                            )
                            DropdownMenuItem(
                                text = { Text("Configuración") },
                                onClick = { menuOpcionesAbierto = false; onAbrirConfiguracion() }
                            )
                            DropdownMenuItem(
                                text = { Text("Moneda") },
                                onClick = { menuOpcionesAbierto = false; onCambiarMoneda() }
                            )
                            if (esAdmin) {
                                Divider()
                                DropdownMenuItem(
                                    text = { Text("Solicitudes") },
                                    onClick = { menuOpcionesAbierto = false; onIrSolicitudes() }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { inner ->
        // Layout central + botón "Atrás"
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 24.dp)
        ) {
            // Contenido CENTRADO
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Nombre de la familia centrado
                nombreFamilia?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp)) // separación con la fecha
                }

                // Fecha / etiqueta del periodo
                Text(
                    text = vm.etiquetaPeriodo.ifBlank { vm.nombreMesActual() },
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                Text(text = "Ingresos: ${formatter.format(ingresos)}")
                Spacer(Modifier.height(8.dp))
                Text(text = "Gastos: ${formatter.format(gastos)}")
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = "Resumen: ${formatter.format(ingresos - gastos)}",
                    color = if ((ingresos - gastos) >= 0)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )

                Spacer(Modifier.height(24.dp))

                Button(onClick = onIrHistorial) { Text("Ver historial") }

                Spacer(Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onIrAddGasto)   { Text("Añadir gasto") }
                    OutlinedButton(onClick = onIrAddIngreso) { Text("Añadir ingreso") }
                }
            }

            // Botón "Atrás" abajo-izquierda
            OutlinedButton(
                onClick = onBackToConfig,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 16.dp)
            ) {
                Text("Atrás")
            }
        }
    }
}



















