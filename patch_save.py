import re

with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "r") as f:
    content = f.read()

new_save_logic = """
            onSaveProfile = { newLabel ->
                val existingProfile = profilesList.find { it.profileLabel.trim().equals(newLabel.trim(), ignoreCase = true) }
                
                val targetId = if (existingProfile != null && existingProfile.id.isNotBlank()) {
                    existingProfile.id
                } else if (newLabel.trim().equals(cvData.profileLabel.trim(), ignoreCase = true) && cvData.id.isNotBlank() && !cvData.id.startsWith("profile_")) {
                    cvData.id
                } else {
                    "custom_profile_" + java.util.UUID.randomUUID().toString()
                }

                val updated = cvData.copy(id = targetId, profileLabel = newLabel)
                updateCvDataState(updated)
                activeProfileId = targetId
                saveActiveProfileId(context, targetId)
                showSaveProfileDialog = false
                showToast(if (isBn) "প্রোফাইল সফলভাবে সেভ হয়েছে!" else "Profile saved successfully!")
            }
"""

content = re.sub(r'onSaveProfile = \{ newLabel ->.*?(?=\n\s*\)\n\s*\})', new_save_logic.strip(), content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "w") as f:
    f.write(content)
