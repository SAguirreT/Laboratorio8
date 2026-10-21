package com.tecsup.huellitasvet.ui.dueno

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tecsup.huellitasvet.data.local.entity.Usuario
import com.tecsup.huellitasvet.ui.components.AppTopBar
import com.tecsup.huellitasvet.ui.components.BotonCambiarPerfil
import com.tecsup.huellitasvet.ui.components.GrupoPreferencias
import com.tecsup.huellitasvet.ui.components.OpcionSwitch
import com.tecsup.huellitasvet.ui.components.TarjetaUsuario
import com.tecsup.huellitasvet.ui.components.VarianteBoton
import com.tecsup.huellitasvet.ui.components.VetButton
import com.tecsup.huellitasvet.ui.theme.AmbarSuave
import com.tecsup.huellitasvet.ui.theme.EstadoAtendida
import com.tecsup.huellitasvet.ui.theme.EstadoPendiente
import com.tecsup.huellitasvet.ui.theme.Superficie2
import com.tecsup.huellitasvet.viewmodel.SesionViewModel

/** Perfil del dueño: datos, contadores, preferencias, cambio de perfil y cierre de sesión. */
@Composable
fun PerfilScreen(
    usuario: Usuario,
    sesionVm: SesionViewModel,
    mascotas: Int,
    proximas: Int,
    atenciones: Int
) {
    val oscuro by sesionVm.modoOscuro.collectAsState()
    val notificaciones by sesionVm.notificaciones.collectAsState()
    val colores = MaterialTheme.colorScheme

    Column(Modifier.fillMaxSize()) {
        AppTopBar("Perfil")
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TarjetaUsuario(usuario)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Contador(mascotas, "Mascotas", colores.primary, colores.primaryContainer, Modifier.weight(1f))
                Contador(proximas, "Próximas", EstadoPendiente, AmbarSuave, Modifier.weight(1f))
                Contador(atenciones, "Atenciones", EstadoAtendida, Superficie2, Modifier.weight(1f))
            }
            BotonCambiarPerfil("Veterinario", onClick = sesionVm::cambiarPerfil)
            Text("Preferencias", style = MaterialTheme.typography.titleMedium)
            GrupoPreferencias(
                listOf(
                    OpcionSwitch("Modo oscuro", "Cambia el tema de la app", oscuro, sesionVm::cambiarModoOscuro),
                    OpcionSwitch("Recordatorios", "Avisarme antes de cada cita", notificaciones, sesionVm::cambiarNotificaciones)
                )
            )
            Text("Se guardan en el dispositivo con DataStore", style = MaterialTheme.typography.bodySmall, color = colores.onSurfaceVariant)
            VetButton("Cerrar sesión", onClick = sesionVm::cerrarSesion, variante = VarianteBoton.PELIGRO)
        }
    }
}

@Composable
private fun Contador(valor: Int, etiqueta: String, color: Color, fondo: Color, modifier: Modifier) {
    Surface(shape = RoundedCornerShape(16.dp), color = fondo, modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 14.dp)) {
            Text(valor.toString(), color = color, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(etiqueta, color = color, style = MaterialTheme.typography.labelMedium)
        }
    }
}
