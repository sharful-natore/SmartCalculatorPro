package com.example.ui.screens.tools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.SystemClock
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

private data class BatteryTimeTuple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

private fun convertBatteryBnDigits(input: String): String {
    val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    val sb = StringBuilder()
    for (ch in input) {
        if (ch in '0'..'9') {
            sb.append(bnDigits[ch - '0'])
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}

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
            
            // Standard Android reports in microamperes (uA), but some manufacturers use milliamperes (mA)
            var currentMa = if (abs(rawCurrentNow) > 20000) {
                rawCurrentNow / 1000
            } else {
                rawCurrentNow
            }
            
            // Adjust current polarity based on battery status if it seems inconsistent
            val isCharging = batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
                    batteryStatus == BatteryManager.BATTERY_STATUS_FULL
            
            if (isCharging && currentMa < 0) {
                currentMa = abs(currentMa)
            } else if (!isCharging && currentMa > 0) {
                currentMa = -currentMa
            }
            
            // Fallback for some emulators or devices that return 0 for CURRENT_NOW
            if (currentMa == 0) {
                val avgCurrent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
                currentMa = if (abs(avgCurrent) > 20000) avgCurrent / 1000 else avgCurrent
            }

            batteryCurrentMa = currentMa

            // Add to telemetry history for active graphical wave
            if (telemetryHistory.size >= 30) {
                telemetryHistory.removeAt(0)
            }
            telemetryHistory.add(currentMa.toFloat())

            delay(1000) // Poll faster (every second) for better real-time feel
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

    val chargeSpeedText = if (isCurrentlyCharging) {
        when {
            batteryCurrentMa < 700 -> if (isBn) "ধীর গতির চার্জিং (Slow)" else "Slow Charging"
            batteryCurrentMa < 1500 -> if (isBn) "স্বাভাবিক চার্জিং (Normal)" else "Normal Charging"
            batteryCurrentMa < 2500 -> if (isBn) "দ্রুত চার্জিং (Fast)" else "Fast Charging"
            else -> if (isBn) "টার্বো চার্জিং (Turbo)" else "Turbo Charging"
        }
    } else {
        if (isBn) "ডিসচার্জিং" else "Discharging"
    }
    
    val chargeSpeedColor = if (isCurrentlyCharging) {
        when {
            batteryCurrentMa < 700 -> Color(0xFFFF9800)
            batteryCurrentMa < 1500 -> Color(0xFF4CAF50)
            batteryCurrentMa < 2500 -> Color(0xFF03A9F4)
            else -> Color(0xFF9C27B0)
        }
    } else themeColors.displayText

    // Dynamic Time Estimation (Discharging remaining time & Charging remaining time)
    // Updated every 30 seconds as requested
    var backupEstimateTick by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            backupEstimateTick = System.currentTimeMillis()
            delay(30000L) // 30 seconds interval
        }
    }

    val timeEstimation = remember(
        backupEstimateTick,
        batteryLevel,
        batteryStatus,
        batteryCurrentMa,
        isCurrentlyCharging,
        batteryPlugged,
        isBn
    ) {
        if (batteryLevel >= 100 || batteryStatus == BatteryManager.BATTERY_STATUS_FULL) {
            val title = if (isBn) "ফুল চার্জ সম্পন্ন" else "Full Charge Complete"
            val value = if (isBn) "১০০% চার্জড" else "100% Charged"
            val sub = if (isBn) "ডিভাইস সম্পূর্ণ চার্জ হয়েছে" else "Device battery is fully charged"
            val icon = Icons.Default.CheckCircle
            BatteryTimeTuple(title, value, sub, icon)
        } else if (isCurrentlyCharging) {
            // Android System API check on P+
            val sysRemainingMs = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                try { batteryManager.computeChargeTimeRemaining() } catch (e: Exception) { -1L }
            } else -1L

            val totalMins = if (sysRemainingMs > 0L) {
                (sysRemainingMs / 60000L).toInt()
            } else {
                // Precise heuristic estimation
                val neededPercent = (100 - batteryLevel).coerceAtLeast(1)
                val effectiveMa = when {
                    batteryCurrentMa > 200 -> batteryCurrentMa.toDouble()
                    batteryPlugged == BatteryManager.BATTERY_PLUGGED_AC -> 1600.0
                    batteryPlugged == BatteryManager.BATTERY_PLUGGED_WIRELESS -> 900.0
                    else -> 500.0
                }
                // Assumed standard 5000 mAh capacity
                val neededCapacityMah = (neededPercent / 100.0) * 5000.0
                val hours = (neededCapacityMah / effectiveMa) * 1.12 // 12% CC/CV taper
                (hours * 60).toInt().coerceIn(2, 600)
            }

            val hrs = totalMins / 60
            val mins = totalMins % 60
            val timeFormatted = if (hrs > 0 && mins > 0) {
                if (isBn) "${convertBatteryBnDigits(hrs.toString())} ঘণ্টা ${convertBatteryBnDigits(mins.toString())} মিনিট"
                else "${hrs}h ${mins}m"
            } else if (hrs > 0) {
                if (isBn) "${convertBatteryBnDigits(hrs.toString())} ঘণ্টা" else "${hrs} hr"
            } else {
                if (isBn) "${convertBatteryBnDigits(mins.toString())} মিনিট" else "${mins} min"
            }

            val title = if (isBn) "ফুল চার্জ হতে বাকি" else "Time to Full Charge"
            val sub = if (isBn) "বর্তমান চার্জিং গতির ভিত্তিতে আনুমানিক হিসাব" else "Estimated based on active charging current"
            val icon = Icons.Default.BatteryChargingFull
            BatteryTimeTuple(title, timeFormatted, sub, icon)
        } else {
            // Discharging state
            val effectiveDrainMa = if (abs(batteryCurrentMa) > 50) abs(batteryCurrentMa).toDouble() else 380.0
            val remainingMah = (batteryLevel / 100.0) * 5000.0
            val hours = remainingMah / effectiveDrainMa
            val totalMins = (hours * 60).toInt().coerceIn(5, 7200)
            val hrs = totalMins / 60
            val mins = totalMins % 60
            val timeFormatted = if (hrs > 0 && mins > 0) {
                if (isBn) "${convertBatteryBnDigits(hrs.toString())} ঘণ্টা ${convertBatteryBnDigits(mins.toString())} মিনিট"
                else "${hrs}h ${mins}m"
            } else if (hrs > 0) {
                if (isBn) "${convertBatteryBnDigits(hrs.toString())} ঘণ্টা" else "${hrs} hr"
            } else {
                if (isBn) "${convertBatteryBnDigits(mins.toString())} মিনিট" else "${mins} min"
            }

            val title = if (isBn) "ব্যাটারি ব্যাকআপ চলবে" else "Estimated Battery Life"
            val sub = if (isBn) "বর্তমান পাওয়ার খরচের হারে আনুমানিক সময়" else "Estimated runtime on current power drain"
            val icon = Icons.Default.HourglassTop
            BatteryTimeTuple(title, timeFormatted, sub, icon)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Dial Circular Indicator
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .themeCardShadow(themeColors, shape = RoundedCornerShape(32.dp)),
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                shape = RoundedCornerShape(32.dp)
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
                        val pulseScale by rememberInfiniteTransition(label = "pulse").animateFloat(
                            initialValue = 1f,
                            targetValue = 1.15f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse"
                        )

                        val waveOffset by rememberInfiniteTransition(label = "wave").animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(3000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "wave"
                        )

                        val circularColor = if (isCurrentlyCharging) Color(0xFF00E5FF) else if (batteryLevel <= 20) Color(0xFFF44336) else themeColors.buttonEqualBg

                        // Decorative background glows
                        if (isCurrentlyCharging) {
                            Box(
                                modifier = Modifier
                                    .size(145.dp * pulseScale)
                                    .clip(CircleShape)
                                    .background(circularColor.copy(alpha = 0.1f))
                            )
                        }

                        // Inner circular track with "Liquid" wave effect
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(circularColor.copy(alpha = 0.05f))
                                .border(
                                    width = 1.dp,
                                    color = circularColor.copy(alpha = 0.2f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCurrentlyCharging) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val wavePath1 = Path()
                                    val wavePath2 = Path()
                                    val centerY = size.height * (1f - (batteryLevel / 100f))
                                    val waveHeight = 8.dp.toPx()
                                    
                                    // Primary Wave
                                    wavePath1.moveTo(0f, size.height)
                                    wavePath1.lineTo(0f, centerY)
                                    for (x in 0..size.width.toInt() step 5) {
                                        val y = centerY + kotlin.math.sin(Math.toRadians(x.toDouble() + waveOffset)) * waveHeight
                                        wavePath1.lineTo(x.toFloat(), y.toFloat())
                                    }
                                    wavePath1.lineTo(size.width, size.height)
                                    wavePath1.close()

                                    // Secondary Wave
                                    wavePath2.moveTo(0f, size.height)
                                    wavePath2.lineTo(0f, centerY)
                                    for (x in 0..size.width.toInt() step 5) {
                                        val y = centerY + kotlin.math.cos(Math.toRadians(x.toDouble() + waveOffset * 0.8)) * (waveHeight * 0.7f)
                                        wavePath2.lineTo(x.toFloat(), y.toFloat())
                                    }
                                    wavePath2.lineTo(size.width, size.height)
                                    wavePath2.close()
                                    
                                    drawPath(
                                        path = wavePath2,
                                        color = circularColor.copy(alpha = 0.15f)
                                    )
                                    drawPath(
                                        path = wavePath1,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(circularColor.copy(alpha = 0.4f), circularColor.copy(alpha = 0.15f))
                                        )
                                    )
                                }
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "$batteryLevel",
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.Black,
                                        color = themeColors.displayText,
                                        letterSpacing = (-2).sp
                                    )
                                    Text(
                                        text = "%",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = circularColor,
                                        modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                                    )
                                }
                                if (isCurrentlyCharging) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = circularColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Text(
                                        text = statusText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = circularColor.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        // Circular Progress indicator (Outer Ring)
                        CircularProgressIndicator(
                            progress = { batteryLevel / 100f },
                            modifier = Modifier.size(154.dp),
                            color = circularColor,
                            strokeWidth = 6.dp,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
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

            // Time Remaining Estimation Card (Discharging runtime & Charging time to full) - Crisp White Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .themeCardShadow(themeColors, shape = RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(
                    1.dp,
                    Color(0xFFE2E8F0)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCurrentlyCharging) Color(0xFFE0F2FE)
                                else Color(0xFFF1F5F9)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = timeEstimation.fourth,
                            contentDescription = null,
                            tint = if (isCurrentlyCharging) Color(0xFF0284C7) else Color(0xFF334155),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = timeEstimation.first,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = timeEstimation.second,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isCurrentlyCharging) Color(0xFF0284C7) else Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = timeEstimation.third,
                            fontSize = 10.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Real-Time Waveform Graphical Visualizer
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .themeCardShadow(themeColors, shape = RoundedCornerShape(32.dp)),
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                shape = RoundedCornerShape(32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
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

                    Spacer(modifier = Modifier.height(14.dp))

                    val waveColor = if (isCurrentlyCharging) Color(0xFF00E5FF) else Color(0xFF4CAF50)
                    var maxVal = telemetryHistory.maxOfOrNull { abs(it) } ?: 800f
                    if (maxVal < 100f) maxVal = 800f
                    val maxValInt = maxVal.toInt()

                    // Graph Container with Left Numeric Scale and Right Canvas
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Numeric Scale Column
                        Column(
                            modifier = Modifier
                                .width(34.dp)
                                .fillMaxHeight()
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "+${if (isBn) convertBatteryBnDigits(maxValInt.toString()) else maxValInt.toString()}",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText.copy(alpha = 0.55f),
                                maxLines = 1
                            )
                            Text(
                                text = if (isBn) "০ mA" else "0 mA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = themeColors.displayText.copy(alpha = 0.4f),
                                maxLines = 1
                            )
                            Text(
                                text = "-${if (isBn) convertBatteryBnDigits(maxValInt.toString()) else maxValInt.toString()}",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText.copy(alpha = 0.55f),
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Live Wave Graph Canvas with Under-Line Shadow
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            Canvas(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val width = size.width
                                val height = size.height

                                // Draw Horizontal Grid Lines aligned with scale
                                drawLine(
                                    color = themeColors.displayText.copy(alpha = 0.08f),
                                    start = androidx.compose.ui.geometry.Offset(0f, 6.dp.toPx()),
                                    end = androidx.compose.ui.geometry.Offset(width, 6.dp.toPx()),
                                    strokeWidth = 0.8.dp.toPx()
                                )
                                drawLine(
                                    color = themeColors.displayText.copy(alpha = 0.14f),
                                    start = androidx.compose.ui.geometry.Offset(0f, height / 2f),
                                    end = androidx.compose.ui.geometry.Offset(width, height / 2f),
                                    strokeWidth = 1.dp.toPx()
                                )
                                drawLine(
                                    color = themeColors.displayText.copy(alpha = 0.08f),
                                    start = androidx.compose.ui.geometry.Offset(0f, height - 6.dp.toPx()),
                                    end = androidx.compose.ui.geometry.Offset(width, height - 6.dp.toPx()),
                                    strokeWidth = 0.8.dp.toPx()
                                )

                                if (telemetryHistory.size > 1) {
                                    val pointsCount = telemetryHistory.size
                                    val stepX = width / (pointsCount - 1)

                                    val strokePath = Path()
                                    val fillPath = Path()

                                    val getPoint = { index: Int ->
                                        val x = index * stepX
                                        val rawVal = telemetryHistory[index]
                                        val relativeY = (rawVal / maxVal) * (height / 2.35f)
                                        val y = (height / 2f) - relativeY
                                        androidx.compose.ui.geometry.Offset(x, y)
                                    }

                                    val firstPoint = getPoint(0)
                                    strokePath.moveTo(firstPoint.x, firstPoint.y)

                                    for (i in 0 until pointsCount - 1) {
                                        val p0 = getPoint(i)
                                        val p1 = getPoint(i + 1)
                                        val controlX = (p0.x + p1.x) / 2f
                                        strokePath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                                    }

                                    // Build closed fillPath for smooth shadow under the line
                                    fillPath.moveTo(firstPoint.x, height)
                                    fillPath.lineTo(firstPoint.x, firstPoint.y)
                                    for (i in 0 until pointsCount - 1) {
                                        val p0 = getPoint(i)
                                        val p1 = getPoint(i + 1)
                                        val controlX = (p0.x + p1.x) / 2f
                                        fillPath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                                    }
                                    val lastPoint = getPoint(pointsCount - 1)
                                    fillPath.lineTo(lastPoint.x, height)
                                    fillPath.close()

                                    // 1. Draw smooth gradient shadow under the line
                                    drawPath(
                                        path = fillPath,
                                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(
                                                waveColor.copy(alpha = 0.28f),
                                                waveColor.copy(alpha = 0.05f),
                                                Color.Transparent
                                            ),
                                            startY = 0f,
                                            endY = height
                                        )
                                    )

                                    // 2. Draw The Single Clean Line
                                    drawPath(
                                        path = strokePath,
                                        color = waveColor,
                                        style = Stroke(
                                            width = 2.dp.toPx(),
                                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                            join = androidx.compose.ui.graphics.StrokeJoin.Round
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isBn) "প্রতি ১ সেকেন্ডে লাইভ কারেন্ট রিডিং আপডেট হয় এবং প্রতি ৩০ সেকেন্ডে ব্যাকআপ সময় পুনর্গণনা হয়"
                        else "Current updates dynamically every 1s • Backup time refreshes every 30s",
                        fontSize = 10.5.sp,
                        color = themeColors.displayText.copy(alpha = 0.45f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Power Metrics Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .themeCardShadow(themeColors, shape = RoundedCornerShape(32.dp)),
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                shape = RoundedCornerShape(32.dp)
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

                    // Row 0: Estimated Time Left
                    MetricRow(
                        icon = timeEstimation.fourth,
                        title = timeEstimation.first,
                        value = timeEstimation.second,
                        valueColor = if (isCurrentlyCharging) Color(0xFF00B0FF) else themeColors.buttonEqualBg,
                        themeColors = themeColors
                    )

                    // Row 1: Charging Speed Power
                    MetricRow(
                        icon = Icons.Default.ElectricBolt,
                        title = if (isBn) "চার্জিং শক্তি (Power)" else "Active Power Speed",
                        value = String.format(Locale.US, "%.2f Watts", powerWatts),
                        themeColors = themeColors
                    )
                    
                    // Row 1.5: Charging Speed Category
                    MetricRow(
                        icon = Icons.Default.Speed,
                        title = if (isBn) "চার্জিং গতি" else "Charging Speed",
                        value = chargeSpeedText,
                        valueColor = chargeSpeedColor,
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
