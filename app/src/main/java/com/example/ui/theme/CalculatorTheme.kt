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
    INDIGO_ESSENCE,
    DEEP_OCEAN,
    SAGE_GARDEN,
    ROSE_PETAL,
    MIDNIGHT_ASH,
    SLATE_BLUE,
    LAVENDER_MIST,
    COFFEE_CREAM,
    FOREST_NIGHT,
    SKY_BREEZE,
    GOLDEN_SANDS,
    PURE_MINIMAL,
    PURE_DARK;

    fun getColors(): CalculatorThemeColors {
        return when (this) {
            INDIGO_ESSENCE -> CalculatorThemeColors(
                background = Color(0xFFF8FAFC),
                displayBackground = Color(0xFFF8FAFC),
                displayText = Color(0xFF1E293B),
                displayExpressionText = Color(0xFF64748B),
                buttonNormalBg = Color(0xFFFFFFFF),
                buttonNormalText = Color(0xFF1E293B),
                buttonOperatorBg = Color(0xFFE8DDFF),
                buttonOperatorText = Color(0xFF6366F1),
                buttonFunctionBg = Color(0xFFE8DDFF),
                buttonFunctionText = Color(0xFF6366F1),
                buttonEqualBg = Color(0xFF6366F1),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFFFFFF),
                unselectedItemText = Color(0xFF64748B),
                navBarBg = Color(0xFF6366F1),
                titleBarBg = Color(0xFF6366F1),
                chipBg = Color(0xFFE8DDFF),
                isDark = false,
                themeName = "Indigo Essence",
                themeNameBn = "ইনডিগো এসেন্স"
            )
            DEEP_OCEAN -> CalculatorThemeColors(
                background = Color(0xFF0F172A),
                displayBackground = Color(0xFF0F172A),
                displayText = Color(0xFFF8FAFC),
                displayExpressionText = Color(0xFF94A3B8),
                buttonNormalBg = Color(0xFF1E293B),
                buttonNormalText = Color(0xFFF8FAFC),
                buttonOperatorBg = Color(0xFF334155),
                buttonOperatorText = Color(0xFF38BDF8),
                buttonFunctionBg = Color(0xFF1E293B),
                buttonFunctionText = Color(0xFF94A3B8),
                buttonEqualBg = Color(0xFF0EA5E9),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFF1E293B),
                unselectedItemText = Color(0xFF94A3B8),
                navBarBg = Color(0xFF0EA5E9),
                titleBarBg = Color(0xFF0EA5E9),
                chipBg = Color(0xFF334155),
                isDark = true,
                themeName = "Deep Ocean",
                themeNameBn = "ডিপ ওশান"
            )
            SAGE_GARDEN -> CalculatorThemeColors(
                background = Color(0xFFF0F4F0),
                displayBackground = Color(0xFFF0F4F0),
                displayText = Color(0xFF2D3436),
                displayExpressionText = Color(0xFF636E72),
                buttonNormalBg = Color(0xFFFFFFFF),
                buttonNormalText = Color(0xFF2D3436),
                buttonOperatorBg = Color(0xFFE8F5E9),
                buttonOperatorText = Color(0xFF2E7D32),
                buttonFunctionBg = Color(0xFFF1F8E9),
                buttonFunctionText = Color(0xFF558B2F),
                buttonEqualBg = Color(0xFF4CAF50),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFFFFFF),
                unselectedItemText = Color(0xFF636E72),
                navBarBg = Color(0xFF4CAF50),
                titleBarBg = Color(0xFF4CAF50),
                chipBg = Color(0xFFE8F5E9),
                isDark = false,
                themeName = "Sage Garden",
                themeNameBn = "সেজ গার্ডেন"
            )
            ROSE_PETAL -> CalculatorThemeColors(
                background = Color(0xFFFFF5F7),
                displayBackground = Color(0xFFFFF5F7),
                displayText = Color(0xFF4A154B),
                displayExpressionText = Color(0xFF8D5D8D),
                buttonNormalBg = Color(0xFFFFFFFF),
                buttonNormalText = Color(0xFF4A154B),
                buttonOperatorBg = Color(0xFFFCE4EC),
                buttonOperatorText = Color(0xFFD81B60),
                buttonFunctionBg = Color(0xFFF3E5F5),
                buttonFunctionText = Color(0xFF8E24AA),
                buttonEqualBg = Color(0xFFE91E63),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFFFFFF),
                unselectedItemText = Color(0xFF8D5D8D),
                navBarBg = Color(0xFFE91E63),
                titleBarBg = Color(0xFFE91E63),
                chipBg = Color(0xFFFCE4EC),
                isDark = false,
                themeName = "Rose Petal",
                themeNameBn = "রোজ পেটাল"
            )
            MIDNIGHT_ASH -> CalculatorThemeColors(
                background = Color(0xFF121212),
                displayBackground = Color(0xFF121212),
                displayText = Color(0xFFE0E0E0),
                displayExpressionText = Color(0xFF9E9E9E),
                buttonNormalBg = Color(0xFF1E1E1E),
                buttonNormalText = Color(0xFFE0E0E0),
                buttonOperatorBg = Color(0xFF2C2C2C),
                buttonOperatorText = Color(0xFFBB86FC),
                buttonFunctionBg = Color(0xFF1E1E1E),
                buttonFunctionText = Color(0xFF03DAC6),
                buttonEqualBg = Color(0xFFBB86FC),
                buttonEqualText = Color(0xFF000000),
                cardBg = Color(0xFF1E1E1E),
                unselectedItemText = Color(0xFF9E9E9E),
                navBarBg = Color(0xFF1E1E1E),
                titleBarBg = Color(0xFF1E1E1E),
                chipBg = Color(0xFF2C2C2C),
                isDark = true,
                themeName = "Midnight Ash",
                themeNameBn = "মিডনাইট অ্যাশ"
            )
            SLATE_BLUE -> CalculatorThemeColors(
                background = Color(0xFF1E293B),
                displayBackground = Color(0xFF1E293B),
                displayText = Color(0xFFF1F5F9),
                displayExpressionText = Color(0xFF94A3B8),
                buttonNormalBg = Color(0xFF334155),
                buttonNormalText = Color(0xFFF1F5F9),
                buttonOperatorBg = Color(0xFF475569),
                buttonOperatorText = Color(0xFF818CF8),
                buttonFunctionBg = Color(0xFF334155),
                buttonFunctionText = Color(0xFF94A3B8),
                buttonEqualBg = Color(0xFF6366F1),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFF334155),
                unselectedItemText = Color(0xFF94A3B8),
                navBarBg = Color(0xFF6366F1),
                titleBarBg = Color(0xFF6366F1),
                chipBg = Color(0xFF475569),
                isDark = true,
                themeName = "Slate Blue",
                themeNameBn = "স্লিট ব্লু"
            )
            LAVENDER_MIST -> CalculatorThemeColors(
                background = Color(0xFFF3F0F9),
                displayBackground = Color(0xFFF3F0F9),
                displayText = Color(0xFF312E81),
                displayExpressionText = Color(0xFF6366F1),
                buttonNormalBg = Color(0xFFFFFFFF),
                buttonNormalText = Color(0xFF312E81),
                buttonOperatorBg = Color(0xFFE0E7FF),
                buttonOperatorText = Color(0xFF4F46E5),
                buttonFunctionBg = Color(0xFFEEF2FF),
                buttonFunctionText = Color(0xFF818CF8),
                buttonEqualBg = Color(0xFF818CF8),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFFFFFF),
                unselectedItemText = Color(0xFF6366F1),
                navBarBg = Color(0xFF818CF8),
                titleBarBg = Color(0xFF818CF8),
                chipBg = Color(0xFFE0E7FF),
                isDark = false,
                themeName = "Lavender Mist",
                themeNameBn = "ল্যাভেন্ডার মিস্ট"
            )
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
            FOREST_NIGHT -> CalculatorThemeColors(
                background = Color(0xFF0A1F0A),
                displayBackground = Color(0xFF0A1F0A),
                displayText = Color(0xFFE8F5E9),
                displayExpressionText = Color(0xFF81C784),
                buttonNormalBg = Color(0xFF1B331B),
                buttonNormalText = Color(0xFFE8F5E9),
                buttonOperatorBg = Color(0xFF2E7D32),
                buttonOperatorText = Color(0xFFA5D6A7),
                buttonFunctionBg = Color(0xFF1B331B),
                buttonFunctionText = Color(0xFF81C784),
                buttonEqualBg = Color(0xFF4CAF50),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFF1B331B),
                unselectedItemText = Color(0xFF81C784),
                navBarBg = Color(0xFF4CAF50),
                titleBarBg = Color(0xFF4CAF50),
                chipBg = Color(0xFF2E7D32),
                isDark = true,
                themeName = "Forest Night",
                themeNameBn = "ফরেস্ট নাইট"
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
            GOLDEN_SANDS -> CalculatorThemeColors(
                background = Color(0xFFFFF9C4),
                displayBackground = Color(0xFFFFF9C4),
                displayText = Color(0xFFF57F17),
                displayExpressionText = Color(0xFFFBC02D),
                buttonNormalBg = Color(0xFFFFFFFF),
                buttonNormalText = Color(0xFFF57F17),
                buttonOperatorBg = Color(0xFFFFF176),
                buttonOperatorText = Color(0xFFF9A825),
                buttonFunctionBg = Color(0xFFFFFDE7),
                buttonFunctionText = Color(0xFFFBC02D),
                buttonEqualBg = Color(0xFFFFD600),
                buttonEqualText = Color(0xFF000000),
                cardBg = Color(0xFFFFFFFF),
                unselectedItemText = Color(0xFFFBC02D),
                navBarBg = Color(0xFFFFD600),
                titleBarBg = Color(0xFFFFD600),
                chipBg = Color(0xFFFFF176),
                isDark = false,
                themeName = "Golden Sands",
                themeNameBn = "গোল্ডেন স্যান্ডস"
            )
            PURE_MINIMAL -> CalculatorThemeColors(
                background = Color(0xFFFFFFFF),
                displayBackground = Color(0xFFFFFFFF),
                displayText = Color(0xFF000000),
                displayExpressionText = Color(0xFF757575),
                buttonNormalBg = Color(0xFFF5F5F5),
                buttonNormalText = Color(0xFF000000),
                buttonOperatorBg = Color(0xFFE0E0E0),
                buttonOperatorText = Color(0xFF000000),
                buttonFunctionBg = Color(0xFFF5F5F5),
                buttonFunctionText = Color(0xFF757575),
                buttonEqualBg = Color(0xFF000000),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFFF5F5F5),
                unselectedItemText = Color(0xFF757575),
                navBarBg = Color(0xFF000000),
                titleBarBg = Color(0xFF000000),
                chipBg = Color(0xFFE0E0E0),
                isDark = false,
                themeName = "Pure Minimal",
                themeNameBn = "পিওর মিনিমাল"
            )
            PURE_DARK -> CalculatorThemeColors(
                background = Color(0xFF000000),
                displayBackground = Color(0xFF000000),
                displayText = Color(0xFFFFFFFF),
                displayExpressionText = Color(0xFFA1A1AA),
                buttonNormalBg = Color(0xFF18181B),
                buttonNormalText = Color(0xFFFFFFFF),
                buttonOperatorBg = Color(0xFF27272A),
                buttonOperatorText = Color(0xFF38BDF8),
                buttonFunctionBg = Color(0xFF18181B),
                buttonFunctionText = Color(0xFFA1A1AA),
                buttonEqualBg = Color(0xFF0284C7),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFF121212),
                unselectedItemText = Color(0xFFA1A1AA),
                navBarBg = Color(0xFF0284C7),
                titleBarBg = Color(0xFF0284C7),
                chipBg = Color(0xFF27272A),
                isDark = true,
                themeName = "Pure Dark (AMOLED)",
                themeNameBn = "পিওর ডার্ক (OLED)"
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

