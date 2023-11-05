package com.example.fortnightly.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news_articles")
data class NewsArticle(
    val title: String,
    @PrimaryKey val url: String,
    val content: String,
    val description: String,
    val source: String,
    val category: String,
    val publishedAt: String,
    val updateAt: Long,
    val urlToImage: String
)
