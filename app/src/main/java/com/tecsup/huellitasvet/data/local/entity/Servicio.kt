package com.tecsup.huellitasvet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa la tabla 'servicios' ofrecidos por la veterinaria.
 * Define la lista de prestaciones disponibles (consultas, vacunas, baños, etc.)
 * con sus precios, duraciones y estado de disponibilidad.
 */
@Entity(tableName = "servicios")
data class Servicio(
    /** Identificador único autogenerado del servicio. */
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    /** Nombre comercial del servicio clínico. */
    val nombre: String,
    /** Explicación detallada de lo que incluye la atención. */
    val descripcion: String,
    /** Estimación de tiempo requerido en minutos (15, 30, 45, 60 min). */
    val duracionMin: Int,
    /** Tarifa del servicio en soles (S/). */
    val precio: Double,
    /** Flag para deshabilitar servicios sin eliminarlos físicamente si tienen historial. */
    val activo: Boolean = true,
    /** Clave del ícono vectorial asociado ("heart", "syringe", "scissors"). */
    val icono: String
)
