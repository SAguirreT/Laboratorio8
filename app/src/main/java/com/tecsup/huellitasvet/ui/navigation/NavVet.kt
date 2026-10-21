package com.tecsup.huellitasvet.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tecsup.huellitasvet.model.Rol
import com.tecsup.huellitasvet.ui.dueno.FichaMascotaScreen
import com.tecsup.huellitasvet.ui.dueno.FormularioMascotaScreen
import com.tecsup.huellitasvet.ui.dueno.InicioScreen
import com.tecsup.huellitasvet.ui.dueno.LoginScreen
import com.tecsup.huellitasvet.ui.dueno.MascotasScreen
import com.tecsup.huellitasvet.ui.dueno.MisCitasScreen
import com.tecsup.huellitasvet.ui.dueno.PedirCitaScreen
import com.tecsup.huellitasvet.ui.dueno.SplashScreen
import com.tecsup.huellitasvet.ui.veterinario.AgendaScreen
import com.tecsup.huellitasvet.ui.veterinario.AtenderCitaScreen
import com.tecsup.huellitasvet.ui.veterinario.PacientesScreen
import com.tecsup.huellitasvet.ui.veterinario.PanelScreen
import com.tecsup.huellitasvet.ui.veterinario.ServiciosScreen
import com.tecsup.huellitasvet.viewmodel.AgendaViewModel
import com.tecsup.huellitasvet.viewmodel.AtenderCitaViewModel
import com.tecsup.huellitasvet.viewmodel.InicioViewModel
import com.tecsup.huellitasvet.viewmodel.MascotaViewModel
import com.tecsup.huellitasvet.viewmodel.MisCitasViewModel
import com.tecsup.huellitasvet.viewmodel.PacientesViewModel
import com.tecsup.huellitasvet.viewmodel.PanelViewModel
import com.tecsup.huellitasvet.viewmodel.PedirCitaViewModel
import com.tecsup.huellitasvet.viewmodel.ServiciosViewModel
import com.tecsup.huellitasvet.viewmodel.SesionViewModel
import com.tecsup.huellitasvet.viewmodel.vetViewModel

private data class Pestana(val ruta: String, val etiqueta: String, val icono: ImageVector)

private val PESTANAS_DUENO = listOf(
    Pestana(Rutas.INICIO, "Inicio", Icons.Filled.Home),
    Pestana(Rutas.MASCOTAS, "Mascotas", Icons.Filled.Pets),
    Pestana(Rutas.MIS_CITAS, "Citas", Icons.Filled.CalendarMonth),
    Pestana(Rutas.PERFIL, "Perfil", Icons.Filled.Person)
)

private val PESTANAS_VET = listOf(
    Pestana(Rutas.PANEL, "Panel", Icons.Filled.BarChart),
    Pestana(Rutas.AGENDA, "Agenda", Icons.Filled.Event),
    Pestana(Rutas.PACIENTES, "Pacientes", Icons.Filled.Pets),
    Pestana(Rutas.SERVICIOS, "Servicios", Icons.Filled.LocalOffer),
    Pestana(Rutas.AJUSTES, "Ajustes", Icons.Filled.Settings)
)

/** Lee un argumento Int de la ruta; -1 significa 'no viene' (por ejemplo, mascota nueva). */
private val argId = listOf(navArgument("id") { type = NavType.IntType; defaultValue = -1 })
private val argMascotaId = listOf(navArgument("mascotaId") { type = NavType.IntType; defaultValue = -1 })

@Composable
fun NavVet(sesionVm: SesionViewModel) {
    val nav = rememberNavController()
    val usuario by sesionVm.usuarioActual.collectAsState()
    val cargando by sesionVm.cargando.collectAsState()
    val oscuro by sesionVm.modoOscuro.collectAsState()

    var splashListo by rememberSaveable { mutableStateOf(false) }
    // id del usuario cuya pantalla principal ya se abrió (0 = sin sesión, -1 = ninguno aún)
    var sesionAplicada by rememberSaveable { mutableIntStateOf(-1) }

    // Un solo lugar decide a dónde ir: login, inicio del dueño o panel del veterinario.
    // Se dispara al terminar el Splash, al iniciar sesión, al cambiar de perfil y al cerrar sesión.
    LaunchedEffect(splashListo, cargando, usuario?.id) {
        if (!splashListo || cargando) return@LaunchedEffect
        val u = usuario
        val id = u?.id ?: 0
        if (sesionAplicada == id) return@LaunchedEffect
        sesionAplicada = id
        val destino = when {
            u == null -> Rutas.LOGIN
            u.rol == Rol.ADMIN.name -> Rutas.PANEL
            else -> Rutas.INICIO
        }
        nav.navigate(destino) {
            popUpTo(nav.graph.id) { inclusive = true } // limpia la pila
            launchSingleTop = true
        }
    }

    val esVet = usuario?.rol == Rol.ADMIN.name
    val pestanas = if (esVet) PESTANAS_VET else PESTANAS_DUENO
    val inicio = if (esVet) Rutas.PANEL else Rutas.INICIO
    val entrada by nav.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (pestanas.any { it.ruta == rutaActual }) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    pestanas.forEach { p ->
                        NavigationBarItem(
                            selected = rutaActual == p.ruta,
                            onClick = { nav.irAPestana(p.ruta, inicio) },
                            icon = { Icon(p.icono, contentDescription = p.etiqueta) },
                            label = { Text(p.etiqueta) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Rutas.SPLASH,
            modifier = Modifier.padding(padding).imePadding()
        ) {
            composable(Rutas.SPLASH) { SplashScreen(onTerminar = { splashListo = true }) }

            composable(Rutas.LOGIN) { LoginScreen(sesionVm, oscuro) }

            // ================= Dueño =================
            composable(Rutas.INICIO) {
                val u = usuario ?: return@composable
                val vm = vetViewModel { app -> InicioViewModel(app.repository, app.preferencias, u.id) }
                InicioScreen(
                    vm = vm,
                    nombre = u.nombre,
                    onFicha = { nav.navigate(Rutas.ficha(it)) },
                    onAgregar = { nav.navigate(Rutas.formMascota()) },
                    onVerMascotas = { nav.irAPestana(Rutas.MASCOTAS, inicio) },
                    onPedirCita = { nav.navigate(Rutas.pedirCita()) }
                )
            }
            composable(Rutas.MASCOTAS) {
                val u = usuario ?: return@composable
                val vm = vetViewModel { app -> InicioViewModel(app.repository, app.preferencias, u.id) }
                MascotasScreen(
                    vm = vm,
                    onFicha = { nav.navigate(Rutas.ficha(it)) },
                    onAgregar = { nav.navigate(Rutas.formMascota()) }
                )
            }
            composable(Rutas.FORM_MASCOTA, arguments = argId) { entrada ->
                val u = usuario ?: return@composable
                val id = entrada.arguments?.getInt("id") ?: -1
                val vm = vetViewModel { app -> MascotaViewModel(app.repository, u.id, id.takeIf { it > 0 }) }
                FormularioMascotaScreen(
                    vm = vm,
                    esEdicion = id > 0,
                    onVolver = { nav.popBackStack() },
                    onEliminada = {
                        // Se eliminó desde 'Editar': también se cierra la ficha de esa mascota
                        if (!nav.popBackStack(Rutas.FICHA, inclusive = true)) nav.popBackStack()
                    }
                )
            }
            composable(Rutas.FICHA, arguments = argId) { entrada ->
                val u = usuario ?: return@composable
                val id = entrada.arguments?.getInt("id") ?: -1
                val vm = vetViewModel { app -> MascotaViewModel(app.repository, u.id, id) }
                FichaMascotaScreen(
                    vm = vm,
                    mascotaId = id,
                    esVeterinario = esVet,
                    onVolver = { nav.popBackStack() },
                    onPedirCita = { nav.navigate(Rutas.pedirCita(id)) },
                    onEditar = { nav.navigate(Rutas.formMascota(id)) }
                )
            }
            composable(Rutas.PEDIR_CITA, arguments = argMascotaId) { entrada ->
                val u = usuario ?: return@composable
                val mascotaId = entrada.arguments?.getInt("mascotaId") ?: -1
                val vm = vetViewModel { app ->
                    PedirCitaViewModel(app.repository, app.preferencias, u.id, mascotaId.takeIf { it > 0 })
                }
                PedirCitaScreen(
                    vm = vm,
                    onVolver = { nav.popBackStack() },
                    onCreada = {
                        nav.navigate(Rutas.MIS_CITAS) {
                            popUpTo(Rutas.INICIO)
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Rutas.MIS_CITAS) {
                val u = usuario ?: return@composable
                val vm = vetViewModel { app -> MisCitasViewModel(app.repository, u.id) }
                MisCitasScreen(vm, onPedirCita = { nav.navigate(Rutas.pedirCita()) })
            }
            composable(Rutas.PERFIL) { EnConstruccion("Perfil") }

            // ================= Veterinario =================
            composable(Rutas.PANEL) {
                val vm = vetViewModel { app -> PanelViewModel(app.repository) }
                PanelScreen(vm, onVerAgenda = { nav.irAPestana(Rutas.AGENDA, inicio) })
            }
            composable(Rutas.AGENDA) {
                val vm = vetViewModel { app -> AgendaViewModel(app.repository) }
                AgendaScreen(vm, onAtender = { nav.navigate(Rutas.atender(it)) })
            }
            composable(Rutas.ATENDER, arguments = argId) { entrada ->
                val id = entrada.arguments?.getInt("id") ?: -1
                val vm = vetViewModel { app -> AtenderCitaViewModel(app.repository, id) }
                AtenderCitaScreen(vm, onVolver = { nav.popBackStack() })
            }
            composable(Rutas.PACIENTES) {
                val vm = vetViewModel { app -> PacientesViewModel(app.repository) }
                PacientesScreen(vm, onFicha = { nav.navigate(Rutas.ficha(it)) })
            }
            composable(Rutas.SERVICIOS) {
                val vm = vetViewModel { app -> ServiciosViewModel(app.repository) }
                ServiciosScreen(vm, onEditar = { /* Paso 13 */ })
            }
            composable(Rutas.FORM_SERVICIO, arguments = argId) { EnConstruccion("Servicio") }
            composable(Rutas.AJUSTES) { EnConstruccion("Ajustes") }
        }
    }
}

/** Navegación entre pestañas: no apila pantallas y recuerda el estado de cada pestaña. */
private fun NavHostController.irAPestana(ruta: String, inicio: String) {
    navigate(ruta) {
        popUpTo(inicio) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Marcador temporal para las pantallas de los pasos siguientes. */
@Composable
private fun EnConstruccion(titulo: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$titulo: próximamente", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
