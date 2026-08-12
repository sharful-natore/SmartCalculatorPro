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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CalculatorThemeColors
import com.example.util.AppLanguage
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
    var r by remember { mutableStateOf((initialColor.red * 255f).toInt()) }
    var g by remember { mutableStateOf((initialColor.green * 255f).toInt()) }
    var b by remember { mutableStateOf((initialColor.blue * 255f).toInt()) }
    
    // Quick presets (color wheel slices)
    val presets = listOf(
        Color(0xFFEF4444), // Red
        Color(0xFFF97316), // Orange
        Color(0xFFFBBF24), // Yellow
        Color(0xFF34D399), // Green
        Color(0xFF10B981), // Emerald
        Color(0xFF06B6D4), // Cyan
        Color(0xFF3B82F6), // Blue
        Color(0xFF6366F1), // Indigo
        Color(0xFF8B5CF6), // Purple
        Color(0xFFD946EF), // Fuchsia
        Color(0xFFEC4899), // Pink
        Color(0xFFFFFFFF), // White
        Color(0xFF1E293B), // Dark Blue Gray
        Color(0xFF000000)  // Black
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.cardBg)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Large Dynamic Live Preview Ring
                val currentColor = Color(r, g, b)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(currentColor)
                        .border(4.dp, themeColors.displayText.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
                    val textColor = if (luminance > 0.5) Color.Black else Color.White
                    Text(
                        text = String.format(Locale.US, "#%02X%02X%02X", r, g, b),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Color Wheel Presets Grid
                Text(
                    text = if (isBn) "রঙের চাকা প্যালেট (Wheel Palette)" else "Color Wheel Presets",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.displayText.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
                )
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { color ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (Color(r, g, b) == color) 3.dp else 1.dp,
                                    color = if (Color(r, g, b) == color) themeColors.buttonEqualBg else Color.Gray.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    r = (color.red * 255f).toInt()
                                    g = (color.green * 255f).toInt()
                                    b = (color.blue * 255f).toInt()
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // RGB Precision Sliders
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Red Slider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("R", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.width(18.dp))
                        Slider(
                            value = r.toFloat(),
                            onValueChange = { r = it.toInt() },
                            valueRange = 0f..255f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.Red,
                                activeTrackColor = Color.Red.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text(r.toString(), color = themeColors.displayText, fontSize = 12.sp, modifier = Modifier.width(28.dp), textAlign = TextAlign.End)
                    }

                    // Green Slider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("G", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.width(18.dp))
                        Slider(
                            value = g.toFloat(),
                            onValueChange = { g = it.toInt() },
                            valueRange = 0f..255f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF10B981),
                                activeTrackColor = Color(0xFF10B981).copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text(g.toString(), color = themeColors.displayText, fontSize = 12.sp, modifier = Modifier.width(28.dp), textAlign = TextAlign.End)
                    }

                    // Blue Slider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("B", color = Color.Blue, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.width(18.dp))
                        Slider(
                            value = b.toFloat(),
                            onValueChange = { b = it.toInt() },
                            valueRange = 0f..255f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.Blue,
                                activeTrackColor = Color.Blue.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text(b.toString(), color = themeColors.displayText, fontSize = 12.sp, modifier = Modifier.width(28.dp), textAlign = TextAlign.End)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText.copy(alpha = 0.6f))
                    }

                    Button(
                        onClick = {
                            onColorSelected(Color(r, g, b))
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
                    ) {
                        Text(if (isBn) "ঠিক আছে" else "Apply", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val layoutWidth = constraints.maxWidth
        
        var currentX = 0
        var currentY = 0
        var rowHeight = 0
        
        val placements = mutableListOf<Pair<androidx.compose.ui.layout.Placeable, Pair<Int, Int>>>()
        
        placeables.forEach { placeable ->
            if (currentX + placeable.width > layoutWidth) {
                currentX = 0
                currentY += rowHeight + verticalArrangement.spacing.roundToPx()
                rowHeight = 0
            }
            placements.add(placeable to (currentX to currentY))
            currentX += placeable.width + horizontalArrangement.spacing.roundToPx()
            rowHeight = maxOf(rowHeight, placeable.height)
        }
        
        val finalHeight = currentY + rowHeight
        layout(layoutWidth, finalHeight) {
            placements.forEach { (placeable, pos) ->
                placeable.place(pos.first, pos.second)
            }
        }
    }
}
