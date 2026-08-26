package com.example.ui.screens

import com.example.ui.components.ToolInfoSection
import com.example.ui.components.InfoToggleButton
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.util.scaleOnPress
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import java.util.Locale
import com.example.util.AppLanguage
import com.example.data.model.ToolCategory
import com.example.data.model.ToolType
import com.example.data.model.ConverterType
import com.example.data.model.isTitleLong
import com.example.data.model.isSubtitleLong
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.theme.themeCardShadow
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.LanguageManager

data class FeaturedDashboardItem(
    val key: String,
    val titleEn: String,
    val titleBn: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isTool: Boolean,
    val toolType: ToolType? = null,
    val converterType: ConverterType? = null
) {
    fun getSubtitle(language: AppLanguage): String {
        return if (isTool && toolType != null) {
            toolType.getDescription(language)
        } else if (converterType != null) {
            if (language == AppLanguage.BENGALI) "${converterType.titleBn} ইউনিট রূপান্তর" else "Convert ${converterType.titleEn} units"
        } else {
            if (language == AppLanguage.BENGALI) "কুইক সায়েন্টিফিক ক্যালকুলেটর" else "Quick Floating Calculator"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val selectedType = viewModel.selectedToolType
    val dashboardScrollState = androidx.compose.runtime.saveable.rememberSaveable(saver = androidx.compose.foundation.ScrollState.Saver) {
        androidx.compose.foundation.ScrollState(0)
    }
    val dashboardFilterScrollState = androidx.compose.runtime.saveable.rememberSaveable(saver = androidx.compose.foundation.ScrollState.Saver) {
        androidx.compose.foundation.ScrollState(0)
    }
    val categoryScrollStates = remember { mutableMapOf<String, androidx.compose.foundation.ScrollState>() }
    val featuredScrollState = androidx.compose.runtime.saveable.rememberSaveable(saver = androidx.compose.foundation.ScrollState.Saver) {
        androidx.compose.foundation.ScrollState(0)
    }

    AnimatedContent(
        targetState = selectedType,
        transitionSpec = {
            if (targetState != null) {
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
            } else {
                slideInHorizontally { width -> -width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> width } + fadeOut()
            }
        },
        label = "tools_screen_transition"
    ) { currentType ->
        if (currentType == null) {
            // View 1: Categories & Tools Grid View
            DashboardCategoriesView(
                viewModel = viewModel,
                themeColors = themeColors,
                scrollState = dashboardScrollState,
                filterScrollState = dashboardFilterScrollState,
                categoryScrollStates = categoryScrollStates,
                featuredScrollState = featuredScrollState
            )
        } else {
            // View 2: Detailed Tool View
            ToolDetailView(currentType, viewModel, themeColors)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardCategoriesView(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    scrollState: androidx.compose.foundation.ScrollState = androidx.compose.runtime.saveable.rememberSaveable(saver = androidx.compose.foundation.ScrollState.Saver) { androidx.compose.foundation.ScrollState(0) },
    filterScrollState: androidx.compose.foundation.ScrollState = androidx.compose.runtime.saveable.rememberSaveable(saver = androidx.compose.foundation.ScrollState.Saver) { androidx.compose.foundation.ScrollState(0) },
    categoryScrollStates: MutableMap<String, androidx.compose.foundation.ScrollState> = mutableMapOf(),
    featuredScrollState: androidx.compose.foundation.ScrollState = androidx.compose.foundation.ScrollState(0)
) {
    val coroutineScope = rememberCoroutineScope()
    val bounceAnimatable = remember { Animatable(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val currentValue = bounceAnimatable.value
                if (currentValue != 0f) {
                    if ((currentValue < 0f && available.y > 0f) || (currentValue > 0f && available.y < 0f)) {
                        val newDelta = available.y * 0.35f
                        val newValue = if (currentValue < 0f) {
                            (currentValue + newDelta).coerceAtMost(0f)
                        } else {
                            (currentValue + newDelta).coerceAtLeast(0f)
                        }
                        coroutineScope.launch {
                            bounceAnimatable.snapTo(newValue)
                        }
                        return Offset(0f, available.y)
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y != 0f) {
                    coroutineScope.launch {
                        bounceAnimatable.snapTo((bounceAnimatable.value + available.y * 0.35f).coerceIn(-140f, 140f))
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                bounceAnimatable.animateTo(
                    0f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                )
                return super.onPostFling(consumed, available)
            }
        }
    }

    LaunchedEffect(scrollState.isScrollInProgress) {
        if (!scrollState.isScrollInProgress && bounceAnimatable.value != 0f) {
            coroutineScope.launch {
                bounceAnimatable.animateTo(
                    0f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchWeather()
    }

    val allTools = ToolType.values()
    val searchQuery = viewModel.toolSearchQuery.lowercase().trim()
    val selectedFilter = viewModel.selectedToolCategoryFilter
    var showWeatherDialog by remember { mutableStateOf(false) }
    var unfavoriteConfirmTool by remember { mutableStateOf<ToolType?>(null) }
    var showAllFeaturedDialog by remember { mutableStateOf(false) }
    var selectedItemForOptions by remember { mutableStateOf<FeaturedDashboardItem?>(null) }

    val context = LocalContext.current
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.toolSearchQuery = spokenText
            }
        }
    }
    fun startVoiceSearch() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, if (viewModel.selectedLanguage == AppLanguage.BENGALI) "কথা বলুন..." else "Speak now...")
            }
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Voice search unavailable", Toast.LENGTH_SHORT).show()
        }
    }

    val filteredTools = allTools.filter { tool ->
        val matchesCategory = selectedFilter == null || tool.category == selectedFilter
        val matchesSearch = searchQuery.isEmpty() ||
                tool.titleEn.lowercase().contains(searchQuery) ||
                tool.titleBn.lowercase().contains(searchQuery) ||
                tool.descriptionBn.lowercase().contains(searchQuery)
        matchesCategory && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .nestedScroll(nestedScrollConnection)
            .graphicsLayer {
                translationY = bounceAnimatable.value
            }
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

        // Time-based Greeting & Multi-Date Header Banner
            val dateInfo = remember { com.example.util.CalendarUtils.getMultiDateInfo(java.util.Calendar.getInstance(), isBn) }

            val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
            val (greetingText, greetingIcon) = remember(currentHour, isBn) {
                when (currentHour) {
                    in 5..11 -> Pair(if (isBn) "শুভ সকাল" else "Good Morning", Icons.Default.WbSunny)
                    in 12..15 -> Pair(if (isBn) "শুভ দুপুর" else "Good Afternoon", Icons.Default.WbSunny)
                    in 16..17 -> Pair(if (isBn) "শুভ বিকেল" else "Good Afternoon", Icons.Default.WbTwilight)
                    in 18..20 -> Pair(if (isBn) "শুভ সন্ধ্যা" else "Good Evening", Icons.Default.WbTwilight)
                    else -> Pair(if (isBn) "শুভ রাত্রি" else "Good Night", Icons.Default.Bedtime)
                }
            }

        val location = viewModel.weatherLocation.ifBlank { "Dhaka" }
        val weatherText = if (viewModel.weatherIsLoading) {
            if (isBn) "লোডিং..." else "Loading..."
        } else if (viewModel.weatherData != null) {
            val current = viewModel.weatherData!!.current
            val temp = current.temperature_2m.toInt()
            val isDay = current.is_day == 1
            val condition = when (current.weather_code) {
                0 -> if (isBn) "পরিষ্কার" else "Clear"
                1, 2, 3 -> if (isBn) "আংশিক মেঘলা" else "Partly Cloudy"
                45, 48 -> if (isBn) "কুয়াশা" else "Fog"
                51, 53, 55 -> if (isBn) "গুঁড়ি গুঁড়ি বৃষ্টি" else "Drizzle"
                61, 63, 65 -> if (isBn) "বৃষ্টি" else "Rain"
                71, 73, 75 -> if (isBn) "তুষারপাত" else "Snow"
                95, 96, 99 -> if (isBn) "বজ্রবৃষ্টি" else "Thunderstorm"
                else -> if (isBn) "অজানা" else "Unknown"
            }
            "$location • $temp°${if (isBn) "সে." else "C"} • $condition"
        } else {
            "$location • --°${if (isBn) "সে." else "C"}"
        }

        val weatherIcon = if (viewModel.weatherData != null) {
            val code = viewModel.weatherData!!.current.weather_code
            val isDay = viewModel.weatherData!!.current.is_day == 1
            when (code) {
                0 -> if (isDay) Icons.Default.WbSunny else Icons.Default.Bedtime
                1, 2, 3 -> Icons.Default.Cloud
                61, 63, 65, 51, 53, 55 -> Icons.Default.WaterDrop
                95, 96, 99 -> Icons.Default.Thunderstorm
                else -> Icons.Default.Cloud
            }
        } else {
            Icons.Default.Cloud
        }

        val weatherLocationName = viewModel.weatherLocation.ifBlank { "Natore" }
        val weatherTempText = if (viewModel.weatherData != null) {
            "${viewModel.weatherData!!.current.temperature_2m.toInt()}°${if (isBn) "সে." else "C"}"
        } else {
            "30°${if (isBn) "সে." else "C"}"
        }
        val weatherConditionText = if (viewModel.weatherData != null) {
            val current = viewModel.weatherData!!.current
            when (current.weather_code) {
                0 -> if (isBn) "পরিষ্কার" else "Clear"
                1, 2, 3 -> if (isBn) "আংশিক মেঘলা" else "Partly Cloudy"
                45, 48 -> if (isBn) "কুয়াশা" else "Fog"
                51, 53, 55 -> if (isBn) "গুঁড়ি গুঁড়ি বৃষ্টি" else "Drizzle"
                61, 63, 65 -> if (isBn) "বৃষ্টি" else "Rain"
                71, 73, 75 -> if (isBn) "তুষারপাত" else "Snow"
                95, 96, 99 -> if (isBn) "বজ্রবৃষ্টি" else "Thunderstorm"
                else -> if (isBn) "আংশিক মেঘলা" else "Partly Cloudy"
            }
        } else {
            if (isBn) "আংশিক মেঘলা" else "Partly Cloudy"
        }

        // Unified Greeting, Weather & Calendar Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .themeCardShadow(themeColors, elevation = 1.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                val currentTimeMillis = remember { System.currentTimeMillis() }
                val currentHourVal = remember(currentTimeMillis) {
                    java.util.Calendar.getInstance().apply { timeInMillis = currentTimeMillis }.get(java.util.Calendar.HOUR_OF_DAY)
                }
                val weatherCodeVal = remember(viewModel.weatherData) {
                    viewModel.weatherData?.current?.weather_code ?: 1
                }
                DynamicGreetingIllustrationBackground(
                    currentHour = currentHourVal,
                    weatherCode = weatherCodeVal,
                    isDark = themeColors.isDark,
                    modifier = Modifier.matchParentSize()
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Color(0xFF0F172A).copy(alpha = 0.42f)
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                // Top section: Greeting & Weather Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Greeting Title & English Date (Clickable to open Calendar)
                    Column(
                        modifier = Modifier
                            .weight(1.05f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.openTool(ToolType.MULTI_CALENDAR) }
                            .padding(2.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(
                                imageVector = greetingIcon,
                                contentDescription = "Greeting",
                                tint = Color(0xFFFDE047),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = greetingText,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${dateInfo.englishDayName}, ${dateInfo.englishDate}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.85f),
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Calendar",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    // Spacer to keep balance
                    Spacer(modifier = Modifier.width(12.dp))

                    // Right Column: Weather Info (Clickable)
                    Column(
                        modifier = Modifier
                            .weight(0.95f)
                            .clickable { viewModel.openTool(ToolType.WEATHER) },
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "$weatherLocationName • $weatherTempText",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = weatherIcon,
                                contentDescription = "Weather",
                                tint = Color.White.copy(alpha = 0.95f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = weatherConditionText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Divider line
                Divider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = Color.White.copy(alpha = 0.15f),
                    thickness = 1.dp
                )

                // Bottom section: Bengali & Hijri Calendars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bengali Date (Left Aligned - Clickable)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { viewModel.openTool(ToolType.MULTI_CALENDAR) }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Bengali Calendar",
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = dateInfo.bengaliDate,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Hijri/Arabic Date (Right Aligned - Clickable)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { viewModel.openTool(ToolType.MULTI_CALENDAR) }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = "Hijri Calendar",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = dateInfo.hijriDate,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } // Closes Column
        } // Closes Box
    } // Closes Card


        // Change Weather Location Dialog
        if (showWeatherDialog) {
            var tempInput by remember { mutableStateOf(viewModel.weatherLocation) }
            var isFetchingLocation by remember { mutableStateOf(false) }
            var locationErrorMsg by remember { mutableStateOf<String?>(null) }
            
            val locationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
                if (granted) {
                    isFetchingLocation = true
                    locationErrorMsg = null
                    getCurrentLocationName(context, isBn) { city ->
                        isFetchingLocation = false
                        if (city != null) {
                            tempInput = city
                        } else {
                            locationErrorMsg = if (isBn) "লোকেশন পাওয়া যায়নি!" else "Location not found!"
                        }
                    }
                } else {
                    locationErrorMsg = if (isBn) "লোকেশন পারমিশন দেওয়া হয়নি!" else "Location permission denied!"
                }
            }

            AlertDialog(
                onDismissRequest = { showWeatherDialog = false },
                title = {
                    Text(
                        text = if (isBn) "আবহাওয়া লোকেশন" else "Weather Location",
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                },
                text = {
                    Column {
                        // Current Location Button
                        Button(
                            onClick = {
                                val hasFine = ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.ACCESS_FINE_LOCATION
                               ) == PackageManager.PERMISSION_GRANTED
                                val hasCoarse = ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                                
                                if (hasFine || hasCoarse) {
                                    isFetchingLocation = true
                                    locationErrorMsg = null
                                    getCurrentLocationName(context, isBn) { city ->
                                        isFetchingLocation = false
                                        if (city != null) {
                                            tempInput = city
                                        } else {
                                            locationErrorMsg = if (isBn) "লোকেশন পাওয়া যায়নি!" else "Location not found!"
                                        }
                                    }
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = themeColors.buttonEqualBg.copy(alpha = 0.15f),
                                contentColor = themeColors.buttonEqualBg
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isFetchingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = themeColors.buttonEqualBg,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBn) "লোকেশন খোঁজা হচ্ছে..." else "Finding Location...",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "My Location",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBn) "আমার বর্তমান লোকেশন নিন" else "Use Current Location",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        if (locationErrorMsg != null) {
                            Text(
                                text = locationErrorMsg!!,
                                color = Color(0xFFEF4444),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Divider(
                            color = themeColors.displayText.copy(alpha = 0.1f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Text(
                            text = if (isBn) "অথবা টাইপ করে সার্চ করুন:" else "Or search/type name manually:",
                            color = themeColors.displayText.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = tempInput,
                            onValueChange = { tempInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = if (isBn) "উদা: ঢাকা, সিলেট, চট্টগ্রাম" else "e.g., Dhaka, London, Tokyo",
                                    color = themeColors.displayText.copy(alpha = 0.4f)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = themeColors.displayText,
                                unfocusedTextColor = themeColors.displayText,
                                focusedBorderColor = themeColors.buttonEqualBg,
                                unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.3f),
                                cursorColor = themeColors.buttonEqualBg
                            ),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateWeatherLocation(tempInput)
                            showWeatherDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
                    ) {
                        Text(text = if (isBn) "নির্ধারণ করুন" else "Save", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showWeatherDialog = false }) {
                        Text(text = if (isBn) "বাতিল" else "Cancel", color = themeColors.buttonEqualBg)
                    }
                },
                containerColor = themeColors.cardBg,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Pending Favorite Add/Remove Confirmation Dialog
        viewModel.pendingFavoriteConfirmAction?.let { action ->
            val isAdding = !action.isCurrentlyFavorite
            val itemKey = if (action.isTool) action.key else "CONV_${action.key}"
            val context = LocalContext.current

            AlertDialog(
                onDismissRequest = { viewModel.dismissPendingFavoriteAction() },
                title = {
                    Text(
                        text = if (isBn) "\"${action.titleBn}\" অপশন" else "\"${action.titleEn}\" Options",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = themeColors.displayText
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = if (isBn) {
                                if (isAdding) "\"${action.titleBn}\" কে প্রিয় তালিকায় যুক্ত বা ওপরের সেরা ৪টিতে পিন করতে পারেন।"
                                else "\"${action.titleBn}\" কে প্রিয় তালিকা থেকে সরাতে পারেন।"
                            } else {
                                if (isAdding) "Add \"${action.titleEn}\" to favorites or pin to Top 4 on main screen."
                                else "Remove \"${action.titleEn}\" from your favorites list."
                            },
                            fontSize = 13.5.sp,
                            color = themeColors.displayText.copy(alpha = 0.85f),
                            lineHeight = 18.sp
                        )

                        // Pin to Top 4 Section
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = themeColors.displayBackground,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PushPin,
                                        contentDescription = null,
                                        tint = themeColors.buttonEqualBg,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (isBn) "📌 সেরা ৪টি প্রাইমারি কার্ডে পিন করুন:" else "📌 Pin to Top 4 Featured Cards:",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.buttonEqualBg
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(
                                        0 to if (isBn) "১ম স্থানে" else "Pos 1",
                                        1 to if (isBn) "২য় স্থানে" else "Pos 2",
                                        2 to if (isBn) "৩য় স্থানে" else "Pos 3",
                                        3 to if (isBn) "৪র্থ স্থানে" else "Pos 4"
                                    ).forEach { (pos, label) ->
                                        Button(
                                            onClick = {
                                                if (action.isTool) {
                                                    val tool = com.example.data.model.ToolType.values().find { it.name == action.key }
                                                    if (tool != null) viewModel.pinToolToCategoryTop4(tool, pos)
                                                } else {
                                                    val conv = com.example.data.model.ConverterType.values().find { it.name == action.key }
                                                    if (conv != null) viewModel.pinConverterToCategoryTop4(conv, pos)
                                                }
                                                viewModel.dismissPendingFavoriteAction()
                                                val posText = if (isBn) "${pos + 1} নম্বর" else "Pos ${pos + 1}"
                                                Toast.makeText(
                                                    context,
                                                    if (isBn) "\"${action.titleBn}\" $posText স্থানে পিন করা হয়েছে!" else "\"${action.titleEn}\" pinned to position $posText!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = themeColors.buttonEqualBg.copy(alpha = 0.15f),
                                                contentColor = themeColors.buttonEqualBg
                                            ),
                                            contentPadding = PaddingValues(vertical = 6.dp, horizontal = 2.dp)
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        // Add to Home Screen Button
                        Button(
                            onClick = {
                                if (action.isTool) {
                                    val tool = com.example.data.model.ToolType.values().find { it.name == action.key }
                                    if (tool != null) com.example.util.ShortcutUtils.pinToolShortcut(context, tool, isBn)
                                } else {
                                    val conv = com.example.data.model.ConverterType.values().find { it.name == action.key }
                                    if (conv != null) com.example.util.ShortcutUtils.pinConverterShortcut(context, conv, isBn)
                                }
                                viewModel.dismissPendingFavoriteAction()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = themeColors.buttonEqualBg,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn) "📱 হোমস্ক্রিনে শর্টকাট যোগ করুন" else "📱 Add to Home Screen",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.confirmPendingFavoriteAction()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAdding) themeColors.buttonEqualBg.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f),
                            contentColor = if (isAdding) themeColors.buttonEqualBg else Color.Red
                        )
                    ) {
                        Text(
                            text = if (isBn) {
                                if (isAdding) "⭐ প্রিয় তালিকায় যোগ" else "🗑️ প্রিয় তালিকা থেকে সরান"
                            } else {
                                if (isAdding) "⭐ Add Favorite" else "🗑️ Remove Favorite"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissPendingFavoriteAction() }) {
                        Text(text = if (isBn) "বাতিল" else "Cancel", color = themeColors.buttonEqualBg)
                    }
                },
                containerColor = themeColors.cardBg,
                shape = RoundedCornerShape(18.dp)
            )
        }

        if (unfavoriteConfirmTool != null) {
            val toolToUnfav = unfavoriteConfirmTool!!
            AlertDialog(
                onDismissRequest = { unfavoriteConfirmTool = null },
                title = {
                    Text(
                        text = if (isBn) "নিশ্চিতকরণ" else "Confirmation",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = themeColors.displayText
                    )
                },
                text = {
                    Text(
                        text = if (isBn) {
                            "আপনি কি এই টুলটি (${toolToUnfav.titleBn}) ফেভারিট তালিকা থেকে বাদ দিতে চান?"
                        } else {
                            "Are you sure you want to remove ${toolToUnfav.titleEn} from your favorites?"
                        },
                        fontSize = 14.sp,
                        color = themeColors.displayText.copy(alpha = 0.8f)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.toggleFavoriteTool(toolToUnfav.name)
                            unfavoriteConfirmTool = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
                    ) {
                        Text(text = if (isBn) "হ্যাঁ" else "Yes", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { unfavoriteConfirmTool = null }) {
                        Text(text = if (isBn) "না" else "No", color = themeColors.buttonEqualBg)
                    }
                },
                containerColor = themeColors.cardBg,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Search Bar (Placed at the top of the tools section)
        OutlinedTextField(
            value = viewModel.toolSearchQuery,
            onValueChange = { viewModel.toolSearchQuery = it },
            placeholder = {
                Text(
                    text = LanguageManager.getString("search_tools", viewModel.selectedLanguage),
                    color = themeColors.displayText.copy(alpha = 0.5f),
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = themeColors.displayText.copy(alpha = 0.6f)
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (viewModel.toolSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.toolSearchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = themeColors.displayText.copy(alpha = 0.6f)
                            )
                        }
                    }
                    IconButton(onClick = { startVoiceSearch() }) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = themeColors.buttonEqualBg
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = themeColors.cardBg,
                unfocusedContainerColor = themeColors.cardBg,
                focusedBorderColor = themeColors.buttonEqualBg,
                unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f),
                focusedTextColor = themeColors.displayText,
                unfocusedTextColor = themeColors.displayText
            ),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("tool_search_input")
        )

        if (searchQuery.isNotBlank()) {
            // Live Search Results View (Featured & Category sections hidden during search)
            DashboardSearchResultsView(
                searchQuery = searchQuery,
                viewModel = viewModel,
                themeColors = themeColors,
                isBn = isBn
            )
        } else {
            // Featured & Favorite Tools Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBn) "ফিচার্ড ও ফেভারিট টুলস" else "Featured & Favorite Tools",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = themeColors.displayText
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(themeColors.buttonEqualBg.copy(alpha = 0.12f))
                        .clickable { showAllFeaturedDialog = true }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = themeColors.buttonEqualBg,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBn) "সব দেখুন" else "View All",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.buttonEqualBg
                    )
                }
            }

        // Horizontal Scroll Layout for the Combined Featured & Favorite Tools and Converters
        val combinedList = remember(viewModel.orderedFavoriteTools) {
            viewModel.orderedFavoriteTools.mapNotNull { name ->
                if (name.startsWith("CONV_")) {
                    val cName = name.removePrefix("CONV_")
                    try {
                        val cType = ConverterType.valueOf(cName)
                        FeaturedDashboardItem(
                            key = name,
                            titleEn = cType.titleEn,
                            titleBn = cType.titleBn,
                            icon = cType.icon,
                            isTool = false,
                            converterType = cType
                        )
                    } catch (e: Exception) { null }
                } else {
                    try {
                        val tType = ToolType.valueOf(name)
                        FeaturedDashboardItem(
                            key = name,
                            titleEn = tType.titleEn,
                            titleBn = tType.titleBn,
                            icon = tType.icon,
                            isTool = true,
                            toolType = tType
                        )
                    } catch (e: Exception) {
                        try {
                            val cType = ConverterType.valueOf(name)
                            FeaturedDashboardItem(
                                key = "CONV_$name",
                                titleEn = cType.titleEn,
                                titleBn = cType.titleBn,
                                icon = cType.icon,
                                isTool = false,
                                converterType = cType
                            )
                        } catch (e2: Exception) { null }
                    }
                }
            }.distinctBy { it.key }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(featuredScrollState)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            combinedList.forEach { item ->
                val isFavorited = if (item.isTool && item.toolType != null) {
                    viewModel.favoriteTools.contains(item.toolType.name)
                } else if (item.converterType != null) {
                    viewModel.favoriteConverters.contains(item.converterType.name)
                } else false
                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier.width(130.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .themeCardShadow(themeColors, elevation = 1.dp)
                            .combinedClickable(
                                interactionSource = interactionSource,
                                indication = androidx.compose.foundation.LocalIndication.current,
                                onClick = {
                                    if (item.isTool && item.toolType != null) {
                                        viewModel.openTool(item.toolType)
                                    } else if (item.converterType != null) {
                                        viewModel.openConverter(item.converterType)
                                    }
                                },
                                onLongClick = { selectedItemForOptions = item }
                            ),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(themeColors.buttonEqualBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.titleEn,
                                    tint = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isBn) item.titleBn else item.titleEn,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.getSubtitle(viewModel.selectedLanguage),
                                fontSize = 10.sp,
                                color = themeColors.displayText.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Favorite Icon in the Top-Right Corner of favorited tools/converters
                    if (isFavorited) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 4.dp, end = 4.dp)
                        ) {
                            IconButton(
                                onClick = { selectedItemForOptions = item },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Options",
                                    tint = themeColors.buttonEqualBg,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // View All / সব দেখুন Dialog
        if (showAllFeaturedDialog) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showAllFeaturedDialog = false },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
            ) {
                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxWidth(0.92f).padding(vertical = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = themeColors.cardBg
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp).fillMaxWidth()
                    ) {
                        Text(
                            text = if (isBn) "ফিচার্ড ও ফেভারিট টুলস" else "Featured & Favorite Tools",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = themeColors.displayText,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = if (isBn) 
                                "কার্ডে লং প্রেস করে রিমুভ করুন অথবা অর্ডার পরিবর্তন করুন।" 
                            else 
                                "Long press on any card to remove it or change its order.",
                            fontSize = 12.sp,
                            color = themeColors.displayText.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        val chunked = combinedList.chunked(2)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .verticalScroll(rememberScrollState())
                        ) {
                            chunked.forEach { rowItems ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowItems.forEach { item ->
                                        val isFav = if (item.isTool && item.toolType != null) {
                                            viewModel.favoriteTools.contains(item.toolType.name)
                                        } else if (item.converterType != null) {
                                            viewModel.favoriteConverters.contains(item.converterType.name)
                                        } else false
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .themeCardShadow(themeColors, elevation = 1.dp)
                                                .combinedClickable(
                                                    onClick = { 
                                                        showAllFeaturedDialog = false
                                                        if (item.isTool && item.toolType != null) {
                                                            viewModel.openTool(item.toolType)
                                                        } else if (item.converterType != null) {
                                                            viewModel.openConverter(item.converterType)
                                                        }
                                                    },
                                                    onLongClick = {
                                                        selectedItemForOptions = item
                                                    }
                                                ),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = themeColors.cardBg)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .padding(10.dp)
                                                    .fillMaxWidth(),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(themeColors.buttonEqualBg),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = item.icon,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = if (isBn) item.titleBn else item.titleEn,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = themeColors.displayText,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Text(
                                                    text = item.getSubtitle(viewModel.selectedLanguage),
                                                    fontSize = 9.sp,
                                                    color = themeColors.displayText.copy(alpha = 0.55f),
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.padding(horizontal = 4.dp)
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = themeColors.buttonEqualBg,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = if (isBn) "লং প্রেস: অপশন" else "Hold for Options",
                                                        fontSize = 9.sp,
                                                        color = themeColors.displayText.copy(alpha = 0.5f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (rowItems.size < 2) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { showAllFeaturedDialog = false }) {
                                Text(
                                    text = if (isBn) "বন্ধ করুন" else "Close",
                                    color = themeColors.buttonEqualBg,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Long Press / Reorder Option Dialog
        if (selectedItemForOptions != null) {
            val item = selectedItemForOptions!!
            val index = combinedList.indexOf(item)
            AlertDialog(
                onDismissRequest = { selectedItemForOptions = null },
                title = {
                    Text(
                        text = if (isBn) item.titleBn else item.titleEn,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isBn) "আপনি কি করতে চান?" else "What would you like to do?",
                            fontSize = 13.sp,
                            color = themeColors.displayText.copy(alpha = 0.7f)
                        )
                        
                        // Move Left button (if index > 0)
                        if (index > 0) {
                            Button(
                                onClick = {
                                    val newList = viewModel.orderedFavoriteTools.toMutableList()
                                    val currentKey = item.key
                                    val actualIdx = newList.indexOfFirst { it == currentKey || (item.converterType != null && it == item.converterType.name) }
                                    if (actualIdx > 0) {
                                        val elem = newList.removeAt(actualIdx)
                                        newList.add(actualIdx - 1, elem)
                                        viewModel.saveOrderedFavorites(newList)
                                    }
                                    selectedItemForOptions = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = themeColors.buttonEqualBg.copy(alpha = 0.15f),
                                    contentColor = themeColors.buttonEqualBg
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = if (isBn) "বামে সরান" else "Move Left")
                            }
                        }

                        // Move Right button (if index < combinedList.size - 1)
                        if (index < combinedList.size - 1 && index >= 0) {
                            Button(
                                onClick = {
                                    val newList = viewModel.orderedFavoriteTools.toMutableList()
                                    val currentKey = item.key
                                    val actualIdx = newList.indexOfFirst { it == currentKey || (item.converterType != null && it == item.converterType.name) }
                                    if (actualIdx != -1 && actualIdx < newList.size - 1) {
                                        val elem = newList.removeAt(actualIdx)
                                        newList.add(actualIdx + 1, elem)
                                        viewModel.saveOrderedFavorites(newList)
                                    }
                                    selectedItemForOptions = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = themeColors.buttonEqualBg.copy(alpha = 0.15f),
                                    contentColor = themeColors.buttonEqualBg
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = if (isBn) "ডানে সরান" else "Move Right")
                            }
                        }

                        // Remove from Favorite/Featured
                        Button(
                            onClick = {
                                if (item.isTool && item.toolType != null) {
                                    viewModel.toggleFavoriteTool(item.toolType.name)
                                } else if (item.converterType != null) {
                                    viewModel.toggleFavoriteConverter(item.converterType.name)
                                }
                                selectedItemForOptions = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = themeColors.buttonEqualBg,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = if (isBn) "রিমুভ করুন" else "Remove")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedItemForOptions = null }) {
                        Text(
                            text = if (isBn) "বাতিল" else "Cancel",
                            color = themeColors.buttonEqualBg
                        )
                    }
                },
                containerColor = themeColors.background
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        val allTools = ToolType.values().toList()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(filterScrollState)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // "All" Chip
            ToolFilterChipItem(
                label = LanguageManager.getString("all", viewModel.selectedLanguage),
                isSelected = selectedFilter == null,
                icon = Icons.Default.Apps,
                themeColors = themeColors,
                count = allTools.size,
                onClick = { viewModel.selectedToolCategoryFilter = null }
            )

            ToolCategory.values().forEach { cat ->
                val catCount = allTools.count { it.category == cat }
                ToolFilterChipItem(
                    label = cat.getTitle(viewModel.selectedLanguage),
                    isSelected = selectedFilter == cat,
                    icon = cat.icon,
                    themeColors = themeColors,
                    count = catCount,
                    onClick = {
                        viewModel.selectedToolCategoryFilter = if (selectedFilter == cat) null else cat
                    }
                )
            }
        }

        // Tools List Grouped by Category with Smooth Category Switch Animation
        AnimatedContent(
            targetState = selectedFilter,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220)) + slideInVertically(animationSpec = tween(220)) { it / 8 }) togetherWith
                fadeOut(animationSpec = tween(150))
            },
            label = "categorySortAnimation"
        ) { currentFilter ->
            val currentFilteredTools = if (currentFilter == null) {
                allTools
            } else {
                allTools.filter { it.category == currentFilter }
            }

            if (currentFilteredTools.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = LanguageManager.getString("no_results", viewModel.selectedLanguage),
                        color = themeColors.displayText.copy(alpha = 0.6f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                val isOverviewMode = currentFilter == null

                Column(modifier = Modifier.fillMaxWidth()) {
                    // Category Active Banner when filtered
                    if (!isOverviewMode && currentFilter != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = themeColors.cardBg,
                            border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(themeColors.buttonEqualBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = currentFilter.icon,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = currentFilter.getTitle(viewModel.selectedLanguage),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.displayText
                                        )
                                        Text(
                                            text = if (isBn) "মোট ${currentFilteredTools.size}টি টুলস" else "Total ${currentFilteredTools.size} Tools",
                                            fontSize = 11.sp,
                                            color = themeColors.buttonEqualBg,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.selectedToolCategoryFilter = null },
                                    color = themeColors.buttonEqualBg.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Apps,
                                            contentDescription = null,
                                            tint = themeColors.buttonEqualBg,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isBn) "সকল টুলস" else "All Tools",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.buttonEqualBg
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val categoriesToShow = if (isOverviewMode) {
                        ToolCategory.values().filter { cat ->
                            currentFilteredTools.any { it.category == cat }
                        }
                    } else {
                        listOfNotNull(currentFilter)
                    }

                    val expandedCategories = viewModel.expandedToolCategories
                    val topToolsMap = viewModel.categoryTopToolsMap
                    categoriesToShow.forEach { category ->
                        val orderedCatTools = viewModel.getAllOrderedToolsForCategory(category)
                        val categoryTools = orderedCatTools.filter { currentFilteredTools.contains(it) }

                        if (categoryTools.isNotEmpty()) {
                            val isCategoryExpanded = expandedCategories.getOrDefault(category, false)

                            // Category Header
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(themeColors.buttonEqualBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = category.icon,
                                            contentDescription = category.titleEn,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = category.getTitle(viewModel.selectedLanguage),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.displayText
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .defaultMinSize(minWidth = 22.dp, minHeight = 22.dp)
                                            .clip(CircleShape)
                                            .background(themeColors.buttonEqualBg.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isBn) "${categoryTools.size}টি" else "${categoryTools.size}",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.buttonEqualBg
                                        )
                                    }
                                }

                                if (isOverviewMode && categoryTools.size > 2) {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(themeColors.buttonEqualBg.copy(alpha = 0.08f))
                                            .clickable { expandedCategories[category] = !isCategoryExpanded }
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isCategoryExpanded) {
                                                if (isBn) "সংকুচিত করুন" else "Collapse"
                                            } else {
                                                if (isBn) "সব দেখুন" else "See all"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.buttonEqualBg
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            imageVector = if (isCategoryExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = themeColors.buttonEqualBg,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            // Dynamic Layout: Horizontal Scrolling Row when collapsed in Overview Mode, Vertical Grid when expanded or filtered
                            AnimatedContent(
                                targetState = if (isOverviewMode) isCategoryExpanded else true,
                                transitionSpec = {
                                    (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.96f, animationSpec = tween(220))) togetherWith
                                            fadeOut(animationSpec = tween(160))
                                },
                                label = "ToolsDisplayMode_${category.name}"
                            ) { isExpandedState ->
                                if (isExpandedState) {
                                    // 2-column Vertical Grid
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    ) {
                                        categoryTools.chunked(2).forEach { rowItems ->
                                            val isBn = viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI
                                             val isAnyTitleLongInRow = rowItems.any { isTitleLong(it.getTitle(viewModel.selectedLanguage), isBn) }
                                             val isAnySubtitleLongInRow = rowItems.any { isSubtitleLong(it.getDescription(viewModel.selectedLanguage), isBn) }
                                             val rowTitleLines = if (isAnyTitleLongInRow) 2 else 1
                                             val rowSubtitleLines = if (isAnySubtitleLongInRow) 2 else 1
                                            Row(
                                                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                rowItems.forEach { tool ->
                                                    key(tool.name) {
                                                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                                            ToolGridCardItem(
                                                                toolType = tool,
                                                                viewModel = viewModel,
                                                                themeColors = themeColors,
                                                                modifier = Modifier.fillMaxHeight(),
                                                                showPinIcon = !isOverviewMode,
                                                                titleLines = rowTitleLines,
                                                                subtitleLines = rowSubtitleLines,
                                                                onClick = { viewModel.openTool(tool) }
                                                            )
                                                        }
                                                    }
                                                }
                                                if (rowItems.size == 1) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Default Horizontal Scrolling Row
                                    val isBn = viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI
                                    val isAnyTitleLong = categoryTools.any { isTitleLong(it.getTitle(viewModel.selectedLanguage), isBn) }
                                    val isAnySubtitleLong = categoryTools.any { isSubtitleLong(it.getDescription(viewModel.selectedLanguage), isBn) }
                                    val titleLines = if (isAnyTitleLong) 2 else 1
                                    val subtitleLines = if (isAnySubtitleLong) 2 else 1
                                    val cardHeight = when {
                                        titleLines == 2 && subtitleLines == 2 -> 135.dp
                                        titleLines == 2 || subtitleLines == 2 -> 122.dp
                                        else -> 110.dp
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(
                                                remember(category.name) {
                                                    categoryScrollStates.getOrPut(category.name) {
                                                        androidx.compose.foundation.ScrollState(0)
                                                     }
                                                }
                                            )
                                            .padding(bottom = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        categoryTools.forEach { tool ->
                                            key(tool.name) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(152.dp)
                                                        .height(cardHeight)
                                                ) {
                                                    ToolGridCardItem(
                                                        toolType = tool,
                                                        viewModel = viewModel,
                                                        themeColors = themeColors,
                                                        modifier = Modifier.fillMaxSize(),
                                                        showPinIcon = false,
                                                        titleLines = titleLines,
                                                        subtitleLines = subtitleLines,
                                                        onClick = { viewModel.openTool(tool) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Clean Collapse Button at the bottom of section when expanded in Overview mode
                            if (isOverviewMode) {
                                if (isCategoryExpanded) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp, bottom = 14.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { expandedCategories[category] = false },
                                        shape = RoundedCornerShape(12.dp),
                                        color = themeColors.cardBg.copy(alpha = 0.6f),
                                        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 9.dp, horizontal = 14.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowUp,
                                                contentDescription = null,
                                                tint = themeColors.displayText.copy(alpha = 0.7f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isBn) 
                                                    "সংকুচিত করুন (কম দেখুন)" 
                                                else 
                                                    "Collapse (Show Less)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = themeColors.displayText.copy(alpha = 0.75f)
                                            )
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            } else {
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
        }
        } // End of else block for searchQuery
    }
}

@Composable
fun ToolFilterChipItem(
    label: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    themeColors: CalculatorThemeColors,
    count: Int = 0,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) themeColors.buttonEqualBg else themeColors.cardBg)
            .scaleOnPress(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else themeColors.displayText.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else themeColors.displayText
            )
            if (count > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color.White.copy(alpha = 0.28f)
                            else themeColors.buttonEqualBg
                        )
                        .padding(horizontal = 5.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$count",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToolGridCardItem(
    toolType: ToolType,
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    modifier: Modifier = Modifier,
    showPinIcon: Boolean = true,
    titleLines: Int = 2,
    subtitleLines: Int = 2,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val cardContext = LocalContext.current
    val isBn = viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI
    val isFavorite = viewModel.favoriteTools.contains(toolType.name)
    val isPinned = viewModel.isToolPinnedInTop4(toolType)
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tool_card_${toolType.name.lowercase()}")
            .scaleOnPress(interactionSource)
            .themeCardShadow(themeColors, elevation = 1.dp)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick,
                onLongClick = {
                    viewModel.requestToggleFavoriteTool(toolType)
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = themeColors.cardBg
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(themeColors.buttonEqualBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = toolType.icon,
                        contentDescription = toolType.getTitle(viewModel.selectedLanguage),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (showPinIcon) {
                        IconButton(
                            onClick = { viewModel.requestToggleFavoriteTool(toolType) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pin Position",
                                tint = if (isPinned) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.35f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.toggleFavoriteTool(toolType.name) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = toolType.getTitle(viewModel.selectedLanguage),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText,
                    maxLines = titleLines,
                    minLines = titleLines,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = toolType.getDescription(viewModel.selectedLanguage),
                    fontSize = 10.sp,
                    color = themeColors.displayText.copy(alpha = 0.55f),
                    maxLines = subtitleLines,
                    minLines = subtitleLines,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    lineHeight = 12.5.sp
                )
            }
        }
    }
}

@Composable
fun ToolDetailView(
    toolType: ToolType,
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val bounceAnimatable = remember { Animatable(0f) }
    var showToolInfo by remember { mutableStateOf(false) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val currentValue = bounceAnimatable.value
                if (currentValue != 0f) {
                    if ((currentValue < 0f && available.y > 0f) || (currentValue > 0f && available.y < 0f)) {
                        val newDelta = available.y * 0.35f
                        val newValue = if (currentValue < 0f) {
                            (currentValue + newDelta).coerceAtMost(0f)
                        } else {
                            (currentValue + newDelta).coerceAtLeast(0f)
                        }
                        coroutineScope.launch {
                            bounceAnimatable.snapTo(newValue)
                        }
                        return Offset(0f, available.y)
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y != 0f) {
                    coroutineScope.launch {
                        bounceAnimatable.snapTo((bounceAnimatable.value + available.y * 0.35f).coerceIn(-140f, 140f))
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                bounceAnimatable.animateTo(
                    0f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                )
                return super.onPostFling(consumed, available)
            }
        }
    }

    LaunchedEffect(scrollState.isScrollInProgress) {
        if (!scrollState.isScrollInProgress && bounceAnimatable.value != 0f) {
            coroutineScope.launch {
                bounceAnimatable.animateTo(
                    0f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                )
            }
        }
    }

    val baseModifier = Modifier
        .fillMaxSize()
        .background(themeColors.background)
        .nestedScroll(nestedScrollConnection)
        .offset { IntOffset(0, bounceAnimatable.value.roundToInt()) }
    
    val isQuranOrNamaz = toolType == com.example.data.model.ToolType.HOLY_QURAN ||
                         toolType == com.example.data.model.ToolType.NAMAZ_EDUCATION ||
                         toolType == com.example.data.model.ToolType.HADITH_LIBRARY ||
                         toolType == com.example.data.model.ToolType.PDF_READER ||
                         toolType == com.example.data.model.ToolType.PDF_MAKER ||
                          toolType == com.example.data.model.ToolType.QR_BARCODE

    val isFullWidthTool = toolType == com.example.data.model.ToolType.MARKET_LIST || isQuranOrNamaz

    val finalModifier = if (toolType != com.example.data.model.ToolType.WEATHER &&
                          toolType != com.example.data.model.ToolType.MARKET_LIST &&
                          toolType != com.example.data.model.ToolType.NOTES_CHECKLIST &&
                          toolType != com.example.data.model.ToolType.WORLD_CLOCK &&
                          toolType != com.example.data.model.ToolType.PDF_READER &&
                          toolType != com.example.data.model.ToolType.PDF_MAKER &&
                          toolType != com.example.data.model.ToolType.QR_BARCODE &&
                          !isQuranOrNamaz) {
        baseModifier.verticalScroll(scrollState)
    } else {
        baseModifier
    }

    val contentPaddingModifier = if (isFullWidthTool) {
        Modifier.padding(0.dp)
    } else {
        Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    }

    Column(
        modifier = finalModifier.then(contentPaddingModifier)
    ) {
        if (!isQuranOrNamaz) {
            // Back Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = if (isFullWidthTool) 16.dp else 0.dp,
                        end = if (isFullWidthTool) 16.dp else 0.dp,
                        bottom = 16.dp,
                        top = if (isFullWidthTool) 12.dp else 0.dp
                    )
            ) {
                val backInteractionSource = remember { MutableInteractionSource() }
                FilledIconButton(
                    onClick = { viewModel.closeToolDetail() },
                    interactionSource = backInteractionSource,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = themeColors.cardBg,
                        contentColor = themeColors.displayText
                    ),
                    modifier = Modifier
                        .size(40.dp)
                        .scaleOnPress(backInteractionSource)
                        .testTag("back_to_tools_list")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = toolType.getTitle(viewModel.selectedLanguage),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        Text(
                            text = toolType.category.getTitle(viewModel.selectedLanguage),
                            fontSize = 12.sp,
                            color = themeColors.buttonEqualBg,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val isFavorite = viewModel.favoriteTools.contains(toolType.name)
                        IconButton(
                            onClick = { viewModel.toggleFavoriteTool(toolType.name) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color.Red.copy(alpha = 0.8f) else themeColors.displayText.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        InfoToggleButton(
                            isExpanded = showToolInfo,
                            onToggle = { showToolInfo = !showToolInfo },
                            themeColors = themeColors
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showToolInfo,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
                val infoTitle = if (isBn) "প্রয়োজনীয় তথ্য ও গাইডলাইন" else "Helpful Information & Guidelines"
                val infoItems = getToolInfoItems(toolType, isBn)
                if (infoItems.isNotEmpty()) {
                    ToolInfoSection(
                        title = infoTitle,
                        infoItems = infoItems,
                        themeColors = themeColors,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
        }

        // Selected Tool UI Composable
        when (toolType) {
            ToolType.BMI -> BMICalculatorCard(viewModel, themeColors)
            ToolType.BMR -> BMRCalculatorCard(viewModel, themeColors)
            ToolType.IDEAL_WEIGHT -> IdealWeightCalculatorCard(viewModel, themeColors)
            ToolType.WATER_INTAKE -> WaterIntakeTrackerCard(viewModel, themeColors)
            ToolType.EMI_LOAN -> EmiLoanCalculatorCard(viewModel, themeColors)
            ToolType.DISCOUNT -> DiscountCalculatorCard(viewModel, themeColors)
            ToolType.PROFIT_LOSS -> ProfitLossMarginCard(viewModel, themeColors)
            ToolType.VAT_TAX -> VatTaxCalculatorCard(viewModel, themeColors)
            ToolType.INTEREST -> InterestCalculatorCard(viewModel, themeColors)
            ToolType.AGE -> AgeCalculatorCard(viewModel, themeColors)
            ToolType.DATE_DIFF -> DateDifferenceCard(viewModel, themeColors)
            ToolType.PERCENTAGE -> PercentageCalculatorCard(viewModel, themeColors)
            ToolType.TIP -> TipCalculatorCard(viewModel, themeColors)
            ToolType.TEXT_COUNTER -> TextCounterCard(viewModel, themeColors)
            ToolType.PASSWORD_GENERATOR -> PasswordGeneratorCard(viewModel, themeColors)
            ToolType.ELECTRICITY_BILL -> ElectricityBillCalculatorCard(viewModel, themeColors)
            ToolType.APPLIANCE_COST -> ApplianceEnergyCostCard(viewModel, themeColors)
            ToolType.BATTERY_BACKUP -> BatteryBackupCard(viewModel, themeColors)
            ToolType.FUEL_COST -> FuelCostCalculatorCard(viewModel, themeColors)
            ToolType.SPEED_DISTANCE_TIME -> SpeedDistanceTimeCard(viewModel, themeColors)
            ToolType.GPA -> GpaCalculatorCard(viewModel, themeColors)
            ToolType.CGPA -> CgpaCalculatorCard(viewModel, themeColors)
            ToolType.TUITION_FEES -> TuitionFeesCalculatorCard(viewModel, themeColors)
            ToolType.ZAKAT -> ZakatCalculatorCard(viewModel, themeColors)
            ToolType.SAVINGS_TARGET -> SavingsTargetCard(viewModel, themeColors)
            ToolType.PREGNANCY_DUE -> PregnancyDueDateCard(viewModel, themeColors)
            ToolType.BLOOD_DONATION -> BloodDonationTrackerCard(viewModel, themeColors)
            ToolType.RESISTOR_CODE -> ResistorColorCodeCard(viewModel, themeColors)
            ToolType.COLOR_CONVERTER -> ColorConverterCard(viewModel, themeColors)
            ToolType.CLOTH_MEASUREMENT -> ClothMeasurementCard(viewModel, themeColors)
            ToolType.GOLD_CALCULATOR -> GoldCalculatorCard(viewModel, themeColors)
            ToolType.MARKET_LIST -> MarketListScreen(viewModel, themeColors)
            ToolType.STOPWATCH_TIMER -> StopwatchTimerCard(viewModel, themeColors)
            ToolType.NOTES_CHECKLIST -> NotesChecklistCard(viewModel, themeColors)
            ToolType.WORLD_CLOCK -> WorldClockCard(viewModel, themeColors)
            ToolType.UNIT_PRICE_COMPARER -> UnitPriceComparerCard(viewModel, themeColors)
            ToolType.SIMPLE_COMPASS -> SimpleCompassCard(viewModel, themeColors)
            ToolType.CAMERA_LEVEL -> com.example.ui.screens.tools.CameraLevelTool(viewModel, themeColors)
            ToolType.PDF_READER -> com.example.ui.screens.tools.PdfReaderTool(viewModel, themeColors, onBackClick = { viewModel.closeToolDetail() })
            ToolType.PDF_MAKER -> com.example.ui.screens.tools.PdfMakerTool(viewModel, themeColors, onBackClick = { viewModel.closeToolDetail() })
            ToolType.QR_BARCODE -> com.example.ui.screens.tools.QrBarcodeTool(viewModel, themeColors, onBackClick = { viewModel.closeToolDetail() })
            ToolType.ASPECT_RATIO -> AspectRatioCard(viewModel, themeColors)
            ToolType.RANDOM_NUMBER_PICKER -> RandomPickerCard(viewModel, themeColors)
            ToolType.MULTI_CALENDAR -> MultiCalendarCard(viewModel, themeColors)
            ToolType.PHOTO_LAB -> PhotoLabCard(viewModel, themeColors)
            ToolType.METAL_DETECTOR -> com.example.ui.screens.tools.MetalDetectorTool(viewModel, themeColors)
            ToolType.PHONE_DIAGNOSTICS -> com.example.ui.screens.tools.PhoneDiagnosticsTool(viewModel, themeColors)
            ToolType.DEVICE_INFO -> com.example.ui.screens.tools.DeviceInfoTool(viewModel, themeColors)
            ToolType.BATTERY_MONITOR -> com.example.ui.screens.tools.BatteryMonitorTool(viewModel, themeColors)
            ToolType.WEATHER -> DynamicWeatherScreen(viewModel, themeColors, isBn = viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI)
            ToolType.QIBLA_COMPASS -> QiblaCompassCard(viewModel, themeColors)
            ToolType.DIGITAL_TASBIH -> DigitalTasbihCard(viewModel, themeColors)
            ToolType.PRAYER_TIMES -> PrayerTimesCard(viewModel, themeColors)
            ToolType.SEHRI_IFTAR -> SehriIftarCard(viewModel, themeColors)
            ToolType.ISLAMIC_DUAS -> IslamicDuasCard(viewModel, themeColors)
            ToolType.HOLY_QURAN -> com.example.ui.quran.HolyQuranModuleScreen(
                themeColors = themeColors,
                onBackClick = { viewModel.selectedToolType = null },
                isBn = viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI
            )
            ToolType.NAMAZ_EDUCATION -> com.example.ui.namaz.NamazEducationScreen(themeColors = themeColors, onBackClick = { viewModel.selectedToolType = null })
            ToolType.HADITH_LIBRARY -> com.example.ui.islamic.HadithLibraryScreen(viewModel = viewModel, themeColors = themeColors, onBackClick = { viewModel.selectedToolType = null })
        }
    }
}

private fun getToolInfoItems(toolType: ToolType, isBn: Boolean): List<Pair<String, String>> {
    return when (toolType) {
        ToolType.BMI -> if (isBn) {
            listOf(
                "১. বিএমআই (BMI) কী?" to "বডি মাস ইনডেক্স বা বিএমআই হলো আপনার শরীরের চর্বির একটি সাধারণ অনুপাত, যা উচ্চতা ও ওজনের সাহায্যে নির্ণয় করা হয়। সূত্র: ওজন (কেজি) / উচ্চতা (মিটার) স্কয়ার।",
                "২. স্বাস্থ্য ঝুঁকি ও ক্যাটাগরি" to "• কম ওজন (<১৮.৫): পুষ্টিহীনতা ও কম রোগ প্রতিরোধ ক্ষমতার ঝুঁকি।\n• স্বাভাবিক ওজন (১৮.৫ - ২৪.৯): আদর্শ স্বাস্থ্যকর অবস্থা।\n• অতিরিক্ত ওজন (২৫ - ২৯.৯): ডায়াবেটিস ও উচ্চ রক্তচাপের ঝুঁকি।\n• স্থূলতা (৩০ বা বেশি): হৃদরোগ ও স্ট্রোকের তীব্র ঝুঁকি।"
            )
        } else {
            listOf(
                "1. What is BMI?" to "Body Mass Index (BMI) is a medical screening tool that estimates your body fat category based on your height and weight. Formula: BMI = weight (kg) / height (m²).",
                "2. Health Risk & Categories" to "• Underweight (<18.5): Risk of nutritional deficiency.\n• Normal (18.5-24.9): Optimal health, lowest risk.\n• Overweight (25-29.9): Increased risk of heart disease and diabetes.\n• Obese (30+): High risk of chronic metabolic conditions."
            )
        }
        ToolType.BMR -> if (isBn) {
            listOf(
                "১. বিএমআর (BMR) কী?" to "বেসাল মেটাবলিক রেট বা বিএমআর হলো আপনি যখন সম্পূর্ণ বিশ্রামে থাকেন তখন শরীরকে সচল ও বাঁচিয়ে রাখতে যে পরিমাণ ন্যূনতম ক্যালোরি প্রয়োজন।",
                "২. টিডিইই (TDEE) কী?" to "টোটাল DAILY এনার্জি এক্সপেন্ডিচার হলো সারাদিনের শারীরিক কার্যকলাপসহ আপনার মোট ক্যালরি ক্ষয়ের পরিমাণ। ওজন হ্রাস বা বৃদ্ধির জন্য এটি অত্যন্ত কার্যকরী।"
            )
        } else {
            listOf(
                "1. What is BMR?" to "Basal Metabolic Rate (BMR) represents the minimum amount of energy (calories) your body needs to survive and function while at complete rest (breathing, circulating blood, cellular recovery).",
                "2. What is TDEE?" to "Total Daily Energy Expenditure (TDEE) is the total calories you burn daily including physical exercise. It determines the calories needed to lose, gain, or maintain weight."
            )
        }
        ToolType.IDEAL_WEIGHT -> if (isBn) {
            listOf(
                "১. আদর্শ ওজন কী?" to "আদর্শ ওজন (IBW) হলো একটি গাণিতিক হিসাব (ডিভাইন বা রবিনসন ফর্মুলা) যা আপনার উচ্চতা ও লিঙ্গের ওপর ভিত্তি করে আপনার জন্য স্বাস্থ্যকর সর্বোচ্চ ও সর্বনিম্ন ওজনসীমা নির্ধারণ করে।",
                "২. এটি জানা কেন জরুরী?" to "আদর্শ ওজনসীমা বজায় রাখলে আপনার হাড়ের জোড়া, ফুসফুস ও হৃদপিণ্ডের ওপর অতিরিক্ত চাপ পড়ে না এবং দীর্ঘায়ু লাভে সাহায্য করে।"
            )
        } else {
            listOf(
                "1. What is Ideal Body Weight?" to "Ideal Body Weight (IBW) uses standard formulas (Devine, Robinson, or Miller) to estimate a healthy target weight range based on your biological gender and height.",
                "2. How is it calculated?" to "For men, it starts from a baseline at 5 feet and adds a set weight per inch of height. Maintaining this range prevents excessive strain on joints and organs."
            )
        }
        ToolType.WATER_INTAKE -> if (isBn) {
            listOf(
                "১. দৈনিক পানি পানের লক্ষ্য" to "আমাদের দেহের প্রায় ৬০% পানি। একজন প্রাপ্তবয়স্ক মানুষের দৈনিক কমপক্ষে ২ থেকে ৩ লিটার (৮-১২ গ্লাস) পানি পান করা প্রয়োজন।",
                "২. পানি পানের উপকারিতা" to "পর্যাপ্ত পানি পান করলে হজমশক্তি বাড়ে, ত্বক সতেজ থাকে, কিডনি সচল থাকে এবং শরীর থেকে টক্সিন বা ক্ষতিকর উপাদান বের হয়ে যায়।"
            )
        } else {
            listOf(
                "1. Daily Hydration Target" to "Your body is about 60% water. Generally, a sedentary adult needs around 2 to 3 liters (8-12 glasses) of water daily to maintain proper hydration.",
                "2. Factors affecting water needs" to "Hot weather, physical exercise, pregnancy, and high-sodium diets increase your water demand. Drink water before feeling excessively thirsty."
            )
        }
        ToolType.PREGNANCY_DUE -> if (isBn) {
            listOf(
                "১. সম্ভাব্য প্রসবের তারিখ (EDD) হিসাব পদ্ধতি" to "গর্ভাবস্থার সাধারণ স্থায়িত্ব শেষ পিরিয়ডের প্রথম দিন (LMP) থেকে ৪০ সপ্তাহ বা ২৮০ দিন ধরা হয়। এই ক্যালকুলেটরটি LMP এর সাথে ২৮০ দিন যোগ করে সম্ভাব্য প্রসবের তারিখ (EDD) নির্ধারণ করে।",
                "২. গর্ভাবস্থার ৩টি ট্রাইমেস্টার বা ধাপ" to "• ১ম ট্রাইমেস্টার (১-১২ সপ্তাহ): ভ্রূণ গঠন শুরু হয়। ক্লান্তি, বমি বমি ভাব, স্তনে সংবেদনশীলতা দেখা দেয়।\n• ২য় ট্রাইমেস্টার (১৩-২৬ সপ্তাহ): পেটের আকার বৃদ্ধি পায়, বাচ্চার নড়াচড়া অনুভূত হয় (সাধারণত ১৮-২০ সপ্তাহে)।\n• ৩য় ট্রাইমেস্টার (২৭-৪০ সপ্তাহ): বাচ্চার দ্রুত বৃদ্ধি ও প্রসবের প্রস্তুতি শুরু হয়। ঘন ঘন প্রস্রাব ও পিঠের ব্যথা হতে পারে।",
                "৩. গর্ভবতী মায়ের জন্য প্রয়োজনীয় পুষ্টি ও উপদেশ" to "• ফলিক অ্যাসিড ও আয়রন: বাচ্চার জন্মগত ত্রুটি রোধে এবং রক্তস্বল্পতা দূর করতে চিকিৎসকের পরামর্শে আয়রন ও ফলিক অ্যাসিড নিন।\n• সুষম খাবার: শাকসবজি, ডিম, দুধ, ফলমূল, ডাল এবং পর্যাপ্ত প্রোটিনসমৃদ্ধ খাবার খান।\n• পর্যাপ্ত বিশ্রাম: দৈনিক ৮ ঘণ্টা ঘুম ও দুপুরে ২ ঘণ্টা বিশ্রাম নেওয়া উচিত।\n• হাইড্রেশন: প্রতিদিন অন্তত ৩ লিটার পানি পান করুন।",
                "৪. গর্ভকালীন বিপদ চিহ্ন বা জরুরী লক্ষণ" to "নিচের লক্ষণগুলো দেখা দিলে দ্রুত চিকিৎসকের শরণাপন্ন হোন:\n• যোনিপথে রক্তপাত বা অতিরিক্ত তরল নির্গমন\n• তীব্র পেটে ব্যথা বা মাথা ঘোরা\n• হঠাৎ হাত-পা বা মুখ ফুলে যাওয়া\n• বাচ্চার নড়াচড়া কমে যাওয়া বা বন্ধ হওয়া\n• তীব্র জ্বর বা অনবরত বমি হওয়া"
            )
        } else {
            listOf(
                "1. How EDD is Calculated" to "Human pregnancy is calculated from the first day of your Last Menstrual Period (LMP) and typically lasts 40 weeks (280 days). The calculator uses Naegele's Rule: LMP + 280 Days to find the Estimated Due Date (EDD).",
                "2. The Three Trimesters of Pregnancy" to "• First Trimester (Weeks 1-12): Core baby organs form. Common symptoms include fatigue, nausea (morning sickness), and breast tenderness.\n• Second Trimester (Weeks 13-26): Known as the golden period. Baby's movements are often felt (weeks 18-20). Energy levels return.\n• Third Trimester (Weeks 27-40): Baby grows rapidly. High pressure on the bladder, backaches, and pre-labor Braxton Hicks contractions may occur.",
                "3. Essential Advice & Nutrition" to "• Supplementation: Take Folic Acid and Iron under medical supervision to prevent neural tube defects and anemia.\n• Balanced Diet: Eat protein-rich foods, leafy greens, dairy, eggs, and fresh fruits.\n• Rest & Sleep: Aim for 8 hours of night sleep and 2 hours of afternoon rest.\n• Hydration: Drink at least 3 liters of water daily.",
                "4. Pregnancy Danger Signs" to "Contact a doctor immediately if you experience:\n• Vaginal bleeding or fluid leakage\n• Severe abdominal pain or persistent headache\n• Sudden swelling of face, hands, or feet\n• Reduced or absent baby movements\n• High fever or uncontrolled vomiting"
            )
        }
        ToolType.BLOOD_DONATION -> if (isBn) {
            listOf(
                "১. রক্তদানের সময়সীমা" to "একজন সুস্থ পুরুষ প্রতি ৩ মাস (৯০ দিন) পর পর এবং একজন সুস্থ নারী প্রতি ৪ মাস (১২০ দিন) পর পর নিরাপদভাবে রক্তদান করতে পারেন।",
                "২. রক্তদানের যোগ্যতা" to "• বয়স: ১৮ থেকে ৬০ বছর\n• ওজন: কমপক্ষে ৪৫ থেকে ৫০ কেজি\n• শারীরিক অবস্থা: রক্তচাপ স্বাভাবিক থাকতে হবে এবং কোনো সংক্রামক রোগ থাকা যাবে না।"
            )
        } else {
            listOf(
                "1. Blood Donation Interval" to "Healthy adults can donate whole blood every 90 days (3 months) for men and 120 days (4 months) for women to allow iron levels to replenish.",
                "2. Eligibility Requirements" to "• Age: 18 - 60 years old\n• Weight: At least 45 - 50 kg\n• Health: Normal blood pressure, no active infections, and hemogloblin level above 12.5 g/dL."
            )
        }
        ToolType.ZAKAT -> if (isBn) {
            listOf(
                "১. যাকাত কেন ফরজ?" to "যাকাত ইসলামের অন্যতম ফরজ স্তম্ভ। নিساب পরিমাণ (সাড়ে ৭ ভরি সোনা বা সাড়ে ৫২ ভরি রুপা বা সমমূল্যের নগদ অর্থ) বছর শেষে থাকলে ২.৫% হারে যাকাত দেওয়া বাধ্যতামূলক। এটি সম্পদ পবিত্র করে এবং অভাবীদের সাহায্য করে।",
                "২. কোন কোন সম্পদের যাকাত দিতে হবে?" to "• নগদ টাকা ও ব্যাংকে জমানো অর্থ\n• সোনা ও রুপা (ব্যবহার্য বা অলংকার)\n• ব্যবসায়িক পণ্য বা স্টক\n• শেয়ার বা স্টক মার্কেটে বিনিয়োগ\n• উসুলযোগ্য পাওনা ঋণ",
                "৩. কি কি যাকাতের আওতামুক্ত?" to "• নিজের বসবাসের ঘরবাড়ি ও ব্যবহারের গাড়ি\n• পরিধেয় পোশাক ও নিত্য ব্যবহার্য আসবাবপত্র\n• পেশাগত কাজের প্রয়োজনীয় যন্ত্রপাতি\n• নিساب পরিমাণের কম সোনা বা রুপা\n• পরিশোধযোগ্য ব্যক্তিগত দেনা বা দায়সমূহ",
                "৪. যাকাত পাওয়ার যোগ্য খাতসমূহ" to "পবিত্র কুরআনে নির্ধারিত ৮টি প্রধান খাত:\n• ফকির ও মিসকিন (অভাবী ও নিঃস্ব)\n• যাকাত আদায়ে নিয়োজিত কর্মচারী\n• ইসলামের প্রতি আকৃষ্ট ব্যক্তি\n• দাস বা বন্দী মুক্তি\n• ঋণগ্রস্ত ব্যক্তি\n• আল্লাহর সন্তুষ্টির পথে ও জনকল্যাণে\n• মুসাফির বা অসহায় পথিক"
            )
        } else {
            listOf(
                "1. Why is Zakat mandatory?" to "Zakat is one of the pillars of Islam. It is compulsory for every Muslim whose wealth exceeds the Nisab threshold (equivalent to 87.48g of gold or 612.36g of silver) for a lunar year. It purifies wealth and guarantees social security for the poor.",
                "2. What assets require Zakat?" to "• Cash on hand or savings in bank accounts\n• Gold and silver ornaments/investments\n• Business stock and merchandise\n• Shares, mutual funds, or stock investments\n• Receivables/strong debts owed to you",
                "3. What assets are exempt?" to "• Personal residence and primary vehicles\n• Daily wear clothes and home furniture\n• Tools used for professional trade/work\n• Gold or silver below the Nisab threshold\n• Payable personal debts and liabilities",
                "4. Eligible Recipients (Asnaf)" to "The 8 categories defined in the Holy Quran:\n• Al-Fuqara (the extremely poor) & Al-Masakin (the needy)\n• Zakat administrators/collectors\n• Those whose hearts are to be reconciled\n• Freeing captives/slaves\n• Debt-ridden individuals\n• In the cause of Allah (social/educational benefits)\n• Stranded travelers in need"
            )
        }
        ToolType.CLOTH_MEASUREMENT -> if (isBn) {
            listOf(
                "১. দেশীয় গজের পরিমাপ" to "বাঙালি সংস্কৃতিতে কাপড় মাপার ঐতিহ্যবাহী গজ-গিরা ব্যবহৃত হয়।\n• ১ গজ = ৩৬ ইঞ্চি = ৩ ফুট\n• ১ গজ = ২ হাত (১ হাত = ১৮ ইঞ্চি)\n• ১ গজ = ১৬ গিরা",
                "২. গিরা ও ইঞ্চি সম্পর্ক" to "• ১ গিরা = ২.২৫ ইঞ্চি (২ ১/৪ ইঞ্চি)\n• ২ গিরা = ৪.৫ ইঞ্চি\n• ৪ গিরা = ৯ ইঞ্চি (১/৪ গজ)\n• ৮ গিরা = ১৮ ইঞ্চি (১/২ গজ বা ১ হাত)\n• ১২ গিরা = ২৭ ইঞ্চি (৩/৪ গজ)"
            )
        } else {
            listOf(
                "1. Bengali Traditional Gaj Units" to "Gaj, Gira, and Haat are traditional South Asian units for measuring textiles.\n• 1 Gaj = 1 Yard = 36 Inches = 3 Feet\n• 1 Gaj = 2 Haat (1 Haat = 18 Inches)\n• 1 Gaj = 16 Gira",
                "2. Gira to Inches breakdown" to "• 1 Gira = 2.25 Inches\n• 4 Gira = 9 Inches (1/4 Gaj)\n• 8 Gira = 18 Inches (1/2 Gaj or 1 Haat)\n• 12 Gira = 27 Inches (3/4 Gaj)\n• 16 Gira = 36 Inches (1 Gaj)"
            )
        }
        ToolType.GOLD_CALCULATOR -> if (isBn) {
            listOf(
                "১. স্বর্ণ পরিমাপের ভরি-আনা-রতি" to "বাংলাদেশে সনাতন পদ্ধতিতে স্বর্ণ ও রৌপ্য পরিমাপ করা হয়:\n• ১ ভরি (Tola) = ১১.৬৬৪ গ্রাম\n• ১ ভরি = ১৬ আনা\n• ১ আনা = ৬ রতি\n• ১ রতি = ১০ পয়েন্ট\n• ১ ভরি = ৯৬ রতি = ৯৬০ পয়েন্ট",
                "২. স্বর্ণের ক্যারেট (Carat) কি?" to "ক্যারেট স্বর্ণের বিশুদ্ধতা নির্দেশ করে:\n• ২২ ক্যারেট: ৯১.৬% বিশুদ্ধ স্বর্ণ (অলংকার তৈরির জন্য সেরা)\n• ২১ ক্যারেট: ৮৭.৫% বিশুদ্ধ স্বর্ণ\n• ১৮ ক্যারেট: ৭৫% বিশুদ্ধ স্বর্ণ\n• ২৪ ক্যারেট: ৯৯.৯% খাঁটি স্বর্ণ (খুব নরম, অলংকার করা যায় না)"
            )
        } else {
            listOf(
                "1. Traditional Gold Weights" to "Gold and silver in Bangladesh are measured in Vori, Anna, Ratti, and Point:\n• 1 Vori (Tola) = 11.664 Grams\n• 1 Vori = 16 Anna\n• 1 Anna = 6 Ratti\n• 1 Ratti = 10 Points\n• 1 Vori = 96 Ratti = 960 Points",
                "2. Carat & Gold Purity" to "Carat measures the purity of gold:\n• 22 Carat: 91.6% pure gold (Ideal for high-end ornaments)\n• 21 Carat: 87.5% pure gold\n• 18 Carat: 75.0% pure gold\n• 24 Carat: 99.9% pure gold (Raw gold bar/coin, too soft for jewelry)"
            )
        }
        ToolType.EMI_LOAN -> if (isBn) {
            listOf(
                "১. ইএমআই (EMI) কী?" to "ইএমআই হলো প্রতি মাসে ব্যাংক বা ঋণদাতাকে পরিশোধ করা একটি নির্দিষ্ট কিস্তি। এর মাধ্যমে মূল টাকা এবং ঋণের সুদ ধীরে ধীরে শোধ করা হয়।",
                "২. কিস্তির হিসাবের ফর্মুলা" to "EMI = [P x R x (1+R)^N]/[(1+R)^N - 1] যেখানে P হলো লোনের পরিমাণ, R হলো মাসিক সুদের হার এবং N হলো মোট মাসের संख्या।"
            )
        } else {
            listOf(
                "1. Equated Monthly Installment (EMI)" to "EMI is a fixed payment amount made by a borrower to a lender at a specified date each month. It repays both the principal loan amount and accrued interest.",
                "2. Monthly EMI Formula" to "EMI = [P x R x (1+R)^N]/[(1+R)^N - 1] where P is the Loan Amount, R is the monthly interest rate (annual rate / 12 / 100), and N is the loan tenure in months."
            )
        }
        ToolType.INTEREST -> if (isBn) {
            listOf(
                "১. সরল বনাম চক্রবৃদ্ধি সুদ" to "• সরল সুদ: শুধুমাত্র মূল টাকার ওপর নির্দিষ্ট হারে সুদ ধার্য করা হয়।\n• চক্রবৃদ্ধি সুদ: পূর্ববর্তী সময়ের সুদের ওপরও পুনরায় সুদ হিসাব করা হয় (সুদ-আসল)।",
                "২. চক্রবৃদ্ধির সময়কাল" to "সুদ যত ঘন ঘন চক্রবৃদ্ধি (মাসিক, ত্রৈমাসিক বা বার্ষিক) হবে, মেয়াদ শেষে প্রাপ্ত মোট লভ্যাংশ বা সুদের পরিমাণ তত বৃদ্ধি পাবে।"
            )
        } else {
            listOf(
                "1. Simple vs Compound Interest" to "• Simple: Computed only on the principal amount.\n• Compound: 'Interest on interest' - calculated on principal plus previously accumulated interest over intervals.",
                "2. Frequency of Compounding" to "The more frequently interest is compounded (annually, monthly, daily), the higher the total return will be over time."
            )
        }
        ToolType.SAVINGS_TARGET -> if (isBn) {
            listOf(
                "১. লক্ষ্যভিত্তিক সঞ্চয়" to "একটি নির্দিষ্ট লক্ষ্য (যেমন: গাড়ি কেনা বা পড়াশোনা) অর্জনের জন্য প্রতি মাসে কত টাকা করে জমানো উচিত তা বের করাই হলো সেভিংস টার্গেটের কাজ।",
                "২. মূল্যস্ফীতি বিবেচনা" to "দীর্ঘমেয়াদী কোনো লক্ষ্য থাকলে ভবিষ্যতের সম্ভাব্য মূল্যবৃদ্ধির বিষয়টি মাথায় রেখে সঞ্চয়ের লক্ষ্যমাত্রা কিছুটা বাড়িয়ে রাখা সুবিধা দেবে।"
            )
        } else {
            listOf(
                "1. Goal-Oriented Savings" to "A savings target calculator determines how much you must put aside regularly (weekly/monthly) to reach a specific financial goal within a timeframe.",
                "2. Adjusting for Inflation" to "When planning long-term goals (like buying a house in 10 years), factor in price increases to ensure your target is sufficient."
            )
        }
        ToolType.ELECTRICITY_BILL -> if (isBn) {
            listOf(
                "১. বিদ্যুৎ বিল কীভাবে হিসাব করা হয়?" to "বিদ্যুৎ বিল ব্যবহারের পরিমাণ (ইউনিট বা কিলোওয়াট-ঘণ্টা) অনুযায়ী করা হয়। ১ ইউনিট = ১০০০ ওয়াট ক্ষমতার কোনো যন্ত্রপাতি ১ ঘণ্টা চললে যে শক্তি ব্যয় হয়।",
                "২. ট্যারিফ স্ল্যাব ও বিলিং" to "বিদ্যুৎ ব্যবহারের পরিমাণের ওপর ভিত্তি করে স্ল্যাব রেট পরিবর্তিত হয়। ফলে বেশি ইউনিট ব্যবহার করলে প্রতি ইউনিটের মূল্য বেড়ে যায়।"
            )
        } else {
            listOf(
                "1. How is Electricity Bill calculated?" to "Bills are calculated based on energy consumption in kilowatt-hours (kWh), where 1 unit = 1000W of electrical power consumed for 1 hour.",
                "2. Tariff Slabs & Demand Charge" to "Many regions use progressive step tariffs (slabs) where the unit rate increases with higher usage, alongside fixed demand charges."
            )
        }
        ToolType.APPLIANCE_COST -> if (isBn) {
            listOf(
                "১. নির্দিষ্ট যন্ত্রের বিদ্যুৎ খরচ" to "যেকোনো যন্ত্রের খরচ বের করার সূত্র: `খরচ = (ওয়াট * দৈনিক ব্যবহারের ঘণ্টা * ইউনিট রেট) / ১০০০`। এর মাধ্যমে কোন ডিভাইসে বেশি কারেন্ট পুড়ছে তা জানা যায়।",
                "২. ইনভার্টার প্রযুক্তির সুবিধা" to "ইনভার্টার সমৃদ্ধ ফ্রিজ বা এসি মোটর গতি নিয়ন্ত্রণ করার মাধ্যমে সাধারণ মোটরের চেয়ে প্রায় ৩০-৫০% বিদ্যুৎ সাশ্রয় করতে পারে।"
            )
        } else {
            listOf(
                "1. Calculating Individual Energy Cost" to "To find an appliance's cost: `Cost = (Watts * Hours * Unit Rate) / 1000`. This helps identify which appliances are draining the most electricity.",
                "2. Energy Star Ratings" to "Modern appliances with high energy efficiency ratings (like 5-star ACs or inverter fridges) use significantly less power."
            )
        }
        ToolType.BATTERY_BACKUP -> if (isBn) {
            listOf(
                "১. ব্যাটারি ব্যাকআপ সময়কাল" to "ব্যাটারির ধারণক্ষমতা মাপা হয় এম্পিয়ার-আওয়ার (Ah) দিয়ে। আইপিএস বা ব্যাটারির স্থায়িত্ব বের করার সূত্র: `ব্যাকআপ সময় = (Ah * ভোল্টেজ * দক্ষতা) / মোট লোড ওয়াট`।",
                "২. ব্যাটারির আয়ু বাড়ানোর উপায়" to "ব্যাটারি সম্পূর্ণ খালি (০%) হওয়া এড়াতে হবে। নিয়মিত পানি চেক করা এবং সঠিক ভোল্টেজে চার্জ করা ব্যাটারির দীর্ঘস্থায়িত্ব নিশ্চিত করে।"
            )
        } else {
            listOf(
                "1. Battery Runtime & Capacity" to "Battery capacity is measured in Ampere-Hours (Ah). Runtime depends on load size: `Backup (Hours) = (Battery Ah * Voltage * Efficiency) / Load (W)`.",
                "2. Preserving Battery Health" to "To extend lifespan, avoid draining lead-acid or lithium batteries to 0%. Maintain shallow discharge cycles and check water levels in IPS batteries."
            )
        }
        ToolType.COLOR_CONVERTER -> if (isBn) {
            listOf(
                "১. কালার কোডের প্রকারভেদ" to "• HEX: ওয়েবসাইট ডিজাইনে ব্যবহৃত ৬ অক্ষরের কোড (যেমন: #FFFFFF)।\n• RGB: লাল, সবুজ ও নীল রঙের তীব্রতার অনুপাত (০-২৫৫)।\n• HSL: হিউ বা রঙ (০-৩৬০°), স্যাচুরেশন (০-১০০%) এবং উজ্জ্বলতা (০-১০০%)।"
            )
        } else {
            listOf(
                "1. Color Models Explained" to "• HEX: Standard 6-character code (e.g., #FFFFFF) used in CSS and web design.\n• RGB: Represents Red, Green, and Blue intensities from 0 to 255.\n• HSL: Focuses on Hue (0-360°), Saturation (0-100%), and Lightness (0-100%)."
            )
        }
        ToolType.RESISTOR_CODE -> if (isBn) {
            listOf(
                "১. রেজিস্টর কালার কোড ডিকোডিং" to "রেজিস্টর বা রোধক বিদ্যুৎ প্রবাহকে নিয়ন্ত্রণ করে। রেজিস্টরের গায়ের রঙিন ব্যান্ডগুলো যথাক্রমে সংখ্যা, গুণক এবং সহনশীলতা (Tolerance) নির্দেশ করে।"
            )
        } else {
            listOf(
                "1. Decoding Resistor Bands" to "Resistors limit electric current flow. The color bands represent digits, multipliers, and tolerance (accuracy level) specified by standard EIA-RS-279."
            )
        }
        ToolType.DISCOUNT -> if (isBn) {
            listOf(
                "১. ডিসকাউন্ট ও সাশ্রয়" to "ডিসকাউন্ট হলো মূল দামের ওপর নির্দিষ্ট পরিমাণ ছাড়। কত শতাংশ ছাড় দেওয়া হলো তা বের করে সহজে সাশ্রয়ের পরিমাণ হিসাব করা যায়।"
            )
        } else {
            listOf(
                "1. Discount & Savings Rate" to "Discounts represent a deduction from the original price of goods. Calculating the savings percentage helps evaluate the true bargain of a deal."
            )
        }
        ToolType.PROFIT_LOSS -> if (isBn) {
            listOf(
                "১. লাভ ও ক্ষতি মার্জিন" to "ব্যবসায়ের ক্রয়মূল্য এবং বিক্রয়মূল্যের পার্থক্য থেকে লাভ বা ক্ষতি নির্ধারিত হয়। লাভ বা ক্ষতির হার বা মার্জিন দিয়ে ব্যবসার কার্যকারিতা মাপা হয়।"
            )
        } else {
            listOf(
                "1. Profit & Loss Margin" to "Profit Margin determines how much money a business makes relative to its cost. Markup is the percentage added to cost to determine the selling price."
            )
        }
        ToolType.VAT_TAX -> if (isBn) {
            listOf(
                "১. মূল্য সংযোজন কর (ভ্যাট)" to "ভ্যাট হলো ভোগ বা সেবার ওপর আরোপিত কর। কোনো পণ্যের উৎপাদন ও বিক্রয়ের প্রতিটি ধাপে যে মূল্য যুক্ত হয় তার ওপর এটি ধার্য করা হয়।"
            )
        } else {
            listOf(
                "1. Value Added Tax (VAT)" to "VAT is a consumption tax placed on a product whenever value is added at each stage of production and at final point of retail."
            )
        }
        ToolType.AGE -> if (isBn) {
            listOf(
                "১. সুনির্দিষ্ট বয়স নির্ণয়" to "জন্মতারিখ থেকে বর্তমান সময়ের মধ্যকার সময়কে বছর, মাস ও দিনে রূপান্তর করে আপনার নিখুঁত বয়স বের করা হয়।",
                "২. পরবর্তী জন্মদিন" to "আপনার পরবর্তী জন্মদিনে পৌঁছাতে আর কত মাস ও দিন বাকি আছে এবং সেই দিনটি কী বার হবে তা নিখুঁতভাবে হিসেব করে।"
            )
        } else {
            listOf(
                "1. Age Calculation Details" to "Age is counted as the total elapsed time since birth. This tool breaks down your age into years, months, and days for exact precision.",
                "2. Next Birthday Countdown" to "It calculates the exact number of months and days remaining until your next birthday and shows the weekday on which it will fall."
            )
        }
        ToolType.DATE_DIFF -> if (isBn) {
            listOf(
                "১. তারিখের ব্যবধান" to "যেকোনো দুটি ক্যালেন্ডার তারিখের মধ্যবর্তী মোট দিন, সপ্তাহ এবং মাসের সুনির্দিষ্ট ব্যবধান বের করতে এটি ব্যবহৃত হয়।"
            )
        } else {
            listOf(
                "1. Date Interval Analysis" to "This tool calculates the absolute span between two selected calendar dates, highlighting the total elapsed days, weeks, or months."
            )
        }
        ToolType.PERCENTAGE -> if (isBn) {
            listOf(
                "১. শতকরা বা পার্সেন্টেজ" to "শতকরা বা পার্সেন্টেজ হলো ১০০ ভাগের একটি অংশ। দৈনন্দিন হিসাব, ছাড়, কর, এবং বিজ্ঞান গবেষণায় এটি বহুল ব্যবহৃত।"
            )
        } else {
            listOf(
                "1. Understanding Percentages" to "Percentage represents a rate or number out of 100. It is essential for tax calculations, scientific data, and performance comparisons."
            )
        }
        ToolType.TIP -> if (isBn) {
            listOf(
                "১. রেস্তোরাঁ টিপস ও বিল বণ্টন" to "টিপস হলো রেস্তোরাঁর কর্মীদের ভালো সেবার জন্য খুশি হয়ে দেওয়া অতিরিক্ত অর্থ। সাধারণত মোট বিলের ১০% থেকে ১৫% টিপ দেওয়া হয়।",
                "২. বিল ভাগ করা" to "বন্ধুদের নিয়ে খাওয়া-দাওয়ার পর কর (Tax) এবং টিপসসহ মোট বিলটি সবার মাঝে সমানভাগে ভাগ করতে এই ক্যালকুলেটরটি সাহায্য করে।"
            )
        } else {
            listOf(
                "1. Restaurant Etiquette & Gratuity" to "Tipping is a gesture of appreciation for services rendered. Customary tipping rates range between 10% to 20% of the total bill.",
                "2. Splitting Bills Fairly" to "When dining with friends, this tool splits the total cost (including taxes and custom tip percentages) equally among the specified group."
            )
        }
        ToolType.TEXT_COUNTER -> if (isBn) {
            listOf(
                "১. শব্দ ও অক্ষর সংখ্যা সীমা" to "সামাজিক যোগাযোগ মাধ্যম বা বিভিন্ন চাকুরির আবেদনে প্যারাগ্রাফ বা লেখার একটি নির্দিষ্ট অক্ষর ও শব্দ সীমা থাকে, যা বজায় রাখা দরকার।"
            )
        } else {
            listOf(
                "1. Word & Character Limits" to "Many platforms (Twitter, academic papers, online job applications) impose strict character or word limits. Tracking text length is crucial."
            )
        }
        ToolType.PASSWORD_GENERATOR -> if (isBn) {
            listOf(
                "১. শক্তিশালী পাসওয়ার্ডের শর্ত" to "একটি নিরাপদ পাসওয়ার্ডে কমপক্ষে ১২-১৬টি অক্ষর থাকা উচিত, যেখানে বড় ও ছোট হাতের অক্ষর, সংখ্যা এবং বিশেষ প্রতীক (@, #, $) মিশ্রিত থাকবে।"
            )
        } else {
            listOf(
                "1. Password Strength Criteria" to "A strong password contains at least 12-16 characters, combining uppercase and lowercase letters, numbers, and special symbols."
            )
        }
        ToolType.FUEL_COST -> if (isBn) {
            listOf(
                "১. ভ্রমণের জ্বালানি খরচ হিসাব" to "আপনার ট্রিপের জ্বালানি খরচ বের করতে মোট দূরত্ব, গাড়ির মাইলেজ (প্রতি লিটারে কত কিমি যায়) এবং জ্বালানির বর্তমান লিটার প্রতি মূল্য প্রয়োজন।"
            )
        } else {
            listOf(
                "1. Trip Fuel Estimation" to "Calculating fuel cost requires: Total Distance, Vehicle's Fuel Efficiency (mileage), and current fuel price per liter."
            )
        }
        ToolType.SPEED_DISTANCE_TIME -> if (isBn) {
            listOf(
                "১. গতি, দূরত্ব ও সময় সম্পর্ক" to "গতির মূল সূত্র হলো: `দূরত্ব = গতিবেগ * সময়`। যেকোনো দুটি মান জানা থাকলে খুব সহজে অপর মানটি নির্ভুলভাবে বের করা যায়।"
            )
        } else {
            listOf(
                "1. The Motion Triangle" to "Relates three fundamental values of motion: `Distance = Speed x Time`. Knowing any two allows you to calculate the third accurately."
            )
        }
        ToolType.GPA -> if (isBn) {
            listOf(
                "১. জিপিএ (GPA) গণনা" to "জিপিএ হলো এক সেমিস্টার বা বিষয়ের একাডেমিক অর্জনের গড় হার। মোট অর্জিত গ্রেড পয়েন্টকে মোট ক্রেডিট দিয়ে ভাগ করে জিপিএ বের করা হয়।"
            )
        } else {
            listOf(
                "1. Grade Point Average (GPA)" to "GPA measures academic achievement for a single term or semester. It divides total grade points earned by the total credits attempted."
            )
        }
        ToolType.CGPA -> if (isBn) {
            listOf(
                "১. সিজিপিএ (CGPA) গণনা" to "সিজিপিএ হলো একাধিক সেমিস্টার বা শিক্ষাবর্ষের অর্জিত জিপিএ-র গড় মান। এটি আপনার পুরো শিক্ষা জীবনের সামগ্রিক মেধার প্রতিফলন দেখায়।"
            )
        } else {
            listOf(
                "1. Cumulative GPA (CGPA)" to "CGPA calculates your overall academic performance across multiple semesters. It weighs each semester's GPA by its credit hours."
            )
        }
        ToolType.TUITION_FEES -> if (isBn) {
            listOf(
                "১. সেমিস্টার টিউশন ফি" to "মোট সেমিস্টার ফি সাধারণত ক্রেডিট প্রতি ফি, রেজিস্ট্রেশন চার্জ, ল্যাব বা লাইব্রেরি ফি এবং অন্যান্য আনুসাঙ্গিক খরচের সমষ্টি।"
            )
        } else {
            listOf(
                "1. Semester Tuition Fees Breakdown" to "Total fees include: credit hour tuition charges, semester admission/registration fees, laboratory or activity costs, and library charges."
            )
        }
        ToolType.MULTI_CALENDAR -> if (isBn) {
            listOf(
                "১. ট্রিপল স্মার্ট ক্যালেন্ডার" to "এই ক্যালেন্ডারে একই সাথে ইংরেজি (গ্রেগরিয়ান), বাংলা (সৌর) এবং আরবি (হিজরী) বর্ষপঞ্জি সমন্বিতভাবে প্রদর্শিত হয়।",
                "২. বাংলা বর্ষপঞ্জি নিয়ম (বাংলা একাডেমি)" to "প্রথম ৫ মাস (বৈশাখ-ভাদ্র) ৩১ দিনে এবং পরের ৭ মাস (আশ্বিন-চৈত্র) ৩০ দিনে হিসাব করা হয়। অধিবর্ষে চৈত্র মাস ৩১ দিনে হয়।",
                "৩. হিজরী/আরবি বর্ষপঞ্জি নিয়ম" to "হিজরী বর্ষপঞ্জি সম্পূর্ণ চাঁদ দেখার ওপর নির্ভর করে (৩৫৪/৩৫৫ দিন)। স্থানভেদে চাঁদ দেখার ভিত্তিতে ১ দিনের তারতম্য হতে পারে।",
                "৪. কালার ডেকোরেশন লেজেন্ড" to "• প্রধান সংখ্যা: ইংরেজি গ্রেগরিয়ান তারিখ\n• সবুজ সংখ্যা: বাংলা সৌর বর্ষপঞ্জির তারিখ\n• হলুদ/অ্যাম্বার সংখ্যা: আরবি হিজরী বর্ষপঞ্জির তারিখ"
            )
        } else {
            listOf(
                "1. Smart Triple Calendar Overview" to "Displays Gregorian (English), Solar Bengali, and Lunar Hijri (Islamic) calendar dates simultaneously in a unified layout.",
                "2. Bengali Calendar Rule" to "According to Bangla Academy revised calendar: The first 5 months (Baishakh-Bhadra) have 31 days, and the last 7 months (Ashwin-Chaitra) have 30 days (Chaitra has 31 days in a leap year).",
                "3. Hijri Calendar Rule" to "The Islamic calendar is purely lunar (354/355 days per year). Local moon sightings may cause a 1-day variance.",
                "4. Color Legend" to "• Primary Number: English Gregorian Day\n• Green Accent: Bengali Solar Day\n• Amber/Gold: Hijri Lunar Day"
            )
        }
        ToolType.WEATHER -> if (isBn) {
            listOf(
                "১. লাইভ আবহাওয়া ও পূর্বাভাস" to "আপনার বর্তমান অবস্থান বা বিশ্বের যেকোনো শহরের বর্তমান তাপমাত্রা, বাতাসের গতি, আর্দ্রতা, দৃশ্যমানতা এবং বায়ুর চাপ দেখতে পারবেন।",
                "২. ৭ দিনের আবহাওয়ার পূর্বাভাস" to "আগামী ৭ দিনের সম্ভাব্য রোদ, বৃষ্টি, মেঘ বা বজ্রসহ ঝড়ের পূর্বাভাস জানতে পারবেন যা আপনার ভ্রমণ ও দৈনন্দিন কাজের পরিকল্পনায় সহায়ক।"
            )
        } else {
            listOf(
                "1. Live Weather & Metrics" to "View real-time temperature, condition, humidity, wind speed, visibility, and atmospheric pressure for your current location or global cities.",
                "2. 7-Day Forecast" to "Plan your outdoor trips and work schedules with accurate daily weather forecasts, high/low temperatures, and precipitation probabilities."
            )
        }
        ToolType.STOPWATCH_TIMER -> if (isBn) {
            listOf(
                "১. নিখুঁত স্টপওয়াচ ও ল্যাপ রেকর্ড" to "মিলিসেকেন্ড পর্যন্ত নিখুঁত স্টপওয়াচ দিয়ে দৌড়, ওয়ার্কআউট বা পড়ার সময় গণনা করুন। প্রতি ল্যাপের সময় আলাদাভাবে সংরক্ষণ করা যায়।",
                "২. কাউন্টডাউন টাইমার" to "রান্না, পড়া বা মেডিটেশনের জন্য সময় নির্ধারণ করে সাউন্ড বা অ্যালার্ম নোটিফিকেশন সহ টাইমার সেট করুন।"
            )
        } else {
            listOf(
                "1. Precision Stopwatch & Laps" to "Count elapsed time down to milliseconds for sports, workouts, or study sessions with individual lap time tracking.",
                "2. Countdown Timer" to "Set customizable countdown alarms for cooking, pomodoro study focus, or workouts with clear audio/visual alerts."
            )
        }
        ToolType.NOTES_CHECKLIST -> if (isBn) {
            listOf(
                "১. কুইক মেমো ও চেকলিস্ট" to "জরুরি হিসাব-নিকাশ, কেনাকাটার তালিকা বা আইডিয়া দ্রুত লিখে রাখুন। কাজ শেষ হলে চেকলিস্টে টিক চিহ্ন দিন।",
                "২. স্বয়ংক্রিয় সেভ ও রঙ বাছাই" to "নোট স্বয়ংক্রিয়ভাবে সংরক্ষিত থাকে এবং বিভিন্ন রঙের ট্যাগ দিয়ে নোটগুলো সুন্দরভাবে গুছিয়ে রাখা যায়।"
            )
        } else {
            listOf(
                "1. Quick Notes & Checklists" to "Capture quick ideas, meeting memos, calculation drafts, and interactive to-do checklists on the fly.",
                "2. Auto-Save & Color Tags" to "All entries are automatically saved locally with custom color categorization tags for fast visual filtering."
            )
        }
        ToolType.WORLD_CLOCK -> if (isBn) {
            listOf(
                "১. বিশ্ব সময় ও টাইমজোন" to "ঢাকা, লন্ডন, নিউইয়র্ক, টোকিও, রিয়াদসহ বিশ্বের প্রধান প্রধান শহরগুলোর বর্তমান লাইভ সময় ও সময়ের ব্যবধান দেখুন।",
                "২. আন্তর্জাতিক কল ও মিটিং শিডিউল" to "অন্য দেশের সাথে যোগাযোগের জন্য কোন সময়ে কল করা সুবিধাজনক তা সহজেই বের করুন।"
            )
        } else {
            listOf(
                "1. Global Clocks & Timezones" to "Monitor real-time clocks and time differences across major worldwide capitals and time zones.",
                "2. International Meeting Scheduling" to "Easily determine convenient working hours and avoid disturbing clients or family abroad across time offsets."
            )
        }
        ToolType.UNIT_PRICE_COMPARER -> if (isBn) {
            listOf(
                "১. একক দাম তুলনা" to "দোকানে কেনাকাটার সময় কোন প্যাকেট বা বোতলের ডিলটি বেশি সাশ্রয়ী তা নির্ধারণ করতে প্রতি গ্রাম, কেজি বা মিলিলিটারের দাম তুলনা করুন।",
                "২. সাশ্রয়ের পরিমাণ" to "কোন পণ্যটি কিনলে কত শতাংশ এবং কত টাকা সাশ্রয় হবে তা তাৎক্ষণিক বের করে সেরা অফার বেছে নিন।"
            )
        } else {
            listOf(
                "1. Compare Unit Pricing" to "Compare two package sizes with different weights/volumes and prices to calculate the exact cost per gram, milliliter, or ounce.",
                "2. Find Best Bargain" to "Instantly identify which product offers better economic value and see your exact percentage and currency savings."
            )
        }
        ToolType.SIMPLE_COMPASS -> if (isBn) {
            listOf(
                "১. ডিজিটাল দিক নির্ণায়ক" to "ডিভাইসের ম্যাগনেটিক সেন্সর ব্যবহার করে উত্তর, দক্ষিণ, পূর্ব ও পশ্চিম দিক এবং সঠিক ডিগ্রি মান জানুন।",
                "২. বাবল স্পিরিট লেভেলার" to "কোনো টেবিল বা সারফেস সমতল আছে কিনা তা স্পিরিট লেভেলিং মিটারের মাধ্যমে নিখুঁতভাবে পরীক্ষা করুন।"
            )
        } else {
            listOf(
                "1. Digital Orientation Compass" to "Uses hardware magnetometer sensors to display cardinal directions (N, S, E, W), precise azimuth angles, and magnetic headings.",
                "2. Integrated Spirit Bubble Level" to "Accurately inspect horizontal surfaces and tilt angles for home carpentry, camera leveling, and construction."
            )
        }
        ToolType.CAMERA_LEVEL -> if (isBn) {
            listOf(
                "১. AR ক্যামেরা লেভেলার কী ও কীভাবে কাজ করে?" to "এই টুলটি আপনার ফোনের ক্যামেরা প্রিভিউয়ের ওপর সরাসরি রিয়েল-টাইম অনুভূমিক ও উলম্ব লেভেল লাইন, এঙ্গেল প্রোট্রাক্টর এবং ক্রসহেয়ার প্রদর্শন করে। ফলে কোনো ছবি, ফ্রেম, দেয়াল, সেলফ, খাট বা আসবাবপত্র সোজা আছে কিনা তা ক্যামেরা দিয়েই সহজে পরীক্ষা ও ঠিক করা যায়।",
                "২. অনুভূমিক ও উলম্ব প্লাম্ব মোড" to "• অনুভূমিক লেভেল (Horizontal 0°): টেবিল, তাক বা ছবির ফ্রেম আনুভূমিকভাবে ঠিক সোজা হলে লাইনটি উজ্জ্বল সবুজ হয়ে ওঠে।\n• উলম্ব প্লাম্ব (Vertical 90°): দেয়াল, পিলার, দরজা বা খাড়া খুঁটি উলম্ব সোজা আছে কিনা তা নিখুঁতভাবে নিশ্চিত করে।",
                "৩. ফ্রিজ ফ্রেম ও জিরো ক্যালিব্রেশন" to "• ফ্রিজ ফ্রেম: পরিমাপের সময় ফ্রেম স্থির বা লক করে কোণ দেখে নেওয়া যায়।\n• জিরো ক্যালিব্রেট: যেকোনো নির্দিষ্ট রেফারেন্স তলকে ০° সেট করে রিলেটিভ কোণ মাপা যায়।"
            )
        } else {
            listOf(
                "1. What is AR Camera Level & Angle Meter?" to "This tool overlays real-time augmented horizontal horizon lines, vertical plumb lines, angle protractor dials, and crosshairs over your live camera feed to verify leveling of frames, shelves, walls, and furniture.",
                "2. Horizontal & Vertical Plumb Detection" to "• Horizontal Level (0°): The live horizon turns neon green with haptic buzz when perfectly level (±0.5°).\n• Vertical Plumb (90°): Verifies wall posts, columns, and vertical doors with plumb bob weight tracking.",
                "3. Freeze Frame & Relative Zero Calibration" to "• Freeze: Lock the live angle measurement to inspect readings comfortably.\n• Zero Calibrate: Set any custom inclination as relative 0° to measure relative slopes."
            )
        }
        ToolType.ASPECT_RATIO -> if (isBn) {
            listOf(
                "১. অ্যাসপেক্ট রেশিও ও ডাইমেনশন" to "ছবি, ডিসপ্লে এবং ভিডিওর দৈর্ঘ্য-প্রস্থের অনুপাত (যেমন ১৬:৯, ৪:৩, ১:১, ৯:১৬) এবং রিসাইজ পিক্সেল গণনা করুন।",
                "২. ডিজাইনার ও ক্রিয়েটরদের জন্য" to "ইউটিউব থাম্বনেইল, ফেসবুক পোস্ট, ইনস্টাগ্রাম রিল বা ওয়েব ব্যানারের জন্য সঠিক রেজোলিউশন নির্ধারণে সহায়ক।"
            )
        } else {
            listOf(
                "1. Aspect Ratios & Dimension Scaling" to "Calculate width-to-height proportions (16:9, 4:3, 1:1, 9:16, 21:9) and scale resolution dimensions seamlessly without distortion.",
                "2. Content Creators & UI Designers" to "Compute optimal dimensions for YouTube thumbnails, Instagram reels, video production, and web layouts."
            )
        }
        ToolType.RANDOM_NUMBER_PICKER -> if (isBn) {
            listOf(
                "১. র‍্যান্ডম লটারি ও লাকি নাম্বার" to "যেকোনো নির্দিষ্ট সীমার মধ্যে (যেমন ১ থেকে ১০০) ডুপ্লিকেট ছাড়া নিরপেক্ষ র‍্যান্ডম সংখ্যা তৈরি করুন।",
                "২. ডাইস রোল ও টস" to "লুডু বা বোর্ড গেম খেলার জন্য ভার্চুয়াল ডাইস রোল এবং যেকোনো সিদ্ধান্তের জন্য কয়েন টস (হেড/টেল) করুন।"
            )
        } else {
            listOf(
                "1. Random Number Generator" to "Generate provably fair random numbers within custom ranges with support for unique non-repeating lottery draws.",
                "2. Virtual Dice & Coin Flipper" to "Roll multi-sided dice for board games or flip a fair digital coin for quick decision-making."
            )
        }
        ToolType.PHOTO_LAB -> if (isBn) {
            listOf(
                "১. ফটো ক্রপ ও প্রিসেট" to "ফ্রি ক্রপ, ১:১ স্কয়ার, ৩:৪ পাসপোর্ট সাইজ, চাকরির আবেদন (৩০০x৩০০, ৩০০x৮০) ইত্যাদি অনুপাতে ছবি কাটুন।",
                "২. কাস্টম সাইজ (px / cm / mm / inch)" to "পিক্সেল বা সেন্টিমিটার/মিলিমিটার/ইঞ্চিতে প্রস্থ ও উচ্চতা এবং অ্যাসপেক্ট রেশিও লক করে নির্ভুল মাপ দিন।",
                "৩. নির্দিষ্ট ফাইল সাইজ (KB) ও ফরম্যাট রূপান্তর" to "লক্ষ্যমাত্রা অনুযায়ী কেবি (যেমন < ১০০ KB, ৫০ KB) সাইজ কম্প্রেস এবং JPG, PNG, WEBP ফরম্যাটে এক্সপোর্ট করুন।"
            )
        } else {
            listOf(
                "1. Photo Crop & Presets" to "Crop photos freely or with standard aspect ratios like 1:1, 3:4 portrait, 300x300 job photo, and signature.",
                "2. Custom Dimensions (px / cm / mm / in)" to "Set exact width and height in pixels, centimeters, millimeters, or inches with aspect ratio lock.",
                "3. Target File Size (KB) & Format" to "Compress directly to a target file size (e.g. 50 KB, 100 KB) and convert to JPG, PNG, or WEBP."
            )
        }
        ToolType.QIBLA_COMPASS -> if (isBn) {
            listOf(
                "১. সঠিক কিবলা নির্দেশক" to "পবিত্র কাবা শরীফের সঠিক দিক ও অ্যাঙ্গেল (২৭৭.৬° পশ্চিম / WNW) নির্ধারণ করতে আপনার ডিভাইসের ডিজিটাল কম্পাস ব্যবহার করা হয়।"
            )
        } else {
            listOf(
                "1. Accurate Qibla Finder" to "Uses hardware orientation sensors to locate the exact direction and compass bearing of the Holy Kaaba in Makkah (277.6° West / WNW from Bangladesh)."
            )
        }
        ToolType.DIGITAL_TASBIH -> if (isBn) {
            listOf(
                "১. জিকির ও তাসবিহ কাউন্টার" to "৩৩, ১০০ বা ১০০০ টার্গেট সেট করে আলহামদুলিল্লাহ, সুবহানাল্লাহসহ দৈনন্দিন জিকির গণনার আধুনিক ট্যালি কাউন্টার।"
            )
        } else {
            listOf(
                "1. Smart Digital Tasbih" to "Interactive tally counter for daily dhikr and tasbih with target goals (33, 100, 1000) and milestone completion feedback."
            )
        }
        ToolType.PRAYER_TIMES -> if (isBn) {
            listOf(
                "১. দৈনিক ৫ ওয়াক্ত সালাত" to "ফজর, যোহর, আসর, মাগরিব ও এশার সালাতের সময়সূচি এবং পরবর্তী সালাতের অবশিষ্ট কাউন্টডাউন সময়।"
            )
        } else {
            listOf(
                "1. Daily 5 Prayer Schedule" to "Displays accurate daily timetable for Fajr, Dhuhr, Asr, Maghrib, and Isha prayers along with a countdown to the next prayer."
            )
        }
        ToolType.SEHRI_IFTAR -> if (isBn) {
            listOf(
                "১. সেহরি ও ইফতারের সময়সূচি" to "আজকের সেহরির শেষ সময় ও ইফতারের নিখুঁত সময় এবং রোজার নিয়ত ও ইফতারের মাসনুন দোয়া।"
            )
        } else {
            listOf(
                "1. Sehri & Iftar Timetable" to "Daily Ramadan Sehri ending time, Iftar time, along with authentic Sehri Niyyat and Iftar Duas."
            )
        }
        ToolType.ISLAMIC_DUAS -> if (isBn) {
            listOf(
                "১. প্রয়োজনীয় মাসনুন দোয়া" to "ঘুম থেকে ওঠা, খাবার খাওয়া, সফর করা ও সায়্যিদুল এস্তেগফারসহ দৈনন্দিন জীবনের গুরুত্বপূর্ণ দোয়াসমূহ।"
            )
        } else {
            listOf(
                "1. Daily Authentic Duas" to "A curated collection of essential daily Islamic supplications with Arabic text and meanings."
            )
        }
        ToolType.HOLY_QURAN -> if (isBn) {
            listOf(
                "১. পবিত্র আল-কুরআন ডিজিটাল মডিউল" to "১১৪টি সূরা, আরবি হরফ, বাংলা অর্থ ও উচ্চারণ, অডিও তেলাওয়াত, অফলাইন ডাউনলোড ও এআই কুরআন অ্যাসিস্ট্যান্ট।"
            )
        } else {
            listOf(
                "1. Holy Quran Digital Module" to "Read all 114 Surahs with Arabic script, Bengali translation, stream/download audio recitation, and ask questions to AI Assistant."
            )
        }
        ToolType.NAMAZ_EDUCATION -> if (isBn) {
            listOf(
                "১. পূর্ণাঙ্গ নামাজ ও অজু শিক্ষা" to "অজু, তাহারাত, ৫ ওয়াক্ত নামাজ, ওয়াক্তভিত্তিক রাকাতের বিন্যাস, জানাজা, ঈদ ও নফল নামাজের বিস্তারিত সহিহ গাইড।"
            )
        } else {
            listOf(
                "1. Complete Namaz & Wudu Guide" to "Step-by-step guide for Wudu, 5 daily prayers, Rakat breakdown, Janazah, Eid, and optional prayers with Arabic audio recitations."
            )
        }
        ToolType.HADITH_LIBRARY -> if (isBn) {
            listOf(
                "১. হাদিস গ্রন্থ" to "সহীহ বুখারী, সহীহ মুসলিম, রিয়াদুস সালেহীন সহ হাদিস গ্রন্থসমূহ ১-ক্লিকে ডাউনলোড করে অফলাইনে পড়ার সুবিধা।"
            )
        } else {
            listOf(
                "1. Hadith Books" to "Download and read Sahih Bukhari, Sahih Muslim, Riyad as-Salihin offline with zero app size impact."
            )
        }
        ToolType.MARKET_LIST -> if (isBn) {
            listOf(
                "১. বাজার লিস্ট ও হিসাব তালিকা" to "দৈনন্দিন ও সাপ্তাহিক বাজারের তালিকা তৈরি করুন, প্রতিটি পণ্যের পরিমাণ ও এককের দাম লিখে মোট হিসাব দেখুন।",
                "২. মেমো সেভ ও PDF এক্সপোর্ট" to "তৈরিকৃত বাজার লিস্ট অ্যাপ হিস্টোরিতে মেমো আকারে সেভ রাখতে পারবেন অথবা সুন্দর পিডিএফ ডকুমেন্টে এক্সপোর্ট করে শেয়ার করতে পারবেন।"
            )
        } else {
            listOf(
                "1. Market List & Cost Calculation" to "Create shopping item lists with quantities, unit prices, and grand total cost calculation.",
                "2. Save Memo & PDF Export" to "Save market shopping lists as memos in history or export clean formatted PDF documents for sharing."
            )
        }
        ToolType.METAL_DETECTOR -> if (isBn) {
            listOf(
                "১. মেটাল ডিটেক্টর কীভাবে কাজ করে?" to "আপনার ফোনের বিল্ট-ইন ম্যাগনেটিক ফিল্ড সেন্সর (ম্যাগনেটোমিটার) ব্যবহার করে চারপাশের চৌম্বক তীব্রতা (MicroTesla) পরিমাপ করা হয়। লোহা বা ধাতুর কাছাকাছি আনলে এই মান উল্লেখযোগ্যভাবে বৃদ্ধি পায়।",
                "২. বেসলাইন জিরো ও ক্যালিব্রেশন" to "স্বাভাবিক পৃথিবীর চৌম্বক ক্ষেত্র প্রায় ৩০-৬০ μT। 'Set Zero Baseline' চাপলে স্বাভাবিক মান বাদ দিয়ে কেবল নতুন ধাতব বস্তুর পরিবর্তন ধরা পড়বে। ফোন বাতাসে ৮ আকারে ঘুরিয়ে সেন্সর ক্যালিব্রেট করা যায়।"
            )
        } else {
            listOf(
                "1. How Metal Detection Works" to "Uses the device's built-in 3-axis magnetometer sensor to measure magnetic flux density in microTesla (μT). Ferromagnetic metals (iron, steel) cause significant spikes.",
                "2. Baseline Zero & Calibration" to "Natural background geomagnetic field is ~30-60 μT. Tap 'Set Zero Baseline' to tare ambient levels. Wave device in a figure-8 to recalibrate the sensor."
            )
        }
        ToolType.PHONE_DIAGNOSTICS -> if (isBn) {
            listOf(
                "১. ফোন ও সেন্সর ডায়াগনসিস" to "অ্যাক্সিলোমিটার, জাইরোস্কোপ, লাইট, প্রক্সিমিটি সহ সকল বিল্ট-ইন সেন্সরের লাইভ রিডিং এবং স্বাস্থ্য পরীক্ষা করুন।",
                "২. হার্ডওয়্যার ইন্টারেক্টিভ টেস্ট" to "টাচস্ক্রিন গ্রিড ম্যাট্রিক্স, ডেড পিক্সেল ডিসপ্লে কালার, স্পিকার ফ্রিকোয়েন্সি, ভাইব্রেশন মোটর এবং ফ্ল্যাশলাইটের কার্যক্ষমতা পরীক্ষা করা যায়।"
            )
        } else {
            listOf(
                "1. Sensor Suite Diagnostics" to "Inspect real-time telemetry from all on-board sensors including Accelerometer, Gyroscope, Magnetometer, Light, Proximity, and Barometer.",
                "2. Hardware Interactive Tests" to "Test touchscreen responsiveness, dead pixels, stereo speaker frequencies, vibration motor haptics, and camera flashlight."
            )
        }
        ToolType.DEVICE_INFO -> if (isBn) {
            listOf(
                "১. ডিভাইস ও হার্ডওয়্যার স্পেসিফিকেশন" to "ফোনের মডেল, সিপিইউ কোর সংখ্যা, আর্কিটেকচার, র‍্যাম ও অভ্যন্তরীণ স্টোরেজ ব্যবহারের নিখুঁত পরিসংখ্যান দেখুন।",
                "২. ব্যাটারি, ডিসপ্লে ও সেন্সর তালিকা" to "ব্যাটারির স্বাস্থ্য, তাপমাত্রা, ভোল্টেজ, স্ক্রিনের রেজোলিউশন, রিফ্রেশ রেট (Hz) এবং সকল হার্ডওয়্যার সেন্সরের বিস্তারিত তালিকা দেখুন।"
            )
        } else {
            listOf(
                "1. Hardware & System Specs" to "Inspect device model, processor cores, ABI architecture, real-time RAM usage, and internal storage metrics.",
                "2. Battery, Display & Sensor Directory" to "View battery health, temperature, voltage, screen resolution, refresh rate, and full directory of registered hardware sensors."
            )
        }
        ToolType.BATTERY_MONITOR -> if (isBn) {
            listOf(
                "১. রিয়েল-টাইম কারেন্ট কীভাবে মাপা হয়?" to "আপনার ডিভাইসের চার্জিং বা ডিসচার্জিং গতির নিখুঁত পরিসংখ্যান প্রতি সেকেন্ডে মিলিঅ্যাম্পিয়ার (mA) ইউনিটে মাপা হয়।",
                "২. পাওয়ার ও ভোল্টেজ ট্র্যাকিং" to "ভোল্টেজ (V) ও কারেন্ট (mA) গুণ করে চার্জের গতি পরিমাপ করা হয় ওয়াটস (Watts) ইউনিটে। লাইভ গ্রাফে চার্জিংয়ের ওঠানামা প্রদর্শিত হয়।"
            )
        } else {
            listOf(
                "1. Real-time Current Monitoring" to "Inspect precise active charging or discharging current flow measured in milliAmperes (mA) dynamically.",
                "2. Power Speed & Live Waveform" to "Calculate wattage (Watts) based on actual voltage and real-time current to monitor and chart battery behavior live."
            )
        }
        ToolType.PDF_READER -> if (isBn) {
            listOf(
                "১. পিডিএফ ফাইল রিডার" to "ডিভাইসের যেকোনো পিডিএফ নথি ওপেন করে সরাসরি পৃষ্ঠা বাই পৃষ্ঠা দেখা, জুম করা এবং নাইট মোডে পড়ার সুবিধা।"
            )
        } else {
            listOf(
                "1. PDF File Reader" to "Open and view any PDF document on your device, scroll page by page, adjust zoom, and toggle night reading mode."
            )
        }
        ToolType.PDF_MAKER -> if (isBn) {
            listOf(
                "১. কাস্টম পিডিএফ মেকার" to "নোটস, শিরোনাম, টেক্সট বা ছবি যুক্ত করে এ৪ (A4) সাইজের প্রফেশনাল পিডিএফ নথি তৈরি, সেভ ও শেয়ার করার সুবিধা।"
            )
        } else {
            listOf(
                "1. Custom PDF Creator" to "Create professional A4 PDF documents from notes, text, titles, and photos to save or share effortlessly."
            )
        }
        ToolType.QR_BARCODE -> if (isBn) {
            listOf(
                "১. কিউআর স্ক্যানার" to "যেকোনো কিউআর বা বারকোড দ্রুত স্ক্যান করুন এবং ফ্ল্যাশলাইট বা গ্যালারি থেকে ছবি নিয়ে স্ক্যান করার সুবিধা।",
                "২. কিউআর ক্রিয়েটর" to "টেক্সট, ওয়েবসাইট লিঙ্ক, ফোন নাম্বার বা ওয়াইফাই ইনফো দিয়ে নিজের কাস্টম কিউআর কোড তৈরি করুন।"
            )
        } else {
            listOf(
                "1. QR Scanner" to "Quickly scan any QR or Barcode, with flashlight support and ability to scan from gallery images.",
                "2. QR Creator" to "Create custom QR codes using text, website links, phone numbers, or WiFi information."
            )
        }
    }
}

fun getCurrentLocationName(context: Context, isBn: Boolean, onResult: (String?) -> Unit) {
    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            onResult(null)
            return
        }
        
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        if (!hasFine && !hasCoarse) {
            onResult(null)
            return
        }
        
        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            val loc = locationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                bestLocation = loc
            }
        }
        
        val loc = bestLocation
        if (loc != null) {
            val geocoder = Geocoder(context, if (isBn) Locale("bn") else Locale.getDefault())
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                geocoder.getFromLocation(loc.latitude, loc.longitude, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<android.location.Address>) {
                        val address = addresses.firstOrNull()
                        val city = address?.locality ?: address?.subAdminArea ?: address?.adminArea ?: address?.countryName
                        onResult(city)
                    }
                    override fun onError(errorMessage: String?) {
                        onResult(null)
                    }
                })
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                val address = addresses?.firstOrNull()
                val city = address?.locality ?: address?.subAdminArea ?: address?.adminArea ?: address?.countryName
                onResult(city)
            }
        } else {
            val provider = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                LocationManager.GPS_PROVIDER
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                LocationManager.NETWORK_PROVIDER
            } else {
                null
            }
            
            if (provider != null) {
                locationManager.requestSingleUpdate(
                    provider,
                    object : android.location.LocationListener {
                        override fun onLocationChanged(location: Location) {
                            val geocoder = Geocoder(context, if (isBn) Locale("bn") else Locale.getDefault())
                            try {
                                @Suppress("DEPRECATION")
                                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                val address = addresses?.firstOrNull()
                                val city = address?.locality ?: address?.subAdminArea ?: address?.adminArea ?: address?.countryName
                                onResult(city)
                            } catch (e: Exception) {
                                onResult(null)
                            }
                        }
                        @Deprecated("Deprecated in Java")
                        override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                    },
                    android.os.Looper.getMainLooper()
                )
            } else {
                onResult(null)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        onResult(null)
    }
}

@Composable
fun HighlightedText(
    text: String,
    query: String,
    highlightColor: Color,
    baseColor: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight = FontWeight.Normal,
    maxLines: Int = Int.MAX_VALUE,
    lineHeight: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    modifier: Modifier = Modifier
) {
    if (query.isBlank() || !text.contains(query, ignoreCase = true)) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = baseColor,
            maxLines = maxLines,
            lineHeight = lineHeight,
            modifier = modifier,
            overflow = TextOverflow.Ellipsis
        )
        return
    }

    val annotatedString = remember(text, query, highlightColor, baseColor) {
        buildAnnotatedString {
            val lowerText = text.lowercase()
            val lowerQuery = query.lowercase()
            var startIndex = 0
            while (startIndex < text.length) {
                val index = lowerText.indexOf(lowerQuery, startIndex)
                if (index == -1) {
                    append(text.substring(startIndex))
                    break
                }
                if (index > startIndex) {
                    append(text.substring(startIndex, index))
                }
                withStyle(
                    SpanStyle(
                        background = highlightColor.copy(alpha = 0.25f),
                        color = highlightColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                ) {
                    append(text.substring(index, index + lowerQuery.length))
                }
                startIndex = index + lowerQuery.length
            }
        }
    }

    Text(
        text = annotatedString,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = baseColor,
        maxLines = maxLines,
        lineHeight = lineHeight,
        modifier = modifier,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun SearchEmptyGraphicsView(
    searchQuery: String,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "emptySearchTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val floatY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .graphicsLayer {
                    translationY = floatY
                },
            contentAlignment = Alignment.Center
        ) {
            // Background ambient canvas aura
            Canvas(
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
            ) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            themeColors.buttonEqualBg.copy(alpha = 0.22f),
                            themeColors.buttonEqualBg.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
            }

            // Outer decorative ring
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(themeColors.cardBg)
                    .border(2.dp, themeColors.buttonEqualBg.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SearchOff,
                    contentDescription = null,
                    tint = themeColors.buttonEqualBg,
                    modifier = Modifier.size(46.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isBn) "কোনো টুলস বা কনভার্টার পাওয়া যায়নি" else "No Tools or Converters Found",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.displayText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isBn)
                "\"$searchQuery\" এর সাথে কোনো টুল বা কনভার্টার মিলেনি। অন্য শব্দ বা বানান দিয়ে চেষ্টা করুন।"
            else
                "No tools match \"$searchQuery\". Try checking the spelling or use a different keyword.",
            fontSize = 13.sp,
            color = themeColors.displayText.copy(alpha = 0.65f),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardSearchResultsView(
    searchQuery: String,
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    val cleanQuery = searchQuery.trim().lowercase()

    val matchedTools = remember(cleanQuery, isBn) {
        ToolType.values().filter { tool ->
            tool.titleEn.lowercase().contains(cleanQuery) ||
            tool.titleBn.lowercase().contains(cleanQuery) ||
            tool.descriptionBn.lowercase().contains(cleanQuery) ||
            tool.category.titleEn.lowercase().contains(cleanQuery) ||
            tool.category.titleBn.lowercase().contains(cleanQuery)
        }
    }

    val matchedConverters = remember(cleanQuery, isBn) {
        ConverterType.values().filter { conv ->
            conv.titleEn.lowercase().contains(cleanQuery) ||
            conv.titleBn.lowercase().contains(cleanQuery) ||
            conv.category.titleEn.lowercase().contains(cleanQuery) ||
            conv.category.titleBn.lowercase().contains(cleanQuery) ||
            conv.units.any { it.lowercase().contains(cleanQuery) }
        }
    }

    val totalMatches = matchedTools.size + matchedConverters.size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        if (totalMatches == 0) {
            SearchEmptyGraphicsView(
                searchQuery = searchQuery,
                themeColors = themeColors,
                isBn = isBn
            )
        } else {
            // Result Count Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isBn)
                        "অনুসন্ধানের ফলাফল ($totalMatches টি পাওয়া গেছে)"
                    else
                        "Search Results ($totalMatches found)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = themeColors.displayText
                )
            }

            // Matching Tools Section
            if (matchedTools.isNotEmpty()) {
                Text(
                    text = if (isBn) "টুলস (${matchedTools.size})" else "Tools (${matchedTools.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.buttonEqualBg,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    matchedTools.chunked(2).forEach { rowTools ->
                        Row(
                            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowTools.forEach { tool ->
                                val toolCtx = LocalContext.current
                                val isFavorite = viewModel.favoriteTools.contains(tool.name)
                                val interactionSource = remember { MutableInteractionSource() }

                                ElevatedCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .testTag("search_tool_${tool.name.lowercase()}")
                                        .scaleOnPress(interactionSource)
                                        .themeCardShadow(themeColors, elevation = 1.dp)
                                        .combinedClickable(
                                            interactionSource = interactionSource,
                                            indication = androidx.compose.foundation.LocalIndication.current,
                                            onClick = { viewModel.openTool(tool) },
                                            onLongClick = {
                                                com.example.util.ShortcutUtils.pinToolShortcut(toolCtx, tool, isBn)
                                            }
                                        ),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = themeColors.cardBg
                                    ),
                                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxHeight().padding(12.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .clip(CircleShape)
                                                        .background(themeColors.buttonEqualBg),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = tool.icon,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { viewModel.requestToggleFavoriteTool(tool) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                        contentDescription = "Favorite",
                                                        tint = if (isFavorite) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.3f),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            HighlightedText(
                                                text = tool.getTitle(viewModel.selectedLanguage),
                                                query = cleanQuery,
                                                highlightColor = themeColors.buttonEqualBg,
                                                baseColor = themeColors.displayText,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 2,
                                                lineHeight = 16.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        HighlightedText(
                                            text = tool.getDescription(viewModel.selectedLanguage),
                                            query = cleanQuery,
                                            highlightColor = themeColors.buttonEqualBg,
                                            baseColor = themeColors.displayText.copy(alpha = 0.65f),
                                            fontSize = 11.sp,
                                            maxLines = 2,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                            if (rowTools.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Matching Converters Section
            if (matchedConverters.isNotEmpty()) {
                Text(
                    text = if (isBn) "কনভার্টার (${matchedConverters.size})" else "Converters (${matchedConverters.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.buttonEqualBg,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    matchedConverters.chunked(2).forEach { rowConverters ->
                        Row(
                            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowConverters.forEach { conv ->
                                val convCtx = LocalContext.current
                                val isFavorite = viewModel.favoriteConverters.contains(conv.name)
                                val interactionSource = remember { MutableInteractionSource() }

                                ElevatedCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .testTag("search_conv_${conv.name.lowercase()}")
                                        .scaleOnPress(interactionSource)
                                        .themeCardShadow(themeColors, elevation = 1.dp)
                                        .combinedClickable(
                                            interactionSource = interactionSource,
                                            indication = androidx.compose.foundation.LocalIndication.current,
                                            onClick = { viewModel.openConverter(conv) },
                                            onLongClick = {
                                                com.example.util.ShortcutUtils.pinConverterShortcut(convCtx, conv, isBn)
                                            }
                                        ),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = themeColors.cardBg
                                    ),
                                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxHeight().padding(12.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .clip(CircleShape)
                                                        .background(themeColors.buttonEqualBg),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = conv.icon,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { viewModel.requestToggleFavoriteConverter(conv) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                        contentDescription = "Favorite",
                                                        tint = if (isFavorite) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.3f),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            HighlightedText(
                                                text = conv.getTitle(viewModel.selectedLanguage),
                                                query = cleanQuery,
                                                highlightColor = themeColors.buttonEqualBg,
                                                baseColor = themeColors.displayText,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 2,
                                                lineHeight = 16.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        val unitsSample = remember(conv, isBn) {
                                            conv.units.take(3).joinToString(", ")
                                        }
                                        HighlightedText(
                                            text = unitsSample,
                                            query = cleanQuery,
                                            highlightColor = themeColors.buttonEqualBg,
                                            baseColor = themeColors.displayText.copy(alpha = 0.65f),
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                            if (rowConverters.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DynamicGreetingIllustrationBackground(
    currentHour: Int,
    weatherCode: Int,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val isDay = currentHour in 6..17
    // WMO Weather interpretation codes
    val isRainyOrSnowy = weatherCode in listOf(51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 71, 73, 75, 77, 80, 81, 82, 85, 86, 95, 96, 99)
    val isCloudy = weatherCode in listOf(3, 45, 48)

    val palette = remember(currentHour, weatherCode) {
        when {
            isRainyOrSnowy -> if (isDay) {
                // Rainy Day
                GreetingScenePalette(
                    skyGradient = listOf(Color(0xFF37474F), Color(0xFF455A64), Color(0xFF607D8B)),
                    sunColor = Color.Transparent,
                    haloColor = Color.Transparent,
                    cloudColor = Color(0x55263238),
                    farMountainColor = Color(0xFF546E7A),
                    midMountainColor = Color(0xFF607D8B),
                    nearHillsColor = Color(0xFF78909C),
                    riverColor = Color(0xFF90A4AE),
                    riverHighlightColor = Color(0x66ECEFF1),
                    bankColor = Color(0xFF1C2833),
                    treeColor1 = Color(0xFF101820),
                    treeColor2 = Color(0xFF0A1016),
                    isNight = false,
                    isRainy = true
                )
            } else {
                // Rainy Night
                GreetingScenePalette(
                    skyGradient = listOf(Color(0xFF0D1117), Color(0xFF161B22), Color(0xFF253040)),
                    sunColor = Color.Transparent,
                    haloColor = Color.Transparent,
                    cloudColor = Color(0x66070A0D),
                    farMountainColor = Color(0xFF1C2430),
                    midMountainColor = Color(0xFF253040),
                    nearHillsColor = Color(0xFF324257),
                    riverColor = Color(0xFF415A77),
                    riverHighlightColor = Color(0x66778DA9),
                    bankColor = Color(0xFF090D12),
                    treeColor1 = Color(0xFF040608),
                    treeColor2 = Color(0xFF020304),
                    isNight = true,
                    isRainy = true
                )
            }
            isCloudy -> if (isDay) {
                // Cloudy Day
                GreetingScenePalette(
                    skyGradient = listOf(Color(0xFF455A64), Color(0xFF607D8B), Color(0xFF78909C)),
                    sunColor = Color(0x44FFF9C4),
                    haloColor = Color(0x22FFFFFF),
                    cloudColor = Color(0x44ECEFF1),
                    farMountainColor = Color(0xFF37474F),
                    midMountainColor = Color(0xFF546E7A),
                    nearHillsColor = Color(0xFF78909C),
                    riverColor = Color(0xFFB0BEC5),
                    riverHighlightColor = Color(0x77FFFFFF),
                    bankColor = Color(0xFF263238),
                    treeColor1 = Color(0xFF182126),
                    treeColor2 = Color(0xFF10171B),
                    isNight = false,
                    isRainy = false
                )
            } else {
                // Cloudy Night
                GreetingScenePalette(
                    skyGradient = listOf(Color(0xFF101820), Color(0xFF1E2A38), Color(0xFF2B3E52)),
                    sunColor = Color(0x44E0FBFC),
                    haloColor = Color(0x156FFFE9),
                    cloudColor = Color(0x33243342),
                    farMountainColor = Color(0xFF1F2D3D),
                    midMountainColor = Color(0xFF2B3E52),
                    nearHillsColor = Color(0xFF3A526A),
                    riverColor = Color(0xFF4A6984),
                    riverHighlightColor = Color(0x667096B8),
                    bankColor = Color(0xFF0D141C),
                    treeColor1 = Color(0xFF060B10),
                    treeColor2 = Color(0xFF030608),
                    isNight = true,
                    isRainy = false
                )
            }
            else -> when (currentHour) {
                in 5..11 -> {
                    // Morning (Warm Dawn Pink/Lavender)
                    GreetingScenePalette(
                        skyGradient = listOf(Color(0xFF3D2652), Color(0xFF5A3A73), Color(0xFF885B9E)),
                        sunColor = Color(0xFFFFF0B3),
                        haloColor = Color(0xFFFFB5A7),
                        cloudColor = Color(0x33FFFFFF),
                        farMountainColor = Color(0xFF6B4582),
                        midMountainColor = Color(0xFF885B9E),
                        nearHillsColor = Color(0xFFA271B8),
                        riverColor = Color(0xFFF4ABC4),
                        riverHighlightColor = Color(0x88FFFFFF),
                        bankColor = Color(0xFF381D4F),
                        treeColor1 = Color(0xFF210F33),
                        treeColor2 = Color(0xFF1B0A2B),
                        isNight = false,
                        isRainy = false
                    )
                }
                in 12..15 -> {
                    // Afternoon (Warm Golden / Terracotta)
                    GreetingScenePalette(
                        skyGradient = listOf(Color(0xFFD35400), Color(0xFFE67E22), Color(0xFFF39C12)),
                        sunColor = Color(0xFFFFF9C4),
                        haloColor = Color(0xFFFFF9C4),
                        cloudColor = Color(0x38FFFFFF),
                        farMountainColor = Color(0xFFB03A2E),
                        midMountainColor = Color(0xFFCA6F1E),
                        nearHillsColor = Color(0xFFE59866),
                        riverColor = Color(0xFFFAD7A0),
                        riverHighlightColor = Color(0x88FFFFFF),
                        bankColor = Color(0xFF6E2C00),
                        treeColor1 = Color(0xFF3E1900),
                        treeColor2 = Color(0xFF2C1200),
                        isNight = false,
                        isRainy = false
                    )
                }
                in 16..19 -> {
                    // Evening (Sunset Crimson / Violet)
                    GreetingScenePalette(
                        skyGradient = listOf(Color(0xFF2C1236), Color(0xFF6C1E47), Color(0xFF9C276A)),
                        sunColor = Color(0xFFFFAB40),
                        haloColor = Color(0xFFFF7043),
                        cloudColor = Color(0x38FF6E40),
                        farMountainColor = Color(0xFF581845),
                        midMountainColor = Color(0xFF7D1E58),
                        nearHillsColor = Color(0xFF9C276A),
                        riverColor = Color(0xFFFF8A65),
                        riverHighlightColor = Color(0x99FFE082),
                        bankColor = Color(0xFF230728),
                        treeColor1 = Color(0xFF150218),
                        treeColor2 = Color(0xFF0D010F),
                        isNight = false,
                        isRainy = false
                    )
                }
                else -> {
                    // Night (Deep Indigo / Glowing Moon)
                    GreetingScenePalette(
                        skyGradient = listOf(Color(0xFF0A1128), Color(0xFF1C2541), Color(0xFF283A63)),
                        sunColor = Color(0xFFE0FBFC),
                        haloColor = Color(0xFF6FFFE9),
                        cloudColor = Color(0x223A506B),
                        farMountainColor = Color(0xFF1E2A4A),
                        midMountainColor = Color(0xFF283A63),
                        nearHillsColor = Color(0xFF3A506B),
                        riverColor = Color(0xFF5BC0BE),
                        riverHighlightColor = Color(0x886FFFE9),
                        bankColor = Color(0xFF0D1B2A),
                        treeColor1 = Color(0xFF060C14),
                        treeColor2 = Color(0xFF03070B),
                        isNight = true,
                        isRainy = false
                    )
                }
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "greeting_anim")
    val cloudShift by infiniteTransition.animateFloat(
        initialValue = -0.035f,
        targetValue = 0.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "clouds"
    )
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo"
    )
    val riverShimmer by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "river"
    )
    val starTwinkle by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "stars"
    )
    val rainProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rain"
    )

    androidx.compose.foundation.Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Sky Gradient Background
        drawRect(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(palette.skyGradient)
        )

        // 2. Stars for Night with gentle twinkle
        if (palette.isNight && !palette.isRainy) {
            val starPositions = listOf(
                Pair(0.15f, 0.12f), Pair(0.28f, 0.22f), Pair(0.42f, 0.08f),
                Pair(0.68f, 0.15f), Pair(0.82f, 0.25f), Pair(0.91f, 0.10f),
                Pair(0.22f, 0.35f), Pair(0.75f, 0.32f), Pair(0.58f, 0.20f),
                Pair(0.35f, 0.15f), Pair(0.08f, 0.28f), Pair(0.88f, 0.38f)
            )
            starPositions.forEachIndexed { i, (sx, sy) ->
                val starRadius = if (i % 3 == 0) 2.2f else 1.4f
                val dynamicAlpha = if (i % 2 == 0) (0.85f * starTwinkle).coerceIn(0.2f, 1f) else (0.55f * (1.45f - starTwinkle)).coerceIn(0.2f, 1f)
                drawCircle(
                    color = Color.White.copy(alpha = dynamicAlpha),
                    radius = starRadius,
                    center = androidx.compose.ui.geometry.Offset(w * sx, h * sy)
                )
            }
        }

        // 3. Sun or Moon & Pulsing Celestial Halos
        if (palette.sunColor != Color.Transparent) {
            val cx = w * 0.5f
            val cy = h * 0.40f

            if (palette.haloColor != Color.Transparent) {
                drawCircle(
                    color = palette.haloColor.copy(alpha = 0.18f),
                    radius = w * 0.18f * haloScale,
                    center = androidx.compose.ui.geometry.Offset(cx, cy)
                )
                drawCircle(
                    color = palette.haloColor.copy(alpha = 0.32f),
                    radius = w * 0.12f * haloScale,
                    center = androidx.compose.ui.geometry.Offset(cx, cy)
                )
            }
            drawCircle(
                color = palette.sunColor,
                radius = w * 0.07f,
                center = androidx.compose.ui.geometry.Offset(cx, cy)
            )
        }

        // 4. Stylized Floating Animated Clouds
        drawOval(
            color = palette.cloudColor,
            topLeft = androidx.compose.ui.geometry.Offset(w * (0.08f + cloudShift), h * 0.15f),
            size = androidx.compose.ui.geometry.Size(w * 0.32f, h * 0.12f)
        )
        drawOval(
            color = palette.cloudColor,
            topLeft = androidx.compose.ui.geometry.Offset(w * (0.62f - cloudShift * 0.8f), h * 0.12f),
            size = androidx.compose.ui.geometry.Size(w * 0.30f, h * 0.10f)
        )

        // 5. Far Mountains (Layer 1)
        val farMountainPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, h * 0.58f)
            lineTo(w * 0.10f, h * 0.46f)
            lineTo(w * 0.25f, h * 0.54f)
            lineTo(w * 0.38f, h * 0.43f)
            lineTo(w * 0.52f, h * 0.53f)
            lineTo(w * 0.68f, h * 0.42f)
            lineTo(w * 0.82f, h * 0.52f)
            lineTo(w * 0.92f, h * 0.45f)
            lineTo(w, h * 0.51f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(farMountainPath, color = palette.farMountainColor)

        // 6. Mid Mountains (Layer 2)
        val midMountainPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, h * 0.64f)
            lineTo(w * 0.15f, h * 0.53f)
            lineTo(w * 0.28f, h * 0.62f)
            lineTo(w * 0.45f, h * 0.50f)
            lineTo(w * 0.62f, h * 0.63f)
            lineTo(w * 0.76f, h * 0.52f)
            lineTo(w * 0.90f, h * 0.60f)
            lineTo(w, h * 0.55f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(midMountainPath, color = palette.midMountainColor)

        // 7. Near Hills (Layer 3)
        val nearHillsPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, h * 0.71f)
            quadraticTo(w * 0.20f, h * 0.60f, w * 0.40f, h * 0.70f)
            quadraticTo(w * 0.70f, h * 0.64f, w, h * 0.69f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(nearHillsPath, color = palette.nearHillsColor)

        // 8. Winding River
        val riverPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.49f, h * 0.58f)
            cubicTo(w * 0.51f, h * 0.67f, w * 0.45f, h * 0.76f, w * 0.38f, h * 1.0f)
            lineTo(w * 0.62f, h * 1.0f)
            cubicTo(w * 0.55f, h * 0.85f, w * 0.54f, h * 0.74f, w * 0.51f, h * 0.58f)
            close()
        }
        drawPath(riverPath, color = palette.riverColor)

        // River Reflections with undulating shimmer
        val reflectionAlpha = (palette.riverHighlightColor.alpha * riverShimmer).coerceIn(0.2f, 1f)
        val reflectionBaseColor = palette.riverHighlightColor.copy(alpha = 1f)
        drawLine(
            color = reflectionBaseColor.copy(alpha = (reflectionAlpha * 0.8f).coerceIn(0f, 1f)),
            start = androidx.compose.ui.geometry.Offset(w * 0.48f, h * 0.65f),
            end = androidx.compose.ui.geometry.Offset(w * 0.52f, h * 0.65f),
            strokeWidth = 2.5f
        )
        drawLine(
            color = reflectionBaseColor.copy(alpha = (reflectionAlpha * 0.9f).coerceIn(0f, 1f)),
            start = androidx.compose.ui.geometry.Offset(w * 0.46f, h * 0.73f),
            end = androidx.compose.ui.geometry.Offset(w * 0.54f, h * 0.73f),
            strokeWidth = 3f
        )
        drawLine(
            color = reflectionBaseColor.copy(alpha = reflectionAlpha),
            start = androidx.compose.ui.geometry.Offset(w * 0.44f, h * 0.82f),
            end = androidx.compose.ui.geometry.Offset(w * 0.56f, h * 0.82f),
            strokeWidth = 4f
        )
        drawLine(
            color = reflectionBaseColor.copy(alpha = reflectionAlpha),
            start = androidx.compose.ui.geometry.Offset(w * 0.41f, h * 0.92f),
            end = androidx.compose.ui.geometry.Offset(w * 0.59f, h * 0.92f),
            strokeWidth = 4.5f
        )

        // 9. Foreground Banks
        val leftBankPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, h * 0.80f)
            quadraticTo(w * 0.20f, h * 0.73f, w * 0.41f, h * 0.83f)
            lineTo(w * 0.38f, h)
            lineTo(0f, h)
            close()
        }
        drawPath(leftBankPath, color = palette.bankColor)

        val rightBankPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(w, h * 0.78f)
            quadraticTo(w * 0.80f, h * 0.73f, w * 0.59f, h * 0.85f)
            lineTo(w * 0.63f, h)
            lineTo(w, h)
            close()
        }
        drawPath(rightBankPath, color = palette.bankColor)

        // 10. Pine Tree Silhouettes (Left Forest)
        drawPineTree(this, cx = w * 0.08f, baseY = h * 0.95f, width = w * 0.11f, height = h * 0.32f, color = palette.treeColor1)
        drawPineTree(this, cx = w * 0.16f, baseY = h * 0.92f, width = w * 0.13f, height = h * 0.38f, color = palette.treeColor2)
        drawPineTree(this, cx = w * 0.25f, baseY = h * 0.96f, width = w * 0.10f, height = h * 0.28f, color = palette.treeColor1)
        drawPineTree(this, cx = w * 0.32f, baseY = h * 0.98f, width = w * 0.07f, height = h * 0.20f, color = palette.treeColor2)

        // 11. Pine Tree Silhouettes (Right Forest)
        drawPineTree(this, cx = w * 0.86f, baseY = h * 0.92f, width = w * 0.14f, height = h * 0.39f, color = palette.treeColor2)
        drawPineTree(this, cx = w * 0.76f, baseY = h * 0.95f, width = w * 0.11f, height = h * 0.33f, color = palette.treeColor1)
        drawPineTree(this, cx = w * 0.67f, baseY = h * 0.97f, width = w * 0.08f, height = h * 0.22f, color = palette.treeColor2)
        drawPineTree(this, cx = w * 0.94f, baseY = h * 0.96f, width = w * 0.10f, height = h * 0.29f, color = palette.treeColor1)

        // 12. Animated Falling Rain Streaks for Rainy Weather
        if (palette.isRainy) {
            val rainDrops = listOf(
                Pair(0.12f, 0.15f), Pair(0.28f, 0.35f), Pair(0.44f, 0.20f), Pair(0.60f, 0.45f), Pair(0.76f, 0.18f), Pair(0.90f, 0.38f),
                Pair(0.18f, 0.60f), Pair(0.35f, 0.75f), Pair(0.52f, 0.65f), Pair(0.70f, 0.80f), Pair(0.85f, 0.55f),
                Pair(0.24f, 0.90f), Pair(0.48f, 0.10f), Pair(0.65f, 0.30f), Pair(0.82f, 0.95f),
                Pair(0.15f, 0.40f), Pair(0.38f, 0.50f), Pair(0.58f, 0.25f), Pair(0.78f, 0.60f)
            )
            rainDrops.forEach { (rx, baseRy) ->
                val currentYFraction = (baseRy + rainProgress) % 1.0f
                val startX = w * rx
                val startY = h * currentYFraction
                drawLine(
                    color = Color.White.copy(alpha = 0.38f),
                    start = androidx.compose.ui.geometry.Offset(startX, startY),
                    end = androidx.compose.ui.geometry.Offset(startX - (w * 0.025f), startY + (h * 0.09f)),
                    strokeWidth = 2.2f
                )
            }
        }
    }
}

private fun drawPineTree(
    drawScope: androidx.compose.ui.graphics.drawscope.DrawScope,
    cx: Float,
    baseY: Float,
    width: Float,
    height: Float,
    color: Color
) {
    val topY = baseY - height
    val tier1Height = height * 0.45f
    val tier2Height = height * 0.40f
    val tier3Height = height * 0.35f

    // Tier 1 (Top)
    val path1 = androidx.compose.ui.graphics.Path().apply {
        moveTo(cx, topY)
        lineTo(cx + (width * 0.30f), topY + tier1Height)
        lineTo(cx - (width * 0.30f), topY + tier1Height)
        close()
    }
    drawScope.drawPath(path1, color = color)

    // Tier 2 (Middle)
    val tier2Top = topY + (height * 0.22f)
    val path2 = androidx.compose.ui.graphics.Path().apply {
        moveTo(cx, tier2Top)
        lineTo(cx + (width * 0.42f), tier2Top + tier2Height)
        lineTo(cx - (width * 0.42f), tier2Top + tier2Height)
        close()
    }
    drawScope.drawPath(path2, color = color)

    // Tier 3 (Bottom)
    val tier3Top = topY + (height * 0.45f)
    val path3 = androidx.compose.ui.graphics.Path().apply {
        moveTo(cx, tier3Top)
        lineTo(cx + (width * 0.50f), tier3Top + tier3Height)
        lineTo(cx - (width * 0.50f), tier3Top + tier3Height)
        close()
    }
    drawScope.drawPath(path3, color = color)
}

private data class GreetingScenePalette(
    val skyGradient: List<Color>,
    val sunColor: Color,
    val haloColor: Color,
    val cloudColor: Color,
    val farMountainColor: Color,
    val midMountainColor: Color,
    val nearHillsColor: Color,
    val riverColor: Color,
    val riverHighlightColor: Color,
    val bankColor: Color,
    val treeColor1: Color,
    val treeColor2: Color,
    val isNight: Boolean,
    val isRainy: Boolean
)


