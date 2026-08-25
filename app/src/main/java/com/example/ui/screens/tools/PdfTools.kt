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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
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

enum class PdfSortOption(val titleBn: String, val titleEn: String) {
    DATE_DESC("নতুন ফাইল আগে (তারিখ ↓)", "Newest First"),
    DATE_ASC("পুরোনো ফাইল আগে (তারিখ ↑)", "Oldest First"),
    NAME_ASC("নাম (A → Z)", "Name (A-Z)"),
    NAME_DESC("নাম (Z → A)", "Name (Z-A)"),
    SIZE_DESC("আকার বড় থেকে ছোট (Size ↓)", "Size (Large to Small)"),
    SIZE_ASC("আকার ছোট থেকে বড় (Size ↑)", "Size (Small to Large)")
}

// ================= 1. PDF READER TOOL (GOOGLE DRIVE STYLE) =================
@OptIn(ExperimentalMaterial3Api::class)
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

    // Fullscreen and Immersive UI visibility states
    var isFullscreen by remember { mutableStateOf(false) }
    var isControlsVisible by remember { mutableStateOf(true) }

    // Google Drive Pinch-to-zoom & Double-tap states
    var scale by remember { mutableFloatStateOf(1.0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var rotationDegrees by remember { mutableIntStateOf(0) }
    var isNightMode by remember { mutableStateOf(false) }

    // Page Navigation states
    var pageCount by remember { mutableIntStateOf(0) }
    var currentPageIndex by remember { mutableIntStateOf(0) }
    var renderedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingPage by remember { mutableStateOf(false) }

    // Modals & Dialogs
    var showJumpDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }

    // Files List View States
    var pdfFileList by remember { mutableStateOf<List<PdfFileItem>>(emptyList()) }
    var isScanningFiles by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSort by remember { mutableStateOf(PdfSortOption.DATE_DESC) }
    var showSortMenu by remember { mutableStateOf(false) }

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

    // Intercept Back Press: If reading a document, return to the PDF list; if already in list, go back to tools menu
    BackHandler {
        if (showJumpDialog) {
            showJumpDialog = false
        } else if (showDetailsDialog) {
            showDetailsDialog = false
        } else if (isFullscreen) {
            isFullscreen = false
            isControlsVisible = true
        } else if (pdfUri != null) {
            pdfUri = null
            scale = 1.0f
            offset = Offset.Zero
            rotationDegrees = 0
            renderedBitmap = null
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
            isScanningFiles = true
        }
    }

    // File Picker Launcher for manual browsing
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pdfUri = uri
            currentPageIndex = 0
            scale = 1.0f
            offset = Offset.Zero
            rotationDegrees = 0
            extractPdfMetadata(context, uri) { name, size, date, path ->
                fileName = name
                fileSizeBytes = size
                fileLastModifiedMs = date
                filePath = path
            }
        }
    }

    // Scan for Device PDFs asynchronously
    LaunchedEffect(hasStoragePermission, isScanningFiles) {
        if (hasStoragePermission) {
            isScanningFiles = true
            withContext(Dispatchers.IO) {
                val scanned = scanDevicePdfFiles(context)
                withContext(Dispatchers.Main) {
                    pdfFileList = scanned
                    isScanningFiles = false
                }
            }
        } else {
            isScanningFiles = false
        }
    }

    // High Quality Page Rendering Effect
    val density = LocalDensity.current.density
    val screenWidth = LocalConfiguration.current.screenWidthDp
    LaunchedEffect(pdfUri, currentPageIndex, isNightMode, rotationDegrees) {
        val currentUri = pdfUri ?: return@LaunchedEffect
        isLoadingPage = true
        withContext(Dispatchers.IO) {
            try {
                val pfd = openPdfParcelFileDescriptor(context, currentUri)
                if (pfd != null) {
                    val renderer = PdfRenderer(pfd)
                    pageCount = renderer.pageCount
                    val safePageIndex = currentPageIndex.coerceIn(0, max(0, pageCount - 1))
                    if (safePageIndex in 0 until pageCount) {
                        val page = renderer.openPage(safePageIndex)

                        // Render at crisp high DPI for Google Drive razor-sharp clarity
                        val renderFactor = (density * 1.5f).coerceIn(2.0f, 3.5f)
                        val targetWidth = (page.width * renderFactor).toInt().coerceIn(400, 3000)
                        val targetHeight = (page.height * renderFactor).toInt().coerceIn(400, 4200)

                        val rawBmp = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(rawBmp)
                        canvas.drawColor(AndroidColor.WHITE)
                        page.render(rawBmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()

                        // Apply Rotation if any
                        val rotatedBmp = if (rotationDegrees % 360 != 0) {
                            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                            val rot = Bitmap.createBitmap(rawBmp, 0, 0, rawBmp.width, rawBmp.height, matrix, true)
                            if (rot != rawBmp) rawBmp.recycle()
                            rot
                        } else {
                            rawBmp
                        }

                        // Apply Night Mode Color Inversion if enabled
                        val finalBmp = if (isNightMode) {
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

                        withContext(Dispatchers.Main) {
                            renderedBitmap = finalBmp
                            isLoadingPage = false
                        }
                    }
                    renderer.close()
                    pfd.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    isLoadingPage = false
                    Toast.makeText(context, if (isBn) "PDF লোড করতে সমস্যা হয়েছে" else "Failed to load PDF", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Filter and Sort PDF List
    val filteredAndSortedPdfs by remember(pdfFileList, searchQuery, selectedSort) {
        derivedStateOf {
            val query = searchQuery.trim().lowercase()
            pdfFileList
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
                                    text = if (isBn) "পিডিএফ রিডার" else "PDF Reader",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText
                                )
                                Text(
                                    text = if (isBn) "${pdfFileList.size}টি PDF নথি পাওয়া গেছে" else "${pdfFileList.size} PDF files found",
                                    fontSize = 11.sp,
                                    color = themeColors.buttonEqualBg,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Refresh Scan button
                            IconButton(
                                onClick = {
                                    isScanningFiles = true
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = themeColors.buttonEqualBg,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Open System File Picker
                            IconButton(
                                onClick = { filePickerLauncher.launch("application/pdf") },
                                modifier = Modifier.size(38.dp)
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
                                focusedBorderColor = themeColors.buttonEqualBg,
                                unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.25f),
                                focusedContainerColor = themeColors.cardBg,
                                unfocusedContainerColor = themeColors.cardBg
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
                                    color = themeColors.buttonEqualBg.copy(alpha = 0.1f),
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            tint = themeColors.buttonEqualBg,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty())
                                        (if (isBn) "কোনো মিল পাওয়া যায়নি" else "No matching PDF found")
                                    else
                                        (if (isBn) "ফোনে কোনো PDF ফাইল পাওয়া যায়নি" else "No PDF files found"),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isBn)
                                        "উপরের ব্রাউজ বাটন চেপে সিস্টেম ফাইল ম্যানেজার থেকে যেকোনো PDF ওপেন করুন।"
                                    else
                                        "Tap the Browse button above to open any PDF from system storage.",
                                    fontSize = 12.sp,
                                    color = themeColors.displayText.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
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
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            pdfUri = item.uri
                                            fileName = item.name
                                            fileSizeBytes = item.sizeBytes
                                            fileLastModifiedMs = item.dateModifiedMs
                                            filePath = item.path
                                            currentPageIndex = 0
                                            scale = 1.0f
                                            offset = Offset.Zero
                                            rotationDegrees = 0
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFEF4444).copy(alpha = 0.14f),
                                            modifier = Modifier.size(46.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.PictureAsPdf,
                                                    contentDescription = "PDF",
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(26.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = themeColors.displayText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
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
                                            }
                                        }

                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = "Open",
                                            tint = themeColors.displayText.copy(alpha = 0.4f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ================= 2. GOOGLE DRIVE STYLE PDF VIEWER =================
            // Reset gesture states on page changes
            LaunchedEffect(currentPageIndex) {
                scale = 1.0f
                offset = Offset.Zero
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isNightMode) Color(0xFF0B0F19) else Color(0xFF1E293B))
            ) {
                val viewportWidth = constraints.maxWidth.toFloat()
                val viewportHeight = constraints.maxHeight.toFloat()

                // Main Page Canvas with Pinch-To-Zoom, Double-Tap Zoom & Clamped Pan Bounds
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(viewportWidth, viewportHeight) {
                            detectTapGestures(
                                onDoubleTap = { tapOffset ->
                                    if (scale > 1.1f) {
                                        scale = 1.0f
                                        offset = Offset.Zero
                                    } else {
                                        scale = 2.5f
                                        // Center the zoom on double tap point
                                        val targetOffsetX = (viewportWidth / 2f - tapOffset.x) * 1.5f
                                        val targetOffsetY = (viewportHeight / 2f - tapOffset.y) * 1.5f
                                        offset = clampOffset(
                                            Offset(targetOffsetX, targetOffsetY),
                                            scale = 2.5f,
                                            viewportWidth = viewportWidth,
                                            viewportHeight = viewportHeight
                                        )
                                    }
                                },
                                onTap = {
                                    // Single tap toggles immersive controls
                                    isControlsVisible = !isControlsVisible
                                }
                            )
                        }
                        .pointerInput(viewportWidth, viewportHeight) {
                            detectTransformGestures { centroid, pan, zoom, _ ->
                                val newScale = (scale * zoom).coerceIn(1.0f, 5.0f)
                                scale = newScale
                                if (newScale > 1.0f) {
                                    val newOffset = Offset(offset.x + pan.x, offset.y + pan.y)
                                    offset = clampOffset(
                                        newOffset,
                                        scale = newScale,
                                        viewportWidth = viewportWidth,
                                        viewportHeight = viewportHeight
                                    )
                                } else {
                                    offset = Offset.Zero
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoadingPage) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = themeColors.buttonEqualBg)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (isBn) "পৃষ্ঠা লোড হচ্ছে..." else "Loading Page...",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    } else if (renderedBitmap != null) {
                        Image(
                            bitmap = renderedBitmap!!.asImageBitmap(),
                            contentDescription = "PDF Page ${currentPageIndex + 1}",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y
                                ),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // TOP ACTION TOOLBAR (Google Drive Style) - Animated Visibility
                AnimatedVisibility(
                    visible = isControlsVisible && !isFullscreen,
                    enter = slideInVertically { -it } + fadeIn(),
                    exit = slideOutVertically { -it } + fadeOut(),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    Surface(
                        color = themeColors.cardBg.copy(alpha = 0.95f),
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    pdfUri = null
                                    renderedBitmap = null
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to list",
                                    tint = themeColors.displayText
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = fileName.ifBlank { "PDF Document" },
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${currentPageIndex + 1} / $pageCount • ${formatFileSize(fileSizeBytes)}",
                                    fontSize = 11.sp,
                                    color = themeColors.buttonEqualBg,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Fit to Screen / Reset Zoom
                            if (scale > 1.05f) {
                                IconButton(
                                    onClick = {
                                        scale = 1.0f
                                        offset = Offset.Zero
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ZoomOutMap,
                                        contentDescription = "Fit to Screen",
                                        tint = themeColors.buttonEqualBg
                                    )
                                }
                            }

                            // Night Mode (Invert Colors) Toggle
                            IconButton(
                                onClick = { isNightMode = !isNightMode }
                            ) {
                                Icon(
                                    imageVector = if (isNightMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Night Mode",
                                    tint = if (isNightMode) Color(0xFFFBBF24) else themeColors.displayText
                                )
                            }

                            // Rotate 90 Degrees Clockwise
                            IconButton(
                                onClick = {
                                    rotationDegrees = (rotationDegrees + 90) % 360
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RotateRight,
                                    contentDescription = "Rotate",
                                    tint = themeColors.displayText
                                )
                            }

                            // Document Info / Details Dialog
                            IconButton(
                                onClick = { showDetailsDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Details",
                                    tint = themeColors.displayText
                                )
                            }

                            // Share PDF
                            IconButton(
                                onClick = {
                                    sharePdfFromUri(context, pdfUri!!, fileName)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = themeColors.buttonEqualBg
                                )
                            }

                            // Fullscreen Toggle
                            IconButton(
                                onClick = { isFullscreen = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen",
                                    tint = themeColors.displayText
                                )
                            }
                        }
                    }
                }

                // BOTTOM GOOGLE DRIVE FLOATING PAGE PILL & SCRUBBER
                AnimatedVisibility(
                    visible = isControlsVisible && pageCount > 0,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (isFullscreen) 24.dp else 16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = Color.Black.copy(alpha = 0.85f),
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            // Previous Page Button
                            IconButton(
                                onClick = {
                                    if (currentPageIndex > 0) currentPageIndex--
                                },
                                enabled = currentPageIndex > 0,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Previous Page",
                                    tint = if (currentPageIndex > 0) Color.White else Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Page Counter Pill (Tap opens "Jump to Page" Dialog)
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.18f),
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .clickable { showJumpDialog = true }
                            ) {
                                Text(
                                    text = if (isBn)
                                        "পৃষ্ঠা ${currentPageIndex + 1} / $pageCount"
                                    else
                                        "Page ${currentPageIndex + 1} of $pageCount",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }

                            // Next Page Button
                            IconButton(
                                onClick = {
                                    if (currentPageIndex < pageCount - 1) currentPageIndex++
                                },
                                enabled = currentPageIndex < pageCount - 1,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Next Page",
                                    tint = if (currentPageIndex < pageCount - 1) Color.White else Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Exit Fullscreen Icon if in Fullscreen
                            if (isFullscreen) {
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { isFullscreen = false },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FullscreenExit,
                                        contentDescription = "Exit Fullscreen",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
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
        var targetPageText by remember { mutableStateOf("${currentPageIndex + 1}") }
        var sliderValue by remember { mutableFloatStateOf((currentPageIndex + 1).toFloat()) }

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
                        if (num != null && num in 1..pageCount) {
                            currentPageIndex = num - 1
                        } else {
                            currentPageIndex = sliderValue.toInt() - 1
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
                                focusedBorderColor = themeColors.buttonEqualBg,
                                focusedTextColor = themeColors.displayText,
                                unfocusedTextColor = themeColors.displayText
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
                                focusedBorderColor = themeColors.buttonEqualBg,
                                focusedTextColor = themeColors.displayText,
                                unfocusedTextColor = themeColors.displayText
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
                                focusedBorderColor = themeColors.buttonEqualBg,
                                focusedTextColor = themeColors.displayText,
                                unfocusedTextColor = themeColors.displayText
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
                                focusedBorderColor = themeColors.buttonEqualBg,
                                focusedTextColor = themeColors.displayText,
                                unfocusedTextColor = themeColors.displayText
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

                                        OutlinedTextField(
                                            value = imgItem.title,
                                            onValueChange = { newTitle ->
                                                selectedImages[idx] = imgItem.copy(title = newTitle)
                                            },
                                            placeholder = { Text(if (isBn) "ছবির শিরোনাম (ঐচ্ছিক)" else "Image Title (Optional)", fontSize = 12.sp) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            singleLine = true
                                        )

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
                                    Text(if (isBn) "ছবি দিয়ে PDF তৈরি করুন" else "Convert Images to PDF", fontWeight = FontWeight.Bold)
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
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        } else {
            null
        }
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
