package com.example.homework3.worker

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.homework3.LogKeys
import java.util.concurrent.TimeUnit

private const val PERIODIC_NEWSWORKER_NAME = "PERIODIC_NEWSWORKER"
private const val SINGLE_NEWSWORKER_NAME = "SINGLE_NEWSWORKER"

class NewsWorkerQueueManager(private val context: Context) {
    fun enqueueDownloadTask(
        url: String, isSoftMode: Boolean, downloadImagesInBackground: Boolean
    ): LiveData<WorkInfo> {
        val workManager = WorkManager.getInstance(context)

        val request = OneTimeWorkRequestBuilder<NewsWorker>().setConstraints(
            Constraints(
                requiresBatteryNotLow = true
            )
        ).setInputData(
            inputData = workDataOf(
                NewsWorker.URL to url,
                NewsWorker.SOFT_MODE to isSoftMode,
                NewsWorker.DOWNLOAD_IMAGES to downloadImagesInBackground,
            )
        ).build()
        workManager.enqueueUniqueWork(SINGLE_NEWSWORKER_NAME, ExistingWorkPolicy.REPLACE, request)

        return workManager.getWorkInfoByIdLiveData(request.id)
    }


    fun enqueuePeriodicDownloadTask(
        url: String, downloadImagesInBackground: Boolean
    ) {
        val workManager = WorkManager.getInstance(context)

        val repeatInterval: Long = 30
        val periodicRequest = PeriodicWorkRequestBuilder<NewsWorker>(
            repeatInterval, TimeUnit.MINUTES
        ).setInitialDelay(repeatInterval, TimeUnit.MINUTES)
            .setConstraints(
                Constraints(
                    requiresBatteryNotLow = true
                )
            ).setInputData(
                inputData = workDataOf(
                    NewsWorker.URL to url,
                    NewsWorker.SOFT_MODE to true,
                    NewsWorker.DOWNLOAD_IMAGES to downloadImagesInBackground
                )
            )

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_NEWSWORKER_NAME, ExistingPeriodicWorkPolicy.UPDATE, periodicRequest.build()
        )

        Log.i(LogKeys.BASIC_KEY, "Enqueued periodic task")
    }
}