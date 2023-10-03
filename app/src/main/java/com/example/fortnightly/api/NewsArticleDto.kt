package com.example.fortnightly.api

import com.example.fortnightly.data.NewsArticle

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

fun NewsArticleDto.asDomainArticle(category: String, currentTimeInMillis: Long) =
    NewsArticle(
        title = title ?: "",
        url = url ?: "",
        content = content ?: "",
        description = description ?: "",
        source = source.name ?: "",
        category = category,
        publishedAt = publishedAt ?: "",
        urlToImage = urlToImage ?: "",
        updateAt = currentTimeInMillis
    )