package com.example.ui.namaz

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.CalculatorThemeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamazEducationScreen(
    themeColors: CalculatorThemeColors,
    onBackClick: () -> Unit,
    viewModel: NamazViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isFemaleMode by viewModel.isFemaleMode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedWaqtId by viewModel.selectedWaqtId.collectAsState()
    val selectedRakatType by viewModel.selectedRakatType.collectAsState()
    val expandedRuleIds by viewModel.expandedRuleIds.collectAsState()
    val playingDuaId by viewModel.playingDuaId.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    val primaryCyan = themeColors.buttonEqualBg
    val femaleAccentColor = Color(0xFFEC4899)
    val activeAccent = if (isFemaleMode) femaleAccentColor else primaryCyan

    val tabs = listOf(
        Triple(0, "অজু ও তাহারাত", Icons.Default.WaterDrop),
        Triple(1, "৫ ওয়াক্ত নামাজ", Icons.Default.Mosque),
        Triple(2, "বিশেষ নামাজ", Icons.Default.AutoAwesome),
        Triple(3, "সূরা ও দোয়া", Icons.Default.MenuBook),
        Triple(4, "আহকাম ও সাহু", Icons.Default.Warning),
        Triple(5, "অঙ্গভঙ্গি ও নিয়ম", Icons.Default.FormatListNumbered)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "পূর্ণাঙ্গ নামাজ ও অজু শিক্ষা",
                            fontSize = 17.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        Text(
                            text = if (isFemaleMode) "নারী ভার্সন (পর্দা ও শরীয়তের বিশেষ নিয়ম)" else "পুরুষ ভার্সন (সহিহ পদ্ধতি ও মাসনূন দুআ)",
                            fontSize = 11.sp,
                            color = activeAccent
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = themeColors.displayText
                        )
                    }
                },
                actions = {
                    // Male / Female Toggle Button Pill
                    Surface(
                        onClick = { viewModel.toggleGenderMode() },
                        shape = RoundedCornerShape(20.dp),
                        color = activeAccent.copy(alpha = 0.18f),
                        border = BorderStroke(1.2.dp, activeAccent),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isFemaleMode) Icons.Default.Female else Icons.Default.Male,
                                contentDescription = "Gender Mode",
                                tint = activeAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isFemaleMode) "নারী ♀" else "পুরুষ ♂",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = activeAccent
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = themeColors.cardBg)
            )
        },
        containerColor = themeColors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Scrollable Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = themeColors.cardBg,
                contentColor = activeAccent,
                edgePadding = 12.dp,
                divider = { HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.1f)) }
            ) {
                tabs.forEach { (index, title, icon) ->
                    val isSelected = selectedTab == index
                    Tab(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedTab(index) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) activeAccent else themeColors.displayText.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title,
                                    fontSize = 13.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) activeAccent else themeColors.displayText.copy(alpha = 0.75f)
                                )
                            }
                        }
                    )
                }
            }

            // Animated Tab Body
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(150))
                },
                label = "NamazTabContentTransition",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> WuduAndTaharatSection(themeColors, isFemaleMode, activeAccent, viewModel)
                    1 -> DailyPrayersSection(themeColors, isFemaleMode, activeAccent, selectedWaqtId, selectedRakatType, viewModel)
                    2 -> SpecialPrayersSection(themeColors, isFemaleMode, activeAccent, expandedRuleIds, viewModel)
                    3 -> AllDuasAndSurahsSection(themeColors, isFemaleMode, activeAccent, searchQuery, playingDuaId, isPlaying, viewModel)
                    4 -> FiqhAndSahwSection(themeColors, isFemaleMode, activeAccent, expandedRuleIds, viewModel)
                    5 -> VisualIllustratorSection(themeColors, isFemaleMode, activeAccent, viewModel)
                }
            }
        }
    }
}

// ==========================================
// 1. TAB 0: WUDU & TAHARAT (অজু ও তাহারাত)
// ==========================================
@Composable
fun WuduAndTaharatSection(
    themeColors: CalculatorThemeColors,
    isFemaleMode: Boolean,
    accentColor: Color,
    viewModel: NamazViewModel
) {
    var activeSubCategory by remember { mutableStateOf("wudu_steps") }
    val playingDuaId by viewModel.playingDuaId.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val downloadedDuaIds by viewModel.downloadedDuaIds.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Subcategory Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "wudu_steps" to "অজুর দোয়া ও ধাপসমূহ",
                    "wudu_farz_sunnah" to "ফরজ, সুন্নত ও মুস্তাহাব",
                    "breakers" to "অজু ভঙ্গের কারণ",
                    "ghusl" to "গোসলের ফরজ ও নিয়ম",
                    "tayammum" to "তায়াম্মুমের পদ্ধতি"
                ).forEach { (key, label) ->
                    val isSelected = activeSubCategory == key
                    Surface(
                        onClick = { activeSubCategory = key },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) accentColor else themeColors.cardBg,
                        border = if (!isSelected) BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.15f)) else null
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else themeColors.displayText,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        when (activeSubCategory) {
            "wudu_steps" -> {
                item {
                    // Wudu Guide Header Text Card
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "সহিহ সুন্নাত অনুযায়ী অজুর নিয়ম ও পদ্ধতি",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "পবিত্রতা অর্জনের জন্য অজুর ৪টি ফরজ ও ১৩টি সুন্নাত অনুযায়ী প্রতিটি অঙ্গ ধারাবাহিকভাবে ধৌত করা আবশ্যক। নিচে অজুর মাসনূন দোয়া ও স্পষ্ট ধাপসমূহ বিস্তৃত বর্ণনা ও অডিওসহ দেওয়া হলো:",
                                fontSize = 12.5.sp,
                                color = themeColors.displayText.copy(alpha = 0.85f),
                                lineHeight = 18.5.sp
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "অজুর শুরুতে ও শেষে পড়ার মাসনূন দোয়া:",
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                }
                items(NamazDataRepository.wuduDuas) { wuduItem ->
                    val duaId = "wudu_dua_${wuduItem.stepNumber}"
                    val progress = downloadProgress[duaId] ?: if (downloadedDuaIds.contains(duaId)) 100 else null
                    PrayerStepCard(
                        step = wuduItem,
                        isFemaleMode = isFemaleMode,
                        themeColors = themeColors,
                        isPlaying = playingDuaId == duaId && isPlaying,
                        onAudioClick = {
                            if (!wuduItem.arabicText.isNullOrEmpty()) {
                                viewModel.playOrPauseDuaAudio(
                                    duaId,
                                    wuduItem.audioUrl,
                                    wuduItem.arabicText,
                                    wuduItem.banglaPronunciation ?: ""
                                )
                            }
                        },
                        downloadProgress = progress,
                        onDownloadClick = {
                            if (!wuduItem.arabicText.isNullOrEmpty()) {
                                viewModel.downloadDuaAudio(duaId, wuduItem.arabicText)
                            }
                        }
                    )
                }
            }
            "wudu_farz_sunnah" -> {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = accentColor)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("অজুর ৪টি ফরজ (আবশ্যকীয় অঙ্গ ধৌত)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = accentColor)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            NamazDataRepository.wuduFarzList.forEach { itemText ->
                                Text(itemText, fontSize = 13.5.sp, color = themeColors.displayText, modifier = Modifier.padding(vertical = 3.dp), lineHeight = 19.sp)
                            }
                        }
                    }
                }
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = accentColor)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("অজুর ১০টি গুরুত্বপূর্ণ সুন্নাত", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            NamazDataRepository.wuduSunnahSteps.forEach { itemText ->
                                Text(itemText, fontSize = 13.5.sp, color = themeColors.displayText, modifier = Modifier.padding(vertical = 3.dp), lineHeight = 19.sp)
                            }
                        }
                    }
                }
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("অজুর মুস্তাহাব ও আদবসমূহ:", fontSize = 15.5.sp, fontWeight = FontWeight.Bold, color = accentColor)
                            Spacer(modifier = Modifier.height(8.dp))
                            NamazDataRepository.wuduMustahabList.forEach { itemText ->
                                Text(itemText, fontSize = 13.sp, color = themeColors.displayText.copy(alpha = 0.9f), modifier = Modifier.padding(vertical = 2.5.dp))
                            }
                        }
                    }
                }
            }
            "breakers" -> {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("অজু ভঙ্গের ৭টি কারণ:", fontSize = 16.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            NamazDataRepository.wuduBreakersList.forEach { itemText ->
                                Text(itemText, fontSize = 13.5.sp, color = themeColors.displayText, modifier = Modifier.padding(vertical = 4.dp), lineHeight = 19.sp)
                            }
                        }
                    }
                }
            }
            "ghusl" -> {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("গোসলের ৩টি ফরজ (বাধ্যতামূলক):", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = accentColor)
                            Spacer(modifier = Modifier.height(8.dp))
                            NamazDataRepository.ghuslFarzList.forEach { itemText ->
                                Text(itemText, fontSize = 13.5.sp, color = themeColors.displayText, modifier = Modifier.padding(vertical = 3.dp), lineHeight = 19.sp)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = themeColors.displayText.copy(alpha = 0.1f))
                            Text("সুন্নাত পদ্ধতিতে গোসলের ধারাবাহিক নিয়ম:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                            Spacer(modifier = Modifier.height(8.dp))
                            NamazDataRepository.ghuslSunnahSteps.forEach { itemText ->
                                Text(itemText, fontSize = 13.5.sp, color = themeColors.displayText, modifier = Modifier.padding(vertical = 3.dp), lineHeight = 19.sp)
                            }
                        }
                    }
                }
            }
            "tayammum" -> {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("তায়াম্মুমের ৩টি ফরজ:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = accentColor)
                            Spacer(modifier = Modifier.height(8.dp))
                            NamazDataRepository.tayammumFarzList.forEach { itemText ->
                                Text(itemText, fontSize = 13.5.sp, color = themeColors.displayText, modifier = Modifier.padding(vertical = 3.dp))
                            }
                        }
                    }
                }
                item {
                    Text("তায়াম্মুম আদায়ের ধারাবাহিক পদ্ধতি:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
                items(NamazDataRepository.tayammumSteps) { step ->
                    val duaId = "tayammum_${step.stepNumber}"
                    val progress = downloadProgress[duaId] ?: if (downloadedDuaIds.contains(duaId)) 100 else null
                    PrayerStepCard(
                        step = step,
                        isFemaleMode = isFemaleMode,
                        themeColors = themeColors,
                        isPlaying = playingDuaId == duaId && isPlaying,
                        onAudioClick = {
                            if (!step.arabicText.isNullOrEmpty()) {
                                viewModel.playOrPauseDuaAudio(
                                    duaId,
                                    step.audioUrl,
                                    step.arabicText,
                                    step.banglaPronunciation ?: ""
                                )
                            }
                        },
                        downloadProgress = progress,
                        onDownloadClick = {
                            if (!step.arabicText.isNullOrEmpty()) {
                                viewModel.downloadDuaAudio(duaId, step.arabicText)
                            }
                        }
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. TAB 1: DAILY PRAYERS (পাঁচ ওয়াক্ত নামাজ)
// ==========================================
@Composable
fun DailyPrayersSection(
    themeColors: CalculatorThemeColors,
    isFemaleMode: Boolean,
    accentColor: Color,
    selectedWaqtId: String,
    selectedRakatType: String,
    viewModel: NamazViewModel
) {
    val selectedWaqt = NamazDataRepository.dailyWaqts.find { it.id == selectedWaqtId } ?: NamazDataRepository.dailyWaqts.first()
    val playingDuaId by viewModel.playingDuaId.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val downloadedDuaIds by viewModel.downloadedDuaIds.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Waqt Selection Tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NamazDataRepository.dailyWaqts.forEach { waqt ->
                    val isSelected = waqt.id == selectedWaqtId
                    Surface(
                        onClick = { viewModel.setSelectedWaqtId(waqt.id) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) accentColor else themeColors.cardBg,
                        border = if (!isSelected) BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.15f)) else null
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                        ) {
                            Text(
                                text = waqt.nameBn,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else themeColors.displayText
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) Color.White.copy(alpha = 0.25f) else accentColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${waqt.totalRakat}র",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else accentColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Waqt Overview & Rakat Breakdown Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${selectedWaqt.nameBn} নামাজের রাকাত বিন্যাস",
                                fontSize = 17.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                            Text(
                                text = selectedWaqt.arabicName,
                                fontSize = 14.sp,
                                color = accentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = accentColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "মোট ${selectedWaqt.totalRakat} রাকাত",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⏰ সময়সীমা: ${selectedWaqt.timeDescriptionBn}",
                        fontSize = 12.5.sp,
                        color = themeColors.displayText.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = selectedWaqt.descriptionBn,
                        fontSize = 12.5.sp,
                        color = themeColors.displayText.copy(alpha = 0.8f),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Rakat Breakdown Table Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RakatPill("ফরজ", "${selectedWaqt.farz} রাকাত", Color(0xFF10B981), Modifier.weight(1f))
                        RakatPill("সুন্নত", "${selectedWaqt.sunnatMuakkadah + selectedWaqt.sunnatGairMuakkadah} রাকাত", accentColor, Modifier.weight(1f))
                        if (selectedWaqt.witr > 0) {
                            RakatPill("বিতর", "${selectedWaqt.witr} রাকাত", Color(0xFFF59E0B), Modifier.weight(1f))
                        }
                        if (selectedWaqt.nafl > 0) {
                            RakatPill("নফল", "${selectedWaqt.nafl} রাকাত", Color(0xFF8B5CF6), Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Sub-Rakat Filter Selector (Farz vs Sunnah vs Witr)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = { viewModel.setSelectedRakatType("farz") },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedRakatType == "farz") Color(0xFF10B981) else themeColors.cardBg,
                    border = BorderStroke(1.dp, if (selectedRakatType == "farz") Color(0xFF10B981) else themeColors.displayText.copy(alpha = 0.15f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "ফরজ নামাজ (${selectedWaqt.farz}র)",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedRakatType == "farz") Color.White else themeColors.displayText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (selectedWaqt.steps2Rakat != null && (selectedWaqt.sunnatMuakkadah > 0 || selectedWaqt.sunnatGairMuakkadah > 0)) {
                    Surface(
                        onClick = { viewModel.setSelectedRakatType("sunnat") },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedRakatType == "sunnat") accentColor else themeColors.cardBg,
                        border = BorderStroke(1.dp, if (selectedRakatType == "sunnat") accentColor else themeColors.displayText.copy(alpha = 0.15f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "সুন্নত নামাজ (২র)",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedRakatType == "sunnat") Color.White else themeColors.displayText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                if (selectedWaqt.stepsWitr != null) {
                    Surface(
                        onClick = { viewModel.setSelectedRakatType("witr") },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedRakatType == "witr") Color(0xFFF59E0B) else themeColors.cardBg,
                        border = BorderStroke(1.dp, if (selectedRakatType == "witr") Color(0xFFF59E0B) else themeColors.displayText.copy(alpha = 0.15f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "বিতর ওয়াজিব (৩র)",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedRakatType == "witr") Color.White else themeColors.displayText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Display Corresponding Steps
        val activeSteps = when (selectedRakatType) {
            "sunnat" -> selectedWaqt.steps2Rakat ?: emptyList()
            "witr" -> selectedWaqt.stepsWitr ?: emptyList()
            else -> selectedWaqt.steps4Rakat ?: selectedWaqt.steps3Rakat ?: selectedWaqt.steps2Rakat ?: emptyList()
        }

        item {
            Text(
                text = "${selectedWaqt.nameBn} নামাজের ধাপভিত্তিক সহিহ আমল:",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )
        }

        items(activeSteps) { step ->
            val duaId = "waqt_${selectedWaqt.id}_${selectedRakatType}_${step.stepNumber}"
            val progress = downloadProgress[duaId] ?: if (downloadedDuaIds.contains(duaId)) 100 else null
            PrayerStepCard(
                step = step,
                isFemaleMode = isFemaleMode,
                themeColors = themeColors,
                isPlaying = playingDuaId == duaId && isPlaying,
                onAudioClick = {
                    if (!step.arabicText.isNullOrEmpty()) {
                        viewModel.playOrPauseDuaAudio(
                            duaId,
                            step.audioUrl,
                            step.arabicText,
                            step.banglaPronunciation ?: ""
                        )
                    }
                },
                downloadProgress = progress,
                onDownloadClick = {
                    if (!step.arabicText.isNullOrEmpty()) {
                        viewModel.downloadDuaAudio(duaId, step.arabicText)
                    }
                }
            )
        }
    }
}

@Composable
fun RakatPill(label: String, count: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            Text(label, fontSize = 11.5.sp, color = color, fontWeight = FontWeight.Bold)
            Text(count, fontSize = 13.sp, color = color, fontWeight = FontWeight.ExtraBold)
        }
    }
}

// ==========================================
// 3. TAB 2: SPECIAL PRAYERS (বিশেষ ও নফল নামাজ)
// ==========================================
@Composable
fun SpecialPrayersSection(
    themeColors: CalculatorThemeColors,
    isFemaleMode: Boolean,
    accentColor: Color,
    expandedRuleIds: Set<String>,
    viewModel: NamazViewModel
) {
    val playingDuaId by viewModel.playingDuaId.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val downloadedDuaIds by viewModel.downloadedDuaIds.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "বিশেষ, ওয়াজিব ও গুরুত্বপূর্ণ নফল নামাজের পূর্ণাঙ্গ নিয়ম:",
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )
        }

        items(NamazDataRepository.specialPrayers) { rule ->
            val isExpanded = expandedRuleIds.contains(rule.id)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, if (isExpanded) accentColor.copy(alpha = 0.4f) else themeColors.displayText.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleRuleExpanded(rule.id) },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = rule.titleBn,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                            if (rule.rakatsCountBn.isNotEmpty()) {
                                Text(
                                    text = "রাকাত ও নিয়ম: ${rule.rakatsCountBn}",
                                    fontSize = 12.sp,
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        IconButton(onClick = { viewModel.toggleRuleExpanded(rule.id) }) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Expand",
                                tint = accentColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = rule.introductionBn,
                        fontSize = 13.sp,
                        color = themeColors.displayText.copy(alpha = 0.85f),
                        lineHeight = 19.sp
                    )

                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.1f))

                            if (rule.khutbahNoteBn != null) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = accentColor.copy(alpha = 0.1f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "📌 খুতবার নির্দেশিকা: ${rule.khutbahNoteBn}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = accentColor,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            if (rule.extraNotesBn != null) {
                                Text(
                                    text = "💡 গুরুত্বপূর্ণ নোট: ${rule.extraNotesBn}",
                                    fontSize = 12.sp,
                                    color = themeColors.displayText.copy(alpha = 0.75f)
                                )
                            }

                            rule.steps.forEach { step ->
                                val duaId = "special_${rule.id}_${step.stepNumber}"
                                val progress = downloadProgress[duaId] ?: if (downloadedDuaIds.contains(duaId)) 100 else null
                                PrayerStepCard(
                                    step = step,
                                    isFemaleMode = isFemaleMode,
                                    themeColors = themeColors,
                                    isPlaying = playingDuaId == duaId && isPlaying,
                                    onAudioClick = {
                                        if (!step.arabicText.isNullOrEmpty()) {
                                            viewModel.playOrPauseDuaAudio(
                                                duaId,
                                                step.audioUrl,
                                                step.arabicText,
                                                step.banglaPronunciation ?: ""
                                            )
                                        }
                                    },
                                    downloadProgress = progress,
                                    onDownloadClick = {
                                        if (!step.arabicText.isNullOrEmpty()) {
                                            viewModel.downloadDuaAudio(duaId, step.arabicText)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. TAB 3: ESSENTIAL SURAHS & DUAS (সূরা ও দোয়া)
// ==========================================
@Composable
fun AllDuasAndSurahsSection(
    themeColors: CalculatorThemeColors,
    isFemaleMode: Boolean,
    accentColor: Color,
    searchQuery: String,
    playingDuaId: String?,
    isPlaying: Boolean,
    viewModel: NamazViewModel
) {
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val downloadedDuaIds by viewModel.downloadedDuaIds.collectAsState()

    val filteredList = NamazDataRepository.allDuasAndNiyyat.filter { item ->
        val matchesCategory = (selectedCategoryFilter == "All" || item.category == selectedCategoryFilter)
        val matchesQuery = searchQuery.isEmpty() ||
                item.titleBn.contains(searchQuery, ignoreCase = true) ||
                item.banglaPronunciation.contains(searchQuery, ignoreCase = true) ||
                item.arabicText.contains(searchQuery) ||
                item.banglaMeaning.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("দোয়া, সূরা বা উচ্চারণ দিয়ে খুঁজুন...", fontSize = 13.5.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = accentColor) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                    focusedContainerColor = themeColors.cardBg,
                    unfocusedContainerColor = themeColors.cardBg
                )
            )
        }

        // Filter Categories
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "All" to "সকল সূরা ও দোয়া",
                    "Surah" to "প্রয়োজনীয় ছোট সূরা",
                    "Prayer Dua" to "নামাজের মূল দোয়া",
                    "Post Prayer" to "নামাজ পরবর্তী তাসবীহাত"
                ).forEach { (catKey, label) ->
                    val isSelected = selectedCategoryFilter == catKey
                    Surface(
                        onClick = { selectedCategoryFilter = catKey },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) accentColor else themeColors.cardBg,
                        border = if (!isSelected) BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.15f)) else null
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else themeColors.displayText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        items(filteredList) { dua ->
            val isCurrentPlaying = (playingDuaId == dua.id && isPlaying)
            val progress = downloadProgress[dua.id] ?: if (downloadedDuaIds.contains(dua.id)) 100 else null
            val prayerStep = PrayerStep(
                stepNumber = 0,
                titleBn = dua.titleBn,
                titleEn = dua.titleEn,
                descriptionBn = dua.contextOrVirtueBn ?: "",
                postureType = PostureType.DUA_GENERIC,
                arabicText = dua.arabicText,
                banglaPronunciation = dua.banglaPronunciation,
                banglaMeaning = dua.banglaMeaning
            )

            PrayerStepCard(
                step = prayerStep,
                isFemaleMode = isFemaleMode,
                themeColors = themeColors,
                isPlaying = isCurrentPlaying,
                onAudioClick = {
                    viewModel.playOrPauseDuaAudio(dua.id, dua.audioUrl, dua.arabicText, dua.banglaPronunciation)
                },
                downloadProgress = progress,
                onDownloadClick = {
                    viewModel.downloadDuaAudio(dua.id, dua.arabicText)
                }
            )
        }
    }
}

// ==========================================
// 5. TAB 4: FIQH, AHKAM, ARKAN & SAHW (আহকাম ও সাহু)
// ==========================================
@Composable
fun FiqhAndSahwSection(
    themeColors: CalculatorThemeColors,
    isFemaleMode: Boolean,
    accentColor: Color,
    expandedRuleIds: Set<String>,
    viewModel: NamazViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = accentColor.copy(alpha = 0.12f)
                ),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = accentColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "নামাজের বিশুদ্ধতার জন্য জরুরি মাসআলা-মাসায়েল",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "নামাজ শুদ্ধ হওয়ার জন্য ১৪টি ফরজ, ১৪টি ওয়াজিব, সাহু সিজদার বিধান এবং নামাজ ভঙ্গের কারণগুলো সঠিকভাবে জানা প্রত্যেক মুসলিমের ওপর আবশ্যক।",
                        fontSize = 12.5.sp,
                        color = themeColors.displayText.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        items(NamazDataRepository.fiqhRulesList) { fiqhItem ->
            val isExpanded = expandedRuleIds.contains(fiqhItem.id)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, if (isExpanded) accentColor.copy(alpha = 0.4f) else themeColors.displayText.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleRuleExpanded(fiqhItem.id) },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = fiqhItem.titleBn,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                            Text(
                                text = fiqhItem.subtitleBn,
                                fontSize = 12.sp,
                                color = accentColor,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        IconButton(onClick = { viewModel.toggleRuleExpanded(fiqhItem.id) }) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Expand",
                                tint = accentColor
                            )
                        }
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 6.dp))

                            fiqhItem.items.forEach { point ->
                                Text(
                                    text = point,
                                    fontSize = 13.sp,
                                    color = themeColors.displayText.copy(alpha = 0.9f),
                                    lineHeight = 19.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }

                            if (fiqhItem.explanationBn != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = accentColor.copy(alpha = 0.1f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "📌 বিশেষ নির্দেশ: ${fiqhItem.explanationBn}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = accentColor,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. TAB 5: PHYSICAL POSITIONS & RULES (অঙ্গভঙ্গি ও নিয়ম)
// ==========================================
@Composable
fun VisualIllustratorSection(
    themeColors: CalculatorThemeColors,
    isFemaleMode: Boolean,
    accentColor: Color,
    viewModel: NamazViewModel
) {
    val currentGenderLabel = if (isFemaleMode) "নারীদের নামাজ আদায়ের শরীয়াহ সম্মত বিশুদ্ধ গাইড" else "পুরুষদের সুন্নাত তরীকায় নামাজ আদায়ের বিশুদ্ধ গাইড"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isFemaleMode) Icons.Default.Female else Icons.Default.Male,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentGenderLabel,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isFemaleMode)
                            "নারীদের নামাজের ক্ষেত্রে সতোর পূর্ণ আবৃত রাখা, তাকবীরে তাহরীমায় কাঁধ পর্যন্ত হাত তোলা, কিয়ামে বুকে হাত বাঁধা, রুকুতে স্বল্প অবনত হওয়া এবং সিজদায় উরু পেটের সাথে মিলিয়ে মাটিতে সংকুচিত হয়ে বসার মাসআলাসমূহ অত্যন্ত নিখুঁত ও সহজভাবে নিচে বর্ণনা করা হয়েছে।"
                        else
                            "পুরুষদের জন্য সুন্নাত নিয়মে তাকবীরে তাহরীমায় কানের লতি পর্যন্ত হাত উঠানো, নাভির নিচে হাত বাঁধা, রুকুতে পিঠ সোজা রাখা এবং সিজদায় বাহু ফাঁক রেখে ক্বিয়াম থেকে সালাম পর্যন্ত প্রতিটির নিখুঁত বিবরণ নিচে বিস্তারিত দেওয়া হলো।",
                        fontSize = 12.5.sp,
                        color = themeColors.displayText.copy(alpha = 0.85f),
                        lineHeight = 18.5.sp
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = accentColor.copy(alpha = 0.12f)
                ),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = accentColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ধাপভিত্তিক সঠিক অঙ্গভঙ্গি ও মাসআলা",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "নিচে নামাজের প্রতিটি ধাপের সঠিক অঙ্গভঙ্গি, শারীরিক অবস্থান, তাসবীহ ও দোয়া বিস্তৃতভাবে দেওয়া হলো:",
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.85f)
                    )
                }
            }
        }

        items(NamazDataRepository.visualPositions) { positionStep ->
            PrayerStepCard(
                step = positionStep,
                isFemaleMode = isFemaleMode,
                themeColors = themeColors
            )
        }
    }
}
