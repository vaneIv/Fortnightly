package com.example.fortnightly.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import app.cash.turbine.test
import com.example.fortnightly.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class FortnightlyArticleDaoTest {

    private lateinit var fortnightlyArticleDatabase: FortnightlyArticlesDatabase
    private lateinit var fortnightlyArticlesDao: FortnightlyArticlesDao

    private val articleSports = NewsArticle(
        "title",
        "url1",
        "content",
        "description",
        "nbc",
        "sports",
        "publishedAt",
        3,
        "urlToImage",
    )
    private val articleBusiness = NewsArticle(
        "title",
        "url2",
        "content",
        "description",
        "nbc",
        "business",
        "publishedAt",
        1,
        "urlToImage"
    )
    private val articleScience = NewsArticle(
        "title",
        "url3",
        "content",
        "description",
        "nbc",
        "science",
        "publishedAt",
        1,
        "urlToImage"
    )
    private val articleGeneral = NewsArticle(
        "title",
        "url4",
        "content",
        "description",
        "nbc",
        "general",
        "publishedAt",
        3,
        "urlToImage"
    )
    private val articleTechnology = NewsArticle(
        "title",
        "url5",
        "content",
        "description",
        "nbc",
        "technology",
        "publishedAt",
        1,
        "urlToImage"
    )
    private val articleGeneral2 = NewsArticle(
        "title",
        "url6",
        "content",
        "description",
        "nbc",
        "general",
        "publishedAt",
        1,
        "urlToImage"
    )

    private val articles = listOf(
        articleSports,
        articleBusiness,
        articleScience,
        articleGeneral,
        articleTechnology,
        articleGeneral2
    )

    private val articlesCategorySports = ArticlesCategory("url1", "sports", 1)
    private val articlesCategoryBusiness = ArticlesCategory("url2", "business", 2)
    private val articlesCategoryScience = ArticlesCategory("url3", "science", 3)
    private val articlesCategoryGeneral = ArticlesCategory("url4", "general", 4)
    private val articlesCategoryTechnology = ArticlesCategory("url5", "technology", 5)
    private val articlesCategoryGeneral2 = ArticlesCategory("url6", "general", 6)

    private val articlesCategory = listOf(
        articlesCategorySports,
        articlesCategoryBusiness,
        articlesCategoryScience,
        articlesCategoryGeneral,
        articlesCategoryTechnology,
        articlesCategoryGeneral2
    )

    @get: Rule
    val dispatcherRule = MainDispatcherRule()

    @Before
    fun setup() {
        fortnightlyArticleDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FortnightlyArticlesDatabase::class.java
        ).build()

        fortnightlyArticlesDao = fortnightlyArticleDatabase.fortnightlyArticlesDao()
    }

    @After
    fun cleanup() {
        fortnightlyArticleDatabase.close()
    }

    @Test
    fun `testInsertAndGetCategoryArticles`() = runTest {
        fortnightlyArticlesDao.insertArticlesCategory(articlesCategory)
        fortnightlyArticlesDao.insertNewsArticles(articles)

        fortnightlyArticlesDao.getArticlesCategory("sports").test {
            val result = awaitItem()
            assertThat(result).hasSize(1)
            assertThat(result).contains(articleSports)
            cancel()
        }

        fortnightlyArticlesDao.getArticlesCategory("business").test {
            val result = awaitItem()
            assertThat(result).hasSize(1)
            assertThat(result).contains(articleBusiness)
            cancel()
        }

        fortnightlyArticlesDao.getArticlesCategory("general").test {
            val result = awaitItem()
            assertThat(result).hasSize(2)
            assertThat(result).containsExactly(articleGeneral, articleGeneral2)
            cancel()
        }
    }

    @Test
    fun `testDeleteCategoryArticles`() = runTest {
        fortnightlyArticlesDao.insertArticlesCategory(articlesCategory)
        fortnightlyArticlesDao.insertNewsArticles(articles)

        fortnightlyArticlesDao.getArticlesCategory("sports").test {
            val result = awaitItem()
            assertThat(result).hasSize(1)
            assertThat(result).contains(articleSports)
            cancel()
        }

        fortnightlyArticlesDao.deleteArticlesCategory("sports")

        fortnightlyArticlesDao.getArticlesCategory("sports").test {
            val result = awaitItem()
            assertThat(result).isEmpty()
            cancel()
        }
    }

    @Test
    fun `testDeleteNewsArticlesOlderThen`() = runTest {
        fortnightlyArticlesDao.insertNewsArticles(articles)

        fortnightlyArticlesDao.getNewsArticles().test {
            val result = awaitItem()
            assertThat(result).containsExactly(
                articleSports,
                articleBusiness,
                articleScience,
                articleGeneral,
                articleTechnology,
                articleGeneral2
            )
            cancel()
        }

        fortnightlyArticlesDao.deleteNewsArticlesOlderThen(2)

        fortnightlyArticlesDao.getNewsArticles().test {
            val result = awaitItem()
            assertThat(result).hasSize(2)
            assertThat(result).containsExactly(articleSports, articleGeneral)
            cancel()
        }
    }
}