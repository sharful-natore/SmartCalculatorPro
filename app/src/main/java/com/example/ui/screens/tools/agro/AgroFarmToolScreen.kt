package com.example.ui.screens.tools.agro

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgroFarmToolScreen(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    initialSection: AgroSection = AgroSection.CROP_CALENDAR,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isBn = viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI

    var currentSection by remember { mutableStateOf(initialSection) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var showCalculatorDialog by remember { mutableStateOf(false) }

    // Sub-filters for tabs
    var selectedCropSeason by remember { mutableStateOf("সব") }
    var selectedDiseasePathogen by remember { mutableStateOf("সব") }
    var selectedFertilizerTab by remember { mutableStateOf(0) } // 0: সার, 1: বালাইনাশক
    var selectedLivestockAnimal by remember { mutableStateOf("সব") }
    var selectedVaccineAnimal by remember { mutableStateOf("সব") }

    // Back handler behavior
    BackHandler {
        when {
            showCalculatorDialog -> showCalculatorDialog = false
            isSearchExpanded || searchQuery.isNotEmpty() -> {
                searchQuery = ""
                isSearchExpanded = false
            }
            currentSection != initialSection -> {
                currentSection = initialSection
            }
            else -> onBackClick()
        }
    }

    fun copyText(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, if (isBn) "$label কপি করা হয়েছে" else "Copied $label", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        containerColor = themeColors.background,
        topBar = {
            Column(
                modifier = Modifier
                    .background(themeColors.cardBg)
                    .statusBarsPadding()
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (isBn) currentSection.titleBn else currentSection.titleEn,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = themeColors.displayText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isBn) currentSection.subtitleBn else currentSection.subtitleEn,
                                style = MaterialTheme.typography.labelSmall,
                                color = themeColors.buttonEqualBg,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (showCalculatorDialog) {
                                showCalculatorDialog = false
                            } else if (isSearchExpanded || searchQuery.isNotEmpty()) {
                                searchQuery = ""
                                isSearchExpanded = false
                            } else if (currentSection != initialSection) {
                                currentSection = initialSection
                            } else {
                                onBackClick()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = themeColors.displayText
                            )
                        }
                    },
                    actions = {
                        // Quick Calculator Button
                        IconButton(
                            onClick = { showCalculatorDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Calculator",
                                tint = themeColors.buttonEqualBg
                            )
                        }

                        // Search Toggle
                        IconButton(
                            onClick = {
                                isSearchExpanded = !isSearchExpanded
                                if (!isSearchExpanded) searchQuery = ""
                            }
                        ) {
                            Icon(
                                imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Search",
                                tint = themeColors.displayText
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = themeColors.cardBg
                    )
                )

                // Search Bar Expandable
                AnimatedVisibility(
                    visible = isSearchExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = if (isBn) "ফসল, রোগ, সার, ওষুধ বা লক্ষণ খুঁজুন..." else "Search crops, diseases, fertilizers...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = themeColors.displayText.copy(alpha = 0.6f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = themeColors.buttonEqualBg
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = themeColors.displayText
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = themeColors.buttonEqualBg,
                                unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                                focusedTextColor = themeColors.displayText,
                                unfocusedTextColor = themeColors.displayText,
                                cursorColor = themeColors.buttonEqualBg,
                                focusedContainerColor = themeColors.background,
                                unfocusedContainerColor = themeColors.background
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 7 Sections Scrollable Tabs
                ScrollableTabRow(
                    selectedTabIndex = currentSection.ordinal,
                    containerColor = themeColors.cardBg,
                    contentColor = themeColors.buttonEqualBg,
                    edgePadding = 12.dp,
                    indicator = { tabPositions ->
                        if (currentSection.ordinal < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[currentSection.ordinal]),
                                color = themeColors.buttonEqualBg,
                                height = 3.dp
                            )
                        }
                    },
                    divider = {
                        HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.1f))
                    }
                ) {
                    AgroSection.values().forEach { sec ->
                        val isSelected = currentSection == sec
                        Tab(
                            selected = isSelected,
                            onClick = {
                                currentSection = sec
                                searchQuery = ""
                            },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = sec.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.5f),
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBn) sec.titleBn else sec.titleEn,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.7f),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentSection) {
                AgroSection.CROP_CALENDAR -> {
                    CropCalendarSectionView(
                        searchQuery = searchQuery,
                        selectedSeason = selectedCropSeason,
                        onSeasonChange = { selectedCropSeason = it },
                        themeColors = themeColors,
                        isBn = isBn,
                        onCopy = ::copyText
                    )
                }
                AgroSection.CROP_DISEASES -> {
                    CropDiseasesSectionView(
                        searchQuery = searchQuery,
                        selectedPathogen = selectedDiseasePathogen,
                        onPathogenChange = { selectedDiseasePathogen = it },
                        themeColors = themeColors,
                        isBn = isBn,
                        onCopy = ::copyText
                    )
                }
                AgroSection.FERTILIZERS_PESTICIDES -> {
                    FertilizersPesticidesSectionView(
                        searchQuery = searchQuery,
                        selectedTab = selectedFertilizerTab,
                        onTabChange = { selectedFertilizerTab = it },
                        themeColors = themeColors,
                        isBn = isBn,
                        onCopy = ::copyText,
                        onOpenCalculator = { showCalculatorDialog = true }
                    )
                }
                AgroSection.LIVESTOCK_DISEASES -> {
                    LivestockDiseasesSectionView(
                        searchQuery = searchQuery,
                        selectedAnimal = selectedLivestockAnimal,
                        onAnimalChange = { selectedLivestockAnimal = it },
                        themeColors = themeColors,
                        isBn = isBn,
                        onCopy = ::copyText
                    )
                }
                AgroSection.LIVESTOCK_FEED -> {
                    LivestockFeedSectionView(
                        searchQuery = searchQuery,
                        themeColors = themeColors,
                        isBn = isBn,
                        onCopy = ::copyText,
                        onOpenCalculator = { showCalculatorDialog = true }
                    )
                }
                AgroSection.VACCINATION_SCHEDULE -> {
                    VaccinationScheduleSectionView(
                        searchQuery = searchQuery,
                        selectedAnimal = selectedVaccineAnimal,
                        onAnimalChange = { selectedVaccineAnimal = it },
                        themeColors = themeColors,
                        isBn = isBn,
                        onCopy = ::copyText
                    )
                }
                AgroSection.AQUACULTURE_GUIDE -> {
                    AquacultureSectionView(
                        searchQuery = searchQuery,
                        themeColors = themeColors,
                        isBn = isBn,
                        onCopy = ::copyText,
                        onOpenCalculator = { showCalculatorDialog = true }
                    )
                }
            }
        }
    }

    // Interactive Agro Calculators Dialog
    if (showCalculatorDialog) {
        AgroCalculatorsModalDialog(
            themeColors = themeColors,
            isBn = isBn,
            initialTab = when (currentSection) {
                AgroSection.FERTILIZERS_PESTICIDES -> 0
                AgroSection.LIVESTOCK_FEED -> 1
                AgroSection.AQUACULTURE_GUIDE -> 2
                else -> 0
            },
            onDismiss = { showCalculatorDialog = false }
        )
    }
}

// -------------------------------------------------------------
// SECTION 1: CROP CALENDAR VIEW
// -------------------------------------------------------------
@Composable
private fun CropCalendarSectionView(
    searchQuery: String,
    selectedSeason: String,
    onSeasonChange: (String) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onCopy: (String, String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("সব ধরন") }
    var selectedMonth by remember { mutableStateOf("সব মাস") }

    val seasons = listOf("সব মৌসুম", "রবি (শীত)", "খরিপ-১ (গ্রীষ্ম)", "খরিপ-২ (বর্ষা)", "সারা বছর")
    val categories = listOf("সব ধরন", "শাকসবজি", "দানা ও খাদ্যশস্য", "ফলমূল", "ফুল ও শোভাবর্ধক", "মসলা ও অর্থকরী", "ডাল ও তৈলবীজ")
    val banglaMonths = listOf(
        "সব মাস",
        "বৈশাখ (এপ্রিল-মে)", "জ্যৈষ্ঠ (মে-জুন)", "আষাঢ় (জুন-জুলাই)", "শ্রাবণ (জুলাই-আগস্ট)",
        "ভাদ্র (আগস্ট-সেপ্টেম্বর)", "আশ্বিন (সেপ্টেম্বর-অক্টোবর)", "কার্তিক (অক্টোবর-নভেম্বর)", "অগ্রহায়ণ (নভেম্বর-ডিসেম্বর)",
        "পৌষ (ডিসেম্বর-জানুয়ারি)", "মাঘ (জানুয়ারি-ফেব্রুয়ারি)", "ফাল্গুন (ফেব্রুয়ারি-মার্চ)", "চৈত্র (মার্চ-এপ্রিল)"
    )

    val filteredList = remember(searchQuery, selectedSeason, selectedCategory, selectedMonth) {
        AgroDataProvider.cropCalendarList.filter { item ->
            val matchSeason = selectedSeason == "সব মৌসুম" || selectedSeason == "সব" ||
                    item.seasonBn.contains(selectedSeason) ||
                    (selectedSeason.contains("রবি") && item.seasonBn.contains("রবি")) ||
                    (selectedSeason.contains("খরিপ-১") && item.seasonBn.contains("খরিপ-১")) ||
                    (selectedSeason.contains("খরিপ-২") && item.seasonBn.contains("খরিপ-২")) ||
                    (selectedSeason.contains("সারা বছর") && item.seasonBn.contains("সারা বছর"))

            val matchCategory = selectedCategory == "সব ধরন" || item.categoryBn.contains(selectedCategory)

            val monthKeyword = if (selectedMonth != "সব মাস") {
                selectedMonth.substringBefore(" (")
            } else ""

            val matchMonth = monthKeyword.isBlank() ||
                    item.sowingTimeBn.contains(monthKeyword) ||
                    item.harvestTimeBn.contains(monthKeyword) ||
                    (monthKeyword == "বৈশাখ" && (item.sowingTimeBn.contains("বৈশাখ") || item.sowingTimeBn.contains("এপ্রিল") || item.sowingTimeBn.contains("মে"))) ||
                    (monthKeyword == "জ্যৈষ্ঠ" && (item.sowingTimeBn.contains("জ্যৈষ্ঠ") || item.sowingTimeBn.contains("মে") || item.sowingTimeBn.contains("জুন"))) ||
                    (monthKeyword == "আষাঢ়" && (item.sowingTimeBn.contains("আষাঢ়") || item.sowingTimeBn.contains("জুন") || item.sowingTimeBn.contains("জুলাই"))) ||
                    (monthKeyword == "শ্রাবণ" && (item.sowingTimeBn.contains("শ্রাবণ") || item.sowingTimeBn.contains("জুলাই") || item.sowingTimeBn.contains("আগস্ট"))) ||
                    (monthKeyword == "ভাদ্র" && (item.sowingTimeBn.contains("ভাদ্র") || item.sowingTimeBn.contains("আগস্ট") || item.sowingTimeBn.contains("সেপ্টেম্বর"))) ||
                    (monthKeyword == "আশ্বিন" && (item.sowingTimeBn.contains("আশ্বিন") || item.sowingTimeBn.contains("সেপ্টেম্বর") || item.sowingTimeBn.contains("অক্টোবর"))) ||
                    (monthKeyword == "কার্তিক" && (item.sowingTimeBn.contains("কার্তিক") || item.sowingTimeBn.contains("অক্টোবর") || item.sowingTimeBn.contains("নভেম্বর"))) ||
                    (monthKeyword == "অগ্রহায়ণ" && (item.sowingTimeBn.contains("অগ্রহায়ণ") || item.sowingTimeBn.contains("নভেম্বর") || item.sowingTimeBn.contains("ডিসেম্বর"))) ||
                    (monthKeyword == "পৌষ" && (item.sowingTimeBn.contains("পৌষ") || item.sowingTimeBn.contains("ডিসেম্বর") || item.sowingTimeBn.contains("জানুয়ারি"))) ||
                    (monthKeyword == "মাঘ" && (item.sowingTimeBn.contains("মাঘ") || item.sowingTimeBn.contains("জানুয়ারি") || item.sowingTimeBn.contains("ফেব্রুয়ারি"))) ||
                    (monthKeyword == "ফাল্গুন" && (item.sowingTimeBn.contains("ফাল্গুন") || item.sowingTimeBn.contains("ফেব্রুয়ারি") || item.sowingTimeBn.contains("মার্চ"))) ||
                    (monthKeyword == "চৈত্র" && (item.sowingTimeBn.contains("চৈত্র") || item.sowingTimeBn.contains("মার্চ") || item.sowingTimeBn.contains("এপ্রিল")))

            val matchQuery = searchQuery.isBlank() ||
                    item.cropNameBn.contains(searchQuery, ignoreCase = true) ||
                    item.cropNameEn.contains(searchQuery, ignoreCase = true) ||
                    item.categoryBn.contains(searchQuery, ignoreCase = true) ||
                    item.sowingTimeBn.contains(searchQuery, ignoreCase = true) ||
                    item.harvestTimeBn.contains(searchQuery, ignoreCase = true)

            matchSeason && matchCategory && matchMonth && matchQuery
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Season Overview Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2E7D32).copy(alpha = 0.08f)
                ),
                border = BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "বাংলাদেশের ৩টি প্রধান কৃষি মৌসুম ও সময়কাল" else "Bangladesh 3 Main Agro Seasons",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1B5E20)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isBn) {
                            "• রবি (শীতকাল): ১৫ অক্টোবর - ১৫ মার্চ (কার্তিক - ফাল্গুন) -> আলু, গম, সরিষা, ডাল, শীতকালীন শাকসবজি ও ফুল\n" +
                            "• খরিপ-১ (গ্রীষ্মকাল): ১৬ মার্চ - ৩০ জুন (চৈত্র - জ্যৈষ্ঠ) -> আউশ ধান, পাট, তিল, গ্রীষ্মকালীন সবজি ও ফল\n" +
                            "• খরিপ-২ (বর্ষাকাল): ১ জুলাই - ১৫ অক্টোবর (আষাঢ় - আশ্বিন) -> রোপা আমন ধান, বর্ষাকালীন সবজি ও ডাল"
                        } else {
                            "• Rabi (Winter): Oct 15 - Mar 15 -> Potato, Wheat, Mustard, Winter Vegetables & Flowers\n" +
                            "• Kharif-1 (Summer): Mar 16 - Jun 30 -> Aus Rice, Jute, Summer Vegetables & Fruits\n" +
                            "• Kharif-2 (Monsoon): Jul 1 - Oct 15 -> Transplanted Aman Rice & Monsoon crops"
                        },
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = themeColors.displayText.copy(alpha = 0.85f)
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // 1. Season filter chips
                Text(
                    text = if (isBn) "মৌসুম নির্বাচন:" else "Select Season:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.displayText.copy(alpha = 0.7f)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(seasons) { s ->
                        val isSelected = selectedSeason == s || (selectedSeason == "সব" && s == "সব মৌসুম")
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSeasonChange(s) },
                            label = { Text(s, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2E7D32),
                                selectedLabelColor = Color.White,
                                containerColor = themeColors.cardBg,
                                labelColor = themeColors.displayText
                            )
                        )
                    }
                }

                // 2. Category filter chips
                Text(
                    text = if (isBn) "ফসলের ধরন:" else "Crop Type:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.displayText.copy(alpha = 0.7f)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeColors.buttonEqualBg,
                                selectedLabelColor = themeColors.buttonEqualText,
                                containerColor = themeColors.cardBg,
                                labelColor = themeColors.displayText
                            )
                        )
                    }
                }

                // 3. Month quick selector
                Text(
                    text = if (isBn) "মাসভিত্তিক রোপণ খুঁজুন:" else "Find by Planting Month:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.displayText.copy(alpha = 0.7f)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(banglaMonths) { m ->
                        val isSelected = selectedMonth == m
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMonth = m },
                            label = { Text(m, fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE65100),
                                selectedLabelColor = Color.White,
                                containerColor = themeColors.cardBg,
                                labelColor = themeColors.displayText
                            )
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBn) "মোট প্রদর্শিত ফসল: ${filteredList.size} টি" else "Total Crops: ${filteredList.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText.copy(alpha = 0.8f)
                )
                if (selectedSeason != "সব মৌসুম" || selectedCategory != "সব ধরন" || selectedMonth != "সব মাস") {
                    TextButton(
                        onClick = {
                            onSeasonChange("সব মৌসুম")
                            selectedCategory = "সব ধরন"
                            selectedMonth = "সব মাস"
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(if (isBn) "ফিল্টার রিসেট" else "Reset Filter", fontSize = 11.sp, color = Color(0xFFD32F2F))
                    }
                }
            }
        }

        if (filteredList.isEmpty()) {
            item {
                EmptyStateCard(
                    message = if (isBn) "নির্বাচিত ফিল্টারে কোনো ফসল পাওয়া যায়নি" else "No crops found for selected filters",
                    themeColors = themeColors
                )
            }
        } else {
            items(filteredList, key = { it.id }) { item ->
                CropCalendarCard(item = item, themeColors = themeColors, isBn = isBn, onCopy = onCopy)
            }
        }
    }
}

@Composable
private fun CropCalendarCard(
    item: CropCalendarItem,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onCopy: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E7D32).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = item.cropNameBn,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = themeColors.displayText
                        )
                        Text(
                            text = "${item.cropNameEn} • ${item.categoryBn}",
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.displayText.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = themeColors.buttonEqualBg.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = item.seasonBn,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = themeColors.buttonEqualBg,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Timings Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(themeColors.background.copy(alpha = 0.6f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                InfoRowLabel(label = "বপন সময়:", value = item.sowingTimeBn, themeColors = themeColors)
                InfoRowLabel(label = "চারার বয়স:", value = item.transplantAgeBn, themeColors = themeColors)
                InfoRowLabel(label = "ফসল সংগ্রহ:", value = item.harvestTimeBn, themeColors = themeColors)
                InfoRowLabel(label = "শতকে বীজ:", value = item.seedRatePerDecimalBn, themeColors = themeColors)
                InfoRowLabel(label = "শতকে ফলন:", value = item.estimatedYieldBn, themeColors = themeColors)
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "জমি তৈরি:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = item.landPrepTipsBn,
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.displayText.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )

                    Text(
                        text = "বিশেষ পরামর্শ:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFE65100)
                    )
                    Text(
                        text = item.specialAdviceBn,
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.displayText.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(
                        text = if (expanded) "সংক্ষেপ করুন ▲" else "বিস্তারিত দেখুন ▼",
                        color = themeColors.buttonEqualBg,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = {
                        val text = "${item.cropNameBn} (${item.cropNameEn})\nমৌসুম: ${item.seasonBn}\nবপন: ${item.sowingTimeBn}\nসংগ্রহ: ${item.harvestTimeBn}\nফলন: ${item.estimatedYieldBn}\nটিপস: ${item.specialAdviceBn}"
                        onCopy(text, item.cropNameBn)
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = themeColors.displayText.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SECTION 2: CROP DISEASES VIEW
// -------------------------------------------------------------
@Composable
private fun CropDiseasesSectionView(
    searchQuery: String,
    selectedPathogen: String,
    onPathogenChange: (String) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onCopy: (String, String) -> Unit
) {
    val pathogens = listOf("সব", "ছত্রাকঘটিত", "কীটপতঙ্গ / পোকা", "ভাইরাসঘটিত")

    val filteredList = remember(searchQuery, selectedPathogen) {
        AgroDataProvider.cropDiseaseList.filter { item ->
            val matchPathogen = selectedPathogen == "সব" || item.pathogenTypeBn.contains(selectedPathogen)
            val matchQuery = searchQuery.isBlank() ||
                    item.cropNameBn.contains(searchQuery, ignoreCase = true) ||
                    item.diseaseNameBn.contains(searchQuery, ignoreCase = true) ||
                    item.symptomsBn.contains(searchQuery, ignoreCase = true) ||
                    item.chemicalRemedyBn.contains(searchQuery, ignoreCase = true)
            matchPathogen && matchQuery
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(pathogens) { p ->
                    val isSelected = selectedPathogen == p
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPathogenChange(p) },
                        label = { Text(p, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColors.buttonEqualBg,
                            selectedLabelColor = themeColors.buttonEqualText,
                            containerColor = themeColors.cardBg,
                            labelColor = themeColors.displayText
                        )
                    )
                }
            }
        }

        if (filteredList.isEmpty()) {
            item {
                EmptyStateCard(
                    message = if (isBn) "কোনো রোগবালাই তথ্য পাওয়া যায়নি" else "No disease records found",
                    themeColors = themeColors
                )
            }
        } else {
            items(filteredList, key = { it.id }) { item ->
                CropDiseaseCard(item = item, themeColors = themeColors, isBn = isBn, onCopy = onCopy)
            }
        }
    }
}

@Composable
private fun CropDiseaseCard(
    item: CropDiseaseItem,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onCopy: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val severityColor = when (item.severityLevelBn) {
        "উচ্চ ঝুঁকিপূর্ণ" -> Color(0xFFD32F2F)
        "মাঝারি" -> Color(0xFFE65100)
        else -> Color(0xFF2E7D32)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(severityColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            tint = severityColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.diseaseNameBn,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = themeColors.displayText
                        )
                        Text(
                            text = "${item.cropNameBn} • ${item.pathogenTypeBn}",
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.displayText.copy(alpha = 0.65f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = severityColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = item.severityLevelBn,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = severityColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "লক্ষণ:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = themeColors.displayText
            )
            Text(
                text = item.symptomsBn,
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.displayText.copy(alpha = 0.85f),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Chemical Spray Solution box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(themeColors.background.copy(alpha = 0.6f))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "বালাইনাশক চিকিৎসা ও স্প্রে মাত্রা:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = themeColors.buttonEqualBg
                        )
                    }
                    Text(
                        text = item.chemicalRemedyBn,
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.displayText,
                        lineHeight = 18.sp
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "জৈব বা সমন্বিত দমন (IPM):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = item.organicControlBn,
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.displayText.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "পূর্বপ্রতিরোধ ব্যবস্থা:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1565C0)
                    )
                    Text(
                        text = item.preventiveCareBn,
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.displayText.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(
                        text = if (expanded) "সংক্ষেপ করুন ▲" else "প্রতিরোধ ও জৈব দমন ▼",
                        color = themeColors.buttonEqualBg,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = {
                        val text = "${item.cropNameBn} - ${item.diseaseNameBn}\nলক্ষণ: ${item.symptomsBn}\nচিকিৎসা: ${item.chemicalRemedyBn}\nজৈব প্রতিকার: ${item.organicControlBn}"
                        onCopy(text, item.diseaseNameBn)
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = themeColors.displayText.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SECTION 3: FERTILIZERS & PESTICIDES VIEW
// -------------------------------------------------------------
@Composable
private fun FertilizersPesticidesSectionView(
    searchQuery: String,
    selectedTab: Int,
    onTabChange: (Int) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onCopy: (String, String) -> Unit,
    onOpenCalculator: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Quick Calculator Banner
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.12f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable { onOpenCalculator() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = themeColors.buttonEqualBg,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isBn) "সার ও স্প্রে দ্রবণ ক্যালকুলেটর" else "Fertilizer & Spray Calculator",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = themeColors.displayText
                        )
                        Text(
                            text = if (isBn) "জমির মাপ বা ড্রামের সাইজ দিয়ে তাৎক্ষণিক হিসাব করুন" else "Calculate exact doses by land or tank size",
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.displayText.copy(alpha = 0.7f)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = themeColors.buttonEqualBg
                )
            }
        }

        // Sub-tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = themeColors.cardBg,
            contentColor = themeColors.buttonEqualBg,
            divider = { HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.1f)) }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { onTabChange(0) },
                text = { Text(if (isBn) "সার পরিচিতি ও কাজ" else "Fertilizers", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { onTabChange(1) },
                text = { Text(if (isBn) "বালাইনাশক গ্রুপ ও মাত্রা" else "Pesticides", fontWeight = FontWeight.Bold) }
            )
        }

        if (selectedTab == 0) {
            val fertList = remember(searchQuery) {
                AgroDataProvider.fertilizerList.filter { item ->
                    searchQuery.isBlank() ||
                            item.nameBn.contains(searchQuery, ignoreCase = true) ||
                            item.nameEn.contains(searchQuery, ignoreCase = true) ||
                            item.nutrientSymbol.contains(searchQuery, ignoreCase = true) ||
                            item.primaryFunctionBn.contains(searchQuery, ignoreCase = true)
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(fertList, key = { it.id }) { fert ->
                    FertilizerItemCard(item = fert, themeColors = themeColors, onCopy = onCopy)
                }
            }
        } else {
            val pestList = remember(searchQuery) {
                AgroDataProvider.pesticideGroupList.filter { item ->
                    searchQuery.isBlank() ||
                            item.groupName.contains(searchQuery, ignoreCase = true) ||
                            item.popularBrandsBn.contains(searchQuery, ignoreCase = true) ||
                            item.targetPestBn.contains(searchQuery, ignoreCase = true) ||
                            item.categoryBn.contains(searchQuery, ignoreCase = true)
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(pestList, key = { it.id }) { pest ->
                    PesticideItemCard(item = pest, themeColors = themeColors, onCopy = onCopy)
                }
            }
        }
    }
}

@Composable
private fun FertilizerItemCard(
    item: FertilizerItem,
    themeColors: CalculatorThemeColors,
    onCopy: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(themeColors.buttonEqualBg.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.nutrientSymbol.take(2).trim(),
                            fontWeight = FontWeight.Bold,
                            color = themeColors.buttonEqualBg,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.nameBn,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = themeColors.displayText
                        )
                        Text(
                            text = item.nutrientSymbol,
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.buttonEqualBg
                        )
                    }
                }

                IconButton(
                    onClick = {
                        val text = "${item.nameBn}\nউপাদান: ${item.nutrientSymbol}\nমূল কাজ: ${item.primaryFunctionBn}\nঅভাবজনিত লক্ষণ: ${item.deficiencySymptomsBn}\nশতকে মাত্রা: ${item.dosePerDecimalBn}"
                        onCopy(text, item.nameBn)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = themeColors.displayText.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "কোন সারের কী কাজ:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF2E7D32)
            )
            Text(
                text = item.primaryFunctionBn,
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.displayText.copy(alpha = 0.85f),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            InfoRowLabel(label = "শতকে আনুমানিক মাত্রা:", value = item.dosePerDecimalBn, themeColors = themeColors)

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "অভাবজনিত লক্ষণ:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFE65100)
                    )
                    Text(
                        text = item.deficiencySymptomsBn,
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.displayText.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )

                    Text(
                        text = "বেশি দিলে ক্ষতি:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFD32F2F)
                    )
                    Text(
                        text = item.excessHarmBn,
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.displayText.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )

                    Text(
                        text = "প্রয়োগ পদ্ধতি:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1565C0)
                    )
                    Text(
                        text = item.applicationMethodBn,
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.displayText.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )
                }
            }

            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = if (expanded) "সংক্ষেপ করুন ▲" else "অভাব ও ক্ষতির লক্ষণ ▼",
                    color = themeColors.buttonEqualBg,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PesticideItemCard(
    item: PesticideGroupItem,
    themeColors: CalculatorThemeColors,
    onCopy: (String, String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00897B).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = Color(0xFF00897B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.groupName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = themeColors.displayText
                        )
                        Text(
                            text = item.categoryBn,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF00897B),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = {
                        val text = "${item.groupName} (${item.categoryBn})\nবাণিজ্যিক নাম: ${item.popularBrandsBn}\nকোন পোকা/রোগে: ${item.targetPestBn}\nমাত্রা: ${item.dilutionDoseBn}\nসতর্কতা: ${item.precautionsBn}"
                        onCopy(text, item.groupName)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = themeColors.displayText.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(themeColors.background.copy(alpha = 0.6f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                InfoRowLabel(label = "প্রচলিত ব্র্যান্ড নাম:", value = item.popularBrandsBn, themeColors = themeColors)
                InfoRowLabel(label = "যেসব পোকা/রোগে কাজ করে:", value = item.targetPestBn, themeColors = themeColors)
                InfoRowLabel(label = "স্প্রে প্রয়োগের মাত্রা:", value = item.dilutionDoseBn, themeColors = themeColors)
                InfoRowLabel(label = "নিরাপদ বিরতিকাল:", value = item.safetyWaitingPeriodBn, themeColors = themeColors)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "সতর্কতা: ${item.precautionsBn}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE65100),
                lineHeight = 17.sp
            )
        }
    }
}

// -------------------------------------------------------------
// SECTION 4: LIVESTOCK DISEASES VIEW
// -------------------------------------------------------------
@Composable
private fun LivestockDiseasesSectionView(
    searchQuery: String,
    selectedAnimal: String,
    onAnimalChange: (String) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onCopy: (String, String) -> Unit
) {
    val animalFilters = listOf("সব", "গরু ও মহিষ", "ছাগল ও ভেড়া", "সকল গবাদিপশু")

    val filteredList = remember(searchQuery, selectedAnimal) {
        AgroDataProvider.livestockDiseaseList.filter { item ->
            val matchAnimal = selectedAnimal == "সব" || item.affectedAnimalBn.contains(selectedAnimal) || item.affectedAnimalBn.contains("সকল")
            val matchQuery = searchQuery.isBlank() ||
                    item.diseaseNameBn.contains(searchQuery, ignoreCase = true) ||
                    item.diseaseNameEn.contains(searchQuery, ignoreCase = true) ||
                    item.symptomsBn.contains(searchQuery, ignoreCase = true) ||
                    item.immediateCareBn.contains(searchQuery, ignoreCase = true)
            matchAnimal && matchQuery
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(animalFilters) { a ->
                    val isSelected = selectedAnimal == a
                    FilterChip(
                        selected = isSelected,
                        onClick = { onAnimalChange(a) },
                        label = { Text(a, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColors.buttonEqualBg,
                            selectedLabelColor = themeColors.buttonEqualText,
                            containerColor = themeColors.cardBg,
                            labelColor = themeColors.displayText
                        )
                    )
                }
            }
        }

        // Warning banner
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F).copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "জরুরি বার্তা: মারাত্মক রোগের লক্ষণ দেখা দিলে প্রাথমিক সেবার পাশাপাশি অতিদ্রুত উপজেলা প্রাণিসম্পদ কর্মকর্তা বা রেজিস্টার্ড চিকিৎসকের শরণাপন্ন হন।"
                        else "Emergency: Contact an authorized veterinary doctor immediately upon severe symptoms.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD32F2F),
                        lineHeight = 17.sp
                    )
                }
            }
        }

        if (filteredList.isEmpty()) {
            item {
                EmptyStateCard(
                    message = if (isBn) "কোনো পশুর রোগ তথ্য পাওয়া যায়নি" else "No animal disease records found",
                    themeColors = themeColors
                )
            }
        } else {
            items(filteredList, key = { it.id }) { item ->
                LivestockDiseaseCard(item = item, themeColors = themeColors, isBn = isBn, onCopy = onCopy)
            }
        }
    }
}

@Composable
private fun LivestockDiseaseCard(
    item: LivestockDiseaseItem,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onCopy: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val isUrgent = item.severityBn.contains("মারাত্মক") || item.severityBn.contains("জরুরি")
    val badgeColor = if (isUrgent) Color(0xFFD32F2F) else Color(0xFFE65100)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(badgeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.diseaseNameBn,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = themeColors.displayText
                        )
                        Text(
                            text = "${item.affectedAnimalBn} • ${item.causeBn}",
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.displayText.copy(alpha = 0.65f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = item.severityBn,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "রোগের মূল লক্ষণ:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = themeColors.displayText
            )
            Text(
                text = item.symptomsBn,
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.displayText.copy(alpha = 0.85f),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Immediate Home Care box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2E7D32).copy(alpha = 0.08f))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "প্রাথমিক ও জরুরি যত্ন:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF2E7D32)
                        )
                    }
                    Text(
                        text = item.immediateCareBn,
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.displayText,
                        lineHeight = 18.sp
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "ডাক্তারি চিকিৎসা সহায়তা:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1565C0)
                    )
                    Text(
                        text = item.medicalTreatmentBn,
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.displayText.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "প্রতিরোধ ও প্রতিষেধক টিকা:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = themeColors.buttonEqualBg
                    )
                    Text(
                        text = item.preventionBn,
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.displayText.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(
                        text = if (expanded) "সংক্ষেপ করুন ▲" else "চিকিৎসা ও প্রতিরোধ গাইড ▼",
                        color = themeColors.buttonEqualBg,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = {
                        val text = "${item.diseaseNameBn} (${item.affectedAnimalBn})\nলক্ষণ: ${item.symptomsBn}\nপ্রাথমিক শুশ্রূষা: ${item.immediateCareBn}\nপ্রতিরোধ: ${item.preventionBn}"
                        onCopy(text, item.diseaseNameBn)
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = themeColors.displayText.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SECTION 5: LIVESTOCK FEED VIEW
// -------------------------------------------------------------
@Composable
private fun LivestockFeedSectionView(
    searchQuery: String,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onCopy: (String, String) -> Unit,
    onOpenCalculator: () -> Unit
) {
    val filteredList = remember(searchQuery) {
        AgroDataProvider.feedRecipeList.filter { item ->
            searchQuery.isBlank() ||
                    item.titleBn.contains(searchQuery, ignoreCase = true) ||
                    item.targetAnimalBn.contains(searchQuery, ignoreCase = true) ||
                    item.dailyFeedingRuleBn.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.12f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenCalculator() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isBn) "গরুর খাদ্য নির্ধারণ ক্যালকুলেটর" else "Cattle Feed Ration Calculator",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = themeColors.displayText
                            )
                            Text(
                                text = if (isBn) "ওজন ও দুধের পরিমাণ অনুযায়ী দৈনিক খাবার হিসাব করুন" else "Calculate daily grass, straw & feed by body weight",
                                style = MaterialTheme.typography.bodySmall,
                                color = themeColors.displayText.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = themeColors.buttonEqualBg
                    )
                }
            }
        }

        items(filteredList, key = { it.id }) { recipe ->
            LivestockFeedCard(recipe = recipe, themeColors = themeColors, onCopy = onCopy)
        }
    }
}

@Composable
private fun LivestockFeedCard(
    recipe: LivestockFeedRecipe,
    themeColors: CalculatorThemeColors,
    onCopy: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E7D32).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Grass,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = recipe.titleBn,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = themeColors.displayText
                        )
                        Text(
                            text = "${recipe.targetAnimalBn} • ${recipe.batchWeightBn}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = {
                        val ingredients = recipe.ingredientListBn.joinToString("\n") { "• ${it.first}: ${it.second}" }
                        val text = "${recipe.titleBn}\n$ingredients\n\nখাওয়ানোর নিয়ম: ${recipe.dailyFeedingRuleBn}\nপ্রস্তুত প্রণালী: ${recipe.preparationGuideBn}"
                        onCopy(text, recipe.titleBn)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = themeColors.displayText.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ingredients Breakdown Table
            Text(
                text = "উপাদান অনুপাত তালিকা:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = themeColors.displayText
            )

            Spacer(modifier = Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(themeColors.background.copy(alpha = 0.6f))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                recipe.ingredientListBn.forEach { (ingredient, amount) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "• $ingredient",
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.displayText.copy(alpha = 0.85f)
                        )
                        Text(
                            text = amount,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = themeColors.buttonEqualBg
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "দৈনিক খাওয়ানোর নিয়ম:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFE65100)
            )
            Text(
                text = recipe.dailyFeedingRuleBn,
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.displayText.copy(alpha = 0.85f),
                lineHeight = 18.sp
            )

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "তৈরি ও মিশ্রণের পদ্ধতি:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1565C0)
                    )
                    Text(
                        text = recipe.preparationGuideBn,
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.displayText.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "বিশেষ যত্ন ও পরামর্শ:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = themeColors.buttonEqualBg
                    )
                    Text(
                        text = recipe.specialAdviceBn,
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.displayText.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )
                }
            }

            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = if (expanded) "সংক্ষেপ করুন ▲" else "প্রস্তুত পদ্ধতি ও পরামর্শ ▼",
                    color = themeColors.buttonEqualBg,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// -------------------------------------------------------------
// SECTION 6: VACCINATION SCHEDULE VIEW
// -------------------------------------------------------------
@Composable
private fun VaccinationScheduleSectionView(
    searchQuery: String,
    selectedAnimal: String,
    onAnimalChange: (String) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onCopy: (String, String) -> Unit
) {
    val animalFilters = listOf("সব", "গরু ও মহিষ", "ছাগল ও ভেড়া", "মুরগি / পোল্ট্রি")

    val filteredList = remember(searchQuery, selectedAnimal) {
        AgroDataProvider.vaccinationList.filter { item ->
            val matchAnimal = selectedAnimal == "সব" || item.animalTypeBn.contains(selectedAnimal) || item.animalTypeBn.contains("সকল")
            val matchQuery = searchQuery.isBlank() ||
                    item.vaccineNameBn.contains(searchQuery, ignoreCase = true) ||
                    item.targetDiseaseBn.contains(searchQuery, ignoreCase = true) ||
                    item.firstDoseAgeBn.contains(searchQuery, ignoreCase = true)
            matchAnimal && matchQuery
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(animalFilters) { a ->
                    val isSelected = selectedAnimal == a
                    FilterChip(
                        selected = isSelected,
                        onClick = { onAnimalChange(a) },
                        label = { Text(a, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColors.buttonEqualBg,
                            selectedLabelColor = themeColors.buttonEqualText,
                            containerColor = themeColors.cardBg,
                            labelColor = themeColors.displayText
                        )
                    )
                }
            }
        }

        // Vaccine guideline tip
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1565C0).copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Vaccines,
                        contentDescription = null,
                        tint = Color(0xFF1565C0),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "ভ্যাকসিনের মূল শর্ত: শুধুমাত্র সুস্থ পশুকে ভ্যাকসিন দিন। ভ্যাকসিন দেওয়ার ৭-১০ দিন পূর্বে কৃমিনাশক দিয়ে শরীর সুস্থ রাখা জরুরি।"
                        else "Rule of thumb: Vaccinate healthy animals only. Deworm 7-10 days prior to vaccination.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1565C0),
                        lineHeight = 17.sp
                    )
                }
            }
        }

        items(filteredList, key = { it.id }) { item ->
            VaccinationItemCard(item = item, themeColors = themeColors, onCopy = onCopy)
        }
    }
}

@Composable
private fun VaccinationItemCard(
    item: VaccinationScheduleItem,
    themeColors: CalculatorThemeColors,
    onCopy: (String, String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(themeColors.buttonEqualBg.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vaccines,
                            contentDescription = null,
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.vaccineNameBn,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = themeColors.displayText
                        )
                        Text(
                            text = "${item.animalTypeBn} • ${item.targetDiseaseBn}",
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.displayText.copy(alpha = 0.65f)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        val text = "${item.vaccineNameBn} (${item.animalTypeBn})\nটার্গেট রোগ: ${item.targetDiseaseBn}\nপ্রথম ডোজ: ${item.firstDoseAgeBn}\nবুস্টার: ${item.boosterIntervalBn}\nপ্রয়োগের স্থান: ${item.routeBn}\nডোজ: ${item.doseAmountBn}\nসতর্কতা: ${item.precautionsBn}"
                        onCopy(text, item.vaccineNameBn)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = themeColors.displayText.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(themeColors.background.copy(alpha = 0.6f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                InfoRowLabel(label = "প্রথম প্রয়োগের বয়স:", value = item.firstDoseAgeBn, themeColors = themeColors)
                InfoRowLabel(label = "বুস্টার ও পরবর্তী ডোজ:", value = item.boosterIntervalBn, themeColors = themeColors)
                InfoRowLabel(label = "প্রয়োগের স্থান ও মাত্রা:", value = "${item.routeBn} (${item.doseAmountBn})", themeColors = themeColors)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "সতর্কতা: ${item.precautionsBn}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE65100),
                lineHeight = 17.sp
            )
        }
    }
}

// -------------------------------------------------------------
// SECTION 7: AQUACULTURE VIEW
// -------------------------------------------------------------
@Composable
private fun AquacultureSectionView(
    searchQuery: String,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onCopy: (String, String) -> Unit,
    onOpenCalculator: () -> Unit
) {
    val filteredList = remember(searchQuery) {
        AgroDataProvider.aquacultureTopicList.filter { item ->
            searchQuery.isBlank() ||
                    item.titleBn.contains(searchQuery, ignoreCase = true) ||
                    item.categoryBn.contains(searchQuery, ignoreCase = true) ||
                    item.summaryBn.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.12f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenCalculator() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isBn) "মাছের খাবার ও চুন ক্যালকুলেটর" else "Fish Feed & Liming Calculator",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = themeColors.displayText
                            )
                            Text(
                                text = if (isBn) "পুকুরের মাপ ও মাছের ওজন দিয়ে দৈনিক খাবার বের করুন" else "Calculate daily feed & liming requirements",
                                style = MaterialTheme.typography.bodySmall,
                                color = themeColors.displayText.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = themeColors.buttonEqualBg
                    )
                }
            }
        }

        items(filteredList, key = { it.id }) { topic ->
            AquacultureTopicCard(topic = topic, themeColors = themeColors, onCopy = onCopy)
        }
    }
}

@Composable
private fun AquacultureTopicCard(
    topic: AquacultureTopicItem,
    themeColors: CalculatorThemeColors,
    onCopy: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0288D1).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = Color(0xFF0288D1),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = topic.titleBn,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = themeColors.displayText
                        )
                        Text(
                            text = topic.categoryBn,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF0288D1),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = {
                        val steps = topic.detailedStepsBn.joinToString("\n")
                        val text = "${topic.titleBn}\nসারসংক্ষেপ: ${topic.summaryBn}\n\n$steps"
                        onCopy(text, topic.titleBn)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = themeColors.displayText.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = topic.summaryBn,
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.displayText.copy(alpha = 0.85f),
                lineHeight = 18.sp
            )

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    topic.detailedStepsBn.forEach { step ->
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.displayText.copy(alpha = 0.9f),
                            lineHeight = 18.sp
                        )
                    }

                    if (!topic.calculationTipBn.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(themeColors.buttonEqualBg.copy(alpha = 0.1f))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "💡 হিসাব সহায়িকা: ${topic.calculationTipBn}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = themeColors.buttonEqualBg,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = if (expanded) "সংক্ষেপ করুন ▲" else "ধাপসমূহ ও বিস্তারিত গাইড ▼",
                    color = themeColors.buttonEqualBg,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// -------------------------------------------------------------
// INTERACTIVE AGRO CALCULATORS MODAL DIALOG
// -------------------------------------------------------------
@Composable
fun AgroCalculatorsModalDialog(
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    initialTab: Int = 0,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(initialTab) }

    // 1. Fertilizer Dose Inputs
    var landDecimalStr by remember { mutableStateOf("১০") }
    // Spray Dilution Inputs
    var tankLitersStr by remember { mutableStateOf("১৬") }
    var dosePerLiterStr by remember { mutableStateOf("২") }

    // 2. Cattle Feed Inputs
    var cattleWeightStr by remember { mutableStateOf("২০০") }
    var dailyMilkStr by remember { mutableStateOf("৫") }

    // 3. Fish Feed Inputs
    var pondDecimalStr by remember { mutableStateOf("২০") }
    var totalFishCountStr by remember { mutableStateOf("৮০০") }
    var avgFishWeightGramStr by remember { mutableStateOf("১৫০") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
            ) {
                Text(if (isBn) "ঠিক আছে" else "Close", color = themeColors.buttonEqualText)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = themeColors.buttonEqualBg
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBn) "কৃষি ও খামার ক্যালকুলেটর" else "Agro & Farm Calculators",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = themeColors.displayText
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = themeColors.cardBg,
                    contentColor = themeColors.buttonEqualBg
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(if (isBn) "সার ও স্প্রে" else "Fertilizer", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(if (isBn) "গরুর খাদ্য" else "Cattle Feed", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text(if (isBn) "মাছের খাবার" else "Fish Feed", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> {
                        // Fertilizer & Spray Dilution
                        val landDec = landDecimalStr.toDoubleOrNull() ?: 0.0
                        val tankL = tankLitersStr.toDoubleOrNull() ?: 0.0
                        val doseL = dosePerLiterStr.toDoubleOrNull() ?: 0.0

                        val ureaKg = (landDec * 0.8).coerceAtLeast(0.0)
                        val tspKg = (landDec * 0.5).coerceAtLeast(0.0)
                        val mopKg = (landDec * 0.5).coerceAtLeast(0.0)
                        val gypsumKg = (landDec * 0.35).coerceAtLeast(0.0)
                        val totalSprayDose = (tankL * doseL).coerceAtLeast(0.0)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "১. সার প্রয়োগ হিসাব (জমির পরিমাণ দিয়ে):",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = themeColors.buttonEqualBg
                            )
                            OutlinedTextField(
                                value = landDecimalStr,
                                onValueChange = { landDecimalStr = it },
                                label = { Text("জমির পরিমাণ (শতক / ডেসিমল)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(themeColors.cardBg)
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("• ইউরিয়া (Urea): %.2f কেজি (২-৩ কিস্তিতে)".format(ureaKg), style = MaterialTheme.typography.bodySmall)
                                Text("• টিএসপি / ডিএপি: %.2f কেজি (জমি তৈরিতে)".format(tspKg), style = MaterialTheme.typography.bodySmall)
                                Text("• পটাশ (MOP): %.2f কেজি (অর্ধেক তৈরিতে, বাকি পরে)".format(mopKg), style = MaterialTheme.typography.bodySmall)
                                Text("• জিপসাম: %.2f কেজি (জমি তৈরিতে)".format(gypsumKg), style = MaterialTheme.typography.bodySmall)
                            }

                            HorizontalDivider()

                            Text(
                                text = "২. কীটনাশক স্প্রে ড্রাম মাপক:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF00897B)
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = tankLitersStr,
                                    onValueChange = { tankLitersStr = it },
                                    label = { Text("ট্যাংক (লিটার)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = dosePerLiterStr,
                                    onValueChange = { dosePerLiterStr = it },
                                    label = { Text("প্রতি লিটারে (মিলি/গ্রাম)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF00897B).copy(alpha = 0.1f))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "👉 এক ড্রামে মোট ওষুধ লাগবে: %.1f মিলি বা গ্রাম".format(totalSprayDose),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF00897B)
                                )
                            }
                        }
                    }
                    1 -> {
                        // Cattle Feed
                        val cWeight = cattleWeightStr.toDoubleOrNull() ?: 0.0
                        val milkL = dailyMilkStr.toDoubleOrNull() ?: 0.0

                        val greenGrassKg = (cWeight * 0.10).coerceAtLeast(0.0) // 10% of body weight
                        val dryStrawKg = (cWeight * 0.015).coerceAtLeast(0.0) // 1.5%
                        val baseConcentrateKg = (cWeight * 0.01).coerceAtLeast(0.0) // 1%
                        val milkConcentrateKg = (milkL / 2.5).coerceAtLeast(0.0) // 1 kg per 2.5 L
                        val totalConcentrateKg = baseConcentrateKg + milkConcentrateKg
                        val waterLiters = (cWeight * 0.12 + milkL * 2.0).coerceAtLeast(0.0)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = cattleWeightStr,
                                onValueChange = { cattleWeightStr = it },
                                label = { Text("গরুর আনুমানিক ওজন (কেজি)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = dailyMilkStr,
                                onValueChange = { dailyMilkStr = it },
                                label = { Text("দৈনিক দুধ উৎপাদন (লিটার) [না থাকলে ০]") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(themeColors.cardBg)
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("দৈনিক পুষ্টি চাহিদা:", fontWeight = FontWeight.Bold, color = themeColors.buttonEqualBg)
                                Text("• সুষম দানাদার খাদ্য: %.2f কেজি".format(totalConcentrateKg), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Text("• কাঁচা ঘাস (নেপিয়ার/ভুট্টা): %.1f কেজি".format(greenGrassKg), style = MaterialTheme.typography.bodySmall)
                                Text("• শুকনা খড় / UMS: %.1f কেজি".format(dryStrawKg), style = MaterialTheme.typography.bodySmall)
                                Text("• বিশুদ্ধ খাবার পানি: %.0f লিটার (কমপক্ষে)".format(waterLiters), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    2 -> {
                        // Fish Feed
                        val pDecimal = pondDecimalStr.toDoubleOrNull() ?: 0.0
                        val fishCount = totalFishCountStr.toDoubleOrNull() ?: 0.0
                        val avgWeightGram = avgFishWeightGramStr.toDoubleOrNull() ?: 0.0

                        val totalBiomassKg = (fishCount * avgWeightGram) / 1000.0
                        val dailyFeedKg = totalBiomassKg * 0.035 // 3.5%
                        val morningFeedKg = dailyFeedKg * 0.5
                        val afternoonFeedKg = dailyFeedKg * 0.5
                        val limeKg = (pDecimal * 1.5).coerceAtLeast(0.0)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = pondDecimalStr,
                                onValueChange = { pondDecimalStr = it },
                                label = { Text("পুকুরের আয়তন (শতক)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = totalFishCountStr,
                                    onValueChange = { totalFishCountStr = it },
                                    label = { Text("মাছের সংখ্যা") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = avgFishWeightGramStr,
                                    onValueChange = { avgFishWeightGramStr = it },
                                    label = { Text("গড় ওজন (গ্রাম)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(themeColors.cardBg)
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("মাছের হিসাব ও খাদ্যের মাত্রা:", fontWeight = FontWeight.Bold, color = Color(0xFF0288D1))
                                Text("• মোট মাছের আনুমানিক ওজন: %.1f কেজি".format(totalBiomassKg), style = MaterialTheme.typography.bodySmall)
                                Text("• দৈনিক মোট খাবার (৩.৫% হারে): %.2f কেজি".format(dailyFeedKg), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = themeColors.buttonEqualBg)
                                Text("• সকাল ৯টায়: %.2f কেজি | বিকেল ৩টায়: %.2f কেজি".format(morningFeedKg, afternoonFeedKg), style = MaterialTheme.typography.bodySmall)
                                Text("• পুকুর প্রস্তুতিতে চুন প্রয়োজন: %.1f কেজি".format(limeKg), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    )
}

// -------------------------------------------------------------
// HELPER COMPOSABLES
// -------------------------------------------------------------
@Composable
private fun InfoRowLabel(label: String, value: String, themeColors: CalculatorThemeColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = themeColors.displayText.copy(alpha = 0.7f),
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = themeColors.displayText,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun EmptyStateCard(message: String, themeColors: CalculatorThemeColors) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = themeColors.displayText.copy(alpha = 0.4f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = themeColors.displayText.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}
