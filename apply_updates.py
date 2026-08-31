import sys

file_path = 'app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt'
with open(file_path, 'r', encoding='utf-8') as f:
    text = f.read()

start_marker = '@Composable\nprivate fun AtsFixReviewDialog('
end_marker = '// ================= TAB 4: PREVIEW & EXPORT ================='

start_pos = text.find(start_marker)
end_pos = text.find(end_marker)

if start_pos != -1 and end_pos != -1:
    new_code = '''@Composable
private fun AtsFixReviewDialog(
    cvData: CvData,
    atsCheckItems: List<AtsCheckItem>,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit,
    onApplyFixes: (CvData) -> Unit
) {
    val failedChecks = remember(atsCheckItems) { atsCheckItems.filter { !it.isPassed } }

    val selectedStates = remember(failedChecks) {
        mutableStateMapOf<Int, Boolean>().apply {
            failedChecks.indices.forEach { idx -> this[idx] = true }
        }
    }

    val sampleSkillSuggestions = remember {
        listOf(
            CvSkillItem(name = "Project Management", description = "End-to-end project planning, resource allocation, and milestone execution."),
            CvSkillItem(name = "Agile & Scrum", description = "Sprint planning, daily standups, backlog grooming, and team collaboration."),
            CvSkillItem(name = "Data Analysis", description = "Analyzing performance metrics, reporting key insights, and data visualization."),
            CvSkillItem(name = "Cross-Functional Leadership", description = "Leading inter-departmental teams to achieve strategic organizational goals."),
            CvSkillItem(name = "Problem Solving", description = "Root cause analysis, workflow troubleshooting, and preventative solution design."),
            CvSkillItem(name = "Process Optimization", description = "Streamlining operational procedures, reducing bottlenecks, and driving efficiency.")
        )
    }

    val selectedSkillIndexes = remember {
        mutableStateMapOf<Int, Boolean>().apply {
            sampleSkillSuggestions.indices.forEach { idx -> this[idx] = true }
        }
    }

    val currentScore = remember(atsCheckItems) {
        atsCheckItems.filter { it.isPassed }.sumOf { it.weightPoints }.coerceIn(0, 100)
    }

    val projectedScore = remember(selectedStates, failedChecks, currentScore) {
        val selectedPoints = failedChecks.indices
            .filter { selectedStates[it] == true }
            .sumOf { failedChecks[it].weightPoints }
        (currentScore + selectedPoints).coerceIn(0, 100)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .padding(vertical = 10.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBn) "🔍 এটিএস ফিক্স ও রিভিউ অপশনসমূহ" else "🔍 ATS Fix & Review Suggestions",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.buttonEqualBg
                        )
                        Text(
                            text = if (isBn) "আপনার সাথে মানানসই পরিবর্তনগুলো পছন্দমত মার্ক করুন" else "Check/Uncheck suggestions that fit your background",
                            fontSize = 10.5.sp,
                            color = themeColors.displayText.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = themeColors.displayText, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = themeColors.buttonEqualBg.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBn) "বর্তমান এটিএস স্কোর: $currentScore%" else "Current Score: $currentScore%",
                                fontSize = 11.sp,
                                color = themeColors.displayText.copy(alpha = 0.8f)
                            )
                            Text(
                                text = if (isBn) "বাছাইকৃত লাইভ স্কোর: $projectedScore%" else "Selected Fix Score: $projectedScore%",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (projectedScore >= 80) Color(0xFF10B981) else themeColors.buttonEqualBg
                            )
                        }

                        TextButton(
                            onClick = {
                                val allSelected = selectedStates.values.all { it }
                                failedChecks.indices.forEach { idx ->
                                    selectedStates[idx] = !allSelected
                                }
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (selectedStates.values.all { it }) (if (isBn) "সব আনমার্ক" else "Unselect All") else (if (isBn) "সব সিলেক্ট" else "Select All"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.buttonEqualBg
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (failedChecks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBn) "🎉 চমৎকার! আপনার সিভিতে কোনো এটিএস ত্রুটি নেই। স্কোর ১০০%!" else "🎉 Awesome! No ATS issues found. Score is 100%!",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(failedChecks.size) { idx ->
                            val check = failedChecks[idx]
                            val isChecked = selectedStates[idx] == true

                            Surface(
                                onClick = { selectedStates[idx] = !isChecked },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isChecked) themeColors.buttonEqualBg.copy(alpha = 0.08f) else Color.Transparent,
                                border = BorderStroke(1.dp, if (isChecked) themeColors.buttonEqualBg.copy(alpha = 0.3f) else themeColors.displayText.copy(alpha = 0.1f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { selectedStates[idx] = it },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = themeColors.buttonEqualBg,
                                                uncheckedColor = themeColors.displayText.copy(alpha = 0.4f)
                                            ),
                                            modifier = Modifier.size(20.dp)
                                        )

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = if (isBn) check.categoryBn else check.categoryEn,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = themeColors.buttonEqualBg
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "+${check.weightPoints} pts",
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF10B981)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (isBn) check.detailBn else check.detailEn,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = themeColors.displayText
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = getFixExplanation(check.categoryEn, isBn),
                                                fontSize = 10.sp,
                                                color = themeColors.displayText.copy(alpha = 0.65f)
                                            )
                                        }
                                    }

                                    if (check.categoryEn == "Skills" && isChecked) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = if (isBn) "অনুমোদিত স্কিল ও বিবরণসমূহ (পছন্দমত বাছাই করুন):" else "Recommended Skills with Descriptions (Select):",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.buttonEqualBg,
                                            modifier = Modifier.padding(start = 30.dp, bottom = 4.dp)
                                        )
                                        sampleSkillSuggestions.forEachIndexed { sIdx, skillItem ->
                                            val isSkChecked = selectedSkillIndexes[sIdx] == true
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 28.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Checkbox(
                                                    checked = isSkChecked,
                                                    onCheckedChange = { selectedSkillIndexes[sIdx] = it },
                                                    modifier = Modifier.size(18.dp),
                                                    colors = CheckboxDefaults.colors(checkedColor = themeColors.buttonEqualBg)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Column {
                                                    Text(
                                                        text = skillItem.name,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = themeColors.displayText
                                                    )
                                                    Text(
                                                        text = skillItem.description,
                                                        fontSize = 9.5.sp,
                                                        color = themeColors.displayText.copy(alpha = 0.7f),
                                                        lineHeight = 13.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = if (isBn) "বাতিল" else "Cancel",
                            color = themeColors.displayText,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = {
                            var updatedCv = cvData
                            failedChecks.indices.forEach { idx ->
                                if (selectedStates[idx] == true) {
                                    val check = failedChecks[idx]
                                    if (check.categoryEn == "Skills") {
                                        val existingSkills = updatedCv.skills.toMutableList()
                                        sampleSkillSuggestions.forEachIndexed { sIdx, sItem ->
                                            if (selectedSkillIndexes[sIdx] == true && existingSkills.none { it.name.equals(sItem.name, ignoreCase = true) }) {
                                                existingSkills.add(sItem)
                                            }
                                        }
                                        updatedCv = updatedCv.copy(skills = existingSkills)
                                    } else {
                                        updatedCv = autoFixIndividualAtsCheck(updatedCv, check.categoryEn)
                                    }
                                }
                            }
                            onApplyFixes(updatedCv)
                            onDismiss()
                        },
                        enabled = selectedStates.values.any { it },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        modifier = Modifier.weight(1.8f)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBn) "বাছাইকৃত পছন্দসমূহ অ্যাপ্লাই করুন" else "Apply Selected Fixes",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CircularMatchReviewDialog(
    cvData: CvData,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit,
    onApplyFixes: (CvData) -> Unit
) {
    val missingKeywords = cvData.lastMissingKeywords
    val tailoredSummary = cvData.lastTailoredSummary
    val baseMatchPct = cvData.lastJobMatchPercentage

    var applySummary by remember { mutableStateOf(tailoredSummary.isNotBlank()) }

    val selectedSkillsState = remember(missingKeywords) {
        mutableStateMapOf<Int, Boolean>().apply {
            missingKeywords.indices.forEach { idx -> this[idx] = true }
        }
    }

    val projectedMatchPct = remember(applySummary, selectedSkillsState, missingKeywords, baseMatchPct) {
        if (missingKeywords.isEmpty() && tailoredSummary.isBlank()) baseMatchPct
        else {
            val totalPoints = (if (tailoredSummary.isNotBlank()) 15 else 0) + (missingKeywords.size * 10)
            val selectedPoints = (if (applySummary && tailoredSummary.isNotBlank()) 15 else 0) +
                    (missingKeywords.indices.filter { selectedSkillsState[it] == true }.size * 10)
            if (totalPoints == 0) baseMatchPct
            else {
                val ratio = selectedPoints.toFloat() / totalPoints.toFloat()
                (baseMatchPct + ((100 - baseMatchPct) * ratio).toInt()).coerceIn(baseMatchPct, 100)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
                .padding(vertical = 10.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBn) "🎯 সার্কুলার টিউন ও রিভিউ ডায়ালগ" else "🎯 Circular Match Review & Tailor",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.buttonEqualBg
                        )
                        Text(
                            text = if (isBn) "সার্কুলারের সাথে মানানসই স্কিল ও বিবরণ পছন্দমত রিভিউ করুন" else "Review skills and summary suggestions from circular",
                            fontSize = 10.5.sp,
                            color = themeColors.displayText.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = themeColors.displayText, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = themeColors.buttonEqualBg.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBn) "বর্তমান সার্কুলার ম্যাচ: $baseMatchPct%" else "Current Match: $baseMatchPct%",
                                fontSize = 11.sp,
                                color = themeColors.displayText.copy(alpha = 0.8f)
                            )
                            Text(
                                text = if (isBn) "প্রজেক্টেড জব ম্যাচ স্কোর: $projectedMatchPct%" else "Projected Job Match: $projectedMatchPct%",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (projectedMatchPct >= 80) Color(0xFF10B981) else themeColors.buttonEqualBg
                            )
                        }

                        if (missingKeywords.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    val allChecked = selectedSkillsState.values.all { it }
                                    missingKeywords.indices.forEach { selectedSkillsState[it] = !allChecked }
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = if (selectedSkillsState.values.all { it }) (if (isBn) "সব আনমার্ক" else "Unselect All") else (if (isBn) "সব সিলেক্ট" else "Select All"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.buttonEqualBg
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (tailoredSummary.isNotBlank()) {
                        item {
                            Text(
                                text = if (isBn) "📝 সার্কুলার অনুযায়ী প্রোফাইল সামারি:" else "📝 Tailored Profile Summary:",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.buttonEqualBg,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Surface(
                                onClick = { applySummary = !applySummary },
                                shape = RoundedCornerShape(10.dp),
                                color = if (applySummary) themeColors.buttonEqualBg.copy(alpha = 0.08f) else Color.Transparent,
                                border = BorderStroke(1.dp, if (applySummary) themeColors.buttonEqualBg.copy(alpha = 0.3f) else themeColors.displayText.copy(alpha = 0.1f)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Checkbox(
                                        checked = applySummary,
                                        onCheckedChange = { applySummary = it },
                                        colors = CheckboxDefaults.colors(checkedColor = themeColors.buttonEqualBg),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (isBn) "সার্কুলার কেন্দ্রিক সামারি আপডেট করুন" else "Apply circular-tailored summary",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.displayText
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = tailoredSummary,
                                            fontSize = 10.5.sp,
                                            color = themeColors.displayText.copy(alpha = 0.8f),
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (missingKeywords.isNotEmpty()) {
                        item {
                            Text(
                                text = if (isBn) "⚡ সার্কুলারে থাকা মিসিং স্কিলসমূহ (লিস্ট থেকে চুজ করুন):" else "⚡ Missing Circular Skills (Select to Add with Descriptions):",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.buttonEqualBg,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        items(missingKeywords.size) { idx ->
                            val kw = missingKeywords[idx]
                            val isChecked = selectedSkillsState[idx] == true
                            val generatedDesc = generateSkillDescription(kw, cvData.jobTitle)

                            Surface(
                                onClick = { selectedSkillsState[idx] = !isChecked },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isChecked) themeColors.buttonEqualBg.copy(alpha = 0.08f) else Color.Transparent,
                                border = BorderStroke(1.dp, if (isChecked) themeColors.buttonEqualBg.copy(alpha = 0.3f) else themeColors.displayText.copy(alpha = 0.1f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { selectedSkillsState[idx] = it },
                                        colors = CheckboxDefaults.colors(checkedColor = themeColors.buttonEqualBg),
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = kw,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.displayText
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "বিবরণ: $generatedDesc",
                                            fontSize = 10.5.sp,
                                            color = themeColors.displayText.copy(alpha = 0.75f),
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = if (isBn) "বাতিল" else "Cancel",
                            color = themeColors.displayText,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = {
                            var updated = cvData
                            if (applySummary && tailoredSummary.isNotBlank()) {
                                updated = updated.copy(summary = tailoredSummary)
                            }
                            val newSkills = updated.skills.toMutableList()
                            val remainingMissing = mutableListOf<String>()

                            missingKeywords.indices.forEach { idx ->
                                val kw = missingKeywords[idx]
                                if (selectedSkillsState[idx] == true) {
                                    if (newSkills.none { it.name.equals(kw, ignoreCase = true) }) {
                                        val desc = generateSkillDescription(kw, cvData.jobTitle)
                                        newSkills.add(CvSkillItem(name = kw, description = desc))
                                    }
                                } else {
                                    remainingMissing.add(kw)
                                }
                            }

                            updated = updated.copy(
                                skills = newSkills,
                                lastMissingKeywords = remainingMissing,
                                lastJobMatchPercentage = projectedMatchPct
                            )

                            onApplyFixes(updated)
                            onDismiss()
                        },
                        enabled = applySummary || selectedSkillsState.values.any { it },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        modifier = Modifier.weight(1.8f)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBn) "বাছাইকৃত স্কিল সিভিতে যুক্ত করুন" else "Apply Selected Circular Fixes",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun getFixExplanation(categoryEn: String, isBn: Boolean): String {
    return when (categoryEn) {
        "Contact Info" -> if (isBn) "অফিসিয়াল ইমেইল, ফোন নম্বর ও প্রোফাইল লিঙ্ক যুক্ত করা হবে" else "Will add official email, phone & profile links"
        "Summary" -> if (isBn) "সামারিতে ৩-৪ লাইনের বিস্তারিত প্রভাব ও সাফল্যসূচক অর্জনের অংশ যুক্ত হবে" else "Will refine summary with impact metrics"
        "Experience" -> if (isBn) "অভিজ্ঞতা ডেসক্রিপশনে সাফল্যসূচক % ও সংখ্যাযুক্ত পয়েন্ট যুক্ত হবে" else "Will add quantified % metrics to experiences"
        "Education" -> if (isBn) "ডিগ্রি, শিক্ষা প্রতিষ্ঠান ও পাসের সনের তথ্য পারফেক্ট করা হবে" else "Will complete degree, institution & passing year"
        "Skills" -> if (isBn) "এটিএস ফ্রেন্ডলি কোর স্কিলস ও বিবরণসমূহ যুক্ত করা হবে" else "Will populate standard ATS industry skills with descriptions"
        "Formatting" -> if (isBn) "প্রমিত এটিএস বুলেট স্টাইল ও ১-কলাম লেআউট সক্রিয় করা হবে" else "Will enable standard ATS bullet structure"
        else -> if (isBn) "সিভির ডাটা এটিএস স্ট্যান্ডার্ডে টিউন করা হবে" else "Will tune CV data to ATS standards"
    }
}

// ================= TAB 3: AI JOB MATCH WITH IMAGE UPLOAD =================

@Composable
private fun AiJobCircularMatchTab(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onMatchCircularAi: (String, ByteArray?, String) -> Unit,
    isScrollable: Boolean = true
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val atsScoreResult = remember(cvData) { calculateLocalAtsScore(cvData) }
    var showBreakdown by remember { mutableStateOf(false) }
    var showFixReviewDialog by remember { mutableStateOf(false) }
    var showCircularReviewDialog by remember { mutableStateOf(false) }

    var circularInputText by remember { mutableStateOf(cvData.targetJobCircular) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            try {
                context.contentResolver.openInputStream(uri).use { stream ->
                    selectedImageBitmap = BitmapFactory.decodeStream(stream)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(if (isScrollable) Modifier.verticalScroll(scrollState) else Modifier)
            .padding(14.dp)
    ) {
        SectionCardHeader(
            title = if (isBn) "এটিএস স্কোর ও জব ম্যাচ অ্যানালাইসিস" else "ATS Score & Job Match Analysis",
            icon = Icons.Default.Speed,
            themeColors = themeColors
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- DUAL TOP METRIC CARDS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // CARD 1: Local ATS Score
            val scoreColor = when {
                atsScoreResult.score >= 80 -> Color(0xFF10B981)
                atsScoreResult.score >= 60 -> Color(0xFFF59E0B)
                else -> Color(0xFFEF4444)
            }
            Surface(
                onClick = { showBreakdown = !showBreakdown },
                shape = RoundedCornerShape(14.dp),
                color = themeColors.cardBg,
                border = BorderStroke(1.dp, scoreColor.copy(alpha = 0.4f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isBn) "এটিএস প্রস্তুত স্কোর" else "ATS Ready Score",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${atsScoreResult.score}%",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = scoreColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when {
                            atsScoreResult.score >= 80 -> if (isBn) "এটিএস ফ্রেন্ডলি" else "ATS Friendly"
                            atsScoreResult.score >= 60 -> if (isBn) "উন্নতি প্রয়োজন" else "Needs Improvement"
                            else -> if (isBn) "নিম্ন এটিএস স্কোর" else "Low ATS Score"
                        },
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isBn) "${atsScoreResult.checkItems.count { it.isPassed }}/${atsScoreResult.checkItems.size} চেক পাস (ট্যাপ)" else "${atsScoreResult.checkItems.count { it.isPassed }}/${atsScoreResult.checkItems.size} checks passed (Tap)",
                        fontSize = 9.5.sp,
                        color = themeColors.displayText.copy(alpha = 0.6f)
                    )
                }
            }

            // CARD 2: Job Circular Match Score
            val matchPct = cvData.lastJobMatchPercentage
            val matchColor = when {
                matchPct >= 80 -> Color(0xFF10B981)
                matchPct >= 60 -> Color(0xFFF59E0B)
                matchPct > 0 -> Color(0xFFEF4444)
                else -> themeColors.displayText.copy(alpha = 0.4f)
            }
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = themeColors.cardBg,
                border = BorderStroke(1.dp, matchColor.copy(alpha = 0.4f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isBn) "সার্কুলার ম্যাচ %" else "Circular Match %",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (matchPct > 0) "$matchPct%" else "— %",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = matchColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when {
                            matchPct >= 80 -> if (isBn) "হাইলি ম্যাচড" else "High Match"
                            matchPct >= 60 -> if (isBn) "মাঝারি ম্যাচ" else "Moderate Match"
                            matchPct > 0 -> if (isBn) "লো ম্যাচ" else "Low Match"
                            else -> if (isBn) "অ্যানালাইসিস করুন" else "Analyze Circular"
                        },
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = matchColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (matchPct > 0) (if (isBn) "জেমিনি এআই অ্যানালাইসিস" else "Gemini AI Analysis") else (if (isBn) "সার্কুলার দিন" else "Provide Circular"),
                        fontSize = 9.5.sp,
                        color = themeColors.displayText.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- ATS FIX & REVIEW MASTER BUTTON CARD ---
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = themeColors.buttonEqualBg.copy(alpha = 0.1f),
            border = BorderStroke(1.5.dp, themeColors.buttonEqualBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "এটিএস স্কোর ফিক্স ও রিভিউ অপশন" else "ATS Score Review & Manual Fix",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = themeColors.displayText
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isBn) "মিছিং ফিল্ড, মেট্রিক্স ও স্কিলস রিভিউ করে ১০০% স্কোর করুন" else "Review missing fields, metrics & skills to reach 100%",
                        fontSize = 10.sp,
                        color = themeColors.displayText.copy(alpha = 0.75f)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { showFixReviewDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Checklist, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBn) "রিভিউ ও ফিক্স" else "Review & Fix",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // --- EXPANDABLE ATS SCORE BREAKDOWN CARD ---
        AnimatedVisibility(visible = showBreakdown) {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = themeColors.cardBg,
                    border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBn) "📋 এটিএস স্কোর ব্রেকডাউন ডিটেইলস" else "📋 Detailed ATS Score Breakdown",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.buttonEqualBg
                            )
                            IconButton(onClick = { showBreakdown = false }, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = themeColors.displayText, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        for (item in atsScoreResult.checkItems) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (item.isPassed) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = if (item.isPassed) Color(0xFF10B981) else Color(0xFFEF4444),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isBn) item.detailBn else item.detailEn,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = themeColors.displayText
                                    )
                                    Text(
                                        text = "${if (isBn) item.categoryBn else item.categoryEn} • (+${item.weightPoints} pts)",
                                        fontSize = 9.5.sp,
                                        color = themeColors.displayText.copy(alpha = 0.6f)
                                    )
                                }
                                if (!item.isPassed) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    OutlinedButton(
                                        onClick = { showFixReviewDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, themeColors.buttonEqualBg),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text(
                                            text = if (isBn) "রিভিউ করুন" else "Review",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.buttonEqualBg
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- AI ACTIONABLE MATCH RESULTS (CIRCULAR SPECIFIC REVIEW & FIX) ---
        if (cvData.lastMissingKeywords.isNotEmpty() || cvData.lastMatchingStrengths.isNotEmpty() || cvData.lastTailoredSummary.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = themeColors.cardBg,
                border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isBn) "জেমিনি এআই সার্কুলার ম্যাচিং রেজাল্ট" else "Gemini AI Circular Match Result",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.buttonEqualBg
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Review & Tailor Circular Button
                    Button(
                        onClick = { showCircularReviewDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Checklist, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "সার্কুলার অনুযায়ী সিভি টিউন ও ফিক্স রিভিউ" else "Review & Tailor CV to Circular",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Missing Keywords Vertical List with Descriptions
                    if (cvData.lastMissingKeywords.isNotEmpty()) {
                        Text(
                            text = if (isBn) "সার্কুলারে থাকা মিসিং স্কিলসমূহ (বিবরণসহ রিভিউ করে যুক্ত করুন):" else "Missing Circular Skills (Review with Descriptions):",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        cvData.lastMissingKeywords.forEach { kw ->
                            val desc = generateSkillDescription(kw, cvData.jobTitle)
                            Surface(
                                onClick = { showCircularReviewDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                color = themeColors.buttonEqualBg.copy(alpha = 0.06f),
                                border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = kw,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.displayText
                                        )
                                        Text(
                                            text = desc,
                                            fontSize = 10.sp,
                                            color = themeColors.displayText.copy(alpha = 0.7f),
                                            lineHeight = 13.5.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBn) "রিভিউ" else "Review",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.buttonEqualBg
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Matching Strengths
                    if (cvData.lastMatchingStrengths.isNotEmpty()) {
                        Text(
                            text = if (isBn) "✅ সিভির যে পয়েন্টগুলো সার্কুলারের সাথে মিলেছে:" else "✅ Qualifications Matched with Circular:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        for (str in cvData.lastMatchingStrengths) {
                            Text(
                                text = "• $str",
                                fontSize = 11.sp,
                                color = themeColors.displayText.copy(alpha = 0.85f),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Improvement Tips
                    if (cvData.lastImprovementTips.isNotEmpty()) {
                        Text(
                            text = if (isBn) "💡 এটিএস স্কোর বাড়াতে জেমিনির পরামর্শ:" else "💡 Tips to Increase ATS Match:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF59E0B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        for (tip in cvData.lastImprovementTips) {
                            Text(
                                text = "• $tip",
                                fontSize = 11.sp,
                                color = themeColors.displayText.copy(alpha = 0.85f),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- CIRCULAR INPUT & IMAGE UPLOAD FORM ---
        Text(
            text = if (isBn)
                "যে চাকরির জন্য আবেদন করতে চান তার সার্কুলার টেক্সট পেস্ট করুন অথবা ক্যামেরা/গ্যালারি থেকে সরাসরি সার্কুলার ইমেজের ছবি দিন! জেমিনি এআই এটিএস ম্যাচ স্কোর বের করবে ও সিভি কাস্টমাইজ করবে।"
            else
                "Paste target job circular text OR select an image of the circular from your gallery. Gemini Multi-modal AI will calculate your match score and tailor your CV.",
            fontSize = 11.5.sp,
            color = themeColors.displayText.copy(alpha = 0.75f),
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Text input area
        OutlinedTextField(
            value = circularInputText,
            onValueChange = { circularInputText = it },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            maxLines = 15,
            placeholder = { Text(text = if (isBn) "জব সার্কুলার ডেসক্রিপশন পেস্ট করুন..." else "Paste Job circular / description requirements text here...", fontSize = 12.5.sp) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = themeColors.buttonEqualBg,
                unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                focusedContainerColor = themeColors.cardBg,
                unfocusedContainerColor = themeColors.cardBg,
                focusedTextColor = themeColors.displayText,
                unfocusedTextColor = themeColors.displayText
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Image input options
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isBn) "সার্কুলার ছবি ইনপুট করুন (অপশনাল)" else "Upload Circular Image (Optional)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        border = BorderStroke(1.dp, themeColors.buttonEqualBg),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isBn) "গ্যালারি থেকে ছবি নিন" else "Choose Image", color = themeColors.buttonEqualBg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (selectedImageUri != null) {
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = {
                                selectedImageUri = null
                                selectedImageBitmap = null
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Cancel, contentDescription = "Clear image", tint = Color.Red.copy(alpha = 0.8f))
                        }
                    }
                }

                if (selectedImageBitmap != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Image(
                        bitmap = selectedImageBitmap!!.asImageBitmap(),
                        contentDescription = "Circular thumbnail",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, themeColors.displayText.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                var imageBytes: ByteArray? = null
                var mimeType = "image/jpeg"
                if (selectedImageUri != null) {
                    try {
                        context.contentResolver.openInputStream(selectedImageUri!!).use { stream ->
                            imageBytes = stream?.readBytes()
                        }
                        mimeType = context.contentResolver.getType(selectedImageUri!!) ?: "image/jpeg"
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                onMatchCircularAi(circularInputText, imageBytes, mimeType)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
        ) {
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isBn) "এআই দিয়ে এটিএস জব ম্যাচ অ্যানালাইসিস করুন" else "Analyze Job Match with Gemini AI",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (showFixReviewDialog) {
            AtsFixReviewDialog(
                cvData = cvData,
                atsCheckItems = atsScoreResult.checkItems,
                isBn = isBn,
                themeColors = themeColors,
                onDismiss = { showFixReviewDialog = false },
                onApplyFixes = { updatedCv: CvData ->
                    onCvDataChange(updatedCv)
                    Toast.makeText(context, if (isBn) "বাছাইকৃত পরিবর্তনগুলো সিভিতে সফলভাবে প্রয়োগ করা হয়েছে!" else "Selected fixes applied successfully!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showCircularReviewDialog) {
            CircularMatchReviewDialog(
                cvData = cvData,
                isBn = isBn,
                themeColors = themeColors,
                onDismiss = { showCircularReviewDialog = false },
                onApplyFixes = { updatedCv: CvData ->
                    onCvDataChange(updatedCv)
                    Toast.makeText(context, if (isBn) "বাছাইকৃত সার্কুলার স্কিল ও সামারি সিভিতে যুক্ত হয়েছে!" else "Selected circular skills applied successfully!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
