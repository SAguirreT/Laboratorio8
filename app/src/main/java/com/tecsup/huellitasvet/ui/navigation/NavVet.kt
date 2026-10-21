package com.tecsup.huellitasvet.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tecsup.huellitasvet.ui.dueno.SplashScreen
import com.tecsup.huellitasvet.ui.veterinario.ServiciosScreen
import com.tecsup.huellitasvet.viewmodel.ServiciosViewModel
import com.tecsup.huellitasvet.viewmodel.vetViewModel

@Composable
fun NavVet() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Rutas.SPLASH) {
        composable(Rutas.SPLASH) {
            SplashScreen(onTerminar = {
                nav.navigate(Rutas.SERVICIOS) { popUpTo(Rutas.SPLASH) { inclusive = true } }
            })
        }
        composable(Rutas.SERVICIOS) {
            val vm = vetViewModel { app -> ServiciosViewModel(app.repository) }
            ServiciosScreen(vm, onEditar = { /* TODO (Paso 13) */ })
        }
        // TODO (Paso 6 en adelante): login y el grafo de cada perfil
    }
}
