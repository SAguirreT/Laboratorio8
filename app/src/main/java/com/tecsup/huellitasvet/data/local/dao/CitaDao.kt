package com.tecsup.huellitasvet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.tecsup.huellitasvet.data.local.entity.Cita
import com.tecsup.huellitasvet.data.local.relation.CitaConDetalle
import com.tecsup.huellitasvet.model.ConteoServicio
import com.tecsup.huellitasvet.model.UltimaVisita
import kotlinx.coroutines.flow.Flow

/*
 * COMMIT 3 - feat(data): agrega DAOs y relaciones CitaConDetalle y MascotaConDueno
 * Rama: main
 * Hash: 4d9158f
 *
 * ¿QUÉ SE REALIZÓ?
 * Se construyó CitaDao con métodos de agendamiento, actualización de atención, consultas de disponibilidad de horarios
 * y agregaciones estadísticas SQL para el dashboard del veterinario.
 *
 * ¿POR QUÉ?
 * Para soportar el flujo médico completo de citas (agendamiento, verificación de cupos, atenciones y cálculo de ingresos).
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
 * CitaDao ejecuta consultas `@Transaction` uniendo Cita, Mascota, Usuario y Servicio mediante `CitaConDetalle`.
 */
@Dao
interface CitaDao {

    // ---------- Escritura ----------
    @Insert
    suspend fun insertar(c: Cita): Long

    @Insert
    suspend fun insertarTodos(lista: List<Cita>)

    @Query("UPDATE citas SET estado = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: Int, estado: String)

    @Query(
        "UPDATE citas SET estado = 'ATENDIDA', diagnostico = :diagnostico, " +
            "tratamiento = :tratamiento, pesoRegistrado = :peso WHERE id = :id"
    )
    suspend fun registrarAtencion(id: Int, diagnostico: String, tratamiento: String, peso: Double)

    @Query("SELECT * FROM citas WHERE id = :id")
    suspend fun obtener(id: Int): Cita?

    // ---------- Horarios ----------
    @Query("SELECT COUNT(*) FROM citas WHERE fecha = :fecha AND hora = :hora AND estado != 'CANCELADA'")
    suspend fun contarOcupadas(fecha: String, hora: String): Int

    @Query("SELECT hora FROM citas WHERE fecha = :fecha AND estado != 'CANCELADA'")
    fun observarHorasOcupadas(fecha: String): Flow<List<String>>

    // ---------- Lectura con relaciones ----------
    @Transaction
    @Query(
        "SELECT c.* FROM citas c INNER JOIN mascotas m ON c.mascotaId = m.id " +
            "WHERE m.duenoId = :duenoId ORDER BY c.fecha, c.hora"
    )
    fun observarPorDueno(duenoId: Int): Flow<List<CitaConDetalle>>

    @Transaction
    @Query("SELECT * FROM citas WHERE fecha = :fecha ORDER BY hora")
    fun observarPorFecha(fecha: String): Flow<List<CitaConDetalle>>

    @Transaction
    @Query("SELECT * FROM citas WHERE id = :id")
    fun observarUna(id: Int): Flow<CitaConDetalle?>

    @Transaction
    @Query("SELECT * FROM citas WHERE mascotaId = :mascotaId AND estado = 'ATENDIDA' ORDER BY fecha DESC, hora DESC")
    fun observarHistorial(mascotaId: Int): Flow<List<CitaConDetalle>>

    // ---------- Panel ----------
    @Query("SELECT COUNT(*) FROM citas WHERE fecha = :fecha AND estado != 'CANCELADA'")
    fun contarPorFecha(fecha: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM citas WHERE estado = :estado")
    fun contarPorEstado(estado: String): Flow<Int>

    @Query(
        "SELECT s.nombre AS nombre, COUNT(c.id) AS cantidad FROM citas c " +
            "INNER JOIN servicios s ON c.servicioId = s.id " +
            "WHERE c.estado != 'CANCELADA' GROUP BY s.id, s.nombre ORDER BY cantidad DESC"
    )
    fun contarPorServicio(): Flow<List<ConteoServicio>>

    @Query("SELECT COALESCE(SUM(precio), 0.0) FROM citas WHERE estado = 'ATENDIDA' AND fecha >= :fechaInicio")
    fun ingresosDesde(fechaInicio: String): Flow<Double>

    @Query("SELECT COUNT(*) FROM citas WHERE servicioId = :servicioId")
    suspend fun contarDeServicio(servicioId: Int): Int

    @Query("SELECT mascotaId, MAX(fecha) AS fecha FROM citas WHERE estado = 'ATENDIDA' GROUP BY mascotaId")
    fun observarUltimasVisitas(): Flow<List<UltimaVisita>>
}
