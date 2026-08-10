import re

with open('app/src/main/java/com/example/ui/screens/BasicScientificScreen.kt', 'r') as f:
    sci_content = f.read()

old_drag_handle = """        // Drag Handle / Visual cue for swiping
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .then(dragModifier)
                .clickable {
                    coroutineScope.launch {
                        if (viewModel.isScientificExpanded) {
                            expansionAnimatable.animateTo(0f)
                            viewModel.isScientificExpanded = false
                        } else {
                            expansionAnimatable.animateTo(1f)
                            viewModel.isScientificExpanded = true
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = buttonPadding)
                    .clip(RoundedCornerShape(8.dp))
                    .background(androidx.compose.ui.graphics.Color.Transparent)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Hide Scientific Mode" else "Show Scientific Mode",
                    tint = themeColors.displayText.copy(alpha = 0.7f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }"""

new_drag_handle = """        // Drag Handle / Visual cue for swiping
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(dragModifier)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    coroutineScope.launch {
                        if (viewModel.isScientificExpanded) {
                            expansionAnimatable.animateTo(0f)
                            viewModel.isScientificExpanded = false
                        } else {
                            expansionAnimatable.animateTo(1f)
                            viewModel.isScientificExpanded = true
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Hide Scientific Mode" else "Show Scientific Mode",
                tint = themeColors.displayText.copy(alpha = 0.7f),
                modifier = Modifier.size(28.dp)
            )
        }"""

if old_drag_handle in sci_content:
    sci_content = sci_content.replace(old_drag_handle, new_drag_handle)
    with open('app/src/main/java/com/example/ui/screens/BasicScientificScreen.kt', 'w') as f:
        f.write(sci_content)
    print("BasicScientificScreen.kt updated successfully.")
else:
    print("Could not find Drag Handle in BasicScientificScreen.kt")
