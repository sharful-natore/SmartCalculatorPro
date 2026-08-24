package com.example.ui.screens.tools

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.Process
import android.os.StatFs
import android.os.SystemClock
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.theme.themeCardShadow
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

data class DeviceStorageData(
    val totalGb: Double,
    val usedGb: Double,
    val freeGb: Double,
    val usedPct: Int
)

data class DeviceDisplayData(
    val resWidth: Int,
    val resHeight: Int,
    val screenDpi: Int,
    val screenDensity: Float,
    val sizeInches: Double,
    val fps: Int
)

data class DeviceBatteryData(
    val percentage: Int,
    val tempC: Float,
    val voltageMv: Int,
    val technology: String,
    val powerSource: String,
    val healthText: String,
    val isCharging: Boolean,
    val statusText: String
)

@Composable
fun DeviceInfoTool(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedTab by remember { mutableIntStateOf(0) } 
    // 0: Overview, 1: Battery & Power, 2: CPU & SoC, 3: RAM & Storage, 4: Display, 5: Sensors

    // --- Device Information Extraction ---
    val activityManager = remember { context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager }
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val windowManager = remember { context.getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    val packageManager = remember { context.packageManager }

    // Memory Info
    val memInfo = remember {
        val mi = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(mi)
        mi
    }

    // Storage Info
    val storageInfo: DeviceStorageData = remember {
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val totalBytes = stat.totalBytes
            val availableBytes = stat.availableBytes
            val usedBytes = totalBytes - availableBytes
            val total = totalBytes / (1024.0 * 1024.0 * 1024.0)
            val used = usedBytes / (1024.0 * 1024.0 * 1024.0)
            val free = availableBytes / (1024.0 * 1024.0 * 1024.0)
            val pct = if (totalBytes > 0) (usedBytes.toDouble() / totalBytes * 100).toInt() else 0
            DeviceStorageData(totalGb = total, usedGb = used, freeGb = free, usedPct = pct)
        } catch (e: Exception) {
            DeviceStorageData(totalGb = 0.0, usedGb = 0.0, freeGb = 0.0, usedPct = 0)
        }
    }

    // Display Info
    val displayInfo: DeviceDisplayData = remember {
        val dm = context.resources.displayMetrics
        val width = dm.widthPixels
        val height = dm.heightPixels
        val dpi = dm.densityDpi
        val xdpi = dm.xdpi
        val ydpi = dm.ydpi
        val density = dm.density

        val screenInches = if (xdpi > 0 && ydpi > 0) {
            val wIn = width / xdpi
            val hIn = height / ydpi
            sqrt(wIn.toDouble().pow(2.0) + hIn.toDouble().pow(2.0))
        } else 6.5

        val refreshRate = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display?.refreshRate?.toInt() ?: 60
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay?.refreshRate?.toInt() ?: 60
            }
        } catch (e: Exception) { 60 }

        DeviceDisplayData(
            resWidth = width,
            resHeight = height,
            screenDpi = dpi,
            screenDensity = density,
            sizeInches = screenInches,
            fps = refreshRate
        )
    }

    // Battery Info
    val batteryInfo: DeviceBatteryData = remember {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryIntent = context.registerReceiver(null, ifilter)
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val temp = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val voltage = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val health = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN) ?: 0
        val tech = batteryIntent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"
        val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1

        val pluggedStr = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> if (isBn) "এসি অ্যাডাপ্টার চার্জার" else "AC Wall Charger"
            BatteryManager.BATTERY_PLUGGED_USB -> if (isBn) "ইউএসবি পোর্ট" else "USB Cable"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> if (isBn) "ওয়্যারলেস ডক" else "Wireless Charger"
            else -> if (isBn) "ব্যাটারি পাওয়ার (ডিসচার্জিং)" else "Not Charging (On Battery)"
        }

        val healthStr = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> if (isBn) "চমৎকার (Good)" else "Good / Healthy"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> if (isBn) "অতিরিক্ত গরম (Overheat)" else "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> if (isBn) "ডেড (Dead)" else "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> if (isBn) "ওভার ভোল্টেজ" else "Over Voltage"
            else -> if (isBn) "স্বাভাবিক (Normal)" else "Normal"
        }

        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val statusStr = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> if (isBn) "চার্জ হচ্ছে (Charging)" else "Charging"
            BatteryManager.BATTERY_STATUS_FULL -> if (isBn) "সম্পূর্ণ চার্জড (100% Full)" else "Fully Charged"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> if (isBn) "ডিসচার্জিং (ব্যবহৃত হচ্ছে)" else "Discharging"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> if (isBn) "চার্জ হচ্ছে না" else "Not Charging"
            else -> if (isBn) "অজানা" else "Unknown"
        }

        val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else 0

        DeviceBatteryData(
            percentage = pct,
            tempC = temp / 10f,
            voltageMv = voltage,
            technology = tech,
            powerSource = pluggedStr,
            healthText = healthStr,
            isCharging = isCharging,
            statusText = statusStr
        )
    }

    // Hardware Features Check
    val hasNfc = remember { packageManager.hasSystemFeature(PackageManager.FEATURE_NFC) }
    val hasBluetooth = remember { packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) }
    val hasFingerprint = remember { packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) }

    // Installed Sensors List
    val sensorList = remember { sensorManager.getSensorList(Sensor.TYPE_ALL) }

    // Formatted System Uptime
    val uptimeHours = SystemClock.elapsedRealtime() / (1000 * 60 * 60)
    val uptimeMinutes = (SystemClock.elapsedRealtime() / (1000 * 60)) % 60

    // Full Specs Report Generator
    val fullSpecReport = remember {
        """
        📱 DEVICE & SYSTEM SPECIFICATIONS
        ---------------------------------
        • Model: ${Build.MODEL} (${Build.PRODUCT})
        • Manufacturer: ${Build.MANUFACTURER}
        • Brand: ${Build.BRAND}
        • Board / Hardware: ${Build.BOARD} / ${Build.HARDWARE}
        • Android Version: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
        • Security Patch: ${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Build.VERSION.SECURITY_PATCH else "N/A"}
        • Build ID: ${Build.ID}
        • Kernel: ${System.getProperty("os.name")} ${System.getProperty("os.version")} (${System.getProperty("os.arch")})
        • Uptime: $uptimeHours hrs $uptimeMinutes mins

        🔋 BATTERY & POWER INFORMATION
        ------------------------------
        • Battery Level: ${batteryInfo.percentage}%
        • Status: ${batteryInfo.statusText}
        • Health: ${batteryInfo.healthText}
        • Temperature: ${batteryInfo.tempC} °C (${String.format(Locale.US, "%.1f", batteryInfo.tempC * 9/5 + 32)} °F)
        • Voltage: ${batteryInfo.voltageMv} mV (${String.format(Locale.US, "%.2f", batteryInfo.voltageMv / 1000.0)} V)
        • Power Source: ${batteryInfo.powerSource}
        • Technology: ${batteryInfo.technology}

        ⚙️ CPU & PROCESSOR
        ------------------
        • Processor Cores: ${Runtime.getRuntime().availableProcessors()} Cores
        • Architecture (ABIs): ${Build.SUPPORTED_ABIS.joinToString(", ")}
        • 64-Bit Mode: ${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Process.is64Bit() else true}
        • Java VM: ${System.getProperty("java.vm.name")} ${System.getProperty("java.vm.version")}

        💾 MEMORY & STORAGE
        -------------------
        • Total RAM: ${String.format(Locale.US, "%.2f GB", memInfo.totalMem / (1024.0 * 1024.0 * 1024.0))}
        • Available RAM: ${String.format(Locale.US, "%.2f GB", memInfo.availMem / (1024.0 * 1024.0 * 1024.0))}
        • Internal ROM: ${String.format(Locale.US, "%.1f GB", storageInfo.totalGb)} Total (${String.format(Locale.US, "%.1f GB", storageInfo.freeGb)} Free)

        🖥️ DISPLAY & SCREEN
        -------------------
        • Resolution: ${displayInfo.resWidth} x ${displayInfo.resHeight} px
        • Refresh Rate: ${displayInfo.fps} Hz
        • Density: ${displayInfo.screenDpi} DPI (${displayInfo.screenDensity}x)
        • Approx Size: ${String.format(Locale.US, "%.1f\"", displayInfo.sizeInches)}

        📡 CONNECTIVITY & SENSORS
        -------------------------
        • Bluetooth: $hasBluetooth | NFC: $hasNfc | Biometric: $hasFingerprint
        • Hardware Sensors: ${sensorList.size} sensors installed
        """.trimIndent()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Hero Card ---
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(themeColors.buttonEqualBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                            Text(
                                text = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                                fontSize = 13.sp,
                                color = themeColors.buttonEqualBg,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Share / Copy Report Button
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(fullSpecReport))
                            Toast.makeText(
                                context,
                                if (isBn) "সম্পূর্ণ ডিভাইস রিপোর্ট কপি করা হয়েছে!" else "Full specs report copied to clipboard!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColors.buttonEqualBg,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Specs",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "কপি রিপোর্ট" else "Copy",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // --- Category Tabs Navigation (Horizontal Scroll) ---
        val tabs = listOf(
            Pair(if (isBn) "ওভারভিউ" else "Overview", Icons.Default.Info),
            Pair(if (isBn) "ব্যাটারি ও চার্জিং" else "Battery & Power", Icons.Default.BatteryChargingFull),
            Pair(if (isBn) "সিপিইউ ও SoC" else "CPU / SoC", Icons.Default.Memory),
            Pair(if (isBn) "র‍্যাম ও রম" else "RAM & Storage", Icons.Default.Storage),
            Pair(if (isBn) "ডিসপ্লে" else "Display", Icons.Default.Tv),
            Pair(if (isBn) "সেন্সরস (${sensorList.size})" else "Sensors (${sensorList.size})", Icons.Default.Sensors)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, pair ->
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
        // --- TAB 0: SYSTEM OVERVIEW ---
        // ==========================================
        if (selectedTab == 0) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // 1. Prominent Battery Quick Card
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (batteryInfo.percentage > 20) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (batteryInfo.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                                        contentDescription = null,
                                        tint = if (batteryInfo.percentage > 20) Color(0xFF10B981) else Color(0xFFEF4444),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isBn) "ব্যাটারি স্ট্যাটাস" else "Battery Status",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.displayText
                                    )
                                    Text(
                                        text = batteryInfo.powerSource,
                                        fontSize = 11.sp,
                                        color = themeColors.displayText.copy(alpha = 0.65f)
                                    )
                                }
                            }

                            // Percentage Pill
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (batteryInfo.percentage > 20) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${batteryInfo.percentage}%",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (batteryInfo.percentage > 20) Color(0xFF10B981) else Color(0xFFEF4444),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { (batteryInfo.percentage / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (batteryInfo.percentage > 20) Color(0xFF10B981) else Color(0xFFEF4444),
                            trackColor = themeColors.background
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🌡️ ${batteryInfo.tempC} °C",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = themeColors.displayText
                            )
                            Text(
                                text = "⚡ ${batteryInfo.voltageMv} mV",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = themeColors.displayText
                            )
                            Text(
                                text = "❤️ ${batteryInfo.healthText}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }

                // 2. System Specs Card
                SpecSectionCard(
                    title = if (isBn) "ডিভাইস ও ওএস স্পেসিফিকেশন" else "Device & OS Specifications",
                    themeColors = themeColors,
                    items = listOf(
                        SpecRowItem(if (isBn) "ডিভাইস মডেল" else "Device Model", Build.MODEL),
                        SpecRowItem(if (isBn) "ম্যানুফ্যাকচারার" else "Manufacturer", Build.MANUFACTURER.replaceFirstChar { it.uppercase() }),
                        SpecRowItem(if (isBn) "ব্র্যান্ড ও প্রোডাক্ট" else "Brand & Product", "${Build.BRAND} (${Build.PRODUCT})"),
                        SpecRowItem(if (isBn) "মাদারবোর্ড / বোর্ড" else "Motherboard / Board", Build.BOARD),
                        SpecRowItem(if (isBn) "হার্ডওয়্যার" else "Hardware", Build.HARDWARE),
                        SpecRowItem(if (isBn) "অ্যান্ড্রয়েড ভার্সন" else "Android Version", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"),
                        SpecRowItem(if (isBn) "সিকিউরিটি প্যাচ" else "Security Patch Level", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) (Build.VERSION.SECURITY_PATCH ?: "N/A") else "N/A"),
                        SpecRowItem(if (isBn) "বিল্ড নম্বর (ID)" else "Build ID", Build.ID),
                        SpecRowItem(if (isBn) "কার্নেল আর্কিটেকচার" else "Kernel & OS Architecture", "${System.getProperty("os.name")} ${System.getProperty("os.arch")}"),
                        SpecRowItem(if (isBn) "সিস্টেম আপটাইম" else "System Uptime", "$uptimeHours hrs $uptimeMinutes mins")
                    )
                )
            }
        }

        // ==========================================
        // --- TAB 1: BATTERY & POWER DASHBOARD ---
        // ==========================================
        if (selectedTab == 1) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Large Battery Visual Gauge Card
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
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(
                                    if (batteryInfo.percentage > 20)
                                        Brush.radialGradient(listOf(Color(0xFF10B981).copy(alpha = 0.2f), Color(0xFF10B981).copy(alpha = 0.05f)))
                                    else
                                        Brush.radialGradient(listOf(Color(0xFFEF4444).copy(alpha = 0.2f), Color(0xFFEF4444).copy(alpha = 0.05f)))
                                )
                                .border(
                                    width = 3.dp,
                                    color = if (batteryInfo.percentage > 20) Color(0xFF10B981) else Color(0xFFEF4444),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${batteryInfo.percentage}%",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (batteryInfo.percentage > 20) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                                if (batteryInfo.isCharging) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = batteryInfo.statusText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )

                        Text(
                            text = batteryInfo.powerSource,
                            fontSize = 12.sp,
                            color = themeColors.displayText.copy(alpha = 0.65f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Battery Detailed Parameters Card
                SpecSectionCard(
                    title = if (isBn) "ব্যাটারি ও চার্জিং ডিটেইলস" else "Battery & Charging Telemetry",
                    themeColors = themeColors,
                    items = listOf(
                        SpecRowItem(if (isBn) "ব্যাটারি স্তর (Charge Level)" else "Battery Level", "${batteryInfo.percentage}%"),
                        SpecRowItem(if (isBn) "চার্জিং অবস্থা" else "Charging State", batteryInfo.statusText),
                        SpecRowItem(if (isBn) "পাওয়ার সোর্স" else "Power Source", batteryInfo.powerSource),
                        SpecRowItem(if (isBn) "ব্যাটারি স্বাস্থ্য (Health)" else "Battery Health", batteryInfo.healthText),
                        SpecRowItem(if (isBn) "তাপমাত্রা সেলসিয়াস" else "Temperature (°C)", "${batteryInfo.tempC} °C"),
                        SpecRowItem(if (isBn) "তাপমাত্রা ফারেনহাইট" else "Temperature (°F)", String.format(Locale.US, "%.1f °F", batteryInfo.tempC * 9/5 + 32)),
                        SpecRowItem(if (isBn) "ভোল্টেজ (Voltage)" else "Voltage", "${batteryInfo.voltageMv} mV (${String.format(Locale.US, "%.2f", batteryInfo.voltageMv / 1000.0)} V)"),
                        SpecRowItem(if (isBn) "ব্যাটারি প্রযুক্তি" else "Battery Technology", batteryInfo.technology),
                        SpecRowItem(if (isBn) "থার্মাল স্ট্যাটাস" else "Thermal Status", if (batteryInfo.tempC < 38f) (if (isBn) "স্বাভাবিক / শীতল ✓" else "Cool / Normal ✓") else (if (isBn) "উষ্ণ / হট ⚠️" else "Warm / Hot ⚠️"))
                    )
                )
            }
        }

        // ==========================================
        // --- TAB 2: CPU & SOC ---
        // ==========================================
        if (selectedTab == 2) {
            SpecSectionCard(
                title = if (isBn) "প্রসেসর ও চিপসেট স্পেক্স" else "CPU & Chipset Specifications",
                themeColors = themeColors,
                items = listOf(
                    SpecRowItem(if (isBn) "সিপিইউ কোর সংখ্যা" else "CPU Core Count", "${Runtime.getRuntime().availableProcessors()} Cores (${when (Runtime.getRuntime().availableProcessors()) { 8 -> "Octa-Core"; 6 -> "Hexa-Core"; 4 -> "Quad-Core"; else -> "Multi-Core" }})"),
                    SpecRowItem(if (isBn) "সিপিইউ আর্কিটেকচার (ABI)" else "Primary ABI", Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"),
                    SpecRowItem(if (isBn) "সাপোর্টেড এবিআই সমূহ" else "Supported ABIs", Build.SUPPORTED_ABIS.joinToString(", ")),
                    SpecRowItem(if (isBn) "৬৪-বিট আর্কিটেকচার" else "64-Bit Architecture", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) (if (Process.is64Bit()) "Yes (64-Bit)" else "32-Bit") else "Yes"),
                    SpecRowItem(if (isBn) "হার্ডওয়্যার প্ল্যাটফর্ম" else "Hardware SoC", Build.HARDWARE),
                    SpecRowItem(if (isBn) "মাদারবোর্ড বোর্ড" else "Board Name", Build.BOARD),
                    SpecRowItem(if (isBn) "জাভা ভিএম (Runtime)" else "Java VM Runtime", "${System.getProperty("java.vm.name")} ${System.getProperty("java.vm.version")}"),
                    SpecRowItem(if (isBn) "ম্যাক্সিমাম মেমোরি হিপ" else "Max Heap Allocation", "${Runtime.getRuntime().maxMemory() / (1024 * 1024)} MB")
                )
            )
        }

        // ==========================================
        // --- TAB 3: RAM & STORAGE ---
        // ==========================================
        if (selectedTab == 3) {
            val totalRamGb = memInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
            val availRamGb = memInfo.availMem / (1024.0 * 1024.0 * 1024.0)
            val usedRamGb = totalRamGb - availRamGb
            val ramPct = if (totalRamGb > 0) ((usedRamGb / totalRamGb) * 100).toInt() else 0

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // RAM Card
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBn) "র‍্যাম মেমোরি (RAM)" else "RAM Memory",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                            Text(
                                text = "$ramPct% Used",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.buttonEqualBg
                            )
                        }

                        LinearProgressIndicator(
                            progress = { (ramPct / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = themeColors.buttonEqualBg,
                            trackColor = themeColors.background
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = String.format(Locale.US, "Used: %.2f GB", usedRamGb),
                                fontSize = 12.sp,
                                color = themeColors.displayText.copy(alpha = 0.7f)
                            )
                            Text(
                                text = String.format(Locale.US, "Total: %.2f GB", totalRamGb),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                        }
                    }
                }

                // Storage Card
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBn) "ইন্টারনাল স্টোরেজ (ROM)" else "Internal Storage (ROM)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                            Text(
                                text = "${storageInfo.usedPct}% Used",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                        }

                        LinearProgressIndicator(
                            progress = { (storageInfo.usedPct / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFFF59E0B),
                            trackColor = themeColors.background
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = String.format(Locale.US, "Free: %.1f GB", storageInfo.freeGb),
                                fontSize = 12.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = String.format(Locale.US, "Total: %.1f GB", storageInfo.totalGb),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // --- TAB 4: DISPLAY ---
        // ==========================================
        if (selectedTab == 4) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SpecSectionCard(
                    title = if (isBn) "ডিসপ্লে স্পেসিফিকেশন" else "Display Specifications",
                    themeColors = themeColors,
                    items = listOf(
                        SpecRowItem(if (isBn) "স্ক্রিন রেজোলিউশন" else "Screen Resolution", "${displayInfo.resWidth} x ${displayInfo.resHeight} px"),
                        SpecRowItem(if (isBn) "রিফ্রেশ রেট" else "Screen Refresh Rate", "${displayInfo.fps} Hz"),
                        SpecRowItem(if (isBn) "স্ক্রিন ডেনসিটি" else "Screen Density", "${displayInfo.screenDpi} DPI (${displayInfo.screenDensity}x scale)"),
                        SpecRowItem(if (isBn) "আনুমানিক সাইজ" else "Screen Size", String.format(Locale.US, "%.2f Inches", displayInfo.sizeInches))
                    )
                )

                SpecSectionCard(
                    title = if (isBn) "কানেক্টিভিটি ও সেন্সর সাপোর্ট" else "Connectivity Features",
                    themeColors = themeColors,
                    items = listOf(
                        SpecRowItem("Bluetooth", if (hasBluetooth) "Supported ✓" else "No"),
                        SpecRowItem("NFC Support", if (hasNfc) "Supported ✓" else "No"),
                        SpecRowItem("Biometric / Fingerprint", if (hasFingerprint) "Supported ✓" else "No"),
                        SpecRowItem(if (isBn) "মোট সেন্সর সংখ্যা" else "Installed Sensors", "${sensorList.size} Hardware Sensors")
                    )
                )
            }
        }

        // ==========================================
        // --- TAB 5: SENSORS DIRECTORY ---
        // ==========================================
        if (selectedTab == 5) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isBn) "ডিভাইসে মোট ${sensorList.size} টি হার্ডওয়্যার সেন্সর ইনস্টল করা আছে:" else "Total ${sensorList.size} hardware sensors registered:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )

                sensorList.forEachIndexed { index, sensor ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .themeCardShadow(themeColors, elevation = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}. ${sensor.name}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = themeColors.buttonEqualBg.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "Type ${sensor.type}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.buttonEqualBg,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Vendor: ${sensor.vendor}  •  Power: ${sensor.power} mA",
                                fontSize = 11.sp,
                                color = themeColors.displayText.copy(alpha = 0.65f)
                            )
                            Text(
                                text = "Resolution: ${sensor.resolution}  •  Max Range: ${sensor.maximumRange}",
                                fontSize = 11.sp,
                                color = themeColors.displayText.copy(alpha = 0.65f)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class SpecRowItem(val title: String, val value: String)

@Composable
private fun SpecSectionCard(
    title: String,
    themeColors: CalculatorThemeColors,
    items: List<SpecRowItem>
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )

            Divider(color = themeColors.displayText.copy(alpha = 0.08f), thickness = 1.dp)

            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.65f),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = item.value,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColors.displayText,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.3f)
                    )
                }
            }
        }
    }
}
