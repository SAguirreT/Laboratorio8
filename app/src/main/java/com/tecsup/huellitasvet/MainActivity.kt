package com.tecsup.huellitasvet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.tecsup.huellitasvet.model.Rol
import com.tecsup.huellitasvet.ui.navigation.NavVet
import com.tecsup.huellitasvet.ui.theme.VetTheme
import com.tecsup.huellitasvet.viewmodel.SesionViewModel
import com.tecsup.huellitasvet.viewmodel.vetViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // La sesión vive a nivel de Activity: la comparten todas las pantallas.
            val sesionVm = vetViewModel { app -> SesionViewModel(app.repository, app.preferencias) }
            val usuario by sesionVm.usuarioActual.collectAsState()
            val oscuro by sesionVm.modoOscuro.collectAsState()
            val rol = Rol.desde(usuario?.rol)
            VetTheme(rol = rol, oscuro = oscuro) {
                NavVet(sesionVm)
            }
        }
    }
}
