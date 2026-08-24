package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CalculatorThemeColors
import com.example.util.CrashReport
import com.example.util.CrashReporter

@Composable
fun CrashReportDialog(
    report: CrashReport,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit,
    onSendReport: (CrashReport, String) -> Unit = { rep, note -> }
) {
    val context = LocalContext.current
    var showFullStacktrace by remember { mutableStateOf(false) }
    var userFeedbackNote by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
            border = BorderStroke(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEF4444).copy(alpha = 0.15f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.BugReport,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (isBn) "ত্রুটি রিপোর্ট ও ডায়াগনস্টিকস" else "Crash Report & Feedback",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                            Text(
                                text = if (isBn) "পূর্ববর্তী সেশনে একটি ত্রুটি ধরা পড়েছে" else "An unexpected error occurred previously",
                                fontSize = 12.sp,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = themeColors.displayText.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Friendly message box
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isBn)
                                    "অ্যাপটি কোনো কারণে অপ্রত্যাশিতভাবে বন্ধ হয়ে গিয়েছিল। আমরা দুঃখিত! এই ত্রুটি দ্রুত সমাধানের জন্য ডেভেলপারের কাছে সরাসরি রিপোর্ট পাঠানোর অনুরোধ করা হচ্ছে।"
                                else
                                    "The app unexpectedly closed or encountered an error. Please send this report to help us fix the issue immediately.",
                                fontSize = 12.sp,
                                color = themeColors.displayText.copy(alpha = 0.85f),
                                lineHeight = 17.sp
                            )
                        }
                    }

                    // Metadata Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = themeColors.background,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isBn) "ত্রুটির প্রাথমিক তথ্য:" else "Crash Summary:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.buttonOperatorBg
                            )

                            Text(
                                text = "• ${if (isBn) "সময়:" else "Time:"} ${report.formattedTime}",
                                fontSize = 11.sp,
                                color = themeColors.displayText.copy(alpha = 0.7f)
                            )
                            if (report.screenContext.isNotBlank()) {
                                Text(
                                    text = "• ${if (isBn) "সক্রিয় স্ক্রিন / টুল:" else "Screen:"} ${report.screenContext}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = themeColors.displayText
                                )
                            }
                            Text(
                                text = "• ${if (isBn) "ডিভাইস:" else "Device:"} ${report.deviceManufacturer} ${report.deviceModel} (Android ${report.androidVersion})",
                                fontSize = 11.sp,
                                color = themeColors.displayText.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "• ${if (isBn) "এরর টাইপ:" else "Exception:"} ${report.exceptionType}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFEF4444)
                            )
                            Text(
                                text = "• ${if (isBn) "বার্তা:" else "Message:"} ${report.errorMessage}",
                                fontSize = 11.sp,
                                color = themeColors.displayText
                            )
                        }
                    }

                    // User Feedback Input
                    OutlinedTextField(
                        value = userFeedbackNote,
                        onValueChange = { userFeedbackNote = it },
                        label = {
                            Text(
                                text = if (isBn) "আপনার মন্তব্য (ঐচ্ছিক - কী করার সময় এরর হয়েছিল)" else "Your Note (Optional - what you were doing)",
                                fontSize = 12.sp
                            )
                        },
                        placeholder = {
                            Text(
                                text = if (isBn) "যেমন: ক্যামেরা লেভেলারে ক্লিক করার সাথে সাথে বন্ধ হয়েছে..." else "e.g. Occurred right when opening Camera Leveler...",
                                fontSize = 11.sp
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.buttonOperatorBg,
                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f)
                        )
                    )

                    // Stacktrace Toggle Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showFullStacktrace = !showFullStacktrace },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = if (showFullStacktrace) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showFullStacktrace)
                                    (if (isBn) "স্ট্যাকট্রেস সংকুচিত করুন" else "Hide Stacktrace")
                                else
                                    (if (isBn) "সম্পূর্ণ টেকনিক্যাল লগ দেখুন" else "View Technical Stacktrace"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        IconButton(
                            onClick = {
                                CrashReporter.copyToClipboard(context, report.toFormattedReport())
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Log",
                                tint = themeColors.buttonOperatorBg,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Stacktrace Box
                    AnimatedVisibility(visible = showFullStacktrace) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0F172A),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                        ) {
                            SelectionContainer {
                                Column(
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .verticalScroll(rememberScrollState())
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    Text(
                                        text = report.stackTrace,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = Color(0xFF38BDF8),
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Send to Developer Button
                    Button(
                        onClick = {
                            CrashReporter.sendEmailReport(context, report, userFeedbackNote)
                            CrashReporter.clearPendingCrashReport(context)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColors.buttonEqualBg,
                            contentColor = themeColors.buttonEqualText
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = themeColors.buttonEqualText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "ডেভেলপারকে রিপোর্ট পাঠান" else "Send Report to Developer",
                            fontWeight = FontWeight.Bold,
                            color = themeColors.buttonEqualText,
                            fontSize = 14.sp
                        )
                    }

                    // Secondary Actions (Copy Log + Dismiss)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                CrashReporter.copyToClipboard(context, report.toFormattedReport())
                            },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, themeColors.buttonOperatorBg.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = themeColors.buttonOperatorBg.copy(alpha = 0.08f),
                                contentColor = themeColors.buttonOperatorBg
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = themeColors.buttonOperatorBg,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBn) "লগ কপি" else "Copy Log",
                                color = themeColors.buttonOperatorBg,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }

                        FilledTonalButton(
                            onClick = {
                                CrashReporter.clearPendingCrashReport(context)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = themeColors.buttonFunctionBg.copy(alpha = 0.35f),
                                contentColor = themeColors.displayText.copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isBn) "মুছে ফেলুন" else "Dismiss",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = themeColors.displayText.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}
