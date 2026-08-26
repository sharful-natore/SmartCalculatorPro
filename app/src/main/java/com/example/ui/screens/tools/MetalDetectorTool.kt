package com.example.ui.screens.tools

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.Locale
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.theme.themeCardShadow
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun MetalDetectorTool(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    var hasMagnetometer by remember { mutableStateOf(true) }
    var isAppInForeground by remember { mutableStateOf(true) }
    var isManuallyPaused by remember { mutableStateOf(false) }

    var rawX by remember { mutableFloatStateOf(0f) }
    var rawY by remember { mutableFloatStateOf(0f) }
    var rawZ by remember { mutableFloatStateOf(0f) }
    var totalMagnitude by remember { mutableFloatStateOf(0f) }
    var peakMagnitude by remember { mutableFloatStateOf(0f) }

    // Baseline Tare / Zero calibration
    var baselineOffset by remember { mutableFloatStateOf(0f) }
    var isZeroModeActive by remember { mutableStateOf(false) }

    // Settings
    var thresholdVal by remember { mutableFloatStateOf(80f) } // microTesla
    var isSoundEnabled by remember { mutableStateOf(true) }
    var isVibrationEnabled by remember { mutableStateOf(true) }

    // Waveform history (keep last 50 points)
    val historyData = remember { mutableStateListOf<Float>() }

    // Vibrator and Audio Tone Generator
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator ?: (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    val toneGenerator = remember {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (e: Exception) {
            null
        }
    }

    DisposableEffect(toneGenerator) {
        onDispose {
            try {
                toneGenerator?.release()
            } catch (e: Exception) { }
        }
    }

    // Lifecycle Observer: Automatically Pause when app is minimized or screen is off
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    isAppInForeground = true
                }
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    isAppInForeground = false
                    try {
                        vibrator?.cancel()
                    } catch (e: Exception) { }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val isDetectionActive = isAppInForeground && !isManuallyPaused

    // Register / Unregister Sensor Listener strictly based on active state
    DisposableEffect(isDetectionActive) {
        val magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (magSensor == null) {
            hasMagnetometer = false
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD && isDetectionActive) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val mag = sqrt(x * x + y * y + z * z)

                    rawX = x
                    rawY = y
                    rawZ = z
                    totalMagnitude = mag

                    val effectiveValue = if (isZeroModeActive) (mag - baselineOffset).coerceAtLeast(0f) else mag
                    if (effectiveValue > peakMagnitude) {
                        peakMagnitude = effectiveValue
                    }

                    // Append to waveform history
                    if (historyData.size >= 50) {
                        historyData.removeAt(0)
                    }
                    historyData.add(effectiveValue)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (magSensor != null && isDetectionActive) {
            sensorManager.registerListener(listener, magSensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
            try {
                vibrator?.cancel()
            } catch (e: Exception) { }
        }
    }

    val displayValue = if (isZeroModeActive) (totalMagnitude - baselineOffset).coerceAtLeast(0f) else totalMagnitude
    val isMetalDetected = isDetectionActive && displayValue >= thresholdVal

    // Sound and vibration alerts loop (only runs when active & foreground)
    LaunchedEffect(isMetalDetected, isSoundEnabled, isVibrationEnabled, displayValue, thresholdVal, isDetectionActive) {
        while (isMetalDetected && isDetectionActive) {
            val severity = ((displayValue - thresholdVal) / 100f).coerceIn(0f, 1f)
            val delayInterval = (350L - (severity * 250L)).toLong().coerceAtLeast(80L)

            if (isSoundEnabled && toneGenerator != null) {
                try {
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
                } catch (e: Exception) {}
            }

            if (isVibrationEnabled && vibrator != null && vibrator.hasVibrator()) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val amplitude = (100 + (severity * 155)).toInt().coerceIn(1, 255)
                        vibrator.vibrate(VibrationEffect.createOneShot(45, amplitude))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(45)
                    }
                } catch (e: Exception) {}
            }

            delay(delayInterval)
        }
    }

    // Animated Needle Angle
    val targetAngle = ((displayValue / 200f).coerceIn(0f, 1f) * 180f) - 90f
    val animatedNeedleAngle by animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "NeedleAngle"
    )

    // Animated Status Color
    val statusColor by animateColorAsState(
        targetValue = when {
            !isDetectionActive -> Color(0xFF94A3B8)
            displayValue < thresholdVal * 0.75f -> Color(0xFF10B981) // Green: Natural
            displayValue < thresholdVal -> Color(0xFFF59E0B) // Amber: Moderate
            displayValue < thresholdVal * 1.5f -> Color(0xFFEF4444) // Red: High Metal
            else -> Color(0xFFDC2626) // Deep Crimson: Very Strong Magnetic Field
        },
        animationSpec = tween(300),
        label = "StatusColor"
    )

    // Radar pulse animation when metal is detected
    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!hasMagnetometer) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isBn) "আপনার ফোনে ম্যাগনেটোমিটার / কম্পাস সেন্সর পাওয়া যায়নি।" else "Magnetometer sensor is not available on this device.",
                        color = Color(0xFF991B1B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // --- Main Visual Gauge Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .themeCardShadow(themeColors, elevation = 2.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.cardBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header badge with detection status & pause button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                !isDetectionActive -> if (isBn) "ডিটেক্টর পজ আছে" else "Detector Paused"
                                isMetalDetected -> if (isBn) "মেটাল / চৌম্বক সনাক্ত!" else "Metal / High Flux Detected!"
                                else -> if (isBn) "স্বাভাবিক চৌম্বক স্তর" else "Normal Magnetic Level"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    // Pause / Play Toggle Button
                    IconButton(
                        onClick = { isManuallyPaused = !isManuallyPaused },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isManuallyPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isManuallyPaused) "Resume" else "Pause",
                            tint = if (isManuallyPaused) Color(0xFF10B981) else themeColors.displayText.copy(alpha = 0.7f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // High-Precision Analog Gauge Meter with Subdivisions & Ticks
                Box(
                    modifier = Modifier
                        .size(280.dp, 160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 14.dp.toPx()
                        val arcPadding = strokeWidth / 2 + 10.dp.toPx()
                        val arcSize = Size(size.width - (arcPadding * 2), (size.height * 2) - (arcPadding * 2))
                        val centerX = size.width / 2
                        val centerY = size.height - 10.dp.toPx()
                        val radius = (size.width - (arcPadding * 2)) / 2

                        // 1. Draw Dial Track Background
                        drawArc(
                            color = Color.LightGray.copy(alpha = 0.25f),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(arcPadding, arcPadding),
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // 2. Draw Colored Multi-Stop Gradient Progress Arc
                        val progressSweep = ((displayValue / 200f).coerceIn(0f, 1f)) * 180f
                        if (progressSweep > 0.5f) {
                            drawArc(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF10B981), // Emerald
                                        Color(0xFF84CC16), // Lime
                                        Color(0xFFF59E0B), // Amber
                                        Color(0xFFF97316), // Orange
                                        Color(0xFFEF4444), // Red
                                        Color(0xFFDC2626)  // Crimson
                                    )
                                ),
                                startAngle = 180f,
                                sweepAngle = progressSweep,
                                useCenter = false,
                                topLeft = Offset(arcPadding, arcPadding),
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        // 3. Draw Precision Tick Marks & Dial Labels (0, 40, 80, 120, 160, 200)
                        val totalTicks = 20
                        val textPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 9.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                        }

                        for (i in 0..totalTicks) {
                            val tickFraction = i / totalTicks.toFloat()
                            val angleDeg = 180f + (tickFraction * 180f)
                            val angleRad = Math.toRadians(angleDeg.toDouble())
                            val isMajor = i % 4 == 0

                            val tickLen = if (isMajor) 10.dp.toPx() else 5.dp.toPx()
                            val startR = radius + (strokeWidth / 2) + 2.dp.toPx()
                            val endR = startR + tickLen

                            val x1 = centerX + (startR * cos(angleRad)).toFloat()
                            val y1 = centerY + (startR * sin(angleRad)).toFloat()
                            val x2 = centerX + (endR * cos(angleRad)).toFloat()
                            val y2 = centerY + (endR * sin(angleRad)).toFloat()

                            drawLine(
                                color = if (isMajor) Color.Gray else Color.Gray.copy(alpha = 0.4f),
                                start = Offset(x1, y1),
                                end = Offset(x2, y2),
                                strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                                cap = StrokeCap.Round
                            )

                            if (isMajor) {
                                val labelR = endR + 10.dp.toPx()
                                val lx = centerX + (labelR * cos(angleRad)).toFloat()
                                val ly = centerY + (labelR * sin(angleRad)).toFloat() + 3.dp.toPx()
                                val labelVal = (tickFraction * 200).toInt().toString()
                                drawContext.canvas.nativeCanvas.drawText(labelVal, lx, ly, textPaint)
                            }
                        }

                        // 4. Draw Threshold Indicator Arrow Marker
                        val threshFraction = (thresholdVal / 200f).coerceIn(0f, 1f)
                        val threshAngleRad = Math.toRadians((180f + (threshFraction * 180f)).toDouble())
                        val threshOuterR = radius - (strokeWidth / 2) - 4.dp.toPx()
                        val threshInnerR = threshOuterR - 8.dp.toPx()
                        val tx1 = centerX + (threshOuterR * cos(threshAngleRad)).toFloat()
                        val ty1 = centerY + (threshOuterR * sin(threshAngleRad)).toFloat()
                        val tx2 = centerX + (threshInnerR * cos(threshAngleRad)).toFloat()
                        val ty2 = centerY + (threshInnerR * sin(threshAngleRad)).toFloat()
                        drawLine(
                            color = Color(0xFFEF4444),
                            start = Offset(tx1, ty1),
                            end = Offset(tx2, ty2),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // 5. Draw Precision Aerodynamic Needle
                        val needleLength = radius * 0.88f
                        val angleRad = Math.toRadians((animatedNeedleAngle - 90.0).toDouble())
                        val needleEndX = centerX + (needleLength * cos(angleRad)).toFloat()
                        val needleEndY = centerY + (needleLength * sin(angleRad)).toFloat()

                        // Shadow / Glow behind needle
                        drawLine(
                            color = statusColor.copy(alpha = 0.35f),
                            start = Offset(centerX, centerY),
                            end = Offset(needleEndX, needleEndY),
                            strokeWidth = 6.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Solid needle pointer
                        drawLine(
                            color = statusColor,
                            start = Offset(centerX, centerY),
                            end = Offset(needleEndX, needleEndY),
                            strokeWidth = 3.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // 6. Center Hub / Pivot Ring with Metallic Accent
                        drawCircle(
                            color = statusColor.copy(alpha = 0.3f),
                            radius = 16.dp.toPx(),
                            center = Offset(centerX, centerY)
                        )
                        drawCircle(
                            color = Color(0xFF1E293B),
                            radius = 11.dp.toPx(),
                            center = Offset(centerX, centerY)
                        )
                        drawCircle(
                            color = statusColor,
                            radius = 6.dp.toPx(),
                            center = Offset(centerX, centerY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.5.dp.toPx(),
                            center = Offset(centerX, centerY)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // High-Contrast Digital HUD Readout
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f", displayValue),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = statusColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "μT",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Text(
                    text = if (isBn) "মাইক্রোটেসলা (ম্যাগনেটিক ফ্লাক্স ডেনসিটি)" else "microTesla (Magnetic Flux Density)",
                    fontSize = 12.sp,
                    color = themeColors.displayText.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(14.dp))

                // X, Y, Z Vector Breakdown Cards
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(themeColors.background.copy(alpha = 0.8f))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "X Axis", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = themeColors.displayText.copy(alpha = 0.5f))
                        Text(text = String.format(Locale.US, "%.1f", rawX), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                    }
                    Divider(modifier = Modifier.height(28.dp).width(1.dp), color = themeColors.displayText.copy(alpha = 0.12f))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Y Axis", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = themeColors.displayText.copy(alpha = 0.5f))
                        Text(text = String.format(Locale.US, "%.1f", rawY), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                    }
                    Divider(modifier = Modifier.height(28.dp).width(1.dp), color = themeColors.displayText.copy(alpha = 0.12f))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Z Axis", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = themeColors.displayText.copy(alpha = 0.5f))
                        Text(text = String.format(Locale.US, "%.1f", rawZ), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                    }
                    Divider(modifier = Modifier.height(28.dp).width(1.dp), color = themeColors.displayText.copy(alpha = 0.12f))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Peak", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFEF4444))
                        Text(text = String.format(Locale.US, "%.1f", peakMagnitude), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    }
                }
            }
        }

        // --- Real-time Professional Oscilloscope Waveform Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .themeCardShadow(themeColors, elevation = 1.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.cardBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "রিয়েল-টাইম ফ্ল্যাক্স অসিলোস্কোপ" else "Real-time Flux Oscilloscope",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isDetectionActive) Color(0xFF10B981).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isDetectionActive) Color(0xFF10B981) else Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isDetectionActive) (if (isBn) "লাইভ" else "LIVE") else (if (isBn) "পজ" else "PAUSED"),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDetectionActive) Color(0xFF10B981) else Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Oscilloscope Screen Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val maxVal = 200f
                        val w = size.width
                        val h = size.height

                        // 1. Draw Oscilloscope Grid Lines (Horizontal & Vertical)
                        val gridColor = Color(0xFF1E293B)
                        for (i in 1..3) {
                            val y = (h / 4) * i
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        for (i in 1..7) {
                            val x = (w / 8) * i
                            drawLine(
                                color = gridColor,
                                start = Offset(x, 0f),
                                end = Offset(x, h),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // 2. Draw Dashed Threshold Line with Value Tag
                        val thresholdY = h - ((thresholdVal / maxVal).coerceIn(0f, 1f) * h)
                        drawLine(
                            color = Color(0xFFEF4444).copy(alpha = 0.6f),
                            start = Offset(0f, thresholdY),
                            end = Offset(w, thresholdY),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )

                        // 3. Draw Waveform Path with Smooth Bezier & Under-Curve Luminous Gradient
                        if (historyData.size > 1) {
                            val stepX = w / (50 - 1)
                            val wavePath = Path()
                            val fillPath = Path()

                            val points = historyData.mapIndexed { index, value ->
                                val x = index * stepX
                                val y = h - ((value / maxVal).coerceIn(0f, 1f) * h)
                                Offset(x, y)
                            }

                            wavePath.moveTo(points[0].x, points[0].y)
                            fillPath.moveTo(points[0].x, h)
                            fillPath.lineTo(points[0].x, points[0].y)

                            for (i in 0 until points.size - 1) {
                                val p0 = points[i]
                                val p1 = points[i + 1]
                                val cx = (p0.x + p1.x) / 2
                                wavePath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                                fillPath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                            }

                            fillPath.lineTo(points.last().x, h)
                            fillPath.close()

                            // Luminous Area Gradient under waveform
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    listOf(
                                        statusColor.copy(alpha = 0.35f),
                                        statusColor.copy(alpha = 0.05f),
                                        Color.Transparent
                                    )
                                )
                            )

                            // Glowing Waveform Stroke
                            drawPath(
                                path = wavePath,
                                color = statusColor.copy(alpha = 0.35f),
                                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                            drawPath(
                                path = wavePath,
                                color = statusColor,
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )

                            // Pulse ping ring and leading bright dot at newest reading
                            val lastPoint = points.last()
                            drawCircle(
                                color = statusColor.copy(alpha = 0.3f),
                                radius = 7.dp.toPx(),
                                center = lastPoint
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3.dp.toPx(),
                                center = lastPoint
                            )
                        }
                    }
                }
            }
        }

        // --- Controls & Calibration Settings Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .themeCardShadow(themeColors, elevation = 1.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.cardBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (isBn) "ক্যালিব্রেশন ও সেটিংস" else "Calibration & Controls",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )

                // Tare / Zero Baseline Button & Reset Peak
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (isZeroModeActive) {
                                isZeroModeActive = false
                                baselineOffset = 0f
                            } else {
                                baselineOffset = totalMagnitude
                                isZeroModeActive = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isZeroModeActive) Color(0xFFEF4444) else themeColors.buttonEqualBg,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isZeroModeActive) Icons.Default.Close else Icons.Default.FilterCenterFocus,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isZeroModeActive) (if (isBn) "রিসেট জিরো" else "Reset Zero") else (if (isBn) "জিরো সেট (Tare)" else "Set Zero Baseline"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = { peakMagnitude = displayValue },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = themeColors.displayText
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.35f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = themeColors.displayText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "পিক রিসেট" else "Reset Peak",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = themeColors.displayText
                        )
                    }
                }

                // Threshold Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBn) "অ্যালার্ট থ্রেশহোল্ড (সংবেদনশীলতা):" else "Detection Alert Threshold:",
                            fontSize = 12.sp,
                            color = themeColors.displayText.copy(alpha = 0.75f)
                        )
                        Text(
                            text = "${thresholdVal.toInt()} μT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.buttonEqualBg
                        )
                    }
                    Slider(
                        value = thresholdVal,
                        onValueChange = { thresholdVal = it },
                        valueRange = 40f..180f,
                        colors = SliderDefaults.colors(
                            thumbColor = themeColors.buttonEqualBg,
                            activeTrackColor = themeColors.buttonEqualBg
                        )
                    )
                }

                // Sound & Vibration Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = null,
                            tint = themeColors.displayText.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "বিপ সাউন্ড অ্যালার্ট" else "Audio Beep Alert",
                            fontSize = 13.sp,
                            color = themeColors.displayText
                        )
                    }
                    Switch(
                        checked = isSoundEnabled,
                        onCheckedChange = { isSoundEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = themeColors.buttonEqualBg
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = themeColors.displayText.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "হ্যাপটিক ভাইব্রেশন" else "Haptic Vibration",
                            fontSize = 13.sp,
                            color = themeColors.displayText
                        )
                    }
                    Switch(
                        checked = isVibrationEnabled,
                        onCheckedChange = { isVibrationEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = themeColors.buttonEqualBg
                        )
                    )
                }
            }
        }

        // --- Usage Guidelines Card ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.cardBg.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = themeColors.buttonEqualBg,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "ব্যবহার নির্দেশিকা ও পরামর্শ" else "Usage Tips & Instructions",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                }

                Text(
                    text = if (isBn)
                        "• পৃথিবীর স্বাভাবিক চৌম্বক ক্ষেত্র সাধারণত ৩০-৬০ μT এর মধ্যে থাকে।\n" +
                        "• লোহা, ইস্পাত, ধাতব বস্তু বা ইলেকট্রিক তারের কাছে ফোন আনলে চৌম্বক মান বেড়ে ১০০-৩০০+ μT হয়।\n" +
                        "• স্ক্রিন অফ বা অ্যাপ মিনিমাইজ করলে স্বয়ংক্রিয়ভাবে সেন্সর ও অ্যালার্ট পজ হয়ে ব্যাটারি বাঁচায়।\n" +
                        "• নির্ভুল ফলাফলের জন্য 'Set Zero Baseline' চাপুন যাতে ব্যাকগ্রাউন্ড ফিল্ড জিরো হয়ে কেবল ধাতব অবজেক্টের পরিবর্তন ধরা পড়ে।\n" +
                        "• সেন্সর ক্যালিব্রেট করতে ফোনটিকে বাতাসে ইংরেজি '8' আকারে কয়েকবার ঘুরান।"
                    else
                        "• Earth's natural magnetic field is typically around 30 - 60 μT.\n" +
                        "• Ferromagnetic metals, electrical wiring, and magnets cause spikes up to 100 - 300+ μT.\n" +
                        "• Turns sensor and alerts off automatically when the screen is off or app minimized to conserve battery.\n" +
                        "• Tap 'Set Zero Baseline' to tare the natural ambient field and detect subtle metal anomalies.\n" +
                        "• To calibrate the magnetometer, wave your device in a figure-8 motion.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = themeColors.displayText.copy(alpha = 0.7f)
                )
            }
        }
    }
}
