import os

file_path = "./app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt"

if not os.path.exists(file_path):
    print(f"Error: {file_path} does not exist!")
    exit(1)

print(f"Found file at {file_path}")

with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

target = """                    4 -> AiJobCircularMatchTab(
                        cvData = cvData,
                        onCvDataChange = { updateCvDataState(it) },
                        themeColors = themeColors,
                        isBn = isBn,
                        onMatchCircularAi = { circularText, imageBytes, imageMime ->
                            if (circularText.isBlank() && imageBytes == null) {
                                showToast(if (isBn) "অন্গ্রহ করে সার্কুলার টেক্সট দিন অথবা ছবি আপলোড করুন!" else "Please provide circular text or pick an image!")
                                return@AiJobCircularMatchTab
                            }
                            updateCvDataState(cvData.copy(targetJobCircular = circularText))

                            val promptBuilder = StringBuilder()
                            promptBuilder.append("Target Job Circular Context:\\n")
                            if (circularText.isNotBlank()) {
                                promptBuilder.append("Circular Text: $circularText\\n")
                            }
                            if (imageBytes != null) {
                                promptBuilder.append("[Image of job circular attached below for detail analysis]\\n")
                            }

                            promptBuilder.append("\\nCandidate Profile:\\n")
                            promptBuilder.append("Target Title: ${cvData.jobTitle}\\n")
                            promptBuilder.append("Summary: ${cvData.summary}\\n")
                            promptBuilder.append("Skills: ${cvData.skills.joinToString { it.name }}\\n")
                            promptBuilder.append("Experiences: ${cvData.experiences.joinToString { "${it.role} at ${it.company}: ${it.description}" }}\\n")
                            promptBuilder.append("Educations: ${cvData.educations.joinToString { "${it.degree} from ${it.institution}" }}\\n")

                            promptBuilder.append("\\nTask Instructions:\\n")
                            promptBuilder.append("1. Compute an accurate job match score (0-100%) by comparing candidate skills, qualifications, and role requirements against the circular.\\n")
                            promptBuilder.append("2. Extract 3-5 matching strengths that align with circular requirements.\\n")
                            promptBuilder.append("3. Extract 3-6 missing critical technical or role keywords present in circular but missing in candidate CV.\\n")
                            promptBuilder.append("4. Provide 3 high-impact improvement recommendations for passing the ATS filter.\\n")
                            promptBuilder.append("5. Write a tailored summary aligned with the circular.\\n")
                            promptBuilder.append("6. Return strictly valid JSON object with keys: matchPercentage (int), tailoredSummary (string), matchingStrengths (array of string), missingKeywords (array of string), improvementTips (array of string), newSkills (array of string).")

                            openAiPrompt(
                                title = if (isBn) "সার্কুলার ম্যাচ এআই প্রম্পট" else "AI Job Circular Match Prompt",
                                defaultPrompt = promptBuilder.toString(),
                                targetField = "CIRCULAR_MATCH",
                                imageBytes = imageBytes,
                                imageMime = imageMime
                            )
                        }
                    )"""

replacement = """                    4 -> AiJobCircularMatchTab(
                        cvData = cvData,
                        onCvDataChange = { updateCvDataState(it) },
                        themeColors = themeColors,
                        isBn = isBn,
                        onMatchCircularAi = { circularText, imageBytes, imageMime ->
                            runJobCircularMatchWithAi(circularText, imageBytes, imageMime)
                        },
                        onAtsAuditAi = {
                            runGeneralAtsAuditWithAi()
                        },
                        onApplySelectedAtsImprovements = { selectedTips ->
                            applySelectedAtsImprovementsWithAi(selectedTips)
                        }
                    )"""

# Check target block in content by doing a substring find
start_str = "4 -> AiJobCircularMatchTab("
start_idx = content.find(start_str)
if start_idx != -1:
    end_idx = content.find("5 -> PreviewAndExportTab", start_idx)
    if end_idx != -1:
        target_block = content[start_idx:end_idx]
        last_bracket = target_block.rfind(")")
        if last_bracket != -1:
            exact_target = target_block[:last_bracket+1]
            content = content.replace(exact_target, replacement.strip())
            with open(file_path, "w", encoding="utf-8") as f:
                f.write(content)
            print("REPLACEMENT SUCCESSFUL VIA BULK SUBSTRING")
        else:
            print("Could not find ending bracket")
    else:
        print("Could not find next tab")
else:
    print("Could not find start string")
