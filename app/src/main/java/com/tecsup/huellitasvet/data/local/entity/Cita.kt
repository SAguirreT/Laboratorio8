package com.tecsup.huellitasvet.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * N citas -> 1 mascota y N citas -> 1 servicio.
 * fecha 'aaaa-MM-dd' y hora 'HH:mm' son texto: se ordenan bien y se comparan con = en SQL.
 * precio es una 'foto' del precio del servicio al momento de pedir la cita.
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
        Index(value = ["fecha", "hora"]) // acelera la consulta de horarios ocupados
    ]
)
data class Cita(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mascotaId: Int,
    val servicioId: Int,
    val fecha: String,
    val hora: String,
    val nota: String = "",
    val estado: String, // PENDIENTE | CONFIRMADA | ATENDIDA | CANCELADA
    val precio: Double,
    val diagnostico: String? = null,
    val tratamiento: String? = null,
    val pesoRegistrado: Double? = null,
    val fechaCreacion: Long = System.currentTimeMillis()
)
