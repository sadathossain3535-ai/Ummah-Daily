package com.example.data.model

import androidx.compose.runtime.Immutable

enum class PrayerWindowCategory {
    FARD,
    NAFL,
    FORBIDDEN
}

@Immutable
data class FardPrayerItem(
    val id: String,
    val nameBn: String,
    val nameAr: String,
    val startMinuteOfDay: Int,
    val endMinuteOfDay: Int,
    val startTimeBn: String,
    val endTimeBn: String,
    val rakatSummaryBn: String,
    val rakatBreakdown: List<String>,
    val virtueBn: String,
    val amalBn: String,
    val isCurrentActive: Boolean = false,
    val isNextUpcoming: Boolean = false
)

@Immutable
data class NaflPrayerItem(
    val id: String,
    val nameBn: String,
    val subtitleBn: String,
    val startMinuteOfDay: Int,
    val endMinuteOfDay: Int,
    val windowTextBn: String,
    val rakatInfoBn: String,
    val isActive: Boolean = false
)

@Immutable
data class ForbiddenTimeItem(
    val id: String,
    val titleBn: String,
    val reasonBn: String,
    val startMinuteOfDay: Int,
    val endMinuteOfDay: Int,
    val windowTextBn: String,
    val isActive: Boolean = false
)

@Immutable
data class ActiveStatusSnapshot(
    val category: PrayerWindowCategory,
    val statusBadgeBn: String,
    val currentTitleBn: String,
    val nextLabelBn: String,
    val countdownSeconds: Long,
    val progressFraction: Float, // Remaining percentage 0f..1f
    val sehriEndTimeBn: String,
    val iftarTimeBn: String,
    val sehriNextDateBn: String,
    val iftarNextDateBn: String,
    val sehriCountdownSeconds: Long,
    val iftarCountdownSeconds: Long,
    val isBeforeSehri: Boolean,
    val isBeforeIftar: Boolean
)

@Immutable
data class PrayerScheduleState(
    val cityApiName: String,
    val cityNameBn: String,
    val dayNameBn: String,
    val hijriDateBn: String,
    val gregorianDateBn: String,
    val sunriseBn: String,
    val sunsetBn: String,
    val tahajjudTimeBn: String,
    val fardPrayers: List<FardPrayerItem>,
    val naflPrayers: List<NaflPrayerItem>,
    val forbiddenTimes: List<ForbiddenTimeItem>,
    val status: ActiveStatusSnapshot,
    val isFromCache: Boolean = false,
    val lastUpdatedLabelBn: String = ""
)

object FardPrayerDetailsCatalog {
    data class PrayerMetadata(
        val id: String,
        val nameBn: String,
        val nameAr: String,
        val rakatSummaryBn: String,
        val rakatBreakdown: List<String>,
        val virtueBn: String,
        val amalBn: String
    )

    val byId: Map<String, PrayerMetadata> = listOf(
        PrayerMetadata(
            id = "fajr",
            nameBn = "ফজর",
            nameAr = "الفجر",
            rakatSummaryBn = "৪ রাকাত (২ সুন্নত + ২ ফরজ)",
            rakatBreakdown = listOf(
                "২ রাকাত সুন্নতে মুয়াক্কাদা",
                "২ রাকাত ফরজ"
            ),
            virtueBn = "রাসূলুল্লাহ (সা.) বলেছেন: ফজরের দুই রাকাত সুন্নত দুনিয়া ও তার মধ্যকার সবকিছুর চেয়ে উত্তম। (সহিহ মুসলিম)",
            amalBn = "নামাজ শেষে সূর্যোদয় পর্যন্ত সকালের যিকির, আয়াতুল কুরসি ও সূরা ইয়াসিন তিলাওয়াত করা অত্যন্ত বরকতময়।"
        ),
        PrayerMetadata(
            id = "dhuhr",
            nameBn = "যোহর",
            nameAr = "الظهر",
            rakatSummaryBn = "১২ রাকাত (৪ সুন্নত + ৪ ফরজ + ২ সুন্নত + ২ নফল)",
            rakatBreakdown = listOf(
                "৪ রাকাত সুন্নতে মুয়াক্কাদা (পূর্ববর্তী)",
                "৪ রাকাত ফরজ",
                "২ রাকাত সুন্নতে মুয়াক্কাদা (পরবর্তী)",
                "২ রাকাত নফল (ঐচ্ছিক)"
            ),
            virtueBn = "যোহরের পূর্বে ৪ রাকাত ও পরে ৪ রাকাত সুন্নত নিয়মিত আদায়কারীর জন্য জাহান্নামের আগুন হারাম হয়ে যায়। (তিরমিযি)",
            amalBn = "যোহরের পর সূরা ফাতহ বা কুরআনের কিছু অংশ তিলাওয়াত ও ৩৩ বার করে তাসবিহ পাঠ করা উত্তম।"
        ),
        PrayerMetadata(
            id = "asr",
            nameBn = "আসর",
            nameAr = "العصر",
            rakatSummaryBn = "৮ রাকাত (৪ সুন্নত গায়রে মুয়াক্কাদা + ৪ ফরজ)",
            rakatBreakdown = listOf(
                "৪ রাকাত সুন্নতে গায়রে মুয়াক্কাদা",
                "৪ রাকাত ফরজ"
            ),
            virtueBn = "যে ব্যক্তি আসরের নামাজ সময়মতো আদায় করে, ফেরেশতারা আল্লাহর দরবারে তার জন্য সাক্ষ্য প্রদান করেন। (সহিহ বুখারি)",
            amalBn = "আসর নামাজের পর সন্ধ্যার যিকির, ৩ কুল ও দুরুদ শরীফ পাঠ করে সূর্যাস্তের আগে দোয়া করুন।"
        ),
        PrayerMetadata(
            id = "maghrib",
            nameBn = "মাগরিব",
            nameAr = "المغرب",
            rakatSummaryBn = "৭ রাকাত (৩ ফরজ + ২ সুন্নত + ২ নফল)",
            rakatBreakdown = listOf(
                "৩ রাকাত ফরজ",
                "২ রাকাত সুন্নতে মুয়াক্কাদা",
                "২ রাকাত নফল (আওয়াবীন)"
            ),
            virtueBn = "মাগরিবের ওয়াক্ত শুরু হওয়ার সাথে সাথে দ্রুত নামাজ আদায় করা সুন্নত এবং এতে অফুরন্ত রহমত নাজিল হয়।",
            amalBn = "মাগরিবের পর ৬ রাকাত সালাতুল আওয়াবীন আদায় করলে ১২ বছরের নফল ইবাদতের সমান সওয়াব পাওয়া যায়।"
        ),
        PrayerMetadata(
            id = "isha",
            nameBn = "ইশা",
            nameAr = "العشاء",
            rakatSummaryBn = "১৫ রাকাত (৪ সুন্নত + ৪ ফরজ + ২ সুন্নত + ২ নফল + ৩ বিতর)",
            rakatBreakdown = listOf(
                "৪ রাকাত সুন্নতে গায়রে মুয়াক্কাদা",
                "৪ রাকাত ফরজ",
                "২ রাকাত সুন্নতে মুয়াক্কাদা",
                "২ রাকাত নফল",
                "৩ রাকাত ওয়াজিব (বিতর)"
            ),
            virtueBn = "ইশার নামাজ জামাতে আদায় করলে অর্ধেক রাত এবং ফজর জামাতে আদায় করলে সারা রাত কিয়ামুল লাইলের সওয়াব মেলে। (সহিহ মুসলিম)",
            amalBn = "ঘুমানোর আগে সূরা মুলক, সূরা বাকারার শেষ দুই আয়াত ও ৩ বার সূরা ইখলাস পড়ে অজু অবস্থায় ঘুমানো সুন্নত।"
        )
    ).associateBy { it.id }
}
