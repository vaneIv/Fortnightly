package com.example.fortnightly.ui.shared

import androidx.recyclerview.widget.DiffUtil
import com.example.fortnightly.data.NewsArticle

object NewsArticleDiffCallback : DiffUtil.ItemCallback<NewsArticle>() {
    override fun areItemsTheSame(oldItem: NewsArticle, newItem: NewsArticle): Boolean {
        return oldItem.url == newItem.url
    }

    override fun areContentsTheSame(oldItem: NewsArticle, newItem: NewsArticle): Boolean {
        return oldItem == newItem
    }
}