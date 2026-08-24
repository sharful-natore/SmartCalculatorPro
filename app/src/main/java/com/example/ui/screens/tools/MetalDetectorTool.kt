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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    var hasMagnetometer by remember { mutableStateOf(true) }
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

    // Waveform history
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

    // Register Sensor Listener
    DisposableEffect(Unit) {
        val magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (magSensor == null) {
            hasMagnetometer = false
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
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

                    // Append to waveform history (keep last 50 points)
                    if (historyData.size >= 50) {
                        historyData.removeAt(0)
                    }
                    historyData.add(effectiveValue)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (magSensor != null) {
            sensorManager.registerListener(listener, magSensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    val displayValue = if (isZeroModeActive) (totalMagnitude - baselineOffset).coerceAtLeast(0f) else totalMagnitude
    val isMetalDetected = displayValue >= thresholdVal

    // Sound and vibration alerts loop
    LaunchedEffect(isMetalDetected, isSoundEnabled, isVibrationEnabled, displayValue, thresholdVal) {
        while (isMetalDetected) {
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

    // Animated Dial Color
    val statusColor by animateColorAsState(
        targetValue = when {
            displayValue < thresholdVal * 0.75f -> Color(0xFF10B981) // Green: Natural
            displayValue < thresholdVal -> Color(0xFFF59E0B) // Amber: Moderate
            displayValue < thresholdVal * 1.5f -> Color(0xFFEF4444) // Red: High Metal
            else -> Color(0xFFDC2626) // Deep Crimson: Very Strong Magnetic Field
        },
        animationSpec = tween(300),
        label = "StatusColor"
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
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.cardBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header badge with detection status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isMetalDetected) (if (isBn) "মেটাল / চৌম্বক সনাক্ত!" else "Metal / Magnetic Field Detected!") else (if (isBn) "স্বাভাবিক স্তর" else "Normal Field"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    // Peak Indicator
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = themeColors.buttonEqualBg.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = String.format("Peak: %.1f μT", peakMagnitude),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = themeColors.buttonEqualBg,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Analog Dial Arc Gauge
                Box(
                    modifier = Modifier
                        .size(240.dp, 140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 14.dp.toPx()
                        val arcPadding = strokeWidth / 2
                        val arcSize = Size(size.width - strokeWidth, (size.height * 2) - strokeWidth)

                        // Background Arc (180 degrees)
                        drawArc(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(arcPadding, arcPadding),
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Progress Colored Gradient Arc
                        val progressSweep = ((displayValue / 200f).coerceIn(0f, 1f)) * 180f
                        drawArc(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFFDC2626))
                            ),
                            startAngle = 180f,
                            sweepAngle = progressSweep,
                            useCenter = false,
                            topLeft = Offset(arcPadding, arcPadding),
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Needle / Pointer
                        val centerX = size.width / 2
                        val centerY = size.height
                        val needleLength = size.height * 0.75f
                        val angleRad = Math.toRadians((animatedNeedleAngle - 90.0).toDouble())
                        val endX = centerX + (needleLength * cos(angleRad)).toFloat()
                        val endY = centerY + (needleLength * sin(angleRad)).toFloat()

                        drawLine(
                            color = statusColor,
                            start = Offset(centerX, centerY),
                            end = Offset(endX, endY),
                            strokeWidth = 4.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Center Pivot Dot
                        drawCircle(
                            color = statusColor,
                            radius = 9.dp.toPx(),
                            center = Offset(centerX, centerY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = Offset(centerX, centerY)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Numerical Readout & Units
                Text(
                    text = String.format(Locale.US, "%.1f", displayValue),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = statusColor
                )
                Text(
                    text = if (isBn) "মাইক্রোটেসলা (μT)" else "microTesla (μT)",
                    fontSize = 13.sp,
                    color = themeColors.displayText.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(14.dp))

                // X, Y, Z Vector Breakdown
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(themeColors.background.copy(alpha = 0.7f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "X Axis", fontSize = 11.sp, color = themeColors.displayText.copy(alpha = 0.5f))
                        Text(text = String.format("%.1f", rawX), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                    }
                    Divider(modifier = Modifier.height(26.dp).width(1.dp), color = themeColors.displayText.copy(alpha = 0.1f))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Y Axis", fontSize = 11.sp, color = themeColors.displayText.copy(alpha = 0.5f))
                        Text(text = String.format("%.1f", rawY), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                    }
                    Divider(modifier = Modifier.height(26.dp).width(1.dp), color = themeColors.displayText.copy(alpha = 0.1f))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Z Axis", fontSize = 11.sp, color = themeColors.displayText.copy(alpha = 0.5f))
                        Text(text = String.format("%.1f", rawZ), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                    }
                }
            }
        }

        // --- Real-time Waveform Chart Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .themeCardShadow(themeColors, elevation = 1.dp),
            shape = RoundedCornerShape(16.dp),
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
                    Text(
                        text = if (isBn) "রিয়েল-টাইম ম্যাগনেটিক ফ্ল্যাক্স গ্রাফ" else "Real-time Flux Waveform",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    Text(
                        text = if (isBn) "লাইভ ডাটা" else "LIVE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(themeColors.background)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (historyData.size > 1) {
                            val maxVal = 200f
                            val stepX = size.width / (50 - 1)
                            val path = Path()

                            val thresholdY = size.height - ((thresholdVal / maxVal).coerceIn(0f, 1f) * size.height)
                            // Draw threshold line
                            drawLine(
                                color = Color(0xFFEF4444).copy(alpha = 0.4f),
                                start = Offset(0f, thresholdY),
                                end = Offset(size.width, thresholdY),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )

                            historyData.forEachIndexed { index, value ->
                                val x = index * stepX
                                val y = size.height - ((value / maxVal).coerceIn(0f, 1f) * size.height)
                                if (index == 0) {
                                    path.moveTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                }
                            }

                            drawPath(
                                path = path,
                                color = statusColor,
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
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
            shape = RoundedCornerShape(16.dp),
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

                // Tare / Zero Baseline Button
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
                        "• নির্ভুল ফলাফলের জন্য 'Set Zero Baseline' চাপুন যাতে ব্যাকগ্রাউন্ড ফিল্ড জিরো হয়ে কেবল ধাতব অবজেক্টের পরিবর্তন ধরা পড়ে।\n" +
                        "• সেন্সর ক্যালিব্রেট করতে ফোনটিকে বাতাসে ইংরেজি '8' আকারে কয়েকবার ঘুরান।"
                    else
                        "• Earth's natural magnetic field is typically around 30 - 60 μT.\n" +
                        "• Ferromagnetic metals, electrical wiring, and magnets cause spikes up to 100 - 300+ μT.\n" +
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
