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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.clickable

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.AppLanguage
import com.example.data.model.ToolCategory
import com.example.data.model.ToolType
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.LanguageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialToolsScreen(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val selectedType = viewModel.selectedToolType

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
            ToolsCategoriesView(viewModel, themeColors)
        } else {
            // View 2: Detailed Tool View
            ToolDetailView(currentType, viewModel, themeColors)
        }
    }
}

@Composable
fun ToolsCategoriesView(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val scrollState = rememberScrollState()
    val filterScrollState = rememberScrollState()
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

    val allTools = ToolType.values()
    val searchQuery = viewModel.toolSearchQuery.lowercase().trim()
    val selectedFilter = viewModel.selectedToolCategoryFilter

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
            .offset { IntOffset(0, bounceAnimatable.value.roundToInt()) }
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Search Bar
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

        // Category Filter Chips
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
                onClick = { viewModel.selectedToolCategoryFilter = null }
            )

            ToolCategory.values().forEach { cat ->
                ToolFilterChipItem(
                    label = cat.getTitle(viewModel.selectedLanguage),
                    isSelected = selectedFilter == cat,
                    icon = cat.icon,
                    themeColors = themeColors,
                    onClick = {
                        viewModel.selectedToolCategoryFilter = if (selectedFilter == cat) null else cat
                    }
                )
            }
        }

        // Tools List Grouped by Category
        if (filteredTools.isEmpty()) {
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
            val categoriesToShow = ToolCategory.values().filter { cat ->
                filteredTools.any { it.category == cat }
            }

            categoriesToShow.forEach { category ->
                val categoryTools = filteredTools.filter { it.category == category }

                if (categoryTools.isNotEmpty()) {
                    // Category Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(themeColors.buttonEqualBg.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = category.titleEn,
                                tint = themeColors.buttonEqualBg,
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
                    }

                    // 2-column Grid of Cards
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        categoryTools.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowItems.forEach { tool ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        ToolGridCardItem(
                                            toolType = tool,
                                            viewModel = viewModel,
                                            themeColors = themeColors,
                                            onClick = { viewModel.openTool(tool) }
                                        )
                                    }
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToolFilterChipItem(
    label: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    themeColors: CalculatorThemeColors,
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
        }
    }
}

@Composable
fun ToolGridCardItem(
    toolType: ToolType,
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    ElevatedCard(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tool_card_${toolType.name.lowercase()}")
            .scaleOnPress(interactionSource),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = themeColors.cardBg
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
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
                        .clip(RoundedCornerShape(10.dp))
                        .background(themeColors.buttonEqualBg.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = toolType.icon,
                        contentDescription = toolType.getTitle(viewModel.selectedLanguage),
                        tint = themeColors.buttonEqualBg,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open",
                    tint = themeColors.displayText.copy(alpha = 0.3f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = toolType.getTitle(viewModel.selectedLanguage),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = toolType.getDescription(viewModel.selectedLanguage),
                fontSize = 11.sp,
                color = themeColors.displayText.copy(alpha = 0.6f),
                maxLines = 2,
                lineHeight = 14.sp
            )
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .nestedScroll(nestedScrollConnection)
            .offset { IntOffset(0, bounceAnimatable.value.roundToInt()) }
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Back Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
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

                if (toolType != ToolType.CLOTH_MEASUREMENT && toolType != ToolType.GOLD_CALCULATOR) {
                    InfoToggleButton(
                        isExpanded = showToolInfo,
                        onToggle = { showToolInfo = !showToolInfo },
                        themeColors = themeColors
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showToolInfo && toolType != ToolType.CLOTH_MEASUREMENT && toolType != ToolType.GOLD_CALCULATOR,
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
        }
    }
}

private fun getToolInfoItems(toolType: ToolType, isBn: Boolean): List<Pair<String, String>> {
    return when (toolType) {
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
        ToolType.BMI, ToolType.BMR, ToolType.IDEAL_WEIGHT -> if (isBn) {
            listOf(
                "১. বিএমআই (BMI) কি ও স্বাস্থ্য ঝুঁকি?" to "বডি মাস ইনডেক্স বা বিএমআই হলো আপনার উচ্চতা এবং ওজনের অনুপাত, যা দিয়ে আপনার শরীর অতিরিক্ত ওজন, স্বাভাবিক নাকি কম ওজন তা নির্ণয় করা হয়।\n• ১৮.৫ এর নিচে: কম ওজন (পুষ্টিহীনতা ও রোগ প্রতিরোধ ক্ষমতা কম)\n• ১৮.৫ - ২৪.৯: স্বাভাবিক ওজন (আদর্শ ও স্বাস্থ্যকর)\n• ২৫ - ২৯.৯: অতিরিক্ত ওজন (হৃদরোগ ও ডায়াবেটিসের ঝুঁকি বৃদ্ধি)\n• ৩০ বা বেশি: স্থূলতা (উচ্চ রক্তচাপ, কোলেস্টেরল ও হৃদরোগের তীব্র ঝুঁকি)",
                "২. বিএমআর (BMR) এবং ক্যালোরি কি?" to "বিএমআর (Basal Metabolic Rate) হলো আপনি যখন সম্পূর্ণ বিশ্রামে থাকেন তখন শরীরকে সচল রাখতে যে পরিমাণ ন্যূনতম ক্যালোরি প্রয়োজন। আর প্রতিদিনের কাজের ওপর ভিত্তি করে মোট কত ক্যালোরি প্রয়োজন তা টিডিইই (TDEE - Total Daily Energy Expenditure) দিয়ে বের করা হয়।",
                "৩. ওজন নিয়ন্ত্রণ ও ক্যালোরির হিসাব" to "• ওজন কমাতে: আপনার প্রতিদিনের টিডিইই (TDEE) থেকে ৩০০-৫০০ ক্যালোরি কম গ্রহণ করুন (Caloric Deficit)।\n• ওজন বাড়াতে: আপনার দৈনিক টিডিইই এর থেকে ৩০০-৫০০ ক্যালোরি বেশি গ্রহণ করুন (Caloric Surplus)।\n• ওজন বজায় রাখতে: ঠিক যতটুকু ক্যালোরি ক্ষয় হয় ততটুকু সমপরিমাণ ক্যালোরির খাবার খান।"
            )
        } else {
            listOf(
                "1. BMI Ranges & Health Risks" to "Body Mass Index (BMI) evaluates health categories based on height & weight:\n• Underweight (< 18.5): Risk of nutrient deficiency and weak immunity.\n• Normal (18.5 – 24.9): Optimal healthy category.\n• Overweight (25.0 – 29.9): Elevated risk of heart issues and type-2 diabetes.\n• Obese (30.0 or Above): High risk of chronic cardiovascular and metabolic illnesses.",
                "2. What is BMR and Daily Calories?" to "Basal Metabolic Rate (BMR) is the number of calories your body needs to perform basic life-sustaining functions at rest. Daily Calorie Need (TDEE) factors in physical activity level.",
                "3. Diet Planning & Caloric Goals" to "• To Lose Weight: Consume 300–500 kcal less than your daily TDEE (Caloric Deficit).\n• To Gain Weight: Consume 300–500 kcal more than your daily TDEE (Caloric Surplus).\n• To Maintain Weight: Consume calories equal to your TDEE."
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
        ToolType.EMI_LOAN, ToolType.INTEREST, ToolType.SAVINGS_TARGET -> if (isBn) {
            listOf(
                "১. ইএমআই (EMI) কিভাবে কাজ করে?" to "EMI (Equated Monthly Installment) হলো সমপরিমাণ মাসিক কিস্তি যা প্রতি মাসে ঋণ পরিশোধে দিতে হয়। এটি মূল টাকা এবং সুদের সমন্বয়ে গঠিত।",
                "২. মাসিক কিস্তি হিসাবের ফর্মুলা" to "ফর্মুলা: `EMI = [P x R x (1+R)^N]/[(1+R)^N - 1]`, যেখানে P = ঋণের পরিমাণ, R = মাসিক সুদের হার, N = কিস্তির সংখ্যা (মাস)।",
                "৩. ঋণের খরচ কমানোর উপায়" to "মেয়াদ কম রাখলে বা সুদের হার কম পেলে মোট অতিরিক্ত সুদের খরচ অনেক হ্রাস পায়। সম্ভব হলে ঋণের কিছু অংশ অগ্রিম পরিশোধ করুন।"
            )
        } else {
            listOf(
                "1. How does EMI work?" to "EMI stands for Equated Monthly Installment. It is a fixed payment made by a borrower to a lender at a specified date each calendar month. EMIs consist of both principal and interest components.",
                "2. Monthly EMI Formula" to "Formula: `EMI = [P x R x (1+R)^N]/[(1+R)^N - 1]` where P is Principal loan amount, R is monthly interest rate, and N is monthly tenure.",
                "3. Tips to Reduce Interest Cost" to "Choosing a shorter loan tenure or securing a lower interest rate significantly reduces the total interest payable. Making prepayments also helps lower the burden."
            )
        }
        ToolType.ELECTRICITY_BILL, ToolType.APPLIANCE_COST, ToolType.BATTERY_BACKUP -> if (isBn) {
            listOf(
                "১. বিদ্যুৎ বিল কিভাবে হিসাব করা হয়?" to "বিদ্যুৎ বিল ব্যবহারের পরিমাণ (ইউনিট বা কিলোওয়াট-ঘণ্টা) অনুযায়ী করা হয়। ১ ইউনিট = ১০০০ ওয়াট ক্ষমতার কোনো যন্ত্রপাতি ১ ঘণ্টা চললে যে শক্তি ব্যয় হয়।",
                "২. ব্যাটারি ব্যাকআপ কিভাবে হিসাব করে?" to "ব্যাকআপ সময় = (ব্যাটারির আহ * ব্যাটারির ভোল্টেজ * দক্ষতা) / লোড (ওয়াট)। যেমন: ১৫০Ah ১২V ব্যাটারি দিয়ে ৩০০ ওয়াট লোডে প্রায় ৪-৫ ঘণ্টা ব্যাকআপ পাওয়া যায়।"
            )
        } else {
            listOf(
                "1. How is Electricity Bill calculated?" to "Bills are calculated based on energy consumption in kilowatt-hours (kWh), where 1 unit = 1000W of electrical power consumed for 1 hour.",
                "2. How is Battery Backup calculated?" to "Backup Time (Hours) = (Battery Ah * Battery Voltage * Efficiency) / Load in Watts. Efficiency is typically assumed to be around 0.8."
            )
        }
        ToolType.COLOR_CONVERTER, ToolType.RESISTOR_CODE -> if (isBn) {
            listOf(
                "১. কালার কোড কনভার্টার কি?" to "এটি হেক্স কোড (যেমন #FF5722), আরজিবি (Red, Green, Blue) মান এবং এইচএসএল ফরম্যাটের মধ্যে পারস্পরিক রূপান্তর করতে ব্যবহৃত হয়।",
                "২. কালার হুইল কিভাবে ব্যবহার করবেন?" to "কালার হুইলে বা চাকাতে স্পর্শ করে বা ড্র্যাগ করে আপনার পছন্দের কালারটি সরাসরি নির্বাচন করতে পারেন এবং এর হেক্স কোড বা আরজিবি মান দেখতে পারেন।"
            )
        } else {
            listOf(
                "1. What is the Color Code Converter?" to "It translates colors between different formats like HEX (e.g., #FF5722), RGB (Red, Green, Blue), and HSL (Hue, Saturation, Lightness).",
                "2. How to use the Color Wheel?" to "Simply touch or drag on the circular Color Wheel to pick and view any color interactively. The corresponding HEX and RGB values update automatically."
            )
        }
        else -> if (isBn) {
            listOf(
                "১. এই ক্যালকুলেটর কিভাবে কাজ করে?" to "প্রয়োজনীয় ইনপুট ফিল্ডগুলোতে সঠিক সংখ্যা প্রদান করুন। মান পরিবর্তনের সাথে সাথে আউটপুট স্বয়ংক্রিয়ভাবে নিচে রিয়েল-টাইমে আপডেট হয়ে যাবে।"
            )
        } else {
            listOf(
                "1. How does this calculator work?" to "Fill in the required input fields with valid numbers. The results will calculate and display at the bottom in real-time as you type."
            )
        }
    }
}
