package com.tecsup.huellitasvet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.huellitasvet.data.local.relation.CitaConDetalle
import com.tecsup.huellitasvet.data.local.relation.estado
import com.tecsup.huellitasvet.data.local.relation.fechaHora
import com.tecsup.huellitasvet.data.repository.VetRepository
import com.tecsup.huellitasvet.model.EstadoCita
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

/**
 * ViewModel para gestionar el listado de citas del cliente (`MisCitasScreen`).
 * Separa automáticamente en dos flujos reactivos las citas Próximas y las del Historial,
 * permitiendo además la cancelación sujeta a la regla de anticipación de 2 horas.
 */
class MisCitasViewModel(
    private val repo: VetRepository,
    duenoId: Int
) : ViewModel() {

    /** Flow principal que emite todas las citas del dueño registradas en Room. */
    private val todas = repo.observarCitasDeDueno(duenoId)

    /**
     * Citas en estado PENDIENTE o CONFIRMADA cuya fecha y hora aún no han transcurrido.
     * Se presentan ordenadas cronológicamente por la más cercana primero.
     */
    val proximas: StateFlow<List<CitaConDetalle>> = todas
        .map { lista ->
            val ahora = LocalDateTime.now()
            lista.filter { esProxima(it, ahora) }.sortedBy { it.fechaHora }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Citas históricas (ATENDIDA, CANCELADA o con horario superado).
     * Se presentan ordenadas descendentemente por la más reciente primero.
     */
    val historial: StateFlow<List<CitaConDetalle>> = todas
        .map { lista ->
            val ahora = LocalDateTime.now()
            lista.filterNot { esProxima(it, ahora) }.sortedByDescending { it.fechaHora }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Mensaje emergente de notificación o advertencia sobre el resultado de una acción. */
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    /** Evalúa si una cita califica como próxima en función de su estado activo y hora futura. */
    private fun esProxima(c: CitaConDetalle, ahora: LocalDateTime) =
        c.estado.esActiva && !c.fechaHora.isBefore(ahora)

    /**
     * Aplica la regla de negocio de cancelación:
     * El usuario solo puede cancelar si la cita está activa y faltan más de 120 minutos (2 horas)
     * para la hora programada.
     */
    fun puedeCancelar(c: CitaConDetalle): Boolean {
        if (!c.estado.esActiva) return false
        val faltan = Duration.between(LocalDateTime.now(), c.fechaHora)
        return faltan.toMinutes() > 120
    }

    /**
     * Solicita la cancelación de la cita al repositorio.
     * Si no cumple la regla de las 2 horas, deniega la acción notificando el error a la UI.
     */
    fun cancelar(c: CitaConDetalle) {
        if (!puedeCancelar(c)) {
            _mensaje.value = "Ya no puedes cancelar esta cita"
            return
        }
        viewModelScope.launch {
            repo.cambiarEstadoCita(c.cita.id, EstadoCita.CANCELADA)
            _mensaje.value = "Cita cancelada"
        }
    }

    /** Limpia el mensaje modal de notificación. */
    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
