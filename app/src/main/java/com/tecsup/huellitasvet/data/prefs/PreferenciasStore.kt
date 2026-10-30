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

/** Delegado para instanciar la propiedad DataStore de Preferencias a nivel de Context. */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ajustes")

/**
 * Gestor de preferencias persistentes del dispositivo utilizando Jetpack DataStore Preferences.
 * Almacena pares clave-valor ligeros como la sesión del usuario activo, tema oscuro y configuraciones de avisos.
 */
class PreferenciasStore(private val context: Context) {

    /** Definición de las claves tipadas para acceder a los valores guardados en DataStore. */
    private object Claves {
        val PERFIL_ACTIVO = stringPreferencesKey("perfilActivo")
        val USUARIO_ID = intPreferencesKey("usuarioId")
        val MODO_OSCURO = booleanPreferencesKey("modoOscuro")
        val NOTIFICACIONES = booleanPreferencesKey("notificaciones")
        val ACEPTAR_CITAS = booleanPreferencesKey("aceptarCitas")
    }

    // ---------- Lectura: flujos de datos asíncronos reactivos ----------
    /** Observa el rol del usuario actualmente logueado ("CLIENTE", "ADMIN" o cadena vacía). */
    val perfilActivo: Flow<String> = context.dataStore.data.map { it[Claves.PERFIL_ACTIVO] ?: "" }
    /** Observa el ID del usuario en sesión actual (0 si no hay sesión activa). */
    val usuarioId: Flow<Int> = context.dataStore.data.map { it[Claves.USUARIO_ID] ?: 0 }
    /** Observa la preferencia de tema oscuro del usuario. */
    val modoOscuro: Flow<Boolean> = context.dataStore.data.map { it[Claves.MODO_OSCURO] ?: false }
    /** Observa la activación de notificaciones y recordatorios. */
    val notificaciones: Flow<Boolean> = context.dataStore.data.map { it[Claves.NOTIFICACIONES] ?: true }
    /** Observa si la agenda veterinaria está abierta para recibir reservas de dueños. */
    val aceptarCitas: Flow<Boolean> = context.dataStore.data.map { it[Claves.ACEPTAR_CITAS] ?: true }

    // ---------- Escritura: funciones suspend para ejecutar de forma asíncrona ----------
    /** Guarda el rol y el id del usuario al iniciar sesión con éxito. */
    suspend fun guardarSesion(rol: String, usuarioId: Int) {
        context.dataStore.edit {
            it[Claves.PERFIL_ACTIVO] = rol
            it[Claves.USUARIO_ID] = usuarioId
        }
    }

    /** Alterna la configuración de modo oscuro. */
    suspend fun guardarModoOscuro(b: Boolean) {
        context.dataStore.edit { it[Claves.MODO_OSCURO] = b }
    }

    /** Guarda la preferencia de avisos y notificaciones. */
    suspend fun guardarNotificaciones(b: Boolean) {
        context.dataStore.edit { it[Claves.NOTIFICACIONES] = b }
    }

    /** Permite al veterinario activar o pausar la recepción de nuevas citas. */
    suspend fun guardarAceptarCitas(b: Boolean) {
        context.dataStore.edit { it[Claves.ACEPTAR_CITAS] = b }
    }

    /**
     * Limpia las credenciales guardadas al cerrar sesión.
     * Mantiene las preferencias de apariencia (como modo oscuro) intactas.
     */
    suspend fun cerrarSesion() {
        context.dataStore.edit {
            it.remove(Claves.PERFIL_ACTIVO)
            it.remove(Claves.USUARIO_ID)
        }
    }
}
