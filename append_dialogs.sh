cat << 'INNER_EOF' >> app/src/main/java/com/example/ui/MainApp.kt

@Composable
fun UnfavoriteConfirmDialog(
    itemType: String, // "Tool" or "Converter"
    itemNameBn: String,
    itemNameEn: String,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBn) "প্রিয় তালিকা থেকে সরান" else "Remove from Favorites",
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )
        },
        text = {
            Text(
                text = if (isBn) "$itemNameBn -কে প্রিয় তালিকা থেকে সরাতে চান?" else "Remove $itemNameEn from favorites?",
                color = themeColors.displayText.copy(alpha = 0.8f)
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = if (isBn) "সরান" else "Remove",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = if (isBn) "বাতিল" else "Cancel",
                    color = themeColors.displayText
                )
            }
        },
        containerColor = themeColors.cardBg,
        titleContentColor = themeColors.displayText,
        textContentColor = themeColors.displayText
    )
}
INNER_EOF
