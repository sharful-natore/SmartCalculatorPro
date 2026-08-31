package com.example.ui.screens.tools

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.StyleSpan
import android.util.Base64
import android.widget.Toast
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

// ================= DATA MODELS FOR ATS CV =================

enum class CvTemplateStyle(
    val titleEn: String,
    val titleBn: String,
    val description: String,
    val primaryColorHex: Int,
    val isTwoColumn: Boolean = false
) {
    CLASSIC_CORPORATE(
        "Classic Corporate (ATS BD)",
        "ক্লাসিক কর্পোরেট (BD ATS Standard)",
        "ব্যাংক, বিসিএস, মাল্টিন্যাশনাল ও করপোরেট চাকরির জন্য ১০০% নিখুঁত টেবিল-বেসড অফিশিয়াল ডিজাইন",
        AndroidColor.parseColor("#0F172A")
    ),
    CANVA_MINIMALIST_CLEAN(
        "Canva Minimalist Clean (Modern)",
        "ক্যানভা মিনিমালিস্ট ক্লিন (Modern ATS)",
        "সেন্টার্ড হেডার, ফিল্ড আইকন কন্টাক্ট বার ও ডানপাশে এলাইন্ড তারিখসহ আধুনিক ক্লিন প্রফেশনাল ডিজাইন",
        AndroidColor.parseColor("#1E293B")
    ),
    SINGLE_COLUMN_HIGH_IMPACT_ATS(
        "Single-Column High Impact",
        "হাই-ইমপ্যাক্ট সিঙ্গেল কলাম (US ATS)",
        "বোল্ড হেডার, সুনির্দিষ্ট ডিভাইডার এবং ফাস্ট-স্ক্যানিং মেট্রিক বুলেট সংবলিত টপ-রেটেড এটিএস লেআউট",
        AndroidColor.parseColor("#0F172A")
    ),
    NORDIC_SLATE_MODERN(
        "Nordic Slate & Minimal",
        "নরডিক স্লেট ও মিনিমাল",
        "স্ক্যান্ডিনেভিয়ান স্লিম স্পেসিং, ডার্ক স্লেট টাইপোগ্রাফি ও মার্জিত সেকশন ডিভাইডার",
        AndroidColor.parseColor("#334155")
    ),
    SILICON_VALLEY_TECH_LEAD(
        "Silicon Valley Tech Lead",
        "সিলিকন ভ্যালি টেক লিড",
        "টেক লিড, ডেভেলপার ও প্রোডাক্ট ম্যানেজারদের জন্য কোবাল্ট ব্লু একসেন্ট ও কোয়ান্ট রেজাল্ট ডিজাইন",
        AndroidColor.parseColor("#0284C7")
    ),
    EXECUTIVE_MONOCHROME_LUXE(
        "Executive Monochrome Luxe",
        "এক্সিকিউটিভ মনোক্রোম লাক্স",
        "হাই-প্রোফাইল কর্পোরেট লিডারদের জন্য পিউর ব্ল্যাক & চারকোল লাক্সারি মিনিমালিস্ট সেরিফ স্টাইল",
        AndroidColor.parseColor("#111827")
    ),
    EXECUTIVE_TWO_COLUMN(
        "Executive Two-Column Sidebar",
        "এক্সিকিউটিভ সাইডবার (২-কলাম)",
        "অভিজ্ঞ পেশাজীবীদের জন্য ডার্ক নেভি সাইডবার, ম্যাটেরিয়াল আইকন ও স্টাইলিশ লেআউট",
        AndroidColor.parseColor("#1E293B"),
        isTwoColumn = true
    ),
    CREATIVE_MARKETING(
        "Modern Creative Banner",
        "ক্রিয়েটিভ ব্যানার প্রো",
        "মার্কেটিং ও ব্র্যান্ডিং প্রফেশনালদের জন্য আকর্ষণীয় টপ ব্যানার, প্রোফাইল ব্যাজ ও কার্ড স্টাইল",
        AndroidColor.parseColor("#881337")
    ),
    MODERN_MINIMALIST(
        "Nordic Minimalist Tech",
        "মডার্ন মিনিমালিস্ট টেক",
        "আইটি, সফটওয়্যার ইঞ্জিনিয়ার ও ডেকের জন্য ভার্টিক্যাল বার একসেন্ট, মডার্ন চিপস ও মার্জিত নকশা",
        AndroidColor.parseColor("#0D9488")
    ),
    HARVARD_CLASSIC(
        "Harvard Academic & Legal",
        "হার্ভার্ড ক্লাসিক (Academic)",
        "আইভি-লিগ স্ট্যান্ডার্ড ক্লাসিক সেরিফ টাইপোগ্রাফি, ডাবল-রুল ডিভাইডার ও সেন্টার্ড হেডার",
        AndroidColor.parseColor("#18181B")
    ),
    ELEGANT_PREMIUM(
        "Executive Serif & Ivory",
        "এলিগেন্ট সেরিফ ও আইভরি",
        "হাই-প্রোফাইল এক্সিকিউটিভ ও কনসালট্যান্টদের জন্য রয়্যাল মিডনাইট সেরিফ ও ডায়মন্ড বুলেট স্টাইল",
        AndroidColor.parseColor("#312E81")
    ),
    CLEAN_TECH_STARTUP(
        "Emerald Tech & Startup",
        "এমারেল্ড টেক স্টার্টআপ",
        "ডাইনামিক স্টার্টআপ ও প্রজেক্ট ম্যানেজমেন্ট রোলের জন্য এমারেল্ড গ্রিন ও টাইমলাইন-ডট ডিজাইন",
        AndroidColor.parseColor("#065F46")
    ),
    BANKING_FINANCE_SPECIALIST(
        "Banking & Finance Specialist",
        "ব্যাংক ও ফিন্যান্স স্পেশালিস্ট",
        "বাণিজ্যিক ও ইনভেস্টমেন্ট ব্যাংকিংয়ের জন্য রয়্যাল ব্লু একসেন্ট, কোয়ান্ট মেট্রিক্স ও টেবুলার রেকর্ড",
        AndroidColor.parseColor("#1E40AF")
    ),
    NGO_DEVELOPMENT_HUMANITARIAN(
        "NGO & Humanitarian Impact",
        "এনজিও ও উন্নয়ন প্রকল্প",
        "আন্তর্জাতিক এনজিও, ইউএন ও সমাজ উন্নয়ন প্রজেক্টের জন্য টেরাকোটা ও ফিল্ড-ইমপ্যাক্ট লেআউট",
        AndroidColor.parseColor("#9A3412")
    ),
    EUROPASS_GLOBAL_STANDARD(
        "Europass & Global MNC",
        "ইউরোপাস গ্লোবাল লেআউট",
        "ইউরোপ ও আন্তর্জাতিক চাকরির জন্য ক্লাসিক লেফট-লেবেল বক্সড গ্রিড ও ফরমাল সেকশন",
        AndroidColor.parseColor("#0369A1")
    )
}

// ================= DELIVERABLE 1: ARCHITECT DATA CLASSES =================
data class HeaderInfo(
    val fullName: String,
    val designation: String,
    val phone: String,
    val email: String,
    val location: String,
    val linkedinUrl: String
)

data class Education(
    val degreeName: String,
    val institute: String,
    val groupOrSubject: String,
    val result: String,
    val passingYear: String
)

data class Experience(
    val jobTitle: String,
    val organization: String,
    val location: String,
    val duration: String,
    val achievements: List<String>
)

data class Skill(
    val name: String,
    val category: String // Functional, Technical, Soft
)

data class PersonalDetails(
    val fatherName: String,
    val motherName: String,
    val religion: String,
    val bloodGroup: String,
    val permanentAddress: String,
    val presentAddress: String
)

data class ResumeModel(
    val header: HeaderInfo,
    val objective: String,
    val educationList: List<Education>,
    val experienceList: List<Experience>,
    val skillList: List<Skill>,
    val certifications: List<String>,
    val personalDetails: PersonalDetails,
    val references: String
)

// ================= APP LEVEL CV MODELS =================
data class CvExperienceItem(
    val id: String = UUID.randomUUID().toString(),
    val company: String = "",
    val role: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val isCurrent: Boolean = false,
    val description: String = "",
    val location: String = "Dhaka, Bangladesh"
)

data class CvEducationItem(
    val id: String = UUID.randomUUID().toString(),
    val degree: String = "",
    val institution: String = "",
    val passingYear: String = "",
    val result: String = "",
    val examLevel: String = "",
    val subjectMajor: String = "",
    val resultType: String = ""
)

data class CvSkillItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val level: String = "Proficient",
    val category: String = "Functional/Core Skills", // Functional/Core Skills, Technical/Digital Proficiency, Soft Skills/Leadership
    val description: String = ""
)

data class CvProjectItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val link: String = ""
)

data class CvCustomSectionItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val sectionType: String = "EXPERIENCE" // "EXPERIENCE", "EDUCATION", "OTHER"
)

data class CvHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val fileName: String = "",
    val filePath: String = "",
    val candidateName: String = "",
    val profileLabel: String = "",
    val templateStyle: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

private const val CV_HISTORY_PREFS = "cv_builder_history_prefs"
private const val CV_HISTORY_KEY = "cv_history_list_v1"

private fun saveCvHistory(context: Context, historyList: List<CvHistoryItem>) {
    val prefs = context.getSharedPreferences(CV_HISTORY_PREFS, Context.MODE_PRIVATE)
    val arr = JSONArray()
    historyList.forEach { item ->
        arr.put(JSONObject().apply {
            put("id", item.id)
            put("fileName", item.fileName)
            put("filePath", item.filePath)
            put("candidateName", item.candidateName)
            put("profileLabel", item.profileLabel)
            put("templateStyle", item.templateStyle)
            put("timestamp", item.timestamp)
        })
    }
    prefs.edit().putString(CV_HISTORY_KEY, arr.toString()).apply()
}

private fun loadCvHistory(context: Context): List<CvHistoryItem> {
    val prefs = context.getSharedPreferences(CV_HISTORY_PREFS, Context.MODE_PRIVATE)
    val jsonStr = prefs.getString(CV_HISTORY_KEY, null) ?: return emptyList()
    val list = mutableListOf<CvHistoryItem>()
    try {
        val arr = JSONArray(jsonStr)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                CvHistoryItem(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    fileName = obj.optString("fileName"),
                    filePath = obj.optString("filePath"),
                    candidateName = obj.optString("candidateName"),
                    profileLabel = obj.optString("profileLabel"),
                    templateStyle = obj.optString("templateStyle"),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                )
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    val validList = list.filter { java.io.File(it.filePath).exists() }
    if (validList.size != list.size) {
        saveCvHistory(context, validList)
    }
    return validList
}

private fun addOrUpdateCvHistory(context: Context, file: File, cvData: CvData) {
    val current = loadCvHistory(context).toMutableList()
    current.removeAll { it.filePath == file.absolutePath }
    current.add(
        0,
        CvHistoryItem(
            fileName = file.name,
            filePath = file.absolutePath,
            candidateName = cvData.fullName.ifBlank { "Candidate" },
            profileLabel = cvData.profileLabel.ifBlank { "Default Profile" },
            templateStyle = cvData.templateStyle.name,
            timestamp = System.currentTimeMillis()
        )
    )
    val trimmed = if (current.size > 50) current.take(50) else current
    saveCvHistory(context, trimmed)
}

private fun deleteCvHistoryItem(context: Context, id: String) {
    val current = loadCvHistory(context).toMutableList()
    val item = current.find { it.id == id }
    if (item != null) {
        try {
            val file = File(item.filePath)
            if (file.exists()) file.delete()
        } catch (_: Exception) {}
        current.removeAll { it.id == id }
        saveCvHistory(context, current)
    }
}

private fun clearAllCvHistory(context: Context) {
    val current = loadCvHistory(context)
    current.forEach { item ->
        try {
            val file = File(item.filePath)
            if (file.exists()) file.delete()
        } catch (_: Exception) {}
    }
    saveCvHistory(context, emptyList())
}

private fun openPdfFile(context: Context, file: File) {
    try {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Open CV PDF"))
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open PDF viewer", Toast.LENGTH_SHORT).show()
    }
}

private fun sharePdfFile(context: Context, file: File) {
    try {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share CV PDF"))
    } catch (e: Exception) {
        Toast.makeText(context, "Could not share file", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun CvHistoryDialog(
    historyList: List<CvHistoryItem>,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit,
    onOpenPdf: (CvHistoryItem) -> Unit,
    onSharePdf: (CvHistoryItem) -> Unit,
    onDeletePdf: (CvHistoryItem) -> Unit,
    onEditProfile: (CvHistoryItem) -> Unit,
    onClearAllHistory: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বন্ধ করুন" else "Close", color = themeColors.buttonEqualBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            if (historyList.isNotEmpty()) {
                TextButton(onClick = onClearAllHistory) {
                    Text(if (isBn) "সমস্ত ইতিহাস মুছুন" else "Clear All History", color = Color.Red.copy(alpha = 0.8f))
                }
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.History, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBn) "সিভি তৈরি ও হিস্টোরি" else "Generated CV History",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
            }
        },
        text = {
            if (historyList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.FolderOff, contentDescription = null, tint = themeColors.displayText.copy(alpha = 0.3f), modifier = Modifier.size(44.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBn) "এখনও কোনো সিভি ফাইল হিস্টোরিতে সেভ হয়নি।" else "No generated CV history found yet.",
                            fontSize = 12.sp,
                            color = themeColors.displayText.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(historyList, key = { it.id }) { item ->
                        val file = File(item.filePath)
                        val fileExists = file.exists()
                        val fileSizeKb = if (fileExists) file.length() / 1024 else 0

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = themeColors.cardBg,
                            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.candidateName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = themeColors.displayText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${item.profileLabel} • ${item.fileName}",
                                            fontSize = 10.5.sp,
                                            color = themeColors.buttonEqualBg,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeletePdf(item) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val dateStr = try {
                                        java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(item.timestamp))
                                    } catch (_: Exception) { "" }

                                    Text(
                                        text = "$dateStr ${if (fileSizeKb > 0) "($fileSizeKb KB)" else ""}",
                                        fontSize = 9.5.sp,
                                        color = themeColors.displayText.copy(alpha = 0.5f)
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (fileExists) {
                                            OutlinedButton(
                                                onClick = { onOpenPdf(item) },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(26.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(if (isBn) "দেখুন" else "Open", fontSize = 10.sp)
                                            }

                                            OutlinedButton(
                                                onClick = { onSharePdf(item) },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(26.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(if (isBn) "শেয়ার" else "Share", fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = themeColors.background,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun CvCustomDropdown(
    label: String,
    selectedValue: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    themeColors: CalculatorThemeColors,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontSize = 11.sp, color = themeColors.displayText.copy(alpha = 0.7f)) },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Expand",
                        tint = themeColors.buttonEqualBg
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = themeColors.buttonEqualBg,
                unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                focusedTextColor = themeColors.displayText,
                unfocusedTextColor = themeColors.displayText,
                focusedContainerColor = themeColors.cardBg,
                unfocusedContainerColor = themeColors.cardBg
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(themeColors.background)
                .heightIn(max = 260.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            fontSize = 12.sp,
                            color = if (option == selectedValue) themeColors.buttonEqualBg else themeColors.displayText,
                            fontWeight = if (option == selectedValue) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SaveProfileDialog(
    currentLabel: String,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit,
    onSaveProfile: (String) -> Unit
) {
    var profileName by remember { mutableStateOf(currentLabel) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (profileName.isNotBlank()) {
                        onSaveProfile(profileName.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isBn) "সেভ করুন" else "Save Profile", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText.copy(alpha = 0.7f))
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBn) "সিভি তথ্য প্রোফাইল হিসেবে সেভ করুন" else "Save CV as Profile",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
            }
        },
        text = {
            Column {
                Text(
                    text = if (isBn) "ভবিষ্যতে চাকরির আবেদন করার সময় এই তথ্য সরাসরি অটো-ইনপুট দেওয়ার জন্য একটি স্মরণীয় নাম দিয়ে প্রোফাইল সেভ করুন:" else "Save your full CV inputs under a custom profile label for quick auto-filling later:",
                    fontSize = 12.sp,
                    color = themeColors.displayText.copy(alpha = 0.7f),
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    label = { Text(if (isBn) "প্রোফাইলের নাম (e.g., Shariful - Bank Job CV)" else "Profile Label (e.g., Shariful - Officer Profile)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColors.buttonEqualBg,
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        containerColor = themeColors.background,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun ProfileManagerDialog(
    profilesList: List<CvData>,
    activeProfileId: String,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit,
    onSelectProfile: (CvData) -> Unit,
    onDeleteProfile: (CvData) -> Unit,
    onImportPdfResume: () -> Unit
) {
    var profileToDelete by remember { mutableStateOf<CvData?>(null) }
    var profileToSelect by remember { mutableStateOf<CvData?>(null) }
    
    if (profileToDelete != null) {
        AlertDialog(
            onDismissRequest = { profileToDelete = null },
            confirmButton = {
                Button(onClick = {
                    profileToDelete?.let { onDeleteProfile(it) }
                    profileToDelete = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text(if (isBn) "হ্যাঁ, মুছুন" else "Yes, Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { profileToDelete = null }) {
                    Text(if (isBn) "না" else "No")
                }
            },
            title = { Text(if (isBn) "নিশ্চিত করুন" else "Confirm Delete") },
            text = { Text(if (isBn) "আপনি কি নিশ্চিত যে এই প্রোফাইলটি ডিলিট করতে চান?" else "Are you sure you want to delete this profile?") }
        )
    }

    if (profileToSelect != null) {
        AlertDialog(
            onDismissRequest = { profileToSelect = null },
            confirmButton = {
                Button(onClick = {
                    profileToSelect?.let {
                        onSelectProfile(it)
                        onDismiss()
                    }
                    profileToSelect = null
                }, colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)) {
                    Text(if (isBn) "হ্যাঁ" else "Yes", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { profileToSelect = null }) {
                    Text(if (isBn) "না" else "No")
                }
            },
            title = { Text(if (isBn) "নিশ্চিত করুন" else "Confirm Selection") },
            text = { Text(if (isBn) "আপনি কি এই প্রোফাইলটি ব্যবহার করতে চান? বর্তমান প্রোফাইলের অসংরক্ষিত ডেটা মুছে যেতে পারে।" else "Do you want to switch to this profile? Unsaved data may be lost.") }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বন্ধ করুন" else "Close", color = themeColors.buttonEqualBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onImportPdfResume,
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isBn) "PDF ইমপোর্ট (এআই)" else "Import PDF (AI)", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBn) "সেভ করা প্রোফাইলসমূহ" else "Saved Profiles",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
            }
        },
        text = {
            if (profilesList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBn) "কোনো সেভ করা প্রোফাইল নেই। 'PDF ইমপোর্ট' বাটনে চাপ দিন অথবা প্রিভিউ ট্যাবে গিয়ে 'প্রোফাইল হিসেবে সেভ করুন' বাটন চাপুন।" else "No saved custom profiles. Click 'Import PDF' or save your current input as a profile.",
                        fontSize = 11.5.sp,
                        color = themeColors.displayText.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(profilesList, key = { it.id }) { profile ->
                        val isActive = profile.id == activeProfileId
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isActive) themeColors.buttonEqualBg.copy(alpha = 0.12f) else themeColors.cardBg,
                            border = BorderStroke(if (isActive) 1.5.dp else 1.dp, if (isActive) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.12f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = profile.profileLabel.ifBlank { "Saved Profile" },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = themeColors.displayText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${profile.fullName.ifBlank { "Candidate" }} • ${profile.jobTitle.ifBlank { "No Title" }}",
                                            fontSize = 10.5.sp,
                                            color = themeColors.displayText.copy(alpha = 0.6f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    IconButton(
                                        onClick = { profileToDelete = profile },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isActive) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = themeColors.buttonEqualBg.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = if (isBn) "সক্রিয় প্রোফাইল" else "Active Profile",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = themeColors.buttonEqualBg,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.width(1.dp))
                                    }

                                    Button(
                                        onClick = { profileToSelect = profile },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
                                    ) {
                                        Icon(imageVector = Icons.Default.Input, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isBn) "সিলেক্ট ও অটো-ইনপুট" else "Select & Auto-Fill", fontSize = 10.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = themeColors.background,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun CvAiPromptDialog(
    title: String,
    defaultPrompt: String,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit,
    onGenerate: (promptText: String) -> Unit
) {
    var userPrompt by remember { mutableStateOf(defaultPrompt) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (userPrompt.isNotBlank()) {
                        onGenerate(userPrompt.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isBn) "এআই দিয়ে জেনারেট করুন" else "Generate with AI", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText.copy(alpha = 0.7f))
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
            }
        },
        text = {
            Column {
                Text(
                    text = if (isBn) "আপনার পছন্দমতো প্রম্পটটি কাস্টমাইজ করুন অথবা ডিফল্ট প্রম্পট অনুযায়ী জেনারেট করুন:" else "Customize your AI prompt instructions below to generate targeted CV content:",
                    fontSize = 11.5.sp,
                    color = themeColors.displayText.copy(alpha = 0.7f),
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = userPrompt,
                    onValueChange = { userPrompt = it },
                    label = { Text(if (isBn) "এআই কাস্টম প্রম্পট" else "Custom AI Prompt Instructions") },
                    minLines = 3,
                    maxLines = 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColors.buttonEqualBg,
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                )
            }
        },
        containerColor = themeColors.background,
        shape = RoundedCornerShape(16.dp)
    )
}

data class CvData(
    val id: String = UUID.randomUUID().toString(),
    val profileLabel: String = "Default",
    val fullName: String = "",
    val jobTitle: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val linkedin: String = "",
    val githubOrPortfolio: String = "",
    val summary: String = "",
    val photoBase64: String = "",
    val photoShape: String = "Circle", // Circle, Rounded, Square
    val photoScale: Float = 1.0f,
    val photoOffsetX: Float = 0f,
    val photoOffsetY: Float = 0f,
    val fatherName: String = "",
    val motherName: String = "",
    val religion: String = "",
    val bloodGroup: String = "",
    val permanentAddress: String = "",
    val presentAddress: String = "",
    val certifications: String = "",
    val references: String = "",
    val experiences: List<CvExperienceItem> = emptyList(),
    val educations: List<CvEducationItem> = emptyList(),
    val skills: List<CvSkillItem> = emptyList(),
    val projects: List<CvProjectItem> = emptyList(),
    val languages: String = "",
    val templateStyle: CvTemplateStyle = CvTemplateStyle.CLASSIC_CORPORATE,
    val targetJobCircular: String = "",
    val isFresher: Boolean = false,
    val fresherAcademicProjects: String = "",
    val fresherInternshipsVolunteer: String = "",
    val fresherLeadershipClubs: String = "",
    val fresherKeyCoursework: String = "",
    val showSignatureLine: Boolean = true,

    // Advanced Customization & Dynamic Flexibility Options
    val customSections: List<CvCustomSectionItem> = emptyList(),
    val sectionOrder: List<String> = listOf("SUMMARY", "EXPERIENCE", "EDUCATION", "SKILLS", "PROJECTS", "CERTIFICATIONS", "LANGUAGES", "CUSTOM_SECTIONS", "PERSONAL_INFO", "REFERENCES"),
    val hiddenSections: List<String> = emptyList(),
    val showContactIcons: Boolean = true,
    val showSectionIcons: Boolean = false,
    val fontScale: String = "STANDARD", // "COMPACT", "STANDARD", "COMFORTABLE", "LARGE"
    val bulletStyle: String = "BULLET", // "BULLET", "DASH", "SQUARE", "DIAMOND", "COMMA", "PIPE", "NONE"
    val customMargin: Float = 36f,
    val sectionSpacing: Float = 8f,
    val itemSpacing: Float = 4f,
    val customLineSpacing: Float = 1.15f
)

private fun getFormattedDegreeText(edu: CvEducationItem): String {
    val level = if (edu.examLevel.isNotBlank() && edu.examLevel != "Others") edu.examLevel else edu.degree
    val major = if (edu.subjectMajor.isNotBlank() && edu.subjectMajor != "Others" && edu.subjectMajor != "General") edu.subjectMajor else ""
    return if (level.isNotBlank()) {
        if (major.isNotBlank()) {
            "$level in $major"
        } else {
            level
        }
    } else {
        major.ifBlank { "Degree" }
    }
}

// ================= CLEAN ATS SYSTEM TYPEFACES (PREVENT SYSTEM FONT OVERRIDE) =================
private object CleanPdfTypefaces {
    val sansRegular: Typeface by lazy {
        loadSystemFont(
            listOf(
                "/system/fonts/Roboto-Regular.ttf",
                "/system/fonts/NotoSans-Regular.ttf",
                "/system/fonts/DroidSans.ttf"
            ),
            Typeface.SANS_SERIF,
            Typeface.NORMAL
        )
    }

    val sansBold: Typeface by lazy {
        loadSystemFont(
            listOf(
                "/system/fonts/Roboto-Bold.ttf",
                "/system/fonts/NotoSans-Bold.ttf",
                "/system/fonts/DroidSans-Bold.ttf"
            ),
            Typeface.SANS_SERIF,
            Typeface.BOLD
        )
    }

    val sansMedium: Typeface by lazy {
        loadSystemFont(
            listOf(
                "/system/fonts/Roboto-Medium.ttf",
                "/system/fonts/Roboto-Bold.ttf",
                "/system/fonts/NotoSans-Medium.ttf"
            ),
            Typeface.SANS_SERIF,
            Typeface.BOLD
        )
    }

    val serifRegular: Typeface by lazy {
        loadSystemFont(
            listOf(
                "/system/fonts/NotoSerif-Regular.ttf",
                "/system/fonts/DroidSerif-Regular.ttf",
                "/system/fonts/TimesNewRoman.ttf"
            ),
            Typeface.SERIF,
            Typeface.NORMAL
        )
    }

    val serifBold: Typeface by lazy {
        loadSystemFont(
            listOf(
                "/system/fonts/NotoSerif-Bold.ttf",
                "/system/fonts/DroidSerif-Bold.ttf"
            ),
            Typeface.SERIF,
            Typeface.BOLD
        )
    }

    private fun loadSystemFont(paths: List<String>, fallbackTypeface: Typeface, style: Int): Typeface {
        for (path in paths) {
            try {
                val f = File(path)
                if (f.exists() && f.canRead()) {
                    val tf = Typeface.createFromFile(f)
                    if (tf != null) {
                        return if (style == Typeface.BOLD) Typeface.create(tf, Typeface.BOLD) else tf
                    }
                }
            } catch (_: Exception) {}
        }
        return Typeface.create(fallbackTypeface, style)
    }
}

// SharedPreferences Multi-Profile Helpers
private const val CV_PREFS_NAME = "ats_cv_builder_multi_prefs_v6"
private const val CV_PROFILES_LIST_KEY = "saved_cv_profiles_json_list"
private const val ACTIVE_PROFILE_ID_KEY = "active_cv_profile_uuid"

private fun getSeedProfilesList(): List<CvData> {
    val defaultMba = CvData(
        id = "profile_mba_shariful",
        profileLabel = "Default",
        fullName = "Md. Shariful Islam",
        jobTitle = "Management Graduate & Business Analyst",
        email = "shariful.mba@example.com",
        phone = "+880 1711-223344",
        address = "Dhaka, Bangladesh",
        linkedin = "linkedin.com/in/shariful-mba",
        githubOrPortfolio = "portfolio.shariful.com",
        summary = "A highly analytical and result-oriented Management MBA Graduate with a strong foundation in business strategy, market research, and financial analysis. Proven expertise in leveraging data-driven insights to optimize business operations, increase project efficiency, and drive marketing campaigns. Excellent communication, team leadership, and strategic planning skills.",
        isFresher = false,
        fresherAcademicProjects = "• Final Year Capstone: Supply Chain Optimization & Demand Forecasting Model\n• Analyzed FMCG retail distribution networks using statistical forecasting, increasing warehouse inventory turnaround by 18%.\n• Business Analytics Seminar: Consumer Credit Risk Assessment with Regression Modeling",
        fresherInternshipsVolunteer = "• Strategic Projects Intern — Bangladesh Business Leadership Forum (3 Months)\n• Coordinated digital communication, executive speaker sessions, and student registration for 1,200+ attendees.\n• Volunteer Community Lead — Youth Empowerment & Skills Initiative (2023)",
        fresherLeadershipClubs = "• Vice President — University Management & Business Club (2022-2023)\n• Finalist — National Inter-University Business Case Competition 2023\n• Chief Organizer — DU National Business Fest 2022",
        fresherKeyCoursework = "Strategic Management, Corporate Finance, Business Statistics, Marketing Analytics, Supply Chain Logistics, Financial Accounting, Econometrics",
        showSignatureLine = true,
        experiences = listOf(
            CvExperienceItem(
                company = "Apex Business Solutions",
                role = "Associate Business Analyst",
                startDate = "Jan 2024",
                endDate = "Present",
                isCurrent = true,
                description = "• Conducted market research and competitor analysis to assist in strategic decision-making.\n• Developed interactive business performance dashboards, increasing analytical clarity by 25%.\n• Managed cross-functional project coordination across marketing and product design teams."
            ),
            CvExperienceItem(
                company = "Strategic Marketing Group",
                role = "Management Trainee (Intern)",
                startDate = "Jun 2023",
                endDate = "Dec 2023",
                isCurrent = false,
                description = "• Collaborated on brand optimization and promotional campaigns for FMCG sector clients.\n• Assisted in preparation of financial models and feasibility reports for new product launches."
            )
        ),
        educations = listOf(
            CvEducationItem(
                degree = "Master of Business Administration (MBA)",
                institution = "Institute of Business Administration (IBA), University of Dhaka",
                passingYear = "2023",
                result = "CGPA: 3.82 / 4.00"
            ),
            CvEducationItem(
                degree = "Bachelor of Business Administration (BBA) in Management",
                institution = "University of Dhaka",
                passingYear = "2021",
                result = "CGPA: 3.78 / 4.00"
            )
        ),
        skills = listOf(
            CvSkillItem(name = "Financial & Record Management", description = "Skilled in tracking, daily record-keeping, and MS Excel analysis/reporting.", level = "Expert", category = "Functional/Core Skills"),
            CvSkillItem(name = "Communication & Negotiation", description = "Strong interpersonal skills for client engagement, negotiation, and relations.", level = "Expert", category = "Soft Skills/Leadership"),
            CvSkillItem(name = "Target-Driven Execution", description = "Goal-oriented approach focused on achieving operational targets and delivering results.", level = "Expert", category = "Functional/Core Skills"),
            CvSkillItem(name = "Digital & Tech Proficiency", description = "Tech-savvy with practical experience in mobile apps, data collection, and reporting.", level = "Expert", category = "Technical/Digital Proficiency"),
            CvSkillItem(name = "Analytical & Problem-Solving", description = "Ability to analyze operational challenges, optimize workflows, and adapt quickly.", level = "Expert", category = "Functional/Core Skills"),
            CvSkillItem(name = "Mobility & Teamwork", description = "Adaptable team player ready to travel extensively and relocate anywhere in Bangladesh.", level = "Expert", category = "Soft Skills/Leadership")
        ),
        projects = emptyList(),
        languages = "English (Professional), Bengali (Native)",
        certifications = "Project Management Professional (PMP) - PMI, 2024\nBusiness Intelligence Certification - Google / Coursera, 2023",
        references = "Available upon request.",
        templateStyle = CvTemplateStyle.CLASSIC_CORPORATE
    )

    val adminHrDraft = CvData(
        id = "profile_admin_hr",
        profileLabel = "HR & Admin Officer (এইচআর ও এডমিন)",
        fullName = "Farhana Yasmin",
        jobTitle = "Senior HR & Administrative Officer",
        email = "farhana.hr@example.com",
        phone = "+880 1819-556677",
        address = "Gulshan, Dhaka, Bangladesh",
        linkedin = "linkedin.com/in/farhana-hr",
        summary = "Dedicated Human Resources and Administrative professional with 5+ years of experience in talent acquisition, employee relations, payroll administration, and office operations. Adept at implementing HR policies, organizing compliance audits, and driving employee engagement initiatives to foster workplace productivity.",
        experiences = listOf(
            CvExperienceItem(
                company = "Beacon Corporate Group",
                role = "Senior HR & Admin Executive",
                startDate = "Feb 2022",
                endDate = "Present",
                isCurrent = true,
                description = "• Spearheaded end-to-end recruitment pipelines, onboarding 60+ skilled professionals across IT and Finance.\n• Supervised monthly payroll administration, provident fund deductions, and statutory compliance.\n• Formulated company-wide HR handbook and code of conduct policies reducing turnover by 15%."
            ),
            CvExperienceItem(
                company = "Orion Technologies Ltd",
                role = "HR Officer",
                startDate = "Jul 2019",
                endDate = "Jan 2022",
                isCurrent = false,
                description = "• Maintained confidential employee databases, leave records, and performance evaluation metrics.\n• Organized quarterly employee wellness workshops and cross-team team building events."
            )
        ),
        educations = listOf(
            CvEducationItem(
                degree = "MBA in Human Resource Management (HRM)",
                institution = "North South University",
                passingYear = "2019",
                result = "CGPA: 3.75 / 4.00"
            ),
            CvEducationItem(
                degree = "BBA in Management Studies",
                institution = "University of Dhaka",
                passingYear = "2017",
                result = "CGPA: 3.65 / 4.00"
            )
        ),
        skills = listOf(
            CvSkillItem(name = "Talent Acquisition & Headhunting", level = "Expert"),
            CvSkillItem(name = "Bangladesh Labor Law & Compliance", level = "Expert"),
            CvSkillItem(name = "Payroll & Compensation Management", level = "Expert"),
            CvSkillItem(name = "Performance Appraisal (KPI & OKR)", level = "Proficient"),
            CvSkillItem(name = "HRIS (Keka, BambooHR, Excel)", level = "Expert"),
            CvSkillItem(name = "Conflict Resolution & Negotiation", level = "Expert")
        ),
        languages = "English (Fluent), Bengali (Native)",
        certifications = "Certified Human Resource Professional (CHRP) - SHRM, 2021\nPost Graduate Diploma in Personnel Management (PGDPM) - BIM, 2018",
        references = "Available upon request.",
        templateStyle = CvTemplateStyle.EXECUTIVE_TWO_COLUMN
    )

    val bankFinanceDraft = CvData(
        id = "profile_banking_finance",
        profileLabel = "Banking & Financial Analyst (ব্যাংক ও ফিন্যান্স)",
        fullName = "Tariqul Anam, CDCS",
        jobTitle = "Principal Financial Analyst & Credit Risk Officer",
        email = "tariqul.bank@example.com",
        phone = "+880 1912-334455",
        address = "Motijheel, Dhaka, Bangladesh",
        linkedin = "linkedin.com/in/tariqul-finance",
        summary = "Results-driven Banking and Financial Analyst with 6+ years of rigorous experience in corporate credit appraisal, risk assessment, syndicated loan structuring, and financial modeling. Demonstrated history of managing BDT 450+ Crore credit portfolio with zero default rate.",
        experiences = listOf(
            CvExperienceItem(
                company = "Eastern Bank PLC",
                role = "Senior Officer - Corporate Credit Division",
                startDate = "Mar 2021",
                endDate = "Present",
                isCurrent = true,
                description = "• Evaluated corporate loan proposals and conducted audited financial statement ratio analyses.\n• Prepared comprehensive Credit Appraisal Memorandums (CAM) aligned with Bangladesh Bank CRG guidelines.\n• Monitored portfolio asset quality and conducted regular stress testing on capital adequacy."
            ),
            CvExperienceItem(
                company = "Standard Chartered Bank",
                role = "Credit Analyst - SME Banking",
                startDate = "Jan 2018",
                endDate = "Feb 2021",
                isCurrent = false,
                description = "• Assessed creditworthiness for 120+ SME clients, disbursing BDT 80 Crore in structured facilities.\n• Optimized verification turn-around time (TAT) by 30% through automated scoring templates."
            )
        ),
        educations = listOf(
            CvEducationItem(
                degree = "Master of Science (M.Sc.) in Finance & Banking",
                institution = "University of Dhaka",
                passingYear = "2017",
                result = "CGPA: 3.88 / 4.00"
            ),
            CvEducationItem(
                degree = "BBA in Banking and Insurance",
                institution = "University of Dhaka",
                passingYear = "2015",
                result = "CGPA: 3.80 / 4.00"
            )
        ),
        skills = listOf(
            CvSkillItem(name = "Credit Risk Grading (CRG)", level = "Expert"),
            CvSkillItem(name = "Financial Ratio & Cash Flow Analysis", level = "Expert"),
            CvSkillItem(name = "Trade Finance (Letter of Credit & LC)", level = "Expert"),
            CvSkillItem(name = "DCF & Valuation Modeling", level = "Proficient"),
            CvSkillItem(name = "Bangladesh Bank Regulatory Compliance", level = "Expert"),
            CvSkillItem(name = "Advanced Excel VBA & FinTech", level = "Expert")
        ),
        languages = "English (Professional Working), Bengali (Native)",
        certifications = "Banking Professional Examination (DAIBB) - IBB, 2020\nCertified Documentary Credit Specialist (CDCS) - London Institute of Banking & Finance, 2022",
        references = "Prof. Dr. M. A. Baqui, Department of Finance, University of Dhaka",
        templateStyle = CvTemplateStyle.BANKING_FINANCE_SPECIALIST
    )

    val marketingDraft = CvData(
        id = "profile_marketing_specialist",
        profileLabel = "Brand & Marketing Manager (মার্কেটিং)",
        fullName = "Nafis Imtiaz",
        jobTitle = "Brand & Strategic Marketing Lead",
        email = "nafis.marketing@example.com",
        phone = "+880 1713-998877",
        address = "Banani, Dhaka, Bangladesh",
        linkedin = "linkedin.com/in/nafis-marketing",
        githubOrPortfolio = "nafismarketing.com",
        summary = "Creative and analytical Brand Marketing Specialist with 5+ years of experience leading multi-channel promotional campaigns, digital performance growth, and product launch roadmaps. Successfully grew customer acquisition by 45% and oversaw BDT 2 Crore annual advertising budgets.",
        experiences = listOf(
            CvExperienceItem(
                company = "Pran-RFL Group",
                role = "Brand Manager - Beverage Category",
                startDate = "Oct 2021",
                endDate = "Present",
                isCurrent = true,
                description = "• Managed 3 flagship national FMCG brands with full P&L accountability.\n• Directed 360-degree ATL/BTL marketing campaigns achieving a 22% uplift in market share.\n• Led social media influencer programs that generated over 15 Million organic digital impressions."
            ),
            CvExperienceItem(
                company = "Mindshare Bangladesh",
                role = "Digital Media Planner",
                startDate = "Jan 2019",
                endDate = "Sep 2021",
                isCurrent = false,
                description = "• Executed programmatic PPC and Meta ad campaigns with an average ROAS of 4.2x.\n• Formulated consumer behavior insights using Google Analytics 4 and Semrush."
            )
        ),
        educations = listOf(
            CvEducationItem(
                degree = "MBA in Marketing",
                institution = "Institute of Business Administration (IBA), University of Dhaka",
                passingYear = "2018",
                result = "CGPA: 3.70 / 4.00"
            ),
            CvEducationItem(
                degree = "BBA in Marketing",
                institution = "BRAC University",
                passingYear = "2016",
                result = "CGPA: 3.65 / 4.00"
            )
        ),
        skills = listOf(
            CvSkillItem(name = "Brand Identity & Positioning", level = "Expert"),
            CvSkillItem(name = "Digital Performance Marketing (Meta & Google)", level = "Expert"),
            CvSkillItem(name = "Consumer Market Research & Insights", level = "Expert"),
            CvSkillItem(name = "Content Strategy & Copywriting", level = "Proficient"),
            CvSkillItem(name = "P&L Budget Allocation", level = "Proficient"),
            CvSkillItem(name = "SEO/SEM & Data Analytics", level = "Proficient")
        ),
        languages = "English (Fluent), Bengali (Native)",
        certifications = "Google Ads & Analytics Certified Professional, 2023\nHubSpot Inbound Marketing Master, 2022",
        references = "Available upon request.",
        templateStyle = CvTemplateStyle.CREATIVE_MARKETING
    )

    val ngoProjectDraft = CvData(
        id = "profile_ngo_project",
        profileLabel = "NGO & Humanitarian Officer (এনজিও ও উন্নয়ন)",
        fullName = "Khadija Sultana",
        jobTitle = "Humanitarian Project Coordinator & MEAL Specialist",
        email = "khadija.ngo@example.com",
        phone = "+880 1814-112233",
        address = "Cox's Bazar / Dhaka, Bangladesh",
        linkedin = "linkedin.com/in/khadija-humanitarian",
        summary = "Dedicated Humanitarian Development Practitioner with 6+ years of field experience in monitoring, evaluation, accountability, and learning (MEAL) across international NGOs and UN partner projects. Proven record in managing USAID and FCDO funded emergency response programs.",
        experiences = listOf(
            CvExperienceItem(
                company = "BRAC International",
                role = "Project Manager - Ultra-Poor Graduation Program",
                startDate = "Apr 2021",
                endDate = "Present",
                isCurrent = true,
                description = "• Supervised field execution of livelihood enhancement interventions supporting 12,000+ vulnerable households.\n• Coordinated baseline, midline, and endline surveys utilizing KoboToolbox and ODK mobile data tools.\n• Liaised with local government authorities, UNO, and community leaders for seamless project facilitation."
            ),
            CvExperienceItem(
                company = "Save the Children International",
                role = "MEAL Officer - Emergency Response",
                startDate = "Jan 2018",
                endDate = "Mar 2021",
                isCurrent = false,
                description = "• Established community feedback and complaint response mechanisms (CRM) ensuring 100% accountability.\n• Drafted monthly donor progress reports adhering to USAID and ECHO grant specifications."
            )
        ),
        educations = listOf(
            CvEducationItem(
                degree = "Master of Social Science (MSS) in Development Studies",
                institution = "University of Dhaka",
                passingYear = "2017",
                result = "First Class (CGPA: 3.80)"
            ),
            CvEducationItem(
                degree = "Bachelor of Social Science (BSS) in Sociology",
                institution = "University of Dhaka",
                passingYear = "2015",
                result = "First Class (CGPA: 3.72)"
            )
        ),
        skills = listOf(
            CvSkillItem(name = "Project Cycle Management (PCM)", level = "Expert"),
            CvSkillItem(name = "MEAL & Qualitative/Quantitative Research", level = "Expert"),
            CvSkillItem(name = "KoboToolbox, ODK & SPSS", level = "Expert"),
            CvSkillItem(name = "Donor Reporting (USAID, FCDO, UN)", level = "Expert"),
            CvSkillItem(name = "Community Mobilization & Safeguarding", level = "Expert"),
            CvSkillItem(name = "Disaster Risk Reduction (DRR)", level = "Proficient")
        ),
        languages = "English (Fluent), Bengali (Native), Chittagonian / Rohingya (Working)",
        certifications = "Project DPro (PMD Pro) - APMG International, 2020\nHumanitarian Logistics & Sphere Standards Certification, 2019",
        references = "Country Representative, Save the Children Bangladesh",
        templateStyle = CvTemplateStyle.NGO_DEVELOPMENT_HUMANITARIAN
    )

    val softwareDevDraft = CvData(
        id = "profile_software_engineer",
        profileLabel = "Software Engineer (সফটওয়্যার ও আইটি)",
        fullName = "Tanvir Ahmed",
        jobTitle = "Senior Full Stack Software Engineer",
        email = "tanvir.dev@example.com",
        phone = "+880 1715-443322",
        address = "Mirpur, Dhaka, Bangladesh",
        linkedin = "linkedin.com/in/tanvir-codes",
        githubOrPortfolio = "github.com/tanvir-dev",
        summary = "Passionate and architecture-driven Senior Software Engineer with 5+ years of production experience building high-throughput microservices, scalable web APIs, and responsive mobile interfaces. Specialized in Kotlin, TypeScript, Node.js, Next.js, and AWS cloud deployment.",
        experiences = listOf(
            CvExperienceItem(
                company = "Brain Station 23 PLC",
                role = "Senior Software Engineer",
                startDate = "Jul 2022",
                endDate = "Present",
                isCurrent = true,
                description = "• Engineered cloud microservices processing 2.5M daily fintech transactions with 99.99% uptime.\n• Spearheaded the migration from monolithic architecture to Dockerized Kubernetes clusters on AWS.\n• Mentored 8 junior and mid-level engineers in TDD, CI/CD automation, and clean code standards."
            ),
            CvExperienceItem(
                company = "Dynamic Solution Innovators (DSi)",
                role = "Software Engineer",
                startDate = "Oct 2019",
                endDate = "Jun 2022",
                isCurrent = false,
                description = "• Developed RESTful and GraphQL backend endpoints using Node.js, NestJS, and PostgreSQL.\n• Reduced database query latency by 40% through indexing optimization and Redis distributed caching."
            )
        ),
        educations = listOf(
            CvEducationItem(
                degree = "B.Sc. in Computer Science and Engineering (CSE)",
                institution = "Bangladesh University of Engineering and Technology (BUET)",
                passingYear = "2019",
                result = "CGPA: 3.85 / 4.00"
            )
        ),
        skills = listOf(
            CvSkillItem(name = "Kotlin, Java & Android", level = "Expert"),
            CvSkillItem(name = "TypeScript, React & Next.js", level = "Expert"),
            CvSkillItem(name = "Node.js, Express & NestJS", level = "Expert"),
            CvSkillItem(name = "PostgreSQL, MongoDB & Redis", level = "Expert"),
            CvSkillItem(name = "Docker, Kubernetes & AWS", level = "Proficient"),
            CvSkillItem(name = "System Design & Microservices", level = "Expert")
        ),
        projects = listOf(
            CvProjectItem(
                title = "FinTech Instant Settlement Engine",
                description = "Developed an open-source high-speed payment gateway reconciliation microservice handling 500+ rps with sub-50ms latency.",
                link = "github.com/tanvir-dev/fintech-engine"
            )
        ),
        languages = "English (Professional Working), Bengali (Native)",
        certifications = "AWS Certified Solutions Architect – Associate (2023)\nOracle Certified Professional: Java SE 11 Developer (2021)",
        references = "Available upon request.",
        templateStyle = CvTemplateStyle.MODERN_MINIMALIST
    )

    val supplyChainDraft = CvData(
        id = "profile_supply_chain",
        profileLabel = "Supply Chain & Procurement (সাপ্লাই চেইন)",
        fullName = "Mahmudur Rahman, CSCP",
        jobTitle = "Supply Chain & Strategic Sourcing Specialist",
        email = "mahmud.scm@example.com",
        phone = "+880 1718-778899",
        address = "Agrabad, Chattogram, Bangladesh",
        linkedin = "linkedin.com/in/mahmud-scm",
        summary = "Seasoned Supply Chain & Procurement professional with 6+ years of expertise in strategic vendor negotiation, customs clearance (Chittagong Port), inventory replenishment, and SAP MM module execution. Achieved annual cost reductions of BDT 3.5 Crore through competitive vendor contracting.",
        experiences = listOf(
            CvExperienceItem(
                company = "BSRM Group of Companies",
                role = "Assistant Manager - Supply Chain & Logistics",
                startDate = "May 2021",
                endDate = "Present",
                isCurrent = true,
                description = "• Directed domestic and overseas raw material procurement with annual spend exceeding BDT 120 Crore.\n• Negotiated freight forwarding tariffs, saving 12% in international ocean transit expenses.\n• Supervised SAP ERP Materials Management (MM) workflows and warehouse cycle counting."
            ),
            CvExperienceItem(
                company = "Abul Khair Steel Products Ltd",
                role = "Procurement Executive",
                startDate = "Aug 2018",
                endDate = "Apr 2021",
                isCurrent = false,
                description = "• Processed commercial letters of credit (LC), HS code assessments, and customs duty assessments.\n• Monitored supplier OTIF (On-Time In-Full) performance benchmarks across 85 active vendors."
            )
        ),
        educations = listOf(
            CvEducationItem(
                degree = "MBA in Supply Chain & Operations Management",
                institution = "University of Chittagong",
                passingYear = "2018",
                result = "CGPA: 3.76 / 4.00"
            ),
            CvEducationItem(
                degree = "BBA in Management",
                institution = "University of Chittagong",
                passingYear = "2016",
                result = "CGPA: 3.68 / 4.00"
            )
        ),
        skills = listOf(
            CvSkillItem(name = "Strategic Sourcing & Vendor Management", level = "Expert"),
            CvSkillItem(name = "SAP ERP (MM / SD Modules)", level = "Expert"),
            CvSkillItem(name = "Customs Regulations & Chittagong Port Clearance", level = "Expert"),
            CvSkillItem(name = "Incoterms 2020 & International Shipping", level = "Expert"),
            CvSkillItem(name = "Inventory Optimization (EOQ & JIT)", level = "Proficient"),
            CvSkillItem(name = "Contract Negotiation & Cost Analysis", level = "Expert")
        ),
        languages = "English (Fluent), Bengali (Native)",
        certifications = "Certified Supply Chain Professional (CSCP) - APICS / ASCM, 2022\nDiploma in International Trade & Commercial Law, 2019",
        references = "Available upon request.",
        templateStyle = CvTemplateStyle.EUROPASS_GLOBAL_STANDARD
    )

    val academicDraft = CvData(
        id = "profile_academic_lecturer",
        profileLabel = "Academic Lecturer & Researcher (শিক্ষকতা)",
        fullName = "Dr. S. M. Ashraful Alam",
        jobTitle = "Assistant Professor & Academic Researcher",
        email = "ashraful.alam@univ.edu",
        phone = "+880 1716-114422",
        address = "Dhaka, Bangladesh",
        linkedin = "linkedin.com/in/dr-ashraful-alam",
        githubOrPortfolio = "researchgate.net/profile/Ashraful-Alam",
        summary = "Dedicated academician and scholar with 7+ years of higher education teaching and research expertise in Economics and Quantitative Methods. Published 9 peer-reviewed papers in Scopus-indexed journals and mentored over 40 undergraduate and postgraduate research dissertations.",
        experiences = listOf(
            CvExperienceItem(
                company = "Department of Economics, Premier University",
                role = "Assistant Professor",
                startDate = "Jan 2021",
                endDate = "Present",
                isCurrent = true,
                description = "• Delivered core undergraduate and graduate lectures on Econometrics, Macroeconomics, and Game Theory.\n• Supervised 25+ student thesis projects and managed departmental curriculum development committees.\n• Secured research grant of BDT 15 Lakh for urban socio-economic mobility assessment."
            ),
            CvExperienceItem(
                company = "University of Liberal Arts Bangladesh (ULAB)",
                role = "Lecturer in Economics",
                startDate = "Sep 2017",
                endDate = "Dec 2020",
                isCurrent = false,
                description = "• Conducted weekly tutorial sections and lab exercises utilizing Stata, R, and Python.\n• Organized international economics symposiums and served as faculty advisor for debate club."
            )
        ),
        educations = listOf(
            CvEducationItem(
                degree = "Ph.D. in Applied Economics",
                institution = "University of Malaya, Malaysia",
                passingYear = "2020",
                result = "Awarded with High Distinction"
            ),
            CvEducationItem(
                degree = "Master of Science in Economics",
                institution = "University of Dhaka",
                passingYear = "2016",
                result = "First Class First (CGPA: 3.94)"
            ),
            CvEducationItem(
                degree = "Bachelor of Science in Economics",
                institution = "University of Dhaka",
                passingYear = "2014",
                result = "First Class First (CGPA: 3.91)"
            )
        ),
        skills = listOf(
            CvSkillItem(name = "Econometric Modeling (Stata, R, EViews)", level = "Expert"),
            CvSkillItem(name = "Academic Curriculum Design & Pedagogy", level = "Expert"),
            CvSkillItem(name = "Peer-Reviewed Journal Publishing", level = "Expert"),
            CvSkillItem(name = "Grant Proposal Writing", level = "Expert"),
            CvSkillItem(name = "Statistical Data Analysis & Python", level = "Proficient"),
            CvSkillItem(name = "Public Speaking & Thesis Mentorship", level = "Expert")
        ),
        languages = "English (Native/Academic), Bengali (Native), Malay (Basic)",
        certifications = "Higher Education Teaching Certification - Cambridge, 2019\nScopus Indexed Journal Reviewer Recognition - Elsevier, 2022",
        references = "Prof. Dr. Wahiduddin Mahmud, Professor Emeritus, Department of Economics, DU",
        templateStyle = CvTemplateStyle.HARVARD_CLASSIC
    )

    val customerOpsDraft = CvData(
        id = "profile_customer_ops",
        profileLabel = "Customer Success & Operations (কাস্টমার অপারেশনস)",
        fullName = "Sadia Chowdhury",
        jobTitle = "Customer Success & Digital Operations Lead",
        email = "sadia.cs@example.com",
        phone = "+880 1817-665544",
        address = "Dhanmondi, Dhaka, Bangladesh",
        linkedin = "linkedin.com/in/sadia-success",
        summary = "Customer-centric Operations Manager with 4+ years of expertise in client retention, escalation workflows, CRM management (Zendesk, Salesforce), and service quality analytics. Proven track record of boosting CSAT score from 82% to 96% in a high-growth fintech environment.",
        experiences = listOf(
            CvExperienceItem(
                company = "Pathao Limited",
                role = "Customer Success Team Lead",
                startDate = "Jun 2022",
                endDate = "Present",
                isCurrent = true,
                description = "• Led a frontline tier-2 support team of 18 specialists delivering 24/7 client resolution.\n• Implemented AI-driven ticket triaging rules in Zendesk, lowering First Response Time (FRT) by 45%.\n• Collaborated with engineering teams to resolve recurring UX bugs affecting app user experience."
            ),
            CvExperienceItem(
                company = "Daraz Bangladesh (Alibaba Group)",
                role = "Senior Customer Experience Associate",
                startDate = "Feb 2020",
                endDate = "May 2022",
                isCurrent = false,
                description = "• Handled priority VIP merchant disputes, resolving 95% of cases within initial SLA.\n• Trained 40+ onboarding agents on soft skills, complaint de-escalation, and empathy guidelines."
            )
        ),
        educations = listOf(
            CvEducationItem(
                degree = "BBA in Management Information Systems (MIS)",
                institution = "East West University",
                passingYear = "2019",
                result = "CGPA: 3.72 / 4.00"
            )
        ),
        skills = listOf(
            CvSkillItem(name = "Customer Experience (CX) & CSAT Strategy", level = "Expert"),
            CvSkillItem(name = "CRM Platforms (Zendesk, Salesforce, Freshdesk)", level = "Expert"),
            CvSkillItem(name = "SLA Monitoring & Crisis De-escalation", level = "Expert"),
            CvSkillItem(name = "Voice of Customer (VoC) Analytics", level = "Proficient"),
            CvSkillItem(name = "Cross-functional Team Leadership", level = "Expert")
        ),
        languages = "English (Fluent), Bengali (Native)",
        certifications = "Certified Customer Success Manager (CCSM) Level 1, 2023\nAgile Customer Service Specialist, 2021",
        references = "Available upon request.",
        templateStyle = CvTemplateStyle.ELEGANT_PREMIUM
    )

    val cleanBlankDraft = CvData(
        id = "profile_clean_draft",
        profileLabel = "New Clean Profile (নতুন ফ্রেশ প্রোফাইল)",
        fullName = "Your Full Name",
        jobTitle = "Your Professional Job Title",
        email = "your.email@example.com",
        phone = "+880 1700-000000",
        address = "Dhaka, Bangladesh",
        linkedin = "linkedin.com/in/yourprofile",
        summary = "Write a clear, concise 3-4 line career summary highlighting your core strengths, years of relevant experience, and key professional achievements tailored to your target industry.",
        templateStyle = CvTemplateStyle.CLASSIC_CORPORATE
    )

    return listOf(
        defaultMba,
        adminHrDraft,
        bankFinanceDraft,
        marketingDraft,
        ngoProjectDraft,
        softwareDevDraft,
        supplyChainDraft,
        academicDraft,
        customerOpsDraft,
        cleanBlankDraft
    )
}

private fun saveAllCvProfiles(context: Context, profiles: List<CvData>) {
    try {
        val prefs = context.getSharedPreferences(CV_PREFS_NAME, Context.MODE_PRIVATE)
        val arr = JSONArray()
        profiles.forEach { profile ->
            val obj = JSONObject().apply {
                put("id", profile.id)
                put("profileLabel", profile.profileLabel)
                put("fullName", profile.fullName)
                put("jobTitle", profile.jobTitle)
                put("email", profile.email)
                put("phone", profile.phone)
                put("address", profile.address)
                put("linkedin", profile.linkedin)
                put("githubOrPortfolio", profile.githubOrPortfolio)
                put("summary", profile.summary)
                put("languages", profile.languages)
                put("templateStyle", profile.templateStyle.name)
                put("targetJobCircular", profile.targetJobCircular)
                put("photoBase64", profile.photoBase64)
                put("photoShape", profile.photoShape)
                put("photoScale", profile.photoScale.toDouble())
                put("photoOffsetX", profile.photoOffsetX.toDouble())
                put("photoOffsetY", profile.photoOffsetY.toDouble())
                
                // Corporate personal details
                put("fatherName", profile.fatherName)
                put("motherName", profile.motherName)
                put("religion", profile.religion)
                put("bloodGroup", profile.bloodGroup)
                put("permanentAddress", profile.permanentAddress)
                put("presentAddress", profile.presentAddress)
                put("certifications", profile.certifications)
                put("references", profile.references)

                // Fresher & Signature configurations
                put("isFresher", profile.isFresher)
                put("fresherAcademicProjects", profile.fresherAcademicProjects)
                put("fresherInternshipsVolunteer", profile.fresherInternshipsVolunteer)
                put("fresherLeadershipClubs", profile.fresherLeadershipClubs)
                put("fresherKeyCoursework", profile.fresherKeyCoursework)
                put("showSignatureLine", profile.showSignatureLine)

                val expArr = JSONArray()
                profile.experiences.forEach { exp ->
                    expArr.put(JSONObject().apply {
                        put("id", exp.id)
                        put("company", exp.company)
                        put("role", exp.role)
                        put("startDate", exp.startDate)
                        put("endDate", exp.endDate)
                        put("isCurrent", exp.isCurrent)
                        put("description", exp.description)
                        put("location", exp.location)
                    })
                }
                put("experiences", expArr)

                val eduArr = JSONArray()
                profile.educations.forEach { edu ->
                    eduArr.put(JSONObject().apply {
                        put("id", edu.id)
                        put("degree", edu.degree)
                        put("institution", edu.institution)
                        put("passingYear", edu.passingYear)
                        put("result", edu.result)
                        put("examLevel", edu.examLevel)
                        put("subjectMajor", edu.subjectMajor)
                        put("resultType", edu.resultType)
                    })
                }
                put("educations", eduArr)

                val skillArr = JSONArray()
                profile.skills.forEach { sk ->
                    skillArr.put(JSONObject().apply {
                        put("id", sk.id)
                        put("name", sk.name)
                        put("level", sk.level)
                        put("category", sk.category)
                        put("description", sk.description)
                    })
                }
                put("skills", skillArr)

                val projArr = JSONArray()
                profile.projects.forEach { pr ->
                    projArr.put(JSONObject().apply {
                        put("id", pr.id)
                        put("title", pr.title)
                        put("description", pr.description)
                        put("link", pr.link)
                    })
                }
                put("projects", projArr)

                val cSecArr = JSONArray()
                profile.customSections.forEach { cs ->
                    cSecArr.put(JSONObject().apply {
                        put("id", cs.id)
                        put("title", cs.title)
                        put("content", cs.content)
                        put("sectionType", cs.sectionType)
                    })
                }
                put("customSections", cSecArr)

                val sOrderArr = JSONArray()
                profile.sectionOrder.forEach { sOrderArr.put(it) }
                put("sectionOrder", sOrderArr)

                val hiddenArr = JSONArray()
                profile.hiddenSections.forEach { hiddenArr.put(it) }
                put("hiddenSections", hiddenArr)

                put("showContactIcons", profile.showContactIcons)
                put("showSectionIcons", profile.showSectionIcons)
                put("fontScale", profile.fontScale)
                put("bulletStyle", profile.bulletStyle)
                put("customMargin", profile.customMargin.toDouble())
                put("sectionSpacing", profile.sectionSpacing.toDouble())
                put("itemSpacing", profile.itemSpacing.toDouble())
                put("customLineSpacing", profile.customLineSpacing.toDouble())
            }
            arr.put(obj)
        }
        prefs.edit().putString(CV_PROFILES_LIST_KEY, arr.toString()).apply()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun loadAllCvProfiles(context: Context): List<CvData> {
    try {
        val prefs = context.getSharedPreferences(CV_PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(CV_PROFILES_LIST_KEY, null)
        if (raw.isNullOrBlank()) {
            return emptyList()
        }
        val arr = JSONArray(raw)
        val list = mutableListOf<CvData>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val expList = mutableListOf<CvExperienceItem>()
            obj.optJSONArray("experiences")?.let { expArr ->
                for (j in 0 until expArr.length()) {
                    val expObj = expArr.getJSONObject(j)
                    expList.add(
                        CvExperienceItem(
                            id = expObj.optString("id", UUID.randomUUID().toString()),
                            company = expObj.optString("company"),
                            role = expObj.optString("role"),
                            startDate = expObj.optString("startDate"),
                            endDate = expObj.optString("endDate"),
                            isCurrent = expObj.optBoolean("isCurrent"),
                            description = expObj.optString("description"),
                            location = expObj.optString("location", "Dhaka, Bangladesh")
                        )
                    )
                }
            }

            val eduList = mutableListOf<CvEducationItem>()
            obj.optJSONArray("educations")?.let { eduArr ->
                for (j in 0 until eduArr.length()) {
                    val eduObj = eduArr.getJSONObject(j)
                    eduList.add(
                        CvEducationItem(
                            id = eduObj.optString("id", UUID.randomUUID().toString()),
                            degree = eduObj.optString("degree"),
                            institution = eduObj.optString("institution"),
                            passingYear = eduObj.optString("passingYear"),
                            result = eduObj.optString("result"),
                            examLevel = eduObj.optString("examLevel"),
                            subjectMajor = eduObj.optString("subjectMajor"),
                            resultType = eduObj.optString("resultType")
                        )
                    )
                }
            }

            val skillList = mutableListOf<CvSkillItem>()
            obj.optJSONArray("skills")?.let { skArr ->
                for (j in 0 until skArr.length()) {
                    val skObj = skArr.getJSONObject(j)
                    skillList.add(
                        CvSkillItem(
                            id = skObj.optString("id", UUID.randomUUID().toString()),
                            name = skObj.optString("name"),
                            level = skObj.optString("level", "Proficient"),
                            category = skObj.optString("category", "Functional/Core Skills"),
                            description = skObj.optString("description", "")
                        )
                    )
                }
            }

            val projList = mutableListOf<CvProjectItem>()
            obj.optJSONArray("projects")?.let { prArr ->
                for (j in 0 until prArr.length()) {
                    val prObj = prArr.getJSONObject(j)
                    projList.add(
                        CvProjectItem(
                            id = prObj.optString("id", UUID.randomUUID().toString()),
                            title = prObj.optString("title"),
                            description = prObj.optString("description"),
                            link = prObj.optString("link")
                        )
                    )
                }
            }

            val cSecList = mutableListOf<CvCustomSectionItem>()
            obj.optJSONArray("customSections")?.let { cSecArr ->
                for (j in 0 until cSecArr.length()) {
                    val cSecObj = cSecArr.getJSONObject(j)
                    cSecList.add(
                        CvCustomSectionItem(
                            id = cSecObj.optString("id", UUID.randomUUID().toString()),
                            title = cSecObj.optString("title", ""),
                            content = cSecObj.optString("content", ""),
                            sectionType = cSecObj.optString("sectionType", "EXPERIENCE")
                        )
                    )
                }
            }

            val sOrderList = mutableListOf<String>()
            obj.optJSONArray("sectionOrder")?.let { sOrderArr ->
                for (j in 0 until sOrderArr.length()) {
                    sOrderList.add(sOrderArr.getString(j))
                }
            }

            val hiddenList = mutableListOf<String>()
            obj.optJSONArray("hiddenSections")?.let { hiddenArr ->
                for (j in 0 until hiddenArr.length()) {
                    hiddenList.add(hiddenArr.getString(j))
                }
            }

            val styleStr = obj.optString("templateStyle", CvTemplateStyle.CLASSIC_CORPORATE.name)
            val style = try { CvTemplateStyle.valueOf(styleStr) } catch (_: Exception) { CvTemplateStyle.CLASSIC_CORPORATE }

            list.add(
                CvData(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    profileLabel = obj.optString("profileLabel", "Unnamed Resume"),
                    fullName = obj.optString("fullName", "Md. Shariful Islam"),
                    jobTitle = obj.optString("jobTitle", "Management Graduate & Business Analyst"),
                    email = obj.optString("email", "shariful.mba@example.com"),
                    phone = obj.optString("phone", "+880 1711-223344"),
                    address = obj.optString("address", "Dhaka, Bangladesh"),
                    linkedin = obj.optString("linkedin", "linkedin.com/in/shariful-mba"),
                    githubOrPortfolio = obj.optString("githubOrPortfolio", "portfolio.shariful.com"),
                    summary = obj.optString("summary", ""),
                    experiences = expList,
                    educations = eduList,
                    skills = skillList,
                    projects = projList,
                    languages = obj.optString("languages", "English (Fluent), Bengali (Native)"),
                    templateStyle = style,
                    targetJobCircular = obj.optString("targetJobCircular", ""),
                    photoBase64 = obj.optString("photoBase64", ""),
                    photoShape = obj.optString("photoShape", "Circle"),
                    photoScale = obj.optDouble("photoScale", 1.0).toFloat(),
                    photoOffsetX = obj.optDouble("photoOffsetX", 0.0).toFloat(),
                    photoOffsetY = obj.optDouble("photoOffsetY", 0.0).toFloat(),
                    fatherName = obj.optString("fatherName", "Md. Nazrul Islam"),
                    motherName = obj.optString("motherName", "Mrs. Sufia Begum"),
                    religion = obj.optString("religion", "Islam"),
                    bloodGroup = obj.optString("bloodGroup", "B+"),
                    permanentAddress = obj.optString("permanentAddress", "House 12, Road 4, Sector 1, Uttara, Dhaka"),
                    presentAddress = obj.optString("presentAddress", "House 12, Road 4, Sector 1, Uttara, Dhaka"),
                    certifications = obj.optString("certifications", "Project Management Professional (PMP) - PMI, 2024\nBusiness Intelligence Certification - Google / Coursera, 2023"),
                    references = obj.optString("references", "Available upon request."),
                    isFresher = obj.optBoolean("isFresher", false),
                    fresherAcademicProjects = obj.optString("fresherAcademicProjects", ""),
                    fresherInternshipsVolunteer = obj.optString("fresherInternshipsVolunteer", ""),
                    fresherLeadershipClubs = obj.optString("fresherLeadershipClubs", ""),
                    fresherKeyCoursework = obj.optString("fresherKeyCoursework", ""),
                    showSignatureLine = obj.optBoolean("showSignatureLine", true),
                    customSections = cSecList,
                    sectionOrder = if (sOrderList.isNotEmpty()) sOrderList else listOf("SUMMARY", "EXPERIENCE", "EDUCATION", "SKILLS", "PROJECTS", "CERTIFICATIONS", "LANGUAGES", "CUSTOM_SECTIONS", "PERSONAL_INFO", "REFERENCES"),
                    hiddenSections = hiddenList,
                    showContactIcons = obj.optBoolean("showContactIcons", true),
                    showSectionIcons = obj.optBoolean("showSectionIcons", false),
                    fontScale = obj.optString("fontScale", "STANDARD"),
                    bulletStyle = obj.optString("bulletStyle", "BULLET"),
                    customMargin = obj.optDouble("customMargin", 36.0).toFloat(),
                    sectionSpacing = obj.optDouble("sectionSpacing", 8.0).toFloat(),
                    itemSpacing = obj.optDouble("itemSpacing", 4.0).toFloat(),
                    customLineSpacing = obj.optDouble("customLineSpacing", 1.15).toFloat()
                )
            )
        }
        return list
    } catch (e: Exception) {
        e.printStackTrace()
        return getSeedProfilesList()
    }
}

private fun loadActiveProfileId(context: Context): String {
    val prefs = context.getSharedPreferences(CV_PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getString(ACTIVE_PROFILE_ID_KEY, "") ?: ""
}

private fun saveActiveProfileId(context: Context, id: String) {
    val prefs = context.getSharedPreferences(CV_PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(ACTIVE_PROFILE_ID_KEY, id).apply()
}

// ================= GEMINI AI MULTI-MODAL CALL =================

private suspend fun callGeminiAiMultiModal(
    prompt: String,
    systemInstruction: String,
    imageBytes: ByteArray? = null,
    mimeType: String = "image/jpeg"
): String = withContext(Dispatchers.IO) {
    val apiKey = com.example.BuildConfig.GEMINI_API_KEY
    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
        throw IllegalStateException("API Key is missing or not configured in Secrets panel.")
    }

    val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
    val conn = url.openConnection() as HttpURLConnection
    conn.requestMethod = "POST"
    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
    conn.doOutput = true
    conn.connectTimeout = 20000
    conn.readTimeout = 25000

    val requestJson = JSONObject().apply {
        val contentsArr = JSONArray()
        contentsArr.put(JSONObject().apply {
            val partsArr = JSONArray()
            partsArr.put(JSONObject().put("text", prompt))

            if (imageBytes != null) {
                val base64Str = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                partsArr.put(JSONObject().apply {
                    put("inline_data", JSONObject().apply {
                        put("mime_type", mimeType)
                        put("data", base64Str)
                    })
                })
            }
            put("parts", partsArr)
        })
        put("contents", contentsArr)

        if (systemInstruction.isNotBlank()) {
            put("system_instruction", JSONObject().apply {
                val sysParts = JSONArray()
                sysParts.put(JSONObject().put("text", systemInstruction))
                put("parts", sysParts)
            })
        }
    }

    conn.outputStream.use { os ->
        os.write(requestJson.toString().toByteArray(Charsets.UTF_8))
    }

    val responseCode = conn.responseCode
    if (responseCode == 200) {
        val responseStr = conn.inputStream.bufferedReader().use { it.readText() }
        val resObj = JSONObject(responseStr)
        val candidates = resObj.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val cand = candidates.getJSONObject(0)
            val content = cand.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                return@withContext parts.getJSONObject(0).optString("text", "")
            }
        }
        throw IllegalStateException("No contents generated from AI model.")
    } else {
        val errStr = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
        throw IllegalStateException("Gemini AI API Error ($responseCode): $errStr")
    }
}

// ================= DELIVERABLE 2 & 3: GEMINI API INTEGRATIONS & FILE NAMING =================

object CvFileNameUtility {
    fun generateFileName(fullName: String, designationOrYear: String): String {
        val cleanName = fullName.trim()
            .replace(Regex("[^a-zA-Z0-9\\s]"), "")
            .replace(Regex("\\s+"), "_")
        val cleanYear = designationOrYear.trim()
            .replace(Regex("[^a-zA-Z0-9\\s]"), "")
            .replace(Regex("\\s+"), "_")
        val result = "CV_${cleanName}_${cleanYear}.pdf"
        return result.replace(Regex("_+"), "_")
    }
}

private suspend fun generateContentWithGemini(degree: String, targetRole: String): Pair<String, List<String>> = withContext(Dispatchers.IO) {
    val systemPrompt = "You are an expert HR Tech and ATS optimization architect specializing in corporate standard recruitment templates. " +
            "Return ONLY a valid JSON object with keys 'objective' (exactly 3-line impact-driven corporate career objective) and 'skills' (exactly 6 impact-driven corporate skills as a string array, using action verbs)."
    val prompt = "Generate ATS-optimized career objective and skills list for degree: '$degree' and target job role: '$targetRole' using modern corporate standards."
    
    try {
        val response = callGeminiAiMultiModal(prompt, systemPrompt)
        val cleanJson = response.substringAfter("{").substringBeforeLast("}").let { "{$it}" }
        val obj = JSONObject(cleanJson)
        val objective = obj.optString("objective", "Results-oriented graduate eager to contribute strategic skills to a dynamic organization.")
        val skillsArr = obj.optJSONArray("skills")
        val skillsList = mutableListOf<String>()
        if (skillsArr != null) {
            for (i in 0 until skillsArr.length()) {
                skillsList.add(skillsArr.getString(i))
            }
        } else {
            skillsList.addAll(listOf("Strategic Planning", "Project Management", "Data Analytics", "Cross-Functional Leadership", "Financial Modeling", "Corporate Strategy"))
        }
        Pair(objective, skillsList)
    } catch (e: Exception) {
        e.printStackTrace()
        Pair(
            "Ambitious $degree graduate aiming to leverage technical and management capabilities in a $targetRole role to improve organizational excellence.",
            listOf("Strategic Planning", "Project Management", "Data Analytics", "Cross-Functional Leadership", "Financial Modeling", "Corporate Strategy")
        )
    }
}

private suspend fun sanitizeInputWithGemini(inputJsonStr: String): String = withContext(Dispatchers.IO) {
    val systemPrompt = "You are a senior professional HR copywriter and ATS system auditor. " +
            "Clean the provided candidate data JSON for spelling, capitalization, and grammar. " +
            "Fix common typos like 'PERSONAL INFORMATIONS' to 'PERSONAL INFORMATION', 'Powerpoint' to 'PowerPoint', " +
            "and convert informal phrases to professional business tone. " +
            "Keep the exact same JSON key names and schema structure. Return ONLY the sanitized JSON string."
    try {
        val result = callGeminiAiMultiModal(inputJsonStr, systemPrompt)
        result.substringAfter("{").substringBeforeLast("}").let { "{$it}" }
    } catch (e: Exception) {
        e.printStackTrace()
        inputJsonStr
    }
}

private suspend fun tailorCvWithGemini(cvJsonStr: String, jobDescription: String): String = withContext(Dispatchers.IO) {
    val systemPrompt = "You are a Principal ATS optimization software engineer. Compare the candidate's current CV data JSON " +
            "against the provided Job Description text. Inject the top missing ATS keywords organically into the objective " +
            "and skills sections of the JSON WITHOUT fabricating fake experience or changing other credentials. " +
            "Return ONLY the updated CV data JSON maintaining the identical JSON keys and schema structure."
    val prompt = "CV Data JSON:\n$cvJsonStr\n\nTarget Job Description:\n$jobDescription"
    try {
        val result = callGeminiAiMultiModal(prompt, systemPrompt)
        result.substringAfter("{").substringBeforeLast("}").let { "{$it}" }
    } catch (e: Exception) {
        e.printStackTrace()
        cvJsonStr
    }
}

private suspend fun generateFresherCvSectionsWithGemini(
    degree: String,
    institution: String,
    targetRole: String,
    skills: String
): Map<String, String> = withContext(Dispatchers.IO) {
    val systemPrompt = "You are a professional university career coach and top ATS resume consultant. " +
            "Given a fresh graduate's degree, target job role, institution, and core skills, generate 4 high-impact resume sections for freshers: " +
            "1. academicProjects (2-3 bulleted projects with technologies, methodology, and quantifiable outcome), " +
            "2. internshipsVolunteer (1-2 bulleted entries showing initiative, leadership, event management or volunteerism), " +
            "3. leadershipClubs (active club roles, case competitions, debate, or campus organizing), " +
            "4. keyCoursework (concise list of high-value courses/core subjects related to the degree and target role). " +
            "Output strictly valid JSON with keys: 'academicProjects', 'internshipsVolunteer', 'leadershipClubs', 'keyCoursework'."

    val prompt = "Degree: $degree\nInstitution: $institution\nTarget Role: $targetRole\nSkills: $skills"
    try {
        val jsonStr = callGeminiAiMultiModal(prompt, systemPrompt)
        val cleanJson = jsonStr.substringAfter("{").substringBeforeLast("}").let { "{$it}" }
        val obj = JSONObject(cleanJson)
        mapOf(
            "academicProjects" to obj.optString("academicProjects", ""),
            "internshipsVolunteer" to obj.optString("internshipsVolunteer", ""),
            "leadershipClubs" to obj.optString("leadershipClubs", ""),
            "keyCoursework" to obj.optString("keyCoursework", "")
        )
    } catch (e: Exception) {
        e.printStackTrace()
        emptyMap()
    }
}

// ================= DYNAMIC HIGH-QUALITY PDF GENERATOR =================

// Helper to draw authentic Material Design filled vector icons onto Canvas
private fun drawMaterialVectorIcon(
    canvas: Canvas,
    iconType: String,
    x: Float,
    y: Float,
    size: Float,
    color: Int
) {
    val paintFill = Paint().apply {
        this.color = color
        this.style = Paint.Style.FILL
        this.isAntiAlias = true
    }
    val paintWhiteStroke = Paint().apply {
        this.color = AndroidColor.WHITE
        this.strokeWidth = maxOf(1f, size * 0.1f)
        this.style = Paint.Style.STROKE
        this.isAntiAlias = true
    }
    val paintWhiteFill = Paint().apply {
        this.color = AndroidColor.WHITE
        this.style = Paint.Style.FILL
        this.isAntiAlias = true
    }

    when (iconType.lowercase()) {
        "phone" -> {
            // Filled Material Phone
            val path = android.graphics.Path().apply {
                moveTo(x + size * 0.22f, y + size * 0.15f)
                cubicTo(x + size * 0.35f, y + size * 0.1f, x + size * 0.5f, y + size * 0.25f, x + size * 0.45f, y + size * 0.4f)
                lineTo(x + size * 0.38f, y + size * 0.47f)
                cubicTo(x + size * 0.45f, y + size * 0.6f, x + size * 0.6f, y + size * 0.75f, x + size * 0.73f, y + size * 0.82f)
                lineTo(x + size * 0.8f, y + size * 0.75f)
                cubicTo(x + size * 0.95f, y + size * 0.7f, x + size * 1.1f, y + size * 0.85f, x + size * 1.05f, y + size * 1.0f)
                cubicTo(x + size * 1.0f, y + size * 1.15f, x + size * 0.7f, y + size * 1.15f, x + size * 0.55f, y + size * 1.05f)
                cubicTo(x + size * 0.3f, y + size * 0.85f, x + size * 0.15f, y + size * 0.7f, x + size * 0.05f, y + size * 0.45f)
                cubicTo(x - size * 0.05f, y + size * 0.3f, x + size * 0.05f, y + size * 0.05f, x + size * 0.22f, y + size * 0.15f)
                close()
            }
            canvas.drawPath(path, paintFill)
        }
        "email" -> {
            // Filled Envelope with crisp white flap indicator
            val rect = android.graphics.RectF(x, y + size * 0.18f, x + size, y + size * 0.82f)
            canvas.drawRoundRect(rect, 2f, 2f, paintFill)
            val flap = android.graphics.Path().apply {
                moveTo(x + 1f, y + size * 0.22f)
                lineTo(x + size / 2f, y + size * 0.54f)
                lineTo(x + size - 1f, y + size * 0.22f)
            }
            canvas.drawPath(flap, paintWhiteStroke)
        }
        "location" -> {
            // Filled Map Pin with white center dot
            val pinHeadRadius = size * 0.32f
            val cx = x + size / 2f
            val cy = y + size * 0.35f
            val path = android.graphics.Path().apply {
                arcTo(android.graphics.RectF(cx - pinHeadRadius, cy - pinHeadRadius, cx + pinHeadRadius, cy + pinHeadRadius), 180f, 180f, false)
                cubicTo(cx + pinHeadRadius, cy + size * 0.3f, cx + size * 0.15f, cy + size * 0.55f, cx, y + size * 0.95f)
                cubicTo(cx - size * 0.15f, cy + size * 0.55f, cx - pinHeadRadius, cy + size * 0.3f, cx - pinHeadRadius, cy)
                close()
            }
            canvas.drawPath(path, paintFill)
            canvas.drawCircle(cx, cy, pinHeadRadius * 0.42f, paintWhiteFill)
        }
        "linkedin" -> {
            // Filled LinkedIn Rounded Badge with 'in'
            val rect = android.graphics.RectF(x, y + size * 0.05f, x + size, y + size * 0.95f)
            canvas.drawRoundRect(rect, 2f, 2f, paintFill)
            val textPaintIn = Paint().apply {
                this.color = AndroidColor.WHITE
                this.textSize = size * 0.62f
                this.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                this.isAntiAlias = true
            }
            canvas.drawText("in", x + size * 0.2f, y + size * 0.72f, textPaintIn)
        }
        "portfolio", "globe" -> {
            // Filled Globe Badge with white lat/long lines
            val cx = x + size / 2f
            val cy = y + size / 2f
            val r = size * 0.46f
            canvas.drawCircle(cx, cy, r, paintFill)
            canvas.drawLine(x + size * 0.12f, cy, x + size * 0.88f, cy, paintWhiteStroke)
            canvas.drawLine(cx, y + size * 0.12f, cx, y + size * 0.88f, paintWhiteStroke)
            val oval = android.graphics.RectF(cx - r * 0.5f, y + size * 0.12f, cx + r * 0.5f, y + size * 0.88f)
            canvas.drawOval(oval, paintWhiteStroke)
        }
        "github" -> {
            // Filled Circle with white code brackets < / >
            val cx = x + size / 2f
            val cy = y + size / 2f
            val r = size * 0.46f
            canvas.drawCircle(cx, cy, r, paintFill)
            val path = android.graphics.Path().apply {
                moveTo(cx - size * 0.16f, cy - size * 0.2f)
                lineTo(cx - size * 0.32f, cy)
                lineTo(cx - size * 0.16f, cy + size * 0.2f)
                moveTo(cx + size * 0.16f, cy - size * 0.2f)
                lineTo(cx + size * 0.32f, cy)
                lineTo(cx + size * 0.16f, cy + size * 0.2f)
            }
            canvas.drawPath(path, paintWhiteStroke)
        }
        "calendar" -> {
            // Filled Calendar
            val rect = android.graphics.RectF(x, y + size * 0.2f, x + size, y + size * 0.9f)
            canvas.drawRoundRect(rect, 2f, 2f, paintFill)
            canvas.drawLine(x + 1f, y + size * 0.42f, x + size - 1f, y + size * 0.42f, paintWhiteStroke)
            val hangerPaint = Paint().apply {
                this.color = color
                this.style = Paint.Style.FILL
                this.isAntiAlias = true
            }
            canvas.drawRoundRect(android.graphics.RectF(x + size * 0.22f, y + size * 0.08f, x + size * 0.34f, y + size * 0.28f), 1f, 1f, hangerPaint)
            canvas.drawRoundRect(android.graphics.RectF(x + size * 0.66f, y + size * 0.08f, x + size * 0.78f, y + size * 0.28f), 1f, 1f, hangerPaint)
        }
        "education", "graduation" -> {
            // Filled Graduation Cap
            val cx = x + size / 2f
            val capPath = android.graphics.Path().apply {
                moveTo(cx, y + size * 0.15f)
                lineTo(x + size * 0.96f, y + size * 0.42f)
                lineTo(cx, y + size * 0.65f)
                lineTo(x + size * 0.04f, y + size * 0.42f)
                close()
            }
            canvas.drawPath(capPath, paintFill)
            val capBase = android.graphics.Path().apply {
                moveTo(x + size * 0.25f, y + size * 0.55f)
                quadTo(cx, y + size * 0.88f, x + size * 0.75f, y + size * 0.55f)
                lineTo(x + size * 0.75f, y + size * 0.68f)
                quadTo(cx, y + size * 0.98f, x + size * 0.25f, y + size * 0.68f)
                close()
            }
            canvas.drawPath(capBase, paintFill)
        }
        "work", "briefcase" -> {
            // Filled Briefcase
            val rect = android.graphics.RectF(x + size * 0.05f, y + size * 0.3f, x + size * 0.95f, y + size * 0.88f)
            canvas.drawRoundRect(rect, 2.5f, 2.5f, paintFill)
            val handle = android.graphics.Path().apply {
                moveTo(x + size * 0.32f, y + size * 0.3f)
                lineTo(x + size * 0.32f, y + size * 0.15f)
                lineTo(x + size * 0.68f, y + size * 0.15f)
                lineTo(x + size * 0.68f, y + size * 0.3f)
            }
            val handlePaint = Paint().apply {
                this.color = color
                this.strokeWidth = 1.6f
                this.style = Paint.Style.STROKE
                this.isAntiAlias = true
            }
            canvas.drawPath(handle, handlePaint)
            canvas.drawLine(x + size * 0.05f, y + size * 0.54f, x + size * 0.95f, y + size * 0.54f, paintWhiteStroke)
        }
        "skill", "star" -> {
            // Filled 5-point Star
            val cx = x + size / 2f
            val cy = y + size / 2f
            val rOuter = size * 0.48f
            val rInner = size * 0.21f
            val path = android.graphics.Path()
            for (i in 0 until 5) {
                val angleOuter = (Math.PI / 2.5 * i - Math.PI / 2).toFloat()
                val angleInner = (angleOuter + Math.PI / 5).toFloat()
                val ox = cx + rOuter * Math.cos(angleOuter.toDouble()).toFloat()
                val oy = cy + rOuter * Math.sin(angleOuter.toDouble()).toFloat()
                val ix = cx + rInner * Math.cos(angleInner.toDouble()).toFloat()
                val iy = cy + rInner * Math.sin(angleInner.toDouble()).toFloat()
                if (i == 0) path.moveTo(ox, oy) else path.lineTo(ox, oy)
                path.lineTo(ix, iy)
            }
            path.close()
            canvas.drawPath(path, paintFill)
        }
        else -> {
            canvas.drawCircle(x + size / 2f, y + size / 2f, size * 0.4f, paintFill)
        }
    }
}

private fun generateCvPdfFile(context: Context, data: CvData): File {
    val pdfDocument = PdfDocument()

    // Standard A4: 595 x 842 pt
    val pageWidth = 595
    val pageHeight = 842

    var currentPageNumber = 1
    var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
    var page = pdfDocument.startPage(pageInfo)
    var canvas = page.canvas

    val margin = data.customMargin
    val sectionGap = data.sectionSpacing
    val entryGap = data.itemSpacing
    val contentWidth = pageWidth - (margin * 2)
    var currentY = margin

    val pdfStyle = data.templateStyle
    val primaryColor = pdfStyle.primaryColorHex
    val textColor = AndroidColor.parseColor("#0F172A")
    val subTextColor = AndroidColor.parseColor("#334155")
    val mutedLineColor = AndroidColor.parseColor("#CBD5E1")

    val isSerif = pdfStyle == CvTemplateStyle.ELEGANT_PREMIUM || pdfStyle == CvTemplateStyle.HARVARD_CLASSIC
    val tfTitle = if (isSerif) CleanPdfTypefaces.serifBold else CleanPdfTypefaces.sansMedium
    val tfRegular = if (isSerif) CleanPdfTypefaces.serifRegular else CleanPdfTypefaces.sansRegular
    val tfBold = if (isSerif) CleanPdfTypefaces.serifBold else CleanPdfTypefaces.sansBold

    val titlePaint = TextPaint().apply {
        isAntiAlias = true
        color = primaryColor
        textSize = 19f
        typeface = tfTitle
    }

    val subtitlePaint = TextPaint().apply {
        isAntiAlias = true
        color = subTextColor
        textSize = 11f
        typeface = tfRegular
    }

    val contactPaint = TextPaint().apply {
        isAntiAlias = true
        color = subTextColor
        textSize = 9.2f
        typeface = tfRegular
    }

    val sectionHeaderPaint = TextPaint().apply {
        isAntiAlias = true
        color = primaryColor
        textSize = 11.5f
        typeface = tfTitle
    }

    val bodyPaint = TextPaint().apply {
        isAntiAlias = true
        color = textColor
        textSize = 9.2f
        typeface = tfRegular
    }

    val bodyBoldPaint = TextPaint().apply {
        isAntiAlias = true
        color = textColor
        textSize = 9.2f
        typeface = tfBold
    }

    fun checkAndAddNewPage(neededHeight: Float) {
        if (currentY + neededHeight > pageHeight - margin) {
            pdfDocument.finishPage(page)
            currentPageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            currentY = margin
        }
    }

    // Profile Photo Decode
    val photoBitmap = if (data.photoBase64.isNotBlank()) {
        try {
            val decodedBytes = Base64.decode(data.photoBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (_: Exception) {
            null
        }
    } else {
        null
    }

    fun drawPhotoAt(px: Float, py: Float, size: Float, shape: String, borderColor: Int = primaryColor) {
        if (photoBitmap == null) return
        canvas.save()
        val path = android.graphics.Path()
        when (shape) {
            "Circle" -> {
                path.addCircle(px + size / 2f, py + size / 2f, size / 2f, android.graphics.Path.Direction.CW)
                canvas.clipPath(path)
            }
            "Rounded" -> {
                val rect = android.graphics.RectF(px, py, px + size, py + size)
                path.addRoundRect(rect, 6f, 6f, android.graphics.Path.Direction.CW)
                canvas.clipPath(path)
            }
            else -> {
                val rect = android.graphics.RectF(px, py, px + size, py + size)
                path.addRect(rect, android.graphics.Path.Direction.CW)
                canvas.clipPath(path)
            }
        }

        canvas.save()
        val clipBox = android.graphics.Path()
        clipBox.addRect(android.graphics.RectF(px, py, px + size, py + size), android.graphics.Path.Direction.CW)
        canvas.clipPath(clipBox)

        val cx = px + size / 2f
        val cy = py + size / 2f
        canvas.translate(cx, cy)
        canvas.scale(data.photoScale, data.photoScale)
        canvas.translate(data.photoOffsetX / 3.2f, data.photoOffsetY / 3.2f)
        canvas.translate(-cx, -cy)

        val dstRect = android.graphics.RectF(px, py, px + size, py + size)
        canvas.drawBitmap(photoBitmap, null, dstRect, Paint(Paint.FILTER_BITMAP_FLAG))
        canvas.restore()
        canvas.restore()

        val borderPaint = Paint().apply {
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = 3.5f
            isAntiAlias = true
        }
        when (shape) {
            "Circle" -> canvas.drawCircle(px + size / 2f, py + size / 2f, size / 2f, borderPaint)
            "Rounded" -> canvas.drawRoundRect(android.graphics.RectF(px, py, px + size, py + size), 6f, 6f, borderPaint)
            else -> canvas.drawRect(android.graphics.RectF(px, py, px + size, py + size), borderPaint)
        }
    }

    // ================= BRANCH 1: EXECUTIVE TWO-COLUMN SIDEBAR =================
    if (pdfStyle == CvTemplateStyle.EXECUTIVE_TWO_COLUMN) {
        val sidebarWidth = 185f
        val rightMargin = 20f
        val mainColX = sidebarWidth + rightMargin
        val mainColWidth = pageWidth - mainColX - margin

        // Draw Left Sidebar Background
        val sideBgPaint = Paint().apply {
            color = primaryColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, sidebarWidth, pageHeight.toFloat(), sideBgPaint)

        val sideTextPaint = TextPaint().apply {
            isAntiAlias = true
            color = AndroidColor.WHITE
            textSize = 8.8f
            typeface = CleanPdfTypefaces.sansRegular
        }
        val sideTitlePaint = TextPaint().apply {
            isAntiAlias = true
            color = AndroidColor.WHITE
            textSize = 10.5f
            typeface = CleanPdfTypefaces.sansMedium
        }

        var sideY = margin
        if (photoBitmap != null) {
            val pSize = 75f
            val px = (sidebarWidth - pSize) / 2f
            drawPhotoAt(px, sideY, pSize, data.photoShape, AndroidColor.WHITE)
            sideY += pSize + 16f
        }

        fun drawSideHeader(title: String) {
            canvas.drawText(title.uppercase(), margin / 2f + 4f, sideY + 10f, sideTitlePaint)
            sideY += 14f
            canvas.drawLine(margin / 2f + 4f, sideY, sidebarWidth - 16f, sideY, Paint().apply {
                color = AndroidColor.parseColor("#64748B")
                strokeWidth = 1f
            })
            sideY += 8f
        }

        // CONTACT
        drawSideHeader("CONTACT")
        val iconSize = 9f
        fun drawSideContactItem(text: String, iconType: String) {
            if (text.isBlank()) return
            val iy = sideY + 1f
            drawMaterialVectorIcon(canvas, iconType, margin / 2f + 4f, iy, iconSize, AndroidColor.parseColor("#94A3B8"))
            val layout = StaticLayout.Builder.obtain(text, 0, text.length, sideTextPaint, (sidebarWidth - margin - 20f).toInt()).setLineSpacing(0f, data.customLineSpacing).build()
            canvas.save()
            canvas.translate(margin / 2f + 4f + iconSize + 6f, sideY)
            layout.draw(canvas)
            canvas.restore()
            sideY += layout.height + 6f
        }

        drawSideContactItem(data.phone, "phone")
        drawSideContactItem(data.email, "email")
        drawSideContactItem(data.address, "location")
        if (data.linkedin.isNotBlank()) drawSideContactItem(data.linkedin.removePrefix("https://").removePrefix("www."), "linkedin")
        if (data.githubOrPortfolio.isNotBlank()) drawSideContactItem(data.githubOrPortfolio.removePrefix("https://").removePrefix("www."), "portfolio")

        // SKILLS in Sidebar
        if (data.skills.isNotEmpty()) {
            sideY += 8f
            drawSideHeader("KEY SKILLS")
            data.skills.forEach { sk ->
                val fullText = if (sk.description.isNotBlank()) "${sk.name}: ${sk.description}" else sk.name
                val bulletText = "• $fullText"
                val spannable = SpannableStringBuilder(bulletText)
                val colonIndex = bulletText.indexOf(':')
                if (colonIndex > 0) {
                    spannable.setSpan(StyleSpan(Typeface.BOLD), 0, colonIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                val sLayout = StaticLayout.Builder.obtain(spannable, 0, spannable.length, sideTextPaint, (sidebarWidth - margin - 10f).toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                canvas.save()
                canvas.translate(margin / 2f + 4f, sideY)
                sLayout.draw(canvas)
                canvas.restore()
                sideY += sLayout.height + 4f
            }
        }

        // LANGUAGES in Sidebar
        if (data.languages.isNotBlank()) {
            sideY += 8f
            drawSideHeader("LANGUAGES")
            val lLayout = StaticLayout.Builder.obtain(data.languages, 0, data.languages.length, sideTextPaint, (sidebarWidth - margin - 10f).toInt()).setLineSpacing(0f, data.customLineSpacing).build()
            canvas.save()
            canvas.translate(margin / 2f + 4f, sideY)
            lLayout.draw(canvas)
            canvas.restore()
            sideY += lLayout.height + 6f
        }

        // PERSONAL DETAILS in Sidebar
        val sidePersonal = mutableListOf<String>()
        if (data.bloodGroup.isNotBlank()) sidePersonal.add("Blood Group: ${data.bloodGroup}")
        if (data.religion.isNotBlank()) sidePersonal.add("Religion: ${data.religion}")
        if (data.fatherName.isNotBlank()) sidePersonal.add("Father: ${data.fatherName}")
        if (data.motherName.isNotBlank()) sidePersonal.add("Mother: ${data.motherName}")
        if (sidePersonal.isNotEmpty()) {
            sideY += 8f
            drawSideHeader("PERSONAL")
            sidePersonal.forEach { p ->
                val pLayout = StaticLayout.Builder.obtain(p, 0, p.length, sideTextPaint, (sidebarWidth - margin - 10f).toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                canvas.save()
                canvas.translate(margin / 2f + 4f, sideY)
                pLayout.draw(canvas)
                canvas.restore()
                sideY += pLayout.height + 4f
            }
        }

        // RIGHT COLUMN: Main Content
        var rightY = margin
        if (data.fullName.isNotBlank()) {
            val nameLayout = StaticLayout.Builder.obtain(data.fullName.uppercase(), 0, data.fullName.length, titlePaint, mainColWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
            canvas.save()
            canvas.translate(mainColX, rightY)
            nameLayout.draw(canvas)
            canvas.restore()
            rightY += nameLayout.height + 2f
        }
        if (data.jobTitle.isNotBlank()) {
            val titleLayout = StaticLayout.Builder.obtain(data.jobTitle, 0, data.jobTitle.length, subtitlePaint, mainColWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
            canvas.save()
            canvas.translate(mainColX, rightY)
            titleLayout.draw(canvas)
            canvas.restore()
            rightY += titleLayout.height + 10f
        }

        fun checkAndAddNewPageTwoColumn(neededHeight: Float) {
            if (rightY + neededHeight > pageHeight - margin) {
                pdfDocument.finishPage(page)
                currentPageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                // Redraw solid left sidebar on new page
                canvas.drawRect(0f, 0f, sidebarWidth, pageHeight.toFloat(), sideBgPaint)
                rightY = margin
            }
        }

        fun drawMainSectionHeader(title: String) {
            checkAndAddNewPageTwoColumn(32f)
            canvas.drawText(title.uppercase(), mainColX, rightY + 10f, sectionHeaderPaint)
            rightY += 14f
            canvas.drawLine(mainColX, rightY, pageWidth - margin, rightY, Paint().apply {
                color = mutedLineColor
                strokeWidth = 1.2f
            })
            rightY += 8f
        }

        if (data.summary.isNotBlank()) {
            val sumLayout = StaticLayout.Builder.obtain(data.summary, 0, data.summary.length, bodyPaint, mainColWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
            checkAndAddNewPageTwoColumn(sumLayout.height.toFloat() + 34f)
            drawMainSectionHeader("EXECUTIVE SUMMARY")
            canvas.save()
            canvas.translate(mainColX, rightY)
            sumLayout.draw(canvas)
            canvas.restore()
            rightY += sumLayout.height + 12f
        }

        if (data.experiences.isNotEmpty()) {
            data.experiences.forEachIndexed { idx, exp ->
                if (exp.role.isNotBlank() || exp.company.isNotBlank()) {
                    if (idx == 0) {
                        drawMainSectionHeader("EXPERIENCE")
                    }
                    val head = "${exp.role} — ${exp.company}"
                    val date = "${exp.startDate}${if (exp.startDate.isNotBlank() && exp.endDate.isNotBlank()) " – " else ""}${if (exp.isCurrent) "Present" else exp.endDate}"
                    val headLayout = StaticLayout.Builder.obtain(head, 0, head.length, bodyBoldPaint, (mainColWidth * 0.72f).toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                    val dateLayout = StaticLayout.Builder.obtain(date, 0, date.length, contactPaint, (mainColWidth * 0.28f).toInt()).setAlignment(Layout.Alignment.ALIGN_OPPOSITE).setLineSpacing(0f, data.customLineSpacing).build()

                    val h = maxOf(headLayout.height, dateLayout.height).toFloat()
                    checkAndAddNewPageTwoColumn(h + 8f)

                    canvas.save()
                    canvas.translate(mainColX, rightY)
                    headLayout.draw(canvas)
                    canvas.restore()
                    canvas.save()
                    canvas.translate(mainColX + mainColWidth * 0.72f, rightY)
                    dateLayout.draw(canvas)
                    canvas.restore()
                    rightY += h + 2f

                    if (exp.description.isNotBlank()) {
                        val lines = exp.description.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                        lines.forEach { line ->
                            val bulletLine = if (line.startsWith("•") || line.startsWith("-") || line.startsWith("▪") || line.startsWith("◆")) line else "• $line"
                            val dLayout = StaticLayout.Builder.obtain(bulletLine, 0, bulletLine.length, bodyPaint, (mainColWidth - 8f).toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                            checkAndAddNewPageTwoColumn(dLayout.height.toFloat() + 4f)
                            canvas.save()
                            canvas.translate(mainColX + 8f, rightY)
                            dLayout.draw(canvas)
                            canvas.restore()
                            rightY += dLayout.height + 3f
                        }
                        rightY += 4f
                    } else {
                        rightY += 6f
                    }
                }
            }
        }

        if (data.educations.isNotEmpty()) {
            data.educations.forEachIndexed { idx, edu ->
                if (idx == 0) {
                    drawMainSectionHeader("EDUCATION")
                }
                val degreePart = getFormattedDegreeText(edu)
                val eduTitle = if (degreePart.isNotBlank() && edu.institution.isNotBlank()) "$degreePart — ${edu.institution}" else degreePart.ifBlank { edu.institution }
                
                val resultText = if (edu.resultType.isNotBlank() && edu.result.isNotBlank()) "${edu.resultType}: ${edu.result}" else edu.result
                val eduSub = "Year: ${edu.passingYear}${if (edu.passingYear.isNotBlank() && resultText.isNotBlank()) "  |  " else ""}$resultText"
                val tLayout = StaticLayout.Builder.obtain(eduTitle, 0, eduTitle.length, bodyBoldPaint, mainColWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                val sLayout = StaticLayout.Builder.obtain(eduSub, 0, eduSub.length, contactPaint, mainColWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                
                checkAndAddNewPageTwoColumn(tLayout.height + sLayout.height + 12f)

                canvas.save()
                canvas.translate(mainColX, rightY)
                tLayout.draw(canvas)
                canvas.restore()
                rightY += tLayout.height + 2f

                canvas.save()
                canvas.translate(mainColX, rightY)
                sLayout.draw(canvas)
                canvas.restore()
                rightY += sLayout.height + 6f
            }
        }

        if (data.certifications.isNotBlank()) {
            val cLayout = StaticLayout.Builder.obtain(data.certifications, 0, data.certifications.length, bodyPaint, mainColWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
            checkAndAddNewPageTwoColumn(cLayout.height.toFloat() + 34f)
            drawMainSectionHeader("CERTIFICATIONS")
            canvas.save()
            canvas.translate(mainColX, rightY)
            cLayout.draw(canvas)
            canvas.restore()
            rightY += cLayout.height + 10f
        }

        if (data.references.isNotBlank()) {
            val rLayout = StaticLayout.Builder.obtain(data.references, 0, data.references.length, bodyPaint, mainColWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
            checkAndAddNewPageTwoColumn(rLayout.height.toFloat() + 34f)
            drawMainSectionHeader("REFERENCES")
            canvas.save()
            canvas.translate(mainColX, rightY)
            rLayout.draw(canvas)
            canvas.restore()
        }

        pdfDocument.finishPage(page)
        val passingYearStr = if (data.educations.isNotEmpty()) data.educations.first().passingYear else "2026"
        val pdfFileName = CvFileNameUtility.generateFileName(data.fullName, passingYearStr)
        val file = File(context.cacheDir, pdfFileName)
        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        pdfDocument.close()
        fos.close()
        return file
    }

    // ================= BRANCH 2: CREATIVE MARKETING BANNER =================
    if (pdfStyle == CvTemplateStyle.CREATIVE_MARKETING) {
        val bannerHeight = 120f
        val bannerPaint = Paint().apply {
            color = AndroidColor.parseColor("#0F172A")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), bannerHeight, bannerPaint)

        val accentPaint = Paint().apply {
            color = primaryColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, bannerHeight, pageWidth.toFloat(), bannerHeight + 4f, accentPaint)

        var bannerY = 22f
        val bannerTextWidth = if (photoBitmap != null) contentWidth - 75f else contentWidth

        val bannerTitlePaint = TextPaint().apply {
            isAntiAlias = true
            color = AndroidColor.WHITE
            textSize = 19f
            typeface = CleanPdfTypefaces.sansMedium
        }
        val bannerSubPaint = TextPaint().apply {
            isAntiAlias = true
            color = AndroidColor.parseColor("#F1F5F9")
            textSize = 11f
            typeface = CleanPdfTypefaces.sansRegular
        }
        val bannerContactPaint = TextPaint().apply {
            isAntiAlias = true
            color = AndroidColor.parseColor("#CBD5E1")
            textSize = 8.8f
            typeface = CleanPdfTypefaces.sansRegular
        }

        if (data.fullName.isNotBlank()) {
            val nameLayout = StaticLayout.Builder.obtain(data.fullName.uppercase(), 0, data.fullName.length, bannerTitlePaint, bannerTextWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
            canvas.save()
            canvas.translate(margin, bannerY)
            nameLayout.draw(canvas)
            canvas.restore()
            bannerY += nameLayout.height + 2f
        }
        if (data.jobTitle.isNotBlank()) {
            val subLayout = StaticLayout.Builder.obtain(data.jobTitle, 0, data.jobTitle.length, bannerSubPaint, bannerTextWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
            canvas.save()
            canvas.translate(margin, bannerY)
            subLayout.draw(canvas)
            canvas.restore()
            bannerY += subLayout.height + 4f
        }

        val contactLine = listOfNotNull(
            data.phone.takeIf { it.isNotBlank() },
            data.email.takeIf { it.isNotBlank() },
            data.address.takeIf { it.isNotBlank() },
            data.linkedin.takeIf { it.isNotBlank() }
        ).joinToString("  •  ")
        if (contactLine.isNotBlank()) {
            val cLayout = StaticLayout.Builder.obtain(contactLine, 0, contactLine.length, bannerContactPaint, bannerTextWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
            canvas.save()
            canvas.translate(margin, bannerY)
            cLayout.draw(canvas)
            canvas.restore()
        }

        if (photoBitmap != null) {
            val pSize = 68f
            val px = pageWidth - margin - pSize
            drawPhotoAt(px, 24f, pSize, "Circle", AndroidColor.WHITE)
        }

        currentY = bannerHeight + 18f
    }

    // ================= BRANCH 3: HARVARD CLASSIC ACADEMIC =================
    if (pdfStyle == CvTemplateStyle.HARVARD_CLASSIC) {
        var headY = margin
        if (data.fullName.isNotBlank()) {
            val hTitlePaint = TextPaint().apply {
                isAntiAlias = true
                color = AndroidColor.BLACK
                textSize = 21f
                typeface = CleanPdfTypefaces.serifBold
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(data.fullName.uppercase(), pageWidth / 2f, headY + 18f, hTitlePaint)
            headY += 24f
        }
        if (data.jobTitle.isNotBlank()) {
            val hSubPaint = TextPaint().apply {
                isAntiAlias = true
                color = AndroidColor.parseColor("#374151")
                textSize = 11f
                typeface = CleanPdfTypefaces.serifRegular
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(data.jobTitle, pageWidth / 2f, headY + 10f, hSubPaint)
            headY += 16f
        }

        val contactLine = listOfNotNull(
            data.address.takeIf { it.isNotBlank() },
            data.phone.takeIf { it.isNotBlank() },
            data.email.takeIf { it.isNotBlank() },
            data.linkedin.takeIf { it.isNotBlank() }?.removePrefix("https://")?.removePrefix("www.")
        ).joinToString("  •  ")

        if (contactLine.isNotBlank()) {
            val hContactPaint = TextPaint().apply {
                isAntiAlias = true
                color = AndroidColor.parseColor("#4B5563")
                textSize = 9.2f
                typeface = CleanPdfTypefaces.serifRegular
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(contactLine, pageWidth / 2f, headY + 8f, hContactPaint)
            headY += 18f
        }
        currentY = headY + 6f
    } else if (pdfStyle == CvTemplateStyle.BANKING_FINANCE_SPECIALIST) {
        // ================= BRANCH 4: BANKING & FINANCE SPECIALIST =================
        val boxHeight = 72f
        val boxRect = android.graphics.RectF(margin, currentY, pageWidth - margin, currentY + boxHeight)
        val boxFillPaint = Paint().apply {
            color = AndroidColor.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }
        val boxStrokePaint = Paint().apply {
            color = primaryColor
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
        }
        canvas.drawRoundRect(boxRect, 4f, 4f, boxFillPaint)
        canvas.drawRoundRect(boxRect, 4f, 4f, boxStrokePaint)

        val halfW = (contentWidth - 24f) / 2f
        var by = currentY + 16f
        if (data.fullName.isNotBlank()) {
            canvas.drawText(data.fullName.uppercase(), margin + 12f, by + 4f, titlePaint)
            by += 16f
        }
        if (data.jobTitle.isNotBlank()) {
            canvas.drawText(data.jobTitle, margin + 12f, by + 4f, subtitlePaint)
        }

        var rby = currentY + 14f
        val rPaint = TextPaint().apply {
            isAntiAlias = true
            color = subTextColor
            textSize = 8.8f
            typeface = CleanPdfTypefaces.sansRegular
        }
        if (data.phone.isNotBlank()) {
            drawMaterialVectorIcon(canvas, "phone", margin + halfW + 12f, rby - 8f, 8.5f, primaryColor)
            canvas.drawText(data.phone, margin + halfW + 24f, rby, rPaint)
            rby += 13f
        }
        if (data.email.isNotBlank()) {
            drawMaterialVectorIcon(canvas, "email", margin + halfW + 12f, rby - 8f, 8.5f, primaryColor)
            canvas.drawText(data.email, margin + halfW + 24f, rby, rPaint)
            rby += 13f
        }
        if (data.address.isNotBlank()) {
            drawMaterialVectorIcon(canvas, "location", margin + halfW + 12f, rby - 8f, 8.5f, primaryColor)
            canvas.drawText(data.address, margin + halfW + 24f, rby, rPaint)
        }

        currentY += boxHeight + 14f
    } else if (pdfStyle == CvTemplateStyle.NGO_DEVELOPMENT_HUMANITARIAN) {
        // ================= BRANCH 5: NGO & HUMANITARIAN =================
        val barPaint = Paint().apply {
            color = primaryColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(margin, currentY, margin + 4f, currentY + 54f, barPaint)

        var hy = currentY + 4f
        if (data.fullName.isNotBlank()) {
            canvas.drawText(data.fullName.uppercase(), margin + 14f, hy + 14f, titlePaint)
            hy += 20f
        }
        if (data.jobTitle.isNotBlank()) {
            canvas.drawText(data.jobTitle, margin + 14f, hy + 10f, subtitlePaint)
            hy += 16f
        }

        val contactLine = listOfNotNull(
            data.phone.takeIf { it.isNotBlank() }?.let { "Tel: $it" },
            data.email.takeIf { it.isNotBlank() }?.let { "Email: $it" },
            data.address.takeIf { it.isNotBlank() }?.let { "Base: $it" }
        ).joinToString("  |  ")
        if (contactLine.isNotBlank()) {
            canvas.drawText(contactLine, margin + 14f, hy + 8f, contactPaint)
        }

        if (photoBitmap != null) {
            val pSize = 60f
            val px = pageWidth - margin - pSize
            drawPhotoAt(px, currentY, pSize, "Rounded", primaryColor)
        }

        currentY += 66f
    } else if (pdfStyle == CvTemplateStyle.CANVA_MINIMALIST_CLEAN) {
        // ================= BRANCH 6: CANVA MINIMALIST CLEAN (CENTERED MODERN) =================
        var headY = currentY
        if (photoBitmap != null) {
            val pSize = 58f
            drawPhotoAt((pageWidth - pSize) / 2f, headY, pSize, "Circle", primaryColor)
            headY += pSize + 10f
        }

        if (data.fullName.isNotBlank()) {
            val canvaTitlePaint = TextPaint().apply {
                isAntiAlias = true
                color = primaryColor
                textSize = 21f
                typeface = CleanPdfTypefaces.sansMedium
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(data.fullName.uppercase(), pageWidth / 2f, headY + 16f, canvaTitlePaint)
            headY += 22f
        }

        if (data.jobTitle.isNotBlank()) {
            val canvaSubPaint = TextPaint().apply {
                isAntiAlias = true
                color = subTextColor
                textSize = 10.5f
                typeface = CleanPdfTypefaces.sansRegular
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(data.jobTitle, pageWidth / 2f, headY + 10f, canvaSubPaint)
            headY += 16f
        }

        val contactLine = listOfNotNull(
            data.phone.takeIf { it.isNotBlank() },
            data.email.takeIf { it.isNotBlank() },
            data.address.takeIf { it.isNotBlank() },
            data.linkedin.takeIf { it.isNotBlank() }?.removePrefix("https://")?.removePrefix("www.")
        ).joinToString("   •   ")

        if (contactLine.isNotBlank()) {
            val cPaint = TextPaint().apply {
                isAntiAlias = true
                color = subTextColor
                textSize = 8.8f
                typeface = CleanPdfTypefaces.sansRegular
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(contactLine, pageWidth / 2f, headY + 8f, cPaint)
            headY += 16f
        }

        val dividerPaint = Paint().apply {
            color = AndroidColor.parseColor("#E2E8F0")
            strokeWidth = 1.2f
        }
        canvas.drawLine(margin + 20f, headY + 4f, pageWidth - margin - 20f, headY + 4f, dividerPaint)
        currentY = headY + 14f
    } else if (pdfStyle == CvTemplateStyle.SILICON_VALLEY_TECH_LEAD) {
        // ================= BRANCH 7: SILICON VALLEY TECH LEAD =================
        val techAccentPaint = Paint().apply {
            color = primaryColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(margin, currentY, margin + 4.5f, currentY + 54f, techAccentPaint)

        var ty = currentY + 4f
        val tTitlePaint = TextPaint().apply {
            isAntiAlias = true
            color = AndroidColor.parseColor("#0F172A")
            textSize = 20f
            typeface = CleanPdfTypefaces.sansMedium
        }
        if (data.fullName.isNotBlank()) {
            canvas.drawText(data.fullName.uppercase(), margin + 14f, ty + 15f, tTitlePaint)
            ty += 21f
        }
        if (data.jobTitle.isNotBlank()) {
            canvas.drawText(data.jobTitle, margin + 14f, ty + 10f, subtitlePaint)
            ty += 15f
        }

        val contactLine = listOfNotNull(
            data.email.takeIf { it.isNotBlank() },
            data.phone.takeIf { it.isNotBlank() },
            data.githubOrPortfolio.takeIf { it.isNotBlank() }?.let { "gh: ${it.removePrefix("https://").removePrefix("www.")}" },
            data.linkedin.takeIf { it.isNotBlank() }?.let { "in: ${it.removePrefix("https://").removePrefix("www.")}" }
        ).joinToString("  |  ")
        if (contactLine.isNotBlank()) {
            val tcPaint = TextPaint().apply {
                isAntiAlias = true
                color = AndroidColor.parseColor("#475569")
                textSize = 8.6f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            }
            canvas.drawText(contactLine, margin + 14f, ty + 8f, tcPaint)
        }

        if (photoBitmap != null) {
            val pSize = 56f
            val px = pageWidth - margin - pSize
            drawPhotoAt(px, currentY, pSize, "Rounded", primaryColor)
        }

        currentY += 66f
    } else if (pdfStyle == CvTemplateStyle.EXECUTIVE_MONOCHROME_LUXE) {
        // ================= BRANCH 8: EXECUTIVE MONOCHROME LUXE =================
        var ey = currentY
        if (data.fullName.isNotBlank()) {
            val exeTitlePaint = TextPaint().apply {
                isAntiAlias = true
                color = AndroidColor.BLACK
                textSize = 22f
                typeface = CleanPdfTypefaces.serifBold
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(data.fullName.uppercase(), pageWidth / 2f, ey + 18f, exeTitlePaint)
            ey += 24f
        }
        if (data.jobTitle.isNotBlank()) {
            val exeSubPaint = TextPaint().apply {
                isAntiAlias = true
                color = AndroidColor.parseColor("#262626")
                textSize = 10.5f
                typeface = CleanPdfTypefaces.serifRegular
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(data.jobTitle, pageWidth / 2f, ey + 10f, exeSubPaint)
            ey += 16f
        }

        val contactLine = listOfNotNull(
            data.address.takeIf { it.isNotBlank() },
            data.phone.takeIf { it.isNotBlank() },
            data.email.takeIf { it.isNotBlank() },
            data.linkedin.takeIf { it.isNotBlank() }?.removePrefix("https://")?.removePrefix("www.")
        ).joinToString("  ❖  ")
        if (contactLine.isNotBlank()) {
            val exeCPaint = TextPaint().apply {
                isAntiAlias = true
                color = AndroidColor.parseColor("#404040")
                textSize = 8.8f
                typeface = CleanPdfTypefaces.serifRegular
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(contactLine, pageWidth / 2f, ey + 8f, exeCPaint)
            ey += 16f
        }

        val rulePaint = Paint().apply {
            color = AndroidColor.BLACK
            strokeWidth = 1.4f
        }
        canvas.drawLine(margin, ey + 2f, pageWidth - margin, ey + 2f, rulePaint)
        currentY = ey + 12f
    } else if (pdfStyle != CvTemplateStyle.CREATIVE_MARKETING) {
        // ================= BRANCH 9: STANDARD HEADER (CLASSIC_CORPORATE, SINGLE_COLUMN_HIGH_IMPACT_ATS, NORDIC_SLATE_MODERN, CLEAN_TECH_STARTUP, MODERN_MINIMALIST, ELEGANT_PREMIUM) =================
        val photoSize = 65f
        val photoMargin = 14f
        val hasTopPhoto = photoBitmap != null
        val headerTextWidth = if (hasTopPhoto) (contentWidth - photoSize - photoMargin) else contentWidth

        // Draw Profile Photo on Top Right
        if (hasTopPhoto) {
            val px = pageWidth - margin - photoSize
            drawPhotoAt(px, currentY, photoSize, data.photoShape, primaryColor)
        }

        // Full Name (Bold Uppercase)
        if (data.fullName.isNotBlank()) {
            val nameLayout = StaticLayout.Builder.obtain(data.fullName.uppercase(), 0, data.fullName.length, titlePaint, headerTextWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
            canvas.save()
            canvas.translate(margin, currentY)
            nameLayout.draw(canvas)
            canvas.restore()
            currentY += nameLayout.height + 2f
        }

        // Designation / Job Title
        if (data.jobTitle.isNotBlank()) {
            val subLayout = StaticLayout.Builder.obtain(data.jobTitle, 0, data.jobTitle.length, subtitlePaint, headerTextWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
            canvas.save()
            canvas.translate(margin, currentY)
            subLayout.draw(canvas)
            canvas.restore()
            currentY += subLayout.height + 6f
        }

        // Contact Lines formatted cleanly with pipes (|) as requested
        val contactLines = mutableListOf<String>()
        val phoneEmailLine = listOfNotNull(
            data.phone.takeIf { it.isNotBlank() }?.let { "Phone: $it" },
            data.email.takeIf { it.isNotBlank() }?.let { "Email: $it" }
        ).joinToString(" | ")
        if (phoneEmailLine.isNotBlank()) contactLines.add(phoneEmailLine)

        val locLnPortLine = mutableListOf<String>()
        if (data.address.isNotBlank()) locLnPortLine.add("Location: ${data.address}")
        if (data.linkedin.isNotBlank()) {
            val cleanLn = data.linkedin.removePrefix("https://").removePrefix("www.")
            locLnPortLine.add("Linkedin: $cleanLn")
        }
        if (data.githubOrPortfolio.isNotBlank()) {
            val cleanPort = data.githubOrPortfolio.removePrefix("https://").removePrefix("www.")
            locLnPortLine.add("Portfolio: $cleanPort")
        }
        if (locLnPortLine.isNotEmpty()) {
            val secondLine = locLnPortLine.joinToString(" | ")
            contactLines.add(secondLine)
        }

        contactLines.forEach { line ->
            val cLayout = StaticLayout.Builder.obtain(line, 0, line.length, contactPaint, headerTextWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
            canvas.save()
            canvas.translate(margin, currentY)
            cLayout.draw(canvas)
            canvas.restore()
            currentY += cLayout.height + 3f
        }

        currentY = maxOf(currentY + 4f, if (hasTopPhoto) margin + photoSize + 10f else currentY)
    }

    val fontScaleMultiplier = when (data.fontScale) {
        "COMPACT" -> 0.88f
        "COMFORTABLE" -> 1.12f
        "LARGE" -> 1.25f
        else -> 1.0f
    }

    // --- Section Header Renderer Custom to Template Style ---
    var isFirstSection = true
    fun drawSectionHeader(title: String, defaultIconKey: String = "") {
        if (!isFirstSection) currentY += sectionGap
        isFirstSection = false
        checkAndAddNewPage(28f)
        val headerText = if (data.showSectionIcons && defaultIconKey.isNotBlank()) "$defaultIconKey $title" else title
        when (pdfStyle) {
            CvTemplateStyle.HARVARD_CLASSIC -> {
                val hPaint = TextPaint().apply {
                    isAntiAlias = true
                    color = AndroidColor.BLACK
                    textSize = 11.5f * fontScaleMultiplier
                    typeface = Typeface.create("serif", Typeface.BOLD)
                }
                canvas.drawText(headerText.uppercase(), margin, currentY + 11f, hPaint)
                currentY += 15f
                val rulePaint = Paint().apply {
                    color = AndroidColor.BLACK
                    strokeWidth = 1f
                }
                canvas.drawLine(margin, currentY, pageWidth - margin, currentY, rulePaint)
                currentY += 8f
            }
            CvTemplateStyle.EXECUTIVE_MONOCHROME_LUXE -> {
                val exePaint = TextPaint().apply {
                    isAntiAlias = true
                    color = AndroidColor.BLACK
                    textSize = 11.5f * fontScaleMultiplier
                    typeface = Typeface.create("serif", Typeface.BOLD)
                }
                canvas.drawText("❖  ${headerText.uppercase()}", margin, currentY + 11f, exePaint)
                currentY += 15f
                val rulePaint = Paint().apply {
                    color = AndroidColor.BLACK
                    strokeWidth = 1f
                }
                canvas.drawLine(margin, currentY, pageWidth - margin, currentY, rulePaint)
                currentY += 8f
            }
            CvTemplateStyle.CANVA_MINIMALIST_CLEAN -> {
                val cPaint = TextPaint().apply {
                    isAntiAlias = true
                    color = primaryColor
                    textSize = 11.5f * fontScaleMultiplier
                    typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
                }
                canvas.drawText(headerText.uppercase(), margin, currentY + 11f, cPaint)
                currentY += 15f
                val rulePaint = Paint().apply {
                    color = AndroidColor.parseColor("#E2E8F0")
                    strokeWidth = 1f
                }
                canvas.drawLine(margin, currentY, pageWidth - margin, currentY, rulePaint)
                currentY += 8f
            }
            CvTemplateStyle.SINGLE_COLUMN_HIGH_IMPACT_ATS -> {
                val atsPaint = TextPaint().apply {
                    isAntiAlias = true
                    color = primaryColor
                    textSize = 11.5f * fontScaleMultiplier
                    typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
                }
                canvas.drawText(headerText.uppercase(), margin, currentY + 11f, atsPaint)
                currentY += 15f
                val rulePaint = Paint().apply {
                    color = primaryColor
                    strokeWidth = 1.4f
                }
                canvas.drawLine(margin, currentY, pageWidth - margin, currentY, rulePaint)
                currentY += 8f
            }
            CvTemplateStyle.NORDIC_SLATE_MODERN -> {
                val slatePaint = TextPaint().apply {
                    isAntiAlias = true
                    color = primaryColor
                    textSize = 11.5f * fontScaleMultiplier
                    typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
                }
                canvas.drawText(headerText.uppercase(), margin, currentY + 11f, slatePaint)
                currentY += 15f
                val rulePaint = Paint().apply {
                    color = AndroidColor.parseColor("#94A3B8")
                    strokeWidth = 0.9f
                }
                canvas.drawLine(margin, currentY, pageWidth - margin, currentY, rulePaint)
                currentY += 8f
            }
            CvTemplateStyle.MODERN_MINIMALIST -> {
                val barPaint = Paint().apply {
                    color = primaryColor
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(android.graphics.RectF(margin, currentY + 1f, margin + 3.5f, currentY + 13f), 2f, 2f, barPaint)
                canvas.drawText(headerText.uppercase(), margin + 9f, currentY + 11f, sectionHeaderPaint)
                currentY += 15f
                val rulePaint = Paint().apply {
                    color = AndroidColor.parseColor("#E2E8F0")
                    strokeWidth = 0.8f
                }
                canvas.drawLine(margin, currentY, pageWidth - margin, currentY, rulePaint)
                currentY += 8f
            }
            CvTemplateStyle.ELEGANT_PREMIUM -> {
                val dPaint = TextPaint().apply {
                    isAntiAlias = true
                    color = primaryColor
                    textSize = 11.5f * fontScaleMultiplier
                    typeface = Typeface.create("serif", Typeface.BOLD)
                }
                canvas.drawText("◆  ${headerText.uppercase()}", margin, currentY + 11f, dPaint)
                currentY += 15f
                val rulePaint = Paint().apply {
                    color = primaryColor
                    strokeWidth = 1.2f
                }
                canvas.drawLine(margin, currentY, pageWidth - margin, currentY, rulePaint)
                currentY += 8f
            }
            CvTemplateStyle.CREATIVE_MARKETING -> {
                val pillRect = android.graphics.RectF(margin, currentY, margin + 6f, currentY + 14f)
                val pillPaint = Paint().apply {
                    color = primaryColor
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(pillRect, 3f, 3f, pillPaint)
                canvas.drawText(headerText.uppercase(), margin + 12f, currentY + 11f, sectionHeaderPaint)
                currentY += 16f
                val rulePaint = Paint().apply {
                    color = AndroidColor.parseColor("#FECDD3")
                    strokeWidth = 1f
                }
                canvas.drawLine(margin, currentY, pageWidth - margin, currentY, rulePaint)
                currentY += 8f
            }
            CvTemplateStyle.CLEAN_TECH_STARTUP, CvTemplateStyle.SILICON_VALLEY_TECH_LEAD -> {
                val codePrefixPaint = TextPaint().apply {
                    isAntiAlias = true
                    color = primaryColor
                    textSize = 11.5f * fontScaleMultiplier
                    typeface = Typeface.create("monospace", Typeface.BOLD)
                }
                canvas.drawText("// ${headerText.uppercase()}", margin, currentY + 11f, codePrefixPaint)
                currentY += 15f
                val rulePaint = Paint().apply {
                    color = primaryColor
                    strokeWidth = 1f
                }
                canvas.drawLine(margin, currentY, pageWidth - margin, currentY, rulePaint)
                currentY += 8f
            }
            else -> {
                val formattedHeader = if (headerText.endsWith(":")) headerText else "$headerText:"
                canvas.drawText(formattedHeader.uppercase(), margin, currentY + 11f, sectionHeaderPaint)
                currentY += 15f
                val rulePaint = Paint().apply {
                    color = if (pdfStyle == CvTemplateStyle.CLASSIC_CORPORATE) AndroidColor.BLACK else primaryColor
                    strokeWidth = 1f
                }
                canvas.drawLine(margin, currentY, pageWidth - margin, currentY, rulePaint)
                currentY += 8f
            }
        }
    }

    // --- DYNAMIC SECTION ORDERING & SHOW/HIDE RENDERER ---
    val effectiveSectionOrder = data.sectionOrder.ifEmpty {
        listOf("SUMMARY", "EXPERIENCE", "EDUCATION", "SKILLS", "PROJECTS", "CERTIFICATIONS", "LANGUAGES", "CUSTOM_SECTIONS", "PERSONAL_INFO", "REFERENCES")
    }

    val bulletPrefix = when (data.bulletStyle) {
        "DASH" -> "- "
        "SQUARE" -> "▪ "
        "DIAMOND" -> "◆ "
        "COMMA" -> ""
        "PIPE" -> ""
        "NONE" -> ""
        else -> "• "
    }

    effectiveSectionOrder.forEach { secKey ->
        if (data.hiddenSections.contains(secKey)) return@forEach

        when (secKey) {
            "SUMMARY" -> {
                if (data.summary.isNotBlank()) {
                    val objTitle = if (pdfStyle == CvTemplateStyle.NGO_DEVELOPMENT_HUMANITARIAN) "MISSION & SOCIAL IMPACT OBJECTIVE" else if (pdfStyle == CvTemplateStyle.HARVARD_CLASSIC) "PROFESSIONAL SUMMARY" else "CAREER OBJECTIVE"
                    drawSectionHeader(objTitle, "🎯")

                    if (pdfStyle == CvTemplateStyle.NGO_DEVELOPMENT_HUMANITARIAN) {
                        val sumLayout = StaticLayout.Builder.obtain(data.summary, 0, data.summary.length, bodyPaint, (contentWidth - 20f).toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                        val boxH = sumLayout.height + 14f
                        checkAndAddNewPage(boxH + 6f)
                        val bgPaint = Paint().apply {
                            color = AndroidColor.parseColor("#FFF7ED")
                            style = Paint.Style.FILL
                        }
                        val borderP = Paint().apply {
                            color = AndroidColor.parseColor("#FED7AA")
                            style = Paint.Style.STROKE
                            strokeWidth = 1f
                        }
                        val r = android.graphics.RectF(margin, currentY, margin + contentWidth, currentY + boxH)
                        canvas.drawRoundRect(r, 4f, 4f, bgPaint)
                        canvas.drawRoundRect(r, 4f, 4f, borderP)
                        canvas.save()
                        canvas.translate(margin + 10f, currentY + 7f)
                        sumLayout.draw(canvas)
                        canvas.restore()
                        currentY += boxH + entryGap
                    } else {
                        val sumLayout = StaticLayout.Builder.obtain(data.summary, 0, data.summary.length, bodyPaint, contentWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                        checkAndAddNewPage(sumLayout.height.toFloat() + 4f)
                        canvas.save()
                        canvas.translate(margin, currentY)
                        sumLayout.draw(canvas)
                        canvas.restore()
                        currentY += sumLayout.height + entryGap
                    }
                }
            }

            "EDUCATION" -> {
                if (data.educations.isNotEmpty()) {
                    drawSectionHeader("EDUCATION", "🎓")

                    val isAtsTextLayout = pdfStyle == CvTemplateStyle.HARVARD_CLASSIC ||
                                          pdfStyle == CvTemplateStyle.CANVA_MINIMALIST_CLEAN ||
                                          pdfStyle == CvTemplateStyle.SINGLE_COLUMN_HIGH_IMPACT_ATS ||
                                          pdfStyle == CvTemplateStyle.SILICON_VALLEY_TECH_LEAD ||
                                          pdfStyle == CvTemplateStyle.NORDIC_SLATE_MODERN ||
                                          pdfStyle == CvTemplateStyle.MODERN_MINIMALIST ||
                                          pdfStyle == CvTemplateStyle.CLEAN_TECH_STARTUP ||
                                          pdfStyle == CvTemplateStyle.ELEGANT_PREMIUM

                    if (isAtsTextLayout) {
                        val sortedEdu = data.educations.sortedByDescending { it.passingYear.toIntOrNull() ?: 0 }
                        sortedEdu.forEach { edu ->
                            val degreeText = getFormattedDegreeText(edu)
                            val instText = "${edu.institution}${if (edu.result.isNotBlank()) "  •  ${edu.result}" else ""}"
                            val yearText = edu.passingYear

                            val degLayout = StaticLayout.Builder.obtain(degreeText, 0, degreeText.length, bodyBoldPaint, (contentWidth * 0.75f).toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                            val yearLayout = StaticLayout.Builder.obtain(yearText, 0, yearText.length, bodyBoldPaint, (contentWidth * 0.25f).toInt()).setAlignment(Layout.Alignment.ALIGN_OPPOSITE).setLineSpacing(0f, data.customLineSpacing).build()

                            val h = maxOf(degLayout.height, yearLayout.height).toFloat()
                            checkAndAddNewPage(h + 16f)

                            canvas.save(); canvas.translate(margin, currentY); degLayout.draw(canvas); canvas.restore()
                            canvas.save(); canvas.translate(margin + contentWidth * 0.75f, currentY); yearLayout.draw(canvas); canvas.restore()
                            currentY += h + 2f

                            val instLayout = StaticLayout.Builder.obtain(instText, 0, instText.length, bodyPaint, contentWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                            canvas.save(); canvas.translate(margin, currentY); instLayout.draw(canvas); canvas.restore()
                            currentY += instLayout.height + entryGap
                        }
                        currentY += 2f
                    } else {
                        val colExamW = 60f
                        val colInstW = 185f
                        val colSubW = 110f
                        val colResW = 110f
                        val colYearW = contentWidth - (colExamW + colInstW + colSubW + colResW)

                        val tableHeaderPaint = TextPaint().apply {
                            isAntiAlias = true
                            color = AndroidColor.parseColor("#0F172A")
                            textSize = 8.5f * fontScaleMultiplier
                            typeface = tfBold
                        }
                        val tableCellPaint = TextPaint().apply {
                            isAntiAlias = true
                            color = textColor
                            textSize = 8.2f * fontScaleMultiplier
                            typeface = tfRegular
                        }
                        val tableBorderPaint = Paint().apply {
                            color = mutedLineColor
                            style = Paint.Style.STROKE
                            strokeWidth = 0.8f
                        }
                        val headerBgColor = when (pdfStyle) {
                            CvTemplateStyle.BANKING_FINANCE_SPECIALIST -> AndroidColor.parseColor("#DBEAFE")
                            CvTemplateStyle.CLEAN_TECH_STARTUP -> AndroidColor.parseColor("#D1FAE5")
                            CvTemplateStyle.MODERN_MINIMALIST -> AndroidColor.parseColor("#CCFBF1")
                            CvTemplateStyle.NGO_DEVELOPMENT_HUMANITARIAN -> AndroidColor.parseColor("#FFEDD5")
                            else -> AndroidColor.parseColor("#E2EEF9")
                        }
                        val tableHeaderBgPaint = Paint().apply {
                            color = headerBgColor
                            style = Paint.Style.FILL
                        }

                        val headerRowH = 18f * fontScaleMultiplier
                        checkAndAddNewPage(headerRowH + 20f)
                        canvas.drawRect(margin, currentY, margin + contentWidth, currentY + headerRowH, tableHeaderBgPaint)
                        canvas.drawRect(margin, currentY, margin + contentWidth, currentY + headerRowH, tableBorderPaint)

                        var hx = margin
                        canvas.drawText("Exam", hx + 4f, currentY + 12f * fontScaleMultiplier, tableHeaderPaint); hx += colExamW
                        canvas.drawLine(hx, currentY, hx, currentY + headerRowH, tableBorderPaint)
                        canvas.drawText("Institute / Board / University", hx + 4f, currentY + 12f * fontScaleMultiplier, tableHeaderPaint); hx += colInstW
                        canvas.drawLine(hx, currentY, hx, currentY + headerRowH, tableBorderPaint)
                        canvas.drawText("Group / Subject", hx + 4f, currentY + 12f * fontScaleMultiplier, tableHeaderPaint); hx += colSubW
                        canvas.drawLine(hx, currentY, hx, currentY + headerRowH, tableBorderPaint)
                        canvas.drawText("Result", hx + 4f, currentY + 12f * fontScaleMultiplier, tableHeaderPaint); hx += colResW
                        canvas.drawLine(hx, currentY, hx, currentY + headerRowH, tableBorderPaint)
                        canvas.drawText("Year", hx + 4f, currentY + 12f * fontScaleMultiplier, tableHeaderPaint)

                        currentY += headerRowH

                        val sortedEdu = data.educations.sortedByDescending { it.passingYear.toIntOrNull() ?: 0 }
                        sortedEdu.forEach { edu ->
                            val examText = if (edu.examLevel.isNotBlank() && edu.examLevel != "Others") {
                                edu.examLevel
                            } else {
                                when {
                                    edu.degree.contains("MBA", ignoreCase = true) -> "MBA"
                                    edu.degree.contains("BBA", ignoreCase = true) -> "BBA"
                                    edu.degree.contains("HSC", ignoreCase = true) || edu.degree.contains("Higher Secondary", ignoreCase = true) -> "H.S.C"
                                    edu.degree.contains("SSC", ignoreCase = true) || edu.degree.contains("Secondary School", ignoreCase = true) -> "S.S.C"
                                    edu.degree.contains("B.Sc", ignoreCase = true) -> "B.Sc"
                                    edu.degree.contains("M.Sc", ignoreCase = true) -> "M.Sc"
                                    else -> edu.degree
                                }
                            }.take(20)

                            val subjectText = if (edu.subjectMajor.isNotBlank() && edu.subjectMajor != "Others") {
                                edu.subjectMajor
                            } else {
                                when {
                                    edu.degree.contains("Management", ignoreCase = true) -> "Management"
                                    edu.degree.contains("Business Studies", ignoreCase = true) -> "Business Studies"
                                    edu.degree.contains("Science", ignoreCase = true) -> "Science"
                                    edu.degree.contains("Commerce", ignoreCase = true) -> "Commerce"
                                    edu.degree.contains("Humanities", ignoreCase = true) || edu.degree.contains("Arts", ignoreCase = true) -> "Humanities"
                                    edu.degree.contains("in ", ignoreCase = true) -> edu.degree.substringAfter("in ").trim()
                                    else -> "General"
                                }
                            }.take(30)

                            val examL = StaticLayout.Builder.obtain(examText, 0, examText.length, tableCellPaint, (colExamW - 6f).toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                            val instL = StaticLayout.Builder.obtain(edu.institution, 0, edu.institution.length, tableCellPaint, (colInstW - 6f).toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                            val subL = StaticLayout.Builder.obtain(subjectText, 0, subjectText.length, tableCellPaint, (colSubW - 6f).toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                            val resL = StaticLayout.Builder.obtain(edu.result, 0, edu.result.length, tableCellPaint, (colResW - 6f).toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                            val yearL = StaticLayout.Builder.obtain(edu.passingYear, 0, edu.passingYear.length, tableCellPaint, (colYearW - 6f).toInt()).setLineSpacing(0f, data.customLineSpacing).build()

                            val rowH = maxOf(examL.height, instL.height, subL.height, resL.height, yearL.height).toFloat() + 8f
                            checkAndAddNewPage(rowH)

                            canvas.drawRect(margin, currentY, margin + contentWidth, currentY + rowH, tableBorderPaint)

                            var cx = margin
                            canvas.save(); canvas.translate(cx + 4f, currentY + 4f); examL.draw(canvas); canvas.restore()
                            cx += colExamW; canvas.drawLine(cx, currentY, cx, currentY + rowH, tableBorderPaint)

                            canvas.save(); canvas.translate(cx + 4f, currentY + 4f); instL.draw(canvas); canvas.restore()
                            cx += colInstW; canvas.drawLine(cx, currentY, cx, currentY + rowH, tableBorderPaint)

                            canvas.save(); canvas.translate(cx + 4f, currentY + 4f); subL.draw(canvas); canvas.restore()
                            cx += colSubW; canvas.drawLine(cx, currentY, cx, currentY + rowH, tableBorderPaint)

                            canvas.save(); canvas.translate(cx + 4f, currentY + 4f); resL.draw(canvas); canvas.restore()
                            cx += colResW; canvas.drawLine(cx, currentY, cx, currentY + rowH, tableBorderPaint)

                            canvas.save(); canvas.translate(cx + 4f, currentY + 4f); yearL.draw(canvas); canvas.restore()

                            currentY += rowH
                        }
                        currentY += 10f
                    }
                }
            }

            "EXPERIENCE" -> {
                if (data.isFresher) {
                    if (data.fresherAcademicProjects.isNotBlank()) {
                        drawSectionHeader("ACADEMIC PROJECTS & CAPSTONE THESIS", "🚀")
                        val pLayout = StaticLayout.Builder.obtain(data.fresherAcademicProjects, 0, data.fresherAcademicProjects.length, bodyPaint, contentWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                        checkAndAddNewPage(pLayout.height.toFloat() + 4f)
                        canvas.save(); canvas.translate(margin, currentY); pLayout.draw(canvas); canvas.restore()
                        currentY += pLayout.height + entryGap
                    }

                    if (data.fresherInternshipsVolunteer.isNotBlank()) {
                        drawSectionHeader("INTERNSHIPS & VOLUNTEER WORK", "🤝")
                        val vLayout = StaticLayout.Builder.obtain(data.fresherInternshipsVolunteer, 0, data.fresherInternshipsVolunteer.length, bodyPaint, contentWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                        checkAndAddNewPage(vLayout.height.toFloat() + 4f)
                        canvas.save(); canvas.translate(margin, currentY); vLayout.draw(canvas); canvas.restore()
                        currentY += vLayout.height + entryGap
                    }

                    if (data.fresherLeadershipClubs.isNotBlank()) {
                        drawSectionHeader("CAMPUS LEADERSHIP & EXTRACURRICULAR", "🏆")
                        val lLayout = StaticLayout.Builder.obtain(data.fresherLeadershipClubs, 0, data.fresherLeadershipClubs.length, bodyPaint, contentWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                        checkAndAddNewPage(lLayout.height.toFloat() + 4f)
                        canvas.save(); canvas.translate(margin, currentY); lLayout.draw(canvas); canvas.restore()
                        currentY += lLayout.height + entryGap
                    }

                    if (data.fresherKeyCoursework.isNotBlank()) {
                        drawSectionHeader("RELEVANT COURSEWORK & ACADEMIC CORE", "📚")
                        val cLayout = StaticLayout.Builder.obtain(data.fresherKeyCoursework, 0, data.fresherKeyCoursework.length, bodyPaint, contentWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                        checkAndAddNewPage(cLayout.height.toFloat() + 4f)
                        canvas.save(); canvas.translate(margin, currentY); cLayout.draw(canvas); canvas.restore()
                        currentY += cLayout.height + entryGap
                    }

                    if (data.experiences.isNotEmpty()) {
                        drawSectionHeader("ADDITIONAL PRACTICUM & WORK EXPERIENCE", "💼")
                        data.experiences.forEach { exp ->
                            if (exp.role.isNotBlank() || exp.company.isNotBlank()) {
                                val expTitle = "$bulletPrefix${exp.role} – ${exp.company} (${exp.location})${if (exp.startDate.isNotBlank()) " [${exp.startDate} - ${if (exp.isCurrent) "Present" else exp.endDate}]" else ""}"
                                val titleLayout = StaticLayout.Builder.obtain(expTitle, 0, expTitle.length, bodyBoldPaint, contentWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                                checkAndAddNewPage(titleLayout.height.toFloat() + 4f)
                                canvas.save(); canvas.translate(margin, currentY); titleLayout.draw(canvas); canvas.restore()
                                currentY += titleLayout.height + 2f

                                if (exp.description.isNotBlank()) {
                                    val lines = exp.description.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                                    lines.forEach { line ->
                                        val bulletLine = if (line.startsWith("•") || line.startsWith("-") || line.startsWith("▪") || line.startsWith("◆")) line else "$bulletPrefix$line"
                                        val descLayout = StaticLayout.Builder.obtain(bulletLine, 0, bulletLine.length, bodyPaint, (contentWidth - 12f).toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                                        checkAndAddNewPage(descLayout.height.toFloat() + 2f)
                                        canvas.save(); canvas.translate(margin + 12f, currentY); descLayout.draw(canvas); canvas.restore()
                                        currentY += descLayout.height + 2f
                                    }
                                    currentY += entryGap
                                } else {
                                    currentY += entryGap
                                }
                            }
                        }
                    }
                } else {
                    if (data.experiences.isNotEmpty()) {
                        val expSectionTitle = if (pdfStyle == CvTemplateStyle.HARVARD_CLASSIC) "PROFESSIONAL EXPERIENCE" else "WORK EXPERIENCES"
                        drawSectionHeader(expSectionTitle, "💼")

                        data.experiences.forEach { exp ->
                            if (exp.role.isNotBlank() || exp.company.isNotBlank()) {
                                val expTitle = "$bulletPrefix${exp.role} – ${exp.company} (${exp.location})${if (exp.startDate.isNotBlank()) " [${exp.startDate} - ${if (exp.isCurrent) "Present" else exp.endDate}]" else ""}"
                                val titleLayout = StaticLayout.Builder.obtain(expTitle, 0, expTitle.length, bodyBoldPaint, contentWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                                checkAndAddNewPage(titleLayout.height.toFloat() + 4f)
                                canvas.save(); canvas.translate(margin, currentY); titleLayout.draw(canvas); canvas.restore()
                                currentY += titleLayout.height + 2f

                                if (exp.description.isNotBlank()) {
                                    val lines = exp.description.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                                    lines.forEach { line ->
                                        val bulletLine = if (line.startsWith("•") || line.startsWith("-") || line.startsWith("▪") || line.startsWith("◆")) line else "$bulletPrefix$line"
                                        val descLayout = StaticLayout.Builder.obtain(bulletLine, 0, bulletLine.length, bodyPaint, (contentWidth - 12f).toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                                        checkAndAddNewPage(descLayout.height.toFloat() + 2f)
                                        canvas.save(); canvas.translate(margin + 12f, currentY); descLayout.draw(canvas); canvas.restore()
                                        currentY += descLayout.height + 2f
                                    }
                                    currentY += entryGap
                                } else {
                                    currentY += entryGap
                                }
                            }
                        }
                    }
                }
            }

            "SKILLS" -> {
                if (data.skills.isNotEmpty()) {
                    drawSectionHeader("KEY SKILLS & COMPETENCIES", "⚡")
                    data.skills.forEach { sk ->
                        if (sk.name.isNotBlank()) {
                            val sb = SpannableStringBuilder()
                            sb.append(bulletPrefix)

                            val titleText = if (sk.description.isNotBlank()) {
                                "${sk.name}: "
                            } else if (sk.name.contains(":")) {
                                "${sk.name.substringBefore(":")}: "
                            } else {
                                "${sk.name} "
                            }

                            val descText = if (sk.description.isNotBlank()) {
                                sk.description
                            } else if (sk.name.contains(":")) {
                                sk.name.substringAfter(":").trim()
                            } else {
                                if (sk.level.isNotBlank() && sk.level != "Proficient") "(${sk.level})" else ""
                            }

                            val titleStart = sb.length
                            sb.append(titleText)
                            val titleEnd = sb.length
                            sb.setSpan(StyleSpan(Typeface.BOLD), titleStart, titleEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                            if (descText.isNotBlank()) {
                                sb.append(descText)
                            }

                            val layout = StaticLayout.Builder.obtain(sb, 0, sb.length, bodyPaint, contentWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                            checkAndAddNewPage(layout.height.toFloat() + 4f)
                            canvas.save()
                            canvas.translate(margin, currentY)
                            layout.draw(canvas)
                            canvas.restore()
                            currentY += layout.height + entryGap
                        }
                    }
                    currentY += 2f
                }
            }

            "PROJECTS" -> {
                if (data.projects.isNotEmpty() && data.projects.any { it.title.isNotBlank() }) {
                    drawSectionHeader("FEATURED PROJECTS & INITIATIVES", "🚀")
                    data.projects.filter { it.title.isNotBlank() }.forEach { pr ->
                        val pHead = "$bulletPrefix${pr.title}${if (pr.link.isNotBlank()) " (${pr.link})" else ""}"
                        val pHeadLayout = StaticLayout.Builder.obtain(pHead, 0, pHead.length, bodyBoldPaint, contentWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                        checkAndAddNewPage(pHeadLayout.height.toFloat() + 4f)
                        canvas.save(); canvas.translate(margin, currentY); pHeadLayout.draw(canvas); canvas.restore()
                        currentY += pHeadLayout.height + 2f

                        if (pr.description.isNotBlank()) {
                            val pDescLayout = StaticLayout.Builder.obtain(pr.description, 0, pr.description.length, bodyPaint, (contentWidth - 12f).toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                            checkAndAddNewPage(pDescLayout.height.toFloat() + 2f)
                            canvas.save(); canvas.translate(margin + 12f, currentY); pDescLayout.draw(canvas); canvas.restore()
                            currentY += pDescLayout.height + entryGap
                        }
                    }
                    currentY += 2f
                }
            }

            "CERTIFICATIONS" -> {
                if (data.certifications.isNotBlank()) {
                    drawSectionHeader("TRAINING & CERTIFICATION", "📜")
                    val certLayout = StaticLayout.Builder.obtain(data.certifications, 0, data.certifications.length, bodyPaint, contentWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                    checkAndAddNewPage(certLayout.height.toFloat() + 4f)
                    canvas.save(); canvas.translate(margin, currentY); certLayout.draw(canvas); canvas.restore()
                    currentY += certLayout.height + 4f
                }
            }

            "LANGUAGES" -> {
                if (data.languages.isNotBlank()) {
                    drawSectionHeader("LANGUAGE FLUENCY", "🌐")
                    val langLayout = StaticLayout.Builder.obtain(data.languages, 0, data.languages.length, bodyPaint, contentWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                    checkAndAddNewPage(langLayout.height.toFloat() + 4f)
                    canvas.save(); canvas.translate(margin, currentY); langLayout.draw(canvas); canvas.restore()
                    currentY += langLayout.height + 4f
                }
            }

            "CUSTOM_SECTIONS" -> {
                if (data.customSections.isNotEmpty()) {
                    data.customSections.forEach { item ->
                        if (item.title.isNotBlank() && item.content.isNotBlank()) {
                            drawSectionHeader(item.title.uppercase(), "📌")
                            val itemLayout = StaticLayout.Builder.obtain(item.content, 0, item.content.length, bodyPaint, contentWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                            checkAndAddNewPage(itemLayout.height.toFloat() + 4f)
                            canvas.save(); canvas.translate(margin, currentY); itemLayout.draw(canvas); canvas.restore()
                            currentY += itemLayout.height + entryGap
                        }
                    }
                }
            }

            "PERSONAL_INFO" -> {
                val leftColList = mutableListOf<Pair<String, String>>()
                if (data.fatherName.isNotBlank()) leftColList.add("Father's Name" to data.fatherName)
                if (data.motherName.isNotBlank()) leftColList.add("Mother's Name" to data.motherName)
                if (data.bloodGroup.isNotBlank()) leftColList.add("Blood Group" to data.bloodGroup)

                val rightColList = mutableListOf<Pair<String, String>>()
                if (data.religion.isNotBlank()) rightColList.add("Religion" to data.religion)
                if (data.presentAddress.isNotBlank()) rightColList.add("Present Address" to data.presentAddress)
                if (data.permanentAddress.isNotBlank()) rightColList.add("Permanent Address" to data.permanentAddress)

                if (leftColList.isNotEmpty() || rightColList.isNotEmpty()) {
                    drawSectionHeader("PERSONAL INFORMATION", "👤")
                    val halfW = contentWidth / 2f - 10f
                    val maxRows = maxOf(leftColList.size, rightColList.size)

                    for (i in 0 until maxRows) {
                        val leftItem = leftColList.getOrNull(i)
                        val rightItem = rightColList.getOrNull(i)

                        val leftStr = if (leftItem != null) "${leftItem.first} : ${leftItem.second}" else ""
                        val rightStr = if (rightItem != null) "${rightItem.first} : ${rightItem.second}" else ""

                        val lLayout = StaticLayout.Builder.obtain(leftStr, 0, leftStr.length, bodyPaint, halfW.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                        val rLayout = StaticLayout.Builder.obtain(rightStr, 0, rightStr.length, bodyPaint, halfW.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                        val rowH = maxOf(lLayout.height, rLayout.height).toFloat() + 3f

                        checkAndAddNewPage(rowH)
                        if (leftStr.isNotBlank()) {
                            canvas.save(); canvas.translate(margin, currentY); lLayout.draw(canvas); canvas.restore()
                        }
                        if (rightStr.isNotBlank()) {
                            canvas.save(); canvas.translate(margin + halfW + 20f, currentY); rLayout.draw(canvas); canvas.restore()
                        }
                        currentY += rowH
                    }
                    currentY += 2f
                }
            }

            "REFERENCES" -> {
                if (data.references.isNotBlank()) {
                    drawSectionHeader("REFERENCES", "🤝")
                    val refLayout = StaticLayout.Builder.obtain(data.references, 0, data.references.length, bodyPaint, contentWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                    checkAndAddNewPage(refLayout.height.toFloat() + 4f)
                    canvas.save(); canvas.translate(margin, currentY); refLayout.draw(canvas); canvas.restore()
                    currentY += refLayout.height + 4f
                }
            }
        }
    }

    // 9. SIGNATURE & DATE (OPTIONAL APPLICANT SIGNATURE PLACEHOLDER)
    if (data.showSignatureLine) {
        checkAndAddNewPage(75f)
        currentY += 24f
        val sigLineW = 165f
        val sigStartX = pageWidth - margin - sigLineW
        val sigLinePaint = Paint().apply {
            color = AndroidColor.parseColor("#334155")
            strokeWidth = 1f
        }
        canvas.drawLine(sigStartX, currentY, pageWidth - margin, currentY, sigLinePaint)
        currentY += 12f
        val sigTitlePaint = TextPaint().apply {
            isAntiAlias = true
            color = AndroidColor.parseColor("#0F172A")
            textSize = 9.2f
            typeface = tfBold
            textAlign = Paint.Align.CENTER
        }
        val sigSubPaint = TextPaint().apply {
            isAntiAlias = true
            color = AndroidColor.parseColor("#475569")
            textSize = 8.5f
            typeface = tfRegular
            textAlign = Paint.Align.CENTER
        }
        val cx = sigStartX + (sigLineW / 2f)
        canvas.drawText("Applicant's Signature", cx, currentY, sigTitlePaint)
        currentY += 12f
        if (data.fullName.isNotBlank()) {
            canvas.drawText("(${data.fullName})", cx, currentY, sigSubPaint)
            currentY += 12f
        }
        canvas.drawText("Date: ____________________", cx, currentY, sigSubPaint)
        currentY += 10f
    }

    pdfDocument.finishPage(page)

    // Dynamic sanitized file naming
    val passingYearStr = if (data.educations.isNotEmpty()) {
        data.educations.sortedByDescending { it.passingYear.toIntOrNull() ?: 0 }.first().passingYear
    } else {
        "2026"
    }
    val pdfFileName = CvFileNameUtility.generateFileName(data.fullName, passingYearStr)
    val file = File(context.cacheDir, pdfFileName)
    val fos = FileOutputStream(file)
    try {
        pdfDocument.writeTo(fos)
    } finally {
        pdfDocument.close()
        fos.close()
    }

    return file
}

private fun renderPdfPageToBitmap(pdfFile: File, pageIndex: Int = 0): Bitmap? {
    if (!pdfFile.exists() || pdfFile.length() == 0L) return null
    var pfd: ParcelFileDescriptor? = null
    var renderer: PdfRenderer? = null
    return try {
        pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        renderer = PdfRenderer(pfd)
        if (renderer.pageCount > pageIndex) {
            val page = renderer.openPage(pageIndex)
            val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(AndroidColor.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            bitmap
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } finally {
        try { renderer?.close() } catch (_: Exception) {}
        try { pfd?.close() } catch (_: Exception) {}
    }
}

private fun renderAllPdfPagesToBitmaps(pdfFile: File): List<Bitmap> {
    if (!pdfFile.exists() || pdfFile.length() == 0L) return emptyList()
    var pfd: ParcelFileDescriptor? = null
    var renderer: PdfRenderer? = null
    val list = mutableListOf<Bitmap>()
    return try {
        pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        renderer = PdfRenderer(pfd)
        val count = renderer.pageCount
        for (i in 0 until count) {
            val page = renderer.openPage(i)
            val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(AndroidColor.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            list.add(bitmap)
        }
        list
    } catch (e: Exception) {
        e.printStackTrace()
        list
    } finally {
        try { renderer?.close() } catch (_: Exception) {}
        try { pfd?.close() } catch (_: Exception) {}
    }
}

private fun parseImportedJsonToCvData(jsonStr: String): CvData {
    val cleanJson = jsonStr.trim().removePrefix("```json").removeSuffix("```").trim()
    try {
        val json = JSONObject(cleanJson)
        val name = json.optString("fullName", "")
        val title = json.optString("jobTitle", "")
        val email = json.optString("email", "")
        val phone = json.optString("phone", "")
        val address = json.optString("presentAddress", json.optString("address", ""))
        val summary = json.optString("summary", "")

        val expList = mutableListOf<CvExperienceItem>()
        val expArr = json.optJSONArray("experiences")
        if (expArr != null) {
            for (i in 0 until expArr.length()) {
                val eObj = expArr.getJSONObject(i)
                val duration = eObj.optString("duration", "")
                var start = "2020"
                var end = "Present"
                var isCurr = true
                if (duration.contains("-")) {
                    val parts = duration.split("-")
                    start = parts.getOrNull(0)?.trim() ?: "2020"
                    end = parts.getOrNull(1)?.trim() ?: "Present"
                    isCurr = end.lowercase().contains("present") || end.lowercase().contains("current")
                }
                expList.add(
                    CvExperienceItem(
                        id = UUID.randomUUID().toString(),
                        company = eObj.optString("companyName", eObj.optString("company", "")),
                        role = eObj.optString("jobTitle", eObj.optString("role", "")),
                        startDate = start,
                        endDate = end,
                        isCurrent = isCurr,
                        description = eObj.optString("description", ""),
                        location = eObj.optString("location", "Dhaka, Bangladesh")
                    )
                )
            }
        }

        val eduList = mutableListOf<CvEducationItem>()
        val eduArr = json.optJSONArray("educations")
        if (eduArr != null) {
            for (i in 0 until eduArr.length()) {
                val edObj = eduArr.getJSONObject(i)
                eduList.add(
                    CvEducationItem(
                        id = UUID.randomUUID().toString(),
                        degree = edObj.optString("degreeName", edObj.optString("degree", "")),
                        institution = edObj.optString("institution", edObj.optString("institute", "")),
                        passingYear = edObj.optString("passingYear", ""),
                        result = edObj.optString("resultValue", edObj.optString("result", "")),
                        examLevel = edObj.optString("examLevel", ""),
                        subjectMajor = edObj.optString("subjectMajor", edObj.optString("groupOrSubject", "")),
                        resultType = edObj.optString("resultType", "CGPA")
                    )
                )
            }
        }

        val skillList = mutableListOf<CvSkillItem>()
        val skillArr = json.optJSONArray("skills")
        if (skillArr != null) {
            for (i in 0 until skillArr.length()) {
                val sObj = skillArr.getJSONObject(i)
                skillList.add(
                    CvSkillItem(
                        id = UUID.randomUUID().toString(),
                        name = sObj.optString("name", ""),
                        description = sObj.optString("description", "")
                    )
                )
            }
        }

        return CvData(
            id = "profile_import_" + UUID.randomUUID().toString().take(6),
            profileLabel = "Imported: " + (title.ifBlank { "Resume Profile" }),
            fullName = name,
            jobTitle = title,
            email = email,
            phone = phone,
            address = address,
            presentAddress = address,
            summary = summary,
            experiences = expList,
            educations = eduList,
            skills = skillList
        )
    } catch (e: Exception) {
        e.printStackTrace()
        return CvData(
            id = "profile_import_" + UUID.randomUUID().toString().take(6),
            profileLabel = "Imported Resume",
            fullName = "Parsed Candidate",
            summary = cleanJson.take(150)
        )
    }
}

private fun generateCvDocxFile(context: Context, data: CvData): File {
    val fileName = "CV_${data.fullName.replace(" ", "_")}_${System.currentTimeMillis()}.docx"
    val docxFile = File(context.cacheDir, fileName)
    
    val xmlSb = StringBuilder()
    xmlSb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
    xmlSb.append("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">\n")
    xmlSb.append("  <w:body>\n")
    
    // Header Candidate Name
    xmlSb.append("    <w:p><w:pPr><w:jc w:val=\"center\"/></w:pPr><w:r><w:rPr><w:b/><w:sz w:val=\"36\"/><w:color w:val=\"111827\"/></w:rPr><w:t>")
    xmlSb.append(escapeXml(data.fullName.ifBlank { "Md. Shariful Islam" }))
    xmlSb.append("</w:t></w:r></w:p>\n")
    
    // Job Title
    xmlSb.append("    <w:p><w:pPr><w:jc w:val=\"center\"/></w:pPr><w:r><w:rPr><w:b/><w:sz w:val=\"24\"/><w:color w:val=\"2563EB\"/></w:rPr><w:t>")
    xmlSb.append(escapeXml(data.jobTitle.ifBlank { "Management Graduate" }))
    xmlSb.append("</w:t></w:r></w:p>\n")
    
    // Contact Line
    val contacts = listOfNotNull<String>(
        data.email.takeIf { it.isNotBlank() },
        data.phone.takeIf { it.isNotBlank() },
        data.presentAddress.takeIf { it.isNotBlank() } ?: data.address.takeIf { it.isNotBlank() },
        data.linkedin.takeIf { it.isNotBlank() }
    ).joinToString(" | ")
    
    if (contacts.isNotBlank()) {
        xmlSb.append("    <w:p><w:pPr><w:jc w:val=\"center\"/></w:pPr><w:r><w:rPr><w:sz w:val=\"20\"/><w:color w:val=\"4B5563\"/></w:rPr><w:t>")
        xmlSb.append(escapeXml(contacts))
        xmlSb.append("</w:t></w:r></w:p>\n")
    }
    
    // Helper function for XML section header
    fun appendSectionHeader(title: String) {
        xmlSb.append("    <w:p><w:pPr><w:pBdr><w:bottom w:val=\"single\" w:sz=\"12\" w:space=\"4\" w:color=\"2563EB\"/></w:pBdr></w:pPr><w:r><w:rPr><w:b/><w:sz w:val=\"26\"/><w:color w:val=\"2563EB\"/></w:rPr><w:t>")
        xmlSb.append(escapeXml(title.uppercase()))
        xmlSb.append("</w:t></w:r></w:p>\n")
    }
    
    // Professional Summary
    if (data.summary.isNotBlank()) {
        appendSectionHeader("Professional Summary")
        xmlSb.append("    <w:p><w:r><w:rPr><w:sz w:val=\"22\"/><w:color w:val=\"1F2937\"/></w:rPr><w:t>")
        xmlSb.append(escapeXml(data.summary))
        xmlSb.append("</w:t></w:r></w:p>\n")
    }
    
    // Work Experience
    if (data.experiences.isNotEmpty()) {
        appendSectionHeader("Work Experience")
        for (exp in data.experiences) {
            xmlSb.append("    <w:p><w:r><w:rPr><w:b/><w:sz w:val=\"24\"/><w:color w:val=\"111827\"/></w:rPr><w:t>")
            xmlSb.append(escapeXml(exp.role))
            xmlSb.append("</w:t></w:r><w:r><w:rPr><w:sz w:val=\"22\"/><w:color w:val=\"374151\"/></w:rPr><w:t> - ")
            xmlSb.append(escapeXml(exp.company))
            xmlSb.append(" (")
            val durationStr = "${exp.startDate} - ${if (exp.isCurrent) "Present" else exp.endDate}"
            xmlSb.append(escapeXml(durationStr))
            xmlSb.append(")</w:t></w:r></w:p>\n")
            
            if (exp.description.isNotBlank()) {
                val lines = exp.description.split("\n")
                for (l in lines) {
                    val clean = l.trim().removePrefix("•").removePrefix("-").trim()
                    if (clean.isNotBlank()) {
                        xmlSb.append("    <w:p><w:pPr><w:ind w:left=\"360\"/></w:pPr><w:r><w:rPr><w:sz w:val=\"20\"/><w:color w:val=\"374151\"/></w:rPr><w:t>• ")
                        xmlSb.append(escapeXml(clean))
                        xmlSb.append("</w:t></w:r></w:p>\n")
                    }
                }
            }
        }
    }
    
    // Education Details
    if (data.educations.isNotEmpty()) {
        appendSectionHeader("Education Details")
        for (edu in data.educations) {
            xmlSb.append("    <w:p><w:r><w:rPr><w:b/><w:sz w:val=\"22\"/><w:color w:val=\"111827\"/></w:rPr><w:t>")
            xmlSb.append(escapeXml(edu.examLevel.ifBlank { edu.degree }))
            xmlSb.append(" in ")
            xmlSb.append(escapeXml(edu.subjectMajor))
            xmlSb.append("</w:t></w:r></w:p>\n")
            
            xmlSb.append("    <w:p><w:r><w:rPr><w:sz w:val=\"20\"/><w:color w:val=\"4B5563\"/></w:rPr><w:t>")
            xmlSb.append(escapeXml(edu.institution))
            xmlSb.append(" | Passing Year: ")
            xmlSb.append(escapeXml(edu.passingYear))
            if (edu.result.isNotBlank()) {
                xmlSb.append(" | Result: ")
                xmlSb.append(escapeXml(edu.result))
            }
            xmlSb.append("</w:t></w:r></w:p>\n")
        }
    }
    
    // Key Skills & Competence
    if (data.skills.isNotEmpty()) {
        appendSectionHeader("Key Skill & Competence")
        for (sk in data.skills) {
            xmlSb.append("    <w:p><w:pPr><w:ind w:left=\"360\"/></w:pPr>")
            xmlSb.append("<w:r><w:rPr><w:b/><w:sz w:val=\"22\"/><w:color w:val=\"111827\"/></w:rPr><w:t>• ")
            xmlSb.append(escapeXml(sk.name))
            if (sk.description.isNotBlank()) {
                xmlSb.append(": </w:t></w:r><w:r><w:rPr><w:sz w:val=\"20\"/><w:color w:val=\"374151\"/></w:rPr><w:t>")
                xmlSb.append(escapeXml(sk.description))
            }
            xmlSb.append("</w:t></w:r></w:p>\n")
        }
    }
    
    // Projects
    if (data.projects.isNotEmpty()) {
        appendSectionHeader("Projects & Key Accomplishments")
        for (proj in data.projects) {
            xmlSb.append("    <w:p><w:r><w:rPr><w:b/><w:sz w:val=\"22\"/><w:color w:val=\"111827\"/></w:rPr><w:t>")
            xmlSb.append(escapeXml(proj.title))
            xmlSb.append("</w:t></w:r></w:p>\n")
            if (proj.description.isNotBlank()) {
                xmlSb.append("    <w:p><w:r><w:rPr><w:sz w:val=\"20\"/><w:color w:val=\"374151\"/></w:rPr><w:t>")
                xmlSb.append(escapeXml(proj.description))
                xmlSb.append("</w:t></w:r></w:p>\n")
            }
        }
    }
    
    xmlSb.append("  </w:body>\n")
    xmlSb.append("</w:document>")

    try {
        ZipOutputStream(FileOutputStream(docxFile)).use { zos ->
            // [Content_Types].xml
            zos.putNextEntry(ZipEntry("[Content_Types].xml"))
            val contentTypes = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">\n" +
                    "  <Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>\n" +
                    "  <Default Extension=\"xml\" ContentType=\"application/xml\"/>\n" +
                    "  <Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>\n" +
                    "</Types>"
            zos.write(contentTypes.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            // _rels/.rels
            zos.putNextEntry(ZipEntry("_rels/.rels"))
            val rels = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
                    "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/>\n" +
                    "</Relationships>"
            zos.write(rels.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            // word/document.xml
            zos.putNextEntry(ZipEntry("word/document.xml"))
            zos.write(xmlSb.toString().toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return docxFile
}

private fun escapeXml(input: String): String {
    return input.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}

private fun shareDocxFile(context: Context, docxFile: File) {
    if (!docxFile.exists()) return
    try {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", docxFile)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share DOCX Resume"))
    } catch (e: Exception) {
        Toast.makeText(context, "Share failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

private fun downloadDocxFile(context: Context, docxFile: File) {
    if (!docxFile.exists()) return
    try {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val destFile = File(downloadsDir, docxFile.name)
        docxFile.copyTo(destFile, overwrite = true)
        Toast.makeText(context, "DOCX Saved to Downloads folder: ${destFile.name}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        shareDocxFile(context, docxFile)
    }
}

// ================= COMPACT SMART EDIT COMPONENTS =================

@Composable
private fun CompactEditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    themeColors: CalculatorThemeColors,
    placeholder: String = "",
    singleLine: Boolean = true,
    onAiPrompt: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = themeColors.buttonEqualBg.copy(alpha = 0.9f),
                letterSpacing = 0.8.sp
            )
            if (onAiPrompt != null) {
                IconButton(onClick = onAiPrompt, modifier = Modifier.size(20.dp)) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI", tint = themeColors.buttonEqualBg, modifier = Modifier.size(14.dp))
                }
            }
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            textStyle = TextStyle(fontSize = 14.sp, color = themeColors.displayText, fontWeight = FontWeight.Medium),
            singleLine = singleLine,
            cursorBrush = SolidColor(themeColors.buttonEqualBg),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()
                            val y = size.height - strokeWidth / 2
                            drawLine(
                                color = themeColors.displayText.copy(alpha = 0.12f),
                                start = androidx.compose.ui.geometry.Offset(0f, y),
                                end = androidx.compose.ui.geometry.Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        }
                        .padding(bottom = 6.dp)
                ) {
                    if (value.isEmpty()) {
                        Text(text = placeholder, fontSize = 14.sp, color = themeColors.displayText.copy(alpha = 0.3f))
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun CompactSectionHeader(
    title: String,
    icon: ImageVector,
    themeColors: CalculatorThemeColors,
    onAdd: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )
        }
        if (onAdd != null) {
            IconButton(onClick = onAdd, modifier = Modifier.size(28.dp)) {
                Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Add", tint = themeColors.buttonEqualBg)
            }
        }
    }
}

@Composable
private fun CompactLargeEditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    themeColors: CalculatorThemeColors,
    placeholder: String = "",
    onAiPrompt: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = themeColors.buttonEqualBg.copy(alpha = 0.9f),
                letterSpacing = 0.8.sp
            )
            if (onAiPrompt != null) {
                IconButton(onClick = onAiPrompt, modifier = Modifier.size(20.dp)) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI", tint = themeColors.buttonEqualBg, modifier = Modifier.size(14.dp))
                }
            }
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            textStyle = TextStyle(fontSize = 13.5.sp, color = themeColors.displayText, fontWeight = FontWeight.Normal, lineHeight = 19.sp),
            cursorBrush = SolidColor(themeColors.buttonEqualBg),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()
                            val y = size.height - strokeWidth / 2
                            drawLine(
                                color = themeColors.displayText.copy(alpha = 0.12f),
                                start = androidx.compose.ui.geometry.Offset(0f, y),
                                end = androidx.compose.ui.geometry.Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        }
                        .padding(bottom = 6.dp)
                ) {
                    if (value.isEmpty()) {
                        Text(text = placeholder, fontSize = 13.5.sp, color = themeColors.displayText.copy(alpha = 0.3f))
                    }
                    innerTextField()
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SmartEditTab(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onRequestAiPrompt: (title: String, defaultPrompt: String, targetField: String, expIdx: Int) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        CompactSectionHeader(if (isBn) "ব্যক্তিগত ও যোগাযোগ তথ্য" else "Personal & Contact", Icons.Default.Person, themeColors)
        CompactEditField(if (isBn) "পূর্ণ নাম" else "Full Name", cvData.fullName, { onCvDataChange(cvData.copy(fullName = it)) }, themeColors, "John Doe")
        CompactEditField(if (isBn) "পদবী" else "Target Job Title", cvData.jobTitle, { onCvDataChange(cvData.copy(jobTitle = it)) }, themeColors, "Software Engineer")
        
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                CompactEditField(if (isBn) "ইমেইল" else "Email", cvData.email, { onCvDataChange(cvData.copy(email = it)) }, themeColors, "john@example.com")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                CompactEditField(if (isBn) "ফোন" else "Phone", cvData.phone, { onCvDataChange(cvData.copy(phone = it)) }, themeColors, "+880...")
            }
        }
        
        CompactEditField(if (isBn) "ঠিকানা" else "Location Address", cvData.address, { onCvDataChange(cvData.copy(address = it)) }, themeColors, "Dhaka, Bangladesh")

        CompactSectionHeader(if (isBn) "ক্যারিয়ার সারসংক্ষেপ" else "Professional Summary", Icons.Default.AutoStories, themeColors)
        CompactLargeEditField(
            label = "",
            value = cvData.summary,
            onValueChange = { onCvDataChange(cvData.copy(summary = it)) },
            themeColors = themeColors,
            placeholder = if (isBn) "আপনার সম্পর্কে ৩টি বাক্যে লিখুন..." else "Write 3 professional sentences about your expertise...",
            onAiPrompt = {
                onRequestAiPrompt(
                    if (isBn) "সারসংক্ষেপ এআই" else "AI Summary",
                    "Write a professional ATS summary for ${cvData.fullName} targeting ${cvData.jobTitle}.",
                    "SUMMARY",
                    -1
                )
            }
        )

        CompactSectionHeader(
            if (isBn) "কাজের অভিজ্ঞতা" else "Work Experience", 
            Icons.Default.Work, 
            themeColors,
            onAdd = {
                val newList = cvData.experiences.toMutableList()
                newList.add(0, CvExperienceItem())
                onCvDataChange(cvData.copy(experiences = newList))
            }
        )
        cvData.experiences.forEachIndexed { index, exp ->
            Surface(
                modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
                color = Color.Transparent
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "${if (isBn) "অভিজ্ঞতা" else "Exp"} #${index + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.buttonEqualBg)
                        IconButton(onClick = {
                            val newList = cvData.experiences.toMutableList()
                            newList.removeAt(index)
                            onCvDataChange(cvData.copy(experiences = newList))
                        }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            CompactEditField(if (isBn) "পদবী" else "Role", exp.role, { r ->
                                val newList = cvData.experiences.toMutableList()
                                newList[index] = exp.copy(role = r)
                                onCvDataChange(cvData.copy(experiences = newList))
                            }, themeColors)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            CompactEditField(if (isBn) "কোম্পানি" else "Company", exp.company, { c ->
                                val newList = cvData.experiences.toMutableList()
                                newList[index] = exp.copy(company = c)
                                onCvDataChange(cvData.copy(experiences = newList))
                            }, themeColors)
                        }
                    }
                    CompactLargeEditField(
                        label = if (isBn) "বিবরণ ও অর্জনসমূহ" else "Description & Achievements",
                        value = exp.description,
                        onValueChange = { d ->
                            val newList = cvData.experiences.toMutableList()
                            newList[index] = exp.copy(description = d)
                            onCvDataChange(cvData.copy(experiences = newList))
                        },
                        themeColors = themeColors,
                        onAiPrompt = {
                            onRequestAiPrompt(
                                if (isBn) "অভিজ্ঞতা এআই" else "AI Rewrite",
                                "Enhance this work description: ${exp.description}",
                                "EXPERIENCE",
                                index
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        CompactSectionHeader(
            if (isBn) "শিক্ষা" else "Education", 
            Icons.Default.School, 
            themeColors,
            onAdd = {
                val newList = cvData.educations.toMutableList()
                newList.add(0, CvEducationItem())
                onCvDataChange(cvData.copy(educations = newList))
            }
        )
        cvData.educations.forEachIndexed { index, edu ->
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${if (isBn) "শিক্ষা" else "Edu"} #${index + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.buttonEqualBg)
                    IconButton(onClick = {
                        val newList = cvData.educations.toMutableList()
                        newList.removeAt(index)
                        onCvDataChange(cvData.copy(educations = newList))
                    }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                    }
                }
                CompactEditField(if (isBn) "ডিগ্রী/পরীক্ষা" else "Degree/Exam", edu.degree, { d ->
                    val newList = cvData.educations.toMutableList()
                    newList[index] = edu.copy(degree = d)
                    onCvDataChange(cvData.copy(educations = newList))
                }, themeColors)
                CompactEditField(if (isBn) "প্রতিষ্ঠান" else "Institution", edu.institution, { i ->
                    val newList = cvData.educations.toMutableList()
                    newList[index] = edu.copy(institution = i)
                    onCvDataChange(cvData.copy(educations = newList))
                }, themeColors)
            }
        }

        CompactSectionHeader(
            if (isBn) "স্কিলস" else "Skills", 
            Icons.Default.Bolt, 
            themeColors,
            onAdd = {
                val newList = cvData.skills.toMutableList()
                newList.add(CvSkillItem(name = "New Skill"))
                onCvDataChange(cvData.copy(skills = newList))
            }
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            cvData.skills.forEachIndexed { index, skill ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = themeColors.buttonEqualBg.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        BasicTextField(
                            value = skill.name,
                            onValueChange = { n ->
                                val newList = cvData.skills.toMutableList()
                                newList[index] = skill.copy(name = n)
                                onCvDataChange(cvData.copy(skills = newList))
                            },
                            textStyle = TextStyle(fontSize = 12.sp, color = themeColors.displayText, fontWeight = FontWeight.Bold),
                            singleLine = true,
                            modifier = Modifier.widthIn(min = 40.dp, max = 120.dp)
                        )
                        IconButton(onClick = {
                            val newList = cvData.skills.toMutableList()
                            newList.removeAt(index)
                            onCvDataChange(cvData.copy(skills = newList))
                        }, modifier = Modifier.size(16.dp)) {
                            Icon(Icons.Default.Close, null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(10.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ================= MAIN TOOL COMPOSABLE =================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtsCvBuilderTool(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

    // Load multi profiles
    var profilesList by remember { mutableStateOf(loadAllCvProfiles(context)) }
    var activeProfileId by remember { mutableStateOf(loadActiveProfileId(context)) }

    // Resolve active CvData
    if (activeProfileId.isBlank() || profilesList.none { it.id == activeProfileId }) {
        activeProfileId = profilesList.firstOrNull()?.id ?: ""
    }

    val activeCvDataIndex = profilesList.indexOfFirst { it.id == activeProfileId }.let { if (it == -1) 0 else it }
    var cvData by remember(activeProfileId) {
        mutableStateOf(profilesList.getOrNull(activeCvDataIndex) ?: CvData())
    }

    // States for screens
    var selectedTab by remember { mutableStateOf(0) } // 0: Profile/Personas, 1: Experience, 2: Education, 3: Job Match, 4: Preview
    var previewRefreshKey by remember { mutableStateOf(0) }
    var isPreviewRendering by remember { mutableStateOf(false) }

    var isAiLoading by remember { mutableStateOf(false) }
    var aiLoadingMessage by remember { mutableStateOf("") }

    var generatedPdfFile by remember { mutableStateOf<File?>(null) }
    var pdfPreviewBitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }

    // History & Profile state
    var historyList by remember { mutableStateOf(loadCvHistory(context)) }
    var showHistoryDialog by remember { mutableStateOf(false) }

    var undoStack by remember { mutableStateOf(listOf<CvData>()) }
    var redoStack by remember { mutableStateOf(listOf<CvData>()) }

    fun performUndo() {
        if (undoStack.isNotEmpty()) {
            val prev = undoStack.last()
            undoStack = undoStack.dropLast(1)
            redoStack = redoStack + cvData
            cvData = prev
            
            val updatedList = profilesList.toMutableList()
            val idx = updatedList.indexOfFirst { it.id == prev.id }
            if (idx != -1) {
                updatedList[idx] = prev
            }
            profilesList = updatedList
            saveAllCvProfiles(context, updatedList)
            previewRefreshKey++
        }
    }

    fun performRedo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.last()
            redoStack = redoStack.dropLast(1)
            undoStack = undoStack + cvData
            cvData = next
            
            val updatedList = profilesList.toMutableList()
            val idx = updatedList.indexOfFirst { it.id == next.id }
            if (idx != -1) {
                updatedList[idx] = next
            }
            profilesList = updatedList
            saveAllCvProfiles(context, updatedList)
            previewRefreshKey++
        }
    }

    var showSaveProfileDialog by remember { mutableStateOf(false) }
    var showProfileManagerDialog by remember { mutableStateOf(false) }

    var showAiPromptDialog by remember { mutableStateOf(false) }
    var aiPromptTitle by remember { mutableStateOf("") }
    var aiPromptDefaultText by remember { mutableStateOf("") }
    var aiPromptTargetField by remember { mutableStateOf("") }
    var activeAiExperienceIndex by remember { mutableStateOf(-1) }
    var activeCircularImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var activeCircularImageMime by remember { mutableStateOf("") }

    val pdfImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isAiLoading = true
            aiLoadingMessage = if (isBn) "✨ জেমিনি এআই আপনার পিডিএফ সিভি বিশ্লেষণ করে প্রোফাইল তৈরি করছে..." else "✨ Gemini AI is analyzing your PDF resume to create a profile..."
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val tempFile = File(context.cacheDir, "temp_import_${System.currentTimeMillis()}.pdf")
                    tempFile.outputStream().use { out -> inputStream?.copyTo(out) }
                    val bitmaps = renderAllPdfPagesToBitmaps(tempFile)
                    val firstBmp = bitmaps.firstOrNull()

                    if (firstBmp != null) {
                        val baos = java.io.ByteArrayOutputStream()
                        firstBmp.compress(Bitmap.CompressFormat.JPEG, 80, baos)

                        val sysPrompt = "You are an expert resume parser. Extract candidate details from this resume image. Return valid JSON only with keys: fullName, jobTitle, email, phone, presentAddress, summary, skills (array of {name, description}), experiences (array of {jobTitle, companyName, duration, description}), educations (array of {examLevel, subjectMajor, institution, passingYear, resultType, resultValue})."
                        val jsonResult = callGeminiAiMultiModal(
                            prompt = "Extract candidate resume details into JSON.",
                            systemInstruction = sysPrompt,
                            imageBytes = baos.toByteArray(),
                            mimeType = "image/jpeg"
                        )

                        val importedCv = parseImportedJsonToCvData(jsonResult)
                        val updatedList = profilesList.toMutableList()
                        updatedList.add(importedCv)
                        profilesList = updatedList
                        saveAllCvProfiles(context, updatedList)
                        activeProfileId = importedCv.id
                        saveActiveProfileId(context, importedCv.id)
                        cvData = importedCv
                        Toast.makeText(context, if (isBn) "পিডিএফ সিভি থেকে নতুন প্রোফাইল তৈরি হয়েছে!" else "New profile imported successfully from PDF!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "PDF Import Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                } finally {
                    isAiLoading = false
                }
            }
        }
    }

    fun openAiPrompt(
        title: String,
        defaultPrompt: String,
        targetField: String,
        expIndex: Int = -1,
        imageBytes: ByteArray? = null,
        imageMime: String = ""
    ) {
        aiPromptTitle = title
        aiPromptDefaultText = defaultPrompt
        aiPromptTargetField = targetField
        activeAiExperienceIndex = expIndex
        activeCircularImageBytes = imageBytes
        activeCircularImageMime = imageMime
        showAiPromptDialog = true
    }

    // Save changes and update cache
    fun updateCvDataState(updated: CvData) {
        if (updated != cvData) {
            val currentUndo = undoStack.toMutableList()
            currentUndo.add(cvData)
            if (currentUndo.size > 30) currentUndo.removeAt(0)
            undoStack = currentUndo
            redoStack = emptyList()
        }
        cvData = updated
        val updatedList = profilesList.toMutableList()
        val idx = updatedList.indexOfFirst { it.id == updated.id }
        if (idx != -1) {
            updatedList[idx] = updated
        } else {
            updatedList.add(updated)
        }
        profilesList = updatedList
        saveAllCvProfiles(context, updatedList)
        previewRefreshKey++
    }

    // Auto-render live PDF vector preview on changes or when switching tabs
    LaunchedEffect(cvData, previewRefreshKey, selectedTab) {
        if (selectedTab != 5) {
            return@LaunchedEffect
        }
        kotlinx.coroutines.delay(300)
        isPreviewRendering = true
        withContext(Dispatchers.IO) {
            try {
                val file = generateCvPdfFile(context, cvData)
                val bitmaps = renderAllPdfPagesToBitmaps(file)
                withContext(Dispatchers.Main) {
                    generatedPdfFile = file
                    pdfPreviewBitmaps = bitmaps
                    isPreviewRendering = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    isPreviewRendering = false
                }
            }
        }
    }

    fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    if (showHistoryDialog) {
        CvHistoryDialog(
            historyList = historyList,
            isBn = isBn,
            themeColors = themeColors,
            onDismiss = { showHistoryDialog = false },
            onOpenPdf = { item ->
                val file = File(item.filePath)
                if (file.exists()) {
                    openPdfFile(context, file)
                } else {
                    showToast(if (isBn) "ফাইলটি খুঁজে পাওয়া যায়নি" else "File not found")
                }
            },
            onSharePdf = { item ->
                val file = File(item.filePath)
                if (file.exists()) {
                    sharePdfFile(context, file)
                } else {
                    showToast(if (isBn) "ফাইলটি খুঁজে পাওয়া যায়নি" else "File not found")
                }
            },
            onDeletePdf = { item ->
                deleteCvHistoryItem(context, item.id)
                historyList = loadCvHistory(context)
                showToast(if (isBn) "হিস্টোরি আইটেম মোছা হয়েছে" else "History item deleted")
            },
            onEditProfile = { item ->
                val profile = profilesList.find { it.profileLabel == item.profileLabel }
                if (profile != null) {
                    updateCvDataState(profile)
                    activeProfileId = profile.id
                    saveActiveProfileId(context, profile.id)
                    showToast(if (isBn) "প্রোফাইল লোড করা হয়েছে" else "Profile loaded")
                } else {
                    showToast(if (isBn) "প্রোফাইল ডেটা পাওয়া যায়নি" else "Profile data not found")
                }
            },
            onClearAllHistory = {
                clearAllCvHistory(context)
                historyList = emptyList()
                showToast(if (isBn) "সমস্ত ইতিহাস মোছা হয়েছে" else "All history cleared")
            }
        )
    }

    if (showSaveProfileDialog) {
        SaveProfileDialog(
            currentLabel = cvData.profileLabel.ifBlank { "${cvData.fullName.ifBlank { "Candidate" }} - Profile" },
            isBn = isBn,
            themeColors = themeColors,
            onDismiss = { showSaveProfileDialog = false },
            onSaveProfile = { newLabel ->
                val existingProfile = profilesList.find { it.profileLabel.trim().equals(newLabel.trim(), ignoreCase = true) }
                
                val targetId = if (existingProfile != null && existingProfile.id.isNotBlank()) {
                    existingProfile.id
                } else if (newLabel.trim().equals(cvData.profileLabel.trim(), ignoreCase = true) && cvData.id.isNotBlank() && !cvData.id.startsWith("profile_")) {
                    cvData.id
                } else {
                    "custom_profile_" + java.util.UUID.randomUUID().toString()
                }

                val updated = cvData.copy(id = targetId, profileLabel = newLabel)
                updateCvDataState(updated)
                activeProfileId = targetId
                saveActiveProfileId(context, targetId)
                showSaveProfileDialog = false
                showToast(if (isBn) "প্রোফাইল সফলভাবে সেভ হয়েছে!" else "Profile saved successfully!")
            }
        )
    }

    if (showProfileManagerDialog) {
        val filteredProfiles = profilesList.filter {
            it.id.startsWith("custom_profile_") ||
            it.id.startsWith("profile_import_") ||
            (!it.id.startsWith("profile_") && it.id.isNotBlank())
        }
        ProfileManagerDialog(
            profilesList = filteredProfiles,
            activeProfileId = activeProfileId,
            isBn = isBn,
            themeColors = themeColors,
            onDismiss = { showProfileManagerDialog = false },
            onSelectProfile = { selectedProfile ->
                activeProfileId = selectedProfile.id
                saveActiveProfileId(context, selectedProfile.id)
                cvData = selectedProfile
                previewRefreshKey++
                showProfileManagerDialog = false
                showToast(if (isBn) "প্রোফাইল লোড ও অটো-ইনপুট করা হয়েছে!" else "Profile loaded & auto-filled!")
            },
            onDeleteProfile = { toDelete ->
                val updatedList = profilesList.filter { it.id != toDelete.id }
                profilesList = updatedList
                saveAllCvProfiles(context, updatedList)
                if (activeProfileId == toDelete.id && updatedList.isNotEmpty()) {
                    activeProfileId = updatedList.first().id
                    saveActiveProfileId(context, activeProfileId)
                    cvData = updatedList.first()
                }
                showToast(if (isBn) "প্রোফাইল মোছা হয়েছে!" else "Profile deleted!")
            },
            onImportPdfResume = {
                showProfileManagerDialog = false
                pdfImportLauncher.launch("application/pdf")
            }
        )
    }

    if (showAiPromptDialog) {
        CvAiPromptDialog(
            title = aiPromptTitle,
            defaultPrompt = aiPromptDefaultText,
            isBn = isBn,
            themeColors = themeColors,
            onDismiss = { showAiPromptDialog = false },
            onGenerate = { promptText ->
                showAiPromptDialog = false
                isAiLoading = true
                aiLoadingMessage = if (isBn) "✨ জেমিনি এআই আপনার কাস্টম প্রম্পট অনুযায়ী লিখছে..." else "✨ Gemini AI is generating content from your prompt..."

                scope.launch {
                    try {
                        val sysPrompt = when (aiPromptTargetField) {
                            "CIRCULAR_MATCH" -> "You are a senior ATS Match consultant. Return ONLY valid JSON: {\"tailoredSummary\": \"...\", \"newSkills\": [\"Skill1\", \"Skill2\"]}"
                            "FRESHER_COMPLETE" -> "You are a professional university career coach and top ATS resume consultant. Generate 4 high-impact resume sections for freshers. Output strictly valid JSON with keys: 'academicProjects', 'internshipsVolunteer', 'leadershipClubs', 'keyCoursework'."
                            else -> "CRITICAL INSTRUCTION: You are an expert HR Manager and professional resume writer. Write ONLY the exact, precise text content requested for the CV field. DO NOT include any conversational text, greetings, explanations, or wrap-up remarks. DO NOT include introductory phrases like \'Here is the objective:\' or \'Certainly!\'. DO NOT use markdown code blocks (```). Your ENTIRE output must be exclusively the raw text to be inserted into the CV, ready to copy-paste. Nothing else."
                        }
                        val resultText = callGeminiAiMultiModal(
                            prompt = promptText,
                            systemInstruction = sysPrompt,
                            imageBytes = activeCircularImageBytes,
                            mimeType = activeCircularImageMime
                        )

                        withContext(Dispatchers.Main) {
                            when (aiPromptTargetField) {
                                "SUMMARY" -> {
                                    updateCvDataState(cvData.copy(summary = resultText.trim()))
                                    showToast(if (isBn) "সামারি আপডেট করা হয়েছে!" else "Summary updated!")
                                }
                                "SKILLS" -> {
                                    val lines = resultText.split("\n")
                                        .map { it.trim() }
                                        .filter { it.isNotBlank() && !it.startsWith("Here") && !it.startsWith("Sure") }
                                    val newItems = lines.map { line ->
                                        val clean = line.removePrefix("•").removePrefix("-").removePrefix("*").removePrefix("▪").trim()
                                        CvSkillItem(name = clean)
                                    }
                                    if (newItems.isNotEmpty()) {
                                        updateCvDataState(cvData.copy(skills = cvData.skills + newItems))
                                        showToast(if (isBn) "${newItems.size}টি স্কিল ফিল্ড যোগ হয়েছে!" else "Added ${newItems.size} skill entries!")
                                    }
                                }
                                "EXPERIENCE" -> {
                                    if (activeAiExperienceIndex in cvData.experiences.indices) {
                                        val list = cvData.experiences.toMutableList()
                                        val currentExp = list[activeAiExperienceIndex]
                                        list[activeAiExperienceIndex] = currentExp.copy(description = resultText.trim())
                                        updateCvDataState(cvData.copy(experiences = list))
                                        showToast(if (isBn) "অভিজ্ঞতার বিবরণী আপডেট হয়েছে!" else "Experience updated!")
                                    }
                                }
                                "FRESHER" -> {
                                    updateCvDataState(cvData.copy(
                                        isFresher = true,
                                        fresherAcademicProjects = resultText.trim()
                                    ))
                                    showToast(if (isBn) "ফ্রেশার প্রজেক্ট সেকশন আপডেট হয়েছে!" else "Fresher section updated!")
                                }
                                "FRESHER_COMPLETE" -> {
                                    try {
                                        val cleanJson = resultText.substringAfter("{").substringBeforeLast("}").let { "{$it}" }
                                        val obj = org.json.JSONObject(cleanJson)
                                        updateCvDataState(cvData.copy(
                                            isFresher = true,
                                            fresherAcademicProjects = obj.optString("academicProjects", cvData.fresherAcademicProjects),
                                            fresherInternshipsVolunteer = obj.optString("internshipsVolunteer", cvData.fresherInternshipsVolunteer),
                                            fresherLeadershipClubs = obj.optString("leadershipClubs", cvData.fresherLeadershipClubs),
                                            fresherKeyCoursework = obj.optString("keyCoursework", cvData.fresherKeyCoursework)
                                        ))
                                        showToast(if (isBn) "ফ্রেশার সেকশনগুলো সফলভাবে এআই দিয়ে প্রস্তুত হয়েছে!" else "Fresher sections generated successfully!")
                                    } catch (e: Exception) {
                                        showToast("Parsing Error: ${e.message}")
                                    }
                                }
                                "CIRCULAR_MATCH" -> {
                                    try {
                                        val cleanJsonStr = resultText.substringAfter("{").substringBeforeLast("}").let { "{$it}" }
                                        val jsonObj = org.json.JSONObject(cleanJsonStr)
                                        val tailoredSummary = jsonObj.optString("tailoredSummary", cvData.summary)

                                        val updatedSkills = cvData.skills.toMutableList()
                                        jsonObj.optJSONArray("newSkills")?.let { arr ->
                                            for (i in 0 until arr.length()) {
                                                val skName = arr.getString(i)
                                                if (updatedSkills.none { it.name.equals(skName, ignoreCase = true) }) {
                                                    updatedSkills.add(CvSkillItem(name = skName, level = "Proficient"))
                                                }
                                            }
                                        }

                                        updateCvDataState(cvData.copy(
                                            summary = tailoredSummary,
                                            skills = updatedSkills
                                        ))
                                        showToast(if (isBn) "সার্কুলার অনুযায়ী প্রোফাইল সফলভাবে অটো-টিউন হয়েছে!" else "Profile auto-tailored to job circular successfully!")
                                    } catch (e: Exception) {
                                        showToast("Parsing Error: ${e.message}")
                                    }
                                }
                                "EDUCATION" -> {
                                    val lines = resultText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                                    val newEduItems = lines.map { line ->
                                        val clean = line.removePrefix("•").removePrefix("-").removePrefix("*").trim()
                                        CvEducationItem(examLevel = "Others", degree = clean, institution = "AI Generated Institution", passingYear = "2024", result = "Pass")
                                    }
                                    if (newEduItems.isNotEmpty()) {
                                        updateCvDataState(cvData.copy(educations = cvData.educations + newEduItems))
                                        showToast(if (isBn) "শিক্ষা সেকশনে ডাটা যোগ হয়েছে!" else "Added education entries!")
                                    }
                                }
                                "EXPERIENCE_GEN" -> {
                                    val lines = resultText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                                    val newExpItems = lines.map { line ->
                                        val clean = line.removePrefix("•").removePrefix("-").removePrefix("*").trim()
                                        CvExperienceItem(role = clean, company = "AI Suggested Organization", startDate = "2023", endDate = "Present")
                                    }
                                    if (newExpItems.isNotEmpty()) {
                                        updateCvDataState(cvData.copy(experiences = cvData.experiences + newExpItems))
                                        showToast(if (isBn) "কাজের অভিজ্ঞতা যোগ হয়েছে!" else "Added work experiences!")
                                    }
                                }
                                "CERTIFICATIONS" -> {
                                    updateCvDataState(cvData.copy(certifications = resultText.trim()))
                                    showToast(if (isBn) "সার্টিফিকেশন আপডেট হয়েছে!" else "Certifications updated!")
                                }
                                "REFERENCES" -> {
                                    updateCvDataState(cvData.copy(references = resultText.trim()))
                                    showToast(if (isBn) "রেফারেন্স আপডেট হয়েছে!" else "References updated!")
                                }
                                                                "SKILLS_SINGLE" -> {
                                    val newList = cvData.skills.toMutableList()
                                    if (activeAiExperienceIndex in newList.indices) {
                                        val old = newList[activeAiExperienceIndex]
                                        newList[activeAiExperienceIndex] = old.copy(name = resultText.trim().substringBefore(":").trim(), description = resultText.trim().substringAfter(":").trim())
                                        updateCvDataState(cvData.copy(skills = newList))
                                        showToast(if (isBn) "স্কিল আপডেট হয়েছে!" else "Skill updated!")
                                    }
                                }
                                "SKILLS_SINGLE" -> {
                                    val newList = cvData.skills.toMutableList()
                                    if (activeAiExperienceIndex in newList.indices) {
                                        val old = newList[activeAiExperienceIndex]
                                        val result = resultText.trim()
                                        val namePart = result.substringBefore(":").trim()
                                        val descPart = result.substringAfter(":").trim()
                                        newList[activeAiExperienceIndex] = old.copy(name = namePart, description = descPart)
                                        updateCvDataState(cvData.copy(skills = newList))
                                        showToast(if (isBn) "স্কিল আপডেট হয়েছে!" else "Skill updated!")
                                    }
                                }
                                "LANGUAGES" -> {
                                    updateCvDataState(cvData.copy(languages = resultText.trim()))
                                    showToast(if (isBn) "ভাষা দক্ষতা আপডেট হয়েছে!" else "Languages updated!")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            showToast("AI Error: ${e.localizedMessage}")
                        }
                    } finally {
                        withContext(Dispatchers.Main) {
                            isAiLoading = false
                        }
                    }
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Header Bar matching the PDF Reader visual motif (circulated in screenshot)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = themeColors.displayText
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBn) "এটিএস সিভি বিল্ডার" else "ATS CV Builder",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    Text(
                        text = cvData.profileLabel.ifBlank { if (isBn) "স্মার্ট এআই এবং প্রফেশনাল টেমপ্লেট" else "Smart AI & Professional Templates" },
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        color = themeColors.displayText.copy(alpha = 0.6f)
                    )
                }

                // Undo Button
                IconButton(
                    onClick = { performUndo() },
                    enabled = undoStack.isNotEmpty(),
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Undo",
                        tint = if (undoStack.isNotEmpty()) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.3f),
                        modifier = Modifier.size(22.dp)
                    )
                }



                Spacer(modifier = Modifier.width(4.dp))

                // Header History Button with count badge
                IconButton(
                    onClick = { showHistoryDialog = true },
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(modifier = Modifier.size(46.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(24.dp)
                        )
                        if (historyList.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 2.dp, y = (-2).dp)
                                    .clip(CircleShape)
                                    .background(Color.Red),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (historyList.size > 9) "9+" else historyList.size.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 10.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Header Profile Icon with saved profiles count badge
                IconButton(
                    onClick = { showProfileManagerDialog = true },
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(modifier = Modifier.size(46.dp), contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Saved Profiles",
                                tint = themeColors.buttonEqualBg,
                                modifier = Modifier.fillMaxSize()
                            )
                            val customCount = profilesList.count {
                                it.id.startsWith("custom_profile_") ||
                                it.id.startsWith("profile_import_") ||
                                (!it.id.startsWith("profile_") && it.id.isNotBlank())
                            }
                            if (customCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = 8.dp, y = (-8).dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF16A34A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                Text(
                                    text = if (customCount > 9) "9+" else customCount.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 10.sp
                                )
                            }
                        }
                    }
                }
            }
            }

            // Beautiful tab controls
            val tabs = if (isBn) {
                listOf("স্মার্ট এডিট", "কাস্টমাইজেশন", "জব ম্যাচ", "প্রিভিউ")
            } else {
                listOf("Smart Edit", "Customization", "Job Match", "Preview")
            }

            val tabIcons = listOf(
                Icons.Default.Edit,
                Icons.Default.Tune,
                Icons.Default.AutoAwesome,
                Icons.Default.Visibility
            )

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = themeColors.cardBg,
                contentColor = themeColors.displayText,
                edgePadding = 12.dp,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = tabIcons[index],
                                contentDescription = null,
                                tint = if (selectedTab == index) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.5f)
                            )
                        },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.7f)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // RENDER ACTIVE SCREEN
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> SmartEditTab(
                        cvData = cvData,
                        onCvDataChange = { updateCvDataState(it) },
                        themeColors = themeColors,
                        isBn = isBn,
                        onRequestAiPrompt = { title, prompt, field, idx -> openAiPrompt(title, prompt, field, idx) }
                    )

                    1 -> CustomizationTab(
                        cvData = cvData,
                        onCvDataChange = { updateCvDataState(it) },
                        themeColors = themeColors,
                        isBn = isBn
                    )

                    2 -> AiJobCircularMatchTab(
                        cvData = cvData,
                        onCvDataChange = { updateCvDataState(it) },
                        themeColors = themeColors,
                        isBn = isBn,
                        onMatchCircularAi = { circularText, imageBytes, imageMime ->
                            if (circularText.isBlank() && imageBytes == null) {
                                showToast(if (isBn) "অনুগ্রহ করে সার্কুলার টেক্সট দিন অথবা ছবি আপলোড করুন!" else "Please provide circular text or pick an image!")
                                return@AiJobCircularMatchTab
                            }
                            updateCvDataState(cvData.copy(targetJobCircular = circularText))

                            val promptBuilder = StringBuilder()
                            promptBuilder.append("Target job circular context:\n")
                            if (circularText.isNotBlank()) {
                                promptBuilder.append("Circular Text: $circularText\n")
                            }
                            if (imageBytes != null) {
                                promptBuilder.append("[An image of the job circular is also attached below for you to read details from]\n")
                            }

                            promptBuilder.append("\nCandidate MBA Profile Summary: ${cvData.summary}\n")
                            promptBuilder.append("Skills: ${cvData.skills.joinToString { it.name }}\n")
                            promptBuilder.append("Current Experiences: ${cvData.experiences.joinToString { "${it.role} at ${it.company}" }}\n")
                            promptBuilder.append("\nTask:\n")
                            promptBuilder.append("1. Rewrite their summary to perfectly align with the circular, emphasizing skills they have that match the circular.\n")
                            promptBuilder.append("2. Suggest 5 additional key skills derived from the circular that match an MBA/Management candidate.\n")
                            promptBuilder.append("Return ONLY a JSON formatted block inside bracket structures, with keys 'tailoredSummary' (string) and 'newSkills' (array of strings).")

                            openAiPrompt(
                                title = if (isBn) "সার্কুলার ম্যাচ এআই প্রম্পট" else "AI Job Circular Match Prompt",
                                defaultPrompt = promptBuilder.toString(),
                                targetField = "CIRCULAR_MATCH",
                                imageBytes = imageBytes,
                                imageMime = imageMime
                            )
                        }
                    )

                    3 -> PreviewAndExportTab(
                        cvData = cvData,
                        onCvDataChange = { updateCvDataState(it) },
                        pdfFile = generatedPdfFile,
                        pdfBitmaps = pdfPreviewBitmaps,
                        isPreviewRendering = isPreviewRendering,
                        themeColors = themeColors,
                        isBn = isBn,
                        onTemplateChange = { newStyle ->
                            updateCvDataState(cvData.copy(templateStyle = newStyle))
                        },
                        onRefreshPreview = {
                            previewRefreshKey++
                        },
                        onDownloadPdf = {
                            val file = generatedPdfFile ?: return@PreviewAndExportTab
                            try {
                                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                val destFile = File(downloadsDir, "CV_${cvData.fullName.replace(" ", "_")}_ATS.pdf")
                                file.copyTo(destFile, overwrite = true)
                                addOrUpdateCvHistory(context, destFile, cvData)
                                historyList = loadCvHistory(context)
                                showToast(if (isBn) "পিডিএফ ডাউনলোড ফোল্ডারে সেভ হয়েছে!" else "PDF saved to Downloads folder!")
                            } catch (e: Exception) {
                                try {
                                    addOrUpdateCvHistory(context, file, cvData)
                                    historyList = loadCvHistory(context)
                                } catch (_: Exception) {}
                                showToast("Saved to App Storage: ${file.name}")
                            }
                        },
                        onSharePdf = {
                            val file = generatedPdfFile ?: return@PreviewAndExportTab
                            try {
                                val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, if (isBn) "সিভি পিডিএফ শেয়ার করুন" else "Share CV PDF"))
                            } catch (e: Exception) {
                                showToast("Share error: ${e.message}")
                            }
                        },
                        onDownloadDocx = {
                            scope.launch(Dispatchers.IO) {
                                val docx = generateCvDocxFile(context, cvData)
                                withContext(Dispatchers.Main) {
                                    downloadDocxFile(context, docx)
                                }
                            }
                        },
                        onShareDocx = {
                            scope.launch(Dispatchers.IO) {
                                val docx = generateCvDocxFile(context, cvData)
                                withContext(Dispatchers.Main) {
                                    shareDocxFile(context, docx)
                                }
                            }
                        },
                        onOpenPdfInAppViewer = {
                            val file = generatedPdfFile ?: return@PreviewAndExportTab
                            viewModel.pdfReaderInitialUri = Uri.fromFile(file)
                            viewModel.pdfReaderInitialName = "CV_${cvData.fullName.replace(" ", "_")}.pdf"
                            viewModel.previousToolType = com.example.data.model.ToolType.ATS_CV_BUILDER
                            viewModel.selectedToolType = com.example.data.model.ToolType.PDF_READER
                        },
                        onSaveProfile = {
                            showSaveProfileDialog = true
                        }
                    )
                }
            }
        }

        // Elegant AI Loading Overlay Window
        if (isAiLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.56f))
                    .pointerInput(Unit) { detectTapGestures { } },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = themeColors.cardBg,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .wrapContentHeight()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = themeColors.buttonEqualBg, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = aiLoadingMessage,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// ================= TAB 0: PROFILE AND PERSONA DETAILS =================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileAndPersonasTab(
    cvData: CvData,
    profilesList: List<CvData>,
    onCvDataChange: (CvData) -> Unit,
    onActiveProfileSelected: (String) -> Unit,
    onAddNewProfile: (String) -> Unit,
    onDeleteProfile: (String) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onOpenPdfInViewer: (CvData) -> Unit,
    onGenerateSummaryAi: () -> Unit,
    isScrollable: Boolean = true,
    isLiveEdit: Boolean = false
) {
    val scrollState = rememberScrollState()
    var newProfileName by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(if (isScrollable) Modifier.verticalScroll(scrollState) else Modifier)
            .padding(14.dp)
    ) {
        // --- MULTI-PROFILE MANAGER HEADER ---
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionCardHeader(
                        title = if (isBn) "সেভ করা প্রোফাইলসমূহ" else "Saved Profiles",
                        icon = Icons.Default.FolderOpen,
                        themeColors = themeColors
                    )
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (isBn) "নতুন প্রোফাইল" else "New Profile", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Auto-save informative hint banner
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = themeColors.buttonEqualBg.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "আপনার সমস্ত তথ্য স্বয়ংক্রিয়ভাবে সেভ (Auto-Save) হয়ে থাকে। এখান থেকে প্রোফাইল নির্বাচন করে সরাসরি তথ্য পরিবর্তন বা অটো-লোড করতে পারবেন।" else "All your profile data is automatically saved. Select any profile above to switch or auto-fill your CV information.",
                            fontSize = 10.sp,
                            color = themeColors.displayText.copy(alpha = 0.8f),
                            lineHeight = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Profiles Horizontal selector
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(profilesList) { profile ->
                        val isActive = profile.id == cvData.id
                        Surface(
                            onClick = { onActiveProfileSelected(profile.id) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isActive) themeColors.buttonEqualBg.copy(alpha = 0.15f) else themeColors.background,
                            border = BorderStroke(
                                width = if (isActive) 1.5.dp else 1.dp,
                                color = if (isActive) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier.width(220.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = profile.profileLabel,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = themeColors.displayText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (profilesList.size > 1) {
                                        IconButton(
                                            onClick = { onDeleteProfile(profile.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Close, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                                Text(
                                    text = profile.jobTitle,
                                    fontSize = 10.sp,
                                    color = themeColors.displayText.copy(alpha = 0.65f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    // Quick launch native PDF Viewer button
                                    IconButton(
                                        onClick = { onOpenPdfInViewer(profile) },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(themeColors.buttonEqualBg.copy(alpha = 0.1f), CircleShape)
                                    ) {
                                        Icon(imageVector = Icons.Default.Visibility, contentDescription = "Quick view", tint = themeColors.buttonEqualBg, modifier = Modifier.size(13.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                CvCustomTextField(
                    label = if (isBn) "সক্রিয় প্রোফাইলের নাম (যেমন: Md. Shariful - Officer Profile)" else "Active Profile Name / Preset Label",
                    value = cvData.profileLabel,
                    onValueChange = { onCvDataChange(cvData.copy(profileLabel = it)) },
                    themeColors = themeColors,
                    isLiveEdit = isLiveEdit,
                    isBn = isBn
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- ACTIVE PROFILE FIELDS ---
        SectionCardHeader(
            title = if (isBn) "ব্যক্তিগত ও যোগাযোগ তথ্য" else "Personal & Contact Details",
            icon = Icons.Default.Person,
            themeColors = themeColors
        )

        Spacer(modifier = Modifier.height(10.dp))

        // --- PROFILE PICTURE CUSTOMIZER ---
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isBn) "সিভি প্রোফাইল ছবি এবং ক্রপ সেটিংস" else "Profile Picture & Interactive Crop",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = themeColors.displayText
                )
                Text(
                    text = if (isBn) "সিভির জন্য ছবি আপলোড করে ইচ্ছামত গোল, গোল-কোণা, বা ষড়ভুজ শেইপ সিলেক্ট করুন এবং ড্র্যাগ বা জুম করে পজিশন ঠিক করুন" else "Upload a photo for your CV, pick your favorite shape, and interactively zoom and drag to adjust the crop position",
                    fontSize = 10.5.sp,
                    color = themeColors.displayText.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                val context = LocalContext.current
                val imagePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    if (uri != null) {
                        try {
                            val inputStream = context.contentResolver.openInputStream(uri)
                            val originalBitmap = BitmapFactory.decodeStream(inputStream)
                            inputStream?.close()

                            if (originalBitmap != null) {
                                // Crop to center square default to keep scale handling simple
                                val minDim = minOf(originalBitmap.width, originalBitmap.height)
                                val cx = (originalBitmap.width - minDim) / 2
                                val cy = (originalBitmap.height - minDim) / 2
                                val squareBmp = Bitmap.createBitmap(originalBitmap, cx, cy, minDim, minDim)
                                val scaledBmp = Bitmap.createScaledBitmap(squareBmp, 300, 300, true)

                                val baos = java.io.ByteArrayOutputStream()
                                scaledBmp.compress(Bitmap.CompressFormat.JPEG, 85, baos)
                                val base64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)

                                onCvDataChange(cvData.copy(
                                    photoBase64 = base64,
                                    photoScale = 1.0f,
                                    photoOffsetX = 0f,
                                    photoOffsetY = 0f
                                ))
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error loading image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Photo Preview with Drag Gesture Crop
                    val decodedBmp = remember(cvData.photoBase64) {
                        if (cvData.photoBase64.isNotBlank()) {
                            try {
                                val bytes = Base64.decode(cvData.photoBase64, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            } catch (_: Exception) {
                                null
                            }
                        } else {
                            null
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.Gray.copy(alpha = 0.1f), shape = when (cvData.photoShape) {
                                "Circle" -> CircleShape
                                "Rounded" -> RoundedCornerShape(12.dp)
                                else -> androidx.compose.ui.graphics.RectangleShape
                            })
                            .border(3.5.dp, Color(cvData.templateStyle.primaryColorHex), shape = when (cvData.photoShape) {
                                "Circle" -> CircleShape
                                "Rounded" -> RoundedCornerShape(12.dp)
                                else -> androidx.compose.ui.graphics.RectangleShape
                            })
                            .clip(shape = when (cvData.photoShape) {
                                "Circle" -> CircleShape
                                "Rounded" -> RoundedCornerShape(12.dp)
                                else -> androidx.compose.ui.graphics.RectangleShape
                            })
                            .pointerInput(cvData.photoScale) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    // Adjust drag offsets based on zoom scale
                                    val bound = 150f * cvData.photoScale
                                    val newX = (cvData.photoOffsetX + dragAmount.x).coerceIn(-bound, bound)
                                    val newY = (cvData.photoOffsetY + dragAmount.y).coerceIn(-bound, bound)
                                    onCvDataChange(cvData.copy(photoOffsetX = newX, photoOffsetY = newY))
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (decodedBmp != null) {
                            Image(
                                bitmap = decodedBmp.asImageBitmap(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = cvData.photoScale,
                                        scaleY = cvData.photoScale,
                                        translationX = cvData.photoOffsetX,
                                        translationY = cvData.photoOffsetY
                                    )
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = null,
                                    tint = themeColors.displayText.copy(alpha = 0.4f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isBn) "ছবি দিন" else "No Photo",
                                    fontSize = 10.sp,
                                    color = themeColors.displayText.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Action buttons & configurations
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = if (isBn) "গ্যালারি" else "Gallery", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            if (cvData.photoBase64.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        onCvDataChange(cvData.copy(
                                            photoBase64 = "",
                                            photoScale = 1.0f,
                                            photoOffsetX = 0f,
                                            photoOffsetY = 0f
                                        ))
                                    },
                                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.6f)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = if (isBn) "বাদ দিন" else "Remove", color = Color.Red.copy(alpha = 0.7f), fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Shape Chips Selection
                        Text(
                            text = if (isBn) "ছবির শেইপ নির্ধারণ করুন:" else "Select Photo Shape:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText.copy(alpha = 0.7f)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            val shapes = listOf("Circle", "Rounded", "Square")
                            shapes.forEach { shapeName ->
                                val isSelected = cvData.photoShape == shapeName
                                val label = when (shapeName) {
                                    "Circle" -> if (isBn) "বৃত্তাকার" else "Circle"
                                    "Rounded" -> if (isBn) "কোণ গোল" else "Rounded"
                                    else -> if (isBn) "চারকোণা" else "Square"
                                }
                                Surface(
                                    onClick = { onCvDataChange(cvData.copy(photoShape = shapeName)) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) themeColors.buttonEqualBg else themeColors.background,
                                    border = BorderStroke(1.dp, if (isSelected) Color.Transparent else themeColors.displayText.copy(alpha = 0.15f)),
                                    modifier = Modifier.padding(2.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 9.5.sp,
                                        color = if (isSelected) Color.White else themeColors.displayText,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (cvData.photoBase64.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Zoom Slider
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = null, tint = themeColors.displayText.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "জুম সাইজ (স্লাইডার):" else "Interactive Zoom Scale:",
                            fontSize = 10.5.sp,
                            color = themeColors.displayText.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Slider(
                            value = cvData.photoScale,
                            onValueChange = { onCvDataChange(cvData.copy(photoScale = it)) },
                            valueRange = 1.0f..3.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = themeColors.buttonEqualBg,
                                activeTrackColor = themeColors.buttonEqualBg,
                                inactiveTrackColor = themeColors.displayText.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${"%.1f".format(cvData.photoScale)}x",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                    }

                    // Reset positioning trigger
                    if (cvData.photoOffsetX != 0f || cvData.photoOffsetY != 0f || cvData.photoScale != 1.0f) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    onCvDataChange(cvData.copy(
                                        photoScale = 1.0f,
                                        photoOffsetX = 0f,
                                        photoOffsetY = 0f
                                    ))
                                },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text(
                                    text = if (isBn) "পজিশন রিসেট" else "Reset Crop & Zoom",
                                    fontSize = 10.sp,
                                    color = themeColors.buttonEqualBg,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        
 
        Spacer(modifier = Modifier.height(8.dp))
 
        CvCustomTextField(
            label = if (isBn) "পূর্ণ নাম" else "Full Name",
            value = cvData.fullName,
            onValueChange = { onCvDataChange(cvData.copy(fullName = it)) },
            themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn,
            placeholderText = if (isBn) "যেমন: মোঃ শরিফুল ইসলাম" else "e.g., Md. Shariful Islam"
        )
 
        Spacer(modifier = Modifier.height(8.dp))
 
        CvCustomTextField(
            label = if (isBn) "কাঙ্ক্ষিত পদবী বা পেশা" else "Job Title / Target Designation",
            value = cvData.jobTitle,
            onValueChange = { onCvDataChange(cvData.copy(jobTitle = it)) },
            themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn,
            placeholderText = if (isBn) "যেমন: বিজনেস অ্যানালিস্ট ও ম্যানেজার" else "e.g., Business Analyst & Manager"
        )
 
        Spacer(modifier = Modifier.height(8.dp))
 
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                CvCustomTextField(
                    label = if (isBn) "ইমেইল" else "Email Address",
                    value = cvData.email,
                    onValueChange = { onCvDataChange(cvData.copy(email = it)) },
                    themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn,
                    placeholderText = if (isBn) "যেমন: shariful@example.com" else "e.g., shariful@example.com"
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                CvCustomTextField(
                    label = if (isBn) "ফোন নম্বর" else "Phone Number",
                    value = cvData.phone,
                    onValueChange = { onCvDataChange(cvData.copy(phone = it)) },
                    themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn,
                    placeholderText = if (isBn) "যেমন: +৮৮০ ১৭১১-২২৩৩৪৪" else "e.g., +880 1711-223344"
                )
            }
        }
 
        Spacer(modifier = Modifier.height(8.dp))
 
        CvCustomTextField(
            label = if (isBn) "ঠিকানা" else "City & Country / Address",
            value = cvData.address,
            onValueChange = { onCvDataChange(cvData.copy(address = it)) },
            themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn,
            placeholderText = if (isBn) "যেমন: উত্তরা, ঢাকা" else "e.g., Uttara, Dhaka, Bangladesh"
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                CvCustomTextField(
                    label = if (isBn) "লিঙ্কডইন প্রোফাইল" else "LinkedIn Profile URL",
                    value = cvData.linkedin,
                    onValueChange = { onCvDataChange(cvData.copy(linkedin = it)) },
                    themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn,
                    placeholderText = if (isBn) "যেমন: linkedin.com/in/shariful" else "e.g., linkedin.com/in/shariful"
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                CvCustomTextField(
                    label = if (isBn) "গিথাব / পোর্টফোলিও" else "GitHub / Portfolio URL",
                    value = cvData.githubOrPortfolio,
                    onValueChange = { onCvDataChange(cvData.copy(githubOrPortfolio = it)) },
                    themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn,
                    placeholderText = if (isBn) "যেমন: portfolio.shariful.com" else "e.g., portfolio.shariful.com"
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- BD CORPORATE STANDARD PERSONAL DETAILS ---
        SectionCardHeader(
            title = if (isBn) "আবশ্যিক ব্যক্তিগত বিবরণী" else "Essential Personal Details",
            icon = Icons.Default.AssignmentInd,
            themeColors = themeColors
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                CvCustomTextField(
                    label = if (isBn) "পিতার নাম" else "Father's Name",
                    value = cvData.fatherName,
                    onValueChange = { onCvDataChange(cvData.copy(fatherName = it)) },
                    themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                CvCustomTextField(
                    label = if (isBn) "মাতার নাম" else "Mother's Name",
                    value = cvData.motherName,
                    onValueChange = { onCvDataChange(cvData.copy(motherName = it)) },
                    themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                CvCustomTextField(
                    label = if (isBn) "ধর্ম" else "Religion",
                    value = cvData.religion,
                    onValueChange = { onCvDataChange(cvData.copy(religion = it)) },
                    themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                CvCustomTextField(
                    label = if (isBn) "রক্তের গ্রুপ" else "Blood Group",
                    value = cvData.bloodGroup,
                    onValueChange = { onCvDataChange(cvData.copy(bloodGroup = it)) },
                    themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        CvCustomTextField(
            label = if (isBn) "বর্তমান ঠিকানা" else "Present Address",
            value = cvData.presentAddress,
            onValueChange = { onCvDataChange(cvData.copy(presentAddress = it)) },
            themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn
        )

        Spacer(modifier = Modifier.height(8.dp))

        CvCustomTextField(
            label = if (isBn) "স্থায়ী ঠিকানা" else "Permanent Address",
            value = cvData.permanentAddress,
            onValueChange = { onCvDataChange(cvData.copy(permanentAddress = it)) },
            themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn
        )

        Spacer(modifier = Modifier.height(18.dp))

        // SUMMARY SECTION WITH ATS AUTO-GENERATE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionCardHeader(
                title = if (isBn) "পেশাগত বিবরণী" else "Professional Summary",
                icon = Icons.Default.Description,
                themeColors = themeColors
            )
            Button(
                onClick = onGenerateSummaryAi,
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                // Explicit high contrast White color applied to resolve the black button issue
                Text(text = if (isBn) "AI দিয়ে লিখুন" else "Generate with AI", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        CvCustomLargeTextField(
            label = "",
            value = cvData.summary,
            onValueChange = { onCvDataChange(cvData.copy(summary = it)) },
            themeColors = themeColors,
            isLiveEdit = isLiveEdit,
            isBn = isBn,
            minLines = 4,
            placeholderText = if (isBn) "আপনার ক্যারিয়ারের সামারি লিখুন অথবা এআই দিয়ে রি-রাইট করুন..." else "Write notes here & use Gemini AI to weave them into a professional corporate summary..."
        )
    }

    // Add Profile Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(text = if (isBn) "নতুন সিভি প্রোফাইল তৈরি করুন" else "Create New CV Profile") },
            text = {
                OutlinedTextField(
                    value = newProfileName,
                    onValueChange = { newProfileName = it },
                    label = { Text(text = if (isBn) "প্রোফাইলের নাম" else "Profile / Resume Label") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddNewProfile(newProfileName)
                        newProfileName = ""
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
                ) {
                    Text(text = if (isBn) "তৈরি করুন" else "Create", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(text = if (isBn) "বাতিল" else "Cancel", color = themeColors.buttonEqualBg)
                }
            }
        )
    }
}

// ================= TAB 1: WORK EXPERIENCES =================

@Composable
private fun ExperienceTab(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onEnhanceBulletAi: (Int) -> Unit,
    onRequestAiPrompt: (title: String, defaultPrompt: String, targetField: String, expIdx: Int) -> Unit = { _, _, _, _ -> },
    onGenerateFresherAi: () -> Unit,
    isScrollable: Boolean = true,
    isLiveEdit: Boolean = false
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(if (isScrollable) Modifier.verticalScroll(scrollState) else Modifier)
            .padding(14.dp)
    ) {
        // --- CANDIDATE TYPE SELECTOR: EXPERIENCED VS FRESHER ---
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isBn) "প্রার্থী ধরণ (অভিজ্ঞ vs ফ্রেশার)" else "Applicant Career Stage",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = themeColors.buttonEqualBg
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        onClick = { onCvDataChange(cvData.copy(isFresher = false)) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (!cvData.isFresher) themeColors.buttonEqualBg else themeColors.background,
                        border = BorderStroke(1.dp, if (!cvData.isFresher) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (isBn) "অভিজ্ঞ পেশাজীবী" else "Experienced",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = if (!cvData.isFresher) Color.White else themeColors.displayText
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        onClick = { onCvDataChange(cvData.copy(isFresher = true)) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (cvData.isFresher) themeColors.buttonEqualBg else themeColors.background,
                        border = BorderStroke(1.dp, if (cvData.isFresher) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (isBn) "🎓 ফ্রেশার / নতুন গ্র্যাজুয়েট" else "🎓 Fresher / Graduate",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = if (cvData.isFresher) Color.White else themeColors.displayText
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (cvData.isFresher) {
            // ================= FRESHER SPECIFIC CV SECTIONS =================
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themeColors.buttonEqualBg.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "ফ্রেশারদের সিভিতে কি থাকা উচিত?" else "What a Fresher CV Needs Most",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = themeColors.displayText
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBn)
                            "ফ্রেশারদের কাজের অভিজ্ঞতা না থাকলেও একাডেমিক প্রজেক্ট, থিসিস, ইন্টার্নশিপ, ভলান্টিয়ারিং, ক্লাব এক্টিভিটি ও প্রাসঙ্গিক কোর্সওয়ার্ক সিভির মূল আকর্ষণ হিসেবে কাজ করে।"
                        else
                            "For freshers, recruiters evaluate Capstone Projects, Internships, Extracurricular Leadership, and Relevant Core Coursework instead of corporate experience.",
                        fontSize = 10.5.sp,
                        color = themeColors.displayText.copy(alpha = 0.75f),
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onGenerateFresherAi,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "এআই দিয়ে ফ্রেশার সেকশন লিখুন" else "Generate Fresher Sections with AI",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Academic & Capstone Projects
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themeColors.cardBg,
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.School, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBn) "১. একাডেমিক প্রজেক্ট ও ক্যাপস্টোন থিসিস" else "1. Academic Projects & Capstone Thesis",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = themeColors.buttonEqualBg
                            )
                        }
                        IconButton(
                            onClick = { onCvDataChange(cvData.copy(fresherAcademicProjects = "")) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Section", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    CvCustomLargeTextField(
                        label = "",
                        value = cvData.fresherAcademicProjects,
                        onValueChange = { onCvDataChange(cvData.copy(fresherAcademicProjects = it)) },
                        themeColors = themeColors,
                        isLiveEdit = isLiveEdit,
                        isBn = isBn,
                        minLines = 3,
                        onAiPrompt = { onRequestAiPrompt("Academic Projects AI", "Generate academic project descriptions for a ${cvData.jobTitle}...", "FRESHER", -1) },
                        placeholderText = "• Automated Sales Pipeline Analysis using Python & PowerBI...\n• FinTech Microfinance Mobile App Case Study..."
                    )
                }
            }

            // 2. Internships & Volunteer Work
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themeColors.cardBg,
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.WorkHistory, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBn) "২. ইন্টার্নশিপ ও সমাজসেবা / ভলান্টিয়ারিং" else "2. Internships & Volunteer Work",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = themeColors.buttonEqualBg
                            )
                        }
                        IconButton(
                            onClick = { onCvDataChange(cvData.copy(fresherInternshipsVolunteer = "")) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Section", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    CvCustomLargeTextField(
                        label = "",
                        value = cvData.fresherInternshipsVolunteer,
                        onValueChange = { onCvDataChange(cvData.copy(fresherInternshipsVolunteer = it)) },
                        themeColors = themeColors,
                        isLiveEdit = isLiveEdit,
                        isBn = isBn,
                        minLines = 3,
                        placeholderText = "• Summer Intern at Apex Logistics (Data Reconciliation)\n• Volunteer Organizer for National Blood Donation Camp..."
                    )
                }
            }

            // 3. Campus Leadership & Extracurricular
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themeColors.cardBg,
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.Groups, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBn) "৩. ক্যাম্পাস নেতৃত্ব ও ক্লাব এক্টিভিটি" else "3. Campus Leadership & Extracurricular",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = themeColors.buttonEqualBg
                            )
                        }
                        IconButton(
                            onClick = { onCvDataChange(cvData.copy(fresherLeadershipClubs = "")) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Section", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    CvCustomLargeTextField(
                        label = "",
                        value = cvData.fresherLeadershipClubs,
                        onValueChange = { onCvDataChange(cvData.copy(fresherLeadershipClubs = it)) },
                        themeColors = themeColors,
                        isLiveEdit = isLiveEdit,
                        isBn = isBn,
                        minLines = 3,
                        placeholderText = "• President / General Secretary at University Business Club\n• Finalist, National Inter-University Case Competition..."
                    )
                }
            }

            // 4. Relevant Coursework
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themeColors.cardBg,
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBn) "৪. প্রাসঙ্গিক কোর্সওয়ার্ক ও কোর বিষয়সমূহ" else "4. Relevant Coursework & Academic Core",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = themeColors.buttonEqualBg
                            )
                        }
                        IconButton(
                            onClick = { onCvDataChange(cvData.copy(fresherKeyCoursework = "")) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Section", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = cvData.fresherKeyCoursework,
                        onValueChange = { onCvDataChange(cvData.copy(fresherKeyCoursework = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 6,
                        placeholder = { Text(text = "Financial Modeling, Strategic Management, Business Analytics, Consumer Behavior, Corporate Finance", fontSize = 11.sp) },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.buttonEqualBg,
                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                            focusedContainerColor = themeColors.cardBg,
                            unfocusedContainerColor = themeColors.cardBg,
                            focusedTextColor = themeColors.displayText,
                            unfocusedTextColor = themeColors.displayText
                        )
                    )
                }
            }
        }

        // ================= WORK EXPERIENCES LIST =================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionCardHeader(
                title = if (cvData.isFresher)
                    (if (isBn) "অতিরিক্ত কাজের অভিজ্ঞতা (${cvData.experiences.size})" else "Optional Practicum / Work (${cvData.experiences.size})")
                else
                    (if (isBn) "কাজের অভিজ্ঞতা (${cvData.experiences.size})" else "Work Experience (${cvData.experiences.size})"),
                icon = Icons.Default.Work,
                themeColors = themeColors
            )
            Spacer(modifier = Modifier.width(6.dp))
            Button(
                onClick = {
                    onRequestAiPrompt(
                        if (isBn) "কাজের অভিজ্ঞতা এআই নির্দেশনা" else "Work Experience AI Prompt",
                        "Generate 3 professional work experience entries for a ${cvData.jobTitle.ifBlank { "Professional" }} profile...",
                        "EXPERIENCE_GEN",
                        -1
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isBn) "AI জেনারেট" else "AI Generate", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = {
                    val newList = cvData.experiences.toMutableList()
                    newList.add(CvExperienceItem())
                    onCvDataChange(cvData.copy(experiences = newList))
                },
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, themeColors.buttonEqualBg),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(text = "+", color = themeColors.buttonEqualBg, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        cvData.experiences.forEachIndexed { index, exp ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themeColors.cardBg,
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "অভিজ্ঞতা #${index + 1}" else "Position #${index + 1}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = themeColors.buttonEqualBg
                        )
                        IconButton(
                            onClick = {
                                val newList = cvData.experiences.toMutableList()
                                newList.removeAt(index)
                                onCvDataChange(cvData.copy(experiences = newList))
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            CvCustomTextField(
                                label = if (isBn) "পদবী" else "Role / Designation",
                                value = exp.role,
                                onValueChange = { r ->
                                    val newList = cvData.experiences.toMutableList()
                                    newList[index] = exp.copy(role = r)
                                    onCvDataChange(cvData.copy(experiences = newList))
                                },
                                themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            CvCustomTextField(
                                label = if (isBn) "কোম্পানির নাম" else "Company Name",
                                value = exp.company,
                                onValueChange = { c ->
                                    val newList = cvData.experiences.toMutableList()
                                    newList[index] = exp.copy(company = c)
                                    onCvDataChange(cvData.copy(experiences = newList))
                                },
                                themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            CvCustomTextField(
                                label = if (isBn) "শুরুর তারিখ" else "Start Date (e.g. Jan 2023)",
                                value = exp.startDate,
                                onValueChange = { s ->
                                    val newList = cvData.experiences.toMutableList()
                                    newList[index] = exp.copy(startDate = s)
                                    onCvDataChange(cvData.copy(experiences = newList))
                                },
                                themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            CvCustomTextField(
                                label = if (isBn) "শেষের তারিখ" else "End Date (or Present)",
                                value = exp.endDate,
                                onValueChange = { e ->
                                    val newList = cvData.experiences.toMutableList()
                                    newList[index] = exp.copy(endDate = e)
                                    onCvDataChange(cvData.copy(experiences = newList))
                                },
                                themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    CvCustomTextField(
                        label = if (isBn) "কর্মস্থল / লোকেশন" else "Work Location (e.g. Dhaka, Bangladesh)",
                        value = exp.location,
                        onValueChange = { loc ->
                            val newList = cvData.experiences.toMutableList()
                            newList[index] = exp.copy(location = loc)
                            onCvDataChange(cvData.copy(experiences = newList))
                        },
                        themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "দায়িত্ব ও অর্জনসমূহ" else "Responsibilities & Key Achievements",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = themeColors.displayText.copy(alpha = 0.8f)
                        )
                        TextButton(
                            onClick = { onEnhanceBulletAi(index) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isBn) "AI রি-রাইট করুন" else "AI Enhance Bullet", color = themeColors.buttonEqualBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedTextField(
                        value = exp.description,
                        onValueChange = { d ->
                            val newList = cvData.experiences.toMutableList()
                            newList[index] = exp.copy(description = d)
                            onCvDataChange(cvData.copy(experiences = newList))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 15,
                        placeholder = { Text(text = "• Conducted business evaluations...\n• Tailored campaign KPIs...\n• Coordinated teams...", fontSize = 11.5.sp) },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.buttonEqualBg,
                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                            focusedContainerColor = themeColors.cardBg,
                            unfocusedContainerColor = themeColors.cardBg,
                            focusedTextColor = themeColors.displayText,
                            unfocusedTextColor = themeColors.displayText
                        )
                    )
                }
            }
        }
    }
}

// ================= TAB 2: EDUCATION AND SKILLS =================

@Composable
private fun EducationAndSkillsTab(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    onRequestAiPrompt: (title: String, defaultPrompt: String, targetField: String, expIdx: Int) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    isScrollable: Boolean = true,
    isLiveEdit: Boolean = false
) {
    val scrollState = rememberScrollState()

    // Standard Job Application Dropdown Options
    val examOptions = listOf(
        "S.S.C / Equivalent",
        "H.S.C / Equivalent",
        "Diploma / Polytechnic",
        "Bachelor / Graduation",
        "Master's / Post Graduation",
        "M.Phil",
        "PhD / Doctorate",
        "Others (ম্যানুয়াল ইনপুট)"
    )

    val subjectOptions = listOf(
        "Science",
        "Business Studies",
        "Commerce",
        "Humanities / Arts",
        "Computer Science & Engineering (CSE)",
        "Electrical & Electronic Engineering (EEE)",
        "Civil Engineering",
        "Mechanical Engineering",
        "Business Administration (BBA/MBA)",
        "Accounting & Information Systems",
        "Finance & Banking",
        "Marketing",
        "Management",
        "Economics",
        "English Literature",
        "Law (LL.B / LL.M)",
        "Medicine / MBBS / BDS",
        "General",
        "Others (ম্যানুয়াল ইনপুট)"
    )

    val instOptions = listOf(
        "Dhaka Board",
        "Chittagong Board",
        "Rajshahi Board",
        "Comilla Board",
        "Jessore Board",
        "Barisal Board",
        "Sylhet Board",
        "Dinajpur Board",
        "Mymensingh Board",
        "Technical Education Board",
        "Madrasah Board",
        "University of Dhaka (DU)",
        "BUET",
        "Jahangirnagar University (JU)",
        "Rajshahi University (RU)",
        "Chittagong University (CU)",
        "North South University (NSU)",
        "BRAC University",
        "Ahsanullah University (AUST)",
        "East West University (EWU)",
        "National University (NU)",
        "Bangladesh Open University (BOU)",
        "Others (ম্যানুয়াল ইনপুট)"
    )

    val yearOptions = (2030 downTo 1980).map { it.toString() } + listOf("Appeared / Studying", "Others (ম্যানুয়াল ইনপুট)")

    val resultSysOptions = listOf(
        "CGPA (Out of 4.0)",
        "GPA (Out of 5.0)",
        "1st Division / Class",
        "2nd Division / Class",
        "3rd Division / Class",
        "Passed / Appeared",
        "Others (ম্যানুয়াল ইনপুট)"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(if (isScrollable) Modifier.verticalScroll(scrollState) else Modifier)
            .padding(14.dp)
    ) {
        // EDUCATION CARD BLOCK
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionCardHeader(
                title = if (isBn) "শিক্ষাগত যোগ্যতা" else "Education Details",
                icon = Icons.Default.School,
                themeColors = themeColors
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = {
                        onRequestAiPrompt(
                            if (isBn) "শিক্ষাগত যোগ্যতা এআই নির্দেশনা" else "Education Details AI Prompt",
                            "Generate my educational background details for ${cvData.jobTitle.ifBlank { "Professional" }} profile...",
                            "EDUCATION",
                            -1
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isBn) "AI জেনারেট" else "AI Generate", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedButton(
                    onClick = {
                        val newList = cvData.educations.toMutableList()
                        newList.add(CvEducationItem(
                            examLevel = "Bachelor / Graduation",
                            subjectMajor = "General",
                            institution = "University of Dhaka (DU)",
                            passingYear = "2022",
                            resultType = "CGPA (Out of 4.0)"
                        ))
                        onCvDataChange(cvData.copy(educations = newList))
                    },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, themeColors.buttonEqualBg),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(text = "+", color = themeColors.buttonEqualBg, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        cvData.educations.forEachIndexed { index, edu ->
            var currentExam by remember(edu.examLevel, edu.degree) {
                mutableStateOf(
                    if (examOptions.contains(edu.examLevel)) edu.examLevel
                    else if (edu.degree.isNotBlank() && examOptions.contains(edu.degree)) edu.degree
                    else if (edu.examLevel == "Others" || edu.degree.isNotBlank()) "Others (ম্যানুয়াল ইনপুট)"
                    else "Bachelor / Graduation"
                )
            }
            var currentSubject by remember(edu.subjectMajor) {
                mutableStateOf(
                    if (subjectOptions.contains(edu.subjectMajor)) edu.subjectMajor
                    else if (edu.subjectMajor.isNotBlank()) "Others (ম্যানুয়াল ইনপুট)"
                    else "General"
                )
            }
            var currentInst by remember(edu.institution) {
                mutableStateOf(
                    if (instOptions.contains(edu.institution)) edu.institution
                    else if (edu.institution.isNotBlank()) "Others (ম্যানুয়াল ইনপুট)"
                    else "Others (ম্যানুয়াল ইনপুট)"
                )
            }
            var currentYear by remember(edu.passingYear) {
                mutableStateOf(
                    if (yearOptions.contains(edu.passingYear)) edu.passingYear
                    else if (edu.passingYear.isNotBlank()) "Others (ম্যানুয়াল ইনপুট)"
                    else "2021"
                )
            }
            var currentResultSys by remember(edu.resultType) {
                mutableStateOf(
                    if (resultSysOptions.contains(edu.resultType)) edu.resultType
                    else if (edu.resultType.isNotBlank()) "Others (ম্যানুয়াল ইনপুট)"
                    else "CGPA (Out of 4.0)"
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themeColors.cardBg,
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Education Entry #${index + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = themeColors.buttonEqualBg)
                        IconButton(
                            onClick = {
                                val newList = cvData.educations.toMutableList()
                                newList.removeAt(index)
                                onCvDataChange(cvData.copy(educations = newList))
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        CvCustomDropdown(
                            label = if (isBn) "পরীক্ষা/ডিগ্রি লেভেল" else "Exam / Degree Level",
                            selectedValue = currentExam,
                            options = examOptions,
                            onValueChange = { selected ->
                                currentExam = selected
                                val newExamVal = if (selected == "Others (ম্যানুয়াল ইনপুট)") "Others" else selected
                                val computedDegree = if (selected == "Others (ম্যানুয়াল ইনপুট)") edu.degree else if (currentSubject != "General" && currentSubject != "Others (ম্যানুয়াল ইনপুট)") "$selected in $currentSubject" else selected
                                val newList = cvData.educations.toMutableList()
                                newList[index] = edu.copy(examLevel = newExamVal, degree = computedDegree)
                                onCvDataChange(cvData.copy(educations = newList))
                            },
                            themeColors = themeColors,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        CvCustomDropdown(
                            label = if (isBn) "বিষয়/গ্রুপ/ডিপার্টমেন্ট" else "Subject / Department",
                            selectedValue = currentSubject,
                            options = subjectOptions,
                            onValueChange = { selected ->
                                currentSubject = selected
                                val newSubVal = if (selected == "Others (ম্যানুয়াল ইনপুট)") "Others" else selected
                                val computedDegree = if (currentExam != "Others (ম্যানুয়াল ইনপুট)" && selected != "General" && selected != "Others (ম্যানুয়াল ইনপুট)") "$currentExam in $selected" else edu.degree
                                val newList = cvData.educations.toMutableList()
                                newList[index] = edu.copy(subjectMajor = newSubVal, degree = computedDegree)
                                onCvDataChange(cvData.copy(educations = newList))
                            },
                            themeColors = themeColors,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (currentExam == "Others (ম্যানুয়াল ইনপুট)") {
                        Spacer(modifier = Modifier.height(6.dp))
                        CvCustomTextField(
                            label = if (isBn) "ম্যানুয়াল ডিগ্রির নাম লিখুন" else "Enter Manual Degree Title",
                            value = edu.degree,
                            onValueChange = { d ->
                                val newList = cvData.educations.toMutableList()
                                newList[index] = edu.copy(degree = d)
                                onCvDataChange(cvData.copy(educations = newList))
                            },
                            themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn
                        )
                    }

                    if (currentSubject == "Others (ম্যানুয়াল ইনপুট)") {
                        Spacer(modifier = Modifier.height(6.dp))
                        CvCustomTextField(
                            label = if (isBn) "ম্যানুয়াল বিষয়ের নাম লিখুন" else "Enter Manual Subject Name",
                            value = if (edu.subjectMajor == "Others") "" else edu.subjectMajor,
                            onValueChange = { sub ->
                                val newList = cvData.educations.toMutableList()
                                newList[index] = edu.copy(subjectMajor = sub)
                                onCvDataChange(cvData.copy(educations = newList))
                            },
                            themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    CvCustomDropdown(
                        label = if (isBn) "বোর্ড / বিশ্ববিদ্যালয় / প্রতিষ্ঠান" else "Board / University / Institution",
                        selectedValue = currentInst,
                        options = instOptions,
                        onValueChange = { selected ->
                            currentInst = selected
                            val instVal = if (selected == "Others (ম্যানুয়াল ইনপুট)") edu.institution else selected
                            val newList = cvData.educations.toMutableList()
                            newList[index] = edu.copy(institution = instVal)
                            onCvDataChange(cvData.copy(educations = newList))
                        },
                        themeColors = themeColors
                    )

                    if (currentInst == "Others (ম্যানুয়াল ইনপুট)") {
                        Spacer(modifier = Modifier.height(6.dp))
                        CvCustomTextField(
                            label = if (isBn) "ম্যানুয়াল প্রতিষ্ঠানের নাম লিখুন" else "Enter Manual Institution Name",
                            value = edu.institution,
                            onValueChange = { inst ->
                                val newList = cvData.educations.toMutableList()
                                newList[index] = edu.copy(institution = inst)
                                onCvDataChange(cvData.copy(educations = newList))
                            },
                            themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        CvCustomDropdown(
                            label = if (isBn) "পাসের বছর" else "Passing Year",
                            selectedValue = currentYear,
                            options = yearOptions,
                            onValueChange = { selected ->
                                currentYear = selected
                                val yrVal = if (selected == "Others (ম্যানুয়াল ইনপুট)") edu.passingYear else selected
                                val newList = cvData.educations.toMutableList()
                                newList[index] = edu.copy(passingYear = yrVal)
                                onCvDataChange(cvData.copy(educations = newList))
                            },
                            themeColors = themeColors,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        CvCustomDropdown(
                            label = if (isBn) "ফলাফল পদ্ধতি" else "Result Type",
                            selectedValue = currentResultSys,
                            options = resultSysOptions,
                            onValueChange = { selected ->
                                currentResultSys = selected
                                val newList = cvData.educations.toMutableList()
                                newList[index] = edu.copy(resultType = selected)
                                onCvDataChange(cvData.copy(educations = newList))
                            },
                            themeColors = themeColors,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (currentYear == "Others (ম্যানুয়াল ইনপুট)") {
                        Spacer(modifier = Modifier.height(6.dp))
                        CvCustomTextField(
                            label = if (isBn) "ম্যানুয়াল বছর/স্ট্যাটাস" else "Enter Manual Passing Year/Status",
                            value = edu.passingYear,
                            onValueChange = { yr ->
                                val newList = cvData.educations.toMutableList()
                                newList[index] = edu.copy(passingYear = yr)
                                onCvDataChange(cvData.copy(educations = newList))
                            },
                            themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    CvCustomTextField(
                        label = if (isBn) "প্রাপ্ত পয়েন্ট / ডিভিশন (যেমন: 3.85)" else "GPA / Score / Division (e.g., 3.85)",
                        value = edu.result,
                        onValueChange = { r ->
                            val newList = cvData.educations.toMutableList()
                            newList[index] = edu.copy(result = r)
                            onCvDataChange(cvData.copy(educations = newList))
                        },
                        themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn,
                        placeholderText = if (isBn) "যেমন: 3.80" else "e.g., 3.80"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // KEY SKILL AND COMPETENCE SECTION BLOCK
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionCardHeader(
                title = if (isBn) "Key Skill and Competence (${cvData.skills.size})" else "Key Skill and Competence (${cvData.skills.size})",
                icon = Icons.Default.Code,
                themeColors = themeColors
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = {
                        onRequestAiPrompt(
                            if (isBn) "কী স্কিল এআই দিয়ে জেনারেট করুন" else "Generate Key Skills with AI",
                            "Generate 6 professional key skills with short descriptions (format: Title: Description) for a ${cvData.jobTitle.ifBlank { "Professional" }} candidate.",
                            "SKILLS",
                            -1
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isBn) "AI জেনারেট" else "AI Generate", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        val newList = cvData.skills.toMutableList()
                        newList.add(CvSkillItem(name = "Skill Title: Description of proficiency..."))
                        onCvDataChange(cvData.copy(skills = newList))
                    },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, themeColors.buttonEqualBg),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(text = "+", color = themeColors.buttonEqualBg, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (isBn) "টিপস: প্রতিটি ফিল্ডে 'টাইটেল: বিবরণ' লিখলে সিভিতে টাইটেলটি অটোমেটিক বোল্ড হয়ে যাবে।" else "Tip: Use 'Title: Description' format in any field to automatically bold the title in CV output.",
            fontSize = 10.5.sp,
            color = themeColors.displayText.copy(alpha = 0.65f),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        cvData.skills.forEachIndexed { index, sk ->
            val fullText = if (sk.description.isNotBlank()) "${sk.name}: ${sk.description}" else sk.name
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = themeColors.cardBg,
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.1f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        CvCustomTextField(
                            label = if (isBn) "কী স্কিল ফিল্ড #${index + 1}" else "Key Skill Entry #${index + 1}",
                            value = fullText,
                            onValueChange = { input ->
                                val newList = cvData.skills.toMutableList()
                                if (input.contains(":")) {
                                    val parts = input.split(":", limit = 2)
                                    newList[index] = sk.copy(name = parts[0].trim(), description = parts[1].trim())
                                } else {
                                    newList[index] = sk.copy(name = input, description = "")
                                }
                                onCvDataChange(cvData.copy(skills = newList))
                            },
                            themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn,
                            placeholderText = if (isBn) "যেমন: Financial Management: Skilled in daily record keeping..." else "e.g., Financial Management: Skilled in daily tracking...",
                            onAiPrompt = { onRequestAiPrompt("Skill Suggestion", "Suggest a skill description for a ${cvData.jobTitle}...", "SKILLS_SINGLE", index) }
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            val newList = cvData.skills.toMutableList()
                            newList.removeAt(index)
                            onCvDataChange(cvData.copy(skills = newList))
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            SectionCardHeader(
                title = if (isBn) "ট্রেনিং ও সার্টিফিকেট" else "Training & Certifications",
                icon = Icons.Default.Verified,
                themeColors = themeColors
            )
            Button(
                onClick = {
                    onRequestAiPrompt(
                        if (isBn) "ট্রেনিং ও সার্টিফিকেট এআই নির্দেশনা" else "Training & Certifications AI Prompt",
                        "Generate a list of 3 relevant professional certifications and training for a ${cvData.jobTitle.ifBlank { "Professional" }} candidate...",
                        "CERTIFICATIONS",
                        -1
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isBn) "AI জেনারেট" else "AI Generate", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = cvData.certifications,
            onValueChange = { onCvDataChange(cvData.copy(certifications = it)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 15,
            placeholder = { Text(text = "• Project Management Professional (PMP) - PMI, 2024\n• Business Intelligence - Coursera, 2023", fontSize = 11.5.sp) },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = themeColors.buttonEqualBg,
                unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                focusedContainerColor = themeColors.cardBg,
                unfocusedContainerColor = themeColors.cardBg,
                focusedTextColor = themeColors.displayText,
                unfocusedTextColor = themeColors.displayText
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            SectionCardHeader(
                title = if (isBn) "রেফারেন্স (References)" else "References",
                icon = Icons.Default.SupervisorAccount,
                themeColors = themeColors
            )
            Button(
                onClick = {
                    onRequestAiPrompt(
                        if (isBn) "রেফারেন্স এআই নির্দেশনা" else "References AI Prompt",
                        "Suggest 2 professional reference placeholders or typical reference format for a ${cvData.jobTitle.ifBlank { "Professional" }} candidate...",
                        "REFERENCES",
                        -1
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isBn) "AI জেনারেট" else "AI Generate", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = cvData.references,
            onValueChange = { onCvDataChange(cvData.copy(references = it)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 10,
            placeholder = { Text(text = "Available upon request.", fontSize = 11.5.sp) },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = themeColors.buttonEqualBg,
                unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                focusedContainerColor = themeColors.cardBg,
                unfocusedContainerColor = themeColors.cardBg,
                focusedTextColor = themeColors.displayText,
                unfocusedTextColor = themeColors.displayText
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isBn) "ভাষাগত দক্ষতা (Languages)" else "Language Fluency",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = themeColors.displayText.copy(alpha = 0.8f)
            )
            TextButton(
                onClick = {
                    onRequestAiPrompt(
                        if (isBn) "ভাষা দক্ষতা এআই নির্দেশনা" else "Language Fluency AI Prompt",
                        "Suggest language fluency levels (e.g., Bengali: Native, English: Professional) for a candidate...",
                        "LANGUAGES",
                        -1
                    )
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isBn) "AI জেনারেট" else "AI Generate", color = themeColors.buttonEqualBg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        CvCustomTextField(
            label = "",
            value = cvData.languages,
            onValueChange = { onCvDataChange(cvData.copy(languages = it)) },
            themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn
        )
    }
}

// ================= TAB 3: AI JOB MATCH WITH IMAGE UPLOAD =================

@Composable
private fun AiJobCircularMatchTab(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onMatchCircularAi: (String, ByteArray?, String) -> Unit,
    isScrollable: Boolean = true
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var circularInputText by remember { mutableStateOf(cvData.targetJobCircular) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            try {
                context.contentResolver.openInputStream(uri).use { stream ->
                    selectedImageBitmap = BitmapFactory.decodeStream(stream)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(if (isScrollable) Modifier.verticalScroll(scrollState) else Modifier)
            .padding(14.dp)
    ) {
        SectionCardHeader(
            title = if (isBn) "জব সার্কুলার অনুযায়ী এআই কাস্টমাইজেশন" else "AI Job Circular Match & Tayloring",
            icon = Icons.Default.AutoAwesome,
            themeColors = themeColors
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (isBn)
                "যে চাকরির জন্য আবেদন করতে চান তার সার্কুলার টেক্সট পেস্ট করুন অথবা ক্যামেরা/গ্যালারি থেকে সরাসরি সার্কুলার ইমেজের একটি ছবি দিন! জেমিনি এআই সার্কুলারটি বিশ্লেষণ করে আপনার সিভির সামারি ও কি-ওয়ার্ড কাস্টমাইজ করবে।"
            else
                "Paste the job circular text OR select an image of the circular from your gallery. Gemini Multi-modal AI will analyze the requirements from both text/image to optimize your summary and key skills.",
            fontSize = 12.sp,
            color = themeColors.displayText.copy(alpha = 0.75f),
            lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Text input area
        OutlinedTextField(
            value = circularInputText,
            onValueChange = { circularInputText = it },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            maxLines = 15,
            placeholder = { Text(text = if (isBn) "জব ডেসক্রিপশন পেস্ট করুন..." else "Paste Job circular / description requirements text here...", fontSize = 12.5.sp) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = themeColors.buttonEqualBg,
                unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                focusedContainerColor = themeColors.cardBg,
                unfocusedContainerColor = themeColors.cardBg,
                focusedTextColor = themeColors.displayText,
                unfocusedTextColor = themeColors.displayText
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Image input options
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isBn) "সার্কুলার ছবি ইনপুট করুন" else "Upload Circular Image (Camera/Gallery)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        border = BorderStroke(1.dp, themeColors.buttonEqualBg),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isBn) "গ্যালারি থেকে ছবি নিন" else "Choose Image", color = themeColors.buttonEqualBg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (selectedImageUri != null) {
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = {
                                selectedImageUri = null
                                selectedImageBitmap = null
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Cancel, contentDescription = "Clear image", tint = Color.Red.copy(alpha = 0.8f))
                        }
                    }
                }

                if (selectedImageBitmap != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Image(
                        bitmap = selectedImageBitmap!!.asImageBitmap(),
                        contentDescription = "Circular thumbnail",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, themeColors.displayText.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                var imageBytes: ByteArray? = null
                var mimeType = "image/jpeg"
                if (selectedImageUri != null) {
                    try {
                        context.contentResolver.openInputStream(selectedImageUri!!).use { stream ->
                            imageBytes = stream?.readBytes()
                        }
                        mimeType = context.contentResolver.getType(selectedImageUri!!) ?: "image/jpeg"
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                onMatchCircularAi(circularInputText, imageBytes, mimeType)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
        ) {
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            // Explicit White text color to ensure contrast
            Text(
                text = if (isBn) "জেমিনি এআই দিয়ে সিভি টিউন করুন" else "Tailor Resume with Gemini AI",
                color = Color.White,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ================= TAB 4: PREVIEW & EXPORT =================

@Composable
private fun PreviewAndExportTab(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    pdfFile: File?,
    pdfBitmaps: List<Bitmap>,
    isPreviewRendering: Boolean,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onTemplateChange: (CvTemplateStyle) -> Unit,
    onRefreshPreview: () -> Unit,
    onDownloadPdf: () -> Unit,
    onSharePdf: () -> Unit,
    onDownloadDocx: () -> Unit,
    onShareDocx: () -> Unit,
    onOpenPdfInAppViewer: () -> Unit,
    onSaveProfile: () -> Unit = {},
    isScrollable: Boolean = true
) {
    val scrollState = rememberScrollState()
    var previewMode by remember { mutableStateOf("PREVIEW") } // "PREVIEW" or "LIVE_EDIT"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(if (isScrollable) Modifier.verticalScroll(scrollState) else Modifier)
            .padding(14.dp)
    ) {
        // Template Selector Grid (At least 5-7 templates)
        SectionCardHeader(
            title = if (isBn) "সিভি টেমপ্লেট নির্বাচন" else "Select ATS Resume Template",
            icon = Icons.Default.Style,
            themeColors = themeColors
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal elegant picker for all 10 templates
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(CvTemplateStyle.values()) { style ->
                val isSelected = cvData.templateStyle == style
                Surface(
                    onClick = { onTemplateChange(style) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) themeColors.buttonEqualBg.copy(alpha = 0.15f) else themeColors.cardBg,
                    border = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.width(205.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(Color(style.primaryColorHex))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn) style.titleBn else style.titleEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = themeColors.displayText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = themeColors.buttonEqualBg,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = style.description,
                            fontSize = 9.5.sp,
                            color = themeColors.displayText.copy(alpha = 0.7f),
                            lineHeight = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- SIGNATURE LINE OPTION CARD ---
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Draw,
                        contentDescription = null,
                        tint = themeColors.buttonEqualBg,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isBn) "আবেদনকারীর স্বাক্ষর লাইন যোগ করুন" else "Include Signature Placeholder",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        Text(
                            text = if (isBn) "প্রিন্ট করার পর হাতে স্বাক্ষর করার জন্য নিচে স্বাক্ষর ও তারিখের ফাঁকা লাইন যুক্ত রাখবে" else "Adds official physical signature line & date at the bottom for printing",
                            fontSize = 10.sp,
                            color = themeColors.displayText.copy(alpha = 0.65f),
                            lineHeight = 13.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = cvData.showSignatureLine,
                    onCheckedChange = { checked ->
                        onCvDataChange(cvData.copy(showSignatureLine = checked))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = themeColors.buttonEqualBg
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- SAVE AS PROFILE BUTTON ---
        OutlinedButton(
            onClick = onSaveProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, Color(0xFF059669))
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isBn) "প্রোফাইল হিসেবে সেভ করে রাখুন" else "Save Current Input as Profile",
                color = Color(0xFF059669),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- NEW ACTION: OPEN IN NATIVE APP PDF READER ---
        Button(
            onClick = onOpenPdfInAppViewer,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
        ) {
            Icon(imageVector = Icons.Default.ChromeReaderMode, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isBn) "পিডিএফ রিডার দিয়ে প্রিভিউ করুন" else "Preview in Native PDF Viewer App",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // HD PDF Export Action Buttons
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onDownloadPdf,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Download PDF", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = onSharePdf,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, themeColors.buttonEqualBg),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isBn) "Share PDF" else "Share PDF", color = themeColors.buttonEqualBg, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // HD DOCX Export Action Buttons
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onDownloadDocx,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            ) {
                Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Download Docx", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = onShareDocx,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF16A34A)),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isBn) "Share DOCX" else "Share DOCX", color = Color(0xFF16A34A), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Mode Selector Chips
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                onClick = { previewMode = "PREVIEW" },
                shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                color = if (previewMode == "PREVIEW") themeColors.buttonEqualBg else themeColors.cardBg,
                border = BorderStroke(1.dp, themeColors.buttonEqualBg),
                modifier = Modifier.height(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(if (isBn) "প্রিভিউ" else "Preview", color = if (previewMode == "PREVIEW") Color.White else themeColors.buttonEqualBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            Surface(
                onClick = { previewMode = "LIVE_EDIT" },
                shape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
                color = if (previewMode == "LIVE_EDIT") themeColors.buttonEqualBg else themeColors.cardBg,
                border = BorderStroke(1.dp, themeColors.buttonEqualBg),
                modifier = Modifier.height(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(if (isBn) "লাইভ এডিট" else "Live Edit", color = if (previewMode == "LIVE_EDIT") Color.White else themeColors.buttonEqualBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        if (previewMode == "LIVE_EDIT") {
            // Embedded Quick Edit Forms
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themeColors.cardBg,
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(if (isBn) "মার্জিন, প্যাডিং ও স্পেসিং" else "Margin, Padding & Spacing", fontSize = 14.sp, color = themeColors.displayText, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(if (isBn) "মার্জিন (Margin): ${cvData.customMargin.toInt()}" else "Margin: ${cvData.customMargin.toInt()}", fontSize = 12.sp, color = themeColors.displayText, fontWeight = FontWeight.Bold)
                    androidx.compose.material3.Slider(
                        value = cvData.customMargin,
                        onValueChange = { onCvDataChange(cvData.copy(customMargin = it)) },
                        valueRange = 20f..80f,
                        steps = 60,
                        colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = themeColors.buttonEqualBg, activeTrackColor = themeColors.buttonEqualBg)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(if (isBn) "সেকশন স্পেসিং (Section Spacing): ${cvData.sectionSpacing.toInt()}" else "Section Spacing: ${cvData.sectionSpacing.toInt()}", fontSize = 12.sp, color = themeColors.displayText, fontWeight = FontWeight.Bold)
                    androidx.compose.material3.Slider(
                        value = cvData.sectionSpacing,
                        onValueChange = { onCvDataChange(cvData.copy(sectionSpacing = it)) },
                        valueRange = 2f..24f,
                        steps = 22,
                        colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = themeColors.buttonEqualBg, activeTrackColor = themeColors.buttonEqualBg)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(if (isBn) "আইটেম স্পেসিং (Item Spacing): ${cvData.itemSpacing.toInt()}" else "Item Spacing: ${cvData.itemSpacing.toInt()}", fontSize = 12.sp, color = themeColors.displayText, fontWeight = FontWeight.Bold)
                    androidx.compose.material3.Slider(
                        value = cvData.itemSpacing,
                        onValueChange = { onCvDataChange(cvData.copy(itemSpacing = it)) },
                        valueRange = 0f..16f,
                        steps = 16,
                        colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = themeColors.buttonEqualBg, activeTrackColor = themeColors.buttonEqualBg)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(if (isBn) "লাইন স্পেসিং (Line Spacing): ${String.format("%.2f", cvData.customLineSpacing)}" else "Line Spacing: ${String.format("%.2f", cvData.customLineSpacing)}", fontSize = 12.sp, color = themeColors.displayText, fontWeight = FontWeight.Bold)
                    androidx.compose.material3.Slider(
                        value = cvData.customLineSpacing,
                        onValueChange = { onCvDataChange(cvData.copy(customLineSpacing = it)) },
                        valueRange = 0.8f..2.0f,
                        steps = 24,
                        colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = themeColors.buttonEqualBg, activeTrackColor = themeColors.buttonEqualBg)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            ProfileAndPersonasTab(
                cvData = cvData,
                profilesList = emptyList(),
                onCvDataChange = onCvDataChange,
                isLiveEdit = true,
                onActiveProfileSelected = {},
                onAddNewProfile = {},
                onDeleteProfile = {},
                themeColors = themeColors,
                isBn = isBn,
                onOpenPdfInViewer = {},
                onGenerateSummaryAi = {},
                isScrollable = false
            )
            Spacer(modifier = Modifier.height(16.dp))
            ExperienceTab(
                cvData = cvData,
                onCvDataChange = onCvDataChange,
                isLiveEdit = true,
                themeColors = themeColors,
                isBn = isBn,
                onEnhanceBulletAi = {},
                onGenerateFresherAi = {},
                isScrollable = false
            )
            Spacer(modifier = Modifier.height(16.dp))
            EducationAndSkillsTab(
                cvData = cvData,
                onCvDataChange = onCvDataChange,
                isLiveEdit = true,
                onRequestAiPrompt = { _, _, _, _ -> },
                themeColors = themeColors,
                isBn = isBn,
                isScrollable = false
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    onRefreshPreview()
                    // Auto switch back to preview is optional, maybe just refresh and stay in LIVE_EDIT so they see changes in background?
                    // Actually, the preview is shown conditionally. Wait, if previewMode is "LIVE_EDIT", the preview is hidden!
                    // Let's modify so `else { // Vector PDF Live Canvas Screen }` becomes `} // Vector PDF Live Canvas Screen` so it ALWAYS shows the preview below the edit controls!
                    onRefreshPreview()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isBn) "প্রিভিউ রিফ্রেশ করুন" else "Refresh Preview & Apply Changes", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Vector PDF Live Canvas Screen
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionCardHeader(
                title = if (isBn) "লাইভ ভেক্টর প্রিভিউ (${pdfBitmaps.size}টি পেজ)" else "Live Vector Preview (${pdfBitmaps.size} Page${if (pdfBitmaps.size > 1) "s" else ""})",
                icon = Icons.Default.Visibility,
                themeColors = themeColors
            )
            OutlinedButton(
                onClick = onRefreshPreview,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, themeColors.buttonEqualBg),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                if (isPreviewRendering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.5.dp,
                        color = themeColors.buttonEqualBg
                    )
                } else {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = themeColors.buttonEqualBg, modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isBn) (if (isPreviewRendering) "আপডেট হচ্ছে..." else "রিফ্রেশ") else (if (isPreviewRendering) "Updating..." else "Refresh"),
                    color = themeColors.buttonEqualBg,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (pdfBitmaps.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                pdfBitmaps.forEachIndexed { pageIdx, bmp ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                        shadowElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF3F4F6))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isBn) "পেজ ${pageIdx + 1} / ${pdfBitmaps.size}" else "Page ${pageIdx + 1} of ${pdfBitmaps.size}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF374151)
                                )
                            }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "A4 Page Vector Preview",
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (isPreviewRendering && pageIdx == 0) {
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(10.dp),
                                                strokeWidth = 1.2.dp,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isBn) "লাইভ আপডেট হচ্ছে" else "Updating preview...",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = themeColors.buttonEqualBg)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isBn) "প্রিভিউ তৈরি হচ্ছে..." else "Rendering vector preview...",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onRefreshPreview) {
                        Text(
                            text = if (isBn) "পুনরায় চেষ্টা করুন" else "Tap to Reload",
                            color = themeColors.buttonEqualBg,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ================= SHARABLE REUSABLE HELPER UI =================

@Composable
private fun SectionCardHeader(
    title: String,
    icon: ImageVector,
    themeColors: CalculatorThemeColors
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = themeColors.buttonEqualBg,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.displayText
        )
    }
}

@Composable
private fun CvCustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    themeColors: CalculatorThemeColors,
    placeholderText: String? = null,
    isLiveEdit: Boolean = false,
    isBn: Boolean = false,
    onAiPrompt: (() -> Unit)? = null
) {
    var draftValue by remember(value) { mutableStateOf(value) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            if (label.isNotBlank()) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = themeColors.displayText.copy(alpha = 0.8f)
                )
            }
            if (onAiPrompt != null) {
                IconButton(onClick = onAiPrompt, modifier = Modifier.size(20.dp)) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Help", tint = themeColors.buttonEqualBg, modifier = Modifier.size(14.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = draftValue,
            onValueChange = { 
                draftValue = it
                if (!isLiveEdit) {
                    onValueChange(it)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = placeholderText?.let { { Text(it, color = themeColors.displayText.copy(alpha = 0.4f), fontSize = 12.sp) } },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = themeColors.buttonEqualBg,
                unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                focusedContainerColor = themeColors.cardBg,
                unfocusedContainerColor = themeColors.cardBg,
                focusedTextColor = themeColors.displayText,
                unfocusedTextColor = themeColors.displayText
            )
        )

        if (isLiveEdit && draftValue != value) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    onClick = { showConfirmDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    modifier = Modifier.height(30.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBn) "সেভ করুন" else "Save", fontSize = 10.sp, color = Color.White)
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(if (isBn) "পরিবর্তন নিশ্চিত করুন" else "Confirm Changes") },
            text = { Text(if (isBn) "'$label' পরিবর্তন করতে চান?" else "Are you sure you want to save changes to '$label'?") },
            confirmButton = {
                TextButton(onClick = {
                    onValueChange(draftValue)
                    showConfirmDialog = false
                }) {
                    Text(if (isBn) "হ্যাঁ" else "Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(if (isBn) "না" else "No")
                }
            }
        )
    }
}

@Composable
private fun CvCustomLargeTextField(
    onAiPrompt: (() -> Unit)? = null,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    themeColors: CalculatorThemeColors,
    placeholderText: String? = null,
    isLiveEdit: Boolean = false,
    isBn: Boolean = false,
    minLines: Int = 3
) {
    var draftValue by remember(value) { mutableStateOf(value) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            if (label.isNotBlank()) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = themeColors.displayText.copy(alpha = 0.8f)
                )
            }
            if (onAiPrompt != null) {
                IconButton(onClick = onAiPrompt, modifier = Modifier.size(22.dp)) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Help", tint = themeColors.buttonEqualBg, modifier = Modifier.size(16.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = draftValue,
            onValueChange = { 
                draftValue = it
                if (!isLiveEdit) {
                    onValueChange(it)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = minLines,
            maxLines = 15,
            placeholder = placeholderText?.let { { Text(it, color = themeColors.displayText.copy(alpha = 0.4f), fontSize = 12.sp) } },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = themeColors.buttonEqualBg,
                unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                focusedContainerColor = themeColors.cardBg,
                unfocusedContainerColor = themeColors.cardBg,
                focusedTextColor = themeColors.displayText,
                unfocusedTextColor = themeColors.displayText
            )
        )

        if (isLiveEdit && draftValue != value) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    onClick = { showConfirmDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    modifier = Modifier.height(30.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBn) "সেভ করুন" else "Save", fontSize = 10.sp, color = Color.White)
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(if (isBn) "পরিবর্তন নিশ্চিত করুন" else "Confirm Changes") },
            text = { Text(if (isBn) "এই তথ্যটি পরিবর্তন করতে চান?" else "Are you sure you want to save changes?") },
            confirmButton = {
                TextButton(onClick = {
                    onValueChange(draftValue)
                    showConfirmDialog = false
                }) {
                    Text(if (isBn) "হ্যাঁ" else "Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(if (isBn) "না" else "No")
                }
            }
        )
    }
}

// ================= TAB 3: CUSTOMIZATION & FLEXIBILITY =================

@Composable
private fun CustomizationTab(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    isScrollable: Boolean = true,
    isLiveEdit: Boolean = false
) {
    val scrollState = rememberScrollState()

    val defaultSectionList = listOf(
        "SUMMARY" to if (isBn) "ক্যারিয়ার অবজেক্টিভ / সামারি" else "Summary / Objective",
        "EXPERIENCE" to if (isBn) "কাজের অভিজ্ঞতা" else "Work Experience",
        "EDUCATION" to if (isBn) "শিক্ষাগত যোগ্যতা" else "Education Details",
        "SKILLS" to if (isBn) "প্রফেশনাল স্কিলস" else "Skills & Competencies",
        "PROJECTS" to if (isBn) "প্রজেক্টসমূহ" else "Projects & Initiatives",
        "CERTIFICATIONS" to if (isBn) "প্রশিক্ষণ ও সার্টিফিকেট" else "Training & Certifications",
        "LANGUAGES" to if (isBn) "ভাষাগত দক্ষতা" else "Language Fluency",
        "CUSTOM_SECTIONS" to if (isBn) "কাস্টম সেকশনসমূহ" else "Custom Sections",
        "PERSONAL_INFO" to if (isBn) "ব্যক্তিগত তথ্যাবলী" else "Personal Information",
        "REFERENCES" to if (isBn) "রেফারেন্স" else "References"
    )

    val currentOrder = if (cvData.sectionOrder.isNotEmpty()) {
        cvData.sectionOrder
    } else {
        defaultSectionList.map { it.first }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(if (isScrollable) Modifier.verticalScroll(scrollState) else Modifier)
            .padding(14.dp)
    ) {
        // --- 1. SECTION ORDER & VISIBILITY ---
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                SectionCardHeader(
                    title = if (isBn) "সেকশন পজিশন ও হাইড/শো কন্ট্রোল" else "Section Ordering & Visibility",
                    icon = Icons.Default.Tune,
                    themeColors = themeColors
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isBn) "উপরে/নিচে অ্যারো বাটন দিয়ে যে কোন সেকশন সিভিতে কোন পজিশনে থাকবে তা ঠিক করুন এবং হাইড/শো সুইচ অন-অফ করুন।"
                    else "Use up/down arrows to reorder CV sections and toggle switches to show or hide any section.",
                    fontSize = 11.sp,
                    color = themeColors.displayText.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                currentOrder.forEachIndexed { index, secKey ->
                    val secLabel = defaultSectionList.find { it.first == secKey }?.second ?: secKey
                    val isHidden = cvData.hiddenSections.contains(secKey)

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isHidden) themeColors.background.copy(alpha = 0.5f) else themeColors.background,
                        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.1f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Up button
                                IconButton(
                                    onClick = {
                                        if (index > 0) {
                                            val mutableOrder = currentOrder.toMutableList()
                                            val temp = mutableOrder[index]
                                            mutableOrder[index] = mutableOrder[index - 1]
                                            mutableOrder[index - 1] = temp
                                            onCvDataChange(cvData.copy(sectionOrder = mutableOrder))
                                        }
                                    },
                                    enabled = index > 0,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Move Up",
                                        tint = if (index > 0) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.2f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Down button
                                IconButton(
                                    onClick = {
                                        if (index < currentOrder.size - 1) {
                                            val mutableOrder = currentOrder.toMutableList()
                                            val temp = mutableOrder[index]
                                            mutableOrder[index] = mutableOrder[index + 1]
                                            mutableOrder[index + 1] = temp
                                            onCvDataChange(cvData.copy(sectionOrder = mutableOrder))
                                        }
                                    },
                                    enabled = index < currentOrder.size - 1,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Move Down",
                                        tint = if (index < currentOrder.size - 1) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.2f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = secLabel,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isHidden) themeColors.displayText.copy(alpha = 0.4f) else themeColors.displayText
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = !isHidden,
                                    onCheckedChange = { checked ->
                                        val mutableHidden = cvData.hiddenSections.toMutableSet()
                                        if (checked) {
                                            mutableHidden.remove(secKey)
                                        } else {
                                            mutableHidden.add(secKey)
                                        }
                                        onCvDataChange(cvData.copy(hiddenSections = mutableHidden.toList()))
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = themeColors.buttonEqualBg,
                                        uncheckedThumbColor = themeColors.displayText.copy(alpha = 0.5f),
                                        uncheckedTrackColor = themeColors.background
                                    ),
                                    modifier = Modifier.scale(0.75f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- 2. FONT SIZE & BULLET FORMATTING CONTROL ---
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                SectionCardHeader(
                    title = if (isBn) "ফন্ট সাইজ ও বুলেট পয়েন্ট স্টাইল" else "Font Scaling & Bullet Style",
                    icon = Icons.Default.FormatSize,
                    themeColors = themeColors
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Font Scale Selector
                Text(
                    text = if (isBn) "পিডিএফ ফন্ট স্কেলিং (অক্ষরের সাইজ)" else "PDF Font Scale",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
                Spacer(modifier = Modifier.height(6.dp))

                val fontScaleOptions = listOf(
                    "COMPACT" to (if (isBn) "কমপ্যাক্ট (৮৮%)" else "Compact (88%)"),
                    "STANDARD" to (if (isBn) "স্ট্যান্ডার্ড (১০০%)" else "Standard (100%)"),
                    "COMFORTABLE" to (if (isBn) "আরামদায়ক (১১২%)" else "Comfortable (112%)"),
                    "LARGE" to (if (isBn) "বড় ফন্ট (১২৫%)" else "Large (125%)")
                )

                fontScaleOptions.chunked(2).forEach { rowOpts ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowOpts.forEach { (key, label) ->
                            val isSelected = cvData.fontScale == key
                            Surface(
                                onClick = { onCvDataChange(cvData.copy(fontScale = key)) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) themeColors.buttonEqualBg else themeColors.background,
                                border = BorderStroke(1.dp, if (isSelected) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.15f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 3.dp)
                            ) {
                                Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else themeColors.displayText
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bullet Point Options
                Text(
                    text = if (isBn) "বুলেট পয়েন্ট স্টাইল (বুলেট vs কমা vs কাস্টম)" else "Bullet Point Style",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
                Spacer(modifier = Modifier.height(6.dp))

                val bulletOptions = listOf(
                    "BULLET" to (if (isBn) "• স্ট্যান্ডার্ড বুলেট" else "• Standard Bullet"),
                    "DASH" to (if (isBn) "- ড্যাশ" else "- Dash"),
                    "SQUARE" to (if (isBn) "▪ স্কয়ার" else "▪ Square"),
                    "DIAMOND" to (if (isBn) "◆ ডায়মন্ড" else "◆ Diamond"),
                    "COMMA" to (if (isBn) "কমা সেপারেটেড (,)" else "Comma Separated (,)"),
                    "PIPE" to (if (isBn) "পাইপ সেপারেটেড (|)" else "Pipe Separated (|)"),
                    "NONE" to (if (isBn) "কোন চিহ্ন নেই" else "None")
                )

                bulletOptions.chunked(2).forEach { rowOpts ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowOpts.forEach { (key, label) ->
                            val isSelected = cvData.bulletStyle == key
                            Surface(
                                onClick = { onCvDataChange(cvData.copy(bulletStyle = key)) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) themeColors.buttonEqualBg else themeColors.background,
                                border = BorderStroke(1.dp, if (isSelected) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.15f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 3.dp)
                            ) {
                                Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else themeColors.displayText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- 3. ICON CONTROLS ---
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                SectionCardHeader(
                    title = if (isBn) "আইকন ব্যবহার কন্ট্রোল" else "Icon Usage Options",
                    icon = Icons.Default.SmartButton,
                    themeColors = themeColors
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isBn) "যোগাযোগের তথ্যে আইকন দেখান (ফোন, ইমেইল, ইত্যাদি)" else "Show icons in contact details (Phone, Email, LinkedIn)",
                        fontSize = 11.5.sp,
                        color = themeColors.displayText,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = cvData.showContactIcons,
                        onCheckedChange = { onCvDataChange(cvData.copy(showContactIcons = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = themeColors.buttonEqualBg
                        ),
                        modifier = Modifier.scale(0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isBn) "সেকশন হেডার শিরোনামে আইকন দেখান" else "Show icons beside section headers",
                        fontSize = 11.5.sp,
                        color = themeColors.displayText,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = cvData.showSectionIcons,
                        onCheckedChange = { onCvDataChange(cvData.copy(showSectionIcons = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = themeColors.buttonEqualBg
                        ),
                        modifier = Modifier.scale(0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- 4. CUSTOM SECTIONS ADDER ---
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionCardHeader(
                        title = if (isBn) "ইচ্ছামত কাস্টম সেকশন যোগ করুন (${cvData.customSections.size})" else "Custom Sections (${cvData.customSections.size})",
                        icon = Icons.Default.AddCircle,
                        themeColors = themeColors
                    )
                    OutlinedButton(
                        onClick = {
                            val newList = cvData.customSections.toMutableList()
                            newList.add(CvCustomSectionItem(title = "অতিরিক্ত অর্জন / Awards", content = "• Won 1st place in National Competition..."))
                            onCvDataChange(cvData.copy(customSections = newList))
                        },
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, themeColors.buttonEqualBg),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = "+", color = themeColors.buttonEqualBg, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                cvData.customSections.forEachIndexed { index, cSec ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = themeColors.background,
                        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.1f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBn) "কাস্টম সেকশন #${index + 1}" else "Custom Section #${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = themeColors.buttonEqualBg
                                )
                                IconButton(
                                    onClick = {
                                        val newList = cvData.customSections.toMutableList()
                                        newList.removeAt(index)
                                        onCvDataChange(cvData.copy(customSections = newList))
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            CvCustomTextField(
                                label = if (isBn) "সেকশন শিরোনাম (Title)" else "Section Title",
                                value = cSec.title,
                                onValueChange = { t ->
                                    val newList = cvData.customSections.toMutableList()
                                    newList[index] = cSec.copy(title = t)
                                    onCvDataChange(cvData.copy(customSections = newList))
                                },
                                themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (isBn) "সেকশন কন্টেন্ট / বিবরণ" else "Section Details & Description",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = themeColors.displayText.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = cSec.content,
                                onValueChange = { c ->
                                    val newList = cvData.customSections.toMutableList()
                                    newList[index] = cSec.copy(content = c)
                                    onCvDataChange(cvData.copy(customSections = newList))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                maxLines = 6,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = themeColors.buttonEqualBg,
                                    unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                                    focusedContainerColor = themeColors.cardBg,
                                    unfocusedContainerColor = themeColors.cardBg,
                                    focusedTextColor = themeColors.displayText,
                                    unfocusedTextColor = themeColors.displayText
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

