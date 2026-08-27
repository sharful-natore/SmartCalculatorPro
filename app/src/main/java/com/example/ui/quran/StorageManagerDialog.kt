@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
package com.example.ui.quran

import android.content.Context
import android.text.format.Formatter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.CalculatorThemeColors

@Composable
fun StorageManagerDialog(
    viewModel: QuranViewModel,
    themeColors: CalculatorThemeColors,
    cyanPrimary: Color,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val downloadedSurahs by viewModel.downloadedSurahs.collectAsStateWithLifecycle()
    val storageBytes by viewModel.storageSizeBytes.collectAsStateWithLifecycle()

    val formattedSize = Formatter.formatShortFileSize(context, storageBytes)
    
    var selectedSurahs by remember { mutableStateOf(setOf<Int>()) }
    val isSelectionMode = selectedSurahs.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = themeColors.cardBg,
        titleContentColor = themeColors.displayText,
        textContentColor = themeColors.displayText,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                if (isSelectionMode) {
                    Text(
                        text = "${selectedSurahs.size} টি সিলেক্টেড",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        selectedSurahs.forEach { num ->
                            viewModel.deleteSurahAudio(context, num, "ALL")
                        }
                        selectedSurahs = setOf()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Selected", tint = Color.Red)
                    }
                } else {
                    Icon(Icons.Default.SdCard, contentDescription = null, tint = cyanPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("স্টোরেজ ম্যানেজার", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText, modifier = Modifier.weight(1f))
                    
                    if (downloadedSurahs.isNotEmpty()) {
                        IconButton(onClick = {
                            downloadedSurahs.forEach { surah ->
                                viewModel.deleteSurahAudio(context, surah.number, "ALL")
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete All", tint = Color.Red.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = cyanPrimary.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "মোট ডাউনলোডকৃত অডিও:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = themeColors.displayText
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = formattedSize,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = cyanPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (downloadedSurahs.isEmpty()) {
                    Text(
                        text = "বর্তমানে কোনো অফলাইন অডিও ডিরেক্টরি ডাউনলোড করা নেই।",
                        fontSize = 12.5.sp,
                        color = themeColors.displayText.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                } else {
                    Text(
                        text = "ডাউনলোডকৃত সূরাসমূহ (${downloadedSurahs.size} টি) (লং প্রেস করে সিলেক্ট করুন):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = themeColors.displayText.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        downloadedSurahs.forEach { surah ->
                            val isSelected = selectedSurahs.contains(surah.number)
                            Surface(
                                color = if (isSelected) cyanPrimary.copy(alpha = 0.15f) else Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            if (isSelectionMode) {
                                                selectedSurahs = if (isSelected) selectedSurahs - surah.number else selectedSurahs + surah.number
                                            }
                                        },
                                        onLongClick = {
                                            selectedSurahs = if (isSelected) selectedSurahs - surah.number else selectedSurahs + surah.number
                                        }
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSelectionMode) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                            contentDescription = "Select",
                                            tint = if (isSelected) cyanPrimary else themeColors.displayText.copy(alpha = 0.5f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${surah.number}. ${surah.nameBangla} (${surah.nameEnglish})",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = themeColors.displayText
                                        )
                                        val typeText = when (surah.downloadedType) {
                                            "ARABIC" -> "শুধুমাত্র আরবি"
                                            "BANGLA" -> "শুধুমাত্র বাংলা"
                                            "BOTH" -> "আরবি + বাংলা"
                                            else -> "অফলাইন প্রস্তুত"
                                        }
                                        Text(
                                            text = "${surah.numberOfAyahs} টি আয়াত • $typeText",
                                            fontSize = 11.sp,
                                            color = Color(0xFF10B981)
                                        )
                                    }

                                    if (!isSelectionMode) {
                                        var showDeleteOptions by remember { mutableStateOf(false) }
                                        
                                        IconButton(
                                            onClick = {
                                                viewModel.playSurah(surah)
                                                onDismiss()
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayCircle,
                                                contentDescription = "Play",
                                                tint = cyanPrimary
                                            )
                                        }
                                        Box {
                                            IconButton(onClick = { showDeleteOptions = true }) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete Options",
                                                    tint = Color.Red.copy(alpha = 0.8f)
                                                )
                                            }
                                            DropdownMenu(
                                                expanded = showDeleteOptions,
                                                onDismissRequest = { showDeleteOptions = false },
                                                containerColor = themeColors.cardBg
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("সব ডিলিট করুন", color = themeColors.displayText) },
                                                    onClick = {
                                                        viewModel.deleteSurahAudio(context, surah.number, "ALL")
                                                        showDeleteOptions = false
                                                    }
                                                )
                                                if (surah.downloadedType == "BOTH" || surah.downloadedType == "ARABIC") {
                                                    DropdownMenuItem(
                                                        text = { Text("শুধু আরবি ডিলিট করুন", color = themeColors.displayText) },
                                                        onClick = {
                                                            viewModel.deleteSurahAudio(context, surah.number, "ARABIC")
                                                            showDeleteOptions = false
                                                        }
                                                    )
                                                }
                                                if (surah.downloadedType == "BOTH" || surah.downloadedType == "BANGLA") {
                                                    DropdownMenuItem(
                                                        text = { Text("শুধু বাংলা ডিলিট করুন", color = themeColors.displayText) },
                                                        onClick = {
                                                            viewModel.deleteSurahAudio(context, surah.number, "BANGLA")
                                                            showDeleteOptions = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.1f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন", color = cyanPrimary, fontWeight = FontWeight.Bold)
            }
        }
    )
}
