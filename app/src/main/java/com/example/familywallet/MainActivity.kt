package com.example.familywallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.familywallet.datos.repositorios.ServiceLocator
import com.example.familywallet.presentacion.autenticacion.AuthViewModel
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

// ----------------------------
// Rutas
// ----------------------------
sealed class Ruta(val route: String) {
    data object Login : Ruta("login")
    data object Registro : Ruta("registro")
    data object Recuperar : Ruta("recuperar")

    data object ConfigFamilia : Ruta("config_familia")
    data object CrearFamilia : Ruta("crear_familia")
    data object UnirseFamilia : Ruta("unirse_familia")

    data object Inicio : Ruta("inicio/{familiaId}")

    data object AddGasto : Ruta("add_gasto/{familiaId}")
    data object AddIngreso : Ruta("add_ingreso/{familiaId}")

    data object Historial : Ruta("historial/{familiaId}")
    data object HistorialMes : Ruta("historial_mes/{familiaId}/{anio}/{mes}")

    data object Configuracion : Ruta("configuracion")
    data object Categorias : Ruta("categorias")
    data object Moneda : Ruta("moneda")

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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Tema global persistente (DataStore)
            val themeVM: ThemeViewModel = viewModel(factory = ThemeVMFactory(application))
            val isDark by themeVM.isDark.collectAsState()

            MaterialTheme(colorScheme = if (isDark) darkColorScheme() else lightColorScheme()) {
                AppNav(
                    isDark = isDark,
                    onToggleDark = { themeVM.toggle() }
                )
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

    // VMs
    val movimientosVM: MovimientosViewModel = viewModel(
        factory = MovimientosVMFactory(ServiceLocator.movimientosRepo)
    )
    val familiaVM: FamiliaViewModel = viewModel(
        factory = FamiliaVMFactory(
            familiaRepo = ServiceLocator.familiaRepo,
            authRepo = ServiceLocator.authRepo
        )
    )
    val authVM: AuthViewModel = viewModel()

    // Navegación a ConfigFamilia al ser expulsado
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
        // LOGIN (redirección si ya hay sesión)
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
                onLoginOk = {
                    nav.navigate(Ruta.ConfigFamilia.route) {
                        popUpTo(Ruta.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onRegistro = { nav.navigate(Ruta.Registro.route) },
                onOlvido = { nav.navigate(Ruta.Recuperar.route) }
            )
        }

        composable(Ruta.Registro.route) {
            PantallaRegistro(
                onRegistrar = { _, _ ->
                    nav.navigate(Ruta.Login.route) {
                        popUpTo(Ruta.Registro.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onVolverLogin = { nav.popBackStack() }
            )
        }

        composable(Ruta.Recuperar.route) {
            PantallaOlvidoPassword(
                onEnviado = { nav.popBackStack() },
                onVolverLogin = { nav.popBackStack() }
            )
        }

        // Config familia
        composable(Ruta.ConfigFamilia.route) {
            val authVMLocal: AuthViewModel = viewModel()
            PantallaConfigFamilia(
                vm = familiaVM,
                onIrALaFamilia = { familiaId ->
                    nav.navigate("inicio/$familiaId") {
                        popUpTo(Ruta.ConfigFamilia.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onCrear = { nav.navigate(Ruta.CrearFamilia.route) },
                onUnirse = { nav.navigate(Ruta.UnirseFamilia.route) },
                onLogout = {
                    authVMLocal.logout()
                    nav.navigate(Ruta.Login.route) {
                        popUpTo(nav.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Ruta.CrearFamilia.route) {
            PantallaCrearFamilia(
                vm = familiaVM,
                onHecho = { familiaId ->
                    nav.navigate("inicio/$familiaId") {
                        popUpTo(Ruta.ConfigFamilia.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onAtras = { nav.popBackStack() }
            )
        }

        composable(route = Ruta.UnirseFamilia.route) {
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
                isDark = isDark,
                onToggleDark = onToggleDark,
                onBack = { nav.popBackStack() },
                onLogout = {
                    authVM.logout()
                    nav.navigate(Ruta.Login.route) {
                        popUpTo(nav.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = Ruta.Moneda.route) {
            PantallaMoneda(
                vm = movimientosVM,
                onGuardar = { nueva ->
                    movimientosVM.cambiarMoneda(nueva)
                    nav.popBackStack()
                },
                onBack = { nav.popBackStack() }
            )
        }

        // Categorías
        composable(Ruta.Categorias.route) {
            PantallaCategorias(
                vm = movimientosVM,
                onBack = { nav.popBackStack() }
            )
        }

        // Lista de Miembros
        composable(
            route = Ruta.Miembros.route,
            arguments = listOf(navArgument(Ruta.Miembros.ARG){ type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString(Ruta.Miembros.ARG) ?: return@composable
            val vmMiembros: MiembrosViewModel = viewModel(
                factory = MiembrosVMFactory(
                    familiaRepo = ServiceLocator.familiaRepo
                )
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
            arguments = listOf(navArgument("familiaId"){ type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString("familiaId") ?: return@composable

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
                onIrAddGasto = { nav.navigate("add_gasto/$familiaId") },
                onIrAddIngreso = { nav.navigate("add_ingreso/$familiaId") },
                onIrHistorial = { nav.navigate("historial/$familiaId") },
                onBackToConfig = {
                    nav.navigate(Ruta.ConfigFamilia.route) {
                        popUpTo(Ruta.Inicio.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onAbrirConfiguracion = { nav.navigate(Ruta.Configuracion.route) },
                onVerCategorias = { nav.navigate(Ruta.Categorias.route) },
                onCambiarMoneda = { nav.navigate(Ruta.Moneda.route) },
                onIrSolicitudes = { nav.navigate(Ruta.Solicitudes.build(familiaId)) },
                onVerMiembros = { nav.navigate(Ruta.Miembros.build(familiaId)) },
                esAdmin = esAdmin,
                onExpulsado = goConfigOnKick
            )
        }

        // Gasto
        composable(
            route = Ruta.AddGasto.route,
            arguments = listOf(navArgument("familiaId") { type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString("familiaId") ?: return@composable
            PantallaAgregarGasto(
                familiaId = familiaId,
                vm = movimientosVM,
                familiaVM = familiaVM,
                onGuardado = { nav.popBackStack() },
                onBack = { nav.popBackStack() },
                onExpulsado = goConfigOnKick
            )
        }

        // Ingreso
        composable(
            route = Ruta.AddIngreso.route,
            arguments = listOf(navArgument("familiaId") { type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString("familiaId") ?: return@composable
            PantallaAgregarIngreso(
                familiaId = familiaId,
                vm = movimientosVM,
                familiaVM = familiaVM,
                onGuardado = { nav.popBackStack() },
                onBack = { nav.popBackStack() },
                onExpulsado = goConfigOnKick
            )
        }

        // Historial (lista de meses)
        composable(
            route = Ruta.Historial.route,
            arguments = listOf(navArgument("familiaId") { type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString("familiaId") ?: return@composable
            PantallaHistorial(
                familiaId = familiaId,
                familiaVM = familiaVM,
                onAbrirMes = { year, month ->
                    nav.navigate("historial_mes/$familiaId/$year/$month")
                },
                onBack = { nav.popBackStack() },
                onExpulsado = goConfigOnKick
            )
        }

        // Historial del mes
        composable(
            route = Ruta.HistorialMes.route,
            arguments = listOf(
                navArgument("familiaId") { type = NavType.StringType },
                navArgument("anio") { type = NavType.IntType },
                navArgument("mes") { type = NavType.IntType },
            )
        ) { backStack ->
            val familiaId = backStack.arguments?.getString("familiaId")
            val year = backStack.arguments?.getInt("anio")
            val month = backStack.arguments?.getInt("mes")
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

        // Solicitudes (admin)
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










