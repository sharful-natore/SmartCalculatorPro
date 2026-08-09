package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

    val allTools = ToolType.values()
    val searchQuery = viewModel.toolSearchQuery.lowercase().trim()
    val selectedFilter = viewModel.selectedToolCategoryFilter

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
            .padding(16.dp)
            .verticalScroll(scrollState)
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
                if (viewModel.toolSearchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.toolSearchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = themeColors.displayText.copy(alpha = 0.6f)
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
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) themeColors.buttonEqualBg else themeColors.cardBg)
            .clickable(onClick = onClick)
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
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tool_card_${toolType.name.lowercase()}"),
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Back Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            FilledIconButton(
                onClick = { viewModel.closeToolDetail() },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = themeColors.cardBg,
                    contentColor = themeColors.displayText
                ),
                modifier = Modifier
                    .size(40.dp)
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
            ToolType.ELECTRICITY_BILL -> ElectricityBillCalculatorCard(viewModel, themeColors)
            ToolType.APPLIANCE_COST -> ApplianceEnergyCostCard(viewModel, themeColors)
            ToolType.BATTERY_BACKUP -> BatteryBackupCard(viewModel, themeColors)
            ToolType.FUEL_COST -> FuelCostCalculatorCard(viewModel, themeColors)
            ToolType.SPEED_DISTANCE_TIME -> SpeedDistanceTimeCard(viewModel, themeColors)
        }
    }
}
