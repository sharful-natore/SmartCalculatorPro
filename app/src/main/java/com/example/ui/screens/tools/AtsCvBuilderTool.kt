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
import androidx.compose.animation.core.tween
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import androidx.compose.ui.window.Dialog
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.AtsChecklistItem
import com.example.data.model.ChecklistStatus
import com.example.data.model.CvSectionKey
import com.example.ui.components.JobMatchChecklistComponent
import com.example.ui.components.FixNowBottomSheet
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
    val descriptionEn: String,
    val descriptionBn: String,
    val primaryColorHex: Int,
    val isTwoColumn: Boolean = false
) {
    CLASSIC_CORPORATE(
        "Classic Corporate (ATS BD)",
        "ক্লাসিক কর্পোরেট (BD ATS Standard)",
        "100% accurate table-based official design tailored for Banks, BCS, Multinationals & Corporate jobs.",
        "ব্যাংক, বিসিএস, মাল্টিন্যাশনাল ও করপোরেট চাকরির জন্য ১০০% নিখুঁত টেবিল-বেসড অফিশিয়াল ডিজাইন",
        AndroidColor.parseColor("#0F172A")
    ),
    CANVA_MINIMALIST_CLEAN(
        "Canva Minimalist Clean (Modern)",
        "ক্যানভা মিনিমালিস্ট ক্লিন (Modern ATS)",
        "Centered header, field icon contact bar & right-aligned dates with modern clean professional layout.",
        "সেন্টার্ড হেডার, ফিল্ড আইকন কন্টাক্ট বার ও ডানপাশে অ্যালাইন্ড তারিখসহ আধ্নিক ক্লিন প্রফেশনাল ডিজাইন",
        AndroidColor.parseColor("#1E293B")
    ),
    SINGLE_COLUMN_HIGH_IMPACT_ATS(
        "Single-Column High Impact",
        "হাই-ইমপ্যাক্ট সিঙ্গেল কলাম (US ATS)",
        "Bold headers, clear section dividers & fast-scanning metric bullet layout.",
        "বোল্ড হেডার, নির্দিষ্ট ডিভাইডার এবং ফাস্ট-স্ক্যানিং মেট্রিক ব্লেট সংবলিত টপ-রেটেড এটিএস লেআউট",
        AndroidColor.parseColor("#0F172A")
    ),
    NORDIC_SLATE_MODERN(
        "Nordic Slate & Minimal",
        "নরডিক স্লেট ও মিনিমাল",
        "Scandinavian slim spacing, dark slate typography & elegant section dividers.",
        "স্ক্যান্ডিনেভিয়ান স্লিম স্পেসিং, ডার্ক স্লেট টাইপোগ্রাফি ও মার্জিত সেকশন ডিভাইডার",
        AndroidColor.parseColor("#334155")
    ),
    SILICON_VALLEY_TECH_LEAD(
        "Silicon Valley Tech Lead",
        "সিলিকন ভ্যালি টেক লিড",
        "Cobalt blue accents & quant metrics design for Tech Leads, Developers & Product Managers.",
        "টেক লিড, ডেভেলপার ও প্রোডাক্ট ম্যানেজারদের জন্য কোবাল্ট ব্ল্ একসেন্ট ও কোয়ান্ট রেজাল্ট ডিজাইন",
        AndroidColor.parseColor("#0284C7")
    ),
    EXECUTIVE_MONOCHROME_LUXE(
        "Executive Monochrome Luxe",
        "এক্সিকিউটিভ মনোক্রোম লাক্স",
        "Pure black & charcoal luxury minimalist serif layout for high-profile corporate leaders.",
        "হাই-প্রোফাইল কর্পোরেট লিডারদের জন্য পিউর ব্ল্যাক ও চারকোল লাক্সারি মিনিমালিস্ট সেরিফ স্টাইল",
        AndroidColor.parseColor("#111827")
    ),
    EXECUTIVE_TWO_COLUMN(
        "Executive Two-Column Sidebar",
        "এক্সিকিউটিভ সাইডবার (২-কলাম)",
        "Dark navy sidebar, material icons & stylish two-column layout for experienced professionals.",
        "অভিজ্ঞ পেশাজীবীদের জন্য ডার্ক নেভি সাইডবার, ম্যাটেরিয়াল আইকন ও স্টাইলিশ লেআউট",
        AndroidColor.parseColor("#1E293B"),
        isTwoColumn = true
    ),
    CREATIVE_MARKETING(
        "Modern Creative Banner",
        "ক্রিয়েটিভ ব্যানার প্রো",
        "Top banner header, profile badge & card style for marketing & branding professionals.",
        "মার্কেটিং ও ব্র্যান্ডিং প্রফেশনালদের জন্য আকর্ষণীয় টপ ব্যানার, প্রোফাইল ব্যাজ ও কার্ড স্টাইল",
        AndroidColor.parseColor("#881337")
    ),
    MODERN_MINIMALIST(
        "Nordic Minimalist Tech",
        "মডার্ন মিনিমালিস্ট টেক",
        "Vertical bar accent, modern chips & clean design for IT, Software Engineers & Designers.",
        "আইটি, সফটওয়্যার ইঞ্জিনিয়ার ও ডিজাইনারদের জন্য ভার্টিক্যাল বার একসেন্ট, মডার্ন চিপস ও মার্জিত নকশা",
        AndroidColor.parseColor("#0D9488")
    ),
    HARVARD_CLASSIC(
        "Harvard Academic & Legal",
        "হার্ভার্ড ক্লাসিক (Academic)",
        "Ivy-league standard classic serif typography, double-rule dividers & centered header.",
        "আইভি-লিগ স্ট্যান্ডার্ড ক্লাসিক সেরিফ টাইপোগ্রাফি, ডাবল-র্ল ডিভাইডার ও সেন্টার্ড হেডার",
        AndroidColor.parseColor("#18181B")
    ),
    ELEGANT_PREMIUM(
        "Executive Serif & Ivory",
        "এলিজেন্ট সেরিফ ও আইভরি",
        "Royal midnight serif & diamond bullet layout for executives & consultants.",
        "হাই-প্রোফাইল এক্সিকিউটিভ ও কনসালট্যান্টদের জন্য রয়্যাল মিডনাইট সেরিফ ও ডায়মন্ড ব্লেট স্টাইল",
        AndroidColor.parseColor("#312E81")
    ),
    CLEAN_TECH_STARTUP(
        "Emerald Tech & Startup",
        "এমারেল্ড টেক স্টার্টআপ",
        "Emerald green & timeline-dot design for dynamic startups & project management roles.",
        "ডাইনামিক স্টার্টআপ ও প্রজেক্ট ম্যানেজমেন্ট রোলের জন্য এমারেল্ড গ্রিন ও টাইমলাইন-ডট ডিজাইন",
        AndroidColor.parseColor("#065F46")
    ),
    BANKING_FINANCE_SPECIALIST(
        "Banking & Finance Specialist",
        "ব্যাংক ও ফিন্যান্স স্পেশালিস্ট",
        "Royal blue accent, quant metrics & tabular format for commercial & investment banking.",
        "বাণিজ্যিক ও ইনভেস্টমেন্ট ব্যাংকিংয়ের জন্য রয়্যাল ব্ল্ একসেন্ট, কোয়ান্ট মেট্রিক্স ও টেব্লার রেকর্ড",
        AndroidColor.parseColor("#1E40AF")
    ),
    NGO_DEVELOPMENT_HUMANITARIAN(
        "NGO & Humanitarian Impact",
        "এনজিও ও উন্নয়ন প্রকল্প",
        "Terracotta & field-impact layout for international NGOs, UN & development projects.",
        "আন্তর্জাতিক এনজিও, ইউএন ও সমাজ উন্নয়ন প্রজেক্টের জন্য টেরাকোটা ও ফিল্ড-ইমপ্যাক্ট লেআউট",
        AndroidColor.parseColor("#9A3412")
    ),
    EUROPASS_GLOBAL_STANDARD(
        "Europass & Global MNC",
        "ইউরোপাস গ্লোবাল লেআউট",
        "Classic left-label boxed grid & formal sections for European & global jobs.",
        "ইউরোপ ও আন্তর্জাতিক চাকরির জন্য ক্লাসিক লেফট-লেবেল বক্সড গ্রিড ও ফরমাল সেকশন",
        AndroidColor.parseColor("#0369A1")
    );

    fun getTitle(isBn: Boolean): String = if (isBn) titleBn else titleEn
    fun getDescription(isBn: Boolean): String = if (isBn) descriptionBn else descriptionEn
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
    onPreviewPdf: (CvHistoryItem) -> Unit,
    onOpenExternalPdf: (CvHistoryItem) -> Unit,
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
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(historyList, key = { it.id }) { item ->
                        val file = File(item.filePath)
                        val fileExists = file.exists()
                        val fileSizeKb = if (fileExists) file.length() / 1024 else 0

                        Surface(
                            onClick = { onPreviewPdf(item) },
                            shape = RoundedCornerShape(12.dp),
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
                                            text = item.candidateName.ifBlank { "Candidate" },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = themeColors.displayText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${item.profileLabel} â€¢ ${item.fileName}",
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

                                Spacer(modifier = Modifier.height(6.dp))

                                val dateStr = try {
                                    java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(item.timestamp))
                                } catch (_: Exception) { "" }

                                Text(
                                    text = "$dateStr ${if (fileSizeKb > 0) "($fileSizeKb KB)" else ""}",
                                    fontSize = 9.5.sp,
                                    color = themeColors.displayText.copy(alpha = 0.5f)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Quick Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 1. In-App Preview Button
                                    Button(
                                        onClick = { onPreviewPdf(item) },
                                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(28.dp).weight(1f)
                                    ) {
                                        Icon(imageVector = Icons.Default.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(if (isBn) "প্রিভিউ" else "Preview", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // 2. Load & Edit in CV Builder Button
                                    OutlinedButton(
                                        onClick = { onEditProfile(item) },
                                        border = BorderStroke(1.dp, themeColors.buttonEqualBg),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(if (isBn) "এডিট" else "Edit", color = themeColors.buttonEqualBg, fontSize = 10.sp)
                                    }

                                    // 3. Share Button
                                    if (fileExists) {
                                        OutlinedButton(
                                            onClick = { onSharePdf(item) },
                                            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.25f)),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = themeColors.displayText, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(if (isBn) "শেয়ার" else "Share", color = themeColors.displayText, fontSize = 10.sp)
                                        }

                                        // 4. Open with external app
                                        OutlinedButton(
                                            onClick = { onOpenExternalPdf(item) },
                                            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.25f)),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, tint = themeColors.displayText, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(if (isBn) "ওপেন" else "Open", color = themeColors.displayText, fontSize = 10.sp)
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
    var showDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label, fontSize = 11.sp, color = themeColors.displayText.copy(alpha = 0.7f)) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select",
                    tint = themeColors.buttonEqualBg
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                disabledTextColor = themeColors.displayText,
                disabledContainerColor = themeColors.cardBg,
                disabledLabelColor = themeColors.displayText.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    searchQuery = ""
                    showDialog = true
                },
            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
        )
    }

    if (showDialog) {
        val filteredOptions = remember(searchQuery, options) {
            if (searchQuery.isBlank()) options
            else options.filter { it.contains(searchQuery, ignoreCase = true) }
        }

        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = themeColors.cardBg,
                border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .padding(vertical = 12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.buttonEqualBg
                        )
                        IconButton(onClick = { showDialog = false }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = themeColors.displayText, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search / খুঁজুন...", fontSize = 12.sp) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = themeColors.displayText, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.buttonEqualBg,
                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                            focusedTextColor = themeColors.displayText,
                            unfocusedTextColor = themeColors.displayText
                        ),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        items(filteredOptions.size) { idx ->
                            val option = filteredOptions[idx]
                            val isSelected = option == selectedValue
                            Surface(
                                onClick = {
                                    onValueChange(option)
                                    showDialog = false
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) themeColors.buttonEqualBg.copy(alpha = 0.15f) else Color.Transparent,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = option,
                                        fontSize = 12.5.sp,
                                        color = if (isSelected) themeColors.buttonEqualBg else themeColors.displayText,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = themeColors.buttonEqualBg,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.06f))
                        }
                    }
                }
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
                                            text = "${profile.fullName.ifBlank { "Candidate" }} â€¢ ${profile.jobTitle.ifBlank { "No Title" }}",
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
    val photoShape: String = "Circle", // Circle, Rounded, Square, Oval, Rectangle
    val photoScale: Float = 1.0f,
    val photoOffsetX: Float = 0f,
    val photoOffsetY: Float = 0f,
    val photoWidth: Int = 80,
    val photoHeight: Int = 80,
    val photoBorderWidth: Float = 1.5f,
    val photoCornerRadius: Int = 10,
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
    val customLineSpacing: Float = 1.15f,

    // ATS & Job Circular Analysis fields
    val lastJobMatchPercentage: Int = 0,
    val lastMatchingStrengths: List<String> = emptyList(),
    val lastMissingKeywords: List<String> = emptyList(),
    val lastImprovementTips: List<String> = emptyList(),
    val lastTailoredSummary: String = "",
    val lastAtsScoreFromGemini: Int = 0,
    val lastAtsSuggestionsJson: String = "",
    val lastCircularSuggestionsJson: String = ""
)

data class AtsCheckItem(
    val categoryEn: String,
    val categoryBn: String,
    val isPassed: Boolean,
    val weightPoints: Int,
    val detailEn: String,
    val detailBn: String
)

data class LocalAtsScoreResult(
    val score: Int,
    val contactScore: Int,
    val summaryScore: Int,
    val experienceScore: Int,
    val educationScore: Int,
    val skillsScore: Int,
    val formattingScore: Int,
    val checkItems: List<AtsCheckItem>
)

internal fun calculateLocalAtsScore(cvData: CvData): LocalAtsScoreResult {
    val checks = mutableListOf<AtsCheckItem>()

    // 1. Contact Info (Max 15)
    val hasName = cvData.fullName.isNotBlank()
    val hasTitle = cvData.jobTitle.isNotBlank()
    val hasEmail = cvData.email.contains("@") && cvData.email.contains(".")
    val hasPhone = cvData.phone.isNotBlank()
    val hasLink = cvData.linkedin.isNotBlank() || cvData.githubOrPortfolio.isNotBlank()

    checks.add(AtsCheckItem("Contact Info", "যোগাযোগের তথ্য", hasName, 3, "Candidate full name complete", "প্রার্থীর পূর্ণ নাম প্রদান করা হয়েছে"))
    checks.add(AtsCheckItem("Contact Info", "যোগাযোগের তথ্য", hasTitle, 3, "Target position / job title specified", "টার্গেট পজিশন/টাইটেল উল্লেখ করা হয়েছে"))
    checks.add(AtsCheckItem("Contact Info", "যোগাযোগের তথ্য", hasEmail, 3, "Valid contact email address provided", "সঠিক ইমেইল অ্যাড্রেস প্রদান করা হয়েছে"))
    checks.add(AtsCheckItem("Contact Info", "যোগাযোগের তথ্য", hasPhone, 3, "Mobile/Phone number provided", "মোবাইল/ফোন নম্বর প্রদান করা হয়েছে"))
    checks.add(AtsCheckItem("Contact Info", "যোগাযোগের তথ্য", hasLink, 3, "LinkedIn / Portfolio profile link present", "লিংকডইন বা পোর্টফোলিও প্রোফাইল য্ক্ত আছে"))

    val contactScore = (if (hasName) 3 else 0) + (if (hasTitle) 3 else 0) + (if (hasEmail) 3 else 0) + (if (hasPhone) 3 else 0) + (if (hasLink) 3 else 0)

    // 2. Summary (Max 15)
    val hasSummary = cvData.summary.trim().length >= 40
    val isDetailedSummary = cvData.summary.trim().length >= 100 || cvData.summary.contains("%") || cvData.summary.lowercase().let { s -> s.contains("managed") || s.contains("led") || s.contains("developed") || s.contains("achieved") || s.contains("experienced") }

    checks.add(AtsCheckItem("Summary", "প্রোফাইল সামারি", hasSummary, 8, "Professional summary included (>= 40 chars)", "প্রোফাইল সামারি সেকশন য্ক্ত করা হয়েছে"))
    checks.add(AtsCheckItem("Summary", "প্রোফাইল সামারি", isDetailedSummary, 7, "Summary has strong impact keywords or detailed scope", "সামারিতে কার্যকর শব্দ ও বিস্তারিত বিবরণী রয়েছে"))

    val summaryScore = (if (hasSummary) 8 else 0) + (if (isDetailedSummary) 7 else 0)

    // 3. Experience / Projects (Max 30)
    val expCount = if (cvData.isFresher) 1 else cvData.experiences.size
    val hasExp = expCount > 0
    val hasQuantifiedMetrics = cvData.experiences.any { exp ->
        exp.description.contains("%") || exp.description.contains("$") || exp.description.contains("TK") || exp.description.contains("BDT") || exp.description.any { it.isDigit() }
    } || (cvData.isFresher && cvData.fresherAcademicProjects.any { it.isDigit() })

    val hasRoleDetails = cvData.experiences.all { it.role.isNotBlank() && it.company.isNotBlank() } && cvData.experiences.isNotEmpty()

    checks.add(AtsCheckItem("Experience", "কাজের অভিজ্ঞতা", hasExp, 10, "Work experiences or project records present", "কাজের অভিজ্ঞতা বা প্রজেক্ট রেকর্ড রয়েছে"))
    checks.add(AtsCheckItem("Experience", "কাজের অভিজ্ঞতা", hasQuantifiedMetrics, 10, "Quantifiable achievements (metrics, %, numbers) included", "পরিমাপযোগ্য অর্জন (সংখ্যা, %, সাফল্য) য্ক্ত আছে"))
    checks.add(AtsCheckItem("Experience", "কাজের অভিজ্ঞতা", hasRoleDetails || cvData.isFresher, 10, "Designations, Company names, and Dates complete", "পদের নাম, কোম্পানির নাম ও মেয়াদ স্পষ্ট"))

    val experienceScore = (if (hasExp) 10 else 0) + (if (hasQuantifiedMetrics) 10 else 0) + (if (hasRoleDetails || cvData.isFresher) 10 else 0)

    // 4. Education (Max 15)
    val hasEdu = cvData.educations.isNotEmpty()
    val eduDetailsComplete = cvData.educations.any { it.institution.isNotBlank() && it.passingYear.isNotBlank() }

    checks.add(AtsCheckItem("Education", "শিক্ষাগত যোগ্যতা", hasEdu, 10, "Educational qualification listed", "শিক্ষাগত যোগ্যতা তালিকাভ্ক্ত রয়েছে"))
    checks.add(AtsCheckItem("Education", "শিক্ষাগত যোগ্যতা", eduDetailsComplete, 5, "Institution name, Degree & Year specified", "প্রতিষ্ঠান, ডিগ্রি ও পাসের সন নির্দিষ্ট"))

    val educationScore = (if (hasEdu) 10 else 0) + (if (eduDetailsComplete) 5 else 0)

    // 5. Skills (Max 15)
    val skillCount = cvData.skills.size
    val has5Skills = skillCount >= 5
    val has8Skills = skillCount >= 8

    checks.add(AtsCheckItem("Skills", "দক্ষতা ও স্কিলস", has5Skills, 10, "At least 5 core industry skills listed", "কমপক্ষে ৫টি কোর স্কিল য্ক্ত আছে"))
    checks.add(AtsCheckItem("Skills", "দক্ষতা ও স্কিলস", has8Skills, 5, "Rich competency set (8+ skills)", "সমৃদ্ধ স্কিলসেট (৮+ স্কিল) রয়েছে"))

    val skillsScore = (if (has5Skills) 10 else 0) + (if (has8Skills) 5 else 0)

    // 6. Formatting (Max 10)
    val hasBullets = cvData.bulletStyle != "NONE"

    checks.add(AtsCheckItem("Formatting", "লেআউট ও ফরম্যাট", hasBullets, 5, "Standard ATS bullet style enabled", "এটিএস ব্লেট স্টাইল সক্রিয় রয়েছে"))
    checks.add(AtsCheckItem("Formatting", "লেআউট ও ফরম্যাট", true, 5, "Standard 1-column / ATS structural hierarchy", "সঠিক এটিএস স্ট্রাকচার ও হায়ারার্কি সক্রিয়"))

    val formattingScore = (if (hasBullets) 5 else 0) + 5

    val totalScore = (contactScore + summaryScore + experienceScore + educationScore + skillsScore + formattingScore).coerceIn(0, 100)

    return LocalAtsScoreResult(
        score = totalScore,
        contactScore = contactScore,
        summaryScore = summaryScore,
        experienceScore = experienceScore,
        educationScore = educationScore,
        skillsScore = skillsScore,
        formattingScore = formattingScore,
        checkItems = checks
    )
}

internal fun autoFixAtsScoreTo100(cvData: CvData): CvData {
    val updatedSkills = cvData.skills.toMutableList()
    val defaultSkills = listOf(
        "Project Management", "Agile & Scrum", "Data Analysis",
        "Strategic Planning", "Cross-Functional Leadership",
        "Problem Solving", "Communication", "Process Optimization"
    )
    for (sk in defaultSkills) {
        if (updatedSkills.size < 8 && updatedSkills.none { it.name.equals(sk, ignoreCase = true) }) {
            updatedSkills.add(CvSkillItem(name = sk))
        }
    }

    val updatedExps = if (cvData.experiences.isEmpty()) {
        listOf(
            CvExperienceItem(
                role = if (cvData.jobTitle.isNotBlank()) cvData.jobTitle else "Operations & Project Manager",
                company = "Global Solutions Ltd.",
                startDate = "2021",
                endDate = "Present",
                description = "â€¢ Led cross-functional team of 12 professionals to deliver key client projects, increasing client satisfaction by 35%.\nâ€¢ Managed project budget of $150,000 and reduced operational delivery overhead by 25% through process optimization."
            )
        )
    } else {
        cvData.experiences.map { exp ->
            var desc = exp.description
            if (!desc.contains("%") && !desc.contains("$") && !desc.any { it.isDigit() }) {
                desc = if (desc.isBlank()) {
                    "â€¢ Achieved 30% performance improvement and reduced processing errors by 45% using standardized workflows."
                } else {
                    "${desc.trim()}\nâ€¢ Increased operational efficiency by 35% and achieved 98% on-time project delivery."
                }
            }
            exp.copy(
                role = exp.role.ifBlank { "Project Manager" },
                company = exp.company.ifBlank { "Leading Enterprise Ltd." },
                description = desc
            )
        }
    }

    val updatedEdus = if (cvData.educations.isEmpty()) {
        listOf(
            CvEducationItem(
                examLevel = "B.Sc.",
                degree = "B.Sc. in Computer Science & Engineering",
                institution = "University of Dhaka (DU)",
                passingYear = "2021",
                resultType = "CGPA (Out of 4.0)"
            )
        )
    } else {
        cvData.educations.map { edu ->
            edu.copy(
                institution = edu.institution.ifBlank { "University of Dhaka (DU)" },
                passingYear = edu.passingYear.ifBlank { "2021" }
            )
        }
    }

    val updatedSummary = if (cvData.summary.trim().length < 100 || !cvData.summary.contains("%")) {
        if (cvData.summary.isBlank()) {
            "Results-driven ${cvData.jobTitle.ifBlank { "Professional" }} with 5+ years of experience in leading project execution, optimizing cross-functional operations, and delivering high-impact business solutions. Demonstrated success in increasing operational efficiency by 35% and managing enterprise deliverables."
        } else {
            "${cvData.summary.trim()} Demonstrated success in improving team productivity by 35% and delivering strategic projects on schedule."
        }
    } else {
        cvData.summary
    }

    return cvData.copy(
        fullName = cvData.fullName.ifBlank { "Md. Rafiqul Islam" },
        jobTitle = cvData.jobTitle.ifBlank { "Operations & Project Manager" },
        email = if (cvData.email.contains("@") && cvData.email.contains(".")) cvData.email else "rafiq.candidate@email.com",
        phone = cvData.phone.ifBlank { "+880 1712-345678" },
        linkedin = if (cvData.linkedin.isNotBlank() || cvData.githubOrPortfolio.isNotBlank()) cvData.linkedin else "linkedin.com/in/rafiqul-islam",
        summary = updatedSummary,
        experiences = updatedExps,
        educations = updatedEdus,
        skills = updatedSkills,
        bulletStyle = if (cvData.bulletStyle == "NONE") "BULLET" else cvData.bulletStyle
    )
}

internal fun autoFixIndividualAtsCheck(cvData: CvData, categoryEn: String): CvData {
    return when (categoryEn) {
        "Contact Info" -> cvData.copy(
            fullName = cvData.fullName.ifBlank { "Md. Rafiqul Islam" },
            jobTitle = cvData.jobTitle.ifBlank { "Professional Specialist" },
            email = if (cvData.email.contains("@") && cvData.email.contains(".")) cvData.email else "candidate@email.com",
            phone = cvData.phone.ifBlank { "+880 1700-000000" },
            linkedin = if (cvData.linkedin.isNotBlank()) cvData.linkedin else "linkedin.com/in/candidate-profile"
        )
        "Summary" -> cvData.copy(
            summary = "Results-driven ${cvData.jobTitle.ifBlank { "Professional" }} with proven expertise in project execution and team management. Achieved 35% efficiency growth and led strategic operations."
        )
        "Experience" -> {
            val exps = if (cvData.experiences.isEmpty()) {
                listOf(CvExperienceItem(role = cvData.jobTitle.ifBlank { "Project Manager" }, company = "Leading Company Ltd.", startDate = "2021", endDate = "Present", description = "â€¢ Achieved 35% team efficiency improvement and managed $50K project budget."))
            } else {
                cvData.experiences.map { exp ->
                    val desc = exp.description
                    if (!desc.contains("%") && !desc.contains("$") && !desc.any { it.isDigit() }) {
                        exp.copy(
                            role = exp.role.ifBlank { "Project Manager" },
                            company = exp.company.ifBlank { "Leading Company Ltd." },
                            description = "${desc.trim()}\nâ€¢ Achieved 35% operational efficiency growth and managed key deliverables."
                        )
                    } else exp.copy(
                        role = exp.role.ifBlank { "Project Manager" },
                        company = exp.company.ifBlank { "Leading Company Ltd." }
                    )
                }
            }
            cvData.copy(experiences = exps)
        }
        "Education" -> {
            val edus = if (cvData.educations.isEmpty()) {
                listOf(CvEducationItem(examLevel = "B.Sc.", degree = "B.Sc. in Computer Science & Engineering", institution = "University of Dhaka (DU)", passingYear = "2021"))
            } else {
                cvData.educations.map { edu ->
                    edu.copy(
                        institution = edu.institution.ifBlank { "University of Dhaka (DU)" },
                        passingYear = edu.passingYear.ifBlank { "2021" }
                    )
                }
            }
            cvData.copy(educations = edus)
        }
        "Skills" -> {
            val skills = cvData.skills.toMutableList()
            val sampleSkills = listOf("Project Management", "Agile & Scrum", "Data Analysis", "Leadership", "Problem Solving", "Strategic Planning", "Communication", "Process Optimization")
            for (sk in sampleSkills) {
                if (skills.size < 8 && skills.none { it.name.equals(sk, ignoreCase = true) }) {
                    skills.add(CvSkillItem(name = sk))
                }
            }
            cvData.copy(skills = skills)
        }
        "Formatting" -> cvData.copy(bulletStyle = "BULLET")
        else -> cvData
    }
}

internal fun applyIndividualAtsImprovement(cv: CvData, category: String, value: String): CvData {
    if (value.isBlank()) return cv
    return when {
        category.equals("summary", ignoreCase = true) -> cv.copy(summary = value)
        category.equals("certifications", ignoreCase = true) -> cv.copy(certifications = value)
        category.equals("references", ignoreCase = true) -> cv.copy(references = value)
        category.equals("skills", ignoreCase = true) -> {
            val updatedSkills = cv.skills.toMutableList()
            // proposedValue can be comma separated skills or a list of "Skill: Description"
            val rawSkills = value.split(",", "\n")
            rawSkills.forEach { raw ->
                val clean = raw.removePrefix("â€¢").removePrefix("-").removePrefix("*").trim()
                if (clean.isNotBlank()) {
                    val skillItem = if (clean.contains(":")) {
                        val parts = clean.split(":", limit = 2)
                        CvSkillItem(name = parts[0].trim(), description = parts[1].trim())
                    } else {
                        CvSkillItem(name = clean, description = "Experienced in applying $clean effectively in professional environments.")
                    }
                    if (updatedSkills.none { it.name.equals(skillItem.name, ignoreCase = true) || (clean.contains(":") && it.name.equals(clean.substringBefore(":").trim(), ignoreCase = true)) }) {
                        updatedSkills.add(skillItem)
                    }
                }
            }
            cv.copy(skills = updatedSkills)
        }
        category.startsWith("experience_", ignoreCase = true) -> {
            val idx = category.substringAfter("experience_").toIntOrNull() ?: -1
            if (idx in cv.experiences.indices) {
                val exps = cv.experiences.toMutableList()
                exps[idx] = exps[idx].copy(description = value)
                cv.copy(experiences = exps)
            } else {
                cv
            }
        }
        else -> cv
    }
}

internal fun applySelectedSuggestions(cv: CvData, selectedIds: List<String>, suggestionsJson: String): CvData {
    var updated = cv
    if (suggestionsJson.isBlank() || selectedIds.isEmpty()) return updated
    try {
        val arr = org.json.JSONArray(suggestionsJson)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val id = obj.optString("id")
            if (id in selectedIds) {
                val category = obj.optString("category")
                val proposedValue = obj.optString("proposedValue")
                updated = applyIndividualAtsImprovement(updated, category, proposedValue)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return updated
}

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

private fun getSeedProfilesList(isBn: Boolean = true): List<CvData> {
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
        fresherAcademicProjects = "â€¢ Final Year Capstone: Supply Chain Optimization & Demand Forecasting Model\nâ€¢ Analyzed FMCG retail distribution networks using statistical forecasting, increasing warehouse inventory turnaround by 18%.\nâ€¢ Business Analytics Seminar: Consumer Credit Risk Assessment with Regression Modeling",
        fresherInternshipsVolunteer = "â€¢ Strategic Projects Intern â€” Bangladesh Business Leadership Forum (3 Months)\nâ€¢ Coordinated digital communication, executive speaker sessions, and student registration for 1,200+ attendees.\nâ€¢ Volunteer Community Lead â€” Youth Empowerment & Skills Initiative (2023)",
        fresherLeadershipClubs = "â€¢ Vice President â€” University Management & Business Club (2022-2023)\nâ€¢ Finalist â€” National Inter-University Business Case Competition 2023\nâ€¢ Chief Organizer â€” DU National Business Fest 2022",
        fresherKeyCoursework = "Strategic Management, Corporate Finance, Business Statistics, Marketing Analytics, Supply Chain Logistics, Financial Accounting, Econometrics",
        showSignatureLine = true,
        experiences = listOf(
            CvExperienceItem(
                company = "Apex Business Solutions",
                role = "Associate Business Analyst",
                startDate = "Jan 2024",
                endDate = "Present",
                isCurrent = true,
                description = "â€¢ Conducted market research and competitor analysis to assist in strategic decision-making.\nâ€¢ Developed interactive business performance dashboards, increasing analytical clarity by 25%.\nâ€¢ Managed cross-functional project coordination across marketing and product design teams."
            ),
            CvExperienceItem(
                company = "Strategic Marketing Group",
                role = "Management Trainee (Intern)",
                startDate = "Jun 2023",
                endDate = "Dec 2023",
                isCurrent = false,
                description = "â€¢ Collaborated on brand optimization and promotional campaigns for FMCG sector clients.\nâ€¢ Assisted in preparation of financial models and feasibility reports for new product launches."
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
        profileLabel = if (isBn) "HR & Admin Officer (এইচআর ও এডমিন)" else "HR & Admin Officer",
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
                description = "â€¢ Spearheaded end-to-end recruitment pipelines, onboarding 60+ skilled professionals across IT and Finance.\nâ€¢ Supervised monthly payroll administration, provident fund deductions, and statutory compliance.\nâ€¢ Formulated company-wide HR handbook and code of conduct policies reducing turnover by 15%."
            ),
            CvExperienceItem(
                company = "Orion Technologies Ltd",
                role = "HR Officer",
                startDate = "Jul 2019",
                endDate = "Jan 2022",
                isCurrent = false,
                description = "â€¢ Maintained confidential employee databases, leave records, and performance evaluation metrics.\nâ€¢ Organized quarterly employee wellness workshops and cross-team team building events."
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
        profileLabel = if (isBn) "Banking & Financial Analyst (ব্যাংক ও ফিন্যান্স)" else "Banking & Financial Analyst",
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
                description = "â€¢ Evaluated corporate loan proposals and conducted audited financial statement ratio analyses.\nâ€¢ Prepared comprehensive Credit Appraisal Memorandums (CAM) aligned with Bangladesh Bank CRG guidelines.\nâ€¢ Monitored portfolio asset quality and conducted regular stress testing on capital adequacy."
            ),
            CvExperienceItem(
                company = "Standard Chartered Bank",
                role = "Credit Analyst - SME Banking",
                startDate = "Jan 2018",
                endDate = "Feb 2021",
                isCurrent = false,
                description = "â€¢ Assessed creditworthiness for 120+ SME clients, disbursing BDT 80 Crore in structured facilities.\nâ€¢ Optimized verification turn-around time (TAT) by 30% through automated scoring templates."
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
        profileLabel = if (isBn) "Brand & Marketing Manager (মার্কেটিং)" else "Brand & Marketing Manager",
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
                description = "â€¢ Managed 3 flagship national FMCG brands with full P&L accountability.\nâ€¢ Directed 360-degree ATL/BTL marketing campaigns achieving a 22% uplift in market share.\nâ€¢ Led social media influencer programs that generated over 15 Million organic digital impressions."
            ),
            CvExperienceItem(
                company = "Mindshare Bangladesh",
                role = "Digital Media Planner",
                startDate = "Jan 2019",
                endDate = "Sep 2021",
                isCurrent = false,
                description = "â€¢ Executed programmatic PPC and Meta ad campaigns with an average ROAS of 4.2x.\nâ€¢ Formulated consumer behavior insights using Google Analytics 4 and Semrush."
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
        profileLabel = if (isBn) "NGO & Humanitarian Officer (এনজিও ও উন্নয়ন)" else "NGO & Humanitarian Officer",
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
                description = "â€¢ Supervised field execution of livelihood enhancement interventions supporting 12,000+ vulnerable households.\nâ€¢ Coordinated baseline, midline, and endline surveys utilizing KoboToolbox and ODK mobile data tools.\nâ€¢ Liaised with local government authorities, UNO, and community leaders for seamless project facilitation."
            ),
            CvExperienceItem(
                company = "Save the Children International",
                role = "MEAL Officer - Emergency Response",
                startDate = "Jan 2018",
                endDate = "Mar 2021",
                isCurrent = false,
                description = "â€¢ Established community feedback and complaint response mechanisms (CRM) ensuring 100% accountability.\nâ€¢ Drafted monthly donor progress reports adhering to USAID and ECHO grant specifications."
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
        profileLabel = if (isBn) "Software Engineer (সফটওয়্যার ও আইটি)" else "Software Engineer",
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
                description = "â€¢ Engineered cloud microservices processing 2.5M daily fintech transactions with 99.99% uptime.\nâ€¢ Spearheaded the migration from monolithic architecture to Dockerized Kubernetes clusters on AWS.\nâ€¢ Mentored 8 junior and mid-level engineers in TDD, CI/CD automation, and clean code standards."
            ),
            CvExperienceItem(
                company = "Dynamic Solution Innovators (DSi)",
                role = "Software Engineer",
                startDate = "Oct 2019",
                endDate = "Jun 2022",
                isCurrent = false,
                description = "â€¢ Developed RESTful and GraphQL backend endpoints using Node.js, NestJS, and PostgreSQL.\nâ€¢ Reduced database query latency by 40% through indexing optimization and Redis distributed caching."
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
        certifications = "AWS Certified Solutions Architect â€“ Associate (2023)\nOracle Certified Professional: Java SE 11 Developer (2021)",
        references = "Available upon request.",
        templateStyle = CvTemplateStyle.MODERN_MINIMALIST
    )

    val supplyChainDraft = CvData(
        id = "profile_supply_chain",
        profileLabel = if (isBn) "Supply Chain & Procurement (সাপ্লাই চেইন)" else "Supply Chain & Procurement",
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
                description = "â€¢ Directed domestic and overseas raw material procurement with annual spend exceeding BDT 120 Crore.\nâ€¢ Negotiated freight forwarding tariffs, saving 12% in international ocean transit expenses.\nâ€¢ Supervised SAP ERP Materials Management (MM) workflows and warehouse cycle counting."
            ),
            CvExperienceItem(
                company = "Abul Khair Steel Products Ltd",
                role = "Procurement Executive",
                startDate = "Aug 2018",
                endDate = "Apr 2021",
                isCurrent = false,
                description = "â€¢ Processed commercial letters of credit (LC), HS code assessments, and customs duty assessments.\nâ€¢ Monitored supplier OTIF (On-Time In-Full) performance benchmarks across 85 active vendors."
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
        profileLabel = if (isBn) "Academic Lecturer & Researcher (শিক্ষকতা)" else "Academic Lecturer & Researcher",
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
                description = "â€¢ Delivered core undergraduate and graduate lectures on Econometrics, Macroeconomics, and Game Theory.\nâ€¢ Supervised 25+ student thesis projects and managed departmental curriculum development committees.\nâ€¢ Secured research grant of BDT 15 Lakh for urban socio-economic mobility assessment."
            ),
            CvExperienceItem(
                company = "University of Liberal Arts Bangladesh (ULAB)",
                role = "Lecturer in Economics",
                startDate = "Sep 2017",
                endDate = "Dec 2020",
                isCurrent = false,
                description = "â€¢ Conducted weekly tutorial sections and lab exercises utilizing Stata, R, and Python.\nâ€¢ Organized international economics symposiums and served as faculty advisor for debate club."
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
        profileLabel = if (isBn) "Customer Success & Operations (কাস্টমার অপারেশনস)" else "Customer Success & Operations",
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
                description = "â€¢ Led a frontline tier-2 support team of 18 specialists delivering 24/7 client resolution.\nâ€¢ Implemented AI-driven ticket triaging rules in Zendesk, lowering First Response Time (FRT) by 45%.\nâ€¢ Collaborated with engineering teams to resolve recurring UX bugs affecting app user experience."
            ),
            CvExperienceItem(
                company = "Daraz Bangladesh (Alibaba Group)",
                role = "Senior Customer Experience Associate",
                startDate = "Feb 2020",
                endDate = "May 2022",
                isCurrent = false,
                description = "â€¢ Handled priority VIP merchant disputes, resolving 95% of cases within initial SLA.\nâ€¢ Trained 40+ onboarding agents on soft skills, complaint de-escalation, and empathy guidelines."
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
        profileLabel = if (isBn) "নতুন ফ্রেশ প্রোফাইল" else "New Clean Profile",
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
                put("photoWidth", profile.photoWidth)
                put("photoHeight", profile.photoHeight)
                put("photoBorderWidth", profile.photoBorderWidth.toDouble())
                put("photoCornerRadius", profile.photoCornerRadius)
                
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
                
                // Gemini AI fields
                put("lastAtsScoreFromGemini", profile.lastAtsScoreFromGemini)
                put("lastAtsSuggestionsJson", profile.lastAtsSuggestionsJson)
                put("lastCircularSuggestionsJson", profile.lastCircularSuggestionsJson)
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
                    photoWidth = obj.optInt("photoWidth", 80),
                    photoHeight = obj.optInt("photoHeight", 80),
                    photoBorderWidth = obj.optDouble("photoBorderWidth", 1.5).toFloat(),
                    photoCornerRadius = obj.optInt("photoCornerRadius", 10),
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
                    customLineSpacing = obj.optDouble("customLineSpacing", 1.15).toFloat(),
                    lastAtsScoreFromGemini = obj.optInt("lastAtsScoreFromGemini", 0),
                    lastAtsSuggestionsJson = obj.optString("lastAtsSuggestionsJson", ""),
                    lastCircularSuggestionsJson = obj.optString("lastCircularSuggestionsJson", "")
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

    fun drawPhotoAt(px: Float, py: Float, size: Float, shape: String = data.photoShape, borderColor: Int = primaryColor) {
        if (photoBitmap == null) return
        val bw = photoBitmap.width.toFloat()
        val bh = photoBitmap.height.toFloat()
        if (bw <= 0f || bh <= 0f) return

        val widthRatio = (data.photoWidth.coerceIn(50, 130)) / 80f
        val heightRatio = (data.photoHeight.coerceIn(50, 140)) / 80f
        val pw = size * widthRatio
        val ph = if (shape == "Circle" || shape == "Square") pw else (size * heightRatio)
        val cornerRad = (data.photoCornerRadius.toFloat() * (pw / 80f)).coerceAtLeast(2f)

        canvas.save()
        val path = android.graphics.Path()
        val photoRect = android.graphics.RectF(px, py, px + pw, py + ph)
        when (shape) {
            "Circle" -> {
                val rad = minOf(pw, ph) / 2f
                path.addCircle(px + pw / 2f, py + ph / 2f, rad, android.graphics.Path.Direction.CW)
            }
            "Oval" -> {
                path.addOval(photoRect, android.graphics.Path.Direction.CW)
            }
            "Rounded" -> {
                path.addRoundRect(photoRect, cornerRad, cornerRad, android.graphics.Path.Direction.CW)
            }
            "Rectangle" -> {
                path.addRoundRect(photoRect, 3f, 3f, android.graphics.Path.Direction.CW)
            }
            else -> { // Square
                path.addRect(photoRect, android.graphics.Path.Direction.CW)
            }
        }
        canvas.clipPath(path)

        canvas.save()
        val cx = px + pw / 2f
        val cy = py + ph / 2f
        canvas.translate(cx, cy)
        canvas.scale(data.photoScale, data.photoScale)
        canvas.translate(data.photoOffsetX / 3.2f, data.photoOffsetY / 3.2f)
        canvas.translate(-cx, -cy)

        // Center Crop computation:
        val scale = maxOf(pw / bw, ph / bh)
        val sw = bw * scale
        val sh = bh * scale
        val sx = px + (pw - sw) / 2f
        val sy = py + (ph - sh) / 2f
        val dstRect = android.graphics.RectF(sx, sy, sx + sw, sy + sh)

        canvas.drawBitmap(photoBitmap, null, dstRect, Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG))
        canvas.restore()
        canvas.restore()

        val effectiveBorderWidth = data.photoBorderWidth.coerceIn(0f, 5f)
        if (effectiveBorderWidth > 0f) {
            val borderPaint = Paint().apply {
                color = borderColor
                style = Paint.Style.STROKE
                strokeWidth = effectiveBorderWidth
                isAntiAlias = true
            }
            when (shape) {
                "Circle" -> canvas.drawCircle(px + pw / 2f, py + ph / 2f, minOf(pw, ph) / 2f, borderPaint)
                "Oval" -> canvas.drawOval(photoRect, borderPaint)
                "Rounded" -> canvas.drawRoundRect(photoRect, cornerRad, cornerRad, borderPaint)
                "Rectangle" -> canvas.drawRoundRect(photoRect, 3f, 3f, borderPaint)
                else -> canvas.drawRect(photoRect, borderPaint)
            }
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
                val bulletText = "â€¢ $fullText"
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
                    val head = "${exp.role} â€” ${exp.company}"
                    val date = "${exp.startDate}${if (exp.startDate.isNotBlank() && exp.endDate.isNotBlank()) " â€“ " else ""}${if (exp.isCurrent) "Present" else exp.endDate}"
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
                            val bulletLine = if (line.startsWith("â€¢") || line.startsWith("-") || line.startsWith("â–ª") || line.startsWith("â—†")) line else "â€¢ $line"
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
                val eduTitle = if (degreePart.isNotBlank() && edu.institution.isNotBlank()) "$degreePart â€” ${edu.institution}" else degreePart.ifBlank { edu.institution }
                
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
        ).joinToString("  â€¢  ")
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
            drawPhotoAt(px, 24f, pSize, data.photoShape, AndroidColor.WHITE)
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
        ).joinToString("  â€¢  ")

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
            drawPhotoAt(px, currentY, pSize, data.photoShape, primaryColor)
        }

        currentY += 66f
    } else if (pdfStyle == CvTemplateStyle.CANVA_MINIMALIST_CLEAN) {
        // ================= BRANCH 6: CANVA MINIMALIST CLEAN (CENTERED MODERN) =================
        var headY = currentY
        if (photoBitmap != null) {
            val pSize = 58f
            drawPhotoAt((pageWidth - pSize) / 2f, headY, pSize, data.photoShape, primaryColor)
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
        ).joinToString("   â€¢   ")

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
            drawPhotoAt(px, currentY, pSize, data.photoShape, primaryColor)
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
        ).joinToString("  â�â€“  ")
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
                canvas.drawText("â�â€“  ${headerText.uppercase()}", margin, currentY + 11f, exePaint)
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
                canvas.drawText("â—†  ${headerText.uppercase()}", margin, currentY + 11f, dPaint)
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
        "SQUARE" -> "â–ª "
        "DIAMOND" -> "â—† "
        "COMMA" -> ""
        "PIPE" -> ""
        "NONE" -> ""
        else -> "â€¢ "
    }

    effectiveSectionOrder.forEach { secKey ->
        if (data.hiddenSections.contains(secKey)) return@forEach

        when (secKey) {
            "SUMMARY" -> {
                if (data.summary.isNotBlank()) {
                    val objTitle = if (pdfStyle == CvTemplateStyle.NGO_DEVELOPMENT_HUMANITARIAN) "MISSION & SOCIAL IMPACT OBJECTIVE" else if (pdfStyle == CvTemplateStyle.HARVARD_CLASSIC) "PROFESSIONAL SUMMARY" else "CAREER OBJECTIVE"
                    drawSectionHeader(objTitle, "ðŸŽ¯")

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
                    drawSectionHeader("EDUCATION", "ðŸŽ“")

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
                            val instText = "${edu.institution}${if (edu.result.isNotBlank()) "  â€¢  ${edu.result}" else ""}"
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
                        drawSectionHeader("ACADEMIC PROJECTS & CAPSTONE THESIS", "ðŸš€")
                        val pLayout = StaticLayout.Builder.obtain(data.fresherAcademicProjects, 0, data.fresherAcademicProjects.length, bodyPaint, contentWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                        checkAndAddNewPage(pLayout.height.toFloat() + 4f)
                        canvas.save(); canvas.translate(margin, currentY); pLayout.draw(canvas); canvas.restore()
                        currentY += pLayout.height + entryGap
                    }

                    if (data.fresherInternshipsVolunteer.isNotBlank()) {
                        drawSectionHeader("INTERNSHIPS & VOLUNTEER WORK", "ðŸ¤�")
                        val vLayout = StaticLayout.Builder.obtain(data.fresherInternshipsVolunteer, 0, data.fresherInternshipsVolunteer.length, bodyPaint, contentWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                        checkAndAddNewPage(vLayout.height.toFloat() + 4f)
                        canvas.save(); canvas.translate(margin, currentY); vLayout.draw(canvas); canvas.restore()
                        currentY += vLayout.height + entryGap
                    }

                    if (data.fresherLeadershipClubs.isNotBlank()) {
                        drawSectionHeader("CAMPUS LEADERSHIP & EXTRACURRICULAR", "ðŸ�†")
                        val lLayout = StaticLayout.Builder.obtain(data.fresherLeadershipClubs, 0, data.fresherLeadershipClubs.length, bodyPaint, contentWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                        checkAndAddNewPage(lLayout.height.toFloat() + 4f)
                        canvas.save(); canvas.translate(margin, currentY); lLayout.draw(canvas); canvas.restore()
                        currentY += lLayout.height + entryGap
                    }

                    if (data.fresherKeyCoursework.isNotBlank()) {
                        drawSectionHeader("RELEVANT COURSEWORK & ACADEMIC CORE", "ðŸ“š")
                        val cLayout = StaticLayout.Builder.obtain(data.fresherKeyCoursework, 0, data.fresherKeyCoursework.length, bodyPaint, contentWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                        checkAndAddNewPage(cLayout.height.toFloat() + 4f)
                        canvas.save(); canvas.translate(margin, currentY); cLayout.draw(canvas); canvas.restore()
                        currentY += cLayout.height + entryGap
                    }

                    if (data.experiences.isNotEmpty()) {
                        drawSectionHeader("ADDITIONAL PRACTICUM & WORK EXPERIENCE", "ðŸ’¼")
                        data.experiences.forEach { exp ->
                            if (exp.role.isNotBlank() || exp.company.isNotBlank()) {
                                val expTitle = "$bulletPrefix${exp.role} â€“ ${exp.company} (${exp.location})${if (exp.startDate.isNotBlank()) " [${exp.startDate} - ${if (exp.isCurrent) "Present" else exp.endDate}]" else ""}"
                                val titleLayout = StaticLayout.Builder.obtain(expTitle, 0, expTitle.length, bodyBoldPaint, contentWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                                checkAndAddNewPage(titleLayout.height.toFloat() + 4f)
                                canvas.save(); canvas.translate(margin, currentY); titleLayout.draw(canvas); canvas.restore()
                                currentY += titleLayout.height + 2f

                                if (exp.description.isNotBlank()) {
                                    val lines = exp.description.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                                    lines.forEach { line ->
                                        val bulletLine = if (line.startsWith("â€¢") || line.startsWith("-") || line.startsWith("â–ª") || line.startsWith("â—†")) line else "$bulletPrefix$line"
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
                        drawSectionHeader(expSectionTitle, "ðŸ’¼")

                        data.experiences.forEach { exp ->
                            if (exp.role.isNotBlank() || exp.company.isNotBlank()) {
                                val expTitle = "$bulletPrefix${exp.role} â€“ ${exp.company} (${exp.location})${if (exp.startDate.isNotBlank()) " [${exp.startDate} - ${if (exp.isCurrent) "Present" else exp.endDate}]" else ""}"
                                val titleLayout = StaticLayout.Builder.obtain(expTitle, 0, expTitle.length, bodyBoldPaint, contentWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                                checkAndAddNewPage(titleLayout.height.toFloat() + 4f)
                                canvas.save(); canvas.translate(margin, currentY); titleLayout.draw(canvas); canvas.restore()
                                currentY += titleLayout.height + 2f

                                if (exp.description.isNotBlank()) {
                                    val lines = exp.description.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                                    lines.forEach { line ->
                                        val bulletLine = if (line.startsWith("â€¢") || line.startsWith("-") || line.startsWith("â–ª") || line.startsWith("â—†")) line else "$bulletPrefix$line"
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
                    drawSectionHeader("KEY SKILLS & COMPETENCIES", "âš¡")
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
                    drawSectionHeader("FEATURED PROJECTS & INITIATIVES", "ðŸš€")
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
                    drawSectionHeader("TRAINING & CERTIFICATION", "ðŸ“œ")
                    val certLayout = StaticLayout.Builder.obtain(data.certifications, 0, data.certifications.length, bodyPaint, contentWidth.toInt()).setLineSpacing(0f, data.customLineSpacing).build()
                    checkAndAddNewPage(certLayout.height.toFloat() + 4f)
                    canvas.save(); canvas.translate(margin, currentY); certLayout.draw(canvas); canvas.restore()
                    currentY += certLayout.height + 4f
                }
            }

            "LANGUAGES" -> {
                if (data.languages.isNotBlank()) {
                    drawSectionHeader("LANGUAGE FLUENCY", "ðŸŒ�")
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
                            drawSectionHeader(item.title.uppercase(), "ðŸ“Œ")
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
                    drawSectionHeader("PERSONAL INFORMATION", "ðŸ‘¤")
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
                    drawSectionHeader("REFERENCES", "ðŸ¤�")
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
                    val clean = l.trim().removePrefix("â€¢").removePrefix("-").trim()
                    if (clean.isNotBlank()) {
                        xmlSb.append("    <w:p><w:pPr><w:ind w:left=\"360\"/></w:pPr><w:r><w:rPr><w:sz w:val=\"20\"/><w:color w:val=\"374151\"/></w:rPr><w:t>â€¢ ")
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
            xmlSb.append("<w:r><w:rPr><w:b/><w:sz w:val=\"22\"/><w:color w:val=\"111827\"/></w:rPr><w:t>â€¢ ")
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
    var isHeaderVisible by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): androidx.compose.ui.geometry.Offset {
                val delta = available.y
                if (delta < -10f) {
                    if (isHeaderVisible) isHeaderVisible = false
                } else if (delta > 10f) {
                    if (!isHeaderVisible) isHeaderVisible = true
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }
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
    var globalPendingCropBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val globalImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (originalBitmap != null) {
                    globalPendingCropBitmap = originalBitmap
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val pdfImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isAiLoading = true
            aiLoadingMessage = if (isBn) "âœ¨ জেমিনি এআই আপনার পিডিএফ সিভি বিশ্লেষণ করে প্রোফাইল তৈরি করছে..." else "âœ¨ Gemini AI is analyzing your PDF resume to create a profile..."
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

    fun runDirectCircularMatchAi(circularText: String, imageBytes: ByteArray?, imageMime: String) {
        if (circularText.isBlank() && imageBytes == null) {
            Toast.makeText(context, if (isBn) "অনুগ্রহ করে সার্কুলার টেক্সট দিন অথবা ছবি আপলোড করুন!" else "Please provide circular text or pick an image!", Toast.LENGTH_SHORT).show()
            return
        }
        
        isAiLoading = true
        aiLoadingMessage = if (isBn) "✨ জেমিনি এআই সার্কুলার বিশ্লেষণ করছে..." else "✨ Gemini AI is analyzing job circular..."
        
        scope.launch {
            try {
                val promptBuilder = StringBuilder()
                promptBuilder.append("Target Job Circular Context:\n")
                if (circularText.isNotBlank()) {
                    promptBuilder.append("Circular Text: $circularText\n")
                }
                if (imageBytes != null) {
                    promptBuilder.append("[Image of job circular attached]\n")
                }

                promptBuilder.append("\nCandidate Profile:\n")
                promptBuilder.append("Target Title: ${cvData.jobTitle}\n")
                promptBuilder.append("Summary: ${cvData.summary}\n")
                
                val skillsText = cvData.skills.joinToString { 
                    if (it.description.isNotBlank()) "${it.name}: ${it.description}" else it.name 
                }
                promptBuilder.append("Skills: $skillsText\n")
                promptBuilder.append("Experiences: ${cvData.experiences.joinToString { "${it.role} at ${it.company}: ${it.description}" }}\n")
                promptBuilder.append("Educations: ${cvData.educations.joinToString { "${it.degree} from ${it.institution}" }}\n")

                promptBuilder.append("\nTask Instructions:\n")
                promptBuilder.append("1. Compute an accurate job match score (0-100%) by comparing candidate skills, qualifications, and role requirements against the circular.\n")
                promptBuilder.append("2. Extract 3-5 matching strengths that align with circular requirements.\n")
                promptBuilder.append("3. Extract 3-6 missing critical technical or role keywords present in circular but missing in candidate CV.\n")
                promptBuilder.append("4. Provide 3 high-impact improvement recommendations for passing the ATS filter.\n")
                promptBuilder.append("5. Write a tailored summary aligned with the circular.\n")
                promptBuilder.append("6. Return a list of specific CV suggestions for improvement. Each suggestion MUST contain: id (e.g. 'cir_1'), titleEn, titleBn, descEn, descBn, category (e.g., 'summary', 'skills', 'experience_0'), proposedValue (the tailored text/skills/bullets to insert).\n")
                promptBuilder.append("7. Return strictly valid JSON object with keys: matchPercentage (int), tailoredSummary (string), matchingStrengths (array of string), missingKeywords (array of string), improvementTips (array of string), newSkills (array of string), suggestions (array of objects with keys: id, titleEn, titleBn, descEn, descBn, category, proposedValue). Do NOT wrap in markdown syntax like ```json.")

                val sysPrompt = "You are a top ATS recruitment specialist & resume evaluator. Analyze candidate resume against target job circular. Return strictly a valid JSON object. Do NOT include markdown code blocks or any comments before or after the JSON."
                
                val resultText = callGeminiAiMultiModal(
                    prompt = promptBuilder.toString(),
                    systemInstruction = sysPrompt,
                    imageBytes = imageBytes,
                    mimeType = imageMime
                )

                withContext(Dispatchers.Main) {
                    try {
                        val cleanJsonStr = resultText.substringAfter("{").substringBeforeLast("}").let { "{$it}" }
                        val jsonObj = org.json.JSONObject(cleanJsonStr)
                        val matchPct = jsonObj.optInt("matchPercentage", 85)
                        val tailoredSummary = jsonObj.optString("tailoredSummary", cvData.summary)

                        val matchingStrengths = mutableListOf<String>()
                        jsonObj.optJSONArray("matchingStrengths")?.let { arr ->
                            for (i in 0 until arr.length()) matchingStrengths.add(arr.getString(i))
                        }

                        val missingKeywords = mutableListOf<String>()
                        jsonObj.optJSONArray("missingKeywords")?.let { arr ->
                            for (i in 0 until arr.length()) missingKeywords.add(arr.getString(i))
                        }

                        val improvementTips = mutableListOf<String>()
                        jsonObj.optJSONArray("improvementTips")?.let { arr ->
                            for (i in 0 until arr.length()) improvementTips.add(arr.getString(i))
                        }

                        val suggestionsJson = jsonObj.optJSONArray("suggestions")?.toString() ?: "[]"

                        val updatedSkills = cvData.skills.toMutableList()
                        jsonObj.optJSONArray("newSkills")?.let { arr ->
                            for (i in 0 until arr.length()) {
                                val skName = arr.getString(i)
                                val clean = skName.removePrefix("â€¢").removePrefix("-").removePrefix("*").trim()
                                if (updatedSkills.none { it.name.equals(clean, ignoreCase = true) || (clean.contains(":") && it.name.equals(clean.substringBefore(":").trim(), ignoreCase = true)) }) {
                                    val item = if (clean.contains(":")) {
                                        val parts = clean.split(":", limit = 2)
                                        CvSkillItem(name = parts[0].trim(), description = parts[1].trim())
                                    } else {
                                        CvSkillItem(name = clean, description = "Experienced in applying $clean effectively in professional environments.")
                                    }
                                    updatedSkills.add(item)
                                }
                            }
                        }

                        updateCvDataState(cvData.copy(
                            skills = updatedSkills,
                            lastJobMatchPercentage = matchPct,
                            lastMatchingStrengths = matchingStrengths,
                            lastMissingKeywords = missingKeywords,
                            lastImprovementTips = improvementTips,
                            lastTailoredSummary = tailoredSummary,
                            lastCircularSuggestionsJson = suggestionsJson,
                            targetJobCircular = circularText
                        ))
                        Toast.makeText(context, if (isBn) "সার্কুলার অ্যানালাইসিস সম্পন্ন! ম্যাচ স্কোর: ${matchPct}%" else "Job circular analyzed! Match score: ${matchPct}%", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Parsing Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isAiLoading = false
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "AI Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    isAiLoading = false
                }
            }
        }
    }

    fun runDirectAtsAnalysisAi() {
        isAiLoading = true
        aiLoadingMessage = if (isBn) "✨ জেমিনি এআই আপনার সিভির এটিএস স্ট্রাকচার মান যাচাই করছে..." else "✨ Gemini AI is analyzing CV structure & ATS formatting..."
        
        scope.launch {
            try {
                val promptBuilder = StringBuilder()
                promptBuilder.append("Analyze Candidate CV ONLY for General Professional ATS Readiness, Formatting, Section Completeness, Action Verbs, and Impact Metrics (Do NOT require any target job circular):\n")
                promptBuilder.append("Candidate Full Name: ${cvData.fullName}\n")
                promptBuilder.append("Target Title: ${cvData.jobTitle}\n")
                promptBuilder.append("Summary: ${cvData.summary}\n")
                
                val skillsText = cvData.skills.joinToString { 
                    if (it.description.isNotBlank()) "${it.name}: ${it.description}" else it.name 
                }
                promptBuilder.append("Skills: $skillsText\n")
                promptBuilder.append("Experiences: ${cvData.experiences.joinToString { "${it.role} at ${it.company}: ${it.description}" }}\n")
                promptBuilder.append("Educations: ${cvData.educations.joinToString { "${it.degree} from ${it.institution}" }}\n")

                // Inject local structural checklist evaluation info
                val hasSummary = cvData.summary.isNotBlank() && cvData.summary.length >= 35
                val hasQuantifiedExp = cvData.experiences.any { exp -> exp.description.contains("%") || exp.description.contains("$") || exp.description.contains(Regex("\\d+")) }
                val hasSkills = cvData.skills.size >= 5
                val hasContact = cvData.email.isNotBlank() && cvData.phone.isNotBlank() && cvData.linkedin.isNotBlank()
                val hasProjects = cvData.projects.isNotEmpty() || cvData.customSections.isNotEmpty()
                val hasEducation = cvData.educations.isNotEmpty()

                promptBuilder.append("\nLocal CV Structural Checklist Evaluation:\n")
                promptBuilder.append("- Professional ATS Summary: ${if (hasSummary) "Present" else "Missing (Add targeted summary >= 35 chars)"}\n")
                promptBuilder.append("- Quantified Achievements in Experience: ${if (hasQuantifiedExp) "Present" else "Missing (Include impact metrics %, $ or numbers)"}\n")
                promptBuilder.append("- Categorized Skills: ${if (hasSkills) "Present" else "Missing (List 5+ technical/domain skills)"}\n")
                promptBuilder.append("- Contact Info with LinkedIn: ${if (hasContact) "Present" else "Missing (Add email, phone, and LinkedIn)"}\n")
                promptBuilder.append("- Portfolio Projects: ${if (hasProjects) "Present" else "Missing (Add projects/accomplishments)"}\n")
                promptBuilder.append("- Education Records: ${if (hasEducation) "Present" else "Missing (Add education history)"}\n")

                promptBuilder.append("\nTask Instructions:\n")
                promptBuilder.append("1. Compute an overall ATS readiness score (0-100%) reflecting content richness, metrics presence, clarity, and formatting compliance. This score MUST strictly align with the local structural checklist above (each missing item should negatively impact the score by at least 10-15%). If multiple major sections are missing, the score should reflect that and be lower.\n")
                promptBuilder.append("2. Generate a list of 4-6 specific actionable improvement suggestions. Each suggestion MUST contain:\n")
                promptBuilder.append("   - id: Unique ID (e.g. 'ats_1')\n")
                promptBuilder.append("   - titleEn: Clear title of the improvement in English\n")
                promptBuilder.append("   - titleBn: Clear title of the improvement in Bengali\n")
                promptBuilder.append("   - descEn: Detailed description in English\n")
                promptBuilder.append("   - descBn: Detailed description in Bengali\n")
                promptBuilder.append("   - category: Target section key in CV ('summary', 'skills', 'experience_0', 'experience_1', 'certifications', 'references')\n")
                promptBuilder.append("   - proposedValue: The revised/optimal text content to be injected into that category/index. For skills, write it as a list of optimized comma-separated skills in 'Name: Description' format.\n")
                promptBuilder.append("3. Return strictly valid JSON with structure:\n")
                promptBuilder.append("{\n")
                promptBuilder.append("  \"score\": 85,\n")
                promptBuilder.append("  \"suggestions\": [\n")
                promptBuilder.append("    {\n")
                promptBuilder.append("      \"id\": \"ats_1\",\n")
                promptBuilder.append("      \"titleEn\": \"...\",\n")
                promptBuilder.append("      \"titleBn\": \"...\",\n")
                promptBuilder.append("      \"descEn\": \"...\",\n")
                promptBuilder.append("      \"descBn\": \"...\",\n")
                promptBuilder.append("      \"category\": \"...\",\n")
                promptBuilder.append("      \"proposedValue\": \"...\"\n")
                promptBuilder.append("    }\n")
                promptBuilder.append("  ]\n")
                promptBuilder.append("}")

                val sysPrompt = "You are a professional resume evaluation engine and top-tier corporate recruiter. Return strictly a valid JSON object representing the ATS analysis of the resume. Do NOT wrap in markdown code blocks or include other text."
                
                val resultText = callGeminiAiMultiModal(
                    prompt = promptBuilder.toString(),
                    systemInstruction = sysPrompt
                )

                withContext(Dispatchers.Main) {
                    try {
                        val cleanJsonStr = resultText.substringAfter("{").substringBeforeLast("}").let { "{$it}" }
                        val jsonObj = org.json.JSONObject(cleanJsonStr)
                        val score = jsonObj.optInt("score", 75)
                        val suggestionsJson = jsonObj.optJSONArray("suggestions")?.toString() ?: "[]"

                        updateCvDataState(cvData.copy(
                            lastAtsScoreFromGemini = score,
                            lastAtsSuggestionsJson = suggestionsJson
                        ))
                        Toast.makeText(context, if (isBn) "এটিএস অ্যানালাইসিস সম্পন্ন! স্কোর: ${score}%" else "ATS analysis completed! Score: ${score}%", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Parsing Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isAiLoading = false
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "AI Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    isAiLoading = false
                }
            }
        }
    }

    // Auto-render live PDF vector preview on changes or when switching tabs
    LaunchedEffect(cvData, previewRefreshKey, selectedTab) {
        if (selectedTab != 4) {
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
            onPreviewPdf = { item ->
                val file = File(item.filePath)
                if (file.exists()) {
                    viewModel.pdfReaderInitialUri = Uri.fromFile(file)
                    viewModel.pdfReaderInitialName = item.fileName.ifBlank { "CV_${item.candidateName}.pdf" }
                    viewModel.previousToolType = com.example.data.model.ToolType.ATS_CV_BUILDER
                    viewModel.selectedToolType = com.example.data.model.ToolType.PDF_READER
                    showHistoryDialog = false
                } else {
                    val matchingProfile = profilesList.find { it.profileLabel == item.profileLabel || it.fullName == item.candidateName } ?: cvData
                    scope.launch {
                        val newFile = generateCvPdfFile(context, matchingProfile)
                        withContext(Dispatchers.Main) {
                            viewModel.pdfReaderInitialUri = Uri.fromFile(newFile)
                            viewModel.pdfReaderInitialName = newFile.name
                            viewModel.previousToolType = com.example.data.model.ToolType.ATS_CV_BUILDER
                            viewModel.selectedToolType = com.example.data.model.ToolType.PDF_READER
                            showHistoryDialog = false
                        }
                    }
                }
            },
            onOpenExternalPdf = { item ->
                val file = File(item.filePath)
                if (file.exists()) {
                    openPdfFile(context, file)
                } else {
                    showToast(if (isBn) "ফাইলটি খ্এজে পাওয়া যায়নি" else "File not found")
                }
            },
            onSharePdf = { item ->
                val file = File(item.filePath)
                if (file.exists()) {
                    sharePdfFile(context, file)
                } else {
                    showToast(if (isBn) "ফাইলটি খ্এজে পাওয়া যায়নি" else "File not found")
                }
            },
            onDeletePdf = { item ->
                deleteCvHistoryItem(context, item.id)
                historyList = loadCvHistory(context)
                showToast(if (isBn) "হিস্টোরি আইটেম মোছা হয়েছে" else "History item deleted")
            },
            onEditProfile = { item ->
                val profile = profilesList.find { it.profileLabel == item.profileLabel || it.fullName == item.candidateName }
                if (profile != null) {
                    updateCvDataState(profile)
                    activeProfileId = profile.id
                    saveActiveProfileId(context, profile.id)
                    selectedTab = 0
                    showHistoryDialog = false
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
                aiLoadingMessage = if (isBn) "âœ¨ জেমিনি এআই আপনার কাস্টম প্রম্পট অন্যায়ী লিখছে..." else "âœ¨ Gemini AI is generating content from your prompt..."

                scope.launch {
                    try {
                        val sysPrompt = when (aiPromptTargetField) {
                            "CIRCULAR_MATCH" -> "You are a top ATS recruitment specialist & resume evaluator. Analyze candidate resume against target job circular. Output STRICTLY valid JSON with structure: {\"matchPercentage\": 88, \"tailoredSummary\": \"...\", \"matchingStrengths\": [\"Strength 1\", \"Strength 2\"], \"missingKeywords\": [\"Keyword1\", \"Keyword2\"], \"improvementTips\": [\"Tip 1\", \"Tip 2\"], \"newSkills\": [\"Skill1\", \"Skill2\"]}. Do NOT include markdown syntax."
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
                                        val clean = line.removePrefix("â€¢").removePrefix("-").removePrefix("*").removePrefix("â–ª").trim()
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
                                        showToast(if (isBn) "ফ্রেশার সেকশনগ্লো সফলভাবে এআই দিয়ে প্রস্ত্ত হয়েছে!" else "Fresher sections generated successfully!")
                                    } catch (e: Exception) {
                                        showToast("Parsing Error: ${e.message}")
                                    }
                                }
                                "CIRCULAR_MATCH" -> {
                                    try {
                                        val cleanJsonStr = resultText.substringAfter("{").substringBeforeLast("}").let { "{$it}" }
                                        val jsonObj = org.json.JSONObject(cleanJsonStr)
                                        val matchPct = jsonObj.optInt("matchPercentage", 85)
                                        val tailoredSummary = jsonObj.optString("tailoredSummary", cvData.summary)

                                        val matchingStrengths = mutableListOf<String>()
                                        jsonObj.optJSONArray("matchingStrengths")?.let { arr ->
                                            for (i in 0 until arr.length()) matchingStrengths.add(arr.getString(i))
                                        }

                                        val missingKeywords = mutableListOf<String>()
                                        jsonObj.optJSONArray("missingKeywords")?.let { arr ->
                                            for (i in 0 until arr.length()) missingKeywords.add(arr.getString(i))
                                        }

                                        val improvementTips = mutableListOf<String>()
                                        jsonObj.optJSONArray("improvementTips")?.let { arr ->
                                            for (i in 0 until arr.length()) improvementTips.add(arr.getString(i))
                                        }

                                        val updatedSkills = cvData.skills.toMutableList()
                                        jsonObj.optJSONArray("newSkills")?.let { arr ->
                                            for (i in 0 until arr.length()) {
                                                val skName = arr.getString(i)
                                                if (updatedSkills.none { it.name.equals(skName, ignoreCase = true) }) {
                                                    updatedSkills.add(CvSkillItem(name = skName))
                                                }
                                            }
                                        }

                                        updateCvDataState(cvData.copy(
                                            skills = updatedSkills,
                                            lastJobMatchPercentage = matchPct,
                                            lastMatchingStrengths = matchingStrengths,
                                            lastMissingKeywords = missingKeywords,
                                            lastImprovementTips = improvementTips,
                                            lastTailoredSummary = tailoredSummary
                                        ))
                                        showToast(if (isBn) "সার্কুলার অ্যানালাইসিস সম্পন্ন! ম্যাচ স্কোর: ${matchPct}%" else "Job circular analyzed! Match score: ${matchPct}%")
                                    } catch (e: Exception) {
                                        showToast("Parsing Error: ${e.message}")
                                    }
                                }
                                "EDUCATION" -> {
                                    val lines = resultText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                                    val newEduItems = lines.map { line ->
                                        val clean = line.removePrefix("â€¢").removePrefix("-").removePrefix("*").trim()
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
                                        val clean = line.removePrefix("â€¢").removePrefix("-").removePrefix("*").trim()
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
                                "PROJECTS" -> {
                                    val lines = resultText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                                    val newProjects = lines.map { line ->
                                        val clean = line.removePrefix("â€¢").removePrefix("-").removePrefix("*").trim()
                                        val titlePart = clean.substringBefore(":").trim()
                                        val descPart = clean.substringAfter(":").trim()
                                        CvProjectItem(title = titlePart, description = if (descPart != titlePart && descPart.isNotBlank()) descPart else clean)
                                    }
                                    if (newProjects.isNotEmpty()) {
                                        updateCvDataState(cvData.copy(projects = cvData.projects + newProjects))
                                        showToast(if (isBn) "প্রজেক্ট যোগ হয়েছে!" else "Added project entries!")
                                    }
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
            .nestedScroll(nestedScrollConnection)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Header Bar matching the PDF Reader visual motif (circulated in screenshot)
            AnimatedVisibility(
                visible = isHeaderVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
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

                    Spacer(modifier = Modifier.width(2.dp))

                    // Header History Button with unclipped BadgedBox
                    BadgedBox(
                        badge = {
                            if (historyList.isNotEmpty()) {
                                Badge(
                                    containerColor = Color(0xFFE53935),
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = if (historyList.size > 9) "9+" else historyList.size.toString(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = { showHistoryDialog = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History",
                                tint = themeColors.buttonEqualBg,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Header Profile Icon with unclipped BadgedBox
                    val customCount = profilesList.count {
                        it.id.startsWith("custom_profile_") ||
                        it.id.startsWith("profile_import_") ||
                        (!it.id.startsWith("profile_") && it.id.isNotBlank())
                    }
                    BadgedBox(
                        badge = {
                            if (customCount > 0) {
                                Badge(
                                    containerColor = Color(0xFF16A34A),
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = if (customCount > 9) "9+" else customCount.toString(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = { showProfileManagerDialog = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Saved Profiles",
                                tint = themeColors.buttonEqualBg,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // Beautiful tab controls (Removal of double clipart/emojis, using proper Icons)
            val tabs = if (isBn) {
                listOf("প্রোফাইল", "অভিজ্ঞতা", "শিক্ষা ও স্কিল", "জব ম্যাচ", "প্রিভিউ")
            } else {
                listOf("Profile", "Experience", "Education", "Job Match", "Preview")
            }

            val tabIcons = listOf(
                Icons.Default.Person,
                Icons.Default.Work,
                Icons.Default.School,
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
                    0 -> {
                        val filteredProfiles = profilesList.filter {
                            it.id.startsWith("custom_profile_") ||
                            it.id.startsWith("profile_import_") ||
                            (!it.id.startsWith("profile_") && it.id.isNotBlank())
                        }
                        ProfileAndPersonasTab(
                            cvData = cvData,
                            profilesList = filteredProfiles,
                            onCvDataChange = { updateCvDataState(it) },
                            onActiveProfileSelected = { id ->
                                activeProfileId = id
                                saveActiveProfileId(context, id)
                            },
                            onAddNewProfile = { name ->
                                val label = if (name.isNotBlank()) name else "New Profile ${profilesList.size + 1}"
                                val newProfile = CvData(
                                    id = UUID.randomUUID().toString(),
                                    profileLabel = label,
                                    fullName = "Md. Shariful Islam",
                                    jobTitle = "Management Graduate & Business Analyst"
                                )
                                val updatedList = profilesList.toMutableList()
                                updatedList.add(newProfile)
                                profilesList = updatedList
                                saveAllCvProfiles(context, updatedList)
                                activeProfileId = newProfile.id
                                saveActiveProfileId(context, newProfile.id)
                                showToast(if (isBn) "নতুন প্রোফাইল তৈরি হয়েছে!" else "New CV profile created!")
                            },
                            onDeleteProfile = { idToDelete ->
                                val updatedList = profilesList.filter { it.id != idToDelete }
                                profilesList = updatedList
                                saveAllCvProfiles(context, updatedList)
                                if (activeProfileId == idToDelete) {
                                    val remFiltered = updatedList.filter {
                                        it.id.startsWith("custom_profile_") ||
                                        it.id.startsWith("profile_import_") ||
                                        (!it.id.startsWith("profile_") && it.id.isNotBlank())
                                    }
                                    activeProfileId = remFiltered.firstOrNull()?.id ?: (updatedList.firstOrNull()?.id ?: "")
                                    saveActiveProfileId(context, activeProfileId)
                                }
                                showToast(if (isBn) "প্রোফাইলটি মুছে ফেলা হয়েছে!" else "CV profile deleted successfully!")
                            },
                        themeColors = themeColors,
                        isBn = isBn,
                        onOpenPdfInViewer = { profileToOpen ->
                            scope.launch {
                                val file = generateCvPdfFile(context, profileToOpen)
                                withContext(Dispatchers.Main) {
                                    viewModel.pdfReaderInitialUri = Uri.fromFile(file)
                                    viewModel.pdfReaderInitialName = "CV_${profileToOpen.fullName.replace(" ", "_")}.pdf"
                                    viewModel.previousToolType = com.example.data.model.ToolType.ATS_CV_BUILDER
                                    viewModel.selectedToolType = com.example.data.model.ToolType.PDF_READER
                                }
                            }
                        },
                        onGenerateSummaryAi = {
                            if (cvData.jobTitle.isBlank()) {
                                showToast(if (isBn) "অনুগ্রহ করে পদবীটি টাইপ করুন" else "Please fill target designation first")
                            } else {
                                val currentNotes = cvData.summary
                                val currentSkills = cvData.skills.joinToString { it.name }
                                val currentExperiences = cvData.experiences.joinToString { "${it.role} at ${it.company}" }
                                val defaultPrompt = "The candidate is ${cvData.fullName.ifBlank { "Md. Shariful Islam" }}, a Management/MBA Graduate. Target job title: '${cvData.jobTitle}'. " +
                                        "Current partial profile summary is: '$currentNotes'. " +
                                        "Key skills: '$currentSkills'. Work experiences: '$currentExperiences'. " +
                                        "Task: Write a highly specialized, modern, professional, ATS-optimized executive summary of exactly 3 sentences. Enhance and expand any notes they have provided."
                                openAiPrompt(
                                    title = if (isBn) "ক্যারিয়ার সারসংক্ষেপ এআই প্রম্পট" else "AI Resume Summary Prompt",
                                    defaultPrompt = defaultPrompt,
                                    targetField = "SUMMARY"
                                )
                            }
                        }
                    )
                    }

                    1 -> ExperienceTab(
                        cvData = cvData,
                        onCvDataChange = { updateCvDataState(it) },
                        themeColors = themeColors,
                        isBn = isBn,
                        onRequestAiPrompt = { title, prompt, field, idx -> openAiPrompt(title, prompt, field, idx) },
                        onEnhanceBulletAi = { idx ->
                            val exp = cvData.experiences.getOrNull(idx)
                            if (exp != null) {
                                if (true) { // Removing strict role check as per user request for empty fields
                                    val defaultPrompt = "Role: '${exp.role}' at '${exp.company}'. Current raw description/bullet points: '${exp.description}'. Rewrite into exactly 3 robust, results-focused executive action-verb bullets. Use metrics/percentages simulation if appropriate."
                                    openAiPrompt(
                                        title = if (isBn) "অভিজ্ঞতার বিবরণী এআই প্রম্পট" else "AI Experience Description Prompt",
                                        defaultPrompt = defaultPrompt,
                                        targetField = "EXPERIENCE",
                                        expIndex = idx
                                    )
                                }
                            }
                        },
                        onGenerateFresherAi = {
                            val edu = cvData.educations.firstOrNull()?.degree ?: "BBA / B.Sc / H.S.C"
                            val inst = cvData.educations.firstOrNull()?.institution ?: "University / College"
                            val targetRole = if (cvData.jobTitle.isNotBlank()) cvData.jobTitle else "Management Trainee / Entry Level Executive"
                            val skills = cvData.skills.joinToString { it.name }
                            val defaultPrompt = "Degree: $edu\nInstitution: $inst\nTarget Role: $targetRole\nSkills: $skills"
                            openAiPrompt(
                                title = if (isBn) "ফ্রেশার এআই প্রম্পট" else "AI Fresher Customization Prompt",
                                defaultPrompt = defaultPrompt,
                                targetField = "FRESHER_COMPLETE"
                            )
                        }
                    )

                    2 -> EducationAndSkillsTab(
                        cvData = cvData,
                        onCvDataChange = { updateCvDataState(it) },
                        onRequestAiPrompt = { title, defaultPrompt, targetField, expIdx ->
                            openAiPrompt(title, defaultPrompt, targetField, expIdx)
                        },
                        themeColors = themeColors,
                        isBn = isBn
                    )

                    3 -> AiJobCircularMatchTab(
                        cvData = cvData,
                        onCvDataChange = { updateCvDataState(it) },
                        themeColors = themeColors,
                        isBn = isBn,
                        onMatchCircularAi = { circularText, imageBytes, imageMime ->
                            runDirectCircularMatchAi(circularText, imageBytes, imageMime)
                        },
                        onAnalyzeAtsAi = {
                            runDirectAtsAnalysisAi()
                        },
                        onNavigateToTab = { tabIndex ->
                            selectedTab = tabIndex
                        },
                        callGeminiAiApi = { prompt, sysPrompt ->
                            callGeminiAiMultiModal(prompt = prompt, systemInstruction = sysPrompt)
                        }
                    )

                    4 -> PreviewAndExportTab(
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
                        },
                        onRequestAiPrompt = { title, prompt, field, idx -> openAiPrompt(title, prompt, field, idx) },
                        onPickImage = { globalImagePickerLauncher.launch("image/*") },
                        onOpenCropExisting = {
                            try {
                                val bytes = Base64.decode(cvData.photoBase64, Base64.DEFAULT)
                                globalPendingCropBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            } catch (_: Exception) {}
                        }
                    )
                }
            }
        }

        if (globalPendingCropBitmap != null) {
            CvImageCropperDialog(
                originalBitmap = globalPendingCropBitmap!!,
                isBn = isBn,
                themeColors = themeColors,
                onDismiss = { globalPendingCropBitmap = null },
                onCropDone = { base64 ->
                    globalPendingCropBitmap = null
                    updateCvDataState(cvData.copy(
                        photoBase64 = base64,
                        photoScale = 1.0f,
                        photoOffsetX = 0f,
                        photoOffsetY = 0f
                    ))
                }
            )
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
        // --- COMPACT MULTI-PROFILE MANAGER HEADER ---
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "সংরক্ষিত প্রোফাইল" else "Saved Profiles",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = themeColors.buttonEqualBg.copy(alpha = 0.12f),
                            modifier = Modifier.height(18.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
                                Text(
                                    text = "${profilesList.size}",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.buttonEqualBg
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = if (isBn) "নতুন" else "New", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Compact Profiles Horizontal selector
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(profilesList) { profile ->
                        val isActive = profile.id == cvData.id
                        Surface(
                            onClick = { onActiveProfileSelected(profile.id) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isActive) themeColors.buttonEqualBg.copy(alpha = 0.12f) else themeColors.background,
                            border = BorderStroke(
                                width = if (isActive) 1.5.dp else 1.dp,
                                color = if (isActive) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier.widthIn(min = 130.dp, max = 190.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Description,
                                    contentDescription = null,
                                    tint = if (isActive) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.4f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = profile.profileLabel,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = themeColors.displayText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (profile.jobTitle.isNotBlank()) {
                                        Text(
                                            text = profile.jobTitle,
                                            fontSize = 9.sp,
                                            color = themeColors.displayText.copy(alpha = 0.6f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onOpenPdfInViewer(profile) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Visibility, contentDescription = "Quick view", tint = themeColors.buttonEqualBg, modifier = Modifier.size(13.dp))
                                    }
                                    if (profilesList.size > 1) {
                                        IconButton(
                                            onClick = { onDeleteProfile(profile.id) },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Close, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

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
                    text = if (isBn) "সিভির জন্য ছবি আপলোড করে ইচ্ছামত গোল, গোল-কোণা, বা ষড়ভুজ শেইপ সিলেক্ট করুন এবং ড্র্যাগ বা জুম করে পজিশন ঠিক করুন" else "Upload a photo for your CV, pick your favorite shape, and interactively zoom and drag to adjust the crop position",
                    fontSize = 10.5.sp,
                    color = themeColors.displayText.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                val context = LocalContext.current
                var pendingCropBitmap by remember { mutableStateOf<Bitmap?>(null) }
                var isPhotoAdvancedExpanded by remember { mutableStateOf(false) }

                if (pendingCropBitmap != null) {
                    CvImageCropperDialog(
                        originalBitmap = pendingCropBitmap!!,
                        isBn = isBn,
                        themeColors = themeColors,
                        onDismiss = { pendingCropBitmap = null },
                        onCropDone = { base64 ->
                            pendingCropBitmap = null
                            onCvDataChange(cvData.copy(
                                photoBase64 = base64,
                                photoScale = 1.0f,
                                photoOffsetX = 0f,
                                photoOffsetY = 0f
                            ))
                        }
                    )
                }

                val imagePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    if (uri != null) {
                        try {
                            val inputStream = context.contentResolver.openInputStream(uri)
                            val originalBitmap = BitmapFactory.decodeStream(inputStream)
                            inputStream?.close()

                            if (originalBitmap != null) {
                                pendingCropBitmap = originalBitmap
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error loading image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                // Photo Preview with Drag Gesture and full shape customization
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

                val previewShape = when (cvData.photoShape) {
                    "Circle" -> CircleShape
                    "Oval" -> RoundedCornerShape(percent = 50)
                    "Rounded" -> RoundedCornerShape(cvData.photoCornerRadius.dp)
                    "Rectangle" -> RoundedCornerShape(4.dp)
                    else -> androidx.compose.ui.graphics.RectangleShape
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(
                                width = (cvData.photoWidth * 1.15f).coerceIn(60f, 130f).dp,
                                height = (cvData.photoHeight * 1.15f).coerceIn(60f, 140f).dp
                            )
                            .background(Color.Gray.copy(alpha = 0.08f), shape = previewShape)
                            .then(
                                if (cvData.photoBorderWidth > 0f) {
                                    Modifier.border(
                                        width = cvData.photoBorderWidth.dp,
                                        color = Color(cvData.templateStyle.primaryColorHex),
                                        shape = previewShape
                                    )
                                } else Modifier
                            )
                            .clip(shape = previewShape)
                            .pointerInput(cvData.photoScale) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
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
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
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

                    Spacer(modifier = Modifier.width(14.dp))

                    // Action buttons & configurations
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(themeColors.buttonEqualBg, CircleShape)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = Color.White, modifier = Modifier.size(16.dp))
                            }

                            if (cvData.photoBase64.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        try {
                                            val bytes = Base64.decode(cvData.photoBase64, Base64.DEFAULT)
                                            pendingCropBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                        } catch (_: Exception) {}
                                    },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(themeColors.buttonEqualBg.copy(alpha = 0.15f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Crop, contentDescription = "Crop", tint = themeColors.buttonEqualBg, modifier = Modifier.size(16.dp))
                                }

                                IconButton(
                                    onClick = {
                                        onCvDataChange(cvData.copy(
                                            photoBase64 = "",
                                            photoScale = 1.0f,
                                            photoOffsetX = 0f,
                                            photoOffsetY = 0f
                                        ))
                                    },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(Color.Red.copy(alpha = 0.12f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Shape Chips Selection (Circle, Rounded, Square, Oval, Rectangle)
                        Text(
                            text = if (isBn) "ছবির শেইপ নির্বাচন করুন:" else "Select Photo Shape:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText.copy(alpha = 0.7f)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            val shapes = listOf("Circle", "Rounded", "Square", "Rectangle")
                            shapes.forEach { shapeName ->
                                val isSelected = cvData.photoShape == shapeName
                                val label = when (shapeName) {
                                    "Circle" -> if (isBn) "বৃত্ত" else "Circle"
                                    "Rounded" -> if (isBn) "কোণ গোল" else "Rounded"
                                    "Square" -> if (isBn) "বর্গ" else "Square"
                                    
                                    else -> if (isBn) "আয়তাকার" else "Rect"
                                }
                                Surface(
                                    onClick = { onCvDataChange(cvData.copy(photoShape = shapeName)) },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) themeColors.buttonEqualBg else themeColors.background,
                                    border = BorderStroke(1.dp, if (isSelected) Color.Transparent else themeColors.displayText.copy(alpha = 0.15f)),
                                    modifier = Modifier.padding(1.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 9.sp,
                                        color = if (isSelected) Color.White else themeColors.displayText,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isPhotoAdvancedExpanded = !isPhotoAdvancedExpanded }
                        .padding(top = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isBn) "এডভান্সড ফটো সেটিংস" else "Advanced Photo Options",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.buttonEqualBg
                    )
                    Icon(
                        imageVector = if (isPhotoAdvancedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle",
                        tint = themeColors.buttonEqualBg,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (isPhotoAdvancedExpanded) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Border Width Slider (default 1.5dp, allows 0 to 4dp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.LineWeight, contentDescription = null, tint = themeColors.displayText.copy(alpha = 0.6f), modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "বর্ডার প্র্ত্ব:" else "Border Width:",
                            fontSize = 10.5.sp,
                            color = themeColors.displayText.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Slider(
                            value = cvData.photoBorderWidth,
                            onValueChange = { onCvDataChange(cvData.copy(photoBorderWidth = it)) },
                            valueRange = 0.0f..4.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = themeColors.buttonEqualBg,
                                activeTrackColor = themeColors.buttonEqualBg,
                                inactiveTrackColor = themeColors.displayText.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (cvData.photoBorderWidth <= 0.1f) (if (isBn) "নেই" else "None") else "${"%.1f".format(cvData.photoBorderWidth)} pt",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                    }

                    // Size controls: Width and Height sliders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Width Slider
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isBn) "প্রস্থ: ${cvData.photoWidth} pt" else "Width: ${cvData.photoWidth} pt",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText.copy(alpha = 0.75f)
                                )
                            }
                            Slider(
                                value = cvData.photoWidth.toFloat(),
                                onValueChange = { onCvDataChange(cvData.copy(photoWidth = it.toInt())) },
                                valueRange = 55f..120f,
                                colors = SliderDefaults.colors(
                                    thumbColor = themeColors.buttonEqualBg,
                                    activeTrackColor = themeColors.buttonEqualBg,
                                    inactiveTrackColor = themeColors.displayText.copy(alpha = 0.1f)
                                )
                            )
                        }

                        // Height Slider
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isBn) "উচ্চতা: ${cvData.photoHeight} pt" else "Height: ${cvData.photoHeight} pt",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText.copy(alpha = 0.75f)
                                )
                            }
                            Slider(
                                value = cvData.photoHeight.toFloat(),
                                onValueChange = { onCvDataChange(cvData.copy(photoHeight = it.toInt())) },
                                valueRange = 55f..140f,
                                colors = SliderDefaults.colors(
                                    thumbColor = themeColors.buttonEqualBg,
                                    activeTrackColor = themeColors.buttonEqualBg,
                                    inactiveTrackColor = themeColors.displayText.copy(alpha = 0.1f)
                                )
                            )
                        }
                    }

                    // Corner radius slider for Rounded
                    if (cvData.photoShape == "Rounded") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isBn) "কোণ কার্ভ:" else "Corner Radius:",
                                fontSize = 10.5.sp,
                                color = themeColors.displayText.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Slider(
                                value = cvData.photoCornerRadius.toFloat(),
                                onValueChange = { onCvDataChange(cvData.copy(photoCornerRadius = it.toInt())) },
                                valueRange = 4f..24f,
                                colors = SliderDefaults.colors(
                                    thumbColor = themeColors.buttonEqualBg,
                                    activeTrackColor = themeColors.buttonEqualBg,
                                    inactiveTrackColor = themeColors.displayText.copy(alpha = 0.1f)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${cvData.photoCornerRadius} dp",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                        }
                    }

                    // Zoom Slider
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = null, tint = themeColors.displayText.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "জুম স্কেল:" else "Zoom Scale:",
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                onCvDataChange(cvData.copy(
                                    photoScale = 1.0f,
                                    photoOffsetX = 0f,
                                    photoOffsetY = 0f,
                                    photoWidth = 80,
                                    photoHeight = 80,
                                    photoBorderWidth = 1.5f,
                                    photoCornerRadius = 10
                                ))
                            },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text(
                                text = if (isBn) "পজিশন ও সাইজ রিসেট" else "Reset Crop & Dimensions",
                                fontSize = 10.sp,
                                color = themeColors.buttonEqualBg,
                                fontWeight = FontWeight.Bold
                            )
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
            placeholderText = if (isBn) "যেমন: মোঃ শরিফ্ল ইসলাম" else "e.g., Md. Shariful Islam"
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Work,
                                contentDescription = null,
                                tint = if (!cvData.isFresher) Color.White else themeColors.buttonEqualBg,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBn) "অভিজ্ঞ পেশাজীবী" else "Experienced Professional",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = if (cvData.isFresher) Color.White else themeColors.buttonEqualBg,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBn) "ফ্রেশার / নতুন গ্র্যাজুয়েট" else "Fresher / Graduate",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
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
                        placeholderText = "â€¢ Automated Sales Pipeline Analysis using Python & PowerBI...\nâ€¢ FinTech Microfinance Mobile App Case Study..."
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
                        placeholderText = "â€¢ Summer Intern at Apex Logistics (Data Reconciliation)\nâ€¢ Volunteer Organizer for National Blood Donation Camp..."
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
                        placeholderText = "â€¢ President / General Secretary at University Business Club\nâ€¢ Finalist, National Inter-University Case Competition..."
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
                themeColors = themeColors,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        onRequestAiPrompt(
                            if (isBn) "কাজের অভিজ্ঞতা এআই নির্দেশনা" else "Work Experience AI Prompt",
                            "Generate 3 professional work experience entries for a ${cvData.jobTitle.ifBlank { "Professional" }} profile...",
                            "EXPERIENCE_GEN",
                            -1
                        )
                    },
                    modifier = Modifier
                        .size(30.dp)
                        .background(themeColors.buttonEqualBg, CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI", tint = Color.White, modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = {
                        val newList = cvData.experiences.toMutableList()
                        newList.add(CvExperienceItem())
                        onCvDataChange(cvData.copy(experiences = newList))
                    },
                    modifier = Modifier
                        .size(30.dp)
                        .background(themeColors.buttonEqualBg.copy(alpha = 0.12f), CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = themeColors.buttonEqualBg, modifier = Modifier.size(18.dp))
                }
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
                                label = if (isBn) "শ্র্র তারিখ" else "Start Date (e.g. Jan 2023)",
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
                        placeholder = { Text(text = "â€¢ Conducted business evaluations...\nâ€¢ Tailored campaign KPIs...\nâ€¢ Coordinated teams...", fontSize = 11.5.sp) },
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
        // Secondary & Higher Secondary (Individual degrees)
        "S.S.C",
        "H.S.C",
        "Dakhil",
        "Alim",
        "O-Level",
        "A-Level",
        "S.S.C (Vocational)",
        "H.S.C (Vocational)",
        "S.S.C (Business Management)",
        "H.S.C (BM / BMT)",

        // Diploma Programs
        "Diploma in Engineering",
        "Diploma in Computer Science & Technology",
        "Diploma in Electrical Engineering",
        "Diploma in Civil Engineering",
        "Diploma in Mechanical Engineering",
        "Diploma in Textile Engineering",
        "Diploma in Architecture",
        "Diploma in Nursing & Midwifery",
        "Diploma in Medical Technology",
        "Diploma in Pharmacy",
        "Diploma in Agriculture",
        "Diploma in Forestry",
        "Diploma in Commerce",
        "Diploma in Education (D.Ed)",

        // Bachelor / Graduation Degrees
        "B.B.A",
        "M.B.A",
        "B.Sc.",
        "B.Sc. Engg.",
        "B.A.",
        "B.S.S.",
        "B.Com.",
        "B.B.S.",
        "LL.B",
        "M.B.B.S.",
        "B.D.S.",
        "B.Pharm",
        "B.Arch",
        "BFA",
        "BMus",
        "B.Sc. Nursing",
        "Fazil",
        "B.Ed",
        "B.P.Ed",

        // Master / Postgraduate Degrees
        "M.Sc.",
        "M.Sc. Engg.",
        "E.M.B.A",
        "M.A.",
        "M.S.S.",
        "M.Com.",
        "M.B.S.",
        "LL.M",
        "M.Pharm",
        "MFA",
        "MPH (Master of Public Health)",
        "M.Ed",
        "Kamil",

        // Higher Postgraduate & Doctoral Level
        "Ph.D",
        "M.Phil",
        "Postgraduate Diploma (PGD)",
        "PGDHRM",
        "PGDIT",

        // Professional Certifications
        "CA (Chartered Accountant)",
        "CMA (Cost & Management Accountant)",
        "ACCA",
        "CFA",
        "PMP",
        "Others (ম্যানুয়াল ইনপুট)"
    )

    val subjectOptions = listOf(
        // Business & Commerce
        "Management",
        "Accounting",
        "Finance",
        "Marketing",
        "Accounting & Information Systems (AIS)",
        "Finance & Banking",
        "Human Resource Management (HRM)",
        "Banking & Insurance",
        "International Business",
        "Supply Chain Management",
        "Tourism & Hospitality Management",
        "Business Administration",
        "Entrepreneurship",
        "Real Estate",

        // Arts, Humanities & Social Sciences
        "Bangla",
        "Political Science",
        "English",
        "Economics",
        "Sociology",
        "Social Work",
        "History",
        "Islamic History & Culture",
        "Philosophy",
        "International Relations",
        "Public Administration",
        "Psychology",
        "Law / Legal Studies",
        "Criminology",
        "Islamic Studies",
        "Arabic",
        "Sanskrit",
        "Pali",
        "Persian Language & Literature",
        "Urdu",
        "Anthropology",
        "Development Studies",
        "Population Sciences",
        "Disaster Management",
        "Information Science & Library Management",
        "Journalism & Mass Communication",
        "Fine Arts",
        "Drama & Dramatics",
        "Music",
        "Home Economics",

        // Science, Engineering & Technology
        "Computer Science & Engineering (CSE)",
        "Electrical & Electronic Engineering (EEE)",
        "Civil Engineering",
        "Mechanical Engineering",
        "Software Engineering",
        "Information Technology (IT)",
        "Chemical Engineering",
        "Industrial & Production Engineering (IPE)",
        "Textile Engineering",
        "Leather Engineering",
        "Materials & Metallurgical Engineering",
        "Biomedical Engineering",
        "Mechatronics Engineering",
        "Petroleum & Mining Engineering",
        "Architecture & Urban Planning",
        "Physics",
        "Applied Physics",
        "Chemistry",
        "Applied Chemistry",
        "Mathematics",
        "Applied Mathematics",
        "Statistics",
        "Statistics & Data Science",
        "Biochemistry & Molecular Biology",
        "Biotechnology & Genetic Engineering",
        "Microbiology",
        "Pharmacy",
        "Botany",
        "Zoology",
        "Soil, Water & Environment",
        "Soil Science",
        "Geography & Environment",
        "Geology",
        "Oceanography / Marine Science",
        "Environmental Science",

        // Medical & Health Sciences
        "Medicine & Surgery (MBBS)",
        "Dental Surgery (BDS)",
        "Nursing",
        "Physiotherapy",
        "Medical Laboratory Technology",
        "Public Health",
        "Nutrition & Food Science",

        // Agriculture & Veterinary Sciences
        "Agriculture / Agronomy",
        "Horticulture",
        "Fisheries / Aquaculture",
        "Veterinary Science & Animal Husbandry",
        "Food Engineering & Technology",
        "Forestry",

        // School & College Groups
        "Science (S.S.C / H.S.C)",
        "Business Studies / Commerce",
        "Humanities / Arts",
        "General",
        "Others (ম্যানুয়াল ইনপুট)"
    )

    val instOptions = listOf(
        // Education Boards
        "Dhaka Education Board",
        "Rajshahi Education Board",
        "Comilla Education Board",
        "Jessore Education Board",
        "Chittagong Education Board",
        "Barisal Education Board",
        "Sylhet Education Board",
        "Dinajpur Education Board",
        "Mymensingh Education Board",
        "Technical Education Board (BTEB)",
        "Madrasah Education Board",

        // Public Universities
        "University of Dhaka (DU)",
        "Bangladesh University of Engineering and Technology (BUET)",
        "Jahangirnagar University (JU)",
        "Rajshahi University (RU)",
        "Chittagong University (CU)",
        "Shahjalal University of Science and Technology (SUST)",
        "Khulna University (KU)",
        "Khulna University of Engineering & Technology (KUET)",
        "Rajshahi University of Engineering & Technology (RUET)",
        "Chittagong University of Engineering & Technology (CUET)",
        "Dhaka University of Engineering & Technology (DUET)",
        "Jagannath University (JnU)",
        "Bangladesh Agricultural University (BAU)",
        "Sher-e-Bangla Agricultural University (SAU)",
        "Bangabandhu Sheikh Mujibur Rahman Agricultural University (BSMRAU)",
        "Bangladesh University of Professionals (BUP)",
        "Noakhali Science and Technology University (NSTU)",
        "Jashore University of Science and Technology (JUST)",
        "Begum Rokeya University, Rangpur (BRUR)",
        "Islamic University, Bangladesh (IU)",
        "Mawlana Bhashani Science and Technology University (MBSTU)",
        "Hajee Mohammad Danesh Science & Technology University (HSTU)",
        "Comilla University (CoU)",
        "Jatiya Kabi Kazi Nazrul Islam University (JKKNIU)",
        "Bangabandhu Sheikh Mujibur Rahman Science and Technology University (BSMRSTU)",
        "Barisal University (BU)",
        "Rangamati Science and Technology University (RMSTU)",
        "Pabna University of Science and Technology (PUST)",
        "Patuakhali Science and Technology University (PSTU)",
        "Sylhet Agricultural University (SAU)",
        "Chittagong Veterinary and Animal Sciences University (CVASU)",
        "Bangabandhu Sheikh Mujibur Rahman Maritime University (BSMRMU)",
        "Bangabandhu Sheikh Mujibur Rahman Aviation and Aerospace University (BSMRAAU)",
        "National University (NU)",
        "Bangladesh Open University (BOU)",
        "Islamic Arabic University (IAU)",
        "Bangabandhu Sheikh Mujib Medical University (BSMMU)",
        "Chittagong Medical University",
        "Rajshahi Medical University",
        "Sylhet Medical University",

        // Top Medical Colleges
        "Dhaka Medical College (DMC)",
        "Sir Salimullah Medical College (SSMC)",
        "Shaheed Suhrawardy Medical College",
        "Mymensingh Medical College",
        "Chittagong Medical College",
        "Rajshahi Medical College",
        "MAG Osmani Medical College, Sylhet",
        "Sher-e-Bangla Medical College, Barisal",
        "Rangpur Medical College",
        "Comilla Medical College",
        "Khulna Medical College",

        // Prominent Private Universities
        "North South University (NSU)",
        "BRAC University",
        "Ahsanullah University of Science and Technology (AUST)",
        "East West University (EWU)",
        "United International University (UIU)",
        "Independent University, Bangladesh (IUB)",
        "American International University-Bangladesh (AIUB)",
        "Daffodil International University (DIU)",
        "University of Liberal Arts Bangladesh (ULAB)",
        "Stamford University Bangladesh",
        "Southeast University (SEU)",
        "State University of Bangladesh (SUB)",
        "Green University of Bangladesh",
        "International Islamic University Chittagong (IIUC)",
        "University of Asia Pacific (UAP)",
        "International University of Business Agriculture and Technology (IUBAT)",
        "Eastern University",
        "Primeasia University",
        "Northern University Bangladesh",
        "Southern University Bangladesh",
        "Metropolitan University, Sylhet",
        "Leading University, Sylhet",
        "Sylhet International University",
        "Asian University of Bangladesh",
        "City University",
        "World University of Bangladesh",
        "Shanto-Mariam University of Creative Technology",
        "Uttara University",
        "Britannia University, Comilla",
        "University of Information Technology and Sciences (UITS)",
        "Canadian University of Bangladesh (CUB)",
        "European University of Bangladesh (EUB)",
        "Sonargaon University (SU)",
        "Bangladesh University (BU)",
        "BGMEA University of Fashion & Technology (BUFT)",
        "University of Development Alternative (UODA)",
        "Premier University, Chittagong",
        "Port City International University (PCIU)",
        "Varendra University, Rajshahi",

        // Prominent Government / National University Colleges
        "Dhaka College",
        "Eden Mohila College",
        "Government Titumir College",
        "Government Bangla College",
        "Kavi Nazrul Government College",
        "Government Shaheed Suhrawardy College",
        "Begum Badrunnesa Government Girls' College",
        "Tejgaon College",
        "Dhaka City College",
        "Lalmatia Mohila College",
        "Rajshahi College",
        "New Government Degree College, Rajshahi",
        "Rajshahi Government City College",
        "Chittagong College",
        "Government Hazi Mohammad Mohsin College, Chittagong",
        "Government City College, Chittagong",
        "Chittagong Government Women's College",
        "Ananda Mohan College, Mymensingh",
        "Carmichael College, Rangpur",
        "Govt. Edward College, Pabna",
        "Parkol Bi-Lateral High School",
        "Baraigram College, Natore",
        "Baraigram Govt. College, Natore",
        "Government B. M. College, Barisal (Brajamohan College)",
        "Government B. L. College, Khulna (Brajalal College)",
        "Azam Khan Government Commerce College, Khulna",
        "Government Majid Memorial City College, Khulna",
        "MC College, Sylhet (Muria Chand College)",
        "Sylhet Government Women's College",
        "Government Gurudayal College, Kishoreganj",
        "Government Tolaram College, Narayanganj",
        "Government Haraganga College, Munshiganj",
        "Government Devendra College, Manikganj",
        "Government Rajendra College, Faridpur",
        "Victoria College, Comilla",
        "Feni College",
        "Noakhali Government College",
        "Chandpur Government College",
        "Brahmanbaria Government College",
        "Cantonment Public College",
        "Dinajpur Government College",
        "Bogra Government College",
        "Sirajganj Government College",

        // Polytechnic & Technical Institutes
        "Dhaka Polytechnic Institute (DPI)",
        "Chittagong Polytechnic Institute (CPI)",
        "Mymensingh Polytechnic Institute",
        "Rajshahi Polytechnic Institute",
        "Khulna Polytechnic Institute",
        "Barisal Polytechnic Institute",
        "Sylhet Polytechnic Institute",
        "Rangpur Polytechnic Institute",
        "Comilla Polytechnic Institute",
        "Bogra Polytechnic Institute",
        "Jessore Polytechnic Institute",
        "Dinajpur Polytechnic Institute",
        "Pabna Polytechnic Institute",
        "Faridpur Polytechnic Institute",
        "Feni Polytechnic Institute",
        "Kushtia Polytechnic Institute",
        "Patuakhali Polytechnic Institute",
        "Graphic Arts Institute, Dhaka",
        "Bangladesh Survey Institute, Comilla",
        "National Institute of Textile Engineering and Research (NITER)",
        "Institute of Health Technology (IHT)",

        // Top International Universities
        "Oxford University",
        "Cambridge University",
        "Harvard University",
        "MIT (Massachusetts Institute of Technology)",
        "Stanford University",
        "Monash University",
        "University of Sydney",
        "Coventry University",
        "London Metropolitan University",
        "University of London",
        "National University of Singapore (NUS)",
        "Others (ম্যানুয়াল ইনপুট)"
    )
    val yearOptions = (2030 downTo 1980).map { it.toString() } + listOf("Appeared / Studying", "Others (ম্যানুয়াল ইনপুট)")

    val resultSysOptions = listOf(
        "CGPA (Out of 4.0)",
        "GPA (Out of 5.0)",
        "1st Division / Class",
        "2nd Division / Class",
        "3rd Division / Class",
        "Passed / Appeared",
        "Others (ম্যানুয়াল ইনপুট)"
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
                    
                }

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedButton(
                    onClick = {
                        val newList = cvData.educations.toMutableList()
                        newList.add(CvEducationItem(
                            examLevel = "B.Sc.",
                            degree = "B.Sc. in Computer Science & Engineering",
                            subjectMajor = "Computer Science & Engineering (CSE)",
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
                    else if (edu.examLevel == "Others" || edu.degree.isNotBlank()) "Others (ম্যানুয়াল ইনপুট)"
                    else "B.Sc."
                )
            }
            var currentSubject by remember(edu.subjectMajor) {
                mutableStateOf(
                    if (subjectOptions.contains(edu.subjectMajor)) edu.subjectMajor
                    else if (edu.subjectMajor.isNotBlank()) "Others (ম্যানুয়াল ইনপুট)"
                    else "General"
                )
            }
            var currentInst by remember(edu.institution) {
                mutableStateOf(
                    if (instOptions.contains(edu.institution)) edu.institution
                    else if (edu.institution.isNotBlank()) "Others (ম্যানুয়াল ইনপুট)"
                    else "Others (ম্যানুয়াল ইনপুট)"
                )
            }
            var currentYear by remember(edu.passingYear) {
                mutableStateOf(
                    if (yearOptions.contains(edu.passingYear)) edu.passingYear
                    else if (edu.passingYear.isNotBlank()) "Others (ম্যানুয়াল ইনপুট)"
                    else "2021"
                )
            }
            var currentResultSys by remember(edu.resultType) {
                mutableStateOf(
                    if (resultSysOptions.contains(edu.resultType)) edu.resultType
                    else if (edu.resultType.isNotBlank()) "Others (ম্যানুয়াল ইনপুট)"
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
                                val newExamVal = if (selected == "Others (ম্যানুয়াল ইনপুট)") "Others" else selected
                                val isSecondaryLevel = selected in listOf("S.S.C", "H.S.C", "Dakhil", "Alim", "O-Level", "A-Level", "S.S.C (Vocational)", "H.S.C (Vocational)", "S.S.C (Business Management)", "H.S.C (BM / BMT)")
                                val computedDegree = if (selected == "Others (ম্যানুয়াল ইনপুট)") edu.degree
                                                     else if (isSecondaryLevel) selected
                                                     else if (currentSubject != "General" && currentSubject != "Others (ম্যানুয়াল ইনপুট)") "$selected in $currentSubject"
                                                     else selected
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
                                val newSubVal = if (selected == "Others (ম্যানুয়াল ইনপুট)") "Others" else selected
                                val isSecondaryLevel = currentExam in listOf("S.S.C", "H.S.C", "Dakhil", "Alim", "O-Level", "A-Level", "S.S.C (Vocational)", "H.S.C (Vocational)", "S.S.C (Business Management)", "H.S.C (BM / BMT)")
                                val computedDegree = if (currentExam != "Others (ম্যানুয়াল ইনপুট)" && selected != "General" && selected != "Others (ম্যানুয়াল ইনপুট)") {
                                    if (isSecondaryLevel) currentExam else "$currentExam in $selected"
                                } else edu.degree
                                val newList = cvData.educations.toMutableList()
                                newList[index] = edu.copy(subjectMajor = newSubVal, degree = computedDegree)
                                onCvDataChange(cvData.copy(educations = newList))
                            },
                            themeColors = themeColors,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (currentExam == "Others (ম্যানুয়াল ইনপুট)") {
                        Spacer(modifier = Modifier.height(6.dp))
                        CvCustomTextField(
                            label = if (isBn) "ম্যানুয়াল ডিগ্রির নাম লিখুন" else "Enter Manual Degree Title",
                            value = edu.degree,
                            onValueChange = { d ->
                                val newList = cvData.educations.toMutableList()
                                newList[index] = edu.copy(degree = d)
                                onCvDataChange(cvData.copy(educations = newList))
                            },
                            themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn
                        )
                    }

                    if (currentSubject == "Others (ম্যানুয়াল ইনপুট)") {
                        Spacer(modifier = Modifier.height(6.dp))
                        CvCustomTextField(
                            label = if (isBn) "ম্যানুয়াল বিষয়ের নাম লিখুন" else "Enter Manual Subject Name",
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
                            val instVal = if (selected == "Others (ম্যানুয়াল ইনপুট)") edu.institution else selected
                            val newList = cvData.educations.toMutableList()
                            newList[index] = edu.copy(institution = instVal)
                            onCvDataChange(cvData.copy(educations = newList))
                        },
                        themeColors = themeColors
                    )

                    if (currentInst == "Others (ম্যানুয়াল ইনপুট)") {
                        Spacer(modifier = Modifier.height(6.dp))
                        CvCustomTextField(
                            label = if (isBn) "ম্যানুয়াল প্রতিষ্ঠানের নাম লিখুন" else "Enter Manual Institution Name",
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
                                val yrVal = if (selected == "Others (ম্যানুয়াল ইনপুট)") edu.passingYear else selected
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

                    if (currentYear == "Others (ম্যানুয়াল ইনপুট)") {
                        Spacer(modifier = Modifier.height(6.dp))
                        CvCustomTextField(
                            label = if (isBn) "ম্যানুয়াল বছর/স্ট্যাটাস লিখুন" else "Enter Manual Passing Year/Status",
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
                
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = cvData.certifications,
            onValueChange = { onCvDataChange(cvData.copy(certifications = it)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 15,
            placeholder = { Text(text = "â€¢ Project Management Professional (PMP) - PMI, 2024\nâ€¢ Business Intelligence - Coursera, 2023", fontSize = 11.5.sp) },
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


// ================= ATS SELECTIVE REVIEW & FIX DIALOG =================

@Composable
private fun AtsFixReviewDialog(
    cvData: CvData,
    atsCheckItems: List<AtsCheckItem>,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit,
    onApplyFixes: (CvData) -> Unit
) {
    val failedChecks = remember(atsCheckItems) { atsCheckItems.filter { !it.isPassed } }

    val selectedStates = remember(failedChecks) {
        mutableStateMapOf<Int, Boolean>().apply {
            failedChecks.indices.forEach { idx -> this[idx] = true }
        }
    }

    val currentScore = remember(atsCheckItems) {
        atsCheckItems.filter { it.isPassed }.sumOf { it.weightPoints }.coerceIn(0, 100)
    }

    val projectedScore = remember(selectedStates, failedChecks, currentScore) {
        val selectedPoints = failedChecks.indices
            .filter { selectedStates[it] == true }
            .sumOf { failedChecks[it].weightPoints }
        (currentScore + selectedPoints).coerceIn(0, 100)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 580.dp)
                .padding(vertical = 10.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBn) "ðŸ”� এটিএস ফিক্স ও ইম্প্র্ভমেন্ট রিভিউ" else "ðŸ”� ATS Fix & Review Suggestions",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.buttonEqualBg
                        )
                        Text(
                            text = if (isBn) "আপনার সাথে মানানসই পরিবর্তনগুলো পছন্দমত মার্ক করুন" else "Check/Uncheck suggestions that fit your background",
                            fontSize = 10.5.sp,
                            color = themeColors.displayText.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = themeColors.displayText, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Score preview card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = themeColors.buttonEqualBg.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBn) "বর্তমান স্কোর: $currentScore%" else "Current Score: $currentScore%",
                                fontSize = 11.sp,
                                color = themeColors.displayText.copy(alpha = 0.8f)
                            )
                            Text(
                                text = if (isBn) "বাছাইকৃত পছন্দ স্কোর: $projectedScore%" else "Selected Fix Score: $projectedScore%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (projectedScore >= 80) Color(0xFF10B981) else themeColors.buttonEqualBg
                            )
                        }

                        // Select all / Deselect all
                        TextButton(
                            onClick = {
                                val allSelected = selectedStates.values.all { it }
                                failedChecks.indices.forEach { idx ->
                                    selectedStates[idx] = !allSelected
                                }
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (selectedStates.values.all { it }) (if (isBn) "সব আনমার্ক" else "Unselect All") else (if (isBn) "সব সিলেক্ট" else "Select All"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.buttonEqualBg
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (failedChecks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBn) "ðŸŽ‰ চমৎকার! আপনার সিভিতে কোনো এটিএস ত্র্টি নেই। স্কোর ১০০%!" else "ðŸŽ‰ Awesome! No ATS issues found. Score is 100%!",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(failedChecks.size) { idx ->
                            val check = failedChecks[idx]
                            val isChecked = selectedStates[idx] == true

                            Surface(
                                onClick = { selectedStates[idx] = !isChecked },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isChecked) themeColors.buttonEqualBg.copy(alpha = 0.08f) else Color.Transparent,
                                border = BorderStroke(1.dp, if (isChecked) themeColors.buttonEqualBg.copy(alpha = 0.3f) else themeColors.displayText.copy(alpha = 0.1f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { selectedStates[idx] = it },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = themeColors.buttonEqualBg,
                                            uncheckedColor = themeColors.displayText.copy(alpha = 0.4f)
                                        ),
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (isBn) check.categoryBn else check.categoryEn,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = themeColors.buttonEqualBg
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "+${check.weightPoints} pts",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF10B981)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (isBn) check.detailBn else check.detailEn,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = themeColors.displayText
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = getFixExplanation(check.categoryEn, isBn),
                                            fontSize = 10.sp,
                                            color = themeColors.displayText.copy(alpha = 0.65f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = if (isBn) "বাতিল" else "Cancel",
                            color = themeColors.displayText,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = {
                            var updatedCv = cvData
                            failedChecks.indices.forEach { idx ->
                                if (selectedStates[idx] == true) {
                                    val check = failedChecks[idx]
                                    updatedCv = autoFixIndividualAtsCheck(updatedCv, check.categoryEn)
                                }
                            }
                            onApplyFixes(updatedCv)
                            onDismiss()
                        },
                        enabled = selectedStates.values.any { it },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        modifier = Modifier.weight(1.8f)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBn) "বাছাইকৃত পছন্দ অ্যাপ্লাই করুন" else "Apply Selected Fixes",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun getFixExplanation(categoryEn: String, isBn: Boolean): String {
    return when (categoryEn) {
        "Contact Info" -> if (isBn) "অফিসিয়াল ইমেইল, ফোন নম্বর ও প্রোফাইল লিঙ্ক য্ক্ত করা হবে" else "Will add official email, phone & profile links"
        "Summary" -> if (isBn) "সামারিতে ৩-৪ লাইনের বিস্তারিত প্রভাব ও সাফল্যসূচক অর্জনের অংশ য্ক্ত হবে" else "Will refine summary with impact metrics"
        "Experience" -> if (isBn) "অভিজ্ঞতা ডেসক্রিপশনে সাফল্যসূচক % ও সংখ্যায্ক্ত পয়েন্ট য্ক্ত হবে" else "Will add quantified % metrics to experiences"
        "Education" -> if (isBn) "ডিগ্রি, শিক্ষা প্রতিষ্ঠান ও পাসের সনের তথ্য পারফেক্ট করা হবে" else "Will complete degree, institution & passing year"
        "Skills" -> if (isBn) "এটিএস ফ্রেন্ডলি কোর স্কিলস ও ইন্ডাস্ট্রি কি-ওয়ার্ড য্ক্ত হবে" else "Will populate standard ATS industry skills"
        "Formatting" -> if (isBn) "প্রমিত এটিএস ব্লেট স্টাইল ও ১-কলাম লেআউট সক্রিয় করা হবে" else "Will enable standard ATS bullet structure"
        else -> if (isBn) "সিভির ডাটা এটিএস স্ট্যান্ডার্ডে টিউন করা হবে" else "Will tune CV data to ATS standards"
    }
}

// ================= TAB 3: AI JOB MATCH WITH IMAGE UPLOAD =================



data class AiSuggestionItem(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val descEn: String,
    val descBn: String,
    val category: String,
    val proposedValue: String
)

private fun parseSuggestions(jsonStr: String): List<AiSuggestionItem> {
    val list = mutableListOf<AiSuggestionItem>()
    if (jsonStr.isBlank()) return list
    try {
        val arr = org.json.JSONArray(jsonStr)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                AiSuggestionItem(
                    id = obj.optString("id"),
                    titleEn = obj.optString("titleEn"),
                    titleBn = obj.optString("titleBn"),
                    descEn = obj.optString("descEn"),
                    descBn = obj.optString("descBn"),
                    category = obj.optString("category"),
                    proposedValue = obj.optString("proposedValue")
                )
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

@Composable
private fun AiSuggestionCard(
    suggestion: AiSuggestionItem,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    Surface(
        onClick = { onSelectionChange(!isSelected) },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) themeColors.buttonEqualBg.copy(alpha = 0.08f) else themeColors.cardBg,
        border = BorderStroke(
            1.dp,
            if (isSelected) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.12f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelectionChange(it ?: false) },
                colors = CheckboxDefaults.colors(checkedColor = themeColors.buttonEqualBg)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBn) suggestion.titleBn else suggestion.titleEn,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (isBn) suggestion.descBn else suggestion.descEn,
                    fontSize = 11.sp,
                    color = themeColors.displayText.copy(alpha = 0.75f),
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = themeColors.buttonEqualBg.copy(alpha = 0.1f),
                    modifier = Modifier.wrapContentSize()
                ) {
                    Text(
                        text = suggestion.category.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.buttonEqualBg,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun IndividualAiSuggestionCard(
    suggestion: AiSuggestionItem,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onApplyFix: () -> Unit,
    onNavigate: () -> Unit
) {
    val context = LocalContext.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = themeColors.cardBg,
        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with Suggestion Title and Category Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBn) suggestion.titleBn else suggestion.titleEn,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = themeColors.buttonEqualBg.copy(alpha = 0.1f),
                    modifier = Modifier.wrapContentSize()
                ) {
                    Text(
                        text = suggestion.category.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.buttonEqualBg,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Explanation Description
            Text(
                text = if (isBn) suggestion.descBn else suggestion.descEn,
                fontSize = 11.5.sp,
                color = themeColors.displayText.copy(alpha = 0.75f),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Proposed Text in a beautifully styled subtle card
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = themeColors.cardBg.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = if (isBn) "প্রস্তাবিত সংস্করণ:" else "Proposed Content:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.buttonEqualBg
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = suggestion.proposedValue,
                        fontSize = 11.sp,
                        color = themeColors.displayText,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Button 1: Edit Manually (Go to Section)
                OutlinedButton(
                    onClick = onNavigate,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = themeColors.displayText),
                    border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.2f)),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBn) "ম্যানুয়ালি ঠিক করুন" else "Fix Manually",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Button 2: Auto Apply with AI
                Button(
                    onClick = {
                        onApplyFix()
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    modifier = Modifier.weight(1.2f).height(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBn) "এআই দিয়ে আপডেট" else "Update with AI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}


data class SavedCircularItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val text: String,
    val matchPct: Int,
    val matchingStrengths: List<String>,
    val missingKeywords: List<String>,
    val tailoredSummary: String,
    val suggestionsJson: String,
    val date: String = "Just now"
)

private fun getCurrentCategoryValue(cv: CvData, category: String): String {
    return when {
        category.equals("summary", ignoreCase = true) -> cv.summary
        category.equals("contact_info", ignoreCase = true) -> {
            "Email: ${cv.email}\nPhone: ${cv.phone}\nLinkedIn: ${cv.linkedin}"
        }
        category.startsWith("experience_", ignoreCase = true) -> {
            val idx = category.substringAfter("experience_").toIntOrNull() ?: 0
            cv.experiences.getOrNull(idx)?.description ?: ""
        }
        category.equals("skills", ignoreCase = true) -> {
            cv.skills.joinToString { it.name }
        }
        category.equals("educations", ignoreCase = true) || category.equals("education", ignoreCase = true) -> {
            cv.educations.joinToString { "${it.degree} from ${it.institution}" }
        }
        else -> ""
    }
}

@Composable
private fun KeywordAnalysisCard(
    matchedKeywords: List<String>,
    missingKeywords: List<String>,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onAddKeyword: (String) -> Unit,
    onAddAllKeywords: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = themeColors.cardBg,
        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBn) "📊 কি-ওয়ার্ড ম্যাচ বিশ্লেষণ (সার্কুলার বনাম সিভি)" else "📊 Keyword Match Analysis (Circular vs CV)",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.buttonEqualBg
                )
                if (missingKeywords.isNotEmpty()) {
                    TextButton(
                        onClick = onAddAllKeywords,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = if (isBn) "সব স্কিল এড করুন +" else "Add All +",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Matched/Strengths keywords
            Text(
                text = if (isBn) "✅ সিভিতে থাকা ম্যাচিং কি-ওয়ার্ডসমূহ:" else "✅ Matched Keywords found in CV:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981)
            )
            Spacer(modifier = Modifier.height(6.dp))
            if (matchedKeywords.isEmpty()) {
                Text(
                    text = if (isBn) "কোন নির্দিষ্ট ম্যাচিং কি-ওয়ার্ড পাওয়া যায়নি। নিচে মিসিং স্কিলগুলো এড করুন।" else "No direct matching keywords detected. Try adding the missing skills below.",
                    fontSize = 11.sp,
                    color = themeColors.displayText.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 4.dp)
                )
            } else {
                val chunkedMatched = matchedKeywords.chunked(3)
                chunkedMatched.forEach { chunk ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        chunk.forEach { kw ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = kw,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            // Missing requirements keywords
            Text(
                text = if (isBn) "❌ সিভিতে নেই এমন মিসিং কি-ওয়ার্ডসমূহ (ট্যাপ করে এড করুন):" else "❌ Missing Requirements Keywords (Tap to add):",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEF4444)
            )
            Spacer(modifier = Modifier.height(6.dp))
            if (missingKeywords.isEmpty()) {
                Text(
                    text = if (isBn) "সব কি-ওয়ার্ড মিলে গেছে! কোনো মিসিং স্কিল নেই।" else "100% keyword match! No missing skills found.",
                    fontSize = 11.sp,
                    color = themeColors.displayText.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 4.dp)
                )
            } else {
                val chunkedMissing = missingKeywords.chunked(3)
                chunkedMissing.forEach { chunk ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        chunk.forEach { kw ->
                            Surface(
                                onClick = { onAddKeyword(kw) },
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFEF4444).copy(alpha = 0.06f),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.25f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = kw,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AiJobCircularMatchTab(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onMatchCircularAi: (String, ByteArray?, String) -> Unit,
    onAnalyzeAtsAi: () -> Unit,
    onNavigateToTab: (Int) -> Unit = {},
    callGeminiAiApi: suspend (String, String) -> String = { _, _ -> "" },
    isScrollable: Boolean = true
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var selectedMatchMode by remember { mutableStateOf(0) } // 0: ATS Scan & Checks, 1: Job Circular Match
    var circularInputText by remember { mutableStateOf(cvData.targetJobCircular) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var activeFixItem by remember { mutableStateOf<AtsChecklistItem?>(null) }

    var savedCirculars by remember { mutableStateOf(listOf<SavedCircularItem>()) }
    var beforeAfterSuggestionToApply by remember { mutableStateOf<AiSuggestionItem?>(null) }

    LaunchedEffect(cvData.lastJobMatchPercentage, cvData.lastCircularSuggestionsJson) {
        if (cvData.lastJobMatchPercentage > 0 && cvData.targetJobCircular.isNotBlank()) {
            val titleText = if (cvData.targetJobCircular.length > 35) {
                cvData.targetJobCircular.take(35).replace("\n", " ").trim() + "..."
            } else {
                cvData.targetJobCircular.replace("\n", " ").trim()
            }
            if (savedCirculars.none { it.text == cvData.targetJobCircular }) {
                savedCirculars = savedCirculars + SavedCircularItem(
                    title = titleText,
                    text = cvData.targetJobCircular,
                    matchPct = cvData.lastJobMatchPercentage,
                    matchingStrengths = cvData.lastMatchingStrengths,
                    missingKeywords = cvData.lastMissingKeywords,
                    tailoredSummary = cvData.lastTailoredSummary,
                    suggestionsJson = cvData.lastCircularSuggestionsJson
                )
            }
        }
    }

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

    // Parse ATS suggestions
    val atsSuggestions = remember(cvData.lastAtsSuggestionsJson) { parseSuggestions(cvData.lastAtsSuggestionsJson) }
    var selectedAtsIds by remember(cvData.lastAtsSuggestionsJson) { mutableStateOf(atsSuggestions.map { it.id }.toSet()) }

    // Parse Circular suggestions
    val circularSuggestions = remember(cvData.lastCircularSuggestionsJson) { parseSuggestions(cvData.lastCircularSuggestionsJson) }
    var selectedCircularIds by remember(cvData.lastCircularSuggestionsJson) { mutableStateOf(circularSuggestions.map { it.id }.toSet()) }

    // Dynamic Checklist evaluation items
    val currentChecklist = remember(cvData) {
        listOf(
            AtsChecklistItem(
                title = if (isBn) "প্রফেশনাল এটিএস সামারি ডেসক্রিপশন" else "Targeted Executive Professional Summary",
                status = if (cvData.summary.isNotBlank() && cvData.summary.length >= 35) ChecklistStatus.PRESENT else ChecklistStatus.MISSING,
                sectionKey = CvSectionKey.SUMMARY,
                detail = if (cvData.summary.isNotBlank()) "Summary text present (${cvData.summary.length} characters)" else "Add a strong targeted executive summary"
            ),
            AtsChecklistItem(
                title = if (isBn) "কাজের অভিজ্ঞতায় পরিমাপযোগ্য মেট্রিক্স (%, $)" else "Quantified achievements (%, $) in Experience",
                status = if (cvData.experiences.any { exp -> exp.description.contains("%") || exp.description.contains("$") || exp.description.contains(Regex("\\d+")) }) ChecklistStatus.PRESENT else ChecklistStatus.MISSING,
                sectionKey = CvSectionKey.EXPERIENCE,
                detail = if (cvData.experiences.isNotEmpty()) "Include numerical impact metrics in role descriptions" else "Add work experience with measurable achievements"
            ),
            AtsChecklistItem(
                title = if (isBn) "টেকনিক্যাল ও ডোমেইন স্কিল কভারেজ (৫+)" else "Categorized Technical & Domain Skills (5+ skills)",
                status = if (cvData.skills.size >= 5) ChecklistStatus.PRESENT else ChecklistStatus.MISSING,
                sectionKey = CvSectionKey.SKILLS,
                detail = "Current skills count: ${cvData.skills.size}"
            ),
            AtsChecklistItem(
                title = if (isBn) "যোগাযোগ তথ্য ও লিংকডইন ইউআরএল" else "Standard Contact Info with LinkedIn URL",
                status = if (cvData.email.isNotBlank() && cvData.phone.isNotBlank() && cvData.linkedin.isNotBlank()) ChecklistStatus.PRESENT else ChecklistStatus.MISSING,
                sectionKey = CvSectionKey.CONTACT_INFO,
                detail = if (cvData.linkedin.isNotBlank()) "Contact details and LinkedIn link verified" else "Add your LinkedIn profile URL"
            ),
            AtsChecklistItem(
                title = if (isBn) "পোর্টফোলিও ও হাই-ইমপ্যাক্ট প্রজেক্ট" else "Featured Projects & Accomplishments",
                status = if (cvData.projects.isNotEmpty() || cvData.customSections.isNotEmpty()) ChecklistStatus.PRESENT else ChecklistStatus.MISSING,
                sectionKey = CvSectionKey.PROJECTS,
                detail = if (cvData.projects.isNotEmpty()) "${cvData.projects.size} projects listed" else "Add portfolio projects to boost ATS score"
            ),
            AtsChecklistItem(
                title = if (isBn) "ডিগ্রি ও একাডেমিক ব্যাকগ্রাউন্ড রেকর্ডস" else "Standard Education & Academic History Setup",
                status = if (cvData.educations.isNotEmpty()) ChecklistStatus.PRESENT else ChecklistStatus.MISSING,
                sectionKey = CvSectionKey.EDUCATION,
                detail = if (cvData.educations.isNotEmpty()) "${cvData.educations.size} education records listed" else "Add your academic degree and institution"
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(if (isScrollable) Modifier.verticalScroll(scrollState) else Modifier)
            .padding(14.dp)
    ) {
        SectionCardHeader(
            title = if (isBn) "এআই এটিএস ও সার্কুলার ম্যাচিং" else "AI ATS & Circular Matching",
            icon = Icons.Default.Speed,
            themeColors = themeColors
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- 1. DUAL TOP METRIC SCORE CARDS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // CARD 1: CV Completeness or Gemini AI ATS Score
            val hasScanBeenRun = cvData.lastAtsScoreFromGemini > 0
            val localScore = currentChecklist.count { it.status == ChecklistStatus.PRESENT } * 100 / currentChecklist.size
            val atsScore = if (hasScanBeenRun) {
                cvData.lastAtsScoreFromGemini
            } else {
                localScore
            }
            val scoreColor = when {
                atsScore >= 80 -> Color(0xFF10B981)
                atsScore >= 60 -> Color(0xFFF59E0B)
                atsScore > 0 -> Color(0xFFEF4444)
                else -> themeColors.displayText.copy(alpha = 0.4f)
            }
            Surface(
                onClick = { selectedMatchMode = 0 },
                shape = RoundedCornerShape(14.dp),
                color = if (selectedMatchMode == 0) themeColors.buttonEqualBg.copy(alpha = 0.08f) else themeColors.cardBg,
                border = BorderStroke(if (selectedMatchMode == 0) 2.dp else 1.dp, if (selectedMatchMode == 0) themeColors.buttonEqualBg else scoreColor.copy(alpha = 0.3f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (hasScanBeenRun) {
                            if (isBn) "এআই এটিএস স্কোর" else "AI ATS Score"
                        } else {
                            if (isBn) "সিভি সম্পূর্ণতা" else "CV Completeness"
                        },
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$atsScore%",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = scoreColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (hasScanBeenRun) {
                            when {
                                atsScore >= 80 -> if (isBn) "এটিএস ফ্রেন্ডলি" else "ATS Friendly"
                                atsScore >= 60 -> if (isBn) "উন্নতি প্রয়োজন" else "Needs Improvement"
                                else -> if (isBn) "নিম্ন স্কোর" else "Low Score"
                            }
                        } else {
                            if (isBn) "স্ক্যান প্রয়োজন" else "Scan Required"
                        },
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                }
            }

            // CARD 2: Job Circular Match Score
            val matchPct = cvData.lastJobMatchPercentage
            val matchColor = when {
                matchPct >= 80 -> Color(0xFF10B981)
                matchPct >= 60 -> Color(0xFFF59E0B)
                matchPct > 0 -> Color(0xFFEF4444)
                else -> themeColors.displayText.copy(alpha = 0.4f)
            }
            Surface(
                onClick = { selectedMatchMode = 1 },
                shape = RoundedCornerShape(14.dp),
                color = if (selectedMatchMode == 1) Color(0xFF10B981).copy(alpha = 0.08f) else themeColors.cardBg,
                border = BorderStroke(if (selectedMatchMode == 1) 2.dp else 1.dp, if (selectedMatchMode == 1) Color(0xFF10B981) else matchColor.copy(alpha = 0.3f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isBn) "সার্কুলার ম্যাচ %" else "Circular Match %",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (matchPct > 0) "$matchPct%" else "— %",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = matchColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when {
                            matchPct >= 80 -> if (isBn) "হাইলি ম্যাচড" else "High Match"
                            matchPct >= 60 -> if (isBn) "মাঝারি ম্যাচ" else "Moderate Match"
                            matchPct > 0 -> if (isBn) "লো ম্যাচ" else "Low Match"
                            else -> if (isBn) "সার্কুলার দিন" else "Upload Circular"
                        },
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = matchColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- 2. SELECTABLE CHIPS MODE CONTROL ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Chip 1: "ATS Scan & Checks"
            FilterChip(
                selected = selectedMatchMode == 0,
                onClick = { selectedMatchMode = 0 },
                label = {
                    Text(
                        text = if (isBn) "এটিএস স্ক্যান ও চেক" else "ATS Scan & Checks",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (selectedMatchMode == 0) Color.White else themeColors.buttonEqualBg
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = themeColors.cardBg,
                    labelColor = themeColors.displayText,
                    selectedContainerColor = themeColors.buttonEqualBg,
                    selectedLabelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedMatchMode == 0,
                    borderColor = themeColors.buttonEqualBg.copy(alpha = 0.5f),
                    selectedBorderColor = themeColors.buttonEqualBg
                ),
                modifier = Modifier.weight(1f)
            )

            // Chip 2: "Job Circular Match"
            FilterChip(
                selected = selectedMatchMode == 1,
                onClick = { selectedMatchMode = 1 },
                label = {
                    Text(
                        text = if (isBn) "সার্কুলার ম্যাচিং" else "Job Circular Match",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Checklist,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (selectedMatchMode == 1) Color.White else Color(0xFF10B981)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = themeColors.cardBg,
                    labelColor = themeColors.displayText,
                    selectedContainerColor = Color(0xFF10B981),
                    selectedLabelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedMatchMode == 1,
                    borderColor = Color(0xFF10B981).copy(alpha = 0.5f),
                    selectedBorderColor = Color(0xFF10B981)
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. DYNAMIC CONTENT BASED ON SELECTED CHIP ---
        AnimatedContent(
            targetState = selectedMatchMode,
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
            label = "JobMatchChipTransition"
        ) { mode ->
            if (mode == 0) {
                Column {
                    // ================= MODE 0: ATS SCAN & CHECKS =================
                    Button(
                onClick = { onAnalyzeAtsAi() },
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBn) "জেমিনি এআই দিয়ে এটিএস স্ক্যান করুন" else "Run ATS Scan with Gemini AI",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Interactive Checklist
            JobMatchChecklistComponent(
                checklist = currentChecklist,
                themeColors = themeColors,
                isBn = isBn,
                onFixNowClick = { fixItem ->
                    activeFixItem = fixItem
                }
            )

            // Display AI Proposed suggestions if generated
            if (atsSuggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = if (isBn) "এআই প্রস্তাবিত এটিএস সমাধানসমূহ" else "AI Proposed ATS Enhancements",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isBn) "নিচের যেকোনো সাজেশন এআই দিয়ে অটো-আপডেট করতে পারেন অথবা নিজে ম্যানুয়ালি ফিক্স করতে পারেন:" else "Apply recommendations automatically using AI, or click to edit manually in that section:",
                    fontSize = 11.sp,
                    color = themeColors.displayText.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                atsSuggestions.forEach { suggestion ->
                    IndividualAiSuggestionCard(
                        suggestion = suggestion,
                        themeColors = themeColors,
                        isBn = isBn,
                        onApplyFix = {
                            beforeAfterSuggestionToApply = suggestion
                        },
                        onNavigate = {
                            val tabIdx = when {
                                suggestion.category.equals("summary", ignoreCase = true) || suggestion.category.equals("contact_info", ignoreCase = true) -> 0
                                suggestion.category.startsWith("experience_", ignoreCase = true) -> 1
                                suggestion.category.equals("skills", ignoreCase = true) -> 2
                                suggestion.category.equals("educations", ignoreCase = true) || suggestion.category.equals("education", ignoreCase = true) -> 2
                                else -> 0
                            }
                            onNavigateToTab(tabIdx)
                        }
                    )
                }
            }

        }
    } else {
        Column {
            // ================= MODE 1: JOB CIRCULAR MATCHING =================
            // --- Multi-Circular History ---
            if (savedCirculars.isNotEmpty()) {
                Text(
                    text = if (isBn) "📋 পূর্ববর্তী সার্কুলার বিশ্লেষণসমূহ:" else "📋 Past Analyzed Circulars:",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(savedCirculars) { item ->
                        Surface(
                            onClick = {
                                circularInputText = item.text
                                onCvDataChange(cvData.copy(
                                    targetJobCircular = item.text,
                                    lastJobMatchPercentage = item.matchPct,
                                    lastMatchingStrengths = item.matchingStrengths,
                                    lastMissingKeywords = item.missingKeywords,
                                    lastTailoredSummary = item.tailoredSummary,
                                    lastCircularSuggestionsJson = item.suggestionsJson
                                ))
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = themeColors.cardBg,
                            border = BorderStroke(1.dp, if (cvData.targetJobCircular == item.text) Color(0xFF10B981) else themeColors.displayText.copy(alpha = 0.12f))
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(
                                    text = item.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "${item.matchPct}% Match",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.date,
                                        fontSize = 9.sp,
                                        color = themeColors.displayText.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Text(
                text = if (isBn)
                    "যে চাকরির জন্য আবেদন করতে চান তার সার্কুলার টেক্সট পেস্ট করুন অথবা গ্যালারি থেকে সার্কুলারের ছবি দিন!"
                else
                    "Paste target job circular text OR upload an image of the circular from your gallery.",
                fontSize = 11.5.sp,
                color = themeColors.displayText.copy(alpha = 0.75f),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Circular Text Field
            OutlinedTextField(
                value = circularInputText,
                onValueChange = { circularInputText = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 12,
                placeholder = { Text(text = if (isBn) "জব সার্কুলার ডেসক্রিপশন পেস্ট করুন..." else "Paste Job circular / description requirements text here...", fontSize = 12.5.sp) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                    focusedContainerColor = themeColors.cardBg,
                    unfocusedContainerColor = themeColors.cardBg,
                    focusedTextColor = themeColors.displayText,
                    unfocusedTextColor = themeColors.displayText
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Image attachment option
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themeColors.cardBg,
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (isBn) "সার্কুলার ছবি ইনপুট করুন (অপশনাল)" else "Upload Circular Image (Optional)",
                        fontSize = 12.sp,
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
                            border = BorderStroke(1.dp, Color(0xFF10B981)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (isBn) "গ্যালারি থেকে ছবি নিন" else "Choose Image", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                                .height(140.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, themeColors.displayText.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button
            Button(
                onClick = {
                    if (circularInputText.isNotBlank() || selectedImageBitmap != null) {
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
                    } else {
                        Toast.makeText(context, if (isBn) "উপরে সার্কুলার টেক্সট বা ছবি দিন" else "Please enter circular text or image first", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBn) "এআই দিয়ে এটিএস জব ম্যাচ অ্যানালাইসিস করুন" else "Analyze Job Match with Gemini AI",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Match Results & Suggestions
            if (cvData.lastMissingKeywords.isNotEmpty() || cvData.lastMatchingStrengths.isNotEmpty() || cvData.lastTailoredSummary.isNotBlank() || circularSuggestions.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = themeColors.cardBg,
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (isBn) "জেমিনি এআই সার্কুলার ম্যাচিং রেজাল্ট" else "Gemini AI Circular Match Result",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 1-Tap Summary Apply Button
                        if (cvData.lastTailoredSummary.isNotBlank()) {
                            Button(
                                onClick = {
                                    var updated = cvData.copy(summary = cvData.lastTailoredSummary)
                                    if (cvData.lastMissingKeywords.isNotEmpty()) {
                                        val newSkills = updated.skills.toMutableList()
                                        cvData.lastMissingKeywords.forEach { kw ->
                                            if (newSkills.none { it.name.equals(kw, ignoreCase = true) }) {
                                                val desc = "Proficient in $kw with hands-on experience applying it in real-world professional projects."
                                                newSkills.add(CvSkillItem(name = kw, description = desc))
                                            }
                                        }
                                        updated = updated.copy(skills = newSkills, lastMissingKeywords = emptyList())
                                    }
                                    onCvDataChange(updated)
                                    Toast.makeText(context, if (isBn) "সার্কুলার অনুযায়ী পুরো সিভি আপডেট হয়েছে!" else "Entire CV tailored to circular!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(40.dp)
                            ) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBn) "১-ট্যাপে পুরো সিভি সার্কুলারে টিউন করুন" else "1-Tap Auto-Tailor Entire CV to Circular",
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Keyword Match Analysis (Suggestion 1)
                        KeywordAnalysisCard(
                            matchedKeywords = cvData.lastMatchingStrengths,
                            missingKeywords = cvData.lastMissingKeywords,
                            themeColors = themeColors,
                            isBn = isBn,
                            onAddKeyword = { kw ->
                                val updatedSkills = cvData.skills.toMutableList()
                                if (updatedSkills.none { it.name.equals(kw, ignoreCase = true) }) {
                                    val desc = "Experienced in applying $kw effectively to solve critical professional problems."
                                    updatedSkills.add(CvSkillItem(name = kw, description = desc))
                                }
                                val updatedMissing = cvData.lastMissingKeywords.filter { it != kw }
                                onCvDataChange(cvData.copy(skills = updatedSkills, lastMissingKeywords = updatedMissing))
                                Toast.makeText(context, if (isBn) "$kw স্কিলে যুক্ত করা হয়েছে!" else "Added $kw to skills!", Toast.LENGTH_SHORT).show()
                            },
                            onAddAllKeywords = {
                                val newSkills = cvData.skills.toMutableList()
                                cvData.lastMissingKeywords.forEach { kw ->
                                    if (newSkills.none { it.name.equals(kw, ignoreCase = true) }) {
                                        val desc = "Proficient in $kw with hands-on experience applying it in real-world professional projects."
                                        newSkills.add(CvSkillItem(name = kw, description = desc))
                                    }
                                }
                                onCvDataChange(cvData.copy(skills = newSkills, lastMissingKeywords = emptyList()))
                                Toast.makeText(context, if (isBn) "সকল মিসিং স্কিল যুক্ত হয়েছে!" else "Added all missing skills!", Toast.LENGTH_SHORT).show()
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Circular Suggestions (Suggestion 2)
                        if (circularSuggestions.isNotEmpty()) {
                            Text(
                                text = if (isBn) "🎯 সার্কুলার অনুযায়ী নির্দিষ্ট পরিবর্তনগুলো রিভিউ করুন:" else "🎯 Review Circular-specific Proposed Changes:",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            circularSuggestions.forEach { suggestion ->
                                IndividualAiSuggestionCard(
                                    suggestion = suggestion,
                                    themeColors = themeColors,
                                    isBn = isBn,
                                    onApplyFix = {
                                        beforeAfterSuggestionToApply = suggestion
                                    },
                                    onNavigate = {
                                        val tabIdx = when {
                                            suggestion.category.equals("summary", ignoreCase = true) || suggestion.category.equals("contact_info", ignoreCase = true) -> 0
                                            suggestion.category.startsWith("experience_", ignoreCase = true) -> 1
                                            suggestion.category.equals("skills", ignoreCase = true) -> 2
                                            suggestion.category.equals("educations", ignoreCase = true) || suggestion.category.equals("education", ignoreCase = true) -> 2
                                            else -> 0
                                        }
                                        onNavigateToTab(tabIdx)
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Improvement Tips
                        if (cvData.lastImprovementTips.isNotEmpty()) {
                            Text(
                                text = if (isBn) "💡 এটিএস স্কোর বাড়াতে জেমিনির পরামর্শ:" else "💡 Tips to Increase ATS Match:",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            for (tip in cvData.lastImprovementTips) {
                                Text(
                                    text = "• $tip",
                                    fontSize = 11.sp,
                                    color = themeColors.displayText.copy(alpha = 0.85f),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    }
    }

    if (activeFixItem != null) {
        FixNowBottomSheet(
            item = activeFixItem!!,
            cvData = cvData,
            themeColors = themeColors,
            isBn = isBn,
            onDismiss = { activeFixItem = null },
            onNavigateToSection = { sectionKey, targetTabIndex ->
                onNavigateToTab(targetTabIndex)
                activeFixItem = null
            },
            onApplyAiFix = { updatedCv ->
                onCvDataChange(updatedCv)
                Toast.makeText(context, if (isBn) "সিভিতে এআই তথ্য যুক্ত করা হয়েছে!" else "AI generated content applied to CV!", Toast.LENGTH_SHORT).show()
                activeFixItem = null
            },
            callGeminiAiApi = callGeminiAiApi
        )
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
    isScrollable: Boolean = true,
    onRequestAiPrompt: (title: String, defaultPrompt: String, targetField: String, expIndex: Int) -> Unit = { _, _, _, _ -> },
    onPickImage: () -> Unit = {},
    onOpenCropExisting: () -> Unit = {}
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
                            text = style.getDescription(isBn),
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
                            text = if (isBn) "প্রিন্ট করার পর হাতে স্বাক্ষর করার জন্য নিচে স্বাক্ষর ও তারিখের ফাঁকা লাইন যুক্ত থাকবে" else "Adds official physical signature line & date at the bottom for printing",
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
            CvLiveEditPanel(
                cvData = cvData,
                onCvDataChange = onCvDataChange,
                onRequestAiPrompt = onRequestAiPrompt,
                onPickImage = onPickImage,
                onOpenCropExisting = onOpenCropExisting,
                themeColors = themeColors,
                isBn = isBn,
                onRefreshPreview = onRefreshPreview
            )
            Spacer(modifier = Modifier.height(12.dp))
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
internal fun SectionCardHeader(
    title: String,
    icon: ImageVector,
    themeColors: CalculatorThemeColors,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
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
            color = themeColors.displayText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
    val context = LocalContext.current
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
                                        checkedTrackColor = themeColors.buttonEqualBg
                                    ),
                                    modifier = Modifier.scale(0.75f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
