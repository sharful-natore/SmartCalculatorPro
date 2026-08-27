package com.example.ui.quran

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors

@Composable
fun DownloadOptionsDialog(
    themeColors: CalculatorThemeColors,
    cyanPrimary: Color,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isFullQuran: Boolean = false,
    downloadedType: String? = null
) {
    val isArabicDownloaded = downloadedType == "ARABIC" || downloadedType == "BOTH"
    val isBanglaDownloaded = downloadedType == "BANGLA" || downloadedType == "BOTH"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = themeColors.cardBg,
        titleContentColor = themeColors.displayText,
        textContentColor = themeColors.displayText,
        title = {
            Text(
                text = if (isFullQuran) "পুরো কুরআন ডাউনলোড" else "সূরা ডাউনলোড করুন",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "কোন ধরনের অডিও ফাইল ডাউনলোড করতে চান?",
                    fontSize = 14.sp,
                    color = themeColors.displayText.copy(alpha = 0.8f)
                )
                
                if (downloadedType != "BOTH") {
                    DownloadOptionItem(
                        title = "আরবি + বাংলা অনুবাদ",
                        subtitle = "উভয় অডিও ফাইল ডাউনলোড হবে",
                        icon = Icons.Default.Language,
                        themeColors = themeColors,
                        cyanPrimary = cyanPrimary,
                        isDownloaded = false,
                        onClick = { onConfirm("BOTH") }
                    )
                }
                
                DownloadOptionItem(
                    title = "শুধুমাত্র আরবি",
                    subtitle = "আয়াত সমূহের আরবি তেলাওয়াত",
                    icon = Icons.Default.AudioFile,
                    themeColors = themeColors,
                    cyanPrimary = cyanPrimary,
                    isDownloaded = isArabicDownloaded,
                    onClick = { if (!isArabicDownloaded) onConfirm("ARABIC") }
                )
                
                DownloadOptionItem(
                    title = "শুধুমাত্র বাংলা",
                    subtitle = "আয়াত সমূহের বাংলা অনুবাদ",
                    icon = Icons.Default.Translate,
                    themeColors = themeColors,
                    cyanPrimary = cyanPrimary,
                    isDownloaded = isBanglaDownloaded,
                    onClick = { if (!isBanglaDownloaded) onConfirm("BANGLA") }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল করুন", color = cyanPrimary)
            }
        }
    )
}

@Composable
fun DownloadOptionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    themeColors: CalculatorThemeColors,
    cyanPrimary: Color,
    isDownloaded: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isDownloaded) Color(0xFF10B981).copy(alpha = 0.1f) else cyanPrimary.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, if (isDownloaded) Color(0xFF10B981).copy(alpha = 0.3f) else cyanPrimary.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isDownloaded) Icons.Default.CheckCircle else icon,
                contentDescription = null,
                tint = if (isDownloaded) Color(0xFF10B981) else cyanPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = themeColors.displayText)
                Text(text = if (isDownloaded) "ডাউনলোড সম্পন্ন" else subtitle, fontSize = 11.sp, color = themeColors.displayText.copy(alpha = 0.7f))
            }
        }
    }
}
