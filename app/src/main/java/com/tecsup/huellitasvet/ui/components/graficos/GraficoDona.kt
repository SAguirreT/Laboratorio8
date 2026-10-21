package com.tecsup.huellitasvet.ui.components.graficos

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Gráfico de dona dibujado con Canvas: cada dato es un arco proporcional a su valor. */
@Composable
fun GraficoDona(
    datos: List<DatoGrafico>,
    modifier: Modifier = Modifier,
    tamano: Dp = 120.dp,
    grosor: Dp = 18.dp,
    subtitulo: String = "citas"
) {
    val total = datos.sumOf { it.valor.toDouble() }.toFloat()
    val vacio = MaterialTheme.colorScheme.surfaceVariant
    val primario = MaterialTheme.colorScheme.primary
    Box(contentAlignment = Alignment.Center, modifier = modifier.size(tamano)) {
        Canvas(Modifier.fillMaxSize()) {
            val trazo = grosor.toPx()
            val diametro = size.minDimension - trazo
            val esquina = Offset((size.width - diametro) / 2, (size.height - diametro) / 2)
            val area = Size(diametro, diametro)
            if (total <= 0f) {
                drawArc(vacio, 0f, 360f, useCenter = false, topLeft = esquina, size = area, style = Stroke(trazo))
            } else {
                var inicio = -90f
                datos.forEach { d ->
                    val barrido = d.valor / total * 360f
                    val color = if (d.color == Color.Unspecified) primario else d.color
                    drawArc(color, inicio, barrido, useCenter = false, topLeft = esquina, size = area, style = Stroke(trazo))
                    inicio += barrido
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(total.toInt().toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(subtitulo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Leyenda para la dona: punto de color, etiqueta y valor. */
@Composable
fun LeyendaGrafico(datos: List<DatoGrafico>, modifier: Modifier = Modifier) {
    val primario = MaterialTheme.colorScheme.primary
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        datos.forEach { d ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(if (d.color == Color.Unspecified) primario else d.color, RoundedCornerShape(3.dp))
                )
                Text(
                    d.etiqueta,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp).weight(1f)
                )
                Text(d.valor.toInt().toString(), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
