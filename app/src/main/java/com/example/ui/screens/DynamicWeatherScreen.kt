package com.example.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .padding(16.dp)
    ) {
        // Location Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = viewModel.weatherLocation.ifBlank { "Dhaka" },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
                Text(
                    text = if (isBn) "বর্তমান আবহাওয়া" else "Current Weather",
                    fontSize = 14.sp,
                    color = themeColors.displayText.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = { showSearchDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Location",
                    tint = themeColors.displayText
                )
            }
            IconButton(onClick = { viewModel.fetchWeather() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = themeColors.displayText
                )
            }
        }

        if (viewModel.weatherIsLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = themeColors.buttonEqualBg)
            }
        } else if (viewModel.weatherFetchError != null) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isBn) "তথ্য লোড করতে সমস্যা হয়েছে: ${viewModel.weatherFetchError}" else "Error loading data: ${viewModel.weatherFetchError}",
                        color = themeColors.displayText,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.fetchWeather() }) {
                        Text(if (isBn) "আবার চেষ্টা করুন" else "Retry")
                    }
                }
            }
        } else if (viewModel.weatherData != null) {
            val weatherData = viewModel.weatherData!!
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

                // Hourly Forecast
                item {
                    Text(
                        text = if (isBn) "প্রতি ঘণ্টার পূর্বাভাস" else "Hourly Forecast",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(24) { index ->
                            // Use next 24 hours
                            val timeStr = weatherData.hourly.time.getOrNull(index) ?: ""
                            val temp = weatherData.hourly.temperature_2m.getOrNull(index) ?: 0.0
                            val code = weatherData.hourly.weather_code.getOrNull(index) ?: 0
                            HourlyWeatherItem(timeStr, temp, code, themeColors, isBn)
                        }
                    }
                }

                // 7-Day Forecast
                item {
                    Text(
                        text = if (isBn) "৭ দিনের পূর্বাভাস" else "7-Day Forecast",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(weatherData.daily.time.size) { index ->
                    val timeStr = weatherData.daily.time[index]
                    val code = weatherData.daily.weather_code[index]
                    val minT = weatherData.daily.temperature_2m_min[index]
                    val maxT = weatherData.daily.temperature_2m_max[index]
                    val precip = weatherData.daily.precipitation_sum[index]
                    DailyWeatherItem(timeStr, code, minT, maxT, precip, themeColors, isBn)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (isBn) "কোনো তথ্য পাওয়া যায়নি" else "No Data Available",
                    color = themeColors.displayText
                )
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
    
    val condition = when (code) {
        0 -> if (isBn) "পরিষ্কার" else "Clear"
        1, 2, 3 -> if (isBn) "আংশিক মেঘলা" else "Partly Cloudy"
        45, 48 -> if (isBn) "কুয়াশা" else "Fog"
        51, 53, 55 -> if (isBn) "গুঁড়ি গুঁড়ি বৃষ্টি" else "Drizzle"
        61, 63, 65 -> if (isBn) "বৃষ্টি" else "Rain"
        71, 73, 75 -> if (isBn) "তুষারপাত" else "Snow"
        95, 96, 99 -> if (isBn) "বজ্রবৃষ্টি" else "Thunderstorm"
        else -> if (isBn) "অজানা" else "Unknown"
    }

    val icon = when (code) {
        0 -> if (isDay) Icons.Default.WbSunny else Icons.Default.NightsStay
        1, 2, 3 -> Icons.Default.Cloud
        61, 63, 65, 51, 53, 55 -> Icons.Default.WaterDrop
        95, 96, 99 -> Icons.Default.Thunderstorm
        else -> Icons.Default.Cloud
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = condition,
                tint = themeColors.buttonEqualText,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$temp°",
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.buttonEqualText
            )
            Text(
                text = condition,
                fontSize = 20.sp,
                color = themeColors.buttonEqualText.copy(alpha = 0.9f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherDetailItem(
                    icon = Icons.Default.WaterDrop,
                    value = "${current.relative_humidity_2m}%",
                    label = if (isBn) "আর্দ্রতা" else "Humidity",
                    themeColors = themeColors
                )
                WeatherDetailItem(
                    icon = Icons.Default.Air,
                    value = "${current.wind_speed_10m} km/h",
                    label = if (isBn) "বাতাস" else "Wind",
                    themeColors = themeColors
                )
                WeatherDetailItem(
                    icon = Icons.Default.Thermostat,
                    value = "${current.apparent_temperature}°",
                    label = if (isBn) "অনুভূত" else "Feels Like",
                    themeColors = themeColors
                )
            }
        }
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
fun HourlyWeatherItem(timeStr: String, temp: Double, code: Int, themeColors: CalculatorThemeColors, isBn: Boolean) {
    val time = try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
        val date = parser.parse(timeStr)
        val formatter = SimpleDateFormat("ha", Locale.US)
        formatter.format(date ?: Date())
    } catch (e: Exception) {
        "--"
    }

    val icon = when (code) {
        0 -> Icons.Default.WbSunny
        1, 2, 3 -> Icons.Default.Cloud
        61, 63, 65, 51, 53, 55 -> Icons.Default.WaterDrop
        95, 96, 99 -> Icons.Default.Thunderstorm
        else -> Icons.Default.Cloud
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
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = themeColors.buttonEqualBg,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${temp.toInt()}°",
            color = themeColors.displayText,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun DailyWeatherItem(
    timeStr: String,
    code: Int,
    minT: Double,
    maxT: Double,
    precip: Double,
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

    val icon = when (code) {
        0 -> Icons.Default.WbSunny
        1, 2, 3 -> Icons.Default.Cloud
        61, 63, 65, 51, 53, 55 -> Icons.Default.WaterDrop
        95, 96, 99 -> Icons.Default.Thunderstorm
        else -> Icons.Default.Cloud
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
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = themeColors.buttonEqualBg,
                modifier = Modifier.size(20.dp)
            )
            if (precip > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${precip.toInt()}mm",
                    color = themeColors.buttonEqualBg,
                    fontSize = 12.sp
                )
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
