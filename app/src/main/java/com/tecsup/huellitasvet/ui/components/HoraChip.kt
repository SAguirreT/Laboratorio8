package com.tecsup.huellitasvet.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

/**
 * Horario de la pantalla Pedir cita.
 * Si está 'ocupado' se ve gris, tachado y no se puede tocar.
 */
@Composable
fun HoraChip(
    hora: String,
    seleccionado: Boolean,
    ocupado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme
    val fondo = when {
        ocupado -> colores.surfaceVariant
        seleccionado -> colores.primary
        else -> colores.surface
    }
    val texto = when {
        ocupado -> colores.onSurfaceVariant.copy(alpha = 0.6f)
        seleccionado -> colores.onPrimary
        else -> colores.onSurface
    }
    Surface(
        onClick = onClick,
        enabled = !ocupado,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = fondo,
        border = if (seleccionado || ocupado) null else BorderStroke(1.dp, colores.outline)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
            Text(
                hora,
                color = texto,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
                textDecoration = if (ocupado) TextDecoration.LineThrough else null
            )
        }
    }
}
