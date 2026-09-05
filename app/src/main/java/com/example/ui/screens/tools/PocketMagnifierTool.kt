package com.example.ui.screens.tools

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.clickable
import androidx.core.content.ContextCompat
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import java.util.concurrent.Executors

private val CalculatorThemeColors.accent: Color get() = this.buttonEqualBg
private val CalculatorThemeColors.onAccent: Color get() = this.buttonEqualText
private val CalculatorThemeColors.onSurface: Color get() = this.displayText
private val CalculatorThemeColors.surface: Color get() = this.cardBg
private val CalculatorThemeColors.surfaceVariant: Color get() = this.chipBg

enum class MagnifierFilterMode(val titleBn: String, val titleEn: String) {
    NORMAL("স্বাভাবিক", "Normal"),
    INVERT("ইনভার্ট (কন্ট্রাস্ট)", "Invert"),
    MONO("সাদা-কালো", "Mono B&W"),
    SEPIA("চোখের আরাম", "Warm Eye-Care")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PocketMagnifierTool(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isBn = viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI

    // Camera Permission State
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            Toast.makeText(context, if (isBn) "ক্যামেরা ব্যবহারের অনুমতি প্রয়োজন" else "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    // CameraX Controls & States
    var camera by remember { mutableStateOf<Camera?>(null) }
    var previewView: PreviewView? by remember { mutableStateOf(null) }
    var zoomRatio by remember { mutableFloatStateOf(1.0f) }
    var maxZoomRatio by remember { mutableFloatStateOf(8.0f) }
    var isTorchOn by remember { mutableStateOf(false) }
    var filterMode by remember { mutableStateOf(MagnifierFilterMode.NORMAL) }
    var showReadingGuide by remember { mutableStateOf(false) }
    var readingGuideY by remember { mutableFloatStateOf(0.5f) }

    // Freeze Frame State
    var isFrozen by remember { mutableStateOf(false) }
    var frozenBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var frozenScale by remember { mutableFloatStateOf(1f) }
    var frozenOffset by remember { mutableStateOf(Offset.Zero) }

    // Launch Permission on enter if not granted
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isBn) "পকেট ম্যাগনিফায়ার" else "Pocket Magnifier",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = if (isFrozen) (if (isBn) "স্থির চিত্র মোড • জুম: ${String.format("%.1f", zoomRatio * frozenScale)}x" else "Frozen View • Zoom: ${String.format("%.1f", zoomRatio * frozenScale)}x")
                            else (if (isBn) "লাইভ ভিউ • জুম: ${String.format("%.1f", zoomRatio)}x" else "Live Camera • Zoom: ${String.format("%.1f", zoomRatio)}x"),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Torch Toggle
                    IconButton(
                        onClick = {
                            if (camera?.cameraInfo?.hasFlashUnit() == true) {
                                isTorchOn = !isTorchOn
                                camera?.cameraControl?.enableTorch(isTorchOn)
                            } else {
                                Toast.makeText(context, if (isBn) "ফ্ল্যাশলাইট পাওয়া যায়নি" else "No flashlight unit found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Torch",
                            tint = if (isTorchOn) Color(0xFFFFEB3B) else Color.White
                        )
                    }

                    // Reading Guide Toggle
                    IconButton(onClick = { showReadingGuide = !showReadingGuide }) {
                        Icon(
                            imageVector = if (showReadingGuide) Icons.Default.FormatAlignJustify else Icons.Default.HorizontalRule,
                            contentDescription = "Reading Guide",
                            tint = if (showReadingGuide) themeColors.accent else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black.copy(alpha = 0.85f))
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        if (!hasCameraPermission) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isBn) "ক্যামেরা পারমিশন প্রয়োজন" else "Camera Permission Required",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isBn) "ছোট লেখা ও সূক্ষ্ম বস্তু ম্যাগনিফাই করার জন্য ক্যামেরার অনুমতি দিন।" else "Grant camera access to magnify tiny text and fine objects.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                    ) {
                        Text(if (isBn) "অনুমতি দিন" else "Grant Permission", color = themeColors.onAccent)
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Live Camera View or Frozen Bitmap View
                if (isFrozen && frozenBitmap != null) {
                    // Frozen Frame with interactive Pinch & Pan
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    frozenScale = (frozenScale * zoom).coerceIn(0.8f, 5f)
                                    frozenOffset += pan
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = frozenBitmap!!.asImageBitmap(),
                            contentDescription = "Frozen frame",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = frozenScale,
                                    scaleY = frozenScale,
                                    translationX = frozenOffset.x,
                                    translationY = frozenOffset.y
                                )
                        )
                    }
                } else {
                    // Live CameraX Preview
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                                previewView = this

                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(surfaceProvider)
                                    }

                                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                                    try {
                                        cameraProvider.unbindAll()
                                        val cam = cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            cameraSelector,
                                            preview
                                        )
                                        camera = cam
                                        val zoomState = cam.cameraInfo.zoomState.value
                                        maxZoomRatio = zoomState?.maxZoomRatio?.coerceAtMost(10f) ?: 8.0f
                                    } catch (exc: Exception) {
                                        exc.printStackTrace()
                                    }
                                }, ContextCompat.getMainExecutor(ctx))
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Filter Overlay for Reading (Invert, Mono, Sepia)
                when (filterMode) {
                    MagnifierFilterMode.NORMAL -> { /* Clear */ }
                    MagnifierFilterMode.INVERT -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = 0.08f))
                        )
                    }
                    MagnifierFilterMode.MONO -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF37474F).copy(alpha = 0.15f))
                        )
                    }
                    MagnifierFilterMode.SEPIA -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFFFCC80).copy(alpha = 0.20f))
                        )
                    }
                }

                // Reading Guidelines & Center Target
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Subtle crosshair at center
                    val cx = w / 2
                    val cy = h / 2
                    drawLine(Color.White.copy(alpha = 0.25f), Offset(cx - 20, cy), Offset(cx + 20, cy), strokeWidth = 2f)
                    drawLine(Color.White.copy(alpha = 0.25f), Offset(cx, cy - 20), Offset(cx, cy + 20), strokeWidth = 2f)

                    // Horizontal Line Reading Guide
                    if (showReadingGuide) {
                        val guideY = h * readingGuideY
                        drawLine(
                            color = Color(0xFFFFD54F).copy(alpha = 0.85f),
                            start = Offset(0f, guideY),
                            end = Offset(w, guideY),
                            strokeWidth = 3f
                        )
                        drawLine(
                            color = Color(0xFFFFD54F).copy(alpha = 0.35f),
                            start = Offset(0f, guideY - 24),
                            end = Offset(w, guideY - 24),
                            strokeWidth = 1f
                        )
                        drawLine(
                            color = Color(0xFFFFD54F).copy(alpha = 0.35f),
                            start = Offset(0f, guideY + 24),
                            end = Offset(w, guideY + 24),
                            strokeWidth = 1f
                        )
                    }
                }

                // Top Filters Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 12.dp, end = 12.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MagnifierFilterMode.values().forEach { mode ->
                        val isSelected = filterMode == mode
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) themeColors.accent else Color.Black.copy(alpha = 0.6f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) themeColors.accent else Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .height(32.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { filterMode = mode }
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isBn) mode.titleBn else mode.titleEn,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) themeColors.onAccent else Color.White
                                )
                            }
                        }
                    }
                }

                // Bottom Floating Controls Overlay
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.78f))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Preset Zoom Chips Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(1.0f, 2.0f, 4.0f, 6.0f, 8.0f).forEach { preset ->
                            val isCurrent = (zoomRatio - preset) in -0.2f..0.2f
                            Surface(
                                shape = CircleShape,
                                color = if (isCurrent) themeColors.accent else Color.White.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        zoomRatio = preset
                                        camera?.cameraControl?.setZoomRatio(preset)
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${preset.toInt()}x",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isCurrent) themeColors.onAccent else Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Continuous Zoom Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Slider(
                            value = zoomRatio,
                            onValueChange = { newZoom ->
                                zoomRatio = newZoom
                                camera?.cameraControl?.setZoomRatio(newZoom)
                            },
                            valueRange = 1.0f..maxZoomRatio,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = themeColors.accent,
                                activeTrackColor = themeColors.accent,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Main Action Row: Freeze / Unfreeze Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (isFrozen) {
                                    isFrozen = false
                                    frozenBitmap = null
                                    frozenScale = 1f
                                    frozenOffset = Offset.Zero
                                } else {
                                    // Capture current frame bitmap
                                    val bmp = previewView?.bitmap
                                    if (bmp != null) {
                                        frozenBitmap = bmp
                                        isFrozen = true
                                        Toast.makeText(context, if (isBn) "ছবি স্থির করা হয়েছে • জুম ও টেনে পড়তে পারেন" else "Frame frozen • Pinch & drag to inspect", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, if (isBn) "ক্যামেরা প্রস্তুত হচ্ছে..." else "Camera initializing...", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFrozen) Color(0xFFF44336) else themeColors.accent
                            ),
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(46.dp)
                        ) {
                            Icon(
                                imageVector = if (isFrozen) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = null,
                                tint = if (isFrozen) Color.White else themeColors.onAccent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isFrozen) (if (isBn) "লাইভ ভিউ চালু করুন" else "Resume Live Camera")
                                else (if (isBn) "স্ক্রিন স্থির করুন (Freeze Frame)" else "Freeze Frame / Pause"),
                                color = if (isFrozen) Color.White else themeColors.onAccent,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}
