package com.tecsup.huellitasvet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.huellitasvet.data.local.relation.MascotaConDueno
import com.tecsup.huellitasvet.data.repository.VetRepository
import com.tecsup.huellitasvet.model.Especie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Modelo de representación UI para la lista de pacientes del veterinario.
 * Asocia la entidad relacional `MascotaConDueno` con la fecha en texto de su última atención médica.
 */
data class PacienteUi(
    val datos: MascotaConDueno,
    val ultimaVisita: String?
)

/**
 * ViewModel que gestiona la lista de pacientes (`PacientesScreen`).
 * Implementa filtrado reactivo simultáneo por texto (búsqueda por nombre de mascota o dueño)
 * y por categoría de especie mediante el operador `combine`.
 */
class PacientesViewModel(repo: VetRepository) : ViewModel() {

    /** Estado interno mutable para la búsqueda por texto. */
    private val _texto = MutableStateFlow("")
    val texto: StateFlow<String> = _texto.asStateFlow()

    /** Estado interno mutable para el filtro de especie seleccionada (null representa 'Todos'). */
    private val _especie = MutableStateFlow<Especie?>(null)
    val especie: StateFlow<Especie?> = _especie.asStateFlow()

    /**
     * Combina 4 flujos en tiempo real:
     * 1. Lista de todas las mascotas con sus dueños.
     * 2. Mapeo de últimas visitas atendidas por mascota.
     * 3. Texto ingresado en el buscador.
     * 4. Especie seleccionada.
     * Aplica los filtros de especie y coincidencia de texto sobre nombre de mascota o dueño.
     */
    val pacientes: StateFlow<List<PacienteUi>> = combine(
        repo.observarTodasLasMascotas(),
        repo.observarUltimasVisitas(),
        _texto,
        _especie
    ) { mascotas, visitas, texto, especie ->
        val ultima = visitas.associate { it.mascotaId to it.fecha }
        val busqueda = texto.trim().lowercase()
        mascotas
            .filter { especie == null || it.mascota.especie == especie.name }
            .filter {
                busqueda.isEmpty() ||
                    it.mascota.nombre.lowercase().contains(busqueda) ||
                    it.dueno.nombre.lowercase().contains(busqueda)
            }
            .map { PacienteUi(it, ultima[it.mascota.id]) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Actualiza la consulta de búsqueda de texto. */
    fun buscar(t: String) {
        _texto.value = t
    }

    /** Asigna el filtro de especie. */
    fun filtrar(e: Especie?) {
        _especie.value = e
    }
}
