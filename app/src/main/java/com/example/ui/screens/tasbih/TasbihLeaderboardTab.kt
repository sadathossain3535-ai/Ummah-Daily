package com.example.ui.screens.tasbih

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.RankedUserRow
import com.example.ui.theme.AlertRed
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
fun TasbihLeaderboardTab(
    entries: List<RankedUserRow>,
    isLoading: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit
) {
    val currentUserRank = entries.firstOrNull { it.isCurrentUser }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "leaderboard_header") {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = IslamicTeal)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(WarmAmberSoft, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EmojiEvents,
                                contentDescription = null,
                                tint = WarmAmber,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "গ্লোবাল তাসবিহ র‍্যাংকিং",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = CleanWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = if (currentUserRank != null) {
                                    "আপনার অবস্থান: #${BengaliFormatter.toBengaliNumber(currentUserRank.rank)} • ${BengaliFormatter.toBengaliNumber(currentUserRank.totalCount)} বার"
                                } else {
                                    "সারা দেশের মুমিনদের তাসবিহ র‍্যাংকিং"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(color = SoftMint)
                            )
                        }
                    }

                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("leaderboard_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "রিফ্রেশ",
                            tint = CleanWhite
                        )
                    }
                }
            }
        }

        if (isLoading && entries.isEmpty()) {
            item(key = "loading_state") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = IslamicTeal)
                }
            }
        } else if (!errorMessage.isNullOrBlank() && entries.isEmpty()) {
            item(key = "error_state") {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanWhite),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AlertRed
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onRefresh,
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicTeal)
                        ) {
                            Text("আবার চেষ্টা করুন", color = CleanWhite)
                        }
                    }
                }
            }
        } else if (entries.isEmpty()) {
            item(key = "empty_state") {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanWhite),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Text(
                        text = "এখনো র‍্যাংকিংয়ে কোনো তথ্য পাওয়া যায়নি। তাসবিহ গণনা শুরু করলে আপনার নাম যুক্ত হবে।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(18.dp)
                    )
                }
            }
        } else {
            items(entries, key = { it.myId.ifBlank { "rank_${it.rank}" } }) { row ->
                LeaderboardRowCard(row = row)
            }
        }
    }
}

@Composable
private fun LeaderboardRowCard(row: RankedUserRow) {
    val badgeColor = when (row.rank) {
        1 -> WarmAmber
        2 -> Color(0xFF90A4AE)
        3 -> Color(0xFFCD7F32)
        else -> SoftMintSurface
    }
    val badgeTextColor = when (row.rank) {
        1, 2, 3 -> CleanWhite
        else -> IslamicTealDark
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (row.isCurrentUser) SoftMint else CleanWhite
        ),
        border = BorderStroke(
            width = if (row.isCurrentUser) 1.5.dp else 1.dp,
            color = if (row.isCurrentUser) IslamicTeal else CardBorder
        ),
        modifier = Modifier.testTag("leaderboard_row_${row.rank}")
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
                    .background(badgeColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${BengaliFormatter.toBengaliNumber(row.rank)}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = badgeTextColor,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = row.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = DarkGray,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (row.isCurrentUser) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = IslamicTeal
                        ) {
                            Text(
                                text = "আপনি",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CleanWhite,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "📍 ${row.locationDisplay}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Highlight current user with exact count; mask others' counts as "(গোপন)"
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (row.isCurrentUser) IslamicTeal else SoftMintTint
            ) {
                Text(
                    text = if (row.isCurrentUser) {
                        "${BengaliFormatter.toBengaliNumber(row.totalCount)} বার"
                    } else {
                        "(গোপন)"
                    },
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = if (row.isCurrentUser) CleanWhite else DarkGray.copy(alpha = 0.72f),
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
