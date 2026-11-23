package com.example.ui.screens.tasbih

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.DailyPrayerChecklist
import com.example.data.model.PrayerChecklistKeys
import com.example.data.model.PrayerGroupSpec
import com.example.data.model.PrayerRozaCatalog
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.DarkGray
import com.example.ui.theme.IslamicTeal
import com.example.ui.theme.IslamicTealDark
import com.example.ui.theme.ShareGreen
import com.example.ui.theme.SoftMintSurface
import com.example.ui.theme.SoftMintTint
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberSoft
import com.example.util.BengaliFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TasbihPrayersTab(
    selectedDateKey: String,
    formattedDateBn: String,
    isToday: Boolean,
    checklist: DailyPrayerChecklist,
    isLoading: Boolean,
    onShiftDate: (Int) -> Unit,
    onSelectDateKey: (String) -> Unit,
    onToggleItem: (key: String, checked: Boolean) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val totalCompleted = checklist.completedTotalCount()
    val maxItems = PrayerChecklistKeys.allItemKeys.size
    val progress = (totalCompleted.toFloat() / maxItems.toFloat()).coerceIn(0f, 1f)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Date Switcher Card: "❮ আগের দিন", "আজ"/date, "পরের দিন ❯"
        item(key = "date_switcher") {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CleanWhite),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
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
                                .clickable { onShiftDate(-1) }
                                .testTag("prayers_prev_day")
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

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(SoftMintSurface)
                                .clickable { showDatePicker = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("prayers_date_picker_trigger"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarToday,
                                contentDescription = "তারিখ নির্বাচন",
                                tint = IslamicTeal,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isToday) "আজ" else formattedDateBn,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = IslamicTealDark,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SoftMintTint,
                            border = BorderStroke(1.dp, CardBorder),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(50))
                                .clickable { onShiftDate(1) }
                                .testTag("prayers_next_day")
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

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = formattedDateBn,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isLoading) "লোড হচ্ছে..." else "নামাজ ও রোজার অগ্রগতি",
                            style = MaterialTheme.typography.labelMedium,
                            color = DarkGray
                        )
                        Text(
                            text = "${BengaliFormatter.toBengaliNumber(totalCompleted)} / ${BengaliFormatter.toBengaliNumber(maxItems)}",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = IslamicTeal,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        color = ShareGreen,
                        trackColor = SoftMintSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50))
                    )
                }
            }
        }

        // All 7 Prayer & Roza Groups
        items(PrayerRozaCatalog.groups, key = { it.id }) { group ->
            PrayerGroupCard(
                group = group,
                checklist = checklist,
                onToggleItem = onToggleItem
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            onSelectDateKey(sdf.format(Date(millis)))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("নির্বাচন করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("বাতিল")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PrayerGroupCard(
    group: PrayerGroupSpec,
    checklist: DailyPrayerChecklist,
    onToggleItem: (key: String, checked: Boolean) -> Unit
) {
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = group.titleBn,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = IslamicTeal,
                        fontWeight = FontWeight.Bold
                    )
                )
                val doneCount = group.items.count { checklist.isChecked(it.first) }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (group.isRozaGroup) WarmAmberSoft else SoftMintTint
                ) {
                    Text(
                        text = "${BengaliFormatter.toBengaliNumber(doneCount)}/${BengaliFormatter.toBengaliNumber(group.items.size)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = IslamicTealDark,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                group.items.forEach { (key, label) ->
                    val checked = checklist.isChecked(key)
                    val bgColor = when {
                        checked && group.isRozaGroup -> WarmAmberSoft
                        checked -> SoftMintSurface
                        else -> SoftMintTint.copy(alpha = 0.55f)
                    }
                    val borderColor = when {
                        checked && group.isRozaGroup -> WarmAmber
                        checked -> IslamicTeal
                        else -> CardBorder
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = bgColor,
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onToggleItem(key, !checked) }
                            .testTag("prayer_check_$key")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 12.dp, start = 4.dp, top = 2.dp, bottom = 2.dp)
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { onToggleItem(key, it) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = if (group.isRozaGroup) WarmAmber else IslamicTeal,
                                    checkmarkColor = CleanWhite
                                )
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (checked) FontWeight.Bold else FontWeight.Medium,
                                    color = if (checked) IslamicTealDark else DarkGray
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
