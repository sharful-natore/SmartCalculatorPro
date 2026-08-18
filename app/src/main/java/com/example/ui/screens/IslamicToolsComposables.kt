package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import com.example.ui.islamic.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.namaz.NamazViewModel
import android.content.Context
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar

// --- 1. QIBLA COMPASS CARD ---
@Composable
fun QiblaCompassCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    val context = LocalContext.current
    var rawPhoneAngle by remember { mutableFloatStateOf(0f) }
    var calibrationOffset by remember { mutableFloatStateOf(0f) }
    var isHardwareSensorActive by remember { mutableStateOf(false) }
    var sensorAccuracy by remember { mutableIntStateOf(SensorManager.SENSOR_STATUS_ACCURACY_HIGH) }
    var showCalibrationDialog by remember { mutableStateOf(false) }
    
    // Exact Astronomical Qibla Bearing from Bangladesh (Dhaka / Central BD) to Makkah (Kaaba)
    // Formula: Great circle azimuth from (23.81°N, 90.41°E) to (21.42°N, 39.83°E) = 277.6° (West-Northwest / পশ্চিম দিক)
    val qiblaBearing = 277.6f

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val orientationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        val accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val lastAccel = FloatArray(3)
        val lastMagnet = FloatArray(3)
        var isAccelSet = false
        var isMagnetSet = false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                if (event.sensor.type == Sensor.TYPE_ORIENTATION) {
                    isHardwareSensorActive = true
                    rawPhoneAngle = (event.values[0] + 360f) % 360f
                } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, lastAccel, 0, event.values.size)
                    isAccelSet = true
                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, lastMagnet, 0, event.values.size)
                    isMagnetSet = true
                }

                if (isAccelSet && isMagnetSet) {
                    val r = FloatArray(9)
                    val i = FloatArray(9)
                    if (SensorManager.getRotationMatrix(r, i, lastAccel, lastMagnet)) {
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(r, orientation)
                        val azimuthInDegrees = (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f
                        isHardwareSensorActive = true
                        rawPhoneAngle = azimuthInDegrees
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                sensorAccuracy = accuracy
            }
        }

        if (orientationSensor != null) {
            sensorManager?.registerListener(listener, orientationSensor, SensorManager.SENSOR_DELAY_UI)
        }
        if (accelSensor != null && magnetSensor != null) {
            sensorManager?.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
            sensorManager?.registerListener(listener, magnetSensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    val phoneAngle = (rawPhoneAngle + calibrationOffset + 360f) % 360f
    val angleDiff = Math.abs((phoneAngle - qiblaBearing + 360f) % 360f)
    val isAligned = angleDiff < 6f || angleDiff > 354f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isBn) "কিবলা নির্দেশক" else "Qibla Direction Finder",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    Text(
                        text = if (isBn) "মক্কা শরীফ (আল-কাবা) দিক: ২৭৭.৬° (পশ্চিম)" else "Makkah (Al-Kaaba) Bearing: 277.6° (West)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF10B981)
                    )
                }

                // Sensor calibration quick button
                FilledTonalButton(
                    onClick = { showCalibrationDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = themeColors.buttonEqualBg.copy(alpha = 0.12f),
                        contentColor = themeColors.buttonEqualBg
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Calibrate",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBn) "ক্যালিব্রেট" else "Calibrate",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status chip (Live / Simulated)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isHardwareSensorActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBn) "লাইভ সেন্সর সক্রিয়" else "Live Compass Sensor",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }

                // Current heading chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(themeColors.displayBackground)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${phoneAngle.toInt()}° " + run {
                            when ((phoneAngle % 360).toInt()) {
                                in 338..360, in 0..22 -> if (isBn) "উত্তর (N)" else "N"
                                in 23..67 -> if (isBn) "উত্তর-পূর্ব (NE)" else "NE"
                                in 68..112 -> if (isBn) "পূর্ব (E)" else "E"
                                in 113..157 -> if (isBn) "দক্ষিণ-পূর্ব (SE)" else "SE"
                                in 158..202 -> if (isBn) "দক্ষিণ (S)" else "S"
                                in 203..247 -> if (isBn) "দক্ষিণ-পশ্চিম (SW)" else "SW"
                                in 248..292 -> if (isBn) "পশ্চিম (W) ★" else "W (West)"
                                else -> if (isBn) "উত্তর-পশ্চিম (NW)" else "NW"
                            }
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fixed Top Heading Indicator Triangle
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = if (isAligned) Color(0xFF10B981) else Color(0xFFEF4444),
                modifier = Modifier
                    .size(24.dp)
                    .rotate(180f)
            )

            // Compass Dial Box
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(themeColors.background)
                    .border(
                        width = if (isAligned) 4.dp else 2.dp,
                        color = if (isAligned) Color(0xFF10B981) else themeColors.buttonEqualBg.copy(alpha = 0.35f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Dial with Degree ticks and Cardinal Directions (Rotates by -phoneAngle)
                androidx.compose.foundation.Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(-phoneAngle)
                ) {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // Draw 360 degree ticks
                    for (degree in 0 until 360 step 5) {
                        val angleRad = (degree - 90) * (Math.PI / 180.0)
                        val isMajor = degree % 30 == 0
                        val isCardinal = degree % 90 == 0
                        val isQiblaMark = Math.abs(degree - 278) <= 2
                        
                        val tickLength = when {
                            isQiblaMark -> radius * 0.22f
                            isCardinal -> radius * 0.16f
                            isMajor -> radius * 0.12f
                            else -> radius * 0.06f
                        }
                        
                        val strokeW = when {
                            isQiblaMark -> 4.dp.toPx()
                            isCardinal -> 3.dp.toPx()
                            isMajor -> 2.dp.toPx()
                            else -> 1.dp.toPx()
                        }
                        
                        val tickColor = when {
                            isQiblaMark -> Color(0xFF10B981)
                            degree == 0 -> Color(0xFFEF4444) // North in Red
                            isCardinal -> Color(0xFF38BDF8)
                            isMajor -> Color.Gray.copy(alpha = 0.7f)
                            else -> Color.Gray.copy(alpha = 0.3f)
                        }

                        val outerX = center.x + (radius - 8.dp.toPx()) * Math.cos(angleRad).toFloat()
                        val outerY = center.y + (radius - 8.dp.toPx()) * Math.sin(angleRad).toFloat()
                        val innerX = center.x + (radius - 8.dp.toPx() - tickLength) * Math.cos(angleRad).toFloat()
                        val innerY = center.y + (radius - 8.dp.toPx() - tickLength) * Math.sin(angleRad).toFloat()

                        drawLine(
                            color = tickColor,
                            start = Offset(innerX, innerY),
                            end = Offset(outerX, outerY),
                            strokeWidth = strokeW,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                }

                // Cardinal Labels on Dial (Rotating with dial)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(-phoneAngle)
                        .padding(26.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // North (0°)
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                        Text("N", fontWeight = FontWeight.ExtraBold, color = Color(0xFFEF4444), fontSize = 15.sp)
                    }
                    // East (90°)
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                        Text("E", fontWeight = FontWeight.Bold, color = themeColors.displayText.copy(alpha = 0.8f), fontSize = 14.sp)
                    }
                    // South (180°)
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                        Text("S", fontWeight = FontWeight.Bold, color = themeColors.displayText.copy(alpha = 0.8f), fontSize = 14.sp)
                    }
                    // West (270° - Qibla side!)
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("W", fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981), fontSize = 15.sp)
                            Text("পশ্চিম", fontSize = 8.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Qibla Pointer Needle (Rotates to point towards Kaaba: qiblaBearing - phoneAngle)
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .rotate(-phoneAngle + qiblaBearing),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        // Green Kaaba Arrow Pointer
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "Qibla Pointer",
                                tint = if (isAligned) Color(0xFF10B981) else Color(0xFF059669),
                                modifier = Modifier.size(44.dp)
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isAligned) Color(0xFF10B981) else Color(0xFF059669)
                            ) {
                                Text(
                                    text = if (isBn) "কিবলা" else "Qibla",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                // Center Kaaba Emblem Hub
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (isAligned) Color(0xFF10B981) else themeColors.buttonEqualBg)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mosque,
                        contentDescription = "Kaaba Makkah",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Alignment Status Banner
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = if (isAligned) Color(0xFF10B981).copy(alpha = 0.2f) else themeColors.displayText.copy(alpha = 0.08f),
                border = if (isAligned) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)) else null
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isAligned) Icons.Default.CheckCircle else Icons.Default.Explore,
                        contentDescription = null,
                        tint = if (isAligned) Color(0xFF10B981) else themeColors.displayText.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAligned) {
                            if (isBn) "যথাযথ কিবলা অভিমুখে রয়েছেন! (কাবা শরীফ - পশ্চিম দিক)" else "Perfectly Aligned with Qibla! (West)"
                        } else {
                            if (isBn) "কমপাস ঘুরিয়ে কিবলা মিলিয়ে নিন" else "Rotate phone to align with Qibla"
                        },
                        fontWeight = FontWeight.Bold,
                        color = if (isAligned) Color(0xFF10B981) else themeColors.displayText,
                        fontSize = 13.sp
                    )
                }
            }

            // Dynamic Rotation Guidance indicating how many degrees to rotate in which direction
            if (!isAligned) {
                val diff = run {
                    var d = qiblaBearing - phoneAngle
                    while (d < -180f) d += 360f
                    while (d > 180f) d -= 360f
                    d
                }
                val degreesToRotate = Math.abs(diff).toInt()
                val directionText = if (diff > 0) {
                    if (isBn) "ডানে (ঘড়ির কাটার দিকে) $degreesToRotate° ঘুরুন" else "Rotate Right (Clockwise) by $degreesToRotate°"
                } else {
                    if (isBn) "বামে (ঘড়ির কাটার বিপরীত দিকে) $degreesToRotate° ঘুরুন" else "Rotate Left (Counter-Clockwise) by $degreesToRotate°"
                }

                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.displayBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (diff > 0) Icons.Default.RotateRight else Icons.Default.RotateLeft,
                            contentDescription = null,
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = directionText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Auto Align Button + Manual Offset Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { rawPhoneAngle = qiblaBearing; calibrationOffset = 0f },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBn) "পশ্চিম দিক সেট (২৭৮°)" else "Set West (278°)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = { rawPhoneAngle = (rawPhoneAngle + 45f) % 360f },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(0.7f)
                ) {
                    Icon(Icons.Default.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBn) "ঘুরান (+৪৫°)" else "Rotate",
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Manual Slider for testing / manual compensation
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isBn) "কমপাস কোণ: ${phoneAngle.toInt()}°" else "Angle: ${phoneAngle.toInt()}°",
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.7f)
                    )
                    Text(
                        text = if (isBn) "কিবলা: ২৭৭.৬° (পশ্চিম)" else "Qibla: 277.6° (West)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
                Slider(
                    value = phoneAngle,
                    onValueChange = { rawPhoneAngle = it; calibrationOffset = 0f },
                    valueRange = 0f..360f,
                    colors = SliderDefaults.colors(thumbColor = themeColors.buttonEqualBg, activeTrackColor = themeColors.buttonEqualBg)
                )
            }

            // Distance & Angle Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isBn) "দূরত্ব: ৫,১৪৮ কি.মি. (মাক্কাহ)" else "Distance: 5,148 km (Makkah)",
                    fontSize = 11.sp,
                    color = themeColors.displayText.copy(alpha = 0.6f)
                )
                Text(
                    text = if (isBn) "দিক: পশ্চিম (২৭৭.৬° WNW)" else "Bearing: 277.6° WNW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Accuracy Disclaimer and Sensor Explanation Note
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.displayBackground.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Sensor Info",
                        tint = themeColors.buttonEqualBg,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isBn)
                            "ফোনের ম্যাগনেটিক সেন্সরের নির্ভুলতা, ব্যাক-কভার বা আশেপাশের চৌম্বকীয় হস্তক্ষেপের কারণে কম্পাসে কখনো কিছুটা তারতম্য হতে পারে। তবে অ্যাপে জ্যোতির্বৈজ্ঞানিক ও ভৌগোলিক সর্বোচ্চ নির্ভুল সূত্র (বাংলাদেশ হতে ২৭৭.৬° পশ্চিম দিক) ব্যবহার করা হয়েছে। সেরা ফলাফলের জন্য খোলা জায়গায় ফোনটি সমতলে রেখে 'ক্যালিব্রেট' করে নিন।"
                        else
                            "Device magnetic sensor accuracy may vary slightly due to metal cases or environmental interference. The app uses the highest astronomical & geographical precision (277.6° West from Bangladesh). For best results, place the phone flat and calibrate the sensor in an open area.",
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = themeColors.displayText.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }

    // Sensor Calibration Dialog
    if (showCalibrationDialog) {
        AlertDialog(
            onDismissRequest = { showCalibrationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = themeColors.buttonEqualBg
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "কমপাস সেন্সর ক্যালিব্রেশন" else "Compass Sensor Calibration",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = themeColors.displayText
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isBn)
                            "সঠিক দিক নির্দেশনার জন্য আপনার ফোনের ম্যাগনেটোমিটার সেন্সর নিয়মিত ক্যালিব্রেট করা প্রয়োজন।"
                        else
                            "To ensure precise direction tracking, calibrate your device's magnetometer sensor.",
                        fontSize = 13.sp,
                        color = themeColors.displayText.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Step 1: Figure-8 Motion Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = themeColors.displayBackground),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "♾️ " + (if (isBn) "চিত্র-৮ (Figure-8) মোশন" else "Figure-8 Motion"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = themeColors.buttonEqualBg
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isBn)
                                    "ফোনটি হাতে নিয়ে বাতাসে ইংরেজি '8' অক্ষরের মতো ২-৩ বার মসৃণভাবে ঘোরান। এটি ফোনের সেন্সরের চৌম্বকীয় ফিল্ড রিসেট করে।"
                                else
                                    "Hold your phone and wave it in a smooth figure-8 (∞) motion 2-3 times in the air to recalibrate the magnetic field.",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = themeColors.displayText.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sensor Accuracy Indicator
                    val accuracyText = when (sensorAccuracy) {
                        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> if (isBn) "উচ্চ নির্ভুলতা (High) ★★★" else "High Accuracy ★★★"
                        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> if (isBn) "মাঝারি (Medium) ★★☆" else "Medium Accuracy ★★☆"
                        else -> if (isBn) "ক্যালিব্রেট করুন (Low) ★☆☆" else "Needs Calibration ★☆☆"
                    }
                    val accuracyColor = when (sensorAccuracy) {
                        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> Color(0xFF10B981)
                        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> Color(0xFFF59E0B)
                        else -> Color(0xFFEF4444)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "সেন্সর নির্ভুলতা স্তর:" else "Sensor Status:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = themeColors.displayText
                        )
                        Text(
                            text = accuracyText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = accuracyColor
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Manual Adjustment Slider
                    Text(
                        text = if (isBn)
                            "ম্যানুয়াল অফসেট অ্যাডজাস্টমেন্ট: ${if (calibrationOffset > 0) "+${calibrationOffset.toInt()}" else "${calibrationOffset.toInt()}"}°"
                        else
                            "Manual Offset Adjustment: ${if (calibrationOffset > 0) "+${calibrationOffset.toInt()}" else "${calibrationOffset.toInt()}"}°",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = themeColors.displayText
                    )

                    Slider(
                        value = calibrationOffset,
                        onValueChange = { calibrationOffset = it },
                        valueRange = -30f..30f,
                        colors = SliderDefaults.colors(
                            thumbColor = themeColors.buttonEqualBg,
                            activeTrackColor = themeColors.buttonEqualBg
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { calibrationOffset = 0f }) {
                            Text(
                                text = if (isBn) "অফসেট রিসেট (০°)" else "Reset (0°)",
                                fontSize = 11.sp,
                                color = themeColors.displayText.copy(alpha = 0.7f)
                            )
                        }
                        Text(
                            text = "-30° থেকে +30°",
                            fontSize = 10.sp,
                            color = themeColors.displayText.copy(alpha = 0.5f),
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCalibrationDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isBn) "ঠিক আছে" else "Done",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// --- 2. DIGITAL TASBIH CARD ---

enum class TasbihVibrationType {
    TAP,            // Single light tap (20ms)
    MILESTONE_33,   // 3 short distinct vibrations for 33
    MILESTONE_99    // 1 long prominent vibration for 99 / 100
}

/**
 * Ultra-safe vibration trigger designed to work reliably across all Android devices,
 * including legacy Oppo A5s (ColorOS/Android 8.1), Xiaomi, Vivo, Samsung, and Android 12-15+.
 * Completely shielded with try-catch and hardware availability checks to prevent any app crashes.
 */
fun triggerTasbihVibration(context: Context, type: TasbihVibrationType) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        if (vibrator == null || !vibrator.hasVibrator()) return

        when (type) {
            TasbihVibrationType.TAP -> {
                // Short light tap vibration (25ms)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createOneShot(25L, VibrationEffect.DEFAULT_AMPLITUDE)
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(25L)
                }
            }
            TasbihVibrationType.MILESTONE_33 -> {
                // 3 short vibrations (50ms on, 60ms off, 50ms on, 60ms off, 50ms on)
                val timings = longArrayOf(0L, 50L, 60L, 50L, 60L, 50L)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val amplitudes = intArrayOf(0, 220, 0, 220, 0, 220)
                    val effect = try {
                        VibrationEffect.createWaveform(timings, amplitudes, -1)
                    } catch (e: Throwable) {
                        VibrationEffect.createWaveform(timings, -1)
                    }
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(timings, -1)
                }
            }
            TasbihVibrationType.MILESTONE_99 -> {
                // 1 long prominent vibration (350ms)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createOneShot(350L, VibrationEffect.DEFAULT_AMPLITUDE)
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(350L)
                }
            }
        }
    } catch (e: Throwable) {
        // Silently and safely ignore any vendor-specific haptic exceptions
    }
}

/**
 * Safe sound click feedback
 */
fun triggerTasbihSound() {
    try {
        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 40)
        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
    } catch (e: Throwable) {
        // Ignored
    }
}

@Composable
fun DigitalTasbihCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    val context = LocalContext.current

    var count by remember { mutableIntStateOf(0) }
    var targetCount by remember { mutableIntStateOf(33) }
    var totalCount by remember { mutableIntStateOf(0) }
    var completedRounds by remember { mutableIntStateOf(0) }
    var selectedZikirIndex by remember { mutableIntStateOf(0) }

    // Sound & Vibration Toggles
    var isVibrationEnabled by remember { mutableStateOf(true) }
    var isSoundEnabled by remember { mutableStateOf(false) }

    // Dialog States
    var showResetDialog by remember { mutableStateOf(false) }
    var showCustomTargetDialog by remember { mutableStateOf(false) }
    var customTargetInput by remember { mutableStateOf("100") }

    val zikirShortNames = remember(isBn) {
        if (isBn) listOf(
            "সুবহানাল্লাহ",
            "আলহামদুলিল্লাহ",
            "আল্লাহু আকবার",
            "আস্তাগফিরুল্লাহ",
            "লা ইলাহা ইল্লাল্লাহ",
            "সুবহানাল্লাহি...",
            "লা হাওলা...",
            "হাসবুনাল্লাহু...",
            "দরুদ শরীফ"
        ) else listOf(
            "SubhanAllah",
            "Alhamdulillah",
            "Allahu Akbar",
            "Astagfirullah",
            "La ilaha...",
            "Subhanallahi...",
            "La hawla...",
            "Hasbunallahu...",
            "Darood"
        )
    }

    val zikirs = remember(isBn) {
        listOf(
            Pair("سُبْحَانَ اللَّهِ", if (isBn) "সুবহানাল্লাহ (আল্লাহ পবিত্র)" else "SubhanAllah (Glory be to Allah)"),
            Pair("الْحَمْدُ لِلَّهِ", if (isBn) "আলহামদুলিল্লাহ (সকল প্রশংসা আল্লাহর)" else "Alhamdulillah (Praise be to Allah)"),
            Pair("اللَّهُ أَكْبَرُ", if (isBn) "আল্লাহু আকবার (আল্লাহ সবচেয়ে মহান)" else "Allahu Akbar (Allah is the Greatest)"),
            Pair("أَسْتَغْفِرُ اللَّهَ", if (isBn) "আস্তাগফিরুল্লাহ (আল্লাহর কাছে ক্ষমা প্রার্থনা)" else "Astagfirullah (I seek forgiveness from Allah)"),
            Pair("لَا إِلٰهَ إِلَّا اللَّهُ", if (isBn) "লা ইলাহা ইল্লাল্লাহ (আল্লাহ ছাড়া উপাস্য নেই)" else "La ilaha illallah (None has the right to be worshipped but Allah)"),
            Pair("سُبْحَانَ اللَّهِ وَبِحَمْدِهِ", if (isBn) "সুবহানাল্লাহি ওয়া বিহামদিহী" else "Subhanallahi wa bihamdihi"),
            Pair("لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ", if (isBn) "লা হাওলা ওয়ালা কুওয়াতা ইল্লা বিল্লাহ" else "La hawla wa la quwwata illa billah"),
            Pair("حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ", if (isBn) "হাসবুনাল্লাহু ওয়া নি'মাল ওয়াকিল" else "Hasbunallahu wa ni'mal wakeel"),
            Pair("صَلَّى اللّٰهُ عَلَيْهِ وَسَلَّمَ", if (isBn) "দরুদ শরীফ (সাল্লাল্লাহু আলাইহি ওয়া সাল্লাম)" else "Darood Sharif (Peace be upon Him)")
        )
    }

    val activeZikir = zikirs.getOrElse(selectedZikirIndex) { zikirs[0] }

    // Interaction source for spring scale animation on tap button
    val tapInteractionSource = remember { MutableInteractionSource() }
    val isPressed by tapInteractionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tasbih_button_scale"
    )

    // Tap Handler
    val handleTap = {
        val newCount = count + 1
        count = newCount
        totalCount++

        // Check if cycle/round completed
        if (targetCount > 0 && newCount % targetCount == 0) {
            completedRounds++
        }

        // Sound Feedback
        if (isSoundEnabled) {
            triggerTasbihSound()
        }

        // Vibration Feedback based on user's exact vibration rules:
        // 1. If 99 -> 1 Long Vibrate
        // 2. If 33 or multiple of 33 -> 3 Short Vibrates
        // 3. Otherwise -> 1 Light Vibrate
        if (isVibrationEnabled) {
            when {
                newCount == 99 || (targetCount == 99 && newCount % 99 == 0) || (targetCount == 100 && newCount == 100) -> {
                    triggerTasbihVibration(context, TasbihVibrationType.MILESTONE_99)
                }
                newCount == 33 || newCount == 66 || (targetCount == 33 && newCount % 33 == 0) -> {
                    triggerTasbihVibration(context, TasbihVibrationType.MILESTONE_33)
                }
                else -> {
                    triggerTasbihVibration(context, TasbihVibrationType.TAP)
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Title + Sound & Vibration Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isBn) "ডিজিটাল জিকির ও তাসবিহ" else "Digital Tasbih Counter",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    Text(
                        text = if (isBn) "স্মার্ট হ্যাপটিক ভাইব্রেশন সহ" else "With Smart Haptic Vibration",
                        fontSize = 11.5.sp,
                        color = themeColors.displayText.copy(alpha = 0.6f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Vibration Toggle
                    FilledTonalIconButton(
                        onClick = {
                            isVibrationEnabled = !isVibrationEnabled
                            if (isVibrationEnabled) {
                                triggerTasbihVibration(context, TasbihVibrationType.TAP)
                            }
                        },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (isVibrationEnabled) themeColors.buttonEqualBg.copy(alpha = 0.18f) else themeColors.displayText.copy(alpha = 0.08f),
                            contentColor = if (isVibrationEnabled) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = if (isVibrationEnabled) Icons.Default.Vibration else Icons.Default.MobileOff,
                            contentDescription = "Toggle Vibration",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Sound Toggle
                    FilledTonalIconButton(
                        onClick = { isSoundEnabled = !isSoundEnabled },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (isSoundEnabled) themeColors.buttonEqualBg.copy(alpha = 0.18f) else themeColors.displayText.copy(alpha = 0.08f),
                            contentColor = if (isSoundEnabled) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Toggle Sound",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active Zikir Display Card with previous & next navigation
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.background)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                selectedZikirIndex = if (selectedZikirIndex > 0) selectedZikirIndex - 1 else zikirs.lastIndex
                                count = 0
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Previous Zikir",
                                tint = themeColors.displayText.copy(alpha = 0.6f)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = themeColors.buttonEqualBg.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${selectedZikirIndex + 1}/${zikirs.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.buttonEqualBg,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                selectedZikirIndex = (selectedZikirIndex + 1) % zikirs.size
                                count = 0
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Next Zikir",
                                tint = themeColors.displayText.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = activeZikir.first,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.buttonEqualBg,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = activeZikir.second,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = themeColors.displayText.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scrollable Zikir Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                zikirs.forEachIndexed { idx, item ->
                    val isSelected = selectedZikirIndex == idx
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.07f),
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(0.5.dp, themeColors.displayText.copy(alpha = 0.12f)),
                        modifier = Modifier.clickable {
                            selectedZikirIndex = idx
                            count = 0
                        }
                    ) {
                        Text(
                            text = zikirShortNames.getOrElse(idx) { item.second.split(" ")[0].replace("(", "") },
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else themeColors.displayText,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Target Selector (33, 99, 100, 1000, Custom)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isBn) "লক্ষ্য (টার্গেট):" else "Target:",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.displayText.copy(alpha = 0.8f),
                    maxLines = 1,
                    softWrap = false
                )

                listOf(33, 99, 100, 1000).forEach { limit ->
                    val isSelected = targetCount == limit
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.07f))
                            .clickable { targetCount = limit }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$limit",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else themeColors.displayText,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Custom Target Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (targetCount !in listOf(33, 99, 100, 1000)) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.07f))
                        .clickable { showCustomTargetDialog = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (targetCount !in listOf(33, 99, 100, 1000)) "$targetCount" else (if (isBn) "কাস্টম" else "Custom"),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (targetCount !in listOf(33, 99, 100, 1000)) Color.White else themeColors.displayText,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Interactive Tap Area with Animated Scale, Progress Ring and Count Display
            Box(
                modifier = Modifier
                    .size(190.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Circular Track
                val progress = remember(count, targetCount) {
                    if (targetCount <= 0) 0f else (count % targetCount).toFloat() / targetCount.toFloat()
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeW = 6.dp.toPx()
                    // Track
                    drawCircle(
                        color = themeColors.displayText.copy(alpha = 0.08f),
                        radius = (size.minDimension - strokeW) / 2f,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
                    )
                    // Active Progress Arc
                    if (progress > 0f) {
                        drawArc(
                            color = themeColors.buttonEqualBg,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = strokeW,
                                cap = StrokeCap.Round
                            )
                        )
                    }
                }

                // Interactive Tap Button with Press Scale & Ripple
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(buttonScale)
                        .clip(CircleShape)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(
                                    themeColors.buttonEqualBg,
                                    themeColors.buttonEqualBg.copy(alpha = 0.85f)
                                )
                            )
                        )
                        .clickable(
                            interactionSource = tapInteractionSource,
                            indication = ripple(bounded = true, color = Color.White),
                            onClick = handleTap
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$count",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.22f)
                        ) {
                            Text(
                                text = if (isBn) "ট্যাপ করুন" else "TAP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Laps / Rounds and Target Progress Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Round / Cycle Counter
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = themeColors.buttonEqualBg.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Loop,
                            contentDescription = "Rounds",
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBn) "চক্র / রাউন্ড: $completedRounds" else "Rounds: $completedRounds",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.buttonEqualBg
                        )
                    }
                }

                // Current Target Progress Indicator
                Text(
                    text = if (isBn) "অগ্রগতি: $count/$targetCount" else "Progress: $count/$targetCount",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.displayText.copy(alpha = 0.75f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Milestone Banner Alert with celebratory visuals
            if (count > 0 && count % targetCount == 0) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🎉", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "মাশাআল্লাহ! $targetCount বার পাঠ সম্পন্ন হয়েছে!" else "MashaAllah! Target of $targetCount reached!",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            fontSize = 12.5.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Vibration Mode Notice (33 vs 99 Indicator)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.displayBackground),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = null,
                        tint = themeColors.buttonEqualBg,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBn)
                            "প্রতি ট্যাপে হালকা ভাইব্রেশন • ৩৩-এ ৩ বার • ৯৯-এ লং ভাইব্রেশন"
                        else
                            "Light tap vibe • 3 vibes on 33 • Long vibe on 99",
                        fontSize = 10.5.sp,
                        color = themeColors.displayText.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Action Controls: Undo (-1), Total Count, and Reset Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Undo / Decrement (-1)
                OutlinedButton(
                    onClick = {
                        if (count > 0) {
                            count--
                            if (totalCount > 0) totalCount--
                            if (isVibrationEnabled) triggerTasbihVibration(context, TasbihVibrationType.TAP)
                        }
                    },
                    enabled = count > 0,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Undo, contentDescription = "Undo", modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "-1", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Total Session Count
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isBn) "মোট পাঠ" else "Total Count",
                        fontSize = 11.sp,
                        color = themeColors.displayText.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "$totalCount",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                }

                // Reset Button
                Button(
                    onClick = { showResetDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.displayText.copy(alpha = 0.12f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = themeColors.displayText,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBn) "রিসেট" else "Reset",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                }
            }
        }
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = themeColors.buttonEqualBg
                )
            },
            title = {
                Text(
                    text = if (isBn) "তাসবিহ কাউন্টার রিসেট" else "Reset Tasbih Counter",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = themeColors.displayText
                )
            },
            text = {
                Text(
                    text = if (isBn)
                        "আপনি কি বর্তমান জিকির কাউন্ট রিসেট করতে চান নাকি সম্পূর্ণ সেশন (মোট পাঠ ও চক্র সহ) রিসেট করবেন?"
                    else
                        "Do you want to reset the current count or clear the entire session?",
                    fontSize = 13.sp,
                    color = themeColors.displayText.copy(alpha = 0.85f)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        count = 0
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = if (isBn) "কাউন্টার ০ করুন" else "Reset Count", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = {
                            count = 0
                            totalCount = 0
                            completedRounds = 0
                            showResetDialog = false
                        }
                    ) {
                        Text(
                            text = if (isBn) "সব রিসেট" else "Reset All",
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TextButton(onClick = { showResetDialog = false }) {
                        Text(text = if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText.copy(alpha = 0.7f))
                    }
                }
            },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Custom Target Dialog
    if (showCustomTargetDialog) {
        AlertDialog(
            onDismissRequest = { showCustomTargetDialog = false },
            title = {
                Text(
                    text = if (isBn) "কাস্টম টার্গেট সংখ্যা" else "Custom Target Count",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = themeColors.displayText
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isBn) "আপনার কাঙ্ক্ষিত জিকির লক্ষ্য সংখ্যা লিখুন:" else "Enter your desired target count:",
                        fontSize = 13.sp,
                        color = themeColors.displayText.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customTargetInput,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 5) {
                                customTargetInput = input
                            }
                        },
                        singleLine = true,
                        placeholder = { Text("Ex: 70, 313, 500") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.buttonEqualBg,
                            cursorColor = themeColors.buttonEqualBg
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = customTargetInput.toIntOrNull()
                        if (num != null && num > 0) {
                            targetCount = num
                        }
                        showCustomTargetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = if (isBn) "সেট করুন" else "Set Target", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTargetDialog = false }) {
                    Text(text = if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText.copy(alpha = 0.7f))
                }
            },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

data class BdDistrict(
    val nameBn: String,
    val nameEn: String,
    val offsetMinutes: Int
)

val bdDistrictsList = listOf(
    BdDistrict("ঢাকা (Dhaka)", "Dhaka", 0),
    BdDistrict("গাজীপুর (Gazipur)", "Gazipur", 0),
    BdDistrict("নারায়ণগঞ্জ (Narayanganj)", "Narayanganj", 0),
    BdDistrict("চট্টগ্রাম (Chittagong)", "Chittagong", -5),
    BdDistrict("কক্সবাজার (Cox's Bazar)", "Cox's Bazar", -7),
    BdDistrict("কুমিল্লা (Comilla)", "Comilla", -3),
    BdDistrict("সিলেট (Sylhet)", "Sylhet", -6),
    BdDistrict("মৌলভীবাজার (Moulvibazar)", "Moulvibazar", -7),
    BdDistrict("হবিগঞ্জ (Habiganj)", "Habiganj", -5),
    BdDistrict("সুনামগঞ্জ (Sunamganj)", "Sunamganj", -5),
    BdDistrict("ব্রাহ্মণবাড়িয়া (Brahmanbaria)", "Brahmanbaria", -3),
    BdDistrict("নোয়াখালী (Noakhali)", "Noakhali", -3),
    BdDistrict("ফেনী (Feni)", "Feni", -4),
    BdDistrict("চাঁদপুর (Chandpur)", "Chandpur", -2),
    BdDistrict("লক্ষ্মীপুর (Lakshmipur)", "Lakshmipur", -2),
    BdDistrict("খাগড়াছড়ি (Khagrachhari)", "Khagrachhari", -6),
    BdDistrict("রাঙ্গামাটি (Rangamati)", "Rangamati", -6),
    BdDistrict("বান্দরবান (Bandarban)", "Bandarban", -6),
    BdDistrict("রাজশাহী (Rajshahi)", "Rajshahi", 7),
    BdDistrict("বগুড়া (Bogra)", "Bogra", 5),
    BdDistrict("পাবনা (Pabna)", "Pabna", 6),
    BdDistrict("সিরাজগঞ্জ (Sirajganj)", "Sirajganj", 3),
    BdDistrict("নওগাঁ (Naogaon)", "Naogaon", 8),
    BdDistrict("নাটোর (Natore)", "Natore", 7),
    BdDistrict("চাঁপাইনবাবগঞ্জ (Chapainawabganj)", "Chapainawabganj", 9),
    BdDistrict("জয়পুরহাট (Joypurhat)", "Joypurhat", 6),
    BdDistrict("রংপুর (Rangpur)", "Rangpur", 8),
    BdDistrict("দিনাজপুর (Dinajpur)", "Dinajpur", 10),
    BdDistrict("গাইবান্ধা (Gaibandha)", "Gaibandha", 6),
    BdDistrict("কুড়িগ্রাম (Kurigram)", "Kurigram", 7),
    BdDistrict("লালমনিরহাট (Lalmonirhat)", "Lalmonirhat", 8),
    BdDistrict("নীলফামারী (Nilphamari)", "Nilphamari", 9),
    BdDistrict("পঞ্চগড় (Panchagarh)", "Panchagarh", 12),
    BdDistrict("ঠাকুরগাঁও (Thakurgaon)", "Thakurgaon", 11),
    BdDistrict("খুলনা (Khulna)", "Khulna", 5),
    BdDistrict("যশোর (Jessore)", "Jessore", 6),
    BdDistrict("কুষ্টিয়া (Kushtia)", "Kushtia", 6),
    BdDistrict("সাতক্ষীরা (Satkhira)", "Satkhira", 7),
    BdDistrict("বাগেরহাট (Bagerhat)", "Bagerhat", 4),
    BdDistrict("ঝিনাইদহ (Jhenaidah)", "Jhenaidah", 6),
    BdDistrict("চুয়াডাঙ্গা (Chuadanga)", "Chuadanga", 7),
    BdDistrict("মেহেরপুর (Meherpur)", "Meherpur", 7),
    BdDistrict("মাগুরা (Magura)", "Magura", 5),
    BdDistrict("নড়াইল (Narail)", "Narail", 5),
    BdDistrict("বরিশাল (Barisal)", "Barisal", 2),
    BdDistrict("পটুয়াখালী (Patuakhali)", "Patuakhali", 2),
    BdDistrict("ভোলা (Bhola)", "Bhola", 0),
    BdDistrict("পিরোজপুর (Pirojpur)", "Pirojpur", 3),
    BdDistrict("বরগুনা (Barguna)", "Barguna", 3),
    BdDistrict("ঝালকাঠি (Jhalokati)", "Jhalokati", 2),
    BdDistrict("ময়মনসিংহ (Mymensingh)", "Mymensingh", -1),
    BdDistrict("জামালপুর (Jamalpur)", "Jamalpur", 3),
    BdDistrict("শেরপুর (Sherpur)", "Sherpur", 2),
    BdDistrict("নেত্রকোনা (Netrokona)", "Netrokona", -2),
    BdDistrict("ফরিদপুর (Faridpur)", "Faridpur", 2),
    BdDistrict("গোপালগঞ্জ (Gopalganj)", "Gopalganj", 3),
    BdDistrict("মাদারীপুর (Madaripur)", "Madaripur", 2),
    BdDistrict("শরীয়তপুর (Shariatpur)", "Shariatpur", 1),
    BdDistrict("রাজবাড়ী (Rajbari)", "Rajbari", 4),
    BdDistrict("মানিকগঞ্জ (Manikganj)", "Manikganj", 2),
    BdDistrict("মুন্সীগঞ্জ (Munshiganj)", "Munshiganj", 0),
    BdDistrict("টাঙ্গাইল (Tangail)", "Tangail", 2),
    BdDistrict("নরসিংদী (Narsingdi)", "Narsingdi", -1),
    BdDistrict("কিশোরগঞ্জ (Kishoreganj)", "Kishoreganj", -2)
)

fun adjustIslamicTime(timeStr: String, offsetMinutes: Int): String {
    if (offsetMinutes == 0) return timeStr
    return try {
        val sdf = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        val date = sdf.parse(timeStr) ?: return timeStr
        val cal = Calendar.getInstance().apply {
            time = date
            add(Calendar.MINUTE, offsetMinutes)
        }
        sdf.format(cal.time)
    } catch (e: Exception) {
        timeStr
    }
}

@Composable
fun DistrictSelectorDropdown(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    var expanded by remember { mutableStateOf(false) }
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

    Box {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.15f)),
            modifier = Modifier.clickable { expanded = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "District",
                    tint = themeColors.buttonEqualBg,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isBn) viewModel.selectedIslamicDistrictBn.split(" ")[0] else viewModel.selectedIslamicDistrictEn,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.buttonEqualBg
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = themeColors.buttonEqualBg,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .heightIn(max = 280.dp)
                .background(themeColors.cardBg)
        ) {
            bdDistrictsList.forEach { district ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (isBn) district.nameBn else district.nameEn,
                            fontSize = 13.sp,
                            fontWeight = if (viewModel.selectedIslamicDistrictEn == district.nameEn) FontWeight.Bold else FontWeight.Normal,
                            color = if (viewModel.selectedIslamicDistrictEn == district.nameEn) themeColors.buttonEqualBg else themeColors.displayText
                        )
                    },
                    onClick = {
                        val fullDistrict = com.example.ui.islamic.allBdDistrictsList.find { it.nameEn.equals(district.nameEn, ignoreCase = true) }
                        val lat = fullDistrict?.lat ?: 23.8103
                        val lon = fullDistrict?.lon ?: 90.4125
                        viewModel.updateIslamicDistrict(
                            nameBn = district.nameBn,
                            nameEn = district.nameEn,
                            lat = lat,
                            lon = lon,
                            offsetMinutes = district.offsetMinutes,
                            isAuto = false
                        )
                        expanded = false
                    }
                )
            }
        }
    }
}

// --- Helper Functions for Time Parsing & Live Countdown ---

fun parseIslamicTimeToCalendar(timeStr: String, offsetMinutes: Int, dayOffset: Int = 0): Calendar {
    val cal = Calendar.getInstance()
    try {
        val sdf = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        val date = sdf.parse(timeStr)
        if (date != null) {
            val timeCal = Calendar.getInstance().apply { time = date }
            cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
            cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.add(Calendar.MINUTE, offsetMinutes)
            cal.add(Calendar.DAY_OF_YEAR, dayOffset)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return cal
}

fun formatIslamicCountdown(diffMillis: Long, isBn: Boolean): String {
    if (diffMillis <= 0) return if (isBn) "০০ঘণ্টা ০০মিনিট ০০সেকেন্ড" else "00h 00m 00s"
    val totalSeconds = diffMillis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val hStr = String.format(Locale.ENGLISH, "%02d", hours)
    val mStr = String.format(Locale.ENGLISH, "%02d", minutes)
    val sStr = String.format(Locale.ENGLISH, "%02d", seconds)

    return if (isBn) {
        val bnDigits = mapOf('0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪', '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯')
        val hBn = hStr.map { bnDigits[it] ?: it }.joinToString("")
        val mBn = mStr.map { bnDigits[it] ?: it }.joinToString("")
        val sBn = sStr.map { bnDigits[it] ?: it }.joinToString("")
        "${hBn}ঘণ্টা ${mBn}মিনিট ${sBn}সেকেন্ড"
    } else {
        "${hStr}h ${mStr}m ${sStr}s"
    }
}

// --- 3. PRAYER TIMES CARD ---
@Composable
fun PrayerTimesCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    ModernPrayerTimesCard(viewModel, themeColors)
}

private data class ScheduleItem(
    val nameBn: String,
    val nameEn: String,
    val timeStr: String,
    val isForbidden: Boolean,
    val noteBn: String?,
    val noteEn: String? = null
)

private data class Octuple<A, B, C, D, E, F, G, H>(
    val first: A, val second: B, val third: C,
    val fourth: D, val fifth: E, val sixth: F,
    val seventh: G, val eighth: H
)

// --- 4. SEHRI & IFTAR CARD ---
@Composable
fun SehriIftarCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    ModernSehriIftarCard(viewModel, themeColors)
}

private data class Hexuple<A, B, C, D, E, F>(
    val first: A, val second: B, val third: C,
    val fourth: D, val fifth: E, val sixth: F
)

// --- 5. ISLAMIC DUAS & VIRTUOUS AMAL (সমৃদ্ধ দোয়া ও আমল) ---

enum class DuaCategory(val titleBn: String, val titleEn: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    ALL("সকল দোয়া ও আমল", "All Duas & Amal", Icons.Default.AllInclusive),
    DAILY("দৈনন্দিন মাসনুন দোয়া", "Daily Masnoon", Icons.Default.WbSunny),
    MORNING_EVENING("সকাল-সন্ধ্যার জিকির", "Morning & Evening", Icons.Default.NightsStay),
    DISTRESS_DEBT("বিপদ ও ঋণমুক্তি", "Distress & Debt", Icons.Default.Healing),
    PRAYER_SALAH("নামাজ ও সালাতের দোয়া", "Salah & Prayer", Icons.Default.Mosque),
    RABBANA_QURAN("কুরআনি মোনাজাত", "Quranic Duas", Icons.Default.MenuBook),
    AMAL_FAZILAT("গুরুত্বপূর্ণ আমল ও ফজিলত", "Virtuous Amal", Icons.Default.AutoAwesome)
}

data class IslamicDuaItem(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val category: DuaCategory,
    val arabic: String,
    val pronunciationBn: String,
    val pronunciationEn: String,
    val meaningBn: String,
    val meaningEn: String,
    val virtuesBn: String,
    val virtuesEn: String,
    val reference: String,
    val repetitionCount: Int = 1,
    val timeOrOccasionBn: String = "",
    val timeOrOccasionEn: String = ""
)

@Composable
fun IslamicDuasCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    namazViewModel: NamazViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

    val playingDuaId by namazViewModel.playingDuaId.collectAsStateWithLifecycle()
    val isPlaying by namazViewModel.isPlaying.collectAsStateWithLifecycle()
    val downloadedDuaIds by namazViewModel.downloadedDuaIds.collectAsStateWithLifecycle()
    val downloadProgress by namazViewModel.downloadProgress.collectAsStateWithLifecycle()

    var selectedCategory by remember { mutableStateOf(DuaCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedDuaId by remember { mutableStateOf<String?>(null) }
    val counters = remember { mutableStateMapOf<String, Int>() }

    val allDuas = remember {
        listOf(
            // 1. DAILY
            IslamicDuaItem(
                id = "wake_up",
                titleBn = "ঘুম থেকে ওঠার দোয়া",
                titleEn = "Dua After Waking Up",
                category = DuaCategory.DAILY,
                arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
                pronunciationBn = "আলহামদু লিল্লাহিল্লাজি আহইয়ানা বা'দা মা আমাতানা ওয়া ইলাইহিন নুশূর।",
                pronunciationEn = "Alhamdu lillahillazi ahyana ba'da ma amatana wa ilaihin nushur.",
                meaningBn = "সকল প্রশংসা আল্লাহর জন্য, যিনি আমাদের মৃত্যুর (ঘুমের) পর পুনরায় জীবিত করলেন এবং তাঁরই দিকে সবার পুনরুত্থান।",
                meaningEn = "All praise is for Allah who gave us life after having taken it from us and unto Him is the resurrection.",
                virtuesBn = "ঘুম ভাঙার সাথে সাথে এই দোয়া পাঠ করা রাসূলুল্লাহ (সা.)-এর সুন্নাত। এর মাধ্যমে প্রতিটি দিনের শুরু হয় আল্লাহর কৃতজ্ঞতা প্রকাশের মধ্য দিয়ে।",
                virtuesEn = "Sunnah of the Prophet (pbuh) upon waking up to begin the day with gratitude to Allah.",
                reference = "সহীহ বুখারী: ৬৩১২, সহীহ মুসলিম: ২৭১১",
                timeOrOccasionBn = "ঘুম থেকে জাগ্রত হয়ে",
                timeOrOccasionEn = "Immediately after waking up"
            ),
            IslamicDuaItem(
                id = "before_sleep",
                titleBn = "ঘুমানোর পূর্বের দোয়া",
                titleEn = "Dua Before Sleeping",
                category = DuaCategory.DAILY,
                arabic = "اللَّهُمَّ بِاسْمِكَ أَمُوتُ وَأَحْيَا",
                pronunciationBn = "আল্লাহুম্মা বিসমিকা আমূতু ওয়া আহ্ইয়া।",
                pronunciationEn = "Allahumma bismika amutu wa ahya.",
                meaningBn = "হে আল্লাহ! আপনার নাম নিয়ে আমি মৃত্যুবরণ (ঘুম) করছি এবং আপনার নামেই জীবিত (জাগ্রত) হবো।",
                meaningEn = "O Allah, in Your name I die and I live.",
                virtuesBn = "ঘুমানোর পূর্বে ডান কাতে শুয়ে এই দোয়া পাঠ করা সুন্নাত। ঘুমে আল্লাহর নিরাপত্তা লাভ হয়।",
                virtuesEn = "Sunnah to recite while lying on the right side before going to sleep.",
                reference = "সহীহ বুখারী: ৬৩২৪, সহীহ মুসলিম: ২৭১১",
                timeOrOccasionBn = "ঘুমানোর প্রস্তুতি নেওয়ার সময়",
                timeOrOccasionEn = "Before falling asleep"
            ),
            IslamicDuaItem(
                id = "leave_home",
                titleBn = "ঘর থেকে বের হওয়ার দোয়া",
                titleEn = "Dua When Leaving Home",
                category = DuaCategory.DAILY,
                arabic = "بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
                pronunciationBn = "বিসমিল্লাহি তাওয়াক্কালতু 'আলাল্লাহ, ওয়া লা হাওলা ওয়া লা ক্বুওয়াতা ইল্লা বিল্লাহ।",
                pronunciationEn = "Bismillahi tawakkaltu 'alallah, wa la hawla wa la quwwata illa billah.",
                meaningBn = "আল্লাহর নামে বের হচ্ছি, আল্লাহর উপরই ভরসা করলাম। আল্লাহর সাহায্য ছাড়া গুনাহ থেকে বাঁচার এবং নেক কাজ করার কোনো শক্তি নেই।",
                meaningEn = "In the name of Allah, I trust in Allah; there is no might and no power except by Allah.",
                virtuesBn = "এই দোয়া পড়লে ফেরেশতারা ঘোষণা দেন: তুমি হেদায়েত প্রাপ্ত হয়েছ, তোমার জন্য যথেষ্ট করা হয়েছে এবং তুমি নিরাপদ হয়েছ। শয়তান তার থেকে দূরে সরে যায়।",
                virtuesEn = "Angels announce guidance, sufficiency, and protection. Shaytan withdraws from the person.",
                reference = "সুনানে আবু দাউদ: ৫০৯৫, তিরমিজি: ৩৪২৬ (হাসান সহীহ)",
                timeOrOccasionBn = "ঘরের বাইরে পা বাড়ানোর সময়",
                timeOrOccasionEn = "Stepping out of home"
            ),
            IslamicDuaItem(
                id = "enter_home",
                titleBn = "ঘরে প্রবেশের দোয়া ও সালাম",
                titleEn = "Dua When Entering Home",
                category = DuaCategory.DAILY,
                arabic = "بِسْمِ اللَّهِ وَلَجْنَا، وَبِسْمِ اللَّهِ خَرَجْنَا، وَعَلَى اللَّهِ رَبِّنَا تَوَكَّلْنَا",
                pronunciationBn = "বিসমিল্লাহি ওয়ালাজনা, ওয়া বিসমিল্লাহি খরাজনা, ওয়া 'আলাল্লাহি রব্বিনা তাওয়াক্কালনা।",
                pronunciationEn = "Bismillahi walajna, wa bismillahi kharajna, wa 'ala Allahi rabbina tawakkalna.",
                meaningBn = "আল্লাহর নামে আমরা প্রবেশ করলাম, আল্লাহর নামেই আমরা বের হয়েছিলাম এবং আমাদের প্রতিপালক আল্লাহর উপরই আমরা ভরসা করলাম।",
                meaningEn = "In the name of Allah we enter, in the name of Allah we leave, and upon our Lord we rely.",
                virtuesBn = "দোয়া পাঠ করে পরিবারের লোকদের সালাম দেওয়া সুন্নাত। ঘরে প্রবেশের সময় আল্লাহর নাম স্মরণ করলে শয়তান সেই ঘরে রাত কাটানোর সুযোগ পায় না।",
                virtuesEn = "Entering with Allah's name prevents Shaytan from dwelling in the home.",
                reference = "সুনানে আবু দাউদ: ৫০৯৬, সহীহ মুসলিম: ২০১৭",
                timeOrOccasionBn = "ঘরে প্রবেশ করার মুহূর্তে",
                timeOrOccasionEn = "Upon entering home"
            ),
            IslamicDuaItem(
                id = "enter_mosque",
                titleBn = "মসজিদে প্রবেশের দোয়া",
                titleEn = "Dua When Entering Mosque",
                category = DuaCategory.DAILY,
                arabic = "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
                pronunciationBn = "আল্লাহুম্মাফতাহ্ লী আবওয়াবা রহমাতিক।",
                pronunciationEn = "Allahummaftah li abwaba rahmatik.",
                meaningBn = "হে আল্লাহ! আমার জন্য আপনার রহমতের দরজাসমূহ উন্মুক্ত করে দিন।",
                meaningEn = "O Allah, open the gates of Your mercy for me.",
                virtuesBn = "ডান পা দিয়ে মসজিদে প্রবেশ করা এবং দুরুদ পাঠসহ এই দোয়াটি পড়া অত্যন্ত বরকতপূর্ণ সুন্নাত।",
                virtuesEn = "Sunnah to enter with right foot and ask Allah for His boundless mercy.",
                reference = "সহীহ মুসলিম: ৭১৩, সুনানে আবু দাউদ: ৪৬৫",
                timeOrOccasionBn = "মসজিদের প্রবেশদ্বারে",
                timeOrOccasionEn = "At mosque entrance"
            ),
            IslamicDuaItem(
                id = "exit_mosque",
                titleBn = "মসজিদ থেকে বের হওয়ার দোয়া",
                titleEn = "Dua When Leaving Mosque",
                category = DuaCategory.DAILY,
                arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ",
                pronunciationBn = "আল্লাহুম্মা ইন্নী আস'আলুকা মিন ফাদ্বলিক।",
                pronunciationEn = "Allahumma inni as'aluka min fadlik.",
                meaningBn = "হে আল্লাহ! আমি আপনার নিকট আপনার অনুগ্রহ ও বরকত প্রার্থনা করছি।",
                meaningEn = "O Allah, I ask You from Your favor and bounty.",
                virtuesBn = "বাম পা দিয়ে মসজিদ থেকে বের হতে হয় এবং আল্লাহর নিকট হালাল রিযিক ও কল্যাণ কামনা করতে হয়।",
                virtuesEn = "Sunnah to step out with left foot seeking Allah's divine grace and livelihood.",
                reference = "সহীহ মুসলিম: ৭১৩, সুনানে ইবনে মাজাহ: ৭৭২",
                timeOrOccasionBn = "মসজিদ হতে বের হওয়ার সময়",
                timeOrOccasionEn = "When exiting mosque"
            ),
            IslamicDuaItem(
                id = "before_eat",
                titleBn = "খাবার শুরুর দোয়া",
                titleEn = "Dua Before Eating",
                category = DuaCategory.DAILY,
                arabic = "بِسْمِ اللَّهِ وَعَلَى بَرَكَةِ اللَّهِ",
                pronunciationBn = "বিসমিল্লাহি ওয়া 'আলা বারাকাতিল্লাহ।",
                pronunciationEn = "Bismillahi wa 'ala barakatillah.",
                meaningBn = "আল্লাহর নামে এবং আল্লাহর বরকতের উপর খাবার শুরু করছি।",
                meaningEn = "In the name of Allah and upon the blessing of Allah.",
                virtuesBn = "শুরুতে বিসমিল্লাহ বলতে ভুলে গেলে মনে পড়ার পর বলবে: 'বিসমিল্লাহি আউওয়ালাহু ওয়া আখিরাহু' (শুরু ও শেষে আল্লাহর নামে)।",
                virtuesEn = "If forgotten initially, recite: 'Bismillahi awwalahu wa akhirahu'.",
                reference = "সুনানে আবু দাউদ: ৩৭৬৭, তিরমিজি: ১৮৫৮",
                timeOrOccasionBn = "খাবারের শুরুতে",
                timeOrOccasionEn = "Before the first bite"
            ),
            IslamicDuaItem(
                id = "after_eat",
                titleBn = "খাবার শেষের দোয়া",
                titleEn = "Dua After Eating",
                category = DuaCategory.DAILY,
                arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنَا وَسَقَانَا وَجَعَلَنَا مُسْلِمِينَ",
                pronunciationBn = "আলহামদু লিল্লাহিল্লাজি আত'আমানা ওয়া সাক্বানা ওয়া জা'আলানা মুসলিমীন।",
                pronunciationEn = "Alhamdu lillahillazi at'amana wa saqana wa ja'alana muslimeen.",
                meaningBn = "সকল প্রশংসা আল্লাহর জন্য, যিনি আমাদের আহার করালেন, পান করালেন এবং আমাদের মুসলিম বানালেন।",
                meaningEn = "All praise is for Allah who fed us, gave us drink, and made us Muslims.",
                virtuesBn = "খাবার শেষে আল্লাহর প্রশংসা করলে আল্লাহ বান্দার উপর অত্যন্ত সন্তুষ্ট হন।",
                virtuesEn = "Praising Allah after nourishment earns immense pleasure of the Almighty.",
                reference = "সুনানে আবু দাউদ: ৩৮৫০, তিরমিজি: ৩৪৫৭",
                timeOrOccasionBn = "আহার শেষ করে",
                timeOrOccasionEn = "After finishing meal"
            ),
            IslamicDuaItem(
                id = "enter_toilet",
                titleBn = "টয়লেট বা বাথরুমে প্রবেশের দোয়া",
                titleEn = "Dua Entering Restroom",
                category = DuaCategory.DAILY,
                arabic = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْخُبُثِ وَالْخَبَائِثِ",
                pronunciationBn = "আল্লাহুম্মা ইন্নী আ'ঊযু বিকা মিনাল খুবুসি ওয়াল খাবা'ইস।",
                pronunciationEn = "Allahumma inni a'udhu bika minal khubuthi wal khaba'ith.",
                meaningBn = "হে আল্লাহ! আমি আপনার নিকট অপবিত্র নর ও নারী শয়তানের অনিষ্ট থেকে আশ্রয় চাই।",
                meaningEn = "O Allah, I seek refuge with You from all evil and malicious jinn (male & female).",
                virtuesBn = "বাম পা দিয়ে প্রবেশ করতে হয়। দোয়াটি পাঠ করলে মানুষের সতর ও শয়তানের দৃষ্টির মাঝে পর্দা হয়ে যায়।",
                virtuesEn = "Creates a protective barrier from harmful spirits.",
                reference = "সহীহ বুখারী: ১৪২, সহীহ মুসলিম: ৩৭৫",
                timeOrOccasionBn = "প্রবেশ করার পূর্বে",
                timeOrOccasionEn = "Before entering"
            ),
            IslamicDuaItem(
                id = "exit_toilet",
                titleBn = "বাথরুম থেকে বের হওয়ার দোয়া",
                titleEn = "Dua Leaving Restroom",
                category = DuaCategory.DAILY,
                arabic = "غُفْرَانَكَ ، الْحَمْدُ لِلَّهِ الَّذِي أَذْهَبَ عَنِّي الْأَذَى وَعَافَانِي",
                pronunciationBn = "গুফরা-নাক। আলহামদু লিল্লাহিল্লাজি আজহাবা 'আন্নিল আজা ওয়া 'আফানী।",
                pronunciationEn = "Ghufranaka. Alhamdu lillahillazi adh-haba 'annil adha wa 'afani.",
                meaningBn = "হে আল্লাহ! আমি আপনার ক্ষমা প্রার্থনা করছি। সকল প্রশংসা আল্লাহর জন্য যিনি আমার শরীর থেকে কষ্টদায়ক বস্তু দূর করে আমাকে সুস্থতা দান করেছেন।",
                meaningEn = "I seek Your forgiveness. Praise be to Allah who relieved me of harm and granted me well-being.",
                virtuesBn = "ডান পা দিয়ে বের হয়ে এই দোয়া পাঠ করা সুন্নাত।",
                virtuesEn = "Sunnah upon stepping out with the right foot.",
                reference = "সুনানে আবু দাউদ: ১৭, তিরমিজি: ৭, ইবনে মাজাহ: ৩০০",
                timeOrOccasionBn = "বের হওয়ার সময়",
                timeOrOccasionEn = "Upon exiting"
            ),
            IslamicDuaItem(
                id = "vehicle_ride",
                titleBn = "যানবাহনে ওঠার দোয়া",
                titleEn = "Dua for Travelling / Boarding Vehicle",
                category = DuaCategory.DAILY,
                arabic = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَٰذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ ، وَإِنَّا إِلَىٰ رَبِّنَا لَمُنْقَلِبُونَ",
                pronunciationBn = "সুবহানাল্লাজি সাখখারা লানা হাজা ওয়া মা কুন্না লাহু মুক্বরিনীন, ওয়া ইন্না ইলা রব্বিনা লামুনক্বলিবূন।",
                pronunciationEn = "Subhanallazi sakhkhara lana hadha wa ma kunna lahu muqrineen, wa inna ila rabbina lamunqalibun.",
                meaningBn = "পবিত্র ও মহান সেই সত্তা, যিনি এই বাহনকে আমাদের জন্য বশীভূত করে দিয়েছেন, অথচ আমরা একে আয়ত্তে আনতে সক্ষম ছিলাম না। নিশ্চয় আমরা আমাদের প্রতিপালকের নিকটই প্রত্যাবর্তনকারী।",
                meaningEn = "Glory to Him who has brought this into our control, though we were unable to subdue it by ourselves. And indeed, unto our Lord we will return.",
                virtuesBn = "গাড়ি, বাস, ট্রেন বা বিমানে ভ্রমণের সময় যাত্রা নিরাপদ রাখার জন্য মাসনুন দোয়া।",
                virtuesEn = "Essential travel prayer ensuring safety throughout journey.",
                reference = "সূরা যুখরুফ: ১৩-১৪, সহীহ মুসলিম: ১৩৪২",
                timeOrOccasionBn = "যেকোনো যানবাহনে ওঠার পর",
                timeOrOccasionEn = "After boarding transport"
            ),

            // 2. MORNING & EVENING
            IslamicDuaItem(
                id = "sayyidul_istighfar",
                titleBn = "সায়্যিদুল ইস্তিগফার (সর্বশ্রেষ্ঠ তাওবা)",
                titleEn = "Sayyidul Istighfar (Chief of Forgiveness)",
                category = DuaCategory.MORNING_EVENING,
                arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَٰهَ إِلَّا أَنْتَ ، خَلَقْتَنِي وَأَنَا عَبْدُكَ ، وَأَنَا عَلَىٰ عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
                pronunciationBn = "আল্লাহুম্মা আনতা রব্বী লা ইলাহা ইল্লা আনতা, খালাক্বতানী ওয়া আনা 'আবদুকা, ওয়া আনা 'আলা 'আহদিকা ওয়া ওয়া'দিকা মাসতাত্বা'তু, আ'ঊযু বিকা মিন শাররি মা সনা'তু, আবূ'উ লাকা বিনি'মাতিকা 'আলাইয়্যা, ওয়া আবূ'উ বিজাম্বী ফাগফির লী, ফা ইন্নাহু লা ইয়াগফিরুজ জুনূবা ইল্লা আনতা।",
                pronunciationEn = "Allahumma anta rabbi la ilaha illa anta, khalaqtani wa ana 'abduka, wa ana 'ala 'ahdika wa wa'dika mastata'tu, a'udhu bika min sharri ma sana'tu, abu'u laka bini'matika 'alayya, wa abu'u bidhambi faghfir li, fa-innahu la yaghfirudh-dhunuba illa anta.",
                meaningBn = "হে আল্লাহ! আপনি আমার প্রতিপালক। আপনি ছাড়া কোনো সত্য উপাস্য নেই। আপনি আমাকে সৃষ্টি করেছেন এবং আমি আপনার বান্দা। আমি আমার সাধ্যমতো আপনার অঙ্গীকার ও প্রতিশ্রুতির উপর কায়েম আছি। আমি আমার কৃতকর্মের অনিষ্ট থেকে আপনার আশ্রয় চাই। আমার উপর আপনার যে নেয়ামত রয়েছে তা স্বীকার করছি এবং আমার অপরাধও স্বীকার করছি। অতএব আমাকে ক্ষমা করে দিন; কারণ আপনি ছাড়া গুনাহ ক্ষমা করার আর কেউ নেই।",
                meaningEn = "O Allah, You are my Lord; none has the right to be worshipped but You. You created me and I am Your slave, and I abide by Your covenant and promise as best I can. I seek refuge in You from the evil of what I have done. I acknowledge Your favors upon me and I confess my sins to You, so forgive me, for none forgives sins but You.",
                virtuesBn = "রাসূলুল্লাহ (সা.) বলেছেন: যে ব্যক্তি দিনে বিশ্বাসের সাথে এটি পড়বে এবং সন্ধ্যা হওয়ার আগে মারা যাবে সে জান্নাতী হবে। আর যে রাতে পড়বে এবং সকাল হওয়ার আগে মারা যাবে সেও জান্নাতী হবে। (১ বার পাঠ)",
                virtuesEn = "Whoever recites this with firm faith in morning and dies before evening will enter Paradise, and vice versa.",
                reference = "সহীহ বুখারী: ৬৩০৬",
                repetitionCount = 1,
                timeOrOccasionBn = "সকাল ও সন্ধ্যায় একবার করে",
                timeOrOccasionEn = "Once in morning & evening"
            ),
            IslamicDuaItem(
                id = "bismillahillazi",
                titleBn = "সর্বপ্রকার ক্ষতি ও অনিষ্ট থেকে রক্ষার দোয়া",
                titleEn = "Protection from All Calamities (3 times)",
                category = DuaCategory.MORNING_EVENING,
                arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
                pronunciationBn = "বিসমিল্লাহিল্লাজি লা ইয়াদুররু মা'আসমিহি শাই'উন ফিল আরদি ওয়া লা ফিস সামা-ই, ওয়া হুওয়াস সামী'উল 'আলীম।",
                pronunciationEn = "Bismillahillazi la yadurru ma'asmihi shay'un fil-ardi wa la fis-sama'i, wa huwas-Sami'ul-'Aleem.",
                meaningBn = "আল্লাহর নামে শুরু করছি, যাঁর নামের বরকতে আসমান ও জমিনের কোনো কিছুই কোনো ক্ষতি করতে পারে না। আর তিনি সর্বশ্রোতা, সর্বজ্ঞ।",
                meaningEn = "In the name of Allah with whose Name nothing on earth or in heaven can cause harm, and He is the All-Hearing, the All-Knowing.",
                virtuesBn = "যে ব্যক্তি সকাল ও সন্ধ্যায় ৩ বার এই দোয়া পাঠ করবে, কোনো বিষাক্ত প্রাণী, মহামারী, আকস্মিক বিপদ বা অনিষ্ট তার ক্ষতি করতে পারবে না।",
                virtuesEn = "Reciting 3 times morning and evening guarantees protection against all harms, poisons and sudden disasters.",
                reference = "সুনানে তিরমিজি: ৩৩৮৮, আবু দাউদ: ৫০৮৮ (সহীহ)",
                repetitionCount = 3,
                timeOrOccasionBn = "সকালে ৩ বার, সন্ধ্যায় ৩ বার",
                timeOrOccasionEn = "3 times Morning & Evening"
            ),
            IslamicDuaItem(
                id = "raditu_billah",
                titleBn = "জান্নাত ওয়াজিব হওয়ার সকাল-সন্ধ্যার জিকির",
                titleEn = "Raditu Billahi Rabba (Pleased with Allah)",
                category = DuaCategory.MORNING_EVENING,
                arabic = "رَضِيتُ بِاللَّهِ رَبًّا ، وَبِالْإِسْلَامِ دِينًا ، وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا",
                pronunciationBn = "রাদীতু বিল্লাহি রব্বা, ওয়া বিল ইসলামী দীনা, ওয়া বিমুহাম্মাদিন (সল্লাল্লাহু 'আলাইহি ওয়া সাল্লাম) নাবিয়্যা।",
                pronunciationEn = "Raditu billahi rabba, wa bil-Islami deena, wa bi-Muhammadin (sallallahu alaihi wa sallam) nabiyya.",
                meaningBn = "আমি আল্লাহকে প্রতিপালক হিসেবে, ইসলামকে দ্বীন হিসেবে এবং মুহাম্মাদ (সা.)-কে নবী হিসেবে সানন্দে গ্রহণ করেছি।",
                meaningEn = "I am pleased with Allah as my Lord, with Islam as my religion, and with Muhammad (pbuh) as my Prophet.",
                virtuesBn = "যে ব্যক্তি সকালে ও সন্ধ্যায় ৩ বার এটি বলবে, কিয়ামতের দিন আল্লাহ তা'আলা তার উপর সন্তুষ্ট হয়ে তাকে জান্নাত দান করা নিজের দায়িত্ব করে নেবেন।",
                virtuesEn = "Allah promises to satisfy the reciter on the Day of Resurrection.",
                reference = "সুনানে আবু দাউদ: ৫০৭২, তিরমিজি: ৩৩৮৯",
                repetitionCount = 3,
                timeOrOccasionBn = "প্রতি সকাল ও সন্ধ্যায় ৩ বার",
                timeOrOccasionEn = "3 times daily"
            ),
            IslamicDuaItem(
                id = "ayatul_kursi",
                titleBn = "আয়াতুল কুরসী (কুরআনের শ্রেষ্ঠতম আয়াত)",
                titleEn = "Ayatul Kursi (Throne Verse)",
                category = DuaCategory.MORNING_EVENING,
                arabic = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
                pronunciationBn = "আল্লাহু লা ইলাহা ইল্লা হুওয়াল হাইয়্যুল ক্বাইয়্যূম। লা তা'খুজুহু সিনাতুঁও ওয়ালা নাউম। লাহু মা ফিস সামাওয়াতি ওয়ামা ফিল আরদ। মান জাল্লাজি ইয়াশফা'উ 'ইনদাহূ ইল্লা বি'ইজনিহ। ইয়া'লামু মা বাইনা আইদিহিম ওয়ামা খলফাহুম, ওয়ালা ইয়ুহিতূনা বিশাই'ইম মিন 'ইলমিহী ইল্লা বিমা শা-আ। ওয়াসি'আ কুরসিয়্যুহুস সামাওয়াতি ওয়াল আরদ, ওয়ালা ইয়া'ঊদুহু হিফজুহুমা, ওয়া হুওয়াল 'আলিয়্যুল 'আজীম।",
                pronunciationEn = "Allahu la ilaha illa huwal-Hayyul-Qayyum. La ta'khudhuhu sinatun wa la nawm. Lahu ma fis-samawati wa ma fil-ard. Man dhal-ladhi yashfa'u 'indahu illa bi-idhnih. Ya'lamu ma baina aydihim wa ma khalfahum, wa la yuhituna bishay'im-min 'ilmihi illa bima sha'a. Wasi'a kursiyyuhus-samawati wal-ard, wa la ya'uduhu hifdhuhuma, wa Huwal-'Aliyyul-'Adheem.",
                meaningBn = "আল্লাহ, তিনি ছাড়া কোনো সত্য ইলাহ নেই। তিনি চিরঞ্জীব, চিরস্থায়ী সবকিছুর ধারক। তন্দ্রা ও ঘুম তাঁকে স্পর্শ করে না। আকাশমণ্ডল ও পৃথিবীতে যা কিছু আছে সবই তাঁর। কে আছে এমন যে তাঁর অনুমতি ছাড়া তাঁর নিকট সুপারিশ করতে পারে? মানুষের সম্মুখে ও পেছনে যা কিছু আছে তা তিনি জানেন। তাঁর জ্ঞানের কিছুই তারা আয়ত্ত করতে পারে না, কেবল যতটুকু তিনি ইচ্ছে করেন। তাঁর কুরসী সমস্ত আকাশমণ্ডল ও পৃথিবীকে পরিবেষ্টন করে আছে। আর এ দুটির রক্ষণাবেক্ষণ তাঁকে বিন্দুমাত্র ক্লান্ত করে না। তিনি পরম উচ্চ, মহামহিম।",
                meaningEn = "Allah - there is no deity except Him, the Ever-Living, the Sustainer of all existence. Neither drowsiness overtakes Him nor sleep...",
                virtuesBn = "প্রতি ফরজ নামাজের পর পড়লে মৃত্যুর সাথে সাথে জান্নাত। সকাল-সন্ধ্যায় পড়লে সকল শয়তানি অনিষ্ট থেকে আল্লাহর সরাসরি সুরক্ষা মেলে। ঘুমানোর আগে পড়লে একজন ফেরেশতা সারারাত পাহারা দেয়।",
                virtuesEn = "Whoever recites it after each obligatory prayer, only death keeps them from entering Paradise.",
                reference = "সূরা বাক্বারা: ২৫৫, নাসায়ী: ৯৮৪৮, সহীহ বুখারী: ২৩১১",
                timeOrOccasionBn = "প্রতি ফরজ সালাতের পর, সকাল-সন্ধ্যা ও ঘুমানোর আগে",
                timeOrOccasionEn = "After every Salah & at bedtime"
            ),

            // 3. DISTRESS & DEBT
            IslamicDuaItem(
                id = "dua_yunus",
                titleBn = "দোয়ায়ে ইউনুস (বিপদ ও সংকট মুক্তির দোয়া)",
                titleEn = "Dua of Prophet Yunus (A.S.)",
                category = DuaCategory.DISTRESS_DEBT,
                arabic = "لَا إِلَٰهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ",
                pronunciationBn = "লা ইলাহা ইল্লা আনতা সুবহানাকা ইন্নী কুনতু মিনাজ জোয়ালিমীন।",
                pronunciationEn = "La ilaha illa anta subhanaka inni kuntu minaz-zalimeen.",
                meaningBn = "আপনি ছাড়া কোনো সত্য উপাস্য নেই, আপনি পরম পবিত্র ও নিষ্কলুষ। নিশ্চয়ই আমি অপরাধীদের অন্তর্ভুক্ত ছিলাম।",
                meaningEn = "There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers.",
                virtuesBn = "রাসূলুল্লাহ (সা.) বলেছেন: কোনো মুসলিম ব্যক্তি বিপদে পড়ে বা কোনো উদ্দেশ্য নিয়ে এই দোয়া করলে আল্লাহ তা'আলা অবশ্যই তার দোয়া কবুল করবেন এবং বিপদ মুক্ত করবেন।",
                virtuesEn = "Prophet (pbuh) said no Muslim supplication made with this is ever rejected by Allah.",
                reference = "সূরা আম্বিয়া: ৮৭, জামে তিরমিজি: ৩৫০৫ (সহীহ)",
                timeOrOccasionBn = "যেকোনো বিপদ, হতাশা বা কষ্টের সময়ে",
                timeOrOccasionEn = "During any distress or difficulty"
            ),
            IslamicDuaItem(
                id = "debt_anxiety",
                titleBn = "ঋণমুক্তি ও দুশ্চিন্তা দূর করার দোয়া",
                titleEn = "Dua for Relief from Debt & Anxiety",
                category = DuaCategory.DISTRESS_DEBT,
                arabic = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ ، وَأَعُوذُ بِكَ مِنَ الْعَجْزِ وَالْكَسَلِ ، وَأَعُوذُ بِكَ مِنَ الْجُبْنِ وَالْبُخْلِ ، وَأَعُوذُ بِكَ مِنْ غَلَبَةِ الدَّيْنِ وَقَهْرِ الرِّجَالِ",
                pronunciationBn = "আল্লাহুম্মা ইন্নী আ'ঊযু বিকা মিনাল হাম্মি ওয়াল হাযানি, ওয়া আ'ঊযু বিকা মিনাল 'আজযি ওয়াল কাসালি, ওয়া আ'ঊযু বিকা মিনাল জুবনি ওয়াল বুখলি, ওয়া আ'ঊযু বিকা মিন গলাবাতিদ দাইনি ওয়া ক্বাহরির রিজাল।",
                pronunciationEn = "Allahumma inni a'udhu bika minal-hammi wal-hazani, wa a'udhu bika minal-'ajzi wal-kasali, wa a'udhu bika minal-jubni wal-bukhli, wa a'udhu bika min ghalabatid-dayni wa qahrir-rijal.",
                meaningBn = "হে আল্লাহ! আমি আপনার আশ্রয় চাই দুশ্চিন্তা ও মনোকষ্ট থেকে, অক্ষমতা ও অলসতা থেকে, ভীরুতা ও কৃপণতা থেকে, এবং ঋণের প্রবল চাপ ও মানুষের অন্যায় দমন-পীড়ন থেকে।",
                meaningEn = "O Allah, I seek refuge in You from grief and sadness, from weakness and laziness, from cowardice and stinginess, and from the burden of debts and oppression of men.",
                virtuesBn = "আনাস (রা.) বলেন, রাসূলুল্লাহ (সা.) এই দোয়াটি অধিক পরিমাণে পাঠ করতেন। ঋণ ও মানসিক বিষণ্নতা দূর করতে এটি মহৌষধ।",
                virtuesEn = "Prophet (pbuh) used to recite this constantly for removing grief and financial burdens.",
                reference = "সহীহ বুখারী: ২৮৯৩, সুনানে আবু দাউদ: ১৫৫৫",
                timeOrOccasionBn = "সকাল-সন্ধ্যায় ও কষ্টের সময়",
                timeOrOccasionEn = "Morning, evening & when in debt"
            ),
            IslamicDuaItem(
                id = "illness_cure",
                titleBn = "রোগ ও শারীরিক কষ্ট মুক্তির দোয়া",
                titleEn = "Dua for Healing Sickness & Pain",
                category = DuaCategory.DISTRESS_DEBT,
                arabic = "اللَّهُمَّ رَبَّ النَّاسِ أَذْهِبِ الْبَأْسَ ، اشْفِ أَنْتَ الشَّافِي ، لَا شِفَاءَ إِلَّا شِفَاؤُكَ ، شِفَاءً لَا يُغَادِرُ سَقَمًا",
                pronunciationBn = "আল্লাহুম্মা রব্বান নাসি আজহিবিল বা'স, ইশফি আনতাশ শাফী, লা শিফা-আ ইল্লা শিফা-উকা, শিফা-আল লা ইয়ুগাদিরু সাক্বামা।",
                pronunciationEn = "Allahumma Rabban-nasi adh-hibil-ba's, ishfi antash-Shafi, la shifa'a illa shifa'uka, shifa'an la yughadiru saqama.",
                meaningBn = "হে মানুষের প্রতিপালক আল্লাহ! রোগ-কষ্ট দূর করে দিন এবং পূর্ণ নিরাময় দান করুন। আপনিই আরোগ্যকারী, আপনার শেফা ব্যতীত কোনো শেফা নেই; এমন আরোগ্য দিন যা কোনো রোগ অবশিষ্ট রাখে না।",
                meaningEn = "O Allah, Lord of mankind, remove the affliction and heal; You are the Healer, there is no healing but Yours, a cure that leaves behind no ailment.",
                virtuesBn = "অসুস্থ ব্যক্তির গায়ে ডান হাত রেখে বা নিজের অসুস্থতার সময় এই দোয়া পাঠ করা সুন্নাত।",
                virtuesEn = "Sunnah to place right hand on affected area and recite for complete recovery.",
                reference = "সহীহ বুখারী: ৫৭৪২, সহীহ মুসলিম: ২১৯১",
                timeOrOccasionBn = "রোগে আক্রান্ত হলে বা রোগী দেখতে গিয়ে",
                timeOrOccasionEn = "During sickness or visiting patients"
            ),
            IslamicDuaItem(
                id = "hasbunallah",
                titleBn = "চরম বিপদে আল্লাহর ওপর তাওয়াক্কুলের দোয়া",
                titleEn = "Hasbunallah wa Ni'mal Wakeel",
                category = DuaCategory.DISTRESS_DEBT,
                arabic = "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ ، نِعْمَ الْمَوْلَىٰ وَنِعْمَ النَّصِيرُ",
                pronunciationBn = "হাসবুনাল্লাহু ওয়া নি'মাল ওয়াকীল, নি'মাল মাওলা ওয়া নি'মান নাসীর।",
                pronunciationEn = "Hasbunallahu wa ni'mal wakeel, ni'mal mawla wa ni'man naseer.",
                meaningBn = "আল্লাহই আমাদের জন্য যথেষ্ট এবং তিনি কতই না উত্তম কর্মবিধায়ক, কতই না উত্তম অভিভাবক ও সাহায্যকারী!",
                meaningEn = "Allah is sufficient for us, and He is the best Disposer of affairs, the best Protector and Helper.",
                virtuesBn = "ইব্রাহিম (আ.)-কে যখন আগুনে নিক্ষেপ করা হয়েছিল তিনি এই বাক্য বলেছিলেন। কঠিন পরিস্থিতি ও শত্রুর ভয় থেকে মুক্তি পেতে এটি অতুলনীয়।",
                virtuesEn = "Recited by Prophet Ibrahim (as) when thrown into fire and by Prophet Muhammad (pbuh).",
                reference = "সূরা আলে ইমরান: ১৭৩, সহীহ বুখারী: ৪৫৬৩",
                timeOrOccasionBn = "ভয়, ষড়যন্ত্র বা বড় বিপদে",
                timeOrOccasionEn = "In times of fear or adversity"
            ),

            // 4. SALAH & PRAYER
            IslamicDuaItem(
                id = "salah_thana",
                titleBn = "নামাজের শুরুতে সানা (তাকবিরে তাহরিমার পর)",
                titleEn = "Thana (Opening Takbeer Dua in Salah)",
                category = DuaCategory.PRAYER_SALAH,
                arabic = "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ ، وَتَبَارَكَ اسْمُكَ ، وَتَعَالَىٰ جَدُّكَ ، وَلَا إِلَٰهَ غَيْرُكَ",
                pronunciationBn = "সুবহানাকাল্লাহুম্মা ওয়া বিহামদিকা, ওয়া তাবারাকাসমুকা, ওয়া তা'আলা জাদ্দুকা, ওয়া লা ইলাহা গয়রুক।",
                pronunciationEn = "Subhanakallahumma wa bihamdika, wa tabarakasmuka, wa ta'ala jadduka, wa la ilaha ghayruk.",
                meaningBn = "হে আল্লাহ! আপনার প্রশংসা সহকারে আপনার পবিত্রতা ঘোষণা করছি। আপনার নাম পরম বরকতময়, আপনার মহিমা অতি উচ্চ এবং আপনি ছাড়া কোনো উপাস্য নেই।",
                meaningEn = "Glory be to You, O Allah, and all praise. Blessed is Your name, exalted is Your majesty, and there is no deity besides You.",
                virtuesBn = "সালাত শুরু করে তাকবিরে তাহরিমা বাঁধার পর সূরা ফাতিহার পূর্বে পাঠ করা সুন্নাত।",
                virtuesEn = "Sunnah opening supplication in every prayer after first Takbeer.",
                reference = "সুনানে আবু দাউদ: ৭৭৬, তিরমিজি: ২৪২",
                timeOrOccasionBn = "সালাতের শুরুতে সূরা ফাতিহার আগে",
                timeOrOccasionEn = "At beginning of Salah"
            ),
            IslamicDuaItem(
                id = "salah_ruku",
                titleBn = "রুকুর তাসবিহ ও দোয়া",
                titleEn = "Dua in Ruku (Bowing in Salah)",
                category = DuaCategory.PRAYER_SALAH,
                arabic = "سُبْحَانَ رَبِّيَ الْعَظِيمِ وَبِحَمْدِهِ",
                pronunciationBn = "সুবহানা রব্বিয়াল 'আজীম (কমপক্ষে ৩ বার)।",
                pronunciationEn = "Subhana Rabbiyal-'Adheem (minimum 3 times).",
                meaningBn = "আমার মহান প্রতিপালকের পবিত্রতা ও প্রশংসা ঘোষণা করছি।",
                meaningEn = "Glory is to my Lord, the Most Great.",
                virtuesBn = "রুকুতে সর্বনিম্ন ৩ বার পাঠ করা সুন্নাত। বিজোড় সংখ্যায় (৩, ৫, ৭ বার) বলা উত্তম।",
                virtuesEn = "Recite at least 3 times with reverence during bowing posture.",
                reference = "সহীহ মুসলিম: ৭৭২, সুনানে আবু দাউদ: ৮৭০",
                repetitionCount = 3,
                timeOrOccasionBn = "সালাতের রুকুতে",
                timeOrOccasionEn = "During Ruku"
            ),
            IslamicDuaItem(
                id = "salah_sujood",
                titleBn = "সিজদার তাসবিহ ও ক্ষমা প্রার্থনা",
                titleEn = "Dua in Sujood (Prostration in Salah)",
                category = DuaCategory.PRAYER_SALAH,
                arabic = "سُبْحَانَ رَبِّيَ الْأَعْلَىٰ وَبِحَمْدِهِ",
                pronunciationBn = "সুবহানা রব্বিয়াল আ'লা (কমপক্ষে ৩ বার)।",
                pronunciationEn = "Subhana Rabbiyal-A'la (minimum 3 times).",
                meaningBn = "আমার সর্বশ্রেষ্ঠ ও সর্বোচ্চ প্রতিপালকের পবিত্রতা ঘোষণা করছি।",
                meaningEn = "Glory is to my Lord, the Most High.",
                virtuesBn = "বান্দা সিজদারত অবস্থায় আল্লাহর সর্বাধিক নিকটবর্তী হয়। তাই সিজদায় বেশি বেশি দোয়া কবুল হয়।",
                virtuesEn = "The closest a servant comes to their Lord is in prostration.",
                reference = "সহীহ মুসলিম: ৪৮২, তিরমিজি: ২৬১",
                repetitionCount = 3,
                timeOrOccasionBn = "সালাতের প্রতিটি সিজদায়",
                timeOrOccasionEn = "During Prostration"
            ),
            IslamicDuaItem(
                id = "between_sujood",
                titleBn = "দুই সিজদার মাঝের বিশেষ দোয়া",
                titleEn = "Dua Between Two Prostrations",
                category = DuaCategory.PRAYER_SALAH,
                arabic = "اللَّهُمَّ اغْفِرْ لِي ، وَارْحَمْنِي ، وَاهْدِنِي ، وَعَافِنِي ، وَارْزُقْنِي",
                pronunciationBn = "আল্লাহুম্মাগফির লী, ওয়ারহামনী, ওয়াহদিনী, ওয়া 'আফিনী, ওয়ারযুক্বনী।",
                pronunciationEn = "Allahummaghfir li, warhamni, wahdini, wa 'afini, warzuqni.",
                meaningBn = "হে আল্লাহ! আমাকে ক্ষমা করুন, আমার উপর দয়া করুন, আমাকে সঠিক পথে পরিচালিত করুন, আমাকে সুস্থতা ও নিরাপত্তা দান করুন এবং আমাকে হালাল রিযিক দান করুন।",
                meaningEn = "O Allah, forgive me, have mercy on me, guide me, grant me well-being, and provide for me.",
                virtuesBn = "দুই সিজদার মাঝে সোজা হয়ে বসে এই দোয়াটি পাঠ করা সুন্নাতে মুয়াক্কাদা সমতুল্য ফজিলতপূর্ণ আমল।",
                virtuesEn = "Sunnah supplication recited while sitting between the two prostrations.",
                reference = "সুনানে আবু দাউদ: ৮৫০, তিরমিজি: ২৮৪",
                timeOrOccasionBn = "সালাতে দুই সিজদার মধ্যবর্তী বৈঠকে",
                timeOrOccasionEn = "Between the two prostrations"
            ),
            IslamicDuaItem(
                id = "tashahhud",
                titleBn = "তাশাহহুদ (আত্তাহিয়্যাতু)",
                titleEn = "Tashahhud (At-Tahiyyat)",
                category = DuaCategory.PRAYER_SALAH,
                arabic = "التَّحِيَّاتُ لِلَّهِ وَالصَّلَوَاتُ وَالطَّيِّبَاتُ ، السَّلَامُ عَلَيْكَ أَيُّهَا النَّبِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ ، السَّلَامُ عَلَيْنَا وَعَلَىٰ عِبَادِ اللَّهِ الصَّالِحِينَ ، أَشْهَدُ أَنْ لَا إِلَٰهَ إِلَّا اللَّهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
                pronunciationBn = "আত্তাহিয়্যাতু লিল্লাহি ওয়াস সলাওয়াতু ওয়াত ত্বইয়্যিবাতু, আসসালামু 'আলাইকা আইয়্যুহান নাবিয়্যু ওয়া রহমাতুল্লাহি ওয়া বারাকাতুহু, আসসালামু 'আলাইনা ওয়া 'আলা 'ইবাদিল্লাহিস সলিহীন। আশহাদু আল লা ইলাহা ইল্লাল্লাহু ওয়া আশহাদু আন্না মুহাম্মাদান 'আবদুহু ওয়া রসূলুহু।",
                pronunciationEn = "At-tahiyyatu lillahi was-salawatu wat-tayyibat. As-salamu 'alayka ayyuhan-Nabiyyu wa rahmatullahi wa barakatuh. As-salamu 'alayna wa 'ala 'ibadillahis-saliheen. Ash-hadu alla ilaha illallahu wa ash-hadu anna Muhammadan 'abduhu wa rasuluh.",
                meaningBn = "সকল মৌখিক, শারীরিক ও আর্থিক ইবাদত আল্লাহর জন্য। হে নবী! আপনার প্রতি শান্তি, আল্লাহর রহমত ও বরকত বর্ষিত হোক। আমাদের প্রতি এবং আল্লাহর নেক বান্দাদের প্রতিও শান্তি বর্ষিত হোক। আমি সাক্ষ্য দিচ্ছি আল্লাহ ছাড়া কোনো উপাস্য নেই এবং মুহাম্মদ (সা.) তাঁর বান্দা ও রাসূল।",
                meaningEn = "All verbal, physical, and monetary worship is for Allah. Peace be upon you, O Prophet, and the mercy of Allah and His blessings, and peace be upon us and upon the righteous servants of Allah. I bear witness that there is no deity except Allah, and I bear witness that Muhammad is His servant and His Messenger.",
                virtuesBn = "সালাতের বৈঠকে তাশাহহুদ পাঠ করা ওয়াজিব। এটি মিরাজের রাতে আল্লাহ ও তাঁর রাসূলের পবিত্র সংলাপের বরকতপূর্ণ রূপ।",
                virtuesEn = "Obligatory recitation in the sitting posture of every prayer.",
                reference = "সহীহ বুখারী: ৮৩১, সহীহ মুসলিম: ৪০২",
                timeOrOccasionBn = "সালাতের প্রথম ও শেষ বৈঠকে",
                timeOrOccasionEn = "During Tashahhud sitting"
            ),
            IslamicDuaItem(
                id = "durood_ibrahim",
                titleBn = "দরূদে ইব্রাহিম (সর্বশ্রেষ্ঠ দরূদ শরীফ)",
                titleEn = "Durood-e-Ibrahim",
                category = DuaCategory.PRAYER_SALAH,
                arabic = "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَعَلَىٰ آلِ مُحَمَّدٍ كَمَا صَلَّيْتَ عَلَىٰ إِبْرَاهِيمَ وَعَلَىٰ آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ ، اللَّهُمَّ بَارِكْ عَلَىٰ مُحَمَّدٍ وَعَلَىٰ آلِ مُحَمَّدٍ كَمَا بَارَكْتَ عَلَىٰ إِبْرَاهِيمَ وَعَلَىٰ آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ",
                pronunciationBn = "আল্লাহুম্মা সল্লি 'আলা মুহাম্মাদিঁও ওয়া 'আলা আলি মুহাম্মাদ, কামা সল্লাইতা 'আলা ইবরাহীমা ওয়া 'আলা আলি ইবরাহীম, ইন্নাকা হামীদুম মাজীদ। আল্লাহুম্মা বারিক 'আলা মুহাম্মাদিঁও ওয়া 'আলা আলি মুহাম্মাদ, কামা বারকতা 'আলা ইবরাহীমা ওয়া 'আলা আলি ইবরাহীম, ইন্নাকা হামীদুম মাজীদ।",
                pronunciationEn = "Allahumma salli 'ala Muhammadin wa 'ala ali Muhammad, kama sallayta 'ala Ibraheema wa 'ala ali Ibraheem, innaka Hameedum Majeed. Allahumma barik 'ala Muhammadin wa 'ala ali Muhammad, kama barakta 'ala Ibraheema wa 'ala ali Ibraheem, innaka Hameedum Majeed.",
                meaningBn = "হে আল্লাহ! মুহাম্মদ (সা.) এবং তাঁর পরিবার-পরিজনের উপর রহমত বর্ষণ করুন, যেমন আপনি ইব্রাহিম (আ.) ও তাঁর পরিবারের উপর রহমত বর্ষণ করেছিলেন। নিশ্চয় আপনি প্রশংসিত ও মহিমান্বিত। হে আল্লাহ! মুহাম্মদ (সা.) এবং তাঁর পরিবারের উপর বরকত নাযিল করুন, যেমন আপনি ইব্রাহিম (আ.) ও তাঁর পরিবারের উপর বরকত নাযিল করেছিলেন। নিশ্চয় আপনি প্রশংসিত ও মহিমান্বিত।",
                meaningEn = "O Allah, send prayers upon Muhammad and upon the family of Muhammad, as You sent prayers upon Ibrahim and upon the family of Ibrahim, indeed You are Praiseworthy and Glorious. O Allah, bless Muhammad and the family of Muhammad, as You blessed Ibrahim and the family of Ibrahim, indeed You are Praiseworthy and Glorious.",
                virtuesBn = "রাসূলুল্লাহ (সা.)-এর উপর একবার দরূদ পাঠ করলে আল্লাহ তা'আলা বান্দার প্রতি ১০টি রহমত নাযিল করেন, ১০টি গুনাহ ক্ষমা করেন এবং ১০টি মর্যাদা বৃদ্ধি করেন।",
                virtuesEn = "Whoever sends blessings upon the Prophet once, Allah will send ten blessings upon him and erase ten sins.",
                reference = "সহীহ বুখারী: ৩৩৭০, সহীহ মুসলিম: ৪০৫",
                timeOrOccasionBn = "সালাতের শেষ বৈঠকে এবং প্রতিদিন বেশি বেশি",
                timeOrOccasionEn = "Final sitting of Salah & daily"
            ),
            IslamicDuaItem(
                id = "dua_masura",
                titleBn = "দোয়ায়ে মাসূরা (সালাম ফেরানোর পূর্বে)",
                titleEn = "Dua Masura (Before Salam in Salah)",
                category = DuaCategory.PRAYER_SALAH,
                arabic = "اللَّهُمَّ إِنِّي ظَلَمْتُ نَفْسِي ظُلْمًا كَثِيرًا ، وَلَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ ، فَاغْفِرْ لِي مَغْفِرَةً مِنْ عِنْدِكَ ، وَارْحَمْنِي إِنَّكَ أَنْتَ الْغَفُورُ الرَّحِيمُ",
                pronunciationBn = "আল্লাহুম্মা ইন্নী জোয়ালামতু নাফসী জুলমান কাসীরাঁও, ওয়া লা ইয়াগফিরুজ জুনূবা ইল্লা আনতা, ফাগফির লী মাগফিরাতাম মিন 'ইনদিকা, ওয়ারহামনী ইন্নাকা আনতাল গফুরুর রহীম।",
                pronunciationEn = "Allahumma inni zalamtu nafsi zulman katheeran, wa la yaghfirudh-dhunuba illa anta, faghfir li maghfiratan min 'indika, warhamni innaka antal-Ghafurur-Raheem.",
                meaningBn = "হে আল্লাহ! আমি নিজের উপর অনেক জুলুম করেছি। আর আপনি ছাড়া গুনাহ মাফ করার আর কেউ নেই। অতএব আপনি নিজ অনুগ্রহে আমাকে ক্ষমা করুন এবং আমার প্রতি দয়া করুন। নিশ্চয়ই আপনি অতি ক্ষমাশীল ও পরম দয়ালু।",
                meaningEn = "O Allah, I have wronged myself greatly and no one forgives sins except You, so grant me forgiveness from You and have mercy on me. Indeed, You are the Forgiving, the Merciful.",
                virtuesBn = "আবু বকর সিদ্দিক (রা.)-এর প্রশ্নের উত্তরে নবীজী (সা.) তাঁকে সালাতের শেষ বৈঠকে এই দোয়াটি পাঠ করতে শিক্ষা দিয়েছিলেন।",
                virtuesEn = "Taught by the Prophet (pbuh) to Abu Bakr (ra) to be recited before concluding prayer.",
                reference = "সহীহ বুখারী: ৮৩৪, সহীহ মুসলিম: ২৭০৫",
                timeOrOccasionBn = "সালাতে দরূদ পড়ার পর সালামের আগে",
                timeOrOccasionEn = "After Durood before Tasleem"
            ),

            // 5. QURANIC DUAS
            IslamicDuaItem(
                id = "rabbana_atina",
                titleBn = "ইহকাল ও পরকালের সর্বাঙ্গীন কল্যাণের দোয়া",
                titleEn = "Dua for Good in This Life & Hereafter",
                category = DuaCategory.RABBANA_QURAN,
                arabic = "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
                pronunciationBn = "রব্বানা আ-তিনা ফিদ দুনয়া হাসানাতাঁও ওয়া ফিল আখিরাতি হাসানাতাঁও ওয়াক্বিনা 'আযাবান নার।",
                pronunciationEn = "Rabbana atina fid-dunya hasanatan wa fil-akhirati hasanatan wa qina 'adhaban-nar.",
                meaningBn = "হে আমাদের প্রতিপালক! আমাদের ইহকালে কল্যাণ দান করুন এবং পরকালেও কল্যাণ দান করুন, আর আমাদেরকে জাহান্নামের আযাব থেকে রক্ষা করুন।",
                meaningEn = "Our Lord, give us in this world that which is good and in the Hereafter that which is good and protect us from the punishment of the Fire.",
                virtuesBn = "কুরআনের সর্বাধিক পঠিত এবং রাসূলুল্লাহ (সা.)-এর সর্বাধিক উচ্চারিত দোয়া। তওয়াফের সময় ও মোনাজাতে এই দোয়া পাঠের বিশেষ গুরুত্ব রয়েছে।",
                virtuesEn = "The most frequent supplication of the Prophet Muhammad (pbuh).",
                reference = "সূরা আল-বাক্বারা: ২০১, সহীহ বুখারী: ৬৩৮৯",
                timeOrOccasionBn = "মোনাজাত, তওয়াফ ও যেকোনো সময়",
                timeOrOccasionEn = "Tawaf, general supplications"
            ),
            IslamicDuaItem(
                id = "parents_dua",
                titleBn = "পিতা-মাতার জন্য কুরআনি মোনাজাত",
                titleEn = "Quranic Dua for Parents",
                category = DuaCategory.RABBANA_QURAN,
                arabic = "رَبِّ ارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا",
                pronunciationBn = "রব্বির হামহুমা কামা রব্বায়ানী সগীরা।",
                pronunciationEn = "Rabbir hamhuma kama rabbayani sagheera.",
                meaningBn = "হে আমার প্রতিপালক! আমার পিতা-মাতার প্রতি দয়া করুন, যেভাবে শৈশবে তাঁরা আমাকে স্নেহ-মমতা দিয়ে লালন-পালন করেছেন।",
                meaningEn = "My Lord, have mercy upon them as they brought me up when I was small.",
                virtuesBn = "পিতা-মাতা জীবিত কিংবা মৃত উভয় অবস্থাতেই তাঁদের জন্য এই দোয়া করা সন্তানের আবশ্যকীয় কর্তব্য।",
                virtuesEn = "Obligatory loving supplication for parents throughout lifetime and after.",
                reference = "সূরা বনী ইসরাঈল (আল-ইসরা): ২৪",
                timeOrOccasionBn = "প্রতিদিন পিতা-মাতার জন্য দোয়ায়",
                timeOrOccasionEn = "Daily prayers for parents"
            ),
            IslamicDuaItem(
                id = "iman_steadfast",
                titleBn = "ঈমানের ওপর অবিচল থাকার দোয়া",
                titleEn = "Dua for Steadfastness in Faith",
                category = DuaCategory.RABBANA_QURAN,
                arabic = "رَبَّنَا لَا تُزِغْ قُلُوبَنَا بَعْدَ إِذْ هَدَيْتَنَا وَهَبْ لَنَا مِنْ لَدُنْكَ رَحْمَةً ۚ إِنَّكَ أَنْتَ الْوَهَّابُ",
                pronunciationBn = "রব্বানা লা তুযিগ ক্বুলূবানা বা'দা ইজ হাদাইতানা ওয়া হাব লানা মিল্লাদুনকা রহমাহ, ইন্নাকা আনতাল ওয়াহহাব।",
                pronunciationEn = "Rabbana la tuzigh qulubana ba'da idh hadaytana wa hab lana min ladunka rahmah, innaka antal-Wahhab.",
                meaningBn = "হে আমাদের প্রতিপালক! সরল পথ প্রদর্শনের পর আপনি আমাদের অন্তরকে সত্যচ্যুত করবেন না এবং আপনার নিকট থেকে আমাদের বিশেষ রহমত দান করুন। নিশ্চয়ই আপনি মহাদাতা।",
                meaningEn = "Our Lord, let not our hearts deviate after You have guided us and grant us from Yourself mercy. Indeed, You are the Bestower.",
                virtuesBn = "ঈমান রক্ষা ও ফিতনার যুগে বিপথগামী হওয়া থেকে নিরাপদ থাকার জন্য অতি শক্তিশালী কুরআনি দোয়া।",
                virtuesEn = "Essential prayer to protect hearts from deviation and fitnah.",
                reference = "সূরা আলে ইমরান: ৮",
                timeOrOccasionBn = "প্রতিদিন সালাতের পর ও মোনাজাতে",
                timeOrOccasionEn = "Daily supplication"
            ),

            // 6. VIRTUOUS AMAL & FAZILAT
            IslamicDuaItem(
                id = "amal_tasbih_100",
                titleBn = "১০০ বার 'সুবহানাল্লাহি ওয়া বিহামদিহি' পাঠের বিশাল ফজিলত",
                titleEn = "Reciting 'Subhanallahi wa Bihamdihi' 100 Times",
                category = DuaCategory.AMAL_FAZILAT,
                arabic = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
                pronunciationBn = "সুবহানাল্লাহি ওয়া বিহামদিহি (প্রতিদিন ১০০ বার)।",
                pronunciationEn = "Subhanallahi wa bihamdihi (100 times daily).",
                meaningBn = "আল্লাহর পবিত্রতা ঘোষণা করছি তাঁর প্রশংসা সহকারে।",
                meaningEn = "Glory is to Allah and praise is to Him.",
                virtuesBn = "রাসূলুল্লাহ (সা.) বলেছেন: যে ব্যক্তি দিনে ১০০ বার এটি পাঠ করবে, তার সমস্ত সগীরা গুনাহ মাফ করে দেওয়া হবে, যদিও তা সমুদ্রের ফেনার সমপরিমাণ হয়! কিয়ামতের দিন এর চেয়ে বেশি নেকি আর কেউ নিয়ে আসতে পারবে না।",
                virtuesEn = "Whoever says this 100 times a day, all sins will be forgiven even if they were like the foam of the sea.",
                reference = "সহীহ বুখারী: ৬৪০৫, সহীহ মুসলিম: ২৬৯১",
                repetitionCount = 100,
                timeOrOccasionBn = "প্রতিদিন সকালে বা যেকোনো সময়ে ১০০ বার",
                timeOrOccasionEn = "100 times daily"
            ),
            IslamicDuaItem(
                id = "amal_tahlil_100",
                titleBn = "১০০ বার তাওহীদের শ্রেষ্ঠ জিকির (১০টি দাস মুক্তির সাওয়াব)",
                titleEn = "Greatest Dhikr of Tawheed (100 Times)",
                category = DuaCategory.AMAL_FAZILAT,
                arabic = "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ ، وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
                pronunciationBn = "লা ইলাহা ইল্লাল্লাহু ওয়াহদাহু লা শারীকা লাহু, লাহুল মুলকু ওয়া লাহুল হামদু, ওয়া হুওয়া 'আলা কুল্লি শাই'ইন ক্বদীর (১০০ বার)।",
                pronunciationEn = "La ilaha illallahu wahdahu la shareeka lah, lahul-mulku wa lahul-hamd, wa huwa 'ala kulli shay'in qadeer (100 times).",
                meaningBn = "এক আল্লাহ ছাড়া কোনো সত্য উপাস্য নেই, তাঁর কোনো শরীক নেই। রাজত্ব ও সার্বভৌমত্ব একমাত্র তাঁরই, সমস্ত প্রশংসা তাঁরই এবং তিনি সবকিছুর উপর পূর্ণ ক্ষমতাবান।",
                meaningEn = "None has the right to be worshipped but Allah alone, who has no partner. His is the dominion and His is the praise, and He is Able to do all things.",
                virtuesBn = "দিনে ১০০ বার পড়লে ১০টি গোলাম আজাদ করার সমান সওয়াব পাওয়া যায়, ১০০টি নেকি লেখা হয়, ১০০টি গুনাহ মোচন করা হয় এবং সারাদিন শয়তানের প্ররোচনা থেকে নিরাপদ থাকা যায়।",
                virtuesEn = "Reward equal to freeing 10 slaves, 100 good deeds recorded, 100 sins erased, and sanctuary from Shaytan all day.",
                reference = "সহীহ বুখারী: ৩২৯৭, সহীহ মুসলিম: ২৬৯১",
                repetitionCount = 100,
                timeOrOccasionBn = "প্রতিদিন সকালে ১০০ বার",
                timeOrOccasionEn = "100 times every morning"
            ),
            IslamicDuaItem(
                id = "amal_surah_mulk",
                titleBn = "প্রতি রাতে সূরা মুলক পাঠের আমল (কবরের আযাব থেকে মুক্তি)",
                titleEn = "Virtue of Reciting Surah Al-Mulk at Night",
                category = DuaCategory.AMAL_FAZILAT,
                arabic = "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
                pronunciationBn = "তাবারাকাল্লাজি বিয়াদিহিল মুলকু ওয়া হুওয়া 'আলা কুল্লি শাই'ইন ক্বদীর (সূরা মুলক - সম্পূর্ণ ৩০ আয়াত)",
                pronunciationEn = "Tabarakallazi biyadihil mulku wa huwa 'ala kulli shay'in qadeer (Surah Al-Mulk - Complete 30 Verses)",
                meaningBn = "পরম বরকতময় তিনি যাঁর হাতে সমগ্র রাজত্ব, এবং তিনি সর্ববিষয়ে সর্বশক্তিমান।",
                meaningEn = "Blessed is He in whose hand is dominion, and He is over all things competent.",
                virtuesBn = "নবী করীম (সা.) সূরা মুলক তিলাওয়াত না করে রাতে ঘুমাতেন না। এই সূরা কবরের আযাব প্রতিহত করে এবং কিয়ামতের দিন বান্দাকে ক্ষমা না করানো পর্যন্ত আল্লাহর দরবারে সুপারিশ করতে থাকবে।",
                virtuesEn = "Protects from the punishment of the grave and intercedes until forgiveness is granted.",
                reference = "জামে তিরমিজি: ২৮৯১, সুনানে নাসায়ী: ১০৫৪৭ (সহীহ)",
                timeOrOccasionBn = "প্রতি রাতে ঘুমানোর পূর্বে",
                timeOrOccasionEn = "Every night before sleeping"
            ),
            IslamicDuaItem(
                id = "amal_surah_kahf",
                titleBn = "জুমার দিনের বিশেষ আমল (সূরা কাহফ ও দরূদ পাঠ)",
                titleEn = "Virtuous Amal for Friday (Surah Al-Kahf & Salawat)",
                category = DuaCategory.AMAL_FAZILAT,
                arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَنزَلَ عَلَىٰ عَبْدِهِ الْكِتَابَ وَلَمْ يَجْعَل لَّهُ عِوَجًا",
                pronunciationBn = "আলহামদু লিল্লাহিল্লাজি আনযালা 'আলা 'আবদিহিল কিতাবা ওয়া লাম ইয়াজ'আল লাহু 'ইওয়াজা (সূরা কাহফ)",
                pronunciationEn = "Alhamdu lillahillazi anzala 'ala 'abdihil-Kitaba wa lam yaj'al lahu 'iwaja (Surah Al-Kahf)",
                meaningBn = "সকল প্রশংসা আল্লাহর জন্য, যিনি তাঁর বান্দার উপর এই কিতাব নাযিল করেছেন এবং এতে কোনো বক্রতা রাখেননি।",
                meaningEn = "Praise be to Allah, who has sent down upon His Servant the Book and has not made therein any deviance.",
                virtuesBn = "১. জুমার দিনে সূরা কাহফ পাঠ করলে দুই জুমার মধ্যবর্তী সময় তার জন্য নূরে উজ্জ্বল থাকবে এবং দাজ্জালের ফিতনা থেকে রক্ষা পাবে।\n২. জুমার দিনে নবীজীর উপর অধিক দরূদ পাঠকারী কিয়ামতের দিন তাঁর সর্বাধিক নিকটবর্তী হবে।\n৩. জুমার দিনে আসরের পর থেকে মাগরিব পর্যন্ত দোয়া কবুলের এক বিশেষ মুহূর্ত (সা'আতুল ইজাবাহ) রয়েছে।",
                virtuesEn = "Illuminates with light between the two Fridays, protects from Dajjal, and marks a special hour where du'as are accepted.",
                reference = "সুনানে বায়হাকী: ৫৯৯৬, সহীহ আল-জামে: ৬৪৭০",
                timeOrOccasionBn = "প্রতি শুক্রবার (বৃহস্পতিবার সূর্যাস্ত থেকে শুক্রবার সূর্যাস্ত পর্যন্ত)",
                timeOrOccasionEn = "Every Friday"
            )
        )
    }

    val filteredDuas = remember(selectedCategory, searchQuery, allDuas) {
        val query = searchQuery.trim().lowercase()
        allDuas.filter { dua ->
            val matchCategory = selectedCategory == DuaCategory.ALL || dua.category == selectedCategory
            val matchSearch = query.isEmpty() ||
                    dua.titleBn.lowercase().contains(query) ||
                    dua.titleEn.lowercase().contains(query) ||
                    dua.pronunciationBn.lowercase().contains(query) ||
                    dua.meaningBn.lowercase().contains(query) ||
                    dua.virtuesBn.lowercase().contains(query) ||
                    dua.reference.lowercase().contains(query)
            matchCategory && matchSearch
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBn) "দোয়া ও বরকতময় আমলসমূহ" else "Authentic Duas & Virtuous Amal",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    Text(
                        text = if (isBn) "সহীহ কুরআন ও হাদিসভিত্তিক সহীহ দোয়া, ফজিলত ও আমল" else "Quranic & Prophetic Supplications with References",
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = themeColors.buttonEqualBg.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${filteredDuas.size} ${if (isBn) "টি দোয়া" else "Items"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.buttonEqualBg,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar inside Dua tool
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = if (isBn) "যেকোনো দোয়া বা আমল খুঁজুন..." else "Search any Dua or Amal...",
                        fontSize = 13.sp,
                        color = themeColors.displayText.copy(alpha = 0.5f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = themeColors.buttonEqualBg,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = themeColors.displayText.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themeColors.buttonEqualBg,
                    unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f),
                    focusedContainerColor = themeColors.background,
                    unfocusedContainerColor = themeColors.background
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DuaCategory.values().forEach { cat ->
                    val isSelected = selectedCategory == cat
                    val catCount = remember(allDuas, cat) {
                        if (cat == DuaCategory.ALL) allDuas.size else allDuas.count { it.category == cat }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) themeColors.buttonEqualBg else themeColors.background,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedCategory = cat }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = cat.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else themeColors.displayText.copy(alpha = 0.7f),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBn) cat.titleBn else cat.titleEn,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else themeColors.displayText
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "($catCount)",
                                fontSize = 10.sp,
                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else themeColors.displayText.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Duas List
            if (filteredDuas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = themeColors.displayText.copy(alpha = 0.4f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBn) "কোনো দোয়া খুঁজে পাওয়া যায়নি" else "No matching Duas found",
                            fontSize = 14.sp,
                            color = themeColors.displayText.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    filteredDuas.forEach { dua ->
                        val isExpanded = expandedDuaId == dua.id || searchQuery.isNotEmpty()
                        val count = counters[dua.id] ?: 0
                        val isCurrentPlaying = playingDuaId == dua.id && isPlaying

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = themeColors.background),
                            border = BorderStroke(
                                1.dp,
                                if (isExpanded) themeColors.buttonEqualBg.copy(alpha = 0.35f) else Color.Transparent
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                // Title Header & Category Badge
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            expandedDuaId = if (expandedDuaId == dua.id) null else dua.id
                                        },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = themeColors.buttonEqualBg.copy(alpha = 0.12f),
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = dua.category.icon,
                                                    contentDescription = null,
                                                    tint = themeColors.buttonEqualBg,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = if (isBn) dua.titleBn else dua.titleEn,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = themeColors.displayText
                                            )
                                            if (dua.timeOrOccasionBn.isNotEmpty()) {
                                                Text(
                                                    text = if (isBn) "সময়: ${dua.timeOrOccasionBn}" else "Occasion: ${dua.timeOrOccasionEn}",
                                                    fontSize = 11.sp,
                                                    color = themeColors.buttonEqualBg,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }

                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Expand",
                                        tint = themeColors.displayText.copy(alpha = 0.5f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Arabic Typography with Tashkeel
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (themeColors.isDark) Color(0xFF0F172A) else Color(0xFFECFDF5),
                                    border = BorderStroke(1.dp, if (themeColors.isDark) Color(0xFF059669).copy(alpha = 0.4f) else Color(0xFF10B981).copy(alpha = 0.25f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = dua.arabic,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (themeColors.isDark) Color(0xFF34D399) else Color(0xFF047857),
                                        textAlign = TextAlign.Center,
                                        lineHeight = 32.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Pronunciation (Distinct Royal Blue Color)
                                Text(
                                    text = if (isBn) "উচ্চারণ: ${dua.pronunciationBn}" else "Pronunciation: ${dua.pronunciationEn}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (themeColors.isDark) Color(0xFF60A5FA) else Color(0xFF1D4ED8),
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Meaning (Distinct Warm Amber Gold Color)
                                Text(
                                    text = if (isBn) "অর্থ: ${dua.meaningBn}" else "Meaning: ${dua.meaningEn}",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (themeColors.isDark) Color(0xFFFBBF24) else Color(0xFFB45309),
                                    lineHeight = 17.5.sp
                                )

                                // Expanded Virtues, Reference & Counter
                                AnimatedVisibility(visible = isExpanded) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp)
                                    ) {
                                        // Virtues / ফজিলত
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = themeColors.buttonEqualBg.copy(alpha = 0.08f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.AutoAwesome,
                                                        contentDescription = null,
                                                        tint = themeColors.buttonEqualBg,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = if (isBn) "ফজিলত ও আমলের নিয়ম" else "Virtue & Instructions",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = themeColors.buttonEqualBg
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = if (isBn) dua.virtuesBn else dua.virtuesEn,
                                                    fontSize = 11.sp,
                                                    color = themeColors.displayText.copy(alpha = 0.85f),
                                                    lineHeight = 15.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Reference / হাদিস সূত্র
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MenuBook,
                                                contentDescription = null,
                                                tint = themeColors.displayText.copy(alpha = 0.6f),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${if (isBn) "রেফারেন্স" else "Reference"}: ${dua.reference}",
                                                fontSize = 11.sp,
                                                color = themeColors.displayText.copy(alpha = 0.65f),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        // Interactive Counter for repeated Duas (e.g., 3x or 100x)
                                        if (dua.repetitionCount > 1) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = themeColors.cardBg,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = if (isBn) "কাউন্টার (টার্গেট: ${dua.repetitionCount} বার)" else "Target: ${dua.repetitionCount} times",
                                                            fontSize = 11.sp,
                                                            color = themeColors.displayText.copy(alpha = 0.7f)
                                                        )
                                                        Text(
                                                            text = "$count / ${dua.repetitionCount}",
                                                            fontSize = 15.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (count >= dua.repetitionCount) Color(0xFF10B981) else themeColors.buttonEqualBg
                                                        )
                                                    }

                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        OutlinedButton(
                                                            onClick = { counters[dua.id] = 0 },
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                            modifier = Modifier.height(32.dp)
                                                        ) {
                                                            Text(
                                                                text = if (isBn) "রিসেট" else "Reset",
                                                                fontSize = 10.sp,
                                                                color = themeColors.displayText
                                                            )
                                                        }

                                                        Button(
                                                            onClick = {
                                                                val current = counters[dua.id] ?: 0
                                                                counters[dua.id] = current + 1
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                                            modifier = Modifier.height(32.dp)
                                                        ) {
                                                            Text(
                                                                text = if (isBn) "কাউন্ট (+১)" else "Count (+1)",
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
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Action Buttons (Play Audio, Copy & Share)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left side: Offline download status / progress
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val progress = downloadProgress[dua.id]
                                        val isDownloaded = downloadedDuaIds.contains(dua.id)
                                        
                                        if (progress != null && progress in 1..99) {
                                            CircularProgressIndicator(
                                                progress = { progress / 100f },
                                                modifier = Modifier.size(14.dp),
                                                strokeWidth = 2.dp,
                                                color = themeColors.buttonEqualBg
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isBn) "ডাউনলোড হচ্ছে ($progress%)" else "Downloading ($progress%)",
                                                fontSize = 10.5.sp,
                                                color = themeColors.buttonEqualBg
                                            )
                                        } else if (isDownloaded || progress == 100) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Offline Available",
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isBn) "ডাউনলোডেড" else "Downloaded",
                                                fontSize = 10.5.sp,
                                                color = Color(0xFF10B981),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    // Right side: Play, Copy, Share buttons
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Play/Pause Button
                                        TextButton(
                                            onClick = {
                                                namazViewModel.playOrPauseDuaAudio(dua.id, null, dua.arabic, dua.pronunciationBn)
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isCurrentPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                                contentDescription = "Play",
                                                tint = if (isCurrentPlaying) Color(0xFF10B981) else themeColors.buttonEqualBg,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isCurrentPlaying) (if (isBn) "থামুন" else "Pause") else (if (isBn) "শুনুন" else "Listen"),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCurrentPlaying) Color(0xFF10B981) else themeColors.buttonEqualBg
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(4.dp))

                                        // Copy Button
                                        TextButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                val fullText = """
                                                    ${if (isBn) dua.titleBn else dua.titleEn}
                                                    ${dua.arabic}
                                                    
                                                    ${if (isBn) "উচ্চারণ" else "Pronunciation"}: ${if (isBn) dua.pronunciationBn else dua.pronunciationEn}
                                                    ${if (isBn) "অর্থ" else "Meaning"}: ${if (isBn) dua.meaningBn else dua.meaningEn}
                                                    
                                                    ${if (isBn) "ফজিলত" else "Virtue"}: ${if (isBn) dua.virtuesBn else dua.virtuesEn}
                                                    ${if (isBn) "রেফারেন্স" else "Reference"}: ${dua.reference}
                                                """.trimIndent()
                                                val clip = android.content.ClipData.newPlainText("Islamic Dua", fullText)
                                                clipboard.setPrimaryClip(clip)
                                                android.widget.Toast.makeText(
                                                    context,
                                                    if (isBn) "দোয়াটি কপি করা হয়েছে" else "Dua copied to clipboard",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy",
                                                tint = themeColors.buttonEqualBg,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isBn) "কপি" else "Copy",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = themeColors.buttonEqualBg
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(4.dp))

                                        // Share Button
                                        TextButton(
                                            onClick = {
                                                val shareText = """
                                                    ${if (isBn) dua.titleBn else dua.titleEn}
                                                    ${dua.arabic}
                                                    
                                                    ${if (isBn) "উচ্চারণ" else "Pronunciation"}: ${if (isBn) dua.pronunciationBn else dua.pronunciationEn}
                                                    ${if (isBn) "অর্থ" else "Meaning"}: ${if (isBn) dua.meaningBn else dua.meaningEn}
                                                    
                                                    ${if (isBn) "ফজিলত" else "Virtue"}: ${if (isBn) dua.virtuesBn else dua.virtuesEn}
                                                    ${if (isBn) "রেফারেন্স" else "Reference"}: ${dua.reference}
                                                """.trimIndent()
                                                val sendIntent = android.content.Intent().apply {
                                                    action = android.content.Intent.ACTION_SEND
                                                    putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                                    type = "text/plain"
                                                }
                                                val shareIntent = android.content.Intent.createChooser(sendIntent, if (isBn) "দোয়া শেয়ার করুন" else "Share Dua")
                                                context.startActivity(shareIntent)
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Share",
                                                tint = themeColors.buttonEqualBg,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isBn) "শেয়ার" else "Share",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = themeColors.buttonEqualBg
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
