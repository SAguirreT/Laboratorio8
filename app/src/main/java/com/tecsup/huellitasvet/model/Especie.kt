package com.tecsup.huellitasvet.model

enum class Especie(val etiqueta: String) {
    PERRO("Perro"),
    GATO("Gato"),
    OTRO("Otro");

    companion object {
        fun desde(texto: String?): Especie = entries.firstOrNull { it.name == texto } ?: OTRO
    }
}
