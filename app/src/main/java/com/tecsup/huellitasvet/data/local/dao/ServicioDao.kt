package com.tecsup.huellitasvet.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tecsup.huellitasvet.data.local.entity.Servicio
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para la entidad Servicio.
 * Las lecturas retornan `Flow` para notificar cambios en la base de datos a los ViewModels en vivo.
 * Las escrituras usan `suspend` para ser ejecutadas de forma asíncrona fuera del hilo principal UI.
 */
@Dao
interface ServicioDao {
    /** Observa todos los servicios registrados (activos e inactivos), ordenados por nombre. */
    @Query("SELECT * FROM servicios ORDER BY nombre")
    fun observarTodos(): Flow<List<Servicio>>

    /** Observa únicamente los servicios en estado activo (`activo = 1`) disponibles para que el dueño solicite citas. */
    @Query("SELECT * FROM servicios WHERE activo = 1 ORDER BY nombre")
    fun observarActivos(): Flow<List<Servicio>>

    /** Obtiene los detalles de un servicio por su ID. */
    @Query("SELECT * FROM servicios WHERE id = :id")
    suspend fun obtener(id: Int): Servicio?

    /** Cuenta la cantidad de servicios existentes para verificar si se deben insertar datos iniciales. */
    @Query("SELECT COUNT(*) FROM servicios")
    suspend fun contar(): Int

    /** Inserta o reemplaza un servicio si ya existe conflicto con la clave primaria. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(s: Servicio): Long

    /** Inserta la lista predeterminada de servicios en la siembra de la base de datos. */
    @Insert
    suspend fun insertarTodos(lista: List<Servicio>): List<Long>

    /** Actualiza las propiedades de un servicio existente. */
    @Update
    suspend fun actualizar(s: Servicio)

    /** Elimina físicamente el servicio de la base de datos. */
    @Delete
    suspend fun eliminar(s: Servicio)

    /** Desactiva o activa un servicio sin necesidad de borrar sus registros e historial. */
    @Query("UPDATE servicios SET activo = :activo WHERE id = :id")
    suspend fun cambiarActivo(id: Int, activo: Boolean)
}
