package com.tecsup.huellitasvet.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** Utilidades para mostrar fechas en español sin depender del idioma del teléfono. */
object Fechas {
    private val DIAS = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    private val MESES = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")

    fun diaCorto(fecha: LocalDate): String = DIAS[fecha.dayOfWeek.value - 1]

    /** "21 oct" */
    fun diaMes(fecha: String): String = runCatching {
        val f = LocalDate.parse(fecha)
        "%02d %s".format(f.dayOfMonth, MESES[f.monthValue - 1])
    }.getOrDefault(fecha)

    /** "Mié 21 oct" */
    fun larga(fecha: String): String = runCatching {
        val f = LocalDate.parse(fecha)
        "${diaCorto(f)} ${f.dayOfMonth} ${MESES[f.monthValue - 1]}"
    }.getOrDefault(fecha)

    /** Junta los textos 'aaaa-MM-dd' y 'HH:mm' de una cita en un LocalDateTime. */
    fun fechaHora(fecha: String, hora: String): LocalDateTime =
        LocalDateTime.of(LocalDate.parse(fecha), LocalTime.parse(hora))
}
