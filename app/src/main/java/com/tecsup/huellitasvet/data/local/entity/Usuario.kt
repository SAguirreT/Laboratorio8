package com.tecsup.huellitasvet.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/*
 * COMMIT 2 - feat(data): agrega entidades Usuario, Mascota y Cita
 * Rama: main
 * Hash: 9a390f3
 *
 * ¿QUÉ SE REALIZÓ?
 * Se definió la entidad Usuario para representar la tabla 'usuarios' en Room,
 * modelando las cuentas de clientes (dueños) y administradores (veterinarios).
 *
 * ¿POR QUÉ?
 * Para autenticar a los usuarios en la app localmente y asociar las mascotas a sus dueños.
 *
 * ARCHIVOS RELACIONADOS DEL MISMO COMMIT:
 * - Usuario.kt
 * - Mascota.kt
 * - Cita.kt
 *
 * RELACIÓN:
 * Usuario es la entidad padre de Mascota (1 a N).
 */
@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["correo"], unique = true)]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val correo: String,
    val clave: String,
    val rol: String
)
