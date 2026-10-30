package com.tecsup.huellitasvet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tecsup.huellitasvet.data.local.dao.CitaDao
import com.tecsup.huellitasvet.data.local.dao.MascotaDao
import com.tecsup.huellitasvet.data.local.dao.ServicioDao
import com.tecsup.huellitasvet.data.local.dao.UsuarioDao
import com.tecsup.huellitasvet.data.local.entity.Cita
import com.tecsup.huellitasvet.data.local.entity.Mascota
import com.tecsup.huellitasvet.data.local.entity.Servicio
import com.tecsup.huellitasvet.data.local.entity.Usuario

/*
 * COMMIT 4 - feat(data): registra entidades y siembra datos iniciales en VetDatabase
 * Rama: main
 * Hash: 005fb16
 *
 * ¿QUÉ SE REALIZÓ?
 * Se configuró VetDatabase registrando las entidades (Servicio, Usuario, Mascota, Cita) y versión de esquema 2.
 *
 * ¿POR QUÉ?
 * Para inicializar SQLite con Room Singleton y permitir la precarga automática de datos de prueba.
 *
 * ARCHIVOS RELACIONADOS DEL MISMO COMMIT:
 * - VetDatabase.kt
 * - DatosIniciales.kt
 *
 * RELACIÓN:
 * VetDatabase expone los DAOs y ejecuta la siembra inicial a través de DatosIniciales.
 */
@Database(
    entities = [Servicio::class, Usuario::class, Mascota::class, Cita::class],
    version = 2,
    exportSchema = false
)
abstract class VetDatabase : RoomDatabase() {
    abstract fun servicioDao(): ServicioDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun mascotaDao(): MascotaDao
    abstract fun citaDao(): CitaDao

    companion object {
        @Volatile
        private var INSTANCE: VetDatabase? = null

        fun obtener(context: Context): VetDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                VetDatabase::class.java,
                "huellitasvet.db"
            )
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
        }
    }
}
