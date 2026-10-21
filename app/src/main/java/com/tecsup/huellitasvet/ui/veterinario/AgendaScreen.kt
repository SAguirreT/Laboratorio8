package com.tecsup.huellitasvet.ui.veterinario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tecsup.huellitasvet.data.local.relation.CitaConDetalle
import com.tecsup.huellitasvet.data.local.relation.estado
import com.tecsup.huellitasvet.model.EstadoCita
import com.tecsup.huellitasvet.model.Fechas
import com.tecsup.huellitasvet.ui.components.AppTopBar
import com.tecsup.huellitasvet.ui.components.EstadoCitaChip
import com.tecsup.huellitasvet.ui.components.MascotaAvatar
import com.tecsup.huellitasvet.ui.components.OpcionChip
import com.tecsup.huellitasvet.viewmodel.AgendaViewModel
import java.time.LocalDate

/** Agenda del día: línea de tiempo por hora con tarjetas y acciones según el estado. */
@Composable
fun AgendaScreen(vm: AgendaViewModel, onAtender: (Int) -> Unit) {
    val fecha by vm.fechaSeleccionada.collectAsState()
    val citas by vm.citas.collectAsState()
    val hoy = LocalDate.now()

    Column(Modifier.fillMaxSize()) {
        AppTopBar("Agenda")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(vm.dias) { d ->
                val texto = if (d == hoy) "Hoy ${d.dayOfMonth}" else "${Fechas.diaCorto(d)} ${d.dayOfMonth}"
                OpcionChip(texto, d == fecha, onClick = { vm.elegirFecha(d) })
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            if (citas.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                        Text("No hay citas para este día", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            items(citas, key = { it.cita.id }) { c ->
                FilaAgenda(
                    c,
                    onConfirmar = { vm.confirmar(c) },
                    onAtender = { onAtender(c.cita.id) },
                    onCancelar = { vm.cancelar(c) }
                )
            }
        }
    }
}

@Composable
private fun FilaAgenda(c: CitaConDetalle, onConfirmar: () -> Unit, onAtender: () -> Unit, onCancelar: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        // Hora + línea de tiempo
        Column(Modifier.width(56.dp)) {
            Text(c.cita.hora, color = colores.primary, style = MaterialTheme.typography.labelLarge)
        }
        Box(Modifier.width(2.dp).fillMaxHeight().background(colores.outline))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colores.surface,
            border = BorderStroke(1.dp, colores.outline),
            modifier = Modifier.weight(1f).padding(start = 10.dp, bottom = 12.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MascotaAvatar(c.mascota.mascota.especie, tamano = 40.dp)
                    Column(Modifier.weight(1f).padding(start = 10.dp)) {
                        Text("${c.mascota.mascota.nombre} · ${c.servicio.nombre}", style = MaterialTheme.typography.titleSmall)
                        Text("Dueño: ${c.mascota.dueno.nombre}", style = MaterialTheme.typography.bodySmall, color = colores.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EstadoCitaChip(c.estado)
                    Spacer(Modifier.weight(1f))
                    if (c.estado.esActiva) {
                        TextButton(onClick = onCancelar) { Text("Cancelar", color = colores.error) }
                    }
                    when (c.estado) {
                        EstadoCita.PENDIENTE -> Button(onClick = onConfirmar) { Text("Confirmar") }
                        EstadoCita.CONFIRMADA -> Button(onClick = onAtender) { Text("Atender") }
                        else -> Unit
                    }
                }
            }
        }
    }
}
