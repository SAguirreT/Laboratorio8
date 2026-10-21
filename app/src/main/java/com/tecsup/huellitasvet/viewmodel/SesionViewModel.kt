package com.tecsup.huellitasvet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.huellitasvet.data.local.entity.Usuario
import com.tecsup.huellitasvet.data.prefs.PreferenciasStore
import com.tecsup.huellitasvet.data.repository.VetRepository
import com.tecsup.huellitasvet.model.Rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Sesión de la app (vive en la Activity): usuario actual, preferencias y login.
 * Las reglas del login viven aquí, no en el Composable.
 */
class SesionViewModel(
    private val repo: VetRepository,
    private val prefs: PreferenciasStore
) : ViewModel() {

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /** true mientras se lee la sesión guardada en DataStore. */
    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    val modoOscuro: StateFlow<Boolean> = prefs.modoOscuro
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val notificaciones: StateFlow<Boolean> = prefs.notificaciones
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val aceptarCitas: StateFlow<Boolean> = prefs.aceptarCitas
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    init {
        // Si ya hay perfil guardado, se recupera el usuario y el Splash salta el login.
        viewModelScope.launch {
            val rol = prefs.perfilActivo.first()
            val id = prefs.usuarioId.first()
            if (rol.isNotEmpty() && id > 0) {
                _usuarioActual.value = repo.obtenerUsuario(id)
            }
            _cargando.value = false
        }
    }

    fun login(correo: String, clave: String, rolSeleccionado: Rol) {
        _error.value = null
        if (correo.isBlank() || clave.isBlank()) {
            _error.value = "Completa el correo y la contraseña"
            return
        }
        viewModelScope.launch {
            val usuario = repo.login(correo.trim(), clave)
            when {
                usuario == null -> _error.value = "Correo o contraseña incorrectos"
                usuario.rol != rolSeleccionado.name ->
                    _error.value = "Esta cuenta no es de ${rolSeleccionado.etiqueta.lowercase()}. Cambia de pestaña."
                else -> {
                    prefs.guardarSesion(usuario.rol, usuario.id)
                    _usuarioActual.value = usuario
                }
            }
        }
    }

    fun limpiarError() {
        _error.value = null
    }

    fun cambiarModoOscuro(b: Boolean) {
        viewModelScope.launch { prefs.guardarModoOscuro(b) }
    }

    fun cambiarNotificaciones(b: Boolean) {
        viewModelScope.launch { prefs.guardarNotificaciones(b) }
    }

    fun cambiarAceptarCitas(b: Boolean) {
        viewModelScope.launch { prefs.guardarAceptarCitas(b) }
    }

    /** Modo práctica: guarda el otro perfil y NavVet navega a su grafo. */
    fun cambiarPerfil() {
        val actual = _usuarioActual.value ?: return
        val otroRol = if (actual.rol == Rol.ADMIN.name) Rol.CLIENTE else Rol.ADMIN
        viewModelScope.launch {
            val otro = repo.primerUsuarioConRol(otroRol) ?: return@launch
            prefs.guardarSesion(otro.rol, otro.id)
            _usuarioActual.value = otro
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            prefs.cerrarSesion()
            _usuarioActual.value = null
        }
    }
}
