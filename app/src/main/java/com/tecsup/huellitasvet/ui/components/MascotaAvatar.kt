package com.tecsup.huellitasvet.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tecsup.huellitasvet.model.Especie

/** Dibuja con Canvas la carita de un perro, un gato u otro animal dentro de un círculo. */
@Composable
fun MascotaAvatar(
    especie: Especie,
    modifier: Modifier = Modifier,
    tamano: Dp = 56.dp,
    fondo: Color = MaterialTheme.colorScheme.primaryContainer
) {
    val huella = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier.size(tamano)) {
        drawCircle(color = fondo)
        when (especie) {
            Especie.PERRO -> dibujarPerro()
            Especie.GATO -> dibujarGato()
            Especie.OTRO -> dibujarHuella(huella)
        }
    }
}

/** Versión que recibe el texto guardado en la tabla (PERRO, GATO, OTRO). */
@Composable
fun MascotaAvatar(
    especie: String,
    modifier: Modifier = Modifier,
    tamano: Dp = 56.dp
) = MascotaAvatar(Especie.desde(especie), modifier, tamano)

private fun DrawScope.dibujarPerro() {
    val s = size.minDimension
    val c = center
    val pelo = Color(0xFFC68642)
    val oreja = Color(0xFF8B5A2B)
    val hocico = Color(0xFFF3D9B1)
    val oscuro = Color(0xFF2D1B0E)
    // orejas caídas
    drawOval(oreja, topLeft = Offset(c.x - s * 0.36f, c.y - s * 0.22f), size = Size(s * 0.17f, s * 0.36f))
    drawOval(oreja, topLeft = Offset(c.x + s * 0.19f, c.y - s * 0.22f), size = Size(s * 0.17f, s * 0.36f))
    // cabeza
    drawCircle(pelo, radius = s * 0.26f, center = Offset(c.x, c.y + s * 0.02f))
    // hocico
    drawOval(hocico, topLeft = Offset(c.x - s * 0.14f, c.y + s * 0.03f), size = Size(s * 0.28f, s * 0.19f))
    // ojos
    drawCircle(oscuro, radius = s * 0.032f, center = Offset(c.x - s * 0.1f, c.y - s * 0.05f))
    drawCircle(oscuro, radius = s * 0.032f, center = Offset(c.x + s * 0.1f, c.y - s * 0.05f))
    // nariz
    drawOval(oscuro, topLeft = Offset(c.x - s * 0.05f, c.y + s * 0.05f), size = Size(s * 0.1f, s * 0.065f))
    // lengua
    drawCircle(Color(0xFFF472B6), radius = s * 0.035f, center = Offset(c.x, c.y + s * 0.18f))
}

private fun DrawScope.dibujarGato() {
    val s = size.minDimension
    val c = center
    val pelo = Color(0xFF9CA3AF)
    val rosado = Color(0xFFF9A8D4)
    val oscuro = Color(0xFF1F2937)
    // orejas en triángulo
    val orejaIzq = Path().apply {
        moveTo(c.x - s * 0.27f, c.y - s * 0.04f)
        lineTo(c.x - s * 0.22f, c.y - s * 0.34f)
        lineTo(c.x - s * 0.03f, c.y - s * 0.2f)
        close()
    }
    val orejaDer = Path().apply {
        moveTo(c.x + s * 0.27f, c.y - s * 0.04f)
        lineTo(c.x + s * 0.22f, c.y - s * 0.34f)
        lineTo(c.x + s * 0.03f, c.y - s * 0.2f)
        close()
    }
    drawPath(orejaIzq, pelo)
    drawPath(orejaDer, pelo)
    // cabeza
    drawCircle(pelo, radius = s * 0.26f, center = Offset(c.x, c.y + s * 0.04f))
    // ojos verdes con pupila
    drawOval(Color(0xFF84CC16), topLeft = Offset(c.x - s * 0.15f, c.y - s * 0.04f), size = Size(s * 0.09f, s * 0.1f))
    drawOval(Color(0xFF84CC16), topLeft = Offset(c.x + s * 0.06f, c.y - s * 0.04f), size = Size(s * 0.09f, s * 0.1f))
    drawOval(oscuro, topLeft = Offset(c.x - s * 0.115f, c.y - s * 0.035f), size = Size(s * 0.025f, s * 0.09f))
    drawOval(oscuro, topLeft = Offset(c.x + s * 0.095f, c.y - s * 0.035f), size = Size(s * 0.025f, s * 0.09f))
    // nariz
    val nariz = Path().apply {
        moveTo(c.x - s * 0.04f, c.y + s * 0.09f)
        lineTo(c.x + s * 0.04f, c.y + s * 0.09f)
        lineTo(c.x, c.y + s * 0.14f)
        close()
    }
    drawPath(nariz, rosado)
    // bigotes
    val grosor = s * 0.012f
    listOf(-1f, 1f).forEach { lado ->
        drawLine(oscuro, Offset(c.x + lado * s * 0.07f, c.y + s * 0.13f), Offset(c.x + lado * s * 0.3f, c.y + s * 0.09f), grosor, StrokeCap.Round)
        drawLine(oscuro, Offset(c.x + lado * s * 0.07f, c.y + s * 0.16f), Offset(c.x + lado * s * 0.3f, c.y + s * 0.18f), grosor, StrokeCap.Round)
    }
}

private fun DrawScope.dibujarHuella(color: Color) {
    val s = size.minDimension
    val c = center
    drawOval(color, topLeft = Offset(c.x - s * 0.15f, c.y - s * 0.01f), size = Size(s * 0.3f, s * 0.24f))
    drawOval(color, topLeft = Offset(c.x - s * 0.25f, c.y - s * 0.14f), size = Size(s * 0.11f, s * 0.14f))
    drawOval(color, topLeft = Offset(c.x - s * 0.12f, c.y - s * 0.27f), size = Size(s * 0.11f, s * 0.15f))
    drawOval(color, topLeft = Offset(c.x + s * 0.01f, c.y - s * 0.27f), size = Size(s * 0.11f, s * 0.15f))
    drawOval(color, topLeft = Offset(c.x + s * 0.14f, c.y - s * 0.14f), size = Size(s * 0.11f, s * 0.14f))
}
