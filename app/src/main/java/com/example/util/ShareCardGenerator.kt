package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.PrayerScheduleState
import java.io.File
import java.io.FileOutputStream

object ShareCardGenerator {

    fun buildFormattedBengaliText(schedule: PrayerScheduleState): String {
        val sb = StringBuilder()
        sb.appendLine("🕌 UMMAH DAILY — আজকের নামাজের সময়সূচী")
        sb.appendLine("📍 জেলা: ${schedule.cityNameBn}, বাংলাদেশ")
        sb.appendLine("📆 বার: ${schedule.dayNameBn}")
        sb.appendLine("📅 ${schedule.gregorianDateBn}")
        sb.appendLine("🌙 ${schedule.hijriDateBn}")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        schedule.fardPrayers.forEach { prayer ->
            sb.appendLine("• ${prayer.nameBn}: ${prayer.startTimeBn}")
        }
        sb.appendLine("• তাহাজ্জুদ: ${schedule.tahajjudTimeBn}")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("🌙 সেহরি শেষ: ${schedule.status.sehriEndTimeBn}")
        sb.appendLine("🍽️ ইফতার শুরু: ${schedule.status.iftarTimeBn}")
        sb.appendLine("🌅 সূর্যোদয়: ${schedule.sunriseBn}")
        sb.appendLine("🌇 সূর্যাস্ত: ${schedule.sunsetBn}")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("🕌 নামাজ কায়েম করুন")
        sb.appendLine("— UMMAH DAILY NEWS App")
        return sb.toString()
    }

    fun generateScheduleBitmap(context: Context, schedule: PrayerScheduleState): Bitmap {
        val width = 1080
        val height = 1560
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val headingTypeface = ResourcesCompat.getFont(context, R.font.anek_bangla) ?: Typeface.DEFAULT_BOLD
        val bodyTypeface = ResourcesCompat.getFont(context, R.font.noto_sans_bengali) ?: Typeface.DEFAULT

        // Clean White Background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Top Deep Teal Header (#04484A)
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF04484A.toInt() }
        canvas.drawRoundRect(RectF(40f, 40f, width - 40f, 360f), 36f, 36f, headerPaint)

        // UMMAH DAILY Golden Amber Badge
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFF9A826.toInt() }
        canvas.drawRoundRect(RectF(80f, 82f, 390f, 138f), 28f, 28f, badgePaint)
        val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF04484A.toInt()
            textSize = 30f
            typeface = Typeface.create(headingTypeface, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("UMMAH DAILY", 235f, 120f, badgeTextPaint)

        // City Pill Top Right
        val cityPillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFCEE6DE.toInt() }
        canvas.drawRoundRect(RectF(width - 420f, 82f, width - 80f, 138f), 28f, 28f, cityPillPaint)
        val cityTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF04484A.toInt()
            textSize = 28f
            typeface = Typeface.create(headingTypeface, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("📍 ${schedule.cityNameBn}, বাংলাদেশ", width - 250f, 119f, cityTextPaint)

        // Day Name + Gregorian + Hijri
        val dayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 46f
            typeface = Typeface.create(headingTypeface, Typeface.BOLD)
        }
        canvas.drawText("${schedule.dayNameBn} • নামাজের সময়সূচী", 80f, 206f, dayPaint)

        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFCEE6DE.toInt()
            textSize = 30f
            typeface = bodyTypeface
        }
        canvas.drawText(schedule.gregorianDateBn, 80f, 262f, datePaint)
        canvas.drawText(schedule.hijriDateBn, 80f, 312f, datePaint)

        // Table Card for 5 Farz + Tahajjud
        val tableCardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFF9FCFB.toInt() }
        val tableBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFCEE6DE.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        val tableRect = RectF(40f, 396f, width - 40f, 1086f)
        canvas.drawRoundRect(tableRect, 32f, 32f, tableCardPaint)
        canvas.drawRoundRect(tableRect, 32f, 32f, tableBorderPaint)

        // Table Header Row
        val tableHeadPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFCEE6DE.toInt() }
        canvas.drawRoundRect(RectF(40f, 396f, width - 40f, 476f), 28f, 28f, tableHeadPaint)
        val thPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF04484A.toInt()
            textSize = 32f
            typeface = Typeface.create(headingTypeface, Typeface.BOLD)
        }
        val thRightPaint = Paint(thPaint).apply { textAlign = Paint.Align.RIGHT }
        canvas.drawText("ওয়াক্ত", 84f, 448f, thPaint)
        canvas.drawText("সময় শুরু", width - 84f, 448f, thRightPaint)

        val rows = schedule.fardPrayers.map {
            Triple(it.nameBn, it.startTimeBn, it.isCurrentActive)
        } + listOf(Triple("তাহাজ্জুদ", schedule.tahajjudTimeBn, false))

        val rowTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF0B2526.toInt()
            textSize = 38f
            typeface = Typeface.create(headingTypeface, Typeface.BOLD)
        }
        val rowTimePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF04484A.toInt()
            textSize = 38f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(headingTypeface, Typeface.BOLD)
        }
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFCEE6DE.toInt()
            strokeWidth = 2f
        }

        var yPos = 552f
        rows.forEachIndexed { idx, (name, time, isActive) ->
            if (isActive) {
                val hlPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFCEE6DE.toInt() }
                canvas.drawRoundRect(RectF(60f, yPos - 48f, width - 60f, yPos + 24f), 18f, 18f, hlPaint)
            }
            canvas.drawText(name, 88f, yPos, rowTitlePaint)
            canvas.drawText(time, width - 88f, yPos, rowTimePaint)
            if (idx < rows.lastIndex) {
                canvas.drawLine(76f, yPos + 34f, width - 76f, yPos + 34f, dividerPaint)
            }
            yPos += 96f
        }

        // 2x2 Grid for Sehri / Iftar / Sunrise / Sunset
        val boxBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFCEE6DE.toInt() }
        val boxLabel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF04484A.toInt()
            textSize = 28f
            typeface = bodyTypeface
        }
        val boxValue = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF04484A.toInt()
            textSize = 38f
            typeface = Typeface.create(headingTypeface, Typeface.BOLD)
        }

        val gridItems = listOf(
            "🌙 সেহরি শেষ" to schedule.status.sehriEndTimeBn,
            "🍽️ ইফতার শুরু" to schedule.status.iftarTimeBn,
            "🌅 সূর্যোদয়" to schedule.sunriseBn,
            "🌇 সূর্যাস্ত" to schedule.sunsetBn
        )

        val cellW = (width - 104f) / 2f
        val cellH = 148f
        gridItems.forEachIndexed { index, (label, value) ->
            val col = index % 2
            val row = index / 2
            val left = 40f + col * (cellW + 24f)
            val top = 1118f + row * (cellH + 20f)
            canvas.drawRoundRect(RectF(left, top, left + cellW, top + cellH), 26f, 26f, boxBg)
            canvas.drawText(label, left + 28f, top + 54f, boxLabel)
            canvas.drawText(value, left + 28f, top + 110f, boxValue)
        }

        // Footer: 🕌 নামাজ কায়েম করুন & UMMAH DAILY NEWS App
        val footerBannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF04484A.toInt() }
        canvas.drawRoundRect(RectF(40f, 1450f, width - 40f, 1524f), 24f, 24f, footerBannerPaint)

        val footerLeftPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF9A826.toInt()
            textSize = 30f
            typeface = Typeface.create(headingTypeface, Typeface.BOLD)
        }
        val footerRightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFCEE6DE.toInt()
            textSize = 26f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(headingTypeface, Typeface.BOLD)
        }
        canvas.drawText("🕌 নামাজ কায়েম করুন", 76f, 1498f, footerLeftPaint)
        canvas.drawText("UMMAH DAILY NEWS App", width - 76f, 1497f, footerRightPaint)

        return bitmap
    }

    fun shareSchedulePng(context: Context, schedule: PrayerScheduleState): Result<Unit> {
        return try {
            val bitmap = generateScheduleBitmap(context, schedule)
            val cacheDir = File(context.cacheDir, "shared_cards").apply { mkdirs() }
            val imageFile = File(cacheDir, "ummah_daily_schedule.png")
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, buildFormattedBengaliText(schedule))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(
                intent,
                context.getString(R.string.share_schedule_chooser)
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun saveSchedulePngToGallery(context: Context, schedule: PrayerScheduleState): Result<String> {
        return try {
            val bitmap = generateScheduleBitmap(context, schedule)
            val fileName = "UmmahDaily_Prayer_${System.currentTimeMillis()}.png"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/UmmahDaily")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return Result.failure(IllegalStateException("গ্যালারিতে ফাইল তৈরি করা যায়নি"))
                resolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                Result.success("গ্যালারিতে সংরক্ষিত হয়েছে (Pictures/UmmahDaily)")
            } else {
                val dir = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.cacheDir,
                    "UmmahDaily"
                ).apply { mkdirs() }
                val outFile = File(dir, fileName)
                FileOutputStream(outFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                Result.success("ছবি সংরক্ষিত হয়েছে: ${outFile.name}")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun shareFormattedText(context: Context, text: String, chooserTitle: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(intent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
