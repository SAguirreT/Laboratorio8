package com.tecsup.huellitasvet.model

import java.time.LocalTime

/** La clínica atiende de 09:00 a 17:30, cada 30 minutos: 18 horarios. */
object Horarios {
    private val INICIO: LocalTime = LocalTime.of(9, 0)
    private const val CANTIDAD = 18
    private const val INTERVALO_MIN = 30L

    /** "09:00", "09:30", ... "17:30" */
    val todos: List<String> = (0 until CANTIDAD).map { i ->
        INICIO.plusMinutes(INTERVALO_MIN * i).toString()
    }
}
