package com.example.ui.components

import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.ConverterType
import com.example.data.model.ToolType
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage

sealed class SearchResult(
    val title: String,
    val subTitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
) {
    class Converter(val type: ConverterType, language: AppLanguage, val onAction: () -> Unit) : SearchResult(
        title = if (language == AppLanguage.BENGALI) type.titleBn else type.titleEn,
        subTitle = if (language == AppLanguage.BENGALI) "কনভার্টার • ${type.units.take(6).joinToString(", ")}" else "Converter • ${type.units.take(6).joinToString(", ")}",
        icon = type.icon,
        onClick = onAction
    )
    class Tool(val type: ToolType, language: AppLanguage, val onAction: () -> Unit) : SearchResult(
        title = if (language == AppLanguage.BENGALI) type.titleBn else type.titleEn,
        subTitle = type.getDescription(language),
        icon = type.icon,
        onClick = onAction
    )
    class History(val expression: String, val result: String, val onAction: () -> Unit) : SearchResult(
        title = expression,
        subTitle = result,
        icon = Icons.Default.History,
        onClick = onAction
    )
    class QuranSurah(val surahNum: Int, val nameEn: String, val nameBn: String, val onAction: () -> Unit) : SearchResult(
        title = nameBn,
        subTitle = "কুরআন সূরা • $nameEn • Surah $surahNum",
        icon = Icons.Default.AutoStories,
        onClick = onAction
    )
    class Hadith(val reference: String, val narrator: String, val text: String, val onAction: () -> Unit) : SearchResult(
        title = reference,
        subTitle = if (narrator.isNotEmpty()) "$narrator: $text" else text,
        icon = Icons.Default.LibraryBooks,
        onClick = onAction
    )
    class Finance(val itemTitle: String, val itemSubtitle: String, val iconType: ImageVector, val onAction: () -> Unit) : SearchResult(
        title = itemTitle,
        subTitle = itemSubtitle,
        icon = iconType,
        onClick = onAction
    )
}

fun ConverterType.matchesConverterQuery(query: String): Boolean {
    val q = query.lowercase().trim()
    if (q.isEmpty()) return false
    if (titleEn.lowercase().contains(q) || titleBn.lowercase().contains(q)) return true
    
    if (units.any { it.lowercase().contains(q) || q.contains(it.lowercase()) }) return true

    val extraKeywords = when (this) {
        ConverterType.LENGTH -> listOf("মিটার", "কিলোমিটার", "কিমি", "ফুট", "ইঞ্চি", "সেন্টিমিটার", "সেমি", "গজ", "মাইল", "meter", "kilometre", "kilometer", "feet", "foot", "inch", "cm", "yard", "mile")
        ConverterType.WEIGHT -> listOf("ওজন", "কেজি", "কিলোগ্রাম", "গ্রাম", "পাউন্ড", "আউন্স", "টন", "মিলিগ্রাম", "kg", "gram", "pound", "ounce", "ton", "mg")
        ConverterType.AREA -> listOf("ক্ষেত্রফল", "বিঘা", "কাঠা", "শতক", "শতাংশ", "একর", "একোর", "হেক্টর", "স্কয়ার ফিট", "বর্গফুট", "বর্গমিটার", "bigha", "katha", "shotok", "acre", "sqft")
        ConverterType.TEMPERATURE -> listOf("তাপমাত্রা", "সেলসিয়াস", "ফারেনহাইট", "কেলভিন", "ডিগ্রি", "ডিগ্রী", "celsius", "fahrenheit", "kelvin", "degree")
        ConverterType.VOLUME -> listOf("আয়তন", "আয়তন", "লিটার", "মিলিলিটার", "গ্যালন", "ঘনমিটার", "liter", "litre", "ml", "gallon", "cubic")
        ConverterType.PRESSURE -> listOf("চাপ", "বার", "প্যাসকেল", "বায়ুমণ্ডল", "bar", "pascal", "psi", "atmosphere")
        ConverterType.POWER -> listOf("ক্ষমতা", "ওয়াট", "ওয়াট", "কিলোওয়াট", "কিলোওয়াট", "অশ্বক্ষমতা", "এইচপি", "watt", "kilowatt", "hp", "horsepower")
        ConverterType.ENERGY -> listOf("শক্তি", "জুল", "কিলোজুল", "ক্যালরি", "ক্যালোরি", "joule", "calorie", "kcal")
        ConverterType.FORCE -> listOf("বল", "নিউটন", "ডাইন", "newton", "dyne")
        ConverterType.TORQUE -> listOf("টর্ক", "নিউটন-মিটার", "torque", "nm")
        ConverterType.DENSITY -> listOf("ঘনত্ব", "kg/m3", "density")
        ConverterType.ANGLE -> listOf("কোণ", "ডিগ্রি", "ডিগ্রী", "রেডিয়ান", "angle", "degree", "radian")
        ConverterType.DIGITAL_STORAGE -> listOf("ডেটা", "বাইট", "কেবি", "এমবি", "জিবি", "টেরাবাইট", "byte", "kb", "mb", "gb", "tb")
        ConverterType.DATA_TRANSFER -> listOf("স্পিড", "স্পীড", "এমবিপিএস", "mbps", "gbps", "mb/s")
        ConverterType.FREQUENCY -> listOf("ফ্রিকোয়েন্সি", "হার্টজ", "হার্জ", "hertz", "hz", "khz", "mhz", "ghz")
        ConverterType.NUMBER_SYSTEM -> listOf("সংখ্যা", "ডেসিমেল", "বাইনারি", "অক্টাল", "হেক্সাডেসিমেল", "decimal", "binary", "octal", "hex")
        ConverterType.SPEED -> listOf("গতি", "গতিবেগ", "কিমি/ঘন্টা", "মাইল/ঘন্টা", "নট", "speed", "kmh", "mph", "knot")
        ConverterType.TIME -> listOf("সময়", "সময়", "সেকেন্ড", "মিনিট", "ঘন্টা", "ঘণ্টা", "দিন", "সপ্তাহ", "মাস", "বছর", "বৎসর", "second", "minute", "hour", "day", "week", "month", "year")
        ConverterType.FUEL_CONSUMPTION -> listOf("জ্বালানি", "মাইলেজ", "km/l", "mpg", "mileage")
        ConverterType.ACCELERATION -> listOf("ত্বরণ", "acceleration", "g-force")
        ConverterType.ELECTRIC_CURRENT -> listOf("বিদ্যুৎ", "কারেন্ট", "অ্যাম্পিয়ার", "এম্পিয়ার", "ampere", "amp")
        ConverterType.VOLTAGE -> listOf("ভোল্টেজ", "ভোল্ট", "volt", "voltage")
        ConverterType.RESISTANCE -> listOf("রোধ", "ওহম", "ওম", "ohm", "kiloohm")
        ConverterType.ELECTRIC_CHARGE -> listOf("চার্জ", "কুলম্ব", "এমএএইচ", "mah", "coulomb")
        ConverterType.COOKING -> listOf("রান্না", "চা চামচ", "টেবিল চামচ", "কাপ", "teaspoon", "tablespoon", "cup")
        ConverterType.TYPOGRAPHY -> listOf("পিক্সেল", "পয়েন্ট", "pixel", "px", "pt", "rem", "em")
        ConverterType.CURRENCY -> listOf("মুদ্রা", "টাকা", "ডলার", "ইউরো", "পাউন্ড", "রুপি", "রিয়াল", "দিরহাম", "রিঙ্গিত", "ইয়েন", "ইউয়ান", "লিরা", "রুবল", "দিনার", "taka", "dollar", "euro", "pound", "rupee", "riyal", "dirham", "yen", "yuan", "bdt", "usd", "inr", "eur")
        ConverterType.ROMAN_NUMERALS -> listOf("রোমান", "সংখ্যা", "roman", "integer")
        ConverterType.TIME_ZONE -> listOf("টাইম জোন", "সময়", "উটিসি", "বিএসটি", "utc", "bst", "ist", "gmt", "est", "pst")
    }

    return extraKeywords.any { it.contains(q) || q.contains(it) }
}

fun ToolType.matchesToolQuery(query: String, language: AppLanguage): Boolean {
    val q = query.lowercase().trim()
    if (q.isEmpty()) return false
    if (titleEn.lowercase().contains(q) || titleBn.lowercase().contains(q)) return true
    if (getDescription(language).lowercase().contains(q) || descriptionBn.lowercase().contains(q)) return true

    val extraKeywords = when (this) {
        ToolType.AGE -> listOf("বয়স", "বয়স", "জন্মদিন", "মাস", "দিন", "age", "birthday")
        ToolType.BMI -> listOf("বিএমআই", "ওজন", "উচ্চতা", "bmi", "weight", "height")
        ToolType.BMR -> listOf("ক্যালরি", "ক্যালোরি", "bmr", "calorie")
        ToolType.IDEAL_WEIGHT -> listOf("আদর্শ ওজন", "ওজন", "ideal weight")
        ToolType.WATER_INTAKE -> listOf("পানি", "জল", "water")
        ToolType.PREGNANCY_DUE -> listOf("গর্ভধারণ", "ডিউ ডেট", "pregnancy")
        ToolType.BLOOD_DONATION -> listOf("রক্ত", "রক্তদান", "blood", "donor")
        ToolType.EMI_LOAN -> listOf("ঋণ", "লোন", "ইএমআই", "সুদ", "kist", "emi", "loan")
        ToolType.DISCOUNT -> listOf("ছাড়", "ছাড়", "ডিসকাউন্ট", "অফার", "discount")
        ToolType.PROFIT_LOSS -> listOf("লাভ", "ক্ষতি", "profit", "loss")
        ToolType.VAT_TAX -> listOf("ভ্যাট", "ট্যাক্স", "কর", "vat", "tax", "gst")
        ToolType.INTEREST -> listOf("সুদ", "মুনাফা", "interest")
        ToolType.ZAKAT -> listOf("যাকাত", "যাকাতুল", "নিসাব", "zakat")
        ToolType.SAVINGS_TARGET -> listOf("সঞ্চয়", "সেভিংস", "savings")
        ToolType.DATE_DIFF -> listOf("তারিখ", "ব্যবধান", "date")
        ToolType.PERCENTAGE -> listOf("শতকরা", "পার্সেন্ট", "পার্সেন্টেজ", "percentage", "percent")
        ToolType.TIP -> listOf("টিপ", "বিল", "tip")
        ToolType.TEXT_COUNTER -> listOf("শব্দ", "অক্ষর", "word", "character")
        ToolType.CLOTH_MEASUREMENT -> listOf("কাপড়", "কাপড়", "গজ", "গিরা", "হাত", "cloth", "gaj", "gira", "haat", "measurement")
        ToolType.GOLD_CALCULATOR -> listOf("স্বর্ণ", "রুপা", "সোনা", "ভরি", "আনা", "রতি", "ক্যারেট", "gold", "silver", "vori", "carat")
        ToolType.PASSWORD_GENERATOR -> listOf("পাসওয়ার্ড", "পাসওয়ার্ড", "পিন", "password", "pin")
        ToolType.ELECTRICITY_BILL -> listOf("বিদ্যুৎ", "কারেন্ট", "বিল", "ইউনিট", "bill", "electricity")
        ToolType.APPLIANCE_COST -> listOf("সরঞ্জাম", "খরচ", "appliance")
        ToolType.BATTERY_BACKUP -> listOf("ব্যাটারি", "আইপিএস", "battery", "ips")
        ToolType.RESISTOR_CODE -> listOf("রেজিস্টর", "ওহম", "কালার কোড", "resistor", "color code")
        ToolType.FUEL_COST -> listOf("তেল", "জ্বালানি", "অকটেন", "পেট্রোল", "ডিজেল", "গ্যাস", "fuel", "petrol", "octane", "diesel")
        ToolType.SPEED_DISTANCE_TIME -> listOf("গতি", "দূরত্ব", "সময়", "স্পিড", "speed", "distance")
        ToolType.GPA, ToolType.CGPA -> listOf("জিপিএ", "সিজিপিএ", "গ্রেড", "পয়েন্ট", "পয়েন্ট", "gpa", "cgpa", "result")
        ToolType.TUITION_FEES -> listOf("টিউশন", "ফি", "tuition", "fee")
        ToolType.ATS_CV_BUILDER -> listOf("সিভি", "রিজিউমি", "বায়োডাটা", "বায়ো-ডাটা", "চাকরি", "জব", "cv", "resume", "ats", "builder", "bio data", "job")
        ToolType.VOCABULARY_MASTER -> listOf("ভোকাবুলারি", "শব্দ", "শব্দার্থ", "ইংরেজি", "ডিকশনারি", "ফ্ল্যাশ কার্ড", "vocabulary", "word", "meaning", "dictionary", "flashcard", "quiz", "ielts", "bcs", "english")
        ToolType.POCKET_MAGNIFIER -> listOf("ম্যাগনিফায়ার", "ম্যাগনিফাইং", "লেন্স", "জুম", "গ্লাস", "পড়া", "কাচ", "magnifier", "magnifying", "zoom", "lens", "reading", "torch")
        ToolType.EMERGENCY_HELPLINE -> listOf("জরুরি", "হেল্পলাইন", "৯৯৯", "পুলিশ", "অ্যাম্বুলেন্স", "ফায়ার", "হাসপাতাল", "ডাক্তার", "emergency", "helpline", "999", "16263", "sos", "ambulance", "police", "fire")
        ToolType.SOUND_METER -> listOf("সাউন্ড", "শব্দ", "নয়েজ", "ডেসিবেল", "মিটার", "মাইক্রোফোন", "sound", "noise", "decibel", "db", "meter", "mic")
        ToolType.KIDS_LEARNING -> listOf("কিডস", "বাচ্চা", "বর্ণমালা", "বানান", "নামতা", "ছড়া", "গল্প", "স্লেট", "ড্রয়িং", "পশুপাখি", "kids", "learning", "child", "children", "alphabet", "spelling", "phonics", "rhymes", "drawing")
        ToolType.COLOR_CONVERTER -> listOf("কালার", "রং", "রঙ", "হেক্স", "আরজিবি", "color", "hex", "rgb", "hsl")
        else -> emptyList()
    }

    return extraKeywords.any { it.contains(q) || q.contains(it) }
}

@Composable
fun GlobalSearchDialog(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    if (!viewModel.showGlobalSearch) return

    val quranViewModel: com.example.ui.quran.QuranViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val dialogWidth = (screenWidth * 0.92f).coerceAtMost(500.dp)
    val dialogHeight = (configuration.screenHeightDp.dp * 0.85f)

    Dialog(
        onDismissRequest = { viewModel.showGlobalSearch = false },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        androidx.activity.compose.BackHandler {
            viewModel.showGlobalSearch = false
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) { viewModel.showGlobalSearch = false },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .width(dialogWidth)
                    .height(dialogHeight)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(28.dp),
                color = themeColors.cardBg,
                tonalElevation = 6.dp,
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.1f))
            ) {
                val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
                val language = viewModel.selectedLanguage
                var searchQuery by remember { mutableStateOf("") }
                val historyItems by viewModel.historyList.collectAsState()
                val financeItems by viewModel.financeTransactions.collectAsState()

                val context = LocalContext.current
                val globalSpeechLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == android.app.Activity.RESULT_OK) {
                        val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
                        if (!spokenText.isNullOrBlank()) {
                            searchQuery = spokenText
                        }
                    }
                }
                fun startGlobalVoiceSearch() {
                    try {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_PROMPT, if (language == AppLanguage.BENGALI) "কথা বলুন..." else "Speak now...")
                        }
                        globalSpeechLauncher.launch(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Voice search unavailable", Toast.LENGTH_SHORT).show()
                    }
                }

                val searchResults by produceState<List<SearchResult>>(initialValue = emptyList(), key1 = searchQuery, key2 = historyItems, key3 = financeItems) {
                    if (searchQuery.isBlank()) {
                        value = emptyList()
                    } else {
                        kotlinx.coroutines.delay(200)
                        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                            val query = searchQuery.trim()
                            val results = mutableListOf<SearchResult>()

                            val eng = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
                            val ben = listOf("০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯")
                            var normalizedQuery = query
                            for (i in 0..9) {
                                normalizedQuery = normalizedQuery.replace(ben[i], eng[i])
                            }

                        // Search Converters
                        com.example.data.model.ConverterType.values().forEach { type ->
                            if (type.matchesConverterQuery(query)) {
                                results.add(SearchResult.Converter(type, language) {
                                    viewModel.selectedConverterType = type
                                    viewModel.activeTab = 1
                                    viewModel.showGlobalSearch = false
                                })
                            }
                        }

                        // Search Tools
                        com.example.data.model.ToolType.values().forEach { type ->
                            if (type.matchesToolQuery(query, language)) {
                                results.add(SearchResult.Tool(type, language) {
                                    viewModel.selectedToolCategoryFilter = null
                                    viewModel.selectedToolType = type
                                    viewModel.activeTab = 0
                                    viewModel.showGlobalSearch = false
                                })
                            }
                        }

                        // Search History
                        historyItems.forEach { item ->
                            var nExpr = item.expression
                            var nRes = item.result
                            for (i in 0..9) {
                                nExpr = nExpr.replace(ben[i], eng[i])
                                nRes = nRes.replace(ben[i], eng[i])
                            }
                            
                            if (nExpr.contains(normalizedQuery, ignoreCase = true) || nRes.contains(normalizedQuery, ignoreCase = true)) {
                                results.add(SearchResult.History(item.expression, item.result) {
                                    viewModel.selectHistoryItem(item)
                                    viewModel.activeTab = 2
                                    viewModel.showGlobalSearch = false
                                })
                            }
                        }

                        // Search Quran Surahs
                        try {
                            com.example.data.quran.QuranMetadata.defaultSurahList.forEach { surah ->
                                val nameEnMatch = surah.nameEnglish.contains(query, ignoreCase = true) || surah.nameTranslation.contains(query, ignoreCase = true)
                                val nameBnMatch = surah.nameBangla.contains(query, ignoreCase = true)
                                val surahNumMatch = surah.number.toString() == normalizedQuery
                                
                                if (nameEnMatch || nameBnMatch || surahNumMatch) {
                                    results.add(SearchResult.QuranSurah(surah.number, surah.nameEnglish, surah.nameBangla) {
                                        viewModel.selectedToolType = com.example.data.model.ToolType.HOLY_QURAN
                                        viewModel.activeTab = 0
                                        quranViewModel.selectSurahByNumber(surah.number)
                                        viewModel.showGlobalSearch = false
                                    })
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        // Search Hadiths
                        try {
                            val matchedHadiths = com.example.data.islamic.AuthenticHadithDatabase.searchHadiths(query)
                            matchedHadiths.take(5).forEach { hadith ->
                                val bookName = when (hadith.bookId) {
                                    "bukhari" -> "সহীহ বুখারী"
                                    "muslim" -> "সহীহ মুসলিম"
                                    "nawawi40" -> "ইমাম নববীর ৪০ হাদিস"
                                    else -> "হাদিস"
                                }
                                results.add(SearchResult.Hadith(
                                    reference = "$bookName - হাদিস নং ${hadith.hadithNumberBn}",
                                    narrator = hadith.narratorBn,
                                    text = hadith.banglaText
                                ) {
                                    viewModel.selectedToolType = com.example.data.model.ToolType.HADITH_LIBRARY
                                    viewModel.activeTab = 0
                                    viewModel.globalHadithSearchQuery = query
                                    viewModel.showGlobalSearch = false
                                })
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        // Search Finance Transactions
                        try {
                            financeItems.forEach { trans ->
                                val titleMatch = trans.title.contains(query, ignoreCase = true)
                                val catMatch = trans.category.contains(query, ignoreCase = true)
                                val noteMatch = trans.note.contains(query, ignoreCase = true)
                                val amountMatch = trans.amount.toString().contains(normalizedQuery) || trans.amount.toLong().toString().contains(normalizedQuery)
                                val typeBn = when (trans.type) {
                                    "INCOME" -> "আয়"
                                    "EXPENSE" -> "ব্যয়"
                                    "SAVINGS" -> "সঞ্চয়"
                                    else -> if (trans.subType == "TAKEN") "দেনা" else "পাওনা"
                                }
                                val typeMatch = typeBn.contains(query, ignoreCase = true) || trans.type.contains(query, ignoreCase = true)

                                if (titleMatch || catMatch || noteMatch || amountMatch || typeMatch) {
                                    val icon = when (trans.type) {
                                        "INCOME" -> Icons.AutoMirrored.Filled.TrendingUp
                                        "EXPENSE" -> Icons.AutoMirrored.Filled.TrendingDown
                                        "SAVINGS" -> Icons.Default.AccountBalance
                                        else -> if (trans.subType == "TAKEN") Icons.Default.CallReceived else Icons.Default.CallMade
                                    }
                                    val formattedAmt = if (language == AppLanguage.BENGALI) "৳ ${trans.amount}" else "৳ ${trans.amount}"
                                    val sub = "$typeBn • ${trans.category.ifEmpty { if (language == AppLanguage.BENGALI) "লেনদেন" else "Transaction" }} • $formattedAmt"
                                    results.add(SearchResult.Finance(
                                        itemTitle = trans.title,
                                        itemSubtitle = sub,
                                        iconType = icon
                                    ) {
                                        viewModel.selectedFinanceTransactionForDetail = trans
                                        viewModel.activeTab = 3
                                        viewModel.showGlobalSearch = false
                                    })
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        // Search Finance Contacts / Persons
                        try {
                            val financePrefs = context.getSharedPreferences("finance_prefs", android.content.Context.MODE_PRIVATE)
                            val contactsJson = financePrefs.getString("contact_persons", null)
                            if (!contactsJson.isNullOrBlank()) {
                                val jsonArray = org.json.JSONArray(contactsJson)
                                for (i in 0 until jsonArray.length()) {
                                    val obj = jsonArray.getJSONObject(i)
                                    val name = obj.optString("name", "")
                                    val phone = obj.optString("phone", "")
                                    val note = obj.optString("note", "")
                                    if (name.contains(query, ignoreCase = true) || phone.contains(query, ignoreCase = true) || note.contains(query, ignoreCase = true)) {
                                        results.add(SearchResult.Finance(
                                            itemTitle = name,
                                            itemSubtitle = if (language == AppLanguage.BENGALI) "ব্যক্তি ও খতিয়ান • ${if (phone.isNotEmpty()) phone else "লেনদেন তালিকা"}" else "Person Ledger • ${if (phone.isNotEmpty()) phone else "History"}",
                                            iconType = Icons.Default.Person
                                        ) {
                                            viewModel.selectedFinancePersonForDetail = name
                                            viewModel.activeTab = 3
                                            viewModel.showGlobalSearch = false
                                        })
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        results
                    }
                }
            }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Header with Title and Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (language == AppLanguage.BENGALI) "স্মার্ট অনুসন্ধান" else "Smart Search",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = themeColors.displayText,
                                fontFamily = FontFamily.SansSerif
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (language == AppLanguage.BENGALI) "সব তথ্য এক জায়গায় খুঁজুন" else "Find everything in one place",
                                fontSize = 14.sp,
                                color = themeColors.displayText.copy(alpha = 0.6f),
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(themeColors.background)
                                .clickable { viewModel.showGlobalSearch = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = themeColors.displayText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Search Input Field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(themeColors.background, RoundedCornerShape(16.dp))
                            .border(1.5.dp, themeColors.buttonEqualBg.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = themeColors.buttonEqualBg,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    color = themeColors.displayText,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(themeColors.buttonEqualBg),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = if (language == AppLanguage.BENGALI) "সার্চ করুন..." else "Search here...",
                                            fontSize = 16.sp,
                                            color = themeColors.displayText.copy(alpha = 0.4f),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    innerTextField()
                                }
                            )

                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = themeColors.displayText.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { startGlobalVoiceSearch() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Search",
                                    tint = themeColors.buttonEqualBg,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Content Area (Results or Empty State)
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isBlank()) {
                            // Initial Empty State
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(100.dp).graphicsLayer(alpha = 0.15f),
                                    tint = themeColors.displayText
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = if (language == AppLanguage.BENGALI) "খুঁজতে টাইপ করুন" else "Type to search",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (language == AppLanguage.BENGALI) 
                                        "যেমন: হিস্ট্রি, বিএমআই, বা তাপমাত্রা" 
                                        else "Example: History, BMI, or Temperature",
                                    fontSize = 13.sp,
                                    color = themeColors.displayText.copy(alpha = 0.4f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else if (searchResults.isEmpty()) {
                            // No Results State
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = themeColors.displayText.copy(alpha = 0.2f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (language == AppLanguage.BENGALI) "কোনো ফলাফল পাওয়া যায়নি" else "No results found",
                                    color = themeColors.displayText.copy(alpha = 0.5f),
                                    fontSize = 18.sp
                                )
                            }
                        } else {
                            // Results List
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(searchResults) { result ->
                                    SearchResultItem(result, searchQuery, themeColors)
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
fun SearchResultItem(
    result: SearchResult,
    query: String,
    themeColors: CalculatorThemeColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { result.onClick() },
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(themeColors.buttonEqualBg.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(result.icon, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(24.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = buildAnnotatedString {
                        appendWithHighlight(result.title, query, themeColors.buttonEqualBg.copy(alpha = 0.3f))
                    },
                    color = themeColors.displayText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = buildAnnotatedString {
                        appendWithHighlight(result.subTitle, query, themeColors.buttonEqualBg.copy(alpha = 0.3f))
                    },
                    color = themeColors.displayText.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

fun AnnotatedString.Builder.appendWithHighlight(text: String, query: String, highlightColor: Color) {
    if (query.isEmpty()) {
        append(text)
        return
    }

    val eng = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
    val ben = listOf("০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯")
    var normalizedText = text.lowercase()
    var normalizedQuery = query.lowercase()
    for (i in 0..9) {
        normalizedText = normalizedText.replace(ben[i], eng[i])
        normalizedQuery = normalizedQuery.replace(ben[i], eng[i])
    }
    
    var lastIndex = 0
    var index = normalizedText.indexOf(normalizedQuery, lastIndex)
    
    while (index != -1) {
        append(text.substring(lastIndex, index))
        withStyle(style = SpanStyle(background = highlightColor)) {
            append(text.substring(index, index + normalizedQuery.length))
        }
        lastIndex = index + normalizedQuery.length
        index = normalizedText.indexOf(normalizedQuery, lastIndex)
    }
    append(text.substring(lastIndex))
}
