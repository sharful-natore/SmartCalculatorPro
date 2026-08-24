package com.example.ui.screens.tools

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.theme.themeCardShadow
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import com.example.util.CrashReporter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.*

enum class LevelToolMode(val titleEn: String, val titleBn: String) {
    AR_CAMERA("AR Camera Level", "AR ক্যামেরা লেভেল"),
    SURFACE_BUBBLE("Surface Bubble Level", "সারফেস বাবল লেভেল"),
    PLUMB_BOB("Plumb Bob (Vertical)", "উলম্ব প্লাম্ব লাইন"),
    ANGLE_GUIDE("Standard Angles Guide", "স্ট্যান্ডার্ড কোণ গাইড")
}

enum class AngleUnit(val symbol: String, val nameEn: String, val nameBn: String) {
    DEGREES("°", "Degrees", "ডিগ্রি"),
    PERCENT("%", "Slope / Gradient", "ঢাল শতাংশ"),
    INCH_PER_FOOT("in/ft", "Roof Pitch", "ইঞ্চি/ফুট পিচ"),
    RADIANS("rad", "Radians", "রেডিয়ান")
}

data class LevelSavedRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val rollDeg: Float,
    val pitchDeg: Float,
    val note: String,
    val timeFormatted: String,
    val isLevel: Boolean
)

@Composable
fun CameraLevelTool(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val context = LocalContext.current
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    val accentColor = themeColors.buttonOperatorBg

    LaunchedEffect(Unit) {
        CrashReporter.currentActiveScreen = "Camera Level Tool"
    }

    var selectedMode by remember { mutableStateOf(LevelToolMode.AR_CAMERA) }
    var selectedUnit by remember { mutableStateOf(AngleUnit.DEGREES) }

    // Camera permission
    var hasCameraPermission by remember {
        mutableStateOf(
            try {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            } catch (_: Throwable) {
                false
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Camera Controls
    var isFrontCamera by remember { mutableStateOf(false) }
    var isTorchOn by remember { mutableStateOf(false) }
    var cameraInstance by remember { mutableStateOf<Camera?>(null) }
    var isFrozen by remember { mutableStateOf(false) }
    var isCameraEnabled by remember { mutableStateOf(true) }

    // Overlays toggles
    var showLaserGrid by remember { mutableStateOf(true) }
    var showProtractor by remember { mutableStateOf(true) }
    var showBullseye by remember { mutableStateOf(true) }
    var showPlumbLine by remember { mutableStateOf(true) }
    var hapticFeedbackEnabled by remember { mutableStateOf(true) }
    var soundToneEnabled by remember { mutableStateOf(false) }

    // Calibration
    var zeroRollOffset by remember { mutableFloatStateOf(0f) }
    var zeroPitchOffset by remember { mutableFloatStateOf(0f) }

    // Live Sensor State
    var rawRoll by remember { mutableFloatStateOf(0f) }
    var rawPitch by remember { mutableFloatStateOf(0f) }
    var rawAzimuth by remember { mutableFloatStateOf(0f) }

    // Frozen snapshot values
    var frozenRoll by remember { mutableFloatStateOf(0f) }
    var frozenPitch by remember { mutableFloatStateOf(0f) }

    // Saved records
    var savedRecords by remember { mutableStateOf(listOf<LevelSavedRecord>()) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var recordNoteText by remember { mutableStateOf("") }

    // Feedback helpers
    val vibrator = remember {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Throwable) {
            null
        }
    }

    var lastBeepTime by remember { mutableLongStateOf(0L) }

    // Register Sensors safely
    DisposableEffect(Unit) {
        val sensorManager = try {
            context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        } catch (_: Throwable) {
            null
        }

        val rotationSensor = sensorManager?.let { sm ->
            try {
                sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
                    ?: sm.getDefaultSensor(Sensor.TYPE_GRAVITY)
                    ?: sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                    ?: sm.getDefaultSensor(Sensor.TYPE_ORIENTATION)
            } catch (_: Throwable) {
                null
            }
        }

        var smoothRoll = 0f
        var smoothPitch = 0f
        val alpha = 0.20f

        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null || isFrozen || event.values == null || event.values.isEmpty()) return

                try {
                    when (event.sensor.type) {
                        Sensor.TYPE_ROTATION_VECTOR -> {
                            val vals = event.values
                            val rotationMatrix = FloatArray(9)
                            if (vals.size >= 4) {
                                SensorManager.getRotationMatrixFromVector(rotationMatrix, vals)
                            } else if (vals.size == 3) {
                                val q0 = (1f - vals[0] * vals[0] - vals[1] * vals[1] - vals[2] * vals[2]).coerceAtLeast(0f)
                                val vec4 = floatArrayOf(vals[0], vals[1], vals[2], sqrt(q0))
                                SensorManager.getRotationMatrixFromVector(rotationMatrix, vec4)
                            } else {
                                return
                            }

                            val orientationValues = FloatArray(3)
                            SensorManager.getOrientation(rotationMatrix, orientationValues)

                            val degAzimuth = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
                            val degPitch = Math.toDegrees(orientationValues[1].toDouble()).toFloat()
                            val degRoll = Math.toDegrees(orientationValues[2].toDouble()).toFloat()

                            if (!degRoll.isNaN() && !degPitch.isNaN()) {
                                smoothRoll += alpha * (degRoll - smoothRoll)
                                smoothPitch += alpha * (degPitch - smoothPitch)

                                rawRoll = if (smoothRoll.isNaN()) 0f else smoothRoll
                                rawPitch = if (smoothPitch.isNaN()) 0f else smoothPitch
                                rawAzimuth = if (degAzimuth.isNaN()) 0f else degAzimuth
                            }
                        }
                        Sensor.TYPE_GRAVITY, Sensor.TYPE_ACCELEROMETER -> {
                            if (event.values.size >= 3) {
                                val ax = event.values[0]
                                val ay = event.values[1]
                                val az = event.values[2]

                                val denom = sqrt((ax * ax + az * az).toDouble())
                                val pitch = if (denom > 0.0001) {
                                    Math.toDegrees(atan2(ay.toDouble(), denom)).toFloat()
                                } else 0f

                                val roll = if (abs(az) > 0.0001 || abs(ax) > 0.0001) {
                                    Math.toDegrees(atan2(-ax.toDouble(), az.toDouble())).toFloat()
                                } else 0f

                                if (!roll.isNaN() && !pitch.isNaN()) {
                                    smoothRoll += alpha * (roll - smoothRoll)
                                    smoothPitch += alpha * (pitch - smoothPitch)

                                    rawRoll = if (smoothRoll.isNaN()) 0f else smoothRoll
                                    rawPitch = if (smoothPitch.isNaN()) 0f else smoothPitch
                                }
                            }
                        }
                        Sensor.TYPE_ORIENTATION -> {
                            if (event.values.size >= 3) {
                                val degAzimuth = event.values[0]
                                val degPitch = event.values[1]
                                val degRoll = event.values[2]
                                if (!degRoll.isNaN() && !degPitch.isNaN()) {
                                    smoothRoll += alpha * (degRoll - smoothRoll)
                                    smoothPitch += alpha * (degPitch - smoothPitch)
                                    rawRoll = if (smoothRoll.isNaN()) 0f else smoothRoll
                                    rawPitch = if (smoothPitch.isNaN()) 0f else smoothPitch
                                    rawAzimuth = if (degAzimuth.isNaN()) 0f else degAzimuth
                                }
                            }
                        }
                    }
                } catch (e: Throwable) {
                    CrashReporter.logHandledException(context, "SensorEvent", e)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        try {
            rotationSensor?.let {
                sensorManager?.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_UI)
            }
        } catch (e: Throwable) {
            CrashReporter.logHandledException(context, "SensorRegister", e)
        }

        onDispose {
            try {
                sensorManager?.unregisterListener(sensorListener)
            } catch (_: Throwable) {}
        }
    }

    // Computed effective angles
    val currentRoll = if (isFrozen) frozenRoll else (rawRoll - zeroRollOffset)
    val currentPitch = if (isFrozen) frozenPitch else (rawPitch - zeroPitchOffset)

    // Check leveling thresholds
    val absRoll = abs(currentRoll)
    val isHorizontalLevel = (absRoll <= 0.6f) || (abs(absRoll - 180f) <= 0.6f)
    val isVerticalLevel = abs(absRoll - 90f) <= 0.6f
    val isSurfaceFlat = (abs(currentRoll) <= 0.6f) && (abs(currentPitch) <= 0.6f)
    val isAnyLevelAchieved = isHorizontalLevel || isVerticalLevel || isSurfaceFlat

    // Haptic pulse & tone
    LaunchedEffect(isAnyLevelAchieved) {
        if (isAnyLevelAchieved && hapticFeedbackEnabled) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(45)
                }
            } catch (_: Throwable) {}
        }
        if (isAnyLevelAchieved && soundToneEnabled) {
            val now = System.currentTimeMillis()
            if (now - lastBeepTime > 700) {
                lastBeepTime = now
                try {
                    val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
                    tone.startTone(ToneGenerator.TONE_PROP_BEEP, 60)
                    tone.release()
                } catch (_: Throwable) {}
            }
        }
    }

    // Format Angle according to unit
    fun formatAngle(angleDeg: Float): String {
        return try {
            when (selectedUnit) {
                AngleUnit.DEGREES -> String.format(Locale.US, "%.1f°", angleDeg)
                AngleUnit.PERCENT -> {
                    val slope = tan(Math.toRadians(abs(angleDeg).toDouble())) * 100.0
                    String.format(Locale.US, "%.1f%%", slope)
                }
                AngleUnit.INCH_PER_FOOT -> {
                    val pitch = tan(Math.toRadians(abs(angleDeg).toDouble())) * 12.0
                    String.format(Locale.US, "%.2f in/ft", pitch)
                }
                AngleUnit.RADIANS -> {
                    val rad = Math.toRadians(angleDeg.toDouble())
                    String.format(Locale.US, "%.3f rad", rad)
                }
            }
        } catch (_: Throwable) {
            String.format(Locale.US, "%.1f°", angleDeg)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Selector Tabs
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .themeCardShadow(themeColors),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.cardBg)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LevelToolMode.values().forEach { mode ->
                    val isSelected = selectedMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedMode = mode },
                        label = {
                            Text(
                                if (isBn) mode.titleBn else mode.titleEn,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when (mode) {
                                    LevelToolMode.AR_CAMERA -> Icons.Default.CameraAlt
                                    LevelToolMode.SURFACE_BUBBLE -> Icons.Default.Adjust
                                    LevelToolMode.PLUMB_BOB -> Icons.Default.Straighten
                                    LevelToolMode.ANGLE_GUIDE -> Icons.Default.MenuBook
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor.copy(alpha = 0.2f),
                            selectedLabelColor = accentColor
                        )
                    )
                }
            }
        }

        when (selectedMode) {
            LevelToolMode.AR_CAMERA -> {
                CameraArLevelSection(
                    hasCameraPermission = hasCameraPermission,
                    isCameraEnabled = isCameraEnabled,
                    onToggleCameraEnabled = { isCameraEnabled = !isCameraEnabled },
                    onRequestPermission = {
                        try {
                            launcher.launch(Manifest.permission.CAMERA)
                        } catch (e: Throwable) {
                            CrashReporter.logHandledException(context, "CameraPermRequest", e)
                        }
                    },
                    currentRoll = currentRoll,
                    currentPitch = currentPitch,
                    isFrozen = isFrozen,
                    onToggleFreeze = {
                        if (!isFrozen) {
                            frozenRoll = rawRoll - zeroRollOffset
                            frozenPitch = rawPitch - zeroPitchOffset
                        }
                        isFrozen = !isFrozen
                    },
                    zeroRollOffset = zeroRollOffset,
                    zeroPitchOffset = zeroPitchOffset,
                    onZeroCalibrate = {
                        if (zeroRollOffset == 0f && zeroPitchOffset == 0f) {
                            zeroRollOffset = rawRoll
                            zeroPitchOffset = rawPitch
                        } else {
                            zeroRollOffset = 0f
                            zeroPitchOffset = 0f
                        }
                    },
                    isTorchOn = isTorchOn,
                    onToggleTorch = {
                        isTorchOn = !isTorchOn
                        try {
                            if (cameraInstance?.cameraInfo?.hasFlashUnit() == true) {
                                cameraInstance?.cameraControl?.enableTorch(isTorchOn)
                            }
                        } catch (_: Throwable) {}
                    },
                    isFrontCamera = isFrontCamera,
                    onToggleCamera = { isFrontCamera = !isFrontCamera },
                    onCameraReady = { cameraInstance = it },
                    showLaserGrid = showLaserGrid,
                    onToggleGrid = { showLaserGrid = !showLaserGrid },
                    showProtractor = showProtractor,
                    onToggleProtractor = { showProtractor = !showProtractor },
                    showBullseye = showBullseye,
                    onToggleBullseye = { showBullseye = !showBullseye },
                    showPlumbLine = showPlumbLine,
                    onTogglePlumbLine = { showPlumbLine = !showPlumbLine },
                    isHorizontalLevel = isHorizontalLevel,
                    isVerticalLevel = isVerticalLevel,
                    isSurfaceFlat = isSurfaceFlat,
                    selectedUnit = selectedUnit,
                    onUnitChange = { selectedUnit = it },
                    formatAngle = ::formatAngle,
                    hapticFeedbackEnabled = hapticFeedbackEnabled,
                    onToggleHaptic = { hapticFeedbackEnabled = !hapticFeedbackEnabled },
                    soundToneEnabled = soundToneEnabled,
                    onToggleSound = { soundToneEnabled = !soundToneEnabled },
                    onSaveRecord = { showSaveDialog = true },
                    isBn = isBn,
                    themeColors = themeColors
                )
            }

            LevelToolMode.SURFACE_BUBBLE -> {
                SurfaceBubbleLevelSection(
                    currentRoll = currentRoll,
                    currentPitch = currentPitch,
                    isSurfaceFlat = isSurfaceFlat,
                    formatAngle = ::formatAngle,
                    zeroRollOffset = zeroRollOffset,
                    onZeroCalibrate = {
                        if (zeroRollOffset == 0f && zeroPitchOffset == 0f) {
                            zeroRollOffset = rawRoll
                            zeroPitchOffset = rawPitch
                        } else {
                            zeroRollOffset = 0f
                            zeroPitchOffset = 0f
                        }
                    },
                    isFrozen = isFrozen,
                    onToggleFreeze = {
                        if (!isFrozen) {
                            frozenRoll = rawRoll - zeroRollOffset
                            frozenPitch = rawPitch - zeroPitchOffset
                        }
                        isFrozen = !isFrozen
                    },
                    isBn = isBn,
                    themeColors = themeColors
                )
            }

            LevelToolMode.PLUMB_BOB -> {
                PlumbBobVerticalSection(
                    currentRoll = currentRoll,
                    isVerticalLevel = isVerticalLevel,
                    formatAngle = ::formatAngle,
                    isBn = isBn,
                    themeColors = themeColors
                )
            }

            LevelToolMode.ANGLE_GUIDE -> {
                StandardAnglesGuideSection(
                    currentRoll = abs(currentRoll),
                    isBn = isBn,
                    themeColors = themeColors
                )
            }
        }

        // Saved Measurements Sheet / History
        if (savedRecords.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            SavedRecordsCard(
                records = savedRecords,
                onDelete = { id -> savedRecords = savedRecords.filterNot { it.id == id } },
                onClearAll = { savedRecords = emptyList() },
                isBn = isBn,
                themeColors = themeColors
            )
        }
    }

    // Save Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    if (isBn) "পরিমাপ রেকর্ড সংরক্ষণ" else "Save Angle Measurement",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "${if (isBn) "রোল / কাত কোণ:" else "Roll Angle:"} ${formatAngle(currentRoll)}",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${if (isBn) "পিচ / ঢাল কোণ:" else "Pitch Angle:"} ${formatAngle(currentPitch)}",
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = recordNoteText,
                        onValueChange = { recordNoteText = it },
                        label = { Text(if (isBn) "নোট / রেফারেন্স (যেমন: ড্রয়িং ফ্রেম, সেলফ)" else "Note (e.g. Living Room Shelf)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                        val newRecord = LevelSavedRecord(
                            rollDeg = currentRoll,
                            pitchDeg = currentPitch,
                            note = recordNoteText.ifBlank { if (isBn) "লেভেল রেকর্ড" else "Level Record" },
                            timeFormatted = sdf.format(Date()),
                            isLevel = isHorizontalLevel || isVerticalLevel || isSurfaceFlat
                        )
                        savedRecords = listOf(newRecord) + savedRecords
                        recordNoteText = ""
                        showSaveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text(if (isBn) "সংরক্ষণ করুন" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// AR Camera Level Section Composable
// -------------------------------------------------------------
@Composable
private fun CameraArLevelSection(
    hasCameraPermission: Boolean,
    isCameraEnabled: Boolean,
    onToggleCameraEnabled: () -> Unit,
    onRequestPermission: () -> Unit,
    currentRoll: Float,
    currentPitch: Float,
    isFrozen: Boolean,
    onToggleFreeze: () -> Unit,
    zeroRollOffset: Float,
    zeroPitchOffset: Float,
    onZeroCalibrate: () -> Unit,
    isTorchOn: Boolean,
    onToggleTorch: () -> Unit,
    isFrontCamera: Boolean,
    onToggleCamera: () -> Unit,
    onCameraReady: (Camera) -> Unit,
    showLaserGrid: Boolean,
    onToggleGrid: () -> Unit,
    showProtractor: Boolean,
    onToggleProtractor: () -> Unit,
    showBullseye: Boolean,
    onToggleBullseye: () -> Unit,
    showPlumbLine: Boolean,
    onTogglePlumbLine: () -> Unit,
    isHorizontalLevel: Boolean,
    isVerticalLevel: Boolean,
    isSurfaceFlat: Boolean,
    selectedUnit: AngleUnit,
    onUnitChange: (AngleUnit) -> Unit,
    formatAngle: (Float) -> String,
    hapticFeedbackEnabled: Boolean,
    onToggleHaptic: () -> Unit,
    soundToneEnabled: Boolean,
    onToggleSound: () -> Unit,
    onSaveRecord: () -> Unit,
    isBn: Boolean,
    themeColors: CalculatorThemeColors
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val accentColor = themeColors.buttonOperatorBg
    val cardBorderColor = if (themeColors.isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    var cameraBindError by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .themeCardShadow(themeColors),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Action Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = when {
                            isHorizontalLevel || isVerticalLevel || isSurfaceFlat -> Color(0xFF10B981)
                            abs(currentRoll) <= 2.0f -> Color(0xFFF59E0B)
                            else -> Color(0xFFEF4444)
                        },
                        modifier = Modifier.size(12.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            isHorizontalLevel -> if (isBn) "নিখুঁত আনুভূমিক লেভেল (০°)" else "PERFECT HORIZONTAL LEVEL (0°)"
                            isVerticalLevel -> if (isBn) "নিখুঁত উলম্ব সোজা (৯০°)" else "PERFECT VERTICAL PLUMB (90°)"
                            isSurfaceFlat -> if (isBn) "সমতল ফ্ল্যাট সারফেস" else "PERFECT FLAT SURFACE"
                            abs(currentRoll) <= 2.0f -> if (isBn) "লেভেলের খুব কাছাকাছি" else "NEAR LEVEL (±2°)"
                            currentRoll > 0 -> if (isBn) "ডান দিকে ${String.format(Locale.US, "%.1f°", currentRoll)} কাত" else "Tilt Right ${String.format(Locale.US, "%.1f°", currentRoll)}"
                            else -> if (isBn) "বাম দিকে ${String.format(Locale.US, "%.1f°", abs(currentRoll))} কাত" else "Tilt Left ${String.format(Locale.US, "%.1f°", abs(currentRoll))}"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isHorizontalLevel || isVerticalLevel || isSurfaceFlat -> Color(0xFF10B981)
                            abs(currentRoll) <= 2.0f -> Color(0xFFF59E0B)
                            else -> themeColors.displayText
                        }
                    )
                }

                // Quick Unit Picker
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = themeColors.background,
                    modifier = Modifier.clickable {
                        val units = AngleUnit.values()
                        val nextIdx = (selectedUnit.ordinal + 1) % units.size
                        onUnitChange(units[nextIdx])
                    }
                ) {
                    Text(
                        text = selectedUnit.symbol,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Big Live Angle Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        when {
                            isHorizontalLevel || isVerticalLevel || isSurfaceFlat -> Color(0xFF10B981).copy(alpha = 0.15f)
                            abs(currentRoll) <= 2.0f -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                            else -> themeColors.background
                        }
                    )
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (isBn) "রোল (Roll / কাত)" else "Roll Angle",
                        fontSize = 11.sp,
                        color = themeColors.displayText.copy(alpha = 0.6f)
                    )
                    Text(
                        text = formatAngle(currentRoll),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = when {
                            isHorizontalLevel || isVerticalLevel -> Color(0xFF10B981)
                            abs(currentRoll) <= 2.0f -> Color(0xFFF59E0B)
                            else -> themeColors.displayText
                        }
                    )
                }

                Divider(
                    modifier = Modifier
                        .height(32.dp)
                        .width(1.dp),
                    color = cardBorderColor
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (isBn) "পিচ (Pitch / ঢাল)" else "Pitch Angle",
                        fontSize = 11.sp,
                        color = themeColors.displayText.copy(alpha = 0.6f)
                    )
                    Text(
                        text = formatAngle(currentPitch),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = if (abs(currentPitch) <= 0.6f) Color(0xFF10B981) else themeColors.displayText
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main AR Viewport Box (Camera Preview + Custom AR Overlay Canvas)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F172A))
                    .border(
                        width = if (isHorizontalLevel || isVerticalLevel || isSurfaceFlat) 3.dp else 1.5.dp,
                        color = if (isHorizontalLevel || isVerticalLevel || isSurfaceFlat) Color(0xFF10B981) else Color(0xFF334155),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                if (hasCameraPermission && isCameraEnabled && !cameraBindError) {
                    // Live Camera View wrapped safely
                    key(isFrontCamera) {
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx).apply {
                                    scaleType = PreviewView.ScaleType.FILL_CENTER
                                }
                                try {
                                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                    cameraProviderFuture.addListener({
                                        try {
                                            val cameraProvider = cameraProviderFuture.get()
                                            val preview = Preview.Builder().build().also {
                                                it.setSurfaceProvider(previewView.surfaceProvider)
                                            }
                                            val preferredSelector = if (isFrontCamera) {
                                                CameraSelector.DEFAULT_FRONT_CAMERA
                                            } else {
                                                CameraSelector.DEFAULT_BACK_CAMERA
                                            }

                                            val cameraSelector = if (cameraProvider.hasCamera(preferredSelector)) {
                                                preferredSelector
                                            } else if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                                                CameraSelector.DEFAULT_BACK_CAMERA
                                            } else if (cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                                                CameraSelector.DEFAULT_FRONT_CAMERA
                                            } else {
                                                null
                                            }

                                            if (cameraSelector != null) {
                                                cameraProvider.unbindAll()
                                                val camera = cameraProvider.bindToLifecycle(
                                                    lifecycleOwner,
                                                    cameraSelector,
                                                    preview
                                                )
                                                onCameraReady(camera)
                                            } else {
                                                cameraBindError = true
                                            }
                                        } catch (e: Throwable) {
                                            cameraBindError = true
                                            CrashReporter.logHandledException(ctx, "CameraXListener", e)
                                        }
                                    }, ContextCompat.getMainExecutor(ctx))
                                } catch (e: Throwable) {
                                    cameraBindError = true
                                    CrashReporter.logHandledException(ctx, "CameraXProvider", e)
                                }
                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else if (!hasCameraPermission) {
                    // Camera permission prompt
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            if (isBn) "ক্যামেরা প্রিভিউ ও AR লেভেলার সক্রিয় করুন" else "Enable Camera for AR Leveling",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            if (isBn) "বাস্তব জিনিসের উপর ক্যামেরা ধরে সোজা আছে কিনা তা দেখতে ক্যামেরার অনুমতি দিন" else "Grant camera permission to see live AR plumb & level lines superimposed on objects",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onRequestPermission,
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                            ) {
                                Text(if (isBn) "ক্যামেরা চালু করুন" else "Allow Camera")
                            }
                            OutlinedButton(
                                onClick = onToggleCameraEnabled,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text(if (isBn) "ভার্চুয়াল মোড" else "Virtual Mode")
                            }
                        }
                    }
                } else {
                    // Virtual HUD mode background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))
                                )
                            ),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.4f),
                            modifier = Modifier.padding(top = 10.dp)
                        ) {
                            Text(
                                text = if (isBn) "🌐 ভার্চুয়াল 3D সেন্সর গ্রিড মোড" else "🌐 Virtual 3D Sensor Grid Mode",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Custom AR Overlay HUD Drawn Over Camera or Virtual Screen
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f
                    val cy = h / 2f

                    // 1. Laser Grid
                    if (showLaserGrid) {
                        val gridPaintColor = Color.White.copy(alpha = 0.18f)
                        val strokeWidth = 1.dp.toPx()

                        drawLine(gridPaintColor, Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth)
                        drawLine(gridPaintColor, Offset(2 * w / 3f, 0f), Offset(2 * w / 3f, h), strokeWidth)
                        drawLine(gridPaintColor, Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth)
                        drawLine(gridPaintColor, Offset(0f, 2 * h / 3f), Offset(w, 2 * h / 3f), strokeWidth)
                    }

                    // 2. Center Bullseye Spirit Rings
                    if (showBullseye) {
                        val ringColor = when {
                            isHorizontalLevel || isVerticalLevel -> Color(0xFF10B981).copy(alpha = 0.85f)
                            abs(currentRoll) <= 2.0f -> Color(0xFFF59E0B).copy(alpha = 0.7f)
                            else -> Color.White.copy(alpha = 0.4f)
                        }

                        drawCircle(
                            color = ringColor.copy(alpha = 0.25f),
                            radius = 60.dp.toPx(),
                            center = Offset(cx, cy),
                            style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                        )
                        drawCircle(
                            color = ringColor.copy(alpha = 0.4f),
                            radius = 30.dp.toPx(),
                            center = Offset(cx, cy),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                        drawCircle(
                            color = if (isHorizontalLevel || isVerticalLevel || isSurfaceFlat) Color(0xFF10B981) else Color.White.copy(alpha = 0.6f),
                            radius = 12.dp.toPx(),
                            center = Offset(cx, cy),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }

                    // 3. Protractor Arc Scale (Top Half)
                    if (showProtractor) {
                        val arcRadius = min(w, h) * 0.38f
                        drawArc(
                            color = Color.White.copy(alpha = 0.25f),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(cx - arcRadius, cy - arcRadius),
                            size = Size(arcRadius * 2, arcRadius * 2),
                            style = Stroke(width = 1.5.dp.toPx())
                        )

                        for (deg in -90..90 step 10) {
                            val rad = Math.toRadians((deg - 90).toDouble())
                            val isMajor = deg % 30 == 0 || deg == 0 || deg == 45 || deg == -45
                            val tickLen = if (isMajor) 14.dp.toPx() else 7.dp.toPx()
                            val tickColor = if (deg == 0) Color(0xFF10B981) else if (isMajor) Color.White.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.35f)

                            val startX = cx + (arcRadius - tickLen) * cos(rad).toFloat()
                            val startY = cy + (arcRadius - tickLen) * sin(rad).toFloat()
                            val endX = cx + arcRadius * cos(rad).toFloat()
                            val endY = cy + arcRadius * sin(rad).toFloat()

                            drawLine(
                                color = tickColor,
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                            )
                        }
                    }

                    // 4. Dynamic Horizon / Horizontal Level Line (Rotates with Roll angle)
                    rotate(degrees = -currentRoll, pivot = Offset(cx, cy)) {
                        val levelLineColor = when {
                            isHorizontalLevel -> Color(0xFF10B981)
                            abs(currentRoll) <= 2.0f -> Color(0xFFF59E0B)
                            else -> Color(0xFF38BDF8).copy(alpha = 0.85f)
                        }
                        val lineThickness = if (isHorizontalLevel) 3.5.dp.toPx() else 2.dp.toPx()

                        drawLine(
                            color = levelLineColor,
                            start = Offset(24.dp.toPx(), cy),
                            end = Offset(w - 24.dp.toPx(), cy),
                            strokeWidth = lineThickness,
                            cap = StrokeCap.Round
                        )

                        drawLine(
                            color = levelLineColor,
                            start = Offset(24.dp.toPx(), cy - 14.dp.toPx()),
                            end = Offset(24.dp.toPx(), cy + 14.dp.toPx()),
                            strokeWidth = lineThickness,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = levelLineColor,
                            start = Offset(w - 24.dp.toPx(), cy - 14.dp.toPx()),
                            end = Offset(w - 24.dp.toPx(), cy + 14.dp.toPx()),
                            strokeWidth = lineThickness,
                            cap = StrokeCap.Round
                        )

                        drawCircle(
                            color = levelLineColor,
                            radius = 6.dp.toPx(),
                            center = Offset(cx, cy),
                            style = Stroke(width = lineThickness)
                        )
                    }

                    // 5. Vertical Plumb Line (90° / 270°)
                    if (showPlumbLine) {
                        val plumbColor = if (isVerticalLevel) Color(0xFF10B981) else Color(0xFFA855F7).copy(alpha = 0.75f)
                        val plumbWidth = if (isVerticalLevel) 3.dp.toPx() else 1.5.dp.toPx()

                        drawLine(
                            color = plumbColor,
                            start = Offset(cx, 20.dp.toPx()),
                            end = Offset(cx, h - 20.dp.toPx()),
                            strokeWidth = plumbWidth,
                            pathEffect = if (!isVerticalLevel) PathEffect.dashPathEffect(floatArrayOf(12f, 8f)) else null,
                            cap = StrokeCap.Round
                        )

                        val bobY = h - 28.dp.toPx()
                        val bobPath = Path().apply {
                            moveTo(cx - 10.dp.toPx(), bobY - 14.dp.toPx())
                            lineTo(cx + 10.dp.toPx(), bobY - 14.dp.toPx())
                            lineTo(cx, bobY + 12.dp.toPx())
                            close()
                        }
                        drawPath(bobPath, color = plumbColor)
                    }

                    // 6. Perfect Level Glowing Indicator
                    if (isHorizontalLevel || isVerticalLevel || isSurfaceFlat) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF10B981).copy(alpha = 0.5f), Color.Transparent),
                                center = Offset(cx, cy),
                                radius = 70.dp.toPx()
                            ),
                            center = Offset(cx, cy),
                            radius = 70.dp.toPx()
                        )
                    }
                }

                // Freeze Overlay Badge
                if (isFrozen) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0284C7).copy(alpha = 0.9f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.AcUnit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (isBn) "ফ্রেম ফ্রিজ / লকড" else "FROZEN",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Calibrated Offset Badge
                if (zeroRollOffset != 0f || zeroPitchOffset != 0f) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.9f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            if (isBn) "ক্যালিব্রেটেড (জিরো সেট)" else "CALIBRATED (ZERO)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // AR Overlay Quick Control Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Freeze Button
                FilledTonalButton(
                    onClick = onToggleFreeze,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isFrozen) Color(0xFF0284C7) else themeColors.background,
                        contentColor = if (isFrozen) Color.White else themeColors.displayText
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = if (isFrozen) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (isFrozen) (if (isBn) "আনফ্রিজ" else "Unfreeze") else (if (isBn) "ফ্রিজ ফ্রেম" else "Freeze"),
                        fontSize = 12.sp
                    )
                }

                // Zero Calibrate Button
                FilledTonalButton(
                    onClick = onZeroCalibrate,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (zeroRollOffset != 0f) Color(0xFFF59E0B) else themeColors.background,
                        contentColor = if (zeroRollOffset != 0f) Color.White else themeColors.displayText
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (zeroRollOffset != 0f) (if (isBn) "রিসেট জিরো" else "Reset Zero") else (if (isBn) "জিরো সেট" else "Set Zero"),
                        fontSize = 12.sp
                    )
                }

                // Camera Background Toggle
                FilterChip(
                    selected = isCameraEnabled && hasCameraPermission,
                    onClick = onToggleCameraEnabled,
                    label = { Text(if (isBn) "ক্যামেরা প্রিভিউ" else "Camera View", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Videocam, null, modifier = Modifier.size(16.dp)) }
                )

                // Laser Grid Toggle
                FilterChip(
                    selected = showLaserGrid,
                    onClick = onToggleGrid,
                    label = { Text(if (isBn) "গ্রিড" else "Grid", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.GridOn, null, modifier = Modifier.size(16.dp)) }
                )

                // Protractor Toggle
                FilterChip(
                    selected = showProtractor,
                    onClick = onToggleProtractor,
                    label = { Text(if (isBn) "চাঁদা/আর্ক" else "Protractor", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.PieChart, null, modifier = Modifier.size(16.dp)) }
                )

                // Plumb line Toggle
                FilterChip(
                    selected = showPlumbLine,
                    onClick = onTogglePlumbLine,
                    label = { Text(if (isBn) "উলম্ব প্লাম্ব" else "Plumb", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Straighten, null, modifier = Modifier.size(16.dp)) }
                )

                // Flashlight / Torch
                if (hasCameraPermission && isCameraEnabled) {
                    FilledIconButton(
                        onClick = onToggleTorch,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isTorchOn) Color(0xFFF59E0B) else themeColors.background,
                            contentColor = if (isTorchOn) Color.White else themeColors.displayText
                        ),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Torch",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Flip Camera
                    FilledIconButton(
                        onClick = onToggleCamera,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = themeColors.background,
                            contentColor = themeColors.displayText
                        ),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlipCameraAndroid,
                            contentDescription = "Flip Camera",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Haptic feedback toggle
                FilledIconButton(
                    onClick = onToggleHaptic,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (hapticFeedbackEnabled) accentColor.copy(alpha = 0.2f) else themeColors.background,
                        contentColor = if (hapticFeedbackEnabled) accentColor else themeColors.displayText.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "Haptic",
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Sound beep toggle
                FilledIconButton(
                    onClick = onToggleSound,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (soundToneEnabled) accentColor.copy(alpha = 0.2f) else themeColors.background,
                        contentColor = if (soundToneEnabled) accentColor else themeColors.displayText.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (soundToneEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Sound",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Save Measurement Button
            Button(
                onClick = onSaveRecord,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBn) "বর্তমান কোণ পরিমাপ সেভ করুন" else "Save Angle Measurement",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 2D Surface Bubble Level Section
// -------------------------------------------------------------
@Composable
private fun SurfaceBubbleLevelSection(
    currentRoll: Float,
    currentPitch: Float,
    isSurfaceFlat: Boolean,
    formatAngle: (Float) -> String,
    zeroRollOffset: Float,
    onZeroCalibrate: () -> Unit,
    isFrozen: Boolean,
    onToggleFreeze: () -> Unit,
    isBn: Boolean,
    themeColors: CalculatorThemeColors
) {
    val accentColor = themeColors.buttonOperatorBg

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .themeCardShadow(themeColors),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (isBn) "সারফেস বাবল লেভেল (সমতল টেবিল/ফ্লোর)" else "2D Surface Bullseye Spirit Level",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = themeColors.displayText
            )
            Text(
                if (isBn) "ফোনটি টেবিল, মেঝে বা সমতল পৃষ্ঠের উপর রাখুন" else "Place phone flat on table, floor, countertop, or shelf",
                fontSize = 12.sp,
                color = themeColors.displayText.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2D Circular Bullseye Bubble Meter
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A))
                    .border(
                        width = if (isSurfaceFlat) 4.dp else 2.dp,
                        color = if (isSurfaceFlat) Color(0xFF10B981) else Color(0xFF334155),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f
                    val cy = h / 2f
                    val maxRadius = w / 2f - 16.dp.toPx()

                    val ringColors = Color.White.copy(alpha = 0.2f)

                    drawCircle(ringColors, radius = maxRadius, center = Offset(cx, cy), style = Stroke(1.dp.toPx()))
                    drawCircle(ringColors, radius = maxRadius * 0.66f, center = Offset(cx, cy), style = Stroke(1.dp.toPx()))
                    drawCircle(ringColors, radius = maxRadius * 0.33f, center = Offset(cx, cy), style = Stroke(1.dp.toPx()))
                    drawCircle(
                        color = if (isSurfaceFlat) Color(0xFF10B981) else Color(0xFF38BDF8),
                        radius = 18.dp.toPx(),
                        center = Offset(cx, cy),
                        style = Stroke(2.dp.toPx())
                    )

                    drawLine(ringColors, Offset(cx, 16.dp.toPx()), Offset(cx, h - 16.dp.toPx()), 1.dp.toPx())
                    drawLine(ringColors, Offset(16.dp.toPx(), cy), Offset(w - 16.dp.toPx(), cy), 1.dp.toPx())

                    val scaleFactor = maxRadius / 15f
                    val bubbleOffsetX = (currentRoll * scaleFactor).coerceIn(-maxRadius, maxRadius)
                    val bubbleOffsetY = (-currentPitch * scaleFactor).coerceIn(-maxRadius, maxRadius)

                    val bubbleCenter = Offset(cx + bubbleOffsetX, cy + bubbleOffsetY)
                    val bubbleColor = if (isSurfaceFlat) Color(0xFF10B981) else Color(0xFF38BDF8)

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(bubbleColor.copy(alpha = 0.6f), Color.Transparent),
                            center = bubbleCenter,
                            radius = 28.dp.toPx()
                        ),
                        radius = 28.dp.toPx(),
                        center = bubbleCenter
                    )

                    drawCircle(
                        color = bubbleColor,
                        radius = 16.dp.toPx(),
                        center = bubbleCenter
                    )

                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = 4.dp.toPx(),
                        center = Offset(bubbleCenter.x - 4.dp.toPx(), bubbleCenter.y - 4.dp.toPx())
                    )
                }

                if (isSurfaceFlat) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.9f),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = if (isBn) "✓ সমতল সারফেস" else "✓ PERFECT LEVEL",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // X and Y Linear Bubble Gauges
            LinearBubbleBar(
                title = if (isBn) "X-অক্ষ (Roll / কাত)" else "X-Axis (Roll)",
                valueDeg = currentRoll,
                isLevel = abs(currentRoll) <= 0.6f,
                formatAngle = formatAngle,
                themeColors = themeColors
            )

            Spacer(modifier = Modifier.height(10.dp))

            LinearBubbleBar(
                title = if (isBn) "Y-অক্ষ (Pitch / ঢাল)" else "Y-Axis (Pitch)",
                valueDeg = currentPitch,
                isLevel = abs(currentPitch) <= 0.6f,
                formatAngle = formatAngle,
                themeColors = themeColors
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Calibrate & Freeze Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onZeroCalibrate,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (zeroRollOffset != 0f) (if (isBn) "রিসেট" else "Reset") else (if (isBn) "ক্যালিব্রেট" else "Calibrate"))
                }

                Button(
                    onClick = onToggleFreeze,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFrozen) Color(0xFF0284C7) else accentColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isFrozen) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isFrozen) (if (isBn) "লকড" else "Locked") else (if (isBn) "হোল্ড" else "Hold"))
                }
            }
        }
    }
}

@Composable
private fun LinearBubbleBar(
    title: String,
    valueDeg: Float,
    isLevel: Boolean,
    formatAngle: (Float) -> String,
    themeColors: CalculatorThemeColors
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontSize = 12.sp, color = themeColors.displayText.copy(alpha = 0.7f))
            Text(
                formatAngle(valueDeg),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isLevel) Color(0xFF10B981) else themeColors.displayText
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, if (isLevel) Color(0xFF10B981) else Color(0xFF334155), RoundedCornerShape(10.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h / 2f

                drawLine(
                    color = Color.White.copy(alpha = 0.3f),
                    start = Offset(cx, 0f),
                    end = Offset(cx, h),
                    strokeWidth = 2.dp.toPx()
                )

                val maxOffset = w / 2f - 12.dp.toPx()
                val offsetNorm = (valueDeg / 15f).coerceIn(-1f, 1f)
                val bubbleX = cx + offsetNorm * maxOffset

                val bubbleColor = if (isLevel) Color(0xFF10B981) else Color(0xFF38BDF8)
                drawCircle(
                    color = bubbleColor,
                    radius = 7.dp.toPx(),
                    center = Offset(bubbleX, cy)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Plumb Bob Vertical Section
// -------------------------------------------------------------
@Composable
private fun PlumbBobVerticalSection(
    currentRoll: Float,
    isVerticalLevel: Boolean,
    formatAngle: (Float) -> String,
    isBn: Boolean,
    themeColors: CalculatorThemeColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .themeCardShadow(themeColors),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (isBn) "উলম্ব প্লাম্ব লাইন (দেয়াল ও কলাম লেভেলার)" else "Vertical Plumb Line & Wall Leveler",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = themeColors.displayText
            )
            Text(
                if (isBn) "দেয়াল, দরজা, কলাম বা খাড়া খুঁটি উলম্বভাবে ৯০° সোজা আছে কিনা তা নিখুঁতভাবে মাপুন" else "Check if walls, doors, pillars, and vertical frames are true 90° plumb straight",
                fontSize = 12.sp,
                color = themeColors.displayText.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Plumb Bob Pendulum Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F172A))
                    .border(
                        1.5.dp,
                        if (isVerticalLevel) Color(0xFF10B981) else Color(0xFF334155),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f
                    val topY = 20.dp.toPx()

                    drawCircle(Color(0xFF64748B), radius = 6.dp.toPx(), center = Offset(cx, topY))

                    val deviationDeg = if (currentRoll >= 0) (currentRoll - 90f) else (currentRoll + 90f)
                    val rad = Math.toRadians((deviationDeg + 90.0)).toFloat()

                    val stringLength = h - 60.dp.toPx()
                    val bobX = cx + stringLength * cos(rad)
                    val bobY = topY + stringLength * sin(rad)

                    val plumbColor = if (isVerticalLevel) Color(0xFF10B981) else Color(0xFFE2E8F0)

                    drawLine(
                        color = plumbColor.copy(alpha = 0.85f),
                        start = Offset(cx, topY),
                        end = Offset(bobX, bobY),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    val conePath = Path().apply {
                        moveTo(bobX - 14.dp.toPx(), bobY - 20.dp.toPx())
                        lineTo(bobX + 14.dp.toPx(), bobY - 20.dp.toPx())
                        lineTo(bobX, bobY + 18.dp.toPx())
                        close()
                    }
                    drawPath(conePath, color = plumbColor)

                    drawLine(
                        color = Color(0xFF10B981).copy(alpha = 0.35f),
                        start = Offset(cx, topY),
                        end = Offset(cx, h - 20.dp.toPx()),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isVerticalLevel) Color(0xFF10B981).copy(alpha = 0.9f) else Color(0xFF334155),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = if (isVerticalLevel) {
                            if (isBn) "✓ ৯০.০° নিখুঁত উলম্ব সোজা (True Plumb)" else "✓ 90.0° TRUE VERTICAL PLUMB"
                        } else {
                            "${if (isBn) "বর্তমান কোণ:" else "Current Angle:"} ${formatAngle(abs(currentRoll))}"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Standard Construction & DIY Angles Guide
// -------------------------------------------------------------
@Composable
private fun StandardAnglesGuideSection(
    currentRoll: Float,
    isBn: Boolean,
    themeColors: CalculatorThemeColors
) {
    val accentColor = themeColors.buttonOperatorBg
    val cardBorderColor = if (themeColors.isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    val guides = remember {
        listOf(
            Triple(if (isBn) "আনুভূমিক সমতল (ফ্লোর, টেবিল)" else "Horizontal Level (Table, Shelf)", 0.0f, "0°"),
            Triple(if (isBn) "ড্রেনেজ ও পানির পাইপ স্লোপ" else "Drainage & Sewer Pipe Slope", 1.2f, "1.2° (~2%)"),
            Triple(if (isBn) "হুইলচেয়ার র‍্যাম্প স্ট্যান্ডার্ড" else "Wheelchair Ramp (ADA)", 4.8f, "4.8° (1:12)"),
            Triple(if (isBn) "সোলার প্যানেল কোণ (বাংলাদেশ)" else "Solar Panel Tilt (Bangladesh)", 23.5f, "23.5°"),
            Triple(if (isBn) "সিঁড়ির স্ট্যান্ডার্ড কোণ (Stairs)" else "Standard Stairs Incline", 34.0f, "30° - 35°"),
            Triple(if (isBn) "টিনের চাল / ছাদের স্লোপ (Roof Pitch)" else "Standard Roof Pitch (4:12)", 18.4f, "18.4°"),
            Triple(if (isBn) "কাঠের ফ্রেম কর্নার জয়েন্ট (Mitre)" else "Carpentry Miter Corner", 45.0f, "45.0°"),
            Triple(if (isBn) "উলম্ব খাঁড়া দেয়াল / কলাম (Plumb)" else "Vertical Plumb Wall / Pillar", 90.0f, "90.0°")
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .themeCardShadow(themeColors),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                if (isBn) "প্রয়োজনীয় স্ট্যান্ডার্ড কোণ ও লেভেল রেফারেন্স" else "Standard Angle & Slope Quick Reference",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = themeColors.displayText
            )
            Spacer(modifier = Modifier.height(14.dp))

            guides.forEach { (name, targetDeg, label) ->
                val isMatched = abs(currentRoll - targetDeg) <= 1.0f

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isMatched) Color(0xFF10B981).copy(alpha = 0.15f) else themeColors.background,
                    border = BorderStroke(1.dp, if (isMatched) Color(0xFF10B981) else cardBorderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = themeColors.displayText
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isMatched) Color(0xFF10B981) else accentColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMatched) Color.White else accentColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Saved Records Card Composable
// -------------------------------------------------------------
@Composable
private fun SavedRecordsCard(
    records: List<LevelSavedRecord>,
    onDelete: (String) -> Unit,
    onClearAll: () -> Unit,
    isBn: Boolean,
    themeColors: CalculatorThemeColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .themeCardShadow(themeColors),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBn) "সংরক্ষিত পরিমাপ তালিকা" else "Saved Angle Records",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = themeColors.displayText
                )
                TextButton(onClick = onClearAll) {
                    Text(if (isBn) "মুছে ফেলুন" else "Clear All", fontSize = 12.sp, color = Color.Red)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            records.take(5).forEach { record ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = themeColors.background,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = record.note,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = themeColors.displayText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${if (isBn) "রোল:" else "Roll:"} ${String.format(Locale.US, "%.1f°", record.rollDeg)} | ${if (isBn) "পিচ:" else "Pitch:"} ${String.format(Locale.US, "%.1f°", record.pitchDeg)} • ${record.timeFormatted}",
                                fontSize = 11.sp,
                                color = themeColors.displayText.copy(alpha = 0.6f)
                            )
                        }

                        IconButton(
                            onClick = { onDelete(record.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = Color.Red.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
