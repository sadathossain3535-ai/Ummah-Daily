package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.model.DuaCatalog
import com.example.data.model.DuaCategory
import com.example.data.model.DuaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope

class DuaViewModel : ViewModel() {

    val categories: List<DuaCategory> = DuaCatalog.categories

    private val _selectedCategory = MutableStateFlow<DuaCategory?>(null)
    val selectedCategory: StateFlow<DuaCategory?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDua = MutableStateFlow<DuaItem?>(null)
    val selectedDua: StateFlow<DuaItem?> = _selectedDua.asStateFlow()

    val visibleDuas: StateFlow<List<DuaItem>> = combine(
        _selectedCategory,
        _searchQuery
    ) { category, query ->
        val base = if (category != null) {
            DuaCatalog.allDuas.filter { it.categoryId == category.id }
        } else {
            DuaCatalog.allDuas
        }
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            base
        } else {
            base.filter {
                it.titleBn.contains(trimmed, ignoreCase = true) ||
                    it.meaningBn.contains(trimmed, ignoreCase = true) ||
                    it.pronunciationBn.contains(trimmed, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, DuaCatalog.allDuas)

    fun countForCategory(categoryId: String): Int =
        DuaCatalog.allDuas.count { it.categoryId == categoryId }

    fun selectCategory(category: DuaCategory) {
        _selectedCategory.value = category
        _searchQuery.value = ""
    }

    fun clearCategory() {
        _selectedCategory.value = null
        _searchQuery.value = ""
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openDuaDetail(dua: DuaItem) {
        _selectedDua.value = dua
    }

    fun closeDuaDetail() {
        _selectedDua.value = null
    }
}
