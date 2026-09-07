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
    val gradients: List<Color>,
    val accentColor: Color,
    val subtitleBn: String,
    val subtitleEn: String
)

object CategoryThemeRegistry {
    fun getTheme(category: ToolCategory): CategoryCardTheme {
        return when (category) {
            ToolCategory.HEALTH -> CategoryCardTheme(
                gradients = listOf(
                    Color(0xFF881337),
                    Color(0xFFBE123C),
                    Color(0xFFE11D48),
                    Color(0xFFFB7185)
                ),
                accentColor = Color(0xFFFDA4AF),
                subtitleBn = "বিএমআই, ক্যালোরি, আদর্শ ওজন, রক্তদান ও স্বাস্থ্য ট্র্যাকার",
                subtitleEn = "BMI, Calorie, Ideal Weight, Blood Donation & Health Trackers"
            )
            ToolCategory.FINANCE -> CategoryCardTheme(
                gradients = listOf(
                    Color(0xFF0F172A),
                    Color(0xFF1E3A8A),
                    Color(0xFF1D4ED8),
                    Color(0xFF3B82F6)
                ),
                accentColor = Color(0xFFFBBF24),
                subtitleBn = "ঋণ ইএমআই, লাভ-ক্ষতি, ভ্যাট, যাকাত, সেভিংস ও সুদ হিসাব",
                subtitleEn = "EMI Loan, Profit & Loss, VAT, Zakat, Savings & Interest"
            )
            ToolCategory.ISLAMIC -> CategoryCardTheme(
                gradients = listOf(
                    Color(0xFF022C22),
                    Color(0xFF064E3B),
                    Color(0xFF047857),
                    Color(0xFF10B981)
                ),
                accentColor = Color(0xFFFCD34D),
                subtitleBn = "নামাজের সময়সূচি, কিবলা কম্পাস, ডিজিটাল তসবিহ ও দোয়া",
                subtitleEn = "Prayer Times, Qibla Compass, Digital Tasbih & Duas"
            )
            ToolCategory.UTILITY -> CategoryCardTheme(
                gradients = listOf(
                    Color(0xFF042F2E),
                    Color(0xFF0F766E),
                    Color(0xFF0E7490),
                    Color(0xFF06B6D4)
                ),
                accentColor = Color(0xFF67E8F9),
                subtitleBn = "বয়স ক্যালকুলেটর, বাজার লিস্ট, ক্যামেরা লেভেল ও প্রয়োজনীয় টুলস",
                subtitleEn = "Age Calculator, Market List, Camera Level & Essential Tools"
            )
            ToolCategory.ELECTRICITY -> CategoryCardTheme(
                gradients = listOf(
                    Color(0xFF451A03),
                    Color(0xFF7C2D12),
                    Color(0xFFC2410C),
                    Color(0xFFF97316)
                ),
                accentColor = Color(0xFFFDE047),
                subtitleBn = "বিদ্যুৎ বিল, জেনারেটর, সোলার প্যানেল, ভোল্টেজ ও লোড ক্যালকুলেশন",
                subtitleEn = "Electricity Bill, Generator, Solar Panel, Voltage & Load"
            )
            ToolCategory.VEHICLE -> CategoryCardTheme(
                gradients = listOf(
                    Color(0xFF0F172A),
                    Color(0xFF1E293B),
                    Color(0xFF0369A1),
                    Color(0xFF0EA5E9)
                ),
                accentColor = Color(0xFF7DD3FC),
                subtitleBn = "মাইলেজ, ফুয়েল ট্রিপ খরচ, স্পিডোমিটার, পার্কিং ও টোল হিসাব",
                subtitleEn = "Mileage, Fuel Cost, Speedometer, Parking & Trip Cost"
            )
            ToolCategory.EDUCATION -> CategoryCardTheme(
                gradients = listOf(
                    Color(0xFF3B0764),
                    Color(0xFF581C87),
                    Color(0xFF7E22CE),
                    Color(0xFFA855F7)
                ),
                accentColor = Color(0xFFF472B6),
                subtitleBn = "জিপিএ, সিজিপিএ, শতকরা গ্রেড, উপস্থিতি ও ফলাফল হিসাব",
                subtitleEn = "GPA, CGPA, Percentage, Grade & Attendance Calculator"
            )
            ToolCategory.AGRICULTURE -> CategoryCardTheme(
                gradients = listOf(
                    Color(0xFF052E16),
                    Color(0xFF14532D),
                    Color(0xFF15803D),
                    Color(0xFF22C55E)
                ),
                accentColor = Color(0xFF86EFAC),
                subtitleBn = "সার প্রয়োগ, জমির হিসাব, পুকুরের মাছ চাষ ও ফসল বীজ গণনা",
                subtitleEn = "Fertilizer, Land Measurement, Fish Farming & Seed Count"
            )
            ToolCategory.KIDS -> CategoryCardTheme(
                gradients = listOf(
                    Color(0xFF500724),
                    Color(0xFF831843),
                    Color(0xFFBE185D),
                    Color(0xFFF43F5E)
                ),
                accentColor = Color(0xFFFDE047),
                subtitleBn = "বর্ণমালা, ছোটদের অংক শেখা, ছড়া, নামতা ও মজার কুইজ",
                subtitleEn = "Alphabet, Math for Kids, Rhymes, Multiplication & Fun Quiz"
            )
            ToolCategory.DEVELOPER -> CategoryCardTheme(
                gradients = listOf(
                    Color(0xFF030712),
                    Color(0xFF111827),
                    Color(0xFF1E1B4B),
                    Color(0xFF4338CA)
                ),
                accentColor = Color(0xFF22D3EE),
                subtitleBn = "বেস কনভার্টার, কালার কোড পিকার, হ্যাশ ও রেগুলার এক্সপ্রেশন",
                subtitleEn = "Base Converter, Color Picker, Hash & Regex Tools"
            )
            ToolCategory.ENGINEERING -> CategoryCardTheme(
                gradients = listOf(
                    Color(0xFF1C1917),
                    Color(0xFF292524),
                    Color(0xFF334155),
                    Color(0xFF475569)
                ),
                accentColor = Color(0xFF93C5FD),
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
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        when (category) {
            ToolCategory.HEALTH -> drawHealthIllustration(w, h)
            ToolCategory.FINANCE -> drawFinanceIllustration(w, h)
            ToolCategory.ISLAMIC -> drawIslamicIllustration(w, h)
            ToolCategory.UTILITY -> drawUtilityIllustration(w, h)
            ToolCategory.ELECTRICITY -> drawElectricityIllustration(w, h)
            ToolCategory.VEHICLE -> drawVehicleIllustration(w, h)
            ToolCategory.EDUCATION -> drawEducationIllustration(w, h)
            ToolCategory.AGRICULTURE -> drawAgricultureIllustration(w, h)
            ToolCategory.KIDS -> drawKidsIllustration(w, h)
            ToolCategory.DEVELOPER -> drawDeveloperIllustration(w, h)
            ToolCategory.ENGINEERING -> drawEngineeringIllustration(w, h)
        }
    }
}

private fun DrawScope.drawHealthIllustration(w: Float, h: Float) {
    val alpha = 0.14f
    val strokeColor = Color.White.copy(alpha = alpha)

    // ECG Heartbeat line across the card
    val path = Path().apply {
        moveTo(w * 0.1f, h * 0.75f)
        lineTo(w * 0.40f, h * 0.75f)
        lineTo(w * 0.45f, h * 0.60f)
        lineTo(w * 0.50f, h * 0.90f)
        lineTo(w * 0.56f, h * 0.30f)
        lineTo(w * 0.62f, h * 0.85f)
        lineTo(w * 0.67f, h * 0.70f)
        lineTo(w * 0.72f, h * 0.75f)
        lineTo(w * 0.95f, h * 0.75f)
    }
    drawPath(path, strokeColor, style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Faint Medical Cross Symbol on the right
    val cx = w * 0.82f
    val cy = h * 0.45f
    val size = 26f
    drawRect(
        color = Color.White.copy(alpha = 0.08f),
        topLeft = Offset(cx - size / 3, cy - size),
        size = Size(size * 2 / 3, size * 2)
    )
    drawRect(
        color = Color.White.copy(alpha = 0.08f),
        topLeft = Offset(cx - size, cy - size / 3),
        size = Size(size * 2, size * 2 / 3)
    )

    // Heart pulse circles
    drawCircle(Color.White.copy(alpha = 0.06f), radius = 45f, center = Offset(w * 0.56f, h * 0.30f))
}

private fun DrawScope.drawFinanceIllustration(w: Float, h: Float) {
    val alpha = 0.15f
    // Trending upward chart line
    val chartPath = Path().apply {
        moveTo(w * 0.15f, h * 0.85f)
        cubicTo(w * 0.35f, h * 0.80f, w * 0.45f, h * 0.55f, w * 0.60f, h * 0.50f)
        cubicTo(w * 0.70f, h * 0.45f, w * 0.75f, h * 0.30f, w * 0.92f, h * 0.22f)
    }
    drawPath(chartPath, Color.White.copy(alpha = alpha), style = Stroke(width = 3f, cap = StrokeCap.Round))

    // Chart bar columns
    val barWidth = 14f
    val bars = listOf(
        Pair(w * 0.55f, h * 0.40f),
        Pair(w * 0.63f, h * 0.55f),
        Pair(w * 0.71f, h * 0.68f),
        Pair(w * 0.79f, h * 0.80f)
    )
    bars.forEach { (x, barH) ->
        drawRoundRect(
            color = Color.White.copy(alpha = 0.08f),
            topLeft = Offset(x, h - barH),
            size = Size(barWidth, barH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
    }

    // Glowing coin circles
    drawCircle(Color(0xFFFBBF24).copy(alpha = 0.10f), radius = 28f, center = Offset(w * 0.85f, h * 0.35f))
    drawCircle(Color(0xFFFBBF24).copy(alpha = 0.18f), radius = 18f, center = Offset(w * 0.85f, h * 0.35f), style = Stroke(width = 2f))
}

private fun DrawScope.drawIslamicIllustration(w: Float, h: Float) {
    // Islamic Crescent Moon
    val moonCenterX = w * 0.84f
    val moonCenterY = h * 0.42f
    val moonRadius = 32f

    val outerCircle = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(moonCenterX - moonRadius, moonCenterY - moonRadius, moonCenterX + moonRadius, moonCenterY + moonRadius))
    }
    val innerCircle = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(moonCenterX - moonRadius + 14f, moonCenterY - moonRadius - 4f, moonCenterX + moonRadius + 6f, moonCenterY + moonRadius - 4f))
    }
    val crescent = Path().apply {
        op(outerCircle, innerCircle, PathOperation.Difference)
    }
    drawPath(crescent, Color(0xFFFCD34D).copy(alpha = 0.16f))

    // 8-pointed Islamic Star near moon
    drawIslamicStar(moonCenterX - 22f, moonCenterY - 18f, 9f, Color(0xFFFCD34D).copy(alpha = 0.22f))
    drawIslamicStar(w * 0.65f, h * 0.25f, 6f, Color.White.copy(alpha = 0.15f))
    drawIslamicStar(w * 0.45f, h * 0.70f, 7f, Color.White.copy(alpha = 0.10f))

    // Arch dome silhouette
    val domePath = Path().apply {
        moveTo(w * 0.65f, h)
        lineTo(w * 0.65f, h * 0.65f)
        cubicTo(w * 0.65f, h * 0.48f, w * 0.76f, h * 0.40f, w * 0.76f, h * 0.35f)
        cubicTo(w * 0.76f, h * 0.40f, w * 0.87f, h * 0.48f, w * 0.87f, h * 0.65f)
        lineTo(w * 0.87f, h)
    }
    drawPath(domePath, Color.White.copy(alpha = 0.05f))
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

private fun DrawScope.drawUtilityIllustration(w: Float, h: Float) {
    // Interlocking gear geometry
    val cx = w * 0.82f
    val cy = h * 0.48f
    val r = 36f

    drawCircle(Color.White.copy(alpha = 0.08f), radius = r, center = Offset(cx, cy))
    drawCircle(Color.White.copy(alpha = 0.12f), radius = r * 0.55f, center = Offset(cx, cy), style = Stroke(width = 2.5f))

    // Gear cogs
    for (i in 0 until 8) {
        val angle = i * (PI.toFloat() / 4f)
        val toothX = cx + (r + 5f) * cos(angle)
        val toothY = cy + (r + 5f) * sin(angle)
        drawCircle(Color.White.copy(alpha = 0.14f), radius = 5f, center = Offset(toothX, toothY))
    }

    // Ruler measurement ticks along top right
    for (i in 0 until 8) {
        val x = w * 0.50f + i * 14f
        val len = if (i % 2 == 0) 12f else 6f
        drawLine(
            color = Color.White.copy(alpha = 0.12f),
            start = Offset(x, h * 0.20f),
            end = Offset(x, h * 0.20f + len),
            strokeWidth = 2f
        )
    }
}

private fun DrawScope.drawElectricityIllustration(w: Float, h: Float) {
    // High-voltage lightning bolt
    val boltPath = Path().apply {
        moveTo(w * 0.82f, h * 0.12f)
        lineTo(w * 0.74f, h * 0.46f)
        lineTo(w * 0.80f, h * 0.46f)
        lineTo(w * 0.70f, h * 0.88f)
        lineTo(w * 0.88f, h * 0.42f)
        lineTo(w * 0.81f, h * 0.42f)
        close()
    }
    drawPath(boltPath, Color(0xFFFDE047).copy(alpha = 0.22f))
    drawPath(boltPath, Color.White.copy(alpha = 0.35f), style = Stroke(width = 1.5f))

    // Electric energy rings
    drawCircle(Color(0xFFF97316).copy(alpha = 0.10f), radius = 48f, center = Offset(w * 0.78f, h * 0.50f))
    drawCircle(Color(0xFFFDE047).copy(alpha = 0.15f), radius = 64f, center = Offset(w * 0.78f, h * 0.50f), style = Stroke(width = 1.5f))
}

private fun DrawScope.drawVehicleIllustration(w: Float, h: Float) {
    // Speedometer Arc
    val cx = w * 0.82f
    val cy = h * 0.58f
    val r = 40f

    drawArc(
        color = Color(0xFF7DD3FC).copy(alpha = 0.22f),
        startAngle = 140f,
        sweepAngle = 260f,
        useCenter = false,
        topLeft = Offset(cx - r, cy - r),
        size = Size(r * 2, r * 2),
        style = Stroke(width = 4f, cap = StrokeCap.Round)
    )

    // Gauge Needle
    val needleAngle = 230f * (PI.toFloat() / 180f)
    drawLine(
        color = Color(0xFFF43F5E).copy(alpha = 0.35f),
        start = Offset(cx, cy),
        end = Offset(cx + (r - 6f) * cos(needleAngle), cy + (r - 6f) * sin(needleAngle)),
        strokeWidth = 3f,
        cap = StrokeCap.Round
    )
    drawCircle(Color.White.copy(alpha = 0.30f), radius = 5f, center = Offset(cx, cy))

    // Motion speed lines
    for (i in 0 until 4) {
        val y = h * 0.35f + i * 10f
        val startX = w * 0.50f + i * 8f
        drawLine(
            color = Color.White.copy(alpha = 0.12f),
            start = Offset(startX, y),
            end = Offset(startX + 40f, y),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawEducationIllustration(w: Float, h: Float) {
    // Graduation Cap (Mortarboard)
    val cx = w * 0.82f
    val cy = h * 0.38f
    val capPath = Path().apply {
        moveTo(cx, cy - 14f)
        lineTo(cx + 30f, cy)
        lineTo(cx, cy + 14f)
        lineTo(cx - 30f, cy)
        close()
    }
    drawPath(capPath, Color.White.copy(alpha = 0.20f))

    // Cap base
    val baseArch = Path().apply {
        moveTo(cx - 16f, cy + 7f)
        lineTo(cx - 16f, cy + 18f)
        cubicTo(cx - 16f, cy + 26f, cx + 16f, cy + 26f, cx + 16f, cy + 18f)
        lineTo(cx + 16f, cy + 7f)
    }
    drawPath(baseArch, Color.White.copy(alpha = 0.14f))

    // Tassel string
    val tassel = Path().apply {
        moveTo(cx, cy)
        lineTo(cx + 28f, cy + 10f)
        lineTo(cx + 28f, cy + 24f)
    }
    drawPath(tassel, Color(0xFFF472B6).copy(alpha = 0.35f), style = Stroke(width = 2f, cap = StrokeCap.Round))

    // Open book pages at bottom
    val bookPath = Path().apply {
        moveTo(w * 0.60f, h * 0.85f)
        cubicTo(w * 0.68f, h * 0.80f, w * 0.72f, h * 0.82f, w * 0.76f, h * 0.86f)
        cubicTo(w * 0.80f, h * 0.82f, w * 0.84f, h * 0.80f, w * 0.92f, h * 0.85f)
    }
    drawPath(bookPath, Color.White.copy(alpha = 0.16f), style = Stroke(width = 2.5f, cap = StrokeCap.Round))
}

private fun DrawScope.drawAgricultureIllustration(w: Float, h: Float) {
    // Sprouting Leaf and Wheat stalk
    val cx = w * 0.82f
    val cy = h * 0.50f

    // Main stalk
    drawLine(
        color = Color(0xFF86EFAC).copy(alpha = 0.25f),
        start = Offset(cx, h * 0.88f),
        end = Offset(cx, h * 0.25f),
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )

    // Wheat grains on both sides
    for (i in 0 until 5) {
        val y = h * 0.32f + i * 11f
        // Left grain
        drawOval(
            color = Color(0xFF86EFAC).copy(alpha = 0.20f),
            topLeft = Offset(cx - 16f, y - 4f),
            size = Size(14f, 8f)
        )
        // Right grain
        drawOval(
            color = Color(0xFF86EFAC).copy(alpha = 0.20f),
            topLeft = Offset(cx + 2f, y - 8f),
            size = Size(14f, 8f)
        )
    }

    // Furrow hills curve at bottom
    val hillPath = Path().apply {
        moveTo(w * 0.40f, h)
        cubicTo(w * 0.55f, h * 0.75f, w * 0.70f, h * 0.85f, w * 0.95f, h * 0.70f)
    }
    drawPath(hillPath, Color.White.copy(alpha = 0.12f), style = Stroke(width = 2f, cap = StrokeCap.Round))
}

private fun DrawScope.drawKidsIllustration(w: Float, h: Float) {
    // Colorful floating balloons with strings
    val b1x = w * 0.78f
    val b1y = h * 0.32f
    drawOval(Color(0xFFFDE047).copy(alpha = 0.25f), topLeft = Offset(b1x - 14f, b1y - 18f), size = Size(28f, 36f))
    drawLine(Color.White.copy(alpha = 0.20f), start = Offset(b1x, b1y + 18f), end = Offset(b1x - 8f, b1y + 48f), strokeWidth = 1.5f)

    val b2x = w * 0.88f
    val b2y = h * 0.45f
    drawOval(Color(0xFFF472B6).copy(alpha = 0.25f), topLeft = Offset(b2x - 12f, b2y - 16f), size = Size(24f, 32f))
    drawLine(Color.White.copy(alpha = 0.20f), start = Offset(b2x, b2y + 16f), end = Offset(b2x - 4f, b2y + 42f), strokeWidth = 1.5f)

    // Playful starbursts
    drawCircle(Color.White.copy(alpha = 0.15f), radius = 6f, center = Offset(w * 0.65f, h * 0.30f))
    drawCircle(Color.White.copy(alpha = 0.10f), radius = 10f, center = Offset(w * 0.60f, h * 0.65f))
}

private fun DrawScope.drawDeveloperIllustration(w: Float, h: Float) {
    // Code brackets and terminal prompt
    val cx = w * 0.80f
    val cy = h * 0.45f

    // Terminal window outline
    drawRoundRect(
        color = Color(0xFF22D3EE).copy(alpha = 0.14f),
        topLeft = Offset(cx - 35f, cy - 25f),
        size = Size(70f, 50f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
        style = Stroke(width = 1.5f)
    )

    // Top window dots
    drawCircle(Color(0xFFEF4444).copy(alpha = 0.30f), radius = 2.5f, center = Offset(cx - 26f, cy - 18f))
    drawCircle(Color(0xFFF59E0B).copy(alpha = 0.30f), radius = 2.5f, center = Offset(cx - 18f, cy - 18f))
    drawCircle(Color(0xFF10B981).copy(alpha = 0.30f), radius = 2.5f, center = Offset(cx - 10f, cy - 18f))

    // Prompt > _
    val promptPath = Path().apply {
        moveTo(cx - 25f, cy - 5f)
        lineTo(cx - 18f, cy + 2f)
        lineTo(cx - 25f, cy + 9f)
    }
    drawPath(promptPath, Color(0xFF22D3EE).copy(alpha = 0.35f), style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawLine(
        color = Color(0xFF22D3EE).copy(alpha = 0.35f),
        start = Offset(cx - 14f, cy + 9f),
        end = Offset(cx - 4f, cy + 9f),
        strokeWidth = 2f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawEngineeringIllustration(w: Float, h: Float) {
    // Blueprint grid & Technical drafting compass
    val cx = w * 0.82f
    val cy = h * 0.45f

    // Grid lines
    for (i in 0 until 4) {
        val y = cy - 25f + i * 16f
        drawLine(Color.White.copy(alpha = 0.07f), start = Offset(cx - 35f, y), end = Offset(cx + 35f, y), strokeWidth = 1f)
    }
    for (i in 0 until 4) {
        val x = cx - 25f + i * 16f
        drawLine(Color.White.copy(alpha = 0.07f), start = Offset(x, cy - 30f), end = Offset(x, cy + 30f), strokeWidth = 1f)
    }

    // Compass angle divider
    val compass = Path().apply {
        moveTo(cx - 18f, cy + 20f)
        lineTo(cx, cy - 16f)
        lineTo(cx + 18f, cy + 20f)
    }
    drawPath(compass, Color(0xFF93C5FD).copy(alpha = 0.28f), style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawCircle(Color.White.copy(alpha = 0.30f), radius = 3.5f, center = Offset(cx, cy - 16f))
}

/**
 * The redesigned, high-end Gradient Category Card with round-stack tool icons.
 */
@Composable
fun CategoryDashboardCard(
    category: ToolCategory,
    categoryTools: List<ToolType>,
    language: AppLanguage,
    isBn: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val theme = remember(category) { CategoryThemeRegistry.getTheme(category) }
    val interactionSource = remember { MutableInteractionSource() }

    val top3Tools = remember(categoryTools) { categoryTools.take(3) }
    val remainingCount = remember(categoryTools) { (categoryTools.size - 3).coerceAtLeast(0) }

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
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.20f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = theme.gradients,
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            // Background artistic illustration matching the category
            CategoryIllustrationBackground(
                category = category,
                modifier = Modifier.matchParentSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Top Row: Category Icon Badge on Left & Tool Count Pill on Right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Icon Badge
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.20f))
                            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = category.getTitle(language),
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Tool count pill on top right
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.20f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.30f))
                    ) {
                        Text(
                            text = if (isBn) "${categoryTools.size}টি" else "${categoryTools.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Middle: Category Title & Subtitle
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = category.getTitle(language),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (isBn) theme.subtitleBn else theme.subtitleEn,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.88f),
                        maxLines = 2,
                        lineHeight = 14.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Row: Solid Round Stack Tool Icons on Right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy((-6).dp)
                    ) {
                        top3Tools.forEachIndexed { index, tool ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .zIndex((4 - index).toFloat())
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE2E8F0), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = tool.icon,
                                    contentDescription = tool.getTitle(language),
                                    tint = theme.gradients.first(),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        if (remainingCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .zIndex(0f)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE2E8F0), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+$remainingCount",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = theme.gradients.first()
                                )
                            }
                        }
                    }
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
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.20f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = theme.gradients,
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    )
            ) {
                CategoryIllustrationBackground(
                    category = category,
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
                            color = Color.White.copy(alpha = 0.22f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (isBn) "সকল ক্যাটাগরি" else "All Categories",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Total count pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.20f)
                        ) {
                            Text(
                                text = if (isBn) "${categoryTools.size}টি টুলস" else "${categoryTools.size} Tools",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
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
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.20f))
                                .border(1.dp, Color.White.copy(alpha = 0.40f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = category.getTitle(viewModel.selectedLanguage),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isBn) theme.subtitleBn else theme.subtitleEn,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f),
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
