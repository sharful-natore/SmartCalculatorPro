import re

with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "r") as f:
    content = f.read()

replacement = """
    onOpenPdfInAppViewer: () -> Unit,
    onSaveProfile: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var previewMode by remember { mutableStateOf("PREVIEW") } // "PREVIEW" or "LIVE_EDIT"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp)
    ) {
        // Mode Selector Chips
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                onClick = { previewMode = "PREVIEW" },
                shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                color = if (previewMode == "PREVIEW") themeColors.buttonEqualBg else themeColors.cardBg,
                border = BorderStroke(1.dp, themeColors.buttonEqualBg),
                modifier = Modifier.height(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(if (isBn) "প্রিভিউ" else "Preview", color = if (previewMode == "PREVIEW") Color.White else themeColors.buttonEqualBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            Surface(
                onClick = { previewMode = "LIVE_EDIT" },
                shape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
                color = if (previewMode == "LIVE_EDIT") themeColors.buttonEqualBg else themeColors.cardBg,
                border = BorderStroke(1.dp, themeColors.buttonEqualBg),
                modifier = Modifier.height(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(if (isBn) "লাইভ এডিট (সেটিংস)" else "Live Edit (Settings)", color = if (previewMode == "LIVE_EDIT") Color.White else themeColors.buttonEqualBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        if (previewMode == "LIVE_EDIT") {
            // Live Edit Spacing & Padding Controls
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themeColors.cardBg,
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
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
        }

        // Template Selector Grid (At least 5-7 templates)
"""

content = re.sub(
    r'    onOpenPdfInAppViewer: \(\) -> Unit,\n    onSaveProfile: \(\) -> Unit = \{\}\n\) \{\n    val scrollState = rememberScrollState\(\)\n    Column\(\n        modifier = Modifier\n            \.fillMaxSize\(\)\n            \.verticalScroll\(scrollState\)\n            \.padding\(14\.dp\)\n    \) \{\n        // Template Selector Grid \(At least 5-7 templates\)',
    replacement.replace('\n', '\\n').replace('$', '\\$'),
    content
)

# Unescape $
content = content.replace('\\$', '$')

with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "w") as f:
    f.write(content)
