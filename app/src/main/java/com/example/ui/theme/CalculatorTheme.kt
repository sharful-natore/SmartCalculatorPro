package com.example.ui.theme

import androidx.compose.ui.graphics.Color

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
    val isDark: Boolean,
    val themeName: String,
    val themeNameBn: String
)

enum class CalculatorThemeType {
    SLEEK_INTERFACE,
    CLASSIC_DARK,
    CLASSIC_LIGHT,
    PASTEL_PEACH,
    RETRO_MONO,
    VIBRANT_SUNSET,
    NORDIC_FROST;

    fun getColors(): CalculatorThemeColors {
        return when (this) {
            SLEEK_INTERFACE -> CalculatorThemeColors(
                background = Color(0xFFF6F8FA),
                displayBackground = Color(0xFFF6F8FA),
                displayText = Color(0xFF1F1F1F),
                displayExpressionText = Color(0xFF5F6368),
                buttonNormalBg = Color(0xFFFFFFFF),
                buttonNormalText = Color(0xFF1F1F1F),
                buttonOperatorBg = Color(0xFFE8DDFF),
                buttonOperatorText = Color(0xFF6200EE),
                buttonFunctionBg = Color(0xFFE8DDFF),
                buttonFunctionText = Color(0xFF6200EE),
                buttonEqualBg = Color(0xFF6200EE),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFFFFFF),
                isDark = false,
                themeName = "Sleek Interface",
                themeNameBn = "স্লিক ইন্টারফেস"
            )
            CLASSIC_DARK -> CalculatorThemeColors(
                background = Color(0xFF121212),
                displayBackground = Color(0xFF1E1E1E),
                displayText = Color(0xFFFFFFFF),
                displayExpressionText = Color(0xFFAAAAAA),
                buttonNormalBg = Color(0xFF252525),
                buttonNormalText = Color(0xFFF3F3F3),
                buttonOperatorBg = Color(0xFFFF9F0A),
                buttonOperatorText = Color(0xFFFFFFFF),
                buttonFunctionBg = Color(0xFF3A3A3C),
                buttonFunctionText = Color(0xFFE5E5EA),
                buttonEqualBg = Color(0xFF0A84FF),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFF1C1C1E),
                isDark = true,
                themeName = "Classic Dark",
                themeNameBn = "ক্ল্যাসিক ডার্ক"
            )
            CLASSIC_LIGHT -> CalculatorThemeColors(
                background = Color(0xFFF2F2F7),
                displayBackground = Color(0xFFFFFFFF),
                displayText = Color(0xFF1C1C1E),
                displayExpressionText = Color(0xFF8E8E93),
                buttonNormalBg = Color(0xFFE5E5EA),
                buttonNormalText = Color(0xFF1C1C1E),
                buttonOperatorBg = Color(0xFFFF9500),
                buttonOperatorText = Color(0xFFFFFFFF),
                buttonFunctionBg = Color(0xFFD1D1D6),
                buttonFunctionText = Color(0xFF1C1C1E),
                buttonEqualBg = Color(0xFF007AFF),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFFFFFF),
                isDark = false,
                themeName = "Classic Light",
                themeNameBn = "ক্ল্যাসিক লাইট"
            )
            PASTEL_PEACH -> CalculatorThemeColors(
                background = Color(0xFFFFF0EC),
                displayBackground = Color(0xFFFFE5DF),
                displayText = Color(0xFF5D4037),
                displayExpressionText = Color(0xFF8D6E63),
                buttonNormalBg = Color(0xFFFFDCD3),
                buttonNormalText = Color(0xFF5D4037),
                buttonOperatorBg = Color(0xFFFFB399),
                buttonOperatorText = Color(0xFF5D4037),
                buttonFunctionBg = Color(0xFFF5C6BA),
                buttonFunctionText = Color(0xFF5D4037),
                buttonEqualBg = Color(0xFFE6A18C),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFFF7F5),
                isDark = false,
                themeName = "Pastel Peach",
                themeNameBn = "প্যাস্টেল পিচ"
            )
            RETRO_MONO -> CalculatorThemeColors(
                background = Color(0xFF000000),
                displayBackground = Color(0xFF050F05),
                displayText = Color(0xFF39FF14),
                displayExpressionText = Color(0xFF1E8210),
                buttonNormalBg = Color(0xFF0C0C0C),
                buttonNormalText = Color(0xFF39FF14),
                buttonOperatorBg = Color(0xFF151515),
                buttonOperatorText = Color(0xFF39FF14),
                buttonFunctionBg = Color(0xFF101010),
                buttonFunctionText = Color(0xFF00E676),
                buttonEqualBg = Color(0xFF39FF14),
                buttonEqualText = Color(0xFF000000),
                cardBg = Color(0xFF0A0A0A),
                isDark = true,
                themeName = "Retro Terminal",
                themeNameBn = "রেট্রো টার্মিনাল"
            )
            VIBRANT_SUNSET -> CalculatorThemeColors(
                background = Color(0xFF1A0B2E),
                displayBackground = Color(0xFF2C134E),
                displayText = Color(0xFFFFD000),
                displayExpressionText = Color(0xFFFF7BB0),
                buttonNormalBg = Color(0xFF3D1D6D),
                buttonNormalText = Color(0xFFFFFFFF),
                buttonOperatorBg = Color(0xFFFF4B91),
                buttonOperatorText = Color(0xFFFFFFFF),
                buttonFunctionBg = Color(0xFF52288C),
                buttonFunctionText = Color(0xFFFFB0D0),
                buttonEqualBg = Color(0xFFFF763B),
                buttonEqualText = Color(0xFFFFFFFF),
                cardBg = Color(0xFF2E1552),
                isDark = true,
                themeName = "Vibrant Sunset",
                themeNameBn = "ভাইব্রেন্ট সানসেট"
            )
            NORDIC_FROST -> CalculatorThemeColors(
                background = Color(0xFF242B35),
                displayBackground = Color(0xFF1B222A),
                displayText = Color(0xFFE5ECF4),
                displayExpressionText = Color(0xFF88A0B5),
                buttonNormalBg = Color(0xFF2E3846),
                buttonNormalText = Color(0xFFE5ECF4),
                buttonOperatorBg = Color(0xFF4C566A),
                buttonOperatorText = Color(0xFFE5ECF4),
                buttonFunctionBg = Color(0xFF3B4252),
                buttonFunctionText = Color(0xFF88C0D0),
                buttonEqualBg = Color(0xFF81A1C1),
                buttonEqualText = Color(0xFF2E3440),
                cardBg = Color(0xFF2B333F),
                isDark = true,
                themeName = "Nordic Frost",
                themeNameBn = "নরডিক ফ্রস্ট"
            )
        }
    }
}
