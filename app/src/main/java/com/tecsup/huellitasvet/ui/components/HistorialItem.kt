package com.tecsup.huellitasvet.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tecsup.huellitasvet.ui.theme.EstadoCancelada

/** Un punto de la línea de tiempo del historial clínico: fecha, servicio, diagnóstico y peso. */
@Composable
fun HistorialItem(
    fecha: String,
    servicio: String,
    diagnostico: String,
    peso: Double?,
    esUltimo: Boolean,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme
    Row(modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(28.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(22.dp).background(colores.primary, CircleShape)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = colores.onPrimary, modifier = Modifier.size(14.dp))
            }
            if (!esUltimo) {
                Box(Modifier.width(2.dp).fillMaxHeight().background(colores.primary.copy(alpha = 0.3f)))
            }
        }
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = colores.surface,
            border = BorderStroke(1.dp, colores.outline),
            modifier = Modifier.weight(1f).padding(start = 8.dp, bottom = 12.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(servicio, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    Text(fecha, style = MaterialTheme.typography.labelMedium, color = colores.primary)
                }
                Spacer(Modifier.height(4.dp))
                Text(diagnostico, style = MaterialTheme.typography.bodySmall, color = colores.onSurfaceVariant)
                if (peso != null) {
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Favorite, contentDescription = null, tint = EstadoCancelada, modifier = Modifier.size(14.dp))
                        Text(" Peso: $peso kg", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
