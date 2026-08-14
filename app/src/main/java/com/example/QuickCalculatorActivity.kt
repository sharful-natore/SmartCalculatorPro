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
        val viewModelFactory = CalculatorViewModelFactory(repository, this)
        viewModel = ViewModelProvider(this, viewModelFactory)[CalculatorViewModel::class.java]

        setContent {
            MyApplicationTheme {
                val themeColors = viewModel.getCurrentThemeColors()
                val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

                // Full screen scrim overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { finish() }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Floating Calculator Dialog Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.96f)
                            .fillMaxHeight(0.90f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { /* prevent dismiss on clicking dialog */ }
                            ),
                        shape = RoundedCornerShape(28.dp),
                        color = themeColors.background,
                        tonalElevation = 8.dp,
                        shadowElevation = 16.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            // Header with Title, Maximize, and Close buttons
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

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Maximize button (opens full app in calculator tab)
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(themeColors.displayText.copy(alpha = 0.08f), CircleShape)
                                            .clip(CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        IconButton(
                                            onClick = {
                                                val intent = Intent(this@QuickCalculatorActivity, MainActivity::class.java).apply {
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                                    putExtra("target_tab", "calculator")
                                                }
                                                startActivity(intent)
                                                finish()
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.OpenInFull,
                                                contentDescription = "Maximize",
                                                tint = themeColors.displayText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    // Close button
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(themeColors.displayText.copy(alpha = 0.08f), CircleShape)
                                            .clip(CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        IconButton(
                                            onClick = { finish() },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Close",
                                                tint = themeColors.displayText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
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
            }
        }
    }
}
