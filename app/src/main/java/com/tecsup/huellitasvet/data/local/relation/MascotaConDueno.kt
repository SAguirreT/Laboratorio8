package com.tecsup.huellitasvet.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.tecsup.huellitasvet.data.local.entity.Mascota
import com.tecsup.huellitasvet.data.local.entity.Usuario

/*
 * COMMIT 3 - feat(data): agrega DAOs y relaciones CitaConDetalle y MascotaConDueno
 * Rama: main
 * Hash: 4d9158f
 *
 * ¿QUÉ SE REALIZÓ?
 * Se definió la clase POJO MascotaConDueno combinando la entidad Mascota con su Usuario dueño mediante `@Relation`.
 *
 * ¿POR QUÉ?
 * Para permitir que las pantallas del veterinario y los reportes muestren el nombre del dueño junto a los datos de la mascota.
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
 * Estructura de relación de 1 nivel (Mascota + Usuario) consumida por MascotaDao.
 */
data class MascotaConDueno(
    @Embedded val mascota: Mascota,
    @Relation(parentColumn = "duenoId", entityColumn = "id")
    val dueno: Usuario
)
