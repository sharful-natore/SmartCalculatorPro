package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel

@Composable
fun PercentageCalculatorCard(viewModel: CalculatorViewModel, themeColors: CalculatorThemeColors) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = themeColors.cardBg),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Percentage Calculator",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = themeColors.displayText
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = viewModel.percentValueA,
                    onValueChange = {
                        viewModel.percentValueA = it
                        viewModel.calculatePercentage()
                    },
                    label = { Text("% of", color = themeColors.displayText.copy(alpha = 0.7f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText,
                        focusedBorderColor = themeColors.buttonOperatorBg,
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("percentage_a_input")
                )
                Text(
                    text = "is what % of",
                    fontSize = 14.sp,
                    color = themeColors.displayText.copy(alpha = 0.7f)
                )
                OutlinedTextField(
                    value = viewModel.percentValueB,
                    onValueChange = {
                        viewModel.percentValueB = it
                        viewModel.calculatePercentage()
                    },
                    label = { Text("Value", color = themeColors.displayText.copy(alpha = 0.7f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText,
                        focusedBorderColor = themeColors.buttonOperatorBg,
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("percentage_b_input")
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Result
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Percentage",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText.copy(alpha = 0.85f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = viewModel.percentageResultText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6366F1),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.testTag("percentage_result_text")
                    )
                }
            }
        }
    }
}
