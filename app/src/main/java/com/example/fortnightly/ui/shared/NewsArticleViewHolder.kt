package com.example.fortnightly.ui.shared

import com.example.fortnightly.data.NewsArticle
import com.example.fortnightly.databinding.NewsArticleListItemBinding

class NewsArticleViewHolder(
    private val binding: NewsArticleListItemBinding,
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