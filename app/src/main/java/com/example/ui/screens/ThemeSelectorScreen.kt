package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.theme.CalculatorThemeType
import com.example.ui.viewmodel.CalculatorViewModel

@Composable
fun ThemeSelectorScreen(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Custom Themes",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.displayText
        )
        Text(
            text = "Choose your preferred color palette and visual styling",
            fontSize = 12.sp,
            color = themeColors.displayExpressionText,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(CalculatorThemeType.values()) { themeType ->
                val colors = themeType.getColors()
                val isSelected = viewModel.currentThemeType == themeType

                ThemeCard(
                    themeType = themeType,
                    colors = colors,
                    isSelected = isSelected,
                    themeColors = themeColors,
                    onClick = { viewModel.setTheme(themeType) }
                )
            }
        }
    }
}

@Composable
fun ThemeCard(
    themeType: CalculatorThemeType,
    colors: CalculatorThemeColors,
    isSelected: Boolean,
    themeColors: CalculatorThemeColors,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("theme_card_${themeType.name.lowercase()}")
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = colors.background),
        border = if (isSelected) BorderStroke(3.dp, themeColors.buttonOperatorBg) else BorderStroke(1.dp, colors.buttonNormalBg.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = colors.themeName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.displayText
                )

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = themeColors.buttonOperatorBg,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Color Palette Circles Showcase
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ColorDot(colors.displayBackground)
                ColorDot(colors.buttonNormalBg)
                ColorDot(colors.buttonOperatorBg)
                ColorDot(colors.buttonEqualBg)
            }
        }
    }
}

@Composable
fun ColorDot(color: Color) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
    )
}
