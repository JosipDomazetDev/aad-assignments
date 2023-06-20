package com.example.homework3.repository

import androidx.lifecycle.LiveData
import com.example.homework3.model.NewsItem
import com.example.homework3.repository.db.NewsDatabase
import com.example.homework3.repository.db.NewsItemDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class NewsDataRepository(database: NewsDatabase) {
    private val newsItemDao: NewsItemDao = database.newsItemDao()

    val newsItems: LiveData<List<NewsItem>> = newsItemDao.getAllNewsItems()

    suspend fun insertNewsItems(newsItems: List<NewsItem>) {
        withContext(Dispatchers.IO) {
            newsItemDao.insertNewsItems(newsItems)
        }
    }

    suspend fun deleteAllNewsItems() {
        withContext(Dispatchers.IO) {
            newsItemDao.deleteAllNewsItems()
        }
    }

    suspend fun isEmpty(): Boolean {
        return newsItemDao.getSize() == 0
    }

    suspend fun deleteOldNewsItems(pastDate: Date) {
        withContext(Dispatchers.IO) {
            newsItemDao.deleteOlderThan(pastDate.time)
        }
    }
}
