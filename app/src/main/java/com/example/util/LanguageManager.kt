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
            "male" -> when (language) {
                AppLanguage.ENGLISH -> "Male"
                AppLanguage.BENGALI -> "পুরুষ"
                AppLanguage.HINDI -> "पुरुष"
                AppLanguage.ARABIC -> "ذكر"
                AppLanguage.FRENCH -> "Homme"
                AppLanguage.SPANISH -> "Hombre"
                AppLanguage.GERMAN -> "Mann"
                AppLanguage.CHINESE -> "男"
            }
            "female" -> when (language) {
                AppLanguage.ENGLISH -> "Female"
                AppLanguage.BENGALI -> "নারী"
                AppLanguage.HINDI -> "महिला"
                AppLanguage.ARABIC -> "أنثى"
                AppLanguage.FRENCH -> "Femme"
                AppLanguage.SPANISH -> "Mujer"
                AppLanguage.GERMAN -> "Frau"
                AppLanguage.CHINESE -> "女"
            }
            "age_years" -> when (language) {
                AppLanguage.ENGLISH -> "Age (Years)"
                AppLanguage.BENGALI -> "বয়স (বছর)"
                AppLanguage.HINDI -> "आयु (वर्ष)"
                AppLanguage.ARABIC -> "العمر (سنوات)"
                AppLanguage.FRENCH -> "Âge (Années)"
                AppLanguage.SPANISH -> "Edad (Años)"
                AppLanguage.GERMAN -> "Alter (Jahre)"
                AppLanguage.CHINESE -> "年龄（岁）"
            }
            "height_cm" -> when (language) {
                AppLanguage.ENGLISH -> "Height (cm)"
                AppLanguage.BENGALI -> "উচ্চতা (সেমি)"
                AppLanguage.HINDI -> "ऊंचाई (सेमी)"
                AppLanguage.ARABIC -> "الارتفاع (سم)"
                AppLanguage.FRENCH -> "Taille (cm)"
                AppLanguage.SPANISH -> "Altura (cm)"
                AppLanguage.GERMAN -> "Größe (cm)"
                AppLanguage.CHINESE -> "身高（厘米）"
            }
            "weight_kg" -> when (language) {
                AppLanguage.ENGLISH -> "Weight (kg)"
                AppLanguage.BENGALI -> "ওজন (কেজি)"
                AppLanguage.HINDI -> "वजन (किग्रा)"
                AppLanguage.ARABIC -> "الوزن (كجم)"
                AppLanguage.FRENCH -> "Poids (kg)"
                AppLanguage.SPANISH -> "Peso (kg)"
                AppLanguage.GERMAN -> "Gewicht (kg)"
                AppLanguage.CHINESE -> "体重（公斤）"
            }
            "height_ft" -> when (language) {
                AppLanguage.ENGLISH -> "Height (ft)"
                AppLanguage.BENGALI -> "উচ্চতা (ফুট)"
                AppLanguage.HINDI -> "ऊंचाई (फिट)"
                AppLanguage.ARABIC -> "الارتفاع (قدم)"
                AppLanguage.FRENCH -> "Taille (pieds)"
                AppLanguage.SPANISH -> "Altura (pies)"
                AppLanguage.GERMAN -> "Größe (Fuß)"
                AppLanguage.CHINESE -> "身高（英尺）"
            }
            "height_in" -> when (language) {
                AppLanguage.ENGLISH -> "Inches"
                AppLanguage.BENGALI -> "ইঞ্চি"
                AppLanguage.HINDI -> "इंच"
                AppLanguage.ARABIC -> "بوصة"
                AppLanguage.FRENCH -> "Pouces"
                AppLanguage.SPANISH -> "Pulgadas"
                AppLanguage.GERMAN -> "Zoll"
                AppLanguage.CHINESE -> "英寸"
            }
            "activity_level" -> when (language) {
                AppLanguage.ENGLISH -> "Daily Physical Activity Level:"
                AppLanguage.BENGALI -> "দৈনিক কায়িক পরিশ্রমের মাত্রা:"
                AppLanguage.HINDI -> "दैनिक शारीरिक गतिविधि का स्तर:"
                AppLanguage.ARABIC -> "مستوى النشاط البدني اليومي:"
                AppLanguage.FRENCH -> "Niveau d'activité physique quotidienne :"
                AppLanguage.SPANISH -> "Nivel de actividad física diaria:"
                AppLanguage.GERMAN -> "Tägliches körperliches Aktivitätsniveau:"
                AppLanguage.CHINESE -> "日常体力活动水平："
            }
            "activity_sedentary" -> when (language) {
                AppLanguage.ENGLISH -> "Sedentary / Desk Job (1.2)"
                AppLanguage.BENGALI -> "কম / বসে কাজ (১.২)"
                AppLanguage.HINDI -> "कम / डेस्क जॉब (1.2)"
                AppLanguage.ARABIC -> "خامل / عمل مكتبي (1.2)"
                AppLanguage.FRENCH -> "Sédentaire / Travail de bureau (1.2)"
                AppLanguage.SPANISH -> "Sedentario / Trabajo de escritorio (1.2)"
                AppLanguage.GERMAN -> "Sitzend / Bürojob (1.2)"
                AppLanguage.CHINESE -> "久坐 / 案头工作（1.2）"
            }
            "activity_light" -> when (language) {
                AppLanguage.ENGLISH -> "Light Exercise 1-3 Days (1.375)"
                AppLanguage.BENGALI -> "হালকা ব্যায়াম ১-৩ দিন (১.৩৭৫)"
                AppLanguage.HINDI -> "हल्का व्यायाम 1-3 दिन (1.375)"
                AppLanguage.ARABIC -> "تمارين خفيفة 1-3 أيام (1.375)"
                AppLanguage.FRENCH -> "Exercice léger 1-3 jours (1.375)"
                AppLanguage.SPANISH -> "Ejercicio ligero 1-3 días (1.375)"
                AppLanguage.GERMAN -> "Leichte Bewegung 1–3 Tage (1.375)"
                AppLanguage.CHINESE -> "每周轻度运动1-3天（1.375）"
            }
            "activity_moderate" -> when (language) {
                AppLanguage.ENGLISH -> "Moderate Exercise 3-5 Days (1.55)"
                AppLanguage.BENGALI -> "মাঝারি ব্যায়াম ৩-৫ দিন (১.৫৫)"
                AppLanguage.HINDI -> "मध्यम व्यायाम 3-5 दिन (1.55)"
                AppLanguage.ARABIC -> "تمارين معتدلة 3-5 أيام (1.55)"
                AppLanguage.FRENCH -> "Exercice modéré 3-5 jours (1.55)"
                AppLanguage.SPANISH -> "Ejercicio moderado 3-5 días (1.55)"
                AppLanguage.GERMAN -> "Mäßige Bewegung 3–5 Tage (1.55)"
                AppLanguage.CHINESE -> "每周中度运动3-5天（1.55）"
            }
            "activity_heavy" -> when (language) {
                AppLanguage.ENGLISH -> "Heavy Exercise / Hard Work (1.725)"
                AppLanguage.BENGALI -> "কঠোর পরিশ্রম / ব্যায়াম (১.৭২৫)"
                AppLanguage.HINDI -> "कड़ी मेहनत / व्यायाम (1.725)"
                AppLanguage.ARABIC -> "تمارين شاقة / عمل شاق (1.725)"
                AppLanguage.FRENCH -> "Exercice intense / Travail dur (1.725)"
                AppLanguage.SPANISH -> "Ejercicio intenso / Trabajo duro (1.725)"
                AppLanguage.GERMAN -> "Schwere Bewegung / Harte Arbeit (1.725)"
                AppLanguage.CHINESE -> "每周重度运动/体力劳动（1.725）"
            }
            "bmr_calc_title" -> when (language) {
                AppLanguage.ENGLISH -> "Calorie & BMR Calculator"
                AppLanguage.BENGALI -> "ক্যালোরি ও BMR হিসাব"
                AppLanguage.HINDI -> "कैलोरी और BMR कैलकुलेटर"
                AppLanguage.ARABIC -> "حاسبة السعرات الحرارية و BMR"
                AppLanguage.FRENCH -> "Calculateur de calories et BMR"
                AppLanguage.SPANISH -> "Calculadora de calorías y BMR"
                AppLanguage.GERMAN -> "Kalorien- und BMR-Rechner"
                AppLanguage.CHINESE -> "卡路里和基础代谢率计算器"
            }
            "bmr_result" -> when (language) {
                AppLanguage.ENGLISH -> "BMR (Basal Metabolic Rate):"
                AppLanguage.BENGALI -> "BMR (বেসাল মেটাবলিক রেট):"
                AppLanguage.HINDI -> "BMR (बेसल मेटाबॉलिक रेट):"
                AppLanguage.ARABIC -> "معدل الأيض الأساسي (BMR):"
                AppLanguage.FRENCH -> "BMR (Taux métabolique de base) :"
                AppLanguage.SPANISH -> "BMR (Tasa metabólica basal):"
                AppLanguage.GERMAN -> "BMR (Grundumsatz):"
                AppLanguage.CHINESE -> "BMR（基础代谢率）："
            }
            "tdee_maintain" -> when (language) {
                AppLanguage.ENGLISH -> "Calories to Maintain Weight:"
                AppLanguage.BENGALI -> "ওজন বজায় রাখতে প্রয়োজনীয় ক্যালোরি:"
                AppLanguage.HINDI -> "वजन बनाए रखने के लिए कैलोरी:"
                AppLanguage.ARABIC -> "السعرات الحرارية للحفاظ على الوزن:"
                AppLanguage.FRENCH -> "Calories pour maintenir le poids :"
                AppLanguage.SPANISH -> "Calorías para mantener el peso:"
                AppLanguage.GERMAN -> "Kalorien zum Halten des Gewichts:"
                AppLanguage.CHINESE -> "维持体重所需的卡路里："
            }
            "weight_loss_goal" -> when (language) {
                AppLanguage.ENGLISH -> "Weight Loss (-500 kcal/day):"
                AppLanguage.BENGALI -> "ওজন কমাতে (-৫০০ kcal/দিন):"
                AppLanguage.HINDI -> "वजन घटाने के लिए (-500 kcal/दिन):"
                AppLanguage.ARABIC -> "إنقاص الوزن (-500 سعرة/يوم):"
                AppLanguage.FRENCH -> "Perte de poids (-500 kcal/jour) :"
                AppLanguage.SPANISH -> "Pérdida de peso (-500 kcal/día):"
                AppLanguage.GERMAN -> "Gewichtsverlust (-500 kcal/Tag):"
                AppLanguage.CHINESE -> "减重（每日-500千卡）："
            }
            "weight_gain_goal" -> when (language) {
                AppLanguage.ENGLISH -> "Weight Gain (+500 kcal/day):"
                AppLanguage.BENGALI -> "ওজন বাড়াতে (+৫০০ kcal/দিন):"
                AppLanguage.HINDI -> "वजन बढ़ाने के लिए (+500 kcal/दिन):"
                AppLanguage.ARABIC -> "زيادة الوزن (+500 سعرة/يوم):"
                AppLanguage.FRENCH -> "Prise de poids (+500 kcal/jour) :"
                AppLanguage.SPANISH -> "Aumento de peso (+500 kcal/día):"
                AppLanguage.GERMAN -> "Gewichtszunahme (+500 kcal/Tag):"
                AppLanguage.CHINESE -> "增重（每日+500千卡）："
            }
            "ideal_weight_title" -> when (language) {
                AppLanguage.ENGLISH -> "Ideal Body Weight Calculator"
                AppLanguage.BENGALI -> "আদর্শ ওজন ক্যালকুলেটর"
                AppLanguage.HINDI -> "आदर्श वजन कैलकुलेटर"
                AppLanguage.ARABIC -> "حاسبة الوزن المثالي"
                AppLanguage.FRENCH -> "Calculateur de poids idéal"
                AppLanguage.SPANISH -> "Calculadora de peso ideal"
                AppLanguage.GERMAN -> "Idealgewicht-Rechner"
                AppLanguage.CHINESE -> "理想体重计算器"
            }
            "ideal_weight_devine" -> when (language) {
                AppLanguage.ENGLISH -> "Ideal Weight (Devine Formula):"
                AppLanguage.BENGALI -> "আদর্শ ওজন (Devine Formula):"
                AppLanguage.HINDI -> "आदर्श वजन (Devine):"
                AppLanguage.ARABIC -> "الوزن المثالي (معادلة ديفين):"
                AppLanguage.FRENCH -> "Poids idéal (Formule Devine) :"
                AppLanguage.SPANISH -> "Peso ideal (Fórmula Devine):"
                AppLanguage.GERMAN -> "Idealgewicht (Devine-Formel):"
                AppLanguage.CHINESE -> "理想体重（Devine公式）："
            }
            "healthy_bmi_range" -> when (language) {
                AppLanguage.ENGLISH -> "Healthy Weight Range (BMI 18.5-24.9):"
                AppLanguage.BENGALI -> "স্বাস্থ্যকর ওজনের সীমা (BMI ১৮.৫-২৪.৯):"
                AppLanguage.HINDI -> "स्वास्थ्यप्रद वजन सीमा (BMI 18.5-24.9):"
                AppLanguage.ARABIC -> "نطاق الوزن الصحي (BMI 18.5-24.9):"
                AppLanguage.FRENCH -> "Plage de poids santé (IMC 18,5-24,9) :"
                AppLanguage.SPANISH -> "Rango de peso saludable (IMC 18.5-24.9):"
                AppLanguage.GERMAN -> "Gesunder Gewichtsbereich (BMI 18,5–24,9):"
                AppLanguage.CHINESE -> "健康体重范围（BMI 18.5-24.9）："
            }
            "water_tracker_title" -> when (language) {
                AppLanguage.ENGLISH -> "Daily Water Intake Goal"
                AppLanguage.BENGALI -> "দৈনিক পানি খাওয়ার পরিমাণ"
                AppLanguage.HINDI -> "दैनिक पानी का लक्ष्य"
                AppLanguage.ARABIC -> "هدف شرب الماء اليومي"
                AppLanguage.FRENCH -> "Objectif quotidien en eau"
                AppLanguage.SPANISH -> "Meta diaria de consumo de agua"
                AppLanguage.GERMAN -> "Tägliches Wasserziel"
                AppLanguage.CHINESE -> "每日饮水目标"
            }
            "water_daily_goal" -> when (language) {
                AppLanguage.ENGLISH -> "Recommended Daily Water Intake:"
                AppLanguage.BENGALI -> "দৈনিক প্রয়োজনীয় পানি:"
                AppLanguage.HINDI -> "अनुशंसित दैनिक पानी:"
                AppLanguage.ARABIC -> "كمية الماء اليومية الموصى بها:"
                AppLanguage.FRENCH -> "Apport quotidien en eau recommandé :"
                AppLanguage.SPANISH -> "Consumo diario de agua recomendado:"
                AppLanguage.GERMAN -> "Empfohlene tägliche Wasseraufnahme:"
                AppLanguage.CHINESE -> "建议每日饮水量："
            }
            "water_glasses" -> when (language) {
                AppLanguage.ENGLISH -> "Glasses (250 ml each):"
                AppLanguage.BENGALI -> "গ্লাস (২৫০ মি.লি. করে):"
                AppLanguage.HINDI -> "ग्लास (250 मिली प्रत्येक):"
                AppLanguage.ARABIC -> "أكواب (250 مل لكل منها):"
                AppLanguage.FRENCH -> "Verres (250 ml chacun) :"
                AppLanguage.SPANISH -> "Vasos (250 ml c/u):"
                AppLanguage.GERMAN -> "Gläser (je 250 ml):"
                AppLanguage.CHINESE -> "杯数（每杯250毫升）："
            }
            "emi_calc_title" -> when (language) {
                AppLanguage.ENGLISH -> "EMI & Loan Calculator"
                AppLanguage.BENGALI -> "ইএমআই ও ঋণ ক্যালকুলেটর"
                AppLanguage.HINDI -> "ईएमआई और ऋण कैलकुलेटर"
                AppLanguage.ARABIC -> "حاسبة الأقساط والقروض"
                AppLanguage.FRENCH -> "Calculateur d'EMI et de prêt"
                AppLanguage.SPANISH -> "Calculadora de préstamos y cuotas"
                AppLanguage.GERMAN -> "EMI- und Kreditrechner"
                AppLanguage.CHINESE -> "等额本息贷款计算器"
            }
            "loan_amount" -> when (language) {
                AppLanguage.ENGLISH -> "Loan Amount"
                AppLanguage.BENGALI -> "ঋণের পরিমাণ"
                AppLanguage.HINDI -> "ऋण राशि"
                AppLanguage.ARABIC -> "مبلغ القرض"
                AppLanguage.FRENCH -> "Montant du prêt"
                AppLanguage.SPANISH -> "Monto del préstamo"
                AppLanguage.GERMAN -> "Kreditbetrag"
                AppLanguage.CHINESE -> "贷款金额"
            }
            "interest_rate" -> when (language) {
                AppLanguage.ENGLISH -> "Annual Interest Rate (%)"
                AppLanguage.BENGALI -> "বার্ষিক সুদের হার (%)"
                AppLanguage.HINDI -> "वार्षिक ब्याज दर (%)"
                AppLanguage.ARABIC -> "نسبة الفائدة السنوية (%)"
                AppLanguage.FRENCH -> "Taux d'intérêt annuel (%)"
                AppLanguage.SPANISH -> "Tasa de interés anual (%)"
                AppLanguage.GERMAN -> "Jährlicher Zinssatz (%)"
                AppLanguage.CHINESE -> "年利率（%）"
            }
            "tenure_months" -> when (language) {
                AppLanguage.ENGLISH -> "Tenure (Months)"
                AppLanguage.BENGALI -> "মেয়াদ (মাস)"
                AppLanguage.HINDI -> "अवधि (महीने)"
                AppLanguage.ARABIC -> "مدة القرض (أشهر)"
                AppLanguage.FRENCH -> "Durée (mois)"
                AppLanguage.SPANISH -> "Plazo (meses)"
                AppLanguage.GERMAN -> "Laufzeit (Monate)"
                AppLanguage.CHINESE -> "期限（个月）"
            }
            "monthly_emi" -> when (language) {
                AppLanguage.ENGLISH -> "Monthly EMI:"
                AppLanguage.BENGALI -> "মাসিক ইএমআই (EMI):"
                AppLanguage.HINDI -> "मासिक ईएमआई:"
                AppLanguage.ARABIC -> "القسط الشهري:"
                AppLanguage.FRENCH -> "Mensualité (EMI) :"
                AppLanguage.SPANISH -> "Cuota mensual:"
                AppLanguage.GERMAN -> "Monatliche Rate:"
                AppLanguage.CHINESE -> "每月供款："
            }
            "total_interest" -> when (language) {
                AppLanguage.ENGLISH -> "Total Interest:"
                AppLanguage.BENGALI -> "মোট সুদ:"
                AppLanguage.HINDI -> "कुल ब्याज:"
                AppLanguage.ARABIC -> "إجمالي الفائدة:"
                AppLanguage.FRENCH -> "Intérêt total :"
                AppLanguage.SPANISH -> "Interés total:"
                AppLanguage.GERMAN -> "Gesamtzinsen:"
                AppLanguage.CHINESE -> "总利息："
            }
            "total_payable" -> when (language) {
                AppLanguage.ENGLISH -> "Total Payable Amount:"
                AppLanguage.BENGALI -> "মোট পরিশোধযোগ্য:"
                AppLanguage.HINDI -> "कुल देय राशि:"
                AppLanguage.ARABIC -> "المبلغ الإجمالي المستحق:"
                AppLanguage.FRENCH -> "Montant total à payer :"
                AppLanguage.SPANISH -> "Monto total a pagar:"
                AppLanguage.GERMAN -> "Gesamtbetrag:"
                AppLanguage.CHINESE -> "应还总额："
            }
            "discount_calc_title" -> when (language) {
                AppLanguage.ENGLISH -> "Discount Calculator"
                AppLanguage.BENGALI -> "ডিসকাউন্ট ক্যালকুলেটর"
                AppLanguage.HINDI -> "छूट कैलकुलेटर"
                AppLanguage.ARABIC -> "حاسبة الخصم"
                AppLanguage.FRENCH -> "Calculateur de remise"
                AppLanguage.SPANISH -> "Calculadora de descuentos"
                AppLanguage.GERMAN -> "Rabattrechner"
                AppLanguage.CHINESE -> "折扣计算器"
            }
            "original_price" -> when (language) {
                AppLanguage.ENGLISH -> "Original Price"
                AppLanguage.BENGALI -> "আসল দাম"
                AppLanguage.HINDI -> "मूल मूल्य"
                AppLanguage.ARABIC -> "السعر الأصلي"
                AppLanguage.FRENCH -> "Prix d'origine"
                AppLanguage.SPANISH -> "Precio original"
                AppLanguage.GERMAN -> "Originalpreis"
                AppLanguage.CHINESE -> "原价"
            }
            "discount_percent" -> when (language) {
                AppLanguage.ENGLISH -> "Discount (%)"
                AppLanguage.BENGALI -> "ডিসকাউন্ট (%)"
                AppLanguage.HINDI -> "छूट (%)"
                AppLanguage.ARABIC -> "الخصم (%)"
                AppLanguage.FRENCH -> "Remise (%)"
                AppLanguage.SPANISH -> "Descuento (%)"
                AppLanguage.GERMAN -> "Rabatt (%)"
                AppLanguage.CHINESE -> "折扣（%）"
            }
            "tax_percent" -> when (language) {
                AppLanguage.ENGLISH -> "Tax (%)"
                AppLanguage.BENGALI -> "ট্যাক্স (%)"
                AppLanguage.HINDI -> "कर (%)"
                AppLanguage.ARABIC -> "الضريبة (%)"
                AppLanguage.FRENCH -> "Taxe (%)"
                AppLanguage.SPANISH -> "Impuesto (%)"
                AppLanguage.GERMAN -> "Steuer (%)"
                AppLanguage.CHINESE -> "税率（%）"
            }
            "you_save" -> when (language) {
                AppLanguage.ENGLISH -> "You Save:"
                AppLanguage.BENGALI -> "আপনার সাশ্রয়:"
                AppLanguage.HINDI -> "आपकी बचत:"
                AppLanguage.ARABIC -> "مقدار التوفير:"
                AppLanguage.FRENCH -> "Vous économisez :"
                AppLanguage.SPANISH -> "Ahorras:"
                AppLanguage.GERMAN -> "Sie sparen:"
                AppLanguage.CHINESE -> "您节省："
            }
            "final_price" -> when (language) {
                AppLanguage.ENGLISH -> "Final Price:"
                AppLanguage.BENGALI -> "ডিসকাউন্টের পর দাম:"
                AppLanguage.HINDI -> "अंतिम मूल्य:"
                AppLanguage.ARABIC -> "السعر النهائي:"
                AppLanguage.FRENCH -> "Prix final :"
                AppLanguage.SPANISH -> "Precio final:"
                AppLanguage.GERMAN -> "Endpreis:"
                AppLanguage.CHINESE -> "最终价格："
            }
            "profit_loss_title" -> when (language) {
                AppLanguage.ENGLISH -> "Profit & Loss Margin"
                AppLanguage.BENGALI -> "লাভ ও ক্ষতি মার্জিন"
                AppLanguage.HINDI -> "लाभ और हानि मार्जिन"
                AppLanguage.ARABIC -> "هامش الربح والخسارة"
                AppLanguage.FRENCH -> "Marge de profit et perte"
                AppLanguage.SPANISH -> "Margen de ganancia y pérdida"
                AppLanguage.GERMAN -> "Gewinn- und Verlustmarge"
                AppLanguage.CHINESE -> "盈亏利润率"
            }
            "cost_price" -> when (language) {
                AppLanguage.ENGLISH -> "Cost Price"
                AppLanguage.BENGALI -> "ক্রয়মূল্য"
                AppLanguage.HINDI -> "क्रय मूल्य"
                AppLanguage.ARABIC -> "سعر التكلفة"
                AppLanguage.FRENCH -> "Prix d'achat"
                AppLanguage.SPANISH -> "Precio de costo"
                AppLanguage.GERMAN -> "Einkaufspreis"
                AppLanguage.CHINESE -> "成本价"
            }
            "selling_price" -> when (language) {
                AppLanguage.ENGLISH -> "Selling Price"
                AppLanguage.BENGALI -> "বিক্রয়মূল্য"
                AppLanguage.HINDI -> "विक्रय मूल्य"
                AppLanguage.ARABIC -> "سعر البيع"
                AppLanguage.FRENCH -> "Prix de vente"
                AppLanguage.SPANISH -> "Precio de venta"
                AppLanguage.GERMAN -> "Verkaufspreis"
                AppLanguage.CHINESE -> "售价"
            }
            "profit_or_loss" -> when (language) {
                AppLanguage.ENGLISH -> "Profit / Loss Amount:"
                AppLanguage.BENGALI -> "লাভ / ক্ষতির পরিমাণ:"
                AppLanguage.HINDI -> "लाभ / हानि राशि:"
                AppLanguage.ARABIC -> "مبلغ الربح / الخسارة:"
                AppLanguage.FRENCH -> "Montant du profit / perte :"
                AppLanguage.SPANISH -> "Monto de ganancia / pérdida:"
                AppLanguage.GERMAN -> "Gewinn-/Verlustbetrag:"
                AppLanguage.CHINESE -> "盈亏金额："
            }
            "margin_percent" -> when (language) {
                AppLanguage.ENGLISH -> "Margin (%):"
                AppLanguage.BENGALI -> "মার্জিন (%):"
                AppLanguage.HINDI -> "मार्जि‍न (%):"
                AppLanguage.ARABIC -> "النسبة المئوية (%):"
                AppLanguage.FRENCH -> "Marge (%) :"
                AppLanguage.SPANISH -> "Margen (%):"
                AppLanguage.GERMAN -> "Marge (%):"
                AppLanguage.CHINESE -> "利润率（%）："
            }
            "vat_tax_title" -> when (language) {
                AppLanguage.ENGLISH -> "VAT & Tax Calculator"
                AppLanguage.BENGALI -> "ভ্যাট ও ট্যাক্স ক্যালকুলেটর"
                AppLanguage.HINDI -> "वैट और टैक्स कैलकुलेटर"
                AppLanguage.ARABIC -> "حاسبة ضريبة القيمة المضافة"
                AppLanguage.FRENCH -> "Calculateur de TVA et taxe"
                AppLanguage.SPANISH -> "Calculadora de IVA y tributos"
                AppLanguage.GERMAN -> "MwSt.- und Steuerrechner"
                AppLanguage.CHINESE -> "增值税和税率计算器"
            }
            "net_amount" -> when (language) {
                AppLanguage.ENGLISH -> "Amount"
                AppLanguage.BENGALI -> "পরিমাণ"
                AppLanguage.HINDI -> "राशि"
                AppLanguage.ARABIC -> "المبلغ"
                AppLanguage.FRENCH -> "Montant"
                AppLanguage.SPANISH -> "Monto"
                AppLanguage.GERMAN -> "Betrag"
                AppLanguage.CHINESE -> "金额"
            }
            "vat_rate" -> when (language) {
                AppLanguage.ENGLISH -> "VAT / Tax Rate (%)"
                AppLanguage.BENGALI -> "ভ্যাট / ট্যাক্স হার (%)"
                AppLanguage.HINDI -> "वैट / टैक्स दर (%)"
                AppLanguage.ARABIC -> "نسبة الضريبة (%)"
                AppLanguage.FRENCH -> "Taux de TVA (%)"
                AppLanguage.SPANISH -> "Tasa de IVA (%)"
                AppLanguage.GERMAN -> "MwSt.-Satz (%)"
                AppLanguage.CHINESE -> "增值税率（%）"
            }
            "vat_amount" -> when (language) {
                AppLanguage.ENGLISH -> "VAT Amount:"
                AppLanguage.BENGALI -> "ভ্যাটের পরিমাণ:"
                AppLanguage.HINDI -> "वैट राशि:"
                AppLanguage.ARABIC -> "مبلغ الضريبة:"
                AppLanguage.FRENCH -> "Montant de la TVA :"
                AppLanguage.SPANISH -> "Monto de IVA:"
                AppLanguage.GERMAN -> "MwSt.-Betrag:"
                AppLanguage.CHINESE -> "税额："
            }
            "gross_amount" -> when (language) {
                AppLanguage.ENGLISH -> "Total Price (with VAT):"
                AppLanguage.BENGALI -> "ভ্যাটসহ মোট দাম:"
                AppLanguage.HINDI -> "कुल मूल्य (वैट सहित):"
                AppLanguage.ARABIC -> "السعر الإجمالي (شامل الضريبة):"
                AppLanguage.FRENCH -> "Prix total (TTC) :"
                AppLanguage.SPANISH -> "Precio total (con IVA):"
                AppLanguage.GERMAN -> "Gesamtpreis (inkl. MwSt.):"
                AppLanguage.CHINESE -> "含税总价："
            }
            "interest_calc_title" -> when (language) {
                AppLanguage.ENGLISH -> "Interest Calculator"
                AppLanguage.BENGALI -> "সুদ ক্যালকুলেটর"
                AppLanguage.HINDI -> "ब्याज कैलकुलेटर"
                AppLanguage.ARABIC -> "حاسبة الفوائد"
                AppLanguage.FRENCH -> "Calculateur d'intérêt"
                AppLanguage.SPANISH -> "Calculadora de interés"
                AppLanguage.GERMAN -> "Zinsrechner"
                AppLanguage.CHINESE -> "利息计算器"
            }
            "principal_amount" -> when (language) {
                AppLanguage.ENGLISH -> "Principal Amount"
                AppLanguage.BENGALI -> "আসল টাকা"
                AppLanguage.HINDI -> "मूलधन राशि"
                AppLanguage.ARABIC -> "المبلغ الأصلي"
                AppLanguage.FRENCH -> "Capital initial"
                AppLanguage.SPANISH -> "Monto principal"
                AppLanguage.GERMAN -> "Kapital"
                AppLanguage.CHINESE -> "本金"
            }
            "simple_interest" -> when (language) {
                AppLanguage.ENGLISH -> "Simple Interest:"
                AppLanguage.BENGALI -> "সরল সুদ:"
                AppLanguage.HINDI -> "साधारण ब्याज:"
                AppLanguage.ARABIC -> "الفائدة البسيطة:"
                AppLanguage.FRENCH -> "Intérêt simple :"
                AppLanguage.SPANISH -> "Interés simple:"
                AppLanguage.GERMAN -> "Einfache Zinsen:"
                AppLanguage.CHINESE -> "单利："
            }
            "compound_interest" -> when (language) {
                AppLanguage.ENGLISH -> "Compound Interest:"
                AppLanguage.BENGALI -> "চক্রবৃদ্ধি সুদ:"
                AppLanguage.HINDI -> "चक्रवृद्धि ब्याज:"
                AppLanguage.ARABIC -> "الفائدة المركبة:"
                AppLanguage.FRENCH -> "Intérêt composé :"
                AppLanguage.SPANISH -> "Interés compuesto:"
                AppLanguage.GERMAN -> "Zinseszins:"
                AppLanguage.CHINESE -> "复利："
            }
            "age_calc_title" -> when (language) {
                AppLanguage.ENGLISH -> "Age & Birthday Calculator"
                AppLanguage.BENGALI -> "বয়স ও জন্মদিন ক্যালকুলেটর"
                AppLanguage.HINDI -> "आयु और जन्मदिन कैलकुलेटर"
                AppLanguage.ARABIC -> "حاسبة العمر وعيد الميلاد"
                AppLanguage.FRENCH -> "Calculateur d'âge et d'anniversaire"
                AppLanguage.SPANISH -> "Calculadora de edad y cumpleaños"
                AppLanguage.GERMAN -> "Alters- und Geburtstagsrechner"
                AppLanguage.CHINESE -> "年龄和生日计算器"
            }
            "date_of_birth" -> when (language) {
                AppLanguage.ENGLISH -> "Date of Birth"
                AppLanguage.BENGALI -> "জন্মতারিখ"
                AppLanguage.HINDI -> "जन्म तिथि"
                AppLanguage.ARABIC -> "تاريخ الميلاد"
                AppLanguage.FRENCH -> "Date de naissance"
                AppLanguage.SPANISH -> "Fecha de nacimiento"
                AppLanguage.GERMAN -> "Geburtsdatum"
                AppLanguage.CHINESE -> "出生日期"
            }
            "years" -> when (language) {
                AppLanguage.ENGLISH -> "Years"
                AppLanguage.BENGALI -> "বছর"
                AppLanguage.HINDI -> "वर्ष"
                AppLanguage.ARABIC -> "سنوات"
                AppLanguage.FRENCH -> "Ans"
                AppLanguage.SPANISH -> "Años"
                AppLanguage.GERMAN -> "Jahre"
                AppLanguage.CHINESE -> "岁"
            }
            "months" -> when (language) {
                AppLanguage.ENGLISH -> "Months"
                AppLanguage.BENGALI -> "মাস"
                AppLanguage.HINDI -> "महीने"
                AppLanguage.ARABIC -> "أشهر"
                AppLanguage.FRENCH -> "Mois"
                AppLanguage.SPANISH -> "Meses"
                AppLanguage.GERMAN -> "Monate"
                AppLanguage.CHINESE -> "月"
            }
            "days" -> when (language) {
                AppLanguage.ENGLISH -> "Days"
                AppLanguage.BENGALI -> "দিন"
                AppLanguage.HINDI -> "दिन"
                AppLanguage.ARABIC -> "أيام"
                AppLanguage.FRENCH -> "Jours"
                AppLanguage.SPANISH -> "Días"
                AppLanguage.GERMAN -> "Tage"
                AppLanguage.CHINESE -> "天"
            }
            "next_birthday" -> when (language) {
                AppLanguage.ENGLISH -> "Next Birthday:"
                AppLanguage.BENGALI -> "পরবর্তী জন্মদিন:"
                AppLanguage.HINDI -> "अगला जन्मदिन:"
                AppLanguage.ARABIC -> "عيد الميلاد القادم:"
                AppLanguage.FRENCH -> "Prochain anniversaire :"
                AppLanguage.SPANISH -> "Próximo cumpleaños:"
                AppLanguage.GERMAN -> "Nächster Geburtstag:"
                AppLanguage.CHINESE -> "距离下个生日："
            }
            "date_diff_title" -> when (language) {
                AppLanguage.ENGLISH -> "Date Difference"
                AppLanguage.BENGALI -> "তারিখের ব্যবধান"
                AppLanguage.HINDI -> "तारीख का अंतर"
                AppLanguage.ARABIC -> "الفرق بين التاريخين"
                AppLanguage.FRENCH -> "Différence de dates"
                AppLanguage.SPANISH -> "Diferencia de fechas"
                AppLanguage.GERMAN -> "Datumsdifferenz"
                AppLanguage.CHINESE -> "日期差计算"
            }
            "start_date" -> when (language) {
                AppLanguage.ENGLISH -> "Start Date"
                AppLanguage.BENGALI -> "শুরুর তারিখ"
                AppLanguage.HINDI -> "प्रारंभ तिथि"
                AppLanguage.ARABIC -> "تاريخ البدء"
                AppLanguage.FRENCH -> "Date de début"
                AppLanguage.SPANISH -> "Fecha de inicio"
                AppLanguage.GERMAN -> "Startdatum"
                AppLanguage.CHINESE -> "开始日期"
            }
            "end_date" -> when (language) {
                AppLanguage.ENGLISH -> "End Date"
                AppLanguage.BENGALI -> "শেষের তারিখ"
                AppLanguage.HINDI -> "अंतिम तिथि"
                AppLanguage.ARABIC -> "تاريخ الانتهاء"
                AppLanguage.FRENCH -> "Date de fin"
                AppLanguage.SPANISH -> "Fecha final"
                AppLanguage.GERMAN -> "Enddatum"
                AppLanguage.CHINESE -> "结束日期"
            }
            "total_days" -> when (language) {
                AppLanguage.ENGLISH -> "Total Duration:"
                AppLanguage.BENGALI -> "মোট ব্যবধান:"
                AppLanguage.HINDI -> "कुल अवधि:"
                AppLanguage.ARABIC -> "المدة الإجمالية:"
                AppLanguage.FRENCH -> "Durée totale :"
                AppLanguage.SPANISH -> "Duración total:"
                AppLanguage.GERMAN -> "Gesamtdauer:"
                AppLanguage.CHINESE -> "总天数："
            }
            "percentage_calc_title" -> when (language) {
                AppLanguage.ENGLISH -> "Percentage Calculator"
                AppLanguage.BENGALI -> "শতকরা ক্যালকুলেটর"
                AppLanguage.HINDI -> "प्रतिशत कैलकुलेटर"
                AppLanguage.ARABIC -> "حاسبة النسبة المئوية"
                AppLanguage.FRENCH -> "Calculateur de pourcentage"
                AppLanguage.SPANISH -> "Calculadora de porcentaje"
                AppLanguage.GERMAN -> "Prozentrechner"
                AppLanguage.CHINESE -> "百分比计算器"
            }
            "percent_of" -> when (language) {
                AppLanguage.ENGLISH -> "% of"
                AppLanguage.BENGALI -> "এর %"
                AppLanguage.HINDI -> "का %"
                AppLanguage.ARABIC -> "% من"
                AppLanguage.FRENCH -> "% de"
                AppLanguage.SPANISH -> "% de"
                AppLanguage.GERMAN -> "% von"
                AppLanguage.CHINESE -> "% 的"
            }
            "value_text" -> when (language) {
                AppLanguage.ENGLISH -> "Value"
                AppLanguage.BENGALI -> "মান"
                AppLanguage.HINDI -> "मान"
                AppLanguage.ARABIC -> "القيمة"
                AppLanguage.FRENCH -> "Valeur"
                AppLanguage.SPANISH -> "Valor"
                AppLanguage.GERMAN -> "Wert"
                AppLanguage.CHINESE -> "数值"
            }
            "percentage_result" -> when (language) {
                AppLanguage.ENGLISH -> "Percentage Result:"
                AppLanguage.BENGALI -> "শতকরা ফলাফল:"
                AppLanguage.HINDI -> "प्रतिशत परिणाम:"
                AppLanguage.ARABIC -> "نتيجة النسبة المئوية:"
                AppLanguage.FRENCH -> "Résultat du pourcentage :"
                AppLanguage.SPANISH -> "Resultado del porcentaje:"
                AppLanguage.GERMAN -> "Prozentuales Ergebnis:"
                AppLanguage.CHINESE -> "计算结果："
            }
            "tip_calc_title" -> when (language) {
                AppLanguage.ENGLISH -> "Tip & Bill Splitter"
                AppLanguage.BENGALI -> "টিপ ও বিল ভাগ"
                AppLanguage.HINDI -> "टिप और बिल शेयर"
                AppLanguage.ARABIC -> "حاسبة الإكرامية وقتسام الفاتورة"
                AppLanguage.FRENCH -> "Calculateur de pourboire et de note"
                AppLanguage.SPANISH -> "Calculadora de propinas y cuenta"
                AppLanguage.GERMAN -> "Trinkgeld- und Rechnungsrechner"
                AppLanguage.CHINESE -> "小费和账单 AA 计算器"
            }
            "bill_amount" -> when (language) {
                AppLanguage.ENGLISH -> "Total Bill Amount"
                AppLanguage.BENGALI -> "মোট বিলের পরিমাণ"
                AppLanguage.HINDI -> "कुल बिल राशि"
                AppLanguage.ARABIC -> "مبلغ الفاتورة الإجمالي"
                AppLanguage.FRENCH -> "Montant total de la facture"
                AppLanguage.SPANISH -> "Monto total de la cuenta"
                AppLanguage.GERMAN -> "Gesamtrechnungsbetrag"
                AppLanguage.CHINESE -> "账单总额"
            }
            "tip_percent" -> when (language) {
                AppLanguage.ENGLISH -> "Tip (%)"
                AppLanguage.BENGALI -> "টিপ (%)"
                AppLanguage.HINDI -> "टिप (%)"
                AppLanguage.ARABIC -> "الإكرامية (%)"
                AppLanguage.FRENCH -> "Pourboire (%)"
                AppLanguage.SPANISH -> "Propina (%)"
                AppLanguage.GERMAN -> "Trinkgeld (%)"
                AppLanguage.CHINESE -> "小费（%）"
            }
            "number_of_people" -> when (language) {
                AppLanguage.ENGLISH -> "Number of People"
                AppLanguage.BENGALI -> "মানুষের সংখ্যা"
                AppLanguage.HINDI -> "लोगों की संख्या"
                AppLanguage.ARABIC -> "عدد الأشخاص"
                AppLanguage.FRENCH -> "Nombre de personnes"
                AppLanguage.SPANISH -> "Número de personas"
                AppLanguage.GERMAN -> "Anzahl der Personen"
                AppLanguage.CHINESE -> "用餐人数"
            }
            "tip_per_person" -> when (language) {
                AppLanguage.ENGLISH -> "Tip Per Person:"
                AppLanguage.BENGALI -> "প্রতিজনের টিপ:"
                AppLanguage.HINDI -> "प्रति व्यक्ति टिप:"
                AppLanguage.ARABIC -> "الإكرامية لكل شخص:"
                AppLanguage.FRENCH -> "Pourboire par personne :"
                AppLanguage.SPANISH -> "Propina por persona:"
                AppLanguage.GERMAN -> "Trinkgeld pro Person:"
                AppLanguage.CHINESE -> "人均小费："
            }
            "total_per_person" -> when (language) {
                AppLanguage.ENGLISH -> "Total Per Person:"
                AppLanguage.BENGALI -> "প্রতিজনের মোট বিল:"
                AppLanguage.HINDI -> "प्रति व्यक्ति कुल:"
                AppLanguage.ARABIC -> "المجموع لكل شخص:"
                AppLanguage.FRENCH -> "Total par personne :"
                AppLanguage.SPANISH -> "Total por persona:"
                AppLanguage.GERMAN -> "Gesamt pro Person:"
                AppLanguage.CHINESE -> "人均总付："
            }
            "electricity_calc_title" -> when (language) {
                AppLanguage.ENGLISH -> "Electricity Bill Calculator"
                AppLanguage.BENGALI -> "বিদ্যুৎ বিল ক্যালকুলেটর"
                AppLanguage.HINDI -> "बिजली बिल कैलकुलेटर"
                AppLanguage.ARABIC -> "حاسبة فاتورة الكهرباء"
                AppLanguage.FRENCH -> "Calculateur de facture d'électricité"
                AppLanguage.SPANISH -> "Calculadora de factura de luz"
                AppLanguage.GERMAN -> "Stromrechnungsrechner"
                AppLanguage.CHINESE -> "电费计算器"
            }
            "units_consumed" -> when (language) {
                AppLanguage.ENGLISH -> "Units Consumed (kWh)"
                AppLanguage.BENGALI -> "ব্যবহৃত ইউনিট (kWh)"
                AppLanguage.HINDI -> "खपत की गई इकाइयां (kWh)"
                AppLanguage.ARABIC -> "الوحدات المستهلكة (كيلوواط ساعة)"
                AppLanguage.FRENCH -> "Unités consommées (kWh)"
                AppLanguage.SPANISH -> "Unidades consumidas (kWh)"
                AppLanguage.GERMAN -> "Verbrauchte Einheiten (kWh)"
                AppLanguage.CHINESE -> "用电度数（kWh）"
            }
            "rate_per_unit" -> when (language) {
                AppLanguage.ENGLISH -> "Rate Per Unit"
                AppLanguage.BENGALI -> "প্রতি ইউনিটের চার্জ"
                AppLanguage.HINDI -> "प्रति इकाई दर"
                AppLanguage.ARABIC -> "سعر الوحدة"
                AppLanguage.FRENCH -> "Tarif par unité"
                AppLanguage.SPANISH -> "Tarifa por unidad"
                AppLanguage.GERMAN -> "Preis pro Einheit"
                AppLanguage.CHINESE -> "每度单价"
            }
            "estimated_bill" -> when (language) {
                AppLanguage.ENGLISH -> "Estimated Monthly Bill:"
                AppLanguage.BENGALI -> "আনুমানিক মাসিক বিল:"
                AppLanguage.HINDI -> "अनुमानित मासिक बिल:"
                AppLanguage.ARABIC -> "الفاتورة الشهرية المقدرة:"
                AppLanguage.FRENCH -> "Facture mensuelle estimée :"
                AppLanguage.SPANISH -> "Factura mensual estimada:"
                AppLanguage.GERMAN -> "Geschätzte monatliche Rechnung:"
                AppLanguage.CHINESE -> "预估每月电费："
            }
            "appliance_cost_title" -> when (language) {
                AppLanguage.ENGLISH -> "Appliance Power Cost"
                AppLanguage.BENGALI -> "সরঞ্জামের বিদ্যুৎ খরচ"
                AppLanguage.HINDI -> "उपकरण ऊर्जा लागत"
                AppLanguage.ARABIC -> "تكلفة طاقة الأجهزة"
                AppLanguage.FRENCH -> "Coût énergétique des appareils"
                AppLanguage.SPANISH -> "Costo de energía por electrodoméstico"
                AppLanguage.GERMAN -> "Gerätestromkosten"
                AppLanguage.CHINESE -> "电器耗电成本"
            }
            "power_watts" -> when (language) {
                AppLanguage.ENGLISH -> "Power Rating (Watts)"
                AppLanguage.BENGALI -> "ক্ষমতা (ওয়াট / Watts)"
                AppLanguage.HINDI -> "पावर रेटिंग (वाट)"
                AppLanguage.ARABIC -> "قدرة الجهاز (واط)"
                AppLanguage.FRENCH -> "Puissance nominale (Watts)"
                AppLanguage.SPANISH -> "Potencia (Vatios)"
                AppLanguage.GERMAN -> "Leistung (Watt)"
                AppLanguage.CHINESE -> "功率（瓦特）"
            }
            "daily_hours" -> when (language) {
                AppLanguage.ENGLISH -> "Daily Usage (Hours)"
                AppLanguage.BENGALI -> "দৈনিক ব্যবহারের সময় (ঘণ্টা)"
                AppLanguage.HINDI -> "दैनिक उपयोग (घंटे)"
                AppLanguage.ARABIC -> "الاستخدام اليومي (ساعات)"
                AppLanguage.FRENCH -> "Utilisation quotidienne (Heures)"
                AppLanguage.SPANISH -> "Uso diario (Horas)"
                AppLanguage.GERMAN -> "Tägliche Nutzung (Stunden)"
                AppLanguage.CHINESE -> "每日使用时长（小时）"
            }
            "daily_cost" -> when (language) {
                AppLanguage.ENGLISH -> "Daily Cost:"
                AppLanguage.BENGALI -> "দৈনিক খরচ:"
                AppLanguage.HINDI -> "दैनिक लागत:"
                AppLanguage.ARABIC -> "التكلفة اليومية:"
                AppLanguage.FRENCH -> "Coût quotidien :"
                AppLanguage.SPANISH -> "Costo diario:"
                AppLanguage.GERMAN -> "Tägliche Kosten:"
                AppLanguage.CHINESE -> "每日花费："
            }
            "monthly_cost" -> when (language) {
                AppLanguage.ENGLISH -> "Monthly Cost:"
                AppLanguage.BENGALI -> "মাসিক খরচ:"
                AppLanguage.HINDI -> "मासिक लागत:"
                AppLanguage.ARABIC -> "التكلفة الشهرية:"
                AppLanguage.FRENCH -> "Coût mensuel :"
                AppLanguage.SPANISH -> "Costo mensual:"
                AppLanguage.GERMAN -> "Monatliche Kosten:"
                AppLanguage.CHINESE -> "每月花费："
            }
            "battery_backup_title" -> when (language) {
                AppLanguage.ENGLISH -> "Battery / IPS Backup Time"
                AppLanguage.BENGALI -> "ব্যাটারি / আইপিএস ব্যাকআপ সময়"
                AppLanguage.HINDI -> "बैटरी / आईपीएस बैकअप समय"
                AppLanguage.ARABIC -> "وقت النسخ الاحتياطي للبطارية"
                AppLanguage.FRENCH -> "Temps de secours batterie / IPS"
                AppLanguage.SPANISH -> "Tiempo de respaldo de batería"
                AppLanguage.GERMAN -> "Batterie-/IPS-Laufzeit"
                AppLanguage.CHINESE -> "电池/IPS备用时间"
            }
            "battery_ah" -> when (language) {
                AppLanguage.ENGLISH -> "Battery Capacity (Ah)"
                AppLanguage.BENGALI -> "ব্যাটারি ক্ষমতা (Ah)"
                AppLanguage.HINDI -> "बैटरी क्षमता (Ah)"
                AppLanguage.ARABIC -> "سعة البطارية (أمبير ساعة)"
                AppLanguage.FRENCH -> "Capacité de batterie (Ah)"
                AppLanguage.SPANISH -> "Capacidad de batería (Ah)"
                AppLanguage.GERMAN -> "Batteriekapazität (Ah)"
                AppLanguage.CHINESE -> "电池容量（安时）"
            }
            "battery_volts" -> when (language) {
                AppLanguage.ENGLISH -> "Voltage (V)"
                AppLanguage.BENGALI -> "ভোল্টেজ (V)"
                AppLanguage.HINDI -> "वोल्टेज (V)"
                AppLanguage.ARABIC -> "الجهد (فولت)"
                AppLanguage.FRENCH -> "Tension (V)"
                AppLanguage.SPANISH -> "Voltaje (V)"
                AppLanguage.GERMAN -> "Spannung (V)"
                AppLanguage.CHINESE -> "电压（伏特）"
            }
            "load_watts" -> when (language) {
                AppLanguage.ENGLISH -> "Total Load Power (Watts)"
                AppLanguage.BENGALI -> "মোট লোড (Watts)"
                AppLanguage.HINDI -> "कुल लोड पावर (वाट)"
                AppLanguage.ARABIC -> "إجمالي طاقة الحمل (واط)"
                AppLanguage.FRENCH -> "Puissance totale de charge (Watts)"
                AppLanguage.SPANISH -> "Carga total (Vatios)"
                AppLanguage.GERMAN -> "Gesamtlast (Watt)"
                AppLanguage.CHINESE -> "总负载功率（瓦特）"
            }
            "estimated_backup" -> when (language) {
                AppLanguage.ENGLISH -> "Estimated Backup Time:"
                AppLanguage.BENGALI -> "আনুমানিক ব্যাকআপ সময়:"
                AppLanguage.HINDI -> "अनुमानित बैकअप समय:"
                AppLanguage.ARABIC -> "وقت التشغيل المقدر:"
                AppLanguage.FRENCH -> "Temps de secours estimé :"
                AppLanguage.SPANISH -> "Tiempo de respaldo estimado:"
                AppLanguage.GERMAN -> "Geschätzte Überbrückungszeit:"
                AppLanguage.CHINESE -> "预计续航时间："
            }
            "fuel_cost_title" -> when (language) {
                AppLanguage.ENGLISH -> "Fuel Cost Calculator"
                AppLanguage.BENGALI -> "জ্বালানি খরচ ক্যালকুলেটর"
                AppLanguage.HINDI -> "ईंधन लागत कैलकुलेटर"
                AppLanguage.ARABIC -> "حاسبة تكلفة الوقود"
                AppLanguage.FRENCH -> "Calculateur de coût de carburant"
                AppLanguage.SPANISH -> "Calculadora de costo de combustible"
                AppLanguage.GERMAN -> "Treibstoffkostenrechner"
                AppLanguage.CHINESE -> "燃油成本计算器"
            }
            "distance_km" -> when (language) {
                AppLanguage.ENGLISH -> "Distance (km)"
                AppLanguage.BENGALI -> "দূরত্ব (কি.মি.)"
                AppLanguage.HINDI -> "दूरी (किमी)"
                AppLanguage.ARABIC -> "المسافة (كم)"
                AppLanguage.FRENCH -> "Distance (km)"
                AppLanguage.SPANISH -> "Distancia (km)"
                AppLanguage.GERMAN -> "Entfernung (km)"
                AppLanguage.CHINESE -> "距离（公里）"
            }
            "mileage_kpl" -> when (language) {
                AppLanguage.ENGLISH -> "Mileage (km/Liter)"
                AppLanguage.BENGALI -> "মাইলেজ (কি.মি./লিটার)"
                AppLanguage.HINDI -> "माइलेज (किमी/लीटर)"
                AppLanguage.ARABIC -> "استهلاك الوقود (كم/لتر)"
                AppLanguage.FRENCH -> "Consommation (km/Litre)"
                AppLanguage.SPANISH -> "Rendimiento (km/Litro)"
                AppLanguage.GERMAN -> "Reichweite (km/Liter)"
                AppLanguage.CHINESE -> "油耗（公里/升）"
            }
            "fuel_price" -> when (language) {
                AppLanguage.ENGLISH -> "Fuel Price (per Liter)"
                AppLanguage.BENGALI -> "প্রতি লিটার তেলের দাম"
                AppLanguage.HINDI -> "ईंधन मूल्य (प्रति लीटर)"
                AppLanguage.ARABIC -> "سعر الوقود (لكل لتر)"
                AppLanguage.FRENCH -> "Prix du carburant (par litre)"
                AppLanguage.SPANISH -> "Precio por litro de combustible"
                AppLanguage.GERMAN -> "Kraftstoffpreis (pro Liter)"
                AppLanguage.CHINESE -> "油价（每升）"
            }
            "fuel_needed" -> when (language) {
                AppLanguage.ENGLISH -> "Fuel Needed:"
                AppLanguage.BENGALI -> "প্রয়োজনীয় তেল:"
                AppLanguage.HINDI -> "आवश्यक ईंधन:"
                AppLanguage.ARABIC -> "الوقود المطلوب:"
                AppLanguage.FRENCH -> "Carburant nécessaire :"
                AppLanguage.SPANISH -> "Combustible necesario:"
                AppLanguage.GERMAN -> "Benötigter Kraftstoff:"
                AppLanguage.CHINESE -> "所需油量："
            }
            "total_fuel_cost" -> when (language) {
                AppLanguage.ENGLISH -> "Total Fuel Cost:"
                AppLanguage.BENGALI -> "মোট তেলের খরচ:"
                AppLanguage.HINDI -> "कुल ईंधन लागत:"
                AppLanguage.ARABIC -> "تكلفة الوقود الإجمالية:"
                AppLanguage.FRENCH -> "Coût total du carburant :"
                AppLanguage.SPANISH -> "Costo total de combustible:"
                AppLanguage.GERMAN -> "Gesamte Treibstoffkosten:"
                AppLanguage.CHINESE -> "燃油总成本："
            }
            "speed_calc_title" -> when (language) {
                AppLanguage.ENGLISH -> "Speed, Distance & Time"
                AppLanguage.BENGALI -> "গতি, দূরত্ব ও সময়"
                AppLanguage.HINDI -> "गति, दूरी और समय"
                AppLanguage.ARABIC -> "السرعة والمسافة والوقت"
                AppLanguage.FRENCH -> "Vitesse, distance et temps"
                AppLanguage.SPANISH -> "Velocidad, distancia y tiempo"
                AppLanguage.GERMAN -> "Geschwindigkeit, Entfernung & Zeit"
                AppLanguage.CHINESE -> "速度、距离与时间"
            }
            "speed_kph" -> when (language) {
                AppLanguage.ENGLISH -> "Speed (km/h)"
                AppLanguage.BENGALI -> "গতিবেগ (কি.মি./ঘণ্টা)"
                AppLanguage.HINDI -> "गति (किमी/घंटा)"
                AppLanguage.ARABIC -> "السرعة (كم/ساعة)"
                AppLanguage.FRENCH -> "Vitesse (km/h)"
                AppLanguage.SPANISH -> "Velocidad (km/h)"
                AppLanguage.GERMAN -> "Geschwindigkeit (km/h)"
                AppLanguage.CHINESE -> "速度（公里/小时）"
            }
            "travel_time_hours" -> when (language) {
                AppLanguage.ENGLISH -> "Time (Hours)"
                AppLanguage.BENGALI -> "সময় (ঘণ্টা)"
                AppLanguage.HINDI -> "समय (घंटे)"
                AppLanguage.ARABIC -> "الالوقت (ساعات)"
                AppLanguage.FRENCH -> "Temps (Heures)"
                AppLanguage.SPANISH -> "Tiempo (Horas)"
                AppLanguage.GERMAN -> "Zeit (Stunden)"
                AppLanguage.CHINESE -> "时间（小时）"
            }
            "distance_result" -> when (language) {
                AppLanguage.ENGLISH -> "Calculated Distance:"
                AppLanguage.BENGALI -> "গণনাকৃত দূরত্ব:"
                AppLanguage.HINDI -> "गणना की गई दूरी:"
                AppLanguage.ARABIC -> "المسافة المحسوبة:"
                AppLanguage.FRENCH -> "Distance calculée :"
                AppLanguage.SPANISH -> "Distancia calculada:"
                AppLanguage.GERMAN -> "Berechnete Entfernung:"
                AppLanguage.CHINESE -> "计算距离："
            }
            "custom_themes_title" -> when (language) {
                AppLanguage.ENGLISH -> "Color Themes"
                AppLanguage.BENGALI -> "কালার থিম"
                AppLanguage.HINDI -> "रंग थीम"
                AppLanguage.ARABIC -> "سمات الألوان"
                AppLanguage.FRENCH -> "Thèmes de couleurs"
                AppLanguage.SPANISH -> "Temas de color"
                AppLanguage.GERMAN -> "Farbthemen"
                AppLanguage.CHINESE -> "颜色主题"
            }
            "custom_themes_desc" -> when (language) {
                AppLanguage.ENGLISH -> "Select your preferred application color palette"
                AppLanguage.BENGALI -> "আপনার পছন্দের কালার প্যালেট নির্বাচন করুন"
                AppLanguage.HINDI -> "अपनी पसंदीदा रंग थीम चुनें"
                AppLanguage.ARABIC -> "اختر نغمة الألوان المفضلة لديك"
                AppLanguage.FRENCH -> "Choisissez votre palette de couleurs préférée"
                AppLanguage.SPANISH -> "Selecciona tu paleta de colores preferida"
                AppLanguage.GERMAN -> "Wählen Sie Ihre bevorzugte Farbpalette"
                AppLanguage.CHINESE -> "选择您喜爱的应用配色方案"
            }
            else -> key
        }
    }
}
