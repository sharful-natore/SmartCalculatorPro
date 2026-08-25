package com.example.ui.screens.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.os.ParcelFileDescriptor
import org.json.JSONArray
import org.json.JSONObject

// Model for scanned PDF file items
data class PdfFileItem(
    val name: String,
    val uri: Uri,
    val sizeBytes: Long,
    val dateModifiedMs: Long,
    val path: String
)

enum class PdfSortOption(val titleBn: String, val titleEn: String) {
    DATE_DESC("নতুন ফাইল আগে (তারিখ ↓)", "Newest First"),
    DATE_ASC("পুরোনো ফাইল আগে (তারিখ ↑)", "Oldest First"),
    NAME_ASC("নাম (A → Z)", "Name (A-Z)"),
    NAME_DESC("নাম (Z → A)", "Name (Z-A)"),
    SIZE_DESC("আকার বড় থেকে ছোট (Size ↓)", "Size (Large to Small)"),
    SIZE_ASC("আকার ছোট থেকে বড় (Size ↑)", "Size (Small to Large)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderTool(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val context = LocalContext.current
    var pdfUri by remember { mutableStateOf<Uri?>(null) }

    // Fullscreen and Pinch-to-zoom states
    var isFullscreen by remember { mutableStateOf(false) }
    var scale by remember { mutableStateOf(1.0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Intercept back presses to return to the PDF files list instead of exiting the screen
    BackHandler(enabled = pdfUri != null) {
        if (isFullscreen) {
            isFullscreen = false
        } else {
            pdfUri = null
        }
    }

    var hasStoragePermission by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= 33) true
            else androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasStoragePermission = isGranted
    }

    var fileName by remember { mutableStateOf("") }
    var pageCount by remember { mutableStateOf(0) }
    var currentPageIndex by remember { mutableStateOf(0) }
    var zoomScale by remember { mutableStateOf(1.5f) }
    var isNightMode by remember { mutableStateOf(false) }
    var renderedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Automatic Device Files Scanning State
    var pdfFileList by remember { mutableStateOf<List<PdfFileItem>>(emptyList()) }
    var isScanningFiles by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSort by remember { mutableStateOf(PdfSortOption.DATE_DESC) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Manual System File Picker Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pdfUri = uri
            currentPageIndex = 0
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        fileName = cursor.getString(nameIndex) ?: "PDF Document"
                    } else {
                        fileName = "PDF Document"
                    }
                }
            } catch (e: Exception) {
                fileName = "PDF Document"
            }
        }
    }

    // Scan for all device PDF files on launch or when permission is granted
    LaunchedEffect(hasStoragePermission) {
        if (hasStoragePermission) {
            isScanningFiles = true
            pdfFileList = scanDevicePdfFiles(context)
            isScanningFiles = false
        } else {
            isScanningFiles = false
        }
    }

    // Effect to render current PDF page using native PdfRenderer
    LaunchedEffect(pdfUri, currentPageIndex, zoomScale, isNightMode) {
        val currentUri = pdfUri ?: return@LaunchedEffect
        isLoading = true
        try {
            val pfd = try {
                context.contentResolver.openFileDescriptor(currentUri, "r")
            } catch (e: Exception) {
                val path = currentUri.path
                val file = if (!path.isNullOrEmpty()) File(path) else null
                if (file != null && file.exists()) {
                    android.os.ParcelFileDescriptor.open(file, android.os.ParcelFileDescriptor.MODE_READ_ONLY)
                } else {
                    throw e
                }
            }
            if (pfd != null) {
                val renderer = PdfRenderer(pfd)
                pageCount = renderer.pageCount
                if (currentPageIndex in 0 until pageCount) {
                    val page = renderer.openPage(currentPageIndex)
                    val width = (page.width * zoomScale).toInt().coerceAtLeast(100)
                    val height = (page.height * zoomScale).toInt().coerceAtLeast(100)
                    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bmp)
                    canvas.drawColor(if (isNightMode) AndroidColor.BLACK else AndroidColor.WHITE)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    if (isNightMode) {
                        val config = bmp.config ?: Bitmap.Config.ARGB_8888
                        val invertedBmp = Bitmap.createBitmap(bmp.width, bmp.height, config)
                        val invCanvas = Canvas(invertedBmp)
                        val paint = Paint()
                        val matrix = floatArrayOf(
                            -1.0f, 0.0f, 0.0f, 0.0f, 255.0f,
                            0.0f, -1.0f, 0.0f, 0.0f, 255.0f,
                            0.0f, 0.0f, -1.0f, 0.0f, 255.0f,
                            0.0f, 0.0f, 0.0f, 1.0f, 0.0f
                        )
                        paint.colorFilter = android.graphics.ColorMatrixColorFilter(matrix)
                        invCanvas.drawBitmap(bmp, 0f, 0f, paint)
                        renderedBitmap = invertedBmp
                    } else {
                        renderedBitmap = bmp
                    }
                }
                renderer.close()
                pfd.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "PDF ফাইল লোড করতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show()
        }
        isLoading = false
    }

    // Filter and Sort PDF List
    val filteredAndSortedPdfs by remember(pdfFileList, searchQuery, selectedSort) {
        derivedStateOf {
            pdfFileList
                .filter { it.name.contains(searchQuery, ignoreCase = true) || it.path.contains(searchQuery, ignoreCase = true) }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
    ) {
        if (pdfUri == null) {
            // ================= FILE LIST VIEW =================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Search and Sort Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("PDF ফাইল খুঁজুন...", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = themeColors.buttonEqualBg) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = themeColors.displayText.copy(alpha = 0.6f))
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = themeColors.displayText,
                            unfocusedTextColor = themeColors.displayText,
                            focusedBorderColor = themeColors.buttonEqualBg,
                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.4f),
                            focusedLabelColor = themeColors.buttonEqualBg,
                            unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.6f),
                            focusedPlaceholderColor = themeColors.displayText.copy(alpha = 0.5f),
                            unfocusedPlaceholderColor = themeColors.displayText.copy(alpha = 0.5f),
                            focusedContainerColor = themeColors.cardBg,
                            unfocusedContainerColor = themeColors.cardBg
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(themeColors.cardBg)
                                .border(1.dp, themeColors.displayText.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort",
                                tint = themeColors.buttonEqualBg
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            PdfSortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.titleBn,
                                            fontWeight = if (selectedSort == option) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedSort == option) themeColors.buttonEqualBg else themeColors.displayText
                                        )
                                    },
                                    onClick = {
                                        selectedSort = option
                                        showSortMenu = false
                                    },
                                    leadingIcon = {
                                        if (selectedSort == option) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = themeColors.buttonEqualBg)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            isScanningFiles = true
                            pdfFileList = scanDevicePdfFiles(context)
                            isScanningFiles = false
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(themeColors.cardBg)
                            .border(1.dp, themeColors.displayText.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = themeColors.displayText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Bar for Manual Picker & Demo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { filePickerLauncher.launch("application/pdf") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColors.buttonEqualBg,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ব্রাউজ করুন", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val demoFile = createDemoPdf(context)
                            if (demoFile != null) {
                                pdfUri = Uri.fromFile(demoFile)
                                fileName = "Sample_Demo_Document.pdf"
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, themeColors.buttonEqualBg),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = themeColors.buttonEqualBg
                        )
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = themeColors.buttonEqualBg)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ডেমো PDF টেস্ট", fontSize = 13.sp, color = themeColors.buttonEqualBg, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!hasStoragePermission) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("পিডিএফ স্ক্যান পারমিশন", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                                Text("ফোনের মেমরি থেকে সকল পিডিএফ অটো-স্ক্যান করতে পারমিশন দিন।", fontSize = 11.sp, color = themeColors.displayText.copy(alpha = 0.7f))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { storagePermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = themeColors.buttonEqualBg,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("অনুমতি দিন", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }

                // Status Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ফোনের সকল PDF ফাইল (${filteredAndSortedPdfs.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    Text(
                        text = selectedSort.titleBn,
                        fontSize = 11.sp,
                        color = themeColors.displayText.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Files List View
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
                            Text("ফোনের পিডিএফ ফাইলগুলো স্ক্যান করা হচ্ছে...", fontSize = 13.sp, color = themeColors.displayText)
                        }
                    }
                } else if (filteredAndSortedPdfs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = themeColors.displayText.copy(alpha = 0.3f),
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "কোনো মিল পাওয়া যায়নি" else "ফোনে কোনো PDF ফাইল পাওয়া যায়নি",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "উপরের 'ব্রাউজ করুন' বাটন চেপে সিস্টেম ফাইল ম্যানেজার থেকে সহজেই পিডিএফ ওপেন করতে পারেন।",
                                    fontSize = 12.sp,
                                    color = themeColors.displayText.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(filteredAndSortedPdfs) { item ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        pdfUri = item.uri
                                        fileName = item.name
                                        currentPageIndex = 0
                                    }
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
                                        Text(
                                            text = item.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.displayText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = formatFileSize(item.sizeBytes),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = themeColors.buttonEqualBg
                                            )
                                            if (item.dateModifiedMs > 0) {
                                                Text(
                                                    text = " • " + formatDate(item.dateModifiedMs),
                                                    fontSize = 11.sp,
                                                    color = themeColors.displayText.copy(alpha = 0.5f)
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
        } else {
            // ================= PDF READER VIEWER =================
            // Reset scale and offset on page index changes
            LaunchedEffect(currentPageIndex) {
                scale = 1.0f
                offset = Offset.Zero
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Top Reader Action Sub-Bar - hidden in fullscreen
                if (!isFullscreen) {
                    Surface(
                        color = themeColors.cardBg,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { pdfUri = null }
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("তালিকা", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            Text(
                                text = fileName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = themeColors.displayText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = { isFullscreen = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen Mode",
                                    tint = themeColors.buttonEqualBg
                                )
                            }

                            IconButton(
                                onClick = { filePickerLauncher.launch("application/pdf") }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = "Open another",
                                    tint = themeColors.buttonEqualBg
                                )
                            }

                            IconButton(
                                onClick = { isNightMode = !isNightMode }
                            ) {
                                Icon(
                                    imageVector = if (isNightMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Night Mode",
                                    tint = if (isNightMode) Color(0xFFFBBF24) else themeColors.displayText.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                // Page & Zoom Control Sub-Bar - hidden in fullscreen
                if (!isFullscreen) {
                    Surface(
                        color = themeColors.cardBg.copy(alpha = 0.85f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (currentPageIndex > 0) currentPageIndex-- },
                                    enabled = currentPageIndex > 0
                                ) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Page")
                                }

                                Text(
                                    text = "পৃষ্ঠা ${currentPageIndex + 1} / $pageCount",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText
                                )

                                IconButton(
                                    onClick = { if (currentPageIndex < pageCount - 1) currentPageIndex++ },
                                    enabled = currentPageIndex < pageCount - 1
                                ) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Page")
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (zoomScale > 1.0f) zoomScale -= 0.3f }) {
                                    Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out")
                                }

                                Text(
                                    text = "${(zoomScale * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = themeColors.displayText
                                )

                                IconButton(onClick = { if (zoomScale < 3.0f) zoomScale += 0.3f }) {
                                    Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In")
                                }
                            }
                        }
                    }
                }

                // Page Canvas View with Pinch-to-Zoom and Floating Controls in Fullscreen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (isNightMode) Color(0xFF121212) else Color(0xFFE2E8F0))
                        .padding(if (isFullscreen) 0.dp else 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = themeColors.buttonEqualBg)
                    } else if (renderedBitmap != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        scale = (scale * zoom).coerceIn(1.0f, 5.0f)
                                        if (scale > 1.0f) {
                                            offset = Offset(
                                                x = offset.x + pan.x,
                                                y = offset.y + pan.y
                                            )
                                        } else {
                                            offset = Offset.Zero
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = renderedBitmap!!.asImageBitmap(),
                                contentDescription = "PDF Page ${currentPageIndex + 1}",
                                modifier = Modifier
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offset.x,
                                        translationY = offset.y
                                    )
                                    .fillMaxWidth()
                            )
                        }
                    }

                    // Floating Controls and Close button in Fullscreen mode
                    if (isFullscreen) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Floating Page navigation inside fullscreen
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.Black.copy(alpha = 0.6f),
                                    contentColor = Color.White
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        IconButton(
                                            onClick = { if (currentPageIndex > 0) currentPageIndex-- },
                                            enabled = currentPageIndex > 0,
                                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                                        ) {
                                            Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Page")
                                        }
                                        Text(
                                            text = "${currentPageIndex + 1} / $pageCount",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        IconButton(
                                            onClick = { if (currentPageIndex < pageCount - 1) currentPageIndex++ },
                                            enabled = currentPageIndex < pageCount - 1,
                                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                                        ) {
                                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Page")
                                        }
                                    }
                                }

                                FloatingActionButton(
                                    onClick = { isFullscreen = false },
                                    containerColor = themeColors.buttonEqualBg,
                                    contentColor = Color.White,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(Icons.Default.FullscreenExit, contentDescription = "Exit Fullscreen", modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class PdfImageItem(
    val uri: Uri,
    val title: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfMakerTool(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val context = LocalContext.current
    var makerTab by remember { mutableStateOf(0) } // 0 = Text to PDF, 1 = Image to PDF, 2 = History

    // Shared States
    var previewFile by remember { mutableStateOf<File?>(null) }
    var historyList by remember { mutableStateOf<List<PdfFileItem>>(emptyList()) }

    // Text to PDF States
    var docTitle by remember { mutableStateOf("") }
    var docSubtitle by remember { mutableStateOf("") }
    var docBody by remember { mutableStateOf("") }
    var authorName by remember { mutableStateOf("") }
    var pageSize by remember { mutableStateOf("A4 Portrait") }
    var selectedColorHex by remember { mutableStateOf(0xFF047857.toInt()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var includePageNumbers by remember { mutableStateOf(true) }
    var createdPdfFile by remember { mutableStateOf<File?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    // Image to PDF States
    val selectedImages = remember { mutableStateListOf<PdfImageItem>() }
    var isImageGenerating by remember { mutableStateOf(false) }
    var createdImagePdfFile by remember { mutableStateOf<File?>(null) }
    var imagePageSize by remember { mutableStateOf("A4 Portrait") }
    var imageThemeColorHex by remember { mutableStateOf(0xFF047857.toInt()) }

    // Safe Bitmap Preview for selected single image in Tab 1
    var imageBitmapPreview by remember(selectedImageUri) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(selectedImageUri) {
        val uri = selectedImageUri
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val orig = android.graphics.BitmapFactory.decodeStream(stream)
                    if (orig != null) {
                        val maxDim = 500
                        val scale = minOf(maxDim.toFloat() / orig.width, maxDim.toFloat() / orig.height, 1.0f)
                        val w = (orig.width * scale).toInt().coerceAtLeast(1)
                        val h = (orig.height * scale).toInt().coerceAtLeast(1)
                        imageBitmapPreview = Bitmap.createScaledBitmap(orig, w, h, true)
                    } else {
                        imageBitmapPreview = null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                imageBitmapPreview = null
            }
        } else {
            imageBitmapPreview = null
        }
    }

    // Load PDF History reactively
    LaunchedEffect(makerTab, createdPdfFile, createdImagePdfFile) {
        historyList = getPdfHistory(context)
    }

    // Launchers
    val imagePickerLauncher = rememberLauncherForActivityResult(
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
        0xFF047857.toInt() to "মরু সবুজ",
        0xFF1D4ED8.toInt() to "রয়্যাল ব্লু",
        0xFF7C3AED.toInt() to "পার্পল",
        0xFFB45309.toInt() to "গোল্ডেন ব্রাউন",
        0xFF0F172A.toInt() to "ডিপ চারকোল"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
    ) {
        // Tab Row switcher
        TabRow(
            selectedTabIndex = makerTab,
            containerColor = themeColors.cardBg,
            contentColor = themeColors.buttonEqualBg
        ) {
            Tab(
                selected = makerTab == 0,
                onClick = { makerTab = 0 },
                text = { Text("টেক্সট PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = makerTab == 1,
                onClick = { makerTab = 1 },
                text = { Text("ছবি PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = makerTab == 2,
                onClick = { makerTab = 2 },
                text = { Text("হিস্টোরি", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        // Main Contents switcher
        when (makerTab) {
            0 -> {
                // ================= TAB 1: TEXT TO PDF =================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (createdPdfFile != null) {
                        // PDF Generated Success Card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = themeColors.buttonEqualBg.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = themeColors.buttonEqualBg,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "আপনার PDF ফাইল প্রস্তুত!",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.displayText
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { previewFile = createdPdfFile },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = themeColors.buttonEqualBg,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("প্রিভিউ", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = { sharePdfFile(context, createdPdfFile!!) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = themeColors.buttonEqualBg,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("শেয়ার", fontSize = 11.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { savePdfToDownloads(context, createdPdfFile!!) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.5f)),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = themeColors.buttonEqualBg
                                        )
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("সেভ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Document Details Section
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "ডকুমেন্টের শিরোনাম ও তথ্য",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = docTitle,
                                onValueChange = { docTitle = it },
                                label = { Text("ডকুমেন্ট টাইটেল / নাম *") },
                                placeholder = { Text("যেমন: মে মাসের হিসাব বা প্রজেক্ট নোট") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = themeColors.displayText,
                                    unfocusedTextColor = themeColors.displayText,
                                    focusedBorderColor = themeColors.buttonEqualBg,
                                    unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.4f),
                                    focusedLabelColor = themeColors.buttonEqualBg,
                                    unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.6f),
                                    focusedPlaceholderColor = themeColors.displayText.copy(alpha = 0.5f),
                                    unfocusedPlaceholderColor = themeColors.displayText.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = docSubtitle,
                                onValueChange = { docSubtitle = it },
                                label = { Text("সাব-টাইটেল (ঐচ্ছিক)") },
                                placeholder = { Text("যেমন: বিষয়: বিবরণ বা তারিখ") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = themeColors.displayText,
                                    unfocusedTextColor = themeColors.displayText,
                                    focusedBorderColor = themeColors.buttonEqualBg,
                                    unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.4f),
                                    focusedLabelColor = themeColors.buttonEqualBg,
                                    unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.6f),
                                    focusedPlaceholderColor = themeColors.displayText.copy(alpha = 0.5f),
                                    unfocusedPlaceholderColor = themeColors.displayText.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = authorName,
                                onValueChange = { authorName = it },
                                label = { Text("প্রস্তুতকারীর নাম / প্রতিষ্ঠান (ঐচ্ছিক)") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = themeColors.displayText,
                                    unfocusedTextColor = themeColors.displayText,
                                    focusedBorderColor = themeColors.buttonEqualBg,
                                    unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.4f),
                                    focusedLabelColor = themeColors.buttonEqualBg,
                                    unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.6f),
                                    focusedPlaceholderColor = themeColors.displayText.copy(alpha = 0.5f),
                                    unfocusedPlaceholderColor = themeColors.displayText.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = docBody,
                                onValueChange = { docBody = it },
                                label = { Text("মূল বিষয়বস্তু / নোটস") },
                                placeholder = { Text("এখানে আপনার বার্তা, রচনা বা বিষদ তথ্য টাইপ করুন...") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = themeColors.displayText,
                                    unfocusedTextColor = themeColors.displayText,
                                    focusedBorderColor = themeColors.buttonEqualBg,
                                    unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.4f),
                                    focusedLabelColor = themeColors.buttonEqualBg,
                                    unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.6f),
                                    focusedPlaceholderColor = themeColors.displayText.copy(alpha = 0.5f),
                                    unfocusedPlaceholderColor = themeColors.displayText.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Formatting & Customization
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "ডিজাইন ও কালার থিম",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "হেডার কালার নির্বাচন করুন:",
                                fontSize = 13.sp,
                                color = themeColors.displayText.copy(alpha = 0.8f)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                themeColorsList.forEach { (colorHex, _) ->
                                    val isSelected = selectedColorHex == colorHex
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(Color(colorHex))
                                            .border(
                                                width = if (isSelected) 3.dp else 0.dp,
                                                color = if (isSelected) themeColors.displayText else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable { selectedColorHex = colorHex },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Safe Image Attachment Selection
                            Text(
                                text = "ছবি যুক্ত করুন (ঐচ্ছিক):",
                                fontSize = 13.sp,
                                color = themeColors.displayText.copy(alpha = 0.8f)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        try {
                                            imagePickerLauncher.launch("image/*")
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            Toast.makeText(context, "গ্যালারি ওপেন করা সম্ভব হয়নি", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = themeColors.buttonEqualBg
                                    )
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (selectedImageUri != null) "ছবি পরিবর্তন করুন" else "গ্যালারি থেকে ছবি নিন")
                                }

                                if (selectedImageUri != null) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = {
                                        selectedImageUri = null
                                        imageBitmapPreview = null
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                                    }
                                }
                            }

                            // Safe Image Preview Container
                            if (imageBitmapPreview != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(1.dp, themeColors.displayText.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                        .background(Color.Black.copy(alpha = 0.05f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        bitmap = imageBitmapPreview!!.asImageBitmap(),
                                        contentDescription = "Selected Photo Preview",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (docTitle.isBlank()) {
                                Toast.makeText(context, "দয়া করে অন্তত একটি টাইটেল লিখুন", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isGenerating = true
                            val pdfFile = generatePdfDocument(
                                context = context,
                                title = docTitle,
                                subtitle = docSubtitle,
                                body = docBody,
                                author = authorName,
                                imageUri = selectedImageUri,
                                pageSizeStr = pageSize,
                                colorHex = selectedColorHex,
                                includePageNumbers = includePageNumbers
                            )
                            isGenerating = false
                            if (pdfFile != null) {
                                createdPdfFile = pdfFile
                                Toast.makeText(context, "পিডিএফ সফলভাবে তৈরি হয়েছে!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "পিডিএফ তৈরি করতে ব্যর্থ হয়েছে", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColors.buttonEqualBg,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("পিডিএফ তৈরি সম্পন্ন করুন", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            1 -> {
                // ================= TAB 2: IMAGE TO PDF =================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    if (createdImagePdfFile != null) {
                        // Image PDF Generated Success Banner
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = themeColors.buttonEqualBg.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = themeColors.buttonEqualBg)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ছবি দিয়ে পিডিএফ তৈরি সম্পন্ন!", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { previewFile = createdImagePdfFile },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = themeColors.buttonEqualBg,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("প্রিভিউ", fontSize = 11.sp)
                                    }
                                    Button(
                                        onClick = { sharePdfFile(context, createdImagePdfFile!!) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = themeColors.buttonEqualBg,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("শেয়ার", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (selectedImages.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        tint = themeColors.buttonEqualBg,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "কোনো ছবি নির্বাচন করা হয়নি",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.displayText
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "গ্যালারি থেকে একাধিক ছবি নির্বাচন করে সেগুলোতে ইচ্ছামত টাইটেল বা বর্ণনা যোগ করে সুন্দর পিডিএফ তৈরি করুন।",
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        color = themeColors.displayText.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Button(
                                        onClick = {
                                            try {
                                                multiImagePickerLauncher.launch("image/*")
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "গ্যালারি ওপেন করা সম্ভব হয়নি", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = themeColors.buttonEqualBg,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("গ্যালারি ওপেন করুন", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        // Options Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "নির্বাচিত ছবি সমূহ (${selectedImages.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )

                            TextButton(
                                onClick = {
                                    try {
                                        multiImagePickerLauncher.launch("image/*")
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "গ্যালারি ওপেন করা সম্ভব হয়নি", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("আরো যোগ করুন", fontSize = 12.sp)
                            }
                        }

                        // Selected Images List with descriptions and actions
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(selectedImages) { index, item ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Image thumbnail
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.Black.copy(alpha = 0.05f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val thumbnail = remember(item.uri) {
                                                try {
                                                    context.contentResolver.openInputStream(item.uri)?.use { stream ->
                                                        val orig = android.graphics.BitmapFactory.decodeStream(stream)
                                                        if (orig != null) {
                                                            Bitmap.createScaledBitmap(orig, 120, 120, true)
                                                        } else null
                                                    }
                                                } catch (e: Exception) {
                                                    null
                                                }
                                            }
                                            if (thumbnail != null) {
                                                Image(
                                                    bitmap = thumbnail.asImageBitmap(),
                                                    contentDescription = "Thumb",
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Icon(Icons.Default.Image, contentDescription = null, tint = themeColors.displayText.copy(alpha = 0.5f))
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // Title input
                                        Column(modifier = Modifier.weight(1f)) {
                                            OutlinedTextField(
                                                value = item.title,
                                                onValueChange = { newText ->
                                                    selectedImages[index] = item.copy(title = newText)
                                                },
                                                label = { Text("ছবির টাইটেল / বর্ণনা (ঐচ্ছিক)", fontSize = 10.sp) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = themeColors.displayText,
                                                    unfocusedTextColor = themeColors.displayText,
                                                    focusedBorderColor = themeColors.buttonEqualBg,
                                                    unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.4f),
                                                    focusedLabelColor = themeColors.buttonEqualBg,
                                                    unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.6f)
                                                ),
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        // Reorder / Delete Actions
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    if (index > 0) {
                                                        val temp = selectedImages[index]
                                                        selectedImages[index] = selectedImages[index - 1]
                                                        selectedImages[index - 1] = temp
                                                    }
                                                },
                                                enabled = index > 0,
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.ArrowUpward, contentDescription = "Up", modifier = Modifier.size(16.dp))
                                            }

                                            IconButton(
                                                onClick = {
                                                    if (index < selectedImages.size - 1) {
                                                        val temp = selectedImages[index]
                                                        selectedImages[index] = selectedImages[index + 1]
                                                        selectedImages[index + 1] = temp
                                                    }
                                                },
                                                enabled = index < selectedImages.size - 1,
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.ArrowDownward, contentDescription = "Down", modifier = Modifier.size(16.dp))
                                            }

                                            IconButton(
                                                onClick = { selectedImages.removeAt(index) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Generate Button
                        Button(
                            onClick = {
                                isImageGenerating = true
                                val pdfFile = generatePdfFromImages(
                                    context = context,
                                    images = selectedImages,
                                    colorHex = imageThemeColorHex,
                                    pageSizeStr = imagePageSize
                                )
                                isImageGenerating = false
                                if (pdfFile != null) {
                                    createdImagePdfFile = pdfFile
                                    Toast.makeText(context, "ছবি দিয়ে পিডিএফ সফলভাবে তৈরি হয়েছে!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "পিডিএফ তৈরি করতে ব্যর্থ হয়েছে", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = themeColors.buttonEqualBg,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            if (isImageGenerating) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ছবি দিয়ে PDF ফাইল তৈরি করুন", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            2 -> {
                // ================= TAB 3: HISTORY =================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "আপনার তৈরি করা পিডিএফ ফাইল সমূহ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (historyList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.HistoryToggleOff,
                                    contentDescription = null,
                                    tint = themeColors.displayText.copy(alpha = 0.3f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "কোনো পিডিএফ ফাইল তৈরির রেকর্ড নেই",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText.copy(alpha = 0.6f)
                                )
                                Text(
                                    "টেক্সট বা ছবি পিডিএফ ট্যাব থেকে তৈরি করুন",
                                    fontSize = 11.sp,
                                    color = themeColors.displayText.copy(alpha = 0.4f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(historyList) { item ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            tint = Color.Red,
                                            modifier = Modifier.size(32.dp)
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = themeColors.displayText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = formatFileSize(item.sizeBytes),
                                                    fontSize = 10.sp,
                                                    color = themeColors.buttonEqualBg
                                                )
                                                Text(
                                                    text = " • " + formatDate(item.dateModifiedMs),
                                                    fontSize = 10.sp,
                                                    color = themeColors.displayText.copy(alpha = 0.5f)
                                                )
                                            }
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = { previewFile = File(item.path) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Visibility, contentDescription = "Preview", tint = themeColors.buttonEqualBg, modifier = Modifier.size(18.dp))
                                            }
                                            IconButton(
                                                onClick = { sharePdfFile(context, File(item.path)) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Share, contentDescription = "Share", tint = themeColors.buttonEqualBg, modifier = Modifier.size(18.dp))
                                            }
                                            IconButton(
                                                onClick = {
                                                    deletePdfFromHistory(context, item)
                                                    // Trigger reactive history list reload
                                                    historyList = getPdfHistory(context)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
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

    // PDF Preview Dialog Overlay
    if (previewFile != null) {
        PdfPreviewDialog(
            file = previewFile!!,
            onDismiss = { previewFile = null },
            themeColors = themeColors
        )
    }
}

// Automatic Device PDF Scanner Helper
private fun scanDevicePdfFiles(context: Context): List<PdfFileItem> {
    val pdfList = mutableListOf<PdfFileItem>()
    val seenPaths = mutableSetOf<String>()

    // 1. Query MediaStore
    try {
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
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
            val sizeCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
            val dateCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)
            val dataCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)

            while (cursor.moveToNext()) {
                val id = if (idCol != -1) cursor.getLong(idCol) else 0L
                val name = if (nameCol != -1) cursor.getString(nameCol) ?: "PDF File" else "PDF File"
                val size = if (sizeCol != -1) cursor.getLong(sizeCol) else 0L
                val dateSec = if (dateCol != -1) cursor.getLong(dateCol) else 0L
                val path = if (dataCol != -1) cursor.getString(dataCol) ?: "" else ""

                val uri = if (id != 0L) {
                    android.content.ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
                } else if (path.isNotEmpty()) {
                    Uri.fromFile(File(path))
                } else null

                if (uri != null && (path.isEmpty() || !seenPaths.contains(path))) {
                    if (path.isNotEmpty()) seenPaths.add(path)
                    pdfList.add(
                        PdfFileItem(
                            name = name,
                            uri = uri,
                            sizeBytes = size,
                            dateModifiedMs = dateSec * 1000L,
                            path = path
                        )
                    )
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // 2. Scan public directories as fallback
    try {
        val dirsToScan = listOfNotNull(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            context.getExternalFilesDir(null),
            context.cacheDir
        )

        for (dir in dirsToScan) {
            if (dir.exists() && dir.isDirectory) {
                dir.walkTopDown().maxDepth(3).filter { it.isFile && it.extension.equals("pdf", ignoreCase = true) }.forEach { file ->
                    if (!seenPaths.contains(file.absolutePath)) {
                        seenPaths.add(file.absolutePath)
                        pdfList.add(
                            PdfFileItem(
                                name = file.name,
                                uri = Uri.fromFile(file),
                                sizeBytes = file.length(),
                                dateModifiedMs = file.lastModified(),
                                path = file.absolutePath
                            )
                        )
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return pdfList
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, 3)
    return String.format(Locale.US, "%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

private fun formatDate(ms: Long): String {
    if (ms <= 0) return ""
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(ms))
}

// Helper: Creates a sample PDF Document natively for testing
private fun createDemoPdf(context: Context): File? {
    return generatePdfDocument(
        context = context,
        title = "স্যাম্পল ডেমো পিডিএফ নথি",
        subtitle = "স্মার্ট অল-ইন-ওয়ান ক্যালকুলেটর অ্যান্ড টুলসমেট",
        body = "এটি একটি পরীক্ষামূলক পিডিএফ ডকুমেন্ট যা অ্যান্ড্রয়েড সিস্টেমের নিজস্ব অ্যান্ড্রয়েড গ্রাফিক্স পিডিএফ ইঞ্জিনের মাধ্যমে সম্পূর্ণ লাইটওয়েট অ্যাপ সাইজে তৈরি করা হয়েছে।\n\nআপনি পিডিএফ মেকার টুলের মাধ্যমে সহজেই যেকোনো টেক্সট, রচনা বা ছবি যুক্ত করে নিজস্ব প্রফেশনাল ফাইল বানিয়ে ডাউনলোড ও শেয়ার করতে পারবেন।",
        author = "ToolsMate App",
        imageUri = null,
        pageSizeStr = "A4 Portrait",
        colorHex = 0xFF047857.toInt(),
        includePageNumbers = true
    )
}

// Helper: Native PDF Document Generator using android.graphics.pdf.PdfDocument
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
        val pageWidth = 595 // Standard A4 width (points)
        val pageHeight = 842 // Standard A4 height (points)
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        canvas.drawColor(AndroidColor.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Header Accent Banner
        paint.color = colorHex
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 40f, paint)

        // Title
        paint.color = colorHex
        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText(title.ifBlank { "Untitled Document" }, 36f, 85f, paint)

        // Subtitle & Author
        var currentY = 110f
        if (subtitle.isNotBlank() || author.isNotBlank()) {
            paint.color = AndroidColor.DKGRAY
            paint.textSize = 12f
            paint.isFakeBoldText = false
            val subText = listOfNotNull(
                if (subtitle.isNotBlank()) subtitle else null,
                if (author.isNotBlank()) "প্রস্তুতকারক: $author" else null
            ).joinToString(" | ")
            canvas.drawText(subText, 36f, currentY, paint)
            currentY += 20f
        }

        // Horizontal Line
        paint.color = colorHex
        paint.strokeWidth = 1.5f
        canvas.drawLine(36f, currentY, pageWidth - 36f, currentY, paint)
        currentY += 24f

        // Embedded Image (if selected)
        if (imageUri != null) {
            try {
                context.contentResolver.openInputStream(imageUri)?.use { stream ->
                    val origBmp = android.graphics.BitmapFactory.decodeStream(stream)
                    if (origBmp != null) {
                        val maxW = pageWidth - 72f
                        val maxH = 200f
                        val scale = minOf(maxW / origBmp.width, maxH / origBmp.height)
                        val scaledW = (origBmp.width * scale).toInt().coerceAtLeast(10)
                        val scaledH = (origBmp.height * scale).toInt().coerceAtLeast(10)
                        val scaledBmp = Bitmap.createScaledBitmap(origBmp, scaledW, scaledH, true)
                        canvas.drawBitmap(scaledBmp, 36f, currentY, null)
                        currentY += scaledH + 20f
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Body Content Rendering with line wrapping
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
            canvas.drawText("Page 1 of 1 • Created with ToolsMate", 36f, pageHeight - 25f, paint)
        }

        pdfDoc.finishPage(page)

        val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputFile = File(outputDir, "Text_PDF_$timeStamp.pdf")
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

private fun sharePdfFile(context: Context, file: File) {
    try {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "পিডিএফ ফাইল শেয়ার করুন"))
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

// PDF Helper: Generate PDF from a list of PdfImageItems
private fun generatePdfFromImages(
    context: Context,
    images: List<PdfImageItem>,
    colorHex: Int,
    pageSizeStr: String
): File? {
    if (images.isEmpty()) return null
    return try {
        val pdfDoc = PdfDocument()
        val pageWidth = 595 // Standard A4 width (points)
        val pageHeight = 842 // Standard A4 height (points)

        val titlePaint = TextPaint().apply {
            color = colorHex
            textSize = 16f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val borderPaint = Paint().apply {
            color = colorHex
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }

        images.forEachIndexed { index, item ->
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
            val page = pdfDoc.startPage(pageInfo)
            val canvas = page.canvas

            // Page Background
            canvas.drawColor(AndroidColor.WHITE)

            // Draw border
            canvas.drawRect(20f, 20f, (pageWidth - 20).toFloat(), (pageHeight - 20).toFloat(), borderPaint)

            var imageTopY = 35f

            // Render Title if present
            if (item.title.isNotBlank()) {
                val layout = StaticLayout.Builder.obtain(
                    item.title,
                    0,
                    item.title.length,
                    titlePaint,
                    pageWidth - 80
                ).setAlignment(android.text.Layout.Alignment.ALIGN_CENTER).build()

                canvas.save()
                canvas.translate(40f, 35f)
                layout.draw(canvas)
                canvas.restore()

                imageTopY += layout.height + 15f
            }

            // Load and fit the image
            try {
                context.contentResolver.openInputStream(item.uri)?.use { stream ->
                    val originalBitmap = android.graphics.BitmapFactory.decodeStream(stream)
                    if (originalBitmap != null) {
                        val maxW = (pageWidth - 80).toFloat()
                        val maxH = (pageHeight - imageTopY - 60).toFloat()

                        val scale = minOf(maxW / originalBitmap.width, maxH / originalBitmap.height)
                        val scaledW = (originalBitmap.width * scale).toInt().coerceAtLeast(1)
                        val scaledH = (originalBitmap.height * scale).toInt().coerceAtLeast(1)

                        val scaledBmp = Bitmap.createScaledBitmap(originalBitmap, scaledW, scaledH, true)

                        val left = (pageWidth - scaledW) / 2f
                        val top = imageTopY + (maxH - scaledH) / 2f

                        canvas.drawBitmap(scaledBmp, left, top, null)
                        scaledBmp.recycle()
                        originalBitmap.recycle()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Page Footer
            val pageNumPaint = Paint().apply {
                color = AndroidColor.GRAY
                textSize = 10f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(
                "পৃষ্ঠা ${index + 1}",
                (pageWidth / 2).toFloat(),
                (pageHeight - 40).toFloat(),
                pageNumPaint
            )

            pdfDoc.finishPage(page)
        }

        val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputFile = File(outputDir, "Images_PDF_$timeStamp.pdf")
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

// PDF Helper: Get PDF history persisted in SharedPreferences with JSONObject/JSONArray
private fun getPdfHistory(context: Context): List<PdfFileItem> {
    val prefs = context.getSharedPreferences("pdf_maker_history_prefs", Context.MODE_PRIVATE)
    val jsonStr = prefs.getString("history_list", null) ?: return emptyList()
    val list = mutableListOf<PdfFileItem>()
    try {
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val path = obj.getString("path")
            val file = File(path)
            if (file.exists() && file.isFile) {
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
    return list.sortedByDescending { it.dateModifiedMs }
}

// PDF Helper: Save generated PDF item into history
private fun savePdfToHistory(context: Context, file: File) {
    try {
        val history = getPdfHistory(context).toMutableList()
        if (history.none { it.path == file.absolutePath }) {
            history.add(
                PdfFileItem(
                    name = file.name,
                    uri = Uri.fromFile(file),
                    sizeBytes = file.length(),
                    dateModifiedMs = file.lastModified(),
                    path = file.absolutePath
                )
            )
        }
        saveHistoryList(context, history)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// PDF Helper: Delete PDF item from history list and also delete the underlying file
private fun deletePdfFromHistory(context: Context, item: PdfFileItem) {
    try {
        val file = File(item.path)
        if (file.exists()) {
            file.delete()
        }
        val history = getPdfHistory(context).filter { it.path != item.path }
        saveHistoryList(context, history)
        Toast.makeText(context, "ফাইলটি ডিলিট করা হয়েছে", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// PDF Helper: Internal helper to save updated list back to preferences
private fun saveHistoryList(context: Context, list: List<PdfFileItem>) {
    try {
        val prefs = context.getSharedPreferences("pdf_maker_history_prefs", Context.MODE_PRIVATE)
        val array = JSONArray()
        list.forEach { item ->
            val obj = JSONObject()
            obj.put("path", item.path)
            obj.put("name", item.name)
            array.put(obj)
        }
        prefs.edit().putString("history_list", array.toString()).apply()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// PDF Component: Preview Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPreviewDialog(
    file: File,
    onDismiss: () -> Unit,
    themeColors: CalculatorThemeColors
) {
    val context = LocalContext.current
    var pageCount by remember { mutableStateOf(0) }
    var currentPageIndex by remember { mutableStateOf(0) }
    var currentPageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(file, currentPageIndex) {
        try {
            val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fileDescriptor)
            pageCount = renderer.pageCount
            if (currentPageIndex in 0 until pageCount) {
                val page = renderer.openPage(currentPageIndex)
                val width = page.width * 2
                val height = page.height * 2
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                currentPageBitmap = bitmap
                page.close()
            }
            renderer.close()
            fileDescriptor.close()
        } catch (e: Exception) {
            e.printStackTrace()
            currentPageBitmap = null
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.background),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .border(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "পিডিএফ প্রিভিউ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        Text(
                            text = file.name,
                            fontSize = 11.sp,
                            color = themeColors.displayText.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 180.dp)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = themeColors.displayText)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentPageBitmap != null) {
                        Image(
                            bitmap = currentPageBitmap!!.asImageBitmap(),
                            contentDescription = "PDF Page Preview",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = themeColors.buttonEqualBg)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("লোডিং...", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (pageCount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (currentPageIndex > 0) currentPageIndex-- },
                            enabled = currentPageIndex > 0
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Previous Page",
                                tint = if (currentPageIndex > 0) themeColors.displayText else themeColors.displayText.copy(alpha = 0.3f)
                            )
                        }

                        Text(
                            text = "পৃষ্ঠা ${currentPageIndex + 1} / $pageCount",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )

                        IconButton(
                            onClick = { if (currentPageIndex < pageCount - 1) currentPageIndex++ },
                            enabled = currentPageIndex < pageCount - 1
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Next Page",
                                tint = if (currentPageIndex < pageCount - 1) themeColors.displayText else themeColors.displayText.copy(alpha = 0.3f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = themeColors.buttonEqualBg
                        )
                    ) {
                        Text("বন্ধ করুন", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            sharePdfFile(context, file)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColors.buttonEqualBg,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("শেয়ার")
                    }
                }
            }
        }
    }
}
