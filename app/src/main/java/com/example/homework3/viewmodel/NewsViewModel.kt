package com.example.homework3.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.homework3.LogKeys
import com.example.homework3.model.DataStatus
import com.example.homework3.model.NewsItem
import com.example.homework3.model.StateWrapper
import com.example.homework3.repository.NewsDataRepository
import com.example.homework3.repository.NewsAPIRepository
import com.example.homework3.repository.SettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.net.MalformedURLException
import java.text.ParseException

class NewsViewModel(
    private val newsAPIRepository: NewsAPIRepository,
    private val newsDataRepository: NewsDataRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

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
        stateWrapper: StateWrapper<List<NewsItem>>,
        newsItems: List<NewsItem>
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

    private fun fetchCards() {
        viewModelScope.launch {
            _newsItemsAPI.postValue(StateWrapper.loading())
            val newsFeedUrl = settingsDataStore.settings.first().newsFeedUrl
            newsDataRepository.deleteAllNewsItems()

            try {

                val fetchCards = newsAPIRepository.fetchNews(newsFeedUrl)
                _newsItemsAPI.postValue(fetchCards)

                // Store the fetched news items into the database
                if (fetchCards.status == DataStatus.SUCCESS) {
                    newsDataRepository.insertNewsItems(fetchCards.data!!)
                }

                Log.i(
                    LogKeys.BASIC_KEY,
                    "Fetched from endpoint with state:" + fetchCards.status.toString()
                )
            } catch (ex: MalformedURLException) {
                _newsItemsAPI.postValue(
                    StateWrapper.error(
                        "MalformedURLException: Cannot fetch from $newsFeedUrl",
                        ex
                    )
                )
                ex.printStackTrace()
            } catch (ex: IOException) {
                _newsItemsAPI.postValue(StateWrapper.error("Error occurred while fetching.", ex))
                ex.printStackTrace()
            } catch (ex: ParseException) {
                _newsItemsAPI.postValue(StateWrapper.error("Error occurred while parsing.", ex))
                ex.printStackTrace()
            } catch (ex: XmlPullParserException) {
                _newsItemsAPI.postValue(StateWrapper.error("Error occurred while parsing.", ex))
                ex.printStackTrace()
            }
        }
    }

    fun reload() {
        Log.i(LogKeys.BASIC_KEY, "Reloading...")
        fetchCards()
    }

    class NewsViewModelFactory(
        private val newsAPIRepository: NewsAPIRepository,
        private val newsDataRepository: NewsDataRepository,
        private val settingsDataStore: SettingsDataStore
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NewsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NewsViewModel(
                    settingsDataStore = settingsDataStore,
                    newsAPIRepository = newsAPIRepository,
                    newsDataRepository = newsDataRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
