package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CalculatorThemeColors
import java.util.Locale

@Composable
fun ColorWheelPickerDialog(
    title: String,
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    // Convert initial color to HSV
    val initialHsv = floatArrayOf(0f, 1f, 1f)
    android.graphics.Color.colorToHSV(initialColor.toArgb(), initialHsv)

    var hue by remember { mutableStateOf(initialHsv[0]) }
    var saturation by remember { mutableStateOf(initialHsv[1]) }
    var value by remember { mutableStateOf(initialHsv[2]) }

    val currentColor = remember(hue, saturation, value) {
        Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value)))
    }

    // Curated professional color palette swatches
    val presetColors = listOf(
        Color(0xFFEF4444), Color(0xFFF97316), Color(0xFFFBBF24), Color(0xFF84CC16),
        Color(0xFF10B981), Color(0xFF06B6D4), Color(0xFF3B82F6), Color(0xFF6366F1),
        Color(0xFF8B5CF6), Color(0xFFD946EF), Color(0xFFEC4899), Color(0xFFF43F5E),
        Color(0xFF0F172A), Color(0xFF334155), Color(0xFF64748B), Color(0xFF94A3B8),
        Color(0xFFCBD5E1), Color(0xFFF1F5F9), Color(0xFFFFFFFF), Color(0xFF000000)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Large Dynamic Live Preview Ring & Hex Code
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(currentColor)
                        .border(3.dp, themeColors.displayText.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val argb = currentColor.toArgb()
                    val r = android.graphics.Color.red(argb)
                    val g = android.graphics.Color.green(argb)
                    val b = android.graphics.Color.blue(argb)
                    val luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
                    val textColor = if (luminance > 0.5) Color.Black else Color.White
                    
                    Text(
                        text = String.format(Locale.US, "#%02X%02X%02X", r, g, b),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Rainbow Hue Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBn) "রঙের বর্ণালী (Hue Spectrum)" else "Hue Spectrum",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = themeColors.displayText.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${hue.toInt()}°",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Rainbow Track Box with Slider overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Red, Color.Yellow, Color.Green,
                                        Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Slider(
                            value = hue,
                            onValueChange = { hue = it },
                            valueRange = 0f..360f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color.Transparent,
                                inactiveTrackColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Brightness Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBn) "উজ্জ্বলতা (Brightness)" else "Brightness",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = themeColors.displayText.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${(value * 100).toInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                    }
                    Slider(
                        value = value,
                        onValueChange = { value = it },
                        valueRange = 0.1f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = themeColors.buttonEqualBg,
                            activeTrackColor = themeColors.buttonEqualBg.copy(alpha = 0.7f),
                            inactiveTrackColor = themeColors.displayText.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Presets Palette Grid (5 columns per row)
                Text(
                    text = if (isBn) "প্রিসেট রঙ প্যালেট (Color Swatches)" else "Preset Swatches",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.displayText.copy(alpha = 0.8f),
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
                )

                val presetRows = presetColors.chunked(5)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetRows.forEach { rowColors ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowColors.forEach { color ->
                                val isSelected = currentColor.toArgb() == color.toArgb()
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            val hsv = floatArrayOf(0f, 1f, 1f)
                                            android.graphics.Color.colorToHSV(color.toArgb(), hsv)
                                            hue = hsv[0]
                                            saturation = hsv[1]
                                            value = hsv[2]
                                        }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = if (isBn) "বাতিল" else "Cancel",
                            color = themeColors.displayText.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            onColorSelected(currentColor)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isBn) "প্রয়োগ করুন" else "Apply Color",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

