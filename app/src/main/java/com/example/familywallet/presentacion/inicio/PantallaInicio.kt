package com.example.familywallet.presentacion.inicio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.familywallet.datos.repositorios.ServiceLocator
import com.example.familywallet.presentacion.familia.FamiliaViewModel
import com.example.familywallet.presentacion.movimientos.MovimientosViewModel
import com.example.familywallet.presentacion.ui.*
import java.util.Locale

@Composable
private fun TriLinesIcon(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    // Icono personalizado de 3 líneas para el menú de periodo.
    Canvas(modifier = modifier.size(24.dp)) {
        val h = size.height
        val w = size.width
        val stroke = 3f

        drawLine(
            color = color,
            start = Offset(w * 0.25f, h * 0.30f),
            end   = Offset(w * 0.95f, h * 0.30f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.40f, h * 0.50f),
            end   = Offset(w * 0.95f, h * 0.50f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
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
    familiaVM: FamiliaViewModel,
    onIrAddGasto: () -> Unit,
    onIrAddIngreso: () -> Unit,
    onIrHistorial: () -> Unit,
    onBackToConfig: () -> Unit,
    onAbrirConfiguracion: () -> Unit,
    onCambiarMoneda: () -> Unit = {},
    onVerCategorias: () -> Unit,
    onIrSolicitudes: () -> Unit,
    onVerMiembros: () -> Unit,
    onExpulsado: () -> Unit,
    esAdmin: Boolean = false,
    appName: String = "FamilyWallet"
) {
    // Guard de seguridad: si ya no pertenezco a la familia, me saca a ConfigFamilia.
    MembershipGuard(
        familiaIdActual = familiaId,
        familiaVM = familiaVM,
        onExpulsado = onExpulsado
    )

    // Al entrar o cambiar de familia:
    // 1) activo listener de movimientos en tiempo real
    // 2) aplico por defecto el rango del mes actual.
    val locale = Locale("es", "ES")
    LaunchedEffect(familiaId) {
        vm.iniciarEscuchaTiempoReal(familiaId)
        vm.aplicarRango(familiaId, rangoMesActual(locale))
    }

    // Cargo el nombre de la familia para mostrarlo en el título.
    var nombreFamilia by remember(familiaId) { mutableStateOf<String?>(null) }
    LaunchedEffect(familiaId) {
        nombreFamilia = runCatching { ServiceLocator.familiaRepo.nombreDe(familiaId) }.getOrNull()
    }

    // Totales calculados en el ViewModel según el rango seleccionado.
    val ingresos  = vm.totalIngresos
    val gastos    = vm.totalGastos
    val formatter = rememberCurrencyFormatter(vm.monedaActual)

    // Estados de apertura de menús.
    var menuPeriodoAbierto  by remember { mutableStateOf(false) }
    var menuOpcionesAbierto by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            // Barra superior principal con:
            // - nombre app a la izquierda
            // - menú de periodo
            // - menú de opciones (3 puntos).
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    Text(
                        text = appName,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                },
                title = {},
                actions = {
                    // Menú de periodo (día/semana/mes/año).
                    Box {
                        IconButton(onClick = { menuPeriodoAbierto = true }) {
                            TriLinesIcon(color = MaterialTheme.colorScheme.onPrimary)
                        }
                        DropdownMenu(
                            expanded = menuPeriodoAbierto,
                            onDismissRequest = { menuPeriodoAbierto = false },
                            containerColor = MaterialTheme.colorScheme.background
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

                    // Menú de opciones generales.
                    Box {
                        IconButton(onClick = { menuOpcionesAbierto = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                        }
                        DropdownMenu(
                            expanded = menuOpcionesAbierto,
                            onDismissRequest = { menuOpcionesAbierto = false },
                            containerColor = MaterialTheme.colorScheme.background
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

                            // Solo el admin ve el acceso a solicitudes.
                            if (esAdmin) {
                                Divider()
                                DropdownMenuItem(
                                    text = { Text("Solicitudes") },
                                    onClick = { menuOpcionesAbierto = false; onIrSolicitudes() }
                                )
                            }

                            Divider()
                            DropdownMenuItem(
                                text = { Text("Miembros") },
                                onClick = { menuOpcionesAbierto = false; onVerMiembros() }
                            )
                        }
                    }
                }
            )
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 24.dp)
        ) {
            // Botón de volver a la pantalla de configuración familiar.
            IconButton(
                onClick = onBackToConfig,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Atrás",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Contenido central con el resumen económico y accesos principales.
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Nombre de la familia.
                Text(
                    text = "Familia ${nombreFamilia.orEmpty()}".trim(),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(24.dp))

                // Etiqueta del periodo seleccionado (si no hay, muestra el mes actual).
                Text(
                    text = vm.etiquetaPeriodo.ifBlank { vm.nombreMesActual() },
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(24.dp))

                // Totales de ingresos y gastos calculados por el VM.
                Text(text = "Ingresos: ${formatter.format(ingresos)}")
                Spacer(Modifier.height(8.dp))
                Text(text = "Gastos: ${formatter.format(gastos)}")
                Spacer(Modifier.height(8.dp))

                // Resumen neto del periodo.
                Text(
                    text = "Resumen: ${formatter.format(ingresos - gastos)}",
                    color = if ((ingresos - gastos) >= 0)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )

                Spacer(Modifier.height(24.dp))

                // Acceso al historial anual/mensual.
                Button(onClick = onIrHistorial) { Text("Ver historial") }

                Spacer(Modifier.height(24.dp))

                // Accesos rápidos para añadir movimientos.
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = onIrAddGasto) { Text("Añadir gasto") }
                    Button(onClick = onIrAddIngreso) { Text("Añadir ingreso") }
                }
            }
        }
    }
}


















