import re
with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "r") as f:
    content = f.read()

pattern = re.compile(r'if \(previewMode == "LIVE_EDIT"\) \{.*?// --- SIGNATURE LINE OPTION CARD ---', re.DOTALL)

replacement = """if (previewMode == "LIVE_EDIT") {
            // Embedded Quick Edit Forms
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themeColors.cardBg,
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(if (isBn) "মার্জিন, প্যাডিং ও স্পেসিং" else "Margin, Padding & Spacing", fontSize = 14.sp, color = themeColors.displayText, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(if (isBn) "মার্জিন (Margin): ${cvData.customMargin.toInt()}" else "Margin: ${cvData.customMargin.toInt()}", fontSize = 12.sp, color = themeColors.displayText, fontWeight = FontWeight.Bold)
                    androidx.compose.material3.Slider(
                        value = cvData.customMargin,
                        onValueChange = { onCvDataChange(cvData.copy(customMargin = it)) },
                        valueRange = 20f..80f,
                        steps = 60,
                        colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = themeColors.buttonEqualBg, activeTrackColor = themeColors.buttonEqualBg)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(if (isBn) "প্যাডিং (Padding): ${cvData.customPadding.toInt()}" else "Padding: ${cvData.customPadding.toInt()}", fontSize = 12.sp, color = themeColors.displayText, fontWeight = FontWeight.Bold)
                    androidx.compose.material3.Slider(
                        value = cvData.customPadding,
                        onValueChange = { onCvDataChange(cvData.copy(customPadding = it)) },
                        valueRange = 5f..30f,
                        steps = 25,
                        colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = themeColors.buttonEqualBg, activeTrackColor = themeColors.buttonEqualBg)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(if (isBn) "লাইন স্পেসিং (Line Spacing): ${String.format("%.2f", cvData.customLineSpacing)}" else "Line Spacing: ${String.format("%.2f", cvData.customLineSpacing)}", fontSize = 12.sp, color = themeColors.displayText, fontWeight = FontWeight.Bold)
                    androidx.compose.material3.Slider(
                        value = cvData.customLineSpacing,
                        onValueChange = { onCvDataChange(cvData.copy(customLineSpacing = it)) },
                        valueRange = 0.8f..2.0f,
                        steps = 24,
                        colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = themeColors.buttonEqualBg, activeTrackColor = themeColors.buttonEqualBg)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            ProfileAndPersonasTab(
                cvData = cvData,
                profilesList = emptyList(),
                onCvDataChange = onCvDataChange,
                onActiveProfileSelected = {},
                onAddNewProfile = {},
                onDeleteProfile = {},
                themeColors = themeColors,
                isBn = isBn,
                onOpenPdfInViewer = {},
                onGenerateSummaryAi = {},
                isScrollable = false
            )
            Spacer(modifier = Modifier.height(16.dp))
            ExperienceTab(
                cvData = cvData,
                onCvDataChange = onCvDataChange,
                themeColors = themeColors,
                isBn = isBn,
                onEnhanceBulletAi = {},
                onGenerateFresherAi = {},
                isScrollable = false
            )
            Spacer(modifier = Modifier.height(16.dp))
            EducationAndSkillsTab(
                cvData = cvData,
                onCvDataChange = onCvDataChange,
                onRequestAiPrompt = { _, _, _, _ -> },
                themeColors = themeColors,
                isBn = isBn,
                isScrollable = false
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    onRefreshPreview()
                    previewMode = "PREVIEW" // Auto switch back
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isBn) "প্রিভিউ রিফ্রেশ করুন" else "Refresh Preview & Apply Changes", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            // Vector PDF Live Canvas Screen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionCardHeader(
                    title = if (isBn) "লাইভ ভেক্টর প্রিভিউ (${pdfBitmaps.size}টি পেজ)" else "Live Vector Preview (${pdfBitmaps.size} Page${if (pdfBitmaps.size > 1) "s" else ""})",
                    icon = Icons.Default.Visibility,
                    themeColors = themeColors
                )
                OutlinedButton(
                    onClick = onRefreshPreview,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, themeColors.buttonEqualBg),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    if (isPreviewRendering) {
                        CircularProgressIndicator(
                            color = themeColors.buttonEqualBg,
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.5.dp
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isBn) "রিফ্রেশ" else "Refresh", color = themeColors.buttonEqualBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (pdfBitmaps.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.LightGray,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        pdfBitmaps.forEachIndexed { index, bitmap ->
                            if (index > 0) Spacer(modifier = Modifier.height(8.dp))
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "PDF Preview Page ${index + 1}",
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.FillWidth
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No PDF Preview Available", color = Color.Gray)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(14.dp))
        // --- SIGNATURE LINE OPTION CARD ---"""

if pattern.search(content):
    content = pattern.sub(replacement, content)
    with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "w") as f:
        f.write(content)
    print("Success updating LIVE_EDIT logic")
else:
    print("Could not find LIVE_EDIT block")
