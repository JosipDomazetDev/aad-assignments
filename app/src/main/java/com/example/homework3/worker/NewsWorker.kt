package com.example.homework3.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.homework3.LogKeys
import com.example.homework3.model.DataStatus
import com.example.homework3.model.NewsItem
import com.example.homework3.repository.persistence.ImageManager
import com.example.homework3.repository.NewsAPIRepository
import com.example.homework3.repository.NewsDataRepository
import com.example.homework3.repository.persistence.NewsDatabase
import com.example.homework3.ui.notification.NotificationManager
import java.util.Calendar
import java.util.Date

class NewsWorker(
    appContext: Context,
    params: WorkerParameters,
) :
    CoroutineWorker(appContext, params) {

    companion object {
        const val URL = "url"
        const val SOFT_MODE = "soft_mode"
        const val DOWNLOAD_IMAGES = "download_images"
    }

    override suspend fun doWork(): Result {
        return try {
            val newsFeedUrl = inputData.getString(URL)!!
            val isSoftMode = inputData.getBoolean(SOFT_MODE, false)
            val downloadImagesInBackground = inputData.getBoolean(DOWNLOAD_IMAGES, false)

            Log.i(LogKeys.BASIC_KEY, "Fetching from $newsFeedUrl")

            val applicationContext = applicationContext
            val newsDatabase = NewsDatabase.getInstance(applicationContext)
            val newsAPIRepository = NewsAPIRepository()
            val imageManager = ImageManager(context = applicationContext)
            val newsDataRepository = NewsDataRepository(newsDatabase, imageManager)

            val fetchCards = newsAPIRepository.fetchNews(newsFeedUrl)

            if (fetchCards.status == DataStatus.SUCCESS) {
                if (isSoftMode) {
                    deleteNewsItemOlderThanFiveDays(newsDataRepository)
                } else {
                    Log.i(LogKeys.BASIC_KEY, "Clearing all news items")
                    newsDataRepository.deleteAllNewsItems()
                }

                val newsItemFromAPI = fetchCards.data!!
                val oldNewsItems = newsDataRepository.getAllNewsItemsRaw()
                newsDataRepository.insertNewsItems(newsItemFromAPI, downloadImagesInBackground)

                createNotifications(newsItemFromAPI, oldNewsItems, applicationContext)

                Log.i(
                    LogKeys.BASIC_KEY,
                    "Inserted ${fetchCards.data.size} news items into database"
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }


    private suspend fun deleteNewsItemOlderThanFiveDays(newsDataRepository: NewsDataRepository) {
        Log.i(LogKeys.BASIC_KEY, "Clearing news items older than 5 days")

        val currentDate = Date()
        val fiveDaysAgo = Calendar.getInstance().apply {
            time = currentDate
            add(Calendar.DAY_OF_MONTH, -5)
        }.time
        newsDataRepository.deleteOldNewsItems(fiveDaysAgo)
    }

    private fun createNotifications(
        newsItems: List<NewsItem>,
        oldNewsItems: List<NewsItem>?,
        applicationContext: Context
    ) {
        val newNewsItems = newsItems.filter { newsItem ->
            oldNewsItems?.none { it.id == newsItem.id } ?: true
        }

        for (newsItem in newNewsItems) {
            NotificationManager.showNotification(applicationContext, newsItem)
        }
    }
}