package com.example.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.squareup.moshi.JsonClass

data class CalculatorThemeColors(
    val background: Color,
    val displayBackground: Color,
    val displayText: Color,
    val displayExpressionText: Color,
    val buttonNormalBg: Color,
    val buttonNormalText: Color,
    val buttonOperatorBg: Color,
    val buttonOperatorText: Color,
    val buttonFunctionBg: Color,
    val buttonFunctionText: Color,
    val buttonEqualBg: Color,
    val buttonEqualText: Color,
    val cardBg: Color,
    val unselectedItemText: Color,
    val navBarBg: Color,
    val titleBarBg: Color,
    val chipBg: Color,
    val isDark: Boolean,
    val themeName: String,
    val themeNameBn: String
)

fun safeParseColor(colorStr: String?, defaultHex: String = "#6366F1"): Color {
    if (colorStr.isNullOrBlank()) {
        return try { Color(android.graphics.Color.parseColor(defaultHex)) } catch (e: Exception) { Color(0xFF6366F1) }
    }
    return try {
        val clean = colorStr.trim()
        val hex = if (clean.startsWith("#")) clean else "#$clean"
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        try {
            Color(android.graphics.Color.parseColor(defaultHex))
        } catch (e2: Exception) {
            Color(0xFF6366F1)
        }
    }
}

fun getToolIconGradient(baseColor: Color): androidx.compose.ui.graphics.Brush {
    val colorLight = Color(
        red = (baseColor.red * 1.38f + 0.15f).coerceIn(0f, 1f),
        green = (baseColor.green * 1.38f + 0.15f).coerceIn(0f, 1f),
        blue = (baseColor.blue * 1.38f + 0.15f).coerceIn(0f, 1f),
        alpha = 1f
    )
    val colorDark = Color(
        red = (baseColor.red * 0.58f).coerceIn(0f, 1f),
        green = (baseColor.green * 0.58f).coerceIn(0f, 1f),
        blue = (baseColor.blue * 0.58f).coerceIn(0f, 1f),
        alpha = 1f
    )
    return androidx.compose.ui.graphics.Brush.linearGradient(
        colors = listOf(colorLight, baseColor, colorDark)
    )
}

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class CustomTheme(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val background: String,
    val displayBackground: String,
    val displayText: String,
    val displayExpressionText: String,
    val buttonNormalBg: String,
    val buttonNormalText: String,
    val buttonOperatorBg: String,
    val buttonOperatorText: String,
    val buttonFunctionBg: String,
    val buttonFunctionText: String,
    val buttonEqualBg: String,
    val buttonEqualText: String,
    val cardBg: String,
    val unselectedItemText: String,
    val navBarBg: String,
    val titleBarBg: String,
    val chipBg: String,
    val isDark: Boolean
) {
    fun toCalculatorThemeColors(): CalculatorThemeColors {
        return CalculatorThemeColors(
            background = safeParseColor(background, "#F8FAFC"),
            displayBackground = safeParseColor(displayBackground, "#FFFFFF"),
            displayText = safeParseColor(displayText, "#1E293B"),
            displayExpressionText = safeParseColor(displayExpressionText, "#64748B"),
            buttonNormalBg = safeParseColor(buttonNormalBg, "#FFFFFF"),
            buttonNormalText = safeParseColor(buttonNormalText, "#1E293B"),
            buttonOperatorBg = safeParseColor(buttonOperatorBg, "#E8DDFF"),
            buttonOperatorText = safeParseColor(buttonOperatorText, "#6366F1"),
            buttonFunctionBg = safeParseColor(buttonFunctionBg, "#E8DDFF"),
            buttonFunctionText = safeParseColor(buttonFunctionText, "#6366F1"),
            buttonEqualBg = safeParseColor(buttonEqualBg, "#6366F1"),
            buttonEqualText = safeParseColor(buttonEqualText, "#FFFFFF"),
            cardBg = safeParseColor(cardBg, "#FFFFFF"),
            unselectedItemText = safeParseColor(unselectedItemText, "#64748B"),
            navBarBg = safeParseColor(navBarBg, "#6366F1"),
            titleBarBg = safeParseColor(titleBarBg, "#6366F1"),
            chipBg = safeParseColor(chipBg, "#E8DDFF"),
            isDark = isDark,
            themeName = name,
            themeNameBn = name
        )
    }
}

fun Color.toHexString(): String {
    return String.format("#%08X", this.toArgb())
}

enum class CalculatorThemeType {
    COFFEE_CREAM,
    SKY_BREEZE,
    MINT_SWEET,
    ROSE_GLOW,
    LAVENDER_DREAM,
    PEACH_BLOSSOM,
    DEEP_FOREST,
    ROYAL_GOLD,
    OCEAN_BLUE,
    CHARCOAL_DARK;

    fun getColors(): CalculatorThemeColors {
        return when (this) {
            COFFEE_CREAM -> CalculatorThemeColors(
                background = Color(0xFFFAF7F2),
                displayBackground = Color(0xFFFAF7F2),
                displayText = Color(0xFF4E342E),
                displayExpressionText = Color(0xFF8D6E63),
                buttonNormalBg = Color(0xFFFFFFFF),
                buttonNormalText = Color(0xFF4E342E),
                buttonOperatorBg = Color(0xFFEFEBE9),
                buttonOperatorText = Color(0xFF6D4C41),
                buttonFunctionBg = Color(0xFFF5F5F5),
                buttonFunctionText = Color(0xFF757575),
                buttonEqualBg = Color(0xFF8D6E63),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFFFFFF),
                unselectedItemText = Color(0xFF8D6E63),
                navBarBg = Color(0xFF8D6E63),
                titleBarBg = Color(0xFF8D6E63),
                chipBg = Color(0xFFEFEBE9),
                isDark = false,
                themeName = "Coffee Cream",
                themeNameBn = "কফি ক্রিম"
            )
            SKY_BREEZE -> CalculatorThemeColors(
                background = Color(0xFFE0F7FA),
                displayBackground = Color(0xFFE0F7FA),
                displayText = Color(0xFF006064),
                displayExpressionText = Color(0xFF00ACC1),
                buttonNormalBg = Color(0xFFFFFFFF),
                buttonNormalText = Color(0xFF006064),
                buttonOperatorBg = Color(0xFFB2EBF2),
                buttonOperatorText = Color(0xFF00838F),
                buttonFunctionBg = Color(0xFFE0F2F1),
                buttonFunctionText = Color(0xFF009688),
                buttonEqualBg = Color(0xFF00BCD4),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFFFFFF),
                unselectedItemText = Color(0xFF00ACC1),
                navBarBg = Color(0xFF00BCD4),
                titleBarBg = Color(0xFF00BCD4),
                chipBg = Color(0xFFB2EBF2),
                isDark = false,
                themeName = "Sky Breeze",
                themeNameBn = "স্কাই ব্রিজ"
            )
            MINT_SWEET -> CalculatorThemeColors(
                background = Color(0xFFEAF9F1),
                displayBackground = Color(0xFFEAF9F1),
                displayText = Color(0xFF0E6251),
                displayExpressionText = Color(0xFF117A65),
                buttonNormalBg = Color(0xFFFFFFFF),
                buttonNormalText = Color(0xFF0E6251),
                buttonOperatorBg = Color(0xFFD1F2EB),
                buttonOperatorText = Color(0xFF16A085),
                buttonFunctionBg = Color(0xFFE8F8F5),
                buttonFunctionText = Color(0xFF1ABC9C),
                buttonEqualBg = Color(0xFF1ABC9C),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFFFFFF),
                unselectedItemText = Color(0xFF117A65),
                navBarBg = Color(0xFF16A085),
                titleBarBg = Color(0xFF16A085),
                chipBg = Color(0xFFD1F2EB),
                isDark = false,
                themeName = "Mint Sweet",
                themeNameBn = "মিষ্টি মিন্ট"
            )
            ROSE_GLOW -> CalculatorThemeColors(
                background = Color(0xFFFDEDEC),
                displayBackground = Color(0xFFFDEDEC),
                displayText = Color(0xFF78281F),
                displayExpressionText = Color(0xFF922B21),
                buttonNormalBg = Color(0xFFFFFFFF),
                buttonNormalText = Color(0xFF78281F),
                buttonOperatorBg = Color(0xFFFADBD8),
                buttonOperatorText = Color(0xFFC0392B),
                buttonFunctionBg = Color(0xFFFDEDEC),
                buttonFunctionText = Color(0xFFE74C3C),
                buttonEqualBg = Color(0xFFE74C3C),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFFFFFF),
                unselectedItemText = Color(0xFF922B21),
                navBarBg = Color(0xFFC0392B),
                titleBarBg = Color(0xFFC0392B),
                chipBg = Color(0xFFFADBD8),
                isDark = false,
                themeName = "Rose Glow",
                themeNameBn = "রোজ গ্লো"
            )
            LAVENDER_DREAM -> CalculatorThemeColors(
                background = Color(0xFFF4ECF7),
                displayBackground = Color(0xFFF4ECF7),
                displayText = Color(0xFF4A235A),
                displayExpressionText = Color(0xFF5B2C6F),
                buttonNormalBg = Color(0xFFFFFFFF),
                buttonNormalText = Color(0xFF4A235A),
                buttonOperatorBg = Color(0xFFE8DAEF),
                buttonOperatorText = Color(0xFF8E44AD),
                buttonFunctionBg = Color(0xFFF5EEF8),
                buttonFunctionText = Color(0xFF9B59B6),
                buttonEqualBg = Color(0xFF8E44AD),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFFFFFF),
                unselectedItemText = Color(0xFF5B2C6F),
                navBarBg = Color(0xFF8E44AD),
                titleBarBg = Color(0xFF8E44AD),
                chipBg = Color(0xFFE8DAEF),
                isDark = false,
                themeName = "Lavender Dream",
                themeNameBn = "ল্যাভেন্ডার ড্রিম"
            )
            PEACH_BLOSSOM -> CalculatorThemeColors(
                background = Color(0xFFFDF2E9),
                displayBackground = Color(0xFFFDF2E9),
                displayText = Color(0xFF6E2C00),
                displayExpressionText = Color(0xFF873600),
                buttonNormalBg = Color(0xFFFFFFFF),
                buttonNormalText = Color(0xFF6E2C00),
                buttonOperatorBg = Color(0xFFF5CBA7),
                buttonOperatorText = Color(0xFFD35400),
                buttonFunctionBg = Color(0xFFFBEEE6),
                buttonFunctionText = Color(0xFFE59866),
                buttonEqualBg = Color(0xFFE67E22),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFFFFFF),
                unselectedItemText = Color(0xFF873600),
                navBarBg = Color(0xFFD35400),
                titleBarBg = Color(0xFFD35400),
                chipBg = Color(0xFFF5CBA7),
                isDark = false,
                themeName = "Peach Blossom",
                themeNameBn = "পিচ ব্লসম"
            )
            DEEP_FOREST -> CalculatorThemeColors(
                background = Color(0xFFF1F9F1),
                displayBackground = Color(0xFFE2EFE2),
                displayText = Color(0xFF0F3A0F),
                displayExpressionText = Color(0xFF1B531B),
                buttonNormalBg = Color(0xFFFFFFFF),
                buttonNormalText = Color(0xFF0F3A0F),
                buttonOperatorBg = Color(0xFFC8E6C9),
                buttonOperatorText = Color(0xFF1B5E20),
                buttonFunctionBg = Color(0xFFE8F5E9),
                buttonFunctionText = Color(0xFF2E7D32),
                buttonEqualBg = Color(0xFF2E7D32),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFFFFFF),
                unselectedItemText = Color(0xFF1B531B),
                navBarBg = Color(0xFF2E7D32),
                titleBarBg = Color(0xFF2E7D32),
                chipBg = Color(0xFFC8E6C9),
                isDark = false,
                themeName = "Deep Forest",
                themeNameBn = "ডিপ ফরেস্ট"
            )
            ROYAL_GOLD -> CalculatorThemeColors(
                background = Color(0xFF0F172A),
                displayBackground = Color(0xFF1E293B),
                displayText = Color(0xFFF8FAFC),
                displayExpressionText = Color(0xFF94A3B8),
                buttonNormalBg = Color(0xFF1E293B),
                buttonNormalText = Color(0xFFF8FAFC),
                buttonOperatorBg = Color(0xFF334155),
                buttonOperatorText = Color(0xFFF59E0B),
                buttonFunctionBg = Color(0xFF1E293B),
                buttonFunctionText = Color(0xFF94A3B8),
                buttonEqualBg = Color(0xFFD97706),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFF1E293B),
                unselectedItemText = Color(0xFF94A3B8),
                navBarBg = Color(0xFF1E293B),
                titleBarBg = Color(0xFF1E293B),
                chipBg = Color(0xFF334155),
                isDark = true,
                themeName = "Royal Gold",
                themeNameBn = "রয়্যাল গোল্ড"
            )
            OCEAN_BLUE -> CalculatorThemeColors(
                background = Color(0xFFEBF5FB),
                displayBackground = Color(0xFFD4E6F1),
                displayText = Color(0xFF1B4F72),
                displayExpressionText = Color(0xFF2471A3),
                buttonNormalBg = Color(0xFFFFFFFF),
                buttonNormalText = Color(0xFF1B4F72),
                buttonOperatorBg = Color(0xFFAED6F1),
                buttonOperatorText = Color(0xFF1A5276),
                buttonFunctionBg = Color(0xFFEBF5FB),
                buttonFunctionText = Color(0xFF2980B9),
                buttonEqualBg = Color(0xFF2980B9),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFFFFFF),
                unselectedItemText = Color(0xFF2471A3),
                navBarBg = Color(0xFF1A5276),
                titleBarBg = Color(0xFF1A5276),
                chipBg = Color(0xFFAED6F1),
                isDark = false,
                themeName = "Ocean Blue",
                themeNameBn = "ওশান ব্লু"
            )
            CHARCOAL_DARK -> CalculatorThemeColors(
                background = Color(0xFF121212),
                displayBackground = Color(0xFF1E1E1E),
                displayText = Color(0xFFF5F5F5),
                displayExpressionText = Color(0xFFA0A0A0),
                buttonNormalBg = Color(0xFF1E1E1E),
                buttonNormalText = Color(0xFFF5F5F5),
                buttonOperatorBg = Color(0xFF2A2A2A),
                buttonOperatorText = Color(0xFF00ADB5),
                buttonFunctionBg = Color(0xFF1E1E1E),
                buttonFunctionText = Color(0xFFA0A0A0),
                buttonEqualBg = Color(0xFF00ADB5),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFF1E1E1E),
                unselectedItemText = Color(0xFFA0A0A0),
                navBarBg = Color(0xFF121212),
                titleBarBg = Color(0xFF121212),
                chipBg = Color(0xFF2A2A2A),
                isDark = true,
                themeName = "Charcoal Dark",
                themeNameBn = "চারকোল ডার্ক"
            )
        }
    }
}

fun Modifier.themeCardShadow(
    themeColors: CalculatorThemeColors,
    elevation: Dp = 1.dp,
    shape: Shape = RoundedCornerShape(16.dp)
): Modifier = this.shadow(
    elevation = elevation,
    shape = shape,
    clip = true,
    ambientColor = themeColors.displayText.copy(alpha = 0.08f),
    spotColor = themeColors.displayText.copy(alpha = 0.12f)
)

