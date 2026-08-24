package com.example.ui.screens

import androidx.compose.ui.unit.TextUnit

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowForward
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Info
import com.example.util.AppLanguage
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Science
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.PlatformTextStyle
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

@Composable
fun PillBadge(
    count: Int,
    themeColors: CalculatorThemeColors,
    isLeft: Boolean,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 9.sp
) {
    if (count <= 0) return
    val text = if (isLeft) "${count}+" else "+${count}"
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
            .background(themeColors.buttonEqualBg, CircleShape)
            .padding(horizontal = 4.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = fontSize,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            )
        )
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun CalculatorScreen(
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

    var showCalculatorHistoryOverlay by remember { mutableStateOf(false) }
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

    val expansionFraction = expansionAnimatable.value

    val displayWeight = 1.15f - (0.30f * expansionFraction)
    val keypadWeight = 2.85f + (0.30f * expansionFraction)

    val buttonPadding = (2f - (1f * expansionFraction)).dp
    val rowSpacing = (4f - (2f * expansionFraction)).dp
    val basicFontSize = (23f - (4f * expansionFraction)).toInt()
    val scientificFontSize = (14f - (2f * expansionFraction)).toInt()

    val bounceAnimatable = remember { Animatable(0f) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .offset { IntOffset(0, bounceAnimatable.value.roundToInt()) }
            .pointerInput(viewModel.isScientificExpanded) {
                if (!viewModel.isScientificExpanded) {
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitFirstDown(pass = PointerEventPass.Initial, requireUnconsumed = false)
                            var totalY = 0f
                            var isDragging = false
                            val pointerId = down.id

                            while (true) {
                                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                val change = event.changes.firstOrNull { it.id == pointerId } ?: break

                                if (!change.pressed) {
                                    if (isDragging && kotlin.math.abs(bounceAnimatable.value) > 0.5f) {
                                        coroutineScope.launch {
                                            bounceAnimatable.animateTo(
                                                0f,
                                                spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessLow
                                                )
                                            )
                                        }
                                    }
                                    break
                                }

                                val posChange = change.positionChange()
                                val deltaY = posChange.y
                                val deltaX = posChange.x

                                totalY += deltaY

                                if (!isDragging) {
                                    if (totalY < -12f && kotlin.math.abs(totalY) > kotlin.math.abs(deltaX)) {
                                        isDragging = true
                                    }
                                }

                                if (isDragging) {
                                    if (deltaY < 0 || bounceAnimatable.value < 0f) {
                                        change.consume()
                                        coroutineScope.launch {
                                            bounceAnimatable.snapTo(
                                                (bounceAnimatable.value + (deltaY * 0.35f)).coerceIn(-150f, 0f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
    ) {
        val screenHeight = maxHeight
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = screenHeight)
                .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 17.dp)
        ) {
        // 1. Calculator Display Screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(displayWeight)
                .background(
                    color = if (themeColors.background == themeColors.displayBackground) {
                        themeColors.cardBg.copy(alpha = 0.5f)
                    } else {
                        themeColors.displayBackground
                    },
                    shape = RoundedCornerShape(28.dp)
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
                .padding(
                    start = 0.dp,
                    end = 0.dp,
                    top = (16f - (6f * expansionFraction)).dp,
                    bottom = (16f - (6f * expansionFraction)).dp
                )
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            if (event.type == PointerEventType.Press) {
                                viewModel.isDisplayInteractionActive = true
                            } else if (event.type == PointerEventType.Release) {
                                viewModel.isDisplayInteractionActive = false
                            }
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                    }
                },
            contentAlignment = Alignment.BottomEnd
        ) {
            val clipboardManager = LocalClipboardManager.current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .offset(y = (-10).dp)
                    .padding(
                        start = (16f - (6f * expansionFraction)).dp,
                        end = (16f - (6f * expansionFraction)).dp,
                        top = 0.dp
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val textToCopy = viewModel.expressionValue.text
                            if (textToCopy.isNotEmpty()) {
                                clipboardManager.setText(AnnotatedString(textToCopy))
                            }
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Expression",
                            tint = themeColors.displayText.copy(alpha = 0.55f),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            clipboardManager.getText()?.let { clipText ->
                                viewModel.onPaste(clipText.text)
                            }
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste Expression",
                            tint = themeColors.displayText.copy(alpha = 0.55f),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Check & Correct Step Undo (1 Digit / Step Back)
                    IconButton(
                        onClick = { viewModel.undoStep() },
                        enabled = viewModel.canUndoCalc,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Step Undo (Check Left)",
                            tint = if (viewModel.canUndoCalc) themeColors.displayText.copy(alpha = 0.75f) else themeColors.displayText.copy(alpha = 0.2f),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Check & Correct Step Redo (1 Digit / Step Forward)
                    IconButton(
                        onClick = { viewModel.redoStep() },
                        enabled = viewModel.canRedoCalc,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Step Redo (Correct Right)",
                            tint = if (viewModel.canRedoCalc) themeColors.displayText.copy(alpha = 0.75f) else themeColors.displayText.copy(alpha = 0.2f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                val canSave = viewModel.expression.isNotBlank() && viewModel.expression != "0"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            showCalculatorHistoryOverlay = !showCalculatorHistoryOverlay
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Calculator History",
                            tint = if (showCalculatorHistoryOverlay) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.5f),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (canSave) {
                                viewModel.showSaveDialog = true
                            }
                        },
                        enabled = canSave,
                        modifier = Modifier
                            .size(34.dp)
                            .offset(x = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save Calculation",
                            tint = if (canSave) themeColors.displayText.copy(alpha = 0.5f) else themeColors.displayText.copy(alpha = 0.2f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                val focusRequester = remember { FocusRequester() }

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
                
                // Animated sizes and colors for Google Calculator transition
                val exprSize by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (viewModel.isEvaluated) {
                        24f
                    } else {
                        val baseSize = if (viewModel.expression.length > 12) 28f else 38f
                        if (viewModel.isScientificExpanded) 28f else baseSize
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

                var exprViewportWidth by remember { mutableStateOf(0) }
                var resultViewportWidth by remember { mutableStateOf(0) }
                var exprLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
                var resultLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

                val exprLeftHiddenCount = remember(exprScrollState.value, exprScrollState.maxValue, viewModel.expression, exprViewportWidth, exprLayoutResult) {
                    val text = viewModel.expression
                    val textLength = text.length
                    if (textLength == 0 || exprScrollState.maxValue <= 0) 0
                    else {
                        val totalWidth = exprLayoutResult?.size?.width?.toFloat() ?: 0f
                        if (totalWidth > 0f) {
                            val charWidth = totalWidth / textLength
                            val count = ((exprScrollState.value - 4f) / charWidth).toInt()
                            maxOf(0, count)
                        } else {
                            0
                        }
                    }
                }

                val exprRightHiddenCount = remember(exprScrollState.value, exprScrollState.maxValue, viewModel.expression, exprViewportWidth, exprLayoutResult) {
                    val text = viewModel.expression
                    val textLength = text.length
                    if (textLength == 0 || exprScrollState.maxValue <= 0) 0
                    else {
                        val totalWidth = exprLayoutResult?.size?.width?.toFloat() ?: 0f
                        if (totalWidth > 0f) {
                            val charWidth = totalWidth / textLength
                            val remainingWidth = exprScrollState.maxValue - exprScrollState.value
                            val count = ((remainingWidth - 4f) / charWidth).toInt()
                            maxOf(0, count)
                        } else {
                            0
                        }
                    }
                }

                val resultLeftHiddenCount = remember(resultScrollState.value, resultScrollState.maxValue, viewModel.result, resultViewportWidth, resultLayoutResult) {
                    val text = viewModel.result
                    val textLength = text.length
                    if (textLength == 0 || resultScrollState.maxValue <= 0) 0
                    else {
                        val totalWidth = resultLayoutResult?.size?.width?.toFloat() ?: 0f
                        if (totalWidth > 0f) {
                            val charWidth = totalWidth / textLength
                            val count = ((resultScrollState.value - 4f) / charWidth).toInt()
                            maxOf(0, count)
                        } else {
                            0
                        }
                    }
                }

                val resultRightHiddenCount = remember(resultScrollState.value, resultScrollState.maxValue, viewModel.result, resultViewportWidth, resultLayoutResult) {
                    val text = viewModel.result
                    val textLength = text.length
                    if (textLength == 0 || resultScrollState.maxValue <= 0) 0
                    else {
                        val totalWidth = resultLayoutResult?.size?.width?.toFloat() ?: 0f
                        if (totalWidth > 0f) {
                            val charWidth = totalWidth / textLength
                            val remainingWidth = resultScrollState.maxValue - resultScrollState.value
                            val count = ((remainingWidth - 4f) / charWidth).toInt()
                            maxOf(0, count)
                        } else {
                            0
                        }
                    }
                }

                LaunchedEffect(viewModel.expressionValue.text, viewModel.expressionValue.selection, exprScrollState.maxValue) {
                    if (exprScrollState.maxValue > 0) {
                        exprScrollState.scrollTo(exprScrollState.maxValue)
                    }
                }

                LaunchedEffect(viewModel.result) {
                    resultScrollState.scrollTo(0)
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
                        ),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    // Hidden text for measurement
                    val styles = parseDisplayStyles(viewModel.expression)
                    val annotatedExpr = buildAnnotatedString {
                        viewModel.expression.forEachIndexed { i, c ->
                            val style = styles.getOrNull(i)
                            if (style == DisplayStyle.SUPERSCRIPT) {
                                withStyle(SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = (exprSize * 0.58f).sp)) {
                                    append(c)
                                }
                            } else {
                                append(c)
                            }
                        }
                    }

                    Text(
                        text = annotatedExpr,
                        fontSize = exprSize.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        ),
                        modifier = Modifier
                            .layout { measurable, constraints ->
                                val placeable = measurable.measure(constraints.copy(maxWidth = androidx.compose.ui.unit.Constraints.Infinity))
                                layout(placeable.width, placeable.height) {
                                    placeable.placeRelative(0, 0)
                                }
                            }
                            .graphicsLayer(alpha = 0f),
                        onTextLayout = { exprLayoutResult = it },
                        maxLines = 1,
                        softWrap = false
                    )

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
                                    .padding(
                                        start = (7.5f - (1.5f * expansionFraction)).dp,
                                        end = (7.5f - (1.5f * expansionFraction)).dp
                                    )
                                    .horizontalScroll(exprScrollState)
                                    .onGloballyPositioned { exprViewportWidth = it.size.width },
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
                                    
                                    val styles = parseDisplayStyles(text)
                                    val isCurrentCursorSuperscript = if (selectionStart > 0) {
                                        val prevStyle = styles.getOrNull(selectionStart - 1)
                                        prevStyle == DisplayStyle.SUPERSCRIPT || (selectionStart < styles.size && styles[selectionStart] == DisplayStyle.SUPERSCRIPT)
                                    } else {
                                        false
                                    }
                                    
                                    // Handle cursor at the very beginning
                                    Box(
                                        modifier = Modifier
                                            .width(if (selectionStart == 0) 2.dp else 0.dp)
                                            .height((exprSize * 1.1f).dp)
                                            .background(if (selectionStart == 0 && cursorVisible) themeColors.displayText else Color.Transparent)
                                    )

                                    (0 until maxLen).forEach { index ->
                                        val char = text.getOrNull(index)
                                        val style = styles.getOrNull(index) ?: DisplayStyle.NORMAL
                                        
                                        if (char != null && style != DisplayStyle.HIDDEN) {
                                            val fontSizeFraction = if (style == DisplayStyle.SUPERSCRIPT) 0.58f else 1f
                                            val offsetVal = if (style == DisplayStyle.SUPERSCRIPT) (- (exprSize * 0.45f)).dp else 0.dp
                                            
                                            Text(
                                                text = char.toString(),
                                                color = exprColor,
                                                fontSize = (exprSize * fontSizeFraction).sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Medium,
                                                textAlign = TextAlign.End,
                                                style = TextStyle(
                                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                                ),
                                                modifier = Modifier
                                                    .offset(y = offsetVal)
                                            )
                                        }

                                        // Cursor after this character
                                        val isCursorAtThisPos = (index + 1 == selectionStart)
                                        val cursorHeight = if (isCursorAtThisPos && isCurrentCursorSuperscript) (exprSize * 0.7f).dp else (exprSize * 1.1f).dp
                                        val cursorOffset = if (isCursorAtThisPos && isCurrentCursorSuperscript) (- (exprSize * 0.35f)).dp else 0.dp
                                        
                                        Box(
                                            modifier = Modifier
                                                .width(if (isCursorAtThisPos) 2.dp else 0.dp)
                                                .height(cursorHeight)
                                                .offset(y = cursorOffset)
                                                .background(if (isCursorAtThisPos && cursorVisible) themeColors.displayText else Color.Transparent)
                                        )
                                    }
                                }
                                // Hidden but present for focus/selection
                                innerTextField()
                            }
                        }
                    )

                    // Expression Badge (Left)
                    if (exprLeftHiddenCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .offset(x = (-10).dp)
                        ) {
                            PillBadge(
                                count = exprLeftHiddenCount,
                                themeColors = themeColors,
                                isLeft = true,
                                fontSize = (exprSize * 0.32f).coerceIn(8f, 10f).sp
                            )
                        }
                    }

                    // Expression Badge (Right)
                    if (exprRightHiddenCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .offset(x = 10.dp)
                        ) {
                            PillBadge(
                                count = exprRightHiddenCount,
                                themeColors = themeColors,
                                isLeft = false,
                                fontSize = (exprSize * 0.32f).coerceIn(8f, 10f).sp
                            )
                        }
                    }

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
                        .onGloballyPositioned { /* Measured on inner scrollable box */ },
                    contentAlignment = Alignment.CenterEnd
                ) {
                    // Hidden result text for measurement
                    Text(
                        text = viewModel.result,
                        fontSize = resultSize.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        ),
                        modifier = Modifier
                            .layout { measurable, constraints ->
                                val placeable = measurable.measure(constraints.copy(maxWidth = androidx.compose.ui.unit.Constraints.Infinity))
                                layout(placeable.width, placeable.height) {
                                    placeable.placeRelative(0, 0)
                                }
                            }
                            .graphicsLayer(alpha = 0f),
                        onTextLayout = { resultLayoutResult = it },
                        maxLines = 1,
                        softWrap = false
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = (7.5f - (1.5f * expansionFraction)).dp,
                                end = (7.5f - (1.5f * expansionFraction)).dp
                            )
                            .horizontalScroll(resultScrollState)
                            .onGloballyPositioned { resultViewportWidth = it.size.width },
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
                            onTextLayout = { resultLayoutResult = it },
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            ),
                            modifier = Modifier
                                .clickable {
                                    if (viewModel.result.isNotEmpty()) {
                                        clipboardManager.setText(AnnotatedString(viewModel.result))
                                    }
                                }
                                .testTag("result_display")
                        )
                    }

                    // Result Badge (Left)
                    if (resultLeftHiddenCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .offset(x = (-10).dp)
                        ) {
                            PillBadge(
                                count = resultLeftHiddenCount,
                                themeColors = themeColors,
                                isLeft = true,
                                fontSize = (resultSize * 0.32f).coerceIn(8f, 10f).sp
                            )
                        }
                    }

                    // Result Badge (Right)
                    if (resultRightHiddenCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .offset(x = 10.dp)
                        ) {
                            PillBadge(
                                count = resultRightHiddenCount,
                                themeColors = themeColors,
                                isLeft = false,
                                fontSize = (resultSize * 0.32f).coerceIn(8f, 10f).sp
                            )
                        }
                    }
                }
            }

            // Calculator History Dialog popup
            if (showCalculatorHistoryOverlay) {
                val historyEntries by viewModel.historyList.collectAsStateWithLifecycle()
                val calcHistory = remember(historyEntries) {
                    historyEntries.filter { 
                        it.type == "Calculator" || it.type == "Basic" || it.type.isBlank() || it.type == "Scientific"
                    }
                }

                AlertDialog(
                    onDismissRequest = { showCalculatorHistoryOverlay = false },
                    containerColor = themeColors.cardBg,
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = themeColors.buttonEqualBg,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isBn) "ক্যালকুলেটর হিস্টোরি" else "Calculator History",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = themeColors.displayText
                                )
                            }
                            IconButton(
                                onClick = { showCalculatorHistoryOverlay = false },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = themeColors.displayText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    text = {
                        if (calcHistory.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isBn) "কোন ক্যালকুলেটর ইতিহাস পাওয়া যায়নি" else "No calculator history yet",
                                    fontSize = 14.sp,
                                    color = themeColors.displayText.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 200.dp, max = 450.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    calcHistory.forEach { entry ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.selectHistoryItem(entry)
                                                    showCalculatorHistoryOverlay = false
                                                },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = themeColors.background.copy(alpha = 0.8f)
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = entry.expression,
                                                        fontSize = 15.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        color = themeColors.displayExpressionText
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "= ${entry.result}",
                                                        fontSize = 17.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.Bold,
                                                        color = themeColors.displayText
                                                    )
                                                    if (!entry.customName.isNullOrBlank()) {
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = entry.customName,
                                                            fontSize = 12.sp,
                                                            color = themeColors.buttonEqualBg,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                                Icon(
                                                    imageVector = Icons.Default.ArrowForward,
                                                    contentDescription = "Load",
                                                    tint = themeColors.buttonEqualBg,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    shape = RoundedCornerShape(24.dp)
                )
            }

            // AC Undo Confirmation Dialog
            if (viewModel.showUndoConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissUndoDialog() },
                    containerColor = themeColors.cardBg,
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = null,
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    title = {
                        Text(
                            text = if (isBn) "হিসাব পূর্বাবস্থায় আনবেন?" else "Undo & Restore Calculation?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = themeColors.displayText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (isBn)
                                    "বর্তমান হিসেব প্রতিস্থাপন করে পূর্বের অল ক্লিয়ার (AC) করা রাশিমালা ফিরিয়ে আনতে চান?"
                                else
                                    "Do you want to replace the current expression and restore the previous cleared calculation?",
                                fontSize = 14.sp,
                                color = themeColors.displayText.copy(alpha = 0.85f)
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = themeColors.background.copy(alpha = 0.7f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (isBn) "বর্তমান: ${viewModel.expression}" else "Current: ${viewModel.expression}",
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = themeColors.displayExpressionText
                                    )
                                    viewModel.lastClearedExpressionValue?.let { lastExp ->
                                        Text(
                                            text = if (isBn) "পুনরুদ্ধার হবে: ${lastExp.text}" else "Will Restore: ${lastExp.text}",
                                            fontSize = 13.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.buttonEqualBg
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.confirmUndo() },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
                        ) {
                            Text(
                                text = if (isBn) "হ্যাঁ, আনডু করুন" else "Yes, Restore",
                                color = themeColors.buttonEqualText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { viewModel.dismissUndoDialog() }
                        ) {
                            Text(
                                text = if (isBn) "বাতিল" else "Cancel",
                                color = themeColors.displayText
                            )
                        }
                    },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        val isExpanded = viewModel.isScientificExpanded || expansionFraction > 0.5f

        val dragModifier = Modifier.pointerInput(Unit) {
            detectVerticalDragGestures(
                onDragStart = {
                    coroutineScope.launch { expansionAnimatable.stop() }
                },
                onDragEnd = {
                    coroutineScope.launch {
                        if (expansionAnimatable.value > 0.4f) {
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
                        if (expansionAnimatable.value > 0.4f) {
                            expansionAnimatable.animateTo(1f)
                            viewModel.isScientificExpanded = true
                        } else {
                            expansionAnimatable.animateTo(0f)
                            viewModel.isScientificExpanded = false
                        }
                    }
                },
                onVerticalDrag = { change, dragAmount ->
                    change.consume()
                    val delta = dragAmount / maxDragPx
                    coroutineScope.launch {
                        val target = (expansionAnimatable.value + delta).coerceIn(0f, 1f)
                        expansionAnimatable.snapTo(target)
                    }
                }
            )
        }

        // Drag Handle / Visual cue for swiping
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp) // Reduced height for minimal padding
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
                modifier = Modifier.size(22.dp)
            )
        }

        // Keypad Container (Dynamically scaled, no scroll needed)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(keypadWeight),
            verticalArrangement = Arrangement.spacedBy(rowSpacing)
        ) {
            if (expansionFraction > 0.01f) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(4f * expansionFraction)
                        .graphicsLayer {
                            alpha = expansionFraction
                            clip = true
                        }
                        .layout { measurable, constraints ->
                            if (expansionFraction <= 0.01f) {
                                val p = measurable.measure(constraints)
                                layout(constraints.maxWidth, constraints.maxHeight) { p.placeRelative(0, 0) }
                            } else {
                                val targetHeight = (constraints.maxHeight / expansionFraction).toInt()
                                val unconstrained = constraints.copy(minHeight = targetHeight, maxHeight = targetHeight)
                                val p = measurable.measure(unconstrained)
                                layout(constraints.maxWidth, constraints.maxHeight) {
                                    val yOffset = constraints.maxHeight - p.height
                                    p.placeRelative(0, yOffset)
                                }
                            }
                        },
                    verticalArrangement = Arrangement.spacedBy(rowSpacing)
                ) {
                    // Row 1: Trigonometric, Antilog, Cube
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .then(dragModifier),
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
                            .weight(1f)
                            .then(dragModifier),
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
                        CalculatorButton("^", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("x^y") }, Modifier.weight(1f), fontSize = scientificFontSize, padding = buttonPadding)
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
                        .weight(1f)
                        .then(dragModifier),
                    horizontalArrangement = Arrangement.spacedBy(rowSpacing)
                ) {
                    CalculatorButton(
                        text = "AC",
                        bgColor = themeColors.buttonFunctionBg,
                        textColor = themeColors.buttonFunctionText,
                        onClick = { viewModel.onBtnClick("AC") },
                        onLongClick = { viewModel.onAcLongClick() },
                        modifier = Modifier.weight(1f),
                        fontSize = basicFontSize,
                        padding = buttonPadding,
                        icon = {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = "AC",
                                    color = themeColors.buttonFunctionText,
                                    fontSize = basicFontSize.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Undo,
                                    contentDescription = "Undo AC",
                                    tint = themeColors.buttonFunctionText.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = if (isExpanded) 4.dp else 6.dp, end = if (isExpanded) 5.dp else 7.dp)
                                        .size(if (isExpanded) 11.dp else 13.dp)
                                )
                            }
                        }
                    )
                    CalculatorButton("()", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("()") }, Modifier.weight(1f), fontSize = basicFontSize, padding = buttonPadding)
                    CalculatorButton("%", themeColors.buttonFunctionBg, themeColors.buttonFunctionText, { viewModel.onBtnClick("%") }, Modifier.weight(1f), fontSize = basicFontSize, padding = buttonPadding)
                    CalculatorButton("÷", themeColors.buttonOperatorBg, themeColors.buttonOperatorText, { viewModel.onBtnClick("÷") }, Modifier.weight(1f), fontSize = basicFontSize, padding = buttonPadding)
                }

                // Row 2: 7, 8, 9, ×
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .then(dragModifier),
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
                        repeatsOnLongPress = true,
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
}

private enum class DisplayStyle {
    NORMAL,
    SUPERSCRIPT,
    HIDDEN
}

private fun parseDisplayStyles(text: String): List<DisplayStyle> {
    val styles = MutableList(text.length) { DisplayStyle.NORMAL }
    var i = 0
    while (i < text.length) {
        if (text[i] == '^') {
            styles[i] = DisplayStyle.SUPERSCRIPT
            i++
            if (i < text.length) {
                if (text[i] == '(') {
                    styles[i] = DisplayStyle.SUPERSCRIPT
                    var parenCount = 1
                    i++
                    while (i < text.length && parenCount > 0) {
                        if (text[i] == '(') parenCount++
                        if (text[i] == ')') parenCount--
                        styles[i] = DisplayStyle.SUPERSCRIPT
                        i++
                    }
                    continue
                } else {
                    var first = true
                    while (i < text.length) {
                        val c = text[i]
                        val isExpChar = c.isLetterOrDigit() || c == '.' || c == 'π' || (first && (c == '-' || c == '−'))
                        if (isExpChar) {
                            styles[i] = DisplayStyle.SUPERSCRIPT
                            first = false
                            i++
                        } else {
                            break
                        }
                    }
                    continue
                }
            }
        } else {
            i++
        }
    }
    return styles
}
