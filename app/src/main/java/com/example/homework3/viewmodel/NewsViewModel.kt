package com.example.homework3.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homework3.R
import com.example.homework3.model.DataStatus
import com.example.homework3.model.NewsItem
import com.example.homework3.model.StateWrapper
import com.example.homework3.repository.NewsRepository
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.text.ParseException

class NewsViewModel(private val repo: NewsRepository) : ViewModel() {
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

            try {
                val fetchCards = repo.fetchNews()
                _newsItems.postValue(fetchCards)

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
}
