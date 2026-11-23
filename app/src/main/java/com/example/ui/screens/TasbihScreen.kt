package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TasbihSubTab
import com.example.ui.screens.tasbih.TasbihAmalnamaTab
import com.example.ui.screens.tasbih.TasbihCounterTab
import com.example.ui.screens.tasbih.TasbihLeaderboardTab
import com.example.ui.screens.tasbih.TasbihOnboardingModalOverlay
import com.example.ui.screens.tasbih.TasbihPrayersTab
import com.example.ui.theme.AppBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.DarkGray
import com.example.ui.theme.IslamicTeal
import com.example.ui.theme.IslamicTealDark
import com.example.ui.theme.SoftMint
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber
import com.example.ui.viewmodel.TasbihViewModel
import com.example.util.BengaliFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihScreen(
    viewModel: TasbihViewModel,
    contentBottomPadding: PaddingValues
) {
    val localUser by viewModel.localUser.collectAsStateWithLifecycle()
    val tasbihState by viewModel.tasbihState.collectAsStateWithLifecycle()
    val activeSubTab by viewModel.activeSubTab.collectAsStateWithLifecycle()
    val currentWaqt by viewModel.currentWaqt.collectAsStateWithLifecycle()
    val allDhikrs by viewModel.allDhikrs.collectAsStateWithLifecycle()
    val perDhikrCounts by viewModel.perDhikrCounts.collectAsStateWithLifecycle()
    val cloudSyncStatus by viewModel.cloudSyncStatus.collectAsStateWithLifecycle()

    val isOnboardingSubmitting by viewModel.isOnboardingSubmitting.collectAsStateWithLifecycle()
    val onboardingFeedback by viewModel.onboardingFeedback.collectAsStateWithLifecycle()

    val showDhikrSheet by viewModel.showDhikrSheet.collectAsStateWithLifecycle()
    val showFullDuaModal by viewModel.showFullDuaModal.collectAsStateWithLifecycle()

    val selectedPrayerDate by viewModel.selectedPrayerDate.collectAsStateWithLifecycle()
    val prayerChecklist by viewModel.prayerChecklist.collectAsStateWithLifecycle()
    val isPrayersLoading by viewModel.isPrayersLoading.collectAsStateWithLifecycle()

    val isRangeMode by viewModel.isRangeMode.collectAsStateWithLifecycle()
    val amalnamaSummary by viewModel.amalnamaSummary.collectAsStateWithLifecycle()
    val isAmalnamaLoading by viewModel.isAmalnamaLoading.collectAsStateWithLifecycle()

    val leaderboard by viewModel.leaderboard.collectAsStateWithLifecycle()
    val isLeaderboardLoading by viewModel.isLeaderboardLoading.collectAsStateWithLifecycle()
    val leaderboardError by viewModel.leaderboardError.collectAsStateWithLifecycle()

    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showAccountDrawer by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(onboardingFeedback, localUser.isRegistered) {
        val msg = onboardingFeedback
        if (localUser.isRegistered && !msg.isNullOrBlank()) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearOnboardingFeedback()
        }
    }

    BackHandler(enabled = showAccountDrawer) {
        showAccountDrawer = false
    }

    val activeDhikr = allDhikrs.find { it.id == tasbihState.selectedDhikrId }
        ?: allDhikrs.first()

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
                .padding(bottom = contentBottomPadding.calculateBottomPadding())
        ) {
            // Header: Left dropdown/drawer button, center title "তাসবিহ", right reset button ("0")
            Surface(
                color = IslamicTeal,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { showAccountDrawer = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("tasbih_drawer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = "প্রোফাইল ও মেনু",
                            tint = CleanWhite
                        )
                    }

                    Text(
                        text = when (activeSubTab) {
                            TasbihSubTab.COUNTER -> "তাসবিহ"
                            TasbihSubTab.NAMAZ_ROZA -> "নামাজ-রোজা"
                            TasbihSubTab.AMALNAMA -> "আমলনামা"
                            TasbihSubTab.RANK -> "গ্লোবাল র‍্যাংক"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = CleanWhite,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )

                    if (activeSubTab == TasbihSubTab.COUNTER) {
                        Surface(
                            shape = CircleShape,
                            color = CleanWhite.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, SoftMint.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable { showResetConfirmDialog = true }
                                .testTag("tasbih_reset_zero_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "0",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = CleanWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }
            }

            // Active Sub-Tab Body
            Box(modifier = Modifier.weight(1f)) {
                when (activeSubTab) {
                    TasbihSubTab.COUNTER -> {
                        TasbihCounterTab(
                            tasbihState = tasbihState,
                            currentWaqt = currentWaqt,
                            activeDhikr = activeDhikr,
                            allDhikrs = allDhikrs,
                            perDhikrCounts = perDhikrCounts,
                            cloudSyncStatus = cloudSyncStatus,
                            showDhikrSheet = showDhikrSheet,
                            showFullDuaModal = showFullDuaModal,
                            onTapIncrement = { viewModel.incrementCount() },
                            onBulkAdd = { viewModel.addCount(it) },
                            onOpenDhikrSheet = { viewModel.openDhikrSheet() },
                            onCloseDhikrSheet = { viewModel.closeDhikrSheet() },
                            onSelectDhikr = { viewModel.selectDhikr(it) },
                            onAddCustomDhikr = { title -> viewModel.addCustomDhikr(title) },
                            onOpenFullDuaModal = { viewModel.openFullDuaModal() },
                            onCloseFullDuaModal = { viewModel.closeFullDuaModal() }
                        )
                    }

                    TasbihSubTab.NAMAZ_ROZA -> {
                        TasbihPrayersTab(
                            selectedDateKey = selectedPrayerDate,
                            formattedDateBn = viewModel.formatDateBn(selectedPrayerDate),
                            isToday = viewModel.isToday(selectedPrayerDate),
                            checklist = prayerChecklist,
                            isLoading = isPrayersLoading,
                            onShiftDate = { viewModel.shiftPrayerDate(it) },
                            onSelectDateKey = { viewModel.selectPrayerDate(it) },
                            onToggleItem = { key, checked -> viewModel.togglePrayerItem(key, checked) }
                        )
                    }

                    TasbihSubTab.AMALNAMA -> {
                        TasbihAmalnamaTab(
                            user = localUser,
                            isRangeMode = isRangeMode,
                            summary = amalnamaSummary,
                            isLoading = isAmalnamaLoading,
                            onSetRangeMode = { viewModel.setAmalnamaRangeMode(it) },
                            onShiftSingleDay = { viewModel.shiftAmalSingleDay(it) },
                            onSelectQuickRange = { viewModel.selectQuickRange(it) },
                            onSelectCustomRange = { start, end ->
                                viewModel.setCustomAmalRange(start, end)
                            }
                        )
                    }

                    TasbihSubTab.RANK -> {
                        TasbihLeaderboardTab(
                            entries = leaderboard,
                            isLoading = isLeaderboardLoading,
                            errorMessage = leaderboardError,
                            onRefresh = { viewModel.refreshLeaderboard() }
                        )
                    }
                }
            }

            // Sub-Navigation inside Tasbih (4 bottom tabs: 📿 তাসবিহ, 🕌 নামাজ-রোজা, 📜 আমলনামা, 🏆 র্যাংক)
            Surface(
                color = CleanWhite,
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TasbihSubTab.entries.forEach { tab ->
                        val selected = activeSubTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) SoftMint else Color.Transparent)
                                .clickable { viewModel.selectSubTab(tab) }
                                .padding(horizontal = 4.dp, vertical = 8.dp)
                                .testTag("tasbih_subtab_${tab.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.navLabelBn,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (selected) IslamicTeal else DarkGray.copy(alpha = 0.78f),
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Two-Step Modal Dialog Onboarding Overlay on first visit (when user info missing)
        if (!localUser.isRegistered) {
            TasbihOnboardingModalOverlay(
                isLoading = isOnboardingSubmitting,
                feedbackMessage = onboardingFeedback,
                onSubmit = { name, phone, area, district ->
                    viewModel.submitOnboarding(name, phone, area, district)
                }
            )
        }
    }

    // Confirmation Dialog for Reset Button ("কাউন্ট ০ করতে চান?")
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            containerColor = CleanWhite,
            title = {
                Text(
                    text = "কাউন্ট ০ করতে চান?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = IslamicTeal,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Text(
                    text = "\"${activeDhikr.titleBn}\"-এর বর্তমান কাউন্ট (${BengaliFormatter.toBengaliNumber(tasbihState.count)}) রিসেট হয়ে ০ হয়ে যাবে।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetCurrentCount()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicTeal),
                    modifier = Modifier.testTag("tasbih_reset_confirm_yes")
                ) {
                    Text("হ্যাঁ, ০ করুন", color = CleanWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetConfirmDialog = false },
                    modifier = Modifier.testTag("tasbih_reset_confirm_no")
                ) {
                    Text("না", color = DarkGray)
                }
            }
        )
    }

    // Profile / Account Drawer Sheet opened from top-left Menu button
    if (showAccountDrawer) {
        ModalBottomSheet(
            onDismissRequest = { showAccountDrawer = false },
            containerColor = CleanWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = null,
                        tint = IslamicTeal,
                        modifier = Modifier.size(44.dp)
                    )
                    Column {
                        Text(
                            text = localUser.name.ifBlank { "অতিথি ব্যবহারকারী" },
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = IslamicTealDark,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "📱 ${localUser.phone.ifBlank { "—" }} • 📍 ${localUser.resolvedLocation.ifBlank { "—" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SoftMint.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "ক্লাউড আইডি: ${localUser.myId.ifBlank { "সেটআপ বাকি" }}",
                            style = MaterialTheme.typography.labelMedium.copy(color = IslamicTealDark)
                        )
                        Text(
                            text = "সিঙ্ক স্ট্যাটাস: $cloudSyncStatus",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                    }
                }

                Button(
                    onClick = {
                        showAccountDrawer = false
                        viewModel.switchAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicTeal),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("tasbih_switch_account_button")
                ) {
                    Icon(Icons.Rounded.SwapHoriz, contentDescription = null, tint = CleanWhite)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("অ্যাকাউন্ট পরিবর্তন / পুনরুদ্ধার", color = CleanWhite, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
