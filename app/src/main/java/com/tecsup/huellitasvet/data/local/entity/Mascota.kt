package com.tecsup.huellitasvet.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** N mascotas -> 1 dueño. especie y sexo guardan el enum.name. */
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
    val especie: String, // PERRO | GATO | OTRO
    val raza: String,
    val anioNacimiento: Int,
    val peso: Double,
    val sexo: String // MACHO | HEMBRA
)
