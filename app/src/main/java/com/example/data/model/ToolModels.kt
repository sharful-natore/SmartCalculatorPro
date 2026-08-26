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
    ISLAMIC("Islamic Services", "ইসলামিক সার্ভিসেস", Icons.Default.NightsStay),
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
                ISLAMIC -> "इस्लामिक सेवाएं"
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
                ISLAMIC -> "الخدمات الإسلامية"
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
                ISLAMIC -> "Services Islamiques"
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
                ISLAMIC -> "Servicios Islámicos"
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
                ISLAMIC -> "Islamische Dienste"
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
                ISLAMIC -> "伊斯兰服务"
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
    MARKET_LIST(
        "Market Shopping List", "বাজার লিস্ট",
        "বাজারের ফর্দ তৈরি, মালামাল কেনা, দাম ও পরিমাণের হিসাব, মেমো সেভ ও পিডিএফ এক্সপোর্ট",
        ToolCategory.UTILITY, Icons.Default.ShoppingCart
    ),
    CAMERA_LEVEL(
        "AR Camera Level & Angle Meter", "ক্যামেরা লেভেলার ও কোণ মিটার",
        "ক্যামেরা ও সেন্সরের সাহায্যে যেকোনো বস্তু উলম্ব বা আনুভূমিক সোজা আছে কিনা তা নিখুঁতভাবে দেখুন ও কোণ পরিমাপ করুন",
        ToolCategory.UTILITY, Icons.Default.SquareFoot
    ),
    METAL_DETECTOR(
        "Metal Detector", "মেটাল ডিটেক্টর",
        "ম্যাগনেটোমিটার সেন্সরের সাহায্যে ধাতু, তার ও চৌম্বক ক্ষেত্র সনাক্তকরণ",
        ToolCategory.UTILITY, Icons.Default.Sensors
    ),
    MULTI_CALENDAR(
        "Smart Calendar", "স্মার্ট ক্যালেন্ডার",
        "ইংরেজি, বাংলা ও আরবি (হিজরী) ট্রিপল ক্যালেন্ডার",
        ToolCategory.UTILITY, Icons.Default.CalendarMonth
    ),
    SIMPLE_COMPASS(
        "Digital Compass", "ডিজিটাল কম্পাস",
        "অ্যানালগ-ডিজিটাল দিক নির্ণায়ক ও লেভেলিং মিটার",
        ToolCategory.UTILITY, Icons.Default.Explore
    ),
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
    PHOTO_LAB(
        "Photo Resizer Tool", "ফটো রিসাইজার টুল",
        "ফটো ক্রপ, কাস্টম মাপ (px/cm/mm/inch), ফাইল সাইজ (KB/MB) ও ফরম্যাট পরিবর্তন",
        ToolCategory.UTILITY, Icons.Default.Image
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
    UNIT_PRICE_COMPARER(
        "Unit Price Comparer", "একক দাম তুলনা",
        "দুইটি পণ্যের সাইজ ও দাম তুলনা করে সেরা ডিল নির্বাচন",
        ToolCategory.UTILITY, Icons.Default.Compare
    ),
    ASPECT_RATIO(
        "Aspect Ratio", "অ্যাসপেক্ট রেশিও",
        "ছবি, ডিসপ্লে ও ভিডিওর দৈর্ঘ্য-প্রস্থ অনুপাত নির্ণয়",
        ToolCategory.UTILITY, Icons.Default.AspectRatio
    ),
    RANDOM_NUMBER_PICKER(
        "Lottery & Toss / Dice", "লটারি/টস",
        "লটারি ও র‍্যান্ডম নাম্বার, বাস্তবসম্মত ৩ডি কয়েন টস ও অ্যানিমেটেড ডাইস রোল",
        ToolCategory.UTILITY, Icons.Default.Casino
    ),
    QR_BARCODE(
        "QR & Barcode Scanner/Creator", "কিউআর ও বারকোড টুল",
        "যেকোনো বারকোড ও কিউআর কোড ফাস্ট স্ক্যান করা এবং নতুন কিউআর তৈরি করার টুল",
        ToolCategory.UTILITY, Icons.Default.QrCodeScanner
    ),
    PDF_READER(
        "PDF Reader & Viewer", "পিডিএফ রিডার",
        "ডিভাইসের যেকোনো পিডিএফ ফাইল ফাইল ওপেন করে পড়া, জুম করা ও ফ্রেম বাই ফ্রেম দেখার সুবিধা",
        ToolCategory.UTILITY, Icons.Default.PictureAsPdf
    ),
    PDF_MAKER(
        "PDF Maker & Document Creator", "পিডিএফ মেকার",
        "টেক্সট নথি, নোট বা ছবি দিয়ে কাস্টম এ৪/লেটার সাইজের পিডিএফ ফাইল তৈরি ও শেয়ারিং",
        ToolCategory.UTILITY, Icons.Default.PictureAsPdf
    ),
    // 4. Developer Tools
    PASSWORD_GENERATOR(
        "Password Generator", "পাসওয়ার্ড জেনারেটর",
        "কাস্টমাইজড এবং সুরক্ষিত স্ট্রং পাসওয়ার্ড তৈরি করুন",
        ToolCategory.DEVELOPER, Icons.Default.Password
    ),
    COLOR_CONVERTER(
        "Color Converter", "কালার কোড কনভার্টার",
        "HEX, RGB এবং HSL কালার কোডের মধ্যে রূপান্তর",
        ToolCategory.DEVELOPER, Icons.Default.Palette
    ),

    // 5. Electricity & Power
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

    // 6. Smart & Vehicle Tools
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

    // 7. Education & Results
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

    PHONE_DIAGNOSTICS(
        "Phone Diagnostics", "ফোন ডায়াগনসিস",
        "ফোনের সকল সেন্সর, টাচস্ক্রিন, স্পিকার, ভাইব্রেশন ও ডিসপ্লে ডায়াগনস্টিক টেস্ট",
        ToolCategory.DEVELOPER, Icons.Default.FactCheck
    ),
    DEVICE_INFO(
        "Device Info", "ডিভাইস ইনফো",
        "প্রসেসর, র‍্যাম, স্টোরেজ, ব্যাটারি, ডিসপ্লে, ওএস এবং সিস্টেমের বিস্তারিত স্পেক্স",
        ToolCategory.DEVELOPER, Icons.Default.PhoneAndroid
    ),
    BATTERY_MONITOR(
        "Battery Monitor", "ব্যাটারি মনিটর",
        "রিয়েল-টাইম চার্জিং কারেন্ট, ভোল্টেজ, চার্জের গতি, ব্যাটারি স্বাস্থ্য ও তাপমাত্রার গ্রাফিকাল পর্যবেক্ষণ",
        ToolCategory.DEVELOPER, Icons.Default.BatteryChargingFull
    ),

    // 9. Islamic Tools
    QIBLA_COMPASS(
        "Qibla Compass", "কিবলা কম্পাস",
        "মক্কা শরিফের আল-কাবা শরীফের দিকে নির্ভুল কিবলা দিক নির্ণয় করুন",
        ToolCategory.ISLAMIC, Icons.Default.Explore
    ),
    HOLY_QURAN(
        "Al Quran", "আল কুরআন",
        "সূরা ও আয়াত পাঠ, বাংলা অর্থ, অডিও তেলাওয়াত ডাউনলোড এবং এআই ডিজিটাল অ্যাসিস্ট্যান্ট",
        ToolCategory.ISLAMIC, Icons.Default.AutoStories
    ),
    DIGITAL_TASBIH(
        "Digital Tasbih", "ডিজিটাল তাসবিহ",
        "ডিজিটাল জিকির ও তাসবিহ গণনার স্মার্ট কাউন্টার",
        ToolCategory.ISLAMIC, Icons.Default.FormatListNumbered
    ),
    PRAYER_TIMES(
        "Prayer Times Schedule", "নামাজের সময়সূচি",
        "দৈনিক ৫ ওয়াক্ত সালাতের নির্ভুল সময়সূচি ও কাউন্টডাউন",
        ToolCategory.ISLAMIC, Icons.Default.Schedule
    ),
    SEHRI_IFTAR(
        "Sehri & Iftar Schedule", "সেহরি ও ইফতারের সময়সূচি",
        "দৈনিক সেহরি ও ইফতারের সময়সূচি, কাউন্টডাউন ও গুরুত্বপূর্ণ দোয়া",
        ToolCategory.ISLAMIC, Icons.Default.NightsStay
    ),
    ISLAMIC_DUAS(
        "Daily Duas & Zikir", "দৈনন্দিন দোয়া ও জিকির",
        "কুরআন ও হাদিসের প্রয়োজনীয় দোয়াসমূহ, বাংলা উচ্চারণ ও অর্থ",
        ToolCategory.ISLAMIC, Icons.Default.MenuBook
    ),
    NAMAZ_EDUCATION(
        "Namaz & Wudu Guide", "পূর্ণাঙ্গ নামাজ ও অজু শিক্ষা",
        "অজু, তাহারাত, ৫ ওয়াক্ত নামাজ, ওয়াক্তভিত্তিক রাকাতের নিয়ম, জানাজা, ঈদ ও নফল নামাজের পূর্ণাঙ্গ শিক্ষা",
        ToolCategory.ISLAMIC, Icons.Default.Mosque
    ),
    HADITH_LIBRARY(
        "Hadith Books", "হাদিস গ্রন্থ",
        "বুখারী, মুসলিম, রিয়াদুস সালেহীন সহ অন্যান্য মূল হাদিস গ্রন্থ ডাউনলোড ও অফলাইনে পড়ুন",
        ToolCategory.ISLAMIC, Icons.Default.LibraryBooks
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
                MARKET_LIST -> "Create shopping lists, track quantity and unit prices, calculate totals, save memos, and export PDF receipts"
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
                CAMERA_LEVEL -> "Check horizontal and vertical leveling, plumb lines, and measure surface tilt angles via camera AR"
                ASPECT_RATIO -> "Calculate screen, video, and image aspect ratios"
                RANDOM_NUMBER_PICKER -> "Lottery number generator, 3D animated coin toss, and realistic dice roller"
                MULTI_CALENDAR -> "Gregorian, Bengali, and Hijri multi-calendar"
                WEATHER -> "Check current weather and 7-day forecast"
                PHOTO_LAB -> "Resize photos with custom dimensions (px/cm/mm/inch), target file size (KB), crop, and convert formats"
                METAL_DETECTOR -> "Detect metal, electromagnetic fields, and hidden wires using magnetometer sensor"
                PHONE_DIAGNOSTICS -> "Test all device sensors, touch screen, speakers, vibration, and display health"
                DEVICE_INFO -> "View comprehensive hardware, CPU, RAM, battery, display, and OS specifications"
                BATTERY_MONITOR -> "Monitor real-time battery current, voltage, temperature, health, and charging stats dynamically with active waveform charts"
                QIBLA_COMPASS -> "Accurate Qibla direction finder pointing towards the Holy Kaaba in Makkah"
                DIGITAL_TASBIH -> "Digital zikir and tasbih tally counter with goal targets and presets"
                PRAYER_TIMES -> "Daily 5 time Islamic prayer timetable with countdowns and alerts"
                SEHRI_IFTAR -> "Daily Ramadan Sehri and Iftar timetable, countdowns, and authentic duas"
                ISLAMIC_DUAS -> "Collection of daily authentic Quranic & Masnoon duas with meanings"
                HOLY_QURAN -> "Holy Quran text, audio recitation, Bangla translation, and AI assistant"
                NAMAZ_EDUCATION -> "Complete Namaz and Wudu learning guide with Rakat table, step-by-step visual postures, and duas"
                HADITH_LIBRARY -> "Read Sahih Bukhari, Sahih Muslim, Riyad as-Salihin and major Hadith collections with zero app size impact"
                QR_BARCODE -> "Ultra-fast QR and Barcode scanner and creator with zero app size impact"
                PDF_READER -> "View, zoom and read PDF documents on your device using native renderer"
                PDF_MAKER -> "Create custom A4 PDF documents from notes, text, and photos with zero app size impact"
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

fun isTitleLong(text: String, isBn: Boolean): Boolean {
    return if (isBn) text.length > 13 else text.length > 16
}

fun isSubtitleLong(text: String, isBn: Boolean): Boolean {
    return if (isBn) text.length > 25 else text.length > 31
}

