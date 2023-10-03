package com.example.fortnightly.ui.viewpager

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.fortnightly.R
import com.example.fortnightly.databinding.FragmentViewPagerBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewPagerFragment : Fragment(R.layout.fragment_view_pager) {

    private var _binding: FragmentViewPagerBinding? = null

    private val binding
        get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentViewPagerBinding.bind(view)

        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager

        viewPager.adapter = NewsCategoriesPagerAdapter(this)

        // Set the text for each tab
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
