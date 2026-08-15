package com.example.data.network

import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    val results: List<GeocodingResult>? = null
)

@JsonClass(generateAdapter = true)
data class GeocodingResult(
    val id: Long = 0L,
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val country: String? = null,
    val admin1: String? = null
)

@JsonClass(generateAdapter = true)
data class WeatherResponse(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timezone: String = "Asia/Dhaka",
    val current: CurrentWeather = CurrentWeather(),
    val hourly: HourlyWeather = HourlyWeather(),
    val daily: DailyWeather = DailyWeather()
)

@JsonClass(generateAdapter = true)
data class CurrentWeather(
    val time: String = "",
    val temperature_2m: Double = 28.0,
    val relative_humidity_2m: Int = 60,
    val apparent_temperature: Double = 30.0,
    val is_day: Int = 1,
    val precipitation: Double = 0.0,
    val weather_code: Int = 1,
    val wind_speed_10m: Double = 10.0,
    val wind_direction_10m: Int = 180
)

@JsonClass(generateAdapter = true)
data class HourlyWeather(
    val time: List<String> = emptyList(),
    val temperature_2m: List<Double> = emptyList(),
    val weather_code: List<Int> = emptyList(),
    val precipitation_probability: List<Int> = emptyList()
)

@JsonClass(generateAdapter = true)
data class DailyWeather(
    val time: List<String> = emptyList(),
    val weather_code: List<Int> = emptyList(),
    val temperature_2m_max: List<Double> = emptyList(),
    val temperature_2m_min: List<Double> = emptyList(),
    val sunrise: List<String> = emptyList(),
    val sunset: List<String> = emptyList(),
    val precipitation_sum: List<Double> = emptyList(),
    val precipitation_probability_max: List<Int> = emptyList()
)

interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,wind_speed_10m,wind_direction_10m",
        @Query("hourly") hourly: String = "temperature_2m,weather_code,precipitation_probability",
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,precipitation_sum,precipitation_probability_max",
        @Query("timezone") timezone: String = "auto"
    ): WeatherResponse
}

interface GeocodingApiService {
    @GET("v1/search")
    suspend fun searchLocation(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): GeocodingResponse
}

object WeatherApiClient {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "ToolsMate-AndroidApp/1.0")
                .build()
            chain.proceed(request)
        }
        .build()

    private val moshi = com.squareup.moshi.Moshi.Builder()
        .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
        .build()

    val weatherApi: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(WeatherApiService::class.java)
    }

    val geocodingApi: GeocodingApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeocodingApiService::class.java)
    }
}
