package com.example.fortnightly.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FortnightlyArticlesDao {

    @Query("SELECT * FROM articles_category INNER JOIN news_articles ON articleUrl = url WHERE articleCategory= :category")
    fun getArticlesCategory(category: String): Flow<List<NewsArticle>>

    @Query("SELECT * FROM news_articles WHERE url = :articleUrl")
    fun getArticle(articleUrl: String): Flow<NewsArticle>

    // This query is used only for testing purposes.
    @Query("SELECT * FROM news_articles")
    fun getNewsArticles(): Flow<List<NewsArticle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewsArticles(newsArticles: List<NewsArticle>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticlesCategory(articlesCategory: List<ArticlesCategory>)

    @Query("DELETE FROM news_articles WHERE category = :category")
    suspend fun deleteNewsArticles(category: String)

    @Query("DELETE FROM articles_category WHERE articleCategory = :category")
    suspend fun deleteArticlesCategory(category: String)

    @Query("DELETE FROM news_articles WHERE updateAt < :timestampInMillis")
    suspend fun deleteNewsArticlesOlderThen(timestampInMillis: Long)
}