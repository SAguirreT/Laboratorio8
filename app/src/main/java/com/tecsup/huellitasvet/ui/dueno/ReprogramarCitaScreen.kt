package com.tecsup.huellitasvet.ui.dueno

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tecsup.huellitasvet.data.local.relation.estado
import com.tecsup.huellitasvet.model.Fechas
import com.tecsup.huellitasvet.ui.components.AppTopBar
import com.tecsup.huellitasvet.ui.components.Aviso
import com.tecsup.huellitasvet.ui.components.CitaCard
import com.tecsup.huellitasvet.ui.components.FechaChip
import com.tecsup.huellitasvet.ui.components.HoraChip
import com.tecsup.huellitasvet.ui.components.VetButton
import com.tecsup.huellitasvet.ui.components.iconoServicio
import com.tecsup.huellitasvet.viewmodel.ReprogramarCitaViewModel

/** Mejora IA: elegir nueva fecha y hora para una cita. */
@Composable
fun ReprogramarCitaScreen(vm: ReprogramarCitaViewModel, onVolver: () -> Unit) {
    val cita by vm.cita.collectAsState()
    val fecha by vm.fecha.collectAsState()
    val hora by vm.hora.collectAsState()
    val horarios by vm.horarios.collectAsState()
    val mensaje by vm.mensaje.collectAsState()
    val listo by vm.listo.collectAsState()
    val contexto = LocalContext.current

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(contexto, it, Toast.LENGTH_SHORT).show()
            vm.limpiarMensaje()
        }
    }
    LaunchedEffect(listo) { if (listo) onVolver() }

    Column(Modifier.fillMaxSize()) {
        AppTopBar("Reprogramar cita", onVolver = onVolver)
        val c = cita ?: return@Column
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Cita actual", style = MaterialTheme.typography.titleSmall)
            CitaCard(
                titulo = c.servicio.nombre,
                subtitulo = "${c.mascota.mascota.nombre} · ${Fechas.larga(c.cita.fecha)} · ${c.cita.hora}",
                icono = iconoServicio(c.servicio.icono),
                estado = c.estado
            )
            if (!vm.sePuedeReprogramar(c)) {
                Aviso(Icons.Filled.Event, "Ya no puedes reprogramar esta cita", "Solo hasta 2 horas antes.")
                return@Column
            }

            Text("Nueva fecha", style = MaterialTheme.typography.titleSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(vm.fechas) { f -> FechaChip(f, f == fecha, onClick = { vm.elegirFecha(f) }) }
            }
            Text("Nueva hora", style = MaterialTheme.typography.titleSmall)
            horarios.chunked(4).forEach { fila ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    fila.forEach { h ->
                        HoraChip(h.hora, h.hora == hora, !h.disponible, { vm.elegirHora(h.hora) }, Modifier.weight(1f))
                    }
                    repeat(4 - fila.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            VetButton("Guardar nuevo horario", onClick = { vm.guardar() }, enabled = hora != null)
        }
    }
}
