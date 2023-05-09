package com.example.homework3.repository

import com.example.homework3.model.NewsItem
import com.example.homework3.model.StateWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class NewsRepository {

    suspend fun fetchNews(newsFeedUrl: String): StateWrapper<List<NewsItem>> = withContext(Dispatchers.IO) {
        val items: List<NewsItem> = loadXmlFromNetwork(newsFeedUrl)
        return@withContext StateWrapper.success(items.sortedByDescending { it.publicationDate })
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun loadXmlFromNetwork(urlString: String): List<NewsItem> {
        val entries: List<NewsItem> = (downloadUrl(urlString)?.use { stream ->
            // Instantiates the parser.
            NewsFeedParser().parse(stream)
        } ?: emptyList())

        return entries
    }

    @Throws(IOException::class)
    private fun downloadUrl(urlString: String): InputStream? {
        val url = URL(urlString)
        return (url.openConnection() as? HttpURLConnection)?.run {
            readTimeout = 10000
            connectTimeout = 15000
            requestMethod = "GET"
            doInput = true
            // Starts the query.
            connect()
            inputStream
        }
    }

}