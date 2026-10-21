package com.tecsup.huellitasvet.ui.veterinario

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tecsup.huellitasvet.data.local.entity.Servicio
import com.tecsup.huellitasvet.ui.components.AppTopBar
import com.tecsup.huellitasvet.ui.components.ICONOS_SERVICIO
import com.tecsup.huellitasvet.ui.components.OpcionChip
import com.tecsup.huellitasvet.ui.components.VarianteBoton
import com.tecsup.huellitasvet.ui.components.VetButton
import com.tecsup.huellitasvet.ui.components.VetTextField
import com.tecsup.huellitasvet.ui.components.iconoServicio
import com.tecsup.huellitasvet.viewmodel.ServiciosViewModel

/** Crear (servicioId = -1) o editar un servicio. */
@Composable
fun FormularioServicioScreen(vm: ServiciosViewModel, servicioId: Int, onVolver: () -> Unit) {
    val servicios by vm.servicios.collectAsState()
    val errores by vm.errores.collectAsState()
    val listo by vm.listo.collectAsState()
    val mensaje by vm.mensaje.collectAsState()
    val contexto = LocalContext.current
    val colores = MaterialTheme.colorScheme
    val original = servicios.firstOrNull { it.id == servicioId }

    var nombre by rememberSaveable { mutableStateOf("") }
    var duracion by rememberSaveable { mutableIntStateOf(30) }
    var precio by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var icono by rememberSaveable { mutableStateOf("heart") }
    var activo by rememberSaveable { mutableStateOf(true) }
    var precargado by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(original) {
        if (original != null && !precargado) {
            nombre = original.nombre
            duracion = original.duracionMin
            precio = original.precio.toString()
            descripcion = original.descripcion
            icono = original.icono
            activo = original.activo
            precargado = true
        }
    }
    LaunchedEffect(listo) { if (listo) onVolver() }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(contexto, it, Toast.LENGTH_LONG).show()
            vm.limpiarMensaje()
        }
    }

    Column(Modifier.fillMaxSize()) {
        AppTopBar(if (servicioId > 0) "Editar servicio" else "Nuevo servicio", onVolver = onVolver)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VetTextField(nombre, { nombre = it }, "Nombre del servicio", error = errores.nombre)

            Text("Duración", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ServiciosViewModel.DURACIONES.forEach { d ->
                    OpcionChip("$d min", duracion == d, { duracion = d }, Modifier.weight(1f))
                }
            }
            errores.duracion?.let { Text(it, color = colores.error, style = MaterialTheme.typography.bodySmall) }

            VetTextField(precio, { precio = it }, "Precio (S/)", error = errores.precio, teclado = KeyboardType.Decimal)
            VetTextField(descripcion, { descripcion = it }, "Descripción", lineas = 3)

            Text("Ícono", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ICONOS_SERVICIO.forEach { i ->
                    val elegido = icono == i
                    Surface(
                        onClick = { icono = i },
                        shape = RoundedCornerShape(14.dp),
                        color = if (elegido) colores.primaryContainer else colores.surface,
                        border = BorderStroke(if (elegido) 1.5.dp else 1.dp, if (elegido) colores.primary else colores.outline),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(Modifier.padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                            Icon(iconoServicio(i), contentDescription = i, tint = if (elegido) colores.primary else colores.onSurfaceVariant)
                        }
                    }
                }
            }

            Surface(shape = RoundedCornerShape(14.dp), color = colores.surface, border = BorderStroke(1.dp, colores.outline), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Servicio activo", style = MaterialTheme.typography.titleSmall)
                        Text("Los dueños pueden pedirlo", style = MaterialTheme.typography.bodySmall, color = colores.onSurfaceVariant)
                    }
                    Switch(checked = activo, onCheckedChange = { activo = it })
                }
            }

            VetButton("Guardar servicio", onClick = {
                vm.guardar(
                    Servicio(
                        id = original?.id ?: 0,
                        nombre = nombre,
                        descripcion = descripcion,
                        duracionMin = duracion,
                        precio = precio.trim().replace(',', '.').toDoubleOrNull() ?: 0.0,
                        activo = activo,
                        icono = icono
                    )
                )
            })
            VetButton("Cancelar", onClick = onVolver, variante = VarianteBoton.SECUNDARIO)
            if (original != null) {
                VetButton("Eliminar servicio", onClick = { vm.eliminar(original) }, variante = VarianteBoton.PELIGRO)
            }
        }
    }
}
