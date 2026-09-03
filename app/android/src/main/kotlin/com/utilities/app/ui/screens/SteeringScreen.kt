package com.utilities.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.utilities.connection.ConnectionState
import com.utilities.steering.AndroidSteeringController
import com.utilities.steering.DeviceOrientation
import com.utilities.steering.SteeringPayload
import kotlinx.coroutines.launch
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SteeringScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val controller = remember { AndroidSteeringController(context) }
    DisposableEffect(Unit) {
        onDispose { controller.release() }
    }

    // Inicia los sensores al abrir la pantalla: la orientación se detecta en vivo
    // aunque el teléfono aún no esté conectado al PC.
    LaunchedEffect(Unit) {
        controller.startSensor()
    }

    var ip by remember { mutableStateOf("") }
    var isConnecting by remember { mutableStateOf(false) }

    // Observar el estado de la conexión (StateFlow)
    val connectionState by controller.connectionState.collectAsState()

    val isConnected = connectionState is ConnectionState.Connected

    val statusText = when {
        isConnecting -> "Conectando..."
        connectionState is ConnectionState.Connected -> "Conectado ✓"
        connectionState is ConnectionState.Connecting -> "Conectando..."
        connectionState is ConnectionState.Error -> "Error: ${(connectionState as ConnectionState.Error).message}"
        else -> "Desconectado"
    }

    // Último valor del volante leído por el sensor
    val steering by controller.steeringState.collectAsState()

    // Pose / orientación física del dispositivo
    val deviceOrientation by controller.orientation.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Volante") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = ip,
                onValueChange = { ip = it },
                label = { Text("IP del PC (ej: 192.168.1.100)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isConnected
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        if (ip.isBlank()) {
                            Toast.makeText(context, "Ingresá la IP del PC", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isConnecting = true
                        scope.launch {
                            try {
                                controller.connect(ip, 8080)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isConnecting = false
                            }
                        }
                    },
                    enabled = !isConnected && !isConnecting,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isConnecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Conectar")
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            controller.disconnect()
                            isConnecting = false
                        }
                    },
                    enabled = isConnected,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Desconectar")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isConnected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Visual del volante ──────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                SteeringArc(
                    roll = steering.roll,
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = "%.1f°".format(steering.roll),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Valores del giroscopio ──────────────────────────────
            // ── Orientación del dispositivo ─────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Posición del teléfono",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = orientationLabel(deviceOrientation),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Lectura del giroscopio",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(8.dp))
                    GyroValue("Roll (giro)", steering.roll, Color(0xFF2196F3))
                    GyroValue("Pitch", steering.pitch, Color(0xFF4CAF50))
                    GyroValue("Yaw", steering.yaw, Color(0xFFFF9800))
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun GyroValue(label: String, value: Float, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(8.dp)) {
            drawCircle(color = color)
        }
        Spacer(Modifier.width(8.dp))
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "%+.1f°".format(value),
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun orientationLabel(orientation: DeviceOrientation): String {
    return when (orientation) {
        DeviceOrientation.UNKNOWN -> "Desconocida"
        DeviceOrientation.FLAT_BACK -> "Horizontal (pantalla arriba) — modo volante ✓"
        DeviceOrientation.FLAT_FRONT -> "Horizontal (pantalla abajo)"
        DeviceOrientation.PORTRAIT -> "Vertical (parado)"
        DeviceOrientation.UPSIDE_DOWN -> "Vertical (invertido)"
        DeviceOrientation.LANDSCAPE_LEFT -> "De costado (izquierda)"
        DeviceOrientation.LANDSCAPE_RIGHT -> "De costado (derecha)"
    }
}

@Composable
private fun SteeringArc(roll: Float, modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline

    Canvas(modifier = modifier.padding(16.dp)) {
        val cx = size.width / 2
        val cy = size.height / 2
        val radius = min(cx, cy) - 16.dp.toPx()

        drawArc(
            color = outlineColor.copy(alpha = 0.15f),
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(cx - radius, cy - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = 4.dp.toPx())
        )

        for (angle in -90..90 step 30) {
            val rad = Math.toRadians((90 + angle).toDouble())
            val cos = kotlin.math.cos(rad).toFloat()
            val sin = kotlin.math.sin(rad).toFloat()
            val x1 = cx + (radius - 12.dp.toPx()) * cos
            val y1 = cy - (radius - 12.dp.toPx()) * sin
            val x2 = cx + (radius + 4.dp.toPx()) * cos
            val y2 = cy - (radius + 4.dp.toPx()) * sin
            drawLine(
                color = outlineColor.copy(alpha = 0.4f),
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 2.dp.toPx()
            )
        }

        val clampedRoll = roll.coerceIn(-90f, 90f)
        val needleAngle = Math.toRadians((90.0 - clampedRoll))
        val needleLen = radius * 0.85f
        val nx = cx + needleLen * kotlin.math.cos(needleAngle).toFloat()
        val ny = cy - needleLen * kotlin.math.sin(needleAngle).toFloat()

        drawLine(
            color = primaryColor,
            start = Offset(cx, cy),
            end = Offset(nx, ny),
            strokeWidth = 4.dp.toPx()
        )

        drawCircle(color = primaryColor, radius = 6.dp.toPx())
    }
}
