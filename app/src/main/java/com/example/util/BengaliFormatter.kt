package com.example.util

import java.util.Calendar
import java.util.Locale

object BengaliFormatter {

    private val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun toBengaliDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(bengaliDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toBengaliNumber(number: Int): String = toBengaliDigits(number.toString())

    fun toBengaliNumber(number: Long): String = toBengaliDigits(number.toString())

    fun formatCountdown(totalSeconds: Long): String {
        val clamped = totalSeconds.coerceAtLeast(0L)
        val hours = clamped / 3600
        val minutes = (clamped % 3600) / 60
        val seconds = clamped % 60
        val raw = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        return toBengaliDigits(raw)
    }

    fun format12HourBengali(hour: Int, minute: Int): String {
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val period = when {
            hour < 4 -> "রাত"
            hour < 6 -> "ভোর"
            hour < 12 -> "সকাল"
            hour < 16 -> "দুপুর"
            hour < 18 -> "বিকাল"
            hour < 20 -> "সন্ধ্যা"
            else -> "রাত"
        }
        val raw = String.format(Locale.US, "%02d:%02d", displayHour, minute)
        return "$period ${toBengaliDigits(raw)}"
    }

    fun formatShort12HourBengali(hour: Int, minute: Int): String {
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val raw = String.format(Locale.US, "%02d:%02d", displayHour, minute)
        return toBengaliDigits(raw)
    }

    fun monthNameBn(monthNumber: Int): String = when (monthNumber) {
        1 -> "জানুয়ারি"
        2 -> "ফেব্রুয়ারি"
        3 -> "মার্চ"
        4 -> "এপ্রিল"
        5 -> "মে"
        6 -> "জুন"
        7 -> "জুলাই"
        8 -> "আগস্ট"
        9 -> "সেপ্টেম্বর"
        10 -> "অক্টোবর"
        11 -> "নভেম্বর"
        12 -> "ডিসেম্বর"
        else -> ""
    }

    fun weekdayNameBn(weekdayEn: String?): String = when (weekdayEn?.lowercase(Locale.US)) {
        "saturday" -> "শনিবার"
        "sunday" -> "রবিবার"
        "monday" -> "সোমবার"
        "tuesday" -> "মঙ্গলবার"
        "wednesday" -> "বুধবার"
        "thursday" -> "বৃহস্পতিবার"
        "friday" -> "শুক্রবার"
        else -> ""
    }

    fun weekdayNameBn(cal: Calendar): String = when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SATURDAY -> "শনিবার"
        Calendar.SUNDAY -> "রবিবার"
        Calendar.MONDAY -> "সোমবার"
        Calendar.TUESDAY -> "মঙ্গলবার"
        Calendar.WEDNESDAY -> "বুধবার"
        Calendar.THURSDAY -> "বৃহস্পতিবার"
        Calendar.FRIDAY -> "শুক্রবার"
        else -> ""
    }

    fun formatShortOccurrenceDateBn(daysOffset: Int): String {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, daysOffset) }
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = monthNameBn(cal.get(Calendar.MONTH) + 1)
        val prefix = if (daysOffset == 0) "আজ" else "আগামীকাল"
        return "$prefix, ${toBengaliNumber(day)} $month"
    }

    fun formatGregorianDateBn(day: Int, monthNumber: Int, year: Int, weekdayEn: String?): String {
        val monthBn = monthNameBn(monthNumber)
        val weekdayBn = weekdayNameBn(weekdayEn)
        val dateStr = "${toBengaliNumber(day)} $monthBn ${toBengaliNumber(year)}"
        return if (weekdayBn.isNotEmpty()) "$weekdayBn, $dateStr" else dateStr
    }

    fun formatHijriDateBn(day: String, monthNumber: Int, year: String): String {
        val hijriMonthBn = when (monthNumber) {
            1 -> "মুহাররম"
            2 -> "সফর"
            3 -> "রবিউল আউয়াল"
            4 -> "রবিউস সানি"
            5 -> "জমাদিউল আউয়াল"
            6 -> "জমাদিউস সানি"
            7 -> "রজব"
            8 -> "শাবান"
            9 -> "রমজান"
            10 -> "শাওয়াল"
            11 -> "জিলকদ"
            12 -> "জিলহজ"
            else -> ""
        }
        return "${toBengaliDigits(day)} $hijriMonthBn ${toBengaliDigits(year)} হিজরি"
    }

    fun formatOrdinalDayBn(day: Int): String {
        val suffix = when {
            day == 1 -> "লা"
            day == 2 || day == 3 -> "রা"
            day == 4 -> "ঠা"
            day in 5..18 -> "ই"
            else -> "শে"
        }
        return "${toBengaliNumber(day)}$suffix"
    }

    fun formatCurrencyBn(amount: Double): String {
        val rounded = kotlin.math.round(amount * 100.0) / 100.0
        val whole = rounded.toLong()
        val frac = kotlin.math.round((rounded - whole) * 100).toInt()
        val formattedWhole = formatSouthAsianGrouping(whole)
        val digits = if (frac > 0) {
            "$formattedWhole.${String.format(Locale.US, "%02d", frac)}"
        } else {
            formattedWhole
        }
        return "৳ ${toBengaliDigits(digits)}"
    }

    private fun formatSouthAsianGrouping(value: Long): String {
        if (value < 0L) return "-" + formatSouthAsianGrouping(-value)
        val s = value.toString()
        if (s.length <= 3) return s
        val lastThree = s.takeLast(3)
        var rest = s.dropLast(3)
        val sb = StringBuilder()
        while (rest.length > 2) {
            sb.insert(0, "," + rest.takeLast(2))
            rest = rest.dropLast(2)
        }
        if (rest.isNotEmpty()) {
            sb.insert(0, rest)
        }
        return sb.toString() + "," + lastThree
    }
}
