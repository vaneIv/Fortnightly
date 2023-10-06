package com.example.fortnightly.di

import com.example.fortnightly.api.ApiService
import com.example.fortnightly.data.FortnightlyArticlesDatabase
import com.example.fortnightly.repo.NewsRepository
import com.example.fortnightly.utils.TimeUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideNewsRepository(
        apiService: ApiService,
        fortnightlyDb: FortnightlyArticlesDatabase,
        timeUtil: TimeUtil
    ): NewsRepository = NewsRepository(apiService, fortnightlyDb, timeUtil)
}