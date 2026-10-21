package com.tecsup.huellitasvet.ui.dueno

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tecsup.huellitasvet.data.local.relation.CitaConDetalle
import com.tecsup.huellitasvet.data.local.relation.estado
import com.tecsup.huellitasvet.model.Fechas
import com.tecsup.huellitasvet.ui.components.AppTopBar
import com.tecsup.huellitasvet.ui.components.Aviso
import com.tecsup.huellitasvet.ui.components.CitaCard
import com.tecsup.huellitasvet.ui.components.SelectorSegmentado
import com.tecsup.huellitasvet.ui.components.iconoServicio
import com.tecsup.huellitasvet.viewmodel.MisCitasViewModel

@Composable
fun MisCitasScreen(
    vm: MisCitasViewModel,
    onPedirCita: () -> Unit,
    onReprogramar: (Int) -> Unit = {}
) {
    val proximas by vm.proximas.collectAsState()
    val historial by vm.historial.collectAsState()
    val mensaje by vm.mensaje.collectAsState()
    var pestana by rememberSaveable { mutableIntStateOf(0) }
    var porCancelar by remember { mutableStateOf<CitaConDetalle?>(null) }
    val contexto = LocalContext.current

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(contexto, it, Toast.LENGTH_SHORT).show()
            vm.limpiarMensaje()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            AppTopBar("Mis citas")
            SelectorSegmentado(
                listOf("Próximas", "Historial"), pestana, { pestana = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            val lista = if (pestana == 0) proximas else historial
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (lista.isEmpty()) {
                    item {
                        Text(
                            if (pestana == 0) "No tienes citas próximas" else "Aún no hay citas en tu historial",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    }
                }
                items(lista, key = { it.cita.id }) { c ->
                    CitaCard(
                        titulo = c.servicio.nombre,
                        subtitulo = "${c.mascota.mascota.nombre} · ${Fechas.larga(c.cita.fecha)} · ${c.cita.hora}",
                        icono = iconoServicio(c.servicio.icono),
                        estado = c.estado
                    ) {
                        if (pestana == 0 && c.estado.esActiva && vm.puedeCancelar(c)) {
                            TextButton(onClick = { onReprogramar(c.cita.id) }) { Text("Reprogramar") }
                        }
                        if (pestana == 0 && c.estado.esActiva) {
                            // Si faltan 2 horas o menos, el ViewModel responde 'Ya no puedes cancelar esta cita'
                            TextButton(onClick = { if (vm.puedeCancelar(c)) porCancelar = c else vm.cancelar(c) }) {
                                Text("Cancelar", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
                if (pestana == 0) {
                    item { Aviso(Icons.Filled.Schedule, "Puedes cancelar hasta 2 horas antes") }
                }
            }
        }
        FloatingActionButton(
            onClick = onPedirCita,
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Pedir cita")
        }
    }

    // Confirmación antes de cancelar
    porCancelar?.let { c ->
        AlertDialog(
            onDismissRequest = { porCancelar = null },
            title = { Text("¿Cancelar la cita?") },
            text = { Text("${c.servicio.nombre} de ${c.mascota.mascota.nombre} el ${Fechas.larga(c.cita.fecha)} a las ${c.cita.hora}.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.cancelar(c)
                    porCancelar = null
                }) { Text("Sí, cancelar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { porCancelar = null }) { Text("No") }
            }
        )
    }
}
