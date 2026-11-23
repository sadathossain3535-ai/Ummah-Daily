package com.example.ui.screens.tools

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AnekBanglaFontFamily
import com.example.ui.theme.AppBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.DarkGray
import com.example.ui.theme.IslamicTeal
import com.example.ui.theme.IslamicTealDark
import com.example.ui.theme.SoftMint
import com.example.ui.theme.SoftMintSurface
import com.example.ui.theme.SoftMintTint
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberSoft
import com.example.util.CalendarHelper
import com.example.util.CalendarSystemTab

@Composable
fun IslamicCalendarScreen(
    onBack: () -> Unit,
    contentBottomPadding: PaddingValues
) {
    BackHandler(onBack = onBack)

    val todayInfo = remember { CalendarHelper.todayDateInfo() }
    var activeTab by remember { mutableStateOf(CalendarSystemTab.HIJRI) }
    var monthOffset by remember { mutableIntStateOf(0) }
    var selectedDateInfo by remember { mutableStateOf(todayInfo) }

    val monthPage = remember(activeTab, monthOffset) {
        CalendarHelper.buildMonthPage(activeTab, monthOffset)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp))
                    .background(IslamicTeal)
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("calendar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "ফিরে যান",
                        tint = CleanWhite
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "ইসলামিক ও বহুভাষিক ক্যালেন্ডার",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = CleanWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "হিজরি (বাংলাদেশ চাঁদ দেখা -১ দিন), বঙ্গাব্দ ও খ্রিস্টাব্দ",
                        style = MaterialTheme.typography.bodySmall.copy(color = SoftMint)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .padding(bottom = contentBottomPadding.calculateBottomPadding() + 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Today / Selected Summary Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = IslamicTeal),
                    border = BorderStroke(1.5.dp, WarmAmber),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("calendar_summary_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = WarmAmberSoft
                            ) {
                                Text(
                                    text = if (selectedDateInfo.isToday) "আজকের তারিখ" else "নির্বাচিত তারিখ",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = IslamicTealDark,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            if (!selectedDateInfo.isToday || monthOffset != 0) {
                                Text(
                                    text = "আজকে ফিরুন",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = WarmAmber,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            monthOffset = 0
                                            selectedDateInfo = todayInfo
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = "🌙 ${selectedDateInfo.hijriFormattedFullBn}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = WarmAmber,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "🇧🇩 ${selectedDateInfo.bengaliFormattedFullBn}",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = CleanWhite,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "📅 ${selectedDateInfo.gregorianFormattedFullBn}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = SoftMint)
                        )
                    }
                }

                // Calendar System Tabs: ইংরেজি | বাংলা | হিজরি
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CleanWhite,
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CalendarSystemTab.entries.forEach { tab ->
                            val selected = activeTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) IslamicTeal else SoftMintTint)
                                    .clickable {
                                        activeTab = tab
                                        monthOffset = 0
                                    }
                                    .padding(vertical = 10.dp)
                                    .testTag("calendar_tab_${tab.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab.titleBn,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = if (selected) CleanWhite else IslamicTealDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                // Interactive Monthly Calendar Grid
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanWhite),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Prev / Month Title / Next
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = SoftMintTint,
                                border = BorderStroke(1.dp, CardBorder),
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .clickable { monthOffset -= 1 }
                                    .testTag("calendar_prev_month")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "❮",
                                        color = IslamicTeal,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = monthPage.headerTitleBn,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = IslamicTealDark,
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )

                            Surface(
                                shape = CircleShape,
                                color = SoftMintTint,
                                border = BorderStroke(1.dp, CardBorder),
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .clickable { monthOffset += 1 }
                                    .testTag("calendar_next_month")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "❯",
                                        color = IslamicTeal,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Sat-Fri Headers
                        Row(modifier = Modifier.fillMaxWidth()) {
                            CalendarHelper.satToFriHeaders.forEachIndexed { idx, dayTitle ->
                                Text(
                                    text = dayTitle,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (idx == 6) WarmAmber else TextSecondary,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Grid Rows (7 columns)
                        val rows = monthPage.cells.chunked(7)
                        rows.forEach { week ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (col in 0 until 7) {
                                    val cell = week.getOrNull(col)
                                    if (cell == null) {
                                        Spacer(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(0.95f)
                                        )
                                    } else {
                                        val isToday = cell.dateInfo.isToday
                                        val isSelected = cell.dateInfo.gregYear == selectedDateInfo.gregYear &&
                                            cell.dateInfo.gregMonth == selectedDateInfo.gregMonth &&
                                            cell.dateInfo.gregDay == selectedDateInfo.gregDay

                                        val bgColor = when {
                                            isToday -> IslamicTeal
                                            isSelected -> SoftMint
                                            else -> Color.Transparent
                                        }
                                        val borderColor = when {
                                            isToday -> WarmAmber
                                            isSelected -> IslamicTeal
                                            else -> Color.Transparent
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = bgColor,
                                            border = if (borderColor != Color.Transparent) {
                                                BorderStroke(1.5.dp, borderColor)
                                            } else {
                                                null
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(0.95f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { selectedDateInfo = cell.dateInfo }
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(2.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = cell.dayLabelBn,
                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                        fontFamily = AnekBanglaFontFamily,
                                                        color = when {
                                                            isToday -> CleanWhite
                                                            col == 6 -> WarmAmber
                                                            else -> DarkGray
                                                        },
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                                Text(
                                                    text = cell.subLabelBn,
                                                    fontSize = 10.sp,
                                                    color = if (isToday) SoftMint else TextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
