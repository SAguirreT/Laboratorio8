package com.tecsup.huellitasvet.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tecsup.huellitasvet.VetApp

/**
 * Fábrica genérica para la instanciación personalizada de ViewModels que requieren dependencias en su constructor.
 * Implementa `ViewModelProvider.Factory` delegando la creación a un lambda builder.
 */
class VetViewModelFactory(private val crear: () -> ViewModel) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = crear() as T
}

/**
 * Función helper composable para instanciar ViewModels de forma concisa inyectando las dependencias globales de `VetApp`.
 * Obtiene el contexto de la aplicación para extraer el repositorio y las preferencias.
 */
@Composable
inline fun <reified VM : ViewModel> vetViewModel(
    key: String? = null,
    crossinline crear: (VetApp) -> VM
): VM {
    val app = LocalContext.current.applicationContext as VetApp
    return viewModel(key = key, factory = VetViewModelFactory { crear(app) })
}
