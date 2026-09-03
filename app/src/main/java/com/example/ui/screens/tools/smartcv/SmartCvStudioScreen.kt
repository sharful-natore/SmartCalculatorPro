package com.example.ui.screens.tools.smartcv

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.ui.screens.tools.CvData
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartCvStudioScreen(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    val scope = rememberCoroutineScope()

    // Profiles state
    var profiles by remember { mutableStateOf(SmartCvStorage.loadProfiles(context)) }
    var activeProfileId by remember {
        mutableStateOf(
            SmartCvStorage.loadActiveProfileId(context).ifBlank {
                profiles.firstOrNull()?.id ?: ""
            }
        )
    }

    val activeProfile = remember(profiles, activeProfileId) {
        profiles.firstOrNull { it.id == activeProfileId } ?: profiles.firstOrNull() ?: CvData()
    }

    // Active Tab in Studio
    var activeTab by remember { mutableStateOf(SmartCvStudioTab.CANVAS) }

    // Active Template
    var selectedTemplate by remember { mutableStateOf(SmartCvTemplate.HARVARD_CLASSIC) }

    // Rendered Bitmaps & Page budget state
    var renderedPages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var isRenderingPreview by remember { mutableStateOf(false) }
    var pageBudget by remember { mutableStateOf<SmartPageBudget?>(null) }
    var currentPdfFile by remember { mutableStateOf<File?>(null) }

    // Modal state for Google XYZ AI
    var xyzModalRawText by remember { mutableStateOf<String?>(null) }
    var xyzModalCallback by remember { mutableStateOf<((String) -> Unit)?>(null) }

    // Dialogs
    var showExportDialog by remember { mutableStateOf(false) }
    var showProfileManagerDialog by remember { mutableStateOf(false) }

    // Function to update profile
    fun updateActiveProfile(newProfile: CvData) {
        val updatedList = profiles.map { if (it.id == newProfile.id) newProfile else it }
        profiles = updatedList
        SmartCvStorage.saveProfiles(context, updatedList)
    }

    // Effect to regenerate preview whenever CV Data or Template changes
    LaunchedEffect(activeProfile, selectedTemplate) {
        isRenderingPreview = true
        withContext(Dispatchers.IO) {
            try {
                val pdf = SmartCvPdfEngine.generatePdf(context, activeProfile, selectedTemplate)
                currentPdfFile = pdf
                val bitmaps = SmartCvPdfEngine.renderAllPagesToBitmaps(pdf)
                val budget = SmartCvPdfEngine.calculatePageBudget(context, activeProfile, selectedTemplate)
                withContext(Dispatchers.Main) {
                    renderedPages = bitmaps
                    pageBudget = budget
                    isRenderingPreview = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    isRenderingPreview = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            SmartCvTopBar(
                candidateName = activeProfile.fullName.ifBlank { if (isBn) "প্রার্থী" else "Candidate" },
                profileLabel = activeProfile.profileLabel,
                isBn = isBn,
                themeColors = themeColors,
                onBackClick = onBackClick,
                onOpenProfiles = { showProfileManagerDialog = true },
                onExportClick = { showExportDialog = true }
            )
        },
        bottomBar = {
            SmartCvStudioBottomDock(
                activeTab = activeTab,
                onTabSelect = { activeTab = it },
                themeColors = themeColors,
                isBn = isBn
            )
        },
        containerColor = themeColors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Live Page Budget Bar
            pageBudget?.let { budget ->
                SmartPageBudgetBar(
                    budget = budget,
                    isBn = isBn,
                    themeColors = themeColors,
                    onSwitchToCanvas = { activeTab = SmartCvStudioTab.CANVAS }
                )
            }

            // Main Content Area depending on Tab
            Box(modifier = Modifier.weight(1f)) {
                when (activeTab) {
                    SmartCvStudioTab.CANVAS -> {
                        SmartCvCanvasView(
                            pages = renderedPages,
                            isRendering = isRenderingPreview,
                            selectedTemplate = selectedTemplate,
                            onTemplateSelect = { selectedTemplate = it },
                            onExportClick = { showExportDialog = true },
                            onShareClick = {
                                currentPdfFile?.let { file ->
                                    sharePdfFile(context, file, isBn)
                                }
                            },
                            themeColors = themeColors,
                            isBn = isBn
                        )
                    }
                    SmartCvStudioTab.STRUCTURE -> {
                        SmartCvStructureTab(
                            cvData = activeProfile,
                            onCvDataChange = { updateActiveProfile(it) },
                            themeColors = themeColors,
                            isBn = isBn,
                            onOpenXyzDialog = { raw, cb ->
                                xyzModalRawText = raw
                                xyzModalCallback = cb
                            }
                        )
                    }
                    SmartCvStudioTab.STYLE -> {
                        SmartCvStyleTab(
                            cvData = activeProfile,
                            onCvDataChange = { updateActiveProfile(it) },
                            selectedTemplate = selectedTemplate,
                            onTemplateSelect = { selectedTemplate = it },
                            themeColors = themeColors,
                            isBn = isBn
                        )
                    }
                    SmartCvStudioTab.AI_COPILOT -> {
                        SmartCvAiCopilotTab(
                            cvData = activeProfile,
                            onApplyTailoredSummary = { tailored ->
                                updateActiveProfile(activeProfile.copy(summary = tailored))
                                Toast.makeText(context, if (isBn) "সামারি সিভিতে সফলভাবে যুক্ত হয়েছে!" else "Tailored summary applied to CV!", Toast.LENGTH_SHORT).show()
                            },
                            themeColors = themeColors,
                            isBn = isBn
                        )
                    }
                }
            }
        }
    }

    // Google XYZ AI Modal
    xyzModalRawText?.let { raw ->
        SmartGoogleXyzModal(
            rawText = raw,
            targetRole = activeProfile.jobTitle,
            onApply = { chosenVariation ->
                xyzModalCallback?.invoke(chosenVariation)
                xyzModalRawText = null
                xyzModalCallback = null
            },
            onDismiss = {
                xyzModalRawText = null
                xyzModalCallback = null
            },
            themeColors = themeColors,
            isBn = isBn
        )
    }

    // PDF Export Dialog
    if (showExportDialog) {
        SmartPdfExportDialog(
            pdfFile = currentPdfFile,
            candidateName = activeProfile.fullName,
            templateName = selectedTemplate.nameEn,
            profileLabel = activeProfile.profileLabel,
            onDismiss = { showExportDialog = false },
            themeColors = themeColors,
            isBn = isBn
        )
    }

    // Profile Manager Dialog
    if (showProfileManagerDialog) {
        SmartProfileManagerDialog(
            profiles = profiles,
            activeId = activeProfileId,
            onSelect = { id ->
                activeProfileId = id
                SmartCvStorage.saveActiveProfileId(context, id)
                showProfileManagerDialog = false
            },
            onCreateNew = { name ->
                val newP = CvData(id = UUID.randomUUID().toString(), profileLabel = name, fullName = activeProfile.fullName)
                val list = profiles + newP
                profiles = list
                activeProfileId = newP.id
                SmartCvStorage.saveProfiles(context, list)
                SmartCvStorage.saveActiveProfileId(context, newP.id)
                showProfileManagerDialog = false
            },
            onDelete = { id ->
                if (profiles.size > 1) {
                    val list = profiles.filter { it.id != id }
                    profiles = list
                    if (activeProfileId == id) activeProfileId = list.first().id
                    SmartCvStorage.saveProfiles(context, list)
                    SmartCvStorage.saveActiveProfileId(context, activeProfileId)
                }
            },
            onDismiss = { showProfileManagerDialog = false },
            themeColors = themeColors,
            isBn = isBn
        )
    }
}

// =============================================================================
// TOP BAR
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartCvTopBar(
    candidateName: String,
    profileLabel: String,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onBackClick: () -> Unit,
    onOpenProfiles: () -> Unit,
    onExportClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isBn) "স্মার্ট সিভি বিল্ডার" else "Smart CV Studio",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = themeColors.displayText
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = themeColors.buttonEqualBg.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "NEXT-GEN",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = themeColors.buttonEqualBg,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "$profileLabel • $candidateName",
                        fontSize = 11.sp,
                        color = themeColors.displayText.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = themeColors.displayText)
            }
        },
        actions = {
            // Profile switcher button
            IconButton(onClick = onOpenProfiles) {
                Icon(imageVector = Icons.Default.SwitchAccount, contentDescription = "Switch Profile", tint = themeColors.displayText)
            }
            // PDF Export button
            FilledTonalButton(
                onClick = onExportClick,
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = themeColors.buttonEqualBg),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = themeColors.buttonEqualText, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isBn) "এক্সপোর্ট" else "Export", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.buttonEqualText)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = themeColors.background)
    )
}

// =============================================================================
// LIVE PAGE BUDGET BAR
// =============================================================================

@Composable
fun SmartPageBudgetBar(
    budget: SmartPageBudget,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onSwitchToCanvas: () -> Unit
) {
    val barColor = if (budget.isOverflowWarning) Color(0xFFD97706) else Color(0xFF16A34A)
    val bgColor = if (budget.isOverflowWarning) Color(0xFFFEF3C7) else Color(0xFFDCFCE7)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onSwitchToCanvas() },
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = BorderStroke(1.dp, barColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (budget.isOverflowWarning) Icons.Default.WarningAmber else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = barColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isBn) budget.statusTextBn else budget.statusTextEn,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = barColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// =============================================================================
// STUDIO BOTTOM DOCK
// =============================================================================

@Composable
fun SmartCvStudioBottomDock(
    activeTab: SmartCvStudioTab,
    onTabSelect: (SmartCvStudioTab) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = themeColors.background,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmartCvStudioTab.values().forEach { tab ->
                val isSelected = activeTab == tab
                val title = if (isBn) tab.titleBn else tab.titleEn

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTabSelect(tab) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) themeColors.buttonEqualBg.copy(alpha = 0.2f) else Color.Transparent)
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = title,
                            tint = if (isSelected) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = title,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

// =============================================================================
// TAB 1: CANVAS VIEW WITH INTERACTIVE PREVIEW
// =============================================================================

@Composable
fun SmartCvCanvasView(
    pages: List<Bitmap>,
    isRendering: Boolean,
    selectedTemplate: SmartCvTemplate,
    onTemplateSelect: (SmartCvTemplate) -> Unit,
    onExportClick: () -> Unit,
    onShareClick: () -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE2E8F0)) // Studio canvas backdrop
    ) {
        // Quick Template Switcher Pills
        Surface(
            color = themeColors.background,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.1f))
        ) {
            LazyRow(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SmartCvTemplate.values()) { tmpl ->
                    val isSelected = selectedTemplate == tmpl
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTemplateSelect(tmpl) },
                        label = {
                            Text(
                                text = if (isBn) tmpl.nameBn else tmpl.nameEn,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = if (isSelected) {
                            { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColors.buttonEqualBg,
                            selectedLabelColor = themeColors.buttonEqualText,
                            selectedLeadingIconColor = themeColors.buttonEqualText
                        )
                    )
                }
            }
        }

        // Canvas Sheet Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.75f, 3.5f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (isRendering) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = themeColors.buttonEqualBg)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isBn) "ক্যানভাস রেন্ডার হচ্ছে..." else "Rendering A4 Canvas...",
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        fontWeight = FontWeight.Medium
                    )
                }
            } else if (pages.isEmpty()) {
                Text(
                    text = if (isBn) "কোনো প্রিভিউ পাওয়া যায়নি" else "Preview not available",
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        ),
                    contentPadding = PaddingValues(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(pages) { index, pageBitmap ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                shadowElevation = 10.dp,
                                color = Color.White,
                                modifier = Modifier
                                    .width(360.dp)
                                    .aspectRatio(595f / 842f)
                            ) {
                                Image(
                                    bitmap = pageBitmap.asImageBitmap(),
                                    contentDescription = "CV Page ${index + 1}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.FillWidth
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = if (isBn) "পৃষ্ঠা ${index + 1} / ${pages.size}" else "Page ${index + 1} of ${pages.size}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Floating Quick Zoom Reset & Share Controls
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                    },
                    containerColor = Color.White,
                    contentColor = Color(0xFF1E293B)
                ) {
                    Icon(imageVector = Icons.Default.FitScreen, contentDescription = "Reset Zoom")
                }
                FloatingActionButton(
                    onClick = onExportClick,
                    containerColor = themeColors.buttonEqualBg,
                    contentColor = themeColors.buttonEqualText
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = "Export PDF")
                }
            }
        }
    }
}

// =============================================================================
// TAB 3: STYLE & THEMES TAB
// =============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SmartCvStyleTab(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    selectedTemplate: SmartCvTemplate,
    onTemplateSelect: (SmartCvTemplate) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Text(
                text = if (isBn) "আন্তর্জাতিক ৪টি টেমপ্লেট নির্বাচন" else "Select Core Template",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = themeColors.displayText
            )
        }

        items(SmartCvTemplate.values()) { tmpl ->
            val isSelected = selectedTemplate == tmpl
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) themeColors.buttonEqualBg.copy(alpha = 0.12f) else themeColors.buttonFunctionBg.copy(alpha = 0.35f),
                border = BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.15f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTemplateSelect(tmpl) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onTemplateSelect(tmpl) },
                        colors = RadioButtonDefaults.colors(selectedColor = themeColors.buttonEqualBg)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isBn) tmpl.nameBn else tmpl.nameEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = themeColors.displayText
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF16A34A).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = tmpl.atsScoreBadge,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF16A34A),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isBn) tmpl.taglineBn else tmpl.taglineEn,
                            fontSize = 11.sp,
                            color = themeColors.displayText.copy(alpha = 0.65f)
                        )
                    }
                }
            }
        }

        item {
            HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.15f))
        }

        item {
            Text(
                text = if (isBn) "প্রাইমারি অ্যাকসেন্ট কালার" else "Primary Accent Color",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = themeColors.displayText
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmartAccentColor.values().forEach { col ->
                    val isSelected = cvData.primaryColorHexOverride.equals(col.hex, ignoreCase = true)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onCvDataChange(cvData.copy(primaryColorHexOverride = col.hex)) }
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(col.composeColor)
                                .border(
                                    if (isSelected) 3.dp else 1.dp,
                                    if (isSelected) themeColors.displayText else Color.Transparent,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBn) col.labelBn else col.labelEn,
                            fontSize = 9.5.sp,
                            color = themeColors.displayText.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        item {
            HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.15f))
        }

        item {
            Text(
                text = if (isBn) "বুলেট পয়েন্ট স্টাইল" else "Bullet Point Style",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = themeColors.displayText
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "BULLET" to "• Bullet",
                    "DASH" to "– Dash",
                    "SQUARE" to "▪ Square",
                    "ARROW" to "▸ Arrow"
                ).forEach { (styleKey, label) ->
                    val isSelected = cvData.bulletStyle == styleKey
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCvDataChange(cvData.copy(bulletStyle = styleKey)) },
                        label = { Text(text = label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColors.buttonEqualBg,
                            selectedLabelColor = themeColors.buttonEqualText
                        )
                    )
                }
            }
        }
    }
}

// =============================================================================
// TAB 4: AI COPILOT & JOB CIRCULAR HEATMAP
// =============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SmartCvAiCopilotTab(
    cvData: CvData,
    onApplyTailoredSummary: (String) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    var circularInput by remember { mutableStateOf(cvData.targetJobCircular) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<SmartJobAnalysis?>(null) }
    var generatedCoverLetter by remember { mutableStateOf<Pair<String, String>?>(null) }
    var isGeneratingLetter by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = themeColors.buttonEqualBg)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isBn) "জব সার্কুলার ও ATS কি-ওয়ার্ড হিটম্যাপ" else "Job Circular ATS Match & Heatmap",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = themeColors.displayText
                    )
                    Text(
                        text = if (isBn) "সার্কুলার পেস্ট করুন। এআই স্ক্যান করে মিসিং কি-ওয়ার্ড ও ম্যাচ স্কোর দেখাবে।"
                        else "Paste circular text. AI will calculate match score and highlight missing keywords.",
                        fontSize = 11.sp,
                        color = themeColors.displayText.copy(alpha = 0.65f)
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = circularInput,
                onValueChange = { circularInput = it },
                label = { Text(if (isBn) "চাকরির সার্কুলার টেক্সট পেস্ট করুন" else "Paste Job Description / Requirements", fontSize = 11.5.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themeColors.buttonEqualBg,
                    unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                    focusedTextColor = themeColors.displayText,
                    unfocusedTextColor = themeColors.displayText
                )
            )
        }

        item {
            Button(
                onClick = {
                    if (circularInput.isNotBlank()) {
                        scope.launch {
                            isAnalyzing = true
                            analysisResult = SmartCvAiCopilot.analyzeCircularMatch(circularInput, cvData)
                            isAnalyzing = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                enabled = !isAnalyzing && circularInput.isNotBlank()
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (isBn) "এআই স্ক্যানিং ও অ্যানালাইসিস..." else "Analyzing Circular...", fontSize = 12.sp)
                } else {
                    Icon(imageVector = Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (isBn) "সার্কুলার স্ক্যান ও হিটম্যাপ দেখুন" else "Analyze ATS Match & Heatmap", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        analysisResult?.let { result ->
            item {
                // Match Score Gauge Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = themeColors.buttonFunctionBg.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        result.atsMatchScore >= 80 -> Color(0xFF16A34A).copy(alpha = 0.2f)
                                        result.atsMatchScore >= 65 -> Color(0xFFD97706).copy(alpha = 0.2f)
                                        else -> Color(0xFFDC2626).copy(alpha = 0.2f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${result.atsMatchScore}%",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = when {
                                    result.atsMatchScore >= 80 -> Color(0xFF16A34A)
                                    result.atsMatchScore >= 65 -> Color(0xFFD97706)
                                    else -> Color(0xFFDC2626)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = if (isBn) "সার্কুলারের সাথে ATS ম্যাচ স্কোর" else "ATS Compatibility Score",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = themeColors.displayText
                            )
                            Text(
                                text = when {
                                    result.atsMatchScore >= 80 -> if (isBn) "চমৎকার ম্যাচ! ইন্টারভিউ কলের সম্ভাবনা ৯৫%+" else "Excellent Match! High interview probability"
                                    result.atsMatchScore >= 65 -> if (isBn) "ভালো ম্যাচ। মিসিং কি-ওয়ার্ড যোগ করলে আরো ভালো হবে" else "Good match. Add missing keywords to reach 85%+"
                                    else -> if (isBn) "নিম্ন ম্যাচ। কি-ওয়ার্ড ও এক্সপেরিয়েন্স টিউন করুন" else "Low match. Integrate key circular competencies"
                                },
                                fontSize = 11.sp,
                                color = themeColors.displayText.copy(alpha = 0.65f)
                            )
                        }
                    }
                }
            }

            item {
                // Keyword Heatmap
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isBn) "🟢 আপনার সিভিতে পাওয়া গেছে (${result.foundKeywords.size})" else "🟢 Matched in your CV (${result.foundKeywords.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = Color(0xFF16A34A)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        result.foundKeywords.forEach { kw ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFDCFCE7),
                                border = BorderStroke(1.dp, Color(0xFF16A34A).copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "✓ $kw",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF14532D),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isBn) "🔴 সার্কুলারের যে কি-ওয়ার্ডগুলো সিভিতে নেই (${result.missingKeywords.size})" else "🔴 Missing Keywords from Circular (${result.missingKeywords.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = Color(0xFFDC2626)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        result.missingKeywords.forEach { kw ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFEE2E2),
                                border = BorderStroke(1.dp, Color(0xFFDC2626).copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "+ $kw",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF7F1D1D),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (result.tailoredSummary.isNotBlank()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = themeColors.buttonEqualBg.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBn) "✨ সার্কুলার অনুযায়ী প্রস্তাবিত সামারি" else "✨ Tailored Executive Pitch",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = themeColors.displayText
                                )
                                Button(
                                    onClick = { onApplyTailoredSummary(result.tailoredSummary) },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
                                ) {
                                    Text(text = if (isBn) "সিভিতে যুক্ত করুন" else "Apply to CV", fontSize = 11.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = result.tailoredSummary,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp,
                                color = themeColors.displayText
                            )
                        }
                    }
                }
            }

            item {
                // Cover Letter Generator Button
                Button(
                    onClick = {
                        scope.launch {
                            isGeneratingLetter = true
                            generatedCoverLetter = SmartCvAiCopilot.generateCoverLetter(circularInput, cvData)
                            isGeneratingLetter = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonFunctionBg)
                ) {
                    if (isGeneratingLetter) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = themeColors.displayText, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = if (isBn) "কভার লেটার তৈরি হচ্ছে..." else "Writing Cover Letter...", fontSize = 12.sp, color = themeColors.displayText)
                    } else {
                        Icon(imageVector = Icons.Default.Mail, contentDescription = null, tint = themeColors.displayText, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isBn) "ম্যাচিং কভার লেটার ও ইমেইল ড্রাফট তৈরি" else "Generate Tailored Cover Letter & Email", fontSize = 12.sp, color = themeColors.displayText)
                    }
                }
            }

            generatedCoverLetter?.let { (subject, body) ->
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = themeColors.background,
                        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBn) "ইমেইল ও কভার লেটার" else "Cover Letter & Email",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = themeColors.displayText
                                )
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString("$subject\n\n$body"))
                                        Toast.makeText(context, if (isBn) "কপি হয়েছে!" else "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = themeColors.buttonEqualBg, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Subject: $subject", fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp, color = themeColors.buttonEqualBg)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = body, fontSize = 11.sp, lineHeight = 15.sp, color = themeColors.displayText)
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// PDF EXPORT DIALOG
// =============================================================================

@Composable
fun SmartPdfExportDialog(
    pdfFile: File?,
    candidateName: String,
    templateName: String,
    profileLabel: String,
    onDismiss: () -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    val context = LocalContext.current
    var customFileName by remember {
        val clean = candidateName.ifBlank { "Candidate" }.replace("[^a-zA-Z0-9]".toRegex(), "_")
        mutableStateOf("CV_${clean}_Smart.pdf")
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = themeColors.background,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.2f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBn) "পিডিএফ এক্সপোর্ট ও শেয়ার" else "Export & Share PDF",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = themeColors.displayText
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = themeColors.displayText)
                    }
                }

                OutlinedTextField(
                    value = customFileName,
                    onValueChange = { customFileName = it },
                    label = { Text(if (isBn) "ফাইল নাম" else "File Name", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColors.buttonEqualBg,
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText
                    ),
                    singleLine = true
                )

                // Actions: View in App, Share, Save to Downloads
                Button(
                    onClick = {
                        pdfFile?.let { file ->
                            val exported = copyToTargetFile(context, file, customFileName)
                            SmartCvStorage.addExportHistory(
                                context,
                                SmartCvHistoryItem(
                                    fileName = customFileName,
                                    filePath = exported.absolutePath,
                                    candidateName = candidateName,
                                    profileLabel = profileLabel,
                                    templateName = templateName
                                )
                            )
                            openPdfFile(context, exported, isBn)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (isBn) "অ্যাপে সরাসরি ওপেন করুন" else "Open & View PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                    onClick = {
                        pdfFile?.let { file ->
                            val exported = copyToTargetFile(context, file, customFileName)
                            SmartCvStorage.addExportHistory(
                                context,
                                SmartCvHistoryItem(
                                    fileName = customFileName,
                                    filePath = exported.absolutePath,
                                    candidateName = candidateName,
                                    profileLabel = profileLabel,
                                    templateName = templateName
                                )
                            )
                            sharePdfFile(context, exported, isBn)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = themeColors.buttonFunctionBg)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = themeColors.displayText, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (isBn) "পিডিএফ শেয়ার করুন (WhatsApp/Email)" else "Share PDF (WhatsApp / Email)", fontSize = 12.sp, color = themeColors.displayText)
                }
            }
        }
    }
}

// =============================================================================
// PROFILE MANAGER DIALOG
// =============================================================================

@Composable
fun SmartProfileManagerDialog(
    profiles: List<CvData>,
    activeId: String,
    onSelect: (String) -> Unit,
    onCreateNew: (String) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    var newProfileName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = themeColors.background,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.2f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBn) "প্রোফাইল ম্যানেজার" else "Manage CV Profiles",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = themeColors.displayText
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = themeColors.displayText)
                    }
                }

                // Create new profile row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = { newProfileName = it },
                        label = { Text(if (isBn) "নতুন প্রোফাইলের নাম" else "Profile Label", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (newProfileName.isNotBlank()) {
                                onCreateNew(newProfileName.trim())
                                newProfileName = ""
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
                    ) {
                        Text(text = if (isBn) "+ তৈরি" else "+ Add", fontSize = 11.sp)
                    }
                }

                HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.15f))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(profiles) { profile ->
                        val isSelected = profile.id == activeId
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) themeColors.buttonEqualBg.copy(alpha = 0.12f) else themeColors.buttonFunctionBg.copy(alpha = 0.35f),
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(profile.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelect(profile.id) },
                                    colors = RadioButtonDefaults.colors(selectedColor = themeColors.buttonEqualBg)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = profile.profileLabel,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = themeColors.displayText
                                    )
                                    Text(
                                        text = profile.jobTitle.ifBlank { "No title" },
                                        fontSize = 11.sp,
                                        color = themeColors.displayText.copy(alpha = 0.6f)
                                    )
                                }
                                if (profiles.size > 1) {
                                    IconButton(
                                        onClick = { onDelete(profile.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
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

// =============================================================================
// INTENT HELPERS
// =============================================================================

private fun copyToTargetFile(context: Context, sourceFile: File, newName: String): File {
    val dir = context.getExternalFilesDir(null) ?: context.filesDir
    val target = File(dir, newName)
    try {
        sourceFile.inputStream().use { input ->
            target.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    } catch (_: Exception) {}
    return target
}

private fun openPdfFile(context: Context, file: File, isBn: Boolean) {
    try {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, if (isBn) "পিডিএফ ওপেন করুন" else "Open PDF"))
    } catch (e: Exception) {
        Toast.makeText(context, if (isBn) "পিডিএফ ভিউয়ার অ্যাপ পাওয়া যায়নি" else "No PDF viewer app found", Toast.LENGTH_SHORT).show()
    }
}

private fun sharePdfFile(context: Context, file: File, isBn: Boolean) {
    try {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, if (isBn) "সিভি পিডিএফ শেয়ার করুন" else "Share CV PDF"))
    } catch (e: Exception) {
        Toast.makeText(context, if (isBn) "শেয়ার করতে সমস্যা হয়েছে" else "Failed to share PDF", Toast.LENGTH_SHORT).show()
    }
}
