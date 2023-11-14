package com.example.fortnightly.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.fortnightly.api.ApiService
import com.example.fortnightly.api.asDomainSearchArticle
import com.example.fortnightly.utils.TimeUtil
import retrofit2.HttpException
import java.io.IOException

private const val NEWS_STARTING_PAGE_INDEX = 1

@OptIn(ExperimentalPagingApi::class)
class SearchNewsRemoteMediator(
    private val searchQuery: String,
    private val apiService: ApiService,
    private val fortnightlyDb: FortnightlyArticlesDatabase,
    private val refreshOnInit: Boolean,
    private val timeUtil: TimeUtil
) : RemoteMediator<Int, NewsArticle>() {

    private val newsArticleDao = fortnightlyDb.fortnightlyArticlesDao()
    private val searchQueryRemoteKeyDao = fortnightlyDb.searchQueryRemoteKeyDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, NewsArticle>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> NEWS_STARTING_PAGE_INDEX
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> searchQueryRemoteKeyDao.getRemoteKey(searchQuery).nextPageKey
        }

        try {
            val response = apiService.searchNews(searchQuery, page, state.config.pageSize)
            val serverSearchResults = response.articles

            val searchResultArticles = serverSearchResults.map { newsArticleDto ->
                newsArticleDto.asDomainSearchArticle(timeUtil.getCurrentSystemTime())
            }

            fortnightlyDb.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    newsArticleDao.deleteSearchResultsForQuery(searchQuery)
                }

                val lastQueryPosition = newsArticleDao.getLastQueryPosition(searchQuery) ?: 0
                var queryPosition = lastQueryPosition + 1

                val searchResults = searchResultArticles.map { article ->
                    SearchResult(searchQuery, article.url, queryPosition++)
                }

                val nextPageKey = page + 1

                newsArticleDao.insertNewsArticles(searchResultArticles)
                newsArticleDao.insertSearchResults(searchResults)
                searchQueryRemoteKeyDao.insertRemoteKey(
                    SearchQueryRemoteKey(searchQuery, nextPageKey)
                )
            }
            return MediatorResult.Success(endOfPaginationReached = serverSearchResults.isEmpty())
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH

//        return if (refreshOnInit) {
//            InitializeAction.LAUNCH_INITIAL_REFRESH
//        } else {
//            InitializeAction.SKIP_INITIAL_REFRESH
//        }
    }

}