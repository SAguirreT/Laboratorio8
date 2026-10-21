# Prompts utilizados

> Fase 2 (rama `mejora-ia`). Las mejoras se hicieron con un asistente de IA y después se revisaron para que respeten MVVM + Repository.
> Revisa y ajusta este documento con tus propias palabras antes de entregar.

## Prompt 1: Gráfico de peso de la mascota

**Texto enviado a la IA:**
Tengo una app Kotlin con Jetpack Compose, Material 3, MVVM, Room y un VetRepository (interface). Agrega en la ficha de la mascota un gráfico de línea con el peso registrado en cada cita ATENDIDA. La consulta va en CitaDao, la función en VetRepository y su implementación, y la lista de puntos se expone como StateFlow en MascotaViewModel. La View solo dibuja el gráfico con Canvas. No uses XML ni librerías externas. Muéstrame solo los archivos que cambian.

**Qué generó la IA:**
- `model/PuntoPeso.kt` (fecha y peso).
- `CitaDao.observarPesos(mascotaId)`: `SELECT fecha, pesoRegistrado AS peso ... WHERE estado = 'ATENDIDA' AND pesoRegistrado IS NOT NULL ORDER BY fecha, hora`.
- `observarPesos` declarada primero en `VetRepository` y luego implementada en `VetRepositoryRoom`.
- `MascotaViewModel.pesos(mascotaId)`: convierte la lista a `List<DatoGrafico>` (últimos 6 puntos).
- `ui/components/graficos/GraficoLineas.kt` dibujado con Canvas (líneas guía, línea con `Path` y círculos en cada punto).
- Una tarjeta "Evolución del peso" en la pestaña Datos de `FichaMascotaScreen`.

**Qué tuve que corregir yo:**
- La primera versión calculaba la lista de puntos dentro del Composable. La moví al ViewModel para que la View solo dibuje.
- Si todos los pesos eran iguales, se dividía entre 0 al calcular la altura. Se agregó el caso `maximo == minimo` para dibujar la línea al centro.
- Con un solo punto no hay línea, así que se muestra un mensaje si hay menos de 2 atenciones.

**¿Respeta MVVM?:** Sí. La consulta está en el DAO, la función se declaró primero en la interface, el ViewModel expone un `StateFlow` y la View solo observa y dibuja.

## Prompt 2: Reprogramar cita

**Texto enviado a la IA:**
En la misma app, el dueño debe poder cambiar la fecha y hora de una cita PENDIENTE o CONFIRMADA, respetando que el horario esté libre (no puede haber dos citas no canceladas en la misma fecha y hora) y la regla de las 2 horas que ya uso para cancelar. Agrega la consulta UPDATE en CitaDao, la función en VetRepository y su implementación con una transacción que vuelva a revisar el horario, un ReprogramarCitaViewModel con flatMapLatest para los horarios ocupados y una pantalla que reutilice FechaChip y HoraChip. Agrega el botón "Reprogramar" en Mis citas.

**Qué generó la IA:**
- `CitaDao.reprogramar(id, fecha, hora)`.
- `VetRepository.reprogramarCita(...)`, que devuelve `Boolean`. La implementación usa `db.withTransaction`: vuelve a contar ocupadas con `contarOcupadas` y solo entonces actualiza.
- `ReprogramarCitaViewModel` con la fecha, la hora, los horarios (con `flatMapLatest` + `combine`), la regla de 2 horas y `guardar()`.
- `ReprogramarCitaScreen`, la ruta `reprogramar/{id}` en `Rutas` y `NavVet`, y el botón en `MisCitasScreen`.

**Qué tuve que corregir yo:**
- La IA permitía guardar el mismo horario que ya tenía la cita. Se agregó la validación "Es el mismo horario".
- El botón "Reprogramar" aparecía aunque faltaran menos de 2 horas. Ahora solo aparece si `puedeCancelar(c)` es verdadero, y la pantalla también lo valida.
- La IA reutilizaba `HorarioUi`, que está definido en `PedirCitaViewModel.kt`. Se dejó así para no duplicar la clase.

**¿Respeta MVVM?:** Sí. Las reglas (2 horas, horario libre, mismo horario) están en el ViewModel y la doble verificación del horario está en el Repository. La pantalla no conoce el DAO.

## Prompt 3: Búsqueda de pacientes

**Texto enviado a la IA:**
Mejora la búsqueda de PacientesViewModel: que busque por nombre de la mascota, del dueño o raza al mismo tiempo que filtra por especie, que ignore mayúsculas y tildes, que ordene por nombre y que la pantalla muestre cuántos resultados hay y un botón para limpiar los filtros. Mantén el combine y no cambies el DAO.

**Qué generó la IA:**
- Una función `normalizar()` con `java.text.Normalizer` (forma NFD) que quita las tildes con la expresión regular `\p{Mn}+`.
- Búsqueda por palabras: cada palabra debe aparecer en "mascota + dueño + raza". Por ejemplo, "ana luna" encuentra a Luna de Ana García.
- `hayFiltros: StateFlow<Boolean>` y `limpiarFiltros()`.
- En `PacientesScreen`: el contador "N pacientes" y el botón "Limpiar filtros".

**Qué tuve que corregir yo:**
- La primera versión normalizaba el texto dentro del Composable, en cada recomposición. Se movió al ViewModel, dentro del `combine`.

**¿Respeta MVVM?:** Sí. Solo cambian el ViewModel y la View, como indica la tabla de mejoras. El DAO y el Repository no se tocaron.

## Reflexión

- La IA aceleró mucho el código repetitivo (DAO, Repository, pantallas con los componentes existentes) y propuso bien el uso de `withTransaction` y `flatMapLatest`.
- No siempre respetó dónde debe vivir cada regla: a veces ponía cálculos o validaciones en el Composable, y hubo que moverlos al ViewModel.
- También cometió errores de casos límite (división entre 0 en el gráfico, reprogramar al mismo horario) que solo se detectan probando.
- Aprendí que la interface `VetRepository` obliga a declarar primero cada función nueva. Eso mantiene ordenado el contrato y permitirá cambiar Room por un servicio web sin tocar los ViewModels.
- Las reglas de negocio (2 horas, horario único, estados de la cita) deben validarse en el ViewModel, y las que dependen de datos concurrentes, también en la transacción del Repository.
