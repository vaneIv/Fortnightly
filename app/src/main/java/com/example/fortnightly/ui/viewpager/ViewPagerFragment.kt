package com.example.fortnightly.ui.viewpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.fortnightly.R
import com.example.fortnightly.databinding.FragmentViewPagerBinding
import com.example.fortnightly.utils.onQueryTextSubmit
import com.example.fortnightly.utils.postponeAndStartEnterTransition
import com.example.fortnightly.utils.setMaterialElevationTransitions
import com.example.fortnightly.utils.setMaterialFadeThroughTransition
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewPagerFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setMaterialFadeThroughTransition()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentViewPagerBinding.inflate(inflater, container, false)

        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager

        viewPager.adapter = NewsCategoriesPagerAdapter(this)

        // Set the text for each tab
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()

        binding.toolbar.inflateMenu(R.menu.menu_search_article)

        val searchItem = binding.toolbar.menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.onQueryTextSubmit { query ->

            findNavController().navigate(
                ViewPagerFragmentDirections.navigatePagerFragmentToSearchArticlesFragment(query)
            )

            //searchView.clearFocus()

            searchItem.collapseActionView()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postponeAndStartEnterTransition(view)

        setMaterialElevationTransitions(R.integer.motion_duration_large)
    }

    private fun getTabTitle(position: Int): String? {
        return when (position) {
            GENERAL_NEWS_PAGE_INDEX -> getString(R.string.category_breaking_news_label)
            BUSINESS_NEWS_PAGE_INDEX -> getString(R.string.category_business_label)
            SPORTS_NEWS_PAGE_INDEX -> getString(R.string.category_sports_label)
            SCIENCE_NEWS_PAGE_INDEX -> getString(R.string.category_science_label)
            else -> null
        }
    }
}
