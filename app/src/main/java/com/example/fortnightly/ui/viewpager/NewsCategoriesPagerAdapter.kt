package com.example.fortnightly.ui.viewpager

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.fortnightly.ui.categories.BusinessNewsFragment
import com.example.fortnightly.ui.categories.GeneralNewsFragment
import com.example.fortnightly.ui.categories.ScienceNewsFragment
import com.example.fortnightly.ui.categories.SportsNewsFragment


const val GENERAL_NEWS_PAGE_INDEX = 0
const val BUSINESS_NEWS_PAGE_INDEX = 1
const val SPORTS_NEWS_PAGE_INDEX = 2
const val SCIENCE_NEWS_PAGE_INDEX = 3

class NewsCategoriesPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int) =
        tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()

    /**
     * Mapping of the ViewPager page index to their respective fragments
     */
    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        GENERAL_NEWS_PAGE_INDEX to { GeneralNewsFragment() },
        BUSINESS_NEWS_PAGE_INDEX to { BusinessNewsFragment() },
        SPORTS_NEWS_PAGE_INDEX to { SportsNewsFragment() },
        SCIENCE_NEWS_PAGE_INDEX to { ScienceNewsFragment() }
    )
}