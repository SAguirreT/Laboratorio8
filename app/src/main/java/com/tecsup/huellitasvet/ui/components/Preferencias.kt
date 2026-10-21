package com.tecsup.huellitasvet.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tecsup.huellitasvet.data.local.entity.Usuario
import com.tecsup.huellitasvet.model.Rol

/** Tarjeta con los datos del usuario (Perfil y Ajustes). */
@Composable
fun TarjetaUsuario(usuario: Usuario) {
    val colores = MaterialTheme.colorScheme
    Surface(shape = RoundedCornerShape(18.dp), color = colores.surface, border = BorderStroke(1.dp, colores.outline), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).background(colores.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = colores.primary)
            }
            Column(Modifier.padding(start = 14.dp)) {
                Text(usuario.nombre, style = MaterialTheme.typography.titleMedium)
                Text(usuario.correo, style = MaterialTheme.typography.bodySmall, color = colores.onSurfaceVariant)
                Surface(shape = RoundedCornerShape(50), color = colores.primaryContainer, modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        Rol.desde(usuario.rol).etiqueta,
                        color = colores.primary,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/** Botón grande 'Cambiar de perfil' (modo práctica). */
@Composable
fun BotonCambiarPerfil(destino: String, onClick: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    Surface(onClick = onClick, shape = RoundedCornerShape(16.dp), color = colores.primary, contentColor = colores.onPrimary, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.SwapHoriz, contentDescription = null)
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text("Cambiar de perfil", style = MaterialTheme.typography.titleSmall)
                Text("Modo práctica: ir a vista $destino", style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

data class OpcionSwitch(val titulo: String, val detalle: String, val activo: Boolean, val onCambio: (Boolean) -> Unit)

/** Grupo de preferencias con switches. */
@Composable
fun GrupoPreferencias(opciones: List<OpcionSwitch>) {
    val colores = MaterialTheme.colorScheme
    Surface(shape = RoundedCornerShape(16.dp), color = colores.surface, border = BorderStroke(1.dp, colores.outline), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            opciones.forEachIndexed { i, o ->
                Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(o.titulo, style = MaterialTheme.typography.titleSmall)
                        Text(o.detalle, style = MaterialTheme.typography.bodySmall, color = colores.onSurfaceVariant)
                    }
                    Switch(checked = o.activo, onCheckedChange = o.onCambio)
                }
                if (i < opciones.lastIndex) HorizontalDivider(color = colores.outline)
            }
        }
    }
}
