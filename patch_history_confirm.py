import re
with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "r") as f:
    content = f.read()

target1 = """    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit,
    onOpenPdf: (CvHistoryItem) -> Unit,
    onSharePdf: (CvHistoryItem) -> Unit,
    onDeletePdf: (CvHistoryItem) -> Unit
) {
    AlertDialog("""

replacement1 = """    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit,
    onOpenPdf: (CvHistoryItem) -> Unit,
    onSharePdf: (CvHistoryItem) -> Unit,
    onDeletePdf: (CvHistoryItem) -> Unit
) {
    var itemToDelete by remember { mutableStateOf<CvHistoryItem?>(null) }
    
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            confirmButton = {
                Button(onClick = {
                    itemToDelete?.let { onDeletePdf(it) }
                    itemToDelete = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text(if (isBn) "হ্যাঁ, মুছুন" else "Yes, Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(if (isBn) "না" else "No")
                }
            },
            title = { Text(if (isBn) "নিশ্চিত করুন" else "Confirm Delete") },
            text = { Text(if (isBn) "আপনি কি নিশ্চিত যে এই সিভি টি ডিলিট করতে চান?" else "Are you sure you want to delete this CV?") }
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

if target1 in content and target2 in content:
    content = content.replace(target1, replacement1)
    content = content.replace(target2, replacement2)
    with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "w") as f:
        f.write(content)
    print("Success history confirm")
else:
    print("Target not found for history confirm")
