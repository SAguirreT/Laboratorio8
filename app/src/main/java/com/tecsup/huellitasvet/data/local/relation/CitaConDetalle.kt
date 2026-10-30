package com.tecsup.huellitasvet.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.tecsup.huellitasvet.data.local.entity.Cita
import com.tecsup.huellitasvet.data.local.entity.Mascota
import com.tecsup.huellitasvet.data.local.entity.Servicio
import com.tecsup.huellitasvet.model.EstadoCita
import com.tecsup.huellitasvet.model.Fechas
import java.time.LocalDateTime

/*
 * COMMIT 3 - feat(data): agrega DAOs y relaciones CitaConDetalle y MascotaConDueno
 * Rama: main
 * Hash: 4d9158f
 *
 * ¿QUÉ SE REALIZÓ?
 * Se creó la relación anidada CitaConDetalle relacionando Cita -> MascotaConDueno (Mascota + Usuario) y Cita -> Servicio.
 *
 * ¿POR QUÉ?
 * Para obtener en una sola consulta relacional de Room todos los datos necesarios para renderizar tarjetas de citas y la agenda.
 *
 * ARCHIVOS RELACIONADOS DEL MISMO COMMIT:
 * - UsuarioDao.kt
 * - MascotaDao.kt
 * - CitaDao.kt
 * - MascotaConDueno.kt
 * - CitaConDetalle.kt
 * - UltimaVisita.kt
 *
 * RELACIÓN:
 * Es el modelo de relación de 2 niveles utilizado ampliamente por CitaDao y consumido por los ViewModels.
 */
data class CitaConDetalle(
    @Embedded val cita: Cita,
    @Relation(entity = Mascota::class, parentColumn = "mascotaId", entityColumn = "id")
    val mascota: MascotaConDueno,
    @Relation(parentColumn = "servicioId", entityColumn = "id")
    val servicio: Servicio
)

val CitaConDetalle.estado: EstadoCita get() = EstadoCita.desde(cita.estado)
val CitaConDetalle.fechaHora: LocalDateTime get() = Fechas.fechaHora(cita.fecha, cita.hora)
