package com.tecsup.huellitasvet.ui.navigation

/** Todas las rutas de la app. Las que reciben un id usan -1 cuando es 'nuevo'. */
object Rutas {
    const val SPLASH = "splash"
    const val LOGIN = "login"

    // Dueño
    const val INICIO = "inicio"
    const val MASCOTAS = "mascotas"
    const val MIS_CITAS = "mis_citas"
    const val PERFIL = "perfil"
    const val FORM_MASCOTA = "form_mascota?id={id}"
    const val FICHA = "ficha/{id}"
    const val PEDIR_CITA = "pedir_cita?mascotaId={mascotaId}"

    fun formMascota(id: Int = -1) = "form_mascota?id=$id"
    fun ficha(id: Int) = "ficha/$id"
    fun pedirCita(mascotaId: Int = -1) = "pedir_cita?mascotaId=$mascotaId"

    const val REPROGRAMAR = "reprogramar/{id}" // mejora IA
    fun reprogramar(id: Int) = "reprogramar/$id"

    // Veterinario
    const val PANEL = "panel"
    const val AGENDA = "agenda"
    const val PACIENTES = "pacientes"
    const val SERVICIOS = "servicios"
    const val AJUSTES = "ajustes"
    const val ATENDER = "atender/{id}"
    const val FORM_SERVICIO = "form_servicio?id={id}"

    fun atender(id: Int) = "atender/$id"
    fun formServicio(id: Int = -1) = "form_servicio?id=$id"
}
