package com.example.homework3.viewmodel

import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.homework3.model.DataStatus
import com.example.homework3.model.LogKeys
import com.example.homework3.model.NewsItem
import com.example.homework3.model.StateWrapper
import com.example.homework3.repository.NewsRepository
import com.example.homework3.repository.SettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.net.MalformedURLException
import java.text.ParseException

class NewsViewModel(
    private val repo: NewsRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _newsItems = MutableLiveData<StateWrapper<List<NewsItem>>>()

    val newsItems: LiveData<StateWrapper<List<NewsItem>>>
        get() = _newsItems


    fun fetchCardsInitially() {
        if (_newsItems.value?.status == DataStatus.SUCCESS) {
            return
        }

        fetchCards()
    }

    fun fetchCards() {
        viewModelScope.launch {
            _newsItems.postValue(StateWrapper.loading())
            val newsFeedUrl = settingsDataStore.settings.first().newsFeedUrl

            try {
                val fetchCards = repo.fetchNews(newsFeedUrl)
                _newsItems.postValue(fetchCards)

                Log.i(LogKeys.BASIC_KEY, "Fetched from endpoint with state:" + fetchCards.status.toString())
            } catch (ex: MalformedURLException) {
                _newsItems.postValue(
                    StateWrapper.error(
                        "MalformedURLException: Cannot fetch from $newsFeedUrl",
                        ex
                    )
                )
                ex.printStackTrace()
            } catch (ex: IOException) {
                _newsItems.postValue(StateWrapper.error("Error occurred while fetching.", ex))
                ex.printStackTrace()
            } catch (ex: ParseException) {
                _newsItems.postValue(StateWrapper.error("Error occurred while parsing.", ex))
                ex.printStackTrace()
            } catch (ex: XmlPullParserException) {
                _newsItems.postValue(StateWrapper.error("Error occurred while parsing.", ex))
                ex.printStackTrace()
            }
        }
    }

//    // Define ViewModel factory in a companion object
//    companion object {
//
//        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
//            @Suppress("UNCHECKED_CAST")
//            override fun <T : ViewModel> create(
//                modelClass: Class<T>,
//                extras: CreationExtras
//            ): T {
//                // Get the Application object from extras
//                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
//                // Create a SavedStateHandle for this ViewModel from extras
//                val savedStateHandle = extras.createSavedStateHandle()
//
//                return NewsViewModel(
//                    // TODO HOW DO I ACCESS THE DEPENDENCIES ??
//                ) as T
//            }
//        }
//    }
}
