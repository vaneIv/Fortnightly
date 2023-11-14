package com.example.fortnightly.ui.categories

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.example.fortnightly.R
import com.example.fortnightly.data.NewsArticle
import com.example.fortnightly.databinding.FragmentBusinessNewsBinding
import com.example.fortnightly.ui.NewsArticlesViewModel
import com.example.fortnightly.ui.shared.NewsArticleAdapter
import com.example.fortnightly.ui.viewpager.ViewPagerFragmentDirections
import com.example.fortnightly.utils.Resource
import com.example.fortnightly.utils.addDividerDecoration
import com.example.fortnightly.utils.exhaustive
import com.example.fortnightly.utils.setMaterialElevationTransitions
import com.example.fortnightly.utils.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BusinessNewsFragment : Fragment(R.layout.fragment_business_news),
    NewsArticleAdapter.ArticleAdapterListener {

    private val viewModel: NewsArticlesViewModel by viewModels()

    private val newsArticleAdapter = NewsArticleAdapter(this)

    private val category = "business"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentBusinessNewsBinding.bind(view)

        binding.apply {
            recyclerViewBusinessNews.apply {
                adapter = newsArticleAdapter
                setHasFixedSize(true)
                addDividerDecoration(R.drawable.divider_horizontal)
            }

            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.categoryArticles[category]?.collect {
                        val result = it ?: return@collect

                        swipeRefreshLayoutBusinessNews.isRefreshing = result is Resource.Loading
                        recyclerViewBusinessNews.isVisible = !result.data.isNullOrEmpty()
                        textViewError.isVisible =
                            result.error != null && result.data.isNullOrEmpty()
                        textViewError.text = getString(
                            R.string.could_not_refresh,
                            result.error?.localizedMessage
                                ?: getString(R.string.unknown_error_occurred)
                        )
                        buttonRetry.isVisible = result.error != null && result.data.isNullOrEmpty()

                        newsArticleAdapter.submitList(result.data) {
                            if (viewModel.pendingScrollToTopAfterRefresh) {
                                recyclerViewBusinessNews.scrollToPosition(0)
                                viewModel.pendingScrollToTopAfterRefresh = false
                            }
                        }
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.events.collect { event ->
                        when (event) {
                            is NewsArticlesViewModel.Event.ShowErrorMessage ->
                                showSnackbar(
                                    getString(
                                        R.string.could_not_refresh,
                                        event.error.localizedMessage
                                            ?: getString(R.string.unknown_error_occurred)
                                    )
                                )
                        }.exhaustive
                    }
                }
            }

            swipeRefreshLayoutBusinessNews.setOnRefreshListener {
                viewModel.onManualRefresh(category)
            }

            buttonRetry.setOnClickListener {
                viewModel.onManualRefresh(category)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.onStart(category)
    }

    override fun onArticleClicked(view: View, article: NewsArticle) {

        setMaterialElevationTransitions(R.integer.motion_duration_large)

        val articleDetailsTransitionName = getString(R.string.article_details_transition_name)
        val extras = FragmentNavigatorExtras(view to articleDetailsTransitionName)

        val directions =
            ViewPagerFragmentDirections.navigatePagerFragmentToDetailsFragment(article.url)
        findNavController().navigate(directions, extras)
    }
}