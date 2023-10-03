package com.example.fortnightly.di

import android.content.Context
import androidx.room.Room
import com.example.fortnightly.data.FortnightlyArticlesDao
import com.example.fortnightly.data.FortnightlyArticlesDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): FortnightlyArticlesDatabase =
        Room.databaseBuilder(
            appContext,
            FortnightlyArticlesDatabase::class.java,
            "fortnightly_articles.db"
        )
            .fallbackToDestructiveMigration()
            .build()

    fun provideFortnightlyArticlesDao(database: FortnightlyArticlesDatabase): FortnightlyArticlesDao =
        database.fortnightlyArticlesDao()
}