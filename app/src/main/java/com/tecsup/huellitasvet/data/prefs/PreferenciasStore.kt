package com.tecsup.huellitasvet.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Declarada una sola vez, a nivel de archivo. */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ajustes")

/**
 * Preferencias pequeñas del dispositivo (clave-valor).
 * Mascotas, citas y servicios NO van aquí: van en Room.
 */
class PreferenciasStore(private val context: Context) {

    private object Claves {
        val PERFIL_ACTIVO = stringPreferencesKey("perfilActivo")
        val USUARIO_ID = intPreferencesKey("usuarioId")
        val MODO_OSCURO = booleanPreferencesKey("modoOscuro")
        val NOTIFICACIONES = booleanPreferencesKey("notificaciones")
        val ACEPTAR_CITAS = booleanPreferencesKey("aceptarCitas")
    }

    // ---------- Lectura: un Flow por preferencia ----------
    /** "CLIENTE", "ADMIN" o "" si no hay sesión. */
    val perfilActivo: Flow<String> = context.dataStore.data.map { it[Claves.PERFIL_ACTIVO] ?: "" }
    val usuarioId: Flow<Int> = context.dataStore.data.map { it[Claves.USUARIO_ID] ?: 0 }
    val modoOscuro: Flow<Boolean> = context.dataStore.data.map { it[Claves.MODO_OSCURO] ?: false }
    val notificaciones: Flow<Boolean> = context.dataStore.data.map { it[Claves.NOTIFICACIONES] ?: true }
    val aceptarCitas: Flow<Boolean> = context.dataStore.data.map { it[Claves.ACEPTAR_CITAS] ?: true }

    // ---------- Escritura: suspend ----------
    suspend fun guardarSesion(rol: String, usuarioId: Int) {
        context.dataStore.edit {
            it[Claves.PERFIL_ACTIVO] = rol
            it[Claves.USUARIO_ID] = usuarioId
        }
    }

    suspend fun guardarModoOscuro(b: Boolean) {
        context.dataStore.edit { it[Claves.MODO_OSCURO] = b }
    }

    suspend fun guardarNotificaciones(b: Boolean) {
        context.dataStore.edit { it[Claves.NOTIFICACIONES] = b }
    }

    suspend fun guardarAceptarCitas(b: Boolean) {
        context.dataStore.edit { it[Claves.ACEPTAR_CITAS] = b }
    }

    /** Borra solo la sesión; el modo oscuro y los demás ajustes se conservan. */
    suspend fun cerrarSesion() {
        context.dataStore.edit {
            it.remove(Claves.PERFIL_ACTIVO)
            it.remove(Claves.USUARIO_ID)
        }
    }
}
