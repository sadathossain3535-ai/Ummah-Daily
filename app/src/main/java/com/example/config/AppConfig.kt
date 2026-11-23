package com.example.config

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.FormatListNumbered
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Config-driven registry for UMMAH DAILY.
 * Tabs, city options, Quran module slots, and Tools grid modules can all be reordered,
 * swapped, or extended from this single file without touching navigation boilerplate.
 */
enum class AppTabId(val route: String) {
    HOME("tab_home"),
    QURAN("tab_quran"),
    DUA("tab_dua"),
    TASBIH("tab_tasbih"),
    TOOLS("tab_tools")
}

@Immutable
data class TabConfig(
    val id: AppTabId,
    val labelBn: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val enabled: Boolean = true
)

@Immutable
data class BangladeshCity(
    val apiCity: String,
    val nameBn: String,
    val divisionBn: String
)

@Immutable
data class QuranModuleSlot(
    val id: String,
    val titleBn: String,
    val subtitleBn: String,
    val icon: ImageVector,
    val badgeBn: String
)

@Immutable
data class ToolModuleConfig(
    val id: String,
    val titleBn: String,
    val subtitleBn: String,
    val icon: ImageVector,
    val accentColor: Color,
    val isPlaceholder: Boolean = false,
    val statusBadgeBn: String = "মডিউল"
)

object AppConfigRegistry {

    const val APP_BRAND_TITLE = "UMMAH DAILY"
    const val ALADHAN_BASE_URL = "https://api.aladhan.com/"
    const val ALADHAN_COUNTRY = "Bangladesh"
    const val ALADHAN_METHOD = 1 // University of Islamic Sciences, Karachi (standard in Bangladesh)

    val bottomTabs: List<TabConfig> = listOf(
        TabConfig(
            id = AppTabId.HOME,
            labelBn = "হোম",
            selectedIcon = Icons.Rounded.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        TabConfig(
            id = AppTabId.QURAN,
            labelBn = "কুরআন",
            selectedIcon = Icons.Rounded.MenuBook,
            unselectedIcon = Icons.Outlined.MenuBook
        ),
        TabConfig(
            id = AppTabId.DUA,
            labelBn = "দোয়া",
            selectedIcon = Icons.Rounded.VolunteerActivism,
            unselectedIcon = Icons.Outlined.VolunteerActivism
        ),
        TabConfig(
            id = AppTabId.TASBIH,
            labelBn = "তাসবিহ",
            selectedIcon = Icons.Rounded.RadioButtonChecked,
            unselectedIcon = Icons.Outlined.RadioButtonChecked
        ),
        TabConfig(
            id = AppTabId.TOOLS,
            labelBn = "টুলস",
            selectedIcon = Icons.Rounded.Widgets,
            unselectedIcon = Icons.Outlined.Widgets
        )
    ).filter { it.enabled }

    val bangladeshCities: List<BangladeshCity> = listOf(
        BangladeshCity(apiCity = "Dhaka", nameBn = "ঢাকা", divisionBn = "ঢাকা বিভাগ"),
        BangladeshCity(apiCity = "Chittagong", nameBn = "চট্টগ্রাম", divisionBn = "চট্টগ্রাম বিভাগ"),
        BangladeshCity(apiCity = "Sylhet", nameBn = "সিলেট", divisionBn = "সিলেট বিভাগ"),
        BangladeshCity(apiCity = "Rajshahi", nameBn = "রাজশাহী", divisionBn = "রাজশাহী বিভাগ"),
        BangladeshCity(apiCity = "Khulna", nameBn = "খুলনা", divisionBn = "খুলনা বিভাগ"),
        BangladeshCity(apiCity = "Barishal", nameBn = "বরিশাল", divisionBn = "বরিশাল বিভাগ"),
        BangladeshCity(apiCity = "Rangpur", nameBn = "রংপুর", divisionBn = "রংপুর বিভাগ"),
        BangladeshCity(apiCity = "Mymensingh", nameBn = "ময়মনসিংহ", divisionBn = "ময়মনসিংহ বিভাগ")
    )

    val quranModuleSlots: List<QuranModuleSlot> = listOf(
        QuranModuleSlot(
            id = "quran_surah_list",
            titleBn = "সূরা তালিকা (১১৪ সূরা)",
            subtitleBn = "আরবি, বাংলা উচ্চারণ ও অনুবাদ মডিউলের জন্য প্রস্তুত",
            icon = Icons.Rounded.MenuBook,
            badgeBn = "স্লট ০১"
        ),
        QuranModuleSlot(
            id = "quran_juz_index",
            titleBn = "পারা / জুজ সূচী (৩০ পারা)",
            subtitleBn = "দ্রুত তিলাওয়াত নেভিগেশন ও আয়াত বুকমার্ক স্লট",
            icon = Icons.Rounded.AutoStories,
            badgeBn = "স্লট ০২"
        ),
        QuranModuleSlot(
            id = "quran_last_read",
            titleBn = "সর্বশেষ পাঠ ও বুকমার্ক",
            subtitleBn = "DataStore ভিত্তিক পঠন অগ্রগতি সংরক্ষণের জন্য প্রস্তুত",
            icon = Icons.Rounded.Book,
            badgeBn = "স্লট ০৩"
        ),
        QuranModuleSlot(
            id = "quran_custom_extension",
            titleBn = "কাস্টম কুরআন এক্সটেনশন",
            subtitleBn = "অডিও তিলাওয়াত বা তাফসির প্লাগইন যুক্ত করার হুক",
            icon = Icons.Rounded.Extension,
            badgeBn = "এক্সটেনশন"
        )
    )

    val toolsGridConfig: List<ToolModuleConfig> = listOf(
        ToolModuleConfig(
            id = "tool_quran",
            titleBn = "আল কুরআন",
            subtitleBn = "কুরআন ট্যাব ও বাংলা অনুবাদ",
            icon = Icons.Rounded.MenuBook,
            accentColor = Color(0xFF04484A),
            isPlaceholder = false,
            statusBadgeBn = "কুরআন"
        ),
        ToolModuleConfig(
            id = "tool_99_names",
            titleBn = "আল্লাহর ৯৯ নাম",
            subtitleBn = "আসমাউল হুসনা, অর্থ ও ফজিলত",
            icon = Icons.Rounded.FormatListNumbered,
            accentColor = Color(0xFF04484A),
            isPlaceholder = false,
            statusBadgeBn = "১-৯৯ নাম"
        ),
        ToolModuleConfig(
            id = "tool_zakat",
            titleBn = "যাকাত ক্যালকুলেটর",
            subtitleBn = "স্বর্ণ, রৌপ্য ও নগদ হিসাব",
            icon = Icons.Rounded.Calculate,
            accentColor = Color(0xFF1B8A5A),
            isPlaceholder = false,
            statusBadgeBn = "২.৫% হিসাব"
        ),
        ToolModuleConfig(
            id = "tool_islamic_calendar",
            titleBn = "ইসলামিক ক্যালেন্ডার",
            subtitleBn = "হিজরি, বাংলা ও ইংরেজি",
            icon = Icons.Rounded.CalendarMonth,
            accentColor = Color(0xFFD68608),
            isPlaceholder = false,
            statusBadgeBn = "বহুভাষিক"
        ),
        ToolModuleConfig(
            id = "tool_hadith",
            titleBn = "হাদিস শরীফ",
            subtitleBn = "সিহাহ সিত্তাহ ও বাংলা অনুবাদ",
            icon = Icons.Rounded.AutoStories,
            accentColor = Color(0xFF0B6669),
            isPlaceholder = false,
            statusBadgeBn = "৬ গ্রন্থ"
        ),
        ToolModuleConfig(
            id = "tool_qibla",
            titleBn = "কিবলা কম্পাস",
            subtitleBn = "সেন্সর ও জিপিএস কিবলা দিক",
            icon = Icons.Rounded.Explore,
            accentColor = Color(0xFF04484A),
            isPlaceholder = false,
            statusBadgeBn = "কম্পাস"
        ),
        ToolModuleConfig(
            id = "tool_nearby_mosque",
            titleBn = "নিকটস্থ মসজিদ",
            subtitleBn = "ম্যাপে আশেপাশের মসজিদ",
            icon = Icons.Rounded.LocationOn,
            accentColor = Color(0xFF1B8A5A),
            isPlaceholder = false,
            statusBadgeBn = "গুগল ম্যাপ"
        ),
        ToolModuleConfig(
            id = "tool_tasbih",
            titleBn = "ডিজিটাল তাসবিহ",
            subtitleBn = "তাসবিহ, আমলনামা ও ট্র্যাকার",
            icon = Icons.Rounded.RadioButtonChecked,
            accentColor = Color(0xFFD68608),
            isPlaceholder = false,
            statusBadgeBn = "জিকির"
        )
    )
}
