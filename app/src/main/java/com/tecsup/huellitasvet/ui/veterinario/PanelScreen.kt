package com.tecsup.huellitasvet.ui.veterinario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tecsup.huellitasvet.model.Fechas
import com.tecsup.huellitasvet.ui.components.Aviso
import com.tecsup.huellitasvet.ui.components.graficos.GraficoBarras
import com.tecsup.huellitasvet.ui.components.graficos.GraficoDona
import com.tecsup.huellitasvet.ui.components.graficos.LeyendaGrafico
import com.tecsup.huellitasvet.ui.theme.AmbarSuave
import com.tecsup.huellitasvet.ui.theme.EstadoAtendida
import com.tecsup.huellitasvet.ui.theme.EstadoPendiente
import com.tecsup.huellitasvet.ui.theme.Superficie2
import com.tecsup.huellitasvet.viewmodel.PanelViewModel
import java.time.LocalDate

@Composable
fun PanelScreen(vm: PanelViewModel, onVerAgenda: () -> Unit) {
    val citasHoy by vm.citasHoy.collectAsState()
    val pendientes by vm.pendientes.collectAsState()
    val atendidas by vm.atendidas.collectAsState()
    val ingresos by vm.ingresosMes.collectAsState()
    val porEstado by vm.porEstado.collectAsState()
    val porServicio by vm.porServicio.collectAsState()
    val colores = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Panel de la clínica", style = MaterialTheme.typography.headlineSmall)
                Text("Hoy · ${Fechas.larga(LocalDate.now().toString())}", color = colores.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Filled.Notifications, contentDescription = null)
        }

        // 3 contadores de color
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Contador(citasHoy, "Citas hoy", colores.primary, colores.primaryContainer, Modifier.weight(1f))
            Contador(pendientes, "Pendientes", EstadoPendiente, AmbarSuave, Modifier.weight(1f))
            Contador(atendidas, "Atendidas", EstadoAtendida, Superficie2, Modifier.weight(1f))
        }

        // Ingresos del mes
        Surface(shape = RoundedCornerShape(18.dp), color = colores.primary, contentColor = colores.onPrimary, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Text("Ingresos del mes", style = MaterialTheme.typography.labelMedium, color = colores.onPrimary.copy(alpha = 0.8f))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("S/ %,.2f".format(ingresos), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                    Text("$atendidas atenciones", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Tarjeta("Citas por estado") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GraficoDona(porEstado)
                LeyendaGrafico(porEstado, Modifier.weight(1f).padding(start = 20.dp))
            }
        }

        Tarjeta("Citas por servicio") {
            if (porServicio.isEmpty()) {
                Text("Sin datos", color = colores.onSurfaceVariant)
            } else {
                GraficoBarras(porServicio)
            }
        }

        if (pendientes > 0) {
            Aviso(
                icono = Icons.Filled.Warning,
                titulo = "$pendientes citas por confirmar",
                detalle = "Revisa la agenda"
            ) {
                Button(
                    onClick = onVerAgenda,
                    colors = ButtonDefaults.buttonColors(containerColor = EstadoPendiente, contentColor = Color.White)
                ) { Text("Ver") }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun Contador(valor: Int, etiqueta: String, color: Color, fondo: Color, modifier: Modifier) {
    Surface(shape = RoundedCornerShape(16.dp), color = fondo, modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 16.dp)) {
            Text(valor.toString(), color = color, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(etiqueta, color = color, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun Tarjeta(titulo: String, contenido: @Composable () -> Unit) {
    val colores = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(titulo, style = MaterialTheme.typography.titleMedium)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colores.surface,
            border = BorderStroke(1.dp, colores.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) { contenido() }
        }
    }
}
