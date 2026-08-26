package com.example.ui.screens.tools

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun QrBarcodeTool(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    onBackClick: () -> Unit
) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    var selectedTab by remember { mutableStateOf(0) } // 0: Scanner, 1: Creator

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
    ) {
        // Tab Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(themeColors.cardBg, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TabButton(
                text = if (isBn) "স্ক্যানার" else "Scanner",
                isSelected = selectedTab == 0,
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            ) { selectedTab = 0 }
            
            TabButton(
                text = if (isBn) "ক্রিয়েটর" else "Creator",
                isSelected = selectedTab == 1,
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            ) { selectedTab = 1 }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (selectedTab == 0) {
                ScannerTab(viewModel, themeColors, isBn)
            } else {
                CreatorTab(viewModel, themeColors, isBn, onBackClick)
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    themeColors: CalculatorThemeColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) themeColors.buttonEqualBg else Color.Transparent
    val contentColor = if (isSelected) Color.White else themeColors.displayText.copy(alpha = 0.6f)
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun ScannerTab(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember { 
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) 
    }
    var scanResult by remember { mutableStateOf<String?>(null) }
    var isFlashOn by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val image = InputImage.fromFilePath(context, uri)
                    val scanner = BarcodeScanning.getClient()
                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            if (barcodes.isNotEmpty()) {
                                scanResult = barcodes[0].rawValue
                            } else {
                                coroutineScope.launch(Dispatchers.Main) {
                                    Toast.makeText(context, if (isBn) "কোনো কোড পাওয়া যায়নি" else "No code found", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .addOnFailureListener {
                            coroutineScope.launch(Dispatchers.Main) {
                                Toast.makeText(context, if (isBn) "স্ক্যান ব্যর্থ হয়েছে" else "Scan failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    BackHandler(enabled = scanResult != null) {
        scanResult = null
    }

    if (hasCameraPermission) {
        if (scanResult == null) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                            
                            val scanner = BarcodeScanning.getClient()
                            val executor = Executors.newSingleThreadExecutor()
                            
                            imageAnalysis.setAnalyzer(executor) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                    scanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            if (barcodes.isNotEmpty()) {
                                                scanResult = barcodes[0].rawValue
                                            }
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                } else {
                                    imageProxy.close()
                                }
                            }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            try {
                                cameraProvider.unbindAll()
                                camera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalysis
                                )
                                camera?.cameraControl?.enableTorch(isFlashOn)
                            } catch(exc: Exception) {
                                exc.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Scanner Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(250.dp)
                            .border(2.dp, themeColors.buttonEqualBg, RoundedCornerShape(16.dp))
                            .background(Color.Transparent)
                    )
                }

                // Top Controls
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 32.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        onClick = { 
                            isFlashOn = !isFlashOn
                            camera?.cameraControl?.enableTorch(isFlashOn)
                        },
                        modifier = Modifier
                            .background(themeColors.cardBg.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFlashOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                            contentDescription = "Flashlight",
                            tint = themeColors.buttonEqualBg
                        )
                    }

                    IconButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .background(themeColors.cardBg.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery",
                            tint = themeColors.buttonEqualBg
                        )
                    }
                }

                Text(
                    text = if (isBn) "স্ক্যান করতে কোডটি ফ্রেমে রাখুন" else "Place code inside the frame",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 64.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        } else {
            // Result View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isBn) "স্ক্যান সফল হয়েছে!" else "Scan Successful!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = scanResult ?: "",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = themeColors.displayText,
                                unfocusedTextColor = themeColors.displayText
                            ),
                            maxLines = 10
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { scanResult = null },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = if (isBn) "আবার স্ক্যান করুন" else "Scan Again", color = themeColors.buttonEqualBg)
                    }
                    Button(
                        onClick = { 
                            // Copy to clipboard
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Scanned Result", scanResult)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, if (isBn) "কপি করা হয়েছে" else "Copied", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = if (isBn) "কপি করুন" else "Copy", color = Color.White)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (scanResult?.startsWith("http") == true) {
                    Button(
                        onClick = { 
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(scanResult))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = if (isBn) "লিঙ্ক ওপেন করুন" else "Open Link", color = Color.White)
                    }
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(64.dp), tint = themeColors.displayText.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isBn) "ক্যামেরা পারমিশন প্রয়োজন" else "Camera Permission Required",
                    color = themeColors.displayText
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                    Text(if (isBn) "পারমিশন দিন" else "Grant Permission")
                }
            }
        }
    }
}

@Composable
fun CreatorTab(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onBackClick: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    BackHandler(enabled = generatedBitmap != null) {
        generatedBitmap = null
        inputText = ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (generatedBitmap == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = if (isBn) "তথ্য দিন" else "Enter Information",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = themeColors.displayText
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text(if (isBn) "এখানে টেক্সট বা লিঙ্ক লিখুন..." else "Enter text or link here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = themeColors.displayText,
                            unfocusedTextColor = themeColors.displayText,
                            focusedBorderColor = themeColors.buttonEqualBg,
                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                coroutineScope.launch(Dispatchers.IO) {
                                    val writer = MultiFormatWriter()
                                    try {
                                        val matrix: BitMatrix = writer.encode(inputText, BarcodeFormat.QR_CODE, 512, 512)
                                        val width = matrix.width
                                        val height = matrix.height
                                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                        
                                        // Use theme color for the QR code foreground
                                        val themeInt = android.graphics.Color.parseColor("#FF" + Integer.toHexString(themeColors.buttonEqualBg.value.toInt()).substring(2))
                                        
                                        for (x in 0 until width) {
                                            for (y in 0 until height) {
                                                bitmap.setPixel(x, y, if (matrix.get(x, y)) themeInt else android.graphics.Color.WHITE)
                                            }
                                        }
                                        withContext(Dispatchers.Main) {
                                            generatedBitmap = bitmap
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        shape = RoundedCornerShape(12.dp),
                        enabled = inputText.isNotBlank()
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = if (isBn) "কিউআর কোড তৈরি করুন" else "Generate QR Code", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        bitmap = generatedBitmap!!.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier
                            .size(220.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(2.dp, themeColors.displayText.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { 
                                generatedBitmap = null 
                                inputText = ""
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = if (isBn) "নতুন বানান" else "Create New", color = themeColors.buttonEqualBg)
                        }
                        Button(
                            onClick = { 
                                // Basic share intent for bitmap
                                coroutineScope.launch(Dispatchers.IO) {
                                    try {
                                        val cachePath = java.io.File(context.cacheDir, "images")
                                        cachePath.mkdirs()
                                        val stream = java.io.FileOutputStream(cachePath.absolutePath + "/qr_code.png")
                                        generatedBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
                                        stream.close()
                                        
                                        val imagePath = java.io.File(context.cacheDir, "images")
                                        val newFile = java.io.File(imagePath, "qr_code.png")
                                        val contentUri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.provider", newFile)
                                        
                                        if (contentUri != null) {
                                            val shareIntent = android.content.Intent().apply {
                                                action = android.content.Intent.ACTION_SEND
                                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                setDataAndType(contentUri, context.contentResolver.getType(contentUri))
                                                putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
                                            }
                                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share QR Code"))
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = if (isBn) "শেয়ার করুন" else "Share", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
