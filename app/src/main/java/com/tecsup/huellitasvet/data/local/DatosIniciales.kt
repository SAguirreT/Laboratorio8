package com.tecsup.huellitasvet.data.local

import com.tecsup.huellitasvet.data.local.entity.Servicio

/** Siembra los datos de prueba la primera vez que se abre la app. */
object DatosIniciales {

    suspend fun sembrar(db: VetDatabase) {
        // TODO (Paso 3): si usuarioDao().contar() == 0 inserta usuarios, mascotas y citas
        if (db.servicioDao().contar() == 0) {
            db.servicioDao().insertarTodos(servicios())
        }
    }

    private fun servicios() = listOf(
        Servicio(nombre = "Consulta general", descripcion = "Revisión completa de la mascota", duracionMin = 30, precio = 60.0, icono = "heart"),
        Servicio(nombre = "Vacunación", descripcion = "Aplicación de vacunas según calendario", duracionMin = 30, precio = 45.0, icono = "syringe"),
        Servicio(nombre = "Baño y corte", descripcion = "Higiene general y corte de pelo", duracionMin = 60, precio = 50.0, icono = "scissors"),
        Servicio(nombre = "Desparasitación", descripcion = "Tratamiento antiparasitario interno y externo", duracionMin = 15, precio = 25.0, activo = false, icono = "heart")
    )
}
