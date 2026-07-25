package com.example.nextstoprealtime.network

import com.example.nextstoprealtime.model.StopSearchResponse
import com.example.nextstoprealtime.model.TimesResponse
import com.example.nextstoprealtime.model.VehicleJourneyResponse
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface BustimesApi {

    @GET("api/stops/")
    suspend fun searchStops(
        @Query("search") query: String,
        @Query("limit") limit: Int = 20
    ): StopSearchResponse

    @GET("stops/{atco}/times.json")
    suspend fun getStopTimes(
        @Path("atco") atcoCode: String,
        @Query("limit") limit: Int = 5
    ): TimesResponse

    // Optional: lookup vehicle journeys by trip for AVL vehicle allocation
    @GET("api/vehiclejourneys/")
    suspend fun getVehicleJourneys(
        @Query("trip") tripId: Long,
        @Query("limit") limit: Int = 5
    ): VehicleJourneyResponse

    companion object {
        private const val BASE_URL = "https://bustimes.org/"

        fun create(): BustimesApi {
            val json = Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            }

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("User-Agent", "NextStopRealtime/1.0 (Android; educational app using public bustimes.org data)")
                        .header("Accept", "application/json")
                        .build()
                    chain.proceed(request)
                }
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(BustimesApi::class.java)
        }
    }
}
