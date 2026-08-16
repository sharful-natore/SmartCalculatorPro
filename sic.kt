// --- 4. SEHRI & IFTAR CARD ---
@Composable
fun SehriIftarCard(
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

    // Calendar Boundaries
    val calSehriToday = parseIslamicTimeToCalendar("04:46 AM", offset, 0)
    val calIftarToday = parseIslamicTimeToCalendar("06:18 PM", offset, 0)
    val calSehriTomorrow = parseIslamicTimeToCalendar("04:46 AM", offset, 1)

    val nowMillis = currentTimeMillis

    // Active Phase Calculation
    val (statusTitleBn, statusTitleEn, targetLabelBn, targetLabelEn, targetMillis, isFastingHours) = when {
        nowMillis < calSehriToday.timeInMillis -> {
            Hexuple(
                "এখন সেহরির সময়", "Sehri Time Active",
                "সেহরির শেষ সময় বাকি:", "Sehri Ends In:",
                calSehriToday.timeInMillis, false
            )
        }
        nowMillis < calIftarToday.timeInMillis -> {
            Hexuple(
                "এখন রোজা পালনের সময়", "Fasting Hours Active",
                "ইফতারের বাকি সময়:", "Iftar Time In:",
                calIftarToday.timeInMillis, true
            )
        }
        else -> {
            Hexuple(
                "আজকের ইফতার সম্পন্ন", "Today's Iftar Completed",
                "আগামীকালের সেহরি শেষ হতে বাকি:", "Tomorrow's Sehri Ends In:",
                calSehriTomorrow.timeInMillis, false
            )
        }
    }

    val countdownMillis = maxOf(0L, targetMillis - nowMillis)

    // Fasting Progress during daytime
    val fastingProgress = if (isFastingHours) {
        val totalFastingMillis = (calIftarToday.timeInMillis - calSehriToday.timeInMillis).toFloat()
        val elapsedMillis = (nowMillis - calSehriToday.timeInMillis).toFloat()
        (elapsedMillis / totalFastingMillis).coerceIn(0f, 1f)
    } else 0f

    val sehriRemainingMillis = maxOf(0L, calSehriToday.timeInMillis - nowMillis)
    val iftarRemainingMillis = maxOf(0L, calIftarToday.timeInMillis - nowMillis)

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isBn) "সেহরি ও ইফতারের সময়সূচি" else "Sehri & Iftar Schedule",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    Text(
                        text = if (isBn) "${viewModel.selectedIslamicDistrictBn.split(" ")[0]} জোন সময়সূচি" else "${viewModel.selectedIslamicDistrictEn} Zone Schedule",
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                DistrictSelectorDropdown(viewModel, themeColors)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Live Countdown Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
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
                                imageVector = if (isFastingHours) Icons.Default.WbSunny else Icons.Default.NightsStay,
                                contentDescription = null,
                                tint = if (isFastingHours) Color(0xFFF59E0B) else Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn) statusTitleBn else statusTitleEn,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = (if (isFastingHours) Color(0xFFF59E0B) else Color(0xFF38BDF8)).copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = if (isBn) "লাইভ সময়" else "Live",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFastingHours) Color(0xFFF59E0B) else Color(0xFF38BDF8),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isBn) targetLabelBn else targetLabelEn,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formatIslamicCountdown(countdownMillis, isBn),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isFastingHours) Color(0xFFF59E0B) else Color(0xFF38BDF8)
                    )

                    if (isFastingHours) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isBn) "রোজার পার্সেন্টেজ" else "Fasting Completed",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${(fastingProgress * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { fastingProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = Color(0xFFF59E0B),
                            trackColor = Color.White.copy(alpha = 0.15f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Side-by-Side Timing Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sehri Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.NightsStay, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBn) "সেহরির শেষ সময়" else "Sehri Ends",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = adjustIslamicTime("04:46 AM", offset),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        if (nowMillis < calSehriToday.timeInMillis) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                Text(
                                    text = formatIslamicCountdown(sehriRemainingMillis, isBn),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Iftar Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBn) "ইফতারের সময়" else "Iftar Time",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = adjustIslamicTime("06:18 PM", offset),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        if (nowMillis in calSehriToday.timeInMillis until calIftarToday.timeInMillis) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                Text(
                                    text = formatIslamicCountdown(iftarRemainingMillis, isBn),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF59E0B),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Sehri & Iftar Duas
            Text(
                text = if (isBn) "সেহরি ও ইফতারের দোয়া" else "Sehri & Iftar Duas",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.background)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isBn) "১. রোজার নিয়ত (Sehri Niyyat):" else "1. Fasting Intention (Sehri):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.buttonEqualBg
                    )
                    Text(
                        text = "نَوَيْتُ أَنْ أَصُومَ غَدًا مِنْ شَهْرِ رَمَضَانَ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    )
                    Text(
                        text = if (isBn) "উচ্চারণ: নাওয়াইতু আন আসুমা গাদাম মিন শাহরি রমাদান।" else "Pronunciation: Nawaitu an asuma gadam min shahri ramadan.",
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.8f)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = themeColors.displayText.copy(alpha = 0.1f))

                    Text(
                        text = if (isBn) "২. ইফতারের দোয়া (Iftar Dua):" else "2. Iftar Dua:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.buttonEqualBg
                    )
                    Text(
                        text = "اللَّهُمَّ إِنِّي لَكَ صُمْتُ وَبِكَ آمَنْتُ وَعَلَى رِزْقِكَ أَفْطَرْتُ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    )
                    Text(
                        text = if (isBn) "উচ্চারণ: আল্লাহুম্মা ইন্নি লাকা সুমতু ওয়া বিকার আমানতু ওয়া 'আলা রিযকিকা আফতারতু।" else "Pronunciation: Allahumma inni laka sumtu wa bika amantu wa 'ala rizqika aftartu.",
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

private data class Hexuple<A, B, C, D, E, F>(
    val first: A, val second: B, val third: C,
    val fourth: D, val fifth: E, val sixth: F
)

// --- 5. ISLAMIC DUAS & VIRTUOUS AMAL (সমৃদ্ধ দোয়া ও আমল) ---
