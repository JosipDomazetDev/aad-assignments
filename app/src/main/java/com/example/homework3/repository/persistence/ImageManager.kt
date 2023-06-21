package com.example.homework3.repository.persistence

import android.content.Context
import android.util.Log
import coil.request.ImageRequest
import com.example.homework3.LogKeys
import com.example.homework3.model.NewsItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class ImageManager(private val context: Context) {
    suspend fun downloadImages(allNewsItems: List<NewsItem>) {
        withContext(Dispatchers.IO) {
            val cacheDir = context.cacheDir

            deleteOrphanedImages(cacheDir, allNewsItems)

            for (newsItem in allNewsItems) {
                val imageUrl = newsItem.imageUrl
                val filename = newsItem.getImagePath()
                val imageFile = File(cacheDir, filename)

                Log.i(LogKeys.BASIC_KEY, "Downloading image from ${imageUrl}...")
                downloadImage(imageUrl, imageFile)
            }
        }
    }


    private fun deleteOrphanedImages(cacheDir: File, newsItems: List<NewsItem>) {
        val imageFiles = cacheDir.listFiles()?.toList() ?: emptyList()

        val referencedFiles = newsItems.map { newsItem ->
            cacheDir.resolve(newsItem.getImagePath())
        }

        val orphanedFiles = imageFiles - referencedFiles.toSet()
        for (orphanedFile in orphanedFiles) {
            orphanedFile.delete()
        }
    }

    private fun downloadImage(imageUrl: String, imageFile: File) {
        try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()

            val inputStream = connection.inputStream
            val outputStream = FileOutputStream(imageFile)
            val buffer = ByteArray(4096)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.close()
            inputStream.close()
            connection.disconnect()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        fun getModel(
            newsItem: NewsItem,
            downloadImagesInBackground: Boolean,
            context: Context
        ): Any {
            return if (downloadImagesInBackground) {
                val cachedImageFile = context.cacheDir.resolve(newsItem.getImagePath())
                ImageRequest.Builder(context)
                    .data(cachedImageFile.absoluteFile)
                    .build()
            } else {
                newsItem.imageUrl
            }
        }
    }
}