package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.LanguageManager
import kotlinx.coroutines.launch

@Composable
fun TextCounterCard(viewModel: CalculatorViewModel, themeColors: CalculatorThemeColors) {
    var textInput by remember { mutableStateOf("") }
    
    val wordCount = if (textInput.isBlank()) 0 else textInput.trim().split("\\s+".toRegex()).size
    val charCount = textInput.length
    val charNoSpaceCount = textInput.replace("\\s+".toRegex(), "").length
    val lineCount = if (textInput.isEmpty()) 0 else textInput.split("\n").size
    
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI) "শব্দ ও অক্ষর গণনা" else "Word & Text Counter",
                color = themeColors.displayText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                label = { Text(if (viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI) "এখানে আপনার লেখা পেস্ট করুন..." else "Type or paste your text here...", color = themeColors.displayText.copy(alpha = 0.7f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themeColors.buttonEqualBg,
                    unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.3f),
                    focusedTextColor = themeColors.displayText,
                    unfocusedTextColor = themeColors.displayText,
                    cursorColor = themeColors.buttonEqualBg
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { textInput = "" }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = themeColors.displayText.copy(alpha = 0.7f))
                }
                IconButton(onClick = {
                    if (textInput.isNotEmpty()) {
                        clipboardManager.setText(AnnotatedString(textInput))
                        coroutineScope.launch { snackbarHostState.showSnackbar("Copied to clipboard") }
                    }
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = themeColors.displayText.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Results grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBox(
                    title = if (viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI) "শব্দ" else "Words",
                    value = wordCount.toString(),
                    themeColors = themeColors,
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    title = if (viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI) "অক্ষর" else "Characters",
                    value = charCount.toString(),
                    themeColors = themeColors,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBox(
                    title = if (viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI) "অক্ষর (স্পেস ছাড়া)" else "Chars (No Spaces)",
                    value = charNoSpaceCount.toString(),
                    themeColors = themeColors,
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    title = if (viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI) "লাইন" else "Lines",
                    value = lineCount.toString(),
                    themeColors = themeColors,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PasswordGeneratorCard(viewModel: CalculatorViewModel, themeColors: CalculatorThemeColors) {
    var length by remember { mutableStateOf(12f) }
    var useUppercase by remember { mutableStateOf(true) }
    var useLowercase by remember { mutableStateOf(true) }
    var useNumbers by remember { mutableStateOf(true) }
    var useSymbols by remember { mutableStateOf(true) }
    
    var generatedPassword by remember { mutableStateOf("") }
    
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val generatePass = {
        val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lower = "abcdefghijklmnopqrstuvwxyz"
        val numbers = "0123456789"
        val symbols = "!@#\$%^&*()-_=+[]{}|;:,.<>?"
        
        var chars = ""
        if (useUppercase) chars += upper
        if (useLowercase) chars += lower
        if (useNumbers) chars += numbers
        if (useSymbols) chars += symbols
        
        if (chars.isEmpty()) {
            chars = lower
            useLowercase = true
        }
        
        var pass = ""
        for (i in 0 until length.toInt()) {
            pass += chars.random()
        }
        generatedPassword = pass
    }
    
    // Auto-generate on first launch or config change
    LaunchedEffect(length, useUppercase, useLowercase, useNumbers, useSymbols) {
        generatePass()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI) "পাসওয়ার্ড জেনারেটর" else "Password Generator",
                color = themeColors.displayText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Password Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(themeColors.buttonEqualBg.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = generatedPassword,
                        color = themeColors.displayText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(generatedPassword))
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = themeColors.buttonEqualBg)
                    }
                    IconButton(onClick = { generatePass() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = themeColors.buttonEqualBg)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Length Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI) "দৈর্ঘ্য: ${length.toInt()}" else "Length: ${length.toInt()}",
                    color = themeColors.displayText,
                    fontWeight = FontWeight.Medium
                )
            }
            Slider(
                value = length,
                onValueChange = { length = it },
                valueRange = 4f..32f,
                steps = 27,
                colors = SliderDefaults.colors(
                    thumbColor = themeColors.buttonEqualBg,
                    activeTrackColor = themeColors.buttonEqualBg,
                    inactiveTrackColor = themeColors.buttonEqualBg.copy(alpha = 0.2f)
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Options
            PasswordOptionRow("A-Z", if (viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI) "বড় হাতের অক্ষর" else "Uppercase", useUppercase, { useUppercase = it }, themeColors)
            PasswordOptionRow("a-z", if (viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI) "ছোট হাতের অক্ষর" else "Lowercase", useLowercase, { useLowercase = it }, themeColors)
            PasswordOptionRow("0-9", if (viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI) "সংখ্যা" else "Numbers", useNumbers, { useNumbers = it }, themeColors)
            PasswordOptionRow("!@#", if (viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI) "সিম্বল" else "Symbols", useSymbols, { useSymbols = it }, themeColors)
        }
    }
}

@Composable
fun PasswordOptionRow(label: String, desc: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, themeColors: CalculatorThemeColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .background(themeColors.buttonEqualBg.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = label, color = themeColors.displayText, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = desc, color = themeColors.displayText)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = themeColors.buttonEqualBg,
                uncheckedThumbColor = themeColors.displayText.copy(alpha = 0.5f),
                uncheckedTrackColor = themeColors.buttonEqualBg.copy(alpha = 0.15f)
            )
        )
    }
}

@Composable
private fun StatBox(title: String, value: String, themeColors: CalculatorThemeColors, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(themeColors.buttonEqualBg.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, color = themeColors.displayText.copy(alpha = 0.7f), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = themeColors.displayText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}
