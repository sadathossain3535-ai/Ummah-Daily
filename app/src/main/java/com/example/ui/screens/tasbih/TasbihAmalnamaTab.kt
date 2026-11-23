package com.example.ui.screens.tasbih

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.LocalTasbihUser
import com.example.data.model.AmalnamaSummary
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.DarkGray
import com.example.ui.theme.IslamicTeal
import com.example.ui.theme.IslamicTealDark
import com.example.ui.theme.ShareGreen
import com.example.ui.theme.SoftMint
import com.example.ui.theme.SoftMintTint
import com.example.ui.theme.WarmAmber
import com.example.util.AmalnamaCardGenerator
import com.example.util.BengaliFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihAmalnamaTab(
    user: LocalTasbihUser,
    isRangeMode: Boolean,
    summary: AmalnamaSummary?,
    isLoading: Boolean,
    onSetRangeMode: (Boolean) -> Unit,
    onShiftSingleDay: (Int) -> Unit,
    onSelectQuickRange: (Int) -> Unit,
    onSelectCustomRange: (startDate: String, endDate: String) -> Unit
) {
    val context = LocalContext.current
    var showDateRangeDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Toggle: "একদিনের আমল" vs "সাপ্তাহিক/মাসিক"
        item(key = "amalnama_controls") {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CleanWhite),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ModeTogglePill(
                            label = "একদিনের আমল",
                            selected = !isRangeMode,
                            onClick = { onSetRangeMode(false) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("amal_mode_single")
                        )
                        ModeTogglePill(
                            label = "সাপ্তাহিক/মাসিক",
                            selected = isRangeMode,
                            onClick = { onSetRangeMode(true) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("amal_mode_range")
                        )
                    }

                    if (!isRangeMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = SoftMintTint,
                                border = BorderStroke(1.dp, CardBorder),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(50))
                                    .clickable { onShiftSingleDay(-1) }
                            ) {
                                Text(
                                    text = "❮ আগের দিন",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = IslamicTeal,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }

                            Text(
                                text = summary?.titleRangeBn ?: "",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = IslamicTealDark,
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = SoftMintTint,
                                border = BorderStroke(1.dp, CardBorder),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(50))
                                    .clickable { onShiftSingleDay(1) }
                            ) {
                                Text(
                                    text = "পরের দিন ❯",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = IslamicTeal,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onSelectQuickRange(7) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("সাপ্তাহিক", maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = { onSelectQuickRange(30) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("মাসিক", maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = { showDateRangeDialog = true },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("amal_custom_range_button")
                            ) {
                                Icon(Icons.Rounded.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("তারিখ বাছুন", maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        // Deep Teal (#04484A) & Gold (#f9a826) "আমার আমলনামা" Card + Save/Share/Copy Actions
        item(key = "amalnama_card") {
            if (isLoading && summary == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = IslamicTeal)
                }
            } else if (summary != null) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = IslamicTeal),
                    border = BorderStroke(2.dp, WarmAmber),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("amalnama_summary_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📜 আমার আমলনামা",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = WarmAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = user.name.ifBlank { "মুসলিম বান্দা" },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = CleanWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "📍 ${user.resolvedLocation.ifBlank { "বাংলাদেশ" }} • ${summary.titleRangeBn}",
                                style = MaterialTheme.typography.bodySmall.copy(color = SoftMint),
                                textAlign = TextAlign.Center
                            )
                        }

                        HorizontalDivider(color = WarmAmber.copy(alpha = 0.5f))

                        // Prayers & Roza in Days
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CleanWhite.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "🕌 নামাজ ও রোজা (দিন হিসেবে)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = WarmAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            AmalDaysRow("ফজর", summary.fajrDays)
                            AmalDaysRow("যোহর", summary.dhuhrDays)
                            AmalDaysRow("আসর", summary.asrDays)
                            AmalDaysRow("মাগরিব", summary.maghribDays)
                            AmalDaysRow("ইশা", summary.ishaDays)
                            AmalDaysRow("তাহাজ্জুদ", summary.tahajjudDays)
                            AmalDaysRow("রোজা (সাওম)", summary.totalRozaDays)
                        }

                        // Dhikrs in Counts
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CleanWhite.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📿 তাসবিহ ও জিকির",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = WarmAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "মোট ${BengaliFormatter.toBengaliNumber(summary.totalTasbih)} বার",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = SoftMint,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            if (summary.dhikrBreakdown.isEmpty()) {
                                Text(
                                    text = "এই সময়কালে কোনো জিকির রেকর্ড করা হয়নি।",
                                    style = MaterialTheme.typography.bodySmall.copy(color = SoftMint)
                                )
                            } else {
                                summary.dhikrBreakdown.forEach { (title, count) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "• $title",
                                            style = MaterialTheme.typography.bodyMedium.copy(color = CleanWhite),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${BengaliFormatter.toBengaliNumber(count)} বার",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = WarmAmber,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons: Save PNG, Share PNG, Copy Text
                Button(
                    onClick = {
                        val res = AmalnamaCardGenerator.savePngToDownloads(context, user, summary)
                        res.fold(
                            onSuccess = { file ->
                                Toast.makeText(
                                    context,
                                    "আমলনামা সেভ হয়েছে: $file",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            onFailure = {
                                Toast.makeText(
                                    context,
                                    "ছবি সেভ করা যায়নি, শেয়ার ব্যবহার করুন",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShareGreen),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("amalnama_download_png_button")
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = null, tint = CleanWhite)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("আমলনামা সেভ করুন (PNG)", color = CleanWhite, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            AmalnamaCardGenerator.shareAmalnamaPng(context, user, summary)
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("amalnama_share_png_button")
                    ) {
                        Icon(Icons.Rounded.Share, contentDescription = null, tint = IslamicTeal, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("শেয়ার (PNG)", color = IslamicTeal, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val text = AmalnamaCardGenerator.buildFormattedBengaliText(user, summary)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("আমার আমলনামা", text))
                            Toast.makeText(context, "আমলনামা কপি হয়েছে", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("amalnama_copy_text_button")
                    ) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = null, tint = IslamicTeal, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("টেক্সট কপি", color = IslamicTeal, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDateRangeDialog) {
        val rangeState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDateRangeDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val startMs = rangeState.selectedStartDateMillis
                        val endMs = rangeState.selectedEndDateMillis ?: startMs
                        if (startMs != null && endMs != null) {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            onSelectCustomRange(sdf.format(Date(startMs)), sdf.format(Date(endMs)))
                        }
                        showDateRangeDialog = false
                    }
                ) {
                    Text("দেখুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangeDialog = false }) {
                    Text("বাতিল")
                }
            }
        ) {
            DateRangePicker(state = rangeState, modifier = Modifier.height(420.dp))
        }
    }
}

@Composable
private fun AmalDaysRow(label: String, days: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(color = CleanWhite)
        )
        Text(
            text = "${BengaliFormatter.toBengaliNumber(days)} দিন",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = SoftMint,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun ModeTogglePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) IslamicTeal else SoftMintTint,
        border = BorderStroke(1.dp, if (selected) IslamicTeal else CardBorder),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = if (selected) CleanWhite else DarkGray,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
