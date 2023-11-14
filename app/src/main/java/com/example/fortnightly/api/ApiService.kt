package com.example.fortnightly.api

import com.example.fortnightly.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiService {

    companion object {
        const val BASE_URL = "https://newsapi.org/v2/"
        const val API_KEY = BuildConfig.NEWS_API_KEY
    }

    @Headers("X-Api-Key: $API_KEY")
    @GET("top-headlines?country=us&pageSize=100")
    suspend fun getTopHeadlines(@Query("category") category: String): NewsResponse

    @Headers("X-Api-Key: $API_KEY")
    @GET("everything")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): NewsResponse
}