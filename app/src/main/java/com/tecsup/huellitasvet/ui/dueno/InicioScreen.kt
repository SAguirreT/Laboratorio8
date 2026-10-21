package com.tecsup.huellitasvet.ui.dueno

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tecsup.huellitasvet.data.local.entity.Mascota
import com.tecsup.huellitasvet.data.local.relation.CitaConDetalle
import com.tecsup.huellitasvet.data.local.relation.estado
import com.tecsup.huellitasvet.model.Fechas
import com.tecsup.huellitasvet.ui.components.Aviso
import com.tecsup.huellitasvet.ui.components.MascotaAvatar
import com.tecsup.huellitasvet.ui.components.VetButton
import com.tecsup.huellitasvet.ui.components.iconoServicio
import com.tecsup.huellitasvet.viewmodel.InicioViewModel

@Composable
fun InicioScreen(
    vm: InicioViewModel,
    nombre: String,
    onFicha: (Int) -> Unit,
    onAgregar: () -> Unit,
    onVerMascotas: () -> Unit,
    onPedirCita: () -> Unit
) {
    val mascotas by vm.mascotas.collectAsState()
    val proxima by vm.proximaCita.collectAsState()
    val servicios by vm.servicios.collectAsState()
    val aceptaCitas by vm.aceptaCitas.collectAsState()
    val colores = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Saludo
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Hola, ${nombre.substringBefore(' ')}", style = MaterialTheme.typography.headlineSmall)
                Text("¿Cómo están tus mascotas hoy?", color = colores.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Filled.Notifications, contentDescription = null, tint = colores.onSurface)
        }

        TarjetaProximaCita(proxima)

        // Mis mascotas
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Mis mascotas", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text("Ver todas", color = colores.primary, style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clickable(onClick = onVerMascotas).padding(4.dp))
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(mascotas, key = { it.id }) { m -> TarjetaMascotaMini(m, onClick = { onFicha(m.id) }) }
            item { TarjetaAgregar(onAgregar) }
        }

        // Servicios
        Text("Servicios", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            servicios.take(3).forEach { s ->
                Surface(
                    onClick = onPedirCita,
                    shape = RoundedCornerShape(14.dp),
                    color = colores.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 14.dp)) {
                        Icon(iconoServicio(s.icono), contentDescription = null, tint = colores.primary)
                        Spacer(Modifier.height(6.dp))
                        Text(s.nombre.substringBefore(' '), style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }

        if (!aceptaCitas) {
            Aviso(Icons.Filled.Warning, "La clínica no está recibiendo citas", "Vuelve a intentarlo más tarde.")
        } else if (proxima == null && mascotas.isNotEmpty()) {
            Aviso(Icons.Filled.Notifications, "Recordatorio", "Agenda un control para tus mascotas.")
        }

        VetButton("Pedir una cita", onClick = onPedirCita, enabled = aceptaCitas && mascotas.isNotEmpty())
    }
}

@Composable
private fun TarjetaProximaCita(cita: CitaConDetalle?) {
    val colores = MaterialTheme.colorScheme
    Surface(shape = RoundedCornerShape(18.dp), color = colores.primary, contentColor = colores.onPrimary, modifier = Modifier.fillMaxWidth()) {
        if (cita == null) {
            Column(Modifier.padding(18.dp)) {
                Text("Próxima cita", style = MaterialTheme.typography.labelMedium, color = colores.onPrimary.copy(alpha = 0.8f))
                Spacer(Modifier.height(4.dp))
                Text("Aún no tienes citas", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Próxima cita", style = MaterialTheme.typography.labelMedium, color = colores.onPrimary.copy(alpha = 0.8f))
                    Text(
                        "${cita.mascota.mascota.nombre} · ${cita.servicio.nombre}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(" ${Fechas.larga(cita.cita.fecha)} · ${cita.cita.hora}", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(8.dp))
                    Surface(shape = RoundedCornerShape(50), color = Color.White) {
                        Text(
                            cita.estado.etiqueta,
                            color = colores.primary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
                        )
                    }
                }
                Box(Modifier.background(Color.White, RoundedCornerShape(14.dp)).padding(6.dp)) {
                    MascotaAvatar(cita.mascota.mascota.especie, tamano = 44.dp)
                }
            }
        }
    }
}

@Composable
private fun TarjetaMascotaMini(m: Mascota, onClick: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = colores.surface,
        border = BorderStroke(1.dp, colores.outline),
        modifier = Modifier.width(100.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 14.dp)) {
            MascotaAvatar(m.especie, tamano = 52.dp)
            Spacer(Modifier.height(8.dp))
            Text(m.nombre, style = MaterialTheme.typography.titleSmall)
            Text("Ver ficha", style = MaterialTheme.typography.labelSmall, color = colores.onSurfaceVariant)
        }
    }
}

@Composable
private fun TarjetaAgregar(onClick: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = colores.surface,
        border = BorderStroke(1.5.dp, colores.primary),
        modifier = Modifier.width(100.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 22.dp)) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = colores.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(10.dp))
            Text("Agregar", color = colores.primary, style = MaterialTheme.typography.labelLarge)
        }
    }
}
