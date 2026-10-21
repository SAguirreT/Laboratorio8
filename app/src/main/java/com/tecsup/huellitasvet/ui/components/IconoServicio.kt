package com.tecsup.huellitasvet.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.graphics.vector.ImageVector

/** Convierte el texto guardado en servicios.icono (heart, syringe, scissors) en un ícono. */
fun iconoServicio(icono: String): ImageVector = when (icono) {
    "syringe" -> Icons.Filled.Colorize
    "scissors" -> Icons.Filled.ContentCut
    else -> Icons.Filled.Favorite
}

val ICONOS_SERVICIO = listOf("heart", "syringe", "scissors")
