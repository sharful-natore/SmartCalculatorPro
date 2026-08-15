package com.example.ui.screens

import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.util.scaleOnPress
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.automirrored.filled.*
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
import com.example.util.AppLanguage
import com.example.data.model.ConverterCategory
import com.example.data.model.ConverterType
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.theme.themeCardShadow
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.LanguageManager
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SmartConverterScreen(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val selectedType = viewModel.selectedConverterType
    val converterScrollState = androidx.compose.runtime.saveable.rememberSaveable(saver = androidx.compose.foundation.ScrollState.Saver) {
        androidx.compose.foundation.ScrollState(0)
    }
    val converterFilterScrollState = androidx.compose.runtime.saveable.rememberSaveable(saver = androidx.compose.foundation.ScrollState.Saver) {
        androidx.compose.foundation.ScrollState(0)
    }

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
            SmartConverterCategoriesView(
                viewModel = viewModel,
                themeColors = themeColors,
                scrollState = converterScrollState,
                filterScrollState = converterFilterScrollState
            )
        } else {
            // Screen 2: Detailed Converter View
            ConverterDetailView(currentType, viewModel, themeColors)
        }
    }
}

@Composable
fun SmartConverterCategoriesView(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    scrollState: androidx.compose.foundation.ScrollState = androidx.compose.runtime.saveable.rememberSaveable(saver = androidx.compose.foundation.ScrollState.Saver) { androidx.compose.foundation.ScrollState(0) },
    filterScrollState: androidx.compose.foundation.ScrollState = androidx.compose.runtime.saveable.rememberSaveable(saver = androidx.compose.foundation.ScrollState.Saver) { androidx.compose.foundation.ScrollState(0) }
) {
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

    val context = LocalContext.current
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.converterSearchQuery = spokenText
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
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (viewModel.converterSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.converterSearchQuery = "" }) {
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
                .testTag("converter_search_input")
        )

        // Category Filter Chips
        val allConverters = ConverterType.values().toList()
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
                count = allConverters.size,
                onClick = { viewModel.selectedCategoryFilter = null }
            )

            ConverterCategory.values().forEach { cat ->
                val catCount = allConverters.count { it.category == cat }
                FilterChipItem(
                    label = cat.getTitle(viewModel.selectedLanguage),
                    isSelected = selectedFilter == cat,
                    icon = cat.icon,
                    themeColors = themeColors,
                    count = catCount,
                    onClick = {
                        viewModel.selectedCategoryFilter = if (selectedFilter == cat) null else cat
                    }
                )
            }
        }

        // Converter Cards grouped by category with Smooth Category Switch Animation
        AnimatedContent(
            targetState = selectedFilter,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220)) + slideInVertically(animationSpec = tween(220)) { it / 8 }) togetherWith
                fadeOut(animationSpec = tween(150))
            },
            label = "converterCategorySortAnimation"
        ) { currentFilter ->
            val currentFilteredConverters = if (currentFilter == null) {
                allConverters.filter { converter ->
                    searchQuery.isEmpty() ||
                    converter.titleEn.lowercase().contains(searchQuery) ||
                    converter.titleBn.lowercase().contains(searchQuery) ||
                    converter.units.any { it.lowercase().contains(searchQuery) }
                }
            } else {
                allConverters.filter { converter ->
                    converter.category == currentFilter && (
                        searchQuery.isEmpty() ||
                        converter.titleEn.lowercase().contains(searchQuery) ||
                        converter.titleBn.lowercase().contains(searchQuery) ||
                        converter.units.any { it.lowercase().contains(searchQuery) }
                    )
                }
            }

            if (currentFilteredConverters.isEmpty()) {
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
                val isOverviewMode = currentFilter == null && searchQuery.isEmpty()

                Column(modifier = Modifier.fillMaxWidth()) {
                    // Category Active Banner when filtered
                    if (currentFilter != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = themeColors.cardBg,
                            border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(themeColors.buttonEqualBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = currentFilter.icon,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = currentFilter.getTitle(viewModel.selectedLanguage),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.displayText
                                        )
                                        Text(
                                            text = if (isBn) "মোট ${currentFilteredConverters.size}টি কনভার্টার" else "Total ${currentFilteredConverters.size} Converters",
                                            fontSize = 11.sp,
                                            color = themeColors.buttonEqualBg,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.selectedCategoryFilter = null },
                                    color = themeColors.buttonEqualBg.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Apps,
                                            contentDescription = null,
                                            tint = themeColors.buttonEqualBg,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isBn) "সব কনভার্টার" else "All",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.buttonEqualBg
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val categoriesToShow = if (isOverviewMode) {
                        ConverterCategory.values().filter { cat ->
                            currentFilteredConverters.any { it.category == cat }
                        }
                    } else if (currentFilter != null) {
                        listOf(currentFilter)
                    } else {
                        ConverterCategory.values().filter { cat ->
                            currentFilteredConverters.any { it.category == cat }
                        }
                    }

                    val topConvertersMap = viewModel.categoryTopConvertersMap
                    categoriesToShow.forEach { category ->
                        val orderedCatConverters = viewModel.getAllOrderedConvertersForCategory(category)
                        val categoryConverters = orderedCatConverters.filter { currentFilteredConverters.contains(it) }

                        if (categoryConverters.isNotEmpty()) {
                            val displayedConverters = if (isOverviewMode) categoryConverters.take(4) else categoryConverters
                            val hasMore = isOverviewMode && categoryConverters.size > 4

                            // Category Header
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
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
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .defaultMinSize(minWidth = 22.dp, minHeight = 22.dp)
                                            .clip(CircleShape)
                                            .background(themeColors.buttonEqualBg)
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isBn) "${categoryConverters.size}টি" else "${categoryConverters.size}",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                if (hasMore) {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(themeColors.buttonEqualBg.copy(alpha = 0.08f))
                                            .clickable { viewModel.selectedCategoryFilter = category }
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isBn) "সব দেখুন" else "See all",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.buttonEqualBg
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = themeColors.buttonEqualBg,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }

                            // Cards Grid (2 columns) with Smooth Animated Position Reordering
                            AnimatedContent(
                                targetState = displayedConverters,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(250)) + scaleIn(initialScale = 0.96f, animationSpec = tween(250)) togetherWith
                                            fadeOut(animationSpec = tween(180))
                                },
                                label = "ConvertersGridReorder_${category.name}"
                            ) { currentDisplayedConverters ->
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    currentDisplayedConverters.chunked(2).forEach { rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            rowItems.forEach { type ->
                                                key(type.name) {
                                                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                                        ConverterCardItem(
                                                            converterType = type,
                                                            viewModel = viewModel,
                                                            themeColors = themeColors,
                                                            modifier = Modifier.fillMaxHeight(),
                                                            onClick = { viewModel.openConverter(type) }
                                                        )
                                                    }
                                                }
                                            }
                                            if (rowItems.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }

                            // Clean "See All" Button at the bottom of the section in Overview mode
                            if (hasMore) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp, bottom = 14.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.selectedCategoryFilter = category },
                                    shape = RoundedCornerShape(12.dp),
                                    color = themeColors.cardBg,
                                    border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.25f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 9.dp, horizontal = 12.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isBn) 
                                                "${category.getTitle(viewModel.selectedLanguage)}-এর সব (${categoryConverters.size}টি) কনভার্টার দেখুন" 
                                            else 
                                                "See all ${categoryConverters.size} ${category.getTitle(viewModel.selectedLanguage)} Converters",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.buttonEqualBg
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = themeColors.buttonEqualBg,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(10.dp))
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
    count: Int = 0,
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
            if (count > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color.White.copy(alpha = 0.28f)
                            else themeColors.buttonEqualBg
                        )
                        .padding(horizontal = 5.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$count",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConverterCardItem(
    converterType: ConverterType,
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFavorite = viewModel.favoriteConverters.contains(converterType.name)
    val isPinned = viewModel.isConverterPinnedInTop4(converterType)
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_${converterType.name.lowercase()}")
            .scaleOnPress(interactionSource)
            .themeCardShadow(themeColors, elevation = 1.dp)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick,
                onLongClick = {
                    viewModel.requestToggleFavoriteConverter(converterType)
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = themeColors.cardBg
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(themeColors.buttonEqualBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = converterType.icon,
                            contentDescription = converterType.getTitle(viewModel.selectedLanguage),
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.requestToggleFavoriteConverter(converterType) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pin Position",
                                tint = if (isPinned) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.35f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.toggleFavoriteConverter(converterType.name) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = converterType.getTitle(viewModel.selectedLanguage),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText,
                    maxLines = 2,
                    lineHeight = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = converterType.units.take(3).map { converterType.getLocalizedUnitName(it, viewModel.selectedLanguage) }.joinToString(", ") + if (converterType.units.size > 3) "..." else "",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = themeColors.displayText.copy(alpha = 0.7f),
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
    val context = LocalContext.current
    var isFromDropdownExpanded by remember { mutableStateOf(false) }
    var isToDropdownExpanded by remember { mutableStateOf(false) }
    var showConverterInfo by remember { mutableStateOf(false) }
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
            .padding(horizontal = 16.dp, vertical = 12.dp)
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

            Spacer(modifier = Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                val isFavorite = viewModel.favoriteConverters.contains(converterType.name)
                IconButton(
                    onClick = { viewModel.toggleFavoriteConverter(converterType.name) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red.copy(alpha = 0.8f) else themeColors.displayText.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                com.example.ui.components.InfoToggleButton(
                    isExpanded = showConverterInfo,
                    onToggle = { showConverterInfo = !showConverterInfo },
                    themeColors = themeColors
                )
            }
        }

        AnimatedVisibility(
            visible = showConverterInfo,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
            val infoTitle = if (isBn) "প্রয়োজনীয় তথ্য ও গাইডলাইন" else "Helpful Information & Guidelines"
            val infoItems = getConverterInfoItems(converterType, isBn)
            if (infoItems.isNotEmpty()) {
                com.example.ui.components.ToolInfoSection(
                    title = infoTitle,
                    infoItems = infoItems,
                    themeColors = themeColors,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }

        // Special UI for Roman Numerals and Time Zone
        if (converterType == ConverterType.ROMAN_NUMERALS) {
            RomanNumeralsCard(viewModel, themeColors)
            return@Column
        }
        if (converterType == ConverterType.TIME_ZONE) {
            TimeZoneCard(viewModel, themeColors)
            return@Column
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
                                text = converterType.getLocalizedUnitName(viewModel.fromUnit, viewModel.selectedLanguage),
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
                                    text = { Text(text = converterType.getLocalizedUnitName(unit, viewModel.selectedLanguage), color = themeColors.displayText) },
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
                                text = converterType.getLocalizedUnitName(viewModel.toUnit, viewModel.selectedLanguage),
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
                                    text = { Text(text = converterType.getLocalizedUnitName(unit, viewModel.selectedLanguage), color = themeColors.displayText) },
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

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val inputVal = viewModel.converterInput
                val outputVal = viewModel.converterOutput
                val fromUnitName = converterType.getLocalizedUnitName(viewModel.fromUnit, viewModel.selectedLanguage)
                val toUnitName = converterType.getLocalizedUnitName(viewModel.toUnit, viewModel.selectedLanguage)
                if (inputVal.isNotEmpty() && outputVal.isNotEmpty()) {
                    val expr = "$inputVal $fromUnitName"
                    val result = "$outputVal $toUnitName"
                    viewModel.saveToolResultToHistory(converterType.getTitle(viewModel.selectedLanguage), expr, result)
                    Toast.makeText(context, if (viewModel.selectedLanguage == AppLanguage.BENGALI) "ইতিহাসে সেভ করা হয়েছে!" else "Saved to history!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, if (viewModel.selectedLanguage == AppLanguage.BENGALI) "অনুগ্রহ করে মান প্রদান করুন" else "Please enter a value to convert", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = themeColors.buttonEqualBg.copy(alpha = 0.15f),
                contentColor = themeColors.buttonEqualBg
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (viewModel.selectedLanguage == AppLanguage.BENGALI) "ফলাফল হিস্টোরিতে রাখুন" else "Save Result to History", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Conversion Quick Matrix Table
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = themeColors.cardBg),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) "সমমান রূপান্তর চার্ট" else "Equivalent Conversion Chart",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) "১ থেকে ৫ মানের জন্য সকল এককে সমমান তালিকা" else "Equivalent values for input 1 to 5 across units",
                    fontSize = 11.sp,
                    color = themeColors.displayText.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                HorizontalDivider(
                    color = themeColors.displayText.copy(alpha = 0.1f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (converterType == ConverterType.NUMBER_SYSTEM) {
                    // Dedicated Number System table
                    val numberSystemRows = listOf(1, 2, 3, 4, 5)
                    val columns = if (viewModel.selectedLanguage == AppLanguage.BENGALI) {
                        listOf("ডেসিমেল (Decimal)", "বাইনারি (Binary)", "অক্টাল (Octal)", "হেক্সাডেসিমেল (Hex)")
                    } else {
                        listOf("Decimal", "Binary", "Octal", "Hexadecimal")
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .border(1.dp, themeColors.displayText.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    ) {
                        Column {
                            // Header Row
                            Row(
                                modifier = Modifier
                                    .background(themeColors.buttonNormalBg.copy(alpha = 0.12f))
                                    .padding(vertical = 10.dp)
                            ) {
                                columns.forEach { header ->
                                    Box(
                                        modifier = Modifier
                                            .width(130.dp)
                                            .padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = header,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.buttonEqualBg
                                        )
                                    }
                                }
                            }

                            // Data Rows
                            numberSystemRows.forEachIndexed { index, decimalVal ->
                                val rowBg = if (index % 2 == 1) themeColors.buttonNormalBg.copy(alpha = 0.05f) else Color.Transparent
                                Row(
                                    modifier = Modifier
                                        .background(rowBg)
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val cellValues = listOf(
                                        decimalVal.toString(),
                                        java.lang.Long.toBinaryString(decimalVal.toLong()),
                                        java.lang.Long.toOctalString(decimalVal.toLong()),
                                        java.lang.Long.toHexString(decimalVal.toLong()).uppercase()
                                    )

                                    cellValues.forEach { valStr ->
                                        Box(
                                            modifier = Modifier
                                                .width(130.dp)
                                                .padding(horizontal = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = valStr,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = themeColors.displayText
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Other Converters table
                    val rowValues = listOf(1.0, 2.0, 3.0, 4.0, 5.0)
                    val currentFromUnit = viewModel.fromUnit
                    val unitsToConvert = availableUnits // Show all available units!

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .border(1.dp, themeColors.displayText.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    ) {
                        Column {
                            // Header Row
                            Row(
                                modifier = Modifier
                                    .background(themeColors.buttonNormalBg.copy(alpha = 0.12f))
                                    .padding(vertical = 10.dp)
                            ) {
                                unitsToConvert.forEach { unit ->
                                    val isFromUnit = unit == currentFromUnit
                                    Box(
                                        modifier = Modifier
                                            .width(130.dp)
                                            .padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = converterType.getLocalizedUnitName(unit, viewModel.selectedLanguage),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isFromUnit) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }

                            // Data Rows
                            val df = java.text.DecimalFormat("#.######")
                            rowValues.forEachIndexed { rowIndex, baseVal ->
                                val rowBg = if (rowIndex % 2 == 1) themeColors.buttonNormalBg.copy(alpha = 0.05f) else Color.Transparent
                                Row(
                                    modifier = Modifier
                                        .background(rowBg)
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    unitsToConvert.forEach { targetUnit ->
                                        val isFromUnit = targetUnit == currentFromUnit
                                        val convertedVal = if (isFromUnit) {
                                            baseVal
                                        } else {
                                            if (converterType == ConverterType.CURRENCY) {
                                                converterType.convert(currentFromUnit, targetUnit, baseVal, customRates = viewModel.exchangeRates)
                                            } else {
                                                converterType.convert(currentFromUnit, targetUnit, baseVal)
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .width(130.dp)
                                                .padding(horizontal = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = df.format(convertedVal),
                                                fontSize = 13.sp,
                                                fontWeight = if (isFromUnit) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isFromUnit) themeColors.buttonEqualBg else themeColors.displayText
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
    }
}

private fun getConverterInfoItems(converterType: ConverterType, isBn: Boolean): List<Pair<String, String>> {
    return when (converterType) {
        ConverterType.LENGTH -> if (isBn) {
            listOf(
                "১. দৈর্ঘ্য রূপান্তর" to "দৈর্ঘ্য হলো এক বিন্দু থেকে অন্য বিন্দুর দূরত্ব। আন্তর্জাতিক মান অনুযায়ী মিটার ও কিলোমিটার ব্যবহার করা হয়। আর দেশীয় ও ব্রিটিশ নিয়মে ইঞ্চি, ফুট, গজ ও মাইল বহুল ব্যবহৃত।",
                "২. কিছু দরকারি সম্পর্ক" to "• ১ মিটার = ৩৯.৩৭ ইঞ্চি = ৩.২৮ ফুট\n• ১ কিলোমিটার = ০.৬২ মাইল\n• ১ গজ = ৩ ফুট = ৩৬ ইঞ্চি"
            )
        } else {
            listOf(
                "1. Length Conversion Basics" to "Length is the measurement of distance from end to end. Common metric units are Meters and Kilometers, while imperial units include Inches, Feet, Yards, and Miles.",
                "2. Handy Relations" to "• 1 Meter = 39.37 Inches = 3.28 Feet\n• 1 Kilometer = 0.62 Miles\n• 1 Yard = 3 Feet = 36 Inches"
            )
        }
        ConverterType.WEIGHT -> if (isBn) {
            listOf(
                "১. ভর ও ওজন রূপান্তর" to "ভর হলো কোনো বস্তুতে মোট পদার্থের পরিমাণ। ভর মাপার আদর্শ একক হলো কেজি ও গ্রাম। আর ব্রিটিশ পদ্ধতিতে পাউন্ড ও আউন্স ব্যবহৃত হয়। এছাড়া স্বর্ণ ও মূল্যবান জিনিস মাপতে ভরি, আনা ও রতি ব্যবহৃত হয়।",
                "২. দরকারি মানসমূহ" to "• ১ কেজি = ২.২০৪ পাউন্ড\n• ১ ভরি = ১১.৬৬৪ গ্রাম\n• ১ আউন্স = ২৮.৩৫ গ্রাম"
            )
        } else {
            listOf(
                "1. Weight & Mass Basics" to "Mass represents the amount of matter in an object, while weight is the gravitational force acting on it. Units include Kilograms, Grams, Pounds, Ounces, Tons, and traditional South Asian units like Vori, Anna, and Ratti.",
                "2. Purity & Benchmarks" to "• 1 Kilogram = 2.204 Pounds\n• 1 Vori = 11.664 Grams\n• 1 Ounce = 28.35 Grams"
            )
        }
        ConverterType.AREA -> if (isBn) {
            listOf(
                "১. ক্ষেত্রফল রূপান্তর" to "ক্ষেত্রফল হলো কোনো দ্বিমাত্রিক তলের পরিমাপ। বৈশ্বিক পরিমাপে বর্গফুট ও বর্গমিটার ব্যবহার করা হয়। বাংলাদেশে জমি কেনাবেচা ও পরিমাপে শতাংশ, কাঠা, বিঘা এবং একর ব্যবহৃত হয়।",
                "২. জমি পরিমাপের নিয়ম" to "• ১ একর = ১০০ শতাংশ = ৪৩,৫৬০ বর্গফুট\n• ১ কাঠা = ৭২০ বর্গফুট\n• ১ বিঘা = ২০ কাঠা = ৩৩ শতাংশ"
            )
        } else {
            listOf(
                "1. Area Measurement Units" to "Area measures the size of a two-dimensional surface. Standard units are Square Meters and Square Feet, alongside traditional land measurement units like Acre, Hectare, Shotangsho, Katha, and Bigha.",
                "2. Traditional Land Units" to "• 1 Acre = 43,560 Square Feet = 100 Shotangsho\n• 1 Katha = 720 Square Feet\n• 1 Bigha = 20 Katha = 33 Shotangsho (standard in Bangladesh)"
            )
        }
        ConverterType.TEMPERATURE -> if (isBn) {
            listOf(
                "১. তাপমাত্রা রূপান্তর" to "तापমাত্রা পরিমাপের প্রধান তিনটি একক হলো সেলসিয়াস, ফারেনহাইট ও কেলভিন। আবহাওয়া পরিমাপে সেলসিয়াস এবং মানুষের শরীরের তাপমাত্রা প্রকাশে ফারেনহাইট ব্যবহৃত হয়।",
                "২. গাণিতিক সূত্র" to "• ফারেনহাইট = (সেলসিয়াস × ৯/৫) + ৩২\n• কেলভিন = সেলসিয়াস + ২৭৩.১৫"
            )
        } else {
            listOf(
                "1. Temperature Scales" to "Celsius (°C) is used globally for weather. Fahrenheit (°F) is common in the US and for body temperature. Kelvin (K) is the scientific absolute standard.",
                "2. Formulas" to "• °F = (°C × 9/5) + 32\n• K = °C + 273.15"
            )
        }
        ConverterType.VOLUME -> if (isBn) {
            listOf(
                "১. আয়তন রূপান্তর" to "কোনো বস্তু বা তরল যতটুকু ত্রিমাত্রিক স্থান দখল করে তা-ই তার আয়তন। প্রধান এককগুলো হলো লিটার, মিলিলিটার, গ্যালন এবং ঘনমিটার।",
                "২. গুরুত্বপূর্ণ মান" to "• ১ লিটার = ১০০০ মিলিলিটার\n• ১ গ্যালন (ইউএস) = ৩.৭৮৫ লিটার"
            )
        } else {
            listOf(
                "1. Volume Conversion" to "Volume measures the 3D space occupied by liquid, gas, or solid. Units include Liters, Milliliters, Gallons, and Cubic Meters.",
                "2. Relations" to "• 1 Liter = 1000 Milliliters\n• 1 Gallon (US) = 3.785 Liters"
            )
        }
        ConverterType.PRESSURE -> if (isBn) {
            listOf(
                "১. চাপ রূপান্তর" to "প্রতি একক ক্ষেত্রফলের ওপর লম্বভাবে প্রযুক্ত বলকে চাপ বলে। প্রধান এককগুলো হলো প্যাসকেল, বার, পিএসআই (PSI) এবং অ্যাটমোস্ফিয়ার (Atm)।",
                "২. দরকারি সম্পর্ক" to "• ১ বার = ১০০,০০০ প্যাসকেল\n• ১ অ্যাটমোস্ফিয়ার = ১৪.৬৯৬ পিএসআই"
            )
        } else {
            listOf(
                "1. Pressure Basics" to "Pressure is force applied perpendicular to a surface per unit area. Common units are Pascal, Bar, PSI, and Atmosphere.",
                "2. Relations" to "• 1 Bar = 100,000 Pascals\n• 1 Atmosphere = 14.696 PSI"
            )
        }
        ConverterType.POWER -> if (isBn) {
            listOf(
                "১. ক্ষমতা রূপান্তর" to "কাজ করার হার বা শক্তি স্থানান্তরের হারকে ক্ষমতা বলে। বৈদ্যুতিক যন্ত্রের রেটিং ওয়াট বা কিলোওয়াটে এবং মোটরের শক্তি হর্সপাওয়ারে (HP) মাপা হয়।",
                "২. দরকারি সম্পর্ক" to "• ১ কিলোওয়াট = ১০০০ ওয়াট\n• ১ হর্সপাওয়ার (HP) = ৭৪৬ ওয়াট"
            )
        } else {
            listOf(
                "1. Power Units" to "Power is the rate at which work is done or energy is transferred. Units include Watts, Kilowatts, and Horsepower (HP).",
                "2. Relations" to "• 1 Kilowatt = 1000 Watts\n• 1 Horsepower (HP) = 746 Watts"
            )
        }
        ConverterType.ENERGY -> if (isBn) {
            listOf(
                "১. শক্তি রূপান্তর" to "কাজ করার সামর্থ্যকে শক্তি বলে। পদার্থবিদ্যায় জুল ব্যবহৃত হয় এবং খাদ্য ও পুষ্টিবিজ্ঞানে ক্যালোরি বা কিলোকেলরি (Kcal) ব্যবহৃত হয়।",
                "২. দরকারি সম্পর্ক" to "• ১ ক্যালোরি = ৪.১৮৪ জুল\n• ১ কিলোকেলরি = ১০০০ ক্যালোরি"
            )
        } else {
            listOf(
                "1. Energy Conversion" to "Energy is the quantitative property transferred to perform work or heat. Units are Joules, Kilojoules, Calories, and Kilocalories.",
                "2. Relations" to "• 1 Calorie = 4.184 Joules\n• 1 Kilocalorie (food calorie) = 1000 Calories"
            )
        }
        ConverterType.FORCE -> if (isBn) {
            listOf(
                "১. বল রূপান্তর" to "যা কোনো স্থির বস্তুকে গতিশীল করে বা গতির পরিবর্তন ঘটায় তাকে বল বলে। এসআই একক হলো নিউটন।"
            )
        } else {
            listOf(
                "1. Force Conversion" to "Force is an influence that changes the motion of an object. Standard SI unit is Newton, alongside Dyne and Pound-force."
            )
        }
        ConverterType.TORQUE -> if (isBn) {
            listOf(
                "১. টর্ক রূপান্তর" to "কোনো বস্তুকে অক্ষের চারদিকে ঘোরাতে যে বল প্রয়োগ করতে হয় তাকে টর্ক বলে। গাড়ি ও ইঞ্জিনের শক্তিতে এটি গুরুত্বপূর্ণ।"
            )
        } else {
            listOf(
                "1. Torque Conversion" to "Torque measures the rotational force acting on an object. Common units are Newton-meter and Pound-foot."
            )
        }
        ConverterType.DENSITY -> if (isBn) {
            listOf(
                "১. ঘনত্ব রূপান্তর" to "ঘনত্ব হলো কোনো পদার্থের একক আয়তনের ভর। বহুল ব্যবহৃত একক হলো কেজি/ঘনমিটার বা গ্রাম/ঘনসেন্টিমিটার।"
            )
        } else {
            listOf(
                "1. Density Conversion" to "Density measures mass per unit volume of a substance. Commonly written in kg/m³ or g/cm³."
            )
        }
        ConverterType.ANGLE -> if (isBn) {
            listOf(
                "১. কোণ রূপান্তর" to "কোণ পরিমাপের প্রধান একক দুটি হলো ডিগ্রি ও রেডিয়ান। সম্পূর্ণ বৃত্তের কোণ হলো ৩৬০ ডিগ্রি বা ২π রেডিয়ান।"
            )
        } else {
            listOf(
                "1. Angle Conversion" to "Angles are measured in Degrees (360° for a full circle) or Radians (2π for a full circle) used in trigonometry."
            )
        }
        ConverterType.DIGITAL_STORAGE -> if (isBn) {
            listOf(
                "১. ডেটা স্টোরেজ রূপান্তর" to "কম্পিউটার বা ফোনের মেমোরি বা ফাইল সাইজ পরিমাপে এটি ব্যবহৃত হয়। ১ বাইট = ৮ বিট।",
                "২. দরকারি সম্পর্ক" to "• ১ কিলোবাইট (KB) = ১০২৪ বাইট\n• ১ মেগাবাইট (MB) = ১০২৪ KB\n• ১ গিগাবাইট (GB) = ১০২৪ MB"
            )
        } else {
            listOf(
                "1. Digital Data Conversion" to "Digital storage measures memory capacity. 1 Byte = 8 Bits. Standard progression is in factors of 1024.",
                "2. Relations" to "• 1 Kilobyte (KB) = 1024 Bytes\n• 1 Megabyte (MB) = 1024 KB\n• 1 Gigabyte (GB) = 1024 MB"
            )
        }
        ConverterType.DATA_TRANSFER -> if (isBn) {
            listOf(
                "১. ডেটা স্পিড রূপান্তর" to "ইন্টারনেটের গতি বা ফাইল স্থানান্তরের গতি পরিমাপ করতে এটি ব্যবহৃত হয়।",
                "২. দরকারি পার্থক্য" to "• Mbps হলো ব্যান্ডউইথ বা নেট স্পিড (বিট)।\n• MB/s হলো প্রকৃত ফাইল ডাউনলোড স্পিড (বাইট)।\n• ১ MB/s = ৮ Mbps"
            )
        } else {
            listOf(
                "1. Internet Speed Basics" to "Data transfer rates measure network speeds. Usually written in Mbps (megabits per second) or MB/s (megabytes per second).",
                "2. Key difference" to "• Mbps is internet bandwidth speed (bits).\n• MB/s is the actual file downloading speed (bytes).\n• 1 MB/s = 8 Mbps"
            )
        }
        ConverterType.FREQUENCY -> if (isBn) {
            listOf(
                "১. ফ্রিকোয়েন্সি রূপান্তর" to "প্রতি সেকেন্ডে কোনো তরঙ্গের পূর্ণ কম্পন সংখ্যাকে ফ্রিকোয়েন্সি বা কম্পাঙ্ক বলে। এর একক হলো হার্টজ (Hz)।"
            )
        } else {
            listOf(
                "1. Frequency Basics" to "Frequency is the number of occurrences of a repeating event per unit of time. Measured in Hertz (Hz), kHz, MHz, and GHz."
            )
        }
        ConverterType.NUMBER_SYSTEM -> if (isBn) {
            listOf(
                "১. সংখ্যা পদ্ধতি" to "ডিজিটাল ডিভাইসে ব্যবহৃত সংখ্যা পদ্ধতির মধ্যে রয়েছে বাইনারি (ভিত্তি ২), দশমিক (ভিত্তি ১০), অক্টাল (ভিত্তি ৮) এবং হেক্সাডেসিমেল (ভিত্তি ১৬)।"
            )
        } else {
            listOf(
                "1. Number Systems" to "Computers use Binary (base-2). Standard systems are Decimal (base-10), Octal (base-8), and Hexadecimal (base-16)."
            )
        }
        ConverterType.SPEED -> if (isBn) {
            listOf(
                "১. গতিবেগ রূপান্তর" to "গতিবেগ হলো নির্দিষ্ট সময়ে অতিক্রান্ত দূরত্ব। প্রধান এককগুলো হলো কিমি/ঘণ্টা, মাইল/ঘণ্টা এবং নট (নৌযান ও বিমানের গতি পরিমাপের একক)।"
            )
        } else {
            listOf(
                "1. Speed Conversion" to "Speed is distance traveled per unit time. Standard units are Km/h (globally), Mph (US/UK), and Knots (for aviation/marine navigation)."
            )
        }
        ConverterType.TIME -> if (isBn) {
            listOf(
                "১. সময় রূপান্তর" to "সেকেন্ড, মিনিট, ঘণ্টা, দিন, সপ্তাহ, মাস এবং বছরের মধ্যে পারস্পরিক নিখুঁত রূপান্তর করা যায়।"
            )
        } else {
            listOf(
                "1. Time Conversions" to "Time units are historical and astronomical. 1 Day = 24 Hours, 1 Hour = 60 Minutes, 1 Minute = 60 Seconds."
            )
        }
        ConverterType.FUEL_CONSUMPTION -> if (isBn) {
            listOf(
                "১. জ্বালানি খরচ রূপান্তর" to "যানবাহনের জ্বালানি দক্ষতা পরিমাপের একক। যেমন: প্রতি লিটারে কত কিমি যায় (km/L) অথবা প্রতি ১০০ কিমিতে কত লিটার লাগে (L/100km)।"
            )
        } else {
            listOf(
                "1. Fuel Efficiency Metrics" to "Measures how far a car goes per volume of fuel: km/L, MPG (Miles Per Gallon), or L/100km (Liters per 100 Kilometers)."
            )
        }
        ConverterType.ACCELERATION -> if (isBn) {
            listOf(
                "১. ত্বরণ রূপান্তর" to "সময়ের সাথে বেগের পরিবর্তনের হারকে ত্বরণ বলে। এর একক মিটার/সেকেন্ড স্কয়ার (m/s²)।"
            )
        } else {
            listOf(
                "1. Acceleration Conversion" to "The rate of change of speed with time. Commonly measured in m/s² or g-force."
            )
        }
        ConverterType.ELECTRIC_CURRENT -> if (isBn) {
            listOf(
                "১. বিদ্যুৎ প্রবাহ রূপান্তর" to "কোনো পরিবাহীর মধ্য দিয়ে বৈদ্যুতিক চার্জ প্রবাহের হারকে কারেন্ট বলে। এর আন্তর্জাতিক একক হলো অ্যাম্পিয়ার (A)।"
            )
        } else {
            listOf(
                "1. Electric Current Units" to "Current is the rate of flow of electric charge. Standard unit is Ampere (A)."
            )
        }
        ConverterType.VOLTAGE -> if (isBn) {
            listOf(
                "১. ভোল্টেজ রূপান্তর" to "বৈদ্যুতিক বিভব পার্থক্য যা পরিবাহীর মধ্য দিয়ে কারেন্টকে চালিত করে। এর একক হলো ভোল্ট (V)।"
            )
        } else {
            listOf(
                "1. Voltage Basics" to "Voltage is the electrical potential difference that drives current. Units are Volt, Kilovolt, and Millivolt."
            )
        }
        ConverterType.RESISTANCE -> if (isBn) {
            listOf(
                "১. রোধ রূপান্তর" to "পরিবাহীর যে ধর্মের জন্য এর মধ্য দিয়ে বিদ্যুৎ প্রবাহ বাধাগ্রস্ত হয় তাকে রোধ বা রেজিস্ট্যান্স বলে। এর একক ওহম (Ω)।"
            )
        } else {
            listOf(
                "1. Resistance Basics" to "Electrical resistance opposes current flow. Measured in Ohms (Ω), Kiloohms, and Megaohms."
            )
        }
        ConverterType.ELECTRIC_CHARGE -> if (isBn) {
            listOf(
                "১. বৈদ্যুতিক চার্জ রূপান্তর" to "ব্যাটারির ধারণক্ষমতা বা সঞ্চিত চার্জ মাপা হয় মিলিঅ্যাম্পিয়ার-আওয়ার (mAh) বা অ্যাম্পিয়ার-আওয়ার (Ah) দিয়ে।"
            )
        } else {
            listOf(
                "1. Electric Charge Units" to "Measures total electricity stored or transferred. Units include Coulomb, Ah, and mAh (common for phone batteries)."
            )
        }
        ConverterType.COOKING -> if (isBn) {
            listOf(
                "১. রন্ধনশিল্প পরিমাপ" to "রান্না বা বেকিংয়ের রেসিপিতে ব্যবহৃত চা-চামচ (Teaspoon), টেবিল-চামচ (Tablespoon) ও কাপের পরিমাপ নির্ভুল করতে এটি ব্যবহৃত হয়।"
            )
        } else {
            listOf(
                "1. Kitchen Measurements" to "Converts volume recipes between Teaspoons, Tablespoons, Cups, and Fluid Ounces for precise baking."
            )
        }
        ConverterType.TYPOGRAPHY -> if (isBn) {
            listOf(
                "১. টাইপোগ্রাফি রূপান্তর" to "ওয়েবসাইট ও স্ক্রিন ডিজাইনে পিক্সেল (Pixel), পয়েন্ট (Point) এবং রিলেティブ ইউনিট যেমন EM ও REM এর রূপান্তর।"
            )
        } else {
            listOf(
                "1. Web Design Typography" to "Pixels (px) are fixed screen dots. Points (pt) are for print. EM and REM are relative to the font scale of the web page."
            )
        }
        ConverterType.CURRENCY -> if (isBn) {
            listOf(
                "১. মুদ্রা রূপান্তর" to "দেশী-বিদেশী বিভিন্ন দেশের টাকার মান (যেমন: ডলার, ইউরো, রুপি, রিয়াল ইত্যাদি) বাংলাদেশী টাকায় বা অন্য যেকোনো মুদ্রায় রূপান্তর করা যায়।"
            )
        } else {
            listOf(
                "1. Exchange Rate Conversions" to "Allows converting money between USD, BDT, EUR, GBP, INR, and SAR. Uses live online rates if connected."
            )
        }
        else -> emptyList()
    }
}
