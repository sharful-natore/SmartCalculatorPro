package com.example.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

// --- Data Models ---

data class SavedPhotoItem(
    val id: String = UUID.randomUUID().toString(),
    val fileName: String,
    val filePath: String?,
    val uriString: String,
    val width: Int,
    val height: Int,
    val sizeBytes: Long,
    val format: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ProcessedPhotoResult(
    val bitmap: Bitmap,
    val byteArray: ByteArray,
    val widthPx: Int,
    val heightPx: Int,
    val sizeBytes: Int,
    val format: String,
    val isBoosted: Boolean = false,
    val originalCompressedSizeBytes: Int = 0
)

// --- History Persistence and File Management ---

object PhotoResizerHistoryManager {
    private const val PREFS_NAME = "photo_resizer_storage"
    private const val KEY_SAVED_PHOTOS = "saved_photos_json"

    fun getValidSavedPhotos(context: Context): List<SavedPhotoItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_SAVED_PHOTOS, null)
        val list = mutableListOf<SavedPhotoItem>()

        if (!jsonStr.isNullOrEmpty()) {
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        SavedPhotoItem(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            fileName = obj.optString("fileName"),
                            filePath = obj.optString("filePath").takeIf { it.isNotEmpty() },
                            uriString = obj.optString("uriString"),
                            width = obj.optInt("width"),
                            height = obj.optInt("height"),
                            sizeBytes = obj.optLong("sizeBytes"),
                            format = obj.optString("format", "JPG"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Also scan public Pictures/PhotoResizer folder in case user saved or placed photos directly
        try {
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val appFolder = File(picturesDir, "PhotoResizer")
            if (appFolder.exists() && appFolder.isDirectory) {
                val existingFiles = appFolder.listFiles { file ->
                    val name = file.name.lowercase()
                    name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp")
                }
                existingFiles?.forEach { f ->
                    if (list.none { it.fileName == f.name || it.filePath == f.absolutePath }) {
                        val fmt = f.extension.uppercase()
                        list.add(
                            SavedPhotoItem(
                                fileName = f.name,
                                filePath = f.absolutePath,
                                uriString = "",
                                width = 300,
                                height = 300,
                                sizeBytes = f.length(),
                                format = fmt,
                                timestamp = f.lastModified()
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // FILTER: Keep ONLY items that truly still exist in device memory/storage!
        val validList = list.filter { isPhotoExists(context, it) }
            .sortedByDescending { it.timestamp }

        // Update preferences so deleted items are removed
        saveListToPrefs(context, validList)
        return validList
    }

    fun addSavedPhoto(context: Context, item: SavedPhotoItem) {
        val current = getValidSavedPhotos(context).toMutableList()
        current.removeAll { it.fileName == item.fileName }
        current.add(0, item)
        saveListToPrefs(context, current)
    }

    fun removeSavedPhoto(context: Context, id: String) {
        val current = getValidSavedPhotos(context).toMutableList()
        current.removeAll { it.id == id }
        saveListToPrefs(context, current)
    }

    fun updateSavedPhoto(context: Context, updated: SavedPhotoItem) {
        val current = getValidSavedPhotos(context).toMutableList()
        val index = current.indexOfFirst { it.id == updated.id }
        if (index >= 0) {
            current[index] = updated
        } else {
            current.add(0, updated)
        }
        saveListToPrefs(context, current)
    }

    private fun saveListToPrefs(context: Context, list: List<SavedPhotoItem>) {
        try {
            val array = JSONArray()
            list.forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("fileName", item.fileName)
                    put("filePath", item.filePath ?: "")
                    put("uriString", item.uriString)
                    put("width", item.width)
                    put("height", item.height)
                    put("sizeBytes", item.sizeBytes)
                    put("format", item.format)
                    put("timestamp", item.timestamp)
                }
                array.put(obj)
            }
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_SAVED_PHOTOS, array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isPhotoExists(context: Context, item: SavedPhotoItem): Boolean {
        // 1. Check physical file path if present
        if (!item.filePath.isNullOrEmpty()) {
            val file = File(item.filePath)
            if (file.exists() && file.length() > 0) return true
        }

        // 2. Check public directory Pictures/PhotoResizer
        try {
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val fileInFolder = File(File(picturesDir, "PhotoResizer"), item.fileName)
            if (fileInFolder.exists() && fileInFolder.length() > 0) return true
        } catch (e: Exception) {
            // ignore
        }

        // 3. Check MediaStore Content Uri
        if (item.uriString.isNotEmpty() && item.uriString.startsWith("content://")) {
            try {
                val uri = Uri.parse(item.uriString)
                context.contentResolver.openInputStream(uri)?.use {
                    return true
                }
            } catch (e: Exception) {
                // Not found or deleted
            }
        }

        return false
    }

    fun deletePhotoFromDevice(context: Context, item: SavedPhotoItem): Boolean {
        var isDeleted = false

        // 1. Delete via MediaStore Uri
        if (item.uriString.isNotEmpty() && item.uriString.startsWith("content://")) {
            try {
                val uri = Uri.parse(item.uriString)
                val rows = context.contentResolver.delete(uri, null, null)
                if (rows > 0) isDeleted = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. Delete physical file if filePath exists
        if (!item.filePath.isNullOrEmpty()) {
            try {
                val file = File(item.filePath)
                if (file.exists()) {
                    if (file.delete()) isDeleted = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 3. Delete from Pictures/PhotoResizer directory
        try {
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val fileInFolder = File(File(picturesDir, "PhotoResizer"), item.fileName)
            if (fileInFolder.exists()) {
                if (fileInFolder.delete()) isDeleted = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Remove from persistent history
        removeSavedPhoto(context, item.id)
        return isDeleted
    }

    fun renamePhotoOnDevice(context: Context, item: SavedPhotoItem, newBaseName: String): SavedPhotoItem? {
        val ext = item.fileName.substringAfterLast('.', "")
        val sanitized = newBaseName.trim().removeSuffix(".$ext")
        if (sanitized.isEmpty()) return null
        val fullNewName = if (ext.isNotEmpty()) "$sanitized.$ext" else sanitized

        var updatedPath = item.filePath

        // 1. MediaStore rename
        if (item.uriString.isNotEmpty() && item.uriString.startsWith("content://")) {
            try {
                val uri = Uri.parse(item.uriString)
                val cv = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fullNewName)
                }
                context.contentResolver.update(uri, cv, null, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. Physical file rename
        if (!item.filePath.isNullOrEmpty()) {
            try {
                val oldFile = File(item.filePath)
                if (oldFile.exists()) {
                    val newFile = File(oldFile.parentFile, fullNewName)
                    if (oldFile.renameTo(newFile)) {
                        updatedPath = newFile.absolutePath
                        android.media.MediaScannerConnection.scanFile(
                            context,
                            arrayOf(updatedPath),
                            null,
                            null
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Also check Pictures/PhotoResizer folder
        try {
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val oldF = File(File(picturesDir, "PhotoResizer"), item.fileName)
            if (oldF.exists()) {
                val newF = File(File(picturesDir, "PhotoResizer"), fullNewName)
                if (oldF.renameTo(newF)) {
                    updatedPath = newF.absolutePath
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val updatedItem = item.copy(fileName = fullNewName, filePath = updatedPath)
        updateSavedPhoto(context, updatedItem)
        return updatedItem
    }

    fun sharePhoto(context: Context, item: SavedPhotoItem) {
        try {
            val shareUri: Uri = if (item.uriString.isNotEmpty() && item.uriString.startsWith("content://")) {
                Uri.parse(item.uriString)
            } else if (!item.filePath.isNullOrEmpty()) {
                val f = File(item.filePath)
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", f)
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val f = File(File(picturesDir, "PhotoResizer"), item.fileName)
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", f)
            }

            val mimeType = when (item.format.uppercase()) {
                "PNG" -> "image/png"
                "WEBP" -> "image/webp"
                else -> "image/jpeg"
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, shareUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, if (item.fileName.isNotEmpty()) item.fileName else "Share Photo")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "শেয়ার করতে সমস্যা হয়েছে: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

// --- JPEG/PNG Safe Padding & Size Boosting Engine for Job Applications ---

/**
 * Standard-compliant JPEG COM (0xFF 0xFE) padding engine.
 * Adds harmless comment metadata segments right after SOI (0xFF 0xD8).
 * All standard image viewers, Android BitmapFactory, and job portal servers parse
 * the JPEG flawlessly, and the file size reaches the exact required target bytes.
 */
fun padJpegToTargetBytes(jpegBytes: ByteArray, targetBytes: Int): ByteArray {
    if (jpegBytes.size >= targetBytes) return jpegBytes
    if (jpegBytes.size < 4 || jpegBytes[0] != 0xFF.toByte() || jpegBytes[1] != 0xD8.toByte()) {
        // Fallback for non-standard JPEG header
        val pad = targetBytes - jpegBytes.size
        return jpegBytes + ByteArray(pad) { 0x20 }
    }

    val padNeeded = targetBytes - jpegBytes.size
    var rem = padNeeded
    val segments = ByteArrayOutputStream()

    while (rem > 0) {
        if (rem < 5) {
            // Append trailing spaces after file if remaining padding is less than 5 bytes
            val trailing = ByteArray(rem) { 0x20 }
            val baseResult = ByteArray(2 + segments.size() + (jpegBytes.size - 2))
            baseResult[0] = jpegBytes[0]
            baseResult[1] = jpegBytes[1]
            val segBytes = segments.toByteArray()
            System.arraycopy(segBytes, 0, baseResult, 2, segBytes.size)
            System.arraycopy(jpegBytes, 2, baseResult, 2 + segBytes.size, jpegBytes.size - 2)
            return baseResult + trailing
        }

        val chunkTotal = rem.coerceAtMost(65530)
        val segLen = chunkTotal - 2
        val payloadLen = segLen - 2

        segments.write(0xFF)
        segments.write(0xFE)
        segments.write((segLen shr 8) and 0xFF)
        segments.write(segLen and 0xFF)

        val fillerText = "ToolsMate Job Portal File Size Optimization Padding Chunk ".toByteArray(Charsets.US_ASCII)
        val payload = ByteArray(payloadLen) { idx -> fillerText[idx % fillerText.size] }
        segments.write(payload)

        rem -= chunkTotal
    }

    val segBytes = segments.toByteArray()
    val finalArray = ByteArray(2 + segBytes.size + (jpegBytes.size - 2))
    finalArray[0] = jpegBytes[0]
    finalArray[1] = jpegBytes[1]
    System.arraycopy(segBytes, 0, finalArray, 2, segBytes.size)
    System.arraycopy(jpegBytes, 2, finalArray, 2 + segBytes.size, jpegBytes.size - 2)
    return finalArray
}

fun padGenericImageBytes(bytes: ByteArray, targetBytes: Int): ByteArray {
    if (bytes.size >= targetBytes) return bytes
    val padding = ByteArray(targetBytes - bytes.size) { 0x20 }
    return bytes + padding
}

// --- Color & Light Adjustments Engine ---
fun applyColorAdjustments(
    src: Bitmap,
    brightness: Float, // -100 to 100
    contrast: Float,   // -100 to 100
    warmth: Float,     // -100 to 100
    saturation: Float  // -100 to 100
): Bitmap {
    if (brightness == 0f && contrast == 0f && warmth == 0f && saturation == 0f) {
        return src
    }

    val finalMatrix = android.graphics.ColorMatrix()

    // 1. Contrast
    if (contrast != 0f) {
        val scale = if (contrast > 0) 1f + (contrast / 100f) * 1.5f else 1f + (contrast / 100f) * 0.8f
        val translate = (-0.5f * scale + 0.5f) * 255f
        val cMatrix = android.graphics.ColorMatrix(floatArrayOf(
            scale, 0f, 0f, 0f, translate,
            0f, scale, 0f, 0f, translate,
            0f, 0f, scale, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        ))
        finalMatrix.postConcat(cMatrix)
    }

    // 2. Brightness
    if (brightness != 0f) {
        val bShift = brightness * 1.5f
        val bMatrix = android.graphics.ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, bShift,
            0f, 1f, 0f, 0f, bShift,
            0f, 0f, 1f, 0f, bShift,
            0f, 0f, 0f, 1f, 0f
        ))
        finalMatrix.postConcat(bMatrix)
    }

    // 3. Saturation
    if (saturation != 0f) {
        val satFactor = ((saturation + 100f) / 100f).coerceIn(0f, 3f)
        val sMatrix = android.graphics.ColorMatrix()
        sMatrix.setSaturation(satFactor)
        finalMatrix.postConcat(sMatrix)
    }

    // 4. Warmth (Color Temperature)
    if (warmth != 0f) {
        val rFactor = (1f + (warmth / 250f)).coerceAtLeast(0.1f)
        val bFactor = (1f - (warmth / 250f)).coerceAtLeast(0.1f)
        val wMatrix = android.graphics.ColorMatrix(floatArrayOf(
            rFactor, 0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f, 0f,
            0f, 0f, bFactor, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
        finalMatrix.postConcat(wMatrix)
    }

    return try {
        val output = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)
        val paint = android.graphics.Paint().apply {
            colorFilter = android.graphics.ColorMatrixColorFilter(finalMatrix)
        }
        canvas.drawBitmap(src, 0f, 0f, paint)
        output
    } catch (e: Exception) {
        src
    }
}

// --- Image Processing Utility ---

private fun processImageResult(
    context: Context,
    originalBmp: Bitmap?,
    cropPreset: String,
    rotation: Float,
    isFlipped: Boolean,
    unit: String,
    widthStr: String,
    heightStr: String,
    targetFormat: String,
    targetKbMode: String,
    customKbStr: String,
    manualQuality: Int,
    boostMinKbEnabled: Boolean,
    minTargetKb: Int,
    brightness: Float = 0f,
    contrast: Float = 0f,
    warmth: Float = 0f,
    saturation: Float = 0f
): ProcessedPhotoResult? {
    if (originalBmp == null) return null

    try {
        var workingBmp: Bitmap = originalBmp

        // 1. Rotation & Flip
        val matrix = Matrix().apply {
            if (rotation != 0f) postRotate(rotation)
            if (isFlipped) postScale(-1f, 1f, workingBmp.width / 2f, workingBmp.height / 2f)
        }
        if (rotation != 0f || isFlipped) {
            workingBmp = Bitmap.createBitmap(workingBmp, 0, 0, workingBmp.width, workingBmp.height, matrix, true)
        }

        // 2. Crop Preset
        workingBmp = when (cropPreset) {
            "1:1", "300x300" -> cropToAspectRatio(workingBmp, 1f, 1f)
            "3:4" -> cropToAspectRatio(workingBmp, 3f, 4f)
            "300x80" -> cropToAspectRatio(workingBmp, 300f, 80f)
            "4:3" -> cropToAspectRatio(workingBmp, 4f, 3f)
            "16:9" -> cropToAspectRatio(workingBmp, 16f, 9f)
            else -> workingBmp
        }

        // 3. Dimension Conversion to Pixels
        val dpi = 300f
        val userW = widthStr.toFloatOrNull() ?: workingBmp.width.toFloat()
        val userH = heightStr.toFloatOrNull() ?: workingBmp.height.toFloat()

        val targetPxW = when (unit) {
            "cm" -> (userW * dpi / 2.54f).toInt()
            "mm" -> (userW * dpi / 25.4f).toInt()
            "in" -> (userW * dpi).toInt()
            else -> userW.toInt()
        }.coerceIn(10, 8000)

        val targetPxH = when (unit) {
            "cm" -> (userH * dpi / 2.54f).toInt()
            "mm" -> (userH * dpi / 25.4f).toInt()
            "in" -> (userH * dpi).toInt()
            else -> userH.toInt()
        }.coerceIn(10, 8000)

        if (workingBmp.width != targetPxW || workingBmp.height != targetPxH) {
            workingBmp = Bitmap.createScaledBitmap(workingBmp, targetPxW, targetPxH, true)
        }

        // Apply Color & Light Adjustments (Brightness, Contrast, Warmth, Saturation)
        workingBmp = applyColorAdjustments(workingBmp, brightness, contrast, warmth, saturation)

        // 4. Target KB / Compression Logic
        val maxTargetKb = when (targetKbMode) {
            "< 50 KB" -> 50
            "< 100 KB" -> 100
            "< 200 KB" -> 200
            "Job Photo (30-100 KB)" -> 95
            "Job Signature (10-50 KB)" -> 45
            "Custom KB" -> customKbStr.toIntOrNull() ?: 100
            else -> 0
        }

        val compressFormat = when (targetFormat) {
            "PNG" -> Bitmap.CompressFormat.PNG
            "WEBP" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                @Suppress("DEPRECATION")
                Bitmap.CompressFormat.WEBP
            }
            else -> Bitmap.CompressFormat.JPEG
        }

        var finalBytes: ByteArray
        var finalBmp = workingBmp
        var q = manualQuality.coerceIn(5, 100)

        if (maxTargetKb > 0 && compressFormat != Bitmap.CompressFormat.PNG) {
            val maxBytes = maxTargetKb * 1024
            var scaleFactor = 1.0f

            do {
                if (scaleFactor < 1.0f) {
                    val scaledW = (workingBmp.width * scaleFactor).toInt().coerceAtLeast(30)
                    val scaledH = (workingBmp.height * scaleFactor).toInt().coerceAtLeast(30)
                    finalBmp = Bitmap.createScaledBitmap(workingBmp, scaledW, scaledH, true)
                }

                q = 95
                val baos = ByteArrayOutputStream()
                finalBmp.compress(compressFormat, q, baos)
                finalBytes = baos.toByteArray()

                while (finalBytes.size > maxBytes && q > 10) {
                    q -= 8
                    val os = ByteArrayOutputStream()
                    finalBmp.compress(compressFormat, q, os)
                    finalBytes = os.toByteArray()
                }

                if (finalBytes.size > maxBytes && (finalBmp.width > 100 || finalBmp.height > 100)) {
                    scaleFactor *= 0.85f
                } else {
                    break
                }
            } while (finalBytes.size > maxBytes && scaleFactor > 0.2f)
        } else {
            val baos = ByteArrayOutputStream()
            finalBmp.compress(compressFormat, q, baos)
            finalBytes = baos.toByteArray()
        }

        val originalCompressedSize = finalBytes.size
        var isBoosted = false

        // 5. JOB PORTAL MINIMUM SIZE / BOOST LOGIC:
        // When a photo (e.g. 5 KB signature or small avatar) needs to be >= 30 KB or target KB:
        val effectiveMinKb = when {
            targetKbMode == "Job Photo (30-100 KB)" -> 40 // Guarantees 40 KB (within 30-100 KB)
            targetKbMode == "Job Signature (10-50 KB)" -> 20 // Guarantees 20 KB (within 10-50 KB)
            boostMinKbEnabled && minTargetKb > 0 -> minTargetKb
            boostMinKbEnabled && targetKbMode == "Custom KB" -> customKbStr.toIntOrNull() ?: 0
            else -> 0
        }

        if (effectiveMinKb > 0) {
            val minTargetBytes = effectiveMinKb * 1024
            if (finalBytes.size < minTargetBytes) {
                finalBytes = if (compressFormat == Bitmap.CompressFormat.JPEG) {
                    padJpegToTargetBytes(finalBytes, minTargetBytes)
                } else {
                    padGenericImageBytes(finalBytes, minTargetBytes)
                }
                isBoosted = true
            }
        }

        return ProcessedPhotoResult(
            bitmap = finalBmp,
            byteArray = finalBytes,
            widthPx = finalBmp.width,
            heightPx = finalBmp.height,
            sizeBytes = finalBytes.size,
            format = targetFormat,
            isBoosted = isBoosted,
            originalCompressedSizeBytes = originalCompressedSize
        )
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

private fun cropToAspectRatio(src: Bitmap, targetW: Float, targetH: Float): Bitmap {
    val srcW = src.width.toFloat()
    val srcH = src.height.toFloat()
    val targetRatio = targetW / targetH
    val srcRatio = srcW / srcH

    var cropW = srcW
    var cropH = srcH

    if (srcRatio > targetRatio) {
        cropW = srcH * targetRatio
    } else {
        cropH = srcW / targetRatio
    }

    val left = ((srcW - cropW) / 2f).coerceAtLeast(0f).toInt()
    val top = ((srcH - cropH) / 2f).coerceAtLeast(0f).toInt()
    val width = cropW.toInt().coerceAtMost(src.width - left)
    val height = cropH.toInt().coerceAtMost(src.height - top)

    return Bitmap.createBitmap(src, left, top, width, height)
}

private suspend fun saveProcessedPhotoToGallery(context: Context, result: ProcessedPhotoResult): SavedPhotoItem? = withContext(Dispatchers.IO) {
    val ext = when (result.format.uppercase()) {
        "PNG" -> "png"
        "WEBP" -> "webp"
        else -> "jpg"
    }
    val mimeType = when (result.format.uppercase()) {
        "PNG" -> "image/png"
        "WEBP" -> "image/webp"
        else -> "image/jpeg"
    }
    val filename = "PhotoResizer_${System.currentTimeMillis()}.$ext"

    // 1. Android Q+ MediaStore
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PhotoResizer")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { os ->
                    os.write(result.byteArray)
                    os.flush()
                }
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)

                val item = SavedPhotoItem(
                    fileName = filename,
                    filePath = null,
                    uriString = uri.toString(),
                    width = result.widthPx,
                    height = result.heightPx,
                    sizeBytes = result.sizeBytes.toLong(),
                    format = result.format
                )
                PhotoResizerHistoryManager.addSavedPhoto(context, item)
                return@withContext item
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 2. Direct File in Pictures/PhotoResizer
    try {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val appFolder = File(picturesDir, "PhotoResizer")
        if (!appFolder.exists()) {
            appFolder.mkdirs()
        }
        val targetFile = File(appFolder, filename)
        FileOutputStream(targetFile).use { fos ->
            fos.write(result.byteArray)
            fos.flush()
        }
        android.media.MediaScannerConnection.scanFile(
            context,
            arrayOf(targetFile.absolutePath),
            arrayOf(mimeType),
            null
        )
        val item = SavedPhotoItem(
            fileName = filename,
            filePath = targetFile.absolutePath,
            uriString = "",
            width = result.widthPx,
            height = result.heightPx,
            sizeBytes = result.sizeBytes.toLong(),
            format = result.format
        )
        PhotoResizerHistoryManager.addSavedPhoto(context, item)
        return@withContext item
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // 3. App External Files Pictures Fallback
    try {
        val extDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (extDir != null) {
            val targetFile = File(extDir, filename)
            FileOutputStream(targetFile).use { fos ->
                fos.write(result.byteArray)
                fos.flush()
            }
            val item = SavedPhotoItem(
                fileName = filename,
                filePath = targetFile.absolutePath,
                uriString = "",
                width = result.widthPx,
                height = result.heightPx,
                sizeBytes = result.sizeBytes.toLong(),
                format = result.format
            )
            PhotoResizerHistoryManager.addSavedPhoto(context, item)
            return@withContext item
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return@withContext null
}

// --- Main Composable: PhotoLabCard ---

@Composable
fun PhotoLabCard(viewModel: CalculatorViewModel, themeColors: CalculatorThemeColors) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Screen Top Tab: 0 = Photo Resizer Workspace, 1 = Saved History
    var mainNavTab by remember { mutableIntStateOf(0) }

    // History state
    var savedHistoryList by remember { mutableStateOf<List<SavedPhotoItem>>(emptyList()) }
    fun refreshHistory() {
        savedHistoryList = PhotoResizerHistoryManager.getValidSavedPhotos(context)
    }

    LaunchedEffect(mainNavTab) {
        refreshHistory()
    }

    // Workspace states
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedWorkspaceTab by remember { mutableIntStateOf(0) }

    // Settings
    var cropPreset by remember { mutableStateOf("Original") }
    var rotationDegrees by remember { mutableFloatStateOf(0f) }
    var isFlippedHorizontal by remember { mutableStateOf(false) }

    var unit by remember { mutableStateOf("px") }
    var inputWidth by remember { mutableStateOf("300") }
    var inputHeight by remember { mutableStateOf("300") }
    var lockAspectRatio by remember { mutableStateOf(true) }

    var targetFormat by remember { mutableStateOf("JPG") }
    var targetKbMode by remember { mutableStateOf("Original") }
    var customTargetKb by remember { mutableStateOf("100") }
    var manualQuality by remember { mutableFloatStateOf(90f) }

    // Size Boosting for Job Portal (< 30 KB fix)
    var boostMinKbEnabled by remember { mutableStateOf(true) }
    var minKbValue by remember { mutableStateOf("35") }

    // Color & Light adjustment states
    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(0f) }
    var warmth by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(0f) }

    var isSaving by remember { mutableStateOf(false) }
    var customCroppedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showInteractiveCropDialog by remember { mutableStateOf(false) }

    // Post-Save Preview Dialog state
    var postSavePhotoItem by remember { mutableStateOf<SavedPhotoItem?>(null) }

    // Item-level action dialog states for history & preview
    var renamingTargetItem by remember { mutableStateOf<SavedPhotoItem?>(null) }
    var deletingTargetItem by remember { mutableStateOf<SavedPhotoItem?>(null) }

    // Load original bitmap
    val originalBitmap = remember(selectedImageUri) {
        selectedImageUri?.let { uri ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                    android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    LaunchedEffect(originalBitmap) {
        originalBitmap?.let { bmp ->
            inputWidth = bmp.width.toString()
            inputHeight = bmp.height.toString()
        }
    }

    val originalSizeBytes = remember(selectedImageUri) {
        selectedImageUri?.let { uri ->
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    pfd.statSize
                } ?: 0L
            } catch (e: Exception) {
                0L
            }
        } ?: 0L
    }

    // Processed photo in real-time
    val processedResult = remember(
        originalBitmap,
        customCroppedBitmap,
        cropPreset,
        rotationDegrees,
        isFlippedHorizontal,
        unit,
        inputWidth,
        inputHeight,
        targetFormat,
        targetKbMode,
        customTargetKb,
        manualQuality,
        boostMinKbEnabled,
        minKbValue,
        brightness,
        contrast,
        warmth,
        saturation
    ) {
        processImageResult(
            context = context,
            originalBmp = customCroppedBitmap ?: originalBitmap,
            cropPreset = cropPreset,
            rotation = rotationDegrees,
            isFlipped = isFlippedHorizontal,
            unit = unit,
            widthStr = inputWidth,
            heightStr = inputHeight,
            targetFormat = targetFormat,
            targetKbMode = targetKbMode,
            customKbStr = customTargetKb,
            manualQuality = manualQuality.toInt(),
            boostMinKbEnabled = boostMinKbEnabled,
            minTargetKb = minKbValue.toIntOrNull() ?: 30,
            brightness = brightness,
            contrast = contrast,
            warmth = warmth,
            saturation = saturation
        )
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            customCroppedBitmap = null
            cropPreset = "Original"
            rotationDegrees = 0f
            isFlippedHorizontal = false
            unit = "px"
            targetFormat = "JPG"
            targetKbMode = "Original"
            customTargetKb = "100"
            manualQuality = 90f
            brightness = 0f
            contrast = 0f
            warmth = 0f
            saturation = 0f
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Tool Header with Navigation Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(themeColors.buttonEqualBg.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AspectRatio,
                            contentDescription = null,
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isBn) "ফটো ল্যাব ও রিসাইজার" else "Photo Lab & Resizer",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        Text(
                            text = if (isBn) "ক্রপ, কাস্টম মাপ, সাইজ বুস্টার ও হিস্টোরি" else "Crop, custom size, KB booster & history",
                            fontSize = 10.5.sp,
                            color = themeColors.displayText.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Navigation: Editor vs Saved History
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(themeColors.displayText.copy(alpha = 0.05f))
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (mainNavTab == 0) themeColors.buttonEqualBg else Color.Transparent)
                        .clickable { mainNavTab = 0 }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = if (mainNavTab == 0) Color.White else themeColors.displayText,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isBn) "রিসাইজ এডিটর" else "Resize Tool",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (mainNavTab == 0) Color.White else themeColors.displayText
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (mainNavTab == 1) themeColors.buttonEqualBg else Color.Transparent)
                        .clickable {
                            refreshHistory()
                            mainNavTab = 1
                        }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FolderSpecial,
                            contentDescription = null,
                            tint = if (mainNavTab == 1) Color.White else themeColors.displayText,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isBn) "সেভ করা হিস্টোরি (${savedHistoryList.size})" else "History (${savedHistoryList.size})",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (mainNavTab == 1) Color.White else themeColors.displayText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (mainNavTab == 0) {
                // ==================== WORKSPACE / EDITOR TAB ====================
                if (selectedImageUri == null) {
                    // Empty State: Select Image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(themeColors.displayText.copy(alpha = 0.03f))
                            .border(
                                width = 1.dp,
                                color = themeColors.displayText.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = themeColors.buttonEqualBg,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isBn) "গ্যালারি থেকে ছবি সিলেক্ট করুন" else "Select Photo from Gallery",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = themeColors.displayText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isBn) "চাকরি/পাসপোর্টের জন্য ছবি ও স্বাক্ষর নিখুঁতভাবে রিসাইজ করুন" else "Perfect resizing for BD Jobs, Signatures & Passports",
                                fontSize = 10.5.sp,
                                color = themeColors.displayText.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    // Active Workspace
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Canvas Preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (processedResult?.bitmap != null) {
                                Image(
                                    bitmap = processedResult.bitmap.asImageBitmap(),
                                    contentDescription = "Preview",
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .padding(8.dp)
                                )
                            } else {
                                CircularProgressIndicator(color = themeColors.buttonEqualBg)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Original vs Processed Info Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(themeColors.displayText.copy(alpha = 0.05f))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                val origMb = String.format(Locale.US, "%.1f KB", originalSizeBytes / 1024f)
                                val origW = originalBitmap?.width ?: 0
                                val origH = originalBitmap?.height ?: 0
                                Text(
                                    text = if (isBn) "মূল ছবি: $origW×$origH px ($origMb)" else "Original: $origW×$origH px ($origMb)",
                                    fontSize = 10.sp,
                                    color = themeColors.displayText.copy(alpha = 0.6f)
                                )
                                if (processedResult != null) {
                                    val procKb = String.format(Locale.US, "%.1f KB", processedResult.sizeBytes / 1024f)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (isBn) "আউটপুট: ${processedResult.widthPx}×${processedResult.heightPx} px • $procKb • ${processedResult.format}"
                                            else "Output: ${processedResult.widthPx}×${processedResult.heightPx} px • $procKb • ${processedResult.format}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.buttonEqualBg
                                        )
                                        if (processedResult.isBoosted) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "🚀 " + if (isBn) "বুস্টেড" else "Boosted",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF10B981)
                                            )
                                        }
                                    }
                                }
                            }

                            TextButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    if (isBn) "🔄 অন্য ছবি" else "🔄 Change",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.buttonEqualBg
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Presets
                        Text(
                            text = if (isBn) "সরকারি/চাকরি ও অফিশিয়াল প্রিসেট:" else "Official Quick Presets:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val officialPresets = listOf(
                                "BD Job Photo (300×300)",
                                "BD Job Signature (300×80)",
                                "Passport (40×50 mm)",
                                "Stamp (20×25 mm)",
                                "Square Avatar (1000×1000)"
                            )
                            officialPresets.forEach { p ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(themeColors.buttonEqualBg.copy(alpha = 0.12f))
                                        .border(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            when (p) {
                                                "BD Job Photo (300×300)" -> {
                                                    cropPreset = "300x300"
                                                    unit = "px"
                                                    inputWidth = "300"
                                                    inputHeight = "300"
                                                    targetKbMode = "Job Photo (30-100 KB)"
                                                    boostMinKbEnabled = true
                                                    minKbValue = "35"
                                                    targetFormat = "JPG"
                                                }
                                                "BD Job Signature (300×80)" -> {
                                                    cropPreset = "300x80"
                                                    unit = "px"
                                                    inputWidth = "300"
                                                    inputHeight = "80"
                                                    targetKbMode = "Job Signature (10-50 KB)"
                                                    boostMinKbEnabled = true
                                                    minKbValue = "15"
                                                    targetFormat = "JPG"
                                                }
                                                "Passport (40×50 mm)" -> {
                                                    cropPreset = "3:4"
                                                    unit = "mm"
                                                    inputWidth = "40"
                                                    inputHeight = "50"
                                                    targetKbMode = "< 100 KB"
                                                    targetFormat = "JPG"
                                                }
                                                "Stamp (20×25 mm)" -> {
                                                    cropPreset = "3:4"
                                                    unit = "mm"
                                                    inputWidth = "20"
                                                    inputHeight = "25"
                                                    targetKbMode = "< 50 KB"
                                                    targetFormat = "JPG"
                                                }
                                                "Square Avatar (1000×1000)" -> {
                                                    cropPreset = "1:1"
                                                    unit = "px"
                                                    inputWidth = "1000"
                                                    inputHeight = "1000"
                                                    targetKbMode = "Original"
                                                    targetFormat = "PNG"
                                                }
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = p,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.buttonEqualBg
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Workspace Tabs
                        ScrollableTabRow(
                            selectedTabIndex = selectedWorkspaceTab,
                            containerColor = Color.Transparent,
                            contentColor = themeColors.buttonEqualBg,
                            edgePadding = 0.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Tab(
                                selected = selectedWorkspaceTab == 0,
                                onClick = { selectedWorkspaceTab = 0 },
                                text = { Text(if (isBn) "১. ক্রপ" else "1. Crop", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = selectedWorkspaceTab == 1,
                                onClick = { selectedWorkspaceTab = 1 },
                                text = { Text(if (isBn) "২. সাইজ" else "2. Dimensions", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = selectedWorkspaceTab == 2,
                                onClick = { selectedWorkspaceTab = 2 },
                                text = { Text(if (isBn) "৩. আলো ও রঙ" else "3. Light & Color", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = selectedWorkspaceTab == 3,
                                onClick = { selectedWorkspaceTab = 3 },
                                text = { Text(if (isBn) "৪. সাইজ বুস্টার" else "4. Size & Booster", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = selectedWorkspaceTab == 4,
                                onClick = { selectedWorkspaceTab = 4 },
                                text = { Text(if (isBn) "৫. ফরম্যাট" else "5. Format", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        when (selectedWorkspaceTab) {
                            0 -> {
                                // CROP & ROTATE
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(text = if (isBn) "ক্রপ অনুপাত (Aspect Ratio):" else "Crop Aspect Ratio:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val cropRatios = listOf("Original", "1:1", "3:4", "300x300", "300x80", "4:3", "16:9")
                                        cropRatios.forEach { ratio ->
                                            val isSel = cropPreset == ratio
                                            FilterChip(
                                                selected = isSel,
                                                onClick = { cropPreset = ratio },
                                                label = { Text(ratio, fontSize = 10.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = themeColors.buttonEqualBg,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = { showInteractiveCropDialog = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Crop, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isBn) "✂️ প্রফেশনাল জুমেবল ক্রপ টুল" else "✂️ Professional Zoomable Crop Tool",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { rotationDegrees = (rotationDegrees + 90f) % 360f },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isBn) "ঘুরান (${rotationDegrees.toInt()}°)" else "Rotate (${rotationDegrees.toInt()}°)", fontSize = 11.sp)
                                        }

                                        OutlinedButton(
                                            onClick = { isFlippedHorizontal = !isFlippedHorizontal },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Flip, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isBn) "ফ্লিপ করুন" else "Flip Horizontal", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            1 -> {
                                // DIMENSIONS (W x H)
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = if (isBn) "একক নির্বাচন (Unit):" else "Select Unit:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            val units = listOf("px", "cm", "mm", "in")
                                            units.forEach { u ->
                                                val isSel = unit == u
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (isSel) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.08f))
                                                        .clickable {
                                                            if (unit != u) {
                                                                unit = u
                                                                val currentW = inputWidth.toFloatOrNull() ?: 300f
                                                                val currentH = inputHeight.toFloatOrNull() ?: 300f
                                                                when (u) {
                                                                    "cm" -> {
                                                                        inputWidth = String.format(Locale.US, "%.1f", currentW / 118.11f)
                                                                        inputHeight = String.format(Locale.US, "%.1f", currentH / 118.11f)
                                                                    }
                                                                    "mm" -> {
                                                                        inputWidth = String.format(Locale.US, "%.0f", currentW / 11.811f)
                                                                        inputHeight = String.format(Locale.US, "%.0f", currentH / 11.811f)
                                                                    }
                                                                    "in" -> {
                                                                        inputWidth = String.format(Locale.US, "%.1f", currentW / 300f)
                                                                        inputHeight = String.format(Locale.US, "%.1f", currentH / 300f)
                                                                    }
                                                                    else -> {
                                                                        inputWidth = "300"
                                                                        inputHeight = "300"
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(u, fontSize = 11.sp, color = if (isSel) Color.White else themeColors.displayText, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = inputWidth,
                                            onValueChange = { newW ->
                                                inputWidth = newW
                                                if (lockAspectRatio) {
                                                    val wVal = newW.toFloatOrNull()
                                                    val origW = originalBitmap?.width?.toFloat() ?: 1f
                                                    val origH = originalBitmap?.height?.toFloat() ?: 1f
                                                    if (wVal != null && origW > 0) {
                                                        val calculatedH = wVal * (origH / origW)
                                                        inputHeight = if (unit == "px" || unit == "mm") calculatedH.toInt().toString()
                                                        else String.format(Locale.US, "%.1f", calculatedH)
                                                    }
                                                }
                                            },
                                            label = { Text(if (isBn) "প্রস্থ ($unit)" else "Width ($unit)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )

                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            tint = themeColors.displayText.copy(alpha = 0.5f),
                                            modifier = Modifier.size(16.dp)
                                        )

                                        OutlinedTextField(
                                            value = inputHeight,
                                            onValueChange = { newH ->
                                                inputHeight = newH
                                                if (lockAspectRatio) {
                                                    val hVal = newH.toFloatOrNull()
                                                    val origW = originalBitmap?.width?.toFloat() ?: 1f
                                                    val origH = originalBitmap?.height?.toFloat() ?: 1f
                                                    if (hVal != null && origH > 0) {
                                                        val calculatedW = hVal * (origW / origH)
                                                        inputWidth = if (unit == "px" || unit == "mm") calculatedW.toInt().toString()
                                                        else String.format(Locale.US, "%.1f", calculatedW)
                                                    }
                                                }
                                            },
                                            label = { Text(if (isBn) "উচ্চতা ($unit)" else "Height ($unit)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { lockAspectRatio = !lockAspectRatio }
                                    ) {
                                        Checkbox(
                                            checked = lockAspectRatio,
                                            onCheckedChange = { lockAspectRatio = it },
                                            colors = CheckboxDefaults.colors(checkedColor = themeColors.buttonEqualBg)
                                        )
                                        Text(
                                            text = if (isBn) "অ্যাসপেক্ট রেশিও লক রাখুন (Lock Aspect Ratio)" else "Lock Aspect Ratio",
                                            fontSize = 11.sp,
                                            color = themeColors.displayText
                                        )
                                    }
                                }
                            }

                            2 -> {
                                // LIGHT & COLOR ADJUSTMENTS
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isBn) "আলো ও রঙ অ্যাডজাস্টমেন্ট:" else "Light & Color Adjustments:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.displayText
                                        )

                                        if (brightness != 0f || contrast != 0f || warmth != 0f || saturation != 0f) {
                                            TextButton(
                                                onClick = {
                                                    brightness = 0f
                                                    contrast = 0f
                                                    warmth = 0f
                                                    saturation = 0f
                                                },
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp), tint = themeColors.buttonEqualBg)
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(if (isBn) "রিসেট" else "Reset", fontSize = 10.5.sp, color = themeColors.buttonEqualBg)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Quick Color & Enhancement Presets
                                    Text(
                                        text = if (isBn) "কুইক ফিল্টার ও প্রিসেট:" else "Quick Filter Presets:",
                                        fontSize = 10.sp,
                                        color = themeColors.displayText.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val presets = listOf(
                                            Triple("স্বাভাবিক", 0f, Triple(0f, 0f, 0f)),
                                            Triple("স্বাক্ষর পরিষ্কার", 25f, Triple(65f, 0f, -100f)),
                                            Triple("পাসপোর্ট বুস্ট", 15f, Triple(20f, 5f, 12f)),
                                            Triple("ক্লিন বি&ডব্লিউ", 10f, Triple(35f, 0f, -100f)),
                                            Triple("উষ্ণ গোল্ডেন", 5f, Triple(10f, 35f, 15f)),
                                            Triple("শীতল স্টুডিও", 10f, Triple(15f, -30f, 5f))
                                        )

                                        presets.forEach { (name, pB, rest) ->
                                            val (pC, pW, pS) = rest
                                            val isCurrent = brightness == pB && contrast == pC && warmth == pW && saturation == pS

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isCurrent) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.06f))
                                                    .clickable {
                                                        brightness = pB
                                                        contrast = pC
                                                        warmth = pW
                                                        saturation = pS
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                            ) {
                                                Text(
                                                    text = name,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isCurrent) Color.White else themeColors.displayText
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Sliders Card
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = themeColors.displayText.copy(alpha = 0.03f)),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.08f))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            // 1. Brightness / Light
                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(imageVector = Icons.Default.Brightness6, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(15.dp))
                                                        Spacer(modifier = Modifier.width(5.dp))
                                                        Text(if (isBn) "উজ্জ্বলতা (Brightness / Light)" else "Brightness", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = themeColors.displayText)
                                                    }
                                                    Text(
                                                        text = "${if (brightness > 0) "+" else ""}${brightness.toInt()}%",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = themeColors.buttonEqualBg
                                                    )
                                                }
                                                Slider(
                                                    value = brightness,
                                                    onValueChange = { brightness = it },
                                                    valueRange = -100f..100f,
                                                    colors = SliderDefaults.colors(
                                                        thumbColor = themeColors.buttonEqualBg,
                                                        activeTrackColor = themeColors.buttonEqualBg
                                                    )
                                                )
                                            }

                                            // 2. Contrast
                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(imageVector = Icons.Default.Contrast, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(15.dp))
                                                        Spacer(modifier = Modifier.width(5.dp))
                                                        Text(if (isBn) "কনট্রাস্ট (Contrast)" else "Contrast", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = themeColors.displayText)
                                                    }
                                                    Text(
                                                        text = "${if (contrast > 0) "+" else ""}${contrast.toInt()}%",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = themeColors.buttonEqualBg
                                                    )
                                                }
                                                Slider(
                                                    value = contrast,
                                                    onValueChange = { contrast = it },
                                                    valueRange = -100f..100f,
                                                    colors = SliderDefaults.colors(
                                                        thumbColor = themeColors.buttonEqualBg,
                                                        activeTrackColor = themeColors.buttonEqualBg
                                                    )
                                                )
                                            }

                                            // 3. Warmth / Temperature
                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(imageVector = Icons.Default.WbSunny, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(15.dp))
                                                        Spacer(modifier = Modifier.width(5.dp))
                                                        Text(if (isBn) "ওয়ার্মনেস / তাপমাত্রা (Warmth)" else "Warmth / Temperature", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = themeColors.displayText)
                                                    }
                                                    Text(
                                                        text = when {
                                                            warmth > 5 -> "+${warmth.toInt()}% " + if (isBn) "(উষ্ণ)" else "(Warm)"
                                                            warmth < -5 -> "${warmth.toInt()}% " + if (isBn) "(শীতল)" else "(Cool)"
                                                            else -> "0%"
                                                        },
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = themeColors.buttonEqualBg
                                                    )
                                                }
                                                Slider(
                                                    value = warmth,
                                                    onValueChange = { warmth = it },
                                                    valueRange = -100f..100f,
                                                    colors = SliderDefaults.colors(
                                                        thumbColor = themeColors.buttonEqualBg,
                                                        activeTrackColor = themeColors.buttonEqualBg
                                                    )
                                                )
                                            }

                                            // 4. Saturation
                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(imageVector = Icons.Default.ColorLens, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(15.dp))
                                                        Spacer(modifier = Modifier.width(5.dp))
                                                        Text(if (isBn) "রঙিন ভাব (Saturation)" else "Saturation", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = themeColors.displayText)
                                                    }
                                                    Text(
                                                        text = when {
                                                            saturation <= -98f -> if (isBn) "সাদা-কালো (B&W)" else "B&W"
                                                            saturation > 0 -> "+${saturation.toInt()}%"
                                                            else -> "${saturation.toInt()}%"
                                                        },
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = themeColors.buttonEqualBg
                                                    )
                                                }
                                                Slider(
                                                    value = saturation,
                                                    onValueChange = { saturation = it },
                                                    valueRange = -100f..100f,
                                                    colors = SliderDefaults.colors(
                                                        thumbColor = themeColors.buttonEqualBg,
                                                        activeTrackColor = themeColors.buttonEqualBg
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            3 -> {
                                // FILE SIZE & JOB PORTAL BOOSTER
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = if (isBn) "টার্গেট সাইজ মোড (Target File Size):" else "Select Target File Size:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.displayText
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    val kbModes = listOf(
                                        "Original",
                                        "Job Photo (30-100 KB)",
                                        "Job Signature (10-50 KB)",
                                        "< 50 KB",
                                        "< 100 KB",
                                        "Custom KB"
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        kbModes.forEach { mode ->
                                            val isSel = targetKbMode == mode
                                            FilterChip(
                                                selected = isSel,
                                                onClick = { targetKbMode = mode },
                                                label = { Text(mode, fontSize = 10.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = themeColors.buttonEqualBg,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }
                                    }

                                    if (targetKbMode == "Custom KB") {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = customTargetKb,
                                            onValueChange = { customTargetKb = it },
                                            label = { Text(if (isBn) "কাস্টম ফাইল সাইজ (KB)" else "Custom Target Size (KB)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Special Job Application Booster Card (< 30 KB Fix)
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.08f)),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.25f))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = "🚀 " + if (isBn) "চাকরির আবেদনের সাইজ বুস্টার" else "Job Portal Size Booster",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = themeColors.buttonEqualBg
                                                        )
                                                    }
                                                    Text(
                                                        text = if (isBn)
                                                            "ছবি ছোট (যেমন ৫ কেবি) হলেও চাকরির পোর্টালে কমপক্ষে ৩০ কেবি পূরণের জন্য স্বয়ংক্রিয়ভাবে সাইজ বাড়াবে"
                                                        else
                                                            "Boosts low KB images (e.g. 5 KB) to at least 30 KB+ for BD job portal criteria",
                                                        fontSize = 10.sp,
                                                        color = themeColors.displayText.copy(alpha = 0.7f)
                                                    )
                                                }
                                                Switch(
                                                    checked = boostMinKbEnabled,
                                                    onCheckedChange = { boostMinKbEnabled = it },
                                                    colors = SwitchDefaults.colors(checkedThumbColor = themeColors.buttonEqualBg)
                                                )
                                            }

                                            if (boostMinKbEnabled) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        text = if (isBn) "ন্যূনতম টার্গেট (KB):" else "Min Target (KB):",
                                                        fontSize = 11.sp,
                                                        color = themeColors.displayText,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    OutlinedTextField(
                                                        value = minKbValue,
                                                        onValueChange = { minKbValue = it },
                                                        modifier = Modifier.width(100.dp),
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        singleLine = true
                                                    )
                                                    Text(
                                                        text = if (isBn) "(প্রস্তাবিত: ৩৫ KB)" else "(Recommended: 35 KB)",
                                                        fontSize = 10.sp,
                                                        color = themeColors.displayText.copy(alpha = 0.5f)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (targetKbMode == "Original") {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "${if (isBn) "ম্যানুয়াল কোয়ালিটি" else "Quality"}: ${manualQuality.toInt()}%",
                                            fontSize = 11.sp,
                                            color = themeColors.displayText
                                        )
                                        Slider(
                                            value = manualQuality,
                                            onValueChange = { manualQuality = it },
                                            valueRange = 10f..100f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = themeColors.buttonEqualBg,
                                                activeTrackColor = themeColors.buttonEqualBg
                                            )
                                        )
                                    }
                                }
                            }

                            4 -> {
                                // FORMAT CONVERSION
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = if (isBn) "আউটপুট ফটো ফরম্যাট নির্বাচন:" else "Output Photo Format:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.displayText
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        val formats = listOf("JPG", "PNG", "WEBP")
                                        formats.forEach { fmt ->
                                            val isSel = targetFormat == fmt
                                            Card(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { targetFormat = fmt },
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSel) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.05f)
                                                ),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 12.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(
                                                            text = fmt,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSel) Color.White else themeColors.displayText
                                                        )
                                                        Text(
                                                            text = when (fmt) {
                                                                "JPG" -> "Best for Portal"
                                                                "PNG" -> "Lossless HD"
                                                                else -> "Web Compact"
                                                            },
                                                            fontSize = 9.sp,
                                                            color = if (isSel) Color.White.copy(alpha = 0.8f) else themeColors.displayText.copy(alpha = 0.5f)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Final Save / Export Button
                        Button(
                            onClick = {
                                if (processedResult != null && !isSaving) {
                                    isSaving = true
                                    coroutineScope.launch {
                                        val savedItem = saveProcessedPhotoToGallery(context, processedResult)
                                        isSaving = false
                                        if (savedItem != null) {
                                            refreshHistory()
                                            // IMMEDIATELY OPEN POST-SAVE PREVIEW DIALOG!
                                            postSavePhotoItem = savedItem
                                            Toast.makeText(
                                                context,
                                                if (isBn) "গ্যালারিতে সফলভাবে সেভ হয়েছে!" else "Saved successfully!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                if (isBn) "সেভ করতে সমস্যা হয়েছে" else "Failed to save photo",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            },
                            enabled = processedResult != null && !isSaving,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = themeColors.buttonEqualBg,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isBn) "সেভ করা হচ্ছে..." else "Saving...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (isBn) "ফাইনাল ফটো গ্যালারিতে সেভ করুন" else "Save Processed Photo to Gallery",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                // ==================== SAVED HISTORY TAB ====================
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "রিসাইজ করা ছবির হিস্টোরি" else "Saved Photos History",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        TextButton(onClick = { refreshHistory() }) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(15.dp), tint = themeColors.buttonEqualBg)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBn) "রিফ্রেশ" else "Refresh", fontSize = 11.sp, color = themeColors.buttonEqualBg)
                        }
                    }

                    if (savedHistoryList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(themeColors.displayText.copy(alpha = 0.03f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = themeColors.displayText.copy(alpha = 0.35f),
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isBn) "কোনো সেভ করা ছবি নেই" else "No saved photos found",
                                    fontSize = 12.sp,
                                    color = themeColors.displayText.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = if (isBn) "(মেমরি থেকে ডিলিট হয়ে গেলে এখানেও স্বয়ংক্রিয়ভাবে সরে যাবে)"
                                    else "(Photos deleted from device memory are automatically removed)",
                                    fontSize = 9.5.sp,
                                    color = themeColors.displayText.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { mainNavTab = 0 },
                                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(if (isBn) "ফটো রিসাইজ করুন" else "Go to Resizer", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            savedHistoryList.forEach { item ->
                                HistoryPhotoCardItem(
                                    item = item,
                                    isBn = isBn,
                                    themeColors = themeColors,
                                    onEdit = {
                                        // Load this photo into the editor workspace
                                        val uri = if (item.uriString.isNotEmpty()) {
                                            Uri.parse(item.uriString)
                                        } else if (!item.filePath.isNullOrEmpty()) {
                                            Uri.fromFile(File(item.filePath))
                                        } else null

                                        if (uri != null) {
                                            selectedImageUri = uri
                                            customCroppedBitmap = null
                                            inputWidth = item.width.toString()
                                            inputHeight = item.height.toString()
                                            targetFormat = item.format
                                            mainNavTab = 0
                                            Toast.makeText(
                                                context,
                                                if (isBn) "ছবিটি এডিটরে ওপেন করা হয়েছে" else "Loaded into Editor",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    onRename = { renamingTargetItem = item },
                                    onShare = { PhotoResizerHistoryManager.sharePhoto(context, item) },
                                    onDelete = { deletingTargetItem = item }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Interactive Crop Dialog ---
    if (showInteractiveCropDialog && (customCroppedBitmap != null || originalBitmap != null)) {
        val baseBmp = customCroppedBitmap ?: originalBitmap!!
        val transformedBmpForCrop = remember(baseBmp, rotationDegrees, isFlippedHorizontal) {
            if (rotationDegrees == 0f && !isFlippedHorizontal) baseBmp
            else {
                val matrix = Matrix().apply {
                    if (rotationDegrees != 0f) postRotate(rotationDegrees)
                    if (isFlippedHorizontal) postScale(-1f, 1f, baseBmp.width / 2f, baseBmp.height / 2f)
                }
                try {
                    Bitmap.createBitmap(baseBmp, 0, 0, baseBmp.width, baseBmp.height, matrix, true)
                } catch (e: Exception) {
                    baseBmp
                }
            }
        }

        InteractiveCropDialog(
            originalBitmap = transformedBmpForCrop,
            isBn = isBn,
            themeColors = themeColors,
            onDismiss = { showInteractiveCropDialog = false },
            onCropApplied = { croppedBmp ->
                customCroppedBitmap = croppedBmp
                rotationDegrees = 0f
                isFlippedHorizontal = false
                showInteractiveCropDialog = false
                inputWidth = croppedBmp.width.toString()
                inputHeight = croppedBmp.height.toString()
                cropPreset = "Original"
            }
        )
    }

    // --- POST-SAVE IMMEDIATE PREVIEW DIALOG ---
    postSavePhotoItem?.let { item ->
        PostSavePreviewDialog(
            item = item,
            isBn = isBn,
            themeColors = themeColors,
            onDismiss = { postSavePhotoItem = null },
            onRename = { renamingTargetItem = item },
            onShare = { PhotoResizerHistoryManager.sharePhoto(context, item) },
            onEdit = {
                val uri = if (item.uriString.isNotEmpty()) {
                    Uri.parse(item.uriString)
                } else if (!item.filePath.isNullOrEmpty()) {
                    Uri.fromFile(File(item.filePath))
                } else null

                if (uri != null) {
                    selectedImageUri = uri
                    customCroppedBitmap = null
                    inputWidth = item.width.toString()
                    inputHeight = item.height.toString()
                    targetFormat = item.format
                    mainNavTab = 0
                }
                postSavePhotoItem = null
            },
            onDelete = { deletingTargetItem = item }
        )
    }

    // --- RENAME DIALOG ---
    renamingTargetItem?.let { item ->
        var newNameInput by remember { mutableStateOf(item.fileName.substringBeforeLast('.')) }
        val ext = item.fileName.substringAfterLast('.', "")

        AlertDialog(
            onDismissRequest = { renamingTargetItem = null },
            title = {
                Text(
                    text = if (isBn) "ছবির নাম পরিবর্তন (Rename)" else "Rename Photo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isBn) "নতুন ফাইল নাম লিখুন:" else "Enter new file name:",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newNameInput,
                        onValueChange = { newNameInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            if (ext.isNotEmpty()) {
                                Text(".$ext", fontSize = 11.sp, color = themeColors.displayText.copy(alpha = 0.6f), modifier = Modifier.padding(end = 8.dp))
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = PhotoResizerHistoryManager.renamePhotoOnDevice(context, item, newNameInput)
                        if (updated != null) {
                            refreshHistory()
                            if (postSavePhotoItem?.id == item.id) {
                                postSavePhotoItem = updated
                            }
                            Toast.makeText(context, if (isBn) "রিনেম সম্পন্ন হয়েছে!" else "Renamed successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, if (isBn) "রিনেম করা সম্ভব হয়নি" else "Failed to rename", Toast.LENGTH_SHORT).show()
                        }
                        renamingTargetItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
                ) {
                    Text(if (isBn) "সংরক্ষণ" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { renamingTargetItem = null }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // --- DELETE CONFIRMATION DIALOG ---
    deletingTargetItem?.let { item ->
        AlertDialog(
            onDismissRequest = { deletingTargetItem = null },
            icon = {
                Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(32.dp))
            },
            title = {
                Text(
                    text = if (isBn) "ছবি ডিলিট নিশ্চিতকরণ" else "Confirm Delete",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                Text(
                    text = if (isBn)
                        "আপনি কি নিশ্চিতভাবে এই ছবিটি (${item.fileName}) ডিভাইস মেমরি থেকে ডিলিট করতে চান? ডিলিট করলে এটি স্থায়ীভাবে মুছে যাবে।"
                    else
                        "Are you sure you want to permanently delete '${item.fileName}' from device storage?",
                    fontSize = 12.5.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val deleted = PhotoResizerHistoryManager.deletePhotoFromDevice(context, item)
                        refreshHistory()
                        if (postSavePhotoItem?.id == item.id) {
                            postSavePhotoItem = null
                        }
                        deletingTargetItem = null
                        Toast.makeText(
                            context,
                            if (isBn) "ছবিটি ডিভাইস থেকে ডিলিট হয়েছে" else "Photo deleted from device",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text(if (isBn) "হ্যাঁ, ডিলিট করুন" else "Yes, Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingTargetItem = null }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

// --- History Item Card Composable ---

@Composable
fun HistoryPhotoCardItem(
    item: SavedPhotoItem,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onEdit: () -> Unit,
    onRename: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(item.timestamp) {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        sdf.format(Date(item.timestamp))
    }
    val sizeKbStr = remember(item.sizeBytes) {
        String.format(Locale.US, "%.1f KB", item.sizeBytes / 1024f)
    }

    val imageModel = remember(item) {
        if (item.uriString.isNotEmpty()) Uri.parse(item.uriString)
        else if (!item.filePath.isNullOrEmpty()) File(item.filePath)
        else null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = themeColors.displayText.copy(alpha = 0.04f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail Image
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = item.fileName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Metadata Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.fileName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${item.width}×${item.height} px • $sizeKbStr",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColors.buttonEqualBg
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(themeColors.displayText.copy(alpha = 0.08f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(item.format, fontSize = 8.5.sp, color = themeColors.displayText)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateStr,
                    fontSize = 9.sp,
                    color = themeColors.displayText.copy(alpha = 0.45f)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Action Buttons: Edit, Rename, Share, Delete
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = themeColors.buttonEqualBg, modifier = Modifier.size(17.dp))
                }
                IconButton(onClick = onRename, modifier = Modifier.size(30.dp)) {
                    Icon(imageVector = Icons.Default.DriveFileRenameOutline, contentDescription = "Rename", tint = themeColors.displayText.copy(alpha = 0.7f), modifier = Modifier.size(17.dp))
                }
                IconButton(onClick = onShare, modifier = Modifier.size(30.dp)) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = themeColors.buttonEqualBg, modifier = Modifier.size(17.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(17.dp))
                }
            }
        }
    }
}

// --- POST-SAVE PREVIEW DIALOG ---

@Composable
fun PostSavePreviewDialog(
    item: SavedPhotoItem,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val imageModel = remember(item) {
        if (item.uriString.isNotEmpty()) Uri.parse(item.uriString)
        else if (!item.filePath.isNullOrEmpty()) File(item.filePath)
        else null
    }

    val sizeKbStr = remember(item.sizeBytes) {
        String.format(Locale.US, "%.1f KB", item.sizeBytes / 1024f)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "সফলভাবে সেভ হয়েছে!" else "Saved Successfully!",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = themeColors.displayText.copy(alpha = 0.6f))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // High Quality Photo Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = item.fileName,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Info Details Grid
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = themeColors.displayText.copy(alpha = 0.04f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = item.fileName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${if (isBn) "সাইজ" else "Size"}: $sizeKbStr",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = themeColors.buttonEqualBg
                            )
                            Text(
                                text = "${if (isBn) "রেজোলিউশন" else "Dimensions"}: ${item.width}×${item.height} px",
                                fontSize = 11.sp,
                                color = themeColors.displayText.copy(alpha = 0.7f)
                            )
                            Text(
                                text = item.format,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "📁 " + if (isBn) "ফোল্ডার: Pictures/PhotoResizer" else "Folder: Pictures/PhotoResizer",
                            fontSize = 9.5.sp,
                            color = themeColors.displayText.copy(alpha = 0.45f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons Row: Rename, Share, Edit, Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Rename
                    OutlinedButton(
                        onClick = onRename,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DriveFileRenameOutline, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBn) "রিনেম" else "Rename", fontSize = 10.5.sp)
                    }

                    // Share
                    Button(
                        onClick = onShare,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBn) "শেয়ার" else "Share", fontSize = 10.5.sp, color = Color.White)
                    }

                    // Edit
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBn) "এডিট" else "Edit", fontSize = 10.5.sp)
                    }

                    // Delete
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBn) "ডিলিট" else "Delete", fontSize = 10.5.sp, color = Color(0xFFEF4444))
                    }
                }
            }
        }
    }
}

// --- Interactive Zoomable Crop Dialog ---

@Composable
private fun InteractiveCropDialog(
    originalBitmap: Bitmap,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit,
    onCropApplied: (Bitmap) -> Unit
) {
    var selectedRatio by remember { mutableStateOf("Free") }
    var customRatioW by remember { mutableStateOf("300") }
    var customRatioH by remember { mutableStateOf("80") }

    var imageScale by remember { mutableFloatStateOf(1f) }
    var imageOffset by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Crop,
                        contentDescription = null,
                        tint = themeColors.buttonEqualBg,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "প্রফেশনাল ক্রপ ও রিসাইজ টুল" else "Professional Crop & Resize Tool",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Ratio selection row
                Column(modifier = Modifier.background(Color(0xFF0F172A))) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val ratios = listOf("Free", "Custom", "1:1", "3:4", "4:3", "16:9", "300x300", "300x80")
                        ratios.forEach { r ->
                            val isSel = selectedRatio == r
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedRatio = r },
                                label = { Text(r, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = themeColors.buttonEqualBg,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White.copy(alpha = 0.12f),
                                    labelColor = Color.White
                                )
                            )
                        }
                    }

                    if (selectedRatio == "Custom") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(if (isBn) "ম্যানুয়াল সাইজ:" else "Manual Size:", color = Color.White, fontSize = 12.sp)

                            OutlinedTextField(
                                value = customRatioW,
                                onValueChange = { customRatioW = it },
                                modifier = Modifier.width(80.dp).height(48.dp),
                                textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = themeColors.buttonEqualBg,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                                ),
                                placeholder = { Text("W", fontSize = 10.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )

                            Text("×", color = Color.White)

                            OutlinedTextField(
                                value = customRatioH,
                                onValueChange = { customRatioH = it },
                                modifier = Modifier.width(80.dp).height(48.dp),
                                textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = themeColors.buttonEqualBg,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                                ),
                                placeholder = { Text("H", fontSize = 10.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }
                    }
                }

                // Interactive Crop Canvas Area
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clipToBounds()
                        .background(Color(0xFF090D16))
                ) {
                    val containerWidth = constraints.maxWidth.toFloat()
                    val containerHeight = constraints.maxHeight.toFloat()

                    val bmpW = originalBitmap.width.toFloat()
                    val bmpH = originalBitmap.height.toFloat()
                    val bmpRatio = bmpW / bmpH

                    val imageWidthOnScreen: Float
                    val imageHeightOnScreen: Float
                    if (containerWidth / containerHeight > bmpRatio) {
                        imageHeightOnScreen = containerHeight * 0.82f
                        imageWidthOnScreen = imageHeightOnScreen * bmpRatio
                    } else {
                        imageWidthOnScreen = containerWidth * 0.82f
                        imageHeightOnScreen = imageWidthOnScreen / bmpRatio
                    }

                    val imageLeft = (containerWidth - imageWidthOnScreen) / 2f
                    val imageTop = (containerHeight - imageHeightOnScreen) / 2f
                    val imageRight = imageLeft + imageWidthOnScreen
                    val imageBottom = imageTop + imageHeightOnScreen

                    val ratio: Float? = remember(selectedRatio, customRatioW, customRatioH) {
                        when (selectedRatio) {
                            "1:1", "300x300" -> 1f
                            "3:4" -> 3f / 4f
                            "4:3" -> 4f / 3f
                            "16:9" -> 16f / 9f
                            "300x80" -> 300f / 80f
                            "Custom" -> {
                                val w = customRatioW.toFloatOrNull() ?: 1f
                                val h = customRatioH.toFloatOrNull() ?: 1f
                                if (h > 0) w / h else null
                            }
                            else -> null
                        }
                    }

                    var cropRect by remember {
                        mutableStateOf(
                            Rect(
                                left = imageLeft + imageWidthOnScreen * 0.1f,
                                top = imageTop + imageHeightOnScreen * 0.1f,
                                right = imageRight - imageWidthOnScreen * 0.1f,
                                bottom = imageBottom - imageHeightOnScreen * 0.1f
                            )
                        )
                    }

                    LaunchedEffect(ratio, imageLeft, imageTop, imageWidthOnScreen, imageHeightOnScreen) {
                        if (ratio != null) {
                            val targetW = imageWidthOnScreen * 0.75f
                            val targetH = targetW / ratio
                            if (targetH <= imageHeightOnScreen * 0.85f) {
                                val l = imageLeft + (imageWidthOnScreen - targetW) / 2f
                                val t = imageTop + (imageHeightOnScreen - targetH) / 2f
                                cropRect = Rect(l, t, l + targetW, t + targetH)
                            } else {
                                val altH = imageHeightOnScreen * 0.75f
                                val altW = altH * ratio
                                val l = imageLeft + (imageWidthOnScreen - altW) / 2f
                                val t = imageTop + (imageHeightOnScreen - altH) / 2f
                                cropRect = Rect(l, t, l + altW, t + altH)
                            }
                        }
                    }

                    var activeHandle by remember { mutableStateOf<String?>(null) }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    val newScale = (imageScale * zoom).coerceIn(0.5f, 4f)
                                    imageScale = newScale
                                    imageOffset += pan
                                }
                            }
                            .pointerInput(imageLeft, imageTop, imageWidthOnScreen, imageHeightOnScreen, ratio) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        val handleRadius = 50f
                                        val tl = cropRect.topLeft
                                        val tr = cropRect.topRight
                                        val bl = cropRect.bottomLeft
                                        val br = cropRect.bottomRight

                                        activeHandle = when {
                                            (offset - tl).getDistance() <= handleRadius -> "TL"
                                            (offset - tr).getDistance() <= handleRadius -> "TR"
                                            (offset - bl).getDistance() <= handleRadius -> "BL"
                                            (offset - br).getDistance() <= handleRadius -> "BR"
                                            cropRect.contains(offset) -> "CENTER"
                                            else -> null
                                        }
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        val handle = activeHandle ?: return@detectDragGestures
                                        val minSize = 60f

                                        var newLeft = cropRect.left
                                        var newTop = cropRect.top
                                        var newRight = cropRect.right
                                        var newBottom = cropRect.bottom

                                        if (handle == "CENTER") {
                                            val dx = dragAmount.x
                                            val dy = dragAmount.y
                                            val width = cropRect.width
                                            val height = cropRect.height

                                            newLeft = (cropRect.left + dx).coerceIn(imageLeft, imageRight - width)
                                            newTop = (cropRect.top + dy).coerceIn(imageTop, imageBottom - height)
                                            newRight = newLeft + width
                                            newBottom = newTop + height
                                        } else {
                                            if (ratio == null) {
                                                when (handle) {
                                                    "TL" -> {
                                                        newLeft = (cropRect.left + dragAmount.x).coerceIn(imageLeft, cropRect.right - minSize)
                                                        newTop = (cropRect.top + dragAmount.y).coerceIn(imageTop, cropRect.bottom - minSize)
                                                    }
                                                    "TR" -> {
                                                        newRight = (cropRect.right + dragAmount.x).coerceIn(cropRect.left + minSize, imageRight)
                                                        newTop = (cropRect.top + dragAmount.y).coerceIn(imageTop, cropRect.bottom - minSize)
                                                    }
                                                    "BL" -> {
                                                        newLeft = (cropRect.left + dragAmount.x).coerceIn(imageLeft, cropRect.right - minSize)
                                                        newBottom = (cropRect.bottom + dragAmount.y).coerceIn(cropRect.top + minSize, imageBottom)
                                                    }
                                                    "BR" -> {
                                                        newRight = (cropRect.right + dragAmount.x).coerceIn(cropRect.left + minSize, imageRight)
                                                        newBottom = (cropRect.bottom + dragAmount.y).coerceIn(cropRect.top + minSize, imageBottom)
                                                    }
                                                }
                                            } else {
                                                when (handle) {
                                                    "BR" -> {
                                                        val proposedRight = (cropRect.right + dragAmount.x).coerceIn(cropRect.left + minSize, imageRight)
                                                        val newW = proposedRight - cropRect.left
                                                        val newH = newW / ratio
                                                        if (cropRect.top + newH <= imageBottom) {
                                                            newRight = proposedRight
                                                            newBottom = cropRect.top + newH
                                                        } else {
                                                            val maxH = imageBottom - cropRect.top
                                                            val maxW = maxH * ratio
                                                            newBottom = imageBottom
                                                            newRight = cropRect.left + maxW
                                                        }
                                                    }
                                                    "TL" -> {
                                                        val proposedLeft = (cropRect.left + dragAmount.x).coerceIn(imageLeft, cropRect.right - minSize)
                                                        val newW = cropRect.right - proposedLeft
                                                        val newH = newW / ratio
                                                        if (cropRect.bottom - newH >= imageTop) {
                                                            newLeft = proposedLeft
                                                            newTop = cropRect.bottom - newH
                                                        } else {
                                                            val maxH = cropRect.bottom - imageTop
                                                            val maxW = maxH * ratio
                                                            newTop = imageTop
                                                            newLeft = cropRect.right - maxW
                                                        }
                                                    }
                                                    "TR" -> {
                                                        val proposedRight = (cropRect.right + dragAmount.x).coerceIn(cropRect.left + minSize, imageRight)
                                                        val newW = proposedRight - cropRect.left
                                                        val newH = newW / ratio
                                                        if (cropRect.bottom - newH >= imageTop) {
                                                            newRight = proposedRight
                                                            newTop = cropRect.bottom - newH
                                                        } else {
                                                            val maxH = cropRect.bottom - imageTop
                                                            val maxW = maxH * ratio
                                                            newTop = imageTop
                                                            newRight = cropRect.left + maxW
                                                        }
                                                    }
                                                    "BL" -> {
                                                        val proposedLeft = (cropRect.left + dragAmount.x).coerceIn(imageLeft, cropRect.right - minSize)
                                                        val newW = cropRect.right - proposedLeft
                                                        val newH = newW / ratio
                                                        if (cropRect.top + newH <= imageBottom) {
                                                            newLeft = proposedLeft
                                                            newBottom = cropRect.top + newH
                                                        } else {
                                                            val maxH = imageBottom - cropRect.top
                                                            val maxW = maxH * ratio
                                                            newBottom = imageBottom
                                                            newLeft = cropRect.right - maxW
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        cropRect = Rect(newLeft, newTop, newRight, newBottom)
                                    },
                                    onDragEnd = { activeHandle = null }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = originalBitmap.asImageBitmap(),
                            contentDescription = "Crop target",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = imageScale
                                    scaleY = imageScale
                                    translationX = imageOffset.x
                                    translationY = imageOffset.y
                                }
                        )

                        // Dark overlay and crop box
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val path = androidx.compose.ui.graphics.Path().apply {
                                fillType = androidx.compose.ui.graphics.PathFillType.EvenOdd
                                addRect(Rect(0f, 0f, size.width, size.height))
                                addRect(cropRect)
                            }
                            drawPath(path, Color.Black.copy(alpha = 0.65f))

                            // Crop border
                            drawRect(
                                color = Color.White,
                                topLeft = cropRect.topLeft,
                                size = cropRect.size,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                            )

                            // Grid lines (rule of thirds)
                            val oneThirdW = cropRect.width / 3f
                            val oneThirdH = cropRect.height / 3f
                            drawLine(
                                color = Color.White.copy(alpha = 0.5f),
                                start = Offset(cropRect.left + oneThirdW, cropRect.top),
                                end = Offset(cropRect.left + oneThirdW, cropRect.bottom),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.5f),
                                start = Offset(cropRect.left + oneThirdW * 2f, cropRect.top),
                                end = Offset(cropRect.left + oneThirdW * 2f, cropRect.bottom),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.5f),
                                start = Offset(cropRect.left, cropRect.top + oneThirdH),
                                end = Offset(cropRect.right, cropRect.top + oneThirdH),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.5f),
                                start = Offset(cropRect.left, cropRect.top + oneThirdH * 2f),
                                end = Offset(cropRect.right, cropRect.top + oneThirdH * 2f),
                                strokeWidth = 1.dp.toPx()
                            )

                            // Corner Handles
                            val handleRadius = 9.dp.toPx()
                            drawCircle(Color.White, handleRadius, cropRect.topLeft)
                            drawCircle(Color.White, handleRadius, cropRect.topRight)
                            drawCircle(Color.White, handleRadius, cropRect.bottomLeft)
                            drawCircle(Color.White, handleRadius, cropRect.bottomRight)
                        }
                    }

                    // Bottom Bar with Reset & Apply
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A).copy(alpha = 0.95f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                selectedRatio = "Free"
                                imageScale = 1f
                                imageOffset = Offset.Zero
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBn) "রিসেট" else "Reset", color = Color.White, fontSize = 12.sp)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = onDismiss,
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                            ) {
                                Text(if (isBn) "বাতিল" else "Cancel", color = Color.White, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    try {
                                        val centerX = containerWidth / 2f + imageOffset.x
                                        val centerY = containerHeight / 2f + imageOffset.y
                                        val curWidth = imageWidthOnScreen * imageScale
                                        val curHeight = imageHeightOnScreen * imageScale
                                        val curLeft = centerX - curWidth / 2f
                                        val curTop = centerY - curHeight / 2f

                                        val relativeLeft = (cropRect.left - curLeft) / curWidth
                                        val relativeTop = (cropRect.top - curTop) / curHeight
                                        val relativeWidth = cropRect.width / curWidth
                                        val relativeHeight = cropRect.height / curHeight

                                        val origW = originalBitmap.width
                                        val origH = originalBitmap.height

                                        val startX = (relativeLeft * origW).toInt().coerceIn(0, origW - 1)
                                        val startY = (relativeTop * origH).toInt().coerceIn(0, origH - 1)
                                        val cropWidth = (relativeWidth * origW).toInt().coerceIn(1, origW - startX)
                                        val cropHeight = (relativeHeight * origH).toInt().coerceIn(1, origH - startY)

                                        val croppedBmp = Bitmap.createBitmap(originalBitmap, startX, startY, cropWidth, cropHeight)
                                        onCropApplied(croppedBmp)
                                    } catch (e: Exception) {
                                        onCropApplied(originalBitmap)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg, contentColor = Color.White)
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isBn) "ক্রপ সম্পন্ন করুন" else "Apply Crop", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
