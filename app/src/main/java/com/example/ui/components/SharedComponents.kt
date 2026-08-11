package com.example.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors

@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    themeColors: CalculatorThemeColors,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Number,
    singleLine: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                color = themeColors.displayText.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = themeColors.cardBg,
            unfocusedContainerColor = themeColors.cardBg,
            focusedTextColor = themeColors.displayText,
            unfocusedTextColor = themeColors.displayText,
            focusedBorderColor = themeColors.buttonEqualBg,
            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.35f),
            focusedLabelColor = themeColors.buttonEqualBg,
            unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.8f),
            cursorColor = themeColors.buttonEqualBg
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier,
        singleLine = singleLine,
        trailingIcon = trailingIcon
    )
}
