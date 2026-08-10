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
    EDUCATION("Education & Results", "শিক্ষা ও ফলাফল", Icons.Default.School);

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
            }
            AppLanguage.ARABIC -> when (this) {
                HEALTH -> "الصحة واللياقة"
                FINANCE -> "المالية والأعمال"
                UTILITY -> "أدوات يومية"
                ELECTRICITY -> "الكهرباء والطاقة"
                VEHICLE -> "أدوات المركبات"
                EDUCATION -> "التعليم والنتائج"
            }
            AppLanguage.FRENCH -> when (this) {
                HEALTH -> "Santé et Forme"
                FINANCE -> "Finance et Affaires"
                UTILITY -> "Utilitaires du Quotidien"
                ELECTRICITY -> "Électricité et Énergie"
                VEHICLE -> "Outils Véhicule"
                EDUCATION -> "Éducation et Résultats"
            }
            AppLanguage.SPANISH -> when (this) {
                HEALTH -> "Salud y Bienestar"
                FINANCE -> "Finanzas y Negocios"
                UTILITY -> "Utilidades Diarias"
                ELECTRICITY -> "Electricidad y Energía"
                VEHICLE -> "Herramientas de Vehículo"
                EDUCATION -> "Educación y Resultados"
            }
            AppLanguage.GERMAN -> when (this) {
                HEALTH -> "Gesundheit & Fitness"
                FINANCE -> "Finanzen & Geschäft"
                UTILITY -> "Alltags-Werkzeuge"
                ELECTRICITY -> "Elektrizität & Strom"
                VEHICLE -> "Fahrzeug-Tools"
                EDUCATION -> "Bildung & Ergebnisse"
            }
            AppLanguage.CHINESE -> when (this) {
                HEALTH -> "健康与健身"
                FINANCE -> "金融与商业"
                UTILITY -> "日常实用工具"
                ELECTRICITY -> "电力与能源"
                VEHICLE -> "车辆与出行"
                EDUCATION -> "教育与成果"
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
                ELECTRICITY_BILL -> "Calculate monthly electricity bill from kWh units and tariff"
                APPLIANCE_COST -> "Calculate power consumption cost for AC, fan, TV, etc."
                BATTERY_BACKUP -> "Calculate IPS / battery backup runtime in hours"
                FUEL_COST -> "Calculate trip fuel cost based on distance and mileage"
                SPEED_DISTANCE_TIME -> "Calculate travel speed, distance, or duration"
                GPA -> "Calculate your semester GPA based on subjects and grades"
                CGPA -> "Calculate cumulative GPA across multiple semesters"
                TUITION_FEES -> "Calculate total semester tuition fees after waivers and other charges"
            }
        }
    }
}
