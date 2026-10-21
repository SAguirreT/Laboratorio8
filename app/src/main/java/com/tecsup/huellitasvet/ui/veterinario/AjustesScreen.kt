package com.tecsup.huellitasvet.ui.veterinario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tecsup.huellitasvet.data.local.entity.Usuario
import com.tecsup.huellitasvet.ui.components.AppTopBar
import com.tecsup.huellitasvet.ui.components.BotonCambiarPerfil
import com.tecsup.huellitasvet.ui.components.GrupoPreferencias
import com.tecsup.huellitasvet.ui.components.OpcionSwitch
import com.tecsup.huellitasvet.ui.components.TarjetaUsuario
import com.tecsup.huellitasvet.ui.components.VarianteBoton
import com.tecsup.huellitasvet.ui.components.VetButton
import com.tecsup.huellitasvet.viewmodel.SesionViewModel

/** Ajustes del veterinario. 'Aceptar citas' apagado impide que los dueños pidan citas. */
@Composable
fun AjustesScreen(usuario: Usuario, sesionVm: SesionViewModel) {
    val oscuro by sesionVm.modoOscuro.collectAsState()
    val notificaciones by sesionVm.notificaciones.collectAsState()
    val aceptarCitas by sesionVm.aceptarCitas.collectAsState()

    Column(Modifier.fillMaxSize()) {
        AppTopBar("Ajustes")
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TarjetaUsuario(usuario)
            BotonCambiarPerfil("Dueño", onClick = sesionVm::cambiarPerfil)
            Text("Preferencias", style = MaterialTheme.typography.titleMedium)
            GrupoPreferencias(
                listOf(
                    OpcionSwitch("Modo oscuro", "Cambia el tema de la app", oscuro, sesionVm::cambiarModoOscuro),
                    OpcionSwitch("Notificaciones", "Avisar citas nuevas", notificaciones, sesionVm::cambiarNotificaciones),
                    OpcionSwitch("Aceptar citas", "Abrir o cerrar la agenda", aceptarCitas, sesionVm::cambiarAceptarCitas)
                )
            )
            Text("Se guardan en el dispositivo con DataStore", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            VetButton("Cerrar sesión", onClick = sesionVm::cerrarSesion, variante = VarianteBoton.PELIGRO)
        }
    }
}
