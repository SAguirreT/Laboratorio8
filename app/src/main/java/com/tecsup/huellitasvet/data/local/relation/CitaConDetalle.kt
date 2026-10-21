package com.tecsup.huellitasvet.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.tecsup.huellitasvet.data.local.entity.Cita
import com.tecsup.huellitasvet.data.local.entity.Mascota
import com.tecsup.huellitasvet.data.local.entity.Servicio
import com.tecsup.huellitasvet.model.EstadoCita
import com.tecsup.huellitasvet.model.Fechas
import java.time.LocalDateTime

/**
 * Relación anidada de dos niveles: cita -> mascota -> dueño, y además cita -> servicio.
 * entity = Mascota::class le dice a Room qué tabla consultar para armar MascotaConDueno.
 */
data class CitaConDetalle(
    @Embedded val cita: Cita,
    @Relation(entity = Mascota::class, parentColumn = "mascotaId", entityColumn = "id")
    val mascota: MascotaConDueno,
    @Relation(parentColumn = "servicioId", entityColumn = "id")
    val servicio: Servicio
)

// Ayudas fuera de la clase para que Room no las confunda con columnas.
val CitaConDetalle.estado: EstadoCita get() = EstadoCita.desde(cita.estado)
val CitaConDetalle.fechaHora: LocalDateTime get() = Fechas.fechaHora(cita.fecha, cita.hora)
