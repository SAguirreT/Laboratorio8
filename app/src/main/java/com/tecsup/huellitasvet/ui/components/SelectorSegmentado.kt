package com.tecsup.huellitasvet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/** Pestañas tipo 'píldora' (Dueño/Veterinario, Próximas/Historial, Datos/Historial clínico). */
@Composable
fun SelectorSegmentado(
    opciones: List<String>,
    seleccion: Int,
    onSeleccion: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colores.surfaceVariant)
            .padding(4.dp)
    ) {
        opciones.forEachIndexed { i, texto ->
            val activo = i == seleccion
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (activo) colores.primary else colores.surfaceVariant)
                    .clickable { onSeleccion(i) }
                    .padding(vertical = 10.dp)
            ) {
                Text(
                    texto,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (activo) colores.onPrimary else colores.onSurfaceVariant
                )
            }
        }
    }
}
