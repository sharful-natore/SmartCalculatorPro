package com.example.ui

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.screens.*
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel

@Composable
fun MainApp(viewModel: CalculatorViewModel) {
    val themeColors = viewModel.currentThemeType.getColors()
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = viewModel.activeTab) { 5 }

    // Sync from pager state to ViewModel
    LaunchedEffect(pagerState.currentPage) {
        viewModel.activeTab = pagerState.currentPage
    }

    // Sync from ViewModel to pager state
    LaunchedEffect(viewModel.activeTab) {
        if (pagerState.currentPage != viewModel.activeTab) {
            pagerState.animateScrollToPage(viewModel.activeTab)
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF6200EE))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when (viewModel.activeTab) {
                            0 -> "স্মার্ট ক্যালকুলেটর"
                            1 -> "ইউনিট কনভার্টার"
                            2 -> "স্পেশাল টুলস"
                            3 -> "ইতিহাস"
                            4 -> "থিমস"
                            else -> "স্মার্ট ক্যালকুলেটর"
                        },
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
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
                        if (viewModel.activeTab == 3) {
                            IconButton(onClick = { viewModel.clearAllHistory() }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Clear History",
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
                    .background(Color(0xFF6200EE))
                    .navigationBarsPadding()
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = viewModel.activeTab == 0,
                        onClick = { viewModel.activeTab = 0 },
                        icon = { Icon(Icons.Default.Calculate, contentDescription = "Calculator") },
                        label = { Text("Calc") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = Color.White.copy(alpha = 0.25f),
                            unselectedIconColor = Color.White.copy(alpha = 0.65f),
                            unselectedTextColor = Color.White.copy(alpha = 0.65f)
                        ),
                        modifier = Modifier.testTag("tab_calculator")
                    )
                    NavigationBarItem(
                        selected = viewModel.activeTab == 1,
                        onClick = { viewModel.activeTab = 1 },
                        icon = { Icon(Icons.Default.SwapVert, contentDescription = "Converter") },
                        label = { Text("Convert") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = Color.White.copy(alpha = 0.25f),
                            unselectedIconColor = Color.White.copy(alpha = 0.65f),
                            unselectedTextColor = Color.White.copy(alpha = 0.65f)
                        ),
                        modifier = Modifier.testTag("tab_converter")
                    )
                    NavigationBarItem(
                        selected = viewModel.activeTab == 2,
                        onClick = { viewModel.activeTab = 2 },
                        icon = { Icon(Icons.Default.Widgets, contentDescription = "Tools") },
                        label = { Text("Tools") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = Color.White.copy(alpha = 0.25f),
                            unselectedIconColor = Color.White.copy(alpha = 0.65f),
                            unselectedTextColor = Color.White.copy(alpha = 0.65f)
                        ),
                        modifier = Modifier.testTag("tab_tools")
                    )
                    NavigationBarItem(
                        selected = viewModel.activeTab == 3,
                        onClick = { viewModel.activeTab = 3 },
                        icon = { Icon(Icons.Default.History, contentDescription = "History") },
                        label = { Text("History") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = Color.White.copy(alpha = 0.25f),
                            unselectedIconColor = Color.White.copy(alpha = 0.65f),
                            unselectedTextColor = Color.White.copy(alpha = 0.65f)
                        ),
                        modifier = Modifier.testTag("tab_history")
                    )
                }
            }
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(themeColors.background)
                .padding(innerPadding)
                .imePadding()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true
            ) { page ->
                Box(modifier = Modifier.fillMaxSize()) {
                    when (page) {
                        0 -> BasicScientificScreen(viewModel, themeColors)
                        1 -> UnitConverterScreen(viewModel, themeColors)
                        2 -> SpecialToolsScreen(viewModel, themeColors)
                        3 -> HistoryScreen(viewModel, themeColors)
                        4 -> ThemeSelectorScreen(viewModel, themeColors)
                    }
                }
            }
        }
    }
}

