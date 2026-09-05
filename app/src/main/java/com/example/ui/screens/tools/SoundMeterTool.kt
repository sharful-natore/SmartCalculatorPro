package com.example.ui.screens.tools

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.*

private val CalculatorThemeColors.accent: Color get() = this.buttonEqualBg
private val CalculatorThemeColors.onAccent: Color get() = this.buttonEqualText
private val CalculatorThemeColors.onSurface: Color get() = this.displayText
private val CalculatorThemeColors.surface: Color get() = this.cardBg
private val CalculatorThemeColors.surfaceVariant: Color get() = this.chipBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundMeterTool(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isBn = viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (!granted) {
            Toast.makeText(context, if (isBn) "মাইক্রোফোন পারমিশন প্রয়োজন" else "Microphone permission required", Toast.LENGTH_SHORT).show()
        }
    }

    // Audio Measurement States
    var isMeasuring by remember { mutableStateOf(true) }
    var currentDb by remember { mutableFloatStateOf(30.0f) }
    var minDb by remember { mutableFloatStateOf(120.0f) }
    var maxDb by remember { mutableFloatStateOf(0.0f) }
    var avgDbAccumulator by remember { mutableDoubleStateOf(0.0) }
    var sampleCount by remember { mutableIntStateOf(0) }
    var calibrationOffset by remember { mutableFloatStateOf(10.0f) }
    var showCalibrationDialog by remember { mutableStateOf(false) }

    // Waveform history buffer (60 points)
    val historyBuffer = remember { mutableStateListOf<Float>().apply { repeat(60) { add(30f) } } }

    fun resetStats() {
        minDb = 120.0f
        maxDb = 0.0f
        avgDbAccumulator = 0.0
        sampleCount = 0
        historyBuffer.clear()
        repeat(60) { historyBuffer.add(30f) }
    }

    // Launch Permission on open
    LaunchedEffect(Unit) {
        if (!hasAudioPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // AudioRecord Measurement Coroutine
    LaunchedEffect(hasAudioPermission, isMeasuring, calibrationOffset) {
        if (!hasAudioPermission || !isMeasuring) return@LaunchedEffect

        withContext(Dispatchers.IO) {
            val sampleRate = 44100
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            val bufferSize = max(minBufferSize, 2048)
            val buffer = ShortArray(bufferSize)

            var audioRecord: AudioRecord? = null
            try {
                @SuppressLint("MissingPermission")
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )

                if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
                    audioRecord.startRecording()

                    var smoothedDb = currentDb
                    while (isActive && isMeasuring) {
                        val read = audioRecord.read(buffer, 0, buffer.size)
                        if (read > 0) {
                            var sumSquares = 0.0
                            for (i in 0 until read) {
                                val s = buffer[i].toDouble()
                                sumSquares += s * s
                            }
                            val rms = sqrt(sumSquares / read)

                            // Convert RMS to decibels: 20 * log10(rms) + offset
                            val rawDb = if (rms > 1.0) {
                                (20.0 * log10(rms) + calibrationOffset.toDouble()).toFloat().coerceIn(15f, 120f)
                            } else {
                                20f
                            }

                            // Damping smoothing
                            smoothedDb = (smoothedDb * 0.7f) + (rawDb * 0.3f)

                            withContext(Dispatchers.Main) {
                                currentDb = smoothedDb
                                if (smoothedDb < minDb && smoothedDb > 10f) minDb = smoothedDb
                                if (smoothedDb > maxDb) maxDb = smoothedDb
                                avgDbAccumulator += smoothedDb
                                sampleCount++

                                if (historyBuffer.size >= 60) {
                                    historyBuffer.removeAt(0)
                                }
                                historyBuffer.add(smoothedDb)
                            }
                        }
                        kotlinx.coroutines.delay(80)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    audioRecord?.stop()
                    audioRecord?.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    val avgDb = remember(avgDbAccumulator, sampleCount) {
        if (sampleCount > 0) (avgDbAccumulator / sampleCount).toFloat() else 0f
    }

    // Smooth Needle Animation
    val animatedDb by animateFloatAsState(
        targetValue = currentDb,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "needle_db"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isBn) "সাউন্ড ও নয়েজ মিটার" else "Sound / Decibel Meter",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = themeColors.onSurface
                        )
                        Text(
                            text = if (isMeasuring) (if (isBn) "রিয়েল-টাইম পরিমাপ চলছে..." else "Measuring Live Sound...")
                            else (if (isBn) "পরিমাপ স্থগিত রয়েছে" else "Paused"),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isMeasuring) Color(0xFF4CAF50) else Color(0xFFFF9800)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = themeColors.onSurface
                        )
                    }
                },
                actions = {
                    // Reset Stats
                    IconButton(onClick = { resetStats() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = themeColors.onSurface)
                    }
                    // Calibration Setting
                    IconButton(onClick = { showCalibrationDialog = true }) {
                        Icon(Icons.Default.Tune, contentDescription = "Calibrate", tint = themeColors.accent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = themeColors.surface)
            )
        },
        containerColor = themeColors.background
    ) { innerPadding ->
        if (!hasAudioPermission) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = themeColors.accent,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isBn) "মাইক্রোফোন ব্যবহারের অনুমতি প্রয়োজন" else "Microphone Permission Required",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = themeColors.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isBn) "আশেপাশের শব্দের তীব্রতা ও ডেসিবেল (dB) পরিমাপ করতে মাইক্রোফোনের অনুমতি দিন।"
                        else "Allow microphone access to measure environmental sound and noise levels.",
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                    ) {
                        Text(if (isBn) "অনুমতি দিন" else "Grant Permission", color = themeColors.onAccent)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // High Noise Hazard Alert Banner (>85 dB)
                if (currentDb >= 85f) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isBn) "সতর্কতা: বিপজ্জনক উচ্চ শব্দ! (৮৫+ dB)" else "Warning: Hazardous Noise Level!",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (isBn) "দীর্ঘক্ষণ এই তীব্রতার শব্দে থাকলে স্থায়ী শ্রবণক্ষতি হতে পারে।"
                                        else "Prolonged exposure to sounds above 85 dB can cause hearing damage.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Analog Dial Needle Gauge Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Circular Semi-Arc Gauge Canvas
                            Box(
                                modifier = Modifier
                                    .size(240.dp)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val h = size.height
                                    val center = Offset(w / 2, h * 0.58f)
                                    val radius = w * 0.42f
                                    val strokeWidth = 22f

                                    val startAngle = 145f
                                    val sweepAngle = 250f

                                    // Background Track
                                    drawArc(
                                        color = Color.Gray.copy(alpha = 0.18f),
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = false,
                                        topLeft = Offset(center.x - radius, center.y - radius),
                                        size = Size(radius * 2, radius * 2),
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )

                                    // Colored Zone Segments:
                                    // 20-55 dB: Safe Green
                                    val greenSweep = sweepAngle * (35f / 100f)
                                    drawArc(
                                        color = Color(0xFF4CAF50),
                                        startAngle = startAngle,
                                        sweepAngle = greenSweep,
                                        useCenter = false,
                                        topLeft = Offset(center.x - radius, center.y - radius),
                                        size = Size(radius * 2, radius * 2),
                                        style = Stroke(width = strokeWidth)
                                    )

                                    // 55-70 dB: Normal Cyan/Blue
                                    val blueSweep = sweepAngle * (20f / 100f)
                                    drawArc(
                                        color = Color(0xFF00BCD4),
                                        startAngle = startAngle + greenSweep,
                                        sweepAngle = blueSweep,
                                        useCenter = false,
                                        topLeft = Offset(center.x - radius, center.y - radius),
                                        size = Size(radius * 2, radius * 2),
                                        style = Stroke(width = strokeWidth)
                                    )

                                    // 70-85 dB: Loud Amber
                                    val amberSweep = sweepAngle * (18f / 100f)
                                    drawArc(
                                        color = Color(0xFFFFB300),
                                        startAngle = startAngle + greenSweep + blueSweep,
                                        sweepAngle = amberSweep,
                                        useCenter = false,
                                        topLeft = Offset(center.x - radius, center.y - radius),
                                        size = Size(radius * 2, radius * 2),
                                        style = Stroke(width = strokeWidth)
                                    )

                                    // 85-120 dB: Danger Red
                                    val redSweep = sweepAngle - (greenSweep + blueSweep + amberSweep)
                                    drawArc(
                                        color = Color(0xFFF44336),
                                        startAngle = startAngle + greenSweep + blueSweep + amberSweep,
                                        sweepAngle = redSweep,
                                        useCenter = false,
                                        topLeft = Offset(center.x - radius, center.y - radius),
                                        size = Size(radius * 2, radius * 2),
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )

                                    // Needle Drawing
                                    val fraction = ((animatedDb - 20f) / 100f).coerceIn(0f, 1f)
                                    val needleAngleRad = Math.toRadians((startAngle + sweepAngle * fraction).toDouble())
                                    val needleLength = radius * 0.88f
                                    val needleTip = Offset(
                                        (center.x + needleLength * cos(needleAngleRad)).toFloat(),
                                        (center.y + needleLength * sin(needleAngleRad)).toFloat()
                                    )

                                    drawLine(
                                        color = if (animatedDb >= 85f) Color(0xFFD32F2F) else themeColors.accent,
                                        start = center,
                                        end = needleTip,
                                        strokeWidth = 6f,
                                        cap = StrokeCap.Round
                                    )

                                    // Pivot center circle
                                    drawCircle(
                                        color = if (animatedDb >= 85f) Color(0xFFD32F2F) else themeColors.accent,
                                        radius = 12f,
                                        center = center
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 4f,
                                        center = center
                                    )
                                }

                                // Center Digital dB Display
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = String.format("%.1f", animatedDb),
                                        style = MaterialTheme.typography.displaySmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 38.sp
                                        ),
                                        color = when {
                                            animatedDb >= 85f -> Color(0xFFF44336)
                                            animatedDb >= 70f -> Color(0xFFFFB300)
                                            animatedDb >= 55f -> Color(0xFF00BCD4)
                                            else -> Color(0xFF4CAF50)
                                        }
                                    )
                                    Text(
                                        text = "dB (Decibel)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = themeColors.onSurface.copy(alpha = 0.55f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Noise Level Interpretation Badge
                            val (levelText, levelColor) = when {
                                animatedDb < 40f -> (if (isBn) "খুব শান্ত পরিবেশ (Quiet)" else "Very Quiet") to Color(0xFF4CAF50)
                                animatedDb < 60f -> (if (isBn) "শান্ত ঘর বা অফিস (Peaceful)" else "Peaceful / Office") to Color(0xFF4CAF50)
                                animatedDb < 70f -> (if (isBn) "স্বাভাবিক কথাবার্তা (Conversation)" else "Normal Conversation") to Color(0xFF00BCD4)
                                animatedDb < 85f -> (if (isBn) "উচ্চ শব্দ / ট্রাফিক (Loud Noise)" else "Loud / City Noise") to Color(0xFFFFB300)
                                else -> (if (isBn) "অতিরিক্ত ক্ষতিকর শব্দ! (Hazardous!)" else "Hazardous Danger!") to Color(0xFFF44336)
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = levelColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = levelText,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = levelColor,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Min / Avg / Max Statistics Cards Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatBox(
                            title = if (isBn) "সর্বনিম্ন (MIN)" else "MIN",
                            value = if (minDb <= 120f) String.format("%.1f", minDb) else "--",
                            color = Color(0xFF4CAF50),
                            themeColors = themeColors,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            title = if (isBn) "গড় (AVG)" else "AVG",
                            value = if (sampleCount > 0) String.format("%.1f", avgDb) else "--",
                            color = Color(0xFF00BCD4),
                            themeColors = themeColors,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            title = if (isBn) "সর্বোচ্চ (MAX)" else "MAX",
                            value = if (maxDb > 0f) String.format("%.1f", maxDb) else "--",
                            color = Color(0xFFF44336),
                            themeColors = themeColors,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Real-Time Waveform Chart
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBn) "রিয়েল-টাইম অডিও তরঙ্গ (Waveform)" else "Real-Time Noise History",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = themeColors.onSurface
                                )
                                Text(
                                    text = "100 dB",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = themeColors.onSurface.copy(alpha = 0.5f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Canvas Graph
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                            ) {
                                val w = size.width
                                val h = size.height
                                val points = historyBuffer.toList()
                                if (points.size < 2) return@Canvas

                                val stepX = w / (points.size - 1)
                                val path = androidx.compose.ui.graphics.Path()
                                val fillPath = androidx.compose.ui.graphics.Path()

                                fillPath.moveTo(0f, h)

                                points.forEachIndexed { index, dbVal ->
                                    val normalized = ((dbVal - 20f) / 100f).coerceIn(0.05f, 1f)
                                    val x = index * stepX
                                    val y = h - (normalized * h)

                                    if (index == 0) {
                                        path.moveTo(x, y)
                                        fillPath.lineTo(x, y)
                                    } else {
                                        path.lineTo(x, y)
                                        fillPath.lineTo(x, y)
                                    }
                                }

                                fillPath.lineTo(w, h)
                                fillPath.close()

                                // Draw Gradient Fill Area
                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            themeColors.accent.copy(alpha = 0.35f),
                                            themeColors.accent.copy(alpha = 0.02f)
                                        )
                                    )
                                )

                                // Draw Waveform Stroke
                                drawPath(
                                    path = path,
                                    color = themeColors.accent,
                                    style = Stroke(width = 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }
                    }
                }

                // Controls Row (Pause/Resume & Reset)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { isMeasuring = !isMeasuring },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMeasuring) Color(0xFFFF9800) else Color(0xFF4CAF50)
                            )
                        ) {
                            Icon(
                                imageVector = if (isMeasuring) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isMeasuring) (if (isBn) "স্থগিত করুন" else "Pause")
                                else (if (isBn) "চালু করুন" else "Resume"),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { resetStats() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isBn) "রিসেট করুন" else "Reset Stats")
                        }
                    }
                }

                // Environmental Noise Reference Comparison Table
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (isBn) "শব্দ মাত্রার সাধারণ রেফারেন্স গাইড" else "Sound Level Reference Scale",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = themeColors.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            listOf(
                                Triple("20 - 30 dB", if (isBn) "ফিসফিস শব্দ বা শান্ত পাতার ঝিরঝির" else "Whisper or rustling leaves", Color(0xFF4CAF50)),
                                Triple("40 - 50 dB", if (isBn) "নীরব লাইব্রেরি বা শান্ত বাসস্থান" else "Quiet library or calm room", Color(0xFF4CAF50)),
                                Triple("60 - 70 dB", if (isBn) "স্বাভাবিক কথোপকথন বা মাঝারি বৃষ্টি" else "Normal speech or light rain", Color(0xFF00BCD4)),
                                Triple("75 - 80 dB", if (isBn) "ব্যস্ত রাজপথের ট্রাফিক বা ব্লেন্ডার" else "City traffic or vacuum cleaner", Color(0xFFFFB300)),
                                Triple("85 - 90 dB", if (isBn) "লন মাওয়ার বা ভারী কারখানা (ক্ষতির শুরু)" else "Lawn mower / heavy machine", Color(0xFFFF5722)),
                                Triple("100+ dB", if (isBn) "রক কনসার্ট, সাইরেন বা জেট বিমান (তীব্র ঝুঁকি)" else "Rock concert, siren, jet engine", Color(0xFFF44336))
                            ).forEach { (range, desc, col) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = col.copy(alpha = 0.15f),
                                        modifier = Modifier.width(80.dp)
                                    ) {
                                        Text(
                                            text = range,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = col,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = themeColors.onSurface.copy(alpha = 0.75f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Microphone Calibration Dialog
        if (showCalibrationDialog) {
            AlertDialog(
                onDismissRequest = { showCalibrationDialog = false },
                title = { Text(if (isBn) "মাইক্রোফোন ক্যালিব্রেশন" else "Microphone Calibration") },
                text = {
                    Column {
                        Text(
                            text = if (isBn) "মোবাইল ব্র্যান্ডভেদে মাইক্রোফোনের সংবেদনশীলতা আলাদা হতে পারে। প্রয়োজনে ডেসিবেল অফসেট সমন্বয় করুন।"
                            else "Adjust decibel offset based on your device's microphone hardware sensitivity.",
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isBn) "বর্তমান অফসেট: ${String.format("%+.1f", calibrationOffset)} dB"
                            else "Offset: ${String.format("%+.1f", calibrationOffset)} dB",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = themeColors.accent
                        )
                        Slider(
                            value = calibrationOffset,
                            onValueChange = { calibrationOffset = it },
                            valueRange = -15f..25f,
                            steps = 40,
                            colors = SliderDefaults.colors(thumbColor = themeColors.accent, activeTrackColor = themeColors.accent)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showCalibrationDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                    ) {
                        Text(if (isBn) "ঠিক আছে" else "Done", color = themeColors.onAccent)
                    }
                }
            )
        }
    }
}

@Composable
fun StatBox(
    title: String,
    value: String,
    color: Color,
    themeColors: CalculatorThemeColors,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = themeColors.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = color
            )
            Text(
                text = "dB",
                style = MaterialTheme.typography.labelSmall,
                color = themeColors.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}
