package com.example.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class HadithSample(
    val id: String,
    val numberBn: String,
    val chapterBn: String,
    val narratorBn: String,
    val arabic: String,
    val translationBn: String
)

@Immutable
data class HadithBook(
    val id: String,
    val titleBn: String,
    val compilerBn: String,
    val totalHadithsBn: String,
    val webUrl: String,
    val hadiths: List<HadithSample>
)

object HadithCatalog {

    val books: List<HadithBook> = listOf(
        HadithBook(
            id = "bukhari",
            titleBn = "সহীহ বুখারী",
            compilerBn = "ইমাম মুহাম্মদ ইবনে ইসমাইল আল-বুখারী (রহ.)",
            totalHadithsBn = "৭,৫৬৩ টি হাদিস",
            webUrl = "https://hadithbd.com/hadith/chapters/?book=1",
            hadiths = listOf(
                HadithSample(
                    id = "bukhari_1",
                    numberBn = "হাদিস নং ১",
                    chapterBn = "ওহীর সূচনা",
                    narratorBn = "হযরত উমর ইবনুল খাত্তাব (রা.)",
                    arabic = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى",
                    translationBn = "সকল কাজ নিয়তের ওপর নির্ভরশীল। আর প্রত্যেক ব্যক্তি তাই পাবে যা সে নিয়ত করবে।"
                ),
                HadithSample(
                    id = "bukhari_2",
                    numberBn = "হাদিস নং ১০",
                    chapterBn = "ঈমান অধ্যায়",
                    narratorBn = "হযরত আব্দুল্লাহ ইবনে আমর (রা.)",
                    arabic = "الْمُسْلِمُ مَنْ سَلِمَ الْمُسْلِمُونَ مِنْ لِسَانِهِ وَيَدِهِ",
                    translationBn = "প্রকৃত মুসলিম সেই ব্যক্তি, যার জিহ্বা ও হাত থেকে অন্য মুসলিমগণ নিরাপদ থাকে।"
                ),
                HadithSample(
                    id = "bukhari_3",
                    numberBn = "হাদিস নং ৬৪০৬",
                    chapterBn = "দোয়া ও জিকির",
                    narratorBn = "হযরত আবু হুরায়রা (রা.)",
                    arabic = "كَلِمَتَانِ خَفِيفَتَانِ عَلَى اللِّسَانِ، ثَقِيلَتَانِ فِي الْمِيزَانِ، حَبِيبَتَانِ إِلَى الرَّحْمَنِ: سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ",
                    translationBn = "দুটি বাক্য উচ্চারণে অত্যন্ত হালকা, মীযানের পাল্লায় অতি ভারী এবং পরম করুণাময়ের নিকট অতীব প্রিয়: সুবহানাল্লাহি ওয়া বিহামদিহি, সুবহানাল্লাহিল আজীম।"
                )
            )
        ),
        HadithBook(
            id = "muslim",
            titleBn = "সহীহ মুসলিম",
            compilerBn = "ইমাম মুসলিম ইবনুল হাজ্জাজ আল-কুশাইরী (রহ.)",
            totalHadithsBn = "৭,৪৫৩ টি হাদিস",
            webUrl = "https://hadithbd.com/hadith/chapters/?book=2",
            hadiths = listOf(
                HadithSample(
                    id = "muslim_1",
                    numberBn = "হাদিস নং ২২৩",
                    chapterBn = "পবিত্রতা ও সালাত",
                    narratorBn = "হযরত আবু মালিক আল-আশআরী (রা.)",
                    arabic = "الطُّهُورُ شَطْرُ الإِيمَانِ، وَالْحَمْدُ لِلَّهِ تَمْلأُ الْمِيزَانَ",
                    translationBn = "পবিত্রতা ঈমানের অর্ধেক। আর 'আলহামদুলিল্লাহ' মীযানের পাল্লাকে পূর্ণ করে দেয়।"
                ),
                HadithSample(
                    id = "muslim_2",
                    numberBn = "হাদিস নং ২৫৬৪",
                    chapterBn = "সদকা ও চরিত্র",
                    narratorBn = "হযরত আবু হুরায়রা (রা.)",
                    arabic = "إِنَّ اللَّهَ لاَ يَنْظُرُ إِلَى صُوَرِكُمْ وَأَمْوَالِكُمْ وَلَكِنْ يَنْظُرُ إِلَى قُلُوبِكُمْ وَأَعْمَالِكُمْ",
                    translationBn = "নিশ্চয়ই আল্লাহ তোমাদের বাহ্যিক আকৃতি ও ধন-সম্পদের দিকে তাকান না; বরং তিনি তোমাদের অন্তর ও আমলের দিকে তাকান।"
                )
            )
        ),
        HadithBook(
            id = "abudawud",
            titleBn = "সুনানে আবু দাউদ",
            compilerBn = "ইমাম আবু দাউদ সুলাইমান আস-সিজিস্তানী (রহ.)",
            totalHadithsBn = "৫,২৭৪ টি হাদিস",
            webUrl = "https://hadithbd.com/hadith/chapters/?book=4",
            hadiths = listOf(
                HadithSample(
                    id = "abudawud_1",
                    numberBn = "হাদিস নং ১৫২২",
                    chapterBn = "জিকির ও দোয়া",
                    narratorBn = "হযরত মুআয ইবনে জাবাল (রা.)",
                    arabic = "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ",
                    translationBn = "হে আল্লাহ! আপনার জিকির, আপনার শুকরিয়া আদায় এবং উত্তমরূপে আপনার ইবাদত করার ব্যাপারে আমাকে সাহায্য করুন।"
                ),
                HadithSample(
                    id = "abudawud_2",
                    numberBn = "হাদিস নং ৪৯৪১",
                    chapterBn = "আদব ও দয়া",
                    narratorBn = "হযরত আব্দুল্লাহ ইবনে আমর (রা.)",
                    arabic = "الرَّاحِمُونَ يَرْحَمُهُمُ الرَّحْمَنُ، ارْحَمُوا مَنْ فِي الأَرْضِ يَرْحَمْكُمْ مَنْ فِي السَّمَاءِ",
                    translationBn = "দয়াশীলদের প্রতি পরম করুণাময় আল্লাহ দয়া করেন। তোমরা জমিনের অধিবাসীদের প্রতি দয়া করো, তবে আসমানের অধিপতি তোমাদের প্রতি দয়া করবেন।"
                )
            )
        ),
        HadithBook(
            id = "tirmidhi",
            titleBn = "জামে তিরমিযী",
            compilerBn = "ইমাম আবু ঈসা মুহাম্মদ আত-তিরমিযী (রহ.)",
            totalHadithsBn = "৩,৯৫৬ টি হাদিস",
            webUrl = "https://hadithbd.com/hadith/chapters/?book=5",
            hadiths = listOf(
                HadithSample(
                    id = "tirmidhi_1",
                    numberBn = "হাদিস নং ১৯৮৭",
                    chapterBn = "সদ্ব্যবহার ও তাকওয়া",
                    narratorBn = "হযরত আবু যর (রা.)",
                    arabic = "اتَّقِ اللَّهِ حَيْثُمَا كُنْتَ، وَأَتْبِعِ السَّيِّئَةَ الْحَسَنَةَ تَمْحُهَا، وَخَالِقِ النَّاسَ بِخُلُقٍ حَسَنٍ",
                    translationBn = "তুমি যেখানেই থাকো আল্লাহকে ভয় করো, মন্দের পর ভালো কাজ করো—তা মন্দকে মুছে দেবে, এবং মানুষের সাথে উত্তম চরিত্রের ব্যবহার করো।"
                ),
                HadithSample(
                    id = "tirmidhi_2",
                    numberBn = "হাদিস নং ২৩১৭",
                    chapterBn = "জুহদ ও দুনিয়া",
                    narratorBn = "হযরত আবু হুরায়রা (রা.)",
                    arabic = "مِنْ حُسْنِ إِسْلَامِ الْمَرْءِ تَرْكُهُ مَا لَا يَعْنِيهِ",
                    translationBn = "কোনো ব্যক্তির ইসলামের অন্যতম সৌন্দর্য হলো অনর্থক কাজ ও কথা পরিত্যাগ করা।"
                )
            )
        ),
        HadithBook(
            id = "nasai",
            titleBn = "সুনান আন-নাসায়ী",
            compilerBn = "ইমাম আহমাদ ইবনে শুআইব আন-নাসায়ী (রহ.)",
            totalHadithsBn = "৫,৭৫৮ টি হাদিস",
            webUrl = "https://hadithbd.com/hadith/chapters/?book=6",
            hadiths = listOf(
                HadithSample(
                    id = "nasai_1",
                    numberBn = "হাদিস নং ১৩০৫",
                    chapterBn = "জুমআ ও দোয়া",
                    narratorBn = "হযরত আনাস ইবনে মালিক (রা.)",
                    arabic = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ، أَصْلِحْ لِي شَأْنِي كُلَّهُ، وَلَا تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ",
                    translationBn = "হে চিরঞ্জীব, হে সর্বসত্তার ধারক! আপনার রহমতের উসিলায় সাহায্য চাই। আমার সব অবস্থা সংশোধন করে দিন এবং এক পলকের জন্যও আমাকে আমার নিজের ওপর ছেড়ে দেবেন না।"
                )
            )
        ),
        HadithBook(
            id = "ibnemajah",
            titleBn = "সুনানে ইবনে মাজাহ",
            compilerBn = "ইমাম মুহাম্মদ ইবনে ইয়াজিদ ইবনে মাজাহ (রহ.)",
            totalHadithsBn = "৪,৩৪১ টি হাদিস",
            webUrl = "https://hadithbd.com/hadith/chapters/?book=7",
            hadiths = listOf(
                HadithSample(
                    id = "majah_1",
                    numberBn = "হাদিস নং ২২৪",
                    chapterBn = "ইলম ও জ্ঞানার্জন",
                    narratorBn = "হযরত আনাস ইবনে মালিক (রা.)",
                    arabic = "طَلَبُ الْعِلْمِ فَرِيضَةٌ عَلَى كُلِّ مُسْلِمٍ",
                    translationBn = "প্রত্যেক মুসলিমের ওপর দ্বীনি জ্ঞান অর্জন করা ফরজ।"
                ),
                HadithSample(
                    id = "majah_2",
                    numberBn = "হাদিস নং ৯২৫",
                    chapterBn = "ফজর পরবর্তী দোয়া",
                    narratorBn = "হযরত উম্মে সালামা (রা.)",
                    arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا، وَرِزْقًا طَيِّبًا، وَعَمَلًا مُتَقَبَّلًا",
                    translationBn = "হে আল্লাহ! আমি আপনার নিকট উপকারী জ্ঞান, পবিত্র রিজিক এবং কবুলযোগ্য আমল প্রার্থনা করছি।"
                )
            )
        )
    )
}
