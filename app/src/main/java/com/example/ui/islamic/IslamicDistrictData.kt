package com.example.ui.islamic

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class BdDivision(val nameBn: String, val nameEn: String) {
    ALL("সকল বিভাগ", "All Divisions"),
    DHAKA("ঢাকা", "Dhaka"),
    CHITTAGONG("চট্টগ্রাম", "Chittagong"),
    RAJSHAHI("রাজশাহী", "Rajshahi"),
    KHULNA("খুলনা", "Khulna"),
    BARISAL("বরিশাল", "Barisal"),
    SYLHET("সিলেট", "Sylhet"),
    RANGPUR("রংপুর", "Rangpur"),
    MYMENSINGH("ময়মনসিংহ", "Mymensingh")
}

data class BdDistrict(
    val nameBn: String,
    val nameEn: String,
    val division: BdDivision,
    val lat: Double,
    val lon: Double,
    val offsetMinutes: Int
)

val allBdDistrictsList = listOf(
    // ঢাকা বিভাগ (Dhaka Division)
    BdDistrict("ঢাকা (Dhaka)", "Dhaka", BdDivision.DHAKA, 23.8103, 90.4125, 0),
    BdDistrict("গাজীপুর (Gazipur)", "Gazipur", BdDivision.DHAKA, 23.9999, 90.4203, 0),
    BdDistrict("নারায়ণগঞ্জ (Narayanganj)", "Narayanganj", BdDivision.DHAKA, 23.6238, 90.4998, 0),
    BdDistrict("মুন্সীগঞ্জ (Munshiganj)", "Munshiganj", BdDivision.DHAKA, 23.5433, 90.5354, 0),
    BdDistrict("মানিকগঞ্জ (Manikganj)", "Manikganj", BdDivision.DHAKA, 23.8644, 89.9967, 2),
    BdDistrict("নরসিংদী (Narsingdi)", "Narsingdi", BdDivision.DHAKA, 23.9229, 90.7177, -1),
    BdDistrict("টাঙ্গাইল (Tangail)", "Tangail", BdDivision.DHAKA, 24.2513, 89.9167, 2),
    BdDistrict("ফরিপুর (Faridpur)", "Faridpur", BdDivision.DHAKA, 23.6071, 89.8429, 2),
    BdDistrict("রাজবাড়ী (Rajbari)", "Rajbari", BdDivision.DHAKA, 23.7574, 89.6444, 4),
    BdDistrict("গোপালগঞ্জ (Gopalganj)", "Gopalganj", BdDivision.DHAKA, 23.0059, 89.8266, 3),
    BdDistrict("মাদারীপুর (Madaripur)", "Madaripur", BdDivision.DHAKA, 23.1641, 90.1896, 2),
    BdDistrict("শরীয়তপুর (Shariatpur)", "Shariatpur", BdDivision.DHAKA, 23.2423, 90.3412, 1),
    BdDistrict("কিশোরগঞ্জ (Kishoreganj)", "Kishoreganj", BdDivision.DHAKA, 24.4449, 90.7766, -2),

    // চট্টগ্রাম বিভাগ (Chittagong Division)
    BdDistrict("চট্টগ্রাম (Chittagong)", "Chittagong", BdDivision.CHITTAGONG, 22.3569, 91.7832, -5),
    BdDistrict("কক্সবাজার (Cox's Bazar)", "Cox's Bazar", BdDivision.CHITTAGONG, 21.4272, 92.0058, -7),
    BdDistrict("কুমিল্লা (Comilla)", "Comilla", BdDivision.CHITTAGONG, 23.4607, 91.1809, -3),
    BdDistrict("ফেনী (Feni)", "Feni", BdDivision.CHITTAGONG, 23.0159, 91.3976, -4),
    BdDistrict("ব্রাহ্মণবাড়িয়া (Brahmanbaria)", "Brahmanbaria", BdDivision.CHITTAGONG, 23.9571, 91.1109, -3),
    BdDistrict("নোয়াখালী (Noakhali)", "Noakhali", BdDivision.CHITTAGONG, 22.8696, 91.0992, -3),
    BdDistrict("লক্ষ্মীপুর (Lakshmipur)", "Lakshmipur", BdDivision.CHITTAGONG, 22.9429, 90.8417, -2),
    BdDistrict("চাঁদপুর (Chandpur)", "Chandpur", BdDivision.CHITTAGONG, 23.2333, 90.6500, -2),
    BdDistrict("খাগড়াছড়ি (Khagrachhari)", "Khagrachhari", BdDivision.CHITTAGONG, 23.1192, 91.9841, -6),
    BdDistrict("রাঙ্গামাটি (Rangamati)", "Rangamati", BdDivision.CHITTAGONG, 22.6574, 92.1733, -6),
    BdDistrict("বান্দরবান (Bandarban)", "Bandarban", BdDivision.CHITTAGONG, 22.1953, 92.2184, -6),

    // রাজশাহী বিভাগ (Rajshahi Division)
    BdDistrict("রাজশাহী (Rajshahi)", "Rajshahi", BdDivision.RAJSHAHI, 24.3745, 88.6042, 7),
    BdDistrict("বগুড়া (Bogra)", "Bogra", BdDivision.RAJSHAHI, 24.8481, 89.3730, 5),
    BdDistrict("পাবনা (Pabna)", "Pabna", BdDivision.RAJSHAHI, 24.0063, 89.2493, 6),
    BdDistrict("সিরাজগঞ্জ (Sirajganj)", "Sirajganj", BdDivision.RAJSHAHI, 24.4534, 89.7084, 3),
    BdDistrict("নওগাঁ (Naogaon)", "Naogaon", BdDivision.RAJSHAHI, 24.7936, 88.9318, 8),
    BdDistrict("নাটোর (Natore)", "Natore", BdDivision.RAJSHAHI, 24.4102, 88.9595, 7),
    BdDistrict("চাঁপাইনবাবগঞ্জ (Chapainawabganj)", "Chapainawabganj", BdDivision.RAJSHAHI, 24.5965, 88.2753, 9),
    BdDistrict("জয়পুরহাট (Joypurhat)", "Joypurhat", BdDivision.RAJSHAHI, 25.0947, 89.0209, 6),

    // খুলনা বিভাগ (Khulna Division)
    BdDistrict("খুলনা (Khulna)", "Khulna", BdDivision.KHULNA, 22.8456, 89.5403, 5),
    BdDistrict("যশোর (Jessore)", "Jessore", BdDivision.KHULNA, 23.1664, 89.2081, 6),
    BdDistrict("কুষ্টিয়া (Kushtia)", "Kushtia", BdDivision.KHULNA, 23.9013, 89.1204, 6),
    BdDistrict("সাতক্ষীরা (Satkhira)", "Satkhira", BdDivision.KHULNA, 22.7185, 89.0705, 7),
    BdDistrict("বাগেরহাট (Bagerhat)", "Bagerhat", BdDivision.KHULNA, 22.6516, 89.7859, 4),
    BdDistrict("ঝিনাইদহ (Jhenaidah)", "Jhenaidah", BdDivision.KHULNA, 23.5450, 89.1726, 6),
    BdDistrict("চুয়াডাঙ্গা (Chuadanga)", "Chuadanga", BdDivision.KHULNA, 23.6401, 88.8504, 7),
    BdDistrict("মেহেরপুর (Meherpur)", "Meherpur", BdDivision.KHULNA, 23.7622, 88.6318, 7),
    BdDistrict("মাগুরা (Magura)", "Magura", BdDivision.KHULNA, 23.4873, 89.4199, 5),
    BdDistrict("নড়াইল (Narail)", "Narail", BdDivision.KHULNA, 23.1725, 89.5126, 5),

    // বরিশাল বিভাগ (Barisal Division)
    BdDistrict("বরিশাল (Barisal)", "Barisal", BdDivision.BARISAL, 22.7010, 90.3535, 2),
    BdDistrict("পটুয়াখালী (Patuakhali)", "Patuakhali", BdDivision.BARISAL, 22.3596, 90.3297, 2),
    BdDistrict("ভোলা (Bhola)", "Bhola", BdDivision.BARISAL, 22.6851, 90.6440, 0),
    BdDistrict("পিরোজপুর (Pirojpur)", "Pirojpur", BdDivision.BARISAL, 22.5781, 89.9699, 3),
    BdDistrict("বরগুনা (Barguna)", "Barguna", BdDivision.BARISAL, 22.1591, 90.1245, 3),
    BdDistrict("ঝালকাঠি (Jhalokati)", "Jhalokati", BdDivision.BARISAL, 22.6395, 90.1987, 2),

    // সিলেট বিভাগ (Sylhet Division)
    BdDistrict("সিলেট (Sylhet)", "Sylhet", BdDivision.SYLHET, 24.8949, 91.8687, -6),
    BdDistrict("মৌলভীবাজার (Moulvibazar)", "Moulvibazar", BdDivision.SYLHET, 24.4829, 91.7476, -7),
    BdDistrict("হবিগঞ্জ (Habiganj)", "Habiganj", BdDivision.SYLHET, 24.3749, 91.4133, -5),
    BdDistrict("সুনামগঞ্জ (Sunamganj)", "Sunamganj", BdDivision.SYLHET, 25.0658, 91.3950, -5),

    // রংপুর বিভাগ (Rangpur Division)
    BdDistrict("রংপুর (Rangpur)", "Rangpur", BdDivision.RANGPUR, 25.7439, 89.2752, 8),
    BdDistrict("দিনাজপুর (Dinajpur)", "Dinajpur", BdDivision.RANGPUR, 25.6217, 88.6354, 10),
    BdDistrict("গাইবান্ধা (Gaibandha)", "Gaibandha", BdDivision.RANGPUR, 25.3288, 89.5280, 6),
    BdDistrict("কুড়িগ্রাম (Kurigram)", "Kurigram", BdDivision.RANGPUR, 25.8054, 89.6361, 7),
    BdDistrict("লালমনিরহাট (Lalmonirhat)", "Lalmonirhat", BdDivision.RANGPUR, 25.9125, 89.4426, 8),
    BdDistrict("নীলফামারী (Nilphamari)", "Nilphamari", BdDivision.RANGPUR, 25.9317, 88.8560, 9),
    BdDistrict("পঞ্চগড় (Panchagarh)", "Panchagarh", BdDivision.RANGPUR, 26.3411, 88.5542, 12),
    BdDistrict("ঠাকুরগাঁও (Thakurgaon)", "Thakurgaon", BdDivision.RANGPUR, 26.0337, 88.4617, 11),

    // ময়মনসিংহ বিভাগ (Mymensingh Division)
    BdDistrict("ময়মনসিংহ (Mymensingh)", "Mymensingh", BdDivision.MYMENSINGH, 24.7471, 90.4203, -1),
    BdDistrict("জামালপুর (Jamalpur)", "Jamalpur", BdDivision.MYMENSINGH, 24.9197, 89.9481, 3),
    BdDistrict("শেরপুর (Sherpur)", "Sherpur", BdDivision.MYMENSINGH, 25.0188, 90.0175, 2),
    BdDistrict("নেত্রকোনা (Netrokona)", "Netrokona", BdDivision.MYMENSINGH, 24.8701, 90.7275, -2)
)

fun adjustIslamicTimeStr(timeStr: String, offsetMinutes: Int): String {
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

fun parseTimeToCal(timeStr: String, offsetMinutes: Int, dayOffset: Int = 0): Calendar {
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

fun formatCountdownUnits(diffMillis: Long, isBn: Boolean): Triple<String, String, String> {
    if (diffMillis <= 0) {
        return if (isBn) Triple("০০", "০০", "০০") else Triple("00", "00", "00")
    }
    val totalSeconds = diffMillis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val hStr = String.format(Locale.ENGLISH, "%02d", hours)
    val mStr = String.format(Locale.ENGLISH, "%02d", minutes)
    val sStr = String.format(Locale.ENGLISH, "%02d", seconds)

    return if (isBn) {
        val bnDigits = mapOf('0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪', '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯')
        Triple(
            hStr.map { bnDigits[it] ?: it }.joinToString(""),
            mStr.map { bnDigits[it] ?: it }.joinToString(""),
            sStr.map { bnDigits[it] ?: it }.joinToString("")
        )
    } else {
        Triple(hStr, mStr, sStr)
    }
}

fun formatIslamicCountdownFull(diffMillis: Long, isBn: Boolean): String {
    val (h, m, s) = formatCountdownUnits(diffMillis, isBn)
    return if (isBn) "${h}ঘণ্টা ${m}মিনিট ${s}সেকেন্ড" else "${h}h ${m}m ${s}s"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistrictSelectionSheet(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedDivision by remember { mutableStateOf(BdDivision.ALL) }
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

    androidx.activity.compose.BackHandler(enabled = searchQuery.isNotEmpty()) {
        searchQuery = ""
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.autoDetectIslamicLocation(context) { success, msg ->
                if (success) {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    onDismiss()
                } else if (msg != null) {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            val msg = if (isBn) "লোকেশন পারমিশন ছাড়া স্বয়ংক্রিয় লোকেশন শনাক্ত করা সম্ভব নয়" else "Location permission is required for auto detection"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    val filteredDistricts = remember(searchQuery, selectedDivision) {
        allBdDistrictsList.filter { district ->
            val matchesDivision = selectedDivision == BdDivision.ALL || district.division == selectedDivision
            val matchesQuery = searchQuery.isBlank() ||
                    district.nameBn.contains(searchQuery, ignoreCase = true) ||
                    district.nameEn.contains(searchQuery, ignoreCase = true)
            matchesDivision && matchesQuery
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = themeColors.cardBg,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isBn) "জেলা ও লোকেশন নির্বাচন" else "Select District & Location",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        Text(
                            text = if (isBn) "বাংলাদেশের ৬৪টি জেলা ও জিপিএস অটো-লোকেশন" else "64 districts & GPS Auto Detection",
                            fontSize = 12.sp,
                            color = themeColors.displayText.copy(alpha = 0.65f)
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = themeColors.displayText.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // GPS Auto-Detect Button Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (viewModel.isIslamicLocationAutoDetected) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFF0284C7).copy(alpha = 0.12f)
                ),
                border = BorderStroke(
                    1.dp,
                    if (viewModel.isIslamicLocationAutoDetected) Color(0xFF10B981) else Color(0xFF0284C7).copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !viewModel.isDetectingIslamicLocation) {
                        if (IslamicLocationHelper.hasLocationPermission(context)) {
                            viewModel.autoDetectIslamicLocation(context) { success, msg ->
                                if (success) {
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                } else if (msg != null) {
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (viewModel.isIslamicLocationAutoDetected) Color(0xFF10B981) else Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (viewModel.isDetectingIslamicLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "Auto GPS",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (viewModel.isDetectingIslamicLocation) {
                                    if (isBn) "জিপিএস লোকেশন শনাক্ত হচ্ছে..." else "Detecting GPS location..."
                                } else {
                                    if (isBn) "আমার বর্তমান লোকেশন অটো-ডিটেক্ট করুন" else "Auto-Detect My Current Location"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (viewModel.isIslamicLocationAutoDetected) Color(0xFF10B981) else Color(0xFF0284C7)
                            )
                            Text(
                                text = if (viewModel.isIslamicLocationAutoDetected) {
                                    if (isBn) "বর্তমান জিপিএস শহর: ${viewModel.selectedIslamicDistrictBn}" else "GPS Detected: ${viewModel.selectedIslamicDistrictEn}"
                                } else {
                                    if (isBn) "জিপিএস দিয়ে নিকটস্থ জেলা ও নির্ভুল সময়সূচি পান" else "Accurate Sehri & Iftar for your exact city"
                                },
                                fontSize = 11.sp,
                                color = themeColors.displayText.copy(alpha = 0.65f)
                            )
                        }
                    }

                    if (viewModel.isIslamicLocationAutoDetected) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF10B981)
                        ) {
                            Text(
                                text = if (isBn) "সক্রিয়" else "Active",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = if (isBn) "জেলার নাম খুঁজুন (যেমন: ঢাকা, বগুড়া)..." else "Search district name...",
                        fontSize = 13.sp,
                        color = themeColors.displayText.copy(alpha = 0.45f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF0284C7)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = themeColors.displayText.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = themeColors.displayText,
                    unfocusedTextColor = themeColors.displayText,
                    cursorColor = Color(0xFF0284C7),
                    focusedBorderColor = Color(0xFF0284C7),
                    unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f),
                    focusedContainerColor = themeColors.background,
                    unfocusedContainerColor = themeColors.background
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Division Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(BdDivision.values()) { division ->
                    val isSelected = selectedDivision == division
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFF0284C7) else themeColors.background,
                        border = if (!isSelected) BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.1f)) else null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedDivision = division }
                    ) {
                        Text(
                            text = if (isBn) division.nameBn else division.nameEn,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else themeColors.displayText.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // District List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .padding(bottom = 20.dp)
            ) {
                items(filteredDistricts) { district ->
                    val isSelected = viewModel.selectedIslamicDistrictEn == district.nameEn
                    val offsetText = when {
                        district.offsetMinutes > 0 -> if (isBn) "+${district.offsetMinutes} মিনিট" else "+${district.offsetMinutes} min"
                        district.offsetMinutes < 0 -> if (isBn) "${district.offsetMinutes} মিনিট" else "${district.offsetMinutes} min"
                        else -> if (isBn) "স্ট্যান্ডার্ড সময় (০)" else "Standard (0)"
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF0284C7).copy(alpha = 0.12f) else themeColors.background
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF0284C7) else themeColors.displayText.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.updateIslamicDistrict(
                                    nameBn = district.nameBn,
                                    nameEn = district.nameEn,
                                    lat = district.lat,
                                    lon = district.lon,
                                    offsetMinutes = district.offsetMinutes,
                                    isAuto = false
                                )
                                onDismiss()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color(0xFF0284C7) else themeColors.displayText.copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Place,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else themeColors.displayText.copy(alpha = 0.5f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isBn) district.nameBn else district.nameEn,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF0284C7) else themeColors.displayText
                                    )
                                    Text(
                                        text = if (isBn) "${district.division.nameBn} বিভাগ" else "${district.division.nameEn} Division",
                                        fontSize = 11.sp,
                                        color = themeColors.displayText.copy(alpha = 0.55f)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (district.offsetMinutes == 0) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFF0284C7).copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = offsetText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (district.offsetMinutes == 0) Color(0xFF10B981) else Color(0xFF0284C7),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
