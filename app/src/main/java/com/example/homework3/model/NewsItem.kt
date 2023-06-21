package com.example.homework3.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "news_items")
class NewsItem(
    @PrimaryKey
    var id: String,
    var title: String,
    var description: String,
    var imageUrl: String,
    var author: String,
    var publicationDate: Date,
    var fullArticleLink: String,
    var keywords: List<String>
) {

    override fun toString(): String {
        return "NewsItem(id='$id', title='$title', description='$description', imageUrl='$imageUrl', author='$author', publicationDate=$publicationDate, fullArticleLink='$fullArticleLink', keywords=$keywords)"
    }

    fun getImagePath(): String {
        return imageUrl.hashCode().toString()
    }
}
