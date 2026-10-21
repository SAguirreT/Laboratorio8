package com.tecsup.huellitasvet.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class VarianteBoton { PRIMARIO, SECUNDARIO, PELIGRO }

/** Botón principal de la app: ancho completo, esquinas redondeadas y color del rol. */
@Composable
fun VetButton(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variante: VarianteBoton = VarianteBoton.PRIMARIO,
    icono: ImageVector? = null
) {
    val forma = RoundedCornerShape(14.dp)
    val mod = modifier.fillMaxWidth().height(52.dp)
    val contenido: @Composable () -> Unit = {
        if (icono != null) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(texto, style = MaterialTheme.typography.labelLarge)
    }
    when (variante) {
        VarianteBoton.PRIMARIO -> Button(
            onClick = onClick, modifier = mod, enabled = enabled, shape = forma
        ) { contenido() }

        VarianteBoton.SECUNDARIO -> OutlinedButton(
            onClick = onClick, modifier = mod, enabled = enabled, shape = forma,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) { contenido() }

        VarianteBoton.PELIGRO -> OutlinedButton(
            onClick = onClick, modifier = mod, enabled = enabled, shape = forma,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) { contenido() }
    }
}
