package com.tecsup.huellitasvet.ui.dueno

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tecsup.huellitasvet.model.Rol
import com.tecsup.huellitasvet.ui.components.SelectorSegmentado
import com.tecsup.huellitasvet.ui.components.VetButton
import com.tecsup.huellitasvet.ui.components.VetTextField
import com.tecsup.huellitasvet.ui.theme.VetTheme
import com.tecsup.huellitasvet.viewmodel.SesionViewModel

/** Un solo Composable para ambos roles: el selector cambia el color del tema. */
@Composable
fun LoginScreen(vm: SesionViewModel, oscuro: Boolean) {
    var rol by rememberSaveable { mutableStateOf(Rol.CLIENTE) }
    var correo by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }
    val error by vm.error.collectAsState()

    VetTheme(rol = rol, oscuro = oscuro) {
        val colores = MaterialTheme.colorScheme
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colores.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Column {
                Text("Bienvenido", style = MaterialTheme.typography.headlineMedium)
                Text("Ingresa para continuar", color = colores.onSurfaceVariant)
            }
            SelectorSegmentado(
                opciones = listOf(Rol.CLIENTE.etiqueta, Rol.ADMIN.etiqueta),
                seleccion = rol.ordinal,
                onSeleccion = {
                    rol = Rol.entries[it]
                    vm.limpiarError()
                }
            )
            VetTextField(
                valor = correo, onCambio = { correo = it }, etiqueta = "Correo",
                icono = Icons.Outlined.Email, teclado = KeyboardType.Email
            )
            VetTextField(
                valor = clave, onCambio = { clave = it }, etiqueta = "Contraseña",
                icono = Icons.Outlined.Lock, esClave = true
            )
            Text(
                "¿Olvidaste tu contraseña?",
                color = colores.primary,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.align(Alignment.End)
            )
            if (error != null) {
                Text(error ?: "", color = colores.error, style = MaterialTheme.typography.bodySmall)
            }
            VetButton("Ingresar", onClick = { vm.login(correo, clave, rol) })

            if (rol == Rol.CLIENTE) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text("¿No tienes cuenta? ", style = MaterialTheme.typography.bodySmall)
                    Text("Regístrate", color = colores.primary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Surface(shape = RoundedCornerShape(14.dp), color = colores.primaryContainer) {
                    Column(Modifier.fillMaxWidth().padding(14.dp)) {
                        Text("Modo práctica", color = colores.primary, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Puedes cambiar de perfil desde Ajustes para probar ambos roles.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colores.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
