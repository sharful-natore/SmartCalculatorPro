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
fun DiscountCalculatorCard(viewModel: CalculatorViewModel, themeColors: CalculatorThemeColors) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = themeColors.cardBg),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Discount Calculator",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = themeColors.displayText
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Inputs
            OutlinedTextField(
                value = viewModel.originalPrice,
                onValueChange = {
                    viewModel.originalPrice = it
                    viewModel.calculateDiscount()
                },
                label = { Text("Original Price", color = themeColors.displayText.copy(alpha = 0.7f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = themeColors.displayText,
                    unfocusedTextColor = themeColors.displayText,
                    focusedBorderColor = themeColors.buttonOperatorBg,
                    unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("discount_price_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.discountPercent,
                    onValueChange = {
                        viewModel.discountPercent = it
                        viewModel.calculateDiscount()
                    },
                    label = { Text("Discount %", color = themeColors.displayText.copy(alpha = 0.7f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText,
                        focusedBorderColor = themeColors.buttonOperatorBg,
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("discount_percent_input")
                )
                OutlinedTextField(
                    value = viewModel.taxPercent,
                    onValueChange = {
                        viewModel.taxPercent = it
                        viewModel.calculateDiscount()
                    },
                    label = { Text("Tax %", color = themeColors.displayText.copy(alpha = 0.7f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText,
                        focusedBorderColor = themeColors.buttonOperatorBg,
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("discount_tax_input")
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Results
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        text = "Final Price",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText.copy(alpha = 0.85f)
                    )
                    Text(
                        text = viewModel.finalPriceResult,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6366F1),
                        modifier = Modifier.testTag("discount_final_price_text")
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "You Save",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText.copy(alpha = 0.85f)
                    )
                    Text(
                        text = viewModel.discountSavingsResult,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6366F1),
                        modifier = Modifier.testTag("discount_savings_text")
                    )
                }
            }
        }
    }
}
