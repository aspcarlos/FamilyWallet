package com.example.familywallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.familywallet.datos.repositorios.ServiceLocator
import com.example.familywallet.presentacion.autenticacion.AuthViewModel
import com.example.familywallet.presentacion.autenticacion.AuthViewModelFactory
import com.example.familywallet.presentacion.autenticacion.PantallaLogin
import com.example.familywallet.presentacion.autenticacion.PantallaOlvidoPassword
import com.example.familywallet.presentacion.autenticacion.PantallaRegistro
import com.example.familywallet.presentacion.familia.*
import com.example.familywallet.presentacion.inicio.PantallaCategorias
import com.example.familywallet.presentacion.inicio.PantallaConfiguracion
import com.example.familywallet.presentacion.inicio.PantallaInicio
import com.example.familywallet.presentacion.inicio.PantallaMoneda
import com.example.familywallet.presentacion.miembros.MiembrosVMFactory
import com.example.familywallet.presentacion.miembros.MiembrosViewModel
import com.example.familywallet.presentacion.miembros.PantallaMiembros
import com.example.familywallet.presentacion.movimientos.*
import com.example.familywallet.presentacion.solicitudes.PantallaSolicitudes
import com.example.familywallet.presentacion.solicitudes.SolicitudesVMFactory
import com.example.familywallet.presentacion.solicitudes.SolicitudesViewModel
import com.example.familywallet.theme.ThemeVMFactory
import com.example.familywallet.theme.ThemeViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

// ----------------------------
// Rutas
// ----------------------------
sealed class Ruta(val route: String) {

    data object Login : Ruta("login")
    data object Registro : Ruta("registro")
    data object Recuperar : Ruta("recuperar")

    data object ConfigFamilia : Ruta("config_familia")
    data object CrearFamilia  : Ruta("crear_familia")
    data object UnirseFamilia : Ruta("unirse_familia")

    // con argumentos + helper build()
    data object Inicio : Ruta("inicio/{familiaId}") {
        const val ARG = "familiaId"
        fun build(familiaId: String) = "inicio/$familiaId"
    }

    data object AddGasto : Ruta("add_gasto/{familiaId}") {
        const val ARG = "familiaId"
        fun build(familiaId: String) = "add_gasto/$familiaId"
    }

    data object AddIngreso : Ruta("add_ingreso/{familiaId}") {
        const val ARG = "familiaId"
        fun build(familiaId: String) = "add_ingreso/$familiaId"
    }

    data object Movimientos : Ruta("movimientos/{familiaId}") {
        const val ARG = "familiaId"
        fun build(familiaId: String) = "movimientos/$familiaId"
    }

    data object Historial : Ruta("historial/{familiaId}") {
        const val ARG = "familiaId"
        fun build(familiaId: String) = "historial/$familiaId"
    }

    data object HistorialMes : Ruta("historial_mes/{familiaId}/{anio}/{mes}") {
        const val ARG_FAMILIA = "familiaId"
        const val ARG_ANIO    = "anio"
        const val ARG_MES     = "mes"
        fun build(familiaId: String, anio: Int, mes: Int) =
            "historial_mes/$familiaId/$anio/$mes"
    }

    // sin argumentos
    data object Configuracion : Ruta("configuracion")
    data object Categorias    : Ruta("categorias")
    data object Moneda        : Ruta("moneda")

    // Solicitudes (ruta con helper + variante con arg)
    data object Solicitudes : Ruta("solicitudes") {
        const val ARG = "familiaId"
        val routeWithArg = "$route/{$ARG}"
        fun build(familiaId: String) = "$route/$familiaId"
    }

    data object Miembros : Ruta("miembros/{familiaId}") {
        const val ARG = "familiaId"
        fun build(familiaId: String) = "miembros/$familiaId"
    }
}

// ==== Tema claro (verde)
private val LightGreenBackground = Color(0xFFE8F5E9)
private val DarkGreenPrimary     = Color(0xFF2E7D32)

private val CustomLightColorScheme = lightColorScheme(
    primary            = DarkGreenPrimary,
    onPrimary          = Color.White,
    primaryContainer   = DarkGreenPrimary,
    onPrimaryContainer = Color.White,

    secondary              = DarkGreenPrimary,
    onSecondary            = Color.White,
    secondaryContainer     = DarkGreenPrimary,
    onSecondaryContainer   = Color.White,

    tertiary            = DarkGreenPrimary,
    onTertiary          = Color.White,
    tertiaryContainer   = DarkGreenPrimary,
    onTertiaryContainer = Color.White,

    background       = LightGreenBackground,
    onBackground     = DarkGreenPrimary,
    surface          = LightGreenBackground,
    onSurface        = DarkGreenPrimary,
    surfaceVariant   = Color(0xFFD0E8D6),
    onSurfaceVariant = DarkGreenPrimary,
    outline          = DarkGreenPrimary
)

// ==== Tipografías
private val TitleFontFamily  = FontFamily(Font(R.font.telma_variable,  weight = FontWeight.Normal))
private val ButtonFontFamily = FontFamily(Font(R.font.ranade_variable, weight = FontWeight.Normal))
private val BodyFontFamily   = FontFamily(Font(R.font.ranade_variable, weight = FontWeight.Normal))

val AppTypography = Typography(
    headlineLarge  = Typography().headlineLarge.copy(fontFamily = TitleFontFamily),
    headlineMedium = Typography().headlineMedium.copy(fontFamily = TitleFontFamily),
    headlineSmall  = Typography().headlineSmall.copy(fontFamily = TitleFontFamily),

    titleLarge  = Typography().titleLarge.copy(fontFamily = TitleFontFamily),
    titleMedium = Typography().titleMedium.copy(fontFamily = TitleFontFamily),
    titleSmall  = Typography().titleSmall.copy(fontFamily = TitleFontFamily),

    labelLarge  = Typography().labelLarge.copy(fontFamily = ButtonFontFamily),
    labelMedium = Typography().labelMedium.copy(fontFamily = ButtonFontFamily),
    labelSmall  = Typography().labelSmall.copy(fontFamily = ButtonFontFamily),

    bodyLarge  = Typography().bodyLarge.copy(fontFamily = BodyFontFamily),
    bodyMedium = Typography().bodyMedium.copy(fontFamily = BodyFontFamily),
    bodySmall  = Typography().bodySmall.copy(fontFamily = BodyFontFamily)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeVM: ThemeViewModel = viewModel(factory = ThemeVMFactory(application))
            val isDark by themeVM.isDark.collectAsState()
            val colors = if (isDark) darkColorScheme() else CustomLightColorScheme

            MaterialTheme(colorScheme = colors, typography = AppTypography) {
                AppNav(isDark = isDark, onToggleDark = { themeVM.toggle() })
            }
        }
    }
}

@Composable
fun AppNav(
    isDark: Boolean,
    onToggleDark: () -> Unit
) {
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()

    // VMs
    val movimientosVM: MovimientosViewModel = viewModel(
        factory = MovimientosVMFactory(ServiceLocator.movimientosRepo)
    )
    val familiaVM: FamiliaViewModel = viewModel(
        factory = FamiliaVMFactory(
            familiaRepo = ServiceLocator.familiaRepo,
            authRepo    = ServiceLocator.authRepo
        )
    )
    val authVM: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(ServiceLocator.authRepo)
    )

    val goConfigOnKick: () -> Unit = {
        nav.navigate(Ruta.ConfigFamilia.route) {
            popUpTo(nav.graph.startDestinationId) { inclusive = false }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = nav,
        startDestination = Ruta.Login.route
    ) {
        // Movimientos (opcional)
        composable(
            route = Ruta.Movimientos.route,
            arguments = listOf(navArgument(Ruta.Movimientos.ARG) { type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString(Ruta.Movimientos.ARG) ?: return@composable
            PantallaMovimientos(
                familiaId = familiaId,
                onNuevo   = { nav.navigate(Ruta.Movimientos.build(familiaId))
                }
            )
        }

        // Login
        composable(Ruta.Login.route) {
            LaunchedEffect(Unit) {
                Firebase.auth.currentUser?.let {
                    nav.navigate(Ruta.ConfigFamilia.route) {
                        popUpTo(Ruta.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            PantallaLogin(
                authVM = authVM,
                onLoginOk   = {
                    nav.navigate(Ruta.ConfigFamilia.route) {
                        popUpTo(Ruta.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onRegistro  = { nav.navigate(Ruta.Registro.route) },
                onOlvido    = { nav.navigate(Ruta.Recuperar.route) }
            )
        }

        // Registro
        composable(Ruta.Registro.route) {
            PantallaRegistro(
                authVM = authVM,
                onRegistroOk   = { _, _ ->
                    nav.navigate(Ruta.Login.route) {
                        popUpTo(Ruta.Registro.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onVolverLogin  = { nav.popBackStack() }
            )
        }

        // Recuperar
        composable(Ruta.Recuperar.route) {
            PantallaOlvidoPassword(
                authVM = authVM,
                onEnviado     = { nav.popBackStack() },
                onVolverLogin = { nav.popBackStack() }
            )
        }

        // Config. Familia
        composable(Ruta.ConfigFamilia.route) {
            PantallaConfigFamilia(
                vm = familiaVM,
                onIrALaFamilia = { familiaId ->
                    nav.navigate(Ruta.Inicio.build(familiaId)) {
                        popUpTo(Ruta.ConfigFamilia.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onCrear  = { nav.navigate(Ruta.CrearFamilia.route) },
                onUnirse = { nav.navigate(Ruta.UnirseFamilia.route) },
                onLogout = {
                    scope.launch {
                        try { authVM.logout() } finally {
                            nav.navigate(Ruta.Login.route) {
                                popUpTo(nav.graph.id) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                }
            )
        }

        // Crear familia
        composable(Ruta.CrearFamilia.route) {
            PantallaCrearFamilia(
                vm = familiaVM,
                onHecho = { familiaId ->
                    nav.navigate(Ruta.Inicio.build(familiaId)) {
                        popUpTo(Ruta.ConfigFamilia.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onAtras = { nav.popBackStack() }
            )
        }

        // Unirse familia
        composable(Ruta.UnirseFamilia.route) {
            val soliVM: SolicitudesViewModel = viewModel(
                factory = SolicitudesVMFactory(
                    solicitudesRepo = ServiceLocator.solicitudesRepo,
                    familiaRepo     = ServiceLocator.familiaRepo,
                    authRepo        = ServiceLocator.authRepo
                )
            )
            PantallaUnirseFamilia(
                vm = soliVM,
                onHecho = { nav.popBackStack() },
                onAtras = { nav.popBackStack() }
            )
        }

        // Configuración
        composable(Ruta.Configuracion.route) {
            PantallaConfiguracion(
                isDark       = isDark,
                onToggleDark = onToggleDark,
                onBack       = { nav.popBackStack() },
                onLogout     = {
                    scope.launch {
                        authVM.logout()
                        nav.navigate(Ruta.Login.route) {
                            popUpTo(nav.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        // Moneda / Categorías
        composable(Ruta.Moneda.route) {
            PantallaMoneda(
                vm = movimientosVM,
                onGuardar = { nueva ->
                    movimientosVM.cambiarMoneda(nueva)
                    nav.popBackStack()
                },
                onBack = { nav.popBackStack() }
            )
        }
        composable(Ruta.Categorias.route) {
            PantallaCategorias(vm = movimientosVM, onBack = { nav.popBackStack() })
        }

        // Miembros
        composable(
            route = Ruta.Miembros.route,
            arguments = listOf(navArgument(Ruta.Miembros.ARG) { type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString(Ruta.Miembros.ARG) ?: return@composable
            val vmMiembros: MiembrosViewModel = viewModel(
                factory = MiembrosVMFactory(familiaRepo = ServiceLocator.familiaRepo)
            )
            var esAdmin by remember { mutableStateOf(false) }
            LaunchedEffect(familiaId) {
                val uid = ServiceLocator.authRepo.usuarioActualUid
                esAdmin = uid != null && ServiceLocator.familiaRepo.esAdmin(familiaId, uid)
            }
            PantallaMiembros(
                familiaId = familiaId,
                vm = vmMiembros,
                esAdmin = esAdmin,
                onBack = { nav.popBackStack() }
            )
        }

        // Inicio
        composable(
            route = Ruta.Inicio.route,
            arguments = listOf(navArgument(Ruta.Inicio.ARG) { type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString(Ruta.Inicio.ARG) ?: return@composable

            val uidActual = ServiceLocator.authRepo.usuarioActualUid
            var esAdmin by remember(familiaId, uidActual) { mutableStateOf(false) }
            LaunchedEffect(familiaId, uidActual) {
                esAdmin = if (uidActual == null) false
                else runCatching { ServiceLocator.familiaRepo.esAdmin(familiaId, uidActual) }
                    .getOrElse { false }
            }

            PantallaInicio(
                familiaId = familiaId,
                vm = movimientosVM,
                familiaVM = familiaVM,
                onIrAddGasto   = { nav.navigate(Ruta.AddGasto.build(familiaId)) },
                onIrAddIngreso = { nav.navigate(Ruta.AddIngreso.build(familiaId)) },
                onIrHistorial  = { nav.navigate(Ruta.Historial.build(familiaId)) },
                onBackToConfig = {
                    nav.navigate(Ruta.ConfigFamilia.route) {
                        popUpTo(Ruta.Inicio.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onAbrirConfiguracion = { nav.navigate(Ruta.Configuracion.route) },
                onVerCategorias      = { nav.navigate(Ruta.Categorias.route) },
                onCambiarMoneda      = { nav.navigate(Ruta.Moneda.route) },
                onIrSolicitudes      = { nav.navigate(Ruta.Solicitudes.build(familiaId)) },
                onVerMiembros        = { nav.navigate(Ruta.Miembros.build(familiaId)) },
                esAdmin = esAdmin,
                onExpulsado = goConfigOnKick
            )
        }

        // Añadir gasto
        composable(
            route = Ruta.AddGasto.route,
            arguments = listOf(navArgument(Ruta.AddGasto.ARG) { type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString(Ruta.AddGasto.ARG) ?: return@composable
            PantallaAgregarGasto(
                familiaId = familiaId,
                vm = movimientosVM,
                familiaVM = familiaVM,
                onGuardado = { nav.popBackStack() },
                onBack     = { nav.popBackStack() },
                onExpulsado = goConfigOnKick
            )
        }

        // Añadir ingreso
        composable(
            route = Ruta.AddIngreso.route,
            arguments = listOf(navArgument(Ruta.AddIngreso.ARG) { type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString(Ruta.AddIngreso.ARG) ?: return@composable
            PantallaAgregarIngreso(
                familiaId = familiaId,
                vm = movimientosVM,
                familiaVM = familiaVM,
                onGuardado = { nav.popBackStack() },
                onBack     = { nav.popBackStack() },
                onExpulsado = goConfigOnKick
            )
        }

        // Historial (lista meses)
        composable(
            route = Ruta.Historial.route,
            arguments = listOf(navArgument(Ruta.Historial.ARG) { type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString(Ruta.Historial.ARG) ?: return@composable
            PantallaHistorial(
                familiaId = familiaId,
                familiaVM = familiaVM,
                onAbrirMes = { year, month ->
                    nav.navigate(Ruta.HistorialMes.build(familiaId, year, month))
                },
                onBack = { nav.popBackStack() },
                onExpulsado = goConfigOnKick
            )
        }

        // Historial Mes (detalle)
        composable(
            route = Ruta.HistorialMes.route,
            arguments = listOf(
                navArgument(Ruta.HistorialMes.ARG_FAMILIA) { type = NavType.StringType },
                navArgument(Ruta.HistorialMes.ARG_ANIO)    { type = NavType.IntType },
                navArgument(Ruta.HistorialMes.ARG_MES)     { type = NavType.IntType },
            )
        ) { backStack ->
            val familiaId = backStack.arguments?.getString(Ruta.HistorialMes.ARG_FAMILIA)
            val year      = backStack.arguments?.getInt(Ruta.HistorialMes.ARG_ANIO)
            val month     = backStack.arguments?.getInt(Ruta.HistorialMes.ARG_MES)
            if (familiaId == null || year == null || month == null) return@composable

            PantallaHistorialMes(
                familiaId = familiaId,
                year = year,
                month = month,
                vm = movimientosVM,
                familiaVM = familiaVM,
                onBack = { nav.popBackStack() },
                onExpulsado = goConfigOnKick
            )
        }

        // Solicitudes
        composable(
            route = Ruta.Solicitudes.routeWithArg,
            arguments = listOf(navArgument(Ruta.Solicitudes.ARG) { type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString(Ruta.Solicitudes.ARG) ?: return@composable
            val soliVM: SolicitudesViewModel = viewModel(
                factory = SolicitudesVMFactory(
                    solicitudesRepo = ServiceLocator.solicitudesRepo,
                    familiaRepo     = ServiceLocator.familiaRepo,
                    authRepo        = ServiceLocator.authRepo
                )
            )
            PantallaSolicitudes(
                familiaId = familiaId,
                vm = soliVM,
                familiaVM = familiaVM,
                onBack = { nav.popBackStack() },
                onExpulsado = goConfigOnKick
            )
        }
    }
}













