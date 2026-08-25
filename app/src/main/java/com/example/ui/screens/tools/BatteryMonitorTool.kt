package com.example.ui.screens.tools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.SystemClock
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.theme.themeCardShadow
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryMonitorTool(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val context = LocalContext.current
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

    // Battery Manager
    val batteryManager = remember { context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager }

    // State for live battery readings
    var batteryLevel by remember { mutableIntStateOf(50) }
    var batteryTemp by remember { mutableFloatStateOf(35.0f) }
    var batteryVoltageMv by remember { mutableIntStateOf(3800) }
    var batteryCurrentMa by remember { mutableIntStateOf(0) }
    var batteryStatus by remember { mutableIntStateOf(BatteryManager.BATTERY_STATUS_UNKNOWN) }
    var batteryPlugged by remember { mutableIntStateOf(0) }
    var batteryHealth by remember { mutableIntStateOf(BatteryManager.BATTERY_HEALTH_UNKNOWN) }
    var batteryTech by remember { mutableStateOf("Li-ion") }

    // Live telemetry history for visual wave graph
    val telemetryHistory = remember { mutableStateListOf<Float>() }
    // Initialize with zeros or default readings
    LaunchedEffect(Unit) {
        if (telemetryHistory.isEmpty()) {
            for (i in 0 until 30) {
                telemetryHistory.add(0f)
            }
        }
    }

    // Broadcast receiver to listen to battery status updates
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    batteryLevel = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val rawTemp = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
                    if (rawTemp > 0) batteryTemp = rawTemp / 10.0f
                    batteryVoltageMv = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
                    batteryStatus = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    batteryPlugged = it.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                    batteryHealth = it.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
                    batteryTech = it.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    // Coroutine to poll the active charge/discharge current in real-time
    LaunchedEffect(Unit) {
        while (true) {
            val rawCurrentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            // divide microamperes by 1000 to get milliamperes (mA)
            var currentMa = rawCurrentNow / 1000
            
            // Adjust current polarity if device reports opposite values
            val isCharging = batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
                    batteryStatus == BatteryManager.BATTERY_STATUS_FULL
            
            if (isCharging && currentMa < 0) {
                currentMa = -currentMa
            } else if (!isCharging && currentMa > 0) {
                currentMa = -currentMa
            }
            
            batteryCurrentMa = currentMa

            // Add to telemetry history for active graphical wave
            if (telemetryHistory.size >= 30) {
                telemetryHistory.removeAt(0)
            }
            telemetryHistory.add(currentMa.toFloat())

            delay(1500)
        }
    }

    // Formatted variables
    val healthText = when (batteryHealth) {
        BatteryManager.BATTERY_HEALTH_GOOD -> if (isBn) "চমৎকার (Good)" else "Good"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> if (isBn) "অতিরিক্ত গরম (Overheat)" else "Overheat"
        BatteryManager.BATTERY_HEALTH_DEAD -> if (isBn) "নিষ্ক্রিয় (Dead)" else "Dead"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> if (isBn) "অতিরিক্ত ভোল্টেজ (Over Voltage)" else "Over Voltage"
        BatteryManager.BATTERY_HEALTH_COLD -> if (isBn) "অতিরিক্ত ঠাণ্ডা (Cold)" else "Cold"
        else -> if (isBn) "স্বাভাবিক (Healthy)" else "Healthy"
    }

    val healthColor = when (batteryHealth) {
        BatteryManager.BATTERY_HEALTH_GOOD -> Color(0xFF4CAF50)
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> Color(0xFFF44336)
        BatteryManager.BATTERY_HEALTH_DEAD -> Color(0xFF9E9E9E)
        else -> Color(0xFF4CAF50)
    }

    val statusText = when (batteryStatus) {
        BatteryManager.BATTERY_STATUS_CHARGING -> if (isBn) "চার্জ হচ্ছে" else "Charging"
        BatteryManager.BATTERY_STATUS_DISCHARGING -> if (isBn) "ডিসচার্জ হচ্ছে" else "Discharging"
        BatteryManager.BATTERY_STATUS_FULL -> if (isBn) "ফুল চার্জড" else "Fully Charged"
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> if (isBn) "চার্জ হচ্ছে না" else "Not Charging"
        else -> if (isBn) "অজানা স্থিতি" else "Unknown"
    }

    val powerSourceText = when (batteryPlugged) {
        BatteryManager.BATTERY_PLUGGED_AC -> if (isBn) "এসি অ্যাডাপ্টার" else "AC Adapter"
        BatteryManager.BATTERY_PLUGGED_USB -> if (isBn) "ইউএসবি পোর্ট" else "USB Port"
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> if (isBn) "ওয়ারলেস চার্জার" else "Wireless Charger"
        else -> if (isBn) "ব্যাটারি ডিসচার্জ" else "Battery (Unplugged)"
    }

    // Estimations
    val voltageV = batteryVoltageMv / 1000.0
    val powerWatts = abs(batteryCurrentMa * voltageV / 1000.0)

    val isCurrentlyCharging = batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
            batteryStatus == BatteryManager.BATTERY_STATUS_FULL

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
    ) {
        // Toolbar with Zero Padding/Headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.selectedToolType = null }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = if (isBn) "পিছনে যান" else "Go Back",
                    tint = themeColors.displayText
                )
            }
            Text(
                text = if (isBn) "ব্যাটারি মনিটর" else "Battery Monitor",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Dial Circular Indicator
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .themeCardShadow(themeColors),
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(160.dp)
                    ) {
                        // Pulsing Wave/Circular animation representing active charging
                        val pulseScale by rememberInfiniteTransition(label = "").animateFloat(
                            initialValue = 0.95f,
                            targetValue = 1.05f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = ""
                        )

                        val circularColor = if (isCurrentlyCharging) Color(0xFF2196F3) else if (batteryLevel <= 20) Color(0xFFF44336) else themeColors.buttonEqualBg

                        // Inner circular track
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(circularColor.copy(alpha = 0.08f * (if (isCurrentlyCharging) pulseScale else 1f)))
                                .border(
                                    width = 6.dp,
                                    brush = Brush.radialGradient(
                                        colors = listOf(circularColor.copy(alpha = 0.1f), circularColor)
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "$batteryLevel",
                                        fontSize = 42.sp,
                                        fontWeight = FontWeight.Black,
                                        color = themeColors.displayText
                                    )
                                    Text(
                                        text = "%",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.displayText.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                                    )
                                }
                                Text(
                                    text = statusText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = circularColor
                                )
                            }
                        }

                        // Circular Progress indicator
                        CircularProgressIndicator(
                            progress = { batteryLevel / 100f },
                            modifier = Modifier.size(150.dp),
                            color = circularColor,
                            strokeWidth = 5.dp,
                            trackColor = themeColors.displayText.copy(alpha = 0.05f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Mini Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = "Temperature",
                                tint = if (batteryTemp > 39f) Color(0xFFFF9800) else themeColors.displayText.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f °C", batteryTemp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                            Text(
                                text = if (isBn) "তাপমাত্রা" else "Temperature",
                                fontSize = 11.sp,
                                color = themeColors.displayText.copy(alpha = 0.5f)
                            )
                        }

                        VerticalDivider(modifier = Modifier.height(40.dp), color = themeColors.displayText.copy(alpha = 0.1f))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Current",
                                tint = if (isCurrentlyCharging) Color(0xFF2196F3) else themeColors.displayText.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${if (batteryCurrentMa > 0) "+" else ""}$batteryCurrentMa mA",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                            Text(
                                text = if (isBn) "রিয়েল-টাইম কারেন্ট" else "Real-time Current",
                                fontSize = 11.sp,
                                color = themeColors.displayText.copy(alpha = 0.5f)
                            )
                        }

                        VerticalDivider(modifier = Modifier.height(40.dp), color = themeColors.displayText.copy(alpha = 0.1f))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = "Voltage",
                                tint = themeColors.displayText.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(Locale.US, "%.2f V", voltageV),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                            Text(
                                text = if (isBn) "ভোল্টেজ" else "Voltage",
                                fontSize = 11.sp,
                                color = themeColors.displayText.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            // Real-Time Waveform Graphical Visualizer
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .themeCardShadow(themeColors),
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "লাইভ কারেন্ট গতির গ্রাফ" else "Live Current Waveform",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isCurrentlyCharging) Color(0xFF2196F3) else Color(0xFF4CAF50))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isCurrentlyCharging) (if (isBn) "চার্জিং" else "Charging") else (if (isBn) "ডিসচার্জিং" else "Discharging"),
                                fontSize = 11.sp,
                                color = themeColors.displayText.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live Wave Graph Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    ) {
                        val waveColor = if (isCurrentlyCharging) Color(0xFF2196F3) else Color(0xFF4CAF50)
                        
                        Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val width = size.width
                            val height = size.height
                            
                            // Draw Grid Lines
                            val gridLines = 4
                            for (i in 0..gridLines) {
                                val y = (height / gridLines) * i
                                drawLine(
                                    color = themeColors.displayText.copy(alpha = 0.04f),
                                    start = androidx.compose.ui.geometry.Offset(0f, y),
                                    end = androidx.compose.ui.geometry.Offset(width, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            // Draw baseline zero
                            drawLine(
                                color = themeColors.displayText.copy(alpha = 0.15f),
                                start = androidx.compose.ui.geometry.Offset(0f, height / 2),
                                end = androidx.compose.ui.geometry.Offset(width, height / 2),
                                strokeWidth = 1.dp.toPx()
                            )

                            // Plot telemetry points
                            if (telemetryHistory.isNotEmpty()) {
                                val pointsCount = telemetryHistory.size
                                val stepX = width / (pointsCount - 1)
                                
                                // Determine min and max mA in history to scale graph beautifully, but prevent division by zero
                                var maxVal = telemetryHistory.maxOf { abs(it) }
                                if (maxVal < 100f) maxVal = 500f // fallback standard scale

                                val path = Path()
                                for (i in 0 until pointsCount) {
                                    val x = i * stepX
                                    val rawVal = telemetryHistory[i]
                                    // Map to height: positive mA (charging) goes up, negative mA goes down from baseline
                                    val relativeY = (rawVal / maxVal) * (height / 2.2f)
                                    val y = (height / 2f) - relativeY

                                    if (i == 0) {
                                        path.moveTo(x, y)
                                    } else {
                                        path.lineTo(x, y)
                                    }
                                }

                                drawPath(
                                    path = path,
                                    color = waveColor,
                                    style = Stroke(width = 3.dp.toPx())
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (isBn) "প্রতি ১.৫ সেকেন্ডে রিয়েল-টাইম রিডিং আপডেট করা হয়" else "Updates telemetry dynamically every 1.5s",
                        fontSize = 11.sp,
                        color = themeColors.displayText.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Power Metrics Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .themeCardShadow(themeColors),
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (isBn) "পাওয়ার পরিসংখ্যান" else "Power & Charging Metrics",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )

                    HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.06f))

                    // Row 1: Charging Speed Power
                    MetricRow(
                        icon = Icons.Default.ElectricBolt,
                        title = if (isBn) "চার্জিং শক্তি (Power)" else "Active Power Speed",
                        value = String.format(Locale.US, "%.2f Watts", powerWatts),
                        themeColors = themeColors
                    )

                    // Row 2: Battery Health
                    MetricRow(
                        icon = Icons.Default.Favorite,
                        title = if (isBn) "ব্যাটারি স্বাস্থ্য" else "Battery Health Status",
                        value = healthText,
                        valueColor = healthColor,
                        themeColors = themeColors
                    )

                    // Row 3: Power Source
                    MetricRow(
                        icon = Icons.Default.Power,
                        title = if (isBn) "পাওয়ার সোর্স" else "Power Source Type",
                        value = powerSourceText,
                        themeColors = themeColors
                    )

                    // Row 4: Battery Technology
                    MetricRow(
                        icon = Icons.Default.SettingsSuggest,
                        title = if (isBn) "ব্যাটারি টেকনোলজি" else "Battery Technology",
                        value = batteryTech,
                        themeColors = themeColors
                    )
                }
            }
        }
    }
}

@Composable
fun MetricRow(
    icon: ImageVector,
    title: String,
    value: String,
    valueColor: Color? = null,
    themeColors: CalculatorThemeColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(themeColors.displayText.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = themeColors.displayText.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.displayText.copy(alpha = 0.7f)
            )
        }
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor ?: themeColors.displayText
        )
    }
}
