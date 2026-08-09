package com.example.ui

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
import androidx.compose.ui.draw.clip
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
import com.example.ui.screens.*
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import com.example.util.LanguageManager

@Composable
fun MainApp(viewModel: CalculatorViewModel) {
    val themeColors = viewModel.currentThemeType.getColors()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

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
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = viewModel.activeTab.coerceAtMost(3)) { 4 }

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
                    Text(
                        text = when (viewModel.activeTab) {
                            0 -> LanguageManager.getString("app_title_calc", viewModel.selectedLanguage)
                            1 -> LanguageManager.getString("app_title_conv", viewModel.selectedLanguage)
                            2 -> LanguageManager.getString("app_title_tools", viewModel.selectedLanguage)
                            3 -> LanguageManager.getString("app_title_history", viewModel.selectedLanguage)
                            4 -> LanguageManager.getString("app_title_themes", viewModel.selectedLanguage)
                            else -> LanguageManager.getString("app_title_calc", viewModel.selectedLanguage)
                        },
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.animateContentSize()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (viewModel.activeTab == 0) {
                            IconButton(onClick = { viewModel.isScientificExpanded = !viewModel.isScientificExpanded }) {
                                Icon(
                                    imageVector = if (viewModel.isScientificExpanded) Icons.Default.Science else Icons.Default.Calculate,
                                    contentDescription = "Toggle Scientific",
                                    tint = Color.White
                                )
                            }
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
                                                    color = if (viewModel.selectedLanguage == lang) Color(0xFF6366F1) else themeColors.displayText,
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
                    }
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(themeColors.buttonEqualBg)
                    .navigationBarsPadding()
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("bottom_nav_bar")
                ) {
                    val tabs = listOf(
                        Triple(0, Icons.Default.Calculate, LanguageManager.getString("tab_calc", viewModel.selectedLanguage)),
                        Triple(1, ImageVector.vectorResource(id = R.drawable.ic_convert_tab), LanguageManager.getString("tab_conv", viewModel.selectedLanguage)),
                        Triple(2, Icons.Default.Widgets, LanguageManager.getString("tab_tools", viewModel.selectedLanguage)),
                        Triple(3, Icons.Default.History, LanguageManager.getString("tab_history", viewModel.selectedLanguage))
                    )
                    
                    tabs.forEach { (index, icon, label) ->
                        NavigationBarItem(
                            selected = viewModel.activeTab == index,
                            onClick = { viewModel.activeTab = index },
                            icon = { 
                                Icon(
                                    imageVector = if (icon is ImageVector) icon else icon as ImageVector, 
                                    contentDescription = label,
                                    modifier = Modifier.size(24.dp)
                                ) 
                            },
                            label = null,
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = themeColors.buttonEqualBg,
                                indicatorColor = Color.White,
                                unselectedIconColor = Color.White.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier.testTag("tab_$label")
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.systemBars,
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
                ThemeSelectorScreen(viewModel, themeColors)
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 3,
                    userScrollEnabled = true
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
    }
}
