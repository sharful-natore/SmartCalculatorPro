package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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

@Composable
fun InfoToggleButton(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    themeColors: CalculatorThemeColors,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onToggle,
        modifier = modifier
    ) {
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.Info,
            contentDescription = "Toggle Info",
            tint = if (isExpanded) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun ToolInfoSection(
    title: String,
    infoItems: List<Pair<String, String>>,
    themeColors: CalculatorThemeColors,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = themeColors.buttonNormalBg.copy(alpha = 0.45f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Info,
                    contentDescription = "Info",
                    tint = themeColors.buttonEqualBg,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            infoItems.forEach { (heading, text) ->
                Text(
                    text = heading,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.buttonEqualBg,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Text(
                    text = text,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = themeColors.displayText.copy(alpha = 0.85f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}
