package com.tecsup.huellitasvet.model

/** Perfil del usuario. Se guarda en la tabla como texto (Rol.name). */
enum class Rol(val etiqueta: String) {
    CLIENTE("Dueño"),
    ADMIN("Veterinario");

    companion object {
        fun desde(texto: String?): Rol = entries.firstOrNull { it.name == texto } ?: CLIENTE
    }
}
