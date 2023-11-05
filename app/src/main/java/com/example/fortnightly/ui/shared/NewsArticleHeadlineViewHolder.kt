package com.example.fortnightly.ui.shared

import com.example.fortnightly.data.NewsArticle
import com.example.fortnightly.databinding.NewsArticleHeadlineListItemBinding

class NewsArticleHeadlineViewHolder(
    private val binding: NewsArticleHeadlineListItemBinding,
    listener: NewsArticleAdapter.ArticleAdapterListener
) : BaseViewHolder<NewsArticle>(binding.root) {

    override fun bind(item: NewsArticle) {
        binding.article = item
    }

    init {
        binding.apply {
            this.listener = listener
        }
    }
}