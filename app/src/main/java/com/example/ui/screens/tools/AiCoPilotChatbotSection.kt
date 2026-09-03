package com.example.ui.screens.tools

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors

@Composable
fun AiCoPilotChatbotSection(
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    coPilotMessages: List<CoPilotMessage>,
    userMessageText: String,
    onUserMessageTextChange: (String) -> Unit,
    isChatLoading: Boolean,
    onSendMessage: (query: String, attachmentUri: Uri?, attachmentName: String?, attachmentBytes: ByteArray?, mimeType: String?) -> Unit,
    onCvDataChange: (CvData) -> Unit,
    onCompareClick: (AiSuggestionItem) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var attachedUri by remember { mutableStateOf<Uri?>(null) }
    var attachedFileName by remember { mutableStateOf<String?>(null) }
    var attachedBytes by remember { mutableStateOf<ByteArray?>(null) }
    var attachedMimeType by remember { mutableStateOf<String?>(null) }

    // File / Image Picker Launcher
    val attachmentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                attachedUri = uri
                val contentResolver = context.contentResolver
                val mime = contentResolver.getType(uri) ?: "application/octet-stream"
                attachedMimeType = mime

                var fileName = "Attached_Document"
                val cursor = contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIdx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIdx != -1) {
                            fileName = it.getString(nameIdx) ?: fileName
                        }
                    }
                }
                attachedFileName = fileName

                val inputStream = contentResolver.openInputStream(uri)
                attachedBytes = inputStream?.readBytes()
                inputStream?.close()

                Toast.makeText(
                    context,
                    if (isBn) "ফাইল যুক্ত হয়েছে: $fileName" else "File attached: $fileName",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    if (isBn) "ফাইল পড়ার সময় ত্রুটি ঘটেছে" else "Error reading file",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Spacer(modifier = Modifier.height(18.dp))
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = themeColors.cardBg,
        border = BorderStroke(1.2.dp, themeColors.buttonEqualBg.copy(alpha = 0.35f)),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box {
                        Surface(
                            shape = CircleShape,
                            color = themeColors.buttonEqualBg.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Forum,
                                    contentDescription = null,
                                    tint = themeColors.buttonEqualBg,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        // Small online status dot on avatar
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(if (isChatLoading) Color(0xFFF59E0B) else Color(0xFF10B981), CircleShape)
                                .border(1.5.dp, themeColors.cardBg, CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isBn) "সিভি কো-পাইলট" else "CV Co-Pilot",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                            
                            // Online/Offline status badge placed directly next to title
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isChatLoading) Color(0xFFF59E0B).copy(alpha = 0.12f) else Color(0xFF10B981).copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, if (isChatLoading) Color(0xFFF59E0B).copy(alpha = 0.3f) else Color(0xFF10B981).copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(if (isChatLoading) Color(0xFFF59E0B) else Color(0xFF10B981), CircleShape)
                                    )
                                    Text(
                                        text = if (isChatLoading) (if (isBn) "প্রসেসিং..." else "Thinking...") else (if (isBn) "অনলাইন" else "Online"),
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isChatLoading) Color(0xFFF59E0B) else Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (isBn) "আপনার সিভি ও জব সার্কুলারের পূর্ণাঙ্গ তথ্য সম্বলিত স্মার্ট চ্যাটবট" else "Full context aware of your CV & job circular details",
                            fontSize = 10.sp,
                            color = themeColors.displayText.copy(alpha = 0.65f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.08f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

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
                            text = if (isBn) "সার্কুলার বা সিভি সম্পর্কিত যেকোনো প্রশ্ন করুন অথবা ছবি/ডকুমেন্ট যুক্ত করুন!" else "Ask anything about your CV & circular or attach an image/doc file!",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = themeColors.displayText.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
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
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                if (isAi) {
                                    Surface(
                                        shape = CircleShape,
                                        color = themeColors.buttonEqualBg,
                                        modifier = Modifier.size(26.dp).align(Alignment.Top)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                Column(
                                    modifier = Modifier.widthIn(max = 290.dp),
                                    horizontalAlignment = if (isAi) Alignment.Start else Alignment.End
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isAi) 2.dp else 12.dp,
                                            bottomEnd = if (isAi) 12.dp else 2.dp
                                        ),
                                        color = if (isAi) themeColors.background else themeColors.buttonEqualBg,
                                        border = BorderStroke(1.dp, if (isAi) themeColors.displayText.copy(alpha = 0.08f) else Color.Transparent),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                            SelectionContainer {
                                                Text(
                                                    text = msg.text,
                                                    fontSize = 11.5.sp,
                                                    color = if (isAi) themeColors.displayText else Color.White,
                                                    lineHeight = 16.sp
                                                )
                                            }

                                            if (isAi && msg.proposedCvData != null) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.End,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0xFF10B981),
                                                        onClick = {
                                                            onCvDataChange(msg.proposedCvData)
                                                            Toast.makeText(context, if (isBn) "সিভিতে প্রয়োগ করা হয়েছে!" else "Applied to CV!", Toast.LENGTH_SHORT).show()
                                                        }
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = "Apply to CV",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(11.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(3.dp))
                                                            Text(
                                                                text = if (isBn) "সিভিতে বসান" else "Apply to CV",
                                                                fontSize = 9.5.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.White
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (isAi && msg.proposedCvData != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = themeColors.background.copy(alpha = 0.7f),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.25f)),
                                    modifier = Modifier
                                        .padding(start = 32.dp, end = 4.dp)
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
                                                    onCompareClick(
                                                        AiSuggestionItem(
                                                            id = msg.id,
                                                            titleEn = "Co-Pilot Proposed Update",
                                                            titleBn = "কো-পাইলট প্রস্তাবিত আপডেট",
                                                            descEn = "Compare the changes suggested by Co-Pilot in this chat turn.",
                                                            descBn = "চ্যাট কো-পাইলট দ্বারা প্রস্তাবিত পরিবর্তনগুলোর তুলনা দেখে নিশ্চিত করুন।",
                                                            category = "copilot_all",
                                                            proposedValue = cvDataToJsonString(msg.proposedCvData!!)
                                                        )
                                                    )
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
                            Spacer(modifier = Modifier.height(4.dp))
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

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Chips Row
            val chips = if (isBn) {
                listOf("সার্কুলারের সাথে গ্যাপ এনালাইসিস", "সিভি সামারি চমৎকার করো", "মিসিং কিওয়ার্ড যুক্ত করো", "অভিজ্ঞতার বুলেটস সুন্দর করো")
            } else {
                listOf("Analyze gaps vs circular", "Polish CV summary", "Add missing keywords", "Improve experience bullets")
            }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                items(chips) { chip ->
                    SuggestionChip(
                        onClick = {
                            onSendMessage(chip, attachedUri, attachedFileName, attachedBytes, attachedMimeType)
                            attachedUri = null
                            attachedFileName = null
                            attachedBytes = null
                            attachedMimeType = null
                        },
                        label = { Text(text = chip, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.2f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Attached File Preview Badge
            if (attachedFileName != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = themeColors.buttonEqualBg.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (attachedMimeType?.startsWith("image") == true) Icons.Default.Image else Icons.Default.InsertDriveFile,
                                contentDescription = null,
                                tint = themeColors.buttonEqualBg,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = attachedFileName!!,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = themeColors.displayText,
                                maxLines = 1
                            )
                        }
                        IconButton(
                            onClick = {
                                attachedUri = null
                                attachedFileName = null
                                attachedBytes = null
                                attachedMimeType = null
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove attachment",
                                tint = themeColors.displayText.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Attach File Button
                IconButton(
                    onClick = { attachmentPickerLauncher.launch("*/*") },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(themeColors.background)
                        .border(1.dp, themeColors.displayText.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attach image or document",
                        tint = themeColors.buttonEqualBg,
                        modifier = Modifier.size(18.dp)
                    )
                }

                OutlinedTextField(
                    value = userMessageText,
                    onValueChange = onUserMessageTextChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = if (isBn) "কো-পাইলটকে সিভি/সার্কুলার বিষয়ক প্রশ্ন করুন..." else "Ask Co-Pilot about CV/circular...",
                            fontSize = 11.5.sp
                        )
                    },
                    maxLines = 3,
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
                    onClick = {
                        if (userMessageText.isNotBlank() || attachedBytes != null || attachedUri != null) {
                            onSendMessage(userMessageText, attachedUri, attachedFileName, attachedBytes, attachedMimeType)
                            attachedUri = null
                            attachedFileName = null
                            attachedBytes = null
                            attachedMimeType = null
                        }
                    },
                    enabled = (userMessageText.isNotBlank() || attachedUri != null) && !isChatLoading,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if ((userMessageText.isNotBlank() || attachedUri != null) && !isChatLoading)
                                themeColors.buttonEqualBg
                            else
                                themeColors.displayText.copy(alpha = 0.12f)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Message",
                        tint = if ((userMessageText.isNotBlank() || attachedUri != null) && !isChatLoading) Color.White else themeColors.displayText.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

