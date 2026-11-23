package com.example.data.model

import androidx.compose.runtime.Immutable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class TasbihSubTab(val titleBn: String, val navLabelBn: String) {
    COUNTER("তাসবিহ", "📿 তাসবিহ"),
    NAMAZ_ROZA("নামাজ-রোজা", "🕌 নামাজ-রোজা"),
    AMALNAMA("আমলনামা", "📜 আমলনামা"),
    RANK("র‍্যাংক", "🏆 র‍্যাংক")
}

enum class WaqtType(val titleBn: String, val badgeBn: String) {
    FAJR("ফজর", "🕌 বর্তমান ওয়াক্ত: ফজর"),
    DHUHR("যোহর", "🕌 বর্তমান ওয়াক্ত: যোহর"),
    ASR("আসর", "🕌 বর্তমান ওয়াক্ত: আসর"),
    MAGHRIB("মাগরিব", "🕌 বর্তমান ওয়াক্ত: মাগরিব"),
    ISHA("ইশা", "🕌 বর্তমান ওয়াক্ত: ইশা"),
    TAHAJJUD("তাহাজ্জুদ", "🕌 বর্তমান ওয়াক্ত: তাহাজ্জুদ")
}

fun detectWaqtByHour(hour: Int): WaqtType = when (hour) {
    in 4..11 -> WaqtType.FAJR
    in 12..15 -> WaqtType.DHUHR
    in 16..17 -> WaqtType.ASR
    in 18..19 -> WaqtType.MAGHRIB
    in 20..23, in 0..2 -> WaqtType.ISHA
    3 -> WaqtType.TAHAJJUD
    else -> WaqtType.ISHA
}

@Immutable
@JsonClass(generateAdapter = true)
data class TasbihDhikrItem(
    val id: String,
    val titleBn: String,
    val arabic: String,
    val pronunciationBn: String,
    val meaningBn: String,
    val virtueBn: String,
    val defaultTarget: Int = 33,
    val waqtTags: List<String> = emptyList(),
    val isCustom: Boolean = false
)

@JsonClass(generateAdapter = true)
data class RtdbUserMeta(
    @Json(name = "myId") val myId: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "phone") val phone: String = "",
    @Json(name = "area") val area: String = "",
    @Json(name = "district") val district: String = "",
    @Json(name = "location") val location: String = "",
    @Json(name = "createdAt") val createdAt: Long = 0L,
    @Json(name = "updatedAt") val updatedAt: Long = 0L
)

@JsonClass(generateAdapter = true)
data class RtdbLeaderboardEntry(
    @Json(name = "myId") val myId: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "location") val location: String = "",
    @Json(name = "area") val area: String = "",
    @Json(name = "district") val district: String = "",
    @Json(name = "totalCount") val totalCount: Long = 0L,
    @Json(name = "totalTasbih") val totalTasbih: Long = 0L,
    @Json(name = "updatedAt") val updatedAt: Long = 0L
)

object PrayerChecklistKeys {
    const val FAJR_FARZ = "fajr_farz"
    const val FAJR_SUNNAT = "fajr_sunnat"

    const val DHUHR_FARZ = "dhuhr_farz"
    const val DHUHR_SUNNAT_BEFORE = "dhuhr_sunnat_before"
    const val DHUHR_SUNNAT_AFTER = "dhuhr_sunnat_after"
    const val DHUHR_NAFL = "dhuhr_nafl"

    const val ASR_FARZ = "asr_farz"
    const val ASR_SUNNAT = "asr_sunnat"

    const val MAGHRIB_FARZ = "maghrib_farz"
    const val MAGHRIB_SUNNAT = "maghrib_sunnat"
    const val MAGHRIB_AWABIN = "maghrib_awabin"

    const val ISHA_FARZ = "isha_farz"
    const val ISHA_WITR = "isha_witr"
    const val ISHA_SUNNAT_BEFORE = "isha_sunnat_before"
    const val ISHA_SUNNAT_AFTER = "isha_sunnat_after"
    const val ISHA_NAFL = "isha_nafl"

    const val NAFL_TAHAJJUD = "nafl_tahajjud"
    const val NAFL_ISHRAQ = "nafl_ishraq"
    const val NAFL_CHASHT = "nafl_chasht"

    const val ROZA_FARZ = "roza_farz"
    const val ROZA_SUNNAT = "roza_sunnat"
    const val ROZA_AYAME_BEEJ = "roza_ayame_beej"
    const val ROZA_NAFL = "roza_nafl"
    const val ROZA_QAZA = "roza_qaza"

    val allFardKeys = listOf(FAJR_FARZ, DHUHR_FARZ, ASR_FARZ, MAGHRIB_FARZ, ISHA_FARZ)
    val allRozaKeys = listOf(ROZA_FARZ, ROZA_SUNNAT, ROZA_AYAME_BEEJ, ROZA_NAFL, ROZA_QAZA)
    val allItemKeys = listOf(
        FAJR_FARZ, FAJR_SUNNAT,
        DHUHR_FARZ, DHUHR_SUNNAT_BEFORE, DHUHR_SUNNAT_AFTER, DHUHR_NAFL,
        ASR_FARZ, ASR_SUNNAT,
        MAGHRIB_FARZ, MAGHRIB_SUNNAT, MAGHRIB_AWABIN,
        ISHA_FARZ, ISHA_WITR, ISHA_SUNNAT_BEFORE, ISHA_SUNNAT_AFTER, ISHA_NAFL,
        NAFL_TAHAJJUD, NAFL_ISHRAQ, NAFL_CHASHT,
        ROZA_FARZ, ROZA_SUNNAT, ROZA_AYAME_BEEJ, ROZA_NAFL, ROZA_QAZA
    )
}

@Immutable
data class PrayerGroupSpec(
    val id: String,
    val titleBn: String,
    val items: List<Pair<String, String>>,
    val isRozaGroup: Boolean = false
)

object PrayerRozaCatalog {
    val groups: List<PrayerGroupSpec> = listOf(
        PrayerGroupSpec(
            id = "fajr",
            titleBn = "ফজর",
            items = listOf(
                PrayerChecklistKeys.FAJR_FARZ to "ফরজ ২",
                PrayerChecklistKeys.FAJR_SUNNAT to "সুন্নত ২"
            )
        ),
        PrayerGroupSpec(
            id = "dhuhr",
            titleBn = "যোহর",
            items = listOf(
                PrayerChecklistKeys.DHUHR_FARZ to "ফরজ ৪",
                PrayerChecklistKeys.DHUHR_SUNNAT_BEFORE to "৪ সুন্নত আগে",
                PrayerChecklistKeys.DHUHR_SUNNAT_AFTER to "২ সুন্নত পরে",
                PrayerChecklistKeys.DHUHR_NAFL to "নফল ২"
            )
        ),
        PrayerGroupSpec(
            id = "asr",
            titleBn = "আসর",
            items = listOf(
                PrayerChecklistKeys.ASR_FARZ to "ফরজ ৪",
                PrayerChecklistKeys.ASR_SUNNAT to "সুন্নত ৪"
            )
        ),
        PrayerGroupSpec(
            id = "maghrib",
            titleBn = "মাগরিব",
            items = listOf(
                PrayerChecklistKeys.MAGHRIB_FARZ to "ফরজ ৩",
                PrayerChecklistKeys.MAGHRIB_SUNNAT to "সুন্নত ২",
                PrayerChecklistKeys.MAGHRIB_AWABIN to "আওয়াবিন ২"
            )
        ),
        PrayerGroupSpec(
            id = "isha",
            titleBn = "ইশা",
            items = listOf(
                PrayerChecklistKeys.ISHA_FARZ to "ফরজ ৪",
                PrayerChecklistKeys.ISHA_WITR to "বিতর ৩",
                PrayerChecklistKeys.ISHA_SUNNAT_BEFORE to "৪ সুন্নত আগে",
                PrayerChecklistKeys.ISHA_SUNNAT_AFTER to "২ সুন্নত পরে",
                PrayerChecklistKeys.ISHA_NAFL to "নফল ২"
            )
        ),
        PrayerGroupSpec(
            id = "nafl",
            titleBn = "নফল নামাজ",
            items = listOf(
                PrayerChecklistKeys.NAFL_TAHAJJUD to "তাহাজ্জুদ",
                PrayerChecklistKeys.NAFL_ISHRAQ to "ইশরাক",
                PrayerChecklistKeys.NAFL_CHASHT to "চাশত"
            )
        ),
        PrayerGroupSpec(
            id = "roza",
            titleBn = "রোজা",
            isRozaGroup = true,
            items = listOf(
                PrayerChecklistKeys.ROZA_FARZ to "ফরজ রোজা",
                PrayerChecklistKeys.ROZA_SUNNAT to "সুন্নত রোজা",
                PrayerChecklistKeys.ROZA_AYAME_BEEJ to "আইয়ামে বিজ",
                PrayerChecklistKeys.ROZA_NAFL to "নফল রোজা",
                PrayerChecklistKeys.ROZA_QAZA to "কাজা রোজা"
            )
        )
    )
}

@Immutable
data class DailyPrayerChecklist(
    val date: String = "",
    val checks: Map<String, Boolean> = emptyMap(),
    val updatedAt: Long = 0L
) {
    fun isChecked(key: String): Boolean = checks[key] == true

    fun completedTotalCount(): Int = PrayerChecklistKeys.allItemKeys.count { isChecked(it) }

    fun completedFardCount(): Int = PrayerChecklistKeys.allFardKeys.count { isChecked(it) }

    fun hasAnyRoza(): Boolean = PrayerChecklistKeys.allRozaKeys.any { isChecked(it) }

    fun toggle(key: String, checked: Boolean): DailyPrayerChecklist {
        val next = checks.toMutableMap()
        next[key] = checked
        return copy(checks = next)
    }

    companion object {
        fun fromRawMap(date: String, raw: Map<String, Any?>?): DailyPrayerChecklist {
            if (raw == null) return DailyPrayerChecklist(date = date)
            val parsed = linkedMapOf<String, Boolean>()
            raw.forEach { (k, v) ->
                if (v is Boolean) parsed[k] = v
            }
            // Compatibility with older camelCase keys if present
            if (raw["fajrFard"] == true) parsed[PrayerChecklistKeys.FAJR_FARZ] = true
            if (raw["fajrSunnah"] == true) parsed[PrayerChecklistKeys.FAJR_SUNNAT] = true
            if (raw["dhuhrFard"] == true) parsed[PrayerChecklistKeys.DHUHR_FARZ] = true
            if (raw["dhuhrSunnah"] == true) parsed[PrayerChecklistKeys.DHUHR_SUNNAT_BEFORE] = true
            if (raw["asrFard"] == true) parsed[PrayerChecklistKeys.ASR_FARZ] = true
            if (raw["asrSunnah"] == true) parsed[PrayerChecklistKeys.ASR_SUNNAT] = true
            if (raw["maghribFard"] == true) parsed[PrayerChecklistKeys.MAGHRIB_FARZ] = true
            if (raw["maghribSunnah"] == true) parsed[PrayerChecklistKeys.MAGHRIB_SUNNAT] = true
            if (raw["awabin"] == true) parsed[PrayerChecklistKeys.MAGHRIB_AWABIN] = true
            if (raw["ishaFard"] == true) parsed[PrayerChecklistKeys.ISHA_FARZ] = true
            if (raw["witr"] == true) parsed[PrayerChecklistKeys.ISHA_WITR] = true
            if (raw["ishaSunnah"] == true) parsed[PrayerChecklistKeys.ISHA_SUNNAT_AFTER] = true
            if (raw["tahajjud"] == true) parsed[PrayerChecklistKeys.NAFL_TAHAJJUD] = true
            if (raw["ishraq"] == true) parsed[PrayerChecklistKeys.NAFL_ISHRAQ] = true
            if (raw["chasht"] == true) parsed[PrayerChecklistKeys.NAFL_CHASHT] = true
            if (raw["roza"] == true) parsed[PrayerChecklistKeys.ROZA_FARZ] = true

            val upd = (raw["updatedAt"] as? Number)?.toLong() ?: 0L
            return DailyPrayerChecklist(date = date, checks = parsed, updatedAt = upd)
        }
    }
}

@Immutable
data class RankedUserRow(
    val rank: Int,
    val myId: String,
    val name: String,
    val locationDisplay: String,
    val totalCount: Long,
    val isCurrentUser: Boolean
)

@Immutable
data class AmalnamaSummary(
    val titleRangeBn: String,
    val startDate: String,
    val endDate: String,
    val totalTasbih: Long,
    val dhikrBreakdown: List<Pair<String, Long>>,
    val fajrDays: Int,
    val dhuhrDays: Int,
    val asrDays: Int,
    val maghribDays: Int,
    val ishaDays: Int,
    val tahajjudDays: Int,
    val totalRozaDays: Int,
    val daysCovered: Int
)

object TasbihDhikrCatalog {

    val allBuiltInDhikrs: List<TasbihDhikrItem> = listOf(
        // Waqt-specific Sunnah Dhikrs
        TasbihDhikrItem(
            id = "tahlil_10x",
            titleBn = "তাহলীল ১০ বার (ফজর ও মাগরিব)",
            arabic = "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ يُحْيِي وَيُمِيتُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            pronunciationBn = "লা ইলাহা ইল্লাল্লাহু ওয়াহদাহু লা শারীকা লাহু, লাহুল মুলকু ওয়া লাহুল হামদু, ইউহয়ী ওয়া ইউমীতু ওয়া হুওয়া আলা কুল্লি শাইয়িন কাদীর।",
            meaningBn = "আল্লাহ ছাড়া কোনো উপাস্য নেই, তিনি একক, তাঁর কোনো শরীক নেই। রাজত্ব তাঁরই এবং সকল প্রশংসা তাঁরই। তিনি জীবন ও মৃত্যু দান করেন এবং তিনি সর্বশক্তিমান।",
            virtueBn = "নবীজি (সা.) বলেন: ফজর ও মাগরিবের নামাজের পর পা ভাঁজ করা অবস্থায় কথা বলার আগে ১০ বার এটি পাঠ করলে প্রতিটি অক্ষরের বিনিময়ে ১০টি নেকি, ১০টি গুনাহ মাফ ও ইসমাইল (আ.)-এর বংশের ১০টি গোলাম আজাদ করার সওয়াব লেখা হয়। (তিরমিযি ৩৪৭৪)",
            defaultTarget = 10,
            waqtTags = listOf(WaqtType.FAJR.name, WaqtType.MAGHRIB.name)
        ),
        TasbihDhikrItem(
            id = "sayyidul_istighfar",
            titleBn = "সায়্যিদুল ইস্তিগফার",
            arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ خَلَقْتَنِي وَأَنَا عَبْدُكَ وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            pronunciationBn = "আল্লাহুম্মা আন্তা রাব্বী লা ইলাহা ইল্লা আন্তা, খালাক্বতানী ওয়া আনা আবদুকা ওয়া আনা আলা আহদিকা ওয়া ওয়াদিকা মাসতাতাতু, আউযুবিকা মিন শাররি মা সানাতু, আবূউ লাকা বিনি'মাতিকা আলাইয়্যা ওয়া আবূউ বিযাম্বী, ফাগফিরলী ফাইন্নাহু লা ইয়াগফিরুয যুনুবা ইল্লা আন্তা।",
            meaningBn = "হে আল্লাহ! আপনি আমার রব, আপনি ছাড়া কোনো মাবুদ নেই। আপনি আমাকে সৃষ্টি করেছেন এবং আমি আপনার বান্দা। আমি আমার সাধ্যমতো আপনার প্রতিশ্রুতির ওপর আছি। আমার কৃতকর্মের অনিষ্ট থেকে আপনার আশ্রয় চাই এবং গুনাহ স্বীকার করছি—আমাকে ক্ষমা করুন।",
            virtueBn = "রাসূলুল্লাহ (সা.) বলেন: যে ব্যক্তি দৃঢ় বিশ্বাসের সাথে সকালে বা সন্ধ্যায় এটি পাঠ করে সেদিন বা সেই রাতে মারা গেলে সে জান্নাতি হবে। (সহিহ বুখারি ৬৩০৬)",
            defaultTarget = 3,
            waqtTags = listOf(WaqtType.FAJR.name, WaqtType.MAGHRIB.name)
        ),
        TasbihDhikrItem(
            id = "allahumma_ajirni_7x",
            titleBn = "আল্লাহুম্মা আজিরনী মিনান নার (৭ বার)",
            arabic = "اللَّهُمَّ أَجِرْنِي مِنَ النَّارِ",
            pronunciationBn = "আল্লাহুম্মা আজিরনী মিনান নার।",
            meaningBn = "হে আল্লাহ! আমাকে জাহান্নামের আগুন থেকে রক্ষা করুন।",
            virtueBn = "ফজর ও মাগরিবের পর কারো সাথে কথা বলার পূর্বে ৭ বার পাঠ করলে সেদিন বা সেই রাতে মৃত্যু হলে জাহান্নাম থেকে মুক্তি মিলবে। (আবু দাউদ ৫০৭৯)",
            defaultTarget = 7,
            waqtTags = listOf(WaqtType.FAJR.name, WaqtType.MAGHRIB.name)
        ),
        TasbihDhikrItem(
            id = "tasbih_33_33_34",
            titleBn = "৩৩-৩৩-৩৪ তাসবিহ (প্রতি ওয়াক্ত নামাজ শেষে)",
            arabic = "سُبْحَانَ اللَّهِ (٣٣) • الْحَمْدُ لِلَّهِ (٣٣) • اللَّهُ أَكْبَرُ (٣٤)",
            pronunciationBn = "৩৩ বার সুবহানাল্লাহ, ৩৩ বার আলহামদুলিল্লাহ, ৩৪ বার আল্লাহু আকবার (মোট ১০০ বার)।",
            meaningBn = "আল্লাহ অতি পবিত্র, সমস্ত প্রশংসা আল্লাহর জন্য, আল্লাহ সর্বশ্রেষ্ঠ।",
            virtueBn = "প্রতি ফরজ নামাজের পর ৩৩ বার সুবহানাল্লাহ, ৩৩ বার আলহামদুলিল্লাহ এবং ৩৩/৩৪ বার আল্লাহু আকবার পড়লে সমুদ্রের ফেনা সমান গুনাহও মাফ হয়ে যায়। (সহিহ মুসলিম ৫৯৭)",
            defaultTarget = 100,
            waqtTags = listOf(
                WaqtType.FAJR.name,
                WaqtType.DHUHR.name,
                WaqtType.ASR.name,
                WaqtType.MAGHRIB.name,
                WaqtType.ISHA.name
            )
        ),
        TasbihDhikrItem(
            id = "tasbih_fatimi_100",
            titleBn = "তাসবিহে ফাতিমি (১০০ বার)",
            arabic = "سُبْحَانَ اللَّهِ (٣٣) • الْحَمْدُ لِلَّهِ (٣٣) • اللَّهُ أَكْبَرُ (٣٤)",
            pronunciationBn = "সুবহানাল্লাহ ৩৩ বার, আলহামদুলিল্লাহ ৩৩ বার, আল্লাহু আকবার ৩৪ বার।",
            meaningBn = "আল্লাহ পবিত্র, সকল প্রশংসা আল্লাহর, আল্লাহ মহান।",
            virtueBn = "হযরত ফাতিমা (রা.)-কে নবীজি (সা.) ঘুমের আগে ও নামাজ শেষে এটি পাঠের নির্দেশ দেন—যা একজন খাদেম অপেক্ষাও উত্তম। (সহিহ বুখারি ৩৭০৫)",
            defaultTarget = 100,
            waqtTags = listOf(WaqtType.ISHA.name, WaqtType.DHUHR.name)
        ),
        TasbihDhikrItem(
            id = "durood_sharif",
            titleBn = "দরুদ শরীফ",
            arabic = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ",
            pronunciationBn = "আল্লাহুম্মা সল্লি আলা মুহাম্মাদিও ওয়া আলা আলি মুহাম্মাদ।",
            meaningBn = "হে আল্লাহ! হযরত মুহাম্মদ (সা.) এবং তাঁর পরিবারবর্গের ওপর রহমত বর্ষণ করুন।",
            virtueBn = "যে ব্যক্তি আমার ওপর একবার দরুদ পাঠ করে, আল্লাহ তার ওপর দশটি রহমত নাজিল করেন এবং দশটি মর্যাদা বৃদ্ধি করেন। (সহিহ মুসলিম ৪০৮)",
            defaultTarget = 100,
            waqtTags = listOf(WaqtType.DHUHR.name, WaqtType.ASR.name)
        ),
        TasbihDhikrItem(
            id = "tahajjud_yunus",
            titleBn = "দোয়া ইউনুস (তাহাজ্জুদ ও নিশি রাত)",
            arabic = "لَا إِلَهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ",
            pronunciationBn = "লা ইলাহা ইল্লা আন্তা সুবহানাকা ইন্নি কুন্তু মিনায জালিমীন।",
            meaningBn = "আপনি ছাড়া কোনো উপাস্য নেই, আপনি পবিত্র মহান। নিশ্চয়ই আমি জালিমদের অন্তর্ভুক্ত ছিলাম।",
            virtueBn = "তাহাজ্জুদের সময় ও সংকটে এই দোয়া পাঠ করে আল্লাহর কাছে দোয়া করলে তা কবুল হয়। (তিরমিযি ৩৫০৫)",
            defaultTarget = 40,
            waqtTags = listOf(WaqtType.TAHAJJUD.name, WaqtType.ISHA.name)
        ),

        // Regular Dhikrs (সাধারণ জিকির)
        TasbihDhikrItem(
            id = "general_tasbih",
            titleBn = "সাধারণ",
            arabic = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ سُبْحَانَ اللَّهِ الْعَظِيمِ",
            pronunciationBn = "সুবহানাল্লাহি ওয়া বিহামদিহি সুবহানাল্লাহিল আজীম।",
            meaningBn = "আল্লাহর প্রশংসাসহ তাঁর পবিত্রতা ঘোষণা করছি, মহান আল্লাহর পবিত্রতা ঘোষণা করছি।",
            virtueBn = "দুটি বাক্য যা উচ্চারণে খুবই সহজ, কিন্তু মীযানের পাল্লায় অত্যন্ত ভারী এবং আল্লাহর নিকট অতীব প্রিয়। (সহিহ বুখারি ৬৪০৬)",
            defaultTarget = 100
        ),
        TasbihDhikrItem(
            id = "subhanallah",
            titleBn = "সুবহানাল্লাহ",
            arabic = "سُبْحَانَ اللَّهِ",
            pronunciationBn = "সুবহানাল্লাহ।",
            meaningBn = "আল্লাহ সর্বপ্রকার ত্রুটি থেকে পবিত্র ও মহান।",
            virtueBn = "প্রতিবার 'সুবহানাল্লাহ' বলায় জান্নাতে একটি করে খেজুর গাছ রোপণ করা হয় এবং আমলনামায় নেকি বৃদ্ধি পায়। (তিরমিযি ৩৪৬৪)",
            defaultTarget = 33
        ),
        TasbihDhikrItem(
            id = "alhamdulillah",
            titleBn = "আলহামদুলিল্লাহ",
            arabic = "الْحَمْدُ لِلَّهِ",
            pronunciationBn = "আলহামদুলিল্লাহ।",
            meaningBn = "সমস্ত প্রশংসা একমাত্র আল্লাহর জন্য।",
            virtueBn = "রাসূলুল্লাহ (সা.) বলেছেন: 'আলহামদুলিল্লাহ' মীযানের পাল্লাকে সওয়াবে পূর্ণ করে দেয়। (সহিহ মুসলিম ২২৩)",
            defaultTarget = 33
        ),
        TasbihDhikrItem(
            id = "allahuakbar",
            titleBn = "আল্লাহু আকবার",
            arabic = "اللَّهُ أَكْبَرُ",
            pronunciationBn = "আল্লাহু আকবার।",
            meaningBn = "আল্লাহ সর্বশ্রেষ্ঠ।",
            virtueBn = "আল্লাহর মহিমা ঘোষণাকারী এই জিকির আসমান ও জমিনের মধ্যবর্তী শূন্যস্থানকে সওয়াবে পূর্ণ করে দেয়। (সহিহ মুসলিম ২২৩)",
            defaultTarget = 34
        ),
        TasbihDhikrItem(
            id = "lailaha_illallah",
            titleBn = "লা ইলাহা ইল্লাল্লাহ",
            arabic = "لَا إِلَهَ إِلَّا اللَّهُ",
            pronunciationBn = "লা ইলাহা ইল্লাল্লাহ।",
            meaningBn = "আল্লাহ ছাড়া কোনো সত্য উপাস্য নেই।",
            virtueBn = "সর্বোত্তম যিকির হলো 'লা ইলাহা ইল্লাল্লাহ'। (তিরমিযি ৩৩৮৩)",
            defaultTarget = 100
        ),
        TasbihDhikrItem(
            id = "astaghfirullah",
            titleBn = "আস্তাগফিরুল্লাহ",
            arabic = "أَسْتَغْفِرُ اللَّهَ",
            pronunciationBn = "আস্তাগফিরুল্লাহ।",
            meaningBn = "আমি আল্লাহর কাছে ক্ষমা প্রার্থনা করছি।",
            virtueBn = "নিয়মিত ইস্তিগফার করলে আল্লাহ রিজিক প্রশস্ত করেন এবং সকল সংকট থেকে উত্তরণের পথ বের করে দেন। (আবু দাউদ ১৫১৮)",
            defaultTarget = 100
        )
    )
}
