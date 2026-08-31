import re
with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "r") as f:
    content = f.read()

target1 = """    onDeleteProfile: (CvData) -> Unit,
    onImportPdfResume: () -> Unit
) {
    AlertDialog("""

replacement1 = """    onDeleteProfile: (CvData) -> Unit,
    onImportPdfResume: () -> Unit
) {
    var profileToDelete by remember { mutableStateOf<CvData?>(null) }
    var profileToSelect by remember { mutableStateOf<CvData?>(null) }
    
    if (profileToDelete != null) {
        AlertDialog(
            onDismissRequest = { profileToDelete = null },
            confirmButton = {
                Button(onClick = {
                    profileToDelete?.let { onDeleteProfile(it) }
                    profileToDelete = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text(if (isBn) "হ্যাঁ, মুছুন" else "Yes, Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { profileToDelete = null }) {
                    Text(if (isBn) "না" else "No")
                }
            },
            title = { Text(if (isBn) "নিশ্চিত করুন" else "Confirm Delete") },
            text = { Text(if (isBn) "আপনি কি নিশ্চিত যে এই প্রোফাইলটি ডিলিট করতে চান?" else "Are you sure you want to delete this profile?") }
        )
    }

    if (profileToSelect != null) {
        AlertDialog(
            onDismissRequest = { profileToSelect = null },
            confirmButton = {
                Button(onClick = {
                    profileToSelect?.let { onSelectProfile(it) }
                    profileToSelect = null
                    onDismiss()
                }, colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)) {
                    Text(if (isBn) "হ্যাঁ" else "Yes", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { profileToSelect = null }) {
                    Text(if (isBn) "না" else "No")
                }
            },
            title = { Text(if (isBn) "নিশ্চিত করুন" else "Confirm Selection") },
            text = { Text(if (isBn) "আপনি কি এই প্রোফাইলটি ব্যবহার করতে চান? বর্তমান প্রোফাইলের অসংরক্ষিত ডেটা মুছে যেতে পারে।" else "Do you want to switch to this profile? Unsaved data may be lost.") }
        )
    }

    AlertDialog("""

target2 = """                                        IconButton(
                                            onClick = { onDeleteProfile(profile) },
                                            modifier = Modifier.size(28.dp)
                                        ) {"""
                                        
replacement2 = """                                        IconButton(
                                            onClick = { profileToDelete = profile },
                                            modifier = Modifier.size(28.dp)
                                        ) {"""
                                        
target3 = """                                        OutlinedButton(
                                            onClick = {
                                                onSelectProfile(profile)
                                                onDismiss()
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {"""
                                        
replacement3 = """                                        OutlinedButton(
                                            onClick = { profileToSelect = profile },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {"""

if target1 in content and target2 in content and target3 in content:
    content = content.replace(target1, replacement1)
    content = content.replace(target2, replacement2)
    content = content.replace(target3, replacement3)
    with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "w") as f:
        f.write(content)
    print("Success profile confirm")
else:
    print("Target not found for profile confirm")
