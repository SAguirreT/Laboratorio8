package com.tecsup.huellitasvet.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/*
 * COMMIT 2 - feat(data): agrega entidades Usuario, Mascota y Cita
 * Rama: main
 * Hash: 9a390f3
 *
 * ¿QUÉ SE REALIZÓ?
 * Se creó la entidad Cita que enlaza una Mascota con un Servicio mediante claves foráneas.
 *
 * ¿POR QUÉ?
 * Para gestionar las reservas médicas, estados (PENDIENTE, CONFIRMADA, ATENDIDA, CANCELADA) y registros clínicos.
 *
 * ARCHIVOS RELACIONADOS DEL MISMO COMMIT:
 * - Usuario.kt
 * - Mascota.kt
 * - Cita.kt
 *
 * RELACIÓN:
 * Cita une Mascota y Servicio, registrando la fecha, hora, diagnóstico y precio en el momento del agendamiento.
 */
@Entity(
    tableName = "citas",
    foreignKeys = [
        ForeignKey(entity = Mascota::class, parentColumns = ["id"], childColumns = ["mascotaId"]),
        ForeignKey(entity = Servicio::class, parentColumns = ["id"], childColumns = ["servicioId"])
    ],
    indices = [
        Index("mascotaId"),
        Index("servicioId"),
        Index(value = ["fecha", "hora"])
    ]
)
data class Cita(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mascotaId: Int,
    val servicioId: Int,
    val fecha: String,
    val hora: String,
    val nota: String = "",
    val estado: String,
    val precio: Double,
    val diagnostico: String? = null,
    val tratamiento: String? = null,
    val pesoRegistrado: Double? = null,
    val fechaCreacion: Long = System.currentTimeMillis()
)
