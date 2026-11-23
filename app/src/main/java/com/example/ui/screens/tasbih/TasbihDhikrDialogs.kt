package com.example.ui.screens.tasbih

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TasbihDhikrItem
import com.example.data.model.WaqtType
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.AppBackground
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

@Composable
fun FullDuaDialog(
    activeDhikr: TasbihDhikrItem,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = CleanWhite),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tasbih_full_dua_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = activeDhikr.titleBn,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = IslamicTeal,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SoftMintTint, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = activeDhikr.arabic,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = AmiriFontFamily,
                            fontSize = 24.sp,
                            lineHeight = 40.sp,
                            color = IslamicTealDark
                        ),
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "উচ্চারণ:",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = IslamicTeal,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = activeDhikr.pronunciationBn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkGray
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "অর্থ:",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = IslamicTeal,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = activeDhikr.meaningBn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "ফজিলত ও রেফারেন্স:",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = WarmAmber,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = activeDhikr.virtueBn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkGray
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicTeal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("tasbih_full_dua_close_button")
                ) {
                    Text("বন্ধ করুন", color = CleanWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DhikrSelectorSheetContent(
    currentWaqt: WaqtType,
    allDhikrs: List<TasbihDhikrItem>,
    perDhikrCounts: Map<String, Int>,
    activeDhikrId: String,
    onSelectDhikr: (TasbihDhikrItem) -> Unit,
    onAddCustomDhikr: (title: String) -> Unit
) {
    var customInput by remember { mutableStateOf("") }

    val waqtDhikrs = remember(currentWaqt, allDhikrs) {
        allDhikrs.filter { it.waqtTags.contains(currentWaqt.name) }
    }
    val regularDhikrs = remember(currentWaqt, allDhikrs) {
        allDhikrs.filterNot { it.waqtTags.contains(currentWaqt.name) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "sheet_header") {
            Text(
                text = "জিকির নির্বাচন করুন",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = IslamicTeal,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        if (waqtDhikrs.isNotEmpty()) {
            item(key = "waqt_section") {
                Text(
                    text = "⚡ ${currentWaqt.titleBn}-এর সুন্নাত",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = IslamicTeal,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            items(waqtDhikrs, key = { "waqt_${it.id}" }) { dhikr ->
                val count = perDhikrCounts[dhikr.id] ?: 0
                DhikrSheetRow(
                    dhikr = dhikr,
                    currentCount = count,
                    selected = dhikr.id == activeDhikrId,
                    onClick = { onSelectDhikr(dhikr) }
                )
            }

            item(key = "divider_regular") {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = CardBorder)
            }
        }

        item(key = "regular_section") {
            Text(
                text = "📿 সাধারণ জিকির",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = IslamicTeal,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        items(regularDhikrs, key = { "reg_${it.id}" }) { dhikr ->
            val count = perDhikrCounts[dhikr.id] ?: 0
            DhikrSheetRow(
                dhikr = dhikr,
                currentCount = count,
                selected = dhikr.id == activeDhikrId,
                onClick = { onSelectDhikr(dhikr) }
            )
        }

        // Bottom Add Custom Dhikr Input
        item(key = "custom_dhikr_bottom_input") {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customInput,
                    onValueChange = { customInput = it },
                    placeholder = { Text("নতুন জিকির যোগ করুন...") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IslamicTeal,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("custom_dhikr_title_input")
                )
                Button(
                    onClick = {
                        if (customInput.isNotBlank()) {
                            onAddCustomDhikr(customInput.trim())
                            customInput = ""
                        }
                    },
                    enabled = customInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicTeal),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .height(52.dp)
                        .testTag("save_custom_dhikr_button")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "যোগ করুন", tint = CleanWhite)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("যোগ", color = CleanWhite, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DhikrSheetRow(
    dhikr: TasbihDhikrItem,
    currentCount: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val isDone = dhikr.defaultTarget > 0 && currentCount >= dhikr.defaultTarget
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) SoftMintSurface else AppBackground,
        border = BorderStroke(1.dp, if (selected) IslamicTeal else CardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("dhikr_option_${dhikr.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = dhikr.titleBn,
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = if (selected) IslamicTealDark else DarkGray,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "সম্পন্ন",
                            tint = ShareGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = WarmAmberSoft
                ) {
                    Text(
                        text = "${BengaliFormatter.toBengaliNumber(currentCount)} / ${BengaliFormatter.toBengaliNumber(dhikr.defaultTarget)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = IslamicTealDark,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dhikr.arabic,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = AmiriFontFamily,
                    fontSize = 18.sp,
                    color = IslamicTeal
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
