package com.example.homework3.repository.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.homework3.model.NewsItem


@Dao
interface NewsItemDao {
    @Query("SELECT * FROM news_items ORDER BY publicationDate DESC")
    fun getAllNewsItems(): LiveData<List<NewsItem>>

    @Query("SELECT * FROM news_items")
    fun getAllNewsItemsRaw(): List<NewsItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewsItems(newsItems: List<NewsItem>)

    @Query("DELETE FROM news_items")
    suspend fun deleteAllNewsItems()

    @Query("SELECT COUNT(*) FROM news_items")
    suspend fun getSize(): Int

    @Query("DELETE FROM news_items WHERE publicationDate < :deletionDate")
    suspend fun deleteOlderThan(deletionDate: Long)
}
