package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.outlined.Science
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.launch
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Mic
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CalculatorButton
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun BasicScientificScreen(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = matches?.firstOrNull()
            if (!recognizedText.isNullOrEmpty()) {
                viewModel.processVoiceInput(recognizedText)
            }
        }
    }

    val isExpanded = viewModel.isScientificExpanded
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val maxDragPx = with(density) { 240.dp.toPx() }

    val expansionAnimatable = remember { Animatable(if (isExpanded) 1f else 0f) }

    LaunchedEffect(isExpanded) {
        expansionAnimatable.animateTo(
            targetValue = if (isExpanded) 1f else 0f,
            animationSpec = spring(stiffness = Spring.StiffnessLow)
        )
    }

    val expansionFraction = expansionAnimatable.value

    val displayWeight = 1.15f - (0.30f * expansionFraction)
    val keypadWeight = 2.85f + (0.30f * expansionFraction)

    val buttonPadding = (2f - (1f * expansionFraction)).dp
    val rowSpacing = (4f - (2f * expansionFraction)).dp
    val basicFontSize = (23f - (4f * expansionFraction)).toInt()
    val scientificFontSize = (14f - (2f * expansionFraction)).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .padding(12.dp)
    ) {
        // 1. Calculator Display Screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(displayWeight)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    if (themeColors.background == themeColors.displayBackground) {
                        themeColors.cardBg.copy(alpha = 0.5f)
                    } else {
                        themeColors.displayBackground
                    }
                )
                .then(
                    if (!themeColors.isDark) {
                        Modifier.border(
                            width = 1.dp,
                            color = themeColors.buttonOperatorBg.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(28.dp)
                        )
                    } else {
                        Modifier.border(
                            width = 1.dp,
                            color = themeColors.displayText.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(28.dp)
                        )
                    }
                )
                .padding((16f - (6f * expansionFraction)).dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                val clipboardManager = LocalClipboardManager.current
                val focusRequester = remember { FocusRequester() }

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
                
                // Animated sizes and colors for Google Calculator transition
                val exprSize by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (viewModel.isEvaluated) {
                        24f
                    } else {
                        if (viewModel.expression.length > 12) 28f else 38f
                    },
                    animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.82f, stiffness = 250f),
                    label = "expr_size"
                )
                val resultSize by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (viewModel.isEvaluated) {
                        if (viewModel.result.length > 10) 34f else 46f
                    } else {
                        24f
                    },
                    animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.82f, stiffness = 250f),
                    label = "result_size"
                )

                val exprColor by androidx.compose.animation.animateColorAsState(
                    targetValue = if (viewModel.isEvaluated) themeColors.displayExpressionText else themeColors.displayText,
                    animationSpec = androidx.compose.animation.core.spring(),
                    label = "expr_color"
                )
                val resultColor by androidx.compose.animation.animateColorAsState(
                    targetValue = if (viewModel.isEvaluated) themeColors.displayText else themeColors.displayExpressionText.copy(alpha = 0.7f),
                    animationSpec = androidx.compose.animation.core.spring(),
                    label = "result_color"
                )

                Spacer(modifier = Modifier.weight(1f))

                // Expression Field with custom cursor and copy/paste support
                var showPasteMenu by remember { mutableStateOf(false) }
                val exprScrollState = rememberScrollState()
                val resultScrollState = rememberScrollState()

                LaunchedEffect(viewModel.expressionValue.text, viewModel.expressionValue.selection) {
                    exprScrollState.animateScrollTo(exprScrollState.maxValue)
                }

                LaunchedEffect(viewModel.result) {
                    resultScrollState.animateScrollTo(resultScrollState.maxValue)
                }
                
                // Cursor blinking logic
                var cursorVisible by remember { mutableStateOf(true) }
                LaunchedEffect(Unit) {
                    while (true) {
                        kotlinx.coroutines.delay(500)
                        cursorVisible = !cursorVisible
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { focusRequester.requestFocus() },
                            onLongClick = { 
                                if (clipboardManager.hasText()) {
                                    showPasteMenu = true 
                                }
                            }
                        )
                ) {
                    BasicTextField(
                        value = viewModel.expressionValue,
                        onValueChange = { viewModel.onExpressionValueChange(it) },
                        readOnly = true, // Force read-only to prevent keyboard
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .testTag("expression_display"),
                        textStyle = TextStyle(
                            color = Color.Transparent, // Text transparent, animated overlay below
                            fontSize = exprSize.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End
                        ),
                        cursorBrush = SolidColor(Color.Transparent), // Hide default cursor
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(exprScrollState),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                // Animated characters layer with custom cursor
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    val text = viewModel.expressionValue.text
                                    val selectionStart = viewModel.expressionValue.selection.start
                                    var prevTextLength by remember { mutableStateOf(text.length) }
                                    
                                    val maxLen = maxOf(text.length, prevTextLength)
                                    SideEffect { prevTextLength = text.length }
                                    
                                    // Handle cursor at the very beginning
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height((exprSize * 1.1f).dp)
                                            .background(if (selectionStart == 0 && cursorVisible) themeColors.displayText else Color.Transparent)
                                    )

                                    (0 until maxLen).forEach { index ->
                                        val char = text.getOrNull(index)
                                        AnimatedVisibility(
                                            visible = char != null,
                                            enter = scaleIn(initialScale = 0.2f, animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)) + fadeIn(),
                                            exit = scaleOut(targetScale = 0.2f, animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)) + fadeOut(),
                                            label = "char_visibility"
                                        ) {
                                            if (char != null) {
                                                AnimatedContent(
                                                    targetState = char,
                                                    transitionSpec = {
                                                        (scaleIn(initialScale = 0.2f, animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)) + fadeIn())
                                                            .togetherWith(scaleOut(targetScale = 0.2f, animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)) + fadeOut())
                                                    },
                                                    label = "char_anim"
                                                ) { animatedChar ->
                                                    Text(
                                                        text = animatedChar.toString(),
                                                        color = exprColor,
                                                        fontSize = exprSize.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.Medium,
                                                        textAlign = TextAlign.End,
                                                        modifier = Modifier.padding(horizontal = 0.5.dp)
                                                    )
                                                }
                                            }
                                        }
                                        
                                        // Cursor after this character
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height((exprSize * 1.1f).dp)
                                                .background(if (index + 1 == selectionStart && cursorVisible) themeColors.displayText else Color.Transparent)
                                        )
                                    }
                                }
                                // Hidden but present for focus/selection
                                innerTextField()
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = showPasteMenu,
                        onDismissRequest = { showPasteMenu = false },
                        containerColor = themeColors.cardBg
                    ) {
                        DropdownMenuItem(
                            text = { Text("Paste", color = themeColors.displayText) },
                            onClick = {
                                clipboardManager.getText()?.let { clipText ->
                                    viewModel.onPaste(clipText.text)
                                }
                                showPasteMenu = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height((10f - (6f * expansionFraction)).dp))

                // Calculated Result string (Click to copy)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(resultScrollState),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = viewModel.result,
                        color = resultColor,
                        fontSize = resultSize.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        modifier = Modifier
                            .clickable {
                                if (viewModel.result.isNotEmpty()) {
                                    clipboardManager.setText(AnnotatedString(viewModel.result))
                                }
                            }
                            .testTag("result_display")
                    )
                }
            }
        }

        // Drag Handle / Visual cue for swiping
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = (4f - (2f * expansionFraction)).dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            coroutineScope.launch { expansionAnimatable.stop() }
                        },
                        onDragEnd = {
                            coroutineScope.launch {
                                if (expansionAnimatable.value > 0.5f) {
                                    expansionAnimatable.animateTo(1f)
                                    viewModel.isScientificExpanded = true
                                } else {
                                    expansionAnimatable.animateTo(0f)
                                    viewModel.isScientificExpanded = false
                                }
                            }
                        },
                        onDragCancel = {
                            coroutineScope.launch {
                                if (expansionAnimatable.value > 0.5f) {
                                    expansionAnimatable.animateTo(1f)
                                    viewModel.isScientificExpanded = true
                                } else {
                                    expansionAnimatable.animateTo(0f)
                                    viewModel.isScientificExpanded = false
                                }
                            }
                        },
                        onVerticalDrag = { change, dragAmount ->
                            val delta = dragAmount / maxDragPx
                            coroutineScope.launch {
                                val target = (expansionAnimatable.value + delta).coerceIn(0f, 1f)
                                expansionAnimatable.snapTo(target)
                            }
                        }
                    )
                }
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
                modifier = Modifier
                    .width(42.dp)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(themeColors.displayText.copy(alpha = 0.25f))
            )
        }

        // Keypad Container (Dynamically scaled, no scroll needed)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(keypadWeight)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            coroutineScope.launch { expansionAnimatable.stop() }
                        },
                        onDragEnd = {
                            coroutineScope.launch {
                                if (expansionAnimatable.value > 0.5f) {
                                    expansionAnimatable.animateTo(1f)
                                    viewModel.isScientificExpanded = true
                                } else {
                                    expansionAnimatable.animateTo(0f)
                                    viewModel.isScientificExpanded = false
                                }
                            }
                        },
                        onDragCancel = {
                            coroutineScope.launch {
                                if (expansionAnimatable.value > 0.5f) {
                                    expansionAnimatable.animateTo(1f)
                                    viewModel.isScientificExpanded = true
                                } else {
                                    expansionAnimatable.animateTo(0f)
                                    viewModel.isScientificExpanded = false
                                }
                            }
                        },
                        onVerticalDrag = { change, dragAmount ->
                            val delta = dragAmount / maxDragPx
                            coroutineScope.launch {
                                val target = (expansionAnimatable.value + delta).coerceIn(0f, 1f)
                                expansionAnimatable.snapTo(target)
                            }
                        }
                    )
                },
            verticalArrangement = Arrangement.spacedBy(rowSpacing)
        ) {
            if (expansionFraction > 0.01f) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(4f * expansionFraction)
                        .graphicsLayer {
                            alpha = expansionFraction
                        },
                    verticalArrangement = Arrangement.spacedBy(rowSpacing)
                ) {
                    // Row 1: Trigonometric, Antilog, Cube
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(rowSpacing)
                    ) {
                        CalculatorButton("sin", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("sin") }, Modifier.weight(1f), fontSize = scientificFontSize, padding = buttonPadding)
                        CalculatorButton("cos", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("cos") }, Modifier.weight(1f), fontSize = scientificFontSize, padding = buttonPadding)
                        CalculatorButton("tan", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("tan") }, Modifier.weight(1f), fontSize = scientificFontSize, padding = buttonPadding)
                        CalculatorButton("DEG", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("DEG") }, Modifier.weight(1f), fontSize = scientificFontSize, padding = buttonPadding)
                        CalculatorButton("antilog", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("antilog") }, Modifier.weight(1f), fontSize = scientificFontSize, padding = buttonPadding)
                        CalculatorButton("x³", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("x³") }, Modifier.weight(1f), fontSize = scientificFontSize, padding = buttonPadding)
                    }
                    // Row 2: Inverse Trig, Square Root, Inverse
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(rowSpacing)
                    ) {
                        CalculatorButton("sin⁻¹", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("sin⁻¹") }, Modifier.weight(1f), fontSize = scientificFontSize, padding = buttonPadding)
                        CalculatorButton("cos⁻¹", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("cos⁻¹") }, Modifier.weight(1f), fontSize = scientificFontSize, padding = buttonPadding)
                        CalculatorButton("tan⁻¹", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("tan⁻¹") }, Modifier.weight(1f), fontSize = scientificFontSize, padding = buttonPadding)
                        CalculatorButton("RAD", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("RAD") }, Modifier.weight(1f), fontSize = scientificFontSize, padding = buttonPadding)
                        CalculatorButton("√", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("√") }, Modifier.weight(1f), fontSize = scientificFontSize, padding = buttonPadding)
                        CalculatorButton("1/x", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("1/x") }, Modifier.weight(1f), fontSize = scientificFontSize, padding = buttonPadding)
                    }
                    // Row 3: Advanced, Log10, Power, Factorial, Square
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(rowSpacing)
                    ) {
                        CalculatorButton("ln", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("ln") }, Modifier.weight(1f), fontSize = scientificFontSize, padding = buttonPadding)
                        CalculatorButton("log", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("log") }, Modifier.weight(1f), fontSize = scientificFontSize, padding = buttonPadding)
                        CalculatorButton("log^10", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("log^10") }, Modifier.weight(1f), fontSize = scientificFontSize, padding = buttonPadding)
                        CalculatorButton("x^y", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("x^y") }, Modifier.weight(1f), fontSize = scientificFontSize, padding = buttonPadding)
                        CalculatorButton("x!", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("x!") }, Modifier.weight(1f), fontSize = scientificFontSize, padding = buttonPadding)
                        CalculatorButton("x²", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("x²") }, Modifier.weight(1f), fontSize = scientificFontSize, padding = buttonPadding)
                    }
                    // Row 4: Constants, Cube Root, Brackets, e^x
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(rowSpacing)
                    ) {
                        CalculatorButton("π", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("π") }, Modifier.weight(1f), fontSize = scientificFontSize, padding = buttonPadding)
                        CalculatorButton("e", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("e") }, Modifier.weight(1f), fontSize = scientificFontSize, padding = buttonPadding)
                        CalculatorButton("3√", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("3√") }, Modifier.weight(1f), fontSize = scientificFontSize, padding = buttonPadding)
                        CalculatorButton("(", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("(") }, Modifier.weight(1f), fontSize = scientificFontSize, padding = buttonPadding)
                        CalculatorButton(")", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick(")") }, Modifier.weight(1f), fontSize = scientificFontSize, padding = buttonPadding)
                        CalculatorButton("e^x", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("e^x") }, Modifier.weight(1f), fontSize = scientificFontSize, padding = buttonPadding)
                    }
                }
            }

            // Basic Keypad Panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(5f),
                verticalArrangement = Arrangement.spacedBy(rowSpacing)
            ) {
                // Row 1: AC, (), %, ÷
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(rowSpacing)
                ) {
                    CalculatorButton("AC", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("AC") }, Modifier.weight(1f), fontSize = basicFontSize, padding = buttonPadding)
                    CalculatorButton("()", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("()") }, Modifier.weight(1f), fontSize = basicFontSize, padding = buttonPadding)
                    CalculatorButton("%", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("%") }, Modifier.weight(1f), fontSize = basicFontSize, padding = buttonPadding)
                    CalculatorButton("÷", themeColors.buttonOperatorBg, themeColors.buttonOperatorText, { viewModel.onBtnClick("÷") }, Modifier.weight(1f), fontSize = basicFontSize, padding = buttonPadding)
                }

                // Row 2: 7, 8, 9, ×
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(rowSpacing)
                ) {
                    CalculatorButton("7", themeColors.buttonNormalBg, themeColors.buttonNormalText, { viewModel.onBtnClick("7") }, Modifier.weight(1f), fontSize = basicFontSize, padding = buttonPadding)
                    CalculatorButton("8", themeColors.buttonNormalBg, themeColors.buttonNormalText, { viewModel.onBtnClick("8") }, Modifier.weight(1f), fontSize = basicFontSize, padding = buttonPadding)
                    CalculatorButton("9", themeColors.buttonNormalBg, themeColors.buttonNormalText, { viewModel.onBtnClick("9") }, Modifier.weight(1f), fontSize = basicFontSize, padding = buttonPadding)
                    CalculatorButton("×", themeColors.buttonOperatorBg, themeColors.buttonOperatorText, { viewModel.onBtnClick("×") }, Modifier.weight(1f), fontSize = basicFontSize, padding = buttonPadding)
                }

                // Row 3: 4, 5, 6, −
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(rowSpacing)
                ) {
                    CalculatorButton("4", themeColors.buttonNormalBg, themeColors.buttonNormalText, { viewModel.onBtnClick("4") }, Modifier.weight(1f), fontSize = basicFontSize, padding = buttonPadding)
                    CalculatorButton("5", themeColors.buttonNormalBg, themeColors.buttonNormalText, { viewModel.onBtnClick("5") }, Modifier.weight(1f), fontSize = basicFontSize, padding = buttonPadding)
                    CalculatorButton("6", themeColors.buttonNormalBg, themeColors.buttonNormalText, { viewModel.onBtnClick("6") }, Modifier.weight(1f), fontSize = basicFontSize, padding = buttonPadding)
                    CalculatorButton("−", themeColors.buttonOperatorBg, themeColors.buttonOperatorText, { viewModel.onBtnClick("−") }, Modifier.weight(1f), fontSize = basicFontSize, padding = buttonPadding)
                }

                // Row 4: 1, 2, 3, +
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(rowSpacing)
                ) {
                    CalculatorButton("1", themeColors.buttonNormalBg, themeColors.buttonNormalText, { viewModel.onBtnClick("1") }, Modifier.weight(1f), fontSize = basicFontSize, padding = buttonPadding)
                    CalculatorButton("2", themeColors.buttonNormalBg, themeColors.buttonNormalText, { viewModel.onBtnClick("2") }, Modifier.weight(1f), fontSize = basicFontSize, padding = buttonPadding)
                    CalculatorButton("3", themeColors.buttonNormalBg, themeColors.buttonNormalText, { viewModel.onBtnClick("3") }, Modifier.weight(1f), fontSize = basicFontSize, padding = buttonPadding)
                    CalculatorButton("+", themeColors.buttonOperatorBg, themeColors.buttonOperatorText, { viewModel.onBtnClick("+") }, Modifier.weight(1f), fontSize = basicFontSize, padding = buttonPadding)
                }

                // Row 5: 0, ., backspace, =
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(rowSpacing)
                ) {
                    CalculatorButton("0", themeColors.buttonNormalBg, themeColors.buttonNormalText, { viewModel.onBtnClick("0") }, Modifier.weight(1f), fontSize = basicFontSize, padding = buttonPadding)
                    CalculatorButton(".", themeColors.buttonNormalBg, themeColors.buttonNormalText, { viewModel.onBtnClick(".") }, Modifier.weight(1f), fontSize = basicFontSize, padding = buttonPadding)
                    CalculatorButton(
                        text = "backspace",
                        bgColor = themeColors.buttonNormalBg,
                        textColor = themeColors.buttonNormalText,
                        onClick = { viewModel.onBtnClick("C") },
                        modifier = Modifier.weight(1f),
                        padding = buttonPadding,
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Backspace",
                                tint = themeColors.buttonNormalText,
                                modifier = Modifier.size(if (isExpanded) 18.dp else 24.dp)
                            )
                        }
                    )
                    CalculatorButton(
                        text = "=",
                        bgColor = themeColors.buttonEqualBg,
                        textColor = themeColors.buttonEqualText,
                        onClick = { viewModel.onBtnClick("=") },
                        onLongClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak calculation (e.g. 500 যোগ 700)")
                            }
                            speechRecognizerLauncher.launch(intent)
                        },
                        modifier = Modifier.weight(1f),
                        fontSize = basicFontSize,
                        padding = buttonPadding,
                        icon = {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = "=",
                                    color = themeColors.buttonEqualText,
                                    fontSize = basicFontSize.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Input",
                                    tint = themeColors.buttonEqualText.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 10.dp, end = 10.dp)
                                        .size(14.dp)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
