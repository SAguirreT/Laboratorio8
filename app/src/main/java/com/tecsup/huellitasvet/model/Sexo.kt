package com.tecsup.huellitasvet.model

enum class Sexo(val etiqueta: String) {
    MACHO("Macho"),
    HEMBRA("Hembra");

    companion object {
        fun desde(texto: String?): Sexo = entries.firstOrNull { it.name == texto } ?: MACHO
    }
}
