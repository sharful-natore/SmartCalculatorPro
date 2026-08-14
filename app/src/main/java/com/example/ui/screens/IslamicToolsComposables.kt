package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
    var phoneAngle by remember { mutableFloatStateOf(0f) }
    var isHardwareSensorActive by remember { mutableStateOf(false) }
    val qiblaBearing = 237.5f // Qibla angle from Bangladesh/Makkah

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
                    phoneAngle = (event.values[0] + 360f) % 360f
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
                        phoneAngle = azimuthInDegrees
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
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

    val angleDiff = Math.abs((phoneAngle - qiblaBearing + 360f) % 360f)
    val isAligned = angleDiff < 8f || angleDiff > 352f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isBn) "কিবলা কিবলা নির্দেশক" else "Qibla Direction Finder",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )
            Text(
                text = if (isBn) "মক্কা শরীফ (আল-কাবা) দিক: ২৩৭.৫° দক্ষিণ-পশ্চিম" else "Makkah (Al-Kaaba) Bearing: 237.5° WSW",
                fontSize = 12.sp,
                color = themeColors.displayText.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            if (isHardwareSensorActive) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isBn) "লাইভ কমপাস সেন্সর সক্রিয় 🧭" else "Live Compass Sensor Active 🧭",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Compass Circle Dial
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .clip(CircleShape)
                    .background(themeColors.background)
                    .border(
                        width = 4.dp,
                        color = if (isAligned) Color(0xFF10B981) else themeColors.buttonEqualBg.copy(alpha = 0.4f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Dial markings (rotates opposite to phone angle)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(-phoneAngle),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("N", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 14.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("W", fontWeight = FontWeight.Bold, color = themeColors.displayText, fontSize = 14.sp)
                            Text("E", fontWeight = FontWeight.Bold, color = themeColors.displayText, fontSize = 14.sp)
                        }
                        Text("S", fontWeight = FontWeight.Bold, color = themeColors.displayText, fontSize = 14.sp)
                    }
                }

                // Needle Pointer (Points to Qibla: qiblaBearing - phoneAngle)
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .rotate(-phoneAngle + qiblaBearing),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        // Green Kaaba Arrow
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Qibla Pointer",
                            tint = if (isAligned) Color(0xFF10B981) else Color(0xFF059669),
                            modifier = Modifier.size(42.dp)
                        )
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .weight(1f)
                                .background(if (isAligned) Color(0xFF10B981) else Color(0xFF059669))
                        )
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .weight(1f)
                                .background(themeColors.displayText.copy(alpha = 0.2f))
                        )
                    }
                }

                // Kaaba Icon in center
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isAligned) Color(0xFF10B981) else themeColors.buttonEqualBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Kaaba",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Alignment Status Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(if (isAligned) Color(0xFF10B981).copy(alpha = 0.18f) else themeColors.displayText.copy(alpha = 0.08f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (isAligned) {
                        if (isBn) "যথাযথ কিবলা অভিমুখে রয়েছেন! (কাবা শরীফ)" else "Perfectly Aligned with Qibla!"
                    } else {
                        if (isBn) "কমপাস ঘুরিয়ে কিবলা মিলিয়ে নিন" else "Rotate phone to align with Qibla"
                    },
                    fontWeight = FontWeight.Bold,
                    color = if (isAligned) Color(0xFF10B981) else themeColors.displayText,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Auto Align Button + Manual Adjustment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { phoneAngle = qiblaBearing },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
                ) {
                    Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBn) "অটো কিবলা এলাইন" else "Auto Align",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = { phoneAngle = (phoneAngle + 45f) % 360f },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBn) "ঘুরান (+৪৫°)" else "Rotate (+45°)",
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isBn) "কমপাস রোটেশন এঙ্গেল: ${phoneAngle.toInt()}°" else "Compass Rotation Angle: ${phoneAngle.toInt()}°",
                fontSize = 12.sp,
                color = themeColors.displayText.copy(alpha = 0.7f)
            )
            Slider(
                value = phoneAngle,
                onValueChange = { phoneAngle = it },
                valueRange = 0f..360f,
                colors = SliderDefaults.colors(thumbColor = themeColors.buttonEqualBg, activeTrackColor = themeColors.buttonEqualBg)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isBn) "দূরত্ব: ৫,১৪৮ কি.মি. (মাক্কাহ)" else "Distance: 5,148 km (Makkah)",
                    fontSize = 11.sp,
                    color = themeColors.displayText.copy(alpha = 0.6f)
                )
                Text(
                    text = if (isBn) "কিবলা কোন: ২৩৭.৫° WSW" else "Qibla Angle: 237.5° WSW",
                    fontSize = 11.sp,
                    color = themeColors.displayText.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// --- 2. DIGITAL TASBIH CARD ---
@Composable
fun DigitalTasbihCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    var count by remember { mutableIntStateOf(0) }
    var targetCount by remember { mutableIntStateOf(33) }
    var totalCount by remember { mutableIntStateOf(0) }
    var selectedZikirIndex by remember { mutableIntStateOf(0) }

    val zikirs = remember {
        listOf(
            Pair("سُبْحَانَ اللَّهِ", if (isBn) "সুবহানাল্লাহ (আল্লাহ পবিত্র)" else "SubhanAllah"),
            Pair("الْحَمْدُ لِلَّهِ", if (isBn) "আলহামদুলিল্লাহ (সকল প্রশংসা আল্লাহর)" else "Alhamdulillah"),
            Pair("اللَّهُ أَكْبَرُ", if (isBn) "আল্লাহু আকবার (আল্লাহ সবচেয়ে মহান)" else "Allahu Akbar"),
            Pair("أَسْتَغْفِرُ اللَّهَ", if (isBn) "আস্তাগফিরুল্লাহ (আল্লাহর কাছে ক্ষমা প্রার্থনা)" else "Astagfirullah"),
            Pair("لَا إِلٰهَ إِلَّا اللَّهُ", if (isBn) "লা ইলাহা ইল্লাল্লাহ (আল্লাহ ছাড়া উপাস্য নেই)" else "La ilaha illallah"),
            Pair("صَلَّى اللّٰهُ عَلَيْهِ وَسَلَّمَ", if (isBn) "দরুদ শরীফ" else "Darood Sharif")
        )
    }

    val activeZikir = zikirs.getOrElse(selectedZikirIndex) { zikirs[0] }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isBn) "ডিজিটাল জিকির ও তাসবিহ" else "Digital Tasbih Counter",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Active Zikir Display Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.background)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = activeZikir.first,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.buttonEqualBg,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = activeZikir.second,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = themeColors.displayText.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Zikir Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                zikirs.take(3).forEachIndexed { idx, item ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedZikirIndex == idx) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.08f))
                            .clickable {
                                selectedZikirIndex = idx
                                count = 0
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.second.split(" ")[0],
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedZikirIndex == idx) Color.White else themeColors.displayText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Target Limit Selector (33, 100, 1000)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isBn) "টার্গেট:" else "Target:",
                    fontSize = 12.sp,
                    color = themeColors.displayText.copy(alpha = 0.7f)
                )
                listOf(33, 100, 1000).forEach { limit ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (targetCount == limit) themeColors.buttonEqualBg.copy(alpha = 0.2f) else themeColors.displayText.copy(alpha = 0.06f))
                            .border(
                                width = 1.dp,
                                color = if (targetCount == limit) themeColors.buttonEqualBg else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { targetCount = limit }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$limit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (targetCount == limit) themeColors.buttonEqualBg else themeColors.displayText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Large Circular Count Tap Button
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .clip(CircleShape)
                    .background(themeColors.buttonEqualBg)
                    .clickable {
                        count++
                        totalCount++
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$count",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = if (isBn) "ট্যাপ করুন" else "TAP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Milestone Banner
            if (count > 0 && count % targetCount == 0) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.15f))
                ) {
                    Text(
                        text = if (isBn) "মাশাআল্লাহ! $targetCount বার জিকির সম্পন্ন হয়েছে 🎉" else "MashaAllah! Completed $targetCount times 🎉",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBn) "মোট পাঠ: $totalCount" else "Total Count: $totalCount",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText.copy(alpha = 0.8f)
                )

                OutlinedButton(
                    onClick = { count = 0 },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isBn) "রিসেট" else "Reset", fontSize = 12.sp)
                }
            }
        }
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
                        viewModel.selectedIslamicDistrictBn = district.nameBn
                        viewModel.selectedIslamicDistrictEn = district.nameEn
                        viewModel.selectedIslamicDistrictOffsetMinutes = district.offsetMinutes
                        expanded = false
                    }
                )
            }
        }
    }
}

// --- 3. PRAYER TIMES CARD ---
@Composable
fun PrayerTimesCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    val offset = viewModel.selectedIslamicDistrictOffsetMinutes

    val rawPrayerList = listOf(
        Triple(if (isBn) "ফজর (Fajr)" else "Fajr", "04:52 AM", true),
        Triple(if (isBn) "সূর্যোদয় (Sunrise)" else "Sunrise", "06:08 AM", false),
        Triple(if (isBn) "যোহর (Dhuhr)" else "Dhuhr", "12:15 PM", false),
        Triple(if (isBn) "আসর (Asr)" else "Asr", "04:35 PM", false),
        Triple(if (isBn) "মাগরিব (Maghrib)" else "Maghrib", "06:18 PM", false),
        Triple(if (isBn) "এশা (Isha)" else "Isha", "07:35 PM", false)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg)
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
                Column {
                    Text(
                        text = if (isBn) "নামাজের সময়সূচি" else "Daily Prayer Times",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    Text(
                        text = if (isBn) "${viewModel.selectedIslamicDistrictBn.split(" ")[0]} ও পার্শ্ববর্তী এলাকা" else "${viewModel.selectedIslamicDistrictEn} & Surrounding Areas",
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.7f)
                    )
                }

                DistrictSelectorDropdown(viewModel, themeColors)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Prayer Highlight Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBn) "পরবর্তী সালাত: যোহর (Dhuhr)" else "Next Prayer: Dhuhr",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isBn) "সময় সমন্বয়: ${if (offset >= 0) "+$offset" else "$offset"} মি." else "District Offset: ${if (offset >= 0) "+$offset" else "$offset"} mins",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Text(
                        text = adjustIslamicTime("12:15 PM", offset),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5 Waqt Timetable List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rawPrayerList.forEach { (name, baseTime, isNext) ->
                    val adjustedTime = adjustIslamicTime(baseTime, offset)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isNext) themeColors.buttonEqualBg.copy(alpha = 0.12f) else themeColors.background)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (name.contains("সূর্যোদয়") || name.contains("Sunrise")) Icons.Default.WbSunny else Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (isNext) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = name,
                                fontSize = 14.sp,
                                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                                color = if (isNext) themeColors.buttonEqualBg else themeColors.displayText
                            )
                        }

                        Text(
                            text = adjustedTime,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNext) themeColors.buttonEqualBg else themeColors.displayText
                        )
                    }
                }
            }
        }
    }
}

// --- 4. SEHRI & IFTAR CARD ---
@Composable
fun SehriIftarCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    val offset = viewModel.selectedIslamicDistrictOffsetMinutes

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg)
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
                Column {
                    Text(
                        text = if (isBn) "সেহরি ও ইফতারের সময়সূচি" else "Sehri & Iftar Schedule",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    Text(
                        text = if (isBn) "${viewModel.selectedIslamicDistrictBn.split(" ")[0]} জোন সময়সূচি" else "${viewModel.selectedIslamicDistrictEn} Zone Schedule",
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                DistrictSelectorDropdown(viewModel, themeColors)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sehri Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.NightsStay, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBn) "সেহরির শেষ সময়" else "Sehri Ends",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = adjustIslamicTime("04:46 AM", offset),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Iftar Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBn) "ইফতারের সময়" else "Iftar Time",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = adjustIslamicTime("06:18 PM", offset),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Sehri & Iftar Duas
            Text(
                text = if (isBn) "সেহরি ও ইফতারের দোয়া" else "Sehri & Iftar Duas",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.background)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isBn) "১. রোজার নিয়ত (Sehri Niyyat):" else "1. Fasting Intention (Sehri):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.buttonEqualBg
                    )
                    Text(
                        text = "نَوَيْتُ أَنْ أَصُومَ غَدًا مِنْ شَهْرِ رَمَضَانَ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    )
                    Text(
                        text = if (isBn) "উচ্চারণ: নাওয়াইতু আন আসুমা গাদাম মিন শাহরি রমাদান।" else "Pronunciation: Nawaitu an asuma gadam min shahri ramadan.",
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.8f)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = themeColors.displayText.copy(alpha = 0.1f))

                    Text(
                        text = if (isBn) "২. ইফতারের দোয়া (Iftar Dua):" else "2. Iftar Dua:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.buttonEqualBg
                    )
                    Text(
                        text = "اللَّهُمَّ إِنِّي لَكَ صُمْتُ وَبِكَ آمَنْتُ وَعَلَى رِزْقِكَ أَفْطَرْتُ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    )
                    Text(
                        text = if (isBn) "উচ্চারণ: আল্লাহুম্মা ইন্নি লাকা সুমতু ওয়া বিকার আমানতু ওয়া 'আলা রিযকিকা আফতারতু।" else "Pronunciation: Allahumma inni laka sumtu wa bika amantu wa 'ala rizqika aftartu.",
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// --- 5. ISLAMIC DUAS CARD ---
@Composable
fun IslamicDuasCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

    val duas = remember {
        listOf(
            Triple(
                if (isBn) "ঘুম থেকে ওঠার দোয়া" else "Dua after waking up",
                "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
                if (isBn) "উচ্চারণ: আলহামদুলিল্লাহিল্লাজি আহইয়ানা বা'দা মা અમાতানা ওয়া ইলাইহিন নুশূর।\nঅর্থ: সকল প্রশংসা আল্লাহর জন্য, যিনি মৃত্যুর (ঘুমের) পর আমাদের জীবন দান করলেন।" else "Pronunciation: Alhamdu lillahillazi ahyana ba'da ma amatana wa ilaihin nushur."
            ),
            Triple(
                if (isBn) "খাবার খাওয়ার শুরূতে দোয়া" else "Dua before eating",
                "بِسْمِ اللَّهِ وَعَلَى بَرَكَةِ اللَّهِ",
                if (isBn) "উচ্চারণ: বিসমিল্লাহি ওয়া 'আলা বারাকাতিল্লাহ।\nঅর্থ: আল্লাহর নামে এবং আল্লাহর বরকতের উপর শুরু করছি।" else "Pronunciation: Bismillahi wa 'ala barakatillah."
            ),
            Triple(
                if (isBn) "ঘর থেকে বের হওয়ার দোয়া" else "Dua when leaving home",
                "بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
                if (isBn) "উচ্চারণ: বিসমিল্লাহি তাওয়াক্কালতু 'আলাল্লাহ, ওয়া লা হাওলা ওয়া লা ক্বুওয়াতা ইল্লা বিল্লাহ।" else "Pronunciation: Bismillahi tawakkaltu 'alallah, wa la hawla wa la quwwata illa billah."
            ),
            Triple(
                if (isBn) "ক্ষমা ও তাওবার দোয়া (সায়্যিদুল এস্তেগফার)" else "Sayyidul Istighfar",
                "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلٰهَ إِلَّا أَنْتَ خَلَقْتَنِي وَأَنَا عَبْدُكَ",
                if (isBn) "উচ্চারণ: আল্লাহুম্মা আন্তা রাব্বি লা ইলাহা ইল্লা আন্তা খালাক্বতানি ওয়া আনা 'আবদুকা..." else "Pronunciation: Allahumma anta rabbi la ilaha illa anta khalaqtani wa ana 'abduka..."
            )
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = if (isBn) "দৈনন্দিন প্রয়োজনীয় দোয়াসমূহ" else "Daily Essential Duas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )
            Text(
                text = if (isBn) "কুরআন ও হাদিসের গুরুত্বপূর্ণ মাসনুন দোয়া" else "Authentic Quranic & Hadith Duas",
                fontSize = 12.sp,
                color = themeColors.displayText.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                duas.forEach { (title, arabic, meaning) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = themeColors.background)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.buttonEqualBg
                                )
                            }
                            Text(
                                text = arabic,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                            )
                            Text(
                                text = meaning,
                                fontSize = 12.sp,
                                color = themeColors.displayText.copy(alpha = 0.8f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
