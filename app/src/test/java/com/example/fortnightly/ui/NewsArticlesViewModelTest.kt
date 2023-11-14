package com.example.fortnightly.ui

import app.cash.turbine.test
import com.example.fortnightly.data.NewsArticle
import com.example.fortnightly.repo.NewsRepository
import com.example.fortnightly.util.MainDispatcherRule
import com.example.fortnightly.utils.Resource
import com.example.fortnightly.utils.TimeUtil
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class NewsArticlesViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @MockK
    lateinit var repository: NewsRepository

    @MockK
    lateinit var timeUtil: TimeUtil

    lateinit var viewModel: NewsArticlesViewModel

    private val article = NewsArticle(
        "title",
        "url",
        "content",
        "description",
        "nbc",
        "category",
        "publishedAt",
        1,
        "urlToImage"
    )

    private val articles = listOf(article)

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = NewsArticlesViewModel(repository, timeUtil)
    }

    @Test
    fun `initShouldDeleteArticlesOlderThenTwoDays`() =
        runTest {
            val currentTimeInMillis = System.currentTimeMillis()
            val twoDaysAgo = currentTimeInMillis - TimeUnit.DAYS.toMillis(2)

            every { timeUtil.getCurrentSystemTime() } answers { currentTimeInMillis }

            coEvery { repository.deleteArticlesOlderThen(twoDaysAgo) } just runs

            // Create the ViewModel, which will trigger the init block
            NewsArticlesViewModel(repository, timeUtil)

            // Verify that deleteArticlesOlderThen was called with the correct timestamp
            coVerify { repository.deleteArticlesOlderThen(twoDaysAgo) }
        }

    @Test
    fun `callingOnStartShouldTriggerNormalRefresh_WhenResourceIsNotLoading`() =
        runTest {
            val category = "general"
            val currentTimeInMillis = System.currentTimeMillis()

            // Mock the timeUtil behavior
            every { timeUtil.getCurrentSystemTime() } answers { currentTimeInMillis }

            // Mock the behavior of the repository
            coEvery {
                repository.getArticles(any(), any(), any(), any())
            } answers { flow { emit(Resource.Success(articles)) } }

            val viewModel = NewsArticlesViewModel(repository, timeUtil)

            viewModel.onStart(category)

            // Mock the refreshTrigger
            viewModel.refreshTrigger.test {
                assertThat(awaitItem()).isEqualTo(NewsArticlesViewModel.Refresh.NORMAL)
            }
        }

    @Test
    fun `callingOnStartShouldNotTriggerNormalRefresh_whenResourceIsLoading`() =
        runTest {
            val category = "general"

            // Mock the timeUtil behavior
            every { timeUtil.getCurrentSystemTime() } answers { article.updateAt }

            // Mock the behavior of the repository
            coEvery {
                repository.getArticles(any(), any(), any(), any())
            } answers { flow { emit(Resource.Loading()) } }

            val viewModel = NewsArticlesViewModel(repository, timeUtil)

            viewModel.onStart(category)

            coVerify(exactly = 0) { repository.getArticles(category, false, any(), any()) }
        }

    @Test
    fun `callingOnManualRefreshShouldTriggerForceRefresh_whenResourceIsNotLoading`() =
        runTest {
            val category = "general"

            // Mock the behavior of repository
            coEvery {
                repository.getArticles(any(), any(), any(), any())
            } answers { flow { emit(Resource.Success(articles)) } }

            // Call the tested method
            viewModel.onManualRefresh(category)

            // Assert that refreshTrigger sends the expected value
            viewModel.refreshTrigger.test {
                assertThat(awaitItem()).isEqualTo(NewsArticlesViewModel.Refresh.FORCE)
            }
        }
}