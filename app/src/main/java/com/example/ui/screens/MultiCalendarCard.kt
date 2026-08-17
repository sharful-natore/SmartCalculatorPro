package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import com.example.util.CalendarUtils
import java.util.*

@Composable
fun MultiCalendarCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    modifier: Modifier = Modifier,
    onCloseRequest: (() -> Unit)? = null
) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    val todayCalendar = remember { Calendar.getInstance() }

    val currentYear = selectedCalendar.get(Calendar.YEAR)
    val currentMonth = selectedCalendar.get(Calendar.MONTH) // 0-indexed

    val selectedDateInfo = remember(selectedCalendar.timeInMillis, isBn, viewModel.hijriSyncVersion) {
        CalendarUtils.getMultiDateInfo(selectedCalendar, isBn)
    }
    val selectedEvents = remember(selectedCalendar.timeInMillis) {
        CalendarUtils.getSpecialEvents(selectedCalendar, isBn)
    }
    val isSelectedToday = remember(selectedCalendar.timeInMillis) {
        selectedCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                selectedCalendar.get(Calendar.MONTH) == todayCalendar.get(Calendar.MONTH) &&
                selectedCalendar.get(Calendar.DAY_OF_MONTH) == todayCalendar.get(Calendar.DAY_OF_MONTH)
    }

    // Grid days calculation
    val daysInMonth = remember(currentYear, currentMonth) {
        val cal = selectedCalendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday
        val totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        Triple(firstDayOfWeek, totalDays, cal)
    }

    val (firstDayOffset, totalDaysInMonth) = daysInMonth

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Title Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(themeColors.buttonEqualBg.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Calendar",
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isBn) "স্মার্ট ক্যালেন্ডার" else "Smart Calendar",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        Text(
                            text = if (isBn) "ইংরেজি, বাংলা ও আরবি ক্যালেন্ডার" else "Gregorian, Bengali & Hijri Calendar",
                            fontSize = 12.sp,
                            color = themeColors.displayText.copy(alpha = 0.6f)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Global Hijri Sync / Refresh Button (Icon only)
                    IconButton(
                        onClick = {
                            viewModel.syncHijriDateOnline(context) { _, msg ->
                                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        if (viewModel.isSyncingHijriDate) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = themeColors.buttonEqualBg
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync Hijri Date",
                                tint = themeColors.buttonEqualBg,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Button(
                        onClick = { selectedCalendar = Calendar.getInstance() },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "Today",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBn) "আজ" else "Today",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    if (onCloseRequest != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(onClick = onCloseRequest) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = themeColors.displayText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Month Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(themeColors.displayText.copy(alpha = 0.05f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val newCal = selectedCalendar.clone() as Calendar
                    newCal.add(Calendar.MONTH, -1)
                    selectedCalendar = newCal
                }) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Prev",
                        tint = themeColors.buttonEqualBg
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = selectedDateInfo.englishMonthYear,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    Text(
                        text = "${selectedDateInfo.bengaliMonthYear} • ${selectedDateInfo.hijriMonthYear}",
                        fontSize = 12.sp,
                        color = themeColors.buttonEqualBg,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(onClick = {
                    val newCal = selectedCalendar.clone() as Calendar
                    newCal.add(Calendar.MONTH, 1)
                    selectedCalendar = newCal
                }) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next",
                        tint = themeColors.buttonEqualBg
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Color Legend Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(themeColors.displayText)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBn) "ইংরেজি" else "English",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText.copy(alpha = 0.8f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBn) "বাংলা (সবুজ)" else "Bengali (Green)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF59E0B))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBn) "হিজরী (হলুদ)" else "Hijri (Amber)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B)
                    )
                }
            }

            // Days of Week Header
            val daysHeader = if (isBn) listOf("রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র", "শনি")
            else listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                daysHeader.forEachIndexed { idx, day ->
                    Text(
                        text = day,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (idx == 5 || idx == 6) Color(0xFFE53935) else themeColors.displayText.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Month Grid
            val totalGridCells = firstDayOffset + totalDaysInMonth
            val gridRows = (totalGridCells + 6) / 7

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (row in 0 until gridRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (col in 0..6) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - firstDayOffset + 1

                            if (dayNumber in 1..totalDaysInMonth) {
                                val cellCal = selectedCalendar.clone() as Calendar
                                cellCal.set(Calendar.DAY_OF_MONTH, dayNumber)

                                val isToday = cellCal.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                                        cellCal.get(Calendar.MONTH) == todayCalendar.get(Calendar.MONTH) &&
                                        cellCal.get(Calendar.DAY_OF_MONTH) == todayCalendar.get(Calendar.DAY_OF_MONTH)

                                val isSelected = cellCal.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR) &&
                                        cellCal.get(Calendar.MONTH) == selectedCalendar.get(Calendar.MONTH) &&
                                        cellCal.get(Calendar.DAY_OF_MONTH) == selectedCalendar.get(Calendar.DAY_OF_MONTH)

                                val isWeekend = col == 5 || col == 6 // Fri or Sat
                                val hasOccasion = CalendarUtils.hasSpecialOccasion(cellCal)

                                val (bDay, _, _) = CalendarUtils.getBengaliDateComponents(cellCal)
                                val (hDay, _, _) = CalendarUtils.getHijriDateComponents(cellCal)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                isToday -> themeColors.buttonEqualBg
                                                isSelected -> themeColors.buttonEqualBg.copy(alpha = 0.18f)
                                                hasOccasion -> Color(0xFF6366F1).copy(alpha = 0.15f)
                                                else -> themeColors.displayText.copy(alpha = 0.04f)
                                            }
                                        )
                                        .then(
                                            when {
                                                isSelected && !isToday -> Modifier.border(1.5.dp, themeColors.buttonEqualBg, RoundedCornerShape(8.dp))
                                                hasOccasion && !isToday -> Modifier.border(1.dp, Color(0xFF6366F1).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                else -> Modifier
                                            }
                                        )
                                        .clickable {
                                            selectedCalendar = cellCal
                                        }
                                        .padding(vertical = 3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (hasOccasion && !isToday) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(3.dp)
                                                .size(5.dp)
                                                .background(Color(0xFF6366F1), CircleShape)
                                        )
                                    }
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        // English Day (Large & Bold)
                                        Text(
                                            text = dayNumber.toString(),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = when {
                                                isToday -> Color.White
                                                isWeekend -> Color(0xFFE53935)
                                                else -> themeColors.displayText
                                            }
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        // Sub-row: Bengali & Hijri Day numbers
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Bengali Day (Green)
                                            Text(
                                                text = if (isBn) CalendarUtils.toBengaliDigits(bDay) else bDay.toString(),
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isToday) Color(0xFFA7F3D0) else Color(0xFF10B981)
                                            )

                                            // Hijri / Arabic Day (Amber)
                                            Text(
                                                text = if (isBn) CalendarUtils.toBengaliDigits(hDay) else hDay.toString(),
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isToday) Color(0xFFFDE68A) else Color(0xFFF59E0B)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selected Date Summary Cards
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(themeColors.buttonEqualBg.copy(alpha = 0.1f))
                    .border(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isSelectedToday) {
                            if (isBn) "📅 আজকের তারিখ: ${selectedDateInfo.englishDayName}, ${selectedDateInfo.englishDate}"
                            else "📅 Today: ${selectedDateInfo.englishDayName}, ${selectedDateInfo.englishDate}"
                        } else {
                            if (isBn) "📅 নির্বাচিত তারিখ: ${selectedDateInfo.englishDayName}, ${selectedDateInfo.englishDate}"
                            else "📅 Selected Date: ${selectedDateInfo.englishDayName}, ${selectedDateInfo.englishDate}"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = themeColors.displayText
                    )
                    Text(
                        text = "🌾 বাংলা: ${selectedDateInfo.bengaliDate}",
                        fontSize = 13.sp,
                        color = themeColors.displayText.copy(alpha = 0.85f)
                    )
                    Text(
                        text = "🌙 আরবি/হিজরী: ${selectedDateInfo.hijriDate}",
                        fontSize = 13.sp,
                        color = themeColors.displayText.copy(alpha = 0.85f)
                    )

                    if (selectedEvents.isNotEmpty()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = themeColors.buttonEqualBg.copy(alpha = 0.3f)
                        )
                        Text(
                            text = if (isBn) "🎉 দিবস ও বিশেষ অনুষ্ঠান:" else "🎉 Special Occasion / Event:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = themeColors.buttonEqualBg
                        )
                        selectedEvents.forEach { event ->
                            Text(
                                text = "• $event",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (event.contains("🔴")) Color(0xFFE53935) else themeColors.displayText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Jump to Today
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = { selectedCalendar = Calendar.getInstance() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = "Today",
                        tint = themeColors.buttonEqualBg,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBn) "আজকের তারিখে যান" else "Jump to Today",
                        fontWeight = FontWeight.Bold,
                        color = themeColors.buttonEqualBg
                    )
                }
            }
        }
    }
}
