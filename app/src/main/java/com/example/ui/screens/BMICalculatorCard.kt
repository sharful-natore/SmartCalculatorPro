package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
    ElevatedCard(
        modifier = Modifier
                .fillMaxWidth().padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = themeColors.cardBg),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier
                .padding(16.dp)) {
            // Top Row: Age and Height
            Row(modifier = Modifier
                .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                // Age Input
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                .weight(1f)) {
                    Text("Age", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                    OutlinedTextField(
                        value = viewModel.bmiAge,
                        onValueChange = { viewModel.bmiAge = it; viewModel.calculateBMI() },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                .width(60.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 16.sp, color = themeColors.displayText)
                    )
                    Divider(color = themeColors.displayText.copy(alpha=0.1f), modifier = Modifier
                .width(60.dp))
                }

                Spacer(modifier = Modifier
                .width(8.dp))

                // Height Input
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                .weight(2f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (viewModel.bmiHeightUnit == "ft/in") {
                            OutlinedTextField(
                                value = viewModel.bmiHeightFt,
                                onValueChange = { viewModel.bmiHeightFt = it; viewModel.calculateBMI() },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                .width(50.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent),
                                textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 16.sp, color = themeColors.displayText)
                            )
                            Text("'", fontSize = 16.sp, color = themeColors.displayText, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = viewModel.bmiHeightIn,
                                onValueChange = { viewModel.bmiHeightIn = it; viewModel.calculateBMI() },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                .width(50.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent),
                                textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 16.sp, color = themeColors.displayText)
                            )
                            Text("\"", fontSize = 16.sp, color = themeColors.displayText, fontWeight = FontWeight.Bold)
                        } else {
                            OutlinedTextField(
                                value = viewModel.bmiHeight,
                                onValueChange = { viewModel.bmiHeight = it; viewModel.calculateBMI() },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                .width(70.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent),
                                textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 16.sp, color = themeColors.displayText)
                            )
                        }
                    }
                    Divider(color = themeColors.displayText.copy(alpha=0.1f), modifier = Modifier
                .width(120.dp))
                }

                // Height Unit Dropdown
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                .clickable { 
                    viewModel.bmiHeightUnit = if(viewModel.bmiHeightUnit == "ft/in") "cm" else "ft/in"
                    viewModel.calculateBMI()
                }) {
                    Text(text = viewModel.bmiHeightUnit, color = Color(0xFF6200EE), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = themeColors.displayText.copy(alpha=0.5f))
                }
            }
            
            Spacer(modifier = Modifier
                .height(24.dp))
            
            // Middle Row: Gender and Weight
            Row(modifier = Modifier
                .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(modifier = Modifier
                .weight(1f), horizontalArrangement = Arrangement.Center) {
                    Icon(
                        Icons.Default.Woman, 
                        contentDescription = "Female", 
                        tint = if (viewModel.bmiGender == "Female") Color(0xFF6200EE) else themeColors.displayText.copy(alpha = 0.3f),
                        modifier = Modifier
                .size(32.dp).clickable { viewModel.bmiGender = "Female"; viewModel.calculateBMI() }
                    )
                    Spacer(modifier = Modifier
                .width(4.dp))
                    Icon(
                        Icons.Default.Man, 
                        contentDescription = "Male", 
                        tint = if (viewModel.bmiGender == "Male") Color(0xFF6200EE) else themeColors.displayText.copy(alpha = 0.3f),
                        modifier = Modifier
                .size(32.dp).clickable { viewModel.bmiGender = "Male"; viewModel.calculateBMI() }
                    )
                }

                Spacer(modifier = Modifier
                .width(8.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                .weight(2f)) {
                    Text("Weight", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                    OutlinedTextField(
                        value = viewModel.bmiWeight,
                        onValueChange = { viewModel.bmiWeight = it; viewModel.calculateBMI() },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                .width(80.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 16.sp, color = themeColors.displayText)
                    )
                    Divider(color = themeColors.displayText.copy(alpha=0.1f), modifier = Modifier
                .width(80.dp))
                }
                
                // Weight Unit Dropdown
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                .clickable { 
                    viewModel.toggleBmiWeightUnit()
                }) {
                    Text(text = viewModel.bmiWeightUnit, color = Color(0xFF6200EE), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = themeColors.displayText.copy(alpha=0.5f))
                }
            }

            Spacer(modifier = Modifier
                .height(32.dp))

            // Gauge Chart
            val bmiValue = viewModel.bmiResultValue.toFloatOrNull() ?: 0f
            val currentBmiColorText = when {
                bmiValue < 18.5f -> Color(0xFF2196F3)
                bmiValue < 25f -> Color(0xFF4CAF50)
                else -> Color(0xFFF44336)
            }
            Box(modifier = Modifier
                .fillMaxWidth().height(150.dp), contentAlignment = Alignment.BottomCenter) {
                Canvas(modifier = Modifier
                .fillMaxSize().padding(horizontal = 24.dp)) {
                    val strokeWidth = 3.dp.toPx()
                    val radius = size.width / 2 - strokeWidth
                    val center = Offset(size.width / 2, size.height)
                    
                    // Draw Arcs
                    // Total range: 15 to 40 (25 units). 180 degrees / 25 = 7.2 degrees per unit
                    // Underweight: 15 to 18.5 = 3.5 units * 7.2 = 25.2 deg
                    // Normal: 18.5 to 25 = 6.5 units * 7.2 = 46.8 deg
                    // Overweight/Obese: 25 to 40 = 15 units * 7.2 = 108 deg
                    val angles = listOf(180f to 205.2f, 205.2f to 252f, 252f to 360f)
                    val colors = listOf(Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFF44336))
                    
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
                                cap = if (i == 0 || i == angles.size - 1) StrokeCap.Round else StrokeCap.Butt
                            )
                        )
                    }

                    // Calculate Needle position
                    val normalizedBmi = (bmiValue - 15f).coerceIn(0f, 25f) / 25f
                    val needleAngle = 180f + (180f * normalizedBmi)
                    val needleAngleRad = Math.toRadians(needleAngle.toDouble())
                    
                    val innerRadius = radius - strokeWidth / 2
                    val needleEnd = Offset(
                        x = center.x + (innerRadius * cos(needleAngleRad)).toFloat(),
                        y = center.y + (innerRadius * sin(needleAngleRad)).toFloat()
                    )
                    
                    val currentBmiColor = when {
                        bmiValue < 18.5f -> Color(0xFF2196F3)
                        bmiValue < 25f -> Color(0xFF4CAF50)
                        else -> Color(0xFFF44336)
                    }
                    drawCircle(color = currentBmiColor, radius = 5.dp.toPx(), center = needleEnd)
                    drawCircle(color = Color.White, radius = 2.dp.toPx(), center = needleEnd)
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                .offset(y = (-16).dp)) {
                    Text(text = viewModel.bmiCategoryResult, color = currentBmiColorText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier
                .height(8.dp))
                    Text(text = "BMI", fontSize = 12.sp, color = themeColors.displayText)
                    Text(text = viewModel.bmiResultValue, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = currentBmiColorText)
                }
            }

            Spacer(modifier = Modifier
                .height(24.dp))

            // BMI Range Table
            Row(modifier = Modifier
                .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                Text("Difference", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
            }
            Spacer(modifier = Modifier
                .height(8.dp))
            Divider(color = themeColors.displayText.copy(alpha=0.1f))
            Spacer(modifier = Modifier
                .height(8.dp))
            
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
                val isCurrent = viewModel.bmiCategoryResult.contains(cat) || (cat == "Normal" && viewModel.bmiCategoryResult == "Normal")
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
