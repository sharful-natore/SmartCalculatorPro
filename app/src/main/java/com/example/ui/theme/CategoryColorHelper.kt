package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.data.model.ConverterCategory
import com.example.data.model.ToolCategory

/**
 * Returns the distinctive primary accent color for a ConverterCategory.
 */
fun ConverterCategory.getColor(): Color {
    return when (this) {
        ConverterCategory.COMMON -> Color(0xFF0284C7)      // Sky / Ocean Blue
        ConverterCategory.ENGINEERING -> Color(0xFFEA580C) // Radiant Orange
        ConverterCategory.TECHNOLOGY -> Color(0xFF7C3AED)  // Modern Violet
        ConverterCategory.MOTION -> Color(0xFF0D9488)      // Teal / Mint
        ConverterCategory.ELECTRICITY -> Color(0xFFD97706) // Electric Amber
        ConverterCategory.MISC -> Color(0xFFDB2777)        // Rose Pink
    }
}

/**
 * Returns a rich 3-step linear gradient brush based on the category color.
 */
fun ConverterCategory.getGradient(): Brush {
    return getToolIconGradient(getColor())
}

/**
 * Returns the distinctive primary accent color for a ToolCategory.
 */
fun ToolCategory.getColor(): Color {
    return when (this) {
        ToolCategory.HEALTH -> Color(0xFFE11D48)      // Vibrant Rose / Crimson
        ToolCategory.FINANCE -> Color(0xFF059669)     // Emerald / Currency Green
        ToolCategory.ISLAMIC -> Color(0xFF0D9488)     // Islamic Teal / Jade Green
        ToolCategory.UTILITY -> Color(0xFF2563EB)     // Royal Blue / Utility
        ToolCategory.ELECTRICITY -> Color(0xFFD97706) // Electric Amber / Gold
        ToolCategory.VEHICLE -> Color(0xFFEA580C)     // Warm Tangerine / Orange
        ToolCategory.EDUCATION -> Color(0xFF7C3AED)   // Academic Violet / Purple
        ToolCategory.DEVELOPER -> Color(0xFF4F46E5)   // Developer Indigo
        ToolCategory.ENGINEERING -> Color(0xFF0891B2) // Cyan / Steel
    }
}

/**
 * Returns a rich 3-step linear gradient brush based on the tool category color.
 */
fun ToolCategory.getGradient(): Brush {
    return getToolIconGradient(getColor())
}
