package com.familytv.app.presentation.search

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.*
import com.familytv.app.data.model.VodItem
import com.familytv.app.data.repository.VodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: VodRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _searchResults = MutableLiveData<List<VodItem>>()
    val searchResults: LiveData<List<VodItem>> = _searchResults

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Idle)
    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()

    private val prefs: SharedPreferences = context.getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
    private val HISTORY_KEY = "search_history"

    fun search(keyword: String) {
        viewModelScope.launch {
            try {
                _loadState.value = LoadState.Loading
                val result = repository.searchVideo(keyword = keyword, page = 1)
                if (result.isSuccess) {
                    _searchResults.postValue(result.getOrNull() ?: emptyList())
                    _loadState.value = LoadState.Success
                } else {
                    _searchResults.postValue(emptyList())
                    _loadState.value = LoadState.Error(result.exceptionOrNull()?.message ?: "搜索失败")
                }
            } catch (e: Exception) {
                _searchResults.postValue(emptyList())
                _loadState.value = LoadState.Error(e.message ?: "网络异常")
            }
        }
    }

    fun saveHistory(keyword: String) {
        viewModelScope.launch {
            try {
                val history = getHistoryList().toMutableList()
                history.remove(keyword)
                history.add(0, keyword)
                if (history.size > 20) history.removeAt(20)
                saveHistoryList(history)
            } catch (e: Exception) {
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            try {
                prefs?.edit()?.remove(HISTORY_KEY)?.apply()
            } catch (e: Exception) {
            }
        }
    }

    fun getHistory(): Flow<List<String>> = flow {
        emit(getHistoryList())
    }

    private fun getHistoryList(): List<String> {
        return try {
            prefs?.getString(HISTORY_KEY, "")
                ?.split("|")
                ?.filter { it.isNotEmpty() }
                ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveHistoryList(history: List<String>) {
        try {
            prefs?.edit()?.putString(HISTORY_KEY, history.joinToString("|"))?.apply()
        } catch (e: Exception) {
        }
    }

    sealed class LoadState {
        object Idle : LoadState()
        object Loading : LoadState()
        object Success : LoadState()
        data class Error(val message: String) : LoadState()
    }
}
