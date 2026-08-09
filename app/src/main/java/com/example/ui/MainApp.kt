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
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import com.example.ui.screens.*
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel

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

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(themeColors.buttonEqualBg)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when (viewModel.activeTab) {
                            0 -> "Smart Calculator"
                            1 -> "Unit Converter"
                            2 -> "Special Tools"
                            3 -> "History"
                            4 -> "Themes"
                            else -> "Smart Calculator"
                        },
                        color = Color.White,
                        fontSize = 19.sp,
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
                        .height(88.dp)
                        .testTag("bottom_nav_bar")
                ) {
                    val tabs = listOf(
                        Triple(0, Icons.Default.Calculate, "Calc"),
                        Triple(1, ImageVector.vectorResource(id = R.drawable.ic_convert_tab), "Conv"),
                        Triple(2, Icons.Default.Widgets, "Tools"),
                        Triple(3, Icons.Default.History, "History")
                    )
                    
                    tabs.forEach { (index, icon, label) ->
                        NavigationBarItem(
                            selected = viewModel.activeTab == index,
                            onClick = { viewModel.activeTab = index },
                            icon = { 
                                Icon(
                                    imageVector = if (icon is ImageVector) icon else icon as ImageVector, 
                                    contentDescription = label,
                                    modifier = Modifier.size(32.dp)
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
