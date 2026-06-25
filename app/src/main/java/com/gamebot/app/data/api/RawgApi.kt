package com.gamebot.app.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class RawgGame(
    val id: Int,
    val name: String,
    val background_image: String?,
    val genres: List<RawgGenre>?,
    val developers: List<RawgDeveloper>?
)

data class RawgGenre(val name: String)
data class RawgDeveloper(val name: String)

data class RawgSearchResponse(
    val results: List<RawgGame>
)

interface RawgApiService {
    @GET("games")
    suspend fun searchGames(
        @Query("key") apiKey: String,
        @Query("search") query: String,
        @Query("page_size") pageSize: Int = 5
    ): RawgSearchResponse
}

object RawgApi {
    private const val BASE_URL = "https://api.rawg.io/api/"
    const val API_KEY = "c98b9e1aaf124fae8edaab9d3c143f67"

    val service: RawgApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RawgApiService::class.java)
    }
}