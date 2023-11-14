package com.example.fortnightly.ui.search

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.example.fortnightly.R
import com.example.fortnightly.data.NewsArticle
import com.example.fortnightly.databinding.FragmentSearchArticlesBinding
import com.example.fortnightly.ui.shared.NewsArticleAdapter
import com.example.fortnightly.utils.addDividerDecoration
import com.example.fortnightly.utils.onQueryTextSubmit
import com.example.fortnightly.utils.postponeAndStartEnterTransition
import com.example.fortnightly.utils.setMaterialFadeThroughTransition
import com.example.fortnightly.utils.showIfOrInvisible
import com.example.fortnightly.utils.showSnackbar
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class SearchArticlesFragment : Fragment(R.layout.fragment_search_articles),
    NewsArticleAdapter.ArticleAdapterListener {

    private val viewModel: SearchNewsViewModel by viewModels()

    private lateinit var newsArticlePagingAdapter: NewsArticlePagingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setMaterialFadeThroughTransition()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postponeAndStartEnterTransition(view)

//        postponeEnterTransition(1000, TimeUnit.MILLISECONDS)
//        view.doOnPreDraw { startPostponedEnterTransition() }

        val binding = FragmentSearchArticlesBinding.bind(view)

        newsArticlePagingAdapter = NewsArticlePagingAdapter(this)

        binding.toolbar.inflateMenu(R.menu.menu_search_article)

        val searchItem = binding.toolbar.menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.onQueryTextSubmit { query ->
            viewModel.onSearchQuerySubmit(query)
            searchView.clearFocus()
        }

        binding.apply {
            recyclerViewSearchNews.apply {
                adapter = newsArticlePagingAdapter
                setHasFixedSize(true)
                addDividerDecoration(R.drawable.divider_horizontal)
            }

            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.searchResults.collectLatest { data ->
                        newsArticlePagingAdapter.submitData(data)
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.hasCurrentQuery.collect { hasCurrentQuery ->
                        swipeRefreshLayoutSearchNews.isEnabled = hasCurrentQuery

                        if (!hasCurrentQuery) {
                            recyclerViewSearchNews.isVisible = false
                        }
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    newsArticlePagingAdapter.loadStateFlow
                        .distinctUntilChangedBy { it.source.refresh }
                        .filter { it.source.refresh is LoadState.NotLoading }
                        .collect {
                            if (viewModel.pendingScrollToTopAfterNewQuery) {
                                recyclerViewSearchNews.scrollToPosition(0)
                                viewModel.pendingScrollToTopAfterNewQuery = false
                            }

                            if (viewModel.pendingScrollToTopAfterRefresh && it.mediator?.refresh is LoadState.NotLoading) {
                                recyclerViewSearchNews.scrollToPosition(0)
                                viewModel.pendingScrollToTopAfterRefresh = false
                            }
                        }
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    newsArticlePagingAdapter.loadStateFlow
                        .collect { loadState ->
                            when (val refresh = loadState.mediator?.refresh) {
                                is LoadState.Loading -> {
                                    textViewError.isVisible = false
                                    buttonRetry.isVisible = false
                                    swipeRefreshLayoutSearchNews.isRefreshing = true
                                    recyclerViewSearchNews.showIfOrInvisible {
                                        !viewModel.newQueryInProgress && newsArticlePagingAdapter.itemCount > 0
                                    }

                                    viewModel.refreshInProgress = true
                                    viewModel.pendingScrollToTopAfterRefresh = true
                                }

                                is LoadState.NotLoading -> {
                                    textViewError.isVisible = false
                                    buttonRetry.isVisible = false
                                    swipeRefreshLayoutSearchNews.isRefreshing = false
                                    recyclerViewSearchNews.isVisible =
                                        newsArticlePagingAdapter.itemCount > 0

                                    val noResults =
                                        newsArticlePagingAdapter.itemCount < 1 && loadState.append.endOfPaginationReached
                                                && loadState.source.append.endOfPaginationReached

                                    textViewNoResults.isVisible = noResults

                                    viewModel.refreshInProgress = false
                                    viewModel.newQueryInProgress = false
                                }

                                is LoadState.Error -> {
                                    swipeRefreshLayoutSearchNews.isRefreshing = false
                                    textViewNoResults.isVisible = false
                                    recyclerViewSearchNews.isVisible =
                                        newsArticlePagingAdapter.itemCount > 0

                                    val noCachedResults =
                                        newsArticlePagingAdapter.itemCount < 1 && loadState.source.append.endOfPaginationReached

                                    textViewError.isVisible = noCachedResults
                                    buttonRetry.isVisible = noCachedResults

                                    val errorMessage = getString(
                                        R.string.could_not_load_search_results,
                                        refresh.error.localizedMessage
                                            ?: getString(R.string.unknown_error_occurred)
                                    )
                                    textViewError.text = errorMessage

                                    if (viewModel.refreshInProgress) {
                                        showSnackbar(errorMessage)
                                    }
                                    viewModel.refreshInProgress = false
                                    viewModel.newQueryInProgress = false
                                    viewModel.pendingScrollToTopAfterRefresh = false
                                }

                                else -> null
                            }
                        }
                }
            }

            swipeRefreshLayoutSearchNews.setOnRefreshListener {
                newsArticlePagingAdapter.refresh()
            }

            buttonRetry.setOnClickListener {
                newsArticlePagingAdapter.retry()
            }

            toolbar.setNavigationOnClickListener {
                it.findNavController().navigateUp()
            }
        }
    }

    override fun onArticleClicked(view: View, article: NewsArticle) {

        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }

        val articleDetailsTransitionName = getString(R.string.article_details_transition_name)
        val extras = FragmentNavigatorExtras(view to articleDetailsTransitionName)

        val directions =
            SearchArticlesFragmentDirections.navigateSearchArticlesFragmentToArticleDetailsFragment(
                article.url
            )
        findNavController().navigate(directions, extras)
    }
}