package com.tecsup.huellitasvet.model

/*
 * COMMIT 16 (EXCLUSIVO MEJORA-IA) - ia: agrega grafico de peso en la ficha de la mascota
 * Rama: mejora-ia
 * Hash: b5f589f
 *
 * ¿QUÉ SE REALIZÓ?
 * Se creó la data class PuntoPeso para representar un punto en la gráfica (fecha y valor numérico de peso).
 *
 * ¿POR QUÉ?
 * Para estructurar el historial de peso procesado desde Room antes de dibujar la gráfica de líneas Canvas.
 *
 * ARCHIVOS RELACIONADOS DEL MISMO COMMIT:
 * - PuntoPeso.kt
 * - GraficoLineas.kt
 * - CitaDao.kt
 * - VetRepository.kt
 * - VetRepositoryRoom.kt
 * - MascotaViewModel.kt
 * - FichaMascotaScreen.kt
 *
 * RELACIÓN:
 * PuntoPeso es transformado en MascotaViewModel y consumido por GraficoLineas en FichaMascotaScreen.
 */
data class PuntoPeso(
    val fecha: String,
    val peso: Double
)
