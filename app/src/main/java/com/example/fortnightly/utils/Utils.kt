package com.example.fortnightly.utils

import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.fortnightly.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough

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

inline fun <T : View> T.showIfOrInvisible(condition: (T) -> Boolean) {
    if (condition(this)) {
        this.visibility = View.VISIBLE
    } else {
        this.visibility = View.INVISIBLE
    }
}

inline fun SearchView.onQueryTextSubmit(crossinline listener: (String) -> Unit) {
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            if (!query.isNullOrBlank()) {
                listener(query)
            }

            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            return true
        }
    })
}

//fun Fragment.postponeEnterTransitionWithDelay(delay: Long, timeUnit: TimeUnit) {
//    postponeEnterTransition(delay, timeUnit)
//    view?.doOnPreDraw { startPostponedEnterTransition() }
//}

fun Fragment.postponeAndStartEnterTransition(view: View) {
    postponeEnterTransition()
    view.doOnPreDraw { startPostponedEnterTransition() }
}

fun Fragment.setMaterialFadeThroughTransition() {
    enterTransition = MaterialFadeThrough().apply {
        duration = resources.getInteger(R.integer.motion_duration_large).toLong()
    }
}

fun Fragment.setMaterialElevationTransitions(durationResId: Int) {
    exitTransition = createMaterialElevationScale(false, durationResId)
    reenterTransition = createMaterialElevationScale(true, durationResId)
}

private fun Fragment.createMaterialElevationScale(
    enter: Boolean,
    durationResId: Int
): MaterialElevationScale {
    return MaterialElevationScale(enter).apply {
        duration = resources.getInteger(durationResId).toLong()
    }
}