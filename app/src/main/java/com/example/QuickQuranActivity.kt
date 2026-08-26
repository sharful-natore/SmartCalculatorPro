package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.CalculatorDatabase
import com.example.data.repository.HistoryRepository
import com.example.data.repository.ToolUsageRepository
import com.example.ui.quran.HolyQuranModuleScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.ui.viewmodel.CalculatorViewModelFactory
import com.example.util.AppLanguage
import com.example.util.bounceOverscroll
import android.graphics.Color as AndroidColor

class QuickQuranActivity : ComponentActivity() {

    private lateinit var viewModel: CalculatorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT
        WindowCompat.getInsetsController(window, window.decorView)?.apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        val database = CalculatorDatabase.getDatabase(this)
        val repository = HistoryRepository(database.historyDao())
        val usageRepository = ToolUsageRepository(database.toolUsageDao())
        val viewModelFactory = CalculatorViewModelFactory(repository, usageRepository, this)
        viewModel = ViewModelProvider(this, viewModelFactory)[CalculatorViewModel::class.java]

        // Record usage for Holy Quran
        viewModel.recordToolUsage("TOOL_HOLY_QURAN")

        setContent {
            MyApplicationTheme {
                val themeColors = viewModel.getCurrentThemeColors()
                val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
                var isMaximized by remember { mutableStateOf(false) }
                var showCloseConfirmDialog by remember { mutableStateOf(false) }

                val dialogShape = if (isMaximized) RoundedCornerShape(0.dp) else RoundedCornerShape(24.dp)

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Dimmed backdrop
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.55f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { showCloseConfirmDialog = true }
                            )
                    )

                    Surface(
                        modifier = (if (isMaximized) {
                            Modifier.fillMaxSize()
                        } else {
                            Modifier
                                .fillMaxWidth(0.96f)
                                .fillMaxHeight(0.94f)
                        }).clip(dialogShape),
                        shape = dialogShape,
                        color = themeColors.background,
                        tonalElevation = 8.dp,
                        shadowElevation = 16.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(dialogShape)
                                .clipToBounds()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(themeColors.buttonEqualBg.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MenuBook,
                                            contentDescription = null,
                                            tint = themeColors.buttonEqualBg,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Text(
                                        text = if (isBn) "আল কুরআনুল কারীম" else "Al-Quran",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.displayText
                                    )
                                }

                                com.example.ui.Windows11TitlebarButtons(
                                    isMaximized = isMaximized,
                                    onMinimize = { moveTaskToBack(true) },
                                    onMaximizeToggle = { isMaximized = !isMaximized },
                                    onClose = { showCloseConfirmDialog = true },
                                    themeColors = themeColors
                                )
                            }

                            HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.1f))

                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(bottom = 6.dp)
                                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                                    .clipToBounds()
                                    .bounceOverscroll()
                            ) {
                                HolyQuranModuleScreen(
                                    themeColors = themeColors,
                                    onBackClick = { showCloseConfirmDialog = true },
                                    isBn = isBn
                                )
                            }
                        }
                    }
                }

                if (showCloseConfirmDialog) {
                    AlertDialog(
                        onDismissRequest = { showCloseConfirmDialog = false },
                        title = {
                            Text(
                                text = if (isBn) "আল কুরআন বন্ধ করুন" else "Close Al-Quran",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = themeColors.displayText
                            )
                        },
                        text = {
                            Text(
                                text = if (isBn) "আপনি কি আল কুরআন বন্ধ করতে চান?" else "Do you want to close Al-Quran?",
                                fontSize = 14.sp,
                                color = themeColors.displayText.copy(alpha = 0.85f)
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showCloseConfirmDialog = false
                                    finish()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                            ) {
                                Text(text = if (isBn) "হ্যাঁ" else "Yes", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showCloseConfirmDialog = false }) {
                                Text(text = if (isBn) "না" else "No", color = themeColors.displayText)
                            }
                        },
                        containerColor = themeColors.cardBg,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }
    }
}
