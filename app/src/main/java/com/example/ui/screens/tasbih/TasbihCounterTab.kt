package com.example.ui.screens.tasbih

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TasbihPrefs
import com.example.data.model.TasbihDhikrItem
import com.example.data.model.WaqtType
import com.example.ui.theme.AnekBanglaFontFamily
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.DarkGray
import com.example.ui.theme.IslamicTeal
import com.example.ui.theme.IslamicTealDark
import com.example.ui.theme.ShareGreen
import com.example.ui.theme.ShareGreenSoft
import com.example.ui.theme.SoftMint
import com.example.ui.theme.SoftMintSurface
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber
import com.example.util.BengaliFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihCounterTab(
    tasbihState: TasbihPrefs,
    currentWaqt: WaqtType,
    activeDhikr: TasbihDhikrItem,
    allDhikrs: List<TasbihDhikrItem>,
    perDhikrCounts: Map<String, Int>,
    cloudSyncStatus: String,
    showDhikrSheet: Boolean,
    showFullDuaModal: Boolean,
    onTapIncrement: () -> Unit,
    onBulkAdd: (Int) -> Unit,
    onOpenDhikrSheet: () -> Unit,
    onCloseDhikrSheet: () -> Unit,
    onSelectDhikr: (TasbihDhikrItem) -> Unit,
    onAddCustomDhikr: (title: String) -> Unit,
    onOpenFullDuaModal: () -> Unit,
    onCloseFullDuaModal: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val haptics = LocalHapticFeedback.current

    val triggerTapVibration: (Boolean) -> Unit = { doublePulse ->
        if (doublePulse) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
        vibrateTasbih(context, doublePulse)
    }

    val targetFraction = if (tasbihState.targetCount > 0) {
        (tasbihState.count.toFloat() / tasbihState.targetCount.toFloat()).coerceIn(0f, 1f)
    } else {
        (tasbihState.count % 100) / 100f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = spring(),
        label = "tasbih_ring_progress"
    )
    val isDone = tasbihState.targetCount > 0 && tasbihState.count >= tasbihState.targetCount
    val countLabel = BengaliFormatter.toBengaliNumber(tasbihState.count)
    val targetLabel = BengaliFormatter.toBengaliNumber(tasbihState.targetCount)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Current Waqt Badge (#CEE6DE pill)
        Surface(
            shape = RoundedCornerShape(50),
            color = SoftMint,
            border = BorderStroke(1.dp, IslamicTeal.copy(alpha = 0.25f)),
            modifier = Modifier.testTag("tasbih_current_waqt_badge")
        ) {
            Text(
                text = currentWaqt.badgeBn,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = IslamicTeal,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dhikr Selector Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = onOpenDhikrSheet)
                .testTag("tasbih_dhikr_selector_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CleanWhite),
            border = BorderStroke(1.dp, CardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = activeDhikr.titleBn,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = DarkGray,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isDone) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = ShareGreenSoft,
                                modifier = Modifier.testTag("tasbih_done_badge")
                            ) {
                                Text(
                                    text = "✔ Done",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ShareGreen,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Icon(
                        imageVector = Icons.Rounded.ExpandMore,
                        contentDescription = "জিকির তালিকা",
                        tint = IslamicTeal,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                TextButton(
                    onClick = onOpenFullDuaModal,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("tasbih_full_dua_button")
                ) {
                    Text(
                        text = "📖 পুরো দোয়া ও অর্থ দেখুন",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = IslamicTeal,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4 Quick Bulk-Add Buttons (+১০, +২০, +৫০, +১০০)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(10, 20, 50, 100).forEach { amount ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CleanWhite,
                    border = BorderStroke(1.dp, IslamicTeal.copy(alpha = 0.25f)),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val reached = tasbihState.targetCount > 0 &&
                                tasbihState.count < tasbihState.targetCount &&
                                (tasbihState.count + amount) >= tasbihState.targetCount
                            triggerTapVibration(reached)
                            onBulkAdd(amount)
                        }
                        .testTag("tasbih_bulk_add_$amount")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = "+${BengaliFormatter.toBengaliNumber(amount)}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = AnekBanglaFontFamily,
                                color = IslamicTeal,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Center Tap Area + 240.dp Circular Progress Ring
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .semantics {
                    role = Role.Button
                    contentDescription =
                        "${activeDhikr.titleBn}, গণনা $countLabel এর মধ্যে $targetLabel, বাড়াতে ট্যাপ করুন"
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    val willReachTarget = tasbihState.targetCount > 0 &&
                        (tasbihState.count + 1) == tasbihState.targetCount
                    triggerTapVibration(willReachTarget)
                    onTapIncrement()
                }
                .padding(vertical = 12.dp)
                .testTag("tasbih_tap_ring"),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokePx = 15.dp.toPx()
                    drawCircle(
                        color = SoftMintSurface,
                        style = Stroke(width = strokePx)
                    )
                    drawArc(
                        color = if (isDone) WarmAmber else IslamicTeal,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(196.dp)
                        .background(CleanWhite, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = countLabel,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontFamily = AnekBanglaFontFamily,
                                fontSize = 54.sp,
                                lineHeight = 58.sp,
                                color = IslamicTeal,
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "/ $targetLabel",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = AnekBanglaFontFamily,
                                color = DarkGray.copy(alpha = 0.72f),
                                fontWeight = FontWeight.SemiBold
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "ট্যাপ করুন",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isDone) ShareGreen else TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "মোট আমল: ${BengaliFormatter.toBengaliNumber(tasbihState.totalCount)} বার • $cloudSyncStatus",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }

    if (showDhikrSheet) {
        ModalBottomSheet(
            onDismissRequest = onCloseDhikrSheet,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = CleanWhite
        ) {
            DhikrSelectorSheetContent(
                currentWaqt = currentWaqt,
                allDhikrs = allDhikrs,
                perDhikrCounts = perDhikrCounts,
                activeDhikrId = activeDhikr.id,
                onSelectDhikr = onSelectDhikr,
                onAddCustomDhikr = onAddCustomDhikr
            )
        }
    }

    if (showFullDuaModal) {
        FullDuaDialog(
            activeDhikr = activeDhikr,
            onDismiss = onCloseFullDuaModal
        )
    }
}

@Suppress("DEPRECATION")
private fun vibrateTasbih(context: Context, doublePulse: Boolean) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            val vibrator = manager?.defaultVibrator ?: return
            if (doublePulse) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 35, 65, 55), -1)
                )
            } else {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(35L, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            }
        } else {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (doublePulse) {
                    vibrator.vibrate(
                        VibrationEffect.createWaveform(longArrayOf(0, 35, 65, 55), -1)
                    )
                } else {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(35L, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                }
            } else {
                if (doublePulse) {
                    vibrator.vibrate(longArrayOf(0, 35, 65, 55), -1)
                } else {
                    vibrator.vibrate(35L)
                }
            }
        }
    } catch (_: Exception) {
        // Ignore if vibrator hardware unavailable
    }
}
