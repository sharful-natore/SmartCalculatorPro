package com.example.ui.screens.tools

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.util.Base64
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import java.io.ByteArrayOutputStream

// ================= INTERACTIVE CUSTOM IMAGE CROPPER DIALOG =================
@Composable
fun CvImageCropperDialog(
    originalBitmap: Bitmap,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit,
    onCropDone: (String) -> Unit
) {
    var selectedRatio by remember { mutableStateOf("FREE") } // "FREE", "1:1", "3:4", "2:3", "4:3"
    var rotationDegrees by remember { mutableStateOf(0f) }

    // Normalized crop box [0f, 1f]
    var cropLeft by remember { mutableStateOf(0.08f) }
    var cropTop by remember { mutableStateOf(0.08f) }
    var cropRight by remember { mutableStateOf(0.92f) }
    var cropBottom by remember { mutableStateOf(0.92f) }

    fun applyAspect(ratioName: String) {
        selectedRatio = ratioName
        val cx = (cropLeft + cropRight) / 2f
        val cy = (cropTop + cropBottom) / 2f
        when (ratioName) {
            "1:1" -> {
                val half = 0.38f
                cropLeft = (cx - half).coerceAtLeast(0.02f)
                cropRight = (cx + half).coerceAtMost(0.98f)
                cropTop = (cy - half).coerceAtLeast(0.02f)
                cropBottom = (cy + half).coerceAtMost(0.98f)
            }
            "3:4" -> {
                val halfW = 0.32f
                val halfH = 0.42f
                cropLeft = (cx - halfW).coerceAtLeast(0.02f)
                cropRight = (cx + halfW).coerceAtMost(0.98f)
                cropTop = (cy - halfH).coerceAtLeast(0.02f)
                cropBottom = (cy + halfH).coerceAtMost(0.98f)
            }
            "2:3" -> {
                val halfW = 0.28f
                val halfH = 0.42f
                cropLeft = (cx - halfW).coerceAtLeast(0.02f)
                cropRight = (cx + halfW).coerceAtMost(0.98f)
                cropTop = (cy - halfH).coerceAtLeast(0.02f)
                cropBottom = (cy + halfH).coerceAtMost(0.98f)
            }
            "4:3" -> {
                val halfW = 0.42f
                val halfH = 0.32f
                cropLeft = (cx - halfW).coerceAtLeast(0.02f)
                cropRight = (cx + halfW).coerceAtMost(0.98f)
                cropTop = (cy - halfH).coerceAtLeast(0.02f)
                cropBottom = (cy + halfH).coerceAtMost(0.98f)
            }
            "FREE" -> {
                // Keep current box
            }
        }
    }

    fun performCrop() {
        try {
            val matrix = android.graphics.Matrix().apply {
                postRotate(rotationDegrees)
            }
            val rotatedBmp = if (rotationDegrees != 0f) {
                Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
            } else {
                originalBitmap
            }

            val bw = rotatedBmp.width
            val bh = rotatedBmp.height

            val pixelX = (cropLeft * bw).toInt().coerceIn(0, bw - 1)
            val pixelY = (cropTop * bh).toInt().coerceIn(0, bh - 1)
            val pixelW = ((cropRight - cropLeft) * bw).toInt().coerceIn(1, bw - pixelX)
            val pixelH = ((cropBottom - cropTop) * bh).toInt().coerceIn(1, bh - pixelY)

            val croppedBmp = Bitmap.createBitmap(rotatedBmp, pixelX, pixelY, pixelW, pixelH)

            val maxDim = 480
            val scaledBmp = if (croppedBmp.width > maxDim || croppedBmp.height > maxDim) {
                val aspect = croppedBmp.width.toFloat() / croppedBmp.height.toFloat()
                val targetW = if (aspect >= 1f) maxDim else (maxDim * aspect).toInt()
                val targetH = if (aspect >= 1f) (maxDim / aspect).toInt() else maxDim
                Bitmap.createScaledBitmap(croppedBmp, targetW.coerceAtLeast(10), targetH.coerceAtLeast(10), true)
            } else {
                croppedBmp
            }

            val baos = ByteArrayOutputStream()
            scaledBmp.compress(Bitmap.CompressFormat.JPEG, 90, baos)
            val base64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
            onCropDone(base64)
        } catch (_: Exception) {
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Crop,
                        contentDescription = null,
                        tint = themeColors.buttonEqualBg,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "ছবি ক্রপ ও রিসাইজ" else "Crop & Adjust Photo",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                }

                IconButton(
                    onClick = { rotationDegrees = (rotationDegrees + 90f) % 360f },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.RotateRight,
                        contentDescription = "Rotate",
                        tint = themeColors.buttonEqualBg,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                // Aspect Ratio Selector Chips
                Text(
                    text = if (isBn) "অ্যাসপেক্ট রেশিও নির্বাচন করুন:" else "Select Aspect Ratio:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "FREE" to if (isBn) "ইচ্ছামত" else "Freeform",
                        "1:1" to "1:1 Square",
                        "3:4" to "3:4 Portrait",
                        "2:3" to "2:3 Photo",
                        "4:3" to "4:3 Standard"
                    ).forEach { (key, label) ->
                        val isSel = selectedRatio == key
                        Surface(
                            onClick = { applyAspect(key) },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSel) themeColors.buttonEqualBg else themeColors.cardBg,
                            border = BorderStroke(1.dp, if (isSel) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.2f)),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp)) {
                                Text(
                                    text = label,
                                    color = if (isSel) Color.White else themeColors.displayText,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Crop Area
                var activeHandle by remember { mutableStateOf<String?>(null) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Color.Black.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val w = size.width.toFloat()
                                    val h = size.height.toFloat()
                                    val nx = offset.x / w
                                    val ny = offset.y / h
                                    val threshold = 0.12f
                                    activeHandle = when {
                                        kotlin.math.abs(nx - cropLeft) < threshold && kotlin.math.abs(ny - cropTop) < threshold -> "TOP_LEFT"
                                        kotlin.math.abs(nx - cropRight) < threshold && kotlin.math.abs(ny - cropTop) < threshold -> "TOP_RIGHT"
                                        kotlin.math.abs(nx - cropLeft) < threshold && kotlin.math.abs(ny - cropBottom) < threshold -> "BOTTOM_LEFT"
                                        kotlin.math.abs(nx - cropRight) < threshold && kotlin.math.abs(ny - cropBottom) < threshold -> "BOTTOM_RIGHT"
                                        nx in cropLeft..cropRight && ny in cropTop..cropBottom -> "MOVE"
                                        else -> null
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val w = size.width.toFloat()
                                    val h = size.height.toFloat()
                                    val dx = dragAmount.x / w
                                    val dy = dragAmount.y / h

                                    when (activeHandle) {
                                        "MOVE" -> {
                                            val bw = cropRight - cropLeft
                                            val bh = cropBottom - cropTop
                                            val newL = (cropLeft + dx).coerceIn(0f, 1f - bw)
                                            val newT = (cropTop + dy).coerceIn(0f, 1f - bh)
                                            cropLeft = newL
                                            cropRight = newL + bw
                                            cropTop = newT
                                            cropBottom = newT + bh
                                        }
                                        "TOP_LEFT" -> {
                                            cropLeft = (cropLeft + dx).coerceIn(0f, cropRight - 0.15f)
                                            cropTop = (cropTop + dy).coerceIn(0f, cropBottom - 0.15f)
                                        }
                                        "TOP_RIGHT" -> {
                                            cropRight = (cropRight + dx).coerceIn(cropLeft + 0.15f, 1f)
                                            cropTop = (cropTop + dy).coerceIn(0f, cropBottom - 0.15f)
                                        }
                                        "BOTTOM_LEFT" -> {
                                            cropLeft = (cropLeft + dx).coerceIn(0f, cropRight - 0.15f)
                                            cropBottom = (cropBottom + dy).coerceIn(cropTop + 0.15f, 1f)
                                        }
                                        "BOTTOM_RIGHT" -> {
                                            cropRight = (cropRight + dx).coerceIn(cropLeft + 0.15f, 1f)
                                            cropBottom = (cropBottom + dy).coerceIn(cropTop + 0.15f, 1f)
                                        }
                                    }
                                },
                                onDragEnd = { activeHandle = null }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        val bmpAspect = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
                        val canvasAspect = w / h
                        val drawW: Float
                        val drawH: Float
                        if (bmpAspect > canvasAspect) {
                            drawW = w
                            drawH = w / bmpAspect
                        } else {
                            drawH = h
                            drawW = h * bmpAspect
                        }
                        val drawX = (w - drawW) / 2f
                        val drawY = (h - drawH) / 2f

                        val matrix = android.graphics.Matrix().apply {
                            postRotate(rotationDegrees, originalBitmap.width / 2f, originalBitmap.height / 2f)
                        }
                        val rotated = if (rotationDegrees != 0f) {
                            Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
                        } else originalBitmap

                        drawContext.canvas.nativeCanvas.drawBitmap(
                            rotated,
                            null,
                            android.graphics.RectF(drawX, drawY, drawX + drawW, drawY + drawH),
                            Paint(Paint.FILTER_BITMAP_FLAG)
                        )

                        val clPx = cropLeft * w
                        val ctPx = cropTop * h
                        val crPx = cropRight * w
                        val cbPx = cropBottom * h

                        val darkPaint = Paint().apply {
                            color = AndroidColor.argb(160, 0, 0, 0)
                            style = Paint.Style.FILL
                        }
                        drawContext.canvas.nativeCanvas.drawRect(0f, 0f, w, ctPx, darkPaint)
                        drawContext.canvas.nativeCanvas.drawRect(0f, cbPx, w, h, darkPaint)
                        drawContext.canvas.nativeCanvas.drawRect(0f, ctPx, clPx, cbPx, darkPaint)
                        drawContext.canvas.nativeCanvas.drawRect(crPx, ctPx, w, cbPx, darkPaint)

                        val borderPaint = Paint().apply {
                            color = AndroidColor.WHITE
                            style = Paint.Style.STROKE
                            strokeWidth = 2.5f
                            isAntiAlias = true
                        }
                        drawContext.canvas.nativeCanvas.drawRect(clPx, ctPx, crPx, cbPx, borderPaint)

                        val gridPaint = Paint().apply {
                            color = AndroidColor.argb(90, 255, 255, 255)
                            style = Paint.Style.STROKE
                            strokeWidth = 1f
                        }
                        val cropBoxW = crPx - clPx
                        val cropBoxH = cbPx - ctPx
                        drawContext.canvas.nativeCanvas.drawLine(clPx + cropBoxW / 3f, ctPx, clPx + cropBoxW / 3f, cbPx, gridPaint)
                        drawContext.canvas.nativeCanvas.drawLine(clPx + 2f * cropBoxW / 3f, ctPx, clPx + 2f * cropBoxW / 3f, cbPx, gridPaint)
                        drawContext.canvas.nativeCanvas.drawLine(clPx, ctPx + cropBoxH / 3f, crPx, ctPx + cropBoxH / 3f, gridPaint)
                        drawContext.canvas.nativeCanvas.drawLine(clPx, ctPx + 2f * cropBoxH / 3f, crPx, ctPx + 2f * cropBoxH / 3f, gridPaint)

                        val handlePaint = Paint().apply {
                            color = AndroidColor.argb(255, 22, 163, 74)
                            style = Paint.Style.FILL
                            isAntiAlias = true
                        }
                        val handleRad = 10f
                        drawContext.canvas.nativeCanvas.drawCircle(clPx, ctPx, handleRad, handlePaint)
                        drawContext.canvas.nativeCanvas.drawCircle(crPx, ctPx, handleRad, handlePaint)
                        drawContext.canvas.nativeCanvas.drawCircle(clPx, cbPx, handleRad, handlePaint)
                        drawContext.canvas.nativeCanvas.drawCircle(crPx, cbPx, handleRad, handlePaint)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            cropLeft = 0.05f
                            cropTop = 0.05f
                            cropRight = 0.95f
                            cropBottom = 0.95f
                            rotationDegrees = 0f
                            selectedRatio = "FREE"
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBn) "রিসেট" else "Reset", fontSize = 11.sp)
                    }

                    Text(
                        text = if (isBn) "কোণ ধরে টেনে ক্রপ সাইজ নির্ধারণ করুন" else "Drag corner dots to adjust crop size",
                        fontSize = 9.5.sp,
                        color = themeColors.displayText.copy(alpha = 0.6f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { performCrop() },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isBn) "ক্রপ সম্পন্ন করুন" else "Apply Crop", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(if (isBn) "বাতিল" else "Cancel", fontSize = 12.sp)
            }
        }
    )
}

// ================= DELIVERABLE: CLEAN & COMPACT LIVE EDIT PANEL =================
@Composable
fun CvLiveEditPanel(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    onRequestAiPrompt: (title: String, defaultPrompt: String, targetField: String, expIndex: Int) -> Unit,
    onPickImage: () -> Unit,
    onOpenCropExisting: () -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onRefreshPreview: () -> Unit
) {
    var localData by remember(cvData) { mutableStateOf(cvData) }

    fun commitAndRefresh(updated: CvData) {
        onCvDataChange(updated)
        onRefreshPreview()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Live Edit Banner Header
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = themeColors.buttonEqualBg.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = themeColors.buttonEqualBg,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isBn) "কমপ্যাক্ট লাইভ এডিট প্যানেল" else "Compact Live Edit Mode",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        Text(
                            text = if (isBn) "যেকোনো সেকশন এডিট করে 'সেভ ও রিফ্রেশ' চাপুন" else "Edit any section and click Save & Refresh",
                            fontSize = 10.sp,
                            color = themeColors.displayText.copy(alpha = 0.7f)
                        )
                    }
                }

                Button(
                    onClick = { commitAndRefresh(localData) },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBn) "রিফ্রেশ" else "Refresh", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // --- 1. PERSONAL INFO SECTION ---
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                SectionCardHeader(
                    title = if (isBn) "ব্যক্তিগত তথ্য (Personal Info)" else "Personal Information",
                    icon = Icons.Default.Person,
                    themeColors = themeColors
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = localData.fullName,
                            onValueChange = { localData = localData.copy(fullName = it) },
                            label = { Text(if (isBn) "পূর্ণ নাম" else "Full Name", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = localData.jobTitle,
                            onValueChange = { localData = localData.copy(jobTitle = it) },
                            label = { Text(if (isBn) "পদবি (Job Title)" else "Job Title", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = localData.email,
                            onValueChange = { localData = localData.copy(email = it) },
                            label = { Text(if (isBn) "ইমেইল (Email)" else "Email Address", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = localData.phone,
                            onValueChange = { localData = localData.copy(phone = it) },
                            label = { Text(if (isBn) "ফোন নম্বর" else "Phone Number", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = localData.address,
                    onValueChange = { localData = localData.copy(address = it) },
                    label = { Text(if (isBn) "ঠিকানা / লোকেশন" else "Address / Location", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = localData.linkedin,
                            onValueChange = { localData = localData.copy(linkedin = it) },
                            label = { Text(if (isBn) "লিংকডইন" else "LinkedIn URL", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = localData.githubOrPortfolio,
                            onValueChange = { localData = localData.copy(githubOrPortfolio = it) },
                            label = { Text(if (isBn) "পোর্টফোলিও / গিটহাব" else "Portfolio / GitHub", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Photo actions and preview in Live Edit
                var isPhotoAdvancedExpanded by remember { mutableStateOf(false) }

                val decodedLiveBmp = remember(localData.photoBase64) {
                    if (localData.photoBase64.isNotBlank()) {
                        try {
                            val bytes = Base64.decode(localData.photoBase64, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        } catch (_: Exception) { null }
                    } else null
                }

                val previewShape = when (localData.photoShape) {
                    "Circle" -> CircleShape
                    "Rounded" -> RoundedCornerShape(localData.photoCornerRadius.dp)
                    "Rectangle" -> RoundedCornerShape(4.dp)
                    else -> CircleShape
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 60.dp, height = 60.dp)
                            .background(Color.Gray.copy(alpha = 0.08f), shape = previewShape)
                            .then(
                                if (localData.photoBorderWidth > 0f) {
                                    Modifier.border(
                                        width = localData.photoBorderWidth.dp,
                                        color = themeColors.buttonEqualBg,
                                        shape = previewShape
                                    )
                                } else Modifier
                            )
                            .clip(shape = previewShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (decodedLiveBmp != null) {
                            Image(
                                bitmap = decodedLiveBmp.asImageBitmap(),
                                contentDescription = "Profile Photo",
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                tint = themeColors.displayText.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onPickImage,
                            modifier = Modifier
                                .size(34.dp)
                                .background(themeColors.buttonEqualBg.copy(alpha = 0.12f), CircleShape)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = themeColors.buttonEqualBg, modifier = Modifier.size(18.dp))
                        }

                        if (localData.photoBase64.isNotBlank()) {
                            IconButton(
                                onClick = onOpenCropExisting,
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(themeColors.buttonEqualBg.copy(alpha = 0.12f), CircleShape)
                            ) {
                                Icon(Icons.Default.Crop, contentDescription = "Edit Crop", tint = themeColors.buttonEqualBg, modifier = Modifier.size(18.dp))
                            }

                            IconButton(
                                onClick = {
                                    localData = localData.copy(photoBase64 = "", photoScale = 1.0f, photoOffsetX = 0f, photoOffsetY = 0f)
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color.Red.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove Photo", tint = Color.Red.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isPhotoAdvancedExpanded = !isPhotoAdvancedExpanded }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isBn) "এডভান্সড ফটো সেটিংস" else "Advanced Photo Options",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.buttonEqualBg
                    )
                    Icon(
                        imageVector = if (isPhotoAdvancedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Advanced Photo Options",
                        tint = themeColors.buttonEqualBg,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (isPhotoAdvancedExpanded) {
                    Text(
                        text = if (isBn) "ছবির শেইপ:" else "Photo Shape:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText.copy(alpha = 0.7f)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp)
                    ) {
                        val shapes = listOf("Circle", "Rounded", "Square", "Rectangle")
                        shapes.forEach { shapeName ->
                            val isSelected = localData.photoShape == shapeName
                            val label = when (shapeName) {
                                "Circle" -> if (isBn) "বৃত্ত" else "Circle"
                                "Rounded" -> if (isBn) "কোণ গোল" else "Rounded"
                                "Square" -> if (isBn) "বর্গ" else "Square"
                                else -> if (isBn) "আয়তাকার" else "Rectangle"
                            }
                            Surface(
                                onClick = { localData = localData.copy(photoShape = shapeName) },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) themeColors.buttonEqualBg else themeColors.background,
                                border = BorderStroke(1.dp, if (isSelected) Color.Transparent else themeColors.displayText.copy(alpha = 0.15f)),
                                modifier = Modifier.padding(1.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 9.sp,
                                    color = if (isSelected) Color.White else themeColors.displayText,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isBn) "বর্ডার: ${if (localData.photoBorderWidth <= 0.1f) "নেই" else "%.1f pt".format(localData.photoBorderWidth)}" else "Border: ${if (localData.photoBorderWidth <= 0.1f) "None" else "%.1f pt".format(localData.photoBorderWidth)}",
                            fontSize = 10.sp,
                            color = themeColors.displayText.copy(alpha = 0.7f),
                            modifier = Modifier.width(72.dp)
                        )
                        Slider(
                            value = localData.photoBorderWidth,
                            onValueChange = { localData = localData.copy(photoBorderWidth = it) },
                            valueRange = 0.0f..4.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = themeColors.buttonEqualBg,
                                activeTrackColor = themeColors.buttonEqualBg,
                                inactiveTrackColor = themeColors.displayText.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "প্রস্থ: ${localData.photoWidth} pt" else "Width: ${localData.photoWidth} pt",
                                fontSize = 9.5.sp,
                                color = themeColors.displayText.copy(alpha = 0.7f)
                            )
                            Slider(
                                value = localData.photoWidth.toFloat(),
                                onValueChange = { localData = localData.copy(photoWidth = it.toInt()) },
                                valueRange = 55f..120f,
                                colors = SliderDefaults.colors(
                                    thumbColor = themeColors.buttonEqualBg,
                                    activeTrackColor = themeColors.buttonEqualBg,
                                    inactiveTrackColor = themeColors.displayText.copy(alpha = 0.1f)
                                )
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "উচ্চতা: ${localData.photoHeight} pt" else "Height: ${localData.photoHeight} pt",
                                fontSize = 9.5.sp,
                                color = themeColors.displayText.copy(alpha = 0.7f)
                            )
                            Slider(
                                value = localData.photoHeight.toFloat(),
                                onValueChange = { localData = localData.copy(photoHeight = it.toInt()) },
                                valueRange = 55f..140f,
                                colors = SliderDefaults.colors(
                                    thumbColor = themeColors.buttonEqualBg,
                                    activeTrackColor = themeColors.buttonEqualBg,
                                    inactiveTrackColor = themeColors.displayText.copy(alpha = 0.1f)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { commitAndRefresh(localData) },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    modifier = Modifier.fillMaxWidth().height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBn) "ব্যক্তিগত তথ্য সেভ ও রিফ্রেশ" else "Save & Refresh Personal Info", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // --- 2. SUMMARY / OBJECTIVE ---
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionCardHeader(
                        title = if (isBn) "ক্যারিয়ার অবজেক্টিভ / সামারি" else "Professional Summary",
                        icon = Icons.Default.Summarize,
                        themeColors = themeColors, modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = {
                            val defaultPrompt = "Write a high-impact, professional 3-sentence ATS-friendly resume summary for a ${localData.jobTitle.ifBlank { "Professional" }}. Highlight strengths, work ethic, and results."
                            onRequestAiPrompt(
                                if (isBn) "এআই সামারি জেনারেট" else "Generate Summary with AI",
                                defaultPrompt,
                                "SUMMARY",
                                -1
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBn) "এআই সামারি" else "AI Summary", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = localData.summary,
                    onValueChange = { localData = localData.copy(summary = it) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    placeholder = { Text(if (isBn) "৩ বাক্যে প্রফেশনাল সামারি লিখুন..." else "Write a 3-sentence professional summary...", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { commitAndRefresh(localData) },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    modifier = Modifier.fillMaxWidth().height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBn) "সামারি সেভ ও রিফ্রেশ" else "Save & Refresh Summary", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // --- 3. WORK EXPERIENCE LIST ---
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionCardHeader(
                        title = if (isBn) "কাজের অভিজ্ঞতা (${localData.experiences.size})" else "Experience (${localData.experiences.size})",
                        icon = Icons.Default.Work,
                        themeColors = themeColors, modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = {
                            val newList = localData.experiences + CvExperienceItem(
                                company = "",
                                role = "",
                                startDate = "2022",
                                endDate = "Present",
                                isCurrent = true,
                                description = ""
                            )
                            localData = localData.copy(experiences = newList)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBn) "নতুন অভিজ্ঞতা" else "Add Exp", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                localData.experiences.forEachIndexed { idx, exp ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = themeColors.background,
                        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (exp.role.isNotBlank()) "${exp.role} @ ${exp.company}" else (if (isBn) "অভিজ্ঞতা #${idx + 1}" else "Experience #${idx + 1}"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = themeColors.buttonEqualBg
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            val prompt = "Role: '${exp.role}' at '${exp.company}'. Current description: '${exp.description}'. Rewrite into 3 strong action-verb result bullet points."
                                            onRequestAiPrompt(
                                                if (isBn) "অভিজ্ঞতার বুলেট এআই" else "AI Bullet Points",
                                                prompt,
                                                "EXPERIENCE",
                                                idx
                                            )
                                        },
                                        modifier = Modifier.size(28.dp).background(themeColors.buttonEqualBg, CircleShape)
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = Color.White, modifier = Modifier.size(13.dp))
                                    }

                                    IconButton(
                                        onClick = {
                                            val list = localData.experiences.toMutableList()
                                            list.removeAt(idx)
                                            localData = localData.copy(experiences = list)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(15.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = exp.role,
                                        onValueChange = {
                                            val list = localData.experiences.toMutableList()
                                            list[idx] = exp.copy(role = it)
                                            localData = localData.copy(experiences = list)
                                        },
                                        label = { Text(if (isBn) "পদবি (Role)" else "Job Role", fontSize = 10.sp) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = exp.company,
                                        onValueChange = {
                                            val list = localData.experiences.toMutableList()
                                            list[idx] = exp.copy(company = it)
                                            localData = localData.copy(experiences = list)
                                        },
                                        label = { Text(if (isBn) "প্রতিষ্ঠান" else "Company", fontSize = 10.sp) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = exp.startDate,
                                        onValueChange = {
                                            val list = localData.experiences.toMutableList()
                                            list[idx] = exp.copy(startDate = it)
                                            localData = localData.copy(experiences = list)
                                        },
                                        label = { Text(if (isBn) "শুরু" else "Start Date", fontSize = 10.sp) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = exp.endDate,
                                        onValueChange = {
                                            val list = localData.experiences.toMutableList()
                                            list[idx] = exp.copy(endDate = it)
                                            localData = localData.copy(experiences = list)
                                        },
                                        label = { Text(if (isBn) "শেষ" else "End Date", fontSize = 10.sp) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedTextField(
                                value = exp.description,
                                onValueChange = {
                                    val list = localData.experiences.toMutableList()
                                    list[idx] = exp.copy(description = it)
                                    localData = localData.copy(experiences = list)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                maxLines = 6,
                                placeholder = { Text(if (isBn) "দায়িত্ব ও অর্জনসমূহ (বুলেট পয়েন্টে লিখুন)..." else "Responsibilities and achievements (bullet points)...", fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { commitAndRefresh(localData) },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    modifier = Modifier.fillMaxWidth().height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBn) "অভিজ্ঞতা সেভ ও রিফ্রেশ" else "Save & Refresh Experience", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }


        // --- 3.5 FRESHER / ENTRY LEVEL SECTIONS ---
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                SectionCardHeader(
                    title = if (isBn) "ফ্রেশার / এন্ট্রি লেভেল সেকশন" else "Fresher / Entry Level Sections",
                    icon = Icons.Default.School,
                    themeColors = themeColors
                )
                Spacer(modifier = Modifier.height(10.dp))
                
                CvCustomLargeTextField(
                    onAiPrompt = {
                        val prompt = "Write 3 bullet points for a fresh graduate's academic project. Highlight problem-solving, tools used (like Python, React), and outcomes. Make it ATS-friendly."
                        onRequestAiPrompt(
                            if (isBn) "একাডেমিক প্রজেক্ট এআই" else "Academic Projects AI",
                            prompt,
                            "FRESHER_ACADEMIC_PROJECTS",
                            -1
                        )
                    },
                    label = if (isBn) "একাডেমিক প্রজেক্টস / থিসিস" else "Academic Projects / Thesis",
                    value = localData.fresherAcademicProjects,
                    onValueChange = { localData = localData.copy(fresherAcademicProjects = it) },
                    themeColors = themeColors,
                    placeholderText = if (isBn) "ভার্সিটির প্রজেক্ট বা থিসিসের বিস্তারিত লিখুন..." else "Describe your university projects or thesis...",
                    isLiveEdit = true,
                    isBn = isBn
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                CvCustomLargeTextField(
                    onAiPrompt = {
                        val prompt = "Write 2-3 bullet points for extra-curricular activities or volunteer work. Highlight leadership, teamwork, and event management skills. ATS-friendly format."
                        onRequestAiPrompt(
                            if (isBn) "এক্সট্রা-কারিকুলার এআই" else "Extra-Curricular AI",
                            prompt,
                            "FRESHER_EXTRACURRICULAR",
                            -1
                        )
                    },
                    label = if (isBn) "এক্সট্রা-কারিকুলার ও ভলান্টিয়ারিং" else "Extra-Curricular & Volunteering",
                    value = localData.fresherInternshipsVolunteer,
                    onValueChange = { localData = localData.copy(fresherInternshipsVolunteer = it) },
                    themeColors = themeColors,
                    placeholderText = if (isBn) "ভলান্টিয়ারিং বা সহশিক্ষামূলক কাজের বিবরণ..." else "Describe your volunteering or extra-curricular activities...",
                    isLiveEdit = true,
                    isBn = isBn
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                CvCustomLargeTextField(
                    onAiPrompt = {
                        val prompt = "Write 2 bullet points describing a club leadership role (e.g., President, General Secretary) for a fresh graduate resume. Focus on organizing events and managing teams."
                        onRequestAiPrompt(
                            if (isBn) "লিডারশিপ ও ক্লাব এআই" else "Leadership & Clubs AI",
                            prompt,
                            "FRESHER_LEADERSHIP",
                            -1
                        )
                    },
                    label = if (isBn) "লিডারশিপ ও ক্লাব এক্টিভিটিস" else "Leadership & Club Activities",
                    value = localData.fresherLeadershipClubs,
                    onValueChange = { localData = localData.copy(fresherLeadershipClubs = it) },
                    themeColors = themeColors,
                    placeholderText = if (isBn) "ক্লাবে আপনার পদ ও কাজের বিবরণ..." else "Describe your club roles and responsibilities...",
                    isLiveEdit = true,
                    isBn = isBn
                )

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { commitAndRefresh(localData) },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    modifier = Modifier.fillMaxWidth().height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBn) "সেভ ও রিফ্রেশ" else "Save & Refresh", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // --- 4. EDUCATION DETAILS ---

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionCardHeader(
                        title = if (isBn) "শিক্ষাগত যোগ্যতা (${localData.educations.size})" else "Education (${localData.educations.size})",
                        icon = Icons.Default.School,
                        themeColors = themeColors, modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = {
                            val newList = localData.educations + CvEducationItem(
                                degree = "",
                                institution = "",
                                passingYear = "2020",
                                result = "3.80"
                            )
                            localData = localData.copy(educations = newList)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBn) "নতুন ডিগ্রি" else "Add Degree", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                localData.educations.forEachIndexed { idx, edu ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = themeColors.background,
                        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (edu.degree.isNotBlank()) edu.degree else (if (isBn) "ডিগ্রি #${idx + 1}" else "Degree #${idx + 1}"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = themeColors.buttonEqualBg
                                )

                                IconButton(
                                    onClick = {
                                        val list = localData.educations.toMutableList()
                                        list.removeAt(idx)
                                        localData = localData.copy(educations = list)
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(15.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.weight(1.2f)) {
                                    OutlinedTextField(
                                        value = edu.degree,
                                        onValueChange = {
                                            val list = localData.educations.toMutableList()
                                            list[idx] = edu.copy(degree = it)
                                            localData = localData.copy(educations = list)
                                        },
                                        label = { Text(if (isBn) "ডিগ্রি / সার্টিফিকেট" else "Degree Title", fontSize = 10.sp) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = edu.institution,
                                        onValueChange = {
                                            val list = localData.educations.toMutableList()
                                            list[idx] = edu.copy(institution = it)
                                            localData = localData.copy(educations = list)
                                        },
                                        label = { Text(if (isBn) "প্রতিষ্ঠান / ভার্সিটি" else "Institute", fontSize = 10.sp) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = edu.passingYear,
                                        onValueChange = {
                                            val list = localData.educations.toMutableList()
                                            list[idx] = edu.copy(passingYear = it)
                                            localData = localData.copy(educations = list)
                                        },
                                        label = { Text(if (isBn) "পাসের সাল" else "Passing Year", fontSize = 10.sp) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = edu.result,
                                        onValueChange = {
                                            val list = localData.educations.toMutableList()
                                            list[idx] = edu.copy(result = it)
                                            localData = localData.copy(educations = list)
                                        },
                                        label = { Text(if (isBn) "ফলাফল (CGPA)" else "Result / CGPA", fontSize = 10.sp) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { commitAndRefresh(localData) },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    modifier = Modifier.fillMaxWidth().height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBn) "শিক্ষাগত তথ্য সেভ ও রিফ্রেশ" else "Save & Refresh Education", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // --- 5. SKILLS SECTION ---
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionCardHeader(
                        title = if (isBn) "দক্ষতাসমূহ (${localData.skills.size})" else "Skills (${localData.skills.size})",
                        icon = Icons.Default.Psychology,
                        themeColors = themeColors, modifier = Modifier.weight(1f)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = {
                                val prompt = "Target Job: '${localData.jobTitle.ifBlank { "Professional" }}'. Suggest top 8 in-demand industry skills as comma-separated items."
                                onRequestAiPrompt(
                                    if (isBn) "এআই স্কিলস সাজেশন" else "AI Skills Suggestion",
                                    prompt,
                                    "SKILLS",
                                    -1
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(if (isBn) "এআই" else "AI", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val newList = localData.skills + CvSkillItem(
                                    name = "",
                                    level = "Expert",
                                    category = "Functional/Core Skills"
                                )
                                localData = localData.copy(skills = newList)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(if (isBn) "যোগ" else "Add", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                localData.skills.forEachIndexed { idx, sk ->
                    val fullText = if (sk.description.isNotBlank()) "${sk.name}: ${sk.description}" else if (sk.name.contains(":")) sk.name else if (sk.level.isNotBlank() && sk.level != "Proficient" && sk.level != "Expert") "${sk.name}: ${sk.level}" else sk.name
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = fullText,
                            onValueChange = { input ->
                                val list = localData.skills.toMutableList()
                                if (input.contains(":")) {
                                    val parts = input.split(":", limit = 2)
                                    list[idx] = sk.copy(
                                        name = parts[0].trim(),
                                        description = parts[1].trim(),
                                        level = parts[1].trim()
                                    )
                                } else {
                                    list[idx] = sk.copy(
                                        name = input.trim(),
                                        description = "",
                                        level = ""
                                    )
                                }
                                localData = localData.copy(skills = list)
                            },
                            label = { Text(if (isBn) "স্কিল ও বিবরণ #${idx + 1}" else "Skill & Detail #${idx + 1}", fontSize = 10.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                val list = localData.skills.toMutableList()
                                list.removeAt(idx)
                                localData = localData.copy(skills = list)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { commitAndRefresh(localData) },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    modifier = Modifier.fillMaxWidth().height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBn) "দক্ষতা সেভ ও রিফ্রেশ" else "Save & Refresh Skills", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // --- 6. PROJECTS SECTION ---
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionCardHeader(
                        title = if (isBn) "প্রজেক্ট পোর্টফোলিও (${localData.projects.size})" else "Projects (${localData.projects.size})",
                        icon = Icons.Default.Code,
                        themeColors = themeColors, modifier = Modifier.weight(1f)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = {
                                val prompt = "Suggest 2 high-impact technical/business project ideas for a '${localData.jobTitle.ifBlank { "Professional" }}' including title, technologies, and bullet points."
                                onRequestAiPrompt(
                                    if (isBn) "এআই প্রজেক্ট আইডিয়া" else "AI Project Ideas",
                                    prompt,
                                    "PROJECTS",
                                    -1
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(if (isBn) "এআই" else "AI", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val newList = localData.projects + CvProjectItem(
                                    title = "",
                                    description = "",
                                    link = ""
                                )
                                localData = localData.copy(projects = newList)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(if (isBn) "যোগ" else "Add", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                localData.projects.forEachIndexed { idx, proj ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = themeColors.background,
                        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (proj.title.isNotBlank()) proj.title else (if (isBn) "প্রজেক্ট #${idx + 1}" else "Project #${idx + 1}"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = themeColors.buttonEqualBg
                                )

                                IconButton(
                                    onClick = {
                                        val list = localData.projects.toMutableList()
                                        list.removeAt(idx)
                                        localData = localData.copy(projects = list)
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(15.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.weight(1.2f)) {
                                    OutlinedTextField(
                                        value = proj.title,
                                        onValueChange = {
                                            val list = localData.projects.toMutableList()
                                            list[idx] = proj.copy(title = it)
                                            localData = localData.copy(projects = list)
                                        },
                                        label = { Text(if (isBn) "প্রজেক্ট নাম" else "Project Title", fontSize = 10.sp) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }

                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = proj.link,
                                        onValueChange = {
                                            val list = localData.projects.toMutableList()
                                            list[idx] = proj.copy(link = it)
                                            localData = localData.copy(projects = list)
                                        },
                                        label = { Text(if (isBn) "লিংক / গিটহাব" else "Link / URL", fontSize = 10.sp) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedTextField(
                                value = proj.description,
                                onValueChange = {
                                    val list = localData.projects.toMutableList()
                                    list[idx] = proj.copy(description = it)
                                    localData = localData.copy(projects = list)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                maxLines = 5,
                                placeholder = { Text(if (isBn) "প্রজেক্টের মূল কাজ ও ফলাফল বর্ণনা করুন..." else "Describe key features and impact...", fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { commitAndRefresh(localData) },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    modifier = Modifier.fillMaxWidth().height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBn) "প্রজেক্ট সেভ ও রিফ্রেশ" else "Save & Refresh Projects", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // --- 7. CERTIFICATIONS & LANGUAGES ---
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionCardHeader(
                        title = if (isBn) "সার্টিফিকেশন ও ভাষা দক্ষতা" else "Certifications & Languages",
                        icon = Icons.Default.WorkspacePremium,
                        themeColors = themeColors, modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = {
                            val prompt = "Suggest 3-4 top professional certifications for a '${localData.jobTitle.ifBlank { "Professional" }}' on new lines."
                            onRequestAiPrompt(
                                if (isBn) "এআই সার্টিফিকেট সাজেস্ট" else "AI Certifications Suggest",
                                prompt,
                                "CERTIFICATIONS",
                                -1
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(if (isBn) "এআই" else "AI", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(if (isBn) "সার্টিফিকেশন ও প্রশিক্ষণ:" else "Certifications & Training:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = localData.certifications,
                    onValueChange = { localData = localData.copy(certifications = it) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 5,
                    placeholder = { Text(if (isBn) "সার্টিফিকেট নাম, প্রতিষ্ঠান ও সাল..." else "Certification Name, Issuing Org, Year...", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(if (isBn) "ভাষা দক্ষতা (Languages):" else "Language Proficiencies:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = localData.languages,
                    onValueChange = { localData = localData.copy(languages = it) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(if (isBn) "যেমন: বাংলা (Native), ইংরেজি (Fluent)" else "e.g., English (Fluent), Bengali (Native)", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { commitAndRefresh(localData) },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    modifier = Modifier.fillMaxWidth().height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBn) "সার্টিফিকেশন সেভ ও রিফ্রেশ" else "Save & Refresh Certifications", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // --- 8. CUSTOM SECTIONS ---
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionCardHeader(
                        title = if (isBn) "কাস্টম সেকশন (${localData.customSections.size})" else "Custom Sections (${localData.customSections.size})",
                        icon = Icons.Default.DashboardCustomize,
                        themeColors = themeColors, modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = {
                            val newList = localData.customSections + CvCustomSectionItem(
                                title = "",
                                content = ""
                            )
                            localData = localData.copy(customSections = newList)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBn) "নতুন সেকশন" else "Add Section", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                localData.customSections.forEachIndexed { idx, cSec ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = themeColors.background,
                        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (cSec.title.isNotBlank()) cSec.title else (if (isBn) "সেকশন #${idx + 1}" else "Section #${idx + 1}"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = themeColors.buttonEqualBg
                                )

                                IconButton(
                                    onClick = {
                                        val list = localData.customSections.toMutableList()
                                        list.removeAt(idx)
                                        localData = localData.copy(customSections = list)
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(15.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedTextField(
                                value = cSec.title,
                                onValueChange = {
                                    val list = localData.customSections.toMutableList()
                                    list[idx] = cSec.copy(title = it)
                                    localData = localData.copy(customSections = list)
                                },
                                label = { Text(if (isBn) "সেকশন শিরোনাম (Title)" else "Section Title", fontSize = 10.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedTextField(
                                value = cSec.content,
                                onValueChange = {
                                    val list = localData.customSections.toMutableList()
                                    list[idx] = cSec.copy(content = it)
                                    localData = localData.copy(customSections = list)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                maxLines = 5,
                                placeholder = { Text(if (isBn) "সেকশনের বিবরণ লিখুন..." else "Enter section details...", fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { commitAndRefresh(localData) },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    modifier = Modifier.fillMaxWidth().height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBn) "কাস্টম সেকশন সেভ ও রিফ্রেশ" else "Save Custom Sections", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // --- 9. MARGINS, PADDING & SPACING ---
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                SectionCardHeader(
                    title = if (isBn) "মার্জিন ও স্পেসিং কন্ট্রোল" else "Margin & Spacing Controls",
                    icon = Icons.Default.SpaceDashboard,
                    themeColors = themeColors
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(if (isBn) "মার্জিন: ${localData.customMargin.toInt()}" else "Margin: ${localData.customMargin.toInt()}", fontSize = 11.sp, color = themeColors.displayText, fontWeight = FontWeight.Bold)
                Slider(
                    value = localData.customMargin,
                    onValueChange = { localData = localData.copy(customMargin = it) },
                    valueRange = 20f..80f,
                    steps = 60,
                    colors = SliderDefaults.colors(thumbColor = themeColors.buttonEqualBg, activeTrackColor = themeColors.buttonEqualBg)
                )

                Text(if (isBn) "সেকশন স্পেসিং: ${localData.sectionSpacing.toInt()}" else "Section Spacing: ${localData.sectionSpacing.toInt()}", fontSize = 11.sp, color = themeColors.displayText, fontWeight = FontWeight.Bold)
                Slider(
                    value = localData.sectionSpacing,
                    onValueChange = { localData = localData.copy(sectionSpacing = it) },
                    valueRange = 2f..24f,
                    steps = 22,
                    colors = SliderDefaults.colors(thumbColor = themeColors.buttonEqualBg, activeTrackColor = themeColors.buttonEqualBg)
                )

                Text(if (isBn) "আইটেম স্পেসিং: ${localData.itemSpacing.toInt()}" else "Item Spacing: ${localData.itemSpacing.toInt()}", fontSize = 11.sp, color = themeColors.displayText, fontWeight = FontWeight.Bold)
                Slider(
                    value = localData.itemSpacing,
                    onValueChange = { localData = localData.copy(itemSpacing = it) },
                    valueRange = 0f..16f,
                    steps = 16,
                    colors = SliderDefaults.colors(thumbColor = themeColors.buttonEqualBg, activeTrackColor = themeColors.buttonEqualBg)
                )

                Text(if (isBn) "লাইন স্পেসিং: ${String.format("%.2f", localData.customLineSpacing)}" else "Line Spacing: ${String.format("%.2f", localData.customLineSpacing)}", fontSize = 11.sp, color = themeColors.displayText, fontWeight = FontWeight.Bold)
                Slider(
                    value = localData.customLineSpacing,
                    onValueChange = { localData = localData.copy(customLineSpacing = it) },
                    valueRange = 0.8f..2.0f,
                    steps = 24,
                    colors = SliderDefaults.colors(thumbColor = themeColors.buttonEqualBg, activeTrackColor = themeColors.buttonEqualBg)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { commitAndRefresh(localData) },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    modifier = Modifier.fillMaxWidth().height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBn) "মার্জিন ও স্পেসিং সেভ ও রিফ্রেশ" else "Save & Refresh Spacing", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}


