package com.tecsup.huellitasvet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.huellitasvet.data.local.entity.Mascota
import com.tecsup.huellitasvet.data.local.relation.CitaConDetalle
import com.tecsup.huellitasvet.data.repository.VetRepository
import com.tecsup.huellitasvet.ui.components.graficos.DatoGrafico
import com.tecsup.huellitasvet.model.Especie
import com.tecsup.huellitasvet.model.Fechas
import com.tecsup.huellitasvet.model.Sexo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Errores del formulario, uno por campo (null = sin error). */
data class ErroresMascota(
    val nombre: String? = null,
    val especie: String? = null,
    val sexo: String? = null,
    val anio: String? = null,
    val peso: String? = null
) {
    val hayErrores: Boolean get() = listOf(nombre, especie, sexo, anio, peso).any { it != null }
}

/**
 * Registrar/editar una mascota y ver su ficha con historial.
 * @param mascotaId id de la mascota a editar o ver; null si es una mascota nueva.
 */
class MascotaViewModel(
    private val repo: VetRepository,
    private val duenoId: Int,
    private val mascotaId: Int?
) : ViewModel() {

    val mascota: StateFlow<Mascota?> = (if (mascotaId != null) repo.observarMascota(mascotaId) else flowOf(null))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _errores = MutableStateFlow(ErroresMascota())
    val errores: StateFlow<ErroresMascota> = _errores.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    /** true cuando se guardó: la pantalla vuelve atrás. */
    private val _guardado = MutableStateFlow(false)
    val guardado: StateFlow<Boolean> = _guardado.asStateFlow()

    private val _eliminado = MutableStateFlow(false)
    val eliminado: StateFlow<Boolean> = _eliminado.asStateFlow()

    private val historiales = mutableMapOf<Int, StateFlow<List<CitaConDetalle>>>()

    /** Historial clínico (citas ATENDIDAS, la más reciente primero). Se crea una vez por mascota. */
    fun historial(mascotaId: Int): StateFlow<List<CitaConDetalle>> = historiales.getOrPut(mascotaId) {
        repo.observarHistorial(mascotaId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    private val pesosPorMascota = mutableMapOf<Int, StateFlow<List<DatoGrafico>>>()

    /** Mejora IA: puntos del gráfico de peso (fecha corta -> kg) de las atenciones de la mascota. */
    fun pesos(mascotaId: Int): StateFlow<List<DatoGrafico>> = pesosPorMascota.getOrPut(mascotaId) {
        repo.observarPesos(mascotaId)
            .map { lista -> lista.takeLast(6).map { DatoGrafico(Fechas.diaMes(it.fecha), it.peso.toFloat()) } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    /** Valida cada campo y, si todo está bien, inserta o actualiza. */
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

    /** Solo se elimina si no tiene citas; si tiene, se muestra un mensaje. */
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

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
