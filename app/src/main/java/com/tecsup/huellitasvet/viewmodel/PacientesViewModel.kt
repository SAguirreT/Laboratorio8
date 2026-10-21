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

/** Un paciente para la lista: mascota con dueño y la fecha de su última visita (o null). */
data class PacienteUi(
    val datos: MascotaConDueno,
    val ultimaVisita: String?
)

/** Lista de pacientes filtrada por texto y por especie al mismo tiempo (combine). */
class PacientesViewModel(repo: VetRepository) : ViewModel() {

    private val _texto = MutableStateFlow("")
    val texto: StateFlow<String> = _texto.asStateFlow()

    /** null = Todos. */
    private val _especie = MutableStateFlow<Especie?>(null)
    val especie: StateFlow<Especie?> = _especie.asStateFlow()

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

    fun buscar(t: String) {
        _texto.value = t
    }

    fun filtrar(e: Especie?) {
        _especie.value = e
    }
}
