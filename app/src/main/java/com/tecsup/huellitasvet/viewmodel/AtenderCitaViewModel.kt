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

/** Data Class para errores de validación en la atención clínica. */
data class ErroresAtencion(
    val diagnostico: String? = null,
    val peso: String? = null
)

/**
 * ViewModel que controla la pantalla de atención médica (`AtenderCitaScreen`).
 * Valida los datos clínicos ingresados (diagnóstico y peso) e invoca la transacción
 * atómica en el repositorio para marcar la cita como ATENDIDA y actualizar el peso de la mascota.
 */
class AtenderCitaViewModel(
    private val repo: VetRepository,
    citaId: Int
) : ViewModel() {

    /** Observa la cita actual con todos sus detalles (Mascota, Dueño y Servicio). */
    val cita: StateFlow<CitaConDetalle?> = repo.observarCita(citaId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _errores = MutableStateFlow(ErroresAtencion())
    val errores: StateFlow<ErroresAtencion> = _errores.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    /** Flag de cierre que notifica a la UI para regresar a la agenda (`onVolver`). */
    private val _terminado = MutableStateFlow(false)
    val terminado: StateFlow<Boolean> = _terminado.asStateFlow()

    /**
     * Valida e ingresa los registros de la atención médica:
     * - Diagnóstico debe tener al menos 5 caracteres.
     * - Peso debe ser mayor a 0.
     * Invoca `repo.registrarAtencion` que ejecuta ambas escrituras dentro de una única transacción `db.withTransaction`.
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

    /** Permite cancelar la cita activa desde la pantalla de atención. */
    fun cancelar() {
        val c = cita.value ?: return
        if (!c.estado.esActiva) return
        viewModelScope.launch {
            repo.cambiarEstadoCita(c.cita.id, EstadoCita.CANCELADA)
            _mensaje.value = "Cita cancelada"
            _terminado.value = true
        }
    }

    /** Limpia los mensajes flotantes de notificación. */
    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
