package com.example.fortnightly.repo

import androidx.room.withTransaction
import app.cash.turbine.test
import com.example.fortnightly.R
import com.example.fortnightly.api.ApiService
import com.example.fortnightly.api.NewsArticleDto
import com.example.fortnightly.api.NewsResponse
import com.example.fortnightly.api.Source
import com.example.fortnightly.api.asDomainArticle
import com.example.fortnightly.data.ArticlesCategory
import com.example.fortnightly.data.FortnightlyArticlesDao
import com.example.fortnightly.data.FortnightlyArticlesDatabase
import com.example.fortnightly.data.NewsArticle
import com.example.fortnightly.util.MainDispatcherRule
import com.example.fortnightly.utils.Resource
import com.example.fortnightly.utils.TimeUtil
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.IOException
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class NewsRepositoryTest {

    private lateinit var repository: NewsRepository

    private lateinit var dao: FortnightlyArticlesDao

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @MockK
    lateinit var apiService: ApiService

    @MockK
    lateinit var database: FortnightlyArticlesDatabase

    @MockK
    lateinit var timeUtil: TimeUtil

    private val article = NewsArticle(
        "title",
        "url",
        "content",
        "description",
        "nbc",
        "general",
        "publishedAt",
        1,
        "urlToImage"
    )
    private val articles = listOf(article)

    private val generalCategoryArticle = ArticlesCategory(
        "url1",
        "general",
        1
    )

    private val newsResponse = NewsResponse(
        listOf(
            NewsArticleDto(
                "title",
                "url",
                "content",
                "description",
                Source(null, "nbc"),
                "category",
                "publishedAt",
                "urlToImage"
            )
        )
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        mockkStatic(
            "androidx.room.RoomDatabaseKt"
        )

        val transactionLambda = slot<suspend () -> R>()
        coEvery { database.withTransaction(capture(transactionLambda)) } coAnswers {
            transactionLambda.captured.invoke()
        }

        repository = NewsRepository(apiService, database, timeUtil)
        dao = database.fortnightlyArticlesDao()
    }

    @After
    fun cleanup() {
        database.close()
    }

    @Test
    fun `givenCachedArticlesExist_And_NetworkServiceReturnErrorWhenGetArticlesIsCalled_ThenErrorResourceReturnedWithCachedArticles`() =
        runTest {
            val forceRefresh = false
            val category = "general"

            every { dao.getArticlesCategory(any()) } answers { flow { emit(articles) } }

            // Returns true
            every { timeUtil.getCurrentSystemTime() } answers { TimeUnit.HOURS.toMillis(3) }

            coEvery { apiService.getTopHeadlines(any()) } throws IOException()

            repository.getArticles(category, forceRefresh, {}, {}).test {
                val result = awaitItem()
                assertThat(result).isInstanceOf(Resource.Error::class.java)
                assertThat(result.data).isEqualTo(articles)
                awaitComplete()
            }
        }

    @Test
    fun `givenCachedArticlesIsEmpty_And_NetworkServiceReturnErrorWhenGetArticlesIsCalled_ThenErrorResourceReturnedWithEmptyList`() =
        runTest {
            val forceRefresh = false
            val category = "general"

            every { dao.getArticlesCategory(any()) } answers { flow { emit(emptyList()) } }

            // Returns true
            every { timeUtil.getCurrentSystemTime() } answers { TimeUnit.HOURS.toMillis(3) }

            coEvery { apiService.getTopHeadlines(any()) } throws IOException()

            repository.getArticles(category, forceRefresh, {}, {}).test {
                val result = awaitItem()
                assertThat(result).isInstanceOf(Resource.Error::class.java)
                assertThat(result.data).isEmpty()
                awaitComplete()
            }
        }

    @Test
    fun `givenCachedArticlesExist_And_NetworkShouldNotFetchWhenGetArticlesIsCalled_ThenSuccessResourceReturnedWithCachedArticles`() =
        runTest {
            val forceRefresh = false
            val category = "general"

            every { dao.getArticlesCategory(any()) } answers { flow { emit(articles) } }

            // Returns false
            every { timeUtil.getCurrentSystemTime() } answers { article.updateAt }

            coEvery { apiService.getTopHeadlines(any()) } answers { newsResponse }

            repository.getArticles(category, forceRefresh, {}, {}).test {
                val result = awaitItem()
                assertThat(result).isInstanceOf(Resource.Success::class.java)
                assertThat(result.data).isEqualTo(articles)
                awaitComplete()
            }
        }

    @Test
    fun `givenCachedArticlesIsEmpty_And_NetworkServiceReturnDataWhenGetArticlesIsCalled_ThenSuccessResourceReturnedWithNetworkArticles`() =
        runTest {
            val forceRefresh = false
            val currentTime = 1L
            val category = "general"

            val updatedData = newsResponse.articles.map {
                it.asDomainArticle(
                    category,
                    currentTime
                )
            }

            val articlesCategory = updatedData.map { article ->
                ArticlesCategory(article.url, article.category)
            }

            // Keep fixed latest fetch timestamp for this test
            every { timeUtil.getCurrentSystemTime() } answers { currentTime }

            every {
                dao.getArticlesCategory(category)
            } answers { flow { emit(emptyList()) } } andThenAnswer { flow { emit(updatedData) } }

            coEvery { apiService.getTopHeadlines(category) } returns newsResponse

            repository.getArticles(category, forceRefresh, {}, {}).test {
                val result = awaitItem()
                assertThat(result).isInstanceOf(Resource.Success::class.java)
                assertThat(result.data).isEqualTo(updatedData)
                awaitComplete()
            }

            coVerify { dao.deleteArticlesCategory(category) }
            coVerify { dao.insertNewsArticles(updatedData) }
            coVerify { dao.insertArticlesCategory(articlesCategory) }
        }

    @Test
    fun `givenCachedArticlesExists_And_WhenForceRefreshIsCalledNetworkServiceReturnsData_ThenSuccessResourceReturnedWithNetworkArticles`() =
        runTest {
            val forceRefresh = true
            val currentTime = 1L
            val category = "general"

            val updatedData = newsResponse.articles.map {
                it.asDomainArticle(
                    category,
                    currentTime
                )
            }

            val articlesCategory = updatedData.map { article ->
                ArticlesCategory(article.url, article.category)
            }

            // Keep fixed latest fetch timestamp for this test
            every { timeUtil.getCurrentSystemTime() } answers { currentTime }

            every {
                dao.getArticlesCategory(category)
            } answers { flow { emit(articles) } } andThenAnswer { flow { emit(updatedData) } }

            coEvery { apiService.getTopHeadlines(category) } returns newsResponse

            repository.getArticles(category, forceRefresh, {}, {}).test {
                val result = awaitItem()
                assertThat(result).isInstanceOf(Resource.Success::class.java)
                assertThat(result.data).isEqualTo(updatedData)
                awaitComplete()
            }

            coVerify { dao.deleteArticlesCategory(category) }
            coVerify { dao.insertNewsArticles(updatedData) }
            coVerify { dao.insertArticlesCategory(articlesCategory) }
        }
}