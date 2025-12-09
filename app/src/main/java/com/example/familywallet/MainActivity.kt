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
// Rutas de navegación de la app
// ----------------------------
sealed class Ruta(val route: String) {

    // Rutas de autenticación
    data object Login : Ruta("login")
    data object Registro : Ruta("registro")
    data object Recuperar : Ruta("recuperar")

    // Rutas de configuración de familia
    data object ConfigFamilia : Ruta("config_familia")
    data object CrearFamilia  : Ruta("crear_familia")
    data object UnirseFamilia : Ruta("unirse_familia")

    // Rutas con argumento familiaId + helper para construir la ruta real
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

    // Ruta de detalle mensual con 3 argumentos
    data object HistorialMes : Ruta("historial_mes/{familiaId}/{anio}/{mes}") {
        const val ARG_FAMILIA = "familiaId"
        const val ARG_ANIO    = "anio"
        const val ARG_MES     = "mes"
        fun build(familiaId: String, anio: Int, mes: Int) =
            "historial_mes/$familiaId/$anio/$mes"
    }

    // Rutas simples sin argumentos
    data object Configuracion : Ruta("configuracion")
    data object Categorias    : Ruta("categorias")
    data object Moneda        : Ruta("moneda")

    // Solicitudes: ruta base + variante con arg y helper build
    data object Solicitudes : Ruta("solicitudes") {
        const val ARG = "familiaId"
        val routeWithArg = "$route/{$ARG}"
        fun build(familiaId: String) = "$route/$familiaId"
    }

    // Miembros con familiaId
    data object Miembros : Ruta("miembros/{familiaId}") {
        const val ARG = "familiaId"
        fun build(familiaId: String) = "miembros/$familiaId"
    }
}

// ----------------------------
// Tema claro personalizado (paleta verde)
// ----------------------------
private val LightGreenBackground = Color(0xFFE8F5E9)
private val DarkGreenPrimary     = Color(0xFF2E7D32)

// Esquema de colores claro usado por defecto en la app
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

// ----------------------------
// Tipografías personalizadas
// ----------------------------
private val TitleFontFamily  = FontFamily(Font(R.font.telma_variable,  weight = FontWeight.Normal))
private val ButtonFontFamily = FontFamily(Font(R.font.ranade_variable, weight = FontWeight.Normal))
private val BodyFontFamily   = FontFamily(Font(R.font.ranade_variable, weight = FontWeight.Normal))

// Mapea familias de fuente a estilos Material3
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

// ----------------------------
// Activity principal: aplica tema y lanza navegación
// ----------------------------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            // VM del tema que lee DataStore
            val themeVM: ThemeViewModel = viewModel(factory = ThemeVMFactory(application))

            // Estado reactivo de modo oscuro
            val isDark by themeVM.isDark.collectAsState()

            // Selección dinámica de esquema de colores
            val colors = if (isDark) darkColorScheme() else CustomLightColorScheme

            // MaterialTheme global de la app
            MaterialTheme(colorScheme = colors, typography = AppTypography) {

                // Navegación principal + callback para alternar theme
                AppNav(isDark = isDark, onToggleDark = { themeVM.toggle() })
            }
        }
    }
}

// ----------------------------
// Grafo de navegación de Compose
// ----------------------------
@Composable
fun AppNav(
    isDark: Boolean,
    onToggleDark: () -> Unit
) {
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()

    // ViewModels compartidos a nivel de grafo
    // Movimientos: lógica de ingresos/gastos e historial
    val movimientosVM: MovimientosViewModel = viewModel(
        factory = MovimientosVMFactory(ServiceLocator.movimientosRepo)
    )

    // Familia: estado de pertenencia y operaciones de familia
    val familiaVM: FamiliaViewModel = viewModel(
        factory = FamiliaVMFactory(
            familiaRepo = ServiceLocator.familiaRepo,
            authRepo    = ServiceLocator.authRepo
        )
    )

    // Auth: login/registro/logout
    val authVM: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(ServiceLocator.authRepo)
    )

    // Navegación de seguridad cuando el usuario deja de pertenecer a la familia
    val goConfigOnKick: () -> Unit = {
        nav.navigate(Ruta.ConfigFamilia.route) {
            popUpTo(nav.graph.startDestinationId) { inclusive = false }
            launchSingleTop = true
        }
    }

    // Host principal de rutas
    NavHost(
        navController = nav,
        startDestination = Ruta.Login.route
    ) {

        // ----------------------------
        // Pantalla de Movimientos (no usada actualmente en tu flujo principal)
        // ----------------------------
        composable(
            route = Ruta.Movimientos.route,
            arguments = listOf(navArgument(Ruta.Movimientos.ARG) { type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString(Ruta.Movimientos.ARG) ?: return@composable
            PantallaMovimientos(
                familiaId = familiaId,
                onNuevo   = {
                    // Mantiene la navegación de ejemplo hacia sí misma
                    nav.navigate(Ruta.Movimientos.build(familiaId))
                }
            )
        }

        // ----------------------------
        // Login
        // ----------------------------
        composable(Ruta.Login.route) {

            // Auto-salto si ya hay sesión de Firebase
            LaunchedEffect(Unit) {
                Firebase.auth.currentUser?.let {
                    nav.navigate(Ruta.ConfigFamilia.route) {
                        popUpTo(Ruta.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            // UI de login y callbacks de navegación
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

        // ----------------------------
        // Registro
        // ----------------------------
        composable(Ruta.Registro.route) {
            PantallaRegistro(
                authVM = authVM,
                onRegistroOk   = { _, _ ->
                    // Tras registrar, vuelve al login
                    nav.navigate(Ruta.Login.route) {
                        popUpTo(Ruta.Registro.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onVolverLogin  = { nav.popBackStack() }
            )
        }

        // ----------------------------
        // Recuperar contraseña
        // ----------------------------
        composable(Ruta.Recuperar.route) {
            PantallaOlvidoPassword(
                authVM = authVM,
                onEnviado     = { nav.popBackStack() },
                onVolverLogin = { nav.popBackStack() }
            )
        }

        // ----------------------------
        // Configuración de familia (puerta de entrada tras login)
        // ----------------------------
        composable(Ruta.ConfigFamilia.route) {
            PantallaConfigFamilia(
                vm = familiaVM,
                onIrALaFamilia = { familiaId ->
                    // Si ya tengo familia, entro al inicio
                    nav.navigate(Ruta.Inicio.build(familiaId)) {
                        popUpTo(Ruta.ConfigFamilia.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onCrear  = { nav.navigate(Ruta.CrearFamilia.route) },
                onUnirse = { nav.navigate(Ruta.UnirseFamilia.route) },
                onLogout = {
                    // Cierra sesión y limpia backstack
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

        // ----------------------------
        // Crear familia
        // ----------------------------
        composable(Ruta.CrearFamilia.route) {
            PantallaCrearFamilia(
                vm = familiaVM,
                onHecho = { familiaId ->
                    // Tras creación, entra al inicio
                    nav.navigate(Ruta.Inicio.build(familiaId)) {
                        popUpTo(Ruta.ConfigFamilia.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onAtras = { nav.popBackStack() }
            )
        }

        // ----------------------------
        // Unirse a familia (envía solicitud)
        // ----------------------------
        composable(Ruta.UnirseFamilia.route) {

            // VM local de solicitudes para esta pantalla
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

        // ----------------------------
        // Configuración general (tema + logout)
        // ----------------------------
        composable(Ruta.Configuracion.route) {
            PantallaConfiguracion(
                isDark       = isDark,
                onToggleDark = onToggleDark,
                onBack       = { nav.popBackStack() },
                onLogout     = {
                    // Logout desde ajustes
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

        // ----------------------------
        // Moneda
        // ----------------------------
        composable(Ruta.Moneda.route) {
            PantallaMoneda(
                vm = movimientosVM,
                onGuardar = { nueva ->
                    // Cambia moneda en VM y vuelve atrás
                    movimientosVM.cambiarMoneda(nueva)
                    nav.popBackStack()
                },
                onBack = { nav.popBackStack() }
            )
        }

        // ----------------------------
        // Categorías (resumen del mes por categoría)
        // ----------------------------
        composable(Ruta.Categorias.route) {
            PantallaCategorias(
                vm = movimientosVM,
                onBack = { nav.popBackStack() }
            )
        }

        // ----------------------------
        // Miembros (lista + expulsión si admin)
        // ----------------------------
        composable(
            route = Ruta.Miembros.route,
            arguments = listOf(navArgument(Ruta.Miembros.ARG) { type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString(Ruta.Miembros.ARG) ?: return@composable

            // VM específico de miembros
            val vmMiembros: MiembrosViewModel = viewModel(
                factory = MiembrosVMFactory(familiaRepo = ServiceLocator.familiaRepo)
            )

            // Calcula si el usuario actual es admin de esta familia
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

        // ----------------------------
        // Inicio de familia (resumen principal)
        // ----------------------------
        composable(
            route = Ruta.Inicio.route,
            arguments = listOf(navArgument(Ruta.Inicio.ARG) { type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString(Ruta.Inicio.ARG) ?: return@composable

            // Determina admin para mostrar opciones avanzadas
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

                // Vuelve a la puerta de familia
                onBackToConfig = {
                    nav.navigate(Ruta.ConfigFamilia.route) {
                        popUpTo(Ruta.Inicio.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },

                // Accesos del menú superior
                onAbrirConfiguracion = { nav.navigate(Ruta.Configuracion.route) },
                onVerCategorias      = { nav.navigate(Ruta.Categorias.route) },
                onCambiarMoneda      = { nav.navigate(Ruta.Moneda.route) },
                onIrSolicitudes      = { nav.navigate(Ruta.Solicitudes.build(familiaId)) },
                onVerMiembros        = { nav.navigate(Ruta.Miembros.build(familiaId)) },

                // Estado de permisos
                esAdmin = esAdmin,

                // Redirección si te expulsan o cambias de familia
                onExpulsado = goConfigOnKick
            )
        }

        // ----------------------------
        // Añadir gasto
        // ----------------------------
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

        // ----------------------------
        // Añadir ingreso
        // ----------------------------
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

        // ----------------------------
        // Historial anual (lista de meses)
        // ----------------------------
        composable(
            route = Ruta.Historial.route,
            arguments = listOf(navArgument(Ruta.Historial.ARG) { type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString(Ruta.Historial.ARG) ?: return@composable
            PantallaHistorial(
                familiaId = familiaId,
                familiaVM = familiaVM,
                onAbrirMes = { year, month ->
                    // Navega al detalle del mes
                    nav.navigate(Ruta.HistorialMes.build(familiaId, year, month))
                },
                onBack = { nav.popBackStack() },
                onExpulsado = goConfigOnKick
            )
        }

        // ----------------------------
        // Historial mensual (detalle)
        // ----------------------------
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

        // ----------------------------
        // Solicitudes (solo accesible desde inicio si eres admin)
        // ----------------------------
        composable(
            route = Ruta.Solicitudes.routeWithArg,
            arguments = listOf(navArgument(Ruta.Solicitudes.ARG) { type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString(Ruta.Solicitudes.ARG) ?: return@composable

            // VM local de solicitudes para gestionar pendientes
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














