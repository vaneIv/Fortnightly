package com.example.fortnightly.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        NewsArticle::class,
        ArticlesCategory::class,
        SearchResult::class,
        SearchQueryRemoteKey::class
    ],
    version = 1
)
abstract class FortnightlyArticlesDatabase : RoomDatabase() {

    abstract fun fortnightlyArticlesDao(): FortnightlyArticlesDao

    abstract fun searchQueryRemoteKeyDao(): SearchQueryRemoteDao
}