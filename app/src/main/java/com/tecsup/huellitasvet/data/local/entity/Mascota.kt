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
 * Se definió la entidad Mascota con su clave foránea relacionándola con la entidad Usuario (duenoId).
 *
 * ¿POR QUÉ?
 * Para permitir que cada dueño tenga múltiples mascotas registradas y ligadas a su cuenta.
 *
 * ARCHIVOS RELACIONADOS DEL MISMO COMMIT:
 * - Usuario.kt
 * - Mascota.kt
 * - Cita.kt
 *
 * RELACIÓN:
 * Mascota pertenece a un Usuario y puede tener asociadas varias Citas clínicas.
 */
@Entity(
    tableName = "mascotas",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["duenoId"]
        )
    ],
    indices = [Index("duenoId")]
)
data class Mascota(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val duenoId: Int,
    val nombre: String,
    val especie: String,
    val raza: String,
    val anioNacimiento: Int,
    val peso: Double,
    val sexo: String
)
