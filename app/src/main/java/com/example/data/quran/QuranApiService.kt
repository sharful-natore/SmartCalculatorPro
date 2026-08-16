package com.example.data.quran

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class QuranApiService {
    private val client = OkHttpClient.Builder().build()

    suspend fun fetchSurahList(): List<SurahEntity> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://api.alquran.cloud/v1/surah")
            .build()

        try {
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()
            val json = JSONObject(body)
            if (json.optInt("code") == 200) {
                val data = json.getJSONArray("data")
                val list = mutableListOf<SurahEntity>()
                for (i in 0 until data.length()) {
                    val item = data.getJSONObject(i)
                    val num = item.getInt("number")
                    val nameAr = item.getString("name")
                    val nameEn = item.getString("englishName")
                    val nameTranslation = item.getString("englishNameTranslation")
                    val totalAyahs = item.getInt("numberOfAyahs")
                    val revelationType = item.getString("revelationType")
                    val bnName = QuranMetadata.getBanglaSurahName(num, nameEn)

                    list.add(
                        SurahEntity(
                            number = num,
                            nameArabic = nameAr,
                            nameEnglish = nameEn,
                            nameTranslation = nameTranslation,
                            nameBangla = bnName,
                            revelationType = revelationType,
                            numberOfAyahs = totalAyahs
                        )
                    )
                }
                return@withContext list
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext emptyList()
    }

    suspend fun fetchSurahAyahs(surahNumber: Int): List<AyahEntity> = withContext(Dispatchers.IO) {
        // Editions: quran-uthmani (Arabic), bn.bengali (Bangla Translation), en.transliteration (English Transliteration), ar.alafasy (Audio Recitation)
        val url = "https://api.alquran.cloud/v1/surah/$surahNumber/editions/quran-uthmani,bn.bengali,en.transliteration,ar.alafasy"
        val request = Request.Builder().url(url).build()

        try {
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()
            val json = JSONObject(body)
            if (json.optInt("code") == 200) {
                val dataArray = json.getJSONArray("data")
                if (dataArray.length() >= 4) {
                    val uthmaniObj = dataArray.getJSONObject(0)
                    val banglaObj = dataArray.getJSONObject(1)
                    val transObj = dataArray.getJSONObject(2)
                    val audioObj = dataArray.getJSONObject(3)

                    val uthmaniAyahs = uthmaniObj.getJSONArray("ayahs")
                    val banglaAyahs = banglaObj.getJSONArray("ayahs")
                    val transAyahs = transObj.getJSONArray("ayahs")
                    val audioAyahs = audioObj.getJSONArray("ayahs")

                    val ayahsList = mutableListOf<AyahEntity>()
                    for (i in 0 until uthmaniAyahs.length()) {
                        val arAyah = uthmaniAyahs.getJSONObject(i)
                        val bnAyah = banglaAyahs.getJSONObject(i)
                        val transAyah = transAyahs.getJSONObject(i)
                        val audioAyah = audioAyahs.getJSONObject(i)

                        val numInSurah = arAyah.getInt("numberInSurah")
                        val numInQuran = arAyah.getInt("number")
                        val textArabic = arAyah.getString("text")
                        val textBangla = bnAyah.getString("text")
                        val textEnglish = transAyah.getString("text") // Storing transliteration here
                        val audioUrl = audioAyah.optString("audio", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/$numInQuran.mp3")

                        ayahsList.add(
                            AyahEntity(
                                id = "${surahNumber}_$numInSurah",
                                surahNumber = surahNumber,
                                numberInSurah = numInSurah,
                                numberInQuran = numInQuran,
                                textArabic = textArabic,
                                textBangla = textBangla,
                                textEnglish = textEnglish,
                                audioUrl = audioUrl
                            )
                        )
                    }
                    return@withContext ayahsList
                } else if (dataArray.length() >= 3) {
                    val uthmaniObj = dataArray.getJSONObject(0)
                    val banglaObj = dataArray.getJSONObject(1)
                    val audioObj = dataArray.getJSONObject(2)

                    val uthmaniAyahs = uthmaniObj.getJSONArray("ayahs")
                    val banglaAyahs = banglaObj.getJSONArray("ayahs")
                    val audioAyahs = audioObj.getJSONArray("ayahs")

                    val ayahsList = mutableListOf<AyahEntity>()
                    for (i in 0 until uthmaniAyahs.length()) {
                        val arAyah = uthmaniAyahs.getJSONObject(i)
                        val bnAyah = banglaAyahs.getJSONObject(i)
                        val audioAyah = audioAyahs.getJSONObject(i)

                        val numInSurah = arAyah.getInt("numberInSurah")
                        val numInQuran = arAyah.getInt("number")
                        val textArabic = arAyah.getString("text")
                        val textBangla = bnAyah.getString("text")
                        val audioUrl = audioAyah.optString("audio", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/$numInQuran.mp3")

                        ayahsList.add(
                            AyahEntity(
                                id = "${surahNumber}_$numInSurah",
                                surahNumber = surahNumber,
                                numberInSurah = numInSurah,
                                numberInQuran = numInQuran,
                                textArabic = textArabic,
                                textBangla = textBangla,
                                textEnglish = "",
                                audioUrl = audioUrl
                            )
                        )
                    }
                    return@withContext ayahsList
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext emptyList()
    }
}

object QuranMetadata {
    private val banglaNames = mapOf(
        1 to "আল-ফাতিহা", 2 to "আল-বাকারা", 3 to "আলে ইমরান", 4 to "আন-নিসা", 5 to "আল-মায়েদাহ",
        6 to "আল-আনআম", 7 to "আল-আরাফ", 8 to "আল-আনফাল", 9 to "আত-তাওবাহ", 10 to "ইউনুস",
        11 to "হূদ", 12 to "ইউসুফ", 13 to "আর-রা’দ", 14 to "ইব্রাহীম", 15 to "আল-হিজর",
        16 to "আন-নাহল", 17 to "বনী ইসরাঈল", 18 to "আল-কাহফ", 19 to "মারিয়াম", 20 to "ত্বা-হা",
        21 to "আল-আম্বিয়া", 22 to "আল-হাজ্জ", 23 to "আল-মুমিনূন", 24 to "আন-নূর", 25 to "আল-ফুরকান",
        26 to "আশ-শুআরা", 27 to "আন-নামল", 28 to "আল-কাসাস", 29 to "আল-আনকাবূত", 30 to "আর-রূম",
        31 to "লুকমান", 32 to "আস-সাজদাহ", 33 to "আল-আহযাব", 34 to "সাবা", 35 to "ফাত্বের",
        36 to "ইয়া-সীন", 37 to "আস-সাফফাত", 38 to "ছোয়াদ", 39 to "আজ-জুমার", 40 to "আল-মুমিন/গাফির",
        41 to "হা-মীম আস-সাজদাহ", 42 to "আশ-শূরা", 43 to "আজ-যুখরুফ", 44 to "আদ-দুখান", 45 to "আল-জাসিয়াহ",
        46 to "আল-আহকাফ", 47 to "মুহাম্মদ", 48 to "আল-ফাতহ", 49 to "আল-হুজুরাত", 50 to "ক্বাফ",
        51 to "আজ-যারিয়াত", 52 to "আত-তূর", 53 to "আন-নাজম", 54 to "আল-কামার", 55 to "আর-রহমান",
        56 to "আল-ওয়াকিয়াহ", 57 to "আল-হাদীদ", 58 to "আল-মুজাদালাহ", 59 to "আল-হাশর", 60 to "আল-মুমতাহিনাহ",
        61 to "আস-সফ", 62 to "আল-জুমুআহ", 63 to "আল-মুনাফিকূন", 64 to "আত-তাগাবুন", 65 to "আত-ত্বলাক",
        66 to "আত-তাহরীম", 67 to "আল-মুলক", 68 to "আল-কলম", 69 to "আল-হাক্কাহ", 70 to "আল-মাআরিজ",
        71 to "নূহ", 72 to "আল-জিন", 73 to "আল-মুযযামমিল", 74 to "আল-মুদ্দাসসির", 75 to "আল-কিয়ামাহ",
        76 to "আল-ইনসান", 77 to "আল-মুরসালাত", 78 to "আন-নাবা", 79 to "আন-নাযিয়াত", 80 to "আবাসা",
        81 to "আত-তাকবীর", 82 to "আল-ইনফিতার", 83 to "আল-মুতাফফিফীন", 84 to "আল-ইনশিকাক", 85 to "আল-বুরূজ",
        86 to "আত-ত্বারিক", 87 to "আল-আ’লা", 88 to "আল-গাশিয়াহ", 89 to "আল-ফজর", 90 to "আল-বালাদ",
        91 to "আশ-শামস", 92 to "আল-লাইল", 93 to "আদ-দুহা", 94 to "আল-ইনশিরাহ", 95 to "আত-তীন",
        96 to "আল-আলাক", 97 to "আল-কদর", 98 to "আল-বাইয়্যিনাহ", 99 to "আল-যালযালাহ", 100 to "আল-আদিয়াত",
        101 to "আল-কারিয়াহ", 102 to "আত-তাকাসুর", 103 to "আল-আসর", 104 to "আল-হুমাযাহ", 105 to "আল-ফীল",
        106 to "কুরাইশ", 107 to "আল-মাউন", 108 to "আল-কাউসার", 109 to "আল-কাফিরূন", 110 to "আন-নসর",
        111 to "আল-লাহাব", 112 to "আল-ইখলাস", 113 to "আল-ফালাক", 114 to "আন-নাস"
    )

    fun getBanglaSurahName(number: Int, defaultEnglish: String): String {
        return banglaNames[number] ?: defaultEnglish
    }

    // Default Surah List for immediate offline display on app first launch
    val defaultSurahList: List<SurahEntity> = listOf(
        SurahEntity(1, "سُورَةُ الفَاتِحَةِ", "Al-Fatiha", "The Opening", "আল-ফাতিহা", "Meccan", 7),
        SurahEntity(2, "سُورَةُ البَقَرَةِ", "Al-Baqarah", "The Cow", "আল-বাকারা", "Medinan", 286),
        SurahEntity(3, "سُورَةُ آلِ عِمْرَانَ", "Aal-i-Imran", "The Family of Imran", "আলে ইমরান", "Medinan", 200),
        SurahEntity(4, "سُورَةُ النِّسَاءِ", "An-Nisa", "The Women", "আন-নিসা", "Medinan", 176),
        SurahEntity(5, "سُورَةُ المَائِدَةِ", "Al-Ma'idah", "The Table Spread", "আল-মায়েদাহ", "Medinan", 120),
        SurahEntity(6, "سُورَةُ الأَنْعَامِ", "Al-An'am", "The Cattle", "আল-আনআম", "Meccan", 165),
        SurahEntity(7, "سُورَةُ الأَعْرَافِ", "Al-A'raf", "The Heights", "আল-আরাফ", "Meccan", 206),
        SurahEntity(8, "سُورَةُ الأَنْفَالِ", "Al-Anfal", "The Spoils of War", "আল-আনফাল", "Medinan", 75),
        SurahEntity(9, "سُورَةُ التَّوْبَةِ", "At-Tawbah", "The Repentance", "আত-তাওবাহ", "Medinan", 129),
        SurahEntity(10, "سُورَةُ يُونُسَ", "Yunus", "Jonah", "ইউনুস", "Meccan", 109),
        SurahEntity(11, "سُورَةُ هُودٍ", "Hud", "Hud", "হূদ", "Meccan", 123),
        SurahEntity(12, "سُورَةُ يُوسُفَ", "Yusuf", "Joseph", "ইউসুফ", "Meccan", 111),
        SurahEntity(13, "سُورَةُ الرَّعْدِ", "Ar-Ra'd", "The Thunder", "আর-রা’দ", "Medinan", 43),
        SurahEntity(14, "سُورَةُ إِبْرَاهِيمَ", "Ibrahim", "Abraham", "ইব্রাহীম", "Meccan", 52),
        SurahEntity(15, "سُورَةُ الحِجْرِ", "Al-Hijr", "The Rocky Tract", "আল-হিজর", "Meccan", 99),
        SurahEntity(16, "سُورَةُ النَّحْلِ", "An-Nahl", "The Bee", "আন-নাহল", "Meccan", 128),
        SurahEntity(17, "سُورَةُ الإِسْرَاءِ", "Al-Isra", "The Night Journey", "বনী ইসরাঈল", "Meccan", 111),
        SurahEntity(18, "سُورَةُ الكَهْفِ", "Al-Kahf", "The Cave", "আল-কাহফ", "Meccan", 110),
        SurahEntity(19, "سُورَةُ مَرْيَمَ", "Maryam", "Mary", "মারিয়াম", "Meccan", 98),
        SurahEntity(20, "سُورَةُ طٰهٰ", "Taha", "Ta-Ha", "ত্বা-হা", "Meccan", 135),
        SurahEntity(36, "سُورَةُ يسٓ", "Ya-Sin", "Ya Sin", "ইয়া-সীন", "Meccan", 83),
        SurahEntity(55, "سُورَةُ الرَّحْمٰنِ", "Ar-Rahman", "The Beneficent", "আর-রহমান", "Medinan", 78),
        SurahEntity(56, "سُورَةُ الوَاقِعَةِ", "Al-Waqi'ah", "The Inevitable", "আল-ওয়াকিয়াহ", "Meccan", 96),
        SurahEntity(67, "سُورَةُ المُلْكِ", "Al-Mulk", "The Sovereignty", "আল-মুলক", "Meccan", 30),
        SurahEntity(112, "سُورَةُ الإِخْلَاصِ", "Al-Ikhlas", "The Sincerity", "আল-ইখলাস", "Meccan", 4),
        SurahEntity(113, "سُورَةُ الفَلَقِ", "Al-Falaq", "The Daybreak", "আল-ফালাক", "Meccan", 5),
        SurahEntity(114, "سُورَةُ النَّاسِ", "An-Nas", "Mankind", "আন-নাস", "Meccan", 6)
    )
}
