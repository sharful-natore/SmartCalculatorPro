import re
with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "r") as f:
    content = f.read()

target = """            onDeletePdf = { item ->
                deleteCvHistoryItem(context, item.id)
                historyList = loadCvHistory(context)
                showToast(if (isBn) "হিস্টোরি আইটেম মোছা হয়েছে" else "History item deleted")
            },
            onClearAllHistory = {"""

replacement = """            onDeletePdf = { item ->
                deleteCvHistoryItem(context, item.id)
                historyList = loadCvHistory(context)
                showToast(if (isBn) "হিস্টোরি আইটেম মোছা হয়েছে" else "History item deleted")
            },
            onEditProfile = { item ->
                val profile = profilesList.find { it.profileLabel == item.profileLabel }
                if (profile != null) {
                    updateCvDataState(profile)
                    activeProfileId = profile.id
                    saveActiveProfileId(context, profile.id)
                    showToast(if (isBn) "প্রোফাইল লোড করা হয়েছে" else "Profile loaded")
                } else {
                    showToast(if (isBn) "প্রোফাইল ডেটা পাওয়া যায়নি" else "Profile data not found")
                }
            },
            onClearAllHistory = {"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "w") as f:
        f.write(content)
    print("Success history caller")
else:
    print("Target not found for history caller")
