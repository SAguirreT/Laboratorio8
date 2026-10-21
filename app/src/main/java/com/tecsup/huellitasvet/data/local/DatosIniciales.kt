package com.tecsup.huellitasvet.data.local

import com.tecsup.huellitasvet.data.local.entity.Cita
import com.tecsup.huellitasvet.data.local.entity.Mascota
import com.tecsup.huellitasvet.data.local.entity.Servicio
import com.tecsup.huellitasvet.data.local.entity.Usuario
import com.tecsup.huellitasvet.model.EstadoCita
import com.tecsup.huellitasvet.model.Especie
import com.tecsup.huellitasvet.model.Rol
import com.tecsup.huellitasvet.model.Sexo
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Siembra los datos de prueba SOLO la primera vez (cuando no hay usuarios).
 * Las fechas se calculan desde hoy para que siempre haya citas pasadas, de hoy y futuras.
 * Todos los datos son ficticios (Ley 29733).
 */
object DatosIniciales {

    suspend fun sembrar(db: VetDatabase) {
        if (db.usuarioDao().contar() > 0) return

        // 1) Servicios (puede que el proyecto base ya los haya insertado)
        if (db.servicioDao().contar() == 0) db.servicioDao().insertarTodos(servicios())
        val todos = db.servicioDao().observarTodos().first()
        fun servicio(nombre: String) = todos.first { it.nombre == nombre }
        val consulta = servicio("Consulta general")
        val vacuna = servicio("Vacunación")
        val bano = servicio("Baño y corte")

        // 2) Usuarios: 2 dueños y 1 veterinario
        val idsUsuarios = db.usuarioDao().insertarTodos(
            listOf(
                Usuario(nombre = "Ana García", correo = "ana@correo.test", clave = "1234", rol = Rol.CLIENTE.name),
                Usuario(nombre = "Luis Ramírez", correo = "luis@correo.test", clave = "1234", rol = Rol.CLIENTE.name),
                Usuario(nombre = "Dra. Rojas", correo = "vet@huellitas.test", clave = "admin", rol = Rol.ADMIN.name)
            )
        ).map { it.toInt() }
        val ana = idsUsuarios[0]
        val luis = idsUsuarios[1]

        // 3) Mascotas
        val idsMascotas = db.mascotaDao().insertarTodos(
            listOf(
                Mascota(duenoId = ana, nombre = "Luna", especie = Especie.GATO.name, raza = "Siamés", anioNacimiento = 2023, peso = 4.3, sexo = Sexo.HEMBRA.name),
                Mascota(duenoId = ana, nombre = "Max", especie = Especie.PERRO.name, raza = "Labrador", anioNacimiento = 2021, peso = 24.5, sexo = Sexo.MACHO.name),
                Mascota(duenoId = luis, nombre = "Rocky", especie = Especie.PERRO.name, raza = "Bulldog", anioNacimiento = 2019, peso = 18.0, sexo = Sexo.MACHO.name),
                Mascota(duenoId = luis, nombre = "Michi", especie = Especie.GATO.name, raza = "Mestizo", anioNacimiento = 2024, peso = 3.8, sexo = Sexo.HEMBRA.name)
            )
        ).map { it.toInt() }
        val luna = idsMascotas[0]
        val max = idsMascotas[1]
        val rocky = idsMascotas[2]
        val michi = idsMascotas[3]

        // 4) Citas
        fun dia(n: Long): String = LocalDate.now().plusDays(n).toString()
        fun cita(
            mascota: Int, s: Servicio, n: Long, hora: String, estado: EstadoCita,
            diagnostico: String? = null, tratamiento: String? = null, peso: Double? = null
        ) = Cita(
            mascotaId = mascota, servicioId = s.id, fecha = dia(n), hora = hora,
            estado = estado.name, precio = s.precio,
            diagnostico = diagnostico, tratamiento = tratamiento, pesoRegistrado = peso
        )

        db.citaDao().insertarTodos(
            listOf(
                // Pasadas y ATENDIDAS (historial clínico)
                cita(luna, bano, -220, "10:00", EstadoCita.ATENDIDA, "Higiene general, piel sana.", "Ninguno", 4.0),
                cita(luna, consulta, -110, "11:30", EstadoCita.ATENDIDA, "Control de rutina, sin hallazgos.", "Vitaminas por 15 días", 4.2),
                cita(luna, vacuna, -9, "09:30", EstadoCita.ATENDIDA, "Vacuna triple felina aplicada.", "Próximo refuerzo en 12 meses", 4.3),
                cita(max, consulta, -30, "16:00", EstadoCita.ATENDIDA, "Leve sobrepeso, buen estado general.", "Dieta controlada", 24.5),
                cita(rocky, vacuna, -15, "12:00", EstadoCita.ATENDIDA, "Vacuna antirrábica aplicada.", "Reposo 24 horas", 18.0),
                // Hoy
                cita(max, bano, 0, "10:30", EstadoCita.CONFIRMADA),
                cita(rocky, consulta, 0, "16:30", EstadoCita.PENDIENTE),
                cita(michi, consulta, 0, "17:00", EstadoCita.PENDIENTE),
                // Próximos días
                cita(luna, vacuna, 1, "10:30", EstadoCita.CONFIRMADA),
                cita(max, consulta, 2, "16:00", EstadoCita.PENDIENTE),
                cita(luna, bano, 3, "09:30", EstadoCita.PENDIENTE),
                cita(rocky, bano, 4, "11:00", EstadoCita.CANCELADA)
            )
        )
    }

    private fun servicios() = listOf(
        Servicio(nombre = "Consulta general", descripcion = "Revisión completa de la mascota", duracionMin = 30, precio = 60.0, icono = "heart"),
        Servicio(nombre = "Vacunación", descripcion = "Aplicación de vacunas según calendario", duracionMin = 30, precio = 45.0, icono = "syringe"),
        Servicio(nombre = "Baño y corte", descripcion = "Higiene general y corte de pelo", duracionMin = 60, precio = 50.0, icono = "scissors"),
        Servicio(nombre = "Desparasitación", descripcion = "Tratamiento antiparasitario interno y externo", duracionMin = 15, precio = 25.0, activo = false, icono = "heart")
    )
}
