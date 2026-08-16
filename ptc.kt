// --- 3. PRAYER TIMES CARD ---
@Composable
fun PrayerTimesCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    val offset = viewModel.selectedIslamicDistrictOffsetMinutes

    // Live Ticker for 1-second dynamic countdown updates
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000L)
        }
    }

    // Calendar Boundaries for Today & Tomorrow
    val calFajrToday = parseIslamicTimeToCalendar("04:52 AM", offset, 0)
    val calSunriseToday = parseIslamicTimeToCalendar("06:08 AM", offset, 0)
    val calIshraqToday = parseIslamicTimeToCalendar("06:23 AM", offset, 0)
    val calZawaalToday = parseIslamicTimeToCalendar("12:00 PM", offset, 0)
    val calDhuhrToday = parseIslamicTimeToCalendar("12:15 PM", offset, 0)
    val calAsrToday = parseIslamicTimeToCalendar("04:35 PM", offset, 0)
    val calSunsetForbiddenToday = parseIslamicTimeToCalendar("06:03 PM", offset, 0)
    val calMaghribToday = parseIslamicTimeToCalendar("06:18 PM", offset, 0)
    val calIshaToday = parseIslamicTimeToCalendar("07:35 PM", offset, 0)

    val calFajrTomorrow = parseIslamicTimeToCalendar("04:52 AM", offset, 1)

    // Current Time in Millis
    val nowMillis = currentTimeMillis

    // Determine Active Waqt or Forbidden Period
    val (currentNameBn, currentNameEn, currentEndTimeMillis, nextNameBn, nextNameEn, nextStartTimeMillis, isForbidden, activeIndex) = when {
        nowMillis < calFajrToday.timeInMillis -> {
            Octuple(
                "এশা (Isha)", "Isha", calFajrToday.timeInMillis,
                "ফজর (Fajr)", "Fajr", calFajrToday.timeInMillis,
                false, 7
            )
        }
        nowMillis < calSunriseToday.timeInMillis -> {
            Octuple(
                "ফজর (Fajr)", "Fajr", calSunriseToday.timeInMillis,
                "⚠️ সূর্যোদয় (নিষিদ্ধ সময়)", "⚠️ Sunrise (Forbidden Time)", calSunriseToday.timeInMillis,
                false, 0
            )
        }
        nowMillis < calIshraqToday.timeInMillis -> {
            Octuple(
                "⚠️ সূর্যোদয়কাল (নিষিদ্ধ সময়)", "⚠️ Sunrise (Forbidden Time)", calIshraqToday.timeInMillis,
                "ইশরাক ও যোহর", "Ishraq & Dhuhr", calIshraqToday.timeInMillis,
                true, 1
            )
        }
        nowMillis < calZawaalToday.timeInMillis -> {
            Octuple(
                "ইশরাক / চাশতের সময়", "Ishraq / Duha Time", calZawaalToday.timeInMillis,
                "⚠️ জাওয়াল (নিষিদ্ধ সময়)", "⚠️ Zawaal (Forbidden Time)", calZawaalToday.timeInMillis,
                false, 2
            )
        }
        nowMillis < calDhuhrToday.timeInMillis -> {
            Octuple(
                "⚠️ ঠিক দুপুর / জাওয়াল (নিষিদ্ধ সময়)", "⚠️ Zawaal (Forbidden Time)", calDhuhrToday.timeInMillis,
                "যোহর (Dhuhr)", "Dhuhr", calDhuhrToday.timeInMillis,
                true, 3
            )
        }
        nowMillis < calAsrToday.timeInMillis -> {
            Octuple(
                "যোহর (Dhuhr)", "Dhuhr", calAsrToday.timeInMillis,
                "আসর (Asr)", "Asr", calAsrToday.timeInMillis,
                false, 4
            )
        }
        nowMillis < calSunsetForbiddenToday.timeInMillis -> {
            Octuple(
                "আসর (Asr)", "Asr", calSunsetForbiddenToday.timeInMillis,
                "⚠️ সূর্যাস্তকাল (নিষিদ্ধ সময়)", "⚠️ Sunset (Forbidden Time)", calSunsetForbiddenToday.timeInMillis,
                false, 5
            )
        }
        nowMillis < calMaghribToday.timeInMillis -> {
            Octuple(
                "⚠️ সূর্যাস্তকাল (নিষিদ্ধ সময়)", "⚠️ Sunset (Forbidden Time)", calMaghribToday.timeInMillis,
                "মাগরিব (Maghrib)", "Maghrib", calMaghribToday.timeInMillis,
                true, 6
            )
        }
        nowMillis < calIshaToday.timeInMillis -> {
            Octuple(
                "মাগরিব (Maghrib)", "Maghrib", calIshaToday.timeInMillis,
                "এশা (Isha)", "Isha", calIshaToday.timeInMillis,
                false, 7
            )
        }
        else -> {
            Octuple(
                "এশা (Isha)", "Isha", calFajrTomorrow.timeInMillis,
                "ফজর (Fajr)", "Fajr", calFajrTomorrow.timeInMillis,
                false, 7
            )
        }
    }

    val currentRemainingMillis = maxOf(0L, currentEndTimeMillis - nowMillis)
    val nextCountdownMillis = maxOf(0L, nextStartTimeMillis - nowMillis)

    // Schedule items list (Includes 5 Waqts + 3 Forbidden Times)
    val scheduleItems = listOf(
        ScheduleItem("ফজর (Fajr)", "Fajr", "04:52 AM", false, null),
        ScheduleItem("⚠️ সূর্যোদয় (নিষিদ্ধ সময়)", "⚠️ Sunrise (Forbidden)", "06:08 AM - 06:23 AM", true, "সূর্য ওঠার পর ১৫ মিনিট নামাজ পড়া নিষেধ"),
        ScheduleItem("ইশরাক / চাশতের সময়", "Ishraq / Duha", "06:23 AM", false, "নফল ইবাদতের সময়"),
        ScheduleItem("⚠️ ঠিক দুপুর / জাওয়াল (নিষিদ্ধ সময়)", "⚠️ Midday / Zawaal (Forbidden)", "12:00 PM - 12:15 PM", true, "সূর্য ঠিক মাথার উপরে থাকলে নামাজ নিষেধ"),
        ScheduleItem("যোহর (Dhuhr)", "Dhuhr", "12:15 PM", false, null),
        ScheduleItem("আসর (Asr)", "Asr", "04:35 PM", false, null),
        ScheduleItem("⚠️ সূর্যাস্তকাল (নিষিদ্ধ সময়)", "⚠️ Sunset (Forbidden)", "06:03 PM - 06:18 PM", true, "সূর্যাস্তের সময় নামাজ নিষেধ (ঐ দিনের আসর ছাড়া)"),
        ScheduleItem("মাগরিব (Maghrib)", "Maghrib", "06:18 PM", false, null),
        ScheduleItem("এশা (Isha)", "Isha", "07:35 PM", false, null)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isBn) "নামাজের সময়সূচি ও কাউন্টডাউন" else "Prayer Times & Live Countdown",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    Text(
                        text = if (isBn) "${viewModel.selectedIslamicDistrictBn.split(" ")[0]} ও পার্শ্ববর্তী এলাকা" else "${viewModel.selectedIslamicDistrictEn} & Surrounding Areas",
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.7f)
                    )
                }

                DistrictSelectorDropdown(viewModel, themeColors)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Active Status & Countdown Highlight Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isForbidden) Color(0xFFEF4444) else themeColors.buttonEqualBg
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isForbidden) Icons.Default.Warning else Icons.Default.Timer,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn) "বর্তমান: ${if (isForbidden) currentNameBn else currentNameBn}" else "Current: ${if (isForbidden) currentNameEn else currentNameEn}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = if (isForbidden) (if (isBn) "⚠️ নিষিদ্ধ সময়" else "Forbidden") else (if (isBn) "সক্রিয় ওয়াক্ত" else "Active"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Remaining time for current state
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn)
                                "${if (isForbidden) "নিষিদ্ধ সময় শেষ হতে বাকি:" else "চলতি ওয়াক্তের বাকি সময়:"}"
                            else
                                "${if (isForbidden) "Forbidden Time Ends In:" else "Current Waqt Ends In:"}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = formatIslamicCountdown(currentRemainingMillis, isBn),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.White.copy(alpha = 0.25f)
                    )

                    // Countdown to next waqt/event
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "পরবর্তী: ${if (isBn) nextNameBn else nextNameEn}" else "Next: ${if (isBn) nextNameBn else nextNameEn}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.95f)
                        )
                        Text(
                            text = formatIslamicCountdown(nextCountdownMillis, isBn),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timetable Header Label
            Text(
                text = if (isBn) "৫ ওয়াক্ত নামাজ ও নিষিদ্ধ সময়সূচি" else "5 Waqt Prayer & Forbidden Times",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Timetable List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                scheduleItems.forEachIndexed { idx, item ->
                    val isActive = (idx == activeIndex)
                    val adjustedTimeStr = if (item.timeStr.contains("-")) {
                        val parts = item.timeStr.split("-").map { it.trim() }
                        "${adjustIslamicTime(parts[0], offset)} - ${adjustIslamicTime(parts[1], offset)}"
                    } else {
                        adjustIslamicTime(item.timeStr, offset)
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isActive && item.isForbidden -> Color(0xFFEF4444).copy(alpha = 0.18f)
                                isActive -> themeColors.buttonEqualBg.copy(alpha = 0.15f)
                                item.isForbidden -> Color(0xFFF59E0B).copy(alpha = 0.08f)
                                else -> themeColors.background
                            }
                        ),
                        border = if (isActive) BorderStroke(1.5.dp, if (item.isForbidden) Color(0xFFEF4444) else themeColors.buttonEqualBg) else null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when {
                                            item.isForbidden -> Icons.Default.Block
                                            item.nameBn.contains("সূর্যোদয়") || item.nameEn.contains("Sunrise") -> Icons.Default.WbSunny
                                            else -> Icons.Default.Schedule
                                        },
                                        contentDescription = null,
                                        tint = when {
                                            item.isForbidden -> Color(0xFFEF4444)
                                            isActive -> themeColors.buttonEqualBg
                                            else -> themeColors.displayText.copy(alpha = 0.6f)
                                        },
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isBn) item.nameBn else item.nameEn,
                                        fontSize = 13.5.sp,
                                        fontWeight = if (isActive || item.isForbidden) FontWeight.Bold else FontWeight.Medium,
                                        color = when {
                                            item.isForbidden -> Color(0xFFDC2626)
                                            isActive -> themeColors.buttonEqualBg
                                            else -> themeColors.displayText
                                        }
                                    )
                                    if (isActive) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (item.isForbidden) Color(0xFFEF4444) else themeColors.buttonEqualBg
                                        ) {
                                            Text(
                                                text = if (isBn) "সক্রিয়" else "Active",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                if (item.noteBn != null) {
                                    Text(
                                        text = if (isBn) item.noteBn else (item.noteEn ?: ""),
                                        fontSize = 10.5.sp,
                                        color = if (item.isForbidden) Color(0xFFDC2626).copy(alpha = 0.85f) else themeColors.displayText.copy(alpha = 0.55f),
                                        modifier = Modifier.padding(start = 25.dp, top = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = adjustedTimeStr,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    item.isForbidden -> Color(0xFFDC2626)
                                    isActive -> themeColors.buttonEqualBg
                                    else -> themeColors.displayText
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class ScheduleItem(
    val nameBn: String,
    val nameEn: String,
    val timeStr: String,
    val isForbidden: Boolean,
    val noteBn: String?,
    val noteEn: String? = null
)

private data class Octuple<A, B, C, D, E, F, G, H>(
    val first: A, val second: B, val third: C,
    val fourth: D, val fifth: E, val sixth: F,
    val seventh: G, val eighth: H
)

// --- 4. SEHRI & IFTAR CARD ---
