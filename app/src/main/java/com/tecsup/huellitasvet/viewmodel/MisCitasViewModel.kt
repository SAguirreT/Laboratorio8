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
 * Citas del dueño separadas en Próximas e Historial.
 * Como vienen de un Flow de Room, si el veterinario confirma o atiende una cita la lista cambia sola.
 */
class MisCitasViewModel(
    private val repo: VetRepository,
    duenoId: Int
) : ViewModel() {

    private val todas = repo.observarCitasDeDueno(duenoId)

    /** PENDIENTE y CONFIRMADA desde ahora, la más cercana primero. */
    val proximas: StateFlow<List<CitaConDetalle>> = todas
        .map { lista ->
            val ahora = LocalDateTime.now()
            lista.filter { esProxima(it, ahora) }.sortedBy { it.fechaHora }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** ATENDIDA, CANCELADA y las que ya pasaron, la más reciente primero. */
    val historial: StateFlow<List<CitaConDetalle>> = todas
        .map { lista ->
            val ahora = LocalDateTime.now()
            lista.filterNot { esProxima(it, ahora) }.sortedByDescending { it.fechaHora }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private fun esProxima(c: CitaConDetalle, ahora: LocalDateTime) =
        c.estado.esActiva && !c.fechaHora.isBefore(ahora)

    /** Regla de las 2 horas: solo PENDIENTE o CONFIRMADA y si faltan MÁS de 2 horas. */
    fun puedeCancelar(c: CitaConDetalle): Boolean {
        if (!c.estado.esActiva) return false
        val faltan = Duration.between(LocalDateTime.now(), c.fechaHora)
        return faltan.toMinutes() > 120
    }

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

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
