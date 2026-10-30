package com.tecsup.huellitasvet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tecsup.huellitasvet.data.local.entity.Usuario

/*
 * COMMIT 3 - feat(data): agrega DAOs y relaciones CitaConDetalle y MascotaConDueno
 * Rama: main
 * Hash: 4d9158f
 *
 * ¿QUÉ SE REALIZÓ?
 * Se creó UsuarioDao para abstraer las consultas SQL de autenticación y consulta de usuarios.
 *
 * ¿POR QUÉ?
 * Para verificar las credenciales de correo/clave y recuperar la sesión de usuario en Room.
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
 * UsuarioDao provee el acceso a la tabla usuarios consultada en el proceso de Login.
 */
@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios WHERE correo = :correo AND clave = :clave LIMIT 1")
    suspend fun login(correo: String, clave: String): Usuario?

    @Insert
    suspend fun insertarTodos(lista: List<Usuario>): List<Long>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun obtener(id: Int): Usuario?

    @Query("SELECT * FROM usuarios WHERE rol = :rol ORDER BY id LIMIT 1")
    suspend fun primeroConRol(rol: String): Usuario?
}
