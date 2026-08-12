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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import com.example.util.AppLanguage
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
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
                label = { Text("Original Price", color = themeColors.displayText.copy(alpha = 0.7f), fontSize = 12.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = themeColors.displayText,
                    unfocusedTextColor = themeColors.displayText,
                    focusedBorderColor = themeColors.buttonOperatorBg,
                    unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("discount_price_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

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
                    label = { Text("Discount %", color = themeColors.displayText.copy(alpha = 0.7f), fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText,
                        focusedBorderColor = themeColors.buttonOperatorBg,
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("discount_percent_input")
                )
                OutlinedTextField(
                    value = viewModel.taxPercent,
                    onValueChange = {
                        viewModel.taxPercent = it
                        viewModel.calculateDiscount()
                    },
                    label = { Text("Tax %", color = themeColors.displayText.copy(alpha = 0.7f), fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText,
                        focusedBorderColor = themeColors.buttonOperatorBg,
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
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
                        color = themeColors.buttonEqualBg,
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
                        color = themeColors.buttonEqualBg,
                        modifier = Modifier.testTag("discount_savings_text")
                    )
                }
            }

            val context = LocalContext.current
            val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val price = viewModel.originalPrice
                    val discount = viewModel.discountPercent
                    val finalPrice = viewModel.finalPriceResult
                    val savings = viewModel.discountSavingsResult
                    if (price.isNotEmpty() && finalPrice.isNotEmpty() && finalPrice != "0" && finalPrice != "0.0") {
                        val expr = if (isBn) "মূল দাম: $price, ডিসকাউন্ট: $discount%" else "Price: $price, Discount: $discount%"
                        val resultStr = if (isBn) "ফাইনাল দাম: $finalPrice (সাশ্রয়: $savings)" else "Final Price: $finalPrice (Saved: $savings)"
                        viewModel.saveToolResultToHistory("Discount Calculator", expr, resultStr)
                        Toast.makeText(context, if (isBn) "ফলাফল হিস্টোরিতে সেভ করা হয়েছে!" else "Saved to history!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, if (isBn) "অনুগ্রহ করে দাম ও ডিসকাউন্ট দিন" else "Please enter original price and discount", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.buttonEqualBg.copy(alpha = 0.15f),
                    contentColor = themeColors.buttonEqualBg
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isBn) "ফলাফল হিস্টোরিতে রাখুন" else "Save Result to History", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
