package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.util.AppLanguage
import java.text.DecimalFormat

enum class ConverterCategory(
    val titleEn: String,
    val titleBn: String,
    val icon: ImageVector
) {
    COMMON("Common", "সাধারণ", Icons.Default.Straighten),
    ENGINEERING("Engineering & Physics", "প্রকৌশল ও পদার্থ", Icons.Default.Build),
    TECHNOLOGY("Technology & Data", "প্রযুক্তি ও ডেটা", Icons.Default.Storage),
    MOTION("Motion & Transport", "গতি ও পরিবহন", Icons.Default.Speed),
    ELECTRICITY("Electricity & Magnetism", "বিদ্যুৎ ও চৌম্বকত্ব", Icons.Default.ElectricalServices),
    MISC("Misc & Utility", "অন্যান্য ও ইউটিলিটি", Icons.Default.Widgets);

    fun getTitle(language: AppLanguage): String {
        return when (language) {
            AppLanguage.ENGLISH -> titleEn
            AppLanguage.BENGALI -> titleBn
            else -> titleEn
        }
    }
}

enum class ConverterType(
    val titleEn: String,
    val titleBn: String,
    val category: ConverterCategory,
    val icon: ImageVector,
    val units: List<String>
) {
    // 1. Common
    LENGTH(
        "Length", "দৈর্ঘ্য", ConverterCategory.COMMON, Icons.Default.Straighten,
        listOf("Meter", "Kilometer", "Feet", "Inch", "Centimeter", "Yard", "Mile")
    ),
    WEIGHT(
        "Weight & Mass", "ওজন", ConverterCategory.COMMON, Icons.Default.FitnessCenter,
        listOf("Kilogram", "Gram", "Pound", "Ounce", "Ton", "Milligram", "Vori", "Anna", "Ratti")
    ),
    AREA(
        "Area", "ক্ষেত্রফল", ConverterCategory.COMMON, Icons.Default.AspectRatio,
        listOf("Square Feet", "Square Meter", "Acre", "Hectare", "শতাংশ/শতক", "কাঠা", "বিঘা")
    ),
    TEMPERATURE(
        "Temperature", "তাপমাত্রা", ConverterCategory.COMMON, Icons.Default.Thermostat,
        listOf("Celsius (°C)", "Fahrenheit (°F)", "Kelvin (K)")
    ),
    VOLUME(
        "Volume", "আয়তন", ConverterCategory.COMMON, Icons.Default.Opacity,
        listOf("Liter", "Milliliter", "Gallon", "Cubic Meter")
    ),

    // 2. Engineering & Physics
    PRESSURE(
        "Pressure", "চাপ", ConverterCategory.ENGINEERING, Icons.Default.Compress,
        listOf("Bar", "Pascal", "PSI", "Atmosphere")
    ),
    POWER(
        "Power", "ক্ষমতা", ConverterCategory.ENGINEERING, Icons.Default.Bolt,
        listOf("Watt", "Kilowatt", "Horsepower (HP)")
    ),
    ENERGY(
        "Energy / Work", "শক্তি", ConverterCategory.ENGINEERING, Icons.Default.LocalFireDepartment,
        listOf("Joule", "Kilojoule", "Calorie", "Kilocalorie")
    ),
    FORCE(
        "Force", "বল", ConverterCategory.ENGINEERING, Icons.Default.TrendingUp,
        listOf("Newton", "Dyne", "Pound-force")
    ),
    TORQUE(
        "Torque", "টর্ক", ConverterCategory.ENGINEERING, Icons.Default.Build,
        listOf("Newton-meter", "Pound-foot")
    ),
    DENSITY(
        "Density", "ঘনত্ব", ConverterCategory.ENGINEERING, Icons.Default.Layers,
        listOf("kg/m³", "g/cm³")
    ),
    ANGLE(
        "Angle", "কোণ", ConverterCategory.ENGINEERING, Icons.Default.ChangeHistory,
        listOf("Degree", "Radian", "Gradian")
    ),

    // 3. Technology & Data
    DIGITAL_STORAGE(
        "Digital Storage", "ডেটা স্টোরেজ", ConverterCategory.TECHNOLOGY, Icons.Default.Storage,
        listOf("Byte", "KB", "MB", "GB", "TB", "PB")
    ),
    DATA_TRANSFER(
        "Data Transfer Rate", "ডেটা স্পিড", ConverterCategory.TECHNOLOGY, Icons.Default.NetworkCheck,
        listOf("Mbps", "Gbps", "MB/s", "KB/s")
    ),
    FREQUENCY(
        "Frequency", "ফ্রিকোয়েন্সি", ConverterCategory.TECHNOLOGY, Icons.Default.Waves,
        listOf("Hertz (Hz)", "Kilohertz (kHz)", "Megahertz (MHz)", "Gigahertz (GHz)")
    ),
    NUMBER_SYSTEM(
        "Number System", "সংখ্যা পদ্ধতি", ConverterCategory.TECHNOLOGY, Icons.Default.Code,
        listOf("Decimal (10)", "Binary (2)", "Octal (8)", "Hexadecimal (16)")
    ),

    // 4. Motion & Transport
    SPEED(
        "Speed", "গতিবেগ", ConverterCategory.MOTION, Icons.Default.Speed,
        listOf("Km/h", "Mph", "m/s", "Knot")
    ),
    TIME(
        "Time", "সময়", ConverterCategory.MOTION, Icons.Default.Schedule,
        listOf("Second", "Minute", "Hour", "Day", "Week", "Month", "Year")
    ),
    FUEL_CONSUMPTION(
        "Fuel Consumption", "জ্বালানি খরচ", ConverterCategory.MOTION, Icons.Default.LocalGasStation,
        listOf("km/L", "L/100km", "MPG")
    ),
    ACCELERATION(
        "Acceleration", "ত্বরণ", ConverterCategory.MOTION, Icons.Default.FastForward,
        listOf("m/s²", "g-force")
    ),

    // 5. Electricity & Magnetism
    ELECTRIC_CURRENT(
        "Electric Current", "বিদ্যুৎ প্রবাহ", ConverterCategory.ELECTRICITY, Icons.Default.ElectricalServices,
        listOf("Ampere", "Milliampere")
    ),
    VOLTAGE(
        "Voltage", "ভোল্টেজ", ConverterCategory.ELECTRICITY, Icons.Default.ElectricMeter,
        listOf("Volt", "Kilovolt", "Millivolt")
    ),
    RESISTANCE(
        "Resistance", "রোধ", ConverterCategory.ELECTRICITY, Icons.Default.Power,
        listOf("Ohm", "Kiloohm", "Megaohm")
    ),
    ELECTRIC_CHARGE(
        "Electric Charge", "চার্জ", ConverterCategory.ELECTRICITY, Icons.Default.BatteryChargingFull,
        listOf("Coulomb", "Ampere-hour (Ah)", "Milliampere-hour (mAh)")
    ),

    // 6. Misc & Utility
    COOKING(
        "Cooking / Kitchen", "রন্ধনশিল্প", ConverterCategory.MISC, Icons.Default.Restaurant,
        listOf("Teaspoon", "Tablespoon", "Cup", "Fluid Ounce")
    ),
    TYPOGRAPHY(
        "Typography", "টাইপোগ্রাফি", ConverterCategory.MISC, Icons.Default.TextFields,
        listOf("Pixel (px)", "Point (pt)", "EM", "REM")
    ),
    CURRENCY(
        "Currency", "মুদ্রা", ConverterCategory.MISC, Icons.Default.AttachMoney,
        listOf(
            "USD - US Dollar",
            "BDT - Bangladeshi Taka",
            "EUR - Euro",
            "GBP - British Pound",
            "INR - Indian Rupee",
            "SAR - Saudi Riyal",
            "AED - UAE Dirham",
            "MYR - Malaysian Ringgit",
            "SGD - Singapore Dollar",
            "CAD - Canadian Dollar",
            "AUD - Australian Dollar",
            "JPY - Japanese Yen",
            "CNY - Chinese Yuan",
            "PKR - Pakistani Rupee",
            "LKR - Sri Lankan Rupee",
            "TRY - Turkish Lira",
            "RUB - Russian Ruble",
            "KWD - Kuwaiti Dinar",
            "BHD - Bahraini Dinar",
            "OMR - Omani Rial",
            "QAR - Qatari Riyal",
            "THB - Thai Baht",
            "IDR - Indonesian Rupiah",
            "KRW - South Korean Won",
            "BRL - Brazilian Real",
            "MXN - Mexican Peso",
            "EGP - Egyptian Pound",
            "NGN - Nigerian Naira",
            "CHF - Swiss Franc",
            "NZD - New Zealand Dollar",
            "ZAR - South African Rand"
        )
    ),
    ROMAN_NUMERALS(
        "Roman Numerals", "রোমান সংখ্যা", ConverterCategory.MISC, Icons.Default.Tag,
        listOf("Integer", "Roman")
    ),
    TIME_ZONE(
        "Time Zone", "টাইম জোন", ConverterCategory.MISC, Icons.Default.Public,
        listOf("UTC", "BST (Bangladesh)", "IST (India)", "EST", "PST", "GMT", "CET", "JST")
    );

    fun getTitle(language: AppLanguage): String {
        return when (language) {
            AppLanguage.ENGLISH -> titleEn
            AppLanguage.BENGALI -> titleBn
            else -> titleEn
        }
    }

    fun getLocalizedUnitName(unit: String, language: AppLanguage): String {
        if (language != AppLanguage.BENGALI) {
            return when (unit) {
                "শতাংশ/শতক" -> "Percent / Shatangsho"
                "কাঠা" -> "Katha"
                "বিঘা" -> "Bigha"
                else -> unit
            }
        }
        return when (unit) {
            "Meter" -> "মিটার"
            "Kilometer" -> "কিলোমিটার"
            "Feet" -> "ফুট"
            "Inch" -> "ইঞ্চি"
            "Centimeter" -> "সেন্টিমিটার"
            "Yard" -> "গজ"
            "Mile" -> "মাইল"

            "Kilogram" -> "কিলোগ্রাম (কেজি)"
            "Gram" -> "গ্রাম"
            "Pound" -> "পাউন্ড"
            "Ounce" -> "আউন্স"
            "Ton" -> "টন"
            "Milligram" -> "মিলিগ্রাম"
            "Vori" -> "ভরি"
            "Anna" -> "আনা"
            "Ratti" -> "রতি"

            "Square Feet" -> "বর্গফুট"
            "Square Meter" -> "বর্গমিটার"
            "Acre" -> "একর"
            "Hectare" -> "হেক্টর"
            "শতাংশ/শতক" -> "শতাংশ/শতক"
            "কাঠা" -> "কাঠা"
            "বিঘা" -> "বিঘা"

            "Celsius (°C)" -> "সেলসিয়াস (°C)"
            "Fahrenheit (°F)" -> "ফারেনহাইট (°F)"
            "Kelvin (K)" -> "কেলভিন (K)"

            "Liter" -> "লিটার"
            "Milliliter" -> "মিলিলিটার"
            "Gallon" -> "গ্যালন"
            "Cubic Meter" -> "ঘনমিটার"

            "Second" -> "সেকেন্ড"
            "Minute" -> "মিনিট"
            "Hour" -> "ঘণ্টা"
            "Day" -> "দিন"
            "Week" -> "সপ্তাহ"
            "Month" -> "মাস"
            "Year" -> "বছর"

            "Km/h" -> "কিমি/ঘণ্টা"
            "Mph" -> "মাইল/ঘণ্টা"
            "m/s" -> "মিটার/সেকেন্ড"
            "Knot" -> "নট"

            "USD - US Dollar" -> "USD - মার্কিন ডলার"
            "BDT - Bangladeshi Taka" -> "BDT - বাংলাদেশী টাকা"
            "EUR - Euro" -> "EUR - ইউরো"
            "GBP - British Pound" -> "GBP - পাউন্ড"
            "INR - Indian Rupee" -> "INR - ভারতীয় রুপি"
            "SAR - Saudi Riyal" -> "SAR - সৌদি রিয়াল"
            "AED - UAE Dirham" -> "AED - ইউএই দিরহাম"
            "MYR - Malaysian Ringgit" -> "MYR - মালয়েশিয়ান রিঙ্গিত"
            "SGD - Singapore Dollar" -> "SGD - সিঙ্গাপুর ডলার"

            else -> unit
        }
    }

    fun convert(from: String, to: String, value: Double, customRates: Map<String, Double>? = null): Double {
        if (from == to) return value
        return when (this) {
            LENGTH -> {
                val inMeters = when (from) {
                    "Meter" -> value
                    "Kilometer" -> value * 1000.0
                    "Feet" -> value * 0.3048
                    "Inch" -> value * 0.0254
                    "Centimeter" -> value * 0.01
                    "Yard" -> value * 0.9144
                    "Mile" -> value * 1609.344
                    else -> value
                }
                when (to) {
                    "Meter" -> inMeters
                    "Kilometer" -> inMeters / 1000.0
                    "Feet" -> inMeters / 0.3048
                    "Inch" -> inMeters / 0.0254
                    "Centimeter" -> inMeters / 0.01
                    "Yard" -> inMeters / 0.9144
                    "Mile" -> inMeters / 1609.344
                    else -> inMeters
                }
            }
            WEIGHT -> {
                val inGrams = when (from) {
                    "Kilogram", "কিলোগ্রাম (কেজি)" -> value * 1000.0
                    "Gram", "গ্রাম" -> value
                    "Pound", "পাউন্ড" -> value * 453.59237
                    "Ounce", "আউন্স" -> value * 28.349523125
                    "Ton", "টন" -> value * 1000000.0
                    "Milligram", "মিলিগ্রাম" -> value * 0.001
                    "Vori", "ভরি" -> value * 11.664
                    "Anna", "আনা" -> value * (11.664 / 16.0)
                    "Ratti", "রতি" -> value * (11.664 / 96.0)
                    else -> value
                }
                when (to) {
                    "Kilogram", "কিলোগ্রাম (কেজি)" -> inGrams / 1000.0
                    "Gram", "গ্রাম" -> inGrams
                    "Pound", "পাউন্ড" -> inGrams / 453.59237
                    "Ounce", "আউন্স" -> inGrams / 28.349523125
                    "Ton", "টন" -> inGrams / 1000000.0
                    "Milligram", "মিলিগ্রাম" -> inGrams / 0.001
                    "Vori", "ভরি" -> inGrams / 11.664
                    "Anna", "আনা" -> inGrams / (11.664 / 16.0)
                    "Ratti", "রতি" -> inGrams / (11.664 / 96.0)
                    else -> inGrams
                }
            }
            AREA -> {
                val inSqM = when (from) {
                    "Square Feet" -> value * 0.09290304
                    "Square Meter" -> value
                    "Acre" -> value * 4046.8564224
                    "Hectare" -> value * 10000.0
                    "শতাংশ/শতক" -> value * 40.468564224
                    "কাঠা" -> value * 66.890304
                    "বিঘা" -> value * 1337.80608
                    else -> value
                }
                when (to) {
                    "Square Feet" -> inSqM / 0.09290304
                    "Square Meter" -> inSqM
                    "Acre" -> inSqM / 4046.8564224
                    "Hectare" -> inSqM / 10000.0
                    "শতাংশ/শতক" -> inSqM / 40.468564224
                    "কাঠা" -> inSqM / 66.890304
                    "বিঘা" -> inSqM / 1337.80608
                    else -> inSqM
                }
            }
            TEMPERATURE -> {
                when (from) {
                    "Celsius (°C)" -> when (to) {
                        "Fahrenheit (°F)" -> (value * 9.0 / 5.0) + 32.0
                        "Kelvin (K)" -> value + 273.15
                        else -> value
                    }
                    "Fahrenheit (°F)" -> when (to) {
                        "Celsius (°C)" -> (value - 32.0) * 5.0 / 9.0
                        "Kelvin (K)" -> ((value - 32.0) * 5.0 / 9.0) + 273.15
                        else -> value
                    }
                    "Kelvin (K)" -> when (to) {
                        "Celsius (°C)" -> value - 273.15
                        "Fahrenheit (°F)" -> ((value - 273.15) * 9.0 / 5.0) + 32.0
                        else -> value
                    }
                    else -> value
                }
            }
            VOLUME -> {
                val inLiters = when (from) {
                    "Liter" -> value
                    "Milliliter" -> value * 0.001
                    "Gallon" -> value * 3.785411784
                    "Cubic Meter" -> value * 1000.0
                    else -> value
                }
                when (to) {
                    "Liter" -> inLiters
                    "Milliliter" -> inLiters / 0.001
                    "Gallon" -> inLiters / 3.785411784
                    "Cubic Meter" -> inLiters / 1000.0
                    else -> inLiters
                }
            }
            PRESSURE -> {
                val inPascal = when (from) {
                    "Bar" -> value * 100000.0
                    "Pascal" -> value
                    "PSI" -> value * 6894.75729
                    "Atmosphere" -> value * 101325.0
                    else -> value
                }
                when (to) {
                    "Bar" -> inPascal / 100000.0
                    "Pascal" -> inPascal
                    "PSI" -> inPascal / 6894.75729
                    "Atmosphere" -> inPascal / 101325.0
                    else -> inPascal
                }
            }
            POWER -> {
                val inWatts = when (from) {
                    "Watt" -> value
                    "Kilowatt" -> value * 1000.0
                    "Horsepower (HP)" -> value * 745.699872
                    else -> value
                }
                when (to) {
                    "Watt" -> inWatts
                    "Kilowatt" -> inWatts / 1000.0
                    "Horsepower (HP)" -> inWatts / 745.699872
                    else -> inWatts
                }
            }
            ENERGY -> {
                val inJoules = when (from) {
                    "Joule" -> value
                    "Kilojoule" -> value * 1000.0
                    "Calorie" -> value * 4.184
                    "Kilocalorie" -> value * 4184.0
                    else -> value
                }
                when (to) {
                    "Joule" -> inJoules
                    "Kilojoule" -> inJoules / 1000.0
                    "Calorie" -> inJoules / 4.184
                    "Kilocalorie" -> inJoules / 4184.0
                    else -> inJoules
                }
            }
            FORCE -> {
                val inNewton = when (from) {
                    "Newton" -> value
                    "Dyne" -> value * 0.00001
                    "Pound-force" -> value * 4.448221615
                    else -> value
                }
                when (to) {
                    "Newton" -> inNewton
                    "Dyne" -> inNewton / 0.00001
                    "Pound-force" -> inNewton / 4.448221615
                    else -> inNewton
                }
            }
            TORQUE -> {
                val inNm = when (from) {
                    "Newton-meter" -> value
                    "Pound-foot" -> value * 1.3558179483
                    else -> value
                }
                when (to) {
                    "Newton-meter" -> inNm
                    "Pound-foot" -> inNm / 1.3558179483
                    else -> inNm
                }
            }
            DENSITY -> {
                val inKgM3 = when (from) {
                    "kg/m³" -> value
                    "g/cm³" -> value * 1000.0
                    else -> value
                }
                when (to) {
                    "kg/m³" -> inKgM3
                    "g/cm³" -> inKgM3 / 1000.0
                    else -> inKgM3
                }
            }
            ANGLE -> {
                val inDeg = when (from) {
                    "Degree" -> value
                    "Radian" -> value * (180.0 / Math.PI)
                    "Gradian" -> value * 0.9
                    else -> value
                }
                when (to) {
                    "Degree" -> inDeg
                    "Radian" -> inDeg / (180.0 / Math.PI)
                    "Gradian" -> inDeg / 0.9
                    else -> inDeg
                }
            }
            DIGITAL_STORAGE -> {
                val inBytes = when (from) {
                    "Byte" -> value
                    "KB" -> value * 1024.0
                    "MB" -> value * 1048576.0
                    "GB" -> value * 1073741824.0
                    "TB" -> value * 1099511627776.0
                    "PB" -> value * 1125899906842624.0
                    else -> value
                }
                when (to) {
                    "Byte" -> inBytes
                    "KB" -> inBytes / 1024.0
                    "MB" -> inBytes / 1048576.0
                    "GB" -> inBytes / 1073741824.0
                    "TB" -> inBytes / 1099511627776.0
                    "PB" -> inBytes / 1125899906842624.0
                    else -> inBytes
                }
            }
            DATA_TRANSFER -> {
                val inMbps = when (from) {
                    "Mbps" -> value
                    "Gbps" -> value * 1000.0
                    "MB/s" -> value * 8.0
                    "KB/s" -> value * 0.008
                    else -> value
                }
                when (to) {
                    "Mbps" -> inMbps
                    "Gbps" -> inMbps / 1000.0
                    "MB/s" -> inMbps / 8.0
                    "KB/s" -> inMbps / 0.008
                    else -> inMbps
                }
            }
            FREQUENCY -> {
                val inHz = when (from) {
                    "Hertz (Hz)" -> value
                    "Kilohertz (kHz)" -> value * 1000.0
                    "Megahertz (MHz)" -> value * 1_000_000.0
                    "Gigahertz (GHz)" -> value * 1_000_000_000.0
                    else -> value
                }
                when (to) {
                    "Hertz (Hz)" -> inHz
                    "Kilohertz (kHz)" -> inHz / 1000.0
                    "Megahertz (MHz)" -> inHz / 1_000_000.0
                    "Gigahertz (GHz)" -> inHz / 1_000_000_000.0
                    else -> inHz
                }
            }
            NUMBER_SYSTEM -> {
                value
            }
            SPEED -> {
                val inMs = when (from) {
                    "m/s" -> value
                    "Km/h" -> value / 3.6
                    "Mph" -> value * 0.44704
                    "Knot" -> value * 0.514444
                    else -> value
                }
                when (to) {
                    "m/s" -> inMs
                    "Km/h" -> inMs * 3.6
                    "Mph" -> inMs / 0.44704
                    "Knot" -> inMs / 0.514444
                    else -> inMs
                }
            }
            TIME -> {
                val inSec = when (from) {
                    "Second" -> value
                    "Minute" -> value * 60.0
                    "Hour" -> value * 3600.0
                    "Day" -> value * 86400.0
                    "Week" -> value * 604800.0
                    "Month" -> value * 2592000.0
                    "Year" -> value * 31536000.0
                    else -> value
                }
                when (to) {
                    "Second" -> inSec
                    "Minute" -> inSec / 60.0
                    "Hour" -> inSec / 3600.0
                    "Day" -> inSec / 86400.0
                    "Week" -> inSec / 604800.0
                    "Month" -> inSec / 2592000.0
                    "Year" -> inSec / 31536000.0
                    else -> inSec
                }
            }
            FUEL_CONSUMPTION -> {
                when (from) {
                    "km/L" -> when (to) {
                        "L/100km" -> if (value == 0.0) 0.0 else 100.0 / value
                        "MPG" -> value * 2.35214583
                        else -> value
                    }
                    "L/100km" -> when (to) {
                        "km/L" -> if (value == 0.0) 0.0 else 100.0 / value
                        "MPG" -> if (value == 0.0) 0.0 else 235.214583 / value
                        else -> value
                    }
                    "MPG" -> when (to) {
                        "km/L" -> value / 2.35214583
                        "L/100km" -> if (value == 0.0) 0.0 else 235.214583 / value
                        else -> value
                    }
                    else -> value
                }
            }
            ACCELERATION -> {
                val inMs2 = when (from) {
                    "m/s²" -> value
                    "g-force" -> value * 9.80665
                    else -> value
                }
                when (to) {
                    "m/s²" -> inMs2
                    "g-force" -> inMs2 / 9.80665
                    else -> inMs2
                }
            }
            ELECTRIC_CURRENT -> {
                val inAmp = when (from) {
                    "Ampere" -> value
                    "Milliampere" -> value * 0.001
                    else -> value
                }
                when (to) {
                    "Ampere" -> inAmp
                    "Milliampere" -> inAmp / 0.001
                    else -> inAmp
                }
            }
            VOLTAGE -> {
                val inVolt = when (from) {
                    "Volt" -> value
                    "Kilovolt" -> value * 1000.0
                    "Millivolt" -> value * 0.001
                    else -> value
                }
                when (to) {
                    "Volt" -> inVolt
                    "Kilovolt" -> inVolt / 1000.0
                    "Millivolt" -> inVolt / 0.001
                    else -> inVolt
                }
            }
            RESISTANCE -> {
                val inOhm = when (from) {
                    "Ohm" -> value
                    "Kiloohm" -> value * 1000.0
                    "Megaohm" -> value * 1000000.0
                    else -> value
                }
                when (to) {
                    "Ohm" -> inOhm
                    "Kiloohm" -> inOhm / 1000.0
                    "Megaohm" -> inOhm / 1000000.0
                    else -> inOhm
                }
            }
            ELECTRIC_CHARGE -> {
                val inCoulomb = when (from) {
                    "Coulomb" -> value
                    "Ampere-hour (Ah)" -> value * 3600.0
                    "Milliampere-hour (mAh)" -> value * 3.6
                    else -> value
                }
                when (to) {
                    "Coulomb" -> inCoulomb
                    "Ampere-hour (Ah)" -> inCoulomb / 3600.0
                    "Milliampere-hour (mAh)" -> inCoulomb / 3.6
                    else -> inCoulomb
                }
            }
            COOKING -> {
                val inMl = when (from) {
                    "Teaspoon" -> value * 4.92892159
                    "Tablespoon" -> value * 14.7867648
                    "Cup" -> value * 240.0
                    "Fluid Ounce" -> value * 29.5735296
                    else -> value
                }
                when (to) {
                    "Teaspoon" -> inMl / 4.92892159
                    "Tablespoon" -> inMl / 14.7867648
                    "Cup" -> inMl / 240.0
                    "Fluid Ounce" -> inMl / 29.5735296
                    else -> inMl
                }
            }
            TYPOGRAPHY -> {
                val inPx = when (from) {
                    "Pixel (px)" -> value
                    "Point (pt)" -> value * 1.33333333
                    "EM" -> value * 16.0
                    "REM" -> value * 16.0
                    else -> value
                }
                when (to) {
                    "Pixel (px)" -> inPx
                    "Point (pt)" -> inPx / 1.33333333
                    "EM" -> inPx / 16.0
                    "REM" -> inPx / 16.0
                    else -> inPx
                }
            }
            CURRENCY -> {
                val defaultRates = mapOf(
                    "USD" to 1.0,
                    "BDT" to 121.5,
                    "EUR" to 0.92,
                    "GBP" to 0.78,
                    "INR" to 83.8,
                    "SAR" to 3.75,
                    "AED" to 3.67,
                    "MYR" to 4.42,
                    "SGD" to 1.34,
                    "CAD" to 1.37,
                    "AUD" to 1.52,
                    "JPY" to 147.5,
                    "CNY" to 7.17,
                    "PKR" to 278.5,
                    "LKR" to 302.0,
                    "TRY" to 33.5,
                    "RUB" to 88.0,
                    "KWD" to 0.31,
                    "BHD" to 0.38,
                    "OMR" to 0.38,
                    "QAR" to 3.64,
                    "THB" to 35.2,
                    "IDR" to 15800.0,
                    "KRW" to 1365.0,
                    "BRL" to 5.50,
                    "MXN" to 18.8,
                    "EGP" to 48.5,
                    "NGN" to 1600.0,
                    "CHF" to 0.86,
                    "NZD" to 1.66,
                    "ZAR" to 18.2
                )
                val rates = customRates ?: defaultRates
                val codeFrom = from.split(" - ").firstOrNull()?.trim() ?: from
                val codeTo = to.split(" - ").firstOrNull()?.trim() ?: to

                val rateFrom = rates[codeFrom] ?: rates[from] ?: 1.0
                val rateTo = rates[codeTo] ?: rates[to] ?: 1.0

                val inUsd = value / rateFrom
                inUsd * rateTo
            }
            ROMAN_NUMERALS -> value
            TIME_ZONE -> value
        }
    }
}
