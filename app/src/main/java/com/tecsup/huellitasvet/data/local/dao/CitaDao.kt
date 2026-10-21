package com.tecsup.huellitasvet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.tecsup.huellitasvet.data.local.entity.Cita
import com.tecsup.huellitasvet.data.local.relation.CitaConDetalle
import com.tecsup.huellitasvet.model.ConteoServicio
import com.tecsup.huellitasvet.model.PuntoPeso
import com.tecsup.huellitasvet.model.UltimaVisita
import kotlinx.coroutines.flow.Flow

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

    /** Mejora IA: cambia fecha y hora de una cita. */
    @Query("UPDATE citas SET fecha = :fecha, hora = :hora WHERE id = :id")
    suspend fun reprogramar(id: Int, fecha: String, hora: String)

    @Query("SELECT * FROM citas WHERE id = :id")
    suspend fun obtener(id: Int): Cita?

    // ---------- Horarios ----------
    /** Cuántas citas NO canceladas hay en esa fecha y hora (0 = libre). */
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

    /** Historial clínico: solo ATENDIDA, la más reciente primero. */
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

    /** Suma del precio de las citas ATENDIDAS desde una fecha. COALESCE evita el null si no hay. */
    @Query("SELECT COALESCE(SUM(precio), 0.0) FROM citas WHERE estado = 'ATENDIDA' AND fecha >= :fechaInicio")
    fun ingresosDesde(fechaInicio: String): Flow<Double>

    @Query("SELECT COUNT(*) FROM citas WHERE servicioId = :servicioId")
    suspend fun contarDeServicio(servicioId: Int): Int

    /** Mejora IA: peso registrado en cada atención, de la más antigua a la más reciente. */
    @Query(
        "SELECT fecha, pesoRegistrado AS peso FROM citas " +
            "WHERE mascotaId = :mascotaId AND estado = 'ATENDIDA' AND pesoRegistrado IS NOT NULL " +
            "ORDER BY fecha, hora"
    )
    fun observarPesos(mascotaId: Int): Flow<List<PuntoPeso>>

    @Query("SELECT mascotaId, MAX(fecha) AS fecha FROM citas WHERE estado = 'ATENDIDA' GROUP BY mascotaId")
    fun observarUltimasVisitas(): Flow<List<UltimaVisita>>
}
