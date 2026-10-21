package com.tecsup.huellitasvet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.huellitasvet.data.local.relation.CitaConDetalle
import com.tecsup.huellitasvet.data.local.relation.estado
import com.tecsup.huellitasvet.data.repository.VetRepository
import com.tecsup.huellitasvet.model.EstadoCita
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ErroresAtencion(
    val diagnostico: String? = null,
    val peso: String? = null
)

/** Registro clínico de una cita. */
class AtenderCitaViewModel(
    private val repo: VetRepository,
    citaId: Int
) : ViewModel() {

    val cita: StateFlow<CitaConDetalle?> = repo.observarCita(citaId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _errores = MutableStateFlow(ErroresAtencion())
    val errores: StateFlow<ErroresAtencion> = _errores.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    /** true cuando se registró o canceló: la pantalla vuelve a la agenda. */
    private val _terminado = MutableStateFlow(false)
    val terminado: StateFlow<Boolean> = _terminado.asStateFlow()

    /**
     * Diagnóstico de al menos 5 caracteres y peso > 0. Solo citas PENDIENTE o CONFIRMADA.
     * repo.registrarAtencion hace todo en UNA transacción (cita ATENDIDA + peso de la mascota).
     */
    fun registrar(diagnostico: String, tratamiento: String, pesoTexto: String) {
        val c = cita.value ?: return
        if (!c.estado.esActiva) {
            _mensaje.value = "Solo se puede atender una cita pendiente o confirmada"
            return
        }
        val peso = pesoTexto.trim().replace(',', '.').toDoubleOrNull()
        val errores = ErroresAtencion(
            diagnostico = if (diagnostico.trim().length < 5) "Mínimo 5 caracteres" else null,
            peso = if (peso == null || peso <= 0.0) "Debe ser mayor que 0" else null
        )
        _errores.value = errores
        if (errores.diagnostico != null || errores.peso != null) return

        viewModelScope.launch {
            try {
                repo.registrarAtencion(c.cita.id, diagnostico.trim(), tratamiento.trim(), peso!!)
                _mensaje.value = "Atención registrada"
                _terminado.value = true
            } catch (e: Exception) {
                _mensaje.value = "No se pudo registrar: ${e.message}"
            }
        }
    }

    fun cancelar() {
        val c = cita.value ?: return
        if (!c.estado.esActiva) return
        viewModelScope.launch {
            repo.cambiarEstadoCita(c.cita.id, EstadoCita.CANCELADA)
            _mensaje.value = "Cita cancelada"
            _terminado.value = true
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
