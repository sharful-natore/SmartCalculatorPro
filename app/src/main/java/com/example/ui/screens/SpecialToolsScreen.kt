package com.example.ui.screens

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
        }
    }
}
