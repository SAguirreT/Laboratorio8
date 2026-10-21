package com.tecsup.huellitasvet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.tecsup.huellitasvet.model.Rol

private val DuenoClaro = lightColorScheme(
    primary = EsmeraldaPrimario,
    onPrimary = Color.White,
    primaryContainer = EsmeraldaContenedor,
    onPrimaryContainer = EsmeraldaProfundo,
    secondary = EsmeraldaPrimario,
    secondaryContainer = EsmeraldaContenedor,
    background = EsmeraldaFondo,
    onBackground = Tinta,
    surface = Color.White,
    onSurface = Tinta,
    surfaceVariant = Superficie2,
    onSurfaceVariant = TintaSuave,
    outline = Borde,
    outlineVariant = Borde,
    error = EstadoCancelada
)

private val DuenoOscuro = darkColorScheme(
    primary = Color(0xFF34D399),
    onPrimary = Color(0xFF003826),
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = EsmeraldaContenedor,
    secondary = Color(0xFF34D399),
    secondaryContainer = Color(0xFF064E3B),
    background = Color(0xFF0B1512),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF13201B),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1E2D27),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF334155),
    error = Color(0xFFF87171)
)

private val VetClaro = lightColorScheme(
    primary = AzulPrimario,
    onPrimary = Color.White,
    primaryContainer = AzulContenedor,
    onPrimaryContainer = AzulProfundo,
    secondary = AzulPrimario,
    secondaryContainer = AzulContenedor,
    background = AzulFondo,
    onBackground = Tinta,
    surface = Color.White,
    onSurface = Tinta,
    surfaceVariant = Superficie2,
    onSurfaceVariant = TintaSuave,
    outline = Borde,
    outlineVariant = Borde,
    error = EstadoCancelada
)

private val VetOscuro = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF0B1E4A),
    primaryContainer = AzulProfundo,
    onPrimaryContainer = AzulContenedor,
    secondary = Color(0xFF60A5FA),
    secondaryContainer = AzulProfundo,
    background = Color(0xFF0B1120),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF111A2E),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1C2740),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF334155),
    error = Color(0xFFF87171)
)

/**
 * Entrega un ColorScheme según el perfil: esmeralda para el dueño y azul para el veterinario.
 * En las pantallas usa MaterialTheme.colorScheme.primary, nunca un color fijo.
 */
@Composable
fun VetTheme(
    rol: Rol = Rol.CLIENTE,
    oscuro: Boolean = false,
    content: @Composable () -> Unit
) {
    val esquema = when (rol) {
        Rol.CLIENTE -> if (oscuro) DuenoOscuro else DuenoClaro
        Rol.ADMIN -> if (oscuro) VetOscuro else VetClaro
    }
    MaterialTheme(colorScheme = esquema, typography = VetTypography, content = content)
}
