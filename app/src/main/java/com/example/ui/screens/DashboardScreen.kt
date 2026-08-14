package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ToolCategory
import com.example.data.model.ToolType
import com.example.ui.components.InfoToggleButton
import com.example.ui.components.ToolInfoSection
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.theme.themeCardShadow
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import com.example.util.CalendarUtils
import com.example.util.LanguageManager
import com.example.util.scaleOnPress
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val lang = viewModel.selectedLanguage
    val isBn = lang == AppLanguage.BENGALI
    val context = LocalContext.current
    
    // Voice Search Launcher
    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val query = data?.get(0) ?: ""
            viewModel.processVoiceCommand(query)
        }
    }

    LaunchedEffect(Unit) {
        if (viewModel.weatherLocation.isEmpty() || viewModel.weatherLocation == "Dhaka") {
            getCurrentLocationInfo(context, isBn) { city, lat, lng ->
                if (city != null) {
                    viewModel.updateWeatherLocation(city, lat, lng)
                } else {
                    viewModel.fetchWeather()
                }
            }
        }
    }

    // Handle back press when a tool is open
    if (viewModel.selectedToolType != null) {
        ToolDetailView(
            tool = viewModel.selectedToolType!!,
            viewModel = viewModel,
            themeColors = themeColors,
            onBack = { viewModel.selectedToolType = null }
        )
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            LanguageManager.getString("app_title_dashboard", lang),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = themeColors.displayText
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = themeColors.background
                    )
                )
            },
            containerColor = themeColors.background
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Weather & Calendar Banner
                DashboardHeader(viewModel, themeColors)

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Search Bar
                SearchBarView(
                    viewModel = viewModel,
                    themeColors = themeColors,
                    onVoiceClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (isBn) "bn-BD" else "en-US")
                            putExtra(RecognizerIntent.EXTRA_PROMPT, if (isBn) "কিছু বলুন..." else "Say something...")
                        }
                        try {
                            voiceLauncher.launch(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Voice search not supported", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Category Filter
                CategoryFilterRow(viewModel, themeColors)

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Tools Grid
                DashboardToolsGrid(viewModel, themeColors)

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DashboardHeader(viewModel: CalculatorViewModel, themeColors: CalculatorThemeColors) {
    val lang = viewModel.selectedLanguage
    val isBn = lang == AppLanguage.BENGALI
    val dateInfo = remember { CalendarUtils.getMultiDateInfo(Calendar.getInstance(), isBn) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .themeCardShadow(themeColors),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Weather info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (viewModel.weatherData == null) (if (isBn) "অবস্থান..." else "Locating...") else viewModel.weatherLocation,
                        fontSize = 13.sp,
                        color = themeColors.displayText.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${viewModel.weatherData?.current?.temperature_2m?.toInt() ?: "--"}°C",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = themeColors.displayText
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = when(viewModel.weatherData?.current?.weather_code ?: -1) {
                                0 -> if (isBn) "পরিষ্কার" else "Clear"
                                1, 2, 3 -> if (isBn) "আংশিক মেঘলা" else "Partly Cloudy"
                                45, 48 -> if (isBn) "কুয়াশা" else "Foggy"
                                51, 53, 55 -> if (isBn) "ঝিরঝিরে বৃষ্টি" else "Drizzle"
                                61, 63, 65 -> if (isBn) "বৃষ্টি" else "Rainy"
                                71, 73, 75 -> if (isBn) "তুষারপাত" else "Snowy"
                                80, 81, 82 -> if (isBn) "বৃষ্টির ঝাপটা" else "Rain Showers"
                                95, 96, 99 -> if (isBn) "বজ্রবৃষ্টি" else "Thunderstorm"
                                else -> if (isBn) "লোডিং..." else "Loading..."
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.buttonEqualBg
                        )
                        Text(
                            text = if (isBn) "আর্দ্রতা: ${viewModel.weatherData?.current?.relative_humidity_2m ?: "--"}%" else "Humidity: ${viewModel.weatherData?.current?.relative_humidity_2m ?: "--"}%",
                            fontSize = 11.sp,
                            color = themeColors.displayText.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Vertical Divider
            Box(modifier = Modifier.width(1.dp).height(50.dp).background(themeColors.displayText.copy(alpha = 0.1f)))

            // Right: Multi-Calendar Date
            Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                Text(
                    text = dateInfo.englishDayName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = themeColors.buttonEqualBg
                )
                Text(
                    text = dateInfo.englishDate,
                    fontSize = 12.sp,
                    color = themeColors.displayText,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateInfo.bengaliDate,
                    fontSize = 11.sp,
                    color = themeColors.displayText.copy(alpha = 0.6f)
                )
                Text(
                    text = dateInfo.hijriDate,
                    fontSize = 11.sp,
                    color = themeColors.displayText.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun SearchBarView(
    viewModel: CalculatorViewModel,
    themeColors: com.example.ui.theme.CalculatorThemeColors,
    onVoiceClick: () -> Unit
) {
    val lang = viewModel.selectedLanguage
    
    OutlinedTextField(
        value = viewModel.dashboardSearchQuery,
        onValueChange = { viewModel.dashboardSearchQuery = it },
        placeholder = { 
            Text(
                text = LanguageManager.getString("search_tools", lang), 
                fontSize = 14.sp,
                color = themeColors.displayText.copy(alpha = 0.5f)
            ) 
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        leadingIcon = { Icon(Icons.Default.Search, null, tint = themeColors.buttonEqualBg) },
        trailingIcon = {
            IconButton(onClick = onVoiceClick) {
                Icon(Icons.Default.Mic, null, tint = themeColors.buttonEqualBg)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = themeColors.cardBg,
            unfocusedContainerColor = themeColors.cardBg,
            focusedBorderColor = themeColors.buttonEqualBg,
            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f),
            focusedTextColor = themeColors.displayText,
            unfocusedTextColor = themeColors.displayText,
            cursorColor = themeColors.buttonEqualBg
        ),
        singleLine = true
    )
}

@Composable
fun CategoryFilterRow(viewModel: CalculatorViewModel, themeColors: CalculatorThemeColors) {
    val lang = viewModel.selectedLanguage
    val categories = ToolCategory.values()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "All" Filter
        FilterChip(
            selected = viewModel.selectedToolCategoryFilter == null,
            onClick = { viewModel.selectedToolCategoryFilter = null },
            label = { Text(LanguageManager.getString("all", lang)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = themeColors.buttonEqualBg,
                selectedLabelColor = Color.White,
                containerColor = themeColors.cardBg,
                labelColor = themeColors.displayText
            )
        )

        categories.forEach { category ->
            FilterChip(
                selected = viewModel.selectedToolCategoryFilter == category,
                onClick = { viewModel.selectedToolCategoryFilter = category },
                label = { Text(category.getTitle(lang)) },
                leadingIcon = { Icon(category.icon, null, modifier = Modifier.size(16.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = themeColors.buttonEqualBg,
                    selectedLabelColor = Color.White,
                    containerColor = themeColors.cardBg,
                    labelColor = themeColors.displayText
                )
            )
        }
    }
}

@Composable
fun DashboardToolsGrid(viewModel: CalculatorViewModel, themeColors: CalculatorThemeColors) {
    val lang = viewModel.selectedLanguage
    val allTools = ToolType.values()
    
    val filteredTools = allTools.filter { tool ->
        val matchesCategory = viewModel.selectedToolCategoryFilter == null || tool.category == viewModel.selectedToolCategoryFilter
        val matchesSearch = viewModel.dashboardSearchQuery.isEmpty() || 
                           tool.getTitle(lang).contains(viewModel.dashboardSearchQuery, ignoreCase = true) ||
                           tool.getDescription(lang).contains(viewModel.dashboardSearchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    if (filteredTools.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text(
                LanguageManager.getString("no_results", lang),
                color = themeColors.displayText.copy(alpha = 0.5f),
                fontSize = 15.sp
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .heightIn(max = 2000.dp), // Adjust height dynamically if possible
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = false // Vertical scroll is handled by DashboardScreen's Column
        ) {
            items(filteredTools) { tool ->
                ToolCard(tool, viewModel, themeColors)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToolCard(tool: ToolType, viewModel: CalculatorViewModel, themeColors: CalculatorThemeColors) {
    val lang = viewModel.selectedLanguage
    val interactionSource = remember { MutableInteractionSource() }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scaleOnPress(interactionSource)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { viewModel.openTool(tool) },
                onLongClick = { /* Maybe add to favorites logic here */ }
            )
            .themeCardShadow(themeColors),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = themeColors.buttonEqualBg.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(tool.icon, null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(26.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = tool.getTitle(lang),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tool.getDescription(lang),
                fontSize = 11.sp,
                color = themeColors.displayText.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ToolDetailView(
    tool: ToolType,
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    onBack: () -> Unit
) {
    val lang = viewModel.selectedLanguage
    val isBn = lang == AppLanguage.BENGALI
    var isInfoExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        tool.getTitle(lang), 
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = themeColors.displayText)
                    }
                },
                actions = {
                    InfoToggleButton(
                        isExpanded = isInfoExpanded,
                        onToggle = { isInfoExpanded = !isInfoExpanded },
                        themeColors = themeColors
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = themeColors.background
                )
            )
        },
        containerColor = themeColors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 1. Tool Content Card
            AnimatedContent(
                targetState = tool,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "tool_content"
            ) { targetTool ->
                when (targetTool) {
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
                    ToolType.GPA -> GpaCalculatorCard(viewModel, themeColors)
                    ToolType.CGPA -> CgpaCalculatorCard(viewModel, themeColors)
                    ToolType.TUITION_FEES -> TuitionFeesCalculatorCard(viewModel, themeColors)
                    ToolType.ELECTRICITY_BILL -> ElectricityBillCalculatorCard(viewModel, themeColors)
                    ToolType.APPLIANCE_COST -> ApplianceEnergyCostCard(viewModel, themeColors)
                    ToolType.BATTERY_BACKUP -> BatteryBackupCard(viewModel, themeColors)
                    ToolType.FUEL_COST -> FuelCostCalculatorCard(viewModel, themeColors)
                    ToolType.SPEED_DISTANCE_TIME -> SpeedDistanceTimeCard(viewModel, themeColors)
                    ToolType.TEXT_COUNTER -> TextCounterCard(viewModel, themeColors)
                    ToolType.CLOTH_MEASUREMENT -> ClothMeasurementCard(viewModel, themeColors)
                    ToolType.GOLD_CALCULATOR -> GoldCalculatorCard(viewModel, themeColors)
                    ToolType.PASSWORD_GENERATOR -> PasswordGeneratorCard(viewModel, themeColors)
                    ToolType.PREGNANCY_DUE -> PregnancyDueDateCard(viewModel, themeColors)
                    ToolType.BLOOD_DONATION -> BloodDonationTrackerCard(viewModel, themeColors)
                    ToolType.ZAKAT -> ZakatCalculatorCard(viewModel, themeColors)
                    ToolType.SAVINGS_TARGET -> SavingsTargetCard(viewModel, themeColors)
                    ToolType.RESISTOR_CODE -> ResistorColorCodeCard(viewModel, themeColors)
                    ToolType.COLOR_CONVERTER -> ColorConverterCard(viewModel, themeColors)
                    ToolType.STOPWATCH_TIMER -> StopwatchTimerCard(viewModel, themeColors)
                    ToolType.NOTES_CHECKLIST -> NotesChecklistCard(viewModel, themeColors)
                    ToolType.WORLD_CLOCK -> WorldClockCard(viewModel, themeColors)
                    ToolType.UNIT_PRICE_COMPARER -> UnitPriceComparerCard(viewModel, themeColors)
                    ToolType.SIMPLE_COMPASS -> SimpleCompassCard(viewModel, themeColors)
                    ToolType.ASPECT_RATIO -> AspectRatioCard(viewModel, themeColors)
                    ToolType.RANDOM_NUMBER_PICKER -> RandomPickerCard(viewModel, themeColors)
                    ToolType.MULTI_CALENDAR -> MultiCalendarCard(viewModel, themeColors)
                    ToolType.QR_CODE -> QrCodeCard(viewModel, themeColors)
                    ToolType.PHOTO_LAB -> PhotoLabCard(viewModel, themeColors)
                    ToolType.WEATHER -> DynamicWeatherScreen(viewModel, themeColors, isBn)
                }
            }

            // 2. Info Section (Conditional)
            AnimatedVisibility(
                visible = isInfoExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ToolInfoSection(
                    title = if (isBn) "তথ্য ও নির্দেশিকা" else "Information & Guide",
                    infoItems = getToolInfoItems(tool, isBn),
                    themeColors = themeColors,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )
            }
        }
    }
}

private fun getCurrentLocationInfo(context: Context, isBn: Boolean, onResult: (String?, Double?, Double?) -> Unit) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    try {
        val location: Location? = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        
        if (location != null) {
            val geocoder = Geocoder(context, if (isBn) Locale("bn") else Locale.ENGLISH)
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                    val city = addresses.firstOrNull()?.locality ?: addresses.firstOrNull()?.subAdminArea
                    onResult(city, location.latitude, location.longitude)
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val city = addresses?.firstOrNull()?.locality ?: addresses?.firstOrNull()?.subAdminArea
                onResult(city, location.latitude, location.longitude)
            }
        } else {
            onResult(null, null, null)
        }
    } catch (e: Exception) {
        onResult(null, null, null)
    }
}

private fun getToolInfoItems(type: ToolType, isBn: Boolean): List<Pair<String, String>> {
    return when (type) {
        ToolType.BMI -> if (isBn) {
            listOf(
                "১. বিএমআই (BMI) কি?" to "BMI বা বডি মাস ইনডেক্স হলো শরীরের ওজন ও উচ্চতার একটি গাণিতিক হার, যা দিয়ে বোঝা যায় আপনার ওজন স্বাস্থ্যের জন্য আদর্শ কি না।",
                "২. সুস্থতার মাপকাঠি" to "১৮.৫ থেকে ২৪.৯ এর মধ্যে মান থাকলে তা স্বাভাবিক ওজন হিসেবে গণ্য করা হয়। ১৮.৫ এর নিচে হলে ওজন কম এবং ২৫ এর বেশি হলে অতিরিক্ত ওজন হিসেবে ধরা হয়।",
                "৩. ব্যবহারের নিয়ম" to "সঠিক ফলাফল পেতে সকালে খালি পেটে ওজন এবং জুতা ছাড়া উচ্চতা মেপে এখানে ইনপুট দিন।"
            )
        } else {
            listOf(
                "1. What is BMI?" to "Body Mass Index (BMI) is a value derived from the mass and height of a person to check if they are in a healthy weight range.",
                "2. Interpretation" to "A BMI between 18.5 and 24.9 is considered a healthy weight for most adults. Below 18.5 is underweight, and above 25 is overweight.",
                "3. Usage Tip" to "For the most accurate result, measure your weight in the morning and height without shoes."
            )
        }
        ToolType.EMI_LOAN -> if (isBn) {
            listOf(
                "১. ইএমআই (EMI) কি?" to "EMI মানে 'Equated Monthly Installment'। এটি একটি নির্দিষ্ট পরিমাণ টাকা যা আপনি ব্যাংক থেকে নেওয়া ঋণ শোধ করার জন্য প্রতি মাসে দিয়ে থাকেন।",
                "২. হিসাবের পদ্ধতি" to "এখানে আপনার মোট ঋণের পরিমাণ, বার্ষিক সুদের হার এবং ঋণের মেয়াদ (বছর/মাস) লিখলে প্রতি মাসের কিস্তি স্বয়ংক্রিয়ভাবে বের হবে।",
                "৩. নোট" to "ব্যাংকভেদে প্রসেসিং ফি বা হিডেন চার্জ আলাদা হতে পারে, তাই এটি একটি সম্ভাব্য ধারণা মাত্র।"
            )
        } else {
            listOf(
                "1. What is EMI?" to "EMI stands for Equated Monthly Installment. It is the fixed amount paid by a borrower to a lender at a specified date each month.",
                "2. Calculation" to "Enter the principal loan amount, interest rate, and loan tenure to calculate your monthly installment, total interest, and total payable amount.",
                "3. Note" to "Actual bank amounts may vary slightly due to processing fees or different compounding methods."
            )
        }
        ToolType.IDEAL_WEIGHT -> if (isBn) {
            listOf(
                "১. আদর্শ ওজন কি?" to "আপনার উচ্চতা এবং লিঙ্গ অনুযায়ী শরীরের কাঙ্ক্ষিত বা স্বাস্থ্যকর ওজনকে আদর্শ ওজন বলা হয়।",
                "২. হিসাবের পদ্ধতি" to "এটি রবিনসন বা ডিভাইন ফর্মুলা ব্যবহার করে আপনার জন্য উপযুক্ত ওজনের একটি রেঞ্জ প্রদান করে।",
                "৩. লক্ষ্য" to "আপনার বর্তমান ওজন এই রেঞ্জের বাইরে থাকলে ডায়েট ও ব্যায়ামের মাধ্যমে আদর্শ ওজনে আসার চেষ্টা করুন।"
            )
        } else {
            listOf(
                "1. What is Ideal Weight?" to "Ideal body weight is the weight that is considered healthy for your height, age, and gender.",
                "2. Calculation" to "The tool uses standard formulas like Robinson or Devine to estimate the weight range most suitable for your frame.",
                "3. Goal" to "If your current weight is outside this range, you can use it as a target for your fitness and nutrition plans."
            )
        }
        ToolType.AGE -> if (isBn) {
            listOf(
                "১. বয়স হিসাব" to "আপনার জন্ম তারিখ এবং বর্তমান তারিখ দিলে এই টুলটি আপনার নির্ভুল বয়স বছর, মাস এবং দিন হিসেবে দেখাবে।",
                "২. পরবর্তী জন্মদিন" to "আপনার পরবর্তী জন্মদিন আসতে ঠিক কত দিন বা কত মাস বাকি আছে তাও এখানে সহজে জানতে পারবেন।",
                "৩. দিন গণনা" to "সপ্তাহের কোন দিনে আপনার জন্ম হয়েছিল এবং আপনার জীবনের মোট কত দিন অতিক্রান্ত হয়েছে তাও এখানে দেখা যাবে।"
            )
        } else {
            listOf(
                "1. Age Calculation" to "Input your date of birth and the current date to calculate your exact age in years, months, and days.",
                "2. Next Birthday" to "See exactly how many months and days are left until your next birthday.",
                "3. Life Stats" to "Find out the day of the week you were born and the total number of days you have lived."
            )
        }
        ToolType.DISCOUNT -> if (isBn) {
            listOf(
                "১. ব্যবহারের নিয়ম" to "পণ্যটির আসল দাম এবং ডিসকাউন্টের শতকরা হার (%) লিখুন।",
                "২. ফলাফল" to "ডিসকাউন্টের পর আপনার কত টাকা সাশ্রয় হলো এবং ফাইনাল দাম কত হবে তা মুহূর্তেই জানতে পারবেন।",
                "৩. ভ্যাট/ট্যাক্স" to "যদি পণ্যের সাথে আলাদা ট্যাক্স যুক্ত থাকে, তবে ট্যাক্স অপশনটি ব্যবহার করে মোট চূড়ান্ত দাম হিসাব করুন।"
            )
        } else {
            listOf(
                "1. Usage" to "Enter the original price and the discount percentage (%) to see how much you save.",
                "2. Result" to "The tool displays the discounted amount and the final price you need to pay.",
                "3. Tax Integration" to "If there is any additional sales tax, you can include the tax percentage to calculate the absolute final price."
            )
        }
        ToolType.PERCENTAGE -> if (isBn) {
            listOf(
                "১. সাধারণ শতকরা" to "যেকোনো সংখ্যার কত শতাংশ বের করতে এটি ব্যবহার করুন। যেমন: ৫০০ এর ১৫% কত?",
                "২. শতাংশ পরিবর্তন" to "একটি সংখ্যা থেকে অন্য সংখ্যার বৃদ্ধির হার বা হ্রাসের হার কত শতাংশ তা বের করতে এটি কার্যকর।",
                "৩. অনুপাত" to "দুইটি সংখ্যার পারস্পরিক অনুপাত ও ভগ্নাংশ বের করার সুবিধাও এখানে রয়েছে।"
            )
        } else {
            listOf(
                "1. Basic Percentage" to "Calculate a percentage of any value. For example, what is 15% of 500?",
                "2. Percentage Change" to "Determine the percentage increase or decrease between two numbers.",
                "3. Ratio & Fraction" to "Find ratios and decimal-to-fraction conversions easily using this tool."
            )
        }
        ToolType.VAT_TAX -> if (isBn) {
            listOf(
                "১. ভ্যাট যোগ করা" to "যদি আসল দামের সাথে ভ্যাট যোগ করতে চান, তবে 'ADD' মোড সিলেক্ট করুন।",
                "২. ভ্যাট বাদ দেওয়া" to "যদি ভ্যাটসহ মোট দাম থেকে ভ্যাট কত তা আলাদা করতে চান, তবে 'REMOVE' মোড ব্যবহার করুন।",
                "৩. হিসাবের নির্ভুলতা" to "সঠিক হার (যেমন ১৫% বা ৫%) দিয়ে মোট বিল বা গ্রস অ্যামাউন্ট বের করুন।"
            )
        } else {
            listOf(
                "1. Add VAT" to "Select 'ADD' mode to calculate the total price including VAT on a base amount.",
                "2. Remove VAT" to "Select 'REMOVE' mode to find the original price before VAT was added to a gross total.",
                "3. Precision" to "Enter the correct local VAT rate to ensure accurate tax calculations for your bills."
            )
        }
        ToolType.PROFIT_LOSS -> if (isBn) {
            listOf(
                "১. ক্রয় ও বিক্রয় মূল্য" to "আপনার পণ্যের কেনা দাম এবং কত টাকায় বিক্রি করলেন তা ইনপুট দিন।",
                "২. মার্জিন হিসাব" to "এটি আপনাকে দেখাবে কত টাকা লাভ বা ক্ষতি হলো এবং তার শতকরা হার কত।",
                "৩. ব্যবসায়িক সিদ্ধান্ত" to "প্রফিট মার্জিন দেখে আপনার পণ্যের দাম পুনর্নির্ধারণ করতে পারেন।"
            )
        } else {
            listOf(
                "1. Buy & Sell Price" to "Enter the cost price and the selling price of your item.",
                "2. Margin Calculation" to "The tool calculates the absolute profit or loss amount and the percentage margin.",
                "3. Business Insight" to "Understanding your profit margin helps in making better pricing and stock decisions."
            )
        }
        ToolType.INTEREST -> if (isBn) {
            listOf(
                "১. সরল সুদ" to "শুধুমাত্র আসল টাকার ওপর যে সুদ হিসাব করা হয় তাকে সরল সুদ বলে।",
                "২. চক্রবৃদ্ধি সুদ" to "সুদ এবং আসল মিলে যে নতুন আসলের ওপর আবার সুদ ধরা হয় তাকে চক্রবৃদ্ধি সুদ বলে।",
                "৩. সঞ্চয় পরিকল্পনা" to "দীর্ঘমেয়াদী সঞ্চয়ে চক্রবৃদ্ধি সুদের প্রভাব কত বেশি হতে পারে তা এখানে দেখতে পাবেন।"
            )
        } else {
            listOf(
                "1. Simple Interest" to "Interest calculated only on the initial principal amount of a loan or deposit.",
                "2. Compound Interest" to "Interest calculated on the initial principal and also on the accumulated interest of previous periods.",
                "3. Planning" to "Use this to understand how your savings grow over time with different compounding frequencies."
            )
        }
        ToolType.BMR -> if (isBn) {
            listOf(
                "১. বিএমআর (BMR) কি?" to "BMR বা বেসাল মেটাবলিক রেট হলো সেই পরিমাণ ক্যালোরি যা আপনার শরীর সম্পূর্ণ বিশ্রামে থাকা অবস্থায় জীবন ধারণের জন্য খরচ করে।",
                "২. ক্যালোরি চাহিদা" to "আপনার কায়িক পরিশ্রমের মাত্রা (Sedentary, Active ইত্যাদি) অনুযায়ী দৈনিক কত ক্যালোরি খাবার গ্রহণ করা উচিত তা এখানে দেখা যাবে।",
                "৩. ওজন নিয়ন্ত্রণ" to "ওজন কমাতে চাইলে এই চাহিদার চেয়ে কম এবং ওজন বাড়াতে চাইলে বেশি ক্যালোরি গ্রহণ করতে হবে।"
            )
        } else {
            listOf(
                "1. What is BMR?" to "BMR (Basal Metabolic Rate) is the number of calories your body burns at rest to maintain vital functions.",
                "2. Calorie Needs" to "Based on your activity level, the tool estimates your Total Daily Energy Expenditure (TDEE).",
                "3. Weight Management" to "To lose weight, eat less than your TDEE; to gain weight, consume more than your TDEE."
            )
        }
        ToolType.WATER_INTAKE -> if (isBn) {
            listOf(
                "১. পানির প্রয়োজনীয়তা" to "আপনার শরীরের ওজন এবং দৈনিক ব্যায়ামের সময় অনুযায়ী কতটুকু পানি পান করা উচিত তা এখানে হিসাব করা হয়।",
                "২. ট্র্যাকার" to "সারাদিন কত গ্লাস পানি পান করলেন তা + বা - বাটনের মাধ্যমে ট্র্যাক করতে পারেন।",
                "৩. গ্লাসের মাপ" to "এখানে ১ গ্লাস পানি সমান ২৫০ মি.লি. বা ১ পোয়া হিসেবে ধরা হয়েছে।"
            )
        } else {
            listOf(
                "1. Water Requirement" to "The tool calculates how much water you should drink based on your body weight and daily exercise duration.",
                "2. Daily Tracker" to "Use the + and - buttons to track how many glasses you have consumed throughout the day.",
                "3. Measurement" to "One glass is estimated to be approximately 250ml."
            )
        }
        ToolType.FUEL_COST -> if (isBn) {
            listOf(
                "১. জ্বালানি খরচ" to "আপনার গন্তব্যের দূরত্ব, গাড়ির মাইলেজ এবং প্রতি লিটার জ্বালানির দাম দিয়ে মোট কত টাকা খরচ হবে তা জানতে পারবেন।",
                "২. সাশ্রয়ী ভ্রমণ" to "মাইলেজ বেশি হলে খরচ কম হবে। এই টুলটি ব্যবহার করে ভ্রমণের বাজেট আগে থেকেই ঠিক করে নিতে পারেন।"
            )
        } else {
            listOf(
                "1. Trip Cost" to "Input the distance, fuel efficiency (mileage), and fuel price to calculate the total cost of your journey.",
                "2. Travel Budget" to "Ideal for planning car trips and estimating fuel expenses beforehand."
            )
        }
        ToolType.GPA -> if (isBn) {
            listOf(
                "১. জিপিএ হিসাব" to "আপনার বিষয়ের নাম (ঐচ্ছিক) এবং প্রাপ্ত গ্রেড পয়েন্ট বা লেটার গ্রেড সিলেক্ট করে সেমিস্টার জিপিএ বের করুন।",
                "২. ক্রেডিট আওয়ার" to "যদি ক্রেডিট সিস্টেম থাকে, তবে সঠিক ক্রেডিট আওয়ার ইনপুট দিলে ওয়েটেড এভারেজ জিপিএ বের হবে।"
            )
        } else {
            listOf(
                "1. GPA Calculation" to "Select your grade or input grade points for each subject to calculate your semester GPA.",
                "2. Credit Hours" to "Enter the credits for each course to calculate the weighted average GPA accurately."
            )
        }
        ToolType.WEATHER -> if (isBn) {
            listOf(
                "১. আবহাওয়া তথ্য" to "আপনার বর্তমান অবস্থানের তাপমাত্রা, বাতাসের আর্দ্রতা এবং আকাশের অবস্থা সরাসরি দেখা যাবে।",
                "২. পূর্বাভাস" to "পরবর্তী ৭ দিনের আবহাওয়ার পূর্বাভাস দেখে আপনার ভ্রমণের পরিকল্পনা করুন।"
            )
        } else {
            listOf(
                "1. Current Weather" to "View real-time temperature, humidity, and weather conditions for your current location.",
                "2. Forecast" to "Plan your week with our 7-day weather forecast feature."
            )
        }
        ToolType.ZAKAT -> if (isBn) {
            listOf(
                "১. যাকাত কি?" to "যাকাত হলো ইসলামের পঞ্চস্তম্ভের একটি। এটি সম্পদের সেই নির্দিষ্ট অংশ যা নির্দিষ্ট নিসাব পরিমাণ মালের অধিকারী হলে বছরে একবার গরীবদের দিতে হয়।",
                "২. নিসাব" to "সাড়ে সাত তোলা সোনা অথবা সাড়ে বায়ান্ন তোলা রুপা বা এর সমপরিমাণ নগদ টাকা থাকলে যাকাত ফরয হয়।",
                "৩. হিসাব" to "আপনার সোনা, রুপা, নগদ টাকা ও বিনিয়োগ থেকে ঋণ বাদ দিয়ে যাকাতযোগ্য সম্পদের ২.৫% যাকাত হিসেবে প্রদান করুন।"
            )
        } else {
            listOf(
                "1. What is Zakat?" to "Zakat is one of the five pillars of Islam, requiring Muslims to donate a portion of their wealth to the poor.",
                "2. Nisab" to "Zakat is obligatory if your wealth exceeds the threshold (Nisab) of 7.5 tola gold or 52.5 tola silver.",
                "3. Calculation" to "Calculate 2.5% of your total zakatable assets after subtracting debts and liabilities."
            )
        }
        ToolType.CGPA -> if (isBn) {
            listOf(
                "১. সিজিপিএ (CGPA) কি?" to "CGPA হলো আপনার সকল সেমিস্টারের গড় ফলাফল। এটি দিয়ে আপনার পুরো শিক্ষাজীবনের সামগ্রিক পারফরম্যান্স বোঝা যায়।",
                "২. হিসাবের পদ্ধতি" to "প্রতিটি সেমিস্টারের প্রাপ্ত জিপিএ এবং ঐ সেমিস্টারের মোট ক্রেডিট আওয়ার ইনপুট দিয়ে ফাইনাল সিজিপিএ বের করুন।"
            )
        } else {
            listOf(
                "1. What is CGPA?" to "CGPA (Cumulative Grade Point Average) is the average of all your semester GPAs throughout your academic program.",
                "2. Calculation" to "Enter each semester's GPA and its corresponding total credits to compute your cumulative score."
            )
        }
        ToolType.ELECTRICITY_BILL -> if (isBn) {
            listOf(
                "১. ইউনিট হিসাব" to "আপনার মিটারের বর্তমান রিডিং থেকে পূর্বের রিডিং বাদ দিলে ব্যবহৃত মোট ইউনিট (kWh) পাওয়া যাবে।",
                "২. ট্যারিফ রেট" to "ইউনিট অনুযায়ী বিদ্যুতের দামের ধাপ আলাদা হয়। আপনার ব্যবহৃত স্লাব অনুযায়ী বিলের সম্ভাব্য পরিমাণ এখানে দেখা যাবে।"
            )
        } else {
            listOf(
                "1. Unit Calculation" to "Subtract your previous meter reading from the current one to find the total units (kWh) consumed.",
                "2. Tariff Rate" to "Electricity costs vary based on usage slabs. This tool estimates your bill based on standard tariff brackets."
            )
        }
        ToolType.GOLD_CALCULATOR -> if (isBn) {
            listOf(
                "১. সোনা ও রুপা" to "ভরি, আনা, রতি ও পয়েন্ট ইউনিটে সোনা বা রুপার ওজন বের করতে এটি ব্যবহার করুন।",
                "২. বাজারমূল্য" to "প্রতি ভরি বা প্রতি গ্রামের বর্তমান বাজার দর অনুযায়ী মোট কত টাকার সোনা আপনার কাছে আছে তা জানা যাবে।"
            )
        } else {
            listOf(
                "1. Gold & Silver" to "Convert weights between Vori, Anna, Ratti, and Point units easily.",
                "2. Valuation" to "Enter the current market rate per Vori or Gram to calculate the total value of your precious metals."
            )
        }
        ToolType.PREGNANCY_DUE -> if (isBn) {
            listOf(
                "১. ডিউ ডেট (EDD)" to "আপনার শেষ পিরিয়ডের তারিখ দিলে সম্ভাব্য প্রসবের তারিখ এবং বর্তমান গর্ভাবস্থার সপ্তাহ হিসাব করা যাবে।",
                "২. স্বাস্থ্য টিপস" to "এটি একটি গাণিতিক হিসাব মাত্র। শারীরিক অবস্থার জন্য নিয়মিত চিকিৎসকের পরামর্শ নিন।"
            )
        } else {
            listOf(
                "1. Due Date (EDD)" to "Calculate your estimated due date and current pregnancy week based on your last menstrual period (LMP).",
                "2. Medical Note" to "This is an estimation. Always consult your doctor for personalized medical advice."
            )
        }
        ToolType.PASSWORD_GENERATOR -> if (isBn) {
            listOf(
                "১. শক্তিশালী পাসওয়ার্ড" to "অন্তত ১২ ক্যারেক্টার লম্বা পাসওয়ার্ড ব্যবহার করুন এবং এতে বড় হাতের অক্ষর, ছোট হাতের অক্ষর, সংখ্যা ও স্পেশাল ক্যারেক্টার রাখুন।",
                "২. নিরাপত্তা" to "পাসওয়ার্ড কোথাও লিখে না রেখে সুরক্ষিত পাসওয়ার্ড ম্যানেজার ব্যবহার করার চেষ্টা করুন।"
            )
        } else {
            listOf(
                "1. Strong Password" to "Use at least 12 characters including uppercase, lowercase, numbers, and symbols for better security.",
                "2. Security Tip" to "Avoid reusing passwords across different sites and consider using a trusted password manager."
            )
        }
        ToolType.SAVINGS_TARGET -> if (isBn) {
            listOf(
                "১. সেভিংস লক্ষ্যমাত্রা" to "একটি নির্দিষ্ট সময়ের মধ্যে আপনার টার্গেট করা টাকা জমাতে প্রতি মাসে কত সঞ্চয় করতে হবে তা এখানে জানা যাবে।",
                "২. কম্পাউন্ড ইন্টারেস্ট" to "বার্ষিক মুনাফা বা সুদের হার যুক্ত করে আপনার জমানো টাকার ভবিষ্যৎ মান সহজেই বের করুন।"
            )
        } else {
            listOf(
                "1. Savings Target" to "Calculate the monthly amount you need to save to reach a specific financial goal within a certain time.",
                "2. Compound Interest" to "Include your annual interest rate to see the future value of your monthly contributions."
            )
        }
        ToolType.QR_CODE -> if (isBn) {
            listOf(
                "১. কিউআর স্ক্যানার" to "ক্যামেরা ব্যবহার করে যেকোনো কিউআর কোড স্ক্যান করে তথ্য বা লিঙ্ক সরাসরি ওপেন করুন।",
                "২. কোড তৈরি" to "আপনার ওয়েবসাইট লিঙ্ক, টেক্সট বা কন্টাক্ট ইনফরমেশন দিয়ে মুহূর্তেই নিজের কিউআর কোড বানিয়ে ডাউনলোড করুন।"
            )
        } else {
            listOf(
                "1. QR Scanner" to "Use your camera to scan any QR code and instantly access the embedded link or information.",
                "2. QR Generator" to "Create your own custom QR codes for websites, plain text, or contact details in seconds."
            )
        }
        ToolType.PHOTO_LAB -> if (isBn) {
            listOf(
                "১. রিসাইজ ও ক্রপ" to "পাসপোর্ট সাইজ বা নির্দিষ্ট পিক্সেল অনুযায়ী ছবি রিসাইজ করুন।",
                "২. ফরম্যাট পরিবর্তন" to "JPG থেকে PNG বা WebP ফরম্যাটে ছবি রূপান্তর করতে এটি ব্যবহার করুন।",
                "৩. ব্যাকগ্রাউন্ড" to "ছবির ব্যাকগ্রাউন্ড রিমুভ করার সুবিধাও এখানে পাওয়া যাবে।"
            )
        } else {
            listOf(
                "1. Resize & Crop" to "Resize images to specific dimensions or aspect ratios for social media or official documents.",
                "2. Format Conversion" to "Convert your photos between JPG, PNG, and WebP formats without losing quality.",
                "3. Background Removal" to "Easily remove backgrounds from your photos using our smart editing tool."
            )
        }
        ToolType.DATE_DIFF -> if (isBn) {
            listOf(
                "১. তারিখ নির্বাচন" to "দুটি নির্দিষ্ট তারিখের মধ্যকার ব্যবধান বের করতে ক্যালেন্ডার থেকে তারিখগুলো সিলেক্ট করুন।",
                "২. ফলাফল" to "ব্যবধানটি বছর, মাস, সপ্তাহ এবং দিন হিসেবে আলাদা আলাদা ভাবে দেখা যাবে।",
                "৩. ইভেন্ট প্ল্যানিং" to "কোনো বড় ইভেন্ট বা ডেডলাইন আসতে কতদিন বাকি তা জানতে এটি ব্যবহার করুন।"
            )
        } else {
            listOf(
                "1. Date Selection" to "Select two specific dates to find the exact duration between them.",
                "2. Breakdown" to "The result is displayed in total years, months, weeks, and days for your convenience.",
                "3. Planning" to "Perfect for tracking project deadlines or countdowns to special occasions."
            )
        }
        ToolType.TIP -> if (isBn) {
            listOf(
                "১. বিল ও টিপ" to "রেস্তোরাঁর মোট বিল এবং কত শতাংশ টিপ দিতে চান তা লিখুন।",
                "২. বিল ভাগ করা" to "আপনারা কতজন বন্ধু মিলে বিল দেবেন তা লিখলে জনপ্রতি কত টাকা দিতে হবে তা বেরিয়ে আসবে।",
                "৩. রাউন্ড ফিগার" to "হিসাব সহজ করতে জনপ্রতি অ্যামাউন্ট রাউন্ড করার সুবিধাও রয়েছে।"
            )
        } else {
            listOf(
                "1. Bill & Tip" to "Enter the total bill amount and the percentage of tip you wish to leave.",
                "2. Split Bill" to "Specify the number of people sharing the bill to see the individual contribution required.",
                "3. Rounding" to "Quickly round up the per-person total to make cash payments simpler."
            )
        }
        ToolType.TEXT_COUNTER -> if (isBn) {
            listOf(
                "১. শব্দ ও অক্ষর" to "আপনার টেক্সট পেস্ট করুন। এটি মুহূর্তেই মোট শব্দ ও অক্ষরের সংখ্যা গুনে দেবে।",
                "২. স্পেস ছাড়া গণনা" to "স্পেসসহ এবং স্পেস ছাড়া কতটি অক্ষর আছে তা আলাদা ভাবে জানা যাবে।",
                "৩. রাইটিং লিমিট" to "সোশ্যাল মিডিয়া পোস্ট বা আর্টিকেলের নির্দিষ্ট শব্দসীমা বজায় রাখতে এটি সহায়ক।"
            )
        } else {
            listOf(
                "1. Words & Characters" to "Paste your text to instantly count the number of words and characters.",
                "2. Space Handling" to "See the character count both with and without spaces for precise writing tasks.",
                "3. Social Media" to "Perfect for ensuring your captions or tweets stay within character limits."
            )
        }
        ToolType.CLOTH_MEASUREMENT -> if (isBn) {
            listOf(
                "১. দেশি মাপ" to "গজ, গিরা, ইঞ্চি এবং হাতের মধ্যে মাপ রূপান্তর করতে এটি ব্যবহার করুন।",
                "২. ব্যবহারের ক্ষেত্র" to "সাধারণত থান কাপড় বা দরজির কাজে এই মাপগুলো বেশি প্রয়োজন হয়।"
            )
        } else {
            listOf(
                "1. Traditional Units" to "Convert measurements between Gaj, Gira, Inch, and Haat.",
                "2. Common Usage" to "Ideal for tailoring and purchasing fabric in traditional markets."
            )
        }
        ToolType.APPLIANCE_COST -> if (isBn) {
            listOf(
                "১. ফ্যান, এসি, টিভি" to "প্রতিটি অ্যাপ্লায়েন্স কত ওয়াট এবং দিনে কত ঘণ্টা চলে তা দিলে মাসিক খরচ বের হবে।",
                "২. সাশ্রয় টিপস" to "বেশি ওয়াটের অ্যাপ্লায়েন্স কম ব্যবহার করে বিদ্যুৎ বিল কমানোর পরিকল্পনা করতে এটি সাহায্য করবে।"
            )
        } else {
            listOf(
                "1. Power Usage" to "Enter the wattage and daily usage hours of appliances like AC, Fan, or TV to see their monthly cost.",
                "2. Efficiency Tip" to "Identify high-consumption devices to better manage your electricity budget."
            )
        }
        ToolType.BATTERY_BACKUP -> if (isBn) {
            listOf(
                "১. আইপিএস/ব্যাটারি" to "ব্যাটারির ক্ষমতা (Ah) এবং লোড (Watt) দিলে ব্যাকআপ সময় (ঘণ্টা) বের হবে।",
                "২. সতর্কতা" to "ব্যাটারি দীর্ঘস্থায়ী করতে কখনোই পুরোপুরি ডিসচার্জ করবেন না।"
            )
        } else {
            listOf(
                "1. Battery Backup" to "Calculate the runtime in hours based on battery capacity (Ah) and total load (Watts).",
                "2. Longevity Tip" to "Avoid deep discharging your battery to ensure a longer lifespan for your IPS system."
            )
        }
        ToolType.BLOOD_DONATION -> if (isBn) {
            listOf(
                "১. রক্তদান ট্র্যাকার" to "আপনার সর্বশেষ রক্তদানের তারিখ দিলে পরবর্তী রক্তদানের সম্ভাব্য তারিখ জানতে পারবেন।",
                "২. যোগ্যতা" to "সাধারণত প্রতি ৩ মাস পর পর সুস্থ ব্যক্তিরা রক্তদান করতে পারেন।"
            )
        } else {
            listOf(
                "1. Donation Tracker" to "Keep track of your last blood donation and find out when you are eligible to donate again.",
                "2. Eligibility" to "Healthy adults can typically donate blood every 3 months (90 days)."
            )
        }
        ToolType.RESISTOR_CODE -> if (isBn) {
            listOf(
                "১. কালার কোড" to "রেজিস্টরের গায়ের রঙের ব্যান্ড সিলেক্ট করে এর ওহম (Ohm) মান বের করুন।",
                "২. টলারেন্স" to "রঙিন ব্যান্ড দিয়ে রেজিস্ট্যান্সের নির্ভুলতা বা টলারেন্সও জানা যাবে।"
            )
        } else {
            listOf(
                "1. Color Bands" to "Select the colors on your resistor to identify its resistance value in Ohms.",
                "2. Tolerance" to "The final color band indicates the precision or tolerance percentage of the resistor."
            )
        }
        ToolType.STOPWATCH_TIMER -> if (isBn) {
            listOf(
                "১. স্টপওয়াচ" to "ল্যাপ কাউন্টিং সুবিধাসহ নিখুঁত সময় গণনা করুন।",
                "২. টাইমার" to "রান্না, পড়াশোনা বা ব্যায়ামের জন্য কাউন্টডাউন টাইমার সেট করুন।"
            )
        } else {
            listOf(
                "1. Stopwatch" to "Measure precise time intervals with lap support for sports or tasks.",
                "2. Timer" to "Set countdown timers for activities like cooking, studying, or workouts."
            )
        }
        ToolType.NOTES_CHECKLIST -> if (isBn) {
            listOf(
                "১. নোটপ্যাড" to "জরুরি তথ্য বা আইডিয়া দ্রুত লিখে রাখার জন্য এটি ব্যবহার করুন।",
                "২. চেকলিস্ট" to "বাজারের তালিকা বা কাজের তালিকা (To-Do List) তৈরি করে সম্পন্ন কাজগুলো মার্ক করুন।"
            )
        } else {
            listOf(
                "1. Quick Notes" to "Write down important thoughts, ideas, or reminders instantly.",
                "2. Checklist" to "Create shopping lists or To-Do tasks and mark them as completed as you go."
            )
        }
        ToolType.WORLD_CLOCK -> if (isBn) {
            listOf(
                "১. বিশ্ব ঘড়ি" to "বিশ্বের বিভিন্ন শহরের বর্তমান সময় এবং টাইম জোন সরাসরি দেখুন।",
                "২. তুলনা" to "বিদেশের আত্মীয় বা ক্লায়েন্টের সাথে মিটিংয়ের জন্য সময়ের পার্থক্য বুঝতে এটি সাহায্য করবে।"
            )
        } else {
            listOf(
                "1. World Clock" to "Check the current time and time zone for major cities around the globe.",
                "2. Time Comparison" to "Easily understand time differences for international meetings or calls."
            )
        }
        ToolType.SIMPLE_COMPASS -> if (isBn) {
            listOf(
                "১. ডিজিটাল কম্পাস" to "সঠিক দিক নির্ণয় করতে ফোনটিকে মাটির সমান্তরালে ধরুন।",
                "২. স্পিরিট লেভেল" to "কোনো তলের সমতলতা পরীক্ষা করতে বাবল লেভেল ব্যবহার করুন।"
            )
        } else {
            listOf(
                "1. Digital Compass" to "Hold your phone flat (parallel to the ground) for accurate direction finding.",
                "2. Spirit Level" to "Use the integrated bubble level to check the flatness of any surface."
            )
        }
        ToolType.SPEED_DISTANCE_TIME -> if (isBn) {
            listOf(
                "১. গতিবেগ হিসাব" to "যেকোনো দুটি মান (গতি, দূরত্ব বা সময়) দিলে তৃতীয়টি স্বয়ংক্রিয়ভাবে বের হবে।",
                "২. সূত্র" to "দূরত্ব = গতিবেগ × সময়। এটি ভ্রমণের পরিকল্পনা বা গড় গতি বের করতে সহায়ক।"
            )
        } else {
            listOf(
                "1. Speed Calculation" to "Input any two values (Speed, Distance, or Time) to calculate the missing third value.",
                "2. Formula" to "Distance = Speed × Time. Useful for trip planning or calculating average speed."
            )
        }
        ToolType.TUITION_FEES -> if (isBn) {
            listOf(
                "১. টিউশন ফি" to "সেমিস্টার ফি, ওয়েভার (ছাড়) এবং অন্যান্য চার্জ ইনপুট দিয়ে মোট প্রদেয় টাকা বের করুন।",
                "২. ওয়েভার হিসাব" to "ফলাফলের ওপর ভিত্তি করে পাওয়া স্কলারশিপ বা ছাড়ের পরিমাণ সহজে সমন্বয় করা যায়।"
            )
        } else {
            listOf(
                "1. Tuition Fees" to "Calculate total payable fees after including semester costs, waivers, and miscellaneous charges.",
                "2. Waiver/Discount" to "Easily adjust scholarship percentages or fixed waivers to find your final balance."
            )
        }
        ToolType.COLOR_CONVERTER -> if (isBn) {
            listOf(
                "১. কালার কনভার্টার" to "RGB, HEX এবং HSL ফরম্যাটের মধ্যে কালার কোড রূপান্তর করুন।",
                "২. ডিজাইন টিপস" to "ওয়েব ডিজাইন বা গ্রাফিক ডিজাইনের কাজে সঠিক কালার কোড খুঁজে পেতে এটি ব্যবহার করুন।"
            )
        } else {
            listOf(
                "1. Color Conversion" to "Convert colors between RGB, HEX, and HSL formats instantly.",
                "2. Design Usage" to "Perfect for web developers and graphic designers to find exact color matches for their projects."
            )
        }
        ToolType.UNIT_PRICE_COMPARER -> if (isBn) {
            listOf(
                "১. সেরা দাম খুঁজুন" to "দুটি ভিন্ন পরিমাণের পণ্যের দাম তুলনা করে কোনটি বেশি সাশ্রয়ী তা বের করুন।",
                "২. কেনাকাটায় সাশ্রয়" to "প্যাকেট সাইজ এবং দামের পার্থক্যে বিভ্রান্ত না হয়ে সঠিক ডিলটি বেছে নিন।"
            )
        } else {
            listOf(
                "1. Find Best Value" to "Compare the unit price of two different sizes or packs to see which one offers the best deal.",
                "2. Smart Shopping" to "Don't be fooled by bulk packaging; calculate the per-unit cost to save money."
            )
        }
        ToolType.ASPECT_RATIO -> if (isBn) {
            listOf(
                "১. অ্যাসপেক্ট রেশিও" to "ছবির দৈর্ঘ্য ও প্রস্থের অনুপাত বের করুন (যেমন ১৬:৯ বা ৪:৩)।",
                "২. রেজোলিউশন" to "ভিডিও এডিটিং বা সোশ্যাল মিডিয়ায় ছবি আপলোডের জন্য সঠিক মাপ নির্ধারণ করতে এটি সহায়ক।"
            )
        } else {
            listOf(
                "1. Aspect Ratio" to "Calculate the ratio of width to height for images and videos (e.g., 16:9 or 4:3).",
                "2. Resolution Tip" to "Determine the correct dimensions for video editing or social media posts."
            )
        }
        ToolType.RANDOM_NUMBER_PICKER -> if (isBn) {
            listOf(
                "১. র‍্যান্ডম নম্বর" to "একটি নির্দিষ্ট সীমার মধ্যে লটারি বা গেমের জন্য নিরপেক্ষ নম্বর জেনারেট করুন।",
                "২. ডাইস ও কয়েন" to "লুডু খেলার ডাইস রোল বা কয়েন টস করার সুবিধাও এখানে রয়েছে।"
            )
        } else {
            listOf(
                "1. Random Number" to "Generate fair random numbers within a specific range for draws or games.",
                "2. Dice & Coin" to "Includes virtual dice rolling and coin flipping for quick decisions or games."
            )
        }
        ToolType.MULTI_CALENDAR -> if (isBn) {
            listOf(
                "১. বহু-ক্যালেন্ডার" to "একসাথে ইংরেজি, বাংলা এবং হিজরি ক্যালেন্ডার ও তারিখ দেখুন।",
                "২. বিশেষ দিবস" to "সরকারি ছুটি এবং ধর্মীয় উৎসবের তারিখগুলো মিলিয়ে দেখতে এটি কার্যকর।"
            )
        } else {
            listOf(
                "1. Multi-Calendar" to "View Gregorian, Bengali, and Hijri dates simultaneously in one view.",
                "2. Special Days" to "Handy for checking public holidays and religious festival dates across calendars."
            )
        }
        // Fallback for tools without specific info yet
        else -> if (isBn) {
            listOf(
                "নির্দেশিকা" to "এই টুলটি ব্যবহারের জন্য প্রয়োজনীয় তথ্য ইনপুট দিন এবং ফলাফল দেখুন।",
                "সহায়তা" to "আরও বিস্তারিত তথ্যের জন্য আমাদের পরবর্তী আপডেটগুলো অনুসরণ করুন।"
            )
        } else {
            listOf(
                "Usage" to "Input the required values to see the calculated result.",
                "Support" to "Follow our future updates for more detailed guides and features."
            )
        }
    }
}
