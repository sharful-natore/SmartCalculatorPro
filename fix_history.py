import re

with open('app/src/main/java/com/example/ui/screens/HistoryScreen.kt', 'r') as f:
    content = f.read()

# Add missing imports for DropdownMenu, DropdownMenuItem
imports = """
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
"""
content = content.replace('import androidx.compose.material.icons.filled.Sort\n', 'import androidx.compose.material.icons.filled.Sort\n' + imports)

# Add showSortMenu var
content = content.replace(
    'var isAscending by remember { mutableStateOf(false) }',
    'var isAscending by remember { mutableStateOf(false) }\n    var showSortMenu by remember { mutableStateOf(false) }'
)

# Update Sort IconButton
old_sort_button = """                        IconButton(
                            onClick = { isAscending = !isAscending }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort",
                                tint = if (isAscending) themeColors.buttonEqualBg else themeColors.buttonEqualBg.copy(alpha=0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                        }"""

new_sort_button = """                        Box {
                            IconButton(
                                onClick = { showSortMenu = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Sort",
                                    tint = themeColors.buttonEqualBg,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                modifier = Modifier.background(themeColors.cardBg)
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (isBn) "নতুন আগে" else "Newest First", color = themeColors.displayText) },
                                    onClick = {
                                        isAscending = false
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (isBn) "পুরোনো আগে" else "Oldest First", color = themeColors.displayText) },
                                    onClick = {
                                        isAscending = true
                                        showSortMenu = false
                                    }
                                )
                            }
                        }"""

content = content.replace(old_sort_button, new_sort_button)

with open('app/src/main/java/com/example/ui/screens/HistoryScreen.kt', 'w') as f:
    f.write(content)

print("History sorted updated")
