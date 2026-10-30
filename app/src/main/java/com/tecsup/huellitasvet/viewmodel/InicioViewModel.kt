package com.tecsup.huellitasvet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.huellitasvet.data.local.entity.Mascota
import com.tecsup.huellitasvet.data.local.entity.Servicio
import com.tecsup.huellitasvet.data.local.relation.CitaConDetalle
import com.tecsup.huellitasvet.data.local.relation.estado
import com.tecsup.huellitasvet.data.local.relation.fechaHora
import com.tecsup.huellitasvet.data.prefs.PreferenciasStore
import com.tecsup.huellitasvet.data.repository.VetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDateTime

/**
 * ViewModel para la pantalla principal del Dueño (`InicioScreen`).
 * Proporciona los flujos de datos necesarios para renderizar el saludo, la tarjeta
 * de la próxima cita agendada, el carrusel de mascotas del dueño y los servicios activos.
 */
class InicioViewModel(
    repo: VetRepository,
    prefs: PreferenciasStore,
    duenoId: Int
) : ViewModel() {

    /**
     * Lista reactiva de mascotas pertenecientes de forma exclusiva al dueño logueado (`duenoId`).
     * `stateIn` convierte el Flow frío del repositorio en un StateFlow caliente retenido durante 5 segundos sin suscriptores.
     */
    val mascotas: StateFlow<List<Mascota>> = repo.observarMascotasDeDueno(duenoId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Transforma el flujo total de citas del dueño para calcular en tiempo real cuál es la cita activa
     * (PENDIENTE o CONFIRMADA) más cercana a la fecha y hora actual.
     */
    val proximaCita: StateFlow<CitaConDetalle?> = repo.observarCitasDeDueno(duenoId)
        .map { citas ->
            val ahora = LocalDateTime.now()
            citas
                .filter { it.estado.esActiva && !it.fechaHora.isBefore(ahora) }
                .minByOrNull { it.fechaHora }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Flujo de servicios clínicos en estado activo para mostrar accesos directos en el inicio. */
    val servicios: StateFlow<List<Servicio>> = repo.observarServiciosActivos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Indica si la clínica veterinaria se encuentra recibiendo nuevas reservas actualmente. */
    val aceptaCitas: StateFlow<Boolean> = prefs.aceptarCitas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
}
