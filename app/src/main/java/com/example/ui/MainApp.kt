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
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.example.data.model.ToolType
import com.example.data.model.ConverterType
import com.example.ui.quran.QuranMiniPlayerBanner
import com.example.data.quran.QuranAudioPlayer
import androidx.compose.foundation.Canvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.scale

import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.drawWithContent
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import com.example.ui.screens.*
import com.example.ui.components.GlobalSearchDialog
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import com.example.util.LanguageManager
import com.example.util.UpdateManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File

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

    var pendingCrashReport by remember { mutableStateOf<com.example.util.CrashReport?>(null) }
    var showCrashReportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val report = com.example.util.CrashReporter.getPendingCrashReport(context)
            if (report != null) {
                pendingCrashReport = report
                showCrashReportDialog = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    val coroutineScope = rememberCoroutineScope()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showGlobalBackupDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showStartButtonCustomizer by remember { mutableStateOf(false) }
    var showAiFabCustomizer by remember { mutableStateOf(false) }
    var showCenterSearchFabCustomizer by remember { mutableStateOf(false) }
    var showVisualThemesDialog by remember { mutableStateOf(false) }

    val globalBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.backupHistoryToUri(it) }
    }

    val globalRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.restoreHistoryFromUri(it) }
    }

    // Quick Shortcut Windows Controls State
    var isCalcMinimized by remember { mutableStateOf(false) }
    var isCalcMaximized by remember { mutableStateOf(false) }
    var showQuickCalcCloseConfirm by remember { mutableStateOf(false) }

    var isCalMinimized by remember { mutableStateOf(false) }
    var isMarketMinimized by remember { mutableStateOf(false) }

    // --- App Update State ---
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateLatestVersion by remember { mutableStateOf("") }
    var updateChangeLog by remember { mutableStateOf("") }
    var updateDownloadUrl by remember { mutableStateOf("") }
    var isCheckingForUpdate by remember { mutableStateOf(false) }
    var isDownloadingUpdate by remember { mutableStateOf(false) }
    var updateDownloadProgress by remember { mutableStateOf(0) }
    var updateErrorMessage by remember { mutableStateOf<String?>(null) }
    var isUpdateFailed by remember { mutableStateOf(false) }
    var downloadedApkFile by remember { mutableStateOf<File?>(null) }

    val performUpdateCheck: (Boolean) -> Unit = remember(context, viewModel.selectedLanguage) {
        { isManual ->
            if (isManual) {
                isCheckingForUpdate = true
                updateErrorMessage = null
                isUpdateFailed = false
            }
            UpdateManager.checkForUpdates(
                context = context,
                onUpdateAvailable = { latestVersion, changeLog, downloadUrl ->
                    isCheckingForUpdate = false
                    updateLatestVersion = latestVersion
                    updateChangeLog = changeLog
                    updateDownloadUrl = downloadUrl
                    showUpdateDialog = true
                },
                onNoUpdate = {
                    isCheckingForUpdate = false
                    if (isManual) {
                        Toast.makeText(
                            context,
                            LanguageManager.getString("update_no_update", viewModel.selectedLanguage),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onError = { err ->
                    isCheckingForUpdate = false
                    if (isManual) {
                        val errMsg = if (err.message?.contains("Firebase is not initialized") == true) {
                            if (viewModel.selectedLanguage == AppLanguage.BENGALI) 
                                "ফায়ারবেস কনফিগারেশন সেটআপ করা নেই।" 
                            else 
                                "Firebase configuration is not set up."
                        } else {
                            "${LanguageManager.getString("update_failed", viewModel.selectedLanguage)}: ${err.message}"
                        }
                        Toast.makeText(
                            context,
                            errMsg,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
        }
    }

    // Ensure initial launch strictly defaults to Index 0 (Dashboard)
    LaunchedEffect(Unit) {
        viewModel.changeActiveTab(0, "App Startup -> Dashboard", "অ্যাপ স্টার্টআপ -> ড্যাশবোর্ড")
        delay(400) // Let UI settle before update check
        performUpdateCheck(false)
    }



    // Handle Back Press
    val activity = context as? Activity
    BackHandler(enabled = true) {
        when {
            // If Quick calculator window or sub-dialog is open, close them
            viewModel.showCalendarDialog -> {
                viewModel.showCalendarDialog = false
            }
            viewModel.showMarketDialog -> {
                viewModel.showMarketDialog = false
            }
            viewModel.showCalculatorDialog -> {
                viewModel.showCalculatorDialog = false
            }
            // If Global Search Dialog is open
            viewModel.showGlobalSearch -> {
                viewModel.showGlobalSearch = false
            }
            // If AI Chat is showing, close it first
            viewModel.showAiChat -> {
                viewModel.showAiChat = false
            }
            // If inside a specific converter detail screen
            viewModel.activeTab == 1 && viewModel.selectedConverterType != null -> {
                viewModel.closeConverterDetail()
            }
            // If on Converter tab and search query is not empty
            viewModel.activeTab == 1 && viewModel.converterSearchQuery.isNotEmpty() -> {
                viewModel.converterSearchQuery = ""
                focusManager.clearFocus()
            }
            // If on Converter tab and category filter is selected
            viewModel.activeTab == 1 && viewModel.selectedCategoryFilter != null -> {
                viewModel.selectedCategoryFilter = null
            }
            // If inside a specific tool detail screen
            viewModel.activeTab == 0 && viewModel.selectedToolType != null -> {
                viewModel.closeToolDetail()
            }
            // If on Dashboard tab and tool search query is not empty
            viewModel.activeTab == 0 && viewModel.toolSearchQuery.isNotEmpty() -> {
                viewModel.toolSearchQuery = ""
                focusManager.clearFocus()
            }
            // If on Dashboard tab and category filter is selected
            viewModel.activeTab == 0 && viewModel.selectedToolCategoryFilter != null -> {
                viewModel.selectedToolCategoryFilter = null
            }
            // If on non-home tab (Calculator, Converter, History, Themes)
            viewModel.activeTab != 0 -> {
                viewModel.activeTab = 0
            }
            // If on home tab (Dashboard) with no active search/filter/detail
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
                        AnimatedContent(
                            targetState = Pair(viewModel.activeTab, viewModel.selectedToolType),
                            transitionSpec = {
                                if (targetState.first > initialState.first) {
                                    (slideInVertically { height -> height } + fadeIn(animationSpec = tween(220))) togetherWith
                                            (slideOutVertically { height -> -height } + fadeOut(animationSpec = tween(180)))
                                } else {
                                    (slideInVertically { height -> -height } + fadeIn(animationSpec = tween(220))) togetherWith
                                            (slideOutVertically { height -> height } + fadeOut(animationSpec = tween(180)))
                                }
                            },
                            label = "TabTitleAnimation"
                        ) { (activeTab, selectedTool) ->
                            Text(
                                text = when {
                                    activeTab == 0 && selectedTool == null -> LanguageManager.getString("app_title_dashboard", viewModel.selectedLanguage)
                                    activeTab == 1 -> LanguageManager.getString("title_converter", viewModel.selectedLanguage)
                                    activeTab == 2 -> LanguageManager.getString("title_calculator", viewModel.selectedLanguage)
                                    activeTab == 3 -> LanguageManager.getString("title_history", viewModel.selectedLanguage)
                                    else -> LanguageManager.getString("app_title_main", viewModel.selectedLanguage)
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

                        if (viewModel.showFavoritesDialog) {
                            FavoritesDialog(
                                viewModel = viewModel,
                                themeColors = themeColors,
                                onDismiss = { viewModel.showFavoritesDialog = false }
                            )
                        }

                        IconButton(onClick = { showVisualThemesDialog = true }) {
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
                                    text = { Text(if (viewModel.selectedLanguage == AppLanguage.BENGALI) "সম্পূর্ণ ডেটা ব্যাকআপ ও রিস্টোর" else "Complete Data Backup & Restore", color = themeColors.displayText) },
                                    leadingIcon = { Icon(Icons.Default.CloudSync, contentDescription = null, tint = themeColors.buttonEqualBg) },
                                    onClick = {
                                        isMoreMenuExpanded = false
                                        showGlobalBackupDialog = true
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
                                    text = { Text(LanguageManager.getString("menu_update", viewModel.selectedLanguage), color = themeColors.displayText) },
                                    leadingIcon = { Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = themeColors.buttonEqualBg) },
                                    onClick = {
                                        isMoreMenuExpanded = false
                                        performUpdateCheck(true)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (viewModel.selectedLanguage == AppLanguage.BENGALI) "রিপোর্ট ও ফিডব্যাক" else "Report & Feedback", color = themeColors.displayText) },
                                    leadingIcon = { Icon(Icons.Default.Feedback, contentDescription = null, tint = themeColors.buttonEqualBg) },
                                    onClick = {
                                        isMoreMenuExpanded = false
                                        showFeedbackDialog = true
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
            ) {
                val bannerContext = LocalContext.current
                val activeTool = viewModel.selectedToolType
                val isIslamicTool = activeTool in listOf(
                    ToolType.HOLY_QURAN,
                    ToolType.NAMAZ_EDUCATION,
                    ToolType.HADITH_LIBRARY,
                    ToolType.ISLAMIC_DUAS,
                    ToolType.PRAYER_TIMES,
                    ToolType.SEHRI_IFTAR,
                    ToolType.QIBLA_COMPASS,
                    ToolType.DIGITAL_TASBIH
                )
                val isCalculatorActive = viewModel.activeTab == 2 ||
                        viewModel.showCalculatorDialog ||
                        (viewModel.activeTab == 0 && activeTool != null && !isIslamicTool)
                if (!isCalculatorActive) {
                    val quranViewModel: com.example.ui.quran.QuranViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                    QuranMiniPlayerBanner(
                        audioPlayer = QuranAudioPlayer.getInstance(bannerContext),
                        themeColors = themeColors,
                        onOpenSurah = { surahNumber ->
                            viewModel.selectedToolType = ToolType.HOLY_QURAN
                            viewModel.activeTab = 0
                            quranViewModel.selectSurahByNumber(surahNumber)
                        },
                        onOpenIslamicCategory = { category ->
                            viewModel.activeTab = 0
                            when (category) {
                                "NAMAZ" -> viewModel.selectedToolType = ToolType.NAMAZ_EDUCATION
                                "DUA" -> viewModel.selectedToolType = ToolType.ISLAMIC_DUAS
                                "SEHRI" -> viewModel.selectedToolType = ToolType.SEHRI_IFTAR
                                else -> viewModel.selectedToolType = ToolType.HOLY_QURAN
                            }
                        }
                    )
                }
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
                        Triple(0, Icons.Default.Dashboard, LanguageManager.getString("tab_tools", viewModel.selectedLanguage)),
                        Triple(1, ImageVector.vectorResource(id = R.drawable.ic_convert_tab), LanguageManager.getString("tab_conv", viewModel.selectedLanguage)),
                        Triple(2, Icons.Default.Calculate, LanguageManager.getString("tab_calc", viewModel.selectedLanguage)),
                        Triple(3, Icons.Default.History, LanguageManager.getString("tab_history", viewModel.selectedLanguage))
                    )
                    
                    // Left 2 tabs
                    for (i in 0..1) {
                        val (index, icon, label) = tabs[i]
                        val isSelected = viewModel.activeTab == index
                        val isStartButton = index == 0
                        
                        val customBg = if (isStartButton && viewModel.searchFabBgColorHex.isNotEmpty() && viewModel.searchFabBgColorHex != "#FFFFFF") {
                            try { Color(android.graphics.Color.parseColor(viewModel.searchFabBgColorHex)) } catch (e: Exception) { if (isSelected) Color.White.copy(alpha = 0.25f) else Color.Transparent }
                        } else {
                            if (isSelected) Color.White.copy(alpha = 0.25f) else Color.Transparent
                        }
                        
                        val customBorderModifier = if (isStartButton && viewModel.searchFabBorderColorHex.isNotEmpty()) {
                            try {
                                Modifier.border(
                                    width = 1.5.dp,
                                    color = Color(android.graphics.Color.parseColor(viewModel.searchFabBorderColorHex)),
                                    shape = RoundedCornerShape(19.dp)
                                )
                            } catch (e: Exception) { Modifier }
                        } else {
                            Modifier
                        }
                        
                        val customIconColor = if (isStartButton && viewModel.searchFabIconColorHex.isNotEmpty()) {
                            try { Color(android.graphics.Color.parseColor(viewModel.searchFabIconColorHex)) } catch (e: Exception) { if (isSelected) Color.White else Color.White.copy(alpha = 0.65f) }
                        } else {
                            if (isSelected) Color.White else Color.White.copy(alpha = 0.65f)
                        }

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
                                    .then(customBorderModifier)
                                    .background(customBg)
                                    .combinedClickable(
                                        interactionSource = tabInteraction,
                                        indication = ripple(bounded = true, color = Color.White),
                                        onClick = {
                                            viewModel.changeActiveTab(index, "Bottom Nav Tab $index", "বটম নেভিগেশন বার ট্যাব $index")
                                        },
                                        onLongClick = {
                                            if (isStartButton) {
                                                showStartButtonCustomizer = true
                                            }
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon, 
                                    contentDescription = label,
                                    modifier = Modifier.size(24.dp),
                                    tint = customIconColor
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
                                        onClick = {
                                            viewModel.changeActiveTab(index, "Bottom Nav Tab $index", "বটম নেভিগেশন বার ট্যাব $index")
                                        }
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
                
                // Floating Action Button in the center with Professional Blurry RGB Glow
                val fabInteractionSource = remember { MutableInteractionSource() }
                
                val infiniteTransition = rememberInfiniteTransition(label = "rgb_glowing_fab")
                
                var showFabGradientEditor by remember { mutableStateOf(false) }

                val hexColors = viewModel.fabGradientHexColors
                val rgbColors = remember(hexColors) {
                    hexColors.map { hex ->
                        try {
                            Color(android.graphics.Color.parseColor(hex))
                        } catch (e: Exception) {
                            Color.Gray
                        }
                    }
                }

                val duration = 7000

                val color1 by infiniteTransition.animateColor(
                    initialValue = rgbColors.firstOrNull() ?: Color.Red,
                    targetValue = rgbColors.lastOrNull() ?: Color.Red,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = duration
                            if (rgbColors.isNotEmpty()) {
                                rgbColors.forEachIndexed { index, color ->
                                    color at (duration * index / Math.max(1, rgbColors.size - 1)) with LinearEasing
                                }
                            }
                        },
                        repeatMode = RepeatMode.Restart
                    ), label = "rgb_color1"
                )

                val color2 by infiniteTransition.animateColor(
                    initialValue = rgbColors.getOrNull(rgbColors.size / 2) ?: Color.Blue,
                    targetValue = rgbColors.getOrNull(rgbColors.size / 2) ?: Color.Blue,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = duration
                            if (rgbColors.isNotEmpty()) {
                                val half = rgbColors.size / 2
                                val shifted = rgbColors.drop(half) + rgbColors.take(half)
                                shifted.forEachIndexed { index, color ->
                                    color at (duration * index / Math.max(1, shifted.size - 1)) with LinearEasing
                                }
                            }
                        },
                        repeatMode = RepeatMode.Restart
                    ), label = "rgb_color2"
                )

                val color3 by infiniteTransition.animateColor(
                    initialValue = rgbColors.getOrNull(rgbColors.size / 3) ?: Color.Yellow,
                    targetValue = rgbColors.getOrNull(rgbColors.size / 3) ?: Color.Yellow,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = duration
                            if (rgbColors.isNotEmpty()) {
                                val third = rgbColors.size / 3
                                val shifted = rgbColors.drop(third) + rgbColors.take(third)
                                shifted.forEachIndexed { index, color ->
                                    color at (duration * index / Math.max(1, shifted.size - 1)) with LinearEasing
                                }
                            }
                        },
                        repeatMode = RepeatMode.Restart
                    ), label = "rgb_color3"
                )

                val iconScale by infiniteTransition.animateFloat(
                    initialValue = 0.9f,
                    targetValue = 1.12f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1600, easing = LinearOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "iconScale"
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-21).dp)
                        .size(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val searchBgColor = remember(viewModel.centerSearchFabBgColorHex, themeColors.buttonEqualBg) {
                        if (viewModel.centerSearchFabBgColorHex.isNotEmpty()) {
                            try { Color(android.graphics.Color.parseColor(viewModel.centerSearchFabBgColorHex)) } catch (e: Exception) { Color.White }
                        } else Color.White
                    }
                    
                    val searchBorderColor = remember(viewModel.centerSearchFabBorderColorHex, themeColors.buttonEqualBg) {
                        if (viewModel.centerSearchFabBorderColorHex.isNotEmpty()) {
                            try { Color(android.graphics.Color.parseColor(viewModel.centerSearchFabBorderColorHex)) } catch (e: Exception) { themeColors.buttonEqualBg }
                        } else themeColors.buttonEqualBg
                    }
                    
                    val searchIconColor = remember(viewModel.centerSearchFabIconColorHex, themeColors.buttonEqualBg) {
                        if (viewModel.centerSearchFabIconColorHex.isNotEmpty()) {
                            try { Color(android.graphics.Color.parseColor(viewModel.centerSearchFabIconColorHex)) } catch (e: Exception) { themeColors.buttonEqualBg }
                        } else themeColors.buttonEqualBg
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(elevation = 2.dp, shape = CircleShape)
                            .background(searchBgColor, shape = CircleShape)
                            .border(2.dp, searchBorderColor, CircleShape)
                            .clip(CircleShape)
                            .combinedClickable(
                                interactionSource = fabInteractionSource,
                                indication = ripple(bounded = false, color = themeColors.buttonEqualBg),
                                onClick = {
                                    viewModel.showGlobalSearch = true
                                },
                                onLongClick = {
                                    showCenterSearchFabCustomizer = true
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = searchIconColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                if (showFabGradientEditor) {
                    FabGradientEditorDialog(
                        viewModel = viewModel,
                        themeColors = themeColors,
                        onDismiss = { showFabGradientEditor = false }
                    )
                }
            }
        }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        val dragAnimatable = remember { androidx.compose.animation.core.Animatable(0f) }
        var isDragging by remember { mutableStateOf(false) }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(themeColors.background)
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            val widthPx = with(LocalDensity.current) { constraints.maxWidth.toFloat() }
            val currentTab = viewModel.activeTab
            val isSubDetailOpen = (currentTab == 0 && viewModel.selectedToolType != null) ||
                                  (currentTab == 1 && viewModel.selectedConverterType != null)

            val targetTab by remember(currentTab) {
                derivedStateOf {
                    val offset = dragAnimatable.value
                    when {
                        offset < -2f && currentTab < 3 -> currentTab + 1
                        offset > 2f && currentTab > 0 -> currentTab - 1
                        else -> null
                    }
                }
            }

            val swipeContainerModifier = if (isSubDetailOpen) {
                Modifier.fillMaxSize()
            } else {
                Modifier
                    .fillMaxSize()
                    .pointerInput(currentTab) {
                        var dragAmountSum = 0f
                        detectHorizontalDragGestures(
                            onDragStart = {
                                isDragging = true
                                dragAmountSum = 0f
                            },
                            onDragEnd = {
                                val threshold = widthPx * 0.15f
                                if (dragAmountSum < -threshold && currentTab < 3) {
                                    coroutineScope.launch {
                                        dragAnimatable.animateTo(
                                            -widthPx,
                                            animationSpec = spring(
                                                stiffness = Spring.StiffnessLow,
                                                dampingRatio = Spring.DampingRatioNoBouncy
                                            )
                                        )
                                        viewModel.selectActiveTab(currentTab + 1)
                                        dragAnimatable.snapTo(0f)
                                        isDragging = false
                                    }
                                } else if (dragAmountSum > threshold && currentTab > 0) {
                                    coroutineScope.launch {
                                        dragAnimatable.animateTo(
                                            widthPx,
                                            animationSpec = spring(
                                                stiffness = Spring.StiffnessLow,
                                                dampingRatio = Spring.DampingRatioNoBouncy
                                            )
                                        )
                                        viewModel.selectActiveTab(currentTab - 1)
                                        dragAnimatable.snapTo(0f)
                                        isDragging = false
                                    }
                                } else {
                                    coroutineScope.launch {
                                        dragAnimatable.animateTo(
                                            0f,
                                            animationSpec = spring(
                                                stiffness = Spring.StiffnessMediumLow,
                                                dampingRatio = Spring.DampingRatioNoBouncy
                                            )
                                        )
                                        isDragging = false
                                    }
                                }
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    dragAnimatable.animateTo(
                                        0f,
                                        animationSpec = spring(
                                            stiffness = Spring.StiffnessMediumLow,
                                            dampingRatio = Spring.DampingRatioNoBouncy
                                        )
                                    )
                                    isDragging = false
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                val canDragLeft = currentTab < 3
                                val canDragRight = currentTab > 0

                                val effectiveDrag = if ((dragAmount > 0 && !canDragRight && dragAmountSum >= 0) ||
                                                        (dragAmount < 0 && !canDragLeft && dragAmountSum <= 0)) {
                                    dragAmount * 0.2f
                                } else {
                                    dragAmount
                                }

                                change.consume()
                                dragAmountSum += effectiveDrag
                                coroutineScope.launch {
                                    dragAnimatable.snapTo(dragAmountSum)
                                }
                            }
                        )
                    }
            }

            Box(modifier = swipeContainerModifier) {
                if (isDragging || dragAnimatable.value != 0f) {
                    // Render current screen sliding
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                translationX = dragAnimatable.value
                            }
                    ) {
                        when (currentTab) {
                            0 -> DashboardScreen(viewModel, themeColors)
                            1 -> SmartConverterScreen(viewModel, themeColors)
                            2 -> CalculatorScreen(viewModel, themeColors)
                            3 -> HistoryLogsScreen(viewModel, themeColors)
                            else -> DashboardScreen(viewModel, themeColors)
                        }
                    }

                    // Render adjacent target screen sliding side-by-side
                    val currentTarget = targetTab
                    if (currentTarget != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    val adjacentOffset = if (currentTarget > currentTab) {
                                        widthPx + dragAnimatable.value
                                    } else {
                                        -widthPx + dragAnimatable.value
                                    }
                                    translationX = adjacentOffset
                                }
                        ) {
                            when (currentTarget) {
                                0 -> DashboardScreen(viewModel, themeColors)
                                1 -> SmartConverterScreen(viewModel, themeColors)
                                2 -> CalculatorScreen(viewModel, themeColors)
                                3 -> HistoryLogsScreen(viewModel, themeColors)
                                else -> DashboardScreen(viewModel, themeColors)
                            }
                        }
                    }
                } else {
                    // Resting state: render active tab with animated transition for tab clicks
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy)) { width -> width } + fadeIn(animationSpec = tween(220))) togetherWith
                                        (slideOutHorizontally(animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy)) { width -> -width } + fadeOut(animationSpec = tween(220)))
                            } else {
                                (slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy)) { width -> -width } + fadeIn(animationSpec = tween(220))) togetherWith
                                        (slideOutHorizontally(animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy)) { width -> width } + fadeOut(animationSpec = tween(220)))
                            }
                        },
                        label = "MainScreenTabTransition",
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> DashboardScreen(viewModel, themeColors)
                            1 -> SmartConverterScreen(viewModel, themeColors)
                            2 -> CalculatorScreen(viewModel, themeColors)
                            3 -> HistoryLogsScreen(viewModel, themeColors)
                            else -> DashboardScreen(viewModel, themeColors)
                        }
                    }
                }
            }

            // Floating Pill AI Button with 2dp Animated Rotating Gemini Border on Right Side (Hides on Calculator tab or when inside Holy Quran tool with smooth enter/exit animation)
            val isAiFabVisible = !viewModel.showAiChat && viewModel.activeTab != 2 && viewModel.selectedToolType != ToolType.HOLY_QURAN

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 8.dp, bottom = 8.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                AnimatedVisibility(
                    visible = isAiFabVisible,
                    enter = fadeIn(animationSpec = tween(280)) + scaleIn(initialScale = 0.6f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                    exit = fadeOut(animationSpec = tween(220)) + scaleOut(targetScale = 0.6f, animationSpec = tween(220))
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "gemini_border_rotation")
                    val rotationAngle by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "aiRotationAngle"
                    )

                    val geminiColors = remember(viewModel.aiFabGradientColorsHex) {
                        val base = if (viewModel.aiFabGradientColorsHex.isNotEmpty()) {
                            viewModel.aiFabGradientColorsHex.split(",").mapNotNull { hex ->
                                try { Color(android.graphics.Color.parseColor(hex.trim())) } catch (e: Exception) { null }
                            }
                        } else emptyList()
                        val list = if (base.isNotEmpty()) base else listOf(
                            Color(0xFF4285F4),
                            Color(0xFF9B51E0),
                            Color(0xFFEA4335),
                            Color(0xFFFBBC05),
                            Color(0xFF34A853)
                        )
                        if (list.firstOrNull() != list.lastOrNull()) {
                            list + list.first()
                        } else {
                            list
                        }
                    }

                    val animatedGradientBrush = remember(rotationAngle, geminiColors) {
                        object : androidx.compose.ui.graphics.ShaderBrush() {
                            override fun createShader(size: androidx.compose.ui.geometry.Size): androidx.compose.ui.graphics.Shader {
                                val shader = androidx.compose.ui.graphics.SweepGradientShader(
                                    center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f),
                                    colors = geminiColors
                                )
                                val matrix = android.graphics.Matrix()
                                matrix.postRotate(rotationAngle, size.width / 2f, size.height / 2f)
                                shader.setLocalMatrix(matrix)
                                return shader
                            }
                        }
                    }

                    val aiBgColor = remember(viewModel.aiFabBgColorHex, themeColors.isDark) {
                        if (viewModel.aiFabBgColorHex.isNotEmpty()) {
                            try { Color(android.graphics.Color.parseColor(viewModel.aiFabBgColorHex)) } catch (e: Exception) { if (themeColors.isDark) Color(0xFF1E293B) else Color.White }
                        } else {
                            if (themeColors.isDark) Color(0xFF1E293B) else Color.White
                        }
                    }

                    val aiIconColor = remember(viewModel.aiFabIconColorHex) {
                        if (viewModel.aiFabIconColorHex.isNotEmpty()) {
                            try { Color(android.graphics.Color.parseColor(viewModel.aiFabIconColorHex)) } catch (e: Exception) { Color(0xFF4285F4) }
                        } else {
                            Color(0xFF4285F4)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(26.dp))
                            .clip(RoundedCornerShape(26.dp))
                            .drawWithContent {
                                drawContent()
                                drawRoundRect(
                                    brush = animatedGradientBrush,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(26.dp.toPx(), 26.dp.toPx())
                                )
                            }
                            .background(aiBgColor)
                            .combinedClickable(
                                onClick = { viewModel.showAiChat = true },
                                onLongClick = { showAiFabCustomizer = true }
                            )
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI",
                                tint = aiIconColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (themeColors.isDark) Color.White else Color(0xFF1E293B)
                            )
                        }
                    }
                }
            }
        }

        // --- Visual Themes Dialog ---
        if (showVisualThemesDialog) {
            Dialog(
                onDismissRequest = { showVisualThemesDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = themeColors.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(themeColors.buttonEqualBg)
                                .statusBarsPadding()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { showVisualThemesDialog = false }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) "ভিজ্যুয়াল থিমস" else "Visual Themes",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        VisualThemesScreen(viewModel = viewModel, themeColors = themeColors)
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
                                val languagesList = listOf(AppLanguage.ENGLISH, AppLanguage.BENGALI).chunked(2)
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

                        // Language Selector
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) "ভাষা (Language)" else "Language",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = themeColors.displayText
                                )
                                Text(
                                    text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) "আপনার পছন্দের ভাষা নির্বাচন করুন" else "Select your preferred language",
                                    fontSize = 11.sp,
                                    color = themeColors.displayText.copy(alpha = 0.6f)
                                )
                            }
                            Box {
                                var isSettingsLangExpanded by remember { mutableStateOf(false) }
                                TextButton(
                                    onClick = { isSettingsLangExpanded = true }
                                ) {
                                    Text(
                                        text = "${viewModel.selectedLanguage.flag} ${viewModel.selectedLanguage.displayName}",
                                        color = themeColors.buttonEqualBg,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = themeColors.buttonEqualBg)
                                }
                                DropdownMenu(
                                    expanded = isSettingsLangExpanded,
                                    onDismissRequest = { isSettingsLangExpanded = false },
                                    modifier = Modifier.background(themeColors.cardBg)
                                ) {
                                    listOf(AppLanguage.ENGLISH, AppLanguage.BENGALI).forEach { lang ->
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
                                                isSettingsLangExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
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

        // --- Global Backup & Restore Dialog ---
        if (showGlobalBackupDialog) {
            val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
            AlertDialog(
                onDismissRequest = { showGlobalBackupDialog = false },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(themeColors.buttonEqualBg.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = if (isBn) "সম্পূর্ণ ডেটা ব্যাকআপ ও রিস্টোর" else "Complete Data Backup & Restore",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = themeColors.displayText,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (isBn)
                                "আপনার অ্যাপের সমস্ত ডেটা (ক্যালকুলেটর হিস্টোরি, বাজার ফর্দ, নোটস, জিকির গণনাকারি, এআই চ্যাট ও পছন্দসমূহ) একটি নিরাপদ ফাইল হিসেবে ব্যাকআপ রাখুন অথবা পূর্বের ব্যাকআপ থেকে রিস্টোর করুন।"
                            else
                                "Safely back up or restore all your app data including Calculator history, Market plans, Notes, AI Chats, and Favorites.",
                            fontSize = 13.sp,
                            color = themeColors.displayText.copy(alpha = 0.85f),
                            lineHeight = 19.sp
                        )

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = themeColors.displayBackground)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = if (isBn) "📁 অন্তর্ভুক্ত সমস্ত ডেটা:" else "📁 Included Data Scope:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.buttonEqualBg
                                )
                                val items = if (isBn) listOf(
                                    "• 🧮 ক্যালকুলেটর হিস্টোরি",
                                    "• 🛒 বাজারের ফর্দ ও কেনাকাটা",
                                    "• 📝 সেভ করা নোটস ও খসড়া",
                                    "• 🤖 এআই চ্যাট হিস্টোরি",
                                    "• ⭐ পছন্দের টুলস ও কনভার্টার",
                                    "• 📷 বারকোড স্ক্যান রেকর্ডস"
                                ) else listOf(
                                    "• 🧮 Calculator History Log",
                                    "• 🛒 Market & Grocery Memos",
                                    "• 📝 Saved Personal Notes",
                                    "• 🤖 AI Assistant Chat Logs",
                                    "• ⭐ Favorite Tools & Converters",
                                    "• 📷 Barcode Scanner Logs"
                                )
                                items.forEach { item ->
                                    Text(
                                        text = item,
                                        fontSize = 11.5.sp,
                                        color = themeColors.displayText.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                    globalBackupLauncher.launch("toolsmate_full_backup_$timestamp.json")
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = if (isBn) "ব্যাকআপ নিন" else "Export",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    globalRestoreLauncher.launch(arrayOf("application/json", "*/*"))
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = if (isBn) "রিস্টোর করুন" else "Restore",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showGlobalBackupDialog = false }) {
                        Text(
                            text = if (isBn) "বন্ধ করুন" else "Close",
                            color = themeColors.displayText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                containerColor = themeColors.cardBg,
                shape = RoundedCornerShape(18.dp)
            )
        }

        // --- Terms & Conditions Dialog ---
        if (showTermsDialog) {
            AlertDialog(
                onDismissRequest = { showTermsDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = LanguageManager.getString("menu_terms", viewModel.selectedLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = themeColors.displayText
                        )
                    }
                },
                text = {
                    val termsText = if (viewModel.selectedLanguage == AppLanguage.BENGALI) {
                        "১. ব্যবহারের অনুমতি ও উদ্দেশ্যে:\n" +
                        "ToolsMate অ্যাপটি আপনার দৈনন্দিন জীবনের বৈজ্ঞানিক হিসাব-নিকাশ, একক রূপান্তর, ইসলামিক ইবাদত সহায়ক ফিচার (কুরআন, হাদিস, নামাজের সময়সূচি) এবং নিত্যপ্রয়োজনীয় টুলস ব্যবহারের সুবিধার্থে তৈরি। অ্যাপটি সম্পূর্ণরূপে ব্যক্তিগত ও অ-বাণিজ্যিক ব্যবহারের জন্য উন্মুক্ত।\n\n" +
                        "২. গাণিতিক হিসাব ও তথ্যের নির্ভুলতা:\n" +
                        "প্রতিটি হিসাব, গাণিতিক সূত্র, একক পরিবর্তন এবং বিষয়বস্তু নির্ভুল রাখার জন্য সর্বাত্মক প্রচেষ্টা করা হয়েছে। তবে কোনো অনিচ্ছাকৃত গাণিতিক বা তথ্যগত ভুলের জন্য ডেভেলপার সরাসরি বা পরোক্ষভাবে আইনি বা আর্থিক দায়ী থাকবে না। গুরুত্বপূর্ণ আর্থিক বা বৈজ্ঞানিক হিসাব ব্যবহারের পূর্বে পুনরায় যাচাই করার পরামর্শ দেওয়া হচ্ছে।\n\n" +
                        "৩. ইসলামিক তথ্য ও ওয়াক্তের সময়সূচি:\n" +
                        "নামাজের সময়সূচি ও সেহরি-ইফতারের সময় অ্যাপে ব্যবহৃত ভৌগোলিক অ্যালগরিদম ও বাংলাদেশ ইসলামিক ফাউন্ডেশনের স্ট্যান্ডার্ড অনুযায়ী হিসাব করা হয়। স্থানীয় দূরত্বের কারণে সামান্য ২-১ মিনিটের পার্থক্য হতে পারে, তাই নিজ এলাকার মসজিদের আজান অনুসরণ করা উত্তম।\n\n" +
                        "৪. অফলাইন সিস্টেম ও ব্যবহারকারীর ডেটা স্বত্ব:\n" +
                        "এটি ১০০% অফলাইন ও প্রাইভেসি-বান্ধব অ্যাপ্লিকেশন। কোনো ব্যবহারকারীর ডেটা আমাদের কোনো সার্ভারে সংরক্ষণ বা প্রেরণ করা হয় না। অ্যাপটিতে সংরক্ষিত তথ্য (যেমন- হিস্ট্রি, মার্কেট লিস্ট, বুকমার্ক) সম্পূর্ণভাবে ব্যবহারকারীর নিজের ডিভাইসে জমা থাকে।\n\n" +
                        "৫. শর্তাবলীর পরিবর্তন ও পরিমার্জন:\n" +
                        "অ্যাপের সার্বিক মানোন্নয়ন, নতুন ফিচার সংযোজন বা টেকনিক্যাল আপডেটের প্রয়োজনে যেকোনো সময় এই শর্তাবলী পরিবর্তন বা পরিমার্জন করার পূর্ণ অধিকার কর্তৃপক্ষের সংরক্ষিত।"
                    } else {
                        "1. Acceptance & Purpose of Use:\n" +
                        "ToolsMate is designed to simplify your daily calculation needs, unit conversions, Islamic practice tools (Holy Quran, Hadith, Prayer Timings), and lifestyle utilities. The app is strictly intended for personal, non-commercial use.\n\n" +
                        "2. Calculation Accuracy Disclaimer:\n" +
                        "While every effort is made to ensure absolute accuracy across all scientific formulas, unit models, and tools, developers are not liable for any direct or indirect financial or calculation errors. Users are encouraged to double-check critical financial/mathematical figures.\n\n" +
                        "3. Prayer Times & Islamic Guidance:\n" +
                        "Namaz and Sehri/Iftar schedules are generated using local coordinate algorithms and recognized Islamic calculation standards. A variance of 1-2 minutes may occur due to hyper-local factors; always prioritize your local mosque calls.\n\n" +
                        "4. Offline Security & User Data Ownership:\n" +
                        "ToolsMate is a 100% offline-first application. All created data (history logs, shopping lists, Quran bookmarks) resides strictly on your local device storage. No user data is sent to external cloud servers.\n\n" +
                        "5. Updates & Modifications:\n" +
                        "The development team reserves the right to update, modify, or enhance features and terms of service at any time to ensure compliance and optimal performance."
                    }
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                    ) {
                        item {
                            Text(
                                text = termsText,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = themeColors.displayText.copy(alpha = 0.85f)
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
                shape = RoundedCornerShape(18.dp)
            )
        }

        // --- Privacy Policy Dialog ---
        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PrivacyTip,
                            contentDescription = null,
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = LanguageManager.getString("menu_privacy", viewModel.selectedLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = themeColors.displayText
                        )
                    }
                },
                text = {
                    val privacyText = if (viewModel.selectedLanguage == AppLanguage.BENGALI) {
                        "১. তথ্যের গোপনীয়তা ও সুরক্ষা (Zero Data Collection):\n" +
                        "আমাদের প্রধান লক্ষ্য হলো আপনার তথ্যের সর্বোচ্চ নিরাপত্তা বজায় রাখা। ToolsMate অ্যাপটি আপনার কোনো নাম, ফোন নম্বর, ইমেইল, বা টাইপ করা সংবেদনশীল ডেটা সংগ্রহ বা সার্ভারে প্রেরণ করে না।\n\n" +
                        "২. লোকাল ডাটাবেজ সংরক্ষণ (Local Room Database):\n" +
                        "আপনার গণনার ইতিহাস (History Logs), পছন্দের থিম সেটিং, মার্কেট শপিং লিস্ট, কুরআনের আয়াতের বুকমার্ক ও তাসবীহ কাউন্ট শুধুমাত্র আপনার ফোনের নিজস্ব লোকাল এনক্রিপ্টেড স্থানে (Room DB & Preferences) জমা থাকে। আপনি চাইলে সেটিংস থেকে ১-ক্লিকে যেকোনো সময় এসব ডেটা মুছে ফেলতে পারেন।\n\n" +
                        "৩. লোকেশন পারমিশন ও ক্বিবলা কম্পাস (GPS Usage):\n" +
                        "আপনার নিখুঁত নামাজের সময়সূচি, সেহরি-ইফতারের সময় এবং ক্বিবলার সঠিক দিক নির্ণয়ের জন্য ডিভাইসের লোকেশন পারমিশন ব্যবহৃত হয়। এই লোকেশন তথ্য শুধুমাত্র আপনার নিজস্ব ডিভাইসেই প্রক্রিয়াজাত হয়, কখনো কোনো থার্ড-পার্টি বা দূরবর্তী সার্ভারে আপলোড হয় না।\n\n" +
                        "৪. অডিও ও মিডিয়া পারমিশন (Local Audio Cache):\n" +
                        "আল-কুরআনের তিলাওয়াত ও আজানের অ্যালার্ম প্লে করার জন্য লোকাল স্টোরেজ ব্যবহার করা হয়। অ্যাপটি ব্যাকগ্রাউন্ডে কোনো গোপন তথ্য প্রসেস করে না।\n\n" +
                        "৫. থার্ড-পার্টি ট্র্যাকিং বা অ্যানালিটিক্স মুক্ত:\n" +
                        "এই অ্যাপে কোনো প্রকার থার্ড-পার্টি ট্র্যাকার, গোপন ডাটা মাইনার বা বিজ্ঞাপন নেটওয়ার্ক যুক্ত নেই। আপনার অভিজ্ঞতা শতভাগ নিরাপদ, বিজ্ঞাপনমুক্ত ও স্বচ্ছন্দ রাখা আমাদের অগ্রাধিকার।"
                    } else {
                        "1. Absolute Zero Data Collection:\n" +
                        "Your privacy is our utmost priority. ToolsMate does not request, collect, transmit, or sell any personal information, phone numbers, emails, or calculation records to external servers.\n\n" +
                        "2. On-Device Local Storage (Room DB):\n" +
                        "All calculation history, market lists, theme preferences, Quran bookmarks, and Tasbih counters are saved exclusively on your device's local storage (Room DB & Encrypted SharedPrefs). You can wipe this data at any moment in settings.\n\n" +
                        "3. Location Access for Namaz & Qibla:\n" +
                        "Location permission is accessed strictly locally to calculate precise Namaz prayer times, Sehri/Iftar bounds, and Qibla compass bearing. Your location coordinates are processed on-device and never uploaded anywhere.\n\n" +
                        "4. Audio & Media System Usage:\n" +
                        "Storage permissions are utilized solely to cache and stream local Quran audio files and prayer notification sounds offline smoothly.\n\n" +
                        "5. No Analytics or Third-Party Trackers:\n" +
                        "ToolsMate contains no user tracking software, invasive analytics, or background telemetry. Enjoy a clean, private, and secure application experience."
                    }
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                    ) {
                        item {
                            Text(
                                text = privacyText,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = themeColors.displayText.copy(alpha = 0.85f)
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
                shape = RoundedCornerShape(18.dp)
            )
        }

        val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

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
                    "ToolsMate"
                }
            }

            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = LanguageManager.getString("menu_about", viewModel.selectedLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = themeColors.displayText
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(72.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.app_logo),
                                contentDescription = null,
                                modifier = Modifier.size(72.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = realAppName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = themeColors.displayText,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = if (isBn) "ভার্সন $realVersionName" else "Version $realVersionName",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = themeColors.displayText.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        val descText = if (isBn) {
                            "ToolsMate (টুলসমেট) হলো একটি সর্বাধুনিক অল-ইন-ওয়ান ইউটিলিটি, ফাইন্যান্সিয়াল, সায়েন্টিফিক ও ইসলামিক লাইফস্টাইল অ্যাপ্লিকেশন। এতে রয়েছে এডভান্সড সায়েন্টিফিক ক্যালকুলেটর, মাল্টি-ইউনিট কনভার্টার, সম্পূর্ণ আল-কুরআন ও হাদিস গ্রন্থ, নির্ভুল নামাজের সময়সূচি, ক্বিবলা কম্পাস, বাজার লিস্ট, বয়স ও বিএমআই ক্যালকুলেটর এবং স্মার্ট অফলাইন এআই সহকারী।"
                        } else {
                            "ToolsMate is an all-in-one modern utility, scientific, financial, and Islamic lifestyle suite. Features a comprehensive scientific calculator, multi-unit converters, complete Holy Quran & Hadith books, precise prayer timings, Qibla compass, market list, and offline AI tools."
                        }
                        Text(
                            text = descText,
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp,
                            color = themeColors.displayText.copy(alpha = 0.85f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Highlights Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Surface(
                                color = themeColors.buttonEqualBg.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = if (isBn) "⚡ ১০০% অফলাইন" else "⚡ 100% Offline",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = themeColors.buttonEqualBg,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                color = themeColors.buttonEqualBg.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = if (isBn) "🔒 জিরো ডাটা ট্র্যাকিং" else "🔒 Zero Tracking",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = themeColors.buttonEqualBg,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.12f))
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Modern Interactive Developer Info Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = if (isBn) "👨‍💻 ডেভেলপার পরিচিতি" else "👨‍💻 Developer Info",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = themeColors.buttonEqualBg
                                )
                                Text(
                                    text = if (isBn) "ডেভেলপার: Md. Shariful Islam" else "Developer: Md. Shariful Islam",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = themeColors.displayText
                                )
                                
                                Text(
                                    text = if (isBn) "যোগাযোগ করতে নিচের যেকোনো একটিতে ট্যাপ করুন:" else "Tap below to redirect and connect instantly:",
                                    fontSize = 11.sp,
                                    color = themeColors.displayText.copy(alpha = 0.6f)
                                )

                                HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.08f))

                                // Email Action
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            launchEmailSafely(context, "Connect.shariful@gmail.com", "ToolsMate - Connection")
                                        }
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "Email",
                                        tint = themeColors.buttonEqualBg,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isBn) "ইমেইল পাঠান" else "Email Support",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = themeColors.displayText
                                        )
                                        Text(
                                            text = "Connect.shariful@gmail.com",
                                            fontSize = 11.sp,
                                            color = themeColors.displayText.copy(alpha = 0.6f)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = themeColors.displayText.copy(alpha = 0.3f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // WhatsApp Action
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            launchUriSafely(context, "https://wa.me/8801768899599")
                                        }
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = "WhatsApp",
                                        tint = themeColors.buttonEqualBg,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isBn) "হোয়াটসঅ্যাপে মেসেজ পাঠান" else "WhatsApp Chat",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = themeColors.displayText
                                        )
                                        Text(
                                            text = "+8801768899599",
                                            fontSize = 11.sp,
                                            color = themeColors.displayText.copy(alpha = 0.6f)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = themeColors.displayText.copy(alpha = 0.3f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Facebook Action
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            launchUriSafely(context, "https://facebook.com/shariful.uxd")
                                        }
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = "Facebook",
                                        tint = themeColors.buttonEqualBg,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isBn) "ফেসবুক প্রোফাইল" else "Facebook Profile",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = themeColors.displayText
                                        )
                                        Text(
                                            text = "fb.com/shariful.uxd",
                                            fontSize = 11.sp,
                                            color = themeColors.displayText.copy(alpha = 0.6f)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = themeColors.displayText.copy(alpha = 0.3f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
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

        // --- Report / Feedback Dialog ---
        if (showFeedbackDialog) {
            val context = androidx.compose.ui.platform.LocalContext.current
            var feedbackText by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showFeedbackDialog = false },
                title = {
                    Text(
                        text = if (isBn) "রিপোর্ট ও ফিডব্যাক" else "Report & Feedback",
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (isBn) {
                                "আপনার মূল্যবান মতামত, পরামর্শ বা যেকোনো সমস্যার কথা আমাদের জানান। নিচে আপনার বার্তাটি লিখুন এবং সরাসরি ইমেইল বা হোয়াটসঅ্যাপের মাধ্যমে পাঠান।"
                            } else {
                                "Please share your valuable feedback, suggestions, or reports with us. Type your message below and send instantly via WhatsApp or Email."
                            },
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = themeColors.displayText.copy(alpha = 0.8f)
                        )

                        OutlinedTextField(
                            value = feedbackText,
                            onValueChange = { feedbackText = it },
                            placeholder = {
                                Text(
                                    text = if (isBn) "এখানে আপনার মতামত বা রিপোর্ট লিখুন..." else "Type your feedback or report here...",
                                    color = themeColors.displayText.copy(alpha = 0.4f),
                                    fontSize = 14.sp
                                )
                            },
                            minLines = 3,
                            maxLines = 6,
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
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Email send button
                        Button(
                            onClick = {
                                val msg = feedbackText.trim()
                                launchEmailSafely(
                                    context = context,
                                    email = "Connect.shariful@gmail.com",
                                    subject = "ToolsMate - Report/Feedback",
                                    body = msg
                                )
                                showFeedbackDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = themeColors.buttonEqualBg,
                                contentColor = themeColors.buttonEqualText
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn) "ইমেইলের মাধ্যমে পাঠান" else "Send via Email",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // WhatsApp send button
                        Button(
                            onClick = {
                                val msg = feedbackText.trim()
                                val finalMsg = if (msg.isEmpty()) "Hello! I want to share feedback." else msg
                                launchUriSafely(
                                    context = context,
                                    uriString = "https://wa.me/8801768899599?text=${android.net.Uri.encode(finalMsg)}"
                                )
                                showFeedbackDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25D366), // Standard WhatsApp Green
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn) "হোয়াটসঅ্যাপে পাঠান" else "Send via WhatsApp",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Close button
                        TextButton(
                            onClick = { showFeedbackDialog = false },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                text = if (isBn) "বাতিল" else "Cancel",
                                color = themeColors.displayText.copy(alpha = 0.6f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                containerColor = themeColors.cardBg,
                shape = RoundedCornerShape(20.dp)
            )
        }

        if (showStartButtonCustomizer) {
            NavbarStartButtonCustomizerDialog(
                viewModel = viewModel,
                themeColors = themeColors,
                onDismiss = { showStartButtonCustomizer = false }
            )
        }

        if (showAiFabCustomizer) {
            AiFabCustomizerDialog(
                viewModel = viewModel,
                themeColors = themeColors,
                onDismiss = { showAiFabCustomizer = false }
            )
        }
        
        if (showCenterSearchFabCustomizer) {
            CenterSearchFabCustomizerDialog(
                viewModel = viewModel,
                themeColors = themeColors,
                onDismiss = { showCenterSearchFabCustomizer = false }
            )
        }

        // --- App Update Dialog ---
        if (showUpdateDialog) {
            AlertDialog(
                onDismissRequest = { 
                    if (!isDownloadingUpdate) {
                        showUpdateDialog = false 
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = LanguageManager.getString("update_title", viewModel.selectedLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = themeColors.displayText
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = if (isBn) "নতুন ভার্সন: $updateLatestVersion" else "New Version: $updateLatestVersion",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = themeColors.displayText
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBn) "পরিবর্তনসমূহ (Changelog):" else "Changelog:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = themeColors.displayText.copy(alpha = 0.8f)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = updateChangeLog,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = themeColors.displayText,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        if (isDownloadingUpdate) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "${LanguageManager.getString("update_downloading", viewModel.selectedLanguage)} (${updateDownloadProgress}%)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = themeColors.buttonEqualBg
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { updateDownloadProgress / 100f },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = themeColors.buttonEqualBg,
                                trackColor = themeColors.buttonEqualBg.copy(alpha = 0.2f)
                            )
                        } else if (downloadedApkFile != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBn) "ডাউনলোড সম্পন্ন হয়েছে!" else "Download complete!",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        } else if (updateErrorMessage != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = updateErrorMessage ?: "",
                                fontSize = 12.sp,
                                color = Color.Red,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                confirmButton = {
                    if (downloadedApkFile != null) {
                        Button(
                            onClick = {
                                val success = UpdateManager.installApk(context, downloadedApkFile!!)
                                if (!success) {
                                    Toast.makeText(context, "Error launching installation", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
                        ) {
                            Text(
                                text = LanguageManager.getString("update_install", viewModel.selectedLanguage),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (!isDownloadingUpdate) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isDownloadingUpdate = true
                                    updateDownloadProgress = 0
                                    updateErrorMessage = null
                                    UpdateManager.downloadApk(
                                        context = context,
                                        url = updateDownloadUrl,
                                        onProgress = { progress ->
                                            updateDownloadProgress = progress
                                        },
                                        onSuccess = { file ->
                                            isDownloadingUpdate = false
                                            downloadedApkFile = file
                                            // Auto-launch installation
                                            UpdateManager.installApk(context, file)
                                        },
                                        onError = { err ->
                                            isDownloadingUpdate = false
                                            updateErrorMessage = err.localizedMessage ?: "Unknown error"
                                        }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
                        ) {
                            Text(
                                text = LanguageManager.getString("update_btn", viewModel.selectedLanguage),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                dismissButton = {
                    if (!isDownloadingUpdate) {
                        TextButton(
                            onClick = { showUpdateDialog = false }
                        ) {
                            Text(
                                text = LanguageManager.getString("update_cancel", viewModel.selectedLanguage),
                                color = themeColors.displayText.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Bold
                            )
                        }
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

    // --- Pending Crash Report & Developer Feedback Dialog ---
    if (showCrashReportDialog && pendingCrashReport != null) {
        com.example.ui.components.CrashReportDialog(
            report = pendingCrashReport!!,
            isBn = viewModel.selectedLanguage == AppLanguage.BENGALI,
            themeColors = themeColors,
            onDismiss = {
                showCrashReportDialog = false
                com.example.util.CrashReporter.clearPendingCrashReport(context)
            }
        )
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

    if (viewModel.pendingUnfavoriteTool != null) {
        val toolName = viewModel.pendingUnfavoriteTool!!
        val tool = try { com.example.data.model.ToolType.valueOf(toolName) } catch (e: Exception) { null }
        val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
        UnfavoriteConfirmDialog(
            itemType = "Tool",
            itemNameBn = tool?.titleBn ?: toolName,
            itemNameEn = tool?.titleEn ?: toolName,
            isBn = isBn,
            themeColors = themeColors,
            onConfirm = { viewModel.confirmUnfavoriteTool() },
            onDismiss = { viewModel.pendingUnfavoriteTool = null }
        )
    }

    if (viewModel.pendingUnfavoriteConverter != null) {
        val convName = viewModel.pendingUnfavoriteConverter!!
        val conv = try { com.example.data.model.ConverterType.valueOf(convName) } catch (e: Exception) { null }
        val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
        UnfavoriteConfirmDialog(
            itemType = "Converter",
            itemNameBn = conv?.getTitle(AppLanguage.BENGALI) ?: convName,
            itemNameEn = conv?.getTitle(AppLanguage.ENGLISH) ?: convName,
            isBn = isBn,
            themeColors = themeColors,
            onConfirm = { viewModel.confirmUnfavoriteConverter() },
            onDismiss = { viewModel.pendingUnfavoriteConverter = null }
        )
    }

    if (viewModel.pendingFavoriteConfirmAction != null) {
        val action = viewModel.pendingFavoriteConfirmAction!!
        val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
        val isAdding = !action.isCurrentlyFavorite

        val (catTitle, totalCatItems) = remember(action, isBn) {
            if (action.isTool) {
                val tool = com.example.data.model.ToolType.values().find { it.name == action.key }
                if (tool != null) {
                    val title = tool.category.getTitle(viewModel.selectedLanguage)
                    val count = com.example.data.model.ToolType.values().count { it.category == tool.category }
                    title to count
                } else ("" to 0)
            } else {
                val conv = com.example.data.model.ConverterType.values().find { it.name == action.key }
                if (conv != null) {
                    val title = conv.category.getTitle(viewModel.selectedLanguage)
                    val count = com.example.data.model.ConverterType.values().count { it.category == conv.category }
                    title to count
                } else ("" to 0)
            }
        }

        AlertDialog(
            onDismissRequest = { viewModel.dismissPendingFavoriteAction() },
            title = {
                Text(
                    text = if (isBn) "\"${action.titleBn}\" অপশন" else "\"${action.titleEn}\" Options",
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (isBn) {
                            if (isAdding) "\"${action.titleBn}\" কে প্রিয় তালিকায় যুক্ত করতে পারেন অথবা $catTitle ক্যাটাগরির সামনের সেরা ৪টি প্রিভিউতে পিন করতে পারেন।"
                            else "\"${action.titleBn}\" কে প্রিয় তালিকা থেকে সরাতে পারেন।"
                        } else {
                            if (isAdding) "Add \"${action.titleEn}\" to favorites or pin to $catTitle Top 4 preview."
                            else "Remove \"${action.titleEn}\" from your favorites list."
                        },
                        fontSize = 13.5.sp,
                        color = themeColors.displayText.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )

                    val maxPos = totalCatItems.coerceAtMost(4)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = themeColors.displayBackground,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = null,
                                    tint = themeColors.buttonEqualBg,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isBn) "📌 $catTitle ক্যাটাগরিতে পজিশন পিন করুন:" else "📌 Pin position in $catTitle:",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.buttonEqualBg
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    0 to if (isBn) "১ম স্থানে" else "Pos 1",
                                    1 to if (isBn) "২য় স্থানে" else "Pos 2",
                                    2 to if (isBn) "৩য় স্থানে" else "Pos 3",
                                    3 to if (isBn) "৪র্থ স্থানে" else "Pos 4"
                                ).take(maxPos.coerceAtLeast(1)).forEach { (pos, label) ->
                                    Button(
                                        onClick = {
                                            if (action.isTool) {
                                                val tool = com.example.data.model.ToolType.values().find { it.name == action.key }
                                                if (tool != null) viewModel.pinToolToCategoryTop4(tool, pos)
                                            } else {
                                                val conv = com.example.data.model.ConverterType.values().find { it.name == action.key }
                                                if (conv != null) viewModel.pinConverterToCategoryTop4(conv, pos)
                                            }
                                            viewModel.dismissPendingFavoriteAction()
                                            val posText = if (isBn) "${pos + 1} নম্বর" else "Pos ${pos + 1}"
                                            Toast.makeText(
                                                context,
                                                if (isBn) "\"${action.titleBn}\" $catTitle এর $posText পজিশনে পিন করা হয়েছে!" else "\"${action.titleEn}\" pinned to $catTitle $posText position!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = themeColors.buttonEqualBg.copy(alpha = 0.15f),
                                            contentColor = themeColors.buttonEqualBg
                                        ),
                                        contentPadding = PaddingValues(vertical = 6.dp, horizontal = 2.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmPendingFavoriteAction() }) {
                    Text(
                        text = if (isBn) {
                            if (isAdding) "⭐ প্রিয় তালিকায় যোগ" else "🗑️ প্রিয় তালিকা থেকে সরান"
                        } else {
                            if (isAdding) "⭐ Add to Favorites" else "🗑️ Remove Favorite"
                        },
                        color = if (isAdding) themeColors.buttonEqualBg else Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissPendingFavoriteAction() }) {
                    Text(
                        text = if (isBn) "বাতিল" else "Cancel",
                        color = themeColors.displayText
                    )
                }
            },
            containerColor = themeColors.cardBg,
            titleContentColor = themeColors.displayText,
            textContentColor = themeColors.displayText,
            shape = RoundedCornerShape(18.dp)
        )
    }

    if (viewModel.showDistrictSelectionDialog) {
        com.example.ui.islamic.DistrictSelectionSheet(
            viewModel = viewModel,
            themeColors = themeColors,
            onDismiss = { viewModel.showDistrictSelectionDialog = false }
        )
    }

    if (viewModel.showCalculatorDialog) {
        if (isCalcMinimized) {
            // Minimized Floating Bar (Windows Taskbar style bubble)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 85.dp, end = 16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Surface(
                    onClick = { isCalcMinimized = false },
                    shape = RoundedCornerShape(16.dp),
                    color = themeColors.cardBg,
                    shadowElevation = 10.dp,
                    modifier = Modifier.border(1.5.dp, themeColors.buttonEqualBg, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Quick Calculator",
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) "ক্যালকুলেটর (মিনিমাইজড)" else "Calculator (Minimized)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(themeColors.buttonEqualBg.copy(alpha = 0.15f))
                                .clickable { isCalcMinimized = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Restore",
                                tint = themeColors.buttonEqualBg,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE81123))
                                .clickable {
                                    showQuickCalcCloseConfirm = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        } else {
            Dialog(
                onDismissRequest = { viewModel.showCalculatorDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = if (isCalcMaximized) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier
                            .fillMaxWidth(0.96f)
                            .fillMaxHeight(0.90f)
                    },
                    shape = if (isCalcMaximized) RoundedCornerShape(0.dp) else RoundedCornerShape(24.dp),
                    color = themeColors.background,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        // Windows 11-Style Titlebar Header with Title and Minimize, Maximize, Close controls
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = themeColors.buttonEqualBg,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) "কুইক ক্যালকুলেটর" else "Quick Calculator",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText
                                )
                            }

                            // Windows 11 Controls Group (Minimize, Maximize/Restore, Close)
                            Windows11TitlebarButtons(
                                isMaximized = isCalcMaximized,
                                onMinimize = { isCalcMinimized = true },
                                onMaximizeToggle = { isCalcMaximized = !isCalcMaximized },
                                onClose = { showQuickCalcCloseConfirm = true },
                                themeColors = themeColors
                            )
                        }

                        HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.1f))

                        Spacer(modifier = Modifier.height(8.dp))

                        // Calculator Screen Content inside Dialog
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            CalculatorScreen(viewModel = viewModel, themeColors = themeColors)
                        }
                    }
                }
            }
        }
    }

    // Quick Calculator Exit Confirmation Dialog
    if (showQuickCalcCloseConfirm) {
        AlertDialog(
            onDismissRequest = { showQuickCalcCloseConfirm = false },
            title = {
                Text(
                    text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) "কুইক ক্যালকুলেটর বন্ধ করুন" else "Close Quick Calculator",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = themeColors.displayText
                )
            },
            text = {
                Text(
                    text = if (viewModel.selectedLanguage == AppLanguage.BENGALI)
                        "আপনি কি কুইক ক্যালকুলেটর বন্ধ করতে চান?"
                    else
                        "Do you want to close Quick Calculator?",
                    fontSize = 14.sp,
                    color = themeColors.displayText.copy(alpha = 0.85f)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showQuickCalcCloseConfirm = false
                        viewModel.showCalculatorDialog = false
                        isCalcMinimized = false
                        isCalcMaximized = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text(
                        text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) "হ্যাঁ" else "Yes",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showQuickCalcCloseConfirm = false }
                ) {
                    Text(
                        text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) "না" else "No",
                        color = themeColors.displayText
                    )
                }
            },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (viewModel.showCalendarDialog) {
        var isCalMaximized by remember { mutableStateOf(false) }
        var showCalCloseConfirm by remember { mutableStateOf(false) }
        val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

        if (isCalMinimized) {
            // Minimized Floating Pill for Quick Calendar
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (isCalcMinimized) 145.dp else 85.dp, end = 16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Surface(
                    onClick = { isCalMinimized = false },
                    shape = RoundedCornerShape(16.dp),
                    color = themeColors.cardBg,
                    shadowElevation = 10.dp,
                    modifier = Modifier.border(1.5.dp, themeColors.buttonEqualBg, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Quick Calendar",
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = if (isBn) "ক্যালেন্ডার (মিনিমাইজড)" else "Calendar (Minimized)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(themeColors.buttonEqualBg.copy(alpha = 0.15f))
                                .clickable { isCalMinimized = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Restore",
                                tint = themeColors.buttonEqualBg,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE81123))
                                .clickable {
                                    showCalCloseConfirm = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        } else {
            Dialog(
                onDismissRequest = { isCalMinimized = true },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = if (isCalMaximized) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier
                            .fillMaxWidth(0.96f)
                            .fillMaxHeight(0.92f)
                    },
                    shape = if (isCalMaximized) RoundedCornerShape(0.dp) else RoundedCornerShape(24.dp),
                    color = themeColors.background,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        // Titlebar with Window Controls
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(themeColors.buttonEqualBg.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = themeColors.buttonEqualBg,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Text(
                                    text = if (isBn) "কুইক ক্যালেন্ডার" else "Quick Calendar",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText
                                )
                            }

                            Windows11TitlebarButtons(
                                isMaximized = isCalMaximized,
                                onMinimize = { isCalMinimized = true },
                                onMaximizeToggle = { isCalMaximized = !isCalMaximized },
                                onClose = { showCalCloseConfirm = true },
                                themeColors = themeColors
                            )
                        }

                        HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.1f))

                        Spacer(modifier = Modifier.height(8.dp))

                        // Calendar Content inside Dialog with Vertical Scroll
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            com.example.ui.screens.MultiCalendarCard(
                                viewModel = viewModel,
                                themeColors = themeColors
                            )
                        }
                    }
                }
            }
        }

        // Quick Calendar Close Confirmation Dialog
        if (showCalCloseConfirm) {
            AlertDialog(
                onDismissRequest = { showCalCloseConfirm = false },
                title = {
                    Text(
                        text = if (isBn) "কুইক ক্যালেন্ডার বন্ধ করুন" else "Close Quick Calendar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = themeColors.displayText
                    )
                },
                text = {
                    Text(
                        text = if (isBn)
                            "আপনি কি কুইক ক্যালেন্ডার বন্ধ করতে চান?"
                        else
                            "Do you want to close Quick Calendar?",
                        fontSize = 14.sp,
                        color = themeColors.displayText.copy(alpha = 0.85f)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showCalCloseConfirm = false
                            viewModel.showCalendarDialog = false
                            isCalMinimized = false
                            isCalMaximized = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text(
                            text = if (isBn) "হ্যাঁ" else "Yes",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showCalCloseConfirm = false }
                    ) {
                        Text(
                            text = if (isBn) "না" else "No",
                            color = themeColors.displayText
                        )
                    }
                },
                containerColor = themeColors.cardBg,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }

    if (viewModel.showMarketDialog) {
        var isMarketMaximized by remember { mutableStateOf(false) }
        var showMarketCloseConfirm by remember { mutableStateOf(false) }
        val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

        if (isMarketMinimized) {
            // Minimized Floating Pill for Quick Market List
            val bottomPadding = when {
                isCalcMinimized && isCalMinimized -> 205.dp
                isCalcMinimized || isCalMinimized -> 145.dp
                else -> 85.dp
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = bottomPadding, end = 16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Surface(
                    onClick = { isMarketMinimized = false },
                    shape = RoundedCornerShape(16.dp),
                    color = themeColors.cardBg,
                    shadowElevation = 10.dp,
                    modifier = Modifier.border(1.5.dp, themeColors.buttonEqualBg, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBasket,
                            contentDescription = "Quick Market List",
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = if (isBn) "বাজার ফর্দ (মিনিমাইজড)" else "Market List (Minimized)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(themeColors.buttonEqualBg.copy(alpha = 0.15f))
                                .clickable { isMarketMinimized = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Restore",
                                tint = themeColors.buttonEqualBg,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE81123))
                                .clickable {
                                    showMarketCloseConfirm = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        } else {
            Dialog(
                onDismissRequest = { isMarketMinimized = true },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = if (isMarketMaximized) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier
                            .fillMaxWidth(0.96f)
                            .fillMaxHeight(0.94f)
                    },
                    shape = if (isMarketMaximized) RoundedCornerShape(0.dp) else RoundedCornerShape(24.dp),
                    color = themeColors.background,
                    tonalElevation = 8.dp,
                    shadowElevation = 16.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        // Header Bar (Windows 11 Style)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(themeColors.buttonEqualBg.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingBasket,
                                        contentDescription = null,
                                        tint = themeColors.buttonEqualBg,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Text(
                                    text = if (isBn) "কুইক বাজার ফর্দ" else "Quick Market List",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText
                                )
                            }

                            Windows11TitlebarButtons(
                                isMaximized = isMarketMaximized,
                                onMinimize = { isMarketMinimized = true },
                                onMaximizeToggle = { isMarketMaximized = !isMarketMaximized },
                                onClose = { showMarketCloseConfirm = true },
                                themeColors = themeColors
                            )
                        }

                        HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.1f))

                        Spacer(modifier = Modifier.height(4.dp))

                        // Market List Content inside Dialog
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            com.example.ui.screens.MarketListScreen(
                                viewModel = viewModel,
                                themeColors = themeColors,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        // Quick Market List Close Confirmation Dialog
        if (showMarketCloseConfirm) {
            AlertDialog(
                onDismissRequest = { showMarketCloseConfirm = false },
                title = {
                    Text(
                        text = if (isBn) "কুইক বাজার ফর্দ বন্ধ করুন" else "Close Quick Market List",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = themeColors.displayText
                    )
                },
                text = {
                    Text(
                        text = if (isBn)
                            "আপনি কি কুইক বাজার ফর্দ বন্ধ করতে চান?"
                        else
                            "Do you want to close Quick Market List?",
                        fontSize = 14.sp,
                        color = themeColors.displayText.copy(alpha = 0.85f)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showMarketCloseConfirm = false
                            viewModel.showMarketDialog = false
                            isMarketMinimized = false
                            isMarketMaximized = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text(
                            text = if (isBn) "হ্যাঁ" else "Yes",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showMarketCloseConfirm = false }
                    ) {
                        Text(
                            text = if (isBn) "না" else "No",
                            color = themeColors.displayText
                        )
                    }
                },
                containerColor = themeColors.cardBg,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
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
            kotlinx.coroutines.delay(10000L)
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

private fun launchUriSafely(context: android.content.Context, uriString: String) {
    try {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(uriString)).apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            android.widget.Toast.makeText(context, "Could not open browser. Please visit manually.", android.widget.Toast.LENGTH_SHORT).show()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}

@Composable
fun FavoritesDialog(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit
) {
    var itemToDelete by remember { mutableStateOf<Pair<String, Boolean>?>(null) }

    if (itemToDelete != null) {
        val (name, isConv) = itemToDelete!!
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = {
                Text(
                    text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) "ফেভারিট মুছে ফেলা" else "Remove Favorite",
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
            },
            text = {
                Text(
                    text = if (viewModel.selectedLanguage == AppLanguage.BENGALI)
                        "আপনি কি নিশ্চিত যে এই আইটেমটি ফেভারিট তালিকা থেকে মুছে ফেলতে চান?"
                    else
                        "Are you sure you want to remove this item from your favorites?",
                    color = themeColors.displayText.copy(alpha = 0.85f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isConv) {
                            viewModel.toggleFavoriteConverter(name)
                        } else {
                            viewModel.toggleFavoriteTool(name)
                        }
                        itemToDelete = null
                    }
                ) {
                    Text(
                        text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) "হ্যাঁ, মুছুন" else "Remove",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { itemToDelete = null }
                ) {
                    Text(
                        text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) "বাতিল" else "Cancel",
                        color = themeColors.displayText
                    )
                }
            },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = themeColors.background,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) "আপনার ফেভারিটস" else "Your Favorites",
                        color = themeColors.displayText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = themeColors.displayText
                        )
                    }
                }

                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (viewModel.favoriteConverters.isEmpty() && viewModel.favoriteTools.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) "কোনো ফেভারিট যুক্ত করা হয়নি।" else "No favorites added yet.",
                                    color = themeColors.displayText.copy(alpha = 0.6f),
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }

                    if (viewModel.favoriteConverters.isNotEmpty()) {
                        item {
                            Text(
                                text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) "কনভার্টার" else "Converters",
                                color = themeColors.buttonEqualBg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                            )
                        }
                        items(viewModel.favoriteConverters.toList()) { convName ->
                            val converter = try { ConverterType.valueOf(convName) } catch (e: Exception) { null }
                            if (converter != null) {
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.elevatedCardColors(containerColor = themeColors.cardBg),
                                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.activeTab = 1
                                                viewModel.openConverter(converter)
                                                onDismiss()
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(themeColors.buttonEqualBg.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = converter.icon,
                                                contentDescription = null,
                                                tint = themeColors.buttonEqualBg,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) converter.titleBn else converter.titleEn,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = themeColors.displayText
                                            )
                                            Text(
                                                text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) converter.category.titleBn else converter.category.titleEn,
                                                fontSize = 12.sp,
                                                color = themeColors.displayText.copy(alpha = 0.65f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        IconButton(onClick = { itemToDelete = Pair(convName, true) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = Color(0xFFEF4444)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (viewModel.favoriteTools.isNotEmpty()) {
                        item {
                            Text(
                                text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) "টুলস" else "Tools",
                                color = themeColors.buttonEqualBg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                            )
                        }
                        items(viewModel.favoriteTools.toList()) { toolName ->
                            val tool = try { ToolType.valueOf(toolName) } catch (e: Exception) { null }
                            if (tool != null) {
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.elevatedCardColors(containerColor = themeColors.cardBg),
                                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.activeTab = 0
                                                viewModel.openTool(tool)
                                                onDismiss()
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(themeColors.buttonEqualBg.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = tool.icon,
                                                contentDescription = null,
                                                tint = themeColors.buttonEqualBg,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) tool.titleBn else tool.titleEn,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = themeColors.displayText
                                            )
                                            Text(
                                                text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) tool.category.titleBn else tool.category.titleEn,
                                                fontSize = 12.sp,
                                                color = themeColors.displayText.copy(alpha = 0.65f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        IconButton(onClick = { itemToDelete = Pair(toolName, false) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = Color(0xFFEF4444)
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

private fun launchEmailSafely(context: android.content.Context, email: String, subject: String, body: String = "") {
    try {
        val uriString = "mailto:$email?subject=${android.net.Uri.encode(subject)}&body=${android.net.Uri.encode(body)}"
        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO, android.net.Uri.parse(uriString)).apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "plain/text"
                putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(android.content.Intent.EXTRA_SUBJECT, subject)
                putExtra(android.content.Intent.EXTRA_TEXT, body)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(android.content.Intent.createChooser(intent, "Send Email"))
        } catch (ex: Exception) {
            android.widget.Toast.makeText(context, "No email client found.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun FabGradientEditorDialog(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var colors by remember { mutableStateOf(viewModel.fabGradientHexColors.toList()) }
    var selectedIndex by remember { mutableStateOf(0) }
    var currentHsv by remember { 
        mutableStateOf(Triple(0f, 1f, 1f))
    }
    
    // Convert hex to HSV when selection changes
    LaunchedEffect(selectedIndex, colors) {
        if (selectedIndex in colors.indices) {
            try {
                val hex = colors[selectedIndex]
                val hsvArr = FloatArray(3)
                android.graphics.Color.colorToHSV(android.graphics.Color.parseColor(hex), hsvArr)
                currentHsv = Triple(hsvArr[0], hsvArr[1], hsvArr[2])
            } catch (e: Exception) {}
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = themeColors.background,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) "এআই বোতামের রং পরিবর্তন" else "Edit AI Button Colors",
                    color = themeColors.displayText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // List of colors
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                ) {
                    items(colors.size) { index ->
                        val hex = colors[index]
                        val isSelected = index == selectedIndex
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Gray }
                                )
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .clickable { selectedIndex = index }
                        )
                    }
                }

                // Premium Color Wheel
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    val down = awaitFirstDown()
                                    val bounds = this.size
                                    val radius = bounds.width / 2f
                                    val center = Offset(radius, radius)
                                    
                                    fun updateFromOffset(offset: Offset) {
                                        val d = offset - center
                                        val r = Math.sqrt((d.x * d.x + d.y * d.y).toDouble()).toFloat()
                                        val sat = (r / radius).coerceIn(0f, 1f)
                                        
                                        var angleDeg = Math.toDegrees(Math.atan2(d.y.toDouble(), d.x.toDouble())).toFloat()
                                        if (angleDeg < 0) angleDeg += 360f
                                        
                                        currentHsv = Triple(angleDeg, sat, currentHsv.third)
                                        val updatedColor = Color.hsv(angleDeg, sat, currentHsv.third)
                                        val hexStr = String.format("#%06X", 0xFFFFFF and updatedColor.toArgb())
                                        val mutColors = colors.toMutableList()
                                        if (selectedIndex in mutColors.indices) {
                                            mutColors[selectedIndex] = hexStr
                                            colors = mutColors
                                        }
                                    }
                                    
                                    updateFromOffset(down.position)
                                    
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val drag = event.changes.firstOrNull { it.pressed } ?: break
                                        updateFromOffset(drag.position)
                                        drag.consume()
                                    }
                                }
                            }
                    ) {
                        val canvasBounds = this.size
                        val radius = canvasBounds.width / 2f
                        val center = Offset(radius, radius)
                        
                        // Hue Sweep
                        val hues = (0..360).map { Color.hsv(it.toFloat(), 1f, 1f) }
                        drawCircle(
                            brush = Brush.sweepGradient(hues, center),
                            radius = radius
                        )
                        
                        // Saturation Fade (White in center)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White, Color.Transparent),
                                center = center,
                                radius = radius
                            ),
                            radius = radius
                        )
                        
                        // Indicator
                        val angleRad = Math.toRadians(currentHsv.first.toDouble())
                        val r = (currentHsv.second * radius).toDouble()
                        val ix = center.x + (r * Math.cos(angleRad)).toFloat()
                        val iy = center.y + (r * Math.sin(angleRad)).toFloat()
                        
                        drawCircle(
                            color = Color.White,
                            radius = 24f,
                            center = Offset(ix, iy)
                        )
                        drawCircle(
                            color = Color.Black,
                            radius = 24f,
                            center = Offset(ix, iy),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            viewModel.updateFabColors(listOf("#4285F4", "#9B51E0", "#EA4335", "#FBBC05", "#34A853"))
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (viewModel.selectedLanguage == AppLanguage.BENGALI) "রিসেট করুন" else "Reset",
                            color = Color(0xFFD32F2F)
                        )
                    }
                    Button(
                        onClick = {
                            viewModel.updateFabColors(colors)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (viewModel.selectedLanguage == AppLanguage.BENGALI) "সংরক্ষণ করুন" else "Save", color = themeColors.buttonEqualText)
                    }
                }
            }
        }
    }
}

@Composable
fun CenterSearchFabCustomizerDialog(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit
) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    var showColorWheelFor by remember { mutableStateOf<String?>(null) } // "bg", "border", "icon"
    
    val bgHex = viewModel.centerSearchFabBgColorHex
    val borderHex = viewModel.centerSearchFabBorderColorHex
    val iconHex = viewModel.centerSearchFabIconColorHex
    
    val defaultBg = Color.White
    val defaultBorder = themeColors.buttonEqualBg
    val defaultIcon = themeColors.buttonEqualBg
    
    val currentBgColor = remember(bgHex) {
        if (bgHex.isNotEmpty()) {
            try { Color(android.graphics.Color.parseColor(bgHex)) } catch (e: Exception) { defaultBg }
        } else { defaultBg }
    }
    
    val currentBorderColor = remember(borderHex) {
        if (borderHex.isNotEmpty()) {
            try { Color(android.graphics.Color.parseColor(borderHex)) } catch (e: Exception) { defaultBorder }
        } else { defaultBorder }
    }
    
    val currentIconColor = remember(iconHex) {
        if (iconHex.isNotEmpty()) {
            try { Color(android.graphics.Color.parseColor(iconHex)) } catch (e: Exception) { defaultIcon }
        } else { defaultIcon }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = themeColors.background,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isBn) "সার্চ বাটন কাস্টমাইজ" else "Customize Search Button",
                    color = themeColors.displayText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Divider(color = themeColors.displayText.copy(alpha = 0.1f))
                
                // Color Selectors
                ColorRowOption(
                    label = if (isBn) "ব্যাকগ্রাউন্ড কালার" else "Background Color",
                    color = currentBgColor,
                    onClick = { showColorWheelFor = "bg" },
                    themeColors = themeColors
                )
                
                ColorRowOption(
                    label = if (isBn) "বর্ডার কালার" else "Border Color",
                    color = currentBorderColor,
                    onClick = { showColorWheelFor = "border" },
                    themeColors = themeColors
                )
                
                ColorRowOption(
                    label = if (isBn) "আইকন কালার" else "Icon Color",
                    color = currentIconColor,
                    onClick = { showColorWheelFor = "icon" },
                    themeColors = themeColors
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            viewModel.updateCenterSearchFabColors("", "", "")
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isBn) "রিসেট করুন" else "Reset",
                            color = Color(0xFFD32F2F)
                        )
                    }
                    
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isBn) "সম্পন্ন" else "Done",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
    
    if (showColorWheelFor != null) {
        val target = showColorWheelFor!!
        val title = when (target) {
            "bg" -> if (isBn) "ব্যাকগ্রাউন্ড কালার নির্বাচন" else "Select Background Color"
            "border" -> if (isBn) "বর্ডার কালার নির্বাচন" else "Select Border Color"
            else -> if (isBn) "আইকন কালার নির্বাচন" else "Select Icon Color"
        }
        val initial = when (target) {
            "bg" -> currentBgColor
            "border" -> currentBorderColor
            else -> currentIconColor
        }
        
        com.example.ui.components.ColorWheelPickerDialog(
            title = title,
            initialColor = initial,
            onColorSelected = { color ->
                val hex = String.format("#%08X", color.toArgb())
                when (target) {
                    "bg" -> viewModel.updateCenterSearchFabColors(hex, borderHex, iconHex)
                    "border" -> viewModel.updateCenterSearchFabColors(bgHex, hex, iconHex)
                    "icon" -> viewModel.updateCenterSearchFabColors(bgHex, borderHex, hex)
                }
            },
            onDismiss = { showColorWheelFor = null },
            themeColors = themeColors,
            isBn = isBn
        )
    }
}

@Composable
fun NavbarStartButtonCustomizerDialog(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit
) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    var showColorWheelFor by remember { mutableStateOf<String?>(null) } // "bg", "border", "icon"
    
    val bgHex = viewModel.searchFabBgColorHex
    val borderHex = viewModel.searchFabBorderColorHex
    val iconHex = viewModel.searchFabIconColorHex
    
    val defaultBg = Color.White.copy(alpha = 0.25f)
    val defaultBorder = Color.Transparent
    val defaultIcon = Color.White
    
    val currentBgColor = remember(bgHex) {
        if (bgHex.isNotEmpty()) {
            try { Color(android.graphics.Color.parseColor(bgHex)) } catch (e: Exception) { defaultBg }
        } else { defaultBg }
    }
    
    val currentBorderColor = remember(borderHex) {
        if (borderHex.isNotEmpty()) {
            try { Color(android.graphics.Color.parseColor(borderHex)) } catch (e: Exception) { defaultBorder }
        } else { defaultBorder }
    }
    
    val currentIconColor = remember(iconHex) {
        if (iconHex.isNotEmpty()) {
            try { Color(android.graphics.Color.parseColor(iconHex)) } catch (e: Exception) { defaultIcon }
        } else { defaultIcon }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = themeColors.background,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isBn) "স্টার্ট বোতাম কাস্টমাইজ" else "Customize Start Button",
                    color = themeColors.displayText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Divider(color = themeColors.displayText.copy(alpha = 0.1f))
                
                ColorRowOption(
                    label = if (isBn) "ব্যাকগ্রাউন্ড কালার" else "Background Color",
                    color = currentBgColor,
                    onClick = { showColorWheelFor = "bg" },
                    themeColors = themeColors
                )
                
                ColorRowOption(
                    label = if (isBn) "বর্ডার কালার" else "Border Color",
                    color = if (borderHex.isNotEmpty()) currentBorderColor else Color.Transparent,
                    onClick = { showColorWheelFor = "border" },
                    themeColors = themeColors,
                    showNone = borderHex.isEmpty()
                )
                
                ColorRowOption(
                    label = if (isBn) "আইকন কালার" else "Icon Color",
                    color = currentIconColor,
                    onClick = { showColorWheelFor = "icon" },
                    themeColors = themeColors
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            viewModel.updateSearchFabColors("", "", "")
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isBn) "রিসেট করুন" else "Reset",
                            color = Color(0xFFD32F2F)
                        )
                    }
                    
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isBn) "সম্পন্ন" else "Done",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
    
    if (showColorWheelFor != null) {
        val target = showColorWheelFor!!
        val title = when (target) {
            "bg" -> if (isBn) "ব্যাকগ্রাউন্ড কালার নির্বাচন" else "Select Background Color"
            "border" -> if (isBn) "বর্ডার কালার নির্বাচন" else "Select Border Color"
            else -> if (isBn) "আইকন কালার নির্বাচন" else "Select Icon Color"
        }
        val initial = when (target) {
            "bg" -> currentBgColor
            "border" -> if (borderHex.isNotEmpty()) currentBorderColor else Color.White
            else -> currentIconColor
        }
        
        com.example.ui.components.ColorWheelPickerDialog(
            title = title,
            initialColor = initial,
            onColorSelected = { color ->
                val hex = String.format("#%08X", color.toArgb())
                when (target) {
                    "bg" -> viewModel.updateSearchFabColors(hex, borderHex, iconHex)
                    "border" -> viewModel.updateSearchFabColors(bgHex, hex, iconHex)
                    "icon" -> viewModel.updateSearchFabColors(bgHex, borderHex, hex)
                }
            },
            onDismiss = { showColorWheelFor = null },
            themeColors = themeColors,
            isBn = isBn
        )
    }
}

@Composable
fun AiFabCustomizerDialog(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit
) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    var showColorWheelFor by remember { mutableStateOf<String?>(null) } // "bg", "icon", "grad_0".."grad_4"
    
    val bgHex = viewModel.aiFabBgColorHex
    val iconHex = viewModel.aiFabIconColorHex
    val gradHex = viewModel.aiFabGradientColorsHex
    
    val defaultBg = if (themeColors.isDark) Color(0xFF1E293B) else Color.White
    val defaultIcon = Color(0xFF4285F4)
    
    val currentBgColor = remember(bgHex) {
        if (bgHex.isNotEmpty()) {
            try { Color(android.graphics.Color.parseColor(bgHex)) } catch (e: Exception) { defaultBg }
        } else { defaultBg }
    }
    
    val currentIconColor = remember(iconHex) {
        if (iconHex.isNotEmpty()) {
            try { Color(android.graphics.Color.parseColor(iconHex)) } catch (e: Exception) { defaultIcon }
        } else { defaultIcon }
    }
    
    val gradientColorsHexList = remember(gradHex) {
        if (gradHex.isNotEmpty()) gradHex.split(",").map { it.trim() } else listOf("#4285F4", "#9B51E0", "#EA4335", "#FBBC05", "#34A853")
    }
    
    val gradientColors = remember(gradientColorsHexList) {
        gradientColorsHexList.map { hex ->
            try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Gray }
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = themeColors.background,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isBn) "এআই চ্যাট বাটন কাস্টমাইজ" else "Customize AI Chat Button",
                    color = themeColors.displayText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Divider(color = themeColors.displayText.copy(alpha = 0.1f))
                
                ColorRowOption(
                    label = if (isBn) "ব্যাকগ্রাউন্ড কালার" else "Background Color",
                    color = currentBgColor,
                    onClick = { showColorWheelFor = "bg" },
                    themeColors = themeColors
                )
                
                ColorRowOption(
                    label = if (isBn) "আইকন কালার" else "Icon Color",
                    color = currentIconColor,
                    onClick = { showColorWheelFor = "icon" },
                    themeColors = themeColors
                )
                
                Divider(color = themeColors.displayText.copy(alpha = 0.1f))
                
                Text(
                    text = if (isBn) "বর্ডার গ্রাডিয়েন্ট কালারসমূহ" else "Border Gradient Colors",
                    color = themeColors.displayText.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (index in 0..4) {
                        val colorVal = gradientColors.getOrNull(index) ?: Color.Gray
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(colorVal)
                                .border(
                                    width = 2.dp,
                                    color = themeColors.displayText.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    showColorWheelFor = "grad_$index"
                                }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            viewModel.updateAiFabColors("", "", "")
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isBn) "রিসেট করুন" else "Reset",
                            color = Color(0xFFD32F2F)
                        )
                    }
                    
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isBn) "সম্পন্ন" else "Done",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
    
    if (showColorWheelFor != null) {
        val target = showColorWheelFor!!
        val isGrad = target.startsWith("grad_")
        val gradIndex = if (isGrad) target.substring(5).toIntOrNull() ?: 0 else 0
        
        val title = if (isGrad) {
            if (isBn) "গ্রাডিয়েন্ট কালার ${gradIndex + 1} নির্বাচন" else "Select Gradient Color ${gradIndex + 1}"
        } else if (target == "bg") {
            if (isBn) "ব্যাকগ্রাউন্ড কালার নির্বাচন" else "Select Background Color"
        } else {
            if (isBn) "আইকন কালার নির্বাচন" else "Select Icon Color"
        }
        
        val initial = if (isGrad) {
            gradientColors.getOrNull(gradIndex) ?: Color.Gray
        } else if (target == "bg") {
            currentBgColor
        } else {
            currentIconColor
        }
        
        com.example.ui.components.ColorWheelPickerDialog(
            title = title,
            initialColor = initial,
            onColorSelected = { color ->
                val hex = String.format("#%08X", color.toArgb())
                if (isGrad) {
                    val newList = gradientColorsHexList.toMutableList()
                    while (newList.size <= gradIndex) { newList.add("#FFFFFF") }
                    newList[gradIndex] = hex
                    val updatedGradStr = newList.joinToString(",")
                    viewModel.updateAiFabColors(bgHex, iconHex, updatedGradStr)
                } else {
                    if (target == "bg") viewModel.updateAiFabColors(hex, iconHex, gradHex)
                    else viewModel.updateAiFabColors(bgHex, hex, gradHex)
                }
            },
            onDismiss = { showColorWheelFor = null },
            themeColors = themeColors,
            isBn = isBn
        )
    }
}

@Composable
fun ColorRowOption(
    label: String,
    color: Color,
    onClick: () -> Unit,
    themeColors: CalculatorThemeColors,
    showNone: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(themeColors.cardBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = themeColors.displayText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (showNone) Color.Transparent else color)
                .border(2.dp, themeColors.displayText.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (showNone) {
                Divider(
                    color = Color.Red,
                    modifier = Modifier.rotate(45f)
                )
            }
        }
    }
}

@Composable
fun UnfavoriteConfirmDialog(
    itemType: String, // "Tool" or "Converter"
    itemNameBn: String,
    itemNameEn: String,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBn) "প্রিয় তালিকা থেকে সরান" else "Remove from Favorites",
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )
        },
        text = {
            Text(
                text = if (isBn) "$itemNameBn -কে প্রিয় তালিকা থেকে সরাতে চান?" else "Remove $itemNameEn from favorites?",
                color = themeColors.displayText.copy(alpha = 0.8f)
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = if (isBn) "সরান" else "Remove",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = if (isBn) "বাতিল" else "Cancel",
                    color = themeColors.displayText
                )
            }
        },
        containerColor = themeColors.cardBg,
        titleContentColor = themeColors.displayText,
        textContentColor = themeColors.displayText
    )
}

@Composable
fun Windows11TitlebarButtons(
    isMaximized: Boolean,
    onMinimize: () -> Unit,
    onMaximizeToggle: () -> Unit,
    onClose: () -> Unit,
    themeColors: com.example.ui.theme.CalculatorThemeColors
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1. Minimize Button (-)
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(themeColors.displayText.copy(alpha = 0.08f))
                .clickable(onClick = onMinimize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(10.dp)) {
                drawLine(
                    color = themeColors.displayText,
                    start = Offset(0f, size.height / 2f),
                    end = Offset(size.width, size.height / 2f),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        // 2. Maximize / Restore Button (▢ / ❐)
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(themeColors.displayText.copy(alpha = 0.08f))
                .clickable(onClick = onMaximizeToggle),
            contentAlignment = Alignment.Center
        ) {
            if (!isMaximized) {
                // Single square for Maximize
                Canvas(modifier = Modifier.size(10.dp)) {
                    drawRect(
                        color = themeColors.displayText,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                    )
                }
            } else {
                // Overlapping squares for Restore
                Canvas(modifier = Modifier.size(11.dp)) {
                    // Back square
                    drawRect(
                        color = themeColors.displayText,
                        topLeft = Offset(size.width * 0.25f, 0f),
                        size = androidx.compose.ui.geometry.Size(size.width * 0.75f, size.height * 0.75f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2.dp.toPx())
                    )
                    // Front square
                    drawRect(
                        color = themeColors.displayText,
                        topLeft = Offset(0f, size.height * 0.25f),
                        size = androidx.compose.ui.geometry.Size(size.width * 0.75f, size.height * 0.75f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2.dp.toPx())
                    )
                }
            }
        }

        // 3. Close Button (✕) with Windows Red Accent
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFE81123))
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
