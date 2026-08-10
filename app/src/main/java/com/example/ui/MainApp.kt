package com.example.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.OverscrollConfiguration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.util.scaleOnPress
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.scale

import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.example.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import com.example.ui.screens.*
import com.example.ui.components.GlobalSearchDialog
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import com.example.util.LanguageManager

@Composable
fun MainApp(viewModel: CalculatorViewModel) {
    val themeColors = viewModel.getCurrentThemeColors()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    MainContent(viewModel, themeColors, focusManager, context)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainContent(
    viewModel: CalculatorViewModel,
    themeColors: com.example.ui.theme.CalculatorThemeColors,
    focusManager: androidx.compose.ui.focus.FocusManager,
    context: android.content.Context
) {
    CompositionLocalProvider(
        LocalOverscrollConfiguration provides OverscrollConfiguration()
    ) {
        // Update system bars color based on theme
        SideEffect {
        val window = (context as? Activity)?.window
        if (window != null) {
            val barColor = themeColors.buttonEqualBg
            window.statusBarColor = android.graphics.Color.rgb(
                (barColor.red * 255).toInt(),
                (barColor.green * 255).toInt(),
                (barColor.blue * 255).toInt()
            )
            window.navigationBarColor = android.graphics.Color.rgb(
                (barColor.red * 255).toInt(),
                (barColor.green * 255).toInt(),
                (barColor.blue * 255).toInt()
            )
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = !themeColors.isDark && barColor.luminance() > 0.5f
                isAppearanceLightNavigationBars = !themeColors.isDark && barColor.luminance() > 0.5f
            }
        }
    }

    // Hide keyboard when switching tabs
    LaunchedEffect(viewModel.activeTab) {
        focusManager.clearFocus()
    }

    LaunchedEffect(Unit) {
        try {
            val prefs = context.getSharedPreferences("app_error_prefs", android.content.Context.MODE_PRIVATE)
            val err = prefs.getString("last_error", null)
            if (err != null) {
                val stack = prefs.getString("last_stacktrace", "")
                prefs.edit().clear().apply()
                viewModel.reportError("⚠️ অ্যাপে পূর্ববর্তী একটি ত্রুটি ধরা পড়েছে (Previous error caught):\n$err\n\nStacktrace:\n$stack")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = viewModel.activeTab.coerceAtMost(3)) { 4 }

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    // Sync from pager state to ViewModel
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            if (viewModel.activeTab < 4) {
                viewModel.activeTab = pagerState.currentPage
            }
        }
    }

    // Sync from ViewModel to pager state
    LaunchedEffect(viewModel.activeTab) {
        if (viewModel.activeTab < 4 && pagerState.currentPage != viewModel.activeTab) {
            pagerState.animateScrollToPage(viewModel.activeTab)
        }
    }

    // Handle Back Press
    val activity = context as? Activity
    BackHandler(enabled = true) {
        when {
            // If AI Chat is showing, close it first
            viewModel.showAiChat -> {
                viewModel.showAiChat = false
            }
            // If inside a specific converter detail screen
            viewModel.activeTab == 1 && viewModel.selectedConverterType != null -> {
                viewModel.closeConverterDetail()
            }
            // If inside a specific tool detail screen
            viewModel.activeTab == 2 && viewModel.selectedToolType != null -> {
                viewModel.closeToolDetail()
            }
            // If on non-home tab (Converter, Tools, History, Themes)
            viewModel.activeTab != 0 -> {
                viewModel.activeTab = 0
            }
            // If on home tab (Calculator)
            else -> {
                viewModel.showExitDialog = true
            }
        }
    }

    // Exit Confirmation Dialog
    if (viewModel.showExitDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showExitDialog = false },
            title = {
                Text(
                    text = LanguageManager.getString("exit_title", viewModel.selectedLanguage),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = themeColors.displayText
                )
            },
            text = {
                Text(
                    text = LanguageManager.getString("exit_msg", viewModel.selectedLanguage),
                    fontSize = 14.sp,
                    color = themeColors.displayText.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.showExitDialog = false
                        activity?.finish()
                    }
                ) {
                    Text(
                        text = LanguageManager.getString("exit_confirm", viewModel.selectedLanguage),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.showExitDialog = false }
                ) {
                    Text(
                        text = LanguageManager.getString("exit_cancel", viewModel.selectedLanguage),
                        color = themeColors.displayText
                    )
                }
            },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(themeColors.buttonEqualBg)
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (viewModel.activeTab == 4) {
                            IconButton(onClick = { viewModel.activeTab = 0 }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                        }
                        
                        AnimatedContent(
                            targetState = viewModel.activeTab,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    (slideInVertically { height -> height } + fadeIn(animationSpec = tween(220))) togetherWith
                                            (slideOutVertically { height -> -height } + fadeOut(animationSpec = tween(180)))
                                } else {
                                    (slideInVertically { height -> -height } + fadeIn(animationSpec = tween(220))) togetherWith
                                            (slideOutVertically { height -> height } + fadeOut(animationSpec = tween(180)))
                                }
                            },
                            label = "TabTitleAnimation"
                        ) { activeTab ->
                            Text(
                                text = when (activeTab) {
                                    0 -> LanguageManager.getString("app_title_calc", viewModel.selectedLanguage)
                                    1 -> LanguageManager.getString("app_title_conv", viewModel.selectedLanguage)
                                    2 -> LanguageManager.getString("app_title_tools", viewModel.selectedLanguage)
                                    3 -> LanguageManager.getString("app_title_history", viewModel.selectedLanguage)
                                    4 -> LanguageManager.getString("app_title_themes", viewModel.selectedLanguage)
                                    else -> LanguageManager.getString("app_title_calc", viewModel.selectedLanguage)
                                },
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { 
                                viewModel.showGlobalSearch = true 
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Global Search",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Language Switcher Dropdown Button
                        var isLangMenuExpanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(
                                onClick = { isLangMenuExpanded = true },
                                modifier = Modifier.testTag("language_selector_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = "Select Language",
                                    tint = Color.White
                                )
                            }

                            DropdownMenu(
                                expanded = isLangMenuExpanded,
                                onDismissRequest = { isLangMenuExpanded = false },
                                modifier = Modifier.background(themeColors.cardBg)
                            ) {
                                AppLanguage.values().forEach { lang ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = lang.flag, fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = lang.displayName,
                                                    color = if (viewModel.selectedLanguage == lang) themeColors.buttonEqualBg else themeColors.displayText,
                                                    fontWeight = if (viewModel.selectedLanguage == lang) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        },
                                        onClick = {
                                            viewModel.setLanguage(lang)
                                            isLangMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { viewModel.activeTab = 4 }) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Themes",
                                tint = Color.White
                            )
                        }

                        // Three-dot Menu (More Options)
                        var isMoreMenuExpanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { isMoreMenuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More Options",
                                    tint = Color.White
                                )
                            }
                            DropdownMenu(
                                expanded = isMoreMenuExpanded,
                                onDismissRequest = { isMoreMenuExpanded = false },
                                modifier = Modifier.background(themeColors.cardBg)
                            ) {
                                DropdownMenuItem(
                                    text = { Text(LanguageManager.getString("menu_settings", viewModel.selectedLanguage), color = themeColors.displayText) },
                                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = themeColors.buttonEqualBg) },
                                    onClick = {
                                        isMoreMenuExpanded = false
                                        showSettingsDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(LanguageManager.getString("menu_terms", viewModel.selectedLanguage), color = themeColors.displayText) },
                                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = themeColors.buttonEqualBg) },
                                    onClick = {
                                        isMoreMenuExpanded = false
                                        showTermsDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(LanguageManager.getString("menu_privacy", viewModel.selectedLanguage), color = themeColors.displayText) },
                                    leadingIcon = { Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = themeColors.buttonEqualBg) },
                                    onClick = {
                                        isMoreMenuExpanded = false
                                        showPrivacyDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(LanguageManager.getString("menu_about", viewModel.selectedLanguage), color = themeColors.displayText) },
                                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = themeColors.buttonEqualBg) },
                                    onClick = {
                                        isMoreMenuExpanded = false
                                        showAboutDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(themeColors.navBarBg)
                    .navigationBarsPadding(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .testTag("bottom_nav_bar"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tabs = listOf(
                        Triple(0, Icons.Default.Calculate, LanguageManager.getString("tab_calc", viewModel.selectedLanguage)),
                        Triple(1, ImageVector.vectorResource(id = R.drawable.ic_convert_tab), LanguageManager.getString("tab_conv", viewModel.selectedLanguage)),
                        Triple(2, Icons.Default.Widgets, LanguageManager.getString("tab_tools", viewModel.selectedLanguage)),
                        Triple(3, Icons.Default.History, LanguageManager.getString("tab_history", viewModel.selectedLanguage))
                    )
                    
                    // Left 2 tabs
                    for (i in 0..1) {
                        val (index, icon, label) = tabs[i]
                        val isSelected = viewModel.activeTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .testTag("tab_$label"),
                            contentAlignment = Alignment.Center
                        ) {
                            val tabInteraction = remember { MutableInteractionSource() }
                            Box(
                                modifier = Modifier
                                    .height(38.dp)
                                    .width(60.dp)
                                    .clip(RoundedCornerShape(19.dp))
                                    .background(if (isSelected) Color.White.copy(alpha = 0.25f) else Color.Transparent)
                                    .clickable(
                                        interactionSource = tabInteraction,
                                        indication = ripple(bounded = true, color = Color.White),
                                        onClick = { viewModel.activeTab = index }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon, 
                                    contentDescription = label,
                                    modifier = Modifier.size(24.dp),
                                    tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.65f)
                                ) 
                            }
                        }
                    }
                    
                    // Middle spacer for FAB room
                    Spacer(modifier = Modifier.weight(0.8f))
                    
                    // Right 2 tabs
                    for (i in 2..3) {
                        val (index, icon, label) = tabs[i]
                        val isSelected = viewModel.activeTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .testTag("tab_$label"),
                            contentAlignment = Alignment.Center
                        ) {
                            val tabInteraction = remember { MutableInteractionSource() }
                            Box(
                                modifier = Modifier
                                    .height(38.dp)
                                    .width(60.dp)
                                    .clip(RoundedCornerShape(19.dp))
                                    .background(if (isSelected) Color.White.copy(alpha = 0.25f) else Color.Transparent)
                                    .clickable(
                                        interactionSource = tabInteraction,
                                        indication = ripple(bounded = true, color = Color.White),
                                        onClick = { viewModel.activeTab = index }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon, 
                                    contentDescription = label,
                                    modifier = Modifier.size(24.dp),
                                    tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.65f)
                                ) 
                            }
                        }
                    }
                }
                
                // Floating Action Button in the center
                val fabInteractionSource = remember { MutableInteractionSource() }
                
                val infiniteTransition = rememberInfiniteTransition(label = "ai_gradient")
                
                val glowingColors1 = remember {
                    listOf(
                        Color(0xFFFF0000), // Red
                        Color(0xFFFFFF00), // Yellow
                        Color(0xFF00FF00), // Green
                        Color(0xFF00FFFF), // Cyan
                        Color(0xFF0000FF), // Blue
                        Color(0xFFFF00FF), // Magenta
                        Color(0xFFFF0000)  // Red (loop)
                    )
                }
                val glowingColors2 = remember {
                    listOf(
                        Color(0xFF00FFFF), // Cyan
                        Color(0xFF0000FF), // Blue
                        Color(0xFFFF00FF), // Magenta
                        Color(0xFFFF0000), // Red
                        Color(0xFFFFFF00), // Yellow
                        Color(0xFF00FF00), // Green
                        Color(0xFF00FFFF)  // Cyan (loop)
                    )
                }

                val duration = 12000

                val color1 by infiniteTransition.animateColor(
                    initialValue = glowingColors1.first(),
                    targetValue = glowingColors1.last(),
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = duration
                            glowingColors1.forEachIndexed { index, color ->
                                color at (duration * index / (glowingColors1.size - 1)) with LinearEasing
                            }
                        },
                        repeatMode = RepeatMode.Restart
                    ), label = "color1"
                )
                
                val color2 by infiniteTransition.animateColor(
                    initialValue = glowingColors2.first(),
                    targetValue = glowingColors2.last(),
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = duration
                            glowingColors2.forEachIndexed { index, color ->
                                color at (duration * index / (glowingColors2.size - 1)) with LinearEasing
                            }
                        },
                        repeatMode = RepeatMode.Restart
                    ), label = "color2"
                )
                
                val iconScale by infiniteTransition.animateFloat(
                    initialValue = 0.85f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "iconScale"
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-22).dp) // 72 (nav) + 10 (outside) - 60 (fab) = 22 (so 10dp sticks out)
                        .size(60.dp)
                        .shadow(elevation = 6.dp, shape = androidx.compose.foundation.shape.CircleShape)
                        .background(Color.White, androidx.compose.foundation.shape.CircleShape)
                        .padding(3.4.dp) // Border thickness reduced by another 5% (from 3.6dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .drawBehind {
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(color1, color2),
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, size.height)
                                ),
                                size = size
                            )
                            // Inner subtle glow/blur overlay to make it look smooth and blurry
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color.White.copy(alpha=0.3f), Color.Transparent),
                                    center = Offset(size.width / 2f, size.height / 2f),
                                    radius = size.width / 2f
                                )
                            )
                        }
                        .clickable(
                            interactionSource = fabInteractionSource,
                            indication = ripple(bounded = false),
                            onClick = {
                                try {
                                    if (!viewModel.showAiChat) {
                                        viewModel.showAiChat = true
                                    }
                                } catch (e: Throwable) {
                                    e.printStackTrace()
                                    viewModel.reportError("AI Chat FAB error: ${e.localizedMessage ?: e.javaClass.simpleName}")
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Assistant",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp).graphicsLayer(
                            scaleX = iconScale,
                            scaleY = iconScale
                        )
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(themeColors.background)
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            if (viewModel.activeTab == 4) {
                Box(modifier = Modifier.fillMaxSize()) {
                    ThemeSelectorScreen(viewModel, themeColors)
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 3,
                    userScrollEnabled = !viewModel.isDisplayInteractionActive
                ) { page ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (page) {
                            0 -> BasicScientificScreen(viewModel, themeColors)
                            1 -> UnitConverterScreen(viewModel, themeColors)
                            2 -> SpecialToolsScreen(viewModel, themeColors)
                            3 -> HistoryScreen(viewModel, themeColors)
                        }
                    }
                }
            }
        }

        // --- Settings Dialog ---
        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = {
                    Text(
                        text = LanguageManager.getString("menu_settings", viewModel.selectedLanguage),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = themeColors.displayText
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Language option
                        Column {
                            Text(
                                text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) "ভাষা / Language" else "Language / ভাষা",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = themeColors.displayText
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val languagesList = AppLanguage.values().toList().chunked(2)
                                for (pair in languagesList) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        for (lang in pair) {
                                            val isSelected = viewModel.selectedLanguage == lang
                                            Button(
                                                onClick = { viewModel.setLanguage(lang) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isSelected) themeColors.buttonEqualBg else themeColors.cardBg,
                                                    contentColor = if (isSelected) Color.White else themeColors.displayText
                                                ),
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.2f)) else null,
                                                contentPadding = PaddingValues(vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = "${lang.flag} ${lang.displayName}",
                                                    fontSize = 13.sp,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        if (pair.size < 2) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.1f))

                        // Vibration option
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = LanguageManager.getString("settings_vibration", viewModel.selectedLanguage),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = themeColors.displayText
                                )
                                Text(
                                    text = LanguageManager.getString("settings_vibration_desc", viewModel.selectedLanguage),
                                    fontSize = 11.sp,
                                    color = themeColors.displayText.copy(alpha = 0.6f)
                                )
                            }
                            Switch(
                                checked = viewModel.vibrationEnabled,
                                onCheckedChange = { viewModel.updateVibrationEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = themeColors.buttonEqualBg
                                )
                            )
                        }

                        HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.1f))

                        // Decimal Precision option
                        Column {
                            Text(
                                text = LanguageManager.getString("settings_precision", viewModel.selectedLanguage),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = themeColors.displayText
                            )
                            Text(
                                text = LanguageManager.getString("settings_precision_desc", viewModel.selectedLanguage),
                                fontSize = 11.sp,
                                color = themeColors.displayText.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(2, 3, 4, 5).forEach { precision ->
                                    val isSelected = viewModel.decimalPrecision == precision
                                    Button(
                                        onClick = { viewModel.updateDecimalPrecision(precision) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) themeColors.buttonEqualBg else themeColors.cardBg,
                                            contentColor = if (isSelected) Color.White else themeColors.displayText
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.2f)) else null
                                    ) {
                                        Text("$precision", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showSettingsDialog = false }
                    ) {
                        Text(
                            text = LanguageManager.getString("close", viewModel.selectedLanguage),
                            color = themeColors.buttonEqualBg,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                containerColor = themeColors.cardBg,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // --- Terms & Conditions Dialog ---
        if (showTermsDialog) {
            AlertDialog(
                onDismissRequest = { showTermsDialog = false },
                title = {
                    Text(
                        text = LanguageManager.getString("menu_terms", viewModel.selectedLanguage),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = themeColors.displayText
                    )
                },
                text = {
                    val termsText = if (viewModel.selectedLanguage == AppLanguage.BENGALI) {
                        "১. এই অ্যাপ্লিকেশনটি আপনার হিসাব-নিকাশ সহজ করার জন্য তৈরি করা হয়েছে।\n\n" +
                        "২. যদিও আমরা প্রতিটি বিষয়ের হিসাব শতভাগ সঠিক রাখার জন্য আন্তরিকভাবে চেষ্টা করেছি, তবে কোনো গাণিতিক ত্রুটি বা ফলাফলের ভুলের জন্য ডেভেলপার দায়ী থাকবে না।\n\n" +
                        "৩. এটি সম্পূর্ণরূপে একটি অফলাইন অ্যাপ্লিকেশন এবং কোনো প্রকার ব্যবহারকারীর ব্যক্তিগত বা সংবেদনশীল ডেটা সংগ্রহ করা হয় না।\n\n" +
                        "৪. অ্যাপ্লিকেশনটির যেকোনো ফিচার পরিবর্তন, পরিবর্ধন বা বাদ দেওয়ার অধিকার কর্তৃপক্ষের রয়েছে।"
                    } else {
                        "1. This application is developed to simplify your daily calculations and utilities.\n\n" +
                        "2. While we strive to ensure 100% accuracy in all calculator functions, the developers are not liable for any financial or calculations errors.\n\n" +
                        "3. This is entirely an offline application, and no user-sensitive or personal data is collected or shared.\n\n" +
                        "4. The developers reserve the right to modify, update, or discontinue features of this application at any time."
                    }
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
                    ) {
                        item {
                            Text(
                                text = termsText,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = themeColors.displayText.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showTermsDialog = false }
                    ) {
                        Text(
                            text = LanguageManager.getString("close", viewModel.selectedLanguage),
                            color = themeColors.buttonEqualBg,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                containerColor = themeColors.cardBg,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // --- Privacy Policy Dialog ---
        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                title = {
                    Text(
                        text = LanguageManager.getString("menu_privacy", viewModel.selectedLanguage),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = themeColors.displayText
                    )
                },
                text = {
                    val privacyText = if (viewModel.selectedLanguage == AppLanguage.BENGALI) {
                        "১. আমাদের মূল লক্ষ্য হলো আপনার তথ্যের সর্বোচ্চ নিরাপত্তা বজায় রাখা।\n\n" +
                        "২. এই অ্যাপটি সম্পূর্ণ অফলাইন-ভিত্তিক। এর অর্থ হলো আপনার টাইপ করা কোনো এক্সপ্রেশন, গণনা বা ইতিহাস আমাদের কাছে বা অন্য কোনো সার্ভারে প্রেরণ করা হয় না।\n\n" +
                        "৩. হিস্ট্রি ডাটা আপনার ডিভাইসের লোকাল ডাটাবেজে (Room Database) সম্পূর্ণ সুরক্ষিতভাবে জমা থাকে। আপনি চাইলে যেকোনো মুহূর্তে হিস্ট্রি মুছে দিতে পারেন।\n\n" +
                        "৪. আমরা কোনো ব্যবহারকারীর অবস্থান বা ব্যক্তিগত ডেটা ট্র্যাকিং করি না।"
                    } else {
                        "1. Our primary goal is to ensure the absolute privacy and security of your data.\n\n" +
                        "2. This application operates entirely offline. This means none of your typed expressions, utility parameters, or history results are sent to any external server.\n\n" +
                        "3. All history logs are securely saved in your local Room Database on your device. You can clear them at any time.\n\n" +
                        "4. We do not track user location, nor do we collect any personally identifiable information."
                    }
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
                    ) {
                        item {
                            Text(
                                text = privacyText,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = themeColors.displayText.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showPrivacyDialog = false }
                    ) {
                        Text(
                            text = LanguageManager.getString("close", viewModel.selectedLanguage),
                            color = themeColors.buttonEqualBg,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                containerColor = themeColors.cardBg,
                shape = RoundedCornerShape(16.dp)
            )
        }

        val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

        // --- Online AI Model Error / Offline Fallback Dialog ---
        if (viewModel.onlineModelErrorReason != null) {
            AlertDialog(
                onDismissRequest = { viewModel.onlineModelErrorReason = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "এআই মডেল লোড স্ট্যাটাস" else "AI Model Load Status",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = themeColors.displayText
                        )
                    }
                },
                text = {
                    Text(
                        text = viewModel.onlineModelErrorReason ?: "",
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = themeColors.displayText.copy(alpha = 0.9f)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.onlineModelErrorReason = null }
                    ) {
                        Text(
                            text = if (isBn) "ঠিক আছে" else "OK",
                            color = themeColors.buttonEqualBg,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                containerColor = themeColors.cardBg,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // --- About App Dialog ---
        if (showAboutDialog) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val realVersionName = remember {
                try {
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
                } catch (e: Exception) {
                    "1.0.0"
                }
            }
            val realAppName = remember {
                try {
                    context.getString(com.example.R.string.app_name)
                } catch (e: Exception) {
                    "Smart Calculator Pro"
                }
            }

            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = {
                    Text(
                        text = LanguageManager.getString("menu_about", viewModel.selectedLanguage),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = themeColors.displayText
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(themeColors.buttonEqualBg, shape = RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.app_logo),
                                contentDescription = null,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = realAppName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = themeColors.displayText,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = if (isBn) "ভার্সন $realVersionName" else "Version $realVersionName",
                            fontSize = 12.sp,
                            color = themeColors.displayText.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val descText = if (isBn) {
                            "এটি একটি বহুমুখী এবং আধুনিক গণনা সমাধান। এতে রয়েছে একটি বৈজ্ঞানিক ক্যালকুলেটর, বিভিন্ন ইউনিট কনভার্টার, দৈনন্দিন জীবনের সবরকম জরুরি হিসাব সম্পন্ন করার বিশেষ বিশেষ টুলস এবং একটি স্মার্ট অফলাইন এআই সহকারী।"
                        } else {
                            "A versatile and modern multi-tool solution. Features a comprehensive scientific calculator, robust unit converters, utility tools, and a smart offline AI assistant."
                        }
                        Text(
                            text = descText,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = themeColors.displayText.copy(alpha = 0.8f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.12f))
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Developer Info Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (isBn) "👨‍💻 ডেভেলপার তথ্য" else "👨‍💻 Developer Info",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = themeColors.buttonEqualBg
                                )
                                Text(
                                    text = if (isBn) "ডেভেলপার: Md. Shariful Islam" else "Developer: Md. Shariful Islam",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = themeColors.displayText
                                )
                                Text(
                                    text = "📧 Email: Connect.shariful@gmail.com",
                                    fontSize = 12.sp,
                                    color = themeColors.displayText.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = "🌐 FB: fb.com/shariful.uxd",
                                    fontSize = 12.sp,
                                    color = themeColors.displayText.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = "💬 WhatsApp: +8801768899599",
                                    fontSize = 12.sp,
                                    color = themeColors.displayText.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showAboutDialog = false }
                    ) {
                        Text(
                            text = LanguageManager.getString("close", viewModel.selectedLanguage),
                            color = themeColors.buttonEqualBg,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                containerColor = themeColors.cardBg,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }

    // --- Offline AI Chat Overlay ---
    if (viewModel.showAiChat) {
        Box(modifier = Modifier.fillMaxSize()) {
            androidx.activity.compose.BackHandler(enabled = viewModel.showAiChat) {
                viewModel.showAiChat = false
            }
            AiChatDialog(
                viewModel = viewModel,
                themeColors = themeColors,
                onDismiss = { viewModel.showAiChat = false }
            )
        }
    }

    if (viewModel.showErrorDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showErrorDialog = false },
            title = {
                Text(text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) "⚠️ এরর মেসেজ (Error Details)" else "⚠️ Error Details")
            },
            text = {
                Text(
                    text = viewModel.currentErrorMessage ?: "Unknown error",
                    color = themeColors.displayText,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.showErrorDialog = false }) {
                    Text(
                        text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) "ঠিক আছে (OK)" else "OK",
                        color = themeColors.buttonEqualBg,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (viewModel.showSaveDialog) {
        val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
        AlertDialog(
            onDismissRequest = { 
                viewModel.showSaveDialog = false
                viewModel.saveNameInput = ""
            },
            title = {
                Text(
                    text = if (isBn) "ক্যালকুলেশন সংরক্ষণ করুন" else "Save Calculation",
                    color = themeColors.displayText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (isBn) "এই ক্যালকুলেশনটি একটি নাম দিয়ে সংরক্ষণ করুন:" else "Save this calculation with a descriptive name:",
                        color = themeColors.displayText.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = viewModel.saveNameInput,
                        onValueChange = { viewModel.saveNameInput = it },
                        placeholder = {
                            Text(
                                text = if (isBn) "যেমন: বাড়ির বাজেট" else "e.g., Home Budget",
                                color = themeColors.displayText.copy(alpha = 0.4f),
                                fontSize = 14.sp
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = themeColors.displayText,
                            unfocusedTextColor = themeColors.displayText,
                            focusedBorderColor = themeColors.buttonEqualBg,
                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.3f),
                            focusedLabelColor = themeColors.buttonEqualBg,
                            cursorColor = themeColors.buttonEqualBg
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = viewModel.saveNameInput.trim()
                        if (name.isNotEmpty()) {
                            viewModel.saveNamedCalculation(name)
                            viewModel.showSaveDialog = false
                            viewModel.saveNameInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
                ) {
                    Text(
                        text = if (isBn) "সংরক্ষণ করুন" else "Save",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.showSaveDialog = false
                        viewModel.saveNameInput = ""
                    }
                ) {
                    Text(
                        text = if (isBn) "বাতিল" else "Cancel",
                        color = themeColors.displayText.copy(alpha = 0.7f)
                    )
                }
            },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    GlobalSearchDialog(viewModel, themeColors)
}
}
}

@Composable
fun AiChatDialog(
    viewModel: com.example.ui.viewmodel.CalculatorViewModel,
    themeColors: com.example.ui.theme.CalculatorThemeColors,
    onDismiss: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    
    var isInitiallyLoading by remember { mutableStateOf(true) }
    var showOnlineErrorBanner by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000L)
        isInitiallyLoading = false
    }

    LaunchedEffect(viewModel.lastOnlineError) {
        if (viewModel.lastOnlineError != null) {
            showOnlineErrorBanner = true
            kotlinx.coroutines.delay(5000L)
            showOnlineErrorBanner = false
        } else {
            showOnlineErrorBanner = false
        }
    }
    
    val speechRecognizerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            val matches = data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                val recognizedText = matches[0]
                viewModel.sendMessageToAi(recognizedText)
            }
        }
    }
    
    // Auto scroll to bottom when a new message is received
    LaunchedEffect(viewModel.aiChatMessages.size) {
        try {
            if (viewModel.aiChatMessages.isNotEmpty()) {
                lazyListState.animateScrollToItem(viewModel.aiChatMessages.size - 1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(themeColors.titleBarBg)
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI) "এআই এ্যাসিস্ট্যান্ট" else "AI Assistant",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val isOnline = viewModel.isNetworkAvailable() && com.example.BuildConfig.GEMINI_API_KEY.isNotBlank() && com.example.BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" && viewModel.lastOnlineError == null
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(if (isOnline) Color(0xFF4CAF50) else Color(0xFFFFC107))
                                    .border(1.dp, Color.White, androidx.compose.foundation.shape.CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val isBn = viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI
                            val statusText = if (isOnline) {
                                if (isBn) "অনলাইন মডেল" else "Online Model"
                            } else {
                                if (isBn) "অফলাইন মডেল" else "Offline Model"
                            }
                            Text(
                                text = statusText,
                                fontSize = 9.5.sp,
                                color = Color.White.copy(alpha = 0.95f),
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }
                    
                    // New Chat Button
                    IconButton(
                        onClick = { viewModel.resetAiChat() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddComment,
                            contentDescription = "New Chat",
                            tint = Color.White
                        )
                    }

                    // History Button
                    IconButton(
                        onClick = { viewModel.showChatHistory = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Chat History",
                            tint = Color.White
                        )
                    }
                }
                
                // Warning Banner
                if (showOnlineErrorBanner && viewModel.lastOnlineError != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF3CD)) // Warm warning yellow background
                            .border(1.dp, Color(0xFFFFEBA5))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = Color(0xFF856404),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = viewModel.lastOnlineError ?: "",
                                color = Color(0xFF856404),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                        IconButton(
                            onClick = { showOnlineErrorBanner = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Banner",
                                tint = Color(0xFF856404),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                
                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Transparent)
                )
                
                // Chat list
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(viewModel.aiChatMessages) { msg ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (msg.isUser) 16.dp else 4.dp,
                                            bottomEnd = if (msg.isUser) 4.dp else 16.dp
                                        )
                                    )
                                    .background(
                                        if (msg.isUser) themeColors.buttonEqualBg.copy(alpha = 0.2f) else themeColors.cardBg
                                    )
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .widthIn(max = 280.dp)
                            ) {
                                Column {
                                    Text(
                                        text = msg.text,
                                        color = themeColors.displayText,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    )
                                    
                                    if (msg.actionType != null && msg.actionData != null) {
                                        Button(
                                            onClick = {
                                                viewModel.performAiChatAction(msg.actionType, msg.actionData)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (msg.isUser) themeColors.buttonEqualBg.copy(alpha = 0.3f) else themeColors.buttonFunctionBg
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier
                                                .padding(top = 8.dp)
                                                .height(34.dp)
                                        ) {
                                            Text(
                                                text = msg.actionLabel ?: "Execute",
                                                color = if (msg.isUser) themeColors.displayText else themeColors.buttonFunctionText,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.ArrowForward,
                                                contentDescription = null,
                                                tint = if (msg.isUser) themeColors.displayText else themeColors.buttonFunctionText,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Time
                            Text(
                                text = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(msg.timestamp)),
                                fontSize = 9.sp,
                                color = themeColors.displayExpressionText,
                                modifier = Modifier.padding(top = 3.dp, start = 4.dp, end = 4.dp)
                            )
                        }
                    }
                    if (viewModel.isAiLoading) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            ) {
                                AiTypingIndicator()
                            }
                        }
                    }
                }
                
                // Suggestions
                val suggestions = if (viewModel.selectedLanguage == AppLanguage.BENGALI) {
                    listOf("৫ কিমি = কত মিটার?", "১০০ ডলার কত টাকা?", "বিএমআই হিসেব করো")
                } else {
                    listOf("5 km to meters", "100 usd to bdt", "Calculate BMI")
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestions.forEach { sugg ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(themeColors.cardBg)
                                .border(
                                    width = 1.dp,
                                    color = themeColors.buttonNormalBg,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    viewModel.sendMessageToAi(sugg)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = sugg,
                                color = themeColors.displayText,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                
                // Input bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(themeColors.background)
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = {
                            Text(
                                text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) "এখানে লিখুন..." else "Type a message...",
                                color = themeColors.displayExpressionText,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp, max = 120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = themeColors.displayText,
                            unfocusedTextColor = themeColors.displayText,
                            focusedBorderColor = themeColors.buttonEqualBg,
                            unfocusedBorderColor = themeColors.displayExpressionText,
                            focusedContainerColor = themeColors.cardBg,
                            unfocusedContainerColor = themeColors.cardBg
                        ),
                        shape = RoundedCornerShape(24.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (textInput.isNotBlank()) {
                                    viewModel.sendMessageToAi(textInput)
                                    textInput = ""
                                }
                            }
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(if (textInput.isNotBlank()) themeColors.buttonEqualBg else themeColors.buttonEqualBg.copy(alpha = 0.85f))
                            .clickable {
                                if (textInput.isNotBlank()) {
                                    viewModel.sendMessageToAi(textInput)
                                    textInput = ""
                                } else {
                                    val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, if (viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI) "bn-BD" else "en-US")
                                    }
                                    try {
                                        speechRecognizerLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (textInput.isNotBlank()) Icons.Default.Send else Icons.Default.Mic,
                            contentDescription = if (textInput.isNotBlank()) "Send" else "Voice Input",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (viewModel.showClearChatDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.showClearChatDialog = false },
                title = {
                    Text(text = if (viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI) "চ্যাট ক্লিয়ার করুন" else "Clear Chat")
                },
                text = {
                    Text(text = if (viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI) "আপনি কি নিশ্চিত যে আপনি সমস্ত চ্যাট হিস্টোরি ক্লিয়ার করতে চান?" else "Are you sure you want to clear all chat history?")
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetAiChat() }) {
                        Text(text = if (viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI) "হ্যাঁ" else "Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showClearChatDialog = false }) {
                        Text(text = if (viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI) "না" else "No")
                    }
                }
            )
        }
        ChatHistoryDialog(viewModel, themeColors)

         // Loading Overlay
        if (isInitiallyLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(themeColors.background),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.app_logo),
                        contentDescription = "Loading Logo",
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    CircularProgressIndicator(
                        color = themeColors.buttonEqualBg,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI) "এআই অ্যাসিস্ট্যান্ট লোড হচ্ছে..." else "Loading AI Assistant...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = themeColors.displayText.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatHistoryDialog(viewModel: com.example.ui.viewmodel.CalculatorViewModel, themeColors: com.example.ui.theme.CalculatorThemeColors) {
    if (viewModel.showChatHistory) {
        val isBn = viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI
        AlertDialog(
            onDismissRequest = { viewModel.showChatHistory = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (viewModel.isChatSelectionMode) {
                            if (isBn) "${viewModel.selectedChatSessionIds.size} টি সিলেক্টেড" else "${viewModel.selectedChatSessionIds.size} Selected"
                        } else {
                            if (isBn) "চ্যাট হিস্টোরি" else "Chat History"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = themeColors.displayText
                    )
                    if (viewModel.isChatSelectionMode) {
                        IconButton(onClick = { viewModel.showDeleteSelectedChatSessionsDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Selected",
                                tint = Color.Red
                            )
                        }
                    }
                }
            },
            text = {
                if (viewModel.chatSessions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBn) "কোনো চ্যাট হিস্টোরি পাওয়া যায়নি।" else "No chat history found.",
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(viewModel.chatSessions) { session ->
                            val isSelected = viewModel.selectedChatSessionIds.contains(session.id)
                            val sessionInteractionSource = remember { MutableInteractionSource() }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .scaleOnPress(sessionInteractionSource)
                                    .combinedClickable(
                                        interactionSource = sessionInteractionSource,
                                        indication = ripple(bounded = true),
                                        onClick = {
                                            if (viewModel.isChatSelectionMode) {
                                                viewModel.toggleChatSelection(session.id)
                                            } else {
                                                viewModel.loadChatSession(session)
                                            }
                                        },
                                        onLongClick = {
                                            if (!viewModel.isChatSelectionMode) {
                                                viewModel.isChatSelectionMode = true
                                                viewModel.toggleChatSelection(session.id)
                                            }
                                        }
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) themeColors.buttonEqualBg.copy(alpha = 0.15f) else themeColors.cardBg
                                ),
                                border = if (isSelected) BorderStroke(1.dp, themeColors.buttonEqualBg) else null,
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (viewModel.isChatSelectionMode) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { viewModel.toggleChatSelection(session.id) },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = themeColors.buttonEqualBg
                                            ),
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    }
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = session.title,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontSize = 15.sp,
                                            color = themeColors.displayText
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(session.timestamp)),
                                            fontSize = 11.sp,
                                            color = themeColors.displayExpressionText
                                        )
                                    }
                                    
                                    if (!viewModel.isChatSelectionMode) {
                                        IconButton(
                                            onClick = { 
                                                viewModel.sessionToDelete = session
                                                viewModel.showDeleteChatSessionDialog = true
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Delete",
                                                tint = Color.Red.copy(alpha = 0.7f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.showChatHistory = false 
                    viewModel.isChatSelectionMode = false
                    viewModel.selectedChatSessionIds = emptySet()
                }) {
                    Text(
                        text = if (isBn) "বন্ধ করুন" else "Close",
                        color = themeColors.buttonEqualBg,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = themeColors.background
        )

        // Confirmation Dialogs
        if (viewModel.showDeleteChatSessionDialog && viewModel.sessionToDelete != null) {
            AlertDialog(
                onDismissRequest = { viewModel.showDeleteChatSessionDialog = false },
                title = { Text(if (isBn) "চ্যাট ডিলিট করবেন?" else "Delete Chat?", color = themeColors.displayText) },
                text = { Text(if (isBn) "আপনি কি নিশ্চিত যে আপনি এই চ্যাটটি ডিলিট করতে চান?" else "Are you sure you want to delete this chat?", color = themeColors.displayText) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteChatSession(viewModel.sessionToDelete!!)
                        viewModel.showDeleteChatSessionDialog = false
                        viewModel.sessionToDelete = null
                    }) {
                        Text(if (isBn) "হ্যাঁ" else "Yes", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showDeleteChatSessionDialog = false }) {
                        Text(if (isBn) "না" else "No", color = themeColors.buttonEqualBg)
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = themeColors.background
            )
        }

        if (viewModel.showDeleteSelectedChatSessionsDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.showDeleteSelectedChatSessionsDialog = false },
                title = { Text(if (isBn) "সিলেক্টেড চ্যাট ডিলিট করবেন?" else "Delete Selected?", color = themeColors.displayText) },
                text = { Text(if (isBn) "আপনি কি নিশ্চিত যে আপনি সিলেক্টেড চ্যাটগুলো ডিলিট করতে চান?" else "Are you sure you want to delete selected chats?", color = themeColors.displayText) },
                confirmButton = {
                    TextButton(onClick = { viewModel.deleteSelectedChatSessions() }) {
                        Text(if (isBn) "হ্যাঁ" else "Yes", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showDeleteSelectedChatSessionsDialog = false }) {
                        Text(if (isBn) "না" else "No", color = themeColors.buttonEqualBg)
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = themeColors.background
            )
        }
    }
}

@Composable
fun AiTypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes { durationMillis = 1000; 0.5f at 500 },
            repeatMode = RepeatMode.Reverse
        )
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes { durationMillis = 1000; 0f at 200; 0.5f at 700 },
            repeatMode = RepeatMode.Reverse
        )
    )
    val alpha3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes { durationMillis = 1000; 0f at 400; 0.5f at 900 },
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
            .background(Color.White)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(6.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFF0D47A1).copy(alpha = alpha1)))
        Box(modifier = Modifier.size(6.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFF0D47A1).copy(alpha = alpha2)))
        Box(modifier = Modifier.size(6.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFF0D47A1).copy(alpha = alpha3)))
    }
}

@Composable
fun SplashScreen(themeColors: com.example.ui.theme.CalculatorThemeColors) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.app_logo),
            contentDescription = "Logo",
            modifier = Modifier.size(160.dp)
        )
    }
}
