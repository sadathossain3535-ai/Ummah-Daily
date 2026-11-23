package com.example.data.model

import androidx.compose.runtime.Immutable
import java.util.Calendar

@Immutable
data class DailyDua(
    val titleBn: String,
    val arabic: String,
    val pronunciationBn: String,
    val meaningBn: String,
    val referenceBn: String
)

@Immutable
data class DailyHadith(
    val textBn: String,
    val narratorBn: String,
    val sourceBn: String
)

object DailyContentCatalog {

    private val duaMasura = DailyDua(
        titleBn = "দোয়া মাসূরা (আজকের বিশেষ দোয়া)",
        arabic = "اللَّهُمَّ إِنِّي ظَلَمْتُ نَفْسِي ظُلْمًا كَثِيرًا وَلَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ، فَاغْفِرْ لِي مَغْفِرَةً مِنْ عِنْدِكَ وَارْحَمْنِي، إِنَّكَ أَنْتَ الْغَفُورُ الرَّحِيمُ",
        pronunciationBn = "আল্লাহুম্মা ইন্নি যলামতু নাফসী যুলমান কাছীরা, ওয়ালা ইয়াগফিরুয যুনূবা ইল্লা আন্তা, ফাগফিরলী মাগফিরাতাম মিন ইন্দিকা ওয়ারহামনী, ইন্নাকা আন্তাল গাফূরুর রাহীম।",
        meaningBn = "হে আল্লাহ! আমি আমার নিজের ওপর অনেক জুলুম করেছি, আর আপনি ছাড়া গুনাহ মাফ করার কেউ নেই। অতএব আপনার পক্ষ থেকে আমাকে বিশেষভাবে ক্ষমা করুন এবং আমার ওপর দয়া করুন। নিশ্চয়ই আপনি পরম ক্ষমাশীল ও দয়ালু।",
        referenceBn = "সহিহ বুখারি ৮৩৪, সহিহ মুসলিম ২৭০৫"
    )

    private val dailyHadiths = listOf(
        DailyHadith(
            textBn = "তোমাদের মধ্যে সেই ব্যক্তিই সর্বোত্তম, যে নিজে কুরআন শেখে এবং অন্যকে তা শেখায়।",
            narratorBn = "হযরত উসমান ইবনে আফফান (রা.)",
            sourceBn = "সহিহ বুখারি ৫০২৭"
        ),
        DailyHadith(
            textBn = "যে ব্যক্তি মানুষের প্রতি দয়া করে না, আল্লাহও তার প্রতি দয়া করেন না।",
            narratorBn = "হযরত জারীর ইবনে আব্দুল্লাহ (রা.)",
            sourceBn = "সহিহ বুখারি ৭৩৭৬, সহিহ মুসলিম ২৩১৯"
        ),
        DailyHadith(
            textBn = "পবিত্রতা ঈমানের অর্ধেক। আর 'আলহামদুলিল্লাহ' মীযানের পাল্লা পূর্ণ করে দেয়।",
            narratorBn = "হযরত আবু মালিক আশআরী (রা.)",
            sourceBn = "সহিহ মুসলিম ২২৩"
        ),
        DailyHadith(
            textBn = "মুসলিম সেই ব্যক্তি, যার জিহ্বা ও হাত থেকে অন্য মুসলিম নিরাপদ থাকে।",
            narratorBn = "হযরত আব্দুল্লাহ ইবনে আমর (রা.)",
            sourceBn = "সহিহ বুখারি ১০"
        )
    )

    fun getTodayDua(): DailyDua = duaMasura

    fun getTodayHadith(): DailyHadith {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return dailyHadiths[day % dailyHadiths.size]
    }
}
