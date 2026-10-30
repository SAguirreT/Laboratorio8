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
 * ViewModel que gestiona el estado global de la sesión de usuario y la configuración general.
 * Persiste a nivel de la Activity principal, controlando el usuario autenticado, el inicio de sesión,
 * la persistencia en DataStore y el cambio dinámico de rol para la demostración.
 */
class SesionViewModel(
    private val repo: VetRepository,
    private val prefs: PreferenciasStore
) : ViewModel() {

    /** Estado interno mutable que contiene el usuario en sesión o null si se deslogueó. */
    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    /** Expone el usuario autenticado a la UI como StateFlow de solo lectura. */
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    /** Estado mutable para notificar mensajes de error en el formulario de login. */
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /** Indica si se está verificando la sesión almacenada previamente en DataStore durante el Splash. */
    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    /** Convierte el Flow de modo oscuro desde DataStore a un StateFlow observable en Compose. */
    val modoOscuro: StateFlow<Boolean> = prefs.modoOscuro
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** Estado reactivo para la preferencia de notificaciones. */
    val notificaciones: StateFlow<Boolean> = prefs.notificaciones
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    /** Estado reactivo que indica si la clínica está aceptando citas. */
    val aceptarCitas: StateFlow<Boolean> = prefs.aceptarCitas
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    init {
        // Al instanciarse, comprueba si DataStore mantiene una sesión guardada de un inicio previo.
        viewModelScope.launch {
            val rol = prefs.perfilActivo.first()
            val id = prefs.usuarioId.first()
            if (rol.isNotEmpty() && id > 0) {
                _usuarioActual.value = repo.obtenerUsuario(id)
            }
            _cargando.value = false
        }
    }

    /**
     * Procesa la autenticación del usuario.
     * Valida que los campos no estén vacíos, consulta el repositorio y verifica que el rol
     * seleccionado en la interfaz coincida con el rol registrado del usuario.
     */
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

    /** Limpia los mensajes de error activos al cambiar de pestaña o editar campos. */
    fun limpiarError() {
        _error.value = null
    }

    /** Actualiza la preferencia del tema visual en DataStore dentro de una corrutina. */
    fun cambiarModoOscuro(b: Boolean) {
        viewModelScope.launch { prefs.guardarModoOscuro(b) }
    }

    /** Actualiza la preferencia de notificaciones en DataStore. */
    fun cambiarNotificaciones(b: Boolean) {
        viewModelScope.launch { prefs.guardarNotificaciones(b) }
    }

    /** Cambia el flag global de recepción de citas. */
    fun cambiarAceptarCitas(b: Boolean) {
        viewModelScope.launch { prefs.guardarAceptarCitas(b) }
    }

    /**
     * Permite conmutar rápidamente entre el perfil de cliente y el de veterinario (modo práctica/demostración).
     * Recupera el primer usuario registrado con el otro rol y actualiza la sesión activa.
     */
    fun cambiarPerfil() {
        val actual = _usuarioActual.value ?: return
        val otroRol = if (actual.rol == Rol.ADMIN.name) Rol.CLIENTE else Rol.ADMIN
        viewModelScope.launch {
            val otro = repo.primerUsuarioConRol(otroRol) ?: return@launch
            prefs.guardarSesion(otro.rol, otro.id)
            _usuarioActual.value = otro
        }
    }

    /** Cierra la sesión activa borrando el id de DataStore y reiniciando el estado del usuario a null. */
    fun cerrarSesion() {
        viewModelScope.launch {
            prefs.cerrarSesion()
            _usuarioActual.value = null
        }
    }
}
