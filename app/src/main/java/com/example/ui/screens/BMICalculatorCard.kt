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

            // Linear Color-Coded BMI Scale Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Result Display
                Text(
                    text = viewModel.bmiCategoryResult,
                    color = currentBmiColorText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = viewModel.bmiResultValue,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = currentBmiColorText
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Indicator Position (Range 15 to 40)
                val normalizedBmi = ((bmiValue - 15f) / 25f).coerceIn(0.02f, 0.98f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Column {
                        // Pointer Thumb triangle pointing down to bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .align(Alignment.CenterStart)
                                    .fillMaxWidth(fraction = normalizedBmi),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Canvas(modifier = Modifier.size(12.dp)) {
                                    val path = androidx.compose.ui.graphics.Path().apply {
                                        moveTo(size.width / 2, size.height)
                                        lineTo(0f, 0f)
                                        lineTo(size.width, 0f)
                                        close()
                                    }
                                    drawPath(path = path, color = currentBmiColorText)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Segmented Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .clip(RoundedCornerShape(7.dp))
                        ) {
                            // Underweight (< 18.5) ~ 14% of bar
                            Box(
                                modifier = Modifier
                                    .weight(0.14f)
                                    .fillMaxHeight()
                                    .background(Color(0xFF29B6F6))
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            // Normal (18.5 - 25) ~ 26% of bar
                            Box(
                                modifier = Modifier
                                    .weight(0.26f)
                                    .fillMaxHeight()
                                    .background(Color(0xFF34C759))
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            // Overweight & Obese (25 - 40) ~ 60% of bar
                            Box(
                                modifier = Modifier
                                    .weight(0.60f)
                                    .fillMaxHeight()
                                    .background(Color(0xFFE65100))
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Labels below bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("15", fontSize = 10.sp, color = themeColors.displayText.copy(alpha = 0.5f))
                            Text("18.5", fontSize = 10.sp, color = themeColors.displayText.copy(alpha = 0.5f))
                            Text("25", fontSize = 10.sp, color = themeColors.displayText.copy(alpha = 0.5f))
                            Text("40", fontSize = 10.sp, color = themeColors.displayText.copy(alpha = 0.5f))
                        }
                    }
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
