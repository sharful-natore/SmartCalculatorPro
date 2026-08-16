import re

with open("app/src/main/java/com/example/ui/namaz/NamazEducationScreen.kt", "r", encoding="utf-8") as f:
    content = f.read()

# Replace wuduSteps
content = content.replace("NamazDataRepository.wuduSteps", "NamazDataRepository.wuduDuas")
# Replace wuduItem fields
content = content.replace("wuduItem.arabicDua", "wuduItem.arabicText")
content = content.replace("wuduItem.banglaDuaPronunciation", "wuduItem.banglaPronunciation")
content = content.replace("wuduItem.banglaDuaMeaning", "wuduItem.banglaMeaning")

# Replace wuduSunnahList
content = content.replace("NamazDataRepository.wuduSunnahList", "NamazDataRepository.wuduSunnahSteps")

# The List<PrayerStep> vs Int issue in Waqt
content = content.replace("items(selectedWaqt.steps3Rakat ?: selectedWaqt.steps4Rakat ?: selectedWaqt.steps2Rakat)", "items(selectedWaqt.steps4Rakat ?: selectedWaqt.steps3Rakat ?: selectedWaqt.steps2Rakat ?: emptyList())")


with open("app/src/main/java/com/example/ui/namaz/NamazEducationScreen.kt", "w", encoding="utf-8") as f:
    f.write(content)

