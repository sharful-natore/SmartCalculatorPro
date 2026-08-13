package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

import com.example.util.AppLanguage

enum class ToolCategory(
    val titleEn: String,
    val titleBn: String,
    val icon: ImageVector
) {
    HEALTH("Health & Fitness", "স্বাস্থ্য ও ফিটনেস", Icons.Default.Favorite),
    FINANCE("Finance & Business", "অর্থ ও ব্যবসা", Icons.Default.AccountBalance),
    UTILITY("Everyday Utility", "দৈনন্দিন ইউটিলিটি", Icons.Default.Widgets),
    ELECTRICITY("Electricity & Power", "বিদ্যুৎ ও শক্তি", Icons.Default.Bolt),
    VEHICLE("Smart & Vehicle Tools", "স্মার্ট ও যানবাহন", Icons.Default.DirectionsCar),
    EDUCATION("Education & Results", "শিক্ষা ও ফলাফল", Icons.Default.School),
    DEVELOPER("Developer Tools", "ডেভেলপার টুলস", Icons.Default.Code),
    ENGINEERING("Engineering", "ইঞ্জিনিয়ারিং", Icons.Default.SettingsInputComponent);

    fun getTitle(language: AppLanguage): String {
        return when (language) {
            AppLanguage.ENGLISH -> titleEn
            AppLanguage.BENGALI -> titleBn
            AppLanguage.HINDI -> when (this) {
                HEALTH -> "स्वास्थ्य और फिटनेस"
                FINANCE -> "वित्त और व्यवसाय"
                UTILITY -> "दैनिक उपयोगिता"
                ELECTRICITY -> "बिजली और ऊर्जा"
                VEHICLE -> "वाहन उपकरण"
                EDUCATION -> "शिक्षा और परिणाम"
                DEVELOPER -> "डेवलपर टूल्स"
                ENGINEERING -> "इंजीनियरिंग"
            }
            AppLanguage.ARABIC -> when (this) {
                HEALTH -> "الصحة واللياقة"
                FINANCE -> "المالية والأعمال"
                UTILITY -> "أدوات يومية"
                ELECTRICITY -> "الكهرباء والطاقة"
                VEHICLE -> "أدوات المركبات"
                EDUCATION -> "التعليم والنتائج"
                DEVELOPER -> "أدوات المطور"
                ENGINEERING -> "الهندسة"
            }
            AppLanguage.FRENCH -> when (this) {
                HEALTH -> "Santé et Forme"
                FINANCE -> "Finance et Affaires"
                UTILITY -> "Utilitaires du Quotidien"
                ELECTRICITY -> "Électricité et Énergie"
                VEHICLE -> "Outils Véhicule"
                EDUCATION -> "Éducation et Résultats"
                DEVELOPER -> "Outils Développeur"
                ENGINEERING -> "Ingénierie"
            }
            AppLanguage.SPANISH -> when (this) {
                HEALTH -> "Salud y Bienestar"
                FINANCE -> "Finanzas y Negocios"
                UTILITY -> "Utilidades Diarias"
                ELECTRICITY -> "Electricidad y Energía"
                VEHICLE -> "Herramientas de Vehículo"
                EDUCATION -> "Educación y Resultados"
                DEVELOPER -> "Herramientas de Desarrollador"
                ENGINEERING -> "Ingeniería"
            }
            AppLanguage.GERMAN -> when (this) {
                HEALTH -> "Gesundheit & Fitness"
                FINANCE -> "Finanzen & Geschäft"
                UTILITY -> "Alltags-Werkzeuge"
                ELECTRICITY -> "Elektrizität & Strom"
                VEHICLE -> "Fahrzeug-Tools"
                EDUCATION -> "Bildung & Ergebnisse"
                DEVELOPER -> "Entwickler-Tools"
                ENGINEERING -> "Ingenieurwesen"
            }
            AppLanguage.CHINESE -> when (this) {
                HEALTH -> "健康与健身"
                FINANCE -> "金融与商业"
                UTILITY -> "日常实用工具"
                ELECTRICITY -> "电力与能源"
                VEHICLE -> "车辆与出行"
                EDUCATION -> "教育与成果"
                DEVELOPER -> "开发者工具"
                ENGINEERING -> "工程"
            }
        }
    }
}

enum class ToolType(
    val titleEn: String,
    val titleBn: String,
    val descriptionBn: String,
    val category: ToolCategory,
    val icon: ImageVector
) {
    // 1. Health & Fitness
    BMI(
        "BMI Calculator", "বিএমআই ক্যালকুলেটর",
        "উচ্চতা ও ওজন দিয়ে স্বাস্থ্য ও BMI ক্যাটাগরি নির্ণয়",
        ToolCategory.HEALTH, Icons.Default.Accessibility
    ),
    BMR(
        "Calorie / BMR Calculator", "ক্যালোরি ও BMR হিসাব",
        "দৈনিক কত ক্যালোরি প্রয়োজন তার হিসাব",
        ToolCategory.HEALTH, Icons.Default.LocalFireDepartment
    ),
    IDEAL_WEIGHT(
        "Ideal Weight Calculator", "আদর্শ ওজন ক্যালকুলেটর",
        "উচ্চতা অনুযায়ী কাঙ্ক্ষিত আদর্শ ওজন কত হওয়া উচিত",
        ToolCategory.HEALTH, Icons.Default.FitnessCenter
    ),
    WATER_INTAKE(
        "Water Intake Tracker", "পানি পান ট্র্যাকার",
        "দৈনিক কত গ্লাস পানি পান করা প্রয়োজন",
        ToolCategory.HEALTH, Icons.Default.Opacity
    ),
    PREGNANCY_DUE(
        "Pregnancy Due Date", "প্রেগনেন্সি ডিউ ডেট",
        "সম্ভাব্য প্রসবের তারিখ এবং বর্তমান সপ্তাহ গণনা",
        ToolCategory.HEALTH, Icons.Default.ChildCare
    ),
    BLOOD_DONATION(
        "Blood Donation Tracker", "রক্তদান ট্র্যাকার",
        "সর্বশেষ রক্তদানের তারিখ দিয়ে পরবর্তী রক্তদানের সময় বের করা",
        ToolCategory.HEALTH, Icons.Default.Bloodtype
    ),

    // 2. Finance & Business
    EMI_LOAN(
        "EMI & Loan Calculator", "ইএমআই ও ঋণ ক্যালকুলেটর",
        "ব্যাংকের ঋণ বা কিস্তির হিসাব",
        ToolCategory.FINANCE, Icons.Default.Calculate
    ),
    DISCOUNT(
        "Discount & Savings", "ডিসকাউন্ট ও সাশ্রয়",
        "কেনাকাটার ডিসকাউন্ট ও সাশ্রয় কত হলো",
        ToolCategory.FINANCE, Icons.Default.Discount
    ),
    PROFIT_LOSS(
        "Profit & Loss Margin", "লাভ ও ক্ষতি মার্জিন",
        "ব্যবসার ক্রয়-বিক্রয়ের লাভ/ক্ষতির শতাংশ",
        ToolCategory.FINANCE, Icons.Default.ShowChart
    ),
    VAT_TAX(
        "VAT & Tax Calculator", "ভ্যাট ও ট্যাক্স ক্যালকুলেটর",
        "পণ্য বা সেবার উপর ট্যাক্স এবং মোট দামের হিসাব",
        ToolCategory.FINANCE, Icons.Default.Receipt
    ),
    INTEREST(
        "Simple & Compound Interest", "সুদ হিসাব (সরল ও চক্রবৃদ্ধি)",
        "সাধারণ ও চক্রবৃদ্ধি সুদের হিসাব",
        ToolCategory.FINANCE, Icons.Default.TrendingUp
    ),
    ZAKAT(
        "Zakat Calculator", "যাকাত ক্যালকুলেটর",
        "সম্পদ ও নিসাব অনুযায়ী যাকাতের পরিমাণ হিসাব",
        ToolCategory.FINANCE, Icons.Default.VolunteerActivism
    ),
    SAVINGS_TARGET(
        "Savings Target", "সেভিংস টার্গেট",
        "নির্দিষ্ট লক্ষ্য অর্জনে মাসিক কত সঞ্চয় প্রয়োজন তার হিসাব",
        ToolCategory.FINANCE, Icons.Default.Savings
    ),

    // 3. Everyday Utility
    AGE(
        "Age & Birthday", "বয়স ও জন্মদিন",
        "সুনির্দিষ্ট বয়স (বছর, মাস, দিন) এবং পরবর্তী জন্মদিনের দিন গণনা",
        ToolCategory.UTILITY, Icons.Default.CalendarMonth
    ),
    DATE_DIFF(
        "Date Difference", "তারিখের ব্যবধান",
        "দুটি নির্দিষ্ট তারিখের মধ্যকার ব্যবধান বা দিন হিসাব",
        ToolCategory.UTILITY, Icons.Default.DateRange
    ),
    PERCENTAGE(
        "Percentage & Ratio", "শতকরা ও অনুপাত",
        "যেকোনো সাধারণ শতকরা এবং অনুপাতের হিসেব",
        ToolCategory.UTILITY, Icons.Default.Percent
    ),
    TIP(
        "Tip Calculator", "টিপ ও বিল ভাগ",
        "রেস্তোরাঁয় বিল ভাগ ও টিপ দেওয়ার হিসেব",
        ToolCategory.UTILITY, Icons.Default.Restaurant
    ),
    TEXT_COUNTER(
        "Word & Text Counter", "শব্দ ও অক্ষর গণনা",
        "যেকোনো টেক্সট বা প্যারাগ্রাফের শব্দ, অক্ষর ও লাইন গণনা",
        ToolCategory.UTILITY, Icons.Default.FormatQuote
    ),
    CLOTH_MEASUREMENT(
        "Cloth Measurement (Gaj-Gira)", "কাপড়ের গজ-গিরা পরিমাপ",
        "গজ, গিরা, হাত, ইঞ্চি ও মিটারের পারস্পরিক রূপান্তর",
        ToolCategory.UTILITY, Icons.Default.Straighten
    ),
    GOLD_CALCULATOR(
        "Gold & Silver Calculator", "স্বর্ণ ও রৌপ্য হিসাব",
        "ভরি, আনা, রতি ও পয়েন্ট অনুযায়ী স্বর্ণের দাম নির্ধারণ",
        ToolCategory.UTILITY, Icons.Default.AttachMoney
    ),
    PASSWORD_GENERATOR(
        "Password Generator", "পাসওয়ার্ড জেনারেটর",
        "কাস্টমাইজড এবং সুরক্ষিত স্ট্রং পাসওয়ার্ড তৈরি করুন",
        ToolCategory.DEVELOPER, Icons.Default.Password
    ),

    // 4. Electricity & Power
    ELECTRICITY_BILL(
        "Electricity Bill Calculator", "বিদ্যুৎ বিল ক্যালকুলেটর",
        "ইউনিট (kWh), বিদ্যুৎ সংযোগের ধরণ ও ট্যারিফ রেট দিয়ে মাসিক বিলের হিসাব",
        ToolCategory.ELECTRICITY, Icons.Default.ElectricMeter
    ),
    APPLIANCE_COST(
        "Appliance Energy Cost", "সরঞ্জামের বিদ্যুৎ খরচ",
        "ফ্যান, এসি, টিভি ইত্যাদির দৈনিক ও মাসিক বিদ্যুৎ খরচের হিসাব",
        ToolCategory.ELECTRICITY, Icons.Default.Power
    ),
    BATTERY_BACKUP(
        "Battery / Power Bank Backup", "ব্যাটারি/আইপিএস ব্যাকআপ",
        "ওয়াট এবং ব্যাটারির Capacity দিয়ে ব্যাটারি কতক্ষণ চলবে বের করার হিসাব",
        ToolCategory.ELECTRICITY, Icons.Default.BatteryChargingFull
    ),
    RESISTOR_CODE(
        "Resistor Color Code", "রেজিস্টর কালার কোড",
        "কালার ব্যান্ড দেখে রেজিস্ট্যান্স (Ohm) মান বের করা",
        ToolCategory.ENGINEERING, Icons.Default.SettingsInputComponent
    ),

    // 5. Smart & Vehicle Tools
    FUEL_COST(
        "Fuel Cost Calculator", "জ্বালানি খরচ ক্যালকুলেটর",
        "দূরত্ব এবং মাইলেজ দিয়ে প্রয়োজনীয় তেলের খরচ নির্ধারণ",
        ToolCategory.VEHICLE, Icons.Default.LocalGasStation
    ),
    SPEED_DISTANCE_TIME(
        "Speed, Distance & Time", "গতি, দূরত্ব ও সময়",
        "গতিবেগ, দূরত্ব বা ভ্রমণের সময় বের করা",
        ToolCategory.VEHICLE, Icons.Default.Speed
    ),

    // 6. Education & Results
    GPA(
        "GPA Calculator", "জিপিএ ক্যালকুলেটর",
        "বিষয়ভিত্তিক গ্রেড ও পয়েন্ট দিয়ে সেমিস্টার জিপিএ হিসাব",
        ToolCategory.EDUCATION, Icons.Default.School
    ),
    CGPA(
        "CGPA Calculator", "সিজিপিএ ক্যালকুলেটর",
        "বিভিন্ন সেমিস্টারের ক্রেডিট ও এসজিপিএ দিয়ে মোট সিজিপিএ হিসাব",
        ToolCategory.EDUCATION, Icons.Default.MenuBook
    ),
    TUITION_FEES(
        "Tuition Fees Calculator", "টিউশন ফিস ক্যালকুলেটর",
        "ক্রেডিট ফি, ওয়েভার ও কিস্তি অনুযায়ী মোট সেমিস্টার ফি হিসাব",
        ToolCategory.EDUCATION, Icons.Default.Receipt
    ),

    // 7. Developer Tools
    COLOR_CONVERTER(
        "Color Converter", "কালার কোড কনভার্টার",
        "HEX, RGB এবং HSL কালার কোডের মধ্যে রূপান্তর",
        ToolCategory.DEVELOPER, Icons.Default.Palette
    ),

    // 8. Everyday Smart Tools
    WEATHER(
        "Weather Forecast", "আবহাওয়া বার্তা",
        "বর্তমান আবহাওয়া, ৭ দিনের পূর্বাভাস এবং বিস্তারিত তথ্য",
        ToolCategory.UTILITY, Icons.Default.Cloud
    ),
    STOPWATCH_TIMER(
        "Stopwatch & Timer", "স্টপওয়াচ ও টাইমার",
        "কাউন্টডাউন টাইমার ও ল্যাপ সহ নির্ভুল স্টপওয়াচ",
        ToolCategory.UTILITY, Icons.Default.Timer
    ),
    NOTES_CHECKLIST(
        "Quick Notes & Memo", "কুইক নোটস ও মেমো",
        "জরুরি হিসাব, নোট ও মেমো সংরক্ষণ করার টুল",
        ToolCategory.UTILITY, Icons.Default.EditNote
    ),
    WORLD_CLOCK(
        "World Clock", "ওয়ার্ল্ড ক্লক",
        "বিশ্বের বিভিন্ন দেশের বর্তমান সময় ও টাইমজোন",
        ToolCategory.UTILITY, Icons.Default.Schedule
    ),
    UNIT_PRICE_COMPARER(
        "Unit Price Comparer", "একক দাম তুলনা",
        "দুইটি পণ্যের সাইজ ও দাম তুলনা করে সেরা ডিল নির্বাচন",
        ToolCategory.UTILITY, Icons.Default.Compare
    ),
    SIMPLE_COMPASS(
        "Digital Compass", "ডিজিটাল কম্পাস",
        "অ্যানালগ-ডিজিটাল দিক নির্ণায়ক ও লেভেলিং মিটার",
        ToolCategory.UTILITY, Icons.Default.Explore
    ),
    ASPECT_RATIO(
        "Aspect Ratio", "অ্যাসপেক্ট রেশিও",
        "ছবি, ডিসপ্লে ও ভিডিওর দৈর্ঘ্য-প্রস্থ অনুপাত নির্ণয়",
        ToolCategory.UTILITY, Icons.Default.AspectRatio
    ),
    RANDOM_NUMBER_PICKER(
        "Random Picker & Dice", "র্যান্ডম নাম্বার ও ডাইস",
        "লটারি নাম্বার, র‍্যান্ডম চয়েস, কয়েন ফ্লিপ ও ডাইস রোল",
        ToolCategory.UTILITY, Icons.Default.Casino
    ),
    MULTI_CALENDAR(
        "Smart Calendar", "স্মার্ট ক্যালেন্ডার",
        "ইংরেজি, বাংলা ও আরবি (হিজরী) ট্রিপল ক্যালেন্ডার",
        ToolCategory.UTILITY, Icons.Default.CalendarMonth
    ),
    QR_CODE(
        "QR Code Scanner & Generator", "কিউআর কোড রিডার ও জেনারেটর",
        "কিউআর কোড স্ক্যান ও দ্রুত নতুন কিউআর তৈরি করুন",
        ToolCategory.UTILITY, Icons.Default.QrCode
    ),
    PHOTO_LAB(
        "Smart Photo Lab & BG Remover", "ফটো এডিটর ও বিজি রিমুভার",
        "ফটো রিসাইজ, ক্রপ, ফরম্যাট কনভার্ট ও ব্যাকগ্রাউন্ড রিমুভ করুন",
        ToolCategory.UTILITY, Icons.Default.Image
    );

    fun getTitle(language: AppLanguage): String {
        return when (language) {
            AppLanguage.ENGLISH -> titleEn
            AppLanguage.BENGALI -> titleBn
            else -> titleEn
        }
    }

    fun getDescription(language: AppLanguage): String {
        return when (language) {
            AppLanguage.BENGALI -> descriptionBn
            else -> when (this) {
                BMI -> "Calculate body mass index and fitness category based on height and weight"
                BMR -> "Calculate daily calorie intake and basal metabolic rate"
                IDEAL_WEIGHT -> "Calculate target ideal body weight based on height"
                WATER_INTAKE -> "Track recommended daily water consumption"
                EMI_LOAN -> "Calculate bank loan monthly EMI and interest schedule"
                DISCOUNT -> "Calculate shopping discount prices and net savings"
                PROFIT_LOSS -> "Calculate business profit or loss margin percentage"
                VAT_TAX -> "Calculate VAT/tax amount and gross total price"
                INTEREST -> "Calculate simple and compound interest returns"
                AGE -> "Calculate exact age in years, months, days and next birthday"
                DATE_DIFF -> "Calculate duration and day count between two dates"
                PERCENTAGE -> "Calculate general percentage, ratio, and change"
                TIP -> "Calculate bill split amount and tip per person"
                TEXT_COUNTER -> "Count words, characters, and spaces in any text"
                CLOTH_MEASUREMENT -> "Convert between traditional Bengali cloth units (Gaj, Gira, Haat, etc.) and metric units"
                GOLD_CALCULATOR -> "Calculate gold and silver weight (Vori, Anna, Ratti, Point) and prices with carat rates"
                PASSWORD_GENERATOR -> "Generate secure customized passwords"
                ELECTRICITY_BILL -> "Calculate monthly electricity bill from kWh units and tariff"
                APPLIANCE_COST -> "Calculate power consumption cost for AC, fan, TV, etc."
                BATTERY_BACKUP -> "Calculate IPS / battery backup runtime in hours"
                FUEL_COST -> "Calculate trip fuel cost based on distance and mileage"
                SPEED_DISTANCE_TIME -> "Calculate travel speed, distance, or duration"
                GPA -> "Calculate your semester GPA based on subjects and grades"
                CGPA -> "Calculate cumulative GPA across multiple semesters"
                TUITION_FEES -> "Calculate total semester tuition fees after waivers and other charges"
                PREGNANCY_DUE -> "Calculate estimated due date and current pregnancy week"
                BLOOD_DONATION -> "Track your blood donation eligibility and next donation date"
                ZAKAT -> "Calculate payable zakat based on your assets and current nisab"
                SAVINGS_TARGET -> "Calculate monthly savings needed to reach your financial goal"
                RESISTOR_CODE -> "Calculate resistance value and tolerance from color bands"
                COLOR_CONVERTER -> "Convert colors between HEX, RGB, and HSL formats"
                STOPWATCH_TIMER -> "Precision stopwatch with laps and countdown timer"
                NOTES_CHECKLIST -> "Quick notepad and memo checklist manager"
                WORLD_CLOCK -> "Live clock and timezone converter across world cities"
                UNIT_PRICE_COMPARER -> "Compare unit prices to find the best value deal"
                SIMPLE_COMPASS -> "Digital orientation compass and spirit level"
                ASPECT_RATIO -> "Calculate screen, video, and image aspect ratios"
                RANDOM_NUMBER_PICKER -> "Random number generator, dice roller, and coin flipper"
                MULTI_CALENDAR -> "Gregorian, Bengali, and Hijri multi-calendar"
                WEATHER -> "Check current weather and 7-day forecast"
                QR_CODE -> "Scan QR codes with camera or generate custom QR codes instantly"
                PHOTO_LAB -> "Resize, crop, convert between formats, and remove backgrounds from photos"
            }
        }
    }
}

data class ChecklistItem(
    val text: String,
    val isChecked: Boolean
) {
    fun serialize(): String {
        val safeText = text.replace("::", "_COLON2_").replace("##", "_HASH2_")
        return "$safeText::$isChecked"
    }
    companion object {
        fun deserialize(str: String): ChecklistItem? {
            val parts = str.split("::")
            if (parts.size < 2) return null
            val restoredText = parts[0].replace("_COLON2_", "::").replace("_HASH2_", "##")
            return ChecklistItem(restoredText, parts[1].toBoolean())
        }
    }
}

data class ProfessionalNote(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val dateString: String = "",
    val isChecklist: Boolean = false,
    val checklistItems: List<ChecklistItem> = emptyList(),
    val colorIndex: Int = 0,
    val tag: String = "General"
) {
    fun serialize(): String {
        val safeTitle = title.replace("|||", "_PIPE3_").replace("\n", "_NL_")
        val safeContent = content.replace("|||", "_PIPE3_").replace("\n", "_NL_")
        val serializedItems = checklistItems.joinToString("##") { it.serialize() }
        return "$id|||$safeTitle|||$safeContent|||$dateString|||$isChecklist|||$serializedItems|||$colorIndex|||$tag"
    }

    companion object {
        fun deserialize(str: String): ProfessionalNote? {
            try {
                val parts = str.split("|||")
                if (parts.size < 8) return null
                val id = parts[0]
                val title = parts[1].replace("_PIPE3_", "|||").replace("_NL_", "\n")
                val content = parts[2].replace("_PIPE3_", "|||").replace("_NL_", "\n")
                val dateString = parts[3]
                val isChecklist = parts[4].toBoolean()
                val itemsList = if (parts[5].isBlank()) emptyList() else parts[5].split("##").mapNotNull {
                    ChecklistItem.deserialize(it)
                }
                val colorIndex = parts[6].toIntOrNull() ?: 0
                val tag = parts[7]
                return ProfessionalNote(id, title, content, dateString, isChecklist, itemsList, colorIndex, tag)
            } catch (e: Exception) {
                return null
            }
        }
    }
}
