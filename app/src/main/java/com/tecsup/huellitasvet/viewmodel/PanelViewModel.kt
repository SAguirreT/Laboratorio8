package com.tecsup.huellitasvet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.huellitasvet.data.repository.VetRepository
import com.tecsup.huellitasvet.model.EstadoCita
import com.tecsup.huellitasvet.ui.components.colorEstado
import com.tecsup.huellitasvet.ui.components.graficos.DatoGrafico
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

/** Panel de la clínica: convierte los datos de Room al formato que piden los gráficos. */
class PanelViewModel(repo: VetRepository) : ViewModel() {

    private val hoy = LocalDate.now().toString()
    private val inicioMes = LocalDate.now().withDayOfMonth(1).toString()

    /** Citas de hoy sin contar las canceladas. */
    val citasHoy: StateFlow<Int> = repo.observarCitasDeFecha(hoy).enEstado(0)
    val pendientes: StateFlow<Int> = repo.observarCantidadPorEstado(EstadoCita.PENDIENTE).enEstado(0)
    val atendidas: StateFlow<Int> = repo.observarCantidadPorEstado(EstadoCita.ATENDIDA).enEstado(0)

    /** Suma del precio de las citas ATENDIDAS desde el día 1 del mes. */
    val ingresosMes: StateFlow<Double> = repo.observarIngresosDesde(inicioMes).enEstado(0.0)

    val porEstado: StateFlow<List<DatoGrafico>> = combine(
        EstadoCita.entries.map { e -> repo.observarCantidadPorEstado(e).map { e to it } }
    ) { pares ->
        pares.map { (estado, cantidad) -> DatoGrafico(estado.etiqueta, cantidad.toFloat(), colorEstado(estado)) }
    }.enEstado(emptyList())

    val porServicio: StateFlow<List<DatoGrafico>> = repo.observarCitasPorServicio()
        .map { lista -> lista.map { DatoGrafico(it.nombre.substringBefore(' '), it.cantidad.toFloat()) } }
        .enEstado(emptyList())

    private fun <T> kotlinx.coroutines.flow.Flow<T>.enEstado(inicial: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), inicial)
}
