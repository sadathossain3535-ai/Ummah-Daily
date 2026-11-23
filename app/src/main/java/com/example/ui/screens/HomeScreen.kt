package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.alarm.PrayerAlarmScheduler
import com.example.config.AppConfigRegistry
import com.example.config.AppTabId
import com.example.data.model.DailyDua
import com.example.data.model.DailyHadith
import com.example.data.model.FardPrayerItem
import com.example.data.model.ForbiddenTimeItem
import com.example.data.model.NaflPrayerItem
import com.example.data.model.PrayerScheduleState
import com.example.ui.components.PrayerCircularRing
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedBorder
import com.example.ui.theme.AlertRedSoft
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.AnekBanglaFontFamily
import com.example.ui.theme.AppBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.IslamicTeal
import com.example.ui.theme.IslamicTealDark
import com.example.ui.theme.ShareGreen
import com.example.ui.theme.SoftMint
import com.example.ui.theme.SoftMintTint
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberSoft
import com.example.ui.viewmodel.HomeUiState
import com.example.ui.viewmodel.HomeViewModel
import com.example.util.BengaliFormatter

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    contentBottomPadding: PaddingValues,
    onNavigateToTab: (AppTabId) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    val enabledAlarms by viewModel.enabledAlarms.collectAsStateWithLifecycle()
    val selectedPrayerDetail by viewModel.selectedPrayerDetail.collectAsStateWithLifecycle()
    val showShareModal by viewModel.showShareModal.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var showExactAlarmPrompt by remember { mutableStateOf(false) }
    var showQiblaCompass by remember { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.syncCurrentScheduleAlarms()
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    val handleAlarmToggle: (String) -> Unit = { prayerId ->
        val turningOn = !enabledAlarms.contains(prayerId)
        if (turningOn && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        viewModel.toggleAlarm(prayerId) { enabledNow ->
            val msg = if (enabledNow) {
                context.getString(R.string.alarm_enabled_toast)
            } else {
                context.getString(R.string.alarm_disabled_toast)
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            if (enabledNow && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !PrayerAlarmScheduler.canScheduleExactAlarms(context)
            ) {
                showExactAlarmPrompt = true
            }
        }
    }

    val activeDetail = selectedPrayerDetail
    if (showQiblaCompass) {
        val cityObj = AppConfigRegistry.bangladeshCities.find { it.apiCity == selectedCity }
            ?: AppConfigRegistry.bangladeshCities.first()
        QiblaCompassScreen(
            selectedCityApi = cityObj.apiCity,
            selectedCityNameBn = cityObj.nameBn,
            onBack = { showQiblaCompass = false },
            contentBottomPadding = contentBottomPadding
        )
        return
    }

    if (activeDetail != null) {
        PrayerDetailScreen(
            prayer = activeDetail,
            isAlarmEnabled = enabledAlarms.contains(activeDetail.id),
            onToggleAlarm = { handleAlarmToggle(activeDetail.id) },
            onBack = { viewModel.closePrayerDetail() },
            contentBottomPadding = contentBottomPadding
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = IslamicTeal)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "নামাজের সময়সূচী লোড হচ্ছে...",
                        style = MaterialTheme.typography.titleMedium,
                        color = IslamicTeal
                    )
                }
            }

            is HomeUiState.Error -> {
                Column(
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WarningAmber,
                        contentDescription = null,
                        tint = AlertRed,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = state.messageBn,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = { viewModel.refresh() },
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicTeal),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("retry_prayer_fetch_button")
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("আবার চেষ্টা করুন")
                    }
                }
            }

            is HomeUiState.Success -> {
                val schedule = state.schedule
                LazyColumn(
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = contentBottomPadding.calculateBottomPadding() + 20.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Section 1: Deep Teal #04484A Top Header + Circular Timer + City/Sunrise-Sunset Bar
                    item(key = "hero_teal_section") {
                        HomeTealHeroSection(
                            schedule = schedule,
                            selectedCityApi = selectedCity,
                            isRefreshing = state.isRefreshing,
                            onSelectCity = { viewModel.selectCity(it) },
                            onShareClick = { viewModel.openShareModal() }
                        )
                    }

                    // Card 1: 🕌 ফরজ নামাজের ওয়াক্ত
                    item(key = "fard_prayers") {
                        FardPrayersCard(
                            fardPrayers = schedule.fardPrayers,
                            enabledAlarms = enabledAlarms,
                            onToggleAlarm = handleAlarmToggle,
                            onPrayerClick = { viewModel.openPrayerDetail(it) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // Card 2: ✨ নফল ও সুন্নত নামাজসমূহ
                    item(key = "nafl_prayers") {
                        NaflPrayersCard(
                            naflPrayers = schedule.naflPrayers,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // Card 3: 🚫 সালাতের নিষিদ্ধ সময় (#fff5f5 + #ff5252 left border)
                    item(key = "forbidden_times") {
                        ForbiddenTimesCard(
                            forbiddenTimes = schedule.forbiddenTimes,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // Side-by-side #CEE6DE Sehri & Iftar cards
                    item(key = "sehri_iftar") {
                        SehriIftarRow(
                            schedule = schedule,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // Daily Special Dua (Dua Masura)
                    item(key = "daily_dua") {
                        DailyDuaCard(
                            dua = viewModel.todayDua,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // Daily Rotating Hadith
                    item(key = "daily_hadith") {
                        DailyHadithCard(
                            hadith = viewModel.todayHadith,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // 🌟 প্রয়োজনীয় ফিচার 4-column circular #CEE6DE icon grid
                    item(key = "quick_grid") {
                        EssentialFeaturesQuickGrid(
                            onQuranClick = { onNavigateToTab(AppTabId.QURAN) },
                            onQiblaClick = { showQiblaCompass = true },
                            onMosqueClick = { openNearbyMosquesMap(context) },
                            onTasbihClick = { onNavigateToTab(AppTabId.TASBIH) },
                            onDuaClick = { onNavigateToTab(AppTabId.DUA) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                if (showShareModal) {
                    SharePrayerDialog(
                        schedule = schedule,
                        onDismiss = { viewModel.closeShareModal() }
                    )
                }

                if (showExactAlarmPrompt) {
                    ExactAlarmPermissionDialog(
                        onDismiss = { showExactAlarmPrompt = false },
                        onOpenSettings = {
                            showExactAlarmPrompt = false
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                runCatching {
                                    context.startActivity(
                                        Intent(
                                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                            Uri.fromParts("package", context.packageName, null)
                                        )
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeTealHeroSection(
    schedule: PrayerScheduleState,
    selectedCityApi: String,
    isRefreshing: Boolean,
    onSelectCity: (String) -> Unit,
    onShareClick: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(IslamicTeal)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header: Left Hijri + Gregorian, Right Green Rounded Share Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = schedule.hijriDateBn,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = CleanWhite,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 2.dp,
                            color = SoftMint
                        )
                    }
                }
                Text(
                    text = schedule.gregorianDateBn,
                    style = MaterialTheme.typography.bodySmall.copy(color = SoftMint),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = onShareClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ShareGreen,
                    contentColor = CleanWhite
                ),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("home_share_button")
            ) {
                Text(
                    text = "📤 শেয়ার",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = CleanWhite,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center Circular Progress Ring (220dp outer, 195dp inner #04484A circle)
        PrayerCircularRing(status = schedule.status)

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom rounded info bar (#04484A with subtle Mint border): City Dropdown + Sunrise | Sunset
        Surface(
            shape = RoundedCornerShape(50),
            color = IslamicTealDark,
            border = BorderStroke(1.dp, SoftMint.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Row(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(50))
                            .clickable { dropdownExpanded = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("city_dropdown_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📍 ${schedule.cityNameBn}",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = WarmAmber,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Icon(
                            imageVector = Icons.Rounded.ExpandMore,
                            contentDescription = "জেলা পরিবর্তন",
                            tint = SoftMint,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        AppConfigRegistry.bangladeshCities.forEach { city ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${city.nameBn} (${city.divisionBn})",
                                        fontWeight = if (city.apiCity == selectedCityApi) {
                                            FontWeight.Bold
                                        } else {
                                            FontWeight.Normal
                                        },
                                        color = if (city.apiCity == selectedCityApi) {
                                            IslamicTeal
                                        } else {
                                            TextPrimary
                                        }
                                    )
                                },
                                onClick = {
                                    dropdownExpanded = false
                                    onSelectCity(city.apiCity)
                                },
                                modifier = Modifier.testTag("city_option_${city.apiCity}")
                            )
                        }
                    }
                }

                Text(
                    text = "🌅 ${schedule.sunriseBn} | 🌇 ${schedule.sunsetBn}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SoftMint,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun FardPrayersCard(
    fardPrayers: List<FardPrayerItem>,
    enabledAlarms: Set<String>,
    onToggleAlarm: (String) -> Unit,
    onPrayerClick: (FardPrayerItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = CleanWhite),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🕌 ফরজ নামাজের ওয়াক্ত",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = IslamicTeal,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "বিস্তারিত দেখতে ট্যাপ করুন",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                fardPrayers.forEach { prayer ->
                    val alarmOn = enabledAlarms.contains(prayer.id)
                    val rowBg = if (prayer.isCurrentActive) SoftMint else AppBackground
                    val rowBorder = when {
                        prayer.isCurrentActive -> BorderStroke(1.5.dp, IslamicTeal)
                        prayer.isNextUpcoming -> BorderStroke(1.dp, WarmAmber)
                        else -> BorderStroke(1.dp, Color.Transparent)
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = rowBg,
                        border = rowBorder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onPrayerClick(prayer) }
                            .testTag("fard_prayer_row_${prayer.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        color = if (prayer.isCurrentActive) {
                                            WarmAmber
                                        } else {
                                            TextSecondary.copy(alpha = 0.3f)
                                        },
                                        shape = CircleShape
                                    )
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prayer.nameBn,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = IslamicTeal,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = prayer.rakatSummaryBn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Text(
                                text = prayer.startTimeBn,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = AnekBanglaFontFamily,
                                    color = IslamicTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = if (alarmOn) "⏰" else "🔕",
                                fontSize = 15.sp
                            )

                            Switch(
                                checked = alarmOn,
                                onCheckedChange = { onToggleAlarm(prayer.id) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CleanWhite,
                                    checkedTrackColor = IslamicTeal,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = CleanWhite,
                                    uncheckedBorderColor = CardBorder
                                ),
                                modifier = Modifier
                                    .scale(0.82f)
                                    .minimumInteractiveComponentSize()
                                    .semantics {
                                        contentDescription = if (alarmOn) {
                                            "${prayer.nameBn} আযান অ্যালার্ম চালু"
                                        } else {
                                            "${prayer.nameBn} আযান অ্যালার্ম বন্ধ"
                                        }
                                    }
                                    .testTag("alarm_switch_${prayer.id}")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NaflPrayersCard(
    naflPrayers: List<NaflPrayerItem>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = CleanWhite),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "✨ নফল ও সুন্নত নামাজসমূহ",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = IslamicTeal,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                naflPrayers.forEach { nafl ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (nafl.isActive) SoftMint else SoftMintTint,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .border(
                                width = if (nafl.isActive) 1.5.dp else 1.dp,
                                color = if (nafl.isActive) IslamicTeal else CardBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (nafl.isActive) "${nafl.nameBn} • চলছে" else nafl.nameBn,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = IslamicTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "${nafl.rakatInfoBn} • ${nafl.subtitleBn}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = nafl.windowTextBn,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = AnekBanglaFontFamily,
                                color = IslamicTeal,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ForbiddenTimesCard(
    forbiddenTimes: List<ForbiddenTimeItem>,
    modifier: Modifier = Modifier
) {
    val hasActiveForbidden = forbiddenTimes.any { it.isActive }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = AlertRedSoft),
        border = BorderStroke(1.dp, if (hasActiveForbidden) AlertRed else AlertRedBorder)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Left #ff5252 accent bar
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(AlertRed)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🚫 সালাতের নিষিদ্ধ সময়",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = AlertRed,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    if (hasActiveForbidden) {
                        Box(
                            modifier = Modifier
                                .background(AlertRed, RoundedCornerShape(50))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "এখন নিষিদ্ধ",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CleanWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    forbiddenTimes.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (item.isActive) AlertRed.copy(alpha = 0.16f) else CleanWhite,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = if (item.isActive) 1.5.dp else 1.dp,
                                    color = if (item.isActive) AlertRed else AlertRedBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.titleBn,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = AlertRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = item.reasonBn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = item.windowTextBn,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = AnekBanglaFontFamily,
                                    color = AlertRed,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SehriIftarRow(
    schedule: PrayerScheduleState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SehriIftarBox(
            title = "🌙 সেহরি শেষ",
            timeBn = schedule.status.sehriEndTimeBn,
            dateBn = schedule.status.sehriNextDateBn,
            countdownBn = "বাকি: ${BengaliFormatter.formatCountdown(schedule.status.sehriCountdownSeconds)}",
            modifier = Modifier.weight(1f)
        )
        SehriIftarBox(
            title = "🍽️ ইফতার শুরু",
            timeBn = schedule.status.iftarTimeBn,
            dateBn = schedule.status.iftarNextDateBn,
            countdownBn = "বাকি: ${BengaliFormatter.formatCountdown(schedule.status.iftarCountdownSeconds)}",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SehriIftarBox(
    title: String,
    timeBn: String,
    dateBn: String,
    countdownBn: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = SoftMint),
        border = BorderStroke(1.dp, IslamicTeal.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = IslamicTeal,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = dateBn,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = timeBn,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = AnekBanglaFontFamily,
                    color = IslamicTeal,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = countdownBn,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = AnekBanglaFontFamily,
                    color = IslamicTealDark,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun DailyDuaCard(
    dua: DailyDua,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = CleanWhite),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🤲 ${dua.titleBn}",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = IslamicTeal,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SoftMintTint, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = dua.arabic,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = AmiriFontFamily,
                        fontSize = 21.sp,
                        lineHeight = 36.sp,
                        color = IslamicTeal
                    ),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "অর্থ: ${dua.meaningBn}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "তথ্যসূত্র: ${dua.referenceBn}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun DailyHadithCard(
    hadith: DailyHadith,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = IslamicTeal)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📜 আজকের হাদিস",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = WarmAmber,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "“${hadith.textBn}”",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = CleanWhite,
                    lineHeight = 24.sp
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = SoftMint.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${hadith.narratorBn} • ${hadith.sourceBn}",
                style = MaterialTheme.typography.labelSmall,
                color = SoftMint
            )
        }
    }
}

@Composable
private fun EssentialFeaturesQuickGrid(
    onQuranClick: () -> Unit,
    onQiblaClick: () -> Unit,
    onMosqueClick: () -> Unit,
    onTasbihClick: () -> Unit,
    onDuaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    data class QuickFeature(val id: String, val emoji: String, val label: String, val onClick: () -> Unit)

    val items = listOf(
        QuickFeature("quran", "📖", "কুরআন", onQuranClick),
        QuickFeature("qibla", "🕋", "কিবলা", onQiblaClick),
        QuickFeature("mosque", "🕌", "মসজিদ", onMosqueClick),
        QuickFeature("tasbih", "📿", "তাসবিহ", onTasbihClick),
        QuickFeature("dua", "🤲", "দোয়া", onDuaClick)
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = CleanWhite),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🌟 প্রয়োজনীয় ফিচার",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = IslamicTeal,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            items.chunked(4).forEachIndexed { rowIndex, rowItems ->
                if (rowIndex > 0) Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { feature ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(onClick = feature.onClick)
                                .padding(vertical = 6.dp)
                                .testTag("quick_feature_${feature.id}"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(SoftMint, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = feature.emoji, fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = feature.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = IslamicTeal,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    repeat(4 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ExactAlarmPermissionDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CleanWhite,
        icon = {
            Icon(
                imageVector = Icons.Rounded.NotificationsActive,
                contentDescription = null,
                tint = IslamicTeal
            )
        },
        title = {
            Text(
                text = "সঠিক সময়ে আযান অ্যালার্ম",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = IslamicTeal,
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Text(
                text = "অ্যাপ বন্ধ থাকা অবস্থায় ঠিক মিনিটে আযান বাজাতে ফোনের 'Alarms & reminders' অনুমতি চালু করুন।",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
        },
        confirmButton = {
            Button(
                onClick = onOpenSettings,
                colors = ButtonDefaults.buttonColors(containerColor = IslamicTeal)
            ) {
                Text("অনুমতি দিন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("পরে", color = TextSecondary)
            }
        }
    )
}

private fun openNearbyMosquesMap(context: android.content.Context) {
    val mapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=mosque+near+me"))
        .setPackage("com.google.android.apps.maps")
    val launchedMaps = runCatching { context.startActivity(mapsIntent) }.isSuccess
    if (!launchedMaps) {
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/search/mosque+near+me")
        )
        runCatching { context.startActivity(webIntent) }
    }
}
