package com.example.ui.screens.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel

private val CalculatorThemeColors.accent: Color get() = this.buttonEqualBg
private val CalculatorThemeColors.onAccent: Color get() = this.buttonEqualText
private val CalculatorThemeColors.onSurface: Color get() = this.displayText
private val CalculatorThemeColors.surface: Color get() = this.cardBg
private val CalculatorThemeColors.surfaceVariant: Color get() = this.chipBg

data class HelplineItem(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val number: String,
    val subtitle: String,
    val category: String, // "National", "Health", "Police", "Fire", "WomenChild", "Utility", "Bank", "Blood"
    val isTollFree: Boolean = false,
    val isAvailable24h: Boolean = true
)

data class PersonalContact(
    val name: String,
    val relation: String,
    val phone: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyHelplineTool(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isBn = viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI

    fun dialNumber(number: String) {
        val cleanNumber = number.replace(Regex("[^0-9+]"), "")
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNumber"))
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, if (isBn) "কল করা সম্ভব হচ্ছে না" else "Cannot initiate call", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, if (isBn) "$text কপি করা হয়েছে" else "Copied $text", Toast.LENGTH_SHORT).show()
    }

    fun sendSosSms(phone: String = "") {
        var locationUrl = ""
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val location: Location? = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            if (location != null) {
                locationUrl = "\nআমার লোকেশন: https://maps.google.com/?q=${location.latitude},${location.longitude}"
            }
        } catch (e: SecurityException) {
            // Permission not granted, continue without gps coordinates
        }

        val msg = if (isBn) {
            "জরুরি বিপদ বার্তা! আমি এই মুহূর্তে বিপদে আছি, দ্রুত সাহায্য প্রয়োজন।$locationUrl"
        } else {
            "Emergency SOS! I am in danger and need immediate assistance.$locationUrl"
        }

        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phone")
            putExtra("sms_body", msg)
        }
        try {
            context.startActivity(smsIntent)
        } catch (e: Exception) {
            Toast.makeText(context, if (isBn) "মেসেজ অ্যাপ পাওয়া যায়নি" else "SMS app not available", Toast.LENGTH_SHORT).show()
        }
    }

    // Personal Contacts Preferences
    val prefs = remember { context.getSharedPreferences("emergency_prefs", Context.MODE_PRIVATE) }
    val personalContacts = remember {
        mutableStateListOf<PersonalContact>().apply {
            val savedStr = prefs.getString("personal_contacts", "") ?: ""
            if (savedStr.isNotEmpty()) {
                savedStr.split(";;").forEach { item ->
                    val parts = item.split("::")
                    if (parts.size >= 3) {
                        add(PersonalContact(parts[0], parts[1], parts[2]))
                    }
                }
            }
        }
    }

    fun savePersonalContacts() {
        val encoded = personalContacts.joinToString(";;") { "${it.name}::${it.relation}::${it.phone}" }
        prefs.edit().putString("personal_contacts", encoded).apply()
    }

    var showAddContactDialog by remember { mutableStateOf(false) }
    var newContactName by remember { mutableStateOf("") }
    var newContactRelation by remember { mutableStateOf("") }
    var newContactPhone by remember { mutableStateOf("") }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val allHelplines = remember { EmergencyDataProvider.getAllHelplines() }

    val filteredHelplines = remember(allHelplines, searchQuery, selectedCategory) {
        allHelplines.filter { item ->
            val matchesQuery = searchQuery.isBlank() ||
                    item.titleBn.contains(searchQuery, ignoreCase = true) ||
                    item.titleEn.contains(searchQuery, ignoreCase = true) ||
                    item.number.contains(searchQuery) ||
                    item.subtitle.contains(searchQuery, ignoreCase = true)

            val matchesCategory = selectedCategory == "All" || item.category == selectedCategory
            matchesQuery && matchesCategory
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isBn) "জরুরি হেল্পলাইন ডিরেক্টরি" else "Emergency Helpline",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = themeColors.onSurface
                        )
                        Text(
                            text = if (isBn) "১-ট্যাপ সরাসরি কল • ২৪/৭ জাতীয় জরুরি সেবা" else "1-Tap Direct Call • 24/7 National SOS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFF44336)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = themeColors.onSurface
                        )
                    }
                },
                actions = {
                    // Quick SOS SMS Action Button
                    IconButton(onClick = { sendSosSms() }) {
                        Icon(
                            imageVector = Icons.Default.SmsFailed,
                            contentDescription = "SOS SMS",
                            tint = Color(0xFFF44336)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = themeColors.surface)
            )
        },
        containerColor = themeColors.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // SOS Banner Card (999 & 16263 Highlight)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Emergency, contentDescription = null, tint = Color.White)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isBn) "জাতীয় জরুরি হটলাইন" else "National Emergency SOS",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (isBn) "পুলিশ • অ্যাম্বুলেন্স • ফায়ার সার্ভিস" else "Police • Ambulance • Fire",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }

                            Button(
                                onClick = { dialNumber("999") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("৯৯৯", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick 4-Grid of Key SOS Hotlines
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple("১৬২৬৩", if (isBn) "স্বাস্থ্য বাতায়ন" else "Health 24/7", "16263"),
                                Triple("১০৯", if (isBn) "নারী ও শিশু" else "Women/Child", "109"),
                                Triple("৩৩৩", if (isBn) "তথ্য ও সেবা" else "Govt Info", "333"),
                                Triple("১০৯০", if (isBn) "দুর্যোগ বার্তা" else "Disaster", "1090")
                            ).forEach { (num, name, callNum) ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { dialNumber(callNum) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = num, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                        Text(text = name, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f), maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // My Personal SOS Contacts Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Contacts, contentDescription = null, tint = themeColors.accent, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBn) "আমার জরুরি পরিচিতি (SOS)" else "My Personal SOS Contacts",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = themeColors.onSurface
                                )
                            }

                            FilledTonalButton(
                                onClick = { showAddContactDialog = true },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isBn) "যুক্ত করুন" else "Add", fontSize = 12.sp)
                            }
                        }

                        if (personalContacts.isEmpty()) {
                            Text(
                                text = if (isBn) "পরিবারের সদস্য বা পরিচিত কারো নম্বর যোগ করে রাখুন যাতে বিপদের মুহূর্তে দ্রুত কল বা এসএমএস দেওয়া যায়।"
                                else "Add family members' numbers for 1-tap quick call or emergency location SMS.",
                                style = MaterialTheme.typography.bodySmall,
                                color = themeColors.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(10.dp))
                            personalContacts.forEachIndexed { index, contact ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = themeColors.surfaceVariant.copy(alpha = 0.35f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = contact.name,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = themeColors.onSurface
                                            )
                                            Text(
                                                text = "${contact.relation} • ${contact.phone}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = themeColors.onSurface.copy(alpha = 0.65f)
                                            )
                                        }

                                        IconButton(onClick = { dialNumber(contact.phone) }) {
                                            Icon(Icons.Default.Call, contentDescription = "Call", tint = Color(0xFF4CAF50))
                                        }
                                        IconButton(onClick = { sendSosSms(contact.phone) }) {
                                            Icon(Icons.Default.Sms, contentDescription = "SMS", tint = Color(0xFF2196F3))
                                        }
                                        IconButton(onClick = {
                                            personalContacts.removeAt(index)
                                            savePersonalContacts()
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFF44336).copy(alpha = 0.7f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Search Bar & Categories
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(if (isBn) "সেবার নাম, বিভাগ বা নম্বর খুঁজুন..." else "Search helpline, service or number...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = themeColors.accent) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColors.accent,
                        unfocusedBorderColor = themeColors.onSurface.copy(alpha = 0.15f),
                        focusedContainerColor = themeColors.surface,
                        unfocusedContainerColor = themeColors.surface
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                val categories = listOf(
                    "All" to (if (isBn) "সকল সেবা" else "All"),
                    "National" to (if (isBn) "জাতীয়" else "National"),
                    "Health" to (if (isBn) "স্বাস্থ্য ও অ্যাম্বুলেন্স" else "Health & Ambulance"),
                    "Police" to (if (isBn) "পুলিশ ও নিরাপত্তা" else "Police"),
                    "Fire" to (if (isBn) "ফায়ার সার্ভিস" else "Fire Rescue"),
                    "WomenChild" to (if (isBn) "নারী ও শিশু" else "Women & Child"),
                    "Utility" to (if (isBn) "বিদ্যুৎ ও গ্যাস" else "Utility"),
                    "Bank" to (if (isBn) "ব্যাংক কার্ড ব্লক" else "Bank Hotlines"),
                    "Blood" to (if (isBn) "ব্লাড ব্যাংক" else "Blood Banks")
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { (catKey, catLabel) ->
                        val isSelected = selectedCategory == catKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = catKey },
                            label = { Text(catLabel, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeColors.accent,
                                selectedLabelColor = themeColors.onAccent
                            )
                        )
                    }
                }
            }

            // Helplines List
            if (filteredHelplines.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBn) "কোনো হেল্পলাইন নম্বর পাওয়া যায়নি" else "No helpline found",
                            color = themeColors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                items(filteredHelplines, key = { it.id }) { item ->
                    HelplineCard(
                        item = item,
                        onCall = { dialNumber(item.number) },
                        onCopy = { copyToClipboard(item.number, item.titleBn) },
                        themeColors = themeColors,
                        isBn = isBn
                    )
                }
            }
        }

        // Add Contact Dialog
        if (showAddContactDialog) {
            AlertDialog(
                onDismissRequest = { showAddContactDialog = false },
                title = { Text(if (isBn) "জরুরি নম্বর যোগ করুন" else "Add Emergency Contact") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = newContactName,
                            onValueChange = { newContactName = it },
                            label = { Text(if (isBn) "নাম" else "Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newContactRelation,
                            onValueChange = { newContactRelation = it },
                            label = { Text(if (isBn) "সম্পর্ক (যেমন: পিতা, ভাই, ডাক্তার)" else "Relation") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newContactPhone,
                            onValueChange = { newContactPhone = it },
                            label = { Text(if (isBn) "ফোন নম্বর" else "Phone Number") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newContactName.isNotBlank() && newContactPhone.isNotBlank()) {
                                personalContacts.add(PersonalContact(newContactName, newContactRelation.ifBlank { "Emergency" }, newContactPhone))
                                savePersonalContacts()
                                newContactName = ""
                                newContactRelation = ""
                                newContactPhone = ""
                                showAddContactDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                    ) {
                        Text(if (isBn) "সংরক্ষণ করুন" else "Save", color = themeColors.onAccent)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddContactDialog = false }) {
                        Text(if (isBn) "বাতিল" else "Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun HelplineCard(
    item: HelplineItem,
    onCall: () -> Unit,
    onCopy: () -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Badge
            Surface(
                shape = CircleShape,
                color = when (item.category) {
                    "National", "Fire" -> Color(0xFFD32F2F).copy(alpha = 0.12f)
                    "Health", "Blood" -> Color(0xFF4CAF50).copy(alpha = 0.12f)
                    "Police" -> Color(0xFF1976D2).copy(alpha = 0.12f)
                    else -> themeColors.accent.copy(alpha = 0.12f)
                },
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (item.category) {
                            "National" -> Icons.Default.Emergency
                            "Health" -> Icons.Default.LocalHospital
                            "Police" -> Icons.Default.Security
                            "Fire" -> Icons.Default.LocalFireDepartment
                            "WomenChild" -> Icons.Default.Favorite
                            "Utility" -> Icons.Default.Bolt
                            "Bank" -> Icons.Default.CreditCard
                            "Blood" -> Icons.Default.Bloodtype
                            else -> Icons.Default.Phone
                        },
                        contentDescription = null,
                        tint = when (item.category) {
                            "National", "Fire" -> Color(0xFFD32F2F)
                            "Health", "Blood" -> Color(0xFF4CAF50)
                            "Police" -> Color(0xFF1976D2)
                            else -> themeColors.accent
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBn) item.titleBn else item.titleEn,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = themeColors.onSurface
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.onSurface.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.number,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = themeColors.accent
                    )
                    if (item.isTollFree) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF4CAF50).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isBn) "ফ্রি কল" else "Toll Free",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onCopy) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = themeColors.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }

            FilledIconButton(
                onClick = onCall,
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF4CAF50)),
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

object EmergencyDataProvider {
    fun getAllHelplines(): List<HelplineItem> {
        return listOf(
            // National SOS
            HelplineItem("sos_999", "জাতীয় জরুরি সেবা", "National Emergency Service", "999", "পুলিশ, অ্যাম্বুলেন্স ও ফায়ার সার্ভিস", "National", isTollFree = true),
            HelplineItem("sos_333", "সরকারি তথ্য ও সেবা", "Govt Information & Services", "333", "সরকারি ভাতা, নাগরিক সেবা ও খাদ্য সহায়তা", "National", isTollFree = false),
            HelplineItem("sos_109", "নারী ও শিশু নির্যাতন প্রতিরোধ", "Women & Children Abuse Prevention", "109", "মহিলা ও শিশু বিষয়ক মন্ত্রণালয়", "WomenChild", isTollFree = true),
            HelplineItem("sos_1098", "চাইল্ড হেল্পলাইন বাংলাদেশ", "Child Helpline Bangladesh", "1098", "বিপদগ্রস্ত শিশুদের তাৎক্ষণিক সুরক্ষা সহায়তা", "WomenChild", isTollFree = true),
            HelplineItem("sos_1090", "দুর্যোগের আগাম বার্তা", "Disaster Early Warning", "1090", "বন্যা, ঘূর্ণিঝড় ও আবহাওয়া পূর্বাভাস", "National", isTollFree = true),
            HelplineItem("sos_106", "দুদক অভিযোগ হটলাইন", "ACC Anti-Corruption Hotline", "106", "দুর্নীতি দমন কমিশন সরাসরি অভিযোগ", "National", isTollFree = true),

            // Health & Ambulance
            HelplineItem("hl_16263", "স্বাস্থ্য বাতায়ন", "Shastho Batayon", "16263", "২৪ ঘণ্টা ফ্রি ডাক্তার পরামর্শ ও স্বাস্থ্য তথ্য", "Health", isTollFree = true),
            HelplineItem("hl_dmch", "ঢাকা মেডিকেল কলেজ হাসপাতাল", "DMCH Emergency", "02-223381188", "জরুরি বিভাগ ও অ্যাম্বুলেন্স", "Health"),
            HelplineItem("hl_nitor", "জাতীয় অর্থোপেডিক (পঙ্গু) হাসপাতাল", "NITOR Orthopedic Hospital", "02-9112150", "হাড় ভাঙা ও ট্রমা জরুরি বিভাগ", "Health"),
            HelplineItem("hl_nicvd", "জাতীয় হৃদরোগ ইনস্টিটিউট", "NICVD Heart Hospital", "02-9122560", "হৃদরোগ ও হার্ট অ্যাটাক জরুরি বিভাগ", "Health"),
            HelplineItem("hl_icddrb", "আইসিডিডিআর,বি (মহাখালী)", "icddr,b Diarrhea Hospital", "02-9840521", "ডায়রিয়া ও কলেরা জরুরি চিকিৎসা", "Health"),
            HelplineItem("hl_redcrescent_amb", "রেড ক্রিসেন্ট অ্যাম্বুলেন্স", "Red Crescent Ambulance", "01811458524", "সারাদেশে জরুরি অ্যাম্বুলেন্স সেবা", "Health"),
            HelplineItem("hl_markazul", "আল মারকাজুল ইসলামী অ্যাম্বুলেন্স", "Al Markazul Ambulance", "02-9127867", "২৪ ঘণ্টা অ্যাম্বুলেন্স ও লাশবাহী গাড়ি", "Health"),

            // Police & Security
            HelplineItem("pol_dmp", "ডিএমপি কেন্দ্রীয় কন্ট্রোল রুম", "DMP Central Control", "01713398311", "ঢাকা মেট্রোপলিটন পুলিশ জরুরি কন্ট্রোল", "Police"),
            HelplineItem("pol_rab", "র‍্যাব সদর দপ্তর কন্ট্রোল", "RAB Headquarter Control", "01777720029", "র‍্যাপিড অ্যাকশন ব্যাটালিয়ন", "Police"),
            HelplineItem("pol_highway", "হাইওয়ে পুলিশ কন্ট্রোল", "Highway Police Control", "01320015555", "মহাসড়কে দুর্ঘটনা ও ডাকাতি প্রতিরোধ", "Police"),
            HelplineItem("pol_tourist", "ট্যুরিস্ট পুলিশ হেল্পলাইন", "Tourist Police Bangladesh", "01320163599", "পর্যটকদের নিরাপত্তা ও সহায়তা", "Police"),
            HelplineItem("pol_cyber", "সাইবার পুলিশ হেল্পডেস্ক", "Cyber Police Helpdesk", "01769691522", "সোশ্যাল মিডিয়া হ্যাক ও ব্ল্যাকমেইল প্রতিরোধ", "Police"),

            // Fire & Rescue
            HelplineItem("fire_central", "ফায়ার সার্ভিস সেন্ট্রাল কন্ট্রোল", "Fire Service Central Control", "02-223355555", "সারাদেশের যেকোনো অগ্নিকাণ্ড ও দুর্ঘটনা", "Fire"),
            HelplineItem("fire_mobile", "ফায়ার সার্ভিস জরুরি মোবাইল", "Fire Service Emergency Mobile", "01730336699", "২৪ ঘণ্টা জরুরি উদ্ধার ও নিয়ন্ত্রণ কক্ষ", "Fire"),
            HelplineItem("fire_hotline", "ফায়ার রেসকিউ হটলাইন", "Fire Rescue Short Hotline", "16163", "শর্টকোড ফায়ার হেল্পলাইন", "Fire"),

            // Women, Child & Mental Health
            HelplineItem("men_kanpeteroi", "কান পেতে রই (মানসিক স্বাস্থ্য)", "Kaan Pete Roi Emotional Helpline", "01779554391", "হতাশা, বিষণ্ণতা ও আত্মহত্যা প্রতিরোধ কাউন্সেলিং", "WomenChild"),
            HelplineItem("men_national", "জাতীয় মানসিক স্বাস্থ্য ইনস্টিটিউট", "National Mental Health Institute", "02-9118171", "মানসিক রোগ জরুরি সেবা", "WomenChild"),

            // Utilities
            HelplineItem("ut_desco", "ডেসকো বিদ্যুৎ কন্ট্রোল", "DESCO Electricity Helpline", "16120", "ঢাকা উত্তর বিদ্যুৎ বিভ্রাট অভিযোগ", "Utility"),
            HelplineItem("ut_dpdc", "ডিপিডিসি বিদ্যুৎ কন্ট্রোল", "DPDC Electricity Helpline", "16116", "ঢাকা দক্ষিণ বিদ্যুৎ বিভ্রাট অভিযোগ", "Utility"),
            HelplineItem("ut_titas", "তিতাস গ্যাস ইমার্জেন্সি", "Titas Gas Leakage Emergency", "16496", "গ্যাস লিকেজ ও জরুরি দুর্ঘটনা নিয়ন্ত্রণ", "Utility"),
            HelplineItem("ut_wasa", "ওয়াসা পানি ও পয়ঃনিষ্কাশন", "WASA Water Helpline", "16162", "পানি সংকট ও ড্রেনেজ সমস্যা", "Utility"),

            // Bank Card Hotlines
            HelplineItem("bk_brac", "ব্র্যাক ব্যাংক কার্ড ব্লক", "BRAC Bank Card Hotline", "16221", "কার্ড হারানো বা জালিয়াতি রোধ", "Bank"),
            HelplineItem("bk_dbbl", "ডাচ-বাংলা ব্যাংক (রকেট/কার্ড)", "Dutch-Bangla Bank Card Block", "16216", "কার্ড ও নেক্সাসপে হেল্পলাইন", "Bank"),
            HelplineItem("bk_city", "সিটি ব্যাংক (অ্যামেক্স/ভিসা)", "City Bank Card Helpline", "16234", "আমেরিকান এক্সপ্রেস ও ক্রেডিট কার্ড", "Bank"),
            HelplineItem("bk_ibbl", "ইসলামী ব্যাংক বাংলাদেশ", "Islami Bank Helpline", "16259", "কার্ড ও সেলফিন হটলাইন", "Bank"),
            HelplineItem("bk_bkash", "বিকাশ হেল্পলাইন", "bKash Customer Helpline", "16247", "অ্যাকাউন্ট পিন ব্লক ও প্রতারণা অভিযোগ", "Bank"),

            // Blood Banks
            HelplineItem("bl_sandhani", "সন্ধানী সেন্ট্রাল ব্লাড ব্যাংক", "Sandhani Central Blood Bank", "02-9668690", "ডিএমসিএইচ ইউনিট জরুরি রক্ত", "Blood"),
            HelplineItem("bl_quantum", "কোয়ান্টাম ব্লাড ব্যাংক", "Quantum Blood Bank Lab", "01714010869", "২৪ ঘণ্টা টেস্টেড নিরাপদ রক্ত সরবরাহ", "Blood"),
            HelplineItem("bl_redcrescent", "রেড ক্রিসেন্ট ব্লাড সেন্টার", "Red Crescent Blood Center", "02-9116563", "মোহাম্মদপুর সেন্ট্রাল ব্লাড ব্যাংক", "Blood"),
            HelplineItem("bl_badhan", "বাঁধন কেন্দ্রীয় রক্তদান", "Badhan Blood Donation Network", "01534982674", "স্বেচ্ছাসেবী রক্তদান নেটওয়ার্ক", "Blood")
        )
    }
}
