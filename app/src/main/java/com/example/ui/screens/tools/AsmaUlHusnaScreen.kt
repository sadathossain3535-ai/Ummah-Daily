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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.AsmaUlHusnaCatalog
import com.example.data.model.AsmaUlHusnaItem
import com.example.ui.theme.AmiriFontFamily
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
import com.example.util.BengaliFormatter

@Composable
fun AsmaUlHusnaScreen(
    onBack: () -> Unit,
    contentBottomPadding: PaddingValues
) {
    BackHandler(onBack = onBack)

    var searchQuery by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<AsmaUlHusnaItem?>(null) }

    val filtered = remember(searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) {
            AsmaUlHusnaCatalog.names
        } else {
            AsmaUlHusnaCatalog.names.filter {
                it.transliterationBn.contains(q, ignoreCase = true) ||
                    it.meaningBn.contains(q, ignoreCase = true) ||
                    it.arabic.contains(q) ||
                    BengaliFormatter.toBengaliNumber(it.number) == q
            }
        }
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
                        .testTag("asma_back_button")
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
                        text = "আল্লাহর ৯৯টি সুন্দর নাম",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = CleanWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "আসমাউল হুসনা — অর্থ, ব্যাখ্যা ও ফজিলত",
                        style = MaterialTheme.typography.bodySmall.copy(color = SoftMint)
                    )
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("নাম বা অর্থ খুঁজুন (যেমন: রহমান)...") },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = IslamicTeal)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = "মুছুন")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CleanWhite,
                    unfocusedContainerColor = CleanWhite,
                    focusedBorderColor = IslamicTeal,
                    unfocusedBorderColor = CardBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("asma_search_input")
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = contentBottomPadding.calculateBottomPadding() + 20.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.number }) { item ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CleanWhite),
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedItem = item }
                            .testTag("asma_item_${item.number}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(SoftMintSurface, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = BengaliFormatter.toBengaliNumber(item.number),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = IslamicTealDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.transliterationBn,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = IslamicTeal,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = item.meaningBn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = item.arabic,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = AmiriFontFamily,
                                    fontSize = 22.sp,
                                    color = IslamicTealDark
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    selectedItem?.let { item ->
        AsmaDetailDialog(
            item = item,
            onPrev = {
                val prevNum = if (item.number > 1) item.number - 1 else 99
                selectedItem = AsmaUlHusnaCatalog.names.first { it.number == prevNum }
            },
            onNext = {
                val nextNum = if (item.number < 99) item.number + 1 else 1
                selectedItem = AsmaUlHusnaCatalog.names.first { it.number == nextNum }
            },
            onDismiss = { selectedItem = null }
        )
    }
}

@Composable
private fun AsmaDetailDialog(
    item: AsmaUlHusnaItem,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = CleanWhite),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("asma_detail_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            text = "নাম #${BengaliFormatter.toBengaliNumber(item.number)} / ৯৯",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = IslamicTealDark,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = item.transliterationBn,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = IslamicTeal,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(IslamicTeal, RoundedCornerShape(18.dp))
                        .padding(vertical = 18.dp, horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = item.arabic,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontFamily = AmiriFontFamily,
                                fontSize = 38.sp,
                                color = WarmAmber
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.meaningBn,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = CleanWhite,
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SoftMintTint, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "📖 বাংলা অর্থ ও ব্যাখ্যা",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = IslamicTeal,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "${item.meaningBn} — ${item.explanationBn}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkGray
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WarmAmberSoft.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "✨ ফজিলত ও আমল",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = IslamicTealDark,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = item.virtueBn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkGray
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onPrev,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                    ) {
                        Text("❮ পূর্বের", color = IslamicTeal)
                    }
                    OutlinedButton(
                        onClick = onNext,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                    ) {
                        Text("পরের ❯", color = IslamicTeal)
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicTeal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("asma_detail_close_button")
                ) {
                    Text("বন্ধ করুন", color = CleanWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
