package com.familytv.app.presentation.detail

import androidx.lifecycle.*
import com.familytv.app.data.model.PlayEpisode
import com.familytv.app.data.model.VodItem
import com.familytv.app.data.repository.VodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: VodRepository
) : ViewModel() {

    private val _detail = MutableLiveData<VodItem>()
    val detail: LiveData<VodItem> = _detail

    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> = _isFavorite

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Idle)
    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()

    var currentEpisodeIndex: Int = 0

    fun loadDetail(id: Long) {
        viewModelScope.launch {
            try {
                _loadState.value = LoadState.Loading
                val result = repository.getVideoDetail(id)
                if (result.isSuccess) {
                    val video = result.getOrNull()
                    _detail.postValue(video)
                    if (video != null) {
                        checkFavorite(id)
                    }
                    _loadState.value = LoadState.Success
                } else {
                    _loadState.value = LoadState.Error(result.exceptionOrNull()?.message ?: "加载失败")
                }
            } catch (e: Exception) {
                _loadState.value = LoadState.Error(e.message ?: "网络异常")
            }
        }
    }

    fun parseEpisodes(vodItem: VodItem): List<PlayEpisode> {
        return repository.parseEpisodes(vodItem)
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            try {
                val video = _detail.value ?: return@launch
                if (_isFavorite.value == true) {
                    repository.removeFromFavorite(video.vodId)
                } else {
                    repository.addToFavorite(video)
                }
                checkFavorite(video.vodId)
            } catch (e: Exception) {
            }
        }
    }

    private suspend fun checkFavorite(id: Long) {
        try {
            _isFavorite.postValue(repository.isFavorite(id))
        } catch (e: Exception) {
            _isFavorite.postValue(false)
        }
    }

    suspend fun getHistoryProgress(id: Long): Pair<Int, Long>? {
        return try {
            repository.getHistoryProgress(id)
        } catch (e: Exception) {
            null
        }
    }

    sealed class LoadState {
        object Idle : LoadState()
        object Loading : LoadState()
        object Success : LoadState()
        data class Error(val message: String) : LoadState()
    }
}
