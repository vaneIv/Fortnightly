package com.example.fortnightly.utils

import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.fortnightly.R
import com.example.fortnightly.data.NewsArticle
import com.example.fortnightly.ui.viewpager.ViewPagerFragmentDirections
import com.google.android.material.snackbar.Snackbar

val <T> T.exhaustive: T
    get() = this

fun Fragment.showSnackbar(
    message: String,
    duration: Int = Snackbar.LENGTH_LONG,
    view: View = requireView()
) {
    Snackbar.make(view, message, duration).show()
}

fun RecyclerView.addDividerDecoration(
    drawableResourceId: Int,
    orientation: Int = RecyclerView.VERTICAL
) {
    val dividerItemDecoration = DividerItemDecoration(context, orientation)
    ResourcesCompat.getDrawable(resources, drawableResourceId, null)?.let {
        dividerItemDecoration.setDrawable(it)
        addItemDecoration(dividerItemDecoration)
    }
}

fun NavController.navigateToDetailsFragment(newsArticle: NewsArticle) {
    if (currentDestination?.id != R.id.viewPagerFragment) return
    navigate(ViewPagerFragmentDirections.navigatePagerFragmentToDetailsDetailsFragment(newsArticle))
}