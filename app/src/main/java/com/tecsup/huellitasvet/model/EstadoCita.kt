package com.tecsup.huellitasvet.model

/**
 * Estados de una cita.
 * PENDIENTE -> CONFIRMADA -> ATENDIDA (solo registrando la atención).
 * Desde PENDIENTE o CONFIRMADA se puede pasar a CANCELADA.
 * ATENDIDA y CANCELADA son estados finales.
 */
enum class EstadoCita(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    ATENDIDA("Atendida"),
    CANCELADA("Cancelada");

    val esActiva: Boolean get() = this == PENDIENTE || this == CONFIRMADA

    companion object {
        fun desde(texto: String?): EstadoCita = entries.firstOrNull { it.name == texto } ?: PENDIENTE
    }
}
