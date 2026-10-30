package com.tecsup.huellitasvet.data.repository

import androidx.room.withTransaction
import com.tecsup.huellitasvet.data.local.VetDatabase
import com.tecsup.huellitasvet.data.local.entity.Cita
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
 * Implementación concreta del repositorio utilizando Room como fuente de datos persistente.
 * Mapea las llamadas del contrato `VetRepository` hacia las ejecuciones en los DAOs correspondientes.
 */
class VetRepositoryRoom(private val db: VetDatabase) : VetRepository {

    private val usuarioDao = db.usuarioDao()
    private val servicioDao = db.servicioDao()
    private val mascotaDao = db.mascotaDao()
    private val citaDao = db.citaDao()

    // ---------- Usuarios ----------
    override suspend fun login(correo: String, clave: String): Usuario? = usuarioDao.login(correo, clave)

    override suspend fun obtenerUsuario(id: Int): Usuario? = usuarioDao.obtener(id)

    override suspend fun primerUsuarioConRol(rol: Rol): Usuario? = usuarioDao.primeroConRol(rol.name)

    // ---------- Servicios ----------
    override fun observarServicios(): Flow<List<Servicio>> = servicioDao.observarTodos()

    override fun observarServiciosActivos(): Flow<List<Servicio>> = servicioDao.observarActivos()

    override suspend fun guardarServicio(s: Servicio) {
        if (s.id == 0) servicioDao.insertar(s) else servicioDao.actualizar(s)
    }

    override suspend fun eliminarServicio(s: Servicio) = servicioDao.eliminar(s)

    override suspend fun cambiarActivo(id: Int, activo: Boolean) = servicioDao.cambiarActivo(id, activo)

    override suspend fun servicioTieneCitas(id: Int): Boolean = citaDao.contarDeServicio(id) > 0

    // ---------- Mascotas ----------
    override fun observarMascotasDeDueno(duenoId: Int): Flow<List<Mascota>> = mascotaDao.observarPorDueno(duenoId)

    override fun observarMascota(id: Int): Flow<Mascota?> = mascotaDao.observarUna(id)

    override fun observarTodasLasMascotas(): Flow<List<MascotaConDueno>> = mascotaDao.observarTodas()

    override suspend fun guardarMascota(m: Mascota) {
        if (m.id == 0) mascotaDao.insertar(m) else mascotaDao.actualizar(m)
    }

    override suspend fun eliminarMascota(m: Mascota) = mascotaDao.eliminar(m)

    override suspend fun mascotaTieneCitas(id: Int): Boolean = mascotaDao.contarCitas(id) > 0

    override fun observarUltimasVisitas(): Flow<List<UltimaVisita>> = citaDao.observarUltimasVisitas()

    // ---------- Citas ----------
    override fun observarHorasOcupadas(fecha: String): Flow<List<String>> = citaDao.observarHorasOcupadas(fecha)

    /**
     * Ejecuta la creación de una cita dentro de un bloque de transacción atómica (`db.withTransaction`).
     * 1. Verifica la existencia del servicio y toma su precio actual.
     * 2. Confirma que el horario no haya sido ocupado por otro usuario simultáneamente.
     * 3. Registra la cita en estado PENDIENTE. Si alguna verificación falla, retorna -1L y no guarda nada.
     */
    override suspend fun crearCita(mascotaId: Int, servicioId: Int, fecha: String, hora: String, nota: String): Long =
        db.withTransaction {
            val servicio = servicioDao.obtener(servicioId) ?: return@withTransaction -1L
            if (citaDao.contarOcupadas(fecha, hora) > 0) return@withTransaction -1L
            citaDao.insertar(
                Cita(
                    mascotaId = mascotaId,
                    servicioId = servicioId,
                    fecha = fecha,
                    hora = hora,
                    nota = nota,
                    estado = EstadoCita.PENDIENTE.name,
                    precio = servicio.precio,
                    fechaCreacion = System.currentTimeMillis()
                )
            )
        }

    override fun observarCitasDeDueno(duenoId: Int): Flow<List<CitaConDetalle>> = citaDao.observarPorDueno(duenoId)

    override fun observarCitasPorFecha(fecha: String): Flow<List<CitaConDetalle>> = citaDao.observarPorFecha(fecha)

    override fun observarCita(id: Int): Flow<CitaConDetalle?> = citaDao.observarUna(id)

    override fun observarHistorial(mascotaId: Int): Flow<List<CitaConDetalle>> = citaDao.observarHistorial(mascotaId)

    override suspend fun cambiarEstadoCita(id: Int, estado: EstadoCita) = citaDao.actualizarEstado(id, estado.name)

    /**
     * Registra la atención médica usando `db.withTransaction` para garantizar consistencia atómica entre tablas.
     * Modifica la cita a 'ATENDIDA' con su diagnóstico y simultáneamente actualiza el peso de la mascota.
     * Si ocurriese un error durante la actualización del peso, la transacción revierte los cambios en la cita.
     */
    override suspend fun registrarAtencion(citaId: Int, diagnostico: String, tratamiento: String, peso: Double) {
        db.withTransaction {
            val cita = citaDao.obtener(citaId) ?: error("La cita $citaId no existe")
            citaDao.registrarAtencion(citaId, diagnostico, tratamiento, peso)
            mascotaDao.actualizarPeso(cita.mascotaId, peso)
        }
    }

    // ---------- Panel administrativo ----------
    override fun observarCitasDeFecha(fecha: String): Flow<Int> = citaDao.contarPorFecha(fecha)

    override fun observarCantidadPorEstado(estado: EstadoCita): Flow<Int> = citaDao.contarPorEstado(estado.name)

    override fun observarCitasPorServicio(): Flow<List<ConteoServicio>> = citaDao.contarPorServicio()

    override fun observarIngresosDesde(fechaInicio: String): Flow<Double> = citaDao.ingresosDesde(fechaInicio)
}
