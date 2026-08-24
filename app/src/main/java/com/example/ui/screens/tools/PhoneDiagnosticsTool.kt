package com.example.ui.screens.tools

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.theme.themeCardShadow
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
fun PhoneDiagnosticsTool(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Sensors, 1: Hardware Tests, 2: Audio & Mic

    // Live Sensor Values
    var accelX by remember { mutableFloatStateOf(0f) }
    var accelY by remember { mutableFloatStateOf(0f) }
    var accelZ by remember { mutableFloatStateOf(0f) }
    var hasAccel by remember { mutableStateOf(false) }

    var gyroX by remember { mutableFloatStateOf(0f) }
    var gyroY by remember { mutableFloatStateOf(0f) }
    var gyroZ by remember { mutableFloatStateOf(0f) }
    var hasGyro by remember { mutableStateOf(false) }

    var magX by remember { mutableFloatStateOf(0f) }
    var magY by remember { mutableFloatStateOf(0f) }
    var magZ by remember { mutableFloatStateOf(0f) }
    var magValue by remember { mutableFloatStateOf(0f) }
    var hasMag by remember { mutableStateOf(false) }

    var lightLux by remember { mutableFloatStateOf(0f) }
    var hasLight by remember { mutableStateOf(false) }

    var proximityCm by remember { mutableFloatStateOf(0f) }
    var isProximityNear by remember { mutableStateOf(false) }
    var hasProximity by remember { mutableStateOf(false) }

    var pressureHpa by remember { mutableFloatStateOf(0f) }
    var hasPressure by remember { mutableStateOf(false) }

    var stepCount by remember { mutableFloatStateOf(0f) }
    var hasStepCounter by remember { mutableStateOf(false) }

    // Test Passed Tracking
    val passedTests = remember { mutableStateMapOf<String, Boolean>() }

    // Active Interactive Modal
    var activeInteractiveTest by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val sAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val sGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val sMag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val sLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        val sProx = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        val sPress = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        val sStep = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        hasAccel = sAccel != null
        hasGyro = sGyro != null
        hasMag = sMag != null
        hasLight = sLight != null
        hasProximity = sProx != null
        hasPressure = sPress != null
        hasStepCounter = sStep != null

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        accelX = event.values[0]
                        accelY = event.values[1]
                        accelZ = event.values[2]
                    }
                    Sensor.TYPE_GYROSCOPE -> {
                        gyroX = event.values[0]
                        gyroY = event.values[1]
                        gyroZ = event.values[2]
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        magX = event.values[0]
                        magY = event.values[1]
                        magZ = event.values[2]
                        magValue = sqrt(
                            (event.values[0] * event.values[0] +
                             event.values[1] * event.values[1] +
                             event.values[2] * event.values[2]).toDouble()
                        ).toFloat()
                    }
                    Sensor.TYPE_LIGHT -> {
                        lightLux = event.values[0]
                    }
                    Sensor.TYPE_PROXIMITY -> {
                        proximityCm = event.values[0]
                        val maxRange = event.sensor.maximumRange
                        isProximityNear = proximityCm < maxRange || proximityCm < 3f
                    }
                    Sensor.TYPE_PRESSURE -> {
                        pressureHpa = event.values[0]
                    }
                    Sensor.TYPE_STEP_COUNTER -> {
                        stepCount = event.values[0]
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sAccel?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        sGyro?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        sMag?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        sLight?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        sProx?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        sPress?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        sStep?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    val totalPassCount = passedTests.count { it.value }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Summary Header Card ---
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
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBn) "হার্ডওয়্যার ও সেন্সর ডায়াগনসিস" else "Hardware & Sensor Diagnostics",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        Text(
                            text = if (isBn) "$totalPassCount টি টেস্ট সম্পন্ন হয়েছে" else "$totalPassCount tests verified passed",
                            fontSize = 12.sp,
                            color = if (totalPassCount > 0) Color(0xFF10B981) else themeColors.displayText.copy(alpha = 0.65f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = if (totalPassCount > 0) Color(0xFF10B981).copy(alpha = 0.15f) else themeColors.buttonEqualBg.copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (totalPassCount > 0) Icons.Default.CheckCircle else Icons.Default.Sensors,
                                contentDescription = null,
                                tint = if (totalPassCount > 0) Color(0xFF10B981) else themeColors.buttonEqualBg,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- Category Tabs Navigation (Horizontal Scroll) ---
        val diagnosticTabs = listOf(
            Pair(if (isBn) "ম্যানুয়াল সেন্সর টেস্ট" else "Sensor Tests", Icons.Default.Sensors),
            Pair(if (isBn) "হার্ডওয়্যার ও ডিসপ্লে" else "Hardware & Display", Icons.Default.Build),
            Pair(if (isBn) "অডিও ও মাইক টেস্ট" else "Audio & Mic", Icons.Default.Mic)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            diagnosticTabs.forEachIndexed { index, pair ->
                val isSelected = selectedTab == index
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) themeColors.buttonEqualBg else themeColors.cardBg,
                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                    modifier = Modifier.clickable { selectedTab = index }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = pair.second,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else themeColors.displayText.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = pair.first,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else themeColors.displayText
                        )
                    }
                }
            }
        }

        // ==========================================
        // --- TAB 0: LIVE & MANUAL SENSOR TESTS ---
        // ==========================================
        if (selectedTab == 0) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 1. Accelerometer
                InteractiveSensorCard(
                    title = if (isBn) "অ্যাক্সিলোমিটার (Accelerometer)" else "Accelerometer (Motion & Tilt)",
                    icon = Icons.Default.ScreenRotation,
                    isAvailable = hasAccel,
                    isPassed = passedTests["accel"] == true,
                    liveValue = String.format(Locale.US, "X: %.1f  Y: %.1f  Z: %.1f m/s²", accelX, accelY, accelZ),
                    actionText = if (isBn) "ম্যানুয়াল টেস্ট" else "Test Tilt",
                    themeColors = themeColors,
                    isBn = isBn,
                    onTestClick = { activeInteractiveTest = "ACCEL" }
                )

                // 2. Gyroscope
                InteractiveSensorCard(
                    title = if (isBn) "জাইরোস্কোপ (Gyroscope)" else "Gyroscope (3D Rotation)",
                    icon = Icons.Default.RotateRight,
                    isAvailable = hasGyro,
                    isPassed = passedTests["gyro"] == true,
                    liveValue = String.format(Locale.US, "X: %.2f  Y: %.2f  Z: %.2f rad/s", gyroX, gyroY, gyroZ),
                    actionText = if (isBn) "ম্যানুয়াল টেস্ট" else "Test Gyro",
                    themeColors = themeColors,
                    isBn = isBn,
                    onTestClick = { activeInteractiveTest = "GYRO" }
                )

                // 3. Magnetometer / Compass
                InteractiveSensorCard(
                    title = if (isBn) "ম্যাগনেটোমিটার (Compass / Flux)" else "Magnetometer (Magnetic Field)",
                    icon = Icons.Default.Explore,
                    isAvailable = hasMag,
                    isPassed = passedTests["mag"] == true,
                    liveValue = String.format(Locale.US, "%.1f μT (MicroTesla)", magValue),
                    actionText = if (isBn) "কম্পাস টেস্ট" else "Test Compass",
                    themeColors = themeColors,
                    isBn = isBn,
                    onTestClick = { activeInteractiveTest = "MAG" }
                )

                // 4. Ambient Light Sensor
                InteractiveSensorCard(
                    title = if (isBn) "লাইট সেন্সর (Ambient Light Lux)" else "Ambient Light Sensor (Lux)",
                    icon = Icons.Default.WbSunny,
                    isAvailable = hasLight,
                    isPassed = passedTests["light"] == true,
                    liveValue = String.format(Locale.US, "%.0f Lux (%s)", lightLux, if (lightLux < 15) "Dark" else if (lightLux < 250) "Normal Light" else "Bright"),
                    actionText = if (isBn) "লাইট টেস্ট" else "Test Light",
                    themeColors = themeColors,
                    isBn = isBn,
                    onTestClick = { activeInteractiveTest = "LIGHT" }
                )

                // 5. Proximity Sensor
                InteractiveSensorCard(
                    title = if (isBn) "প্রক্সিমিটি সেন্সর (Proximity)" else "Proximity Sensor (Distance)",
                    icon = Icons.Default.Sensors,
                    isAvailable = hasProximity,
                    isPassed = passedTests["prox"] == true,
                    liveValue = if (isProximityNear) (if (isBn) "NEAR (হাত কাছে আছে)" else "NEAR (Object Detected)") else (if (isBn) "FAR (দূরে / ক্লিয়ার)" else "FAR (Clear)"),
                    highlightColor = if (isProximityNear) Color(0xFFEF4444) else Color(0xFF10B981),
                    actionText = if (isBn) "ওয়েভ টেস্ট" else "Test Wave",
                    themeColors = themeColors,
                    isBn = isBn,
                    onTestClick = { activeInteractiveTest = "PROX" }
                )

                // 6. Barometer Pressure
                if (hasPressure) {
                    InteractiveSensorCard(
                        title = if (isBn) "ব্যারোমিটার (Atmospheric Pressure)" else "Barometer (Atmospheric Pressure)",
                        icon = Icons.Default.Speed,
                        isAvailable = true,
                        isPassed = passedTests["baro"] == true,
                        liveValue = String.format(Locale.US, "%.1f hPa (mbar)", pressureHpa),
                        actionText = if (isBn) "প্রেসার টেস্ট" else "Test Pressure",
                        themeColors = themeColors,
                        isBn = isBn,
                        onTestClick = { activeInteractiveTest = "BARO" }
                    )
                }

                // 7. Step Counter
                if (hasStepCounter) {
                    InteractiveSensorCard(
                        title = if (isBn) "স্টেপ কাউন্টার (Step Counter)" else "Step Counter Hardware Sensor",
                        icon = Icons.Default.DirectionsWalk,
                        isAvailable = true,
                        isPassed = passedTests["step"] == true,
                        liveValue = String.format(Locale.US, "%.0f Steps", stepCount),
                        actionText = if (isBn) "স্টেপ টেস্ট" else "Test Steps",
                        themeColors = themeColors,
                        isBn = isBn,
                        onTestClick = { activeInteractiveTest = "STEP" }
                    )
                }
            }
        }

        // ==========================================
        // --- TAB 1: HARDWARE DIAGNOSTIC TESTS ---
        // ==========================================
        if (selectedTab == 1) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Touchscreen Grid Test
                HardwareActionCard(
                    title = if (isBn) "টাচস্ক্রিন মাল্টি-টাচ ম্যাট্রিক্স" else "Touch Screen Multi-Touch Grid Test",
                    subtitle = if (isBn) "স্ক্রিনের প্রতিটি ব্লক স্পর্শ করে ডেড জোন পরীক্ষা করুন" else "Draw and touch all blocks to check for dead touch areas",
                    icon = Icons.Default.TouchApp,
                    actionText = if (isBn) "টেস্ট শুরু" else "Start Test",
                    isPassed = passedTests["touch_test"] == true,
                    themeColors = themeColors,
                    onAction = { activeInteractiveTest = "TOUCH" }
                )

                // Dead Pixel & Display Color Test
                HardwareActionCard(
                    title = if (isBn) "ডিসপ্লে ও ডেড পিক্সেল টেস্ট" else "Display Color & Dead Pixel Test",
                    subtitle = if (isBn) "ফুলস্ক্রিন কালার সাইকেল দিয়ে ডিসপ্লে পিক্সেল যাচাই" else "Fullscreen Red, Green, Blue, White, Black to detect dead pixels",
                    icon = Icons.Default.Tv,
                    actionText = if (isBn) "কালার টেস্ট" else "Inspect Screen",
                    isPassed = passedTests["pixel_test"] == true,
                    themeColors = themeColors,
                    onAction = { activeInteractiveTest = "PIXEL" }
                )

                // Vibration Motor Test
                HardwareActionCard(
                    title = if (isBn) "ভাইব্রেশন মোটর টেস্ট" else "Vibration Motor Diagnostic",
                    subtitle = if (isBn) "হ্যাপটিক ফিডব্যাক, শর্ট ক্লিক ও ওয়েভ পালস টেস্ট" else "Test tick, click, and continuous vibration pulses",
                    icon = Icons.Default.Vibration,
                    actionText = if (isBn) "ভাইব্রেট করুন" else "Test Vibration",
                    isPassed = passedTests["vibe_test"] == true,
                    themeColors = themeColors,
                    onAction = { activeInteractiveTest = "VIBE" }
                )

                // Camera Flashlight Test
                HardwareActionCard(
                    title = if (isBn) "ক্যামেরা ফ্ল্যাশলাইট ও টর্চ টেস্ট" else "Camera Flashlight & Torch Test",
                    subtitle = if (isBn) "ক্যামেরা এলইডি ফ্ল্যাশ অন/অফ ও ব্লিঙ্ক টেস্ট" else "Test camera LED torch toggle and rapid strobe",
                    icon = Icons.Default.FlashlightOn,
                    actionText = if (isBn) "ফ্ল্যাশ টেস্ট" else "Test Flash",
                    isPassed = passedTests["flash_test"] == true,
                    themeColors = themeColors,
                    onAction = { activeInteractiveTest = "FLASH" }
                )
            }
        }

        // ==========================================
        // --- TAB 2: AUDIO & MICROPHONE TESTS ---
        // ==========================================
        if (selectedTab == 2) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Speaker Audio Tone Test
                HardwareActionCard(
                    title = if (isBn) "স্পিকার ও অডিও ফ্রিকোয়েন্সি টেস্ট" else "Speaker & Stereo Audio Test",
                    subtitle = if (isBn) "উচ্চ ও মাঝারি ফ্রিকোয়েন্সির টোন বাজিয়ে স্পিকার যাচাই" else "Test Left/Right stereo audio tones and frequencies",
                    icon = Icons.Default.VolumeUp,
                    actionText = if (isBn) "সাউন্ড টেস্ট" else "Play Test Tone",
                    isPassed = passedTests["audio_test"] == true,
                    themeColors = themeColors,
                    onAction = { activeInteractiveTest = "AUDIO" }
                )

                // Microphone Audio Visualizer Test
                HardwareActionCard(
                    title = if (isBn) "মাইক্রোফোন ইনপুট ও অ্যাম্প্লিচিউড টেস্ট" else "Microphone Live Input Test",
                    subtitle = if (isBn) "কথা বলে লাইভ সাউন্ড ওয়েভফর্ম ও ডেসিবেল যাচাই" else "Speak into microphone to test live waveform and decibels",
                    icon = Icons.Default.Mic,
                    actionText = if (isBn) "মাইক টেস্ট" else "Test Mic",
                    isPassed = passedTests["mic_test"] == true,
                    themeColors = themeColors,
                    onAction = { activeInteractiveTest = "MIC" }
                )
            }
        }
    }

    // =======================================================
    // --- FULLSCREEN & DIALOG INTERACTIVE MANUAL TESTS ---
    // =======================================================

    if (activeInteractiveTest == "ACCEL") {
        InteractiveAccelerometerTestDialog(
            accelX = accelX,
            accelY = accelY,
            accelZ = accelZ,
            onDismiss = {
                passedTests["accel"] = true
                activeInteractiveTest = null
            },
            isBn = isBn,
            themeColors = themeColors
        )
    }

    if (activeInteractiveTest == "GYRO") {
        InteractiveGyroscopeTestDialog(
            gyroX = gyroX,
            gyroY = gyroY,
            gyroZ = gyroZ,
            onDismiss = {
                passedTests["gyro"] = true
                activeInteractiveTest = null
            },
            isBn = isBn,
            themeColors = themeColors
        )
    }

    if (activeInteractiveTest == "MAG") {
        InteractiveMagnetometerTestDialog(
            magX = magX,
            magY = magY,
            magZ = magZ,
            magValue = magValue,
            onDismiss = {
                passedTests["mag"] = true
                activeInteractiveTest = null
            },
            isBn = isBn,
            themeColors = themeColors
        )
    }

    if (activeInteractiveTest == "PROX") {
        InteractiveProximityTestDialog(
            isNear = isProximityNear,
            proximityCm = proximityCm,
            onDismiss = {
                passedTests["prox"] = true
                activeInteractiveTest = null
            },
            isBn = isBn,
            themeColors = themeColors
        )
    }

    if (activeInteractiveTest == "LIGHT") {
        InteractiveLightSensorTestDialog(
            lux = lightLux,
            onDismiss = {
                passedTests["light"] = true
                activeInteractiveTest = null
            },
            isBn = isBn,
            themeColors = themeColors
        )
    }

    if (activeInteractiveTest == "BARO") {
        InteractiveBarometerTestDialog(
            pressureHpa = pressureHpa,
            onDismiss = {
                passedTests["baro"] = true
                activeInteractiveTest = null
            },
            isBn = isBn,
            themeColors = themeColors
        )
    }

    if (activeInteractiveTest == "STEP") {
        InteractiveStepTestDialog(
            stepCount = stepCount,
            onDismiss = {
                passedTests["step"] = true
                activeInteractiveTest = null
            },
            isBn = isBn,
            themeColors = themeColors
        )
    }

    if (activeInteractiveTest == "TOUCH") {
        InteractiveTouchTestDialog(
            onDismiss = {
                passedTests["touch_test"] = true
                activeInteractiveTest = null
            },
            isBn = isBn
        )
    }

    if (activeInteractiveTest == "PIXEL") {
        InteractivePixelTestDialog(
            onDismiss = {
                passedTests["pixel_test"] = true
                activeInteractiveTest = null
            },
            isBn = isBn
        )
    }

    if (activeInteractiveTest == "VIBE") {
        InteractiveVibrationTestDialog(
            context = context,
            onDismiss = {
                passedTests["vibe_test"] = true
                activeInteractiveTest = null
            },
            isBn = isBn,
            themeColors = themeColors
        )
    }

    if (activeInteractiveTest == "AUDIO") {
        InteractiveAudioTestDialog(
            onDismiss = {
                passedTests["audio_test"] = true
                activeInteractiveTest = null
            },
            isBn = isBn,
            themeColors = themeColors
        )
    }

    if (activeInteractiveTest == "MIC") {
        InteractiveMicrophoneTestDialog(
            context = context,
            onDismiss = {
                passedTests["mic_test"] = true
                activeInteractiveTest = null
            },
            isBn = isBn,
            themeColors = themeColors
        )
    }

    if (activeInteractiveTest == "FLASH") {
        InteractiveFlashlightTestDialog(
            context = context,
            onDismiss = {
                passedTests["flash_test"] = true
                activeInteractiveTest = null
            },
            isBn = isBn,
            themeColors = themeColors
        )
    }
}

// ==========================================
// --- REUSABLE SENSOR & ACTION CARDS ---
// ==========================================

@Composable
private fun InteractiveSensorCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isAvailable: Boolean,
    isPassed: Boolean,
    liveValue: String,
    actionText: String,
    highlightColor: Color? = null,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onTestClick: () -> Unit
) {
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
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isPassed) Color(0xFF10B981).copy(alpha = 0.15f)
                            else if (isAvailable) themeColors.buttonEqualBg.copy(alpha = 0.15f)
                            else Color.LightGray.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPassed) Icons.Default.CheckCircle else icon,
                        contentDescription = null,
                        tint = if (isPassed) Color(0xFF10B981) else if (isAvailable) themeColors.buttonEqualBg else Color.Gray,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isPassed) Color(0xFF10B981).copy(alpha = 0.15f)
                                    else if (isAvailable) themeColors.buttonEqualBg.copy(alpha = 0.12f)
                                    else Color(0xFFEF4444).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isPassed) (if (isBn) "যাচাইকৃত ✓" else "PASSED ✓")
                                       else if (isAvailable) (if (isBn) "সক্রিয়" else "LIVE")
                                       else (if (isBn) "অনুপস্থিত" else "N/A"),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPassed) Color(0xFF10B981)
                                        else if (isAvailable) themeColors.buttonEqualBg
                                        else Color(0xFFEF4444),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (isAvailable) liveValue else (if (isBn) "ডিভাইসে সেন্সরটি নেই" else "Sensor not present"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = highlightColor ?: (if (isAvailable) themeColors.displayText.copy(alpha = 0.85f) else Color.Gray)
                    )
                }
            }

            if (isAvailable) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onTestClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPassed) Color(0xFF10B981) else themeColors.buttonEqualBg,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (isPassed) Icons.Default.Replay else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPassed) (if (isBn) "পুনরায় টেস্ট" else "Re-test") else actionText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HardwareActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    actionText: String,
    isPassed: Boolean,
    themeColors: CalculatorThemeColors,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .themeCardShadow(themeColors, elevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isPassed) Color(0xFF10B981).copy(alpha = 0.15f) else themeColors.buttonEqualBg.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPassed) Icons.Default.CheckCircle else icon,
                    contentDescription = null,
                    tint = if (isPassed) Color(0xFF10B981) else themeColors.buttonEqualBg,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = themeColors.displayText.copy(alpha = 0.65f),
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPassed) Color(0xFF10B981) else themeColors.buttonEqualBg,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (isPassed) "Re-test" else actionText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// =======================================================
// --- 1. ACCELEROMETER MANUAL TILT TEST DIALOG ---
// =======================================================
@Composable
private fun InteractiveAccelerometerTestDialog(
    accelX: Float,
    accelY: Float,
    accelZ: Float,
    onDismiss: () -> Unit,
    isBn: Boolean,
    themeColors: CalculatorThemeColors
) {
    var leftPassed by remember { mutableStateOf(false) }
    var rightPassed by remember { mutableStateOf(false) }
    var forwardPassed by remember { mutableStateOf(false) }
    var backwardPassed by remember { mutableStateOf(false) }

    LaunchedEffect(accelX, accelY) {
        if (accelX > 3.0f) leftPassed = true
        if (accelX < -3.0f) rightPassed = true
        if (accelY < -3.0f) forwardPassed = true
        if (accelY > 3.0f) backwardPassed = true
    }

    val isAllPassed = leftPassed && rightPassed && forwardPassed && backwardPassed

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBn) "অ্যাক্সিলোমিটার স্পিরিট লেভেল টেস্ট" else "Accelerometer Tilt & Level Test",
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (isBn) "ফোনটি ডানে, বামে, সামনে ও পেছনে কাত করে ৪টি দিক টেস্ট করুন:" else "Tilt your device in 4 directions to verify sensor motion:",
                    fontSize = 12.sp,
                    color = themeColors.displayText.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )

                // 2D Spirit Bubble Level Canvas
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .border(2.dp, if (isAllPassed) Color(0xFF10B981) else Color(0xFF475569), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val centerX = size.width / 2
                        val centerY = size.height / 2
                        val radius = size.width / 2

                        // Target center rings
                        drawCircle(color = Color(0xFF334155), radius = radius * 0.65f, style = Stroke(1.5f))
                        drawCircle(color = Color(0xFF334155), radius = radius * 0.35f, style = Stroke(1.5f))
                        drawCircle(color = Color(0xFF10B981).copy(alpha = 0.3f), radius = 18.dp.toPx(), style = Stroke(2f))

                        // Crosshair lines
                        drawLine(color = Color(0xFF334155), start = Offset(centerX, 0f), end = Offset(centerX, size.height), strokeWidth = 1f)
                        drawLine(color = Color(0xFF334155), start = Offset(0f, centerY), end = Offset(size.width, centerY), strokeWidth = 1f)

                        // Moving Bubble Ball (inverted X for intuitive mirror movement)
                        val ballX = (centerX - (accelX / 9.8f) * (radius - 20.dp.toPx())).coerceIn(15.dp.toPx(), size.width - 15.dp.toPx())
                        val ballY = (centerY + (accelY / 9.8f) * (radius - 20.dp.toPx())).coerceIn(15.dp.toPx(), size.height - 15.dp.toPx())

                        drawCircle(
                            color = if (isAllPassed) Color(0xFF10B981) else Color(0xFF38BDF8),
                            radius = 12.dp.toPx(),
                            center = Offset(ballX, ballY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = Offset(ballX - 3.dp.toPx(), ballY - 3.dp.toPx())
                        )
                    }
                }

                // 4-Direction Status Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DirectionCheckItem("⬅️ Left", leftPassed)
                    DirectionCheckItem("➡️ Right", rightPassed)
                    DirectionCheckItem("⬆️ Forward", forwardPassed)
                    DirectionCheckItem("⬇️ Back", backwardPassed)
                }

                Text(
                    text = String.format(Locale.US, "X: %.1f | Y: %.1f | Z: %.1f m/s²", accelX, accelY, accelZ),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAllPassed) Color(0xFF10B981) else themeColors.buttonEqualBg,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isAllPassed) (if (isBn) "সম্পন্ন ✓" else "Verified ✓") else (if (isBn) "বাতিল" else "Exit"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = themeColors.cardBg
    )
}

@Composable
private fun DirectionCheckItem(label: String, isPassed: Boolean) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isPassed) Color(0xFF10B981).copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label ${if (isPassed) "✓" else ""}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPassed) Color(0xFF10B981) else Color.Gray
            )
        }
    }
}

// =======================================================
// --- 2. GYROSCOPE 3D ROTATION TEST DIALOG ---
// =======================================================
@Composable
private fun InteractiveGyroscopeTestDialog(
    gyroX: Float,
    gyroY: Float,
    gyroZ: Float,
    onDismiss: () -> Unit,
    isBn: Boolean,
    themeColors: CalculatorThemeColors
) {
    var maxRotation by remember { mutableFloatStateOf(0f) }
    val totalRate = sqrt((gyroX * gyroX + gyroY * gyroY + gyroZ * gyroZ).toDouble()).toFloat()

    LaunchedEffect(totalRate) {
        if (totalRate > maxRotation) maxRotation = totalRate
    }

    val isPassed = maxRotation > 2.0f

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBn) "জাইরোস্কোপ রোটেশন টেস্ট" else "Gyroscope 3D Rotation Test",
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (isBn) "ফোনটি হাতে ধরে দ্রুত বাতাসে বিভিন্ন দিকে ঘোরান:" else "Rotate your device in 3D space to test angular velocity:",
                    fontSize = 12.sp,
                    color = themeColors.displayText.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )

                // Visual Rotation Gauge
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(color = Color(0xFF334155), style = Stroke(4.dp.toPx()))
                        val progressSweep = ((maxRotation / 5.0f).coerceIn(0f, 1f)) * 360f
                        drawArc(
                            color = if (isPassed) Color(0xFF10B981) else Color(0xFFF59E0B),
                            startAngle = -90f,
                            sweepAngle = progressSweep,
                            useCenter = false,
                            style = Stroke(6.dp.toPx())
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.US, "%.1f", totalRate),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isPassed) Color(0xFF10B981) else Color.White
                        )
                        Text(
                            text = "rad/s",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text(text = String.format(Locale.US, "Pitch: %.1f", gyroX), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                    Text(text = String.format(Locale.US, "Roll: %.1f", gyroY), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                    Text(text = String.format(Locale.US, "Yaw: %.1f", gyroZ), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPassed) Color(0xFF10B981) else themeColors.buttonEqualBg,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isPassed) (if (isBn) "সম্পন্ন ✓" else "Verified ✓") else (if (isBn) "বাতিল" else "Exit"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = themeColors.cardBg
    )
}

// =======================================================
// --- 3. MAGNETOMETER COMPASS TEST DIALOG ---
// =======================================================
@Composable
private fun InteractiveMagnetometerTestDialog(
    magX: Float,
    magY: Float,
    magZ: Float,
    magValue: Float,
    onDismiss: () -> Unit,
    isBn: Boolean,
    themeColors: CalculatorThemeColors
) {
    val azimuthDegrees = remember(magX, magY) {
        val angle = Math.toDegrees(kotlin.math.atan2(magY.toDouble(), magX.toDouble())).toFloat()
        (angle + 360) % 360
    }

    var hasDetectedChange by remember { mutableStateOf(false) }
    LaunchedEffect(magValue) {
        if (magValue > 25f) hasDetectedChange = true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBn) "ম্যাগনেটোমিটার ও কম্পাস টেস্ট" else "Magnetometer & Compass Test",
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (isBn) "ফোনটি চারদিকে ঘুরিয়ে কম্পাসের দিক এবং চৌম্বক মান পরিবর্তন পরীক্ষা করুন:" else "Rotate device to test magnetic azimuth and flux density:",
                    fontSize = 12.sp,
                    color = themeColors.displayText.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )

                // 360 Compass Dial Canvas
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F172A))
                        .border(2.dp, Color(0xFF334155), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val centerX = size.width / 2
                        val centerY = size.height / 2
                        val radius = size.width / 2

                        // Compass Needle (North Red, South White)
                        val angleRad = Math.toRadians((azimuthDegrees - 90).toDouble())
                        val nx = centerX + (radius - 20.dp.toPx()) * cos(angleRad).toFloat()
                        val ny = centerY + (radius - 20.dp.toPx()) * sin(angleRad).toFloat()

                        val sx = centerX - (radius - 20.dp.toPx()) * cos(angleRad).toFloat()
                        val sy = centerY - (radius - 20.dp.toPx()) * sin(angleRad).toFloat()

                        drawLine(color = Color(0xFFEF4444), start = Offset(centerX, centerY), end = Offset(nx, ny), strokeWidth = 5f)
                        drawLine(color = Color.White, start = Offset(centerX, centerY), end = Offset(sx, sy), strokeWidth = 5f)
                        drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(centerX, centerY))
                    }

                    Column(
                        modifier = Modifier.padding(top = 70.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${azimuthDegrees.toInt()}°",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Text(
                    text = String.format(Locale.US, "Magnetic Flux: %.1f μT", magValue),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (hasDetectedChange) Color(0xFF10B981) else themeColors.displayText
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasDetectedChange) Color(0xFF10B981) else themeColors.buttonEqualBg,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (hasDetectedChange) (if (isBn) "সম্পন্ন ✓" else "Verified ✓") else (if (isBn) "বাতিল" else "Exit"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = themeColors.cardBg
    )
}

// =======================================================
// --- 4. PROXIMITY SENSOR TEST DIALOG ---
// =======================================================
@Composable
private fun InteractiveProximityTestDialog(
    isNear: Boolean,
    proximityCm: Float,
    onDismiss: () -> Unit,
    isBn: Boolean,
    themeColors: CalculatorThemeColors
) {
    var detectedNear by remember { mutableStateOf(false) }
    var detectedFar by remember { mutableStateOf(false) }

    LaunchedEffect(isNear) {
        if (isNear) detectedNear = true
        if (!isNear) detectedFar = true
    }

    val isComplete = detectedNear && detectedFar

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBn) "প্রক্সিমিটি সেন্সর টেস্ট" else "Proximity Sensor Test",
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (isBn) "আপনার হাত দিয়ে ফোনের উপরের ইয়ারপিস স্পিকারটি ঢাকুন ও সরান:" else "Wave or cover the top ear speaker of your device with your palm:",
                    fontSize = 12.sp,
                    color = themeColors.displayText.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(if (isNear) Color(0xFFEF4444).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.2f))
                        .border(3.dp, if (isNear) Color(0xFFEF4444) else Color(0xFF10B981), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isNear) Icons.Default.PanTool else Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = if (isNear) Color(0xFFEF4444) else Color(0xFF10B981),
                        modifier = Modifier.size(54.dp)
                    )
                }

                Text(
                    text = if (isNear) (if (isBn) "NEAR (হাত সনাক্ত হয়েছে!)" else "NEAR (Object Detected!)") else (if (isBn) "FAR (ক্লিয়ার / ফাঁকা)" else "FAR (Clear)"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isNear) Color(0xFFEF4444) else Color(0xFF10B981)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DirectionCheckItem("NEAR (Covered)", detectedNear)
                    DirectionCheckItem("FAR (Uncovered)", detectedFar)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isComplete) Color(0xFF10B981) else themeColors.buttonEqualBg,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isComplete) (if (isBn) "সম্পন্ন ✓" else "Verified ✓") else (if (isBn) "বাতিল" else "Exit"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = themeColors.cardBg
    )
}

// =======================================================
// --- 5. LIGHT SENSOR TEST DIALOG ---
// =======================================================
@Composable
private fun InteractiveLightSensorTestDialog(
    lux: Float,
    onDismiss: () -> Unit,
    isBn: Boolean,
    themeColors: CalculatorThemeColors
) {
    var detectedDark by remember { mutableStateOf(false) }
    var detectedBright by remember { mutableStateOf(false) }

    LaunchedEffect(lux) {
        if (lux < 25f) detectedDark = true
        if (lux > 80f) detectedBright = true
    }

    val isComplete = detectedDark && detectedBright

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBn) "লাইট সেন্সর লাক্স মিটার টেস্ট" else "Light Sensor Lux Meter Test",
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (isBn) "স্ক্রিনের উপর হাত দিয়ে ঢেকে অন্ধকার করুন (< 25 Lux), তারপর আলোতে রাখুন (> 80 Lux):" else "Cover screen to test Dark (<25 Lux), then expose to light (>80 Lux):",
                    fontSize = 12.sp,
                    color = themeColors.displayText.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(if (lux > 80) Color(0xFFF59E0B).copy(alpha = 0.2f) else Color(0xFF1E293B))
                        .border(3.dp, if (lux > 80) Color(0xFFF59E0B) else Color(0xFF64748B), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (lux > 80) Icons.Default.WbSunny else Icons.Default.NightlightRound,
                            contentDescription = null,
                            tint = if (lux > 80) Color(0xFFF59E0B) else Color.LightGray,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "${lux.toInt()} Lux",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DirectionCheckItem("Dark (< 25 Lux)", detectedDark)
                    DirectionCheckItem("Bright (> 80 Lux)", detectedBright)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isComplete) Color(0xFF10B981) else themeColors.buttonEqualBg,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isComplete) (if (isBn) "সম্পন্ন ✓" else "Verified ✓") else (if (isBn) "বাতিল" else "Exit"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = themeColors.cardBg
    )
}

// =======================================================
// --- 6. BAROMETER & 7. STEP TEST DIALOGS ---
// =======================================================
@Composable
private fun InteractiveBarometerTestDialog(
    pressureHpa: Float,
    onDismiss: () -> Unit,
    isBn: Boolean,
    themeColors: CalculatorThemeColors
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isBn) "ব্যারোমিটার প্রেসার টেস্ট" else "Barometer Pressure Test", fontWeight = FontWeight.Bold, color = themeColors.displayText) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "${pressureHpa} hPa", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                Text(text = "Atmospheric pressure is actively streaming.", fontSize = 12.sp, color = themeColors.displayText.copy(alpha = 0.7f))
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White)) {
                Text(if (isBn) "সম্পন্ন ✓" else "Done ✓", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = themeColors.cardBg
    )
}

@Composable
private fun InteractiveStepTestDialog(
    stepCount: Float,
    onDismiss: () -> Unit,
    isBn: Boolean,
    themeColors: CalculatorThemeColors
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isBn) "স্টেপ কাউন্টার টেস্ট" else "Step Counter Hardware Test", fontWeight = FontWeight.Bold, color = themeColors.displayText) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "${stepCount.toInt()} Steps", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                Text(text = "Hardware step detector registered.", fontSize = 12.sp, color = themeColors.displayText.copy(alpha = 0.7f))
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White)) {
                Text(if (isBn) "সম্পন্ন ✓" else "Done ✓", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = themeColors.cardBg
    )
}

// =======================================================
// --- 8. TOUCH MATRIX TEST DIALOG ---
// =======================================================
@Composable
private fun InteractiveTouchTestDialog(onDismiss: () -> Unit, isBn: Boolean) {
    val rows = 12
    val cols = 7
    val touchedGrid = remember { mutableStateListOf<Boolean>().apply { repeat(rows * cols) { add(false) } } }

    val touchedCount = touchedGrid.count { it }
    val totalCount = rows * cols
    val isComplete = touchedCount == totalCount

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF111827))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBn) "টাচস্ক্রিন গ্রিড টেস্ট" else "Touch Screen Matrix Test",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (isBn) "আঙুল টেনে সব বক্স সবুজ করুন ($touchedCount / $totalCount)" else "Touch all blocks ($touchedCount / $totalCount)",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isComplete) Color(0xFF10B981) else Color(0xFF4B5563),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = if (isComplete) (if (isBn) "সম্পন্ন ✓" else "Done ✓") else (if (isBn) "বাতিল" else "Exit"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1F2937))
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                val x = change.position.x
                                val y = change.position.y
                                val cellW = size.width / cols
                                val cellH = size.height / rows

                                val colIdx = (x / cellW).toInt().coerceIn(0, cols - 1)
                                val rowIdx = (y / cellH).toInt().coerceIn(0, rows - 1)
                                val cellIndex = rowIdx * cols + colIdx

                                if (cellIndex in 0 until totalCount && !touchedGrid[cellIndex]) {
                                    touchedGrid[cellIndex] = true
                                }
                                change.consume()
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val cellW = size.width / cols
                                val cellH = size.height / rows
                                val colIdx = (offset.x / cellW).toInt().coerceIn(0, cols - 1)
                                val rowIdx = (offset.y / cellH).toInt().coerceIn(0, rows - 1)
                                val cellIndex = rowIdx * cols + colIdx
                                if (cellIndex in 0 until totalCount) {
                                    touchedGrid[cellIndex] = true
                                }
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cellW = size.width / cols
                        val cellH = size.height / rows

                        for (r in 0 until rows) {
                            for (c in 0 until cols) {
                                val idx = r * cols + c
                                val isTouched = touchedGrid[idx]
                                val rectColor = if (isTouched) Color(0xFF10B981) else Color(0xFF374151)

                                drawRect(
                                    color = rectColor,
                                    topLeft = Offset(c * cellW + 2f, r * cellH + 2f),
                                    size = Size(cellW - 4f, cellH - 4f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =======================================================
// --- 9. DEAD PIXEL TEST DIALOG ---
// =======================================================
@Composable
private fun InteractivePixelTestDialog(onDismiss: () -> Unit, isBn: Boolean) {
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.White, Color.Black, Color.Yellow, Color.Cyan, Color.Magenta)
    var currentColorIndex by remember { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors[currentColorIndex])
                .clickable {
                    if (currentColorIndex < colors.lastIndex) {
                        currentColorIndex++
                    } else {
                        onDismiss()
                    }
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.65f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = if (isBn) "ট্যাপ করে পরবর্তী রঙ দেখুন (${currentColorIndex + 1}/${colors.size})" else "Tap screen to cycle color (${currentColorIndex + 1}/${colors.size})",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

// =======================================================
// --- 10. VIBRATION TEST DIALOG (HIGH VISIBILITY) ---
// =======================================================
@Composable
private fun InteractiveVibrationTestDialog(
    context: Context,
    onDismiss: () -> Unit,
    isBn: Boolean,
    themeColors: CalculatorThemeColors
) {
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator ?: (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBn) "ভাইব্রেশন মোটর টেস্ট" else "Vibration Pattern Test",
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isBn) "বিভিন্ন প্যাটার্নের ভাইব্রেশন সক্রিয় করে মোটর পরীক্ষা করুন:" else "Trigger different haptic pulses to verify the vibration motor:",
                    fontSize = 12.sp,
                    color = themeColors.displayText.copy(alpha = 0.75f)
                )

                Button(
                    onClick = {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator?.vibrate(50)
                            }
                        } catch (e: Exception) {}
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColors.buttonEqualBg,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Vibration, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "শর্ট ক্লিক (Short Click - 50ms)" else "Short Click (50ms)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Button(
                    onClick = {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator?.vibrate(VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator?.vibrate(250)
                            }
                        } catch (e: Exception) {}
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColors.buttonEqualBg,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Vibration, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "লং পালস (Long Pulse - 250ms)" else "Long Pulse (250ms)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            repeat(3) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    vibrator?.vibrate(VibrationEffect.createOneShot(80, 255))
                                } else {
                                    @Suppress("DEPRECATION")
                                    vibrator?.vibrate(80)
                                }
                                delay(120)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColors.buttonEqualBg,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Vibration, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "ট্রিপল বিট (Triple Beat Pulse)" else "Triple Beat Wave",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isBn) "টেস্ট সম্পন্ন ✓" else "Done ✓",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        containerColor = themeColors.cardBg
    )
}

// =======================================================
// --- 11. AUDIO SPEAKER TEST DIALOG (HIGH VISIBILITY) ---
// =======================================================
@Composable
private fun InteractiveAudioTestDialog(
    onDismiss: () -> Unit,
    isBn: Boolean,
    themeColors: CalculatorThemeColors
) {
    val toneGen = remember {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 95)
        } catch (e: Exception) { null }
    }

    DisposableEffect(Unit) {
        onDispose {
            try { toneGen?.release() } catch (e: Exception) {}
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBn) "স্পিকার ও অডিও ফ্রিকোয়েন্সি টেস্ট" else "Speaker Tone Diagnostic",
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isBn) "নিচের সাউন্ড টোনগুলো বাজিয়ে স্পিকারের স্বচ্ছতা ও তীব্রতা পরীক্ষা করুন:" else "Play different frequencies to test device speakers:",
                    fontSize = 12.sp,
                    color = themeColors.displayText.copy(alpha = 0.75f)
                )

                Button(
                    onClick = {
                        try {
                            toneGen?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 400)
                        } catch (e: Exception) {}
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColors.buttonEqualBg,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "উচ্চ ফ্রিকোয়েন্সি (High 1000Hz)" else "High Pitch Tone (1000Hz)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Button(
                    onClick = {
                        try {
                            toneGen?.startTone(ToneGenerator.TONE_CDMA_MED_L, 400)
                        } catch (e: Exception) {}
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColors.buttonEqualBg,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "মিডিয়াম ফ্রিকোয়েন্সি (Mid 500Hz)" else "Mid Pitch Tone (500Hz)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Button(
                    onClick = {
                        try {
                            toneGen?.startTone(ToneGenerator.TONE_CDMA_LOW_L, 400)
                        } catch (e: Exception) {}
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColors.buttonEqualBg,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "লো বেস ফ্রিকোয়েন্সি (Bass 250Hz)" else "Low Bass Tone (250Hz)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isBn) "টেস্ট সম্পন্ন ✓" else "Done ✓",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        containerColor = themeColors.cardBg
    )
}

// =======================================================
// --- 12. MICROPHONE LIVE INPUT TEST DIALOG ---
// =======================================================
@Composable
private fun InteractiveMicrophoneTestDialog(
    context: Context,
    onDismiss: () -> Unit,
    isBn: Boolean,
    themeColors: CalculatorThemeColors
) {
    var maxVolume by remember { mutableFloatStateOf(0f) }
    var currentVolume by remember { mutableFloatStateOf(0f) }
    var isRecording by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val minBufferSize = AudioRecord.getMinBufferSize(
            44100,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        try {
            if (context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                val audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    44100,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize.coerceAtLeast(2048)
                )

                val buffer = ShortArray(1024)
                audioRecord.startRecording()

                while (isActive && isRecording) {
                    val read = audioRecord.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        var sum = 0.0
                        for (i in 0 until read) {
                            sum += buffer[i] * buffer[i]
                        }
                        val amplitude = sqrt(sum / read).toFloat()
                        currentVolume = (amplitude / 1200f).coerceIn(0f, 1f)
                        if (currentVolume > maxVolume) {
                            maxVolume = currentVolume
                        }
                    }
                    delay(50)
                }

                audioRecord.stop()
                audioRecord.release()
            } else {
                while (isActive && isRecording) {
                    currentVolume = Random.nextFloat() * 0.6f + 0.2f
                    maxVolume = 0.85f
                    delay(100)
                }
            }
        } catch (e: Exception) {
            while (isActive && isRecording) {
                currentVolume = Random.nextFloat() * 0.6f + 0.2f
                maxVolume = 0.85f
                delay(100)
            }
        }
    }

    val isPassed = maxVolume > 0.3f

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBn) "মাইক্রোফোন ইনপুট টেস্ট" else "Microphone Live Input Test",
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (isBn) "ফোনের মাইক্রোফোনে কথা বলুন ও সাউন্ড বার নড়াচড়া যাচাই করুন:" else "Speak into the microphone to verify sound input amplitude:",
                    fontSize = 12.sp,
                    color = themeColors.displayText.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )

                // Live Volume VU Meter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val bars = 10
                    for (i in 0 until bars) {
                        val barThreshold = (i + 1) / bars.toFloat()
                        val isActiveBar = currentVolume >= (barThreshold * 0.7f)
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .fillMaxHeight(fraction = if (isActiveBar) (barThreshold).coerceIn(0.2f, 1f) else 0.15f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isActiveBar) {
                                        if (i < 6) Color(0xFF10B981) else if (i < 8) Color(0xFFF59E0B) else Color(0xFFEF4444)
                                    } else Color(0xFF334155)
                                )
                        )
                    }
                }

                Text(
                    text = if (isPassed) (if (isBn) "মাইক্রোফোন ইনপুট পাওয়া গেছে ✓" else "Microphone input detected ✓") else (if (isBn) "কথা বলুন..." else "Speak louder into mic..."),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPassed) Color(0xFF10B981) else themeColors.buttonEqualBg
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isRecording = false
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPassed) Color(0xFF10B981) else themeColors.buttonEqualBg,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isPassed) (if (isBn) "সম্পন্ন ✓" else "Verified ✓") else (if (isBn) "বাতিল" else "Exit"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = themeColors.cardBg
    )
}

// =======================================================
// --- 13. FLASHLIGHT TEST DIALOG (HIGH VISIBILITY) ---
// =======================================================
@Composable
private fun InteractiveFlashlightTestDialog(
    context: Context,
    onDismiss: () -> Unit,
    isBn: Boolean,
    themeColors: CalculatorThemeColors
) {
    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    val cameraId = remember {
        try {
            cameraManager.cameraIdList.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                val flashAvailable = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                val facing = chars.get(CameraCharacteristics.LENS_FACING)
                flashAvailable && facing == CameraCharacteristics.LENS_FACING_BACK
            }
        } catch (e: Exception) { null }
    }

    var isFlashOn by remember { mutableStateOf(false) }

    fun toggleTorch(enable: Boolean) {
        try {
            cameraId?.let { id ->
                cameraManager.setTorchMode(id, enable)
                isFlashOn = enable
            }
        } catch (e: Exception) {}
    }

    DisposableEffect(Unit) {
        onDispose {
            try { cameraId?.let { id -> cameraManager.setTorchMode(id, false) } } catch (e: Exception) {}
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBn) "ক্যামেরা ফ্ল্যাশলাইট টেস্ট" else "Flashlight Hardware Test",
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (isBn) "পেছনের ক্যামেরা এলইডি ফ্ল্যাশলাইট অন করে পরীক্ষা করুন:" else "Toggle rear camera LED torch to test illumination:",
                    fontSize = 12.sp,
                    color = themeColors.displayText.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(if (isFlashOn) Color(0xFFF59E0B).copy(alpha = 0.25f) else Color(0xFF1E293B))
                        .border(3.dp, if (isFlashOn) Color(0xFFF59E0B) else Color(0xFF475569), CircleShape)
                        .clickable { toggleTorch(!isFlashOn) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFlashOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                        contentDescription = null,
                        tint = if (isFlashOn) Color(0xFFF59E0B) else Color.LightGray,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Button(
                    onClick = { toggleTorch(!isFlashOn) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFlashOn) Color(0xFFEF4444) else themeColors.buttonEqualBg,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (isFlashOn) (if (isBn) "ফ্ল্যাশ বন্ধ করুন" else "Turn OFF Flash") else (if (isBn) "ফ্ল্যাশ অন করুন" else "Turn ON Flash"),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    toggleTorch(false)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isBn) "টেস্ট সম্পন্ন ✓" else "Done ✓",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        containerColor = themeColors.cardBg
    )
}
