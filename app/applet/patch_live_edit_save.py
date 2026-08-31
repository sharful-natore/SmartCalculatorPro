import sys
import re

# In this environment, use relative paths for the file operations inside python
# or use the absolute path /app/applet/...
file_path = "/app/applet/app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt"

with open(file_path, "r") as f:
    content = f.read()

# 1. Update Tab signatures
# ProfileAndPersonasTab
old_sig_p = """private fun ProfileAndPersonasTab(
    cvData: CvData,
    profilesList: List<CvData>,
    onCvDataChange: (CvData) -> Unit,
    onActiveProfileSelected: (String) -> Unit,
    onAddNewProfile: (String) -> Unit,
    onDeleteProfile: (String) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onOpenPdfInViewer: (CvData) -> Unit,
    onGenerateSummaryAi: () -> Unit
) {"""
new_sig_p = """private fun ProfileAndPersonasTab(
    cvData: CvData,
    profilesList: List<CvData>,
    onCvDataChange: (CvData) -> Unit,
    onActiveProfileSelected: (String) -> Unit,
    onAddNewProfile: (String) -> Unit,
    onDeleteProfile: (String) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onOpenPdfInViewer: (CvData) -> Unit,
    onGenerateSummaryAi: () -> Unit,
    isScrollable: Boolean = true,
    isLiveEdit: Boolean = false
) {"""
content = content.replace(old_sig_p, new_sig_p)

# ExperienceTab
old_sig_e = """private fun ExperienceTab(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onEnhanceBulletAi: (Int) -> Unit,
    onGenerateFresherAi: () -> Unit
) {"""
new_sig_e = """private fun ExperienceTab(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onEnhanceBulletAi: (Int) -> Unit,
    onGenerateFresherAi: () -> Unit,
    isScrollable: Boolean = true,
    isLiveEdit: Boolean = false
) {"""
content = content.replace(old_sig_e, new_sig_e)

# EducationAndSkillsTab
old_sig_edu = """private fun EducationAndSkillsTab(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    onRequestAiPrompt: (String, String, (String) -> Unit, () -> Unit) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {"""
new_sig_edu = """private fun EducationAndSkillsTab(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    onRequestAiPrompt: (String, String, (String) -> Unit, () -> Unit) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    isScrollable: Boolean = true,
    isLiveEdit: Boolean = false
) {"""
content = content.replace(old_sig_edu, new_sig_edu)

# CustomizationTab
old_sig_c = """private fun CustomizationTab(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {"""
new_sig_c = """private fun CustomizationTab(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    isScrollable: Boolean = true,
    isLiveEdit: Boolean = false
) {"""
content = content.replace(old_sig_c, new_sig_c)

# 2. Update scroll modifier in tabs
content = content.replace(
    '.verticalScroll(scrollState)',
    '.then(if (isScrollable) Modifier.verticalScroll(scrollState) else Modifier)'
)

# 3. Update CvCustomTextField calls to include isLiveEdit and isBn
# Using a more robust regex
content = re.sub(
    r'(CvCustomTextField\(\s*label = .*?,\s*value = .*?,\s*onValueChange = \{ .*? \},\s*themeColors = themeColors)(.*?)(\))',
    r'\1, isLiveEdit = isLiveEdit, isBn = isBn\2\3',
    content, flags=re.DOTALL
)

# 4. Handle summary in ProfileAndPersonasTab
summary_block_pattern = r'OutlinedTextField\(\s+value = cvData\.summary,\s+onValueChange = \{ onCvDataChange\(cvData\.copy\(summary = it\)\) \},\s+modifier = Modifier\.fillMaxWidth\(\),\s+minLines = 4,\s+maxLines = 15,\s+placeholder = \{ Text\(text = if \(isBn\) "(.*?)" else "(.*?)", fontSize = 12\.5\.sp\) \},\s+shape = RoundedCornerShape\(12\.dp\),\s+colors = OutlinedTextFieldDefaults\.colors\(.*?\)\s+\)'
content = re.sub(
    summary_block_pattern,
    r'CvCustomLargeTextField(\n            label = "",\n            value = cvData.summary,\n            onValueChange = { onCvDataChange(cvData.copy(summary = it)) },\n            themeColors = themeColors,\n            isLiveEdit = isLiveEdit,\n            isBn = isBn,\n            minLines = 4,\n            placeholderText = if (isBn) "\1" else "\2"\n        )',
    content, flags=re.DOTALL
)

# 5. Handle Fresher sections in ExperienceTab
fresher_pattern = r'OutlinedTextField\(\s+value = cvData\.(fresher\w+),\s+onValueChange = \{ onCvDataChange\(cvData\.copy\(\w+ = it\)\) \},\s+modifier = Modifier\.fillMaxWidth\(\),\s+minLines = 3,\s+maxLines = 10,\s+placeholder = \{ Text\(text = "(.*?)", fontSize = 11\.sp\) \},\s+shape = RoundedCornerShape\(10\.dp\),\s+colors = OutlinedTextFieldDefaults\.colors\(.*?\)\s+\)'
content = re.sub(
    fresher_pattern,
    r'CvCustomLargeTextField(\n                        label = "",\n                        value = cvData.\1,\n                        onValueChange = { onCvDataChange(cvData.copy(\1 = it)) },\n                        themeColors = themeColors,\n                        isLiveEdit = isLiveEdit,\n                        isBn = isBn,\n                        minLines = 3,\n                        placeholderText = "\2"\n                    )',
    content, flags=re.DOTALL
)

# 6. Update LIVE_EDIT block calls
content = content.replace(
    'ProfileAndPersonasTab(\n                cvData = cvData,\n                profilesList = emptyList(),\n                onCvDataChange = onCvDataChange,',
    'ProfileAndPersonasTab(\n                cvData = cvData,\n                profilesList = emptyList(),\n                onCvDataChange = onCvDataChange,\n                isLiveEdit = true,'
)
content = content.replace(
    'ExperienceTab(\n                cvData = cvData,\n                onCvDataChange = onCvDataChange,',
    'ExperienceTab(\n                cvData = cvData,\n                onCvDataChange = onCvDataChange,\n                isLiveEdit = true,'
)
content = content.replace(
    'EducationAndSkillsTab(\n                cvData = cvData,\n                onCvDataChange = onCvDataChange,',
    'EducationAndSkillsTab(\n                cvData = cvData,\n                onCvDataChange = onCvDataChange,\n                isLiveEdit = true,'
)

with open(file_path, "w") as f:
    f.write(content)
print("Applied all Live Edit Save button changes")
