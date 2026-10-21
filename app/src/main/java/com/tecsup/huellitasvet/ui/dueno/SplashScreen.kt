package com.tecsup.huellitasvet.ui.dueno

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tecsup.huellitasvet.model.Especie
import com.tecsup.huellitasvet.ui.components.MascotaAvatar
import com.tecsup.huellitasvet.ui.components.VetButton
import kotlinx.coroutines.delay

/**
 * Pantalla de inicio.
 * - Si ya hay una sesión guardada, salta sola a la pantalla principal (no muestra botones).
 * - Si no hay sesión, muestra 'Comenzar' y 'Ya tengo una cuenta', que llevan al login.
 */
@Composable
fun SplashScreen(
    cargando: Boolean,
    haySesion: Boolean,
    onContinuar: () -> Unit
) {
    LaunchedEffect(cargando, haySesion) {
        if (!cargando && haySesion) {
            delay(1000)
            onContinuar()
        }
    }
    val colores = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(colores.background)
            .padding(24.dp)
    ) {
        Spacer(Modifier.weight(1f))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(96.dp).background(colores.primary, RoundedCornerShape(26.dp))
        ) {
            Icon(Icons.Filled.Pets, contentDescription = null, tint = colores.onPrimary, modifier = Modifier.size(52.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("HuellitasVet", style = MaterialTheme.typography.headlineMedium)
        Text("Cuidamos a tu mejor amigo", color = colores.onSurfaceVariant)
        Spacer(Modifier.height(48.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(Especie.PERRO, Especie.GATO, Especie.OTRO).forEach { e ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(84.dp).background(colores.primaryContainer, RoundedCornerShape(20.dp))
                ) {
                    MascotaAvatar(e, tamano = 60.dp, fondo = Color.Transparent)
                }
            }
        }
        Spacer(Modifier.weight(1f))
        if (!cargando && !haySesion) {
            VetButton("Comenzar", onClick = onContinuar)
            TextButton(onClick = onContinuar, modifier = Modifier.padding(top = 4.dp)) {
                Text("Ya tengo una cuenta", color = colores.primary, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Spacer(Modifier.height(100.dp))
        }
    }
}
