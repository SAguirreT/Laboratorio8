package com.tecsup.huellitasvet.ui.dueno

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tecsup.huellitasvet.data.local.entity.Servicio
import com.tecsup.huellitasvet.model.Fechas
import com.tecsup.huellitasvet.ui.components.AppTopBar
import com.tecsup.huellitasvet.ui.components.Aviso
import com.tecsup.huellitasvet.ui.components.FechaChip
import com.tecsup.huellitasvet.ui.components.HoraChip
import com.tecsup.huellitasvet.ui.components.MascotaAvatar
import com.tecsup.huellitasvet.ui.components.VetButton
import com.tecsup.huellitasvet.ui.components.iconoServicio
import com.tecsup.huellitasvet.viewmodel.PedirCitaViewModel

@Composable
fun PedirCitaScreen(
    vm: PedirCitaViewModel,
    onVolver: () -> Unit,
    onCreada: () -> Unit
) {
    val mascotas by vm.mascotas.collectAsState()
    val servicios by vm.servicios.collectAsState()
    val mascotaId by vm.mascotaId.collectAsState()
    val servicioId by vm.servicioId.collectAsState()
    val fecha by vm.fecha.collectAsState()
    val hora by vm.hora.collectAsState()
    val horarios by vm.horarios.collectAsState()
    val puedeConfirmar by vm.puedeConfirmar.collectAsState()
    val aceptaCitas by vm.aceptaCitas.collectAsState()
    val mensaje by vm.mensaje.collectAsState()
    val creada by vm.citaCreada.collectAsState()
    val contexto = LocalContext.current
    val colores = MaterialTheme.colorScheme

    LaunchedEffect(creada) {
        if (creada) {
            Toast.makeText(contexto, "Cita registrada como PENDIENTE", Toast.LENGTH_SHORT).show()
            onCreada()
        }
    }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(contexto, it, Toast.LENGTH_LONG).show()
            vm.limpiarMensaje()
        }
    }

    Column(Modifier.fillMaxSize()) {
        AppTopBar("Pedir cita", onVolver = onVolver)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!aceptaCitas) {
                Aviso(Icons.Filled.Warning, "La clínica no está recibiendo citas", "Vuelve a intentarlo más tarde.")
            }

            Text("¿Quién viene?", style = MaterialTheme.typography.titleSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(mascotas, key = { it.id }) { m ->
                    val elegido = m.id == mascotaId
                    Surface(
                        onClick = { vm.elegirMascota(m.id) },
                        shape = RoundedCornerShape(50),
                        color = if (elegido) colores.primaryContainer else colores.surface,
                        border = BorderStroke(if (elegido) 1.5.dp else 1.dp, if (elegido) colores.primary else colores.outline)
                    ) {
                        Row(Modifier.padding(start = 6.dp, end = 16.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            MascotaAvatar(m.especie, tamano = 30.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(m.nombre, style = MaterialTheme.typography.labelLarge, color = if (elegido) colores.primary else colores.onSurface)
                        }
                    }
                }
            }

            Text("Servicio", style = MaterialTheme.typography.titleSmall)
            servicios.forEach { s ->
                FilaServicio(s, elegido = s.id == servicioId, onClick = { vm.elegirServicio(s.id) })
            }

            Text("Fecha", style = MaterialTheme.typography.titleSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(vm.fechas) { f -> FechaChip(f, f == fecha, onClick = { vm.elegirFecha(f) }) }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Hora", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text("Ocupado = gris", style = MaterialTheme.typography.labelSmall, color = colores.onSurfaceVariant)
            }
            // 18 horarios en filas de 4
            horarios.chunked(4).forEach { fila ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    fila.forEach { h ->
                        HoraChip(
                            hora = h.hora,
                            seleccionado = h.hora == hora,
                            ocupado = !h.disponible,
                            onClick = { vm.elegirHora(h.hora) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(4 - fila.size) { Spacer(Modifier.weight(1f)) }
                }
            }

            // Resumen
            val mascota = mascotas.firstOrNull { it.id == mascotaId }
            val servicio = servicios.firstOrNull { it.id == servicioId }
            if (mascota != null && servicio != null) {
                Surface(shape = RoundedCornerShape(14.dp), color = colores.primaryContainer.copy(alpha = 0.6f), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text("${mascota.nombre} · ${servicio.nombre}", color = colores.primary, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${Fechas.larga(fecha.toString())} · ${hora ?: "--:--"} · S/ %.2f".format(servicio.precio),
                            style = MaterialTheme.typography.bodySmall,
                            color = colores.onSurfaceVariant
                        )
                    }
                }
            }

            VetButton("Confirmar cita", onClick = { vm.confirmar() }, enabled = puedeConfirmar)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FilaServicio(s: Servicio, elegido: Boolean, onClick: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (elegido) colores.primaryContainer.copy(alpha = 0.6f) else colores.surface,
        border = BorderStroke(if (elegido) 1.5.dp else 1.dp, if (elegido) colores.primary else colores.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(iconoServicio(s.icono), contentDescription = null, tint = colores.primary)
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(s.nombre, style = MaterialTheme.typography.titleSmall)
                Text("${s.duracionMin} min", style = MaterialTheme.typography.bodySmall, color = colores.onSurfaceVariant)
            }
            Text("S/ %.0f".format(s.precio), color = colores.primary, style = MaterialTheme.typography.titleSmall)
        }
    }
}
