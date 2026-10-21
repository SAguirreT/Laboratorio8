package com.tecsup.huellitasvet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.huellitasvet.data.local.relation.CitaConDetalle
import com.tecsup.huellitasvet.data.local.relation.estado
import com.tecsup.huellitasvet.data.local.relation.fechaHora
import com.tecsup.huellitasvet.data.repository.VetRepository
import com.tecsup.huellitasvet.model.Horarios
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Mejora IA: el dueño cambia la fecha y hora de una cita PENDIENTE o CONFIRMADA.
 * Respeta la regla de las 2 horas y que el nuevo horario esté libre y en el futuro.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReprogramarCitaViewModel(
    private val repo: VetRepository,
    citaId: Int
) : ViewModel() {

    val cita: StateFlow<CitaConDetalle?> = repo.observarCita(citaId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val fechas: List<LocalDate> = (0L..7L).map { LocalDate.now().plusDays(it) }

    private val _fecha = MutableStateFlow(LocalDate.now())
    val fecha: StateFlow<LocalDate> = _fecha.asStateFlow()

    private val _hora = MutableStateFlow<String?>(null)
    val hora: StateFlow<String?> = _hora.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private val _listo = MutableStateFlow(false)
    val listo: StateFlow<Boolean> = _listo.asStateFlow()

    private val ocupadas: StateFlow<List<String>> = _fecha
        .flatMapLatest { repo.observarHorasOcupadas(it.toString()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val horarios: StateFlow<List<HorarioUi>> = combine(_fecha, ocupadas) { f, ocup ->
        val ahora = LocalTime.now()
        Horarios.todos.map { h ->
            HorarioUi(h, ocupado = h in ocup, pasado = f == LocalDate.now() && !LocalTime.parse(h).isAfter(ahora))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Misma regla que cancelar: solo PENDIENTE/CONFIRMADA y si faltan más de 2 horas. */
    fun sePuedeReprogramar(c: CitaConDetalle): Boolean =
        c.estado.esActiva && Duration.between(LocalDateTime.now(), c.fechaHora).toMinutes() > 120

    fun elegirFecha(f: LocalDate) {
        if (f != _fecha.value) {
            _fecha.value = f
            _hora.value = null
        }
    }

    fun elegirHora(h: String) {
        if (horarios.value.firstOrNull { it.hora == h }?.disponible == true) _hora.value = h
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    fun guardar() {
        val c = cita.value ?: return
        val h = _hora.value
        val f = _fecha.value
        when {
            !sePuedeReprogramar(c) -> { _mensaje.value = "Ya no puedes reprogramar esta cita"; return }
            h == null -> { _mensaje.value = "Elige un nuevo horario"; return }
            f.toString() == c.cita.fecha && h == c.cita.hora -> { _mensaje.value = "Es el mismo horario"; return }
        }
        viewModelScope.launch {
            val ok = repo.reprogramarCita(c.cita.id, f.toString(), h!!)
            if (ok) {
                _mensaje.value = "Cita reprogramada"
                _listo.value = true
            } else {
                _mensaje.value = "Ese horario acaba de ocuparse. Elige otro."
                _hora.value = null
            }
        }
    }
}
