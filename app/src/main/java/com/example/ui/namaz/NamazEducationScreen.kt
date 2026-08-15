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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    val expandedRuleIds by viewModel.expandedRuleIds.collectAsState()
    val playingDuaId by viewModel.playingDuaId.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    val primaryCyan = themeColors.buttonEqualBg
    val femaleAccentColor = Color(0xFFEC4899)

    val tabs = listOf(
        Triple(0, "অজু ও তাহারাত", Icons.Default.WaterDrop),
        Triple(1, "৫ ওয়াক্ত নামাজ", Icons.Default.Mosque),
        Triple(2, "বিশেষ নামাজ", Icons.Default.AutoAwesome),
        Triple(3, "নিয়ত ও দোয়া", Icons.Default.MenuBook),
        Triple(4, "চিত্রসহ শিক্ষা", Icons.Default.FormatListNumbered)
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
                            text = if (isFemaleMode) "নারী ভার্সন (নিয়ম ও দুআ)" else "পুরুষ ভার্সন (নিয়ম ও দুআ)",
                            fontSize = 11.sp,
                            color = if (isFemaleMode) femaleAccentColor else primaryCyan
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
                    // Male / Female Gender Toggle Pill Switch
                    Surface(
                        onClick = { viewModel.toggleGenderMode() },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isFemaleMode) femaleAccentColor.copy(alpha = 0.18f) else primaryCyan.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, if (isFemaleMode) femaleAccentColor else primaryCyan),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = if (isFemaleMode) Icons.Default.Female else Icons.Default.Male,
                                contentDescription = "Gender Mode",
                                tint = if (isFemaleMode) femaleAccentColor else primaryCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isFemaleMode) "নারী ♀" else "পুরুষ ♂",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFemaleMode) femaleAccentColor else primaryCyan
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
            // Category ScrollableTabRow
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = themeColors.cardBg,
                contentColor = primaryCyan,
                edgePadding = 12.dp,
                divider = { HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.1f)) }
            ) {
                tabs.forEach { (index, title, icon) ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { viewModel.setSelectedTab(index) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (selectedTab == index) (if (isFemaleMode) femaleAccentColor else primaryCyan) else themeColors.displayText.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title,
                                    fontSize = 13.5.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == index) (if (isFemaleMode) femaleAccentColor else primaryCyan) else themeColors.displayText.copy(alpha = 0.7f)
                                )
                            }
                        }
                    )
                }
            }

            // Tab Content with Animated Content Transition
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(150))
                },
                label = "NamazTabAnimation",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> WuduAndTaharatSection(themeColors, isFemaleMode)
                    1 -> DailyPrayersSection(themeColors, isFemaleMode, selectedWaqtId, viewModel)
                    2 -> SpecialPrayersSection(themeColors, isFemaleMode, expandedRuleIds, viewModel)
                    3 -> AllDuasAndNiyyatSection(themeColors, isFemaleMode, searchQuery, playingDuaId, isPlaying, viewModel)
                    4 -> VisualIllustratorSection(themeColors, isFemaleMode)
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
    isFemaleMode: Boolean
) {
    var activeSubCategory by remember { mutableStateOf("wudu_steps") }
    val primaryCyan = themeColors.buttonEqualBg

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Subcategory Chip Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "wudu_steps" to "অজুর পদক্ষেপ",
                    "wudu_farz_sunnah" to "ফরজ ও সুন্নত",
                    "tayammum" to "তায়াম্মুম",
                    "ghusl" to "গোসলের নিয়ম",
                    "breakers" to "অজু ভঙ্গের কারণ"
                ).forEach { (key, label) ->
                    val isSelected = activeSubCategory == key
                    Surface(
                        onClick = { activeSubCategory = key },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) primaryCyan else themeColors.cardBg,
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
                    Text(
                        text = "অজুর ধারাবাহিক সঠিক চিত্র ও নিয়মাবলি:",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                }
                items(NamazDataRepository.wuduSteps) { wuduItem ->
                    val prayerStep = PrayerStep(
                        stepNumber = wuduItem.stepNumber,
                        titleBn = wuduItem.titleBn,
                        titleEn = wuduItem.titleEn,
                        descriptionBn = wuduItem.descriptionBn,
                        postureType = PostureType.WUDU_GENERIC,
                        arabicText = wuduItem.arabicDua,
                        banglaPronunciation = wuduItem.banglaDuaPronunciation,
                        banglaMeaning = wuduItem.banglaDuaMeaning
                    )
                    PrayerStepCard(
                        step = prayerStep,
                        isFemaleMode = isFemaleMode,
                        themeColors = themeColors
                    )
                }
            }
            "wudu_farz_sunnah" -> {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = primaryCyan)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("অজুর ৪টি ফরজ (আবশ্যকীয়)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            NamazDataRepository.wuduFarzList.forEach { itemText ->
                                Text(itemText, fontSize = 13.5.sp, color = themeColors.displayText, modifier = Modifier.padding(vertical = 4.dp), lineHeight = 19.sp)
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
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = primaryCyan)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("অজুর ১৩টি সুন্নত", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            NamazDataRepository.wuduSunnahList.forEach { itemText ->
                                Text(itemText, fontSize = 13.5.sp, color = themeColors.displayText, modifier = Modifier.padding(vertical = 3.dp), lineHeight = 19.sp)
                            }
                        }
                    }
                }
            }
            "tayammum" -> {
                item {
                    Text("তায়াম্মুমের পদ্ধতি (পবিত্র মাটি দিয়ে অজুর বিকল্প):", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
                items(NamazDataRepository.tayammumSteps) { step ->
                    PrayerStepCard(step = step, isFemaleMode = isFemaleMode, themeColors = themeColors)
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
                            Text("গোসলের ৩টি ফরজ:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primaryCyan)
                            Spacer(modifier = Modifier.height(8.dp))
                            NamazDataRepository.ghuslFarzList.forEach { itemText ->
                                Text(itemText, fontSize = 13.5.sp, color = themeColors.displayText, modifier = Modifier.padding(vertical = 3.dp))
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = themeColors.displayText.copy(alpha = 0.1f))
                            Text("সুন্নতে মুয়াক্কাদাহ গোসলের নিয়ম:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                            Spacer(modifier = Modifier.height(8.dp))
                            NamazDataRepository.ghuslSunnahSteps.forEach { itemText ->
                                Text(itemText, fontSize = 13.5.sp, color = themeColors.displayText, modifier = Modifier.padding(vertical = 3.dp))
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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("অজু ভঙ্গের ৭টি প্রধান কারণ:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                            Spacer(modifier = Modifier.height(10.dp))
                            NamazDataRepository.wuduBreakersList.forEach { itemText ->
                                Text(itemText, fontSize = 13.5.sp, color = themeColors.displayText, modifier = Modifier.padding(vertical = 4.dp), lineHeight = 19.sp)
                            }
                        }
                    }
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
    selectedWaqtId: String,
    viewModel: NamazViewModel
) {
    val primaryCyan = themeColors.buttonEqualBg
    val selectedWaqt = NamazDataRepository.dailyWaqts.find { it.id == selectedWaqtId } ?: NamazDataRepository.dailyWaqts.first()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Waqt Selector Tabs (Fajr, Dhuhr, Asr, Maghrib, Isha, Witr)
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
                        color = if (isSelected) primaryCyan else themeColors.cardBg,
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
                                color = if (isSelected) Color.White.copy(alpha = 0.25f) else primaryCyan.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${waqt.totalRakat}র",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else primaryCyan,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Rakat Breakdown Table Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                modifier = Modifier.fillMaxWidth()
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
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                            Text(
                                text = selectedWaqt.arabicName,
                                fontSize = 14.sp,
                                color = primaryCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = primaryCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "মোট ${selectedWaqt.totalRakat} রাকাত",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryCyan,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

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
                        RakatPill("সুন্নত", "${selectedWaqt.sunnatMuakkadah + selectedWaqt.sunnatGairMuakkadah} রাকাত", primaryCyan, Modifier.weight(1f))
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

        // Step-by-Step Prayer Flow Cards
        item {
            Text(
                text = "${selectedWaqt.nameBn} নামাজের ধাপভিত্তিক নিয়ম:",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )
        }

        items(selectedWaqt.steps3Rakat ?: selectedWaqt.steps4Rakat ?: selectedWaqt.steps2Rakat) { step ->
            PrayerStepCard(
                step = step,
                isFemaleMode = isFemaleMode,
                themeColors = themeColors
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
    expandedRuleIds: Set<String>,
    viewModel: NamazViewModel
) {
    val primaryCyan = themeColors.buttonEqualBg

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "বিশেষ, ওয়াজিব ও নফল নামাজের পূর্ণাঙ্গ নিয়ম:",
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
                modifier = Modifier.fillMaxWidth()
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
                                    text = "নিয়ম: ${rule.rakatsCountBn}",
                                    fontSize = 12.sp,
                                    color = primaryCyan,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        IconButton(onClick = { viewModel.toggleRuleExpanded(rule.id) }) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Expand",
                                tint = primaryCyan
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
                                    color = primaryCyan.copy(alpha = 0.1f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "📌 খুতবার নির্দেশিকা: ${rule.khutbahNoteBn}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = primaryCyan,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            rule.steps.forEach { step ->
                                PrayerStepCard(
                                    step = step,
                                    isFemaleMode = isFemaleMode,
                                    themeColors = themeColors
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
// 4. TAB 3: ALL NIYYAT & DUAS (সকল নিয়ত ও দোয়া)
// ==========================================
@Composable
fun AllDuasAndNiyyatSection(
    themeColors: CalculatorThemeColors,
    isFemaleMode: Boolean,
    searchQuery: String,
    playingDuaId: String?,
    isPlaying: Boolean,
    viewModel: NamazViewModel
) {
    val primaryCyan = themeColors.buttonEqualBg
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val filteredList = NamazDataRepository.allDuasAndNiyyat.filter { dua ->
        val matchesCategory = (selectedCategoryFilter == "All" || dua.category == selectedCategoryFilter)
        val matchesQuery = searchQuery.isEmpty() ||
                dua.titleBn.contains(searchQuery, ignoreCase = true) ||
                dua.banglaPronunciation.contains(searchQuery, ignoreCase = true) ||
                dua.arabicText.contains(searchQuery)
        matchesCategory && matchesQuery
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("দোয়া বা নিয়ত সার্চ করুন...", fontSize = 13.5.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = primaryCyan) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryCyan,
                    unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                    focusedContainerColor = themeColors.cardBg,
                    unfocusedContainerColor = themeColors.cardBg
                )
            )
        }

        item {
            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "All" to "সব দোয়া ও নিয়ত",
                    "Niyyat" to "নামাজের নিয়ত",
                    "Prayer Dua" to "নামাজের দোয়া",
                    "Wudu Dua" to "অজুর দোয়া"
                ).forEach { (catKey, label) ->
                    val isSelected = selectedCategoryFilter == catKey
                    Surface(
                        onClick = { selectedCategoryFilter = catKey },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) primaryCyan else themeColors.cardBg,
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
                }
            )
        }
    }
}

// ==========================================
// 5. TAB 4: VISUAL ILLUSTRATOR (চিত্রসহ শিক্ষা)
// ==========================================
@Composable
fun VisualIllustratorSection(
    themeColors: CalculatorThemeColors,
    isFemaleMode: Boolean
) {
    val primaryCyan = themeColors.buttonEqualBg

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = primaryCyan.copy(alpha = 0.12f)
                ),
                border = BorderStroke(1.dp, primaryCyan.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = primaryCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "চিত্রসহ নামাজ আদায়ের অবস্থানসমূহ (Postures)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "নিচে কিয়াম, রুকু, কওমা, সিজদা ও সালামের সঠিক শারীরিক অবস্থান চিত্রিত করা হলো। উপরে ডানপাশের সুইচ দিয়ে পুরুষ ও নারীদের অবস্থানের পার্থক্য দেখতে পারেন।",
                        fontSize = 12.5.sp,
                        color = themeColors.displayText.copy(alpha = 0.85f),
                        lineHeight = 18.sp
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
