package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppUpdateInfo
import com.example.data.repository.AppUpdateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppUpdateViewModel : ViewModel() {

    private val repository = AppUpdateRepository()

    private val _availableUpdate = MutableStateFlow<AppUpdateInfo?>(null)
    val availableUpdate: StateFlow<AppUpdateInfo?> = _availableUpdate.asStateFlow()

    private var dismissedVersionCode: Int? = null

    init {
        checkForUpdate()
    }

    fun checkForUpdate() {
        viewModelScope.launch {
            val info = repository.checkForAvailableUpdate() ?: return@launch
            if (info.forceUpdate || dismissedVersionCode != info.latestVersionCode) {
                _availableUpdate.value = info
            }
        }
    }

    fun dismissOptionalUpdate() {
        val current = _availableUpdate.value ?: return
        if (!current.forceUpdate) {
            dismissedVersionCode = current.latestVersionCode
            _availableUpdate.value = null
        }
    }
}
