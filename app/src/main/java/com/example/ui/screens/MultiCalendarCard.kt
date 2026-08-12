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

    var selectedCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    val todayCalendar = remember { Calendar.getInstance() }

    val currentYear = selectedCalendar.get(Calendar.YEAR)
    val currentMonth = selectedCalendar.get(Calendar.MONTH) // 0-indexed

    val todayDateInfo = remember(selectedCalendar.timeInMillis) {
        CalendarUtils.getMultiDateInfo(selectedCalendar, isBn)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                            text = if (isBn) "স্মার্ট ট্রিপল ক্যালেন্ডার" else "Smart Multi-Calendar",
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

                if (onCloseRequest != null) {
                    IconButton(onClick = onCloseRequest) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = themeColors.displayText
                        )
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
                        text = todayDateInfo.englishMonthYear,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    Text(
                        text = "${todayDateInfo.bengaliMonthYear} • ${todayDateInfo.hijriMonthYear}",
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
                        color = if (idx == 5) Color(0xFFD32F2F) else themeColors.displayText.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Month Grid
            val totalGridCells = firstDayOffset + totalDaysInMonth
            val gridRows = (totalGridCells + 6) / 7

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (row in 0 until gridRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
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

                                val cellDateInfo = CalendarUtils.getMultiDateInfo(cellCal, isBn)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isToday) themeColors.buttonEqualBg
                                            else themeColors.displayText.copy(alpha = 0.04f)
                                        )
                                        .clickable {
                                            selectedCalendar = cellCal
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = if (isBn) CalendarUtils.toBengaliDigits(dayNumber) else dayNumber.toString(),
                                            fontSize = 14.sp,
                                            fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
                                            color = if (isToday) Color.White else themeColors.displayText
                                        )
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
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "📅 ${todayDateInfo.englishDayName}, ${todayDateInfo.englishDate}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = themeColors.displayText
                    )
                    Text(
                        text = "🌾 বাংলা: ${todayDateInfo.bengaliDate}",
                        fontSize = 13.sp,
                        color = themeColors.displayText.copy(alpha = 0.85f)
                    )
                    Text(
                        text = "🌙 আরবি/হিজরী: ${todayDateInfo.hijriDate}",
                        fontSize = 13.sp,
                        color = themeColors.displayText.copy(alpha = 0.85f)
                    )
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
