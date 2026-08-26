package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.CalculatorDatabase
import com.example.data.repository.HistoryRepository
import com.example.data.repository.ToolUsageRepository
import com.example.ui.screens.CalculatorScreen
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.ui.viewmodel.CalculatorViewModelFactory
import com.example.util.AppLanguage
import android.graphics.Color as AndroidColor

class QuickCalculatorActivity : ComponentActivity() {

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

        // Record usage for Main Calculator
        viewModel.recordToolUsage("MAIN_CALC")

        setContent {
            MyApplicationTheme {
                val themeColors = viewModel.getCurrentThemeColors()
                val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
                var isMaximized by remember { mutableStateOf(false) }
                var showExitConfirmDialog by remember { mutableStateOf(false) }

                // Full screen scrim overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { showExitConfirmDialog = true }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Floating Calculator Dialog Card
                    Surface(
                        modifier = if (isMaximized) {
                            Modifier.fillMaxSize()
                        } else {
                            Modifier
                                .fillMaxWidth(0.96f)
                                .fillMaxHeight(0.90f)
                        }.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { /* prevent dismiss on clicking dialog */ }
                        ),
                        shape = if (isMaximized) RoundedCornerShape(0.dp) else RoundedCornerShape(24.dp),
                        color = themeColors.background,
                        tonalElevation = 8.dp,
                        shadowElevation = 16.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            // Header with Title and Windows 11 style controls
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
                                            imageVector = Icons.Default.Calculate,
                                            contentDescription = null,
                                            tint = themeColors.buttonEqualBg,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Text(
                                        text = if (isBn) "কুইক ক্যালকুলেটর" else "Quick Calculator",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.displayText
                                    )
                                }

                                com.example.ui.Windows11TitlebarButtons(
                                    isMaximized = isMaximized,
                                    onMinimize = { moveTaskToBack(true) },
                                    onMaximizeToggle = { isMaximized = !isMaximized },
                                    onClose = { showExitConfirmDialog = true },
                                    themeColors = themeColors
                                )
                            }

                            HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.1f))

                            Spacer(modifier = Modifier.height(8.dp))

                            // Calculator Screen Content
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                CalculatorScreen(viewModel = viewModel, themeColors = themeColors)
                            }
                        }
                    }
                }

                // Close Confirmation Dialog
                if (showExitConfirmDialog) {
                    AlertDialog(
                        onDismissRequest = { showExitConfirmDialog = false },
                        title = {
                            Text(
                                text = if (isBn) "কুইক ক্যালকুলেটর বন্ধ করুন" else "Close Quick Calculator",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = themeColors.displayText
                            )
                        },
                        text = {
                            Text(
                                text = if (isBn)
                                    "আপনি কি কুইক ক্যালকুলেটর বন্ধ করতে চান?"
                                else
                                    "Do you want to close Quick Calculator?",
                                fontSize = 14.sp,
                                color = themeColors.displayText.copy(alpha = 0.85f)
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showExitConfirmDialog = false
                                    finish()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                            ) {
                                Text(
                                    text = if (isBn) "হ্যাঁ" else "Yes",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showExitConfirmDialog = false }
                            ) {
                                Text(
                                    text = if (isBn) "না" else "No",
                                    color = themeColors.displayText
                                )
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
