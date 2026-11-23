package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.local.LocalTasbihUser
import com.example.data.model.AmalnamaSummary
import java.io.File
import java.io.FileOutputStream

object AmalnamaCardGenerator {

    fun buildFormattedBengaliText(user: LocalTasbihUser, summary: AmalnamaSummary): String {
        val sb = StringBuilder()
        sb.appendLine("📜 আমার আমলনামা — UMMAH DAILY")
        sb.appendLine("👤 ${user.name.ifBlank { "মুসলিম বান্দা" }} (📍 ${user.resolvedLocation.ifBlank { "বাংলাদেশ" }})")
        sb.appendLine("🗓️ সময়কাল: ${summary.titleRangeBn}")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("🕌 নামাজ ও রোজা (দিন হিসেবে):")
        sb.appendLine("   • ফজর: ${BengaliFormatter.toBengaliNumber(summary.fajrDays)} দিন")
        sb.appendLine("   • যোহর: ${BengaliFormatter.toBengaliNumber(summary.dhuhrDays)} দিন")
        sb.appendLine("   • আসর: ${BengaliFormatter.toBengaliNumber(summary.asrDays)} দিন")
        sb.appendLine("   • মাগরিব: ${BengaliFormatter.toBengaliNumber(summary.maghribDays)} দিন")
        sb.appendLine("   • ইশা: ${BengaliFormatter.toBengaliNumber(summary.ishaDays)} দিন")
        sb.appendLine("   • তাহাজ্জুদ: ${BengaliFormatter.toBengaliNumber(summary.tahajjudDays)} দিন")
        sb.appendLine("   • রোজা (সাওম): ${BengaliFormatter.toBengaliNumber(summary.totalRozaDays)} দিন")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("📿 তাসবিহ ও জিকির (মোট: ${BengaliFormatter.toBengaliNumber(summary.totalTasbih)} বার):")
        if (summary.dhikrBreakdown.isEmpty()) {
            sb.appendLine("   • এই সময়কালে কোনো জিকির রেকর্ড হয়নি")
        } else {
            summary.dhikrBreakdown.forEach { (name, count) ->
                sb.appendLine("   • $name: ${BengaliFormatter.toBengaliNumber(count)} বার")
            }
        }
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("— UMMAH DAILY")
        return sb.toString()
    }

    fun generateAmalnamaBitmap(
        context: Context,
        user: LocalTasbihUser,
        summary: AmalnamaSummary
    ): Bitmap {
        val width = 1080
        val height = 1600
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val headingTypeface = ResourcesCompat.getFont(context, R.font.anek_bangla) ?: Typeface.DEFAULT_BOLD
        val bodyTypeface = ResourcesCompat.getFont(context, R.font.noto_sans_bengali) ?: Typeface.DEFAULT

        // Deep Teal Background
        val tealPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                0xFF023133.toInt(), 0xFF04484A.toInt(),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), tealPaint)

        // Gold Border Frame
        val goldBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF9A826.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawRoundRect(RectF(32f, 32f, width - 32f, height - 32f), 36f, 36f, goldBorderPaint)

        val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF9A826.toInt()
            textSize = 46f
            typeface = Typeface.create(headingTypeface, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("📜 আমার আমলনামা", width / 2f, 118f, goldPaint)

        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 48f
            typeface = Typeface.create(headingTypeface, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(user.name.ifBlank { "মুসলিম বান্দা" }, width / 2f, 192f, namePaint)

        val locPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFCEE6DE.toInt()
            textSize = 30f
            typeface = bodyTypeface
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("📍 ${user.resolvedLocation.ifBlank { "বাংলাদেশ" }}  •  🗓️ ${summary.titleRangeBn}", width / 2f, 246f, locPaint)

        val goldLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF9A826.toInt()
            strokeWidth = 3f
        }
        canvas.drawLine(80f, 286f, width - 80f, 286f, goldLinePaint)

        // Section 1: Prayer & Roza Summary (in Days)
        val panelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x1AFFFFFF }
        val panelBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x55CEE6DE
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        val prayerPanel = RectF(64f, 316f, width - 64f, 770f)
        canvas.drawRoundRect(prayerPanel, 28f, 28f, panelPaint)
        canvas.drawRoundRect(prayerPanel, 28f, 28f, panelBorder)

        val secTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF9A826.toInt()
            textSize = 36f
            typeface = Typeface.create(headingTypeface, Typeface.BOLD)
        }
        canvas.drawText("🕌 নামাজ ও রোজা (দিন হিসেবে)", 96f, 378f, secTitlePaint)

        val itemNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 32f
            typeface = bodyTypeface
        }
        val itemValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFCEE6DE.toInt()
            textSize = 34f
            typeface = Typeface.create(headingTypeface, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        val prayerRows = listOf(
            "ফজর" to "${BengaliFormatter.toBengaliNumber(summary.fajrDays)} দিন",
            "যোহর" to "${BengaliFormatter.toBengaliNumber(summary.dhuhrDays)} দিন",
            "আসর" to "${BengaliFormatter.toBengaliNumber(summary.asrDays)} দিন",
            "মাগরিব" to "${BengaliFormatter.toBengaliNumber(summary.maghribDays)} দিন",
            "ইশা" to "${BengaliFormatter.toBengaliNumber(summary.ishaDays)} দিন",
            "তাহাজ্জুদ" to "${BengaliFormatter.toBengaliNumber(summary.tahajjudDays)} দিন",
            "রোজা (সাওম)" to "${BengaliFormatter.toBengaliNumber(summary.totalRozaDays)} দিন"
        )

        // Two-column grid inside Prayer Panel
        prayerRows.forEachIndexed { idx, (label, value) ->
            val col = idx % 2
            val row = idx / 2
            val xStart = if (col == 0) 96f else width / 2f + 30f
            val xEnd = if (col == 0) width / 2f - 30f else width - 96f
            val y = 446f + row * 82f
            canvas.drawText(label, xStart, y, itemNamePaint)
            canvas.drawText(value, xEnd, y, itemValuePaint)
        }

        // Section 2: Dhikr Summary (in Counts)
        val dhikrPanel = RectF(64f, 804f, width - 64f, 1464f)
        canvas.drawRoundRect(dhikrPanel, 28f, 28f, panelPaint)
        canvas.drawRoundRect(dhikrPanel, 28f, 28f, panelBorder)

        canvas.drawText(
            "📿 জিকির ও তাসবিহ (মোট: ${BengaliFormatter.toBengaliNumber(summary.totalTasbih)} বার)",
            96f,
            866f,
            secTitlePaint
        )

        val dhikrs = summary.dhikrBreakdown.take(6)
        if (dhikrs.isEmpty()) {
            canvas.drawText("এই সময়কালে কোনো জিকির রেকর্ড করা হয়নি।", 96f, 948f, itemNamePaint)
        } else {
            var y = 944f
            val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0x28CEE6DE
                strokeWidth = 2f
            }
            dhikrs.forEachIndexed { idx, (title, count) ->
                val trimmed = if (title.length > 26) title.take(25) + "…" else title
                canvas.drawText("• $trimmed", 96f, y, itemNamePaint)
                canvas.drawText(
                    "${BengaliFormatter.toBengaliNumber(count)} বার",
                    width - 96f,
                    y,
                    itemValuePaint
                )
                if (idx < dhikrs.lastIndex) {
                    canvas.drawLine(96f, y + 26f, width - 96f, y + 26f, dividerPaint)
                }
                y += 82f
            }
        }

        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFCEE6DE.toInt()
            textSize = 26f
            textAlign = Paint.Align.CENTER
            typeface = bodyTypeface
        }
        canvas.drawText("UMMAH DAILY • আল্লাহ আপনার ইবাদত কবুল করুন", width / 2f, 1530f, footerPaint)

        return bitmap
    }

    fun savePngToDownloads(
        context: Context,
        user: LocalTasbihUser,
        summary: AmalnamaSummary
    ): Result<String> {
        return try {
            val bitmap = generateAmalnamaBitmap(context, user, summary)
            val fileName = "UmmahDaily_Amalnama_${summary.startDate}.png"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/UmmahDaily")
                }
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: throw IllegalStateException("MediaStore URI null")
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            } else {
                val dir = File(context.cacheDir, "shared_cards").apply { mkdirs() }
                val file = File(dir, fileName)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            }
            Result.success(fileName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun shareAmalnamaPng(
        context: Context,
        user: LocalTasbihUser,
        summary: AmalnamaSummary
    ): Result<Unit> {
        return try {
            val bitmap = generateAmalnamaBitmap(context, user, summary)
            val cacheDir = File(context.cacheDir, "shared_cards").apply { mkdirs() }
            val imageFile = File(cacheDir, "ummah_daily_amalnama.png")
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
                putExtra(Intent.EXTRA_TEXT, buildFormattedBengaliText(user, summary))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "আমলনামা শেয়ার করুন").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
