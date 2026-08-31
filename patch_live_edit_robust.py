import sys

with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "r") as f:
    lines = f.readlines()

start_idx = -1
end_idx = -1

for i, line in enumerate(lines):
    if 'if (previewMode == "LIVE_EDIT") {' in line and start_idx == -1:
        start_idx = i
    if '// --- SIGNATURE LINE OPTION CARD ---' in line and start_idx != -1:
        end_idx = i
        break

if start_idx != -1 and end_idx != -1:
    before = lines[:start_idx]
    after = lines[end_idx:]
    
    replacement = """        if (previewMode == "LIVE_EDIT") {
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
"""

    original_preview_block = lines[start_idx+1 : end_idx]
    
    # Extract only the "Vector PDF Live Canvas Screen" part
    vector_screen_start = -1
    for i, line in enumerate(original_preview_block):
        if '// Vector PDF Live Canvas Screen' in line:
            vector_screen_start = i
            break
            
    if vector_screen_start != -1:
        replacement += "".join(original_preview_block[vector_screen_start:])
        replacement += "        }\n\n"
        
        with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "w") as f:
            f.writelines(before)
            f.write(replacement)
            f.writelines(after)
        print("Success replacing robustly")
    else:
        print("Vector screen start not found")
else:
    print("Start or end not found")
