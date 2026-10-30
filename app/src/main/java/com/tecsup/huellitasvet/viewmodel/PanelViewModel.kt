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

/**
 * ViewModel que alimenta el Dashboard o Panel de la clínica (`PanelScreen`).
 * Procesa agregaciones SQL desde Room (contadores por estado, acumulación de ingresos mensuales y distribución por servicios)
 * y las mapea en tiempo real hacia las estructuras `DatoGrafico` requeridas por los componentes de gráficos (Dona y Barras).
 */
class PanelViewModel(repo: VetRepository) : ViewModel() {

    private val hoy = LocalDate.now().toString()
    private val inicioMes = LocalDate.now().withDayOfMonth(1).toString()

    /** Citas agendadas para el día de hoy. */
    val citasHoy: StateFlow<Int> = repo.observarCitasDeFecha(hoy).enEstado(0)
    /** Total de citas pendientes de confirmación en el sistema. */
    val pendientes: StateFlow<Int> = repo.observarCantidadPorEstado(EstadoCita.PENDIENTE).enEstado(0)
    /** Total de citas completadas exitosamente. */
    val atendidas: StateFlow<Int> = repo.observarCantidadPorEstado(EstadoCita.ATENDIDA).enEstado(0)

    /** Suma acumulada de ingresos por consultas atendidas en el mes en curso. */
    val ingresosMes: StateFlow<Double> = repo.observarIngresosDesde(inicioMes).enEstado(0.0)

    /**
     * Combina las observaciones de cada estado de cita para estructurar los datos del gráfico de dona.
     * Mapea cada `EstadoCita` a `DatoGrafico` asignando su color distintivo.
     */
    val porEstado: StateFlow<List<DatoGrafico>> = combine(
        EstadoCita.entries.map { e -> repo.observarCantidadPorEstado(e).map { e to it } }
    ) { pares ->
        pares.map { (estado, cantidad) -> DatoGrafico(estado.etiqueta, cantidad.toFloat(), colorEstado(estado)) }
    }.enEstado(emptyList())

    /**
     * Transforma el flujo agrupado por tipo de servicio para alimentar el gráfico de barras del panel.
     */
    val porServicio: StateFlow<List<DatoGrafico>> = repo.observarCitasPorServicio()
        .map { lista -> lista.map { DatoGrafico(it.nombre.substringBefore(' '), it.cantidad.toFloat()) } }
        .enEstado(emptyList())

    /** Función de extensión auxiliar para convertir un Flow en un StateFlow compartido en viewModelScope. */
    private fun <T> kotlinx.coroutines.flow.Flow<T>.enEstado(inicial: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), inicial)
}
