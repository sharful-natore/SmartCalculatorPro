package com.example.ui.screens.tools.kids

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CalculatorThemeColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun KidsMathTab(
    themeColors: CalculatorThemeColors,
    audioPlayer: KidsAudioPlayer,
    subTab: Int = 0,
    onSubTabChange: (Int) -> Unit = {},
    onRewardStars: (Int) -> Unit
) {
    var selectedNumberItem by remember { mutableStateOf<NumberItem?>(null) }
    var selectedTableNumber by remember { mutableStateOf(1) }
    val coroutineScope = rememberCoroutineScope()
    var isReadingWholeTable by remember { mutableStateOf(false) }
    var highlightedTableRowIndex by remember { mutableStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        if (subTab == 0) {
            // COUNTING (সংখ্যা ও গণনা ১-২০)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "সংখ্যা স্পর্শ করে গণনা করো 👇",
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "১ থেকে ২০",
                    style = MaterialTheme.typography.labelSmall,
                    color = themeColors.accent,
                    fontWeight = FontWeight.Bold
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp)
            ) {
                items(KidsDataProvider.numberItems) { item ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, item.accentColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .clickable {
                                audioPlayer.playClickSound()
                                audioPlayer.speak("${item.numberBn}। ${item.wordBn}। ${item.wordEn}", isBn = true)
                                selectedNumberItem = item
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .background(item.accentColor.copy(alpha = 0.15f), CircleShape)
                                        .border(2.dp, item.accentColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${item.numberBn}",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Black,
                                        color = item.accentColor
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = "${item.wordBn} (${item.wordEn})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.onSurface
                                    )
                                    Text(
                                        text = "English: ${item.numberEn}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = themeColors.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            // Interactive count visual icons
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = item.accentColor.copy(alpha = 0.10f),
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = item.countEmoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "× ${item.count}",
                                        fontWeight = FontWeight.Bold,
                                        color = item.accentColor,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // MULTIPLICATION TABLES (নামতা ১-১০)
            val currentTable = remember(selectedTableNumber) {
                KidsDataProvider.getMultiplicationTable(selectedTableNumber)
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Table Selector Row (1 to 10)
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val bnDigits = listOf("০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯")
                    items((1..10).toList()) { num ->
                        val isSelected = selectedTableNumber == num
                        val bnNum = if (num == 10) "১০" else bnDigits[num]
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) themeColors.accent else themeColors.surface,
                            border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, themeColors.onSurface.copy(alpha = 0.2f)) else null,
                            modifier = Modifier
                                .size(46.dp)
                                .clickable {
                                    audioPlayer.playClickSound()
                                    selectedTableNumber = num
                                    isReadingWholeTable = false
                                    highlightedTableRowIndex = -1
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = bnNum,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) themeColors.onAccent else themeColors.onSurface
                                )
                            }
                        }
                    }
                }

                // Table Title & Recite All Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${currentTable.numberBn}-এর ঘরের নামতা",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.accent
                    )

                    Button(
                        onClick = {
                            if (isReadingWholeTable) {
                                isReadingWholeTable = false
                                highlightedTableRowIndex = -1
                                audioPlayer.stop()
                            } else {
                                isReadingWholeTable = true
                                coroutineScope.launch {
                                    for (i in currentTable.items.indices) {
                                        if (!isReadingWholeTable) break
                                        highlightedTableRowIndex = i
                                        val item = currentTable.items[i]
                                        audioPlayer.speak(item.speechBn, isBn = true)
                                        delay(1600)
                                    }
                                    isReadingWholeTable = false
                                    highlightedTableRowIndex = -1
                                    onRewardStars(5)
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isReadingWholeTable) Color(0xFFD32F2F) else themeColors.accent,
                            contentColor = if (isReadingWholeTable) Color.White else themeColors.onAccent
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        val btnContentColor = if (isReadingWholeTable) Color.White else themeColors.onAccent
                        Icon(
                            imageVector = if (isReadingWholeTable) Icons.Default.Close else Icons.Default.VolumeUp,
                            contentDescription = "Read whole table",
                            tint = btnContentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isReadingWholeTable) "থামাও" else "পুরো নামতা শোনো",
                            style = MaterialTheme.typography.labelSmall,
                            color = btnContentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Multiplication Rows
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp)
                ) {
                    items(currentTable.items) { item ->
                        val isHighlighted = isReadingWholeTable && highlightedTableRowIndex == item.multiplier - 1

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isHighlighted) themeColors.accent else themeColors.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isHighlighted) 4.dp else 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isHighlighted) 2.dp else 1.dp,
                                    color = if (isHighlighted) themeColors.accent else themeColors.onSurface.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    audioPlayer.playClickSound()
                                    audioPlayer.speak(item.speechBn, isBn = true)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item.textBn,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isHighlighted) themeColors.onAccent else themeColors.onSurface
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.speechBn,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isHighlighted) themeColors.onAccent.copy(alpha = 0.9f) else themeColors.accent,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        Icons.Default.VolumeUp,
                                        contentDescription = "Speak row",
                                        tint = if (isHighlighted) themeColors.onAccent else themeColors.accent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Interactive Counting Board for Selected Number
    selectedNumberItem?.let { item ->
        InteractiveCountingDialog(
            item = item,
            themeColors = themeColors,
            audioPlayer = audioPlayer,
            onDismiss = { selectedNumberItem = null },
            onComplete = { onRewardStars(5) }
        )
    }
}

/**
 * Interactive Counting Dialog:
 * Shows the number of items and lets the child tap each item one-by-one to count!
 */
@Composable
fun InteractiveCountingDialog(
    item: NumberItem,
    themeColors: CalculatorThemeColors,
    audioPlayer: KidsAudioPlayer,
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    val tappedIndices = remember { mutableStateListOf<Int>() }
    val bnDigits = listOf("০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯")
    fun toBn(num: Int): String = num.toString().map { bnDigits[it - '0'] }.joinToString("")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = themeColors.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "গণনা শেখার বোর্ড 🎈",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = themeColors.onSurface.copy(alpha = 0.6f))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Big Number Badge
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(item.accentColor.copy(alpha = 0.15f), CircleShape)
                        .border(3.dp, item.accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.numberBn,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = item.accentColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${item.wordBn} (${item.wordEn})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "একটি একটি করে ছবিতে ট্যাপ করে গুনে দেখো:",
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Grid of Countable Items
                val columns = if (item.count > 10) 5 else 4
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val rows = (item.count + columns - 1) / columns
                    for (r in 0 until rows) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (c in 0 until columns) {
                                val idx = r * columns + c
                                if (idx < item.count) {
                                    val isTapped = tappedIndices.contains(idx)
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isTapped) item.accentColor.copy(alpha = 0.20f) else themeColors.surfaceVariant.copy(alpha = 0.4f),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.5.dp,
                                            if (isTapped) item.accentColor else themeColors.onSurface.copy(alpha = 0.2f)
                                        ),
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                if (!isTapped) {
                                                    tappedIndices.add(idx)
                                                    val currentCount = tappedIndices.size
                                                    audioPlayer.playSuccessChime()
                                                    audioPlayer.speak(toBn(currentCount), isBn = true)

                                                    if (tappedIndices.size == item.count) {
                                                        audioPlayer.playCelebrationSound()
                                                        audioPlayer.speak("মোট ${item.numberBn}টি! শাবাশ!", isBn = true)
                                                        onComplete()
                                                    }
                                                }
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = item.countEmoji,
                                                fontSize = if (isTapped) 24.sp else 20.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress status
                Text(
                    text = "গণনা হয়েছে: ${toBn(tappedIndices.size)} / ${item.numberBn}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = item.accentColor
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColors.accent,
                        contentColor = themeColors.onAccent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text(text = "বুঝেছি 👍", fontWeight = FontWeight.Bold, color = themeColors.onAccent)
                }
            }
        }
    }
}
