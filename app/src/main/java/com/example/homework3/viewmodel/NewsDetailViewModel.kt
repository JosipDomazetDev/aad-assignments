package com.example.homework3.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.homework3.model.NewsItem

class NewsDetailViewModel() : ViewModel() {
   private val currentNewsItem = MutableLiveData<NewsItem>()

    fun getCurrentNewsItem(): LiveData<NewsItem> {
        return currentNewsItem
    }

    fun setCurrentNewsItem(newsItem: NewsItem) {
        currentNewsItem.value = newsItem
    }
}
