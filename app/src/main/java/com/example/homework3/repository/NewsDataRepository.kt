package com.example.homework3.repository

import androidx.lifecycle.LiveData
import com.example.homework3.model.NewsItem
import com.example.homework3.repository.persistence.ImageManager
import com.example.homework3.repository.persistence.NewsDatabase
import com.example.homework3.repository.persistence.NewsItemDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class NewsDataRepository(database: NewsDatabase, private val imageManager: ImageManager) {
    private val newsItemDao: NewsItemDao = database.newsItemDao()

    val newsItems: LiveData<List<NewsItem>> = newsItemDao.getAllNewsItems()

    suspend fun getAllNewsItemsRaw(): List<NewsItem> {
        return withContext(Dispatchers.IO) {
            newsItemDao.getAllNewsItemsRaw()
        }
    }

    suspend fun insertNewsItems(newsItems: List<NewsItem>, downloadImagesInBackground: Boolean) {
        withContext(Dispatchers.IO) {
            newsItemDao.insertNewsItems(newsItems)
            if (downloadImagesInBackground) {
                imageManager.downloadImages(getAllNewsItemsRaw())
            }
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
