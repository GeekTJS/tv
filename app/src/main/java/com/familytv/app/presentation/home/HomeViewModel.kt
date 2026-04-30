package com.familytv.app.presentation.home

import androidx.lifecycle.*
import com.familytv.app.data.model.VodItem
import com.familytv.app.data.repository.VodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: VodRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _videoList = MutableLiveData<List<VodItem>>()
    val videoList: LiveData<List<VodItem>> = _videoList

    private val _banners = MutableLiveData<List<VodItem>>()
    val banners: LiveData<List<VodItem>> = _banners

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Idle)
    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()

    private var currentCategoryId: Long = 1
    private var currentPage: Int = 1

    init {
        loadDefaultVideos()
    }

    fun loadVideosByCategory(categoryId: Long, page: Int = 1) {
        currentCategoryId = categoryId
        currentPage = page
        loadVideos()
    }

    private fun loadDefaultVideos() {
        loadVideosByCategory(1, 1)
    }

    private fun loadVideos() {
        viewModelScope.launch {
            try {
                _loadState.value = LoadState.Loading
                val result = repository.getVideoList(page = currentPage, typeId = currentCategoryId)
                _loadState.value = if (result.isSuccess) {
                    _videoList.value = result.getOrNull() ?: emptyList()
                    LoadState.Success
                } else {
                    _videoList.value = emptyList()
                    LoadState.Error(result.exceptionOrNull()?.message ?: "加载失败")
                }
            } catch (e: Exception) {
                _videoList.value = emptyList()
                _loadState.value = LoadState.Error(e.message ?: "网络异常")
            }
        }
    }

    fun loadBannerVideos() {
        viewModelScope.launch {
            val result = repository.getVideoList(page = 1, typeId = null)
            if (result.isSuccess) {
                val list = result.getOrNull() ?: emptyList()
                _banners.value = list.take(5)
            }
        }
    }

    sealed class LoadState {
        object Idle : LoadState()
        object Loading : LoadState()
        object Success : LoadState()
        data class Error(val message: String) : LoadState()
    }
}
