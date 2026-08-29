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
    val hsv = FloatArray(3)
    val argb = android.graphics.Color.argb(
        (baseColor.alpha * 255).toInt().coerceIn(0, 255),
        (baseColor.red * 255).toInt().coerceIn(0, 255),
        (baseColor.green * 255).toInt().coerceIn(0, 255),
        (baseColor.blue * 255).toInt().coerceIn(0, 255)
    )
    android.graphics.Color.colorToHSV(argb, hsv)

    val h = hsv[0]
    val s = hsv[1]
    val v = hsv[2]

    // Soft, elegant pastel-frosted hues with controlled saturation & gentle luminance
    val startHsv = floatArrayOf(
        (h - 25f + 360f) % 360f,
        (s * 0.50f).coerceIn(0.20f, 0.60f),
        (v * 1.10f + 0.10f).coerceIn(0.70f, 0.98f)
    )
    val startColor = Color(android.graphics.Color.HSVToColor(startHsv)).copy(alpha = 0.90f)

    val midHsv = floatArrayOf(
        h,
        (s * 0.60f).coerceIn(0.25f, 0.70f),
        (v * 1.02f).coerceIn(0.65f, 0.92f)
    )
    val midColor = Color(android.graphics.Color.HSVToColor(midHsv)).copy(alpha = 0.88f)

    val endHsv = floatArrayOf(
        (h + 30f) % 360f,
        (s * 0.70f).coerceIn(0.30f, 0.75f),
        (v * 0.88f).coerceIn(0.55f, 0.88f)
    )
    val endColor = Color(android.graphics.Color.HSVToColor(endHsv)).copy(alpha = 0.85f)

    return androidx.compose.ui.graphics.Brush.linearGradient(
        colors = listOf(startColor, midColor, endColor),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
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
    INDIGO_CYAN,
    CHARCOAL_DARK,
    IRIS_GLOW;

    fun getColors(): CalculatorThemeColors {
        return when (this) {
            COFFEE_CREAM -> CalculatorThemeColors(
                background = Color(0xFFFAFAFA),
                displayBackground = Color(0xFFFAFAFA),
                displayText = Color(0xFF4E342E),
                displayExpressionText = Color(0xFF8D6E63),
                buttonNormalBg = Color(0xFFFFFFFF),
                buttonNormalText = Color(0xFF4E342E),
                buttonOperatorBg = Color(0xFFF5EFEA),
                buttonOperatorText = Color(0xFF6D4C41),
                buttonFunctionBg = Color(0xFFF9F6F3),
                buttonFunctionText = Color(0xFF757575),
                buttonEqualBg = Color(0xFF8D6E63),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFFFFFF),
                unselectedItemText = Color(0xFF8D6E63),
                navBarBg = Color(0xFF8D6E63),
                titleBarBg = Color(0xFF8D6E63),
                chipBg = Color(0xFFF5EFEA),
                isDark = false,
                themeName = "Coffee Cream",
                themeNameBn = "কফি ক্রিম"
            )
            SKY_BREEZE -> CalculatorThemeColors(
                background = Color(0xFFF6FCFD),
                displayBackground = Color(0xFFF6FCFD),
                displayText = Color(0xFF006064),
                displayExpressionText = Color(0xFF00ACC1),
                buttonNormalBg = Color(0xFFFFFFFF),
                buttonNormalText = Color(0xFF006064),
                buttonOperatorBg = Color(0xFFE0F7FA),
                buttonOperatorText = Color(0xFF00838F),
                buttonFunctionBg = Color(0xFFF0FDFE),
                buttonFunctionText = Color(0xFF009688),
                buttonEqualBg = Color(0xFF00BCD4),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFFFFFF),
                unselectedItemText = Color(0xFF00ACC1),
                navBarBg = Color(0xFF00BCD4),
                titleBarBg = Color(0xFF00BCD4),
                chipBg = Color(0xFFE0F7FA),
                isDark = false,
                themeName = "Sky Breeze",
                themeNameBn = "স্কাই ব্রিজ"
            )
            MINT_SWEET -> CalculatorThemeColors(
                background = Color(0xFFF7FCF9),
                displayBackground = Color(0xFFF7FCF9),
                displayText = Color(0xFF0E6251),
                displayExpressionText = Color(0xFF117A65),
                buttonNormalBg = Color(0xFFFFFFFF),
                buttonNormalText = Color(0xFF0E6251),
                buttonOperatorBg = Color(0xFFE8F8F5),
                buttonOperatorText = Color(0xFF16A085),
                buttonFunctionBg = Color(0xFFF2FBF7),
                buttonFunctionText = Color(0xFF1ABC9C),
                buttonEqualBg = Color(0xFF1ABC9C),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFFFFFF),
                unselectedItemText = Color(0xFF117A65),
                navBarBg = Color(0xFF16A085),
                titleBarBg = Color(0xFF16A085),
                chipBg = Color(0xFFE8F8F5),
                isDark = false,
                themeName = "Mint Sweet",
                themeNameBn = "মিষ্টি মিন্ট"
            )
            ROSE_GLOW -> CalculatorThemeColors(
                background = Color(0xFFFDF8F8),
                displayBackground = Color(0xFFFDF8F8),
                displayText = Color(0xFF78281F),
                displayExpressionText = Color(0xFF922B21),
                buttonNormalBg = Color(0xFFFFFFFF),
                buttonNormalText = Color(0xFF78281F),
                buttonOperatorBg = Color(0xFFFDEDEC),
                buttonOperatorText = Color(0xFFC0392B),
                buttonFunctionBg = Color(0xFFFEF5F5),
                buttonFunctionText = Color(0xFFE74C3C),
                buttonEqualBg = Color(0xFFE74C3C),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFFFFFF),
                unselectedItemText = Color(0xFF922B21),
                navBarBg = Color(0xFFC0392B),
                titleBarBg = Color(0xFFC0392B),
                chipBg = Color(0xFFFDEDEC),
                isDark = false,
                themeName = "Rose Glow",
                themeNameBn = "রোজ গ্লো"
            )
            LAVENDER_DREAM -> CalculatorThemeColors(
                background = Color(0xFFFAF7FC),
                displayBackground = Color(0xFFFAF7FC),
                displayText = Color(0xFF4A235A),
                displayExpressionText = Color(0xFF5B2C6F),
                buttonNormalBg = Color(0xFFFFFFFF),
                buttonNormalText = Color(0xFF4A235A),
                buttonOperatorBg = Color(0xFFF4ECF7),
                buttonOperatorText = Color(0xFF8E44AD),
                buttonFunctionBg = Color(0xFFFAF4FD),
                buttonFunctionText = Color(0xFF9B59B6),
                buttonEqualBg = Color(0xFF8E44AD),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFFFFFF),
                unselectedItemText = Color(0xFF5B2C6F),
                navBarBg = Color(0xFF8E44AD),
                titleBarBg = Color(0xFF8E44AD),
                chipBg = Color(0xFFF4ECF7),
                isDark = false,
                themeName = "Lavender Dream",
                themeNameBn = "ল্যাভেন্ডার ড্রিম"
            )
            PEACH_BLOSSOM -> CalculatorThemeColors(
                background = Color(0xFFFDF9F6),
                displayBackground = Color(0xFFFDF9F6),
                displayText = Color(0xFF6E2C00),
                displayExpressionText = Color(0xFF873600),
                buttonNormalBg = Color(0xFFFFFFFF),
                buttonNormalText = Color(0xFF6E2C00),
                buttonOperatorBg = Color(0xFFFDF2E9),
                buttonOperatorText = Color(0xFFD35400),
                buttonFunctionBg = Color(0xFFFEF8F4),
                buttonFunctionText = Color(0xFFE59866),
                buttonEqualBg = Color(0xFFE67E22),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFFFFFF),
                unselectedItemText = Color(0xFF873600),
                navBarBg = Color(0xFFD35400),
                titleBarBg = Color(0xFFD35400),
                chipBg = Color(0xFFFDF2E9),
                isDark = false,
                themeName = "Peach Blossom",
                themeNameBn = "পিচ ব্লসম"
            )
            DEEP_FOREST -> CalculatorThemeColors(
                background = Color(0xFFFAFDFA),
                displayBackground = Color(0xFFF1F9F1),
                displayText = Color(0xFF0F3A0F),
                displayExpressionText = Color(0xFF1B531B),
                buttonNormalBg = Color(0xFFFFFFFF),
                buttonNormalText = Color(0xFF0F3A0F),
                buttonOperatorBg = Color(0xFFE8F5E9),
                buttonOperatorText = Color(0xFF1B5E20),
                buttonFunctionBg = Color(0xFFF3FAF3),
                buttonFunctionText = Color(0xFF2E7D32),
                buttonEqualBg = Color(0xFF2E7D32),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFFFFFF),
                unselectedItemText = Color(0xFF1B531B),
                navBarBg = Color(0xFF2E7D32),
                titleBarBg = Color(0xFF2E7D32),
                chipBg = Color(0xFFE8F5E9),
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
                background = Color(0xFFF7FAFD),
                displayBackground = Color(0xFFEBF5FB),
                displayText = Color(0xFF1B4F72),
                displayExpressionText = Color(0xFF2471A3),
                buttonNormalBg = Color(0xFFFFFFFF),
                buttonNormalText = Color(0xFF1B4F72),
                buttonOperatorBg = Color(0xFFD4E6F1),
                buttonOperatorText = Color(0xFF1A5276),
                buttonFunctionBg = Color(0xFFF2F8FC),
                buttonFunctionText = Color(0xFF2980B9),
                buttonEqualBg = Color(0xFF2980B9),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFFFFFF),
                unselectedItemText = Color(0xFF2471A3),
                navBarBg = Color(0xFF1A5276),
                titleBarBg = Color(0xFF1A5276),
                chipBg = Color(0xFFD4E6F1),
                isDark = false,
                themeName = "Ocean Blue",
                themeNameBn = "ওশান ব্লু"
            )
            INDIGO_CYAN -> CalculatorThemeColors(
                background = Color(0xFFFAFAFC),
                displayBackground = Color(0xFFF0F4FA),
                displayText = Color(0xFF1E293B),
                displayExpressionText = Color(0xFF475569),
                buttonNormalBg = Color(0xFFFFFFFF),
                buttonNormalText = Color(0xFF1E293B),
                buttonOperatorBg = Color(0xFFE0E7FF),
                buttonOperatorText = Color(0xFF4F46E5),
                buttonFunctionBg = Color(0xFFE0F2FE),
                buttonFunctionText = Color(0xFF0284C7),
                buttonEqualBg = Color(0xFF4F46E5),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFFFFFF),
                unselectedItemText = Color(0xFF64748B),
                navBarBg = Color(0xFF4F46E5),
                titleBarBg = Color(0xFF4F46E5),
                chipBg = Color(0xFFE0E7FF),
                isDark = false,
                themeName = "Indigo Cyan",
                themeNameBn = "ইনডিগো সায়ান"
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
            IRIS_GLOW -> CalculatorThemeColors(
                background = Color(0xFFF7F8FE),
                displayBackground = Color(0xFFEFF1FE),
                displayText = Color(0xFF1E2156),
                displayExpressionText = Color(0xFF53589B),
                buttonNormalBg = Color(0xFFFFFFFF),
                buttonNormalText = Color(0xFF1E2156),
                buttonOperatorBg = Color(0xFFEBEDFE),
                buttonOperatorText = Color(0xFF767FF6),
                buttonFunctionBg = Color(0xFFF3F4FE),
                buttonFunctionText = Color(0xFF5A63EA),
                buttonEqualBg = Color(0xFF767FF6),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFFFFFF),
                unselectedItemText = Color(0xFF53589B),
                navBarBg = Color(0xFF767FF6),
                titleBarBg = Color(0xFF767FF6),
                chipBg = Color(0xFFEBEDFE),
                isDark = false,
                themeName = "Iris Glow",
                themeNameBn = "আইরিস গ্লো"
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

