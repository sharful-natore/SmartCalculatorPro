package com.example.util

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val flag: String,
    val shortLabel: String
) {
    ENGLISH("en", "English", "🇬🇧", "EN"),
    BENGALI("bn", "বাংলা", "🇧🇩", "BN"),
    HINDI("hi", "हिंदी", "🇮🇳", "HI"),
    ARABIC("ar", "العربية", "🇸🇦", "AR"),
    FRENCH("fr", "Français", "🇫🇷", "FR"),
    SPANISH("es", "Español", "🇪🇸", "ES"),
    GERMAN("de", "Deutsch", "🇩🇪", "DE"),
    CHINESE("zh", "中文", "🇨🇳", "ZH")
}

object LanguageManager {
    fun getString(key: String, language: AppLanguage): String {
        return when (key) {
            "app_title_calc" -> when (language) {
                AppLanguage.ENGLISH -> "Smart Calculator"
                AppLanguage.BENGALI -> "স্মার্ট ক্যালকুলেটর"
                AppLanguage.HINDI -> "स्मार्ट कैलकुलेटर"
                AppLanguage.ARABIC -> "الحاسبة الذكية"
                AppLanguage.FRENCH -> "Calculatrice Intelligente"
                AppLanguage.SPANISH -> "Calculadora Inteligente"
                AppLanguage.GERMAN -> "Intelligenter Rechner"
                AppLanguage.CHINESE -> "智能计算器"
            }
            "app_title_conv" -> when (language) {
                AppLanguage.ENGLISH -> "Unit Converter"
                AppLanguage.BENGALI -> "ইউনিট কনভার্টার"
                AppLanguage.HINDI -> "इकाई कनवर्टर"
                AppLanguage.ARABIC -> "محول الوحدات"
                AppLanguage.FRENCH -> "Convertisseur d'unités"
                AppLanguage.SPANISH -> "Convertidor de unidades"
                AppLanguage.GERMAN -> "Einheitenumrechner"
                AppLanguage.CHINESE -> "单位转换器"
            }
            "app_title_tools" -> when (language) {
                AppLanguage.ENGLISH -> "Special Tools"
                AppLanguage.BENGALI -> "বিশেষ টুলস"
                AppLanguage.HINDI -> "विशेष उपकरण"
                AppLanguage.ARABIC -> "أدوات خاصة"
                AppLanguage.FRENCH -> "Outils Spéciaux"
                AppLanguage.SPANISH -> "Herramientas Especiales"
                AppLanguage.GERMAN -> "Spezialwerkzeuge"
                AppLanguage.CHINESE -> "特殊工具"
            }
            "app_title_history" -> when (language) {
                AppLanguage.ENGLISH -> "Calculation History"
                AppLanguage.BENGALI -> "হিসাবের ইতিহাস"
                AppLanguage.HINDI -> "गणना इतिहास"
                AppLanguage.ARABIC -> "سجل الحسابات"
                AppLanguage.FRENCH -> "Historique des calculs"
                AppLanguage.SPANISH -> "Historial de cálculos"
                AppLanguage.GERMAN -> "Berechnungsverlauf"
                AppLanguage.CHINESE -> "计算历史"
            }
            "app_title_themes" -> when (language) {
                AppLanguage.ENGLISH -> "Color Themes"
                AppLanguage.BENGALI -> "কালার থিম"
                AppLanguage.HINDI -> "रंग थीम"
                AppLanguage.ARABIC -> "سمات الألوان"
                AppLanguage.FRENCH -> "Thèmes de couleurs"
                AppLanguage.SPANISH -> "Temas de color"
                AppLanguage.GERMAN -> "Farbthemen"
                AppLanguage.CHINESE -> "颜色主题"
            }
            "tab_calc" -> when (language) {
                AppLanguage.ENGLISH -> "Calc"
                AppLanguage.BENGALI -> "হিসাব"
                AppLanguage.HINDI -> "गणक"
                AppLanguage.ARABIC -> "حاسبة"
                AppLanguage.FRENCH -> "Calc"
                AppLanguage.SPANISH -> "Calc"
                AppLanguage.GERMAN -> "Rechner"
                AppLanguage.CHINESE -> "计算"
            }
            "tab_conv" -> when (language) {
                AppLanguage.ENGLISH -> "Conv"
                AppLanguage.BENGALI -> "কনভার্ট"
                AppLanguage.HINDI -> "कनवर्ट"
                AppLanguage.ARABIC -> "تحويل"
                AppLanguage.FRENCH -> "Conv"
                AppLanguage.SPANISH -> "Conv"
                AppLanguage.GERMAN -> "Umrech."
                AppLanguage.CHINESE -> "转换"
            }
            "tab_tools" -> when (language) {
                AppLanguage.ENGLISH -> "Tools"
                AppLanguage.BENGALI -> "টুলস"
                AppLanguage.HINDI -> "उपकरण"
                AppLanguage.ARABIC -> "أدوات"
                AppLanguage.FRENCH -> "Outils"
                AppLanguage.SPANISH -> "Herram."
                AppLanguage.GERMAN -> "Tools"
                AppLanguage.CHINESE -> "工具"
            }
            "tab_history" -> when (language) {
                AppLanguage.ENGLISH -> "History"
                AppLanguage.BENGALI -> "ইতিহাস"
                AppLanguage.HINDI -> "इतिहास"
                AppLanguage.ARABIC -> "السجل"
                AppLanguage.FRENCH -> "Historique"
                AppLanguage.SPANISH -> "Historial"
                AppLanguage.GERMAN -> "Verlauf"
                AppLanguage.CHINESE -> "历史"
            }
            "search_tools" -> when (language) {
                AppLanguage.ENGLISH -> "Search tools... (e.g. BMI, EMI, Bill, Age)"
                AppLanguage.BENGALI -> "টুল খুঁজুন... (যেমন: BMI, EMI, বিদ্যুৎ বিল, বয়স)"
                AppLanguage.HINDI -> "उपकरण खोजें... (जैसे BMI, EMI, बिल, आयु)"
                AppLanguage.ARABIC -> "بحث عن أدوات... (مثل BMI, EMI, الفاتورة, العمر)"
                AppLanguage.FRENCH -> "Rechercher des outils... (ex: IMC, EMI, Facture, Âge)"
                AppLanguage.SPANISH -> "Buscar herramientas... (ej: IMC, EMI, Factura, Edad)"
                AppLanguage.GERMAN -> "Werkzeuge suchen... (z. B. BMI, EMI, Rechnung, Alter)"
                AppLanguage.CHINESE -> "搜索工具... (如 BMI, 房贷, 电费, 年龄)"
            }
            "search_converter" -> when (language) {
                AppLanguage.ENGLISH -> "Search converters... (e.g. Length, Currency, Mass)"
                AppLanguage.BENGALI -> "কনভার্টার খুঁজুন... (যেমন: Length, Currency, বিঘা)"
                AppLanguage.HINDI -> "कनवर्टर खोजें... (जैसे लंबाई, मुद्रा, द्रव्यमान)"
                AppLanguage.ARABIC -> "بحث عن محولات... (مثل الطول, العملة, الكتلة)"
                AppLanguage.FRENCH -> "Rechercher des convertisseurs... (ex: Longueur, Devise)"
                AppLanguage.SPANISH -> "Buscar convertidores... (ej: Longitud, Moneda, Masa)"
                AppLanguage.GERMAN -> "Umrechner suchen... (z. B. Länge, Währung, Masse)"
                AppLanguage.CHINESE -> "搜索转换器... (如 长度, 货币, 质量)"
            }
            "update_rates" -> when (language) {
                AppLanguage.ENGLISH -> "Update Exchange Rates"
                AppLanguage.BENGALI -> "লাইভ এক্সচেঞ্জ রেট আপডেট করুন"
                AppLanguage.HINDI -> "लाइव विनिमय दरें अपडेट करें"
                AppLanguage.ARABIC -> "تحديث أسعار الصرف المباشرة"
                AppLanguage.FRENCH -> "Actualiser les taux de change"
                AppLanguage.SPANISH -> "Actualizar tipos de cambio"
                AppLanguage.GERMAN -> "Wechselkurse aktualisieren"
                AppLanguage.CHINESE -> "更新实时汇率"
            }
            "updating_rates" -> when (language) {
                AppLanguage.ENGLISH -> "Updating live exchange rates..."
                AppLanguage.BENGALI -> "লাইভ এক্সচেঞ্জ রেট আপডেট করা হচ্ছে..."
                AppLanguage.HINDI -> "लाइव दरें अपडेट की जा रही हैं..."
                AppLanguage.ARABIC -> "جاري تحديث أسعار الصرف..."
                AppLanguage.FRENCH -> "Mise à jour des taux..."
                AppLanguage.SPANISH -> "Actualizando tipos de cambio..."
                AppLanguage.GERMAN -> "Kurse werden aktualisiert..."
                AppLanguage.CHINESE -> "正在更新实时汇率..."
            }
            "last_updated" -> when (language) {
                AppLanguage.ENGLISH -> "Last Updated"
                AppLanguage.BENGALI -> "সর্বশেষ আপডেট"
                AppLanguage.HINDI -> "अंतिम अपडेट"
                AppLanguage.ARABIC -> "آخر تحديث"
                AppLanguage.FRENCH -> "Dernière mise à jour"
                AppLanguage.SPANISH -> "Última actualización"
                AppLanguage.GERMAN -> "Zuletzt aktualisiert"
                AppLanguage.CHINESE -> "上次更新"
            }
            "live_rates_badge" -> when (language) {
                AppLanguage.ENGLISH -> "Live Rates (Auto-Updated)"
                AppLanguage.BENGALI -> "লাইভ রেট (অটো-আপডেটেড)"
                AppLanguage.HINDI -> "लाइव दरें (ऑटो-अपडेटेड)"
                AppLanguage.ARABIC -> "أسعار مباشرة (محدثة تلقائياً)"
                AppLanguage.FRENCH -> "Taux en direct (Mis à jour)"
                AppLanguage.SPANISH -> "Tipos en vivo (Actualizados)"
                AppLanguage.GERMAN -> "Live-Kurse (Automatisch)"
                AppLanguage.CHINESE -> "实时汇率（自动更新）"
            }
            "all" -> when (language) {
                AppLanguage.ENGLISH -> "All"
                AppLanguage.BENGALI -> "সব"
                AppLanguage.HINDI -> "सभी"
                AppLanguage.ARABIC -> "الكل"
                AppLanguage.FRENCH -> "Tous"
                AppLanguage.SPANISH -> "Todos"
                AppLanguage.GERMAN -> "Alle"
                AppLanguage.CHINESE -> "全部"
            }
            "from" -> when (language) {
                AppLanguage.ENGLISH -> "From"
                AppLanguage.BENGALI -> "ফ্রম"
                AppLanguage.HINDI -> "से (From)"
                AppLanguage.ARABIC -> "من"
                AppLanguage.FRENCH -> "De"
                AppLanguage.SPANISH -> "Desde"
                AppLanguage.GERMAN -> "Von"
                AppLanguage.CHINESE -> "从"
            }
            "to" -> when (language) {
                AppLanguage.ENGLISH -> "To"
                AppLanguage.BENGALI -> "টু"
                AppLanguage.HINDI -> "तक"
                AppLanguage.ARABIC -> "إلى"
                AppLanguage.FRENCH -> "À"
                AppLanguage.SPANISH -> "Hasta"
                AppLanguage.GERMAN -> "Nach"
                AppLanguage.CHINESE -> "至"
            }
            "exit_title" -> when (language) {
                AppLanguage.ENGLISH -> "Exit Application"
                AppLanguage.BENGALI -> "অ্যাপ বন্ধ করুন"
                AppLanguage.HINDI -> "ऐप से बाहर निकलें"
                AppLanguage.ARABIC -> "الخروج من التطبيق"
                AppLanguage.FRENCH -> "Quitter l'application"
                AppLanguage.SPANISH -> "Salir de la aplicación"
                AppLanguage.GERMAN -> "App beenden"
                AppLanguage.CHINESE -> "退出应用"
            }
            "exit_msg" -> when (language) {
                AppLanguage.ENGLISH -> "Are you sure you want to exit the app?"
                AppLanguage.BENGALI -> "আপনি কি নিশ্চিত যে অ্যাপ থেকে বের হতে চান?"
                AppLanguage.HINDI -> "क्या आप निश्चित रूप से ऐप से बाहर निकलना चाहते हैं?"
                AppLanguage.ARABIC -> "هل أنت تأكد من أنك تريد الخروج من التطبيق؟"
                AppLanguage.FRENCH -> "Êtes-vous sûr de vouloir quitter l'application ?"
                AppLanguage.SPANISH -> "<ctrl42>¿Estás seguro de que quieres salir?"
                AppLanguage.GERMAN -> "Möchten Sie die App wirklich beenden?"
                AppLanguage.CHINESE -> "确定要退出应用吗？"
            }
            "exit_confirm" -> when (language) {
                AppLanguage.ENGLISH -> "Exit"
                AppLanguage.BENGALI -> "বের হন"
                AppLanguage.HINDI -> "बाहर जाएं"
                AppLanguage.ARABIC -> "خروج"
                AppLanguage.FRENCH -> "Quitter"
                AppLanguage.SPANISH -> "Salir"
                AppLanguage.GERMAN -> "Beenden"
                AppLanguage.CHINESE -> "退出"
            }
            "exit_cancel" -> when (language) {
                AppLanguage.ENGLISH -> "Cancel"
                AppLanguage.BENGALI -> "বাতিল"
                AppLanguage.HINDI -> "रद्द करें"
                AppLanguage.ARABIC -> "إلغاء"
                AppLanguage.FRENCH -> "Annuler"
                AppLanguage.SPANISH -> "Cancelar"
                AppLanguage.GERMAN -> "Abbrechen"
                AppLanguage.CHINESE -> "取消"
            }
            "quick_table" -> when (language) {
                AppLanguage.ENGLISH -> "Quick Conversion Table"
                AppLanguage.BENGALI -> "সমমান তালিকা"
                AppLanguage.HINDI -> "त्वरित रूपांतरण तालिका"
                AppLanguage.ARABIC -> "جدول التحويل السريع"
                AppLanguage.FRENCH -> "Tableau de conversion rapide"
                AppLanguage.SPANISH -> "Tabla de conversión rápida"
                AppLanguage.GERMAN -> "Schnellumrechnungstabelle"
                AppLanguage.CHINESE -> "快速转换表"
            }
            "select_language" -> when (language) {
                AppLanguage.ENGLISH -> "Select Language"
                AppLanguage.BENGALI -> "ভাষা নির্বাচন করুন"
                AppLanguage.HINDI -> "भाषा चुनें"
                AppLanguage.ARABIC -> "اختر اللغة"
                AppLanguage.FRENCH -> "Choisir la langue"
                AppLanguage.SPANISH -> "Seleccionar idioma"
                AppLanguage.GERMAN -> "Sprache auswählen"
                AppLanguage.CHINESE -> "选择语言"
            }
            "no_results" -> when (language) {
                AppLanguage.ENGLISH -> "No items found"
                AppLanguage.BENGALI -> "কোনো আইটেম পাওয়া যায়নি"
                AppLanguage.HINDI -> "कोई परिणाम नहीं मिला"
                AppLanguage.ARABIC -> "لم يتم العثور على نتائج"
                AppLanguage.FRENCH -> "Aucun élément trouvé"
                AppLanguage.SPANISH -> "No se encontraron elementos"
                AppLanguage.GERMAN -> "Keine Elemente gefunden"
                AppLanguage.CHINESE -> "未找到相关项"
            }
            else -> key
        }
    }
}
