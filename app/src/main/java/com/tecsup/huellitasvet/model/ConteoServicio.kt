package com.tecsup.huellitasvet.model

/** Resultado de la consulta GROUP BY del panel: cuántas citas tiene cada servicio. */
data class ConteoServicio(
    val nombre: String,
    val cantidad: Int
)
