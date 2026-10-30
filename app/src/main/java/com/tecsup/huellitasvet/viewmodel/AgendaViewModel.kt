package com.tecsup.huellitasvet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.huellitasvet.data.local.relation.CitaConDetalle
import com.tecsup.huellitasvet.data.local.relation.estado
import com.tecsup.huellitasvet.data.repository.VetRepository
import com.tecsup.huellitasvet.model.EstadoCita
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel que respalda la gestión de la agenda del veterinario (`AgendaScreen`).
 * Permite filtrar por días de la semana y modificar el ciclo de vida de la cita
 * (transición PENDIENTE -> CONFIRMADA o CANCELADA).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AgendaViewModel(private val repo: VetRepository) : ViewModel() {

    /** Lista de los 7 días consecutivos a partir de hoy para los chips de selección. */
    val dias: List<LocalDate> = (0L..6L).map { LocalDate.now().plusDays(it) }

    /** Estado reactivo del día seleccionado actualmente. Por defecto inicia en hoy. */
    private val _fechaSeleccionada = MutableStateFlow(LocalDate.now())
    val fechaSeleccionada: StateFlow<LocalDate> = _fechaSeleccionada.asStateFlow()

    /**
     * Observa las citas programadas para el día seleccionado.
     * `flatMapLatest` reconecta la consulta SQL cada vez que el usuario toca un chip de fecha diferente.
     */
    val citas: StateFlow<List<CitaConDetalle>> = _fechaSeleccionada
        .flatMapLatest { repo.observarCitasPorFecha(it.toString()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Actualiza la fecha activa en la agenda. */
    fun elegirFecha(f: LocalDate) {
        _fechaSeleccionada.value = f
    }

    /**
     * Confirma una cita en estado PENDIENTE pasándola a CONFIRMADA.
     * La modificación se refleja de inmediato en Room y actualiza los flujos observables.
     */
    fun confirmar(c: CitaConDetalle) {
        if (c.estado != EstadoCita.PENDIENTE) return
        viewModelScope.launch { repo.cambiarEstadoCita(c.cita.id, EstadoCita.CONFIRMADA) }
    }

    /** Cancela una cita activa (PENDIENTE o CONFIRMADA). */
    fun cancelar(c: CitaConDetalle) {
        if (!c.estado.esActiva) return
        viewModelScope.launch { repo.cambiarEstadoCita(c.cita.id, EstadoCita.CANCELADA) }
    }
}
