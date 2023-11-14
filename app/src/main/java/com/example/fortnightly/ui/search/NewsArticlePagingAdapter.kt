package com.example.fortnightly.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import com.example.fortnightly.data.NewsArticle
import com.example.fortnightly.databinding.NewsArticleListItemBinding
import com.example.fortnightly.ui.shared.NewsArticleAdapter
import com.example.fortnightly.ui.shared.NewsArticleDiffCallback
import com.example.fortnightly.ui.shared.NewsArticleViewHolder

class NewsArticlePagingAdapter(private val listener: NewsArticleAdapter.ArticleAdapterListener) :
    PagingDataAdapter<NewsArticle, NewsArticleViewHolder>(NewsArticleDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsArticleViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return NewsArticleViewHolder(
            NewsArticleListItemBinding.inflate(
                inflater, parent, false
            ),
            listener
        )
    }

    override fun onBindViewHolder(holder: NewsArticleViewHolder, position: Int) {
        val newsArticle = getItem(position)
        newsArticle?.let { holder.bind(newsArticle) }
    }
}