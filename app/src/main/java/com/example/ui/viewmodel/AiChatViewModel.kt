package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserPreferencesRepository
import com.example.data.repository.AiAssistantRole
import com.example.data.repository.AiChatMessage
import com.example.data.repository.AiCompanionRepository
import com.example.data.repository.PlaceholderAiCompanionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AiChatViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = UserPreferencesRepository(application)
    private val aiRepo: AiCompanionRepository = PlaceholderAiCompanionRepository()

    val isPlaceholderMode: Boolean = aiRepo.isPlaceholderMode

    val selectedCity: StateFlow<String> = prefsRepo.selectedCityFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "Dhaka")

    private val _selectedRole = MutableStateFlow(AiAssistantRole.GENERAL_COMPANION)
    val selectedRole: StateFlow<AiAssistantRole> = _selectedRole.asStateFlow()

    private val _messages = MutableStateFlow(
        listOf(
            AiChatMessage(
                id = "welcome_msg",
                isUser = false,
                text = "আসসালামু আলাইকুম! আমি UMMAH DAILY ইসলামিক সহায়ক। নামাজ, রোজা, দোয়া বা নিকটস্থ মসজিদ সম্পর্কে যে কোনো প্রশ্ন করতে পারেন।\n\n(নোট: Firebase AI Logic সক্রিয় না থাকায় বর্তমানে নমুনা উত্তর প্রদর্শিত হচ্ছে।)",
                isPlaceholderNotice = true
            )
        )
    )
    val messages: StateFlow<List<AiChatMessage>> = _messages.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    fun selectRole(role: AiAssistantRole) {
        _selectedRole.value = role
    }

    fun sendMessage(rawText: String) {
        val text = rawText.trim()
        if (text.isEmpty() || _isSending.value) return

        val userMsg = AiChatMessage(
            id = "user_${System.currentTimeMillis()}",
            isUser = true,
            text = text
        )
        val currentHistory = _messages.value + userMsg
        _messages.value = currentHistory
        _isSending.value = true

        viewModelScope.launch {
            try {
                val reply = aiRepo.sendMessage(
                    history = currentHistory,
                    userMessage = text,
                    role = _selectedRole.value,
                    userLocationHint = "${selectedCity.value}, Bangladesh"
                )
                _messages.value = _messages.value + reply
            } finally {
                _isSending.value = false
            }
        }
    }

    fun clearHistory() {
        _messages.value = listOf(
            AiChatMessage(
                id = "welcome_${System.currentTimeMillis()}",
                isUser = false,
                text = "নতুন কথোপকথন শুরু হয়েছে। আপনার প্রশ্ন লিখুন।",
                isPlaceholderNotice = true
            )
        )
    }
}
