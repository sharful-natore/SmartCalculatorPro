package com.example.ui.quran

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.quran.SurahEntity
import com.example.ui.theme.CalculatorThemeColors

@Composable
fun SurahDownloadConfirmDialog(
    surah: SurahEntity,
    viewModel: QuranViewModel,
    themeColors: CalculatorThemeColors,
    cyanPrimary: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val estimatedSize = viewModel.getEstimatedSurahSize(surah)
    val revTypeBn = if (surah.revelationType.lowercase().contains("meccan")) "মাক্কী" else "মাদানী"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = themeColors.cardBg,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, cyanPrimary.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(cyanPrimary, cyanPrimary.copy(alpha = 0.7f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "সূরা অডিও ডাউনলোড",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )

                Text(
                    text = "সূরা ${surah.nameBangla} (${surah.nameEnglish}) - ${surah.nameArabic}",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = cyanPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Detail Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cyanPrimary.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, cyanPrimary.copy(alpha = 0.18f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        InfoRow(
                            icon = Icons.Default.Person,
                            label = "তেলাওয়াতকারী:",
                            value = "মিশারি রশিদ আল-আফাসি (Alafasy)",
                            tint = cyanPrimary,
                            themeColors = themeColors
                        )

                        InfoRow(
                            icon = Icons.Default.MenuBook,
                            label = "সূরা ও আয়াত:",
                            value = "সূরা নং ${surah.number} • ${surah.numberOfAyahs}টি আয়াত ($revTypeBn)",
                            tint = cyanPrimary,
                            themeColors = themeColors
                        )

                        InfoRow(
                            icon = Icons.Default.Audiotrack,
                            label = "অডিও ফরম্যাট:",
                            value = "128 kbps MP3 ক্লিয়ার সাউন্ড",
                            tint = cyanPrimary,
                            themeColors = themeColors
                        )

                        InfoRow(
                            icon = Icons.Default.Storage,
                            label = "আনুমানিক সাইজ:",
                            value = "~$estimatedSize",
                            tint = cyanPrimary,
                            themeColors = themeColors
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Note Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(themeColors.displayText.copy(alpha = 0.06f))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = cyanPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ডাউনলোড সম্পন্ন হলে ইন্টারনেট ছাড়াই সম্পূর্ণ সূরা শুনতে পারবেন।",
                        fontSize = 11.sp,
                        color = themeColors.displayText.copy(alpha = 0.75f),
                        lineHeight = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = "বাতিল",
                            color = themeColors.displayText.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onConfirm()
                        },
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = cyanPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ডাউনলোড করুন",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FullQuranDownloadConfirmDialog(
    themeColors: CalculatorThemeColors,
    cyanPrimary: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = themeColors.cardBg,
            shadowElevation = 10.dp,
            border = BorderStroke(1.5.dp, cyanPrimary.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(cyanPrimary, Color(0xFF0097A7))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "সম্পূর্ণ কুরআন অডিও ডাউনলোড",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )

                Text(
                    text = "পবিত্র কুরআনের ১১৪টি পূর্ণাঙ্গ সূরা",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = cyanPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Detail Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cyanPrimary.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, cyanPrimary.copy(alpha = 0.18f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        InfoRow(
                            icon = Icons.Default.Person,
                            label = "তেলাওয়াতকারী:",
                            value = "মিশারি রশিদ আল-আফাসি (Alafasy)",
                            tint = cyanPrimary,
                            themeColors = themeColors
                        )

                        InfoRow(
                            icon = Icons.Default.MenuBook,
                            label = "মোট সূরা ও আয়াত:",
                            value = "১১৪টি সূরা • ৬,২৩৬টি আয়াত",
                            tint = cyanPrimary,
                            themeColors = themeColors
                        )

                        InfoRow(
                            icon = Icons.Default.Audiotrack,
                            label = "অডিও কোয়ালিটি:",
                            value = "128 kbps MP3 ক্লিয়ার সাউন্ড",
                            tint = cyanPrimary,
                            themeColors = themeColors
                        )

                        InfoRow(
                            icon = Icons.Default.Storage,
                            label = "মোট প্যাকেজ সাইজ:",
                            value = "~৬৮৫ MB (আনুমানিক)",
                            tint = cyanPrimary,
                            themeColors = themeColors
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Note Badge (Wi-Fi & Pause recommendation)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFEF3C7).copy(alpha = 0.7f))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "প্যাকেজ সাইজ বড় হওয়ায় Wi-Fi ব্যবহারের পরামর্শ দেওয়া হচ্ছে। ডাউনলোড চলাকালীন যেকোনো সময় পজ বা ক্যানসেল করতে পারবেন।",
                        fontSize = 11.sp,
                        color = Color(0xFF92400E),
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = "বাতিল",
                            color = themeColors.displayText.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onConfirm()
                        },
                        modifier = Modifier.weight(1.4f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = cyanPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "সব ডাউনলোড শুরু",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color,
    themeColors: CalculatorThemeColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = themeColors.displayText.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.displayText,
            modifier = Modifier.weight(1f)
        )
    }
}
