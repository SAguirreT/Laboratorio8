package com.tecsup.huellitasvet.model

/** Fecha de la última atención de cada mascota (para la lista de Pacientes). */
data class UltimaVisita(
    val mascotaId: Int,
    val fecha: String
)
