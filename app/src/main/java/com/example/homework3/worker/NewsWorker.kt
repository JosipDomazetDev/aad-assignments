package com.example.homework3.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.homework3.LogKeys
import com.example.homework3.model.DataStatus
import com.example.homework3.repository.NewsAPIRepository
import com.example.homework3.repository.NewsDataRepository
import com.example.homework3.repository.db.NewsDatabase
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
    }

    override suspend fun doWork(): Result {
        return try {
            val newsFeedUrl = inputData.getString(URL)!!
            val isSoftMode = inputData.getBoolean(SOFT_MODE, false)
            Log.i(LogKeys.BASIC_KEY, "Fetching from $newsFeedUrl")

            val applicationContext = applicationContext
            val newsDatabase = NewsDatabase.getInstance(applicationContext)
            val newsAPIRepository = NewsAPIRepository()
            val newsDataRepository = NewsDataRepository(newsDatabase)


            val fetchCards = newsAPIRepository.fetchNews(newsFeedUrl)

            if (fetchCards.status == DataStatus.SUCCESS) {
               if (!isSoftMode) {
                   Log.i(LogKeys.BASIC_KEY, "Clearing all news items")
                   newsDataRepository.deleteAllNewsItems()
                }
                val newsItems = fetchCards.data!!
                newsDataRepository.insertNewsItems(newsItems)

                if (isSoftMode){
                    Log.i(LogKeys.BASIC_KEY, "Clearing news items older than 5 days")
                    // Delete news items older than five days
                    val currentDate = Date()
                    val fiveDaysAgo = Calendar.getInstance().apply {
                        time = currentDate
                        add(Calendar.DAY_OF_MONTH, -5)
                    }.time
                    newsDataRepository.deleteOldNewsItems(fiveDaysAgo)
                }

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
}