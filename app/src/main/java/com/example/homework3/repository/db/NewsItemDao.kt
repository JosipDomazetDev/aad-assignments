package com.example.homework3.repository.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.homework3.model.NewsItem


@Dao
interface NewsItemDao {
    @Query("SELECT * FROM news_items")
    fun getAllNewsItems(): LiveData<List<NewsItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewsItems(newsItems: List<NewsItem>)

    @Query("DELETE FROM news_items")
    suspend fun deleteAllNewsItems()
}
