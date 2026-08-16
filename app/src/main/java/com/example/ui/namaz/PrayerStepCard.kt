package com.example.ui.namaz

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors

@Composable
fun PrayerStepCard(
    step: PrayerStep,
    isFemaleMode: Boolean,
    themeColors: CalculatorThemeColors,
    isPlaying: Boolean = false,
    onAudioClick: (() -> Unit)? = null,
    downloadProgress: Int? = null,
    onDownloadClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val primaryCyan = themeColors.buttonEqualBg
    val femaleAccentColor = Color(0xFFEC4899)
    val activeAccent = if (isFemaleMode) femaleAccentColor else primaryCyan

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Step Number, Title, Posture Graphic
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Step Number Badge (if > 0)
                    if (step.stepNumber > 0) {
                        Surface(
                            shape = CircleShape,
                            color = activeAccent,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${step.stepNumber}",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Column {
                        Text(
                            text = step.titleBn,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        if (step.titleEn.isNotEmpty()) {
                            Text(
                                text = step.titleEn,
                                fontSize = 11.5.sp,
                                color = themeColors.displayText.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            if (step.descriptionBn.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = step.descriptionBn,
                    fontSize = 13.5.sp,
                    color = themeColors.displayText.copy(alpha = 0.9f),
                    lineHeight = 20.sp
                )
            }

            // Gender Specific Guidance Box
            if (step.maleNoteBn != null || step.femaleNoteBn != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = activeAccent.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isFemaleMode) Icons.Default.Female else Icons.Default.Male,
                                contentDescription = null,
                                tint = activeAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isFemaleMode) "নারীদের আদায়ের বিশেষ পদ্ধতি:" else "পুরুষদের আদায়ের বিশেষ পদ্ধতি:",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = activeAccent
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isFemaleMode) (step.femaleNoteBn ?: step.maleNoteBn ?: "") else (step.maleNoteBn ?: step.femaleNoteBn ?: ""),
                            fontSize = 12.5.sp,
                            color = themeColors.displayText.copy(alpha = 0.95f),
                            lineHeight = 18.5.sp
                        )
                    }
                }
            }

            // Arabic Text & Pronunciation / Meaning Container
            if (!step.arabicText.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (themeColors.isDark) Color(0xFF0F172A) else Color(0xFFF0FDF4)
                    ),
                    border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Header Bar with Audio & Copy Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = activeAccent.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "القرآن والأذكار (আরবি)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = activeAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (onAudioClick != null) {
                                    IconButton(
                                        onClick = onAudioClick,
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.VolumeUp,
                                            contentDescription = "Play Audio",
                                            tint = activeAccent,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                if (onDownloadClick != null) {
                                    when {
                                        downloadProgress != null && downloadProgress in 1..99 -> {
                                            CircularProgressIndicator(
                                                progress = { downloadProgress / 100f },
                                                modifier = Modifier
                                                    .padding(horizontal = 6.dp)
                                                    .size(18.dp),
                                                strokeWidth = 2.dp,
                                                color = activeAccent
                                            )
                                        }
                                        downloadProgress == 100 -> {
                                            IconButton(
                                                onClick = {},
                                                enabled = false,
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Downloaded Offline",
                                                    tint = Color(0xFF10B981),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        else -> {
                                            IconButton(
                                                onClick = onDownloadClick,
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Download,
                                                    contentDescription = "Download Audio",
                                                    tint = themeColors.displayText.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        val combined = buildString {
                                            append(step.arabicText ?: "")
                                            if (!step.banglaPronunciation.isNullOrEmpty()) {
                                                append("\n\nউচ্চারণ: ${step.banglaPronunciation}")
                                            }
                                            if (!step.banglaMeaning.isNullOrEmpty()) {
                                                append("\n\nঅর্থ: ${step.banglaMeaning}")
                                            }
                                        }
                                        copyToClipboard(context, combined)
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Text",
                                        tint = themeColors.displayText.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Beautiful Arabic Tashkeel Text
                        Text(
                            text = step.arabicText ?: "",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (themeColors.isDark) Color(0xFF34D399) else Color(0xFF047857),
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Serif,
                            lineHeight = 36.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Bangla Pronunciation
                        if (!step.banglaPronunciation.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "উচ্চারণ: ${step.banglaPronunciation}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (themeColors.isDark) Color(0xFF60A5FA) else Color(0xFF1D4ED8),
                                lineHeight = 19.sp
                            )
                        }

                        // Bangla Meaning
                        if (!step.banglaMeaning.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "অর্থ: ${step.banglaMeaning}",
                                fontSize = 12.5.sp,
                                color = if (themeColors.isDark) Color(0xFFFBBF24) else Color(0xFFB45309),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostureIllustrationGraphic(
    postureType: PostureType,
    isFemaleMode: Boolean,
    color: Color
) {
    // Posture illustrations removed
}

private fun copyToClipboard(context: Context, text: String) {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Namaz Dua", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "টেক্সট কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
