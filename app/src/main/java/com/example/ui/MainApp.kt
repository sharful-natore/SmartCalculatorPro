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
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                indicatorColor = Color.White.copy(alpha = 0.22f),
                                unselectedIconColor = Color.White.copy(alpha = 0.6f)
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AppLanguage.values().forEach { lang ->
                                    val isSelected = viewModel.selectedLanguage == lang
                                    Button(
                                        onClick = { viewModel.setLanguage(lang) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) themeColors.buttonEqualBg else themeColors.cardBg,
                                            contentColor = if (isSelected) Color.White else themeColors.displayText
                                        ),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.2f)) else null
                                    ) {
                                        Text("${lang.flag} ${lang.displayName}", fontSize = 13.sp)
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

        // --- About App Dialog ---
        if (showAboutDialog) {
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(themeColors.buttonEqualBg, shape = RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) "স্মার্ট ইউটিলিটি ও ক্যালকুলেটর" else "Smart Utility & Calculator",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = themeColors.displayText
                        )
                        Text(
                            text = "Version 1.2.0",
                            fontSize = 12.sp,
                            color = themeColors.displayText.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        val descText = if (viewModel.selectedLanguage == AppLanguage.BENGALI) {
                            "এটি একটি বহুমুখী এবং আধুনিক গণনা সমাধান। এতে রয়েছে একটি বৈজ্ঞানিক ক্যালকুলেটর, বিভিন্ন ইউনিট কনভার্টার এবং দৈনন্দিন জীবনের সবরকম জরুরি হিসাব সম্পন্ন করার বিশেষ বিশেষ টুলস।"
                        } else {
                            "A versatile and modern multi-tool solution. Features a comprehensive scientific calculator, robust unit converters, and high-quality utility tools for all daily computation needs."
                        }
                        Text(
                            text = descText,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = themeColors.displayText.copy(alpha = 0.8f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
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
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
