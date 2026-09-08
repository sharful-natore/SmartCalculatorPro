package com.example.ui.screens.tools

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import android.os.CancellationSignal
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

// ================= DATA MODELS =================
data class PdfFileItem(
    val name: String,
    val uri: Uri,
    val sizeBytes: Long,
    val dateModifiedMs: Long,
    val path: String
)

data class PdfImageItem(
    val uri: Uri,
    val title: String = ""
)

enum class PdfListFilterTab(val titleBn: String, val titleEn: String) {
    ALL("সব ফাইল", "All Files"),
    FAVORITES("ফেভারিট", "Favorites"),
    HISTORY("সাম্প্রতিক", "Recent")
}

sealed class PdfOpenResult {
    data class Success(val pageCount: Int, val textPages: List<String>) : PdfOpenResult()
    data class Error(val reasonBn: String, val reasonEn: String) : PdfOpenResult()
}

enum class PdfSortOption(val titleBn: String, val titleEn: String) {
    DATE_DESC("নতুন ফাইল আগে (তারিখ ↓)", "Newest First"),
    DATE_ASC("পুরোনো ফাইল আগে (তারিখ ↑)", "Oldest First"),
    NAME_ASC("নাম (A → Z)", "Name (A-Z)"),
    NAME_DESC("নাম (Z → A)", "Name (Z-A)"),
    SIZE_DESC("আকার বড় থেকে ছোট (Size ↓)", "Size (Large to Small)"),
    SIZE_ASC("আকার ছোট থেকে বড় (Size ↑)", "Size (Small to Large)")
}

// ================= 1. PDF READER TOOL (GOOGLE DRIVE STYLE) =================
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PdfReaderTool(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

    // Active Opened PDF State
    var pdfUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var fileSizeBytes by remember { mutableLongStateOf(0L) }
    var fileLastModifiedMs by remember { mutableLongStateOf(0L) }
    var filePath by remember { mutableStateOf("") }

    // React to initial PDF set from outside
    LaunchedEffect(viewModel.pdfReaderInitialUri) {
        viewModel.pdfReaderInitialUri?.let { uri ->
            pdfUri = uri
            fileName = viewModel.pdfReaderInitialName.ifBlank { "Document.pdf" }
            viewModel.pdfReaderInitialUri = null
        }
    }

    // Fullscreen and Immersive UI visibility states
    var isFullscreen by remember { mutableStateOf(false) }
    var isControlsVisible by remember { mutableStateOf(true) }

    LaunchedEffect(isFullscreen) {
        viewModel.isPdfReaderFullscreen = isFullscreen
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.isPdfReaderFullscreen = false
        }
    }

    // Google Drive Pinch-to-zoom & Double-tap states
    var rotationDegrees by remember { mutableIntStateOf(0) }
    var isNightMode by remember { mutableStateOf(false) }

    // Google Drive Document-level zoom & pan states
    var docScale by remember { mutableFloatStateOf(1.0f) }
    var docOffset by remember { mutableStateOf(Offset.Zero) }

    // Page Navigation states (Vertical Continuous Scroll)
    var pageCount by remember { mutableIntStateOf(0) }
    var currentPageScale by remember { mutableFloatStateOf(1.0f) }
    val coroutineScope = rememberCoroutineScope()
    val verticalLazyListState = rememberLazyListState()
    val visibleCurrentPage by remember { derivedStateOf { verticalLazyListState.firstVisibleItemIndex } }

    // Text Search State inside PDF Viewer
    var isSearchActive by remember { mutableStateOf(false) }
    var pdfSearchQuery by remember { mutableStateOf("") }
    var pdfTextPages by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentMatchIndex by remember { mutableIntStateOf(0) }

    val searchMatches = remember(pdfSearchQuery, pdfTextPages) {
        if (pdfSearchQuery.trim().isEmpty()) emptyList()
        else {
            val query = pdfSearchQuery.trim().lowercase()
            val matchesList = mutableListOf<Int>()
            pdfTextPages.forEachIndexed { idx, pageText ->
                if (pageText.lowercase().contains(query)) {
                    matchesList.add(idx)
                }
            }
            matchesList
        }
    }

    // System Window Bar Insets for True Immersive Fullscreen
    val window = (context as? android.app.Activity)?.window
    LaunchedEffect(isFullscreen) {
        window?.let { win ->
            val controller = androidx.core.view.WindowCompat.getInsetsController(win, win.decorView)
            if (isFullscreen) {
                controller.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                controller.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            window?.let { win ->
                androidx.core.view.WindowCompat.getInsetsController(win, win.decorView).show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Modals & Dialogs
    var showJumpDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showTextSelectDialogPage by remember { mutableStateOf<Int?>(null) }
    var showViewerOverflowMenu by remember { mutableStateOf(false) }
    var showDeleteCurrentFileDialog by remember { mutableStateOf(false) }

    // Files List View States
    var pdfFileList by remember { mutableStateOf<List<PdfFileItem>>(emptyList()) }
    var isScanningFiles by remember { mutableStateOf(true) }
    var scanTrigger by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSort by remember { mutableStateOf(PdfSortOption.DATE_DESC) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Tab Filter & Storage State (All, Favorites, History)
    var activeTab by remember { mutableStateOf(PdfListFilterTab.ALL) }
    var favoritePdfList by remember { mutableStateOf<List<PdfFileItem>>(emptyList()) }
    var historyPdfList by remember { mutableStateOf<List<PdfFileItem>>(emptyList()) }

    // Robust Loading & Error States
    var isPdfLoading by remember { mutableStateOf(false) }
    var pdfErrorMessage by remember { mutableStateOf<String?>(null) }

    // Action Menus & Operations (Rename, Delete, Long-press)
    var selectedFileForAction by remember { mutableStateOf<PdfFileItem?>(null) }
    var fileToRename by remember { mutableStateOf<PdfFileItem?>(null) }
    var fileToDelete by remember { mutableStateOf<PdfFileItem?>(null) }
    var newRenameText by remember { mutableStateOf("") }

    // Collapsing Top Header on scroll (like Quran & Hadith screen)
    var isHeaderVisible by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < -12f && isHeaderVisible && (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 40)) {
                    isHeaderVisible = false
                } else if (delta > 12f && !isHeaderVisible) {
                    isHeaderVisible = true
                }
                return Offset.Zero
            }
        }
    }

    // Intercept Back Press: Handled systematically for all dialogs, errors, tabs, and viewer states
    BackHandler {
        if (pdfErrorMessage != null) {
            pdfErrorMessage = null
            pdfUri = null
            pageCount = 0
            isPdfLoading = false
        } else if (selectedFileForAction != null) {
            selectedFileForAction = null
        } else if (fileToRename != null) {
            fileToRename = null
        } else if (fileToDelete != null) {
            fileToDelete = null
        } else if (showJumpDialog) {
            showJumpDialog = false
        } else if (showDetailsDialog) {
            showDetailsDialog = false
        } else if (isSearchActive) {
            isSearchActive = false
            pdfSearchQuery = ""
        } else if (isFullscreen) {
            isFullscreen = false
            isControlsVisible = true
        } else if (docScale > 1.1f) {
            docScale = 1.0f
            docOffset = Offset.Zero
        } else if (pdfUri != null) {
            val closingUri = pdfUri
            if (closingUri != null && pageCount > 0) {
                val docKey = getPdfUniqueKey(closingUri, filePath, fileName)
                saveLastReadPdfPage(context, docKey, visibleCurrentPage)
            }
            pdfUri = null
            pageCount = 0
            currentPageScale = 1.0f
            docScale = 1.0f
            docOffset = Offset.Zero
            rotationDegrees = 0
            isPdfLoading = false
            pdfErrorMessage = null
        } else if (searchQuery.isNotEmpty()) {
            searchQuery = ""
        } else if (activeTab != PdfListFilterTab.ALL) {
            activeTab = PdfListFilterTab.ALL
        } else {
            onBackClick()
        }
    }

    // Storage permission checks
    var hasStoragePermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= 33) true
            else ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasStoragePermission = isGranted
        if (isGranted) {
            scanTrigger++
        }
    }

    // Load incoming pending PDF from MainActivity intent
    LaunchedEffect(viewModel.pendingPdfUri) {
        val uri = viewModel.pendingPdfUri
        if (uri != null) {
            pdfUri = uri
            viewModel.pendingPdfUri = null
            currentPageScale = 1.0f
            docScale = 1.0f
            docOffset = Offset.Zero
            rotationDegrees = 0
            extractPdfMetadata(context, uri) { name, size, date, path ->
                fileName = name
                fileSizeBytes = size
                fileLastModifiedMs = date
                filePath = path
            }
        }
    }

    // File Picker Launcher for manual browsing
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pdfUri = uri
            currentPageScale = 1.0f
            docScale = 1.0f
            docOffset = Offset.Zero
            rotationDegrees = 0
            extractPdfMetadata(context, uri) { name, size, date, path ->
                fileName = name
                fileSizeBytes = size
                fileLastModifiedMs = date
                filePath = path
            }
        }
    }

    // Scan for Device PDFs asynchronously (fixed infinite scanning loop)
    LaunchedEffect(hasStoragePermission, scanTrigger) {
        if (hasStoragePermission) {
            isScanningFiles = true
            withContext(Dispatchers.IO) {
                val scanned = scanDevicePdfFiles(context)
                val favs = getFavoritePdfs(context)
                val recents = getRecentPdfHistory(context)
                withContext(Dispatchers.Main) {
                    pdfFileList = scanned
                    favoritePdfList = favs
                    historyPdfList = recents
                    isScanningFiles = false
                }
            }
        } else {
            isScanningFiles = false
        }
    }

    // Refresh favorites & history when tab changes
    LaunchedEffect(activeTab) {
        withContext(Dispatchers.IO) {
            val favs = getFavoritePdfs(context)
            val recents = getRecentPdfHistory(context)
            withContext(Dispatchers.Main) {
                favoritePdfList = favs
                historyPdfList = recents
            }
        }
    }

    // Load PDF Page Count safely when a document is selected (Never gets stuck on corrupted file)
    LaunchedEffect(pdfUri) {
        val currentUri = pdfUri
        if (currentUri == null) {
            pdfTextPages = emptyList()
            pdfSearchQuery = ""
            currentMatchIndex = 0
            isSearchActive = false
            isPdfLoading = false
            pdfErrorMessage = null
            return@LaunchedEffect
        }
        isPdfLoading = true
        pdfErrorMessage = null
        val result = loadPdfDocument(context, currentUri)
        when (result) {
            is PdfOpenResult.Success -> {
                pageCount = result.pageCount
                pdfTextPages = result.textPages
                pdfSearchQuery = ""
                currentMatchIndex = 0
                isSearchActive = false
                isPdfLoading = false
                pdfErrorMessage = null
                val recordedItem = PdfFileItem(
                    name = fileName.ifBlank { "Document.pdf" },
                    uri = currentUri,
                    sizeBytes = fileSizeBytes,
                    dateModifiedMs = if (fileLastModifiedMs > 0) fileLastModifiedMs else System.currentTimeMillis(),
                    path = filePath
                )
                withContext(Dispatchers.IO) {
                    recordPdfToRecentHistory(context, recordedItem)
                    val recents = getRecentPdfHistory(context)
                    withContext(Dispatchers.Main) {
                        historyPdfList = recents
                    }
                }

                // Restore last read page state automatically
                val docKey = getPdfUniqueKey(currentUri, filePath, fileName)
                val savedPage = getLastReadPdfPage(context, docKey)
                if (savedPage in 1 until pageCount) {
                    withContext(Dispatchers.Main) {
                        verticalLazyListState.scrollToItem(savedPage)
                        Toast.makeText(
                            context,
                            if (isBn) "পৃষ্ঠা ${savedPage + 1}-এ ফিরিয়ে আনা হয়েছে" else "Resumed from page ${savedPage + 1}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            is PdfOpenResult.Error -> {
                isPdfLoading = false
                pageCount = 0
                pdfTextPages = emptyList()
                pdfErrorMessage = if (isBn) result.reasonBn else result.reasonEn
            }
        }
    }

    // Continuously persist the last read page as user scrolls
    LaunchedEffect(visibleCurrentPage, pdfUri, pageCount) {
        val currentUri = pdfUri
        if (currentUri != null && pageCount > 0) {
            val docKey = getPdfUniqueKey(currentUri, filePath, fileName)
            saveLastReadPdfPage(context, docKey, visibleCurrentPage)
        }
    }

    val density = LocalDensity.current.density

    // Determine current active list (All, Favorites, or Recent History)
    val currentSourceList = remember(activeTab, pdfFileList, favoritePdfList, historyPdfList) {
        when (activeTab) {
            PdfListFilterTab.ALL -> pdfFileList
            PdfListFilterTab.FAVORITES -> favoritePdfList
            PdfListFilterTab.HISTORY -> historyPdfList
        }
    }

    // Filter and Sort PDF List
    val filteredAndSortedPdfs by remember(currentSourceList, searchQuery, selectedSort) {
        derivedStateOf {
            val query = searchQuery.trim().lowercase()
            currentSourceList
                .filter {
                    query.isEmpty() ||
                            it.name.lowercase().contains(query) ||
                            it.path.lowercase().contains(query)
                }
                .let { list ->
                    when (selectedSort) {
                        PdfSortOption.DATE_DESC -> list.sortedByDescending { it.dateModifiedMs }
                        PdfSortOption.DATE_ASC -> list.sortedBy { it.dateModifiedMs }
                        PdfSortOption.NAME_ASC -> list.sortedBy { it.name.lowercase() }
                        PdfSortOption.NAME_DESC -> list.sortedByDescending { it.name.lowercase() }
                        PdfSortOption.SIZE_DESC -> list.sortedByDescending { it.sizeBytes }
                        PdfSortOption.SIZE_ASC -> list.sortedBy { it.sizeBytes }
                    }
                }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (pdfUri != null && (isFullscreen || isNightMode)) Color(0xFF0F172A) else themeColors.background)
    ) {
        if (pdfUri == null) {
            // ================= PDF FILES LIST SCREEN =================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection)
            ) {
                // Collapsing Slim Header (Quran & Hadith style)
                AnimatedVisibility(
                    visible = isHeaderVisible,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        color = themeColors.cardBg,
                        shadowElevation = 3.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (activeTab != PdfListFilterTab.ALL) {
                                        activeTab = PdfListFilterTab.ALL
                                    } else {
                                        onBackClick()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = themeColors.displayText
                                )
                            }

                            Spacer(modifier = Modifier.width(2.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when (activeTab) {
                                        PdfListFilterTab.ALL -> if (isBn) "পিডিএফ রিডার" else "PDF Reader"
                                        PdfListFilterTab.FAVORITES -> if (isBn) "প্রিয় PDF নথি" else "Favorite PDFs"
                                        PdfListFilterTab.HISTORY -> if (isBn) "পড়ার ইতিহাস" else "Recent History"
                                    },
                                    fontSize = 16.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = when (activeTab) {
                                        PdfListFilterTab.ALL -> if (isBn) "${pdfFileList.size}টি PDF নথি পাওয়া গেছে" else "${pdfFileList.size} PDF files found"
                                        PdfListFilterTab.FAVORITES -> if (isBn) "${favoritePdfList.size}টি প্রিয় নথি সংরক্ষিত" else "${favoritePdfList.size} favorites saved"
                                        PdfListFilterTab.HISTORY -> if (isBn) "${historyPdfList.size}টি সম্প্রতি পড়া ফাইল" else "${historyPdfList.size} recent files"
                                    },
                                    fontSize = 11.sp,
                                    color = themeColors.buttonEqualBg,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Header Action 1: Favorite List Toggle
                            IconButton(
                                onClick = {
                                    activeTab = if (activeTab == PdfListFilterTab.FAVORITES) {
                                        PdfListFilterTab.ALL
                                    } else {
                                        PdfListFilterTab.FAVORITES
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (activeTab == PdfListFilterTab.FAVORITES)
                                            Color(0xFFEF4444).copy(alpha = 0.16f)
                                        else
                                            Color.Transparent
                                    )
                            ) {
                                Icon(
                                    imageVector = if (activeTab == PdfListFilterTab.FAVORITES) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorites",
                                    tint = if (activeTab == PdfListFilterTab.FAVORITES) Color(0xFFEF4444) else themeColors.displayText.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Header Action 2: History List Toggle
                            IconButton(
                                onClick = {
                                    activeTab = if (activeTab == PdfListFilterTab.HISTORY) {
                                        PdfListFilterTab.ALL
                                    } else {
                                        PdfListFilterTab.HISTORY
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (activeTab == PdfListFilterTab.HISTORY)
                                            themeColors.buttonEqualBg.copy(alpha = 0.18f)
                                        else
                                            Color.Transparent
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "History",
                                    tint = if (activeTab == PdfListFilterTab.HISTORY) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Header Action 3: Refresh Scan button
                            IconButton(
                                onClick = {
                                    scanTrigger++
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = themeColors.buttonEqualBg,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Header Action 4: Open System File Picker
                            IconButton(
                                onClick = { filePickerLauncher.launch("application/pdf") },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = "Browse Files",
                                    tint = themeColors.buttonEqualBg,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    // Search and Sort Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    if (isBn) "PDF ফাইলের নাম দিয়ে খুঁজুন..." else "Search PDF files by name...",
                                    fontSize = 13.sp,
                                    color = themeColors.displayText.copy(alpha = 0.5f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = themeColors.buttonEqualBg,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = themeColors.displayText.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = themeColors.displayText,
                                unfocusedTextColor = themeColors.displayText,
                                cursorColor = themeColors.buttonEqualBg,
                                focusedContainerColor = themeColors.buttonEqualBg.copy(alpha = 0.04f),
                                unfocusedContainerColor = themeColors.buttonEqualBg.copy(alpha = 0.04f),
                                focusedBorderColor = themeColors.buttonEqualBg,
                                unfocusedBorderColor = themeColors.buttonEqualBg.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box {
                            IconButton(
                                onClick = { showSortMenu = true },
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(themeColors.cardBg)
                                    .border(1.dp, themeColors.displayText.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Sort",
                                    tint = themeColors.buttonEqualBg,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                modifier = Modifier.background(themeColors.cardBg)
                            ) {
                                PdfSortOption.values().forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = if (isBn) option.titleBn else option.titleEn,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (selectedSort == option) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (selectedSort == option) themeColors.buttonEqualBg else themeColors.displayText
                                                )
                                                if (selectedSort == option) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = themeColors.buttonEqualBg,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedSort = option
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Storage Permission Notice if not granted
                    if (!hasStoragePermission && Build.VERSION.SDK_INT < 33) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.12f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.FolderSpecial,
                                    contentDescription = null,
                                    tint = themeColors.buttonEqualBg,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isBn) "স্টোরেজ পারমিশন প্রয়োজন" else "Storage Permission Needed",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.displayText
                                    )
                                    Text(
                                        text = if (isBn) "ফোনের সব PDF স্বয়ংক্রিয়ভাবে পেতে পারমিশন দিন" else "Grant permission to list all PDF files on device",
                                        fontSize = 11.sp,
                                        color = themeColors.displayText.copy(alpha = 0.7f)
                                    )
                                }
                                Button(
                                    onClick = { storagePermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE) },
                                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg, contentColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(if (isBn) "অনুমতি দিন" else "Allow", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Tab Filter Active Banner
                    AnimatedVisibility(visible = activeTab != PdfListFilterTab.ALL) {
                        Surface(
                            color = (if (activeTab == PdfListFilterTab.FAVORITES) Color(0xFFEF4444) else themeColors.buttonEqualBg).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (activeTab == PdfListFilterTab.FAVORITES) Icons.Default.Favorite else Icons.Default.History,
                                    contentDescription = null,
                                    tint = if (activeTab == PdfListFilterTab.FAVORITES) Color(0xFFEF4444) else themeColors.buttonEqualBg,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (activeTab == PdfListFilterTab.FAVORITES)
                                        (if (isBn) "শুধুমাত্র প্রিয় ফাইলগুলো প্রদর্শিত হচ্ছে" else "Showing favorite files only")
                                    else
                                        (if (isBn) "সম্প্রতি পড়া ফাইলগুলো প্রদর্শিত হচ্ছে" else "Showing recently opened files only"),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = themeColors.displayText,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(
                                    onClick = { activeTab = PdfListFilterTab.ALL },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Text(
                                        text = if (isBn) "সব দেখুন" else "Show All",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.buttonEqualBg
                                    )
                                }
                            }
                        }
                    }

                    // List Content or Empty State
                    if (isScanningFiles) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = themeColors.buttonEqualBg)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (isBn) "ফোনের PDF ফাইলগুলো স্ক্যান করা হচ্ছে..." else "Scanning device PDF files...",
                                    fontSize = 13.sp,
                                    color = themeColors.displayText.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else if (filteredAndSortedPdfs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = (if (activeTab == PdfListFilterTab.FAVORITES) Color(0xFFEF4444) else themeColors.buttonEqualBg).copy(alpha = 0.1f),
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = when (activeTab) {
                                                PdfListFilterTab.ALL -> Icons.Default.PictureAsPdf
                                                PdfListFilterTab.FAVORITES -> Icons.Default.FavoriteBorder
                                                PdfListFilterTab.HISTORY -> Icons.Default.History
                                            },
                                            contentDescription = null,
                                            tint = if (activeTab == PdfListFilterTab.FAVORITES) Color(0xFFEF4444) else themeColors.buttonEqualBg,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) {
                                        if (isBn) "কোনো মিল পাওয়া যায়নি" else "No matching PDF found"
                                    } else {
                                        when (activeTab) {
                                            PdfListFilterTab.ALL -> if (isBn) "ফোনে কোনো PDF ফাইল পাওয়া যায়নি" else "No PDF files found"
                                            PdfListFilterTab.FAVORITES -> if (isBn) "কোনো প্রিয় PDF নথি সংরক্ষিত নেই" else "No favorite PDFs saved yet"
                                            PdfListFilterTab.HISTORY -> if (isBn) "সাম্প্রতিক কোনো পড়ার ইতিহাস নেই" else "No recent PDF history yet"
                                        }
                                    },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) {
                                        if (isBn) "অন্য কোনো নাম দিয়ে অনুসন্ধান করুন" else "Try searching with a different name"
                                    } else {
                                        when (activeTab) {
                                            PdfListFilterTab.ALL -> if (isBn) "উপরের ব্রাউজ বাটন চেপে সিস্টেম ফাইল ম্যানেজার থেকে যেকোনো PDF ওপেন করুন।" else "Tap the Browse button above to open any PDF from system storage."
                                            PdfListFilterTab.FAVORITES -> if (isBn) "তালিকার যেকোনো ফাইলে লং-প্রেস করে প্রিয় তালিকায় যোগ করতে পারেন।" else "Long-press any file in the list to add it to your favorites."
                                            PdfListFilterTab.HISTORY -> if (isBn) "কোনো PDF ফাইল ওপেন করলে তা স্বয়ংক্রিয়ভাবে এখানে তালিকাভুক্ত হবে।" else "Any PDF file you open will be automatically remembered here."
                                        }
                                    },
                                    fontSize = 12.sp,
                                    color = themeColors.displayText.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                if (activeTab != PdfListFilterTab.ALL) {
                                    Button(
                                        onClick = { activeTab = PdfListFilterTab.ALL },
                                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg, contentColor = Color.White),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isBn) "সব PDF ফাইল দেখুন" else "View All PDFs")
                                    }
                                } else {
                                    Button(
                                        onClick = { filePickerLauncher.launch("application/pdf") },
                                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg, contentColor = Color.White),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isBn) "ফাইল ব্রাউজ করুন" else "Browse Files")
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(filteredAndSortedPdfs, key = { it.path.ifBlank { it.uri.toString() } }) { item ->
                                val isItemFavorite = favoritePdfList.any { fav ->
                                    (fav.path.isNotBlank() && fav.path == item.path) || (fav.path.isBlank() && fav.uri == item.uri)
                                }

                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = {
                                                pdfUri = item.uri
                                                fileName = item.name
                                                fileSizeBytes = item.sizeBytes
                                                fileLastModifiedMs = item.dateModifiedMs
                                                filePath = item.path
                                                currentPageScale = 1.0f
                                                rotationDegrees = 0
                                            },
                                            onLongClick = {
                                                selectedFileForAction = item
                                            }
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFEF4444).copy(alpha = 0.14f),
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.PictureAsPdf,
                                                    contentDescription = "PDF",
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = item.name,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = themeColors.displayText,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                                if (isItemFavorite) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.Favorite,
                                                        contentDescription = "Favorite",
                                                        tint = Color(0xFFEF4444),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = formatFileSize(item.sizeBytes),
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = themeColors.buttonEqualBg
                                                )
                                                if (item.dateModifiedMs > 0) {
                                                    Text(
                                                        text = " • " + formatFormattedDateTime(item.dateModifiedMs, isBn),
                                                        fontSize = 11.sp,
                                                        color = themeColors.displayText.copy(alpha = 0.55f),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                val savedLastPage = getLastReadPdfPage(context, getPdfUniqueKey(item.uri, item.path, item.name))
                                                if (savedLastPage > 0) {
                                                    Text(
                                                        text = " • " + (if (isBn) "পৃষ্ঠা ${savedLastPage + 1}-এ ছিলেন" else "Page ${savedLastPage + 1}"),
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF10B981),
                                                        fontWeight = FontWeight.SemiBold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }

                                        // Long-press or click 3-dots to show full management options
                                        IconButton(
                                            onClick = { selectedFileForAction = item },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Options",
                                                tint = themeColors.displayText.copy(alpha = 0.65f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ================= 2. GOOGLE DRIVE STYLE VERTICAL MULTI-PAGE VIEWER =================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isNightMode) Color(0xFF0B0F19) else Color(0xFF1E293B))
            ) {
                // TOP ACTION TOOLBAR (Google Drive Style)
                AnimatedVisibility(
                    visible = isControlsVisible && !isFullscreen,
                    enter = slideInVertically { -it } + fadeIn(),
                    exit = slideOutVertically { -it } + fadeOut()
                ) {
                    Surface(
                        color = themeColors.cardBg,
                        shadowElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        val closingUri = pdfUri
                                        if (closingUri != null && pageCount > 0) {
                                            val docKey = getPdfUniqueKey(closingUri, filePath, fileName)
                                            saveLastReadPdfPage(context, docKey, visibleCurrentPage)
                                        }
                                        pdfUri = null
                                        pageCount = 0
                                        currentPageScale = 1.0f
                                        isSearchActive = false
                                        pdfSearchQuery = ""
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back to list",
                                        tint = themeColors.displayText
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 6.dp)
                                ) {
                                    Text(
                                        text = fileName.ifBlank { "PDF Document" },
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.displayText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (pageCount > 0) {
                                            if (isBn) "পৃষ্ঠা ${visibleCurrentPage + 1} / $pageCount • ${formatFileSize(fileSizeBytes)}"
                                            else "Page ${visibleCurrentPage + 1} of $pageCount • ${formatFileSize(fileSizeBytes)}"
                                        } else {
                                            formatFileSize(fileSizeBytes)
                                        },
                                        fontSize = 11.5.sp,
                                        color = themeColors.buttonEqualBg,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Search Text Toggle Button (Left of 3-dot menu)
                                IconButton(
                                    onClick = {
                                        isSearchActive = !isSearchActive
                                        if (!isSearchActive) pdfSearchQuery = ""
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search PDF",
                                        tint = if (isSearchActive) themeColors.buttonEqualBg else themeColors.displayText
                                    )
                                }

                                // 3-Dot Overflow Action Menu
                                Box {
                                    IconButton(
                                        onClick = { showViewerOverflowMenu = true }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "More Options",
                                            tint = themeColors.displayText
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showViewerOverflowMenu,
                                        onDismissRequest = { showViewerOverflowMenu = false },
                                        modifier = Modifier.background(themeColors.cardBg)
                                    ) {
                                        // 1. Share
                                        DropdownMenuItem(
                                            text = { Text(if (isBn) "শেয়ার করুন" else "Share", color = themeColors.displayText) },
                                            leadingIcon = {
                                                Icon(Icons.Default.Share, contentDescription = null, tint = themeColors.buttonEqualBg)
                                            },
                                            onClick = {
                                                showViewerOverflowMenu = false
                                                pdfUri?.let { sharePdfFromUri(context, it, fileName) }
                                            }
                                        )

                                        // 2. Night / Day Mode Toggle
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = if (isNightMode) {
                                                        if (isBn) "লাইট মোড" else "Light Mode"
                                                    } else {
                                                        if (isBn) "নাইট মোড" else "Night Mode"
                                                    },
                                                    color = themeColors.displayText
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = if (isNightMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                                    contentDescription = null,
                                                    tint = if (isNightMode) Color(0xFFFBBF24) else themeColors.buttonEqualBg
                                                )
                                            },
                                            onClick = {
                                                showViewerOverflowMenu = false
                                                isNightMode = !isNightMode
                                            }
                                        )

                                        // 3. Rotate 90 deg
                                        DropdownMenuItem(
                                            text = { Text(if (isBn) "ঘোরান (৯০°)" else "Rotate (90°)", color = themeColors.displayText) },
                                            leadingIcon = {
                                                Icon(Icons.Default.RotateRight, contentDescription = null, tint = themeColors.buttonEqualBg)
                                            },
                                            onClick = {
                                                showViewerOverflowMenu = false
                                                rotationDegrees = (rotationDegrees + 90) % 360
                                            }
                                        )

                                        // 4. Print
                                        DropdownMenuItem(
                                            text = { Text(if (isBn) "প্রিন্ট করুন" else "Print PDF", color = themeColors.displayText) },
                                            leadingIcon = {
                                                Icon(Icons.Default.Print, contentDescription = null, tint = themeColors.buttonEqualBg)
                                            },
                                            onClick = {
                                                showViewerOverflowMenu = false
                                                pdfUri?.let { printPdfDocument(context, it, fileName) }
                                            }
                                        )

                                        // 5. Fullscreen
                                        DropdownMenuItem(
                                            text = { Text(if (isBn) "ফুলস্ক্রিন" else "Fullscreen", color = themeColors.displayText) },
                                            leadingIcon = {
                                                Icon(Icons.Default.Fullscreen, contentDescription = null, tint = themeColors.buttonEqualBg)
                                            },
                                            onClick = {
                                                showViewerOverflowMenu = false
                                                isFullscreen = true
                                            }
                                        )

                                        // 6. File Details
                                        DropdownMenuItem(
                                            text = { Text(if (isBn) "ফাইলের তথ্য" else "File Info", color = themeColors.displayText) },
                                            leadingIcon = {
                                                Icon(Icons.Default.Info, contentDescription = null, tint = themeColors.buttonEqualBg)
                                            },
                                            onClick = {
                                                showViewerOverflowMenu = false
                                                showDetailsDialog = true
                                            }
                                        )

                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            color = themeColors.displayText.copy(alpha = 0.12f)
                                        )

                                        // 7. Delete File
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = if (isBn) "ফাইল মুছুন" else "Delete File",
                                                    color = MaterialTheme.colorScheme.error,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            },
                                            onClick = {
                                                showViewerOverflowMenu = false
                                                showDeleteCurrentFileDialog = true
                                            }
                                        )
                                    }
                                }
                            }

                            // SEARCH TOOLBAR (Drive PDF Text Search)
                            AnimatedVisibility(
                                visible = isSearchActive,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Surface(
                                    color = themeColors.cardBg.copy(alpha = 0.95f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = themeColors.buttonEqualBg,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        OutlinedTextField(
                                            value = pdfSearchQuery,
                                            onValueChange = {
                                                pdfSearchQuery = it
                                                currentMatchIndex = 0
                                            },
                                            placeholder = {
                                                Text(
                                                    text = if (isBn) "PDF নথিতে টেক্সট খুঁজুন..." else "Find text in PDF...",
                                                    fontSize = 12.5.sp,
                                                    color = themeColors.displayText.copy(alpha = 0.5f)
                                                )
                                            },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color.Transparent,
                                                unfocusedBorderColor = Color.Transparent,
                                                focusedTextColor = themeColors.displayText,
                                                unfocusedTextColor = themeColors.displayText
                                            )
                                        )

                                        if (pdfSearchQuery.isNotEmpty()) {
                                            IconButton(
                                                onClick = { pdfSearchQuery = "" },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = "Clear",
                                                    tint = themeColors.displayText.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        if (searchMatches.isNotEmpty()) {
                                            Text(
                                                text = "${currentMatchIndex + 1}/${searchMatches.size}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = themeColors.buttonEqualBg,
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            )

                                            // Previous Match
                                            IconButton(
                                                onClick = {
                                                    if (searchMatches.isNotEmpty()) {
                                                        val prevIdx = (currentMatchIndex - 1 + searchMatches.size) % searchMatches.size
                                                        currentMatchIndex = prevIdx
                                                        val targetPage = searchMatches[prevIdx]
                                                        coroutineScope.launch {
                                                            verticalLazyListState.animateScrollToItem(targetPage)
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.KeyboardArrowUp,
                                                    contentDescription = "Previous Match",
                                                    tint = themeColors.displayText,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            // Next Match
                                            IconButton(
                                                onClick = {
                                                    if (searchMatches.isNotEmpty()) {
                                                        val nextIdx = (currentMatchIndex + 1) % searchMatches.size
                                                        currentMatchIndex = nextIdx
                                                        val targetPage = searchMatches[nextIdx]
                                                        coroutineScope.launch {
                                                            verticalLazyListState.animateScrollToItem(targetPage)
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.KeyboardArrowDown,
                                                    contentDescription = "Next Match",
                                                    tint = themeColors.displayText,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        } else if (pdfSearchQuery.isNotBlank()) {
                                            Text(
                                                text = if (isBn) "পাওয়া যায়নি" else "0 results",
                                                fontSize = 11.sp,
                                                color = Color.Red.copy(alpha = 0.8f),
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // MAIN VIEWPORT FOR PDF PAGES (Google Drive Vertical Scroll & Document-Level Zoom)
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clipToBounds()
                ) {
                    val containerWidth = constraints.maxWidth.toFloat()
                    val containerHeight = constraints.maxHeight.toFloat()

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(containerWidth, containerHeight) {
                                val tapThreshold = 300L
                                var lastTapTime = 0L
                                var lastTapOffset = Offset.Zero
                                
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    val now = android.os.SystemClock.uptimeMillis()
                                    
                                    // Manual Double Tap Detection
                                    if (now - lastTapTime < tapThreshold && (down.position - lastTapOffset).getDistance() < 40f) {
                                        val tapOffset = down.position
                                        if (docScale > 1.1f) {
                                            docScale = 1.0f
                                            docOffset = Offset.Zero
                                        } else {
                                            docScale = 2.5f
                                            val targetOffsetX = (containerWidth / 2f - tapOffset.x) * 1.5f
                                            val targetOffsetY = (containerHeight / 2f - tapOffset.y) * 1.5f
                                            val maxPanX = (containerWidth * (2.5f - 1.0f)) / 2f
                                            val maxPanY = (containerHeight * (2.5f - 1.0f)) / 2f
                                            docOffset = Offset(
                                                targetOffsetX.coerceIn(-maxPanX, maxPanX),
                                                targetOffsetY.coerceIn(-maxPanY, maxPanY)
                                            )
                                        }
                                        lastTapTime = 0
                                        down.consume()
                                    } else {
                                        lastTapTime = now
                                        lastTapOffset = down.position
                                        
                                        // Handle Single Tap for UI Toggles
                                        // We don't consume 'down' yet, we wait for 'up'
                                    }

                                    var isMultiTouch = false
                                    do {
                                        val event = awaitPointerEvent()
                                        val pressedCount = event.changes.count { it.pressed }

                                        if (pressedCount >= 2) {
                                            isMultiTouch = true
                                            val zoom = event.calculateZoom()
                                            val pan = event.calculatePan()

                                            val newScale = (docScale * zoom).coerceIn(1.0f, 5.0f)
                                            docScale = newScale

                                            if (newScale > 1.0f) {
                                                val maxPanX = (containerWidth * (newScale - 1.0f)) / 2f
                                                val maxPanY = (containerHeight * (newScale - 1.0f)) / 2f
                                                
                                                val targetOffsetY = docOffset.y + pan.y
                                                val clampedOffsetY = targetOffsetY.coerceIn(-maxPanY, maxPanY)
                                                val overscroll = targetOffsetY - clampedOffsetY
                                                
                                                val newOffset = docOffset + pan
                                                docOffset = Offset(
                                                    newOffset.x.coerceIn(-maxPanX, maxPanX),
                                                    clampedOffsetY
                                                )
                                                
                                                if (overscroll != 0f) {
                                                    verticalLazyListState.dispatchRawDelta(-overscroll / docScale)
                                                }
                                            } else {
                                                docOffset = Offset.Zero
                                            }
                                            event.changes.forEach { it.consume() }
                                        } else if (docScale > 1.0f && pressedCount == 1 && !isMultiTouch) {
                                            val pan = event.calculatePan()
                                            if (pan != Offset.Zero) {
                                                val maxPanX = (containerWidth * (docScale - 1.0f)) / 2f
                                                val maxPanY = (containerHeight * (docScale - 1.0f)) / 2f
                                                
                                                val targetOffsetY = docOffset.y + pan.y
                                                val clampedOffsetY = targetOffsetY.coerceIn(-maxPanY, maxPanY)
                                                val overscroll = targetOffsetY - clampedOffsetY
                                                
                                                val newOffsetX = (docOffset.x + pan.x).coerceIn(-maxPanX, maxPanX)
                                                docOffset = Offset(
                                                    newOffsetX,
                                                    clampedOffsetY
                                                )
                                                
                                                if (overscroll != 0f) {
                                                    verticalLazyListState.dispatchRawDelta(-overscroll / docScale)
                                                }
                                                if (pan.getDistance() > 1f) {
                                                    event.changes.forEach { it.consume() }
                                                }
                                            }
                                        } else if (docScale == 1.0f && pressedCount == 1 && !isMultiTouch) {
                                            // Detect Single Tap on Up
                                            val upEvent = event.changes.find { !it.pressed && it.previousPressed }
                                            if (upEvent != null && now - lastTapTime != 0L) {
                                                if (isFullscreen) {
                                                    isFullscreen = false
                                                    isControlsVisible = true
                                                } else {
                                                    isControlsVisible = !isControlsVisible
                                                }
                                            }
                                        }
                                    } while (event.changes.any { it.pressed })
                                }
                            }
                    ) {
                        if (pageCount > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = docScale,
                                        scaleY = docScale,
                                        translationX = docOffset.x,
                                        translationY = docOffset.y,
                                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                                    )
                            ) {
                                LazyColumn(
                                    state = verticalLazyListState,
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(vertical = 10.dp, horizontal = 6.dp)
                                ) {
                                    items(pageCount) { pageIdx ->
                                        val isMatchPage = isSearchActive && pdfSearchQuery.isNotBlank() && searchMatches.contains(pageIdx)
                                        val isCurrentMatchPage = isSearchActive && searchMatches.getOrNull(currentMatchIndex) == pageIdx

                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .combinedClickable(
                                                    onClick = {
                                                        if (isFullscreen) {
                                                            isFullscreen = false
                                                            isControlsVisible = true
                                                        } else {
                                                            isControlsVisible = !isControlsVisible
                                                        }
                                                    },
                                                    onLongClick = {
                                                        showTextSelectDialogPage = pageIdx
                                                    }
                                                ),
                                            shape = RoundedCornerShape(4.dp),
                                            shadowElevation = 4.dp,
                                            border = if (isCurrentMatchPage) {
                                                BorderStroke(2.5.dp, Color(0xFFEAB308))
                                            } else if (isMatchPage) {
                                                BorderStroke(1.5.dp, themeColors.buttonEqualBg)
                                            } else null,
                                            color = if (isNightMode) Color(0xFF1E293B) else Color.White
                                        ) {
                                            Box {
                                                PdfPageViewerItem(
                                                    context = context,
                                                    pdfUri = pdfUri!!,
                                                    pageIndex = pageIdx,
                                                    isNightMode = isNightMode,
                                                    rotationDegrees = rotationDegrees,
                                                    density = density,
                                                    themeColors = themeColors,
                                                    isBn = isBn
                                                )
                                                if (isMatchPage) {
                                                    Surface(
                                                        shape = RoundedCornerShape(bottomStart = 8.dp),
                                                        color = if (isCurrentMatchPage) Color(0xFFEAB308) else themeColors.buttonEqualBg,
                                                        modifier = Modifier.align(Alignment.TopEnd)
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Search,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = if (isBn) "ম্যাচ" else "Match",
                                                                fontSize = 10.5.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.White
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (pdfErrorMessage != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFFEF4444).copy(alpha = 0.12f),
                                            modifier = Modifier.size(60.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.WarningAmber,
                                                    contentDescription = "Error",
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text(
                                            text = if (isBn) "PDF ফাইলটি ওপেন করা সম্ভব হয়নি" else "Failed to Open PDF",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.displayText,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = pdfErrorMessage ?: "",
                                            fontSize = 12.5.sp,
                                            color = themeColors.displayText.copy(alpha = 0.7f),
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(18.dp))
                                        Button(
                                            onClick = {
                                                pdfUri = null
                                                pdfErrorMessage = null
                                                pageCount = 0
                                                isPdfLoading = false
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = themeColors.buttonEqualBg,
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (isBn) "তালিকায় ফিরে যান" else "Return to List")
                                        }
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = themeColors.buttonEqualBg)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = if (isBn) "PDF লোড হচ্ছে..." else "Loading PDF...",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    TextButton(
                                        onClick = {
                                            pdfUri = null
                                            isPdfLoading = false
                                            pageCount = 0
                                            pdfErrorMessage = null
                                        }
                                    ) {
                                        Text(
                                            text = if (isBn) "বাতিল করুন" else "Cancel",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // BOTTOM FROSTED GLASS FLOATING PAGE PILL & SCRUBBER
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isControlsVisible && pageCount > 0 && !isFullscreen,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut(),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = if (isFullscreen) 24.dp else 16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(26.dp),
                            color = if (isNightMode) {
                                Color(0xFF1E293B).copy(alpha = 0.88f)
                            } else {
                                Color.White.copy(alpha = 0.88f)
                            },
                            shadowElevation = 12.dp,
                            border = BorderStroke(
                                1.5.dp,
                                if (isNightMode) Color.White.copy(alpha = 0.20f)
                                else Color.Black.copy(alpha = 0.12f)
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                // Previous Page Button
                                IconButton(
                                    onClick = {
                                        if (visibleCurrentPage > 0) {
                                            coroutineScope.launch {
                                                verticalLazyListState.animateScrollToItem(visibleCurrentPage - 1)
                                            }
                                        }
                                    },
                                    enabled = visibleCurrentPage > 0,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronLeft,
                                        contentDescription = "Previous Page",
                                        tint = if (visibleCurrentPage > 0) {
                                            if (isNightMode) Color.White else Color(0xFF0F172A)
                                        } else {
                                            if (isNightMode) Color.White.copy(alpha = 0.3f) else Color(0xFF0F172A).copy(alpha = 0.3f)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // Frosted Page Counter Pill (Tap opens "Jump to Page" Dialog)
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = themeColors.buttonEqualBg.copy(alpha = if (isNightMode) 0.25f else 0.15f),
                                    border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .clickable { showJumpDialog = true }
                                ) {
                                    Text(
                                        text = if (isBn)
                                            "পৃষ্ঠা ${visibleCurrentPage + 1} / $pageCount"
                                        else
                                            "Page ${visibleCurrentPage + 1} of $pageCount",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isNightMode) Color.White else Color(0xFF0F172A),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }

                                // Next Page Button
                                IconButton(
                                    onClick = {
                                        if (visibleCurrentPage < pageCount - 1) {
                                            coroutineScope.launch {
                                                verticalLazyListState.animateScrollToItem(visibleCurrentPage + 1)
                                            }
                                        }
                                    },
                                    enabled = visibleCurrentPage < pageCount - 1,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Next Page",
                                        tint = if (visibleCurrentPage < pageCount - 1) {
                                            if (isNightMode) Color.White else Color(0xFF0F172A)
                                        } else {
                                            if (isNightMode) Color.White.copy(alpha = 0.3f) else Color(0xFF0F172A).copy(alpha = 0.3f)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // Exit Fullscreen Icon if in Fullscreen
                                if (isFullscreen) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { isFullscreen = false },
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FullscreenExit,
                                            contentDescription = "Exit Fullscreen",
                                            tint = if (isNightMode) Color.White else Color(0xFF0F172A),
                                            modifier = Modifier.size(22.dp)
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

    // ================= MODAL DIALOGS =================
    // 1. Jump To Page Dialog (Google Drive Style)
    if (showJumpDialog && pageCount > 0) {
        var targetPageText by remember { mutableStateOf("${visibleCurrentPage + 1}") }
        var sliderValue by remember { mutableFloatStateOf((visibleCurrentPage + 1).toFloat()) }

        AlertDialog(
            onDismissRequest = { showJumpDialog = false },
            containerColor = themeColors.cardBg,
            title = {
                Text(
                    text = if (isBn) "পৃষ্ঠায় যান" else "Jump to Page",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isBn)
                            "১ থেকে $pageCount এর মধ্যে পৃষ্ঠা নির্বাচন করুন"
                        else
                            "Select a page between 1 and $pageCount",
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Slider(
                        value = sliderValue,
                        onValueChange = {
                            sliderValue = it
                            targetPageText = it.toInt().toString()
                        },
                        valueRange = 1f..pageCount.toFloat(),
                        steps = if (pageCount > 2) pageCount - 2 else 0,
                        colors = SliderDefaults.colors(
                            thumbColor = themeColors.buttonEqualBg,
                            activeTrackColor = themeColors.buttonEqualBg
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = targetPageText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                targetPageText = input
                                val num = input.toIntOrNull()
                                if (num != null && num in 1..pageCount) {
                                    sliderValue = num.toFloat()
                                }
                            }
                        },
                        label = { Text(if (isBn) "পৃষ্ঠা নম্বর" else "Page Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.buttonEqualBg,
                            focusedTextColor = themeColors.displayText,
                            unfocusedTextColor = themeColors.displayText
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = targetPageText.toIntOrNull()
                        val targetPage = if (num != null && num in 1..pageCount) {
                            num - 1
                        } else {
                            sliderValue.toInt() - 1
                        }
                        coroutineScope.launch {
                            verticalLazyListState.animateScrollToItem(targetPage.coerceIn(0, max(0, pageCount - 1)))
                        }
                        showJumpDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg, contentColor = Color.White)
                ) {
                    Text(if (isBn) "যান" else "Go")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJumpDialog = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText)
                }
            }
        )
    }

    // 2. Document Details Dialog
    if (showDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
            containerColor = themeColors.cardBg,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = themeColors.buttonEqualBg,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "নথির তথ্য ও বিবরণ" else "Document Information",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailRow(
                        label = if (isBn) "ফাইলের নাম" else "File Name",
                        value = fileName,
                        themeColors = themeColors
                    )
                    DetailRow(
                        label = if (isBn) "মোট পৃষ্ঠা" else "Total Pages",
                        value = "$pageCount",
                        themeColors = themeColors
                    )
                    DetailRow(
                        label = if (isBn) "ফাইল সাইজ" else "File Size",
                        value = formatFileSize(fileSizeBytes),
                        themeColors = themeColors
                    )
                    if (fileLastModifiedMs > 0) {
                        DetailRow(
                            label = if (isBn) "সর্বশেষ পরিবর্তন" else "Last Modified",
                            value = formatFormattedDateTime(fileLastModifiedMs, isBn),
                            themeColors = themeColors
                        )
                    }
                    if (filePath.isNotBlank()) {
                        DetailRow(
                            label = if (isBn) "লোকেশন" else "Location Path",
                            value = filePath,
                            themeColors = themeColors
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDetailsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg, contentColor = Color.White)
                ) {
                    Text(if (isBn) "ঠিক আছে" else "OK")
                }
            }
        )
    }

    // 3. Select & Copy Page Text Dialog (PdfBox Powered)
    if (showTextSelectDialogPage != null) {
        val pageIdx = showTextSelectDialogPage!!
        var pageText by remember(pageIdx, pdfTextPages) {
            mutableStateOf(pdfTextPages.getOrNull(pageIdx) ?: "")
        }
        var isExtractingPageText by remember(pageIdx) {
            mutableStateOf(pageText.isBlank())
        }

        LaunchedEffect(pageIdx) {
            if (pageText.isBlank() && pdfUri != null) {
                isExtractingPageText = true
                withContext(Dispatchers.IO) {
                    try {
                        try {
                            com.tom_roush.pdfbox.android.PDFBoxResourceLoader.init(context.applicationContext)
                        } catch (_: Exception) {}
                        val inputStream = context.contentResolver.openInputStream(pdfUri!!)
                        if (inputStream != null) {
                            val pdDoc = com.tom_roush.pdfbox.pdmodel.PDDocument.load(inputStream)
                            pdDoc.use { doc ->
                                val stripper = com.tom_roush.pdfbox.text.PDFTextStripper()
                                stripper.startPage = pageIdx + 1
                                stripper.endPage = pageIdx + 1
                                val extracted = stripper.getText(doc)?.trim() ?: ""
                                withContext(Dispatchers.Main) {
                                    if (extracted.isNotBlank()) {
                                        pageText = extracted
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        withContext(Dispatchers.Main) {
                            isExtractingPageText = false
                        }
                    }
                }
            } else {
                isExtractingPageText = false
            }
        }

        AlertDialog(
            onDismissRequest = { showTextSelectDialogPage = null },
            containerColor = themeColors.cardBg,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isBn) "পৃষ্ঠা ${pageIdx + 1} - টেক্সট নির্বাচন ও কপি" else "Page ${pageIdx + 1} - Select & Copy",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    IconButton(
                        onClick = { showTextSelectDialogPage = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = themeColors.displayText.copy(alpha = 0.6f)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                ) {
                    Text(
                        text = if (isBn) "নিচের টেক্সট সিলেক্ট করুন অথবা এক ক্লিকে সম্পূর্ণ কপি করুন:" else "Select text below or tap Copy All:",
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.15f)), RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isNightMode) Color(0xFF0F172A) else Color(0xFFF1F5F9)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            if (isExtractingPageText) {
                                Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        color = themeColors.buttonEqualBg,
                                        modifier = Modifier.size(28.dp),
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (isBn) "টেক্সট লোড হচ্ছে..." else "Extracting page text...",
                                        fontSize = 11.5.sp,
                                        color = themeColors.displayText.copy(alpha = 0.7f)
                                    )
                                }
                            } else if (pageText.isNotBlank()) {
                                SelectionContainer {
                                    Text(
                                        text = pageText,
                                        fontSize = 13.5.sp,
                                        lineHeight = 20.sp,
                                        color = themeColors.displayText
                                    )
                                }
                            } else {
                                Text(
                                    text = if (isBn) "এই পৃষ্ঠা থেকে কোনো টেক্সট উদ্ধার করা যায়নি (হতে পারে এটি স্ক্যান করা ইমেজ)।" else "No selectable text found on this page (it may be a scanned image).",
                                    fontSize = 12.sp,
                                    color = themeColors.displayText.copy(alpha = 0.5f),
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (pageText.isNotBlank()) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("PDF Page ${pageIdx + 1}", pageText)
                                clipboard.setPrimaryClip(clip)
                                android.widget.Toast.makeText(
                                    context,
                                    if (isBn) "সম্পূর্ণ টেক্সট ক্লিপবোর্ডে কপি করা হয়েছে" else "Copied all page text to clipboard",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                showTextSelectDialogPage = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg, contentColor = Color.White),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isBn) "সব কপি করুন" else "Copy All")
                        }
                    }
                    TextButton(
                        onClick = { showTextSelectDialogPage = null },
                        modifier = Modifier.weight(if (pageText.isNotBlank()) 0.5f else 1f)
                    ) {
                        Text(if (isBn) "বন্ধ করুন" else "Cancel", color = themeColors.displayText)
                    }
                }
            }
        )
    }

    // 4. Long-Press / More Options Bottom Sheet
    if (selectedFileForAction != null) {
        val activeItem = selectedFileForAction!!
        val isItemFav = favoritePdfList.any { fav ->
            (fav.path.isNotBlank() && fav.path == activeItem.path) || (fav.path.isBlank() && fav.uri == activeItem.uri)
        }

        ModalBottomSheet(
            onDismissRequest = { selectedFileForAction = null },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            dragHandle = {
                Surface(
                    modifier = Modifier.padding(top = 10.dp, bottom = 6.dp),
                    color = themeColors.displayText.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Box(modifier = Modifier.size(width = 38.dp, height = 4.dp))
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                // Header: File Info Preview
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.14f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeItem.name,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${formatFileSize(activeItem.sizeBytes)}${if (activeItem.dateModifiedMs > 0) " • " + formatFormattedDateTime(activeItem.dateModifiedMs, isBn) else ""}",
                            fontSize = 11.sp,
                            color = themeColors.buttonEqualBg,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(6.dp))

                // Action 1: Open PDF
                PdfActionOptionItem(
                    icon = Icons.Default.Visibility,
                    title = if (isBn) "পিডিএফ ওপেন করুন" else "Open PDF",
                    color = themeColors.buttonEqualBg,
                    textColor = themeColors.displayText,
                    onClick = {
                        pdfUri = activeItem.uri
                        fileName = activeItem.name
                        fileSizeBytes = activeItem.sizeBytes
                        fileLastModifiedMs = activeItem.dateModifiedMs
                        filePath = activeItem.path
                        currentPageScale = 1.0f
                        rotationDegrees = 0
                        selectedFileForAction = null
                    }
                )

                // Action 2: Rename PDF
                PdfActionOptionItem(
                    icon = Icons.Default.Edit,
                    title = if (isBn) "ফাইলের নাম পরিবর্তন (রিনেম)" else "Rename File",
                    color = themeColors.buttonEqualBg,
                    textColor = themeColors.displayText,
                    onClick = {
                        val baseName = activeItem.name.removeSuffix(".pdf").removeSuffix(".PDF")
                        newRenameText = baseName
                        fileToRename = activeItem
                        selectedFileForAction = null
                    }
                )

                // Action 3: Toggle Favorite
                PdfActionOptionItem(
                    icon = if (isItemFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    title = if (isItemFav) {
                        if (isBn) "প্রিয় তালিকা থেকে বাদ দিন" else "Remove from Favorites"
                    } else {
                        if (isBn) "প্রিয় তালিকায় যোগ করুন" else "Add to Favorites"
                    },
                    color = if (isItemFav) Color(0xFFEF4444) else themeColors.displayText.copy(alpha = 0.8f),
                    textColor = themeColors.displayText,
                    onClick = {
                        val nowFav = toggleFavoritePdf(context, activeItem)
                        selectedFileForAction = null
                        coroutineScope.launch {
                            val favs = getFavoritePdfs(context)
                            favoritePdfList = favs
                        }
                        android.widget.Toast.makeText(
                            context,
                            if (nowFav) {
                                if (isBn) "প্রিয় তালিকায় যুক্ত করা হয়েছে" else "Added to favorites"
                            } else {
                                if (isBn) "প্রিয় তালিকা থেকে বাদ দেওয়া হয়েছে" else "Removed from favorites"
                            },
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                )

                // Action 4: Share PDF
                PdfActionOptionItem(
                    icon = Icons.Default.Share,
                    title = if (isBn) "পিডিএফ শেয়ার করুন" else "Share PDF",
                    color = themeColors.buttonEqualBg,
                    textColor = themeColors.displayText,
                    onClick = {
                        sharePdfItem(context, activeItem)
                        selectedFileForAction = null
                    }
                )

                // Action 5: Print PDF
                PdfActionOptionItem(
                    icon = Icons.Default.Print,
                    title = if (isBn) "প্রিন্ট করুন" else "Print PDF",
                    color = themeColors.buttonEqualBg,
                    textColor = themeColors.displayText,
                    onClick = {
                        printPdfDocument(context, activeItem)
                        selectedFileForAction = null
                    }
                )

                // Action 6: Delete PDF
                PdfActionOptionItem(
                    icon = Icons.Default.Delete,
                    title = if (isBn) "ডিলেট করুন" else "Delete File",
                    color = Color(0xFFEF4444),
                    textColor = Color(0xFFEF4444),
                    onClick = {
                        fileToDelete = activeItem
                        selectedFileForAction = null
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }

    // 5. Rename Dialog
    if (fileToRename != null) {
        val targetItem = fileToRename!!
        AlertDialog(
            onDismissRequest = { fileToRename = null },
            containerColor = themeColors.cardBg,
            title = {
                Text(
                    text = if (isBn) "ফাইলের নাম পরিবর্তন করুন" else "Rename PDF File",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isBn) "নতুন নাম লিখুন (.pdf স্বয়ংক্রিয়ভাবে যুক্ত হবে):" else "Enter new name (.pdf will be added automatically):",
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = newRenameText,
                        onValueChange = { newRenameText = it },
                        singleLine = true,
                        placeholder = { Text(if (isBn) "ফাইলের নাম..." else "File name...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.buttonEqualBg,
                            focusedTextColor = themeColors.displayText,
                            unfocusedTextColor = themeColors.displayText,
                            cursorColor = themeColors.buttonEqualBg
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newRenameText.trim()
                        if (trimmed.isNotBlank()) {
                            val success = renamePdfFileItem(context, targetItem, trimmed)
                            if (success) {
                                val newFullName = if (trimmed.endsWith(".pdf", ignoreCase = true)) trimmed else "$trimmed.pdf"
                                pdfFileList = pdfFileList.map {
                                    if (it.path == targetItem.path || it.uri == targetItem.uri) {
                                        it.copy(name = newFullName)
                                    } else it
                                }
                                coroutineScope.launch {
                                    val favs = getFavoritePdfs(context)
                                    val recents = getRecentPdfHistory(context)
                                    favoritePdfList = favs
                                    historyPdfList = recents
                                }
                                android.widget.Toast.makeText(
                                    context,
                                    if (isBn) "ফাইলের নাম সফলভাবে পরিবর্তন করা হয়েছে" else "File renamed successfully",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                android.widget.Toast.makeText(
                                    context,
                                    if (isBn) "ফাইলের নাম পরিবর্তন করা যায়নি (স্টোরেজ অনুমতি চেক করুন)" else "Failed to rename file",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        fileToRename = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg, contentColor = Color.White)
                ) {
                    Text(if (isBn) "সংরক্ষণ করুন" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToRename = null }) {
                    Text(if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText)
                }
            }
        )
    }

    // 6. Delete Confirmation Dialog
    if (fileToDelete != null) {
        val targetItem = fileToDelete!!
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            containerColor = themeColors.cardBg,
            title = {
                Text(
                    text = if (isBn) "ফাইল ডিলেট নিশ্চিতকরণ" else "Confirm Delete",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444)
                )
            },
            text = {
                Text(
                    text = if (isBn)
                        "আপনি কি নিশ্চিত যে '${targetItem.name}' ফাইলটি মুছে ফেলতে চান? এটি ডিভাইস এবং পড়ার ইতিহাস থেকে মুছে যাবে।"
                    else
                        "Are you sure you want to delete '${targetItem.name}'? This will remove the file from storage and history.",
                    fontSize = 13.sp,
                    color = themeColors.displayText
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val deleted = removePdfFromStorageAndTracking(context, targetItem)
                        pdfFileList = pdfFileList.filterNot { it.path == targetItem.path || it.uri == targetItem.uri }
                        coroutineScope.launch {
                            val favs = getFavoritePdfs(context)
                            val recents = getRecentPdfHistory(context)
                            favoritePdfList = favs
                            historyPdfList = recents
                        }
                        android.widget.Toast.makeText(
                            context,
                            if (deleted) {
                                if (isBn) "ফাইলটি সফলভাবে মুছে ফেলা হয়েছে" else "File deleted successfully"
                            } else {
                                if (isBn) "তালিকা থেকে সরিয়ে দেওয়া হয়েছে" else "Removed from list"
                            },
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        fileToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White)
                ) {
                    Text(if (isBn) "ডিলেট করুন" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) {
                    Text(if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText)
                }
            }
        )
    }

    // 7. Delete Currently Viewed PDF in Reader
    if (showDeleteCurrentFileDialog && pdfUri != null) {
        val currentFileName = fileName.ifBlank { "PDF Document" }
        AlertDialog(
            onDismissRequest = { showDeleteCurrentFileDialog = false },
            containerColor = themeColors.cardBg,
            title = {
                Text(
                    text = if (isBn) "ফাইল ডিলেট নিশ্চিতকরণ" else "Confirm Delete",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444)
                )
            },
            text = {
                Text(
                    text = if (isBn)
                        "আপনি কি নিশ্চিত যে '$currentFileName' ফাইলটি মুছে ফেলতে চান? এটি ডিভাইস এবং পড়ার ইতিহাস থেকে মুছে যাবে।"
                    else
                        "Are you sure you want to delete '$currentFileName'? This will remove the file from storage and history.",
                    fontSize = 13.sp,
                    color = themeColors.displayText
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentItem = PdfFileItem(
                            name = fileName,
                            uri = pdfUri!!,
                            sizeBytes = fileSizeBytes,
                            dateModifiedMs = fileLastModifiedMs,
                            path = filePath
                        )
                        val deleted = removePdfFromStorageAndTracking(context, currentItem)
                        pdfFileList = pdfFileList.filterNot { it.path == currentItem.path || it.uri == currentItem.uri }
                        coroutineScope.launch {
                            val favs = getFavoritePdfs(context)
                            val recents = getRecentPdfHistory(context)
                            favoritePdfList = favs
                            historyPdfList = recents
                        }
                        android.widget.Toast.makeText(
                            context,
                            if (deleted) {
                                if (isBn) "ফাইলটি সফলভাবে মুছে ফেলা হয়েছে" else "File deleted successfully"
                            } else {
                                if (isBn) "তালিকা থেকে সরিয়ে দেওয়া হয়েছে" else "Removed from list"
                            },
                            android.widget.Toast.LENGTH_SHORT
                        ).show()

                        // Close reader
                        pdfUri = null
                        pageCount = 0
                        isPdfLoading = false
                        isSearchActive = false
                        pdfSearchQuery = ""
                        showDeleteCurrentFileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White)
                ) {
                    Text(if (isBn) "ডিলেট করুন" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCurrentFileDialog = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText)
                }
            }
        )
    }
}

// Helper to strictly clamp pan offset so zoomed page NEVER drifts out of canvas
private fun clampOffset(
    offset: Offset,
    scale: Float,
    viewportWidth: Float,
    viewportHeight: Float
): Offset {
    if (scale <= 1.0f) return Offset.Zero
    val maxPanX = (viewportWidth * (scale - 1.0f)) / 2f
    val maxPanY = (viewportHeight * (scale - 1.0f)) / 2f
    return Offset(
        x = offset.x.coerceIn(-maxPanX, maxPanX),
        y = offset.y.coerceIn(-maxPanY, maxPanY)
    )
}

@Composable
private fun PdfPageViewerItem(
    context: Context,
    pdfUri: Uri,
    pageIndex: Int,
    isNightMode: Boolean,
    rotationDegrees: Int,
    density: Float,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    var pageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Render single page bitmap asynchronously
    LaunchedEffect(pdfUri, pageIndex, isNightMode, rotationDegrees) {
        isLoading = true
        withContext(Dispatchers.IO) {
            val bmp = renderPdfPageBitmap(
                context = context,
                uri = pdfUri,
                pageIndex = pageIndex,
                isNightMode = isNightMode,
                rotationDegrees = rotationDegrees,
                density = density
            )
            withContext(Dispatchers.Main) {
                pageBitmap = bmp
                isLoading = false
            }
        }
    }

    val pageAspectRatio = if (pageBitmap != null && pageBitmap!!.height > 0) {
        pageBitmap!!.width.toFloat() / pageBitmap!!.height.toFloat()
    } else {
        0.707f // Default A4 Aspect Ratio
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(pageAspectRatio)
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    color = themeColors.buttonEqualBg,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isBn) "পৃষ্ঠা ${pageIndex + 1} লোড হচ্ছে..." else "Loading Page ${pageIndex + 1}...",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        } else if (pageBitmap != null) {
            Image(
                bitmap = pageBitmap!!.asImageBitmap(),
                contentDescription = "PDF Page ${pageIndex + 1}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

private fun renderPdfPageBitmap(
    context: Context,
    uri: Uri,
    pageIndex: Int,
    isNightMode: Boolean,
    rotationDegrees: Int,
    density: Float
): Bitmap? {
    var pfd: ParcelFileDescriptor? = null
    var renderer: PdfRenderer? = null
    var page: PdfRenderer.Page? = null
    return try {
        pfd = openPdfParcelFileDescriptor(context, uri) ?: return null
        renderer = PdfRenderer(pfd)
        val safeIdx = pageIndex.coerceIn(0, max(0, renderer.pageCount - 1))
        if (safeIdx !in 0 until renderer.pageCount) {
            return null
        }
        page = renderer.openPage(safeIdx)
        val renderFactor = (density * 1.5f).coerceIn(2.0f, 3.5f)
        val targetWidth = (page.width * renderFactor).toInt().coerceIn(400, 3000)
        val targetHeight = (page.height * renderFactor).toInt().coerceIn(400, 4200)

        val rawBmp = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(rawBmp)
        canvas.drawColor(AndroidColor.WHITE)
        page.render(rawBmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        try { page.close() } catch (_: Exception) {}
        page = null
        try { renderer.close() } catch (_: Exception) {}
        renderer = null
        try { pfd.close() } catch (_: Exception) {}
        pfd = null

        val rotatedBmp = if (rotationDegrees % 360 != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            val rot = Bitmap.createBitmap(rawBmp, 0, 0, rawBmp.width, rawBmp.height, matrix, true)
            if (rot != rawBmp) rawBmp.recycle()
            rot
        } else {
            rawBmp
        }

        if (isNightMode) {
            val invBmp = Bitmap.createBitmap(rotatedBmp.width, rotatedBmp.height, Bitmap.Config.ARGB_8888)
            val invCanvas = Canvas(invBmp)
            val paint = Paint()
            val colorMatrix = ColorMatrix(
                floatArrayOf(
                    -1.0f, 0.0f, 0.0f, 0.0f, 255.0f,
                    0.0f, -1.0f, 0.0f, 0.0f, 255.0f,
                    0.0f, 0.0f, -1.0f, 0.0f, 255.0f,
                    0.0f, 0.0f, 0.0f, 1.0f, 0.0f
                )
            )
            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            invCanvas.drawBitmap(rotatedBmp, 0f, 0f, paint)
            if (invBmp != rotatedBmp) rotatedBmp.recycle()
            invBmp
        } else {
            rotatedBmp
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } finally {
        try { page?.close() } catch (_: Exception) {}
        try { renderer?.close() } catch (_: Exception) {}
        try { pfd?.close() } catch (_: Exception) {}
    }
}

@Composable
private fun DetailRow(label: String, value: String, themeColors: CalculatorThemeColors) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = themeColors.buttonEqualBg,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            color = themeColors.displayText,
            fontWeight = FontWeight.Normal
        )
        HorizontalDivider(
            color = themeColors.displayText.copy(alpha = 0.08f),
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun PdfActionOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    color: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 11.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}

// ================= 2. PDF MAKER TOOL =================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfMakerTool(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

    var makerTab by remember { mutableIntStateOf(0) } // 0 = Text to PDF, 1 = Image to PDF, 2 = History
    var previewFile by remember { mutableStateOf<File?>(null) }
    var historyList by remember { mutableStateOf<List<PdfFileItem>>(emptyList()) }

    // Text to PDF Form States
    var docTitle by remember { mutableStateOf("") }
    var docSubtitle by remember { mutableStateOf("") }
    var docBody by remember { mutableStateOf("") }
    var authorName by remember { mutableStateOf("") }
    var pageSize by remember { mutableStateOf("A4 Portrait") }
    var selectedColorHex by remember { mutableIntStateOf(0xFF047857.toInt()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var includePageNumbers by remember { mutableStateOf(true) }
    var createdPdfFile by remember { mutableStateOf<File?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    // Image to PDF States
    val selectedImages = remember { mutableStateListOf<PdfImageItem>() }
    var isImageGenerating by remember { mutableStateOf(false) }
    var createdImagePdfFile by remember { mutableStateOf<File?>(null) }
    var imageThemeColorHex by remember { mutableIntStateOf(0xFF047857.toInt()) }

    // Collapsing Top Header on scroll (Quran & Hadith style)
    var isHeaderVisible by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < -12f && isHeaderVisible) {
                    isHeaderVisible = false
                } else if (delta > 12f && !isHeaderVisible) {
                    isHeaderVisible = true
                }
                return Offset.Zero
            }
        }
    }

    // Intercept back presses
    BackHandler {
        if (previewFile != null) {
            previewFile = null
        } else {
            onBackClick()
        }
    }

    // Reactively refresh PDF History
    LaunchedEffect(makerTab, createdPdfFile, createdImagePdfFile) {
        historyList = getPdfHistory(context)
    }

    val singleImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    val multiImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                selectedImages.add(PdfImageItem(uri = uri))
            }
        }
    }

    val themeColorsList = listOf(
        0xFF047857.toInt() to (if (isBn) "মরু সবুজ" else "Emerald Green"),
        0xFF1D4ED8.toInt() to (if (isBn) "রয়্যাল ব্লু" else "Royal Blue"),
        0xFF7C3AED.toInt() to (if (isBn) "পার্পল" else "Purple"),
        0xFFB45309.toInt() to (if (isBn) "গোল্ডেন ব্রাউন" else "Golden Brown"),
        0xFF0F172A.toInt() to (if (isBn) "ডিপ চারকোল" else "Deep Charcoal")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .nestedScroll(nestedScrollConnection)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Collapsing Top Header Bar
            AnimatedVisibility(
                visible = isHeaderVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    color = themeColors.cardBg,
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = themeColors.displayText
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "পিডিএফ মেকার" else "PDF Maker",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                            Text(
                                text = if (isBn) "টেক্সট ও ছবি দিয়ে প্রফেশনাল PDF বানান" else "Create text and photo PDFs easily",
                                fontSize = 11.sp,
                                color = themeColors.buttonEqualBg,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Tab Row Switcher
            TabRow(
                selectedTabIndex = makerTab,
                containerColor = themeColors.cardBg,
                contentColor = themeColors.buttonEqualBg
            ) {
                Tab(
                    selected = makerTab == 0,
                    onClick = { makerTab = 0 },
                    text = { Text(if (isBn) "টেক্সট PDF" else "Text to PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = makerTab == 1,
                    onClick = { makerTab = 1 },
                    text = { Text(if (isBn) "ছবি থেকে PDF" else "Image to PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = makerTab == 2,
                    onClick = { makerTab = 2 },
                    text = { Text(if (isBn) "তৈরি ফাইলসমূহ (${historyList.size})" else "History (${historyList.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            // Tab Content
            when (makerTab) {
                0 -> {
                    // ================= TAB 1: TEXT TO PDF =================
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = docTitle,
                            onValueChange = { docTitle = it },
                            label = { Text(if (isBn) "ডকুমেন্ট বা লেখার শিরোনাম *" else "Document Title *") },
                            placeholder = { Text(if (isBn) "যেমন: বার্ষিক পরীক্ষার প্রস্তুতি নোট" else "e.g. Annual Study Notes") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = themeColors.displayText,
                                unfocusedTextColor = themeColors.displayText,
                                cursorColor = themeColors.buttonEqualBg,
                                focusedContainerColor = themeColors.buttonEqualBg.copy(alpha = 0.04f),
                                unfocusedContainerColor = themeColors.buttonEqualBg.copy(alpha = 0.04f),
                                focusedBorderColor = themeColors.buttonEqualBg,
                                unfocusedBorderColor = themeColors.buttonEqualBg.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = docSubtitle,
                            onValueChange = { docSubtitle = it },
                            label = { Text(if (isBn) "উপ-শিরোনাম বা বিষয় (ঐচ্ছিক)" else "Subtitle / Subject (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = themeColors.displayText,
                                unfocusedTextColor = themeColors.displayText,
                                cursorColor = themeColors.buttonEqualBg,
                                focusedContainerColor = themeColors.buttonEqualBg.copy(alpha = 0.04f),
                                unfocusedContainerColor = themeColors.buttonEqualBg.copy(alpha = 0.04f),
                                focusedBorderColor = themeColors.buttonEqualBg,
                                unfocusedBorderColor = themeColors.buttonEqualBg.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = authorName,
                            onValueChange = { authorName = it },
                            label = { Text(if (isBn) "লেখক বা প্রতিষ্ঠানের নাম (ঐচ্ছিক)" else "Author / Institution Name (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = themeColors.displayText,
                                unfocusedTextColor = themeColors.displayText,
                                cursorColor = themeColors.buttonEqualBg,
                                focusedContainerColor = themeColors.buttonEqualBg.copy(alpha = 0.04f),
                                unfocusedContainerColor = themeColors.buttonEqualBg.copy(alpha = 0.04f),
                                focusedBorderColor = themeColors.buttonEqualBg,
                                unfocusedBorderColor = themeColors.buttonEqualBg.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = docBody,
                            onValueChange = { docBody = it },
                            label = { Text(if (isBn) "মূল বিবরণ বা লেখার বিষয়বস্তু *" else "Main Body Content *") },
                            placeholder = { Text(if (isBn) "এখানে আপনার বিস্তারিত বক্তব্য, রচনা বা নোট লিখুন..." else "Write detailed content here...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 140.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = themeColors.displayText,
                                unfocusedTextColor = themeColors.displayText,
                                cursorColor = themeColors.buttonEqualBg,
                                focusedContainerColor = themeColors.buttonEqualBg.copy(alpha = 0.04f),
                                unfocusedContainerColor = themeColors.buttonEqualBg.copy(alpha = 0.04f),
                                focusedBorderColor = themeColors.buttonEqualBg,
                                unfocusedBorderColor = themeColors.buttonEqualBg.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Theme Accent Color Selector
                        Text(
                            text = if (isBn) "থিম অ্যাকসেন্ট কালার" else "Theme Accent Color",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = themeColors.displayText
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            themeColorsList.forEach { (colorInt, name) ->
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(colorInt))
                                        .border(
                                            width = if (selectedColorHex == colorInt) 3.dp else 1.dp,
                                            color = if (selectedColorHex == colorInt) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColorHex = colorInt },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selectedColorHex == colorInt) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Attach Header Cover Image
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = { singleImagePickerLauncher.launch("image/*") },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = themeColors.buttonEqualBg)
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (selectedImageUri != null) (if (isBn) "ছবি পরিবর্তন" else "Change Image") else (if (isBn) "হেডার ছবি যুক্ত করুন" else "Add Header Photo"))
                            }

                            if (selectedImageUri != null) {
                                TextButton(onClick = { selectedImageUri = null }) {
                                    Text(if (isBn) "ছবি মুছুন" else "Remove", color = Color.Red, fontSize = 12.sp)
                                }
                            }
                        }

                        // Generate PDF Button
                        Button(
                            onClick = {
                                if (docTitle.isBlank() && docBody.isBlank()) {
                                    Toast.makeText(context, if (isBn) "শিরোনাম অথবা মূল বিবরণ লিখুন" else "Please write a title or content", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isGenerating = true
                                val file = generatePdfDocument(
                                    context = context,
                                    title = docTitle.ifBlank { "নথি" },
                                    subtitle = docSubtitle,
                                    body = docBody,
                                    author = authorName,
                                    imageUri = selectedImageUri,
                                    pageSizeStr = pageSize,
                                    colorHex = selectedColorHex,
                                    includePageNumbers = includePageNumbers
                                )
                                createdPdfFile = file
                                    if(file != null) previewFile = file
                                isGenerating = false
                                if (file != null) {
                                    Toast.makeText(context, if (isBn) "PDF সফলভাবে তৈরি হয়েছে!" else "PDF created successfully!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = themeColors.buttonEqualBg,
                                contentColor = Color.White
                            )
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isBn) "পিডিএফ তৈরি করুন" else "Create PDF Document", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Success Result Card
                        if (createdPdfFile != null) {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isBn) "পিডিএফ তৈরি সম্পন্ন!" else "PDF Ready!",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.displayText
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = createdPdfFile!!.name,
                                        fontSize = 12.sp,
                                        color = themeColors.displayText.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { previewFile = createdPdfFile },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg, contentColor = Color.White)
                                        ) {
                                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isBn) "প্রিভিউ" else "Preview", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = { sharePdfFile(context, createdPdfFile!!) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg, contentColor = Color.White)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isBn) "শেয়ার" else "Share", fontSize = 12.sp)
                                        }

                                        OutlinedButton(
                                            onClick = { savePdfToDownloads(context, createdPdfFile!!) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.5f)),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = themeColors.buttonEqualBg)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isBn) "সেভ" else "Save", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // ================= TAB 2: IMAGE TO PDF =================
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Button(
                            onClick = { multiImagePickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg, contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isBn) "গ্যালারি থেকে ছবি যুক্ত করুন" else "Add Images from Gallery", fontWeight = FontWeight.Bold)
                        }

                        if (selectedImages.isNotEmpty()) {
                            Text(
                                text = if (isBn) "নির্বাচিত ছবিসমূহ (${selectedImages.size}টি):" else "Selected Images (${selectedImages.size}):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )

                            selectedImages.forEachIndexed { idx, imgItem ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = themeColors.buttonEqualBg.copy(alpha = 0.15f),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${idx + 1}",
                                                    fontWeight = FontWeight.Bold,
                                                    color = themeColors.buttonEqualBg
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))
                                        
                                        coil.compose.AsyncImage(
                                            model = imgItem.uri,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )

                                        Spacer(modifier = Modifier.width(10.dp))

                                        OutlinedTextField(
                                            value = imgItem.title,
                                            onValueChange = { newTitle ->
                                                selectedImages[idx] = imgItem.copy(title = newTitle)
                                            },
                                            placeholder = { Text(if (isBn) "শিরোনাম" else "Title", fontSize = 12.sp) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = themeColors.displayText,
                                                unfocusedTextColor = themeColors.displayText,
                                                cursorColor = themeColors.buttonEqualBg,
                                                focusedContainerColor = themeColors.buttonEqualBg.copy(alpha = 0.04f),
                                                unfocusedContainerColor = themeColors.buttonEqualBg.copy(alpha = 0.04f),
                                                focusedBorderColor = themeColors.buttonEqualBg,
                                                unfocusedBorderColor = themeColors.buttonEqualBg.copy(alpha = 0.3f)
                                            )
                                        )

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            if (idx > 0) {
                                                IconButton(
                                                    onClick = { 
                                                        val temp = selectedImages[idx]
                                                        selectedImages[idx] = selectedImages[idx - 1]
                                                        selectedImages[idx - 1] = temp
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Up", tint = themeColors.buttonEqualBg)
                                                }
                                            }
                                            if (idx < selectedImages.size - 1) {
                                                IconButton(
                                                    onClick = { 
                                                        val temp = selectedImages[idx]
                                                        selectedImages[idx] = selectedImages[idx + 1]
                                                        selectedImages[idx + 1] = temp
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Down", tint = themeColors.buttonEqualBg)
                                                }
                                            }
                                        }

                                        IconButton(
                                            onClick = { selectedImages.removeAt(idx) }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    isImageGenerating = true
                                    val file = generatePdfFromImages(
                                        context = context,
                                        images = selectedImages,
                                        colorHex = imageThemeColorHex,
                                        pageSizeStr = "A4 Portrait"
                                    )
                                    createdImagePdfFile = file
                                    if(file != null) previewFile = file
                                    isImageGenerating = false
                                    if (file != null) {
                                        Toast.makeText(context, if (isBn) "ছবি থেকে PDF তৈরি সম্পন্ন!" else "Image PDF created successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg, contentColor = Color.White)
                            ) {
                                if (isImageGenerating) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                } else {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (isBn) "প্রিভিউ তৈরি করুন (Preview)" else "Generate Preview", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (createdImagePdfFile != null) {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isBn) "ছবি দিয়ে PDF তৈরি সফল!" else "Image PDF Created!",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.displayText
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = createdImagePdfFile!!.name,
                                        fontSize = 12.sp,
                                        color = themeColors.displayText.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { previewFile = createdImagePdfFile },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg, contentColor = Color.White)
                                        ) {
                                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isBn) "প্রিভিউ" else "Preview", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = { sharePdfFile(context, createdImagePdfFile!!) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg, contentColor = Color.White)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isBn) "শেয়ার" else "Share", fontSize = 12.sp)
                                        }

                                        OutlinedButton(
                                            onClick = { savePdfToDownloads(context, createdImagePdfFile!!) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.5f)),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = themeColors.buttonEqualBg)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isBn) "সেভ" else "Save", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // ================= TAB 3: SAVED HISTORY =================
                    if (historyList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.HistoryEdu,
                                    contentDescription = null,
                                    tint = themeColors.displayText.copy(alpha = 0.3f),
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (isBn) "এখনো কোনো PDF তৈরি করা হয়নি" else "No created PDFs yet",
                                    fontSize = 14.sp,
                                    color = themeColors.displayText.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(historyList) { item ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFEF4444).copy(alpha = 0.12f),
                                            modifier = Modifier.size(42.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.PictureAsPdf,
                                                    contentDescription = null,
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.name,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = themeColors.displayText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${formatFileSize(item.sizeBytes)} • ${formatFormattedDateTime(item.dateModifiedMs, isBn)}",
                                                fontSize = 11.sp,
                                                color = themeColors.displayText.copy(alpha = 0.6f)
                                            )
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = { previewFile = File(item.path) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Visibility,
                                                    contentDescription = "Preview",
                                                    tint = themeColors.buttonEqualBg,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { sharePdfFile(context, File(item.path)) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Share,
                                                    contentDescription = "Share",
                                                    tint = themeColors.buttonEqualBg,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    deletePdfFromHistory(context, item)
                                                    historyList = getPdfHistory(context)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = Color.Red,
                                                    modifier = Modifier.size(18.dp)
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

    // PDF Preview Modal Dialog
    if (previewFile != null) {
        PdfPreviewDialog(
            file = previewFile!!,
            onDismiss = { previewFile = null },
            themeColors = themeColors,
            isBn = isBn
        )
    }
}

// ================= DIALOGS & HELPER FUNCTIONS =================
@Composable
private fun PdfPreviewDialog(
    file: File,
    onDismiss: () -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean = true
) {
    val context = LocalContext.current
    var currentPageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pageCount by remember { mutableIntStateOf(0) }
    var currentPageIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(file, currentPageIndex) {
        withContext(Dispatchers.IO) {
            try {
                val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                if (pfd != null) {
                    val renderer = PdfRenderer(pfd)
                    pageCount = renderer.pageCount
                    if (currentPageIndex in 0 until pageCount) {
                        val page = renderer.openPage(currentPageIndex)
                        val bmp = Bitmap.createBitmap((page.width * 1.6f).toInt(), (page.height * 1.6f).toInt(), Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bmp)
                        canvas.drawColor(AndroidColor.WHITE)
                        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()
                        withContext(Dispatchers.Main) {
                            currentPageBitmap = bmp
                        }
                    }
                    renderer.close()
                    pfd.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f),
        containerColor = themeColors.cardBg,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = file.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = themeColors.displayText)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                ) {
                    if (currentPageBitmap != null) {
                        Image(
                            bitmap = currentPageBitmap!!.asImageBitmap(),
                            contentDescription = "PDF Page",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = themeColors.buttonEqualBg)
                        }
                    }
                }

                if (pageCount > 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (currentPageIndex > 0) currentPageIndex-- },
                            enabled = currentPageIndex > 0
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Prev")
                        }
                        Text(
                            text = "${currentPageIndex + 1} / $pageCount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        IconButton(
                            onClick = { if (currentPageIndex < pageCount - 1) currentPageIndex++ },
                            enabled = currentPageIndex < pageCount - 1
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { sharePdfFile(context, file) },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg, contentColor = Color.White)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isBn) "শেয়ার" else "Share")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = themeColors.buttonEqualBg)
            ) {
                Text(if (isBn) "বন্ধ করুন" else "Close")
            }
        }
    )
}

// ================= NATIVE DEVICE SCANNER & METADATA EXTRACTION =================
private fun scanDevicePdfFiles(context: Context): List<PdfFileItem> {
    val pdfList = mutableListOf<PdfFileItem>()
    val seenKeys = mutableSetOf<String>()

    fun addUniqueItem(name: String, uri: Uri, size: Long, dateMs: Long, path: String) {
        val cleanName = resolvePdfDisplayName(name, path, uri)
        val dedupeKey = if (path.isNotBlank()) path else uri.toString()
        if (!seenKeys.contains(dedupeKey)) {
            seenKeys.add(dedupeKey)
            pdfList.add(
                PdfFileItem(
                    name = cleanName,
                    uri = uri,
                    sizeBytes = max(0L, size),
                    dateModifiedMs = if (dateMs > 0L) dateMs else System.currentTimeMillis(),
                    path = path
                )
            )
        }
    }

    // 1. Query MediaStore Files Table
    try {
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.TITLE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.DATA
        )
        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ?"
        val selectionArgs = arrayOf("application/pdf", "%.pdf")

        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)
            val nameCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val titleCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE)
            val sizeCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
            val dateCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)
            val dataCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)

            while (cursor.moveToNext()) {
                val id = if (idCol != -1) cursor.getLong(idCol) else 0L
                val rawName = if (nameCol != -1) cursor.getString(nameCol) ?: "" else ""
                val rawTitle = if (titleCol != -1) cursor.getString(titleCol) ?: "" else ""
                val size = if (sizeCol != -1) cursor.getLong(sizeCol) else 0L
                val dateSec = if (dateCol != -1) cursor.getLong(dateCol) else 0L
                val path = if (dataCol != -1) cursor.getString(dataCol) ?: "" else ""

                val uri = if (id != 0L) {
                    android.content.ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
                } else if (path.isNotEmpty()) {
                    Uri.fromFile(File(path))
                } else null

                if (uri != null) {
                    val resolvedName = when {
                        rawName.isNotBlank() && !isGenericPlaceholder(rawName) -> rawName
                        path.isNotBlank() && File(path).name.isNotBlank() -> File(path).name
                        rawTitle.isNotBlank() -> if (rawTitle.endsWith(".pdf", ignoreCase = true)) rawTitle else "$rawTitle.pdf"
                        else -> rawName
                    }
                    val actualSize = if (size > 0L) size else if (path.isNotBlank()) File(path).length() else 0L
                    val actualDate = if (dateSec > 0L) dateSec * 1000L else if (path.isNotBlank()) File(path).lastModified() else System.currentTimeMillis()

                    addUniqueItem(resolvedName, uri, actualSize, actualDate, path)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // 2. Query MediaStore Downloads Table (API 29+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        try {
            val projection = arrayOf(
                MediaStore.Downloads._ID,
                MediaStore.Downloads.DISPLAY_NAME,
                MediaStore.Downloads.SIZE,
                MediaStore.Downloads.DATE_MODIFIED
            )
            context.contentResolver.query(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                projection,
                "${MediaStore.Downloads.MIME_TYPE} = ?",
                arrayOf("application/pdf"),
                "${MediaStore.Downloads.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndex(MediaStore.Downloads._ID)
                val nameCol = cursor.getColumnIndex(MediaStore.Downloads.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndex(MediaStore.Downloads.SIZE)
                val dateCol = cursor.getColumnIndex(MediaStore.Downloads.DATE_MODIFIED)

                while (cursor.moveToNext()) {
                    val id = if (idCol != -1) cursor.getLong(idCol) else 0L
                    val name = if (nameCol != -1) cursor.getString(nameCol) ?: "" else ""
                    val size = if (sizeCol != -1) cursor.getLong(sizeCol) else 0L
                    val dateSec = if (dateCol != -1) cursor.getLong(dateCol) else 0L

                    if (id != 0L) {
                        val uri = android.content.ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id)
                        addUniqueItem(name, uri, size, dateSec * 1000L, "")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 3. Fallback Scan of Standard Storage Directories
    try {
        val dirsToScan = listOfNotNull(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            context.getExternalFilesDir(null),
            context.filesDir,
            context.cacheDir
        )

        for (dir in dirsToScan) {
            if (dir.exists() && dir.isDirectory) {
                dir.walkTopDown()
                    .maxDepth(3)
                    .filter { it.isFile && it.extension.equals("pdf", ignoreCase = true) }
                    .forEach { file ->
                        addUniqueItem(
                            name = file.name,
                            uri = Uri.fromFile(file),
                            size = file.length(),
                            dateMs = file.lastModified(),
                            path = file.absolutePath
                        )
                    }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return pdfList
}

private fun isGenericPlaceholder(name: String): Boolean {
    val trimmed = name.trim().lowercase()
    return trimmed == "pdf document" || trimmed == "pdf file" || trimmed == "document.pdf" || trimmed == "untitled.pdf"
}

private fun resolvePdfDisplayName(name: String, path: String, uri: Uri): String {
    if (name.isNotBlank() && !isGenericPlaceholder(name)) {
        return if (name.endsWith(".pdf", ignoreCase = true)) name else "$name.pdf"
    }
    if (path.isNotBlank()) {
        val file = File(path)
        if (file.name.isNotBlank()) return file.name
    }
    val lastSegment = uri.lastPathSegment
    if (!lastSegment.isNullOrBlank()) {
        val extracted = lastSegment.substringAfterLast('/')
        if (extracted.isNotBlank()) {
            return if (extracted.endsWith(".pdf", ignoreCase = true)) extracted else "$extracted.pdf"
        }
    }
    return "Document.pdf"
}

private fun extractPdfMetadata(
    context: Context,
    uri: Uri,
    onResult: (name: String, size: Long, date: Long, path: String) -> Unit
) {
    var name = ""
    var size = 0L
    var date = System.currentTimeMillis()
    var path = ""

    try {
        if (uri.scheme == "file") {
            val file = File(uri.path ?: "")
            if (file.exists()) {
                name = file.name
                size = file.length()
                date = file.lastModified()
                path = file.absolutePath
            }
        } else {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIdx != -1) name = cursor.getString(nameIdx) ?: ""
                    if (sizeIdx != -1) size = cursor.getLong(sizeIdx)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    if (name.isBlank()) {
        name = uri.lastPathSegment?.substringAfterLast('/') ?: "PDF Document"
    }
    if (!name.endsWith(".pdf", ignoreCase = true)) {
        name = "$name.pdf"
    }

    onResult(name, size, date, path)
}

private fun openPdfParcelFileDescriptor(context: Context, uri: Uri): ParcelFileDescriptor? {
    return try {
        context.contentResolver.openFileDescriptor(uri, "r")
    } catch (e: Exception) {
        val path = uri.path
        val file = if (!path.isNullOrEmpty()) File(path) else null
        if (file != null && file.exists()) {
            try {
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
    }
}

internal fun doesPdfFileExist(context: Context, uri: Uri, path: String = ""): Boolean {
    return try {
        if (path.isNotBlank()) {
            val f = File(path)
            if (f.exists() && f.length() > 0) return true
        }
        if (uri.scheme == "file") {
            val f = uri.path?.let { File(it) }
            if (f != null && f.exists() && f.length() > 0) return true
        }
        context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            pfd.statSize > 0
        } ?: false
    } catch (_: Exception) {
        false
    }
}

internal suspend fun loadPdfDocument(context: Context, uri: Uri): PdfOpenResult = withContext(Dispatchers.IO) {
    var pfd: ParcelFileDescriptor? = null
    var renderer: PdfRenderer? = null
    try {
        pfd = openPdfParcelFileDescriptor(context, uri)
        if (pfd == null) {
            return@withContext PdfOpenResult.Error(
                reasonBn = "ফাইলটি খুঁজে পাওয়া যায়নি বা স্টোরেজ থেকে মুছে ফেলা হয়েছে।",
                reasonEn = "File not found or has been deleted from storage."
            )
        }
        val fileSize = try { pfd.statSize } catch (_: Exception) { -1L }
        if (fileSize == 0L) {
            return@withContext PdfOpenResult.Error(
                reasonBn = "ফাইলটি সম্পূর্ণ খালি (০ বাইট) অথবা ক্ষতিগ্রস্ত।",
                reasonEn = "The file is completely empty (0 bytes) or corrupted."
            )
        }
        try {
            renderer = PdfRenderer(pfd)
        } catch (se: SecurityException) {
            return@withContext PdfOpenResult.Error(
                reasonBn = "ফাইলটি পাসওয়ার্ড দ্বারা সুরক্ষিত (Password Protected), যা বর্তমানে সমর্থিত নয়।",
                reasonEn = "The file is password protected, which is currently not supported."
            )
        } catch (ioe: IOException) {
            return@withContext PdfOpenResult.Error(
                reasonBn = "ফাইলটি ক্ষতিগ্রস্ত (Corrupted) বা এটি কোনো বৈধ PDF নথি নয়।",
                reasonEn = "The file is corrupted or not a valid PDF document."
            )
        } catch (e: Exception) {
            return@withContext PdfOpenResult.Error(
                reasonBn = "ফাইলটি লোড করতে সমস্যা হয়েছে: ${e.localizedMessage ?: "অজানা ত্রুটি"}",
                reasonEn = "Failed to load document: ${e.localizedMessage ?: "Unknown error"}"
            )
        }
        val count = renderer.pageCount
        if (count <= 0) {
            return@withContext PdfOpenResult.Error(
                reasonBn = "PDF নথিতে কোনো পাতা পাওয়া যায়নি।",
                reasonEn = "No pages found in this PDF document."
            )
        }
        try { renderer.close() } catch (_: Exception) {}
        renderer = null
        try { pfd.close() } catch (_: Exception) {}
        pfd = null

        val textList = try {
            extractPdfTextByPage(context, uri, count)
        } catch (_: Exception) {
            emptyList()
        }

        PdfOpenResult.Success(pageCount = count, textPages = textList)
    } catch (e: Exception) {
        PdfOpenResult.Error(
            reasonBn = "ফাইলটি ওপেন করতে ব্যর্থ: ${e.localizedMessage ?: "ক্ষতিগ্রস্ত ফাইল"}",
            reasonEn = "Failed to open file: ${e.localizedMessage ?: "Corrupted file"}"
        )
    } finally {
        try { renderer?.close() } catch (_: Exception) {}
        try { pfd?.close() } catch (_: Exception) {}
    }
}

internal fun getFavoritePdfs(context: Context): List<PdfFileItem> {
    val list = mutableListOf<PdfFileItem>()
    try {
        val prefs = context.getSharedPreferences("pdf_reader_prefs", Context.MODE_PRIVATE)
        val jsonString = prefs.getString("favorite_pdfs", "[]") ?: "[]"
        val arr = JSONArray(jsonString)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val path = obj.optString("path", "")
            val uriStr = obj.optString("uri", "")
            val uri = if (uriStr.isNotEmpty()) Uri.parse(uriStr) else if (path.isNotEmpty()) Uri.fromFile(File(path)) else null
            if (uri != null && doesPdfFileExist(context, uri, path)) {
                list.add(
                    PdfFileItem(
                        name = obj.optString("name", "Document.pdf"),
                        uri = uri,
                        sizeBytes = obj.optLong("size", 0L),
                        dateModifiedMs = obj.optLong("date", 0L),
                        path = path
                    )
                )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

internal fun toggleFavoritePdf(context: Context, item: PdfFileItem): Boolean {
    try {
        val prefs = context.getSharedPreferences("pdf_reader_prefs", Context.MODE_PRIVATE)
        val jsonString = prefs.getString("favorite_pdfs", "[]") ?: "[]"
        val arr = JSONArray(jsonString)
        val targetKey = if (item.path.isNotBlank()) item.path else item.uri.toString()
        var foundIndex = -1
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val key = if (obj.optString("path", "").isNotBlank()) obj.optString("path") else obj.optString("uri")
            if (key == targetKey) {
                foundIndex = i
                break
            }
        }
        val isNowFavorite: Boolean
        val newArr = JSONArray()
        if (foundIndex != -1) {
            for (i in 0 until arr.length()) {
                if (i != foundIndex) newArr.put(arr.getJSONObject(i))
            }
            isNowFavorite = false
        } else {
            val newObj = JSONObject().apply {
                put("name", item.name)
                put("path", item.path)
                put("uri", item.uri.toString())
                put("size", item.sizeBytes)
                put("date", item.dateModifiedMs)
            }
            newArr.put(newObj)
            for (i in 0 until arr.length()) {
                newArr.put(arr.getJSONObject(i))
            }
            isNowFavorite = true
        }
        prefs.edit().putString("favorite_pdfs", newArr.toString()).apply()
        return isNowFavorite
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}

internal fun recordPdfToRecentHistory(context: Context, item: PdfFileItem) {
    try {
        val prefs = context.getSharedPreferences("pdf_reader_prefs", Context.MODE_PRIVATE)
        val jsonString = prefs.getString("recent_opened_pdfs", "[]") ?: "[]"
        val arr = JSONArray(jsonString)
        val targetKey = if (item.path.isNotBlank()) item.path else item.uri.toString()
        val newArr = JSONArray()
        val newObj = JSONObject().apply {
            put("name", item.name)
            put("path", item.path)
            put("uri", item.uri.toString())
            put("size", item.sizeBytes)
            put("date", item.dateModifiedMs)
            put("lastOpenedMs", System.currentTimeMillis())
        }
        newArr.put(newObj)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val key = if (obj.optString("path", "").isNotBlank()) obj.optString("path") else obj.optString("uri")
            if (key != targetKey && newArr.length() < 60) {
                newArr.put(obj)
            }
        }
        prefs.edit().putString("recent_opened_pdfs", newArr.toString()).apply()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

internal fun getRecentPdfHistory(context: Context): List<PdfFileItem> {
    val list = mutableListOf<PdfFileItem>()
    try {
        val prefs = context.getSharedPreferences("pdf_reader_prefs", Context.MODE_PRIVATE)
        val jsonString = prefs.getString("recent_opened_pdfs", "[]") ?: "[]"
        val arr = JSONArray(jsonString)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val path = obj.optString("path", "")
            val uriStr = obj.optString("uri", "")
            val uri = if (uriStr.isNotEmpty()) Uri.parse(uriStr) else if (path.isNotEmpty()) Uri.fromFile(File(path)) else null
            if (uri != null && doesPdfFileExist(context, uri, path)) {
                list.add(
                    PdfFileItem(
                        name = obj.optString("name", "Document.pdf"),
                        uri = uri,
                        sizeBytes = obj.optLong("size", 0L),
                        dateModifiedMs = obj.optLong("lastOpenedMs", obj.optLong("date", 0L)),
                        path = path
                    )
                )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

internal fun printPdfDocument(context: Context, uri: Uri, fileName: String) {
    try {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager != null) {
            val cleanTitle = fileName.ifBlank { "PDF_Document" }.replace(".pdf", "")
            printManager.print(cleanTitle, object : PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes?,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback?,
                    extras: android.os.Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }
                    val info = PrintDocumentInfo.Builder("$cleanTitle.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .build()
                    callback?.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor?,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback?
                ) {
                    var inPfd: ParcelFileDescriptor? = null
                    try {
                        inPfd = openPdfParcelFileDescriptor(context, uri)
                        if (inPfd == null || destination == null) {
                            callback?.onWriteFailed("Cannot access document for printing")
                            return
                        }
                        val inStream = FileInputStream(inPfd.fileDescriptor)
                        val outStream = FileOutputStream(destination.fileDescriptor)
                        val buffer = ByteArray(16384)
                        var read: Int
                        while (inStream.read(buffer).also { read = it } >= 0) {
                            if (cancellationSignal?.isCanceled == true) {
                                callback?.onWriteCancelled()
                                inStream.close()
                                outStream.close()
                                return
                            }
                            outStream.write(buffer, 0, read)
                        }
                        inStream.close()
                        outStream.close()
                        callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback?.onWriteFailed(e.message)
                    } finally {
                        try { inPfd?.close() } catch (_: Exception) {}
                    }
                }
            }, null)
        } else {
            Toast.makeText(context, "প্রিন্ট সেবা ডিভাইসে উপলব্ধ নয়", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "প্রিন্ট করতে সমস্যা হয়েছে: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

internal fun printPdfDocument(context: Context, item: PdfFileItem) {
    printPdfDocument(context, item.uri, item.name)
}

internal fun sharePdfItem(context: Context, item: PdfFileItem) {
    try {
        val shareUri = if (item.path.isNotEmpty()) {
            val f = File(item.path)
            if (f.exists()) {
                try {
                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", f)
                } catch (_: Exception) {
                    item.uri
                }
            } else {
                item.uri
            }
        } else {
            item.uri
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, shareUri)
            putExtra(Intent.EXTRA_SUBJECT, item.name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "পিডিএফ ফাইল শেয়ার করুন"))
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "শেয়ার করতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show()
    }
}

private fun removeFromPreferences(context: Context, prefKey: String, item: PdfFileItem) {
    try {
        val prefs = context.getSharedPreferences("pdf_reader_prefs", Context.MODE_PRIVATE)
        val jsonString = prefs.getString(prefKey, "[]") ?: "[]"
        val arr = JSONArray(jsonString)
        val targetKey = if (item.path.isNotBlank()) item.path else item.uri.toString()
        val newArr = JSONArray()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val key = if (obj.optString("path", "").isNotBlank()) obj.optString("path") else obj.optString("uri")
            if (key != targetKey) {
                newArr.put(obj)
            }
        }
        prefs.edit().putString(prefKey, newArr.toString()).apply()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun updateNameInPreferences(context: Context, item: PdfFileItem, newName: String, newPath: String) {
    listOf("favorite_pdfs", "recent_opened_pdfs").forEach { prefKey ->
        try {
            val prefs = context.getSharedPreferences("pdf_reader_prefs", Context.MODE_PRIVATE)
            val jsonString = prefs.getString(prefKey, "[]") ?: "[]"
            val arr = JSONArray(jsonString)
            val targetKey = if (item.path.isNotBlank()) item.path else item.uri.toString()
            val newArr = JSONArray()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val key = if (obj.optString("path", "").isNotBlank()) obj.optString("path") else obj.optString("uri")
                if (key == targetKey) {
                    obj.put("name", newName)
                    if (newPath.isNotBlank()) {
                        obj.put("path", newPath)
                    }
                }
                newArr.put(obj)
            }
            prefs.edit().putString(prefKey, newArr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

internal fun removePdfFromStorageAndTracking(context: Context, item: PdfFileItem): Boolean {
    var deleted = false
    try {
        if (item.path.isNotBlank()) {
            val f = File(item.path)
            if (f.exists()) {
                deleted = f.delete()
            }
        }
        if (!deleted) {
            try {
                val rows = context.contentResolver.delete(item.uri, null, null)
                if (rows > 0) deleted = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    removeFromPreferences(context, "favorite_pdfs", item)
    removeFromPreferences(context, "recent_opened_pdfs", item)
    return deleted || true
}

internal fun renamePdfFileItem(context: Context, item: PdfFileItem, newNameWithoutExt: String): Boolean {
    val cleanName = if (newNameWithoutExt.endsWith(".pdf", ignoreCase = true)) newNameWithoutExt else "$newNameWithoutExt.pdf"
    try {
        if (item.path.isNotBlank()) {
            val currentFile = File(item.path)
            if (currentFile.exists()) {
                val newFile = File(currentFile.parentFile, cleanName)
                if (currentFile.renameTo(newFile)) {
                    updateNameInPreferences(context, item, cleanName, newFile.absolutePath)
                    return true
                }
            }
        }
        val values = android.content.ContentValues().apply {
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, cleanName)
        }
        val rows = context.contentResolver.update(item.uri, values, null, null)
        if (rows > 0) {
            updateNameInPreferences(context, item, cleanName, "")
            return true
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return false
}

internal fun getPdfUniqueKey(uri: Uri, path: String, fileName: String): String {
    val key = if (path.isNotBlank()) path else "${uri}_$fileName"
    return (key.hashCode().toLong() and 0xFFFFFFFFL).toString(16)
}

internal fun saveLastReadPdfPage(context: Context, key: String, pageIndex: Int) {
    if (key.isBlank() || pageIndex < 0) return
    try {
        val prefs = context.getSharedPreferences("pdf_reader_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("last_page_$key", pageIndex).apply()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

internal fun getLastReadPdfPage(context: Context, key: String): Int {
    if (key.isBlank()) return 0
    return try {
        val prefs = context.getSharedPreferences("pdf_reader_prefs", Context.MODE_PRIVATE)
        prefs.getInt("last_page_$key", 0)
    } catch (e: Exception) {
        0
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 KB"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format(Locale.US, "%.1f GB", gb)
        mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
        else -> String.format(Locale.US, "%.0f KB", kb)
    }
}

private fun formatFormattedDateTime(ms: Long, isBn: Boolean): String {
    if (ms <= 0) return ""
    val date = Date(ms)
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val formatted = sdf.format(date)
    return if (isBn) {
        formatted
            .replace("AM", "AM")
            .replace("PM", "PM")
            .replace("Jan", "জানু")
            .replace("Feb", "ফেব্রু")
            .replace("Mar", "মার্চ")
            .replace("Apr", "এপ্রিল")
            .replace("May", "মে")
            .replace("Jun", "জুন")
            .replace("Jul", "জুলাই")
            .replace("Aug", "আগস্ট")
            .replace("Sep", "সেপ্টে")
            .replace("Oct", "অক্টো")
            .replace("Nov", "নভে")
            .replace("Dec", "ডিসে")
    } else {
        formatted
    }
}

private fun sharePdfFromUri(context: Context, uri: Uri, fileName: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, fileName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "পিডিএফ ফাইল শেয়ার করুন"))
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "শেয়ার করতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show()
    }
}

private fun sharePdfFile(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, file.name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "পিডিএফ শেয়ার করুন"))
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "শেয়ার করতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show()
    }
}

private fun savePdfToDownloads(context: Context, file: File) {
    try {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val destFile = File(downloadsDir, file.name)
        file.copyTo(destFile, overwrite = true)
        Toast.makeText(context, "ডাউনলোড ফোল্ডারে সেভ হয়েছে: ${destFile.name}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "সেভ হয়েছে cache ফোল্ডারে", Toast.LENGTH_SHORT).show()
    }
}

// ================= GENERATORS & HISTORY =================
private fun generatePdfDocument(
    context: Context,
    title: String,
    subtitle: String,
    body: String,
    author: String,
    imageUri: Uri?,
    pageSizeStr: String,
    colorHex: Int,
    includePageNumbers: Boolean
): File? {
    return try {
        val pdfDoc = PdfDocument()
        val pageWidth = 595 // A4 standard width
        val pageHeight = 842 // A4 standard height
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        canvas.drawColor(AndroidColor.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Accent Banner
        paint.color = colorHex
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 36f, paint)

        // Title
        paint.color = colorHex
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText(title, 36f, 75f, paint)

        var currentY = 100f

        // Subtitle
        if (subtitle.isNotBlank()) {
            paint.color = AndroidColor.DKGRAY
            paint.textSize = 13f
            paint.isFakeBoldText = false
            canvas.drawText(subtitle, 36f, currentY, paint)
            currentY += 24f
        }

        // Author
        if (author.isNotBlank()) {
            paint.color = AndroidColor.GRAY
            paint.textSize = 11f
            canvas.drawText("লেখক: $author", 36f, currentY, paint)
            currentY += 24f
        }

        // Divider
        paint.color = AndroidColor.LTGRAY
        paint.strokeWidth = 1f
        canvas.drawLine(36f, currentY, pageWidth - 36f, currentY, paint)
        currentY += 20f

        // Optional Cover Image
        if (imageUri != null) {
            try {
                context.contentResolver.openInputStream(imageUri)?.use { stream ->
                    val origBitmap = BitmapFactory.decodeStream(stream)
                    if (origBitmap != null) {
                        val maxImgW = (pageWidth - 72).toFloat()
                        val maxImgH = 200f
                        val scale = min(maxImgW / origBitmap.width, maxImgH / origBitmap.height)
                        val scaledW = origBitmap.width * scale
                        val scaledH = origBitmap.height * scale
                        val destRect = android.graphics.RectF(36f, currentY, 36f + scaledW, currentY + scaledH)
                        canvas.drawBitmap(origBitmap, null, destRect, null)
                        currentY += scaledH + 20f
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Body Content
        paint.color = AndroidColor.BLACK
        paint.textSize = 12f
        paint.isFakeBoldText = false
        val textPaint = TextPaint(paint)
        val layout = StaticLayout.Builder.obtain(
            body.ifBlank { "কোনো বিষদ তথ্য দেওয়া হয়নি।" },
            0,
            body.ifBlank { "কোনো বিষদ তথ্য দেওয়া হয়নি।" }.length,
            textPaint,
            pageWidth - 72
        ).build()

        canvas.save()
        canvas.translate(36f, currentY)
        layout.draw(canvas)
        canvas.restore()

        // Page Footer
        if (includePageNumbers) {
            paint.color = AndroidColor.GRAY
            paint.textSize = 10f
            canvas.drawText("Page 1 of 1 • ToolsMate Smart Calculator", 36f, pageHeight - 25f, paint)
        }

        pdfDoc.finishPage(page)

        val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputFile = File(outputDir, "Note_$timeStamp.pdf")
        val fos = FileOutputStream(outputFile)
        pdfDoc.writeTo(fos)
        fos.close()
        pdfDoc.close()
        savePdfToHistory(context, outputFile)
        outputFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun generatePdfFromImages(
    context: Context,
    images: List<PdfImageItem>,
    colorHex: Int,
    pageSizeStr: String
): File? {
    if (images.isEmpty()) return null
    return try {
        val pdfDoc = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        val titlePaint = TextPaint().apply {
            color = colorHex
            textSize = 15f
            isFakeBoldText = true
            isAntiAlias = true
        }

        images.forEachIndexed { index, item ->
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
            val page = pdfDoc.startPage(pageInfo)
            val canvas = page.canvas

            canvas.drawColor(AndroidColor.WHITE)

            var imageTopY = 35f

            if (item.title.isNotBlank()) {
                canvas.drawText(item.title, 36f, 35f, titlePaint)
                imageTopY = 55f
            }

            try {
                context.contentResolver.openInputStream(item.uri)?.use { stream ->
                    val origBmp = BitmapFactory.decodeStream(stream)
                    if (origBmp != null) {
                        val maxW = (pageWidth - 72).toFloat()
                        val maxH = (pageHeight - imageTopY - 50).toFloat()
                        val scale = min(maxW / origBmp.width, maxH / origBmp.height)
                        val scaledW = origBmp.width * scale
                        val scaledH = origBmp.height * scale
                        val left = 36f + (maxW - scaledW) / 2f
                        val top = imageTopY + (maxH - scaledH) / 2f
                        val destRect = android.graphics.RectF(left, top, left + scaledW, top + scaledH)
                        canvas.drawBitmap(origBmp, null, destRect, null)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Footer
            val footerPaint = Paint().apply {
                color = AndroidColor.GRAY
                textSize = 10f
                isAntiAlias = true
            }
            canvas.drawText("পৃষ্ঠা ${index + 1} / ${images.size}", (pageWidth - 100).toFloat(), (pageHeight - 25).toFloat(), footerPaint)

            pdfDoc.finishPage(page)
        }

        val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputFile = File(outputDir, "PhotoDoc_$timeStamp.pdf")
        val fos = FileOutputStream(outputFile)
        pdfDoc.writeTo(fos)
        fos.close()
        pdfDoc.close()
        savePdfToHistory(context, outputFile)
        outputFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// History Storage via SharedPreferences
private const val PDF_HISTORY_PREF = "pdf_maker_history_prefs"
private const val PDF_HISTORY_KEY = "created_pdf_files"

private fun savePdfToHistory(context: Context, file: File) {
    try {
        val prefs = context.getSharedPreferences(PDF_HISTORY_PREF, Context.MODE_PRIVATE)
        val existingJson = prefs.getString(PDF_HISTORY_KEY, "[]")
        val jsonArray = JSONArray(existingJson)

        val newObj = JSONObject().apply {
            put("name", file.name)
            put("path", file.absolutePath)
            put("size", file.length())
            put("date", file.lastModified())
        }

        val updatedArray = JSONArray()
        updatedArray.put(newObj)
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            if (item.getString("path") != file.absolutePath) {
                updatedArray.put(item)
            }
        }

        prefs.edit().putString(PDF_HISTORY_KEY, updatedArray.toString()).apply()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun getPdfHistory(context: Context): List<PdfFileItem> {
    val list = mutableListOf<PdfFileItem>()
    try {
        val prefs = context.getSharedPreferences(PDF_HISTORY_PREF, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(PDF_HISTORY_KEY, "[]") ?: "[]"
        val jsonArray = JSONArray(jsonString)

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val path = obj.getString("path")
            val file = File(path)
            if (file.exists()) {
                list.add(
                    PdfFileItem(
                        name = obj.optString("name", file.name),
                        uri = Uri.fromFile(file),
                        sizeBytes = file.length(),
                        dateModifiedMs = file.lastModified(),
                        path = path
                    )
                )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

private fun deletePdfFromHistory(context: Context, item: PdfFileItem) {
    try {
        val file = File(item.path)
        if (file.exists()) {
            file.delete()
        }
        val prefs = context.getSharedPreferences(PDF_HISTORY_PREF, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(PDF_HISTORY_KEY, "[]") ?: "[]"
        val jsonArray = JSONArray(jsonString)
        val updatedArray = JSONArray()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            if (obj.getString("path") != item.path) {
                updatedArray.put(obj)
            }
        }
        prefs.edit().putString(PDF_HISTORY_KEY, updatedArray.toString()).apply()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private class PdfObject(val id: Int, val dict: String, val streamBytes: ByteArray?)

private fun parsePdfObjects(bytes: ByteArray): Map<Int, PdfObject> {
    val objects = HashMap<Int, PdfObject>()
    var pos = 0
    val size = bytes.size
    
    while (pos < size - 10) {
        if (bytes[pos] == 'o'.toByte() && bytes[pos+1] == 'b'.toByte() && bytes[pos+2] == 'j'.toByte() && (pos == 0 || isPdfWhitespace(bytes[pos-1]))) {
            var backtrack = pos - 1
            while (backtrack >= 0 && isPdfWhitespace(bytes[backtrack])) backtrack--
            var endOfGen = backtrack
            while (backtrack >= 0 && isPdfDigit(bytes[backtrack])) backtrack--
            var startOfGen = backtrack + 1
            while (backtrack >= 0 && isPdfWhitespace(bytes[backtrack])) backtrack--
            var endOfId = backtrack
            while (backtrack >= 0 && isPdfDigit(bytes[backtrack])) backtrack--
            var startOfId = backtrack + 1
            
            if (startOfId <= endOfId && startOfGen <= endOfGen) {
                val idStr = String(bytes, startOfId, endOfId - startOfId + 1, Charsets.US_ASCII)
                val id = idStr.toIntOrNull()
                if (id != null) {
                    var endObjPos = pos + 3
                    var foundEnd = false
                    while (endObjPos < size - 6) {
                        if (bytes[endObjPos] == 'e'.toByte() &&
                            bytes[endObjPos+1] == 'n'.toByte() &&
                            bytes[endObjPos+2] == 'd'.toByte() &&
                            bytes[endObjPos+3] == 'o'.toByte() &&
                            bytes[endObjPos+4] == 'b'.toByte() &&
                            bytes[endObjPos+5] == 'j'.toByte()) {
                            foundEnd = true
                            break
                        }
                        endObjPos++
                    }
                    if (foundEnd) {
                        val startContent = pos + 3
                        val endContent = endObjPos
                        
                        var dictStart = -1
                        var dictEnd = -1
                        var search = startContent
                        while (search < endContent - 1) {
                            if (bytes[search] == '<'.toByte() && bytes[search+1] == '<'.toByte()) {
                                dictStart = search
                                break
                            }
                            search++
                        }
                        if (dictStart != -1) {
                            var searchEnd = endContent - 2
                            while (searchEnd > dictStart) {
                                if (bytes[searchEnd] == '>'.toByte() && bytes[searchEnd+1] == '>'.toByte()) {
                                    dictEnd = searchEnd + 2
                                    break
                                }
                                searchEnd--
                            }
                        }
                        
                        val dict = if (dictStart != -1 && dictEnd != -1) {
                            String(bytes, dictStart, dictEnd - dictStart, Charsets.UTF_8)
                        } else ""
                        
                        var streamStart = -1
                        var streamEnd = -1
                        var sSearch = startContent
                        while (sSearch < endContent - 6) {
                            if (bytes[sSearch] == 's'.toByte() &&
                                bytes[sSearch+1] == 't'.toByte() &&
                                bytes[sSearch+2] == 'r'.toByte() &&
                                bytes[sSearch+3] == 'e'.toByte() &&
                                bytes[sSearch+4] == 'a'.toByte() &&
                                bytes[sSearch+5] == 'm'.toByte()) {
                                var sPos = sSearch + 6
                                if (bytes[sPos] == '\r'.toByte()) sPos++
                                if (bytes[sPos] == '\n'.toByte()) sPos++
                                streamStart = sPos
                                break
                            }
                            sSearch++
                        }
                        if (streamStart != -1) {
                            var esSearch = endContent - 9
                            while (esSearch > streamStart) {
                                if (bytes[esSearch] == 'e'.toByte() &&
                                    bytes[esSearch+1] == 'n'.toByte() &&
                                    bytes[esSearch+2] == 'd'.toByte() &&
                                    bytes[esSearch+3] == 's'.toByte() &&
                                    bytes[esSearch+4] == 't'.toByte() &&
                                    bytes[esSearch+5] == 'r'.toByte() &&
                                    bytes[esSearch+6] == 'e'.toByte() &&
                                    bytes[esSearch+7] == 'a'.toByte() &&
                                    bytes[esSearch+8] == 'm'.toByte()) {
                                    var ePos = esSearch
                                    if (bytes[ePos - 1] == '\n'.toByte()) ePos--
                                    if (bytes[ePos - 1] == '\r'.toByte()) ePos--
                                    streamEnd = ePos
                                    break
                                }
                                esSearch--
                            }
                        }
                        
                        val streamBytes = if (streamStart != -1 && streamEnd != -1 && streamEnd >= streamStart) {
                            val dest = ByteArray(streamEnd - streamStart)
                            System.arraycopy(bytes, streamStart, dest, 0, dest.size)
                            dest
                        } else null
                        
                        objects[id] = PdfObject(id, dict, streamBytes)
                        pos = endObjPos + 6
                        continue
                    }
                }
            }
        }
        pos++
    }
    return objects
}

private fun isPdfWhitespace(b: Byte): Boolean {
    return b == ' '.toByte() || b == '\r'.toByte() || b == '\n'.toByte() || b == '\t'.toByte()
}

private fun isPdfDigit(b: Byte): Boolean {
    return b >= '0'.toByte() && b <= '9'.toByte()
}

private fun extractContentsIds(dict: String): List<Int> {
    val ids = ArrayList<Int>()
    val contentsIndex = dict.indexOf("/Contents")
    if (contentsIndex != -1) {
        val sub = dict.substring(contentsIndex + 9)
        val bracketOpen = sub.indexOf('[')
        val bracketClose = sub.indexOf(']')
        if (bracketOpen != -1 && bracketClose != -1 && bracketOpen < 15) {
            val listText = sub.substring(bracketOpen + 1, bracketClose)
            val regex = Regex("(\\d+)\\s+\\d+\\s+R")
            regex.findAll(listText).forEach { match ->
                match.groupValues[1].toIntOrNull()?.let { ids.add(it) }
            }
        } else {
            val regex = Regex("\\s*(\\d+)\\s+\\d+\\s+R")
            val match = regex.find(sub)
            if (match != null) {
                match.groupValues[1].toIntOrNull()?.let { ids.add(it) }
            }
        }
    }
    return ids
}

private fun decompressFlateDecode(compressedBytes: ByteArray): ByteArray? {
    val inflater = java.util.zip.Inflater()
    inflater.setInput(compressedBytes)
    val outputStream = java.io.ByteArrayOutputStream(compressedBytes.size * 2)
    val buffer = ByteArray(1024)
    try {
        while (!inflater.finished()) {
            val count = inflater.inflate(buffer)
            if (count == 0) break
            outputStream.write(buffer, 0, count)
        }
        inflater.end()
        return outputStream.toByteArray()
    } catch (e: Exception) {
        inflater.end()
        try {
            val inflaterNoHeader = java.util.zip.Inflater(true)
            inflaterNoHeader.setInput(compressedBytes)
            val outputStream2 = java.io.ByteArrayOutputStream(compressedBytes.size * 2)
            while (!inflaterNoHeader.finished()) {
                val count = inflaterNoHeader.inflate(buffer)
                if (count == 0) break
                outputStream2.write(buffer, 0, count)
            }
            inflaterNoHeader.end()
            return outputStream2.toByteArray()
        } catch (e2: Exception) {
            e2.printStackTrace()
        }
    }
    return null
}

private fun extractTextFromContentStream(decompressedText: String): String {
    val sb = StringBuilder()
    var i = 0
    val len = decompressedText.length
    while (i < len) {
        if (decompressedText[i] == '(') {
            i++
            val pagePiece = StringBuilder()
            while (i < len) {
                val c = decompressedText[i]
                if (c == ')') {
                    if (i > 0 && decompressedText[i-1] == '\\') {
                        pagePiece.append(')')
                        i++
                        continue
                    }
                    break
                } else if (c == '\\') {
                    i++
                    if (i < len) {
                        val nextC = decompressedText[i]
                        if (nextC.isDigit()) {
                            var octalVal = nextC.toString()
                            if (i + 1 < len && decompressedText[i+1].isDigit()) {
                                octalVal += decompressedText[i+1]
                                i++
                                if (i + 1 < len && decompressedText[i+1].isDigit()) {
                                    octalVal += decompressedText[i+1]
                                    i++
                                }
                            }
                            val charVal = octalVal.toIntOrNull(8)?.toChar()
                            if (charVal != null) {
                                pagePiece.append(charVal)
                            }
                        } else {
                            when (nextC) {
                                'n' -> pagePiece.append('\n')
                                'r' -> pagePiece.append('\r')
                                't' -> pagePiece.append('\t')
                                'b' -> pagePiece.append('\b')
                                'f' -> pagePiece.append('\u000C')
                                else -> pagePiece.append(nextC)
                            }
                        }
                    }
                } else {
                    pagePiece.append(c)
                }
                i++
            }
            sb.append(pagePiece.toString()).append(" ")
        }
        i++
    }
    return sb.toString().replace("\\s+".toRegex(), " ").trim()
}

private suspend fun extractPdfTextByPage(context: Context, pdfUri: Uri, pageCount: Int): List<String> = withContext(Dispatchers.IO) {
    val results = ArrayList<String>(pageCount)
    var successWithPdfBox = false

    // Primary High-Accuracy Extractor: PDFBox Android
    try {
        try {
            com.tom_roush.pdfbox.android.PDFBoxResourceLoader.init(context.applicationContext)
        } catch (_: Exception) {}

        val inputStream = context.contentResolver.openInputStream(pdfUri)
        if (inputStream != null) {
            val pdDoc = com.tom_roush.pdfbox.pdmodel.PDDocument.load(inputStream)
            pdDoc.use { doc ->
                val actualPageCount = doc.numberOfPages
                val stripper = com.tom_roush.pdfbox.text.PDFTextStripper()
                for (p in 1..actualPageCount) {
                    stripper.startPage = p
                    stripper.endPage = p
                    val text = stripper.getText(doc)?.trim() ?: ""
                    results.add(text)
                }
                if (results.any { it.isNotBlank() }) {
                    successWithPdfBox = true
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // Secondary / Fallback Extractor: Direct Byte Object Parsing
    if (!successWithPdfBox || results.isEmpty()) {
        results.clear()
        try {
            val inputStream = context.contentResolver.openInputStream(pdfUri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                val objects = parsePdfObjects(bytes)
                val pageObjects = objects.values.filter { it.dict.contains("/Page") && !it.dict.contains("/Pages") }
                
                if (pageObjects.isNotEmpty()) {
                    val sortedPages = pageObjects.sortedBy { it.id }
                    for (p in 0 until pageCount) {
                        val pageObj = sortedPages.getOrNull(p)
                        if (pageObj != null) {
                            val contentIds = extractContentsIds(pageObj.dict)
                            val sbPageText = StringBuilder()
                            for (cid in contentIds) {
                                val streamObj = objects[cid]
                                if (streamObj != null && streamObj.streamBytes != null) {
                                    val decompressed = decompressFlateDecode(streamObj.streamBytes)
                                    if (decompressed != null) {
                                        val textStr = String(decompressed, Charsets.UTF_8)
                                        val pageTxt = extractTextFromContentStream(textStr)
                                        sbPageText.append(pageTxt).append(" ")
                                    } else {
                                        val textStr = String(streamObj.streamBytes, Charsets.ISO_8859_1)
                                        val pageTxt = extractTextFromContentStream(textStr)
                                        sbPageText.append(pageTxt).append(" ")
                                    }
                                }
                            }
                            results.add(sbPageText.toString().trim())
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    while (results.size < pageCount) {
        results.add("")
    }
    results
}
