package com.example.ui.screens

import com.example.util.LanguageManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Man
import androidx.compose.material.icons.filled.Woman
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BMICalculatorCard(viewModel: CalculatorViewModel, themeColors: CalculatorThemeColors) {
    val lang = viewModel.selectedLanguage
    val primaryAccent = themeColors.buttonEqualBg

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = themeColors.cardBg),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Age and Height
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Age Input
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(LanguageManager.getString("age_years", lang), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(36.dp)
                            .border(1.dp, themeColors.displayText.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BasicTextField(
                            value = viewModel.bmiAge,
                            onValueChange = { viewModel.bmiAge = it; viewModel.calculateBMI() },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 14.sp,
                                color = themeColors.displayText,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(primaryAccent),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Height Input
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(2f)) {
                    Text(LanguageManager.getString("height_cm", lang), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (viewModel.bmiHeightUnit == "ft/in") {
                            Box(
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(36.dp)
                                    .border(1.dp, themeColors.displayText.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                BasicTextField(
                                    value = viewModel.bmiHeightFt,
                                    onValueChange = { viewModel.bmiHeightFt = it; viewModel.calculateBMI() },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        fontSize = 14.sp,
                                        color = themeColors.displayText,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    cursorBrush = SolidColor(primaryAccent),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Text("'", fontSize = 16.sp, color = themeColors.displayText, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 2.dp))
                            Box(
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(36.dp)
                                    .border(1.dp, themeColors.displayText.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                BasicTextField(
                                    value = viewModel.bmiHeightIn,
                                    onValueChange = { viewModel.bmiHeightIn = it; viewModel.calculateBMI() },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        fontSize = 14.sp,
                                        color = themeColors.displayText,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    cursorBrush = SolidColor(primaryAccent),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Text("\"", fontSize = 16.sp, color = themeColors.displayText, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 2.dp))
                        } else {
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(36.dp)
                                    .border(1.dp, themeColors.displayText.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                BasicTextField(
                                    value = viewModel.bmiHeight,
                                    onValueChange = { viewModel.bmiHeight = it; viewModel.calculateBMI() },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        fontSize = 14.sp,
                                        color = themeColors.displayText,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    cursorBrush = SolidColor(primaryAccent),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Height Unit Dropdown
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                    .clickable { 
                        viewModel.bmiHeightUnit = if(viewModel.bmiHeightUnit == "ft/in") "cm" else "ft/in"
                        viewModel.calculateBMI()
                    }) {
                    Text(text = viewModel.bmiHeightUnit, color = primaryAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = themeColors.displayText.copy(alpha=0.5f))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Middle Row: Gender and Weight
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
                    Icon(
                        Icons.Default.Woman, 
                        contentDescription = "Female", 
                        tint = if (viewModel.bmiGender == "Female") primaryAccent else themeColors.displayText.copy(alpha = 0.3f),
                        modifier = Modifier.size(32.dp).clickable { viewModel.bmiGender = "Female"; viewModel.calculateBMI() }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.Man, 
                        contentDescription = "Male", 
                        tint = if (viewModel.bmiGender == "Male") primaryAccent else themeColors.displayText.copy(alpha = 0.3f),
                        modifier = Modifier.size(32.dp).clickable { viewModel.bmiGender = "Male"; viewModel.calculateBMI() }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(2f)) {
                    Text(LanguageManager.getString("weight_kg", lang), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(90.dp)
                            .height(36.dp)
                            .border(1.dp, themeColors.displayText.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BasicTextField(
                            value = viewModel.bmiWeight,
                            onValueChange = { viewModel.bmiWeight = it; viewModel.calculateBMI() },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 14.sp,
                                color = themeColors.displayText,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(primaryAccent),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // Weight Unit Dropdown
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                    .clickable { 
                        viewModel.toggleBmiWeightUnit()
                    }) {
                    Text(text = viewModel.bmiWeightUnit, color = primaryAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = themeColors.displayText.copy(alpha=0.5f))
                }
            }

            Spacer(modifier = Modifier
                .height(20.dp))

            // Gauge Chart
            val bmiValue = viewModel.bmiResultValue.toFloatOrNull() ?: 0f
            val currentBmiColorText = when {
                bmiValue < 18.5f -> Color(0xFF29B6F6) // Light Blue
                bmiValue < 25f -> Color(0xFF34C759)   // Vibrant Green
                else -> Color(0xFFE65100)              // Deep Orange
            }
            Box(modifier = Modifier
                .fillMaxWidth().height(140.dp), contentAlignment = Alignment.BottomCenter) {
                Canvas(modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val radius = size.width / 2 - strokeWidth / 2
                    val center = Offset(size.width / 2, size.height)
                    
                    // Draw Arcs
                    // Total range: 40 - 15 = 25 units
                    // Underweight: 15 to 18.5 (3.5 units) -> 14% of 180 deg = 25.2 deg
                    // Normal: 18.5 to 25 (6.5 units) -> 26% of 180 deg = 46.8 deg
                    // Overweight/Obese: 25 to 40 (15 units) -> 60% of 180 deg = 108 deg
                    val angles = listOf(
                        180f to 205.2f,    // Underweight (Light Blue)
                        205.2f to 252f,    // Normal (Vibrant Green)
                        252f to 360f       // Overweight (Deep Orange)
                    )
                    val colors = listOf(Color(0xFF29B6F6), Color(0xFF34C759), Color(0xFFE65100))
                    
                    for (i in angles.indices) {
                        drawArc(
                            color = colors[i],
                            startAngle = angles[i].first,
                            sweepAngle = angles[i].second - angles[i].first,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Butt
                            )
                        )
                    }

                    // Calculate Needle position (Range 15 to 40)
                    val normalizedBmi = (bmiValue - 15f).coerceIn(0f, 25f) / 25f
                    val needleAngle = 180f + (180f * normalizedBmi)
                    val needleAngleRad = Math.toRadians(needleAngle.toDouble())
                    
                    val innerRadius = radius - strokeWidth / 2
                    val needleEnd = Offset(
                        x = center.x + (innerRadius * cos(needleAngleRad)).toFloat(),
                        y = center.y + (innerRadius * sin(needleAngleRad)).toFloat()
                    )
                    
                    // Draw Needle Line
                    drawLine(
                        color = currentBmiColorText,
                        start = center,
                        end = needleEnd,
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Draw Pivot Circle
                    drawCircle(
                        color = currentBmiColorText,
                        radius = 8.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = center
                    )
                    
                    // Draw Arrow Pointer (Pointing Up)
                    val arrowSize = 12.dp.toPx()
                    val arrowPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(needleEnd.x, needleEnd.y)
                        // Calculate two points for the base of the triangle
                        // The arrow should point along the needleAngle
                        val angleLeft = needleAngleRad - Math.PI / 6 + Math.PI
                        val angleRight = needleAngleRad + Math.PI / 6 + Math.PI
                        lineTo(
                            (needleEnd.x + arrowSize * cos(angleLeft)).toFloat(),
                            (needleEnd.y + arrowSize * sin(angleLeft)).toFloat()
                        )
                        lineTo(
                            (needleEnd.x + arrowSize * cos(angleRight)).toFloat(),
                            (needleEnd.y + arrowSize * sin(angleRight)).toFloat()
                        )
                        close()
                    }
                    drawPath(path = arrowPath, color = currentBmiColorText)
                    drawPath(path = arrowPath, color = Color.White, style = Stroke(width = 1.dp.toPx()))
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                    .offset(y = (-4).dp)) {
                    Text(text = viewModel.bmiCategoryResult, color = currentBmiColorText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = viewModel.bmiResultValue, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = currentBmiColorText)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Healthy Weight Range Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = themeColors.buttonEqualBg.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Healthy Weight Range (BMI 18.5 - 24.9)",
                            fontSize = 12.sp,
                            color = themeColors.displayText.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = viewModel.bmiIdealWeightRange,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.buttonEqualBg
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BMI Range Table
            Row(modifier = Modifier
                .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                Text("BMI Range", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
            }
            Spacer(modifier = Modifier
                .height(4.dp))
            Divider(color = themeColors.displayText.copy(alpha=0.1f))
            Spacer(modifier = Modifier
                .height(4.dp))
            
            val categories = listOf(
                "Very Severely Underweight" to "≤ 15.9",
                "Severely Underweight" to "16.0 - 16.9",
                "Underweight" to "17.0 - 18.4",
                "Normal" to "18.5 - 24.9",
                "Overweight" to "25.0 - 29.9",
                "Obese Class I" to "30.0 - 34.9",
                "Obese Class II" to "35.0 - 39.9",
                "Obese Class III" to "≥ 40.0"
            )

            categories.forEach { (cat, range) ->
                val isCurrent = viewModel.bmiCategoryResult == cat
                Row(modifier = Modifier
                .fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isCurrent) {
                            Text("▶ ", color = currentBmiColorText, fontSize = 12.sp)
                        } else {
                            Spacer(modifier = Modifier
                .width(14.dp))
                        }
                        Text(cat, fontSize = 13.sp, color = if(isCurrent) currentBmiColorText else themeColors.displayText)
                    }
                    Text(range, fontSize = 13.sp, color = if(isCurrent) currentBmiColorText else themeColors.displayText)
                }
            }
        }
    }
}
