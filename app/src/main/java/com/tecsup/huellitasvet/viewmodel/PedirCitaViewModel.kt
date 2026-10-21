package com.tecsup.huellitasvet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.huellitasvet.data.local.entity.Mascota
import com.tecsup.huellitasvet.data.local.entity.Servicio
import com.tecsup.huellitasvet.data.prefs.PreferenciasStore
import com.tecsup.huellitasvet.data.repository.VetRepository
import com.tecsup.huellitasvet.model.Horarios
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

/** Un horario del día y si se puede elegir. */
data class HorarioUi(
    val hora: String,
    val ocupado: Boolean, // ya hay una cita no cancelada
    val pasado: Boolean   // es hoy y la hora ya pasó
) {
    val disponible: Boolean get() = !ocupado && !pasado
}

/**
 * Pedir cita. Reglas: mascota del dueño, servicio ACTIVO, fecha de hoy a 7 días,
 * horario libre y futuro. La cita nace PENDIENTE con el precio del servicio.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PedirCitaViewModel(
    private val repo: VetRepository,
    prefs: PreferenciasStore,
    duenoId: Int,
    mascotaInicial: Int?
) : ViewModel() {

    val mascotas: StateFlow<List<Mascota>> = repo.observarMascotasDeDueno(duenoId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Solo servicios activos: un servicio inactivo no aparece al pedir cita. */
    val servicios: StateFlow<List<Servicio>> = repo.observarServiciosActivos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aceptaCitas: StateFlow<Boolean> = prefs.aceptarCitas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    /** Hoy y los 7 días siguientes. */
    val fechas: List<LocalDate> = (0L..7L).map { LocalDate.now().plusDays(it) }

    private val _mascotaId = MutableStateFlow(mascotaInicial)
    val mascotaId: StateFlow<Int?> = _mascotaId.asStateFlow()

    private val _servicioId = MutableStateFlow<Int?>(null)
    val servicioId: StateFlow<Int?> = _servicioId.asStateFlow()

    private val _fecha = MutableStateFlow(LocalDate.now())
    val fecha: StateFlow<LocalDate> = _fecha.asStateFlow()

    private val _hora = MutableStateFlow<String?>(null)
    val hora: StateFlow<String?> = _hora.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private val _citaCreada = MutableStateFlow(false)
    val citaCreada: StateFlow<Boolean> = _citaCreada.asStateFlow()

    /** Cada vez que cambia la fecha, se cancela la consulta anterior y se observa la nueva. */
    val horasOcupadas: StateFlow<List<String>> = _fecha
        .flatMapLatest { f -> repo.observarHorasOcupadas(f.toString()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Los 18 horarios del día marcando los ocupados y, si es hoy, los que ya pasaron. */
    val horarios: StateFlow<List<HorarioUi>> = combine(_fecha, horasOcupadas) { f, ocupadas ->
        val ahora = LocalTime.now()
        Horarios.todos.map { h ->
            HorarioUi(
                hora = h,
                ocupado = h in ocupadas,
                pasado = f == LocalDate.now() && !LocalTime.parse(h).isAfter(ahora)
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** El botón 'Confirmar cita' se habilita solo cuando todo está elegido. */
    val puedeConfirmar: StateFlow<Boolean> = combine(_mascotaId, _servicioId, _hora, aceptaCitas) { m, s, h, acepta ->
        m != null && s != null && h != null && acepta
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun elegirMascota(id: Int) {
        _mascotaId.value = id
    }

    fun elegirServicio(id: Int) {
        _servicioId.value = id
    }

    /** Si la fecha cambia, se limpia la hora elegida. */
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

    fun confirmar(nota: String = "") {
        val mascota = mascotas.value.firstOrNull { it.id == _mascotaId.value }
        val servicio = servicios.value.firstOrNull { it.id == _servicioId.value }
        val f = _fecha.value
        val h = _hora.value
        when {
            !aceptaCitas.value -> { _mensaje.value = "La clínica no está recibiendo citas"; return }
            mascota == null -> { _mensaje.value = "Elige una de tus mascotas"; return }
            servicio == null -> { _mensaje.value = "Elige un servicio disponible"; return }
            f.isBefore(LocalDate.now()) || f.isAfter(LocalDate.now().plusDays(7)) -> {
                _mensaje.value = "La fecha debe estar entre hoy y 7 días"; return
            }
            h == null -> { _mensaje.value = "Elige un horario"; return }
            f == LocalDate.now() && !LocalTime.parse(h).isAfter(LocalTime.now()) -> {
                _mensaje.value = "Ese horario ya pasó"; _hora.value = null; return
            }
        }
        val hora = h ?: return
        viewModelScope.launch {
            // Antes de crear, se vuelve a consultar: el horario pudo ocuparse mientras tanto.
            val ocupadas = repo.observarHorasOcupadas(f.toString()).first()
            if (hora in ocupadas) {
                _mensaje.value = "Ese horario acaba de ocuparse. Elige otro."
                _hora.value = null
                return@launch
            }
            val id = repo.crearCita(mascota!!.id, servicio!!.id, f.toString(), hora, nota.trim())
            if (id > 0) {
                _citaCreada.value = true
            } else {
                _mensaje.value = "Ese horario acaba de ocuparse. Elige otro."
                _hora.value = null
            }
        }
    }
}
