package com.example.fortnightly.data

import androidx.paging.PagingSource
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

    @Query("SELECT * FROM search_result INNER JOIN news_articles ON articleUrl = url WHERE searchQuery = :query ORDER BY queryPosition")
    fun getSearchResultArticlesPaged(query: String): PagingSource<Int, NewsArticle>

    @Query("SELECT MAX(queryPosition) FROM search_result WHERE searchQuery = :searchQuery")
    suspend fun getLastQueryPosition(searchQuery: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchResults(searchResults: List<SearchResult>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewsArticles(newsArticles: List<NewsArticle>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticlesCategory(articlesCategory: List<ArticlesCategory>)

    @Query("DELETE FROM news_articles WHERE category = :category")
    suspend fun deleteNewsArticles(category: String)

    @Query("DELETE FROM articles_category WHERE articleCategory = :category")
    suspend fun deleteArticlesCategory(category: String)

    @Query("DELETE FROM search_result WHERE searchQuery = :query")
    suspend fun deleteSearchResultsForQuery(query: String)

    @Query("DELETE FROM news_articles WHERE updateAt < :timestampInMillis")
    suspend fun deleteNewsArticlesOlderThen(timestampInMillis: Long)
}