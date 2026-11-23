package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LocalTasbihUser
import com.example.data.local.TasbihPrefs
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.AmalnamaSummary
import com.example.data.model.DailyPrayerChecklist
import com.example.data.model.RankedUserRow
import com.example.data.model.TasbihDhikrCatalog
import com.example.data.model.TasbihDhikrItem
import com.example.data.model.TasbihSubTab
import com.example.data.model.WaqtType
import com.example.data.repository.OnboardingOutcome
import com.example.data.repository.TasbihRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TasbihViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = UserPreferencesRepository(application)
    private val repo = TasbihRepository(prefsRepo)

    val tasbihState: StateFlow<TasbihPrefs> = prefsRepo.tasbihPrefsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, TasbihPrefs())

    val localUser: StateFlow<LocalTasbihUser> = prefsRepo.localTasbihUserFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, LocalTasbihUser())

    private val _customDhikrs = MutableStateFlow<List<TasbihDhikrItem>>(emptyList())
    val allDhikrs: StateFlow<List<TasbihDhikrItem>> = _customDhikrs
        .combine(MutableStateFlow(TasbihDhikrCatalog.allBuiltInDhikrs)) { custom, builtIn ->
            custom + builtIn
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, TasbihDhikrCatalog.allBuiltInDhikrs)

    private val _perDhikrCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val perDhikrCounts: StateFlow<Map<String, Int>> = _perDhikrCounts.asStateFlow()

    private val _activeSubTab = MutableStateFlow(TasbihSubTab.COUNTER)
    val activeSubTab: StateFlow<TasbihSubTab> = _activeSubTab.asStateFlow()

    private val _currentWaqt = MutableStateFlow(repo.detectCurrentWaqt())
    val currentWaqt: StateFlow<WaqtType> = _currentWaqt.asStateFlow()

    // Onboarding state
    private val _isOnboardingSubmitting = MutableStateFlow(false)
    val isOnboardingSubmitting: StateFlow<Boolean> = _isOnboardingSubmitting.asStateFlow()

    private val _onboardingFeedback = MutableStateFlow<String?>(null)
    val onboardingFeedback: StateFlow<String?> = _onboardingFeedback.asStateFlow()

    private val _cloudSyncStatus = MutableStateFlow("ক্লাউড সিঙ্ক প্রস্তুত")
    val cloudSyncStatus: StateFlow<String> = _cloudSyncStatus.asStateFlow()

    // Counter Sheets & Modals
    private val _showDhikrSheet = MutableStateFlow(false)
    val showDhikrSheet: StateFlow<Boolean> = _showDhikrSheet.asStateFlow()

    private val _showFullDuaModal = MutableStateFlow(false)
    val showFullDuaModal: StateFlow<Boolean> = _showFullDuaModal.asStateFlow()

    // Namaz & Roza Checklist State
    private val _selectedPrayerDate = MutableStateFlow(repo.todayDateKey())
    val selectedPrayerDate: StateFlow<String> = _selectedPrayerDate.asStateFlow()

    private val _prayerChecklist = MutableStateFlow(DailyPrayerChecklist(date = repo.todayDateKey()))
    val prayerChecklist: StateFlow<DailyPrayerChecklist> = _prayerChecklist.asStateFlow()

    private val _isPrayersLoading = MutableStateFlow(false)
    val isPrayersLoading: StateFlow<Boolean> = _isPrayersLoading.asStateFlow()

    // Amalnama State
    private val _isRangeMode = MutableStateFlow(false)
    val isRangeMode: StateFlow<Boolean> = _isRangeMode.asStateFlow()

    private val _amalStartDate = MutableStateFlow(repo.todayDateKey())
    val amalStartDate: StateFlow<String> = _amalStartDate.asStateFlow()

    private val _amalEndDate = MutableStateFlow(repo.todayDateKey())
    val amalEndDate: StateFlow<String> = _amalEndDate.asStateFlow()

    private val _amalnamaSummary = MutableStateFlow<AmalnamaSummary?>(null)
    val amalnamaSummary: StateFlow<AmalnamaSummary?> = _amalnamaSummary.asStateFlow()

    private val _isAmalnamaLoading = MutableStateFlow(false)
    val isAmalnamaLoading: StateFlow<Boolean> = _isAmalnamaLoading.asStateFlow()

    // Leaderboard State
    private val _leaderboard = MutableStateFlow<List<RankedUserRow>>(emptyList())
    val leaderboard: StateFlow<List<RankedUserRow>> = _leaderboard.asStateFlow()

    private val _isLeaderboardLoading = MutableStateFlow(false)
    val isLeaderboardLoading: StateFlow<Boolean> = _isLeaderboardLoading.asStateFlow()

    private val _leaderboardError = MutableStateFlow<String?>(null)
    val leaderboardError: StateFlow<String?> = _leaderboardError.asStateFlow()

    private var pendingDelta: Int = 0
    private var pendingDhikrId: String = "general_tasbih"
    private var syncDebounceJob: Job? = null

    init {
        viewModelScope.launch {
            prefsRepo.customDhikrsJsonFlow.collectLatest { json ->
                _customDhikrs.value = repo.loadCustomDhikrs(json)
            }
        }
        viewModelScope.launch {
            _perDhikrCounts.value = repo.loadLocalCountsMap()
        }
        viewModelScope.launch {
            prefsRepo.localTasbihUserFlow.collectLatest { user ->
                _perDhikrCounts.value = repo.loadLocalCountsMap()
                if (user.isRegistered) {
                    loadPrayersForDate(_selectedPrayerDate.value)
                    refreshAmalnama()
                }
            }
        }
    }

    fun selectSubTab(tab: TasbihSubTab) {
        _activeSubTab.value = tab
        when (tab) {
            TasbihSubTab.COUNTER -> refreshWaqt()
            TasbihSubTab.NAMAZ_ROZA -> loadPrayersForDate(_selectedPrayerDate.value)
            TasbihSubTab.AMALNAMA -> refreshAmalnama()
            TasbihSubTab.RANK -> refreshLeaderboard()
        }
    }

    fun refreshWaqt() {
        _currentWaqt.value = repo.detectCurrentWaqt()
    }

    fun submitOnboarding(
        name: String,
        phone: String,
        area: String,
        district: String
    ) {
        if (name.isBlank() || phone.isBlank() || area.isBlank() || district.isBlank()) {
            _onboardingFeedback.value = "অনুগ্রহ করে নাম, মোবাইল নম্বর, এরিয়া ও জেলা পূরণ করুন।"
            return
        }
        viewModelScope.launch {
            _isOnboardingSubmitting.value = true
            _onboardingFeedback.value = null
            val result = repo.registerOrRecoverUser(name, phone, area, district)
            _isOnboardingSubmitting.value = false
            result.fold(
                onSuccess = { outcome ->
                    _perDhikrCounts.value = repo.loadLocalCountsMap()
                    when (outcome) {
                        is OnboardingOutcome.RecoveredExisting -> {
                            _onboardingFeedback.value =
                                "স্বাগতম ${outcome.user.name}! আপনার পূর্বের অ্যাকাউন্ট পুনরুদ্ধার হয়েছে।"
                            _cloudSyncStatus.value = "ক্লাউড থেকে পুনরুদ্ধার সম্পন্ন"
                        }
                        is OnboardingOutcome.CreatedNew -> {
                            _cloudSyncStatus.value = if (outcome.syncedToCloud) {
                                "ক্লাউড অ্যাকাউন্ট সংযুক্ত"
                            } else {
                                "অফলাইন মোড (পরে সিঙ্ক হবে)"
                            }
                        }
                    }
                    loadPrayersForDate(_selectedPrayerDate.value)
                    refreshAmalnama()
                },
                onFailure = { err ->
                    _onboardingFeedback.value = err.message ?: "তথ্য সংরক্ষণে সমস্যা হয়েছে।"
                }
            )
        }
    }

    fun clearOnboardingFeedback() {
        _onboardingFeedback.value = null
    }

    fun switchAccount() {
        viewModelScope.launch {
            prefsRepo.clearTasbihUser()
        }
    }

    fun openDhikrSheet() {
        refreshWaqt()
        _showDhikrSheet.value = true
    }

    fun closeDhikrSheet() {
        _showDhikrSheet.value = false
    }

    fun openFullDuaModal() {
        _showFullDuaModal.value = true
    }

    fun closeFullDuaModal() {
        _showFullDuaModal.value = false
    }

    fun getActiveDhikr(): TasbihDhikrItem {
        val id = tasbihState.value.selectedDhikrId
        return allDhikrs.value.find { it.id == id }
            ?: TasbihDhikrCatalog.allBuiltInDhikrs.find { it.id == "general_tasbih" }
            ?: TasbihDhikrCatalog.allBuiltInDhikrs.first()
    }

    fun addCount(delta: Int) {
        if (delta <= 0) return
        val current = tasbihState.value
        val activeDhikr = getActiveDhikr()
        val nextCount = current.count + delta
        val nextTotal = current.totalCount + delta.toLong()

        val updatedMap = _perDhikrCounts.value.toMutableMap()
        updatedMap[activeDhikr.id] = nextCount
        _perDhikrCounts.value = updatedMap

        viewModelScope.launch {
            prefsRepo.saveTasbihState(
                count = nextCount,
                totalCount = nextTotal,
                targetCount = current.targetCount,
                selectedDhikrId = activeDhikr.id
            )
        }

        if (pendingDhikrId != activeDhikr.id) {
            pendingDelta = 0
            pendingDhikrId = activeDhikr.id
        }
        pendingDelta += delta
        scheduleCloudSync(
            tasbeehId = activeDhikr.id,
            newCount = nextCount,
            newLifetimeTotal = nextTotal,
            immediate = delta >= 10
        )
    }

    fun incrementCount() = addCount(1)

    fun resetCurrentCount() {
        val current = tasbihState.value
        val activeId = getActiveDhikr().id
        val updatedMap = _perDhikrCounts.value.toMutableMap()
        updatedMap[activeId] = 0
        _perDhikrCounts.value = updatedMap
        pendingDelta = 0

        viewModelScope.launch {
            prefsRepo.saveTasbihState(
                count = 0,
                totalCount = current.totalCount,
                targetCount = current.targetCount,
                selectedDhikrId = activeId
            )
            _cloudSyncStatus.value = "সিঙ্ক হচ্ছে..."
            val synced = repo.resetDhikrCloudCount(localUser.value, activeId)
            _cloudSyncStatus.value = if (synced) "কাউন্ট ০ করা হয়েছে" else "অফলাইনে ০ করা হয়েছে"
        }
    }

    fun selectTarget(target: Int) {
        val current = tasbihState.value
        viewModelScope.launch {
            prefsRepo.saveTasbihState(
                count = current.count,
                totalCount = current.totalCount,
                targetCount = target.coerceAtLeast(1),
                selectedDhikrId = current.selectedDhikrId
            )
        }
    }

    fun selectDhikr(dhikr: TasbihDhikrItem) {
        val current = tasbihState.value
        val savedDhikrCount = _perDhikrCounts.value[dhikr.id] ?: 0
        val nextTarget = if (dhikr.defaultTarget > 0) dhikr.defaultTarget else current.targetCount
        viewModelScope.launch {
            prefsRepo.saveTasbihState(
                count = savedDhikrCount,
                totalCount = current.totalCount,
                targetCount = nextTarget,
                selectedDhikrId = dhikr.id
            )
            _showDhikrSheet.value = false
        }
    }

    fun addCustomDhikr(
        titleBn: String,
        target: Int = 33
    ) {
        val cleanTitle = titleBn.trim()
        if (cleanTitle.isEmpty()) return
        val newItem = TasbihDhikrItem(
            id = "custom_${System.currentTimeMillis()}",
            titleBn = cleanTitle,
            arabic = cleanTitle,
            pronunciationBn = cleanTitle,
            meaningBn = "ব্যক্তিগত কাস্টম জিকির",
            virtueBn = "নিয়মিত আল্লাহর জিকির অন্তরে প্রশান্তি আনে ও নেকি বৃদ্ধি করে।",
            defaultTarget = target.coerceAtLeast(1),
            isCustom = true
        )
        viewModelScope.launch {
            val updated = listOf(newItem) + _customDhikrs.value
            _customDhikrs.value = updated
            repo.saveCustomDhikrs(updated)
            selectDhikr(newItem)
        }
    }

    private fun scheduleCloudSync(
        tasbeehId: String,
        newCount: Int,
        newLifetimeTotal: Long,
        immediate: Boolean
    ) {
        syncDebounceJob?.cancel()
        syncDebounceJob = viewModelScope.launch {
            if (!immediate) delay(600L)
            val deltaToFlush = pendingDelta
            if (deltaToFlush <= 0) return@launch
            pendingDelta = 0
            _cloudSyncStatus.value = "সিঙ্ক হচ্ছে..."
            val synced = repo.recordTasbihDelta(
                user = localUser.value,
                tasbeehId = tasbeehId,
                delta = deltaToFlush,
                newCurrentCount = newCount,
                newLifetimeTotal = newLifetimeTotal
            )
            _cloudSyncStatus.value = if (synced) "ক্লাউড সিঙ্ক সম্পন্ন" else "অফলাইনে সংরক্ষিত"
        }
    }

    // --- Namaz & Roza Checklist ---
    fun shiftPrayerDate(deltaDays: Int) {
        val next = repo.offsetDateKey(_selectedPrayerDate.value, deltaDays)
        _selectedPrayerDate.value = next
        loadPrayersForDate(next)
    }

    fun selectPrayerDate(dateKey: String) {
        _selectedPrayerDate.value = dateKey
        loadPrayersForDate(dateKey)
    }

    fun formatDateBn(dateKey: String): String = repo.formatDateBn(dateKey)

    fun isToday(dateKey: String): Boolean = dateKey == repo.todayDateKey()

    private fun loadPrayersForDate(dateKey: String) {
        viewModelScope.launch {
            _isPrayersLoading.value = true
            val checklist = repo.getPrayersChecklist(localUser.value.myId, dateKey)
            _prayerChecklist.value = checklist
            _isPrayersLoading.value = false
        }
    }

    fun togglePrayerItem(key: String, checked: Boolean) {
        val date = _selectedPrayerDate.value
        val updated = _prayerChecklist.value.toggle(key, checked).copy(date = date)
        _prayerChecklist.value = updated
        viewModelScope.launch {
            repo.savePrayersChecklist(localUser.value.myId, date, updated)
        }
    }

    // --- Amalnama ---
    fun setAmalnamaRangeMode(rangeMode: Boolean) {
        _isRangeMode.value = rangeMode
        val today = repo.todayDateKey()
        if (!rangeMode) {
            _amalStartDate.value = today
            _amalEndDate.value = today
        } else {
            _amalStartDate.value = repo.offsetDateKey(today, -6)
            _amalEndDate.value = today
        }
        refreshAmalnama()
    }

    fun shiftAmalSingleDay(deltaDays: Int) {
        val next = repo.offsetDateKey(_amalStartDate.value, deltaDays)
        _amalStartDate.value = next
        _amalEndDate.value = next
        refreshAmalnama()
    }

    fun selectQuickRange(daysBack: Int) {
        val today = repo.todayDateKey()
        _isRangeMode.value = true
        _amalStartDate.value = repo.offsetDateKey(today, -(daysBack - 1))
        _amalEndDate.value = today
        refreshAmalnama()
    }

    fun setCustomAmalRange(startDate: String, endDate: String) {
        _isRangeMode.value = true
        _amalStartDate.value = startDate
        _amalEndDate.value = endDate
        refreshAmalnama()
    }

    fun refreshAmalnama() {
        viewModelScope.launch {
            _isAmalnamaLoading.value = true
            val summary = repo.getAmalnamaSummary(
                myId = localUser.value.myId,
                startDate = _amalStartDate.value,
                endDate = _amalEndDate.value,
                allDhikrs = allDhikrs.value
            )
            _amalnamaSummary.value = summary
            _isAmalnamaLoading.value = false
        }
    }

    // --- Leaderboard ---
    fun refreshLeaderboard() {
        viewModelScope.launch {
            _isLeaderboardLoading.value = true
            _leaderboardError.value = null
            val res = repo.getLeaderboard(localUser.value)
            _isLeaderboardLoading.value = false
            res.fold(
                onSuccess = { _leaderboard.value = it },
                onFailure = {
                    _leaderboardError.value = "র‍্যাংকিং লোড করা যায়নি। ইন্টারনেট সংযোগ পরীক্ষা করুন।"
                }
            )
        }
    }
}
