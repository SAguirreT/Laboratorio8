package com.tecsup.huellitasvet

import android.app.Application
import com.tecsup.huellitasvet.data.local.DatosIniciales
import com.tecsup.huellitasvet.data.local.VetDatabase
import com.tecsup.huellitasvet.data.prefs.PreferenciasStore
import com.tecsup.huellitasvet.data.repository.VetRepository
import com.tecsup.huellitasvet.data.repository.VetRepositoryRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Crea la base de datos y el repository una sola vez para toda la app. */
class VetApp : Application() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var repository: VetRepository
        private set

    lateinit var preferencias: PreferenciasStore
        private set

    override fun onCreate() {
        super.onCreate()
        val db = VetDatabase.obtener(this)
        repository = VetRepositoryRoom(db)
        preferencias = PreferenciasStore(this)
        scope.launch { DatosIniciales.sembrar(db) }
    }
}
