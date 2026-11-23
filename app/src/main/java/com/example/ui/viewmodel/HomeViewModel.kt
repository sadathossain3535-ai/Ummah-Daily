package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.PrayerAlarmScheduler
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.AladhanResponse
import com.example.data.model.DailyContentCatalog
import com.example.data.model.DailyDua
import com.example.data.model.DailyHadith
import com.example.data.model.FardPrayerItem
import com.example.data.model.PrayerScheduleState
import com.example.data.repository.PrayerRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val schedule: PrayerScheduleState,
        val isRefreshing: Boolean = false
    ) : HomeUiState
    data class Error(val messageBn: String) : HomeUiState
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = UserPreferencesRepository(application)
    private val prayerRepo = PrayerRepository(prefsRepo)

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val selectedCity: StateFlow<String> = prefsRepo.selectedCityFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "Dhaka")

    val enabledAlarms: StateFlow<Set<String>> = prefsRepo.enabledAlarmsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    private val _selectedPrayerDetail = MutableStateFlow<FardPrayerItem?>(null)
    val selectedPrayerDetail: StateFlow<FardPrayerItem?> = _selectedPrayerDetail.asStateFlow()

    private val _showShareModal = MutableStateFlow(false)
    val showShareModal: StateFlow<Boolean> = _showShareModal.asStateFlow()

    val todayDua: DailyDua = DailyContentCatalog.getTodayDua()
    val todayHadith: DailyHadith = DailyContentCatalog.getTodayHadith()

    private var lastRawResponse: AladhanResponse? = null
    private var lastCity: String = "Dhaka"
    private var lastIsFromCache: Boolean = false
    private var tickerJob: Job? = null

    init {
        viewModelScope.launch {
            prefsRepo.selectedCityFlow.collectLatest { city ->
                loadTimingsForCity(city)
            }
        }
    }

    fun selectCity(cityApiName: String) {
        viewModelScope.launch {
            prefsRepo.setSelectedCity(cityApiName)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            loadTimingsForCity(selectedCity.value)
        }
    }

    fun toggleAlarm(prayerId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val enabledNow = prefsRepo.togglePrayerAlarm(prayerId)
            syncCurrentScheduleAlarms()
            onResult(enabledNow)
        }
    }

    fun syncCurrentScheduleAlarms() {
        val currentSchedule = (_uiState.value as? HomeUiState.Success)?.schedule ?: return
        viewModelScope.launch {
            val enabled = prefsRepo.enabledAlarmsFlow.first()
            PrayerAlarmScheduler.syncPrayerAlarms(
                context = getApplication(),
                prayers = currentSchedule.fardPrayers,
                enabledIds = enabled
            )
        }
    }

    fun openPrayerDetail(prayer: FardPrayerItem) {
        _selectedPrayerDetail.value = prayer
    }

    fun closePrayerDetail() {
        _selectedPrayerDetail.value = null
    }

    fun openShareModal() {
        _showShareModal.value = true
    }

    fun closeShareModal() {
        _showShareModal.value = false
    }

    private suspend fun loadTimingsForCity(city: String) {
        lastCity = city
        val cached = prayerRepo.getCachedResponse(city)
        if (cached != null) {
            lastRawResponse = cached
            lastIsFromCache = true
            val cachedState = prayerRepo.buildScheduleState(cached, city, isFromCache = true)
            _uiState.value = HomeUiState.Success(
                schedule = cachedState,
                isRefreshing = true
            )
            syncCurrentScheduleAlarms()
            startTicker()
        } else {
            _uiState.value = HomeUiState.Loading
        }

        val result = prayerRepo.fetchPrayerTimings(city)
        result.fold(
            onSuccess = { fresh ->
                lastRawResponse = fresh
                lastIsFromCache = false
                val freshState = prayerRepo.buildScheduleState(fresh, city, isFromCache = false)
                _uiState.value = HomeUiState.Success(
                    schedule = freshState,
                    isRefreshing = false
                )
                syncCurrentScheduleAlarms()
                startTicker()
            },
            onFailure = {
                val existing = lastRawResponse
                if (existing != null) {
                    _uiState.value = HomeUiState.Success(
                        schedule = prayerRepo.buildScheduleState(existing, city, isFromCache = true),
                        isRefreshing = false
                    )
                } else {
                    _uiState.value = HomeUiState.Error(
                        "ইন্টারনেট সংযোগ পরীক্ষা করুন। আল-আযান সার্ভার থেকে নামাজের সময়সূচী লোড করা যায়নি।"
                    )
                }
            }
        )
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val raw = lastRawResponse ?: continue
                val currentRefreshing = (_uiState.value as? HomeUiState.Success)?.isRefreshing ?: false
                _uiState.value = HomeUiState.Success(
                    schedule = prayerRepo.buildScheduleState(raw, lastCity, lastIsFromCache),
                    isRefreshing = currentRefreshing
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
    }
}
