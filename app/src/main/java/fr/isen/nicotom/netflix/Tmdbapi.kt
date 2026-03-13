package fr.isen.nicotom.netflix

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


data class TmdbSearchResponse(
    @SerializedName("results") val results: List<TmdbFilm> = emptyList()
)

data class TmdbFilm(
    @SerializedName("id")           val id: Int = 0,
    @SerializedName("title")        val title: String = "",
    @SerializedName("overview")     val overview: String = "",
    @SerializedName("poster_path")  val posterPath: String? = null
) {
    fun posterUrl(): String? {
        return posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
    }
}

interface TmdbApiService {

    @GET("search/movie")
    suspend fun searchMovie(
        @Query("api_key")  apiKey: String,
        @Query("query")    query: String,
        @Query("language") language: String = "fr-FR"
    ): TmdbSearchResponse
}


object TmdbApi {
    const val API_KEY = "e6083c01505e4d11d27fe57611ac5432"

    val service: TmdbApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbApiService::class.java)
    }
}