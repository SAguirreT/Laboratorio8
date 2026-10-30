package com.tecsup.huellitasvet.data.repository

import com.tecsup.huellitasvet.data.local.entity.Mascota
import com.tecsup.huellitasvet.data.local.entity.Servicio
import com.tecsup.huellitasvet.data.local.entity.Usuario
import com.tecsup.huellitasvet.data.local.relation.CitaConDetalle
import com.tecsup.huellitasvet.data.local.relation.MascotaConDueno
import com.tecsup.huellitasvet.model.ConteoServicio
import com.tecsup.huellitasvet.model.EstadoCita
import com.tecsup.huellitasvet.model.Rol
import com.tecsup.huellitasvet.model.UltimaVisita
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define el contrato de datos abstraído para la aplicación.
 * Sigue el patrón Repository de la arquitectura MVVM: desacopla la fuente de datos real
 * ( Room local o API REST remota) de la capa de presentación (ViewModels).
 *
 * Convención de diseño:
 * - Operaciones de consulta continua de la interfaz -> devuelven `Flow` (observación reactiva).
 * - Operaciones de un solo paso o modificación -> declaradas como `suspend`.
 */
interface VetRepository {
    // Usuarios
    suspend fun login(correo: String, clave: String): Usuario?
    suspend fun obtenerUsuario(id: Int): Usuario?
    suspend fun primerUsuarioConRol(rol: Rol): Usuario?

    // Servicios
    fun observarServicios(): Flow<List<Servicio>>
    fun observarServiciosActivos(): Flow<List<Servicio>>
    suspend fun guardarServicio(s: Servicio)
    suspend fun eliminarServicio(s: Servicio)
    suspend fun cambiarActivo(id: Int, activo: Boolean)
    suspend fun servicioTieneCitas(id: Int): Boolean

    // Mascotas
    fun observarMascotasDeDueno(duenoId: Int): Flow<List<Mascota>>
    fun observarMascota(id: Int): Flow<Mascota?>
    fun observarTodasLasMascotas(): Flow<List<MascotaConDueno>>
    suspend fun guardarMascota(m: Mascota)
    suspend fun eliminarMascota(m: Mascota)
    suspend fun mascotaTieneCitas(id: Int): Boolean
    fun observarUltimasVisitas(): Flow<List<UltimaVisita>>

    // Citas
    fun observarHorasOcupadas(fecha: String): Flow<List<String>>
    suspend fun crearCita(mascotaId: Int, servicioId: Int, fecha: String, hora: String, nota: String): Long
    fun observarCitasDeDueno(duenoId: Int): Flow<List<CitaConDetalle>>
    fun observarCitasPorFecha(fecha: String): Flow<List<CitaConDetalle>>
    fun observarCita(id: Int): Flow<CitaConDetalle?>
    fun observarHistorial(mascotaId: Int): Flow<List<CitaConDetalle>>
    suspend fun cambiarEstadoCita(id: Int, estado: EstadoCita)
    suspend fun registrarAtencion(citaId: Int, diagnostico: String, tratamiento: String, peso: Double)

    // Panel administrativo
    fun observarCitasDeFecha(fecha: String): Flow<Int>
    fun observarCantidadPorEstado(estado: EstadoCita): Flow<Int>
    fun observarCitasPorServicio(): Flow<List<ConteoServicio>>
    fun observarIngresosDesde(fechaInicio: String): Flow<Double>
}
