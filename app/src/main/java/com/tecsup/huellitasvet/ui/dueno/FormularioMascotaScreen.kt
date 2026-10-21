package com.tecsup.huellitasvet.ui.dueno

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import com.tecsup.huellitasvet.model.Especie
import com.tecsup.huellitasvet.model.Sexo
import com.tecsup.huellitasvet.ui.components.AppTopBar
import com.tecsup.huellitasvet.ui.components.MascotaAvatar
import com.tecsup.huellitasvet.ui.components.OpcionChip
import com.tecsup.huellitasvet.ui.components.VarianteBoton
import com.tecsup.huellitasvet.ui.components.VetButton
import com.tecsup.huellitasvet.ui.components.VetTextField
import com.tecsup.huellitasvet.viewmodel.MascotaViewModel

/** Sirve para crear (esEdicion = false) y para editar una mascota. */
@Composable
fun FormularioMascotaScreen(
    vm: MascotaViewModel,
    esEdicion: Boolean,
    onVolver: () -> Unit,
    onEliminada: () -> Unit
) {
    val mascota by vm.mascota.collectAsState()
    val errores by vm.errores.collectAsState()
    val guardado by vm.guardado.collectAsState()
    val eliminado by vm.eliminado.collectAsState()
    val mensaje by vm.mensaje.collectAsState()
    val contexto = LocalContext.current

    var nombre by rememberSaveable { mutableStateOf("") }
    var especie by rememberSaveable { mutableStateOf<Especie?>(null) }
    var raza by rememberSaveable { mutableStateOf("") }
    var anio by rememberSaveable { mutableStateOf("") }
    var peso by rememberSaveable { mutableStateOf("") }
    var sexo by rememberSaveable { mutableStateOf<Sexo?>(null) }
    var precargado by rememberSaveable { mutableStateOf(false) }

    // Al editar, llena los campos una sola vez cuando llega la mascota
    LaunchedEffect(mascota) {
        val m = mascota
        if (esEdicion && m != null && !precargado) {
            nombre = m.nombre
            especie = Especie.desde(m.especie)
            raza = m.raza
            anio = m.anioNacimiento.toString()
            peso = m.peso.toString()
            sexo = Sexo.desde(m.sexo)
            precargado = true
        }
    }
    LaunchedEffect(guardado) { if (guardado) onVolver() }
    LaunchedEffect(eliminado) { if (eliminado) onEliminada() }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(contexto, it, Toast.LENGTH_LONG).show()
            vm.limpiarMensaje()
        }
    }

    Column(Modifier.fillMaxSize()) {
        AppTopBar(if (esEdicion) "Editar mascota" else "Nueva mascota", onVolver = onVolver)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                MascotaAvatar(especie ?: Especie.OTRO, tamano = 88.dp)
            }
            VetTextField(nombre, { nombre = it }, "Nombre", error = errores.nombre)

            Etiqueta("Especie", errores.especie)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Especie.entries.forEach { e ->
                    OpcionChip(e.etiqueta, especie == e, { especie = e }, Modifier.weight(1f))
                }
            }

            VetTextField(raza, { raza = it }, "Raza")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                VetTextField(
                    anio, { anio = it.filter(Char::isDigit).take(4) }, "Año de nacimiento",
                    error = errores.anio, teclado = KeyboardType.Number, modifier = Modifier.weight(1f)
                )
                VetTextField(
                    peso, { peso = it }, "Peso (kg)",
                    error = errores.peso, teclado = KeyboardType.Decimal, modifier = Modifier.weight(1f)
                )
            }

            Etiqueta("Sexo", errores.sexo)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Sexo.entries.forEach { s ->
                    OpcionChip(s.etiqueta, sexo == s, { sexo = s }, Modifier.weight(1f))
                }
            }

            VetButton("Guardar mascota", onClick = { vm.guardar(nombre, especie, raza, anio, peso, sexo) })
            VetButton("Cancelar", onClick = onVolver, variante = VarianteBoton.SECUNDARIO)
            val m = mascota
            if (esEdicion && m != null) {
                VetButton("Eliminar mascota", onClick = { vm.eliminar(m) }, variante = VarianteBoton.PELIGRO)
            }
        }
    }
}

@Composable
private fun Etiqueta(texto: String, error: String?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(texto, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}
