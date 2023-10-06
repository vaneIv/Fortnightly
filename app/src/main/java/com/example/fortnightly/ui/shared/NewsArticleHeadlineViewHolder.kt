package com.example.fortnightly.ui.shared

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fortnightly.R
import com.example.fortnightly.data.NewsArticle
import com.example.fortnightly.databinding.NewsArticleHeadlineListItemBinding

class NewsArticleHeadlineViewHolder(
    private val binding: NewsArticleHeadlineListItemBinding,
    private val onItemClick: (Int) -> Unit
) : BaseViewHolder<NewsArticle>(binding.root) {

    override fun bind(item: NewsArticle) {
        binding.apply {
            Glide.with(itemView)
                .load(item.urlToImage)
                .error(R.drawable.image_placeholder)
                .into(imageViewArticle)
        }
    }

    init {
        binding.root.setOnClickListener {
            val position = absoluteAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onItemClick(position)
            }
        }
    }
}