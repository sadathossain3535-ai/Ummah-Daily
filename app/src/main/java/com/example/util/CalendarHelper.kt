package com.example.util

import android.icu.util.IslamicCalendar
import java.util.Calendar
import java.util.GregorianCalendar

enum class CalendarSystemTab(val titleBn: String) {
    GREGORIAN("ইংরেজি"),
    BENGALI("বাংলা"),
    HIJRI("হিজরি")
}

data class MultiCalendarDateInfo(
    val gregYear: Int,
    val gregMonth: Int,
    val gregDay: Int,
    val weekdayBn: String,
    val hijriYear: Int,
    val hijriMonth: Int,
    val hijriDay: Int,
    val bengaliYear: Int,
    val bengaliMonth: Int, // 1..12
    val bengaliDay: Int,
    val isToday: Boolean
) {
    val hijriFormattedFullBn: String
        get() = "${BengaliFormatter.formatOrdinalDayBn(hijriDay)} ${CalendarHelper.hijriMonthNameBn(hijriMonth)}, ${BengaliFormatter.toBengaliNumber(hijriYear)} হিজরি"

    val bengaliFormattedFullBn: String
        get() = "${BengaliFormatter.formatOrdinalDayBn(bengaliDay)} ${CalendarHelper.bengaliMonthNameBn(bengaliMonth)}, ${BengaliFormatter.toBengaliNumber(bengaliYear)} বঙ্গাব্দ"

    val gregorianFormattedFullBn: String
        get() = "$weekdayBn, ${BengaliFormatter.toBengaliNumber(gregDay)} ${BengaliFormatter.monthNameBn(gregMonth)} ${BengaliFormatter.toBengaliNumber(gregYear)} খ্রিস্টাব্দ"
}

data class CalendarGridCell(
    val dayLabelBn: String,
    val subLabelBn: String,
    val dateInfo: MultiCalendarDateInfo
)

data class CalendarMonthPage(
    val headerTitleBn: String,
    val cells: List<CalendarGridCell?> // Leading nulls align column 0 = শনিবার (Sat) .. column 6 = শুক্রবার (Fri)
)

object CalendarHelper {

    private val banglaMonths = listOf(
        "বৈশাখ", "জ্যৈষ্ঠ", "আষাঢ়", "শ্রাবণ", "ভাদ্র", "আশ্বিন",
        "কার্তিক", "অগ্রহায়ণ", "পৌষ", "মাঘ", "ফাল্গুন", "চৈত্র"
    )

    private val hijriMonths = listOf(
        "মুহাররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি",
        "জমাদিউল আউয়াল", "জমাদিউস সানি", "রজব", "শাবান",
        "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
    )

    val satToFriHeaders = listOf("শনি", "রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র")

    fun bengaliMonthNameBn(month1to12: Int): String =
        banglaMonths.getOrElse(month1to12 - 1) { "" }

    fun hijriMonthNameBn(month1to12: Int): String =
        hijriMonths.getOrElse(month1to12 - 1) { "" }

    fun todayDateInfo(): MultiCalendarDateInfo {
        val now = Calendar.getInstance()
        return fromGregorian(
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH) + 1,
            now.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun fromGregorian(year: Int, month1to12: Int, day: Int): MultiCalendarDateInfo {
        val cal = GregorianCalendar(year, month1to12 - 1, day)
        val today = Calendar.getInstance()
        val isToday = year == today.get(Calendar.YEAR) &&
            (month1to12 - 1) == today.get(Calendar.MONTH) &&
            day == today.get(Calendar.DAY_OF_MONTH)

        // Hijri with -1 day moon-sighting adjustment for Bangladesh
        val adjustedCal = (cal.clone() as GregorianCalendar).apply {
            add(Calendar.DAY_OF_MONTH, -1)
        }
        val islamic = IslamicCalendar(adjustedCal.time).apply {
            calculationType = IslamicCalendar.CalculationType.ISLAMIC_UMALQURA
            timeInMillis = adjustedCal.timeInMillis
        }
        val hijriYear = islamic.get(IslamicCalendar.YEAR)
        val hijriMonth = islamic.get(IslamicCalendar.MONTH) + 1
        val hijriDay = islamic.get(IslamicCalendar.DAY_OF_MONTH)

        // Bangladesh Bangla Academy Revised Calendar (since 2019)
        val (bYear, bMonth, bDay) = gregorianToBangla(year, month1to12, day)

        return MultiCalendarDateInfo(
            gregYear = year,
            gregMonth = month1to12,
            gregDay = day,
            weekdayBn = BengaliFormatter.weekdayNameBn(cal),
            hijriYear = hijriYear,
            hijriMonth = hijriMonth,
            hijriDay = hijriDay,
            bengaliYear = bYear,
            bengaliMonth = bMonth,
            bengaliDay = bDay,
            isToday = isToday
        )
    }

    fun buildMonthPage(
        tab: CalendarSystemTab,
        monthOffset: Int
    ): CalendarMonthPage {
        val now = Calendar.getInstance()
        return when (tab) {
            CalendarSystemTab.GREGORIAN -> {
                val cal = GregorianCalendar(
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    1
                ).apply { add(Calendar.MONTH, monthOffset) }
                val gYear = cal.get(Calendar.YEAR)
                val gMonth = cal.get(Calendar.MONTH) + 1
                val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                val leading = satBasedColumnIndex(cal.get(Calendar.DAY_OF_WEEK))
                val cells = MutableList<CalendarGridCell?>(leading) { null }
                for (d in 1..daysInMonth) {
                    val info = fromGregorian(gYear, gMonth, d)
                    cells.add(
                        CalendarGridCell(
                            dayLabelBn = BengaliFormatter.toBengaliNumber(d),
                            subLabelBn = BengaliFormatter.toBengaliNumber(info.hijriDay),
                            dateInfo = info
                        )
                    )
                }
                CalendarMonthPage(
                    headerTitleBn = "${BengaliFormatter.monthNameBn(gMonth)} ${BengaliFormatter.toBengaliNumber(gYear)}",
                    cells = cells
                )
            }

            CalendarSystemTab.BENGALI -> {
                val todayInfo = todayDateInfo()
                val totalMonths = todayInfo.bengaliYear * 12 + (todayInfo.bengaliMonth - 1) + monthOffset
                val targetYear = Math.floorDiv(totalMonths, 12)
                val targetMonth = Math.floorMod(totalMonths, 12) + 1
                val daysInMonth = banglaMonthLength(targetYear, targetMonth)
                val firstDayGreg = banglaToGregorian(targetYear, targetMonth, 1)
                val leading = satBasedColumnIndex(firstDayGreg.get(Calendar.DAY_OF_WEEK))
                val cells = MutableList<CalendarGridCell?>(leading) { null }
                for (d in 1..daysInMonth) {
                    val g = banglaToGregorian(targetYear, targetMonth, d)
                    val info = fromGregorian(
                        g.get(Calendar.YEAR),
                        g.get(Calendar.MONTH) + 1,
                        g.get(Calendar.DAY_OF_MONTH)
                    )
                    cells.add(
                        CalendarGridCell(
                            dayLabelBn = BengaliFormatter.toBengaliNumber(d),
                            subLabelBn = BengaliFormatter.toBengaliNumber(info.gregDay),
                            dateInfo = info
                        )
                    )
                }
                CalendarMonthPage(
                    headerTitleBn = "${bengaliMonthNameBn(targetMonth)} ${BengaliFormatter.toBengaliNumber(targetYear)} বঙ্গাব্দ",
                    cells = cells
                )
            }

            CalendarSystemTab.HIJRI -> {
                val adjToday = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }
                val baseIslamic = IslamicCalendar().apply {
                    calculationType = IslamicCalendar.CalculationType.ISLAMIC_UMALQURA
                    timeInMillis = adjToday.timeInMillis
                    set(IslamicCalendar.DAY_OF_MONTH, 1)
                    add(IslamicCalendar.MONTH, monthOffset)
                }
                val hYear = baseIslamic.get(IslamicCalendar.YEAR)
                val hMonth = baseIslamic.get(IslamicCalendar.MONTH) + 1
                val daysInMonth = baseIslamic.getActualMaximum(IslamicCalendar.DAY_OF_MONTH)

                // Convert 1st of Hijri month back to civil Gregorian (+1 day for Bangladesh moon-sighting offset)
                val firstCivil = GregorianCalendar().apply {
                    timeInMillis = baseIslamic.timeInMillis
                    add(Calendar.DAY_OF_MONTH, 1)
                }
                val leading = satBasedColumnIndex(firstCivil.get(Calendar.DAY_OF_WEEK))
                val cells = MutableList<CalendarGridCell?>(leading) { null }
                for (d in 1..daysInMonth) {
                    val g = (firstCivil.clone() as GregorianCalendar).apply {
                        add(Calendar.DAY_OF_MONTH, d - 1)
                    }
                    val info = fromGregorian(
                        g.get(Calendar.YEAR),
                        g.get(Calendar.MONTH) + 1,
                        g.get(Calendar.DAY_OF_MONTH)
                    )
                    cells.add(
                        CalendarGridCell(
                            dayLabelBn = BengaliFormatter.toBengaliNumber(d),
                            subLabelBn = BengaliFormatter.toBengaliNumber(info.gregDay),
                            dateInfo = info
                        )
                    )
                }
                CalendarMonthPage(
                    headerTitleBn = "${hijriMonthNameBn(hMonth)} ${BengaliFormatter.toBengaliNumber(hYear)} হিজরি",
                    cells = cells
                )
            }
        }
    }

    private fun satBasedColumnIndex(dayOfWeek: Int): Int = when (dayOfWeek) {
        Calendar.SATURDAY -> 0
        Calendar.SUNDAY -> 1
        Calendar.MONDAY -> 2
        Calendar.TUESDAY -> 3
        Calendar.WEDNESDAY -> 4
        Calendar.THURSDAY -> 5
        Calendar.FRIDAY -> 6
        else -> 0
    }

    private fun isGregorianLeapYear(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

    private fun banglaMonthLength(bengaliYear: Int, month1to12: Int): Int {
        // Falgun falls in Feb-Mar of (bengaliYear + 594)
        val gregYearOfFalgun = bengaliYear + 594
        return when (month1to12) {
            in 1..6 -> 31 // Boishakh through Ashwin = 31 days
            11 -> if (isGregorianLeapYear(gregYearOfFalgun)) 30 else 29 // Falgun
            else -> 30 // Kartik, Agrahayan, Poush, Magh, Chaitra = 30 days
        }
    }

    private fun gregorianToBangla(gYear: Int, gMonth: Int, gDay: Int): Triple<Int, Int, Int> {
        val target = GregorianCalendar(gYear, gMonth - 1, gDay)
        val startYearForBengali = if (gMonth > 4 || (gMonth == 4 && gDay >= 14)) gYear else gYear - 1
        val bYear = startYearForBengali - 593
        val pohelaBoishakh = GregorianCalendar(startYearForBengali, Calendar.APRIL, 14)
        var diffDays = ((target.timeInMillis - pohelaBoishakh.timeInMillis + 43200000L) / 86400000L).toInt()
        var m = 1
        while (m <= 12) {
            val len = banglaMonthLength(bYear, m)
            if (diffDays < len) {
                return Triple(bYear, m, diffDays + 1)
            }
            diffDays -= len
            m++
        }
        return Triple(bYear + 1, 1, 1)
    }

    private fun banglaToGregorian(bYear: Int, bMonth: Int, bDay: Int): GregorianCalendar {
        val gStartYear = bYear + 593
        var offsetDays = bDay - 1
        for (m in 1 until bMonth) {
            offsetDays += banglaMonthLength(bYear, m)
        }
        return GregorianCalendar(gStartYear, Calendar.APRIL, 14).apply {
            add(Calendar.DAY_OF_MONTH, offsetDays)
        }
    }
}
