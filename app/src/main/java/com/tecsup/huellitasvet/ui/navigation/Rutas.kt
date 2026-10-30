package com.tecsup.huellitasvet.ui.navigation

/**
 * Objeto constante que define centralizadamente todas las cadenas de ruta de navegación para Jetpack Navigation.
 * Define los argumentos requeridos y opcionales para transferir parámetros entre pantallas (e.g. ID de mascota, cita o servicio).
 */
object Rutas {
    const val SPLASH = "splash"
    const val LOGIN = "login"

    // Dueño (Cliente)
    const val INICIO = "inicio"
    const val MASCOTAS = "mascotas"
    const val MIS_CITAS = "mis_citas"
    const val PERFIL = "perfil"
    const val FORM_MASCOTA = "form_mascota?id={id}"
    const val FICHA = "ficha/{id}"
    const val PEDIR_CITA = "pedir_cita?mascotaId={mascotaId}"

    /** Helper para construir la URI de navegación al formulario de mascota (id = -1 para registrar nueva). */
    fun formMascota(id: Int = -1) = "form_mascota?id=$id"
    /** Helper para construir la URI de la ficha de mascota específica. */
    fun ficha(id: Int) = "ficha/$id"
    /** Helper para construir la URI de agendamiento de cita precargando opcionalmente la mascota. */
    fun pedirCita(mascotaId: Int = -1) = "pedir_cita?mascotaId=$mascotaId"

    // Veterinario (Administrador)
    const val PANEL = "panel"
    const val AGENDA = "agenda"
    const val PACIENTES = "pacientes"
    const val SERVICIOS = "servicios"
    const val AJUSTES = "ajustes"
    const val ATENDER = "atender/{id}"
    const val FORM_SERVICIO = "form_servicio?id={id}"

    /** Helper para navegar a la atención clínica de una cita por su ID. */
    fun atender(id: Int) = "atender/$id"
    /** Helper para navegar al formulario de creación o edición de un servicio clínico. */
    fun formServicio(id: Int = -1) = "form_servicio?id=$id"
}
