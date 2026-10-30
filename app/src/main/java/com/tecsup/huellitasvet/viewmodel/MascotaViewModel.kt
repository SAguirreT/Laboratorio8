package com.tecsup.huellitasvet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.huellitasvet.data.local.entity.Mascota
import com.tecsup.huellitasvet.data.local.relation.CitaConDetalle
import com.tecsup.huellitasvet.data.repository.VetRepository
import com.tecsup.huellitasvet.model.Especie
import com.tecsup.huellitasvet.model.Sexo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Data Class de soporte para encapsular las validaciones del formulario de mascotas.
 * Mantiene un mensaje de error individual por cada campo para ser mostrado bajo el componente UI.
 */
data class ErroresMascota(
    val nombre: String? = null,
    val especie: String? = null,
    val sexo: String? = null,
    val anio: String? = null,
    val peso: String? = null
) {
    /** Devuelve true si al menos un campo presenta alguna inconsistencia o error de validación. */
    val hayErrores: Boolean get() = listOf(nombre, especie, sexo, anio, peso).any { it != null }
}

/**
 * ViewModel encargado del registro, edición, eliminación y visualización de la ficha de una mascota.
 * Contiene el historial de atenciones clínicas de la mascota observada desde Room.
 */
class MascotaViewModel(
    private val repo: VetRepository,
    private val duenoId: Int,
    private val mascotaId: Int?
) : ViewModel() {

    /** Observa la mascota específica desde Room si `mascotaId` no es nulo, de lo contrario emite null. */
    val mascota: StateFlow<Mascota?> = (if (mascotaId != null) repo.observarMascota(mascotaId) else flowOf(null))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Estado reactivo con los errores de validación activos para la pantalla del formulario. */
    private val _errores = MutableStateFlow(ErroresMascota())
    val errores: StateFlow<ErroresMascota> = _errores.asStateFlow()

    /** Notificaciones informativas o advertencias hacia el usuario (por ejemplo, imposibilidad de borrar). */
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    /** Flag de éxito al guardar para detonar la navegación hacia atrás en Compose (`popBackStack`). */
    private val _guardado = MutableStateFlow(false)
    val guardado: StateFlow<Boolean> = _guardado.asStateFlow()

    /** Flag de éxito tras eliminar para cerrar pantallas vinculadas. */
    private val _eliminado = MutableStateFlow(false)
    val eliminado: StateFlow<Boolean> = _eliminado.asStateFlow()

    /** Caché en memoria para retener los StateFlows de historiales de atenciones generados por ID de mascota. */
    private val historiales = mutableMapOf<Int, StateFlow<List<CitaConDetalle>>>()

    /**
     * Devuelve el historial de atenciones clínicas (citas en estado 'ATENDIDA') de la mascota.
     * Utiliza memorización en mapa para no duplicar suscripciones de Flow al recomponer.
     */
    fun historial(mascotaId: Int): StateFlow<List<CitaConDetalle>> = historiales.getOrPut(mascotaId) {
        repo.observarHistorial(mascotaId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    /**
     * Valida minuciosamente cada campo ingresado:
     * - Nombre >= 2 caracteres.
     * - Especie y Sexo seleccionados obligatoriamente.
     * - Año de nacimiento entre 1990 y el año actual.
     * - Peso numérico positivo (> 0.0).
     * Si todo es correcto, ejecuta el guardado en base de datos e informa a `_guardado`.
     */
    fun guardar(
        nombre: String,
        especie: Especie?,
        raza: String,
        anioTexto: String,
        pesoTexto: String,
        sexo: Sexo?
    ) {
        val anioActual = LocalDate.now().year
        val anio = anioTexto.trim().toIntOrNull()
        val peso = pesoTexto.trim().replace(',', '.').toDoubleOrNull()
        val errores = ErroresMascota(
            nombre = if (nombre.trim().length < 2) "Mínimo 2 caracteres" else null,
            especie = if (especie == null) "Elige la especie" else null,
            sexo = if (sexo == null) "Elige el sexo" else null,
            anio = if (anio == null || anio < 1990 || anio > anioActual) "Entre 1990 y $anioActual" else null,
            peso = if (peso == null || peso <= 0.0) "Debe ser mayor que 0" else null
        )
        _errores.value = errores
        if (errores.hayErrores) return

        val nueva = Mascota(
            id = mascotaId ?: 0,
            duenoId = mascota.value?.duenoId ?: duenoId,
            nombre = nombre.trim(),
            especie = especie!!.name,
            raza = raza.trim().ifEmpty { "Sin raza" },
            anioNacimiento = anio!!,
            peso = peso!!,
            sexo = sexo!!.name
        )
        viewModelScope.launch {
            repo.guardarMascota(nueva)
            _guardado.value = true
        }
    }

    /**
     * Elimina el registro de la mascota.
     * Verifica previamente en Room si la mascota posee citas asociadas. Si existen registros,
     * se bloquea el borrado por integridad de clave foránea y muestra un mensaje al usuario.
     */
    fun eliminar(m: Mascota) {
        viewModelScope.launch {
            if (repo.mascotaTieneCitas(m.id)) {
                _mensaje.value = "${m.nombre} tiene citas registradas y no se puede eliminar"
            } else {
                repo.eliminarMascota(m)
                _eliminado.value = true
            }
        }
    }

    /** Resetea el mensaje informativo mostrado en Toast. */
    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
