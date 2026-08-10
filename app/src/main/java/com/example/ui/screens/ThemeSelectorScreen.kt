package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import com.example.util.LanguageManager
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.util.scaleOnPress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectorScreen(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val bounceAnimatable = remember { Animatable(0f) }
    
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val currentValue = bounceAnimatable.value
                if (currentValue != 0f) {
                    if ((currentValue < 0f && available.y > 0f) || (currentValue > 0f && available.y < 0f)) {
                        val newDelta = available.y * 0.35f
                        val newValue = if (currentValue < 0f) {
                            (currentValue + newDelta).coerceAtMost(0f)
                        } else {
                            (currentValue + newDelta).coerceAtLeast(0f)
                        }
                        coroutineScope.launch {
                            bounceAnimatable.snapTo(newValue)
                        }
                        return Offset(0f, available.y)
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y != 0f) {
                    coroutineScope.launch {
                        bounceAnimatable.snapTo((bounceAnimatable.value + available.y * 0.35f).coerceIn(-140f, 140f))
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                bounceAnimatable.animateTo(
                    0f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                )
                return super.onPostFling(consumed, available)
            }
        }
    }

    LaunchedEffect(scrollState.isScrollInProgress) {
        if (!scrollState.isScrollInProgress && bounceAnimatable.value != 0f) {
            coroutineScope.launch {
                bounceAnimatable.animateTo(
                    0f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                )
            }
        }
    }
    
    var showAddThemeDialog by remember { mutableStateOf(false) }
    var themeToEdit by remember { mutableStateOf<CustomTheme?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<String?>(null) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
    ) {
        val screenHeight = maxHeight
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = screenHeight)
                .nestedScroll(nestedScrollConnection)
                .offset { IntOffset(0, bounceAnimatable.value.roundToInt()) }
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
        // --- Preset Themes Section ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = themeColors.buttonEqualBg,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isBn) "প্রিসেট থিমসমূহ" else "Preset Themes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )
        }

        val presetThemes = CalculatorThemeType.values()
        presetThemes.toList().chunked(2).forEach { rowThemes ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowThemes.forEach { themeType ->
                    val colors = themeType.getColors()
                    val isSelected = !viewModel.isCustomThemeActive && viewModel.currentThemeType == themeType
                    
                    Box(modifier = Modifier.weight(1f)) {
                        ThemeCard(
                            name = if (isBn) colors.themeNameBn else colors.themeName,
                            colors = colors,
                            isSelected = isSelected,
                            currentThemeColors = themeColors,
                            onClick = { viewModel.setTheme(themeType) },
                            onLongClick = {
                                // Convert preset to a new custom theme for editing
                                themeToEdit = CustomTheme(
                                    name = colors.themeName + " (Edit)",
                                    background = colors.background.toHexString(),
                                    displayBackground = colors.displayBackground.toHexString(),
                                    displayText = colors.displayText.toHexString(),
                                    displayExpressionText = colors.displayExpressionText.toHexString(),
                                    buttonNormalBg = colors.buttonNormalBg.toHexString(),
                                    buttonNormalText = colors.buttonNormalText.toHexString(),
                                    buttonOperatorBg = colors.buttonOperatorBg.toHexString(),
                                    buttonOperatorText = colors.buttonOperatorText.toHexString(),
                                    buttonFunctionBg = colors.buttonFunctionBg.toHexString(),
                                    buttonFunctionText = colors.buttonFunctionText.toHexString(),
                                    buttonEqualBg = colors.buttonEqualBg.toHexString(),
                                    buttonEqualText = colors.buttonEqualText.toHexString(),
                                    cardBg = colors.cardBg.toHexString(),
                                    unselectedItemText = colors.unselectedItemText.toHexString(),
                                    navBarBg = colors.navBarBg.toHexString(),
                                    titleBarBg = colors.titleBarBg.toHexString(),
                                    chipBg = colors.chipBg.toHexString(),
                                    isDark = colors.isDark
                                )
                                showAddThemeDialog = true
                            }
                        )
                    }
                }
                if (rowThemes.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Custom Themes Section ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = themeColors.buttonEqualBg,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBn) "কাস্টম থিমসমূহ" else "Custom Themes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
            }
            
            IconButton(
                onClick = { 
                    themeToEdit = null
                    showAddThemeDialog = true 
                },
                colors = IconButtonDefaults.iconButtonColors(containerColor = themeColors.buttonEqualBg, contentColor = Color.White),
                modifier = Modifier.size(32.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Theme", modifier = Modifier.size(20.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        if (viewModel.customThemes.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isBn) "কোনো কাস্টম থিম নেই" else "No custom themes created yet",
                        color = themeColors.displayExpressionText,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            viewModel.customThemes.chunked(2).forEach { rowThemes ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowThemes.forEach { customTheme ->
                        val colors = customTheme.toCalculatorThemeColors()
                        val isSelected = viewModel.isCustomThemeActive && viewModel.currentCustomThemeId == customTheme.id
                        
                        Box(modifier = Modifier.weight(1f)) {
                            ThemeCard(
                                name = customTheme.name,
                                colors = colors,
                                isSelected = isSelected,
                                currentThemeColors = themeColors,
                                onClick = { viewModel.setCustomTheme(customTheme.id) },
                                onLongClick = { showDeleteConfirmDialog = customTheme.id },
                                onEditClick = {
                                    themeToEdit = customTheme
                                    showAddThemeDialog = true
                                }
                            )
                        }
                    }
                    if (rowThemes.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // --- Add/Edit Theme Dialog ---
    if (showAddThemeDialog) {
        CustomThemeEditorDialog(
            themeToEdit = themeToEdit,
            onDismiss = { showAddThemeDialog = false },
            onSave = { theme ->
                if (themeToEdit != null) {
                    viewModel.updateCustomTheme(theme)
                } else {
                    viewModel.addCustomTheme(theme)
                }
                showAddThemeDialog = false
            },
            isBn = isBn,
            themeColors = themeColors
        )
    }

    // --- Delete Confirm Dialog ---
    if (showDeleteConfirmDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text(text = if (isBn) "থিম ডিলিট করুন" else "Delete Theme") },
            text = { Text(text = if (isBn) "আপনি কি নিশ্চিত যে আপনি এই থিমটি ডিলিট করতে চান?" else "Are you sure you want to delete this theme?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmDialog?.let { viewModel.deleteCustomTheme(it) }
                    showDeleteConfirmDialog = null
                }) {
                    Text(text = if (isBn) "ডিলিট" else "Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text(text = if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ThemeCard(
    name: String,
    colors: CalculatorThemeColors,
    isSelected: Boolean,
    currentThemeColors: CalculatorThemeColors,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .scaleOnPress(interactionSource)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = colors.background),
        border = if (isSelected) BorderStroke(3.dp, currentThemeColors.buttonEqualBg) else BorderStroke(1.dp, colors.buttonNormalBg.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.displayText,
                    maxLines = 1
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onEditClick != null) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = colors.displayText.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp).clickable { onEditClick() }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = currentThemeColors.buttonEqualBg,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Color Palette Circles Showcase
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ColorDot(colors.navBarBg)
                ColorDot(colors.buttonEqualBg)
                ColorDot(colors.buttonOperatorBg)
                ColorDot(colors.buttonFunctionBg)
            }
        }
    }
}

@Composable
fun ColorDot(color: Color) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomThemeEditorDialog(
    themeToEdit: CustomTheme?,
    onDismiss: () -> Unit,
    onSave: (CustomTheme) -> Unit,
    isBn: Boolean,
    themeColors: CalculatorThemeColors
) {
    var name by remember(themeToEdit) { mutableStateOf(themeToEdit?.name ?: "") }
    var isDark by remember(themeToEdit) { mutableStateOf(themeToEdit?.isDark ?: false) }

    var background by remember(themeToEdit) { mutableStateOf(safeParseColor(themeToEdit?.background, if (isDark) "#121212" else "#F8FAFC")) }
    var displayBackground by remember(themeToEdit) { mutableStateOf(safeParseColor(themeToEdit?.displayBackground, if (isDark) "#1E293B" else "#FFFFFF")) }
    var displayText by remember(themeToEdit) { mutableStateOf(safeParseColor(themeToEdit?.displayText, if (isDark) "#FFFFFF" else "#1E293B")) }
    var displayExpressionText by remember(themeToEdit) { mutableStateOf(safeParseColor(themeToEdit?.displayExpressionText, if (isDark) "#94A3B8" else "#64748B")) }
    
    var buttonNormalBg by remember(themeToEdit) { mutableStateOf(safeParseColor(themeToEdit?.buttonNormalBg, if (isDark) "#1E293B" else "#FFFFFF")) }
    var buttonNormalText by remember(themeToEdit) { mutableStateOf(safeParseColor(themeToEdit?.buttonNormalText, if (isDark) "#FFFFFF" else "#1E293B")) }
    
    var buttonOperatorBg by remember(themeToEdit) { mutableStateOf(safeParseColor(themeToEdit?.buttonOperatorBg, "#E8DDFF")) }
    var buttonOperatorText by remember(themeToEdit) { mutableStateOf(safeParseColor(themeToEdit?.buttonOperatorText, "#6366F1")) }
    
    var buttonFunctionBg by remember(themeToEdit) { mutableStateOf(safeParseColor(themeToEdit?.buttonFunctionBg, "#E8DDFF")) }
    var buttonFunctionText by remember(themeToEdit) { mutableStateOf(safeParseColor(themeToEdit?.buttonFunctionText, "#6366F1")) }
    
    var buttonEqualBg by remember(themeToEdit) { mutableStateOf(safeParseColor(themeToEdit?.buttonEqualBg, "#6366F1")) }
    var buttonEqualText by remember(themeToEdit) { mutableStateOf(safeParseColor(themeToEdit?.buttonEqualText, "#FFFFFF")) }
    
    var cardBg by remember(themeToEdit) { mutableStateOf(safeParseColor(themeToEdit?.cardBg, if (isDark) "#1E293B" else "#FFFFFF")) }
    var unselectedItemText by remember(themeToEdit) { mutableStateOf(safeParseColor(themeToEdit?.unselectedItemText, if (isDark) "#94A3B8" else "#64748B")) }
    var navBarBg by remember(themeToEdit) { mutableStateOf(safeParseColor(themeToEdit?.navBarBg, "#6366F1")) }
    var titleBarBg by remember(themeToEdit) { mutableStateOf(safeParseColor(themeToEdit?.titleBarBg, "#6366F1")) }
    var chipBg by remember(themeToEdit) { mutableStateOf(safeParseColor(themeToEdit?.chipBg, "#E8DDFF")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        title = {
            Text(
                text = if (themeToEdit != null) (if (isBn) "থিম এডিট করুন" else "Edit Theme") else (if (isBn) "নতুন থিম" else "New Theme"),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isBn) "থিমের নাম" else "Theme Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isDark, onCheckedChange = { isDark = it })
                    Text(text = if (isBn) "ডার্ক মোড" else "Dark Mode")
                }

                Text(text = if (isBn) "রংসমূহ (Theme Colors Structure)" else "Theme Colors Structure", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                ColorPickerRow(if (isBn) "অ্যাপ ব্যাকগ্রাউন্ড (Background)" else "App Background", background) { background = it }
                ColorPickerRow(if (isBn) "ডিসপ্লে ব্যাকগ্রাউন্ড (Display BG)" else "Display Background", displayBackground) { displayBackground = it }
                ColorPickerRow(if (isBn) "ডিসপ্লে মূল টেক্সট (Display Text)" else "Display Main Text", displayText) { displayText = it }
                ColorPickerRow(if (isBn) "ডিসপ্লে এক্সপ্রেশন টেক্সট (Expression Text)" else "Display Expression Text", displayExpressionText) { displayExpressionText = it }
                
                ColorPickerRow(if (isBn) "নরমাল বাটন ব্যাকগ্রাউন্ড (Normal Button BG)" else "Normal Button Background", buttonNormalBg) { buttonNormalBg = it }
                ColorPickerRow(if (isBn) "নরমাল বাটন টেক্সট (Normal Button Text)" else "Normal Button Text", buttonNormalText) { buttonNormalText = it }
                
                ColorPickerRow(if (isBn) "অপারেটর বাটন ব্যাকগ্রাউন্ড (+, - ইত্যাদি)" else "Operator Button Background", buttonOperatorBg) { buttonOperatorBg = it }
                ColorPickerRow(if (isBn) "অপারেটর বাটন টেক্সট" else "Operator Button Text", buttonOperatorText) { buttonOperatorText = it }
                
                ColorPickerRow(if (isBn) "ফাংশন বাটন ব্যাকগ্রাউন্ড (C, AC ইত্যাদি)" else "Function Button Background", buttonFunctionBg) { buttonFunctionBg = it }
                ColorPickerRow(if (isBn) "ফাংশন বাটন টেক্সট" else "Function Button Text", buttonFunctionText) { buttonFunctionText = it }
                
                ColorPickerRow(if (isBn) "সমান/প্রাইমারি বাটন ব্যাকগ্রাউন্ড (=)" else "Equal Button Background", buttonEqualBg) { buttonEqualBg = it }
                ColorPickerRow(if (isBn) "সমান বাটন টেক্সট" else "Equal Button Text", buttonEqualText) { buttonEqualText = it }
                
                ColorPickerRow(if (isBn) "কার্ড ব্যাকগ্রাউন্ড (Card BG)" else "Card Background", cardBg) { cardBg = it }
                ColorPickerRow(if (isBn) "আনসিলেক্টেড টেক্সট (Unselected Text)" else "Unselected Item Text", unselectedItemText) { unselectedItemText = it }
                ColorPickerRow(if (isBn) "ন্যাভবার ব্যাকগ্রাউন্ড (Navbar BG)" else "Navbar Background", navBarBg) { navBarBg = it }
                ColorPickerRow(if (isBn) "টাইটেল বার ব্যাকগ্রাউন্ড (Title Bar BG)" else "Title Bar Background", titleBarBg) { titleBarBg = it }
                ColorPickerRow(if (isBn) "চিপ ব্যাকগ্রাউন্ড (Chip BG)" else "Chip Background", chipBg) { chipBg = it }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val newTheme = CustomTheme(
                            id = themeToEdit?.id ?: java.util.UUID.randomUUID().toString(),
                            name = name,
                            background = background.toHexString(),
                            displayBackground = displayBackground.toHexString(),
                            displayText = displayText.toHexString(),
                            displayExpressionText = displayExpressionText.toHexString(),
                            buttonNormalBg = buttonNormalBg.toHexString(),
                            buttonNormalText = buttonNormalText.toHexString(),
                            buttonOperatorBg = buttonOperatorBg.toHexString(),
                            buttonOperatorText = buttonOperatorText.toHexString(),
                            buttonFunctionBg = buttonFunctionBg.toHexString(),
                            buttonFunctionText = buttonFunctionText.toHexString(),
                            buttonEqualBg = buttonEqualBg.toHexString(),
                            buttonEqualText = buttonEqualText.toHexString(),
                            cardBg = cardBg.toHexString(),
                            unselectedItemText = unselectedItemText.toHexString(),
                            navBarBg = navBarBg.toHexString(),
                            titleBarBg = titleBarBg.toHexString(),
                            chipBg = chipBg.toHexString(),
                            isDark = isDark
                        )
                        onSave(newTheme)
                    }
                },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isBn) "সংরক্ষণ" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বাতিল" else "Cancel")
            }
        }
    )
}

@Composable
fun ColorPickerRow(label: String, selectedColor: Color, onColorSelected: (Color) -> Unit) {
    var showPickerDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Gray.copy(alpha = 0.05f))
            .clickable { showPickerDialog = true }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = selectedColor.toHexString().uppercase().replace("#FF", "#"),
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(end = 8.dp)
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(selectedColor)
                    .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            )
        }
    }

    if (showPickerDialog) {
        AdvancedColorPickerDialog(
            initialColor = selectedColor,
            onDismiss = { showPickerDialog = false },
            onColorSelected = {
                onColorSelected(it)
                showPickerDialog = false
            }
        )
    }
}

@Composable
fun AdvancedColorPickerDialog(
    initialColor: Color,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    var hsv by remember { 
        val hsvArr = FloatArray(3)
        android.graphics.Color.colorToHSV(initialColor.toArgb(), hsvArr)
        mutableStateOf(Triple(hsvArr[0], hsvArr[1], hsvArr[2]))
    }
    var alpha by remember { mutableStateOf(initialColor.alpha) }
    
    val currentColor = Color.hsv(hsv.first, hsv.second, hsv.third, alpha)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        title = { Text("Pick Color", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(currentColor)
                        .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentColor.toHexString().uppercase(),
                        color = if (hsv.third > 0.5f || alpha < 0.5f) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                
                // Hue Slider
                Text("Hue: ${hsv.first.toInt()}°", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = hsv.first,
                    onValueChange = { hsv = hsv.copy(first = it) },
                    valueRange = 0f..360f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.hsv(hsv.first, 1f, 1f),
                        activeTrackColor = Color.hsv(hsv.first, 1f, 1f).copy(alpha = 0.5f)
                    )
                )
                
                // Saturation Slider
                Text("Saturation: ${(hsv.second * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = hsv.second,
                    onValueChange = { hsv = hsv.copy(second = it) },
                    valueRange = 0f..1f
                )
                
                // Value (Brightness) Slider
                Text("Brightness: ${(hsv.third * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = hsv.third,
                    onValueChange = { hsv = hsv.copy(third = it) },
                    valueRange = 0f..1f
                )
                
                // Alpha Slider
                Text("Transparency: ${(alpha * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = alpha,
                    onValueChange = { alpha = it },
                    valueRange = 0f..1f
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onColorSelected(currentColor) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
