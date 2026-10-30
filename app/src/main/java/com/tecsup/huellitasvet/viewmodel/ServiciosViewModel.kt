package com.tecsup.huellitasvet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.huellitasvet.data.local.entity.Servicio
import com.tecsup.huellitasvet.data.repository.VetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Data Class para capturar inconsistencias al crear o editar un servicio. */
data class ErroresServicio(
    val nombre: String? = null,
    val duracion: String? = null,
    val precio: String? = null
)

/**
 * ViewModel para administrar los servicios de la veterinaria (`ServiciosScreen` y `FormularioServicioScreen`).
 * Permite listar, habilitar/deshabilitar mediante switch, guardar nuevos servicios y eliminarlos con validación previa.
 */
class ServiciosViewModel(private val repo: VetRepository) : ViewModel() {

    /** Observa la lista completa de servicios desde la base de datos. */
    val servicios: StateFlow<List<Servicio>> = repo.observarServicios()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _errores = MutableStateFlow(ErroresServicio())
    val errores: StateFlow<ErroresServicio> = _errores.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    /** Flag de éxito que avisa a la UI que el formulario terminó correctamente. */
    private val _listo = MutableStateFlow(false)
    val listo: StateFlow<Boolean> = _listo.asStateFlow()

    /** Conmuta el estado del atributo `activo` mediante un switch en la interfaz. */
    fun alternar(s: Servicio) {
        viewModelScope.launch { repo.cambiarActivo(s.id, !s.activo) }
    }

    /**
     * Valida el formulario e inserta o actualiza el servicio:
     * - Nombre obligatorio.
     * - Duración en minutos permitida (15, 30, 45, 60).
     * - Precio mayor que 0.0.
     */
    fun guardar(servicio: Servicio) {
        val errores = ErroresServicio(
            nombre = if (servicio.nombre.isBlank()) "El nombre es obligatorio" else null,
            duracion = if (servicio.duracionMin !in DURACIONES) "Elige 15, 30, 45 o 60 min" else null,
            precio = if (servicio.precio <= 0.0) "Debe ser mayor que 0" else null
        )
        _errores.value = errores
        if (errores.nombre != null || errores.duracion != null || errores.precio != null) return
        viewModelScope.launch {
            repo.guardarServicio(servicio.copy(nombre = servicio.nombre.trim(), descripcion = servicio.descripcion.trim()))
            _listo.value = true
        }
    }

    /**
     * Intenta eliminar el servicio.
     * Verifica previamente en Room si el servicio posee citas registradas. Si cuenta con citas,
     * impide la eliminación física sugiriendo desactivarlo para conservar el historial.
     */
    fun eliminar(servicio: Servicio) {
        viewModelScope.launch {
            if (repo.servicioTieneCitas(servicio.id)) {
                _mensaje.value = "${servicio.nombre} tiene citas: solo se puede desactivar"
            } else {
                repo.eliminarServicio(servicio)
                _mensaje.value = "Servicio eliminado"
                _listo.value = true
            }
        }
    }

    /** Limpia los mensajes en pantalla. */
    fun limpiarMensaje() {
        _mensaje.value = null
    }

    companion object {
        val DURACIONES = listOf(15, 30, 45, 60)
    }
}
