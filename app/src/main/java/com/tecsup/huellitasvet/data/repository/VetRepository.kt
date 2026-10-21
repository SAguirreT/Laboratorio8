package com.tecsup.huellitasvet.data.repository

import com.tecsup.huellitasvet.data.local.entity.Servicio
import kotlinx.coroutines.flow.Flow

/**
 * El contrato de datos de la app. Los ViewModels solo conocen esta interface;
 * hoy la implementa Room (VetRepositoryRoom) y en la Semana 9 la implementará un servicio web.
 */
interface VetRepository {
    // Usuarios, Mascotas, Citas y Panel: se agregan cuando existan las entidades (Pasos 1 a 4)

    // Servicios (ejemplo resuelto)
    fun observarServicios(): Flow<List<Servicio>>
    fun observarServiciosActivos(): Flow<List<Servicio>>
    suspend fun guardarServicio(s: Servicio)
    suspend fun eliminarServicio(s: Servicio)
    suspend fun cambiarActivo(id: Int, activo: Boolean)
    suspend fun servicioTieneCitas(id: Int): Boolean
}
