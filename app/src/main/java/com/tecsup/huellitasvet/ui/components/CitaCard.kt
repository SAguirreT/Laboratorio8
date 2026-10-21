package com.tecsup.huellitasvet.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.tecsup.huellitasvet.model.EstadoCita

/**
 * Tarjeta de una cita: ícono del servicio, título, subtítulo, estado y acciones opcionales
 * (por ejemplo el botón 'Cancelar').
 */
@Composable
fun CitaCard(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    estado: EstadoCita,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    acciones: @Composable RowScope.() -> Unit = {}
) {
    val colores = MaterialTheme.colorScheme
    val contenido: @Composable () -> Unit = {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .background(colores.primaryContainer, RoundedCornerShape(12.dp))
            ) {
                Icon(icono, contentDescription = null, tint = colores.primary)
            }
            Column(
                modifier = Modifier.weight(1f).padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(titulo, style = MaterialTheme.typography.titleSmall)
                Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = colores.onSurfaceVariant)
                Spacer(Modifier.height(2.dp))
                EstadoCitaChip(estado)
            }
            Row(verticalAlignment = Alignment.CenterVertically, content = acciones)
        }
    }
    val forma = RoundedCornerShape(16.dp)
    val borde = BorderStroke(1.dp, colores.outline)
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = forma, color = colores.surface, border = borde) { contenido() }
    } else {
        Surface(modifier = modifier.fillMaxWidth(), shape = forma, color = colores.surface, border = borde) { contenido() }
    }
}
