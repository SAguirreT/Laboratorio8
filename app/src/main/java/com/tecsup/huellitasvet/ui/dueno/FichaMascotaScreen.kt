package com.tecsup.huellitasvet.ui.dueno

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tecsup.huellitasvet.data.local.entity.Mascota
import com.tecsup.huellitasvet.data.local.relation.CitaConDetalle
import com.tecsup.huellitasvet.model.Especie
import com.tecsup.huellitasvet.model.Fechas
import com.tecsup.huellitasvet.model.Sexo
import com.tecsup.huellitasvet.ui.components.AppTopBar
import com.tecsup.huellitasvet.ui.components.HistorialItem
import com.tecsup.huellitasvet.ui.components.MascotaAvatar
import com.tecsup.huellitasvet.ui.components.SelectorSegmentado
import com.tecsup.huellitasvet.ui.components.VarianteBoton
import com.tecsup.huellitasvet.ui.components.VetButton
import com.tecsup.huellitasvet.viewmodel.MascotaViewModel

/** Ficha de la mascota: cabecera, acciones y pestañas Datos / Historial clínico. */
@Composable
fun FichaMascotaScreen(
    vm: MascotaViewModel,
    mascotaId: Int,
    esVeterinario: Boolean,
    onVolver: () -> Unit,
    onPedirCita: () -> Unit,
    onEditar: () -> Unit
) {
    val mascota by vm.mascota.collectAsState()
    val historial by vm.historial(mascotaId).collectAsState()
    var pestana by rememberSaveable { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        AppTopBar(mascota?.nombre ?: "Ficha", onVolver = onVolver)
        val m = mascota ?: return@Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CabeceraMascota(m)
            if (!esVeterinario) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    VetButton("Pedir cita", onClick = onPedirCita, icono = Icons.Filled.CalendarMonth, modifier = Modifier.weight(1f))
                    VetButton("Editar", onClick = onEditar, icono = Icons.Filled.Edit, variante = VarianteBoton.SECUNDARIO, modifier = Modifier.weight(1f))
                }
            }
            SelectorSegmentado(listOf("Datos", "Historial clínico"), pestana, { pestana = it })
            if (pestana == 0) DatosMascota(m, historial.size) else HistorialClinico(historial)
        }
    }
}

@Composable
private fun CabeceraMascota(m: Mascota) {
    val colores = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colores.surface,
        border = BorderStroke(1.dp, colores.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            MascotaAvatar(m.especie, tamano = 72.dp)
            Column(Modifier.padding(start = 14.dp)) {
                Text(m.nombre, style = MaterialTheme.typography.titleLarge)
                Text(
                    "${Especie.desde(m.especie).etiqueta} ${m.raza.lowercase()} · ${edadTexto(m.anioNacimiento)}",
                    style = MaterialTheme.typography.bodySmall, color = colores.onSurfaceVariant
                )
                Text(
                    "${Sexo.desde(m.sexo).etiqueta} · ${m.peso} kg",
                    style = MaterialTheme.typography.bodySmall, color = colores.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DatosMascota(m: Mascota, atenciones: Int) {
    val colores = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colores.surface,
        border = BorderStroke(1.dp, colores.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            val filas = listOf(
                "Especie" to Especie.desde(m.especie).etiqueta,
                "Raza" to m.raza,
                "Sexo" to Sexo.desde(m.sexo).etiqueta,
                "Año de nacimiento" to m.anioNacimiento.toString(),
                "Peso actual" to "${m.peso} kg",
                "Atenciones" to atenciones.toString()
            )
            filas.forEachIndexed { i, (etiqueta, valor) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                    Text(etiqueta, color = colores.onSurfaceVariant, modifier = Modifier.weight(1f))
                    Text(valor, style = MaterialTheme.typography.titleSmall)
                }
                if (i < filas.lastIndex) HorizontalDivider(color = colores.outline)
            }
        }
    }
}

@Composable
private fun HistorialClinico(historial: List<CitaConDetalle>) {
    if (historial.isEmpty()) {
        Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
            Text("Aún no tiene atenciones", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    Column {
        historial.forEachIndexed { i, c ->
            HistorialItem(
                fecha = Fechas.diaMes(c.cita.fecha),
                servicio = c.servicio.nombre,
                diagnostico = c.cita.diagnostico ?: "",
                peso = c.cita.pesoRegistrado,
                esUltimo = i == historial.lastIndex
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}
