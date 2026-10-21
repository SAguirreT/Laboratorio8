package com.tecsup.huellitasvet.ui.components.graficos

import androidx.compose.ui.graphics.Color

/**
 * Un valor para los gráficos. Si el color es Unspecified, el gráfico usa el color primario del tema.
 */
data class DatoGrafico(
    val etiqueta: String,
    val valor: Float,
    val color: Color = Color.Unspecified
)
