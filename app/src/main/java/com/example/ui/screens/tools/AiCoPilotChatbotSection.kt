package com.example.ui.screens.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.ui.theme.CalculatorThemeColors

@Composable
fun AiCoPilotChatbotSection(
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    coPilotMessages: List<CoPilotMessage>,
    userMessageText: String,
    onUserMessageTextChange: (String) -> Unit,
    isChatLoading: Boolean,
    onSendMessage: (String) -> Unit,
    onCvDataChange: (CvData) -> Unit,
    onCompareClick: (AiSuggestionItem) -> Unit
) {
    val context = LocalContext.current
    
    Spacer(modifier = Modifier.height(18.dp))
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = themeColors.cardBg,
        border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = themeColors.buttonEqualBg.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = null,
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = if (isBn) "এআই সিভি কো-পাইলট (AI CV Co-Pilot)" else "AI CV Co-Pilot Chat",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    Text(
                        text = if (isBn) "এআই-এর সাথে কথা বলে রিয়েল-টাইমে আপনার সিভি টিউন করুন" else "Chat with AI to refine your resume in real-time",
                        fontSize = 10.5.sp,
                        color = themeColors.displayText.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chat Messages Container
            if (coPilotMessages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(themeColors.background.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .border(1.dp, themeColors.displayText.copy(alpha = 0.06f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = themeColors.buttonEqualBg.copy(alpha = 0.4f),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (isBn) "সার্কুলার বিশ্লেষণ করুন অথবা নির্দেশ দিন, এআই চ্যাটবট স্বয়ংক্রিয়ভাবে চালু হবে!" else "Analyze the circular or give an instruction to start chatting with your AI Co-Pilot!",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = themeColors.displayText.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 8.dp)
                ) {
                    coPilotMessages.forEach { msg ->
                        val isAi = msg.sender == "ai"
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isAi) Alignment.Start else Alignment.End
                        ) {
                            Row(
                                horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                if (isAi) {
                                    Surface(
                                        shape = CircleShape,
                                        color = themeColors.buttonEqualBg,
                                        modifier = Modifier.size(24.dp).align(Alignment.Top)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                
                                Surface(
                                    shape = RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (isAi) 2.dp else 12.dp,
                                        bottomEnd = if (isAi) 12.dp else 2.dp
                                    ),
                                    color = if (isAi) themeColors.background else themeColors.buttonEqualBg,
                                    border = BorderStroke(1.dp, if (isAi) themeColors.displayText.copy(alpha = 0.08f) else Color.Transparent),
                                    modifier = Modifier.widthIn(max = 270.dp)
                                ) {
                                    Text(
                                        text = msg.text,
                                        fontSize = 11.5.sp,
                                        color = if (isAi) themeColors.displayText else Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            // If AI message contains proposed CvData, show an Action Card
                            if (isAi && msg.proposedCvData != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = themeColors.background.copy(alpha = 0.7f),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.25f)),
                                    modifier = Modifier
                                        .padding(start = 30.dp, end = 10.dp)
                                        .fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = if (isBn) "খসড়া সিভি আপডেট প্রস্তুত!" else "Draft CV Update Ready!",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF10B981)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (isBn) "এই কথোপকথনের খসড়াটি আপনার সিভিতে চূড়ান্ত করতে নিচের বাটনে চাপুন।" else "Apply this modified version directly to your master CV data.",
                                            fontSize = 9.5.sp,
                                            color = themeColors.displayText.copy(alpha = 0.6f)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Button(
                                                onClick = {
                                                    onCvDataChange(msg.proposedCvData)
                                                    Toast.makeText(context, if (isBn) "খসড়া সিভিতে সফলভাবে প্রয়োগ করা হয়েছে!" else "Draft applied to master CV!", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = if (isBn) "প্রয়োগ করুন" else "Apply", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                            
                                            OutlinedButton(
                                                onClick = {
                                                    onCompareClick(AiSuggestionItem(
                                                        id = msg.id,
                                                        titleEn = "Co-Pilot Proposed Update",
                                                        titleBn = "কো-পাইলট প্রস্তাবিত আপডেট",
                                                        descEn = "Compare the changes suggested by Co-Pilot in this chat turn.",
                                                        descBn = "চ্যাট কো-পাইলট দ্বারা প্রস্তাবিত পরিবর্তনগুলোর তুলনা দেখে নিশ্চিত করুন।",
                                                        category = "Summary",
                                                        proposedValue = msg.proposedCvData.summary
                                                    ))
                                                },
                                                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.3f)),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Compare, contentDescription = null, tint = themeColors.displayText, modifier = Modifier.size(11.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = if (isBn) "তুলনা করুন" else "Compare", color = themeColors.displayText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    if (isChatLoading) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = themeColors.buttonEqualBg
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn) "কো-পাইলট টাইপ করছে..." else "Co-Pilot is typing...",
                                fontSize = 11.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = themeColors.displayText.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Suggestion Quick Chips Row
            if (coPilotMessages.isNotEmpty()) {
                val chips = if (isBn) {
                    listOf("সিভি সামারি চমৎকার করো", "মিসিং কিওয়ার্ড সব যুক্ত করো", "অভিজ্ঞতা বুলেটস আরও ভালো করো")
                } else {
                    listOf("Polish summary nicely", "Add all missing keywords", "Improve experience bullets")
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    items(chips) { chip ->
                        SuggestionChip(
                            onClick = { onSendMessage(chip) },
                            label = { Text(text = chip, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.15f))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Chat Input Field Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = userMessageText,
                    onValueChange = onUserMessageTextChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(text = if (isBn) "কো-পাইলটকে বলুন কি পরিবর্তন করবে..." else "Tell Co-Pilot what to edit...", fontSize = 11.5.sp) },
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = themeColors.background,
                        unfocusedContainerColor = themeColors.background,
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText,
                        focusedBorderColor = themeColors.buttonEqualBg,
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f)
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                )

                IconButton(
                    onClick = { onSendMessage(userMessageText) },
                    enabled = userMessageText.isNotBlank() && !isChatLoading,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (userMessageText.isNotBlank() && !isChatLoading) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Message",
                        tint = if (userMessageText.isNotBlank() && !isChatLoading) Color.White else themeColors.displayText.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
