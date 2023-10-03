package com.example.fortnightly.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles_category")
data class ArticlesCategory(
    val articleUrl: String,
    val articleCategory: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)
