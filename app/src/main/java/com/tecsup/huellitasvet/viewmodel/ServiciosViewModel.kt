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

data class ErroresServicio(
    val nombre: String? = null,
    val duracion: String? = null,
    val precio: String? = null
)

class ServiciosViewModel(private val repo: VetRepository) : ViewModel() {

    val servicios: StateFlow<List<Servicio>> = repo.observarServicios()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _errores = MutableStateFlow(ErroresServicio())
    val errores: StateFlow<ErroresServicio> = _errores.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    /** true cuando se guardó o eliminó: el formulario vuelve a la lista. */
    private val _listo = MutableStateFlow(false)
    val listo: StateFlow<Boolean> = _listo.asStateFlow()

    fun alternar(s: Servicio) {
        viewModelScope.launch { repo.cambiarActivo(s.id, !s.activo) }
    }

    /** Nuevo (id = 0) o editado. Nombre obligatorio, duración 15/30/45/60 y precio > 0. */
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

    /** Un servicio con citas no se elimina: solo se puede desactivar. */
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

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    companion object {
        val DURACIONES = listOf(15, 30, 45, 60)
    }
}
