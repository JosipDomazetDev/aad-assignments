package com.example.homework3.viewmodel

import android.util.Log
import androidx.lifecycle.*
import androidx.work.WorkInfo
import com.example.homework3.LogKeys
import com.example.homework3.model.DataStatus
import com.example.homework3.model.NewsItem
import com.example.homework3.model.StateWrapper
import com.example.homework3.repository.NewsDataRepository
import com.example.homework3.repository.SettingsDataStore
import com.example.homework3.worker.NewsWorkerQueueManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

class NewsViewModel(
    private val newsDataRepository: NewsDataRepository,
    private val settingsDataStore: SettingsDataStore,
    private val newsWorkerQueueManager: NewsWorkerQueueManager
) : ViewModel() {

    init {
        viewModelScope.launch {
            if (newsDataRepository.isEmpty()) {
                reload(isSoftMode = false)
            }

            val newsFeedUrl = settingsDataStore.settings.first().newsFeedUrl
            newsWorkerQueueManager.enqueuePeriodicDownloadTask(newsFeedUrl)
        }
    }

    private val _newsItemsAPI =
        MutableLiveData<StateWrapper<List<NewsItem>>>(StateWrapper.cached(null))

    // Combine the news items from the database with the state of the API call
    val newsItemsMerged: LiveData<StateWrapper<List<NewsItem>>> =
        _newsItemsAPI.switchMap { stateWrapper ->
            newsDataRepository.newsItems.map { newsItems ->
                combineStateWithNewsItems(stateWrapper, newsItems)
            }
        }

    private fun combineStateWithNewsItems(
        stateWrapper: StateWrapper<List<NewsItem>>, newsItems: List<NewsItem>
    ): StateWrapper<List<NewsItem>> {
        return when (stateWrapper.status) {
            DataStatus.SUCCESS -> {
                StateWrapper.success(newsItems)
            }

            DataStatus.CACHED -> {
                Log.i(LogKeys.BASIC_KEY, "Loaded ${newsItems.size} news items from cache")
                StateWrapper.cached(newsItems)
            }

            else -> {
                stateWrapper
            }
        }
    }

    fun reload(isSoftMode: Boolean, urlHasChanged: Boolean = false) {
        viewModelScope.launch {
            Log.i(LogKeys.BASIC_KEY, "Reloading...")
            val newsFeedUrl = settingsDataStore.settings.first().newsFeedUrl

            if (urlHasChanged) {
                // Reset the periodic download task because the URL has changed
                newsWorkerQueueManager.enqueuePeriodicDownloadTask(newsFeedUrl)
            }

            val asFlow =
                newsWorkerQueueManager.enqueueDownloadTask(newsFeedUrl, isSoftMode).asFlow()

            asFlow.collect {
                if (it == null) {
                    _newsItemsAPI.postValue(StateWrapper.loading())
                } else when (it.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        _newsItemsAPI.postValue(StateWrapper.success(null))
                        this.coroutineContext.job.cancel()
                    }

                    WorkInfo.State.FAILED -> {
                        _newsItemsAPI.postValue(
                            StateWrapper.error(
                                "Error occurred while fetching.", null
                            )
                        )
                        this.coroutineContext.job.cancel()
                    }

                    else -> {
                        _newsItemsAPI.postValue(StateWrapper.loading())
                    }
                }
            }
        }
    }

    class NewsViewModelFactory(
        private val newsDataRepository: NewsDataRepository,
        private val settingsDataStore: SettingsDataStore,
        private val newsWorkerQueueManager: NewsWorkerQueueManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NewsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST") return NewsViewModel(
                    newsDataRepository = newsDataRepository,
                    settingsDataStore = settingsDataStore,
                    newsWorkerQueueManager = newsWorkerQueueManager
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}