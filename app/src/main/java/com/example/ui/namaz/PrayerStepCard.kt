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

                // Posture Icon Graphic Canvas
                PostureIllustrationGraphic(
                    postureType = step.postureType,
                    isFemaleMode = isFemaleMode,
                    color = activeAccent
                )
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
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(54.dp)) {
            val w = size.width
            val h = size.height

            // Realistic Color Palette
            val matColor = Color(0xFF0F766E)
            val skinTone = Color(0xFFE5C09A)
            val maleRobeColor = Color(0xFFF8FAFC)
            val maleRobeShadow = Color(0xFFE2E8F0)
            val femaleRobeColor = if (color == Color(0xFFEC4899)) Color(0xFFFCE7F3) else Color(0xFFF1F5F9)
            val femaleHijabColor = Color(0xFFBE185D)
            val capColor = Color(0xFF0284C7)
            val hairColor = Color(0xFF334155)

            val garmentColor = if (isFemaleMode) femaleRobeColor else maleRobeColor
            val garmentShadow = if (isFemaleMode) Color(0xFFFBCFE8) else maleRobeShadow

            // 1. Draw Prayer Mat Base Line at Bottom
            drawLine(
                color = matColor,
                start = Offset(w * 0.05f, h * 0.94f),
                end = Offset(w * 0.95f, h * 0.94f),
                strokeWidth = 3.5f
            )

            if (isFemaleMode) {
                // FEMALE ILLUSTRATIONS - FLAT FACE SILHOUETTE (NO VISIBLE FACIAL FEATURES)
                when (postureType) {
                    PostureType.QIYAM, PostureType.NIYYAT -> {
                        // Standing in full modest abaya, hands folded over chest
                        val abayaPath = Path().apply {
                            moveTo(w * 0.35f, h * 0.34f)
                            lineTo(w * 0.65f, h * 0.34f)
                            lineTo(w * 0.78f, h * 0.92f)
                            lineTo(w * 0.22f, h * 0.92f)
                            close()
                        }
                        drawPath(abayaPath, garmentColor)
                        drawPath(abayaPath, garmentShadow, style = Stroke(width = 2f))

                        // Hands folded at chest
                        drawCircle(skinTone, radius = w * 0.065f, center = Offset(w * 0.5f, h * 0.46f))

                        // Full Hijab/Khimar
                        drawCircle(femaleHijabColor, radius = w * 0.16f, center = Offset(w * 0.5f, h * 0.22f))
                        // Flat face oval (smooth skin tone, no facial features)
                        drawCircle(skinTone, radius = w * 0.09f, center = Offset(w * 0.5f, h * 0.23f))
                    }

                    PostureType.TAKBEER -> {
                        // Takbeer: Hands raised up to chest/shoulder level inside shawl/abaya
                        val abayaPath = Path().apply {
                            moveTo(w * 0.32f, h * 0.36f)
                            lineTo(w * 0.68f, h * 0.36f)
                            lineTo(w * 0.78f, h * 0.92f)
                            lineTo(w * 0.22f, h * 0.92f)
                            close()
                        }
                        drawPath(abayaPath, garmentColor)
                        drawPath(abayaPath, garmentShadow, style = Stroke(width = 2f))

                        // Hands raised to chest/shoulder level
                        drawCircle(skinTone, radius = w * 0.05f, center = Offset(w * 0.35f, h * 0.36f))
                        drawCircle(skinTone, radius = w * 0.05f, center = Offset(w * 0.65f, h * 0.36f))

                        // Hijab & Flat face
                        drawCircle(femaleHijabColor, radius = w * 0.16f, center = Offset(w * 0.5f, h * 0.22f))
                        drawCircle(skinTone, radius = w * 0.09f, center = Offset(w * 0.5f, h * 0.23f))
                    }

                    PostureType.RUKU -> {
                        // Compact Ruku (slightly bowed, arms kept close)
                        val torsoPath = Path().apply {
                            moveTo(w * 0.3f, h * 0.45f)
                            lineTo(w * 0.68f, h * 0.52f)
                            lineTo(w * 0.68f, h * 0.92f)
                            lineTo(w * 0.48f, h * 0.92f)
                            lineTo(w * 0.48f, h * 0.68f)
                            lineTo(w * 0.3f, h * 0.62f)
                            close()
                        }
                        drawPath(torsoPath, garmentColor)
                        drawPath(torsoPath, garmentShadow, style = Stroke(width = 2f))

                        // Arms resting gently on knees
                        drawLine(skinTone, start = Offset(w * 0.42f, h * 0.52f), end = Offset(w * 0.58f, h * 0.68f), strokeWidth = 4.5f)
                        drawCircle(skinTone, radius = w * 0.045f, center = Offset(w * 0.58f, h * 0.68f))

                        // Hijab & Flat Face
                        drawCircle(femaleHijabColor, radius = w * 0.13f, center = Offset(w * 0.25f, h * 0.44f))
                        drawCircle(skinTone, radius = w * 0.08f, center = Offset(w * 0.25f, h * 0.45f))
                    }

                    PostureType.QAUMA -> {
                        // Standing straight in full abaya
                        val abayaPath = Path().apply {
                            moveTo(w * 0.35f, h * 0.34f)
                            lineTo(w * 0.65f, h * 0.34f)
                            lineTo(w * 0.76f, h * 0.92f)
                            lineTo(w * 0.24f, h * 0.92f)
                            close()
                        }
                        drawPath(abayaPath, garmentColor)
                        drawPath(abayaPath, garmentShadow, style = Stroke(width = 2f))

                        // Arms hanging naturally at sides
                        drawLine(skinTone, start = Offset(w * 0.32f, h * 0.4f), end = Offset(w * 0.32f, h * 0.62f), strokeWidth = 3.5f)
                        drawLine(skinTone, start = Offset(w * 0.68f, h * 0.4f), end = Offset(w * 0.68f, h * 0.62f), strokeWidth = 3.5f)

                        // Hijab & Flat Face
                        drawCircle(femaleHijabColor, radius = w * 0.16f, center = Offset(w * 0.5f, h * 0.22f))
                        drawCircle(skinTone, radius = w * 0.09f, center = Offset(w * 0.5f, h * 0.23f))
                    }

                    PostureType.SUJUD -> {
                        // Female Sujud: Compact, forearms/elbows flat on mat, body enclosed
                        val sujudPath = Path().apply {
                            moveTo(w * 0.16f, h * 0.88f)
                            lineTo(w * 0.38f, h * 0.60f)
                            lineTo(w * 0.78f, h * 0.72f)
                            lineTo(w * 0.88f, h * 0.88f)
                            lineTo(w * 0.38f, h * 0.88f)
                            close()
                        }
                        drawPath(sujudPath, garmentColor)
                        drawPath(sujudPath, garmentShadow, style = Stroke(width = 2f))

                        // Forearms flat on ground
                        drawLine(skinTone, start = Offset(w * 0.2f, h * 0.86f), end = Offset(w * 0.38f, h * 0.86f), strokeWidth = 3.5f)

                        // Hijab & Flat Face touching mat
                        drawCircle(femaleHijabColor, radius = w * 0.12f, center = Offset(w * 0.18f, h * 0.8f))
                        drawCircle(skinTone, radius = w * 0.07f, center = Offset(w * 0.16f, h * 0.83f))
                    }

                    PostureType.JALSA, PostureType.TASHAHHUD -> {
                        // Female Sitting (Taworruk): दोनों পা ডান দিকে
                        val sitPath = Path().apply {
                            moveTo(w * 0.42f, h * 0.38f)
                            lineTo(w * 0.62f, h * 0.38f)
                            lineTo(w * 0.84f, h * 0.88f)
                            lineTo(w * 0.28f, h * 0.88f)
                            close()
                        }
                        drawPath(sitPath, garmentColor)
                        drawPath(sitPath, garmentShadow, style = Stroke(width = 2f))

                        // Hands flat on knees
                        drawLine(skinTone, start = Offset(w * 0.45f, h * 0.52f), end = Offset(w * 0.65f, h * 0.68f), strokeWidth = 3.5f)
                        if (postureType == PostureType.TASHAHHUD) {
                            drawLine(femaleHijabColor, start = Offset(w * 0.65f, h * 0.68f), end = Offset(w * 0.72f, h * 0.62f), strokeWidth = 2.5f)
                        }

                        // Hijab & Flat Face
                        drawCircle(femaleHijabColor, radius = w * 0.15f, center = Offset(w * 0.52f, h * 0.22f))
                        drawCircle(skinTone, radius = w * 0.09f, center = Offset(w * 0.52f, h * 0.23f))
                    }

                    PostureType.SALAM -> {
                        val sitPath = Path().apply {
                            moveTo(w * 0.42f, h * 0.38f)
                            lineTo(w * 0.62f, h * 0.38f)
                            lineTo(w * 0.84f, h * 0.88f)
                            lineTo(w * 0.28f, h * 0.88f)
                            close()
                        }
                        drawPath(sitPath, garmentColor)
                        drawPath(sitPath, garmentShadow, style = Stroke(width = 2f))

                        // Head turned right, flat face
                        drawCircle(femaleHijabColor, radius = w * 0.15f, center = Offset(w * 0.66f, h * 0.22f))
                        drawCircle(skinTone, radius = w * 0.09f, center = Offset(w * 0.68f, h * 0.23f))
                    }

                    PostureType.WUDU_GENERIC, PostureType.DUA_GENERIC -> {
                        val abayaPath = Path().apply {
                            moveTo(w * 0.35f, h * 0.45f)
                            lineTo(w * 0.65f, h * 0.45f)
                            lineTo(w * 0.76f, h * 0.92f)
                            lineTo(w * 0.24f, h * 0.92f)
                            close()
                        }
                        drawPath(abayaPath, garmentColor)

                        // Open Palms
                        drawCircle(skinTone, radius = w * 0.065f, center = Offset(w * 0.38f, h * 0.46f))
                        drawCircle(skinTone, radius = w * 0.065f, center = Offset(w * 0.62f, h * 0.46f))

                        // Hijab & Flat Face
                        drawCircle(femaleHijabColor, radius = w * 0.15f, center = Offset(w * 0.5f, h * 0.24f))
                        drawCircle(skinTone, radius = w * 0.09f, center = Offset(w * 0.5f, h * 0.25f))
                    }
                }
            } else {
                // MALE ILLUSTRATIONS
                when (postureType) {
                    PostureType.QIYAM, PostureType.NIYYAT -> {
                        // Male Thobe & Cap
                        val thobePath = Path().apply {
                            moveTo(w * 0.38f, h * 0.35f)
                            lineTo(w * 0.62f, h * 0.35f)
                            lineTo(w * 0.7f, h * 0.92f)
                            lineTo(w * 0.3f, h * 0.92f)
                            close()
                        }
                        drawPath(thobePath, garmentColor)
                        drawPath(thobePath, garmentShadow, style = Stroke(width = 2f))

                        // Feet
                        drawCircle(skinTone, radius = w * 0.04f, center = Offset(w * 0.42f, h * 0.92f))
                        drawCircle(skinTone, radius = w * 0.04f, center = Offset(w * 0.58f, h * 0.92f))

                        // Folded Hands on Navel
                        drawCircle(skinTone, radius = w * 0.06f, center = Offset(w * 0.5f, h * 0.52f))

                        // Head, Beard & Cap
                        drawCircle(hairColor, radius = w * 0.14f, center = Offset(w * 0.5f, h * 0.21f))
                        drawCircle(skinTone, radius = w * 0.11f, center = Offset(w * 0.5f, h * 0.2f))
                        val capPath = Path().apply {
                            moveTo(w * 0.36f, h * 0.18f)
                            quadraticTo(w * 0.5f, h * 0.06f, w * 0.64f, h * 0.18f)
                            close()
                        }
                        drawPath(capPath, capColor)
                    }

                    PostureType.TAKBEER -> {
                        val thobePath = Path().apply {
                            moveTo(w * 0.38f, h * 0.38f)
                            lineTo(w * 0.62f, h * 0.38f)
                            lineTo(w * 0.7f, h * 0.92f)
                            lineTo(w * 0.3f, h * 0.92f)
                            close()
                        }
                        drawPath(thobePath, garmentColor)
                        drawPath(thobePath, garmentShadow, style = Stroke(width = 2f))

                        val leftArm = Path().apply {
                            moveTo(w * 0.38f, h * 0.42f)
                            lineTo(w * 0.22f, h * 0.25f)
                        }
                        val rightArm = Path().apply {
                            moveTo(w * 0.62f, h * 0.42f)
                            lineTo(w * 0.78f, h * 0.25f)
                        }
                        drawPath(leftArm, skinTone, style = Stroke(width = 4.5f))
                        drawPath(rightArm, skinTone, style = Stroke(width = 4.5f))

                        drawCircle(skinTone, radius = w * 0.05f, center = Offset(w * 0.2f, h * 0.22f))
                        drawCircle(skinTone, radius = w * 0.05f, center = Offset(w * 0.8f, h * 0.22f))

                        drawCircle(skinTone, radius = w * 0.11f, center = Offset(w * 0.5f, h * 0.22f))
                        val capPath = Path().apply {
                            moveTo(w * 0.36f, h * 0.2f)
                            quadraticTo(w * 0.5f, h * 0.08f, w * 0.64f, h * 0.2f)
                            close()
                        }
                        drawPath(capPath, capColor)
                    }

                    PostureType.RUKU -> {
                        val torsoPath = Path().apply {
                            moveTo(w * 0.25f, h * 0.45f)
                            lineTo(w * 0.7f, h * 0.45f)
                            lineTo(w * 0.7f, h * 0.92f)
                            lineTo(w * 0.55f, h * 0.92f)
                            lineTo(w * 0.55f, h * 0.62f)
                            lineTo(w * 0.25f, h * 0.6f)
                            close()
                        }
                        drawPath(torsoPath, garmentColor)
                        drawPath(torsoPath, garmentShadow, style = Stroke(width = 2f))

                        drawLine(skinTone, start = Offset(w * 0.4f, h * 0.52f), end = Offset(w * 0.62f, h * 0.72f), strokeWidth = 5f)
                        drawCircle(skinTone, radius = w * 0.045f, center = Offset(w * 0.62f, h * 0.72f))

                        drawCircle(skinTone, radius = w * 0.1f, center = Offset(w * 0.2f, h * 0.5f))
                        val capPath = Path().apply {
                            moveTo(w * 0.12f, h * 0.46f)
                            quadraticTo(w * 0.2f, h * 0.36f, w * 0.28f, h * 0.46f)
                            close()
                        }
                        drawPath(capPath, capColor)
                    }

                    PostureType.QAUMA -> {
                        val thobePath = Path().apply {
                            moveTo(w * 0.38f, h * 0.35f)
                            lineTo(w * 0.62f, h * 0.35f)
                            lineTo(w * 0.7f, h * 0.92f)
                            lineTo(w * 0.3f, h * 0.92f)
                            close()
                        }
                        drawPath(thobePath, garmentColor)
                        drawPath(thobePath, garmentShadow, style = Stroke(width = 2f))

                        drawLine(skinTone, start = Offset(w * 0.32f, h * 0.4f), end = Offset(w * 0.32f, h * 0.68f), strokeWidth = 4f)
                        drawLine(skinTone, start = Offset(w * 0.68f, h * 0.4f), end = Offset(w * 0.68f, h * 0.68f), strokeWidth = 4f)

                        drawCircle(skinTone, radius = w * 0.11f, center = Offset(w * 0.5f, h * 0.21f))
                        val capPath = Path().apply {
                            moveTo(w * 0.36f, h * 0.19f)
                            quadraticTo(w * 0.5f, h * 0.07f, w * 0.64f, h * 0.19f)
                            close()
                        }
                        drawPath(capPath, capColor)
                    }

                    PostureType.SUJUD -> {
                        val bodyPath = Path().apply {
                            moveTo(w * 0.18f, h * 0.88f)
                            lineTo(w * 0.42f, h * 0.52f)
                            lineTo(w * 0.78f, h * 0.68f)
                            lineTo(w * 0.88f, h * 0.88f)
                            lineTo(w * 0.42f, h * 0.88f)
                            close()
                        }
                        drawPath(bodyPath, garmentColor)
                        drawPath(bodyPath, garmentShadow, style = Stroke(width = 2f))

                        drawCircle(skinTone, radius = w * 0.09f, center = Offset(w * 0.16f, h * 0.82f))
                        val capPath = Path().apply {
                            moveTo(w * 0.1f, h * 0.78f)
                            quadraticTo(w * 0.18f, h * 0.68f, w * 0.26f, h * 0.78f)
                            close()
                        }
                        drawPath(capPath, capColor)

                        drawCircle(skinTone, radius = w * 0.04f, center = Offset(w * 0.28f, h * 0.88f))
                    }

                    PostureType.JALSA, PostureType.TASHAHHUD -> {
                        val sitPath = Path().apply {
                            moveTo(w * 0.45f, h * 0.38f)
                            lineTo(w * 0.62f, h * 0.38f)
                            lineTo(w * 0.82f, h * 0.88f)
                            lineTo(w * 0.32f, h * 0.88f)
                            close()
                        }
                        drawPath(sitPath, garmentColor)
                        drawPath(sitPath, garmentShadow, style = Stroke(width = 2f))

                        drawLine(skinTone, start = Offset(w * 0.48f, h * 0.5f), end = Offset(w * 0.65f, h * 0.68f), strokeWidth = 4f)
                        if (postureType == PostureType.TASHAHHUD) {
                            drawLine(capColor, start = Offset(w * 0.65f, h * 0.68f), end = Offset(w * 0.72f, h * 0.62f), strokeWidth = 3f)
                        }

                        drawCircle(skinTone, radius = w * 0.11f, center = Offset(w * 0.52f, h * 0.22f))
                        val capPath = Path().apply {
                            moveTo(w * 0.38f, h * 0.2f)
                            quadraticTo(w * 0.52f, h * 0.08f, w * 0.66f, h * 0.2f)
                            close()
                        }
                        drawPath(capPath, capColor)
                    }

                    PostureType.SALAM -> {
                        val sitPath = Path().apply {
                            moveTo(w * 0.45f, h * 0.38f)
                            lineTo(w * 0.62f, h * 0.38f)
                            lineTo(w * 0.82f, h * 0.88f)
                            lineTo(w * 0.32f, h * 0.88f)
                            close()
                        }
                        drawPath(sitPath, garmentColor)
                        drawPath(sitPath, garmentShadow, style = Stroke(width = 2f))

                        drawCircle(skinTone, radius = w * 0.11f, center = Offset(w * 0.68f, h * 0.22f))
                        val capPath = Path().apply {
                            moveTo(w * 0.55f, h * 0.2f)
                            quadraticTo(w * 0.68f, h * 0.08f, w * 0.8f, h * 0.2f)
                            close()
                        }
                        drawPath(capPath, capColor)
                    }

                    PostureType.WUDU_GENERIC, PostureType.DUA_GENERIC -> {
                        drawCircle(skinTone, radius = w * 0.07f, center = Offset(w * 0.38f, h * 0.42f))
                        drawCircle(skinTone, radius = w * 0.07f, center = Offset(w * 0.62f, h * 0.42f))

                        val leftArm = Path().apply {
                            moveTo(w * 0.32f, h * 0.68f)
                            lineTo(w * 0.38f, h * 0.42f)
                        }
                        val rightArm = Path().apply {
                            moveTo(w * 0.68f, h * 0.68f)
                            lineTo(w * 0.62f, h * 0.42f)
                        }
                        drawPath(leftArm, skinTone, style = Stroke(width = 5f))
                        drawPath(rightArm, skinTone, style = Stroke(width = 5f))

                        drawCircle(capColor.copy(alpha = 0.2f), radius = w * 0.25f, center = Offset(w * 0.5f, h * 0.38f))
                    }
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
