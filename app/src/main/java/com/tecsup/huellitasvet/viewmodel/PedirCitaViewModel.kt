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

/**
 * Modelo de representación UI para cada opción de horario.
 * Indica la hora, si está ocupada en Room por otra cita activa o si ya transcurrió en el día de hoy.
 */
data class HorarioUi(
    val hora: String,
    val ocupado: Boolean,
    val pasado: Boolean
) {
    /** Propiedad calculada: el chip de hora solo se habilita si no está ocupado ni ha pasado. */
    val disponible: Boolean get() = !ocupado && !pasado
}

/**
 * ViewModel que controla la pantalla de agendamiento de citas (`PedirCitaScreen`).
 * Gestiona la selección en cascada de mascota, servicio, fecha y hora, aplicando validaciones
 * en tiempo real para evitar reservas duplicadas o fuera de horario.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PedirCitaViewModel(
    private val repo: VetRepository,
    prefs: PreferenciasStore,
    duenoId: Int,
    mascotaInicial: Int?
) : ViewModel() {

    /** Lista de mascotas pertenecientes al dueño actual para el selector. */
    val mascotas: StateFlow<List<Mascota>> = repo.observarMascotasDeDueno(duenoId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Lista de servicios activos cargados desde Room. */
    val servicios: StateFlow<List<Servicio>> = repo.observarServiciosActivos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Bandera global que indica si la veterinaria está aceptando reservas. */
    val aceptaCitas: StateFlow<Boolean> = prefs.aceptarCitas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    /** Lista fija de fechas disponibles (desde la fecha actual hasta 7 días posteriores). */
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

    /**
     * Reacciona dinámicamente al cambio de la fecha seleccionada (`_fecha`).
     * `flatMapLatest` cancela la observación del día anterior y consulta en Room los horarios ocupados del nuevo día.
     */
    val horasOcupadas: StateFlow<List<String>> = _fecha
        .flatMapLatest { f -> repo.observarHorasOcupadas(f.toString()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Combina la fecha elegida con la lista de horas ocupadas para construir la lista de `HorarioUi`.
     * Deshabilita los chips que ya están reservados o aquellos que corresponden a horas pasadas del día actual.
     */
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

    /**
     * Evalúa si todos los requerimientos se cumplen para activar el botón 'Confirmar cita'.
     * Requiere tener elegidos: mascota, servicio, horario válido y que la recepción esté activa.
     */
    val puedeConfirmar: StateFlow<Boolean> = combine(_mascotaId, _servicioId, _hora, aceptaCitas) { m, s, h, acepta ->
        m != null && s != null && h != null && acepta
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** Asigna la mascota seleccionada. */
    fun elegirMascota(id: Int) {
        _mascotaId.value = id
    }

    /** Asigna el servicio elegido. */
    fun elegirServicio(id: Int) {
        _servicioId.value = id
    }

    /** Asigna la fecha seleccionada y limpia la hora elegida previamente. */
    fun elegirFecha(f: LocalDate) {
        if (f != _fecha.value) {
            _fecha.value = f
            _hora.value = null
        }
    }

    /** Asigna la hora si el objeto `HorarioUi` indica disponibilidad. */
    fun elegirHora(h: String) {
        if (horarios.value.firstOrNull { it.hora == h }?.disponible == true) _hora.value = h
    }

    /** Resetea el mensaje de aviso activo. */
    fun limpiarMensaje() {
        _mensaje.value = null
    }

    /**
     * Ejecuta el proceso de confirmación de cita:
     * 1. Verifica nuevamente todas las condiciones (mascota, servicio, rango de fechas y horario no transcurrido).
     * 2. Consulta en Room si el horario seleccionado se ocupó mientras el usuario interactuaba.
     * 3. Crea la cita llamando al repositorio en estado PENDIENTE dentro de una transacción atómica.
     */
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
