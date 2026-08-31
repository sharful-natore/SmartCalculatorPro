import re
with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "r") as f:
    content = f.read()

target1 = """    onDeletePdf: (CvHistoryItem) -> Unit,
    onEditProfile: (CvHistoryItem) -> Unit,
    onClearAllHistory: () -> Unit
) {
    AlertDialog("""

replacement1 = """    onDeletePdf: (CvHistoryItem) -> Unit,
    onEditProfile: (CvHistoryItem) -> Unit,
    onClearAllHistory: () -> Unit
) {
    var itemToDelete by remember { mutableStateOf<CvHistoryItem?>(null) }
    var itemToEdit by remember { mutableStateOf<CvHistoryItem?>(null) }
    
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            confirmButton = {
                Button(onClick = {
                    itemToDelete?.let { onDeletePdf(it) }
                    itemToDelete = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text(if (isBn) "হ্যাঁ, মুছুন" else "Yes, Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(if (isBn) "না" else "No")
                }
            },
            title = { Text(if (isBn) "নিশ্চিত করুন" else "Confirm Delete") },
            text = { Text(if (isBn) "আপনি কি নিশ্চিত যে এই ফাইলটি ডিলিট করতে চান?" else "Are you sure you want to delete this file?") }
        )
    }

    if (itemToEdit != null) {
        AlertDialog(
            onDismissRequest = { itemToEdit = null },
            confirmButton = {
                Button(onClick = {
                    itemToEdit?.let { onEditProfile(it) }
                    itemToEdit = null
                    onDismiss()
                }, colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)) {
                    Text(if (isBn) "হ্যাঁ" else "Yes", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToEdit = null }) {
                    Text(if (isBn) "না" else "No")
                }
            },
            title = { Text(if (isBn) "নিশ্চিত করুন" else "Confirm Edit") },
            text = { Text(if (isBn) "আপনি কি এই প্রোফাইলটি এডিট করতে চান? বর্তমান প্রোফাইলের অসংরক্ষিত ডেটা মুছে যেতে পারে।" else "Do you want to edit this profile? Unsaved data in the current profile may be lost.") }
        )
    }

    AlertDialog("""

target2 = """                                    IconButton(
                                        onClick = { onDeletePdf(item) },
                                        modifier = Modifier.size(24.dp)
                                    ) {"""
                                    
replacement2 = """                                    IconButton(
                                        onClick = { itemToDelete = item },
                                        modifier = Modifier.size(24.dp)
                                    ) {"""
                                    
target3 = """                                            OutlinedButton(
                                                onClick = { onEditProfile(item); onDismiss() },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(26.dp)
                                            ) {"""
                                            
replacement3 = """                                            OutlinedButton(
                                                onClick = { itemToEdit = item },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(26.dp)
                                            ) {"""

if target1 in content and target2 in content and target3 in content:
    content = content.replace(target1, replacement1)
    content = content.replace(target2, replacement2)
    content = content.replace(target3, replacement3)
    with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "w") as f:
        f.write(content)
    print("Success history confirm")
else:
    print("Target not found for history confirm")
