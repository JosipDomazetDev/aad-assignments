package com.example.homework3.repository.persistence

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
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

        suspend fun getLocalImage(context: Context, newsItem: NewsItem) : Bitmap {
            val cachedImageFile = context.cacheDir.resolve(newsItem.getImagePath())

            return withContext(Dispatchers.IO) {
                BitmapFactory.decodeFile(cachedImageFile.absolutePath)
            }
        }


        suspend fun getUrlImage(url: String, context: Context): Bitmap? {
            return withContext(Dispatchers.IO) {
                try {
                    val imageLoader = ImageLoader.Builder(context)
                        .build()

                    val request = ImageRequest.Builder(context)
                        .data(url)
                        .build()

                    val result = (imageLoader.execute(request) as SuccessResult).drawable
                    if (result is BitmapDrawable) {
                        return@withContext result.bitmap
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return@withContext null
            }
        }

    }
}