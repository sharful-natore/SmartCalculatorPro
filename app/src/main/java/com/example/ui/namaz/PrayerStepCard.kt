package com.example.ui.namaz

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val primaryCyan = themeColors.buttonEqualBg
    val femaleAccentColor = Color(0xFFEC4899)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Step Number, Title & Posture Graphic Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Step Number Badge
                    Surface(
                        shape = CircleShape,
                        color = if (isFemaleMode && step.femaleNoteBn != null) femaleAccentColor else primaryCyan,
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

                // Posture Icon Illustrator Canvas
                PostureIllustrationGraphic(
                    postureType = step.postureType,
                    isFemaleMode = isFemaleMode,
                    color = if (isFemaleMode) femaleAccentColor else primaryCyan
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step Description
            Text(
                text = step.descriptionBn,
                fontSize = 13.5.sp,
                color = themeColors.displayText.copy(alpha = 0.9f),
                lineHeight = 20.sp
            )

            // Gender Difference Badge & Instruction Note
            if (step.maleNoteBn != null || step.femaleNoteBn != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isFemaleMode) femaleAccentColor.copy(alpha = 0.12f) else primaryCyan.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isFemaleMode) femaleAccentColor.copy(alpha = 0.4f) else primaryCyan.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isFemaleMode) Icons.Default.Female else Icons.Default.Male,
                                contentDescription = null,
                                tint = if (isFemaleMode) femaleAccentColor else primaryCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isFemaleMode) "নারী ও পুরুষের নিয়মগত পার্থক্য (নারী ভার্সন):" else "নারী ও পুরুষের নিয়মগত পার্থক্য (পুরুষ ভার্সন):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFemaleMode) femaleAccentColor else primaryCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isFemaleMode) (step.femaleNoteBn ?: step.maleNoteBn ?: "") else (step.maleNoteBn ?: step.femaleNoteBn ?: ""),
                            fontSize = 12.5.sp,
                            color = themeColors.displayText.copy(alpha = 0.9f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Arabic Text Container with Tashkeel Styling & Audio Action
            if (!step.arabicText.isNull_or_empty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (themeColors.isDark) Color(0xFF0F172A) else Color(0xFFECFDF5)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        primaryCyan.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Top Action Row (Audio & Copy)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = primaryCyan.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "القرآن / الأذكار",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (onAudioClick != null) {
                                    IconButton(
                                        onClick = onAudioClick,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                            contentDescription = "Play Audio",
                                            tint = primaryCyan,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        copyToClipboard(context, step.arabicText ?: "")
                                    },
                                    modifier = Modifier.size(32.dp)
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

                        Spacer(modifier = Modifier.height(8.dp))

                        // Arabic Text
                        Text(
                            text = step.arabicText ?: "",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (themeColors.isDark) Color(0xFF6EE7B7) else Color(0xFF047857),
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Serif,
                            lineHeight = 36.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Bangla Pronunciation
                        if (!step.banglaPronunciation.isNull_or_empty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "উচ্চারণ: ${step.banglaPronunciation}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = themeColors.displayText.copy(alpha = 0.85f),
                                lineHeight = 19.sp
                            )
                        }

                        // Bangla Meaning
                        if (!step.banglaMeaning.isNull_or_empty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "অর্থ: ${step.banglaMeaning}",
                                fontSize = 12.5.sp,
                                color = themeColors.displayText.copy(alpha = 0.75f),
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
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(28.dp)) {
            val w = size.width
            val h = size.height
            val strokeWidth = 2.5f

            when (postureType) {
                PostureType.QIYAM, PostureType.NIYYAT -> {
                    // Standing Person Vector
                    drawCircle(color, radius = w * 0.15f, center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.2f))
                    drawLine(color, start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.35f), end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.7f), strokeWidth = strokeWidth)
                    drawLine(color, start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.7f), end = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.95f), strokeWidth = strokeWidth)
                    drawLine(color, start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.7f), end = androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.95f), strokeWidth = strokeWidth)
                }
                PostureType.TAKBEER -> {
                    // Raised Hands Person
                    drawCircle(color, radius = w * 0.15f, center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.2f))
                    drawLine(color, start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.35f), end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.7f), strokeWidth = strokeWidth)
                    // Raised Arms
                    val armPath = Path().apply {
                        moveTo(w * 0.2f, h * 0.25f)
                        lineTo(w * 0.35f, h * 0.4f)
                        lineTo(w * 0.5f, h * 0.4f)
                        lineTo(w * 0.65f, h * 0.4f)
                        lineTo(w * 0.8f, h * 0.25f)
                    }
                    drawPath(armPath, color, style = Stroke(width = strokeWidth))
                }
                PostureType.RUKU -> {
                    // Bowed Ruku Vector
                    drawCircle(color, radius = w * 0.14f, center = androidx.compose.ui.geometry.Offset(w * 0.3f, h * 0.35f))
                    // Back
                    drawLine(color, start = androidx.compose.ui.geometry.Offset(w * 0.3f, h * 0.45f), end = androidx.compose.ui.geometry.Offset(w * 0.7f, h * 0.45f), strokeWidth = strokeWidth)
                    // Legs
                    drawLine(color, start = androidx.compose.ui.geometry.Offset(w * 0.7f, h * 0.45f), end = androidx.compose.ui.geometry.Offset(w * 0.7f, h * 0.9f), strokeWidth = strokeWidth)
                }
                PostureType.SUJUD -> {
                    // Prostrating Sujud Vector
                    drawCircle(color, radius = w * 0.12f, center = androidx.compose.ui.geometry.Offset(w * 0.2f, h * 0.75f))
                    val bodyPath = Path().apply {
                        moveTo(w * 0.2f, h * 0.75f)
                        lineTo(w * 0.5f, h * 0.55f)
                        lineTo(w * 0.8f, h * 0.85f)
                    }
                    drawPath(bodyPath, color, style = Stroke(width = strokeWidth))
                }
                PostureType.QAUMA, PostureType.JALSA, PostureType.TASHAHHUD -> {
                    // Sitting Person
                    drawCircle(color, radius = w * 0.14f, center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.3f))
                    val sitPath = Path().apply {
                        moveTo(w * 0.5f, h * 0.45f)
                        lineTo(w * 0.5f, h * 0.75f)
                        lineTo(w * 0.8f, h * 0.75f)
                    }
                    drawPath(sitPath, color, style = Stroke(width = strokeWidth))
                }
                PostureType.SALAM -> {
                    // Turn Head Person
                    drawCircle(color, radius = w * 0.15f, center = androidx.compose.ui.geometry.Offset(w * 0.6f, h * 0.2f))
                    drawLine(color, start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.35f), end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.85f), strokeWidth = strokeWidth)
                }
                PostureType.WUDU_GENERIC, PostureType.DUA_GENERIC -> {
                    // Water Drop / Hands Dua Vector
                    drawCircle(color, radius = w * 0.2f, center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.5f), style = Stroke(width = strokeWidth))
                }
            }
        }
    }
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

private fun String?.isNull_or_empty(): Boolean = this == null || this.isEmpty()
