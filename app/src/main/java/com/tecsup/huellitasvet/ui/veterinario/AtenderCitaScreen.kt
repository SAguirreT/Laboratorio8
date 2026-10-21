package com.tecsup.huellitasvet.ui.veterinario

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tecsup.huellitasvet.data.local.relation.estado
import com.tecsup.huellitasvet.model.Especie
import com.tecsup.huellitasvet.model.Fechas
import com.tecsup.huellitasvet.model.Sexo
import com.tecsup.huellitasvet.ui.components.AppTopBar
import com.tecsup.huellitasvet.ui.components.EstadoCitaChip
import com.tecsup.huellitasvet.ui.components.MascotaAvatar
import com.tecsup.huellitasvet.ui.components.VarianteBoton
import com.tecsup.huellitasvet.ui.components.VetButton
import com.tecsup.huellitasvet.ui.components.VetTextField
import com.tecsup.huellitasvet.ui.components.iconoServicio
import com.tecsup.huellitasvet.ui.dueno.edadTexto
import com.tecsup.huellitasvet.ui.theme.EstadoCancelada
import com.tecsup.huellitasvet.viewmodel.AtenderCitaViewModel

@Composable
fun AtenderCitaScreen(vm: AtenderCitaViewModel, onVolver: () -> Unit) {
    val cita by vm.cita.collectAsState()
    val errores by vm.errores.collectAsState()
    val mensaje by vm.mensaje.collectAsState()
    val terminado by vm.terminado.collectAsState()
    val contexto = LocalContext.current
    val colores = MaterialTheme.colorScheme

    var peso by rememberSaveable { mutableStateOf("") }
    var diagnostico by rememberSaveable { mutableStateOf("") }
    var tratamiento by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(contexto, it, Toast.LENGTH_SHORT).show()
            vm.limpiarMensaje()
        }
    }
    LaunchedEffect(terminado) { if (terminado) onVolver() }

    Column(Modifier.fillMaxSize()) {
        AppTopBar("Atender cita", onVolver = onVolver)
        val c = cita ?: return@Column
        val m = c.mascota.mascota
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabecera
            Surface(shape = RoundedCornerShape(18.dp), color = colores.surface, border = BorderStroke(1.dp, colores.outline), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    MascotaAvatar(m.especie, tamano = 60.dp)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text("${m.nombre} · ${Especie.desde(m.especie).etiqueta} ${m.raza.lowercase()}", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${edadTexto(m.anioNacimiento)} · ${Sexo.desde(m.sexo).etiqueta} · Dueño: ${c.mascota.dueno.nombre}",
                            style = MaterialTheme.typography.bodySmall, color = colores.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Icon(iconoServicio(c.servicio.icono), contentDescription = null, tint = colores.primary, modifier = Modifier.size(16.dp))
                            Text(
                                " ${c.servicio.nombre} · ${Fechas.larga(c.cita.fecha)} · ${c.cita.hora}",
                                color = colores.primary, style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    EstadoCitaChip(c.estado)
                }
            }

            Text("Registro clínico", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                VetTextField(
                    peso, { peso = it }, "Peso (kg)", error = errores.peso,
                    teclado = KeyboardType.Decimal, modifier = Modifier.weight(1f)
                )
                Surface(shape = RoundedCornerShape(12.dp), color = colores.primaryContainer, modifier = Modifier.weight(1f).padding(top = 8.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Favorite, contentDescription = null, tint = EstadoCancelada, modifier = Modifier.size(18.dp))
                        Column(Modifier.padding(start = 8.dp)) {
                            Text("Peso anterior", style = MaterialTheme.typography.labelSmall, color = colores.onSurfaceVariant)
                            Text("${m.peso} kg", style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }
            }
            VetTextField(diagnostico, { diagnostico = it }, "Diagnóstico", error = errores.diagnostico, lineas = 3)
            VetTextField(tratamiento, { tratamiento = it }, "Tratamiento", lineas = 3)

            Surface(shape = RoundedCornerShape(12.dp), color = colores.primaryContainer.copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = colores.primary, modifier = Modifier.size(18.dp))
                    Text(
                        "  Se actualizará el peso de ${m.nombre} a ${peso.ifBlank { "--" }} kg",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            val activa = c.estado.esActiva
            VetButton("Registrar atención", onClick = { vm.registrar(diagnostico, tratamiento, peso) }, enabled = activa)
            VetButton("Cancelar cita", onClick = { vm.cancelar() }, variante = VarianteBoton.PELIGRO, enabled = activa)
        }
    }
}
