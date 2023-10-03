package com.example.fortnightly.api

data class NewsArticleDto(
    val title: String?,
    val url: String?,
    val content: String?,
    val description: String?,
    val source: Source,
    val category: String,
    val publishedAt: String?,
    val urlToImage: String?
)