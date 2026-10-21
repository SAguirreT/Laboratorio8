# HuellitasVet 🐾

App Android para gestionar una veterinaria con **dos perfiles en el mismo dispositivo**:

- **Dueño:** registra a sus mascotas, pide citas eligiendo servicio, fecha y hora, cancela citas (hasta 2 horas antes) y consulta el historial clínico.
- **Veterinario:** ve el panel con contadores y gráficos, gestiona la agenda (confirmar, atender, cancelar), registra la atención clínica, consulta pacientes y administra los servicios.

Ambos perfiles comparten la misma base de datos Room. Cuando el veterinario registra una atención, el dueño la ve al instante en el historial de su mascota.

**Tecnología:** Kotlin · Jetpack Compose · Material 3 (sin XML) · Room 2.6.1 · DataStore · Navigation Compose · minSdk 26 (java.time)
**Patrón:** MVVM + Repository (interface)

## Cómo ejecutar

1. Clona el repositorio y ábrelo en Android Studio.
2. Sincroniza Gradle y ejecuta en un emulador con API 26 o superior.
3. Cuentas de prueba (datos ficticios, Ley 29733):

| Perfil | Correo | Clave |
|---|---|---|
| Dueño | ana@correo.test | 1234 |
| Dueño | luis@correo.test | 1234 |
| Veterinario | vet@huellitas.test | admin |

## Arquitectura MVVM

```
VIEW (Composable)
   │ eventos (clics)          ▲ collectAsState()
   ▼                          │
VIEWMODEL (StateFlow + reglas de negocio)
   │ llama                    ▲ Flow / resultados
   ▼                          │
VetRepository (interface)
   │ la implementa
   ▼
VetRepositoryRoom ──► DAO ──► SQLite (huellitasvet.db)

DataStore (solo preferencias) ──► ViewModel
```

| Capa | Conoce a | NO debe |
|---|---|---|
| View (`ui/`) | Solo a su ViewModel | Usar DAO, Repository ni reglas de negocio |
| ViewModel | Solo al Repository (interface) | Conocer Composables ni el DAO |
| Repository | Los DAOs (en la implementación) | Conocer pantallas ni ViewModels |
| DAO / Room | SQL y entidades | Conocer ViewModels |

**Regla de oro:** una lectura que debe actualizar la pantalla devuelve `Flow` (sin `suspend`). Una escritura o acción única es `suspend`.

## Estructura de paquetes

```
com.tecsup.huellitasvet
├── MainActivity.kt
├── VetApp.kt                       crea la base, el repository y las preferencias
├── data
│   ├── local
│   │   ├── entity      Servicio, Usuario, Mascota, Cita
│   │   ├── dao         ServicioDao, UsuarioDao, MascotaDao, CitaDao
│   │   ├── relation    MascotaConDueno, CitaConDetalle (relación anidada)
│   │   ├── VetDatabase.kt          singleton
│   │   └── DatosIniciales.kt       datos de prueba la primera vez
│   ├── repository      VetRepository (interface), VetRepositoryRoom
│   └── prefs           PreferenciasStore (DataStore)
├── model               Rol, EstadoCita, Especie, Sexo, ConteoServicio, Horarios, Fechas, UltimaVisita
├── viewmodel           VetViewModelFactory + 11 ViewModels
└── ui
    ├── theme           Color, Type, Theme (VetTheme por rol, modo oscuro)
    ├── components      AppTopBar, VetButton, VetTextField, EstadoCitaChip, MascotaAvatar (Canvas),
    │                   CitaCard, FechaChip, HoraChip, HistorialItem, ...
    │   └── graficos    GraficoDona, GraficoBarras (Canvas)
    ├── navigation      Rutas, NavVet
    ├── dueno           Splash, Login, Inicio, Mascotas, FormularioMascota, FichaMascota,
    │                   PedirCita, MisCitas, Perfil
    └── veterinario     Panel, Agenda, AtenderCita, Pacientes, Servicios, FormularioServicio, Ajustes
```

## Base de datos (4 tablas)

| Tabla | Relación |
|---|---|
| usuarios | — |
| servicios | — |
| mascotas | N mascotas → 1 dueño (`duenoId`) |
| citas | N citas → 1 mascota (`mascotaId`) · N citas → 1 servicio (`servicioId`) |

Cada clave foránea tiene su índice, y además hay un índice en `(fecha, hora)` de `citas` para acelerar la consulta de horarios ocupados.

## Reglas de negocio (viven en los ViewModels)

- **Visibilidad:** el dueño solo ve SUS mascotas y SUS citas.
- **Registrar mascota:** nombre de 2 caracteres o más, especie y sexo elegidos, año entre 1990 y el actual, peso mayor que 0.
- **Eliminar mascota:** solo si no tiene citas.
- **Pedir cita:** servicio ACTIVO, fecha de hoy hasta 7 días, 18 horarios de 09:00 a 17:30. Si la fecha es hoy, solo se permiten horas futuras. Nace PENDIENTE y guarda el precio del servicio.
- **Horario único:** si existe una cita NO cancelada en esa fecha y hora, el horario aparece gris y tachado.
- **Cancelar (dueño):** solo PENDIENTE o CONFIRMADA y si faltan más de 2 horas.
- **Estados (veterinario):** PENDIENTE → CONFIRMADA. Ambas pueden pasar a CANCELADA. ATENDIDA solo se logra registrando la atención.
- **Registrar atención:** diagnóstico de 5 caracteres o más y peso mayor que 0. Todo ocurre en **una transacción** (`db.withTransaction`): la cita pasa a ATENDIDA y se actualiza el peso de la mascota.
- **Servicios:** un servicio con citas no se elimina, solo se desactiva. Un servicio inactivo no aparece al pedir cita.

## Capturas

Las capturas están en la carpeta [`capturas/`](capturas/).

| Dueño | | |
|---|---|---|
| ![Inicio](capturas/dueno_inicio.png) | ![Pedir cita](capturas/dueno_pedir_cita.png) | ![Ficha](capturas/dueno_ficha.png) |

| Veterinario | | |
|---|---|---|
| ![Panel](capturas/vet_panel.png) | ![Agenda](capturas/vet_agenda.png) | ![Atender](capturas/vet_atender.png) |

## Para pensar

**¿Por qué el Repository es una interface?**
Porque así los ViewModels dependen de un contrato y no de Room. En la Semana 9 se podrá crear `VetRepositoryApi` (servicio web) sin cambiar ningún ViewModel ni pantalla: solo cambia la clase que se crea en `VetApp`.

**¿Por qué las lecturas son Flow y las escrituras suspend?**
Un `Flow` de Room vuelve a emitir cada vez que la tabla cambia. Por eso, cuando el veterinario confirma una cita, la lista del dueño se actualiza sola. Una escritura ocurre una sola vez y devuelve un resultado, así que basta con `suspend`.

**¿Por qué registrar la atención usa una transacción?**
Porque modifica dos tablas (`citas` y `mascotas`). Si fallara la segunda operación, quedaría una cita ATENDIDA con el peso de la mascota sin actualizar. `withTransaction` garantiza que se hagan las dos o ninguna.

**¿Por qué se guarda el precio en la cita?**
Es una "foto" del momento. Si la clínica cambia el precio de un servicio, las citas antiguas conservan lo que costaron y los ingresos del panel no se alteran.

**¿Por qué la fecha y la hora son texto?**
El formato `aaaa-MM-dd` se ordena igual que las fechas, se compara con `=` en SQL y se convierte con `LocalDate.parse()`. Así no hacen falta convertidores de tipos.

**¿Qué hace `flatMapLatest` en Pedir cita?**
Cada vez que cambia la fecha elegida, cancela la consulta de horarios de la fecha anterior y empieza a observar la nueva. Así la grilla de horarios siempre corresponde al día seleccionado.

**¿Cómo se arma `CitaConDetalle`?**
Con `@Embedded Cita`, una `@Relation` a `MascotaConDueno`, que es una relación anidada (`entity = Mascota::class`) que a su vez trae al dueño, y otra `@Relation` a `Servicio`. Los métodos del DAO llevan `@Transaction` porque Room ejecuta varias consultas para armar el objeto.

**¿Qué va en DataStore y qué en Room?**
DataStore guarda preferencias pequeñas del dispositivo: perfil activo, usuario, modo oscuro, notificaciones y aceptar citas. Los datos de la clínica (mascotas, citas y servicios) van en Room.
