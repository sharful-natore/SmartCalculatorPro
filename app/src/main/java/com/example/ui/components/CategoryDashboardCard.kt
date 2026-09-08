package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.data.model.ToolCategory
import com.example.data.model.ToolType
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Metadata and visual styling for each category card.
 */
data class CategoryCardTheme(
    val bgLight: Color,
    val bgDark: Color,
    val accentColor: Color,
    val titleColorLight: Color,
    val subtitleColorLight: Color,
    val iconBadgeBgLight: Color,
    val iconBadgeTintLight: Color,
    val subtitleBn: String,
    val subtitleEn: String
)

object CategoryThemeRegistry {
    fun getTheme(category: ToolCategory): CategoryCardTheme {
        return when (category) {
            ToolCategory.HEALTH -> CategoryCardTheme(
                bgLight = Color(0xFFFFF1F2),
                bgDark = Color(0xFF381119),
                accentColor = Color(0xFFE11D48),
                titleColorLight = Color(0xFF881337),
                subtitleColorLight = Color(0xFF9F1239),
                iconBadgeBgLight = Color(0xFFFFE4E6),
                iconBadgeTintLight = Color(0xFFE11D48),
                subtitleBn = "বিএমআই, ক্যালোরি, আদর্শ ওজন, রক্তদান ও স্বাস্থ্য ট্র্যাকার",
                subtitleEn = "BMI, Calorie, Ideal Weight, Blood Donation & Health Trackers"
            )
            ToolCategory.FINANCE -> CategoryCardTheme(
                bgLight = Color(0xFFEFF6FF),
                bgDark = Color(0xFF101E38),
                accentColor = Color(0xFF2563EB),
                titleColorLight = Color(0xFF1E3A8A),
                subtitleColorLight = Color(0xFF1D4ED8),
                iconBadgeBgLight = Color(0xFFDBEAFE),
                iconBadgeTintLight = Color(0xFF2563EB),
                subtitleBn = "ঋণ ইএমআই, লাভ-ক্ষতি, ভ্যাট, যাকাত, সেভিংস ও সুদ হিসাব",
                subtitleEn = "EMI Loan, Profit & Loss, VAT, Zakat, Savings & Interest"
            )
            ToolCategory.ISLAMIC -> CategoryCardTheme(
                bgLight = Color(0xFFECFDF5),
                bgDark = Color(0xFF0A2B1D),
                accentColor = Color(0xFF059669),
                titleColorLight = Color(0xFF065F46),
                subtitleColorLight = Color(0xFF047857),
                iconBadgeBgLight = Color(0xFFD1FAE5),
                iconBadgeTintLight = Color(0xFF059669),
                subtitleBn = "নামাজের সময়সূচি, কিবলা কম্পাস, ডিজিটাল তসবিহ ও দোয়া",
                subtitleEn = "Prayer Times, Qibla Compass, Digital Tasbih & Duas"
            )
            ToolCategory.UTILITY -> CategoryCardTheme(
                bgLight = Color(0xFFECFEFF),
                bgDark = Color(0xFF0B2D33),
                accentColor = Color(0xFF0891B2),
                titleColorLight = Color(0xFF155E75),
                subtitleColorLight = Color(0xFF0E7490),
                iconBadgeBgLight = Color(0xFFCFFAFE),
                iconBadgeTintLight = Color(0xFF0891B2),
                subtitleBn = "বয়স ক্যালকুলেটর, বাজার লিস্ট, ক্যামেরা লেভেল ও প্রয়োজনীয় টুলস",
                subtitleEn = "Age Calculator, Market List, Camera Level & Essential Tools"
            )
            ToolCategory.ELECTRICITY -> CategoryCardTheme(
                bgLight = Color(0xFFFFFBEB),
                bgDark = Color(0xFF332009),
                accentColor = Color(0xFFD97706),
                titleColorLight = Color(0xFF78350F),
                subtitleColorLight = Color(0xFF92400E),
                iconBadgeBgLight = Color(0xFFFEF3C7),
                iconBadgeTintLight = Color(0xFFD97706),
                subtitleBn = "বিদ্যুৎ বিল, জেনারেটর, সোলার প্যানেল, ভোল্টেজ ও লোড ক্যালকুলেশন",
                subtitleEn = "Electricity Bill, Generator, Solar Panel, Voltage & Load"
            )
            ToolCategory.VEHICLE -> CategoryCardTheme(
                bgLight = Color(0xFFF0F9FF),
                bgDark = Color(0xFF0C2538),
                accentColor = Color(0xFF0284C7),
                titleColorLight = Color(0xFF075985),
                subtitleColorLight = Color(0xFF0369A1),
                iconBadgeBgLight = Color(0xFFE0F2FE),
                iconBadgeTintLight = Color(0xFF0284C7),
                subtitleBn = "মাইলেজ, ফুয়েল ট্রিপ খরচ, স্পিডোমিটার, পার্কিং ও টোল হিসাব",
                subtitleEn = "Mileage, Fuel Cost, Speedometer, Parking & Trip Cost"
            )
            ToolCategory.EDUCATION -> CategoryCardTheme(
                bgLight = Color(0xFFFAF5FF),
                bgDark = Color(0xFF2B123D),
                accentColor = Color(0xFF9333EA),
                titleColorLight = Color(0xFF581C87),
                subtitleColorLight = Color(0xFF6B21A8),
                iconBadgeBgLight = Color(0xFFF3E8FF),
                iconBadgeTintLight = Color(0xFF9333EA),
                subtitleBn = "জিপিএ, সিজিপিএ, শতকরা গ্রেড, উপস্থিতি ও ফলাফল হিসাব",
                subtitleEn = "GPA, CGPA, Percentage, Grade & Attendance Calculator"
            )
            ToolCategory.AGRICULTURE -> CategoryCardTheme(
                bgLight = Color(0xFFF0FDF4),
                bgDark = Color(0xFF0F2E1B),
                accentColor = Color(0xFF16A34A),
                titleColorLight = Color(0xFF14532D),
                subtitleColorLight = Color(0xFF166534),
                iconBadgeBgLight = Color(0xFFDCFCE7),
                iconBadgeTintLight = Color(0xFF16A34A),
                subtitleBn = "সার প্রয়োগ, জমির হিসাব, পুকুরের মাছ চাষ ও ফসল বীজ গণনা",
                subtitleEn = "Fertilizer, Land Measurement, Fish Farming & Seed Count"
            )
            ToolCategory.KIDS -> CategoryCardTheme(
                bgLight = Color(0xFFFDF2F8),
                bgDark = Color(0xFF3B0E23),
                accentColor = Color(0xFFDB2777),
                titleColorLight = Color(0xFF831843),
                subtitleColorLight = Color(0xFF9D174D),
                iconBadgeBgLight = Color(0xFFFCE7F3),
                iconBadgeTintLight = Color(0xFFDB2777),
                subtitleBn = "বর্ণমালা, ছোটদের অংক শেখা, ছড়া, নামতা ও মজার কুইজ",
                subtitleEn = "Alphabet, Math for Kids, Rhymes, Multiplication & Fun Quiz"
            )
            ToolCategory.DEVELOPER -> CategoryCardTheme(
                bgLight = Color(0xFFEEF2FF),
                bgDark = Color(0xFF141838),
                accentColor = Color(0xFF4F46E5),
                titleColorLight = Color(0xFF312E81),
                subtitleColorLight = Color(0xFF3730A3),
                iconBadgeBgLight = Color(0xFFE0E7FF),
                iconBadgeTintLight = Color(0xFF4F46E5),
                subtitleBn = "বেস কনভার্টার, কালার কোড পিকার, হ্যাশ ও রেগুলার এক্সপ্রেশন",
                subtitleEn = "Base Converter, Color Picker, Hash & Regex Tools"
            )
            ToolCategory.ENGINEERING -> CategoryCardTheme(
                bgLight = Color(0xFFF8FAFC),
                bgDark = Color(0xFF1B222E),
                accentColor = Color(0xFF475569),
                titleColorLight = Color(0xFF0F172A),
                subtitleColorLight = Color(0xFF1E293B),
                iconBadgeBgLight = Color(0xFFF1F5F9),
                iconBadgeTintLight = Color(0xFF334155),
                subtitleBn = "সিভিল, মেকানিক্যাল, পাইপ ফ্লো, বিম লোড ও স্ট্রাকচারাল গণনা",
                subtitleEn = "Civil, Mechanical, Pipe Flow, Beam Load & Structural Calculations"
            )
        }
    }
}

/**
 * Custom Canvas vector illustration background for each category.
 */
@Composable
fun CategoryIllustrationBackground(
    category: ToolCategory,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val theme = remember(category) { CategoryThemeRegistry.getTheme(category) }
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val accent = theme.accentColor

        when (category) {
            ToolCategory.HEALTH -> drawHealthIllustration(w, h, accent, isDark)
            ToolCategory.FINANCE -> drawFinanceIllustration(w, h, accent, isDark)
            ToolCategory.ISLAMIC -> drawIslamicIllustration(w, h, accent, isDark)
            ToolCategory.UTILITY -> drawUtilityIllustration(w, h, accent, isDark)
            ToolCategory.ELECTRICITY -> drawElectricityIllustration(w, h, accent, isDark)
            ToolCategory.VEHICLE -> drawVehicleIllustration(w, h, accent, isDark)
            ToolCategory.EDUCATION -> drawEducationIllustration(w, h, accent, isDark)
            ToolCategory.AGRICULTURE -> drawAgricultureIllustration(w, h, accent, isDark)
            ToolCategory.KIDS -> drawKidsIllustration(w, h, accent, isDark)
            ToolCategory.DEVELOPER -> drawDeveloperIllustration(w, h, accent, isDark)
            ToolCategory.ENGINEERING -> drawEngineeringIllustration(w, h, accent, isDark)
        }
    }
}

private fun DrawScope.drawHealthIllustration(w: Float, h: Float, accentColor: Color, isDark: Boolean) {
    val lineAlpha = if (isDark) 0.22f else 0.18f
    val strokeColor = accentColor.copy(alpha = lineAlpha)
    val fillAlpha = if (isDark) 0.12f else 0.08f
    val fillColor = accentColor.copy(alpha = fillAlpha)

    // ECG Heartbeat line across the card
    val path = Path().apply {
        moveTo(w * 0.05f, h * 0.75f)
        lineTo(w * 0.35f, h * 0.75f)
        lineTo(w * 0.40f, h * 0.60f)
        lineTo(w * 0.45f, h * 0.90f)
        lineTo(w * 0.51f, h * 0.30f)
        lineTo(w * 0.57f, h * 0.85f)
        lineTo(w * 0.62f, h * 0.70f)
        lineTo(w * 0.67f, h * 0.75f)
        lineTo(w * 0.95f, h * 0.75f)
    }
    drawPath(path, strokeColor, style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Faint Medical Cross Symbol on the right
    val cx = w * 0.80f
    val cy = h * 0.48f
    val crossSize = 30f
    drawRoundRect(
        color = fillColor,
        topLeft = Offset(cx - crossSize / 3, cy - crossSize),
        size = Size(crossSize * 2 / 3, crossSize * 2),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = fillColor,
        topLeft = Offset(cx - crossSize, cy - crossSize / 3),
        size = Size(crossSize * 2, crossSize * 2 / 3),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )

    // Heart pulse circles on the right
    drawCircle(accentColor.copy(alpha = if (isDark) 0.06f else 0.04f), radius = 55f, center = Offset(cx, cy))
    drawCircle(accentColor.copy(alpha = if (isDark) 0.12f else 0.08f), radius = 35f, center = Offset(cx, cy), style = Stroke(width = 1.5f))
}

private fun DrawScope.drawFinanceIllustration(w: Float, h: Float, accentColor: Color, isDark: Boolean) {
    val lineAlpha = if (isDark) 0.22f else 0.18f
    val strokeColor = accentColor.copy(alpha = lineAlpha)
    val fillAlpha = if (isDark) 0.12f else 0.08f
    val fillColor = accentColor.copy(alpha = fillAlpha)

    // Trending upward chart line
    val chartPath = Path().apply {
        moveTo(w * 0.15f, h * 0.85f)
        cubicTo(w * 0.35f, h * 0.80f, w * 0.45f, h * 0.55f, w * 0.60f, h * 0.50f)
        cubicTo(w * 0.70f, h * 0.45f, w * 0.75f, h * 0.30f, w * 0.92f, h * 0.22f)
    }
    drawPath(chartPath, strokeColor, style = Stroke(width = 3.5f, cap = StrokeCap.Round))

    // Chart bar columns
    val barWidth = 14f
    val bars = listOf(
        Pair(w * 0.50f, h * 0.38f),
        Pair(w * 0.58f, h * 0.50f),
        Pair(w * 0.66f, h * 0.65f),
        Pair(w * 0.74f, h * 0.78f)
    )
    bars.forEach { (x, barH) ->
        drawRoundRect(
            color = fillColor,
            topLeft = Offset(x, h - barH),
            size = Size(barWidth, barH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
    }

    // Glowing coin circles on the right
    val cx = w * 0.80f
    val cy = h * 0.45f
    drawCircle(accentColor.copy(alpha = if (isDark) 0.14f else 0.10f), radius = 30f, center = Offset(cx, cy))
    drawCircle(accentColor.copy(alpha = if (isDark) 0.22f else 0.16f), radius = 20f, center = Offset(cx, cy), style = Stroke(width = 2.5f))
    drawCircle(accentColor.copy(alpha = if (isDark) 0.22f else 0.16f), radius = 10f, center = Offset(cx, cy), style = Stroke(width = 1.5f))
}

private fun DrawScope.drawIslamicIllustration(w: Float, h: Float, accentColor: Color, isDark: Boolean) {
    val lineAlpha = if (isDark) 0.24f else 0.18f
    val strokeColor = accentColor.copy(alpha = lineAlpha)
    val fillAlpha = if (isDark) 0.12f else 0.08f
    val fillColor = accentColor.copy(alpha = fillAlpha)

    // Islamic Crescent Moon
    val moonCenterX = w * 0.80f
    val moonCenterY = h * 0.48f
    val moonRadius = 36f

    val outerCircle = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(moonCenterX - moonRadius, moonCenterY - moonRadius, moonCenterX + moonRadius, moonCenterY + moonRadius))
    }
    val innerCircle = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(moonCenterX - moonRadius + 14f, moonCenterY - moonRadius - 4f, moonCenterX + moonRadius + 6f, moonCenterY + moonRadius - 4f))
    }
    val crescent = Path().apply {
        op(outerCircle, innerCircle, PathOperation.Difference)
    }
    drawPath(crescent, strokeColor)

    // 8-pointed Islamic Star near moon
    drawIslamicStar(moonCenterX - 25f, moonCenterY - 22f, 10f, accentColor.copy(alpha = if (isDark) 0.26f else 0.20f))
    drawIslamicStar(w * 0.62f, h * 0.28f, 7f, accentColor.copy(alpha = if (isDark) 0.18f else 0.12f))
    drawIslamicStar(w * 0.45f, h * 0.72f, 8f, accentColor.copy(alpha = if (isDark) 0.15f else 0.10f))

    // Arch dome silhouette
    val domePath = Path().apply {
        moveTo(w * 0.60f, h)
        lineTo(w * 0.60f, h * 0.65f)
        cubicTo(w * 0.60f, h * 0.48f, w * 0.72f, h * 0.40f, w * 0.72f, h * 0.35f)
        cubicTo(w * 0.72f, h * 0.40f, w * 0.84f, h * 0.48f, w * 0.84f, h * 0.65f)
        lineTo(w * 0.84f, h)
    }
    drawPath(domePath, fillColor)
}

private fun DrawScope.drawIslamicStar(cx: Float, cy: Float, radius: Float, color: Color) {
    val path1 = Path().apply {
        for (i in 0 until 4) {
            val angle = i * PI.toFloat() / 2f
            val x = cx + radius * cos(angle)
            val y = cy + radius * sin(angle)
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    val path2 = Path().apply {
        for (i in 0 until 4) {
            val angle = i * PI.toFloat() / 2f + (PI.toFloat() / 4f)
            val x = cx + radius * cos(angle)
            val y = cy + radius * sin(angle)
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    drawPath(path1, color)
    drawPath(path2, color)
}

private fun DrawScope.drawUtilityIllustration(w: Float, h: Float, accentColor: Color, isDark: Boolean) {
    val lineAlpha = if (isDark) 0.22f else 0.18f
    val strokeColor = accentColor.copy(alpha = lineAlpha)
    val fillAlpha = if (isDark) 0.12f else 0.08f
    val fillColor = accentColor.copy(alpha = fillAlpha)

    // Interlocking gear geometry
    val cx = w * 0.80f
    val cy = h * 0.48f
    val r = 38f

    drawCircle(fillColor, radius = r, center = Offset(cx, cy))
    drawCircle(strokeColor, radius = r * 0.55f, center = Offset(cx, cy), style = Stroke(width = 3f))

    // Gear cogs
    for (i in 0 until 8) {
        val angle = i * (PI.toFloat() / 4f)
        val toothX = cx + (r + 5f) * cos(angle)
        val toothY = cy + (r + 5f) * sin(angle)
        drawCircle(accentColor.copy(alpha = if (isDark) 0.20f else 0.14f), radius = 6f, center = Offset(toothX, toothY))
    }

    // Ruler measurement ticks along top right
    for (i in 0 until 8) {
        val x = w * 0.48f + i * 14f
        val len = if (i % 2 == 0) 14f else 7f
        drawLine(
            color = strokeColor,
            start = Offset(x, h * 0.18f),
            end = Offset(x, h * 0.18f + len),
            strokeWidth = 2.5f
        )
    }
}

private fun DrawScope.drawElectricityIllustration(w: Float, h: Float, accentColor: Color, isDark: Boolean) {
    val lineAlpha = if (isDark) 0.25f else 0.18f
    val strokeColor = accentColor.copy(alpha = lineAlpha)
    val fillAlpha = if (isDark) 0.15f else 0.10f
    val fillColor = accentColor.copy(alpha = fillAlpha)

    val cx = w * 0.80f
    val cy = h * 0.48f

    // High-voltage lightning bolt
    val boltPath = Path().apply {
        moveTo(cx + 10f, h * 0.15f)
        lineTo(cx - 12f, cy + 2f)
        lineTo(cx + 2f, cy + 2f)
        lineTo(cx - 15f, h * 0.85f)
        lineTo(cx + 18f, cy - 2f)
        lineTo(cx + 4f, cy - 2f)
        close()
    }
    drawPath(boltPath, fillColor)
    drawPath(boltPath, strokeColor, style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Electric energy rings
    drawCircle(accentColor.copy(alpha = if (isDark) 0.08f else 0.05f), radius = 54f, center = Offset(cx, cy))
    drawCircle(accentColor.copy(alpha = if (isDark) 0.16f else 0.12f), radius = 70f, center = Offset(cx, cy), style = Stroke(width = 2f))
}

private fun DrawScope.drawVehicleIllustration(w: Float, h: Float, accentColor: Color, isDark: Boolean) {
    val lineAlpha = if (isDark) 0.24f else 0.18f
    val strokeColor = accentColor.copy(alpha = lineAlpha)
    val fillAlpha = if (isDark) 0.12f else 0.08f
    val fillColor = accentColor.copy(alpha = fillAlpha)

    // Speedometer Arc
    val cx = w * 0.80f
    val cy = h * 0.55f
    val r = 42f

    drawArc(
        color = strokeColor,
        startAngle = 140f,
        sweepAngle = 260f,
        useCenter = false,
        topLeft = Offset(cx - r, cy - r),
        size = Size(r * 2, r * 2),
        style = Stroke(width = 4.5f, cap = StrokeCap.Round)
    )

    // Gauge Needle
    val needleAngle = 220f * (PI.toFloat() / 180f)
    drawLine(
        color = accentColor.copy(alpha = if (isDark) 0.35f else 0.25f),
        start = Offset(cx, cy),
        end = Offset(cx + (r - 4f) * cos(needleAngle), cy + (r - 4f) * sin(needleAngle)),
        strokeWidth = 3.5f,
        cap = StrokeCap.Round
    )
    drawCircle(accentColor.copy(alpha = if (isDark) 0.30f else 0.20f), radius = 6f, center = Offset(cx, cy))

    // Motion speed lines
    for (i in 0 until 4) {
        val y = h * 0.32f + i * 11f
        val startX = w * 0.45f + i * 8f
        drawLine(
            color = strokeColor,
            start = Offset(startX, y),
            end = Offset(startX + 45f, y),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawEducationIllustration(w: Float, h: Float, accentColor: Color, isDark: Boolean) {
    val lineAlpha = if (isDark) 0.24f else 0.18f
    val strokeColor = accentColor.copy(alpha = lineAlpha)
    val fillAlpha = if (isDark) 0.12f else 0.08f
    val fillColor = accentColor.copy(alpha = fillAlpha)

    // Graduation Cap (Mortarboard)
    val cx = w * 0.80f
    val cy = h * 0.40f
    val capPath = Path().apply {
        moveTo(cx, cy - 16f)
        lineTo(cx + 34f, cy)
        lineTo(cx, cy + 16f)
        lineTo(cx - 34f, cy)
        close()
    }
    drawPath(capPath, fillColor)
    drawPath(capPath, strokeColor, style = Stroke(width = 2.5f, join = StrokeJoin.Round))

    // Cap base
    val baseArch = Path().apply {
        moveTo(cx - 18f, cy + 8f)
        lineTo(cx - 18f, cy + 20f)
        cubicTo(cx - 18f, cy + 28f, cx + 18f, cy + 20f, cx + 18f, cy + 8f)
    }
    drawPath(baseArch, fillColor)
    drawPath(baseArch, strokeColor, style = Stroke(width = 2f))

    // Tassel string
    val tassel = Path().apply {
        moveTo(cx, cy)
        lineTo(cx + 32f, cy + 11f)
        lineTo(cx + 32f, cy + 26f)
    }
    drawPath(tassel, strokeColor, style = Stroke(width = 2.5f, cap = StrokeCap.Round))

    // Open book pages at bottom
    val bookPath = Path().apply {
        moveTo(w * 0.58f, h * 0.85f)
        cubicTo(w * 0.66f, h * 0.79f, w * 0.71f, h * 0.81f, w * 0.76f, h * 0.86f)
        cubicTo(w * 0.81f, h * 0.81f, w * 0.86f, h * 0.79f, w * 0.94f, h * 0.85f)
    }
    drawPath(bookPath, strokeColor, style = Stroke(width = 3f, cap = StrokeCap.Round))
}

private fun DrawScope.drawAgricultureIllustration(w: Float, h: Float, accentColor: Color, isDark: Boolean) {
    val lineAlpha = if (isDark) 0.24f else 0.18f
    val strokeColor = accentColor.copy(alpha = lineAlpha)
    val fillAlpha = if (isDark) 0.12f else 0.08f
    val fillColor = accentColor.copy(alpha = fillAlpha)

    val cx = w * 0.80f
    val cy = h * 0.50f

    // Main stalk
    drawLine(
        color = strokeColor,
        start = Offset(cx, h * 0.88f),
        end = Offset(cx, h * 0.22f),
        strokeWidth = 3f,
        cap = StrokeCap.Round
    )

    // Wheat grains on both sides (highly detailed)
    for (i in 0 until 5) {
        val y = h * 0.28f + i * 12f
        // Left grain
        drawOval(
            color = fillColor,
            topLeft = Offset(cx - 18f, y - 4f),
            size = Size(16f, 9f)
        )
        drawOval(
            color = strokeColor,
            topLeft = Offset(cx - 18f, y - 4f),
            size = Size(16f, 9f),
            style = Stroke(width = 1.5f)
        )
        // Right grain
        drawOval(
            color = fillColor,
            topLeft = Offset(cx + 2f, y - 8f),
            size = Size(16f, 9f)
        )
        drawOval(
            color = strokeColor,
            topLeft = Offset(cx + 2f, y - 8f),
            size = Size(16f, 9f),
            style = Stroke(width = 1.5f)
        )
    }

    // Furrow hills curve at bottom
    val hillPath1 = Path().apply {
        moveTo(w * 0.35f, h)
        cubicTo(w * 0.52f, h * 0.72f, w * 0.68f, h * 0.82f, w * 0.95f, h * 0.68f)
    }
    val hillPath2 = Path().apply {
        moveTo(w * 0.45f, h)
        cubicTo(w * 0.60f, h * 0.80f, w * 0.75f, h * 0.88f, w * 0.95f, h * 0.78f)
    }
    drawPath(hillPath1, strokeColor, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
    drawPath(hillPath2, strokeColor, style = Stroke(width = 1.5f, cap = StrokeCap.Round))
}

private fun DrawScope.drawKidsIllustration(w: Float, h: Float, accentColor: Color, isDark: Boolean) {
    val lineAlpha = if (isDark) 0.24f else 0.18f
    val strokeColor = accentColor.copy(alpha = lineAlpha)
    val fillAlpha = if (isDark) 0.12f else 0.08f
    val fillColor = accentColor.copy(alpha = fillAlpha)

    // Colorful floating balloons with strings
    val b1x = w * 0.76f
    val b1y = h * 0.34f
    drawOval(fillColor, topLeft = Offset(b1x - 15f, b1y - 20f), size = Size(30f, 40f))
    drawOval(strokeColor, topLeft = Offset(b1x - 15f, b1y - 20f), size = Size(30f, 40f), style = Stroke(width = 2.5f))
    drawLine(strokeColor, start = Offset(b1x, b1y + 20f), end = Offset(b1x - 10f, b1y + 54f), strokeWidth = 2f)

    val b2x = w * 0.86f
    val b2y = h * 0.46f
    drawOval(fillColor, topLeft = Offset(b2x - 13f, b2y - 18f), size = Size(26f, 36f))
    drawOval(strokeColor, topLeft = Offset(b2x - 13f, b2y - 18f), size = Size(26f, 36f), style = Stroke(width = 2f))
    drawLine(strokeColor, start = Offset(b2x, b2y + 18f), end = Offset(b2x - 5f, b2y + 46f), strokeWidth = 1.5f)

    // Playful starbursts & Twinkle stars
    drawCircle(strokeColor, radius = 6f, center = Offset(w * 0.62f, h * 0.28f))
    drawCircle(fillColor, radius = 10f, center = Offset(w * 0.58f, h * 0.68f))
}

private fun DrawScope.drawDeveloperIllustration(w: Float, h: Float, accentColor: Color, isDark: Boolean) {
    val lineAlpha = if (isDark) 0.24f else 0.18f
    val strokeColor = accentColor.copy(alpha = lineAlpha)
    val fillAlpha = if (isDark) 0.12f else 0.08f
    val fillColor = accentColor.copy(alpha = fillAlpha)

    val cx = w * 0.80f
    val cy = h * 0.45f

    // Terminal window outline
    drawRoundRect(
        color = fillColor,
        topLeft = Offset(cx - 38f, cy - 26f),
        size = Size(76f, 52f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = strokeColor,
        topLeft = Offset(cx - 38f, cy - 26f),
        size = Size(76f, 52f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
        style = Stroke(width = 2.5f)
    )

    // Top window dots
    drawCircle(strokeColor, radius = 3f, center = Offset(cx - 26f, cy - 18f))
    drawCircle(strokeColor, radius = 3f, center = Offset(cx - 16f, cy - 18f))
    drawCircle(strokeColor, radius = 3f, center = Offset(cx - 6f, cy - 18f))

    // Prompt > _
    val promptPath = Path().apply {
        moveTo(cx - 26f, cy - 3f)
        lineTo(cx - 18f, cy + 4f)
        lineTo(cx - 26f, cy + 11f)
    }
    drawPath(promptPath, strokeColor, style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawLine(
        color = strokeColor,
        start = Offset(cx - 12f, cy + 11f),
        end = Offset(cx - 2f, cy + 11f),
        strokeWidth = 3f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawEngineeringIllustration(w: Float, h: Float, accentColor: Color, isDark: Boolean) {
    val lineAlpha = if (isDark) 0.24f else 0.18f
    val strokeColor = accentColor.copy(alpha = lineAlpha)
    val fillAlpha = if (isDark) 0.12f else 0.08f
    val fillColor = accentColor.copy(alpha = fillAlpha)

    val cx = w * 0.80f
    val cy = h * 0.45f

    // Grid lines
    for (i in 0 until 5) {
        val y = cy - 28f + i * 14f
        drawLine(strokeColor.copy(alpha = lineAlpha * 0.4f), start = Offset(cx - 38f, y), end = Offset(cx + 38f, y), strokeWidth = 1f)
    }
    for (i in 0 until 5) {
        val x = cx - 28f + i * 14f
        drawLine(strokeColor.copy(alpha = lineAlpha * 0.4f), start = Offset(x, cy - 34f), end = Offset(x, cy + 34f), strokeWidth = 1f)
    }

    // Compass angle divider
    val compass = Path().apply {
        moveTo(cx - 20f, cy + 22f)
        lineTo(cx, cy - 18f)
        lineTo(cx + 20f, cy + 22f)
    }
    drawPath(compass, strokeColor, style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawCircle(strokeColor, radius = 4f, center = Offset(cx, cy - 18f))
}

/**
 * The redesigned, high-end soft-background Category Card with round icon badge & stacked tool icons.
 */
@Composable
fun CategoryDashboardCard(
    category: ToolCategory,
    categoryTools: List<ToolType>,
    language: AppLanguage,
    isBn: Boolean,
    themeColors: CalculatorThemeColors? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val theme = remember(category) { CategoryThemeRegistry.getTheme(category) }
    val interactionSource = remember { MutableInteractionSource() }

    val top3Tools = remember(categoryTools) { categoryTools.take(3) }
    val remainingCount = remember(categoryTools) { (categoryTools.size - 3).coerceAtLeast(0) }

    val isDark = themeColors?.isDark == true
    val cardBgColor = if (isDark) theme.bgDark else theme.bgLight
    val titleTextColor = if (isDark) Color.White else theme.titleColorLight
    val subtitleTextColor = if (isDark) Color.White.copy(alpha = 0.82f) else theme.subtitleColorLight
    val iconBadgeBg = if (isDark) theme.accentColor.copy(alpha = 0.25f) else theme.iconBadgeBgLight
    val iconBadgeTint = if (isDark) theme.accentColor else theme.iconBadgeTintLight
    val cardBorderColor = theme.accentColor.copy(alpha = if (isDark) 0.35f else 0.25f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("category_card_${category.name.lowercase()}")
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                onClick = onClick
            ),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBgColor)
        ) {
            // Background artistic illustration matching the category
            CategoryIllustrationBackground(
                category = category,
                isDark = isDark,
                modifier = Modifier.matchParentSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 9.dp)
            ) {
                // Top Row: Category Icon Badge on Left & Tool Icons Stack on Top Right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Icon Badge - Round Shape
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(iconBadgeBg)
                            .border(0.8.dp, theme.accentColor.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = category.getTitle(language),
                            tint = iconBadgeTint,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Tool Icons Stack on Top Right (Gaps reduced / tightly stacked: spacedBy(-10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy((-10).dp)
                    ) {
                        top3Tools.forEachIndexed { index, tool ->
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .zIndex((4 - index).toFloat())
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(0.8.dp, theme.accentColor.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = tool.icon,
                                    contentDescription = tool.getTitle(language),
                                    tint = theme.accentColor,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        if (remainingCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .zIndex(0f)
                                    .clip(CircleShape)
                                    .background(theme.accentColor.copy(alpha = 0.18f))
                                    .border(0.8.dp, theme.accentColor.copy(alpha = 0.40f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+$remainingCount",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = theme.accentColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Title & Subtitle
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = category.getTitle(language),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = titleTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(1.5.dp))

                    Text(
                        text = if (isBn) theme.subtitleBn else theme.subtitleEn,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = subtitleTextColor,
                        maxLines = 2,
                        lineHeight = 12.5.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun isTitleLong(title: String, isBn: Boolean): Boolean {
    val threshold = if (isBn) 16 else 18
    return title.length > threshold
}

private fun isSubtitleLong(subtitle: String, isBn: Boolean): Boolean {
    val threshold = if (isBn) 28 else 32
    return subtitle.length > threshold
}

/**
 * Filtered Category View when a specific Category Card is tapped.
 */
@Composable
fun CategoryDetailToolsView(
    category: ToolCategory,
    categoryTools: List<ToolType>,
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onBackClick: () -> Unit
) {
    val theme = remember(category) { CategoryThemeRegistry.getTheme(category) }
    var inCategorySearchQuery by remember { mutableStateOf("") }

    val filteredTools = remember(categoryTools, inCategorySearchQuery, isBn) {
        if (inCategorySearchQuery.isBlank()) {
            categoryTools
        } else {
            val q = inCategorySearchQuery.trim().lowercase()
            categoryTools.filter { tool ->
                tool.titleEn.lowercase().contains(q) ||
                tool.titleBn.lowercase().contains(q) ||
                tool.descriptionBn.lowercase().contains(q)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero Category Header Banner
        val isDark = themeColors.isDark
        val headerBg = if (isDark) theme.bgDark else theme.bgLight
        val headerTitleColor = if (isDark) Color.White else theme.titleColorLight
        val headerSubtitleColor = if (isDark) Color.White.copy(alpha = 0.85f) else theme.subtitleColorLight
        val iconBadgeBg = if (isDark) theme.accentColor.copy(alpha = 0.25f) else theme.iconBadgeBgLight
        val iconBadgeTint = if (isDark) theme.accentColor else theme.iconBadgeTintLight

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, theme.accentColor.copy(alpha = if (isDark) 0.35f else 0.25f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBg)
            ) {
                CategoryIllustrationBackground(
                    category = category,
                    isDark = isDark,
                    modifier = Modifier.matchParentSize()
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // Back Button & All Categories Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = onBackClick,
                            shape = RoundedCornerShape(12.dp),
                            color = theme.accentColor.copy(alpha = if (isDark) 0.25f else 0.15f),
                            border = BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = if (isDark) Color.White else theme.titleColorLight,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (isBn) "সকল ক্যাটাগরি" else "All Categories",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else theme.titleColorLight
                                )
                            }
                        }

                        // Total count pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = theme.accentColor.copy(alpha = if (isDark) 0.20f else 0.12f)
                        ) {
                            Text(
                                text = if (isBn) "${categoryTools.size}টি টুলস" else "${categoryTools.size} Tools",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else theme.titleColorLight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(iconBadgeBg)
                                .border(1.dp, theme.accentColor.copy(alpha = 0.35f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                tint = iconBadgeTint,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = category.getTitle(viewModel.selectedLanguage),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = headerTitleColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isBn) theme.subtitleBn else theme.subtitleEn,
                                fontSize = 11.sp,
                                color = headerSubtitleColor,
                                lineHeight = 14.5.sp
                            )
                        }
                    }
                }
            }
        }

        // Search Bar for filtering within this category
        if (categoryTools.size > 4) {
            OutlinedTextField(
                value = inCategorySearchQuery,
                onValueChange = { inCategorySearchQuery = it },
                placeholder = {
                    Text(
                        text = if (isBn) "এই ক্যাটাগরিতে টুল খুঁজুন..." else "Search in ${category.getTitle(viewModel.selectedLanguage)}...",
                        color = themeColors.displayText.copy(alpha = 0.5f),
                        fontSize = 12.5.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = themeColors.displayText.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (inCategorySearchQuery.isNotEmpty()) {
                        IconButton(onClick = { inCategorySearchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = themeColors.displayText.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = themeColors.cardBg,
                    unfocusedContainerColor = themeColors.cardBg,
                    focusedBorderColor = themeColors.buttonEqualBg,
                    unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f),
                    focusedTextColor = themeColors.displayText,
                    unfocusedTextColor = themeColors.displayText
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Quick Category Switcher Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ToolCategory.values().forEach { cat ->
                val isSelected = cat == category
                Surface(
                    onClick = { viewModel.selectedToolCategoryFilter = cat },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) themeColors.buttonEqualBg else themeColors.cardBg,
                    border = if (!isSelected) BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)) else null
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = cat.icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else themeColors.displayText.copy(alpha = 0.7f),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = cat.getTitle(viewModel.selectedLanguage),
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else themeColors.displayText
                        )
                    }
                }
            }
        }

        // 2-Column Grid of Tools
        if (filteredTools.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isBn) "কোনো টুল পাওয়া যায়নি" else "No tools found",
                    color = themeColors.displayText.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            }
        } else {
            val categoryToolRankMap = remember(filteredTools) {
                filteredTools.mapIndexed { index, tool -> tool.name to (if (index < 3) index + 1 else 0) }.toMap()
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                filteredTools.chunked(2).forEach { rowItems ->
                    val isAnyTitleLongInRow = rowItems.any { isTitleLong(it.getTitle(viewModel.selectedLanguage), isBn) }
                    val isAnySubtitleLongInRow = rowItems.any { isSubtitleLong(it.getDescription(viewModel.selectedLanguage), isBn) }
                    val rowTitleLines = if (isAnyTitleLongInRow) 2 else 1
                    val rowSubtitleLines = if (isAnySubtitleLongInRow) 2 else 1

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { tool ->
                            key(tool.name) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                ) {
                                    com.example.ui.screens.ToolGridCardItem(
                                        toolType = tool,
                                        viewModel = viewModel,
                                        themeColors = themeColors,
                                        modifier = Modifier.fillMaxHeight(),
                                        categoryRank = categoryToolRankMap[tool.name] ?: 0,
                                        titleLines = rowTitleLines,
                                        subtitleLines = rowSubtitleLines,
                                        onClick = { viewModel.openTool(tool) }
                                    )
                                }
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
