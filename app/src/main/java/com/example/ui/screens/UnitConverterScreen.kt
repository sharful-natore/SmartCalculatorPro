package com.example.ui.screens

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
import androidx.compose.foundation.border

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConverterCategory
import com.example.data.model.ConverterType
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.LanguageManager
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UnitConverterScreen(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val selectedType = viewModel.selectedConverterType

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
        label = "converter_screen_transition"
    ) { currentType ->
        if (currentType == null) {
            // Screen 1: Category Cards Grid View
            ConverterCategoriesView(viewModel, themeColors)
        } else {
            // Screen 2: Detailed Converter View
            ConverterDetailView(currentType, viewModel, themeColors)
        }
    }
}

@Composable
fun ConverterCategoriesView(
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

    val allConverters = ConverterType.values()
    val searchQuery = viewModel.converterSearchQuery.lowercase().trim()
    val selectedFilter = viewModel.selectedCategoryFilter

    val filteredConverters = allConverters.filter { converter ->
        val matchesCategory = selectedFilter == null || converter.category == selectedFilter
        val matchesSearch = searchQuery.isEmpty() ||
                converter.titleEn.lowercase().contains(searchQuery) ||
                converter.titleBn.lowercase().contains(searchQuery) ||
                converter.units.any { it.lowercase().contains(searchQuery) }
        matchesCategory && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .nestedScroll(nestedScrollConnection)
            .offset { IntOffset(0, bounceAnimatable.value.roundToInt()) }
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 2.dp)
    ) {
        // Search TextField
        OutlinedTextField(
            value = viewModel.converterSearchQuery,
            onValueChange = { viewModel.converterSearchQuery = it },
            placeholder = {
                Text(
                    text = LanguageManager.getString("search_converter", viewModel.selectedLanguage),
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
                if (viewModel.converterSearchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.converterSearchQuery = "" }) {
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
                .testTag("converter_search_input")
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
            FilterChipItem(
                label = LanguageManager.getString("all", viewModel.selectedLanguage),
                isSelected = selectedFilter == null,
                icon = Icons.Default.Apps,
                themeColors = themeColors,
                onClick = { viewModel.selectedCategoryFilter = null }
            )

            ConverterCategory.values().forEach { cat ->
                FilterChipItem(
                    label = cat.getTitle(viewModel.selectedLanguage),
                    isSelected = selectedFilter == cat,
                    icon = cat.icon,
                    themeColors = themeColors,
                    onClick = {
                        viewModel.selectedCategoryFilter = if (selectedFilter == cat) null else cat
                    }
                )
            }
        }

        // Converter Cards grouped by category
        if (filteredConverters.isEmpty()) {
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
            val categoriesToShow = ConverterCategory.values().filter { cat ->
                filteredConverters.any { it.category == cat }
            }

            categoriesToShow.forEach { category ->
                val categoryConverters = filteredConverters.filter { it.category == category }

                if (categoryConverters.isNotEmpty()) {
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

                    // Cards Grid (2 columns)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        categoryConverters.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowItems.forEach { type ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        ConverterCardItem(
                                            converterType = type,
                                            viewModel = viewModel,
                                            themeColors = themeColors,
                                            onClick = { viewModel.openConverter(type) }
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
fun FilterChipItem(
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
fun ConverterCardItem(
    converterType: ConverterType,
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
            .testTag("card_${converterType.name.lowercase()}")
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
                        imageVector = converterType.icon,
                        contentDescription = converterType.getTitle(viewModel.selectedLanguage),
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
                text = converterType.getTitle(viewModel.selectedLanguage),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = converterType.units.take(3).joinToString(", ") + if (converterType.units.size > 3) "..." else "",
                fontSize = 10.sp,
                color = themeColors.displayText.copy(alpha = 0.45f),
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterDetailView(
    converterType: ConverterType,
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    var isFromDropdownExpanded by remember { mutableStateOf(false) }
    var isToDropdownExpanded by remember { mutableStateOf(false) }
    val availableUnits = converterType.units

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
            .padding(horizontal = 16.dp, vertical = 2.dp)
    ) {
        // Back Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            FilledIconButton(
                onClick = { viewModel.closeConverterDetail() },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = themeColors.cardBg,
                    contentColor = themeColors.displayText
                ),
                modifier = Modifier
                    .size(40.dp)
                    .testTag("back_to_converter_list")
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
                    text = converterType.getTitle(viewModel.selectedLanguage),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
                Text(
                    text = converterType.category.getTitle(viewModel.selectedLanguage),
                    fontSize = 12.sp,
                    color = themeColors.buttonEqualBg,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Live Exchange Rates Status & Manual Update Banner for Currency
        if (converterType == ConverterType.CURRENCY) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = themeColors.cardBg),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Currency Rates",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = LanguageManager.getString("live_rates_badge", viewModel.selectedLanguage),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                        Text(
                            text = "${LanguageManager.getString("last_updated", viewModel.selectedLanguage)}: ${viewModel.lastCurrencyUpdateTimestamp}",
                            fontSize = 11.sp,
                            color = themeColors.displayText.copy(alpha = 0.6f)
                        )
                    }

                    Button(
                        onClick = { viewModel.fetchExchangeRates() },
                        enabled = !viewModel.isFetchingExchangeRates,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColors.buttonEqualBg,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("update_currency_rates_button")
                    ) {
                        if (viewModel.isFetchingExchangeRates) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Rates",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = LanguageManager.getString("update_rates", viewModel.selectedLanguage),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // From Unit Panel Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = themeColors.cardBg),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = LanguageManager.getString("from", viewModel.selectedLanguage),
                        fontSize = 13.sp,
                        color = themeColors.displayExpressionText,
                        fontWeight = FontWeight.Bold
                    )

                    // Dropdown Trigger on the right side
                    val fromInteractionSource = remember { MutableInteractionSource() }
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(themeColors.background)
                                .scaleOnPress(fromInteractionSource)
                                .clickable(
                                    interactionSource = fromInteractionSource,
                                    indication = null
                                ) { isFromDropdownExpanded = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("from_unit_dropdown")
                        ) {
                            Text(
                                text = viewModel.fromUnit,
                                color = themeColors.buttonNormalText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown Arrow",
                                tint = themeColors.buttonNormalText
                            )
                        }

                        DropdownMenu(
                            expanded = isFromDropdownExpanded,
                            onDismissRequest = { isFromDropdownExpanded = false },
                            modifier = Modifier.background(themeColors.cardBg)
                        ) {
                            availableUnits.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(text = unit, color = themeColors.displayText) },
                                    onClick = {
                                        viewModel.fromUnit = unit
                                        viewModel.calculateConverter()
                                        isFromDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = TextFieldValue(
                        text = viewModel.converterInput,
                        selection = TextRange(viewModel.converterInput.length)
                    ),
                    onValueChange = {
                        viewModel.converterInput = it.text
                        viewModel.calculateConverter()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = themeColors.buttonEqualBg,
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText
                    ),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag("converter_input_field")
                )
            }
        }

        // Swap Floating Action Button
        val swapInteractionSource = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            FilledIconButton(
                onClick = { viewModel.swapUnits() },
                interactionSource = swapInteractionSource,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = themeColors.buttonEqualBg,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .size(44.dp)
                    .scaleOnPress(swapInteractionSource)
                    .testTag("swap_units_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = "Swap Units",
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // To Unit Panel Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = themeColors.cardBg),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = LanguageManager.getString("to", viewModel.selectedLanguage),
                        fontSize = 13.sp,
                        color = themeColors.displayExpressionText,
                        fontWeight = FontWeight.Bold
                    )

                    // Dropdown Trigger on the right side
                    val toInteractionSource = remember { MutableInteractionSource() }
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(themeColors.background)
                                .scaleOnPress(toInteractionSource)
                                .clickable(
                                    interactionSource = toInteractionSource,
                                    indication = null
                                ) { isToDropdownExpanded = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("to_unit_dropdown")
                        ) {
                            Text(
                                text = viewModel.toUnit,
                                color = themeColors.buttonNormalText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown Arrow",
                                tint = themeColors.buttonNormalText
                            )
                        }

                        DropdownMenu(
                            expanded = isToDropdownExpanded,
                            onDismissRequest = { isToDropdownExpanded = false },
                            modifier = Modifier.background(themeColors.cardBg)
                        ) {
                            availableUnits.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(text = unit, color = themeColors.displayText) },
                                    onClick = {
                                        viewModel.toUnit = unit
                                        viewModel.calculateConverter()
                                        isToDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = TextFieldValue(
                        text = viewModel.converterOutput,
                        selection = TextRange(viewModel.converterOutput.length)
                    ),
                    onValueChange = {
                        viewModel.converterOutput = it.text
                        viewModel.calculateConverterReverse()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = themeColors.buttonEqualBg,
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText
                    ),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag("converter_output_text")
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Conversion Quick Matrix Table
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = themeColors.cardBg),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = LanguageManager.getString("quick_table", viewModel.selectedLanguage),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
                Text(
                    text = "1 ${viewModel.fromUnit} =",
                    fontSize = 12.sp,
                    color = themeColors.buttonEqualBg,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                HorizontalDivider(
                    color = themeColors.displayText.copy(alpha = 0.1f),
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                val df = DecimalFormat("#.######")
                val otherUnits = availableUnits.filter { it != viewModel.fromUnit }

                otherUnits.forEach { targetUnit ->
                    val equivalentValue = if (converterType == ConverterType.CURRENCY) {
                        converterType.convert(viewModel.fromUnit, targetUnit, 1.0, customRates = viewModel.exchangeRates)
                    } else {
                        converterType.convert(viewModel.fromUnit, targetUnit, 1.0)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = targetUnit,
                            fontSize = 13.sp,
                            color = themeColors.displayText.copy(alpha = 0.8f)
                        )
                        Text(
                            text = df.format(equivalentValue),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                    }
                }
            }
        }
    }
}
