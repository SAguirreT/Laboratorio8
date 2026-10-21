package com.tecsup.huellitasvet.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.tecsup.huellitasvet.ui.theme.AmbarSuave
import com.tecsup.huellitasvet.ui.theme.EstadoPendiente

private val TextoAmbar = Color(0xFF92400E)

/** Tarjeta ámbar para recordatorios y alertas. Puede llevar una acción a la derecha. */
@Composable
fun Aviso(
    icono: ImageVector,
    titulo: String,
    detalle: String? = null,
    modifier: Modifier = Modifier,
    accion: @Composable RowScope.() -> Unit = {}
) {
    Surface(shape = RoundedCornerShape(14.dp), color = AmbarSuave, modifier = modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, contentDescription = null, tint = EstadoPendiente)
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(titulo, style = MaterialTheme.typography.titleSmall, color = TextoAmbar)
                if (detalle != null) {
                    Text(detalle, style = MaterialTheme.typography.bodySmall, color = TextoAmbar)
                }
            }
            accion()
        }
    }
}
