package com.tecsup.huellitasvet.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tecsup.huellitasvet.VetApp

/** Fábrica genérica: recibe una función que construye el ViewModel con sus dependencias. */
class VetViewModelFactory(private val crear: () -> ViewModel) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = crear() as T
}

/**
 * Atajo para las pantallas:
 * val vm = vetViewModel { app -> ServiciosViewModel(app.repository) }
 * El ViewModel vive mientras viva su destino de navegación.
 */
@Composable
inline fun <reified VM : ViewModel> vetViewModel(
    key: String? = null,
    crossinline crear: (VetApp) -> VM
): VM {
    val app = LocalContext.current.applicationContext as VetApp
    return viewModel(key = key, factory = VetViewModelFactory { crear(app) })
}
