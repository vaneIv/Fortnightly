package com.example.fortnightly.di

import com.example.fortnightly.utils.TimeUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilsModule {

    @Provides
    @Singleton
    fun provideTimeUtil(): TimeUtil =
        TimeUtil()
}