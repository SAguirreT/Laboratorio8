package com.tecsup.huellitasvet.ui.components.graficos

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Gráfico de líneas dibujado con Canvas: une los puntos en orden y marca cada valor con un círculo.
 * Las etiquetas (fechas) van debajo y los valores encima de la línea.
 */
@Composable
fun GraficoLineas(
    datos: List<DatoGrafico>,
    modifier: Modifier = Modifier,
    alto: Dp = 140.dp,
    unidad: String = ""
) {
    val primario = MaterialTheme.colorScheme.primary
    val rejilla = MaterialTheme.colorScheme.outline
    val superficie = MaterialTheme.colorScheme.surface
    if (datos.isEmpty()) return

    val minimo = datos.minOf { it.valor }
    val maximo = datos.maxOf { it.valor }
    val rango = (maximo - minimo).takeIf { it > 0f } ?: 1f

    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            datos.forEach {
                Text(
                    "%.1f%s".format(it.valor, unidad),
                    style = MaterialTheme.typography.labelSmall,
                    color = primario,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Canvas(Modifier.fillMaxWidth().height(alto).padding(vertical = 10.dp)) {
            val ranura = size.width / datos.size
            // 3 líneas guía horizontales
            repeat(3) { i ->
                val y = size.height * i / 2f
                drawLine(rejilla, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            }
            // Si todos los pesos son iguales, la línea queda al centro
            val puntos = datos.mapIndexed { i, d ->
                val x = ranura * i + ranura / 2
                val y = if (maximo == minimo) size.height / 2 else size.height - (d.valor - minimo) / rango * size.height
                Offset(x, y)
            }
            val linea = Path().apply {
                puntos.forEachIndexed { i, p -> if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) }
            }
            drawPath(linea, primario, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            puntos.forEach { p ->
                drawCircle(primario, radius = 6.dp.toPx(), center = p)
                drawCircle(superficie, radius = 3.dp.toPx(), center = p)
            }
        }
        Row(Modifier.fillMaxWidth()) {
            datos.forEach {
                Text(
                    it.etiqueta,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
