package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.DuaCategory
import com.example.data.model.DuaItem
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
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberSoft
import com.example.ui.viewmodel.DuaViewModel
import com.example.util.BengaliFormatter
import com.example.util.ShareCardGenerator

@Composable
fun DuaScreen(
    viewModel: DuaViewModel,
    contentBottomPadding: PaddingValues
) {
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val visibleDuas by viewModel.visibleDuas.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedDua by viewModel.selectedDua.collectAsStateWithLifecycle()

    BackHandler(enabled = selectedDua != null || selectedCategory != null) {
        if (selectedDua != null) {
            viewModel.closeDuaDetail()
        } else {
            viewModel.clearCategory()
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
            // Top Teal Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(IslamicTealDark, IslamicTeal)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedDua != null || selectedCategory != null) {
                        IconButton(
                            onClick = {
                                if (selectedDua != null) {
                                    viewModel.closeDuaDetail()
                                } else {
                                    viewModel.clearCategory()
                                }
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("dua_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "পেছনে যান",
                                tint = CleanWhite
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedDua?.titleBn
                                ?: selectedCategory?.titleBn
                                ?: "দোয়া ও জিকির",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = CleanWhite,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = when {
                                selectedDua != null -> selectedDua?.referenceBn ?: "মাসনুন দোয়া"
                                selectedCategory != null -> selectedCategory?.subtitleBn ?: ""
                                else -> "প্রতিদিনের মাসনুন দোয়া ও যিকিরের ভান্ডার"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(color = SoftMint),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            when {
                selectedDua != null -> {
                    DuaDetailScreenContent(
                        dua = selectedDua!!,
                        contentBottomPadding = contentBottomPadding
                    )
                }

                selectedCategory == null -> {
                    // 2-Column Category Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = contentBottomPadding.calculateBottomPadding() + 20.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(viewModel.categories, key = { it.id }) { category ->
                            DuaCategoryCard(
                                category = category,
                                count = viewModel.countForCategory(category.id),
                                onClick = { viewModel.selectCategory(category) }
                            )
                        }
                    }
                }

                else -> {
                    // Search Bar + Dua List
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("দোয়া খুঁজুন...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "অনুসন্ধান",
                                tint = IslamicTeal
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
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
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("dua_search_input")
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
                        items(visibleDuas, key = { it.id }) { dua ->
                            DuaListRow(
                                dua = dua,
                                onClick = { viewModel.openDuaDetail(dua) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DuaCategoryCard(
    category: DuaCategory,
    count: Int,
    onClick: () -> Unit
) {
    val accent = Color(category.accentHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .testTag("dua_category_${category.id}"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = CleanWhite),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(accent.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.titleBn,
                        tint = accent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = SoftMint
                ) {
                    Text(
                        text = "${BengaliFormatter.toBengaliNumber(count)}টি",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = IslamicTeal,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = category.titleBn,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = category.subtitleBn,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DuaListRow(
    dua: DuaItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .testTag("dua_item_${dua.id}"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = CleanWhite),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dua.titleBn,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = IslamicTeal,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = dua.arabic,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = AmiriFontFamily,
                        fontSize = 18.sp,
                        color = IslamicTealDark
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dua.meaningBn,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(SoftMintSurface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = "বিস্তারিত",
                    tint = IslamicTeal
                )
            }
        }
    }
}

@Composable
private fun DuaDetailScreenContent(
    dua: DuaItem,
    contentBottomPadding: PaddingValues
) {
    val context = LocalContext.current
    val shareFormatted = buildString {
        appendLine("🤲 ${dua.titleBn}")
        appendLine()
        appendLine(dua.arabic)
        appendLine()
        appendLine("উচ্চারণ: ${dua.pronunciationBn}")
        appendLine("অর্থ: ${dua.meaningBn}")
        appendLine("সূত্র: ${dua.referenceBn}")
        appendLine("— UMMAH DAILY")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = contentBottomPadding.calculateBottomPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Arabic Banner Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CleanWhite),
            border = BorderStroke(1.dp, CardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = WarmAmberSoft
                ) {
                    Text(
                        text = dua.referenceBn,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = IslamicTealDark,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SoftMintTint, RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    Text(
                        text = dua.arabic,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = AmiriFontFamily,
                            fontSize = 26.sp,
                            lineHeight = 44.sp,
                            color = IslamicTealDark
                        ),
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "উচ্চারণ:",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = IslamicTeal,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = dua.pronunciationBn,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = DarkGray,
                            lineHeight = 26.sp
                        )
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "অর্থ:",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = WarmAmber,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = dua.meaningBn,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = TextSecondary,
                            lineHeight = 26.sp
                        )
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText(dua.titleBn, shareFormatted))
                    Toast.makeText(context, "দোয়া কপি হয়েছে", Toast.LENGTH_SHORT).show()
                },
                border = BorderStroke(1.dp, IslamicTeal),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("dua_detail_copy_button")
            ) {
                Icon(Icons.Rounded.ContentCopy, contentDescription = null, tint = IslamicTeal, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("কপি করুন", color = IslamicTeal, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    ShareCardGenerator.shareFormattedText(
                        context,
                        shareFormatted,
                        context.getString(R.string.share_dua_chooser)
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = IslamicTeal),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("dua_detail_share_button")
            ) {
                Icon(Icons.Rounded.Share, contentDescription = null, tint = CleanWhite, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("শেয়ার করুন", color = CleanWhite, fontWeight = FontWeight.Bold)
            }
        }
    }
}
