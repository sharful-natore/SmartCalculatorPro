package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicWeatherScreen(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    var showSearchDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (viewModel.weatherData == null) {
            viewModel.fetchWeather()
        }
    }

    // Refresh icon rotation animation while loading
    val infiniteTransition = rememberInfiniteTransition(label = "weather_refresh_transition")
    val rotationAngle by if (viewModel.weatherIsLoading) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "refresh_rotation"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Location Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = viewModel.weatherLocation.ifBlank { "Natore" },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    if (viewModel.isOfflineWeatherData) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFD97706).copy(alpha = 0.18f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isBn) "অফলাইন" else "Offline",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )
                        }
                    }
                }
                val updateTimeFormatted = remember(viewModel.lastWeatherFetchTime, isBn) {
                    val fetchTime = if (viewModel.lastWeatherFetchTime > 0L) viewModel.lastWeatherFetchTime else System.currentTimeMillis()
                    val date = java.util.Date(fetchTime)
                    val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.ENGLISH)
                    val timeStr = timeFormat.format(date)
                    if (isBn) {
                        val bnDigits = timeStr
                            .replace("0", "০")
                            .replace("1", "১")
                            .replace("2", "২")
                            .replace("3", "৩")
                            .replace("4", "৪")
                            .replace("5", "৫")
                            .replace("6", "৬")
                            .replace("7", "৭")
                            .replace("8", "৮")
                            .replace("9", "৯")
                            .replace("AM", "AM")
                            .replace("PM", "PM")
                        "হালনাগাদ হয়েছেঃ $bnDigits"
                    } else {
                        "Updated on: $timeStr"
                    }
                }

                Text(
                    text = updateTimeFormatted,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.displayText.copy(alpha = 0.8f)
                )
            }
            IconButton(onClick = { showSearchDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Location",
                    tint = themeColors.displayText
                )
            }
            IconButton(onClick = { viewModel.fetchWeather(force = true) }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = themeColors.displayText,
                    modifier = Modifier.rotate(rotationAngle)
                )
            }
        }

        // Saved Locations Selector Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(items = viewModel.availableWeatherCities) { city ->
                val isSelected = city.name.equals(viewModel.weatherLocation, ignoreCase = true)
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectWeatherCity(city) },
                    label = {
                        Text(
                            text = city.name,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.LocationOn else Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = themeColors.buttonEqualBg,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                        containerColor = themeColors.displayBackground,
                        labelColor = themeColors.displayText,
                        iconColor = themeColors.displayText.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        // Weather Fetch Error / Status Notice (Disabled to keep weather details clean)
        if (false) {
            viewModel.weatherFetchError?.let { err ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFEF3C7)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Weather Status Notice",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "আবহাওয়া স্ট্যাটাস / কারণ:" else "Weather Status Cause:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = err,
                                fontSize = 12.sp,
                                color = Color(0xFF78350F)
                            )
                        }
                    }
                }
            }
        }

        val weatherData = viewModel.weatherData

        if (viewModel.weatherIsLoading && weatherData == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = themeColors.buttonEqualBg)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isBn) "আবহাওয়া উপাত্ত সংগ্রহ করা হচ্ছে..." else "Fetching weather data...",
                        color = themeColors.displayText.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
        } else if (weatherData == null) {
            // Empty Graphics State when Weather Data is missing/failed
            WeatherEmptyGraphicsView(
                viewModel = viewModel,
                themeColors = themeColors,
                isBn = isBn,
                onSearchClick = { showSearchDialog = true }
            )
        } else {
            val current = weatherData.current
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Current Weather Card
                item {
                    CurrentWeatherCard(current, themeColors, isBn)
                }

                // Sun & Moon Tracker
                item {
                    SunMoonTrackerCard(weatherData, themeColors, isBn)
                }

                // Hourly Forecast
                item {
                    Text(
                        text = if (isBn) "প্রতি ঘণ্টার পূর্বাভাস" else "Hourly Forecast",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(24) { index ->
                            // Use next 24 hours
                            val timeStr = weatherData.hourly.time.getOrNull(index) ?: ""
                            val temp = weatherData.hourly.temperature_2m.getOrNull(index) ?: 0.0
                            val code = weatherData.hourly.weather_code.getOrNull(index) ?: 0
                            val rainProb = weatherData.hourly.precipitation_probability.getOrNull(index) ?: 0
                            HourlyWeatherItem(timeStr, temp, code, rainProb, themeColors, isBn)
                        }
                    }
                }

                // 7-Day Forecast (with padding cleaned up)
                item {
                    Text(
                        text = if (isBn) "৭ দিনের পূর্বাভাস" else "7-Day Forecast",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                items(weatherData.daily.time.size) { index ->
                    val timeStr = weatherData.daily.time[index]
                    val code = weatherData.daily.weather_code[index]
                    val minT = weatherData.daily.temperature_2m_min[index]
                    val maxT = weatherData.daily.temperature_2m_max[index]
                    val precip = weatherData.daily.precipitation_sum[index]
                    val rainProbMax = weatherData.daily.precipitation_probability_max.getOrNull(index) ?: 0
                    DailyWeatherItem(timeStr, code, minT, maxT, precip, rainProbMax, themeColors, isBn)
                }
            }
        }
    }

    if (showSearchDialog) {
        LocationSearchDialog(
            viewModel = viewModel,
            themeColors = themeColors,
            isBn = isBn,
            onDismiss = { showSearchDialog = false }
        )
    }
}

@Composable
fun CurrentWeatherCard(
    current: com.example.data.network.CurrentWeather,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    val temp = current.temperature_2m.toInt()
    val isDay = current.is_day == 1
    val code = current.weather_code
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)

    val condition = when (code) {
        0 -> if (isBn) "পরিষ্কার আকাশ" else "Clear Sky"
        1, 2, 3 -> if (isBn) "আংশিক মেঘলা" else "Partly Cloudy"
        45, 48 -> if (isBn) "ঘন কুয়াশা" else "Fog"
        51, 53, 55 -> if (isBn) "গুঁড়ি গুঁড়ি বৃষ্টি" else "Drizzle"
        61, 63, 65 -> if (isBn) "বৃষ্টিপাত" else "Rain"
        71, 73, 75 -> if (isBn) "তুষারপাত" else "Snow"
        95, 96, 99 -> if (isBn) "বজ্রবৃষ্টি" else "Thunderstorm"
        else -> if (isBn) "সাধারণ আবহাওয়া" else "Moderate Weather"
    }

    fun toBnDigits(input: String): String {
        return input
            .replace("0", "০")
            .replace("1", "১")
            .replace("2", "২")
            .replace("3", "৩")
            .replace("4", "৪")
            .replace("5", "৫")
            .replace("6", "৬")
            .replace("7", "৭")
            .replace("8", "৮")
            .replace("9", "৯")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 260.dp)
                .clip(RoundedCornerShape(24.dp))
        ) {
            // Dynamic Greeting Section Canvas Illustration Background
            DynamicGreetingIllustrationBackground(
                currentHour = currentHour,
                weatherCode = code,
                isDark = isDark,
                modifier = Modifier.matchParentSize()
            )

            // Protective scrim for high contrast typography over all sky colors
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.15f),
                                Color.Black.copy(alpha = 0.45f)
                            )
                        )
                    )
            )

            // Weather Info Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Weather condition graphic badge
                WeatherConditionGraphic(
                    code = code,
                    isDay = isDay,
                    modifier = Modifier.size(76.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                val tempStr = if (isBn) toBnDigits("$temp") else "$temp"
                Text(
                    text = "$tempStr°",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = condition,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.95f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Chips Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.28f))
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val humidityVal = if (isBn) toBnDigits("${current.relative_humidity_2m}") else "${current.relative_humidity_2m}"
                    val windVal = if (isBn) toBnDigits("${current.wind_speed_10m}") else "${current.wind_speed_10m}"
                    val feelsLikeVal = if (isBn) toBnDigits("${current.apparent_temperature}") else "${current.apparent_temperature}"

                    WeatherDetailItemOverlay(
                        icon = Icons.Default.WaterDrop,
                        value = "$humidityVal%",
                        label = if (isBn) "আর্দ্রতা" else "Humidity"
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(Color.White.copy(alpha = 0.25f))
                    )

                    WeatherDetailItemOverlay(
                        icon = Icons.Default.Air,
                        value = "$windVal km/h",
                        label = if (isBn) "বাতাস" else "Wind"
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(Color.White.copy(alpha = 0.25f))
                    )

                    WeatherDetailItemOverlay(
                        icon = Icons.Default.Thermostat,
                        value = "$feelsLikeVal°",
                        label = if (isBn) "অনুভূত" else "Feels Like"
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherDetailItemOverlay(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = value,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.75f)
        )
    }
}

@Composable
fun WeatherDetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, themeColors: CalculatorThemeColors) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = themeColors.buttonEqualText,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.buttonEqualText
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = themeColors.buttonEqualText.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun HourlyWeatherItem(timeStr: String, temp: Double, code: Int, rainProb: Int, themeColors: CalculatorThemeColors, isBn: Boolean) {
    val time = try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
        val date = parser.parse(timeStr)
        val formatter = SimpleDateFormat("ha", Locale.US)
        formatter.format(date ?: Date())
    } catch (e: Exception) {
        "--"
    }

    val isItemDay = try {
        val hourStr = timeStr.substringAfter('T').substringBefore(':')
        val hour = hourStr.toIntOrNull() ?: 12
        hour in 6..18
    } catch (e: Exception) {
        true
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(themeColors.displayBackground, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = time,
            color = themeColors.displayText.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        WeatherConditionGraphic(
            code = code,
            isDay = isItemDay,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${temp.toInt()}°",
            color = themeColors.displayText,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "$rainProb%",
                color = themeColors.displayText.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DailyWeatherItem(
    timeStr: String,
    code: Int,
    minT: Double,
    maxT: Double,
    precip: Double,
    rainProbMax: Int,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    val day = try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = parser.parse(timeStr)
        val formatter = SimpleDateFormat("EEEE", Locale.US)
        formatter.format(date ?: Date())
    } catch (e: Exception) {
        "--"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = day,
            color = themeColors.displayText,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        
        Row(
            modifier = Modifier.weight(1.2f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            WeatherConditionGraphic(
                code = code,
                isDay = true,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column(horizontalAlignment = Alignment.Start) {
                if (precip > 0) {
                    Text(
                        text = "${precip.toInt()}mm",
                        color = themeColors.buttonEqualBg,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = "Rain probability",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "$rainProbMax%",
                        color = themeColors.displayText.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "${maxT.toInt()}°",
                color = themeColors.displayText,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${minT.toInt()}°",
                color = themeColors.displayText.copy(alpha = 0.5f),
                fontSize = 16.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchDialog(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(searchQuery) {
        delay(500) // Debounce
        viewModel.searchLocationForWeather(searchQuery)
    }

    AlertDialog(
        onDismissRequest = {
            viewModel.clearGeocodingResults()
            onDismiss()
        },
        containerColor = themeColors.background,
        title = {
            Text(
                text = if (isBn) "লোকেশন খুঁজুন" else "Search Location",
                color = themeColors.displayText
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = if (isBn) "শহরের নাম লিখুন..." else "Enter city name...",
                            color = themeColors.displayText.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText,
                        focusedBorderColor = themeColors.buttonEqualBg,
                        unfocusedBorderColor = themeColors.displayBackground,
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                if (viewModel.geocodingIsLoading) {
                    CircularProgressIndicator(
                        color = themeColors.buttonEqualBg,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(viewModel.geocodingResults) { result ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateWeatherLocation(
                                            location = "${result.name}${if (result.country != null) ", ${result.country}" else ""}",
                                            lat = result.latitude,
                                            lng = result.longitude
                                        )
                                        viewModel.clearGeocodingResults()
                                        onDismiss()
                                    }
                                    .padding(vertical = 12.dp)
                            ) {
                                Text(
                                    text = result.name,
                                    color = themeColors.displayText,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${result.admin1 ?: ""}, ${result.country ?: ""}",
                                    color = themeColors.displayText.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                            HorizontalDivider(color = themeColors.displayBackground)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.clearGeocodingResults()
                    onDismiss()
                }
            ) {
                Text(
                    text = if (isBn) "বন্ধ করুন" else "Close",
                    color = themeColors.buttonEqualBg
                )
            }
        }
    )
}

@Composable
fun WeatherEmptyGraphicsView(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onSearchClick: () -> Unit
) {
    val causeText = viewModel.weatherFetchError ?: if (isBn) {
        "ইন্টারনেট সংযোগ চালু নেই অথবা আবহাওয়া ওয়েব সার্ভিস থেকে সাড়া পাওয়া যায়নি।"
    } else {
        "No internet connection or weather service did not respond."
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.displayBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Empty State Illustration Badge
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(themeColors.buttonEqualBg.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(themeColors.buttonEqualBg.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "No Weather Data",
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = if (isBn) "আবহাওয়ার বিবরণ পাওয়া যায়নি" else "No Weather Data Available",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Cause Explanation Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Reason",
                            tint = Color(0xFFD97706),
                            modifier = Modifier
                                .size(22.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isBn) "কারণ / পরিস্থিতি:" else "Cause / Reason:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF92400E)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = causeText,
                                fontSize = 13.sp,
                                color = Color(0xFF78350F),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons Row
                Button(
                    onClick = { viewModel.fetchWeather(force = true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "পুনরায় রিফ্রেশ করুন" else "Retry Refresh",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onSearchClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, themeColors.buttonEqualBg)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search City",
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "শহরের নাম খুঁজুন" else "Search City",
                            color = themeColors.buttonEqualBg,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.loadDemoWeather() },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Demo Data",
                            tint = themeColors.displayText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "ডেমো ডেটা দেখুন" else "View Demo Data",
                            color = themeColors.displayText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherConditionGraphic(
    code: Int,
    isDay: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2f, height / 2f)
        val radius = width.coerceAtMost(height) / 2f

        when (code) {
            0 -> { // Clear (Sunny Day / Clear Night)
                if (isDay) {
                    // Radiant Golden Sun with multi-layer glowing aura
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x66FDE047), Color(0x22FBBF24), Color(0x00F59E0B)),
                            center = center,
                            radius = radius * 1.05f
                        ),
                        radius = radius * 1.05f,
                        center = center
                    )
                    // Sun Core Disc with rich gradient
                    drawCircle(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFFEF08A), Color(0xFFFBBF24), Color(0xFFF59E0B)),
                            start = Offset(center.x - radius * 0.45f, center.y - radius * 0.45f),
                            end = Offset(center.x + radius * 0.45f, center.y + radius * 0.45f)
                        ),
                        radius = radius * 0.46f,
                        center = center
                    )
                    // Sun Rays (8 rays with alternating lengths and rounded caps)
                    val rayCount = 8
                    for (i in 0 until rayCount) {
                        val angle = (i * (360f / rayCount)) * (Math.PI / 180f)
                        val innerDist = radius * 0.58f
                        val rayLen = if (i % 2 == 0) radius * 0.26f else radius * 0.18f
                        val startX = center.x + Math.cos(angle).toFloat() * innerDist
                        val startY = center.y + Math.sin(angle).toFloat() * innerDist
                        val endX = center.x + Math.cos(angle).toFloat() * (innerDist + rayLen)
                        val endY = center.y + Math.sin(angle).toFloat() * (innerDist + rayLen)
                        
                        drawLine(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFFBBF24), Color(0xFFF59E0B)),
                                start = Offset(startX, startY),
                                end = Offset(endX, endY)
                            ),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = radius * 0.085f,
                            cap = StrokeCap.Round
                        )
                    }
                } else {
                    // Luminous Crescent Moon with soft night glow and twinkling stars
                    // Twinkling stars
                    val star1 = Offset(center.x - radius * 0.45f, center.y - radius * 0.45f)
                    val star2 = Offset(center.x + radius * 0.55f, center.y - radius * 0.25f)
                    val star3 = Offset(center.x - radius * 0.35f, center.y + radius * 0.5f)
                    
                    drawCircle(Color(0xFFFEF08A), radius * 0.045f, star1)
                    drawCircle(Color(0xFFFEF08A), radius * 0.035f, star2)
                    drawCircle(Color(0xCCFEF08A), radius * 0.03f, star3)
                    
                    // Star sparkle lines on star1
                    drawLine(Color(0xAAFEF08A), Offset(star1.x - radius * 0.09f, star1.y), Offset(star1.x + radius * 0.09f, star1.y), strokeWidth = 1.5f, cap = StrokeCap.Round)
                    drawLine(Color(0xAAFEF08A), Offset(star1.x, star1.y - radius * 0.09f), Offset(star1.x, star1.y + radius * 0.09f), strokeWidth = 1.5f, cap = StrokeCap.Round)
                    
                    // Moon Aura
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x33FDE047), Color(0x00FDE047)),
                            center = Offset(center.x + radius * 0.05f, center.y),
                            radius = radius * 0.75f
                        ),
                        radius = radius * 0.75f,
                        center = Offset(center.x + radius * 0.05f, center.y)
                    )

                    val moonPath = Path().apply {
                        addOval(Rect(center.x - radius * 0.42f, center.y - radius * 0.45f, center.x + radius * 0.45f, center.y + radius * 0.45f))
                    }
                    val cutoutPath = Path().apply {
                        addOval(Rect(center.x - radius * 0.22f, center.y - radius * 0.5f, center.x + radius * 0.65f, center.y + radius * 0.4f))
                    }
                    val finalMoon = Path.combine(PathOperation.Difference, moonPath, cutoutPath)
                    drawPath(
                        path = finalMoon,
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFFFFBEB), Color(0xFFFDE047), Color(0xFFF59E0B)),
                            start = Offset(center.x - radius * 0.4f, center.y - radius * 0.4f),
                            end = Offset(center.x + radius * 0.4f, center.y + radius * 0.4f)
                        )
                    )
                }
            }
            1, 2 -> { // Partly Cloudy
                if (isDay) {
                    // Sun in background
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x55FBBF24), Color(0x00FBBF24)),
                            center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f),
                            radius = radius * 0.6f
                        ),
                        radius = radius * 0.6f,
                        center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f)
                    )
                    drawCircle(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFFEF08A), Color(0xFFF59E0B)),
                            start = Offset(center.x - radius * 0.55f, center.y - radius * 0.55f),
                            end = Offset(center.x - radius * 0.05f, center.y - radius * 0.05f)
                        ),
                        radius = radius * 0.38f,
                        center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f)
                    )
                } else {
                    // Crescent Moon in background
                    val moonPath = Path().apply {
                        addOval(Rect(center.x - radius * 0.55f, center.y - radius * 0.55f, center.x - radius * 0.05f, center.y - radius * 0.05f))
                    }
                    val cutoutPath = Path().apply {
                        addOval(Rect(center.x - radius * 0.4f, center.y - radius * 0.6f, center.x + radius * 0.1f, center.y))
                    }
                    val finalMoon = Path.combine(PathOperation.Difference, moonPath, cutoutPath)
                    drawPath(
                        path = finalMoon,
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFFEF08A), Color(0xFFF59E0B))
                        )
                    )
                }
                
                // Front Volumetric Cloud with Highlights
                val cloudBrush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0), Color(0xFFCBD5E1)),
                    start = Offset(center.x - radius * 0.3f, center.y - radius * 0.2f),
                    end = Offset(center.x + radius * 0.4f, center.y + radius * 0.4f)
                )

                drawCircle(cloudBrush, radius * 0.36f, Offset(center.x - radius * 0.22f, center.y + radius * 0.12f))
                drawCircle(cloudBrush, radius * 0.48f, Offset(center.x + radius * 0.08f, center.y - radius * 0.06f))
                drawCircle(cloudBrush, radius * 0.32f, Offset(center.x + radius * 0.42f, center.y + radius * 0.15f))
                drawRoundRect(
                    brush = cloudBrush,
                    topLeft = Offset(center.x - radius * 0.38f, center.y + radius * 0.08f),
                    size = Size(radius * 0.95f, radius * 0.38f),
                    cornerRadius = CornerRadius(radius * 0.2f)
                )
            }
            3 -> { // Overcast / Gloomy Cloud
                // Darker Back Cloud
                val backCloudBrush = Brush.linearGradient(
                    colors = listOf(Color(0xFF64748B), Color(0xFF475569)),
                    start = Offset(center.x - radius * 0.4f, center.y - radius * 0.3f),
                    end = Offset(center.x + radius * 0.3f, center.y + radius * 0.3f)
                )
                drawCircle(backCloudBrush, radius * 0.38f, Offset(center.x - radius * 0.1f, center.y - radius * 0.2f))
                drawCircle(backCloudBrush, radius * 0.34f, Offset(center.x + radius * 0.25f, center.y - radius * 0.12f))
                
                // Foreground Cloud
                val cloudBrush = Brush.linearGradient(
                    colors = listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8), Color(0xFF64748B)),
                    start = Offset(center.x - radius * 0.4f, center.y - radius * 0.1f),
                    end = Offset(center.x + radius * 0.4f, center.y + radius * 0.45f)
                )
                drawCircle(cloudBrush, radius * 0.36f, Offset(center.x - radius * 0.28f, center.y + radius * 0.1f))
                drawCircle(cloudBrush, radius * 0.46f, Offset(center.x + radius * 0.02f, center.y - radius * 0.02f))
                drawCircle(cloudBrush, radius * 0.34f, Offset(center.x + radius * 0.38f, center.y + radius * 0.14f))
                drawRoundRect(
                    brush = cloudBrush,
                    topLeft = Offset(center.x - radius * 0.42f, center.y + radius * 0.06f),
                    size = Size(radius * 0.96f, radius * 0.38f),
                    cornerRadius = CornerRadius(radius * 0.2f)
                )
            }
            45, 48 -> { // Fog / Mist
                val strokeW = radius * 0.11f
                // Flowing layered mist bars
                drawLine(
                    brush = Brush.linearGradient(colors = listOf(Color(0x0094A3B8), Color(0xCC94A3B8), Color(0x0094A3B8))),
                    start = Offset(center.x - radius * 0.7f, center.y - radius * 0.3f),
                    end = Offset(center.x + radius * 0.7f, center.y - radius * 0.3f),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
                drawLine(
                    brush = Brush.linearGradient(colors = listOf(Color(0x00E2E8F0), Color(0xFFF1F5F9), Color(0x00E2E8F0))),
                    start = Offset(center.x - radius * 0.5f, center.y - radius * 0.05f),
                    end = Offset(center.x + radius * 0.5f, center.y - radius * 0.05f),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
                drawLine(
                    brush = Brush.linearGradient(colors = listOf(Color(0x0094A3B8), Color(0xDD94A3B8), Color(0x0094A3B8))),
                    start = Offset(center.x - radius * 0.65f, center.y + radius * 0.2f),
                    end = Offset(center.x + radius * 0.65f, center.y + radius * 0.2f),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
                drawLine(
                    brush = Brush.linearGradient(colors = listOf(Color(0x00CBD5E1), Color(0xAA94A3B8), Color(0x00CBD5E1))),
                    start = Offset(center.x - radius * 0.4f, center.y + radius * 0.42f),
                    end = Offset(center.x + radius * 0.4f, center.y + radius * 0.42f),
                    strokeWidth = strokeW * 0.85f,
                    cap = StrokeCap.Round
                )
            }
            61, 63, 65, 51, 53, 55, 80, 81, 82 -> { // Rain / Drizzle / Showers
                // Storm Cloud
                val cloudBrush = Brush.linearGradient(
                    colors = listOf(Color(0xFF94A3B8), Color(0xFF475569), Color(0xFF334155)),
                    start = Offset(center.x - radius * 0.4f, center.y - radius * 0.35f),
                    end = Offset(center.x + radius * 0.4f, center.y + radius * 0.25f)
                )
                drawCircle(cloudBrush, radius * 0.34f, Offset(center.x - radius * 0.25f, center.y - radius * 0.05f))
                drawCircle(cloudBrush, radius * 0.44f, Offset(center.x + radius * 0.06f, center.y - radius * 0.2f))
                drawCircle(cloudBrush, radius * 0.28f, Offset(center.x + radius * 0.38f, center.y - radius * 0.02f))
                drawRoundRect(
                    brush = cloudBrush,
                    topLeft = Offset(center.x - radius * 0.38f, center.y - radius * 0.08f),
                    size = Size(radius * 0.88f, radius * 0.34f),
                    cornerRadius = CornerRadius(radius * 0.18f)
                )

                // Raindrops with cyan/blue gradient and soft drop angle
                val dropBrush = Brush.linearGradient(
                    colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7)),
                    start = Offset(0f, 0f),
                    end = Offset(0f, radius * 0.3f)
                )
                val dropW = radius * 0.07f
                val dropH = radius * 0.26f
                
                // 3 diagonal rain streaks
                drawLine(
                    brush = dropBrush,
                    start = Offset(center.x - radius * 0.22f, center.y + radius * 0.22f),
                    end = Offset(center.x - radius * 0.32f, center.y + radius * 0.22f + dropH),
                    strokeWidth = dropW,
                    cap = StrokeCap.Round
                )
                drawLine(
                    brush = dropBrush,
                    start = Offset(center.x + radius * 0.05f, center.y + radius * 0.24f),
                    end = Offset(center.x - radius * 0.05f, center.y + radius * 0.24f + dropH),
                    strokeWidth = dropW,
                    cap = StrokeCap.Round
                )
                drawLine(
                    brush = dropBrush,
                    start = Offset(center.x + radius * 0.3f, center.y + radius * 0.22f),
                    end = Offset(center.x + radius * 0.2f, center.y + radius * 0.22f + dropH),
                    strokeWidth = dropW,
                    cap = StrokeCap.Round
                )
            }
            71, 73, 75, 77, 85, 86 -> { // Snow
                // Winter Cloud
                val cloudBrush = Brush.linearGradient(
                    colors = listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8)),
                    start = Offset(center.x - radius * 0.4f, center.y - radius * 0.35f),
                    end = Offset(center.x + radius * 0.4f, center.y + radius * 0.25f)
                )
                drawCircle(cloudBrush, radius * 0.34f, Offset(center.x - radius * 0.25f, center.y - radius * 0.05f))
                drawCircle(cloudBrush, radius * 0.44f, Offset(center.x + radius * 0.06f, center.y - radius * 0.2f))
                drawCircle(cloudBrush, radius * 0.28f, Offset(center.x + radius * 0.38f, center.y - radius * 0.02f))
                drawRoundRect(
                    brush = cloudBrush,
                    topLeft = Offset(center.x - radius * 0.38f, center.y - radius * 0.08f),
                    size = Size(radius * 0.88f, radius * 0.34f),
                    cornerRadius = CornerRadius(radius * 0.18f)
                )

                // Falling Snowflakes
                val flakePositions = listOf(
                    Offset(center.x - radius * 0.25f, center.y + radius * 0.35f),
                    Offset(center.x + radius * 0.05f, center.y + radius * 0.4f),
                    Offset(center.x + radius * 0.32f, center.y + radius * 0.35f)
                )
                for (pos in flakePositions) {
                    drawCircle(Color.White, radius * 0.07f, pos)
                    drawLine(Color(0xFFBAE6FD), Offset(pos.x - radius * 0.1f, pos.y), Offset(pos.x + radius * 0.1f, pos.y), strokeWidth = 2f, cap = StrokeCap.Round)
                    drawLine(Color(0xFFBAE6FD), Offset(pos.x, pos.y - radius * 0.1f), Offset(pos.x, pos.y + radius * 0.1f), strokeWidth = 2f, cap = StrokeCap.Round)
                }
            }
            95, 96, 99 -> { // Thunderstorm
                // Deep Dark Thunder Cloud
                val cloudBrush = Brush.linearGradient(
                    colors = listOf(Color(0xFF475569), Color(0xFF1E293B), Color(0xFF0F172A))
                )
                drawCircle(cloudBrush, radius * 0.34f, Offset(center.x - radius * 0.25f, center.y - radius * 0.05f))
                drawCircle(cloudBrush, radius * 0.44f, Offset(center.x + radius * 0.06f, center.y - radius * 0.2f))
                drawCircle(cloudBrush, radius * 0.28f, Offset(center.x + radius * 0.38f, center.y - radius * 0.02f))
                drawRoundRect(
                    brush = cloudBrush,
                    topLeft = Offset(center.x - radius * 0.38f, center.y - radius * 0.08f),
                    size = Size(radius * 0.88f, radius * 0.34f),
                    cornerRadius = CornerRadius(radius * 0.18f)
                )

                // Lightning Glow
                val boltPath = Path().apply {
                    moveTo(center.x + radius * 0.12f, center.y + radius * 0.05f)
                    lineTo(center.x - radius * 0.16f, center.y + radius * 0.38f)
                    lineTo(center.x + radius * 0.02f, center.y + radius * 0.38f)
                    lineTo(center.x - radius * 0.12f, center.y + radius * 0.72f)
                    lineTo(center.x + radius * 0.22f, center.y + radius * 0.32f)
                    lineTo(center.x + radius * 0.06f, center.y + radius * 0.32f)
                    close()
                }
                // Outer Bolt Glow
                drawPath(
                    path = boltPath,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFEF08A), Color(0xFFF59E0B))
                    )
                )
            }
            else -> { // Default Cloudy
                val cloudBrush = Brush.linearGradient(
                    colors = listOf(Color(0xFFF1F5F9), Color(0xFFCBD5E1), Color(0xFF94A3B8))
                )
                drawCircle(cloudBrush, radius * 0.34f, Offset(center.x - radius * 0.25f, center.y))
                drawCircle(cloudBrush, radius * 0.44f, Offset(center.x + radius * 0.05f, center.y - radius * 0.15f))
                drawCircle(cloudBrush, radius * 0.28f, Offset(center.x + radius * 0.35f, center.y + radius * 0.05f))
                drawRoundRect(
                    brush = cloudBrush,
                    topLeft = Offset(center.x - radius * 0.38f, center.y - radius * 0.05f),
                    size = Size(radius * 0.85f, radius * 0.32f),
                    cornerRadius = CornerRadius(radius * 0.16f)
                )
            }
        }
    }
}

// --- Astronomical Helper Functions ---

fun getSunPosition(
    currentTime: Long,
    sunriseStr: String?,
    sunsetStr: String?,
    isBn: Boolean
): Pair<String, Float> {
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
    val sunriseTime = try { sunriseStr?.let { parser.parse(it)?.time } } catch (e: Exception) { null }
    val sunsetTime = try { sunsetStr?.let { parser.parse(it)?.time } } catch (e: Exception) { null }

    if (sunriseTime == null || sunsetTime == null) {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val dayPercent = (hour * 60 + minute) / 1440f
        return if (hour in 6..18) {
            val angle = (dayPercent - 0.25f) / 0.5f * 180f
            val status = if (isBn) "আকাশে (${angle.toInt()}°)" else "In Sky (${angle.toInt()}°)"
            Pair(status, angle)
        } else {
            val status = if (isBn) "দিগন্তের নিচে (রাত)" else "Below Horizon (Night)"
            Pair(status, -1f)
        }
    }

    return if (currentTime in sunriseTime..sunsetTime) {
        val totalDaylight = sunsetTime - sunriseTime
        val elapsed = currentTime - sunriseTime
        val fraction = elapsed.toFloat() / totalDaylight
        val angle = fraction * 180f
        val status = when {
            angle < 15f -> if (isBn) "উদীয়মান (পূর্ব দিগন্তে)" else "Rising (East)"
            angle > 165f -> if (isBn) "অস্তগামী (পশ্চিম দিগন্তে)" else "Setting (West)"
            angle in 75f..105f -> if (isBn) "মধ্যগগন (মাথার উপর)" else "Zenith (Overhead)"
            else -> if (isBn) "আকাশে (${angle.toInt()}°)" else "In Sky (${angle.toInt()}°)"
        }
        Pair(status, angle)
    } else {
        val status = if (isBn) "দিগন্তের নিচে (রাত)" else "Below Horizon (Night)"
        Pair(status, -1f)
    }
}

fun getMoonPhase(currentTime: Long): Double {
    val referenceNewMoon = 1704974220000L
    val synodicPeriodMs = 29.530588853 * 24 * 60 * 60 * 1000
    val diff = currentTime - referenceNewMoon
    var phase = (diff % synodicPeriodMs).toDouble() / synodicPeriodMs
    if (phase < 0) phase += 1.0
    return phase
}

fun getMoonPhaseDetails(phase: Double, isBn: Boolean): Triple<String, String, String> {
    return when {
        phase < 0.03 || phase > 0.97 -> Triple(
            if (isBn) "নতুন চাঁদ (অমাবস্যা)" else "New Moon",
            if (isBn) "চাঁদ সম্পূর্ণ অদৃশ্য (০%)" else "Completely dark (0%)",
            "🌑"
        )
        phase >= 0.03 && phase < 0.22 -> Triple(
            if (isBn) "শুক্লপক্ষ ক্রিসেন্ট" else "Waxing Crescent",
            if (isBn) "সরু উজ্জ্বল অংশ (২৫%)" else "Thin sliver (25%)",
            "🌒"
        )
        phase >= 0.22 && phase < 0.28 -> Triple(
            if (isBn) "প্রথম চতুর্থাংশ" else "First Quarter",
            if (isBn) "অর্ধেক দৃশ্যমান (৫০%)" else "Half visible (50%)",
            "🌓"
        )
        phase >= 0.28 && phase < 0.47 -> Triple(
            if (isBn) "শুক্লপক্ষ গিব্বাস" else "Waxing Gibbous",
            if (isBn) "বেশিরভাগ দৃশ্যমান (৭৫%)" else "Mostly lit (75%)",
            "🌔"
        )
        phase >= 0.47 && phase < 0.53 -> Triple(
            if (isBn) "পূর্ণিমা (পূর্ণ চাঁদ)" else "Full Moon",
            if (isBn) "সম্পূর্ণ দৃশ্যমান (১০০%)" else "Fully illuminated (100%)",
            "🌕"
        )
        phase >= 0.53 && phase < 0.72 -> Triple(
            if (isBn) "কৃষ্ণপক্ষ গিব্বাস" else "Waning Gibbous",
            if (isBn) "আলো কমছে (৭৫%)" else "Decreasing (75%)",
            "🌖"
        )
        phase >= 0.72 && phase < 0.78 -> Triple(
            if (isBn) "শেষ চতুর্থাংশ" else "Third Quarter",
            if (isBn) "বাম অর্ধেক দৃশ্যমান (৫০%)" else "Left half lit (50%)",
            "🌗"
        )
        else -> Triple(
            if (isBn) "কৃষ্ণপক্ষ ক্রিসেন্ট" else "Waning Crescent",
            if (isBn) "শেষ সরু অংশ (২৫%)" else "Final sliver (25%)",
            "🌘"
        )
    }
}

fun getMoonPosition(
    currentTime: Long,
    phase: Double,
    sunriseStr: String?,
    sunsetStr: String?,
    isBn: Boolean
): Pair<String, Float> {
    val cal = Calendar.getInstance()
    val hourOfDay = cal.get(Calendar.HOUR_OF_DAY)
    val minute = cal.get(Calendar.MINUTE)
    val currentSunHour = hourOfDay + (minute / 60.0)
    
    val moonHourDiff = phase * 24.0
    val moonTransitHour = (currentSunHour - moonHourDiff + 24.0) % 24.0
    
    val isVisible = moonTransitHour in 6.0..18.0
    return if (isVisible) {
        val fraction = (moonTransitHour - 6.0) / 12.0
        val angle = (fraction * 180.0).toFloat()
        val status = when {
            angle < 15f -> if (isBn) "উদীয়মান (পূর্ব দিগন্তে)" else "Rising (East)"
            angle > 165f -> if (isBn) "অস্তগামী (পশ্চিম দিগন্তে)" else "Setting (West)"
            angle in 75f..105f -> if (isBn) "মধ্যগগন (মাথার উপর)" else "Zenith (Overhead)"
            else -> if (isBn) "আকাশে (${angle.toInt()}°)" else "In Sky (${angle.toInt()}°)"
        }
        Pair(status, angle)
    } else {
        val status = if (isBn) "দিগন্তের নিচে" else "Below Horizon"
        Pair(status, -1f)
    }
}

@Composable
fun SunMoonTrackerCard(
    weatherData: com.example.data.network.WeatherResponse,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    val currentTime = remember { System.currentTimeMillis() }
    val todaySunrise = weatherData.daily.sunrise.getOrNull(0)
    val todaySunset = weatherData.daily.sunset.getOrNull(0)
    
    val sunPos = remember(currentTime, todaySunrise, todaySunset, isBn) {
        getSunPosition(currentTime, todaySunrise, todaySunset, isBn)
    }
    
    val moonPhaseVal = remember(currentTime) {
        getMoonPhase(currentTime)
    }
    
    val moonDetails = remember(moonPhaseVal, isBn) {
        getMoonPhaseDetails(moonPhaseVal, isBn)
    }
    
    val moonPos = remember(currentTime, moonPhaseVal, todaySunrise, todaySunset, isBn) {
        getMoonPosition(currentTime, moonPhaseVal, todaySunrise, todaySunset, isBn)
    }

    // Format Sunrise / Sunset strings
    val sunriseDisplay = remember(todaySunrise) {
        todaySunrise?.substringAfter("T") ?: "06:00"
    }
    val sunsetDisplay = remember(todaySunset) {
        todaySunset?.substringAfter("T") ?: "18:00"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBn) "মহাজাগতিক ট্র্যাকার (সূর্য ও চন্দ্র)" else "Celestial Tracker (Sun & Moon)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
                Text(
                    text = "☀️ 🌙",
                    fontSize = 15.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Equal sized Side-by-Side Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Sun Tracker Card (Left)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = themeColors.displayBackground)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Title
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBn) "সূর্যের অবস্থান" else "Sun Position",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Curved Arc Canvas for Sun
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(76.dp)
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val baselineY = h * 0.82f
                                val arcRadius = (w * 0.44f).coerceAtMost(h * 0.72f)
                                val centerX = w / 2f

                                // Baseline horizon line
                                drawLine(
                                    color = Color.Gray.copy(alpha = 0.25f),
                                    start = Offset(10f, baselineY),
                                    end = Offset(w - 10f, baselineY),
                                    strokeWidth = 1.5.dp.toPx(),
                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                                )

                                // Horizon celestial arc (Semi-circle)
                                val arcRect = Rect(centerX - arcRadius, baselineY - arcRadius, centerX + arcRadius, baselineY + arcRadius)
                                drawArc(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFFFDE047).copy(alpha = 0.4f), Color(0xFFF59E0B).copy(alpha = 0.6f), Color(0xFFFDE047).copy(alpha = 0.4f))
                                    ),
                                    startAngle = 180f,
                                    sweepAngle = 180f,
                                    useCenter = false,
                                    style = Stroke(width = 2.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)),
                                    topLeft = arcRect.topLeft,
                                    size = arcRect.size
                                )

                                // If sun is above horizon
                                if (sunPos.second >= 0f) {
                                    val angleRad = (180f + sunPos.second) * (Math.PI / 180f)
                                    val sunX = centerX + Math.cos(angleRad).toFloat() * arcRadius
                                    val sunY = baselineY + Math.sin(angleRad).toFloat() * arcRadius

                                    // Outer Sun Glow
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(Color(0x99FDE047), Color(0x33F59E0B), Color(0x00F59E0B)),
                                            center = Offset(sunX, sunY),
                                            radius = 16.dp.toPx()
                                        ),
                                        radius = 16.dp.toPx(),
                                        center = Offset(sunX, sunY)
                                    )

                                    // Sun Core
                                    drawCircle(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0xFFFFFBEB), Color(0xFFF59E0B))
                                        ),
                                        radius = 7.dp.toPx(),
                                        center = Offset(sunX, sunY)
                                    )
                                } else {
                                    // Sun is below horizon (Night)
                                    drawCircle(
                                        color = Color.Gray.copy(alpha = 0.4f),
                                        radius = 6.dp.toPx(),
                                        center = Offset(centerX, baselineY + 10.dp.toPx())
                                    )
                                }
                            }

                            // East / West tags
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isBn) "পূর্ব 🌅" else "E 🌅",
                                    fontSize = 9.sp,
                                    color = themeColors.displayText.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = if (isBn) "🌇 পশ্চিম" else "🌇 W",
                                    fontSize = 9.sp,
                                    color = themeColors.displayText.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Status Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (sunPos.second >= 0f) Color(0xFFF59E0B).copy(alpha = 0.15f) else themeColors.displayText.copy(alpha = 0.08f)
                        ) {
                            Text(
                                text = sunPos.first,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (sunPos.second >= 0f) Color(0xFFF59E0B) else themeColors.displayText.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Sunrise & Sunset Times
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🌅 $sunriseDisplay",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = themeColors.displayText.copy(alpha = 0.75f)
                            )
                            Text(
                                text = "🌇 $sunsetDisplay",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = themeColors.displayText.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
                
                // 2. Moon Tracker Card (Right) - Exactly matching layout & size!
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = themeColors.displayBackground)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Title
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = moonDetails.third,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBn) "চন্দ্রের অবস্থান" else "Moon Position",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Curved Arc Canvas for Moon (Matching the Sun's Arc!)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(76.dp)
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val baselineY = h * 0.82f
                                val arcRadius = (w * 0.44f).coerceAtMost(h * 0.72f)
                                val centerX = w / 2f

                                // Baseline horizon line
                                drawLine(
                                    color = Color.Gray.copy(alpha = 0.25f),
                                    start = Offset(10f, baselineY),
                                    end = Offset(w - 10f, baselineY),
                                    strokeWidth = 1.5.dp.toPx(),
                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                                )

                                // Moon orbit arc (Semi-circle)
                                val arcRect = Rect(centerX - arcRadius, baselineY - arcRadius, centerX + arcRadius, baselineY + arcRadius)
                                drawArc(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFF93C5FD).copy(alpha = 0.4f), Color(0xFFC084FC).copy(alpha = 0.6f), Color(0xFF93C5FD).copy(alpha = 0.4f))
                                    ),
                                    startAngle = 180f,
                                    sweepAngle = 180f,
                                    useCenter = false,
                                    style = Stroke(width = 2.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)),
                                    topLeft = arcRect.topLeft,
                                    size = arcRect.size
                                )

                                // If Moon is in sky
                                if (moonPos.second >= 0f) {
                                    val angleRad = (180f + moonPos.second) * (Math.PI / 180f)
                                    val moonX = centerX + Math.cos(angleRad).toFloat() * arcRadius
                                    val moonY = baselineY + Math.sin(angleRad).toFloat() * arcRadius

                                    // Moon Aura Glow
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(Color(0x88BAE6FD), Color(0x33A855F7), Color(0x00A855F7)),
                                            center = Offset(moonX, moonY),
                                            radius = 16.dp.toPx()
                                        ),
                                        radius = 16.dp.toPx(),
                                        center = Offset(moonX, moonY)
                                    )

                                    // Luminous Moon Disc
                                    drawCircle(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0), Color(0xFF94A3B8))
                                        ),
                                        radius = 7.dp.toPx(),
                                        center = Offset(moonX, moonY)
                                    )
                                } else {
                                    // Moon below horizon
                                    drawCircle(
                                        color = Color.Gray.copy(alpha = 0.35f),
                                        radius = 6.dp.toPx(),
                                        center = Offset(centerX, baselineY + 10.dp.toPx())
                                    )
                                }
                            }

                            // East / West tags
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isBn) "পূর্ব 🌙" else "E 🌙",
                                    fontSize = 9.sp,
                                    color = themeColors.displayText.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = if (isBn) "পশ্চিম 🌌" else "W 🌌",
                                    fontSize = 9.sp,
                                    color = themeColors.displayText.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Status Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (moonPos.second >= 0f) themeColors.buttonEqualBg.copy(alpha = 0.15f) else themeColors.displayText.copy(alpha = 0.08f)
                        ) {
                            Text(
                                text = if (moonPos.second >= 0f) {
                                    if (isBn) "আকাশে (${moonPos.second.toInt()}°)" else "In Sky (${moonPos.second.toInt()}°)"
                                } else {
                                    moonPos.first
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (moonPos.second >= 0f) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Moon Phase & Illumination Subtitle
                        Text(
                            text = "${moonDetails.first} • ${moonDetails.second}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = themeColors.displayText.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
