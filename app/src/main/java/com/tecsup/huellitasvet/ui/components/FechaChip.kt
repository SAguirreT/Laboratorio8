package com.tecsup.huellitasvet.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tecsup.huellitasvet.model.Fechas
import java.time.LocalDate

/** Selector de día para Pedir cita: muestra el día de la semana y el número. */
@Composable
fun FechaChip(
    fecha: LocalDate,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier.width(58.dp),
        shape = RoundedCornerShape(14.dp),
        color = if (seleccionado) colores.primary else colores.surface,
        contentColor = if (seleccionado) colores.onPrimary else colores.onSurface,
        border = if (seleccionado) null else BorderStroke(1.dp, colores.outline)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            Text(Fechas.diaCorto(fecha), style = MaterialTheme.typography.labelSmall)
            Text(
                fecha.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
