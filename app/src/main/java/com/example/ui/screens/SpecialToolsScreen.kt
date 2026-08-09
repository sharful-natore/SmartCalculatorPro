package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel

@Composable
fun SpecialToolsScreen(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val scrollState = rememberScrollState()

    var activeTool by remember { mutableStateOf(0) } // 0=BMI, 1=Age, 2=Discount, 3=Percentage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Tool Selector Tabs Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ToolTabButton("BMI", Icons.Default.Accessibility, activeTool == 0, themeColors, Modifier.weight(1f)) { activeTool = 0 }
            ToolTabButton("Age", Icons.Default.CalendarMonth, activeTool == 1, themeColors, Modifier.weight(1f)) { activeTool = 1 }
            ToolTabButton("Discount", Icons.Default.Discount, activeTool == 2, themeColors, Modifier.weight(1f)) { activeTool = 2 }
            ToolTabButton("Percent", Icons.Default.Percent, activeTool == 3, themeColors, Modifier.weight(1f)) { activeTool = 3 }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Tool Screen
        when (activeTool) {
            0 -> BMICalculatorCard(viewModel, themeColors)
            1 -> AgeCalculatorCard(viewModel, themeColors)
            2 -> DiscountCalculatorCard(viewModel, themeColors)
            3 -> PercentageCalculatorCard(viewModel, themeColors)
        }
    }
}

@Composable
fun ToolTabButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    themeColors: CalculatorThemeColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) Color(0xFF6366F1) else themeColors.buttonNormalBg)
            .clickable(onClick = onClick)
            .testTag("tool_tab_$label")
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else themeColors.unselectedItemText,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else themeColors.unselectedItemText
            )
        }
    }
}

