package com.tecsup.huellitasvet.ui.veterinario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tecsup.huellitasvet.model.Especie
import com.tecsup.huellitasvet.model.Fechas
import com.tecsup.huellitasvet.ui.components.AppTopBar
import com.tecsup.huellitasvet.ui.components.MascotaAvatar
import com.tecsup.huellitasvet.ui.components.OpcionChip
import com.tecsup.huellitasvet.ui.components.VetTextField
import com.tecsup.huellitasvet.ui.dueno.edadTexto
import com.tecsup.huellitasvet.viewmodel.PacienteUi
import com.tecsup.huellitasvet.viewmodel.PacientesViewModel

@Composable
fun PacientesScreen(vm: PacientesViewModel, onFicha: (Int) -> Unit) {
    val texto by vm.texto.collectAsState()
    val especie by vm.especie.collectAsState()
    val pacientes by vm.pacientes.collectAsState()

    Column(Modifier.fillMaxSize()) {
        AppTopBar("Pacientes")
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            VetTextField(texto, vm::buscar, "Buscar mascota o dueño...", icono = Icons.Filled.Search)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OpcionChip("Todos", especie == null, onClick = { vm.filtrar(null) })
                OpcionChip("Perros", especie == Especie.PERRO, onClick = { vm.filtrar(Especie.PERRO) })
                OpcionChip("Gatos", especie == Especie.GATO, onClick = { vm.filtrar(Especie.GATO) })
                OpcionChip("Otros", especie == Especie.OTRO, onClick = { vm.filtrar(Especie.OTRO) })
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (pacientes.isEmpty()) {
                item {
                    Text("No se encontraron pacientes", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 24.dp))
                }
            }
            items(pacientes, key = { it.datos.mascota.id }) { p ->
                TarjetaPaciente(p, onClick = { onFicha(p.datos.mascota.id) })
            }
        }
    }
}

@Composable
private fun TarjetaPaciente(p: PacienteUi, onClick: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    val m = p.datos.mascota
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = colores.surface,
        border = BorderStroke(1.dp, colores.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            MascotaAvatar(m.especie)
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(m.nombre, style = MaterialTheme.typography.titleSmall)
                Text("${m.raza} · ${edadTexto(m.anioNacimiento)}", style = MaterialTheme.typography.bodySmall, color = colores.onSurfaceVariant)
                Text("Dueño: ${p.datos.dueno.nombre}", style = MaterialTheme.typography.bodySmall, color = colores.onSurfaceVariant)
                Text(
                    p.ultimaVisita?.let { "Última visita: ${Fechas.diaMes(it)}" } ?: "Sin visitas",
                    style = MaterialTheme.typography.labelSmall,
                    color = colores.onSurfaceVariant
                )
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = colores.onSurfaceVariant)
        }
    }
}
