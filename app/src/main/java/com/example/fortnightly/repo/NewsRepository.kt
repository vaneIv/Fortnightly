package com.example.fortnightly.repo

import androidx.room.withTransaction
import com.example.fortnightly.api.ApiService
import com.example.fortnightly.api.asDomainArticle
import com.example.fortnightly.data.ArticlesCategory
import com.example.fortnightly.data.FortnightlyArticlesDatabase
import com.example.fortnightly.data.NewsArticle
import com.example.fortnightly.utils.Resource
import com.example.fortnightly.utils.TimeUtil
import com.example.fortnightly.utils.networkBoundResource
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NewsRepository @Inject constructor(
    private val apiService: ApiService,
    private val fortnightlyDb: FortnightlyArticlesDatabase,
    private val timeUtil: TimeUtil
) {

    private val fortnightlyDao = fortnightlyDb.fortnightlyArticlesDao()

    fun getArticles(
        category: String,
        forceRefresh: Boolean,
        onFetchSuccess: () -> Unit,
        onFetchFailed: (Throwable) -> Unit
    ): Flow<Resource<List<NewsArticle>>> =
        networkBoundResource(
            query = {
                fortnightlyDao.getArticlesCategory(category)
            },
            fetch = {
                val response = apiService.getTopHeadlines(category)
                response.articles
            },
            saveFetchResult = { serverArticles ->
                val newsArticles = serverArticles.map { newsArticleDto ->
                    newsArticleDto.asDomainArticle(
                        category,
                        timeUtil.getCurrentSystemTime()
                    )
                }

                val articlesCategory = newsArticles.map { newsArticle ->
                    ArticlesCategory(newsArticle.url, newsArticle.category)
                }

                fortnightlyDb.withTransaction {
                    fortnightlyDao.deleteArticlesCategory(category)
                    fortnightlyDao.insertNewsArticles(newsArticles)
                    fortnightlyDao.insertArticlesCategory(articlesCategory)
                }
            },
            shouldFetch = { cachedArticles ->
                if (forceRefresh) {
                    true
                } else {
                    val sortedArticles = cachedArticles.sortedBy { newsArticle ->
                        newsArticle.updateAt
                    }
                    val oldestTimestamp = sortedArticles.firstOrNull()?.updateAt
                    val needsRefresh = oldestTimestamp == null ||
                            oldestTimestamp < timeUtil.getCurrentSystemTime() -
                            TimeUnit.HOURS.toMillis(1)

                    needsRefresh
                }
            },
            onFetchSuccess = onFetchSuccess,
            onFetchFailed = { t ->
                if (t !is HttpException && t !is IOException) {
                    throw t
                }
                onFetchFailed(t)
            }
        )

    fun getArticle(articleUrl: String) = fortnightlyDao.getArticle(articleUrl)
    suspend fun deleteArticlesOlderThen(timestampInMillis: Long) {
        fortnightlyDao.deleteNewsArticlesOlderThen(timestampInMillis)
    }
}