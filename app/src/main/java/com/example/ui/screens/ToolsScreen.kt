package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.AppConfigRegistry
import com.example.config.AppTabId
import com.example.config.ToolModuleConfig
import com.example.ui.screens.tools.AsmaUlHusnaScreen
import com.example.ui.screens.tools.HadithBooksScreen
import com.example.ui.screens.tools.IslamicCalendarScreen
import com.example.ui.screens.tools.ZakatCalculatorScreen
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

private enum class ToolsSubScreen {
    GRID,
    ASMA_UL_HUSNA,
    ZAKAT,
    CALENDAR,
    HADITH,
    QIBLA
}

private fun toolEmojiFor(toolId: String): String = when (toolId) {
    "tool_quran" -> "📖"
    "tool_99_names" -> "ﷲ"
    "tool_zakat" -> "💰"
    "tool_islamic_calendar" -> "📅"
    "tool_hadith" -> "📚"
    "tool_qibla" -> "🕋"
    "tool_nearby_mosque" -> "🕌"
    "tool_tasbih" -> "📿"
    else -> "🛠️"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    contentBottomPadding: PaddingValues,
    selectedCityApi: String,
    selectedCityNameBn: String,
    onNavigateToTab: (AppTabId) -> Unit
) {
    var activeSubScreen by remember { mutableStateOf(ToolsSubScreen.GRID) }
    var showQuranChooserSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    when (activeSubScreen) {
        ToolsSubScreen.ASMA_UL_HUSNA -> {
            AsmaUlHusnaScreen(
                onBack = { activeSubScreen = ToolsSubScreen.GRID },
                contentBottomPadding = contentBottomPadding
            )
            return
        }
        ToolsSubScreen.ZAKAT -> {
            ZakatCalculatorScreen(
                onBack = { activeSubScreen = ToolsSubScreen.GRID },
                contentBottomPadding = contentBottomPadding
            )
            return
        }
        ToolsSubScreen.CALENDAR -> {
            IslamicCalendarScreen(
                onBack = { activeSubScreen = ToolsSubScreen.GRID },
                contentBottomPadding = contentBottomPadding
            )
            return
        }
        ToolsSubScreen.HADITH -> {
            HadithBooksScreen(
                onBack = { activeSubScreen = ToolsSubScreen.GRID },
                contentBottomPadding = contentBottomPadding
            )
            return
        }
        ToolsSubScreen.QIBLA -> {
            QiblaCompassScreen(
                selectedCityApi = selectedCityApi,
                selectedCityNameBn = selectedCityNameBn,
                onBack = { activeSubScreen = ToolsSubScreen.GRID },
                contentBottomPadding = contentBottomPadding
            )
            return
        }
        ToolsSubScreen.GRID -> Unit
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(IslamicTealDark, IslamicTeal)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Column {
                    Text(
                        text = "🛠️ টুলস ও ফিচারসমূহ",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = CleanWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "দৈনন্দিন ইসলামিক আমল ও প্রয়োজনীয় সহায়ক টুলস",
                        style = MaterialTheme.typography.bodySmall.copy(color = SoftMint)
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = contentBottomPadding.calculateBottomPadding() + 22.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(AppConfigRegistry.toolsGridConfig, key = { it.id }) { tool ->
                    ToolFeatureGridCard(
                        tool = tool,
                        emoji = toolEmojiFor(tool.id),
                        onClick = {
                            when (tool.id) {
                                "tool_quran" -> showQuranChooserSheet = true
                                "tool_99_names" -> activeSubScreen = ToolsSubScreen.ASMA_UL_HUSNA
                                "tool_zakat" -> activeSubScreen = ToolsSubScreen.ZAKAT
                                "tool_islamic_calendar" -> activeSubScreen = ToolsSubScreen.CALENDAR
                                "tool_hadith" -> activeSubScreen = ToolsSubScreen.HADITH
                                "tool_qibla" -> activeSubScreen = ToolsSubScreen.QIBLA
                                "tool_nearby_mosque" -> openNearbyMosquesInMaps(context)
                                "tool_tasbih" -> onNavigateToTab(AppTabId.TASBIH)
                            }
                        }
                    )
                }

                item(
                    key = "tool_full_width_dua",
                    span = { GridItemSpan(2) }
                ) {
                    FullWidthDuaCard(
                        onClick = { onNavigateToTab(AppTabId.DUA) }
                    )
                }
            }
        }

        if (showQuranChooserSheet) {
            ModalBottomSheet(
                onDismissRequest = { showQuranChooserSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = CleanWhite
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "📖 আল কুরআন",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = IslamicTeal,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "অ্যাপের কুরআন ট্যাব অথবা Quran.com বাংলা ওয়েবসাইট থেকে তিলাওয়াত ও অনুবাদ পড়ুন।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Button(
                        onClick = {
                            showQuranChooserSheet = false
                            onNavigateToTab(AppTabId.QURAN)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicTeal),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("tool_quran_open_tab_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MenuBook,
                            contentDescription = "কুরআন ট্যাব",
                            tint = CleanWhite
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "অ্যাপের কুরআন ট্যাবে যান",
                            color = CleanWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            showQuranChooserSheet = false
                            openUrl(context, "https://quran.com/bn")
                        },
                        border = BorderStroke(1.dp, IslamicTeal),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("tool_quran_open_web_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = "Quran.com বাংলা",
                            tint = IslamicTeal
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Quran.com বাংলায় খুলুন",
                            color = IslamicTeal,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolFeatureGridCard(
    tool: ToolModuleConfig,
    emoji: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("tool_card_${tool.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CleanWhite),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(SoftMintSurface, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        fontSize = if (tool.id == "tool_99_names") 18.sp else 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicTeal
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = SoftMintTint
                ) {
                    Text(
                        text = tool.statusBadgeBn,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = IslamicTeal,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = tool.titleBn,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DarkGray
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = tool.subtitleBn,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FullWidthDuaCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("tool_card_dua_full"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CleanWhite),
        border = BorderStroke(1.5.dp, SoftMint),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(SoftMintTint, CleanWhite)
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(WarmAmberSoft, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🤲",
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "দোয়া ও জিকির",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = IslamicTeal,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = WarmAmber.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "মাসনুন দোয়া",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = IslamicTealDark,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "নামাজ, রোজা, সকাল-সন্ধ্যা ও কুরআনের দোয়ার ভান্ডার",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = "দোয়া ও জিকির ট্যাবে যান",
                tint = IslamicTeal
            )
        }
    }
}

private fun openNearbyMosquesInMaps(context: Context) {
    val gmmUri = Uri.parse("geo:0,0?q=mosque+near+me")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmUri)
    val fallbackUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=mosque+near+me")
    val launchIntent = if (mapIntent.resolveActivity(context.packageManager) != null) {
        mapIntent
    } else {
        Intent(Intent.ACTION_VIEW, fallbackUri)
    }
    runCatching { context.startActivity(launchIntent) }
}

private fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    runCatching { context.startActivity(intent) }
}
